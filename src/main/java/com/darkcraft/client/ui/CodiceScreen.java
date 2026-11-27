package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.espejo.EspejoHud;
import com.darkcraft.net.Net;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * El codice (parte 2e-4): los libros de la historia a pantalla completa, en pixel art. Dos paginas de pergamino oscuro
 * sobre la tapa de cuero negro con hierros de oro y la cinta roja; la primera pagina de cada libro con su miniatura
 * ({@code tools/ui/miniaturas.py}), la cabecera en letra gotica, el titulo, un floron y la letra capital iluminada; el
 * texto con la letra del juego, en tinta, y un calderon rojo en cada parrafo. Las paginas se pasan con una animacion
 * (la hoja gira sobre el lomo) y su sonido. Sirve para la cronica (capitulos), los libros perdidos, el diario, el
 * acertijo de la fortaleza y cualquier libro escrito: sustituye a la pantalla de libro y a la del atril de Minecraft
 * ({@link #alAbrir}).
 */
public class CodiceScreen extends Screen {
    public static final ResourceLocation PERGAMINO = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/codice_pergamino.png"),
            CUERO = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/codice_cuero.png"),
            MINIATURAS = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/miniaturas.png"),
            VINIETA = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/vinieta.png");
    /** Estilos: la cronica de Aldous (capital roja), la Sombra (negra y roja, tinta roja), libros perdidos (azul), diario (verde). */
    public static final int CRONICA = 0, SOMBRA = 1, PERDIDO = 2, DIARIO = 3;
    /** Las miniaturas de miniaturas.png: 0-5 los capitulos, y estas; 12-15 los capitulos de Morvain (7-10, parte 7ae). */
    public static final int MIN_PERDIDO_VALDRENN = 6, MIN_PERDIDO_FORTALEZA = 7, MIN_DIARIO = 8, MIN_ACERTIJO = 9, MIN_PERDIDO_BRUMAVELA = 10,
            MIN_PERDIDO_MORVAIN = 11, MIN_MORVAIN = 12;
    /** Lo alto del atlas de las miniaturas (cuatro filas de cuatro). */
    private static final int MINI_ATLAS_H = 320;

    /** La miniatura de un capitulo de la cronica: 0-5 los de Valdrenn y el de la Sombra; 12-15 los de Morvain. */
    public static int miniaturaCapitulo(int ch) {
        return ch >= 7 ? MIN_MORVAIN + Mth.clamp(ch - 7, 0, 3) : Mth.clamp(ch - 1, 0, 5);
    }

    /** La miniatura de un libro perdido: los de Valdrenn (1-5), los de la fortaleza (6-11), las notas de Brumavela (12-16) y
     * los de Morvain (18 en adelante). */
    public static int miniaturaPerdido(int n) {
        // (1-5 Valdrenn, 6-11 la fortaleza, 12-16 Brumavela, 17 la fortaleza otra vez: la cripta de la parte 5c; 18 Velmor y
        // 19 el registro del verdugo de la Arboleda (y 20, la hermana de la capilla de los pilotes, parte 7i; y 21, el portero del
        // Monasterio de la Helada, parte 7k): la de Morvain, desde la parte 7h —el Arbol de la Horca y la ciudadela—)
        // (31, la carta del Rey que llevaba el Jinete Nocturno por el camino de nubes, parte 7x: la de Valdrenn)
        return n <= 5 || n == 31 ? MIN_PERDIDO_VALDRENN : n <= 11 || n == 17 ? MIN_PERDIDO_FORTALEZA : n <= 16 ? MIN_PERDIDO_BRUMAVELA : MIN_PERDIDO_MORVAIN;
    }
    static final int TINTA = 0xFF24170C, TINTA_SOMBRA = 0xFF3C0A0E, RUBRICA = 0xFF801212, AZUL_T = 0xFF1C2E78, PALIDA = 0xFF5E4A30;
    private static final int LINEA = 10, CAPITAL = 30, MIN_W = 128, MIN_H = 80, MARCO = 6, GIRO_MS = 380;
    private static final int PAD = 13, PAD_ARRIBA = 12, PAD_ABAJO = 20, BORDE = 11;

    // ------------------------------------------------------------------ los libros

    private static final int PARRAFO = 0, ENCABEZADO = 1;

    record Bloque(int tipo, String texto) {}

    /** Un libro para el codice: cabecera, titulo, autor, miniatura, estilo y su texto en bloques. */
    public static final class Libro {
        public final String id;
        Component cabecera, titulo, autor;
        int miniatura = -1, estilo = CRONICA;
        boolean acertijo;
        final List<Bloque> bloques = new ArrayList<>();

        Libro(String id) {
            this.id = id;
        }

        /** Un texto con parrafos separados por una linea en blanco (dentro de un parrafo, cada salto de linea se respeta). */
        void parrafos(String s) {
            for (String p : s.replace("\r", "").split("\n\\s*\n"))
                if (!p.isBlank()) bloques.add(new Bloque(PARRAFO, p.strip()));
        }
    }

    static String romano(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            case 11 -> "XI";
            default -> Integer.toString(n);
        };
    }

    /** Un capitulo de la cronica (el 6 lo escribe la Sombra; los 7-10, la cronica de Morvain, los cuenta Maelis). */
    public static Libro capitulo(int ch) {
        Libro l = new Libro("cap" + ch);
        l.cabecera = Component.translatable("darkcraft.capitulo", romano(ch));
        l.titulo = Component.translatable("darkcraft.cap." + ch + ".titulo");
        l.miniatura = miniaturaCapitulo(ch);
        l.estilo = ch == 6 ? SOMBRA : CRONICA;
        for (int p = 1; p <= 40; p++) {
            String k = "darkcraft.cap." + ch + ".p" + p;
            if (!I18n.exists(k)) break;
            l.parrafos(I18n.get(k));
        }
        return l;
    }

    /** Un libro perdido (las cosas que decia la gente de Valdrenn y de la fortaleza). */
    public static Libro perdido(int n) {
        Libro l = new Libro("perdido" + n);
        l.cabecera = Component.translatable("darkcraft.codice.perdido");
        l.titulo = Component.translatable("darkcraft.perdido." + n + ".titulo");
        l.autor = Component.translatable("darkcraft.perdido." + n + ".autor");
        l.miniatura = miniaturaPerdido(n);
        l.estilo = PERDIDO;
        for (int p = 1; p <= 40; p++) {
            String k = "darkcraft.perdido." + n + ".p" + p;
            if (!I18n.exists(k)) break;
            l.parrafos(I18n.get(k));
        }
        return l;
    }

    /** El diario del jugador (lo que el Espejo de la Sombra ha ido apuntando: el dia del mundo y lo que paso). */
    public static Libro diario(List<Net.DiarioE> entradas) {
        Libro l = new Libro("diario");
        l.cabecera = Component.translatable("darkcraft.sombra.diario");
        l.titulo = Component.translatable("darkcraft.codice.diario.titulo");
        l.miniatura = MIN_DIARIO;
        l.estilo = DIARIO;
        if (entradas == null || entradas.isEmpty()) l.parrafos(I18n.get("darkcraft.sombra.diario.vacio"));
        else for (Net.DiarioE e : entradas) {
            l.bloques.add(new Bloque(ENCABEZADO, I18n.get("darkcraft.sombra.dia", e.dia())));
            l.parrafos(I18n.get("darkcraft.diario." + e.clave()));
        }
        return l;
    }

    /** El libro del acertijo del maestre Velan: sus versos, el sello y (al pulsarlo) la forja del amuleto. */
    public static Libro acertijo() {
        Libro l = new Libro("acertijo");
        l.titulo = Component.translatable("darkcraft.acertijo.titulo");
        l.autor = Component.translatable("darkcraft.acertijo.autor");
        l.miniatura = MIN_ACERTIJO;
        l.estilo = SOMBRA;
        l.acertijo = true;
        int n = 3;
        try {
            n = Math.max(1, Integer.parseInt(I18n.get("darkcraft.acertijo.paginas")));
        } catch (NumberFormatException e) {
            // tres paginas
        }
        for (int p = 1; p <= n; p++) l.parrafos(I18n.get("darkcraft.acertijo.p" + p));
        return l;
    }

    /** Un libro escrito: los del mod con su texto del idioma del juego; cualquier otro, con lo que lleve escrito. */
    public static Libro escrito(ItemStack st) {
        if (st.hasTag() && st.getTag().contains("darkcraft_libro"))
            return capitulo(Mth.clamp(st.getTag().getInt("darkcraft_libro"), 1, com.darkcraft.story.Story.CHAPTERS));
        int perdido = com.darkcraft.fortaleza.Perdidos.de(st);
        if (perdido > 0) return perdido(perdido);
        Libro l = new Libro("escrito");
        CompoundTag t = st.getTag();
        l.titulo = t != null && t.contains("title") ? Component.literal(t.getString("title")) : st.getHoverName();
        if (t != null && t.contains("author")) l.autor = Component.translatable("book.byAuthor", t.getString("author"));
        if (t != null) {
            ListTag pags = t.getList("pages", Tag.TAG_STRING);
            for (int i = 0; i < pags.size(); i++) {
                String s = pags.getString(i);
                String texto;
                try {
                    Component c = Component.Serializer.fromJson(s);
                    texto = c == null ? s : c.getString();
                } catch (RuntimeException e) {
                    texto = s;
                }
                l.parrafos(texto);
            }
        }
        return l;
    }

    // ------------------------------------------------------------------ abrir

    public static void abrir(Libro l) {
        Minecraft.getInstance().setScreen(new CodiceScreen(l, null, null));
    }

    /** Abre el codice y, al cerrarlo, vuelve a {@code volver} (el menu, por ejemplo). */
    public static void abrir(Libro l, Screen volver) {
        Minecraft.getInstance().setScreen(new CodiceScreen(l, volver, null));
    }

    /**
     * La pantalla de libro y la del atril de Minecraft no se ven nunca: en su lugar, el codice (con el libro que se lleva
     * en la mano, o el del atril cuando llegue del servidor).
     */
    public static Screen alAbrir(Screen nueva) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return nueva;
        // (la del atril hereda de la de libro: va primero)
        if (nueva instanceof net.minecraft.client.gui.screens.inventory.LecternScreen ls) return new CodiceScreen(null, null, ls.getMenu());
        if (nueva instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen) {
            ItemStack st = mc.player.getMainHandItem();
            if (!st.is(Items.WRITTEN_BOOK)) st = mc.player.getOffhandItem();
            if (st.is(Items.WRITTEN_BOOK)) return new CodiceScreen(escrito(st), null, null);
        }
        return nueva;
    }

    // ------------------------------------------------------------------ la pantalla

    /** Lo que va en una pagina ya maquetada. */
    record Elem(int tipo, int x, int y, String texto, int color, int extra) {}

    private static final int E_TEXTO = 0, E_MINIATURA = 1, E_CABECERA = 2, E_TITULO = 3, E_FLORON = 4, E_CAPITAL = 5, E_FIN = 6,
            E_SELLO = 7, E_RECETA = 8, E_AUTOR = 9, E_ENCABEZADO = 10;

    private Libro libro;
    private final Screen volver;
    private final LecternMenu atril;
    private List<List<Elem>> paginas = new ArrayList<>();
    private int pliego, destino = -1, dir;
    private long giro0;
    private boolean receta;
    private int bx, by, bw, bh, lomo, pagW, pagH, pagY, izqX, derX;
    private int maquetaW = -1, maquetaH = -1;
    private final long abierto = Util.getMillis();
    private int t;

    private CodiceScreen(Libro libro, Screen volver, LecternMenu atril) {
        super(Component.translatable("darkcraft.codice.titulo"));
        this.libro = libro;
        this.volver = volver;
        this.atril = atril;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_CODICE_ABRE.get(), 1.0f, 0.9f));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        // el libro abierto: dos paginas de hasta 272 de ancho, centrado, con la barra de teclas abajo
        bw = Math.min(width - 12, 2 * 272 + 2 * BORDE);
        bh = Math.min(height - 26, 336);
        bx = (width - bw) / 2;
        by = Math.max(4, (height - 18 - bh) / 2);
        lomo = bx + bw / 2;
        pagY = by + BORDE;
        pagH = bh - 2 * BORDE;
        izqX = bx + BORDE;
        derX = lomo;
        pagW = lomo - izqX;
        if (maquetaW != pagW || maquetaH != pagH) maquetar();
    }

    @Override
    public void tick() {
        t++;
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0) {
            onClose();
            return;
        }
        // el libro del atril llega del servidor un poco despues de abrirse
        if (libro == null && atril != null) {
            ItemStack st = atril.getBook();
            if (!st.isEmpty()) {
                libro = escrito(st);
                maquetar();
            }
        }
    }

    @Override
    public void onClose() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_CODICE_CIERRA.get(), 1.0f, 0.8f));
        if (atril != null && minecraft.player != null) {
            // el atril es un contenedor del servidor: se cierra (y el juego quita la pantalla)
            minecraft.player.closeContainer();
            return;
        }
        if (volver != null) minecraft.setScreen(volver);
        else super.onClose();
    }

    // ------------------------------------------------------------------ maquetar

    private int ancho() {
        return pagW - 2 * PAD;
    }

    private int alto() {
        return pagH - PAD_ARRIBA - PAD_ABAJO;
    }

    private int tinta() {
        return libro != null && libro.estilo == SOMBRA ? TINTA_SOMBRA : TINTA;
    }

    private int colorCabecera() {
        if (libro == null) return RUBRICA;
        return switch (libro.estilo) {
            case SOMBRA -> 0xFF5A0A0E;
            case PERDIDO -> AZUL_T;
            default -> RUBRICA;
        };
    }

    private final class Maqueta {
        final int w = ancho(), h = alto();
        List<Elem> pag = new ArrayList<>();
        int y;

        Maqueta() {
            paginas.add(pag);
        }

        void elem(int tipo, int x, int yy, String texto, int color, int extra) {
            pag.add(new Elem(tipo, x, yy, texto, color, extra));
        }

        /** Que quepan {@code alto} pixeles mas en esta pagina; si no, a la siguiente. */
        void sitio(int alto) {
            if (y + alto > h) nueva();
        }

        void nueva() {
            pag = new ArrayList<>();
            paginas.add(pag);
            y = 0;
        }
    }

    private void maquetar() {
        maquetaW = pagW;
        maquetaH = pagH;
        paginas = new ArrayList<>();
        if (libro == null || font == null) {
            paginas.add(new ArrayList<>());
            paginas.add(new ArrayList<>());
            return;
        }
        Maqueta m = new Maqueta();
        int w = m.w;
        // la portada: miniatura, cabecera, titulo, autor y floron
        if (libro.miniatura >= 0 && m.h >= MIN_H + 2 * MARCO + 70 && w >= MIN_W + 2 * MARCO) {
            m.elem(E_MINIATURA, (w - MIN_W) / 2, MARCO, "", 0, libro.miniatura);
            m.y = MIN_H + 2 * MARCO + 6;
        }
        if (libro.cabecera != null) {
            m.elem(E_CABECERA, w / 2, m.y, libro.cabecera.getString(), colorCabecera(), 0);
            m.y += Letra.ALTO_M + 5;
        }
        if (libro.titulo != null) {
            for (String l : envolver(libro.titulo.getString(), w, w)) {
                m.elem(E_TITULO, w / 2, m.y, l, tinta(), 0);
                m.y += LINEA;
            }
        }
        if (libro.autor != null) {
            m.elem(E_AUTOR, w / 2, m.y, libro.autor.getString(), PALIDA, 0);
            m.y += LINEA;
        }
        m.elem(E_FLORON, (w - CodiceTex.FLORON.w()) / 2, m.y + 2, "", 0, 0);
        m.y += CodiceTex.FLORON.h() + 7;
        boolean primero = true;
        for (Bloque b : libro.bloques) {
            if (b.tipo() == ENCABEZADO) {
                m.sitio(2 * LINEA + 4);
                if (m.y > 0) m.y += 4;
                m.elem(E_ENCABEZADO, 0, m.y, b.texto(), RUBRICA, 0);
                m.y += LINEA + 1;
                primero = true;
                continue;
            }
            parrafo(m, b.texto(), primero && (libro.estilo != DIARIO || !hayCapital()));
            primero = false;
        }
        if (libro.acertijo) {
            m.sitio(48);
            m.elem(E_SELLO, w / 2, m.y + 6, "", 0, 0);
            m.y += 48;
            if (receta) {
                m.nueva();
                m.elem(E_RECETA, 0, 0, "", 0, 0);
            }
        } else {
            m.sitio(14);
            m.elem(E_FIN, (w - CodiceTex.FIN.w()) / 2, m.y + 4, "", 0, 0);
            m.y += 14;
        }
        if (paginas.size() % 2 == 1) paginas.add(new ArrayList<>());
        pliego = Mth.clamp(pliego, 0, paginas.size() / 2 - 1);
    }

    private boolean hayCapital() {
        for (List<Elem> p : paginas) for (Elem e : p) if (e.tipo() == E_CAPITAL) return true;
        return false;
    }

    /** Un parrafo: con la letra capital (si es el primero) o con su calderon rojo; respeta los saltos de linea (versos). */
    private void parrafo(Maqueta m, String texto, boolean capital) {
        String txt = texto;
        int capY = -1;
        List<Elem> capPag = null;
        if (capital && !txt.isEmpty() && Character.isLetter(txt.charAt(0))) {
            m.sitio(CAPITAL + 2);
            if (m.y > 0 && m.y < 6) m.y = 0;
            capY = m.y;
            capPag = m.pag;
            m.elem(E_CAPITAL, 0, m.y, String.valueOf(Character.toUpperCase(txt.charAt(0))), 0, libro.estilo);
            txt = txt.substring(1);
        } else {
            if (m.y > 0) m.y += 3;
            txt = "¶ " + txt;
        }
        String[] versos = txt.split("\n");
        for (int v = 0; v < versos.length; v++) {
            String resto = versos[v].strip();
            if (v == 0 && capY >= 0) resto = versos[v];
            if (resto.isEmpty()) {
                m.y += LINEA / 2;
                continue;
            }
            while (!resto.isEmpty()) {
                boolean junto = capY >= 0 && m.pag == capPag && m.y < capY + CAPITAL;
                // la primera linea de debajo de la capital, con un poco de aire
                if (!junto && capY >= 0 && m.pag == capPag && m.y < capY + CAPITAL + 2) m.y = capY + CAPITAL + 2;
                int x = junto ? CAPITAL + 4 : 0, w = m.w - x;
                m.sitio(LINEA);
                if (m.pag != capPag) {
                    junto = false;
                    x = 0;
                    w = m.w;
                }
                int corte = cabe(resto, w);
                String linea = resto.substring(0, corte).stripTrailing();
                resto = resto.substring(corte).stripLeading();
                m.elem(E_TEXTO, x, m.y + (junto ? 1 : 0), linea, tinta(), linea.startsWith("¶") ? 1 : 0);
                m.y += LINEA;
            }
        }
        if (capY >= 0 && m.pag == capPag && m.y < capY + CAPITAL + 2) m.y = capY + CAPITAL + 2;
    }

    /** Cuantas letras de {@code s} caben en {@code w} (cortando entre palabras; si una sola no cabe, por letras). */
    private int cabe(String s, int w) {
        if (font.width(s) <= w) return s.length();
        int ultimo = -1;
        for (int i = 1; i <= s.length(); i++) {
            if (i < s.length() && s.charAt(i) != ' ') continue;
            if (font.width(s.substring(0, i)) > w) break;
            ultimo = i;
        }
        if (ultimo > 0) return ultimo;
        int i = 1;
        while (i < s.length() && font.width(s.substring(0, i + 1)) <= w) i++;
        return Math.max(1, i);
    }

    /** Un texto en lineas: la primera de {@code w0} de ancho y las demas de {@code w}. */
    private List<String> envolver(String s, int w0, int w) {
        List<String> out = new ArrayList<>();
        String resto = s.strip();
        while (!resto.isEmpty()) {
            int c = cabe(resto, out.isEmpty() ? w0 : w);
            out.add(resto.substring(0, c).stripTrailing());
            resto = resto.substring(c).stripLeading();
        }
        return out;
    }

    // ------------------------------------------------------------------ pasar pagina

    private int pliegos() {
        return Math.max(1, paginas.size() / 2);
    }

    private boolean girando() {
        return destino >= 0;
    }

    /** Para la prueba de la CI: la hoja quieta a medio pasar (o -1). */
    private float congelado = -1f;

    private float giro() {
        if (congelado >= 0f) return congelado;
        return Mth.clamp((Util.getMillis() - giro0) / (float) GIRO_MS, 0f, 1f);
    }

    private void pasar(int d) {
        congelado = -1f;
        if (girando()) {
            pliego = destino;
            destino = -1;
        }
        int nuevo = pliego + d;
        if (nuevo < 0 || nuevo >= pliegos()) {
            if (d > 0 && nuevo >= pliegos()) onClose();
            return;
        }
        destino = nuevo;
        dir = d;
        giro0 = Util.getMillis();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_CODICE_PAGINA.get(), 0.95f + (float) Math.random() * 0.1f, 0.9f));
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (tecla == GLFW.GLFW_KEY_RIGHT || tecla == GLFW.GLFW_KEY_D || tecla == GLFW.GLFW_KEY_SPACE || tecla == GLFW.GLFW_KEY_PAGE_DOWN
                || minecraft.options.keyInventory.matches(tecla, scan)) {
            pasar(1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_LEFT || tecla == GLFW.GLFW_KEY_A || tecla == GLFW.GLFW_KEY_BACKSPACE || tecla == GLFW.GLFW_KEY_PAGE_UP) {
            pasar(-1);
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double r) {
        pasar(r < 0 ? 1 : -1);
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b != 0) return super.mouseClicked(mx, my, b);
        // el sello del acertijo
        if (!girando() && libro != null && libro.acertijo && !receta) {
            for (int lado = 0; lado < 2; lado++) {
                int i = 2 * pliego + lado;
                if (i >= paginas.size()) continue;
                for (Elem e : paginas.get(i)) {
                    if (e.tipo() != E_SELLO) continue;
                    int cx = xTexto(lado == 1) + e.x(), cy = yTexto() + e.y() + 13;
                    if (Math.abs(mx - cx) < 16 && my >= cy - 16 && my < cy + 26) {
                        verReceta();
                        return true;
                    }
                }
            }
        }
        if (my >= pagY && my < pagY + pagH) {
            if (mx >= izqX && mx < lomo) {
                pasar(-1);
                return true;
            }
            if (mx >= lomo && mx < lomo + pagW) {
                pasar(1);
                return true;
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    private void verReceta() {
        receta = true;
        maquetar();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 0.7f));
        // a la pagina de la receta (la ultima)
        int ultimo = pliegos() - 1;
        if (ultimo != pliego) {
            destino = ultimo;
            dir = 1;
            giro0 = Util.getMillis();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_CODICE_PAGINA.get(), 1.0f, 0.9f));
        }
    }

    // ------------------------------------------------------------------ dibujar

    private int xTexto(boolean derecha) {
        return (derecha ? derX : izqX) + PAD;
    }

    private int yTexto() {
        return pagY + PAD_ARRIBA;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (girando() && congelado < 0f && giro() >= 1f) {
            pliego = destino;
            destino = -1;
        }
        // el fondo: casi negro con la vineta roja de la Sombra
        g.fill(0, 0, width, height, 0xE8070306);
        Pix.mezcla();
        RenderSystem.setShaderColor(0.42f, 0.03f, 0.05f, 0.8f);
        g.blit(VINIETA, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // al abrir, el libro crece un poco
        float p = Mth.clamp((Util.getMillis() - abierto) / 240f, 0f, 1f);
        float e = 0.95f + 0.05f * (1 - (1 - p) * (1 - p));
        g.pose().pushPose();
        g.pose().translate(lomo, by + bh / 2f, 0);
        g.pose().scale(e, e, 1);
        g.pose().translate(-lomo, -(by + bh / 2f), 0);
        tapa(g);
        grosor(g);
        if (!girando()) {
            pagina(g, 2 * pliego, false, mx, my);
            pagina(g, 2 * pliego + 1, true, mx, my);
        } else {
            float k = giro();
            k = k * k * (3 - 2 * k);
            int s = pliego, n = destino;
            if (dir > 0) {
                pagina(g, 2 * s, false, mx, my);
                pagina(g, 2 * n + 1, true, mx, my);
                if (k < 0.5f) hoja(g, 2 * s + 1, true, Mth.cos(k * Mth.PI), k);
                else hoja(g, 2 * n, false, -Mth.cos(k * Mth.PI), k);
            } else {
                pagina(g, 2 * n, false, mx, my);
                pagina(g, 2 * s + 1, true, mx, my);
                if (k < 0.5f) hoja(g, 2 * s, false, Mth.cos(k * Mth.PI), k);
                else hoja(g, 2 * n + 1, true, -Mth.cos(k * Mth.PI), k);
            }
        }
        lomo(g);
        cinta(g);
        g.pose().popPose();
        // las teclas, abajo
        int ty = height - 15;
        Component pasar = Component.translatable("darkcraft.codice.pasar"), cerrar = Component.translatable("darkcraft.menu.tecla.cerrar");
        Component flechas = Component.literal("◀ ▶");
        int total = Panel.anchoTecla(font, flechas, pasar) + 12 + Panel.anchoTecla(font, Component.literal("Esc"), cerrar);
        int tx = (width - total) / 2;
        tx = Panel.tecla(g, font, flechas, pasar, tx, ty, 0xFFB8A888) + 12;
        Panel.tecla(g, font, Component.literal("Esc"), cerrar, tx, ty, 0xFFB8A888);
        Pix.blanco();
        Pix.mezcla();
        if (!girando()) tooltipReceta(g, mx, my);
    }

    /** La tapa de cuero con su filete de oro (nueve trozos) y el cuero repetido por dentro. */
    private void tapa(GuiGraphics g) {
        Pix.mezcla();
        int e = 20;
        g.blit(CUERO, bx + e, by + e, 0, 0, bw - 2 * e, bh - 2 * e, 64, 64);
        nueve(g, bx, by, bw, bh, e);
    }

    private void nueve(GuiGraphics g, int x, int y, int w, int h, int e) {
        trozo(g, CodiceTex.TAPA_AI, x, y);
        trozo(g, CodiceTex.TAPA_AD, x + w - e, y);
        trozo(g, CodiceTex.TAPA_BI, x, y + h - e);
        trozo(g, CodiceTex.TAPA_BD, x + w - e, y + h - e);
        mosaico(g, CodiceTex.TAPA_A, x + e, y, w - 2 * e, e);
        mosaico(g, CodiceTex.TAPA_B, x + e, y + h - e, w - 2 * e, e);
        mosaico(g, CodiceTex.TAPA_I, x, y + e, e, h - 2 * e);
        mosaico(g, CodiceTex.TAPA_D, x + w - e, y + e, e, h - 2 * e);
    }

    private static void trozo(GuiGraphics g, CodiceTex.T t, int x, int y) {
        g.blit(CodiceTex.TEX, x, y, t.u(), t.v(), t.w(), t.h(), CodiceTex.TW, CodiceTex.TH);
    }

    private static void mosaico(GuiGraphics g, CodiceTex.T t, int x, int y, int w, int h) {
        for (int j = 0; j < h; j += t.h())
            for (int i = 0; i < w; i += t.w())
                g.blit(CodiceTex.TEX, x + i, y + j, t.u(), t.v(), Math.min(t.w(), w - i), Math.min(t.h(), h - j), CodiceTex.TW, CodiceTex.TH);
    }

    /** El canto de las hojas por fuera de las dos paginas y por abajo (el libro es gordo). */
    private void grosor(GuiGraphics g) {
        for (int k = 1; k <= 3; k++) {
            int c = k % 2 == 1 ? 0xFF7A6646 : 0xFF5A4630;
            g.fill(izqX - k, pagY + k, izqX, pagY + pagH + k, c);
            g.fill(derX + pagW, pagY + k, derX + pagW + k, pagY + pagH + k, c);
            g.fill(izqX - k, pagY + pagH, derX + pagW + k, pagY + pagH + k, c);
        }
    }

    /** La sombra del lomo en medio y la costura. */
    private void lomo(GuiGraphics g) {
        for (int i = 0; i < 14; i++) {
            int a = Math.round(120 * (1 - i / 14f) * (1 - i / 14f));
            g.fill(lomo - 1 - i, pagY, lomo - i, pagY + pagH, a << 24 | 0x140a04);
            g.fill(lomo + i, pagY, lomo + i + 1, pagY + pagH, a << 24 | 0x140a04);
        }
        g.fill(lomo - 1, pagY, lomo + 1, pagY + pagH, 0xC0140A04);
        for (int y = pagY + 6; y < pagY + pagH - 6; y += 9) g.fill(lomo - 1, y, lomo + 1, y + 3, 0xFF6E1616);
    }

    /** La cinta roja: cuelga del canto de arriba por el lomo y asoma por abajo. */
    private void cinta(GuiGraphics g) {
        Pix.mezcla();
        CodiceTex.T c = CodiceTex.CINTA;
        int x = lomo - c.w() / 2, y0 = by + 3, y1 = pagY + pagH + 9;
        trozo(g, new CodiceTex.T(c.u(), c.v(), c.w(), 4), x, y0);
        for (int y = y0 + 4; y < y1 - 6; y += 40)
            g.blit(CodiceTex.TEX, x, y, c.u(), c.v() + 8, c.w(), Math.min(40, y1 - 6 - y), CodiceTex.TW, CodiceTex.TH);
        trozo(g, new CodiceTex.T(c.u(), c.v() + c.h() - 6, c.w(), 6), x, y1 - 6);
    }

    /** La hoja que gira: la pagina {@code i} estrechada sobre el lomo ({@code ancho} de 0 a 1) y oscurecida al levantarse. */
    private void hoja(GuiGraphics g, int i, boolean derecha, float ancho, float k) {
        if (ancho < 0.02f) return;
        g.pose().pushPose();
        g.pose().translate(lomo, 0, 50);
        g.pose().scale(ancho, 1f, 1f);
        g.pose().translate(-lomo, 0, 0);
        pagina(g, i, derecha, -1, -1);
        int a = Math.round(150 * Mth.sin(k * Mth.PI));
        int x0 = derecha ? derX : izqX;
        g.fill(x0, pagY, x0 + pagW, pagY + pagH, a << 24);
        g.pose().popPose();
        // la sombra que echa la hoja sobre la pagina de debajo
        int sw = Math.round(pagW * ancho);
        int borde = derecha ? lomo + sw : lomo - sw;
        for (int j = 0; j < 10; j++) {
            int al = Math.round(90 * Mth.sin(k * Mth.PI) * (1 - j / 10f));
            int x = derecha ? borde + j : borde - 1 - j;
            if (derecha ? x < derX + pagW : x >= izqX) g.fill(x, pagY, x + 1, pagY + pagH, al << 24);
        }
    }

    /** Una pagina: el pergamino con sus bordes, las esquinas y lo maquetado. */
    private void pagina(GuiGraphics g, int i, boolean derecha, int mx, int my) {
        int x0 = derecha ? derX : izqX;
        Pix.mezcla();
        g.blit(PERGAMINO, x0, pagY, derecha ? 101 + i * 37 : i * 37, i * 53, pagW, pagH, 256, 256);
        // los bordes de fuera, un poco quemados
        for (int j = 0; j < 5; j++) {
            int a = Math.round(70 * (1 - j / 5f));
            int x = derecha ? x0 + pagW - 1 - j : x0 + j;
            g.fill(x, pagY, x + 1, pagY + pagH, a << 24 | 0x2a1808);
            g.fill(x0, pagY + j, x0 + pagW, pagY + j + 1, (a / 2) << 24 | 0x2a1808);
            g.fill(x0, pagY + pagH - 1 - j, x0 + pagW, pagY + pagH - j, a << 24 | 0x2a1808);
        }
        // la filigrana de las esquinas de fuera
        esquina(g, derecha ? x0 + pagW - 4 : x0 + 4, pagY + 4, derecha, false);
        esquina(g, derecha ? x0 + pagW - 4 : x0 + 4, pagY + pagH - 4, derecha, true);
        if (i < 0 || i >= paginas.size()) return;
        int tx = x0 + PAD, ty = pagY + PAD_ARRIBA;
        for (Elem e : paginas.get(i)) dibujar(g, e, tx, ty, mx, my);
        // el numero de la pagina
        if (!paginas.get(i).isEmpty() || i > 0) {
            String n = "– " + (i + 1) + " –";
            g.drawString(font, n, x0 + (pagW - font.width(n)) / 2, pagY + pagH - 13, PALIDA, false);
        }
    }

    private void esquina(GuiGraphics g, int x, int y, boolean espejoH, boolean espejoV) {
        Pix.mezcla();
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(espejoH ? -1 : 1, espejoV ? -1 : 1, 1);
        trozo(g, CodiceTex.ESQUINA, 0, 0);
        g.pose().popPose();
    }

    private void dibujar(GuiGraphics g, Elem e, int tx, int ty, int mx, int my) {
        int x = tx + e.x(), y = ty + e.y();
        switch (e.tipo()) {
            case E_TEXTO -> {
                if (e.extra() == 1 && e.texto().startsWith("¶")) {
                    g.drawString(font, "¶", x, y, RUBRICA, false);
                    g.drawString(font, e.texto().substring(1), x + font.width("¶"), y, e.color(), false);
                } else g.drawString(font, e.texto(), x, y, e.color(), false);
            }
            case E_MINIATURA -> {
                Pix.mezcla();
                int i = e.extra();
                g.blit(MINIATURAS, x, y, (i % 4) * MIN_W, (i / 4) * MIN_H, MIN_W, MIN_H, 512, MINI_ATLAS_H);
                marco(g, x - MARCO, y - MARCO, MIN_W + 2 * MARCO, MIN_H + 2 * MARCO);
            }
            case E_CABECERA -> {
                Component c = Component.literal(e.texto());
                Letra.mediana(g, font, c, x - Letra.anchoM(font, c) / 2, y, e.color(), 0x403A2010);
            }
            case E_TITULO -> {
                Component c = Component.literal(e.texto()).withStyle(ChatFormatting.ITALIC);
                g.drawString(font, c, x - font.width(c) / 2, y, e.color(), false);
            }
            case E_AUTOR -> {
                Component c = Component.literal(e.texto()).withStyle(ChatFormatting.ITALIC);
                g.drawString(font, c, x - font.width(c) / 2, y, e.color(), false);
            }
            case E_ENCABEZADO -> g.drawString(font, Component.literal(e.texto()).withStyle(ChatFormatting.BOLD), x, y, e.color(), false);
            case E_FLORON -> {
                Pix.mezcla();
                trozo(g, CodiceTex.FLORON, x, y);
            }
            case E_FIN -> {
                Pix.mezcla();
                trozo(g, CodiceTex.FIN, x, y);
            }
            case E_CAPITAL -> {
                Pix.mezcla();
                CodiceTex.T marco = switch (e.extra()) {
                    case SOMBRA -> CodiceTex.CAP_NEGRO;
                    case PERDIDO -> CodiceTex.CAP_AZUL;
                    case DIARIO -> CodiceTex.CAP_VERDE;
                    default -> CodiceTex.CAP_ROJO;
                };
                trozo(g, marco, x, y);
                Component c = Component.literal(e.texto());
                int col = e.extra() == SOMBRA ? 0xFFE04848 : 0xFFF6E3A0;
                Letra.grande(g, font, c, x + (CAPITAL - Letra.anchoG(font, c)) / 2 + 1, y + (CAPITAL - Letra.ALTO_G) / 2, col, 0xFF1A0A04);
            }
            case E_SELLO -> {
                boolean sobre = mx >= x - 16 && mx < x + 16 && my >= y - 3 && my < y + 39;
                float lat = 1f + 0.06f * Mth.sin((Util.getMillis() - abierto) / 260f);
                float l = 24 * (sobre ? 1.15f : lat);
                Pix.mezcla();
                EspejoHud.icono(g, 28, x, y + 13, l);
                Component bt = Component.translatable("darkcraft.acertijo.boton");
                g.drawString(font, bt, x - font.width(bt) / 2, y + 29, sobre ? 0xFFA01818 : PALIDA, false);
            }
            case E_RECETA -> receta(g, x, y);
            default -> {
            }
        }
    }

    public static final int MINI_W = MIN_W, MINI_H = MIN_H, MINI_MARCO = MARCO;

    /** Una miniatura con su marco de oro (x, y: donde empieza la pintura, sin el marco). */
    public static void miniatura(GuiGraphics g, int i, int x, int y) {
        Pix.mezcla();
        g.blit(MINIATURAS, x, y, (i % 4) * MIN_W, (i / 4) * MIN_H, MIN_W, MIN_H, 512, MINI_ATLAS_H);
        marco(g, x - MARCO, y - MARCO, MIN_W + 2 * MARCO, MIN_H + 2 * MARCO);
    }

    /** El marco de oro de la miniatura (nueve trozos de 6). */
    private static void marco(GuiGraphics g, int x, int y, int w, int h) {
        int e = MARCO;
        trozo(g, CodiceTex.MARCO_AI, x, y);
        trozo(g, CodiceTex.MARCO_AD, x + w - e, y);
        trozo(g, CodiceTex.MARCO_BI, x, y + h - e);
        trozo(g, CodiceTex.MARCO_BD, x + w - e, y + h - e);
        mosaico(g, CodiceTex.MARCO_A, x + e, y, w - 2 * e, e);
        mosaico(g, CodiceTex.MARCO_B, x + e, y + h - e, w - 2 * e, e);
        mosaico(g, CodiceTex.MARCO_I, x, y + e, e, h - 2 * e);
        mosaico(g, CodiceTex.MARCO_D, x + w - e, y + e, e, h - 2 * e);
    }

    // ------------------------------------------------------------------ la receta del acertijo

    private int recetaX = -1, recetaY = -1;

    private static ItemStack[] rejilla() {
        ItemStack d = new ItemStack(Items.DIAMOND), n = new ItemStack(Items.NETHERITE_INGOT), k = new ItemStack(DarkCraftMod.LLAVE_HUESO.get());
        return new ItemStack[]{d, n, d, n, k, n, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    }

    /** La forja dibujada: la mesa de crafteo con lo que lleva y el amuleto que sale, y la cuenta en palabras. */
    private void receta(GuiGraphics g, int x, int y) {
        int w = ancho();
        Component tit = Component.translatable("darkcraft.acertijo.receta");
        Letra.mediana(g, font, tit, x + (w - Letra.anchoM(font, tit)) / 2, y + 2, 0xFF5A0A0E, 0x403A2010);
        int c = 20, gx = x + (w - (3 * c + 48)) / 2, gy = y + 26;
        recetaX = gx;
        recetaY = gy;
        g.fill(gx - 3, gy - 3, gx + 3 * c + 1, gy + 3 * c + 1, 0x40301808);
        ItemStack[] r = rejilla();
        for (int i = 0; i < 9; i++) {
            int sx = gx + (i % 3) * c, sy = gy + (i / 3) * c;
            g.fill(sx, sy, sx + c - 2, sy + c - 2, 0x30000000);
            g.fill(sx, sy + c - 3, sx + c - 2, sy + c - 2, 0x40000000);
            if (!r[i].isEmpty()) g.renderItem(r[i], sx + 1, sy + 1);
        }
        int ax = gx + 3 * c + 6, ay = gy + c + 4;
        g.fill(ax, ay + 4, ax + 14, ay + 6, 0xFF5A2A10);
        for (int k = 0; k < 5; k++) g.fill(ax + 13 - k, ay + 5 - k, ax + 14 - k, ay + 6 + k, 0xFF5A2A10);
        int rx = ax + 20, ry = gy + c - 1;
        g.fill(rx - 3, ry - 3, rx + 21, ry + 21, 0x50701010);
        g.renderItem(new ItemStack(DarkCraftMod.AMULETO_HUESO.get()), rx + 1, ry + 1);
        Pix.mezcla();
        int yy = gy + 3 * c + 12;
        for (FormattedCharSequence l : font.split(Component.translatable("darkcraft.acertijo.lista"), w)) {
            g.drawString(font, l, x, yy, TINTA_SOMBRA, false);
            yy += LINEA;
        }
    }

    private void tooltipReceta(GuiGraphics g, int mx, int my) {
        if (libro == null || !libro.acertijo || !receta || recetaX < 0) return;
        boolean visible = false;
        for (int lado = 0; lado < 2; lado++) {
            int i = 2 * pliego + lado;
            if (i < paginas.size()) for (Elem e : paginas.get(i)) if (e.tipo() == E_RECETA) visible = true;
        }
        if (!visible) return;
        int c = 20;
        ItemStack[] r = rejilla();
        for (int i = 0; i < 9; i++) {
            int sx = recetaX + (i % 3) * c, sy = recetaY + (i / 3) * c;
            if (!r[i].isEmpty() && mx >= sx && mx < sx + c - 2 && my >= sy && my < sy + c - 2) g.renderTooltip(font, r[i], mx, my);
        }
        int rx = recetaX + 3 * c + 26, ry = recetaY + c - 1;
        if (mx >= rx - 3 && mx < rx + 21 && my >= ry - 3 && my < ry + 21) g.renderTooltip(font, new ItemStack(DarkCraftMod.AMULETO_HUESO.get()), mx, my);
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    public String resumen() {
        return "codice " + (libro == null ? "(esperando el libro)" : libro.id) + " pliego " + (pliego + 1) + "/" + pliegos() + " paginas " + paginas.size()
                + (girando() ? " (girando)" : "") + (receta ? " receta" : "") + (atril != null ? " atril" : "")
                + String.format(java.util.Locale.ROOT, " pagina %dx%d texto %dx%d", pagW, pagH, ancho(), alto());
    }

    /** Ir a un pliego (sin animacion) o, con {@code animar}, pasar una pagina hacia alli. */
    public void probar(int nuevo, boolean animar) {
        congelado = -1f;
        nuevo = Mth.clamp(nuevo, 0, pliegos() - 1);
        if (animar && nuevo != pliego) {
            destino = nuevo;
            dir = nuevo > pliego ? 1 : -1;
            giro0 = Util.getMillis();
        } else {
            pliego = nuevo;
            destino = -1;
        }
    }

    /** Pasa a la pagina siguiente y deja la hoja quieta a medio girar ({@code k} de 0 a 1) para la captura. */
    public void probarGiro(float k) {
        if (pliego + 1 >= pliegos()) return;
        destino = pliego + 1;
        dir = 1;
        giro0 = Util.getMillis();
        congelado = Mth.clamp(k, 0f, 0.99f);
    }

    /** Pulsa el sello del acertijo y va (sin animacion) a la pagina de la receta. */
    public void probarReceta() {
        if (libro != null && libro.acertijo && !receta) verReceta();
        probar(pliegos() - 1, false);
    }

    public int pliegosTotal() {
        return pliegos();
    }
}
