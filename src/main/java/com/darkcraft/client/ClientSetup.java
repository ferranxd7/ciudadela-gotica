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
        // de abajo arriba: el HUD del mod y el espejo; encima, las bandas de las entradas de los jefes y las cinematicas
        // (los tapan); y por encima de todo, los carteles y los subtitulos de los jefes
        e.registerAboveAll("hud", com.darkcraft.client.ui.Hud::render);
        e.registerAboveAll("espejo", com.darkcraft.client.espejo.EspejoHud::render);
        e.registerAboveAll("rey", com.darkcraft.client.rey.ReyCliente::renderOverlay);
        e.registerAboveAll("cinematica", ClientCine::renderOverlay);
        e.registerAboveAll("carteles", com.darkcraft.client.ui.Carteles::render);
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
        // los hechizos que vuelan no tienen modelo: se ven por sus particulas
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PROYECTIL.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GUIA.get(), com.darkcraft.client.personajes.GuiaRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ASCENSOR.get(), com.darkcraft.client.arbol.AscensorRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PUERTA_ARBOL.get(), com.darkcraft.client.arbol.PuertaArbolRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GAVRAN.get(), com.darkcraft.client.gavran.GavranRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CABALLERO_ESPADA.get(), com.darkcraft.client.caballeros.CaballeroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CABALLERO_ALABARDA.get(), com.darkcraft.client.caballeros.CaballeroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CABALLERO_BALLESTA.get(), com.darkcraft.client.caballeros.CaballeroRenderer::new);
    }

    @SubscribeEvent
    public static void layers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(com.darkcraft.client.rey.ReyModel.LAYER, com.darkcraft.client.rey.ReyGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.gavran.GavranModel.LAYER, com.darkcraft.client.gavran.GavranGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.caballeros.CaballeroModelo.CAPA, com.darkcraft.client.caballeros.CaballeroModelo::crear);
    }
}
