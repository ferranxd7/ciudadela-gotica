package com.darkcraft.client.escudos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Escudo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * Los escudos en el cliente (parte 7ak): sus capas de modelo (la geometria de {@code tools/escudos/escudos.py}), el dibujante
 * (un {@link EscudoRenderer}, como el del escudo del juego: los modelos de objeto son {@code builtin/entity}), la propiedad
 * {@code blocking} (el modelo de objeto de cuando se esta cubierto) y, con la rodela alzada, se anda a la mitad en vez de a la
 * quinta parte (el juego frena lo que se mueve quien usa algo: aqui se le devuelve parte antes).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EscudoCliente {
    private EscudoCliente() {}

    public static final ModelLayerLocation[] CAPAS = new ModelLayerLocation[Escudo.Tamano.values().length];

    static {
        for (Escudo.Tamano t : Escudo.Tamano.values())
            CAPAS[t.ordinal()] = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "escudo_" + t.id), "main");
    }

    /** La de todos los escudos (la pide el objeto desde su constructor: no puede mirar sus campos). */
    public static final IClientItemExtensions EXTENSION = new IClientItemExtensions() {
        private EscudoRenderer r;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (r == null) r = new EscudoRenderer();
            return r;
        }
    };

    @SubscribeEvent
    public static void capas(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(CAPAS[Escudo.Tamano.PEQUENO.ordinal()], RodelaGeometria::crear);
        e.registerLayerDefinition(CAPAS[Escudo.Tamano.MEDIANO.ordinal()], EscudoCaballeroGeometria::crear);
        e.registerLayerDefinition(CAPAS[Escudo.Tamano.GRANDE.ordinal()], PavesGeometria::crear);
    }

    /** Al recargar los recursos se vuelven a hornear. */
    @SubscribeEvent
    public static void recargar(EntityRenderersEvent.AddLayers e) {
        EscudoRenderer.olvidar();
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            ResourceLocation blocking = new ResourceLocation("blocking");
            for (RegistryObject<net.minecraft.world.item.Item> it : DarkCraftMod.ESCUDOS.values())
                ItemProperties.register(it.get(), blocking, (st, lvl, ent, seed) -> ent != null && ent.isUsingItem() && ent.getUseItem() == st ? 1f : 0f);
        });
    }

    /** Lo del bus del juego. */
    @Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
    public static final class Juego {
        private Juego() {}

        /** Con la rodela alzada se anda a la mitad (el juego, despues de esto, lo deja en la quinta parte: x 2,5 = la mitad). */
        @SubscribeEvent
        public static void andar(MovementInputUpdateEvent e) {
            if (Escudo.alzado(e.getEntity()) != Escudo.Tamano.PEQUENO || e.getEntity().isPassenger()) return;
            e.getInput().leftImpulse *= 2.5f;
            e.getInput().forwardImpulse *= 2.5f;
        }
    }
}
