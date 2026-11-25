package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

/**
 * Lo comun de los modelos de los enemigos de tools/enemigos: piezas con nombre, las que se esconden en cada variante
 * (el sombrero, el arma que no lleva...), y poner una animacion con un peso (para pasar de tranquilo a en guardia sin
 * salto). Cada enemigo dice que bucles y que acciones se ponen ({@link #animar}).
 */
public abstract class EnemigoModelo<T extends Enemigo> extends HierarchicalModel<T> {
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart root;
    protected final ModelPart cabeza;
    private final ModelPart[][] ocultas;
    private final ModelPart[] todasLasOcultas;

    protected EnemigoModelo(ModelPart root, String[]... ocultasPorVariante) {
        this.root = root;
        this.cabeza = pieza("cabeza");
        this.ocultas = new ModelPart[ocultasPorVariante.length][];
        java.util.Set<ModelPart> todas = new java.util.LinkedHashSet<>();
        for (int v = 0; v < ocultasPorVariante.length; v++) {
            ocultas[v] = new ModelPart[ocultasPorVariante[v].length];
            for (int i = 0; i < ocultas[v].length; i++) todas.add(ocultas[v][i] = pieza(ocultasPorVariante[v][i]));
        }
        this.todasLasOcultas = todas.toArray(new ModelPart[0]);
    }

    protected ModelPart pieza(String nombre) {
        return getAnyDescendantWithName(nombre).orElseThrow(() -> new IllegalStateException("el modelo no tiene la pieza " + nombre));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    /** Pone una animacion con su peso (0 = nada, 1 = entera). */
    protected void poner(AnimationState s, AnimationDefinition d, float edad, float peso) {
        if (peso <= 0.001f) return;
        s.updateTime(edad, 1f);
        s.ifStarted(st -> KeyframeAnimations.animate(this, d, st.getAccumulatedTime(), peso, CACHE));
    }

    @Override
    public void setupAnim(T e, float paso, float cuanto, float edad, float giroCabeza, float inclinacion) {
        root.getAllParts().forEach(ModelPart::resetPose);
        for (ModelPart p : todasLasOcultas) p.visible = true;
        int v = e.variante();
        if (v >= 0 && v < ocultas.length) for (ModelPart p : ocultas[v]) p.visible = false;
        // (un muneco del bestiario no hace tick: su reloj se arranca aqui)
        e.reloj.startIfStopped(e.tickCount);
        float parcial = edad - e.tickCount;
        float guardia = Mth.lerp(Mth.clamp(parcial, 0f, 1f), e.guardiaO, e.guardia);
        if (animar(e, paso, cuanto, edad, guardia)) {
            cabeza.yRot += Mth.clamp(giroCabeza, -50f, 50f) * Mth.DEG_TO_RAD * 0.8f;
            cabeza.xRot += Mth.clamp(inclinacion, -30f, 30f) * Mth.DEG_TO_RAD * 0.6f;
        }
    }

    /** Los bucles y las acciones de este enemigo. Devuelve si la cabeza puede seguir con la mirada a quien mire. */
    protected abstract boolean animar(T e, float paso, float cuanto, float edad, float guardia);
}
