package com.ciudadela;

import com.ciudadela.test.CiTest;
import com.ciudadela.world.CastleSites;
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
        d.register(Commands.literal("ciudadela").requires(s -> s.hasPermission(2))
                .then(Commands.literal("localizar").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    Vec3 p = src.getPosition();
                    long seed = src.getServer().overworld().getSeed();
                    int[] s = CastleSites.nearest(seed, (int) p.x, (int) p.z, 4);
                    if (s == null) {
                        src.sendFailure(Component.literal("No hay ninguna ciudadela cerca."));
                        return 0;
                    }
                    int dist = (int) Math.sqrt((s[0] - p.x) * (s[0] - p.x) + (s[1] - p.z) * (s[1] - p.z));
                    String tp = "/tp @s " + s[0] + " 400 " + (s[1] + 60);
                    Component msg = Component.literal("Ciudadela gotica en X=" + s[0] + " Z=" + s[1] + " (a " + dist + " bloques). ")
                            .append(Component.literal("[Teletransportarme]").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
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
                        src.sendFailure(Component.literal("No hay ninguna ciudadela cerca."));
                        return 0;
                    }
                    pl.teleportTo(src.getServer().overworld(), s[0] + 0.5, 400, s[1] + 60.5, pl.getYRot(), pl.getXRot());
                    src.sendSuccess(() -> Component.literal("Bienvenido a la Ciudadela."), false);
                    return 1;
                })));
    }

    @SubscribeEvent
    public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        com.ciudadela.gen.Calib.run(event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.side != net.minecraftforge.fml.LogicalSide.SERVER) return;
        if (!(event.player instanceof ServerPlayer sp) || sp.tickCount % 20 != 0) return;
        com.ciudadela.world.Storm.tick(sp);
    }

    @SubscribeEvent
    public static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        com.ciudadela.world.Storm.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (Boolean.getBoolean("ciudadela.ciTest")) CiTest.run(event.getServer());
    }
}
