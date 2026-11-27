package com.darkcraft.client.ui;

import com.darkcraft.combate.Habilidad;
import com.darkcraft.dialogo.Dialogos;
import com.darkcraft.forja.CenizaGuerra;
import com.darkcraft.forja.Forja;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * La forja de Dorvan (y la de cada herrero de Morvain, parte 7af: la misma con su voz y su nombre), con dos pestanas (arriba, en gotica; TAB o clic para cambiar):
 * <ul>
 * <li><b>Mejorar</b>: a la izquierda las armas y catalizadores que se llevan (con su mejora), a la derecha el elegido:
 * lo que pega ahora y con la siguiente mejora, las piedras y los ecos que pide (en rojo lo que falta).</li>
 * <li><b>Cenizas de guerra</b>: a la izquierda las armas que admiten cenizas, a la derecha su habilidad (la suya o la de
 * su ceniza), lo que gasta y para que sirve, y debajo las cenizas que se llevan (flechas para elegir; las que no le
 * valen, apagadas; si ya lleva una, la primera opcion es quitarsela).</li>
 * </ul>
 * Abajo, lo que va diciendo Dorvan ({@link Bocadillo}): al abrir, la frase 3; al mejorar o poner una ceniza, la 4; si
 * falta algo, la 5; si ya esta al maximo, la 8. El servidor lo comprueba todo ({@link Forja#mejorar}, {@link Forja#ceniza}).
 */
public class ForjaScreen extends Screen {
    private static final int ANCHO = 396, ALTO = 182, FILA = 20, FILAS = 6, IZQ = 186, LISTA_Y = 40;
    private static final int CLARO = 0xFFEADBC4, ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, APAGADO = 0xFFB8A888, VERDE = 0xFF9BE08A, ROJO = 0xFFE0564E;
    /** El orden en que salen los huecos: mano derecha, mano izquierda, bolsa y objetos rapidos. */
    private static final int[] ORDEN;
    public static final int MEJORAR = 0, CENIZAS = 1;

    static {
        List<Integer> o = new ArrayList<>(List.of(0, 1, 2, 40, 3, 4));
        for (int i = 9; i <= 35; i++) o.add(i);
        for (int i = 5; i <= 8; i++) o.add(i);
        ORDEN = o.stream().mapToInt(Integer::intValue).toArray();
    }

    private final int entidad;
    /** De quien es la forja ("herrero" Dorvan; en Morvain, parte 7af, "hagen", "brenna", "ulrik" o "tobin") y lo que dice. */
    private final String quien;
    private final Bocadillo dorvan;
    private final List<Integer> huecos = new ArrayList<>();
    /** En la pestana de cenizas: lo que se puede elegir (-1 = quitarle la suya; si no, el hueco de una ceniza). */
    private final List<Integer> opciones = new ArrayList<>();
    private int modo = MEJORAR, sel, arriba, opcion, t, esperando, destello = -100, destelloHueco = -1;
    private boolean saludo;

    private ForjaScreen(int entidad, String quien) {
        super("herrero".equals(quien) ? Component.translatable("darkcraft.forja.titulo")
                : Component.translatable("darkcraft.forja.de", Component.translatable("darkcraft.personaje." + quien)));
        this.entidad = entidad;
        this.quien = quien;
        Dialogos.Personaje pj = Dialogos.de(quien);
        this.dorvan = new Bocadillo(pj == null ? Dialogos.HERRERO : pj);
    }

    public static void abrir(Net.AbrirForja m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String quien = mc.level != null && mc.level.getEntity(m.entidad()) instanceof com.darkcraft.personajes.Herrero h ? h.dialogo() : "herrero";
        mc.setScreen(new ForjaScreen(m.entidad(), quien));
        Panel.abre();
    }

    public static void hecho(Net.ForjaHecho m) {
        if (Minecraft.getInstance().screen instanceof ForjaScreen f && f.entidad == m.entidad()) f.resultado(m);
    }

    @Override
    protected void init() {
        leer();
        if (!saludo) {
            saludo = true;
            dorvan.decir(3);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        dorvan.parar();
        super.removed();
    }

    private boolean vale(ItemStack st) {
        return modo == MEJORAR ? Forja.mejorable(st) : Habilidad.admite(st);
    }

    /** Lo que se puede mejorar (o lo que admite cenizas) de lo que se lleva; se vuelve a mirar cada tick. */
    private void leer() {
        Integer antes = huecos.isEmpty() ? null : huecos.get(Mth.clamp(sel, 0, huecos.size() - 1));
        huecos.clear();
        Inventory inv = minecraft.player.getInventory();
        for (int h : ORDEN) if (vale(inv.getItem(h))) huecos.add(h);
        if (antes != null && huecos.contains(antes)) sel = huecos.indexOf(antes);
        sel = huecos.isEmpty() ? 0 : Mth.clamp(sel, 0, huecos.size() - 1);
        if (sel < arriba) arriba = sel;
        if (sel >= arriba + FILAS) arriba = sel - FILAS + 1;
        arriba = Mth.clamp(arriba, 0, Math.max(0, huecos.size() - FILAS));
        // las cenizas que se llevan (y quitar la puesta, si la hay)
        Integer antesOpcion = opciones.isEmpty() ? null : opciones.get(Mth.clamp(opcion, 0, opciones.size() - 1));
        opciones.clear();
        if (modo == CENIZAS && !huecos.isEmpty()) {
            if (Habilidad.puesta(elegido()) != null) opciones.add(-1);
            for (int h = 0; h < inv.getContainerSize(); h++) if (CenizaGuerra.de(inv.getItem(h)) != null) opciones.add(h);
        }
        if (antesOpcion != null && opciones.contains(antesOpcion)) opcion = opciones.indexOf(antesOpcion);
        opcion = opciones.isEmpty() ? 0 : Mth.clamp(opcion, 0, opciones.size() - 1);
    }

    private ItemStack elegido() {
        return huecos.isEmpty() ? ItemStack.EMPTY : minecraft.player.getInventory().getItem(huecos.get(sel));
    }

    @Override
    public void tick() {
        t++;
        dorvan.tick();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0) {
            minecraft.setScreen(null);
            return;
        }
        if (esperando > 0) esperando--;
        leer();
    }

    private void cambiarModo(int m) {
        if (m == modo) return;
        modo = m;
        sel = 0;
        arriba = 0;
        opcion = 0;
        Panel.mueve();
        leer();
    }

    // ------------------------------------------------------------------ mejorar y poner cenizas

    private boolean gasta() {
        return !minecraft.player.isCreative();
    }

    private void hacer() {
        if (modo == MEJORAR) mejorar();
        else aplicar();
    }

    private void mejorar() {
        if (esperando > 0 || huecos.isEmpty()) {
            Panel.nada();
            return;
        }
        ItemStack st = elegido();
        int n = Forja.nivel(st);
        if (n >= Forja.MAX) {
            dorvan.decirSiHaceRato(8, 60);
            Panel.nada();
            return;
        }
        int d = n + 1;
        if (gasta() && (minecraft.player.getInventory().countItem(Forja.piedra(Forja.grado(d))) < Forja.piedras(d) || ClientRol.ecos < Forja.ecos(d))) {
            dorvan.decirSiHaceRato(5, 60);
            Panel.nada();
            return;
        }
        Net.CH.sendToServer(new Net.Mejorar(entidad, huecos.get(sel)));
        esperando = 40;
        Panel.acepta();
    }

    /** Si la opcion elegida se puede poner (o quitar) en el arma elegida. */
    private boolean aplicable() {
        if (huecos.isEmpty() || opciones.isEmpty()) return false;
        int o = opciones.get(opcion);
        if (o < 0) return true;
        Habilidad h = CenizaGuerra.de(minecraft.player.getInventory().getItem(o));
        ItemStack st = elegido();
        return h != null && h.vale(com.darkcraft.combate.Armas.de(st)) && h != Habilidad.de(st);
    }

    private void aplicar() {
        if (esperando > 0 || !aplicable()) {
            Panel.nada();
            return;
        }
        Net.CH.sendToServer(new Net.Ceniza(entidad, huecos.get(sel), opciones.get(opcion)));
        esperando = 40;
        Panel.acepta();
    }

    private void resultado(Net.ForjaHecho m) {
        esperando = 0;
        switch (m.codigo()) {
            case Forja.HECHO, Forja.CENIZA, Forja.QUITADA -> {
                destello = t;
                destelloHueco = m.hueco();
                dorvan.decirSiHaceRato(4, 600);
            }
            case Forja.FALTA -> dorvan.decirSiHaceRato(5, 60);
            case Forja.TOPE -> dorvan.decirSiHaceRato(8, 60);
            default -> Panel.nada();
        }
    }

    private void volver() {
        Panel.cancela();
        minecraft.setScreen(null);
    }

    // ------------------------------------------------------------------ medidas

    private int px() {
        return (width - ANCHO) / 2;
    }

    private int py() {
        return Math.max(2, (height - ALTO - Bocadillo.ALTO - 4) / 2);
    }

    private int filaY(int i) {
        return py() + LISTA_Y + (i - arriba) * FILA;
    }

    private int filaEn(double mx, double my) {
        if (mx < px() + 10 || mx >= px() + 10 + IZQ) return -1;
        for (int i = arriba; i < Math.min(huecos.size(), arriba + FILAS); i++) if (my >= filaY(i) && my < filaY(i) + FILA) return i;
        return -1;
    }

    private static final int BOTON_W = 84;

    private int botonX(int i) {
        return px() + ANCHO - 14 - BOTON_W - (1 - i) * (BOTON_W + 6);
    }

    private int botonY() {
        return py() + ALTO - 31;
    }

    private boolean enBoton(double mx, double my, int i) {
        return mx >= botonX(i) && mx < botonX(i) + BOTON_W && my >= botonY() && my < botonY() + 20;
    }

    /** Las dos pestanas del titulo: {x, ancho} de cada una. */
    private int[] pestana(int i) {
        Component a = Component.translatable("darkcraft.forja.pestana.mejorar"), b = Component.translatable("darkcraft.forja.pestana.cenizas");
        int wa = Letra.anchoM(font, a), wb = Letra.anchoM(font, b), hueco = 26;
        int x0 = px() + (ANCHO - wa - wb - hueco) / 2;
        return i == 0 ? new int[]{x0, wa} : new int[]{x0 + wa + hueco, wb};
    }

    private int pestanaEn(double mx, double my) {
        if (my < py() + 8 || my > py() + 28) return -1;
        for (int i = 0; i < 2; i++) {
            int[] p = pestana(i);
            if (mx >= p[0] - 4 && mx < p[0] + p[1] + 4) return i;
        }
        return -1;
    }

    /** La fila de las cenizas en el detalle: x de la opcion i y su y. */
    private int opcionX(int i, int dx) {
        return dx + 8 + i * 22;
    }

    private int opcionY() {
        return py() + LISTA_Y - 3 + 84;
    }

    private int opcionEn(double mx, double my) {
        if (modo != CENIZAS || my < opcionY() - 2 || my >= opcionY() + 20) return -1;
        int dx = px() + 10 + IZQ + 10;
        for (int i = 0; i < Math.min(opciones.size(), 7); i++) if (mx >= opcionX(i, dx) - 2 && mx < opcionX(i, dx) + 20) return i;
        return -1;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int x = px(), y = py();
        float e = t + pt;
        g.fill(0, 0, width, height, (Math.round(150 * Mth.clamp(e / 5f, 0f, 1f)) << 24) | 0x050408);
        Pix.blanco();
        Panel.losa(g, x, y, ANCHO, ALTO);
        // las dos pestanas, en gotica (la elegida en oro con su raya; la otra apagada)
        int encimaP = pestanaEn(mx, my);
        for (int i = 0; i < 2; i++) {
            int[] p = pestana(i);
            Component c = Component.translatable(i == 0 ? "darkcraft.forja.pestana.mejorar" : "darkcraft.forja.pestana.cenizas");
            boolean es = i == modo;
            Letra.mediana(g, font, c, p[0], y + 12, es ? 0xFFE9C766 : i == encimaP ? 0xFFB8A888 : 0xFF7A6E5C, 0xFF050408);
            if (es) Panel.raya(g, p[0] - 2, p[0] + p[1] + 2, y + 27);
        }
        Pix.blanco();
        int[] p0 = pestana(0), p1 = pestana(1);
        g.drawString(font, "◆", (p0[0] + p0[1] + p1[0]) / 2 - 3, y + 15, 0xFF6E5A3A, false);
        Panel.raya(g, x + 12, x + ANCHO - 12, y + 33);

        // ---- a la izquierda: las armas
        Pix.blanco();
        Panel.hueco(g, x + 10, y + LISTA_Y - 3, IZQ, FILAS * FILA + 6);
        Inventory inv = minecraft.player.getInventory();
        if (huecos.isEmpty()) {
            List<FormattedCharSequence> l = font.split(Component.translatable(modo == MEJORAR ? "darkcraft.forja.nada" : "darkcraft.forja.nada_cenizas"), IZQ - 16);
            for (int i = 0; i < l.size(); i++) g.drawString(font, l.get(i), x + 18, y + LISTA_Y + 8 + i * 10, APAGADO, false);
        }
        int encima = filaEn(mx, my);
        for (int i = arriba; i < Math.min(huecos.size(), arriba + FILAS); i++) {
            int fy = filaY(i);
            ItemStack st = inv.getItem(huecos.get(i));
            Pix.blanco();
            if (i == sel) Panel.fila(g, x + 12, fy + 1, IZQ - 4);
            g.renderItem(st, x + 16, fy + 2);
            String derecha;
            int colorDer;
            if (modo == MEJORAR) {
                int n = Forja.nivel(st);
                derecha = n > 0 ? "+" + n : "";
                colorDer = n >= Forja.MAX ? 0xFFFFD866 : ORO;
            } else {
                derecha = Habilidad.puesta(st) != null ? "◆" : "";
                colorDer = 0xFFE2BF62;
            }
            int nw = font.width(derecha);
            String nombre = recortar(st.getHoverName().getString(), IZQ - 44 - nw);
            g.drawString(font, nombre, x + 36, fy + 6, i == sel ? LUZ : i == encima ? CLARO : 0xFFC8B89A, true);
            if (!derecha.isEmpty()) g.drawString(font, derecha, x + 10 + IZQ - 8 - nw, fy + 6, colorDer, true);
            if (huecos.get(i) == destelloHueco && t - destello < 24) {
                Pix.suma();
                Pix.color(1f, 0.8f, 0.4f, 0.6f * (1f - (e - destello) / 24f));
                g.fill(x + 13, fy + 1, x + 10 + IZQ - 3, fy + FILA - 1, 0xFFFFFFFF);
                Pix.blanco();
                Pix.mezcla();
            }
        }
        if (arriba > 0) g.drawString(font, "▲", x + 10 + IZQ - 12, y + LISTA_Y - 12, APAGADO, false);
        if (arriba + FILAS < huecos.size()) g.drawString(font, "▼", x + 10 + IZQ - 12, y + LISTA_Y + FILAS * FILA + 4, APAGADO, false);

        // ---- a la derecha: el elegido
        int dx = x + 10 + IZQ + 10, dw = ANCHO - 14 - (10 + IZQ + 10);
        Pix.blanco();
        Panel.hueco(g, dx, y + LISTA_Y - 3, dw, FILAS * FILA + 6);
        String ecos = Hud.numero(ClientRol.ecos);
        Pix.mezcla();
        Pix.t(g, HudTex.ECO_0, x + ANCHO - 16 - font.width(ecos) - 18, y + 14);
        g.drawString(font, ecos, x + ANCHO - 16 - font.width(ecos), y + 18, ORO, true);
        if (!huecos.isEmpty()) {
            if (modo == MEJORAR) detalle(g, dx, y + LISTA_Y - 3, dw);
            else detalleCeniza(g, dx, y + LISTA_Y - 3, dw, mx, my);
        }

        // ---- abajo: botones y teclas
        boolean puede = esperando <= 0 && (modo == MEJORAR ? !huecos.isEmpty() && Forja.nivel(elegido()) < Forja.MAX : aplicable());
        Component accion = Component.translatable(modo == MEJORAR ? "darkcraft.forja.mejorar"
                : !opciones.isEmpty() && opciones.get(opcion) < 0 ? "darkcraft.forja.quitar" : "darkcraft.forja.aplicar");
        boton(g, 0, accion, puede, enBoton(mx, my, 0));
        boton(g, 1, Component.translatable("darkcraft.menu.volver"), true, enBoton(mx, my, 1));
        int ty = botonY() + 4, tx = x + 14;
        tx = Panel.tecla(g, font, Component.literal("TAB"), Component.translatable("darkcraft.menu.tecla.pestana"), tx, ty, APAGADO) + 8;
        if (modo == CENIZAS) tx = Panel.tecla(g, font, Component.literal("←→"), Component.translatable("darkcraft.forja.tecla.ceniza"), tx, ty, APAGADO) + 8;
        else tx = Panel.tecla(g, font, Component.literal("↑↓"), Component.translatable("darkcraft.menu.tecla.elegir"), tx, ty, APAGADO) + 8;
        Component k = minecraft.options.keyInventory.getTranslatedKeyMessage();
        if (tx + Panel.anchoTecla(font, k, accion) < botonX(0) - 6) Panel.tecla(g, font, k, accion, tx, ty, APAGADO);

        // ---- lo que dice Dorvan
        dorvan.render(g, font, x, y + ALTO + 4, ANCHO, pt);
        Pix.blanco();
        Pix.mezcla();
    }

    /** El objeto en grande con su nombre en gotica (arriba del detalle). */
    private void cabecera(GuiGraphics g, ItemStack st, int x, int y, int w) {
        g.pose().pushPose();
        g.pose().translate(x + 8, y + 7, 0);
        g.pose().scale(2f, 2f, 1f);
        g.renderItem(st, 0, 0);
        g.pose().popPose();
        Panel.nombre(g, font, st.getHoverName(), x + 46, y + 9, w - 54);
    }

    private void detalle(GuiGraphics g, int x, int y, int w) {
        ItemStack st = elegido();
        Inventory inv = minecraft.player.getInventory();
        int n = Forja.nivel(st);
        cabecera(g, st, x, y, w);
        String paso = n >= Forja.MAX ? "+" + n : "+" + n + "  →  +" + (n + 1);
        g.drawString(font, paso, x + 46, y + 27, n >= Forja.MAX ? 0xFFFFD866 : LUZ, true);
        Panel.raya(g, x + 6, x + w - 6, y + 42);
        int ly = y + 48;
        boolean catalizador = com.darkcraft.hechizos.Catalizador.de(st) != null;
        if (catalizador) {
            stat(g, x, w, ly, "darkcraft.forja.potencia", String.format(Locale.ROOT, "× %.2f", Forja.mult(n)),
                    n >= Forja.MAX ? null : String.format(Locale.ROOT, "× %.2f", Forja.mult(n + 1)));
        } else {
            float base = com.darkcraft.combate.Escalado.base(st);
            float ahora = com.darkcraft.combate.Escalado.total(st, base, ClientRol.atributos);
            float luego = ahora / Forja.mult(n) * Forja.mult(n + 1);
            stat(g, x, w, ly, "darkcraft.menu.ataque", String.format(Locale.ROOT, "%.1f", ahora),
                    n >= Forja.MAX ? null : String.format(Locale.ROOT, "%.1f", luego));
        }
        if (n >= Forja.MAX) {
            List<FormattedCharSequence> l = font.split(Component.translatable("darkcraft.forja.tope"), w - 14);
            for (int i = 0; i < l.size(); i++) g.drawString(font, l.get(i), x + 7, ly + 20 + i * 10, 0xFFFFD866, true);
            return;
        }
        int d = n + 1;
        Item piedra = Forja.piedra(Forja.grado(d));
        int tiene = inv.countItem(piedra), pide = Forja.piedras(d);
        boolean libre = !gasta();
        g.drawString(font, Component.translatable("darkcraft.forja.piedras"), x + 7, ly + 20, CLARO, true);
        String sp = pide + " / " + tiene;
        int spw = font.width(sp);
        g.drawString(font, sp, x + w - 7 - spw, ly + 20, tiene >= pide || libre ? ORO : ROJO, true);
        g.renderItem(new ItemStack(piedra), x + w - 7 - spw - 19, ly + 16);
        g.drawString(font, Component.translatable("darkcraft.forja.ecos"), x + 7, ly + 40, CLARO, true);
        String se = Hud.numero(Forja.ecos(d));
        int sew = font.width(se);
        g.drawString(font, se, x + w - 7 - sew, ly + 40, ClientRol.ecos >= Forja.ecos(d) || libre ? ORO : ROJO, true);
        Pix.mezcla();
        Pix.t(g, HudTex.ECO_0, x + w - 7 - sew - 18, ly + 36);
        Pix.blanco();
        String np = recortar(new ItemStack(piedra).getHoverName().getString(), w - 14);
        g.drawString(font, np, x + 7, ly + 30, APAGADO, false);
    }

    /** La pestana de cenizas: la habilidad del arma, lo que gasta, para que sirve y las cenizas que se llevan. */
    private void detalleCeniza(GuiGraphics g, int x, int y, int w, int mx, int my) {
        ItemStack st = elegido();
        Inventory inv = minecraft.player.getInventory();
        Habilidad h = Habilidad.de(st);
        boolean puesta = Habilidad.puesta(st) != null;
        cabecera(g, st, x, y, w);
        if (h != null) {
            Component nombre = Component.translatable(h.clave());
            g.drawString(font, nombre, x + 46, y + 27, LUZ, true);
            Component marca = Component.translatable(puesta ? "darkcraft.forja.de_ceniza" : "darkcraft.forja.la_suya");
            g.drawString(font, marca, x + 46 + font.width(nombre) + 6, y + 27, puesta ? ORO : APAGADO, false);
        }
        Panel.raya(g, x + 6, x + w - 6, y + 42);
        if (h != null) {
            g.drawString(font, CenizaGuerra.coste(h), x + 7, y + 46, 0xFF8FC8E8, false);
            List<FormattedCharSequence> d = font.split(Component.translatable(h.clave() + ".desc"), w - 14);
            for (int i = 0; i < Math.min(3, d.size()); i++) g.drawString(font, d.get(i), x + 7, y + 55 + i * 9, APAGADO, false);
        }
        // las cenizas que se llevan
        int oy = opcionY();
        if (opciones.isEmpty()) {
            g.drawString(font, Component.translatable("darkcraft.forja.sin_cenizas"), x + 7, oy + 5, 0xFF7A705E, false);
            return;
        }
        int encimaO = opcionEn(mx, my);
        for (int i = 0; i < Math.min(opciones.size(), 7); i++) {
            int ox = opcionX(i, x);
            int o = opciones.get(i);
            Pix.blanco();
            Panel.hueco(g, ox - 2, oy - 2, 20, 20);
            boolean vale;
            if (o < 0) {
                // quitar: la habilidad de siempre del arma (una cruz roja encima)
                ItemStack suya = new ItemStack(net.minecraft.world.item.Items.BARRIER);
                g.renderItem(suya, ox, oy);
                vale = true;
            } else {
                ItemStack c = inv.getItem(o);
                Habilidad ch = CenizaGuerra.de(c);
                vale = ch != null && ch.vale(com.darkcraft.combate.Armas.de(st)) && ch != h;
                g.renderItem(c, ox, oy);
                if (!vale) {
                    Pix.mezcla();
                    g.fill(ox, oy, ox + 16, oy + 16, 0xA0101014);
                }
            }
            if (i == opcion || i == encimaO) {
                int c = i == opcion ? 0xFFE2BF62 : 0x80E2BF62;
                g.fill(ox - 2, oy - 2, ox + 18, oy - 1, c);
                g.fill(ox - 2, oy + 17, ox + 18, oy + 18, c);
                g.fill(ox - 2, oy - 2, ox - 1, oy + 18, c);
                g.fill(ox + 17, oy - 2, ox + 18, oy + 18, c);
            }
        }
        // lo que hace la elegida
        int o = opciones.get(opcion);
        Component que;
        if (o < 0) que = Component.translatable("darkcraft.forja.quitar_ceniza", Component.translatable(Habilidad.porDefecto(com.darkcraft.combate.Armas.de(st)).clave()));
        else {
            Habilidad ch = CenizaGuerra.de(inv.getItem(o));
            que = ch == null ? Component.empty() : !ch.vale(com.darkcraft.combate.Armas.de(st))
                    ? Component.translatable("darkcraft.forja.no_vale")
                    : ch == h ? Component.translatable("darkcraft.forja.ya_la_tiene")
                    : Component.translatable("darkcraft.forja.cambia", Component.translatable(h == null ? "darkcraft.habilidad.ninguna_corta" : h.clave()),
                    Component.translatable(ch.clave()));
        }
        // lo que hara (encima de los botones)
        g.drawString(font, recortar(que.getString(), w - 14), x + 7, oy + 20, aplicable() ? LUZ : 0xFF9A8F7E, true);
    }

    private void stat(GuiGraphics g, int x, int w, int y, String clave, String antes, String despues) {
        g.drawString(font, Component.translatable(clave), x + 7, y, CLARO, true);
        if (despues == null) {
            g.drawString(font, antes, x + w - 7 - font.width(antes), y, ORO, true);
            return;
        }
        int wd = font.width(despues), wf = font.width(" → "), wa = font.width(antes);
        int rx = x + w - 7 - wd;
        g.drawString(font, despues, rx, y, VERDE, true);
        g.drawString(font, " → ", rx - wf, y, APAGADO, true);
        g.drawString(font, antes, rx - wf - wa, y, ORO, true);
    }

    private void boton(GuiGraphics g, int i, Component texto, boolean activo, boolean encima) {
        int x = botonX(i), y = botonY();
        boolean luz = activo && encima;
        Pix.color(1f, 1f, 1f, activo ? 1f : 0.45f);
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, x, y, BOTON_W);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, x, y, BOTON_W);
        Pix.blanco();
        g.drawString(font, texto, x + (BOTON_W - font.width(texto)) / 2, y + 6, !activo ? 0xFF7A705E : luz ? LUZ : ORO, true);
    }

    private String recortar(String s, int ancho) {
        if (font.width(s) <= ancho) return s;
        return font.plainSubstrByWidth(s, Math.max(0, ancho - font.width("…"))) + "…";
    }

    // ------------------------------------------------------------------ raton y teclado

    private void elegir(int i) {
        if (huecos.isEmpty()) return;
        int n = Mth.clamp(i, 0, huecos.size() - 1);
        if (n != sel) {
            Panel.mueve();
            opcion = 0;
        }
        sel = n;
        if (sel < arriba) arriba = sel;
        if (sel >= arriba + FILAS) arriba = sel - FILAS + 1;
        leer();
    }

    private void elegirOpcion(int i) {
        if (opciones.isEmpty()) return;
        int n = Mth.clamp(i, 0, opciones.size() - 1);
        if (n == opcion) {
            Panel.nada();
            return;
        }
        opcion = n;
        Panel.mueve();
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int i = filaEn(mx, my);
        if (i >= 0 && i != sel) elegir(i);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b != 0) return false;
        int p = pestanaEn(mx, my);
        if (p >= 0) {
            cambiarModo(p);
            return true;
        }
        if (enBoton(mx, my, 0)) {
            hacer();
            return true;
        }
        if (enBoton(mx, my, 1)) {
            volver();
            return true;
        }
        int o = opcionEn(mx, my);
        if (o >= 0) {
            elegirOpcion(o);
            return true;
        }
        int i = filaEn(mx, my);
        if (i >= 0) elegir(i);
        return i >= 0;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double rueda) {
        if (rueda == 0 || huecos.isEmpty()) return false;
        elegir(sel + (rueda > 0 ? -1 : 1));
        return true;
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        var o = minecraft.options;
        if (tecla == GLFW.GLFW_KEY_ESCAPE) {
            volver();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_TAB) {
            cambiarModo(1 - modo);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_UP || o.keyUp.matches(tecla, scan)) {
            elegir(sel - 1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || o.keyDown.matches(tecla, scan)) {
            elegir(sel + 1);
            return true;
        }
        if (modo == CENIZAS && (tecla == GLFW.GLFW_KEY_LEFT || o.keyLeft.matches(tecla, scan))) {
            elegirOpcion(opcion - 1);
            return true;
        }
        if (modo == CENIZAS && (tecla == GLFW.GLFW_KEY_RIGHT || o.keyRight.matches(tecla, scan))) {
            elegirOpcion(opcion + 1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || o.keyInventory.matches(tecla, scan)) {
            hacer();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    public String resumen() {
        ItemStack st = elegido();
        return "forja " + (modo == MEJORAR ? "mejorar" : "cenizas") + " " + huecos.size() + " armas, elegida "
                + (huecos.isEmpty() ? "-" : huecos.get(sel) + " " + st.getItem() + " +" + Forja.nivel(st) + " habilidad " + Habilidad.de(st))
                + (modo == CENIZAS ? " opciones " + opciones + " elegida " + opcion : "")
                + " | " + ("herrero".equals(quien) ? "dorvan" : quien) + " dice " + dorvan.numero() + (dorvan.hablando() ? " (sonando)" : "") + (esperando > 0 ? " (esperando)" : "");
    }

    /** Elige el hueco del inventario {@code hueco} (si esta en la lista). */
    public void pulsarHueco(int hueco) {
        int i = huecos.indexOf(hueco);
        if (i >= 0) elegir(i);
    }

    public void pulsarMejorar() {
        mejorar();
    }

    public void pulsarModo(int m) {
        cambiarModo(m);
    }

    /** Elige la ceniza del hueco {@code hueco} (o -1: quitar) y la pone. */
    public void pulsarCeniza(int hueco) {
        int i = opciones.indexOf(hueco);
        if (i >= 0) opcion = i;
        aplicar();
    }
}
