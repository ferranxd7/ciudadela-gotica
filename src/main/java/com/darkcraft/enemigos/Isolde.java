package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Homenaje;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * La reina Isolde (parte 7ab): lo que la Sombra devolvio con la cara de la reina de Valdrenn, en el salon del trono de la casa de
 * la Sombra (lo alto de la torre del homenaje de la ciudadela). Hereda de {@link Guardian} (su barra, su musica, la pelea).
 * <ul>
 * <li>Espera <b>sentada en su trono</b> tarareando ({@code REZA}); quien entra en el salon (a la vista) la despierta: dice su frase 1
 * mientras se levanta despacio ("Corvin...? ¿Eres tu el que esta en la puerta?") y, al acabarla, grita y empieza la pelea. Mientras
 * pelea, el salon queda sellado (lo hace {@link com.darkcraft.morvain.CasaSombra}).</li>
 * <li>Las <b>garras</b> (dos zarpazos cruzados), el <b>velo</b> (gira con los brazos abiertos: todo alrededor), el <b>abrazo</b> (se
 * lanza de lejos con los brazos abiertos; a quien coge lo <b>aprieta</b>: no puede moverse, le chupa la vida dos veces y lo suelta de
 * un empujon), la <b>nana</b> (canta: a quien la este <b>mirando</b> al acabar se le cierran los ojos —no se mueve, no ve, no pega—;
 * hay que darle la espalda) y el <b>desvanecerse</b> (se apaga y aparece detras de su presa, y le echa las garras).</li>
 * <li>A <b>media vida le arde el velo</b> (frase 3: "¿Quieres verme la cara?") y se le ve la cara (variante 1); desde entonces el
 * velo quema, llora <b>lagrimas negras</b> (marcas bajo los pies: a los segundos cae la lluvia, que pudre) y <b>llama a su corte</b>
 * (los espectros de los cortesanos, que se deshacen al acabar; frase 2 la primera vez: "Arrodillate ante tu reina").</li>
 * <li>Si cae su presa, la frase 4 ("Calla... la Sombra te despertara otra vez"). Al caer, de rodillas en el aire, su ultima frase
 * (6: "Corvin... oigo otra vez la fuente... Nunca fui yo") y se deshace subiendo. Lo demas (cartel, ecos, hazana, bestiario) lo
 * reparte {@code valdrenn/Guardianes}; la reja de la escalera de la azotea se abre ({@code CasaSombra}).</li>
 * </ul>
 */
public class Isolde extends Guardian {
    public static final int VOZ_BASE = Voces.BASE_ISOLDE;
    public static final int ECOS = 30000;
    /** Lo que espera entre un abrazo y el siguiente, una nana, desvanecerse, las lagrimas y llamar a la corte (ticks). */
    public static final int ESPERA_ABRAZO = 20 * 9, ESPERA_NANA = 20 * 17, ESPERA_DESVANECE = 20 * 7, ESPERA_LAGRIMAS = 20 * 11,
            ESPERA_CORTE = 20 * 32;
    /** El abrazo: desde que distancia se lanza, de que tick a que tick cruza y lo que dura el aprieto. */
    public static final double ABRAZO_MIN = 4.5, ABRAZO_MAX = 12.0;
    public static final int ABRAZO_DESDE = 10, ABRAZO_HASTA = 16;
    /** La nana: lo que hay que haberla mirado (de los ticks que canta) para dormirse, y lo que dura el sueno. */
    public static final float NANA_MIRAR = 0.45f;
    public static final int SUENO = 70;
    /** Desvanecerse: cuando se apaga, cuando aparece y lo que se pone detras. */
    public static final int APAGA = 6, APARECE = 12;
    public static final double DETRAS = 2.4;
    /** Las lagrimas: cuando se marcan, cuando caen, su radio. */
    public static final int MARCA_T = 4, CAEN_T = 28;
    public static final double R_LAGRIMA = 2.2;
    static final DustParticleOptions NEGRO = new DustParticleOptions(new Vector3f(0.05f, 0.02f, 0.06f), 1.6f),
            VIOLETA = new DustParticleOptions(new Vector3f(0.45f, 0.18f, 0.6f), 1.2f),
            ROJO = new DustParticleOptions(new Vector3f(0.75f, 0.06f, 0.08f), 1.3f);

