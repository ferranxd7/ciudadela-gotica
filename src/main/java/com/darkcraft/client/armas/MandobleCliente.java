package com.darkcraft.client.armas;

import com.darkcraft.armas.MandobleJuramento;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** El Mandoble del Juramento en la mano: se lleva con las dos manos en la empunadura (si la otra mano esta libre). */
public final class MandobleCliente implements IClientItemExtensions {
    public static final MandobleCliente INSTANCIA = new MandobleCliente();

    /** Las dos manos juntas en la empunadura, la hoja hacia delante y arriba. */
    private static final HumanoidModel.ArmPose DOS_MANOS = HumanoidModel.ArmPose.create("DARKCRAFT_MANDOBLE", true, (model, e, arm) -> {
        boolean derecha = arm == HumanoidArm.RIGHT;
        ModelPart principal = derecha ? model.rightArm : model.leftArm, otra = derecha ? model.leftArm : model.rightArm;
        float cabeza = Mth.clamp(model.head.xRot, -0.6f, 0.6f) * 0.35f;
        principal.xRot = -0.72f + cabeza;
        principal.yRot = derecha ? -0.32f : 0.32f;
        otra.xRot = -0.82f + cabeza;
        otra.yRot = derecha ? 0.62f : -0.62f;
        otra.zRot = 0f;
    });

    private MandobleCliente() {}

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity e, InteractionHand hand, ItemStack s) {
        if (hand != InteractionHand.MAIN_HAND || !(e instanceof Player p) || !MandobleJuramento.dosManos(p)) return null;
        return DOS_MANOS;
    }
}
