package com.darkcraft.pueblo.gen;

import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;

/** Una pieza pequena del pueblo escrita en el sitio (un pozo, una farola, una tumba): su caja y lo que pone. */
public final class Trozo extends Element {
    public interface Obra {
        void build(Canvas c);
    }

    private final Obra obra;

    public Trozo(int capa, int x0, int y0, int z0, int x1, int y1, int z1, Obra obra) {
        super(0);
        this.layer = capa;
        this.obra = obra;
        bounds(x0, y0, z0, x1, y1, z1);
    }

    @Override
    public void build(Canvas c) {
        obra.build(c);
    }
}
