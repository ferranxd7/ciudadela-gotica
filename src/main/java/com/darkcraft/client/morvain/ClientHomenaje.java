package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Isolde;
import com.darkcraft.morvain.Morvain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * La torre del homenaje en el cliente (parte 7ab): mientras la reina Isolde espera sentada en su trono, tararea su nana (se la oye
 * desde la escalera; se calla despacio en cuanto se levanta). El aire de la torre lo pone {@code ClientMusic} (con el del crater).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientHomenaje {
    private ClientHomenaje() {}

    /** Lo lejos que se la oye tararear (bloques). */
    public static final double OIDO = 44;

    private static Tarareo tarareo;
    private static int espera;
    /** Para la prueba: las veces que ha empezado a tararear. */
    public static int TARAREOS;

    /** El tarareo: sigue a la reina y se apaga si se levanta (o se va). */
    static final class Tarareo extends AbstractTickableSoundInstance {
        final Isolde reina;
        private float v;

        Tarareo(Isolde r) {
            super(DarkCraftMod.SFX_ISOLDE_TARAREO.get(), SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
            reina = r;
            looping = false;
            delay = 0;
            volume = 0.01f;
            attenuation = Attenuation.LINEAR;
            x = r.getX();
            y = r.getY() + 2.4;
            z = r.getZ();
        }

        @Override
        public void tick() {
            boolean sigue = reina.isAlive() && !reina.isRemoved() && reina.postura() == Enemigo.REZA;
            v = sigue ? Math.min(1f, v + 0.04f) : v - 0.06f;
            if (v <= 0f) {
                stop();
                return;
            }
            // (con LINEAR, el volumen es tambien lo lejos que se oye: 16 bloques por cada 1)
            volume = Math.max(0.01f, v * (float) (OIDO / 16.0));
            x = reina.getX();
            y = reina.getY() + 2.4;
            z = reina.getZ();
        }

        boolean acabado() {
            return isStopped();
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.isPaused()) return;
        if (mc.level.dimension() != Morvain.NIVEL) {
            tarareo = null;
            return;
        }
        boolean suena = tarareo != null && !tarareo.acabado() && mc.getSoundManager().isActive(tarareo);
        if (suena || --espera > 0) return;
        espera = 20;
        if (p.getY() < 590) return;
        for (Isolde r : mc.level.getEntitiesOfClass(Isolde.class, p.getBoundingBox().inflate(OIDO), x -> x.isAlive() && x.postura() == Enemigo.REZA)) {
            tarareo = new Tarareo(r);
            mc.getSoundManager().play(tarareo);
            TARAREOS++;
            break;
        }
    }

    /** Para la prueba: si suena el tarareo. */
    public static String estado() {
        Minecraft mc = Minecraft.getInstance();
        if (tarareo == null) return "sin tarareo";
        return String.format(java.util.Locale.ROOT, "tarareo %.2f%s (%d)", tarareo.getVolume(), mc.getSoundManager().isActive(tarareo) ? "" : " (parado)", TARAREOS);
    }

    @SubscribeEvent
    public static void onSalir(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) {
        tarareo = null;
        espera = 0;
    }
}
