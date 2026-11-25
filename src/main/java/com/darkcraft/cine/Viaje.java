package com.darkcraft.cine;

import com.darkcraft.pueblo.gen.Barco;

import java.util.ArrayList;
import java.util.List;

/**
 * Los viajes de La Garza Gris (parte 3d): los tiempos, por donde vuela el barco y los planos de camara. Lo usan igual el
 * servidor (manda los planos, quita el barco de bloques de un amarre y lo pone en el otro mientras la pantalla esta en
 * negro) y el cliente ({@code client/Guion}: el barco que vuela es una entidad que solo existe en el cliente y se mueve con
 * {@link #barco}, la voz, los efectos y la musica).
 * <p>
 * Tres cinematicas: {@link #PRIMERO} (el primer viaje a Valdrenn: la Sombra habla mientras se llega), {@link #NORMAL}
 * (ir o volver las demas veces) y {@link #LLAMADA} (el barco acude a la campana del muelle donde no esta).
 * <p>
 * <b>Como vuela</b>: sale subiendo en vertical por encima de los faroles del muelle y avanza hacia el sur (la proa);
 * llega del sur con la proa al norte, se detiene sobre su amarre, se da la vuelta en el aire y baja. Los dos amarres son
 * iguales (el mismo barco desplazado, proa al sur, el muelle al este), asi que todo esta escrito en las coordenadas del
 * barco en Brumavela y se desplaza ({@code o}: de donde sale; {@code d}: a donde llega).
 */
public final class Viaje {
    private Viaje() {}

    public static final int PRIMERO = 40, NORMAL = 41, LLAMADA = 42;

    /** El punto del barco que se mueve: el pie del palo, en la cubierta (coordenadas de Brumavela). */
    public static final double PX = Barco.BX + 0.5, PY = Barco.CUBIERTA, PZ = Barco.PALO + 0.5;
    /** Lo que sube antes de avanzar, y la altura a la que se da la vuelta al llegar (el casco libra los faroles). */
    public static final double SUBE = 11, VUELTA = 12;

    /** Los tiempos de un viaje, en segundos desde que empieza. */
    public static final class Tiempos {
        public final int tipo;
        /** Cuando el barco de bloques deja paso al que vuela (en negro); -1 si ya llega volando (la llamada). */
        public float cambioA = -1;
        /** Cuando empieza a subir, lo que tarda, y cuando empieza a avanzar. */
        public float sube, tSube, avanza;
        /** El negro del salto al otro amarre: de cuando a cuando, y en que momento se lleva alli a los jugadores. */
        public float corteB0 = -1, corteB1 = -1, salto = -1;
        /** La llegada: cuando empieza, lo que va a velocidad de crucero, lo que tarda en frenar, en girar y en bajar. */
        public float llega, crucero, frena, gira, baja, velocidad;
        public double alto;
        /** Cuando el que vuela vuelve a ser de bloques (en negro), cuando empieza el ultimo plano y cuando acaba. */
        public float cambioC, ultimo, fin;
        /** Cuando empieza cada plano y cuando acaba (uno detras de otro, sin huecos). */
        public float[] plano;
        /** Las frases de la Sombra: {numero, segundo en que empieza}. */
        public float[][] voz = new float[0][];

        Tiempos(int tipo) {
            this.tipo = tipo;
        }

        /** Cuando toca su amarre. */
        public float toca() {
            return llega + crucero + frena + gira + baja;
        }

        /** A que distancia aparece viniendo. */
        public double lejos() {
            return velocidad * crucero + velocidad * frena / 2.0;
        }

        /** Lo que dura la cinematica entera. */
        public float total() {
            return fin + 0.6f;
        }
    }

    private static final Tiempos T_PRIMERO, T_NORMAL, T_LLAMADA;

