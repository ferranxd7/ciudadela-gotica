package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Los enemigos del mod estan <b>en sus puestos</b> (lo pidio el usuario: como en Dark Souls, nada de salir al azar): cada
 * sitio (Brumavela; luego Valdrenn y lo demas) tiene su lista de puestos —que enemigo, donde, hacia donde mira y como
 * espera— y aqui se llevan:
 * <ul>
 * <li>salen cuando alguien se acerca a menos de {@value #APARECE} bloques y se recogen (sin morir) cuando no queda nadie
 * a menos de {@value #SE_VA}; no se guardan con el mundo (asi no se duplican ni se pierden);</li>
 * <li>el que muere <b>no vuelve</b> hasta que alguien <b>descansa en un santuario o muere</b>
 * ({@link #reaparecer}): entonces vuelven todos, salvo los que quedarian encima de otro jugador;</li>
 * <li>con amigos, los mismos enemigos con mas vida ({@link Enemigo#paraGrupo}); en pacifico no hay ninguno.</li>
 * </ul>
 * Lo muerto de cada sitio va en el SavedData {@code darkcraft_puestos}.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Puestos {
    private Puestos() {}

    static final int APARECE = 72, SE_VA = 104;

    /** Un puesto: el enemigo (su tipo y su variante), donde, hacia donde mira, como espera, cuanto ronda y su grupo. */
    public record Puesto(String tipo, int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {
        public BlockPos pos() {
            return new BlockPos(x, y, z);
        }

        /** Los jefes no vuelven al descansar ni al morir: caen una vez. */
        public boolean jefe() {
            return grupo.equals(JEFE);
        }
    }

    /** El grupo de los jefes. */
    public static final String JEFE = "jefe";

    public static final class Sitio {
        public final String clave;
        public final List<Puesto> puestos;
        final Predicate<ServerLevel> aqui;
        final Enemigo[] fuera;

        Sitio(String clave, List<Puesto> puestos, Predicate<ServerLevel> aqui) {
            this.clave = clave;
            this.puestos = List.copyOf(puestos);
            this.aqui = aqui;
            this.fuera = new Enemigo[puestos.size()];
        }
    }

    private static final Map<String, Sitio> SITIOS = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends EntityType<? extends Enemigo>>> TIPOS = new LinkedHashMap<>();
    /** Quien quiere enterarse de que ha caido un enemigo de un puesto: (el sitio y el puesto, quien lo mato o null). */
    private static final List<BiConsumer<Enemigo, ServerPlayer>> AL_CAER = new ArrayList<>();

    /** Apunta un tipo de enemigo por su nombre (el de los puestos). */
    public static void tipo(String nombre, Supplier<? extends EntityType<? extends Enemigo>> tipo) {
        TIPOS.put(nombre, tipo);
    }

    /** Apunta un sitio con sus puestos. {@code aqui}: si ese mundo es el suyo (y el sitio esta ahi). */
    public static void registrar(String clave, List<Puesto> puestos, Predicate<ServerLevel> aqui) {
        SITIOS.put(clave, new Sitio(clave, puestos, aqui));
    }

    public static void alCaer(BiConsumer<Enemigo, ServerPlayer> f) {
        AL_CAER.add(f);
    }

    public static Sitio sitio(String clave) {
        return SITIOS.get(clave);
    }

    // ------------------------------------------------------------------ lo que se guarda: los muertos de cada sitio

    public static final class Datos extends SavedData {
        final Map<String, BitSet> muertos = new LinkedHashMap<>();

        static Datos load(CompoundTag t) {
            Datos d = new Datos();
            for (String k : t.getAllKeys()) d.muertos.put(k, BitSet.valueOf(t.getLongArray(k)));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            muertos.forEach((k, v) -> t.putLongArray(k, v.toLongArray()));
            return t;
        }

        BitSet de(String sitio) {
            return muertos.computeIfAbsent(sitio, k -> new BitSet());
        }

        public static Datos get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Datos::load, Datos::new, "darkcraft_puestos");
        }
    }

    /** Si el enemigo de ese puesto esta muerto (hasta que alguien descanse o muera). */
    public static boolean muerto(MinecraftServer srv, String sitio, int puesto) {
        return Datos.get(srv).de(sitio).get(puesto);
    }

    /** Cuantos quedan por caer en ese grupo de puestos del sitio (esten a la vista o no). */
    public static int quedan(MinecraftServer srv, String sitio, String grupo) {
        Sitio s = SITIOS.get(sitio);
        if (s == null) return 0;
        BitSet m = Datos.get(srv).de(sitio);
        int n = 0;
        for (int i = 0; i < s.puestos.size(); i++) if (!m.get(i) && s.puestos.get(i).grupo().equals(grupo)) n++;
        return n;
    }

    public static int quedan(MinecraftServer srv, String sitio) {
        Sitio s = SITIOS.get(sitio);
        return s == null ? 0 : s.puestos.size() - Datos.get(srv).de(sitio).cardinality();
    }

    /** Los que hay ahora mismo fuera (vivos y en el mundo) en ese sitio. */
    public static int fuera(String sitio) {
        Sitio s = SITIOS.get(sitio);
        int n = 0;
        if (s != null) for (Enemigo e : s.fuera) if (e != null && e.isAlive() && !e.isRemoved()) n++;
        return n;
    }

    public static Enemigo enemigo(String sitio, int puesto) {
        Sitio s = SITIOS.get(sitio);
        Enemigo e = s == null || puesto < 0 || puesto >= s.fuera.length ? null : s.fuera[puesto];
        return e != null && e.isAlive() && !e.isRemoved() ? e : null;
    }

    // ------------------------------------------------------------------ salir y recogerse

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ev.getServer().getTickCount() % 10 != 7 || SITIOS.isEmpty()) return;
        MinecraftServer srv = ev.getServer();
        ServerLevel lvl = srv.overworld();
        boolean nadie = lvl.players().isEmpty();
        Datos datos = null;
        for (Sitio s : SITIOS.values()) {
            if (!s.aqui.test(lvl)) continue;
            if (nadie || lvl.getDifficulty() == Difficulty.PEACEFUL) {
                recoger(s);
                continue;
            }
            if (datos == null) datos = Datos.get(srv);
            BitSet muertos = datos.de(s.clave);
            for (int i = 0; i < s.fuera.length; i++) {
                Enemigo e = s.fuera[i];
                if (e != null && (e.isRemoved() || !e.isAlive())) e = s.fuera[i] = null;
                if (muertos.get(i)) continue;
                Puesto p = s.puestos.get(i);
                double d2 = masCerca(lvl, e != null ? e.getX() : p.x() + 0.5, e != null ? e.getY() : p.y(), e != null ? e.getZ() : p.z() + 0.5);
                if (e == null) {
                    if (d2 < APARECE * APARECE && lvl.isPositionEntityTicking(p.pos())) s.fuera[i] = sacar(lvl, s, i);
                } else if (d2 > SE_VA * SE_VA) {
                    e.discard();
                    s.fuera[i] = null;
                }
            }
        }
    }

    private static double masCerca(ServerLevel lvl, double x, double y, double z) {
        double mejor = Double.MAX_VALUE;
        for (ServerPlayer p : lvl.players()) {
            if (p.isSpectator()) continue;
            mejor = Math.min(mejor, p.distanceToSqr(x, y, z));
        }
        return mejor;
    }

    private static Enemigo sacar(ServerLevel lvl, Sitio s, int i) {
        Puesto p = s.puestos.get(i);
        Supplier<? extends EntityType<? extends Enemigo>> t = TIPOS.get(p.tipo());
        Enemigo e = t == null ? null : t.get().create(lvl);
        if (e == null) return null;
        e.moveTo(p.x() + 0.5, suelo(lvl, p.x(), p.y(), p.z()), p.z() + 0.5, p.giro(), 0f);
        e.variante(p.variante());
        e.asignar(s.clave, i, p.pos(), p.giro(), p.postura(), p.ronda());
        int cerca = 0;
        for (ServerPlayer q : lvl.players()) if (!q.isSpectator() && q.distanceToSqr(e) < 96 * 96) cerca++;
        e.paraGrupo(cerca);
        return lvl.addFreshEntity(e) ? e : null;
    }

    /** Lo alto del suelo de esa columna cerca de esa altura (una losa, una alfombra...): donde quedan los pies. */
    public static double suelo(ServerLevel lvl, int x, int y, int z) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int by = y + 1; by >= y - 3; by--) {
            m.set(x, by, z);
            net.minecraft.world.phys.shapes.VoxelShape f = lvl.getBlockState(m).getCollisionShape(lvl, m);
            if (f.isEmpty()) continue;
            double arriba = by + f.max(net.minecraft.core.Direction.Axis.Y);
            if (arriba > y + 1.01) continue;
            return arriba;
        }
        return y;
    }

    private static void recoger(Sitio s) {
        for (int i = 0; i < s.fuera.length; i++) {
            if (s.fuera[i] != null && !s.fuera[i].isRemoved()) s.fuera[i].discard();
            s.fuera[i] = null;
        }
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        for (Sitio s : SITIOS.values()) java.util.Arrays.fill(s.fuera, null);
    }

    // ------------------------------------------------------------------ caer y volver

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMuerte(LivingDeathEvent ev) {
        if (ev.isCanceled() || !(ev.getEntity() instanceof Enemigo e) || !(e.level() instanceof ServerLevel lvl)) return;
        ServerPlayer quien = ev.getSource().getEntity() instanceof ServerPlayer p ? p : e.getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (e.puesto() >= 0) {
            Sitio s = SITIOS.get(e.sitio());
            if (s != null && e.puesto() < s.fuera.length) {
                Datos d = Datos.get(lvl.getServer());
                d.de(s.clave).set(e.puesto());
                d.setDirty();
                if (s.fuera[e.puesto()] == e) s.fuera[e.puesto()] = null;
            }
        }
        for (BiConsumer<Enemigo, ServerPlayer> f : AL_CAER) f.accept(e, quien);
    }

    /**
     * Alguien ha descansado en un santuario o ha muerto: vuelven los enemigos caidos (los que quedarian a menos de 28
     * bloques de OTRO jugador vivo, no: se quedan muertos hasta la proxima) y los que siguen en pie vuelven a su puesto
     * con la vida entera si no estan peleando con otro.
     */
    public static int reaparecer(ServerPlayer quien) {
        ServerLevel lvl = quien.server.overworld();
        Datos d = Datos.get(quien.server);
        int vuelven = 0;
        for (Sitio s : SITIOS.values()) {
            if (!s.aqui.test(lvl)) continue;
            BitSet m = d.de(s.clave);
            for (int i = m.nextSetBit(0); i >= 0; i = m.nextSetBit(i + 1)) {
                if (i >= s.puestos.size()) continue;
                Puesto p = s.puestos.get(i);
                if (p.jefe() || otroCerca(lvl, quien, p.x() + 0.5, p.y(), p.z() + 0.5, 28)) continue;
                m.clear(i);
                vuelven++;
            }
            for (Enemigo e : s.fuera) {
                if (e == null || e.isRemoved() || !e.isAlive()) continue;
                if (e.getTarget() != null && e.getTarget() != quien) continue;
                if (otroCerca(lvl, quien, e.getX(), e.getY(), e.getZ(), 20)) continue;
                e.setTarget(null);
                e.setHealth(e.getMaxHealth());
            }
        }
        if (vuelven > 0) d.setDirty();
        return vuelven;
    }

    private static boolean otroCerca(ServerLevel lvl, ServerPlayer menos, double x, double y, double z, double r) {
        for (ServerPlayer p : lvl.players())
            if (p != menos && p.isAlive() && !p.isSpectator() && !p.isCreative() && p.distanceToSqr(x, y, z) < r * r) return true;
        return false;
    }

    /** Para las pruebas: saca ya a todos los que falten de ese sitio, haya o no jugadores cerca. Devuelve los que hay fuera. */
    public static List<Enemigo> sacarTodos(ServerLevel lvl, String sitio) {
        List<Enemigo> r = new ArrayList<>();
        Sitio s = SITIOS.get(sitio);
        if (s == null) return r;
        BitSet muertos = Datos.get(lvl.getServer()).de(sitio);
        for (int i = 0; i < s.fuera.length; i++) {
            if (s.fuera[i] != null && (s.fuera[i].isRemoved() || !s.fuera[i].isAlive())) s.fuera[i] = null;
            if (s.fuera[i] == null && !muertos.get(i)) s.fuera[i] = sacar(lvl, s, i);
            if (s.fuera[i] != null) r.add(s.fuera[i]);
        }
        return r;
    }

    /** Da por caido (o por vivo) el enemigo de un puesto sin que nadie lo mate (comandos, pruebas, lo que ya estaba hecho). */
    public static void marcar(MinecraftServer srv, String sitio, int puesto, boolean muerto) {
        Sitio s = SITIOS.get(sitio);
        if (s == null || puesto < 0 || puesto >= s.puestos.size()) return;
        Datos d = Datos.get(srv);
        if (d.de(sitio).get(puesto) == muerto) return;
        d.de(sitio).set(puesto, muerto);
        d.setDirty();
        if (muerto && s.fuera[puesto] != null) {
            if (!s.fuera[puesto].isRemoved()) s.fuera[puesto].discard();
            s.fuera[puesto] = null;
        }
    }

    /** Para los comandos y las pruebas: todos muertos, todos vivos, o fuera los que hay (vuelven a salir solos). */
    public static void todos(MinecraftServer srv, String sitio, boolean muertos) {
        Sitio s = SITIOS.get(sitio);
        if (s == null) return;
        Datos d = Datos.get(srv);
        BitSet m = d.de(sitio);
        m.clear();
        if (muertos) m.set(0, s.puestos.size());
        d.setDirty();
        recoger(s);
    }
}
