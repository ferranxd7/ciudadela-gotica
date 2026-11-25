package com.darkcraft.pueblo.gen;

import com.darkcraft.fortaleza.Perdidos;
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
 * La capilla de Brumavela, en lo alto de la colina del noroeste: se entra por el pie del campanario (al este), la nave
 * va hacia el oeste con sus bancos y el altar, y debajo esta la cripta, cerrada por una losa oscura delante del altar
 * que solo se aparta al tocar las tres campanas en el orden en que murieron los fundadores. Las campanas cuelgan en los
 * arcos del campanario (norte, este y sur); se sube por la escalera de mano de dentro de la torre.
 */
public final class Capilla extends Element {
    /** El centro del campanario y la nave (por fuera). */
    public static final int TX = -41, TZ = -46;
    public static final int NX0 = -62, NX1 = TX - 3, NZ0 = -52, NZ1 = -40;
    /** La boca de la cripta en el suelo de la nave (la tapa la losa) y la cripta (por dentro). */
    public static final int BX0 = -57, BX1 = -53, BZ0 = TZ - 1, BZ1 = TZ + 1;
    public static final int CX0 = -60, CX1 = -47, CZ0 = -50, CZ1 = -42;

    /** Lo que se pisa en la nave, en el campanario (arriba) y en la cripta. */
    public final int y, campanario, cripta;
    /** A que altura cuelgan las campanas y el dintel de sus arcos. */
    private final int campanaY, dintel;
    private final Terreno terreno;

    public Capilla(long seed, PuebloPlan plan) {
        super(seed);
        this.layer = 3;
        this.terreno = plan.terreno;
        int alto = Terreno.NADA;
        for (int x = NX0; x <= TX + 3; x++)
            for (int z = NZ0; z <= NZ1; z++) alto = Math.max(alto, terreno.alto(x, z));
        this.y = alto + 1;
        this.campanario = y + 17;
        this.campanaY = campanario + 3;
        this.dintel = campanario + 4;
        this.cripta = y - 5;
        bounds(NX0 - 2, y - 9, NZ0 - 2, TX + 5, y + 30, NZ1 + 2);
        // las campanas: la del Pescador mira al muelle (sur), la de la Tejedora al norte y la del Farero al faro (este)
        plan.sitio("campana_pescador", TX, campanaY, TZ + 3);
        plan.sitio("campana_tejedora", TX, campanaY, TZ - 3);
        plan.sitio("campana_farero", TX + 3, campanaY, TZ);
        plan.sitio("campanario", TX, campanario, TZ);
        plan.sitio("capilla", TX + 5, y, TZ);
        plan.sitio("nave", -50, y, TZ);
        plan.sitio("losa", (BX0 + BX1) / 2, y, TZ);
        plan.sitio("cripta", CX0 + 3, cripta, TZ);
        plan.sitio("mastil", CX0 + 2, cripta, CZ0);
        plan.sitio("nota_campanas", NX0 + 4, y, TZ - 3);
    }

    @Override
    public void build(Canvas c) {
        cimientos(c);
        hacerCripta(c);
        nave(c);
        tejado(c);
        roseton(c);
        dentro(c);
        torre(c);
    }

    private boolean enNave(int x, int z) {
        return x >= NX0 && x <= NX1 && z >= NZ0 && z <= NZ1;
    }

    private boolean enTorre(int x, int z) {
        return Math.abs(x - TX) <= 3 && Math.abs(z - TZ) <= 3;
    }

    private void cimientos(Canvas c) {
        for (int x = NX0 - 1; x <= TX + 3; x++)
            for (int z = NZ0 - 1; z <= NZ1 + 1; z++) {
                if (!enNave(x, z) && !enTorre(x, z)) continue;
                int t = terreno.alto(x, z);
                for (int yy = t == Terreno.NADA ? y - 6 : Math.min(t + 1, y - 1); yy <= y - 2; yy++) c.set(x, yy, z, Mat.muro(x, yy, z, seed));
                c.set(x, y - 1, z, Mat.losas(x, y - 1, z, seed));
            }
    }

    // ------------------------------------------------------------------ la cripta

