package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Sombra;
import com.darkcraft.fortaleza.CieloRojo;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Ficha;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * El Corazon de la Aguja (parte 7ac): lo que pasa en lo alto de la ciudadela, desde {@link Morvain#onTick}.
 * <ul>
 * <li><b>La puerta de la aguja</b> (en la azotea, al norte): al llegar ante ella la Sombra dice su frase 25 ("Ahi esta... la puerta que
 * nunca pude abrir. Pon la mano en ella, marcado. Dejame salir."; una vez a cada uno, el bit 16 de la marca de la cadena). Se abre
 * poniendo la mano (la tecla del aviso o clic derecho en ella; con la reina Isolde ya caida): el sello se deshace de dentro afuera
 * con su estruendo y queda abierta para siempre (en los datos de Morvain).</li>
 * <li><b>El sello</b> mientras pelea la Sombra: barreras en el hueco de la puerta (con su cortina de chispas); quien este en medio pasa
 * dentro. Al acabar la pelea, o al rendirse ella, se abre.</li>
 * <li><b>El corazon</b>: rendida la Sombra ({@link Sombra#rendirse}), su corazon ({@link Corazon}) flota delante de ella hasta que alguien
 * responde: <b>golpearlo</b> (golpe con el arma o la respuesta del dialogo) o <b>tomarlo</b>. Entonces la Sombra dice su ultima frase y
 * se deshace; al morir, el cielo rojo se apaga en todo el mundo (y en Morvain) y se apunta como acabo (las dos cosas para todos).</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Aguja {
    private Aguja() {}

    /** La marca de la cadena en la ficha (las frases de la Sombra ya dichas a cada uno). */
    public static final String MARCA = Cadena.MARCA;
    /** El bit de la frase 25 (ante la puerta). */
    public static final int BIT_PUERTA = 16;
    /** Lo que se apunta en los datos de Morvain: la puerta abierta, la Sombra rendida y como acabo. */
    static final String PUERTA = "aguja_puerta", RENDIDA = "sombra_rendida", FINAL_ALBA = "final_alba", FINAL_SOMBRA = "final_sombra";
    /** En la ficha de cada uno: lo que respondio (1 golpeo el corazon, 2 lo tomo). */
    public static final String FICHA_FINAL = "sombra_final";
    /** Desde donde se pone la mano en la puerta (bloques del centro de su cara de fuera). */
    public static final double MANO = 3.6;
    /** La etiqueta del corazon. */
    public static final String TAG_CORAZON = "darkcraft_corazon_sombra";
    static final DustParticleOptions ROJO = new DustParticleOptions(new Vector3f(0.8f, 0.08f, 0.08f), 1.2f),
            NEGRO = new DustParticleOptions(new Vector3f(0.06f, 0.03f, 0.07f), 1.4f);

    /** Para la prueba: puertas abiertas, sellos, corazones puestos, elecciones y frases 25 dichas. */
    public static int ABIERTAS, SELLOS, CORAZONES, ELECCIONES, FRASES;

    private static int abriendo = -1;
    /** Las veces que se ha llamado a {@link #tick} (las pruebas lo llaman sin que pase el tiempo del mundo). */
    private static long tic;
    private static boolean sellado, mantenida;
    /** Los que oyeron a la Sombra ofrecer su corazon (para empezar su dialogo por el menu). */
    private static final Set<UUID> OYERON = new HashSet<>();

    // ------------------------------------------------------------------ donde

    /** El centro de la cara de fuera de la puerta (a la altura de la mano). */
    public static Vec3 puerta() {
        return new Vec3(0.0, Homenaje.pisa(Homenaje.AZOTEA) + 1.5, Ciudadela.RAG + 0.6);
    }

    /** Si la puerta esta cerrada (queda alguno de los bloques de su sello). */
    public static boolean cerrada(ServerLevel m) {
        for (BlockPos q : Homenaje.puertaAguja()) if (m.isLoaded(q) && !m.getBlockState(q).isAir() && !m.getBlockState(q).is(Blocks.BARRIER)) return true;
        return false;
    }

    public static boolean abierta(MinecraftServer srv) {
        return Morvain.Datos.get(srv).hecho(PUERTA);
    }

    /** Si la Sombra esta rendida esperando respuesta (se guarda: si el servidor se cierra, vuelve a salir de rodillas). */
    public static boolean rendida(MinecraftServer srv) {
        return srv != null && Morvain.Datos.get(srv).hecho(RENDIDA);
    }

    /** Como acabo (null si aun no): "alba" (se golpeo el corazon) o "sombra" (se tomo). */
    public static String final_(MinecraftServer srv) {
        Morvain.Datos d = Morvain.Datos.get(srv);
        return d.hecho(FINAL_SOMBRA) ? "sombra" : d.hecho(FINAL_ALBA) ? "alba" : null;
    }

    /** La Sombra que esta fuera (o null). */
    public static Sombra sombra() {
        Enemigo e = Puestos.enemigo(MorvainEnemigos.AGUJA, Homenaje.SOMBRA);
        return e instanceof Sombra s && !s.isRemoved() ? s : null;
    }

    /** Si la Sombra ha caido (su puesto). */
    public static boolean caida(ServerLevel m) {
        return Puestos.muerto(m.getServer(), MorvainEnemigos.AGUJA, Homenaje.SOMBRA);
    }

    // ------------------------------------------------------------------ cada tick

    public static void tick(ServerLevel m) {
        long t = ++tic;
        if (abriendo >= 0) deshacerSello(m);
        Sombra s = sombra();
        boolean pelea = s != null && s.isAlive() && s.enPelea();
        if (pelea != sellado && m.isLoaded(BlockPos.containing(puerta()))) sellar(m, pelea);
        if (sellado && t % 3 == 0) cortina(m);
        if (t % 10 == 7) corazon(m, s);
        boolean alguien = false;
        for (ServerPlayer p : m.players()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            double x = p.getX(), y = p.getY(), z = p.getZ();
            if (Math.abs(x) > 40 || Math.abs(z) > 40 || y < Homenaje.SUELO[Homenaje.AZOTEA] - 6 || y > Homenaje.TECHO_AGUJA + 10) continue;
            alguien = true;
            if ((t + p.getId()) % 10 != 0) continue;
            // ante la puerta (aun cerrada): la Sombra
            if (Homenaje.enAzotea(x, y, z) && p.distanceToSqr(puerta()) < 9 * 9 && !abierta(m.getServer())) decir(p, 25, BIT_PUERTA);
            // dentro de la sala: el logro
            if (Homenaje.enAguja(x, y + 0.1, z)) Logros.dar(p, "aguja");
        }
        if (alguien && !mantenida && t % 20 == 11) mantener(m);
    }

    /** La frase n de la Sombra a ese jugador, si aun no se la ha dicho (el bit de la marca de la cadena). */
    static void decir(ServerPlayer p, int n, int bit) {
        int ya = Ficha.dialogo(p, MARCA);
        if ((ya & bit) != 0) return;
        Ficha.dialogo(p, MARCA, ya | bit);
        Net.to(p, new Net.SombraDice(n));
        FRASES++;
        DarkCraftMod.LOGGER.info("[DarkCraft] aguja: la Sombra le dice a {} su frase {}", p.getGameProfile().getName(), n);
    }

    /** Una vez por sesion: la puerta abierta si ya se abrio (un chunk que se vuelva a levantar la traeria cerrada). */
    private static void mantener(ServerLevel m) {
        if (!m.isLoaded(BlockPos.containing(puerta()))) return;
        mantenida = true;
        if (abierta(m.getServer()) && cerrada(m) && abriendo < 0)
            for (BlockPos q : Homenaje.puertaAguja()) if (m.isLoaded(q)) m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        // un sello que se quedo puesto (el servidor se cerro en mitad de la pelea): fuera
        if (!sellado) for (BlockPos q : Homenaje.puertaAguja())
            if (m.isLoaded(q) && m.getBlockState(q).is(Blocks.BARRIER)) m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    // ------------------------------------------------------------------ la puerta

    /** El jugador pone la mano en la puerta (la tecla del aviso o clic derecho en ella). */
    public static void tocar(ServerPlayer p) {
        if (!(p.level() instanceof ServerLevel m) || !Morvain.es(m) || p.isSpectator()) return;
        if (p.distanceToSqr(puerta()) > (MANO + 1.5) * (MANO + 1.5)) return;
        if (abierta(m.getServer()) && !cerrada(m)) return;
        if (!CasaSombra.reinaCaida(m) && !p.isCreative()) {
            Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.aguja.no_cede", 0));
            return;
        }
        abrir(m, false);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} pone la mano en la puerta de la aguja", p.getGameProfile().getName());
    }

    /** Abre la puerta: el sello se deshace de dentro afuera (o de golpe, {@code ya}); queda apuntado. */
    public static void abrir(ServerLevel m, boolean ya) {
        Morvain.Datos.get(m.getServer()).marcar(PUERTA);
        ABIERTAS++;
        Vec3 c = puerta();
        m.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_PUERTA_AGUJA.get(), SoundSource.BLOCKS, 2.8f, 1.0f);
        m.sendParticles(ParticleTypes.SOUL, c.x, c.y + 0.5, c.z, 30, 1.0, 1.4, 0.4, 0.04);
        if (ya) {
            abriendo = -1;
            for (BlockPos q : Homenaje.puertaAguja()) if (m.isLoaded(q)) m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else abriendo = 0;
    }

    /** El sello que se deshace: primero las gemas del centro, luego lo demas de abajo arriba (un bloque cada pocos ticks). */
    private static void deshacerSello(ServerLevel m) {
        int k = abriendo++;
        List<BlockPos> l = new ArrayList<>(Homenaje.puertaAguja());
        l.removeIf(q -> !m.isLoaded(q) || m.getBlockState(q).isAir());
        if (l.isEmpty()) {
            abriendo = -1;
            return;
        }
        if (k < 30) {
            // primero tiembla y se encienden las grietas
            if (k % 4 == 0) for (BlockPos q : l) m.sendParticles(k % 8 == 0 ? ROJO : NEGRO, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 1.05, 1, 0.3, 0.3, 0.05, 0.0);
            return;
        }
        if (k % 3 != 0) return;
        Vec3 c = puerta();
        l.sort(Comparator.comparingDouble((BlockPos q) -> q.getY() * 10 + Math.abs(q.getX() + 0.5) * 3 - q.getZ() * 0.1));
        BlockPos q = l.get(0);
        BlockState antes = m.getBlockState(q);
        m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        m.levelEvent(2001, q, Block.getId(antes));
        m.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.02);
        if (l.size() == 1) {
            m.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_SELLO_ROMPE.get(), SoundSource.BLOCKS, 2.0f, 0.8f);
            abriendo = -1;
        }
    }

    /** Vuelve a cerrar la puerta (pone su sello) y la olvida (para las pruebas y el comando). */
    public static void cerrar(ServerLevel m) {
        Morvain.Datos.get(m.getServer()).quitar(PUERTA);
        abriendo = -1;
        for (BlockPos q : Homenaje.puertaAguja()) if (m.isLoaded(q)) m.setBlock(q, Homenaje.sello(q.getX(), q.getY(), q.getZ()), Block.UPDATE_ALL);
    }

    /** Pone o quita las barreras del hueco de la puerta (mientras pelea la Sombra). */
    public static void sellar(ServerLevel m, boolean cerrar) {
        sellado = cerrar;
        List<BlockPos> l = Homenaje.puertaAguja();
        if (cerrar) {
            SELLOS++;
            // quien este en medio, dentro
            AABB caja = new AABB(-2, l.get(0).getY() - 1, Homenaje.R_AGUJA - 1, 2, l.get(0).getY() + 4, Ciudadela.RAG + 2);
            for (ServerPlayer p : m.getEntitiesOfClass(ServerPlayer.class, caja)) p.teleportTo(p.getX(), p.getY() + 0.05, Homenaje.R_AGUJA - 2.5);
        }
        for (BlockPos q : l) {
            BlockState s = m.getBlockState(q);
            if (cerrar && s.isAir()) m.setBlock(q, Blocks.BARRIER.defaultBlockState(), Block.UPDATE_CLIENTS);
            else if (!cerrar && s.is(Blocks.BARRIER)) m.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        Vec3 c = puerta();
        m.playSound(null, c.x, c.y, c.z, cerrar ? DarkCraftMod.SFX_SOMBRA_NIEBLA.get() : DarkCraftMod.SFX_SELLO_ROMPE.get(), SoundSource.BLOCKS, 1.6f, cerrar ? 0.7f : 1.0f);
        m.sendParticles(ParticleTypes.SOUL, c.x, c.y, c.z - 1.0, 24, 1.0, 1.2, 0.5, 0.03);
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: la puerta de la aguja {}", cerrar ? "queda sellada (la Sombra pelea)" : "se abre");
    }

    private static void cortina(ServerLevel m) {
        int y0 = Homenaje.pisa(Homenaje.AZOTEA);
        for (int i = 0; i < 5; i++) {
            double x = -1 + m.random.nextDouble() * 2, y = y0 + m.random.nextDouble() * 4, z = Homenaje.R_AGUJA + m.random.nextDouble() * 3;
            m.sendParticles(m.random.nextBoolean() ? ROJO : NEGRO, x, y, z, 1, 0.05, 0.1, 0.05, 0.0);
        }
        if (m.random.nextFloat() < 0.3f) m.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, -1 + m.random.nextDouble() * 2, y0 + 0.2, Homenaje.R_AGUJA + 1.0, 1, 0.1, 0.1, 0.1, 0.01);
    }

    public static boolean sellado() {
        return sellado;
    }

    @SubscribeEvent
    public static void onClic(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel m) || !Morvain.es(m)) return;
        BlockPos b = e.getPos();
        if (Math.abs(b.getX()) > 2 || Math.abs(b.getZ() - Ciudadela.RAG) > 3) return;
        for (BlockPos q : Homenaje.puertaAguja())
            if (q.equals(b)) {
                tocar(p);
                e.setCanceled(true);
                return;
            }
    }

    // ------------------------------------------------------------------ el corazon

    /** La Sombra se rinde: se apunta (si el servidor se cierra, vuelve a salir rendida) y quien la oye ofrecer su corazon. */
    public static void alRendirse(Sombra s) {
        if (!(s.level() instanceof ServerLevel m)) return;
        Morvain.Datos.get(m.getServer()).marcar(RENDIDA);
        OYERON.clear();
        for (ServerPlayer p : m.players()) if (p.distanceToSqr(s) < 48 * 48) OYERON.add(p.getUUID());
    }

    /** El corazon, mientras la Sombra espera respuesta: que este (delante de ella) y que no haya otro. */
    private static void corazon(ServerLevel m, Sombra s) {
        boolean quiere = s != null && s.isAlive() && s.rendida() && s.finalElegido() == 0 && s.rendidaDesde() >= 0
                && s.tickCount - s.rendidaDesde() >= Sombra.SALE_CORAZON;
        List<Corazon> hay = new ArrayList<>();
        Vec3 donde = s != null ? s.sitioCorazon() : puerta();
        for (Corazon c : m.getEntitiesOfClass(Corazon.class, new AABB(donde, donde).inflate(40))) if (!c.isRemoved()) hay.add(c);
        if (!quiere) {
            for (Corazon c : hay) c.discard();
            return;
        }
        if (hay.size() > 1) for (int i = 1; i < hay.size(); i++) hay.get(i).discard();
        if (hay.isEmpty()) {
            Corazon c = DarkCraftMod.CORAZON_SOMBRA.get().create(m);
            if (c == null) return;
            c.moveTo(donde.x, donde.y, donde.z, s.getYRot() + 180f, 0f);
            c.addTag(TAG_CORAZON);
            m.addFreshEntity(c);
            CORAZONES++;
            m.sendParticles(ROJO, donde.x, donde.y + 0.4, donde.z, 30, 0.3, 0.3, 0.3, 0.05);
            m.sendParticles(ParticleTypes.SOUL, donde.x, donde.y + 0.4, donde.z, 10, 0.2, 0.2, 0.2, 0.02);
            m.playSound(null, donde.x, donde.y, donde.z, DarkCraftMod.SFX_SOMBRA_LATIDO.get(), SoundSource.HOSTILE, 1.6f, 1.0f);
        } else if (hay.get(0).position().distanceToSqr(donde) > 0.5) hay.get(0).moveTo(donde.x, donde.y, donde.z);
    }

    /** Por donde empieza el dialogo del corazon: quien oyo la oferta, por las respuestas; quien no, por la frase 30. */
    public static int inicio(ServerPlayer p) {
        return OYERON.contains(p.getUUID()) ? com.darkcraft.dialogo.Dialogos.MENU : 30;
    }

    /** Si ya se puede responder (el corazon esta fuera y nadie ha respondido). */
    public static boolean puedeElegir() {
        Sombra s = sombra();
        return s != null && s.isAlive() && s.rendida() && s.finalElegido() == 0;
    }

    /**
     * Alguien responde: {@code golpear} (el corazon se rompe) o tomarlo (vuela a su pecho). La Sombra dice su ultima frase y se deshace
     * ({@link Sombra#acabar}); el resto, al morir ({@link #alMorir}).
     */
    public static void elegir(ServerPlayer p, Corazon c, boolean golpear) {
        if (!(p.level() instanceof ServerLevel m) || !puedeElegir() || p.distanceToSqr(c) > 7 * 7) return;
        Sombra s = sombra();
        ELECCIONES++;
        Vec3 o = c.position().add(0, 0.4, 0);
        if (golpear) {
            m.playSound(null, o.x, o.y, o.z, DarkCraftMod.SFX_CORAZON_ROMPE.get(), SoundSource.HOSTILE, 2.4f, 1.0f);
            m.sendParticles(ROJO, o.x, o.y, o.z, 60, 0.4, 0.4, 0.4, 0.2);
            m.sendParticles(ParticleTypes.DAMAGE_INDICATOR, o.x, o.y, o.z, 12, 0.3, 0.3, 0.3, 0.2);
            m.sendParticles(ParticleTypes.SOUL, o.x, o.y, o.z, 20, 0.3, 0.3, 0.3, 0.06);
        } else {
            m.playSound(null, o.x, o.y, o.z, DarkCraftMod.SFX_CORAZON_TOMA.get(), SoundSource.HOSTILE, 2.4f, 1.0f);
            // vuela a su pecho
            Vec3 pecho = p.position().add(0, 1.2, 0);
            for (int k = 0; k <= 12; k++) {
                Vec3 q = o.lerp(pecho, k / 12.0);
                m.sendParticles(ROJO, q.x, q.y, q.z, 3, 0.08, 0.08, 0.08, 0.0);
            }
            m.sendParticles(NEGRO, pecho.x, pecho.y, pecho.z, 40, 0.4, 0.6, 0.4, 0.04);
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false));
        }
        c.discard();
        s.acabar(golpear, p);
        Ficha.dialogo(p, FICHA_FINAL, golpear ? 1 : 2);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} {} el corazon de la Sombra", p.getGameProfile().getName(), golpear ? "golpea" : "toma");
    }

    /**
     * La Sombra ha muerto (tras la respuesta): se apunta como acabo (para todos), el cielo rojo se apaga (en el mundo de las nubes) y
     * los logros del final a los que estaban.
     */
    public static void alMorir(Sombra s, int final_) {
        if (!(s.level() instanceof ServerLevel m)) return;
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        d.quitar(RENDIDA);
        d.marcar(final_ == 2 ? FINAL_SOMBRA : FINAL_ALBA);
        CieloRojo.apagar(m.getServer(), true);
        for (ServerPlayer p : m.players()) {
            if (p.distanceToSqr(s) > 64 * 64) continue;
            if (Ficha.dialogo(p, FICHA_FINAL) == 2) Logros.dar(p, "nueva_sombra");
            else Logros.dar(p, "alba");
        }
        OYERON.clear();
        DarkCraftMod.LOGGER.info("[DarkCraft] La Sombra sin Nombre ha caido ({}): el cielo rojo se apaga", final_ == 2 ? "le tomaron el corazon" : "le golpearon el corazon");
    }

    // ------------------------------------------------------------------ para las pruebas y los comandos

    /** Vuelve a dejarlo todo como al principio (la puerta cerrada, la Sombra sin rendir ni vencida, sin final). */
    public static void reiniciar(ServerLevel m) {
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        d.quitar(RENDIDA);
        d.quitar(FINAL_ALBA);
        d.quitar(FINAL_SOMBRA);
        OYERON.clear();
        Sombra s = sombra();
        if (s != null) s.discard();
        Puestos.marcar(m.getServer(), MorvainEnemigos.AGUJA, Homenaje.SOMBRA, false);
    }

    /** Que la Sombra vuelva a decirle a ese jugador sus frases de la aguja (25 y el discurso 26). */
    public static void olvidar(ServerPlayer p) {
        int ya = Ficha.dialogo(p, MARCA);
        Ficha.dialogo(p, MARCA, ya & ~BIT_PUERTA & ~Sombra.BIT_DISCURSO);
        Ficha.dialogo(p, FICHA_FINAL, 0);
    }

    /** Como estan las cosas de la aguja (para la prueba y el comando). */
    public static String resumen(ServerLevel m) {
        int barreras = 0, sello = 0;
        for (BlockPos q : Homenaje.puertaAguja()) {
            if (!m.isLoaded(q)) continue;
            BlockState s = m.getBlockState(q);
            if (s.is(Blocks.BARRIER)) barreras++;
            else if (!s.isAir()) sello++;
        }
        Sombra s = sombra();
        String f = final_(m.getServer());
        return "puerta " + (abierta(m.getServer()) ? "abierta" : "cerrada") + " (" + sello + "/" + Homenaje.puertaAguja().size() + " bloques del sello, "
                + barreras + " barreras), " + (sellado ? "sellada" : "sin sellar") + ", la Sombra "
                + (caida(m) ? "caida" : s == null ? "esperando (no ha salido)" : s.rendida() ? "rendida" : s.enPelea() ? "peleando" : "fuera")
                + (rendida(m.getServer()) ? " (apuntada rendida)" : "") + ", final " + (f == null ? "ninguno" : f)
                + ", cielo rojo " + (CieloRojo.activo(m.getServer()) ? "SI" : "NO") + "; abiertas " + ABIERTAS + ", sellos " + SELLOS
                + ", corazones " + CORAZONES + ", elecciones " + ELECCIONES + ", frases " + FRASES;
    }

    /** Al parar el servidor. */
    public static void parar() {
        abriendo = -1;
        sellado = false;
        mantenida = false;
        OYERON.clear();
    }
}
