package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * La espada de luz que se ve en el mundo: una entidad {@code item_display} con el modelo de {@code darkcraft:espada_luz}
 * clavada en el suelo, con luz propia y contorno dorado (se ve igual con shaders). Solo es la imagen de lo que guarda la
 * ficha del jugador: se pone cuando su dueno anda cerca y se quita cuando se aleja, se va o la recoge; nunca se guarda
 * con el mundo (si quedara alguna en el disco, no vuelve a entrar). Las chispas y el zumbido solo los nota su dueno.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Espadas {
    private Espadas() {}

    static final String TAG = "darkcraft_espada_luz";
    private static final double CERCA = 96;
    private static final float ALTO = 1.7f, HUNDIDA = 0.34f, INCLINADA = 0.17f;
    private static final DustColorTransitionOptions POLVO = new DustColorTransitionOptions(new Vector3f(1.0f, 0.93f, 0.62f),
            new Vector3f(0.95f, 0.62f, 0.16f), 0.7f);

    private static final Map<UUID, Display.ItemDisplay> VISTAS = new HashMap<>();

    /** Cada medio segundo: que la espada del jugador este (o no) a la vista, y sus chispas y su zumbido. */
    static void vista(ServerPlayer sp) {
        Ficha.Espada e = Ficha.espada(sp);
        Display.ItemDisplay d = VISTAS.get(sp.getUUID());
        boolean debe = e != null && sp.isAlive() && e.dim() == sp.level().dimension() && e.distancia2(sp) < CERCA * CERCA
                && sp.serverLevel().hasChunkAt(BlockPos.containing(e.x(), e.y(), e.z()));
        if (!debe) {
            if (d != null) quitar(sp);
            return;
        }
        ServerLevel sl = sp.serverLevel();
        if (d == null || d.isRemoved() || d.level() != sl) {
            if (d != null) d.discard();
            d = crear(sl, e, sp.getUUID());
            if (d == null) return;
            VISTAS.put(sp.getUUID(), d);
        }
        // chispas doradas que suben (solo para su dueno)
        sl.sendParticles(sp, POLVO, true, e.x(), e.y() + 0.9, e.z(), 3, 0.16, 0.45, 0.16, 0.0);
        if (sp.tickCount % 20 == 4) sl.sendParticles(sp, ParticleTypes.END_ROD, true, e.x(), e.y() + 0.6, e.z(), 1, 0.12, 0.3, 0.12, 0.012);
        if (sp.tickCount % 80 == 4 && e.distancia2(sp) < 20 * 20)
            sp.connection.send(new ClientboundSoundPacket(ForgeRegistries.SOUND_EVENTS.getHolder(DarkCraftMod.SFX_ESPADA_LUZ.get()).orElseThrow(),
                    SoundSource.AMBIENT, e.x(), e.y() + 1, e.z(), 0.55f, 1.0f, sl.getRandom().nextLong()));
    }

    private static Display.ItemDisplay crear(ServerLevel sl, Ficha.Espada e, UUID dueno) {
        Display.ItemDisplay d = EntityType.ITEM_DISPLAY.create(sl);
        if (d == null) return null;
        // cada espada, girada a su manera (siempre igual para el mismo sitio)
        long h = Double.doubleToLongBits(e.x()) * 31 + Double.doubleToLongBits(e.z());
        float giro = (float) ((h >>> 12 & 1023) / 1023.0 * Math.PI * 2);
        Matrix4f m = new Matrix4f().translate(0f, 0.5f * ALTO * (float) Math.cos(INCLINADA) - HUNDIDA, 0f)
                .rotateY(giro).rotateZ(INCLINADA).scale(ALTO);
        CompoundTag tag = new CompoundTag();
        tag.put("item", new ItemStack(DarkCraftMod.ESPADA_LUZ.get()).save(new CompoundTag()));
        tag.putString("item_display", "none");
        ListTag l = new ListTag();
        for (int i = 0; i < 16; i++) l.add(FloatTag.valueOf(m.getRowColumn(i >> 2, i & 3)));
        tag.put("transformation", l);
        CompoundTag luz = new CompoundTag();
        luz.putInt("sky", 15);
        luz.putInt("block", 15);
        tag.put("brightness", luz);
        tag.putFloat("view_range", 1.6f);
        tag.putInt("glow_color_override", 0xFFD98A);
        d.load(tag);
        d.moveTo(e.x(), e.y(), e.z(), 0f, 0f);
        d.setGlowingTag(true);
        d.addTag(TAG);
        sl.addFreshEntity(d);
        return d;
    }

    static void quitar(ServerPlayer sp) {
        Display.ItemDisplay d = VISTAS.remove(sp.getUUID());
        if (d != null) d.discard();
    }

    /** La recoge: la espada se deshace en luz. */
    static void recoger(ServerPlayer sp, Ficha.Espada e) {
        quitar(sp);
        ServerLevel sl = sp.serverLevel();
        sl.sendParticles(ParticleTypes.END_ROD, e.x(), e.y() + 0.9, e.z(), 26, 0.15, 0.5, 0.15, 0.06);
        sl.sendParticles(POLVO, e.x(), e.y() + 0.9, e.z(), 30, 0.25, 0.6, 0.25, 0.0);
        sl.sendParticles(ParticleTypes.FLASH, e.x(), e.y() + 1.0, e.z(), 1, 0, 0, 0, 0);
    }

    static void olvidar() {
        VISTAS.clear();
    }

    /** La espada de un jugador (para la prueba de la CI). */
    public static Display.ItemDisplay de(UUID jugador) {
        return VISTAS.get(jugador);
    }

    /** Una espada que se quedo guardada en el disco (el mundo se cerro con ella puesta) no vuelve a entrar. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent ev) {
        if (ev.getLevel().isClientSide() || !ev.loadedFromDisk()) return;
        if (ev.getEntity() instanceof Display && ev.getEntity().getTags().contains(TAG)) ev.setCanceled(true);
    }
}
