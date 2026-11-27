package com.darkcraft.armadura;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.rol.Resistencias;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Los conjuntos de armadura con modelo propio (parte 7ai, respuesta 111 del plan: "conjuntos con modelo 3D propio: yelmo,
 * pecho, guantes, piernas"). Cada conjunto tiene sus cuatro piezas ({@link PiezaArmadura}: 0 yelmo, 1 peto, 2 guantes, 3
 * grebas), su defensa por pieza (los puntos de armadura de Minecraft), su dureza, lo que aguanta los empujones, lo que pesa
 * entero (repartido entre las piezas: {@link #REPARTO}) y lo que resiste entero de cada elemento (cada pieza lo suyo, y
 * multiplicadas dan el total: {@link #resistencia}). Los guantes van en el hueco de los PIES de Minecraft (no hay hueco de
 * manos): su modelo pone las cajas en los brazos y las grebas llevan tambien las botas. El modelo y la textura los hace
 * {@code tools/armaduras/armaduras.py}.
 * <p>
 * Los de las clases (de momento todos): el caballero, el confesor, el vagabundo, el guerrero, el barbaro, el bandido, el
 * hechicero, el profeta y el prisionero; el desdichado no lleva nada.
 */
public enum Conjunto {
    //            id            sonido            defensa (yelmo, peto, guantes, grebas)   dureza  empuje  peso   fuego  hielo  rayo  magia veneno sombra
    CABALLERO("caballero", Sonido.PLACAS, new int[]{3, 7, 2, 5}, 1.0f, 0.05f, 21.0f, new float[]{0.14f, 0.12f, 0.05f, 0.08f, 0.06f, 0.08f}),
    CONFESOR("confesor", Sonido.PLACAS, new int[]{3, 6, 2, 5}, 0.5f, 0.04f, 19.0f, new float[]{0.10f, 0.10f, 0.06f, 0.14f, 0.06f, 0.16f}),
    VAGABUNDO("vagabundo", Sonido.MALLA, new int[]{2, 6, 2, 4}, 0f, 0.02f, 15.0f, new float[]{0.08f, 0.12f, 0.04f, 0.06f, 0.08f, 0.06f}),
    GUERRERO("guerrero", Sonido.MALLA, new int[]{2, 5, 2, 4}, 0f, 0.02f, 13.5f, new float[]{0.08f, 0.10f, 0.08f, 0.06f, 0.08f, 0.06f}),
    BARBARO("barbaro", Sonido.CUERO, new int[]{2, 5, 2, 4}, 0.5f, 0.03f, 14.0f, new float[]{0.06f, 0.20f, 0.06f, 0.04f, 0.10f, 0.06f}),
    BANDIDO("bandido", Sonido.CUERO, new int[]{1, 4, 1, 3}, 0f, 0f, 8.5f, new float[]{0.06f, 0.08f, 0.06f, 0.06f, 0.14f, 0.10f}),
    HECHICERO("hechicero", Sonido.TELA, new int[]{1, 3, 1, 2}, 0f, 0f, 6.5f, new float[]{0.08f, 0.08f, 0.12f, 0.22f, 0.06f, 0.12f}),
    PROFETA("profeta", Sonido.TELA, new int[]{1, 3, 1, 2}, 0f, 0f, 6.5f, new float[]{0.20f, 0.06f, 0.06f, 0.12f, 0.06f, 0.14f}),
    PRISIONERO("prisionero", Sonido.MALLA, new int[]{3, 3, 2, 2}, 0f, 0.02f, 11.0f, new float[]{0.06f, 0.08f, 0.04f, 0.16f, 0.08f, 0.08f});

    /** Las piezas, en el orden de sus numeros (y de los objetos {@code <conjunto>_<pieza>}). */
    public static final String[] PIEZAS = {"yelmo", "peto", "guantes", "grebas"};
    /** Lo que pesa y lo que resiste cada pieza del total del conjunto (suman 1). */
    public static final float[] REPARTO = {0.20f, 0.40f, 0.13f, 0.27f};
    /** Los elementos de {@link #resistencias}, en su orden (lo fisico lo para la defensa de Minecraft). */
    public static final Resistencias.Tipo[] ELEMENTOS = {Resistencias.Tipo.FUEGO, Resistencias.Tipo.HIELO, Resistencias.Tipo.RAYO,
            Resistencias.Tipo.MAGIA, Resistencias.Tipo.VENENO, Resistencias.Tipo.SOMBRA};

    public enum Sonido {PLACAS, MALLA, CUERO, TELA}

    public final String id;
    public final Sonido sonido;
    private final int[] defensa;
    public final float dureza, empuje, peso;
    private final float[] resistencias;
    private final Material material = new Material();

    Conjunto(String id, Sonido sonido, int[] defensa, float dureza, float empuje, float peso, float[] resistencias) {
        this.id = id;
        this.sonido = sonido;
        this.defensa = defensa;
        this.dureza = dureza;
        this.empuje = empuje;
        this.peso = peso;
        this.resistencias = resistencias;
    }

    public static Conjunto de(String id) {
        for (Conjunto c : values()) if (c.id.equals(id)) return c;
        return null;
    }

    /** El numero de pieza (0-3) de un tipo de armadura de Minecraft (los guantes son las botas). */
    public static int pieza(ArmorItem.Type t) {
        return switch (t) {
            case HELMET -> 0;
            case CHESTPLATE -> 1;
            case BOOTS -> 2;
            case LEGGINGS -> 3;
        };
    }

    public static ArmorItem.Type tipo(int pieza) {
        return switch (pieza) {
            case 0 -> ArmorItem.Type.HELMET;
            case 1 -> ArmorItem.Type.CHESTPLATE;
            case 2 -> ArmorItem.Type.BOOTS;
            default -> ArmorItem.Type.LEGGINGS;
        };
    }

    /** El id del objeto de una pieza. */
    public String objeto(int pieza) {
        return id + "_" + PIEZAS[pieza];
    }

    public int defensa(int pieza) {
        return defensa[pieza];
    }

    public int defensaTotal() {
        int t = 0;
        for (int d : defensa) t += d;
        return t;
    }

    /** Lo que pesa una pieza (con un decimal). */
    public float peso(int pieza) {
        return Math.round(peso * REPARTO[pieza] * 10f) / 10f;
    }

    /** Lo que resiste del elemento el conjunto entero (0..1), o 0 si no es de los suyos. */
    public float resistenciaTotal(Resistencias.Tipo t) {
        for (int i = 0; i < ELEMENTOS.length; i++) if (ELEMENTOS[i] == t) return resistencias[i];
        return 0f;
    }

    /** Lo que resiste una pieza: con las cuatro puestas, multiplicadas, da justo el total del conjunto. */
    public float resistencia(int pieza, Resistencias.Tipo t) {
        float total = resistenciaTotal(t);
        if (total <= 0f) return 0f;
        return (float) (1.0 - Math.pow(1.0 - total, REPARTO[pieza]));
    }

    public ArmorMaterial material() {
        return material;
    }

    /** El sonido de ponerse una pieza (de ElevenLabs). */
    public SoundEvent sonidoPoner() {
        try {
            return switch (sonido) {
                case PLACAS -> DarkCraftMod.SFX_ARMADURA_PLACAS.get();
                case MALLA -> DarkCraftMod.SFX_ARMADURA_MALLA.get();
                case CUERO -> DarkCraftMod.SFX_ARMADURA_CUERO.get();
                case TELA -> DarkCraftMod.SFX_ARMADURA_TELA.get();
            };
        } catch (Exception e) {
            return SoundEvents.ARMOR_EQUIP_GENERIC;
        }
    }

    /** El material de Minecraft del conjunto: no se rompe, no se encanta ni se repara. */
    private final class Material implements ArmorMaterial {
        @Override
        public int getDurabilityForType(ArmorItem.Type t) {
            return 0;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type t) {
            return defensa[pieza(t)];
        }

        @Override
        public int getEnchantmentValue() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return sonidoPoner();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return DarkCraftMod.MODID + ":" + id;
        }

        @Override
        public float getToughness() {
            return dureza;
        }

        @Override
        public float getKnockbackResistance() {
            return empuje;
        }
    }
}
