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

    /** Musica del combate: 0 la apaga; 1..3 pone (o cambia a) el tema de esa fase del Rey (11 y 12: Ser Gavran; 21: el Capitan; 31 y 32: los guardianes de Valdrenn). Suave en la entrada. */
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
            case 31 -> DarkCraftMod.MUSICA_BRANDT.get();
            case 32 -> DarkCraftMod.MUSICA_VARETH.get();
            case 33 -> DarkCraftMod.MUSICA_MOLINERA.get();
            case 34 -> DarkCraftMod.MUSICA_VERDUGO.get();
            case 35 -> DarkCraftMod.MUSICA_BARQUERO.get();
            case 36 -> DarkCraftMod.MUSICA_ABAD.get();
            case 37 -> DarkCraftMod.MUSICA_CONDESTABLE.get();
            case 38 -> DarkCraftMod.MUSICA_RECTORA.get();
            default -> DarkCraftMod.MUSICA_REY_3.get();
        };
        boss = new Track(ev, phase == 21 ? "capitan" : phase == 31 ? "brandt" : phase == 32 ? "vareth" : phase == 33 ? "molinera" : phase == 34 ? "verdugo" : phase == 35 ? "barquero" : phase == 36 ? "abad" : phase == 37 ? "condestable" : phase == 38 ? "rectora" : phase >= 11 ? "gavran" + (phase - 10) : "rey" + phase, true);
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

    // ------------------------------------------------------------------ el viento de la Arboleda de los Ahorcados (parte 7g)

    private static Track arboleda;
    private static int arboledaTicks;
    private static boolean enArboleda;

    /**
     * En la arboleda, rachas de viento entre los arboles muertos (con crujidos de sogas): una toma tras otra (cada vez una de
     * las dos, al azar), sin musica de jefe ni de cofre encima.
     */
    private static void tickArboleda(Minecraft mc) {
        if (mc.player == null || mc.level == null) enArboleda = false;
        else if (mc.player.tickCount % 20 == 3)
            enArboleda = mc.level.dimension() == com.darkcraft.morvain.Morvain.NIVEL && com.darkcraft.morvain.Arboleda.dentro(mc.player.getX(), mc.player.getZ())
                    && mc.player.getY() < 160;
        boolean quiere = enArboleda && (boss == null || boss.dying()) && (story == null || story.dying()) && (viaje == null || viaje.dying());
        if (quiere) {
            if (arboleda == null || arboleda.dying() || !mc.getSoundManager().isActive(arboleda) && arboledaTicks > 20) {
                if (arboleda != null && !arboleda.dying()) mc.getSoundManager().stop(arboleda);
                arboleda = new Track(DarkCraftMod.AMBIENTE_ARBOLEDA.get(), "arboleda", false);
                arboledaTicks = 0;
                mc.getSoundManager().play(arboleda);
                arboleda.fadeTo(0.55f, 1.0f);
            }
        } else if (arboleda != null && !arboleda.dying()) arboleda.kill(2.5f);
        if (arboleda != null) {
            arboledaTicks++;
            if (arboleda.dying() && !mc.getSoundManager().isActive(arboleda) && arboledaTicks > 20) arboleda = null;
        }
    }

    // ------------------------------------------------------------------ el pantano (parte 7i): borbotea, croa y zumba

    private static Track pantano;
    private static int pantanoTicks;
    private static boolean enPantano;

    private static void tickPantano(Minecraft mc) {
        if (mc.player == null || mc.level == null) enPantano = false;
        else if (mc.player.tickCount % 20 == 11)
            enPantano = mc.level.dimension() == com.darkcraft.morvain.Morvain.NIVEL && mc.player.getY() < 120
                    && com.darkcraft.morvain.Terreno.region(mc.player.getX(), mc.player.getZ()) == com.darkcraft.morvain.Region.PANTANO;
        boolean quiere = enPantano && (boss == null || boss.dying()) && (story == null || story.dying()) && (viaje == null || viaje.dying());
        if (quiere) {
            if (pantano == null || pantano.dying() || !mc.getSoundManager().isActive(pantano) && pantanoTicks > 20) {
                if (pantano != null && !pantano.dying()) mc.getSoundManager().stop(pantano);
                pantano = new Track(DarkCraftMod.AMBIENTE_PANTANO.get(), "pantano", false);
                pantanoTicks = 0;
                mc.getSoundManager().play(pantano);
                pantano.fadeTo(0.8f, 2.0f);
            }
        } else if (pantano != null && !pantano.dying()) pantano.kill(3.0f);
        if (pantano != null) {
            pantanoTicks++;
            if (pantano.dying() && !mc.getSoundManager().isActive(pantano) && pantanoTicks > 20) pantano = null;
        }
    }

    // ------------------------------------------------------------------ las montanas (parte 7k): aulla la ventisca

    private static Track montana;
    private static int montanaTicks;
    private static boolean enMontana;

    private static void tickMontana(Minecraft mc) {
        if (mc.player == null || mc.level == null) enMontana = false;
        else if (mc.player.tickCount % 20 == 13)
            enMontana = mc.level.dimension() == com.darkcraft.morvain.Morvain.NIVEL && mc.player.getY() > 150
                    && com.darkcraft.morvain.Terreno.region(mc.player.getX(), mc.player.getZ()) == com.darkcraft.morvain.Region.MONTANAS;
        boolean quiere = enMontana && (boss == null || boss.dying()) && (story == null || story.dying()) && (viaje == null || viaje.dying());
        if (quiere) {
            if (montana == null || montana.dying() || !mc.getSoundManager().isActive(montana) && montanaTicks > 20) {
                if (montana != null && !montana.dying()) mc.getSoundManager().stop(montana);
                montana = new Track(DarkCraftMod.AMBIENTE_MONTANA.get(), "montana", false);
                montanaTicks = 0;
                mc.getSoundManager().play(montana);
                montana.fadeTo(0.85f, 2.5f);
            }
        } else if (montana != null && !montana.dying()) montana.kill(3.0f);
        if (montana != null) {
            montanaTicks++;
            if (montana.dying() && !mc.getSoundManager().isActive(montana) && montanaTicks > 20) montana = null;
        }
    }

    // ------------------------------------------------------------------ la capital (parte 7m): crujen las ruinas quemadas

    private static Track capital;
    private static int capitalTicks;
    private static boolean enCapital;

    private static void tickCapital(Minecraft mc) {
        if (mc.player == null || mc.level == null) enCapital = false;
        else if (mc.player.tickCount % 20 == 17)
            enCapital = mc.level.dimension() == com.darkcraft.morvain.Morvain.NIVEL && mc.player.getY() < 170
                    && com.darkcraft.morvain.Terreno.region(mc.player.getX(), mc.player.getZ()) == com.darkcraft.morvain.Region.CAPITAL;
        boolean quiere = enCapital && (boss == null || boss.dying()) && (story == null || story.dying()) && (viaje == null || viaje.dying());
        if (quiere) {
            if (capital == null || capital.dying() || !mc.getSoundManager().isActive(capital) && capitalTicks > 20) {
                if (capital != null && !capital.dying()) mc.getSoundManager().stop(capital);
                capital = new Track(DarkCraftMod.AMBIENTE_CAPITAL.get(), "capital", false);
                capitalTicks = 0;
                mc.getSoundManager().play(capital);
                capital.fadeTo(0.85f, 2.5f);
            }
        } else if (capital != null && !capital.dying()) capital.kill(3.0f);
        if (capital != null) {
            capitalTicks++;
            if (capital.dying() && !mc.getSoundManager().isActive(capital) && capitalTicks > 20) capital = null;
        }
    }

    // ------------------------------------------------------------------ la academia (parte 7o): viento en la meseta y susurros

    private static Track academia;
    private static int academiaTicks;
    private static boolean enAcademia;

    private static void tickAcademia(Minecraft mc) {
        if (mc.player == null || mc.level == null) enAcademia = false;
        else if (mc.player.tickCount % 20 == 13)
            enAcademia = mc.level.dimension() == com.darkcraft.morvain.Morvain.NIVEL && mc.player.getY() > 200 && mc.player.getY() < 330
                    && com.darkcraft.morvain.Terreno.region(mc.player.getX(), mc.player.getZ()) == com.darkcraft.morvain.Region.ACADEMIA;
        boolean quiere = enAcademia && (boss == null || boss.dying()) && (story == null || story.dying()) && (viaje == null || viaje.dying());
        if (quiere) {
            if (academia == null || academia.dying() || !mc.getSoundManager().isActive(academia) && academiaTicks > 20) {
                if (academia != null && !academia.dying()) mc.getSoundManager().stop(academia);
                academia = new Track(DarkCraftMod.AMBIENTE_ACADEMIA.get(), "academia", false);
                academiaTicks = 0;
                mc.getSoundManager().play(academia);
                academia.fadeTo(0.85f, 2.5f);
            }
        } else if (academia != null && !academia.dying()) academia.kill(3.0f);
        if (academia != null) {
            academiaTicks++;
            if (academia.dying() && !mc.getSoundManager().isActive(academia) && academiaTicks > 20) academia = null;
        }
    }

    /** Para la prueba de la CI: si suena el viento de la academia. */
    public static String academia() {
        Minecraft mc = Minecraft.getInstance();
        return academia == null ? "sin la academia" : String.format("la academia %.2f%s", academia.level(), mc.getSoundManager().isActive(academia) ? "" : " (parada)");
    }

    /** Para la prueba de la CI: si suenan las ruinas de la capital. */
    public static String capital() {
        Minecraft mc = Minecraft.getInstance();
        return capital == null ? "sin las ruinas" : String.format("las ruinas %.2f%s", capital.level(), mc.getSoundManager().isActive(capital) ? "" : " (paradas)");
    }

    /** Para la prueba de la CI: si suena la ventisca. */
    public static String montana() {
        Minecraft mc = Minecraft.getInstance();
        return montana == null ? "sin la ventisca" : String.format("la ventisca %.2f%s", montana.level(), mc.getSoundManager().isActive(montana) ? "" : " (parada)");
    }

    /** Para la prueba de la CI: si suena el pantano. */
    public static String pantano() {
        Minecraft mc = Minecraft.getInstance();
        return pantano == null ? "sin el pantano" : String.format("el pantano %.2f%s", pantano.level(), mc.getSoundManager().isActive(pantano) ? "" : " (parado)");
    }

    /** Para la prueba de la CI: si suena el viento de la arboleda. */
    public static String arboleda() {
        Minecraft mc = Minecraft.getInstance();
        return arboleda == null ? "sin viento de la arboleda" : String.format("viento de la arboleda %.2f%s", arboleda.level(),
                mc.getSoundManager().isActive(arboleda) ? "" : " (parado)");
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
        tickArboleda(mc);
        if (arboleda != null && !arboleda.dying()) any = true;
        tickPantano(mc);
        if (pantano != null && !pantano.dying()) any = true;
        tickMontana(mc);
        if (montana != null && !montana.dying()) any = true;
        tickCapital(mc);
        if (capital != null && !capital.dying()) any = true;
        tickAcademia(mc);
        if (academia != null && !academia.dying()) any = true;
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
        if (arboleda != null) mc.getSoundManager().stop(arboleda);
        if (pantano != null) mc.getSoundManager().stop(pantano);
        if (montana != null) mc.getSoundManager().stop(montana);
        if (capital != null) mc.getSoundManager().stop(capital);
        if (academia != null) mc.getSoundManager().stop(academia);
        story = boss = zona = bruma = viaje = arboleda = pantano = montana = capital = academia = null;
        enFortaleza = enBruma = enArboleda = enPantano = enMontana = enCapital = enAcademia = false;
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
