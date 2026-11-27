package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Corazon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * El corazon de la Sombra (parte 7ac): su objeto ({@code corazon_sombra}, un corazon de cubos rojo oscuro con venas negras) a plena
 * luz, flotando, girando despacio y latiendo (un golpe doble: {@link Corazon#latido}).
 */
public class CorazonRenderer extends EntityRenderer<Corazon> {
    private final ItemRenderer items;
    private ItemStack objeto;

    public CorazonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        items = ctx.getItemRenderer();
        shadowRadius = 0f;
    }

    @Override
    public void render(Corazon e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (objeto == null) objeto = new ItemStack(DarkCraftMod.CORAZON_SOMBRA_OBJETO.get());
        float t = e.tickCount + pt;
        float lat = e.latido(pt);
        ps.pushPose();
        ps.translate(0, 0.45 + Mth.sin(t * 0.06f) * 0.08f, 0);
        ps.mulPose(Axis.YP.rotationDegrees(t * 1.2f));
        float s = 1.25f * (1f + 0.16f * lat);
        ps.scale(s, s, s);
        Level lvl = e.level();
        items.renderStatic(objeto, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, ps, buf, lvl, e.getId());
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Corazon e) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}
