package com.darkcraft.combate;

import com.darkcraft.armas.EspadaRey;
import com.darkcraft.armas.MandobleJuramento;
import com.darkcraft.combate.Armas.Anim;
import com.darkcraft.combate.Armas.Tipo;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.Set;

/**
 * Las habilidades de las armas (como las cenizas de guerra de Elden Ring): cada tipo de arma trae la suya y en la forja
 * de Dorvan se le puede poner otra con una ceniza ({@code forja.CenizaGuerra}; va en el NBT del arma, {@link #NBT}). Se
 * usan con su tecla (la C) y gastan mana (la sangrienta, vida) y estamina. Cada una tiene su movimiento del combate
 * propio ({@link Armas.Mov}); lo especial (el salto del sismo, el paso rapido, el grito, la parada, el fuego, el hielo,
 * el rayo y la sangre) lo hacen {@code Combate} en el servidor y {@code client.combate.ClientCombate} en el cliente.
 * Las armas con nombre (las Espadas del Rey, el Mandoble del Juramento) llevan sus propios golpes y no tienen.
 */
public enum Habilidad {
    /** Una vuelta entera con el arma: dos golpes alrededor. */
    TORBELLINO("torbellino", m(Anim.GIRO, 22, 7, 13, 1.25f, 0.15f, 1.15f, 180f, false, 16, 18, 0.6f), 14,
            Tipo.ESPADA, Tipo.HACHA, Tipo.ALABARDA, Tipo.MANDOBLE),
    /** Se lanza hacia delante y atraviesa: lo que haya en la linea. */
    ESTOCADA("estocada", m(Anim.ESTOCADA, 22, 9, 12, 2.0f, 0f, 1.35f, 0f, true, 22, 20, 0.9f), 14,
            Tipo.LANZA, Tipo.ESPADA, Tipo.DAGA, Tipo.ALABARDA),
    /** Un grito de guerra: un rato pegando mas fuerte ({@code rol.Efectos.FURIA}). */
    GRITO("grito", m(Anim.GRITO, 26, 10, 10, 0f, 0f, 0f, 0f, false, 0, 12, 0f), 18,
            Tipo.MANDOBLE, Tipo.HACHA, Tipo.ALABARDA),
    /** Salta y golpea el suelo: una onda alrededor de donde cae. */
    SISMO("sismo", m(Anim.SALTO, 26, 0, 0, 1.8f, 0f, 1.0f, 0f, false, 30, 24, 1.0f), 20,
            Tipo.HACHA, Tipo.MANDOBLE, Tipo.ALABARDA),
    /** Un paso rapido de sombra hacia donde se vaya, sin que llegue nada mientras. */
    PASO("paso", m(Anim.PASO, 9, 0, 0, 0f, 0f, 0f, 0f, false, 0, 10, 0f), 6,
            Tipo.DAGA, Tipo.ESPADA),
    /** Un tajo ancho de fuego que quema. */
    LLAMAS("llamas", m(Anim.BARRIDO_DI, 18, 7, 10, 1.4f, 0.2f, 1.2f, 70f, false, 14, 16, 0.5f), 16, todas()),
    /** Un tajo helado que congela y frena. */
    ESCARCHA("escarcha", m(Anim.TAJO_ID, 16, 6, 8, 1.3f, 0.2f, 1.1f, 60f, false, 12, 14, 0.4f), 16, todas()),
    /** Un tajo de arriba abajo que suelta un rayo en linea hacia delante. */
    RELAMPAGO("relampago", m(Anim.VERTICAL, 20, 8, 10, 1.5f, 0.15f, 1.6f, 0f, true, 16, 16, 0.5f), 20, todas()),
    /** Un tajo de sangre muy fuerte que cuesta vida en vez de mana. */
    SANGRE("sangre", m(Anim.TAJO_DI, 18, 6, 9, 2.1f, 0.3f, 1.3f, 75f, false, 18, 14, 0.6f), 0,
            Tipo.ESPADA, Tipo.DAGA, Tipo.HACHA, Tipo.MANDOBLE),
    /** Desvia el golpe que llegue en un instante (y deja al que pego aturdido). */
    PARADA("parada", m(Anim.PARADA, 14, 2, 7, 0f, 0f, 0f, 0f, false, 0, 10, 0f), 0,
            Tipo.DAGA, Tipo.ESPADA);

