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
 * los "+N de armadura" de Minecraft no salen (dirian "en los pies" para los guantes). Se mejora en la forja de un herrero
 * hasta +5 (parte 7am, {@link com.darkcraft.forja.Forja}): la mejora suma defensa y dureza (sus modificadores de atributo,
 * {@link #getAttributeModifiers}) y lo que resiste ({@link #resistencia(ItemStack, Resistencias.Tipo)}).
 */
public class PiezaArmadura extends ArmorItem {
    public final Conjunto conjunto;
    public final int pieza;
    /** Los de la mejora de la forja, uno por pieza (cada pieza va en su hueco: no chocan). */
    private static final java.util.UUID[] MEJORA = {
            java.util.UUID.fromString("7d1c0a4e-3b6f-4c1a-9e1d-2f6a8b0c7a01"),
            java.util.UUID.fromString("7d1c0a4e-3b6f-4c1a-9e1d-2f6a8b0c7a02"),
            java.util.UUID.fromString("7d1c0a4e-3b6f-4c1a-9e1d-2f6a8b0c7a03"),
            java.util.UUID.fromString("7d1c0a4e-3b6f-4c1a-9e1d-2f6a8b0c7a04")};

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

    /** La defensa de esta pieza con su mejora (+10 % por nivel: 8 a +5 son 12). */
    public float defensa(ItemStack st) {
        return conjunto.defensa(pieza) * (1f + com.darkcraft.forja.Forja.POR_NIVEL_ARMADURA * com.darkcraft.forja.Forja.nivel(st));
    }

    /** Su dureza con la mejora (la del conjunto mas 0,25 por nivel). */
    public float dureza(ItemStack st) {
        return conjunto.dureza + com.darkcraft.forja.Forja.DUREZA_POR_NIVEL * com.darkcraft.forja.Forja.nivel(st);
    }

    /** Lo que resiste esta pieza de un elemento con su mejora (+8 % de lo suyo por nivel). */
    public float resistencia(ItemStack st, Resistencias.Tipo t) {
        float r = conjunto.resistencia(pieza, t);
        if (r <= 0f) return 0f;
        return Math.min(0.9f, r * (1f + com.darkcraft.forja.Forja.RESISTE_POR_NIVEL * com.darkcraft.forja.Forja.nivel(st)));
    }

    /** Lo de Minecraft (la defensa, la dureza y el empuje del conjunto) y, si esta mejorada, lo que le suma la mejora. */
    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> getAttributeModifiers(
            EquipmentSlot slot, ItemStack st) {
        var base = super.getAttributeModifiers(slot, st);
        int n = com.darkcraft.forja.Forja.nivel(st);
        if (n <= 0 || slot != getEquipmentSlot()) return base;
        com.google.common.collect.ImmutableMultimap.Builder<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> b =
                com.google.common.collect.ImmutableMultimap.builder();
        b.putAll(base);
        b.put(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, new net.minecraft.world.entity.ai.attributes.AttributeModifier(MEJORA[pieza],
                "Mejora de la armadura", defensa(st) - conjunto.defensa(pieza), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        b.put(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS, new net.minecraft.world.entity.ai.attributes.AttributeModifier(MEJORA[pieza],
                "Mejora de la armadura", com.darkcraft.forja.Forja.DUREZA_POR_NIVEL * n, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        return b.build();
    }

    @Override
    public boolean isEnchantable(ItemStack st) {
        return false;
    }

    /** La defensa para la ficha: entera sin mejorar y con un decimal mejorada ("8" / "8,8"). */
    public String defensaTexto(ItemStack st) {
        return com.darkcraft.forja.Forja.nivel(st) <= 0 ? String.valueOf(conjunto.defensa(pieza))
                : String.format(Locale.ROOT, "%.1f", defensa(st));
    }

    /** Las lineas de su ficha (sin mejorar). */
    public List<Component> lineas() {
        return lineas(ItemStack.EMPTY);
    }

    /** Las lineas de su ficha: defensa y peso (con la mejora de esa pieza), lo que resiste (lo que tenga) y de quien era. */
    public List<Component> lineas(ItemStack st) {
        List<Component> l = new ArrayList<>();
        l.add(Component.translatable("darkcraft.armadura.defensa", defensaTexto(st),
                String.format(Locale.ROOT, "%.1f", conjunto.peso(pieza))).withStyle(ChatFormatting.GOLD));
        MutableComponent r = Component.translatable("darkcraft.armadura.resiste");
        boolean alguna = false;
        for (Resistencias.Tipo t : Conjunto.ELEMENTOS) {
            float v = resistencia(st, t);
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
        if (com.darkcraft.forja.Forja.nivel(st) > 0) tip.add(com.darkcraft.forja.Forja.linea(st));
        tip.addAll(lineas(st));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // ojo: Forge llama a esto desde el constructor de Item, antes de que se pongan conjunto y pieza (en la build 199 el
        // cliente no arrancaba): la extension los mira en el objeto al dibujar
        consumer.accept(com.darkcraft.client.armadura.ModelosArmadura.EXTENSION);
    }
}
