package com.darkcraft.montura;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.net.Net;
import com.darkcraft.story.Logros;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * La montura de Morvain (parte 7b, respuesta 129 del plan): el silbato que da Maelis en el Arco del Umbral llama al
 * corcel de hueso y niebla ({@link Corcel}). Con la tecla H ({@code Net.CorcelLlamar}) o usando el silbato:
 * <ul>
 * <li>solo en Morvain, con el silbato encima (en creativo no hace falta) y con sitio para el corcel donde se esta;</li>
 * <li>aparece debajo de quien silba, ya montado (niebla, el silbido y su relincho); si ya se iba montado, se deshace;</li>
 * <li>cada uno tiene uno: si tenia otro por ahi, ese se va;</li>
 * <li>la vida que le queda se apunta en la ficha ({@code darkcraft_rol} → {@code corcel}: vida, cuando, vuelve) y se
 * recupera sola (una cada 2 s mientras no esta); herido de muerte se deshace y tarda {@link #VUELTA} en poder volver
 * (vuelve con media vida). Descansar en un santuario lo deja entero y sin espera.</li>
 * </ul>
 * Los golpes que van al jinete los reparte ({@link #onHerido}): el jinete se lleva el 65 % y el corcel pierde el 60 % del
 * golpe.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Montura {
    private Montura() {}

    /** Lo que tarda en volver si se deshizo herido (ticks). */
    public static final int VUELTA = 60 * 20;
    /** Lo que recupera mientras no esta: una de vida cada tantos ticks. */
    public static final int RECUPERA = 40;
    /** Del golpe al jinete: lo que se lleva el y lo que pierde el corcel. */
    public static final float AL_JINETE = 0.65f, AL_CORCEL = 0.6f;

    private static final String CLAVE = "corcel";
    /** El corcel de cada jugador que anda por ahi (dueno → corcel). */
    private static final Map<UUID, Corcel> ACTIVOS = new HashMap<>();

    // ------------------------------------------------------------------ la ficha del corcel

    private static CompoundTag datos(Player p) {
        return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("darkcraft_rol").getCompound(CLAVE);
    }

    private static void guardar(Player p, CompoundTag c) {
        CompoundTag root = p.getPersistentData();
        CompoundTag pers = root.getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag rol = pers.getCompound("darkcraft_rol");
        rol.put(CLAVE, c);
        pers.put("darkcraft_rol", rol);
        root.put(Player.PERSISTED_NBT_TAG, pers);
    }

    private static long ahora(ServerPlayer sp) {
        return sp.server.overworld().getGameTime();
    }

    /** Los ticks que le faltan para poder volver (0 si ya puede). */
    public static int espera(ServerPlayer sp) {
        long v = datos(sp).getLong("vuelve");
        return (int) Math.max(0, v - ahora(sp));
    }

    /** La vida con la que vendria ahora (con lo que ha recuperado desde que se fue). */
    public static float vida(ServerPlayer sp) {
        CompoundTag c = datos(sp);
        if (!c.contains("vida")) return Corcel.VIDA;
        long desde = Math.max(c.getLong("cuando"), c.getLong("vuelve"));
        float ganada = Math.max(0, ahora(sp) - desde) / (float) RECUPERA;
        return Mth.clamp(c.getFloat("vida") + ganada, 1f, Corcel.VIDA);
    }

    /** Al descansar en un santuario: entero y sin espera. */
    public static void descanso(ServerPlayer sp) {
        guardar(sp, new CompoundTag());
        Corcel c = ACTIVOS.get(sp.getUUID());
        if (c != null && c.isAlive() && !c.deshaciendo()) c.setHealth(c.getMaxHealth());
    }

    // ------------------------------------------------------------------ llamar

    public static boolean tieneSilbato(ServerPlayer sp) {
        if (sp.isCreative()) return true;
        for (ItemStack st : sp.getInventory().items) if (st.is(DarkCraftMod.SILBATO_CORCEL.get())) return true;
        for (ItemStack st : sp.getInventory().offhand) if (st.is(DarkCraftMod.SILBATO_CORCEL.get())) return true;
        return false;
    }

    /** El corcel de un jugador (o null). */
    public static Corcel de(ServerPlayer sp) {
        Corcel c = ACTIVOS.get(sp.getUUID());
        return c != null && c.isAlive() && !c.isRemoved() ? c : null;
    }

    /** Silba (la tecla H o el silbato). Devuelve si ha venido. */
    public static boolean llamar(ServerPlayer sp) {
        if (!sp.isAlive() || sp.isSpectator()) return false;
        // montado: se baja y el corcel se va
        if (sp.getVehicle() instanceof Corcel c) {
            c.deshacer(false);
            return false;
        }
        if (!tieneSilbato(sp)) {
            aviso(sp, "darkcraft.corcel.sin_silbato", 0);
            return false;
        }
        ServerLevel sl = sp.serverLevel();
        if (!Morvain.es(sl)) {
            aviso(sp, "darkcraft.corcel.solo_morvain", 0);
            silbido(sl, sp, 0.7f);
            return false;
        }
        if (sp.isPassenger()) {
            aviso(sp, "darkcraft.corcel.montado", 0);
            return false;
        }
        // por la cadena del sur no se sube a caballo (parte 7aa)
        if (com.darkcraft.morvain.Subida.sobreCadena(sp.getX(), sp.getY(), sp.getZ()) || com.darkcraft.morvain.Subida.enBoca(sp.getX(), sp.getY(), sp.getZ())) {
            aviso(sp, "darkcraft.corcel.cadena", 0);
            silbido(sl, sp, 0.7f);
            return false;
        }
        int espera = espera(sp);
        if (espera > 0) {
            aviso(sp, "darkcraft.corcel.espera", (espera + 19) / 20);
            silbido(sl, sp, 0.8f);
            return false;
        }
        if (sp.isInWater() && sp.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 1.0) {
            aviso(sp, "darkcraft.corcel.agua", 0);
            return false;
        }
        Corcel viejo = ACTIVOS.remove(sp.getUUID());
        if (viejo != null && viejo.isAlive()) {
            guardarVida(sp, viejo);
            viejo.quitar();
        }
        Corcel c = DarkCraftMod.CORCEL.get().create(sl);
        if (c == null) return false;
        double[] sitio = sitio(sl, c, sp);
        if (sitio == null) {
            aviso(sp, "darkcraft.corcel.sitio", 0);
            silbido(sl, sp, 0.8f);
            return false;
        }
        c.moveTo(sitio[0], sitio[1], sitio[2], sp.getYRot(), 0f);
        c.setYBodyRot(sp.getYRot());
        c.setYHeadRot(sp.getYRot());
        c.dueno(sp.getUUID());
        c.setHealth(vida(sp));
        sl.addFreshEntity(c);
        ACTIVOS.put(sp.getUUID(), c);
        silbido(sl, sp, 1f);
        c.relinchar();
        sl.playSound(null, c.getX(), c.getY(), c.getZ(), DarkCraftMod.SFX_CORCEL_APARECE.get(), SoundSource.NEUTRAL, 1f, 1f);
        c.niebla(sl, 26);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.getX(), c.getY() + 0.1, c.getZ(), 16, 0.6, 0.05, 0.6, 0.02);
        // (a la fuerza: justo despues de bajarse el juego no deja volver a montar en 3 s)
        if (Math.abs(sp.getX() - c.getX()) < 2.5 && Math.abs(sp.getZ() - c.getZ()) < 2.5) sp.startRiding(c, true);
        Logros.dar(sp, "corcel");
        DarkCraftMod.LOGGER.info("[DarkCraft] corcel: {} lo llama en {} {} {} (vida {}, montado {})", sp.getGameProfile().getName(),
                Mth.floor(c.getX()), Mth.floor(c.getY()), Mth.floor(c.getZ()), Math.round(c.getHealth()), c.hasPassenger(sp));
        return true;
    }

    /** Donde cabe: donde se esta o al lado (hasta 2 bloques, a la misma altura o uno mas arriba). */
    private static double[] sitio(ServerLevel sl, Corcel c, ServerPlayer sp) {
        double x = sp.getX(), y = sp.getY(), z = sp.getZ();
        for (int r = 0; r <= 2; r++)
            for (int dy = 0; dy <= 1; dy++)
                for (int dx = -r; dx <= r; dx++)
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                        double cx = x + dx, cy = Math.floor(y) + dy, cz = z + dz;
                        if (dy == 0 && r == 0) cy = y;
                        AABB caja = c.getType().getDimensions().makeBoundingBox(cx, cy, cz);
                        // con sitio y sin agua honda (la que le pasa de la barriga)
                        AABB honda = new AABB(caja.minX, caja.minY + 1.15, caja.minZ, caja.maxX, caja.minY + 1.6, caja.maxZ);
                        if (sl.noCollision(c, caja) && !sl.containsAnyLiquid(honda)) return new double[]{cx, cy, cz};
                    }
        return null;
    }

    private static void silbido(ServerLevel sl, ServerPlayer sp, float vol) {
        sl.playSound(null, sp.getX(), sp.getEyeY(), sp.getZ(), DarkCraftMod.SFX_CORCEL_LLAMADA.get(), SoundSource.PLAYERS, vol, 1f);
    }

    private static void aviso(ServerPlayer sp, String clave, int n) {
        Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, clave, n));
    }

    // ------------------------------------------------------------------ lo que avisa el corcel

    private static void guardarVida(ServerPlayer sp, Corcel c) {
        CompoundTag t = datos(sp);
        t.putFloat("vida", Math.max(1f, c.getHealth()));
        t.putLong("cuando", ahora(sp));
        guardar(sp, t);
    }

    static void alDeshacerse(Corcel c, boolean herido) {
        if (c.dueno() == null || !(c.level() instanceof ServerLevel sl)) return;
        ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(c.dueno());
        if (sp == null) return;
        if (herido) {
            CompoundTag t = datos(sp);
            t.putFloat("vida", Corcel.VIDA * 0.5f);
            t.putLong("vuelve", ahora(sp) + VUELTA);
            t.putLong("cuando", ahora(sp) + VUELTA);
            guardar(sp, t);
            aviso(sp, "darkcraft.corcel.deshecho", VUELTA / 20);
        } else guardarVida(sp, c);
        DarkCraftMod.LOGGER.info("[DarkCraft] corcel: el de {} se deshace{} (vida {})", sp.getGameProfile().getName(), herido ? " herido" : "",
                Math.round(c.getHealth()));
    }

    static void alIrse(Corcel c) {
        if (c.dueno() != null && ACTIVOS.get(c.dueno()) == c) ACTIVOS.remove(c.dueno());
    }

    // ------------------------------------------------------------------ eventos

    /** Lo que le pegan al jinete se lo reparte con su corcel (lo que no es caer, ahogarse, el vacio...). */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHerido(LivingHurtEvent e) {
        if (e.isCanceled() || !(e.getEntity() instanceof ServerPlayer sp) || !(sp.getVehicle() instanceof Corcel c)) return;
        DamageSource s = e.getSource();
        if (s.getEntity() == null && s.getDirectEntity() == null && !s.is(DamageTypes.EXPLOSION) && !s.is(DamageTypes.PLAYER_EXPLOSION)) return;
        if (s.getEntity() == c || s.getEntity() == sp) return;
        float golpe = e.getAmount();
        e.setAmount(golpe * AL_JINETE);
        c.recibir(golpe * AL_CORCEL);
    }

    /** Al irse del servidor montado, el corcel se va antes (no se guarda con el jugador). */
    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        Corcel c = ACTIVOS.remove(sp.getUUID());
        if (c != null && c.isAlive()) {
            guardarVida(sp, c);
            c.quitar();
        }
        if (sp.getVehicle() instanceof Corcel otro) otro.quitar();
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        ACTIVOS.clear();
    }

    /** Lo pide el cliente: el segundo salto (o el primero) suena y echa niebla para todos. */
    public static void salto(ServerPlayer sp, boolean doble) {
        if (!(sp.getVehicle() instanceof Corcel c) || c.deshaciendo()) return;
        ServerLevel sl = sp.serverLevel();
        sl.playSound(null, c.getX(), c.getY(), c.getZ(), DarkCraftMod.SFX_CORCEL_SALTO.get(), SoundSource.NEUTRAL, doble ? 1f : 0.7f,
                doble ? 1.08f : 0.95f);
        if (doble) {
            sl.sendParticles(ParticleTypes.CLOUD, c.getX(), c.getY() + 0.2, c.getZ(), 16, 0.5, 0.1, 0.5, 0.03);
            sl.sendParticles(ParticleTypes.SOUL, c.getX(), c.getY() + 0.3, c.getZ(), 6, 0.4, 0.1, 0.4, 0.02);
        }
        SALTOS.merge(sp.getUUID(), doble ? 2 : 1, Integer::sum);
    }

    /** Para la prueba: saltos que ha contado el servidor de cada jugador (1 el primero, 2 el doble). */
    public static final Map<UUID, Integer> SALTOS = new HashMap<>();

    // ------------------------------------------------------------------ comandos

    /** {@code /darkcraft corcel [llamar | silbato | deshacer | herir <n> | estado]}. */
    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("corcel")
                .executes(c -> estado(c.getSource()))
                .then(Commands.literal("estado").executes(c -> estado(c.getSource())))
                .then(Commands.literal("llamar").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    if (sp.getVehicle() instanceof Corcel) return 1;
                    return llamar(sp) ? 1 : 0;
                }))
                .then(Commands.literal("silbato").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    com.darkcraft.rol.Equipo.dar(sp, new ItemStack(DarkCraftMod.SILBATO_CORCEL.get()));
                    return 1;
                }))
                .then(Commands.literal("deshacer").executes(c -> {
                    Corcel k = de(c.getSource().getPlayerOrException());
                    if (k != null) k.deshacer(false);
                    return 1;
                }))
                .then(Commands.literal("espera").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    guardar(sp, new CompoundTag());
                    return 1;
                }))
                .then(Commands.literal("herir").then(Commands.argument("n", FloatArgumentType.floatArg(0f, 200f)).executes(c -> {
                    Corcel k = de(c.getSource().getPlayerOrException());
                    if (k != null) k.recibir(FloatArgumentType.getFloat(c, "n"));
                    return 1;
                })));
    }

    private static int estado(CommandSourceStack src) {
        ServerPlayer sp = src.getPlayer();
        if (sp == null) return 0;
        src.sendSuccess(() -> Component.literal(resumen(sp)), false);
        return 1;
    }

    /** Una linea para las pruebas y el comando. */
    public static String resumen(ServerPlayer sp) {
        Corcel c = de(sp);
        StringBuilder b = new StringBuilder("corcel ");
        if (c == null) b.append("no esta");
        else b.append(String.format(java.util.Locale.ROOT, "en %d %d %d, vida %.0f/%.0f, montado %s, accion %d", Mth.floor(c.getX()),
                Mth.floor(c.getY()), Mth.floor(c.getZ()), c.getHealth(), c.getMaxHealth(), c.hasPassenger(sp), c.accion()));
        b.append(String.format(java.util.Locale.ROOT, "; vendria con %.0f; espera %d s; silbato %s; saltos %d", vida(sp), (espera(sp) + 19) / 20,
                tieneSilbato(sp), SALTOS.getOrDefault(sp.getUUID(), 0)));
        return b.toString();
    }
}
