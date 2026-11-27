package com.darkcraft.mision;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * La Pertiga del farolero (parte 7ao): la vara de fresno de Corwen, con el gancho de laton con que colgaba los faroles y una punta de
 * hierro; se la da a quien le pide que se quede a guardar la luz. Para el combate es una lanza ({@code Armas.de}: sus golpes, su
 * estocada) que escala con la Destreza y con la Fe ({@code Escalado.de}: D, C y Fe C) y, como el gancho aun guarda brasas, uno de cada
 * cuatro golpes prende al enemigo ({@link #PRENDE} de probabilidad, {@link #SEGUNDOS} segundos).
 */
public class Pertiga extends SwordItem {
    /** Lo que prende: la probabilidad de cada golpe y lo que arde. */
    public static final float PRENDE = 0.25f;
    public static final int SEGUNDOS = 4;
    /** Para la prueba: los golpes que han prendido desde que arranco el juego. */
    public static int PRENDIDOS;

    public Pertiga(Properties p) {
        super(Tiers.IRON, 3, -2.6f, p);
    }

    @Override
    public boolean hurtEnemy(ItemStack st, LivingEntity a, LivingEntity quien) {
        if (!a.level().isClientSide && a.isAlive() && !a.fireImmune() && a.getRandom().nextFloat() < PRENDE) {
            a.setSecondsOnFire(SEGUNDOS);
            PRENDIDOS++;
        }
        return super.hurtEnemy(st, a, quien);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.pertiga_farolero.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
