package com.darkcraft.pueblo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Ahogado;
import com.darkcraft.enemigos.Capitan;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Hueco;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Puestos.Puesto;
import com.darkcraft.net.Net;
import com.darkcraft.pueblo.gen.Andar;
import com.darkcraft.pueblo.gen.Faro;
import com.darkcraft.pueblo.gen.PuebloPlan;
import com.darkcraft.pueblo.gen.Recorrido;
import com.darkcraft.pueblo.gen.Terreno;
import com.darkcraft.rol.Frascos;
import com.darkcraft.rol.Rol;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Los que quedan en Brumavela (parte 3c): donde espera cada uno ({@link Puestos}) y lo que guardan.
 * <ul>
 * <li><b>Aldeanos huecos</b> por el pueblo: en las calles, rezando en la capilla, tirados entre las tumbas, en los
 * campos... y los cuatro de la taberna (la tabernera tras la barra): mientras quede uno, <b>el timon no se puede coger</b>.</li>
 * <li><b>Marineros ahogados</b> donde llega la bruma del mar de nubes: el muelle, el almacen de la vela, lo alto de la
 * Cuesta, el Saliente del ancla y el pie del penon.</li>
 * <li><b>El Capitan</b> ante la puerta de su faro, que esta <b>cerrada con una reja</b> mientras siga en pie: dentro esta el
 * corazon del barco. Al caer, la reja se abre; no vuelve (los jefes caen una vez).</li>
 * </ul>
 * En pacifico no hay nadie y nada esta guardado. Nadie espera a menos de 18 bloques de donde se despierta ni cerca del
 * santuario de la plaza.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class PuebloEnemigos {
    private PuebloEnemigos() {}

    public static final String SITIO = "brumavela";
    public static final String TABERNA = "taberna";
    private static final String HUECO = "hueco", AHOGADO = "ahogado", CAPITAN = "capitan";
    // hacia donde miran (el giro de Minecraft: 0 = sur, 90 = oeste, 180 = norte, -90 = este)
    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;
    private static final int DE_PIE = Enemigo.DE_PIE, REZA = Enemigo.REZA, TUMBADO = Enemigo.TUMBADO;

    private static final List<Puesto> PUESTOS = new ArrayList<>();
    /** El puesto del Capitan (su numero en la lista). */
    private static int capitan = -1;

    private static void p(String tipo, int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {
        if (tipo.equals(CAPITAN)) capitan = PUESTOS.size();
        PUESTOS.add(new Puesto(tipo, variante, x, y, z, giro, postura, ronda, grupo));
    }

    static {
        // ---- el muelle: uno de rodillas mirando al mar de nubes (el primero que se ve), y la tripulacion
        p(HUECO, Hueco.HORCA, -24, 251, 116, SUR, REZA, 0, "muelle");
        p(AHOGADO, Ahogado.SABLE, 0, 251, 89, NORTE, DE_PIE, 0, "muelle");          // al pie de la Cuesta, de espaldas
        p(AHOGADO, Ahogado.SABLE, 19, 251, 99, OESTE, DE_PIE, 0, "almacen");        // en el pasillo del almacen, ante el arcon de la vela
        p(AHOGADO, Ahogado.ARPON, 30, 251, 103, OESTE, DE_PIE, 0, "muelle");
        p(AHOGADO, Ahogado.SABLE, -28, 251, 97, ESTE, DE_PIE, 4, "muelle");
        // ---- la Cuesta: uno a media subida y un arponero arriba, bajo el arco
        p(HUECO, Hueco.HORCA, 0, 260, 60, SUR, DE_PIE, 0, "cuesta");
        p(AHOGADO, Ahogado.ARPON, 0, 267, 46, SUR, DE_PIE, 0, "cuesta");
        // ---- La Red Vacia: la tabernera tras la barra y los que miran la jarra sin beber (guardan el timon)
        p(HUECO, Hueco.HOZ, 25, 267, -5, OESTE, DE_PIE, 0, TABERNA);
        p(HUECO, Hueco.HORCA, 18, 267, -9, ESTE, REZA, 0, TABERNA);
        p(HUECO, Hueco.HOZ, 17, 267, -5, ESTE, REZA, 0, TABERNA);
        p(HUECO, Hueco.ANTORCHA, 15, 267, -8, SUR, DE_PIE, 0, TABERNA);
        // ---- la calle del norte
        p(HUECO, Hueco.HOZ, 1, 267, -22, SUR, TUMBADO, 0, "calle");
        p(HUECO, Hueco.ANTORCHA, -2, 267, -40, SUR, DE_PIE, 5, "calle");
        p(HUECO, Hueco.HORCA, 4, 267, -44, SUR, DE_PIE, 0, "calle");
        // ---- la calle del oeste, hacia el Saliente
        p(HUECO, Hueco.HOZ, -34, 267, 6, ESTE, DE_PIE, 0, "calle");
        p(HUECO, Hueco.HORCA, -58, 267, 4, ESTE, DE_PIE, 4, "calle");
        p(HUECO, Hueco.ANTORCHA, -74, 267, 5, ESTE, DE_PIE, 0, "calle");
        p(HUECO, Hueco.HOZ, -71, 267, 8, NORTE, REZA, 0, "calle");
        p(AHOGADO, Ahogado.SABLE, -124, 253, 6, OESTE, DE_PIE, 0, "saliente");      // lo que queda de Tobias, mirando al vacio
        // ---- la calle del este, hasta el mirador
        p(HUECO, Hueco.HORCA, 46, 267, 6, OESTE, DE_PIE, 0, "calle");
        p(HUECO, Hueco.HOZ, 68, 267, 5, OESTE, DE_PIE, 4, "calle");
        p(HUECO, Hueco.HOZ, 86, 267, 6, ESTE, REZA, 0, "mirador");
        // ---- los de las notas: el capitan de puerto ante su mesa y Lena, que guardo la cena
        p(HUECO, Hueco.HOZ, -13, 267, 34, OESTE, REZA, 0, "capitania");
        p(HUECO, Hueco.HOZ, -45, 267, 17, ESTE, REZA, 0, "casa");
        // ---- el cementerio, la capilla y la cripta
        p(HUECO, Hueco.HOZ, -50, 273, -35, SUR, TUMBADO, 0, "cementerio");
        p(HUECO, Hueco.HORCA, -44, 273, -34, OESTE, TUMBADO, 0, "cementerio");
        p(HUECO, Hueco.ANTORCHA, -56, 273, -35, NORTE, REZA, 0, "cementerio");
        p(HUECO, Hueco.HORCA, -51, 273, -46, OESTE, REZA, 0, "capilla");
        p(HUECO, Hueco.HOZ, -49, 273, -45, OESTE, REZA, 0, "capilla");
        p(HUECO, Hueco.ANTORCHA, -40, 273, -46, ESTE, DE_PIE, 0, "capilla");
        p(HUECO, Hueco.HOZ, -52, 268, -44, ESTE, TUMBADO, 0, "cripta");
        p(HUECO, Hueco.HORCA, -50, 268, -48, NORTE, TUMBADO, 0, "cripta");
        // ---- los campos secos
        p(HUECO, Hueco.HORCA, 36, 267, -45, SUR, DE_PIE, 5, "campos");
        p(HUECO, Hueco.HOZ, 57, 267, -43, SUR, DE_PIE, 5, "campos");
        p(HUECO, Hueco.HORCA, 47, 267, -44, OESTE, DE_PIE, 0, "campos");
        // ---- el pie del penon y, arriba, el Capitan ante la puerta del faro
        p(AHOGADO, Ahogado.SABLE, 44, 267, -72, OESTE, DE_PIE, 0, "penon");
        p(AHOGADO, Ahogado.ARPON, 46, 267, -76, OESTE, DE_PIE, 0, "penon");
        p(CAPITAN, 0, Faro.X - 8, Terreno.P + Terreno.PENON_H + 1, Faro.Z, OESTE, DE_PIE, 0, Puestos.JEFE);
    }

    /** Lo apunta el mod al arrancar. */
    public static void registrar() {
        Puestos.tipo(HUECO, DarkCraftMod.HUECO);
        Puestos.tipo(AHOGADO, DarkCraftMod.AHOGADO);
        Puestos.tipo(CAPITAN, DarkCraftMod.CAPITAN);
        Puestos.registrar(SITIO, PUESTOS, Pueblo::mundo);
        Puestos.alCaer(PuebloEnemigos::alCaer);
    }

    public static List<Puesto> puestos() {
        return List.copyOf(PUESTOS);
    }

    public static int puestoCapitan() {
        return capitan;
    }

    // ------------------------------------------------------------------ lo que guardan

    private static boolean pacifico(ServerLevel lvl) {
        return lvl.getDifficulty() == Difficulty.PEACEFUL;
    }

    /** Si aun queda en la taberna alguien que guarde el timon. */
    public static boolean timonGuardado(ServerLevel lvl) {
        return !pacifico(lvl) && Puestos.quedan(lvl.getServer(), SITIO, TABERNA) > 0;
    }

    /** Si el Capitan sigue en pie (el corazon no se puede coger y la puerta del faro esta cerrada). */
    public static boolean capitanEnPie(ServerLevel lvl) {
        return !pacifico(lvl) && !Pueblo.Datos.get(lvl.getServer()).capitan;
    }

    /** El hueco de la puerta del faro (el bloque de abajo; son dos de alto). */
    public static BlockPos puertaFaro() {
        return new BlockPos(Faro.X - 3, PuebloPlan.get().sitio("faro")[1], Faro.Z);
    }

    /** Cada segundo: la reja de la puerta del faro, puesta mientras el Capitan siga en pie. */
    public static void reja(ServerLevel lvl) {
        BlockPos puerta = puertaFaro();
        if (!lvl.hasChunkAt(puerta)) return;
        boolean cerrar = capitanEnPie(lvl);
        for (int dy = 0; dy < 2; dy++) {
            BlockPos b = puerta.above(dy);
            BlockState s = lvl.getBlockState(b);
            if (cerrar && s.isAir())
                lvl.setBlock(b, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true), 3);
            else if (!cerrar && s.is(Blocks.IRON_BARS)) {
                lvl.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
                lvl.sendParticles(ParticleTypes.SOUL, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 10, 0.2, 0.3, 0.3, 0.03);
                if (dy == 0) lvl.playSound(null, b, DarkCraftMod.SFX_FARO_SELLO.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
            }
        }
    }

    /** Si la reja esta puesta ahora mismo (para las pruebas). */
    public static boolean rejaPuesta(ServerLevel lvl) {
        return lvl.getBlockState(puertaFaro()).is(Blocks.IRON_BARS);
    }

    // ------------------------------------------------------------------ cuando cae uno

    /** Lo que queda por repartir cuando el Capitan acaba de caer: (tick del servidor, los que pelearon). */
    private record Reparto(int cuando, Set<UUID> quienes, double x, double y, double z) {}

    private static final List<Reparto> REPARTOS = new ArrayList<>();
    /** Lo que tarda en caer del todo (su animacion de morir) antes del cartel y lo que deja. */
    private static final int ESPERA = 70;

    private static void alCaer(Enemigo e, ServerPlayer quien) {
        if (!(e.level() instanceof ServerLevel lvl)) return;
        if (quien != null && !e.esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, e.tipo());
            // (el logro del primero que se vence es de los de Brumavela; la guardia de Valdrenn tiene los suyos)
            if (e instanceof Hueco || e instanceof com.darkcraft.enemigos.Ahogado) Logros.dar(quien, "hueco");
        }
        if (!(e instanceof Capitan c) || !SITIO.equals(e.sitio())) return;
        Pueblo.Datos d = Pueblo.Datos.get(lvl.getServer());
        d.capitan = true;
        d.cambio();
        Set<UUID> quienes = new HashSet<>(c.rivales());
        if (quien != null) quienes.add(quien.getUUID());
        REPARTOS.add(new Reparto(lvl.getServer().getTickCount() + ESPERA, quienes, e.getX(), e.getY(), e.getZ()));
        DarkCraftMod.LOGGER.info("[DarkCraft] Brumavela: el Capitan ha caido ({} le plantaron cara)", quienes.size());
    }

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || REPARTOS.isEmpty()) return;
        MinecraftServer srv = ev.getServer();
        int ahora = srv.getTickCount();
        for (int i = REPARTOS.size() - 1; i >= 0; i--) {
            Reparto r = REPARTOS.get(i);
            if (ahora < r.cuando()) continue;
            REPARTOS.remove(i);
            repartir(srv, r);
        }
    }

    /** El cartel de JEFE ABATIDO, sus ecos, un frasco mas, su ropa y el logro: a los que pelearon y a los que estaban al lado. */
    private static void repartir(MinecraftServer srv, Reparto r) {
        ServerLevel lvl = srv.overworld();
        for (ServerPlayer p : srv.getPlayerList().getPlayers()) {
            boolean cerca = p.level() == lvl && p.distanceToSqr(r.x(), r.y(), r.z()) < 48 * 48;
            if (!r.quienes().contains(p.getUUID()) && !cerca) continue;
            Rol.jefe(p, "entity.darkcraft.capitan", Capitan.ECOS);
            Logros.dar(p, "capitan");
            com.darkcraft.espejo.Espejo.bestia(p, "capitan");
            if (Frascos.otro(p)) Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.frasco", 0));
            // su ropa (parte 7al; una vez a cada uno)
            com.darkcraft.armadura.Armaduras.premio(p, com.darkcraft.armadura.Conjunto.CAPITAN);
        }
        reja(lvl);
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        REPARTOS.clear();
    }

    /** Para los comandos y las pruebas: da al Capitan por vencido (o lo devuelve a su puerta). */
    public static void capitan(ServerLevel lvl, boolean vencido) {
        Pueblo.Datos d = Pueblo.Datos.get(lvl.getServer());
        d.capitan = vencido;
        d.cambio();
        Puestos.marcar(lvl.getServer(), SITIO, capitan, vencido);
        reja(lvl);
    }

    // ------------------------------------------------------------------ comprobar los puestos (pruebas y diseno)

    /**
     * Que en cada puesto se puede estar de pie y que se llega andando desde donde se despierta (con la cripta abierta),
     * que ninguno queda a menos de 18 bloques de alli ni a menos de 14 del santuario. Una linea de resumen.
     */
    public static String comprobar(Andar.Mundo m) {
        PuebloPlan plan = PuebloPlan.get();
        int[] ini = plan.sitio("inicio"), san = plan.sitio("santuario");
        Andar.Zona zona = Andar.desde(Recorrido.sinLosa(m, plan.capilla.y), ini[0], ini[1], ini[2], -Terreno.LADO, -Terreno.LADO, Terreno.LADO, Terreno.LADO + 20,
                400000);
        int bien = 0;
        StringBuilder mal = new StringBuilder();
        for (int i = 0; i < PUESTOS.size(); i++) {
            Puesto q = PUESTOS.get(i);
            String falla = null;
            // (la zona apunta el bloque que se pisa: uno por debajo de los pies)
            if (!zona.pisa(q.x(), q.y() - 1, q.z())) falla = "no se pisa";
            else if (!libre(m, q.x(), q.y(), q.z()) || !libre(m, q.x(), q.y() + 1, q.z())) falla = "no cabe";
            else if (Math.hypot(q.x() - ini[0], q.z() - ini[2]) < 18) falla = "muy cerca de donde se despierta";
            else if (Math.hypot(q.x() - san[0], q.z() - san[2]) < 14) falla = "muy cerca del santuario";
            if (falla == null) bien++;
            else mal.append(' ').append(i).append(' ').append(q.tipo()).append(" en ").append(q.x()).append(',').append(q.y()).append(',').append(q.z())
                    .append(" (").append(falla).append(");");
        }
        return bien + " de " + PUESTOS.size() + " bien" + (mal.length() > 0 ? " | MAL:" + mal : "");
    }

    private static boolean libre(Andar.Mundo m, int x, int y, int z) {
        BlockState s = m.en(x, y, z);
        if (s.isAir()) return true;
        // (una alfombra no estorba)
        var f = s.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        return f.isEmpty() || f.max(net.minecraft.core.Direction.Axis.Y) < 0.2;
    }
}
