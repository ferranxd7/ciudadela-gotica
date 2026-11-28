package com.darkcraft.mision;

import com.darkcraft.enemigos.Congelacion;
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
 * La Espada de la Guardiana (parte 7ap): la espada larga de Sigrun, para quien acaba con ella en el Paso del Colmillo. Para el combate
 * es una espada ({@code Armas.de}: sus golpes y su habilidad) que escala con la Fuerza, mejor con la Destreza y algo con la Fe
 * ({@code Escalado.de}: C, B y Fe E) y, como guardo nueve dias el paso helado, cada golpe suma {@link #FRIO} de frio
 * ({@link Congelacion}: a la quinta, se congela; a los jefes les entra la mitad).
 */
public class EspadaGuardiana extends SwordItem {
    /** Lo que suma de frio cada golpe. */
    public static final float FRIO = 22f;
    /** Para la prueba: los golpes que han sumado frio desde que arranco el juego. */
    public static int GOLPES;

    public EspadaGuardiana(Properties p) {
        super(Tiers.IRON, 4, -2.5f, p);
    }

    @Override
    public boolean hurtEnemy(ItemStack st, LivingEntity a, LivingEntity quien) {
        if (!a.level().isClientSide && a.isAlive()) {
            Congelacion.sumar(a, FRIO, quien);
            GOLPES++;
        }
        return super.hurtEnemy(st, a, quien);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.espada_guardiana.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("item.darkcraft.espada_guardiana.frio").withStyle(ChatFormatting.AQUA));
    }
}
