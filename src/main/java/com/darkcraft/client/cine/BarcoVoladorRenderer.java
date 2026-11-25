package com.darkcraft.client.cine;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.BarcoVolador;
import com.darkcraft.cine.Viaje;
import com.darkcraft.pueblo.Garza;
import com.darkcraft.pueblo.Pueblo;
import com.darkcraft.pueblo.gen.Barco;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dibuja La Garza Gris volando: los bloques del barco entero ({@link Garza#plano}) con sus modelos de siempre, girados
 * alrededor del pie del palo. Las caras que quedan tapadas por otro bloque macizo del propio barco no se dibujan; lo que
 * da luz (los faroles) se dibuja encendido; y el ancla cuelga de su serviola como en el barco amarrado.
 */
public class BarcoVoladorRenderer extends EntityRenderer<BarcoVolador> {
    /** Un bloque del barco: donde va (respecto al pie del palo), sus caras a la vista y si luce. */
    private record Pieza(float x, float y, float z, List<BakedQuad> caras, boolean luce) {}

    private static List<Pieza> piezas;
    private static int caras;

    public BarcoVoladorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0f;
    }

    /** Las piezas se preparan una vez (y otra vez si se recargan los recursos: los modelos cambian). */
    public static void olvidar() {
        piezas = null;
    }

    public static int cuantas() {
        return piezas == null ? -1 : piezas.size();
    }

    public static int cuantasCaras() {
        return piezas == null ? -1 : caras;
    }

    private static List<Pieza> piezas() {
        if (piezas != null) return piezas;
        Map<BlockPos, BlockState> m = new HashMap<>();
        for (Garza.Celda c : Garza.plano(true)) m.put(new BlockPos(c.x(), c.y(), c.z()), c.estado());
        var modelos = Minecraft.getInstance().getBlockRenderer();
        RandomSource azar = RandomSource.create();
        List<Pieza> l = new ArrayList<>();
        int n = 0;
        for (Map.Entry<BlockPos, BlockState> e : m.entrySet()) {
            BlockState s = e.getValue();
            if (s.isAir() || s.getRenderShape() != RenderShape.MODEL) continue;
            BakedModel modelo = modelos.getBlockModel(s);
            List<BakedQuad> q = new ArrayList<>();
            for (Direction d : Direction.values()) {
                // una cara pegada a otro bloque macizo del barco no se ve
                BlockState v = m.get(e.getKey().relative(d));
                if (v != null && v.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) continue;
                azar.setSeed(42L);
                q.addAll(modelo.getQuads(s, d, azar, ModelData.EMPTY, null));
            }
            azar.setSeed(42L);
            q.addAll(modelo.getQuads(s, null, azar, ModelData.EMPTY, null));
            if (q.isEmpty()) continue;
            n += q.size();
            l.add(new Pieza((float) (e.getKey().getX() - Viaje.PX), (float) (e.getKey().getY() - Viaje.PY), (float) (e.getKey().getZ() - Viaje.PZ), q,
                    s.getLightEmission() > 0));
        }
        caras = n;
        piezas = l;
        DarkCraftMod.LOGGER.info("[DarkCraft] La Garza Gris que vuela: {} bloques, {} caras", l.size(), n);
        return l;
    }

    @Override
    public void render(BarcoVolador e, float giroEntidad, float pt, PoseStack pose, MultiBufferSource buffers, int luz) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(pt, e.yRotO, e.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(pt, e.cabeceoO, e.cabeceo)));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pt, e.balanceoO, e.balanceo)));
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        int encendido = LightTexture.pack(15, LightTexture.sky(luz));
        for (Pieza p : piezas()) {
            pose.pushPose();
            pose.translate(p.x(), p.y(), p.z());
            PoseStack.Pose ultimo = pose.last();
            int l = p.luce() ? encendido : luz;
            for (BakedQuad q : p.caras()) vc.putBulkData(ultimo, q, 1f, 1f, 1f, l, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        // el ancla, colgada de su serviola (como la entidad de dibujo del barco amarrado: de canto, dada la vuelta)
        int[] a = Barco.ancla();
        pose.pushPose();
        pose.translate(a[0] + 0.5 - Viaje.PX, a[1] - Pueblo.ANCLA_TAM * 0.47 - Viaje.PY, a[2] + 0.5 - Viaje.PZ);
        pose.mulPoseMatrix(new Matrix4f().rotateY((float) (Math.PI / 2)).scale(Pueblo.ANCLA_TAM));
        pose.mulPose(Axis.YP.rotation((float) Math.PI));
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(Pueblo.Pieza.ANCLA.item()), ItemDisplayContext.NONE,
                LightTexture.pack(Math.max(8, LightTexture.block(luz)), LightTexture.sky(luz)), OverlayTexture.NO_OVERLAY, pose, buffers, e.level(), 0);
        pose.popPose();
        pose.popPose();
        super.render(e, giroEntidad, pt, pose, buffers, luz);
    }

    /**
     * El barco vuela siempre a cielo abierto: toda la luz del cielo, este donde este. (Lejos del jugador, o muy alto, el
     * cliente a veces no tiene la luz de ese sitio y da 0: en build-100 el barco se veia negro al alejarse.)
     */
    @Override
    protected int getSkyLightLevel(BarcoVolador e, BlockPos p) {
        return 15;
    }

    /** Y algo de luz propia (sus faroles y el corazon): de noche no se queda en una sombra. */
    @Override
    protected int getBlockLightLevel(BarcoVolador e, BlockPos p) {
        return Math.max(6, super.getBlockLightLevel(e, p));
    }

    @Override
    public ResourceLocation getTextureLocation(BarcoVolador e) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
