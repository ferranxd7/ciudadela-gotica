package com.darkcraft.hechizos;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Un hechizo aprendido en un pergamino (hechicerias) o en una plegaria (milagros). Se lleva en el inventario y se
 * memoriza descansando en un santuario; lo memorizado se elige con la flecha arriba (la cruz) y se lanza con el
 * baston o el sello en la mano.
 */
public final class HechizoItem extends Item {
    public final Hechizo hechizo;

    public HechizoItem(Hechizo h, Properties p) {
        super(p);
        this.hechizo = h;
    }

    public static Hechizo de(ItemStack st) {
        return st.getItem() instanceof HechizoItem h ? h.hechizo : null;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        Hechizo h = hechizo;
        tip.add(Component.translatable("item.darkcraft.hechizo_" + h.id + ".desc").withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable(h.escuela == Hechizo.Escuela.HECHICERIA ? "darkcraft.hechizo.hechiceria" : "darkcraft.hechizo.milagro",
                h.mana, h.huecos, h.requisito).withStyle(ChatFormatting.GRAY));
    }
}
