package com.darkcraft.gavran;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import com.darkcraft.net.Net;
import com.darkcraft.rey.ReyEstado;
import com.darkcraft.rey.Victoria;
import com.darkcraft.story.StoryEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Cuando Ser Gavran cae: a sus pies sube, en un haz de luz, el cofre del capitulo 5 (el libro, la llave de hueso, el
 * Mandoble del Juramento y el botin para quien lo vencio).
 */
public final class VictoriaGavran {
    private VictoriaGavran() {}

    /** Hasta que tick del servidor (Victoria) esta subiendo un cofre (mientras, no se pone otro). */
    private static long subeHasta = -1;

    public static boolean subiendo() {
        return Victoria.ahora() < subeHasta;
    }

    /** El cofre sube del suelo en chest (como el del Rey, con hojas y luz verde). */
    public static void cofre(ServerLevel sl, SalaArbol s, BlockPos chest) {
        subeHasta = Victoria.ahora() + Victoria.SUBIDA + 5;
        sl.setBlock(chest, Blocks.AIR.defaultBlockState(), 3);
        Net.Cofre m = new Net.Cofre(chest.getX(), chest.getY(), chest.getZ(), Victoria.SUBIDA + 160);
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(chest.getX(), chest.getY(), chest.getZ()) < 120 * 120) Net.to(p, m);
        sl.playSound(null, chest, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0f, 0.7f);
        sl.playSound(null, chest, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 2.0f, 0.6f);
        for (int i = 0; i < Victoria.SUBIDA; i += 5) {
            final int k = i;
            Victoria.despues(i, () -> {
                sl.sendParticles(Raices.VERDE, chest.getX() + 0.5, chest.getY() + 0.6, chest.getZ() + 0.5, 12, 0.6, 0.8, 0.6, 0.02);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, chest.getX() + 0.5, chest.getY() + 2.5, chest.getZ() + 0.5, 6, 1.0, 1.0, 1.0, 0.01);
                if (k % 10 == 0) sl.playSound(null, chest, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5f, 0.6f + k / 100f);
            });
        }
        Victoria.despues(Victoria.SUBIDA, () -> {
            StoryEvents.placeChapter(sl, 5, chest, Direction.SOUTH);
            sl.sendParticles(ParticleTypes.END_ROD, chest.getX() + 0.5, chest.getY() + 1, chest.getZ() + 0.5, 40, 0.5, 1.0, 0.5, 0.08);
            sl.sendParticles(ParticleTypes.GLOW, chest.getX() + 0.5, chest.getY() + 1, chest.getZ() + 0.5, 30, 0.6, 1.0, 0.6, 0.05);
            sl.playSound(null, chest, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.5f, 0.7f);
            DarkCraftMod.LOGGER.info("[DarkCraft] Cofre del capitulo 5 en {}", chest);
        });
    }

    // ------------------------------------------------------------------ la llave de hueso de la cinematica del capitulo 5

    private static Display.ItemDisplay llave;
    private static long llaveHasta;

    /** Donde flota la llave de hueso en la cinematica del capitulo 5: encima del cofre. */
    public static Vec3 llavePos(SalaArbol s) {
        BlockPos c = BlockPos.containing(s.aterrizaje());
        return new Vec3(c.getX() + 0.5, c.getY() + 3.4, c.getZ() + 0.5);
    }

    /** La llave de hueso flota y gira sobre el cofre durante 'ticks' (mientras el narrador habla de ella). */
    public static void llaveFlotante(ServerLevel sl, SalaArbol s, int ticks) {
        long now = sl.getGameTime();
        if (llave != null && llave.isAlive() && llave.level() == sl) {
            llaveHasta = Math.max(llaveHasta, now + ticks);
            return;
        }
        Display.ItemDisplay d = EntityType.ITEM_DISPLAY.create(sl);
        if (d == null) return;
        CompoundTag tag = new CompoundTag();
        tag.put("item", new ItemStack(DarkCraftMod.LLAVE_HUESO.get()).save(new CompoundTag()));
        tag.putString("item_display", "fixed");
        tag.putString("billboard", "vertical");
        tag.put("transformation", matriz(new Matrix4f().scale(1.2f)));
        CompoundTag luz = new CompoundTag();
        luz.putInt("sky", 15);
        luz.putInt("block", 15);
        tag.put("brightness", luz);
        tag.putFloat("view_range", 2.0f);
        d.load(tag);
        Vec3 k = llavePos(s);
        d.moveTo(k.x, k.y, k.z, 0f, 0f);
        EfectosRaiz.efimera(d);
        sl.addFreshEntity(d);
        llave = d;
        llaveHasta = now + ticks;
        sl.playSound(null, BlockPos.containing(k), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.2f, 0.5f);
        flotar(sl, d, 1);
    }

    /** Siempre de cara a quien la mira: sube y baja despacio, meciendose un poco, con chispas; al acabar el tiempo se va. */
    private static void flotar(ServerLevel sl, Display.ItemDisplay d, int paso) {
        if (!d.isAlive()) return;
        if (sl.getGameTime() >= llaveHasta) {
            Vec3 k = d.position();
            sl.sendParticles(ParticleTypes.END_ROD, k.x, k.y, k.z, 12, 0.2, 0.2, 0.2, 0.05);
            d.discard();
            if (llave == d) llave = null;
            return;
        }
        CompoundTag t = d.saveWithoutId(new CompoundTag());
        float arriba = paso % 2 == 0 ? 0.09f : -0.09f, mece = (float) Math.toRadians(paso % 4 < 2 ? 7 : -7);
        t.put("transformation", matriz(new Matrix4f().translate(0, arriba, 0).rotateZ(mece).scale(1.2f)));
        t.putInt("interpolation_duration", 20);
        t.putInt("start_interpolation", 0);
        d.load(t);
        Vec3 k = d.position();
        sl.sendParticles(ParticleTypes.END_ROD, k.x, k.y - 0.2, k.z, 2, 0.35, 0.4, 0.35, 0.01);
        sl.sendParticles(Raices.VERDE, k.x, k.y - 0.9, k.z, 3, 0.25, 0.6, 0.25, 0);
        Victoria.despues(20, () -> flotar(sl, d, paso + 1));
    }

    private static ListTag matriz(Matrix4f m) {
        ListTag l = new ListTag();
        for (int i = 0; i < 16; i++) l.add(FloatTag.valueOf(m.getRowColumn(i >> 2, i & 3)));
        return l;
    }

    /** Lo que encuentra en el cofre del capitulo 5 quien ayudo a vencer a Ser Gavran (cada uno su copia). */
    public static void llenar(ServerPlayer sp, ServerLevel lvl, BlockPos pos, NonNullList<ItemStack> items) {
        SalaArbol s = SalaArbol.cerca(lvl, pos.getX(), pos.getZ());
        if (s == null || !ReyEstado.get(lvl.getServer()).gano(s.key(), sp.getUUID())) return;
        items.set(4, new ItemStack(DarkCraftMod.MANDOBLE_JURAMENTO.get()));
        // la llave ya apunta a la Fortaleza de Hueso de este reino
        items.set(22, com.darkcraft.armas.LlaveHueso.apuntar(new ItemStack(DarkCraftMod.LLAVE_HUESO.get()),
                com.darkcraft.fortaleza.Fortalezas.delReino(lvl.getSeed(), pos.getX(), pos.getZ())));
        items.set(10, new ItemStack(Items.NETHERITE_INGOT, 6));
        items.set(16, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 3));
        items.set(19, new ItemStack(Items.EXPERIENCE_BOTTLE, 48));
        items.set(25, new ItemStack(Items.TOTEM_OF_UNDYING, 2));
        items.set(1, new ItemStack(Items.FLOWERING_AZALEA, 8));
        // el campeon del Rey: su talisman de hueso (y vencerle abre el tercer hueco de talisman)
        items.set(13, new ItemStack(DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.HUESO).get()));
        items.set(12, new ItemStack(DarkCraftMod.FRUTOS.get(com.darkcraft.rol.Fruto.Clase.CENIZA).get(), 3));
        // piedras de forja para Dorvan
        items.set(14, new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        // y la ceniza de sus espinas
        items.set(15, new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.SANGRE).get()));
        for (int slot : new int[]{20, 21, 23, 24})
            items.set(slot, EnchantmentHelper.enchantItem(lvl.random, new ItemStack(Items.BOOK), 30, true));
    }
}
