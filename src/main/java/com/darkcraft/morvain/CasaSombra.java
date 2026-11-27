package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Isolde;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.net.Net;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lo que pasa en la casa de la Sombra (la torre del homenaje de la ciudadela por dentro, parte 7ab), desde {@link Morvain#onTick}:
 * <ul>
 * <li><b>El rastrillo</b> de la puerta: cerrado al generarse; la primera vez que alguien llega ante la puerta sube (los barrotes se van de
 * abajo arriba, con su estruendo) y se queda abierto para siempre (se apunta en los datos de Morvain).</li>
 * <li><b>La Sombra habla</b> al entrar (su frase 24, una vez a cada jugador: el bit 8 de la marca de la cadena) y el logro
 * {@code homenaje}; junto a la Ultima Llama encendida, el logro {@code ultima_llama}.</li>
 * <li><b>Maelis</b> se sienta una vez en la capilla negra, junto a la Ultima Llama, mirando hacia donde se llega.</li>
 * <li><b>El sello del salon del trono</b>: mientras la reina pelea, barreras sobre lo alto de la escalera que sube de la capilla (con
 * una cortina de chispas rojas y negras); quien este en medio pasa al salon. Al acabar la pelea o al caer ella, se abre.</li>
 * <li><b>La reja de la escalera de la azotea</b>: cerrada mientras viva la reina; al caer, se abre (y si vuelve, se cierra).</li>
 * </ul>
 */
public final class CasaSombra {
    private CasaSombra() {}

    /** El rastrillo de la puerta: se abre cuando alguien llega a menos de esto de su centro (y a la altura del patio). */
    public static final double LLEGA = 10;
    /** Las marcas: el rastrillo abierto y Maelis en la capilla (en los datos de Morvain). */
    static final String RASTRILLO = "homenaje_rastrillo", GUIA = "homenaje_guia";
    /** El bit de la frase 24 de la Sombra en la marca de la cadena. */
    static final int BIT_SOMBRA = 8;
    /** El sello del salon del trono: lo alto de la escalera que sube de la capilla (x, z) y su altura (de lo que se pisa). */
    public static final int SX0 = -6, SX1 = -1, SZ0 = -23, SZ1 = -20;
    static final DustParticleOptions ROJO = new DustParticleOptions(new Vector3f(0.8f, 0.08f, 0.08f), 1.2f),
            NEGRO = new DustParticleOptions(new Vector3f(0.06f, 0.03f, 0.07f), 1.4f);

    /** Para la prueba: veces que se ha abierto el rastrillo, que se ha sellado el salon y que se ha abierto la reja; frases dichas. */
    public static int RASTRILLOS, SELLOS, REJAS, FRASES, LLAMAS;

    private static int rastrilloT = -1;
    private static boolean sellado;
    private static final Map<UUID, Long> AVISADO = new HashMap<>();

    /** Lo alto de la escalera del trono (donde se pone el sello). */
    public static List<BlockPos> sello() {
        List<BlockPos> l = new ArrayList<>();
        int y0 = Homenaje.pisa(Homenaje.TRONO);
        for (int y = y0; y <= y0 + 2; y++)
            for (int x = SX0; x <= SX1; x++)
                for (int z = SZ0; z <= SZ1; z++) l.add(new BlockPos(x, y, z));
        return l;
    }

    public static void tick(ServerLevel m) {
        long t = m.getGameTime();
        Morvain.Datos d = null;
        // el rastrillo que sube (una fila cada cuatro ticks)
        if (rastrilloT >= 0) subir(m);
        // el sello mientras la reina pelea (y su cortina)
        boolean pelea = Puestos.enemigo(MorvainEnemigos.HOMENAJE, Homenaje.ISOLDE) instanceof Isolde i && i.isAlive() && i.enPelea();
        if (pelea != sellado && m.isLoaded(new BlockPos(SX0, Homenaje.pisa(Homenaje.TRONO), SZ0))) sellar(m, pelea);
        if (sellado && t % 3 == 0) cortina(m);
        boolean alguien = false;
        for (ServerPlayer p : m.players()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            double x = p.getX(), y = p.getY(), z = p.getZ();
            if (Math.abs(x) > Ciudadela.A || Math.abs(z) > Ciudadela.A || y < Ciudadela.TOP - 4 || y > Homenaje.SUELO[Homenaje.AZOTEA] + 40) continue;
            alguien = true;
            if ((t + p.getId()) % 10 != 0) continue;
            if (d == null) d = Morvain.Datos.get(m.getServer());
            // ante la puerta: sube el rastrillo (la primera vez)
            if (!d.hecho(RASTRILLO) && rastrilloT < 0 && Math.abs(x + 0.5) < LLEGA && Math.abs(z - (Homenaje.PZ_FUERA + 2)) < LLEGA
                    && Math.abs(y - Homenaje.pisa(Homenaje.BAJA)) < 4)
                abrirRastrillo(m);
            // dentro: la Sombra y el logro
            if (Homenaje.enHomenaje(x, y, z)) {
                Cadena.decir(p, 24, BIT_SOMBRA);
                Logros.dar(p, "homenaje");
            }
            // junto a la Ultima Llama (encendida)
            if (Homenaje.enCapilla(x, y, z) && p.distanceToSqr(Homenaje.SANT_X + 0.5, Homenaje.pisa(Homenaje.CAPILLA), Homenaje.SANT_Z + 0.5) < 5 * 5
                    && encendida(m) && !Logros.tiene(p, "ultima_llama")) {
                Logros.dar(p, "ultima_llama");
                LLAMAS++;
            }
            // los avisos: el sello del salon (desde la escalera) y la reja de la azotea (con la reina en pie)
            if (sellado && Homenaje.planta(y) == Homenaje.CAPILLA && Homenaje.enTramo(Homenaje.CAPILLA, (int) Math.floor(x), (int) Math.floor(z), 0)
                    && y > Homenaje.pisa(Homenaje.CAPILLA) + 6)
                avisar(p, t, "darkcraft.homenaje.sello");
            int[] rj = Homenaje.get().sitio("reja_azotea");
            if (Homenaje.planta(y) == Homenaje.TRONO && p.distanceToSqr(rj[0] + 0.5, rj[1], rj[2] + 0.5) < 3.5 * 3.5 && !reinaCaida(m))
                avisar(p, t, "darkcraft.homenaje.reja");
            int[] ps = Homenaje.get().sitio("puerta_sombra");
            if (Homenaje.enAzotea(x, y, z) && p.distanceToSqr(ps[0] + 0.5, ps[1], ps[2] + 0.5) < 4 * 4) avisar(p, t, "darkcraft.homenaje.aguja");
        }
        if (!alguien) return;
        if (t % 20 == 3) {
            if (d == null) d = Morvain.Datos.get(m.getServer());
            mantener(m, d);
        }
    }

    /** Pone ya como toca el rastrillo, la reja de la azotea y la guia (lo que se hace cada segundo con alguien cerca; para las pruebas). */
    public static void revisar(ServerLevel m) {
        mantener(m, Morvain.Datos.get(m.getServer()));
    }

    private static void avisar(ServerPlayer p, long t, String clave) {
        Long antes = AVISADO.get(p.getUUID());
        if (antes != null && t - antes < 200 && t >= antes) return;
        AVISADO.put(p.getUUID(), t);
        Net.to(p, new Net.Cartel(Net.Cartel.AVISO, clave, 0));
    }

    /** Si la Ultima Llama esta encendida. */
    static boolean encendida(ServerLevel m) {
        BlockPos q = SantuariosMorvain.ULTIMA.pos();
        if (!m.isLoaded(q)) return false;
        BlockState s = m.getBlockState(q);
        return s.is(DarkCraftMod.SANTUARIO.get()) && s.getValue(com.darkcraft.santuario.SantuarioBlock.ENCENDIDO);
    }

    /** Si la reina ha caido (su puesto esta por caido). */
    public static boolean reinaCaida(ServerLevel m) {
        return Puestos.muerto(m.getServer(), MorvainEnemigos.HOMENAJE, Homenaje.ISOLDE);
    }

    // ------------------------------------------------------------------ el rastrillo de la puerta

    /** Empieza a subir el rastrillo (y lo apunta: ya no se cierra). */
    public static void abrirRastrillo(ServerLevel m) {
        Morvain.Datos.get(m.getServer()).marcar(RASTRILLO);
        rastrilloT = 0;
        RASTRILLOS++;
        BlockPos c = new BlockPos(0, Homenaje.pisa(Homenaje.BAJA) + 3, Homenaje.PZ_MURO);
        m.playSound(null, c, DarkCraftMod.SFX_PUERTA_HOMENAJE.get(), SoundSource.BLOCKS, 2.6f, 1.0f);
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: se abre el rastrillo de la torre del homenaje");
    }

    private static void subir(ServerLevel m) {
        int k = rastrilloT++;
        if (k % 4 != 0) return;
        int fila = Homenaje.pisa(Homenaje.BAJA) + k / 4;
        boolean quedan = false;
        for (BlockPos q : Homenaje.rastrillo()) {
            if (q.getY() == fila && m.isLoaded(q) && m.getBlockState(q).is(Blocks.IRON_BARS)) {
                m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                m.sendParticles(ParticleTypes.SMOKE, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.01);
            }
            if (q.getY() > fila) quedan = true;
        }
        if (!quedan) rastrilloT = -1;
    }

    // ------------------------------------------------------------------ cada segundo: lo que tiene que estar como toca

    private static void mantener(ServerLevel m, Morvain.Datos d) {
        // un sello que se quedo puesto (el servidor se cerro en mitad de la pelea): fuera
        if (!sellado && m.isLoaded(new BlockPos(SX0, Homenaje.pisa(Homenaje.TRONO), SZ0))
                && m.getBlockState(new BlockPos(SX0, Homenaje.pisa(Homenaje.TRONO), SZ0)).is(Blocks.BARRIER)
                && !(Puestos.enemigo(MorvainEnemigos.HOMENAJE, Homenaje.ISOLDE) instanceof Isolde i && i.isAlive() && i.enPelea()))
            sellar(m, false);
        // el rastrillo, abierto si ya se abrio (en un chunk que se vuelva a cargar con los barrotes, se quitan)
        if (d.hecho(RASTRILLO) && rastrilloT < 0)
            for (BlockPos q : Homenaje.rastrillo())
                if (m.isLoaded(q) && m.getBlockState(q).is(Blocks.IRON_BARS)) m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        // la reja de la azotea: abierta con la reina caida; cerrada si no
        boolean abierta = reinaCaida(m);
        List<BlockPos> reja = Homenaje.rejaAzotea();
        if (m.isLoaded(reja.get(0)) && m.isLoaded(reja.get(reja.size() - 1))) {
            boolean cambio = false;
            for (BlockPos q : reja) {
                BlockState s = m.getBlockState(q);
                if (abierta && s.is(Blocks.IRON_BARS)) {
                    m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    m.sendParticles(ParticleTypes.SOUL, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 3, 0.2, 0.3, 0.2, 0.02);
                    cambio = true;
                } else if (!abierta && s.isAir() && m.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new AABB(q)).isEmpty()) {
                    m.setBlock(q, Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
                    cambio = true;
                }
            }
            if (cambio && abierta) {
                REJAS++;
                BlockPos c = reja.get(reja.size() / 2);
                m.playSound(null, c, DarkCraftMod.SFX_PUERTA_HOMENAJE.get(), SoundSource.BLOCKS, 1.6f, 1.25f);
                DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: se abre la reja de la escalera de la azotea (la reina ha caido)");
            }
        }
        // Maelis en la capilla, junto a la Ultima Llama (una vez)
        if (!d.hecho(GUIA)) {
            BlockPos g = new BlockPos(Homenaje.GUIA_X, Homenaje.pisa(Homenaje.CAPILLA), Homenaje.GUIA_Z);
            if (m.isLoaded(g) && m.isLoaded(SantuariosMorvain.ULTIMA.pos())) {
                // mirando hacia donde se llega (lo alto de la escalera que sube de la prision, al sureste)
                if (com.darkcraft.personajes.Guia.poner(m, g, new BlockPos(6, g.getY(), 18), SantuariosMorvain.ULTIMA.pos()) != null) {
                    d.marcar(GUIA);
                    DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: la guia, junto a la Ultima Llama de la capilla: {}", g.toShortString());
                }
            }
        }
    }

    // ------------------------------------------------------------------ el sello del salon del trono

    /** Pone o quita el sello (a quien este en medio al cerrarse, se le pasa al salon). */
    public static void sellar(ServerLevel m, boolean cerrar) {
        sellado = cerrar;
        List<BlockPos> l = sello();
        if (cerrar) {
            SELLOS++;
            AABB caja = new AABB(SX0, l.get(0).getY() - 1, SZ0, SX1 + 1, l.get(0).getY() + 3, SZ1 + 1);
            int[] e = Homenaje.get().sitio("trono_entrada");
            for (ServerPlayer p : m.getEntitiesOfClass(ServerPlayer.class, caja))
                p.teleportTo(e[0] + 0.5, e[1] + 0.05, e[2] + 0.5);
        }
        for (BlockPos q : l) {
            BlockState s = m.getBlockState(q);
            if (cerrar && s.isAir()) m.setBlock(q, Blocks.BARRIER.defaultBlockState(), Block.UPDATE_CLIENTS);
            else if (!cerrar && s.is(Blocks.BARRIER)) m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockPos c = new BlockPos((SX0 + SX1) / 2, l.get(0).getY() + 1, (SZ0 + SZ1) / 2);
        m.playSound(null, c, cerrar ? DarkCraftMod.SFX_ISOLDE_DESVANECE.get() : DarkCraftMod.SFX_SELLO_ROMPE.get(), SoundSource.BLOCKS, 1.6f, cerrar ? 0.7f : 1.0f);
        m.sendParticles(ParticleTypes.SOUL, c.getX() + 0.5, c.getY(), c.getZ() + 0.5, 30, 2.5, 1.0, 1.6, 0.03);
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: el salon del trono {}", cerrar ? "queda sellado (la reina pelea)" : "se abre");
    }

    /** La cortina de chispas del sello (por el lado del salon y por encima de la escalera). */
    private static void cortina(ServerLevel m) {
        int y0 = Homenaje.pisa(Homenaje.TRONO);
        for (int i = 0; i < 6; i++) {
            double x = SX0 + m.random.nextDouble() * (SX1 - SX0 + 1), z = SZ0 + m.random.nextDouble() * (SZ1 - SZ0 + 1), y = y0 + m.random.nextDouble() * 3;
            m.sendParticles(m.random.nextBoolean() ? ROJO : NEGRO, x, y, z, 1, 0.1, 0.1, 0.1, 0.0);
        }
        if (m.random.nextFloat() < 0.3f)
            m.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, SX0 + m.random.nextDouble() * 6, y0 + 0.2, SZ0 + m.random.nextDouble() * 4, 1, 0.1, 0.1, 0.1, 0.01);
    }

    public static boolean sellado() {
        return sellado;
    }

    // ------------------------------------------------------------------ para las pruebas y los comandos

    /** Vuelve a cerrar el rastrillo (pone sus barrotes) y lo olvida: se abrira otra vez al llegar alguien. */
    public static void cerrarRastrillo(ServerLevel m) {
        Morvain.Datos.get(m.getServer()).quitar(RASTRILLO);
        rastrilloT = -1;
        for (BlockPos q : Homenaje.rastrillo()) if (m.isLoaded(q)) m.setBlock(q, Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** Abre ya el rastrillo (de golpe). */
    public static void abrirRastrilloYa(ServerLevel m) {
        Morvain.Datos.get(m.getServer()).marcar(RASTRILLO);
        rastrilloT = -1;
        for (BlockPos q : Homenaje.rastrillo()) if (m.isLoaded(q) && m.getBlockState(q).is(Blocks.IRON_BARS)) m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    public static boolean rastrilloAbierto(ServerLevel m) {
        return Morvain.Datos.get(m.getServer()).hecho(RASTRILLO);
    }

    /** Que la Sombra vuelva a decir su frase 24 a ese jugador. */
    public static void olvidar(ServerPlayer p) {
        int ya = com.darkcraft.rol.Ficha.dialogo(p, Cadena.MARCA);
        com.darkcraft.rol.Ficha.dialogo(p, Cadena.MARCA, ya & ~BIT_SOMBRA);
    }

    /** Como estan las cosas de la casa (para la prueba y el comando). */
    public static String resumen(ServerLevel m) {
        int barreras = 0, reja = 0;
        for (BlockPos q : sello()) if (m.isLoaded(q) && m.getBlockState(q).is(Blocks.BARRIER)) barreras++;
        for (BlockPos q : Homenaje.rejaAzotea()) if (m.isLoaded(q) && m.getBlockState(q).is(Blocks.IRON_BARS)) reja++;
        Enemigo e = Puestos.enemigo(MorvainEnemigos.HOMENAJE, Homenaje.ISOLDE);
        return "rastrillo " + (rastrilloAbierto(m) ? "abierto" : "cerrado") + ", sello " + (sellado ? "cerrado" : "abierto") + " (" + barreras + "/"
                + sello().size() + " barreras), reja de la azotea " + reja + "/" + Homenaje.rejaAzotea().size() + ", la reina "
                + (reinaCaida(m) ? "caida" : e == null ? "esperando" : e instanceof Isolde i && i.enPelea() ? "peleando" : "fuera")
                + ", Ultima Llama " + (encendida(m) ? "encendida" : "apagada") + ", guia " + (Morvain.Datos.get(m.getServer()).hecho(GUIA) ? "SI" : "NO")
                + "; rastrillos " + RASTRILLOS + ", sellos " + SELLOS + ", rejas " + REJAS + ", llamas " + LLAMAS;
    }

    /** Al parar el servidor. */
    public static void parar() {
        rastrilloT = -1;
        sellado = false;
        AVISADO.clear();
    }

}
