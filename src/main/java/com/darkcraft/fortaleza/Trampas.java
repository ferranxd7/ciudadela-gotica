package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.FortalezaPlan;
import com.darkcraft.gen.Pal;
import com.darkcraft.story.Logros;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * Las trampas y el muro falso de la Fortaleza de Hueso (parte 5c, respuestas 44 y 46 del plan). Las posiciones las da el
 * plano (gen/TorreCupula); aqui lo que pasa:
 * <ul>
 * <li>el <b>suelo que cede</b> (planta 4): al pisarlo cruje y a los 0,3 s se hunde (se cae a la planta de abajo); vuelve
 * a estar a los 30 s;</li>
 * <li>los <b>pinchos de hueso</b> (pasillo norte del sotano y camino norte del jardin): al pisar una de sus losas cinceladas,
 * un chasquido y a los 0,3 s salen pinchos de hueso en todas (6 de dano y frena);</li>
 * <li>las <b>flechas</b> del paso de la puerta (por dentro de la reja): al cruzarlo salen flechas de las aspilleras de los
 * dos lados;</li>
 * <li>el <b>muro falso</b> del sotano: parece el muro de la torre; al golpearlo se deshace y deja ver la sala secreta (logro
 * `secreto`; queda abierto para siempre, `FortalezaDatos.Estado.muro`).</li>
 * </ul>
 * Las trampas solo saltan con jugadores en supervivencia o aventura (en creativo no).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Trampas {
    private Trampas() {}

    /** Lo de cada fortaleza (en memoria): cuando se piso cada trampa. */
    private static final class Estado {
        // (muy atras, pero sin llegar a Long.MIN_VALUE: t - MIN_VALUE se da la vuelta y saldria negativo)
        long cede = -1_000_000L;
        boolean hueco, revisado;
        final long[] pinchos = {-1_000_000L, -1_000_000L};
        long flechas = -1_000_000L;
    }

    private static final Map<String, Estado> ESTADOS = new HashMap<>();
    /** Ticks que tarda en volver el suelo y lo que hay que esperar entre pinchos y entre flechas. */
    public static final int VUELVE = 600, ENTRE_PINCHOS = 50, ENTRE_FLECHAS = 80;
    /** Lo que tarda en volver el suelo ahora (la prueba del cliente lo acorta). */
    public static int vuelve = VUELVE;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER || !(e.player instanceof ServerPlayer p)) return;
        if ((p.tickCount & 1) != 0) return;
        Fortalezas.Sitio s = FortalezaEventos.zona(p);
        if (s == null) return;
        ServerLevel lvl = p.serverLevel();
        FortalezaDatos.Estado datos = FortalezaDatos.get(p.server).de(s);
        if (!datos.completa) return;
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        Estado est = ESTADOS.computeIfAbsent(s.clave(), k -> new Estado());
        long t = lvl.getGameTime();
        // (una vez por sesion, con su chunk cargado: si el servidor se cerro con el suelo hundido, se vuelve a poner)
        if (!est.revisado) {
            int[] c = f.f.sueloQueCede()[4];
            if (lvl.isLoaded(new BlockPos(c[0], c[1], c[2]))) {
                est.revisado = true;
                if (!est.hueco) poner(lvl, f);
            }
        }
        if (est.hueco && t - est.cede > vuelve) {
            if (nadieEnElHueco(lvl, f)) {
                poner(lvl, f);
                est.hueco = false;
            } else est.cede = t - vuelve + 20;
        }
        if (p.isCreative() || p.isSpectator() || !p.isAlive()) return;
        int bx = p.getBlockX(), by = p.getBlockY(), bz = p.getBlockZ();
        // 1) el suelo que cede
        if (!est.hueco && est.cede < t - vuelve) {
            for (int[] q : f.f.sueloQueCede())
                if (q[0] == bx && q[2] == bz && by == q[1] + 1 && p.onGround()) {
                    est.cede = t;
                    est.hueco = true;
                    ceder(lvl, f);
                    break;
                }
        }
        // 2) los pinchos de hueso (dos grupos de seis losas)
        int[][] pin = f.f.pinchos();
        for (int g = 0; g < 2; g++) {
            if (t - est.pinchos[g] < ENTRE_PINCHOS) continue;
            for (int i = g * 6; i < g * 6 + 6; i++) {
                int[] q = pin[i];
                if (q[0] == bx && q[2] == bz && Math.abs(p.getY() - q[1]) < 0.6) {
                    est.pinchos[g] = t;
                    pinchos(lvl, f, g);
                    break;
                }
            }
        }
        // 3) las flechas del paso de la puerta
        if (t - est.flechas >= ENTRE_FLECHAS) {
            for (int[] q : f.f.flechasDisparo())
                if (q[0] == bx && q[2] == bz && Math.abs(p.getY() - q[1]) < 1.5) {
                    est.flechas = t;
                    flechas(lvl, f);
                    break;
                }
        }
    }

    // ------------------------------------------------------------------ el suelo que cede

    private static void ceder(ServerLevel lvl, FortalezaPlan f) {
        int[][] cs = f.f.sueloQueCede();
        int[] m = cs[4];
        lvl.playSound(null, m[0] + 0.5, m[1] + 1, m[2] + 0.5, SoundEvents.DEEPSLATE_TILES_BREAK, SoundSource.BLOCKS, 1.6f, 0.6f);
        lvl.playSound(null, m[0] + 0.5, m[1] + 1, m[2] + 0.5, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.4f, 0.5f);
        lvl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Pal.DS_TILES_CRACKED), m[0] + 0.5, m[1] + 1.05, m[2] + 0.5, 30, 1.2, 0.05, 1.2, 0.05);
        FortalezaEventos.despues(lvl.getServer(), 6, () -> {
            for (int[] q : cs) {
                BlockPos b = new BlockPos(q[0], q[1], q[2]);
                if (lvl.getBlockState(b).is(Blocks.CRACKED_DEEPSLATE_TILES)) lvl.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
            }
            lvl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Pal.DS_TILES_CRACKED), m[0] + 0.5, m[1] + 0.5, m[2] + 0.5, 80, 1.3, 0.4, 1.3, 0.1);
            lvl.playSound(null, m[0] + 0.5, m[1], m[2] + 0.5, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 2.0f, 0.5f);
        });
    }

    /** Vuelve a poner el suelo que cede (si falta). */
    private static void poner(ServerLevel lvl, FortalezaPlan f) {
        for (int[] q : f.f.sueloQueCede()) {
            BlockPos b = new BlockPos(q[0], q[1], q[2]);
            if (lvl.isLoaded(b) && lvl.getBlockState(b).isAir()) lvl.setBlock(b, Pal.DS_TILES_CRACKED, 3);
        }
    }

    private static boolean nadieEnElHueco(ServerLevel lvl, FortalezaPlan f) {
        int[][] cs = f.f.sueloQueCede();
        for (ServerPlayer q : lvl.players())
            for (int[] c : cs)
                if (q.getBlockX() == c[0] && q.getBlockZ() == c[2] && q.getY() > c[1] - 1 && q.getY() < c[1] + 3) return false;
        return true;
    }

    // ------------------------------------------------------------------ los pinchos de hueso

    private static void pinchos(ServerLevel lvl, FortalezaPlan f, int g) {
        int[][] pin = f.f.pinchos();
        int[] c = pin[g * 6 + 1];
        lvl.playSound(null, c[0] + 0.5, c[1], c[2] + 0.5, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 1.2f, 0.6f);
        lvl.playSound(null, c[0] + 0.5, c[1], c[2] + 0.5, SoundEvents.SKELETON_HURT, SoundSource.BLOCKS, 0.8f, 0.4f);
        FortalezaEventos.despues(lvl.getServer(), 6, () -> {
            var hueso = Blocks.BONE_BLOCK.defaultBlockState();
            for (int i = g * 6; i < g * 6 + 6; i++) {
                int[] q = pin[i];
                for (int k = 0; k < 3; k++) {
                    double ox = (lvl.random.nextDouble() - 0.5) * 0.6, oz = (lvl.random.nextDouble() - 0.5) * 0.6;
                    com.darkcraft.armas.EfectosRaiz.estaca(lvl, hueso, q[0] + 0.5 + ox, q[1] - 0.3, q[2] + 0.5 + oz,
                            lvl.random.nextFloat() * 6.28f, (lvl.random.nextFloat() - 0.5f) * 0.4f, 1.6f + lvl.random.nextFloat() * 0.8f, 0.3f, 0, 26);
                }
            }
            lvl.playSound(null, c[0] + 0.5, c[1], c[2] + 0.5, SoundEvents.BONE_BLOCK_BREAK, SoundSource.BLOCKS, 1.6f, 0.7f);
            lvl.sendParticles(ParticleTypes.WHITE_ASH, c[0] + 0.5, c[1] + 0.6, c[2] + 0.5, 40, 1.5, 0.5, 1.5, 0.05);
            for (ServerPlayer q : lvl.players()) {
                if (q.isCreative() || q.isSpectator()) continue;
                for (int i = g * 6; i < g * 6 + 6; i++) {
                    int[] w = pin[i];
                    if (Math.abs(q.getX() - (w[0] + 0.5)) < 0.9 && Math.abs(q.getZ() - (w[2] + 0.5)) < 0.9 && Math.abs(q.getY() - w[1]) < 1.2) {
                        q.hurt(lvl.damageSources().cactus(), 6f);
                        q.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
                        break;
                    }
                }
            }
        });
    }

    // ------------------------------------------------------------------ las flechas

    private static void flechas(ServerLevel lvl, FortalezaPlan f) {
        int[][] as = f.f.flechasAspilleras();
        lvl.playSound(null, f.cx + 0.5, f.Y + 1, as[0][2] + 0.5, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 1.0f, 0.8f);
        for (int k = 0; k < as.length; k++) {
            int[] a = as[k];
            FortalezaEventos.despues(lvl.getServer(), 4 + k * 3, () -> {
                // de la aspillera hacia el otro lado del paso
                double dir = -a[3];
                Arrow fl = new Arrow(lvl, a[0] + 0.5 + dir * 0.7, a[1] + 0.5, a[2] + 0.5);
                fl.setDeltaMovement(dir * 2.2, 0.02, (lvl.random.nextDouble() - 0.5) * 0.05);
                fl.pickup = AbstractArrow.Pickup.DISALLOWED;
                fl.setBaseDamage(1.2);
                lvl.addFreshEntity(fl);
                lvl.playSound(null, a[0] + 0.5, a[1] + 0.5, a[2] + 0.5, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0f, 1.2f);
            });
        }
    }

    // ------------------------------------------------------------------ el muro falso

    /** Si el bloque es del muro falso de la fortaleza f. */
    static boolean deMuro(FortalezaPlan f, BlockPos b) {
        for (int[] q : f.f.muroFalso()) if (q[0] == b.getX() && q[1] == b.getY() && q[2] == b.getZ()) return true;
        return false;
    }

    /** Un golpe con arma (Combate): si se mira al muro falso de cerca, se abre. */
    public static void golpe(ServerPlayer p) {
        Fortalezas.Sitio s = FortalezaEventos.zona(p);
        if (s == null) return;
        Vec3 ojo = p.getEyePosition(), fin = ojo.add(p.getLookAngle().scale(4.0));
        BlockHitResult hit = p.level().clip(new ClipContext(ojo, fin, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        FortalezaPlan f = Fortalezas.plan(p.serverLevel().getSeed(), s);
        if (deMuro(f, hit.getBlockPos())) abrirMuro(p, s);
    }

    /** Con la mano (o en supervivencia): el clic izquierdo en el muro falso tambien lo abre. */
    @SubscribeEvent
    public static void onIzquierdo(PlayerInteractEvent.LeftClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        Fortalezas.Sitio s = FortalezaEventos.zona(p);
        if (s == null) return;
        FortalezaPlan f = Fortalezas.plan(p.serverLevel().getSeed(), s);
        if (!deMuro(f, e.getPos())) return;
        abrirMuro(p, s);
        e.setCanceled(true);
    }

    /** Se deshace el muro falso (una vez por fortaleza; queda abierto). */
    public static boolean abrirMuro(ServerPlayer p, Fortalezas.Sitio s) {
        ServerLevel lvl = p.serverLevel();
        FortalezaDatos d = FortalezaDatos.get(p.server);
        FortalezaDatos.Estado est = d.de(s);
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        quitarMuro(lvl, f, true);
        Logros.dar(p, "secreto");
        if (est.muro) return false;
        est.muro = true;
        d.setDirty();
        p.displayClientMessage(Component.translatable("darkcraft.fortaleza.muro_falso").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
        return true;
    }

    /** Quita los bloques del muro falso (con su polvo de almas, si se ve). */
    static void quitarMuro(ServerLevel lvl, FortalezaPlan f, boolean efectos) {
        int[][] m = f.f.muroFalso();
        for (int[] q : m) {
            BlockPos b = new BlockPos(q[0], q[1], q[2]);
            if (!lvl.isLoaded(b) || lvl.getBlockState(b).isAir()) continue;
            lvl.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
            if (efectos) lvl.sendParticles(ParticleTypes.SOUL, q[0] + 0.5, q[1] + 0.5, q[2] + 0.5, 3, 0.3, 0.3, 0.3, 0.02);
        }
        if (efectos) {
            int[] c = m[m.length / 2];
            lvl.playSound(null, c[0] + 0.5, c[1] + 0.5, c[2] + 0.5, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.6f, 0.5f);
            lvl.playSound(null, c[0] + 0.5, c[1] + 0.5, c[2] + 0.5, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.7f);
            lvl.sendParticles(ParticleTypes.LARGE_SMOKE, c[0] + 0.5, c[1] + 0.5, c[2] + 0.5, 24, 0.6, 0.8, 1.2, 0.01);
        }
    }

    /** Para las pruebas: vuelve a dejar el muro (y el suelo) como al principio. */
    public static void reiniciar(ServerLevel lvl, FortalezaPlan f, FortalezaDatos.Estado est) {
        for (int[] q : f.f.muroFalso()) {
            BlockPos b = new BlockPos(q[0], q[1], q[2]);
            if (lvl.isLoaded(b) && lvl.getBlockState(b).isAir()) lvl.setBlock(b, f.f.bloqueMuro(q[0], q[1], q[2]), 3);
        }
        est.muro = false;
        poner(lvl, f);
        ESTADOS.clear();
    }

    /** Lo que se ve de una trampa para la prueba: el suelo que cede (bloques que quedan), etc. */
    public static String resumen(ServerLevel lvl, FortalezaPlan f) {
        int suelo = 0, muro = 0;
        for (int[] q : f.f.sueloQueCede()) if (!lvl.getBlockState(new BlockPos(q[0], q[1], q[2])).isAir()) suelo++;
        for (int[] q : f.f.muroFalso()) if (!lvl.getBlockState(new BlockPos(q[0], q[1], q[2])).isAir()) muro++;
        return "suelo que cede " + suelo + "/9, muro falso " + muro + "/18";
    }

}
