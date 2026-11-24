package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.client.espejo.EspejoCliente;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.ArrayDeque;

/**
 * Los carteles grandes del mod, con la letra gotica y florituras de oro: HAS MUERTO (lo dibuja la pantalla de muerte),
 * JEFE ABATIDO con el nombre del jefe, el nombre de una zona al entrar por primera vez y ECOS RECUPERADOS. Salen de uno
 * en uno (hacen cola), nunca encima de una cinematica ni del aviso del espejo, y cada uno con su sonido.
 */
public final class Carteles {
    private Carteles() {}

    private record Cartel(int tipo, Component titulo, Component sub) {}

    /** Sin carteles de zona (para la prueba de la CI: taparian las demas capturas). */
    public static boolean callados;

    private static final ArrayDeque<Cartel> COLA = new ArrayDeque<>();
    private static Cartel actual;
    private static int inicio;

    public static void recibir(Net.Cartel m) {
        switch (m.tipo()) {
            case Net.Cartel.JEFE -> COLA.add(new Cartel(Net.Cartel.JEFE, Component.translatable("darkcraft.cartel.jefe"), Component.translatable(m.clave())));
            case Net.Cartel.ZONA -> {
                // las zonas grandes (un reino, la fortaleza), con el cartel grande; los sitios de dentro, con uno pequeno
                if (callados) break;
                if (mayor(m.clave())) COLA.add(new Cartel(Net.Cartel.ZONA, Component.translatable(m.clave()), Component.translatable("darkcraft.cartel.zona")));
                else COLA.add(new Cartel(LUGAR, Component.translatable(m.clave()), null));
            }
            case Net.Cartel.ECOS -> COLA.add(new Cartel(Net.Cartel.ECOS, Component.translatable("darkcraft.cartel.ecos"),
                    Component.literal("+" + Hud.numero(m.n()))));
            case Net.Cartel.GUARDIA -> ClientRol.sinEstaminaTick = ClientRol.ticks;
            default -> {
            }
        }
        while (COLA.size() > 4) COLA.poll();
    }

    /** Un sitio de dentro de una zona (solo existe en el cliente: el servidor manda ZONA y aqui se decide). */
    public static final int LUGAR = 5;

    private static boolean mayor(String clave) {
        return clave.equals("darkcraft.mapa.titulo") || clave.equals("darkcraft.mapa.lugar.fortaleza") || clave.startsWith("darkcraft.zona.");
    }

    private static int dura(int tipo) {
        return tipo == Net.Cartel.JEFE ? 120 : tipo == Net.Cartel.ZONA ? 96 : tipo == LUGAR ? 64 : 76;
    }

    private static SoundEvent sonido(int tipo) {
        return tipo == Net.Cartel.JEFE ? DarkCraftMod.SFX_JEFE_ABATIDO.get()
                : tipo == Net.Cartel.ZONA || tipo == LUGAR ? DarkCraftMod.SFX_ZONA_DESCUBIERTA.get()
                : tipo == Net.Cartel.MUERTE ? DarkCraftMod.SFX_HAS_MUERTO.get() : DarkCraftMod.SFX_ECOS_RECUPERADOS.get();
    }

