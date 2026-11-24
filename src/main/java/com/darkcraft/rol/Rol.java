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
 * <li><b>Sin hambre</b>: el nivel de comida va fijo (no se regenera vida sola ni se pasa hambre); de momento la comida
 * cura al comerla (hasta que lleguen los frascos).</li>
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

    public static boolean enCombate(Player p) {
        return de(p).combate > 0;
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
                s.estamina = EST_MAX;
                s.agotado = false;
                s.pausa = 0;
            } else {
                if (s.combate > 0 && sp.isSprinting() && !sp.isPassenger()) {
                    s.estamina -= GASTO_CORRER;
                    s.pausa = Math.max(s.pausa, 8);
                }
                if (s.pausa > 0) s.pausa--;
                else if (s.estamina < EST_MAX)
                    s.estamina = Math.min(EST_MAX, s.estamina + (sp.isBlocking() ? RECUPERA_GUARDIA : s.combate > 0 ? RECUPERA_COMBATE : RECUPERA));
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
            Net.to(sp, new Net.Estamina(dec, Math.round(EST_MAX * 10f), s.agotado));
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
    }

    private static void gastar(Estado s, float cuanto, int pausa) {
        s.estamina -= cuanto;
        s.pausa = Math.max(s.pausa, pausa);
    }

    // ------------------------------------------------------------------ estamina: atacar, saltar y cubrirse

    @SubscribeEvent
    public static void onAtaque(AttackEntityEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || libre(sp)) return;
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

    // ------------------------------------------------------------------ sin hambre: la comida cura

    @SubscribeEvent
    public static void onComer(LivingEntityUseItemEvent.Finish e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        FoodProperties fp = e.getItem().getFoodProperties(sp);
        if (fp != null && fp.getNutrition() > 0) sp.heal(fp.getNutrition());
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
            dar(p, valor, v.position().add(0, v.getBbHeight() * 0.5, 0));
        }
    }

    /** Lo que deja un enemigo: segun su vida y lo que pega. Los animales y demas, nada. */
    static int valor(LivingEntity e) {
        if (!(e instanceof Enemy)) return 0;
        // los jefes dan los suyos al acabar su muerte (Rol.jefe)
        if (e instanceof com.darkcraft.rey.Rey || e instanceof com.darkcraft.gavran.Gavran) return 0;
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
        dar(p, ecos, null);
        Net.to(p, new Net.Cartel(Net.Cartel.JEFE, claveNombre, ecos));
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
        s.estamina = EST_MAX;
        s.agotado = false;
        s.pausa = s.combate = 0;
        s.sucio = true;
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
        s.enviadaDec = -1;
        s.zona = "";
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer) refrescar(e.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer) refrescar(e.getEntity());
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
}
