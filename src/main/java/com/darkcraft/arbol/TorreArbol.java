package com.darkcraft.arbol;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Shot;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.TreeTower;
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
        llaveEnLaCerradura(sl, d.cerradura());
        Cinematics.playShots(sp, Cinematics.SELLO, planos(d), 8.4f);
        // el logro lo da la propia puerta al terminar de abrirse (PuertaArbol.tick)
        DarkCraftMod.LOGGER.info("[DarkCraft] {} abre la puerta de la Sala del Arbol en {}", sp.getGameProfile().getName(), d.blockPosition());
    }

    /**
     * La Llave del Arbol de verdad en la cerradura: una entidad (item_display), que se ve igual con y sin shaders (antes la
     * dibujaba el cliente por su cuenta: con shaders no salia, y ademas entraba por el mango y al girar quedaba de canto).
     * Entra de punta, gira un cuarto de vuelta sobre su eje, se queda mientras se encienden las runas y se descorren los
     * cerrojos, y se desvanece justo antes de que se abran las hojas.
     */
    private static void llaveEnLaCerradura(ServerLevel sl, double[] k) {
        net.minecraft.world.entity.Display.ItemDisplay d = net.minecraft.world.entity.EntityType.ITEM_DISPLAY.create(sl);
        if (d == null) return;
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.put("item", new ItemStack(DarkCraftMod.LLAVE_ARBOL.get()).save(new net.minecraft.nbt.CompoundTag()));
        tag.putString("item_display", "none");
        tag.put("transformation", matriz(llaveM(1.05f, 0f, 0.8f)));
        net.minecraft.nbt.CompoundTag luz = new net.minecraft.nbt.CompoundTag();
        luz.putInt("sky", 15);
        luz.putInt("block", 15);
        tag.put("brightness", luz);
        tag.putFloat("view_range", 2.0f);
        d.load(tag);
        d.moveTo(k[0], k[1], k[2], 0f, 0f);
        com.darkcraft.armas.EfectosRaiz.efimera(d);
        sl.addFreshEntity(d);
        float cuarto = (float) (Math.PI / 2);
        com.darkcraft.rey.Victoria.despues(2, () -> llaveA(d, llaveM(0.40f, 0f, 0.8f), 20));
        com.darkcraft.rey.Victoria.despues(26, () -> llaveA(d, llaveM(0.40f, cuarto, 0.8f), 14));
        com.darkcraft.rey.Victoria.despues(40, () -> sl.sendParticles(net.minecraft.core.particles.ParticleTypes.WAX_ON,
                k[0], k[1], k[2] + 0.15, 10, 0.18, 0.18, 0.05, 0.6));
        com.darkcraft.rey.Victoria.despues(PuertaArbol.T_HOJAS - 10, () -> llaveA(d, llaveM(0.40f, cuarto, 0.02f), 8));
        com.darkcraft.rey.Victoria.despues(PuertaArbol.T_HOJAS, d::discard);
    }

    /**
     * La llave con la punta hacia la puerta (-Z, el norte), los dientes hacia abajo, su centro a {@code d} bloques por
     * delante de la cerradura y girada {@code giro} radianes sobre su eje.
     * <p>En la textura la punta esta abajo a la derecha y los dientes hacia abajo a la izquierda, pero el juego dibuja
     * los objetos de las entidades item_display dados la vuelta (media vuelta sobre el eje vertical, antes de esta
     * matriz): la punta llega aqui abajo a la IZQUIERDA. Con tres octavos de vuelta queda tumbada hacia +X, el cuarto
     * de vuelta sobre Y la apunta al norte y la media vuelta final pone los dientes hacia abajo.
     */
    private static org.joml.Matrix4f llaveM(float d, float giro, float escala) {
        return new org.joml.Matrix4f().translate(0, 0, d).rotateZ(giro + (float) Math.PI).rotateY((float) (Math.PI / 2))
                .rotateZ((float) (Math.PI * 3 / 4)).scale(escala);
    }

    private static void llaveA(net.minecraft.world.entity.Display.ItemDisplay d, org.joml.Matrix4f m, int ticks) {
        if (!d.isAlive()) return;
        net.minecraft.nbt.CompoundTag t = d.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        t.put("transformation", matriz(m));
        t.putInt("interpolation_duration", ticks);
        t.putInt("start_interpolation", 0);
        d.load(t);
    }

    private static net.minecraft.nbt.ListTag matriz(org.joml.Matrix4f m) {
        net.minecraft.nbt.ListTag l = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < 16; i++) l.add(net.minecraft.nbt.FloatTag.valueOf(m.getRowColumn(i >> 2, i & 3)));
        return l;
    }

    /** Planos: la llave en la cerradura, las runas y los cerrojos, y las hojas abriendose hacia la sala. */
    private static List<Shot> planos(PuertaArbol d) {
        double x = d.getX(), y = d.getY(), z = d.getZ() + PuertaArbol.GROSOR / 2;
        double[] k = d.cerradura();
        // de lado y subiendo un poco mientras la llave gira: se le ve la cara plana antes y despues del giro
        Shot a = new Shot(0f, 2.6f, 48, 0, 1,
                new double[]{k[0] + 1.6, k[1] + 0.6, z + 2.0, k[0] + 1.15, k[1] + 1.0, z + 1.5},
                new double[]{k[0], k[1], k[2] + 0.5, k[0], k[1] - 0.02, k[2] + 0.3});
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
