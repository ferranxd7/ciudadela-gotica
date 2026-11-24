package com.darkcraft.story;

import com.darkcraft.cine.Cinematics;
import com.darkcraft.gen.Canvas;
import com.darkcraft.world.CastleSites;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Cofres de la cronica: cada jugador saca su propia copia del libro (y el mapa en el primero), la
 * primera vez se ve la cinematica del capitulo y los cofres y carteles no se pueden romper.
 */
public final class StoryEvents {
    private StoryEvents() {}

    private static final Map<BlockPos, Integer> OPEN = new HashMap<>();

    public static int chapterAt(Level lvl, BlockPos pos) {
        BlockEntity be = lvl.getBlockEntity(pos);
        if (be == null) return 0;
        int ch = be.getPersistentData().getInt(Canvas.STORY_TAG);
        return ch >= 1 && ch <= Story.CHAPTERS ? ch : 0;
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e) {
        Level lvl = e.getLevel();
        if (lvl.isClientSide()) return;
        BlockPos pos = e.getPos();
        if (!(lvl.getBlockEntity(pos) instanceof ChestBlockEntity)) return;
        int ch = chapterAt(lvl, pos);
        if (ch == 0) return;
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);
        if (!(e.getEntity() instanceof ServerPlayer sp) || e.getHand() != InteractionHand.MAIN_HAND || Cinematics.active(sp)) return;
        StoryData data = StoryData.get((ServerLevel) lvl);
        if (data.items(pos, sp.getUUID()) == null) {
            // primera vez: la copia del libro de este jugador (y el mapa de la ciudadela en el capitulo 1)
            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            items.set(13, Story.book(ch));
            if (ch == 1) {
                int[] site = CastleSites.nearest(((ServerLevel) lvl).getSeed(), pos.getX(), pos.getZ(), 1);
                if (site != null) items.set(11, Story.map(site[0], site[1]));
            }
            if (ch == 3) com.darkcraft.rey.Recompensa.llenar(sp, (ServerLevel) lvl, pos, items);
            if (ch == 5) com.darkcraft.gavran.VictoriaGavran.llenar(sp, (ServerLevel) lvl, pos, items);
            data.setItems(pos, sp.getUUID(), ContainerHelper.saveAllItems(new CompoundTag(), items));
            Story.markRead(sp, ch);
            com.darkcraft.espejo.Espejo.leido(sp, ch);
        }
        if (!Story.seenCine(sp, ch)) Cinematics.play(sp, ch, pos.immutable(), true);
        else openChest(sp, pos, ch);
    }

    /** Abre el cofre con lo que este jugador tiene guardado dentro. */
    public static void openChest(ServerPlayer sp, BlockPos pos, int ch) {
        ServerLevel lvl = sp.serverLevel();
        if (!(lvl.getBlockEntity(pos) instanceof ChestBlockEntity)) return;
        // despues de la cinematica (o al saltarla): el logro de ese capitulo
        Logros.dar(sp, Logros.capitulo(ch));
        StoryData data = StoryData.get(lvl);
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        CompoundTag tag = data.items(pos, sp.getUUID());
        if (tag != null) ContainerHelper.loadAllItems(tag, items);
        SimpleContainer box = new SimpleContainer(27) {
            @Override
            public boolean stillValid(Player p) {
                return p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64 && lvl.getBlockState(pos).is(Blocks.CHEST);
            }

            @Override
            public void startOpen(Player p) {
                lid(lvl, pos, 1);
            }

            @Override
            public void stopOpen(Player p) {
                NonNullList<ItemStack> out = NonNullList.withSize(27, ItemStack.EMPTY);
                for (int i = 0; i < 27; i++) out.set(i, getItem(i));
                data.setItems(pos, p.getUUID(), ContainerHelper.saveAllItems(new CompoundTag(), out));
                lid(lvl, pos, -1);
            }
        };
        for (int i = 0; i < 27; i++) box.setItem(i, items.get(i));
        sp.openMenu(new SimpleMenuProvider((id, inv, pl) -> ChestMenu.threeRows(id, inv, box), Component.translatable("darkcraft.cofre", ch)));
    }

    private static void lid(ServerLevel lvl, BlockPos pos, int d) {
        int n = Math.max(0, OPEN.getOrDefault(pos, 0) + d);
        if (n == 0) OPEN.remove(pos);
        else OPEN.put(pos.immutable(), n);
        lvl.blockEvent(pos, Blocks.CHEST, 1, n);
        if (d > 0 && n == 1) lvl.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, 0.9f + lvl.random.nextFloat() * 0.1f);
        if (d < 0 && n == 0) lvl.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, 0.9f + lvl.random.nextFloat() * 0.1f);
    }

    /** Pone el cofre de un capitulo en tiempo de juego (lo usaran los jefes al morir). */
    public static void placeChapter(ServerLevel lvl, int ch, BlockPos pos, Direction facing) {
        lvl.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 3);
        BlockEntity be = lvl.getBlockEntity(pos);
        if (be != null) {
            be.getPersistentData().putInt(Canvas.STORY_TAG, ch);
            be.setChanged();
        }
        // (sin cartel al lado: el cofre se reconoce por su brillo)
    }

    // ------------------------------------------------------------------ sin carteles: los cofres brillan

    /** Cofres de la cronica que hay cerca de algun jugador, por dimension (se buscan cada 2 s). */
    private static final Map<net.minecraft.resources.ResourceKey<Level>, Map<BlockPos, Integer>> CERCA = new HashMap<>();
    /** El brillo de los cofres: polvo dorado que se vuelve rojo oscuro (los colores del mod). */
    private static final net.minecraft.core.particles.DustColorTransitionOptions BRILLO = new net.minecraft.core.particles.DustColorTransitionOptions(
            new org.joml.Vector3f(1.0f, 0.80f, 0.30f), new org.joml.Vector3f(0.70f, 0.10f, 0.10f), 0.8f);

    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent e) {
        if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        net.minecraft.server.MinecraftServer srv = e.getServer();
        int t = srv.getTickCount();
        if (t % 40 == 9) buscar(srv);
        if (t % 8 == 3) brillar(srv, t / 8);
    }

    /**
     * Mira los cofres y carteles de la cronica en los chunks de alrededor de cada jugador: los carteles (de mundos de
     * antes) se quitan y los cofres se apuntan para que brillen.
     */
    private static void buscar(net.minecraft.server.MinecraftServer srv) {
        CERCA.clear();
        for (ServerPlayer p : srv.getPlayerList().getPlayers()) {
            ServerLevel lvl = p.serverLevel();
            Map<BlockPos, Integer> m = CERCA.computeIfAbsent(lvl.dimension(), k -> new HashMap<>());
            java.util.List<BlockPos> carteles = null;
            int pcx = p.chunkPosition().x, pcz = p.chunkPosition().z;
            for (int cx = pcx - 2; cx <= pcx + 2; cx++)
                for (int cz = pcz - 2; cz <= pcz + 2; cz++) {
                    net.minecraft.world.level.chunk.LevelChunk ch = lvl.getChunkSource().getChunkNow(cx, cz);
                    if (ch == null) continue;
                    for (BlockEntity be : ch.getBlockEntities().values()) {
                        boolean cartel = be instanceof SignBlockEntity;
                        if (!cartel && !(be instanceof ChestBlockEntity)) continue;
                        int cap = be.getPersistentData().getInt(Canvas.STORY_TAG);
                        if (cap < 1 || cap > Story.CHAPTERS) continue;
                        if (cartel) {
                            if (carteles == null) carteles = new java.util.ArrayList<>();
                            carteles.add(be.getBlockPos().immutable());
                        } else m.put(be.getBlockPos().immutable(), cap);
                    }
                }
            if (carteles != null) for (BlockPos c : carteles) lvl.setBlock(c, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /**
     * El brillo de los cofres de la cronica (cada jugador ve el suyo): polvo dorado y destellos sobre el cofre mientras no
     * haya leido ese capitulo; despues, solo un poco de polvo de vez en cuando.
     */
    private static void brillar(net.minecraft.server.MinecraftServer srv, int paso) {
        for (ServerPlayer p : srv.getPlayerList().getPlayers()) {
            ServerLevel lvl = p.serverLevel();
            Map<BlockPos, Integer> m = CERCA.get(lvl.dimension());
            if (m == null || m.isEmpty()) continue;
            int leidos = Story.read(p);
            for (Map.Entry<BlockPos, Integer> en : m.entrySet()) {
                BlockPos c = en.getKey();
                if (c.distSqr(p.blockPosition()) > 28 * 28) continue;
                boolean nuevo = (leidos & (1 << en.getValue())) == 0;
                if (!nuevo && paso % 5 != 0) continue;
                double x = c.getX() + 0.5, y = c.getY() + 0.95, z = c.getZ() + 0.5;
                lvl.sendParticles(p, BRILLO, false, x, y, z, nuevo ? 2 : 1, 0.3, 0.14, 0.3, 0.0);
                if (nuevo && paso % 3 == 0)
                    lvl.sendParticles(p, net.minecraft.core.particles.ParticleTypes.WAX_ON, false, x, y + 0.25, z, 1, 0.32, 0.22, 0.32, 0.4);
                if (nuevo && paso % 7 == 0)
                    lvl.sendParticles(p, net.minecraft.core.particles.ParticleTypes.END_ROD, false, x, y + 0.1, z, 1, 0.2, 0.05, 0.2, 0.01);
            }
        }
    }

    // ------------------------------------------------------------------ que no se puedan romper

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (e.getPlayer() != null && e.getPlayer().isCreative()) return;
        if (e.getLevel() instanceof Level lvl && chapterAt(lvl, e.getPos()) > 0) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplode(ExplosionEvent.Detonate e) {
        e.getAffectedBlocks().removeIf(p -> chapterAt(e.getLevel(), p) > 0);
    }

    // ------------------------------------------------------------------ comandos (para probar)

    /** /darkcraft valdrenn historia ... (lo registra Comandos). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> comandoHistoria() {
        return Commands.literal("historia")
                .then(Commands.literal("capitulo").then(Commands.argument("n", IntegerArgumentType.integer(1, Story.CHAPTERS))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!Cinematics.active(p)) Cinematics.play(p, IntegerArgumentType.getInteger(ctx, "n"), null, false);
                            return 1;
                        })))
                .then(Commands.literal("reiniciar").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Story.reset(p);
                    StoryData.get(p.serverLevel()).forgetPlayer(p.getUUID());
                    ctx.getSource().sendSuccess(() -> Component.translatable("darkcraft.historia.reiniciada"), false);
                    return 1;
                }))
                .then(Commands.literal("libro").then(Commands.argument("n", IntegerArgumentType.integer(1, Story.CHAPTERS))
                        .executes(ctx -> give(ctx.getSource(), Story.book(IntegerArgumentType.getInteger(ctx, "n"))))))
                .then(Commands.literal("mapa").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    int[] s = CastleSites.nearest(p.serverLevel().getSeed(), p.getBlockX(), p.getBlockZ(), 4);
                    if (s == null) return 0;
                    return give(ctx.getSource(), Story.map(s[0], s[1]));
                }))
                .then(Commands.literal("cofre").then(Commands.argument("n", IntegerArgumentType.integer(1, Story.CHAPTERS))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            Direction f = p.getDirection();
                            placeChapter(p.serverLevel(), IntegerArgumentType.getInteger(ctx, "n"), p.blockPosition().relative(f, 2), f.getOpposite());
                            return 1;
                        })));
    }

    private static int give(CommandSourceStack src, ItemStack st) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        if (!p.getInventory().add(st)) p.drop(st, false);
        return 1;
    }
}
