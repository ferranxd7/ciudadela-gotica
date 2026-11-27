package com.darkcraft.cine;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * La cinematica del final (parte 7ad): cuando la Sombra sin Nombre se deshace en su aguja. Dos versiones, segun lo que se hizo
 * con su corazon: {@link #ALBA} (se golpeo: el rojo se va de las nubes y amanece por primera vez en mil inviernos) y
 * {@link #SOMBRA} (se tomo: las nubes vuelven a ser grises y los fuegos arden frios). Los mismos cuatro planos en las dos:
 * <ol>
 * <li>la sala de la aguja: la Sombra que se deshace (en la del alba la camara mira como sube y se va; en la otra, acaba en quien
 * se quedo su corazon, con la oscuridad alrededor);</li>
 * <li>fuera, la aguja contra el ocaso: alli cambia el cielo de Morvain (lo pinta {@code client/morvain/CieloFinal} desde
 * {@link #CIELO});</li>
 * <li>la isla de la ciudadela colgada de sus cadenas, desde lejos: habla Maelis (su frase 17 o la 21, en off) y entra la
 * musica de los creditos;</li>
 * <li>por encima de todo: hacia el horizonte (el alba; acaba en una luz calida) o mirando abajo, a la ciudadela que ahora es
 * suya (la nueva Sombra); y a negro.</li>
 * </ol>
 * Al acabar (o al saltarla) salen los creditos ({@code client/ui/CreditosScreen}) y despues Maelis, junto a la Ultima Llama
 * ({@code morvain/Epilogo}). Aqui estan los tiempos (los usan el servidor y el cliente, {@code client/Guion}) y los planos.
 */
public final class Desenlace {
    private Desenlace() {}

    public static final int ALBA = 43, SOMBRA = 44;

    /** Los cortes entre planos (s): cada plano empieza en P[i] y acaba en Q[i]; entre uno y otro, negro. */
    public static final float[] P = {0f, 7.0f, 16.1f, 28.5f}, Q = {6.4f, 15.6f, 28.0f, 38.5f};
    /** El final entero; a negro desde {@link #NEGRO} (en la del alba, antes una luz calida desde {@link #LUZ}). */
    public static final float FIN = 38.6f, NEGRO = 36.9f, LUZ = 33.6f;
    /** Cuando empieza a cambiar el cielo de Morvain y lo que tarda (s). */
    public static final float CIELO = 8.0f, FUNDIDO = 9.0f;
    /** Cuando entra la musica de los creditos y cuando habla Maelis. */
    public static final float MUSICA = 16.1f, VOZ = 17.0f;
    /** La frase de Maelis de cada final (y lo que dura, por si no se sabe). */
    public static final int VOZ_ALBA = 17, VOZ_SOMBRA = 21;
    public static final float DURA_ALBA = 7.8f, DURA_SOMBRA = 6.5f;

    public static boolean es(int id) {
        return id == ALBA || id == SOMBRA;
    }

    /** 1 el alba, 2 la nueva Sombra (0 si no es la del final). */
    public static int cual(int id) {
        return id == ALBA ? 1 : id == SOMBRA ? 2 : 0;
    }

    public static int id(int cual) {
        return cual == 2 ? SOMBRA : ALBA;
    }

    public static int voz(int id) {
        return id == SOMBRA ? VOZ_SOMBRA : VOZ_ALBA;
    }

    /** El plano que se ve en {@code t} (-1 si ninguno: entre dos, en negro). */
    public static int plano(float t) {
        for (int i = 0; i < P.length; i++) if (t >= P[i] && t < Q[i]) return i;
        return -1;
    }

    /**
     * Los cuatro planos. {@code sombra}: donde estaba la Sombra (sus pies); {@code quien}: los pies del que eligio (o del jugador
     * que la ve); {@code pies}: lo que se pisa en la sala de la aguja (la azotea).
     */
    public static List<Shot> planos(int id, Vec3 sombra, Vec3 quien, double pies) {
        boolean alba = id != SOMBRA;
        List<Shot> l = new ArrayList<>();
        // ---- 1: la sala. La camara de lado (entre la Sombra y quien la mira), dentro de la sala y lejos de los pilares
        double sx = sombra.x, sy = sombra.y, sz = sombra.z;
        double ux = quien.x - sx, uz = quien.z - sz, d = Math.hypot(ux, uz);
        if (d < 0.5) {
            ux = 0;
            uz = 1;
        } else {
            ux /= d;
            uz /= d;
        }
        double[] c0 = dentro(lado(sx, sz, ux, uz, 62, 7.2), 9.2), c1 = dentro(lado(sx, sz, ux, uz, alba ? 96 : 30, alba ? 6.0 : 5.6), 9.2);
        double[] pos1 = {c0[0], pies + 2.1, c0[1], (c0[0] + c1[0]) / 2, pies + 2.6, (c0[1] + c1[1]) / 2, c1[0], pies + (alba ? 3.4 : 2.4), c1[1]};
        double[] mira1 = alba
                ? new double[]{sx, sy + 3.0, sz, sx, sy + 4.4, sz, sx, sy + 7.5, sz}
                : new double[]{sx, sy + 3.0, sz, (sx + quien.x) / 2, sy + 2.4, (sz + quien.z) / 2, quien.x, quien.y + 1.3, quien.z};
        l.add(new Shot(P[0], Q[0], alba ? 62f : 58f, 0f, 1, pos1, mira1));
        // ---- 2: la aguja contra el ocaso (el sol se pone al oeste, -x: detras de la aguja), subiendo y alejandose
        double y = pies;
        l.add(new Shot(P[1], Q[1], 66f, 0f, 1,
                new double[]{40, y + 3, 6, 46, y + 9, 10, 52, y + 15, 14},
                new double[]{0, y + 23, 0, 0, y + 31, 0, 0, y + 39, 0}));
        // ---- 3: la isla colgada de sus cadenas, desde lejos (al sudeste: el ocaso a la izquierda)
        l.add(new Shot(P[2], Q[2], 64f, 0f, 1,
                new double[]{96, y - 27, 118, 78, y - 22, 130, 60, y - 19, 140},
                new double[]{0, y - 25, 0, 0, y - 18, 0, 0, y - 13, 0}));
        // ---- 4: por encima de todo
        if (alba) l.add(new Shot(P[3], Q[3], 74f, 0f, 1,
                new double[]{20, y + 66, 40, 15, y + 96, 55, 10, y + 126, 70},
                new double[]{-300, y - 24, -20, -350, y + 20, -5, -400, y + 96, 10}));
        else l.add(new Shot(P[3], Q[3], 70f, 0f, 1,
                new double[]{34, y + 70, 40, 0, y + 100, 52, -34, y + 130, 40},
                new double[]{0, y, 0, 0, y, 0, 0, y, 0}));
        return l;
    }

    /** Un punto a {@code r} de (x, z), girado {@code grados} desde la direccion (ux, uz). */
    private static double[] lado(double x, double z, double ux, double uz, double grados, double r) {
        double a = Math.toRadians(grados), c = Math.cos(a), s = Math.sin(a);
        return new double[]{x + (ux * c - uz * s) * r, z + (ux * s + uz * c) * r};
    }

    /** Que no se salga de la sala (ni se meta en los pilares): como mucho a {@code r} del centro. */
    private static double[] dentro(double[] p, double r) {
        double d = Math.hypot(p[0], p[1]);
        if (d <= r) return p;
        return new double[]{p[0] * r / d, p[1] * r / d};
    }
}
