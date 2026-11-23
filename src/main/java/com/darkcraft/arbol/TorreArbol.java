package com.darkcraft.arbol;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Shot;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.TreeTower;
import com.darkcraft.net.Net;
import com.darkcraft.rey.ReyEstado;
import com.darkcraft.rey.Sello;
import com.darkcraft.story.Story;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * La torre del arbol en el mundo: pone (y vuelve a poner) el ascensor y la puerta de la Sala del Arbol
 * cuando hay alguien cerca, abre la puerta con la llave (con su mini cinematica) y no deja bajarse del
 * ascensor a medio viaje.
 */
public final class TorreArbol {
    private TorreArbol() {}

    /** Pone el ascensor y la puerta si faltan (y quita los repetidos). p = el jugador que esta cerca. */
    public static void asegurar(ServerLevel ow, CastlePlan plan, ServerPlayer p) {
        int[] h = plan.treeHall();
        if (h == null) return;
        int cx = h[0], cz = h[1], F = h[2], base = h[4];
        boolean abierta = ReyEstado.get(ow.getServer()).selloAbierto(Sello.key(plan));
        // ---- la puerta
        BlockPos dp = new BlockPos(cx, F, cz - 15);
        if (ow.isPositionEntityTicking(dp)) {
            List<PuertaArbol> ps = ow.getEntitiesOfClass(PuertaArbol.class, new AABB(dp).inflate(6));
            for (int i = 1; i < ps.size(); i++) ps.get(i).discard();
            if (ps.isEmpty()) {
                PuertaArbol d = DarkCraftMod.PUERTA_ARBOL.get().create(ow);
                if (d != null) {
                    d.moveTo(cx + 0.5, F, cz - 14 - PuertaArbol.GROSOR / 2, 0f, 0f);
                    if (abierta) d.ponerAbierta();
                    ow.addFreshEntity(d);
                    if (abierta) d.quitarBarrera(ow);
                }
            }
        }
        // ---- el ascensor
        BlockPos ap = new BlockPos(cx, base, cz + TreeTower.SHAFT_Z);
        if (ow.isPositionEntityTicking(ap)) {
            AABB col = new AABB(cx - 3, base - 3, cz + TreeTower.SHAFT_Z - 3, cx + 4, F + 4, cz + TreeTower.SHAFT_Z + 4);
            List<Ascensor> as = ow.getEntitiesOfClass(Ascensor.class, col);
            for (int i = 1; i < as.size(); i++) if (as.get(i).getPassengers().isEmpty()) as.get(i).discard();
            if (as.isEmpty()) {
                Ascensor a = DarkCraftMod.ASCENSOR.get().create(ow);
                if (a != null) {
                    boolean arriba = p != null && p.getY() > (base + F) / 2.0;
                    a.moveTo(cx + 0.5, arriba ? F : base, cz + TreeTower.SHAFT_Z + 0.5, 0f, 0f);
                    a.preparar(base, F, arriba);
                    ow.addFreshEntity(a);
                }
            }
        }
        // ---- Ser Gavran en su rama
        com.darkcraft.gavran.GavranEventos.asegurar(ow, plan);
    }

    /** La puerta cerrada mas cercana a este jugador (a menos de 8 bloques), o null. */
    public static PuertaArbol puertaCerca(ServerPlayer sp) {
        PuertaArbol best = null;
        double bd = 64;
        for (PuertaArbol d : sp.serverLevel().getEntitiesOfClass(PuertaArbol.class, sp.getBoundingBox().inflate(12))) {
            if (d.estado() != PuertaArbol.CERRADA) continue;
            double dd = sp.distanceToSqr(d.getX(), d.getY() + 2, d.getZ());
            if (dd < bd) {
                bd = dd;
                best = d;
            }
        }
        return best;
    }

