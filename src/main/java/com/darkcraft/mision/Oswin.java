package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Arboleda;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.morvain.SantuariosMorvain;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Viajero;
import com.darkcraft.rol.Equipo;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Rol;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * La campana del peregrino (parte 7ag; respuestas 95 y 117 del plan): la mision de <b>Oswin</b>, el peregrino ciego que espera
 * junto a la Encrucijada de los Peregrinos (al oeste del fuego, apoyado en su bordon junto a un mojon con un farol).
 * <ol>
 * <li>Perdio su campana y sin ella "los muertos no le dejan pasar" (frase 1). Se le responde: <b>buscarla</b> (2: la mision
 * empieza; la tienen los ahorcados del Bosque Muerto, cuelga del arbol mas viejo, donde se inclina sobre el camino),
 * <b>que se gana</b> (3: el rosario de su orden; y otra vez las respuestas) o <b>no</b> (4). Quien le dejo con la palabra en la
 * boca o le dijo que no, la vez siguiente oye la 6 y puede cambiar de idea.</li>
 * <li>La campana ({@link Campana}) cuelga de la punta de la rama este del Arbol de la Horca, en la Arboleda de los Ahorcados
 * ({@code Arboleda#campana}), entre dos ahorcados; suena con el viento. Se coge con clic derecho o la E ({@link #tomar}); cada
 * jugador la suya. Tambien se puede coger sin conocer a Oswin.</li>
 * <li>Con la campana (frase 7) se le puede <b>devolver</b> (8: la alza y la toca, los peregrinos muertos echan a andar hacia el
 * norte —{@link #procesion}—, da el Rosario del peregrino —{@code Talisman.Clase.PEREGRINO}: los frascos de vida curan un 25 %
 * mas— y, a quien no pregunto que ganaba, la 9 y las limosnas de su orden: {@link #LIMOSNAS} ecos; logro
 * {@code campana_peregrino}) o <b>quedarsela</b> (10: la campana pasa a ser la Campana de los difuntos, que aparta a los muertos
 * —{@link CampanaDifuntos}—; logro {@code campana_difuntos}). Despues, la 12 o la 11.</li>
 * </ol>
 * Lo de cada jugador, en su ficha ({@code Ficha.mision}, marcas de bits); Oswin y la campana se ponen una vez, la primera vez que
 * alguien pasa cerca ({@code Morvain.Datos} "oswin" y "campana").
 */
public final class Oswin {
    private Oswin() {}

    public static final String ID = "oswin";
    /** Las marcas de la mision ({@code Ficha.mision}). */
    public static final int CONOCIDA = 1, ACEPTADA = 2, RECHAZADA = 4, PRECIO = 8, TOMADA = 16, DEVUELTA = 32, QUEDADA = 64;
    /** No se guarda: la pone el servidor al mandar las misiones (lleva la campana ahora). */
    public static final int LLEVA = 1 << 20;
    /** Las limosnas de su orden (a quien la devuelve sin haber preguntado que ganaba). */
    public static final int LIMOSNAS = 3000;
    /** Donde esta: junto a la Encrucijada de los Peregrinos, a {@link #DX}, {@link #DZ} de su fuego (al oeste, de cara a el). */
    public static final String SANTUARIO = "encrucijada";
    static final int DX = -6, DZ = -3;
    /** A esta distancia de su sitio se pone Oswin (y su campana, a {@link #CERCA_CAMPANA} de la suya). */
    static final int CERCA = 56, CERCA_CAMPANA = 48;

    // ------------------------------------------------------------------ donde esta todo

    /** Donde se pone Oswin (la y, la del sitio del santuario: la de verdad sale del suelo al ponerlo). */
    public static BlockPos sitio() {
        SantuariosMorvain.Sitio s = SantuariosMorvain.porId(SANTUARIO);
        return new BlockPos(s.x() + DX, s.y() + 1, s.z() + DZ);
    }

    /** El fuego de su santuario (hacia donde mira). */
    public static BlockPos fuego() {
        return SantuariosMorvain.porId(SANTUARIO).pos();
    }

    /** El cabo de la soga de la campana ({x, y, z}; la campana cuelga debajo) o null. */
    public static int[] cabo() {
        return Arboleda.get().campana();
    }

    // ------------------------------------------------------------------ cada segundo: ponerlos

    static void tick(ServerLevel m) {
        if (!Morvain.hecho(m.getServer(), "oswin")) {
            BlockPos p = sitio();
            // (si ya estaba —se olvido que se puso con su chunk sin cargar—, solo se apunta)
            if (m.isPositionEntityTicking(p) && alguien(m, p, CERCA)) {
                if (buscarOswin(m) != null) Morvain.marcarHecho(m.getServer(), "oswin");
                else ponerOswin(m);
            }
        }
        if (!Morvain.hecho(m.getServer(), "campana")) {
            int[] c = cabo();
            if (c != null) {
                BlockPos q = new BlockPos(c[0], c[1], c[2]);
                if (m.isPositionEntityTicking(q) && alguien(m, q, CERCA_CAMPANA)) {
                    if (buscarCampana(m) != null) Morvain.marcarHecho(m.getServer(), "campana");
                    else ponerCampana(m);
                }
            }
        }
    }

    private static boolean alguien(ServerLevel m, BlockPos p, int r) {
        for (ServerPlayer sp : m.players()) if (!sp.isSpectator() && sp.blockPosition().distSqr(p) < (double) r * r) return true;
        return false;
    }

    /** Pone a Oswin (de pie en el suelo de su sitio, mirando al fuego) y su mojon con el farol detras. */
    public static Viajero ponerOswin(ServerLevel m) {
        BlockPos s = sitio();
        m.getChunk(s.getX() >> 4, s.getZ() >> 4);
        int y = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX(), s.getZ());
        BlockPos pie = new BlockPos(s.getX(), y, s.getZ());
        // las matas de alrededor fuera (que no le tapen los pies)
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos q = pie.offset(dx, dy, dz);
                    BlockState b = m.getBlockState(q);
                    if (!b.isAir() && b.canBeReplaced()) m.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
                }
        // el mojon del camino de los peregrinos, a su espalda: piedra con musgo, un murete y el farol encima
        BlockPos mojon = pie.west();
        int ym = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mojon.getX(), mojon.getZ());
        mojon = new BlockPos(mojon.getX(), Math.min(ym, pie.getY()), mojon.getZ());
        m.setBlock(mojon, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
        m.setBlock(mojon.above(), Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState(), 3);
        m.setBlock(mojon.above(2), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false), 3);
        Viajero v = Viajero.poner(m, pie, fuego(), Viajero.Quien.OSWIN);
        if (v == null) return null;
        Morvain.marcarHecho(m.getServer(), "oswin");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Oswin, el peregrino ciego, junto a la Encrucijada ({})", pie.toShortString());
        return v;
    }

    /** Cuelga la campana del cabo de su soga (si la soga no estuviera —un chunk de antes de la 7ag—, la pone hasta la rama). */
    public static Campana ponerCampana(ServerLevel m) {
        int[] c = cabo();
        if (c == null) return null;
        BlockPos cabo = new BlockPos(c[0], c[1], c[2]);
        m.getChunk(cabo.getX() >> 4, cabo.getZ() >> 4);
        if (!(m.getBlockState(cabo).getBlock() instanceof com.darkcraft.morvain.SogaBlock)) {
            BlockPos q = cabo;
            for (int i = 0; i < 16 && m.getBlockState(q).isAir(); i++, q = q.above())
                m.setBlock(q, DarkCraftMod.SOGA.get().defaultBlockState(), 2);
        }
        Campana e = DarkCraftMod.CAMPANA_ARBOL.get().create(m);
        if (e == null) return null;
        e.moveTo(cabo.getX() + 0.5, cabo.getY() - Campana.ALTO, cabo.getZ() + 0.5, 0f, 0f);
        e.addTag("darkcraft_campana");
        m.addFreshEntity(e);
        Morvain.marcarHecho(m.getServer(), "campana");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: la campana del peregrino cuelga en la Arboleda ({})", cabo.toShortString());
        return e;
    }

    /** Oswin (si su chunk esta cargado). */
    public static Viajero buscarOswin(ServerLevel m) {
        BlockPos s = sitio();
        if (!m.isLoaded(s)) return null;
        for (Viajero v : m.getEntitiesOfClass(Viajero.class, new AABB(s).inflate(6, 24, 6)))
            if (v.quien() == Viajero.Quien.OSWIN) return v;
        return null;
    }

    /** La campana (si su chunk esta cargado). */
    public static Campana buscarCampana(ServerLevel m) {
        int[] c = cabo();
        if (c == null) return null;
        BlockPos q = new BlockPos(c[0], c[1], c[2]);
        if (!m.isLoaded(q)) return null;
        for (Campana e : m.getEntitiesOfClass(Campana.class, new AABB(q).inflate(3))) return e;
        return null;
    }

    /** Para las pruebas y el comando: quita a Oswin y su campana (los cargados) y olvida que se pusieron. */
    public static void olvidar(ServerLevel m) {
        Viajero v = buscarOswin(m);
        if (v != null) v.discard();
        Campana c = buscarCampana(m);
        if (c != null) c.discard();
        Morvain.olvidarHecho(m.getServer(), "oswin");
        Morvain.olvidarHecho(m.getServer(), "campana");
    }

    // ------------------------------------------------------------------ lo de cada jugador

    public static int marcas(ServerPlayer sp) {
        return Ficha.mision(sp, ID);
    }

    /** Si lleva la campana del peregrino. */
    public static boolean lleva(ServerPlayer sp) {
        return sp.getInventory().countItem(DarkCraftMod.CAMPANA_PEREGRINO.get()) > 0;
    }

    /** Adonde tiene que ir ahora (la brujula y el mapa): con la campana, a Oswin; con la mision y sin ella, a la campana. */
    public static BlockPos objetivo(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & (DEVUELTA | QUEDADA)) != 0) return null;
        if (lleva(sp)) return sitio();
        if ((k & ACEPTADA) != 0) {
            int[] c = cabo();
            return c == null ? null : new BlockPos(c[0], c[1], c[2]);
        }
        return null;
    }

    /** Por donde empieza a hablar (y lo apunta). */
    public static int inicio(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & DEVUELTA) != 0) return 12;
        if ((k & QUEDADA) != 0) return 11;
        if (lleva(sp)) {
            Ficha.mision(sp, ID, k | CONOCIDA);
            return (k & PRECIO) != 0 ? 7 : 107;
        }
        if ((k & ACEPTADA) != 0) return 5;
        if ((k & CONOCIDA) != 0) return 6;
        Ficha.mision(sp, ID, k | CONOCIDA);
        Misiones.enviar(sp);
        return 1;
    }

    /** Lo que se le responde (lo manda el cliente al elegirlo): buscarla, el precio, que no, devolversela o quedarsela. */
    public static void accion(ServerPlayer sp, Viajero v, String accion) {
        int k = marcas(sp);
        if ((k & (DEVUELTA | QUEDADA)) != 0) return;
        switch (accion) {
            case "aceptar" -> {
                if ((k & ACEPTADA) != 0) return;
                Ficha.mision(sp, ID, (k | ACEPTADA | CONOCIDA) & ~RECHAZADA);
                Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.oswin.titulo", 0));
                DarkCraftMod.LOGGER.info("[DarkCraft] {} acepta la mision de Oswin (la campana del peregrino)", sp.getGameProfile().getName());
            }
            case "precio" -> Ficha.mision(sp, ID, k | PRECIO | CONOCIDA);
            case "rechazar" -> Ficha.mision(sp, ID, k | RECHAZADA | CONOCIDA);
            case "devolver" -> devolver(sp, v, k);
            case "quedar" -> quedar(sp, k);
            default -> {
                return;
            }
        }
        Misiones.enviar(sp);
    }

    /** Se la devuelve: el rosario, las limosnas (si no pregunto que ganaba), el logro y los peregrinos que echan a andar. */
    private static void devolver(ServerPlayer sp, Viajero v, int k) {
        if (!quitarCampana(sp)) return;
        Ficha.mision(sp, ID, k | DEVUELTA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.PEREGRINO).get()));
        if ((k & PRECIO) == 0) Rol.dar(sp, LIMOSNAS, v.position().add(0, 1.4, 0));
        Logros.dar(sp, "campana_peregrino");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.oswin.titulo", 1));
        ServerLevel m = sp.serverLevel();
        v.tocar();
        m.playSound(null, v.getX(), v.getY() + 1.6, v.getZ(), DarkCraftMod.SFX_CAMPANA_TOQUE.get(), SoundSource.NEUTRAL, 1.4f, 1f);
        procesion(m, v);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} le devuelve la campana a Oswin{}", sp.getGameProfile().getName(), (k & PRECIO) == 0 ? " (sin pedir nada: limosnas)" : "");
    }

    /** Se la queda: la campana pasa a ser la de los difuntos, y el logro. */
    private static void quedar(ServerPlayer sp, int k) {
        if (!quitarCampana(sp)) return;
        Ficha.mision(sp, ID, k | QUEDADA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.CAMPANA_DIFUNTOS.get()));
        Logros.dar(sp, "campana_difuntos");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.oswin.titulo", 2));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} se queda la campana de Oswin", sp.getGameProfile().getName());
    }

    private static boolean quitarCampana(ServerPlayer sp) {
        Inventory inv = sp.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.is(DarkCraftMod.CAMPANA_PEREGRINO.get())) {
                st.shrink(1);
                inv.setChanged();
                return true;
            }
        }
        return false;
    }

    /** Coger la campana del arbol (clic derecho o la E): cada uno la suya, una vez. */
    public static void tomar(ServerPlayer sp, Campana c) {
        if (sp.isSpectator() || sp.distanceToSqr(c) > 36) return;
        int k = marcas(sp);
        if ((k & (DEVUELTA | QUEDADA)) != 0 || lleva(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, (k & (DEVUELTA | QUEDADA)) != 0 ? "darkcraft.mision.oswin.ya" : "darkcraft.mision.oswin.la_llevas", 0));
            return;
        }
        Equipo.dar(sp, new ItemStack(DarkCraftMod.CAMPANA_PEREGRINO.get()));
        Ficha.mision(sp, ID, k | TOMADA);
        c.sonar(true);
        Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, (k & CONOCIDA) != 0 ? "darkcraft.mision.oswin.cogida" : "darkcraft.mision.oswin.cogida_sin", 0));
        Misiones.enviar(sp);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} coge la campana del peregrino", sp.getGameProfile().getName());
    }

    /**
     * Los peregrinos muertos que por fin pueden pasar: cinco, al oeste de Oswin, que echan a andar hacia el norte por el camino de los
     * peregrinos (por el lado oeste del Refugio de la Fragua), rezando, y se desvanecen; con su rumor de rezos y pasos.
     */
    public static int procesion(ServerLevel m, Viajero v) {
        double[][] donde = {{-3.0, 1.0}, {-4.6, 2.6}, {-3.4, 4.2}, {-5.2, 5.4}, {-3.8, 7.0}};
        int n = 0;
        for (double[] d : donde) {
            Peregrino p = DarkCraftMod.PEREGRINO.get().create(m);
            if (p == null) continue;
            double x = v.getX() + d[0], z = v.getZ() + d[1];
            int y = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
            p.moveTo(x, y, z, 180f, 0f);
            p.rumbo(180f);
            m.addFreshEntity(p);
            m.sendParticles(ParticleTypes.SOUL, x, y + 1.0, z, 6, 0.25, 0.5, 0.25, 0.01);
            n++;
        }
        Vec3 c = v.position();
        m.playSound(null, c.x - 4, c.y + 1, c.z + 3, DarkCraftMod.SFX_PEREGRINOS.get(), SoundSource.NEUTRAL, 1.5f, 1f);
        return n;
    }
}
