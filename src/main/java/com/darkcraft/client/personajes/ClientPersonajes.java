package com.darkcraft.client.personajes;

import com.darkcraft.dialogo.Conversaciones;
import com.darkcraft.personajes.PersonajeBase;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/** Con que personaje se puede hablar ahora: el que se mira de cerca (para el aviso de "Pulsa [E] para hablar"). */
public final class ClientPersonajes {
    private ClientPersonajes() {}

    /** La entidad del personaje que se mira (o -1). */
    public static int aMano = -1;

    public static void tick(Minecraft mc) {
        aMano = -1;
        if (mc.player == null || mc.level == null || !mc.player.isAlive() || mc.player.isSpectator()) return;
        Vec3 ojo = mc.player.getEyePosition(), mira = mc.player.getLookAngle();
        double mejor = Math.cos(Math.toRadians(26));
        for (PersonajeBase g : mc.level.getEntitiesOfClass(PersonajeBase.class, mc.player.getBoundingBox().inflate(Conversaciones.ALCANCE + 1))) {
            // hacia el pecho (la guia, sentada: la cabeza le queda a 1,1 del suelo y el regazo a 0,4)
            Vec3 c = g.position().add(0, g.getBbHeight() * 0.58, 0).subtract(ojo);
            double d = c.length();
            if (d > Conversaciones.ALCANCE || d < 1e-3) continue;
            double cos = c.dot(mira) / d;
            // de muy cerca vale con mirar mas o menos hacia ella
            if (d < 1.8) cos += 0.08;
            if (cos > mejor) {
                mejor = cos;
                aMano = g.getId();
            }
        }
    }
}
