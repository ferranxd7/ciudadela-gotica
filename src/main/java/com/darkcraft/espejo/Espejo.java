package com.darkcraft.espejo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.MapShapes;
import com.darkcraft.gen.TreeTower;
import com.darkcraft.net.Net;
import com.darkcraft.story.Logros;
import com.darkcraft.story.Story;
import com.darkcraft.world.CastleSites;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * El Espejo de la Sombra (servidor). Se despierta la primera vez que un jugador pisa el Reino de Valdrenn y desde
 * entonces lleva lo que se sabe: los lugares, cofres y jefes descubiertos (entre todos los jugadores del mundo: lo
 * que uno ve lo saben todos; los libros de la cronica tambien desvelan sitios), y de cada jugador su coleccion de
 * objetos del mod, su bestiario y su diario. El mapa explorado y las marcas propias son del cliente
 * ({@code client.espejo}).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Espejo {
    private Espejo() {}

    /** Tipos de lugar. */
    public static final int LUGAR = 0, COFRE = 1, JEFE = 2;
    /** Estado de un lugar para el cliente. */
    public static final int CONOCIDO = 1, HECHO = 2;
    /** Iconos (indices del atlas espejo_iconos.png; los mismos que EspejoIconos en el cliente). */
    public static final int I_TORRE = 1, I_CATEDRAL = 2, I_ARBOL = 3, I_FUENTE = 4, I_COFRE = 5, I_CALAVERA = 7,
            I_CORONA = 8, I_ESPADA = 9, I_PUERTA = 10, I_CASA = 11, I_LIBRO = 14, I_ESPADAS = 26, I_PREGUNTA = 27;

    /** Logros del mod en el orden de la historia (los que no esten aqui van al final). */
    public static final String[] ORDEN_LOGROS = {"clase", "encontrar", "libro_sombra", "primera_marca", "capitulo1", "capitulo2",
            "brandt", "rey", "golpe_final", "capitulo3", "capitulo4", "vareth", "arbol", "gavran_guardia", "gavran_fase1", "gavran_corazon",
            "gavran_juicio", "gavran_fase2", "gavran", "capitulo5", "cartografo", "caballero", "caballeros_50", "capitan_elite", "emboscada", "fortaleza", "puente",
            "puerta_hueso", "acertijo", "amuleto", "portal", "capitulo6", "morvain", "silbato", "corcel", "viaje", "fragmento", "mapa_morvain", "velmor", "marchito", "molinera", "arboleda", "ahorcado", "verdugo", "pilotes", "podrido", "barquero", "monasterio", "helado", "abad", "capital", "calcinado", "juramento", "condestable", "academia", "erudito", "observatorio", "rectora", "puerto", "salobre", "faro", "costillar", "ballenero", "libro_perdido",
            "libros_perdidos"};

    /** Un sitio del mapa: id unico ("x,z:nombre"), tipo, icono, posicion, clave del nombre, radio para descubrirlo. */
    public record Lugar(String id, int tipo, int icono, int x, int y, int z, String nombre, int radio, int cap) {}

    private static final Map<String, List<Lugar>> CACHE = new HashMap<>();
    private static final Set<UUID> SUCIOS = new HashSet<>();

    // ------------------------------------------------------------------ datos del mundo (lo que saben todos)

    /** Lo que se sabe en el mundo: reinos visitados y lugares descubiertos. */
    public static final class Mundo extends SavedData {
        final Set<String> sitios = new LinkedHashSet<>();
        final Set<String> descubiertos = new HashSet<>();

        static Mundo get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Mundo::load, Mundo::new, "darkcraft_espejo");
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag s = new ListTag(), d = new ListTag();
            for (String x : sitios) s.add(StringTag.valueOf(x));
            for (String x : descubiertos) d.add(StringTag.valueOf(x));
            tag.put("sitios", s);
            tag.put("descubiertos", d);
            return tag;
        }

        static Mundo load(CompoundTag tag) {
            Mundo m = new Mundo();
            for (Tag t : tag.getList("sitios", Tag.TAG_STRING)) m.sitios.add(t.getAsString());
            for (Tag t : tag.getList("descubiertos", Tag.TAG_STRING)) m.descubiertos.add(t.getAsString());
            return m;
        }

        boolean descubrir(String id) {
            if (!descubiertos.add(id)) return false;
            setDirty();
            return true;
        }
    }

    // ------------------------------------------------------------------ datos de cada jugador

    private static CompoundTag datos(Player p) {
        CompoundTag root = p.getPersistentData();
        CompoundTag pers = root.getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag mine = pers.getCompound("darkcraft");
        CompoundTag e = mine.getCompound("espejo");
        mine.put("espejo", e);
        pers.put("darkcraft", mine);
        root.put(Player.PERSISTED_NBT_TAG, pers);
        return e;
    }

    public static boolean activo(Player p) {
        return datos(p).getBoolean("activo");
    }

    /** Libros perdidos encontrados (bit n = libro n). */
    public static int perdidos(Player p) {
        return datos(p).getInt("perdidos");
    }

    public static void ponerPerdidos(ServerPlayer p, int v) {
        datos(p).putInt("perdidos", v);
        SUCIOS.add(p.getUUID());
    }

    private static Set<String> conjunto(CompoundTag d, String clave) {
        Set<String> s = new LinkedHashSet<>();
        for (Tag t : d.getList(clave, Tag.TAG_STRING)) s.add(t.getAsString());
        return s;
    }

    private static boolean anadir(CompoundTag d, String clave, String v) {
        ListTag l = d.getList(clave, Tag.TAG_STRING);
        for (Tag t : l) if (t.getAsString().equals(v)) return false;
        l.add(StringTag.valueOf(v));
        d.put(clave, l);
        return true;
    }

    /** Apunta una entrada en el diario del jugador (texto darkcraft.diario.CLAVE), con el dia del mundo. */
    public static void diario(ServerPlayer p, String clave) {
        CompoundTag d = datos(p);
        ListTag l = d.getList("diario", Tag.TAG_COMPOUND);
        for (Tag t : l) if (((CompoundTag) t).getString("k").equals(clave)) return;
        CompoundTag e = new CompoundTag();
        e.putInt("d", dia(p));
        e.putString("k", clave);
        l.add(e);
        d.put("diario", l);
        SUCIOS.add(p.getUUID());
    }

    private static int dia(ServerPlayer p) {
        return (int) (p.serverLevel().getDayTime() / 24000L) + 1;
    }

    // ------------------------------------------------------------------ los lugares de cada reino

    private static String claveSitio(int sx, int sz) {
        return sx + "," + sz;
    }

    private static int icono(String label) {
        return switch (label) {
            case "torre_ascenso", "torreon", "alquimista", "observatorio" -> I_TORRE;
            case "puerta_exterior", "puerta_interior" -> I_PUERTA;
            case "fuente" -> I_FUENTE;
            case "arbol", "jardin" -> I_ARBOL;
            case "catedral" -> I_CATEDRAL;
            case "biblioteca" -> I_LIBRO;
            case "cementerio" -> I_CALAVERA;
            case "laberinto" -> I_PREGUNTA;
            case "patio_armas" -> I_ESPADAS;
            case "pueblo" -> I_CASA;
            default -> I_TORRE;
        };
    }

    /** Los lugares, cofres y jefes del reino que esta en (sx, sz). */
    public static List<Lugar> lugares(MinecraftServer srv, int sx, int sz) {
        String k = claveSitio(sx, sz);
        synchronized (CACHE) {
            List<Lugar> c = CACHE.get(k);
            if (c != null) return c;
        }
        List<Lugar> out = new ArrayList<>();
        try {
            CastlePlan plan = Story.planAt(srv.overworld(), sx, sz);
            MapShapes.Data d = MapShapes.build(plan);
            int y = CastlePlan.Y0;
            for (int i = 0; i < d.labels().length; i++) {
                String l = d.labels()[i];
                out.add(new Lugar(k + ":" + l, LUGAR, icono(l), plan.cx + d.labelPos()[i * 2], y, plan.cz + d.labelPos()[i * 2 + 1],
                        "darkcraft.mapa.lugar." + l, 40, 0));
            }
            for (int ch = 1; ch <= Story.CHAPTERS; ch++) {
                int[] s = plan.storySpot(ch);
                if (s != null) out.add(new Lugar(k + ":cofre" + ch, COFRE, I_COFRE, s[0], s[1], s[2], "darkcraft.espejo.cofre", 14, ch));
            }
            CastlePlan.Throne t = plan.throne();
            if (t != null) out.add(new Lugar(k + ":rey", JEFE, I_CORONA, t.seatX(), t.seatY(), t.seatZ(), "entity.darkcraft.rey", 30, 0));
            int[] h = plan.treeHall();
            if (h != null) out.add(new Lugar(k + ":gavran", JEFE, I_ESPADA, h[0], h[2], h[1] - TreeTower.HALL_R + 3,
                    "entity.darkcraft.gavran", 36, 0));
            // su Fortaleza de Hueso, a 2500 bloques (no cuenta para el cartografo del reino)
            int[] fo = com.darkcraft.fortaleza.Fortalezas.de(srv.overworld().getSeed(), sx, sz);
            com.darkcraft.gen.FortalezaPlan fp = com.darkcraft.fortaleza.Fortalezas.plan(srv.overworld().getSeed(), fo[0], fo[1]);
            int fy = fp.Y;
            out.add(new Lugar(k + ":fortaleza", LUGAR, I_CALAVERA, fp.cx, fy, fp.cz, "darkcraft.mapa.lugar.fortaleza", 150, 0));
            out.add(new Lugar(k + ":fortaleza_puerta", LUGAR, I_PUERTA, fp.cx, fy, fp.cz + com.darkcraft.gen.TorreCupula.PFUERA + 3,
                    "darkcraft.mapa.lugar.fortaleza_puerta", 28, 0));
            int[] al = fp.f.altar();
            out.add(new Lugar(k + ":fortaleza_portal", LUGAR, I_PREGUNTA, al[0], al[1], al[2], "darkcraft.mapa.lugar.fortaleza_portal", 22, 0));
            // la montana del sur: el pie de su sendero (donde llega el camino de nubes) y el mirador del arco de la llave
            int[] pie = com.darkcraft.fortaleza.Puente.pie(fp);
            out.add(new Lugar(k + ":fortaleza_montana", LUGAR, I_TORRE, pie[0], com.darkcraft.gen.Montana.NUBES, pie[1],
                    "darkcraft.mapa.lugar.fortaleza_montana", 60, 0));
            int[] ar = com.darkcraft.fortaleza.Puente.arco(fp);
            out.add(new Lugar(k + ":fortaleza_mirador", LUGAR, I_PUERTA, ar[0], ar[1], ar[2], "darkcraft.mapa.lugar.fortaleza_mirador", 30, 0));
        } catch (RuntimeException e) {
            DarkCraftMod.LOGGER.warn("Espejo: no se pudieron sacar los lugares de {}: {}", k, e.toString());
        }
        synchronized (CACHE) {
            CACHE.put(k, out);
        }
        return out;
    }

    private static Lugar buscar(MinecraftServer srv, String sitio, String sufijo) {
        String[] p = sitio.split(",");
        for (Lugar l : lugares(srv, Integer.parseInt(p[0]), Integer.parseInt(p[1]))) if (l.id().equals(sitio + ":" + sufijo)) return l;
        return null;
    }

    // ------------------------------------------------------------------ despertar

    /** Desde Storm: el jugador esta pisando la isla del reino (sx, sz). */
    public static void enIsla(ServerPlayer p, int sx, int sz) {
        Mundo m = Mundo.get(p.server);
        if (m.sitios.add(claveSitio(sx, sz))) {
            m.setDirty();
            todosSucios(p.server);
        }
        if (!activo(p)) activar(p, sx, sz);
    }

    private static void activar(ServerPlayer p, int sx, int sz) {
        CompoundTag d = datos(p);
        d.putBoolean("activo", true);
        Mundo m = Mundo.get(p.server);
        String sitio = claveSitio(sx, sz);
        if (m.sitios.add(sitio)) m.setDirty();
        // el espejo ya sabe donde empieza la cronica
        if (m.descubrir(sitio + ":cofre1")) todosSucios(p.server);
        diario(p, "espejo");
        // lo que ya se hubiera hecho antes de tener el espejo (mundos de antes de esta version)
        for (String id : ORDEN_LOGROS) if (Logros.tiene(p, id)) logroHecho(p, id, false);
        int leidos = Story.read(p);
        for (int ch = 1; ch <= Story.CHAPTERS; ch++) if ((leidos & (1 << ch)) != 0) desvelar(p, ch);
        SUCIOS.add(p.getUUID());
        enviar(p, true);
    }

    // ------------------------------------------------------------------ lo que pasa en el juego

    /** Desde Logros.dar: se acaba de conseguir un logro del mod. */
    public static void logro(ServerPlayer p, String id) {
        if (!activo(p) && !id.equals(Logros.ENCONTRAR)) return;
        logroHecho(p, id, true);
        SUCIOS.add(p.getUUID());
    }

    private static void logroHecho(ServerPlayer p, String id, boolean ahora) {
        if (!id.equals(Logros.ENCONTRAR) && !id.equals("libro_sombra") && !id.equals("primera_marca")) diario(p, id);
        if (id.equals(Logros.REY)) anadir(datos(p), "bestiario", "rey");
        if (id.equals(Logros.GAVRAN)) anadir(datos(p), "bestiario", "gavran");
    }

    /** Una criatura vencida para el bestiario (los caballeros zombi lo llaman al morir). */
    public static void bestia(ServerPlayer p, String clave) {
        if (anadir(datos(p), "bestiario", clave)) SUCIOS.add(p.getUUID());
    }

    /** Desde StoryEvents: el jugador ha leido el capitulo ch (los libros desvelan lo siguiente). */
    public static void leido(ServerPlayer p, int ch) {
        if (!activo(p)) return;
        desvelar(p, ch);
        SUCIOS.add(p.getUUID());
    }

    private static void desvelar(ServerPlayer p, int ch) {
        MinecraftServer srv = p.server;
        Mundo m = Mundo.get(srv);
        String sitio = sitioCercano(p);
        if (sitio == null) return;
        List<String> ids = new ArrayList<>();
        ids.add(sitio + ":cofre" + ch);
        switch (ch) {
            case 1 -> ids.add(sitio + ":cofre2");
            case 2 -> {
                ids.add(sitio + ":rey");
                ids.add(sitio + ":cofre3");
            }
            case 3 -> ids.add(sitio + ":cofre4");
            case 4 -> {
                ids.add(sitio + ":gavran");
                ids.add(sitio + ":cofre5");
            }
            default -> {
            }
        }
        boolean cambio = false;
        for (String id : ids) cambio |= m.descubrir(id);
        if (cambio) todosSucios(srv);
    }

    /** El reino visitado mas cercano al jugador (clave "x,z") o null. */
    private static String sitioCercano(ServerPlayer p) {
        Mundo m = Mundo.get(p.server);
        String mejor = null;
        double md = Double.MAX_VALUE;
        for (String s : m.sitios) {
            String[] q = s.split(",");
            double d = Math.hypot(Integer.parseInt(q[0]) - p.getX(), Integer.parseInt(q[1]) - p.getZ());
            if (d < md) {
                md = d;
                mejor = s;
            }
        }
        if (mejor == null) {
            int[] s = CastleSites.nearest(p.serverLevel().getSeed(), p.getBlockX(), p.getBlockZ(), 3);
            if (s != null) mejor = claveSitio(s[0], s[1]);
        }
        return mejor;
    }

    private static void todosSucios(MinecraftServer srv) {
        for (ServerPlayer q : srv.getPlayerList().getPlayers()) SUCIOS.add(q.getUUID());
    }

    /** Desde el cliente: ha abierto el libro (0) o ha escrito una marca (1). */
    public static void accion(ServerPlayer p, int tipo) {
        if (!activo(p)) return;
        if (tipo == 0) Logros.dar(p, "libro_sombra");
        else if (tipo == 1) Logros.dar(p, "primera_marca");
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (!activo(p) && Logros.tiene(p, Logros.ENCONTRAR)) {
            // mundos de antes: el jugador ya habia pisado Valdrenn
            String s = sitioCercano(p);
            if (s != null) {
                String[] q = s.split(",");
                activar(p, Integer.parseInt(q[0]), Integer.parseInt(q[1]));
                return;
            }
        }
        SUCIOS.add(p.getUUID());
        enviar(p, false);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        SUCIOS.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity().level().isClientSide()) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer p)) return;
        if (e.getEntity().getTags().contains(com.darkcraft.gavran.GavranEventos.TAG_ESCUDERO)) {
            if (anadir(datos(p), "bestiario", "escudero")) SUCIOS.add(p.getUUID());
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 20 != 7) return;
        if (!activo(p)) {
            // dormido: solo lo que necesita el menu (logros, diario, libros), cuando cambia
            if (SUCIOS.remove(p.getUUID())) enviar(p, false);
            return;
        }
        descubrir(p);
        if (p.tickCount % 40 == 7) coleccion(p);
        String lugar = lugarActual(p);
        Net.to(p, new Net.EspejoPos(lugar, amigos(p)));
        com.darkcraft.rol.Rol.zona(p, lugar);
        if (SUCIOS.remove(p.getUUID())) enviar(p, false);
    }

    /** Los lugares que el jugador tiene cerca pasan a ser conocidos (por todos). */
    private static void descubrir(ServerPlayer p) {
        if (p.level().dimension() != Level.OVERWORLD) return;
        Mundo m = Mundo.get(p.server);
        boolean cambio = false;
        for (String s : m.sitios) {
            String[] q = s.split(",");
            int sx = Integer.parseInt(q[0]), sz = Integer.parseInt(q[1]);
            boolean cercaReino = Math.hypot(sx - p.getX(), sz - p.getZ()) <= 700;
            int[] fo = com.darkcraft.fortaleza.Fortalezas.de(p.serverLevel().getSeed(), sx, sz);
            boolean cercaFortaleza = Math.hypot(fo[0] - p.getX(), fo[1] - p.getZ()) <= 320;
            if (!cercaReino && !cercaFortaleza) continue;
            List<Lugar> ls = lugares(p.server, sx, sz);
            int lugaresTotal = 0, lugaresVistos = 0;
            for (Lugar l : ls) {
                boolean deFortaleza = l.id().contains(":fortaleza");
                if (l.tipo() == LUGAR && !deFortaleza) lugaresTotal++;
                boolean ya = m.descubiertos.contains(l.id());
                if (!ya) {
                    double dh = Math.hypot(l.x() + 0.5 - p.getX(), l.z() + 0.5 - p.getZ());
                    boolean cerca = dh <= l.radio() && (l.tipo() != COFRE || Math.abs(l.y() - p.getY()) < 12);
                    if (cerca) {
                        m.descubrir(l.id());
                        cambio = true;
                        ya = true;
                    }
                }
                if (ya && l.tipo() == LUGAR && !deFortaleza) lugaresVistos++;
            }
            if (cercaReino && lugaresTotal > 0 && lugaresVistos >= lugaresTotal) Logros.dar(p, "cartografo");
        }
        if (cambio) todosSucios(p.server);
    }

    /** Objetos del mod que el jugador ha tenido alguna vez. */
    private static void coleccion(ServerPlayer p) {
        CompoundTag d = datos(p);
        boolean cambio = false;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty()) continue;
            int perdido = com.darkcraft.fortaleza.Perdidos.de(st);
            if (perdido > 0) com.darkcraft.fortaleza.Perdidos.encontrado(p, perdido);
            String k = claveColeccion(st);
            if (k != null && anadir(d, "coleccion", k)) cambio = true;
        }
        if (cambio) SUCIOS.add(p.getUUID());
    }

    /** Clave de la coleccion de un objeto (null si no es del mod). */
    public static String claveColeccion(ItemStack st) {
        if (st.is(Items.WRITTEN_BOOK) && st.hasTag() && st.getTag().contains("darkcraft_libro"))
            return "libro" + st.getTag().getInt("darkcraft_libro");
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(st.getItem());
        if (id != null && id.getNamespace().equals(DarkCraftMod.MODID)) return id.getPath();
        return null;
    }

    /** Nombre del sitio donde esta el jugador (clave de idioma) o "" (el cliente pone el bioma). */
    private static String lugarActual(ServerPlayer p) {
        // en Morvain (parte 7a): su region, o el Arco del Umbral
        if (p.level().dimension() == com.darkcraft.morvain.Morvain.NIVEL) return com.darkcraft.morvain.Morvain.lugar(p);
        if (p.level().dimension() != Level.OVERWORLD) return "";
        String f = com.darkcraft.fortaleza.FortalezaEventos.lugar(p);
        if (f != null) return f;
        // en Brumavela, la isla del pueblo (parte 3b)
        String b = com.darkcraft.pueblo.Pueblo.lugar(p);
        if (b != null) return b;
        // en una parada del camino de nubes (parte 5d)
        String parada = com.darkcraft.camino.Paradas.lugar(p);
        if (parada != null) return parada;
        // de pie en el camino de nubes (el mundo de nubes, parte 3a)
        if (p.onGround() && p.level().getBlockState(p.blockPosition().below()).is(com.darkcraft.DarkCraftMod.NUBE_FIRME.get()))
            return "darkcraft.mapa.lugar.camino_nubes";
        Mundo m = Mundo.get(p.server);
        for (String s : m.sitios) {
            String[] q = s.split(",");
            int sx = Integer.parseInt(q[0]), sz = Integer.parseInt(q[1]);
            double dc = Math.hypot(sx - p.getX(), sz - p.getZ());
            if (dc > CastlePlan.FOOTPRINT + 40) continue;
            if (p.getY() < CastlePlan.Y0 - 60) return "";
            // salas de los jefes primero
            Lugar arbol = buscar(p.server, s, "gavran");
            if (arbol != null && p.getY() > arbol.y() - 8) {
                // la sala del arbol esta muy por encima del castillo: solo cuenta a su altura
                double da = Math.hypot(arbol.x() + 0.5 - p.getX(), (arbol.z() + TreeTower.HALL_R - 3) + 0.5 - p.getZ());
                if (da < TreeTower.HALL_R + 2) return "darkcraft.mapa.lugar.arbol";
            }
            // el salon del trono (en la base de la torre-arbol)
            CastlePlan.Throne t = Story.planAt(p.serverLevel(), sx, sz).throne();
            if (t != null && p.getX() >= t.x0() - 1 && p.getX() <= t.x1() + 2 && p.getZ() >= t.z0() - 1 && p.getZ() <= t.z1() + 6
                    && p.getY() >= t.y() - 2 && p.getY() <= t.y() + t.h() + 2)
                return "darkcraft.mapa.lugar.trono";
            String mejor = null;
            double md = 46;
            for (Lugar l : lugares(p.server, sx, sz)) {
                if (l.tipo() != LUGAR) continue;
                double d = Math.hypot(l.x() + 0.5 - p.getX(), l.z() + 0.5 - p.getZ());
                if (d < md) {
                    md = d;
                    mejor = l.nombre();
                }
            }
            return mejor != null ? mejor : "darkcraft.mapa.titulo";
        }
        return "";
    }

    private static List<Net.Amigo> amigos(ServerPlayer p) {
        List<Net.Amigo> out = new ArrayList<>();
        for (ServerPlayer q : p.server.getPlayerList().getPlayers()) {
            if (q == p || q.level() != p.level() || q.isSpectator()) continue;
            out.add(new Net.Amigo(q.getUUID(), q.getGameProfile().getName(), q.getBlockX(), q.getBlockY(), q.getBlockZ()));
            if (out.size() >= 32) break;
        }
        return out;
    }

    // ------------------------------------------------------------------ lo que se manda al cliente

    public static void enviar(ServerPlayer p, boolean aviso) {
        MinecraftServer srv = p.server;
        Mundo m = Mundo.get(srv);
        CompoundTag d = datos(p);
        int leidos = Story.read(p);
        List<Net.LugarE> ls = new ArrayList<>();
        for (String s : m.sitios) {
            String[] q = s.split(",");
            for (Lugar l : lugares(srv, Integer.parseInt(q[0]), Integer.parseInt(q[1]))) {
                if (!m.descubiertos.contains(l.id())) continue;
                int estado = CONOCIDO;
                if (l.tipo() == COFRE && (leidos & (1 << l.cap())) != 0) estado = HECHO;
                if (l.tipo() == JEFE && l.id().endsWith(":rey") && Logros.tiene(p, Logros.REY)) estado = HECHO;
                if (l.tipo() == JEFE && l.id().endsWith(":gavran") && Logros.tiene(p, Logros.GAVRAN)) estado = HECHO;
                ls.add(new Net.LugarE(l.id(), l.tipo(), l.icono(), l.x(), l.y(), l.z(), l.nombre(), l.cap(), estado));
            }
        }
        List<Net.LogroE> lg = new ArrayList<>();
        List<Advancement> todos = new ArrayList<>();
        for (Advancement a : srv.getAdvancements().getAllAdvancements())
            if (a.getId().getNamespace().equals(DarkCraftMod.MODID) && a.getDisplay() != null && !a.getId().getPath().equals("raiz"))
                todos.add(a);
        todos.sort((a, b) -> Integer.compare(orden(a.getId().getPath()), orden(b.getId().getPath())));
        for (Advancement a : todos) {
            DisplayInfo di = a.getDisplay();
            ResourceLocation icon = ForgeRegistries.ITEMS.getKey(di.getIcon().getItem());
            boolean hecho = p.getAdvancements().getOrStartProgress(a).isDone();
            lg.add(new Net.LogroE(a.getId().getPath(), icon == null ? "minecraft:book" : icon.toString(), di.getFrame().ordinal(),
                    di.isHidden(), hecho, clave(di.getTitle()), clave(di.getDescription())));
        }
        List<Net.DiarioE> di = new ArrayList<>();
        for (Tag t : d.getList("diario", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            di.add(new Net.DiarioE(c.getInt("d"), c.getString("k")));
        }
        // aunque el espejo aun no haya despertado: el menu (R) ensena la historia, el diario y los logros
        Net.to(p, new Net.EspejoEstado(activo(p), aviso, leidos, new ArrayList<>(conjunto(d, "coleccion")),
                new ArrayList<>(conjunto(d, "bestiario")), ls, lg, di, d.getInt("perdidos")));
    }

    private static String clave(net.minecraft.network.chat.Component c) {
        return c.getContents() instanceof TranslatableContents t ? t.getKey() : c.getString();
    }

    private static int orden(String id) {
        for (int i = 0; i < ORDEN_LOGROS.length; i++) if (ORDEN_LOGROS[i].equals(id)) return i;
        return 1000 + id.hashCode() % 100;
    }

    // ------------------------------------------------------------------ comandos (/darkcraft valdrenn espejo ...)

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("espejo")
                .then(net.minecraft.commands.Commands.literal("despertar").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    String s = sitioCercano(p);
                    if (s == null) return 0;
                    String[] q = s.split(",");
                    datos(p).putBoolean("activo", false);
                    activar(p, Integer.parseInt(q[0]), Integer.parseInt(q[1]));
                    return 1;
                }))
                .then(net.minecraft.commands.Commands.literal("dormir").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    CompoundTag d = datos(p);
                    for (String k : List.copyOf(d.getAllKeys())) d.remove(k);
                    Net.to(p, new Net.EspejoEstado(false, false, 0, List.of(), List.of(), List.of(), List.of(), List.of(), 0));
                    return 1;
                }))
                .then(net.minecraft.commands.Commands.literal("revelar").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    Mundo m = Mundo.get(p.server);
                    for (String s : m.sitios) {
                        String[] q = s.split(",");
                        for (Lugar l : lugares(p.server, Integer.parseInt(q[0]), Integer.parseInt(q[1]))) m.descubrir(l.id());
                    }
                    todosSucios(p.server);
                    return 1;
                }));
    }
}
