package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.personajes.PersonajeBase;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * El corazon de la Sombra (parte 7ac): sale de su pecho cuando se rinde y flota delante de ella, latiendo, hasta que alguien responde.
 * Se habla con el como con un personaje (la tecla del aviso o clic derecho: su dialogo, {@code "sombra"}, con las respuestas golpear,
 * tomar o callar) y golpearlo con el arma es responder "golpear" (despues de que acabe de ofrecerlo). No se guarda con el mundo: lo
 * pone {@link Aguja} mientras la Sombra espera.
 */
public class Corazon extends PersonajeBase {
    static final DustParticleOptions SANGRE = new DustParticleOptions(new Vector3f(0.55f, 0.02f, 0.04f), 0.9f);

    public Corazon(EntityType<? extends Corazon> t, Level l) {
        super(t, l);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return PersonajeBase.atributos();
    }

    @Override
    public String dialogo() {
        return "sombra";
    }

    @Override
    protected float vista() {
        return 6f;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0, 0, 0);
        if (level().isClientSide) {
            // gotea y suelta brasas; cada latido, una bocanada
            if (random.nextInt(3) == 0) level().addParticle(SANGRE, getX() + (random.nextDouble() - 0.5) * 0.3, getY() + 0.15, getZ() + (random.nextDouble() - 0.5) * 0.3, 0, -0.05, 0);
            if (tickCount % 24 == 0)
                for (int k = 0; k < 6; k++)
                    level().addParticle(SANGRE, getX(), getY() + 0.45, getZ(), (random.nextDouble() - 0.5) * 0.15, (random.nextDouble() - 0.5) * 0.15, (random.nextDouble() - 0.5) * 0.15);
            return;
        }
        if (tickCount % 24 == 0) level().playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_SOMBRA_LATIDO.get(), SoundSource.HOSTILE, 1.1f, 1.0f);
        // si la Sombra ya no espera, fuera
        if (tickCount % 20 == 5 && !Aguja.puedeElegir()) discard();
    }

    /** Latido (0-1) para el dibujante: un golpe doble cada 24 ticks. */
    public float latido(float pt) {
        float t = ((tickCount + pt) % 24f) / 24f;
        float a = (float) Math.exp(-Math.pow((t - 0.05f) / 0.05f, 2)), b = (float) Math.exp(-Math.pow((t - 0.22f) / 0.06f, 2));
        return Math.min(1f, a + 0.7f * b);
    }

    /** Golpearlo con el arma es responder "golpear" (cuando ya ha acabado de ofrecerlo). */
    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (!level().isClientSide && s.getEntity() instanceof ServerPlayer p && !p.isSpectator() && Aguja.puedeElegir()
                && tickCount > Math.round(com.darkcraft.rey.Voces.sombra(30) * 20) + com.darkcraft.enemigos.Sombra.OFRECE - com.darkcraft.enemigos.Sombra.SALE_CORAZON + 10)
            Aguja.elegir(p, this, true);
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }
}
