package com.ciudadela.gen;

/** Pieza del castillo con caja envolvente. build() se llama una vez por cada chunk que toca. */
public abstract class Element {
    public int x0, y0, z0, x1, y1, z1;
    protected final long seed;
    /** Orden de construccion: 0 isla, 1 suelos especiales, 2 arboles, 3 edificios, 4 detalles, 5 puentes. */
    public int layer = 3;

    protected Element(long seed) {
        this.seed = seed;
    }

    protected final void bounds(int ax, int ay, int az, int bx, int by, int bz) {
        x0 = Math.min(ax, bx);
        x1 = Math.max(ax, bx);
        y0 = Math.min(ay, by);
        y1 = Math.max(ay, by);
        z0 = Math.min(az, bz);
        z1 = Math.max(az, bz);
    }

    public Element layer(int l) {
        this.layer = l;
        return this;
    }

    public boolean touchesChunk(int cx, int cz) {
        int mx = cx << 4, mz = cz << 4;
        return x1 >= mx && x0 <= mx + 15 && z1 >= mz && z0 <= mz + 15;
    }

    public abstract void build(Canvas c);
}
