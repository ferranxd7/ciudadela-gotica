package com.darkcraft;

import com.darkcraft.test.CiTest;
import com.darkcraft.world.CastleSites;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ModEvents {
    private ModEvents() {}

    // ------------------------------------------------------------------ /darkcraft valdrenn localizar | ir (los registra Comandos)

    /** Donde esta el Reino de Valdrenn mas cercano, con un boton para ir. */
    static int localizar(CommandSourceStack src) {
        Vec3 p = src.getPosition();
        long seed = src.getServer().overworld().getSeed();
        int[] s = CastleSites.nearest(seed, (int) p.x, (int) p.z, 4);
        if (s == null) {
            src.sendFailure(Component.translatable("darkcraft.cmd.no_reino"));
            return 0;
        }
        int dist = (int) Math.sqrt((s[0] - p.x) * (s[0] - p.x) + (s[1] - p.z) * (s[1] - p.z));
        String tp = "/tp @s " + s[0] + " 400 " + (s[1] + 60);
        Component msg = Component.translatable("darkcraft.cmd.localizar", s[0], s[1], dist).append(" ")
                .append(Component.translatable("darkcraft.cmd.tp").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tp))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tp)))));
        src.sendSuccess(() -> msg, false);
        return 1;
    }

    /** Te lleva al Reino de Valdrenn mas cercano. */
    static int ir(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer pl = src.getPlayerOrException();
        long seed = src.getServer().overworld().getSeed();
        int[] s = CastleSites.nearest(seed, (int) pl.getX(), (int) pl.getZ(), 4);
        if (s == null) {
            src.sendFailure(Component.translatable("darkcraft.cmd.no_reino"));
            return 0;
        }
        pl.teleportTo(src.getServer().overworld(), s[0] + 0.5, 400, s[1] + 60.5, pl.getYRot(), pl.getXRot());
        src.sendSuccess(() -> Component.translatable("darkcraft.cmd.bienvenido"), false);
        return 1;
    }

    @SubscribeEvent
    public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        com.darkcraft.gen.Calib.run(event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.side != net.minecraftforge.fml.LogicalSide.SERVER) return;
        if (!(event.player instanceof ServerPlayer sp) || sp.tickCount % 20 != 0) return;
        com.darkcraft.world.Storm.tick(sp);
    }

    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) com.darkcraft.world.Storm.night(event.getServer());
    }

    @SubscribeEvent
    public static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        com.darkcraft.world.Storm.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (Boolean.getBoolean("darkcraft.ciTest")) CiTest.run(event.getServer());
    }
}
