package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
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

import java.util.ArrayList;
import java.util.List;

/**
 * La Molinera de Velmor (parte 7f): el jefe de las Llanuras Palidas. La molinera del libro del campanero ("sigue en su
 * campo, segando lo que ya no crece"), crecida por la Sombra hasta el doble de alto. Espera de rodillas ante la piedra
 * de moler, en medio de la era de su molino ({@code morvain/Molino}), de espaldas a quien llega por el camino de Velmor.
 * <ul>
 * <li>la <b>siega</b>: un barrido enorme a ras de suelo que abarca casi todo lo de delante (se salta o se rueda);</li>
 * <li>el <b>tajo</b> de arriba abajo: la hoja se clava en la era y levanta una onda (saltando o rodando se libra);</li>
 * <li>la <b>estocada</b>: se lanza con la punta de la guadana a quien se aleja;</li>
 * <li>el <b>pisoton</b>: a quien se le pone detras o pegado a ella, lo aparta;</li>
 * <li>a <b>media vida</b> grita y <b>llama a los suyos</b> (salen marchitos de la ceniza de la era: dos, y uno mas por cada
 * amigo, hasta cuatro) y empieza el <b>giro</b>: dos vueltas con la guadana abierta avanzando hacia su presa.</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_MOLINERA}): 1 al empezar (no la repite en 4 min; hasta
 * acabarla no grita ni ataca), 2 al llamar a los suyos, 3 si cae su presa, 4 la primera vez que gira y 5 al morir. Al caer
 * (lo reparte {@code valdrenn/Guardianes}): el cartel con sus ecos, la hazana {@code molinera} y el bestiario; y el molino
 * deja de crujir ({@code morvain/Molino}).
 */
public class Molinera extends Guardian {
    public static final int ECOS = 7000;
    public static final int VOZ_BASE = Voces.BASE_MOLINERA;

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int giroEspera = 40, estocadaEspera = 30, pisotonEspera, siegas, hablaHasta, esperaRugido;
    private boolean llamo, giroDicho;
    private LivingEntity ultimaPresa;
    private final List<Marchito> suyos = new ArrayList<>();
    /** Para las pruebas: cuantos ha llamado y cuantas ondas ha levantado desde que existe. */
    private int llamados, ondas;

    public Molinera(EntityType<? extends Molinera> t, Level l) {
        super(t, l, "molinera", BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 700.0).add(Attributes.ATTACK_DAMAGE, 11.0).add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0).add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public String tipo() {
        return "molinera";
    }

    @Override
    public int musica() {
        return 33;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "molinera";
    }

    @Override
    protected int[] ticks() {
        return MolineraAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return MolineraAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return MolineraAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return MolineraAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return MolineraAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return MolineraAnims.LEVANTARSE;
    }

    public int llamados() {
        return llamados;
    }

    public int ondas() {
        return ondas;
    }

    /** Cuantos de los suyos siguen en pie (pruebas). */
    public int suyos() {
        int n = 0;
        for (Marchito m : suyos) if (m.isAlive() && !m.isRemoved()) n++;
        return n;
    }

    // ------------------------------------------------------------------ que hace

