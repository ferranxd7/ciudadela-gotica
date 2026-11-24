package com.darkcraft.rol;

/**
 * Las cuentas del nivel: cuanto cuesta subir y lo que da cada atributo. Las usan igual el servidor (que es el que manda)
 * y el cliente (para ensenar en el menu lo que se va a ganar antes de confirmar).
 */
public final class Nivel {
    private Nivel() {}

    /** El nivel sale de los atributos: con todos a 10 se es nivel 1. */
    public static int de(int[] a) {
        int s = 0;
        for (int v : a) s += v;
        return Math.max(1, s - Atributo.N * Atributo.BASE + 1);
    }

    /** Ecos que cuesta pasar del nivel n al n + 1 (la curva de Elden Ring: 673 el primero, cada vez mas). */
    public static int coste(int n) {
        double x = Math.max(0.0, (n + 81 - 92) * 0.02);
        return (int) Math.floor((x + 0.1) * (n + 81) * (double) (n + 81) + 1);
    }

    /** Lo que cuesta subir del nivel {@code desde} al nivel {@code hasta}. */
    public static long coste(int desde, int hasta) {
        long t = 0;
        for (int n = desde; n < hasta; n++) t += coste(n);
        return t;
    }

    /** Vida maxima (20 = diez corazones de los de antes): +2 por punto hasta 30, +1 hasta 50 y +0,4 despues. */
    public static float vida(int vigor) {
        float v = 20f;
        if (vigor < 10) return Math.max(6f, 20f - (10 - vigor) * 1.5f);
        v += (Math.min(vigor, 30) - 10) * 2f;
        if (vigor > 30) v += (Math.min(vigor, 50) - 30) * 1f;
        if (vigor > 50) v += (vigor - 50) * 0.4f;
        return v;
    }

    /** Mana maximo: 100 con Mente 10; +6 por punto hasta 35, +3 hasta 60 y +1 despues. */
    public static int mana(int mente) {
        if (mente < 10) return Math.max(40, 100 - (10 - mente) * 6);
        int m = 100 + (Math.min(mente, 35) - 10) * 6;
        if (mente > 35) m += (Math.min(mente, 60) - 35) * 3;
        if (mente > 60) m += mente - 60;
        return m;
    }

    /** Estamina maxima: 100 con Aguante 10; +3 por punto hasta 40 y +1 despues. */
    public static float estamina(int aguante) {
        if (aguante < 10) return Math.max(70f, 100f - (10 - aguante) * 3f);
        float e = 100f + (Math.min(aguante, 40) - 10) * 3f;
        if (aguante > 40) e += aguante - 40;
        return e;
    }

    /** Cuanto mas pegan las armas (multiplica el dano): +1,2 % por punto de Fuerza y +0,8 % por punto de Destreza sobre 10. */
    public static float ataque(int fuerza, int destreza) {
        return 1f + (fuerza - 10) * 0.012f + (destreza - 10) * 0.008f;
    }

    /** Lo que cura un trago del frasco de vida (segun la vida maxima) y lo que da el de mana. */
    public static float curaFrasco(float vidaMax, int mejora) {
        return 7f + vidaMax * (0.34f + 0.03f * mejora);
    }

    public static int manaFrasco(int manaMax, int mejora) {
        return Math.round(40 + manaMax * (0.3f + 0.03f * mejora));
    }
}
