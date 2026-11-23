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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
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
        tickFase2(sl);
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
        if (e.bruto() <= 0) return;
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
        // corteza casi negra con venas de savia verde (darkcraft:raiz_gavran); alguna, maraña de raices
        BlockState st = r.nextInt(5) == 0 ? Blocks.MANGROVE_ROOTS.defaultBlockState() : DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        float yaw = r.nextFloat() * Mth.TWO_PI, tilt = (0.12f + r.nextFloat() * 0.38f);
        float h = (float) (alto * (0.8 + r.nextDouble() * 0.4));
        estaca(sl, st, x, y, z, yaw, tilt, h, (float) grueso, 1, dura, fin);
    }

    /**
     * Estaca que crece en el tick t+crece desde (x, y, z), inclinada 'tilt' radianes hacia (sin yaw, cos yaw), se
     * recoge en t+dura y se quita en t+fin. Devuelve la estaca (o null).
     */
    private Estaca estaca(ServerLevel sl, BlockState st, double x, double y, double z, float yaw, float tilt, float h, float w,
                          int crece, int dura, int fin) {
        Matrix4f lleno = new Matrix4f().rotateY(yaw).rotateX(tilt).scale(w, h, w).translate(-0.5f, 0f, -0.5f);
        Display.BlockDisplay d = display(sl, st, x, y, z, encoger(lleno), 9);
        if (d == null) return null;
        Estaca e = new Estaca(d, lleno, t + crece, t + dura, t + fin);
        estacas.add(e);
        return e;
    }

    /** Una entidad que muestra un bloque, con su forma (matriz), luz propia y que se borra al reiniciar (etiqueta). */
    private Display.BlockDisplay display(ServerLevel sl, BlockState st, double x, double y, double z, Matrix4f m, int luz) {
        Display.BlockDisplay d = EntityType.BLOCK_DISPLAY.create(sl);
        if (d == null) return null;
        CompoundTag tag = new CompoundTag();
        tag.put("block_state", NbtUtils.writeBlockState(st));
        tag.put("transformation", matriz(m));
        tag.putInt("interpolation_duration", 0);
        CompoundTag l = new CompoundTag();
        l.putInt("sky", 0);
        l.putInt("block", luz);
        tag.put("brightness", l);
        tag.putFloat("view_range", 1.5f);
        d.load(tag);
        d.moveTo(x, y, z, 0f, 0f);
        d.addTag(TAG);
        sl.addFreshEntity(d);
        return d;
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
        for (Paso p : pasos) p.d().discard();
        pasos.clear();
        for (Display.BlockDisplay d : grandes) d.discard();
        grandes.clear();
        capullo.clear();
        caidas.clear();
        muros.clear();
        marea = null;
    }

    /** Quita estacas que se hubieran quedado (si el mundo se cerro a mitad de un ataque). */
    static void limpiarSueltas(ServerLevel sl, SalaArbol s) {
        for (Display.BlockDisplay d : sl.getEntitiesOfClass(Display.BlockDisplay.class, s.caja(), e -> e.getTags().contains(TAG)))
            d.discard();
    }

    boolean activas() {
        return !pendientes.isEmpty() || !ondas.isEmpty() || !caidas.isEmpty() || marea != null;
    }

    // ================================================================== fase 2 (parte B2)

    /** Cambio de forma programado de una entidad que se ve: en el tick t pasa a m en dur ticks (o se quita). */
    private record Paso(Display.BlockDisplay d, int t, Matrix4f m, int dur, boolean quitar) {}

    /** Hojas cortantes que caen en un circulo: centro, tick en que llegan al suelo, radio y dano. */
    private record Caida(double x, double y, double z, int t, double r, float bruto) {}

    /** Un bloque de un muro de corteza que crece en el tick t. */
    private record Muro(BlockPos p, int t) {}

    /** Marea de raices por el suelo: crece desde c hasta max (v bloques por tick) y dura hasta fin. */
    private static final class Marea {
        final Vec3 c;
        final double v, max;
        final int fin;
        final float bruto;
        double r = 2;

        Marea(Vec3 c, double v, double max, int fin, float bruto) {
            this.c = c;
            this.v = v;
            this.max = max;
            this.fin = fin;
            this.bruto = bruto;
        }
    }

    private final List<Paso> pasos = new ArrayList<>();
    private final List<Caida> caidas = new ArrayList<>();
    private final List<Muro> muros = new ArrayList<>();
    /** Raices grandes que se quedan en la sala toda la fase 2 (el arbol ha despertado). */
    private final List<Display.BlockDisplay> grandes = new ArrayList<>();
    /** Las raices que le envuelven en el cambio de fase. */
    private final List<Estaca> capullo = new ArrayList<>();
    private Marea marea;

    private void tickFase2(ServerLevel sl) {
        if (!pasos.isEmpty()) {
            Iterator<Paso> it = pasos.iterator();
            while (it.hasNext()) {
                Paso p = it.next();
                if (p.t() > t) continue;
                it.remove();
                if (p.d().isRemoved()) continue;
                if (p.quitar()) p.d().discard();
                else poner(p.d(), p.m(), p.dur());
            }
        }
        if (!caidas.isEmpty()) {
            Iterator<Caida> it = caidas.iterator();
            boolean sono = false;
            while (it.hasNext()) {
                Caida c = it.next();
                if (c.t() - 8 == t) soltarHojas(sl, c);
                if (c.t() > t) continue;
                it.remove();
                if (!sono) {
                    sl.playSound(null, c.x(), c.y(), c.z(), DarkCraftMod.SFX_HOJAS_CORTANTES.get(), SoundSource.HOSTILE, 1.2f,
                            0.9f + sl.random.nextFloat() * 0.2f);
                    sono = true;
                }
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AZALEA_LEAVES.defaultBlockState()),
                        c.x(), c.y() + 0.4, c.z(), 30, c.r() * 0.45, 0.2, c.r() * 0.45, 0.15);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x(), c.y() + 0.6, c.z(), 3, c.r() * 0.4, 0.1, c.r() * 0.4, 0);
                for (ServerPlayer p : g.vivos(sl))
                    if (Math.hypot(p.getX() - c.x(), p.getZ() - c.z()) < c.r() + p.getBbWidth() / 2 && p.getY() > c.y() - 1.0
                            && p.getY() < c.y() + 3.5) g.golpear(sl, p, c.bruto(), 0.3, 0.12, true);
            }
        }
        if (!muros.isEmpty()) {
            Iterator<Muro> it = muros.iterator();
            BlockState raiz = DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
            while (it.hasNext()) {
                Muro m = it.next();
                if (m.t() > t) continue;
                it.remove();
                BlockState old = sl.getBlockState(m.p());
                // nunca encima de nadie (se quedaria encerrado dentro del muro)
                if (!sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new net.minecraft.world.phys.AABB(m.p())).isEmpty())
                    continue;
                if (old.isAir() || old.canBeReplaced() || old.is(Blocks.LIGHT)) {
                    sl.setBlock(m.p(), raiz, 3);
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()),
                            m.p().getX() + 0.5, m.p().getY() + 0.2, m.p().getZ() + 0.5, 4, 0.3, 0.1, 0.3, 0.1);
                }
            }
        }
        if (marea != null) tickMarea(sl);
    }

    // ------------------------------------------------------------------ raices que atrapan

    /** Jaula de raices que se cierra sobre c (se queda 'dura' ticks y se recoge). */
    void jaula(ServerLevel sl, Vec3 c, int dura) {
        RandomSource r = sl.random;
        BlockState st = DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        int n = 7;
        for (int i = 0; i < n; i++) {
            double a = i * Mth.TWO_PI / n + r.nextDouble() * 0.3;
            double x = c.x + Math.cos(a) * 1.35, z = c.z + Math.sin(a) * 1.35;
            // inclinada hacia el centro (por encima de quien esta dentro)
            float yaw = (float) Math.atan2(-Math.cos(a), -Math.sin(a));
            estaca(sl, st, x, c.y, z, yaw, 0.55f + r.nextFloat() * 0.15f, 2.6f + r.nextFloat() * 0.5f, 0.34f, 1 + i % 3, dura, dura + 8);
        }
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), c.x, c.y + 0.2, c.z, 24, 0.8, 0.1, 0.8, 0.15);
        sl.sendParticles(VERDE, c.x, c.y + 1.0, c.z, 14, 0.7, 0.8, 0.7, 0);
        sl.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_RAICES_ATRAPAN.get(), SoundSource.HOSTILE, 1.4f, 0.9f + r.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ lluvia de hojas cortantes

    /** Hojas cortantes que caen en un circulo de radio r alrededor de c dentro de 'retraso' ticks (minimo 9). */
    void hojas(Vec3 c, double r, int retraso, float bruto) {
        caidas.add(new Caida(c.x, c.y, c.z, t + Math.max(9, retraso), r, bruto));
    }

    /** Las hojas empiezan a caer (8 ticks antes de llegar al suelo): laminas verdes que giran mientras bajan. */
    private void soltarHojas(ServerLevel sl, Caida c) {
        RandomSource r = sl.random;
        for (int i = 0; i < 11; i++) {
            double a = r.nextDouble() * Mth.TWO_PI, d = Math.sqrt(r.nextDouble()) * c.r();
            double x = c.x() + Math.cos(a) * d, z = c.z() + Math.sin(a) * d;
            BlockState st = r.nextInt(3) == 0 ? Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState() : Blocks.AZALEA_LEAVES.defaultBlockState();
            float giro = r.nextFloat() * Mth.TWO_PI, vuelta = (r.nextFloat() - 0.5f) * 2.2f;
            float alto = 6.5f + r.nextFloat() * 2.5f;
            Matrix4f arriba = hoja(alto, giro, 0.3f, vuelta * 0.2f);
            Matrix4f abajo = hoja(0.15f, giro + vuelta * 3f, 1.25f, vuelta);
            Display.BlockDisplay dd = display(sl, st, x, c.y(), z, arriba, 13);
            if (dd == null) continue;
            pasos.add(new Paso(dd, t + 1, abajo, 7, false));
            pasos.add(new Paso(dd, t + 14, abajo, 0, true));
        }
    }

    /** Lamina de hoja a 'alto' bloques sobre el suelo, girada. */
    private static Matrix4f hoja(float alto, float giro, float inclina, float vuelta) {
        return new Matrix4f().translate(0, alto, 0).rotateY(giro).rotateX(inclina).rotateZ(vuelta).scale(0.75f, 0.05f, 0.3f)
                .translate(-0.5f, -0.5f, -0.5f);
    }

    // ------------------------------------------------------------------ raices por todo el suelo

    /** La marea de raices: se extiende por el suelo desde c y hace dano a quien la pisa (hay que subirse a algo). */
    void marea(Vec3 c, double v, double max, int dura, float bruto) {
        marea = new Marea(c, v, max, t + dura, bruto);
    }

    boolean hayMarea() {
        return marea != null;
    }

    private void tickMarea(ServerLevel sl) {
        Marea m = marea;
        if (t >= m.fin) {
            marea = null;
            return;
        }
        m.r = Math.min(m.max, m.r + m.v);
        SalaArbol s = g.sala();
        RandomSource r = sl.random;
        // raices que brotan en el frente y en lo ya cubierto (sin dano: el dano va por tiempo)
        if (t % 3 == 0) {
            int n = (int) Mth.clamp(m.r * 0.35, 3, 12);
            for (int i = 0; i < n; i++) {
                double a = r.nextDouble() * Mth.TWO_PI;
                double d = i < n / 2 ? m.r - r.nextDouble() * 1.5 : Math.sqrt(r.nextDouble()) * m.r;
                double x = m.c.x + Math.cos(a) * d, z = m.c.z + Math.sin(a) * d;
                if (s != null) {
                    double rr = s.dist(x, z);
                    if (rr < SalaArbol.R_TRONCO - 0.5 || rr > SalaArbol.R_MURO + 0.8) continue;
                }
                double y = s != null ? s.suelo(sl, x, z) : m.c.y;
                if (s != null && y > s.F() + 0.5) continue;
                estaca(sl, x, y, z, r, 1.0 + r.nextDouble() * 0.9, 0.22, 10 + r.nextInt(8), 18 + r.nextInt(8));
            }
        }
        if (t % 2 == 0)
            for (int i = 0; i < 10; i++) {
                double a = r.nextDouble() * Mth.TWO_PI, d = Math.sqrt(r.nextDouble()) * m.r;
                double x = m.c.x + Math.cos(a) * d, z = m.c.z + Math.sin(a) * d;
                if (s != null && (s.dist(x, z) < SalaArbol.R_TRONCO - 0.5 || s.dist(x, z) > SalaArbol.R_MURO + 0.8)) continue;
                sl.sendParticles(VERDE, x, (s != null ? s.F() : m.c.y) + 0.15, z, 1, 0.2, 0.02, 0.2, 0);
            }
        // dano cada segundo a quien este en el suelo de la sala dentro de la marea (no en la tarima, la escalera ni el balcon)
        if (t % 20 == 0)
            for (ServerPlayer p : g.vivos(sl)) {
                if (Math.hypot(p.getX() - m.c.x, p.getZ() - m.c.z) > m.r) continue;
                double suelo = s != null ? s.F() : m.c.y;
                if (p.getY() > suelo + 1.2) continue;
                if (s != null && s.suelo(sl, p.getX(), p.getZ()) > s.F() + 0.5) continue;
                g.golpear(sl, p, m.bruto, 0, 0.3, true);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, true));
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()),
                        p.getX(), p.getY() + 0.2, p.getZ(), 12, 0.3, 0.1, 0.3, 0.1);
            }
    }

    // ------------------------------------------------------------------ el Juicio de las espinas

    /**
     * Muro de corteza de verdad (bloques que se deshacen solos): centrado en c, de 'ancho' x 'alto', perpendicular a
     * 'normal' (la direccion desde Gavran). Crece por capas desde el tick t+retraso.
     */
    void muro(ServerLevel sl, Vec3 c, Vec3 normal, int ancho, int alto, int retraso) {
        Vec3 n = new Vec3(normal.x, 0, normal.z).normalize();
        Vec3 tg = new Vec3(-n.z, 0, n.x);
        SalaArbol s = g.sala();
        Set<BlockPos> hechos = new HashSet<>();
        for (int i = 0; i < ancho; i++) {
            double k = i - (ancho - 1) / 2.0;
            // un poco curvado hacia Gavran por los lados
            Vec3 p = c.add(tg.scale(k)).subtract(n.scale(k * k * 0.06));
            int y0 = s != null ? (int) Math.floor(s.suelo(sl, p.x, p.z) + 0.01) : (int) Math.floor(c.y);
            for (int h = 0; h < alto - (Math.abs(k) > ancho / 2.0 - 1.2 ? 1 : 0); h++) {
                BlockPos bp = BlockPos.containing(p.x, y0 + h, p.z);
                if (hechos.add(bp)) muros.add(new Muro(bp, t + retraso + h * 2));
            }
        }
        sl.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_CORTEZA_MURO.get(), SoundSource.HOSTILE, 1.6f, 0.9f + sl.random.nextFloat() * 0.2f);
    }

    /**
     * Las espinas salen de Gavran en todas direcciones por el suelo y se clavan en lo primero que encuentran. Devuelve
     * a quien protegio algo (un muro, el tronco...) para el logro.
     */
    List<ServerPlayer> espinas(ServerLevel sl, float bruto) {
        Vec3 o = g.position().add(0, 1.3, 0);
        RandomSource r = sl.random;
        BlockState st = DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        int n = 44;
        for (int i = 0; i < n; i++) {
            double a = i * Mth.TWO_PI / n + r.nextDouble() * 0.05;
            double dx = Math.cos(a), dz = Math.sin(a);
            Vec3 fin = o.add(dx * 46, 0, dz * 46);
            HitResult hit = sl.clip(new ClipContext(o, fin, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, g));
            double L = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().distanceTo(o) : 46;
            float yaw = (float) Math.atan2(dx, dz);
            float alto = (float) (r.nextDouble() * 0.8 - 0.3), largo = 2.4f + r.nextFloat() * 0.8f;
            Matrix4f m0 = espina(yaw, 0.6f, alto, largo * 0.3f);
            Matrix4f m1 = espina(yaw, (float) Math.max(0.6, L - largo * 0.75), alto, largo);
            Display.BlockDisplay d = display(sl, st, g.getX(), g.getY() + 1.3, g.getZ(), m0, 12);
            if (d == null) continue;
            pasos.add(new Paso(d, t + 1, m1, (int) Mth.clamp(L / 6, 2, 7), false));
            pasos.add(new Paso(d, t + 30, espina(yaw, (float) Math.max(0.6, L - largo * 0.75), alto, 0.05f), 6, false));
            pasos.add(new Paso(d, t + 37, m1, 0, true));
            if (hit.getType() == HitResult.Type.BLOCK) {
                Vec3 h = hit.getLocation();
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, sl.getBlockState(((net.minecraft.world.phys.BlockHitResult) hit).getBlockPos())),
                        h.x, h.y, h.z, 6, 0.2, 0.2, 0.2, 0.1);
            }
        }
        sl.playSound(null, o.x, o.y, o.z, DarkCraftMod.SFX_ESPINAS.get(), SoundSource.HOSTILE, 3.0f, 0.95f);
        List<ServerPlayer> salvados = new ArrayList<>();
        for (ServerPlayer p : g.vivos(sl)) {
            boolean tapado = tapado(sl, o, p.position().add(0, 0.9, 0)) && tapado(sl, o, p.getEyePosition());
            if (tapado) salvados.add(p);
            else g.golpear(sl, p, bruto, 1.6, 0.45, true);
        }
        return salvados;
    }

    private boolean tapado(ServerLevel sl, Vec3 de, Vec3 a) {
        return sl.clip(new ClipContext(de, a, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, g)).getType() == HitResult.Type.BLOCK;
    }

    /** Espina tumbada que sale hacia (sin yaw, cos yaw), con la punta a 'hasta' bloques. */
    private static Matrix4f espina(float yaw, float hasta, float alto, float largo) {
        float w = 0.2f;
        return new Matrix4f().rotateY(yaw).translate(-w / 2, alto - w / 2, hasta - largo).scale(w, w, largo);
    }

    // ------------------------------------------------------------------ cambio de fase: le envuelven y el arbol despierta

    /** Raices que suben en espiral alrededor de c durante 'dura' ticks (se quedan hasta estallar). */
    void capullo(ServerLevel sl, Vec3 c, int dura) {
        BlockState st = DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        RandomSource r = sl.random;
        // tres raices que suben enroscadas (desfasadas un tercio de vuelta), en trozos cortos pegados al cuerpo
        int n = 30;
        for (int i = 0; i < n; i++) {
            int hebra = i % 3, k = i / 3;
            double a = k * 0.78 + hebra * Mth.TWO_PI / 3, y = c.y + 0.15 + k * 0.56 + hebra * 0.18;
            double rr = 1.45 + (k > 6 ? -0.25 : 0) + r.nextDouble() * 0.12;
            double x = c.x + Math.cos(a) * rr, z = c.z + Math.sin(a) * rr;
            // tumbada a lo largo de la espiral, subiendo un poco
            float yaw = (float) Math.atan2(-Math.sin(a) - Math.cos(a) * 0.18, Math.cos(a) - Math.sin(a) * 0.18);
            Estaca e = estaca(sl, st, x, y, z, yaw, 1.3f + r.nextFloat() * 0.08f, 1.35f + r.nextFloat() * 0.3f, 0.34f,
                    1 + k * dura / (n / 3) + hebra * 2, 100000, 100000);
            if (e != null) capullo.add(e);
        }
    }

    /** El capullo revienta: las raices se recogen de golpe y salen astillas, hojas y luz verde. */
    void estallar(ServerLevel sl, Vec3 c) {
        for (Estaca e : capullo) {
            e.crece = Math.min(e.crece, t);
            e.recoge = t + 1;
            e.quita = t + 6;
        }
        capullo.clear();
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState()),
                c.x, c.y + 2.6, c.z, 120, 1.2, 1.8, 1.2, 0.35);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AZALEA_LEAVES.defaultBlockState()), c.x, c.y + 3, c.z, 80, 1.6, 2.0, 1.6, 0.3);
        sl.sendParticles(VERDE, c.x, c.y + 3, c.z, 80, 1.6, 2.2, 1.6, 0);
        sl.sendParticles(ParticleTypes.FLASH, c.x, c.y + 3, c.z, 1, 0, 0, 0, 0);
    }

    /**
     * El arbol despierta: una ola de raices rompe el suelo de la sala desde el tronco hacia el muro y crecen raices
     * enormes junto al tronco y al muro que se quedan toda la fase 2.
     */
    void despertar(ServerLevel sl, SalaArbol s) {
        RandomSource r = sl.random;
        for (int k = 0; k < 10; k++) {
            double rr = SalaArbol.R_TRONCO + 0.8 + k * 2.2;
            for (int i = 0; i < 9; i++) {
                double a = r.nextDouble() * Mth.TWO_PI;
                Vec3 p = s.enAnillo(a, rr, s.F());
                if (rr > SalaArbol.R_MURO + 0.5) continue;
                pendientes.add(new Erupcion(p.x, s.suelo(sl, p.x, p.z), p.z, t + k * 5 + r.nextInt(4), 1.2, 0f, 2.4));
            }
        }
        BlockState st = DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState();
        for (int i = 0; i < 14; i++) {
            double a = i * Mth.TWO_PI / 14 + r.nextDouble() * 0.25;
            boolean junto = i % 2 == 0;
            double rr = junto ? SalaArbol.R_TRONCO + 0.3 : SalaArbol.R_MURO - 0.2;
            Vec3 p = s.enAnillo(a, rr, s.F());
            // inclinadas hacia el centro del anillo (las del tronco hacia fuera, las del muro hacia dentro)
            double sx = Math.cos(a) * (junto ? 1 : -1), sz = Math.sin(a) * (junto ? 1 : -1);
            float yaw = (float) Math.atan2(sx, sz);
            float h = 5.5f + r.nextFloat() * 3f, w = 0.9f + r.nextFloat() * 0.5f;
            Matrix4f lleno = new Matrix4f().rotateY(yaw).rotateX(0.55f + r.nextFloat() * 0.35f).scale(w, h, w).translate(-0.5f, 0f, -0.5f);
            Display.BlockDisplay d = display(sl, st, p.x, s.suelo(sl, p.x, p.z) - 0.4, p.z, encoger(lleno), 8);
            if (d == null) continue;
            grandes.add(d);
            pasos.add(new Paso(d, t + 10 + i * 4, lleno, 18, false));
        }
        // ramas que bajan de la cupula y se mecen mientras el arbol despierta (luego se quedan quietas)
        BlockState madera = Blocks.DARK_OAK_WOOD.defaultBlockState();
        for (int i = 0; i < 7; i++) {
            double a = i * Mth.TWO_PI / 7 + r.nextDouble() * 0.4;
            double rr = 21 + r.nextDouble() * 9;
            Vec3 p = s.enAnillo(a, rr, 0);
            double techo = s.F() + 25 + Math.round(14 * Math.sqrt(Math.max(0, 1 - (rr / 39) * (rr / 39)))) - 0.3;
            // colgando hacia abajo e inclinada hacia el tronco
            float yaw = (float) Math.atan2(-Math.cos(a), -Math.sin(a));
            float largo = 7.5f + r.nextFloat() * 3.5f, w = 0.9f + r.nextFloat() * 0.4f;
            Display.BlockDisplay d = display(sl, i % 2 == 0 ? madera : st, p.x, techo, p.z, rama(yaw, 0.4f, 0.02f, w), 7);
            if (d == null) continue;
            grandes.add(d);
            int t0 = t + 15 + i * 3;
            pasos.add(new Paso(d, t0, rama(yaw, 0.4f, largo, w), 16, false));
            for (int k = 0; k < 6; k++)
                pasos.add(new Paso(d, t0 + 18 + k * 16, rama(yaw + (k % 2 == 0 ? 0.12f : -0.12f), k % 2 == 0 ? 0.55f : 0.28f, largo, w), 16, false));
            pasos.add(new Paso(d, t0 + 18 + 6 * 16, rama(yaw, 0.4f, largo, w), 20, false));
        }
    }

    /** Rama que cuelga desde su punto de anclaje: hacia abajo, inclinada 'tilt' hacia (sin yaw, cos yaw). */
    private static Matrix4f rama(float yaw, float tilt, float largo, float w) {
        return new Matrix4f().rotateY(yaw).rotateX((float) Math.PI - tilt).scale(w, largo, w).translate(-0.5f, 0f, -0.5f);
    }

    static BlockPos bp(Vec3 v) {
        return BlockPos.containing(v);
    }
}
