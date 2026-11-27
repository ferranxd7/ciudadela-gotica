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
 * <p>
 * Las piezas de armadura con modelo propio (parte 7am, respuesta 24 del plan: los ecos sirven para "mejorar armas y
 * armaduras") van de +0 a +{@link #MAX_ARMADURA}: cada mejora, un {@link #POR_NIVEL_ARMADURA} mas de defensa, {@link
 * #DUREZA_POR_NIVEL} de dureza y un {@link #RESISTE_POR_NIVEL} mas de lo que resiste de cada elemento
 * ({@code PiezaArmadura}); piden una y dos piedras de grado I, una y dos de grado II y una de grado III, y los mismos ecos.
 */
public final class Forja {
    private Forja() {}

    public static final int MAX = 9;
    public static final float POR_NIVEL = 0.08f;
    public static final String NBT = "darkcraft_mejora";
    /** Las armaduras (parte 7am): hasta +5; cada mejora, +10 % de defensa, +0,25 de dureza y +8 % de lo que resiste. */
    public static final int MAX_ARMADURA = 5;
    public static final float POR_NIVEL_ARMADURA = 0.10f, DUREZA_POR_NIVEL = 0.25f, RESISTE_POR_NIVEL = 0.08f;
    /** Lo que piden las armaduras para subir a +1..+5: el grado de las piedras y cuantas. */
    private static final int[] GRADO_ARMADURA = {1, 1, 2, 2, 3}, PIEDRAS_ARMADURA = {1, 2, 1, 2, 1};
    /** Lo que contesta el servidor al forjar (CENIZA: se ha puesto una ceniza de guerra; QUITADA: se le ha quitado). */
    public static final int HECHO = 0, FALTA = 1, TOPE = 2, NO = 3, CENIZA = 4, QUITADA = 5;

    /** Si es una pieza de armadura con modelo propio (se mejora hasta +{@value #MAX_ARMADURA}). */
    public static boolean esArmadura(ItemStack st) {
        return !st.isEmpty() && st.getItem() instanceof com.darkcraft.armadura.PiezaArmadura;
    }

    /** Hasta donde se mejora: las armas y los catalizadores +9; las armaduras +5. */
    public static int max(ItemStack st) {
        return esArmadura(st) ? MAX_ARMADURA : MAX;
    }

    public static int nivel(ItemStack st) {
        return st.isEmpty() || !st.hasTag() ? 0 : Mth.clamp(st.getTag().getInt(NBT), 0, max(st));
    }

    public static void nivel(ItemStack st, int n) {
        if (st.isEmpty()) return;
        if (n <= 0) {
            if (st.hasTag()) st.getTag().remove(NBT);
        } else st.getOrCreateTag().putInt(NBT, Math.min(max(st), n));
    }

    /** Lo que multiplica la mejora (1 sin mejorar, 1,72 a +9). */
    public static float mult(int nivel) {
        return 1f + POR_NIVEL * nivel;
    }

    public static float mult(ItemStack st) {
        return mult(nivel(st));
    }

    /** Las armas del combate (no los punos), los catalizadores y las piezas de armadura con modelo propio (parte 7am). */
    public static boolean mejorable(ItemStack st) {
        if (st.isEmpty()) return false;
        if (Catalizador.de(st) != null || esArmadura(st)) return true;
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

    /** El grado de las piedras que pide subir ese objeto a {@code destino} (las armaduras: I, I, II, II, III). */
    public static int grado(ItemStack st, int destino) {
        return esArmadura(st) ? GRADO_ARMADURA[Mth.clamp(destino - 1, 0, MAX_ARMADURA - 1)] : grado(destino);
    }

    /** Cuantas piedras pide subir ese objeto a {@code destino} (las armaduras: 1, 2, 1, 2, 1). */
    public static int piedras(ItemStack st, int destino) {
        return esArmadura(st) ? PIEDRAS_ARMADURA[Mth.clamp(destino - 1, 0, MAX_ARMADURA - 1)] : piedras(destino);
    }

    /** Los ecos que cuesta subir ese objeto a {@code destino} (los mismos para todo: 3625 una pieza de +0 a +5). */
    public static int ecos(ItemStack st, int destino) {
        return ecos(destino);
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
            else if (nivel >= max(st)) codigo = TOPE;
            else {
                int d = nivel + 1;
                Item piedra = piedra(grado(st, d));
                int pide = piedras(st, d), cuesta = ecos(st, d);
                boolean gasta = Rol.gasta(sp);
                if (gasta && (inv.countItem(piedra) < pide || Ficha.ecos(sp) < cuesta)) codigo = FALTA;
                else {
                    if (gasta) {
                        quitar(inv, piedra, pide);
                        Rol.pagar(sp, cuesta);
                    }
                    nivel(st, d);
                    nivel = d;
                    inv.setChanged();
                    codigo = HECHO;
                    chispas(sp.serverLevel(), h);
                    if (esArmadura(st)) Logros.dar(sp, "mejora_armadura");
                    else {
                        Logros.dar(sp, "mejora");
                        if (d == MAX) Logros.dar(sp, "mejora_maxima");
                    }
                    DarkCraftMod.LOGGER.info("[DarkCraft] {} mejora {} a +{} ({} x {} y {} ecos)", sp.getGameProfile().getName(),
                            st.getItem(), d, pide, piedra, gasta ? cuesta : 0);
                }
            }
        }
        Net.to(sp, new Net.ForjaHecho(entidad, hueco, nivel, codigo));
    }

    /**
     * Ponerle al arma del hueco {@code arma} la ceniza de guerra del hueco {@code ceniza} (o, con -1, quitarle la que
     * lleve: vuelve a su habilidad de siempre). La ceniza que tuviera antes vuelve a la bolsa; la nueva se gasta.
     */
    public static void ceniza(ServerPlayer sp, int entidad, int arma, int ceniza) {
        int codigo = NO;
        Entity e = sp.level().getEntity(entidad);
        Inventory inv = sp.getInventory();
        if (e instanceof Herrero h && sp.distanceToSqr(h) <= (Conversaciones.ALCANCE + 3) * (Conversaciones.ALCANCE + 3)
                && arma >= 0 && arma < inv.getContainerSize() && ceniza >= -1 && ceniza < inv.getContainerSize()) {
            ItemStack st = inv.getItem(arma);
            com.darkcraft.combate.Habilidad antes = com.darkcraft.combate.Habilidad.puesta(st);
            if (com.darkcraft.combate.Habilidad.admite(st)) {
                if (ceniza < 0) {
                    if (antes != null) {
                        com.darkcraft.combate.Habilidad.poner(st, null);
                        com.darkcraft.rol.Equipo.dar(sp, new ItemStack(DarkCraftMod.CENIZAS.get(antes).get()));
                        codigo = QUITADA;
                    }
                } else {
                    ItemStack c = inv.getItem(ceniza);
                    com.darkcraft.combate.Habilidad nueva = CenizaGuerra.de(c);
                    if (nueva != null && nueva.vale(Armas.de(st)) && nueva != com.darkcraft.combate.Habilidad.de(st)) {
                        c.shrink(1);
                        com.darkcraft.combate.Habilidad.poner(st, nueva);
                        if (antes != null) com.darkcraft.rol.Equipo.dar(sp, new ItemStack(DarkCraftMod.CENIZAS.get(antes).get()));
                        codigo = CENIZA;
                        Logros.dar(sp, "ceniza");
                    }
                }
                if (codigo != NO) {
                    inv.setChanged();
                    Vec3 p = h.puntoGolpe();
                    ServerLevel sl = sp.serverLevel();
                    sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_CENIZA_APLICAR.get(), SoundSource.NEUTRAL, 0.9f, 1.0f);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y + 0.1, p.z, 16, 0.2, 0.2, 0.2, 0.03);
                    sl.sendParticles(ParticleTypes.ASH, p.x, p.y + 0.3, p.z, 30, 0.4, 0.4, 0.4, 0.02);
                    DarkCraftMod.LOGGER.info("[DarkCraft] {} {} la ceniza de {}: {}", sp.getGameProfile().getName(),
                            codigo == CENIZA ? "pone" : "quita", st.getItem(), com.darkcraft.combate.Habilidad.de(st));
                }
            }
        }
        Net.to(sp, new Net.ForjaHecho(entidad, arma, nivel(inv.getItem(Math.max(0, Math.min(inv.getContainerSize() - 1, arma)))), codigo));
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

    /** Para el comando y la prueba (parte 7am): deja las piezas de armadura que lleva puestas en +n (cuantas). */
    public static int ponerArmadura(ServerPlayer sp, int n) {
        int k = 0;
        for (ItemStack st : sp.getArmorSlots())
            if (esArmadura(st)) {
                nivel(st, n);
                k++;
            }
        sp.getInventory().setChanged();
        return k;
    }

    /** La linea de la ficha del objeto con su mejora (si la tiene). */
    public static Component linea(ItemStack st) {
        int n = nivel(st), m = max(st);
        return Component.translatable("darkcraft.forja.mejora_linea", n, m).withStyle(n >= m ? ChatFormatting.GOLD : ChatFormatting.GRAY);
    }
}
