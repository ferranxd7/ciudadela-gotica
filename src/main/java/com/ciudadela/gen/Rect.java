package com.ciudadela.gen;

/** Rectangulo en planta, coordenadas incluidas. */
public final class Rect {
    public final int x0, z0, x1, z1;

    public Rect(int x0, int z0, int x1, int z1) {
        this.x0 = Math.min(x0, x1);
        this.z0 = Math.min(z0, z1);
        this.x1 = Math.max(x0, x1);
        this.z1 = Math.max(z0, z1);
    }

    public int w() { return x1 - x0 + 1; }
    public int d() { return z1 - z0 + 1; }
    public int area() { return w() * d(); }
    public int cx() { return (x0 + x1) / 2; }
    public int cz() { return (z0 + z1) / 2; }

    public boolean contains(int x, int z) {
        return x >= x0 && x <= x1 && z >= z0 && z <= z1;
    }

    public boolean intersects(Rect o) {
        return o.x1 >= x0 && o.x0 <= x1 && o.z1 >= z0 && o.z0 <= z1;
    }

    public Rect grow(int n) {
        return new Rect(x0 - n, z0 - n, x1 + n, z1 + n);
    }

    @Override
    public String toString() {
        return "[" + x0 + "," + z0 + " .. " + x1 + "," + z1 + "]";
    }
}
