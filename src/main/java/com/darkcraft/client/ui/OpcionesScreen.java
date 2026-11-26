package com.darkcraft.client.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Las opciones (parte 2f), en una losa del mod: los apartados a la izquierda y, a la derecha, las opciones del que se
 * elige (los botones y deslizadores son los de Minecraft, pero con la piedra y el oro del mod: tools/ui/vanilla.py).
 * Los apartados con una flecha abren su propia pantalla: graficos (o la de Embeddium si esta), idioma, recursos, la
 * piel, los shaders de Oculus y Distant Horizons (si estan) y los mods.
 */
public class OpcionesScreen extends Screen {
    private static final int ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, CLARO = 0xFFEADBC4, APAGADO = 0xFFB8A888, GRIS = 0xFF8A7F6C;
    private static final int FILA = 24;

    /** Un apartado: o trae sus opciones (se ven a la derecha) o abre otra pantalla. */
    private record Seccion(String id, Runnable opciones, Supplier<Screen> abre) {
        boolean abreOtra() {
            return abre != null;
        }
    }

    private final Screen volver;
    private final List<Seccion> secciones = new ArrayList<>();
    private final List<AbstractWidget[]> filas = new ArrayList<>();
    private static int recordada;
    private int sel, scroll, t;
    private int encima = -1;

    public OpcionesScreen(Screen volver) {
        super(Component.translatable("darkcraft.opciones.titulo"));
        this.volver = volver;
        this.sel = recordada;
    }

    // ------------------------------------------------------------------ medidas

    private int ancho() {
        return Math.min(width - 12, 470);
    }

    private int alto() {
        return Math.min(height - 12, 300);
    }

    private int x0() {
        return (width - ancho()) / 2;
    }

    private int y0() {
        return (height - alto()) / 2;
    }

    private int lw() {
        return Mth.clamp(ancho() * 28 / 100, 104, 132);
    }

    private int lx() {
        return x0() + 10;
    }

    private int ly() {
        return y0() + 38;
    }

    private int paso() {
        return Mth.clamp((alto() - 38 - 34) / Math.max(1, secciones.size()), 12, 18);
    }

    private int px() {
        return lx() + lw() + 8;
    }

    private int pw() {
        return x0() + ancho() - 10 - px();
    }

    private int py() {
        return y0() + 36;
    }

    private int ph() {
        return alto() - 36 - 32;
    }

    private int visibles() {
        return Math.max(1, (ph() - 26) / FILA);
    }

    private int volverX() {
        return (width - 120) / 2;
    }

    private int volverY() {
        return y0() + alto() - 27;
    }

    // ------------------------------------------------------------------ los apartados

