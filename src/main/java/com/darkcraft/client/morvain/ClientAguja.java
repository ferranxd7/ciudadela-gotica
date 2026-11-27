package com.darkcraft.client.morvain;

import com.darkcraft.morvain.Aguja;
import com.darkcraft.morvain.Homenaje;
import com.darkcraft.morvain.Morvain;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * La puerta de la aguja en el cliente (parte 7ac): si se tiene delante (cerrada, a mano y mirandola), el aviso de "Pulsa [E] para poner
 * la mano en la puerta" (la tecla la lleva {@code client.ui.ClientRol}, que manda {@code Net.PuertaAguja}).
 */
public final class ClientAguja {
    private ClientAguja() {}

    /** Si ahora se puede poner la mano en la puerta. */
    public static boolean aMano;

    /** Lo llama {@code ClientRol} en cada tick del cliente. */
    public static void tick(Minecraft mc) {
        aMano = false;
        if (mc.level == null || mc.player == null || mc.level.dimension() != Morvain.NIVEL || !mc.player.isAlive() || mc.player.isSpectator()) return;
        Vec3 c = Aguja.puerta();
        if (mc.player.distanceToSqr(c) > Aguja.MANO * Aguja.MANO) return;
        // cerrada: la cara de fuera del sello sigue ahi
        BlockPos q = BlockPos.containing(0.5, Homenaje.pisa(Homenaje.AZOTEA) + 1, c.z - 1.0);
        BlockState s = mc.level.getBlockState(q);
        if (s.isAir() || s.is(Blocks.BARRIER)) return;
        Vec3 a = c.subtract(mc.player.getEyePosition());
        double l = a.length();
        if (l > 1.6 && a.dot(mc.player.getViewVector(1f)) / l < 0.55) return;
        aMano = true;
    }
}
