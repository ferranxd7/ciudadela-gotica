package com.darkcraft.pueblo.gen;

import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.world.NubeBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * La Garza Gris, el barco de Brumavela, amarrado al oeste del espigon con la proa al sur (hacia el mar de nubes). Roto,
 * como se encuentra: el palo tronchado, sin vela, sin timon, la cadena del ancla colgando vacia, la cubierta con
 * boquetes y la cuna del corazon vacia en la bodega. Reparado ({@code entero}): el palo con su cofa y sus dos vergas, las
 * velas grises, la rueda del timon en el alcazar, el ancla en su serviola y el corazon alumbrando la bodega. El casco es
 * el mismo: reparar es volver a levantar esta pieza encima (ver {@code pueblo/Pueblo}).
 */
public final class Barco extends Element {
    /** La linea de crujia, la popa (norte) y la proa (sur), y la cubierta (el bloque: se pisa uno mas arriba). */
    public static final int BX = -8, POPA = 122, PROA = 150, CUBIERTA = Terreno.Q;
    /** El palo mayor, el alcazar (hasta donde llega, y su cubierta) y el castillo de proa (desde donde). */
    public static final int PALO = 135, ALCAZAR = 128, ALCAZAR_Y = CUBIERTA + 4, CASTILLO = 143;
    /** El timon (en el alcazar), la escotilla y la cuna del corazon (en la bodega). */
    public static final int TIMON_Z = 125, ESCOTILLA_Z = 131, CORAZON_Z = 139;
    public static final int PALO_ALTO = 23;

    public final boolean entero;

    /** La semilla de este barco (para volver a levantarlo igual, entero). */
    public long semilla() {
        return seed;
    }

    /** Las tablas de la cubierta: roble oscuro, con alguna de abeto. */
    private BlockState tablazon(int x, int y, int z) {
        int p = Hash.pct(x, y, z, seed + 6);
        return p < 66 ? Pal.DO_PLANKS : p < 90 ? Pal.SP_PLANKS : Pal.b(Blocks.STRIPPED_DARK_OAK_WOOD);
    }

    public Barco(long seed, boolean entero) {
        super(seed);
        this.layer = 5;
        this.entero = entero;
        bounds(BX - 7, CUBIERTA - 6, POPA - 2, BX + 7, CUBIERTA + PALO_ALTO + 4, PROA + 9);
    }

    public static void sitios(PuebloPlan plan) {
        plan.sitio("barco", BX, CUBIERTA + 1, PALO + 2);
        plan.sitio("barco_timon", BX, ALCAZAR_Y + 1, TIMON_Z);
        plan.sitio("barco_corazon", BX, CUBIERTA - 3, CORAZON_Z);
        plan.sitio("barco_bodega", BX, CUBIERTA - 4, CORAZON_Z - 2);
    }

    /** Media manga del casco en esa cuaderna (-1 = fuera del barco). */
    public static int manga(int z) {
        if (z < POPA || z > PROA) return -1;
        if (z == POPA) return 2;
        if (z <= POPA + 4) return 3;
        if (z <= PROA - 8) return 4;
        if (z <= PROA - 5) return 3;
        if (z <= PROA - 3) return 2;
        if (z <= PROA - 1) return 1;
        return 0;
    }

    /** Hasta donde llega el casco a esa altura (de la quilla a la cubierta). */
    private static int ancho(int m, int y) {
        int d = CUBIERTA - y;
        return d >= 5 ? m - 3 : d == 4 ? m - 2 : d == 3 ? m - 1 : m;
    }

    /** Si esa celda cae dentro del casco, por dentro del forro (el aire de la bodega y lo que haya en ella). */
    public static boolean dentro(int x, int y, int z) {
        if (y < CUBIERTA - 4 || y >= CUBIERTA || z <= POPA || z >= PROA) return false;
        int a = Math.abs(x - BX);
        return a < ancho(manga(z), y) && a < ancho(manga(z - 1), y) && a < ancho(manga(z + 1), y);
    }

