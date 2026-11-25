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
        e.register(ClientNubes.EFECTOS, new ClientNubes.Efectos());
    }

    @SubscribeEvent
    public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.REY.get(), com.darkcraft.client.rey.ReyRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.REY_PROYECTIL.get(), com.darkcraft.client.rey.ProyectilRenderer::new);
        // los hechizos que vuelan no tienen modelo: se ven por sus particulas
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PROYECTIL.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GUIA.get(), com.darkcraft.client.personajes.GuiaRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.HERRERO.get(), com.darkcraft.client.personajes.HerreroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.MERCADER.get(), com.darkcraft.client.personajes.MercaderRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ASCENSOR.get(), com.darkcraft.client.arbol.AscensorRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PUERTA_ARBOL.get(), com.darkcraft.client.arbol.PuertaArbolRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GAVRAN.get(), com.darkcraft.client.gavran.GavranRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CABALLERO_ESPADA.get(), com.darkcraft.client.caballeros.CaballeroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CABALLERO_ALABARDA.get(), com.darkcraft.client.caballeros.CaballeroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CABALLERO_BALLESTA.get(), com.darkcraft.client.caballeros.CaballeroRenderer::new);
        // los enemigos de Brumavela (modelos y animaciones de tools/enemigos)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.HUECO.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.HuecoModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.HuecoModelo.CAPA)), "hueco", 3, 0.45f, 1f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.AHOGADO.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.AhogadoModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.AhogadoModelo.CAPA)), "ahogado", 2, 0.55f, 1.06f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CAPITAN.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.CapitanModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.CapitanModelo.CAPA)), "capitan", 1, 0.7f, 1.22f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ARPON.get(), com.darkcraft.client.enemigos.ArponRenderer::new);
        // la guardia de Valdrenn y el arquero esqueleto (parte 4a)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GUARDIA.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.GuardiaModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.GuardiaModelo.CAPA)), "guardia", 3, 0.5f, 1.08f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ARQUERO.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.ArqueroModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.ArqueroModelo.CAPA)), "arquero", 1, 0.45f, 1.0f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PERRO.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.PerroModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.PerroModelo.CAPA)), "perro", 1, 0.45f, 1.0f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ESPECTRO.get(), com.darkcraft.client.enemigos.EspectroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ELITE.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.EliteModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.EliteModelo.CAPA)), "elite", 1, 0.6f, 1.15f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.BRANDT.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.BrandtModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.BrandtModelo.CAPA)), "brandt", 1, 0.9f, 1.5f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CORTEZA.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.CortezaModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.CortezaModelo.CAPA)), "corteza", 1, 0.9f, 1.45f));
        // La Garza Gris volando en las cinematicas del viaje (solo existe en el cliente)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.BARCO_VOLADOR.get(), com.darkcraft.client.cine.BarcoVoladorRenderer::new);
    }

    @SubscribeEvent
    public static void layers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(com.darkcraft.client.rey.ReyModel.LAYER, com.darkcraft.client.rey.ReyGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.gavran.GavranModel.LAYER, com.darkcraft.client.gavran.GavranGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.caballeros.CaballeroModelo.CAPA, com.darkcraft.client.caballeros.CaballeroModelo::crear);
        e.registerLayerDefinition(com.darkcraft.client.personajes.MercaderRenderer.CAPA, com.darkcraft.client.personajes.MercaderRenderer::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.HuecoModelo.CAPA, com.darkcraft.client.enemigos.HuecoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.AhogadoModelo.CAPA, com.darkcraft.client.enemigos.AhogadoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.CapitanModelo.CAPA, com.darkcraft.client.enemigos.CapitanGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.GuardiaModelo.CAPA, com.darkcraft.client.enemigos.GuardiaGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.ArqueroModelo.CAPA, com.darkcraft.client.enemigos.ArqueroGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.PerroModelo.CAPA, com.darkcraft.client.enemigos.PerroGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.EspectroModelo.CAPA, com.darkcraft.client.enemigos.EspectroGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.EliteModelo.CAPA, com.darkcraft.client.enemigos.EliteGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.BrandtModelo.CAPA, com.darkcraft.client.enemigos.BrandtGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.CortezaModelo.CAPA, com.darkcraft.client.enemigos.CortezaGeometria::crear);
    }
}
