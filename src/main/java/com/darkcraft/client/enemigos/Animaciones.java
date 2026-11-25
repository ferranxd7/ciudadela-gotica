package com.darkcraft.client.enemigos;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Lee las animaciones que escribe tools/enemigos (los fotogramas clave van en cadenas, que ocupan mucho menos que el
 * codigo de crearlos uno a uno): "duracion;bucle;pieza:R|P:L|C:t x y z/t x y z/...;...". R = giro (grados), P =
 * posicion (pixeles, la y hacia arriba); L = lineal, C = Catmull-Rom.
 */
public final class Animaciones {
    private Animaciones() {}

    public static AnimationDefinition leer(String s) {
        String[] p = s.split(";");
        AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(Float.parseFloat(p[0]));
        if (p[1].equals("1")) b.looping();
        for (int i = 2; i < p.length; i++) {
            String[] c = p[i].split(":");
            boolean rot = c[1].equals("R");
            AnimationChannel.Interpolation in = c[2].equals("L") ? AnimationChannel.Interpolations.LINEAR : AnimationChannel.Interpolations.CATMULLROM;
            String[] ks = c[3].split("/");
            Keyframe[] kf = new Keyframe[ks.length];
            for (int k = 0; k < ks.length; k++) {
                String[] v = ks[k].split(" ");
                float t = Float.parseFloat(v[0]), x = Float.parseFloat(v[1]), y = Float.parseFloat(v[2]), z = Float.parseFloat(v[3]);
                kf[k] = new Keyframe(t, rot ? KeyframeAnimations.degreeVec(x, y, z) : KeyframeAnimations.posVec(x, y, z), in);
            }
            b.addAnimation(c[0], new AnimationChannel(rot ? AnimationChannel.Targets.ROTATION : AnimationChannel.Targets.POSITION, kf));
        }
        return b.build();
    }
}
