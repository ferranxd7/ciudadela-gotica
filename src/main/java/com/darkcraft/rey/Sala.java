package com.darkcraft.rey;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.story.Story;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * El salon del trono de una ciudadela: donde esta, el asiento del Rey y sus puertas (que se cierran con
 * rastrillos de hierro negro durante la pelea).
 */
public final class Sala {
    public final CastlePlan.Throne t;
    public final int castleX, castleZ;

    private Sala(CastlePlan.Throne t, int castleX, int castleZ) {
        this.t = t;
        this.castleX = castleX;
        this.castleZ = castleZ;
    }

    /** La sala del trono de la ciudadela cercana a (x, z), o null. */
    public static Sala near(ServerLevel lvl, int x, int z) {
        CastlePlan plan = Story.planNear(lvl, x, z);
        if (plan == null) return null;
        CastlePlan.Throne th = plan.throne();
        if (th == null) return null;
        return new Sala(th, plan.cx, plan.cz);
    }

    /** Clave de esta ciudadela en el estado guardado. */
    public String key() {
        return castleX + "," + castleZ;
    }

    public Vec3 seat() {
        return new Vec3(t.seatX() + 0.5, t.seatY(), t.seatZ() + 0.5);
    }

    /** Donde se queda el Rey al levantarse: delante del trono, al pie de la tarima. */
    public Vec3 front() {
        return new Vec3(t.seatX() + 0.5, t.y(), t.z0() + 7.5);
    }

    public Vec3 center() {
        return new Vec3((t.x0() + t.x1() + 1) / 2.0, t.y(), (t.z0() + t.z1() + 1) / 2.0);
    }

    public BlockPos chest() {
        return new BlockPos(t.chestX(), t.y(), t.chestZ());
    }

    public AABB box() {
        return new AABB(t.x0(), t.y() - 1, t.z0(), t.x1() + 1, t.y() + t.h(), t.z1() + 1);
    }

    /** La sala con sus muros, suelo y techo (lo que no se puede romper). */
    public AABB shell() {
        return new AABB(t.x0() - 1, t.y() - 1, t.z0() - 1, t.x1() + 2, t.y() + t.h() + 1, t.z1() + 2);
    }

    public boolean inside(Vec3 p, double margin) {
        return t.inside(p.x, p.y, p.z, margin);
    }

    public boolean inShell(BlockPos p) {
        return p.getX() >= t.x0() - 1 && p.getX() <= t.x1() + 1 && p.getZ() >= t.z0() - 1 && p.getZ() <= t.z1() + 1
                && p.getY() >= t.y() - 1 && p.getY() <= t.y() + t.h();
    }

    /** Lo alto del suelo (para los avisos): el suelo normal o la tarima. */
    public double floorAt(double x, double z) {
        int dx = (int) Math.floor(x) - t.seatX();
        int dz = (int) Math.floor(z) - t.z0();
        if (dz < 0) return t.y();
        if (dz <= 2 && Math.abs(dx) <= 5) return t.y() + 3;
        if (dz <= 3 && Math.abs(dx) <= 6) return t.y() + 2;
        if (dz <= 4 && Math.abs(dx) <= 7) return t.y() + 1;
        return t.y();
    }

    // ------------------------------------------------------------------ rastrillos

    private List<int[]> doors() {
        return t.doors();
    }

    private BlockState bars(int[] d) {
        // el muro va a lo largo de Z (puerta mirando a X): la reja tambien va a lo largo de Z
        Direction.Axis ax = d[3] == 1 ? Direction.Axis.Z : Direction.Axis.X;
        return DarkCraftMod.RASTRILLO.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, ax);
    }

    /**
     * Baja (step = cuantas filas desde arriba ya estan puestas) o sube los rastrillos un paso. Devuelve
     * true cuando ha terminado.
     */
    public boolean gateStep(ServerLevel lvl, int step, boolean close) {
        boolean any = false;
        for (int[] d : doors()) {
            int h = d[2];
            if (step >= h) continue;
            any = true;
            int y = close ? t.y() + h - 1 - step : t.y() + step;
            BlockPos p = new BlockPos(d[0], y, d[1]);
            if (close) {
                BlockState old = lvl.getBlockState(p);
                if (old.isAir() || old.canBeReplaced() || old.is(DarkCraftMod.RASTRILLO.get())) lvl.setBlock(p, bars(d), 3);
            } else if (lvl.getBlockState(p).is(DarkCraftMod.RASTRILLO.get())) {
                lvl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        if (!doors().isEmpty() && any) {
            int[] d0 = doors().get(0);
            Vec3 c = center();
            float pitch = close ? 0.55f : 0.75f;
            for (int[] d : doors())
                lvl.playSound(null, d[0] + 0.5, t.y() + 2, d[1] + 0.5, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.2f, pitch);
            if (step == 0) lvl.playSound(null, c.x, t.y() + 3, c.z, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.8f, 0.5f);
        }
        if (!any && close && !doors().isEmpty()) {
            for (int[] d : doors())
                lvl.playSound(null, d[0] + 0.5, t.y(), d[1] + 0.5, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.9f, 0.5f);
        }
        return !any;
    }

    /** Quita todos los rastrillos de golpe (al reiniciar o al cargar el mundo). */
    public void openAll(ServerLevel lvl) {
        for (int[] d : doors())
            for (int y = t.y(); y < t.y() + d[2]; y++) {
                BlockPos p = new BlockPos(d[0], y, d[1]);
                if (lvl.getBlockState(p).is(DarkCraftMod.RASTRILLO.get())) lvl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
    }

    /** Si un jugador esta en el hueco de una puerta, lo mete dentro antes de cerrar. */
    public Vec3 pushInside(Vec3 p) {
        for (int[] d : doors()) {
            double dx = p.x - (d[0] + 0.5), dz = p.z - (d[1] + 0.5);
            if (Math.abs(dx) < 1.6 && Math.abs(dz) < 1.6 && p.y < t.y() + d[2]) {
                Vec3 c = center();
                Vec3 dir = new Vec3(c.x - (d[0] + 0.5), 0, c.z - (d[1] + 0.5)).normalize();
                Vec3 in = new Vec3(d[0] + 0.5, t.y(), d[1] + 0.5).add(dir.scale(3));
                return in;
            }
        }
        return null;
    }
}
