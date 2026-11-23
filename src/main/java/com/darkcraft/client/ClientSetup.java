package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("energia", com.darkcraft.client.armas.ArmasCliente::render);
        e.registerAboveAll("rey", com.darkcraft.client.rey.ReyCliente::renderOverlay);
        e.registerAboveAll("cinematica", ClientCine::renderOverlay);
        e.registerAboveAll("espejo", com.darkcraft.client.espejo.EspejoHud::render);
        e.registerAboveAll("rey_subtitulos", com.darkcraft.client.rey.ReyCliente::renderSubtitulos);
    }

    @SubscribeEvent
    public static void setup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            net.minecraft.resources.ResourceLocation forma = new net.minecraft.resources.ResourceLocation(DarkCraftMod.MODID, "forma");
            for (var it : java.util.List.of(DarkCraftMod.ESPADA_DIESTRA.get(), DarkCraftMod.ESPADA_SINIESTRA.get()))
                net.minecraft.client.renderer.item.ItemProperties.register(it, forma,
                        (stack, level, ent, seed) -> com.darkcraft.armas.EspadaRey.forma(stack) / 2f);
        });
    }

    @SubscribeEvent
    public static void dimensiones(net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent e) {
        e.register(com.darkcraft.client.rey.VolcanCliente.EFECTOS, new com.darkcraft.client.rey.VolcanCliente.Efectos());
    }

    @SubscribeEvent
    public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.REY.get(), com.darkcraft.client.rey.ReyRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.REY_PROYECTIL.get(), com.darkcraft.client.rey.ProyectilRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ASCENSOR.get(), com.darkcraft.client.arbol.AscensorRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PUERTA_ARBOL.get(), com.darkcraft.client.arbol.PuertaArbolRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GAVRAN.get(), com.darkcraft.client.gavran.GavranRenderer::new);
    }

    @SubscribeEvent
    public static void layers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(com.darkcraft.client.rey.ReyModel.LAYER, com.darkcraft.client.rey.ReyGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.gavran.GavranModel.LAYER, com.darkcraft.client.gavran.GavranGeometria::crear);
    }
}
