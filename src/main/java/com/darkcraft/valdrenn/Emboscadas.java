package com.darkcraft.valdrenn;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Las emboscadas de Valdrenn (parte 4c; respuesta 14 del plan: "unas pocas, en 4-5 sitios clave: el puente, la plaza, la
 * escalera del castillo..."). Cada una es un grupo de puestos ({@code emb_<nombre>}) que <b>no estan</b> hasta que
 * alguien entra en su zona: entonces suena un cuerno, salen de la tierra entre humo negro (tumbados: se alzan con su
 * animacion) y van a por el. Si se les vence, no vuelven hasta descansar o morir (como todos); al descansar o morir la
 * emboscada se vuelve a armar y los suyos que quedaran en pie sin pelea vuelven a esconderse.
 * <ul>
 * <li><b>el puente</b>: a medio puente de la Torre de Ascenso, dos perros por detras (desde la torre) y un alabardero
 * delante;</li>
 * <li><b>la plaza</b>: junto a la Fuente Mayor, cuatro alrededor;</li>
 * <li><b>la escalera del torreon</b>: subiendo de la segunda planta a la tercera, dos arriba y un perro abajo;</li>
 * <li><b>el cementerio</b>: en medio de las tumbas, los muertos se levantan;</li>
 * <li><b>la biblioteca</b>: en la sala grande, tres espectros entre las estanterias.</li>
 * </ul>
 * Lo disparado va en memoria (si se cierra el servidor, se vuelve a armar: los de un puesto no se guardan con el mundo).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Emboscadas {
    private Emboscadas() {}

    /** Los grupos de puestos de las emboscadas empiezan asi. */
    public static final String GRUPO = "emb_";

    /** Una emboscada: su nombre (sus puestos son los del grupo {@code emb_<nombre>}) y la zona que la dispara, en el mundo. */
    public record Emboscada(String nombre, AABB zona) {}

    private static final Map<String, List<Emboscada>> POR_SITIO = new LinkedHashMap<>();
    private static final Map<String, Set<String>> DISPARADAS = new LinkedHashMap<>();

    /** Lo apunta {@link ValdrennEnemigos#apuntar}: las emboscadas de ese reino. */
    public static void apuntar(String sitio, List<Emboscada> l) {
        POR_SITIO.put(sitio, List.copyOf(l));
    }

    public static List<Emboscada> de(String sitio) {
        return POR_SITIO.getOrDefault(sitio, List.of());
    }

    /** El nombre de la emboscada de un grupo de puestos ("emb_puente~rey" es "puente"), o null si no es de una emboscada. */
    public static String nombre(String grupo) {
        if (grupo == null || !grupo.startsWith(GRUPO)) return null;
        int t = grupo.indexOf('~');
        return grupo.substring(GRUPO.length(), t < 0 ? grupo.length() : t);
    }

    static boolean esDe(String grupo, String nombre) {
        return nombre.equals(nombre(grupo));
    }

    /** Si un puesto de ese grupo esta en uso: los de una emboscada, solo una vez disparada (hasta descansar o morir). */
    public static boolean disparada(String sitio, String grupo) {
        String n = nombre(grupo);
        return n == null || DISPARADAS.getOrDefault(sitio, Set.of()).contains(n);
    }

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ev.getServer().getTickCount() % 5 != 2 || POR_SITIO.isEmpty()) return;
        ServerLevel lvl = ev.getServer().overworld();
        if (lvl.getDifficulty() == Difficulty.PEACEFUL) return;
        for (ServerPlayer p : lvl.players()) {
            if (p.isSpectator() || p.isCreative() || !p.isAlive()) continue;
            for (var en : POR_SITIO.entrySet()) {
                if (!Puestos.apuntado(en.getKey())) continue;
                Set<String> ya = DISPARADAS.getOrDefault(en.getKey(), Set.of());
                for (Emboscada e : en.getValue())
                    if (!ya.contains(e.nombre()) && e.zona().contains(p.position())) disparar(lvl, en.getKey(), e, p);
            }
        }
    }

    /** Salta la emboscada: salen los suyos (los que no esten ya caidos) alrededor de {@code p}. Devuelve cuantos. */
    public static int disparar(ServerLevel lvl, String sitio, Emboscada e, ServerPlayer p) {
        DISPARADAS.computeIfAbsent(sitio, k -> new HashSet<>()).add(e.nombre());
        List<Enemigo> salen = Puestos.sacarGrupo(lvl, sitio, g -> esDe(g, e.nombre()));
        if (salen.isEmpty()) return 0;
        lvl.playSound(null, p.getX(), p.getY(), p.getZ(), DarkCraftMod.SFX_EMBOSCADA.get(), SoundSource.HOSTILE, 2.2f, 1.0f);
        for (Enemigo en : salen) {
            // salen de la tierra, entre humo negro
            BlockPos pie = en.blockPosition().below();
            BlockState suelo = lvl.getBlockState(pie);
            if (!suelo.isAir()) lvl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), en.getX(), en.getY() + 0.1, en.getZ(), 30, 0.5, 0.05, 0.5, 0.15);
            lvl.sendParticles(ParticleTypes.LARGE_SMOKE, en.getX(), en.getY() + 0.4, en.getZ(), 28, 0.45, 0.55, 0.45, 0.015);
            lvl.sendParticles(ParticleTypes.SOUL, en.getX(), en.getY() + 0.6, en.getZ(), 8, 0.4, 0.5, 0.4, 0.02);
            lvl.playSound(null, en.getX(), en.getY(), en.getZ(), DarkCraftMod.SFX_EMBOSCADA_ALZARSE.get(), SoundSource.HOSTILE, 1.3f,
                    0.88f + lvl.random.nextFloat() * 0.24f);
            en.acudir(p);
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] Emboscada '{}' en {}: salen {}", e.nombre(), sitio, salen.size());
        return salen.size();
    }

    /** Alguien ha descansado o ha muerto: se vuelven a armar; los suyos que quedan en pie y sin pelea, a esconderse. */
    static void rearmar(ServerPlayer quien) {
        for (var en : DISPARADAS.entrySet()) {
            if (en.getValue().isEmpty()) continue;
            Puestos.recogerGrupo(en.getKey(), g -> nombre(g) != null,
                    e -> e.getTarget() == null || e.getTarget() == quien || !e.getTarget().isAlive());
            en.getValue().clear();
        }
    }

    /** Cuando cae uno de una emboscada: si era el ultimo, el logro para los que estaban. */
    static void alCaer(Enemigo e, ServerPlayer quien) {
        if (!(e.level() instanceof ServerLevel lvl) || e.puesto() < 0) return;
        Puestos.Sitio s = Puestos.sitio(e.sitio());
        if (s == null || e.puesto() >= s.puestos.size()) return;
        String n = nombre(s.puestos.get(e.puesto()).grupo());
        if (n == null) return;
        for (int i = 0; i < s.puestos.size(); i++)
            if (esDe(s.puestos.get(i).grupo(), n) && !Puestos.muerto(lvl.getServer(), e.sitio(), i)) return;
        for (ServerPlayer p : lvl.players())
            if (!p.isSpectator() && p.distanceToSqr(e) < 40 * 40) Logros.dar(p, "emboscada");
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent ev) {
        POR_SITIO.clear();
        DISPARADAS.clear();
    }

    /** Para los comandos y las pruebas: dispara a mano la de ese nombre junto a {@code p}. */
    public static int disparar(ServerLevel lvl, String sitio, String nombre, ServerPlayer p) {
        for (Emboscada e : de(sitio)) if (e.nombre().equals(nombre)) return disparar(lvl, sitio, e, p);
        return -1;
    }

    /** Para los comandos y las pruebas: las vuelve a armar todas (sin recoger a nadie). */
    public static void armar(String sitio) {
        Set<String> d = DISPARADAS.get(sitio);
        if (d != null) d.clear();
    }

    /** Para las pruebas: las da todas por disparadas sin sacar a nadie (asi salen con los demas, con {@code sacarTodos}). */
    public static void marcarTodas(String sitio) {
        Set<String> d = DISPARADAS.computeIfAbsent(sitio, k -> new HashSet<>());
        for (Emboscada e : de(sitio)) d.add(e.nombre());
    }

    /** Las disparadas de ese reino (pruebas). */
    public static Set<String> disparadas(String sitio) {
        return Set.copyOf(DISPARADAS.getOrDefault(sitio, Set.of()));
    }
}
