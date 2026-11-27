package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.morvain.Pilotes;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Viajero;
import com.darkcraft.rol.Equipo;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Talisman;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Las balizas de la niebla (parte 7ao; respuestas 95 y 117 del plan): la mision de <b>Corwen</b>, el viejo farolero de la Aldea de los
 * Pilotes, sentado en una caja de pescado al final de la pasarela que sale del santuario de los Juncos, mirando a la laguna.
 * <ol>
 * <li>Cada noche encendia las tres balizas del pantano para que las barcas encontraran el camino a casa; la noche del cielo rojo el
 * viento le apago la primera, le temblaban las manos y oyo las campanas de las barcas toda la noche, y luego nada (frase 1; aquella
 * noche, la 3). Se le responde: <b>encenderlas</b> (2: te da su yesquero, {@code yesquero}), <b>aquella noche</b> (3, y otra vez las
 * respuestas) o <b>ahora no</b> (4).</li>
 * <li><b>Las tres balizas</b> ({@link Baliza}): al final del muelle del embarcadero (con el fuego fatuo que lo guarda cerca), en el
 * islote del oeste y en el de junto a la capilla ahogada (hay que cruzar el agua, entre los que flotan boca abajo). Se encienden con el
 * yesquero ({@link #encender}: cada jugador las suyas, con su sonido, sus llamas y un aviso de cuantas lleva). Con las tres: las
 * campanas de las barcas suenan en la niebla (solo para el) y el logro {@code balizas}.</li>
 * <li>De vuelta con Corwen (7: las ve, oye las campanas: "¿debería bajar a recibirlas?"): <b>que vaya</b> (8: se mete en el agua y se
 * pierde en la niebla —en el cliente de quien se lo dijo—; te deja su farol, el talisman {@code Talisman.Clase.FAROLERO}; logro
 * {@code corwen_barcas}) o <b>que se quede</b> a guardar la luz (9: te da su pertiga, {@code Pertiga}; se queda con su farol encendido
 * en el suelo; logro {@code corwen_luz}; despues, la 10).</li>
 * </ol>
 * Lo de cada jugador, en su ficha ({@code Ficha.mision}); Corwen y las balizas se ponen una vez ({@code Morvain.Datos} "corwen",
 * "baliza_0..2"). A quien le dejo ir, Corwen ya no se le ve.
 */
public final class Corwen {
    private Corwen() {}

    public static final String ID = "corwen";
    /** Las marcas de la mision ({@code Ficha.mision}): las tres balizas son B1 (el muelle), B2 (el islote del oeste) y B3 (la capilla). */
    public static final int CONOCIDA = 1, ACEPTADA = 2, RECHAZADA = 4, NOCHE = 8, B1 = 16, B2 = 32, B3 = 64, IDA = 128, QUEDA = 256;
    /** Las tres encendidas y lo acabado (de una de las dos maneras). */
    public static final int TODAS = B1 | B2 | B3, ACABADA = IDA | QUEDA;
    /** Donde se sienta Corwen (coordenadas de la aldea: u = x - CX, v = z - CZ): al final de la pasarela del santuario, en tierra. */
    static final int U = 47, V = 40;
    /** Las tres balizas: {u, v}. El muelle (en las tablas, junto a la laguna), el islote del oeste y el de la capilla. */
    public static final int[][] BALIZAS = {{36, 22}, {-26, 10}, {12, -33}};
    /** A esta distancia de su sitio se pone Corwen, y las balizas a {@link #CERCA_BALIZA}. */
    static final int CERCA = 56, CERCA_BALIZA = 64;
    /** Para las pruebas: las balizas encendidas desde que arranco el juego. */
    public static int ENCENDIDAS;

    // ------------------------------------------------------------------ donde esta todo

    /** Donde se sienta Corwen (lo que pisa: el suelo de la orilla). */
    public static BlockPos sitio() {
        int x = Pilotes.CX + U, z = Pilotes.CZ + V;
        return new BlockPos(x, Pilotes.pie(x, z), z);
    }

    /** Hacia donde mira: la laguna, donde se perdieron las barcas. */
    public static BlockPos mira() {
        return new BlockPos(Pilotes.CX, Pilotes.PASO, Pilotes.CZ);
    }

    /** Donde esta la baliza i (lo que pisa: en el muelle, las tablas; en los islotes, el barro). */
    public static BlockPos baliza(int i) {
        int x = Pilotes.CX + BALIZAS[i][0], z = Pilotes.CZ + BALIZAS[i][1];
        return new BlockPos(x, i == 0 ? Pilotes.PASO + 1 : Pilotes.pie(x, z), z);
    }

    // ------------------------------------------------------------------ cada segundo: ponerlos

    static void tick(ServerLevel m) {
        var srv = m.getServer();
        if (!Morvain.hecho(srv, "corwen")) {
            BlockPos p = sitio();
            if (m.isPositionEntityTicking(p) && alguien(m, p, CERCA)) {
                if (buscarCorwen(m) != null) Morvain.marcarHecho(srv, "corwen");
                else ponerCorwen(m);
            }
        }
        for (int i = 0; i < BALIZAS.length; i++) {
            if (Morvain.hecho(srv, "baliza_" + i)) continue;
            BlockPos q = baliza(i);
            if (m.isPositionEntityTicking(q) && alguien(m, q, CERCA_BALIZA)) {
                if (buscarBaliza(m, i) != null) Morvain.marcarHecho(srv, "baliza_" + i);
                else ponerBaliza(m, i);
            }
        }
    }

    private static boolean alguien(ServerLevel m, BlockPos p, int r) {
        for (ServerPlayer sp : m.players())
            if (!sp.isSpectator() && sp.blockPosition().distSqr(p) < (double) r * r) return true;
        return false;
    }

    /** Pone a Corwen en su caja, al final de la pasarela del santuario, mirando a la laguna. */
    public static Viajero ponerCorwen(ServerLevel m) {
        BlockPos s = sitio();
        m.getChunk(s.getX() >> 4, s.getZ() >> 4);
        int y = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX(), s.getZ());
        if (Math.abs(y - s.getY()) > 2) y = s.getY();
        BlockPos pie = new BlockPos(s.getX(), y, s.getZ());
        despejar(m, pie, 1, 1);
        Viajero v = Viajero.poner(m, pie, mira(), Viajero.Quien.CORWEN);
        if (v == null) return null;
        Morvain.marcarHecho(m.getServer(), "corwen");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Corwen, al final de la pasarela del santuario de los Juncos ({})", pie.toShortString());
        return v;
    }

    /** Lo que crece donde va algo (hierba, helechos, juncos), fuera: de r alrededor y {@code alto} + 1 de alto. */
    private static void despejar(ServerLevel m, BlockPos pie, int r, int alto) {
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++)
                for (int dy = 0; dy <= alto; dy++) {
                    BlockPos q = pie.offset(dx, dy, dz);
                    BlockState b = m.getBlockState(q);
                    if (!b.isAir() && (b.canBeReplaced() || b.is(Blocks.SUGAR_CANE) || b.is(Blocks.DEAD_BUSH)) && b.getFluidState().isEmpty())
                        m.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
                }
    }

    /** Pone la baliza i (0 el muelle, 1 el islote del oeste, 2 el de la capilla), con el farol hacia la laguna. */
    public static Baliza ponerBaliza(ServerLevel m, int i) {
        BlockPos p = baliza(i);
        m.getChunk(p.getX() >> 4, p.getZ() >> 4);
        double y = sueloEn(m, p);
        // los juncos y la hierba de alrededor (los juncos suben hasta tres)
        despejar(m, new BlockPos(p.getX(), (int) Math.floor(y), p.getZ()), 0, 3);
        Baliza b = DarkCraftMod.BALIZA.get().create(m);
        if (b == null) return null;
        b.variante(i);
        double dx = Pilotes.CX - p.getX(), dz = Pilotes.CZ - p.getZ();
        float giro = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        b.moveTo(p.getX() + 0.5, y, p.getZ() + 0.5, giro, 0f);
        b.addTag("darkcraft_baliza_" + i);
        m.addFreshEntity(b);
        Morvain.marcarHecho(m.getServer(), "baliza_" + i);
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: la baliza {} de los Pilotes ({}, {})", i, new BlockPos(p.getX(), (int) Math.floor(y), p.getZ()).toShortString(),
                String.format(java.util.Locale.ROOT, "%.0f", giro));
        return b;
    }

    /** Lo alto de lo que se pisa en esa columna, cerca de p (lo que crece no cuenta: no tiene choque). */
    private static double sueloEn(ServerLevel m, BlockPos p) {
        for (int dy = 1; dy >= -3; dy--) {
            BlockPos q = p.above(dy);
            var s = m.getBlockState(q).getCollisionShape(m, q);
            if (!s.isEmpty()) return q.getY() + s.max(net.minecraft.core.Direction.Axis.Y);
        }
        return p.getY();
    }

    /** Corwen, si su chunk esta cargado. */
    public static Viajero buscarCorwen(ServerLevel m) {
        BlockPos s = sitio();
        if (!m.isLoaded(s)) return null;
        for (Viajero v : m.getEntitiesOfClass(Viajero.class, new AABB(s).inflate(6, 20, 6)))
            if (v.quien() == Viajero.Quien.CORWEN) return v;
        return null;
    }

    /** La baliza i, si su chunk esta cargado. */
    public static Baliza buscarBaliza(ServerLevel m, int i) {
        BlockPos p = baliza(i);
        if (!m.isLoaded(p)) return null;
        for (Baliza b : m.getEntitiesOfClass(Baliza.class, new AABB(p).inflate(3, 8, 3))) if (b.variante() == i) return b;
        return null;
    }

    /** Para las pruebas y el comando: quita a Corwen y las balizas (las cargadas) y olvida que se pusieron. */
    public static void olvidar(ServerLevel m) {
        Viajero v = buscarCorwen(m);
        if (v != null) v.discard();
        for (int i = 0; i < BALIZAS.length; i++) {
            Baliza b = buscarBaliza(m, i);
            if (b != null) b.discard();
            Morvain.olvidarHecho(m.getServer(), "baliza_" + i);
        }
        Morvain.olvidarHecho(m.getServer(), "corwen");
    }

    // ------------------------------------------------------------------ lo de cada jugador

    public static int marcas(ServerPlayer sp) {
        return Ficha.mision(sp, ID);
    }

    /** Si lleva el yesquero de Corwen. */
    public static boolean lleva(ServerPlayer sp) {
        return sp.getInventory().countItem(DarkCraftMod.YESQUERO.get()) > 0;
    }

    /** Cuantas balizas lleva encendidas (0-3). */
    public static int encendidas(int k) {
        return Integer.bitCount(k & TODAS);
    }

    /** Si a este jugador le toca ver a Corwen: hasta que le deja ir a recibir las barcas. */
    public static boolean visible(ServerPlayer sp) {
        return (marcas(sp) & IDA) == 0;
    }

    /** Adonde tiene que ir ahora (la brujula y el mapa): con la mision, a la baliza apagada mas cercana; con las tres, a Corwen. */
    public static BlockPos objetivo(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & ACABADA) != 0 || (k & ACEPTADA) == 0) return null;
        if ((k & TODAS) == TODAS) return sitio();
        BlockPos mejor = null;
        double d = Double.MAX_VALUE;
        for (int i = 0; i < BALIZAS.length; i++) {
            if ((k & (B1 << i)) != 0) continue;
            BlockPos q = baliza(i);
            double e = sp.blockPosition().distSqr(q);
            if (e < d) {
                d = e;
                mejor = q;
            }
        }
        return mejor;
    }

    /** Por donde empieza a hablar (y lo apunta). */
    public static int inicio(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & QUEDA) != 0) return 10;
        if ((k & ACEPTADA) != 0 && (k & TODAS) == TODAS) return 7;
        if ((k & ACEPTADA) != 0) {
            // si perdio el yesquero, otro
            if (!lleva(sp)) Equipo.dar(sp, new ItemStack(DarkCraftMod.YESQUERO.get()));
            return 5;
        }
        if ((k & CONOCIDA) != 0) return 6;
        Ficha.mision(sp, ID, k | CONOCIDA);
        Misiones.enviar(sp);
        return 1;
    }

    /** Lo que se le responde (lo manda el cliente al elegirlo): encenderlas, aquella noche, ahora no, que vaya o que se quede. */
    public static void accion(ServerPlayer sp, Viajero v, String accion) {
        int k = marcas(sp);
        if ((k & ACABADA) != 0) return;
        switch (accion) {
            case "aceptar" -> {
                if ((k & ACEPTADA) != 0) return;
                Ficha.mision(sp, ID, (k | ACEPTADA | CONOCIDA) & ~RECHAZADA);
                if (!lleva(sp)) Equipo.dar(sp, new ItemStack(DarkCraftMod.YESQUERO.get()));
                Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.corwen.titulo", 0));
                DarkCraftMod.LOGGER.info("[DarkCraft] {} acepta la mision de Corwen (las balizas de la niebla) y se lleva su yesquero",
                        sp.getGameProfile().getName());
            }
            case "precio" -> Ficha.mision(sp, ID, k | NOCHE | CONOCIDA);
            case "rechazar" -> Ficha.mision(sp, ID, k | RECHAZADA | CONOCIDA);
            case "devolver" -> ir(sp, v, k);
            case "quedar" -> quedar(sp, v, k);
            default -> {
                return;
            }
        }
        Misiones.enviar(sp);
    }

    /** Que vaya a recibir las barcas: se mete en el agua y se pierde en la niebla; te deja su farol. */
    private static void ir(ServerPlayer sp, Viajero v, int k) {
        if ((k & TODAS) != TODAS) return;
        quitarYesquero(sp);
        Ficha.mision(sp, ID, k | IDA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.TALISMANES.get(Talisman.Clase.FAROLERO).get()));
        Logros.dar(sp, "corwen_barcas");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.corwen.titulo", 1));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} deja que Corwen baje al agua a recibir las barcas", sp.getGameProfile().getName());
    }

    /** Que se quede a guardar la luz: te da su pertiga y se queda con su farol encendido. */
    private static void quedar(ServerPlayer sp, Viajero v, int k) {
        if ((k & TODAS) != TODAS) return;
        quitarYesquero(sp);
        Ficha.mision(sp, ID, k | QUEDA | CONOCIDA);
        Equipo.dar(sp, new ItemStack(DarkCraftMod.PERTIGA_FAROLERO.get()));
        Logros.dar(sp, "corwen_luz");
        Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.corwen.titulo", 1));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} le pide a Corwen que se quede a guardar la luz", sp.getGameProfile().getName());
    }

    private static void quitarYesquero(ServerPlayer sp) {
        Inventory inv = sp.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++)
            if (inv.getItem(i).is(DarkCraftMod.YESQUERO.get())) inv.setItem(i, ItemStack.EMPTY);
        inv.setChanged();
    }

    // ------------------------------------------------------------------ las balizas

    /**
     * Encender una baliza (clic derecho o la E): con la mision en marcha y el yesquero, la suya; el sonido, las chispas y las llamas (solo
     * para el) y el aviso de cuantas lleva. Con las tres, las campanas de las barcas en la niebla (desde la laguna) y el logro.
     */
    public static void encender(ServerPlayer sp, Baliza b) {
        if (sp.isSpectator() || sp.distanceToSqr(b) > 36) return;
        int k = marcas(sp);
        int bit = B1 << b.variante();
        if ((k & ACABADA) != 0 || (k & bit) != 0) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.mision.corwen.ya", 0));
            return;
        }
        if ((k & ACEPTADA) == 0 || !lleva(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.mision.corwen.apagada", 0));
            return;
        }
        k |= bit;
        Ficha.mision(sp, ID, k);
        ENCENDIDAS++;
        ServerLevel m = sp.serverLevel();
        double fx = b.farolX(), fy = b.getY() + Baliza.ALTO_FAROL, fz = b.farolZ();
        m.playSound(null, fx, fy, fz, DarkCraftMod.SFX_BALIZA_ENCIENDE.get(), SoundSource.PLAYERS, 1.0f, 1f);
        m.sendParticles(sp, ParticleTypes.LAVA, true, fx, fy, fz, 6, 0.15, 0.1, 0.15, 0.0);
        m.sendParticles(sp, ParticleTypes.FLAME, true, fx, fy, fz, 14, 0.12, 0.18, 0.12, 0.02);
        m.sendParticles(sp, ParticleTypes.LARGE_SMOKE, true, fx, fy + 0.4, fz, 5, 0.1, 0.1, 0.1, 0.01);
        int n = encendidas(k);
        if ((k & TODAS) == TODAS) {
            // las campanas de las barcas, lejos en la niebla de la laguna (solo para el)
            sp.connection.send(new ClientboundSoundPacket(ForgeRegistries.SOUND_EVENTS.getHolder(DarkCraftMod.SFX_CAMPANAS_NIEBLA.get()).orElseThrow(),
                    SoundSource.AMBIENT, Pilotes.CX + 0.5, Pilotes.PASO + 4, Pilotes.CZ + 0.5, 3.0f, 1f, m.getRandom().nextLong()));
            Logros.dar(sp, "balizas");
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.mision.corwen.todas", 0));
        } else Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.mision.corwen.encendida", n));
        Misiones.enviar(sp);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} enciende la baliza {} de los Pilotes ({} de 3)", sp.getGameProfile().getName(), b.variante(), n);
    }
}
