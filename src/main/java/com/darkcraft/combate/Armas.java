package com.darkcraft.combate;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EspadaRey;
import com.darkcraft.armas.MandobleJuramento;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

/**
 * Las armas del combate propio (como en Elden Ring o Epic Fight): de que tipo es cada una y como se mueve. Cada tipo
 * tiene su cadena de golpes ligeros, un golpe fuerte que se carga manteniendo el clic, un ataque corriendo y otro
 * cayendo de un salto. Cada movimiento ({@link Mov}) dice cuanto dura, en que ticks hace dano, cuanto pega, cuanto
 * empuja hacia delante, su alcance y su arco, cuanta estamina gasta y cuanto desequilibra.
 * <p>
 * Lo usan igual el servidor (que es el que pega) y el cliente (que anima y lleva el ritmo).
 */
public final class Armas {
    private Armas() {}

    /**
     * Las acciones (HECHIZO: lanzar un hechizo con el baston o el sello; su paso es el hechizo. HABILIDAD: la habilidad
     * del arma, la de su ceniza de guerra; su paso es la {@link Habilidad}).
     */
    public static final int LIGERO = 0, PESADO = 1, CORRIENDO = 2, SALTO = 3, CARGA = 4, RODAR = 5, PASO_ATRAS = 6, HECHIZO = 7, HABILIDAD = 8;
    /** Lo que se puede cargar un golpe fuerte (ticks) y lo que da de mas cargado del todo. */
    public static final int CARGA_MAX = 24;
    public static final float CARGA_EXTRA = 0.45f;

    /** Las animaciones (el cliente sabe como se ven; aqui solo el nombre). */
    public enum Anim {
        TAJO_DI, TAJO_ID, VERTICAL, ESTOCADA, MAZAZO, BARRIDO_DI, BARRIDO_ID, SALTO, CARGA, PUNO_D, PUNO_I, DAGA_D, DAGA_I, RODAR, PASO_ATRAS,
        /** Hechiceria: el baston adelante. Milagro: el sello en alto. */
        LANZAR, REZAR,
        /** Habilidades: la vuelta entera del torbellino, el grito de guerra, la parada y el paso rapido. */
        GIRO, GRITO, PARADA, PASO
    }

    /**
     * Un movimiento: animacion, duracion (ticks), primer y ultimo tick en que hace dano, multiplicador de dano, empuje
     * hacia delante del que pega, alcance (multiplica el del arma), arco (grados a cada lado; 0 = el del arma), si es una
     * estocada (en linea recta), cuanto desequilibra, estamina que gasta y cuanto empuja al que recibe.
     */
    public record Mov(Anim anim, int dura, int golpe0, int golpe1, float mult, float empuje, float alcance, float arco, boolean estocada,
                      float aturde, float estamina, float retroceso) {
        /** Desde que tick se puede encadenar otro golpe o rodar (lo que queda es recuperarse). */
        public int libre() {
            return golpe1 + 2;
        }
    }

    private static Mov m(Anim a, int dura, int g0, int g1, float mult, float empuje, float aturde, float est, float kb) {
        return new Mov(a, dura, g0, g1, mult, empuje, 1f, 0f, a == Anim.ESTOCADA, aturde, est, kb);
    }

