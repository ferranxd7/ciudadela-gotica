package com.darkcraft.client.ui;

import com.darkcraft.client.armas.ArmasCliente;
import com.darkcraft.client.ui.ClientEquipo.Clase;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Atributo;
import com.darkcraft.rol.Equipo;
import com.darkcraft.rol.Nivel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * El menu del mod (tecla R; en supervivencia tambien la del inventario), en pixel art, en vez de las pantallas de
 * Minecraft. Cuatro pestanas:
 * <ul>
 * <li><b>Equipo</b>: las tres armas de cada mano, la armadura y los objetos rapidos, con el personaje girando al lado;
 * al elegir un hueco sale la lista de lo que se puede poner en el.</li>
 * <li><b>Inventario</b>: todo lo que se lleva, por clases (armas, armaduras, objetos, materiales, objetos clave), con la
 * ficha de cada cosa; se equipa, se quita y se tira desde aqui.</li>
 * <li><b>Estado</b>: nivel, ecos, atributos y lo que dan.</li>
 * <li><b>Sistema</b>: el mapa y el diario, los logros, los controles y las opciones.</li>
 * </ul>
 * Con raton o con teclado (flechas o WASD, INTRO o la tecla del inventario para elegir, TAB para cambiar de pestana, ESC
 * o R para cerrar). El juego no se para: si pegan al jugador, se cierra solo.
 */
public class MenuScreen extends Screen {
    public static final int EQUIPO = 0, INVENTARIO = 1, ESTADO = 2, SISTEMA = 3;
    private static final String[] PESTANAS = {"equipo", "inventario", "estado", "sistema"};
    private static final int CLARO = 0xFFEADBC4, ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, APAGADO = 0xFFB8A888, GRIS = 0xFF8A8072, VERDE = 0xFF9BE08A;
    private static final int FRASCO_VIDA = 100, FRASCO_MANA = 101, TALISMAN = 200, LADO = 22, PASO = 25;

    private static boolean esFrasco(int id) {
        return id == FRASCO_VIDA || id == FRASCO_MANA;
    }

    private static boolean esTalisman(int id) {
        return id >= TALISMAN && id < TALISMAN + com.darkcraft.rol.Talismanes.MAX;
    }

    /** Si el hueco de talisman esta abierto (uno al empezar y otro por cada gran jefe vencido). */
    private static boolean abierto(int id) {
        return id - TALISMAN < ClientRol.huecosTalisman;
    }

    private final Pagina[] paginas = {new PEquipo(), new PBolsa(), new PEstado(), new PSistema()};
    private int pestana, t;
    private float giro = 25f;
    private boolean girando;
    /** La losa grande donde va cada pestana. */
    private int px, py, pw, ph;

