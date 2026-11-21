package com.ciudadela.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.cine.Cinematics;
import com.ciudadela.cine.Shot;
import com.ciudadela.gen.CastlePlan;
import com.ciudadela.net.Net;
import com.ciudadela.story.Story;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * El sello de la Sala del Arbol: una barrera de runas rojas alrededor de la salida de la escalera que
 * solo se rompe con la Llave del Arbol (la guardaba el Rey). La sala tampoco se puede romper.
 */
public final class Sello {
    private Sello() {}

    private static final int RING = 4;

    public static String key(CastlePlan plan) {
        return "sello@" + plan.cx + "," + plan.cz;
    }

    public static boolean enSalaArbol(ServerLevel sl, BlockPos p) {
        CastlePlan plan = Story.planNear(sl, p.getX(), p.getZ());
        if (plan == null) return false;
        int[] h = plan.treeHall();
        if (h == null) return false;
        int dy = p.getY() - h[2];
        if (dy < -1 || dy > 40) return false;
        double dx = p.getX() + 0.5 - h[0], dz = p.getZ() + 0.5 - h[1];
        return dx * dx + dz * dz <= (h[3] + 1.5) * (h[3] + 1.5);
    }

    private static boolean esSello(BlockState st) {
        return st.is(CiudadelaMod.SELLO.get());
    }

    /** Pone el sello si falta (y no se ha abierto ya en este mundo). */
    public static void asegurar(ServerLevel ow, CastlePlan plan) {
        int[] h = plan.treeHall();
        if (h == null || ReyEstado.get(ow.getServer()).selloAbierto(key(plan))) return;
        int cx = h[0], cz = h[1], F = h[2];
        if (!ow.isLoaded(new BlockPos(cx, F, cz)) || !ow.isLoaded(new BlockPos(cx + RING, F, cz + RING)) || !ow.isLoaded(new BlockPos(cx - RING, F, cz - RING)))
            return;
        if (esSello(ow.getBlockState(new BlockPos(cx, F + RING, cz)))) return;
        BlockState s = CiudadelaMod.SELLO.get().defaultBlockState();
        for (int dx = -RING; dx <= RING; dx++)
            for (int dz = -RING; dz <= RING; dz++) {
                boolean borde = Math.max(Math.abs(dx), Math.abs(dz)) == RING;
                for (int y = F; y <= F + RING; y++) {
                    if (!borde && y < F + RING) continue;
                    BlockPos p = new BlockPos(cx + dx, y, cz + dz);
                    BlockState old = ow.getBlockState(p);
                    if (old.isAir() || old.canBeReplaced() || old.is(Blocks.RED_CARPET) || old.is(Blocks.BLACK_CARPET)) ow.setBlock(p, s, 2);
                }
            }
        CiudadelaMod.LOGGER.info("[Ciudadela] Sello de la Sala del Arbol puesto en {},{},{}", cx, F, cz);
    }

    // ------------------------------------------------------------------ abrirlo con la llave

    private record Rotura(ServerLevel lvl, int cx, int cz, int F, int start) {}

    private static final List<Rotura> ROTURAS = new ArrayList<>();
    private static int tick;

    public static boolean usarLlave(ServerPlayer sp, BlockPos pos, ItemStack llave) {
        ServerLevel sl = sp.serverLevel();
        if (!esSello(sl.getBlockState(pos))) {
            sp.displayClientMessage(Component.translatable("item.ciudadela.llave_arbol.aqui_no"), true);
            return false;
        }
        CastlePlan plan = Story.planNear(sl, pos.getX(), pos.getZ());
        if (plan == null || plan.treeHall() == null) return false;
        int[] h = plan.treeHall();
        ReyEstado.get(sl.getServer()).abrirSello(key(plan));
        if (!sp.isCreative()) llave.shrink(1);
        // la llave entra en el sello por la cara que mira al jugador
        Vec3 eye = sp.getEyePosition();
        Vec3 c = Vec3.atCenterOf(pos);
        Vec3 n = eye.subtract(c);
        Vec3 face;
        if (Math.abs(n.x) > Math.abs(n.z) && Math.abs(n.x) > Math.abs(n.y)) face = new Vec3(Math.signum(n.x), 0, 0);
        else if (Math.abs(n.z) > Math.abs(n.y)) face = new Vec3(0, 0, Math.signum(n.z));
        else face = new Vec3(0, Math.signum(n.y), 0);
        Vec3 lock = c.add(face.scale(0.52));
        Net.Llave m = new Net.Llave(lock.x, lock.y, lock.z, 70);
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(lock) < 60 * 60) Net.to(p, m);
        // mini cinematica: la camara delante del sello
        Vec3 side = new Vec3(-face.z, 0, face.x);
        if (side.lengthSqr() < 0.1) side = new Vec3(1, 0, 0);
        Vec3 a = lock.add(face.scale(2.2)).add(side.scale(0.9)).add(0, 0.35, 0);
        Vec3 b = lock.add(face.scale(3.6)).add(side.scale(-0.6)).add(0, 1.0, 0);
        List<Shot> shots = List.of(new Shot(0f, 5.6f, 62, 0, 1,
                new double[]{a.x, a.y, a.z, b.x, b.y, b.z},
                new double[]{lock.x, lock.y, lock.z, lock.x - face.x * 4, lock.y + 0.8, lock.z - face.z * 4}));
        Cinematics.playShots(sp, Cinematics.SELLO, shots, 6.2f);
        sl.playSound(null, lock.x, lock.y, lock.z, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, 0.5f);
        ROTURAS.add(new Rotura(sl, h[0], h[1], h[2], tick));
        return true;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        tick++;
        if (ROTURAS.isEmpty()) return;
        Iterator<Rotura> it = ROTURAS.iterator();
        DustParticleOptions rojo = new DustParticleOptions(new Vector3f(1f, 0.1f, 0.05f), 1.5f);
        while (it.hasNext()) {
            Rotura r = it.next();
            int t = tick - r.start();
            ServerLevel sl = r.lvl();
            if (t == 24) sl.playSound(null, r.cx(), r.F() + 2, r.cz(), SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.2f, 0.6f);
            if (t == 40) {
                sl.playSound(null, r.cx(), r.F() + 2, r.cz(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.5f, 0.5f);
                sl.sendParticles(rojo, r.cx() + 0.5, r.F() + 2, r.cz() + 0.5, 120, 3, 2, 3, 0.05);
            }
            // se rompe de arriba abajo
            if (t >= 64 && t < 64 + 5 * 4 && (t - 64) % 4 == 0) {
                int y = r.F() + RING - (t - 64) / 4;
                for (int dx = -RING; dx <= RING; dx++)
                    for (int dz = -RING; dz <= RING; dz++) {
                        BlockPos p = new BlockPos(r.cx() + dx, y, r.cz() + dz);
                        if (!esSello(sl.getBlockState(p))) continue;
                        sl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.RED_STAINED_GLASS.defaultBlockState()),
                                p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.1);
                        sl.sendParticles(rojo, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.02);
                    }
                sl.playSound(null, r.cx(), y, r.cz(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.6f, 0.6f + (t - 64) * 0.01f);
                sl.playSound(null, r.cx(), y, r.cz(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.2f, 0.5f);
            }
            if (t >= 90) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, r.cx() + 0.5, r.F() + 1, r.cz() + 0.5, 40, 3, 1, 3, 0.02);
                it.remove();
            }
        }
    }
}
