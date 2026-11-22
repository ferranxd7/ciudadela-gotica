package com.darkcraft;

import com.darkcraft.test.CiTest;
import com.darkcraft.world.CastleSites;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("darkcraft").requires(s -> s.hasPermission(2))
                .then(Commands.literal("localizar").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
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
                }))
                .then(Commands.literal("ir").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
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
                })));
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
