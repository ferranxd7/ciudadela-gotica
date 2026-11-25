package com.darkcraft.pueblo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Reparacion;
import com.darkcraft.cine.Shot;
import com.darkcraft.cine.Viaje;
import com.darkcraft.net.Net;
import com.darkcraft.pueblo.gen.Barco;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Los viajes de La Garza Gris entre Brumavela y el muelle de Valdrenn (parte 3d), y su reparacion por etapas.
 * <ul>
 * <li><b>Zarpar</b> (desde el timon): viajan todos los que estan en el barco o en su muelle. Cada uno ve la cinematica
 * ({@link Viaje}: la primera vez que se va a Valdrenn, con la Sombra; despues, la corta); mientras la pantalla esta en
 * negro el barco de bloques se quita de un muelle y, al llegar, se pone en el otro. El barco que vuela solo existe en el
 * cliente de los que viajan.</li>
 * <li><b>Llamar</b> (la campana del muelle donde no esta): el barco acude; quien esta en ese muelle le ve llegar.</li>
 * <li><b>Reparar</b>: los bloques que le faltan al barco roto se van poniendo segun {@link Reparacion#cuando}.</li>
 * </ul>
 * El barco es <b>de todos</b> y solo hay uno: lo que se guarda ({@code Pueblo.Datos}) es donde esta ({@code barcoEn}),
 * donde estan de verdad sus bloques ({@code puesto}, -1 mientras vuela) y de donde falta quitarlo ({@code resto}). Cada
 * tick se pone al dia lo que se pueda (si el servidor se cierra a medio viaje o todos saltan la cinematica, el barco
 * aparece en su destino en cuanto esten cargados sus chunks; lo que guardaban sus barriles viaja con el).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Travesia {
    private Travesia() {}

    private static final TicketType<ChunkPos> TICKET = TicketType.create("darkcraft_barco", Comparator.comparingLong(ChunkPos::toLong), 20 * 120);
    private static final int CALLADO = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    /** A cuanto del palo hay que estar para viajar con el barco (o para ver como llega o como se repara). */
    private static final double CERCA = 40, MIRAN = 56;

    /** El viaje que hay en marcha. */
    private static final class Marcha {
        int tipo, de, a, inicio;
        Viaje.Tiempos t;
        final List<UUID> quien = new ArrayList<>();
    }

    /** Un bloque de la reparacion: cuando (tick desde que empieza) y que. */
    private record Paso(int tick, Garza.Celda celda) {}

    private static Marcha marcha;
    private static List<Paso> obra;
    private static int obraInicio, obraHecho;

    /** Si el barco esta de viaje o reparandose (no se puede zarpar ni llamarle). */
    public static boolean ocupado() {
        return marcha != null || obra != null;
    }

    /** Lo deja todo quieto (al cerrar el servidor o reiniciar la isla): lo que falte se pone al dia en {@link #onTick}. */
    static void parar() {
        marcha = null;
        obra = null;
    }

    /** El amarre donde esta ahora mismo el barco de bloques entero (-1 si esta volando o reparandose). */
    static int enPie(Pueblo.Datos d) {
        if (obra != null) return -1;
        return d.resto >= 0 ? d.resto : d.puesto;
    }

    private static double[] palo(int[] o) {
        return new double[]{Viaje.PX + o[0], Viaje.PY, Viaje.PZ + o[1]};
    }

    private static List<ServerPlayer> junto(ServerLevel lvl, int[] o, double radio) {
        double[] c = palo(o);
        List<ServerPlayer> l = new ArrayList<>();
        for (ServerPlayer q : lvl.players()) {
            if (!q.isAlive() || q.isSpectator() || Cinematics.active(q)) continue;
            double dx = q.getX() - c[0], dz = q.getZ() - c[2];
            if (dx * dx + dz * dz < radio * radio && Math.abs(q.getY() - c[1]) < 48) l.add(q);
        }
        return l;
    }

    private static void billetes(ServerLevel lvl, int[] o, int radio) {
        ChunkPos cp = new ChunkPos(BlockPos.containing(Amarres.CALMA_X + o[0], Viaje.PY, Amarres.CALMA_Z + o[1]));
        lvl.getChunkSource().addRegionTicket(TICKET, cp, radio, cp);
    }

    // ------------------------------------------------------------------ zarpar

    /** Desde el timon: el barco sale hacia el otro muelle con todos los que esten a bordo o en el muelle. */
    public static void zarpar(ServerPlayer p, boolean comoLaPrimera) {
        ServerLevel lvl = p.serverLevel();
        Pueblo.Datos d = Pueblo.Datos.get(p.server);
        if (!d.amarrado() || ocupado() || Cinematics.active(p)) return;
        int de = d.barcoEn, a = 1 - de;
        int[] o = Amarres.de(lvl, de), dd = Amarres.de(lvl, a);
        if (o == null || dd == null) {
            Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.sin_rumbo", 0));
            return;
        }
        List<ServerPlayer> quien = junto(lvl, o, CERCA);
        quien.remove(p);
        quien.add(0, p);
        int tipo = comoLaPrimera || (a == Amarres.VALDRENN && !Logros.tiene(p, "zarpar")) ? Viaje.PRIMERO : Viaje.NORMAL;
        // el primer viaje siempre lleva a Valdrenn (la Sombra habla de lo que se ve al llegar)
        if (tipo == Viaje.PRIMERO && a != Amarres.VALDRENN) tipo = Viaje.NORMAL;
        Viaje.Tiempos T = Viaje.tiempos(tipo);
        // que se vaya cargando (y haciendo, si falta) el otro muelle
        billetes(lvl, dd, 6);
        if (a == Amarres.VALDRENN) Amarres.pedir(lvl);
        // quien lleva el barco, detras de la rueda del timon y mirando a proa
        double[] tim = Viaje.timonel(o);
        p.teleportTo(lvl, tim[0], tim[1], tim[2], 0f, 6f);
        Marcha m = new Marcha();
        m.tipo = tipo;
        m.de = de;
        m.a = a;
        m.t = T;
        m.inicio = p.server.getTickCount();
        double[] v0 = Amarres.vista(a), vista = {v0[0] + dd[0], v0[1], v0[2] + dd[1]};
        double[] seguro = Amarres.seguro(lvl, a);
        for (int i = 0; i < quien.size(); i++) {
            ServerPlayer q = quien.get(i);
            int n = i;
            double[] al = Amarres.alcazar(lvl, a, n), ojos = {al[0], al[1] + 1.62, al[2]};
            List<Shot> planos = Viaje.planos(tipo, o, dd, ojos, vista);
            Cinematics.playViaje(q, tipo, planos, T.total(), Viaje.datos(tipo, o, dd, a == Amarres.VALDRENN),
                    new float[]{T.cambioA, T.salto}, new double[][]{Viaje.flota(o, false), Viaje.flota(dd, true)}, T.cambioC,
                    pl -> Pueblo.Datos.get(pl.server).puesto == a ? Amarres.alcazar(pl.serverLevel(), a, n) : Amarres.seguro(pl.serverLevel(), a),
                    () -> Pueblo.Datos.get(p.server).puesto == a, seguro);
            m.quien.add(q.getUUID());
        }
        marcha = m;
        d.resto = de;
        d.puesto = -1;
        d.barcoEn = a;
        d.cambio();
        DarkCraftMod.LOGGER.info("[DarkCraft] La Garza Gris zarpa de {} hacia {} con {} a bordo (cinematica {})", nombre(de), nombre(a), quien.size(), tipo);
    }

    private static String nombre(int amarre) {
        return amarre == Amarres.VALDRENN ? "Valdrenn" : "Brumavela";
    }

    // ------------------------------------------------------------------ llamar

    /** La campana del muelle {@code aqui}: el barco, que esta en el otro, acude. */
    public static void llamar(ServerPlayer p, int aqui) {
        ServerLevel lvl = p.serverLevel();
        Pueblo.Datos d = Pueblo.Datos.get(p.server);
        if (!d.amarrado() || ocupado() || d.barcoEn == aqui || aqui < 0 || aqui > 1 || Cinematics.active(p)) return;
        int[] o = Amarres.de(lvl, d.barcoEn), dd = Amarres.de(lvl, aqui);
        if (o == null || dd == null || (aqui == Amarres.VALDRENN && !d.muelle)) return;
        BlockPos c = Amarres.campana(lvl, aqui);
        if (c != null) lvl.playSound(null, c, DarkCraftMod.SFX_BARCO_CAMPANA.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
        // el otro muelle tiene que cargarse para quitar de alli el barco
        billetes(lvl, o, 2);
        List<ServerPlayer> quien = junto(lvl, dd, MIRAN);
        if (!quien.contains(p)) quien.add(p);
        Viaje.Tiempos T = Viaje.tiempos(Viaje.LLAMADA);
        Marcha m = new Marcha();
        m.tipo = Viaje.LLAMADA;
        m.de = d.barcoEn;
        m.a = aqui;
        m.t = T;
        m.inicio = p.server.getTickCount();
        List<Shot> planos = Viaje.planos(Viaje.LLAMADA, o, dd, null, null);
        for (ServerPlayer q : quien) {
            // (el cliente avisa cuando acaba de verla: lo de mas es por si no avisa)
            Cinematics.playGuion(q, Viaje.LLAMADA, planos, T.total() + Cinematics.SIN_AVISO, Viaje.datos(Viaje.LLAMADA, o, dd, aqui == Amarres.VALDRENN));
            m.quien.add(q.getUUID());
        }
        marcha = m;
        d.resto = d.barcoEn;
        d.puesto = -1;
        d.barcoEn = aqui;
        d.cambio();
        DarkCraftMod.LOGGER.info("[DarkCraft] {} llama a La Garza Gris desde {}", p.getGameProfile().getName(), nombre(aqui));
    }

    /** Pone el barco (ya reparado) en ese muelle sin viaje ni cinematica: para los comandos y las pruebas. */
    public static void mover(ServerLevel lvl, int a) {
        Pueblo.Datos d = Pueblo.Datos.get(lvl.getServer());
        int[] dd = Amarres.de(lvl, a);
        if (!d.reparado || dd == null || ocupado()) return;
        if (a == Amarres.VALDRENN) Amarres.hacerYa(lvl);
        if (d.amarrado() && d.barcoEn == a) return;
        int de = d.resto >= 0 ? d.resto : d.puesto;
        int[] o = de < 0 ? null : Amarres.de(lvl, de);
        if (o != null) {
            ListTag c = Garza.carga(lvl, o[0], o[1]);
            if (!c.isEmpty()) d.carga = c;
            Garza.dejar(lvl, -1, o[0], o[1], Amarres.cima(lvl, de));
            Pueblo.soltarVistas();
        }
        Garza.dejar(lvl, 1, dd[0], dd[1], Amarres.cima(lvl, a));
        Garza.descargar(lvl, d.carga, dd[0], dd[1]);
        d.carga = new ListTag();
        d.barcoEn = d.puesto = a;
        d.resto = -1;
        d.cambio();
    }

    /** Lleva al jugador al muelle de Valdrenn (haciendolo si falta): para los comandos. */
    public static void irAlMuelle(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        if (Amarres.de(lvl, Amarres.VALDRENN) == null) {
            Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.sin_rumbo", 0));
            return;
        }
        Amarres.hacerYa(lvl);
        double[] s = Amarres.seguro(lvl, Amarres.VALDRENN);
        p.teleportTo(lvl, s[0], s[1] + 0.1, s[2], (float) s[3], (float) s[4]);
    }

    // ------------------------------------------------------------------ reparar

    /** Empieza la reparacion por etapas (el barco roto esta en Brumavela) y su cinematica para los que estan cerca. */
    static void reparar(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        java.util.Map<BlockPos, BlockState> hay = new java.util.HashMap<>();
        for (Garza.Celda c : Garza.plano(false)) hay.put(new BlockPos(c.x(), c.y(), c.z()), c.estado());
        List<Paso> l = new ArrayList<>();
        java.util.Set<BlockPos> vistas = new java.util.HashSet<>();
        for (Garza.Celda c : Garza.plano(true)) {
            BlockPos b = new BlockPos(c.x(), c.y(), c.z());
            vistas.add(b);
            if (hay.get(b) != c.estado()) l.add(new Paso(Math.round(Reparacion.cuando(c.x(), c.y(), c.z(), c.estado()) * 20), c));
        }
        // lo que solo tiene el barco roto (la cadena del ancla colgando): se va y vuelve la nube
        for (Garza.Celda c : Garza.plano(false)) {
            if (vistas.contains(new BlockPos(c.x(), c.y(), c.z()))) continue;
            int cima = com.darkcraft.world.NubesGenerador.cima(c.x(), c.z());
            BlockState va = c.y() > cima ? Blocks.AIR.defaultBlockState()
                    : DarkCraftMod.NUBE.get().defaultBlockState().setValue(com.darkcraft.world.NubeBlock.TONO, com.darkcraft.world.NubeBlock.tono(cima - c.y()));
            l.add(new Paso(Math.round(Reparacion.LIMPIA * 20), new Garza.Celda(c.x(), c.y(), c.z(), va)));
        }
        l.sort(Comparator.comparingInt(Paso::tick));
        obra = l;
        obraHecho = 0;
        obraInicio = p.server.getTickCount();
        List<ServerPlayer> quien = junto(lvl, new int[]{0, 0}, MIRAN);
        if (!quien.contains(p)) quien.add(p);
        List<Shot> planos = Reparacion.planos();
        for (ServerPlayer q : quien) Cinematics.playGuion(q, Reparacion.ID, planos, Reparacion.FIN + 0.6f, new double[0]);
    }

    private static void obraTick(ServerLevel lvl) {
        if (obra == null) return;
        int t = lvl.getServer().getTickCount() - obraInicio;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int polvo = 0;
        while (obraHecho < obra.size() && obra.get(obraHecho).tick() <= t) {
            Garza.Celda c = obra.get(obraHecho++).celda();
            p.set(c.x(), c.y(), c.z());
            BlockState antes = lvl.getBlockState(p);
            if (antes == c.estado()) continue;
            lvl.setBlock(p, c.estado(), CALLADO);
            // un poco de serrin de lo que aparece (o de lo que se va), sin pasarse
            BlockState pinta = c.estado().isAir() || c.estado().getBlock() instanceof com.darkcraft.world.NubeBlock ? antes : c.estado();
            if (polvo++ < 6 && !pinta.isAir() && !(pinta.getBlock() instanceof com.darkcraft.world.NubeBlock) && pinta.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL)
                lvl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, pinta), c.x() + 0.5, c.y() + 0.5, c.z() + 0.5, 5, 0.3, 0.3, 0.3, 0.02);
        }
        if (t == Math.round(Reparacion.PIEZAS * 20)) {
            // la rueda, el ancla y el corazon: chispas doradas donde van
            int[] a = Barco.ancla();
            lvl.sendParticles(ParticleTypes.WAX_ON, Barco.BX + 0.5, Barco.ALCAZAR_Y + 2.5, Barco.TIMON_Z + 0.5, 14, 0.4, 0.4, 0.4, 0.05);
            lvl.sendParticles(ParticleTypes.WAX_ON, a[0] + 0.5, a[1] - 0.5, a[2] + 0.5, 14, 0.4, 0.6, 0.4, 0.05);
            lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, Barco.BX + 0.5, Barco.CUBIERTA + 0.6, Barco.ESCOTILLA_Z + 0.5, 12, 0.3, 0.5, 0.3, 0.02);
        }
        if (t >= Math.round(Reparacion.DESTELLO * 20)) {
            // por si algo no hubiera quedado en su sitio, el barco entero de una vez; y la fanfarria
            Pueblo.levantar(lvl, true);
            obra = null;
            Pueblo.Datos d = Pueblo.Datos.get(lvl.getServer());
            d.obra = false;
            d.cambio();
            Pueblo.reparado(lvl);
        }
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer srv = e.getServer();
        ServerLevel lvl = srv.overworld();
        if (!Pueblo.mundo(lvl)) return;
        Amarres.tick(lvl);
        obraTick(lvl);
        Pueblo.Datos d = Pueblo.Datos.get(srv);
        if (!d.reparado || obra != null) return;
        if (d.obra) {
            // la reparacion se quedo a medias (se cerro el servidor): el barco entero de una vez, en cuanto se pueda
            if (!Garza.cargado(lvl, 0, 0)) return;
            Pueblo.levantar(lvl, true);
            d.obra = false;
            d.cambio();
            DarkCraftMod.LOGGER.info("[DarkCraft] La Garza Gris se habia quedado a medio reparar: terminada");
        }
        if (marcha == null && d.amarrado() && d.carga.isEmpty()) {
            asegurar(lvl, d);
            return;
        }
        float t = marcha == null ? Float.MAX_VALUE : (srv.getTickCount() - marcha.inicio) / 20f;
        // los que siguen viendo la cinematica, y por que corte va el que va mas atrasado: el mundo se cambia cuando TODOS
        // dicen que ya tienen la pantalla en negro (o, si alguno no avisa, un rato despues de la hora)
        boolean mirando = false;
        int corte = 99;
        if (marcha != null) {
            for (UUID id : marcha.quien) {
                ServerPlayer q = srv.getPlayerList().getPlayer(id);
                if (q == null || Cinematics.viendo(q) != marcha.tipo) continue;
                mirando = true;
                corte = Math.min(corte, Cinematics.fase(q));
            }
            // mientras dure el viaje, que el muelle de Valdrenn se haga si falta
            if (Amarres.falta(lvl)) Amarres.pedir(lvl);
        }
        // 1. quitar el barco del muelle del que se ha ido (en la llamada, enseguida: alli no mira nadie)
        if (d.resto >= 0 && (!mirando || marcha.t.cambioA < 0 || corte >= 1 || t >= marcha.t.cambioA + Cinematics.ESPERA)) {
            int[] o = Amarres.de(lvl, d.resto);
            if (o == null) d.resto = -1;
            else if (Garza.cargado(lvl, o[0], o[1])) {
                ListTag c = Garza.carga(lvl, o[0], o[1]);
                if (!c.isEmpty()) d.carga = c;
                Garza.dejar(lvl, -1, o[0], o[1], Amarres.cima(lvl, d.resto));
                Pueblo.soltarVistas();
                despedida(lvl, o);
                d.resto = -1;
                d.setDirty();
            }
        }
        // 2. ponerlo en el muelle al que llega
        if (d.puesto < 0 && (!mirando || corte >= 3 || t >= marcha.t.cambioC + Cinematics.ESPERA)) {
            int[] o = Amarres.de(lvl, d.barcoEn);
            if (o == null) {
                // no hay rumbo (no deberia pasar): se queda en Brumavela
                d.barcoEn = Amarres.BRUMAVELA;
                d.setDirty();
            } else if ((d.barcoEn == Amarres.BRUMAVELA || d.muelle) && Garza.cargado(lvl, o[0], o[1])) {
                Garza.dejar(lvl, 1, o[0], o[1], Amarres.cima(lvl, d.barcoEn));
                d.puesto = d.barcoEn;
                d.cambio();
                // (los que vienen a bordo, o lo estan viendo llegar, lo oyen en su cinematica)
                oyen(lvl, o, DarkCraftMod.SFX_BARCO_ATRACA.get());
                DarkCraftMod.LOGGER.info("[DarkCraft] La Garza Gris, amarrada en {}", nombre(d.barcoEn));
            } else if (d.barcoEn == Amarres.VALDRENN && Amarres.falta(lvl)) Amarres.pedir(lvl);
        }
        // 3. lo que guardaban sus barriles
        if (d.amarrado() && !d.carga.isEmpty()) {
            int[] o = Amarres.de(lvl, d.puesto);
            if (o != null && Garza.cargado(lvl, o[0], o[1])) {
                Garza.descargar(lvl, d.carga, o[0], o[1]);
                d.carga = new ListTag();
                d.setDirty();
            }
        }
        // 4. se acaba el viaje cuando el barco esta en su sitio y ya nadie mira la cinematica: el logro de los que han llegado
        if (marcha != null && !mirando && d.puesto >= 0 && d.resto < 0) {
            if (d.puesto == marcha.a && marcha.tipo != Viaje.LLAMADA && marcha.a == Amarres.VALDRENN)
                for (UUID id : marcha.quien) {
                    ServerPlayer q = srv.getPlayerList().getPlayer(id);
                    if (q != null) Logros.dar(q, "zarpar");
                }
            marcha = null;
            d.cambio();
        }
    }

    private static int vigilado = -1;

    /**
     * Una vez por sesion (y por muelle), con el barco amarrado y sus chunks cargados: se remienda lo que le falte (lo
     * que le haya quemado un rayo o le haya roto alguien). Lo que los jugadores hayan puesto en sus huecos no se toca.
     */
    private static void asegurar(ServerLevel lvl, Pueblo.Datos d) {
        if (vigilado == d.puesto || lvl.getServer().getTickCount() % 40 != 7) return;
        int[] o = Amarres.de(lvl, d.puesto);
        if (o == null || !Garza.cargado(lvl, o[0], o[1])) return;
        vigilado = d.puesto;
        int mal = Garza.remendar(lvl, o[0], o[1]);
        if (mal > 0) DarkCraftMod.LOGGER.info("[DarkCraft] La Garza Gris no estaba entera en {} ({} bloques): remendada", nombre(d.puesto), mal);
    }

    /** Un sonido del barco en ese muelle, para los que estan cerca y no lo estan viendo en una cinematica (que ya lo oyen). */
    private static void oyen(ServerLevel lvl, int[] o, net.minecraft.sounds.SoundEvent que) {
        double[] c = palo(o);
        for (ServerPlayer q : lvl.players()) {
            if (Cinematics.active(q) || q.distanceToSqr(c[0], c[1], c[2]) > 80 * 80) continue;
            q.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(que), SoundSource.BLOCKS, c[0], c[1], c[2], 2.5f, 1.0f, lvl.random.nextLong()));
        }
    }

    /** El barco se va de ese muelle: una nube que se abre y las velas, para los que se quedan mirando. */
    private static void despedida(ServerLevel lvl, int[] o) {
        double[] c = palo(o);
        vigilado = -1;
        oyen(lvl, o, DarkCraftMod.SFX_BARCO_VELAS.get());
        lvl.sendParticles(ParticleTypes.CLOUD, c[0], c[1] - 2, c[2], 80, 3.5, 1.2, 12, 0.02);
        lvl.sendParticles(ParticleTypes.END_ROD, c[0], c[1] + 4, c[2], 24, 2.5, 4, 8, 0.02);
    }
}
