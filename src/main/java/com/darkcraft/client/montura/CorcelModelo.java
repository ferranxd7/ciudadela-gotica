package com.darkcraft.client.montura;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.enemigos.CorcelAnimaciones;
import com.darkcraft.enemigos.CorcelAnims;
import com.darkcraft.montura.Corcel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

/**
 * El corcel de hueso y niebla (modelo de {@code tools/enemigos/corcel.py}). Parado, su bucle de quieto; andando, el paso
 * y, segun lo que corre, el galope (mezclados, y cada ciclo al ritmo de lo que avanza: {@link Corcel#fasePaso},
 * {@link Corcel#faseGalope}); en el aire, el salto; y sus dos acciones, encabritarse y deshacerse, encima de todo.
 */
public class CorcelModelo extends HierarchicalModel<Corcel> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "corcel"), "main");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart root;
    private final ModelPart cabeza, cuello;

    public CorcelModelo(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.cabeza = getAnyDescendantWithName("cabeza").orElseThrow();
        this.cuello = getAnyDescendantWithName("cuello").orElseThrow();
    }

    @Override
    public ModelPart root() {
        return root;
    }

    /** Un bucle en el segundo que diga {@code segundos} (no el reloj del mundo: el de lo que avanza), con su peso. */
    private void bucle(AnimationDefinition d, float segundos, float peso) {
        if (peso <= 0.001f) return;
        KeyframeAnimations.animate(this, d, (long) (segundos * 1000f), peso, CACHE);
    }

    private void poner(AnimationState s, AnimationDefinition d, float edad, float peso) {
        if (peso <= 0.001f) return;
        s.updateTime(edad, 1f);
        s.ifStarted(st -> KeyframeAnimations.animate(this, d, st.getAccumulatedTime(), peso, CACHE));
    }

    @Override
    public void setupAnim(Corcel c, float paso, float cuanto, float edad, float giroCabeza, float inclinacion) {
        root.getAllParts().forEach(ModelPart::resetPose);
        c.reloj.startIfStopped(c.tickCount);
        float pt = Mth.clamp(edad - c.tickCount, 0f, 1f);
        int accion = c.accionVista();
        if (accion == 0) {
            float v = Mth.lerp(pt, c.velocidadO, c.velocidad);
            float aire = Mth.clamp((c.enAire - 2) / 3f, 0f, 1f);
            float mov = Mth.clamp(v / 0.05f, 0f, 1f) * (1f - aire);
            float galope = Mth.clamp((v - 0.28f) / 0.16f, 0f, 1f);
            poner(c.reloj, CorcelAnimaciones.QUIETO, edad, 1f - Math.max(mov, aire));
            bucle(CorcelAnimaciones.ANDAR, Mth.lerp(pt, c.fasePasoO, c.fasePaso), mov * (1f - galope));
            bucle(CorcelAnimaciones.CORRER, Mth.lerp(pt, c.faseGalopeO, c.faseGalope), mov * galope);
            poner(c.reloj, CorcelAnimaciones.SALTO, edad, aire);
            // sin jinete mira a quien tenga delante (solo un poco: el cuello largo hace el resto)
            if (!c.isVehicle() && mov < 0.5f) {
                float g = Mth.clamp(giroCabeza, -40f, 40f) * Mth.DEG_TO_RAD;
                cuello.yRot += g * 0.4f;
                cabeza.yRot += g * 0.35f;
                cabeza.xRot += Mth.clamp(inclinacion, -25f, 25f) * Mth.DEG_TO_RAD * 0.4f;
            }
        }
        for (int i = 1; i < CorcelAnimaciones.ACCIONES.length; i++)
            if (CorcelAnimaciones.ACCIONES[i] != null) poner(c.acciones[i], CorcelAnimaciones.ACCIONES[i], edad, 1f);
    }

    /** Lo que se ha deshecho (0..1) mientras hace {@link CorcelAnims#DESHACERSE}: el brillo se va apagando. */
    public static float deshecho(Corcel c, float pt) {
        if (c.accionVista() != CorcelAnims.DESHACERSE) return 0f;
        float t = c.acciones[CorcelAnims.DESHACERSE].getAccumulatedTime() / 50f + pt;
        return Mth.clamp(t / CorcelAnims.TICKS[CorcelAnims.DESHACERSE], 0f, 1f);
    }
}
