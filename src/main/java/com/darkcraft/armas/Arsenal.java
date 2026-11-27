package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Congelacion;
import com.darkcraft.enemigos.Sangrado;
import com.darkcraft.rol.Equipo;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

/**
 * El comando de las armas de la parte 7aj: {@code /darkcraft armas [martillos | flechas | sangre <n> | frio <n> | limpiar]} (sin
 * mas: lo que se lleva sumado de sangrado y de congelacion). {@code sangre} y {@code frio} suman a lo que se tenga delante (a 16
 * bloques) o, si no hay nada, a uno mismo.
 */
public final class Arsenal {
    private Arsenal() {}

    private static int decir(CommandSourceStack s, String t) {
        s.sendSuccess(() -> Component.literal(t), false);
        return 1;
    }

    /** Lo que se tiene delante (a 16 bloques) o uno mismo. */
    private static LivingEntity delante(ServerPlayer sp) {
        Vec3 ojo = sp.getEyePosition(), fin = ojo.add(sp.getLookAngle().scale(16));
        AABB caja = sp.getBoundingBox().expandTowards(sp.getLookAngle().scale(16)).inflate(1.0);
        EntityHitResult r = ProjectileUtil.getEntityHitResult(sp, ojo, fin, caja, e -> e instanceof LivingEntity && e.isAlive() && e != sp, 256);
        Entity e = r == null ? null : r.getEntity();
        return e instanceof LivingEntity le ? le : sp;
    }

    public static String resumen(LivingEntity le) {
        return String.format(Locale.ROOT, "%s: sangrado %.0f, congelacion %.0f%s", le.getName().getString(), Sangrado.nivel(le),
                Congelacion.nivel(le), Congelacion.debil(le) ? " (congelado: recibe mas)" : "");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("armas")
                .requires(s -> s.hasPermission(2))
                .executes(c -> decir(c.getSource(), resumen(c.getSource().getPlayerOrException())))
                .then(Commands.literal("martillos").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    Equipo.dar(sp, new ItemStack(DarkCraftMod.MAZA.get()));
                    Equipo.dar(sp, new ItemStack(DarkCraftMod.MARTILLO_GUERRA.get()));
                    Equipo.dar(sp, new ItemStack(DarkCraftMod.GRAN_MARTILLO.get()));
                    return decir(c.getSource(), "La maza, el martillo de guerra y el gran martillo");
                }))
                .then(Commands.literal("flechas").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    for (RegistryObject<net.minecraft.world.item.Item> f : DarkCraftMod.FLECHAS.values()) Equipo.dar(sp, new ItemStack(f.get(), 16));
                    if (sp.getInventory().countItem(Items.BOW) == 0) Equipo.dar(sp, new ItemStack(Items.BOW));
                    if (sp.getInventory().countItem(Items.CROSSBOW) == 0) Equipo.dar(sp, new ItemStack(Items.CROSSBOW));
                    return decir(c.getSource(), "16 flechas de cada (fuego, sangre y escarcha), un arco y una ballesta");
                }))
                .then(Commands.literal("sangre").then(Commands.argument("n", IntegerArgumentType.integer(1, 200)).executes(c -> {
                    LivingEntity le = delante(c.getSource().getPlayerOrException());
                    boolean revento = Sangrado.sumar(le, IntegerArgumentType.getInteger(c, "n"), c.getSource().getPlayerOrException());
                    return decir(c.getSource(), resumen(le) + (revento ? " | HEMORRAGIA" : ""));
                })))
                .then(Commands.literal("frio").then(Commands.argument("n", IntegerArgumentType.integer(1, 200)).executes(c -> {
                    LivingEntity le = delante(c.getSource().getPlayerOrException());
                    boolean revento = Congelacion.sumar(le, IntegerArgumentType.getInteger(c, "n"), c.getSource().getPlayerOrException());
                    return decir(c.getSource(), resumen(le) + (revento ? " | CONGELADO" : ""));
                })))
                .then(Commands.literal("limpiar").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    Sangrado.limpiar(sp);
                    Congelacion.limpiar(sp);
                    return decir(c.getSource(), resumen(sp));
                }));
    }
}
