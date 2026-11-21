package com.ciudadela.client.armas;

import com.ciudadela.armas.EspadaRey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Las espadas del Rey en la mano: cruzadas delante cuando se cubre. */
public final class EspadaCliente implements IClientItemExtensions {
    public static final EspadaCliente INSTANCIA = new EspadaCliente();

    private EspadaCliente() {}

    private static boolean cubre(LivingEntity e) {
        return e instanceof Player p && p.isUsingItem() && EspadaRey.es(p.getUseItem());
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity e, InteractionHand hand, ItemStack s) {
        return cubre(e) ? HumanoidModel.ArmPose.BLOCK : null;
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack ps, LocalPlayer p, HumanoidArm arm, ItemStack s, float pt, float equip, float swing) {
        if (!cubre(p)) return false;
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        ps.translate(side * 0.30f, -0.42f, -0.72f);
        ps.mulPose(Axis.YP.rotationDegrees(side * 12f));
        ps.mulPose(Axis.ZP.rotationDegrees(side * 62f));
        ps.mulPose(Axis.XP.rotationDegrees(-18f));
        return true;
    }
}
