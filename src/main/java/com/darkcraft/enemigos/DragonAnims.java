package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class DragonAnims {
    private DragonAnims() {}

    public static final int DESPIERTA = 1;
    public static final int RUGIDO = 2;
    public static final int MORDISCO = 3;
    public static final int ZARPAZO = 4;
    public static final int COLETAZO = 5;
    public static final int ALETAZO = 6;
    public static final int PISOTON = 7;
    public static final int ALIENTO = 8;
    public static final int DESPEGA = 9;
    public static final int ATERRIZA = 10;
    public static final int RASANTE = 11;
    public static final int ESCUPE = 12;
    public static final int PICADO = 13;
    public static final int ASCUAS = 14;
    public static final int ATURDIDO = 15;
    public static final int MORIR = 16;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 64, 52, 28, 26, 36, 30, 40, 76, 40, 32, 60, 24, 48, 60, 60, 100};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {}, {}, {14}, {14}, {19}, {16}, {23}, {}, {}, {5}, {}, {11}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "despierta", "rugido", "mordisco", "zarpazo", "coletazo", "aletazo", "pisoton", "aliento", "despega", "aterriza", "rasante", "escupe", "picado", "ascuas", "aturdido", "morir"};

    /** El aliento: de que tick a que tick echa fuego y lo que gira la cabeza (de +GIRO, a su derecha, a -GIRO). */
    public static final int ALIENTO_DESDE = 18, ALIENTO_HASTA = 66;
    public static final float ALIENTO_GIRO = 34f;
    /** El vuelo rasante: de que tick a que tick echa fuego. */
    public static final int RASANTE_DESDE = 12, RASANTE_HASTA = 52;
    /** El despegue: el tick en que deja el suelo. */
    public static final int DESPEGA_SALTO = 15;
    /** La lluvia de ascuas: el tick del rugido al cielo. */
    public static final int ASCUAS_RUGIDO = 16;
}
