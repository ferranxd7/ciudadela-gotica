package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Altar;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Anselmo, el Pontifice Sangrante (parte 7t): el jefe de la Tierra sin Fe. Era el obispo que guiaba a los peregrinos al crater a
 * pedir perdon; la noche del cielo rojo la Sombra les contesto y el lo entendio como una orden: que dieran su sangre. Se los llevo
 * al Altar de la Sangre (libro 28) y alli sigue, de rodillas ante el altar profanado, con el caliz lleno (libro 29, su sermon).
 * Un viejo altisimo y enjuto con la tiara de tres coronas, la ferula de tres travesanos en la derecha y el caliz en la izquierda.
 * <ul>
 * <li>espera <b>de rodillas</b> ante el altar, de espaldas a la nave; al ver a alguien dentro de la basilica se levanta mientras
 * dice su frase y al acabarla <b>predica</b> (el sermon: los penitentes de alrededor se enfervorizan y van a por quien sea);</li>
 * <li>el <b>ferulazo</b> (alza la ferula y la estrella delante) y el <b>barrido</b>: cortan (hacen sangrar, {@link Sangrado});</li>
 * <li>la <b>aspersion</b>: echa la sangre del caliz en abanico delante: hace sangrar mucho a quien pille;</li>
 * <li>la <b>comunion</b>: alza el caliz; la sangre de quien este sangrando cerca vuela a el (le duele) y le cura (cuanta mas
 * sangre perdida, mas); despues bebe. Mejor no llegar sangrando;</li>
 * <li>el <b>juicio</b>: alza la ferula al cielo y la clava: bajo los pies de cada uno sale una marca y al segundo una
 * <b>columna de sangre</b> que lanza por el aire (apartarse a tiempo);</li>
 * <li>a <b>media vida</b>, la <b>uncion</b>: se vierte el caliz por la cabeza; desde entonces sangra (deja <b>charcos</b>,
 * {@link CharcoSangre}), va mas deprisa, juzga mas a menudo (y con mas marcas) y de los canales de la nave se alza la
 * <b>procesion</b>: penitentes que se deshacen al acabar la pelea.</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_PONTIFICE}): 1 al empezar (no la repite en 4 min), 2 al ungirse,
 * 3 si cae su presa, 4 la primera vez que bebe la sangre de alguien y 5 al caer. Al caer se queda de rodillas, el caliz se le cae
 * y se derrama, y se deshace en sangre; lo demas (el cartel con sus ecos, la hazana {@code pontifice} y el bestiario) lo reparte
 * {@code valdrenn/Guardianes}; los penitentes que seguian rezando en la Tierra sin Fe descansan ({@code morvain/Altar}).
 */
public class Pontifice extends Guardian {
    public static final int ECOS = 17000;
    public static final int VOZ_BASE = Voces.BASE_PONTIFICE;
    /** Si se ha ungido (sangra: lo sabe el cliente, que echa las gotas). */
    private static final EntityDataAccessor<Boolean> UNGIDO = SynchedEntityData.defineId(Pontifice.class, EntityDataSerializers.BOOLEAN);
    /** Hasta donde llega la comunion y la aspersion (y lo abierto del abanico, a cada lado). */
    public static final double R_COMUNION = 14.0, R_ASPERSION = 7.5, ABANICO = 42.0;
    /** Lo que tarda en salir la columna desde la marca (ticks) y su radio. */
    public static final int MARCA_T = 22;
    public static final double R_COLUMNA = 1.45;
    /** Cada cuanto deja un charco desde que se unge (ticks). */
    public static final int CHARCO_CADA = 55;

    public static final DustParticleOptions MARCA = new DustParticleOptions(new Vector3f(0.75f, 0.02f, 0.06f), 1.4f),
            ORO = new DustParticleOptions(new Vector3f(0.95f, 0.78f, 0.3f), 1.0f);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private record Marca(double x, double y, double z, int cuando) {}

