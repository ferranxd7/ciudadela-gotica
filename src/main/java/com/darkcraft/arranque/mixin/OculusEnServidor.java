package com.darkcraft.arranque.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Vacio a proposito: solo en el servidor dedicado ({@code "server"} en {@code darkcraft.mixins.json}) y solo para que
 * Mixin le pase la clase de mod de Oculus a {@link com.darkcraft.arranque.Arranque#preApply}, que la deja sin nada
 * (Oculus es solo del cliente y va dentro del jar de Dark Craft). {@code @Pseudo}: si Oculus no esta, no pasa nada.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.Iris", remap = false)
public abstract class OculusEnServidor {
}