    @Override
    protected void init() {
        Options o = minecraft.options;
        secciones.clear();
        secciones.add(new Seccion("general", this::general, null));
        secciones.add(new Seccion("sonido", this::sonido, null));
        // (con Embeddium, que va dentro de Dark Craft, la suya: la de Minecraft no trae sus opciones)
        Supplier<Screen> embeddium = deMod("embeddium");
        secciones.add(new Seccion("graficos", null, embeddium != null ? embeddium : () -> new VideoSettingsScreen(this, o)));
        secciones.add(new Seccion("controles", this::controles, null));
        secciones.add(new Seccion("chat", this::chat, null));
        secciones.add(new Seccion("accesibilidad", this::accesibilidad, null));
        secciones.add(new Seccion("idioma", null, () -> new LanguageSelectScreen(this, o, minecraft.getLanguageManager())));
        secciones.add(new Seccion("recursos", null, () -> new PackSelectionScreen(minecraft.getResourcePackRepository(), repo -> {
            o.updateResourcePacks(repo);
            minecraft.setScreen(this);
        }, minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title"))));
        secciones.add(new Seccion("aspecto", null, () -> new SkinCustomizationScreen(this, o)));
        Supplier<Screen> shaders = deMod("oculus");
        if (shaders != null) secciones.add(new Seccion("shaders", null, shaders));
        Supplier<Screen> lejania = deMod("distanthorizons");
        if (lejania != null) secciones.add(new Seccion("lejania", null, lejania));
        secciones.add(new Seccion("mods", null, () -> new ModsScreen(this)));
        sel = Mth.clamp(sel, 0, secciones.size() - 1);
        if (secciones.get(sel).abreOtra()) sel = Mth.clamp(recordada, 0, secciones.size() - 1);
        if (secciones.get(sel).abreOtra()) sel = 0;
        construir();
    }

    /** La pantalla de opciones de un mod (si esta y tiene). */
    private Supplier<Screen> deMod(String id) {
        if (!ModList.get().isLoaded(id)) return null;
        for (IModInfo m : ModList.get().getMods())
            if (m.getModId().equals(id)) {
                var f = ConfigScreenHandler.getScreenFactoryFor(m);
                if (f.isPresent()) return () -> f.get().apply(minecraft, this);
            }
        return null;
    }

    private void construir() {
        clearWidgets();
        filas.clear();
        scroll = 0;
        Seccion s = secciones.get(sel);
        if (s.opciones() != null) s.opciones().run();
        for (AbstractWidget[] f : filas)
            for (AbstractWidget w : f) {
                // el texto en oro, como los botones del mod (los apagados se quedan grises)
                if (w.active) w.setFGColor(0xE2BF62);
                addRenderableWidget(w);
            }
        colocar();
    }

    private int anchoGrande() {
        return pw() - 18;
    }

    private int anchoMedio() {
        return (anchoGrande() - 4) / 2;
    }

    private void grande(AbstractWidget w) {
        w.setWidth(anchoGrande());
        filas.add(new AbstractWidget[]{w});
    }

    /** De dos en dos (la ultima sola si son impares). */
    private void opciones(OptionInstance<?>... os) {
        Options o = minecraft.options;
        for (int i = 0; i < os.length; i += 2) {
            if (i + 1 < os.length)
                filas.add(new AbstractWidget[]{os[i].createButton(o, 0, 0, anchoMedio()), os[i + 1].createButton(o, 0, 0, anchoMedio())});
            else filas.add(new AbstractWidget[]{os[i].createButton(o, 0, 0, anchoMedio())});
        }
    }

    private void general() {
        Options o = minecraft.options;
        if (minecraft.level != null && minecraft.hasSingleplayerServer()) {
            CycleButton<Difficulty> d = OptionsScreen.createDifficultyButton(0, 0, "options.difficulty", minecraft);
            d.active = !minecraft.level.getLevelData().isHardcore() && !minecraft.level.getLevelData().isDifficultyLocked();
            grande(d);
        }
        grande(o.fov().createButton(o, 0, 0, anchoGrande()));
        opciones(o.gamma(), o.showSubtitles(), o.bobView(), o.attackIndicator(), o.screenEffectScale(), o.fovEffectScale(),
                o.damageTiltStrength(), o.darknessEffectScale(), o.hideLightningFlash(), o.showAutosaveIndicator());
    }

    private void sonido() {
        Options o = minecraft.options;
        grande(o.getSoundSourceOptionInstance(SoundSource.MASTER).createButton(o, 0, 0, anchoGrande()));
        List<OptionInstance<?>> v = new ArrayList<>();
        for (SoundSource s : SoundSource.values()) if (s != SoundSource.MASTER) v.add(o.getSoundSourceOptionInstance(s));
        opciones(v.toArray(OptionInstance<?>[]::new));
        grande(o.soundDevice().createButton(o, 0, 0, anchoGrande()));
        opciones(o.showSubtitles(), o.directionalAudio());
    }

    private void controles() {
        Options o = minecraft.options;
        grande(Button.builder(Component.translatable("darkcraft.opciones.teclas"), b -> minecraft.setScreen(new KeyBindsScreen(this, o)))
                .bounds(0, 0, anchoGrande(), 20).build());
        List<OptionInstance<?>> v = new ArrayList<>(List.of(o.sensitivity(), o.invertYMouse(), o.autoJump(), o.toggleCrouch(),
                o.toggleSprint(), o.mouseWheelSensitivity(), o.discreteMouseScroll()));
        if (InputConstants.isRawMouseInputSupported()) v.add(o.rawMouseInput());
        opciones(v.toArray(OptionInstance<?>[]::new));
    }

    private void chat() {
        Options o = minecraft.options;
        opciones(o.chatVisibility(), o.chatColors(), o.chatLinks(), o.chatLinksPrompt(), o.chatOpacity(), o.textBackgroundOpacity(),
                o.chatScale(), o.chatLineSpacing(), o.chatDelay(), o.chatWidth(), o.chatHeightFocused(), o.chatHeightUnfocused(),
                o.narrator(), o.autoSuggestions(), o.hideMatchedNames(), o.reducedDebugInfo(), o.onlyShowSecureChat());
    }

    private void accesibilidad() {
        Options o = minecraft.options;
        List<OptionInstance<?>> v = new ArrayList<>(List.of(o.narrator(), o.showSubtitles()));
        if (minecraft.getResourcePackRepository().getAvailableIds().contains("high_contrast")) v.add(o.highContrast());
        v.addAll(List.of(o.autoJump(), o.textBackgroundOpacity(), o.backgroundForChatOnly(), o.chatOpacity(), o.chatLineSpacing(),
                o.chatDelay(), o.notificationDisplayTime(), o.toggleCrouch(), o.toggleSprint(), o.screenEffectScale(),
                o.fovEffectScale(), o.darknessEffectScale(), o.damageTiltStrength(), o.glintSpeed(), o.glintStrength(),
                o.hideLightningFlash(), o.darkMojangStudiosBackground(), o.panoramaSpeed()));
        opciones(v.toArray(OptionInstance<?>[]::new));
    }

    /** Pone cada fila en su sitio; las que no caben (por el desplazamiento) no se ven ni se pueden pulsar. */
    private void colocar() {
        int vis = visibles();
        scroll = Mth.clamp(scroll, 0, Math.max(0, filas.size() - vis));
        int x = px() + 9, y = py() + 22;
        for (int r = 0; r < filas.size(); r++) {
            AbstractWidget[] f = filas.get(r);
            boolean ve = r >= scroll && r < scroll + vis;
            int fy = y + (r - scroll) * FILA;
            for (int i = 0; i < f.length; i++) {
                f[i].visible = ve;
                f[i].setX(x + i * (anchoMedio() + 4));
                f[i].setY(fy);
            }
        }
    }

    // ------------------------------------------------------------------ dibujar

    @Override
    public void tick() {
        t++;
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        if (minecraft.level == null) TituloScreen.fondoMenus(g, width, height);
        else super.renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = x0(), y = y0(), w = ancho(), h = alto();
        Pix.blanco();
        Panel.losa(g, x, y, w, h);
        Letra.grande(g, font, title, (width - Letra.anchoG(font, title)) / 2, y + 9, 0xFFE9C766, 0xFF050408);
        Panel.raya(g, x + 12, x + w - 12, y + 31);
        // los apartados
        encima = enSeccion(mx, my);
        int paso = paso();
        for (int i = 0; i < secciones.size(); i++) {
            Seccion s = secciones.get(i);
            int iy = ly() + i * paso;
            boolean elegida = i == sel;
            if (elegida) {
                Pix.blanco();
                Panel.fila(g, lx(), iy + (paso - 18) / 2, lw());
            } else if (i == encima) {
                g.fill(lx() + 2, iy + 1, lx() + lw() - 2, iy + paso - 1, 0x40E2BF62);
            }
            Component c = Component.translatable("darkcraft.opciones." + s.id());
            int ty = iy + (paso - 8) / 2;
            g.drawString(font, c, lx() + 8, ty, elegida ? LUZ : i == encima ? CLARO : ORO, true);
            if (s.abreOtra()) g.drawString(font, "›", lx() + lw() - 10, ty, elegida ? LUZ : APAGADO, true);
        }
        // el panel de la derecha
        Pix.blanco();
        Panel.hueco(g, px(), py(), pw(), ph());
        Seccion s = secciones.get(sel);
        Component desc = Component.translatable("darkcraft.opciones." + s.id() + ".desc");
        List<FormattedCharSequence> lineas = font.split(desc, pw() - 20);
        if (s.abreOtra()) {
            int ly = py() + 12;
            for (FormattedCharSequence l : lineas) {
                g.drawString(font, l, px() + 10, ly, CLARO, true);
                ly += 11;
            }
        } else if (!lineas.isEmpty()) {
            g.drawString(font, lineas.get(0), px() + 10, py() + 7, APAGADO, true);
        }
        // volver
        boolean luz = enVolver(mx, my);
        Pix.blanco();
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, volverX(), volverY(), 120);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, volverX(), volverY(), 120);
        Pix.blanco();
        Component v = Component.translatable("darkcraft.opciones.volver");
        g.drawString(font, v, volverX() + (120 - font.width(v)) / 2, volverY() + 6, luz ? LUZ : ORO, true);
        Pix.mezcla();
        super.render(g, mx, my, pt);
        barra(g);
        Pix.blanco();
        Pix.mezcla();
    }

    /** La barra de desplazamiento de la derecha (si no caben todas las filas). */
    private void barra(GuiGraphics g) {
        int vis = visibles();
        if (filas.size() <= vis) return;
        int bx = px() + pw() - 7, by = py() + 22, bh = vis * FILA - 4;
        g.fill(bx, by, bx + 3, by + bh, 0xFF0B090F);
        int max = filas.size() - vis;
        int th = Math.max(10, bh * vis / filas.size());
        int ty = by + (bh - th) * scroll / Math.max(1, max);
        g.fill(bx, ty, bx + 3, ty + th, 0xFFC0963C);
        g.fill(bx, ty, bx + 1, ty + th, 0xFFFFE38A);
    }

    // ------------------------------------------------------------------ manejo

    private int enSeccion(double mx, double my) {
        if (mx < lx() || mx >= lx() + lw()) return -1;
        int paso = paso();
        for (int i = 0; i < secciones.size(); i++) {
            int iy = ly() + i * paso;
            if (my >= iy && my < iy + paso) return i;
        }
        return -1;
    }

    private boolean enVolver(double mx, double my) {
        return mx >= volverX() && mx < volverX() + 120 && my >= volverY() && my < volverY() + 20;
    }

    private void elegir(int i) {
        if (i < 0 || i >= secciones.size()) return;
        Seccion s = secciones.get(i);
        Panel.acepta();
        if (s.abreOtra()) {
            minecraft.setScreen(s.abre().get());
            return;
        }
        if (i != sel) {
            sel = i;
            recordada = i;
            construir();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b == 0) {
            int i = enSeccion(mx, my);
            if (i >= 0) {
                elegir(i);
                return true;
            }
            if (enVolver(mx, my)) {
                Panel.cancela();
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        if (mx >= px() && mx < px() + pw() && my >= py() && my < py() + ph()) {
            int antes = scroll;
            scroll -= (int) Math.signum(d);
            colocar();
            if (scroll != antes) Panel.mueve();
            return true;
        }
        return super.mouseScrolled(mx, my, d);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int i = enSeccion(mx, my);
        if (i >= 0 && i != encima) Panel.mueve();
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (tecla == GLFW.GLFW_KEY_UP || tecla == GLFW.GLFW_KEY_DOWN) {
            int n = secciones.size();
            int i = Math.floorMod(sel + (tecla == GLFW.GLFW_KEY_UP ? -1 : 1), n);
            // con el teclado los apartados que abren otra pantalla tambien se eligen (se abren con Intro)
            sel = i;
            Panel.mueve();
            if (!secciones.get(i).abreOtra()) {
                recordada = i;
                construir();
            } else {
                clearWidgets();
                filas.clear();
            }
            return true;
        }
        if ((tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER) && secciones.get(sel).abreOtra()) {
            elegir(sel);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_PAGE_UP || tecla == GLFW.GLFW_KEY_PAGE_DOWN) {
            scroll += tecla == GLFW.GLFW_KEY_PAGE_UP ? -visibles() : visibles();
            colocar();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public void removed() {
        minecraft.options.save();
    }

    @Override
    public void onClose() {
        minecraft.options.save();
        minecraft.setScreen(volver);
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    /** Elige un apartado por su id (los que abren otra pantalla, la abren). */
    public boolean probar(String id) {
        for (int i = 0; i < secciones.size(); i++)
            if (secciones.get(i).id().equals(id)) {
                elegir(i);
                return true;
            }
        return false;
    }

    public void probarScroll(int filas) {
        scroll = filas;
        colocar();
    }

    public String resumen() {
        StringBuilder b = new StringBuilder("opciones:");
        for (Seccion s : secciones) b.append(' ').append(s.id()).append(s.abreOtra() ? ">" : "");
        return b + " | elegida " + secciones.get(sel).id() + " filas=" + filas.size() + " visibles=" + visibles();
    }
}
