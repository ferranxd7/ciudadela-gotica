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
    private static final int GUIA_CONOCIDA = 1, GUIA_REY = 2, GUIA_GAVRAN = 4, GUIA_MORVAIN = 8, GUIA_CAPILLA = 16, GUIA_EPILOGO = 32;
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
        // a quien ya no le toca ver a quien da una mision (cada uno lleva la suya: parte 7ah), no se le habla
        if (p instanceof com.darkcraft.personajes.Viajero v && !com.darkcraft.mision.Misiones.visible(sp, v)) return;
        if (!sp.isCreative() && Rol.enCombate(sp) && !(p instanceof com.darkcraft.morvain.Corazon)
                && !(p instanceof Guia && com.darkcraft.morvain.Epilogo.pendiente(sp))) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.dialogo.combate", 0));
            return;
        }
        int nodo = p instanceof Guia g ? inicioGuia(sp, g) : p instanceof com.darkcraft.morvain.Corazon ? com.darkcraft.morvain.Aguja.inicio(sp)
                // quien da una mision (parte 7ag): segun como la lleve
                : p instanceof com.darkcraft.personajes.Viajero v ? com.darkcraft.mision.Misiones.inicio(sp, v) : inicio(sp, d.id());
        Net.to(sp, new Net.Dialogo(p.getId(), d.id(), nodo));
        String logro = switch (d.id()) {
            case "herrero" -> "dorvan";
            case "mercader" -> "kerrick";
            // los de Morvain (parte 7af): Hagen y Silas, el suyo; los cuatro herreros, el de todos
            case "hagen", "silas" -> d.id();
            case "brenna", "ulrik", "tobin", "sombra", "oswin", "hedda" -> null;
            default -> "maelis";
        };
        if (logro != null) Logros.dar(sp, logro);
        if (p instanceof Herrero h && h.quien().morvain() && herrerosMorvain(sp) == HERREROS_MORVAIN.length) Logros.dar(sp, "herreros");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} habla con {} (nodo {})", sp.getGameProfile().getName(), d.id(), nodo);
    }

    /**
     * Por donde empieza la guia: la primera vez se presenta; luego saluda (y una vez, habla del Rey y de Gavran). En Morvain,
     * la primera vez le da el silbato del corcel (y otra vez si lo ha perdido) y despues saluda a su manera. En la capilla de la
     * torre del homenaje (parte 7ab), la primera vez 15 (y 16); despues 16, o 14 con la reina ya caida. Despues del final (parte 7ad),
     * una vez, su epilogo: el alba 18-19-20, la nueva Sombra 22-23 (quien no vio la cinematica, desde la 17 o la 21).
     */
    private static int inicioGuia(ServerPlayer sp, Guia guia) {
        int m = Ficha.dialogo(sp, "guia");
        int nodo;
        // despues del final (parte 7ad), una vez: su epilogo (quien vio la cinematica ya oyo la 17 o la 21 en ella)
        String fin = com.darkcraft.morvain.Aguja.final_(sp.server);
        boolean vio = com.darkcraft.morvain.Epilogo.pendiente(sp);
        if (fin != null && ((m & GUIA_EPILOGO) == 0 || vio)) {
            Ficha.dialogo(sp, "guia", m | GUIA_EPILOGO | GUIA_CONOCIDA);
            com.darkcraft.morvain.Epilogo.alHablar(sp, fin);
            return fin.equals("sombra") ? (vio ? 22 : 21) : (vio ? 18 : 17);
        }
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

    /** Los herreros de Morvain (parte 7af). */
    public static final String[] HERREROS_MORVAIN = {"hagen", "brenna", "ulrik", "tobin"};

    /** Con cuantos de los herreros de Morvain ha hablado ya (para su logro). */
    public static int herrerosMorvain(ServerPlayer sp) {
        int n = 0;
        for (String id : HERREROS_MORVAIN) if ((Ficha.dialogo(sp, id) & CONOCIDO) != 0) n++;
        return n;
    }

    /** Los demas: la primera vez se presentan (1) y luego saludan (2). */
    private static int inicio(ServerPlayer sp, String id) {
        int m = Ficha.dialogo(sp, id);
        if ((m & CONOCIDO) != 0) return 2;
        Ficha.dialogo(sp, id, m | CONOCIDO);
        return 1;
    }

    /** Para la prueba: por donde empezaria a hablar con ese personaje (y lo apunta, como al hablar con el). */
    public static int nodoInicio(ServerPlayer sp, String id) {
        return inicio(sp, id);
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
        } else if ("tienda".equals(accion) && e instanceof Mercader m) {
            Net.to(sp, new Net.AbrirTienda(entidad, com.darkcraft.tienda.Tienda.comprados(sp, m.dialogo())));
        } else if ("rumbo".equals(accion) && e instanceof Mercader m && m.quien() == Mercader.Quien.SILAS) {
            // adonde ira Silas despues (parte 7af)
            com.darkcraft.morvain.Ambulante.rumbo(sp);
        } else if ("silbato".equals(accion) && e instanceof Guia) {
            darSilbato(sp);
        } else if (e instanceof com.darkcraft.personajes.Viajero v) {
            // lo que se le responde a quien da una mision (parte 7ag)
            com.darkcraft.mision.Misiones.accion(sp, v, accion);
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

    /** Para el comando y la prueba: olvida lo hablado con un personaje ("guia", "herrero", "mercader", "hagen", "silas"...). */
    public static void olvidar(ServerPlayer sp, String id) {
        Ficha.dialogo(sp, id, 0);
    }

    public static void olvidar(ServerPlayer sp) {
        olvidar(sp, "guia");
    }

    /** Para la prueba: por donde empezaria Maelis con ese jugador (y lo apunta, como al hablar con ella). */
    public static int nodoGuia(ServerPlayer sp, Guia g) {
        return inicioGuia(sp, g);
    }

    /** Que Maelis le vuelva a decir su epilogo (parte 7ad). */
    public static void olvidarEpilogo(ServerPlayer sp) {
        Ficha.dialogo(sp, "guia", Ficha.dialogo(sp, "guia") & ~GUIA_EPILOGO);
    }
}
