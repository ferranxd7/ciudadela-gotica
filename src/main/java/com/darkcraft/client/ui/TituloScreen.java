package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.fml.ModList;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * La pantalla de titulo del mod (parte 2f-2): Valdrenn sobre su isla flotante en una noche de tormenta (el cielo con la
 * luna, el castillo, dos mares de nubes que se mueven, rayos con su destello y su trueno y brasas que suben), el logo en
 * oro y el menu a la izquierda: continuar (la ultima partida, con su nivel y su zona), partidas, nueva partida,
 * multijugador, opciones, mods y salir. Con su musica (ClientMusic). Sustituye a la de Minecraft.
 */
public class TituloScreen extends Screen {
    private static ResourceLocation tex(String n) {
        return new ResourceLocation(DarkCraftMod.MODID, "textures/gui/titulo/" + n + ".png");
    }

    private static final ResourceLocation CIELO = tex("cielo"), LEJOS = tex("lejos"), NUBES_LEJOS = tex("nubes_lejos"),
            NUBES_CERCA = tex("nubes_cerca"), RAYO = tex("rayo"), LOGO = tex("logo");
    /** Medidas del fondo y de las capas (en pixeles del fondo), como las dibuja tools/ui/titulo.py. */
    private static final int FW = 480, FH = 270, NUBES_W = 512, NUBES_LEJOS_H = 90, NUBES_CERCA_H = 80,
            NUBES_LEJOS_Y = 204, NUBES_CERCA_Y = 222, RAYO_W = 64, RAYO_H = 170, LOGO_W = 253, LOGO_H = 62;
    private static final int ORO = 0xFFD9B45A, LUZ = 0xFFFFF2C4, APAGADO = 0xFF8F8068, GRIS = 0xFF6F6656;

    private record Opcion(String clave, Runnable accion, boolean activa) {}

    private final List<Opcion> opciones = new ArrayList<>();
    private final RandomSource azar = RandomSource.create();
    private final float[][] brasas = new float[40][5];
    private List<Guardadas.Una> partidas;
    private Guardadas.Una ultima;
    private int sel, t, encima = -1;
    /** El rayo: cuando cae (en ticks), donde, y cuando suena el trueno. */
    private int rayoT = -1000, rayoX, siguienteRayo = 140, truenoT = -1;
    private boolean cargando;

    public TituloScreen() {
        super(Component.translatable("darkcraft.titulo.titulo"));
        for (float[] b : brasas) nuevaBrasa(b, true);
    }

    @Override
    protected void init() {
        if (partidas == null && !cargando) {
            cargando = true;
            Guardadas.cargar(minecraft).thenAcceptAsync(l -> {
                partidas = l;
                ultima = null;
                for (Guardadas.Una u : l)
                    if (u.jugable()) {
                        ultima = u;
                        break;
                    }
                cargando = false;
                montar();
            }, minecraft);
        }
        montar();
    }

    private void montar() {
        String elegida = opciones.isEmpty() ? null : opciones.get(Mth.clamp(sel, 0, opciones.size() - 1)).clave();
        opciones.clear();
        if (ultima != null) opciones.add(new Opcion("darkcraft.titulo.continuar", this::continuar, true));
        opciones.add(new Opcion("darkcraft.titulo.partidas", () -> minecraft.setScreen(new PartidasScreen(this)), true));
        opciones.add(new Opcion("darkcraft.titulo.nueva", () -> CreateWorldScreen.openFresh(minecraft, this), true));
        boolean multi = minecraft.allowsMultiplayer();
        opciones.add(new Opcion("darkcraft.titulo.multijugador", () -> minecraft.setScreen(minecraft.options.skipMultiplayerWarning
                ? new JoinMultiplayerScreen(this) : new SafetyScreen(this)), multi));
        opciones.add(new Opcion("darkcraft.titulo.opciones", () -> minecraft.setScreen(new OpcionesScreen(this)), true));
        opciones.add(new Opcion("darkcraft.titulo.mods", () -> minecraft.setScreen(new ModsScreen(this)), true));
        opciones.add(new Opcion("darkcraft.titulo.salir", minecraft::stop, true));
        sel = 0;
        if (elegida != null) for (int i = 0; i < opciones.size(); i++) if (opciones.get(i).clave().equals(elegida)) sel = i;
    }

