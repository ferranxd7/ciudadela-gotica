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
    private static int storyChapter = 1;

    /** Al abrir un cofre de la cronica (empieza la cinematica). */
    public static void storyStart(int chapter) {
        Minecraft mc = Minecraft.getInstance();
        if (story != null && !story.dying() && mc.getSoundManager().isActive(story)) {
            storyCine = true;
            storyNarrated = Timeline.narrated(chapter);
            storyChapter = chapter;
            return;
        }
        story = new Track(DarkCraftMod.MUSICA_COFRE.get(), "cofre", false);
        storyTicks = 0;
        idleTicks = 0;
        storyNarrated = Timeline.narrated(chapter);
        storyChapter = chapter;
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

    /** Musica del combate: 0 la apaga; 1..3 pone (o cambia a) el tema de esa fase del Rey (11 y 12: Ser Gavran; 21: el Capitan). Suave en la entrada. */
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
            case 21 -> DarkCraftMod.MUSICA_CAPITAN.get();
            default -> DarkCraftMod.MUSICA_REY_3.get();
        };
        boss = new Track(ev, phase == 21 ? "capitan" : phase >= 11 ? "gavran" + (phase - 10) : "rey" + phase, true);
        bossTicks = 0;
        Minecraft.getInstance().getSoundManager().play(boss);
        boss.fadeTo(soft ? 0.3f : 0.85f, phase == 1 ? 3.0f : 2.2f);
        if (story != null) story.kill(1.5f);
    }

    public static boolean bossPlaying() {
        return boss != null && !boss.dying();
    }

    // ------------------------------------------------------------------ la Fortaleza de Hueso

    private static Track zona;
    private static boolean enFortaleza;
    private static int zonaTicks;

    /** El servidor avisa al entrar y salir de la zona de una Fortaleza de Hueso: su musica, en bucle y suave. */
    public static void fortaleza(boolean dentro) {
        enFortaleza = dentro;
    }

    private static void tickZona(Minecraft mc) {
        boolean quiere = enFortaleza && mc.player != null && (boss == null || boss.dying()) && (story == null || story.dying())
                && !ClientCine.active();
        if (quiere) {
            if (zona == null || zona.dying() || !mc.getSoundManager().isActive(zona) && zonaTicks > 40) {
                if (zona != null && !zona.dying()) mc.getSoundManager().stop(zona);
                zona = new Track(DarkCraftMod.MUSICA_FORTALEZA.get(), "fortaleza", true);
                zonaTicks = 0;
                mc.getSoundManager().play(zona);
                zona.fadeTo(0.55f, 6.0f);
            }
        } else if (zona != null && !zona.dying()) zona.kill(enFortaleza ? 1.5f : 4.0f);
        if (zona != null) {
            zonaTicks++;
            if (zona.dying() && !mc.getSoundManager().isActive(zona) && zonaTicks > 40) zona = null;
        }
    }

    // ------------------------------------------------------------------ el viaje de La Garza Gris (parte 3d)

    private static Track viaje;

    /** La musica del viaje del barco: entra poco a poco en esos segundos; con {@code false}, se va en esos segundos. */
    public static void viaje(boolean suena, float segundos) {
        Minecraft mc = Minecraft.getInstance();
        if (suena) {
            if (viaje != null && !viaje.dying() && mc.getSoundManager().isActive(viaje)) return;
            viaje = new Track(DarkCraftMod.MUSICA_VIAJE.get(), "viaje", false);
            mc.getSoundManager().play(viaje);
            viaje.fadeTo(VIAJE, segundos);
        } else if (viaje != null && !viaje.dying()) viaje.kill(segundos);
    }

    /** La musica del viaje, a ese volumen en esos segundos (baja mientras habla la Sombra; lo normal es {@link #VIAJE}). */
    public static void viajeNivel(float nivel, float segundos) {
        if (viaje != null && !viaje.dying()) viaje.fadeTo(nivel, segundos);
    }

    public static final float VIAJE = 0.8f;

    // ------------------------------------------------------------------ Brumavela: el viento sobre el mar de nubes (parte 3b)

    private static Track bruma;
    private static int brumaTicks;
    private static boolean enBruma;

    /** En Brumavela (por su bioma) suena el viento, en bucle y bajito, y calla la musica de Minecraft. */
    private static void tickBruma(Minecraft mc) {
        if (mc.player == null || mc.level == null) enBruma = false;
        else if (mc.player.tickCount % 20 == 0) enBruma = mc.level.getBiome(mc.player.blockPosition()).is(com.darkcraft.world.PuebloFeature.BIOMA);
        boolean quiere = enBruma && (boss == null || boss.dying()) && (story == null || story.dying()) && (viaje == null || viaje.dying());
        if (quiere) {
            if (bruma == null || bruma.dying() || !mc.getSoundManager().isActive(bruma) && brumaTicks > 40) {
                if (bruma != null && !bruma.dying()) mc.getSoundManager().stop(bruma);
                bruma = new Track(DarkCraftMod.AMBIENTE_BRUMAVELA.get(), "brumavela", true);
                brumaTicks = 0;
                mc.getSoundManager().play(bruma);
                bruma.fadeTo(0.5f, 4.0f);
            }
        } else if (bruma != null && !bruma.dying()) bruma.kill(3.0f);
        if (bruma != null) {
            brumaTicks++;
            if (bruma.dying() && !mc.getSoundManager().isActive(bruma) && brumaTicks > 40) bruma = null;
        }
    }

    // ------------------------------------------------------------------ la pantalla de titulo (parte 2f-2)

    private static Track titulo;
    private static int tituloTicks;

    /** Fuera de un mundo, con una pantalla de menu delante (no las de cargar o conectar): la musica del titulo. */
    private static boolean enMenus(Minecraft mc) {
        if (mc.level != null || mc.getOverlay() != null || mc.screen == null) return false;
        var s = mc.screen;
        return !(s instanceof net.minecraft.client.gui.screens.LevelLoadingScreen
                || s instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen
                || s instanceof net.minecraft.client.gui.screens.GenericDirtMessageScreen
                || s instanceof net.minecraft.client.gui.screens.ProgressScreen
                || s instanceof net.minecraft.client.gui.screens.ConnectScreen);
    }

    private static void tickTitulo(Minecraft mc) {
        boolean quiere = enMenus(mc);
        if (quiere) {
            if (titulo == null || titulo.dying() || !mc.getSoundManager().isActive(titulo) && tituloTicks > 40) {
                if (titulo != null && !titulo.dying()) mc.getSoundManager().stop(titulo);
                titulo = new Track(DarkCraftMod.MUSICA_TITULO.get(), "titulo", true);
                tituloTicks = 0;
                mc.getSoundManager().play(titulo);
                titulo.fadeTo(0.6f, 3.0f);
            }
        } else if (titulo != null && !titulo.dying()) titulo.kill(2.0f);
        if (titulo != null) {
            tituloTicks++;
            if (titulo.dying() && !mc.getSoundManager().isActive(titulo) && tituloTicks > 40) titulo = null;
        }
    }

    /** Para la prueba de la CI: si suena la musica del titulo. */
    public static String titulo() {
        Minecraft mc = Minecraft.getInstance();
        return titulo == null ? "sin musica de titulo" : String.format("musica de titulo %.2f%s", titulo.level(),
                mc.getSoundManager().isActive(titulo) ? "" : " (parada)");
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
                        for (float[] l : Timeline.lines(storyChapter)) if (n >= l[0] - 0.4f && n <= l[1] + 0.2f) speaking = true;
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
        tickZona(mc);
        if (zona != null && !zona.dying()) any = true;
        tickBruma(mc);
        if (bruma != null && !bruma.dying()) any = true;
        tickTitulo(mc);
        if (titulo != null && !titulo.dying()) any = true;
        if (viaje != null) {
            if (!mc.getSoundManager().isActive(viaje) && viaje.dying()) viaje = null;
            else any = true;
        }
        // que no se mezcle con la musica normal del juego
        if (any) mc.getMusicManager().stopPlaying();
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        Minecraft mc = Minecraft.getInstance();
        if (story != null) mc.getSoundManager().stop(story);
        if (boss != null) mc.getSoundManager().stop(boss);
        if (zona != null) mc.getSoundManager().stop(zona);
        if (bruma != null) mc.getSoundManager().stop(bruma);
        if (viaje != null) mc.getSoundManager().stop(viaje);
        story = boss = zona = bruma = viaje = null;
        enFortaleza = enBruma = false;
        bossPhase = 0;
    }

    /** Para la prueba de la CI. */
    public static String debug() {
        Minecraft mc = Minecraft.getInstance();
        String s = story == null ? "-" : String.format("cofre %.2f%s", story.level(), mc.getSoundManager().isActive(story) ? "" : " (parada)");
        String b = boss == null ? "-" : String.format("%s %.2f", boss.name, boss.level());
        String z = zona == null ? "" : String.format(" / fortaleza %.2f", zona.level());
        String v = viaje == null ? "" : String.format(" / viaje %.2f%s", viaje.level(), mc.getSoundManager().isActive(viaje) ? "" : " (parada)");
        return s + " / " + b + z + v;
    }
}
