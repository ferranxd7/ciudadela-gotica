package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientMusic;
import com.darkcraft.net.Net;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Los creditos del final (parte 7ad): despues de la cinematica del final ({@code cine/Desenlace}), a pantalla completa sobre negro.
 * Suben despacio con la musica del final (la que entro en la cinematica: {@link ClientMusic#final_}) y tardan lo que le queda a la
 * pista (de 70 a 170 s; con ESPACIO van seis veces mas deprisa; ESC los acaba). El titulo en letra gotica, como acabo, quien lo
 * hizo, las voces (los personajes y sus voces de ElevenLabs), el narrador, la musica (de {@code creditos_musica.json}, que escribe
 * la CI con {@code ci/musica/pistas.json}), los mods que van dentro, la letra y, al final, "Gracias por jugar". Con el alba, motas
 * doradas que suben y un resplandor calido abajo; con la nueva Sombra, ceniza gris que cae y frio arriba. Al cerrarse avisa al
 * servidor ({@code Net.CreditosFin}): Maelis espera junto a la Ultima Llama.
 */
public class CreditosScreen extends Screen {
    private static final int TITULO = 0, SUB = 1, CABECERA = 2, NOMBRE = 3, DETALLE = 4, FRASE = 5, HUECO = 6, GRACIAS = 7;

    /** Una linea de los creditos. */
    private record Linea(int tipo, Component texto) {}

    /** Los personajes con voz y la voz de ElevenLabs de cada uno. */
    private static final String[][] VOCES = {
            {"sombra", "Grav - Deep, Dark & Sinister"},
            {"guia", "Autumn Veil"},
            {"rey", "David - British Storyteller"},
            {"gavran", "Final Boss - Evil All Powerful Villain"},
            {"herrero", "Fjalar - Gruff Scottish Dwarf Warrior"},
            {"mercader", "Cornelius - Wise Sage"},
            {"capitan", "Matthew Schmitz - Old Pirate Captain"},
            {"verdugo", "Declan Graves - Haunted Rasps and Old World Dread"},
            {"barquero", "Gravel Midnight - Deep Character Voice"},
            {"abad", "Elder Rowan"},
            {"condestable", "Jerry B. - Military Commander"},
            {"rectora", "Enchantress - mysterious, witchy, villain character"},
            {"ballenero", "Declan Graves"},
            {"gigantes", "Mossbeard | The God of the Wild"},
            {"jinete", "Zane - Deep Dramatic Late Night Voice"},
            {"dragon", "Malvoryx The Monster"},
            {"isolde", "Elariel X - Epic Ethereal Queen"},
            {"sigrun", "Kaisa - Strong and authoritative"}};
    /** Los mods que van dentro y el shader (con su licencia o su autor). */
    private static final String[][] MODS = {
            {"Distant Horizons", "LGPL-3.0"}, {"Oculus", "LGPL-3.0"}, {"Embeddium", "LGPL-3.0"},
            {"Complementary Shaders - Unbound", "EminGT · complementary.dev"}};

    private final int cual;
    private final List<Linea> lineas = new ArrayList<>();
    private final List<Integer> alturas = new ArrayList<>();
    private int alto;
    /** Segundos (con lo acelerado), segundos de verdad, lo que tarda en pasar todo y desde cuando se apaga. */
    private float t, real, duracion, apaga = -1;
    private boolean avisado;

    public CreditosScreen(int cual) {
        super(Component.translatable("darkcraft.creditos.titulo"));
        this.cual = cual == 2 ? 2 : 1;
    }

    /** Los abre (y pone la musica del final, si no sonaba ya: se salto la cinematica antes de que entrara). */
    public static void abrir(int cual) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ClientMusic.final_(cual == 2 ? 2 : 1, 3.0f);
        mc.setScreen(new CreditosScreen(cual));
        DarkCraftMod.LOGGER.info("[DarkCraft] Los creditos del final ({})", cual == 2 ? "la nueva Sombra" : "el alba");
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        if (lineas.isEmpty()) montar();
        medir();
    }

    // ------------------------------------------------------------------ lo que dicen

    private void add(int tipo, Component c) {
        lineas.add(new Linea(tipo, c));
    }

    private void add(int tipo, String clave) {
        add(tipo, Component.translatable(clave));
    }

    private void montar() {
        String f = cual == 2 ? "sombra" : "alba";
        add(TITULO, Component.literal("Dark Craft"));
        add(SUB, "darkcraft.creditos.final." + f);
        add(FRASE, "darkcraft.creditos.frase." + f);
        add(HUECO, Component.empty());
        add(HUECO, Component.empty());
        add(CABECERA, "darkcraft.creditos.hecho");
        add(NOMBRE, Component.literal("ferranxd7"));
        add(DETALLE, "darkcraft.creditos.hecho_con");
        add(CABECERA, "darkcraft.creditos.voces");
        add(DETALLE, "darkcraft.creditos.voces_nota");
        for (String[] v : VOCES) {
            add(NOMBRE, "darkcraft.creditos.pj." + v[0]);
            add(DETALLE, Component.literal(v[1]));
        }
        add(CABECERA, "darkcraft.creditos.narrador");
        add(NOMBRE, Component.literal("Piper TTS — \"alan\" (en_GB)"));
        add(DETALLE, "darkcraft.creditos.narrador_nota");
        List<String[]> musica = musica();
        if (!musica.isEmpty()) {
            add(CABECERA, "darkcraft.creditos.musica");
            add(DETALLE, "darkcraft.creditos.musica_nota");
            for (String[] m : musica) {
                add(NOMBRE, Component.literal("“" + m[0] + "”"));
                add(DETALLE, Component.literal(m[1] + " · " + m[2]));
            }
        }
        add(CABECERA, "darkcraft.creditos.sonidos");
        add(DETALLE, "darkcraft.creditos.sonidos_nota");
        add(CABECERA, "darkcraft.creditos.mods");
        for (String[] m : MODS) {
            add(NOMBRE, Component.literal(m[0]));
            add(DETALLE, Component.literal(m[1]));
        }
        add(CABECERA, "darkcraft.creditos.letra");
        add(NOMBRE, Component.literal("New Rocker"));
        add(DETALLE, Component.literal("Pablo Impallari, Brenda Gallo, Rodrigo Fuenzalida · SIL OFL 1.1"));
        add(CABECERA, "darkcraft.creditos.forge");
        add(NOMBRE, Component.literal("Minecraft Forge 1.20.1"));
        for (int i = 0; i < 5; i++) add(HUECO, Component.empty());
        add(GRACIAS, "darkcraft.creditos.gracias");
        add(FRASE, "darkcraft.creditos.cierre." + f);
    }

    /** La musica: {titulo, autor, licencia} de cada pista (lo escribe la CI; sin el fichero, nada). */
    private static List<String[]> musica() {
        List<String[]> l = new ArrayList<>();
        try (InputStream in = CreditosScreen.class.getResourceAsStream("/assets/darkcraft/creditos_musica.json")) {
            if (in == null) return l;
            for (com.google.gson.JsonElement e : com.google.gson.JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray()) {
                com.google.gson.JsonObject o = e.getAsJsonObject();
                l.add(new String[]{o.get("titulo").getAsString(), o.get("autor").getAsString(), o.get("licencia").getAsString()});
            }
        } catch (Exception ignorado) {
            // sin musica en los creditos
        }
        return l;
    }

    /** Lo que mide cada linea (en pixeles de la interfaz) y, con eso, lo que tarda todo. */
    private void medir() {
        alturas.clear();
        int y = 0, ancho = Math.max(120, Math.min(width - 40, 360));
        for (Linea l : lineas) {
            alturas.add(y);
            y += switch (l.tipo()) {
                case TITULO -> 48;
                case SUB -> 22;
                case CABECERA -> 34;
                case NOMBRE -> 12;
                case DETALLE -> 4 + 10 * Math.max(1, font.split(l.texto(), ancho).size());
                case FRASE -> 8 + 11 * Math.max(1, font.split(l.texto(), ancho).size());
                case GRACIAS -> 30;
                default -> 16;
            };
        }
        alto = y;
        // tanto como le quede a la musica (que no se acabe antes), entre 70 y 170 s
        float dura = duracionMusica(cual == 2 ? "final_sombra" : "final_alba"), ya = Math.max(0f, ClientMusic.finalSegundos());
        duracion = dura > 0 ? Mth.clamp(dura - ya - 9f, 70f, 170f) : 110f;
    }

    /** Lo que dura una pista (de {@code musica.json}, que escribe la CI; -1 si no se sabe). */
    private static float duracionMusica(String id) {
        try (InputStream in = CreditosScreen.class.getResourceAsStream("/assets/darkcraft/musica.json")) {
            if (in == null) return -1f;
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            return o.has(id) ? o.getAsJsonObject(id).get("duracion").getAsFloat() : -1f;
        } catch (Exception ignorado) {
            return -1f;
        }
    }

    /** Lo que ha subido todo (pixeles): sale por abajo y la ultima linea acaba en el centro. */
    private float subido() {
        float recorrido = alto + height * 0.5f;
        return Math.min(recorrido, t / duracion * recorrido);
    }

    private boolean alFinal() {
        return t >= duracion;
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        boolean deprisa = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        real += 0.05f;
        if (real > 1.2f) t += deprisa ? 0.3f : 0.05f;
        // al acabar: unos segundos con "Gracias por jugar" en el centro y se apaga
        if (alFinal() && apaga < 0) apaga = real + 6.5f;
        if (apaga >= 0 && real >= apaga + 2.5f) onClose();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        avisar();
        ClientMusic.final_(0, 3.5f);
        super.onClose();
    }

    @Override
    public void removed() {
        avisar();
        super.removed();
    }

    private void avisar() {
        if (avisado) return;
        avisado = true;
        if (minecraft != null && minecraft.getConnection() != null) Net.CH.sendToServer(new Net.CreditosFin());
    }

    // ------------------------------------------------------------------ dibujar

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int w = width, h = height;
        float rt = real + pt * 0.05f;
        g.fill(0, 0, w, h, 0xFF000000);
        fondo(g, w, h, rt);
        float entra = suave(rt / 1.6f), sale = apaga < 0 ? 1f : 1f - suave((rt - apaga) / 2.5f);
        float vis = entra * sale;
        float sube = subido() + (t > 0 ? pt * 0.05f / duracion * (alto + h * 0.5f) : 0f);
        int ancho = Math.max(120, Math.min(w - 40, 360));
        for (int i = 0; i < lineas.size(); i++) {
            Linea l = lineas.get(i);
            float y = h + alturas.get(i) - Math.min(sube, alto + h * 0.5f);
            if (y < -60 || y > h + 10) continue;
            // se apagan cerca de los bordes de arriba y de abajo
            float borde = Math.min(suave((y + 20) / (h * 0.16f)), suave((h - y) / (h * 0.14f)));
            float a = vis * borde;
            if (a * 255 < 4) continue;
            dibujar(g, l, w, Math.round(y), ancho, a);
        }
        // abajo a la derecha, un momento: como se aceleran y como se salen
        float pista = Math.min(suave((rt - 2f) / 1f), 1f - suave((rt - 9f) / 1.5f)) * 0.7f;
        if (pista * 255 >= 4) {
            Component c = Component.translatable("darkcraft.creditos.teclas");
            g.drawString(font, c, w - 8 - font.width(c), h - 14, argb(pista, 170, 160, 150), false);
        }
    }

    private void dibujar(GuiGraphics g, Linea l, int w, int y, int ancho, float a) {
        switch (l.tipo()) {
            case TITULO -> {
                PoseStack ps = g.pose();
                ps.pushPose();
                float s = 2f;
                ps.translate(w / 2f - Letra.anchoG(font, l.texto()) * s / 2f, y, 0);
                ps.scale(s, s, 1);
                Letra.grande(g, font, l.texto(), 0, 0, argb(a, cual == 2 ? 196 : 232, cual == 2 ? 40 : 196, cual == 2 ? 40 : 120), argb(a, 20, 6, 4));
                ps.popPose();
            }
            case SUB -> {
                int x = (w - Letra.anchoM(font, l.texto())) / 2;
                Letra.mediana(g, font, l.texto(), x, y, cual == 2 ? argb(a, 176, 184, 200) : argb(a, 246, 210, 168), argb(a, 14, 8, 6));
            }
            case CABECERA -> {
                int x = (w - Letra.anchoM(font, l.texto())) / 2;
                Letra.mediana(g, font, l.texto(), x, y + 12, argb(a, 214, 172, 92), argb(a, 18, 10, 4));
            }
            case NOMBRE -> centrado(g, l.texto(), w, y, argb(a, 236, 228, 214), ancho);
            case DETALLE -> centrado(g, l.texto(), w, y + 1, argb(a * 0.85f, 150, 142, 132), ancho);
            case FRASE -> centrado(g, l.texto().copy().withStyle(net.minecraft.ChatFormatting.ITALIC), w, y + 4,
                    cual == 2 ? argb(a, 168, 176, 196) : argb(a, 240, 214, 180), ancho);
            case GRACIAS -> {
                int x = (w - Letra.anchoG(font, l.texto())) / 2;
                Letra.grande(g, font, l.texto(), x, y, argb(a, 232, 204, 150), argb(a, 20, 8, 4));
            }
            default -> {
            }
        }
    }

    private void centrado(GuiGraphics g, Component c, int w, int y, int color, int ancho) {
        List<FormattedCharSequence> partes = font.split(c, ancho);
        for (int k = 0; k < partes.size(); k++) {
            FormattedCharSequence p = partes.get(k);
            g.drawString(font, p, (w - font.width(p)) / 2, y + k * 10, color, false);
        }
    }

    /** El fondo: un resplandor calido abajo y motas doradas que suben (el alba), o frio arriba y ceniza que cae (la nueva Sombra). */
    private void fondo(GuiGraphics g, int w, int h, float rt) {
        float a = suave(rt / 3f);
        if (cual == 1) g.fillGradient(0, h / 2, w, h, argb(0f, 0, 0, 0), argb(0.38f * a, 92, 40, 12));
        else g.fillGradient(0, 0, w, h / 2, argb(0.3f * a, 26, 32, 44), argb(0f, 0, 0, 0));
        for (int i = 0; i < 70; i++) {
            float vel = 0.012f + 0.03f * azar(i, 1);
            float p = (rt * vel + azar(i, 2)) % 1f;
            float x = (azar(i, 3) * w + Mth.sin(rt * 0.4f + i) * 6f) % w;
            float y = cual == 1 ? h * (1f - p) : h * p;
            float al = a * (float) Math.sin(Math.PI * p) * (0.25f + 0.45f * azar(i, 4));
            if (al * 255 < 4) continue;
            int t = azar(i, 5) < 0.25f ? 2 : 1;
            int c = cual == 1 ? argb(al, 255, 196 + (int) (40 * azar(i, 6)), 120) : argb(al * 0.8f, 150, 154, 160);
            g.fill(Math.round(x), Math.round(y), Math.round(x) + t, Math.round(y) + t, c);
        }
    }

    // ------------------------------------------------------------------ cuentas

    private static float suave(float v) {
        v = v < 0 ? 0 : Math.min(1, v);
        return v * v * (3 - 2 * v);
    }

    private static float azar(int i, int k) {
        float v = Mth.sin(i * 12.9898f + k * 78.233f) * 43758.547f;
        return v - Mth.floor(v);
    }

    private static int argb(float a, int r, int g, int b) {
        int al = Math.round((a < 0 ? 0 : Math.min(1, a)) * 255);
        return (al << 24) | (r << 16) | (g << 8) | b;
    }

    // ------------------------------------------------------------------ para las pruebas

    /** Lleva los creditos a esa parte del recorrido (0 al empezar, 1 al acabar). */
    public void saltarA(float parte) {
        real = Math.max(real, 2f);
        t = Mth.clamp(parte, 0f, 1f) * duracion;
    }

    public float tiempo() {
        return t;
    }

    public String resumen() {
        return String.format(java.util.Locale.ROOT, "creditos %s: %d lineas, %d px, %.1f de %.1f s (de verdad %.1f)%s", cual == 2 ? "de la nueva Sombra" : "del alba",
                lineas.size(), alto, t, duracion, real, apaga >= 0 ? ", apagandose" : "");
    }
}
