package com.darkcraft.armadura;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.rol.Resistencias;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Una pieza de un conjunto de armadura con modelo propio (parte 7ai): yelmo, peto, guantes (en el hueco de los pies) o
 * grebas. No se rompe. Su ficha (la del menu y el recuadro) dice su defensa, lo que pesa, lo que resiste y de quien era;
 * los "+N de armadura" de Minecraft no salen (dirian "en los pies" para los guantes).
 */
public class PiezaArmadura extends ArmorItem {
    public final Conjunto conjunto;
    public final int pieza;

    public PiezaArmadura(Conjunto conjunto, int pieza, Properties p) {
        super(conjunto.material(), Conjunto.tipo(pieza), p);
        this.conjunto = conjunto;
        this.pieza = pieza;
    }

    /** La pieza de un objeto, o null si no es de un conjunto. */
    @Nullable
    public static PiezaArmadura de(ItemStack st) {
        return st.getItem() instanceof PiezaArmadura p ? p : null;
    }

    @Override
    public String getArmorTexture(ItemStack st, Entity e, EquipmentSlot slot, String tipo) {
        return DarkCraftMod.MODID + ":textures/models/armor/" + conjunto.id + ".png";
    }

    @Override
    public int getDefaultTooltipHideFlags(ItemStack st) {
        return ItemStack.TooltipPart.MODIFIERS.getMask();
    }

    @Override
    public boolean isValidRepairItem(ItemStack st, ItemStack con) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack st) {
        return false;
    }

    /** Las lineas de su ficha: defensa y peso, lo que resiste (lo que tenga) y de quien era. */
    public List<Component> lineas() {
        List<Component> l = new ArrayList<>();
        l.add(Component.translatable("darkcraft.armadura.defensa", conjunto.defensa(pieza),
                String.format(Locale.ROOT, "%.1f", conjunto.peso(pieza))).withStyle(ChatFormatting.GOLD));
        MutableComponent r = Component.translatable("darkcraft.armadura.resiste");
        boolean alguna = false;
        for (Resistencias.Tipo t : Conjunto.ELEMENTOS) {
            float v = conjunto.resistencia(pieza, t);
            if (v < 0.005f) continue;
            r.append(alguna ? ", " : " ").append(Component.translatable("darkcraft.resistencia." + t.id))
                    .append(" " + Math.round(v * 100f) + " %");
            alguna = true;
        }
        if (alguna) l.add(r.withStyle(ChatFormatting.GRAY));
        l.add(Component.translatable("darkcraft.armadura.conjunto", Component.translatable("darkcraft.armadura." + conjunto.id))
                .withStyle(ChatFormatting.DARK_GRAY));
        if (conjunto.esDeJefe())
            l.add(Component.translatable("darkcraft.armadura.jefe", Component.translatable("entity.darkcraft." + conjunto.jefe))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        l.add(Component.translatable("item.darkcraft." + conjunto.objeto(pieza) + ".desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return l;
    }

    @Override
    public void appendHoverText(ItemStack st, @Nullable Level lvl, List<Component> tip, TooltipFlag f) {
        tip.addAll(lineas());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // ojo: Forge llama a esto desde el constructor de Item, antes de que se pongan conjunto y pieza (en la build 199 el
        // cliente no arrancaba): la extension los mira en el objeto al dibujar
        consumer.accept(com.darkcraft.client.armadura.ModelosArmadura.EXTENSION);
    }
}