    /** El NBT del arma con la ceniza que se le ha puesto (el id de la habilidad). */
    public static final String NBT = "darkcraft_ceniza";
    /** Lo que cuesta la sangrienta: esta parte de la vida maxima. */
    public static final float VIDA_SANGRE = 0.08f;
    /** Los ticks invulnerables del paso rapido, lo que dura la furia del grito (ticks) y lo que pega de mas. */
    public static final int PASO_INVULNERABLE = 8, FURIA_DURA = 400;
    public static final float FURIA_EXTRA = 0.20f;

    public final String id;
    public final Armas.Mov mov;
    public final int mana;
    private final Set<Tipo> tipos;

    Habilidad(String id, Armas.Mov mov, int mana, Tipo... tipos) {
        this.id = id;
        this.mov = mov;
        this.mana = mana;
        this.tipos = tipos.length == 0 ? EnumSet.noneOf(Tipo.class) : EnumSet.of(tipos[0], tipos);
    }

    private static Armas.Mov m(Anim a, int dura, int g0, int g1, float mult, float empuje, float alcance, float arco, boolean estocada,
                               float aturde, float estamina, float kb) {
        return new Armas.Mov(a, dura, g0, g1, mult, empuje, alcance, arco, estocada, aturde, estamina, kb);
    }

    private static Tipo[] todas() {
        return new Tipo[]{Tipo.DAGA, Tipo.ESPADA, Tipo.MANDOBLE, Tipo.HACHA, Tipo.LANZA, Tipo.ALABARDA};
    }

    /** Si esta habilidad se le puede poner a ese tipo de arma. */
    public boolean vale(Tipo t) {
        return tipos.contains(t);
    }

    public Set<Tipo> tipos() {
        return tipos;
    }

    public String clave() {
        return "darkcraft.habilidad." + id;
    }

    /** Si va de un salto (el sismo: se anima y se cuenta como el golpe del salto, al caer). */
    public boolean salta() {
        return this == SISMO;
    }

    public static Habilidad de(String id) {
        for (Habilidad h : values()) if (h.id.equals(id)) return h;
        return null;
    }

    /** La que trae cada tipo de arma. */
    public static Habilidad porDefecto(Tipo t) {
        return switch (t) {
            case ESPADA, ALABARDA -> TORBELLINO;
            case DAGA -> PASO;
            case MANDOBLE -> GRITO;
            case HACHA -> SISMO;
            case LANZA -> ESTOCADA;
            default -> null;
        };
    }

    /** Si el arma admite cenizas (las del combate, no los punos ni las que tienen nombre). */
    public static boolean admite(ItemStack st) {
        if (st.isEmpty() || EspadaRey.es(st) || MandobleJuramento.es(st)) return false;
        Tipo t = Armas.de(st);
        return t != null && t != Tipo.PUNOS;
    }

    /** La ceniza que se le ha puesto al arma (o null si lleva la suya). */
    public static Habilidad puesta(ItemStack st) {
        if (!admite(st) || !st.hasTag()) return null;
        Habilidad h = de(st.getTag().getString(NBT));
        return h != null && h.vale(Armas.de(st)) ? h : null;
    }

    /** La habilidad del arma: la de su ceniza o la de su tipo (o null si no tiene). */
    public static Habilidad de(ItemStack st) {
        if (!admite(st)) return null;
        Habilidad h = puesta(st);
        return h != null ? h : porDefecto(Armas.de(st));
    }

    public static void poner(ItemStack st, Habilidad h) {
        if (st.isEmpty()) return;
        if (h == null) {
            if (st.hasTag()) st.getTag().remove(NBT);
        } else st.getOrCreateTag().putString(NBT, h.id);
    }
}
