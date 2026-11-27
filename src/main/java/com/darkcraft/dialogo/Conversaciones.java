package com.darkcraft.dialogo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Guia;
import com.darkcraft.personajes.Herrero;
import com.darkcraft.personajes.Mercader;
import com.darkcraft.personajes.PersonajeBase;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Rol;
import com.darkcraft.santuario.Santuarios;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Las conversaciones, en el servidor: quien habla con quien (de cerca y sin pelear), por donde empieza segun lo que ya se
 * haya hablado (en la ficha del jugador, {@link Ficha#dialogo}) y lo que pasa al acabar ciertas frases o al elegir ciertas
 * respuestas (con la guia, subir de nivel en su santuario; con el herrero, la forja; con el mercader, la tienda).
 */
public final class Conversaciones {
    private Conversaciones() {}

    /** Hasta donde se puede hablar con alguien (bloques). */
    public static final double ALCANCE = 4.5;

    // marcas de lo hablado con la guia (8: ya hablo con ella en Morvain y le dio el silbato)
    private static final int GUIA_CONOCIDA = 1, GUIA_REY = 2, GUIA_GAVRAN = 4, GUIA_MORVAIN = 8, GUIA_CAPILLA = 16;
    // con los demas: ya se presento
    private static final int CONOCIDO = 1;

    public static void hablar(ServerPlayer sp, int entidad) {
        Entity e = sp.level().getEntity(entidad);
        if (e instanceof PersonajeBase p) hablar(sp, p);
    }

    public static void hablar(ServerPlayer sp, PersonajeBase p) {
        if (!sp.isAlive() || sp.isSpectator() || sp.distanceToSqr(p) > (ALCANCE + 1) * (ALCANCE + 1)) return;
        Dialogos.Personaje d = Dialogos.de(p.dialogo());
        if (d == null) return;
        if (!sp.isCreative() && Rol.enCombate(sp) && !(p instanceof com.darkcraft.morvain.Corazon)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.dialogo.combate", 0));
            return;
        }
        int nodo = p instanceof Guia g ? inicioGuia(sp, g) : p instanceof com.darkcraft.morvain.Corazon ? com.darkcraft.morvain.Aguja.inicio(sp) : inicio(sp, d.id());
        Net.to(sp, new Net.Dialogo(p.getId(), d.id(), nodo));
        String logro = switch (d.id()) {
            case "herrero" -> "dorvan";
            case "mercader" -> "kerrick";
            case "sombra" -> null;
            default -> "maelis";
        };
        if (logro != null) Logros.dar(sp, logro);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} habla con {} (nodo {})", sp.getGameProfile().getName(), d.id(), nodo);
    }

    /**
     * Por donde empieza la guia: la primera vez se presenta; luego saluda (y una vez, habla del Rey y de Gavran). En Morvain,
     * la primera vez le da el silbato del corcel (y otra vez si lo ha perdido) y despues saluda a su manera. En la capilla de la
     * torre del homenaje (parte 7ab), la primera vez 15 (y 16); despues 16, o 14 con la reina ya caida.
     */
    private static int inicioGuia(ServerPlayer sp, Guia guia) {
        int m = Ficha.dialogo(sp, "guia");
        int nodo;
        boolean morvain = com.darkcraft.morvain.Morvain.es(sp.level());
        boolean capilla = morvain && com.darkcraft.morvain.Homenaje.enCapilla(guia.getX(), guia.getY() + 0.5, guia.getZ());
        if (capilla) {
            if ((m & GUIA_CAPILLA) == 0) {
                m |= GUIA_CAPILLA | GUIA_CONOCIDA;
                nodo = 15;
            } else nodo = com.darkcraft.morvain.CasaSombra.reinaCaida(sp.serverLevel()) ? 14 : 16;
        } else if (morvain && (m & GUIA_MORVAIN) == 0) {
            m |= GUIA_MORVAIN | GUIA_CONOCIDA;
            nodo = 11;
        } else if (morvain && !com.darkcraft.montura.Montura.tieneSilbato(sp)) {
            nodo = 12;
        } else if ((m & GUIA_CONOCIDA) == 0) {
            m |= GUIA_CONOCIDA;
            nodo = 1;
        } else if ((m & GUIA_GAVRAN) == 0 && Logros.tiene(sp, Logros.GAVRAN)) {
            m |= GUIA_GAVRAN | GUIA_REY;
            nodo = 10;
        } else if ((m & GUIA_REY) == 0 && Logros.tiene(sp, Logros.REY)) {
            m |= GUIA_REY;
            nodo = 9;
        } else nodo = morvain ? 14 : 8;
        Ficha.dialogo(sp, "guia", m);
        return nodo;
    }

    /** Los demas: la primera vez se presentan (1) y luego saludan (2). */
    private static int inicio(ServerPlayer sp, String id) {
        int m = Ficha.dialogo(sp, id);
        if ((m & CONOCIDO) != 0) return 2;
        Ficha.dialogo(sp, id, m | CONOCIDO);
        return 1;
    }

    /** Lo que pide el cliente al acabar una frase o elegir una respuesta con accion. */
    public static void accion(ServerPlayer sp, int entidad, String accion) {
        Entity e = sp.level().getEntity(entidad);
        if (!(e instanceof PersonajeBase) || sp.distanceToSqr(e) > (ALCANCE + 3) * (ALCANCE + 3)) return;
        if ("nivel".equals(accion) && e instanceof Guia g) {
            BlockPos s = g.santuario();
            if (s == null || !Santuarios.permitir(sp, s)) {
                s = Santuarios.encendidoCerca(sp.serverLevel(), g.blockPosition(), 8);
                if (s == null || !Santuarios.permitir(sp, s)) {
                    Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.dialogo.sin_santuario", 0));
                    return;
                }
                g.santuario(s);
            }
            Net.to(sp, new Net.AbrirNivel());
        } else if ("forja".equals(accion) && e instanceof Herrero) {
            Net.to(sp, new Net.AbrirForja(entidad));
        } else if ("tienda".equals(accion) && e instanceof Mercader) {
            Net.to(sp, new Net.AbrirTienda(entidad, com.darkcraft.tienda.Tienda.comprados(sp)));
        } else if ("silbato".equals(accion) && e instanceof Guia) {
            darSilbato(sp);
        } else if (("golpear".equals(accion) || "tomar".equals(accion)) && e instanceof com.darkcraft.morvain.Corazon c) {
            // la respuesta a la Sombra (parte 7ac)
            com.darkcraft.morvain.Aguja.elegir(sp, c, "golpear".equals(accion));
        }
    }

    /** El silbato del corcel (de la guia, en Morvain): si no lo lleva ya. */
    public static void darSilbato(ServerPlayer sp) {
        if (com.darkcraft.montura.Montura.tieneSilbato(sp) && !sp.isCreative()) return;
        com.darkcraft.rol.Equipo.dar(sp, new net.minecraft.world.item.ItemStack(DarkCraftMod.SILBATO_CORCEL.get()));
        Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.corcel.como", 0));
        Logros.dar(sp, "silbato");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} recibe el silbato del corcel", sp.getGameProfile().getName());
    }

    /** Para el comando y la prueba: olvida lo hablado con un personaje ("guia", "herrero", "mercader"). */
    public static void olvidar(ServerPlayer sp, String id) {
        Ficha.dialogo(sp, id, 0);
    }

    public static void olvidar(ServerPlayer sp) {
        olvidar(sp, "guia");
    }
}
