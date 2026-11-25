package com.darkcraft.pueblo.gen;

import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.world.NubeBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * El muelle de Brumavela: el frente de sillar de la cala, el espigon de madera que entra en el mar de nubes (con el
 * barco amarrado a su costado del oeste: ver {@link Barco}), su cabeza con la cabria, el embarcadero pequeno del este
 * con un bote, y lo que quedo en el muelle: redes puestas a secar, un bote boca abajo, cajas y faroles de almas.
 */
public final class Muelle extends Element {
    public static final int Y = Terreno.Q;
    /** El frente de sillar y el espigon. */
    public static final int FRENTE = Terreno.CALA_BORDE + 1, Z0 = FRENTE + 1, Z1 = 153;
    /** La cabeza del espigon (hacia el este) y el embarcadero pequeno. */
    public static final int CABEZA_X1 = 9, CABEZA_Z0 = 147;
    public static final int CHICO_X0 = 14, CHICO_X1 = 16, CHICO_Z1 = 130;
    /** Donde se despierta: en el muelle, junto al barco. */
    public static final int INICIO_X = -6, INICIO_Z = 114;

    private final Terreno terreno;

    public Muelle(long seed, PuebloPlan plan) {
        super(seed);
        this.layer = 4;
        this.terreno = plan.terreno;
        bounds(-38, Y - 16, 84, 38, Y + 8, Z1 + 2);
        plan.sitio("inicio", INICIO_X, Y + 1, INICIO_Z);
        plan.sitio("muelle", 0, Y + 1, Z0 + 2);
        plan.sitio("espigon", 0, Y + 1, Z1 - 3);
    }

    /** Si ahi hay tablas del espigon (o de su cabeza). */
    public static boolean enEspigon(int x, int z) {
        if (z < Z0 || z > Z1) return false;
        if (z >= CABEZA_Z0 && x >= -2 && x <= CABEZA_X1) return true;
        int oeste = z >= Barco.ALCAZAR - 2 && z <= Barco.CASTILLO ? -3 : -2;
        return x >= oeste && x <= 2;
    }

    public static boolean enChico(int x, int z) {
        return x >= CHICO_X0 && x <= CHICO_X1 && z >= Z0 && z <= CHICO_Z1;
    }