    private BlockState forro(int x, int y, int z) {
        int p = Hash.pct(x, y, z, seed + 1);
        if (y <= CUBIERTA - 4) return p < 70 ? Pal.DO_PLANKS : Pal.b(Blocks.STRIPPED_DARK_OAK_WOOD);
        if (y == CUBIERTA - 1) return Pal.b(Blocks.STRIPPED_SPRUCE_WOOD);
        return p < 64 ? Pal.SP_PLANKS : p < 86 ? Pal.DO_PLANKS : Pal.b(Blocks.STRIPPED_SPRUCE_WOOD);
    }

    /** Los boquetes de la cubierta del barco roto (se cae a la bodega, no al vacio). */
    private boolean boquete(int x, int z) {
        if (entero) return false;
        return Math.hypot(x - (BX + 2), z - (PALO + 5)) < 1.7 || Math.hypot(x - (BX + 1), z - (PALO - 2)) < 1.3
                || (Math.abs(x - BX) <= 1 && z == CASTILLO - 2 && Hash.pct(x, 0, z, seed + 2) < 60);
    }

    /**
     * Quita lo que haya del barco (no las nubes), para volver a levantarlo entero o roto. Fuera de la manga del casco
     * esta el espigon, con una farola que llega a {@code CUBIERTA + 4}: ahi solo se quita lo que vuela por encima, que
     * son las vergas y las velas (la mayor empieza en {@code CUBIERTA + 5}).
     */
    public void despejar(Canvas c) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) {
                boolean casco = x >= BX - 5 && x <= BX + 4;
                for (int y = y0; y <= y1; y++) {
                    if (!casco && y < CUBIERTA + 5) continue;
                    BlockState s = c.get(x, y, z);
                    if (!s.isAir() && !(s.getBlock() instanceof NubeBlock)) c.set(x, y, z, Pal.AIR);
                }
            }
    }

    @Override
    public void build(Canvas c) {
        casco(c);
        alcazar(c);
        castillo(c);
        bodega(c);
        palo(c);
        proa(c);
        detalles(c);
    }

    private void casco(Canvas c) {
        for (int z = POPA; z <= PROA; z++) {
            int m = manga(z);
            for (int y = CUBIERTA - 5; y <= CUBIERTA - 1; y++) {
                int w = ancho(m, y), abajo = y == CUBIERTA - 5 ? -1 : ancho(m, y - 1);
                boolean cierre = z == POPA || z == PROA || ancho(manga(z - 1), y) < 0 || ancho(manga(z + 1), y) < 0;
                for (int dx = -m; dx <= m; dx++) {
                    int x = BX + dx, a = Math.abs(dx);
                    if (a > w) continue;
                    boolean forro = a == w || a > abajo || cierre || a > ancho(manga(z - 1), y) || a > ancho(manga(z + 1), y);
                    c.set(x, y, z, forro ? forro(x, y, z) : Pal.AIR);
                }
            }
            // la cubierta y la borda
            for (int dx = -m; dx <= m; dx++) {
                int x = BX + dx;
                boolean escotilla = dx == 0 && z == ESCOTILLA_Z;
                if (escotilla || boquete(x, z)) c.set(x, CUBIERTA, z, Pal.AIR);
                else c.set(x, CUBIERTA, z, tablazon(x, CUBIERTA, z));
                for (int y = CUBIERTA + 1; y <= CUBIERTA + 8; y++) c.set(x, y, z, Pal.AIR);
            }
            if (z > ALCAZAR && z < CASTILLO) {
                for (int lado = -1; lado <= 1; lado += 2) {
                    int x = BX + lado * m;
                    // el portalon, por donde se sube desde el espigon
                    if (lado == 1 && (z == PALO + 1 || z == PALO + 2)) continue;
                    // la borda: entera del lado de las nubes; del lado del espigon, rota a trozos en el barco roto
                    if (!entero && lado == 1 && Hash.pct(x, 1, z, seed + 3) < 30) continue;
                    c.set(x, CUBIERTA + 1, z, forro(x, CUBIERTA - 1, z));
                    if ((z - POPA) % 4 == 2) c.set(x, CUBIERTA + 2, z, Pal.SP_FENCE);
                }
            }
        }
        // la quilla asoma por delante y por detras, y el codaste
        c.set(BX, CUBIERTA - 3, POPA - 1, Pal.DO_LOG);
        c.set(BX, CUBIERTA - 2, POPA - 1, Pal.DO_LOG);
    }

    private void alcazar(Canvas c) {
        // los costados suben, la cubierta del alcazar y la camara debajo
        for (int z = POPA; z <= ALCAZAR; z++) {
            int m = manga(z);
            for (int dx = -m; dx <= m; dx++) {
                int x = BX + dx;
                boolean costado = Math.abs(dx) == m || z == POPA || z == ALCAZAR;
                for (int y = CUBIERTA + 1; y <= ALCAZAR_Y - 1; y++) {
                    BlockState s = costado ? forro(x, y, z) : Pal.AIR;
                    // las ventanas de popa y los portillos de los costados
                    if (z == POPA && Math.abs(dx) <= 1 && y == CUBIERTA + 2) {
                        c.unir(x, y, z, Pal.b(Blocks.GLASS_PANE));
                        continue;
                    }
                    if (Math.abs(dx) == m && z == POPA + 3 && y == CUBIERTA + 2) {
                        c.unir(x, y, z, Pal.b(Blocks.GLASS_PANE));
                        continue;
                    }
                    // la puerta de la camara: sin hoja
                    if (z == ALCAZAR && dx == 0 && y <= CUBIERTA + 2) s = Pal.AIR;
                    c.set(x, y, z, s);
                }
                boolean roto = !entero && Math.hypot(dx - 3, z - (POPA + 1)) < 1.6;
                c.set(x, ALCAZAR_Y, z, roto ? Pal.AIR : tablazon(x, ALCAZAR_Y, z));
                // la barandilla
                boolean borde = Math.abs(dx) == m || z == POPA || (z == ALCAZAR && dx != -m + 1);
                if (borde && !roto) c.unir(x, ALCAZAR_Y + 1, z, Pal.SP_FENCE);
            }
        }
        // la escalera del costado de babor (al oeste) hasta el alcazar
        int ex = BX - manga(ALCAZAR + 1) + 1;
        for (int k = 0; k < 4; k++) {
            int z = ALCAZAR + 4 - k, y = CUBIERTA + 1 + k;
            c.set(ex, y, z, Pal.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, false));
            for (int yy = CUBIERTA + 1; yy < y; yy++) c.set(ex, yy, z, Pal.SP_PLANKS);
        }
        c.set(ex, ALCAZAR_Y + 1, ALCAZAR, Pal.AIR);
        // la camara del capitan: la mesa, el catre y un cofre
        c.set(BX + 2, CUBIERTA + 1, POPA + 1, Pal.SP_FENCE);
        c.set(BX + 2, CUBIERTA + 2, POPA + 1, Pal.b(Blocks.SPRUCE_PRESSURE_PLATE));
        c.set(BX - 2, CUBIERTA + 1, POPA + 2, Pal.slab(Blocks.SPRUCE_SLAB, false));
        c.set(BX - 2, CUBIERTA + 1, POPA + 3, Pal.slab(Blocks.SPRUCE_SLAB, false));
        c.set(BX - 2, CUBIERTA + 1, POPA + 1, Mat.barril(Direction.UP));
        c.set(BX, ALCAZAR_Y - 1, POPA + 2, Pal.SOUL_LANTERN_HANG);
        if (!entero) c.set(BX + 2, CUBIERTA + 3, POPA + 4, Pal.COBWEB);
        // el timon: su pie, y la rueda si esta puesta
        c.set(BX, ALCAZAR_Y + 1, TIMON_Z, Pal.SP_FENCE);
        if (entero) c.set(BX, ALCAZAR_Y + 2, TIMON_Z, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                .setValue(GrindstoneBlock.FACING, Direction.EAST));
        // el farol de popa
        c.set(BX, ALCAZAR_Y + 1, POPA, Pal.DO_LOG);
        c.set(BX, ALCAZAR_Y + 2, POPA, Pal.SP_FENCE);
        c.set(BX, ALCAZAR_Y + 3, POPA, Pal.SOUL_LANTERN);
    }

    private void castillo(Canvas c) {
        for (int z = CASTILLO; z <= PROA; z++) {
            int m = manga(z);
            for (int dx = -m; dx <= m; dx++) {
                int x = BX + dx;
                c.set(x, CUBIERTA + 1, z, tablazon(x, CUBIERTA + 1, z));
                if (Math.abs(dx) == m && m > 0 && (entero || Hash.pct(x, 2, z, seed + 3) < 70)) c.unir(x, CUBIERTA + 2, z, Pal.SP_FENCE);
            }
        }
        // dos peldanos de media altura para subir desde la cubierta
        for (int dx = -1; dx <= 1; dx++) c.set(BX + dx, CUBIERTA + 1, CASTILLO - 1, Pal.slab(Blocks.SPRUCE_SLAB, false));
    }

    private void bodega(Canvas c) {
        // la escala de la escotilla, contra un mamparo
        for (int y = CUBIERTA - 4; y <= CUBIERTA; y++) {
            if (y < CUBIERTA) c.set(BX, y, ESCOTILLA_Z - 1, Pal.DO_PLANKS);
            c.set(BX, y, ESCOTILLA_Z, Mat.escalera(Direction.SOUTH));
        }
        // la cuna del corazon: un pedestal entre cuatro cadenas
        c.set(BX, CUBIERTA - 4, CORAZON_Z, Pal.PB_CHISELED);
        c.set(BX - 1, CUBIERTA - 1, CORAZON_Z, Pal.CHAIN);
        c.set(BX + 1, CUBIERTA - 1, CORAZON_Z, Pal.CHAIN);
        c.set(BX, CUBIERTA - 3, CORAZON_Z, entero ? Blocks.LIGHT.defaultBlockState() : Pal.AIR);
        // la carga: barriles y cajas hacia proa, y telaranas
        int[][] carga = {{-1, CORAZON_Z + 2}, {1, CORAZON_Z + 2}, {0, CORAZON_Z + 3}, {1, CORAZON_Z + 3}, {-1, CORAZON_Z + 3}, {1, ESCOTILLA_Z - 2}};
        for (int[] k : carga) c.set(BX + k[0], CUBIERTA - 4, k[1], Hash.pct(k[0], 3, k[1], seed + 4) < 60 ? Mat.barril(Direction.UP) : Pal.SP_PLANKS);
        c.set(BX + 1, CUBIERTA - 3, CORAZON_Z + 3, Mat.barril(Direction.NORTH));
        if (!entero) {
            c.set(BX + 2, CUBIERTA - 2, CORAZON_Z - 3, Pal.COBWEB);
            c.set(BX - 2, CUBIERTA - 2, CORAZON_Z + 2, Pal.COBWEB);
        } else c.set(BX, CUBIERTA - 1, CORAZON_Z - 4, Pal.SOUL_LANTERN_HANG);
    }

    private void palo(Canvas c) {
        int y0 = CUBIERTA + 1;
        // el pie del palo baja hasta la quilla
        for (int y = CUBIERTA - 4; y <= CUBIERTA; y++) c.set(BX, y, PALO, Pal.SP_LOG);
        if (!entero) {
            // tronchado a media altura: el tocon astillado y un trozo caido sobre la cubierta
            for (int y = y0; y <= y0 + 2; y++) c.set(BX, y, PALO, Pal.SP_LOG);
            c.set(BX, y0 + 3, PALO, Pal.SP_FENCE);
            for (int k = 1; k <= 4; k++) c.set(BX - 1 - k / 3, y0, PALO + 1 + k, Pal.log(Blocks.SPRUCE_LOG, Direction.Axis.Z));
            return;
        }
        int cima = y0 + PALO_ALTO - 1;
        for (int y = y0; y <= cima; y++) c.set(BX, y, PALO, y > cima - 3 ? Pal.SP_FENCE : Pal.SP_LOG);
        // las dos vergas y la cofa
        int mayor = y0 + 12, gavia = y0 + 18;
        for (int dx = -6; dx <= 6; dx++) if (dx != 0) c.set(BX + dx, mayor, PALO, Pal.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        for (int dx = -4; dx <= 4; dx++) if (dx != 0) c.set(BX + dx, gavia, PALO, Pal.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                if (dx != 0 || dz != 0) c.set(BX + dx, mayor + 2, PALO + dz, Pal.slab(Blocks.SPRUCE_SLAB, true));
        // las velas: la mayor y la gavia, grises y remendadas, un poco hinchadas hacia proa
        for (int dx = -6; dx <= 6; dx++)
            for (int y = y0 + 4; y < mayor; y++) {
                int panza = Math.abs(dx) <= 4 && y > y0 + 5 && y < mayor - 1 ? 2 : 1;
                c.set(BX + dx, y, PALO + panza, lona(BX + dx, y));
            }
        for (int dx = -4; dx <= 4; dx++)
            for (int y = mayor + 3; y < gavia; y++) c.set(BX + dx, y, PALO + (Math.abs(dx) <= 2 && y > mayor + 3 ? 2 : 1), lona(BX + dx, y));
        // el gallardete
        c.set(BX, cima + 1, PALO, Pal.b(Blocks.LIGHTNING_ROD));
        c.set(BX, cima, PALO + 1, Pal.b(Blocks.RED_WOOL));
        c.set(BX, cima, PALO + 2, Pal.b(Blocks.RED_WOOL));
    }

    private BlockState lona(int x, int y) {
        // panos a lo alto, con remiendos grandes (no a pixeles sueltos)
        int p = Hash.pct(x >> 1, y / 3, 7, seed + 5), q = Hash.pct(x, y, 9, seed + 5);
        if (p < 3) return Pal.b(Blocks.GRAY_WOOL);
        if (Math.floorMod(x - BX, 4) == 2) return Pal.b(q < 85 ? Blocks.WHITE_WOOL : Blocks.LIGHT_GRAY_WOOL);
        return Pal.b(q < 8 ? Blocks.WHITE_WOOL : Blocks.LIGHT_GRAY_WOOL);
    }

    private void proa(Canvas c) {
        // la roda y el baupres
        c.set(BX, CUBIERTA + 2, PROA, Pal.DO_LOG);
        for (int k = 1; k <= 6; k++) c.set(BX, CUBIERTA + 1 + (k + 1) / 3, PROA + k, Pal.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Z));
        c.set(BX, CUBIERTA + 2, PROA + 6, Pal.SOUL_LANTERN_HANG);
        // la serviola del ancla, a estribor (del lado del espigon): la cadena colgando vacia, o recogida con el ancla
        // (que no es un bloque: un yunque colgado se cae; la pinta pueblo/Pueblo con el dibujo de la pieza)
        int[] a = ancla();
        c.set(a[0], CUBIERTA + 1, a[2], Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
        for (int k = 0; k < (entero ? 1 : 4); k++) c.set(a[0], CUBIERTA - k, a[2], Pal.CHAIN);
    }

    /** Donde cuelga el ancla del barco reparado: bajo la serviola (el bloque de su eslabon). */
    public static int[] ancla() {
        return new int[]{BX + manga(PROA - 4) + 1, CUBIERTA, PROA - 4};
    }

    private void detalles(Canvas c) {
        // lo que queda en cubierta
        c.set(BX - 3, CUBIERTA + 1, PALO + 4, Mat.barril(Direction.UP));
        c.set(BX + 3, CUBIERTA + 1, ALCAZAR + 2, Mat.barril(Direction.UP));
        c.set(BX + 3, CUBIERTA + 1, ALCAZAR + 3, Pal.SP_PLANKS);
        if (!entero) {
            c.set(BX - 2, CUBIERTA + 1, CASTILLO - 3, Pal.COBWEB);
            c.set(BX + 1, CUBIERTA + 2, PALO, Pal.COBWEB);
        }
    }
}
