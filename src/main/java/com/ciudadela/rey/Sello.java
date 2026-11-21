package com.ciudadela.rey;

import com.ciudadela.gen.CastlePlan;
import com.ciudadela.story.Story;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** La sala del arbol (la del segundo jefe): que no se pueda romper. */
public final class Sello {
    private Sello() {}

    public static boolean enSalaArbol(ServerLevel sl, BlockPos p) {
        CastlePlan plan = Story.planNear(sl, p.getX(), p.getZ());
        if (plan == null) return false;
        int[] h = plan.treeHall();
        if (h == null) return false;
        int dy = p.getY() - h[2];
        if (dy < -1 || dy > 40) return false;
        double dx = p.getX() + 0.5 - h[0], dz = p.getZ() + 0.5 - h[1];
        return dx * dx + dz * dz <= (h[3] + 1.5) * (h[3] + 1.5);
    }
}