    @Override
    public void build(Canvas c) {
        frente(c);
        for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
            for (int z = Math.max(Z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) {
                if (!enEspigon(x, z) && !enChico(x, z)) continue;
                tablas(c, x, z);
            }
        pilotes(c);
        cabria(c);
        bote(c);
        enseres(c);
    }

    /** El frente del muelle: un muro de sillar que baja hasta dentro de la nube, con sus norayes. */
    private void frente(Canvas c) {
        for (int x = -37; x <= 37; x++) {
            int t = terreno.alto(x, FRENTE - 1);
            if (t == Terreno.NADA) continue;
            int cima = Math.max(Y, Math.min(t, Y + 12));
            for (int y = Y - 10; y <= cima; y++) c.set(x, y, FRENTE, y == Y && cima == Y ? Mat.losas(x, y, FRENTE, seed) : Mat.sillar(x, y, FRENTE, seed));
            for (int y = cima + 1; y <= Y + 4; y++)
                if (c.get(x, y, FRENTE).getBlock() instanceof NubeBlock) c.set(x, y, FRENTE, Pal.AIR);
            // los norayes (ninguno delante de donde se despierta, que tapa el barco)
            if (cima == Y && Math.abs(x) % 8 == 2 && !enEspigon(x, Z0) && !enChico(x, Z0)) {
                c.set(x, Y + 1, FRENTE, Pal.DO_LOG);
                c.set(x, Y + 2, FRENTE, Pal.slab(Blocks.DARK_OAK_SLAB, false));
            }
        }
    }

    private void tablas(Canvas c, int x, int z) {
        boolean chico = enChico(x, z);
        boolean borde = !(chico ? enChico(x - 1, z) && enChico(x + 1, z) && enChico(x, z + 1) : enEspigon(x - 1, z) && enEspigon(x + 1, z) && enEspigon(x, z + 1));
        int p = Hash.pct(x, Y, z, seed + 1);
        // alguna tabla de menos en los bordes del este y en la cabeza (nunca en el paso, ni junto al barco)
        if (borde && x > 0 && p < 9) return;
        c.set(x, Y, z, borde && p < 40 ? Pal.slab(Blocks.SPRUCE_SLAB, true) : Mat.tabla(x, Y, z, seed));
        for (int y = Y + 1; y <= Y + 4; y++)
            if (c.get(x, y, z).getBlock() instanceof NubeBlock) c.set(x, y, z, Pal.AIR);
    }

    private void pilote(Canvas c, int x, int z, boolean poste) {
        for (int y = Y - 13; y <= Y - 1; y++) c.set(x, y, z, Pal.SP_LOG);
        if (poste) {
            c.set(x, Y, z, Pal.SP_LOG);
            c.set(x, Y + 1, z, Pal.SP_LOG);
        }
    }

    private void pilotes(Canvas c) {
        for (int z = Z0; z <= Z1; z += 4) {
            boolean barco = z >= Barco.ALCAZAR - 2 && z <= Barco.CASTILLO;
            pilote(c, -2, z, !barco && z > Z0);
            pilote(c, 2, z, (z - Z0) % 8 == 4 && z < CABEZA_Z0);
            for (int x = -1; x <= 1; x++) c.set(x, Y - 1, z, Pal.log(Blocks.SPRUCE_LOG, Direction.Axis.X));
        }
        pilote(c, CABEZA_X1, CABEZA_Z0, true);
        pilote(c, CABEZA_X1, Z1, true);
        pilote(c, 5, Z1, false);
        pilote(c, 5, CABEZA_Z0, false);
        for (int z = Z0; z <= CHICO_Z1; z += 5) {
            pilote(c, CHICO_X0, z, z > Z0);
            pilote(c, CHICO_X1, z, z == CHICO_Z1);
        }
        // las defensas entre el espigon y el barco y la pasarela del portalon
        for (int z = Barco.PALO + 1; z <= Barco.PALO + 2; z++) c.set(-3, Y, z, Pal.SP_PLANKS);
        // faroles de almas en el espigon
        Mat.farola(c, 2, Y + 1, Z0 + 6, 4, Direction.WEST);
        Mat.farola(c, 2, Y + 1, Z0 + 22, 4, Direction.WEST);
        Mat.farola(c, -2, Y + 1, Z0 + 2, 4, Direction.EAST);
        c.set(CABEZA_X1, Y + 2, Z1, Pal.SP_FENCE);
        c.set(CABEZA_X1, Y + 3, Z1, Pal.SOUL_LANTERN);
    }

    /** La cabria de la cabeza del espigon: un pie derecho, el brazo sobre las nubes y un barril colgando. */
    private void cabria(Canvas c) {
        int x = CABEZA_X1 - 1, z = CABEZA_Z0 + 3;
        for (int y = Y + 1; y <= Y + 5; y++) c.set(x, y, z, Pal.DO_LOG);
        for (int k = -1; k <= 4; k++) c.set(x + k, Y + 5, z, k == 0 ? Pal.DO_LOG : Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        c.set(x + 1, Y + 4, z, Pal.stairs(Blocks.DARK_OAK_STAIRS, Direction.WEST, true));
        c.set(x + 4, Y + 4, z, Pal.CHAIN);
        c.set(x + 4, Y + 3, z, Pal.CHAIN);
        c.set(x + 4, Y + 2, z, Mat.barril(Direction.UP));
        // la carga que se quedo en la cabeza
        c.set(CABEZA_X1 - 3, Y + 1, Z1 - 1, Mat.barril(Direction.UP));
        c.set(CABEZA_X1 - 4, Y + 1, Z1 - 1, Pal.SP_PLANKS);
        c.set(CABEZA_X1 - 4, Y + 2, Z1 - 1, Mat.barril(Direction.EAST));
        c.set(CABEZA_X1 - 4, Y + 1, Z1 - 2, Mat.barril(Direction.UP));
        c.set(4, Y + 1, CABEZA_Z0, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
    }

    /** Un bote amarrado al embarcadero pequeno, posado en la nube. */
    private void bote(Canvas c) {
        int x = CHICO_X1 + 3, z = Z0 + 3, y = Y - 2;
        for (int k = 0; k <= 5; k++) {
            boolean punta = k == 0 || k == 5;
            c.set(x, y, z + k, punta ? Pal.stairs(Blocks.SPRUCE_STAIRS, k == 0 ? Direction.SOUTH : Direction.NORTH, true) : Pal.slab(Blocks.SPRUCE_SLAB, true));
            if (punta) continue;
            c.set(x - 1, y + 1, z + k, trampilla(Direction.EAST));
            c.set(x + 1, y + 1, z + k, trampilla(Direction.WEST));
            c.set(x, y + 1, z + k, Pal.AIR);
        }
        c.set(x, y + 1, z, trampilla(Direction.SOUTH));
        c.set(x, y + 1, z + 5, trampilla(Direction.NORTH));
        c.set(x, y + 1, z + 3, Pal.slab(Blocks.SPRUCE_SLAB, false));
        // la amarra
        c.set(CHICO_X1 + 1, Y, z + 2, Pal.CHAIN.setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.X));
    }

    private static BlockState trampilla(Direction mira) {
        return Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, mira).setValue(TrapDoorBlock.OPEN, true);
    }

    /** Lo que quedo en el muelle. */
    private void enseres(Canvas c) {
        int y = Y + 1;
        // las redes puestas a secar: dos tendederos
        for (int[] t : new int[][]{{-22, 106}, {-17, 102}}) {
            for (int k = 0; k <= 4; k++) {
                boolean poste = k == 0 || k == 4;
                if (poste) c.set(t[0] + k, y, t[1], Pal.SP_FENCE);
                c.unir(t[0] + k, y + 1, t[1], Pal.SP_FENCE);
                if (!poste && Hash.pct(t[0] + k, y, t[1], seed + 5) < 75) c.set(t[0] + k, y, t[1], Pal.COBWEB);
            }
        }
        // un bote boca abajo
        for (int k = 0; k <= 4; k++) {
            int z = 96 + k;
            c.set(-26, y, z, k == 0 ? Pal.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, false) : k == 4 ? Pal.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, false)
                    : Pal.slab(Blocks.SPRUCE_SLAB, false));
        }
        // cajas y barriles a los lados del porton del almacen
        int[][] cajas = {{8, 94, 0}, {8, 95, 1}, {7, 94, 0}, {8, 103, 1}, {8, 104, 0}, {7, 104, 1}, {9, 108, 0}, {-12, 92, 1}, {-13, 92, 0}, {-13, 93, 1}};
        for (int[] k : cajas) {
            if (terreno.alto(k[0], k[1]) != Terreno.Q) continue;
            c.set(k[0], y, k[1], k[2] == 0 ? Mat.barril(Direction.UP) : Pal.SP_PLANKS);
            if (Hash.pct(k[0], y, k[1], seed + 6) < 35) c.set(k[0], y + 1, k[1], Mat.barril(Hash.pct(k[0], 2, k[1], seed + 6) < 50 ? Direction.UP : Direction.WEST));
        }
        // faroles: al pie de la Cuesta y en el frente
        Mat.farola(c, -5, y, 86, 4, Direction.EAST);
        Mat.farola(c, 5, y, 86, 4, Direction.WEST);
        Mat.farola(c, 20, y, FRENTE - 1, 4, Direction.NORTH);
        Mat.farola(c, -24, y, FRENTE - 1, 4, Direction.NORTH);
    }
}