    static {
        Tiempos t = new Tiempos(PRIMERO);
        t.cambioA = 4.95f;
        t.sube = 6.0f;
        t.tSube = 6.5f;
        t.avanza = 11.4f;
        t.corteB0 = 20.0f;
        t.corteB1 = 22.2f;
        t.salto = 20.35f;
        t.llega = 22.2f;
        t.velocidad = 7f;
        t.crucero = 9.8f;
        t.frena = 7.0f;
        t.gira = 5.8f;
        t.baja = 3.2f;
        t.alto = 16;
        t.cambioC = 48.95f;
        t.ultimo = 49.6f;
        t.fin = 54.8f;
        // el timon | se eleva | las velas | Valdrenn | los juramentos | atraca | en cubierta
        t.plano = new float[]{0f, 5.1f, 13.4f, 21.1f, 30.8f, 39.0f, 49.1f, 54.8f};
        t.voz = new float[][]{{7, 23.0f}, {8, 31.2f}, {9, 39.8f}};
        T_PRIMERO = t;

        t = new Tiempos(NORMAL);
        t.cambioA = 3.1f;
        t.sube = 4.0f;
        t.tSube = 5.2f;
        t.avanza = 8.2f;
        t.corteB0 = 13.6f;
        t.corteB1 = 15.8f;
        t.salto = 13.95f;
        t.llega = 15.8f;
        t.velocidad = 8f;
        t.crucero = 0f;
        t.frena = 5.0f;
        t.gira = 4.4f;
        t.baja = 3.0f;
        t.alto = 15;
        t.cambioC = 28.95f;
        t.ultimo = 29.6f;
        t.fin = 34.0f;
        // el timon | se eleva | las velas | atraca | en cubierta
        t.plano = new float[]{0f, 3.25f, 9.6f, 14.7f, 29.1f, 34.0f};
        T_NORMAL = t;

        t = new Tiempos(LLAMADA);
        t.llega = 0.4f;
        t.velocidad = 8f;
        t.crucero = 2.6f;
        t.frena = 5.0f;
        t.gira = 4.4f;
        t.baja = 3.0f;
        t.alto = 15;
        t.cambioC = 16.15f;
        t.ultimo = 16.8f;
        t.fin = 19.0f;
        // llega | amarrado
        t.plano = new float[]{0f, 16.3f, 19.0f};
        T_LLAMADA = t;
    }

    public static boolean es(int capitulo) {
        return capitulo >= PRIMERO && capitulo <= LLAMADA;
    }

    /**
     * En que segundo llega el cliente a cada corte en negro en que cambia el mundo (1: el barco de bloques deja paso al que
     * vuela; 2: el salto al otro amarre; 3: vuelve a ser de bloques); -1 si ese viaje no lo tiene. Ahi el cliente avisa al
     * servidor y <b>espera en negro</b> (el reloj de la cinematica se para) hasta ver hecho lo que toca: asi da igual que
     * el servidor o el cliente vayan con retraso (al llegar a un sitio nuevo se generan muchos chunks).
     */
    public static float corte(Tiempos T, int k) {
        if (k == 1) return T.cambioA < 0 ? -1 : T.cambioA - 0.1f;
        if (k == 2) return T.corteB0 < 0 ? -1 : T.corteB0 + 0.32f;
        return T.cambioC - 0.1f;
    }

    public static Tiempos tiempos(int tipo) {
        return tipo == PRIMERO ? T_PRIMERO : tipo == LLAMADA ? T_LLAMADA : T_NORMAL;
    }

    private static double suave(double v) {
        v = v < 0 ? 0 : Math.min(1, v);
        return v * v * (3 - 2 * v);
    }

    /** Un corte en negro: sube de {@code a} a {@code b}, se queda hasta {@code c} y se va en {@code d}. */
    private static float corte(float t, float a, float b, float c, float d) {
        if (t <= a || t >= d) return 0;
        if (t < b) return (float) suave((t - a) / (b - a));
        if (t <= c) return 1;
        return (float) (1 - suave((t - c) / (d - c)));
    }

