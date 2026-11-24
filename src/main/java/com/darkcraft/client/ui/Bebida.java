package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Frascos;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * Beber de un frasco, lo que se ve (los frascos no son objetos del inventario: los lleva la ficha). Mientras alguien
 * bebe, el brazo del arma sube a la boca con el frasco en la mano en vez de lo que llevaba (lo ven todos, tambien con
 * shaders: se hace con el modelo del jugador de siempre), en primera persona el frasco sube al centro y se bebe, y el que
 * bebe anda despacio y no ataca. Si no puede beber (frasco vacio o no le hace falta), el frasco del HUD se sacude.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class Bebida {
    private Bebida() {}

    private record Trago(int tipo, int inicio) {}

    /** Quien esta bebiendo (por id de entidad). */
    private static final Map<Integer, Trago> BEBEN = new HashMap<>();
    /** El frasco del HUD que se sacude (no se pudo beber) y por que. */
    static int sacudeTick = -1000, sacudeTipo, sacudeQue;
    /** El ultimo trago del jugador (su frasco del HUD da un destello). */
    static int tragoTick = -1000, tragoTipo;

    private static ItemStack frascoVida, frascoMana;

    public static void recibir(Net.Bebe m) {
        Minecraft mc = Minecraft.getInstance();
        if (m.que() == Net.Bebe.BEBE) {
            BEBEN.put(m.entity(), new Trago(m.tipo(), ClientRol.ticks));
            if (mc.player != null && mc.player.getId() == m.entity()) {
                tragoTick = ClientRol.ticks;
                tragoTipo = m.tipo();
            }
        } else {
            sacudeTick = ClientRol.ticks;
            sacudeTipo = m.tipo();
            sacudeQue = m.que();
            Panel.nada();
        }
    }

    /** Lo llama {@link ClientRol} en cada tick del cliente. */
    static void tick(Minecraft mc) {
        if (!BEBEN.isEmpty()) BEBEN.values().removeIf(t -> ClientRol.ticks - t.inicio() >= Frascos.DURA || ClientRol.ticks < t.inicio());
        restaurar();
    }

    static void olvidar() {
        BEBEN.clear();
        restaurar();
        sacudeTick = tragoTick = -1000;
    }

    /** Si el jugador de este cliente esta bebiendo. */
    public static boolean yo() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && BEBEN.containsKey(mc.player.getId());
    }

    public static boolean bebe(Entity e) {
        return BEBEN.containsKey(e.getId());
    }

    /** Cuanto tiene el frasco levantado (0 abajo, 1 en la boca): sube en 6 ticks, se bebe y baja en los 6 ultimos. */
    private static float alzado(Trago t, float pt) {
        float e = ClientRol.ticks - t.inicio() + pt;
        float sube = Mth.clamp(e / 6f, 0f, 1f), baja = Mth.clamp((Frascos.DURA - e) / 6f, 0f, 1f);
        float k = Math.min(sube, baja);
        return k * k * (3f - 2f * k);
    }

    private static ItemStack frasco(int tipo) {
        if (frascoVida == null) {
            frascoVida = new ItemStack(DarkCraftMod.FRASCO_VIDA.get());
            frascoMana = new ItemStack(DarkCraftMod.FRASCO_MANA.get());
        }
        return tipo == Frascos.MANA ? frascoMana : frascoVida;
    }

    // ------------------------------------------------------------------ en tercera persona (lo ven todos)

    /** El brazo sube a la boca (y la cabeza se echa un poco atras mientras bebe). */
    private static final HumanoidModel.ArmPose BEBER = HumanoidModel.ArmPose.create("DARKCRAFT_BEBER", false, (model, e, arm) -> {
        Trago t = BEBEN.get(e.getId());
        if (t == null) return;
        float pt = Minecraft.getInstance().getFrameTime();
        float k = alzado(t, pt);
        float edad = ClientRol.ticks - t.inicio() + pt;
        boolean derecha = arm == HumanoidArm.RIGHT;
        ModelPart brazo = derecha ? model.rightArm : model.leftArm;
        // traga: un vaiven pequeno mientras el frasco esta arriba
        float traga = k > 0.95f ? Mth.sin(edad * 1.3f) * 0.05f : 0f;
        // como quien toca un cuerno: la mano delante de la boca (el brazo no se dobla), siguiendo a la cabeza
        brazo.xRot = Mth.lerp(k, brazo.xRot, -1.52f + Mth.clamp(model.head.xRot, -1.0f, 1.0f) * 0.6f + traga);
        brazo.yRot = Mth.lerp(k, brazo.yRot, (derecha ? -0.56f : 0.56f) + model.head.yRot * 0.7f);
        brazo.zRot = Mth.lerp(k, brazo.zRot, 0f);
        model.head.xRot -= 0.24f * k;
    });

    private static ItemStack guardado;
    private static Player guardadoDe;
    private static int guardadoHueco;

    /** Devuelve a su sitio lo que el jugador llevaba en la mano (se cambia por el frasco solo mientras se le dibuja). */
    private static void restaurar() {
        if (guardadoDe == null) return;
        guardadoDe.getInventory().items.set(guardadoHueco, guardado);
        guardadoDe = null;
        guardado = null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void antes(RenderPlayerEvent.Pre e) {
        restaurar();
        Player p = e.getEntity();
        Trago t = BEBEN.get(p.getId());
        if (t == null || e.isCanceled()) return;
        Inventory inv = p.getInventory();
        if (!Inventory.isHotbarSlot(inv.selected)) return;
        guardadoDe = p;
        guardadoHueco = inv.selected;
        guardado = inv.items.get(inv.selected);
        inv.items.set(inv.selected, frasco(t.tipo()));
        PlayerModel<AbstractClientPlayer> m = e.getRenderer().getModel();
        if (p.getMainArm() == HumanoidArm.RIGHT) m.rightArmPose = BEBER;
        else m.leftArmPose = BEBER;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void despues(RenderPlayerEvent.Post e) {
        restaurar();
    }

    // ------------------------------------------------------------------ en primera persona

    @SubscribeEvent
    public static void mano(RenderHandEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || e.getHand() != InteractionHand.MAIN_HAND) return;
        Trago t = BEBEN.get(mc.player.getId());
        if (t == null) return;
        e.setCanceled(true);
        float pt = e.getPartialTick();
        float k = alzado(t, pt), edad = ClientRol.ticks - t.inicio() + pt;
        boolean derecha = mc.player.getMainArm() == HumanoidArm.RIGHT;
        int i = derecha ? 1 : -1;
        PoseStack ps = e.getPoseStack();
        ps.pushPose();
        // como el gesto de beber del juego: el objeto viene al centro, girado hacia la cara, y sube y baja con cada trago
        if (k > 0.9f) ps.translate(0f, Mth.abs(Mth.cos(edad / 4f * Mth.PI) * 0.1f), 0f);
        ps.translate(k * 0.6f * i, k * -0.5f - (1f - k) * 0.35f, 0f);
        ps.mulPose(Axis.YP.rotationDegrees(i * k * 90f));
        ps.mulPose(Axis.XP.rotationDegrees(k * 10f));
        ps.mulPose(Axis.ZP.rotationDegrees(i * k * 30f));
        ps.translate(i * 0.56f, -0.52f, -0.72f);
        mc.gameRenderer.itemInHandRenderer.renderItem(mc.player, frasco(t.tipo()),
                derecha ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !derecha, ps,
                e.getMultiBufferSource(), e.getPackedLight());
        ps.popPose();
    }

    // ------------------------------------------------------------------ el que bebe anda despacio y no ataca

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent e) {
        if (!yo() || ClientCine.active()) return;
        Input in = e.getInput();
        in.forwardImpulse *= 0.4f;
        in.leftImpulse *= 0.4f;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isSprinting()) mc.player.setSprinting(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onClic(InputEvent.InteractionKeyMappingTriggered e) {
        if (!yo() || e.isPickBlock()) return;
        e.setCanceled(true);
        e.setSwingHand(false);
    }
}
