package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.morvain.Subida;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.List;

/**
 * La subida por la cadena del sur en el cliente (parte 7aa):
 * <ul>
 * <li><b>Las rafagas</b> ({@link Net.Rafaga}): mientras se oye venir, jirones de nube que cruzan de lado; luego empuja al jugador
 * hacia ese lado (crece y se apaga; agachado, una quinta parte: con el borde del juego, no se cae).</li>
 * <li><b>La Sombra</b> ({@link Net.SombraDice}): su frase sin sitio (en estereo, con sus susurros) y su subtitulo abajo, con su
 * letra lila; si ya esta hablando, la siguiente espera.</li>
 * <li>Lo que suena: los eslabones que crujen bajo los pies de vez en cuando y, cerca de la Boca, su aliento y su latido.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientCadena {
    private ClientCadena() {}

    private static final RandomSource R = RandomSource.create();

    // ---- la rafaga que sopla
    private static int lado, aviso, empuja, t = -1;
    private static float fuerza;
    /** Para la prueba: las rafagas que han llegado y lo que ha movido la ultima (bloques de lado). */
    public static int RAFAGAS;
    public static double MOVIDO;
    private static double xAntes;

    // ---- la Sombra
    private static final ArrayDeque<Integer> COLA = new ArrayDeque<>();
    private static SoundInstance voz;
    private static int vozN, vozT, vozDura;
    /** Para la prueba: la ultima frase de la Sombra que ha sonado. */
    public static int ULTIMA;

    // ---- lo que suena
    private static int crujido = 200, aliento = 60, latido = 30;

    public static void rafaga(Net.Rafaga m) {
        lado = m.lado();
        aviso = m.aviso();
        empuja = m.empuja();
        fuerza = m.fuerza();
        t = 0;
        RAFAGAS++;
        LocalPlayer p = Minecraft.getInstance().player;
        xAntes = p == null ? 0 : p.getX();
    }

    public static void sombra(int n) {
        if (n <= 0 || n >= DarkCraftMod.SOMBRA_VOZ.length || DarkCraftMod.SOMBRA_VOZ[n] == null) return;
        if (COLA.size() < 4) COLA.add(n);
    }

    /** Si sopla ahora (o se oye venir). */
    public static boolean sopla() {
        return t >= 0;
    }

    @SubscribeEvent
    public static void onSalir(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) {
        t = -1;
        COLA.clear();
        voz = null;
        vozN = 0;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.isPaused()) return;
        viento(mc, p);
        laSombra(mc);
        if (mc.level.dimension() == Morvain.NIVEL) sonidos(mc, p);
    }

    // ------------------------------------------------------------------ el viento

    private static void viento(Minecraft mc, LocalPlayer p) {
        if (t < 0) return;
        if (mc.level.dimension() != Morvain.NIVEL || p.isSpectator()) {
            t = -1;
            return;
        }
        int k = t++;
        double cara = p.getEyeY();
        // los jirones de nube que cruzan de lado: pocos mientras se oye venir y muchos mientras empuja
        int n = k < aviso ? (k > aviso / 2 ? 2 : 1) : k < aviso + empuja ? 5 : 1;
        for (int i = 0; i < n; i++) {
            double ax = p.getX() - lado * (5 + R.nextDouble() * 3), ay = cara - 1.5 + R.nextDouble() * 3.5, az = p.getZ() - 4 + R.nextDouble() * 8;
            mc.level.addParticle(i % 2 == 0 ? ParticleTypes.CLOUD : ParticleTypes.WHITE_ASH, ax, ay, az, lado * (0.45 + R.nextDouble() * 0.3), 0.0, 0.0);
        }
        if (k >= aviso && k < aviso + empuja) {
            // empuja: crece y se apaga (lo mas fuerte a la mitad)
            float f = Mth.sin((k - aviso + 0.5f) / empuja * Mth.PI);
            float cuanto = fuerza * f * (p.isCrouching() ? 0.2f : 1f);
            if (!p.getAbilities().flying && !p.isPassenger()) {
                p.setDeltaMovement(p.getDeltaMovement().add(lado * cuanto, 0, 0));
            }
        }
        if (k >= aviso + empuja + 20) {
            MOVIDO = p.getX() - xAntes;
            t = -1;
        }
    }

    // ------------------------------------------------------------------ la Sombra

    private static void laSombra(Minecraft mc) {
        if (vozN > 0) {
            vozT++;
            boolean suena = voz != null && mc.getSoundManager().isActive(voz);
            if (vozT > vozDura + 10 && !suena) vozN = 0;
        }
        if (vozN == 0 && !COLA.isEmpty()) {
            int n = COLA.poll();
            voz = new SimpleSoundInstance(DarkCraftMod.SOMBRA_VOZ[n].get().getLocation(), SoundSource.VOICE, 1f, 1f, SoundInstance.createUnseededRandom(),
                    false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
            mc.getSoundManager().play(voz);
            vozN = n;
            vozT = 0;
            vozDura = Math.round((Voces.sombra(n) + 0.9f) * 20);
            ULTIMA = n;
        }
    }

    /** Si la Sombra esta hablando (para la prueba). */
    public static int hablando() {
        return vozN;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGui(RenderGuiEvent.Post e) {
        if (vozN <= 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        float tt = vozT + e.getPartialTick();
        float a = Math.min(Mth.clamp(tt / 6f, 0f, 1f), Mth.clamp((vozDura - tt) / 8f, 0f, 1f));
        if (a * 255 < 4) return;
        GuiGraphics g = e.getGuiGraphics();
        Font font = mc.font;
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        List<FormattedCharSequence> partes = font.split(Component.translatable("darkcraft.sombra.voz." + vozN), (int) (w * 0.6f));
        int lh = 10, y0 = h - 74 - partes.size() * lh;
        for (int k = 0; k < partes.size(); k++) {
            FormattedCharSequence s = partes.get(k);
            int x = (w - font.width(s)) / 2;
            g.drawString(font, s, x + 1, y0 + k * lh + 1, argb(a * 0.85f, 24, 0, 30), false);
            g.drawString(font, s, x, y0 + k * lh, argb(a, 216, 200, 236), false);
        }
    }

    private static int argb(float a, int r, int g, int b) {
        return (Mth.clamp((int) (a * 255), 0, 255) << 24) | (r << 16) | (g << 8) | b;
    }

    // ------------------------------------------------------------------ lo que suena

    private static void sonidos(Minecraft mc, LocalPlayer p) {
        double x = p.getX(), y = p.getY(), z = p.getZ();
        // los eslabones que crujen bajo los pies (sobre la cadena)
        if (Subida.sobreCadena(x, y, z) && --crujido <= 0) {
            crujido = 240 + R.nextInt(300);
            double eje = Subida.eje(z);
            mc.level.playLocalSound(x + (R.nextDouble() - 0.5) * 8, eje, z + (R.nextDouble() - 0.5) * 16, DarkCraftMod.SFX_CADENA_CRUJIDO.get(),
                    SoundSource.AMBIENT, 1.4f, 0.85f + R.nextFloat() * 0.2f, false);
        }
        // la Boca: su aliento y su latido (se oyen desde lejos, mas fuerte al entrar)
        double dz = z - Subida.Z_BOCA, dy = y - Subida.linea(Subida.Z_BOCA);
        double d = Math.sqrt(x * x + dz * dz + dy * dy);
        if (d < 64 && z > Subida.Z_TORRE - 4) {
            double by = Subida.linea(Subida.Z_BOCA) + 3;
            if (--aliento <= 0) {
                aliento = 180 + R.nextInt(100);
                mc.level.playLocalSound(0.5, by, Subida.Z_BOCA - 3, DarkCraftMod.SFX_BOCA_ALIENTO.get(), SoundSource.AMBIENT, 1.8f, 0.9f + R.nextFloat() * 0.1f, false);
            }
            if (--latido <= 0) {
                latido = 100 + R.nextInt(40);
                mc.level.playLocalSound(0.5, by - 2, Subida.Z_BOCA - 8, DarkCraftMod.SFX_BOCA_LATIDO.get(), SoundSource.AMBIENT, d < 20 ? 1.6f : 1.1f, 1f, false);
            }
        }
    }
}
