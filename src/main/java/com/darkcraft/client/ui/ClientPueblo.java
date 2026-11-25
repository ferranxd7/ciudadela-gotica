package com.darkcraft.client.ui;

import com.darkcraft.pueblo.Pueblo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Lo de Brumavela en el cliente (parte 3b): las "cosas" que el servidor dice que se pueden usar (el arcon de la vela,
 * las piezas, las campanas, las lapidas, el barco) y cual tiene el jugador a mano para la tecla del aviso: la mas
 * cercana a donde mira, a menos de cuatro bloques.
 */
public final class ClientPueblo {
    private ClientPueblo() {}

    private static final String[] ACCION = {"abrir", "recoger", "registrar", "tocar", "leer", "reparar", "zarpar", "volver", "llamar"};
    private static final String[] QUIEN = {"pescador", "tejedora", "farero"};

    private static List<Pueblo.Cosa> cosas = List.of();
    /** La cosa que se tiene a mano (su numero), o -1. */
    public static int aMano = -1;
    private static int accion;

    /** Las piezas del barco que ya se han encontrado (un bit por pieza) y si el barco esta reparado. */
    public static int piezas;
    public static boolean reparado;
    /** Cuando cambio lo encontrado (para el destello del marcador). */
    static int cambio = -1000, nuevas;

    public static void recibir(List<Pueblo.Cosa> lista, int piezasAhora, boolean reparadoAhora) {
        if (!lista.isEmpty() && !cosas.isEmpty() && (piezasAhora & ~piezas) != 0) {
            cambio = ClientRol.ticks;
            nuevas = piezasAhora & ~piezas;
        }
        cosas = lista;
        piezas = piezasAhora;
        reparado = reparadoAhora;
        aMano = -1;
    }

    static void olvidar() {
        cosas = List.of();
        aMano = -1;
        piezas = 0;
        reparado = false;
    }

    /** Si hay que ensenar el marcador de las piezas: en la isla, mientras el barco siga roto. */
    static boolean marcador() {
        return !cosas.isEmpty() && !reparado;
    }

    /** Lo que dice el aviso de la cosa que se tiene a mano ("Pulsa [E] " + esto). */
    static String clave() {
        if (aMano >= Pueblo.CAMPANA && aMano < Pueblo.CAMPANA + 3) return "darkcraft.aviso.cosa.tocar." + QUIEN[aMano - Pueblo.CAMPANA];
        return "darkcraft.aviso.cosa." + ACCION[Math.max(0, Math.min(ACCION.length - 1, accion))];
    }

    public static int cuantas() {
        return cosas.size();
    }

    static void tick(Minecraft mc) {
        aMano = -1;
        if (cosas.isEmpty() || mc.player == null || mc.player.isSpectator()) return;
        Vec3 ojo = mc.player.getEyePosition(), mira = mc.player.getLookAngle();
        double mejor = Double.MAX_VALUE;
        for (Pueblo.Cosa c : cosas) {
            Vec3 a = new Vec3(c.x() + 0.5 - ojo.x, c.y() + 0.5 - ojo.y, c.z() + 0.5 - ojo.z);
            double d = a.length();
            // las campanas cuelgan altas: un poco mas de alcance
            if (d > (c.accion() == Pueblo.TOCAR || c.accion() == Pueblo.A_LLAMAR ? 4.6 : 3.9)) continue;
            double cos = d < 0.01 ? 1 : a.dot(mira) / d;
            // delante (un cono ancho), o casi encima
            if (cos < 0.45 && d > 1.7) continue;
            double nota = d * (1.6 - cos);
            if (nota < mejor) {
                mejor = nota;
                aMano = c.id();
                accion = c.accion();
            }
        }
    }
}
