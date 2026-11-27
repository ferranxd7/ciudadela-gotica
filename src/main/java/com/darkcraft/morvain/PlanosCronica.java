package com.darkcraft.morvain;

import com.darkcraft.cine.Shot;
import com.darkcraft.cine.Timeline;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

/**
 * Los planos de la cronica de Morvain (parte 7ae): mientras Maelis cuenta cada capitulo, la camara recorre el sitio de su
 * cofre sin mover al jugador (todo esta cerca: lo que se ve son los chunks de alrededor; con la lejania, tambien lo de mas
 * alla). El 7 en el Arco del Umbral, el 8 en la plaza del Juramento de la Capital, el 9 en el Cerco del Ancla y el 10 en la
 * capilla negra de la torre del homenaje. Cada plano va con las frases de {@link Timeline#groups}.
 * <p>
 * No mira el mundo directamente: pregunta a un {@link Mundo} (el de verdad, o el de mentira de {@code tools/vista3d}, que asi
 * los puede dibujar sin abrir el juego). Si un plano chocaria con algo, prueba otros parecidos ({@link #buscar}) y nunca pasa
 * por donde esta el jugador.
 */
public final class PlanosCronica {
    private PlanosCronica() {}

    /** Lo que hay en (x, y, z), o null si no esta cargado (ahi la camara puede ir). */
    public interface Mundo {
        BlockState en(int x, int y, int z);
    }

