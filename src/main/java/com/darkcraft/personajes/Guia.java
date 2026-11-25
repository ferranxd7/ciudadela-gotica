package com.darkcraft.personajes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Maelis, la guia de los santuarios: sentada en el suelo junto a los santuarios importantes, con un farol de almas en el
 * regazo (lo comun de los personajes, en {@link PersonajeBase}). Guarda el santuario al que acompana (para subir de nivel
 * con ella).
 */
public class Guia extends PersonajeBase {
    private BlockPos santuario;

    public Guia(EntityType<? extends Guia> t, Level l) {
        super(t, l);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SOUL_LANTERN));
        setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    @Override
    public String dialogo() {
        return "guia";
    }

    public BlockPos santuario() {
        return santuario;
    }

    public void santuario(BlockPos p) {
        santuario = p == null ? null : p.immutable();
    }

    @Override
    public void tick() {
        super.tick();
        // de vez en cuando, una mota azul del farol
        if (level().isClientSide && random.nextInt(9) == 0) {
            double a = Math.toRadians(yBodyRot);
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() - Math.sin(a) * 0.45 + (random.nextDouble() - 0.5) * 0.1,
                    getY() + 0.4, getZ() + Math.cos(a) * 0.45 + (random.nextDouble() - 0.5) * 0.1, 0, 0.012, 0);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        if (santuario != null) t.put("santuario", NbtUtils.writeBlockPos(santuario));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        if (t.contains("santuario")) santuario = NbtUtils.readBlockPos(t.getCompound("santuario"));
    }

    /** Pone a la guia sentada en {@code pos}, mirando hacia {@code mira}. */
    public static Guia poner(ServerLevel lvl, BlockPos pos, BlockPos mira, BlockPos santuario) {
        return poner(com.darkcraft.DarkCraftMod.GUIA.get(), lvl, pos, mira, "darkcraft_guia", g -> g.santuario(santuario));
    }
}
