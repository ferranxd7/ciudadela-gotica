package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Combos;
import com.darkcraft.armas.EspadaRey;
import com.darkcraft.armas.MandobleJuramento;
import com.darkcraft.client.armas.ArmasCliente;
import com.darkcraft.client.espejo.EspejoCliente;
import com.darkcraft.client.espejo.EspejoHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * El HUD del mod, en pixel art (piedra oscura, oro gastado y rojo), en vez del de Minecraft:
 * <ul>
 * <li>arriba a la izquierda, el emblema de la Sombra y las barras de <b>vida</b> (roja), <b>mana</b> (azul: la energia
 * de las armas) y <b>estamina</b> (verde); nada de corazones, comida, armadura ni experiencia;</li>
 * <li>abajo a la derecha, el contador de <b>Ecos de la Sombra</b>;</li>
 * <li>abajo a la izquierda, la <b>cruz del equipo</b> ({@link Cruz}: las dos manos, el objeto rapido con los frascos);</li>
 * <li>abajo en el centro, lo que se va recogiendo ({@link Conseguidos});</li>
 * <li>arriba en el centro, la <b>brujula</b>;</li>
 * <li>encima de la barra de objetos, los avisos de "Pulsa [E]...".</li>
 * </ul>
 * Las barras crecen con los atributos (mas Vigor, barra de vida mas larga; lo mismo la Mente y el Aguante).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class Hud {
    private Hud() {}

    static final ResourceLocation BRILLO = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/brillo.png");
    /** Donde va el emblema; las barras salen de su derecha. */
    private static final int X0 = 3, Y0 = 3;

    /** Lo que mide la barra de mana (76 con 100 de mana) y la de estamina (96 con 100), sin pasarse de media pantalla. */
    private static int largoMana(int w) {
        return Mth.clamp(Math.round(46 + ClientRol.manaMax * 0.3f), 50, Math.max(60, Math.min(200, w / 2 - 44)));
    }

    private static int largoEstamina(int w) {
        return Mth.clamp(Math.round(46 + ClientRol.estaminaMax * 0.5f), 50, Math.max(60, Math.min(230, w / 2 - 44)));
    }

    // ------------------------------------------------------------------ lo que se anima (un paso por tick)

    private static float vidaAnt = -1, vidaVista = -1, rastro, manaVista, estaminaVista = 100f, avisoAlfa;
    private static int rastroTick = -1000, vidaBrillo = -1000, manaBrillo = -1000, estBrillo = -1000, jefeTick = -1000;
    private static int manaAnt, avisoQue;
    private static boolean estLlena = true, avisoEncendido;
    private static String avisoCosa = "";

    /** Lo llama {@link ClientRol} en cada tick del cliente. */
    static void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        int t = ClientRol.ticks;
        com.darkcraft.montura.Corcel montado = corcel(p);
        if (montado != null && corcelRastro > montado.getHealth()) corcelRastro = Math.max(montado.getHealth(), corcelRastro - 0.6f);
        float vida = p.getHealth(), max = Math.max(1f, p.getMaxHealth());
        if (vidaAnt < 0 || !p.isAlive()) {
            vidaAnt = vida;
            vidaVista = vida;
            rastro = vida;
        }
        if (vida < vidaAnt - 0.01f) {
            rastro = Math.max(rastro, vidaAnt);
            rastroTick = t;
        } else if (vida > vidaAnt + 0.9f) vidaBrillo = t;
        vidaAnt = vida;
        if (t - rastroTick > 14) rastro = Math.max(vida, rastro - max * 0.03f);
        if (rastro < vida) rastro = vida;
        // lo que se pierde, de golpe (lo cuenta el rastro); lo que se cura, sube deprisa pero se ve subir
        if (vidaVista < 0 || vida <= vidaVista) vidaVista = vida;
        else vidaVista = Math.min(vida, vidaVista + Math.max(0.4f, max * 0.045f));

        int mana = ArmasCliente.energiaCliente();
        manaVista += Mth.clamp(mana - manaVista, -3f, Math.max(3f, ClientRol.manaMax * 0.05f));
        if ((manaAnt < Combos.COSTE_1 && mana >= Combos.COSTE_1) || (manaAnt < Combos.COSTE_2 && mana >= Combos.COSTE_2)) manaBrillo = t;
        manaAnt = mana;

        float paso = Math.max(6f, ClientRol.estaminaMax * 0.06f);
        estaminaVista += Mth.clamp(ClientRol.estamina - estaminaVista, -paso, paso);
        boolean llena = ClientRol.estamina >= ClientRol.estaminaMax - 0.05f;
        if (llena && !estLlena) estBrillo = t;
        estLlena = llena;

        int que = mc.screen == null ? ClientRol.aMano() : ClientRol.NADA;
        if (que != ClientRol.NADA) {
            avisoQue = que;
            avisoEncendido = ClientSantuario.aManoEncendido;
            if (que == ClientRol.COSA) avisoCosa = ClientPueblo.clave();
        }
        avisoAlfa = Mth.clamp(avisoAlfa + (que != ClientRol.NADA ? 0.14f : -0.2f), 0f, 1f);
    }

    // ------------------------------------------------------------------ fuera el HUD de Minecraft

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Pre e) {
        var o = e.getOverlay();
        if (o == VanillaGuiOverlay.PLAYER_HEALTH.type() || o == VanillaGuiOverlay.FOOD_LEVEL.type()
                || o == VanillaGuiOverlay.ARMOR_LEVEL.type() || o == VanillaGuiOverlay.EXPERIENCE_BAR.type()
                || o == VanillaGuiOverlay.POTION_ICONS.type() || o == VanillaGuiOverlay.MOUNT_HEALTH.type()
                || o == VanillaGuiOverlay.JUMP_BAR.type())
            e.setCanceled(true);
    }

    /** Mientras hay una barra de jefe arriba, la brujula se quita de en medio. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onJefe(CustomizeGuiOverlayEvent.BossEventProgress e) {
        jefeTick = ClientRol.ticks;
    }

    // ------------------------------------------------------------------ dibujo

    /** Con una pantalla grande del mod delante (el libro, el menu, subir de nivel) el HUD no se dibuja: estorbaria. */
    public static boolean tapado(Minecraft mc) {
        return mc.screen instanceof com.darkcraft.client.espejo.LibroScreen || mc.screen instanceof MenuScreen || mc.screen instanceof NivelScreen;
    }

    public static void render(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.options.hideGui || p.isSpectator() || !p.isAlive()) return;
        if (tapado(mc)) return;
        float t = ClientRol.ticks + pt;
        int fin = X0 + 24;
        // la marca del objetivo fijado, debajo de todo lo demas
        com.darkcraft.client.combate.Fijar.dibujar(g, w, h, pt);
        if (gui.shouldDrawSurvivalElements()) {
            fin = barras(mc, g, p, t, w);
            efectos(mc, g, p, t);
            // abajo a la izquierda, la cruz del equipo (lleva los frascos en su hueco de abajo)
            if (!(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen) && !(mc.screen instanceof MenuScreen) && ClientEquipo.activo())
                Cruz.dibujar(mc, g, t, h);
        }
        ecos(mc, g, t, w, h);
        if (mc.screen == null) Conseguidos.dibujar(mc, g, t, w, h);
        if (!mc.options.renderDebug && ClientRol.ticks - jefeTick > 3) brujula(mc, g, p, pt, w, fin);
        if (ClientPueblo.marcador() && !mc.options.renderDebug && mc.screen == null) piezas(g, t, w, h);
        if (avisoAlfa > 0.02f) avisoDeTecla(mc, g, w, h);
        Pix.blanco();
        Pix.mezcla();
    }

    // ---- los efectos (frutos, pociones): en fila bajo las barras, con lo que les queda

    /** Lo que duraba cada efecto al ponerse (para la rayita de lo que le queda). */
    private static final java.util.Map<net.minecraft.world.effect.MobEffect, Integer> DURA = new java.util.HashMap<>();

    private static void efectos(Minecraft mc, GuiGraphics g, LocalPlayer p, float t) {
        var activos = p.getActiveEffects();
        if (activos.isEmpty()) {
            DURA.clear();
            return;
        }
        java.util.List<net.minecraft.world.effect.MobEffectInstance> l = new java.util.ArrayList<>(activos);
        l.removeIf(m -> !m.showIcon());
        // los buenos primero, y entre ellos los que antes se acaban
        l.sort((a, b) -> a.getEffect().isBeneficial() != b.getEffect().isBeneficial() ? (a.getEffect().isBeneficial() ? -1 : 1)
                : Integer.compare(a.isInfiniteDuration() ? Integer.MAX_VALUE : a.getDuration(), b.isInfiniteDuration() ? Integer.MAX_VALUE : b.getDuration()));
        var tex = mc.getMobEffectTextures();
        int x = X0 + 22, y = Y0 + 27 + (corcel(p) != null ? 10 : 0);
        for (int i = 0; i < Math.min(8, l.size()); i++) {
            var m = l.get(i);
            int dur = m.getDuration();
            int max = Math.max(dur, DURA.getOrDefault(m.getEffect(), dur));
            DURA.put(m.getEffect(), max);
            float f = m.isInfiniteDuration() ? 1f : dur / (float) Math.max(1, max);
            // en los ultimos diez segundos parpadea
            boolean apaga = !m.isInfiniteDuration() && dur < 200 && ((int) (t / 5f)) % 2 == 0;
            g.fill(x - 1, y - 1, x + 19, y + 19, 0x8C050408);
            g.fill(x - 1, y + 19, x + 19, y + 21, 0xC0050408);
            g.fill(x, y + 19, x + Math.max(1, Math.round(18 * f)), y + 20, m.getEffect().isBeneficial() ? 0xFFE2BF62 : 0xFFDE3A3A);
            Pix.mezcla();
            Pix.color(1f, 1f, 1f, apaga ? 0.4f : 1f);
            g.blit(x, y, 0, 18, 18, tex.get(m.getEffect()));
            Pix.blanco();
            if (m.getAmplifier() > 0) g.drawString(mc.font, Integer.toString(m.getAmplifier() + 1), x + 13, y + 10, 0xFFF6E3A0, true);
            x += 22;
        }
        DURA.keySet().removeIf(e -> !p.hasEffect(e));
    }

    // ---- las barras

    private static int barras(Minecraft mc, GuiGraphics g, LocalPlayer p, float t, int w) {
        int x = X0 + 22;
        float max = Math.max(1f, p.getMaxHealth()), vida = Mth.clamp(p.getHealth(), 0f, max);
        int largoVida = Mth.clamp(Math.round(48 + max * 3.6f), 60, Math.max(60, Math.min(260, w / 2 - 44)));
        // con una barra de jefe arriba en el centro, la de vida no se mete debajo
        if (ClientRol.ticks - jefeTick <= 3) largoVida = Math.max(60, Math.min(largoVida, w / 2 - 128 - 14 - x - 11));

        // --- vida
        HudTex.T relleno = HudTex.VIDA;
        float cr = 1f, cg = 1f, cb = 1f;
        if (p.hasEffect(MobEffects.WITHER)) {
            cr = 0.34f;
            cg = 0.26f;
            cb = 0.3f;
        } else if (p.hasEffect(MobEffects.POISON)) relleno = HudTex.ESTAMINA;
        else if (p.isFullyFrozen()) {
            relleno = HudTex.MANA;
            cr = 1.2f;
            cg = 1.3f;
            cb = 1.3f;
        }
        boolean poca = vida / max < 0.26f;
        if (poca) {
            float late = 1.0f + 0.4f * (0.5f + 0.5f * Mth.sin(t * 0.38f));
            cr *= late;
            cg *= late;
            cb *= late;
        }
        float vista = vidaVista < 0 ? vida : Math.min(vida, vidaVista);
        barra(g, x, Y0, largoVida, vista / max, rastro / max, relleno, cr, cg, cb);
        // absorcion (corazones dorados del juego): un tramo dorado a continuacion de la vida
        float abs = p.getAbsorptionAmount();
        if (abs > 0.01f) {
            int wf = Math.round(largoVida * vida / max), wa = Math.min(largoVida - wf, Math.max(1, Math.round(largoVida * abs / max)));
            if (wa > 0) {
                Pix.color(1f, 0.92f, 0.55f, 1f);
                Pix.tira(g, HudTex.RASTRO, x + 2 + wf, Y0 + 2, wa);
                Pix.blanco();
            }
        }
        destello(g, x, Y0, largoVida, vida / max, t - vidaBrillo, 1f, 0.9f, 0.9f);

        // --- mana (la energia de las armas)
        int largoMana = largoMana(w), largoEst = largoEstamina(w);
        float fm = Mth.clamp(manaVista / ClientRol.manaMax, 0f, 1f);
        barra(g, x, Y0 + 8, largoMana, fm, 0f, HudTex.MANA, 1f, 1f, 1f);
        if (EspadaRey.es(p.getMainHandItem()) || EspadaRey.es(p.getOffhandItem()) || MandobleJuramento.es(p.getMainHandItem()))
            for (int c : new int[]{Combos.COSTE_1, Combos.COSTE_2}) {
                int mx = x + 2 + Math.round(largoMana * c / (float) ClientRol.manaMax);
                boolean hay = manaVista >= c;
                g.fill(mx, Y0 + 10, mx + 1, Y0 + 15, hay ? 0xFFF6E3A0 : 0xC0050408);
                g.fill(mx - 1, Y0 + 9, mx + 2, Y0 + 10, hay ? 0xFFF6E3A0 : 0xFF6E4E1C);
            }
        destello(g, x, Y0 + 8, largoMana, fm, t - manaBrillo, 0.85f, 0.92f, 1f);

        // --- estamina
        float fe = Mth.clamp(estaminaVista / ClientRol.estaminaMax, 0f, 1f);
        float oscuro = ClientRol.agotado ? 0.55f : 1f;
        barra(g, x, Y0 + 16, largoEst, fe, 0f, HudTex.ESTAMINA, oscuro, oscuro, oscuro);
        destello(g, x, Y0 + 16, largoEst, fe, t - estBrillo, 0.9f, 1f, 0.9f);
        float sin = 1f - (t - ClientRol.sinEstaminaTick) / 12f;
        if (ClientRol.agotado) sin = Math.max(sin, 0.3f + 0.25f * Mth.sin(t * 0.6f));
        if (sin > 0.02f) g.fill(x + 2, Y0 + 18, x + 3 + largoEst, Y0 + 23, (Math.round(150 * Math.min(1f, sin)) << 24) | 0xB81C26);

        // --- montado en el corcel (parte 7b): su vida, con una herradura delante
        int largoCorcel = 0;
        com.darkcraft.montura.Corcel corcel = corcel(p);
        if (corcel != null) {
            largoCorcel = 64;
            float cm = Math.max(1f, corcel.getMaxHealth()), cv = Mth.clamp(corcel.getHealth(), 0f, cm);
            if (corcelRastro < cv) corcelRastro = cv;
            Pix.blanco();
            Pix.t(g, HudTex.CORCEL_ICONO, x + 1, Y0 + 24);
            barra(g, x + 11, Y0 + 24, largoCorcel, cv / cm, corcelRastro / cm, HudTex.CORCEL, 1f, 1f, 1f);
            largoCorcel += 11;
        } else corcelRastro = 0f;

        // --- el emblema, encima del arranque de las barras; con poca vida, el ojo late
        Pix.blanco();
        Pix.t(g, HudTex.EMBLEMA, X0, Y0);
        if (poca) {
            float late = 0.5f + 0.5f * Mth.sin(t * 0.38f);
            Pix.suma();
            Pix.color(1f, 0.12f, 0.1f, 0.35f + 0.5f * late);
            g.blit(BRILLO, X0 - 6, Y0 - 6, 36, 36, 0, 0, 64, 64, 64, 64);
            Pix.blanco();
            Pix.mezcla();
        }
        return x + 3 + Math.max(Math.max(largoVida, largoCorcel), Math.max(largoMana, largoEst)) + 8;
    }

    /** Lo que queda del rastro del dano en la barra del corcel (baja poco a poco, como el de la vida). */
    private static float corcelRastro;

    /** El corcel en el que se va montado (o null). */
    static com.darkcraft.montura.Corcel corcel(LocalPlayer p) {
        return p.getVehicle() instanceof com.darkcraft.montura.Corcel c && !c.isRemoved() ? c : null;
    }

    /** Una barra: marco de oro con punta, hueco oscuro, rastro del dano y relleno. */
    private static void barra(GuiGraphics g, int x, int y, int largo, float f, float fRastro, HudTex.T relleno, float cr, float cg, float cb) {
        int wf = Math.round(largo * Mth.clamp(f, 0f, 1f)), wr = Math.round(largo * Mth.clamp(fRastro, 0f, 1f));
        if (f > 0.001f && wf < 1) wf = 1;
        Pix.blanco();
        Pix.tira(g, HudTex.BARRA_FONDO, x + 2, y + 2, largo + 1);
        punta(g, HudTex.BARRA_FONDO, x + 3 + largo, y);
        if (wr > wf) Pix.tira(g, HudTex.RASTRO, x + 2 + wf, y + 2, wr - wf + 1);
        if (wf > 0) {
            Pix.color(cr, cg, cb, 1f);
            Pix.tira(g, relleno, x + 2, y + 2, wf + 1);
            if (wf >= largo) punta(g, relleno, x + 3 + largo, y);
            Pix.blanco();
        }
        Pix.t(g, HudTex.BARRA_IZQ, x, y);
        Pix.tira(g, HudTex.BARRA_MED, x + 3, y, largo);
        Pix.t(g, HudTex.BARRA_PUNTA, x + 3 + largo, y);
    }

    /** El hueco de dentro de la punta de flecha (5 filas que se estrechan), con el dibujo que toque. */
    private static void punta(GuiGraphics g, HudTex.T t, int x, int y) {
        Pix.mezcla();
        int[] anchos = {3, 4, 5, 4, 3};
        for (int i = 0; i < 5; i++) g.blit(HudTex.TEX, x, y + 2 + i, t.u(), t.v() + i, anchos[i], 1, HudTex.TW, HudTex.TH);
    }

    /** El brillo que recorre el relleno de una barra cuando se acaba de llenar o de curar. */
    private static void destello(GuiGraphics g, int x, int y, int largo, float f, float hace, float cr, float cg, float cb) {
        if (hace < 0 || hace > 14) return;
        int wf = Math.round(largo * Mth.clamp(f, 0f, 1f));
        if (wf < 4) return;
        int bx = x + 2 + Math.round((wf + 14) * hace / 14f) - 14;
        g.enableScissor(x + 2, y + 2, x + 3 + wf, y + 7);
        Pix.suma();
        Pix.color(cr, cg, cb, 1f);
        g.blit(HudTex.TEX, bx, y + 2, HudTex.BRILLO.u(), HudTex.BRILLO.v(), HudTex.BRILLO.w(), HudTex.BRILLO.h(), HudTex.TW, HudTex.TH);
        Pix.blanco();
        Pix.mezcla();
        g.disableScissor();
    }

    // ---- los Ecos de la Sombra

    private static NumberFormat formato;
    private static String formatoIdioma = "";

    /** El numero con los puntos (o comas) de los miles del idioma del juego. */
    public static String numero(int n) {
        String idioma = Minecraft.getInstance().getLanguageManager().getSelected();
        if (formato == null || !idioma.equals(formatoIdioma)) {
            String[] q = idioma.split("_");
            Locale l = q.length >= 2 ? new Locale(q[0], q[1].toUpperCase(Locale.ROOT)) : new Locale(q[0]);
            formato = NumberFormat.getIntegerInstance(l);
            formato.setGroupingUsed(true);
            formatoIdioma = idioma;
        }
        // los espacios finos de algunos idiomas no estan en la letra gotica: un espacio normal
        return formato.format(n).replace('\u00a0', ' ').replace('\u202f', ' ').replace('\u2009', ' ');
    }

    private static void ecos(Minecraft mc, GuiGraphics g, float t, int w, int h) {
        Font f = mc.font;
        Component num = Component.literal(numero(Math.round(ClientRol.ecosVistos)));
        int tw = Letra.anchoM(f, num);
        int n = Math.max(2, (tw + 6 + 17) / 18);
        int wpl = 24 + 18 * n + 16, px = w - 4 - wpl, py = h - 4 - 22;
        Pix.blanco();
        Pix.t(g, HudTex.PLACA_IZQ, px, py);
        Pix.tira(g, HudTex.PLACA_MED, px + 24, py, 18 * n);
        Pix.t(g, HudTex.PLACA_DER, px + 24 + 18 * n, py);
        // el eco: late despacio, y da un destello cuando llegan ecos
        float hace = t - ClientRol.destelloTick;
        boolean llega = hace >= 0 && hace < 8;
        HudTex.T ico = llega ? HudTex.ECO_1 : (ClientRol.ticks / 6) % 9 == 0 ? HudTex.ECO_2 : HudTex.ECO_0;
        Pix.t(g, ico, px + 3, py + 3);
        if (hace >= 0 && hace < 16) {
            Pix.suma();
            Pix.color(1f, 0.3f, 0.2f, 0.8f * (1f - hace / 16f));
            g.blit(BRILLO, px - 5, py - 5, 32, 32, 0, 0, 64, 64, 64, 64);
            Pix.blanco();
            Pix.mezcla();
        }
        boolean contando = hace >= 0 && hace < 30 && Math.round(ClientRol.ecosVistos) != ClientRol.ecos;
        Letra.mediana(g, f, num, px + 24 + 18 * n + 2 - tw, py + 5, contando ? 0xFFFFF2C4 : 0xFFE2BF62, 0xFF050408);
        // el "+120"
        float edad = t - ClientRol.ganadoTick;
        if (ClientRol.ganado > 0 && edad >= 0 && edad < 56) {
            float a = edad < 4 ? edad / 4f : edad > 42 ? (56 - edad) / 14f : 1f;
            Component mas = Component.literal("+" + numero(ClientRol.ganado));
            int mw = Letra.anchoM(f, mas);
            int sube = Math.round(Math.min(1f, edad / 10f) * 5f);
            Letra.mediana(g, f, mas, px + 24 + 18 * n + 2 - mw, py - 15 - sube, (Math.round(255 * a) << 24) | 0xFFF2C4, 0xFF050408);
        }
        Pix.mezcla();
    }

    // ---- el aviso de "Pulsa [E]...": la espada de luz o un santuario

    private static void avisoDeTecla(Minecraft mc, GuiGraphics g, int w, int h) {
        Component antes = Component.translatable("darkcraft.aviso.recuperar.antes");
        Component tecla = mc.options.keyInventory.getTranslatedKeyMessage();
        if (avisoQue == ClientRol.ESPADA)
            aviso(mc, g, w, h - 80, avisoAlfa, antes, tecla, Component.translatable("darkcraft.aviso.recuperar.despues"),
                    Component.literal(numero(ClientRol.ecosEspada)));
        else if (avisoQue == ClientRol.PERSONAJE)
            aviso(mc, g, w, h - 80, avisoAlfa, antes, tecla, Component.translatable("darkcraft.aviso.hablar"), null);
        else if (avisoQue == ClientRol.COSA)
            aviso(mc, g, w, h - 80, avisoAlfa, antes, tecla, Component.translatable(avisoCosa), null);
        else if (avisoQue == ClientRol.ESTELA)
            aviso(mc, g, w, h - 80, avisoAlfa, antes, tecla, Component.translatable("darkcraft.aviso.estela"), null);
        else if (avisoQue == ClientRol.CAMPANA)
            aviso(mc, g, w, h - 80, avisoAlfa, antes, tecla, Component.translatable("darkcraft.aviso.campana"), null);
        else if (avisoQue == ClientRol.PUERTA)
            aviso(mc, g, w, h - 80, avisoAlfa, antes, tecla, Component.translatable("darkcraft.aviso.puerta_aguja"), null);
        else
            aviso(mc, g, w, h - 80, avisoAlfa, antes, tecla,
                    Component.translatable(avisoEncendido ? "darkcraft.aviso.descansar" : "darkcraft.aviso.encender"), null);
    }

    /**
     * Un aviso de "Pulsa [TECLA] para...": banda oscura con filos de oro, la tecla dibujada como un boton de piedra y,
     * si hace falta, un numero en dorado al final. Centrado en la pantalla, con el borde de arriba en y.
     */
    public static void aviso(Minecraft mc, GuiGraphics g, int w, int y, float alfa, Component antes, Component tecla, Component despues,
                             Component numero) {
        Font f = mc.font;
        String kt = tecla.getString().toUpperCase(Locale.ROOT);
        int wa = antes.getString().isEmpty() ? 0 : f.width(antes) + 5;
        int wk = Math.max(13, f.width(kt) + 8);
        int wd = despues.getString().isEmpty() ? 0 : f.width(despues) + 5;
        int wn = numero == null ? 0 : f.width(numero) + 20;
        int dentro = wa + wk + wd + wn;
        int ancho = dentro + 28, x = (w - ancho) / 2;
        int a = Math.round(255 * alfa);
        if (a < 8) return;
        Pix.color(1f, 1f, 1f, alfa);
        Pix.tres(g, HudTex.AVISO_IZQ, HudTex.AVISO_MED, HudTex.AVISO_DER, x, y, ancho);
        int cx = x + 14;
        int claro = (a << 24) | 0xEADBC4;
        Pix.blanco();
        if (wa > 0) g.drawString(f, antes, cx, y + 6, claro, true);
        cx += wa;
        Pix.color(1f, 1f, 1f, alfa);
        Pix.tres(g, HudTex.TECLA_IZQ, HudTex.TECLA_MED, HudTex.TECLA_DER, cx, y + 3, wk);
        Pix.blanco();
        g.drawString(f, kt, cx + (wk - f.width(kt)) / 2 + 1, y + 5, (a << 24) | 0xF6E3A0, false);
        cx += wk + 5;
        if (wd > 0) g.drawString(f, despues, cx, y + 6, claro, true);
        cx += wd;
        if (numero != null) {
            Pix.color(1f, 1f, 1f, alfa);
            Pix.t(g, HudTex.ECO_0, cx, y + 1);
            Pix.blanco();
            g.drawString(f, numero, cx + 17, y + 6, (a << 24) | 0xE2BF62, true);
        }
        Pix.blanco();
        Pix.mezcla();
    }

    // ---- la brujula

    private static final int[] RUMBOS = {180, -90, 0, 90};
    private static final String[] LETRAS = {"n", "e", "s", "o"};

    private static final net.minecraft.world.item.ItemStack[] PIEZAS = new net.minecraft.world.item.ItemStack[5];

    /**
     * En Brumavela, en el borde derecho de la pantalla: las cinco piezas de La Garza Gris, una debajo de otra; las que
     * faltan, apagadas. La que se acaba de encontrar destella un momento. (Ahi no hay nada mas: arriba en el centro
     * salen los carteles y los logros.)
     */
    private static void piezas(GuiGraphics g, float t, int w, int h) {
        if (PIEZAS[0] == null) {
            PIEZAS[0] = new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEZA_VELA.get());
            PIEZAS[1] = new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEZA_TIMON.get());
            PIEZAS[2] = new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEZA_MASTIL.get());
            PIEZAS[3] = new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEZA_ANCLA.get());
            PIEZAS[4] = new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEZA_CORAZON.get());
        }
        int paso = 20, x = w - 5 - 17, y0 = h / 2 - (5 * paso - 2) / 2;
        float hace = t - ClientPueblo.cambio;
        for (int i = 0; i < 5; i++) {
            int y = y0 + i * paso;
            boolean hay = (ClientPueblo.piezas & (1 << i)) != 0;
            // la casilla: fondo oscuro con sus filos de oro (apagados si falta la pieza)
            int filo = hay ? 0xD0C0963C : 0x904A3312;
            g.fill(x - 1, y - 1, x + 17, y + 17, 0xA6050408);
            g.fill(x - 1, y - 1, x + 17, y, filo);
            g.fill(x - 1, y + 16, x + 17, y + 17, filo);
            g.fill(x - 1, y, x, y + 16, filo);
            g.fill(x + 16, y, x + 17, y + 16, filo);
            g.renderItem(PIEZAS[i], x, y);
            if (!hay) {
                // la que falta: su sombra
                g.pose().pushPose();
                g.pose().translate(0, 0, 220);
                g.fill(x, y, x + 16, y + 16, 0xC8050408);
                g.pose().popPose();
            } else if (hace < 30 && (ClientPueblo.nuevas & (1 << i)) != 0) {
                g.pose().pushPose();
                g.pose().translate(0, 0, 220);
                int a = Math.round(150 * (1f - hace / 30f));
                g.fill(x - 1, y - 1, x + 17, y + 17, (a << 24) | 0xFFF2C4);
                g.pose().popPose();
            }
        }
        Pix.blanco();
        Pix.mezcla();
    }

    private static void brujula(Minecraft mc, GuiGraphics g, LocalPlayer p, float pt, int w, int finBarras) {
        int libre = w / 2 - Math.max(finBarras, EspejoHud.anchoEnPantalla() + 8);
        int bw = Math.min(180, libre * 2);
        if (bw < 84) return;
        Font f = mc.font;
        int x = (w - bw) / 2, y = 3, cx = w / 2;
        Pix.blanco();
        Pix.tres(g, HudTex.BRUJULA_IZQ, HudTex.BRUJULA_MED, HudTex.BRUJULA_DER, x, y, bw);
        float yaw = Mth.wrapDegrees(Mth.rotLerp(pt, p.yRotO, p.getYRot()));
        float medio = bw / 2f;
        // rayitas cada 15 grados (mas largas cada 45)
        for (int a = 0; a < 360; a += 15) {
            if (a % 90 == 0) continue;
            float d = Mth.wrapDegrees(a - yaw);
            if (Math.abs(d) > medio - 6) continue;
            int al = Math.round(200 * Mth.clamp((medio - Math.abs(d)) / 22f, 0f, 1f));
            if (al < 8) continue;
            int rx = cx + Math.round(d);
            g.fill(rx, y + (a % 45 == 0 ? 4 : 6), rx + 1, y + 9, (al << 24) | (a % 45 == 0 ? 0xC0963C : 0x96702A));
        }
        // los cuatro rumbos (el norte, en rojo)
        for (int i = 0; i < 4; i++) {
            float d = Mth.wrapDegrees(RUMBOS[i] - yaw);
            if (Math.abs(d) > medio - 8) continue;
            int al = Math.round(255 * Mth.clamp((medio - Math.abs(d)) / 22f, 0f, 1f));
            if (al < 8) continue;
            String s = Component.translatable("darkcraft.brujula." + LETRAS[i]).getString();
            g.drawString(f, s, cx + Math.round(d) - f.width(s) / 2 + (f.width(s) % 2), y + 3, (al << 24) | (i == 0 ? 0xE8302A : 0xE2BF62), true);
        }
        Pix.mezcla();
        // marcas: el cofre del capitulo que toca y la espada de luz
        double px = Mth.lerp(pt, p.xo, p.getX()), pz = Mth.lerp(pt, p.zo, p.getZ());
        int[] obj = EspejoCliente.objetivo();
        if (obj != null) marca(g, HudTex.MARCA_OBJETIVO, cx, y + 3, medio, rumbo(obj[0] + 0.5 - px, obj[1] + 0.5 - pz) - yaw);
        if (ClientRol.hayEspada && mc.level.dimension().location().toString().equals(ClientRol.espadaDim))
            marca(g, HudTex.MARCA_ESPADA, cx, y + 1, medio, rumbo(ClientRol.ex - px, ClientRol.ez - pz) - yaw);
        if (mc.level.dimension() == com.darkcraft.morvain.Morvain.NIVEL) {
            santuarios(g, cx, y, medio, px, pz, yaw);
            // adonde ir en las misiones (parte 7ag): una campanita de oro
            for (com.darkcraft.net.Net.MisionE mi : com.darkcraft.client.mision.ClientMisiones.LISTA) {
                if (!mi.hay()) continue;
                double dx = mi.x() + 0.5 - px, dz = mi.z() + 0.5 - pz;
                if (dx * dx + dz * dz < 16) continue;
                marca(g, HudTex.MARCA_MISION, cx, y + 1, medio, rumbo(dx, dz) - yaw);
            }
        }
        Pix.blanco();
        Pix.t(g, HudTex.BRUJULA_AGUJA, cx - 4, y - 2);
    }

    /** Hasta donde se ven en la brujula los santuarios de Morvain que conoce y los que no (bloques). */
    private static final double SANTUARIO_CONOCIDO = 1600, SANTUARIO_NUEVO = 480;

    /**
     * Los santuarios de Morvain cerca (parte 7c): los que ya conoce, con su llama de almas, hasta 1600 bloques; los que no,
     * apagados y solo a menos de 480 (para que se busquen). El de al lado no se marca.
     */
    private static void santuarios(GuiGraphics g, int cx, int y, float medio, double px, double pz, float yaw) {
        for (com.darkcraft.morvain.SantuariosMorvain.Sitio s : com.darkcraft.morvain.SantuariosMorvain.todos()) {
            double dx = s.x() + 0.5 - px, dz = s.z() + 0.5 - pz, d2 = dx * dx + dz * dz;
            boolean conocido = com.darkcraft.client.morvain.ClientViaje.CONOCIDOS.contains(s.id());
            double lejos = conocido ? SANTUARIO_CONOCIDO : SANTUARIO_NUEVO;
            if (d2 < 64 || d2 > lejos * lejos) continue;
            marca(g, conocido ? HudTex.MARCA_SANTUARIO : HudTex.MARCA_SANTUARIO_APAGADO, cx, y + 2, medio, rumbo(dx, dz) - yaw);
        }
    }

    /** Hacia donde queda algo (en grados de los de Minecraft: 0 el sur, 90 el oeste). */
    private static float rumbo(double dx, double dz) {
        return (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
    }

    private static void marca(GuiGraphics g, HudTex.T t, int cx, int y, float medio, float grados) {
        float d = Mth.wrapDegrees(grados);
        // lo que queda detras se pega al borde de la banda (apagado), para saber hacia donde girar
        float tope = medio - 10;
        boolean fuera = Math.abs(d) > tope;
        if (fuera) d = Math.signum(d) * tope;
        Pix.color(1f, 1f, 1f, fuera ? 0.55f : 1f);
        Pix.t(g, t, cx + Math.round(d) - t.w() / 2, y);
        Pix.blanco();
    }
}
