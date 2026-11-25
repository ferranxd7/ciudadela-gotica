package com.darkcraft.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.dialogo.Conversaciones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Dorvan, el herrero de Valdrenn: de pie delante de su yunque, martilleando sin parar (un golpe cada {@link #CICLO}
 * ticks, con chispas y el martillazo); cuando alguien se le acerca deja el martillo quieto y le mira. Mejora las armas
 * ({@code forja.Forja}). Todo lo del martilleo lo lleva el cliente (cada uno el suyo: no hace falta que vaya a la par).
 */
public class Herrero extends PersonajeBase {
    /** Lo que dura un martillazo (ticks) y el tick del golpe en el yunque. */
    public static final int CICLO = 30, GOLPE = 20;
    /** A esta distancia de un jugador deja de martillear y le mira. */
    public static final double ATENCION = Conversaciones.ALCANCE + 0.1;

    // en el cliente: por donde va el martillazo y cuanto atiende (0 martilleando, 1 mirando a alguien)
    public int ciclo;
    public float atencion, atencionO;

    public Herrero(EntityType<? extends Herrero> t, Level l) {
        super(t, l);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(DarkCraftMod.MARTILLO_HERRERO.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    @Override
    public String dialogo() {
        return "herrero";
    }

    @Override
    protected float vista() {
        return (float) ATENCION;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) return;
        atencionO = atencion;
        boolean cerca = level().getNearestPlayer(this, ATENCION) != null;
        atencion = Mth.approach(atencion, cerca ? 1f : 0f, 0.12f);
        if (atencion > 0.02f) return;
        ciclo++;
        if (ciclo % CICLO == GOLPE) golpe();
    }

    /** Donde da el martillo: encima del yunque, que esta un bloque por delante. */
    public Vec3 puntoGolpe() {
        double a = Math.toRadians(yBodyRot);
        return new Vec3(getX() - Math.sin(a) * 0.62 - Math.cos(a) * 0.12, getY() + 1.02, getZ() + Math.cos(a) * 0.62 - Math.sin(a) * 0.12);
    }

    private void golpe() {
        Vec3 p = puntoGolpe();
        for (int i = 0; i < 4; i++)
            level().addParticle(ParticleTypes.SMALL_FLAME, p.x, p.y, p.z, (random.nextDouble() - 0.5) * 0.12, 0.04 + random.nextDouble() * 0.08,
                    (random.nextDouble() - 0.5) * 0.12);
        if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.LAVA, p.x, p.y, p.z, 0, 0, 0);
        level().playLocalSound(p.x, p.y, p.z, DarkCraftMod.SFX_HERRERO_GOLPE.get(), SoundSource.NEUTRAL, 0.55f, 0.9f + random.nextFloat() * 0.2f, false);
    }

    /** Pone a Dorvan en {@code pos} mirando hacia {@code yunque}. */
    public static Herrero poner(ServerLevel lvl, BlockPos pos, BlockPos yunque) {
        return poner(DarkCraftMod.HERRERO.get(), lvl, pos, yunque, "darkcraft_herrero", null);
    }
}
