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
 * Las tiendas de los mercaderes: lo que vende cada uno (igual en el servidor y en el cliente), con su precio en ecos y,
 * algunas cosas, un limite por jugador (lo comprado va en la ficha, {@link Ficha#comprado}; lo de Silas con su prefijo, para
 * que sus limites no se mezclen con los de Kerrick). El servidor cobra y da lo comprado (lo que no quepa, al suelo).
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
            new Oferta("darkcraft:talisman_porteador", 1, 3500, 1),
            new Oferta("darkcraft:ceniza_parada", 1, 2200, 1),
            new Oferta("darkcraft:ceniza_llamas", 1, 2800, 1),
            new Oferta("darkcraft:ceniza_escarcha", 1, 2800, 1));

    /**
     * La mercancia de Silas (parte 7af), el mercader ambulante de Morvain: lo de alli, mas caro (piedras II sin limite y III,
     * los frutos que faltaban, hechizos, talismanes y cenizas que Kerrick no tiene).
     */
    public static final List<Oferta> SILAS = List.of(
            new Oferta("darkcraft:piedra_forja_2", 1, 820, 0),
            new Oferta("darkcraft:piedra_forja_3", 1, 2600, 6),
            new Oferta("darkcraft:fruto_tormenta", 1, 450, 0),
            new Oferta("darkcraft:fruto_lunar", 1, 450, 0),
            new Oferta("darkcraft:fruto_hueso", 1, 400, 0),
            new Oferta("darkcraft:fruto_sombra", 1, 500, 0),
            new Oferta("minecraft:arrow", 10, 90, 0),
            new Oferta("darkcraft:hechizo_esquirlas", 1, 2400, 1),
            new Oferta("darkcraft:hechizo_lanza", 1, 3200, 1),
            new Oferta("darkcraft:hechizo_rayo", 1, 3600, 1),
            new Oferta("darkcraft:hechizo_bendicion", 1, 3000, 1),
            new Oferta("darkcraft:talisman_aliento", 1, 4200, 1),
            new Oferta("darkcraft:talisman_mente", 1, 4200, 1),
            new Oferta("darkcraft:talisman_codicia", 1, 6000, 1),
            new Oferta("darkcraft:ceniza_paso", 1, 3000, 1),
            new Oferta("darkcraft:ceniza_relampago", 1, 3400, 1),
            new Oferta("darkcraft:ceniza_sangre", 1, 3400, 1));

    /** Lo que vende el mercader {@code id} ("mercader" Kerrick, "silas" Silas). */
    public static List<Oferta> de(String id) {
        return "silas".equals(id) ? SILAS : KERRICK;
    }

    /** Donde se apunta lo comprado de una oferta en la ficha (lo de Kerrick, con su id de siempre). */
    public static String clave(String mercader, Oferta o) {
        return "silas".equals(mercader) ? "silas:" + o.id() : o.id();
    }

    /** Lo ya comprado de cada oferta de Kerrick (para mandarlo al cliente al abrir la tienda). */
    public static int[] comprados(ServerPlayer sp) {
        return comprados(sp, "mercader");
    }

    /** Lo ya comprado de cada oferta del mercader {@code id}. */
    public static int[] comprados(ServerPlayer sp, String id) {
        List<Oferta> l = de(id);
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) r[i] = Ficha.comprado(sp, clave(id, l.get(i)));
        return r;
    }

    /** Lo que se puede comprar aun de una oferta (Integer.MAX_VALUE sin limite). */
    public static int quedan(Oferta o, int comprado) {
        return o.limitada() ? Math.max(0, o.limite() - comprado) : Integer.MAX_VALUE;
    }

    /** El jugador compra {@code veces} la oferta {@code i} al mercader {@code entidad} (Kerrick o Silas). */
    public static void comprar(ServerPlayer sp, int entidad, int i, int veces) {
        int codigo = NO, comprado = 0;
        Entity e = sp.level().getEntity(entidad);
        String quien = e instanceof Mercader m ? m.dialogo() : "mercader";
        List<Oferta> lista = de(quien);
        if (e instanceof Mercader && sp.distanceToSqr(e) <= (Conversaciones.ALCANCE + 3) * (Conversaciones.ALCANCE + 3)
                && i >= 0 && i < lista.size() && veces >= 1 && veces <= TANDA) {
            Oferta o = lista.get(i);
            String clave = clave(quien, o);
            comprado = Ficha.comprado(sp, clave);
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
                        Ficha.comprado(sp, clave, comprado);
                    }
                    codigo = HECHO;
                    sp.level().playSound(null, e.getX(), e.getY() + 1, e.getZ(), DarkCraftMod.SFX_TIENDA_COMPRA.get(), SoundSource.NEUTRAL, 0.8f, 1.0f);
                    Logros.dar(sp, "compra");
                    if ("silas".equals(quien)) Logros.dar(sp, "silas_compra");
                    DarkCraftMod.LOGGER.info("[DarkCraft] {} compra {} x {} a {} por {} ecos", sp.getGameProfile().getName(), veces, o.id(),
                            "silas".equals(quien) ? "Silas" : "Kerrick", gasta ? precio : 0);
                }
            }
        }
        Net.to(sp, new Net.TiendaHecho(entidad, i, comprado, codigo));
    }

    /** Para el comando y la prueba: olvida lo comprado (a los dos). */
    public static void olvidar(ServerPlayer sp) {
        for (Oferta o : KERRICK) Ficha.comprado(sp, clave("mercader", o), 0);
        for (Oferta o : SILAS) Ficha.comprado(sp, clave("silas", o), 0);
    }
}
