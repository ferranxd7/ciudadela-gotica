package com.darkcraft.pueblo.gen;

import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;

/**
 * El muelle de Valdrenn (parte 3d), donde atraca La Garza Gris al llegar de Brumavela: un anden de piedra negra pegado al
 * costado oeste del camino de nubes que sale de la Torre de Ascenso, sobre pilotes de basalto que se hunden en la nube.
 * El barco queda amarrado a su oeste con la proa al sur, igual que en Brumavela.
 * <p>
 * Esta dibujado <b>en las coordenadas del barco en Brumavela</b> (el camino de nubes cae de {@code X1 + 1} a
 * {@code X1 + 3}; el portalon del barco, en {@code Barco.PALO + 1} y {@code + 2}): no se genera con el mundo; se apunta en
 * un lienzo sin mundo y se pone, desplazado, la primera vez que alguien se acerca o zarpa hacia alli
 * ({@code pueblo/Amarres}). El santuario del muelle lo pone tambien {@code Amarres} (es un bloque del mod).
 */
public final class MuelleValdrenn extends Element {
    /** El anden (su suelo esta a la altura de la cubierta del barco y del camino de nubes). */
    public static final int X0 = -3, X1 = 1, Z0 = 127, Z1 = 151, Y = Barco.CUBIERTA;
    /** El poste de la campana con la que se llama al barco (la campana va encima: {@code Y + 2}). */
    public static final int CAMPANA_X = -1, CAMPANA_Z = 130;
    /** Donde va el santuario y donde se deja a quien llega sin barco que pisar. */
    public static final int SANTUARIO_X = -1, SANTUARIO_Z = 147, SEGURO_X = 0, SEGURO_Z = 140;

    public MuelleValdrenn(long seed) {
        super(seed);
        this.layer = 4;
        bounds(X0, Y - 14, Z0, X1, Y + 5, Z1);
    }

    /** La campana del muelle, de pie en su poste. */
    public static BlockState campana() {
        return Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.FLOOR).setValue(BellBlock.FACING, Direction.NORTH);
    }

    @Override
    public void build(Canvas c) {
        for (int x = X0; x <= X1; x++)
            for (int z = Z0; z <= Z1; z++) {
                boolean borde = x == X0 || z == Z0 || z == Z1;
                int p = Hash.pct(x, Y, z, seed + 1);
                BlockState s;
                if (borde) s = p < 14 ? Pal.PB_BRICKS_CRACKED : Pal.PB_BRICKS;
                else if (Math.abs(x - SANTUARIO_X) <= 1 && Math.abs(z - SANTUARIO_Z) <= 1) s = x == SANTUARIO_X && z == SANTUARIO_Z ? Pal.PB_CHISELED : Pal.PB;
                else if (Math.floorMod(z - Z0, 4) == 0) s = Pal.DS_POLISHED;
                else s = p < 12 ? Pal.DS_TILES_CRACKED : Pal.DS_TILES;
                c.set(x, Y, z, s);
                // el canto del anden, de dos de grueso
                if (borde) c.set(x, Y - 1, z, Pal.DS_BRICKS);
                for (int y = Y + 1; y <= Y + 5; y++) c.set(x, y, z, Pal.AIR);
            }
        // los pilotes
        int[][] pilotes = {{X0, Z0}, {X0, 133}, {X0, 139}, {X0, 145}, {X0, Z1}, {X1, Z0}, {X1, 139}, {X1, Z1}, {-1, Z0}, {-1, Z1}};
        for (int[] q : pilotes) for (int y = Y - 14; y <= Y - 2; y++) c.set(q[0], y, q[1], Pal.BASALT);
        // dos farolas en las esquinas del lado del barco
        for (int z : new int[]{Z0, Z1}) {
            for (int k = 1; k <= 3; k++) c.set(X0, Y + k, z, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
            c.set(X0, Y + 4, z, Pal.SOUL_LANTERN);
        }
        // el pretil de los dos extremos
        for (int x = X0 + 1; x <= X1 - 1; x++) {
            c.unir(x, Y + 1, Z0, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
            c.unir(x, Y + 1, Z1, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        }
        // los norayes, a los lados del portalon
        for (int z : new int[]{132, 144}) {
            c.set(X0, Y + 1, z, Pal.DO_LOG);
            c.set(X0, Y + 2, z, Pal.slab(Blocks.DARK_OAK_SLAB, false));
        }
        // la campana para llamar al barco
        c.set(CAMPANA_X, Y + 1, CAMPANA_Z, Pal.DO_LOG);
        c.set(CAMPANA_X, Y + 2, CAMPANA_Z, campana());
        // lo que espera a embarcar
        c.set(X1, Y + 1, Z1 - 1, Mat.barril(Direction.UP));
        c.set(X1 - 1, Y + 1, Z1 - 1, Pal.SP_PLANKS);
        c.set(X1, Y + 2, Z1 - 1, Mat.barril(Direction.WEST));
        c.set(X1, Y + 1, Z1 - 2, Mat.barril(Direction.UP));
        c.set(X1, Y + 1, Z0 + 1, Pal.SP_PLANKS);
        c.set(X1, Y + 1, Z0 + 2, Mat.barril(Direction.UP));
    }
}
