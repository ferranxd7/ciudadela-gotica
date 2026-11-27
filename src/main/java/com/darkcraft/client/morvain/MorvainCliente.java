package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lo que se ve en Morvain (parte 7a): siempre al caer la tarde (la hora la fija el tipo de dimension), sin las nubes de
 * Minecraft (la ciudadela y las cumbres asoman por encima de donde irian), el resplandor del ocaso dorado y algo rojo, y
 * nunca llueve (el tiempo de Morvain es el del mundo de las nubes: en el cliente se le quita). El color del cielo, de la
 * niebla y del agua (roja en la Tierra sin Fe y el crater) lo pone cada region con su bioma. Despues del final (parte 7ad) cambia
 * segun como acabo: {@link CieloFinal}.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class MorvainCliente {
    private MorvainCliente() {}

    public static final ResourceLocation EFECTOS = new ResourceLocation(DarkCraftMod.MODID, "morvain");

    public static final class Efectos extends DimensionSpecialEffects {
        private final float[] ocaso = new float[4];

        public Efectos() {
            super(Float.NaN, true, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 c, float b) {
            return c.multiply(b * 0.94F + 0.06F, b * 0.94F + 0.06F, b * 0.91F + 0.09F);
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }

        /** El resplandor del horizonte por donde se pone el sol: dorado, con un poco de rojo. */
        @Override
        public float[] getSunriseColor(float tiempo, float pt) {
            float c = Mth.cos(tiempo * ((float) Math.PI * 2F));
            if (c < -0.42F || c > 0.42F) return null;
            float f = (c + 0.42F) / 0.84F;
            float a = 1.0F - (1.0F - Mth.sin(f * (float) Math.PI)) * 0.99F;
            ocaso[0] = 0.86F + f * 0.1F;
            ocaso[1] = 0.48F + f * 0.22F;
            ocaso[2] = 0.16F + f * 0.06F;
            ocaso[3] = a * a * 0.92F;
            // despues del final (parte 7ad): rosa y dorado con el alba, apagado con la nueva Sombra
            return CieloFinal.ocaso(ocaso, pt);
        }

        /** La luz del cielo, despues del final (parte 7ad): mas viva con el alba, sin color con la nueva Sombra. */
        @Override
        public void adjustLightmapColors(net.minecraft.client.multiplayer.ClientLevel level, float pt, float skyDarken, float blockFlicker,
                                         float skyLight, int px, int py, org.joml.Vector3f colors) {
            CieloFinal.luz(pt, py, colors);
        }
    }

    /** En Morvain no llueve nunca (comparte el tiempo con el mundo de las nubes). */
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != com.darkcraft.morvain.Morvain.NIVEL) return;
        if (mc.level.getRainLevel(1f) > 0f) mc.level.setRainLevel(0f);
        if (mc.level.getThunderLevel(1f) > 0f) mc.level.setThunderLevel(0f);
    }
}