    /** El mundo de verdad. */
    public static Mundo de(ServerLevel sl) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        return (x, y, z) -> {
            m.set(x, y, z);
            return sl.isLoaded(m) ? sl.getBlockState(m) : null;
        };
    }

    /** Si la camara puede estar en ese bloque (aire, luz, plantas...; nada que se pise ni agua). */
    static boolean libre(Mundo w, double x, double y, double z) {
        BlockState st = w.en((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        return st == null || st.isAir() || st.is(Blocks.LIGHT) || st.is(Blocks.BARRIER) || !st.blocksMotion() && st.getFluidState().isEmpty();
    }

    /** Si ese bloque tapa la vista. */
    static boolean tapa(Mundo w, int x, int y, int z) {
        BlockState st = w.en(x, y, z);
        return st != null && !st.isAir() && st.blocksMotion();
    }

    // ------------------------------------------------------------------ lo comun

    private static double[] p(double... v) {
        return v;
    }

    private static double[] cat(double[]... v) {
        double[] o = new double[v.length * 3];
        for (int i = 0; i < v.length; i++) System.arraycopy(v[i], 0, o, i * 3, 3);
        return o;
    }

    /** Punto a r bloques de (cx, cz), en el angulo dado (grados: 0 al este, 90 al sur, como los pilares del arco). */
    private static double[] anillo(double cx, double cz, double grados, double r, double y) {
        double a = Math.toRadians(grados);
        return new double[]{cx + r * Math.cos(a), y, cz + r * Math.sin(a)};
    }

    /** Si la camara del plano recorre solo sitios libres (con {@code margen} alrededor) y no pasa a menos de 1,3 del jugador. */
    static boolean camaraLibre(Mundo w, Shot sh, Vec3 eye, double margen) {
        for (int i = 0; i <= 24; i++) {
            double[] a = sh.at(sh.t0 + (sh.t1 - sh.t0) * i / 24.0);
            if (!libre(w, a[0], a[1], a[2])) return false;
            for (int k = 0; k < 6; k++) {
                double dx = k == 0 ? margen : k == 1 ? -margen : 0, dy = k == 2 ? margen : k == 3 ? -margen : 0, dz = k == 4 ? margen : k == 5 ? -margen : 0;
                if (!libre(w, a[0] + dx, a[1] + dy, a[2] + dz)) return false;
            }
            if (eye != null) {
                // el cuerpo del jugador: de los pies a los ojos
                double hx = a[0] - eye.x, hz = a[2] - eye.z, dy = a[1] - Math.max(eye.y - 1.62, Math.min(eye.y + 0.2, a[1]));
                if (hx * hx + hz * hz + dy * dy < 1.3 * 1.3) return false;
            }
        }
        return true;
    }

    /** Cuantas muestras del plano (de 25) tienen algo entre la camara y lo que mira (hasta 1,5 antes de llegar). */
    public static int tapadas(Mundo w, Shot sh) {
        int n = 0;
        for (int i = 0; i <= 24; i++) {
            double[] a = sh.at(sh.t0 + (sh.t1 - sh.t0) * i / 24.0);
            double dx = a[3] - a[0], dy = a[4] - a[1], dz = a[5] - a[2], len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            // (lo que mira puede estar muy lejos, como la ciudadela desde el arco: se mira solo lo que este cerca)
            double hasta = Math.min(len - 1.5, 90);
            for (double u = 0.6; u < hasta; u += 0.35)
                if (tapa(w, (int) Math.floor(a[0] + dx * u / len), (int) Math.floor(a[1] + dy * u / len), (int) Math.floor(a[2] + dz * u / len))) {
                    n++;
                    break;
                }
        }
        return n;
    }

    /** Cuantas muestras del plano (de 25) tienen la camara dentro de algo. */
    public static int metidas(Mundo w, Shot sh) {
        int n = 0;
        for (int i = 0; i <= 24; i++) {
            double[] a = sh.at(sh.t0 + (sh.t1 - sh.t0) * i / 24.0);
            if (!libre(w, a[0], a[1], a[2])) n++;
        }
        return n;
    }

    /**
     * El plano de {@code base} o, si choca con algo o no se ve lo que mira, el primero que si probando de {@code paso} en
     * {@code paso} a un lado y a otro; si ninguno, el que menos se tapa de los libres; y si no, el de {@code base}.
     */
    private static Shot buscar(Mundo w, Vec3 eye, double base, double paso, double margen, DoubleFunction<Shot> plano) {
        Shot mejor = null;
        int menos = Integer.MAX_VALUE;
        for (int i = 0; i < 13; i++) {
            double a = base + paso * ((i + 1) / 2) * (i % 2 == 0 ? 1 : -1);
            Shot sh = plano.apply(a);
            if (!camaraLibre(w, sh, eye, margen)) continue;
            int t = tapadas(w, sh);
            if (t <= 2) return sh;
            if (t < menos) {
                menos = t;
                mejor = sh;
            }
        }
        return mejor != null ? mejor : plano.apply(base);
    }

    /**
     * El jugador visto por encima del hombro (derecho o izquierdo, y mas cerca si no cabe); la camara se aleja y sube mientras
     * mira a lo que tiene delante. Si no cabe, de frente; y si tampoco, desde arriba.
     */
    private static Shot jugador(Mundo w, Vec3 eye, float yaw, float t0, float t1) {
        double yr = Math.toRadians(yaw);
        double lx = -Math.sin(yr), lz = Math.cos(yr), rx = -Math.cos(yr), rz = -Math.sin(yr);
        for (double esc : new double[]{1.0, 0.7, 0.45})
            for (int v : new int[]{1, -1}) {
                double[] pos = p(
                        eye.x - lx * 2.8 * esc + rx * 1.0 * v, eye.y + 0.45, eye.z - lz * 2.8 * esc + rz * 1.0 * v,
                        eye.x - lx * 5.0 * esc + rx * 2.0 * v, eye.y + 2.4 * esc, eye.z - lz * 5.0 * esc + rz * 2.0 * v,
                        eye.x - lx * 7.5 * esc + rx * 2.8 * v, eye.y + 5.0 * esc, eye.z - lz * 7.5 * esc + rz * 2.8 * v);
                double[] look = p(eye.x + lx * 6, eye.y - 0.4, eye.z + lz * 6, eye.x + lx * 4, eye.y - 0.8, eye.z + lz * 4,
                        eye.x + lx * 2, eye.y - 1.4, eye.z + lz * 2);
                Shot sh = new Shot(t0, t1, 62, 0, 1, pos, look);
                if (camaraLibre(w, sh, null, 0.6) && seVe(w, sh, eye)) return sh;
            }
        // de frente, un poco por encima
        for (double esc : new double[]{1.0, 0.6}) {
            Shot sh = new Shot(t0, t1, 58, 0, 1,
                    p(eye.x + lx * 3.2 * esc, eye.y + 0.5, eye.z + lz * 3.2 * esc, eye.x + lx * 4.2 * esc, eye.y + 1.4 * esc, eye.z + lz * 4.2 * esc),
                    p(eye.x, eye.y - 0.3, eye.z, eye.x, eye.y - 0.5, eye.z));
            if (camaraLibre(w, sh, null, 0.5) && seVe(w, sh, eye)) return sh;
        }
        return new Shot(t0, t1, 62, 0, 1, p(eye.x + 0.6, eye.y + 3.5, eye.z + 0.6, eye.x + 0.9, eye.y + 7.0, eye.z + 0.9),
                p(eye.x, eye.y - 1.0, eye.z, eye.x, eye.y - 1.4, eye.z));
    }

    /** Si desde la camara del plano se ve al jugador (en cinco momentos). */
    private static boolean seVe(Mundo w, Shot sh, Vec3 eye) {
        for (int i = 0; i <= 4; i++) {
            double[] a = sh.at(sh.t0 + (sh.t1 - sh.t0) * i / 4.0);
            double bx = eye.x, by = eye.y - 0.6, bz = eye.z;
            double dx = bx - a[0], dy = by - a[1], dz = bz - a[2], len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            for (double u = 0.5; u < len - 0.6; u += 0.4)
                if (tapa(w, (int) Math.floor(a[0] + dx * u / len), (int) Math.floor(a[1] + dy * u / len), (int) Math.floor(a[2] + dz * u / len)))
                    return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ los capitulos

    /**
     * Los planos del capitulo {@code ch} (7-10) para quien esta en {@code eye} mirando a {@code yaw} (sus ojos y su giro: el
     * ultimo plano del 7 y del 8 es el suyo). Vacio si no es de Morvain.
     */
    public static List<Shot> capitulo(int ch, Mundo w, Vec3 eye, float yaw) {
        return switch (ch) {
            case 7 -> arco(w, eye, yaw);
            case 8 -> capital(w, eye, yaw);
            case 9 -> cerco(w, eye, yaw);
            case 10 -> capilla(w, eye, yaw);
            default -> List.of();
        };
    }

    private static float t0() {
        return Timeline.NARR_START - 0.6f;
    }

    private static float[] cortes(int ch) {
        int n = Timeline.groups(ch).length;
        float[] c = new float[n + 1];
        c[0] = t0();
        for (int g = 0; g < n - 1; g++) c[g + 1] = Timeline.cut(ch, g);
        c[n] = Timeline.back(ch) + 0.05f;
        return c;
    }

    /**
     * El 7, en el Arco del Umbral: desde detras de lo alto del arco, la plaza y las llanuras hacia el norte; los pilares rotos a
     * ras de suelo; el arco del portal con su remolino rojo, desde abajo; el camino del norte con la estela; el horizonte donde
     * cuelga la ciudadela (con la lejania se ve; sin ella, el ocaso); y el santuario con Maelis (o el jugador).
     */
    private static List<Shot> arco(Mundo w, Vec3 eye, float yaw) {
        float[] c = cortes(7);
        double cx = Llegada.X + 0.5, cz = Llegada.Z + 0.5, F = Llegada.Y + 1;
        List<Shot> l = new ArrayList<>();
        // 1) "Antes de la Sombra, esto era Morvain...": sube y avanza por encima del arco, mirando al norte
        l.add(buscar(w, eye, 0, 4, 0.9, a -> new Shot(c[0], c[1], 70, 0, 1,
                p(cx + 13 + a, F + 19, cz + 34, cx + 7 + a, F + 26, cz + 24, cx + 1 + a, F + 33, cz + 14),
                p(cx, F + 6, cz - 20, cx, F + 12, cz - 70, cx, F + 22, cz - 150))));
        // 2) "Su gente juraba como otros respiran...": a ras de suelo, por fuera de los pilares rotos del oeste, mirando al centro
        l.add(buscar(w, eye, 200, 10, 0.6, a -> new Shot(c[1], c[2], 60, 0, 1,
                cat(anillo(cx, cz, a, 19, F + 2.4), anillo(cx, cz, a - 22, 18.5, F + 2.9), anillo(cx, cz, a - 44, 18, F + 3.4)),
                p(cx, F + 2.6, cz, cx, F + 3.0, cz, cx, F + 3.4, cz))));
        // 3) "Entonces llego la noche del cielo rojo... Nadie acudio.": ante el arco del portal, abajo, mirando arriba al remolino
        double pz = Llegada.PZ + 0.5;
        l.add(buscar(w, eye, 0, 1.5, 0.6, a -> new Shot(c[2], c[3], 62, 0, 1,
                p(cx + 2.5 + a, F + 1.4, pz - 9.5, cx + 1.8 + a, F + 1.8, pz - 7.8, cx + 1.2 + a, F + 2.2, pz - 6.4),
                p(cx, F + 6.5, pz, cx, F + 7.5, pz, cx, F + 8.5, pz))));
        // 4) "No murieron, marcado. Se quedaron...": por el camino del norte, a ras de suelo, dejando atras la estela
        l.add(buscar(w, eye, 0, 1.5, 0.6, a -> new Shot(c[3], c[4], 64, 0, 0,
                p(cx + 2.6 + a, F + 1.5, cz - 6, cx + 2.4 + a, F + 1.7, cz - 15, cx + 2.2 + a, F + 1.9, cz - 26),
                p(cx + 0.5, F + 1.6, cz - 40, cx + 0.5, F + 2.2, cz - 70, cx + 0.5, F + 3, cz - 110))));
        // 5) "Y en el corazon de todo... la ciudadela cuelga de sus ocho cadenas": hacia la ciudadela, lejos al norte; la camara sube
        l.add(new Shot(c[4], c[5], 48, 0, 1,
                p(cx - 1, F + 5, cz - 10, cx - 1, F + 13, cz - 13, cx - 1, F + 22, cz - 16),
                p(0.5, 560, 0.5, 0.5, 600, 0.5, 0.5, 640, 0.5)));
        // 6) "Descansa cuando puedas...": Maelis de cara (mira al arco) con el santuario detras; si no cabe, el jugador
        Shot s6 = buscar(w, eye, 72, 10, 0.5, a -> new Shot(c[5], c[6], 56, 0, 1,
                cat(anillo(cx, cz, a, 5.2, F + 1.8), anillo(cx, cz, a - 6, 4.5, F + 1.5)),
                p(cx + 1.3, F + 0.9, cz + 0.6, cx + 1.4, F + 0.95, cz + 0.7)));
        l.add(camaraLibre(w, s6, eye, 0.4) ? s6 : jugador(w, eye, yaw, c[5], c[6]));
        return l;
    }

    /**
     * El 8, en la Capital: la Puerta Oeste desde la calle mayor (el rastrillo medio bajado); la camara sube en el cruce sobre los
     * tejados quemados; las fachadas negras de la calle; los calcinados de la plaza alrededor de la estatua; la estatua caida de
     * cerca; y el jugador ante su cofre.
     */
    private static List<Shot> capital(Mundo w, Vec3 eye, float yaw) {
        float[] c = cortes(8);
        double X = Capital.CX, Z = Capital.CZ + 0.5, F = Capital.PISO;
        double ex = X + Capital.PLAZA_U + 0.5;
        List<Shot> l = new ArrayList<>();
        // 1) "Ser Halvard cerro las puertas...": la puerta, desde dentro, a ras de calle
        l.add(buscar(w, eye, 0, 1.5, 0.6, a -> new Shot(c[0], c[1], 60, 0, 1,
                p(X + 18, F + 2.0, Z - 1.5 + a, X + 15, F + 2.3, Z - 1.0 + a, X + 12, F + 2.6, Z - 0.5 + a),
                p(X - 3, F + 4, Z, X - 3.5, F + 4.5, Z, X - 4, F + 5, Z))));
        // 2) "La Rectora subio a su torre... El Abad subio a la cumbre...": en el cruce, la camara sube sobre los tejados quemados
        double kx = X + Capital.CRUCE_U + 0.5;
        l.add(buscar(w, eye, 0, 3, 0.9, a -> new Shot(c[1], c[2], 68, 0, 1,
                p(kx + a, F + 2.2, Z - 4, kx + 5 + a, F + 18, Z - 12, kx + 10 + a, F + 34, Z - 20),
                p(kx - 14, F + 6, Z - 45, kx - 30, F + 10, Z - 110, kx - 45, F + 14, Z - 190))));
        // 3) "El barquero aun rema... Brann espera... Anselmo sangra...": por la calle mayor, las fachadas quemadas del norte
        l.add(buscar(w, eye, 0, 1, 0.5, a -> new Shot(c[2], c[3], 56, 0, 0,
                p(X + 24, F + 1.8, Z - 1.5 + a, X + 34, F + 1.9, Z - 1.5 + a, X + 44, F + 2.0, Z - 1.5 + a),
                p(X + 28, F + 2.6, Z - 9, X + 38, F + 2.6, Z - 9, X + 48, F + 2.6, Z - 9))));
        // 4) "Ninguno eligio quedarse...": la plaza, alrededor de la estatua (por el lado del este: al oeste esta el jugador), con
        //    los calcinados
        l.add(buscar(w, eye, -12, 12, 0.6, a -> new Shot(c[3], c[4], 62, 0, 1,
                cat(anillo(ex, Z, a, 12, F + 2.4), anillo(ex, Z, a + 18, 12, F + 3.4), anillo(ex, Z, a + 36, 12, F + 4.4)),
                p(ex, F + 3, Z, ex, F + 3.2, Z, ex, F + 3.4, Z))));
        // 5) "Cuando los derribas, no los matas...": la estatua caida, de cerca y desde abajo
        l.add(buscar(w, eye, 20, 15, 0.5, a -> new Shot(c[4], c[5], 54, 0, 1,
                cat(anillo(ex, Z, a, 7.6, F + 1.8), anillo(ex, Z, a + 10, 6.6, F + 2.3)),
                p(ex, F + 3.6, Z, ex, F + 3.9, Z))));
        // 6) "Recuerda sus nombres, marcado...": el jugador ante su cofre
        l.add(jugador(w, eye, yaw, c[5], c[6]));
        return l;
    }

    /**
     * El 9, en el Cerco del Ancla: el ancla y la cadena que sube, desde el patio; los que la golpean, a ras de suelo; el primer
     * eslabon de cerca; la camara sube a lo largo de la cadena; la cima del ancla donde Aldric se arrodillo; y el Cerco entero desde
     * lo alto, con la cadena hacia el cielo.
     */
    private static List<Shot> cerco(Mundo w, Vec3 eye, float yaw) {
        float[] c = cortes(9);
        double X = Cerco.CX + 0.5, Z = Cerco.CZ, F = Cerco.PISO;
        double cimaZ = Z + 3.5;
        List<Shot> l = new ArrayList<>();
        // 1) "Aldric fue el primero...": desde el patio, junto a la puerta del sur, el ancla y la cadena que sube
        l.add(buscar(w, eye, 0, 2.5, 0.9, a -> new Shot(c[0], c[1], 64, 0, 1,
                p(X - 3 + a, F + 3, Z + 32, X - 2 + a, F + 3.8, Z + 28.5, X - 1 + a, F + 4.6, Z + 25),
                p(X, F + 9, cimaZ, X, F + 14, Z - 5, X, F + 20, Z - 15))));
        // 2) "Otros llegaron despues... golpeaban la cadena por turnos...": el ancla entera desde el lado del campamento (al
        //    oeste-noroeste, de espaldas al jugador), algo por encima de las cabezas de los marcados, rodeandola despacio y
        //    mirando a lo alto de su cara de este lado (la cadena sale de la cima; mirar a la cima misma da en el primer eslabon,
        //    que la tapa siempre); en la build 192, a ras de suelo y a 17, solo se veia la pared de la piramide (tapada 25 de 25)
        l.add(buscar(w, eye, 215, 10, 0.6, a -> new Shot(c[1], c[2], 60, 0, 1,
                cat(anillo(X, cimaZ, a, 26, F + 3.2), anillo(X, cimaZ, a + 9, 25.5, F + 3.8), anillo(X, cimaZ, a + 18, 25, F + 4.4)),
                cat(anillo(X, cimaZ, a, 6, F + 5.5), anillo(X, cimaZ, a + 9, 6, F + 6.0), anillo(X, cimaZ, a + 18, 6, F + 6.5)))));
        // 3) "El hierro que forjaron mis hermanas no se rompe...": el primer eslabon, de cerca y de lado
        l.add(buscar(w, eye, 0, 2, 0.8, a -> new Shot(c[2], c[3], 55, 0, 1,
                p(X + 15 + a, F + 6, Z + 6, X + 14 + a, F + 7.5, Z + 3, X + 13 + a, F + 9, Z),
                p(X + 9, F + 12, Z - 8, X + 9, F + 14, Z - 12, X + 9, F + 16, Z - 16))));
        // 4) "Asi que Aldric subio... Arriba no habia ninguna puerta...": la camara sube junto a la cadena, mirando hacia arriba
        l.add(buscar(w, eye, 0, 3, 1.0, a -> new Shot(c[3], c[4], 70, 0, 0,
                p(X + 18 + a, F + 16, Z - 8, X + 20 + a, F + 38, Z - 52, X + 21 + a, F + 58, Z - 90),
                p(X, F + 30, Z - 40, X, F + 55, Z - 100, X, F + 82, Z - 160))));
        // 5) "Bajo corriendo y nunca volvio a subir. Se arrodillo junto al ancla...": la cima del ancla, donde espera Aldric
        l.add(buscar(w, eye, 135, 15, 0.6, a -> new Shot(c[4], c[5], 52, 0, 1,
                cat(anillo(X, cimaZ, a, 8.0, F + 10.5), anillo(X, cimaZ, a + 6, 7.0, F + 10.0)),
                p(X, F + 8.8, cimaZ, X, F + 8.6, cimaZ))));
        // 6) "La Sombra no necesita que rompan sus cadenas... La tuya es la primera mano...": el Cerco desde lo alto, con la cadena
        l.add(buscar(w, eye, 0, 6, 1.0, a -> new Shot(c[5], c[6], 60, 0, 1,
                p(X + a, F + 40, Z + 52, X + a, F + 46, Z + 58, X + a, F + 52, Z + 64),
                p(X, F + 10, cimaZ, X, F + 16, Z - 2, X, F + 22, Z - 10))));
        return l;
    }

    /**
     * El 10, en la capilla negra: la nave hacia el altar y el retablo con la marca; la isla colgada de sus cadenas, desde fuera;
     * los candelabros de la nave; el trono vacio de la reina, un piso mas arriba; la Ultima Llama, de cerca; la aguja contra el
     * cielo; y Maelis junto al fuego.
     */
    private static List<Shot> capilla(Mundo w, Vec3 eye, float yaw) {
        float[] c = cortes(10);
        double y0 = Homenaje.pisa(Homenaje.CAPILLA), yt = Homenaje.pisa(Homenaje.TRONO), ya = Homenaje.pisa(Homenaje.AZOTEA);
        double sx = Homenaje.SANT_X + 0.5, sz = Homenaje.SANT_Z + 0.5;
        List<Shot> l = new ArrayList<>();
        // 1) "Hace mil inviernos...": por la nave hacia el altar y el retablo con la marca en gemas
        l.add(buscar(w, eye, 0, 1.5, 0.6, a -> new Shot(c[0], c[1], 60, 0, 1,
                p(-0.5 + a, y0 + 3.6, 7, -0.5 + a, y0 + 3.3, 1, -0.5 + a, y0 + 3.0, -4),
                p(-0.5, y0 + 3.4, -19, -0.5, y0 + 3.8, -19, -0.5, y0 + 4.0, -19))));
        // 2) "Forjaron ocho cadenas...": la isla colgada de sus cadenas, desde fuera (la de la cinematica del final)
        l.add(new Shot(c[1], c[2], 64, 0, 1,
                p(96, y0, 118, 78, y0 + 4, 130, 60, y0 + 8, 140),
                p(0, y0 + 2, 0, 0, y0 + 8, 0, 0, y0 + 14, 0)));
        // 3) "Despues se repartieron por la tierra y encendieron los fuegos...": los candelabros del lado oeste de la nave
        l.add(buscar(w, eye, 0, 0.8, 0.5, a -> new Shot(c[2], c[3], 50, 0, 0,
                p(-11.5 + a, y0 + 2.3, -7, -11.5 + a, y0 + 2.4, -3, -11.5 + a, y0 + 2.5, 1),
                p(-13.5, y0 + 2.4, -3.5, -13.5, y0 + 2.5, 0.5, -13.5, y0 + 2.6, 4.5))));
        // 4) "Pero una cadena solo es tan fuerte como los juramentos... por una reina": el trono, un piso mas arriba
        double tz = Homenaje.TRONO_Z + 0.5, tx = Homenaje.TRONO_X + 0.5;
        l.add(buscar(w, eye, 0, 1.2, 0.6, a -> new Shot(c[3], c[4], 56, 0, 1,
                p(tx - 3 + a, yt + 1.6, tz - 13, tx - 2 + a, yt + 1.9, tz - 10.5, tx - 1 + a, yt + 2.2, tz - 8),
                p(tx, yt + 2.2, tz, tx, yt + 2.5, tz, tx, yt + 2.8, tz))));
        // 5) "Una a una, mis hermanas se apagaron... Esta es la ultima llama.": la Ultima Llama, de cerca, rodeandola
        l.add(buscar(w, eye, 125, 15, 0.45, a -> new Shot(c[4], c[5], 54, 0, 1,
                cat(anillo(sx, sz, a - 25, 3.3, y0 + 1.7), anillo(sx, sz, a, 3.1, y0 + 1.6), anillo(sx, sz, a + 25, 3.0, y0 + 1.5)),
                p(sx, y0 + 0.9, sz, sx, y0 + 1.0, sz, sx, y0 + 1.1, sz))));
        // 6) "Ahi arriba, la Sombra te lo ofrecera todo...": la aguja contra el cielo, desde fuera de la azotea
        l.add(new Shot(c[5], c[6], 66, 0, 1,
                p(38, ya + 3, 10, 44, ya + 9, 14, 50, ya + 15, 18),
                p(0, ya + 25, 0, 0, ya + 34, 0, 0, ya + 43, 0)));
        // 7) "No puedo elegir por ti, marcado...": Maelis junto al fuego, de frente (mira al sur)
        double gx = Homenaje.GUIA_X + 0.5, gz = Homenaje.GUIA_Z + 0.5;
        Shot s7 = buscar(w, eye, 0, 0.6, 0.4, a -> new Shot(c[6], c[7], 50, 0, 1,
                p(gx - 0.9 + a, y0 + 1.4, gz + 2.9, gx - 0.6 + a, y0 + 1.2, gz + 2.2),
                p(gx, y0 + 0.85, gz, gx, y0 + 0.85, gz)));
        l.add(camaraLibre(w, s7, eye, 0.3) ? s7 : jugador(w, eye, yaw, c[6], c[7]));
        return l;
    }
}
