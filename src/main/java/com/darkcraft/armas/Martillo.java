package com.darkcraft.armas;

import com.darkcraft.combate.Armas;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Los martillos (parte 7aj, respuesta 109 del plan): la maza de clerigo y el martillo de guerra (de una mano: {@link Armas.Tipo#MARTILLO})
 * y el gran martillo de piedra (a dos manos: {@link Armas.Tipo#GRAN_MARTILLO}). Para el combate son su tipo (sus golpes de arriba
 * abajo, su mazazo cargado); lo suyo lo hace {@code combate.Combate}: pegan un poco mas a los que llevan armadura, desequilibran
 * mucho y, a quien se cubre con un escudo, le rompen la guardia. La maza escala ademas con la Fe ({@code combate.Escalado}).
 */
public class Martillo extends SwordItem {
    /** Lo que pegan de mas a los que llevan armadura (como el dano contundente de Elden Ring). */
    public static final float CONTRA_ARMADURA = 1.15f;

    public final Armas.Tipo tipo;
    private final String id;

    public Martillo(String id, Armas.Tipo tipo, int dano, float velocidad, Properties p) {
        super(Tiers.IRON, dano, velocidad, p);
        this.id = id;
        this.tipo = tipo;
    }

    public static boolean es(ItemStack st) {
        return st.getItem() instanceof Martillo;
    }

    /** Si ese golpe lo da alguien con un martillo en la mano (cuerpo a cuerpo): lo miran los escudos de los enemigos del mod. */
    public static boolean rompe(net.minecraft.world.damagesource.DamageSource s) {
        return s.getEntity() instanceof net.minecraft.world.entity.LivingEntity le && s.getDirectEntity() == le && es(le.getMainHandItem());
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft." + id + ".desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("darkcraft.martillo.guardia").withStyle(ChatFormatting.GOLD));
    }
}
