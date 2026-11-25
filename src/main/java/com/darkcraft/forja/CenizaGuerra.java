package com.darkcraft.forja;

import com.darkcraft.combate.Armas;
import com.darkcraft.combate.Habilidad;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Una ceniza de guerra: en la forja de Dorvan se le pone a un arma y le cambia la habilidad ({@link Habilidad}). Al
 * ponerla se gasta; si el arma ya llevaba otra, esa vuelve a la bolsa ({@link Forja#ceniza}).
 */
public class CenizaGuerra extends Item {
    public final Habilidad habilidad;

    public CenizaGuerra(Habilidad h, Properties p) {
        super(p);
        this.habilidad = h;
    }

    public static Habilidad de(ItemStack st) {
        return st.getItem() instanceof CenizaGuerra c ? c.habilidad : null;
    }

    /** Para que tipos de arma vale (los nombres, separados por comas). */
    public static MutableComponent tipos(Habilidad h) {
        MutableComponent c = Component.empty();
        boolean primero = true;
        for (Armas.Tipo t : h.tipos()) {
            if (!primero) c.append(", ");
            c.append(Component.translatable("darkcraft.arma.tipo." + t.name().toLowerCase(java.util.Locale.ROOT)));
            primero = false;
        }
        return c;
    }

    /** Lo que cuesta usarla ("14 de mana" o "vida"). */
    public static Component coste(Habilidad h) {
        if (h == Habilidad.SANGRE) return Component.translatable("darkcraft.habilidad.coste_vida", Math.round(Habilidad.VIDA_SANGRE * 100));
        if (h.mana <= 0) return Component.translatable("darkcraft.habilidad.coste_nada");
        return Component.translatable("darkcraft.habilidad.coste_mana", h.mana);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable(habilidad.clave()).withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable(habilidad.clave() + ".desc").withStyle(ChatFormatting.GRAY));
        tip.add(coste(habilidad).copy().withStyle(ChatFormatting.DARK_AQUA));
        tip.add(Component.translatable("darkcraft.ceniza.para", tipos(habilidad)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
