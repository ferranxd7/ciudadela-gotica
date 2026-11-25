package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

import java.util.*;

/**
 * Raices y espinas de los combos del Mandoble del Juramento: entidades que muestran un bloque y crecen, se quedan un
 * momento y se recogen solas. Son de usar y tirar: si el mundo se cierra con alguna fuera, al volver se borra.
 */
public final class EfectosRaiz {
    private EfectosRaiz() {}

    public static final String TAG = "darkcraft_efimera";

    /** En el tick t (del servidor) la entidad pasa a la forma m en dur ticks (o se quita). */
    private record Paso(Display.BlockDisplay d, long t, Matrix4f m, int dur, boolean quitar) {}

    private static final List<Paso> PASOS = new ArrayList<>();
    private static final Set<UUID> VIVAS = new HashSet<>();
    private static long tick;

    /** Estaca de raiz que crece en (x, y, z) inclinada 'tilt' hacia (sin yaw, cos yaw), dura 'dura' ticks y se recoge. */
    public static void estaca(ServerLevel sl, double x, double y, double z, float yaw, float tilt, float alto, float grueso, int retraso, int dura) {
        RandomSource r = sl.random;
        BlockState st = r.nextInt(4) == 0 ? Blocks.MANGROVE_ROOTS.defaultBlockState() : DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        estaca(sl, st, x, y, z, yaw, tilt, alto, grueso, retraso, dura);
    }

    /** La misma estaca, de otro bloque (los pinchos de hueso de las trampas de la fortaleza). */
    public static void estaca(ServerLevel sl, BlockState st, double x, double y, double z, float yaw, float tilt, float alto, float grueso, int retraso, int dura) {
        Matrix4f lleno = new Matrix4f().rotateY(yaw).rotateX(tilt).scale(grueso, alto, grueso).translate(-0.5f, 0f, -0.5f);
        Matrix4f vacio = new Matrix4f(lleno).scale(1f, 0.02f, 1f);
        Display.BlockDisplay d = display(sl, st, x, y, z, vacio);
        if (d == null) return;
        PASOS.add(new Paso(d, tick + 1 + retraso, lleno, 3, false));
        PASOS.add(new Paso(d, tick + 1 + retraso + dura, vacio, 6, false));
        PASOS.add(new Paso(d, tick + 8 + retraso + dura, vacio, 0, true));
    }

    /** Espina tumbada que sale desde (x, y, z) hacia (sin yaw, cos yaw) hasta 'hasta' bloques y se recoge. */
    public static void espina(ServerLevel sl, double x, double y, double z, float yaw, float hasta, int dura) {
        BlockState st = DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        float w = 0.18f, largo = 1.8f;
        Matrix4f m0 = new Matrix4f().rotateY(yaw).translate(-w / 2, -w / 2, 0.3f).scale(w, w, 0.3f);
        Matrix4f m1 = new Matrix4f().rotateY(yaw).translate(-w / 2, -w / 2, Math.max(0.3f, hasta - largo)).scale(w, w, largo);
        Matrix4f m2 = new Matrix4f().rotateY(yaw).translate(-w / 2, -w / 2, Math.max(0.3f, hasta - largo)).scale(w, w, 0.05f);
        Display.BlockDisplay d = display(sl, st, x, y, z, m0);
        if (d == null) return;
        PASOS.add(new Paso(d, tick + 1, m1, 4, false));
        PASOS.add(new Paso(d, tick + 1 + dura, m2, 5, false));
        PASOS.add(new Paso(d, tick + 7 + dura, m2, 0, true));
    }

    private static Display.BlockDisplay display(ServerLevel sl, BlockState st, double x, double y, double z, Matrix4f m) {
        Display.BlockDisplay d = EntityType.BLOCK_DISPLAY.create(sl);
        if (d == null) return null;
        CompoundTag tag = new CompoundTag();
        tag.put("block_state", NbtUtils.writeBlockState(st));
        tag.put("transformation", matriz(m));
        tag.putInt("interpolation_duration", 0);
        CompoundTag luz = new CompoundTag();
        luz.putInt("sky", 0);
        luz.putInt("block", 11);
        tag.put("brightness", luz);
        tag.putFloat("view_range", 1.0f);
        d.load(tag);
        d.moveTo(x, y, z, 0f, 0f);
        d.addTag(TAG);
        VIVAS.add(d.getUUID());
        sl.addFreshEntity(d);
        return d;
    }

    private static ListTag matriz(Matrix4f m) {
        ListTag l = new ListTag();
        for (int i = 0; i < 16; i++) l.add(FloatTag.valueOf(m.getRowColumn(i >> 2, i & 3)));
        return l;
    }

    private static void poner(Display.BlockDisplay d, Matrix4f m, int ticks) {
        CompoundTag t = d.saveWithoutId(new CompoundTag());
        t.put("transformation", matriz(m));
        t.putInt("interpolation_duration", ticks);
        t.putInt("start_interpolation", 0);
        d.load(t);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        tick++;
        if (PASOS.isEmpty()) return;
        Iterator<Paso> it = PASOS.iterator();
        List<Paso> ahora = new ArrayList<>();
        while (it.hasNext()) {
            Paso p = it.next();
            if (p.t() > tick) continue;
            it.remove();
            ahora.add(p);
        }
        for (Paso p : ahora) {
            if (p.d().isRemoved()) continue;
            if (p.quitar()) {
                VIVAS.remove(p.d().getUUID());
                p.d().discard();
            } else poner(p.d(), p.m(), p.dur());
        }
    }

    /** Las que se quedaron de una partida anterior (el mundo se cerro con ellas fuera) se borran al cargar. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Display d)) return;
        if (d.getTags().contains(TAG) && !VIVAS.contains(d.getUUID())) e.setCanceled(true);
    }

    /** Otra entidad de usar y tirar (con TAG): puede entrar en el mundo ahora, pero no si se guarda y se vuelve a cargar. */
    public static void efimera(net.minecraft.world.entity.Entity d) {
        d.addTag(TAG);
        VIVAS.add(d.getUUID());
    }

    public static float yaw(double dx, double dz) {
        return (float) Math.atan2(dx, dz);
    }

    public static float ang(RandomSource r) {
        return r.nextFloat() * Mth.TWO_PI;
    }
}
