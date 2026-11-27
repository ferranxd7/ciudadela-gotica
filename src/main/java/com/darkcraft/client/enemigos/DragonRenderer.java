package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Dragon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Vorthak: el de los enemigos de siempre a su escala (1,75: unos 21 bloques de largo). Volando, el cuerpo se
 * inclina con lo que sube o baja (el morro arriba al subir, abajo en el picado) y se ladea en los giros, alrededor de su centro.
 * Las grietas de brasa (la capa de brillo) laten despacio; al morir se van apagando hasta quedar en ceniza.
 */
public class DragonRenderer extends EnemigoRenderer<Dragon, DragonModelo> {
    public static final float ESCALA = 1.75f;
    /** A que altura (bloques) esta el centro del cuerpo: alrededor de el se inclina y se ladea. */
    public static final float CENTRO = 2.8f;

    public DragonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DragonModelo(ctx.bakeLayer(DragonModelo.CAPA)), "dragon", 1, 3.2f, ESCALA);
    }

    @Override
    protected void setupRotations(Dragon e, PoseStack ps, float edad, float giro, float pt) {
        super.setupRotations(e, ps, edad, giro, pt);
        float cabeceo = Mth.lerp(pt, e.cabeceoO, e.cabeceo), alabeo = Mth.lerp(pt, e.alabeoO, e.alabeo);
        if (Math.abs(cabeceo) < 0.01f && Math.abs(alabeo) < 0.01f) return;
        ps.translate(0, CENTRO, 0);
        ps.mulPose(Axis.XP.rotationDegrees(cabeceo));
        ps.mulPose(Axis.ZP.rotationDegrees(alabeo));
        ps.translate(0, -CENTRO, 0);
    }

    @Override
    public void render(Dragon e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float t = e.tickCount + pt;
        if (e.isDeadOrDying()) brillo = Mth.clamp(1f - (e.deathTime + pt - 50f) / 130f, 0f, 1f);
        else brillo = 0.82f + 0.18f * Mth.sin(t * 0.09f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
