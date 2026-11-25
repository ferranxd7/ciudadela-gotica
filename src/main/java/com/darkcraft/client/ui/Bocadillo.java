package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.dialogo.Dialogos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que dice un personaje mientras se usa su forja o su tienda: una caja de piedra con su retrato pequeno, su nombre y
 * la frase, que se escribe al ritmo de su voz (como en {@link DialogoScreen}), y la voz. Se apaga un poco cuando ya hace
 * rato que acabo de hablar.
 */
public final class Bocadillo {
    public static final int ALTO = 44;

    private final Dialogos.Personaje pj;
    private SoundInstance voz;
    private String texto = "";
    private List<String> lineas = List.of();
    private int anchoPartido = -1, letras, edad, numero = -1;
    private float escritas, ritmo, dura;
    /** Cuando se dijo cada frase por ultima vez (en ticks de esta caja), para no repetirlas seguidas. */
    private final java.util.Map<Integer, Integer> dichas = new java.util.HashMap<>();

    public Bocadillo(Dialogos.Personaje pj) {
        this.pj = pj;
    }

    /** Dice la frase {@code n} (para la voz anterior, si la habia). */
    public void decir(int n) {
        parar();
        numero = n;
        texto = Component.translatable(pj.linea(n)).getString();
        anchoPartido = -1;
        letras = texto.length();
        escritas = 0;
        edad = 0;
        float seg = Dialogos.habla(pj.id(), n);
        dura = seg > 0.3f ? seg * 20f : letras / 1.6f;
        ritmo = seg > 0.3f ? letras / Math.max(10f, seg * 20f * 0.92f) : 1.6f;
        voz = new SimpleSoundInstance(new ResourceLocation(DarkCraftMod.MODID, pj.sonido(n)), SoundSource.VOICE, 1f, 1f,
                RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        Minecraft.getInstance().getSoundManager().play(voz);
        dichas.put(n, reloj);
    }

    private int reloj;

    /** Dice la frase {@code n} si no la ha dicho en los ultimos {@code ticks} (y no esta hablando ya). */
    public void decirSiHaceRato(int n, int ticks) {
        Integer cuando = dichas.get(n);
        if (cuando != null && reloj - cuando < ticks) return;
        decir(n);
    }

    public void tick() {
        reloj++;
        edad++;
        if (escritas < letras) escritas = Math.min(letras, escritas + ritmo);
    }

    public void parar() {
        if (voz != null) Minecraft.getInstance().getSoundManager().stop(voz);
        voz = null;
    }

    public boolean hablando() {
        return voz != null && Minecraft.getInstance().getSoundManager().isActive(voz);
    }

    /** La ultima frase dicha (o -1). */
    public int numero() {
        return numero;
    }

    public void render(GuiGraphics g, Font font, int x, int y, int w, float pt) {
        if (numero < 0) return;
        // ya callado un rato: mas apagado
        float apaga = Mth.clamp((edad + pt - dura - 80f) / 30f, 0f, 1f);
        float alfa = 1f - 0.45f * apaga;
        Pix.color(1f, 1f, 1f, alfa);
        Panel.losa(g, x, y, w, ALTO);
        Panel.hueco(g, x + 6, y + 5, 34, 34);
        Pix.mezcla();
        Pix.color(1f, 1f, 1f, alfa);
        g.blit(new ResourceLocation(DarkCraftMod.MODID, "textures/gui/retratos/" + pj.id() + ".png"), x + 7, y + 6, 32, 32, 0, 0, 64, 64, 64, 64);
        Pix.blanco();
        int tx = x + 46, tw = w - 46 - 10;
        int a = Math.round(255 * alfa) << 24;
        g.drawString(font, Component.translatable(pj.nombre()), tx, y + 6, a | 0xE9C766, true);
        if (anchoPartido != tw) {
            List<String> r = new ArrayList<>();
            for (FormattedText ft : font.getSplitter().splitLines(texto, Math.max(60, tw), Style.EMPTY)) r.add(ft.getString());
            lineas = r;
            anchoPartido = tw;
        }
        int quedan = (int) escritas;
        for (int i = 0; i < Math.min(2, lineas.size()) && quedan > 0; i++) {
            String l = lineas.get(i);
            String vis = l.substring(0, Math.min(l.length(), quedan));
            quedan -= l.length();
            g.drawString(font, vis, tx, y + 18 + i * 10, a | 0xEADBC4, true);
        }
        Pix.blanco();
        Pix.mezcla();
    }
}
