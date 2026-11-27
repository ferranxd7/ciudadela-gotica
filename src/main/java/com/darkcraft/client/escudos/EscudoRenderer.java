package com.darkcraft.client.escudos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Escudo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * El dibujante de los escudos (como el del escudo del juego en {@code BlockEntityWithoutLevelRenderer}): el modelo de su tamano
 * (horneado la primera vez y guardado hasta que se recargan los recursos) con su textura ({@code textures/entity/escudos/<id>.png}),
 * dado la vuelta en Y y en Z como el del juego (asi le valen las posturas de sus modelos de objeto), con el brillo si esta encantado.
 */
public class EscudoRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation[] TEX = new ResourceLocation[Escudo.Tamano.values().length];
    private static final ModelPart[] HECHOS = new ModelPart[Escudo.Tamano.values().length];

    static {
        for (Escudo.Tamano t : Escudo.Tamano.values())
            TEX[t.ordinal()] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/escudos/" + t.id + ".png");
    }

    public EscudoRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    static void olvidar() {
        java.util.Arrays.fill(HECHOS, null);
    }

    private static ModelPart modelo(Escudo.Tamano t) {
        ModelPart m = HECHOS[t.ordinal()];
        if (m == null) {
            m = Minecraft.getInstance().getEntityModels().bakeLayer(EscudoCliente.CAPAS[t.ordinal()]);
            HECHOS[t.ordinal()] = m;
        }
        return m;
    }

    /** Para la prueba de la CI: cuantos modelos hay horneados. */
    public static int horneados() {
        int n = 0;
        for (ModelPart m : HECHOS) if (m != null) n++;
        return n;
    }

    @Override
    public void renderByItem(ItemStack st, ItemDisplayContext ctx, PoseStack ps, MultiBufferSource buf, int luz, int capa) {
        if (!(st.getItem() instanceof Escudo e)) return;
        ps.pushPose();
        ps.scale(1.0f, -1.0f, -1.0f);
        VertexConsumer vc = ItemRenderer.getFoilBufferDirect(buf, RenderType.entityCutoutNoCull(TEX[e.tamano.ordinal()]), true, st.hasFoil());
        modelo(e.tamano).render(ps, vc, luz, capa);
        ps.popPose();
    }
}
