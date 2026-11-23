package com.darkcraft.gavran;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Las raices de Gavran: brotan del suelo como estacas de madera (entidades que muestran un bloque, estiradas y
 * torcidas, que crecen y se recogen solas) y hacen dano a quien pillan. Tambien la onda que corre por el suelo
 * cuando clava el mandoble. Todo se borra al acabar (no toca los bloques de la sala).
 */
public final class Raices {
    public static final String TAG = "darkcraft_raiz";
    public static final DustParticleOptions VERDE = new DustParticleOptions(new Vector3f(0.35f, 1f, 0.45f), 1.3f);

    /** Una erupcion pendiente: donde, cuando (tick de la pelea), radio y dano. */
    private record Erupcion(double x, double y, double z, int t, double r, float bruto, double alto) {}

    /** Una estaca viva: cuando se recoge y cuando se quita. */
    private static final class Estaca {
        final Display.BlockDisplay e;
        final Matrix4f lleno;
        int crece, recoge, quita;

        Estaca(Display.BlockDisplay e, Matrix4f lleno, int crece, int recoge, int quita) {
            this.e = e;
            this.lleno = lleno;
            this.crece = crece;
            this.recoge = recoge;
            this.quita = quita;
        }
    }

    /** Onda que se abre en el suelo: centro, radio, velocidad, radio maximo, a quien ya ha dado. */
    private static final class Onda {
        final Vec3 c;
        double r;
        final double v, max;
        final float bruto;
        final Set<UUID> dados = new HashSet<>();

        Onda(Vec3 c, double r0, double v, double max, float bruto) {
            this.c = c;
            this.r = r0;
            this.v = v;
            this.max = max;
            this.bruto = bruto;
        }
    }

    private final Gavran g;
    private final List<Erupcion> pendientes = new ArrayList<>();
    private final List<Estaca> estacas = new ArrayList<>();
    private final List<Onda> ondas = new ArrayList<>();
    private final Set<UUID> dadosLinea = new HashSet<>();
    private int t;

    Raices(Gavran g) {
        this.g = g;
    }

    // ------------------------------------------------------------------ lanzar

    /** Una linea de raices que corre por el suelo desde 'desde' en la direccion dir (cada 1,4 bloques, 2 ticks). */
    void linea(ServerLevel sl, Vec3 desde, Vec3 dir, double largo, int retraso, float bruto) {
        SalaArbol s = g.sala();
        dadosLinea.clear();
        Vec3 d = new Vec3(dir.x, 0, dir.z).normalize();
        int k = 0;
        for (double a = 2.5; a <= largo; a += 1.4, k++) {
            Vec3 p = desde.add(d.scale(a));
            if (s != null) {
                double r = s.dist(p.x, p.z);
                if (r < SalaArbol.R_TRONCO - 1.0 || r > SalaArbol.R_MURO + 1.2) break;
            }
            double y = s != null ? s.suelo(sl, p.x, p.z) : Math.floor(desde.y);
            pendientes.add(new Erupcion(p.x, y, p.z, t + retraso + 2 * k, 1.45, bruto, 2.6));
        }
    }

    /** Raices que brotan justo donde esta alguien (con aviso antes): para quien se sube al balcon. */
    void bajo(ServerLevel sl, Vec3 p, int retraso, float bruto) {
        dadosLinea.clear();
        pendientes.add(new Erupcion(p.x, Math.floor(p.y + 0.01), p.z, t + retraso, 1.9, bruto, 3.0));
    }

    /** La onda del mandoble clavado: un anillo que se abre por el suelo (hay que saltarlo). */
    void onda(Vec3 c, double v, double max, float bruto) {
        ondas.add(new Onda(c, 1.5, v, max, bruto));
    }

    /** Raices que salen alrededor de los escuderos cuando aparecen (sin dano). */
    void adorno(ServerLevel sl, Vec3 p) {
        for (int i = 0; i < 4; i++) estaca(sl, p.x, p.y, p.z, sl.random, 1.1, 1.6, 4, 26);
    }

