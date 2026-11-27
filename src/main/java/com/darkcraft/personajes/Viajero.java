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
 * lleva {@code mision.Misiones}. Quien es ({@link Quien}, sincronizado y guardado): <b>Oswin</b>, el peregrino ciego de la
 * Encrucijada de los Peregrinos, de pie apoyado en su bordon (cuando le devuelven su campana, la alza y la toca: {@link #tocar}), y
 * <b>Hedda</b> (parte 7ah), la madre que teje en el camino de la Puerta Oeste de la Capital, sentada en su taburete; la misma Hedda
 * de rodillas junto a los huesos de su hijo, en la sala de la guardia, es la {@link #variante} 1 (la que solo ve quien le dijo la
 * verdad). <b>Ylva</b> (parte 7an), la hija del ballenero, sentada en un noray al final del espigon del Puerto Varado con su farol de
 * fuego frio en el regazo. <b>Cada jugador lleva su mision</b>: a quien ya no le toca ver a uno, su cliente no lo dibuja ni lo deja mirar
 * ({@link #OCULTO}, lo pone el cliente) y el servidor no le deja hablar con el ({@code Misiones.visible}).
 */
public class Viajero extends PersonajeBase {
    /** Lo que dura alzar y tocar la campana (ticks). */
    public static final int TOCA = 60;
    /** El evento de entidad de tocar la campana. */
    public static final byte EV_TOCA = 61;

    /** Quien es: el id de su dialogo (y de sus voces, sus textos, su piel y su retrato). */
    public enum Quien {
        OSWIN("oswin"), HEDDA("hedda"), YLVA("ylva");

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
    /** Su postura o su sitio: 0 el de siempre; 1 la otra (Hedda de rodillas junto a Tam). */
    private static final EntityDataAccessor<Integer> VARIANTE = SynchedEntityData.defineId(Viajero.class, EntityDataSerializers.INT);

    /**
     * En el cliente: si a quien juega ya no le toca ver a este (lo pone {@code client.mision.ClientMisiones}; en el servidor no se
     * mira: en un mundo de un jugador comparten la JVM).
     */
    public static volatile java.util.function.Predicate<Viajero> OCULTO = v -> false;

    /** En el cliente: desde cuando toca la campana (-1 si no). */
    public int tocaDesde = -1000;

    public Viajero(EntityType<? extends Viajero> t, Level l) {
        super(t, l);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(QUIEN, 0);
        entityData.define(VARIANTE, 0);
    }

    public Quien quien() {
        return Quien.de(entityData.get(QUIEN));
    }

    public void quien(Quien q) {
        entityData.set(QUIEN, q.ordinal());
    }

    public int variante() {
        return entityData.get(VARIANTE);
    }

    public void variante(int n) {
        entityData.set(VARIANTE, n);
    }

    /** A quien ya no le toca verle, su cliente no le deja mirarle (ni hablarle). */
    @Override
    public boolean isPickable() {
        return !(level().isClientSide && OCULTO.test(this)) && super.isPickable();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putString("quien", quien().id);
        t.putInt("variante", variante());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        Quien q = Quien.porId(t.getString("quien"));
        quien(q == null ? Quien.OSWIN : q);
        variante(t.getInt("variante"));
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
        return poner(lvl, pos, mira, q, 0);
    }

    /** Lo mismo con su variante (la etiqueta lleva el numero detras si no es la 0: darkcraft_viajero_hedda_1). */
    public static Viajero poner(ServerLevel lvl, BlockPos pos, BlockPos mira, Quien q, int variante) {
        return poner(DarkCraftMod.VIAJERO.get(), lvl, pos, mira, "darkcraft_viajero_" + q.id + (variante == 0 ? "" : "_" + variante), v -> {
            v.quien(q);
            v.variante(variante);
        });
    }
}
