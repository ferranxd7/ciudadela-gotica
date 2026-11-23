package com.darkcraft.client.caballeros;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.caballeros.CaballeroZombi;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * El caballero zombi: cuerpo de humano (piel de 64x64) con poses segun el arma: espada (brazos de zombi al atacar y
 * el escudo levantado cuando se cubre), alabarda (a dos manos, tajo de arriba abajo) y ballesta (cargar y apuntar).
 */
public class CaballeroModelo extends HumanoidModel<CaballeroZombi> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "caballero"), "main");

    public static LayerDefinition crear() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0f), 64, 64);
    }

    public CaballeroModelo(ModelPart root) {
        super(root);
    }

    @Override
    public void prepareMobModel(CaballeroZombi e, float swing, float amount, float pt) {
        rightArmPose = ArmPose.EMPTY;
        leftArmPose = ArmPose.EMPTY;
        ItemStack main = e.getMainHandItem();
        if (main.is(Items.CROSSBOW)) {
            if (e.cargando() && e.isUsingItem()) rightArmPose = ArmPose.CROSSBOW_CHARGE;
            else if (e.isAggressive()) rightArmPose = ArmPose.CROSSBOW_HOLD;
            else rightArmPose = ArmPose.ITEM;
        } else if (!main.isEmpty()) rightArmPose = ArmPose.ITEM;
        if (e.isUsingItem() && e.getUsedItemHand() == InteractionHand.OFF_HAND && e.getOffhandItem().is(Items.SHIELD)) leftArmPose = ArmPose.BLOCK;
        else if (!e.getOffhandItem().isEmpty()) leftArmPose = ArmPose.ITEM;
        super.prepareMobModel(e, swing, amount, pt);
    }

    @Override
    public void setupAnim(CaballeroZombi e, float swing, float amount, float edad, float yaw, float pitch) {
        super.setupAnim(e, swing, amount, edad, yaw, pitch);
        if (e.getMainHandItem().is(DarkCraftMod.ALABARDA.get())) {
            // a dos manos: la alabarda por delante y el tajo de arriba abajo
            float s = Mth.sin(Mth.sqrt(attackTime) * Mth.PI);
            float listo = e.isAggressive() ? 1f : 0.45f;
            rightArm.xRot = -0.55f * listo - 0.25f - s * 1.9f + Mth.cos(edad * 0.09f) * 0.03f;
            rightArm.yRot = -0.32f + s * 0.25f;
            rightArm.zRot = 0.08f;
            leftArm.xRot = -0.85f * listo - 0.2f - s * 1.5f;
            leftArm.yRot = 0.62f - s * 0.2f;
            leftArm.zRot = -0.05f;
            body.yRot = -0.12f * listo + s * 0.3f;
        } else if (rightArmPose == ArmPose.ITEM && leftArmPose != ArmPose.BLOCK && e.isAggressive() && !e.getMainHandItem().is(Items.CROSSBOW)) {
            // espada: brazos de zombi, con el del escudo un poco mas bajo
            AnimationUtils.animateZombieArms(leftArm, rightArm, true, attackTime, edad);
            leftArm.xRot += 0.35f;
            leftArm.yRot += 0.25f;
        }
        hat.copyFrom(head);
    }
}
