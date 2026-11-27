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
 * Los mercaderes ambulantes: de pie junto a los fuegos con su mochila enorme (sombrero de ala ancha, manta enrollada, una
 * olla y un farol colgando). Venden con ecos ({@code tienda.Tienda}). Dos ({@link Quien}, sincronizado y guardado): Kerrick,
 * en Valdrenn (parte 2e-2), y Silas, en Morvain (parte 7af), que va de santuario en santuario ({@code morvain.Ambulante}:
 * apunta en {@link #sitio} el santuario junto al que esta).
 */
public class Mercader extends PersonajeBase {
    /** Quien es: el id de su dialogo (y de sus voces, sus textos, su piel y su retrato) y el de su mercancia. */
    public enum Quien {
        KERRICK("mercader"), SILAS("silas");

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

    private static final EntityDataAccessor<Integer> QUIEN = SynchedEntityData.defineId(Mercader.class, EntityDataSerializers.INT);
    /** El santuario de Morvain junto al que esta (Silas; "" los demas). */
    private String sitio = "";

    public Mercader(EntityType<? extends Mercader> t, Level l) {
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

    public String sitio() {
        return sitio;
    }

    public void sitio(String s) {
        sitio = s == null ? "" : s;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putString("quien", quien().id);
        if (!sitio.isEmpty()) t.putString("sitio", sitio);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        Quien q = Quien.porId(t.getString("quien"));
        quien(q == null ? Quien.KERRICK : q);
        sitio = t.getString("sitio");
    }

    @Override
    public String dialogo() {
        return quien().id;
    }

    @Override
    protected float vista() {
        return 8f;
    }

    /** Pone a Kerrick en {@code pos} mirando hacia {@code mira}. */
    public static Mercader poner(ServerLevel lvl, BlockPos pos, BlockPos mira) {
        return poner(DarkCraftMod.MERCADER.get(), lvl, pos, mira, "darkcraft_mercader", null);
    }

    /** Pone a Silas en {@code pos} mirando hacia {@code mira}, junto al santuario {@code sitio}. */
    public static Mercader ponerSilas(ServerLevel lvl, BlockPos pos, BlockPos mira, String sitio) {
        return poner(DarkCraftMod.MERCADER.get(), lvl, pos, mira, "darkcraft_silas", m -> {
            m.quien(Quien.SILAS);
            m.sitio(sitio);
            m.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET));
            m.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0f);
        });
    }
}