    public enum Tipo {
        PUNOS(2.3f, 40f, false, 0f, "golpe_ligero",
                new Mov[]{m(Anim.PUNO_D, 7, 2, 3, 1.0f, 0.10f, 4, 5, 0.2f), m(Anim.PUNO_I, 7, 2, 3, 1.0f, 0.10f, 4, 5, 0.2f),
                        m(Anim.PUNO_D, 9, 3, 4, 1.2f, 0.15f, 6, 6, 0.3f)},
                m(Anim.PUNO_D, 14, 6, 7, 1.6f, 0.25f, 14, 10, 0.5f), m(Anim.PUNO_D, 10, 3, 5, 1.3f, 0.5f, 8, 8, 0.4f),
                m(Anim.SALTO, 14, 0, 0, 1.3f, 0f, 10, 9, 0.4f)),
        DAGA(2.6f, 50f, false, 1.5f, "golpe_ligero",
                new Mov[]{m(Anim.DAGA_D, 7, 2, 3, 0.85f, 0.1f, 4, 6, 0.15f), m(Anim.DAGA_I, 7, 2, 3, 0.85f, 0.1f, 4, 6, 0.15f),
                        m(Anim.DAGA_D, 7, 2, 3, 0.9f, 0.1f, 4, 6, 0.15f), m(Anim.ESTOCADA, 10, 3, 5, 1.1f, 0.25f, 6, 7, 0.3f)},
                m(Anim.ESTOCADA, 14, 6, 8, 1.5f, 0.3f, 14, 12, 0.4f), m(Anim.ESTOCADA, 11, 3, 5, 1.1f, 0.55f, 8, 9, 0.35f),
                m(Anim.SALTO, 14, 0, 0, 1.25f, 0f, 10, 9, 0.35f)),
        ESPADA(3.1f, 65f, false, 3f, "golpe_ligero",
                new Mov[]{m(Anim.TAJO_DI, 11, 4, 5, 1.0f, 0.15f, 6, 9, 0.25f), m(Anim.TAJO_ID, 11, 4, 5, 1.0f, 0.15f, 6, 9, 0.25f),
                        m(Anim.VERTICAL, 12, 5, 6, 1.1f, 0.2f, 8, 10, 0.3f), m(Anim.ESTOCADA, 14, 5, 7, 1.3f, 0.35f, 10, 11, 0.45f)},
                m(Anim.VERTICAL, 18, 7, 9, 1.6f, 0.3f, 22, 16, 0.6f), m(Anim.ESTOCADA, 14, 4, 7, 1.2f, 0.6f, 12, 12, 0.5f),
                m(Anim.SALTO, 18, 0, 0, 1.35f, 0f, 18, 13, 0.5f)),
        MANDOBLE(3.8f, 80f, true, 12f, "golpe_pesado",
                new Mov[]{m(Anim.BARRIDO_DI, 17, 7, 9, 1.0f, 0.2f, 12, 14, 0.4f), m(Anim.BARRIDO_ID, 17, 7, 9, 1.0f, 0.2f, 12, 14, 0.4f),
                        m(Anim.MAZAZO, 20, 9, 11, 1.25f, 0.25f, 18, 15, 0.55f)},
                m(Anim.MAZAZO, 24, 11, 13, 1.9f, 0.3f, 35, 22, 0.8f), m(Anim.BARRIDO_DI, 18, 6, 9, 1.3f, 0.55f, 18, 16, 0.6f),
                m(Anim.SALTO, 20, 0, 0, 1.6f, 0f, 28, 16, 0.7f)),
        HACHA(3.0f, 60f, false, 4.5f, "golpe_pesado",
                new Mov[]{m(Anim.TAJO_DI, 13, 5, 6, 1.0f, 0.15f, 9, 11, 0.3f), m(Anim.VERTICAL, 14, 6, 7, 1.1f, 0.2f, 10, 11, 0.35f),
                        m(Anim.TAJO_ID, 13, 5, 6, 1.05f, 0.15f, 9, 11, 0.3f)},
                m(Anim.VERTICAL, 20, 9, 11, 1.75f, 0.3f, 26, 18, 0.65f), m(Anim.TAJO_DI, 15, 5, 7, 1.25f, 0.55f, 14, 13, 0.5f),
                m(Anim.SALTO, 18, 0, 0, 1.45f, 0f, 22, 14, 0.55f)),
        LANZA(4.2f, 18f, true, 6f, "estocada",
                new Mov[]{m(Anim.ESTOCADA, 11, 4, 5, 1.0f, 0.15f, 7, 10, 0.3f), m(Anim.ESTOCADA, 11, 4, 5, 1.0f, 0.15f, 7, 10, 0.3f),
                        m(Anim.ESTOCADA, 13, 5, 7, 1.15f, 0.3f, 9, 11, 0.4f)},
                m(Anim.ESTOCADA, 18, 8, 10, 1.6f, 0.4f, 20, 16, 0.6f), m(Anim.ESTOCADA, 14, 4, 7, 1.3f, 0.7f, 12, 12, 0.55f),
                m(Anim.SALTO, 16, 0, 0, 1.3f, 0f, 14, 12, 0.5f)),
        ALABARDA(4.0f, 55f, true, 9f, "golpe_pesado",
                new Mov[]{m(Anim.BARRIDO_DI, 16, 6, 8, 1.0f, 0.2f, 10, 13, 0.4f), m(Anim.ESTOCADA, 14, 5, 7, 1.0f, 0.25f, 9, 12, 0.35f),
                        m(Anim.MAZAZO, 18, 8, 10, 1.2f, 0.25f, 15, 14, 0.5f)},
                m(Anim.MAZAZO, 22, 10, 12, 1.8f, 0.3f, 30, 20, 0.75f), m(Anim.ESTOCADA, 15, 4, 7, 1.3f, 0.6f, 14, 14, 0.55f),
                m(Anim.SALTO, 18, 0, 0, 1.5f, 0f, 24, 15, 0.6f)),
        /**
         * El martillo de una mano (parte 7aj: la maza y el martillo de guerra): lento para una mano, pero cada golpe desequilibra
         * mucho (rompe guardias); de arriba abajo, de lado y otra vez de arriba.
         */
        MARTILLO(2.9f, 45f, false, 5.5f, "golpe_pesado",
                new Mov[]{m(Anim.VERTICAL, 14, 6, 7, 1.0f, 0.15f, 16, 11, 0.4f), m(Anim.TAJO_DI, 13, 5, 6, 1.0f, 0.15f, 14, 10, 0.35f),
                        m(Anim.VERTICAL, 15, 6, 8, 1.15f, 0.2f, 18, 12, 0.45f)},
                m(Anim.MAZAZO, 22, 10, 12, 1.8f, 0.3f, 40, 18, 0.8f), m(Anim.VERTICAL, 15, 5, 7, 1.3f, 0.55f, 20, 13, 0.55f),
                m(Anim.SALTO, 18, 0, 0, 1.5f, 0f, 28, 14, 0.65f)),
        /**
         * El gran martillo a dos manos (parte 7aj): barridos y mazazos lentisimos que tiran al suelo a casi cualquiera; el golpe
         * fuerte y el del salto, los que mas desequilibran de todas las armas.
         */
        GRAN_MARTILLO(3.5f, 60f, true, 14f, "golpe_pesado",
                new Mov[]{m(Anim.BARRIDO_DI, 20, 9, 11, 1.0f, 0.2f, 22, 17, 0.6f), m(Anim.MAZAZO, 23, 11, 13, 1.25f, 0.25f, 30, 18, 0.75f)},
                m(Anim.MAZAZO, 28, 13, 15, 2.1f, 0.3f, 50, 26, 1.0f), m(Anim.BARRIDO_DI, 20, 7, 10, 1.35f, 0.55f, 26, 19, 0.75f),
                m(Anim.SALTO, 22, 0, 0, 1.75f, 0f, 40, 19, 0.85f));

