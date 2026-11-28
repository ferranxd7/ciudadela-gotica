package com.darkcraft.combate;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EspadaRey;
import com.darkcraft.armas.MandobleJuramento;
import com.darkcraft.rol.Atributo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * Lo que pega un arma segun los atributos de quien la lleva (como en Elden Ring): cada arma tiene un ataque base y una
 * letra por atributo (S, A, B, C, D, E o nada) que dice cuanto le suma ese atributo, y unos requisitos: sin ellos pega
 * mucho menos. Lo usan el servidor (al pegar, {@link Combate}) y el cliente (el menu).
 * <p>
 * Lo que da cada letra: S 1,30, A 1,00, B 0,70, C 0,45, D 0,25, E 0,10 del ataque base, por lo que se ha subido el
 * atributo (la curva: casi nada a 10, un tercio a 20, el 70 % a 40, el 85 % a 60 y entero a 99).
 */
public final class Escalado {
    private Escalado() {}

    /** Los atributos que cuentan para las armas, en este orden: Fuerza, Destreza, Inteligencia, Fe y Arcano. */
    public static final Atributo[] ATRIBUTOS = {Atributo.FUERZA, Atributo.DESTREZA, Atributo.INTELIGENCIA, Atributo.FE, Atributo.ARCANO};

    /** Las letras de un arma ('-' = no escala) y sus requisitos, en el orden de {@link #ATRIBUTOS}. */
    public record Datos(String letras, int[] requisitos) {
        public char letra(int i) {
            return letras.charAt(i);
        }
    }

    private static Datos d(String letras, int... req) {
        return new Datos(letras, req);
    }

    /** Los datos de cada tipo de arma (los de las armas con nombre van aparte). */
    private static Datos deTipo(Armas.Tipo t) {
        return switch (t) {
            case PUNOS -> d("DD---", 0, 0, 0, 0, 0);
            case DAGA -> d("EB---", 5, 9, 0, 0, 0);
            case ESPADA -> d("DC---", 8, 10, 0, 0, 0);
            case MANDOBLE -> d("BE---", 16, 10, 0, 0, 0);
            case HACHA -> d("CE---", 12, 8, 0, 0, 0);
            case LANZA -> d("DB---", 10, 12, 0, 0, 0);
            case ALABARDA -> d("CC---", 14, 12, 0, 0, 0);
            // los martillos (parte 7aj): de Fuerza
            case MARTILLO -> d("BE---", 14, 8, 0, 0, 0);
            case GRAN_MARTILLO -> d("AE---", 22, 8, 0, 0, 0);
        };
    }

    /** Las letras y requisitos de un arma, o null si no es un arma del combate. */
    public static Datos de(ItemStack st) {
        if (EspadaRey.es(st)) return d("CC-D-", 12, 12, 0, 10, 0);
        if (MandobleJuramento.es(st)) return d("B---D", 18, 10, 0, 0, 10);
        if (st.is(DarkCraftMod.ALABARDA.get())) return d("CC---", 14, 12, 0, 0, 0);
        // la Lanza de la Puerta Oeste (parte 7ah): mejor que una lanza cualquiera con la Fuerza y la Destreza
        if (st.is(DarkCraftMod.LANZA_TAM.get())) return d("CB---", 11, 13, 0, 0, 0);
        // la Pertiga del farolero (parte 7ao): la luz que guarda escala con la Fe
        if (st.is(DarkCraftMod.PERTIGA_FAROLERO.get())) return d("DC-C-", 10, 12, 0, 12, 0);
        // la Espada de la Guardiana (parte 7ap): la de una duelista, mejor con la Destreza; el juramento, algo con la Fe
        if (st.is(DarkCraftMod.ESPADA_GUARDIANA.get())) return d("CB-E-", 12, 14, 0, 10, 0);
        // la maza de clerigo (parte 7aj): un martillo que escala tambien con la Fe
        if (st.is(DarkCraftMod.MAZA.get())) return d("CD-C-", 12, 8, 0, 12, 0);
        Armas.Tipo t = Armas.de(st);
        return t == null ? null : deTipo(t);
    }

    public static float coef(char letra) {
        return switch (letra) {
            case 'S' -> 1.30f;
            case 'A' -> 1.00f;
            case 'B' -> 0.70f;
            case 'C' -> 0.45f;
            case 'D' -> 0.25f;
            case 'E' -> 0.10f;
            default -> 0f;
        };
    }

    /** Cuanto del maximo da un atributo (0..1): casi nada a 10, 0,35 a 20, 0,70 a 40, 0,85 a 60, 0,95 a 80 y 1 a 99. */
    public static float curva(int a) {
        if (a <= 10) return 0.08f * Math.max(0, a) / 10f;
        if (a <= 20) return 0.08f + 0.27f * (a - 10) / 10f;
        if (a <= 40) return 0.35f + 0.35f * (a - 20) / 20f;
        if (a <= 60) return 0.70f + 0.15f * (a - 40) / 20f;
        if (a <= 80) return 0.85f + 0.10f * (a - 60) / 20f;
        return Math.min(1f, 0.95f + 0.05f * (a - 80) / 19f);
    }

    /** El ataque base de un arma: el del jugador (1) mas lo que le da el objeto. */
    public static float base(ItemStack st) {
        double b = 1.0;
        if (!st.isEmpty())
            for (AttributeModifier m : st.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE))
                if (m.getOperation() == AttributeModifier.Operation.ADDITION) b += m.getAmount();
        return (float) b;
    }

    /** Si los atributos llegan a los requisitos del arma. */
    public static boolean cumple(Datos d, int[] atributos) {
        if (d == null) return true;
        for (int i = 0; i < ATRIBUTOS.length; i++) if (atributos[ATRIBUTOS[i].ordinal()] < d.requisitos()[i]) return false;
        return true;
    }

    /**
     * Lo que suman los atributos al ataque base (puede ser negativo: sin los requisitos el arma pega un 40 % menos y sus
     * letras no cuentan).
     */
    public static float extra(ItemStack st, float base, int[] atributos) {
        Datos d = de(st);
        if (d == null) return 0f;
        if (!cumple(d, atributos)) return -0.4f * base;
        float k = 0f;
        for (int i = 0; i < ATRIBUTOS.length; i++) k += coef(d.letra(i)) * curva(atributos[ATRIBUTOS[i].ordinal()]);
        return base * k;
    }

    /**
     * Lo que pega el arma con esos atributos (base + escalado, por la mejora de la forja: {@code forja.Forja}), partiendo
     * de un ataque base ya medido.
     */
    public static float total(ItemStack st, float base, int[] atributos) {
        return Math.max(0.5f, (base + extra(st, base, atributos)) * com.darkcraft.forja.Forja.mult(st));
    }
}
