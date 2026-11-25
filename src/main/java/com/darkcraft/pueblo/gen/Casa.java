package com.darkcraft.pueblo.gen;

import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.Rect;
import com.darkcraft.gen.Rng;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Una casa de Brumavela: zocalo de piedra, entramado de madera con relleno claro (o toda de piedra, o toda de madera),
 * tejado a dos aguas con alero, chimenea y dentro lo justo (cama, mesa, arcon, barril, fogon). Abandonada: ventanas
 * rotas, telaranas y polvo; {@code ruina} 1 = tejado hundido a trozos y una esquina caida; 2 = solo quedan los muros a
 * medias y los escombros. Todo sale de la semilla y de la posicion: cada chunk ve lo mismo.
 * <p>
 * Lo de dentro se coloca en coordenadas "de quien entra": {@code a} de izquierda a derecha y {@code b} hacia el fondo
 * ({@link #lx}, {@link #lz}).
 */
public class Casa extends Element {
    public enum Estilo { ENTRAMADO, PIEDRA, MADERA }

    public enum Tipo { HOGAR, TABERNA, ALMACEN, CAPITANIA, HERRERIA }

    public final Rect fp;
    /** Lo que se pisa en la planta baja. */
    public final int y;
    public final Direction frente;
    public final int plantas, ruina;
    public final Estilo estilo;
    public final Tipo tipo;
    protected final int tejado;
    protected final Terreno terreno;
    /** La cumbrera va a lo largo de X (el tejado cae hacia el norte y el sur). */
    protected final boolean cumbreraX;
    /** Hasta donde llegan los muros, y el lado de dentro (de izquierda a derecha y de fondo). */
    public final int cima, A, B;
    /** La puerta (en el muro) y la esquina que se ha venido abajo (ruina 1). */
    public final int px, pz;
    private final int caidaX, caidaZ;

    public static final ResourceLocation BOTIN = new ResourceLocation("darkcraft", "chests/pueblo");
    public static final ResourceLocation BOTIN_BUENO = new ResourceLocation("darkcraft", "chests/pueblo_bueno");

    public Casa(long seed, Terreno terreno, Rect fp, Direction frente, int plantas, int ruina, Estilo estilo, int tejado, Tipo tipo) {
        super(seed);
        this.layer = 3;
        this.terreno = terreno;
        this.fp = fp;
        this.frente = frente;
        this.plantas = ruina == 2 ? 1 : plantas;
        this.ruina = ruina;
        this.estilo = estilo;
        this.tejado = tejado;
        this.tipo = tipo;
        int alto = Terreno.NADA;
        for (int x = fp.x0; x <= fp.x1; x++)
            for (int z = fp.z0; z <= fp.z1; z++) alto = Math.max(alto, terreno.alto(x, z));
        this.y = alto + 1;
        this.cima = y + (this.plantas == 1 ? 2 : 6) + (tipo == Tipo.ALMACEN ? 2 : 0);
        this.cumbreraX = fp.w() >= fp.d();
        boolean ns = frente.getAxis() == Direction.Axis.Z;
        this.A = (ns ? fp.w() : fp.d()) - 2;
        this.B = (ns ? fp.d() : fp.w()) - 2;
        this.px = ns ? fp.cx() : (frente == Direction.WEST ? fp.x0 : fp.x1);
        this.pz = ns ? (frente == Direction.NORTH ? fp.z0 : fp.z1) : fp.cz();
        Rng r = new Rng(seed ^ 0x51);
        // la esquina caida: una de las del fondo
        Direction dentro = frente.getOpposite(), der = dentro.getClockWise();
        boolean izq = r.chance(0.5);
        this.caidaX = lx(izq ? -1 : A, B);
        this.caidaZ = lz(izq ? -1 : A, B);
        int ancho = cumbreraX ? fp.d() : fp.w();
        bounds(fp.x0 - 2, y - 8, fp.z0 - 2, fp.x1 + 2, cima + ancho / 2 + 6, fp.z1 + 2);
    }

    // ------------------------------------------------------------------ coordenadas de quien entra

    /** x del mundo del sitio (a, b) de dentro: a de izquierda a derecha, b hacia el fondo (0 = junto a la puerta). */
    public int lx(int a, int b) {
        return switch (frente) {
            case SOUTH -> fp.x0 + 1 + a;
            case NORTH -> fp.x1 - 1 - a;
            case EAST -> fp.x1 - 1 - b;
            default -> fp.x0 + 1 + b;
        };
    }

    public int lz(int a, int b) {
        return switch (frente) {
            case SOUTH -> fp.z1 - 1 - b;
            case NORTH -> fp.z0 + 1 + b;
            case EAST -> fp.z1 - 1 - a;
            default -> fp.z0 + 1 + a;
        };
    }

    /** Hacia dentro (por donde se entra), a la derecha y a la izquierda de quien entra. */
    protected Direction dentro() {
        return frente.getOpposite();
    }

    protected Direction derecha() {
        return frente.getOpposite().getClockWise();
    }

    protected Direction izquierda() {
        return frente.getOpposite().getCounterClockWise();
    }

    /** La columna de la puerta, por dentro. */
    public int puertaA() {
        return A / 2;
    }

    // ------------------------------------------------------------------ materiales

    protected BlockState zocalo(int x, int yy, int z) {
        int p = Hash.pct(x, yy, z, seed + 1);
        if (p < 40) return Pal.b(Blocks.COBBLESTONE);
        if (p < 62) return Pal.b(Blocks.MOSSY_COBBLESTONE);
        if (p < 82) return Pal.STONE_BRICKS;
        return Pal.b(Blocks.CRACKED_STONE_BRICKS);
    }

    protected BlockState relleno(int x, int yy, int z) {
        int p = Hash.pct(x, yy, z, seed + 2);
        switch (estilo) {
            case PIEDRA:
                if (p < 38) return Pal.b(Blocks.COBBLESTONE);
                if (p < 56) return Pal.STONE_BRICKS;
                if (p < 72) return Pal.b(Blocks.MOSSY_COBBLESTONE);
                if (p < 86) return Pal.ANDESITE;
                return Pal.b(Blocks.CRACKED_STONE_BRICKS);
            case MADERA:
                return p < 70 ? Pal.SP_PLANKS : p < 90 ? Pal.DO_PLANKS : Pal.b(Blocks.STRIPPED_SPRUCE_WOOD);
            default:
                if (p < 62) return Pal.b(Blocks.WHITE_TERRACOTTA);
                if (p < 84) return Pal.b(Blocks.LIGHT_GRAY_TERRACOTTA);
                if (p < 93) return Pal.b(Blocks.CALCITE);
                return Pal.SP_PLANKS;
        }
    }

    protected BlockState poste() {
        return estilo == Estilo.MADERA ? Pal.SP_LOG : Pal.DO_LOG;
    }

    protected BlockState viga(Direction.Axis eje) {
        return Pal.log(estilo == Estilo.MADERA ? Blocks.STRIPPED_SPRUCE_LOG : Blocks.STRIPPED_DARK_OAK_LOG, eje);
    }

    protected BlockState suelo(int x, int z) {
        int p = Hash.pct(x, y, z, seed + 4);
        if (ruina >= 1 && p < 9 + ruina * 10) return p % 2 == 0 ? Pal.COARSE_DIRT : Pal.MOSS;
        return p < 78 ? Pal.SP_PLANKS : Pal.DO_PLANKS;
    }

    protected Block escalonTejado() {
        return switch (tejado) {
            case 1 -> Blocks.DARK_OAK_STAIRS;
            case 2 -> Blocks.DEEPSLATE_TILE_STAIRS;
            default -> Blocks.SPRUCE_STAIRS;
        };
    }

    protected BlockState cumbrera() {
        return switch (tejado) {
            case 1 -> Pal.slab(Blocks.DARK_OAK_SLAB, false);
            case 2 -> Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false);
            default -> Pal.slab(Blocks.SPRUCE_SLAB, false);
        };
    }

    // ------------------------------------------------------------------ lo que queda en pie

    /** Hasta que altura queda el muro en esa columna (la casa entera: {@link #cima}). */
    protected int quedaMuro(int x, int z) {
        if (ruina == 0) return cima;
        if (ruina == 1) {
            double d = Math.hypot(x - caidaX, z - caidaZ);
            if (d > 4.6) return cima;
            return y - 1 + (int) Math.round(d * 0.9 + Hash.pct(x, 0, z, seed + 8) % 3 * 0.6);
        }
        // ruina total: un perfil a mordiscos, mas alto en las esquinas
        int h = 1 + Hash.pct(x >> 1, 1, z >> 1, seed + 9) % 3;
        boolean esquina = (x == fp.x0 || x == fp.x1) && (z == fp.z0 || z == fp.z1);
        if (esquina) h += 1;
        if (Math.hypot(x - caidaX, z - caidaZ) < 4) h = Hash.pct(x, 2, z, seed + 9) % 2;
        return Math.min(cima, y - 1 + h);
    }

    /** Si ese trozo del tejado se ha hundido (ruina 1): dos boquetes. */
    protected boolean hundido(int x, int z) {
        if (ruina != 1) return false;
        if (Math.hypot(x - caidaX, z - caidaZ) < 5.4) return true;
        int hx = fp.x0 + 2 + Hash.pct(3, 3, 3, seed + 10) % Math.max(1, fp.w() - 4), hz = fp.z0 + 2 + Hash.pct(4, 4, 4, seed + 10) % Math.max(1, fp.d() - 4);
        return Math.hypot(x - hx, z - hz) < 2.3 + Hash.pct(x, 5, z, seed + 10) % 2 * 0.7;
    }

    // ------------------------------------------------------------------ construir

    @Override
    public void build(Canvas c) {
        cimientos(c);
        muros(c);
        if (plantas == 2) entreplanta(c);
        if (ruina < 2) tejado(c);
        if (ruina < 2 && tipo != Tipo.ALMACEN && fp.w() >= 7 && fp.d() >= 7) chimenea(c);
        puerta(c);
        amueblar(c);
    }

    private void cimientos(Canvas c) {
        for (int x = fp.x0; x <= fp.x1; x++)
            for (int z = fp.z0; z <= fp.z1; z++) {
                int t = terreno.alto(x, z);
                boolean borde = x == fp.x0 || x == fp.x1 || z == fp.z0 || z == fp.z1;
                int desde = t == Terreno.NADA ? y - 4 : Math.min(t + 1, y - 1);
                for (int yy = desde; yy <= y - 2; yy++) c.set(x, yy, z, zocalo(x, yy, z));
                c.set(x, y - 1, z, borde ? zocalo(x, y - 1, z) : suelo(x, z));
                // dentro, aire hasta arriba (el tejado lo cierra)
                if (!borde) c.column(x, z, y, cima + Math.max(fp.w(), fp.d()) / 2 + 2, Pal.AIR);
            }
    }

    private boolean esPoste(int x, int z) {
        boolean ex = x == fp.x0 || x == fp.x1, ez = z == fp.z0 || z == fp.z1;
        if (ex && ez) return true;
        if (estilo == Estilo.PIEDRA) return false;
        // postes cada cuatro a lo largo de cada muro
        if (ez) return (x - fp.x0) % 4 == 0 && x < fp.x1 - 1;
        return (z - fp.z0) % 4 == 0 && z < fp.z1 - 1;
    }

    /** Si en esa celda del muro va una ventana (a la altura de los ojos de cada planta). */
    protected boolean ventana(int x, int z) {
        boolean ex = x == fp.x0 || x == fp.x1, ez = z == fp.z0 || z == fp.z1;
        if (ex && ez) return false;
        if (x == px && z == pz) return false;
        if (Math.abs(x - px) + Math.abs(z - pz) == 1) return false;
        int k = ez ? x - fp.x0 : z - fp.z0, n = ez ? fp.w() : fp.d();
        if (k < 2 || k > n - 3) return false;
        return k % 4 == 2;
    }

    private void muros(Canvas c) {
        for (int x = fp.x0; x <= fp.x1; x++)
            for (int z = fp.z0; z <= fp.z1; z++) {
                boolean ex = x == fp.x0 || x == fp.x1, ez = z == fp.z0 || z == fp.z1;
                if (!ex && !ez) continue;
                int queda = quedaMuro(x, z);
                boolean poste = esPoste(x, z);
                Direction.Axis eje = ez ? Direction.Axis.X : Direction.Axis.Z;
                for (int yy = y; yy <= cima; yy++) {
                    if (yy > queda) {
                        c.set(x, yy, z, Pal.AIR);
                        continue;
                    }
                    BlockState s;
                    if (poste) s = poste();
                    else if (yy == y) s = zocalo(x, yy, z);
                    else if (plantas == 2 && yy == y + 3 && estilo != Estilo.PIEDRA) s = viga(eje);
                    else if (yy == cima && estilo == Estilo.ENTRAMADO) s = viga(eje);
                    else s = relleno(x, yy, z);
                    boolean ojo = yy == y + 1 || (plantas == 2 && yy == y + 5) || (tipo == Tipo.ALMACEN && yy == y + 3);
                    if (ojo && ventana(x, z) && !poste) {
                        int p = Hash.pct(x, yy, z, seed + 6);
                        // rotas casi todas: hueco, algun cristal y barrotes en el almacen
                        s = tipo == Tipo.ALMACEN ? Pal.connected(Blocks.IRON_BARS, eje)
                                : p < 34 + ruina * 20 ? Pal.AIR : Pal.connected(Blocks.GLASS_PANE, eje);
                    }
                    c.set(x, yy, z, s);
                }
                // musgo y enredaderas al pie
                if (queda < cima && queda >= y && Hash.pct(x, 7, z, seed + 7) < 40) c.set(x, queda, z, Pal.b(Blocks.MOSSY_COBBLESTONE));
            }
    }

    private void entreplanta(Canvas c) {
        int ea = A - 1, eb = 0; // el hueco de la escalera de mano: a la derecha, junto a la puerta
        for (int a = 0; a < A; a++)
            for (int b = 0; b < B; b++) {
                int x = lx(a, b), z = lz(a, b);
                if (a == ea && b == eb) continue;
                if (ruina == 1 && Math.hypot(x - caidaX, z - caidaZ) < 4.2) continue;
                c.set(x, y + 3, z, Hash.pct(x, y + 3, z, seed + 4) < 75 ? Pal.SP_PLANKS : Pal.DO_PLANKS);
            }
        // la escalera de mano, pegada al muro de la derecha
        Direction mira = izquierda();
        for (int yy = y; yy <= y + 3; yy++)
            c.set(lx(ea, eb), yy, lz(ea, eb), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, mira));
    }

    private void tejado(Canvas c) {
        Block esc = escalonTejado();
        int a0 = cumbreraX ? fp.z0 : fp.x0, a1 = cumbreraX ? fp.z1 : fp.x1;   // a lo ancho
        int l0 = (cumbreraX ? fp.x0 : fp.z0) - 1, l1 = (cumbreraX ? fp.x1 : fp.z1) + 1; // a lo largo, con alero
        Direction sube0 = cumbreraX ? Direction.SOUTH : Direction.EAST, sube1 = sube0.getOpposite();
        for (int k = 0; ; k++) {
            int lo = a0 - 1 + k, hi = a1 + 1 - k, yy = cima + k;
            if (lo > hi) break;
            for (int l = l0; l <= l1; l++) {
                for (int lado = 0; lado < 2; lado++) {
                    int a = lado == 0 ? lo : hi;
                    if (lado == 1 && lo == hi) break;
                    int x = cumbreraX ? l : a, z = cumbreraX ? a : l;
                    if (hundido(x, z)) {
                        // alguna viga rota asomando
                        if (Hash.pct(x, yy, z, seed + 11) < 9) c.set(x, yy, z, viga(cumbreraX ? Direction.Axis.Z : Direction.Axis.X));
                        continue;
                    }
                    if (lo == hi) c.set(x, yy, z, cumbrera());
                    else c.set(x, yy, z, Pal.stairs(esc, lado == 0 ? sube0 : sube1, false));
                }
            }
            // los hastiales: el muro sube hasta el tejado
            for (int a = lo + 1; a <= hi - 1; a++)
                for (int lado = 0; lado < 2; lado++) {
                    int l = lado == 0 ? l0 + 1 : l1 - 1;
                    int x = cumbreraX ? l : a, z = cumbreraX ? a : l;
                    if (yy <= cima) continue;
                    if (ruina == 1 && (hundido(x, z) || quedaMuro(x, z) < cima)) continue;
                    c.set(x, yy, z, estilo == Estilo.PIEDRA ? relleno(x, yy, z) : Hash.pct(x, yy, z, seed + 2) < 80 ? Pal.SP_PLANKS : Pal.DO_PLANKS);
                }
        }
    }

    private void chimenea(Canvas c) {
        // por fuera del muro de la izquierda de quien entra, a medio fondo
        int a = -1, b = Math.max(1, B / 2);
        int x = lx(a, b), z = lz(a, b);
        Direction fuera = izquierda();
        int cx = x + fuera.getStepX(), cz = z + fuera.getStepZ();
        // lo justo por encima del tejado en ese sitio (el alero si da a un lado del tejado, la pendiente si da a un hastial)
        int a0 = cumbreraX ? fp.z0 : fp.x0, a1 = cumbreraX ? fp.z1 : fp.x1, at = cumbreraX ? cz : cx;
        int alto = at < a0 || at > a1 ? cima + 2 : cima + Math.min(at - (a0 - 1), (a1 + 1) - at) + 2;
        if (ruina == 1 && Math.hypot(x - caidaX, z - caidaZ) < 6) alto = y + 2;
        for (int yy = terreno.alto(cx, cz) == Terreno.NADA ? y - 1 : Math.min(y - 1, terreno.alto(cx, cz) + 1); yy <= alto; yy++) {
            int p = Hash.pct(cx, yy, cz, seed + 12);
            c.set(cx, yy, cz, p < 50 ? Pal.b(Blocks.COBBLESTONE) : p < 75 ? Pal.STONE_BRICKS : Pal.b(Blocks.MOSSY_COBBLESTONE));
        }
        if (alto > y + 2) c.set(cx, alto + 1, cz, Pal.b(Blocks.COBBLESTONE_WALL));
        // el hogar, por dentro
        c.set(x, y, z, Pal.b(Blocks.COBBLESTONE));
        c.set(x, y + 1, z, Pal.b(Blocks.COBBLESTONE));
        int hx = lx(0, b), hz = lz(0, b);
        c.set(hx, y - 1, hz, Pal.b(Blocks.BRICKS));
        c.set(hx, y, hz, Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
    }

    private void puerta(Canvas c) {
        if (quedaMuro(px, pz) < y + 2 && ruina == 2) {
            c.set(px, y, pz, Pal.AIR);
            c.set(px, y + 1, pz, Pal.AIR);
            return;
        }
        boolean hoja = ruina == 0 ? Hash.pct(px, y, pz, seed + 13) < 70 : Hash.pct(px, y, pz, seed + 13) < 30;
        c.set(px, y - 1, pz, zocalo(px, y - 1, pz));
        if (tipo == Tipo.ALMACEN) {
            // el porton: tres de ancho y tres de alto, abierto
            Direction lado = derecha();
            for (int k = -1; k <= 1; k++)
                for (int yy = y; yy <= y + 2; yy++) c.set(px + lado.getStepX() * k, yy, pz + lado.getStepZ() * k, Pal.AIR);
            return;
        }
        if (hoja) {
            BlockState d = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, dentro());
            c.set(px, y, pz, d.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            c.set(px, y + 1, pz, d.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        } else {
            c.set(px, y, pz, Pal.AIR);
            c.set(px, y + 1, pz, Pal.AIR);
        }
        c.set(px, y + 2, pz, viga(frente.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z));
        // el escalon de fuera si el suelo queda mas bajo
        int fx = px + frente.getStepX(), fz = pz + frente.getStepZ();
        int t = terreno.alto(fx, fz);
        if (t != Terreno.NADA && t < y - 1) {
            for (int yy = t + 1; yy <= y - 2; yy++) c.set(fx, yy, fz, zocalo(fx, yy, fz));
            c.set(fx, y - 1, fz, Pal.stairs(Blocks.COBBLESTONE_STAIRS, dentro(), false));
        }
    }

    // ------------------------------------------------------------------ por dentro

    protected void pon(Canvas c, int a, int b, int dy, BlockState s) {
        c.set(lx(a, b), y + dy, lz(a, b), s);
    }

    protected void cama(Canvas c, int a, int b, int dy, Block cama) {
        // la cabecera en (a, b), los pies a la derecha
        Direction f = izquierda();
        c.set(lx(a, b), y + dy, lz(a, b), cama.defaultBlockState().setValue(BedBlock.FACING, f).setValue(BedBlock.PART, BedPart.HEAD));
        c.set(lx(a + 1, b), y + dy, lz(a + 1, b), cama.defaultBlockState().setValue(BedBlock.FACING, f).setValue(BedBlock.PART, BedPart.FOOT));
    }

    protected void arcon(Canvas c, int a, int b, int dy, ResourceLocation botin) {
        c.chest(lx(a, b), y + dy, lz(a, b), frente, botin);
    }

    protected void mesa(Canvas c, int a, int b) {
        pon(c, a, b, 0, estilo == Estilo.MADERA ? Pal.SP_FENCE : Pal.DO_FENCE);
        pon(c, a, b, 1, Pal.b(Blocks.SPRUCE_PRESSURE_PLATE));
    }

    protected void silla(Canvas c, int a, int b, Direction respaldo) {
        pon(c, a, b, 0, Pal.stairs(Blocks.SPRUCE_STAIRS, respaldo, false));
    }

    protected void escombros(Canvas c, Rng r, int n) {
        for (int k = 0; k < n; k++) {
            int a = r.nextInt(Math.max(1, A)), b = 1 + r.nextInt(Math.max(1, B - 1));
            if (a == puertaA() && b <= 1) continue;
            int p = r.nextInt(6);
            BlockState s = switch (p) {
                case 0 -> Pal.b(Blocks.COBBLESTONE);
                case 1 -> Pal.slab(Blocks.COBBLESTONE_SLAB, false);
                case 2 -> Pal.b(Blocks.MOSSY_COBBLESTONE);
                case 3 -> Pal.slab(Blocks.SPRUCE_SLAB, false);
                case 4 -> Pal.b(Blocks.DEAD_BUSH);
                default -> Pal.slab(Blocks.MOSSY_COBBLESTONE_SLAB, false);
            };
            if (p == 4) pon(c, a, b, -1, Pal.COARSE_DIRT);
            pon(c, a, b, 0, s);
        }
    }

    protected void telaranas(Canvas c, Rng r, int n) {
        int techo = plantas == 1 ? 2 : 2;
        int[][] esquinas = {{0, 0}, {A - 1, 0}, {0, B - 1}, {A - 1, B - 1}};
        for (int k = 0; k < n; k++) {
            int[] e = esquinas[r.nextInt(4)];
            if (plantas == 2 && e[0] == A - 1 && e[1] == 0) continue; // ahi va la escalera de mano
            pon(c, e[0], e[1], techo, Pal.COBWEB);
        }
    }

    protected void amueblar(Canvas c) {
        Rng r = new Rng(seed ^ 0xA11);
        if (A < 3 || B < 3) return;
        if (ruina == 2) {
            escombros(c, r, 5 + (A * B) / 8);
            return;
        }
        switch (tipo) {
            case TABERNA -> taberna(c, r);
            case ALMACEN -> almacen(c, r);
            case CAPITANIA -> capitania(c, r);
            case HERRERIA -> herreria(c, r);
            default -> hogar(c, r);
        }
        telaranas(c, r, 2 + ruina * 2);
        if (ruina == 1) escombros(c, r, 3 + (A * B) / 12);
    }

    private void hogar(Canvas c, Rng r) {
        boolean arriba = plantas == 2;
        // al fondo: la cama (abajo si no hay planta alta) y el arcon
        if (!arriba) cama(c, 0, B - 1, 0, r.chance(0.5) ? Blocks.RED_BED : Blocks.BROWN_BED);
        else pon(c, 0, B - 1, 0, Pal.BOOKSHELF);
        arcon(c, A - 1, B - 1, 0, BOTIN);
        // la mesa en medio con dos sillas
        int ma = Math.max(2, A / 2), mb = Math.max(1, B / 2);
        if (ma == puertaA() && mb <= 1) mb = 2;
        mesa(c, ma, mb);
        if (ma - 1 >= 1) silla(c, ma - 1, mb, izquierda());
        if (ma + 1 < A - 1) silla(c, ma + 1, mb, derecha());
        // junto a la puerta: barril y banco de trabajo
        pon(c, 0, 0, 0, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP));
        if (!arriba && A > 4) pon(c, A - 1, 0, 0, Pal.b(r.chance(0.5) ? Blocks.CRAFTING_TABLE : Blocks.LOOM));
        // el fogon o el caldero a la derecha
        if (B > 4) pon(c, A - 1, B / 2, 0, r.chance(0.5) ? Pal.facing(Blocks.SMOKER, izquierda()) : Pal.b(Blocks.CAULDRON));
        if (arriba) {
            cama(c, 0, B - 1, 4, r.chance(0.5) ? Blocks.RED_BED : Blocks.GRAY_BED);
            if (A > 4) arcon(c, A - 1, B - 1, 4, BOTIN);
            pon(c, 0, 0, 4, Pal.BOOKSHELF);
            pon(c, 0, 0, 6, Pal.COBWEB);
        }
    }

    // la taberna: la barra al fondo a la derecha, mesas, barriles y el cuarto de atras (lo pone el plano: ahi va el timon)
    private void taberna(Canvas c, Rng r) {
        // la barra: a lo largo del muro derecho
        for (int b = 2; b <= Math.min(B - 3, 8); b++) {
            pon(c, A - 3, b, 0, b == 2 || b == Math.min(B - 3, 8) ? Pal.SP_PLANKS : Pal.slab(Blocks.SPRUCE_SLAB, true));
            if (b % 2 == 0) pon(c, A - 1, b, 0, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, izquierda()));
            if (b % 2 == 0) pon(c, A - 1, b, 1, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, izquierda()));
        }
        pon(c, A - 3, 4, 1, Pal.b(Blocks.FLOWER_POT));
        // mesas con sus taburetes
        int[][] mesas = {{2, 3}, {2, 7}, {5, 5}, {2, 11}, {6, 10}};
        for (int[] m : mesas) {
            if (m[0] >= A - 4 || m[1] >= B - 1) continue;
            mesa(c, m[0], m[1]);
            silla(c, m[0] - 1, m[1], izquierda());
            if (r.chance(0.7)) silla(c, m[0] + 1, m[1], derecha());
            if (r.chance(0.4)) pon(c, m[0], m[1] + 1, 0, Pal.stairs(Blocks.SPRUCE_STAIRS, dentro(), false));
        }
        arcon(c, 0, B - 1, 0, BOTIN_BUENO);
        if (plantas == 2) {
            // arriba, los cuartos: camas a lo largo del muro izquierdo
            for (int b = 1; b < B - 1; b += 4) cama(c, 0, b, 4, Blocks.RED_BED);
            arcon(c, A - 1, B - 1, 4, BOTIN);
        }
    }

    // el almacen: estanterias de barriles y cajas, y el arcon de la vela al fondo (lo pone el plano)
    private void almacen(Canvas c, Rng r) {
        for (int a = 0; a < A; a++)
            for (int b = 0; b < B; b++) {
                boolean pasillo = Math.abs(a - puertaA()) <= 1 || b == B / 2;
                if (pasillo) continue;
                int p = Hash.pct(lx(a, b), y, lz(a, b), seed + 20);
                if (p < 38) {
                    int alto = 1 + p % 3;
                    for (int k = 0; k < alto; k++)
                        pon(c, a, b, k, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING,
                                k == alto - 1 ? Direction.UP : frente));
                } else if (p < 52) pon(c, a, b, 0, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
                else if (p < 60) pon(c, a, b, 0, Pal.b(Blocks.COMPOSTER));
            }
        // la cabria: una viga con cadena y farol en medio
        for (int a = puertaA() - 1; a <= puertaA() + 1; a++) pon(c, a, B / 2, cima - y, viga(frente.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z));
        pon(c, puertaA(), B / 2, cima - y - 1, Pal.CHAIN);
        pon(c, puertaA(), B / 2, cima - y - 2, Pal.SOUL_LANTERN_HANG);
    }

    // la capitania: el escritorio con los mapas, estantes y la cama del capitan de puerto arriba
    private void capitania(Canvas c, Rng r) {
        pon(c, A / 2, B - 2, 0, Pal.b(Blocks.CARTOGRAPHY_TABLE));
        pon(c, A / 2 + 1, B - 2, 0, Pal.DO_FENCE);
        pon(c, A / 2 + 1, B - 2, 1, Pal.b(Blocks.SPRUCE_PRESSURE_PLATE));
        silla(c, A / 2, B - 1, dentro());
        for (int a = 0; a < A; a++)
            if (a != A / 2 && a != A / 2 + 1) {
                pon(c, a, B - 1, 0, Pal.BOOKSHELF);
                if (a % 2 == 0) pon(c, a, B - 1, 1, Pal.BOOKSHELF);
            }
        pon(c, 0, 0, 0, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP));
        pon(c, 0, 2, 0, Pal.b(Blocks.LOOM));
        arcon(c, 0, B / 2, 0, BOTIN_BUENO);
        if (plantas == 2) {
            cama(c, 0, B - 1, 4, Blocks.BLUE_BED);
            arcon(c, A - 1, B - 1, 4, BOTIN);
            pon(c, A / 2, B - 1, 4, Pal.BOOKSHELF);
        }
    }

    // la herreria: el yunque, la fragua y el barreno; medio abierta por delante
    private void herreria(Canvas c, Rng r) {
        pon(c, 1, B - 2, 0, Pal.b(Blocks.ANVIL));
        pon(c, A - 1, B - 1, 0, Pal.facing(Blocks.BLAST_FURNACE, frente));
        pon(c, A - 2, B - 1, 0, Pal.facing(Blocks.FURNACE, frente));
        pon(c, A - 1, B - 2, 0, Pal.b(Blocks.CAULDRON));
        pon(c, 0, 0, 0, Pal.b(Blocks.SMITHING_TABLE));
        pon(c, A - 1, 1, 0, Pal.b(Blocks.GRINDSTONE));
        pon(c, A / 2, B - 1, 0, Pal.b(Blocks.COAL_BLOCK));
        arcon(c, 0, B - 1, 0, BOTIN_BUENO);
    }
}
