package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lienzo de muralla entre dos puntos: almenas, camino de ronda, galeria interior con troneras,
 * contrafuertes exteriores y estandartes.
 */
public class CurtainWall extends Element {
    private final double ax, az, bx, bz, len, ux, uz, nx, nz; // u = direccion, n = normal hacia fuera
    private final int groundY, height;
    private final double half;

    /** outX/outZ: un punto hacia el exterior (para orientar la normal). */
    public CurtainWall(long seed, int ax, int az, int bx, int bz, int groundY, int height, int thickness, int outX, int outZ) {
        super(seed);
        this.ax = ax;
        this.az = az;
        this.bx = bx;
        this.bz = bz;
        double dx = bx - ax, dz = bz - az;
        this.len = Math.sqrt(dx * dx + dz * dz);
        this.ux = dx / len;
        this.uz = dz / len;
        double nnx = -uz, nnz = ux;
        double mx = (ax + bx) / 2.0, mz = (az + bz) / 2.0;
        if ((outX - mx) * nnx + (outZ - mz) * nnz < 0) {
            nnx = -nnx;
            nnz = -nnz;
        }
        this.nx = nnx;
        this.nz = nnz;
        this.groundY = groundY;
        this.height = height;
        this.half = thickness / 2.0;
        int pad = thickness + 4;
        bounds(Math.min(ax, bx) - pad, groundY - 6, Math.min(az, bz) - pad, Math.max(ax, bx) + pad, groundY + height + 4, Math.max(az, bz) + pad);
    }

    @Override
    public void build(Canvas c) {
        int walk = groundY + height;          // nivel del camino de ronda (se pisa aqui)
        int galY = groundY + height / 2 - 2;  // suelo de la galeria interior
        boolean axisAligned = Math.abs(nx) < 1e-6 || Math.abs(nz) < 1e-6;
        Direction inward = Pal.dirOf((int) Math.round(-nx * 10), (int) Math.round(-nz * 10));
        for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
            for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) {
                double px = x + 0.5 - ax - 0.5, pz = z + 0.5 - az - 0.5;
                double t = px * ux + pz * uz;
                double s = px * nx + pz * nz;
                if (t < -half || t > len + half) continue;
                double as = Math.abs(s);
                if (as <= half) {
                    // cuerpo
                    c.column(x, z, groundY - 6, walk - 1, Pal::wall);
                    // galeria interior
                    if (as <= 1.5) {
                        c.column(x, z, galY, galY + 3, Pal.AIR);
                        c.set(x, galY - 1, z, Pal.DS_POLISHED);
                        if (Math.floorMod((int) Math.floor(t), 9) == 0 && as < 0.6) c.set(x, galY + 3, z, Pal.SOUL_LANTERN_HANG);
                    }
                    // troneras de la galeria hacia fuera
                    if (s > 1.5 && Math.floorMod((int) Math.floor(t), 8) == 4) {
                        c.set(x, galY + 1, z, Pal.AIR);
                        c.set(x, galY + 2, z, Pal.AIR);
                    }
                    // camino de ronda
                    c.set(x, walk - 1, z, (Math.floorMod((int) Math.floor(t), 6) == 0) ? Pal.DS_POLISHED : Pal.DS_TILES);
                    if (s > half - 1.3) {
                        // parapeto exterior con almenas
                        c.set(x, walk, z, Pal.wall(x, walk, z));
                        if (Math.floorMod((int) Math.floor(t / 2), 2) == 0) {
                            c.set(x, walk + 1, z, Pal.wall(x, walk + 1, z));
                            c.set(x, walk + 2, z, Pal.slab(Blocks.DEEPSLATE_BRICK_SLAB, false));
                        }
                    } else if (s < -half + 1.1) {
                        // pretil interior bajo
                        c.set(x, walk, z, Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false));
                        if (Math.floorMod((int) Math.floor(t), 12) == 6) {
                            c.set(x, walk, z, Pal.PB_BRICKS);
                            c.set(x, walk + 1, z, Pal.SOUL_LANTERN);
                        }
                    }
                    // canones en el camino de ronda
                    if (s > half - 2.3 && s <= half - 1.3 && Math.floorMod((int) Math.floor(t), 24) == 12) {
                        BlockState cannon = ModDeco.cannon(Pal.dirOf((int) Math.round(nx * 10), (int) Math.round(nz * 10)));
                        if (cannon != null) c.set(x, walk, z, cannon);
                        BlockState balls = Mods.state("supplementaries:cannonball", "balls=4");
                        if (balls != null) c.set(x - (int) Math.round(ux), walk, z - (int) Math.round(uz), balls);
                    }
                    // estandartes en la cara interior
                    if (axisAligned && s < -half + 0.9 && s >= -half && Math.floorMod((int) Math.floor(t), 16) == 8) {
                        int bxx = x + inward.getStepX(), bzz = z + inward.getStepZ();
                        c.set(bxx, walk - 3, bzz, Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, inward));
                    }
                } else if (s > half && s <= half + 2.0) {
                    // talud y contrafuertes exteriores
                    double ts = Math.floorMod((int) Math.floor(t), 12);
                    if (s <= half + 1.0) c.column(x, z, groundY - 6, groundY + 1, Pal::foundation);
                    if (ts >= 5 && ts <= 7 && t > 2 && t < len - 2) {
                        int topB = s <= half + 1.0 ? walk - 5 : groundY + height / 2;
                        c.column(x, z, groundY - 6, topB, Pal::wall);
                        c.set(x, topB + 1, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, inward, false));
                    }
                }
            }
    }
}
