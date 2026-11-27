package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Ballenero;
import com.darkcraft.morvain.Costillar;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.morvain.Varadero;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Viajero;
import com.darkcraft.rol.Equipo;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Talisman;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * La luz del muelle (parte 7an; respuestas 95 y 117 del plan): la mision de <b>Ylva</b>, la hija de Brann el ballenero, sentada en un
 * noray al final del espigon del Puerto Varado con su farol de fuego frio en el regazo, mirando al norte, hacia el Costillar.
 * <ol>
 * <li>La noche del cielo rojo el mar se fue de golpe; ella estaba en el faro con Tome, el farero, que no la dejo bajar a la arena. Su
 * padre sigue en el Costillar arponeando a la Vieja: desde alli no ve el puerto, ha olvidado el camino a casa (frase 1). Se le
 * responde: <b>ayudarle</b> (2: le da su farol, {@code farol_ylva}: "si ve esta luz, sabra que es el muelle"), <b>que paso aquella
 * noche</b> (3, y otra vez las respuestas) o <b>no</b> (4: "seguire con la luz encendida un rato mas").</li>
 * <li><b>Brann ve la luz</b> ({@code enemigos.Ballenero}): con el por debajo del 30 % de la vida y alguien que lleve el farol de Ylva
 * a 16 bloques, deja de pelear ("...Esa luz. Es la luz del muelle. ¿Ylva?"), se arrodilla y se va en paz ("Cuarenta anos... y la
 * mantuviste encendida. Pon la tetera, chiquilla. Vuelvo a casa"): {@link #calmado} (la marca {@link #CALMA} y el logro
 * {@code brann_calma} a quien estaba en la mision). Si cae peleando, como siempre.</li>
 * <li>Con Brann caido su farol queda en la arena del Costillar ({@link FarolBrann}, variante 0); se coge con clic derecho o la E
 * ({@link #tomar}), cada jugador el suyo; tambien sin conocer a Ylva.</li>
 * <li>Con el farol de su padre (7 si cayo peleando, 8 si vio la luz; 13 si no la conocia): <b>darselo</b> (9 y 10: los colgara juntos
 * al final del muelle, el de el y el de ella; se queda tranquila y descansa —en el cliente de quien se lo dio—; deja su farol de
 * fuego frio, el talisman {@code Talisman.Clase.YLVA}; logro {@code ylva_luz}) o <b>quedartelo</b> (11: le devuelves su luz; el
 * lado de el del muelle sigue a oscuras y ella seguira esperando —despues, la 12—; el farol de Brann, el talisman
 * {@code Talisman.Clase.BALLENERO}; logro {@code ylva_farol}).</li>
 * </ol>
 * Lo de cada jugador, en su ficha ({@code Ficha.mision}); Ylva y los dos faroles se ponen una vez ({@code Morvain.Datos} "ylva",
 * "farol_brann" —con Brann caido— y "farol_muelle" —cuando llega quien se lo dio—). A quien se lo dio, Ylva ya no se le ve.
 */
public final class Ylva {
    private Ylva() {}

    public static final String ID = "ylva";
    /** Las marcas de la mision ({@code Ficha.mision}). */
    public static final int CONOCIDA = 1, ACEPTADA = 2, RECHAZADA = 4, NOCHE = 8, TOMADA = 16, CALMA = 32, DADA = 64, QUEDADA = 128;
    /** No se guarda: la pone el servidor al mandar las misiones (lleva el farol de Brann ahora). */
    public static final int LLEVA = 1 << 20;
    /** Lo acabado (de una de las dos maneras). */
    public static final int ACABADA = DADA | QUEDADA;
    /** Donde se sienta Ylva (coordenadas del puerto: u = x - CX, v = z - CZ): al final del espigon, junto al pretil del norte. */
    static final int U = 75, V = Varadero.ESPIGON_V - 1;
    /** El farol de Brann en el muelle: sobre el pretil del norte (u 77), junto al farol de almas del final (u 78). */
    static final int U_FAROL = 77, V_FAROL = Varadero.ESPIGON_V - 3;
    /** A esta distancia de su sitio se pone Ylva, y los faroles a {@link #CERCA_FAROL}. */
    static final int CERCA = 56, CERCA_FAROL = 40;
    /** Brann ve la luz: por debajo de esta parte de su vida, con quien lleve el farol de Ylva a esta distancia. */
    public static final float CALMA_VIDA = 0.30f;
    public static final double CALMA_R = 16.0;
    /** Para las pruebas: las veces que Brann ha visto la luz y los faroles cogidos de la arena. */
    public static int CALMAS, COGIDOS;

    // ------------------------------------------------------------------ donde esta todo

    /** Donde se sienta Ylva (en el espigon). */
    public static BlockPos sitio() {
        return new BlockPos(Varadero.CX + U, Varadero.PISO, Varadero.CZ + V);
    }

    /** Hacia donde mira: el Costillar, al norte, donde esta su padre. */
    public static BlockPos mira() {
        return new BlockPos(Costillar.CX, Varadero.PISO, Costillar.CZ);
    }

    /** Donde queda el farol de Brann en la arena: donde velaba de rodillas (el sitio "brann" del costillar). */
    public static BlockPos farolArena() {
        int[] b = Costillar.get().sitio("brann");
        return new BlockPos(b[0], b[1], b[2]);
    }

    /** Donde queda en el muelle, junto al de ella (encima del pretil). */
    public static BlockPos farolMuelle() {
        return new BlockPos(Varadero.CX + U_FAROL, Varadero.PISO + 1, Varadero.CZ + V_FAROL);
    }

    // ------------------------------------------------------------------ cada segundo: ponerlos

    static void tick(ServerLevel m) {
        var srv = m.getServer();
        if (!Morvain.hecho(srv, "ylva")) {
            BlockPos p = sitio();
            if (m.isPositionEntityTicking(p) && alguien(m, p, CERCA, false)) {
                if (buscarYlva(m) != null) Morvain.marcarHecho(srv, "ylva");
                else ponerYlva(m);
            }
        }
        // el farol de Brann en la arena, en cuanto ha caido (y se ha deshecho del todo)
        if (!Morvain.hecho(srv, "farol_brann") && Costillar.enPaz(srv)) {
            BlockPos q = farolArena();
            if (m.isPositionEntityTicking(q) && alguien(m, q, CERCA_FAROL, false)
                    && m.getEntitiesOfClass(Ballenero.class, new AABB(q).inflate(40, 20, 40)).isEmpty()) {
                if (buscarFarol(m, 0) != null) Morvain.marcarHecho(srv, "farol_brann");
                else ponerFarol(m, 0);
            }
        }
        // el del muelle: solo hace falta cuando se acerca alguien que se lo dio
        if (!Morvain.hecho(srv, "farol_muelle")) {
            BlockPos q = farolMuelle();
            if (m.isPositionEntityTicking(q) && alguien(m, q, CERCA_FAROL, true)) {
                if (buscarFarol(m, 1) != null) Morvain.marcarHecho(srv, "farol_muelle");
                else ponerFarol(m, 1);
            }
        }
    }

    private static boolean alguien(ServerLevel m, BlockPos p, int r, boolean dada) {
        for (ServerPlayer sp : m.players())
            if (!sp.isSpectator() && sp.blockPosition().distSqr(p) < (double) r * r && (!dada || (marcas(sp) & DADA) != 0)) return true;
        return false;
    }

    /** Pone a Ylva en su noray al final del espigon, mirando al norte. */
    public static Viajero ponerYlva(ServerLevel m) {
        BlockPos s = sitio();
        m.getChunk(s.getX() >> 4, s.getZ() >> 4);
        int y = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX(), s.getZ());
        if (Math.abs(y - Varadero.PISO) > 2) y = Varadero.PISO;
        BlockPos pie = new BlockPos(s.getX(), y, s.getZ());
        // la sal suelta o lo que crece donde se sienta, fuera
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos q = pie.offset(dx, dy, dz);
                    BlockState b = m.getBlockState(q);
                    if (!b.isAir() && b.canBeReplaced()) m.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
                }
        Viajero v = Viajero.poner(m, pie, mira(), Viajero.Quien.YLVA);
        if (v == null) return null;
        Morvain.marcarHecho(m.getServer(), "ylva");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Ylva, al final del espigon del Puerto Varado ({})", pie.toShortString());
        return v;
    }

    /** Pone el farol de Brann: tumbado en la arena del Costillar (0) o en el pretil del muelle, junto al de Ylva (1). */
    public static FarolBrann ponerFarol(ServerLevel m, int variante) {
        BlockPos p = variante == 0 ? farolArena() : farolMuelle();
        m.getChunk(p.getX() >> 4, p.getZ() >> 4);
        int y = p.getY();
        if (variante == 0) {
            int h = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ());
            if (Math.abs(h - p.getY()) <= 3) y = h;
        }
        FarolBrann f = DarkCraftMod.FAROL_BRANN.get().create(m);
        if (f == null) return null;
        f.variante(variante);
        // (en la arena, sobre lo que se pisa; en el muelle, sobre el pretil: el muro se dibuja de un bloque aunque choque a 1,5)
        double yy = variante == 0 ? sueloEn(m, new BlockPos(p.getX(), y, p.getZ())) : p.getY();
        f.moveTo(p.getX() + 0.5, yy, p.getZ() + 0.5, variante == 0 ? 35f : 0f, 0f);
        f.addTag("darkcraft_farol_brann" + (variante == 0 ? "" : "_" + variante));
        m.addFreshEntity(f);
        Morvain.marcarHecho(m.getServer(), variante == 0 ? "farol_brann" : "farol_muelle");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: el farol de Brann {} ({})", variante == 0 ? "en la arena del Costillar" : "en el muelle, junto al de Ylva",
                new BlockPos(p.getX(), y, p.getZ()).toShortString());
        return f;
    }

    /** Lo alto de lo que se pisa en esa columna, cerca de p (la sal suelta, una capa fina, no cuenta: no tiene choque). */
    private static double sueloEn(ServerLevel m, BlockPos p) {
        for (int dy = 1; dy >= -3; dy--) {
            BlockPos q = p.above(dy);
            var s = m.getBlockState(q).getCollisionShape(m, q);
            if (!s.isEmpty()) return q.getY() + s.max(net.minecraft.core.Direction.Axis.Y);
        }
        return p.getY();
    }

    /** Ylva, si su chunk esta cargado. */
    public static Viajero buscarYlva(ServerLevel m) {
        BlockPos s = sitio();
        if (!m.isLoaded(s)) return null;
        for (Viajero v : m.getEntitiesOfClass(Viajero.class, new AABB(s).inflate(6, 20, 6)))
            if (v.quien() == Viajero.Quien.YLVA) return v;
        return null;
    }

    /** El farol de Brann (0 el de la arena, 1 el del muelle), si su chunk esta cargado. */
    public static FarolBrann buscarFarol(ServerLevel m, int variante) {
        BlockPos p = variante == 0 ? farolArena() : farolMuelle();
        if (!m.isLoaded(p)) return null;
        for (FarolBrann f : m.getEntitiesOfClass(FarolBrann.class, new AABB(p).inflate(3, 6, 3))) if (f.variante() == variante) return f;
        return null;
    }

    /** Para las pruebas y el comando: quita a Ylva y los faroles (los cargados) y olvida que se pusieron. */
    public static void olvidar(ServerLevel m) {
        Viajero v = buscarYlva(m);
        if (v != null) v.discard();
        for (int i = 0; i <= 1; i++) {
            FarolBrann f = buscarFarol(m, i);
            if (f != null) f.discard();
        }
        Morvain.olvidarHecho(m.getServer(), "ylva");
        Morvain.olvidarHecho(m.getServer(), "farol_brann");
        Morvain.olvidarHecho(m.getServer(), "farol_muelle");
    }

    // ------------------------------------------------------------------ lo de cada jugador

    public static int marcas(ServerPlayer sp) {
        return Ficha.mision(sp, ID);
    }

    /** Si lleva el farol de Brann (lo que se busca). */
    public static boolean lleva(ServerPlayer sp) {
        return sp.getInventory().countItem(DarkCraftMod.FAROL_BALLENERO.get()) > 0;
    }

    /** Si lleva el farol de fuego frio de Ylva (la luz que Brann puede ver). */
    public static boolean conLuz(ServerPlayer sp) {
        return sp.getInventory().countItem(DarkCraftMod.FAROL_YLVA.get()) > 0;
    }

    /** Si a este jugador le toca ver a Ylva: hasta que le da el farol de su padre (entonces descansa). */
    public static boolean visible(ServerPlayer sp) {
        return (marcas(sp) & DADA) == 0;
    }

    /** Adonde tiene que ir ahora (la brujula y el mapa): con el farol de Brann, a Ylva; con la mision y sin el, al Costillar. */
    public static BlockPos objetivo(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & ACABADA) != 0) return null;
        if (lleva(sp)) return sitio();
        if ((k & ACEPTADA) != 0) return farolArena();
        return null;
    }

    /** Por donde empieza a hablar (y lo apunta). */
    public static int inicio(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & QUEDADA) != 0) return 12;
        if ((k & DADA) != 0) return 10;
        if (lleva(sp)) {
            Ficha.mision(sp, ID, k | CONOCIDA);
            Misiones.enviar(sp);
            if ((k & CONOCIDA) == 0) return 13;
            return (k & CALMA) != 0 ? 8 : 7;
        }
        if ((k & ACEPTADA) != 0) return 5;
        if ((k & CONOCIDA) != 0) return 6;
        Ficha.mision(sp, ID, k | CONOCIDA);
        Misiones.enviar(sp);
        return 1;
    }

    /** Lo que se le responde (lo manda el cliente al elegirlo): ayudarle, aquella noche, que no, darle el farol o quedarselo. */
    public static void accion(ServerPlayer sp, Viajero v, String accion) {
        int k = marcas(sp);
        if ((k & ACABADA) != 0) return;
        switch (accion) {
            case "aceptar" -> {
                if ((k & ACEPTADA) != 0) return;
                Ficha.mision(sp, ID, (k | ACEPTADA | CONOCIDA) & ~RECHAZADA);
                if (!conLuz(sp)) Equipo.dar(sp, new ItemStack(DarkCraftMod.FAROL_YLVA.get()));
                sp.serverLevel().playSound(null, v.getX(), v.getY() + 0.8, v.getZ(), DarkCraftMod.SFX_FAROL_FRIO.get(), SoundSource.NEUTRAL, 1.0f, 1f);
                Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.ylva.titulo", 0));
                DarkCraftMod.LOGGER.info("[DarkCraft] {} acepta la mision de Ylva (la luz del muelle) y se lleva su farol", sp.getGameProfile().getName());
            }
            case "precio" -> Ficha.mision(sp, ID, k | NOCHE | CONOCIDA);
            case "rechazar" -> Ficha.mision(sp, ID, k | RECHAZADA | CONOCIDA);
            case "devolver" -> dar(sp, v, k);
            case "quedar" -> quedar(sp, k);
            default -> {
                return;
            }
        }
        Misiones.enviar(sp);
    }

    /** Darle el farol de su padre: los colgara juntos al final del muelle y descansa; te deja su farol de fuego frio. */
    private static void dar(ServerPlayer sp, Viajero v, int k) {
        if (!Hedda.quitar(sp, DarkCraftMod.FAROL_BALLENERO.get())) return;
        quitarTodos(sp, DarkCraftMod.FAROL_YLVA.get());
        Ficha.mision(sp, ID, k | DADA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.TALISMANES.get(Talisman.Clase.YLVA).get()));
        Logros.dar(sp, "ylva_luz");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.ylva.titulo", 1));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} le da a Ylva el farol de su padre", sp.getGameProfile().getName());
    }

    /** Quedarse el farol de Brann: le devuelves su luz y su lado del muelle sigue a oscuras. */
    private static void quedar(ServerPlayer sp, int k) {
        if (!Hedda.quitar(sp, DarkCraftMod.FAROL_BALLENERO.get())) return;
        quitarTodos(sp, DarkCraftMod.FAROL_YLVA.get());
        Ficha.mision(sp, ID, k | QUEDADA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.TALISMANES.get(Talisman.Clase.BALLENERO).get()));
        Logros.dar(sp, "ylva_farol");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.ylva.titulo", 1));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} se queda el farol de Brann y le devuelve a Ylva su luz", sp.getGameProfile().getName());
    }

    private static void quitarTodos(ServerPlayer sp, Item item) {
        Inventory inv = sp.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++)
            if (inv.getItem(i).is(item)) inv.setItem(i, ItemStack.EMPTY);
        inv.setChanged();
    }

    /** Coger el farol de Brann de la arena (clic derecho o la E): cada uno el suyo, si no lo lleva y no acabo la mision. */
    public static void tomar(ServerPlayer sp, FarolBrann f) {
        if (sp.isSpectator() || sp.distanceToSqr(f) > 36 || f.variante() != 0) return;
        int k = marcas(sp);
        if ((k & ACABADA) != 0 || lleva(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, lleva(sp) ? "darkcraft.mision.ylva.lo_llevas" : "darkcraft.mision.ylva.nada", 0));
            return;
        }
        Equipo.dar(sp, new ItemStack(DarkCraftMod.FAROL_BALLENERO.get()));
        Ficha.mision(sp, ID, k | TOMADA);
        ServerLevel m = sp.serverLevel();
        m.playSound(null, f.getX(), f.getY() + 0.3, f.getZ(), SoundEvents.LANTERN_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
        m.sendParticles(ParticleTypes.WHITE_ASH, f.getX(), f.getY() + 0.3, f.getZ(), 14, 0.25, 0.2, 0.25, 0.01);
        Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, (k & CONOCIDA) != 0 ? "darkcraft.mision.ylva.cogido" : "darkcraft.mision.ylva.cogido_sin", 0));
        Misiones.enviar(sp);
        COGIDOS++;
        DarkCraftMod.LOGGER.info("[DarkCraft] {} coge el farol de Brann de la arena del Costillar", sp.getGameProfile().getName());
    }

    // ------------------------------------------------------------------ Brann ve la luz

    /** Quien lleva el farol de Ylva cerca de Brann (a {@link #CALMA_R}), o null. */
    public static ServerPlayer luzCerca(ServerLevel m, Entity brann) {
        for (ServerPlayer sp : m.players())
            if (sp.isAlive() && !sp.isSpectator() && sp.distanceToSqr(brann) < CALMA_R * CALMA_R && conLuz(sp)) return sp;
        return null;
    }

    /**
     * Brann ha visto la luz que lleva {@code luz} (lo llama {@code Ballenero}): a ese y a quien este en la mision o lleve la luz, a 48
     * bloques, la marca de que lo vio (Ylva dira la 8) y el logro.
     */
    public static void calmado(ServerLevel m, Entity brann, ServerPlayer luz) {
        CALMAS++;
        java.util.Set<ServerPlayer> quienes = new java.util.LinkedHashSet<>();
        if (luz != null) quienes.add(luz);
        for (ServerPlayer sp : m.players())
            if (sp.isAlive() && !sp.isSpectator() && sp.distanceToSqr(brann) < 48 * 48 && ((marcas(sp) & ACEPTADA) != 0 || conLuz(sp))) quienes.add(sp);
        for (ServerPlayer sp : quienes) {
            Ficha.mision(sp, ID, marcas(sp) | CALMA);
            Logros.dar(sp, "brann_calma");
            Misiones.enviar(sp);
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] Brann ve la luz del muelle (la lleva {}) y baja el arpon", luz == null ? "-" : luz.getGameProfile().getName());
    }
}
