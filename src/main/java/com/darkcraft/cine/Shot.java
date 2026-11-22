package com.darkcraft.cine;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Un plano de camara de la cinematica: la camara recorre una curva suave por unos puntos mientras
 * mira hacia otra curva de puntos. Lo calcula el servidor (que conoce el castillo) y lo usan los dos.
 */
public final class Shot {
    public final float t0, t1, fov, roll;
    public final int ease;          // 0 lineal, 1 suave al entrar y salir, 2 suave al salir
    public final double[] pos;      // x,y,z por punto
    public final double[] look;     // x,y,z por punto

    public Shot(float t0, float t1, float fov, float roll, int ease, double[] pos, double[] look) {
        this.t0 = t0;
        this.t1 = t1;
        this.fov = fov;
        this.roll = roll;
        this.ease = ease;
        this.pos = pos;
        this.look = look;
    }

    private static double easeOf(int e, double u) {
        u = Math.max(0, Math.min(1, u));
        return switch (e) {
            case 1 -> u * u * (3 - 2 * u);
            case 2 -> 1 - (1 - u) * (1 - u);
            default -> u;
        };
    }

    /** Catmull-Rom por los puntos (x,y,z) de pts. */
    private static void spline(double[] pts, double u, double[] out, int off) {
        int n = pts.length / 3;
        if (n == 1) {
            out[off] = pts[0];
            out[off + 1] = pts[1];
            out[off + 2] = pts[2];
            return;
        }
        double f = u * (n - 1);
        int i = Math.min(n - 2, (int) Math.floor(f));
        double t = f - i;
        int i0 = Math.max(0, i - 1), i1 = i, i2 = i + 1, i3 = Math.min(n - 1, i + 2);
        for (int k = 0; k < 3; k++) {
            double p0 = pts[i0 * 3 + k], p1 = pts[i1 * 3 + k], p2 = pts[i2 * 3 + k], p3 = pts[i3 * 3 + k];
            double t2 = t * t, t3 = t2 * t;
            out[off + k] = 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (-p0 + 3 * p1 - 3 * p2 + p3) * t3);
        }
    }

    /** {x, y, z, mirarX, mirarY, mirarZ} en el instante t (segundos desde el principio de la cinematica). */
    public double[] at(double t) {
        double u = easeOf(ease, (t - t0) / Math.max(0.001, t1 - t0));
        double[] o = new double[6];
        spline(pos, u, o, 0);
        spline(look, u, o, 3);
        return o;
    }

    /** {yaw, pitch} de Minecraft para mirar desde p hacia el objetivo. */
    public static float[] angles(double[] p) {
        double dx = p[3] - p[0], dy = p[4] - p[1], dz = p[5] - p[2];
        double h = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, h));
        return new float[]{yaw, pitch};
    }

    public void write(FriendlyByteBuf b) {
        b.writeFloat(t0);
        b.writeFloat(t1);
        b.writeFloat(fov);
        b.writeFloat(roll);
        b.writeByte(ease);
        b.writeVarInt(pos.length);
        for (double d : pos) b.writeDouble(d);
        b.writeVarInt(look.length);
        for (double d : look) b.writeDouble(d);
    }

    public static Shot read(FriendlyByteBuf b) {
        float t0 = b.readFloat(), t1 = b.readFloat(), fov = b.readFloat(), roll = b.readFloat();
        int ease = b.readByte();
        double[] pos = new double[Math.min(b.readVarInt(), 300)];
        for (int i = 0; i < pos.length; i++) pos[i] = b.readDouble();
        double[] look = new double[Math.min(b.readVarInt(), 300)];
        for (int i = 0; i < look.length; i++) look[i] = b.readDouble();
        return new Shot(t0, t1, fov, roll, ease, pos, look);
    }
}
