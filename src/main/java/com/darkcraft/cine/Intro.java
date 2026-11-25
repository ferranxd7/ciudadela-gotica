package com.darkcraft.cine;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.pueblo.gen.Barco;
import com.darkcraft.world.NubesGenerador;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * La cinematica de inicio (parte 3d del plan grande): nada mas elegir clase, la Sombra te habla en lo oscuro ("estabas
 * muerto"), te arroja de vuelta al mundo (caes del cielo en primera persona sobre el muelle de Brumavela), te atrapa
 * antes de tocar el suelo, te marca ("hay una puerta que yo no puedo abrir") y despiertas tumbado en el muelle, junto
 * al barco roto.
 * <p>
 * Aqui estan los tiempos (los usan el servidor, para los planos, y el cliente, {@code client/Guion}, para la voz, los
 * efectos y lo que se pinta encima) y los dos planos de camara: la caida y el despertar. El resto del tiempo la pantalla
 * esta en negro. Las seis frases son de la Sombra ({@code sombra.voz.1..6}; lo que dura cada una, {@code Voces.sombra}).
 */
public final class Intro {
    private Intro() {}

    public static final int ID = 38;

    /** Cuando empieza cada frase de la Sombra (s); la 0 no existe. */
    public static final float[] VOZ = {0f, 1.0f, 4.4f, 9.5f, 14.0f, 23.0f, 29.4f};
    /**
     * La caida empieza con "...and I cast you back" (a los 2,15 s de la frase 3), la Sombra te atrapa justo antes del suelo,
     * la marca arde en lo negro, dos latidos y se abren los ojos; a los {@link #FIN} segundos la camara ya es la del jugador.
     */
    public static final float CAIDA = 11.6f, ATRAPA = 21.4f, MARCA = 22.2f, MARCA_FUERA = 31.6f, LATIDO = 32.4f, OJOS = 33.4f, FIN = 39.6f;
    /** Desde donde cae (bloques por encima de los pies) y a cuanto del suelo le atrapa la Sombra. */
    public static final double ALTO = 510, BAJO = 2.3;

    /** La hace empezar para ese jugador, donde este (mirando al barco roto si lo tiene cerca). */
    public static void empezar(ServerPlayer p) {
        if (Cinematics.active(p) || !p.isAlive()) return;
        float yaw = p.getYRot();
        if (NubesGenerador.es(p.serverLevel())) {
            double dx = Barco.BX + 0.5 - p.getX(), dz = Barco.PALO + 0.5 - p.getZ();
            if (dx * dx + dz * dz < 80 * 80) yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        }
        p.teleportTo(p.serverLevel(), p.getX(), p.getY(), p.getZ(), yaw, 4f);
        p.setDeltaMovement(Vec3.ZERO);
        Cinematics.playShots(p, ID, planos(p.position(), yaw), FIN + 0.6f);
        DarkCraftMod.LOGGER.info("[DarkCraft] Cinematica de inicio para {}", p.getGameProfile().getName());
    }

    /** Los dos planos: la caida sobre {@code pies} y el despertar ahi mismo, acabando en los ojos del jugador (mirando a {@code yaw}). */
    public static List<Shot> planos(Vec3 pies, float yaw) {
        List<Shot> l = new ArrayList<>();
        double x = pies.x, y = pies.y, z = pies.z;
        // la caida: recta hacia abajo, girando despacio; al principio se ve algo de horizonte, al final solo el suelo
        int n = 9;
        double[] pos = new double[n * 3], mira = new double[n * 3];
        for (int i = 0; i < n; i++) {
            double u = i / (double) (n - 1);
            double cy = y + ALTO + (BAJO - ALTO) * u;
            double a = Math.toRadians(yaw + 90 + 40 + 330 * u), r = 34 - 29 * u;
            pos[i * 3] = x;
            pos[i * 3 + 1] = cy;
            pos[i * 3 + 2] = z;
            mira[i * 3] = x + Math.cos(a) * r;
            mira[i * 3 + 1] = cy - 70;
            mira[i * 3 + 2] = z + Math.sin(a) * r;
        }
        l.add(new Shot(CAIDA, ATRAPA, 96f, 0f, 0, pos, mira));
        // el despertar: tumbado boca arriba, se incorpora mirando al barco y se pone en pie
        double fx = -Math.sin(Math.toRadians(yaw)), fz = Math.cos(Math.toRadians(yaw));
        double[] suelo = {x, y + 0.34, z}, sentado = {x + fx * 0.15, y + 0.98, z + fz * 0.15}, ojos = {x, y + 1.62, z};
        double[] cielo = {x + fx * 2.2, y + 8.5, z + fz * 2.2}, barco = {x + fx * 10, y + 2.4, z + fz * 10};
        // al final mira a donde mira el jugador (4 grados hacia abajo, como lo deja empezar())
        double[] frente = {x + fx * 10, y + 1.62 - 10 * Math.tan(Math.toRadians(4)), z + fz * 10};
        pos = junta(suelo, suelo, suelo, sentado, ojos, ojos);
        mira = junta(cielo, cielo, cielo, barco, frente, frente);
        l.add(new Shot(OJOS, FIN, 70f, 0f, 1, pos, mira));
        return l;
    }

    private static double[] junta(double[]... p) {
        double[] o = new double[p.length * 3];
        for (int i = 0; i < p.length; i++) System.arraycopy(p[i], 0, o, i * 3, 3);
        return o;
    }
}
