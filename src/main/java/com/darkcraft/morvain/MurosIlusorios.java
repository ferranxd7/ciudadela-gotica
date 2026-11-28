package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.story.Logros;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Los muros ilusorios de Morvain (parte 7y): trozos de pared que parecen de verdad y no lo son. Se deshacen al golpearlos (con
 * el arma: en aventura no llega el clic de romper bloques, asi que lo mira el combate, {@code Combate.pedir}; en creativo, el
 * clic izquierdo) y quedan abiertos para siempre (SavedData {@code darkcraft_muros}: los que se han abierto; si un chunk vuelve
 * a tener sus bloques, se quitan al pasar alguien). Abrir uno da el logro {@code secreto}. Los dos del Teatro Hundido (la puerta
 * real tapiada del muro de la escena y el de detras del trono de Pell), el de detras del altar de la capilla de los mineros, en
 * las Minas de Hierro Negro (parte 7aq), y el del fondo del pasillo de los priores, en las catacumbas del Osario (parte 7as).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class MurosIlusorios {
    private MurosIlusorios() {}

    /** Un muro: su nombre y sus celdas. */
    public record Muro(String id, List<BlockPos> celdas) {
        public BlockPos centro() {
            return celdas.get(celdas.size() / 2);
        }
    }

    private static List<Muro> todos;
    private static Map<Long, Muro> porCelda;

    /** Todos los muros ilusorios de Morvain. */
    public static synchronized List<Muro> todos() {
        if (todos == null) {
            List<Muro> l = new ArrayList<>();
            l.add(new Muro(Teatro.MURO_PUERTA, Teatro.muro(Teatro.MURO_PUERTA)));
            l.add(new Muro(Teatro.MURO_TRONO, Teatro.muro(Teatro.MURO_TRONO)));
            l.add(new Muro(Mina.MURO_CAPILLA, Mina.muro(Mina.MURO_CAPILLA)));
            l.add(new Muro(Osario.MURO_PRIORES, Osario.muro(Osario.MURO_PRIORES)));
            Map<Long, Muro> m = new HashMap<>();
            for (Muro w : l) for (BlockPos p : w.celdas()) m.put(p.asLong(), w);
            todos = l;
            porCelda = m;
        }
        return todos;
    }

    public static Muro muro(String id) {
        for (Muro w : todos()) if (w.id().equals(id)) return w;
        return null;
    }

    /** El muro de esa celda (o null). */
    public static Muro de(BlockPos p) {
        todos();
        return porCelda.get(p.asLong());
    }

    /** Lo que hay en una celda del muro cuando esta cerrado. */
    static net.minecraft.world.level.block.state.BlockState bloque(Muro w, BlockPos p) {
        if (w.id().startsWith("mina")) return Mina.bloqueMuro(w.id(), p);
        if (w.id().startsWith("osario")) return Osario.bloqueMuro(w.id(), p);
        return Teatro.bloqueMuro(w.id(), p);
    }

    // ------------------------------------------------------------------ lo que se guarda

    public static final class Datos extends SavedData {
        final Set<String> abiertos = new LinkedHashSet<>();

        static Datos leer(CompoundTag t) {
            Datos d = new Datos();
            for (Tag x : t.getList("abiertos", Tag.TAG_STRING)) d.abiertos.add(x.getAsString());
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            ListTag l = new ListTag();
            for (String s : abiertos) l.add(StringTag.valueOf(s));
            t.put("abiertos", l);
            return t;
        }
    }

    static Datos datos(MinecraftServer srv) {
        return srv.overworld().getDataStorage().computeIfAbsent(Datos::leer, Datos::new, "darkcraft_muros");
    }

    /** Si ese muro ya se ha abierto (para siempre). */
    public static boolean abierto(MinecraftServer srv, String id) {
        return datos(srv).abiertos.contains(id);
    }

    // ------------------------------------------------------------------ golpearlo

    /** Un golpe con arma (lo llama el combate): si se mira de cerca a un muro ilusorio cerrado, se deshace. */
    public static void golpe(ServerPlayer p) {
        if (p.level().dimension() != Morvain.NIVEL) return;
        Vec3 ojo = p.getEyePosition(), fin = ojo.add(p.getLookAngle().scale(4.5));
        BlockHitResult hit = p.level().clip(new ClipContext(ojo, fin, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        Muro w = de(hit.getBlockPos());
        if (w != null) abrir(p.serverLevel(), w, p);
    }

    /** En creativo (y si llega): el clic izquierdo en un muro ilusorio tambien lo deshace (y no se rompe el bloque). */
    @SubscribeEvent
    public static void onIzquierdo(PlayerInteractEvent.LeftClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.level().dimension() != Morvain.NIVEL) return;
        Muro w = de(e.getPos());
        if (w == null) return;
        abrir(p.serverLevel(), w, p);
        e.setCanceled(true);
    }

    /**
     * Se deshace el muro: sus bloques se van en polvo de almas y ceniza (con su sonido), queda abierto para siempre y quien lo
     * golpeo se lleva el logro {@code secreto}. Devuelve si estaba cerrado.
     */
    public static boolean abrir(ServerLevel lvl, Muro w, ServerPlayer quien) {
        int n = 0;
        for (BlockPos q : w.celdas()) {
            if (!lvl.isLoaded(q) || lvl.getBlockState(q).isAir()) continue;
            lvl.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
            lvl.sendParticles(ParticleTypes.SOUL, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.02);
            lvl.sendParticles(ParticleTypes.WHITE_ASH, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.02);
            n++;
        }
        if (n > 0) {
            BlockPos c = w.centro();
            lvl.playSound(null, c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5, DarkCraftMod.SFX_MURO_ILUSORIO.get(), SoundSource.BLOCKS, 1.8f, 1.0f);
            lvl.playSound(null, c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0f, 0.5f);
            lvl.sendParticles(ParticleTypes.LARGE_SMOKE, c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5, 20, 0.8, 0.8, 0.4, 0.01);
            lvl.sendParticles(ParticleTypes.ENCHANT, c.getX() + 0.5, c.getY() + 0.8, c.getZ() + 0.5, 60, 1.0, 1.0, 0.5, 0.6);
        }
        if (quien != null) Logros.dar(quien, "secreto");
        Datos d = datos(lvl.getServer());
        boolean nuevo = d.abiertos.add(w.id());
        if (nuevo) {
            d.setDirty();
            ABIERTOS++;
            if (quien != null)
                quien.displayClientMessage(Component.translatable("darkcraft.morvain.muro_ilusorio").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
            DarkCraftMod.LOGGER.info("[DarkCraft] Se deshace el muro ilusorio {} ({} bloques)", w.id(), n);
        }
        return nuevo || n > 0;
    }

    /** Para las pruebas: las veces que se ha abierto un muro. */
    public static int ABIERTOS;

    /** Cada segundo con alguien cerca (lo llama {@link Morvain#onTick}): lo que ya se abrio no vuelve a aparecer. */
    public static void tick(ServerLevel lvl) {
        Datos d = datos(lvl.getServer());
        if (d.abiertos.isEmpty()) return;
        for (Muro w : todos()) {
            if (!d.abiertos.contains(w.id())) continue;
            for (BlockPos q : w.celdas())
                if (lvl.isLoaded(q) && !lvl.getBlockState(q).isAir()) lvl.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /** Para las pruebas y el comando: vuelve a cerrar un muro (sus bloques y lo guardado). */
    public static void cerrar(ServerLevel lvl, String id) {
        Muro w = muro(id);
        if (w == null) return;
        for (BlockPos q : w.celdas()) if (lvl.isLoaded(q)) lvl.setBlock(q, bloque(w, q), 3);
        Datos d = datos(lvl.getServer());
        if (d.abiertos.remove(id)) d.setDirty();
    }

    /** Como estan (para la prueba y el comando): cuantos bloques le quedan a cada uno y si esta abierto. */
    public static String estado(ServerLevel lvl) {
        StringBuilder b = new StringBuilder();
        Datos d = datos(lvl.getServer());
        for (Muro w : todos()) {
            int hay = 0;
            for (BlockPos q : w.celdas()) if (lvl.isLoaded(q) && !lvl.getBlockState(q).isAir()) hay++;
            if (b.length() > 0) b.append("; ");
            b.append(w.id()).append(' ').append(hay).append('/').append(w.celdas().size()).append(d.abiertos.contains(w.id()) ? " (abierto)" : "");
        }
        return b.toString();
    }
}
