package com.darkcraft.client.rey;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Formas de dibujo propias (avisos del suelo que brillan, sin tapar nada). */
public abstract class Tipos extends RenderType {
    private Tipos(String n, VertexFormat f, VertexFormat.Mode m, int b, boolean c, boolean s, Runnable a, Runnable z) {
        super(n, f, m, b, c, s, a, z);
    }

    /** Colores que se suman (brillan), por las dos caras y sin escribir profundidad. */
    public static final RenderType BRILLO = create("darkcraft_brillo", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false));
}
