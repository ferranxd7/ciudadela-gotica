package com.darkcraft.tienda;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.dialogo.Conversaciones;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Mercader;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Rol;
import com.darkcraft.story.Logros;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * La tienda de Kerrick: lo que vende (igual en el servidor y en el cliente), con su precio en ecos y, algunas cosas, un
 * limite por jugador (lo comprado va en la ficha, {@link Ficha#comprado}). El servidor cobra y da lo comprado (lo que no
 * quepa, al suelo).
 */
public final class Tienda {
    private Tienda() {}

    /** Lo que contesta el servidor al comprar. */
    public static final int HECHO = 0, FALTA = 1, AGOTADO = 2, NO = 3;
    /** Lo mas que se compra de una vez (de lo que no tiene limite). */
    public static final int TANDA = 20;

    /** Una cosa a la venta: el objeto, cuantos da cada compra, su precio y cuantas se pueden comprar (0 = sin limite). */
    public record Oferta(String id, int cantidad, int precio, int limite) {
        public Item item() {
            Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            return it == null ? Items.AIR : it;
        }

        public ItemStack muestra() {
            return new ItemStack(item(), cantidad);
        }

        public boolean limitada() {
            return limite > 0;
        }
    }

    /** La mercancia de Kerrick. */
    public static final List<Oferta> KERRICK = List.of(
            new Oferta("darkcraft:piedra_forja_1", 1, 220, 0),
            new Oferta("darkcraft:piedra_forja_2", 1, 760, 6),
            new Oferta("darkcraft:fruto_ceniza", 1, 350, 0),
            new Oferta("darkcraft:fruto_pluma", 1, 300, 0),
            new Oferta("darkcraft:fruto_brasa", 1, 450, 0),
            new Oferta("darkcraft:fruto_escarcha", 1, 450, 0),
            new Oferta("minecraft:bow", 1, 600, 1),
            new Oferta("minecraft:arrow", 10, 90, 0),
            new Oferta("minecraft:iron_sword", 1, 900, 1),
            new Oferta("minecraft:iron_axe", 1, 900, 1),
            new Oferta("darkcraft:baston", 1, 1600, 1),
            new Oferta("darkcraft:sello", 1, 1600, 1),
            new Oferta("darkcraft:hechizo_guijarro", 1, 1200, 1),
            new Oferta("darkcraft:hechizo_llama", 1, 1200, 1),
            new Oferta("darkcraft:hechizo_curar", 1, 2400, 1),
            new Oferta("darkcraft:talisman_porteador", 1, 3500, 1));

    /** Lo ya comprado de cada oferta (para mandarlo al cliente al abrir la tienda). */
    public static int[] comprados(ServerPlayer sp) {
        int[] r = new int[KERRICK.size()];
        for (int i = 0; i < r.length; i++) r[i] = Ficha.comprado(sp, KERRICK.get(i).id());
        return r;
    }

    /** Lo que se puede comprar aun de una oferta (Integer.MAX_VALUE sin limite). */
    public static int quedan(Oferta o, int comprado) {
        return o.limitada() ? Math.max(0, o.limite() - comprado) : Integer.MAX_VALUE;
    }

    /** El jugador compra {@code veces} la oferta {@code i} a Kerrick ({@code entidad}). */
    public static void comprar(ServerPlayer sp, int entidad, int i, int veces) {
        int codigo = NO, comprado = 0;
        Entity e = sp.level().getEntity(entidad);
        if (e instanceof Mercader && sp.distanceToSqr(e) <= (Conversaciones.ALCANCE + 3) * (Conversaciones.ALCANCE + 3)
                && i >= 0 && i < KERRICK.size() && veces >= 1 && veces <= TANDA) {
            Oferta o = KERRICK.get(i);
            comprado = Ficha.comprado(sp, o.id());
            if (o.item() == Items.AIR) codigo = NO;
            else if (quedan(o, comprado) < veces) codigo = AGOTADO;
            else {
                int precio = o.precio() * veces;
                boolean gasta = Rol.gasta(sp);
                if (gasta && !Rol.pagar(sp, precio)) codigo = FALTA;
                else {
                    int n = o.cantidad() * veces;
                    while (n > 0) {
                        ItemStack st = new ItemStack(o.item(), Math.min(n, o.item().getMaxStackSize()));
                        n -= st.getCount();
                        com.darkcraft.rol.Equipo.dar(sp, st);
                    }
                    if (o.limitada()) {
                        comprado += veces;
                        Ficha.comprado(sp, o.id(), comprado);
                    }
                    codigo = HECHO;
                    sp.level().playSound(null, e.getX(), e.getY() + 1, e.getZ(), DarkCraftMod.SFX_TIENDA_COMPRA.get(), SoundSource.NEUTRAL, 0.8f, 1.0f);
                    Logros.dar(sp, "compra");
                    DarkCraftMod.LOGGER.info("[DarkCraft] {} compra {} x {} a Kerrick por {} ecos", sp.getGameProfile().getName(), veces, o.id(),
                            gasta ? precio : 0);
                }
            }
        }
        Net.to(sp, new Net.TiendaHecho(entidad, i, comprado, codigo));
    }

    /** Para el comando y la prueba: olvida lo comprado. */
    public static void olvidar(ServerPlayer sp) {
        for (Oferta o : KERRICK) Ficha.comprado(sp, o.id(), 0);
    }
}
