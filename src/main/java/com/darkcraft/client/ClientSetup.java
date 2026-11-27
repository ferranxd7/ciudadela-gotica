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
        e.register(com.darkcraft.client.morvain.MorvainCliente.EFECTOS, new com.darkcraft.client.morvain.MorvainCliente.Efectos());
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
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.REMOLINO.get(), com.darkcraft.client.fortaleza.RemolinoRenderer::new);
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
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ERUDITO.get(), com.darkcraft.client.enemigos.EruditoRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.TINTA.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ORBE.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.SALOBRE.get(), com.darkcraft.client.enemigos.SalobreRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PENITENTE.get(), com.darkcraft.client.enemigos.PenitenteRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.MARCADO.get(), com.darkcraft.client.enemigos.MarcadoRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.RED.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 2.2f, false));
        // Brann, el Ballenero (parte 7r): el, su arpon con el cabo, el farol de aceite que lanza y la Vieja
        // Anselmo, el Pontifice Sangrante (parte 7t)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PONTIFICE.get(), com.darkcraft.client.enemigos.PontificeRenderer::new);
        // Aldric, el Primer Marcado (parte 7v)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ALDRIC.get(), com.darkcraft.client.enemigos.AldricRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GIGANTE.get(), com.darkcraft.client.enemigos.GiganteRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ROCA_GIGANTE.get(), com.darkcraft.client.enemigos.RocaGiganteRenderer::new);
        // Ser Lucan, el Jinete Nocturno del camino de nubes (parte 7x)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.JINETE.get(), com.darkcraft.client.enemigos.JineteRenderer::new);
        // Pell, el Bufon del Rey, y lo que tira (parte 7y)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.BUFON.get(), com.darkcraft.client.enemigos.BufonRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PROYECTIL_BUFON.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.1f, true));
        // Vorthak, el Dragon de Ceniza, y su bola de fuego (grande y a plena luz) (parte 7z)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.DRAGON.get(), com.darkcraft.client.enemigos.DragonRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.GARGOLA.get(), com.darkcraft.client.enemigos.GargolaRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CENTINELA.get(), com.darkcraft.client.enemigos.CentinelaRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ISOLDE.get(), com.darkcraft.client.enemigos.IsoldeRenderer::new);
        // la Sombra sin Nombre, sus lanzas y su corazon (parte 7ac)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.SOMBRA.get(), com.darkcraft.client.enemigos.SombraRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.LANZA_SOMBRA.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.6f, true));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CORAZON_SOMBRA.get(), com.darkcraft.client.morvain.CorazonRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PROYECTIL_DRAGON.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 3.2f, true));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.BALLENERO.get(), com.darkcraft.client.enemigos.BalleneroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ARPON_BALLENERO.get(), com.darkcraft.client.enemigos.ArponBalleneroRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.FAROL_ACEITE.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.4f, true));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.VIEJA.get(), com.darkcraft.client.enemigos.ViejaRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ELITE.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.EliteModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.EliteModelo.CAPA)), "elite", 1, 0.6f, 1.15f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.BRANDT.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.BrandtModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.BrandtModelo.CAPA)), "brandt", 1, 0.9f, 1.5f));
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CORTEZA.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.CortezaModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.CortezaModelo.CAPA)), "corteza", 1, 0.9f, 1.45f));
        // el corcel de Morvain (parte 7b)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CORCEL.get(), com.darkcraft.client.montura.CorcelRenderer::new);
        // los podridos del Pantano Podrido y su fuego fatuo (parte 7i)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.PODRIDO.get(), com.darkcraft.client.enemigos.PodridoRenderer::new);
        // los helados de las Montanas Nevadas y sus esquirlas (parte 7k)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.HELADO.get(), com.darkcraft.client.enemigos.HeladoRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CALCINADO.get(), com.darkcraft.client.enemigos.CalcinadoRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CANTARO.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ESQUIRLA.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.FUEGO_FATUO.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        // los marchitos de Morvain (parte 7e)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.MARCHITO.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.MarchitoModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.MarchitoModelo.CAPA)), "marchito", 3, 0.5f, 1.08f));
        // los ahorcados del Bosque Muerto (parte 7g): con la soga del lazo hasta quien engancha
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.AHORCADO.get(), com.darkcraft.client.enemigos.AhorcadoRenderer::new);
        // la Molinera de Velmor (parte 7f)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.MOLINERA.get(), ctx -> new com.darkcraft.client.enemigos.EnemigoRenderer<>(ctx,
                new com.darkcraft.client.enemigos.MolineraModelo(ctx.bakeLayer(com.darkcraft.client.enemigos.MolineraModelo.CAPA)), "molinera", 1, 1.0f, 1.9f));
        // Maese Odran, el Verdugo de la Arboleda (parte 7h): con la soga de su lazo hasta quien engancha
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.VERDUGO.get(), com.darkcraft.client.enemigos.VerdugoRenderer::new);
        // el Barquero de la Laguna Negra (parte 7j)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.BARQUERO.get(), com.darkcraft.client.enemigos.BarqueroRenderer::new);
        // Hrodric, el Abad de la Cumbre (parte 7l)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.ABAD.get(), com.darkcraft.client.enemigos.AbadRenderer::new);
        // el Condestable del Juramento (parte 7n)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.CONDESTABLE.get(), com.darkcraft.client.enemigos.CondestableRenderer::new);
        // Ysolde, la Rectora de la Academia (parte 7p)
        e.registerEntityRenderer(com.darkcraft.DarkCraftMod.RECTORA.get(), com.darkcraft.client.enemigos.RectoraRenderer::new);
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
        e.registerLayerDefinition(com.darkcraft.client.enemigos.EruditoModelo.CAPA, com.darkcraft.client.enemigos.EruditoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.SalobreModelo.CAPA, com.darkcraft.client.enemigos.SalobreGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.PenitenteModelo.CAPA, com.darkcraft.client.enemigos.PenitenteGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.MarcadoModelo.CAPA, com.darkcraft.client.enemigos.MarcadoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.EliteModelo.CAPA, com.darkcraft.client.enemigos.EliteGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.BrandtModelo.CAPA, com.darkcraft.client.enemigos.BrandtGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.CortezaModelo.CAPA, com.darkcraft.client.enemigos.CortezaGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.montura.CorcelModelo.CAPA, com.darkcraft.client.enemigos.CorcelGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.MarchitoModelo.CAPA, com.darkcraft.client.enemigos.MarchitoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.PodridoModelo.CAPA, com.darkcraft.client.enemigos.PodridoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.HeladoModelo.CAPA, com.darkcraft.client.enemigos.HeladoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.CalcinadoModelo.CAPA, com.darkcraft.client.enemigos.CalcinadoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.AhorcadoModelo.CAPA, com.darkcraft.client.enemigos.AhorcadoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.MolineraModelo.CAPA, com.darkcraft.client.enemigos.MolineraGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.VerdugoModelo.CAPA, com.darkcraft.client.enemigos.VerdugoGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.BarqueroModelo.CAPA, com.darkcraft.client.enemigos.BarqueroGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.AbadModelo.CAPA, com.darkcraft.client.enemigos.AbadGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.CondestableModelo.CAPA, com.darkcraft.client.enemigos.CondestableGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.RectoraModelo.CAPA, com.darkcraft.client.enemigos.RectoraGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.BalleneroModelo.CAPA, com.darkcraft.client.enemigos.BalleneroGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.PontificeModelo.CAPA, com.darkcraft.client.enemigos.PontificeGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.AldricModelo.CAPA, com.darkcraft.client.enemigos.AldricGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.GiganteModelo.CAPA, com.darkcraft.client.enemigos.GiganteGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.JineteModelo.CAPA, com.darkcraft.client.enemigos.JineteGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.BufonModelo.CAPA, com.darkcraft.client.enemigos.BufonGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.DragonModelo.CAPA, com.darkcraft.client.enemigos.DragonGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.GargolaModelo.CAPA, com.darkcraft.client.enemigos.GargolaGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.CentinelaModelo.CAPA, com.darkcraft.client.enemigos.CentinelaGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.IsoldeModelo.CAPA, com.darkcraft.client.enemigos.IsoldeGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.SombraModelo.CAPA, com.darkcraft.client.enemigos.SombraGeometria::crear);
        e.registerLayerDefinition(com.darkcraft.client.enemigos.ViejaModelo.CAPA, com.darkcraft.client.enemigos.ViejaGeometria::crear);
    }
}
