package com.darkcraft.hechizos;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Con lo que se lanzan los hechizos: el baston (hechicerias) y el sello (milagros). Se llevan en la mano derecha; con
 * el en la mano el clic de atacar lanza el hechizo elegido (si es de su escuela). Pega poco como arma.
 */
public final class Catalizador extends Item {
    public final Hechizo.Escuela escuela;

    public Catalizador(Hechizo.Escuela escuela, Properties p) {
        super(p);
        this.escuela = escuela;
    }

    public static Hechizo.Escuela de(ItemStack st) {
        return st.getItem() instanceof Catalizador c ? c.escuela : null;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable(escuela == Hechizo.Escuela.HECHICERIA ? "darkcraft.catalizador.baston.desc" : "darkcraft.catalizador.sello.desc")
                .withStyle(ChatFormatting.GOLD));
    }
}