        /** Alcance (bloques), arco (grados a cada lado), si se lleva a dos manos, peso y el sonido de sus golpes ligeros. */
        public final float alcance, arco, peso;
        public final boolean dosManos;
        public final String sonido;
        public final Mov[] ligeros;
        public final Mov pesado, corriendo, salto;

        Tipo(float alcance, float arco, boolean dosManos, float peso, String sonido, Mov[] ligeros, Mov pesado, Mov corriendo, Mov salto) {
            this.alcance = alcance;
            this.arco = arco;
            this.dosManos = dosManos;
            this.peso = peso;
            this.sonido = sonido;
            this.ligeros = ligeros;
            this.pesado = pesado;
            this.corriendo = corriendo;
            this.salto = salto;
        }

        /** El movimiento de una accion (el paso solo cuenta en los ligeros). */
        public Mov mov(int accion, int paso) {
            return switch (accion) {
                case PESADO, CARGA -> pesado;
                case CORRIENDO -> corriendo;
                case SALTO -> salto;
                default -> ligeros[Math.floorMod(paso, ligeros.length)];
            };
        }
    }

    /** El movimiento de rodar y el del paso atras (no son de ningun arma). */
    public static final Mov RODAR_MOV = new Mov(Anim.RODAR, 10, 0, 0, 0f, 0f, 0f, 0f, false, 0f, 14f, 0f),
            PASO_ATRAS_MOV = new Mov(Anim.PASO_ATRAS, 7, 0, 0, 0f, 0f, 0f, 0f, false, 0f, 10f, 0f);

