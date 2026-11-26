package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * El mundo de nubes en el cliente (parte 3a): con la camara dentro de una nube (cayendo a traves de ella, o volando en
 * creativo) no se ven las caras de los bloques por dentro sino una niebla blanca muy cerrada, como dentro de una nube de
 * verdad. Y las nubes de Minecraft, por encima de las islas ({@link Efectos}).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientNubes {
    private ClientNubes() {}

    /** Los efectos del mundo de nubes (lo que pide {@code data/minecraft/dimension_type/overworld.json}). */
    public static final net.minecraft.resources.ResourceLocation EFECTOS = new net.minecraft.resources.ResourceLocation(DarkCraftMod.MODID, "nubes");

    /**
     * Como los del mundo normal, pero con las nubes de Minecraft muy arriba (a 640, por encima de la copa de la
     * torre-arbol): a su altura de siempre (192) quedaban escondidas debajo del mar de nubes; asi hay cielo con nubes
     * sobre las islas (y los shaders, que las ponen a esa altura, tambien las dejan arriba).
     */
    public static final class Efectos extends net.minecraft.client.renderer.DimensionSpecialEffects {
        public Efectos() {
            super(640f, true, SkyType.NORMAL, false, false);
        }

        @Override
        public net.minecraft.world.phys.Vec3 getBrightnessDependentFogColor(net.minecraft.world.phys.Vec3 c, float b) {
            return c.multiply(b * 0.94F + 0.06F, b * 0.94F + 0.06F, b * 0.91F + 0.09F);
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false;
        }

        /** Dentro de la llamada de abajo (las dibuja Minecraft). */
        private boolean dibujando;

        /**
         * Con el cielo rojo (parte 5e) las nubes de Minecraft salen rojas: se le pide a Minecraft que las dibuje con ese color
         * (Minecraft no lo pone ni lo quita al dibujarlas) y luego se deja el color como estaba.
         */
        @Override
        public boolean renderClouds(net.minecraft.client.multiplayer.ClientLevel level, int ticks, float pt, com.mojang.blaze3d.vertex.PoseStack ps,
                                    double x, double y, double z, org.joml.Matrix4f proj) {
            if (dibujando || com.darkcraft.client.fortaleza.CieloRojoCliente.k(pt) <= 0) return false;
            dibujando = true;
            try {
                com.darkcraft.client.fortaleza.CieloRojoCliente.tenirNubes(pt);
                net.minecraft.client.Minecraft.getInstance().levelRenderer.renderClouds(ps, proj, pt, x, y, z);
            } finally {
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                dibujando = false;
            }
            return true;
        }

        /** Y la luz del cielo tira a rojo. */
        @Override
        public void adjustLightmapColors(net.minecraft.client.multiplayer.ClientLevel level, float pt, float skyDarken, float blockFlicker,
                                         float skyLight, int px, int py, org.joml.Vector3f colors) {
            com.darkcraft.client.fortaleza.CieloRojoCliente.luz(pt, py, colors);
        }
    }

    private static boolean dentro(Camera c) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || c == null) return false;
        return mc.level.getBlockState(BlockPos.containing(c.getPosition())).is(DarkCraftMod.NUBE.get());
    }

    @SubscribeEvent
    public static void onNiebla(ViewportEvent.RenderFog e) {
        if (!dentro(e.getCamera())) return;
        e.setNearPlaneDistance(0f);
        e.setFarPlaneDistance(6f);
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onColor(ViewportEvent.ComputeFogColor e) {
        if (!dentro(e.getCamera())) return;
        e.setRed(0.84f);
        e.setGreen(0.83f);
        e.setBlue(0.89f);
    }
}
