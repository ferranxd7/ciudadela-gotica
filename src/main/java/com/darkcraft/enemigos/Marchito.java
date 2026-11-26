package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * El marchito de Morvain (parte 7e): lo que la Sombra dejo de la gente de las Tierras Marchitas. Alto, lento y de golpe
 * largo; aguanta mas que un hueco. Tres:
 * <ul>
 * <li>la <b>guadana</b>: siega ancha a ras de suelo (se salta o se rueda) y tajo de arriba abajo que llega lejos;</li>
 * <li>el <b>farol</b>: dos cuchilladas rapidas y el farol de almas, que estrella delante y prende fuego de almas;</li>
 * <li>la <b>campana</b> (el peregrino): baculazo y, si ve a alguien de lejos, toca la campana: cada campanada llama a
 * los enemigos de alrededor (hasta {@value #LLAMADA} bloques) a por quien ha visto, y el que la oye de cerca se queda
 * un momento lento.</li>
 * </ul>
 * Esperan de pie, de rodillas ante las tumbas ({@code REZA}) o tirados entre los muertos ({@code TUMBADO}).
 */
public class Marchito extends Enemigo {
    public static final int GUADANA = 0, FAROL = 1, CAMPANA = 2;
    /** Hasta donde se oye la campana (bloques) y cada cuanto puede tocarla (ticks). */
    public static final int LLAMADA = 26, ESPERA_CAMPANA = 20 * 22;

    private int ultimaCampana = -100000;
    /** El fuego de almas que deja el farol al estrellarse: donde y hasta que tick arde (solo en el servidor). */
    private double fuegoX, fuegoY, fuegoZ;
    private int fuegoHasta = -1;
    /** Lo que dura y lo ancho del fuego del farol. */
    public static final int FUEGO_TICKS = 50;
    public static final double FUEGO_R = 1.6;

    public Marchito(EntityType<? extends Marchito> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.ATTACK_DAMAGE, 5.5)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case FAROL -> "marchito_farol";
            case CAMPANA -> "marchito_campana";
            default -> "marchito_guadana";
        };
    }

    @Override
    protected int[] ticks() {
        return MarchitoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return MarchitoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return MarchitoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return MarchitoAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? MarchitoAnims.ALZARSE : MarchitoAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        return switch (variante()) {
            case FAROL -> d < 2.3 ? (random.nextFloat() < 0.6f ? MarchitoAnims.FAROL_TAJOS : MarchitoAnims.FAROL_GOLPE)
                    : d < 3.2 && random.nextFloat() < 0.35f ? MarchitoAnims.FAROL_GOLPE : 0;
            case CAMPANA -> {
                if (d > 4.5 && d < 20 && tickCount - ultimaCampana > ESPERA_CAMPANA) yield MarchitoAnims.CAMPANA_TOCAR;
                yield d < 3.0 ? MarchitoAnims.CAMPANA_GOLPE : 0;
            }
            default -> d < 3.8 ? (d > 2.6 || random.nextFloat() < 0.55f ? MarchitoAnims.GUADANA_TAJO : MarchitoAnims.GUADANA_SIEGA) : 0;
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            // la siega va a ras de suelo y abarca casi todo lo de delante: se salta o se rueda
            case MarchitoAnims.GUADANA_SIEGA -> new Golpe(3.4f, 170f, 1.1f, 0.6f);
            case MarchitoAnims.GUADANA_TAJO -> new Golpe(3.8f, 40f, 1.45f, 0.4f);
            case MarchitoAnims.FAROL_TAJOS -> new Golpe(2.3f, 110f, 0.75f, 0f);
            case MarchitoAnims.FAROL_GOLPE -> new Golpe(2.9f, 60f, 1.2f, 0.5f);
            case MarchitoAnims.CAMPANA_GOLPE -> new Golpe(3.0f, 50f, 1.15f, 0.6f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case MarchitoAnims.GUADANA_SIEGA -> {
                if (t == 19) empujar(0.5);
            }
            case MarchitoAnims.GUADANA_TAJO -> {
                if (t == 18) empujar(0.7);
            }
            case MarchitoAnims.FAROL_TAJOS -> {
                if (t == 7 || t == 13) empujar(0.4);
            }
            case MarchitoAnims.FAROL_GOLPE -> {
                if (t == 19) empujar(0.5);
                if (t == 20) estallarFarol();
            }
            case MarchitoAnims.CAMPANA_GOLPE -> {
                if (t == 16) empujar(0.55);
            }
            case MarchitoAnims.CAMPANA_TOCAR -> {
                for (int g : MarchitoAnims.GOLPES[MarchitoAnims.CAMPANA_TOCAR]) if (t == g) campanada(g == MarchitoAnims.GOLPES[MarchitoAnims.CAMPANA_TOCAR][0]);
            }
            default -> {
            }
        }
    }

    /** El farol se estrella delante: fuego de almas y chispas azules alli (pega con su golpe). */
    private void estallarFarol() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        double x = getX() + d.x * 2.0, z = getZ() + d.z * 2.0, y = getY() + 0.2;
        fuegoX = x;
        fuegoY = getY();
        fuegoZ = z;
        fuegoHasta = tickCount + FUEGO_TICKS;
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.3, z, 26, 0.5, 0.25, 0.5, 0.04);
        sl.sendParticles(ParticleTypes.SOUL, x, y + 0.4, z, 6, 0.4, 0.2, 0.4, 0.02);
        sl.sendParticles(new DustParticleOptions(new Vector3f(0.4f, 0.85f, 1f), 1.4f), x, y + 0.3, z, 14, 0.6, 0.2, 0.6, 0.1);
        playSound(DarkCraftMod.SFX_MARCHITO_FAROL.get(), 1.4f, 0.9f + random.nextFloat() * 0.2f);
    }

    /** Una campanada: los de alrededor van a por su presa; el que la oye de cerca se queda un momento lento. */
    private void campanada(boolean primera) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (primera) ultimaCampana = tickCount;
        playSound(DarkCraftMod.SFX_MARCHITO_CAMPANA.get(), 2.6f, 0.95f + random.nextFloat() * 0.1f);
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        sl.sendParticles(new DustParticleOptions(new Vector3f(0.85f, 0.65f, 0.25f), 1.2f), getX() + d.x * 0.3, getY() + 3.1, getZ() + d.z * 0.3,
                10, 0.25, 0.2, 0.25, 0.05);
        for (int k = 0; k < 20; k++) {
            double a = k * Math.PI * 2 / 20;
            sl.sendParticles(ParticleTypes.SCULK_CHARGE_POP, getX() + Math.cos(a) * 1.4, getY() + 2.6, getZ() + Math.sin(a) * 1.4, 1, 0, 0.02, 0, 0);
        }
        LivingEntity presa = getTarget();
        if (presa == null) return;
        int vienen = 0;
        for (Enemigo e : level().getEntitiesOfClass(Enemigo.class, new AABB(blockPosition()).inflate(LLAMADA, 10, LLAMADA),
                o -> o != this && o.isAlive() && !o.muriendo())) {
            if (e.getTarget() == null && e.acudir(presa)) vienen++;
        }
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(6, 3, 6),
                o -> o instanceof net.minecraft.world.entity.player.Player p && !p.isCreative() && !p.isSpectator()))
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true));
        if (primera) CAMPANADAS++;
        if (primera && vienen > 0) DarkCraftMod.LOGGER.debug("[DarkCraft] marchito: la campana llama a {}", vienen);
    }

    /** Para la prueba: campanas tocadas desde que arranco el juego. */
    public static int CAMPANADAS;

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == MarchitoAnims.FAROL_GOLPE) le.setSecondsOnFire(4);
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == MarchitoAnims.GUADANA_TAJO || accion == MarchitoAnims.CAMPANA_GOLPE ? DarkCraftMod.SFX_GOLPE_PESADO.get()
                : accion == MarchitoAnims.GUADANA_SIEGA ? DarkCraftMod.SFX_ALABARDA_TAJO.get() : DarkCraftMod.SFX_GOLPE_LIGERO.get();
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_MARCHITO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        if (accion == MarchitoAnims.CAMPANA_TOCAR) return 6;
        return switch (variante()) {
            case FAROL -> 10 + random.nextInt(14);
            case CAMPANA -> 16 + random.nextInt(16);
            default -> 20 + random.nextInt(20);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == GUADANA ? 3.0 : variante() == CAMPANA ? 2.4 : 1.7;
    }

    @Override
    protected double prisa() {
        return variante() == FAROL ? 1.15 : 0.95;
    }

    /** Si el fuego del farol sigue ardiendo (para la prueba). */
    public boolean fuegoArde() {
        return tickCount < fuegoHasta;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // el fuego de almas que dejo el farol: arde un momento y quema a quien lo pisa
        if (!level().isClientSide && tickCount < fuegoHasta && level() instanceof ServerLevel sl) {
            if (tickCount % 3 == 0) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, fuegoX, fuegoY + 0.15, fuegoZ, 5, FUEGO_R * 0.45, 0.05, FUEGO_R * 0.45, 0.01);
                if (tickCount % 9 == 0) sl.sendParticles(ParticleTypes.SOUL, fuegoX, fuegoY + 0.3, fuegoZ, 1, 0.4, 0.1, 0.4, 0.01);
            }
            if (tickCount % 10 == 0)
                for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(fuegoX - FUEGO_R, fuegoY - 0.5, fuegoZ - FUEGO_R,
                        fuegoX + FUEGO_R, fuegoY + 1.5, fuegoZ + FUEGO_R), o -> !(o instanceof Enemigo) && o.isAlive())) {
                    if (le instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())) continue;
                    if (Math.hypot(le.getX() - fuegoX, le.getZ() - fuegoZ) > FUEGO_R) continue;
                    le.setSecondsOnFire(3);
                    le.hurt(damageSources().inFire(), 1.5f);
                }
        }
        // las brasas de las grietas sueltan alguna chispa; el farol, su llama de almas
        if (level().isClientSide && isAlive() && postura() != TUMBADO) {
            if (tickCount % 9 == 0)
                level().addParticle(ParticleTypes.SMALL_FLAME, getX() + (random.nextDouble() - 0.5) * 0.5, getY() + 0.6 + random.nextDouble() * 1.3,
                        getZ() + (random.nextDouble() - 0.5) * 0.5, 0, 0.01, 0);
            if (variante() == FAROL && accion() == 0 && tickCount % 4 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                // (a su izquierda: con el frente (-sen, cos), la izquierda es (cos, sen))
                double x = getX() + d.x * 0.55 + d.z * 0.35, z = getZ() + d.z * 0.55 - d.x * 0.35;
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, getY() + 0.95, z, 0, 0.012, 0);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_MARCHITO_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_MARCHITO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_MARCHITO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (!(level() instanceof ServerLevel sl)) return;
        // se deshace en ceniza con brasas
        sl.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY() + 1.0, getZ(), 40, 0.4, 0.8, 0.4, 0.02);
        sl.sendParticles(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.8, getZ(), 10, 0.3, 0.6, 0.3, 0.01);
        net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "marchito");
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
