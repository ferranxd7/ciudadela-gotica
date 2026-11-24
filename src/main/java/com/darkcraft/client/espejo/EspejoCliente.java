package com.darkcraft.client.espejo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * El Espejo de la Sombra en el cliente: lo que manda el servidor, las teclas (M el libro, N el espejo, + y - el
 * zoom), el mapa explorado de la dimension en la que esta el jugador y sus marcas, la planta del piso cuando esta
 * bajo techo y el aviso de cuando despierta.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class EspejoCliente {
    private EspejoCliente() {}

    public static final KeyMapping TECLA_LIBRO = new KeyMapping("key.darkcraft.libro", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.darkcraft");
    public static final KeyMapping TECLA_ESPEJO = new KeyMapping("key.darkcraft.espejo", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.darkcraft");
    public static final KeyMapping TECLA_ACERCAR = new KeyMapping("key.darkcraft.acercar", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, "key.categories.darkcraft");
    public static final KeyMapping TECLA_ALEJAR = new KeyMapping("key.darkcraft.alejar", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, "key.categories.darkcraft");

    /** Radio (en bloques) que se ve en el espejo con cada zoom. */
    public static final int[] ZOOM = {20, 32, 48, 72, 104, 128};
    /** Tamanos del espejo (escala de la textura del marco). */
    public static final float[] TAMANO = {0.27f, 0.34f, 0.43f};

    // lo que manda el servidor
    static boolean activo;
    static Net.EspejoEstado estado;
    static String lugar = "";
    static List<Net.Amigo> amigos = new ArrayList<>();
    static long estadoVersion;

    // opciones (se guardan en opciones.properties)
    static boolean visible = true;
    static int zoom = 2, tamano = 1;

    // mapa y marcas de la dimension actual
    static String mundo;
    static Exploracion ex;
    static Marcas marcas;
    private static final ArrayDeque<Long> COLA = new ArrayDeque<>();
    private static final Set<Long> EN_COLA = new HashSet<>();
    private static int ticks, ultimoGuardado;

    // bajo techo: la planta del piso
    static boolean dentro;
    private static int dentroVotos;

    // aviso de que despierta (en ticks desde que llego)
    static int aviso = -1;
    static int aparecer = 1000;

    public static boolean activo() {
        return activo && estado != null;
    }

    /** Si esta saliendo el aviso de que el espejo despierta (los carteles esperan a que acabe). */
    public static boolean avisando() {
        return aviso >= 0;
    }

    /** El cofre del capitulo que toca (x, z), si el espejo lo conoce: para la brujula. */
    public static int[] objetivo() {
        Minecraft mc = Minecraft.getInstance();
        if (!activo() || mc.level == null || mc.level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return null;
        int siguiente = com.darkcraft.story.Story.next(estado.leidos());
        for (Net.LugarE lu : estado.lugares())
            if (lu.tipo() == com.darkcraft.espejo.Espejo.COFRE && lu.cap() == siguiente && lu.estado() != com.darkcraft.espejo.Espejo.HECHO)
                return new int[]{lu.x(), lu.z()};
        return null;
    }

    /** Para que el HUD y el libro sepan que algo ha cambiado. */
    public static long version() {
        return estadoVersion;
    }

    // ------------------------------------------------------------------ paquetes

    public static void estado(Net.EspejoEstado e) {
        boolean antes = activo;
        estado = e;
        activo = e.activo();
        estadoVersion++;
        if (!activo) return;
        if (e.aviso()) {
            aviso = 0;
            aparecer = 0;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_ESPEJO_DESPIERTA.get(), 1.0f, 0.9f));
        } else if (!antes) aparecer = 1000;
    }

    public static void pos(Net.EspejoPos p) {
        lugar = p.lugar();
        amigos = p.amigos();
    }

    // ------------------------------------------------------------------ opciones

    private static Path opciones() {
        return Exploracion.raiz().resolve("opciones.properties");
    }

    private static boolean opcionesLeidas;

    static void leerOpciones() {
        if (opcionesLeidas) return;
        opcionesLeidas = true;
        Properties p = new Properties();
        try {
            if (Files.isRegularFile(opciones())) p.load(Files.newBufferedReader(opciones(), StandardCharsets.UTF_8));
            visible = !"false".equals(p.getProperty("visible"));
            zoom = Math.max(0, Math.min(ZOOM.length - 1, Integer.parseInt(p.getProperty("zoom", "2"))));
            tamano = Math.max(0, Math.min(TAMANO.length - 1, Integer.parseInt(p.getProperty("tamano", "1"))));
        } catch (IOException | RuntimeException ignored) {
        }
    }

    static void guardarOpciones() {
        Properties p = new Properties();
        p.setProperty("visible", Boolean.toString(visible));
        p.setProperty("zoom", Integer.toString(zoom));
        p.setProperty("tamano", Integer.toString(tamano));
        try {
            Files.createDirectories(opciones().getParent());
            try (var w = Files.newBufferedWriter(opciones(), StandardCharsets.UTF_8)) {
                p.store(w, "Dark Craft - Espejo de la Sombra");
            }
        } catch (IOException ignored) {
        }
    }

    // ------------------------------------------------------------------ teclas

    @Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registro {
        private Registro() {}

        @SubscribeEvent
        public static void teclas(RegisterKeyMappingsEvent e) {
            e.register(TECLA_LIBRO);
            e.register(TECLA_ESPEJO);
            e.register(TECLA_ACERCAR);
            e.register(TECLA_ALEJAR);
        }
    }

    public static void cambiarZoom(int d) {
        int z = Math.max(0, Math.min(ZOOM.length - 1, zoom + d));
        if (z != zoom) {
            zoom = z;
            guardarOpciones();
        }
    }

    /** El + y el - de la distribucion del teclado del jugador (sin tener que cambiar las teclas). */
    @SubscribeEvent
    public static void onKey(InputEvent.Key e) {
        Minecraft mc = Minecraft.getInstance();
        if (e.getAction() != GLFW.GLFW_PRESS || mc.screen != null || mc.player == null || !activo()) return;
        if (TECLA_ACERCAR.matches(e.getKey(), e.getScanCode()) || TECLA_ALEJAR.matches(e.getKey(), e.getScanCode())) return;
        String n;
        try {
            n = GLFW.glfwGetKeyName(e.getKey(), e.getScanCode());
        } catch (RuntimeException ex) {
            return;
        }
        if (n == null) return;
        for (KeyMapping k : mc.options.keyMappings) if (k != TECLA_ACERCAR && k != TECLA_ALEJAR && k.matches(e.getKey(), e.getScanCode())) return;
        if (n.equals("+")) cambiarZoom(-1);
        else if (n.equals("-")) cambiarZoom(1);
    }

    private static void teclas(Minecraft mc) {
        while (TECLA_LIBRO.consumeClick()) {
            if (!activo()) mc.gui.setOverlayMessage(Component.translatable("darkcraft.espejo.dormido"), false);
            else if (mc.screen == null) LibroScreen.abrir();
        }
        while (TECLA_ESPEJO.consumeClick()) {
            if (!activo()) {
                mc.gui.setOverlayMessage(Component.translatable("darkcraft.espejo.dormido"), false);
                continue;
            }
            visible = !visible;
            guardarOpciones();
            mc.gui.setOverlayMessage(Component.translatable(visible ? "darkcraft.espejo.mostrar" : "darkcraft.espejo.ocultar"), false);
        }
        while (TECLA_ACERCAR.consumeClick()) if (activo()) cambiarZoom(-1);
        while (TECLA_ALEJAR.consumeClick()) if (activo()) cambiarZoom(1);
    }

    // ------------------------------------------------------------------ mapa explorado

    static Exploracion exploracion() {
        return ex;
    }

    static Marcas marcas() {
        return marcas;
    }

    private static ClientLevel ultimoNivel;

    private static void dimension(ClientLevel lvl) {
        if (lvl == ultimoNivel && ex != null) return;
        ultimoNivel = lvl;
        String dim = lvl.dimension().location().toString();
        String m = Exploracion.mundoActual();
        EspejoHud.Planta.reiniciar();
        if (ex != null && ex.dim.equals(dim) && m.equals(mundo)) return;
        if (ex != null) ex.guardar();
        mundo = m;
        ex = new Exploracion(m, dim);
        marcas = new Marcas(ex.carpeta());
        COLA.clear();
        EN_COLA.clear();
        // los chunks que ya estan cargados
        var p = Minecraft.getInstance().player;
        if (p != null) {
            ChunkPos c = p.chunkPosition();
            int rd = Minecraft.getInstance().options.getEffectiveRenderDistance();
            for (int r = 0; r <= rd; r++)
                for (int dx = -r; dx <= r; dx++)
                    for (int dz = -r; dz <= r; dz++)
                        if (Math.max(Math.abs(dx), Math.abs(dz)) == r) encolar(c.x + dx, c.z + dz);
        }
    }

    /** Ejemplares de cada dimension: para el selector del libro (abre otra sin cambiar la del jugador). */
    static Exploracion otra(String dim) {
        if (ex != null && ex.dim.equals(dim)) return ex;
        return new Exploracion(mundo == null ? Exploracion.mundoActual() : mundo, dim);
    }

    private static void encolar(int cx, int cz) {
        long k = ChunkPos.asLong(cx, cz);
        if (EN_COLA.add(k)) COLA.add(k);
    }

    @SubscribeEvent
    public static void onChunk(ChunkEvent.Load e) {
        if (!e.getLevel().isClientSide() || !activo()) return;
        encolar(e.getChunk().getPos().x, e.getChunk().getPos().z);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        leerOpciones();
        teclas(mc);
        if (mc.level == null || mc.player == null || !activo()) return;
        ticks++;
        if (aviso >= 0 && ++aviso > 200) aviso = -1;
        if (aparecer < 1000) aparecer++;
        dimension(mc.level);
        // los chunks de alrededor se vuelven a mirar de vez en cuando (por lo que se construye o se rompe)
        ChunkPos c = mc.player.chunkPosition();
        if (ticks % 40 == 0) for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) encolar(c.x + dx, c.z + dz);
        int yj = mc.player.getBlockY();
        for (int n = 0; n < 8 && !COLA.isEmpty(); n++) {
            long k = COLA.poll();
            EN_COLA.remove(k);
            LevelChunk ch = mc.level.getChunkSource().getChunk(ChunkPos.getX(k), ChunkPos.getZ(k), false);
            if (ch != null && !ch.isEmpty()) ex.escanear(mc.level, ch, yj);
        }
        if (ticks - ultimoGuardado > 1200) {
            ultimoGuardado = ticks;
            ex.guardar();
            ex.soltarLejos(mc.player.getBlockX(), mc.player.getBlockZ(), 3);
        }
        if (ticks % 5 == 0) techo(mc);
        if (dentro) EspejoHud.Planta.tick(mc.level, mc.player.blockPosition());
    }

    /** Bajo techo (dentro de un edificio o una cueva): el espejo ensena la planta del piso. */
    private static void techo(Minecraft mc) {
        ClientLevel lvl = mc.level;
        BlockPos p = mc.player.blockPosition();
        int votos = 0;
        int[][] muestras = {{0, 0}, {3, 0}, {-3, 0}, {0, 3}, {0, -3}};
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int[] s : muestras) {
            for (int dy = 2; dy <= 24; dy++) {
                BlockState st = lvl.getBlockState(m.set(p.getX() + s[0], p.getY() + dy, p.getZ() + s[1]));
                if (st.isAir() || st.getBlock() instanceof LeavesBlock || !st.canOcclude()) continue;
                votos++;
                break;
            }
        }
        boolean ahora = votos >= 4 || lvl.dimensionType().hasCeiling();
        if (ahora != dentro) {
            dentroVotos++;
            if (dentroVotos >= 2) {
                dentro = ahora;
                dentroVotos = 0;
            }
        } else dentroVotos = 0;
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        if (ex != null) ex.guardar();
        ex = null;
        marcas = null;
        mundo = null;
        ultimoNivel = null;
        activo = false;
        estado = null;
        lugar = "";
        amigos = new ArrayList<>();
        COLA.clear();
        EN_COLA.clear();
        aviso = -1;
        dentro = false;
        EspejoHud.soltar();
    }

    // ------------------------------------------------------------------ ayudas

    /** Los otros jugadores (y el amigo de mentira de las pruebas de la CI). */
    static List<Net.Amigo> amigos() {
        if (prueba == null) return amigos;
        List<Net.Amigo> l = new ArrayList<>(amigos);
        l.addAll(prueba);
        return l;
    }

    private static List<Net.Amigo> prueba;

    // ------------------------------------------------------------------ para las pruebas de la CI

    public static String resumen() {
        Net.EspejoEstado e = estado;
        return "activo=" + activo + " visible=" + visible + " dentro=" + dentro + " zoom=" + zoom + " tamano=" + tamano + " lugar=" + lugar
                + (e == null ? "" : " lugares=" + e.lugares().size() + " logros=" + e.logros().size() + " diario=" + e.diario().size()
                + " coleccion=" + e.coleccion() + " bestiario=" + e.bestiario() + " leidos=" + e.leidos())
                + " regiones=" + (ex == null ? 0 : ex.cargadas().size()) + " cola=" + COLA.size() + " marcas=" + (marcas == null ? 0 : marcas.lista.size())
                + " mundo=" + mundo + " dim=" + (ex == null ? "-" : ex.dim);
    }

    public static void probar(int zoomNuevo, int tamanoNuevo) {
        zoom = Math.max(0, Math.min(ZOOM.length - 1, zoomNuevo));
        tamano = Math.max(0, Math.min(TAMANO.length - 1, tamanoNuevo));
    }

    public static void probarAmigo(boolean si) {
        var p = Minecraft.getInstance().player;
        prueba = si && p != null ? List.of(new Net.Amigo(new UUID(0x5eedL, 0xa111L), "Amigo", p.getBlockX() + 9, p.getBlockY(), p.getBlockZ() - 6)) : null;
    }

    static boolean amigoConocido(UUID id) {
        for (Net.Amigo a : amigos) if (a.id().equals(id)) return true;
        return false;
    }

    /** Manda al servidor que se ha abierto el libro (0) o escrito una marca (1): logros. */
    static void accion(int tipo) {
        Net.CH.sendToServer(new Net.EspejoAccion(tipo));
    }
}
