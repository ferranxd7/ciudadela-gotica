package com.ciudadela.gen;

import net.minecraft.core.Direction;

/** Cofre de la cronica con su cartel al lado. Se construye el ultimo para que nada lo tape. */
public final class StoryChest extends Element {
    public final int chapter, x, y, z, sx, sz;
    private final Direction facing, signFront;

    public StoryChest(long seed, int chapter, int x, int y, int z, Direction facing, int sx, int sz, Direction signFront) {
        super(seed);
        this.layer = 6;
        this.chapter = chapter;
        this.x = x;
        this.y = y;
        this.z = z;
        this.facing = facing;
        this.sx = sx;
        this.sz = sz;
        this.signFront = signFront;
        bounds(Math.min(x, sx) - 1, y - 1, Math.min(z, sz) - 1, Math.max(x, sx) + 1, y + 3, Math.max(z, sz) + 1);
    }

    @Override
    public void build(Canvas c) {
        for (int[] p : new int[][]{{x, z}, {sx, sz}}) {
            c.column(p[0], p[1], y, y + 2, Pal.AIR);
            if (c.inXZ(p[0], p[1]) && c.isAir(p[0], y - 1, p[1])) c.set(p[0], y - 1, p[1], Pal.PB_BRICKS);
        }
        c.storyChest(x, y, z, facing, chapter);
        c.storySign(sx, y, sz, signFront, chapter);
    }
}