    /** Lo negra que esta la pantalla en ese instante: la entrada y los cortes en que se cambia el barco o se salta de amarre. */
    public static float negro(Tiempos T, float t) {
        float a = t < 0.6f ? (float) (1 - suave(t / 0.6)) : 0;
        if (T.cambioA >= 0) a = Math.max(a, corte(t, T.cambioA - 0.45f, T.cambioA - 0.15f, T.cambioA + 0.35f, T.cambioA + 0.75f));
        if (T.corteB0 >= 0) a = Math.max(a, corte(t, T.corteB0, T.corteB0 + 0.3f, T.corteB1 - 0.4f, T.corteB1 + 0.2f));
        a = Math.max(a, corte(t, T.cambioC - 0.45f, T.cambioC - 0.15f, T.cambioC + 0.35f, T.cambioC + 0.75f));
        if (T.tipo == LLAMADA) {
            // la llamada acaba volviendo a los ojos del jugador con un fundido (las otras acaban ya en sus ojos)
            float vuelve = T.total() - 0.45f;
            a = Math.max(a, corte(t, vuelve - 0.4f, vuelve, vuelve, T.total()));
        }
        return a;
    }

    // ------------------------------------------------------------------ por donde vuela el barco

    /**
     * El barco que vuela en el instante {@code t}: {x, y, z, giro, cabeceo, balanceo} (el pie del palo en el mundo; giro 0
     * = proa al sur; cabeceo positivo = proa arriba), o null si en ese instante el barco es de bloques.
     */
    public static double[] barco(Tiempos T, double t, int[] o, int[] d) {
        if (t >= T.cambioC || (T.cambioA >= 0 && t < T.cambioA)) return null;
        double mece = 1.3 * Math.sin(t * 0.9) + 0.5 * Math.sin(t * 2.3 + 1.0);
        if (T.cambioA >= 0 && t < (T.corteB0 + T.corteB1) / 2) {
            // la salida: arriba en vertical y luego hacia el sur, cada vez mas deprisa y ganando altura
            double h = SUBE * suave((t - T.sube) / T.tSube), f = t - T.avanza, z = 0, cabeceo = 0;
            if (f > 0) {
                z = f < 4 ? 1.2 * f * f : 19.2 + 9.6 * (f - 4);
                h += Math.min(18, 0.25 * z);
                cabeceo = 3.5 * suave(f / 1.5) * (1 - suave((f - 5) / 3));
            }
            double aire = suave((t - T.sube) / 2.5);
            return new double[]{PX + o[0], PY + h, PZ + o[1] + z, 0, cabeceo + 0.5 * mece * aire, mece * aire};
        }
        // la llegada: del sur, con la proa al norte; frena sobre su amarre, se da la vuelta y baja
        double u = Math.max(0, t - T.llega), dist, h = T.alto, giro = 180, ladeo = 0, cabeceo = 0;
        double c = T.crucero, b = T.frena;
        if (u < c) dist = T.lejos() - T.velocidad * u;
        else if (u < c + b) {
            double s = u - c;
            dist = T.velocidad * b / 2.0 - (T.velocidad * s - T.velocidad * s * s / (2.0 * b));
            cabeceo = -2.0 * Math.sin(Math.PI * s / b);
        } else {
            dist = 0;
            double k = (u - c - b) / T.gira;
            if (k < 1) {
                double e = suave(k);
                giro = 180 * (1 - e);
                h = T.alto + (VUELTA - T.alto) * e;
                ladeo = 5.5 * Math.sin(Math.PI * k);
            } else {
                giro = 0;
                double k2 = Math.min(1, (u - c - b - T.gira) / T.baja);
                h = VUELTA * (1 - k2) * (1 - k2);
            }
        }
        double aire = Math.min(1, h / 3.0);
        return new double[]{PX + d[0], PY + h, PZ + d[1] + dist, giro, cabeceo + 0.5 * mece * aire, ladeo + mece * aire};
    }

    // ------------------------------------------------------------------ los planos

    private interface Punto {
        double[] en(double t);
    }