    private void continuar() {
        if (ultima == null) return;
        minecraft.createWorldOpenFlows().loadLevel(this, ultima.id());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ cada tick: rayos, trueno y brasas

    @Override
    public void tick() {
        t++;
        if (t >= siguienteRayo) {
            rayoT = t;
            rayoX = 150 + azar.nextInt(300);
            siguienteRayo = t + 140 + azar.nextInt(200);
            truenoT = t + 12 + azar.nextInt(18);
        }
        if (t == truenoT && minecraft.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.WEATHER) > 0) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.7f + azar.nextFloat() * 0.2f, 0.18f));
        }
        for (float[] b : brasas) {
            b[1] -= b[2];
            b[0] += Mth.sin((t + b[3]) * 0.07f) * 0.25f;
            b[4] -= 1f;
            if (b[4] <= 0 || b[1] < 60) nuevaBrasa(b, false);
        }
    }

    /** x, y, velocidad, fase, vida (en pixeles del fondo y ticks). */
    private void nuevaBrasa(float[] b, boolean repartida) {
        b[0] = azar.nextFloat() * FW;
        b[1] = repartida ? 120 + azar.nextFloat() * 150 : FH + 2;
        b[2] = 0.25f + azar.nextFloat() * 0.45f;
        b[3] = azar.nextFloat() * 100;
        b[4] = 120 + azar.nextFloat() * 200;
    }

    // ------------------------------------------------------------------ dibujo

    private float escala() {
        return Math.max(width / (float) FW, height / (float) FH);
    }

    private int k() {
        return Math.max(1, (int) Math.floor(Math.min(width / 420f, height / 236f)));
    }

    private int menuX() {
        return 16 * k() + 6;
    }

    private int menuY() {
        return 14 * k() + LOGO_H * k() + 12;
    }

    private int paso() {
        return Math.max(15, Math.min(19, (height - menuY() - 28) / Math.max(1, opciones.size() + 1)));
    }

    /** La y de cada opcion (la de continuar ocupa mas: lleva debajo su linea). */
    private int opcionY(int i) {
        int y = menuY();
        for (int j = 0; j < i; j++) y += paso() + (j == 0 && ultima != null ? 9 : 0);
        return y;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        float tt = t + pt;
        fondo(g, tt);
        // el logo
        int k = k();
        Pix.mezcla();
        Pix.blanco();
        g.pose().pushPose();
        g.pose().translate(16 * k, 14 * k, 0);
        g.pose().scale(k, k, 1);
        g.blit(LOGO, 0, 0, 0, 0, LOGO_W, LOGO_H, LOGO_W, LOGO_H);
        g.pose().popPose();
        Component sub = Component.translatable("darkcraft.titulo.subtitulo");
        g.drawString(font, sub, 16 * k + (LOGO_W * k - font.width(sub)) / 2, 14 * k + LOGO_H * k - 4, 0xFFB89A6A, true);
        // el menu
        encima = en(mx, my);
        for (int i = 0; i < opciones.size(); i++) {
            Opcion o = opciones.get(i);
            int y = opcionY(i);
            boolean luz = i == sel;
            Component c = Component.translatable(o.clave());
            int w = Letra.anchoM(font, c);
            if (luz) {
                // la franja de la elegida: roja oscura y con un filo de oro que se apaga hacia la derecha
                Pix.franja(g, menuX() - 40, menuX() + w + 60, y - 3, y + 14, 0x2A0A10, 0.85f);
                Pix.franja(g, menuX() - 40, menuX() + w + 60, y + 14, y + 15, 0xC0963C, 0.9f);
                g.fill(menuX() - 7, y + 4, menuX() - 4, y + 7, 0xFFE2BF62);
                g.fill(menuX() - 6, y + 3, menuX() - 5, y + 8, 0xFFE2BF62);
            }
            int color = !o.activa() ? GRIS : luz ? LUZ : i == encima ? 0xFFF0D890 : ORO;
            Letra.mediana(g, font, c, menuX(), y, color, 0xFF050408);
            if (i == 0 && ultima != null) {
                String linea = ultima.nombre() + " · " + ultima.linea().getString();
                while (font.width(linea) > width / 2 && linea.length() > 6) linea = linea.substring(0, linea.length() - 2);
                g.drawString(font, linea, menuX(), y + 15, luz ? 0xFFC8B48A : APAGADO, true);
            }
        }
        // abajo: version y musica
        String ver = ModList.get().getModContainerById(DarkCraftMod.MODID).map(c -> c.getModInfo().getVersion().toString()).orElse("?");
        g.drawString(font, "Dark Craft " + ver + " · Minecraft " + SharedConstants.getCurrentVersion().getName(), 4, height - 10, GRIS, true);
        Component musica = Component.translatable("darkcraft.titulo.musica");
        g.drawString(font, musica, width - 4 - font.width(musica), height - 10, GRIS, true);
        // con Distant Horizons a 256 chunks y shaders hace falta memoria: si el lanzador da poca, se avisa
        long mem = Runtime.getRuntime().maxMemory();
        if (mem < 5_500_000_000L && com.darkcraft.client.Lejania.hayDH()) {
            Component aviso = Component.translatable("darkcraft.titulo.memoria", String.format(java.util.Locale.ROOT, "%.1f", mem / 1073741824.0));
            g.drawString(font, aviso, 4, height - 21, 0xFFD8704A, true);
        }
        // al abrir, sale de la oscuridad
        if (tt < 30) g.fill(0, 0, width, height, (Math.round(255 * (1f - tt / 30f)) << 24));
        Pix.blanco();
        Pix.mezcla();
        super.render(g, mx, my, pt);
    }

    /**
     * El fondo del titulo para las demas pantallas de los menus fuera del mundo (opciones, mods, partidas...): las nubes
     * siguen moviendose, sin rayos ni brasas, y oscurecido para que se lea la losa.
     */
    public static void fondoMenus(GuiGraphics g, int width, int height) {
        float tt = (net.minecraft.Util.getMillis() % 10_000_000L) / 50f;
        float s = Math.max(width / (float) FW, height / (float) FH);
        float ox = (width - FW * s) / 2f, oy = (height - FH * s) / 2f;
        g.pose().pushPose();
        g.pose().translate(ox, oy, 0);
        g.pose().scale(s, s, 1);
        Pix.mezcla();
        Pix.blanco();
        g.blit(CIELO, 0, 0, 0, 0, FW, FH, FW, FH);
        g.blit(LEJOS, 0, 0, 0, 0, FW, FH, FW, FH);
        int offL = Math.round((tt * 0.12f) % NUBES_W), offC = Math.round((tt * 0.35f) % NUBES_W);
        for (int x = -offL; x < FW; x += NUBES_W) g.blit(NUBES_LEJOS, x, NUBES_LEJOS_Y, 0, 0, NUBES_W, NUBES_LEJOS_H, NUBES_W, NUBES_LEJOS_H);
        for (int x = -offC; x < FW; x += NUBES_W) g.blit(NUBES_CERCA, x, NUBES_CERCA_Y, 0, 0, NUBES_W, NUBES_CERCA_H, NUBES_W, NUBES_CERCA_H);
        g.pose().popPose();
        g.fill(0, 0, width, height, 0x9A050308);
        Pix.blanco();
        Pix.mezcla();
    }

    private void fondo(GuiGraphics g, float tt) {
        float s = escala();
        float ox = (width - FW * s) / 2f, oy = (height - FH * s) / 2f;
        g.pose().pushPose();
        g.pose().translate(ox, oy, 0);
        g.pose().scale(s, s, 1);
        Pix.mezcla();
        Pix.blanco();
        g.blit(CIELO, 0, 0, 0, 0, FW, FH, FW, FH);
        // el destello del rayo ilumina el cielo (detras de Valdrenn: el castillo se recorta negro)
        float edad = tt - rayoT;
        float destello = 0f;
        if (edad >= 0 && edad < 14) destello = edad < 3 ? 0.55f * (1f - edad / 3f) : edad < 5 ? 0f : 0.4f * (1f - (edad - 5) / 9f);
        if (destello > 0.01f) g.fill(0, 0, FW, FH, (Math.round(255 * destello) << 24) | 0xB8A8E0);
        if (edad >= 0 && edad < 7) {
            Pix.suma();
            Pix.color(1f, 1f, 1f, edad < 4 ? 1f : 1f - (edad - 4) / 3f);
            g.blit(RAYO, rayoX - RAYO_W / 2, 0, 0, 0, RAYO_W, RAYO_H, RAYO_W, RAYO_H);
            Pix.blanco();
            Pix.mezcla();
        }
        g.blit(LEJOS, 0, 0, 0, 0, FW, FH, FW, FH);
        // los mares de nubes (se repiten de lado a lado)
        int offL = Math.round((tt * 0.12f) % NUBES_W), offC = Math.round((tt * 0.35f) % NUBES_W);
        for (int x = -offL; x < FW; x += NUBES_W) g.blit(NUBES_LEJOS, x, NUBES_LEJOS_Y, 0, 0, NUBES_W, NUBES_LEJOS_H, NUBES_W, NUBES_LEJOS_H);
        // brasas que suben (entre los dos mares)
        for (float[] b : brasas) {
            float vida = Mth.clamp(b[4] / 60f, 0f, 1f);
            int a = Math.round(200 * vida);
            if (a < 8) continue;
            int c = (b[3] % 3 < 1) ? 0xF8C040 : (b[3] % 3 < 2) ? 0xE0701A : 0xB8301C;
            g.fill((int) b[0], (int) b[1], (int) b[0] + 1, (int) b[1] + 1, (a << 24) | c);
        }
        for (int x = -offC; x < FW; x += NUBES_W) g.blit(NUBES_CERCA, x, NUBES_CERCA_Y, 0, 0, NUBES_W, NUBES_CERCA_H, NUBES_W, NUBES_CERCA_H);
        g.pose().popPose();
        // el velo de la izquierda, para leer el menu
        int ancho = width / 2;
        for (int x = 0; x < ancho; x += 2) {
            float f = 1f - x / (float) ancho;
            int a = Math.round(150 * (float) Math.pow(f, 1.4));
            if (a > 0) g.fill(x, 0, x + 2, height, (a << 24) | 0x040208);
        }
        Pix.blanco();
        Pix.mezcla();
    }

    // ------------------------------------------------------------------ manejo

    private int en(double mx, double my) {
        if (mx < menuX() - 12 || mx > menuX() + 220) return -1;
        for (int i = 0; i < opciones.size(); i++) {
            int y = opcionY(i);
            int h = paso() + (i == 0 && ultima != null ? 9 : 0);
            if (my >= y - 3 && my < y - 3 + h) return i;
        }
        return -1;
    }

    private void elegir(int i) {
        if (i < 0 || i >= opciones.size()) return;
        Opcion o = opciones.get(i);
        if (!o.activa()) {
            Panel.nada();
            return;
        }
        sel = i;
        Panel.acepta();
        o.accion().run();
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int i = en(mx, my);
        if (i >= 0 && i != sel) {
            sel = i;
            Panel.mueve();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        int i = en(mx, my);
        if (i >= 0 && b == 0) {
            elegir(i);
            return true;
        }
        return super.mouseClicked(mx, my, b);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (opciones.isEmpty()) return super.keyPressed(tecla, scan, mods);
        if (tecla == GLFW.GLFW_KEY_UP || tecla == GLFW.GLFW_KEY_W) {
            sel = Math.floorMod(sel - 1, opciones.size());
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || tecla == GLFW.GLFW_KEY_S) {
            sel = Math.floorMod(sel + 1, opciones.size());
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE || tecla == GLFW.GLFW_KEY_E) {
            elegir(sel);
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    /** Que caiga un rayo ya (para la captura). */
    public void probarRayo() {
        rayoT = t - 1;
        rayoX = 300;
    }

    public void probarElegir(int i) {
        sel = Mth.clamp(i, 0, Math.max(0, opciones.size() - 1));
    }

    public String resumen() {
        StringBuilder b = new StringBuilder("titulo:");
        for (Opcion o : opciones) b.append(' ').append(o.clave().replace("darkcraft.titulo.", "")).append(o.activa() ? "" : "(no)");
        b.append(" | partidas ").append(partidas == null ? "cargando" : partidas.size());
        if (ultima != null) b.append(" | ultima ").append(ultima.id()).append(" nivel ").append(ultima.nivel()).append(" zona '").append(ultima.zona()).append('\'');
        return b.toString();
    }
}
