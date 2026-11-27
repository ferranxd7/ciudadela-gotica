package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.montura.Corcel;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Ficha;
import com.darkcraft.story.Logros;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lo que pasa en la subida por la cadena del sur (parte 7aa), cada tick, a cada jugador que este por ella:
 * <ul>
 * <li><b>La Sombra le habla</b> (una vez a cada jugador, se apunta en su ficha): al poner el pie en el camino (su frase 21), al
 * llegar al Descansillo (22) y al entrar en la Boca (23).</li>
 * <li><b>Las rafagas</b>: por el camino (no en el Descansillo ni en la Boca) cada 22-40 s sopla una de lado; primero se oye venir
 * (un segundo) y luego empuja un segundo y medio ({@link Net.Rafaga}: la lleva el cliente). Agachado apenas mueve; saltando, mucho.</li>
 * <li><b>Caer</b>: quien cae de la cadena y pasa {@link #CAIDA} bloques por debajo del camino muere (el crater esta muy abajo;
 * como el vacio del mundo de las nubes: la espada de luz se queda en el ultimo suelo firme). Con el fruto de la pluma, no.</li>
 * <li><b>Sin corcel</b>: el corcel no sube a la cadena (no se le puede llamar en ella y, si se entra montado, se deshace).</li>
 * <li>Los logros de la subida: {@code cadena}, {@code descansillo}, {@code boca} y {@code ciudadela} (llegar al patio).</li>
 * </ul>
 */
public final class Cadena {
    private Cadena() {}

    /** Lo que se puede caer por debajo del camino antes de que sea la muerte. */
    public static final double CAIDA = 28;
    /** Cada cuanto sopla (ticks): de RAFAGA_MIN a RAFAGA_MAX; la primera, entre PRIMERA y el doble. */
    public static final int RAFAGA_MIN = 440, RAFAGA_MAX = 800, PRIMERA = 200;
    /** La rafaga: lo que se oye venir, lo que empuja y lo fuerte (lo que suma a la velocidad cada tick, en lo mas fuerte). */
    public static final int AVISO = 30, EMPUJA = 30;
    /** (0,075 en la build 185: de pie echaba de la cadena desde el centro del camino; ahora unos dos bloques de pie en el suelo) */
    public static final float FUERZA = 0.05f;
    /** La marca de la ficha: lo que ya le ha dicho la Sombra en la cadena (bit 1 la 21, 2 la 22, 4 la 23; y 8 la 24, al entrar en la torre
     * del homenaje: {@link CasaSombra}). */
    public static final String MARCA = "sombra_cadena";

    /** Para la prueba: las rafagas que han soplado, los que han caido, los corceles que se han deshecho y las frases dichas. */
    public static int RAFAGAS, CAIDOS, CORCELES, FRASES;

    private static final class Estado {
        long rafaga = -1, camino = Long.MIN_VALUE / 4;
    }

    private static final Map<UUID, Estado> ESTADOS = new HashMap<>();
    private static final RandomSource R = RandomSource.create();

    /** Si (x, z) queda cerca de la cadena del sur (para no mirar nada mas a los que estan lejos). */
    public static boolean cerca(double x, double z) {
        return Math.abs(x) < 80 && z > Subida.Z_PUERTA - 30 && z < Subida.Z_ANCLA + 30;
    }

