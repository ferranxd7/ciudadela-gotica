package com.darkcraft.hechizos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

/**
 * Un hechizo que vuela: el guijarro y la lanza de sombra, las esquirlas (que buscan al enemigo) y la lanza de rayo. No
 * tiene modelo: se ve por sus particulas (las pone el cliente a lo largo de lo que avanza en cada tick, y el impacto lo
 * manda el servidor), asi se ve igual con shaders. Vuela recto (sin caer) hasta 3 s.
 */
public class ProyectilHechizo extends ThrowableProjectile {
    private static final EntityDataAccessor<Byte> TIPO = SynchedEntityData.defineId(ProyectilHechizo.class, EntityDataSerializers.BYTE);
    private static final DustParticleOptions SOMBRA = new DustParticleOptions(new Vector3f(0.42f, 0.5f, 1f), 1.1f),
            SOMBRA_GRANDE = new DustParticleOptions(new Vector3f(0.36f, 0.42f, 1f), 1.7f),
            RAYO = new DustParticleOptions(new Vector3f(1f, 0.9f, 0.35f), 1.2f);

    private float dano;
    private final Set<Integer> tocados = new HashSet<>();
    private int edad;

    public ProyectilHechizo(EntityType<? extends ProyectilHechizo> t, Level l) {
        super(t, l);
    }

    public ProyectilHechizo(Level l, LivingEntity dueno, Hechizo h, float dano) {
        super(DarkCraftMod.PROYECTIL.get(), dueno, l);
        this.entityData.set(TIPO, (byte) h.ordinal());
        this.dano = dano;
    }

    public Hechizo hechizo() {
        Hechizo[] hs = Hechizo.values();
        return hs[Math.max(0, Math.min(hs.length - 1, entityData.get(TIPO)))];
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TIPO, (byte) 0);
    }

    @Override
    protected float getGravity() {
        return 0f;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void tick() {
        Vec3 antes = position();
        super.tick();
        if (level().isClientSide) {
            particulas(antes, position());
            return;
        }
        if (++edad > 60) {
            discard();
            return;
        }
        // las esquirlas buscan al enemigo mas cercano que tengan delante
        if (hechizo() == Hechizo.ESQUIRLAS && edad > 3 && getOwner() instanceof Player duenno) {
            LivingEntity mejor = null;
            double nota = 1e9;
            Vec3 v = getDeltaMovement();
            for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(position(), position()).inflate(10),
                    x -> com.darkcraft.armas.Combos.valido(duenno, x) && !tocados.contains(x.getId()))) {
                Vec3 d = le.position().add(0, le.getBbHeight() * 0.5, 0).subtract(position());
                if (d.normalize().dot(v.normalize()) < 0.3) continue;
                if (d.lengthSqr() < nota) {
                    nota = d.lengthSqr();
                    mejor = le;
                }
            }
            if (mejor != null) {
                Vec3 d = mejor.position().add(0, mejor.getBbHeight() * 0.5, 0).subtract(position()).normalize().scale(v.length());
                setDeltaMovement(v.scale(0.8).add(d.scale(0.2)).normalize().scale(v.length()));
            }
        }
    }

    /** La estela, repartida por lo que ha avanzado en este tick (no a saltos). */
    private void particulas(Vec3 a, Vec3 b) {
        Hechizo h = hechizo();
        int n = h == Hechizo.LANZA ? 7 : h == Hechizo.ESQUIRLAS ? 2 : 4;
        for (int i = 0; i < n; i++) {
            Vec3 p = a.lerp(b, i / (double) n);
            double j = h == Hechizo.LANZA ? 0.12 : 0.05;
            double ox = (random.nextDouble() - 0.5) * j, oy = (random.nextDouble() - 0.5) * j, oz = (random.nextDouble() - 0.5) * j;
            switch (h) {
                case LANZA -> level().addParticle(SOMBRA_GRANDE, p.x + ox, p.y + oy, p.z + oz, 0, 0, 0);
                case RAYO -> {
                    level().addParticle(RAYO, p.x + ox, p.y + oy, p.z + oz, 0, 0, 0);
                    if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, ox, oy, oz);
                }
                default -> level().addParticle(SOMBRA, p.x + ox, p.y + oy, p.z + oz, 0, 0, 0);
            }
        }
        if (h != Hechizo.ESQUIRLAS && random.nextInt(2) == 0) level().addParticle(ParticleTypes.END_ROD, b.x, b.y, b.z, 0, 0, 0);
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !tocados.contains(e.getId()) && (!(getOwner() instanceof Player p) || com.darkcraft.armas.Combos.valido(p, e));
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide || !(r.getEntity() instanceof LivingEntity le)) return;
        tocados.add(le.getId());
        Hechizo h = hechizo();
        DamageSource fuente = h == Hechizo.RAYO
                ? new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), this, getOwner())
                : damageSources().indirectMagic(this, getOwner());
        le.invulnerableTime = 0;
        if (le.hurt(fuente, dano)) {
            Vec3 v = getDeltaMovement().normalize();
            le.knockback(h == Hechizo.LANZA ? 0.6 : 0.25, -v.x, -v.z);
        }
        impacto();
        // la lanza de sombra atraviesa; lo demas se deshace al dar
        if (h != Hechizo.LANZA) discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult r) {
        super.onHitBlock(r);
        if (level().isClientSide) return;
        impacto();
        discard();
    }

    private void impacto() {
        if (!(level() instanceof ServerLevel sl)) return;
        Hechizo h = hechizo();
        Vec3 p = position();
        if (h == Hechizo.RAYO) {
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 24, 0.4, 0.4, 0.4, 0.3);
            sl.sendParticles(RAYO, p.x, p.y, p.z, 12, 0.3, 0.3, 0.3, 0.02);
        } else {
            sl.sendParticles(h == Hechizo.LANZA ? SOMBRA_GRANDE : SOMBRA, p.x, p.y, p.z, h == Hechizo.LANZA ? 26 : 12, 0.3, 0.3, 0.3, 0.02);
            sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 6, 0.2, 0.2, 0.2, 0.06);
        }
        sl.playSound(null, p.x, p.y, p.z, h == Hechizo.RAYO ? DarkCraftMod.SFX_HECHIZO_RAYO.get() : DarkCraftMod.SFX_HECHIZO_IMPACTO.get(), SoundSource.PLAYERS,
                h == Hechizo.ESQUIRLAS ? 0.5f : 0.9f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putFloat("dano", dano);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        dano = t.getFloat("dano");
    }
}