    /** Un plano con la camara y la mira muestreadas cada poco (asi siguen al barco, que no va en linea recta). */
    private static Shot plano(float t0, float t1, float fov, int n, Punto cam, Punto mira) {
        double[] p = new double[n * 3], m = new double[n * 3];
        for (int i = 0; i < n; i++) {
            double t = t0 + (t1 - t0) * i / (double) (n - 1);
            double[] a = cam.en(t), b = mira.en(t);
            System.arraycopy(a, 0, p, i * 3, 3);
            System.arraycopy(b, 0, m, i * 3, 3);
        }
        return new Shot(t0, t1, fov, 0f, 0, p, m);
    }

    private static double[] mezcla(double[] a, double[] b, double k) {
        return new double[]{a[0] + (b[0] - a[0]) * k, a[1] + (b[1] - a[1]) * k, a[2] + (b[2] - a[2]) * k};
    }

    private static double[] en(int[] off, double x, double y, double z) {
        return new double[]{x + off[0], y, z + off[1]};
    }

    /** Donde se pone el timonel para el primer plano: detras de la rueda, mirando a proa. {x, y, z} en el amarre {@code o}. */
    public static double[] timonel(int[] o) {
        return en(o, Barco.BX + 0.5, Barco.ALCAZAR_Y + 1, Barco.TIMON_Z - 0.6);
    }

