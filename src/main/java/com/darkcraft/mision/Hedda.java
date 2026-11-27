package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Capital;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Viajero;
import com.darkcraft.rol.Equipo;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Talisman;
import com.darkcraft.rol.Talismanes;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * La bufanda roja (parte 7ah; respuestas 95 y 117 del plan): la mision de <b>Hedda</b>, la madre que teje sentada en el camino de
 * la Puerta Oeste de la Capital en ruinas, entre el santuario y los carros quemados, con su farol al lado.
 * <ol>
 * <li>Su hijo Tam guardaba la Puerta Oeste; la noche del cielo rojo la cerraron y no volvio (frase 1). Se le responde: <b>buscarle</b>
 * (2: la mision empieza; los guardias tenian su sala encima del arco, junto al torno), <b>que paso aquella noche</b> (3: el
 * Condestable mando cerrar todas las puertas; y otra vez las respuestas) o <b>no</b> (4).</li>
 * <li>Tam ({@link RestosTam}) esta en la sala de la guardia, sentado de espaldas al torno del rastrillo con su lanza atravesada en
 * la rueda: nadie iba a subir la reja. Lleva al cuello la bufanda roja que le tejio su madre; se coge con clic derecho o la E
 * ({@link #tomar}), cada jugador la suya; tambien sin conocer a Hedda.</li>
 * <li>Con la bufanda (7, o 14 si no la conocia) pregunta como murio su hijo. Tres respuestas que acaban la mision de tres maneras:
 * <b>la verdad</b> (8 y 9: que sostuvo la puerta cerrada, por orden; ella se va a cerrarle los ojos —para quien se lo dijo, se la
 * encuentra de rodillas junto a el, {@code Viajero} variante 1, frase 13— y le pide que saque su lanza de la rueda: la Lanza de la
 * Puerta Oeste; logro {@code hedda_verdad}), <b>la mentira</b> (10: que murio intentando abrirla; se pone la bufanda, deja de
 * esperar y se apaga en paz —en el cliente de quien se lo dijo—; deja su bendicion, {@code Talisman.Clase.MADRE}; logro {@code hedda_mentira}) o
 * <b>callar</b> (11: que no le encontro; ella seguira tejiendo y esperando —despues, la 12—; la bufanda se queda contigo:
 * {@code Talisman.Clase.BUFANDA}; logro {@code hedda_callar}).</li>
 * </ol>
 * Lo de cada jugador, en su ficha ({@code Ficha.mision}); Hedda, Tam y la Hedda de rodillas se ponen una vez, la primera vez que
 * alguien pasa cerca ({@code Morvain.Datos} "hedda", "tam" y "hedda_luto"; la de rodillas, solo cuando llega quien le dijo la
 * verdad). Quien ya no la tiene que ver no la ve ({@link #visible}).
 */
public final class Hedda {
    private Hedda() {}

    public static final String ID = "hedda";
    /** Las marcas de la mision ({@code Ficha.mision}). */
    public static final int CONOCIDA = 1, ACEPTADA = 2, RECHAZADA = 4, NOCHE = 8, TOMADA = 16, VERDAD = 32, MENTIRA = 64, CALLADA = 128,
            LANZA = 256;
    /** No se guarda: la pone el servidor al mandar las misiones (lleva la bufanda ahora). */
    public static final int LLEVA = 1 << 20;
    /** Lo acabado (de una de las tres maneras). */
    public static final int ACABADA = VERDAD | MENTIRA | CALLADA;
    /** Donde se sienta Hedda (coordenadas de la capital: u = x - CX, v = z - CZ): en el camino de la Puerta Oeste, de cara a ella. */
    static final int U = -45, V = 4;
    /** A esta distancia de su sitio se pone Hedda, y Tam (y la Hedda de rodillas) a {@link #CERCA_TAM} de la sala de la guardia. */
    static final int CERCA = 56, CERCA_TAM = 40;
    /** La bendicion de Hedda: cada cuanto puede salvar (ticks), lo que cura y por debajo de cuanta vida salta. */
    public static final int ESPERA_BENDICION = 2400;
    public static final float CURA_BENDICION = 0.30f, UMBRAL_BENDICION = 0.25f;

    // ------------------------------------------------------------------ donde esta todo

    /** Donde se sienta Hedda (la y, la del suelo de la ciudad: la de verdad sale del terreno al ponerla). */
    public static BlockPos sitio() {
        return new BlockPos(Capital.CX + U, Capital.PISO, Capital.CZ + V);
    }

    /** Hacia donde mira: la Puerta Oeste. */
    public static BlockPos puerta() {
        return new BlockPos(Capital.CX - 6, Capital.PISO, Capital.CZ);
    }

    /** La sala de la guardia ({x, y que se pisa, z} del sitio "camara" de la capital: u 1, v 0). */
    private static int[] camara() {
        return Capital.get().sitio("camara");
    }

    /** Donde esta Tam: sentado de espaldas al torno (u -1, v 0), mirando al este. */
    public static BlockPos tam() {
        int[] c = camara();
        return new BlockPos(c[0] - 2, c[1], c[2]);
    }

    /** Donde se arrodilla Hedda junto a el, tras la verdad (u 0, v -1). */
    public static BlockPos luto() {
        int[] c = camara();
        return new BlockPos(c[0] - 1, c[1], c[2] - 1);
    }

    // ------------------------------------------------------------------ cada segundo: ponerlos

    static void tick(ServerLevel m) {
        var srv = m.getServer();
        if (!Morvain.hecho(srv, "hedda")) {
            BlockPos p = sitio();
            if (m.isPositionEntityTicking(p) && alguien(m, p, CERCA, false)) {
                if (buscarHedda(m, 0) != null) Morvain.marcarHecho(srv, "hedda");
                else ponerHedda(m);
            }
        }
        if (!Morvain.hecho(srv, "tam")) {
            BlockPos q = tam();
            if (m.isPositionEntityTicking(q) && alguien(m, q, CERCA_TAM, false)) {
                if (buscarTam(m) != null) Morvain.marcarHecho(srv, "tam");
                else ponerTam(m);
            }
        }
        // la Hedda de rodillas: solo hace falta cuando se acerca alguien que le dijo la verdad
        if (!Morvain.hecho(srv, "hedda_luto")) {
            BlockPos q = luto();
            if (m.isPositionEntityTicking(q) && alguien(m, q, CERCA_TAM, true)) {
                if (buscarHedda(m, 1) != null) Morvain.marcarHecho(srv, "hedda_luto");
                else ponerLuto(m);
            }
        }
    }

    private static boolean alguien(ServerLevel m, BlockPos p, int r, boolean verdad) {
        for (ServerPlayer sp : m.players())
            if (!sp.isSpectator() && sp.blockPosition().distSqr(p) < (double) r * r && (!verdad || (marcas(sp) & VERDAD) != 0)) return true;
        return false;
    }

    /** Pone a Hedda (en el suelo de su sitio, mirando a la Puerta Oeste) con su farol al lado. */
    public static Viajero ponerHedda(ServerLevel m) {
        BlockPos s = sitio();
        m.getChunk(s.getX() >> 4, s.getZ() >> 4);
        int y = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX(), s.getZ());
        BlockPos pie = new BlockPos(s.getX(), y, s.getZ());
        // lo que crece o la ceniza de alrededor fuera (que no le tapen el taburete ni el ovillo)
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos q = pie.offset(dx, dy, dz);
                    BlockState b = m.getBlockState(q);
                    if (!b.isAir() && b.canBeReplaced()) m.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
                }
        // su farol, en el suelo a su izquierda (al norte)
        BlockPos farol = pie.north();
        int yf = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, farol.getX(), farol.getZ());
        if (Math.abs(yf - pie.getY()) <= 1) m.setBlock(new BlockPos(farol.getX(), yf, farol.getZ()), Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, false), 3);
        Viajero v = Viajero.poner(m, pie, puerta(), Viajero.Quien.HEDDA);
        if (v == null) return null;
        Morvain.marcarHecho(m.getServer(), "hedda");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Hedda teje en el camino de la Puerta Oeste ({})", pie.toShortString());
        return v;
    }

    /** Pone a Tam en la sala de la guardia, sentado de espaldas al torno, mirando al este. */
    public static RestosTam ponerTam(ServerLevel m) {
        BlockPos t = tam();
        m.getChunk(t.getX() >> 4, t.getZ() >> 4);
        RestosTam e = DarkCraftMod.RESTOS_TAM.get().create(m);
        if (e == null) return null;
        e.moveTo(t.getX() + 0.5, t.getY(), t.getZ() + 0.5, -90f, 0f);
        e.addTag("darkcraft_restos_tam");
        m.addFreshEntity(e);
        Morvain.marcarHecho(m.getServer(), "tam");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: los huesos de Tam junto al torno de la Puerta Oeste ({})", t.toShortString());
        return e;
    }

    /** Pone a Hedda de rodillas junto a Tam (la que solo ve quien le dijo la verdad). */
    public static Viajero ponerLuto(ServerLevel m) {
        BlockPos l = luto();
        m.getChunk(l.getX() >> 4, l.getZ() >> 4);
        Viajero v = Viajero.poner(m, l, tam(), Viajero.Quien.HEDDA, 1);
        if (v == null) return null;
        Morvain.marcarHecho(m.getServer(), "hedda_luto");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Hedda, de rodillas junto a Tam ({})", l.toShortString());
        return v;
    }

    /** Hedda (la variante 0: la del camino; la 1: la de rodillas), si su chunk esta cargado. */
    public static Viajero buscarHedda(ServerLevel m, int variante) {
        BlockPos s = variante == 0 ? sitio() : luto();
        if (!m.isLoaded(s)) return null;
        for (Viajero v : m.getEntitiesOfClass(Viajero.class, new AABB(s).inflate(6, 40, 6)))
            if (v.quien() == Viajero.Quien.HEDDA && v.variante() == variante) return v;
        return null;
    }

    /** Tam, si su chunk esta cargado. */
    public static RestosTam buscarTam(ServerLevel m) {
        BlockPos t = tam();
        if (!m.isLoaded(t)) return null;
        for (RestosTam e : m.getEntitiesOfClass(RestosTam.class, new AABB(t).inflate(3))) return e;
        return null;
    }

    /** Para las pruebas y el comando: quita a Hedda, a Tam y a la Hedda de rodillas (los cargados) y olvida que se pusieron. */
    public static void olvidar(ServerLevel m) {
        for (int i = 0; i <= 1; i++) {
            Viajero v = buscarHedda(m, i);
            if (v != null) v.discard();
        }
        RestosTam t = buscarTam(m);
        if (t != null) t.discard();
        Morvain.olvidarHecho(m.getServer(), "hedda");
        Morvain.olvidarHecho(m.getServer(), "tam");
        Morvain.olvidarHecho(m.getServer(), "hedda_luto");
    }

    // ------------------------------------------------------------------ lo de cada jugador

    public static int marcas(ServerPlayer sp) {
        return Ficha.mision(sp, ID);
    }

    /** Si lleva la bufanda roja. */
    public static boolean lleva(ServerPlayer sp) {
        return sp.getInventory().countItem(DarkCraftMod.BUFANDA_ROJA.get()) > 0;
    }

    /** Si a este jugador le toca ver a esta Hedda (la del camino, hasta que se acaba con la verdad o la mentira; la de rodillas, tras la verdad). */
    public static boolean visible(ServerPlayer sp, Viajero v) {
        int k = marcas(sp);
        return v.variante() == 1 ? (k & VERDAD) != 0 : (k & (VERDAD | MENTIRA)) == 0;
    }

    /**
     * Adonde tiene que ir ahora (la brujula y el mapa): con la bufanda, a Hedda; con la mision y sin ella, a Tam; tras la verdad, a la
     * lanza (si no la saco ya).
     */
    public static BlockPos objetivo(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & (MENTIRA | CALLADA)) != 0) return null;
        if ((k & VERDAD) != 0) return (k & LANZA) != 0 ? null : tam();
        if (lleva(sp)) return sitio();
        if ((k & ACEPTADA) != 0) return tam();
        return null;
    }

    /** Por donde empieza a hablar (y lo apunta). */
    public static int inicio(ServerPlayer sp, Viajero v) {
        int k = marcas(sp);
        if (v.variante() == 1) return 13;
        if ((k & CALLADA) != 0) return 12;
        if (lleva(sp) && (k & ACABADA) == 0) {
            Ficha.mision(sp, ID, k | CONOCIDA);
            Misiones.enviar(sp);
            return (k & CONOCIDA) != 0 ? 7 : 14;
        }
        if ((k & ACEPTADA) != 0) return 5;
        if ((k & CONOCIDA) != 0) return 6;
        Ficha.mision(sp, ID, k | CONOCIDA);
        Misiones.enviar(sp);
        return 1;
    }

    /** Lo que se le responde (lo manda el cliente al elegirlo): buscarle, aquella noche, que no, la verdad, la mentira o callar. */
    public static void accion(ServerPlayer sp, Viajero v, String accion) {
        int k = marcas(sp);
        if ((k & ACABADA) != 0 || v.variante() != 0) return;
        switch (accion) {
            case "aceptar" -> {
                if ((k & ACEPTADA) != 0) return;
                Ficha.mision(sp, ID, (k | ACEPTADA | CONOCIDA) & ~RECHAZADA);
                Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.hedda.titulo", 0));
                DarkCraftMod.LOGGER.info("[DarkCraft] {} acepta la mision de Hedda (la bufanda roja)", sp.getGameProfile().getName());
            }
            case "precio" -> Ficha.mision(sp, ID, k | NOCHE | CONOCIDA);
            case "rechazar" -> Ficha.mision(sp, ID, k | RECHAZADA | CONOCIDA);
            case "verdad" -> verdad(sp, v, k);
            case "mentira" -> mentira(sp, v, k);
            case "callar" -> callar(sp, k);
            default -> {
                return;
            }
        }
        Misiones.enviar(sp);
    }

    /** La verdad: Hedda se lleva la bufanda y se va con su hijo (a quien se lo dijo se le aparece alli); la lanza le espera en la rueda. */
    private static void verdad(ServerPlayer sp, Viajero v, int k) {
        if (!quitar(sp, DarkCraftMod.BUFANDA_ROJA.get())) return;
        Ficha.mision(sp, ID, k | VERDAD | CONOCIDA);
        Logros.dar(sp, "hedda_verdad");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.hedda.titulo", 1));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} le dice a Hedda la verdad: Tam sostuvo la puerta cerrada", sp.getGameProfile().getName());
    }

    /** La mentira: Hedda se pone la bufanda, deja de esperar y se apaga; deja su bendicion. */
    private static void mentira(ServerPlayer sp, Viajero v, int k) {
        if (!quitar(sp, DarkCraftMod.BUFANDA_ROJA.get())) return;
        Ficha.mision(sp, ID, k | MENTIRA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.TALISMANES.get(Talisman.Clase.MADRE).get()));
        Logros.dar(sp, "hedda_mentira");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.hedda.titulo", 1));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} le dice a Hedda que Tam murio intentando abrir la puerta", sp.getGameProfile().getName());
    }

    /** Callar: la bufanda se queda contigo (el talisman) y ella seguira esperando. */
    private static void callar(ServerPlayer sp, int k) {
        if (!quitar(sp, DarkCraftMod.BUFANDA_ROJA.get())) return;
        Ficha.mision(sp, ID, k | CALLADA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.TALISMANES.get(Talisman.Clase.BUFANDA).get()));
        Logros.dar(sp, "hedda_callar");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.hedda.titulo", 1));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} le calla a Hedda lo de Tam y se queda la bufanda", sp.getGameProfile().getName());
    }

    static boolean quitar(ServerPlayer sp, Item item) {
        Inventory inv = sp.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.is(item)) {
                st.shrink(1);
                inv.setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Lo que se hace con Tam (clic derecho o la E): coger la bufanda (cada uno la suya, si no la lleva y no acabo la mision) o, tras
     * decirle la verdad a Hedda, sacar la lanza de la rueda.
     */
    public static void tomar(ServerPlayer sp, RestosTam r) {
        if (sp.isSpectator() || sp.distanceToSqr(r) > 36) return;
        int k = marcas(sp);
        ServerLevel m = sp.serverLevel();
        if ((k & VERDAD) != 0) {
            if ((k & LANZA) != 0) {
                Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.mision.hedda.nada", 0));
                return;
            }
            Equipo.dar(sp, new ItemStack(DarkCraftMod.LANZA_TAM.get()));
            Ficha.mision(sp, ID, k | LANZA);
            m.playSound(null, r.getX(), r.getY() + 0.6, r.getZ(), DarkCraftMod.SFX_LANZA_SACAR.get(), SoundSource.PLAYERS, 1.1f, 1f);
            m.sendParticles(ParticleTypes.ASH, r.getX() - 1, r.getY() + 0.6, r.getZ(), 18, 0.3, 0.3, 0.6, 0.01);
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.mision.hedda.lanza_cogida", 0));
            Misiones.enviar(sp);
            DarkCraftMod.LOGGER.info("[DarkCraft] {} saca la lanza de Tam de la rueda del torno", sp.getGameProfile().getName());
            return;
        }
        if ((k & ACABADA) != 0 || lleva(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, lleva(sp) ? "darkcraft.mision.hedda.la_llevas" : "darkcraft.mision.hedda.nada", 0));
            return;
        }
        Equipo.dar(sp, new ItemStack(DarkCraftMod.BUFANDA_ROJA.get()));
        Ficha.mision(sp, ID, k | TOMADA);
        Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, (k & CONOCIDA) != 0 ? "darkcraft.mision.hedda.cogida" : "darkcraft.mision.hedda.cogida_sin", 0));
        Misiones.enviar(sp);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} coge la bufanda roja de Tam", sp.getGameProfile().getName());
    }

    // ------------------------------------------------------------------ la bendicion de Hedda (el talisman de la mentira)

    private static final Map<UUID, Long> BENDECIDO = new HashMap<>();
    private static final Set<UUID> POR_CURAR = new HashSet<>();
    /** Para la prueba: las veces que ha salvado a alguien. */
    public static int BENDICIONES;

    /**
     * Un golpe que deja al que lleva la Bendicion de Hedda por debajo de un cuarto de la vida (sin matarle): una vez cada dos minutos,
     * en el tick siguiente le cura un 30 %.
     */
    static void golpe(ServerPlayer sp, float dano) {
        if (!Talismanes.lleva(sp, Talisman.Clase.MADRE)) return;
        float queda = sp.getHealth() - dano;
        if (queda <= 0f || queda >= sp.getMaxHealth() * UMBRAL_BENDICION) return;
        long t = sp.serverLevel().getGameTime();
        Long antes = BENDECIDO.get(sp.getUUID());
        if (antes != null && t - antes < ESPERA_BENDICION && t >= antes) return;
        BENDECIDO.put(sp.getUUID(), t);
        POR_CURAR.add(sp.getUUID());
    }

    /** Despues del golpe (el tick del jugador): la cura. */
    static void curar(ServerPlayer sp) {
        if (!POR_CURAR.remove(sp.getUUID()) || !sp.isAlive()) return;
        sp.heal(sp.getMaxHealth() * CURA_BENDICION);
        ServerLevel m = sp.serverLevel();
        m.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0, sp.getZ(), 16, 0.35, 0.6, 0.35, 0.02);
        m.playSound(null, sp.getX(), sp.getY() + 1, sp.getZ(), DarkCraftMod.SFX_HECHIZO_CURAR.get(), SoundSource.PLAYERS, 0.8f, 1.15f);
        Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.mision.hedda.bendicion", 0));
        BENDICIONES++;
    }

    /** Para las pruebas (el jugador de mentira no tiene tick): la cura que tocaria en su tick siguiente. */
    public static void curarParaPrueba(ServerPlayer sp) {
        curar(sp);
    }

    /** Para las pruebas: que pueda volver a salvarle ya. */
    public static void bendicionLista(ServerPlayer sp) {
        BENDECIDO.remove(sp.getUUID());
    }

    static void olvidarJugador(UUID u) {
        POR_CURAR.remove(u);
    }
}
