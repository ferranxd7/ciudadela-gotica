package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Teatro;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Pell, el Bufon del Rey (parte 7y): el jefe secreto del Teatro Hundido. Hacia reir al ultimo rey de Morvain; la noche del cielo
 * rojo estaba en escena, y siguio actuando. Mil anos despues aun espera publico en la Sala de las Mascaras, bajo el escenario,
 * detras de la puerta real tapiada (un muro ilusorio). Delgado y agil, la mascara mitad blanca y mitad negra, el traje de rombos
 * rojos y negros, el gorro de tres puntas con cascabeles y una daga en cada mano.
 * <ul>
 * <li>al ver a alguien en su sala hace una <b>reverencia</b> mientras dice su frase (y se rie); despues pelea a saltitos;</li>
 * <li>el <b>tajo</b> doble con las dagas, la <b>pirueta</b> (dos vueltas con los brazos abiertos: pega todo alrededor), los
 * <b>cuchillos</b> (tres, de lejos, uno tras otro), los <b>malabares</b> (tres bolas de fuego que tira en arco: revientan en
 * llamas), el <b>humo</b> (estrella una bomba, ciega a quien este cerca y sale de un salto por su espalda) y la
 * <b>voltereta</b> hacia atras (mientras gira no le entra nada; luego tira cuchillos);</li>
 * <li>a <b>media vida</b> (su frase 2) se <b>parte en tres</b>: dos ilusiones iguales a el (la misma entidad, con
 * {@link #ilusion()}) que pelean (pegan menos) y se deshacen de un golpe; el de verdad cambia de sitio con una de ellas. Pegarle
 * al de verdad deshace las que queden. Desde entonces lo repite cada rato.</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_BUFON}): 1 al empezar (no la repite en 4 min), 2 al partirse,
 * 3 si cae su presa, 4 la primera vez que hace malabares y 5 al caer. Al caer se sienta, se tumba, dice su ultima frase y, al
 * acabar, el teatro vacio aplaude. Lo demas (el cartel con sus ecos, la hazana {@code bufon} y el bestiario) lo reparte
 * {@code valdrenn/Guardianes}.
 */
public class Bufon extends Guardian {
    public static final int ECOS = 16000;
    public static final int VOZ_BASE = Voces.BASE_BUFON;
    private static final EntityDataAccessor<Boolean> ILUSION = SynchedEntityData.defineId(Bufon.class, EntityDataSerializers.BOOLEAN);
    /** Lo que dura una ilusion (ticks) y cuantas salen cada vez. */
    public static final int VIDA_ILUSION = 360, COPIAS = 2;
    /** La nube de la bomba de humo (radio) y lo que ciega (ticks). */
    public static final double R_HUMO = 3.8;
    public static final int CIEGA = 40;
    /** Mientras da la voltereta no le entra nada (ticks). */
    public static final int VOLTERETA_INVULNERABLE = 13;
    /** Hasta donde ve desde el medio de su sala (bloques). */
    public static final double VE = 20;
    static final DustParticleOptions ROJO = new DustParticleOptions(new Vector3f(0.85f, 0.08f, 0.1f), 1.2f),
            ORO = new DustParticleOptions(new Vector3f(0.95f, 0.78f, 0.2f), 1.1f),
            NEGRO = new DustParticleOptions(new Vector3f(0.08f, 0.06f, 0.08f), 1.3f),
            BLANCO = new DustParticleOptions(new Vector3f(0.95f, 0.93f, 0.9f), 1.0f);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int cuchilloEspera = 30, malabarEspera = 80, humoEspera = 120, ilusionEspera, volteretaEspera = 40, piruetaEspera = 60,
            risaEspera = 240, hablaHasta, esperaPelea, golpesSeguidos, ultimoGolpe, idle = 200;
    private boolean cuatro, humoPendiente, trasVoltereta, enVuelo;
    private double vueloX, vueloZ;
    private LivingEntity ultimaPresa;
    private final List<Bufon> ilusiones = new ArrayList<>();
    /** En una ilusion: el de verdad. */
    private Bufon dueno;
    private int vidaIlusion;
    /** Para las pruebas. */
    private int tajos, piruetas, cuchillos, malabares, bolas, humos, cegados, apariciones, volteretas, esquivados, partidas, deshechas, risas;
    /** Para las pruebas: no elige golpes por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue). */
    public boolean quieta;

