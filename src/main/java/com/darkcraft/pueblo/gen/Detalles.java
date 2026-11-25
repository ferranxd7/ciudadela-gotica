package com.darkcraft.pueblo.gen;

import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Landscape;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.Rect;
import com.darkcraft.gen.Rng;
import com.darkcraft.gen.Town;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo pequeno de Brumavela: el pozo y los puestos de la plaza, las farolas, los carros, el cementerio de la capilla con
 * las tumbas de los tres fundadores, los campos, los arboles muertos, las rocas del borde, el arco de la Cuesta, el
 * Saliente con lo que queda de Tobias y su cadena, y dentro de las casas lo que hace falta para la historia (el arcon de
 * la vela, las notas en sus atriles).
 */
final class Detalles {
    private final PuebloPlan plan;
    private final Terreno t;
    private final long seed;

    /** El cementerio, al sur de la capilla. */
    static final Rect CEMENTERIO = new Rect(-66, -38, -38, -21);
    /** Las tumbas de los fundadores (la lapida): el Pescador, la Tejedora y el Farero. */
    static final int[][] FUNDADORES = {{-58, -37}, {-52, -37}, {-46, -37}};
    static final Rect[] CAMPOS = {new Rect(28, -52, 44, -37), new Rect(50, -50, 64, -36)};

    private Detalles(PuebloPlan plan, long seed) {
        this.plan = plan;
        this.t = plan.terreno;
        this.seed = seed;
    }

    static void poner(PuebloPlan plan, Rng rng) {
        Detalles d = new Detalles(plan, rng.fork());
        d.pozo(PuebloPlan.PLAZA_X, PuebloPlan.PLAZA_Z);
        d.puesto(-6, 0, Direction.EAST, 1);
        d.puesto(6, 12, Direction.WEST, 2);
        d.farolas();
        d.carros(rng);
        d.cementerio();
        for (int i = 0; i < CAMPOS.length; i++) d.campo(CAMPOS[i], i);
        d.arco();
        d.saliente();
        d.mirador();
        d.historia();
        d.arboles(rng);
        d.rocas(rng);
    }

    private int suelo(int x, int z) {
        int a = t.alto(x, z);
        return a == Terreno.NADA ? Terreno.P + 1 : a + 1;
    }

    private void trozo(int capa, int x0, int y0, int z0, int x1, int y1, int z1, Trozo.Obra o) {
        plan.add(new Trozo(capa, x0, y0, z0, x1, y1, z1, o));
    }

    // ------------------------------------------------------------------ la plaza