    /** Usar la llave sin apuntar a la puerta (al aire o a un bloque de al lado). */
    public static boolean usarLlave(ServerPlayer sp, ItemStack llave) {
        PuertaArbol d = puertaCerca(sp);
        if (d == null) {
            sp.displayClientMessage(Component.translatable("item.darkcraft.llave_arbol.aqui_no"), true);
            return false;
        }
        abrir(d, sp, llave);
        return true;
    }

    /** Abre la puerta: gasta la llave, guarda que esta abierta, mini cinematica de 8 s y logro al final. */
    public static void abrir(PuertaArbol d, ServerPlayer sp, ItemStack llave) {
        if (d.estado() != PuertaArbol.CERRADA) return;
        ServerLevel sl = sp.serverLevel();
        CastlePlan plan = Story.planNear(sl, d.getBlockX(), d.getBlockZ());
        if (plan != null) ReyEstado.get(sl.getServer()).abrirSello(Sello.key(plan));
        if (!sp.isCreative()) llave.shrink(1);
        d.empezar(sl, sp);
        double[] k = d.cerradura();
        Net.Llave m = new Net.Llave(k[0], k[1], k[2], 70);
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(k[0], k[1], k[2]) < 60 * 60) Net.to(p, m);
        Cinematics.playShots(sp, Cinematics.SELLO, planos(d), 8.4f);
        // el logro lo da la propia puerta al terminar de abrirse (PuertaArbol.tick)
        DarkCraftMod.LOGGER.info("[DarkCraft] {} abre la puerta de la Sala del Arbol en {}", sp.getGameProfile().getName(), d.blockPosition());
    }

    /** Planos: la llave en la cerradura, las runas y los cerrojos, y las hojas abriendose hacia la sala. */
    private static List<Shot> planos(PuertaArbol d) {
        double x = d.getX(), y = d.getY(), z = d.getZ() + PuertaArbol.GROSOR / 2;
        double[] k = d.cerradura();
        Shot a = new Shot(0f, 2.6f, 48, 0, 1,
                new double[]{k[0] + 1.1, k[1] + 0.35, z + 2.1, k[0] + 0.55, k[1] + 0.2, z + 1.5},
                new double[]{k[0], k[1], k[2], k[0], k[1] - 0.05, k[2]});
        Shot b = new Shot(2.6f, 4.6f, 62, 0, 1,
                new double[]{x + 3.4, y + 2.6, z + 6.5, x + 2.2, y + 3.4, z + 6.0},
                new double[]{x, y + 5.0, z, x - 0.4, y + 5.2, z});
        Shot c = new Shot(4.6f, 8.4f, 70, 0, 2,
                new double[]{x, y + 2.4, z + 11.5, x, y + 2.8, z + 7.0, x, y + 3.3, z + 2.5},
                new double[]{x, y + 4.6, z - 8, x, y + 4.6, z - 16, x, y + 4.2, z - 22});
        return List.of(a, b, c);
    }

    // ------------------------------------------------------------------ comandos de prueba: /darkcraft valdrenn arbol ...

    /** /darkcraft valdrenn arbol ... (lo registra Comandos). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("arbol")
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> ir(c.getSource(), false)))
                .then(net.minecraft.commands.Commands.literal("arriba").executes(c -> ir(c.getSource(), true)))
                .then(net.minecraft.commands.Commands.literal("llave").executes(c -> llave(c.getSource())))
                .then(net.minecraft.commands.Commands.literal("cerrar").executes(c -> cerrar(c.getSource())));
    }

    private static int[] torreDe(net.minecraft.commands.CommandSourceStack src) {
        ServerLevel ow = src.getServer().overworld();
        var p = src.getPosition();
        CastlePlan plan = Story.planNear(ow, (int) p.x, (int) p.z);
        if (plan == null) {
            int[] s = com.darkcraft.world.CastleSites.nearest(ow.getSeed(), (int) p.x, (int) p.z, 3);
            if (s == null) return null;
            plan = com.darkcraft.world.PlanCache.get(ow.getSeed(), s[0], s[1], (x, z) -> 64);
        }
        return plan == null ? null : plan.treeHall();
    }

    /** Abajo de la torre (delante del disco) o arriba, en la recepcion mirando a la puerta. */
    private static int ir(net.minecraft.commands.CommandSourceStack src, boolean arriba) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        int[] h = torreDe(src);
        if (h == null) {
            src.sendFailure(Component.translatable("darkcraft.cmd.no_reino"));
            return 0;
        }
        ServerLevel ow = src.getServer().overworld();
        if (arriba) p.teleportTo(ow, h[0] + 0.5, h[2], h[1] - 2.5, 180f, 0f);
        else p.teleportTo(ow, h[0] + 0.5, h[4], h[1] + 10.5, 180f, 0f);
        return 1;
    }

    private static int llave(net.minecraft.commands.CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        p.getInventory().add(new ItemStack(DarkCraftMod.LLAVE_ARBOL.get()));
        return 1;
    }

    /** Vuelve a cerrar la puerta (para probar otra vez la llave). */
    private static int cerrar(net.minecraft.commands.CommandSourceStack src) {
        ServerLevel ow = src.getServer().overworld();
        var pos = src.getPosition();
        CastlePlan plan = Story.planNear(ow, (int) pos.x, (int) pos.z);
        if (plan == null || plan.treeHall() == null) {
            src.sendFailure(Component.translatable("darkcraft.cmd.no_reino"));
            return 0;
        }
        int[] h = plan.treeHall();
        ReyEstado.get(src.getServer()).cerrarSello(Sello.key(plan));
        PuertaArbol d = puerta(ow, h);
        if (d != null) d.discard();
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = 0; dy < 10; dy++) {
                BlockPos b = new BlockPos(h[0] + dx, h[2] + dy, h[1] - 15);
                if (ow.getBlockState(b).isAir()) ow.setBlock(b, net.minecraft.world.level.block.Blocks.BARRIER.defaultBlockState(), 3);
            }
        src.sendSuccess(() -> Component.literal("La puerta de la Sala del Arbol vuelve a estar cerrada"), true);
        return 1;
    }

    // ------------------------------------------------------------------ no bajarse a medio viaje

    @SubscribeEvent
    public static void onMount(EntityMountEvent e) {
        if (!e.isDismounting() || e.getLevel().isClientSide()) return;
        if (e.getEntityBeingMounted() instanceof Ascensor a && !a.dejaBajar()) e.setCanceled(true);
    }

    /** Al pasar por la tapa invisible de arriba del tubo, la cabeza atraviesa la barrera: sin ahogarse. */
    @SubscribeEvent
    public static void onAttack(net.minecraftforge.event.entity.living.LivingAttackEvent e) {
        if (e.getEntity().getVehicle() instanceof Ascensor && e.getSource().is(net.minecraft.world.damagesource.DamageTypes.IN_WALL))
            e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.getVehicle() instanceof Ascensor a) a.rescatar(sp);
    }

    /** Para las pruebas: el ascensor de esta torre (o null). */
    public static Ascensor ascensor(ServerLevel sl, int[] h) {
        AABB col = new AABB(h[0] - 3, h[4] - 3, h[1] + TreeTower.SHAFT_Z - 3, h[0] + 4, h[2] + 4, h[1] + TreeTower.SHAFT_Z + 4);
        List<Ascensor> as = sl.getEntitiesOfClass(Ascensor.class, col);
        return as.isEmpty() ? null : as.get(0);
    }

    /** Para las pruebas: la puerta de esta torre (o null). */
    public static PuertaArbol puerta(ServerLevel sl, int[] h) {
        List<PuertaArbol> ps = sl.getEntitiesOfClass(PuertaArbol.class, new AABB(new BlockPos(h[0], h[2], h[1] - 15)).inflate(6));
        return ps.isEmpty() ? null : ps.get(0);
    }

    static boolean esPasajero(Entity e) {
        return e.getVehicle() instanceof Ascensor;
    }
}