    public static void abrir(int pestana) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new MenuScreen(pestana));
        Panel.abre();
    }

    public MenuScreen(int pestana) {
        super(Component.translatable("key.darkcraft.menu"));
        this.pestana = Mth.clamp(pestana, 0, PESTANAS.length - 1);
    }

    @Override
    protected void init() {
        px = 8;
        py = 30;
        pw = width - 16;
        ph = height - py - 22;
        for (Pagina p : paginas) p.medir();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        t++;
        LocalPlayer p = minecraft.player;
        if (p == null || !p.isAlive() || p.hurtTime > 0) {
            minecraft.setScreen(null);
            return;
        }
        if (!girando) giro += 0.9f;
        paginas[pestana].tick();
    }

    private LocalPlayer yo() {
        return minecraft.player;
    }

    private Inventory inv() {
        return minecraft.player.getInventory();
    }

    private void cambiarPestana(int n) {
        n = Math.floorMod(n, PESTANAS.length);
        if (n == pestana) return;
        pestana = n;
        paginas[pestana].medir();
        Panel.mueve();
    }

    // ------------------------------------------------------------------ dibujo comun

    private int pestanaX(int i) {
        int paso = Math.min(104, (width - 20) / PESTANAS.length);
        return width / 2 + Math.round((i - (PESTANAS.length - 1) / 2f) * paso);
    }

    private int pestanaEn(double mx, double my) {
        if (my < 2 || my >= 26) return -1;
        int paso = Math.min(104, (width - 20) / PESTANAS.length);
        for (int i = 0; i < PESTANAS.length; i++) if (Math.abs(mx - pestanaX(i)) < paso / 2.0) return i;
        return -1;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        float abre = Mth.clamp((t + pt) / 4f, 0f, 1f);
        g.fill(0, 0, width, height, (Math.round(176 * abre) << 24) | 0x050408);
        // las pestanas
        int encima = pestanaEn(mx, my);
        for (int i = 0; i < PESTANAS.length; i++) {
            Component c = Component.translatable("darkcraft.menu.pestana." + PESTANAS[i]);
            int w = Letra.anchoM(font, c), x = pestanaX(i) - w / 2;
            Letra.mediana(g, font, c, x, 7, i == pestana ? 0xFFF6E3A0 : i == encima ? 0xFFC0963C : 0xFF8A6A30, 0xFF050408);
            if (i == pestana) {
                Pix.blanco();
                Panel.t(g, MenuTex.PESTANA, pestanaX(i) - 20, 21);
            }
        }
        Pix.blanco();
        Panel.losa(g, px, py, pw, ph);
        paginas[pestana].dibujar(g, mx, my, pt);
        // las teclas, abajo
        int ty = height - 17, tx = px + 4;
        tx = Panel.tecla(g, font, Component.literal("Tab"), Component.translatable("darkcraft.menu.tecla.pestana"), tx, ty, APAGADO) + 10;
        tx = Panel.tecla(g, font, minecraft.options.keyInventory.getTranslatedKeyMessage(), Component.translatable("darkcraft.menu.tecla.elegir"), tx, ty, APAGADO) + 10;
        tx = paginas[pestana].teclas(g, tx, ty);
        Panel.tecla(g, font, Component.literal("Esc"), Component.translatable("darkcraft.menu.tecla.cerrar"), tx, ty, APAGADO);
        Pix.blanco();
        Pix.mezcla();
    }

    /** Una ranura con lo que lleva (o su silueta), la marca de elegida y, si se pide, la de equipado. */
    private void ranura(GuiGraphics g, int x, int y, ItemStack st, MenuTex.T silueta, boolean elegida, boolean equipado) {
        Cruz.ranura(g, font, x, y, st, silueta, elegida, 1f);
        if (equipado) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 250);
            Pix.blanco();
            Panel.t(g, MenuTex.EQUIPADO, x + 1, y + 1);
            g.pose().popPose();
        }
        if (elegida) cursor(g, x, y, LADO, LADO);
    }

    /** Las cuatro esquinas del cursor alrededor de algo (laten: se abren y se cierran un pixel). */
    private void cursor(GuiGraphics g, int x, int y, int w, int h) {
        int d = (t / 8) % 2;
        g.pose().pushPose();
        g.pose().translate(0, 0, 260);
        Pix.blanco();
        Panel.t(g, MenuTex.CURSOR_AI, x - 2 - d, y - 2 - d);
        Panel.t(g, MenuTex.CURSOR_AD, x + w - 5 + d, y - 2 - d);
        Panel.t(g, MenuTex.CURSOR_BI, x - 2 - d, y + h - 5 + d);
        Panel.t(g, MenuTex.CURSOR_BD, x + w - 5 + d, y + h - 5 + d);
        g.pose().popPose();
    }

    /** El personaje del jugador, girando despacio (o como lo deje el raton), dentro de su rebaje. */
    private void personaje(GuiGraphics g, int x, int y, int w, int h) {
        Pix.blanco();
        Panel.hueco(g, x, y, w, h);
        LocalPlayer p = yo();
        int escala = Mth.clamp(Math.min((h - 26) * 10 / 21, w * 10 / 13), 20, 78);
        float cuerpo = p.yBodyRot, cuerpoO = p.yBodyRotO, giroY = p.getYRot(), giroYO = p.yRotO, cabeza = p.yHeadRot, cabezaO = p.yHeadRotO,
                inclina = p.getXRot(), inclinaO = p.xRotO;
        p.yBodyRot = p.yBodyRotO = 180f + giro;
        p.setYRot(180f + giro);
        p.yRotO = 180f + giro;
        p.yHeadRot = p.yHeadRotO = 180f + giro;
        p.setXRot(0f);
        p.xRotO = 0f;
        g.enableScissor(x + 2, y + 2, x + w - 2, y + h - 2);
        // bien por delante de la losa: lo que el personaje lleva en la mano no se hunde en ella al girar
        g.pose().pushPose();
        g.pose().translate(0, 0, 150);
        try {
            InventoryScreen.renderEntityInInventory(g, x + w / 2, y + h - 12, escala, new Quaternionf().rotateZ((float) Math.PI), null, p);
        } finally {
            g.pose().popPose();
            g.disableScissor();
            p.yBodyRot = cuerpo;
            p.yBodyRotO = cuerpoO;
            p.setYRot(giroY);
            p.yRotO = giroYO;
            p.yHeadRot = cabeza;
            p.yHeadRotO = cabezaO;
            p.setXRot(inclina);
            p.xRotO = inclinaO;
        }
        Pix.blanco();
        Pix.mezcla();
    }

    private void dato(GuiGraphics g, int x, int x1, int y, String clave, String valor, int color) {
        g.drawString(font, Component.translatable(clave), x, y, CLARO, true);
        g.drawString(font, valor, x1 - font.width(valor), y, color, true);
    }

    /** Lo que pega el arma de la mano derecha con los atributos de ahora (como lo cuenta el servidor al pegar). */
    private static float ataque(LocalPlayer p) {
        ItemStack st = p.getMainHandItem();
        float medido = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float base = com.darkcraft.combate.Escalado.base(st);
        return com.darkcraft.combate.Escalado.total(st, base, ClientRol.atributos) + (medido - base);
    }

    /** El peso del equipo y el maximo que se lleva sin pasar a sobrecargado (con una cifra decimal). */
    private static String peso(LocalPlayer p) {
        return String.format(Locale.ROOT, "%.1f / %.1f", com.darkcraft.combate.Peso.de(p.getInventory()), ClientRol.pesoMaximo());
    }

    private static int colorPeso(int clase) {
        return switch (clase) {
            case 0 -> VERDE;
            case 1 -> ORO;
            case 2 -> 0xFFE89A40;
            default -> 0xFFE0564E;
        };
    }

    private static String vida(float v) {
        return Integer.toString(Math.round(v * 5f));
    }

    /** Las lineas de la ficha de un objeto (lo que diria su recuadro en Minecraft, sin el nombre). */
    private List<FormattedCharSequence> ficha(ItemStack st, int ancho) {
        List<FormattedCharSequence> l = new ArrayList<>();
        // un arma: lo que pega con los atributos de ahora, sus letras y sus requisitos (en rojo los que no se cumplen)
        com.darkcraft.combate.Escalado.Datos d = com.darkcraft.combate.Escalado.de(st);
        if (d != null) {
            int[] at = ClientRol.atributos;
            float base = com.darkcraft.combate.Escalado.base(st), extra = com.darkcraft.combate.Escalado.extra(st, base, at);
            l.addAll(font.split(Component.translatable("darkcraft.arma.ataque", String.format(Locale.ROOT, "%.1f", base),
                    (extra >= 0 ? "+ " : "- ") + String.format(Locale.ROOT, "%.1f", Math.abs(extra))).withStyle(net.minecraft.ChatFormatting.GOLD), ancho));
            net.minecraft.network.chat.MutableComponent letras = Component.translatable("darkcraft.arma.escala");
            net.minecraft.network.chat.MutableComponent req = Component.translatable("darkcraft.arma.requisitos");
            boolean algunReq = false;
            for (int i = 0; i < com.darkcraft.combate.Escalado.ATRIBUTOS.length; i++) {
                com.darkcraft.rol.Atributo a = com.darkcraft.combate.Escalado.ATRIBUTOS[i];
                Component corto = Component.translatable("darkcraft.atributo." + a.clave + ".corto");
                if (d.letra(i) != '-') letras.append(" ").append(corto).append(" " + d.letra(i));
                int r = d.requisitos()[i];
                if (r > 0) {
                    algunReq = true;
                    req.append(" ").append(Component.empty().append(corto).append(" " + r)
                            .withStyle(at[a.ordinal()] >= r ? net.minecraft.ChatFormatting.GRAY : net.minecraft.ChatFormatting.RED));
                }
            }
            l.addAll(font.split(letras.withStyle(net.minecraft.ChatFormatting.GRAY), ancho));
            if (algunReq) l.addAll(font.split(req.withStyle(net.minecraft.ChatFormatting.GRAY), ancho));
            if (!com.darkcraft.combate.Escalado.cumple(d, at))
                l.addAll(font.split(Component.translatable("darkcraft.arma.sin_requisitos").withStyle(net.minecraft.ChatFormatting.RED), ancho));
        }
        List<Component> t = st.getTooltipLines(yo(), TooltipFlag.Default.NORMAL);
        for (int i = 1; i < t.size(); i++) {
            if (t.get(i).getString().isBlank()) continue;
            l.addAll(font.split(t.get(i), ancho));
        }
        return l;
    }

    private String recortar(String s, int ancho) {
        if (font.width(s) <= ancho) return s;
        return font.plainSubstrByWidth(s, ancho - font.width("…")) + "…";
    }

    // ------------------------------------------------------------------ raton y teclado

    @Override
    public void mouseMoved(double mx, double my) {
        paginas[pestana].raton(mx, my);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        int p = pestanaEn(mx, my);
        if (p >= 0 && b == 0) {
            cambiarPestana(p);
            return true;
        }
        return paginas[pestana].clic(mx, my, b);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int b) {
        girando = false;
        return super.mouseReleased(mx, my, b);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (girando) {
            giro -= (float) dx * 1.4f;
            return true;
        }
        return super.mouseDragged(mx, my, b, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double rueda) {
        return paginas[pestana].rueda(mx, my, rueda);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        var o = minecraft.options;
        if (paginas[pestana].modal()) return paginas[pestana].tecla(tecla, scan) || tecla == GLFW.GLFW_KEY_ESCAPE;
        if (tecla == GLFW.GLFW_KEY_ESCAPE || Teclas.MENU.matches(tecla, scan)) {
            onClose();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_TAB) {
            cambiarPestana(pestana + (hasShiftDown() ? -1 : 1));
            return true;
        }
        if (paginas[pestana].tecla(tecla, scan)) return true;
        // la tecla del inventario, si la pagina no la ha usado para elegir, cierra (como la cerraba el inventario de siempre)
        if (o.keyInventory.matches(tecla, scan)) {
            onClose();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public void onClose() {
        Panel.cancela();
        super.onClose();
    }

    private static boolean esArriba(Minecraft mc, int tecla, int scan) {
        return tecla == GLFW.GLFW_KEY_UP || mc.options.keyUp.matches(tecla, scan);
    }

    private static boolean esAbajo(Minecraft mc, int tecla, int scan) {
        return tecla == GLFW.GLFW_KEY_DOWN || mc.options.keyDown.matches(tecla, scan);
    }

    private static boolean esIzquierda(Minecraft mc, int tecla, int scan) {
        return tecla == GLFW.GLFW_KEY_LEFT || mc.options.keyLeft.matches(tecla, scan);
    }

    private static boolean esDerecha(Minecraft mc, int tecla, int scan) {
        return tecla == GLFW.GLFW_KEY_RIGHT || mc.options.keyRight.matches(tecla, scan);
    }

    private boolean esElegir(int tecla, int scan) {
        return tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE
                || minecraft.options.keyInventory.matches(tecla, scan);
    }

    private static boolean esQuitar(int tecla) {
        return tecla == GLFW.GLFW_KEY_DELETE || tecla == GLFW.GLFW_KEY_BACKSPACE;
    }

    /** Una pestana del menu. */
    private abstract class Pagina {
        void medir() {
        }

        void tick() {
        }

        abstract void dibujar(GuiGraphics g, int mx, int my, float pt);

        /** Las teclas propias de la pagina en la barra de abajo: devuelve donde acaba. */
        int teclas(GuiGraphics g, int x, int y) {
            return x;
        }

        boolean tecla(int tecla, int scan) {
            return false;
        }

        boolean clic(double mx, double my, int b) {
            return false;
        }

        boolean rueda(double mx, double my, double r) {
            return false;
        }

        void raton(double mx, double my) {
        }

        /** Si hay algo abierto encima (una lista): se queda con todas las teclas. */
        boolean modal() {
            return false;
        }
    }

    /** Un hueco del equipo en la pantalla: que hueco del inventario es (o un frasco) y donde va. */
    private record Zona(int id, int x, int y, MenuTex.T silueta) {
        boolean dentro(double mx, double my) {
            return mx >= x && mx < x + LADO && my >= y && my < y + LADO;
        }
    }

    // ================================================================== EQUIPO

    private final class PEquipo extends Pagina {
        private final List<Zona> zonas = new ArrayList<>();
        private int foco = 0;
        /** La lista de lo que se puede poner en el hueco elegido (null = cerrada): -1 es "quitar". */
        private List<Integer> lista;
        private int listaSel, listaDesde, sinNada = -1000;
        private static final int FILAS = 7, ANCHO_LISTA = 178;

        private int x0() {
            return px + 14;
        }

        private int fila(int i) {
            return py + 22 + i * 42;
        }

        @Override
        void medir() {
            zonas.clear();
            for (int i = 0; i < 3; i++) zonas.add(new Zona(Equipo.DERECHA[i], x0() + i * PASO, fila(0), MenuTex.SIL_ESPADA));
            for (int i = 0; i < 3; i++) zonas.add(new Zona(Equipo.IZQUIERDA[i], x0() + 90 + i * PASO, fila(0), MenuTex.SIL_ESCUDO));
            MenuTex.T[] sil = {MenuTex.SIL_YELMO, MenuTex.SIL_PECHO, MenuTex.SIL_PIERNAS, MenuTex.SIL_BOTAS};
            for (int i = 0; i < 4; i++) zonas.add(new Zona(Equipo.ARMADURA[i], x0() + i * PASO, fila(1), sil[i]));
            for (int i = 0; i < com.darkcraft.rol.Talismanes.MAX; i++) zonas.add(new Zona(TALISMAN + i, x0() + 110 + i * PASO, fila(1), MenuTex.SIL_TALISMAN));
            zonas.add(new Zona(FRASCO_VIDA, x0(), fila(2), null));
            zonas.add(new Zona(FRASCO_MANA, x0() + PASO, fila(2), null));
            for (int i = 0; i < 4; i++) zonas.add(new Zona(Equipo.RAPIDOS[i], x0() + (i + 2) * PASO, fila(2), MenuTex.SIL_OBJETO));
            lista = null;
        }

        private Zona zona(int id) {
            for (Zona z : zonas) if (z.id == id) return z;
            return zonas.get(0);
        }

        private ItemStack de(int id) {
            if (esTalisman(id)) return ClientRol.talismanes[id - TALISMAN];
            return id >= 100 ? ItemStack.EMPTY : inv().getItem(id);
        }

        @Override
        boolean modal() {
            return lista != null;
        }

        @Override
        void dibujar(GuiGraphics g, int mx, int my, float pt) {
            String[] grupos = {"derecha", "izquierda", "armadura", "talismanes", "rapidos"};
            int[][] donde = {{x0(), fila(0)}, {x0() + 90, fila(0)}, {x0(), fila(1)}, {x0() + 110, fila(1)}, {x0(), fila(2)}};
            for (int i = 0; i < grupos.length; i++)
                g.drawString(font, Component.translatable("darkcraft.menu.equipo." + grupos[i]), donde[i][0], donde[i][1] - 11, APAGADO, true);
            for (Zona z : zonas) {
                boolean elegida = z.id == foco;
                if (esTalisman(z.id) && !abierto(z.id)) {
                    // cerrado: se abre al vencer a un gran jefe
                    Pix.color(1f, 1f, 1f, 0.85f);
                    Panel.t(g, MenuTex.RANURA_CERRADA, z.x, z.y);
                    Pix.blanco();
                    Panel.t(g, MenuTex.CANDADO, z.x + 6, z.y + 5);
                    if (elegida) cursor(g, z.x, z.y, LADO, LADO);
                } else if (esTalisman(z.id)) ranura(g, z.x, z.y, de(z.id), z.silueta, elegida, false);
                else if (z.id >= 100) {
                    boolean mana = z.id == FRASCO_MANA;
                    int total = mana ? ClientRol.frascosMana : ClientRol.frascosVida, quedan = mana ? ClientRol.quedanMana : ClientRol.quedanVida;
                    ranura(g, z.x, z.y, ItemStack.EMPTY, null, elegida, false);
                    Pix.blanco();
                    Panel.t(g, total > 0 && quedan > 0 ? (mana ? MenuTex.FRASCO_MANA : MenuTex.FRASCO_VIDA)
                            : (mana ? MenuTex.FRASCO_MANA_VACIO : MenuTex.FRASCO_VIDA_VACIO), z.x + 3, z.y + 2);
                    String n = Integer.toString(quedan);
                    g.drawString(font, n, z.x + 20 - font.width(n), z.y + 12, quedan > 0 ? 0xFFF6E3A0 : 0xFFB03038, true);
                } else ranura(g, z.x, z.y, de(z.id), z.silueta, elegida, false);
            }
            // en medio: lo que da el equipo
            LocalPlayer p = yo();
            int cw = Mth.clamp(pw / 4, 98, 150), cx = px + pw - 12 - cw;
            int sx = x0() + 218, sx1 = cx - 10;
            if (sx1 - sx >= 84) {
                int sy = py + 14;
                dato(g, sx, sx1, sy, "darkcraft.menu.nivel", Integer.toString(ClientRol.nivel()), ORO);
                dato(g, sx, sx1, sy + 13, "darkcraft.menu.vida", vida(p.getHealth()) + "/" + vida(p.getMaxHealth()), ORO);
                dato(g, sx, sx1, sy + 26, "darkcraft.menu.mana", ArmasCliente.energiaCliente() + "/" + ClientRol.manaMax, ORO);
                dato(g, sx, sx1, sy + 39, "darkcraft.menu.estamina", Integer.toString(Math.round(ClientRol.estaminaMax)), ORO);
                dato(g, sx, sx1, sy + 52, "darkcraft.menu.ataque", String.format(Locale.ROOT, "%.1f", ataque(p)), ORO);
                dato(g, sx, sx1, sy + 65, "darkcraft.menu.defensa", Integer.toString(p.getArmorValue()), ORO);
                // el peso del equipo (lo que se lleva en las manos y puesto) y como deja rodar
                int clase = com.darkcraft.client.combate.ClientCombate.clasePeso(p);
                dato(g, sx, sx1, sy + 78, "darkcraft.menu.peso", peso(p), colorPeso(clase));
                Component c = Component.translatable("darkcraft.peso." + clase);
                g.drawString(font, c, sx1 - font.width(c), sy + 89, colorPeso(clase), true);
            }
            personaje(g, cx, py + 10, cw, ph - 20);
            // abajo: lo que hay en el hueco elegido
            int dy = fila(2) + LADO + 9, ancho = cx - 12 - x0();
            Panel.raya(g, x0() - 4, cx - 8, dy - 5);
            if (t - sinNada < 50) {
                g.drawString(font, Component.translatable("darkcraft.menu.nada_que_poner"), x0(), dy + 2, 0xFFE0564E, true);
            } else if (esTalisman(foco) && !abierto(foco)) {
                Letra.mediana(g, font, Component.translatable("darkcraft.menu.talisman.cerrado"), x0(), dy, 0xFFE9C766, 0xFF050408);
                List<FormattedCharSequence> l = font.split(Component.translatable("darkcraft.menu.talisman.cerrado.desc"), ancho);
                for (int i = 0; i < Math.min(2, l.size()); i++) g.drawString(font, l.get(i), x0(), dy + 17 + i * 9, APAGADO, false);
            } else if (esFrasco(foco)) {
                boolean mana = foco == FRASCO_MANA;
                Letra.mediana(g, font, Component.translatable(mana ? "item.darkcraft.frasco_mana" : "item.darkcraft.frasco_vida"), x0(), dy, 0xFFE9C766, 0xFF050408);
                String cuanto = mana ? Integer.toString(Nivel.manaFrasco(ClientRol.manaMax, ClientRol.mejoraFrascos))
                        : vida(Nivel.curaFrasco(p.getMaxHealth(), ClientRol.mejoraFrascos));
                List<FormattedCharSequence> l = font.split(Component.translatable(mana ? "darkcraft.menu.frasco_mana.desc" : "darkcraft.menu.frasco_vida.desc", cuanto), ancho);
                for (int i = 0; i < Math.min(2, l.size()); i++) g.drawString(font, l.get(i), x0(), dy + 17 + i * 9, APAGADO, false);
            } else {
                ItemStack st = de(foco);
                if (st.isEmpty()) g.drawString(font, Component.translatable("darkcraft.menu.vacio"), x0(), dy + 2, GRIS, true);
                else {
                    Letra.mediana(g, font, Component.literal(recortar(st.getHoverName().getString(), ancho - 8)), x0(), dy, 0xFFE9C766, 0xFF050408);
                    List<FormattedCharSequence> l = ficha(st, ancho);
                    int caben = Math.max(0, (py + ph - 8 - (dy + 17)) / 9);
                    for (int i = 0; i < Math.min(caben, l.size()); i++) g.drawString(font, l.get(i), x0(), dy + 17 + i * 9, APAGADO, false);
                }
            }
            if (lista != null) dibujarLista(g, mx, my);
        }

        // ---- la lista de lo que se puede poner

        private int listaX() {
            Zona z = zona(foco);
            return Math.min(z.x + LADO + 6, px + pw - 12 - ANCHO_LISTA);
        }

        private int listaAlto() {
            return 12 + Math.min(FILAS, lista.size()) * 18;
        }

        private int listaY() {
            Zona z = zona(foco);
            return Mth.clamp(z.y - 6, py + 4, Math.max(py + 4, py + ph - 4 - listaAlto()));
        }

        private int listaEn(double mx, double my) {
            int x = listaX(), y = listaY();
            if (mx < x + 6 || mx >= x + ANCHO_LISTA - 6) return -1;
            for (int i = 0; i < Math.min(FILAS, lista.size()); i++) if (my >= y + 6 + i * 18 && my < y + 24 + i * 18) return listaDesde + i;
            return -1;
        }

        private void dibujarLista(GuiGraphics g, int mx, int my) {
            int x = listaX(), y = listaY();
            g.pose().pushPose();
            g.pose().translate(0, 0, 420);
            Pix.blanco();
            Panel.losa(g, x, y, ANCHO_LISTA, listaAlto());
            for (int i = 0; i < Math.min(FILAS, lista.size()); i++) {
                int k = listaDesde + i, fy = y + 6 + i * 18, hueco = lista.get(k);
                Pix.blanco();
                if (k == listaSel) Panel.fila(g, x + 5, fy, ANCHO_LISTA - 10);
                if (hueco < 0) {
                    g.drawString(font, Component.translatable("darkcraft.menu.quitar"), x + 30, fy + 5, k == listaSel ? LUZ : 0xFFE0A090, true);
                } else {
                    ItemStack st = inv().getItem(hueco);
                    g.renderItem(st, x + 9, fy + 1);
                    g.renderItemDecorations(font, st, x + 9, fy + 1);
                    g.drawString(font, recortar(st.getHoverName().getString(), ANCHO_LISTA - 44), x + 30, fy + 5, k == listaSel ? LUZ : CLARO, true);
                }
            }
            // las flechitas de que hay mas arriba o abajo
            Pix.blanco();
            if (listaDesde > 0) g.drawString(font, "▲", x + ANCHO_LISTA - 14, y + 4, ORO, false);
            if (listaDesde + FILAS < lista.size()) g.drawString(font, "▼", x + ANCHO_LISTA - 14, y + listaAlto() - 12, ORO, false);
            g.pose().popPose();
        }

        private void abrirLista() {
            if (esFrasco(foco) || (esTalisman(foco) && !abierto(foco))) {
                Panel.nada();
                return;
            }
            Inventory inv = inv();
            List<Integer> l = new ArrayList<>();
            if (esTalisman(foco)) {
                // los talismanes que se llevan en el inventario (en cualquier hueco)
                if (!de(foco).isEmpty()) l.add(-1);
                for (int i = 0; i < 36; i++) if (inv.getItem(i).getItem() instanceof com.darkcraft.rol.Talisman) l.add(i);
                if (l.isEmpty()) {
                    sinNada = t;
                    Panel.nada();
                    return;
                }
                lista = l;
                listaSel = l.get(0) < 0 && l.size() > 1 ? 1 : 0;
                listaDesde = 0;
                Panel.acepta();
                return;
            }
            if (!inv.getItem(foco).isEmpty()) l.add(-1);
            boolean arma = foco <= 4 || foco == Equipo.OTRA_MANO;
            // primero lo propio del hueco (armas en las manos, objetos en los rapidos), luego lo demas que quepa
            for (int pasada = 0; pasada < 2; pasada++)
                for (int i = Equipo.BOLSA_DESDE; i <= Equipo.BOLSA_HASTA; i++) {
                    ItemStack st = inv.getItem(i);
                    if (st.isEmpty() || !Equipo.cabe(yo(), foco, st)) continue;
                    Clase c = Clase.de(st);
                    boolean propio = Equipo.pieza(foco) != null || (arma ? c == Clase.ARMAS : c == Clase.OBJETOS);
                    if (propio == (pasada == 0)) l.add(i);
                }
            if (l.isEmpty()) {
                sinNada = t;
                Panel.nada();
                return;
            }
            lista = l;
            listaSel = l.get(0) < 0 && l.size() > 1 ? 1 : 0;
            listaDesde = 0;
            Panel.acepta();
        }

        private void elegirLista(int k) {
            int hueco = lista.get(k);
            if (esTalisman(foco)) {
                if (hueco < 0) Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.TALISMAN_QUITAR, foco - TALISMAN, 0));
                else Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.TALISMAN_PONER, foco - TALISMAN, hueco));
                lista = null;
                Panel.acepta();
                return;
            }
            if (hueco < 0) Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.GUARDAR, foco, 0));
            else Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.MOVER, hueco, foco));
            lista = null;
            Panel.acepta();
        }

        /** Quita lo del hueco elegido (a la bolsa); false si no hay nada que quitar. */
        private boolean quitar() {
            if (esFrasco(foco) || de(foco).isEmpty()) return false;
            if (esTalisman(foco)) Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.TALISMAN_QUITAR, foco - TALISMAN, 0));
            else Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.GUARDAR, foco, 0));
            return true;
        }

        private void moverLista(int d) {
            int n = Mth.clamp(listaSel + d, 0, lista.size() - 1);
            if (n == listaSel) return;
            listaSel = n;
            if (listaSel < listaDesde) listaDesde = listaSel;
            if (listaSel >= listaDesde + FILAS) listaDesde = listaSel - FILAS + 1;
            Panel.mueve();
        }

        // ---- moverse por los huecos

        private void mover(int dx, int dy) {
            Zona a = zona(foco);
            Zona mejor = null;
            double nota = Double.MAX_VALUE;
            for (Zona z : zonas) {
                if (z == a) continue;
                double vx = z.x - a.x, vy = z.y - a.y, hacia = vx * dx + vy * dy;
                if (hacia < 1) continue;
                double lado = Math.abs(vx * dy) + Math.abs(vy * dx), n = hacia + lado * 2.5;
                if (n < nota) {
                    nota = n;
                    mejor = z;
                }
            }
            if (mejor != null) {
                foco = mejor.id;
                Panel.mueve();
            }
        }

        @Override
        int teclas(GuiGraphics g, int x, int y) {
            return Panel.tecla(g, font, Component.literal("Supr"), Component.translatable("darkcraft.menu.quitar"), x, y, APAGADO) + 10;
        }

        @Override
        boolean tecla(int tecla, int scan) {
            if (lista != null) {
                if (tecla == GLFW.GLFW_KEY_ESCAPE || Teclas.MENU.matches(tecla, scan)) {
                    lista = null;
                    Panel.cancela();
                } else if (esArriba(minecraft, tecla, scan)) moverLista(-1);
                else if (esAbajo(minecraft, tecla, scan)) moverLista(1);
                else if (esElegir(tecla, scan)) elegirLista(listaSel);
                return true;
            }
            if (esArriba(minecraft, tecla, scan)) mover(0, -1);
            else if (esAbajo(minecraft, tecla, scan)) mover(0, 1);
            else if (esIzquierda(minecraft, tecla, scan)) mover(-1, 0);
            else if (esDerecha(minecraft, tecla, scan)) mover(1, 0);
            else if (esElegir(tecla, scan)) abrirLista();
            else if (esQuitar(tecla)) {
                if (quitar()) Panel.acepta();
                else Panel.nada();
            } else return false;
            return true;
        }

        @Override
        void raton(double mx, double my) {
            if (lista != null) {
                int k = listaEn(mx, my);
                if (k >= 0 && k != listaSel) {
                    listaSel = k;
                    Panel.mueve();
                }
                return;
            }
            for (Zona z : zonas)
                if (z.dentro(mx, my) && z.id != foco) {
                    foco = z.id;
                    Panel.mueve();
                }
        }

        @Override
        boolean clic(double mx, double my, int b) {
            if (lista != null) {
                int k = listaEn(mx, my);
                if (k >= 0 && b == 0) elegirLista(k);
                else {
                    lista = null;
                    Panel.cancela();
                }
                return true;
            }
            for (Zona z : zonas)
                if (z.dentro(mx, my)) {
                    foco = z.id;
                    if (b == 1 && quitar()) Panel.acepta();
                    else abrirLista();
                    return true;
                }
            int cw = Mth.clamp(pw / 4, 98, 150), cx = px + pw - 12 - cw;
            if (mx >= cx && mx < cx + cw && my >= py + 10 && my < py + ph - 10) {
                girando = true;
                return true;
            }
            return false;
        }

        @Override
        boolean rueda(double mx, double my, double r) {
            if (lista == null) return false;
            moverLista(r > 0 ? -1 : 1);
            return true;
        }
    }

    // ================================================================== INVENTARIO

    private final class PBolsa extends Pagina {
        private static final int COLUMNAS = 9, PASO_C = 24, FICHA_MAX = 220;
        private Clase clase = Clase.TODO;
        private final List<Integer> cosas = new ArrayList<>();
        private int sel, desde, tirar = -1000;
        private boolean enClases;

        /** Lo que ocupa la rejilla y la ficha juntas: en pantallas anchas se centra (y la ficha no pasa de FICHA_MAX). */
        private int fichaAncho() {
            return Math.min(FICHA_MAX, px + pw - 12 - (px + 14 + COLUMNAS * PASO_C + 6));
        }

        private int gx() {
            int todo = COLUMNAS * PASO_C + 6 + fichaAncho();
            return Math.max(px + 14, px + (pw - todo) / 2);
        }

        private int gy() {
            return py + 40;
        }

        private int filas() {
            return Mth.clamp((ph - 40 - 22) / PASO_C, 1, 5);
        }

        private int fx() {
            return gx() + COLUMNAS * PASO_C + 6;
        }

        @Override
        void medir() {
            leer();
        }

        @Override
        void tick() {
            leer();
        }

        /** Lo que se lleva de la clase elegida: primero la bolsa y luego lo equipado. */
        private void leer() {
            Inventory inv = inv();
            int antes = sel >= 0 && sel < cosas.size() ? cosas.get(sel) : -1;
            cosas.clear();
            for (int pasada = 0; pasada < 2; pasada++)
                for (int i = 0; i < Equipo.HUECOS; i++) {
                    if (Equipo.enBolsa(i) != (pasada == 0)) continue;
                    ItemStack st = inv.getItem(i);
                    if (st.isEmpty() || (clase != Clase.TODO && Clase.de(st) != clase)) continue;
                    cosas.add(i);
                }
            // lo elegido sigue elegido aunque cambie de sitio en la lista
            int k = cosas.indexOf(antes);
            if (k >= 0) sel = k;
            sel = Mth.clamp(sel, 0, Math.max(0, cosas.size() - 1));
            if (cosas.isEmpty()) enClases = true;
            int f = filas();
            if (sel / COLUMNAS < desde) desde = sel / COLUMNAS;
            if (sel / COLUMNAS >= desde + f) desde = sel / COLUMNAS - f + 1;
            desde = Mth.clamp(desde, 0, Math.max(0, (cosas.size() - 1) / COLUMNAS - f + 1));
        }

        private int enUso() {
            int n = 0;
            for (int i = Equipo.BOLSA_DESDE; i <= Equipo.BOLSA_HASTA; i++) if (!inv().getItem(i).isEmpty()) n++;
            return n;
        }

        @Override
        void dibujar(GuiGraphics g, int mx, int my, float pt) {
            Clase[] clases = Clase.values();
            for (int i = 0; i < clases.length; i++) {
                int x = gx() + i * PASO, y = py + 10;
                boolean elegida = clases[i] == clase;
                Pix.blanco();
                Panel.t(g, elegida ? MenuTex.RANURA_LUZ : MenuTex.RANURA, x, y);
                Pix.color(1f, 1f, 1f, elegida ? 1f : 0.6f);
                Panel.t(g, clases[i].icono, x + 3, y + 3);
                Pix.blanco();
                if (elegida && enClases) cursor(g, x, y, LADO, LADO);
            }
            Letra.mediana(g, font, Component.translatable("darkcraft.menu.clase." + clase.clave), gx() + clases.length * PASO + 6, py + 14, 0xFFE9C766, 0xFF050408);
            // la rejilla
            int f = filas();
            for (int r = 0; r < f; r++)
                for (int c = 0; c < COLUMNAS; c++) {
                    int k = (desde + r) * COLUMNAS + c, x = gx() + c * PASO_C, y = gy() + r * PASO_C;
                    if (k < cosas.size()) {
                        int hueco = cosas.get(k);
                        ranura(g, x, y, inv().getItem(hueco), null, k == sel && !enClases, !Equipo.enBolsa(hueco));
                    } else {
                        Pix.color(1f, 1f, 1f, 0.35f);
                        Panel.t(g, MenuTex.RANURA, x, y);
                        Pix.blanco();
                    }
                }
            g.drawString(font, Component.translatable("darkcraft.menu.bolsa", enUso(), Equipo.BOLSA_HASTA - Equipo.BOLSA_DESDE + 1), gx(),
                    Math.min(py + ph - 17, gy() + f * PASO_C + 4), APAGADO, true);
            // la ficha de lo elegido
            int x = fx(), y = py + 10, w = fichaAncho(), h = ph - 20;
            if (w < 90) return;
            Pix.blanco();
            Panel.hueco(g, x, y, w, h);
            if (cosas.isEmpty() || enClases) {
                if (cosas.isEmpty()) {
                    List<FormattedCharSequence> l = font.split(Component.translatable("darkcraft.menu.clase.vacia"), w - 16);
                    for (int i = 0; i < l.size(); i++) g.drawString(font, l.get(i), x + 8, y + 10 + i * 9, GRIS, false);
                }
                return;
            }
            int hueco = cosas.get(sel);
            ItemStack st = inv().getItem(hueco);
            if (st.isEmpty()) return;
            g.pose().pushPose();
            g.pose().translate(x + 7, y + 7, 0);
            g.pose().scale(2f, 2f, 1f);
            g.renderItem(st, 0, 0);
            g.pose().popPose();
            Pix.mezcla();
            // el nombre, en dos lineas si hace falta
            List<FormattedCharSequence> nombre = font.split(Letra.m(st.getHoverName()), w - 50);
            for (int i = 0; i < Math.min(2, nombre.size()); i++) Letra.contorno(g, font, nombre.get(i), x + 44, y + 8 + Letra.BAJA_M + i * 15, 0xFFE9C766, 0xFF050408);
            boolean equipado = !Equipo.enBolsa(hueco);
            int ly = y + 44;
            g.drawString(font, Component.translatable("darkcraft.menu.clase." + Clase.de(st).clave).append(st.getCount() > 1 ? "  ×" + st.getCount() : ""), x + 8, ly, ORO, true);
            if (equipado) {
                Component e = Component.translatable("darkcraft.menu.equipado");
                g.drawString(font, e, x + w - 8 - font.width(e), ly, 0xFFDE3A3A, true);
            }
            Panel.raya(g, x + 6, x + w - 6, ly + 11);
            List<FormattedCharSequence> l = ficha(st, w - 16);
            int caben = Math.max(0, (y + h - 32 - (ly + 17)) / 9);
            for (int i = 0; i < Math.min(caben, l.size()); i++) g.drawString(font, l.get(i), x + 8, ly + 17 + i * 9, APAGADO, false);
            // lo que se puede hacer
            int ay = y + h - 29;
            Panel.tecla(g, font, minecraft.options.keyInventory.getTranslatedKeyMessage(),
                    Component.translatable(equipado ? "darkcraft.menu.quitar" : "darkcraft.menu.equipar"), x + 8, ay, CLARO);
            boolean dudando = t - tirar < 40;
            Panel.tecla(g, font, Component.literal("Supr"), Component.translatable(dudando ? "darkcraft.menu.tirar.seguro" : "darkcraft.menu.tirar"), x + 8, ay + 14,
                    dudando ? 0xFFE0564E : CLARO);
        }

        /** Equipa lo de la bolsa donde toque segun lo que sea, o guarda lo que estaba equipado. */
        private void accion() {
            if (cosas.isEmpty()) return;
            int hueco = cosas.get(sel);
            Inventory inv = inv();
            ItemStack st = inv.getItem(hueco);
            if (st.getItem() instanceof com.darkcraft.rol.Talisman) {
                // un talisman va a su hueco: el primero libre de los abiertos (o en lugar del primero)
                int h = -1;
                for (int i = 0; i < ClientRol.huecosTalisman && h < 0; i++) if (ClientRol.talismanes[i].isEmpty()) h = i;
                Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.TALISMAN_PONER, Math.max(0, h), hueco));
                Panel.acepta();
                return;
            }
            if (!Equipo.enBolsa(hueco)) {
                if (Equipo.libre(yo()) < 0) {
                    Panel.nada();
                    return;
                }
                Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.GUARDAR, hueco, 0));
                Panel.acepta();
                return;
            }
            int destino = -1;
            Clase c = Clase.de(st);
            if (c == Clase.ARMADURAS) {
                Equipable eq = Equipable.get(st);
                EquipmentSlot pieza = eq != null ? eq.getEquipmentSlot() : null;
                for (int a : Equipo.ARMADURA) if (Equipo.pieza(a) == pieza) destino = a;
            } else if (st.getItem() instanceof ShieldItem) {
                destino = primeroLibre(inv, Equipo.IZQUIERDA);
                if (destino < 0) destino = Equipo.OTRA_MANO;
            } else if (c == Clase.OBJETOS) {
                destino = primeroLibre(inv, Equipo.RAPIDOS);
                if (destino < 0) destino = Equipo.RAPIDOS[0];
            } else {
                destino = primeroLibre(inv, Equipo.DERECHA);
                if (destino < 0) destino = c == Clase.ARMAS ? Mth.clamp(inv.selected, 0, 2) : primeroLibre(inv, Equipo.RAPIDOS);
            }
            if (destino < 0 || !Equipo.cabe(yo(), destino, st)) {
                Panel.nada();
                return;
            }
            Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.MOVER, hueco, destino));
            Panel.acepta();
        }

        private static int primeroLibre(Inventory inv, int[] huecos) {
            for (int h : huecos) if (inv.getItem(h).isEmpty()) return h;
            return -1;
        }

        private void tirar() {
            if (cosas.isEmpty() || enClases) return;
            if (t - tirar < 40) {
                Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.TIRAR, cosas.get(sel), 0));
                tirar = -1000;
                Panel.acepta();
            } else {
                tirar = t;
                Panel.mueve();
            }
        }

        private void elegirClase(int i) {
            Clase[] clases = Clase.values();
            Clase n = clases[Math.floorMod(i, clases.length)];
            if (n == clase) return;
            clase = n;
            sel = 0;
            desde = 0;
            leer();
            Panel.mueve();
        }

        private void mover(int d) {
            int n = sel + d;
            if (n < 0 || n >= cosas.size()) return;
            // de lado no se salta de fila
            if (Math.abs(d) == 1 && n / COLUMNAS != sel / COLUMNAS) return;
            sel = n;
            tirar = -1000;
            leer();
            Panel.mueve();
        }

        @Override
        int teclas(GuiGraphics g, int x, int y) {
            return x;
        }

        @Override
        boolean tecla(int tecla, int scan) {
            if (enClases) {
                if (esIzquierda(minecraft, tecla, scan)) elegirClase(clase.ordinal() - 1);
                else if (esDerecha(minecraft, tecla, scan)) elegirClase(clase.ordinal() + 1);
                else if ((esAbajo(minecraft, tecla, scan) || esElegir(tecla, scan)) && !cosas.isEmpty()) {
                    enClases = false;
                    Panel.mueve();
                } else return esElegir(tecla, scan) || esAbajo(minecraft, tecla, scan);
                return true;
            }
            if (esArriba(minecraft, tecla, scan)) {
                if (sel < COLUMNAS) {
                    enClases = true;
                    Panel.mueve();
                } else mover(-COLUMNAS);
            } else if (esAbajo(minecraft, tecla, scan)) mover(sel + COLUMNAS < cosas.size() ? COLUMNAS : 0);
            else if (esIzquierda(minecraft, tecla, scan)) mover(-1);
            else if (esDerecha(minecraft, tecla, scan)) mover(1);
            else if (esElegir(tecla, scan)) accion();
            else if (esQuitar(tecla)) tirar();
            else return false;
            return true;
        }

        private int claseEn(double mx, double my) {
            if (my < py + 10 || my >= py + 10 + LADO) return -1;
            for (int i = 0; i < Clase.values().length; i++) if (mx >= gx() + i * PASO && mx < gx() + i * PASO + LADO) return i;
            return -1;
        }

        private int cosaEn(double mx, double my) {
            int c = (int) Math.floor((mx - gx()) / PASO_C), r = (int) Math.floor((my - gy()) / PASO_C);
            if (c < 0 || c >= COLUMNAS || r < 0 || r >= filas() || mx - gx() - c * PASO_C >= LADO || my - gy() - r * PASO_C >= LADO) return -1;
            int k = (desde + r) * COLUMNAS + c;
            return k < cosas.size() ? k : -1;
        }

        @Override
        void raton(double mx, double my) {
            int k = cosaEn(mx, my);
            if (k >= 0 && (k != sel || enClases)) {
                sel = k;
                enClases = false;
                tirar = -1000;
                Panel.mueve();
            }
        }

        @Override
        boolean clic(double mx, double my, int b) {
            int c = claseEn(mx, my);
            if (c >= 0) {
                elegirClase(c);
                enClases = cosas.isEmpty();
                return true;
            }
            int k = cosaEn(mx, my);
            if (k >= 0) {
                sel = k;
                enClases = false;
                accion();
                return true;
            }
            return false;
        }

        @Override
        boolean rueda(double mx, double my, double r) {
            int f = filas(), total = (cosas.size() + COLUMNAS - 1) / COLUMNAS;
            if (total <= f) return false;
            desde = Mth.clamp(desde + (r > 0 ? -1 : 1), 0, total - f);
            sel = Mth.clamp(sel, desde * COLUMNAS, Math.min(cosas.size() - 1, (desde + f) * COLUMNAS - 1));
            return true;
        }
    }

    // ================================================================== ESTADO

    private final class PEstado extends Pagina {
        private final MenuTex.T[] iconos = {MenuTex.AT_VIGOR, MenuTex.AT_MENTE, MenuTex.AT_AGUANTE, MenuTex.AT_FUERZA, MenuTex.AT_DESTREZA,
                MenuTex.AT_INTELIGENCIA, MenuTex.AT_FE, MenuTex.AT_ARCANO};

        @Override
        void dibujar(GuiGraphics g, int mx, int my, float pt) {
            LocalPlayer p = yo();
            int x = px + 14, y = py + 11;
            int cw = Mth.clamp(pw / 4, 98, 150), cx = px + pw - 12 - cw;
            int a = ClientRol.atributos.length;
            // a la izquierda: nivel, ecos y atributos
            int w1 = Math.min(150, (cx - 10 - x - 8) / 2);
            Letra.mediana(g, font, Component.translatable("darkcraft.menu.nivel").append(" " + ClientRol.nivel()), x, y, 0xFFE9C766, 0xFF050408);
            Pix.blanco();
            Pix.t(g, HudTex.ECO_0, x, y + 15);
            g.drawString(font, Hud.numero(ClientRol.ecos), x + 18, y + 20, ORO, true);
            Panel.raya(g, x - 4, x + w1, y + 34);
            int alto = Mth.clamp((py + ph - 10 - (y + 40)) / a, 12, 17);
            for (int i = 0; i < a; i++) {
                int fy = y + 40 + i * alto;
                Pix.blanco();
                Panel.t(g, iconos[i], x, fy + (alto - 16) / 2);
                g.drawString(font, Component.translatable("darkcraft.atributo." + Atributo.values()[i].clave), x + 20, fy + (alto - 8) / 2, CLARO, true);
                Component v = Component.literal(Integer.toString(ClientRol.atributos[i]));
                Letra.mediana(g, font, v, x + w1 - 4 - Letra.anchoM(font, v), fy + (alto - 13) / 2, ORO, 0xFF050408);
            }
            // en medio: lo que dan
            int x2 = x + w1 + 10, w2 = cx - 10 - x2;
            if (w2 >= 90) {
                Pix.blanco();
                Panel.hueco(g, x2, y - 1, w2, Math.min(ph - 20, 152));
                int sy = y + 6, x21 = x2 + w2 - 7;
                int[] at = ClientRol.atributos;
                dato(g, x2 + 7, x21, sy, "darkcraft.menu.vida", vida(p.getHealth()) + " / " + vida(p.getMaxHealth()), ORO);
                dato(g, x2 + 7, x21, sy + 13, "darkcraft.menu.mana", ArmasCliente.energiaCliente() + " / " + ClientRol.manaMax, ORO);
                dato(g, x2 + 7, x21, sy + 26, "darkcraft.menu.estamina", Integer.toString(Math.round(ClientRol.estaminaMax)), ORO);
                dato(g, x2 + 7, x21, sy + 39, "darkcraft.menu.ataque", String.format(Locale.ROOT, "%.1f", ataque(p)), ORO);
                dato(g, x2 + 7, x21, sy + 52, "darkcraft.menu.defensa", Integer.toString(p.getArmorValue()), ORO);
                dato(g, x2 + 7, x21, sy + 65, "darkcraft.menu.peso", peso(p), colorPeso(com.darkcraft.client.combate.ClientCombate.clasePeso(p)));
                long sig = Nivel.coste(ClientRol.nivel());
                dato(g, x2 + 7, x21, sy + 78, "darkcraft.menu.siguiente", Hud.numero((int) Math.min(Integer.MAX_VALUE, sig)), sig <= ClientRol.ecos ? VERDE : APAGADO);
                Panel.raya(g, x2 + 6, x2 + w2 - 6, sy + 91);
                // las resistencias (de los talismanes y de los frutos que se han comido), en dos columnas
                com.darkcraft.rol.Resistencias.Tipo[] ts = com.darkcraft.rol.Resistencias.Tipo.values();
                int cw2 = (w2 - 14 - 8) / 2;
                for (int i = 0; i < ts.length; i++) {
                    int rx = x2 + 7 + (i % 2) * (cw2 + 8), ry = sy + 97 + (i / 2) * 11;
                    float r = com.darkcraft.rol.Resistencias.de(ClientRol.talismanes, p, ts[i]);
                    String v = Math.round(r * 100f) + " %";
                    g.drawString(font, Component.translatable("darkcraft.resistencia." + ts[i].id), rx, ry, r > 0f ? CLARO : GRIS, true);
                    g.drawString(font, v, rx + cw2 - font.width(v), ry, r > 0f ? VERDE : GRIS, true);
                }
            }
            personaje(g, cx, py + 10, cw, ph - 20);
        }

        @Override
        boolean clic(double mx, double my, int b) {
            int cw = Mth.clamp(pw / 4, 98, 150), cx = px + pw - 12 - cw;
            if (mx >= cx && mx < cx + cw && my >= py + 10 && my < py + ph - 10) {
                girando = true;
                return true;
            }
            return false;
        }
    }

    // ================================================================== SISTEMA

    private final class PSistema extends Pagina {
        private final String[] opciones = {"mapa", "controles", "opciones", "volver"};
        private int sel;

        private int ancho() {
            return Math.min(220, pw - 40);
        }

        private int fx() {
            return px + (pw - ancho()) / 2;
        }

        private int fy(int i) {
            return py + 26 + i * 22;
        }

        @Override
        void dibujar(GuiGraphics g, int mx, int my, float pt) {
            for (int i = 0; i < opciones.length; i++) {
                Pix.blanco();
                if (i == sel) Panel.fila(g, fx(), fy(i), ancho());
                Component c = Component.translatable("darkcraft.menu.sistema." + opciones[i]);
                g.drawString(font, c, fx() + (ancho() - font.width(c)) / 2, fy(i) + 5, i == sel ? LUZ : 0xFFC8B89A, true);
            }
            List<FormattedCharSequence> l = font.split(Component.translatable("darkcraft.menu.sistema." + opciones[sel] + ".desc"), ancho() + 60);
            for (int i = 0; i < Math.min(3, l.size()); i++) {
                int w = font.width(l.get(i));
                g.drawString(font, l.get(i), px + (pw - w) / 2, fy(opciones.length) + 10 + i * 9, APAGADO, false);
            }
        }

        private void elegir() {
            Panel.acepta();
            switch (opciones[sel]) {
                case "mapa" -> com.darkcraft.client.espejo.LibroScreen.abrir();
                case "controles" -> minecraft.setScreen(new net.minecraft.client.gui.screens.controls.KeyBindsScreen(MenuScreen.this, minecraft.options));
                case "opciones" -> minecraft.setScreen(new net.minecraft.client.gui.screens.OptionsScreen(MenuScreen.this, minecraft.options));
                default -> minecraft.setScreen(null);
            }
        }

        @Override
        boolean tecla(int tecla, int scan) {
            if (esArriba(minecraft, tecla, scan)) {
                sel = Math.floorMod(sel - 1, opciones.length);
                Panel.mueve();
            } else if (esAbajo(minecraft, tecla, scan)) {
                sel = Math.floorMod(sel + 1, opciones.length);
                Panel.mueve();
            } else if (esElegir(tecla, scan)) elegir();
            else return false;
            return true;
        }

        private int en(double mx, double my) {
            if (mx < fx() || mx >= fx() + ancho()) return -1;
            for (int i = 0; i < opciones.length; i++) if (my >= fy(i) && my < fy(i) + 18) return i;
            return -1;
        }

        @Override
        void raton(double mx, double my) {
            int i = en(mx, my);
            if (i >= 0 && i != sel) {
                sel = i;
                Panel.mueve();
            }
        }

        @Override
        boolean clic(double mx, double my, int b) {
            int i = en(mx, my);
            if (i < 0 || b != 0) return false;
            sel = i;
            elegir();
            return true;
        }
    }

    /** Para la prueba de la CI. */
    public String resumen() {
        String s = "menu pestana " + PESTANAS[pestana];
        if (paginas[pestana] instanceof PEquipo e) s += " foco " + e.foco + (e.lista != null ? " lista " + e.lista + " sel " + e.listaSel : "");
        if (paginas[pestana] instanceof PBolsa b) s += " clase " + b.clase + " cosas " + b.cosas + " sel " + b.sel + (b.enClases ? " (en clases)" : "");
        return s;
    }
}
