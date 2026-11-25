package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.TorreCupula;
import com.darkcraft.gen.FortalezaPlan;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * /darkcraft fortaleza ...: localizar (la del reino mas cercano), ir (a la puerta), escalera (al pie del puente),
 * construir (levanta ya lo que este cargado), puente, abrir (la puerta), portal (el capitulo 6), amuleto, acertijo,
 * libros (los once libros perdidos) y reiniciar (cierra la puerta, quita el portal y olvida el puente).
 */
public final class ComandosFortaleza {
    private ComandosFortaleza() {}

    static Fortalezas.Sitio sitio(ServerPlayer p) {
        long seed = p.serverLevel().getSeed();
        Fortalezas.Sitio s = Fortalezas.cercana(seed, p.getX(), p.getZ(), 400);
        return s != null ? s : Fortalezas.delReino(seed, p.getX(), p.getZ());
    }

    private static int conSitio(CommandContext<CommandSourceStack> c, Accion a) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (p.level().dimension() != Level.OVERWORLD) {
            c.getSource().sendFailure(Component.translatable("darkcraft.llave.nada"));
            return 0;
        }
        Fortalezas.Sitio s = sitio(p);
        if (s == null) {
            c.getSource().sendFailure(Component.translatable("darkcraft.fortaleza.ninguna"));
            return 0;
        }
        return a.hacer(p, s, Fortalezas.plan(p.serverLevel().getSeed(), s));
    }

    interface Accion {
        int hacer(ServerPlayer p, Fortalezas.Sitio s, FortalezaPlan f) throws CommandSyntaxException;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("fortaleza")
                .then(Commands.literal("localizar").executes(c -> conSitio(c, (p, s, f) -> {
                    int d = (int) Math.round(Math.hypot(s.x() - p.getX(), s.z() - p.getZ()));
                    Component pos = Component.literal("X " + s.x() + "  Y " + f.Y + "  Z " + s.z()).withStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW).withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + s.x() + " " + (f.Y + 1) + " " + (s.z() + TorreCupula.PFUERA + 6)))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("darkcraft.ayuda.clic"))));
                    c.getSource().sendSuccess(() -> Component.translatable("darkcraft.fortaleza.localizar", pos, d,
                            s.reinoX() + ", " + s.reinoZ()).withStyle(ChatFormatting.GOLD), false);
                    return 1;
                })))
                .then(Commands.literal("ir").executes(c -> conSitio(c, (p, s, f) -> {
                    p.teleportTo(p.serverLevel(), f.cx + 0.5, f.Y, f.cz + TorreCupula.PFUERA + 6.5, 180f, 5f);
                    return 1;
                })))
                .then(Commands.literal("montana").executes(c -> conSitio(c, (p, s, f) -> {
                    // al pie del sendero, en la plazoleta donde llega el camino de nubes, mirando a la montana
                    double[] pie = f.montana.pie();
                    p.teleportTo(p.serverLevel(), pie[0] + 0.5, pie[2], pie[1] + 3.5, 180f, -12f);
                    return 1;
                })))
                .then(Commands.literal("mirador").executes(c -> conSitio(c, (p, s, f) -> {
                    // en lo alto, delante del arco de colmillos, mirando a la fortaleza
                    int[] a = Puente.arco(f);
                    p.teleportTo(p.serverLevel(), a[0] + 0.5, a[1], a[2] + 4.5, 180f, -6f);
                    return 1;
                })))
                .then(Commands.literal("construir").executes(c -> conSitio(c, (p, s, f) -> {
                    int n = FortalezaEventos.construirYa(p.serverLevel(), s);
                    c.getSource().sendSuccess(() -> Component.literal("fortaleza: " + n + " chunks"), false);
                    return n;
                })))
                .then(Commands.literal("puente").executes(c -> conSitio(c, (p, s, f) -> {
                    FortalezaDatos d = FortalezaDatos.get(p.server);
                    d.de(s).puente = false;
                    return FortalezaEventos.levantarPuente(p, s) ? 1 : 0;
                })))
                .then(Commands.literal("abrir").executes(c -> conSitio(c, (p, s, f) -> FortalezaEventos.abrirPuerta(p, s) ? 1 : 0)))
                .then(Commands.literal("portal").executes(c -> conSitio(c, (p, s, f) -> {
                    FortalezaDatos d = FortalezaDatos.get(p.server);
                    boolean abre = !d.de(s).portal;
                    d.de(s).portal = true;
                    d.setDirty();
                    FortalezaEventos.capitulo6(p, f, abre);
                    return 1;
                })))
                .then(Commands.literal("amuleto").executes(c -> dar(c.getSource(), new ItemStack(DarkCraftMod.AMULETO_HUESO.get()))))
                .then(Commands.literal("acertijo").executes(c -> dar(c.getSource(), new ItemStack(DarkCraftMod.LIBRO_ACERTIJO.get()))))
                .then(Commands.literal("libros").executes(c -> {
                    for (int n = 1; n <= Perdidos.TOTAL; n++) dar(c.getSource(), Perdidos.libro(n));
                    return Perdidos.TOTAL;
                }))
                .then(Commands.literal("reiniciar").executes(c -> conSitio(c, (p, s, f) -> {
                    FortalezaDatos d = FortalezaDatos.get(p.server);
                    FortalezaDatos.Estado e = d.de(s);
                    ServerLevel lvl = p.serverLevel();
                    if (e.puerta) FortalezaEventos.cerrarPuerta(lvl, f);
                    if (e.puente) Puente.quitar(lvl, f);
                    if (e.portal) {
                        int[] q = f.f.portal();
                        for (int x = q[0]; x <= q[3]; x++)
                            for (int y = q[1]; y <= q[4]; y++)
                                if (f.f.enPortal(x, y)) lvl.setBlock(new net.minecraft.core.BlockPos(x, y, q[2]), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                    e.puerta = e.portal = e.puente = false;
                    e.baseEscalera = Integer.MIN_VALUE;
                    d.setDirty();
                    c.getSource().sendSuccess(() -> Component.translatable("darkcraft.fortaleza.reiniciada"), false);
                    return 1;
                })));
    }

    private static int dar(CommandSourceStack src, ItemStack st) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        if (!p.getInventory().add(st)) p.drop(st, false);
        return 1;
    }
}
