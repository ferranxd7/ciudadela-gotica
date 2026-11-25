package com.darkcraft.dialogo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Guia;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Rol;
import com.darkcraft.santuario.Santuarios;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Las conversaciones, en el servidor: quien habla con quien (de cerca y sin pelear), por donde empieza segun lo que ya se
 * haya hablado (en la ficha del jugador, {@link Ficha#dialogo}) y lo que pasa al acabar ciertas frases (con la guia,
 * subir de nivel en su santuario).
 */
public final class Conversaciones {
    private Conversaciones() {}

    /** Hasta donde se puede hablar con alguien (bloques). */
    public static final double ALCANCE = 4.5;

    // marcas de lo hablado con la guia
    private static final int GUIA_CONOCIDA = 1, GUIA_REY = 2, GUIA_GAVRAN = 4;

    public static void hablar(ServerPlayer sp, int entidad) {
        Entity e = sp.level().getEntity(entidad);
        if (e instanceof Guia g) hablar(sp, g);
    }

    public static void hablar(ServerPlayer sp, Guia g) {
        if (!sp.isAlive() || sp.isSpectator() || sp.distanceToSqr(g) > (ALCANCE + 1) * (ALCANCE + 1)) return;
        if (!sp.isCreative() && Rol.enCombate(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.dialogo.combate", 0));
            return;
        }
        int nodo = inicioGuia(sp);
        Net.to(sp, new Net.Dialogo(g.getId(), Dialogos.GUIA.id(), nodo));
        Logros.dar(sp, "maelis");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} habla con la guia (nodo {})", sp.getGameProfile().getName(), nodo);
    }

    /** Por donde empieza la guia: la primera vez se presenta; luego saluda (y una vez, habla del Rey y de Gavran). */
    private static int inicioGuia(ServerPlayer sp) {
        int m = Ficha.dialogo(sp, "guia");
        int nodo;
        if ((m & GUIA_CONOCIDA) == 0) {
            m |= GUIA_CONOCIDA;
            nodo = 1;
        } else if ((m & GUIA_GAVRAN) == 0 && Logros.tiene(sp, Logros.GAVRAN)) {
            m |= GUIA_GAVRAN | GUIA_REY;
            nodo = 10;
        } else if ((m & GUIA_REY) == 0 && Logros.tiene(sp, Logros.REY)) {
            m |= GUIA_REY;
            nodo = 9;
        } else nodo = 8;
        Ficha.dialogo(sp, "guia", m);
        return nodo;
    }

    /** Lo que pide el cliente al acabar una frase con accion. */
    public static void accion(ServerPlayer sp, int entidad, String accion) {
        Entity e = sp.level().getEntity(entidad);
        if (!(e instanceof Guia g) || sp.distanceToSqr(g) > (ALCANCE + 3) * (ALCANCE + 3)) return;
        if ("nivel".equals(accion)) {
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
        }
    }

    /** Para el comando y la prueba: olvida lo hablado con la guia. */
    public static void olvidar(ServerPlayer sp) {
        Ficha.dialogo(sp, "guia", 0);
    }
}
