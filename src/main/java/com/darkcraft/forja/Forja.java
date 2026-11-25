package com.darkcraft.forja;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.combate.Armas;
import com.darkcraft.combate.Escalado;
import com.darkcraft.dialogo.Conversaciones;
import com.darkcraft.hechizos.Catalizador;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Herrero;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Rol;
import com.darkcraft.story.Logros;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * La forja de Dorvan: mejorar armas y catalizadores de +0 a +{@link #MAX}. Cada mejora da un {@link #POR_NIVEL} mas de
 * ataque (en {@link Escalado#total}, sobre el base y el escalado; en los catalizadores, sobre lo que pegan y curan los
 * hechizos) y pide piedras de forja y ecos: a +1, +2 y +3, una, dos y tres piedras de grado I; a +4..+6, de grado II; a
 * +7..+9, de grado III. La mejora va en el NBT del objeto ({@value #NBT}). Lo usan el servidor (que es el que forja) y el
 * cliente (la pantalla de la forja, el nombre "+3" y lo que pega en el menu).
 */
public final class Forja {
    private Forja() {}

    public static final int MAX = 9;
    public static final float POR_NIVEL = 0.08f;
    public static final String NBT = "darkcraft_mejora";
    /** Lo que contesta el servidor al forjar. */
    public static final int HECHO = 0, FALTA = 1, TOPE = 2, NO = 3;

    public static int nivel(ItemStack st) {
        return st.isEmpty() || !st.hasTag() ? 0 : Mth.clamp(st.getTag().getInt(NBT), 0, MAX);
    }

    public static void nivel(ItemStack st, int n) {
        if (st.isEmpty()) return;
        if (n <= 0) {
            if (st.hasTag()) st.getTag().remove(NBT);
        } else st.getOrCreateTag().putInt(NBT, Math.min(MAX, n));
    }

    /** Lo que multiplica la mejora (1 sin mejorar, 1,72 a +9). */
    public static float mult(int nivel) {
        return 1f + POR_NIVEL * nivel;
    }

    public static float mult(ItemStack st) {
        return mult(nivel(st));
    }

    /** Las armas del combate (no los punos) y los catalizadores. */
    public static boolean mejorable(ItemStack st) {
        if (st.isEmpty()) return false;
        if (Catalizador.de(st) != null) return true;
        return Escalado.de(st) != null && Armas.de(st) != Armas.Tipo.PUNOS;
    }

    /** El grado de las piedras que pide subir a {@code destino} (1, 2 o 3) y cuantas. */
    public static int grado(int destino) {
        return Mth.clamp((destino - 1) / 3 + 1, 1, 3);
    }

    public static int piedras(int destino) {
        return (destino - 1) % 3 + 1;
    }

    /** Los ecos que cuesta subir a {@code destino}: 275, 450, 675... 3075 a +9 (12 925 de +0 a +9). */
    public static int ecos(int destino) {
        return 150 + 100 * destino + 25 * destino * destino;
    }

    public static Item piedra(int grado) {
        return switch (grado) {
            case 1 -> DarkCraftMod.PIEDRA_FORJA_1.get();
            case 2 -> DarkCraftMod.PIEDRA_FORJA_2.get();
            default -> DarkCraftMod.PIEDRA_FORJA_3.get();
        };
    }

    /** El nombre del objeto con su mejora ("Espada de hierro +3"). */
    public static MutableComponent nombre(ItemStack st) {
        MutableComponent c = st.getHoverName().copy();
        int n = nivel(st);
        return n > 0 ? c.append(Component.literal(" +" + n)) : c;
    }

    // ------------------------------------------------------------------ servidor

    /** El jugador pide mejorar lo que tiene en el hueco {@code hueco} de su inventario, junto a Dorvan ({@code entidad}). */
    public static void mejorar(ServerPlayer sp, int entidad, int hueco) {
        int codigo = NO, nivel = 0;
        Entity e = sp.level().getEntity(entidad);
        Inventory inv = sp.getInventory();
        if (e instanceof Herrero h && sp.distanceToSqr(h) <= (Conversaciones.ALCANCE + 3) * (Conversaciones.ALCANCE + 3)
                && hueco >= 0 && hueco < inv.getContainerSize()) {
            ItemStack st = inv.getItem(hueco);
            nivel = nivel(st);
            if (!mejorable(st)) codigo = NO;
            else if (nivel >= MAX) codigo = TOPE;
            else {
                int d = nivel + 1;
                Item piedra = piedra(grado(d));
                boolean gasta = Rol.gasta(sp);
                if (gasta && (inv.countItem(piedra) < piedras(d) || Ficha.ecos(sp) < ecos(d))) codigo = FALTA;
                else {
                    if (gasta) {
                        quitar(inv, piedra, piedras(d));
                        Rol.pagar(sp, ecos(d));
                    }
                    nivel(st, d);
                    nivel = d;
                    inv.setChanged();
                    codigo = HECHO;
                    chispas(sp.serverLevel(), h);
                    Logros.dar(sp, "mejora");
                    if (d == MAX) Logros.dar(sp, "mejora_maxima");
                    DarkCraftMod.LOGGER.info("[DarkCraft] {} mejora {} a +{} ({} x {} y {} ecos)", sp.getGameProfile().getName(),
                            st.getItem(), d, piedras(d), piedra, gasta ? ecos(d) : 0);
                }
            }
        }
        Net.to(sp, new Net.ForjaHecho(entidad, hueco, nivel, codigo));
    }

    private static void quitar(Inventory inv, Item it, int n) {
        for (int i = 0; i < inv.getContainerSize() && n > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.is(it)) continue;
            int q = Math.min(n, s.getCount());
            s.shrink(q);
            n -= q;
        }
    }

    private static void chispas(ServerLevel sl, Herrero h) {
        Vec3 p = h.puntoGolpe();
        sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_FORJA_MEJORA.get(), SoundSource.NEUTRAL, 0.9f, 1.0f);
        sl.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 6, 0.15, 0.05, 0.15, 0.0);
        sl.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.1, p.z, 14, 0.2, 0.15, 0.2, 0.03);
        sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.2, p.z, 10, 0.25, 0.3, 0.25, 0.04);
    }

    /** Para el comando y la prueba: deja el arma de la mano derecha en +n. */
    public static void poner(ServerPlayer sp, int n) {
        ItemStack st = sp.getMainHandItem();
        if (!mejorable(st)) return;
        nivel(st, n);
        sp.getInventory().setChanged();
    }

    /** La linea de la ficha del objeto con su mejora (si la tiene). */
    public static Component linea(ItemStack st) {
        int n = nivel(st);
        return Component.translatable("darkcraft.forja.mejora_linea", n, MAX).withStyle(n >= MAX ? ChatFormatting.GOLD : ChatFormatting.GRAY);
    }
}