    private void pozo(int x, int z) {
        int y = suelo(x, z);
        plan.sitio("pozo", x, y, z);
        plan.sitio("santuario", x - 5, y, z + 5);
        plan.ocupar(new Rect(x - 2, z - 2, x + 2, z + 2));
        trozo(4, x - 2, y - 8, z - 2, x + 2, y + 5, z + 2, c -> {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    boolean centro = dx == 0 && dz == 0, esquina = dx != 0 && dz != 0;
                    if (centro) {
                        for (int yy = y - 5; yy <= y + 2; yy++) c.set(x, yy, z, Pal.AIR);
                        c.set(x, y - 6, z, Pal.WATER);
                        c.set(x, y - 7, z, Pal.b(Blocks.MOSSY_COBBLESTONE));
                        continue;
                    }
                    for (int yy = y - 6; yy <= y; yy++) c.set(x + dx, yy, z + dz, Mat.muro(x + dx, yy, z + dz, seed));
                    if (esquina) {
                        c.set(x + dx, y + 1, z + dz, Pal.SP_FENCE);
                        c.set(x + dx, y + 2, z + dz, Pal.SP_FENCE);
                    }
                    c.set(x + dx, y + 3, z + dz, Hash.pct(x + dx, y, z + dz, seed + 1) < 22 ? Pal.AIR : Pal.slab(Blocks.SPRUCE_SLAB, false));
                }
            c.set(x, y + 3, z, Pal.SP_PLANKS);
            c.set(x, y + 2, z, Pal.CHAIN);
            c.set(x, y + 1, z, Pal.CHAIN);
            c.set(x, y, z, Pal.b(Blocks.CAULDRON));
        });
    }

    /** Un puesto del mercado, medio caido: cuatro postes, el toldo roto y el mostrador. */
    private void puesto(int x, int z, Direction mira, int variante) {
        int y = suelo(x, z);
        plan.ocupar(new Rect(x - 2, z - 2, x + 2, z + 2));
        Direction lado = mira.getClockWise();
        trozo(4, x - 3, y - 1, z - 3, x + 3, y + 4, z + 3, c -> {
            for (int a = -2; a <= 2; a++)
                for (int b = -1; b <= 1; b++) {
                    int px = x + lado.getStepX() * a + mira.getStepX() * b, pz = z + lado.getStepZ() * a + mira.getStepZ() * b;
                    boolean poste = Math.abs(a) == 2 && Math.abs(b) == 1;
                    int h = Hash.pct(px, y, pz, seed + 30 + variante);
                    if (poste) {
                        boolean caido = variante == 2 && a == 2 && b == 1;
                        c.set(px, y, pz, Pal.SP_FENCE);
                        if (!caido) c.set(px, y + 1, pz, Pal.SP_FENCE);
                        if (caido) continue;
                    }
                    // el toldo: lana vieja con rotos
                    if (h >= 32 && !(variante == 2 && a >= 1)) c.set(px, y + 2, pz, Pal.b((a + 2) % 2 == 0 ? Blocks.GRAY_WOOL : variante == 1 ? Blocks.RED_WOOL : Blocks.BROWN_WOOL));
                    // el mostrador, por delante
                    if (b == 1 && !poste) c.set(px, y, pz, h < 35 ? Mat.barril(Direction.UP) : h < 80 ? Pal.SP_PLANKS : Pal.slab(Blocks.SPRUCE_SLAB, false));
                    if (b == 1 && !poste && h >= 35 && h < 50) c.set(px, y + 1, pz, Pal.b(Blocks.FLOWER_POT));
                }
            c.set(x - mira.getStepX(), y, z - mira.getStepZ(), Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            c.set(x + lado.getStepX() * 2 - mira.getStepX(), y + 1, z + lado.getStepZ() * 2 - mira.getStepZ(), Pal.COBWEB);
        });
    }

    private void farolas() {
        // {x, z, hacia donde alarga el brazo (0 norte, 1 este, 2 sur, 3 oeste)}
        int[][] f = {{4, -58, 3}, {-4, -46, 1}, {4, -20, 3}, {-4, -17, 1}, {4, 24, 3}, {-4, 43, 1}, {-80, 9, 0}, {-63, 9, 0}, {-20, 9, 0}, {-10, 2, 2},
                {10, 2, 2}, {49, 3, 2}, {70, 9, 0}, {86, 3, 2}, {-34, -47, 2}, {30, -66, 2}, {-37, 26, 1}, {23, 26, 1}, {47, -24, 3}, {-96, 9, 0}};
        Direction[] dir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int[] k : f) {
            int x = k[0], z = k[1], y = suelo(x, z);
            if (!plan.libre(x, z, 0) || plan.calle(x, z) <= 0.2) continue;
            plan.ocupar(new Rect(x, z, x, z));
            Direction b = dir[k[2]];
            boolean apagada = Hash.pct(x, 0, z, seed + 40) < 22;
            trozo(4, x - 1, y, z - 1, x + 1, y + 5, z + 1, c -> {
                Mat.farola(c, x, y, z, 4, b);
                if (apagada) c.set(x + b.getStepX(), y + 2, z + b.getStepZ(), Pal.AIR);
            });
        }
    }

    private void carros(Rng rng) {
        int[][] k = {{3, -34, 0}, {-58, 9, 1}, {36, 9, 3}, {-3, 30, 2}, {52, -30, 1}};
        Direction[] dir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int[] q : k) {
            if (!plan.libre(q[0], q[1], 2)) continue;
            plan.ocupar(new Rect(q[0] - 2, q[1] - 2, q[0] + 2, q[1] + 2));
            plan.add(new Town.Cart(rng.fork(), q[0], suelo(q[0], q[1]), q[1], dir[q[2]]));
        }
    }

    // ------------------------------------------------------------------ el cementerio

    private BlockState lapida(int x, int z) {
        int p = Hash.pct(x, 3, z, seed + 50);
        if (p < 30) return Pal.b(Blocks.COBBLESTONE_WALL);
        if (p < 55) return Pal.b(Blocks.MOSSY_COBBLESTONE_WALL);
        if (p < 75) return Pal.b(Blocks.STONE_BRICK_WALL);
        if (p < 90) return Pal.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false);
        return Pal.b(Blocks.MOSSY_STONE_BRICK_WALL);
    }

    private void cementerio() {
        Rect r = CEMENTERIO;
        plan.ocupar(r);
        String[] quien = {"pescador", "tejedora", "farero"};
        for (int i = 0; i < 3; i++) plan.sitio("lapida_" + quien[i], FUNDADORES[i][0], suelo(FUNDADORES[i][0], FUNDADORES[i][1]) + 1, FUNDADORES[i][1]);
        plan.sitio("cementerio", r.x1 - 2, suelo(r.x1 - 2, -34), -34);
        trozo(4, r.x0 - 1, Terreno.P - 2, r.z0 - 1, r.x1 + 1, Terreno.P + Terreno.COLINA_H + 6, r.z1 + 1, c -> {
            // la tapia: muro bajo de piedra, caido a trozos, con la cancela al este
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    boolean borde = x == r.x0 || x == r.x1 || z == r.z1;
                    if (!borde) continue;
                    boolean cancela = x == r.x1 && (z == -35 || z == -34);
                    if (cancela) continue;
                    int y = suelo(x, z), p = Hash.pct(x, 1, z, seed + 51);
                    boolean pilar = (x == r.x1 && (z == -36 || z == -33)) || ((x == r.x0 || x == r.x1) && z == r.z1);
                    if (pilar) {
                        c.set(x, y, z, Pal.STONE_BRICKS);
                        c.set(x, y + 1, z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
                        c.set(x, y + 2, z, x == r.x1 && z == -36 ? Pal.SOUL_LANTERN : Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                        continue;
                    }
                    if (p < 14) continue;
                    c.unir(x, y, z, Pal.b(p < 50 ? Blocks.COBBLESTONE_WALL : p < 80 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.STONE_BRICK_WALL));
                }
            // las tumbas de los fundadores, las primeras: la losa y la lapida labrada con su farol
            for (int[] f : FUNDADORES) {
                int x = f[0], z = f[1], y = suelo(x, z);
                c.set(x, y, z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
                c.set(x, y + 1, z, Pal.b(Blocks.STONE_BRICK_WALL));
                c.set(x, y, z + 1, Pal.slab(Blocks.SMOOTH_STONE_SLAB, false));
                c.set(x, y, z + 2, Pal.slab(Blocks.SMOOTH_STONE_SLAB, false));
                c.set(x - 1, y, z, Mat.velas(2));
                c.set(x + 1, y, z, Hash.pct(x, 0, z, seed + 52) < 50 ? Pal.b(Blocks.WITHER_ROSE) : Mat.velas(1));
            }
            // las demas, en hileras
            for (int z = -32; z <= -24; z += 4)
                for (int x = r.x0 + 3 + (z == -28 ? 2 : 0); x <= r.x1 - 3; x += 4) {
                    int p = Hash.pct(x, 2, z, seed + 53);
                    if (p < 16) continue;
                    int y = suelo(x, z);
                    c.set(x, y, z, lapida(x, z));
                    c.set(x, y - 1, z + 1, p < 60 ? Pal.COARSE_DIRT : Pal.PODZOL);
                    c.set(x, suelo(x, z + 2) - 1, z + 2, p < 60 ? Pal.COARSE_DIRT : Pal.PODZOL);
                    if (p > 84) c.set(x, suelo(x, z + 1), z + 1, Pal.b(Blocks.WITHER_ROSE));
                    else if (p > 70) c.set(x, suelo(x, z + 1), z + 1, Pal.b(Blocks.DEAD_BUSH));
                    if (p > 92) c.set(x + 1, y, z, Mat.calavera(p));
                }
        });
    }

    // ------------------------------------------------------------------ los campos

    private void campo(Rect r, int n) {
        plan.ocupar(r);
        trozo(1, r.x0, Terreno.P - 1, r.z0, r.x1, Terreno.P + 5, r.z1, c -> {
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    int y = suelo(x, z);
                    boolean borde = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                    int p = Hash.pct(x, 4, z, seed + 60 + n);
                    if (borde) {
                        boolean puerta = z == r.z1 && Math.abs(x - r.cx()) <= 1;
                        if (!puerta && p >= 18) c.unir(x, y, z, Pal.SP_FENCE);
                        continue;
                    }
                    // surcos resecos: tierra, alguna espiga y matojos
                    boolean surco = (n == 0 ? x : z) % 2 == 0;
                    c.set(x, y - 1, z, surco ? (p < 55 ? Pal.COARSE_DIRT : Pal.b(Blocks.FARMLAND)) : (p < 70 ? Pal.DIRT : Pal.PODZOL));
                    BlockState arriba = Pal.AIR;
                    if (surco) {
                        if (p < 12) arriba = Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7);
                        else if (p < 26) arriba = Pal.b(Blocks.DEAD_BUSH);
                        else if (p < 32) arriba = Pal.b(Blocks.GRASS);
                    }
                    c.set(x, y, z, arriba);
                }
            // el espantapajaros y las pacas
            int ex = r.cx() + (n == 0 ? -3 : 2), ez = r.cz() - 1, ey = suelo(ex, ez);
            c.set(ex, ey - 1, ez, Pal.COARSE_DIRT);
            c.set(ex, ey, ez, Pal.SP_FENCE);
            c.set(ex, ey + 1, ez, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            c.set(ex, ey + 2, ez, Pal.facing(Blocks.CARVED_PUMPKIN, Direction.SOUTH));
            c.unir(ex - 1, ey + 1, ez, Pal.SP_FENCE);
            c.unir(ex + 1, ey + 1, ez, Pal.SP_FENCE);
            int hx = r.x1 - 2, hz = r.z0 + 2, hy = suelo(hx, hz);
            c.set(hx, hy, hz, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
            c.set(hx - 1, hy, hz, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
            c.set(hx, hy + 1, hz, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Z));
            c.set(hx, hy, hz + 1, Pal.b(Blocks.COMPOSTER));
        });
    }

    // ------------------------------------------------------------------ la Cuesta, el Saliente y el mirador

    /** El arco de lo alto de la Cuesta (la puerta del muelle) y el pretil a los lados del tajo. */
    private void arco() {
        int z = Terreno.CUESTA_Z0 - 1, a = Terreno.CUESTA_ANCHO + 1, y = Terreno.P + 1;
        plan.sitio("cuesta", 0, y, z - 2);
        trozo(4, -a - 1, y - 1, z - 1, a + 1, y + 7, Terreno.CUESTA_Z1 + 8, c -> {
            for (int lado = -1; lado <= 1; lado += 2) {
                int x = lado * a;
                for (int yy = y; yy <= y + 4; yy++) c.set(x, yy, z, Mat.sillar(x, yy, z, seed));
                c.set(x - lado, y + 4, z, Pal.stairs(Blocks.STONE_BRICK_STAIRS, lado < 0 ? Direction.WEST : Direction.EAST, true));
                // el pretil a lo largo del tajo, por arriba
                for (int zz = z + 1; zz <= Terreno.CUESTA_Z1 + 6; zz++) {
                    int alto = t.alto(x, zz);
                    if (alto == Terreno.NADA || alto < Terreno.P - 4 || alto <= t.alto(x - lado, zz) + 1) continue;
                    if (Hash.pct(x, 5, zz, seed + 70) < 12) continue;
                    c.unir(x, alto + 1, zz, Pal.b(Hash.pct(x, 6, zz, seed + 70) < 40 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.COBBLESTONE_WALL));
                }
            }
            for (int x = -a; x <= a; x++) c.set(x, y + 5, z, Math.abs(x) == a ? Pal.b(Blocks.CHISELED_STONE_BRICKS) : Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
            Mat.farolColgado(c, 0, y + 4, z, 1);
        });
    }

    /** El Saliente: lo que queda de Tobias, agarrado a la cadena del ancla que cuelga hacia las nubes. */
    private void saliente() {
        int x = Terreno.SALIENTE_X - Terreno.SALIENTE_R + 2, z = Terreno.SALIENTE_Z, y = Terreno.SALIENTE_Y + 1;
        plan.sitio("ancla", x + 1, y, z);
        plan.sitio("saliente", Terreno.SALIENTE_X + 3, y, z);
        trozo(4, x - 4, y - 16, z - 3, x + 4, y + 4, z + 3, c -> {
            // el noray de piedra y la cadena que baja por el borde
            c.set(x, y, z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
            c.set(x, y + 1, z, Pal.b(Blocks.STONE_BRICK_WALL));
            c.set(x - 1, y, z, Pal.CHAIN.setValue(ChainBlock.AXIS, Direction.Axis.X));
            for (int k = 0; k < 14; k++) {
                int cx = x - 2, cy = y - k;
                if (t.alto(cx, z) != Terreno.NADA && cy <= t.alto(cx, z)) cx = x - 3;
                c.set(cx, cy, z, k == 0 ? Pal.CHAIN.setValue(ChainBlock.AXIS, Direction.Axis.X) : Pal.CHAIN);
            }
            // Tobias
            c.set(x + 1, y, z, Mat.calavera(5));
            c.set(x + 1, y, z + 1, Pal.log(Blocks.BONE_BLOCK, Direction.Axis.X));
            c.set(x + 2, y, z + 1, Pal.b(Blocks.GRAY_CARPET));
            c.set(x + 2, y, z - 1, Pal.SOUL_LANTERN);
        });
    }

    /** Al final de la traviesa, al este: un banco y una farola mirando al mar de nubes. */
    private void mirador() {
        int x = 91, z = 6, y = suelo(x, z);
        plan.sitio("mirador", x - 2, y, z);
        trozo(4, x - 1, y, z - 3, x + 3, y + 5, z + 3, c -> {
            for (int dz = -1; dz <= 1; dz++) c.set(x, suelo(x, z + dz), z + dz, Pal.stairs(Blocks.SPRUCE_STAIRS, Direction.EAST, false));
            Mat.farola(c, x, suelo(x, z + 3), z + 3, 4, Direction.NORTH);
        });
    }

    // ------------------------------------------------------------------ lo de la historia dentro de las casas

    private void historia() {
        // el arcon de la vela, al fondo del almacen
        Casa al = plan.almacen;
        int vx = al.lx(al.puertaA(), al.B - 1), vz = al.lz(al.puertaA(), al.B - 1);
        plan.sitio("vela", vx, al.y, vz);
        trozo(5, vx, al.y, vz, vx, al.y + 1, vz, c -> {
            c.set(vx, al.y, vz, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, al.frente));
            c.set(vx, al.y + 1, vz, Pal.AIR);
        });
        // el timon, colgado tras la barra de la taberna
        Casa ta = plan.taberna;
        plan.sitio("timon", ta.lx(ta.A - 2, ta.B - 1), ta.y, ta.lz(ta.A - 2, ta.B - 1));
        plan.sitio("taberna", ta.px + ta.frente.getStepX() * 2, ta.y, ta.pz + ta.frente.getStepZ() * 2);
        plan.sitio("almacen", al.px + al.frente.getStepX() * 2, al.y, al.pz + al.frente.getStepZ() * 2);
        // las notas: el aviso del capitan de puerto, el cuaderno de la tabernera y la carta de un marinero
        Casa ca = plan.capitania;
        plan.sitio("capitania", ca.px + ca.frente.getStepX() * 2, ca.y, ca.pz + ca.frente.getStepZ() * 2);
        nota("nota_capitania", ca, 4, ca.B - 2, 12);
        nota("nota_taberna", ta, 1, 1, 13);
        nota("nota_marinero", plan.casaCarta, 1, 1, 15);
    }

    private void nota(String sitio, Casa k, int a, int b, int libro) {
        int x = k.lx(a, b), z = k.lz(a, b);
        plan.sitio(sitio, x, k.y, z);
        trozo(5, x, k.y, z, x, k.y + 1, z, c -> c.lectern(x, k.y, z, k.frente, Perdidos.libro(libro)));
    }

    // ------------------------------------------------------------------ arboles y rocas

    private void arboles(Rng rng) {
        List<int[]> puestos = new ArrayList<>();
        int quedan = 64, intentos = 0;
        while (quedan > 0 && intentos++ < 4000) {
            double ang = rng.nextDouble() * Math.PI * 2, r = 12 + rng.nextDouble() * 94;
            int x = (int) Math.round(Math.cos(ang) * r), z = (int) Math.round(Math.sin(ang) * r);
            boolean gordo = rng.chance(0.22);
            if (!plan.libre(x, z, gordo ? 4 : 3) || plan.calle(x, z) < 3.2) continue;
            int a = t.alto(x, z);
            if (a < Terreno.P - 1) continue;
            // en llano: ni en la ladera del penon ni al borde de la cala
            if (Math.abs(t.alto(x + 2, z) - a) > 1 || Math.abs(t.alto(x - 2, z) - a) > 1 || Math.abs(t.alto(x, z + 2) - a) > 1 || Math.abs(t.alto(x, z - 2) - a) > 1) continue;
            boolean cerca = false;
            for (int[] p : puestos) if (Math.hypot(p[0] - x, p[1] - z) < 9) cerca = true;
            if (cerca) continue;
            puestos.add(new int[]{x, z});
            plan.add(new Landscape.DeadTree(rng.fork(), x, a + 1, z, gordo));
            quedan--;
        }
    }

    private void rocas(Rng rng) {
        for (int i = 0; i < 46; i++) {
            double ang = rng.nextDouble() * Math.PI * 2, r = 60 + rng.nextDouble() * 48;
            int x = (int) Math.round(Math.cos(ang) * r), z = (int) Math.round(Math.sin(ang) * r);
            if (!plan.libre(x, z, 2) || plan.calle(x, z) < 2.5) continue;
            int a = t.alto(x, z);
            if (a == Terreno.NADA || a < Terreno.P - 3) continue;
            int tam = rng.range(1, 3);
            long s = rng.fork();
            trozo(2, x - 2, a - 1, z - 2, x + 2, a + 4, z + 2, c -> {
                for (int dx = -tam; dx <= tam; dx++)
                    for (int dz = -tam; dz <= tam; dz++) {
                        double d = Math.hypot(dx, dz) / (tam + 0.4) + (Hash.pct(x + dx, 0, z + dz, s) % 30) / 100.0;
                        if (d > 1) continue;
                        int base = t.alto(x + dx, z + dz);
                        if (base == Terreno.NADA) continue;
                        int h = Math.max(1, (int) Math.round((1 - d) * (tam + 0.8)));
                        for (int k = 1; k <= h; k++) {
                            int p = Hash.pct(x + dx, k, z + dz, s + 1);
                            c.set(x + dx, base + k, z + dz, p < 45 ? Pal.STONE : p < 70 ? Pal.ANDESITE : p < 86 ? Pal.b(Blocks.MOSSY_COBBLESTONE) : Pal.TUFF);
                        }
                    }
            });
        }
    }
}
