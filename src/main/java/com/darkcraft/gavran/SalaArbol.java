package com.darkcraft.gavran;

import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.TreeTower;
import com.darkcraft.story.Story;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * La Sala del Arbol vista desde la pelea con Ser Gavran: un anillo de suelo entre el tronco (radio ~17) y el
 * muro (radio 40), con la puerta del tronco al norte, la tarima del trono detras y los balcones a media altura.
 */
public record SalaArbol(int cx, int cz, int F, int base) {
    /** Radio del tronco (con margen) y del muro por dentro: Gavran anda entre los dos. */
    public static final double R_TRONCO = TreeTower.TRUNK_R + 1.2, R_MURO = TreeTower.HALL_R - 2.2;

    public static SalaArbol de(CastlePlan plan) {
        int[] h = plan == null ? null : plan.treeHall();
        return h == null ? null : new SalaArbol(h[0], h[1], h[2], h[4]);
    }

    public static SalaArbol cerca(ServerLevel sl, double x, double z) {
        return de(Story.planNear(sl, (int) Math.floor(x), (int) Math.floor(z)));
    }

    /** Clave del estado guardado (ReyEstado): dormido, peleando o muerto. */
    public String key() {
        return "gavran@" + cx + "," + cz;
    }

    public double ccx() {
        return cx + 0.5;
    }

    public double ccz() {
        return cz + 0.5;
    }

    public double dist(double x, double z) {
        return Math.hypot(x - ccx(), z - ccz());
    }

    /** Angulo (radianes) de un punto respecto al centro del arbol. */
    public double ang(double x, double z) {
        return Math.atan2(z - ccz(), x - ccx());
    }

    public Vec3 enAnillo(double ang, double r, double y) {
        return new Vec3(ccx() + Math.cos(ang) * r, y, ccz() + Math.sin(ang) * r);
    }

    /** Dentro de la sala (fuera del tronco, pasada la puerta; dentro del muro; del suelo a la cupula). */
    public boolean dentro(Vec3 p) {
        double d = dist(p.x, p.z);
        return d >= 15.6 && d <= TreeTower.HALL_R - 1.0 && p.y >= F - 1.5 && p.y <= F + TreeTower.HALL_H + 14;
    }

    /** En el umbral de la puerta (donde se pone la barrera): hay que empujarle hacia dentro. */
    public boolean enUmbral(Vec3 p) {
        return Math.abs(p.x - ccx()) <= 4.2 && p.z <= cz - 13.0 && p.z >= cz - 16.2 && p.y >= F - 1 && p.y <= F + 11;
    }

    /** Caja que cubre toda la sala (para buscar entidades). */
    public AABB caja() {
        double R = TreeTower.HALL_R + 1;
        return new AABB(ccx() - R, F - 3, ccz() - R, ccx() + R, F + TreeTower.HALL_H + 16, ccz() + R);
    }