    static void sonar(int tipo) {
        // un sitio pequeno: la misma campana, mas aguda y bajita
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sonido(tipo), tipo == LUGAR ? 1.3f : 1.0f,
                tipo == LUGAR ? 0.3f : tipo == Net.Cartel.ZONA ? 0.7f : 0.9f));
    }

    /** Lo llama {@link ClientRol} en cada tick del cliente. */
    static void tick(Minecraft mc) {
        if (actual != null && ClientRol.ticks - inicio > dura(actual.tipo())) actual = null;
        if (actual == null && !COLA.isEmpty() && mc.screen == null && mc.player != null && mc.player.isAlive() && !ClientCine.active()
                && !EspejoCliente.avisando()) {
            actual = COLA.poll();
            inicio = ClientRol.ticks;
            sonar(actual.tipo());
        }
    }

    static void olvidar() {
        COLA.clear();
        actual = null;
    }

    /** El cartel que se esta viendo (para la prueba de la CI). */
    public static String resumen() {
        return (actual == null ? "ninguno" : "tipo " + actual.tipo() + " '" + actual.titulo().getString() + "' / '"
                + (actual.sub() == null ? "" : actual.sub().getString()) + "'") + " en cola " + COLA.size();
    }

    public static void render(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (actual == null || mc.options.hideGui) return;
        dibujar(g, mc.font, w, h, actual.tipo(), actual.titulo(), actual.sub(), ClientRol.ticks - inicio + pt, dura(actual.tipo()));
    }

    /**
     * Dibuja un cartel: una franja oscura que se apaga hacia los lados con filos de oro, el titulo con la letra gotica
     * grande entre dos florituras y, segun el tipo, un texto mas pequeno encima o debajo.
     *
     * @param edad ticks desde que salio; {@code dura}: lo que dura en total (se apaga al final)
     */
    public static void dibujar(GuiGraphics g, Font f, int w, int h, int tipo, Component titulo, Component sub, float edad, float dura) {
        float entra = Mth.clamp(edad / 12f, 0f, 1f), sale = Mth.clamp((dura - edad) / 22f, 0f, 1f);
        float a = Math.min(entra * entra * (3 - 2 * entra), sale);
        if (a <= 0.03f) return;
        boolean muerte = tipo == Net.Cartel.MUERTE, zona = tipo == Net.Cartel.ZONA, ecos = tipo == Net.Cartel.ECOS, lugar = tipo == LUGAR;
        int tw1 = Letra.anchoG(f, titulo);
        // el titulo va al doble si cabe con holgura (en pantallas estrechas, a su tamano)
        int esc = !ecos && !lugar && tw1 * 2 + 200 <= w ? 2 : 1;
        int tw = tw1 * esc, alto = Letra.ALTO_G * esc;
        int cy = Math.round(h * (muerte ? 0.40f : zona || lugar ? 0.24f : 0.30f));
        int arriba = cy - alto / 2;
        int y0 = arriba - (zona ? 20 : lugar ? 9 : 12), y1 = arriba + alto + (sub != null && !zona ? 26 : lugar ? 12 : 14);
        // la franja y sus filos
        Pix.franja(g, lugar ? w / 6 : 0, lugar ? w - w / 6 : w, y0, y1, muerte ? 0x0A0204 : 0x050408, (muerte ? 0.84f : lugar ? 0.6f : 0.72f) * a);
        int oro = muerte ? 0x8A121B : 0x96702A;
        Pix.franja(g, w / 8, w - w / 8, y0, y0 + 1, oro, 0.9f * a);
        Pix.franja(g, w / 8, w - w / 8, y1 - 1, y1, oro, 0.9f * a);
        Pix.franja(g, w / 5, w - w / 5, y0 + 2, y0 + 3, muerte ? 0x3A070C : 0x4A3312, 0.8f * a);
        Pix.franja(g, w / 5, w - w / 5, y1 - 3, y1 - 2, muerte ? 0x3A070C : 0x4A3312, 0.8f * a);
        int alfa = Math.round(255 * a);
        int color = (alfa << 24) | (muerte ? 0xC01A22 : zona || lugar ? 0xF4F0E0 : 0xE9C766);
        int borde = 0xFF050408;
        // las florituras se abren desde el centro
        int abre = Math.round((1f - entra) * 26f);
        int fy = cy - 5;
        if (tw + 2 * (72 + 10) <= w) {
            Pix.color(1f, muerte ? 0.45f : 1f, muerte ? 0.4f : 1f, a);
            Pix.t(g, HudTex.FLORITURA, w / 2 - tw / 2 - 10 - 72 + abre, fy);
            Pix.tEspejo(g, HudTex.FLORITURA, w / 2 + tw / 2 + 10 - abre, fy);
            Pix.blanco();
        }
        // el titulo
        g.pose().pushPose();
        g.pose().translate(w / 2f, arriba, 0);
        g.pose().scale(esc, esc, 1f);
        Letra.grande(g, f, titulo, -tw1 / 2, 0, color, borde);
        g.pose().popPose();
        // lo pequeno
        if (sub != null) {
            if (zona) {
                // encima: "Zona descubierta", en la letra del juego, dorado
                int sw = f.width(sub);
                g.drawString(f, sub, w / 2 - sw / 2, y0 + 7, (alfa << 24) | 0xC0963C, true);
            } else {
                int sw = Letra.anchoM(f, sub);
                Letra.mediana(g, f, sub, w / 2 - sw / 2, arriba + alto + 7, (alfa << 24) | (ecos ? 0xFFF2C4 : 0xEADBC4), borde);
            }
        }
        Pix.blanco();
        Pix.mezcla();
    }
}