    /** Si {@code p} esta a su espalda (mas de 110 grados de donde mira). */
    private boolean detras(LivingEntity p) {
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = p.getX() - getX(), dz = p.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l < -0.34;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 3.5) return 0;
        // quien se le pone detras (o se le mete entre las piernas): el pisoton
        if (pisotonEspera <= 0 && (d < 3.4 && detras(presa) || d < 1.6)) {
            pisotonEspera = 50 + random.nextInt(30);
            return MolineraAnims.PISOTON;
        }
        if (segunda && giroEspera <= 0 && d < 7.0 && random.nextFloat() < 0.5f) {
            giroEspera = 150 + random.nextInt(90);
            return MolineraAnims.GIRO;
        }
        // a quien se aleja: la estocada (cruza media era)
        if (d > 5.4 && d < 9.5 && estocadaEspera <= 0 && random.nextFloat() < 0.6f) {
            estocadaEspera = 90 + random.nextInt(60);
            return MolineraAnims.ESTOCADA;
        }
        if (d > 5.6) return 0;
        // la siega, pero no tres seguidas (se aprende a saltarla); si no, el tajo
        if (siegas < 2 && random.nextFloat() < 0.55f) {
            siegas++;
            return MolineraAnims.SIEGA;
        }
        siegas = 0;
        return MolineraAnims.TAJO;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case MolineraAnims.SIEGA -> new Golpe(5.6f, 210f, 1.15f, 0.9f);
            case MolineraAnims.TAJO -> new Golpe(5.4f, 46f, 1.6f, 0.5f);
            case MolineraAnims.GIRO -> n == 0 ? new Golpe(5.2f, 360f, 1.0f, 0.9f) : new Golpe(5.2f, 360f, 1.2f, 1.3f);
            case MolineraAnims.ESTOCADA -> new Golpe(6.6f, 34f, 1.5f, 1.4f);
            case MolineraAnims.PISOTON -> new Golpe(3.4f, 360f, 0.8f, 1.9f);
            default -> null;
        };
    }

    /** La siega va a ras de suelo: quien la salta (los pies a mas de medio bloque) se libra. */
    @Override
    protected boolean pilla(int accion, int n, LivingEntity le) {
        if (accion == MolineraAnims.SIEGA) return le.getY() - getY() < 0.55;
        return true;
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = MolineraAnims.GOLPES[accion];
        switch (accion) {
            case MolineraAnims.SIEGA -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_MOLINERA_SIEGA.get(), 1.7f, 0.9f + random.nextFloat() * 0.15f);
                if (t == g[0] - 1) empujar(0.35);
                if (t == g[0]) barrido(5.0, 105);
            }
            case MolineraAnims.TAJO -> {
                if (t == g[0] - 1) empujar(0.45);
                if (t == g[0] + 1) onda(4.6, 2.7, 0.7f, 0.42);
            }
            case MolineraAnims.GIRO -> {
                if (t == 12 || t == g[1] - 12) playSound(DarkCraftMod.SFX_MOLINERA_GIRO.get(), 1.9f, t == 12 ? 0.95f : 1.05f);
                if (t == 12 && !giroDicho) {
                    giroDicho = true;
                    hablaSiCalla(4);
                }
                // gira avanzando hacia su presa
                LivingEntity p = getTarget();
                if (p != null && t >= 14 && t <= g[1] + 2 && t % 3 == 0) avanzarHacia(p, 0.44);
                if (t == g[0] || t == g[1]) barrido(4.8, 180);
            }
            case MolineraAnims.ESTOCADA -> {
                if (t == g[0] - 4) empujar(1.55);
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_ESTOCADA.get(), 1.4f, 0.7f);
            }
            case MolineraAnims.PISOTON -> {
                if (t == g[0]) onda(0.0, 3.3, 0.75f, 0.55);
            }
            case MolineraAnims.GRITO -> {
                if (t == 8) {
                    playSound(DarkCraftMod.SFX_MOLINERA_GRITO.get(), 2.8f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() * 0.85, getZ(), 18, 0.5, 0.4, 0.5, 0.03);
                        sl.sendParticles(ParticleTypes.ASH, getX(), getY() + getBbHeight() * 0.6, getZ(), 60, 2.5, 1.2, 2.5, 0.02);
                    }
                }
                // la de media vida: "venid, hijos de Velmor" y salen de la ceniza de la era
                if (t == 14 && segunda && !llamo) {
                    llamo = true;
                    habla(2);
                    llamar();
                }
            }
            default -> {
            }
        }
    }

    /** Un paso hacia alguien (girando: no importa hacia donde mira). */
    private void avanzarHacia(LivingEntity t, double f) {
        double dx = t.getX() - getX(), dz = t.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        if (l < 2.6) return;
        setDeltaMovement(dx / l * f, getDeltaMovement().y, dz / l * f);
        hasImpulse = true;
    }

    /** Lo que levanta la hoja al barrer la era: polvo y harina a ras de suelo, en un arco delante (o alrededor). */
    private void barrido(double r, double medio) {
        if (!(level() instanceof ServerLevel sl)) return;
        BlockState suelo = level().getBlockState(BlockPos.containing(getX(), getY() - 0.5, getZ()));
        for (int k = 0; k <= 12; k++) {
            double a = Math.toRadians(getYRot() + 90 - medio + 2 * medio * k / 12.0);
            double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 0.35, z, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.WHITE_ASH, x, getY() + 0.2, z, 6, 0.4, 0.1, 0.4, 0.01);
            if (!suelo.isAir() && k % 2 == 0) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, 4, 0.3, 0.05, 0.3, 0.1);
        }
    }

    /**
     * La guadana (o el pie) contra el suelo: polvo, piedras y una onda (a {@code delante} bloques, de radio {@code r}) que
     * alcanza a quien este de pie (saltando o rodando se libra).
     */
    private void onda(double delante, double r, float mult, double arriba) {
        if (!(level() instanceof ServerLevel sl)) return;
        ondas++;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * delante, z = getZ() + d.z * delante;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        int n = (int) Math.round(r * 14);
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, n, r * 0.4, 0.05, r * 0.4, 0.25);
        sl.sendParticles(ParticleTypes.EXPLOSION, x, getY() + 0.3, z, 1, r * 0.3, 0, r * 0.3, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, n / 2, r * 0.4, 0.1, r * 0.4, 0.02);
        sl.sendParticles(ParticleTypes.WHITE_ASH, x, getY() + 0.4, z, n, r * 0.5, 0.3, r * 0.5, 0.02);
        level().playSound(null, x, getY(), z, DarkCraftMod.SFX_MOLINERA_GOLPE.get(), SoundSource.HOSTILE, 2.0f, delante < 1 ? 1.1f : 0.92f);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * mult;
        for (Player p : level().getEntitiesOfClass(Player.class, new AABB(x - r - 1, getY() - 1, z - r - 1, x + r + 1, getY() + 2.5, z + r + 1))) {
            if (!objetivoValido(p) || !p.onGround() || p.distanceToSqr(x, p.getY(), z) > r * r) continue;
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                p.knockback(0.6, x - p.getX(), z - p.getZ());
                p.setDeltaMovement(p.getDeltaMovement().add(0, arriba, 0));
                p.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------------ los suyos: salen de la ceniza de la era

    private void llamar() {
        if (!(level() instanceof ServerLevel sl)) return;
        int jugadores = 0;
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) jugadores++;
        int n = Math.max(2, Math.min(4, 1 + jugadores));
        double giro = random.nextDouble() * Math.PI * 2;
        int salen = 0;
        for (int k = 0; k < 14 && salen < n; k++) {
            double a = giro + k * (Math.PI * 2 / n) + (k >= n ? 0.45 * (k / n) : 0);
            double r = 5.0 + (k % 3) * 1.3;
            int bx = Mth.floor(getX() + Math.cos(a) * r), bz = Mth.floor(getZ() + Math.sin(a) * r);
            double y = Puestos.suelo(sl, bx, Mth.floor(getY()) + 1, bz);
            if (Math.abs(y - getY()) > 2.2) continue;
            Marchito m = DarkCraftMod.MARCHITO.get().create(sl);
            if (m == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            m.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            if (!sl.noCollision(m)) continue;
            m.variante(salen % 2 == 0 ? Marchito.GUADANA : Marchito.FAROL);
            // tumbados en la ceniza: se alzan al ver a su presa (sin puesto: no vuelven ni se guardan)
            m.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            m.efimero(200);
            sl.addFreshEntity(m);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(m.getX(), m.getY(), m.getZ(), 30, e -> e instanceof Player q && objetivoValido(q));
            if (t != null) m.setTarget(t);
            BlockState suelo = sl.getBlockState(BlockPos.containing(bx + 0.5, y - 0.5, bz + 0.5));
            if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), bx + 0.5, y + 0.1, bz + 0.5, 24, 0.5, 0.05, 0.5, 0.15);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, bx + 0.5, y + 0.4, bz + 0.5, 20, 0.45, 0.5, 0.45, 0.015);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, bx + 0.5, y + 0.3, bz + 0.5, 10, 0.4, 0.3, 0.4, 0.02);
            sl.playSound(null, bx + 0.5, y, bz + 0.5, DarkCraftMod.SFX_EMBOSCADA_ALZARSE.get(), SoundSource.HOSTILE, 1.3f, 0.85f + random.nextFloat() * 0.2f);
            suyos.add(m);
            salen++;
        }
        llamados += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] La Molinera llama a los suyos: {} marchitos ({} jugadores)", salen, jugadores);
    }

    private void despedir() {
        for (Marchito m : suyos) if (m.isAlive() && !m.isRemoved()) m.disolver();
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
        llamo = giroDicho = false;
        giroEspera = 40;
        estocadaEspera = 30;
        siegas = 0;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se levanta mientras lo dice y no grita ni ataca hasta acabarlo (se acerca despacio)
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaRugido = tickCount + dura - 24;
            pausa = Math.max(pausa, dura);
        } else esperaRugido = 0;
    }

    @Override
    protected boolean puedeRugir() {
        return segunda || tickCount >= esperaRugido;
    }

    @Override
    protected void alAcabarPelea() {
        despedir();
        esperaRugido = 0;
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            // la ceniza que se le cae (las brasas de las grietas)
            if (isAlive() && tickCount % 4 == 0)
                level().addParticle(ParticleTypes.WHITE_ASH, getX() + (random.nextDouble() - 0.5) * 1.2, getY() + 1.2 + random.nextDouble() * 2.2,
                        getZ() + (random.nextDouble() - 0.5) * 1.2, 0, -0.02, 0);
            return;
        }
        if (!isAlive()) return;
        if (giroEspera > 0) giroEspera--;
        if (estocadaEspera > 0) estocadaEspera--;
        if (pisotonEspera > 0) pisotonEspera--;
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    @Override
    protected void alSegundaMitad() {
        giroEspera = 30;
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        habla(5);
        despedir();
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    @Override
    protected double prisa() {
        return segunda ? 1.3 : 1.15;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case MolineraAnims.GRITO -> 6;
            case MolineraAnims.GIRO -> 22 + random.nextInt(12);
            case MolineraAnims.PISOTON -> 6 + random.nextInt(6);
            default -> (segunda ? 9 : 15) + random.nextInt(13);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return 4.0;
    }

    @Override
    protected double alturaMaxima() {
        return 3.5;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case MolineraAnims.TAJO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(SoundEvents.SAND_STEP, 0.9f, 0.55f + random.nextFloat() * 0.1f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_MOLINERA_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_MOLINERA_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get()));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.TORBELLINO).get()));
    }
}
