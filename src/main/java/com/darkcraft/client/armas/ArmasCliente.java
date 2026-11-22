package com.darkcraft.client.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Combos;
import com.darkcraft.armas.EspadaRey;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Combos con Shift / Ctrl + clic izquierdo y la barra de energia encima de la barra de objetos. */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ArmasCliente {
    private ArmasCliente() {}

    private static int energia, vista, aviso;

    public static void energia(int v) {
        energia = v;
    }

    /** La energia que sabe el cliente (para la prueba de la CI). */
    public static int energiaCliente() {
        return energia;
    }

    @SubscribeEvent
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered e) {
        if (!e.isAttack()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !EspadaRey.es(mc.player.getMainHandItem())) return;
        boolean shift = mc.options.keyShift.isDown();
        boolean ctrl = mc.options.keySprint.isDown() || Screen.hasControlDown();
        if (!shift && !ctrl) return;
        int tipo = ctrl ? Combos.JUICIO : Combos.TAJO_REAL;
        int coste = tipo == Combos.JUICIO ? Combos.COSTE_2 : Combos.COSTE_1;
        if (energia < coste && !mc.player.isCreative()) {
            if (aviso <= 0) {
                mc.player.displayClientMessage(Component.translatable("darkcraft.espada.sin_energia", coste), true);
                aviso = 20;
            }
            return;
        }
        Net.CH.sendToServer(new Net.Combo(tipo));
        e.setCanceled(true);
        e.setSwingHand(false);
    }

    /** Barra de energia (solo con una espada del Rey en la mano). */
    private static final net.minecraft.resources.ResourceLocation TEX = new net.minecraft.resources.ResourceLocation(com.darkcraft.DarkCraftMod.MODID, "textures/gui/energia.png");
    private static final int CANAL_INI = 31, CANAL_FIN = 86;

    public static void render(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (aviso > 0 && mc.player != null && mc.player.tickCount % 2 == 0) aviso--;
        if (mc.player == null || mc.options.hideGui || mc.player.isSpectator()) return;
        boolean lleva = EspadaRey.es(mc.player.getMainHandItem()) || EspadaRey.es(mc.player.getOffhandItem());
        if (!lleva) return;
        vista += Mth.clamp(energia - vista, -2, 2);
        // una espada en pixeles a la derecha de la barra de objetos: su canal se llena de fuego con la energia
        boolean zurdo = mc.player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT;
        int x0 = zurdo ? w / 2 - 91 - 8 - 96 : w / 2 + 91 + 8, y0 = h - 19;
        if (x0 < 2 || x0 + 96 > w - 2) {
            x0 = w / 2 - 48;
            y0 = h - 32 - (mc.player.isCreative() ? 0 : 8) - 24;
        }
        g.blit(TEX, x0, y0, 0, 0, 96, 16, 128, 128);
        int largo = CANAL_FIN - CANAL_INI + 1;
        int lleno = Math.round(largo * Mth.clamp(vista / (float) Combos.MAX, 0f, 1f));
        int cuadro = (mc.player.tickCount / 3) & 3;
        if (lleno > 0) g.blit(TEX, x0 + CANAL_INI, y0, CANAL_INI, 16 + 16 * cuadro, lleno, 16, 128, 128);
        // runas: se encienden cuando hay energia para cada combo
        for (int c : new int[]{Combos.COSTE_1, Combos.COSTE_2}) {
            if (vista < c) continue;
            int rx = CANAL_INI + Math.round((CANAL_FIN - CANAL_INI) * c / (float) Combos.MAX);
            g.blit(TEX, x0 + rx - 1, y0, rx - 1, 80, 3, 16, 128, 128);
        }
        // con la energia del Juicio, la hoja brilla y late
        if (vista >= Combos.COSTE_2) {
            float a = 0.55f + 0.45f * Mth.sin((mc.player.tickCount + pt) * 0.25f);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            g.setColor(1f, 1f, 1f, a);
            g.blit(TEX, x0, y0, 0, 96, 96, 16, 128, 128);
            g.setColor(1f, 1f, 1f, 1f);
        }
    }
}
