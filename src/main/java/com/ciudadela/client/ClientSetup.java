package com.ciudadela.client;

import com.ciudadela.CiudadelaMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CiudadelaMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("energia", com.ciudadela.client.armas.ArmasCliente::render);
        e.registerAboveAll("rey", com.ciudadela.client.rey.ReyCliente::renderOverlay);
        e.registerAboveAll("cinematica", ClientCine::renderOverlay);
        e.registerAboveAll("rey_subtitulos", com.ciudadela.client.rey.ReyCliente::renderSubtitulos);
    }

    @SubscribeEvent
    public static void setup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            net.minecraft.resources.ResourceLocation forma = new net.minecraft.resources.ResourceLocation(CiudadelaMod.MODID, "forma");
            for (var it : java.util.List.of(CiudadelaMod.ESPADA_DIESTRA.get(), CiudadelaMod.ESPADA_SINIESTRA.get()))
                net.minecraft.client.renderer.item.ItemProperties.register(it, forma,
                        (stack, level, ent, seed) -> com.ciudadela.armas.EspadaRey.forma(stack) / 2f);
        });
    }

    @SubscribeEvent
    public static void dimensiones(net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent e) {
        e.register(com.ciudadela.client.rey.VolcanCliente.EFECTOS, new com.ciudadela.client.rey.VolcanCliente.Efectos());
    }

    @SubscribeEvent
    public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(com.ciudadela.CiudadelaMod.REY.get(), com.ciudadela.client.rey.ReyRenderer::new);
        e.registerEntityRenderer(com.ciudadela.CiudadelaMod.REY_PROYECTIL.get(), com.ciudadela.client.rey.ProyectilRenderer::new);
    }

    @SubscribeEvent
    public static void layers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(com.ciudadela.client.rey.ReyModel.LAYER, com.ciudadela.client.rey.ReyGeometria::crear);
    }
}