    private void hacerCripta(Canvas c) {
        int suelo = cripta - 1, techo = y - 2;
        for (int x = CX0 - 1; x <= CX1 + 1; x++)
            for (int z = CZ0 - 1; z <= CZ1 + 1; z++) {
                boolean borde = x < CX0 || x > CX1 || z < CZ0 || z > CZ1;
                c.set(x, suelo, z, Hash.pct(x, suelo, z, seed + 3) < 70 ? Pal.DS_TILES : Pal.DS_COBBLED);
                for (int yy = cripta; yy <= techo; yy++) c.set(x, yy, z, borde ? Mat.cripta(x, yy, z, seed) : Pal.AIR);
            }
        // la escalera que baja desde la nave hacia el oeste (tres de ancho), sobre su cuna de piedra
        for (int k = 0; k <= 4; k++) {
            int x = BX1 - k, ey = y - 1 - k;
            for (int z = BZ0; z <= BZ1; z++) {
                for (int yy = cripta; yy < ey; yy++) c.set(x, yy, z, Mat.cripta(x, yy, z, seed));
                c.set(x, ey, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.EAST, false));
                for (int yy = ey + 1; yy <= y - 1; yy++) c.set(x, yy, z, Pal.AIR);
            }
        }
        // la losa que la tapa (la quita el servidor cuando suenan las campanas)
        for (int x = BX0; x <= BX1; x++)
            for (int z = BZ0; z <= BZ1; z++) c.set(x, y, z, losa(x, z));
        // sepulcros junto al muro del sur, con su tapa
        for (int[] s : new int[][]{{CX0, CZ1}, {CX0 + 4, CZ1}, {CX1 - 4, CZ1}, {CX1 - 1, CZ1}}) {
            for (int k = 0; k < 2; k++) {
                c.set(s[0] + k, cripta, s[1], Pal.STONE_BRICKS);
                c.set(s[0] + k, cripta + 1, s[1], Pal.slab(Blocks.SMOOTH_STONE_SLAB, false));
            }
        }
        // el osario del muro del oeste: huesos y calaveras, y velas apagadas
        for (int z = CZ0 + 2; z <= CZ1 - 2; z++) {
            c.set(CX0, cripta, z, Pal.log(Blocks.BONE_BLOCK, Direction.Axis.Z));
            c.set(CX0, cripta + 1, z, Hash.pct(CX0, 0, z, seed + 5) < 60 ? Mat.calavera(4 + Hash.pct(CX0, 1, z, seed + 5) % 3 - 1) : Mat.velas(1 + z % 3));
        }
        // la mesa del mastil, junto al muro del norte: dos apoyos de piedra y cadenas
        c.set(CX0 + 1, cripta, CZ0, Pal.DS_POLISHED);
        c.set(CX0 + 3, cripta, CZ0, Pal.DS_POLISHED);
        c.set(CX0 + 2, cripta, CZ0, Pal.slab(Blocks.POLISHED_DEEPSLATE_SLAB, false));
        Mat.farolColgado(c, CX0 + 5, techo, CZ0 + 1, 1);
        Mat.farolColgado(c, CX1 - 3, techo, CZ1 - 1, 1);
        // el arcon, tras la escalera, y telaranas
        c.chest(CX1, cripta, CZ0, Direction.SOUTH, Casa.BOTIN_BUENO);
        c.set(CX1, techo, CZ1, Pal.COBWEB);
        c.set(CX0, techo, CZ0, Pal.COBWEB);
        c.set(CX1, techo, CZ0 + 1, Pal.COBWEB);
        c.set(CX1 - 1, cripta, CZ0 + 3, Pal.log(Blocks.BONE_BLOCK, Direction.Axis.X));
        c.set(CX1, cripta, CZ0 + 4, Mat.calavera(11));
    }

    /** La losa de la cripta: piedra negra pulida con el centro labrado. */
    public static BlockState losa(int x, int z) {
        return Pal.slab(x == (BX0 + BX1) / 2 && z == TZ ? Blocks.DEEPSLATE_TILE_SLAB : Blocks.POLISHED_DEEPSLATE_SLAB, false);
    }

    // ------------------------------------------------------------------ la nave

    private static boolean ventanal(int x) {
        return x > NX0 + 1 && x < NX1 - 1 && Math.floorMod(x - NX0, 4) == 2;
    }

    private static boolean contrafuerte(int x) {
        return x >= NX0 && x < NX1 && Math.floorMod(x - NX0, 4) == 0;
    }

    private void nave(Canvas c) {
        int cima = y + 5;
        for (int x = NX0; x <= NX1; x++)
            for (int z = NZ0; z <= NZ1; z++) {
                boolean ex = x == NX0 || x == NX1, ez = z == NZ0 || z == NZ1;
                if (!ex && !ez) {
                    // dentro: aire hasta el tejado (menos donde baja la escalera de la cripta, que ya esta hecha)
                    for (int yy = y; yy <= cima + 8; yy++)
                        if (!(yy == y && x >= BX0 && x <= BX1 && z >= BZ0 && z <= BZ1)) c.set(x, yy, z, Pal.AIR);
                    continue;
                }
                for (int yy = y; yy <= cima; yy++) {
                    BlockState s = yy == y ? Pal.STONE_BRICKS : Mat.muro(x, yy, z, seed);
                    if (ez && !ex && ventanal(x) && yy >= y + 2 && yy <= y + 4) {
                        // vidrieras: casi todas rotas
                        int p = Hash.pct(x, yy, z, seed + 7);
                        if (p < 45) s = Pal.AIR;
                        else {
                            c.unir(x, yy, z, Pal.b(p < 65 ? Blocks.RED_STAINED_GLASS_PANE : p < 82 ? Blocks.BLUE_STAINED_GLASS_PANE : Blocks.IRON_BARS));
                            continue;
                        }
                    }
                    c.set(x, yy, z, s);
                }
            }
        // los contrafuertes, por fuera de los muros largos
        for (int x = NX0; x < NX1; x++) {
            if (!contrafuerte(x)) continue;
            for (int lado = 0; lado < 2; lado++) {
                int z = lado == 0 ? NZ0 - 1 : NZ1 + 1;
                int t = terreno.alto(x, z);
                for (int yy = t == Terreno.NADA ? y - 3 : t + 1; yy <= y + 3; yy++) c.set(x, yy, z, Mat.sillar(x, yy, z, seed));
                c.set(x, y + 4, z, Pal.stairs(Blocks.STONE_BRICK_STAIRS, lado == 0 ? Direction.SOUTH : Direction.NORTH, false));
            }
        }
    }

    /** El roseton del muro del oeste, sobre el altar. */
    private void roseton(Canvas c) {
        for (int dz = -1; dz <= 1; dz++)
            for (int dy = -1; dy <= 1; dy++) {
                if (Math.abs(dz) + Math.abs(dy) == 2) continue;
                int p = Hash.pct(NX0, y + 7 + dy, TZ + dz, seed + 8);
                c.set(NX0, y + 7 + dy, TZ + dz, p < 30 ? Pal.AIR : p < 70 ? Pal.GLASS_RED : Pal.GLASS_PURPLE);
            }
    }

    /** Si ese trozo del tejado se ha hundido: dos boquetes por los que entra la luz. */
    private boolean hundido(int x, int z) {
        return Math.hypot(x - (NX0 + 6), z - (TZ + 3)) < 2.6 || Math.hypot(x - (NX1 - 5), z - (TZ - 4)) < 2.1;
    }

    private void tejado(Canvas c) {
        int cima = y + 5;
        for (int k = 0; ; k++) {
            int lo = NZ0 - 1 + k, hi = NZ1 + 1 - k, yy = cima + k;
            if (lo > hi) break;
            for (int x = NX0 - 1; x <= NX1; x++) {
                for (int lado = 0; lado < 2; lado++) {
                    int z = lado == 0 ? lo : hi;
                    if (lado == 1 && lo == hi) break;
                    if (hundido(x, z)) {
                        if (Hash.pct(x, yy, z, seed + 11) < 12) c.set(x, yy, z, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
                        continue;
                    }
                    if (lo == hi) c.set(x, yy, z, Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false));
                    else c.set(x, yy, z, Pal.stairs(Hash.pct(x, yy, z, seed + 12) < 14 ? Blocks.COBBLED_DEEPSLATE_STAIRS : Blocks.DEEPSLATE_TILE_STAIRS,
                            lado == 0 ? Direction.SOUTH : Direction.NORTH, false));
                }
            }
            // los hastiales (el del este queda contra la torre)
            if (yy > cima)
                for (int z = lo + 1; z <= hi - 1; z++) {
                    c.set(NX0, yy, z, Mat.muro(NX0, yy, z, seed));
                    c.set(NX1, yy, z, Mat.muro(NX1, yy, z, seed));
                }
            // las cerchas: una viga de lado a lado cada cuatro
            if (k == 1)
                for (int x = NX0 + 4; x < NX1; x += 4)
                    for (int z = NZ0 + 1; z <= NZ1 - 1; z++) c.set(x, cima + 1, z, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        }
    }

    private void dentro(Canvas c) {
        // el pasillo: restos de la alfombra
        for (int x = NX0 + 4; x <= NX1 - 1; x++) {
            if (x >= BX0 && x <= BX1) continue;
            for (int z = TZ - 1; z <= TZ + 1; z++)
                if (Hash.pct(x, y, z, seed + 20) < (z == TZ ? 62 : 28)) c.set(x, y, z, Pal.b(Blocks.RED_CARPET));
        }
        // los bancos, mirando al altar
        for (int x = NX0 + 6; x <= NX1 - 3; x += 2) {
            for (int z = NZ0 + 2; z <= NZ1 - 2; z++) {
                if (Math.abs(z - TZ) <= 1) continue;
                int p = Hash.pct(x, y, z, seed + 21);
                if (p < 14) continue;
                c.set(x, y, z, p < 22 ? Pal.slab(Blocks.SPRUCE_SLAB, false) : Pal.stairs(Blocks.SPRUCE_STAIRS, Direction.EAST, false));
            }
        }
        // el presbiterio: un escalon, el altar con sus velas y el atril del sermon
        for (int x = NX0 + 1; x <= NX0 + 3; x++)
            for (int z = NZ0 + 1; z <= NZ1 - 1; z++) c.set(x, y, z, x == NX0 + 3 ? Pal.slab(Blocks.STONE_BRICK_SLAB, false) : Pal.STONE_BRICKS);
        for (int z = TZ - 1; z <= TZ + 1; z++) {
            c.set(NX0 + 1, y + 1, z, z == TZ ? Pal.b(Blocks.CHISELED_STONE_BRICKS) : Pal.STONE_BRICKS);
            c.set(NX0 + 1, y + 2, z, z == TZ ? Mat.velas(3) : Mat.velas(1));
        }
        c.lectern(NX0 + 4, y, TZ - 3, Direction.EAST, Perdidos.libro(14));
        c.set(NX0 + 1, y + 1, NZ0 + 1, Pal.b(Blocks.DECORATED_POT));
        c.set(NX0 + 1, y + 1, NZ1 - 1, Pal.b(Blocks.DECORATED_POT));
        // faroles de almas colgados de las cerchas, telaranas y escombros bajo los boquetes
        Mat.farolColgado(c, NX0 + 8, y + 5, TZ, 2);
        Mat.farolColgado(c, NX1 - 6, y + 5, TZ, 2);
        c.set(NX0 + 1, y + 5, NZ0 + 1, Pal.COBWEB);
        c.set(NX1 - 1, y + 5, NZ1 - 1, Pal.COBWEB);
        c.set(NX1 - 1, y + 4, NZ0 + 1, Pal.COBWEB);
        int[][] cascotes = {{NX0 + 6, TZ + 3}, {NX0 + 7, TZ + 4}, {NX0 + 5, TZ + 3}, {NX1 - 5, TZ - 4}, {NX1 - 6, TZ - 3}};
        for (int[] k : cascotes) {
            int p = Hash.pct(k[0], y, k[1], seed + 22);
            c.set(k[0], y, k[1], p < 40 ? Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false) : p < 70 ? Pal.b(Blocks.COBBLED_DEEPSLATE) : Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false));
        }
    }

    // ------------------------------------------------------------------ el campanario

    private void torre(Canvas c) {
        int techo = dintel;
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++) {
                int x = TX + dx, z = TZ + dz;
                boolean muro = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                boolean esquina = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                for (int yy = y; yy <= techo; yy++) {
                    if (!muro) {
                        c.set(x, yy, z, Pal.AIR);
                        continue;
                    }
                    BlockState s = esquina || yy == y ? Mat.sillar(x, yy, z, seed) : Mat.muro(x, yy, z, seed);
                    // la puerta (al este) y el paso a la nave (al oeste): tres de ancho con su arco
                    boolean vano = Math.abs(dx) == 3 && Math.abs(dz) <= 1 && yy <= y + 3;
                    if (vano) {
                        if (yy <= y + 2) s = Pal.AIR;
                        else if (dz != 0) s = Pal.stairs(Blocks.STONE_BRICK_STAIRS, dz < 0 ? Direction.NORTH : Direction.SOUTH, true);
                        else s = Pal.b(Blocks.CHISELED_STONE_BRICKS);
                        if (yy == y + 2 && dz != 0) s = Pal.stairs(Blocks.STONE_BRICK_STAIRS, dz < 0 ? Direction.NORTH : Direction.SOUTH, true);
                        if (yy == y + 3 && dz != 0) s = Mat.sillar(x, yy, z, seed);
                    }
                    // saeteras al norte y al sur
                    if (Math.abs(dz) == 3 && dx == 0 && (yy == y + 8 || yy == y + 9 || yy == y + 13 || yy == y + 14)) s = Pal.AIR;
                    // los arcos del campanario
                    if (yy >= campanario && yy < techo && !esquina) {
                        int k = Math.abs(dx) == 3 ? dz : dx;
                        if (Math.abs(k) <= 1) {
                            if (yy == campanario) {
                                c.unir(x, yy, z, Pal.b(Blocks.COBBLESTONE_WALL));
                                continue;
                            }
                            s = Pal.AIR;
                            if (yy == techo - 1 && k != 0) {
                                Direction d = Math.abs(dx) == 3 ? (k < 0 ? Direction.NORTH : Direction.SOUTH) : (k < 0 ? Direction.WEST : Direction.EAST);
                                s = Pal.stairs(Blocks.STONE_BRICK_STAIRS, d, true);
                            }
                        }
                    }
                    c.set(x, yy, z, s);
                }
            }
        // los suelos de madera (con el hueco de la escalera de mano) y la escalera, en el rincon del noroeste
        int ex = TX - 2, ez = TZ - 2;
        for (int piso : new int[]{y + 6, y + 11, campanario - 1})
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    if (TX + dx == ex && TZ + dz == ez) continue;
                    c.set(TX + dx, piso, TZ + dz, piso == campanario - 1 ? Pal.DO_PLANKS : Mat.tabla(TX + dx, piso, TZ + dz, seed));
                }
        for (int yy = y; yy <= campanario - 1; yy++) c.set(ex, yy, ez, Mat.escalera(Direction.SOUTH));
        // las tres campanas, colgadas en los arcos del sur, del norte y del este
        c.set(TX, campanaY, TZ + 3, campana(Direction.SOUTH));
        c.set(TX, campanaY, TZ - 3, campana(Direction.NORTH));
        c.set(TX + 3, campanaY, TZ, campana(Direction.EAST));
        // la aguja: una piramide de pizarra con alero, hueca
        for (int k = 0; k <= 4; k++) {
            int m = 4 - k, yy = techo + 1 + k;
            for (int dx = -m; dx <= m; dx++)
                for (int dz = -m; dz <= m; dz++) {
                    boolean borde = Math.abs(dx) == m || Math.abs(dz) == m;
                    if (!borde) continue;
                    int x = TX + dx, z = TZ + dz;
                    if (m == 0) {
                        c.set(x, yy, z, Pal.DS_TILES);
                        continue;
                    }
                    Direction d = Math.abs(dz) == m && Math.abs(dx) < m ? (dz < 0 ? Direction.SOUTH : Direction.NORTH)
                            : Math.abs(dx) == m && Math.abs(dz) < m ? (dx < 0 ? Direction.EAST : Direction.WEST)
                            : (dz < 0 ? Direction.SOUTH : Direction.NORTH);
                    c.unir(x, yy, z, Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, d, false));
                }
        }
        c.set(TX, techo + 6, TZ, Pal.b(Blocks.LIGHTNING_ROD));
        // dentro, abajo: un farol, telaranas y un banco
        Mat.farolColgado(c, TX, y + 5, TZ, 1);
        c.set(TX + 2, y + 4, TZ + 2, Pal.COBWEB);
        c.set(TX - 2, y + 10, TZ + 2, Pal.COBWEB);
        c.set(TX + 2, campanario + 3, TZ - 2, Pal.COBWEB);
        c.set(TX + 2, y, TZ + 2, Mat.barril(Direction.UP));
    }

    private static BlockState campana(Direction mira) {
        return Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING).setValue(BellBlock.FACING, mira);
    }
}
