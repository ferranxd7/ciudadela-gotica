package com.darkcraft.caballeros;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.TreeTower;
import com.darkcraft.story.Logros;
import com.darkcraft.world.CastleSites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Donde salen los caballeros zombi (grupos de 2 a 4, por la noche eterna de Valdrenn: el castillo, la torre-arbol
 * menos la sala de Ser Gavran, el pueblo, la catedral, las murallas y los patios; y en la Fortaleza de Hueso), y lo
 * que pasa al vencerlos (bestiario y logros).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Caballeros {
    private Caballeros() {}

    /** Cuantos puede haber cerca de un jugador y cada cuanto se intenta (ticks). */
    static final int TOPE = 8, CADA = 400;

    /** Zonas donde salen ademas de Valdrenn (la Fortaleza de Hueso las anade). */
    public static final List<Predicate<ServerPlayer>> ZONAS = new ArrayList<>();

    /** El escudo rojo de la guardia del Rey con una calavera negra. */
    public static ItemStack escudo() {
        ItemStack s = new ItemStack(Items.SHIELD);
        CompoundTag be = new CompoundTag();
        be.putInt("Base", DyeColor.RED.getId());
        ListTag pats = new ListTag();
        CompoundTag p = new CompoundTag();
        p.putString("Pattern", "sku");
        p.putInt("Color", DyeColor.BLACK.getId());
        pats.add(p);
        CompoundTag p2 = new CompoundTag();
        p2.putString("Pattern", "bo");
        p2.putInt("Color", DyeColor.BLACK.getId());
        pats.add(p2);
        be.put("Patterns", pats);
        s.getOrCreateTag().put("BlockEntityTag", be);
        return s;
    }

    // ------------------------------------------------------------------ zonas

    /** Si el jugador esta en un sitio donde salen caballeros. */
    static boolean enZona(ServerPlayer p) {
        if (p.level().dimension() != Level.OVERWORLD) return false;
        for (Predicate<ServerPlayer> z : ZONAS) if (z.test(p)) return true;
        return enValdrenn(p.serverLevel(), p.getX(), p.getY(), p.getZ());
    }

    /** Dentro de las murallas de Valdrenn, en la isla, y fuera de las salas de los jefes. */
    public static boolean enValdrenn(ServerLevel lvl, double x, double y, double z) {
        int[] s = CastleSites.nearest(lvl.getSeed(), (int) x, (int) z, 1);
        if (s == null) return false;
        double d = Math.hypot(s[0] - x, s[1] - z);
        if (d > CastlePlan.OCT + 25 || y < CastlePlan.Y0 - 30) return false;
        CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
        int[] h = plan.treeHall();
        if (h != null && Math.hypot(h[0] + 0.5 - x, h[1] + 0.5 - z) < TreeTower.HALL_R + 5 && y > h[2] - 6) return false;
        var sala = com.darkcraft.rey.Sala.near(lvl, (int) x, (int) z);
        return sala == null || !sala.box().inflate(4).contains(x, y, z);
    }

    // ------------------------------------------------------------------ aparecer

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % CADA != 123 || p.isSpectator() || p.isCreative()) return;
        ServerLevel lvl = p.serverLevel();
        if (lvl.getDifficulty() == Difficulty.PEACEFUL || !lvl.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING)) return;
        if (com.darkcraft.cine.Cinematics.active(p) || com.darkcraft.rey.ReyEventos.luchaDe(p) != null) return;
        if (!enZona(p) || p.getRandom().nextFloat() > 0.7f) return;
        int cerca = lvl.getEntitiesOfClass(CaballeroZombi.class, new AABB(p.blockPosition()).inflate(48)).size();
        if (cerca >= TOPE) return;
        grupo(lvl, p, Math.min(TOPE - cerca, 2 + p.getRandom().nextInt(3)));
    }

    /** Un grupo de n caballeros a 18-34 bloques del jugador, en el suelo y a oscuras. */
    public static int grupo(ServerLevel lvl, ServerPlayer p, int n) {
        var r = p.getRandom();
        for (int intento = 0; intento < 12; intento++) {
            double a = r.nextDouble() * Math.PI * 2, d = 18 + r.nextDouble() * 16;
            BlockPos c = suelo(lvl, (int) Math.floor(p.getX() + Math.cos(a) * d), p.getBlockY(), (int) Math.floor(p.getZ() + Math.sin(a) * d));
            if (c == null || !enZona(p) || (!enValdrenn(lvl, c.getX(), c.getY(), c.getZ()) && ZONAS.stream().noneMatch(z -> z.test(p)))) continue;
            int hechos = 0;
            for (int i = 0; i < n * 3 && hechos < n; i++) {
                BlockPos q = suelo(lvl, c.getX() + r.nextInt(7) - 3, c.getY(), c.getZ() + r.nextInt(7) - 3);
                if (q == null) continue;
                EntityType<? extends CaballeroZombi> t = switch (r.nextInt(5)) {
                    case 0, 1 -> DarkCraftMod.CABALLERO_ESPADA.get();
                    case 2, 3 -> DarkCraftMod.CABALLERO_ALABARDA.get();
                    default -> DarkCraftMod.CABALLERO_BALLESTA.get();
                };
                CaballeroZombi z = t.create(lvl);
                if (z == null) continue;
                z.moveTo(q.getX() + 0.5, q.getY(), q.getZ() + 0.5, r.nextFloat() * 360f, 0);
                if (!lvl.noCollision(z)) continue;
                z.finalizeSpawn(lvl, lvl.getCurrentDifficultyAt(q), MobSpawnType.EVENT, null, null);
                lvl.addFreshEntity(z);
                hechos++;
            }
            if (hechos > 0) return hechos;
        }
        return 0;
    }

    /** Un sitio para ponerse de pie cerca de (x, y, z): suelo firme, dos de aire y poca luz. */
    static BlockPos suelo(ServerLevel lvl, int x, int y0, int z) {
        if (!lvl.hasChunkAt(new BlockPos(x, y0, z))) return null;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = 8; dy >= -14; dy--) {
            int y = y0 + dy;
            BlockState pie = lvl.getBlockState(m.set(x, y - 1, z));
            if (!pie.isFaceSturdy(lvl, m, Direction.UP)) continue;
            if (!lvl.getBlockState(m.set(x, y, z)).getCollisionShape(lvl, m).isEmpty()) continue;
            if (!lvl.getBlockState(m.set(x, y + 1, z)).getCollisionShape(lvl, m).isEmpty()) continue;
            if (!lvl.getFluidState(m.set(x, y, z)).isEmpty()) continue;
            if (lvl.getBrightness(LightLayer.BLOCK, m.set(x, y, z)) > 9) continue;
            return new BlockPos(x, y, z);
        }
        return null;
    }

    // ------------------------------------------------------------------ escudo y muertes

    @SubscribeEvent
    public static void onEscudo(ShieldBlockEvent e) {
        if (e.getEntity() instanceof CaballeroZombi c) c.sonarEscudo();
    }

    @SubscribeEvent
    public static void onMuerte(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof CaballeroZombi c) || c.level().isClientSide()) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer p)) return;
        com.darkcraft.espejo.Espejo.bestia(p, c.tipo());
        Logros.dar(p, "caballero");
        CompoundTag d = datos(p);
        int n = d.getInt("caballeros") + 1;
        d.putInt("caballeros", n);
        if (n >= 50) Logros.dar(p, "caballeros_50");
    }

    private static CompoundTag datos(Player p) {
        CompoundTag root = p.getPersistentData();
        CompoundTag pers = root.getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag mine = pers.getCompound("darkcraft");
        pers.put("darkcraft", mine);
        root.put(Player.PERSISTED_NBT_TAG, pers);
        return mine;
    }

    // ------------------------------------------------------------------ comandos (/darkcraft valdrenn caballeros ...)

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("caballeros")
                .then(net.minecraft.commands.Commands.literal("grupo").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    int n = grupo(p.serverLevel(), p, 3);
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("caballeros: " + n), false);
                    return n;
                }))
                .then(net.minecraft.commands.Commands.literal("quitar").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    var l = p.serverLevel().getEntitiesOfClass(CaballeroZombi.class, new AABB(p.blockPosition()).inflate(96));
                    l.forEach(net.minecraft.world.entity.Entity::discard);
                    return l.size();
                }));
    }
}
