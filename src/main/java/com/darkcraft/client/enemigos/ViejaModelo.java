package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Vieja;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/**
 * La Vieja (parte 7r), el cachalote fantasma (modelo de {@code tools/enemigos/vieja.py}): nada siempre (la cola sube y baja, cada
 * trozo un poco despues); embistiendo, mas deprisa y con la boca abierta. Se dibuja medio transparente y luciendo
 * ({@code ViejaRenderer}).
 */
public class ViejaModelo extends HierarchicalModel<Vieja> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "vieja"), "main");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart root;

    public ViejaModelo(ModelPart root) {
        super(RenderType::entityTranslucentEmissive);
        this.root = root;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    private void bucle(AnimationDefinition d, float segundos, float peso) {
        if (peso <= 0.001f) return;
        KeyframeAnimations.animate(this, d, (long) (segundos * 1000f), peso, CACHE);
    }

    @Override
    public void setupAnim(Vieja v, float paso, float cuanto, float edad, float giroCabeza, float inclinacion) {
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean embiste = v.estado() == Vieja.SALE || v.estado() == Vieja.EMBISTE;
        // embistiendo nada el doble de deprisa
        bucle(ViejaAnimaciones.NADAR, (edad / 20f) * (embiste ? 1.9f : 0.8f), 1f);
        if (embiste) bucle(ViejaAnimaciones.BOCA, edad / 20f, 1f);
    }
}