    /** La voltereta segun lo cargado que se va (ligero, medio, pesado): cuanto dura y lo rapida que es (bloques por tick). */
    private static final Mov[] RODAR_PESO = {
            new Mov(Anim.RODAR, 9, 0, 0, 0f, 0f, 0f, 0f, false, 0f, 14f, 0f), RODAR_MOV,
            new Mov(Anim.RODAR, 12, 0, 0, 0f, 0f, 0f, 0f, false, 0f, 14f, 0f)};
    private static final float[] RODAR_RAPIDEZ = {0.52f, 0.44f, 0.31f};
    /** Los instantes invulnerables de la voltereta (ligero, medio, pesado) y los del paso atras. */
    private static final int[] RODAR_INVULNERABLE = {9, 8, 7};
    public static final int PASO_ATRAS_INVULNERABLE = 5;
    public static final float PASO_ATRAS_RAPIDEZ = 0.42f;

    private static final Mov[] HECHIZOS = new Mov[com.darkcraft.hechizos.Hechizo.values().length];

    /** El gesto de lanzar un hechizo: dura lo suyo y "pega" (sale) en su tick. */
    public static Mov hechizo(com.darkcraft.hechizos.Hechizo h) {
        Mov m = HECHIZOS[h.ordinal()];
        if (m == null) {
            Anim a = h.escuela == com.darkcraft.hechizos.Hechizo.Escuela.HECHICERIA ? Anim.LANZAR : Anim.REZAR;
            HECHIZOS[h.ordinal()] = m = new Mov(a, h.dura, h.sale, h.sale, 0f, 0f, 0f, 0f, false, 0f, 0f, 0f);
        }
        return m;
    }

    public static Mov rodar(int clasePeso) {
        return RODAR_PESO[Math.max(0, Math.min(2, clasePeso))];
    }

    public static float rapidezRodar(int clasePeso) {
        return RODAR_RAPIDEZ[Math.max(0, Math.min(2, clasePeso))];
    }

    public static int invulnerableRodar(int clasePeso) {
        return RODAR_INVULNERABLE[Math.max(0, Math.min(2, clasePeso))];
    }

    /**
     * El tipo de un arma, o null si no es un arma del combate (herramientas, bloques, comida...: con eso el clic hace lo
     * de siempre). La mano vacia pega con los punos.
     */
    public static Tipo de(ItemStack st) {
        if (st.isEmpty()) return Tipo.PUNOS;
        if (MandobleJuramento.es(st)) return Tipo.MANDOBLE;
        if (EspadaRey.es(st)) return Tipo.ESPADA;
        if (st.is(DarkCraftMod.ALABARDA.get())) return Tipo.ALABARDA;
        // la lanza de Tam (parte 7ah): una lanza, aunque no sea un tridente
        if (st.is(DarkCraftMod.LANZA_TAM.get())) return Tipo.LANZA;
        if (st.getItem() instanceof TridentItem) return Tipo.LANZA;
        // los martillos (parte 7aj): la maza, el martillo de guerra y el gran martillo
        if (st.getItem() instanceof com.darkcraft.armas.Martillo mt) return mt.tipo;
        if (st.getItem() instanceof AxeItem) return Tipo.HACHA;
        if (st.getItem() instanceof SwordItem) {
            String id = String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(st.getItem()));
            return id.contains("dagger") || id.contains("daga") || id.contains("knife") ? Tipo.DAGA : Tipo.ESPADA;
        }
        return null;
    }

    /** Si se pelea con las dos Espadas del Rey a la vez (los golpes ligeros van alternando de mano). */
    public static boolean dobles(net.minecraft.world.entity.player.Player p) {
        return EspadaRey.es(p.getMainHandItem()) && EspadaRey.es(p.getOffhandItem());
    }
}
