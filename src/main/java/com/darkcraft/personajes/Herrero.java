package com.darkcraft.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.dialogo.Conversaciones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Los herreros: de pie delante de su yunque, martilleando sin parar (un golpe por martillazo, con chispas y el martillazo);
 * cuando alguien se le acerca dejan el martillo quieto y le miran. Mejoran las armas ({@code forja.Forja}). Todo lo del
 * martilleo lo lleva el cliente (cada uno el suyo: no hace falta que vaya a la par).
 * <p>
 * Son cinco ({@link Quien}, sincronizado con el cliente y guardado): Dorvan, el de Valdrenn (parte 2e-2), y los de Morvain
 * (parte 7af): Hagen en el Refugio de la Fragua, Brenna junto al Faro de los Huesos, Ulrik en el Paso del Colmillo y Tobin
 * a la puerta de la Capital. Cada uno con su piel, su voz, su dialogo y su ritmo: Ulrik pega despacio y fuerte, Tobin deprisa
 * y con prisas; cada pocos martillazos se paran a mirar la pieza que sujetan con las tenazas ({@link #mirando}).
 */
public class Herrero extends PersonajeBase {
    /** A esta distancia de un jugador deja de martillear y le mira. */
    public static final double ATENCION = Conversaciones.ALCANCE + 0.1;
    /** Cada cuantos martillazos se para a mirar la pieza (uno de cada tantos). */
    public static final int CADA_MIRA = 5;

    /**
     * Quien es: el id de su dialogo (y de sus voces, sus textos, su piel y su retrato), lo que dura su martillazo (ticks), el tick
     * del golpe en el yunque, lo fuerte que suena y lo grande que es.
     */
    public enum Quien {
        DORVAN("herrero", 30, 20, 0.55f, 1.0f),
        HAGEN("hagen", 34, 23, 0.6f, 1.0f),
        BRENNA("brenna", 28, 19, 0.5f, 0.96f),
        ULRIK("ulrik", 40, 28, 0.7f, 1.08f),
        TOBIN("tobin", 22, 15, 0.45f, 0.92f);

        public final String id;
        public final int ciclo, golpe;
        public final float fuerza, escala;

        Quien(String id, int ciclo, int golpe, float fuerza, float escala) {
            this.id = id;
            this.ciclo = ciclo;
            this.golpe = golpe;
            this.fuerza = fuerza;
            this.escala = escala;
        }

        public static Quien de(int n) {
            Quien[] v = values();
            return v[Mth.clamp(n, 0, v.length - 1)];
        }

        public static Quien porId(String id) {
            for (Quien q : values()) if (q.id.equals(id) || q.name().equalsIgnoreCase(id)) return q;
            return null;
        }

        /** Los de Morvain (parte 7af). */
        public boolean morvain() {
            return this != DORVAN;
        }
    }

    /** Lo que dura un martillazo de Dorvan (ticks) y el tick del golpe en el yunque (los de los demas, en {@link Quien}). */
    public static final int CICLO = 30, GOLPE = 20;

    private static final EntityDataAccessor<Integer> QUIEN = SynchedEntityData.defineId(Herrero.class, EntityDataSerializers.INT);

    // en el cliente: por donde va el martillazo y cuanto atiende (0 martilleando, 1 mirando a alguien)
    public int ciclo;
    public float atencion, atencionO;

    public Herrero(EntityType<? extends Herrero> t, Level l) {
        super(t, l);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(DarkCraftMod.MARTILLO_HERRERO.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(QUIEN, 0);
    }

    public Quien quien() {
        return Quien.de(entityData.get(QUIEN));
    }

    public void quien(Quien q) {
        entityData.set(QUIEN, q.ordinal());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putString("quien", quien().id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        Quien q = Quien.porId(t.getString("quien"));
        quien(q == null ? Quien.DORVAN : q);
    }

    @Override
    public String dialogo() {
        return quien().id;
    }

    @Override
    protected float vista() {
        return (float) ATENCION;
    }

    /** Si en este martillazo se para a mirar la pieza (no golpea). */
    public boolean mirando() {
        return mirando(ciclo);
    }

    public boolean mirando(int c) {
        return (c / quien().ciclo) % CADA_MIRA == CADA_MIRA - 1;
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
        Quien q = quien();
        if (ciclo % q.ciclo == q.golpe && !mirando()) golpe();
    }

    /** Donde da el martillo: encima del yunque, que esta un bloque por delante. */
    public Vec3 puntoGolpe() {
        double a = Math.toRadians(yBodyRot);
        return new Vec3(getX() - Math.sin(a) * 0.62 - Math.cos(a) * 0.12, getY() + 1.02, getZ() + Math.cos(a) * 0.62 - Math.sin(a) * 0.12);
    }

    private void golpe() {
        Vec3 p = puntoGolpe();
        Quien q = quien();
        int chispas = q == Quien.ULRIK ? 7 : q == Quien.TOBIN ? 3 : 4;
        for (int i = 0; i < chispas; i++)
            level().addParticle(ParticleTypes.SMALL_FLAME, p.x, p.y, p.z, (random.nextDouble() - 0.5) * 0.12, 0.04 + random.nextDouble() * 0.08,
                    (random.nextDouble() - 0.5) * 0.12);
        if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.LAVA, p.x, p.y, p.z, 0, 0, 0);
        float tono = q == Quien.ULRIK ? 0.78f : q == Quien.TOBIN ? 1.08f : q == Quien.BRENNA ? 0.98f : 0.9f;
        level().playLocalSound(p.x, p.y, p.z, DarkCraftMod.SFX_HERRERO_GOLPE.get(), SoundSource.NEUTRAL, q.fuerza, tono + random.nextFloat() * 0.2f, false);
    }

    /** Pone a Dorvan en {@code pos} mirando hacia {@code yunque}. */
    public static Herrero poner(ServerLevel lvl, BlockPos pos, BlockPos yunque) {
        return poner(lvl, pos, yunque, Quien.DORVAN);
    }

    /** Pone a un herrero en {@code pos} mirando hacia {@code yunque} (su etiqueta: darkcraft_herrero o darkcraft_herrero_hagen...). */
    public static Herrero poner(ServerLevel lvl, BlockPos pos, BlockPos yunque, Quien q) {
        return poner(DarkCraftMod.HERRERO.get(), lvl, pos, yunque, q == Quien.DORVAN ? "darkcraft_herrero" : "darkcraft_herrero_" + q.id,
                h -> h.quien(q));
    }
}
