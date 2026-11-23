package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Timeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Musica propia: el fondo de los cofres de la cronica (entra poco a poco y baja un poco cuando habla el
 * narrador) y los temas del jefe por fase (con fundido entre fases). Va con el volumen General, para que se
 * oiga aunque la barra de "Musica" de Minecraft este bajada.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientMusic {
    private ClientMusic() {}

    /** Una pista con volumen que cambia poco a poco. */
    static final class Track extends AbstractTickableSoundInstance {
        private float current, target, rate;
        private boolean dying;
        final String name;

        Track(SoundEvent ev, String name, boolean loop) {
            super(ev, SoundSource.MASTER, SoundInstance.createUnseededRandom());
            this.name = name;
            this.looping = loop;
            this.delay = 0;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.relative = true;
            this.volume = 0.0001f;
            this.x = this.y = this.z = 0;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            if (current < target) current = Math.min(target, current + rate);
            else if (current > target) current = Math.max(target, current - rate);
            volume = Math.max(0.0001f, current);
            if (dying && current <= 0.0005f) stop();
        }

        void fadeTo(float v, float seconds) {
            target = v;
            rate = Math.max(0.00005f, Math.abs(v - current) / Math.max(1f, seconds * 20f));
        }

        void kill(float seconds) {
            dying = true;
            fadeTo(0, seconds);
        }

        boolean dying() {
            return dying;
        }

        float level() {
            return current;
        }
    }

    // ------------------------------------------------------------------ cofres de la cronica

    private static Track story;
    private static int storyTicks, idleTicks;
    private static boolean storyNarrated, storyCine;

    /** Al abrir un cofre de la cronica (empieza la cinematica). */
    public static void storyStart(int chapter) {
        Minecraft mc = Minecraft.getInstance();
        if (story != null && !story.dying() && mc.getSoundManager().isActive(story)) {
            storyCine = true;
            storyNarrated = chapter == 1;
            return;
        }
        story = new Track(DarkCraftMod.MUSICA_COFRE.get(), "cofre", false);
        storyTicks = 0;
        idleTicks = 0;
        storyNarrated = chapter == 1;
        storyCine = true;
        mc.getSoundManager().play(story);
        story.fadeTo(storyNarrated ? 0.75f : 0.9f, 6.0f);
    }

    /** La cinematica ha terminado: la musica sigue mientras el cofre o el libro esten abiertos. */
    public static void storyCineEnd() {
        storyCine = false;
        idleTicks = 0;
        if (story != null && !story.dying()) story.fadeTo(0.85f, 2.5f);
    }

    // ------------------------------------------------------------------ jefe

    private static Track boss;
    private static int bossPhase, bossTicks;
    private static boolean bossSoft;

    /** Musica del combate: 0 la apaga; 1..3 pone (o cambia a) el tema de esa fase del Rey (11 y 12: Ser Gavran). Suave en la entrada. */
    public static void boss(int phase, boolean soft) {
        if (phase == bossPhase && (phase == 0 || (boss != null && !boss.dying()))) {
            if (boss != null && soft != bossSoft) {
                bossSoft = soft;
                boss.fadeTo(soft ? 0.3f : 0.85f, soft ? 1.0f : 2.5f);
            }
            return;
        }
        bossPhase = phase;
        bossSoft = soft;
        if (boss != null) boss.kill(phase == 0 ? 3.5f : 1.8f);
        boss = null;
        if (phase <= 0) return;
        SoundEvent ev = switch (phase) {
            case 1 -> DarkCraftMod.MUSICA_REY_1.get();
            case 2 -> DarkCraftMod.MUSICA_REY_2.get();
            case 11 -> DarkCraftMod.MUSICA_GAVRAN_1.get();
            case 12 -> DarkCraftMod.MUSICA_GAVRAN_2.get();
            default -> DarkCraftMod.MUSICA_REY_3.get();
        };
        boss = new Track(ev, phase >= 11 ? "gavran" + (phase - 10) : "rey" + phase, true);
        bossTicks = 0;
        Minecraft.getInstance().getSoundManager().play(boss);
        boss.fadeTo(soft ? 0.3f : 0.85f, phase == 1 ? 3.0f : 2.2f);
        if (story != null) story.kill(1.5f);
    }

    public static boolean bossPlaying() {
        return boss != null && !boss.dying();
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        boolean any = false;
        if (story != null) {
            if (!mc.getSoundManager().isActive(story) && storyTicks > 40) story = null;
            else {
                any = true;
                storyTicks++;
                if (!story.dying()) {
                    if (storyCine && storyNarrated) {
                        // mas baja mientras habla el narrador
                        float n = ClientCine.time() - Timeline.NARR_START;
                        boolean speaking = false;
                        for (float[] l : Timeline.lines()) if (n >= l[0] - 0.4f && n <= l[1] + 0.2f) speaking = true;
                        float want = n < -1.5f ? 0.8f : (speaking ? 0.5f : 0.75f);
                        if (Math.abs(want - story.target) > 0.01f && storyTicks > 140) story.fadeTo(want, 0.8f);
                    }
                    if (!storyCine) {
                        if (mc.screen == null) idleTicks++;
                        else idleTicks = 0;
                        if (idleTicks > 20 * 8) story.kill(5.0f);
                    }
                }
            }
        }
        if (boss != null) {
            bossTicks++;
            if (!mc.getSoundManager().isActive(boss)) {
                if (boss.dying()) boss = null;
                else if (bossTicks > 40) {
                    // se ha parado sola (al cambiar de mundo, al morir y reaparecer...): que la vuelva a poner
                    // la siguiente llamada de boss(), aunque sea la misma fase
                    boss = null;
                    bossPhase = 0;
                }
            } else any = true;
        }
        // que no se mezcle con la musica normal del juego
        if (any) mc.getMusicManager().stopPlaying();
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        Minecraft mc = Minecraft.getInstance();
        if (story != null) mc.getSoundManager().stop(story);
        if (boss != null) mc.getSoundManager().stop(boss);
        story = boss = null;
        bossPhase = 0;
    }

    /** Para la prueba de la CI. */
    public static String debug() {
        Minecraft mc = Minecraft.getInstance();
        String s = story == null ? "-" : String.format("cofre %.2f%s", story.level(), mc.getSoundManager().isActive(story) ? "" : " (parada)");
        String b = boss == null ? "-" : String.format("%s %.2f", boss.name, boss.level());
        return s + " / " + b;
    }
}