    // ------------------------------------------------------------------ cada tick

    void tick(ServerLevel sl) {
        t++;
        // erupciones que tocan ahora
        if (!pendientes.isEmpty()) {
            Iterator<Erupcion> it = pendientes.iterator();
            boolean sono = false;
            while (it.hasNext()) {
                Erupcion e = it.next();
                if (e.t() > t) continue;
                it.remove();
                brotar(sl, e);
                if (!sono) {
                    sl.playSound(null, e.x(), e.y(), e.z(), DarkCraftMod.SFX_GAVRAN_RAICES.get(), SoundSource.HOSTILE, 0.9f,
                            0.85f + sl.random.nextFloat() * 0.3f);
                    sono = true;
                }
            }
        }
        // estacas: crecer, recogerse y quitarse
        Iterator<Estaca> it = estacas.iterator();
        while (it.hasNext()) {
            Estaca e = it.next();
            if (e.e.isRemoved()) {
                it.remove();
                continue;
            }
            if (t == e.crece) poner(e.e, e.lleno, 3);
            if (t == e.recoge) poner(e.e, encoger(e.lleno), 6);
            if (t >= e.quita) {
                e.e.discard();
                it.remove();
            }
        }
        // ondas
        Iterator<Onda> io = ondas.iterator();
        while (io.hasNext()) {
            Onda o = io.next();
            o.r += o.v;
            if (o.r > o.max) {
                io.remove();
                continue;
            }
            SalaArbol s = g.sala();
            int n = (int) Math.min(90, o.r * 2.2);
            BlockState suelo = Blocks.ROOTED_DIRT.defaultBlockState();
            for (int i = 0; i < n; i++) {
                double a = (i + sl.random.nextDouble()) * Math.PI * 2 / n;
                double x = o.c.x + Math.cos(a) * o.r, z = o.c.z + Math.sin(a) * o.r;
                if (s != null) {
                    double d = s.dist(x, z);
                    if (d < SalaArbol.R_TRONCO - 1.5 || d > SalaArbol.R_MURO + 1.5) continue;
                }
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, o.c.y + 0.15, z, 2, 0.15, 0.05, 0.15, 0.12);
                if (i % 3 == 0) sl.sendParticles(VERDE, x, o.c.y + 0.4, z, 1, 0.1, 0.2, 0.1, 0);
            }
            for (ServerPlayer p : g.vivos(sl)) {
                if (o.dados.contains(p.getUUID())) continue;
                double d = Math.hypot(p.getX() - o.c.x, p.getZ() - o.c.z);
                // salta por encima si no esta pisando el suelo (o ya esta muy arriba)
                boolean enSuelo = p.onGround() && p.getY() < o.c.y + 0.9;
                if (Math.abs(d - o.r) < 1.05 && enSuelo) {
                    o.dados.add(p.getUUID());
                    g.golpear(sl, p, o.bruto, 1.0, 0.55, true);
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_LOG.defaultBlockState()),
                            p.getX(), p.getY() + 0.3, p.getZ(), 20, 0.4, 0.3, 0.4, 0.2);
                }
            }
        }
    }

    private void brotar(ServerLevel sl, Erupcion e) {
        RandomSource r = sl.random;
        int n = e.r() > 1.6 ? 5 : 3;
        for (int i = 0; i < n; i++) {
            double a = r.nextDouble() * Math.PI * 2, d = i == 0 ? 0 : 0.35 + r.nextDouble() * (e.r() - 0.6);
            estaca(sl, e.x() + Math.cos(a) * d, e.y(), e.z() + Math.sin(a) * d, r, i == 0 ? e.alto() : e.alto() * 0.6, i == 0 ? 0.46 : 0.32,
                    i == 0 ? 15 : 12, i == 0 ? 22 : 18);
        }
        BlockState suelo = Blocks.ROOTED_DIRT.defaultBlockState();
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), e.x(), e.y() + 0.2, e.z(), 18, 0.5, 0.1, 0.5, 0.15);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_LOG.defaultBlockState()), e.x(), e.y() + 1.0, e.z(), 10, 0.3, 0.6, 0.3, 0.1);
        sl.sendParticles(VERDE, e.x(), e.y() + 0.8, e.z(), 6, 0.4, 0.6, 0.4, 0);
        for (ServerPlayer p : g.vivos(sl)) {
            if (dadosLinea.contains(p.getUUID())) continue;
            if (Math.hypot(p.getX() - e.x(), p.getZ() - e.z()) < e.r() + p.getBbWidth() / 2 && p.getY() > e.y() - 1.5 && p.getY() < e.y() + 2.8) {
                dadosLinea.add(p.getUUID());
                g.golpear(sl, p, e.bruto(), 0.35, 0.62, true);
            }
        }
    }

    /** Una estaca de madera (o de raices) que crece desde (x, y, z). */
    private void estaca(ServerLevel sl, double x, double y, double z, RandomSource r, double alto, double grueso, int dura, int fin) {
        Display.BlockDisplay d = EntityType.BLOCK_DISPLAY.create(sl);
        if (d == null) return;
        // corteza casi negra con venas de savia verde (darkcraft:raiz_gavran); alguna, maraña de raices
        BlockState st = r.nextInt(5) == 0 ? Blocks.MANGROVE_ROOTS.defaultBlockState() : DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        float yaw = r.nextFloat() * Mth.TWO_PI, tilt = (0.12f + r.nextFloat() * 0.38f);
        float h = (float) (alto * (0.8 + r.nextDouble() * 0.4)), w = (float) grueso;
        Matrix4f lleno = new Matrix4f().rotateY(yaw).rotateX(tilt).scale(w, h, w).translate(-0.5f, 0f, -0.5f);
        CompoundTag tag = new CompoundTag();
        tag.put("block_state", NbtUtils.writeBlockState(st));
        tag.put("transformation", matriz(encoger(lleno)));
        tag.putInt("interpolation_duration", 0);
        CompoundTag luz = new CompoundTag();
        luz.putInt("sky", 0);
        luz.putInt("block", 9);
        tag.put("brightness", luz);
        tag.putFloat("view_range", 1.5f);
        d.load(tag);
        d.moveTo(x, y, z, 0f, 0f);
        d.addTag(TAG);
        sl.addFreshEntity(d);
        estacas.add(new Estaca(d, lleno, t + 1, t + dura, t + fin));
    }

    private static Matrix4f encoger(Matrix4f m) {
        return new Matrix4f(m).scale(1f, 0.02f, 1f);
    }

    /** Cambia la forma de la estaca con interpolacion (el cliente la hace crecer o encoger poco a poco). */
    private static void poner(Display.BlockDisplay d, Matrix4f m, int ticks) {
        CompoundTag t = d.saveWithoutId(new CompoundTag());
        t.put("transformation", matriz(m));
        t.putInt("interpolation_duration", ticks);
        t.putInt("start_interpolation", 0);
        d.load(t);
    }

    private static ListTag matriz(Matrix4f m) {
        ListTag l = new ListTag();
        for (int i = 0; i < 16; i++) l.add(FloatTag.valueOf(m.getRowColumn(i >> 2, i & 3)));
        return l;
    }

    // ------------------------------------------------------------------ limpiar

    /** Quita todo (al reiniciar, morir o cambiar de fase). */
    void limpiar() {
        for (Estaca e : estacas) e.e.discard();
        estacas.clear();
        pendientes.clear();
        ondas.clear();
    }

    /** Quita estacas que se hubieran quedado (si el mundo se cerro a mitad de un ataque). */
    static void limpiarSueltas(ServerLevel sl, SalaArbol s) {
        for (Display.BlockDisplay d : sl.getEntitiesOfClass(Display.BlockDisplay.class, s.caja(), e -> e.getTags().contains(TAG)))
            d.discard();
    }

    boolean activas() {
        return !pendientes.isEmpty() || !ondas.isEmpty();
    }

    static BlockPos bp(Vec3 v) {
        return BlockPos.containing(v);
    }
}
