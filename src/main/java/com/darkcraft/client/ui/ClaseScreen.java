package com.darkcraft.client.ui;

import com.darkcraft.net.Net;
import com.darkcraft.rol.Atributo;
import com.darkcraft.rol.Clase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Elegir la clase con la que se empieza (parte 2f-3): las diez a la izquierda, en el centro el personaje con lo que
 * lleva esa clase (girando) y a la derecha lo que es, su nivel, sus ocho atributos, los frascos y con que empieza.
 * "Elegir" pregunta antes de empezar. La primera vez que se entra en un mundo no se puede cerrar sin elegir.
 */
public class ClaseScreen extends Screen {
    private static final Clase[] CLASES = Clase.values();
    private static final MenuTex.T[] ICONOS = {MenuTex.AT_VIGOR, MenuTex.AT_MENTE, MenuTex.AT_AGUANTE, MenuTex.AT_FUERZA, MenuTex.AT_DESTREZA,
            MenuTex.AT_INTELIGENCIA, MenuTex.AT_FE, MenuTex.AT_ARCANO};
    private static final int ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, CLARO = 0xFFEADBC4, APAGADO = 0xFFB8A888, GRIS = 0xFF6F6656,
            VERDE = 0xFF9AD27A, ROJO = 0xFFE07060;

    private final boolean forzada;
    private int sel, t, encima = -1;
    private boolean confirmando, enviada;
    private int confSel;
    private RemotePlayer muneco;
    private int munecoDe = -1;