    /** Altura del suelo en (x, z): la sala es plana salvo la tarima del trono, al norte. */
    public double suelo(ServerLevel sl, double x, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z);
        for (int y = F + 3; y >= F - 1; y--) {
            BlockState s = sl.getBlockState(new BlockPos(bx, y, bz));
            if (!s.isAir() && s.isCollisionShapeFullBlock(sl, new BlockPos(bx, y, bz)) || s.is(BlockTags.STAIRS))
                return y + 1;
        }
        return F;
    }

    /** El punto mas cercano del anillo donde puede estar Gavran (no dentro del tronco ni del muro). */
    public Vec3 alAnillo(Vec3 p, double margen) {
        double d = dist(p.x, p.z);
        double r = Mth.clamp(d, R_TRONCO + margen, R_MURO - margen);
        double a = ang(p.x, p.z);
        return new Vec3(ccx() + Math.cos(a) * r, p.y, ccz() + Math.sin(a) * r);
    }

    /** Donde cae tras bajar del arbol: en la tarima, delante del trono (la piedra que llora). */
    public Vec3 aterrizaje() {
        return new Vec3(ccx(), F + 2, cz - TreeTower.HALL_R + 7 + 0.5);
    }

    /** Su sitio en el arbol: encima de la rama del norte, sobre la puerta (se busca la madera de la rama). */
    public Vec3 rama(ServerLevel sl) {
        int x = cx, z = cz - 20;
        for (int y = F + 36; y >= F + 20; y--) {
            BlockState s = sl.getBlockState(new BlockPos(x, y, z));
            if (s.is(BlockTags.LOGS) || s.is(BlockTags.OVERWORLD_NATURAL_LOGS))
                return new Vec3(x + 0.5, y + 1, z + 0.5);
        }
        return new Vec3(x + 0.5, F + 31, z + 0.5);
    }

    /** Hojas, lianas y luces de hongo que cuelgan en la sala (lo unico que se quita para que se le vea). */
    private static boolean follaje(BlockState s) {
        return s.is(BlockTags.LEAVES) || s.is(Blocks.CAVE_VINES) || s.is(Blocks.CAVE_VINES_PLANT) || s.is(Blocks.WEEPING_VINES)
                || s.is(Blocks.WEEPING_VINES_PLANT) || s.is(Blocks.TWISTING_VINES) || s.is(Blocks.TWISTING_VINES_PLANT)
                || s.is(Blocks.VINE) || s.is(Blocks.GLOW_LICHEN) || s.is(Blocks.SHROOMLIGHT) || s.is(Blocks.HANGING_ROOTS)
                || s.is(Blocks.SPORE_BLOSSOM) || s.is(Blocks.MOSS_CARPET);
    }

    /** Despeja las hojas y lianas alrededor de la rama y delante de el (para que se le vea entero desde la sala). */
    public void despejarRama(ServerLevel sl, Vec3 r) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -5; dx <= 5; dx++)
            for (int dz = -11; dz <= 3; dz++)
                for (int dy = -7; dy <= 7; dy++) {
                    m.set(Mth.floor(r.x) + dx, Mth.floor(r.y) + dy, Mth.floor(r.z) + dz);
                    if (follaje(sl.getBlockState(m))) sl.setBlock(m, Blocks.AIR.defaultBlockState(), 2);
                }
    }

    /**
     * Despeja el follaje entre la camara de un plano y lo que mira (pos y look: x,y,z por punto, como en Shot): un
     * tubo de tres bloques de ancho por cada par de puntos y por los de en medio.
     */
    public void despejarVista(ServerLevel sl, double[] pos, double[] look) {
        int np = pos.length / 3, nl = look.length / 3, n = Math.max(np, nl);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int k = 0; k < (n - 1) * 4 + 1; k++) {
            double f = k / 4.0;
            Vec3 a = punto(pos, f * (np - 1) / Math.max(1, n - 1)), b = punto(look, f * (nl - 1) / Math.max(1, n - 1));
            Vec3 d = b.subtract(a);
            double len = d.length();
            // hasta un poco antes de el (no se toca la rama en la que esta)
            for (double u = 0; u <= len - 1.5; u += 0.6) {
                Vec3 q = a.add(d.scale(u / len));
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dz = -1; dz <= 1; dz++) {
                            m.set(Mth.floor(q.x) + dx, Mth.floor(q.y) + dy, Mth.floor(q.z) + dz);
                            if (dist(m.getX() + 0.5, m.getZ() + 0.5) < TreeTower.TRUNK_R + 0.5) continue;
                            if (follaje(sl.getBlockState(m))) sl.setBlock(m, Blocks.AIR.defaultBlockState(), 2);
                        }
            }
        }
    }

    /** Punto de una lista x,y,z (i puede tener decimales: se interpola en linea recta entre dos puntos). */
    private static Vec3 punto(double[] v, double i) {
        int n = v.length / 3;
        int a = Mth.clamp((int) Math.floor(i), 0, n - 1), b = Math.min(n - 1, a + 1);
        double t = Mth.clamp(i - a, 0, 1);
        return new Vec3(Mth.lerp(t, v[a * 3], v[b * 3]), Mth.lerp(t, v[a * 3 + 1], v[b * 3 + 1]), Mth.lerp(t, v[a * 3 + 2], v[b * 3 + 2]));
    }

    /** Puerta de la sala (en el tronco, al norte): centro del umbral. */
    public Vec3 puerta() {
        return new Vec3(ccx(), F, cz - 14.8);
    }
}
