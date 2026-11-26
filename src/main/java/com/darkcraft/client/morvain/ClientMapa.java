package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.EstelaBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Los fragmentos del mapa de Morvain en el cliente (parte 7d): que regiones tiene (lo manda el servidor con
 * `Net.Mapas`), la estela que tiene a mano (para el aviso "Pulsa [E] para copiar el mapa": solo las de las regiones que
 * aun no tiene) y las runas que vuelan hacia las estelas que le faltan (las del encantamiento: asi se ven de lejos).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientMapa {
    private ClientMapa() {}

    /** Las regiones cuyo mapa tiene (ids de {@code morvain.Region}); cambia cuando cambia {@link #version}. */
    public static final Set<String> MAPAS = new HashSet<>();
    public static int version;

    /** La estela que tiene a mano (o null). */
    public static BlockPos aMano;

    private static final List<BlockPos> CERCA = new ArrayList<>();
    private static int ticks;

    public static void mapas(List<String> ids) {
        MAPAS.clear();
        MAPAS.addAll(ids);
        version++;
    }

    public static boolean tiene(String region) {
        return MAPAS.contains(region);
    }

    /** La region de la estela de pos (si es una de las de Morvain) o null. */
    private static String region(BlockPos p) {
        var e = com.darkcraft.morvain.Fragmentos.en(p);
        return e == null ? null : e.region().id;
    }

    /** Lo llama `ClientRol` en cada tick del cliente. */
    public static void tick(Minecraft mc) {
        ClientLevel lvl = mc.level;
        if (lvl == null || mc.player == null || lvl.dimension() != com.darkcraft.morvain.Morvain.NIVEL) {
            aMano = null;
            CERCA.clear();
            return;
        }
        ticks++;
        if (ticks % 20 == 3) buscarCerca(mc, lvl);
        if (ticks % 4 == 0) buscarAMano(mc, lvl);
        RandomSource r = lvl.random;
        for (BlockPos p : CERCA) {
            String reg = region(p);
            if (reg == null || tiene(reg)) continue;
            // las runas del encantamiento vuelan hacia el pergamino; alguna mota de luz encima
            if (r.nextInt(2) == 0) {
                double x = p.getX() + 0.5, y = p.getY() + 1.0, z = p.getZ() + 0.5;
                lvl.addParticle(ParticleTypes.ENCHANT, x, y + 0.2, z, (r.nextDouble() - 0.5) * 2.4, r.nextDouble() * 1.2 - 0.2, (r.nextDouble() - 0.5) * 2.4);
            }
            if (r.nextInt(9) == 0)
                lvl.addParticle(ParticleTypes.END_ROD, p.getX() + 0.5 + (r.nextDouble() - 0.5) * 0.6, p.getY() + 1.5, p.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.6,
                        0, 0.015, 0);
        }
    }

    /** Las estelas de alrededor (16 bloques), una vez por segundo. */
    private static void buscarCerca(Minecraft mc, ClientLevel lvl) {
        CERCA.clear();
        BlockPos c = mc.player.blockPosition();
        for (var e : com.darkcraft.morvain.Fragmentos.ESTELAS) {
            BlockPos p = e.pos();
            if (p.distManhattan(c) > 40 || !lvl.hasChunkAt(p)) continue;
            BlockState st = lvl.getBlockState(p);
            if (st.getBlock() instanceof EstelaBlock) CERCA.add(p);
        }
    }

    /** La estela que tiene a mano: a menos de 3,6 bloques y mirando hacia ella (o pegado), si aun no tiene su mapa. */
    private static void buscarAMano(Minecraft mc, ClientLevel lvl) {
        BlockPos mejor = null;
        double dMejor = 3.6 * 3.6;
        var ojo = mc.player.getEyePosition();
        var mira = mc.player.getViewVector(1f);
        for (BlockPos p : CERCA) {
            String reg = region(p);
            if (reg == null || tiene(reg)) continue;
            double d = mc.player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.7, p.getZ() + 0.5);
            if (d >= dMejor) continue;
            var a = new net.minecraft.world.phys.Vec3(p.getX() + 0.5 - ojo.x, p.getY() + 0.9 - ojo.y, p.getZ() + 0.5 - ojo.z);
            double largo = a.length();
            if (d > 1.5 * 1.5 && largo > 1e-3 && a.dot(mira) / largo < 0.84) continue;
            dMejor = d;
            mejor = p;
        }
        aMano = mejor;
    }

    @SubscribeEvent
    public static void onSalir(ClientPlayerNetworkEvent.LoggingOut e) {
        MAPAS.clear();
        version++;
        aMano = null;
        CERCA.clear();
    }

    /** Para la prueba. */
    public static String resumen() {
        return "mapas en el cliente " + new java.util.TreeSet<>(MAPAS) + ", estelas cerca " + CERCA.size() + ", a mano "
                + (aMano == null ? "-" : aMano.toShortString());
    }
}