    private final List<Marca> marcas = new ArrayList<>();
    private final List<Penitente> suyos = new ArrayList<>();
    /** Los que estaban sangrando al alzar el caliz: de donde vuela la sangre mientras lo sostiene. */
    private final List<Vec3> fuentes = new ArrayList<>();
    private int juicioEspera = 80, comunionEspera = 160, aspEspera = 40, charcoEspera, hablaHasta, esperaRugido, espera = 60;
    private boolean cuatro;
    private LivingEntity ultimaPresa;
    /** Para las pruebas. */
    private int ferulazos, barridos, aspersiones, asperjados, comuniones, bebidos, juicios, marcadas, columnas, columnasQueDan, sermones,
            enfervoriza, alzados, charcos;
    private float curado;
    /** Para las pruebas: no elige golpes ni predica por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue). */
    public boolean quieta;

    public Pontifice(EntityType<? extends Pontifice> t, Level l) {
        super(t, l, "pontifice", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1100.0).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 5.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0).add(Attributes.MOVEMENT_SPEED, 0.21).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(UNGIDO, false);
    }

    /** Si se ha ungido (desde media vida sangra). */
    public boolean ungido() {
        return entityData.get(UNGIDO);
    }

    @Override
    public String tipo() {
        return "pontifice";
    }

    @Override
    public int musica() {
        return 40;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "pontifice";
    }

    @Override
    protected int[] ticks() {
        return PontificeAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return PontificeAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return PontificeAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return PontificeAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return PontificeAnims.SERMON;
    }

    @Override
    protected int accionLevantarse() {
        return PontificeAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int ferulazos() {
        return ferulazos;
    }

    public int barridos() {
        return barridos;
    }

    /** Las aspersiones y a cuantos ha dado. */
    public int aspersiones() {
        return aspersiones;
    }

    public int asperjados() {
        return asperjados;
    }

    /** Las comuniones, a cuantos ha sacado la sangre y lo que se ha curado con ella. */
    public int comuniones() {
        return comuniones;
    }

    public int bebidos() {
        return bebidos;
    }

    public float curado() {
        return curado;
    }

    /** Los juicios, las marcas que ha puesto, las columnas que han salido y las que han dado. */
    public int juicios() {
        return juicios;
    }

    public int marcadas() {
        return marcadas;
    }

    public int columnas() {
        return columnas;
    }

    public int columnasQueDan() {
        return columnasQueDan;
    }

    public int sermones() {
        return sermones;
    }

    public int enfervoriza() {
        return enfervoriza;
    }

    /** Los de la procesion que se han alzado y los que siguen en pie. */
    public int alzados() {
        return alzados;
    }

    public int suyos() {
        int n = 0;
        for (Penitente p : suyos) if (p.isAlive() && !p.isRemoved()) n++;
        return n;
    }

    public List<Penitente> procesion() {
        return List.copyOf(suyos);
    }

    public int charcos() {
        return charcos;
    }

    /** Las marcas que esperan su columna (pruebas). */
    public int marcasPendientes() {
        return marcas.size();
    }

    /** Para las pruebas: pasa ya a media vida (se unge), sin esperar a que acabe lo que hace. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
    }

    /** Para el comando y las pruebas: que caiga ya. */
    public void vencer() {
        if (level().isClientSide || !isAlive()) return;
        kill();
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta || Math.abs(presa.getY() - getY()) > 5.0) return 0;
        // el juicio: de lejos o (ungido) tambien de cerca
        if (juicioEspera <= 0 && d >= (segunda ? 2.0 : 5.0) && d < 22 && random.nextFloat() < (segunda ? 0.45f : 0.3f)) {
            juicioEspera = (segunda ? 150 : 220) + random.nextInt(60);
            return PontificeAnims.JUICIO;
        }
        // la comunion, si hay alguien sangrando cerca
        if (comunionEspera <= 0 && sangrando() > 0 && random.nextFloat() < 0.6f) {
            comunionEspera = 240 + random.nextInt(80);
            return PontificeAnims.COMUNION;
        }
        if (d < 4.4) {
            if (aspEspera <= 0 && random.nextFloat() < 0.3f) {
                aspEspera = 90 + random.nextInt(40);
                return PontificeAnims.ASPERSION;
            }
            return random.nextFloat() < 0.5f ? PontificeAnims.FERULAZO : PontificeAnims.BARRIDO;
        }
        if (d < 7.0 && aspEspera <= 0 && random.nextFloat() < 0.6f) {
            aspEspera = (segunda ? 70 : 100) + random.nextInt(40);
            return PontificeAnims.ASPERSION;
        }
        return 0;
    }

    /** Cuantos estan sangrando (lo bastante) a su alcance. */
    private int sangrando() {
        if (!(level() instanceof ServerLevel sl)) return 0;
        int n = 0;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_COMUNION, 4, R_COMUNION),
                e -> e != this && e.isAlive() && objetivoValido(e)))
            if (le.distanceToSqr(this) < R_COMUNION * R_COMUNION && Sangrado.nivel(le) >= 25f) n++;
        return n;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case PontificeAnims.FERULAZO -> new Golpe(4.8f, 50f, 1.2f, 1.1f);
            case PontificeAnims.BARRIDO -> new Golpe(4.5f, 160f, 0.95f, 1.0f);
            default -> null;
        };
    }

    /** Los travesanos de la ferula cortan: hacen sangrar (ungido, mas). */
    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == PontificeAnims.FERULAZO || accion == PontificeAnims.BARRIDO) Sangrado.sumar(le, ungido() ? 28f : 18f, this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = PontificeAnims.GOLPES[accion];
        switch (accion) {
            case PontificeAnims.FERULAZO -> {
                if (t == g[0] - 7) playSound(DarkCraftMod.SFX_PONTIFICE_FERULA.get(), 1.8f, 0.9f + random.nextFloat() * 0.1f);
                if (t == g[0] - 1) empujar(0.35);
                if (t == g[0]) {
                    ferulazos++;
                    playSound(DarkCraftMod.SFX_PONTIFICE_GOLPE.get(), 2.0f, 0.95f + random.nextFloat() * 0.1f);
                    if (level() instanceof ServerLevel sl) {
                        Vec3 d = Vec3.directionFromRotation(0, getYRot());
                        sl.sendParticles(ParticleTypes.CRIT, getX() + d.x * 3.2, getY() + 0.3, getZ() + d.z * 3.2, 12, 0.4, 0.1, 0.4, 0.1);
                        sl.sendParticles(Sangrado.CHORRO, getX() + d.x * 3.2, getY() + 0.4, getZ() + d.z * 3.2, 14, 0.5, 0.3, 0.5, 0.0);
                    }
                }
            }
            case PontificeAnims.BARRIDO -> {
                if (t == g[0] - 5) playSound(DarkCraftMod.SFX_PONTIFICE_FERULA.get(), 1.8f, 1.05f + random.nextFloat() * 0.1f);
                if (t == g[0] - 1) empujar(0.25);
                if (t == g[0]) {
                    barridos++;
                    if (level() instanceof ServerLevel sl)
                        for (int j = 0; j <= 10; j++) {
                            double a = Math.toRadians(getYRot() + 90 - 80 + 160 * j / 10.0);
                            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + Math.cos(a) * 3.2, getY() + 1.8, getZ() + Math.sin(a) * 3.2, 1, 0, 0, 0, 0);
                        }
                }
            }
            case PontificeAnims.ASPERSION -> {
                if (t == g[0] - 6) playSound(DarkCraftMod.SFX_PONTIFICE_ASPERSION.get(), 1.8f, 1.0f);
                if (t == g[0]) aspersion();
            }
            case PontificeAnims.COMUNION -> {
                if (t == g[0]) comunion();
                // mientras sostiene el caliz, la sangre vuela a el desde donde estaban los que sangraban
                if (t > g[0] && t < g[0] + 30 && t % 2 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 c = caliz();
                    for (Vec3 f : fuentes)
                        for (int k = 0; k < 6; k++) {
                            double s = ((t - g[0]) * 0.05 + k / 6.0) % 1.0;
                            sl.sendParticles(Sangrado.CHORRO, Mth.lerp(s, f.x, c.x), Mth.lerp(s, f.y, c.y) + Math.sin(s * Math.PI) * 1.6, Mth.lerp(s, f.z, c.z),
                                    1, 0.05, 0.05, 0.05, 0.0);
                        }
                }
                if (t == 55) playSound(DarkCraftMod.SFX_PONTIFICE_BEBE.get(), 1.6f, 1.0f);
            }
            case PontificeAnims.JUICIO -> {
                if (t == 8) playSound(DarkCraftMod.SFX_PONTIFICE_JUICIO.get(), 2.0f, 1.0f);
                if (t == g[0]) juicio();
            }
            case PontificeAnims.SERMON -> {
                if (t == 4) playSound(DarkCraftMod.SFX_PONTIFICE_SERMON.get(), 2.6f, 1.0f);
                if (t == g[0]) sermon();
            }
            case PontificeAnims.UNCION -> {
                if (t == 10) playSound(DarkCraftMod.SFX_PONTIFICE_UNCION.get(), 2.4f, 1.0f);
                if (t >= g[0] - 2 && t <= g[0] + 12 && level() instanceof ServerLevel sl)
                    sl.sendParticles(Sangrado.CHORRO, getX(), getY() + 4.4, getZ(), 6, 0.35, 0.2, 0.35, 0.0);
                if (t == g[0]) uncion();
            }
            case PontificeAnims.LEVANTARSE -> {
                if (t == 3 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(Sangrado.GOTA, getX(), getY() + 1.4, getZ(), 30, 0.6, 0.8, 0.6, 0.0);
                    sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0, getZ(), 8, 0.6, 0.4, 0.6, 0.02);
                }
            }
            default -> {
            }
        }
    }

    /** Donde esta el caliz (en la izquierda, a la altura del pecho; alzado, por encima de la tiara). */
    public Vec3 caliz() {
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        double alto = accion() == PontificeAnims.COMUNION ? 4.6 : 2.4;
        return new Vec3(getX() + d.x * 0.6 + d.z * 0.5, getY() + alto, getZ() + d.z * 0.6 - d.x * 0.5);
    }

    // ------------------------------------------------------------------ la aspersion

    /** Echa la sangre del caliz en abanico delante: a quien pille (a la vista), sangre y algo de dano. */
    private void aspersion() {
        if (!(level() instanceof ServerLevel sl)) return;
        aspersiones++;
        Vec3 o = caliz();
        for (int k = 0; k <= 16; k++) {
            double a = Math.toRadians(getYRot() + 90 - ABANICO + 2 * ABANICO * k / 16.0);
            for (double r = 1.5; r <= R_ASPERSION; r += 1.5)
                sl.sendParticles(Sangrado.CHORRO, getX() + Math.cos(a) * r, getY() + 1.6 - r * 0.15, getZ() + Math.sin(a) * r, 2, 0.15, 0.15, 0.15, 0.0);
        }
        sl.sendParticles(Sangrado.GOTA, o.x, o.y, o.z, 20, 0.3, 0.2, 0.3, 0.0);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.35f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_ASPERSION, 3, R_ASPERSION),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > R_ASPERSION + le.getBbWidth() * 0.5 || !hasLineOfSight(le)) continue;
            float ang = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
            if (Math.abs(Mth.wrapDegrees(ang - getYRot())) > ABANICO + 8) continue;
            asperjados++;
            le.hurt(damageSources().mobAttack(this), dano);
            Sangrado.sumar(le, ungido() ? 44f : 34f, this);
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), this);
        }
    }

    // ------------------------------------------------------------------ la comunion

    /** Alza el caliz: la sangre de quien este sangrando cerca vuela a el (le duele) y le cura. */
    private void comunion() {
        if (!(level() instanceof ServerLevel sl)) return;
        comuniones++;
        fuentes.clear();
        playSound(DarkCraftMod.SFX_PONTIFICE_COMUNION.get(), 2.4f, 1.0f);
        float cura = 0;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_COMUNION, 4, R_COMUNION),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(this) > R_COMUNION * R_COMUNION) continue;
            float n = Sangrado.nivel(le);
            if (n < 1f) continue;
            bebidos++;
            fuentes.add(new Vec3(le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ()));
            Sangrado.limpiar(le);
            le.invulnerableTime = 0;
            le.hurt(damageSources().indirectMagic(this, this), Math.max(1f, n * 0.08f));
            cura += Math.min(70f, n * 0.6f);
            sl.sendParticles(Sangrado.CHORRO, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 24, 0.3, 0.4, 0.3, 0.0);
        }
        if (cura > 0) {
            heal(cura);
            curado += cura;
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 4.2, getZ(), 3, 0.4, 0.3, 0.4, 0.0);
            if (!cuatro) {
                cuatro = true;
                hablaSiCalla(4);
            }
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] El Pontifice alza el caliz: bebe de {} y se cura {}", fuentes.size(), Math.round(cura));
    }

    // ------------------------------------------------------------------ el juicio

    /** Clava la ferula: una marca bajo los pies de cada uno (ungido, ademas otra cerca); al segundo, la columna de sangre. */
    private void juicio() {
        if (!(level() instanceof ServerLevel sl)) return;
        juicios++;
        playSound(DarkCraftMod.SFX_PONTIFICE_GOLPE.get(), 2.4f, 0.7f);
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        sl.sendParticles(Sangrado.CHORRO, getX() + d.x * 1.6, getY() + 0.2, getZ() + d.z * 1.6, 30, 0.6, 0.1, 0.6, 0.0);
        List<LivingEntity> blancos = new ArrayList<>(sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(24, 6, 24),
                e -> e != this && e.isAlive() && objetivoValido(e) && (e instanceof Player || e == getTarget())));
        for (LivingEntity le : blancos) {
            if (le.distanceToSqr(this) > 24 * 24) continue;
            marcar(sl, le.getX(), le.getZ(), le.getY());
            if (ungido()) {
                double a = random.nextDouble() * Math.PI * 2, r = 2.2 + random.nextDouble();
                marcar(sl, le.getX() + Math.cos(a) * r, le.getZ() + Math.sin(a) * r, le.getY());
            }
        }
    }

    private void marcar(ServerLevel sl, double x, double z, double cerca) {
        double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(cerca) + 2, Mth.floor(z));
        if (Math.abs(y - cerca) > 4) y = cerca;
        marcas.add(new Marca(x, y, z, tickCount + MARCA_T));
        marcadas++;
        sl.playSound(null, x, y, z, SoundEvents.SCULK_CLICKING, SoundSource.HOSTILE, 1.4f, 0.6f);
    }

    /** Cada tick: las marcas echan su aviso y, a su hora, la columna. */
    private void tickMarcas(ServerLevel sl) {
        for (int i = marcas.size() - 1; i >= 0; i--) {
            Marca m = marcas.get(i);
            if (tickCount < m.cuando()) {
                if (tickCount % 2 == 0)
                    for (int k = 0; k < 10; k++) {
                        double a = (k / 10.0 + tickCount * 0.02) * Math.PI * 2;
                        sl.sendParticles(MARCA, m.x() + Math.cos(a) * R_COLUMNA, m.y() + 0.08, m.z() + Math.sin(a) * R_COLUMNA, 1, 0, 0, 0, 0);
                    }
                if (tickCount % 4 == 0) sl.sendParticles(Sangrado.GOTA, m.x(), m.y() + 0.1, m.z(), 3, 0.5, 0.0, 0.5, 0.0);
                continue;
            }
            marcas.remove(i);
            columna(sl, m);
        }
    }

    /** La columna de sangre: lanza por el aire a quien este encima, le hace dano y sangrar. */
    private void columna(ServerLevel sl, Marca m) {
        columnas++;
        sl.playSound(null, m.x(), m.y(), m.z(), DarkCraftMod.SFX_PONTIFICE_COLUMNA.get(), SoundSource.HOSTILE, 1.8f, 0.9f + random.nextFloat() * 0.2f);
        for (double h = 0; h <= 4.5; h += 0.5)
            sl.sendParticles(Sangrado.CHORRO, m.x(), m.y() + h, m.z(), 6, 0.35, 0.15, 0.35, 0.0);
        sl.sendParticles(ParticleTypes.FALLING_DRIPSTONE_LAVA, m.x(), m.y() + 4.6, m.z(), 10, 0.6, 0.1, 0.6, 0.0);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.85f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(m.x() - 2, m.y() - 1, m.z() - 2, m.x() + 2, m.y() + 3.5, m.z() + 2),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(m.x(), le.getY(), m.z()) > R_COLUMNA * R_COLUMNA) continue;
            if (le.hurt(damageSources().indirectMagic(this, this), dano)) {
                columnasQueDan++;
                le.setDeltaMovement(le.getDeltaMovement().x * 0.3, 0.78, le.getDeltaMovement().z * 0.3);
                le.hurtMarked = true;
                Sangrado.sumar(le, 40f, this);
            }
        }
    }

    // ------------------------------------------------------------------ el sermon, la uncion y la procesion

    /** Predica: los penitentes de alrededor se enfervorizan (pegan mas) y van a por su presa. */
    private void sermon() {
        if (!(level() instanceof ServerLevel sl)) return;
        sermones++;
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 3.0, getZ(), 30, 1.2, 1.2, 1.2, 0.04);
        sl.sendParticles(Sangrado.GOTA, getX(), getY() + 3.0, getZ(), 30, 1.4, 1.0, 1.4, 0.0);
        LivingEntity t = getTarget();
        for (Penitente p : sl.getEntitiesOfClass(Penitente.class, getBoundingBox().inflate(24, 8, 24), Penitente::isAlive)) {
            p.animo(0.25, 240);
            enfervoriza++;
            if (t != null && p.getTarget() == null) p.acudir(t);
        }
    }

    /** Se vierte el caliz por la cabeza: desde ahora sangra (charcos, mas deprisa) y se alza la procesion. */
    private void uncion() {
        if (!(level() instanceof ServerLevel sl)) return;
        entityData.set(UNGIDO, true);
        charcoEspera = 10;
        sl.sendParticles(Sangrado.CHORRO, getX(), getY() + 2.5, getZ(), 80, 0.5, 1.4, 0.5, 0.0);
        hablaHasta = 0;
        habla(2);
        procesion(sl);
    }

    /** De los canales de sangre de la nave se alzan los penitentes de la procesion (efimeros: se deshacen al acabar la pelea). */
    private void procesion(ServerLevel sl) {
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.max(2, Math.min(4, 1 + cerca.size()));
        List<int[]> sitios = Altar.enNave(getX(), getZ()) ? new ArrayList<>(Altar.procesion()) : new ArrayList<>();
        if (sitios.isEmpty())
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3;
                sitios.add(new int[]{Mth.floor(getX() + Math.cos(a) * 7), Mth.floor(getZ() + Math.sin(a) * 7)});
            }
        // los sitios mas lejos de los jugadores primero (que no salgan encima de nadie)
        sitios.sort((a, b) -> Double.compare(lejania(b, cerca), lejania(a, cerca)));
        int[] variantes = {Penitente.FLAGELANTE, Penitente.PORTADOR, Penitente.FLAGELANTE, Penitente.TURIFERARIO};
        int salen = 0;
        for (int[] q : sitios) {
            if (salen >= n) break;
            int bx = q[0], bz = q[1];
            double y = Puestos.suelo(sl, bx, Mth.floor(getY()) + 2, bz);
            if (Math.abs(y - getY()) > 2.6 || lejania(q, cerca) < 3.0) continue;
            Penitente p = DarkCraftMod.PENITENTE.get().create(sl);
            if (p == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            p.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            p.variante(variantes[salen % variantes.length]);
            if (!sl.noCollision(p)) continue;
            // tendidos en la sangre: se alzan de golpe (sin puesto: no vuelven)
            p.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            p.efimero(240);
            sl.addFreshEntity(p);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(p.getX(), p.getY(), p.getZ(), 30, e -> e instanceof Player pl && objetivoValido(pl));
            if (t != null) p.setTarget(t);
            sl.sendParticles(Sangrado.CHORRO, bx + 0.5, y + 0.3, bz + 0.5, 30, 0.4, 0.4, 0.4, 0.0);
            sl.playSound(null, bx + 0.5, y, bz + 0.5, DarkCraftMod.SFX_PENITENTE_ALZARSE.get(), SoundSource.HOSTILE, 1.4f, 0.8f + random.nextFloat() * 0.2f);
            suyos.add(p);
            salen++;
        }
        alzados += salen;
        playSound(DarkCraftMod.SFX_PONTIFICE_PROCESION.get(), 2.4f, 1.0f);
        DarkCraftMod.LOGGER.info("[DarkCraft] El Pontifice se unge: se alza la procesion, {} penitentes ({} jugadores)", salen, cerca.size());
    }

    private static double lejania(int[] q, List<ServerPlayer> jugadores) {
        double m = Double.MAX_VALUE;
        for (ServerPlayer p : jugadores) m = Math.min(m, Math.hypot(p.getX() - (q[0] + 0.5), p.getZ() - (q[1] + 0.5)));
        return m;
    }

    private void despedir() {
        for (Penitente p : suyos) if (p.isAlive() && !p.isRemoved()) p.disolver();
        suyos.clear();
    }

    // ------------------------------------------------------------------ lo que dice

    /** Dice la frase n (1..5) a quien este cerca (voz y subtitulo). */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 64 * 64) Net.to(p, new Net.ReyHabla(getId(), VOZ_BASE + n));
        hablaHasta = tickCount + Voces.ticks(VOZ_BASE + n);
    }

    private void hablaSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) habla(n);
    }

    @Override
    protected void alEmpezarPelea() {
        cuatro = false;
        juicioEspera = 80;
        comunionEspera = 160;
        aspEspera = 40;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se levanta mientras lo dice y no predica ni ataca hasta acabarlo
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaRugido = tickCount + dura - 10;
            pausa = Math.max(pausa, dura);
        } else esperaRugido = 0;
    }

    @Override
    protected boolean puedeRugir() {
        return !quieta && (segunda || tickCount >= esperaRugido);
    }

    @Override
    protected void alAcabarPelea() {
        despedir();
        marcas.clear();
        fuentes.clear();
        entityData.set(UNGIDO, false);
        esperaRugido = 0;
    }

    /** A media vida no predica: se unge (se vierte el caliz) y se alza la procesion. */
    @Override
    protected void alSegundaMitad() {
        empezar(PontificeAnims.UNCION);
        pausa = Math.max(pausa, 6);
        juicioEspera = Math.min(juicioEspera, 60);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Solo repara en quien esta dentro de la basilica (los de fuera no le importan), si reza en ella. */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (Altar.enNave(getX(), getZ()) && !Altar.enNave(p.getX(), p.getZ())) return false;
        return super.percibe(p);
    }

    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Penitente) return false;
        if (e instanceof Player && enPelea() && Altar.enNave(getX(), getZ()) && !Altar.dentro(e.getX(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive() && deathTime > PontificeAnims.TICKS[PontificeAnims.MORIR]) return;
            // ungido, sangra: gotas que le caen de la tiara y del alba
            if (ungido() && tickCount % 2 == 0)
                level().addParticle(Sangrado.GOTA, getX() + (random.nextDouble() - 0.5) * 1.0, getY() + 0.4 + random.nextDouble() * 3.8,
                        getZ() + (random.nextDouble() - 0.5) * 1.0, 0, -0.05, 0);
            return;
        }
        if (!isAlive()) return;
        if (juicioEspera > 0) juicioEspera--;
        if (comunionEspera > 0) comunionEspera--;
        if (aspEspera > 0) aspEspera--;
        ServerLevel sl = (ServerLevel) level();
        tickMarcas(sl);
        // ungido, deja charcos por donde pisa
        if (ungido() && enPelea() && onGround() && --charcoEspera <= 0) {
            charcoEspera = CHARCO_CADA + random.nextInt(20);
            sl.addFreshEntity(new CharcoSangre(sl, getX(), getY(), getZ(), this));
            charcos++;
        }
        // de rodillas ante el altar: su rezo
        if (!enPelea() && postura() == REZA && --espera <= 0) {
            espera = 180 + random.nextInt(80);
            playSound(DarkCraftMod.SFX_PONTIFICE_REZO.get(), 1.5f, 1.0f);
        }
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        despedir();
        marcas.clear();
        super.die(s);
        if (level().isClientSide || ya) return;
        hablaHasta = 0;
        habla(5);
        if (level() instanceof ServerLevel sl) sl.sendParticles(Sangrado.CHORRO, getX(), getY() + 2.4, getZ(), 50, 0.8, 1.2, 0.8, 0.0);
    }

    /** Al caer no se va en humo: se queda de rodillas, el caliz se le cae y se derrama, dice su ultima frase y se deshace en sangre. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = PontificeAnims.TICKS[PontificeAnims.MORIR] + 50;
        if (level() instanceof ServerLevel sl) {
            Vec3 d = Vec3.directionFromRotation(0, getYRot());
            if (deathTime >= 30 && deathTime <= 60 && deathTime % 3 == 0)
                // el caliz derramado: la sangre se extiende delante de el
                sl.sendParticles(Sangrado.CHORRO, getX() + d.x * 1.2, getY() + 0.08, getZ() + d.z * 1.2, 6, 0.3 + (deathTime - 30) * 0.03, 0.0,
                        0.3 + (deathTime - 30) * 0.03, 0.0);
            if (deathTime == 32) sl.playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 1.4f, 0.6f);
            if (deathTime % 4 == 0 && deathTime < fin - 6)
                sl.sendParticles(Sangrado.GOTA, getX(), getY() + 1.8, getZ(), 4, 0.4, 0.9, 0.4, 0.0);
            if (deathTime > fin - 30 && deathTime % 2 == 0)
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0 + (deathTime - (fin - 30)) * 0.08, getZ(), 3, 0.5, 0.6, 0.5, 0.02);
            if (deathTime == fin - 2) {
                // se deshace en sangre
                sl.sendParticles(Sangrado.CHORRO, getX(), getY() + 1.8, getZ(), 160, 0.7, 1.6, 0.7, 0.0);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.0, getZ(), 40, 0.6, 1.2, 0.6, 0.05);
                sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_PENITENTE_HEMORRAGIA.get(), SoundSource.HOSTILE, 2.0f, 0.6f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    /** Mientras dice su primera frase se queda donde esta (levantandose); despues, cerca: pelea de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 3.4;
    }

    @Override
    protected double prisa() {
        return ungido() ? 1.15 : 1.0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case PontificeAnims.SERMON, PontificeAnims.UNCION -> 6;
            case PontificeAnims.JUICIO -> (ungido() ? 6 : 10) + random.nextInt(8);
            case PontificeAnims.COMUNION -> 8 + random.nextInt(8);
            case PontificeAnims.ASPERSION -> (ungido() ? 8 : 12) + random.nextInt(10);
            default -> (ungido() ? 7 : 11) + random.nextInt(12);
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    /** Las zapatillas en la piedra, y la contera de la ferula. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(tickCount % 2 == 0 ? SoundEvents.DEEPSLATE_TILES_STEP : SoundEvents.CHAIN_STEP, 0.5f, 0.8f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_PONTIFICE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_PONTIFICE_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.SANGRE).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; ferulazos %d, barridos %d, aspersiones %d (dan a %d), "
                        + "comuniones %d (bebe de %d, se cura %.0f), juicios %d (marcas %d, columnas %d, dan %d), sermones %d (enfervoriza a %d), "
                        + "procesion %d (en pie %d), charcos %d%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "de rodillas",
                getX(), getY(), getZ(), ferulazos, barridos, aspersiones, asperjados, comuniones, bebidos, curado, juicios, marcadas, columnas,
                columnasQueDan, sermones, enfervoriza, alzados, suyos(), charcos, ungido() ? " (ungido)" : "");
    }
}