    public static void tick(ServerLevel m) {
        long t = m.getGameTime();
        for (ServerPlayer p : m.players()) {
            if (p.isSpectator() || !p.isAlive() || !cerca(p.getX(), p.getZ())) {
                if (!p.isAlive()) ESTADOS.remove(p.getUUID());
                continue;
            }
            Estado e = ESTADOS.computeIfAbsent(p.getUUID(), k -> new Estado());
            double x = p.getX(), y = p.getY(), z = p.getZ();
            boolean camino = Subida.enCamino(x, y, z), boca = Subida.enBoca(x, y, z), cadena = Subida.sobreCadena(x, y, z);
            boolean descansillo = camino && Subida.enDescansillo(x, y, z);
            if (camino) e.camino = t;
            // lo que dice la Sombra y los logros (cada medio segundo basta)
            if ((t + p.getId()) % 10 == 0) {
                if (camino && z < Subida.Z_ANCLA - 4) {
                    decir(p, 21, 1);
                    Logros.dar(p, "cadena");
                }
                if (descansillo) {
                    decir(p, 22, 2);
                    Logros.dar(p, "descansillo");
                }
                if (boca) {
                    decir(p, 23, 4);
                    Logros.dar(p, "boca");
                }
                if (Subida.enCiudadela(x, y, z)) Logros.dar(p, "ciudadela");
            }
            // el corcel no sube a la cadena
            if ((cadena || boca) && p.getVehicle() instanceof Corcel c) {
                c.deshacer(false);
                CORCELES++;
                Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.corcel.cadena", 0));
            }
            // caer de la cadena
            if (!p.isCreative() && !p.onGround() && t - e.camino < 400 && Math.abs(x) < 70 && z > Subida.Z_BOCA && z < Subida.Z_ANCLA
                    && y < Subida.suelo(z) - CAIDA && !p.hasEffect(com.darkcraft.rol.Efectos.PLUMA.get())) {
                CAIDOS++;
                DarkCraftMod.LOGGER.info("[DarkCraft] cadena: {} cae de la cadena en z {} (y {}, el camino a {})", p.getGameProfile().getName(),
                        Math.round(z), Math.round(y), Math.round(Subida.suelo(z)));
                p.hurt(p.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                continue;
            }
            // el viento
            boolean sopla = camino && !descansillo && !boca && !p.getAbilities().flying;
            if (sopla) {
                if (e.rafaga < 0) e.rafaga = t + PRIMERA + R.nextInt(PRIMERA);
                else if (t >= e.rafaga) {
                    rafaga(m, p, R.nextBoolean() ? 1 : -1);
                    e.rafaga = t + RAFAGA_MIN + R.nextInt(RAFAGA_MAX - RAFAGA_MIN);
                }
            } else if (e.rafaga >= 0 && e.rafaga < t + 60) e.rafaga = t + 60;
        }
    }

    /** Una rafaga para ese jugador hacia {@code lado} (+1 este, -1 oeste). */
    public static void rafaga(ServerLevel m, ServerPlayer p, int lado) {
        RAFAGAS++;
        Net.to(p, new Net.Rafaga(lado, AVISO, EMPUJA, FUERZA));
        // se oye venir del lado del que sopla (para todos los de alrededor)
        m.playSound(null, p.getX() - lado * 6, p.getEyeY(), p.getZ(), DarkCraftMod.SFX_CADENA_RAFAGA.get(), SoundSource.AMBIENT, 1.6f,
                0.9f + R.nextFloat() * 0.2f);
    }

    /** La frase {@code n} de la Sombra, si a ese jugador aun no se la ha dicho. */
    static void decir(ServerPlayer p, int n, int bit) {
        int ya = Ficha.dialogo(p, MARCA);
        if ((ya & bit) != 0) return;
        Ficha.dialogo(p, MARCA, ya | bit);
        Net.to(p, new Net.SombraDice(n));
        FRASES++;
        DarkCraftMod.LOGGER.info("[DarkCraft] cadena: la Sombra le dice a {} su frase {}", p.getGameProfile().getName(), n);
    }

    /** Para la prueba y los comandos: que vuelva a decirlo todo. */
    public static void olvidar(ServerPlayer p) {
        Ficha.dialogo(p, MARCA, 0);
        ESTADOS.remove(p.getUUID());
    }

    /** Para la prueba: la proxima rafaga de ese jugador, ya. */
    public static void rafagaYa(ServerPlayer p) {
        Estado e = ESTADOS.computeIfAbsent(p.getUUID(), k -> new Estado());
        e.rafaga = 0;
    }

    /** Para la prueba: que no sople en un rato (ticks). */
    public static void calma(ServerPlayer p, int ticks) {
        Estado e = ESTADOS.computeIfAbsent(p.getUUID(), k -> new Estado());
        e.rafaga = p.level().getGameTime() + ticks;
    }

    public static String resumen(ServerPlayer p) {
        Estado e = ESTADOS.get(p.getUUID());
        long t = p.level().getGameTime();
        return "en el camino " + Subida.enCamino(p.getX(), p.getY(), p.getZ()) + ", en el Descansillo " + Subida.enDescansillo(p.getX(), p.getY(), p.getZ())
                + ", en la Boca " + Subida.enBoca(p.getX(), p.getY(), p.getZ()) + ", la Sombra " + Ficha.dialogo(p, MARCA)
                + ", proxima rafaga " + (e == null || e.rafaga < 0 ? "-" : (e.rafaga - t) + " ticks") + "; rafagas " + RAFAGAS + ", caidos " + CAIDOS
                + ", corceles " + CORCELES + ", frases " + FRASES;
    }
}