    public Bufon(EntityType<? extends Bufon> t, Level l) {
        super(t, l, "bufon", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 11.0).add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ILUSION, false);
    }

    /** Si es una de sus ilusiones (una copia que pelea y se deshace de un golpe). */
    public boolean ilusion() {
        return entityData.get(ILUSION);
    }

    @Override
    public String tipo() {
        return "bufon";
    }

    @Override
    public int musica() {
        return 44;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "bufon";
    }

    @Override
    protected int[] ticks() {
        return BufonAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return BufonAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return BufonAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return BufonAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return BufonAnims.REVERENCIA;
    }

    @Override
    protected int accionLevantarse() {
        return BufonAnims.APARECE;
    }

    @Override
    protected boolean empiezaPeleas() {
        return !ilusion();
    }

    // ------------------------------------------------------------------ para las pruebas

    public int tajos() {
        return tajos;
    }

    public int piruetas() {
        return piruetas;
    }

    /** Los cuchillos que ha tirado y las bolas de fuego (y las tandas de malabares). */
    public int cuchillos() {
        return cuchillos;
    }

    public int bolas() {
        return bolas;
    }

    public int malabares() {
        return malabares;
    }

    /** Las bombas de humo, a cuantos ha cegado y las veces que ha salido del humo. */
    public int humos() {
        return humos;
    }

    public int cegados() {
        return cegados;
    }

    public int apariciones() {
        return apariciones;
    }

    /** Las volteretas y los golpes que ha esquivado en ellas. */
    public int volteretas() {
        return volteretas;
    }

    public int esquivados() {
        return esquivados;
    }

    /** Las veces que se ha partido en tres y las ilusiones que se han deshecho. */
    public int partidas() {
        return partidas;
    }

    public int deshechas() {
        return deshechas;
    }

    /** Las ilusiones que tiene ahora. */
    public List<Bufon> ilusiones() {
        ilusiones.removeIf(b -> b.isRemoved() || !b.isAlive());
        return ilusiones;
    }

    /** Para las pruebas: pasa ya a media vida (se parte en tres), sin esperar a que acabe lo que hace. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive() || ilusion()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
    }

    /** Para el comando y las pruebas: que caiga ya. */
    public void vencer() {
        if (level().isClientSide || !isAlive() || ilusion()) return;
        kill();
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta) return 0;
        if (ilusion()) {
            if (d < 3.0) return random.nextFloat() < 0.7f ? BufonAnims.TAJO : BufonAnims.PIRUETA;
            if (d < 12 && cuchilloEspera <= 0 && random.nextFloat() < 0.2f) {
                cuchilloEspera = 70 + random.nextInt(40);
                return BufonAnims.CUCHILLOS;
            }
            return 0;
        }
        if (Math.abs(presa.getY() - getY()) > 5) return 0;
        // a media vida: se parte en tres (cuando no le queda ninguna)
        if (segunda && ilusionEspera <= 0 && ilusiones().isEmpty() && d < 16) {
            ilusionEspera = 320 + random.nextInt(120);
            return BufonAnims.ILUSION;
        }
        // el humo: se esfuma y sale por la espalda
        if (humoEspera <= 0 && d < 9 && random.nextFloat() < (segunda ? 0.3f : 0.18f)) {
            humoEspera = (segunda ? 150 : 220) + random.nextInt(60);
            return BufonAnims.HUMO;
        }
        if (d < 3.2) {
            if (volteretaEspera <= 0 && (golpesSeguidos >= 2 || random.nextFloat() < 0.12f)) {
                volteretaEspera = 80 + random.nextInt(30);
                golpesSeguidos = 0;
                return BufonAnims.VOLTERETA;
            }
            if (piruetaEspera <= 0 && random.nextFloat() < 0.3f) {
                piruetaEspera = 90 + random.nextInt(40);
                return BufonAnims.PIRUETA;
            }
            return BufonAnims.TAJO;
        }
        if (d < 18 && hasLineOfSight(presa) && (d > 6 || trasVoltereta && d > 3.4)) {
            if (malabarEspera <= 0 && !trasVoltereta && random.nextFloat() < 0.35f) {
                malabarEspera = (segunda ? 140 : 200) + random.nextInt(60);
                return BufonAnims.MALABARES;
            }
            if (cuchilloEspera <= 0 && (trasVoltereta || random.nextFloat() < 0.45f)) {
                trasVoltereta = false;
                cuchilloEspera = (segunda ? 50 : 70) + random.nextInt(30);
                return BufonAnims.CUCHILLOS;
            }
            if (risaEspera <= 0 && random.nextFloat() < 0.06f) {
                risaEspera = 300 + random.nextInt(200);
                return BufonAnims.RISA;
            }
        }
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case BufonAnims.TAJO -> new Golpe(3.3f, 110f, 0.75f, 0.35f);
            case BufonAnims.PIRUETA -> new Golpe(3.1f, 360f, 0.65f, 0.7f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == BufonAnims.PIRUETA) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 14, 0), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = BufonAnims.GOLPES[accion];
        float vol = ilusion() ? 0.7f : 1.4f;
        switch (accion) {
            case BufonAnims.REVERENCIA -> {
                if (t == 4) playSound(DarkCraftMod.SFX_BUFON_CASCABELES.get(), 1.2f, 1.0f);
                if (t == g[0]) confeti(18);
            }
            case BufonAnims.TAJO -> {
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_BUFON_TAJO.get(), vol, 0.95f + random.nextFloat() * 0.1f);
                if (t == g[0] - 1) empujar(0.28);
                if (t == g[0] && !ilusion()) tajos++;
            }
            case BufonAnims.PIRUETA -> {
                if (t == 5) playSound(DarkCraftMod.SFX_BUFON_PIRUETA.get(), vol, 1.0f);
                if (t == g[0] - 2 || t == g[1] - 2) empujar(0.18);
                if (t == g[0] && !ilusion()) piruetas++;
            }
            case BufonAnims.CUCHILLOS -> {
                if (t == g[0] - 5) playSound(DarkCraftMod.SFX_BUFON_CUCHILLOS.get(), vol, 1.0f);
                for (int k = 0; k < g.length; k++) if (t == g[k]) tirarCuchillo(k);
            }
            case BufonAnims.MALABARES -> {
                if (t == 6) {
                    playSound(DarkCraftMod.SFX_BUFON_MALABAR.get(), vol, 1.0f);
                    if (!ilusion()) malabares++;
                }
                if (t == 10 && !cuatro && !ilusion()) {
                    cuatro = true;
                    hablaSiCalla(4);
                }
                for (int k = 0; k < g.length; k++) if (t == g[k]) tirarBola(k);
            }
            case BufonAnims.HUMO -> {
                if (t == g[0]) bomba();
                if (t == g[0] + 2) esfumarse();
            }
            case BufonAnims.APARECE -> {
                if (t == 1 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 16, 0.4, 0.6, 0.4, 0.02);
                    confeti(10);
                    playSound(DarkCraftMod.SFX_BUFON_CASCABELES.get(), 1.2f, 1.1f);
                }
            }
            case BufonAnims.VOLTERETA -> voltereta(t);
            case BufonAnims.ILUSION -> {
                if (t == 4) playSound(DarkCraftMod.SFX_BUFON_ILUSION.get(), 1.8f, 1.0f);
                if (t == g[0]) partirse();
            }
            case BufonAnims.RISA -> {
                if (t == 2) {
                    playSound(DarkCraftMod.SFX_BUFON_RISA.get(), vol, 1.0f + random.nextFloat() * 0.08f);
                    if (!ilusion()) risas++;
                }
            }
            default -> {
            }
        }
    }

    /** Confeti de papel rojo, dorado, negro y blanco (en la reverencia, al salir del humo y al caer). */
    private void confeti(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        for (DustParticleOptions o : new DustParticleOptions[]{ROJO, ORO, NEGRO, BLANCO})
            sl.sendParticles(o, getX(), getY() + 2.0, getZ(), n, 0.7, 0.5, 0.7, 0.05);
    }

    /** De donde salen los cuchillos y las bolas: la mano derecha, delante del pecho. */
    private Vec3 mano() {
        Vec3 d = Vec3.directionFromRotation(0, getYRot()), lado = Vec3.directionFromRotation(0, getYRot() + 90);
        return new Vec3(getX() + d.x * 0.5 - lado.x * 0.35, getY() + 1.55, getZ() + d.z * 0.5 - lado.z * 0.35);
    }

    /** Un cuchillo hacia el pecho de su presa (el del medio, recto; los otros dos, un poco a cada lado). */
    private void tirarCuchillo(int k) {
        if (!(level() instanceof ServerLevel sl)) return;
        LivingEntity p = getTarget();
        Vec3 o = mano();
        Vec3 dir = p == null ? Vec3.directionFromRotation(0, getYRot())
                : new Vec3(p.getX() - o.x, p.getY() + p.getBbHeight() * 0.6 - o.y, p.getZ() - o.z).normalize();
        double desvio = (k - 1) * Math.toRadians(5);
        dir = new Vec3(dir.x * Math.cos(desvio) - dir.z * Math.sin(desvio), dir.y, dir.x * Math.sin(desvio) + dir.z * Math.cos(desvio));
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5f;
        ProyectilBufon c = new ProyectilBufon(sl, this, false, dano);
        c.setPos(o.x, o.y, o.z);
        c.shoot(dir.x, dir.y, dir.z, 1.8f, 1.2f);
        sl.addFreshEntity(c);
        if (!ilusion()) cuchillos++;
    }

    /** Una bola de fuego en arco hacia donde va su presa (la primera a su izquierda, la segunda a ella, la tercera a su derecha). */
    private void tirarBola(int k) {
        if (!(level() instanceof ServerLevel sl)) return;
        LivingEntity p = getTarget();
        Vec3 o = mano().add(0, 0.3, 0);
        Vec3 d = Vec3.directionFromRotation(0, getYRot()), lado = Vec3.directionFromRotation(0, getYRot() + 90);
        double tx, ty, tz;
        if (p != null) {
            Vec3 v = p.getDeltaMovement();
            tx = p.getX() + v.x * 12;
            tz = p.getZ() + v.z * 12;
            ty = p.getY();
        } else {
            tx = getX() + d.x * 8;
            tz = getZ() + d.z * 8;
            ty = getY();
        }
        tx += lado.x * (k - 1) * 1.8;
        tz += lado.z * (k - 1) * 1.8;
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
        ProyectilBufon b = new ProyectilBufon(sl, this, true, dano);
        b.setPos(o.x, o.y, o.z);
        b.setDeltaMovement(ProyectilBufon.arco(o.x, o.y, o.z, tx, ty, tz));
        sl.addFreshEntity(b);
        if (!ilusion()) bolas++;
    }

    /** La bomba de humo contra el suelo: la nube; a quien este dentro le ciega un momento. */
    private void bomba() {
        if (!(level() instanceof ServerLevel sl)) return;
        humos++;
        playSound(DarkCraftMod.SFX_BUFON_HUMO.get(), 1.8f, 1.0f);
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.6, getZ(), 40, R_HUMO * 0.35, 0.5, R_HUMO * 0.35, 0.03);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 60, R_HUMO * 0.4, 0.8, R_HUMO * 0.4, 0.02);
        sl.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.3, getZ(), 30, 0.8, 0.2, 0.8, 0.08);
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_HUMO, 2, R_HUMO),
                e -> e != this && e.isAlive() && e instanceof Player && objetivoValido(e))) {
            if (le.distanceToSqr(this) > R_HUMO * R_HUMO) continue;
            le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, CIEGA, 0), this);
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CIEGA / 2, 0), this);
            cegados++;
        }
    }

    /** Se esfuma en el humo y aparece por la espalda de su presa (o en otro sitio de su sala, si no cabe alli). */
    private void esfumarse() {
        if (!(level() instanceof ServerLevel sl)) return;
        setInvisible(true);
        humoPendiente = true;
        LivingEntity p = getTarget();
        Vec3 sitio = null;
        if (p != null) {
            Vec3 m = Vec3.directionFromRotation(0, p.getYRot());
            for (double a : new double[]{0, 40, -40, 80, -80}) {
                double r = Math.toRadians(a);
                Vec3 atras = new Vec3(-(m.x * Math.cos(r) - m.z * Math.sin(r)), 0, -(m.x * Math.sin(r) + m.z * Math.cos(r)));
                Vec3 q = new Vec3(p.getX() + atras.x * 2.6, p.getY(), p.getZ() + atras.z * 2.6);
                if (cabe(sl, q)) {
                    sitio = q;
                    break;
                }
            }
        }
        if (sitio == null) {
            for (int i = 0; i < 12 && sitio == null; i++) {
                double a = random.nextDouble() * Math.PI * 2, r = 4 + random.nextDouble() * 4;
                double cx = p != null ? p.getX() : getX(), cz = p != null ? p.getZ() : getZ();
                Vec3 q = new Vec3(cx + Math.cos(a) * r, getY(), cz + Math.sin(a) * r);
                if (cabe(sl, q)) sitio = q;
            }
        }
        if (sitio != null) {
            teleportTo(sitio.x, sitio.y, sitio.z);
            if (p != null) {
                float y = (float) (Mth.atan2(p.getZ() - sitio.z, p.getX() - sitio.x) * Mth.RAD_TO_DEG) - 90f;
                setYRot(y);
                yBodyRot = yHeadRot = y;
            }
        }
    }

    /** Si cabe de pie en q (en su sala, con suelo y sin nada en medio). */
    private boolean cabe(ServerLevel sl, Vec3 q) {
        if (!Teatro.enSala(q.x, q.y + 0.5, q.z)) return false;
        BlockPos pie = BlockPos.containing(q.x, q.y + 0.1, q.z);
        if (sl.getBlockState(pie.below()).getCollisionShape(sl, pie.below()).isEmpty()) return false;
        return sl.noCollision(this, getBoundingBox().move(q.x - getX(), q.y - getY(), q.z - getZ()).inflate(-0.05));
    }

    /** La voltereta hacia atras: salta lejos de su presa y, mientras gira, no le entra nada. */
    private void voltereta(int t) {
        if (t == 3) {
            if (!ilusion()) volteretas++;
            playSound(DarkCraftMod.SFX_BUFON_PIRUETA.get(), ilusion() ? 0.6f : 1.1f, 1.25f);
            LivingEntity p = getTarget();
            Vec3 lejos = p == null ? Vec3.directionFromRotation(0, getYRot()).scale(-1)
                    : new Vec3(getX() - p.getX(), 0, getZ() - p.getZ());
            if (lejos.lengthSqr() < 1e-4) lejos = Vec3.directionFromRotation(0, getYRot()).scale(-1);
            lejos = lejos.normalize();
            vueloX = lejos.x * 0.62;
            vueloZ = lejos.z * 0.62;
            setDeltaMovement(vueloX, 0.5, vueloZ);
            hasImpulse = true;
            enVuelo = true;
            trasVoltereta = true;
            cuchilloEspera = 0;
        } else if (enVuelo && t > 3) {
            if (onGround() && t > 6) enVuelo = false;
            else setDeltaMovement(vueloX, getDeltaMovement().y, vueloZ);
        }
    }

    /**
     * Se parte en tres: el de verdad y dos ilusiones, a tres bloques a cada lado; el de verdad se queda en uno cualquiera de los
     * tres sitios (cambia de sitio con una de ellas en una nube de humo).
     */
    private void partirse() {
        if (!(level() instanceof ServerLevel sl) || ilusion()) return;
        partidas++;
        deshacerIlusiones(false);
        Vec3 lado = Vec3.directionFromRotation(0, getYRot() + 90);
        List<Vec3> sitios = new ArrayList<>();
        sitios.add(position());
        for (int s : new int[]{-1, 1}) {
            for (double r = 3.2; r >= 1.6; r -= 0.8) {
                Vec3 q = position().add(lado.x * r * s, 0, lado.z * r * s);
                if (cabe(sl, q)) {
                    sitios.add(q);
                    break;
                }
            }
        }
        java.util.Collections.shuffle(sitios, new java.util.Random(random.nextLong()));
        Vec3 aqui = sitios.get(0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        if (aqui.distanceToSqr(position()) > 0.01) teleportTo(aqui.x, aqui.y, aqui.z);
        for (int i = 1; i < sitios.size() && i <= COPIAS; i++) {
            Vec3 q = sitios.get(i);
            Bufon b = DarkCraftMod.BUFON.get().create(sl);
            if (b == null) continue;
            b.entityData.set(ILUSION, true);
            b.dueno = this;
            b.vidaIlusion = VIDA_ILUSION;
            var dano = b.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dano != null) dano.setBaseValue(getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5);
            b.moveTo(q.x, q.y, q.z, getYRot(), 0f);
            b.yBodyRot = b.yHeadRot = getYRot();
            b.efimero(VIDA_ILUSION);
            sl.addFreshEntity(b);
            if (getTarget() != null) b.setTarget(getTarget());
            b.empezar(BufonAnims.APARECE);
            ilusiones.add(b);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, q.x, q.y + 1.0, q.z, 16, 0.4, 0.6, 0.4, 0.02);
            sl.sendParticles(ParticleTypes.ENCHANT, q.x, q.y + 1.2, q.z, 30, 0.4, 0.8, 0.4, 0.4);
        }
        empezar(BufonAnims.APARECE);
    }

    /** Se deshacen las ilusiones que le queden (con su risa, si {@code risa}). */
    private void deshacerIlusiones(boolean risa) {
        for (Bufon b : ilusiones()) b.deshacer(risa);
        ilusiones.clear();
    }

    /** La ilusion se deshace: una nube de humo, confeti y (a veces) una risa. */
    private void deshacer(boolean risa) {
        if (!(level() instanceof ServerLevel sl) || isRemoved()) return;
        sl.sendParticles(ParticleTypes.POOF, getX(), getY() + 1.0, getZ(), 20, 0.3, 0.6, 0.3, 0.05);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 10, 0.3, 0.6, 0.3, 0.02);
        confeti(6);
        if (risa) sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_BUFON_RISA.get(), SoundSource.HOSTILE, 0.9f, 1.15f);
        if (dueno != null) dueno.deshechas++;
        discard();
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // una ilusion se deshace de un golpe (sin hacer nada a nadie)
        if (ilusion()) {
            if (level().isClientSide || s.is(net.minecraft.tags.DamageTypeTags.IS_FALL) || s.getEntity() instanceof Enemigo) return false;
            deshacer(true);
            return false;
        }
        // en la voltereta no le entra nada
        if (accion() == BufonAnims.VOLTERETA && accionT < VOLTERETA_INVULNERABLE && s.getEntity() instanceof LivingEntity) {
            esquivados++;
            return false;
        }
        boolean ok = super.hurt(s, cuanto);
        if (ok && !level().isClientSide && s.getEntity() instanceof LivingEntity) {
            golpesSeguidos = tickCount - ultimoGolpe < 30 ? golpesSeguidos + 1 : 1;
            ultimoGolpe = tickCount;
            // pegarle al de verdad deshace el truco
            if (!ilusiones().isEmpty()) deshacerIlusiones(true);
        }
        return ok;
    }

    // ------------------------------------------------------------------ lo que dice

    /** Dice la frase n (1..5) a quien este cerca (voz y subtitulo). */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl) || ilusion()) return;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 64 * 64) Net.to(p, new Net.ReyHabla(getId(), VOZ_BASE + n));
        hablaHasta = tickCount + Voces.ticks(VOZ_BASE + n);
    }

    private void hablaSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) habla(n);
    }

    @Override
    protected void alEmpezarPelea() {
        cuatro = false;
        cuchilloEspera = 30;
        malabarEspera = 80;
        humoEspera = 120;
        volteretaEspera = 40;
        piruetaEspera = 60;
        ilusionEspera = 0;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se inclina mientras lo dice (y se rie); hasta acabar no ataca
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaPelea = tickCount + dura;
            pausa = Math.max(pausa, dura);
        } else esperaPelea = 0;
    }

    @Override
    protected void alAcabarPelea() {
        deshacerIlusiones(false);
        humoPendiente = enVuelo = trasVoltereta = false;
        setInvisible(false);
        esperaPelea = 0;
    }

    /** A media vida (su frase 2): se parte en tres. */
    @Override
    protected void alSegundaMitad() {
        hablaHasta = 0;
        habla(2);
        ilusionEspera = 0;
        empezar(BufonAnims.ILUSION);
        pausa = Math.max(pausa, 4);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide && !ilusion()) ultimaPresa = t;
    }

    /** Solo repara en quien entra en su sala (a la vista, a 20 bloques); sus ilusiones no buscan a nadie. */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (ilusion() || !Teatro.enSala(p.getX(), p.getY(), p.getZ())) return false;
        return distanceToSqr(p) < VE * VE && hasLineOfSight(p);
    }

    /** A quien puede pegar: los jugadores (no en creativo) y su presa. */
    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (ilusion()) {
            if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
            return e == getTarget();
        }
        return super.objetivoValido(e);
    }

    @Override
    protected double correa() {
        return 24.0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            // la ilusion: alguna chispa de vez en cuando (lo que la delata, si se mira bien)
            if (ilusion() && isAlive() && tickCount % 7 == 0)
                level().addParticle(ParticleTypes.ENCHANT, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + 1.0 + random.nextDouble(),
                        getZ() + (random.nextDouble() - 0.5) * 0.6, 0, 0.05, 0);
            return;
        }
        if (!isAlive()) return;
        if (cuchilloEspera > 0) cuchilloEspera--;
        if (malabarEspera > 0) malabarEspera--;
        if (humoEspera > 0) humoEspera--;
        if (ilusionEspera > 0) ilusionEspera--;
        if (volteretaEspera > 0) volteretaEspera--;
        if (piruetaEspera > 0) piruetaEspera--;
        if (risaEspera > 0) risaEspera--;
        if (ilusion()) {
            // la ilusion dura lo suyo (y nada si el de verdad ya no esta)
            if (--vidaIlusion <= 0 || dueno == null || !dueno.isAlive() || dueno.isRemoved()) deshacer(false);
            return;
        }
        // cayo de la voltereta sin acabarla (la corto un aturdido)
        if (enVuelo && accion() != BufonAnims.VOLTERETA && onGround()) enVuelo = false;
        // sale del humo
        if (humoPendiente && accion() != BufonAnims.HUMO) {
            humoPendiente = false;
            setInvisible(false);
            apariciones++;
            empezar(BufonAnims.APARECE);
            pausa = Math.max(pausa, 2);
        }
        // sin nadie: actua solo (saltitos, alguna risa en la sala vacia)
        if (!enPelea() && accion() == 0 && --idle <= 0) {
            idle = 240 + random.nextInt(260);
            if (random.nextBoolean()) empezar(BufonAnims.RISA);
            else playSound(DarkCraftMod.SFX_BUFON_CASCABELES.get(), 0.8f, 1.0f);
        }
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
            if (accion() == 0) empezar(BufonAnims.RISA);
        }
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        if (!level().isClientSide && !ilusion()) deshacerIlusiones(false);
        setInvisible(false);
        super.die(s);
        if (level().isClientSide || ya || ilusion()) return;
        hablaHasta = 0;
        habla(5);
    }

    /** Al caer: se sienta, se tumba, dice su ultima frase; al acabar, su ultima risa, el teatro vacio aplaude y se deshace en confeti. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (ilusion()) {
            if (deathTime > 2) deshacer(false);
            return;
        }
        int fin = Math.max(BufonAnims.TICKS[BufonAnims.MORIR] + 30, Voces.ticks(VOZ_BASE + 5) + 70);
        if (level() instanceof ServerLevel sl) {
            if (deathTime == 24) playSound(DarkCraftMod.SFX_BUFON_CASCABELES.get(), 1.0f, 0.8f);
            if (deathTime == fin - 50) playSound(DarkCraftMod.SFX_BUFON_MUERTE.get(), 1.6f, 1.0f);
            if (deathTime == fin - 34)
                sl.playSound(null, getX(), getY() + 2, getZ(), DarkCraftMod.SFX_TEATRO_APLAUSO.get(), SoundSource.AMBIENT, 2.4f, 1.0f);
            if (deathTime > fin - 30 && deathTime % 3 == 0) confeti(3);
            if (deathTime == fin - 2) {
                confeti(40);
                sl.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.5, getZ(), 40, 0.6, 0.3, 0.6, 0.05);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.8, getZ(), 20, 0.5, 0.4, 0.5, 0.04);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    /** Mientras dice su primera frase se queda donde esta; despues, pegado a su presa. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaPelea ? 99.0 : 2.4;
    }

    @Override
    protected double alturaMaxima() {
        return 5.0;
    }

    @Override
    protected double prisa() {
        return segunda ? 1.35 : 1.25;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case BufonAnims.REVERENCIA -> 2;
            case BufonAnims.HUMO, BufonAnims.APARECE, BufonAnims.VOLTERETA -> 1;
            case BufonAnims.ILUSION -> 4;
            case BufonAnims.PIRUETA -> 10 + random.nextInt(8);
            case BufonAnims.MALABARES -> 10 + random.nextInt(6);
            default -> (segunda ? 4 : 7) + random.nextInt(8);
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

    /** Los cascabeles del gorro, a cada saltito. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        if (tickCount % 2 == 0) playSound(DarkCraftMod.SFX_BUFON_CASCABELES.get(), ilusion() ? 0.15f : 0.3f, 1.1f + random.nextFloat() * 0.2f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return ilusion() ? null : DarkCraftMod.SFX_BUFON_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean shouldBeSaved() {
        return !ilusion() && super.shouldBeSaved();
    }

    @Override
    protected boolean shouldDropLoot() {
        return !ilusion() && super.shouldDropLoot();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (ilusion()) return;
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.PASO).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s en %.1f,%.1f,%.1f; tajos %d, piruetas %d, cuchillos %d, malabares %d (bolas %d),"
                        + " humos %d (ciega a %d, sale %d), volteretas %d (esquiva %d), se parte %d (ilusiones %d, deshechas %d), risas %d%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", getX(), getY(), getZ(), tajos, piruetas, cuchillos, malabares, bolas,
                humos, cegados, apariciones, volteretas, esquivados, partidas, ilusiones().size(), deshechas, risas, isInvisible() ? " (en el humo)" : "");
    }
}
