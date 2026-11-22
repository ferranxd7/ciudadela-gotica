package com.darkcraft.rey;

import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.TreeTower;
import com.darkcraft.story.Story;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * La Sala del Arbol se abre con la Llave del Arbol (la guardaba el Rey): la puerta y el ascensor estan en
 * com.darkcraft.arbol. Aqui queda lo comun: la clave del estado guardado y que la sala y la torre no se
 * puedan romper.
 */
public final class Sello {
    private Sello() {}

    public static String key(CastlePlan plan) {
        return "sello@" + plan.cx + "," + plan.cz;
    }

    /** Dentro de la Sala del Arbol (con su cupula) o de la torre que sube hasta ella. */
    public static boolean enSalaArbol(ServerLevel sl, BlockPos p) {
        CastlePlan plan = Story.planNear(sl, p.getX(), p.getZ());
        if (plan == null) return false;
        int[] h = plan.treeHall();
        if (h == null) return false;
        double dx = p.getX() + 0.5 - h[0], dz = p.getZ() + 0.5 - h[1];
        int dy = p.getY() - h[2];
        if (dy >= -1 && dy <= TreeTower.HALL_H + 16 && dx * dx + dz * dz <= (h[3] + 1.5) * (h[3] + 1.5)) return true;
        // la torre: escalera, barandillas y el tubo del ascensor
        return p.getY() >= h[4] - 1 && p.getY() < h[2] && Math.abs(dx) <= 14 && Math.abs(dz) <= 14;
    }
}