    public static void abrir(boolean forzada) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new ClaseScreen(forzada));
        Panel.abre();
    }

    public ClaseScreen(boolean forzada) {
        super(Component.translatable("darkcraft.clase.titulo"));
        this.forzada = forzada;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !forzada;
    }

    @Override
    public void tick() {
        t++;
    }

    // ------------------------------------------------------------------ medidas

    private int ancho() {
        return Math.min(width - 10, 540);
    }

    private int alto() {
        return Math.min(height - 10, 330);
    }

    private int x0() {
        return (width - ancho()) / 2;
    }

    private int y0() {
        return (height - alto()) / 2;
    }

    private int lx() {
        return x0() + 10;
    }

    private int lw() {
        return Mth.clamp(ancho() * 26 / 100, 104, 140);
    }

    private int ly() {
        return y0() + 44;
    }

    private int lh() {
        return alto() - 44 - 34;
    }

    private int paso() {
        return Math.min(19, lh() / CLASES.length);
    }

    private int cx() {
        return lx() + lw() + 6;
    }

    private int cw() {
        return Mth.clamp(ancho() * 24 / 100, 86, 130);
    }

    private int rx() {
        return cx() + cw() + 6;
    }

    private int rw() {
        return x0() + ancho() - 10 - rx();
    }

    private int botonX() {
        return (width - 130) / 2;
    }

    private int botonY() {
        return y0() + alto() - 27;
    }

    // ------------------------------------------------------------------ el personaje con lo de la clase

    private RemotePlayer muneco() {
        if (minecraft.level == null || minecraft.player == null) return null;
        if (muneco == null) muneco = new RemotePlayer(minecraft.level, minecraft.player.getGameProfile());
        if (munecoDe != sel) {
            Clase.Kit k = CLASES[sel].kit();
            muneco.setItemSlot(EquipmentSlot.MAINHAND, k.derecha()[0].copy());
            muneco.setItemSlot(EquipmentSlot.OFFHAND, k.izquierda().copy());
            EquipmentSlot[] piezas = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < 4; i++) muneco.setItemSlot(piezas[i], k.armadura()[i].copy());
            munecoDe = sel;
        }
        return muneco;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void renderBackground(GuiGraphics g) {
        TituloScreen.fondoMenus(g, width, height);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = x0(), y = y0(), w = ancho(), h = alto();
        Pix.blanco();
        Panel.losa(g, x, y, w, h);
        Letra.grande(g, font, title, (width - Letra.anchoG(font, title)) / 2, y + 8, 0xFFE9C766, 0xFF050408);
        Component sub = Component.translatable("darkcraft.clase.subtitulo");
        String s = sub.getString();
        while (font.width(s) > w - 30 && s.length() > 4) s = s.substring(0, s.length() - 2);
        g.drawString(font, s, (width - font.width(s)) / 2, y + 27, APAGADO, true);
        Panel.raya(g, x + 12, x + w - 12, y + 38);
        lista(g, mx, my);
        figura(g, pt);
        ficha(g, mx, my);
        // abajo: elegir y las teclas
        boolean luz = !confirmando && mx >= botonX() && mx < botonX() + 130 && my >= botonY() && my < botonY() + 20;
        boton(g, botonX(), botonY(), 130, Component.translatable("darkcraft.clase.elegir"), luz);
        int tx = x + 14, ty = botonY() + 4;
        tx = Panel.tecla(g, font, Component.literal("↑↓"), Component.translatable("darkcraft.clase.tecla.clase"), tx, ty, APAGADO) + 8;
        if (tx + 60 < botonX())
            Panel.tecla(g, font, minecraft.options.keyInventory.getTranslatedKeyMessage(), Component.translatable("darkcraft.clase.elegir"), tx, ty, APAGADO);
        if (confirmando) confirmar(g, mx, my);
        Pix.blanco();
        Pix.mezcla();
        super.render(g, mx, my, pt);
    }

    private void lista(GuiGraphics g, int mx, int my) {
        Pix.blanco();
        Panel.hueco(g, lx(), ly(), lw(), lh());
        encima = confirmando ? -1 : enLista(mx, my);
        int paso = paso();
        for (int i = 0; i < CLASES.length; i++) {
            Clase c = CLASES[i];
            int fy = ly() + 4 + i * paso;
            if (i == sel) {
                g.fill(lx() + 2, fy - 1, lx() + lw() - 2, fy + paso - 1, 0x70300A10);
                Pix.franja(g, lx() + 2, lx() + lw() - 2, fy - 1, fy, 0xC0963C, 0.95f);
                Pix.franja(g, lx() + 2, lx() + lw() - 2, fy + paso - 2, fy + paso - 1, 0xC0963C, 0.95f);
            } else if (i == encima) {
                g.fill(lx() + 2, fy - 1, lx() + lw() - 2, fy + paso - 1, 0x30E2BF62);
            }
            Component n = Component.translatable(c.clave());
            String nivel = Integer.toString(c.nivel());
            int color = i == sel ? LUZ : i == encima ? 0xFFF0D890 : ORO;
            int nw = lw() - 14 - font.width(nivel) - 6;
            if (paso >= 15 && Letra.anchoM(font, n) <= nw) Letra.mediana(g, font, n, lx() + 7, fy + (paso - 14) / 2, color, 0xFF050408);
            else g.drawString(font, n, lx() + 7, fy + (paso - 9) / 2, color, true);
            g.drawString(font, nivel, lx() + lw() - 7 - font.width(nivel), fy + (paso - 9) / 2, i == sel ? CLARO : GRIS, true);
        }
    }

    private void figura(GuiGraphics g, float pt) {
        int x = cx(), y = ly(), w = cw(), hh = lh();
        Pix.blanco();
        Panel.hueco(g, x, y, w, hh);
        RemotePlayer p = muneco();
        if (p == null) return;
        float giro = 200f + (t + pt) * 1.2f;
        p.yBodyRot = p.yBodyRotO = giro;
        p.setYRot(giro);
        p.yRotO = giro;
        p.yHeadRot = p.yHeadRotO = giro;
        p.setXRot(0f);
        p.xRotO = 0f;
        int escala = Mth.clamp(Math.min((hh - 24) * 10 / 21, w * 10 / 13), 20, 76);
        g.enableScissor(x + 2, y + 2, x + w - 2, y + hh - 2);
        g.pose().pushPose();
        g.pose().translate(0, 0, 150);
        try {
            InventoryScreen.renderEntityInInventory(g, x + w / 2, y + hh - 12, escala, new Quaternionf().rotateZ((float) Math.PI), null, p);
        } catch (RuntimeException ex) {
            com.darkcraft.DarkCraftMod.LOGGER.debug("ClaseScreen: no se pudo dibujar el personaje: {}", ex.toString());
        } finally {
            g.pose().popPose();
            g.disableScissor();
        }
        Pix.blanco();
        Pix.mezcla();
    }

    private void ficha(GuiGraphics g, int mx, int my) {
        int x = rx(), y = ly(), w = rw(), hh = lh();
        Pix.blanco();
        Panel.hueco(g, x, y, w, hh);
        Clase c = CLASES[sel];
        int fy = y + 6;
        Component n = Component.translatable(c.clave());
        if (Letra.anchoG(font, n) <= w - 16) {
            Letra.grande(g, font, n, x + 8, fy, 0xFFE9C766, 0xFF050408);
            fy += 20;
        } else {
            Letra.mediana(g, font, n, x + 8, fy, 0xFFE9C766, 0xFF050408);
            fy += 16;
        }
        g.drawString(font, Component.translatable("darkcraft.clase.nivel", c.nivel()), x + 8, fy, APAGADO, true);
        fy += 12;
        for (FormattedCharSequence l : font.split(Component.translatable(c.clave() + ".desc"), w - 16)) {
            if (fy > y + hh - 96) break;
            g.drawString(font, l, x + 8, fy, CLARO, true);
            fy += 10;
        }
        fy += 3;
        Panel.raya(g, x + 6, x + w - 6, fy);
        fy += 5;
        // los ocho atributos, en dos columnas
        int[] a = c.atributos();
        int col = (w - 16) / 2;
        for (int i = 0; i < Atributo.N; i++) {
            int ax = x + 8 + (i / 4) * col, ay = fy + (i % 4) * 17;
            Pix.blanco();
            Panel.t(g, ICONOS[i], ax, ay);
            Component an = Component.translatable("darkcraft.atributo." + Atributo.values()[i].clave);
            String as = an.getString();
            String v = Integer.toString(a[i]);
            while (font.width(as) > col - 22 - font.width(v) - 4 && as.length() > 3) as = as.substring(0, as.length() - 1);
            g.drawString(font, as, ax + 19, ay + 4, APAGADO, true);
            int color = a[i] > Atributo.BASE ? VERDE : a[i] < Atributo.BASE ? ROJO : CLARO;
            g.drawString(font, v, ax + col - 6 - font.width(v), ay + 4, color, true);
        }
        fy += 4 * 17 + 2;
        g.drawString(font, Component.translatable("darkcraft.clase.frascos", c.frascosVida, c.frascosMana), x + 8, fy, APAGADO, true);
        fy += 11;
        // con que empieza
        g.drawString(font, Component.translatable("darkcraft.clase.equipo"), x + 8, fy, ORO, true);
        fy += 10;
        List<ItemStack> todo = c.kit().todo();
        int ix = x + 8, iy = fy;
        ItemStack bajo = ItemStack.EMPTY;
        for (ItemStack st : todo) {
            if (ix + 16 > x + w - 6) {
                ix = x + 8;
                iy += 18;
            }
            if (iy + 16 > y + hh - 2) break;
            g.renderItem(st, ix, iy);
            g.renderItemDecorations(font, st, ix, iy);
            if (!confirmando && mx >= ix && mx < ix + 16 && my >= iy && my < iy + 16) bajo = st;
            ix += 18;
        }
        if (!bajo.isEmpty()) g.renderTooltip(font, bajo, mx, my);
    }

    private void boton(GuiGraphics g, int x, int y, int w, Component texto, boolean luz) {
        Pix.blanco();
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, x, y, w);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, x, y, w);
        Pix.blanco();
        g.drawString(font, texto, x + (w - font.width(texto)) / 2, y + 6, luz ? LUZ : ORO, true);
    }

    // ------------------------------------------------------------------ la pregunta

    private int cw2() {
        return Math.min(width - 40, 250);
    }

    private int cy2() {
        return height / 2 - 40;
    }

    private int siX(int i) {
        int bw = (cw2() - 30) / 2;
        return (width - cw2()) / 2 + 10 + i * (bw + 10);
    }

    private int siY() {
        return cy2() + 50;
    }

    private void confirmar(GuiGraphics g, int mx, int my) {
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.fill(0, 0, width, height, 0x90050308);
        int w = cw2(), x = (width - w) / 2, y = cy2();
        Pix.blanco();
        Panel.losa(g, x, y, w, 80);
        Component q = Component.translatable("darkcraft.clase.pregunta", Component.translatable(CLASES[sel].clave()));
        List<FormattedCharSequence> ls = font.split(q, w - 24);
        int ty = y + 14;
        for (FormattedCharSequence l : ls) {
            g.drawString(font, l, (width - font.width(l)) / 2, ty, LUZ, true);
            ty += 11;
        }
        int bw = (w - 30) / 2;
        for (int i = 0; i < 2; i++) {
            boolean luz = i == confSel || (mx >= siX(i) && mx < siX(i) + bw && my >= siY() && my < siY() + 20);
            boton(g, siX(i), siY(), bw, Component.translatable(i == 0 ? "darkcraft.clase.si" : "darkcraft.clase.no"), luz);
        }
        g.pose().popPose();
    }

    // ------------------------------------------------------------------ manejo

    private int enLista(double mx, double my) {
        if (mx < lx() || mx >= lx() + lw()) return -1;
        int paso = paso();
        for (int i = 0; i < CLASES.length; i++) {
            int fy = ly() + 4 + i * paso;
            if (my >= fy - 1 && my < fy + paso - 1) return i;
        }
        return -1;
    }

    private void mover(int d) {
        sel = Math.floorMod(sel + d, CLASES.length);
        Panel.mueve();
    }

    private void empezar() {
        if (enviada) return;
        enviada = true;
        Panel.acepta();
        Net.CH.sendToServer(new Net.ElegirClase(CLASES[sel].id));
        minecraft.setScreen(null);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        boolean elegir = tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE
                || minecraft.options.keyInventory.matches(tecla, scan);
        if (confirmando) {
            if (tecla == GLFW.GLFW_KEY_LEFT || tecla == GLFW.GLFW_KEY_RIGHT || tecla == GLFW.GLFW_KEY_A || tecla == GLFW.GLFW_KEY_D) {
                confSel = 1 - confSel;
                Panel.mueve();
                return true;
            }
            if (elegir) {
                if (confSel == 0) empezar();
                else {
                    confirmando = false;
                    Panel.cancela();
                }
                return true;
            }
            if (tecla == GLFW.GLFW_KEY_ESCAPE) {
                confirmando = false;
                Panel.cancela();
                return true;
            }
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_UP || tecla == GLFW.GLFW_KEY_W) {
            mover(-1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || tecla == GLFW.GLFW_KEY_S) {
            mover(1);
            return true;
        }
        if (elegir) {
            confirmando = true;
            confSel = 0;
            Panel.acepta();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b != 0) return super.mouseClicked(mx, my, b);
        if (confirmando) {
            int bw = (cw2() - 30) / 2;
            for (int i = 0; i < 2; i++)
                if (mx >= siX(i) && mx < siX(i) + bw && my >= siY() && my < siY() + 20) {
                    if (i == 0) empezar();
                    else {
                        confirmando = false;
                        Panel.cancela();
                    }
                    return true;
                }
            return true;
        }
        int i = enLista(mx, my);
        if (i >= 0) {
            if (i != sel) {
                sel = i;
                Panel.mueve();
            }
            return true;
        }
        if (mx >= botonX() && mx < botonX() + 130 && my >= botonY() && my < botonY() + 20) {
            confirmando = true;
            confSel = 0;
            Panel.acepta();
            return true;
        }
        return super.mouseClicked(mx, my, b);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        if (!confirmando) mover(d > 0 ? -1 : 1);
        return true;
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    public void probarElegir(String id) {
        for (int i = 0; i < CLASES.length; i++) if (CLASES[i].id.equals(id)) sel = i;
    }

    public void probarPregunta() {
        confirmando = true;
        confSel = 0;
    }

    public void probarEmpezar() {
        empezar();
    }

    public String resumen() {
        Clase c = CLASES[sel];
        return "clase: " + c.id + " nivel " + c.nivel() + " forzada=" + forzada + (confirmando ? " (preguntando)" : "") + " | equipo "
                + c.kit().todo().size() + " cosas";
    }
}