    private int esperaPelea, hablaHasta, abrazoEspera, nanaEspera, desvaneceEspera, lagrimasEspera, corteEspera;
    private boolean dijoNana, dijoCorte, llamaCorte;
    private long ultimaEntrada = -100000;
    private LivingEntity ultimaPresa;
    private Vec3 abrazoDestino;
    private LivingEntity agarrado;
    private int encadena;
    private final java.util.Map<java.util.UUID, Integer> miraron = new java.util.HashMap<>();
    private final List<Vec3> marcas = new ArrayList<>();
    private final List<Espectro> corte = new ArrayList<>();
    private Vec3 trono;
    /** Para la prueba: no elige ataques sola (solo hace lo que se le manda). */
    public boolean quieta;

    /** Para las pruebas: cuantas veces ha hecho cada cosa y a cuantos ha pillado. */
    public static int GARRAS, VELOS, VELOS_QUEMAN, ABRAZOS, AGARRA, APRIETOS, NANAS, DORMIDOS, DESPIERTOS, DESVANECE, DETRAS_SALE, LAGRIMAS,
            LAGRIMAS_PILLAN, CORTE, CORTESANOS, VELO_ARDE, CAE;

    public Isolde(EntityType<? extends Isolde> t, Level l) {
        super(t, l, "isolde", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1800.0).add(Attributes.ATTACK_DAMAGE, 13.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.92)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    public String tipo() {
        return "isolde";
    }

    @Override
    public int musica() {
        return 46;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "isolde";
    }

    @Override
    protected int[] ticks() {
        return IsoldeAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return IsoldeAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return IsoldeAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return IsoldeAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return IsoldeAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return IsoldeAnims.LEVANTARSE;
    }

    /** Si se le ve ya la cara (le ardio el velo). */
    public boolean sinVelo() {
        return variante() == 1;
    }

    // ------------------------------------------------------------------ lo que dice

    /** Dice la frase n (1..6) a quien este cerca (voz y subtitulo). */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 64 * 64) Net.to(p, new Net.ReyHabla(getId(), VOZ_BASE + n));
        hablaHasta = tickCount + Voces.ticks(VOZ_BASE + n);
    }

    private void hablaSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) habla(n);
    }

    public boolean hablando() {
        return tickCount < hablaHasta;
    }

    @Override
    protected void alEmpezarPelea() {
        abrazoEspera = 60;
        nanaEspera = 160;
        desvaneceEspera = 100;
        lagrimasEspera = 40;
        corteEspera = 0;
        dijoNana = dijoCorte = llamaCorte = false;
        encadena = 0;
        agarrado = null;
        if (trono == null && hogar() != null) trono = Vec3.atBottomCenterOf(hogar());
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se levanta despacio mientras lo dice; hasta acabar no ataca
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaPelea = tickCount + dura;
            pausa = Math.max(pausa, dura);
        } else esperaPelea = 0;
    }

    @Override
    protected boolean puedeRugir() {
        return tickCount >= esperaPelea;
    }

    @Override
    protected void alAcabarPelea() {
        soltar(false);
        deshacerCorte();
        marcas.clear();
        esperaPelea = 0;
        // vuelve a ser la de antes (con su velo)
        variante(0);
    }

    /** A media vida (su frase 3): le arde el velo. */
    @Override
    protected void alSegundaMitad() {
        hablaHasta = 0;
        habla(3);
        soltar(true);
        empezar(IsoldeAnims.VELO_ARDE);
        VELO_ARDE++;
        corteEspera = 0;
        lagrimasEspera = 60;
        pausa = Math.max(pausa, 4);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Solo repara en quien entra en el salon del trono (a la vista). */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (!Homenaje.enSalaTrono(p.getX(), p.getY(), p.getZ())) return false;
        return distanceToSqr(p) < 44 * 44 && hasLineOfSight(p);
    }

    @Override
    protected double correa() {
        return 34.0;
    }

    /** Mientras dice su primera frase se queda donde esta; despues, pegada a su presa. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaPelea ? 99.0 : 2.2;
    }

    @Override
    protected double alturaMaxima() {
        return 4.0;
    }

    @Override
    protected double prisa() {
        return sinVelo() ? 1.3 : 1.15;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case IsoldeAnims.GRITO, IsoldeAnims.LEVANTARSE -> 4;
            case IsoldeAnims.DESVANECE, IsoldeAnims.ABRAZO -> 0;
            case IsoldeAnims.VELO_ARDE -> 6;
            case IsoldeAnims.NANA, IsoldeAnims.LAGRIMAS -> 8 + random.nextInt(6);
            default -> (sinVelo() ? 5 : 9) + random.nextInt(9);
        };
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (tickCount < esperaPelea || quieta) return 0;
        // a media vida: llamar a su corte, las lagrimas
        if (sinVelo()) {
            if (corteEspera <= 0 && random.nextFloat() < 0.5f) {
                llamaCorte = true;
                return IsoldeAnims.GRITO;
            }
            if (lagrimasEspera <= 0 && d < 24 && random.nextFloat() < 0.45f) return IsoldeAnims.LAGRIMAS;
        }
        if (nanaEspera <= 0 && d < 22 && random.nextFloat() < 0.35f) return IsoldeAnims.NANA;
        if (abrazoEspera <= 0 && d > ABRAZO_MIN && d < ABRAZO_MAX) return IsoldeAnims.ABRAZO;
        if (desvaneceEspera <= 0 && (d > 7 || random.nextFloat() < 0.18f)) return IsoldeAnims.DESVANECE;
        if (d < 3.4) return random.nextFloat() < (sinVelo() ? 0.45f : 0.6f) ? IsoldeAnims.GARRAS : IsoldeAnims.VELO;
        if (d < 4.2) return IsoldeAnims.VELO;
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case IsoldeAnims.GARRAS -> new Golpe(3.5f, 120f, 1.0f, 0.4f);
            case IsoldeAnims.VELO -> new Golpe(4.3f, 360f, 1.1f, 1.3f);
            case IsoldeAnims.ABRAZO -> new Golpe(2.6f, 110f, 0.45f, 0.0f);
            default -> null;
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == IsoldeAnims.GARRAS) GARRAS++;
        if (accion == IsoldeAnims.VELO) {
            VELOS++;
            if (sinVelo()) {
                le.setSecondsOnFire(4);
                VELOS_QUEMAN++;
            }
        }
        if (accion == IsoldeAnims.ABRAZO && agarrado == null) {
            agarrado = le;
            AGARRA++;
        }
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case IsoldeAnims.GARRAS -> {
                if (t == 9 || t == 15) {
                    empujar(0.35);
                    playSound(DarkCraftMod.SFX_ISOLDE_GARRAS.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
                }
            }
            case IsoldeAnims.VELO -> {
                if (t == 13) playSound(DarkCraftMod.SFX_ISOLDE_VELO.get(), 1.3f, sinVelo() ? 0.85f : 1.0f);
                if (t >= 12 && t <= 22 && level() instanceof ServerLevel sl) {
                    double a = (t - 12) * Math.PI / 5;
                    for (int k = 0; k < 3; k++) {
                        double b = a + k * Math.PI * 2 / 3;
                        double x = getX() + Math.cos(b) * 2.6, z = getZ() + Math.sin(b) * 2.6;
                        sl.sendParticles(sinVelo() ? ParticleTypes.FLAME : NEGRO, x, getY() + 1.2, z, 2, 0.2, 0.3, 0.2, 0.01);
                    }
                }
            }
            case IsoldeAnims.ABRAZO -> abrazo(t);
            case IsoldeAnims.APRIETA -> aprieta(t);
            case IsoldeAnims.NANA -> nana(t);
            case IsoldeAnims.DESVANECE -> desvanece(t);
            case IsoldeAnims.LAGRIMAS -> lagrimas(t);
            case IsoldeAnims.GRITO -> grito(t);
            case IsoldeAnims.VELO_ARDE -> veloArde(t);
            default -> {
            }
        }
    }

    /** El abrazo: abre los brazos, cruza hasta su presa (se para un paso antes) y los cierra (el golpe: si pilla a alguien, lo coge). */
    private void abrazo(int t) {
        LivingEntity p = getTarget();
        if (t == 2) {
            abrazoEspera = ESPERA_ABRAZO;
            ABRAZOS++;
        }
        if (t == 8) playSound(DarkCraftMod.SFX_ISOLDE_ABRAZO.get(), 1.3f, 1.0f);
        if (t == ABRAZO_DESDE && p != null) {
            Vec3 a = p.position().subtract(position());
            double l = Math.sqrt(a.x * a.x + a.z * a.z);
            abrazoDestino = l > 0.1 ? position().add(a.x / l * Math.max(0, l - 1.1), 0, a.z / l * Math.max(0, l - 1.1)) : position();
        }
        if (t >= ABRAZO_DESDE && t <= ABRAZO_HASTA && abrazoDestino != null) {
            Vec3 falta = abrazoDestino.subtract(position());
            double l = Math.sqrt(falta.x * falta.x + falta.z * falta.z);
            double v = Math.min(1.4, l / (ABRAZO_HASTA - t + 1));
            if (l > 0.05) setDeltaMovement(falta.x / l * v, getDeltaMovement().y, falta.z / l * v);
            hasImpulse = true;
            if (p != null) girarHacia(p, 30f);
            if (level() instanceof ServerLevel sl) sl.sendParticles(NEGRO, getX(), getY() + 1.4, getZ(), 4, 0.4, 0.8, 0.4, 0.01);
        }
        if (t == ABRAZO_HASTA + 1) setDeltaMovement(0, getDeltaMovement().y, 0);
    }

    /** Aprieta a quien ha cogido: lo tiene delante, sin poder moverse; le chupa la vida dos veces y lo suelta de un empujon. */
    private void aprieta(int t) {
        if (agarrado == null || !agarrado.isAlive() || agarrado.isSpectator()) {
            agarrado = null;
            return;
        }
        Vec3 m = Vec3.directionFromRotation(0, getYRot());
        Vec3 sitio = position().add(m.x * 1.15, 0.1, m.z * 1.15);
        Vec3 tira = sitio.subtract(agarrado.position());
        agarrado.setDeltaMovement(tira.x * 0.6, Math.min(0.05, tira.y * 0.4), tira.z * 0.6);
        agarrado.hurtMarked = true;
        agarrado.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 6, false, false));
        if (t == 8 || t == 16) {
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.42f;
            if (agarrado.hurt(damageSources().mobAttack(this), dano)) {
                APRIETOS++;
                heal(dano * 0.6f);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, agarrado.getX(), agarrado.getY() + 1.2, agarrado.getZ(), 4, 0.3, 0.3, 0.3, 0.1);
                    sl.sendParticles(ROJO, agarrado.getX(), agarrado.getY() + 1.2, agarrado.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
                }
            }
        }
        if (t == 26) soltar(true);
    }

    /** Suelta a quien tenia cogido (con un empujon si {@code empuja}). */
    private void soltar(boolean empuja) {
        if (agarrado == null) return;
        if (empuja && agarrado.isAlive()) {
            Vec3 m = Vec3.directionFromRotation(0, getYRot());
            agarrado.knockback(1.6, -m.x, -m.z);
            agarrado.setDeltaMovement(agarrado.getDeltaMovement().add(0, 0.35, 0));
            agarrado.hurtMarked = true;
            agarrado.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        agarrado = null;
    }

    /**
     * La nana: canta (frase 5 la primera vez; luego el canto) y a quien la este mirando (la vista hacia su cara, a 30 bloques, sin
     * nada en medio) mas de la mitad del tiempo que canta, al acabar se le cierran los ojos: no se mueve, no ve, no pega.
     */
    private void nana(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 1) {
            nanaEspera = ESPERA_NANA;
            NANAS++;
            miraron.clear();
            if (!dijoNana) {
                dijoNana = true;
                hablaHasta = 0;
                habla(5);
            } else playSound(DarkCraftMod.SFX_ISOLDE_CANTO.get(), 1.8f, 1.0f);
        }
        if (t >= 8 && t < 56) {
            Vec3 cara = new Vec3(getX(), getY() + getBbHeight() * 0.88, getZ());
            if (t % 4 == 0) {
                sl.sendParticles(ParticleTypes.NOTE, cara.x, cara.y + 0.6, cara.z, 1, 0.5, 0.2, 0.5, 1.0);
                double r = 1.0 + (t - 8) * 0.25;
                for (int k = 0; k < 10; k++) {
                    double a = k * Math.PI * 2 / 10 + t * 0.1;
                    sl.sendParticles(VIOLETA, getX() + Math.cos(a) * r, getY() + 0.3, getZ() + Math.sin(a) * r, 1, 0.05, 0.05, 0.05, 0.0);
                }
            }
            for (ServerPlayer p : sl.players()) {
                if (!objetivoValido(p) || p.distanceToSqr(this) > 30 * 30) continue;
                if (laMira(p)) {
                    miraron.merge(p.getUUID(), 1, Integer::sum);
                    Vec3 ojo = p.getEyePosition(), mira = p.getViewVector(1f);
                    if (t % 6 == 0) sl.sendParticles(VIOLETA, ojo.x + mira.x * 0.6, ojo.y, ojo.z + mira.z * 0.6, 2, 0.1, 0.1, 0.1, 0.0);
                }
            }
        }
        if (t == 56) {
            int canta = 56 - 8;
            for (ServerPlayer p : sl.players()) {
                if (!objetivoValido(p) || p.distanceToSqr(this) > 30 * 30) continue;
                int vio = miraron.getOrDefault(p.getUUID(), 0);
                if (vio >= canta * NANA_MIRAR) dormir(p);
                else DESPIERTOS++;
            }
            miraron.clear();
        }
    }

    /** Si ese jugador la esta mirando a la cara (la vista hacia ella, a unos 44 grados como mucho, sin nada en medio). */
    public boolean laMira(Player p) {
        Vec3 cara = new Vec3(getX(), getY() + getBbHeight() * 0.88, getZ());
        Vec3 ojo = p.getEyePosition(), mira = p.getViewVector(1f), hacia = cara.subtract(ojo).normalize();
        return mira.dot(hacia) > 0.72 && p.hasLineOfSight(this);
    }

    /** Se le cierran los ojos: no se mueve, no ve, no pega (un momento). */
    public void dormir(ServerPlayer p) {
        DORMIDOS++;
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SUENO, 6, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, SUENO - 10, 0, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SUENO, 3, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, SUENO, 4, false, false));
        Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.isolde.dormido", 0));
        if (level() instanceof ServerLevel sl) sl.sendParticles(VIOLETA, p.getX(), p.getY() + 1.6, p.getZ(), 16, 0.3, 0.3, 0.3, 0.02);
    }

    /** Desvanecerse: se apaga (no se le puede pegar), aparece detras de su presa y le echa las garras. */
    private void desvanece(int t) {
        LivingEntity p = getTarget();
        if (t == 1) {
            desvaneceEspera = ESPERA_DESVANECE;
            DESVANECE++;
        }
        if (t == APAGA) {
            playSound(DarkCraftMod.SFX_ISOLDE_DESVANECE.get(), 1.3f, 1.0f);
            if (level() instanceof ServerLevel sl) sl.sendParticles(NEGRO, getX(), getY() + 1.5, getZ(), 30, 0.5, 1.0, 0.5, 0.02);
        }
        if (t == APARECE && p != null) {
            Vec3 m = p.getViewVector(1f);
            Vec3 h = new Vec3(m.x, 0, m.z);
            if (h.lengthSqr() < 1e-4) h = new Vec3(0, 0, 1);
            h = h.normalize();
            Vec3 detras = p.position().subtract(h.scale(DETRAS));
            Vec3 donde = libre(detras) ? detras : libre(p.position().add(h.scale(DETRAS))) ? p.position().add(h.scale(DETRAS)) : null;
            if (donde != null) {
                moveTo(donde.x, donde.y, donde.z, getYRot(), 0);
                girarHacia(p, 360f);
                DETRAS_SALE++;
            }
            if (level() instanceof ServerLevel sl) sl.sendParticles(NEGRO, getX(), getY() + 1.5, getZ(), 30, 0.5, 1.0, 0.5, 0.02);
            encadena = IsoldeAnims.GARRAS;
        }
    }

    /** Si ahi cabe (sin bloques que choquen) y hay suelo debajo, dentro del salon. */
    private boolean libre(Vec3 v) {
        if (!Homenaje.enSalaTrono(v.x, v.y + 0.2, v.z)) return false;
        AABB c = getType().getDimensions().makeBoundingBox(v.x, v.y, v.z);
        BlockPos bajo = BlockPos.containing(v.x, v.y - 0.5, v.z);
        return level().noCollision(this, c) && !level().getBlockState(bajo).getCollisionShape(level(), bajo).isEmpty();
    }

    /** Las lagrimas: marca el suelo bajo cada uno (y por donde va); a los segundos cae la lluvia negra, que pudre. */
    private void lagrimas(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 1) {
            lagrimasEspera = ESPERA_LAGRIMAS;
            LAGRIMAS++;
            // llora: el lamento, mas grave
            playSound(DarkCraftMod.SFX_ISOLDE_CANTO.get(), 1.2f, 0.72f);
        }
        if (t == MARCA_T) {
            marcas.clear();
            for (ServerPlayer p : sl.players()) {
                if (!objetivoValido(p) || p.distanceToSqr(this) > 26 * 26 || !Homenaje.enSalaTrono(p.getX(), p.getY(), p.getZ())) continue;
                marcas.add(new Vec3(p.getX(), Math.floor(p.getY() + 0.01), p.getZ()));
                Vec3 v = p.getDeltaMovement();
                if (v.x * v.x + v.z * v.z > 0.004) marcas.add(new Vec3(p.getX() + v.x * 12, Math.floor(p.getY() + 0.01), p.getZ() + v.z * 12));
            }
            LivingEntity q = getTarget();
            if (q != null && !(q instanceof Player)) marcas.add(new Vec3(q.getX(), Math.floor(q.getY() + 0.01), q.getZ()));
        }
        if (t > MARCA_T && t < CAEN_T) {
            double r = R_LAGRIMA * (1.0 - (t - MARCA_T) / (double) (CAEN_T - MARCA_T) * 0.6);
            for (Vec3 m : marcas) {
                if (t % 2 == 0)
                    for (int k = 0; k < 8; k++) {
                        double a = k * Math.PI / 4 + t * 0.15;
                        sl.sendParticles(NEGRO, m.x + Math.cos(a) * r, m.y + 0.1, m.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
                    }
                if (t > CAEN_T - 12) sl.sendParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR, m.x, m.y + 5.0, m.z, 3, R_LAGRIMA * 0.5, 0.2, R_LAGRIMA * 0.5, 0);
            }
        }
        if (t == CAEN_T) {
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75f;
            for (Vec3 m : marcas) {
                sl.sendParticles(ParticleTypes.SQUID_INK, m.x, m.y + 0.4, m.z, 24, R_LAGRIMA * 0.5, 0.3, R_LAGRIMA * 0.5, 0.05);
                sl.playSound(null, m.x, m.y, m.z, DarkCraftMod.SFX_ISOLDE_LAGRIMAS.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
                for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(m.x - 3, m.y - 1, m.z - 3, m.x + 3, m.y + 3, m.z + 3),
                        e -> e != this && e.isAlive() && objetivoValido(e))) {
                    if (le.distanceToSqr(m.x, le.getY(), m.z) > R_LAGRIMA * R_LAGRIMA) continue;
                    if (le.hurt(damageSources().indirectMagic(this, this), dano)) {
                        LAGRIMAS_PILLAN++;
                        le.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                    }
                }
            }
            marcas.clear();
        }
    }

    /** El grito: al empezar la pelea y, sin velo, el que llama a su corte (los espectros de los cortesanos, que se deshacen al acabar). */
    private void grito(int t) {
        if (t == 2) playSound(DarkCraftMod.SFX_ISOLDE_GRITO.get(), 2.0f, 0.95f + random.nextFloat() * 0.1f);
        if (t == 14 && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.4, getZ(), 20, 0.4, 0.5, 0.4, 0.05);
            if (llamaCorte) {
                llamaCorte = false;
                corteEspera = ESPERA_CORTE;
                if (!dijoCorte) {
                    dijoCorte = true;
                    hablaHasta = 0;
                    habla(2);
                }
                llamarCorte(sl);
            }
        }
    }

    private void llamarCorte(ServerLevel sl) {
        CORTE++;
        corte.removeIf(e -> !e.isAlive() || e.isRemoved());
        int cerca = 0;
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 48 * 48) cerca++;
        int n = Math.min(4, 2 + Math.max(0, cerca - 1)) - corte.size();
        LivingEntity presa = getTarget();
        for (int k = 0; k < n; k++) {
            Espectro e = DarkCraftMod.ESPECTRO.get().create(sl);
            if (e == null) continue;
            Vec3 donde = null;
            for (int intento = 0; intento < 12 && donde == null; intento++) {
                double a = random.nextDouble() * Math.PI * 2, r = 4.0 + random.nextDouble() * 4.0;
                Vec3 v = new Vec3(getX() + Math.cos(a) * r, getY(), getZ() + Math.sin(a) * r);
                if (libre(v)) donde = v;
            }
            if (donde == null) donde = position();
            e.moveTo(donde.x, donde.y, donde.z, random.nextFloat() * 360f, 0);
            e.variante(k % 2 == 0 ? Espectro.ESTOQUE : Espectro.GARRAS);
            e.efimero(20 * 12);
            sl.addFreshEntity(e);
            if (presa != null) e.setTarget(presa);
            sl.sendParticles(ParticleTypes.SOUL, donde.x, donde.y + 1.0, donde.z, 14, 0.3, 0.6, 0.3, 0.03);
            corte.add(e);
            CORTESANOS++;
        }
    }

    private void deshacerCorte() {
        for (Espectro e : corte) if (e.isAlive() && !e.isRemoved()) e.disolver();
        corte.clear();
    }

    /** A media vida: se lleva las manos a la cara y el velo arde (desde el tick 26 se le ve la cara). */
    private void veloArde(int t) {
        if (t == 6) playSound(DarkCraftMod.SFX_ISOLDE_VELO_ARDE.get(), 1.8f, 1.0f);
        if (t >= 6 && t <= 34 && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + getBbHeight() * 0.85, getZ(), 4, 0.3, 0.3, 0.3, 0.02);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() * 0.95, getZ(), 2, 0.2, 0.2, 0.2, 0.01);
        }
        if (t == 26) variante(1);
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            // sin velo, las ascuas del bajo del vestido; y siempre, algo de bruma negra a sus pies
            if (isAlive() && tickCount % 4 == 0) {
                level().addParticle(ParticleTypes.SMOKE, getX() + (random.nextDouble() - 0.5) * 0.8, getY() + 0.2, getZ() + (random.nextDouble() - 0.5) * 0.8, 0, 0.01, 0);
                if (sinVelo() && random.nextInt(3) == 0)
                    level().addParticle(ParticleTypes.SMALL_FLAME, getX() + (random.nextDouble() - 0.5) * 0.9, getY() + 0.3, getZ() + (random.nextDouble() - 0.5) * 0.9, 0, 0.02, 0);
            }
            return;
        }
        if (!isAlive()) return;
        if (abrazoEspera > 0) abrazoEspera--;
        if (nanaEspera > 0) nanaEspera--;
        if (desvaneceEspera > 0) desvaneceEspera--;
        if (lagrimasEspera > 0) lagrimasEspera--;
        if (corteEspera > 0) corteEspera--;
        // tras el abrazo, si cogio a alguien, lo aprieta; tras desvanecerse, las garras
        if (accion() == 0) {
            if (agarrado != null) {
                empezar(IsoldeAnims.APRIETA);
            } else if (encadena != 0) {
                int a = encadena;
                encadena = 0;
                if (getTarget() != null) girarHacia(getTarget(), 360f);
                empezar(a);
            }
        } else if (accion() != IsoldeAnims.ABRAZO && accion() != IsoldeAnims.APRIETA && agarrado != null) soltar(false);
        // ha caido aquel con quien peleaba
        if (enPelea() && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(4);
        }
        // nunca sale del salon del trono (si la empujan por el hueco de la escalera, vuelve a su trono)
        if (tickCount % 20 == 0 && !Homenaje.enSalaTrono(getX(), getY() + 0.5, getZ()) && hogar() != null) {
            BlockPos h = hogar();
            moveTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5, getYRot(), 0);
        }
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // apagada (desvanecida) no se le puede pegar
        int a = accion();
        if (a == IsoldeAnims.DESVANECE && accionTicks() >= APAGA && accionTicks() < APARECE + 2 && !s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))
            return false;
        return super.hurt(s, cuanto);
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        if (!level().isClientSide) {
            soltar(false);
            deshacerCorte();
        }
        super.die(s);
        if (level().isClientSide || ya) return;
        CAE++;
        hablaHasta = 0;
        habla(6);
    }

    /** Al caer: de rodillas en el aire, su ultima frase y se deshace subiendo (lo que dura la frase y un poco mas). */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = fin();
        if (level() instanceof ServerLevel sl) {
            if (deathTime == fin - 40) playSound(DarkCraftMod.SFX_ISOLDE_MUERTE.get(), 1.8f, 1.0f);
            if (deathTime > fin - 40 && deathTime % 2 == 0) {
                sl.sendParticles(NEGRO, getX(), getY() + 1.6, getZ(), 6, 0.4, 0.8, 0.4, 0.03);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.0, getZ(), 1, 0.3, 0.5, 0.3, 0.03);
            }
            if (deathTime == fin - 2) {
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.5, getZ(), 40, 0.6, 1.0, 0.6, 0.05);
                sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.8, getZ(), 20, 0.4, 0.8, 0.4, 0.05);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    /** Lo que tarda en irse al caer (lo que dura su ultima frase y la animacion). */
    public int fin() {
        return Math.max(IsoldeAnims.TICKS[IsoldeAnims.MORIR] + 16, Voces.ticks(VOZ_BASE + 6) + 50);
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ISOLDE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        // flota: no hace ruido al moverse
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.SANGRE).get()));
    }

    // ------------------------------------------------------------------ para las pruebas y los comandos

    /** Pasa ya a media vida (le arde el velo). */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
    }

    /** Que caiga ya. */
    public void vencer() {
        if (level().isClientSide || !isAlive()) return;
        kill();
    }

    /** Hace esa accion ya (mirando a su presa), sin esperar a nada. */
    public void probar(int accion) {
        if (level().isClientSide || !isAlive()) return;
        esperaPelea = 0;
        if (accion == IsoldeAnims.GRITO) llamaCorte = true;
        if (getTarget() != null) girarHacia(getTarget(), 360f);
        empezar(accion);
    }

    public LivingEntity agarrado() {
        return agarrado;
    }

    public List<Espectro> corte() {
        corte.removeIf(e -> !e.isAlive() || e.isRemoved());
        return corte;
    }

    /** Como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s%s en %.1f,%.1f,%.1f; garras %d, velos %d (queman %d), abrazos %d (coge %d,"
                        + " aprieta %d), nanas %d (duerme %d, no %d), desvanece %d (detras %d), lagrimas %d (pillan %d), corte %d (%d), velo arde %d",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : postura() == REZA ? "en su trono" : "de pie", sinVelo() ? " sin velo" : "",
                getX(), getY(), getZ(), GARRAS, VELOS, VELOS_QUEMAN, ABRAZOS, AGARRA, APRIETOS, NANAS, DORMIDOS, DESPIERTOS, DESVANECE, DETRAS_SALE,
                LAGRIMAS, LAGRIMAS_PILLAN, CORTE, CORTESANOS, VELO_ARDE);
    }

    @Override
    protected int accionDespertar(int postura) {
        return IsoldeAnims.LEVANTARSE;
    }
}
