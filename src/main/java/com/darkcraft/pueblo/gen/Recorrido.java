package com.darkcraft.pueblo.gen;

import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La prueba de que Brumavela se puede recorrer: desde donde se despierta se llega andando a todos los sitios de la
 * aventura (el arcon de la vela, el timon, las campanas, el corazon, el barco...), y a la cripta solo cuando la losa se
 * ha apartado. La usan la prueba del servidor de la CI y la herramienta de diseno (tools/vista3d).
 */
public final class Recorrido {
    private Recorrido() {}

    public record Paso(String sitio, int[] donde, boolean llega, boolean debe) {
        public boolean bien() {
            return llega == debe;
        }
    }

    /** Los sitios a los que no se llega hasta abrir la cripta. */
    private static final Set<String> TRAS_LA_LOSA = Set.of("cripta", "mastil");

    /** El mundo con la losa de la cripta apartada. */
    public static Andar.Mundo sinLosa(Andar.Mundo m, int nave) {
        return (x, y, z) -> y == nave && x >= Capilla.BX0 && x <= Capilla.BX1 && z >= Capilla.BZ0 && z <= Capilla.BZ1 ? Blocks.AIR.defaultBlockState()
                : m.en(x, y, z);
    }

    public static List<Paso> comprobar(Andar.Mundo m, boolean abierta) {
        PuebloPlan p = PuebloPlan.get();
        int[] ini = p.sitio("inicio");
        Andar.Mundo mundo = abierta ? sinLosa(m, p.capilla.y) : m;
        Andar.Zona zona = Andar.desde(mundo, ini[0], ini[1], ini[2], -Terreno.LADO, -Terreno.LADO, Terreno.LADO, Terreno.LADO + 20, 400000);
        List<Paso> r = new ArrayList<>();
        r.add(new Paso("(celdas: " + zona.celdas() + ")", ini, zona.celdas() > 20000, true));
        for (Map.Entry<String, int[]> e : p.sitios().entrySet()) {
            int[] d = e.getValue();
            boolean debe = abierta || !TRAS_LA_LOSA.contains(e.getKey());
            r.add(new Paso(e.getKey(), d, zona.alcanza(d[0], d[1], d[2], 2.6), debe));
        }
        return r;
    }

    /** Un resumen de una linea y, si algo falla, cuales. */
    public static String resumen(List<Paso> pasos) {
        StringBuilder mal = new StringBuilder();
        int bien = 0;
        for (Paso p : pasos) {
            if (p.bien()) bien++;
            else mal.append(' ').append(p.sitio()).append(p.llega() ? " (se llega y no se deberia)" : " (NO se llega)").append(" en ").append(p.donde()[0]).append(',')
                    .append(p.donde()[1]).append(',').append(p.donde()[2]).append(';');
        }
        return bien + " de " + pasos.size() + " bien" + (mal.length() > 0 ? " | MAL:" + mal : "") + " " + pasos.get(0).sitio();
    }
}
