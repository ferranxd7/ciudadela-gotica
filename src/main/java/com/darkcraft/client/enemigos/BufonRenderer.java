package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Bufon;
import com.darkcraft.enemigos.BufonAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Pell, el Bufon del Rey: el de los enemigos de siempre, a su escala (1,1); los ojos tras la mascara y las bolas de
 * fuego (la capa de brillo) laten un poco y al caer se apagan. Sus ilusiones son iguales pero algo transparentes y sin sombra (lo
 * que las delata si se mira bien).
 */
public class BufonRenderer extends EnemigoRenderer<Bufon, BufonModelo> {
    public static final float ESCALA = 1.1f;
    private final BufonModelo normal, copia;

    public BufonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BufonModelo(ctx.bakeLayer(BufonModelo.CAPA)), "bufon", 1, 0.55f, ESCALA);
        normal = model;
        copia = new BufonModelo(ctx.bakeLayer(BufonModelo.CAPA), true);
    }

    @Override
    public void render(Bufon e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.isInvisible()) return;
        float t = e.tickCount + pt;
        if (e.ilusion()) {
            copia.alfa = 0.80f + 0.06f * Mth.sin(t * 0.3f);
            model = copia;
            shadowRadius = 0f;
            brillo = 0.85f;
            try {
                super.render(e, yaw, pt, ps, buf, luz);
            } finally {
                model = normal;
                shadowRadius = 0.55f;
                brillo = 1f;
            }
            return;
        }
        model = normal;
        shadowRadius = 0.55f;
        if (e.accionVista() == BufonAnims.MORIR || e.isDeadOrDying()) {
            float k = (t - e.accionDesde) / (float) BufonAnims.TICKS[BufonAnims.MORIR];
            brillo = Mth.clamp(1f - (k - 0.6f) / 0.6f, 0f, 1f);
        } else brillo = 0.8f + 0.2f * Mth.sin(t * 0.12f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
