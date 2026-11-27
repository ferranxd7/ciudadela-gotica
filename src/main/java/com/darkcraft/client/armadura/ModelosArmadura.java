package com.darkcraft.client.armadura;

import com.darkcraft.DarkCraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Los modelos de las armaduras con modelo propio (parte 7ai): una capa de modelo por conjunto y pieza (la geometria la escribe
 * {@code tools/armaduras/armaduras.py} en {@link ArmaduraGeometria}), horneada la primera vez que se dibuja y guardada hasta
 * que se recargan los recursos. La capa de armadura de Minecraft copia en el modelo la postura del jugador y dibuja con la
 * textura de {@code PiezaArmadura.getArmorTexture}.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModelosArmadura {
    private ModelosArmadura() {}

    private static final String[] PIEZAS = {"yelmo", "peto", "guantes", "grebas"};
    private static final Map<String, Modelo> HECHOS = new ConcurrentHashMap<>();
    /** Los que no se pudieron hornear (para no intentarlo cada fotograma). */
    private static final Map<String, Boolean> FALLIDOS = new ConcurrentHashMap<>();

    public static ModelLayerLocation capa(String conjunto, int pieza) {
        return new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "armadura_" + conjunto), PIEZAS[pieza]);
    }

    @SubscribeEvent
    public static void capas(EntityRenderersEvent.RegisterLayerDefinitions e) {
        for (String c : ArmaduraGeometria.CONJUNTOS)
            for (int p = 0; p < 4; p++) {
                LayerDefinition d = ArmaduraGeometria.crear(c, p);
                if (d != null) e.registerLayerDefinition(capa(c, p), () -> d);
            }
    }

    /** Al recargar los recursos (F3 + T, cambiar de paquete) se hornean otra vez. */
    @SubscribeEvent
    public static void recargar(EntityRenderersEvent.AddLayers e) {
        HECHOS.clear();
        FALLIDOS.clear();
    }

    /** El modelo de una pieza, o null si ese conjunto no tiene esa pieza (se dibuja entonces el de Minecraft). */
    public static HumanoidModel<?> de(String conjunto, int pieza) {
        String k = conjunto + ":" + pieza;
        Modelo m = HECHOS.get(k);
        if (m != null) return m;
        if (FALLIDOS.containsKey(k)) return null;
        try {
            m = new Modelo(Minecraft.getInstance().getEntityModels().bakeLayer(capa(conjunto, pieza)));
            HECHOS.put(k, m);
            return m;
        } catch (RuntimeException ex) {
            FALLIDOS.put(k, true);
            DarkCraftMod.LOGGER.warn("[DarkCraft] sin modelo de armadura {}: {}", k, ex.toString());
            return null;
        }
    }

    /** Cuantos modelos de armadura hay horneados (la prueba del cliente). */
    public static int horneados() {
        return HECHOS.size();
    }

    /** La de todas las piezas: el modelo del conjunto y la pieza del objeto que se dibuja. */
    public static final IClientItemExtensions EXTENSION = new IClientItemExtensions() {
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity quien, ItemStack st, EquipmentSlot hueco, HumanoidModel<?> original) {
            if (!(st.getItem() instanceof com.darkcraft.armadura.PiezaArmadura pa) || pa.conjunto == null) return original;
            HumanoidModel<?> m = de(pa.conjunto.id, pa.pieza);
            return m != null ? m : original;
        }
    };

    /**
     * El modelo de una pieza: un {@link HumanoidModel} con las siete piezas del jugador y las cajas de la armadura. La capa de
     * Minecraft enciende solo las piezas del hueco (los guantes van en el de los pies, que enciende las piernas): aqui se
     * dibujan siempre todas (las que no tienen cajas de esa pieza no dibujan nada).
     */
    public static final class Modelo extends HumanoidModel<LivingEntity> {
        public Modelo(ModelPart raiz) {
            super(raiz);
        }

        @Override
        public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
            head.visible = hat.visible = body.visible = true;
            rightArm.visible = leftArm.visible = rightLeg.visible = leftLeg.visible = true;
            super.renderToBuffer(ps, vc, luz, capa, r, g, b, a);
        }
    }
}