    /**
     * Los planos de un viaje para un jugador. {@code ojos}: donde quedan sus ojos al llegar (en el alcazar) y
     * {@code vista}: a donde mira entonces (los dos en el mundo); en la llamada no hacen falta (no se le mueve).
     */
    public static List<Shot> planos(int tipo, int[] o, int[] d, double[] ojos, double[] vista) {
        Tiempos T = tiempos(tipo);
        List<Shot> l = new ArrayList<>();
        float[] p = T.plano;
        int k = 0;
        if (tipo != LLAMADA) {
            // 1. el timon: delante de la rueda, la camara se aparta y sube; detras de quien lleva el barco, la tierra que se deja
            double[] tim = timonel(o), cara = {tim[0], tim[1] + 1.5, tim[2]};
            double[] a0 = en(o, -6.4, 256.3, 127.2), a1 = en(o, -5.6, 257.6, 129.6), a2 = en(o, -5.0, 259.4, 132.4);
            float t0 = p[k], t1 = p[k + 1];
            l.add(plano(t0, t1, 62f, 12, t -> {
                double u = suave((t - t0) / (t1 - t0));
                return u < 0.5 ? mezcla(a0, a1, u * 2) : mezcla(a1, a2, u * 2 - 1);
            }, t -> cara));
            k++;
            // 2. se eleva: a ras del mar de nubes, desde la aleta de babor
            double[] b0 = en(o, -31, 249.8, 150), b1 = en(o, -34, 254.5, 157);
            float t2 = p[k], t3 = p[k + 1];
            l.add(plano(t2, t3, 66f, 24, t -> mezcla(b0, b1, suave((t - t2) / (t3 - t2))), t -> {
                double[] s = barco(T, Math.max(t, T.cambioA), o, d);
                return new double[]{s[0], s[1] + 6, s[2] + 1};
            }));
            k++;
            // 3. las velas: desde lo alto, por la popa, se le ve irse mar de nubes adentro
            double[] c0 = en(o, -17, 266, 121), c1 = en(o, -11, 271, 138);
            float t4 = p[k], t5 = p[k + 1];
            l.add(plano(t4, t5, 70f, 24, t -> mezcla(c0, c1, suave((t - t4) / (T.corteB0 - t4))), t -> {
                double[] s = barco(T, Math.min(t, T.corteB0), o, d);
                return new double[]{s[0], s[1] + 7, s[2]};
            }));
            k++;
        }
        if (tipo == PRIMERO) {
            // 4. Valdrenn: detras y por encima del barco, con la torre y la isla al fondo
            float t0 = p[k], t1 = p[k + 1];
            double torreX = 3.5 + d[0];
            l.add(plano(t0, t1, 70f, 30, t -> {
                double[] s = barco(T, Math.max(t, T.llega), o, d);
                return new double[]{s[0] - 9, s[1] + 10, s[2] + 26};
            }, t -> {
                double[] s = barco(T, Math.max(t, T.llega), o, d);
                return new double[]{s[0] + (torreX - s[0]) * 0.7, s[1] + 18, s[2] - 60};
            }));
            k++;
            // 5. los juramentos: desde abajo, a ras de nube; pasa por encima y se detiene sobre el muelle
            double[] e0 = en(d, -36, 254.5, 156), e1 = en(d, -31, 257, 149);
            float t2 = p[k], t3 = p[k + 1];
            l.add(plano(t2, t3, 64f, 30, t -> mezcla(e0, e1, suave((t - t2) / (t3 - t2))), t -> {
                double[] s = barco(T, t, o, d);
                return new double[]{s[0], s[1] + 3, s[2] - 3};
            }));
            k++;
        }
        // atraca: desde el mar de nubes en calma, al sureste (el muelle delante y, en Valdrenn, la torre detras): pasa, se
        // detiene sobre su amarre, se da la vuelta en el aire y baja. (Desde el propio anden no cabia en el plano.)
        double[] f0 = en(d, 13.0, 253.8, 166.0), f1 = en(d, 10.5, 254.8, 161.5);
        float t0 = p[k], t1 = p[k + 1];
        float desde = Math.max(t0, T.llega), hasta = T.toca();
        Punto camMuelle = t -> mezcla(f0, f1, suave((t - desde) / (hasta - desde)));
        Punto miraMuelle = t -> {
            double[] s = barco(T, Math.min(Math.max(t, T.llega), T.cambioC - 0.05), o, d);
            return new double[]{s[0], s[1] + 7, s[2]};
        };
        l.add(plano(t0, t1, 72f, 40, camMuelle, miraMuelle));
        k++;
        float t2 = p[k], t3 = p[k + 1];
        if (tipo == LLAMADA) {
            // amarrado: el barco, ya de bloques, desde el muelle
            double[] quieto = {PX + d[0], PY + 7, PZ + d[1]};
            l.add(plano(t2, t3, 72f, 4, t -> f1, t -> quieto));
            return l;
        }
        // en cubierta: la camara baja del aparejo a los ojos del jugador, que mira a tierra
        double[] s0 = {ojos[0] - 2.0, ojos[1] + 5.6, ojos[2] + 4.5}, s1 = {ojos[0] - 0.7, ojos[1] + 2.2, ojos[2] + 2.0};
        double[] alto = {vista[0], vista[1] + (d[0] == 0 && d[1] == 0 ? 22 : 55), vista[2] - 8};
        l.add(plano(t2, t3, 70f, 16, t -> {
            double u = suave((t - T.ultimo) / (t3 - 0.7 - T.ultimo));
            return u < 0.5 ? mezcla(s0, s1, u * 2) : mezcla(s1, ojos, u * 2 - 1);
        }, t -> mezcla(alto, vista, suave((t - T.ultimo) / (t3 - 0.9 - T.ultimo)))));
        return l;
    }

    /**
     * Donde flota (sin verse) quien viaja mientras el barco vuela junto a ese amarre: encima del muelle al salir y, al
     * llegar, algo mas al sur (a medio camino de por donde viene el barco: asi le llegan los chunks de todo lo que se ve,
     * tambien con poca distancia de vision). Muy arriba: a un jugador invisible se le siguen viendo la armadura y lo que
     * lleva en la mano, y asi no lo ve flotando quien se haya quedado en el muelle.
     */
    public static double[] flota(int[] off, boolean llegando) {
        return en(off, 0.5, Barco.CUBIERTA + 80, llegando ? 175.5 : 139.5);
    }

    /** Lo que se le manda al cliente con los planos: {tipo, de dx, de dz, a dx, a dz, si se llega a Valdrenn}. */
    public static double[] datos(int tipo, int[] o, int[] d, boolean aValdrenn) {
        return new double[]{tipo, o[0], o[1], d[0], d[1], aValdrenn ? 1 : 0};
    }
}
