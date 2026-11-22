package com.ciudadela.client.armas;

import com.ciudadela.armas.EspadaRey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.joml.Quaternionf;

/** Las espadas del Rey en la mano: cruzadas en X delante de la cara cuando se cubre. */
public final class EspadaCliente implements IClientItemExtensions {
    public static final EspadaCliente INSTANCIA = new EspadaCliente();

    private static final float DEG = (float) (Math.PI / 180.0);

    private EspadaCliente() {}

    private static boolean cubre(LivingEntity e) {
        return e instanceof Player p && p.isUsingItem() && EspadaRey.es(p.getUseItem());
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity e, InteractionHand hand, ItemStack s) {
        return cubre(e) ? HumanoidModel.ArmPose.BLOCK : null;
    }

    /**
     * Primera persona cubriendose: cada espada de frente a la camara, inclinada hacia atras (asi le da la luz) y
     * girada para que las dos hojas formen una X un poco por debajo del centro de la pantalla. La transformacion
     * de "mano en primera persona" del propio modelo se deshace antes, para que la pose sea exactamente esta.
     */
    @Override
    public boolean applyForgeHandTransform(PoseStack ps, LocalPlayer p, HumanoidArm arm, ItemStack s, float pt, float equip, float swing) {
        if (!cubre(p)) return false;
        boolean right = arm == HumanoidArm.RIGHT;
        int side = right ? 1 : -1;
        float k = Mth.clamp((p.getTicksUsingItem() + pt) / 4f, 0f, 1f);
        k = k * k * (3 - 2 * k);
        ps.translate(side * (0.11f + 0.30f * (1 - k)), -0.27f - 0.30f * (1 - k), right ? -0.74f : -0.77f);
        ps.mulPose(Axis.XP.rotationDegrees(-30f));
        ps.mulPose(Axis.ZP.rotationDegrees(right ? 90f : 0f));
        ps.scale(0.8f, 0.8f, 0.8f);
        deshacerMano(ps, p, s, right);
        return true;
    }

    /** Lo contrario de lo que hara el modelo del objeto en primera persona (giro y desplazamiento). */
    private static void deshacerMano(PoseStack ps, LocalPlayer p, ItemStack s, boolean right) {
        BakedModel m = Minecraft.getInstance().getItemRenderer().getModel(s, p.level(), p, p.getId());
        ItemTransform t = m.getTransforms().getTransform(right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        if (t == ItemTransform.NO_TRANSFORM) return;
        float rx = t.rotation.x(), ry = t.rotation.y(), rz = t.rotation.z();
        if (!right) {
            ry = -ry;
            rz = -rz;
        }
        int i = right ? 1 : -1;
        ps.mulPose(new Quaternionf().rotationXYZ(rx * DEG, ry * DEG, rz * DEG).conjugate());
        ps.translate(-i * t.translation.x(), -t.translation.y(), -t.translation.z());
    }
}
