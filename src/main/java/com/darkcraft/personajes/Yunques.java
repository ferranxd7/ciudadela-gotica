package com.darkcraft.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.dialogo.Conversaciones;
import com.darkcraft.net.Net;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;

/**
 * Los yunques ya no abren la pantalla de Minecraft (toda la interfaz es del mod): junto a un herrero, usar su yunque es
 * hablar con el (y de ahi, a su forja); sin herrero cerca, un aviso. En creativo se deja como siempre.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Yunques {
    private Yunques() {}

    @SubscribeEvent
    public static void onUsar(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getLevel().getBlockState(e.getPos()).getBlock() instanceof AnvilBlock) || e.getEntity().isCreative()) return;
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        List<Herrero> h = sp.level().getEntitiesOfClass(Herrero.class, new net.minecraft.world.phys.AABB(e.getPos()).inflate(6));
        h.sort(Comparator.comparingDouble(x -> x.distanceToSqr(e.getPos().getCenter())));
        if (!h.isEmpty() && sp.distanceToSqr(h.get(0)) <= (Conversaciones.ALCANCE + 1) * (Conversaciones.ALCANCE + 1)) Conversaciones.hablar(sp, h.get(0));
        else Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.forja.sin_herrero", 0));
    }
}
