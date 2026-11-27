package com.darkcraft.rol;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Un talisman: se lleva en uno de sus huecos (menu → Equipo; el primero desde el principio y otro por cada gran jefe
 * vencido, hasta cuatro) y mientras se lleva da lo suyo. No se pueden llevar dos iguales. Lo que hace cada uno lo
 * miran donde toca ({@link Talismanes#lleva}): Rol (vida, mana, estamina, ecos), Peso, Resistencias y Combate.
 */
public final class Talisman extends Item {
    public enum Clase {
        /** +10 % de vida maxima. */
        VIGOR,
        /** La estamina se recupera un 25 % mas deprisa. */
        ALIENTO,
        /** +15 % de mana maximo. */
        MENTE,
        /** +15 % de peso que se puede llevar. */
        PORTEADOR,
        /** -40 % de dano de fuego. */
        BRASA,
        /** -40 % de dano de hielo (y el frio de la nieve). */
        ESCARCHA,
        /** -40 % de dano del rayo. */
        TRUENO,
        /** -40 % de dano de la sombra (el marchitamiento). */
        SOMBRA,
        /** -10 % de todo el dano fisico. */
        HUESO,
        /** +20 % de ecos al matar. */
        CODICIA,
        /** +15 % de dano de los golpes del salto. */
        GARRA,
        /** +12 % de dano de los golpes fuertes (cargados). */
        FURIA,
        /** Los frascos de vida curan un 25 % mas (el Rosario del peregrino, de Oswin: parte 7ag). */
        PEREGRINO,
        /**
         * La Bendicion de Hedda (parte 7ah, a quien le dice que su hijo murio intentando abrir la puerta): un golpe que deja por
         * debajo de un cuarto de la vida, sin matar, cura un 30 % (una vez cada dos minutos; {@code mision.Hedda}).
         */
        MADRE,
        /** La Bufanda de Tam (parte 7ah, a quien calla): cubrirse cuesta un 30 % menos de estamina y el fuego hace un 20 % menos. */
        BUFANDA,
        /**
         * El Farol de Ylva (parte 7an, a quien le devuelve el farol de su padre): el fuego frio del faro te trae de vuelta; si caes otra
         * vez antes de recuperar tu espada de luz, sus ecos no se pierden: pasan a la nueva ({@code Rol.muerte}).
         */
        YLVA,
        /**
         * El Farol del ballenero (parte 7an, a quien se queda el farol de Brann): la luz del vigia; los enemigos a 24 bloques se ven a
         * traves de las paredes (solo quien lo lleva: {@code Enemigo.BRILLA}, lo pone el cliente).
         */
        BALLENERO,
        /**
         * El Farol de Corwen (parte 7ao, a quien le deja ir a recibir las barcas): su luz nunca se perdio en la niebla. Los enemigos que
         * esperan tumbados, flotando o rezando no reparan en quien lo lleva hasta tenerlo casi encima ({@code Enemigo.percibe}: a la
         * mitad de distancia).
         */
        FAROLERO;

        /** "vigor", "aliento"... (el objeto es darkcraft:talisman_ + esto). */
        public final String id = name().toLowerCase(java.util.Locale.ROOT);
    }

    public final Clase clase;

    public Talisman(Clase clase, Properties p) {
        super(p);
        this.clase = clase;
    }

    public static Clase de(ItemStack st) {
        return st.getItem() instanceof Talisman t ? t.clase : null;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.talisman_" + clase.id + ".desc").withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("darkcraft.talisman.como").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
