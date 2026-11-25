package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * El rol del mod, parte del servidor (lo que manda: vale igual en un servidor dedicado).
 * <ul>
 * <li><b>Ecos de la Sombra</b>: la moneda. Se ganan al matar enemigos (todos los que le pegaron se los llevan enteros) y
 * jefes. Al morir se conserva todo el equipo y los ecos se quedan en una <b>espada de luz</b> clavada donde se murio (o
 * en el ultimo suelo firme si fue una caida, la lava o el vacio); junto a ella, la tecla del inventario los recoge. Si se
 * muere otra vez antes, se pierden.</li>
 * <li><b>Estamina</b>: atacar, cubrirse de un golpe y (solo en combate) correr y saltar la gastan; se recupera sola al
 * poco de dejar de gastar. Sin estamina no se puede atacar, correr ni saltar hasta recuperar un poco, y un golpe en el
 * escudo rompe la guardia.</li>
 * <li><b>Sin hambre</b>: el nivel de comida va fijo (no se regenera vida sola ni se pasa hambre) y la comida ya no hace
 * nada: se cura con el frasco rojo ({@link Frascos}) y descansando en un santuario.</li>
 * <li><b>Atributos y nivel</b>: los ocho atributos de la {@link Ficha} dan la vida, el mana y la estamina maximos y lo
 * que pegan las armas ({@link #aplicar}); se suben en un santuario pagando ecos ({@link #subir}).</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Rol {
    private Rol() {}

    // ------------------------------------------------------------------ numeros

    public static final float EST_MAX = 100f;
    static final float GASTO_ATAQUE = 8f, GASTO_ATAQUE_PESADO = 13f, GASTO_SALTO = 7f, GASTO_CORRER = 0.36f;
    static final float RECUPERA = 2.2f, RECUPERA_COMBATE = 1.5f, RECUPERA_GUARDIA = 0.45f;
    /** Ticks sin recuperar tras gastar, y tras quedarse sin nada. */
    static final int PAUSA = 16, PAUSA_AGOTADO = 34;
    /** Estamina que hay que recuperar para dejar de estar agotado. */
    static final float VUELVE = 22f;
    /** Ticks que dura "en combate" desde el ultimo golpe dado o recibido. */
    static final int COMBATE = 160;
    /** Nivel de comida fijo: con 10 ni se regenera vida ni se pasa hambre; con 6 el juego no deja correr. */
    static final int COMIDA = 10, COMIDA_AGOTADO = 6;

    public static final int ECOS_REY = 9000, ECOS_GAVRAN = 14000;

    // ------------------------------------------------------------------ lo que no se guarda (por sesion)

    static final class Estado {
        float estamina = EST_MAX;
        int pausa, combate;
        boolean agotado;
        int enviadaDec = -1, enviadaTick;
        boolean enviadoAgotado, enviadoCombate;
        /** Hay que mandar el estado entero (ecos, espada) al cliente. */
        boolean sucio = true;
        /** Ecos ganados desde el ultimo envio (para el "+120" del contador). */
        int ganados;
        Vec3 seguro;
        ResourceKey<Level> seguroDim;
        String zona = "";
        /** Lo que dan los atributos (se recalcula en {@link #aplicar}). */
        float estMax = EST_MAX;
        int manaMax = 100;
        /** Hay que mandar la ficha (atributos, frascos) al cliente. */
        boolean fichaSucia = true;
        int avisoComida = -1000;
    }

    private static final Map<UUID, Estado> ESTADOS = new HashMap<>();
    /** Quien le ha pegado a cada enemigo (para repartir sus ecos). */
    private static final Map<LivingEntity, Set<UUID>> GOLPES = new WeakHashMap<>();

    static Estado de(Player p) {
        return ESTADOS.computeIfAbsent(p.getUUID(), k -> new Estado());
    }

    /** Solo para la prueba de la CI: sin gasto de estamina (sus peleas con los jefes van con un guion de golpes seguidos). */
    public static boolean sinLimite;

    private static boolean libre(Player p) {
        return sinLimite || p.isCreative() || p.isSpectator();
    }

    public static float estamina(Player p) {
        return de(p).estamina;
    }

    public static float estaminaMax(Player p) {
        return de(p).estMax;
    }

    /** El mana maximo del jugador (segun su Mente). */
    public static int manaMax(Player p) {
        return de(p).manaMax;
    }

    // ------------------------------------------------------------------ lo que dan los atributos

    private static final UUID MOD_VIDA = UUID.fromString("5e0c1a2b-7d14-4c0a-9a51-0d4c6f1e2a01"),
            MOD_ATAQUE = UUID.fromString("5e0c1a2b-7d14-4c0a-9a51-0d4c6f1e2a02");

    /** Pone en el jugador lo que le dan sus atributos: vida maxima, mana y estamina maximos y lo que pegan sus armas. */
    public static void aplicar(ServerPlayer sp) {
        int[] a = Ficha.atributos(sp);
        Estado s = de(sp);
        s.estMax = Nivel.estamina(a[Atributo.AGUANTE.ordinal()]);
        s.manaMax = Math.round(Nivel.mana(a[Atributo.MENTE.ordinal()]) * (Talismanes.lleva(sp, Talisman.Clase.MENTE) ? 1.15f : 1f));
        double vida = Nivel.vida(a[Atributo.VIGOR.ordinal()]) * (Talismanes.lleva(sp, Talisman.Clase.VIGOR) ? 1.10 : 1.0);
        modificador(sp, Attributes.MAX_HEALTH, MOD_VIDA, "darkcraft_vigor", Math.round(vida) - 20.0,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
        // lo que pegan las armas ya no es un tanto por ciento para todas: cada arma escala con sus letras (combate/Escalado)
        modificador(sp, Attributes.ATTACK_DAMAGE, MOD_ATAQUE, "darkcraft_fuerza", 0.0,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (sp.getHealth() > sp.getMaxHealth()) sp.setHealth(sp.getMaxHealth());
        if (s.estamina > s.estMax) s.estamina = s.estMax;
        s.fichaSucia = true;
        s.enviadaDec = -1;
    }

    private static void modificador(ServerPlayer sp, net.minecraft.world.entity.ai.attributes.Attribute at, UUID id, String nombre, double v,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation op) {
        var inst = sp.getAttribute(at);
        if (inst == null) return;
        var ya = inst.getModifier(id);
        if (ya != null && Math.abs(ya.getAmount() - v) < 1e-6) return;
        inst.removeModifier(id);
        // se guarda con el jugador: al volver a entrar, la vida que tenia cabe en su vida maxima (si no, se le recortaria a 20)
        if (Math.abs(v) > 1e-6) inst.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, nombre, v, op));
    }

    /**
     * Subir de nivel en un santuario: {@code nuevos} son los atributos que quiere dejar el jugador. Se cobra en ecos lo que
     * cuesta cada nivel de mas; si no llega, no se hace nada.
     */
    public static boolean subir(ServerPlayer sp, int[] nuevos) {
        int[] a = Ficha.atributos(sp);
        if (nuevos == null || nuevos.length != a.length) return false;
        for (int i = 0; i < a.length; i++) if (nuevos[i] < a[i] || nuevos[i] > Atributo.TOPE) return false;
        int desde = Nivel.de(a), hasta = Nivel.de(nuevos);
        if (hasta <= desde) return false;
        if (!sp.isCreative() && !com.darkcraft.santuario.Santuarios.junto(sp)) return false;
        long coste = Nivel.coste(desde, hasta);
        if (coste > Ficha.ecos(sp)) return false;
        Ficha.ecos(sp, (int) (Ficha.ecos(sp) - coste));
        Ficha.atributos(sp, nuevos);
        float antes = sp.getMaxHealth();
        aplicar(sp);
        // la vida que se gana se da ya (si no, la barra creceria vacia)
        if (sp.getMaxHealth() > antes) sp.heal(sp.getMaxHealth() - antes);
        de(sp).sucio = true;
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), DarkCraftMod.SFX_NIVEL_SUBE.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
        if (sp.level() instanceof ServerLevel sl)
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0, sp.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        com.darkcraft.story.Logros.dar(sp, "subir_nivel");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} sube del nivel {} al {} por {} ecos", sp.getGameProfile().getName(), desde, hasta, coste);
        return true;
    }

    /** Cobra ecos (la forja, la tienda): si no llegan, no cobra nada y devuelve false. */
    public static boolean pagar(ServerPlayer sp, int n) {
        if (n <= 0) return true;
        int e = Ficha.ecos(sp);
        if (e < n) return false;
        Ficha.ecos(sp, e - n);
        de(sp).sucio = true;
        return true;
    }

    public static boolean enCombate(Player p) {
        return de(p).combate > 0;
    }

    /** Para la prueba de la CI: fuera del combate (como si hubieran pasado los 8 s sin pelear). */
    public static void sinCombate(ServerPlayer sp) {
        de(sp).combate = 0;
    }

    /** La estamina entera y fuera el agotamiento (al descansar en un santuario). */
    public static void recuperado(ServerPlayer sp) {
        Estado s = de(sp);
        s.estamina = s.estMax;
        s.agotado = false;
        s.pausa = 0;
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER || !(e.player instanceof ServerPlayer sp)) return;
        Estado s = de(sp);
        if (sp.isAlive()) {
            if (s.combate > 0) s.combate--;
            if (sp.tickCount % 20 == 3) acecho(sp, s);
            if (libre(sp)) {
                s.estamina = s.estMax;
                s.agotado = false;
                s.pausa = 0;
            } else {
                if (s.combate > 0 && sp.isSprinting() && !sp.isPassenger()) {
                    s.estamina -= GASTO_CORRER;
                    s.pausa = Math.max(s.pausa, 8);
                }
                if (s.pausa > 0) s.pausa--;
                else if (s.estamina < s.estMax)
                    s.estamina = Math.min(s.estMax, s.estamina + (sp.isBlocking() ? RECUPERA_GUARDIA : s.combate > 0 ? RECUPERA_COMBATE : RECUPERA)
                            * (Talismanes.lleva(sp, Talisman.Clase.ALIENTO) ? 1.25f : 1f));
                if (s.estamina <= 0f) {
                    s.estamina = 0f;
                    if (!s.agotado) {
                        s.agotado = true;
                        s.pausa = Math.max(s.pausa, PAUSA_AGOTADO);
                    }
                } else if (s.agotado && s.estamina >= VUELVE) s.agotado = false;
            }
            // sin hambre: la comida va fija (y hace de "puede correr": con 6 el juego no deja)
            FoodData f = sp.getFoodData();
            int nivel = s.agotado ? COMIDA_AGOTADO : COMIDA;
            if (f.getFoodLevel() != nivel) f.setFoodLevel(nivel);
            if (f.getSaturationLevel() != 0f) f.setSaturation(0f);
            f.setExhaustion(0f);
            // el ultimo suelo firme (alli se queda la espada si se muere cayendo, en la lava o en el vacio)
            if (sp.tickCount % 5 == 0 && sp.onGround() && !sp.isInLava() && !sp.isPassenger()
                    && sp.getY() > sp.level().getMinBuildHeight() + 1) {
                s.seguro = sp.position();
                s.seguroDim = sp.level().dimension();
            }
        }
        if (sp.tickCount % 10 == 4) Espadas.vista(sp);
        if (sp.tickCount % 40 == 27) com.darkcraft.santuario.Santuarios.tick(sp);
        enviar(sp, s);
    }

    /** Un enemigo que le tiene echado el ojo, o un jefe en plena pelea: eso tambien es combate. */
    private static void acecho(ServerPlayer sp, Estado s) {
        if (s.combate >= 60 || libre(sp)) return;
        if (com.darkcraft.rey.ReyEventos.luchaDe(sp) != null || com.darkcraft.gavran.GavranEventos.luchaDe(sp) != null) {
            s.combate = 60;
            return;
        }
        AABB caja = sp.getBoundingBox().inflate(16, 8, 16);
        if (!sp.level().getEntitiesOfClass(Mob.class, caja, m -> m.isAlive() && m.getTarget() == sp).isEmpty()) s.combate = 60;
    }

    private static void enviar(ServerPlayer sp, Estado s) {
        int dec = Math.round(s.estamina * 10f);
        if (s.agotado != s.enviadoAgotado || (dec != s.enviadaDec && sp.tickCount - s.enviadaTick >= 2) || sp.tickCount < s.enviadaTick) {
            Net.to(sp, new Net.Estamina(dec, Math.round(s.estMax * 10f), s.agotado));
            s.enviadaDec = dec;
            s.enviadoAgotado = s.agotado;
            s.enviadaTick = sp.tickCount;
        }
        boolean combate = s.combate > 0;
        if (s.sucio || combate != s.enviadoCombate) {
            Ficha.Espada e = Ficha.espada(sp);
            Net.to(sp, e == null ? new Net.RolEstado(Ficha.ecos(sp), s.ganados, combate, false, "", 0, 0, 0, 0)
                    : new Net.RolEstado(Ficha.ecos(sp), s.ganados, combate, true, e.dim().location().toString(), e.x(), e.y(), e.z(), e.ecos()));
            s.sucio = false;
            s.ganados = 0;
            s.enviadoCombate = combate;
        }
        if (s.fichaSucia) {
            Ficha.Frascos f = Ficha.frascos(sp);
            Net.to(sp, new Net.FichaRol(Ficha.atributos(sp), s.manaMax, f.vidaMax(), f.manaMax(), f.vida(), f.mana(), f.mejora(),
                    Talismanes.huecos(sp), Talismanes.nombres(sp), Memoria.nombres(sp)));
            s.fichaSucia = false;
        }
    }

    /** Algo de la ficha ha cambiado (los frascos, los atributos): se vuelve a mandar al cliente. */
    public static void fichaCambiada(Player p) {
        de(p).fichaSucia = true;
    }

    /**
     * El combate propio gasta estamina al empezar cada golpe o voltereta: si no queda (o esta agotado), no se puede
     * (devuelve false y el cliente lo nota). Sin limite (creativo, la prueba de la CI), siempre se puede.
     */
    public static boolean gastarCombate(ServerPlayer sp, float cuanto) {
        if (libre(sp)) return true;
        Estado s = de(sp);
        if (s.agotado || s.estamina <= 0f) return false;
        gastar(s, cuanto, PAUSA);
        s.enviadaDec = -1;
        return true;
    }

    /** Si el jugador gasta estamina (no en creativo ni en la prueba de la CI). */
    public static boolean gasta(Player p) {
        return !libre(p);
    }

    private static void gastar(Estado s, float cuanto, int pausa) {
        s.estamina -= cuanto;
        s.pausa = Math.max(s.pausa, pausa);
    }

    // ------------------------------------------------------------------ estamina: atacar, saltar y cubrirse

    @SubscribeEvent
    public static void onAtaque(AttackEntityEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        // con el frasco en la boca no se pega
        if (Frascos.bebiendo(sp)) {
            e.setCanceled(true);
            return;
        }
        if (libre(sp)) return;
        Estado s = de(sp);
        if (s.agotado || s.estamina <= 0f) {
            e.setCanceled(true);
            return;
        }
        gastar(s, com.darkcraft.armas.MandobleJuramento.es(sp.getMainHandItem()) ? GASTO_ATAQUE_PESADO : GASTO_ATAQUE, PAUSA);
    }

    @SubscribeEvent
    public static void onSalto(LivingEvent.LivingJumpEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || libre(sp)) return;
        Estado s = de(sp);
        if (s.combate > 0) gastar(s, GASTO_SALTO, PAUSA);
    }

    @SubscribeEvent
    public static void onEscudo(ShieldBlockEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) guardia(sp, e.getBlockedDamage());
    }

    /**
     * Ha parado un golpe (con el escudo o cruzando las espadas del Rey): cuesta estamina; si se queda sin nada, se le
     * rompe la guardia (baja el arma, no puede volver a cubrirse en unos segundos y se queda un momento lento).
     */
    public static void guardia(ServerPlayer sp, float golpe) {
        if (libre(sp)) return;
        Estado s = de(sp);
        s.combate = COMBATE;
        gastar(s, 6f + 2.2f * Math.max(0f, golpe), PAUSA + 6);
        if (s.estamina > 0f) return;
        ItemStack con = sp.getUseItem();
        if (!con.isEmpty()) sp.getCooldowns().addCooldown(con.getItem(), 70);
        sp.stopUsingItem();
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24, 2, false, false, false));
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), DarkCraftMod.SFX_GUARDIA_ROTA.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        Net.to(sp, new Net.Cartel(Net.Cartel.GUARDIA, "", 0));
    }

    // ------------------------------------------------------------------ sin hambre: la comida ya no hace nada

    /**
     * La comida corriente ya no se come (no curaria ni quitaria un hambre que no hay): se avisa de que ahora se cura con
     * el frasco. Lo que tiene efectos (una manzana dorada, una pocion) si se puede tomar. Vale en el cliente y en el
     * servidor (el cliente no llega a empezar el gesto).
     */
    @SubscribeEvent
    public static void onComer(LivingEntityUseItemEvent.Start e) {
        if (!(e.getEntity() instanceof Player p) || p.isCreative()) return;
        FoodProperties fp = e.getItem().getFoodProperties(p);
        if (fp == null || fp.canAlwaysEat() || !fp.getEffects().isEmpty()) return;
        e.setCanceled(true);
        if (p instanceof ServerPlayer sp) {
            Estado s = de(sp);
            if (sp.tickCount - s.avisoComida > 100 || sp.tickCount < s.avisoComida) {
                s.avisoComida = sp.tickCount;
                Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.aviso.comida", 0));
            }
        }
    }

    // ------------------------------------------------------------------ ecos: quien pega y quien muere

    @SubscribeEvent
    public static void onDano(LivingDamageEvent e) {
        LivingEntity v = e.getEntity();
        if (v.level().isClientSide() || e.getAmount() <= 0f) return;
        Entity a = e.getSource().getEntity();
        if (v instanceof ServerPlayer sp) {
            if (a instanceof LivingEntity && a != sp) de(sp).combate = COMBATE;
        } else if (a instanceof ServerPlayer sp && v instanceof Mob) {
            de(sp).combate = COMBATE;
            GOLPES.computeIfAbsent(v, k -> new HashSet<>()).add(sp.getUUID());
        }
    }

    /** Antes de que el juego suelte nada: el equipo se conserva siempre (lo pidio el usuario), este como este la regla. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMuerteAntes(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) conservar(sp.server);
    }

    private static void conservar(net.minecraft.server.MinecraftServer srv) {
        GameRules.BooleanValue r = srv.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY);
        if (!r.get()) r.set(true, srv);
    }

    @SubscribeEvent
    public static void onArranque(ServerStartedEvent e) {
        conservar(e.getServer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMuerte(LivingDeathEvent e) {
        LivingEntity v = e.getEntity();
        if (v.level().isClientSide() || e.isCanceled()) return;
        if (v instanceof ServerPlayer sp) {
            muerte(sp, e.getSource());
            return;
        }
        Set<UUID> quien = GOLPES.remove(v);
        int valor = valor(v);
        if (valor <= 0 || !(v.level() instanceof ServerLevel sl)) return;
        Set<UUID> todos = new HashSet<>();
        if (quien != null) todos.addAll(quien);
        if (e.getSource().getEntity() instanceof ServerPlayer k) todos.add(k.getUUID());
        if (v.getKillCredit() instanceof ServerPlayer k) todos.add(k.getUUID());
        for (UUID u : todos) {
            ServerPlayer p = sl.getServer().getPlayerList().getPlayer(u);
            if (p == null || p.level() != sl || p.distanceToSqr(v) > 96 * 96) continue;
            dar(p, Talismanes.lleva(p, Talisman.Clase.CODICIA) ? Math.round(valor * 1.2f) : valor, v.position().add(0, v.getBbHeight() * 0.5, 0));
        }
    }

    /** Lo que deja un enemigo: segun su vida y lo que pega. Los animales y demas, nada. */
    static int valor(LivingEntity e) {
        if (!(e instanceof Enemy)) return 0;
        // los jefes dan los suyos al acabar su muerte (Rol.jefe)
        if (e instanceof com.darkcraft.rey.Rey || e instanceof com.darkcraft.gavran.Gavran || e instanceof com.darkcraft.enemigos.Capitan) return 0;
        // ni los que llama un jefe (se deshacen con el)
        if (e instanceof com.darkcraft.enemigos.Enemigo en && en.esEfimero()) return 0;
        double vida = e.getMaxHealth();
        double golpe = e.getAttribute(Attributes.ATTACK_DAMAGE) != null ? e.getAttributeValue(Attributes.ATTACK_DAMAGE) : 2;
        double armadura = e.getArmorValue();
        return (int) Math.max(5, Math.round(vida * 2.2 + golpe * 7 + armadura * 3));
    }

    /** Da ecos al jugador; {@code desde}: de donde salen volando hacia el (o null). */
    public static void dar(ServerPlayer p, int n, Vec3 desde) {
        if (n <= 0) return;
        Ficha.ecos(p, Ficha.ecos(p) + n);
        Estado s = de(p);
        s.ganados += n;
        s.sucio = true;
        if (desde != null) Net.to(p, new Net.Ecos(desde.x, desde.y, desde.z, n));
    }

    /** Un jefe ha caido: sus ecos y el cartel grande con su nombre. */
    public static void jefe(ServerPlayer p, String claveNombre, int ecos) {
        if (Talismanes.lleva(p, Talisman.Clase.CODICIA)) ecos = Math.round(ecos * 1.2f);
        dar(p, ecos, null);
        Net.to(p, new Net.Cartel(Net.Cartel.JEFE, claveNombre, ecos));
    }

    /** Si el jugador ya ha estado en esa zona (la clave de su nombre). */
    public static boolean zonaVista(ServerPlayer p, String clave) {
        return Ficha.zonaVista(p, clave);
    }

    /** El sitio donde esta el jugador (lo dice el Espejo cada segundo): la primera vez que entra, el cartel de la zona. */
    public static void zona(ServerPlayer p, String clave) {
        Estado s = de(p);
        if (clave == null || clave.isEmpty() || clave.equals(s.zona)) return;
        s.zona = clave;
        if (Ficha.zonaNueva(p, clave)) Net.to(p, new Net.Cartel(Net.Cartel.ZONA, clave, 0));
    }

    // ------------------------------------------------------------------ morir: la espada de luz

    private static void muerte(ServerPlayer sp, DamageSource causa) {
        Estado s = de(sp);
        Ficha.Espada vieja = Ficha.espada(sp);
        int ecos = Ficha.ecos(sp);
        Espadas.quitar(sp);
        if (ecos > 0) {
            Vec3 p = dondeClavar(sp, causa, s);
            Ficha.espada(sp, new Ficha.Espada(sp.level().dimension(), p.x, p.y, p.z, ecos));
            Ficha.ecos(sp, 0);
        } else Ficha.espada(sp, null);
        s.estamina = s.estMax;
        s.agotado = false;
        s.pausa = s.combate = 0;
        s.sucio = true;
        // al morir, los enemigos caidos vuelven a sus puestos
        com.darkcraft.enemigos.Puestos.reaparecer(sp);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} ha muerto: {} ecos en la espada de luz{}", sp.getGameProfile().getName(), ecos,
                vieja != null ? " (se pierden los " + vieja.ecos() + " de la anterior)" : "");
    }

    /** Donde se clava la espada: en el suelo bajo el jugador, o en el ultimo suelo firme si murio cayendo, en lava o en el vacio. */
    private static Vec3 dondeClavar(ServerPlayer sp, DamageSource causa, Estado s) {
        ServerLevel lvl = sp.serverLevel();
        boolean caida = causa.is(DamageTypes.FALL) && sp.fallDistance > 24f;
        boolean vacio = causa.is(DamageTypes.FELL_OUT_OF_WORLD) || sp.getY() < lvl.getMinBuildHeight() + 3;
        boolean lava = sp.isInLava() || causa.is(DamageTypes.LAVA);
        if (!caida && !vacio && !lava) {
            Vec3 suelo = sueloBajo(lvl, sp.position(), 10);
            if (suelo != null) return suelo;
        }
        if (s.seguro != null && s.seguroDim == lvl.dimension()) {
            double dx = s.seguro.x - sp.getX(), dz = s.seguro.z - sp.getZ();
            if (vacio || lava || dx * dx + dz * dz < 96 * 96) return s.seguro;
        }
        Vec3 suelo = sueloBajo(lvl, sp.position(), 32);
        return suelo != null ? suelo : new Vec3(sp.getX(), Math.max(sp.getY(), lvl.getMinBuildHeight() + 2), sp.getZ());
    }

    /** El primer suelo firme debajo de un punto (hasta {@code max} bloques), o null si no hay o es lava. */
    static Vec3 sueloBajo(ServerLevel lvl, Vec3 p, int max) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int y0 = (int) Math.floor(p.y + 0.5);
        for (int y = y0; y >= y0 - max && y >= lvl.getMinBuildHeight(); y--) {
            m.set(Math.floor(p.x), y, Math.floor(p.z));
            BlockState st = lvl.getBlockState(m);
            if (st.getFluidState().is(Fluids.LAVA) || st.getFluidState().is(Fluids.FLOWING_LAVA)) return null;
            VoxelShape forma = st.getCollisionShape(lvl, m);
            if (forma.isEmpty()) continue;
            double arriba = y + forma.max(Direction.Axis.Y);
            if (arriba > p.y + 1.2) continue;
            return new Vec3(p.x, arriba, p.z);
        }
        return null;
    }

    /** Junto a su espada de luz, el jugador ha pulsado la tecla: recupera los ecos. */
    public static void recuperar(ServerPlayer sp) {
        Ficha.Espada e = Ficha.espada(sp);
        if (e == null || !sp.isAlive() || e.dim() != sp.level().dimension() || e.distancia2(sp) > 5.0 * 5.0) return;
        Ficha.espada(sp, null);
        Ficha.ecos(sp, Ficha.ecos(sp) + e.ecos());
        Espadas.recoger(sp, e);
        Estado s = de(sp);
        s.ganados += e.ecos();
        s.sucio = true;
        Net.to(sp, new Net.Cartel(Net.Cartel.ECOS, "", e.ecos()));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} recupera {} ecos de su espada de luz", sp.getGameProfile().getName(), e.ecos());
    }

    // ------------------------------------------------------------------ entrar, salir, reaparecer

    private static void refrescar(Player p) {
        Estado s = de(p);
        s.sucio = true;
        s.fichaSucia = true;
        s.enviadaDec = -1;
        s.zona = "";
        if (p instanceof ServerPlayer sp) {
            aplicar(sp);
            s.estamina = s.estMax;
            com.darkcraft.armas.Combos.ajustar(sp);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer) refrescar(e.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            refrescar(sp);
            // se reaparece con la vida entera (la vida maxima se acaba de volver a poner) y los frascos y el mana llenos
            if (!e.isEndConquered()) {
                sp.setHealth(sp.getMaxHealth());
                Frascos.rellenar(sp);
                com.darkcraft.armas.Combos.llenar(sp);
            }
        }
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            Espadas.quitar(sp);
            refrescar(sp);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Espadas.quitar(sp);
        ESTADOS.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        ESTADOS.clear();
        GOLPES.clear();
        Espadas.olvidar();
    }

    // ------------------------------------------------------------------ comandos (/darkcraft ecos ...)

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("ecos")
                .executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    Ficha.Espada e = Ficha.espada(p);
                    c.getSource().sendSuccess(() -> Component.translatable("darkcraft.cmd.ecos", Ficha.ecos(p),
                            e == null ? "-" : e.ecos() + " @ " + BlockPos.containing(e.x(), e.y(), e.z()).toShortString()), false);
                    return Ficha.ecos(p);
                })
                .then(Commands.literal("dar").then(Commands.argument("n", IntegerArgumentType.integer(1, Ficha.TOPE)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    dar(p, IntegerArgumentType.getInteger(c, "n"), null);
                    return Ficha.ecos(p);
                })))
                .then(Commands.literal("quitar").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    Ficha.ecos(p, 0);
                    Ficha.espada(p, null);
                    Espadas.quitar(p);
                    de(p).sucio = true;
                    return 1;
                }))
                .then(Commands.literal("cartel").then(Commands.argument("tipo", IntegerArgumentType.integer(1, 3)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    int t = IntegerArgumentType.getInteger(c, "tipo");
                    Net.to(p, new Net.Cartel(t, t == Net.Cartel.JEFE ? "entity.darkcraft.rey" : t == Net.Cartel.ZONA ? "darkcraft.mapa.titulo" : "", 1234));
                    return 1;
                })));
    }
    // ------------------------------------------------------------------ comandos (/darkcraft rol ...), para probar

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> comandoRol() {
        var atributo = Commands.literal("atributo");
        for (Atributo a : Atributo.values())
            atributo.then(Commands.literal(a.clave).then(Commands.argument("valor", IntegerArgumentType.integer(1, Atributo.TOPE)).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                int[] v = Ficha.atributos(p);
                v[a.ordinal()] = IntegerArgumentType.getInteger(c, "valor");
                Ficha.atributos(p, v);
                aplicar(p);
                return verNivel(c.getSource(), p);
            })));
        // talismanes y frutos: darlos (o, con "poner", llevar el talisman en un hueco aunque este cerrado: para probar)
        var talisman = Commands.literal("talisman").executes(c -> verTalismanes(c.getSource(), c.getSource().getPlayerOrException()));
        for (Talisman.Clase t : Talisman.Clase.values())
            talisman.then(Commands.literal(t.id).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                p.getInventory().add(new net.minecraft.world.item.ItemStack(DarkCraftMod.TALISMANES.get(t).get()));
                return verTalismanes(c.getSource(), p);
            }).then(Commands.literal("poner").then(Commands.argument("hueco", IntegerArgumentType.integer(1, Talismanes.MAX)).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                Talismanes.ponerDirecto(p, IntegerArgumentType.getInteger(c, "hueco") - 1, new net.minecraft.world.item.ItemStack(DarkCraftMod.TALISMANES.get(t).get()));
                return verTalismanes(c.getSource(), p);
            }))));
        talisman.then(Commands.literal("quitar").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            for (int h = 0; h < Talismanes.MAX; h++) Talismanes.ponerDirecto(p, h, net.minecraft.world.item.ItemStack.EMPTY);
            return verTalismanes(c.getSource(), p);
        }));
        var fruto = Commands.literal("fruto");
        for (Fruto.Clase f : Fruto.Clase.values())
            fruto.then(Commands.literal(f.id).executes(c -> darFruto(c.getSource(), f, 1))
                    .then(Commands.argument("n", IntegerArgumentType.integer(1, 64)).executes(c -> darFruto(c.getSource(), f, IntegerArgumentType.getInteger(c, "n")))));
        // hechizos: darlos (pergamino o plegaria), memorizarlos sin santuario, el catalizador y el resumen (para probar)
        var hechizo = Commands.literal("hechizo").executes(c -> verHechizos(c.getSource(), c.getSource().getPlayerOrException()));
        for (com.darkcraft.hechizos.Hechizo h : com.darkcraft.hechizos.Hechizo.values())
            hechizo.then(Commands.literal(h.id).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                p.getInventory().add(new net.minecraft.world.item.ItemStack(DarkCraftMod.HECHIZOS.get(h).get()));
                return verHechizos(c.getSource(), p);
            }));
        hechizo.then(Commands.literal("memorizar").executes(c -> {
            // memoriza todos los que lleve encima, en orden, sin mirar los huecos
            ServerPlayer p = c.getSource().getPlayerOrException();
            java.util.List<com.darkcraft.hechizos.Hechizo> l = new java.util.ArrayList<>();
            for (com.darkcraft.hechizos.Hechizo h : com.darkcraft.hechizos.Hechizo.values()) if (Memoria.tiene(p, h)) l.add(h);
            Memoria.forzar(p, l);
            return verHechizos(c.getSource(), p);
        }));
        hechizo.then(Commands.literal("olvidar").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            Memoria.forzar(p, java.util.List.of());
            return verHechizos(c.getSource(), p);
        }));
        hechizo.then(Commands.literal("baston").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.getInventory().add(new net.minecraft.world.item.ItemStack(DarkCraftMod.BASTON.get()));
            return verHechizos(c.getSource(), p);
        }));
        hechizo.then(Commands.literal("sello").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.getInventory().add(new net.minecraft.world.item.ItemStack(DarkCraftMod.SELLO_SAGRADO.get()));
            return verHechizos(c.getSource(), p);
        }));
        // la guia: sentarla delante (mirando al jugador; recuerda el santuario encendido que haya cerca) u olvidar lo hablado
        var guia = Commands.literal("guia").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            ServerLevel lvl = p.serverLevel();
            BlockPos pos = p.blockPosition().relative(p.getDirection(), 2);
            com.darkcraft.personajes.Guia g = com.darkcraft.personajes.Guia.poner(lvl, pos, p.blockPosition(),
                    com.darkcraft.santuario.Santuarios.encendidoCerca(lvl, pos, 8));
            c.getSource().sendSuccess(() -> Component.literal(g == null ? "-" : "guia en " + pos.toShortString()), false);
            return g == null ? 0 : 1;
        }).then(Commands.literal("olvidar").executes(c -> {
            com.darkcraft.dialogo.Conversaciones.olvidar(c.getSource().getPlayerOrException());
            return 1;
        }));
        // el herrero y el mercader: ponerlos delante (el herrero con su yunque entre los dos) u olvidar lo hablado
        var herrero = Commands.literal("herrero").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            ServerLevel lvl = p.serverLevel();
            BlockPos yunque = p.blockPosition().relative(p.getDirection(), 2);
            BlockPos pos = yunque.relative(p.getDirection());
            lvl.setBlock(yunque, net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AnvilBlock.FACING, p.getDirection().getClockWise()), 3);
            com.darkcraft.personajes.Herrero h = com.darkcraft.personajes.Herrero.poner(lvl, pos, yunque);
            c.getSource().sendSuccess(() -> Component.literal(h == null ? "-" : "herrero en " + pos.toShortString()), false);
            return h == null ? 0 : 1;
        }).then(Commands.literal("olvidar").executes(c -> {
            com.darkcraft.dialogo.Conversaciones.olvidar(c.getSource().getPlayerOrException(), "herrero");
            return 1;
        }));
        var mercader = Commands.literal("mercader").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            BlockPos pos = p.blockPosition().relative(p.getDirection(), 2);
            com.darkcraft.personajes.Mercader m = com.darkcraft.personajes.Mercader.poner(p.serverLevel(), pos, p.blockPosition());
            c.getSource().sendSuccess(() -> Component.literal(m == null ? "-" : "mercader en " + pos.toShortString()), false);
            return m == null ? 0 : 1;
        }).then(Commands.literal("olvidar").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            com.darkcraft.dialogo.Conversaciones.olvidar(p, "mercader");
            com.darkcraft.tienda.Tienda.olvidar(p);
            return 1;
        }));
        // la mejora del arma de la mano derecha y piedras de forja
        var mejora = Commands.literal("mejora").then(Commands.argument("n", IntegerArgumentType.integer(0, com.darkcraft.forja.Forja.MAX)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            com.darkcraft.forja.Forja.poner(p, IntegerArgumentType.getInteger(c, "n"));
            c.getSource().sendSuccess(() -> com.darkcraft.forja.Forja.nombre(p.getMainHandItem()), false);
            return 1;
        }));
        var piedras = Commands.literal("piedras").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            for (int g = 1; g <= 3; g++) Equipo.dar(p, new net.minecraft.world.item.ItemStack(com.darkcraft.forja.Forja.piedra(g), 6));
            return 1;
        });
        // las cenizas de guerra: darla o ponersela al arma de la mano derecha
        var ceniza = Commands.literal("ceniza");
        for (com.darkcraft.combate.Habilidad h : com.darkcraft.combate.Habilidad.values())
            ceniza.then(Commands.literal(h.id).executes(c -> {
                Equipo.dar(c.getSource().getPlayerOrException(), new net.minecraft.world.item.ItemStack(DarkCraftMod.CENIZAS.get(h).get()));
                return 1;
            }).then(Commands.literal("poner").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                net.minecraft.world.item.ItemStack st = p.getMainHandItem();
                if (!com.darkcraft.combate.Habilidad.admite(st) || !h.vale(com.darkcraft.combate.Armas.de(st))) return 0;
                com.darkcraft.combate.Habilidad.poner(st, h);
                p.getInventory().setChanged();
                c.getSource().sendSuccess(() -> Component.translatable(h.clave()), false);
                return 1;
            })));
        ceniza.then(Commands.literal("quitar").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            com.darkcraft.combate.Habilidad.poner(p.getMainHandItem(), null);
            p.getInventory().setChanged();
            return 1;
        }));
        return Commands.literal("rol")
                .then(Clase.comando())
                .then(ceniza)
                .then(talisman)
                .then(fruto)
                .then(hechizo)
                .then(guia)
                .then(herrero)
                .then(mercader)
                .then(mejora)
                .then(piedras)
                .then(Commands.literal("resistencias").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    StringBuilder b = new StringBuilder();
                    for (Resistencias.Tipo t : Resistencias.Tipo.values())
                        b.append(b.length() > 0 ? " · " : "").append(t.id).append(' ').append(Math.round(Resistencias.de(p, t) * 100)).append('%');
                    c.getSource().sendSuccess(() -> Component.literal(b.toString()), false);
                    return 1;
                }))
                .then(Commands.literal("santuario").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    ServerLevel lvl = p.serverLevel();
                    BlockPos pos = p.blockPosition().relative(p.getDirection());
                    BlockPos pie = pos.below();
                    if (!lvl.getBlockState(pos).canBeReplaced() || !lvl.getBlockState(pie).isFaceSturdy(lvl, pie, Direction.UP)) {
                        c.getSource().sendFailure(Component.translatable("darkcraft.cmd.santuario.no"));
                        return 0;
                    }
                    com.darkcraft.santuario.Santuarios.poner(lvl, pos, com.darkcraft.santuario.Santuarios.SIN_NOMBRE);
                    c.getSource().sendSuccess(() -> Component.translatable("darkcraft.cmd.santuario", pos.toShortString()), false);
                    return 1;
                }))
                .then(Commands.literal("frascos")
                        .executes(c -> verFrascos(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("total", IntegerArgumentType.integer(1, Frascos.TOPE)).executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            Ficha.Frascos f = Ficha.frascos(p);
                            int total = IntegerArgumentType.getInteger(c, "total");
                            int mana = Math.min(f.manaMax(), total - 1), vida = total - mana;
                            Ficha.frascos(p, new Ficha.Frascos(vida, mana, vida, mana, f.mejora()));
                            fichaCambiada(p);
                            return verFrascos(c.getSource(), p);
                        }))
                        .then(Commands.literal("mejora").then(Commands.argument("n", IntegerArgumentType.integer(0, 12)).executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            Frascos.mejorar(p, IntegerArgumentType.getInteger(c, "n"));
                            return verFrascos(c.getSource(), p);
                        }))))
                .then(Commands.literal("nivel")
                        .executes(c -> verNivel(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.literal("reiniciar").executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            Ficha.atributos(p, Atributo.deBase());
                            aplicar(p);
                            return verNivel(c.getSource(), p);
                        })))
                .then(atributo);
    }

    private static int verTalismanes(CommandSourceStack src, ServerPlayer p) {
        src.sendSuccess(() -> Component.literal(Talismanes.resumen(p)), false);
        return Talismanes.huecos(p);
    }

    private static int verHechizos(CommandSourceStack src, ServerPlayer p) {
        src.sendSuccess(() -> Component.literal(com.darkcraft.hechizos.Lanzar.resumen(p)), false);
        return Memoria.memorizados(p).size();
    }

    private static int darFruto(CommandSourceStack src, Fruto.Clase f, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        p.getInventory().add(new net.minecraft.world.item.ItemStack(DarkCraftMod.FRUTOS.get(f).get(), n));
        return n;
    }

    private static int verFrascos(CommandSourceStack src, ServerPlayer p) {
        Ficha.Frascos f = Ficha.frascos(p);
        src.sendSuccess(() -> Component.translatable("darkcraft.cmd.frascos", f.vida() + "/" + f.vidaMax(), f.mana() + "/" + f.manaMax(), f.mejora()), false);
        return f.vidaMax() + f.manaMax();
    }

    private static int verNivel(CommandSourceStack src, ServerPlayer p) {
        int[] a = Ficha.atributos(p);
        StringBuilder b = new StringBuilder();
        for (Atributo at : Atributo.values()) b.append(b.length() == 0 ? "" : ", ").append(at.clave).append(' ').append(a[at.ordinal()]);
        src.sendSuccess(() -> Component.translatable("darkcraft.cmd.nivel", Nivel.de(a), b.toString()), false);
        return Nivel.de(a);
    }
}
