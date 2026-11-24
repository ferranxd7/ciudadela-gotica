package com.darkcraft.hechizos;

import com.darkcraft.rol.Atributo;

/**
 * Los hechizos (como los de Elden Ring): hechicerias, que se lanzan con un baston y escalan con la Inteligencia, y
 * milagros, que se lanzan con un sello y escalan con la Fe. Cada uno gasta mana, tarda en salir (y mientras se anda
 * despacio), pide un minimo del atributo de su escuela y ocupa huecos de memoria (se memorizan en un santuario).
 * Lo usan igual el servidor ({@link Lanzar}) y el cliente.
 */
public enum Hechizo {
    /** Un guijarro de sombra que vuela recto: rapido y barato. */
    GUIJARRO(Escuela.HECHICERIA, 8, 10, 1, 16, 7, 9f),
    /** Una gran lanza de sombra: lenta de lanzar, pega mucho y atraviesa. */
    LANZA(Escuela.HECHICERIA, 24, 20, 2, 30, 18, 24f),
    /** Cinco esquirlas que buscan al enemigo. */
    ESQUIRLAS(Escuela.HECHICERIA, 16, 14, 1, 22, 10, 4.5f),
    /** Un tajo de espada fantasma delante (de cerca, sin proyectil). */
    ESPADA(Escuela.HECHICERIA, 12, 12, 1, 18, 8, 11f),
    /** Una llamarada delante, de cerca: quema. */
    LLAMA(Escuela.MILAGRO, 12, 10, 1, 18, 8, 10f),
    /** Una lanza de rayo que se lanza lejos. */
    RAYO(Escuela.MILAGRO, 22, 18, 2, 26, 14, 20f),
    /** Cura al que lo lanza y a los que estan cerca. */
    CURAR(Escuela.MILAGRO, 30, 12, 1, 30, 18, 0f),
    /** Durante 30 s, las armas pegan un 15 % mas. */
    BENDICION(Escuela.MILAGRO, 25, 14, 1, 28, 16, 0f);

    public enum Escuela {
        HECHICERIA(Atributo.INTELIGENCIA), MILAGRO(Atributo.FE);

        public final Atributo atributo;

        Escuela(Atributo a) {
            this.atributo = a;
        }
    }

    public final Escuela escuela;
    /** Mana que gasta, el minimo del atributo de su escuela, los huecos de memoria que ocupa, lo que dura el gesto
     *  (ticks), el tick en que sale y el dano base (0 = no hace dano). */
    public final int mana, requisito, huecos, dura, sale;
    public final float dano;
    /** "guijarro", "lanza"... (el objeto es darkcraft:hechizo_ + esto). */
    public final String id = name().toLowerCase(java.util.Locale.ROOT);

    Hechizo(Escuela escuela, int mana, int requisito, int huecos, int dura, int sale, float dano) {
        this.escuela = escuela;
        this.mana = mana;
        this.requisito = requisito;
        this.huecos = huecos;
        this.dura = dura;
        this.sale = sale;
        this.dano = dano;
    }

    public static Hechizo de(String id) {
        for (Hechizo h : values()) if (h.id.equals(id)) return h;
        return null;
    }

    /** Los huecos de memoria segun la Mente: 2 a 10, y uno mas a 14, 20, 28, 38 y 50 (hasta 7). */
    public static int memoria(int mente) {
        int n = 2;
        for (int u : new int[]{14, 20, 28, 38, 50}) if (mente >= u) n++;
        return n;
    }

    /**
     * Lo que pega con un atributo (el de su escuela): el dano base y lo que suma el catalizador (escala A con ese
     * atributo: un 100 % mas a 99, un 70 % a 40). Sin el minimo no se puede lanzar.
     */
    public float danoCon(int atributo) {
        return dano * (1f + com.darkcraft.combate.Escalado.coef('A') * com.darkcraft.combate.Escalado.curva(atributo));
    }
}
