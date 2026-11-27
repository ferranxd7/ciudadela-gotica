package com.darkcraft.personajes;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Los que dan misiones por Morvain (parte 7ag, respuestas 95 y 117 del plan: "personajes con misiones" y "a veces 2-3 respuestas
 * que cambian la mision"). Estan quietos en su sitio como los demas personajes; lo que se habla con ellos y la mision que dan lo
 * lleva {@code mision.Misiones}. Por ahora uno ({@link Quien}, sincronizado y guardado): <b>Oswin</b>, el peregrino ciego de la
 * Encrucijada de los Peregrinos, de pie apoyado en su bordon, con su sombrero de ala ancha y la concha de peregrino; cuando le
 * devuelven su campana, la alza y la toca ({@link #tocar}).
 */
public class Viajero extends PersonajeBase {
    /** Lo que dura alzar y tocar la campana (ticks). */
    public static final int TOCA = 60;
    /** El evento de entidad de tocar la campana. */
    public static final byte EV_TOCA = 61;

    /** Quien es: el id de su dialogo (y de sus voces, sus textos, su piel y su retrato). */
    public enum Quien {
        OSWIN("oswin");

        public final String id;

        Quien(String id) {
            this.id = id;
        }

        public static Quien de(int n) {
            Quien[] v = values();
            return v[Mth.clamp(n, 0, v.length - 1)];
        }

        public static Quien porId(String id) {
            for (Quien q : values()) if (q.id.equals(id) || q.name().equalsIgnoreCase(id)) return q;
            return null;
        }
    }

    private static final EntityDataAccessor<Integer> QUIEN = SynchedEntityData.defineId(Viajero.class, EntityDataSerializers.INT);

    /** En el cliente: desde cuando toca la campana (-1 si no). */
    public int tocaDesde = -1000;

    public Viajero(EntityType<? extends Viajero> t, Level l) {
        super(t, l);
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
        quien(q == null ? Quien.OSWIN : q);
    }

    @Override
    public String dialogo() {
        return quien().id;
    }

    /** Alza la campana y la toca (lo ven todos los de cerca; el sonido lo pone quien lo llama). */
    public void tocar() {
        level().broadcastEntityEvent(this, EV_TOCA);
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == EV_TOCA) tocaDesde = tickCount;
        else super.handleEntityEvent(b);
    }

    /** Lo alzada que esta la campana ahora (0 a 1 a 0), en el cliente. */
    public float toca(float pt) {
        float t = tickCount + pt - tocaDesde;
        if (t < 0 || t > TOCA) return 0f;
        float k = t < 12 ? t / 12f : t > TOCA - 14 ? (TOCA - t) / 14f : 1f;
        k = Mth.clamp(k, 0f, 1f);
        return k * k * (3 - 2 * k);
    }

    /** Pone a uno en {@code pos} mirando hacia {@code mira} (su etiqueta: darkcraft_viajero_oswin...). */
    public static Viajero poner(ServerLevel lvl, BlockPos pos, BlockPos mira, Quien q) {
        return poner(DarkCraftMod.VIAJERO.get(), lvl, pos, mira, "darkcraft_viajero_" + q.id, v -> v.quien(q));
    }
}
