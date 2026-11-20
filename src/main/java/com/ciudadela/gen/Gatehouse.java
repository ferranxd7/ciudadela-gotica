package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

/** Puerta fortificada: pasaje abovedado con rastrillo y salas de guardia encima. El pasaje va en el eje Z. */
public class Gatehouse extends Building {
    private final int cx, cz, groundY, passH, halfW, halfD;

    public Gatehouse(long seed, int cx, int cz, int groundY, int halfW, int halfD, int passH, int[] floors) {
        super(seed, new Rect(cx - halfW, cz - halfD, cx + halfW, cz + halfD), groundY + passH, floors, 0, 6, Roof.TERRACE, 1,
                simple(6, 14, new Room.Type[]{Room.Type.STORAGE},
                        new Room.Type[]{Room.Type.GALLERY, Room.Type.ARMORY},
                        new Room.Type[]{Room.Type.BARRACKS, Room.Type.STORAGE}),
                Direction.WEST, Direction.EAST);
        this.cx = cx;
        this.cz = cz;
        this.groundY = groundY;
        this.passH = passH;
        this.halfW = halfW;
        this.halfD = halfD;
        turret(-1);
        this.y0 = Math.min(this.y0, groundY - 6);
    }

    @Override
    protected void extra(Canvas c) {
        int top = groundY + passH - 2;
        for (int x = cx - halfW; x <= cx + halfW; x++)
            for (int z = cz - halfD - 1; z <= cz + halfD + 1; z++) {
                if (!c.inXZ(x, z)) continue;
                int adx = Math.abs(x - cx);
                boolean inFp = z >= cz - halfD && z <= cz + halfD;
                if (inFp) c.column(x, z, groundY - 6, top, (a, b, cc) -> Pal.wall(a, b, cc));
                if (adx <= 4) {
                    int archTop = top - 1 - Math.max(0, adx - 2);
                    c.set(x, groundY - 1, z, Landscape.road(x, groundY, z));
                    c.column(x, z, groundY, archTop, Pal.AIR);
                    if (adx == 4 && inFp && Math.floorMod(z - cz, 5) == 0) c.set(x, groundY + 4, z, Pal.SOUL_LANTERN);
                    // rastrillo levantado y agujeros asesinos
                    if (z == cz + halfD - 2 || z == cz - halfD + 2) {
                        for (int y = archTop - 3; y <= archTop; y++) c.set(x, y, z, Pal.connected(Blocks.IRON_BARS, Direction.Axis.X));
                    }
                    if (adx <= 2 && Math.floorMod(z - cz, 4) == 0 && inFp) c.set(x, archTop + 1, z, Pal.connected(Blocks.IRON_BARS, Direction.Axis.X));
                } else if (adx == 5 && (z == cz - halfD - 1 || z == cz + halfD + 1)) {
                    c.column(x, z, groundY, top, Pal.PB_CHISELED);
                }
            }
    }
}
