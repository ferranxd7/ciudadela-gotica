package com.darkcraft.cine;

import com.darkcraft.pueblo.gen.Barco;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * La cinematica de la reparacion de La Garza Gris (parte 3d): con las cinco piezas, el barco se rehace solo delante de
 * quien lo repara. Aqui estan los tiempos (cuando aparece cada bloque: {@link #cuando}; los usa el servidor, que los va
 * poniendo) y los tres planos: la cubierta que se cierra, el palo que crece y el barco entero desplegando las velas.
 */
public final class Reparacion {
    private Reparacion() {}

    public static final int ID = 39;

    /** Se va lo roto | se cierran casco y cubierta | crece el palo | las vergas | las velas | rueda, ancla y corazon | el destello. */
    public static final float LIMPIA = 0.9f, CASCO0 = 1.1f, CASCO1 = 3.9f, PALO0 = 4.7f, PALO1 = 8.3f, VERGAS = 8.4f, VELAS0 = 9.9f, VELAS1 = 11.6f,
            PIEZAS = 12.2f, DESTELLO = 12.8f, FIN = 15.2f;
    /** Cuando cambia cada plano. */
    public static final float[] PLANO = {0f, 4.4f, 9.5f, FIN};

    private static final int Y0 = Barco.CUBIERTA + 1, CIMA = Y0 + Barco.PALO_ALTO - 1, MAYOR = Y0 + 12, GAVIA = Y0 + 18;

    /** En que segundo de la cinematica se pone el bloque {@code va} en esa celda del barco (coordenadas de Brumavela). */
    public static float cuando(int x, int y, int z, BlockState va) {
        // lo que sobra del barco roto (telaranas, el trozo de palo caido, la cadena colgando) se va lo primero
        if (va.isAir() || va.getBlock() instanceof com.darkcraft.world.NubeBlock) return LIMPIA + 0.02f * Math.floorMod(x * 7 + z * 3 + y, 12);
        // la rueda del timon, el corazon y el farol de la bodega, al final
        if (va.is(Blocks.GRINDSTONE) || va.is(Blocks.LIGHT) || (y < Barco.CUBIERTA && va.is(Blocks.SOUL_LANTERN))) return PIEZAS;
        boolean palo = x == Barco.BX && z == Barco.PALO;
        if (palo && y >= Y0) return PALO0 + (PALO1 - PALO0) * (y - Y0) / (float) (CIMA + 1 - Y0);
        if (va.is(Blocks.RED_WOOL)) return PALO1 + 0.1f * (z - Barco.PALO);
        // las velas caen de su verga
        if (va.is(Blocks.WHITE_WOOL) || va.is(Blocks.LIGHT_GRAY_WOOL) || va.is(Blocks.GRAY_WOOL)) {
            int verga = y > MAYOR ? GAVIA : MAYOR;
            return (y > MAYOR ? VELAS0 + 0.5f : VELAS0) + (VELAS1 - VELAS0) * (verga - 1 - y) / 8f;
        }
        // las vergas y la cofa salen del palo hacia fuera
        if (y >= MAYOR) return VERGAS + 0.11f * Math.abs(x - Barco.BX) + (y >= GAVIA ? 0.3f : 0f);
        // casco, cubierta, bordas y barandillas: de popa a proa
        return CASCO0 + (CASCO1 - CASCO0) * (z - Barco.POPA) / (float) (Barco.PROA + 6 - Barco.POPA);
    }

    private static double[] junta(double[]... p) {
        double[] o = new double[p.length * 3];
        for (int i = 0; i < p.length; i++) System.arraycopy(p[i], 0, o, i * 3, 3);
        return o;
    }

    /** Los tres planos (el barco roto esta siempre en Brumavela). */
    public static List<Shot> planos() {
        List<Shot> l = new ArrayList<>();
        double bx = Barco.BX + 0.5, pz = Barco.PALO + 0.5;
        // 1. la cubierta: desde la borda del portalon, lo roto se va y las tablas vuelven a su sitio
        l.add(new Shot(PLANO[0], PLANO[1], 68f, 0f, 1,
                junta(new double[]{-3.4, 253.2, 141.6}, new double[]{-3.0, 253.9, 139.2}, new double[]{-3.3, 254.6, 136.4}),
                junta(new double[]{bx - 1.0, 251.2, pz + 2}, new double[]{bx - 0.5, 251.6, pz}, new double[]{bx, 252.5, pz - 2})));
        // 2. el palo: desde el espigon, la camara sube la vista con el
        l.add(new Shot(PLANO[1], PLANO[2], 72f, 0f, 0,
                junta(new double[]{0.6, 252.6, 131.4}, new double[]{1.0, 253.0, 130.2}, new double[]{1.4, 253.4, 129.0}),
                junta(new double[]{bx, 254.5, pz}, new double[]{bx, 262, pz}, new double[]{bx, 270.5, pz})));
        // 3. entero: a ras del mar de nubes, por la aleta de babor; caen las velas y prende el corazon
        l.add(new Shot(PLANO[2], PLANO[3], 66f, 0f, 1,
                junta(new double[]{-30, 250.2, 156}, new double[]{-32.5, 251.6, 152}, new double[]{-34, 253.4, 147}),
                junta(new double[]{bx, 259, pz + 1}, new double[]{bx, 260, pz + 1}, new double[]{bx, 260.5, pz + 1})));
        return l;
    }
}
