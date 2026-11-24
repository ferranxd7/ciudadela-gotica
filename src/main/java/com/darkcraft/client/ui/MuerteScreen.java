package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * La pantalla de muerte del mod, en vez de la de Minecraft: la imagen se oscurece en rojo, sale el cartel de
 * "HAS MUERTO" con su golpe de coro y, al poco, dos botones de piedra y oro: reaparecer (tambien con ESPACIO, INTRO o la
 * tecla del inventario) y salir al menu (hay que pulsarlo dos veces). Lo que se llevaba se conserva; los ecos se quedan
 * en la espada de luz. En el modo extremo se deja la pantalla del juego.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public class MuerteScreen extends DeathScreen {
    private static final int BOTONES = 46, ANCHO = 150;
    private int t;
    private int seguro = -1000;

    public MuerteScreen() {
        super(null, false);
    }

    /** Cuando el juego va a abrir su pantalla de muerte, se abre esta. */
    @SubscribeEvent
    public static void onOpen(ScreenEvent.Opening e) {
        if (!(e.getNewScreen() instanceof DeathScreen) || e.getNewScreen() instanceof MuerteScreen) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.getLevelData().isHardcore()) return;
        if (e.getCurrentScreen() instanceof MuerteScreen) {
            // el juego la vuelve a pedir mientras se esta muerto: se queda la que ya hay
            e.setCanceled(true);
            return;
        }
        e.setNewScreen(new MuerteScreen());
        Carteles.olvidar();
        Carteles.sonar(Net.Cartel.MUERTE);
    }

    @Override
    protected void init() {
        // sin los botones del juego
    }

    @Override
    public void tick() {
        t++;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int bx() {
        return (width - ANCHO) / 2;
    }

    private int by(int i) {
        return Math.round(height * 0.40f) + 58 + i * 26;
    }

    private boolean sobre(int i, double mx, double my) {
        return mx >= bx() && mx < bx() + ANCHO && my >= by(i) && my < by(i) + 20;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        float e = t + pt;
        float k = Mth.clamp(e / 36f, 0f, 1f);
        // la imagen se hunde en un rojo casi negro
        g.fillGradient(0, 0, width, height, (Math.round(150 * k) << 24) | 0x120204, (Math.round(205 * k) << 24) | 0x060102);
        Carteles.dibujar(g, font, width, height, Net.Cartel.MUERTE, Component.translatable("darkcraft.cartel.muerte"), null, e, 1_000_000f);
        float a = Mth.clamp((e - BOTONES) / 14f, 0f, 1f);
        if (a > 0.02f) {
            boton(g, 0, Component.translatable("darkcraft.muerte.reaparecer"), mx, my, a);
            boolean dudando = t - seguro < 70;
            boton(g, 1, Component.translatable(dudando ? "darkcraft.muerte.salir.seguro" : "darkcraft.muerte.salir"), mx, my, a);
            Component pista = ClientRol.hayEspada ? Component.translatable("darkcraft.muerte.pista.ecos", Hud.numero(ClientRol.ecosEspada))
                    : Component.translatable("darkcraft.muerte.pista");
            g.drawString(font, pista, (width - font.width(pista)) / 2, by(1) + 30, (Math.round(150 * a) << 24) | 0xB8A888, false);
        }
        Pix.blanco();
        Pix.mezcla();
    }

    private void boton(GuiGraphics g, int i, Component texto, int mx, int my, float a) {
        boolean luz = sobre(i, mx, my);
        int x = bx(), y = by(i);
        Pix.color(1f, 1f, 1f, a);
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, x, y, ANCHO);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, x, y, ANCHO);
        Pix.blanco();
        int al = Math.round(255 * a);
        if (al >= 8) g.drawString(font, texto, x + (ANCHO - font.width(texto)) / 2, y + 6, (al << 24) | (luz ? 0xFFF2C4 : 0xE2BF62), true);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int boton) {
        if (boton != 0 || t < BOTONES) return false;
        if (sobre(0, mx, my)) {
            reaparecer();
            return true;
        }
        if (sobre(1, mx, my)) {
            clic();
            if (t - seguro < 70) salir();
            else seguro = t;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (t < BOTONES) return false;
        if (tecla == GLFW.GLFW_KEY_SPACE || tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER
                || minecraft.options.keyInventory.matches(tecla, scan)) {
            reaparecer();
            return true;
        }
        return false;
    }

    private void clic() {
        minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    private void reaparecer() {
        clic();
        if (minecraft.player != null) minecraft.player.respawn();
        minecraft.setScreen(null);
    }

    private void salir() {
        if (minecraft.level != null) minecraft.level.disconnect();
        minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        minecraft.setScreen(new TitleScreen());
    }
}
