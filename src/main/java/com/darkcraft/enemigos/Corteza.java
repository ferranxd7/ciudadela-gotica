package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Vareth, el Guardian de Corteza (parte 4d): el escudero de Ser Gavran, al que se trago el arbol. Espera de rodillas en
 * la base de la torre del arbol, ante la escalera y el ascensor (sellados mientras viva: {@code valdrenn/Guardianes}).
 * <ul>
 * <li><b>zarpazo</b> y <b>doble zarpazo</b> cruzado con sus garras de madera;</li>
 * <li><b>golpe al suelo</b>: las raices corren por el suelo hacia delante (se esquivan de lado);</li>
 * <li><b>espinas</b>: abre los brazos y suelta una rafaga de espinas delante (frenan);</li>
 * <li>con media vida, las <b>raices de alrededor</b>: dos anillos que brotan a su alrededor (hay que alejarse).</li>
 * </ul>
 * Es de madera: el fuego le hace el doble.
 */
public class Corteza extends Guardian {
    public static final int ECOS = 5000;

    /** Raices que van a brotar: donde, cuando (tick suyo), radio y multiplicador del golpe. */
    private record Brote(double x, double y, double z, int cuando, double r, float mult) {}

    private final List<Brote> brotes = new ArrayList<>();
    private final Set<UUID> dados = new HashSet<>();
    private int sueloEspera = 40, espinasEspera = 60, raicesEspera;

    public Corteza(EntityType<? extends Corteza> t, Level l) {
        super(t, l, "corteza", BossEvent.BossBarColor.GREEN);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 520.0).add(Attributes.ATTACK_DAMAGE, 9.5).add(Attributes.ARMOR, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public String tipo() {
        return "corteza";
    }

    @Override
    public int musica() {
        return 32;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "vareth";
    }

    @Override
    protected int[] ticks() {
        return CortezaAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return CortezaAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return CortezaAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return CortezaAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return CortezaAnims.RUGIDO;
    }

    @Override
    protected int accionLevantarse() {
        return CortezaAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 3.0) return 0;
        if (segunda && raicesEspera <= 0 && d < 6 && random.nextFloat() < 0.45f) {
            raicesEspera = 160 + random.nextInt(80);
            return CortezaAnims.RAICES;
        }
        if (d > 4.5 && d < 13 && sueloEspera <= 0 && random.nextFloat() < 0.5f) {
            sueloEspera = 90 + random.nextInt(60);
            return CortezaAnims.GOLPE_SUELO;
        }
        if (d > 3.2 && d < 8 && espinasEspera <= 0 && random.nextFloat() < 0.4f) {
            espinasEspera = 110 + random.nextInt(60);
            return CortezaAnims.ESPINAS;
        }
        if (d > 3.8) return 0;
        return random.nextFloat() < 0.55f ? CortezaAnims.ZARPAZO : CortezaAnims.DOBLE;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case CortezaAnims.ZARPAZO -> new Golpe(3.6f, 110f, 1.2f, 0.8f);
            case CortezaAnims.DOBLE -> new Golpe(3.4f, 150f, 1.55f, 1.1f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = CortezaAnims.GOLPES[accion];
        switch (accion) {
            case CortezaAnims.ZARPAZO -> {
                if (t == g[0] - 1) empujar(0.45);
                if (t == g[0]) playSound(DarkCraftMod.SFX_CORTEZA_ZARPAZO.get(), 1.2f, 0.9f + random.nextFloat() * 0.2f);
            }
            case CortezaAnims.DOBLE -> {
                if (t == g[0] - 1) empujar(0.55);
                if (t == g[0]) playSound(DarkCraftMod.SFX_CORTEZA_ZARPAZO.get(), 1.4f, 0.75f);
            }
            case CortezaAnims.GOLPE_SUELO -> {
                if (t == g[0]) lineaDeRaices();
            }
            case CortezaAnims.ESPINAS -> {
                if (t == g[0]) espinas();
            }
            case CortezaAnims.RAICES -> {
                if (t == g[0]) raicesAlrededor();
            }
            case CortezaAnims.RUGIDO -> {
                if (t == 9) {
                    playSound(DarkCraftMod.SFX_CORTEZA_RUGIDO.get(), 2.4f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, getX(), getY() + 4.0, getZ(), 40, 2.0, 0.5, 2.0, 0.0);
                        sl.sendParticles(com.darkcraft.gavran.Raices.VERDE, getX(), getY() + 1.6, getZ(), 30, 0.6, 0.8, 0.6, 0.02);
                    }
                }
            }
            default -> {
            }
        }
    }

    /** Las raices corren por el suelo hacia delante (cada 1,4 bloques, dos ticks una de otra), hasta 13 bloques. */
    private void lineaDeRaices() {
        if (!(level() instanceof ServerLevel sl)) return;
        dados.clear();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        level().playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_CORTEZA_RAICES.get(), SoundSource.HOSTILE, 1.8f, 1.0f);
        int k = 0;
        for (double a = 2.2; a <= 13; a += 1.4, k++) {
            double x = getX() + d.x * a, z = getZ() + d.z * a;
            double y = suelo(sl, x, z);
            if (Double.isNaN(y)) break;
            brotar(sl, x, y, z, 2 + 2 * k, 1.3, 0.9f);
        }
    }

    /** Las raices de alrededor: dos anillos (a 2,5 y a 4,8 bloques), uno tras otro. */
    private void raicesAlrededor() {
        if (!(level() instanceof ServerLevel sl)) return;
        dados.clear();
        level().playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_CORTEZA_RAICES.get(), SoundSource.HOSTILE, 2.2f, 0.8f);
        for (int anillo = 0; anillo < 2; anillo++) {
            double r = anillo == 0 ? 2.5 : 4.8;
            int n = anillo == 0 ? 8 : 14;
            for (int i = 0; i < n; i++) {
                double a = i * Math.PI * 2 / n + anillo * 0.2;
                double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
                double y = suelo(sl, x, z);
                if (!Double.isNaN(y)) brotar(sl, x, y, z, 4 + anillo * 10, 1.25, 1.0f);
            }
        }
    }

    /** Una raiz que brota en (x, y, z) dentro de {@code retraso} ticks (antes, el aviso: tierra que salta y verde). */
    private void brotar(ServerLevel sl, double x, double y, double z, int retraso, double r, float mult) {
        sl.sendParticles(com.darkcraft.gavran.Raices.VERDE, x, y + 0.1, z, 6, 0.3, 0.02, 0.3, 0.0);
        EfectosRaiz.estaca(sl, x, y, z, EfectosRaiz.ang(sl.random), 0.25f * (sl.random.nextFloat() - 0.5f), 2.2f + sl.random.nextFloat() * 0.8f,
                0.5f + sl.random.nextFloat() * 0.2f, retraso, 22);
        brotes.add(new Brote(x, y, z, tickCount + retraso + 1, r, mult));
    }

    /** La rafaga de espinas: en un cono de 70 grados delante, hasta 8 bloques (frenan). */
    private void espinas() {
        if (!(level() instanceof ServerLevel sl)) return;
        level().playSound(null, getX(), getY() + 1.5, getZ(), DarkCraftMod.SFX_CORTEZA_ESPINAS.get(), SoundSource.HOSTILE, 1.8f, 1.0f);
        float yaw = getYRot();
        for (int i = -4; i <= 4; i++) {
            float a = (float) Math.toRadians(-(yaw + i * 8.5f));
            EfectosRaiz.espina(sl, getX(), getY() + 1.4 + sl.random.nextFloat() * 0.6, getZ(), a, 6.5f + sl.random.nextFloat() * 1.5f, 14);
        }
        Vec3 mira = Vec3.directionFromRotation(0, yaw);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.85f;
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(8.5, 2.5, 8.5))) {
            if (!objetivoValido(p) || !hasLineOfSight(p)) continue;
            double dx = p.getX() - getX(), dz = p.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > 8.2 || (l > 0.8 && (dx * mira.x + dz * mira.z) / l < Math.cos(Math.toRadians(37)))) continue;
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
                p.knockback(0.5, -mira.x, -mira.z);
            }
        }
    }

    /** Lo alto del suelo bajo (x, z) cerca de su altura, o NaN si no hay (un hueco, un muro). */
    private double suelo(ServerLevel sl, double x, double z) {
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z), by = (int) Math.floor(getY());
        for (int y = by + 1; y >= by - 3; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            BlockState s = sl.getBlockState(p);
            if (!s.getCollisionShape(sl, p).isEmpty() && sl.getBlockState(p.above()).getCollisionShape(sl, p.above()).isEmpty())
                return y + 1;
        }
        return Double.NaN;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            // el corazon late (motas verdes) y caen hojas
            if (isAlive() && tickCount % 6 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                level().addParticle(com.darkcraft.gavran.Raices.VERDE, getX() + d.x * 0.55, getY() + 2.4, getZ() + d.z * 0.55, 0, 0.01, 0);
            }
            return;
        }
        if (sueloEspera > 0) sueloEspera--;
        if (espinasEspera > 0) espinasEspera--;
        if (raicesEspera > 0) raicesEspera--;
        if (!brotes.isEmpty() && level() instanceof ServerLevel sl) {
            float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
            for (int i = brotes.size() - 1; i >= 0; i--) {
                Brote b = brotes.get(i);
                if (tickCount < b.cuando()) continue;
                brotes.remove(i);
                sl.sendParticles(com.darkcraft.gavran.Raices.VERDE, b.x(), b.y() + 0.8, b.z(), 8, 0.3, 0.5, 0.3, 0.02);
                for (Player p : level().getEntitiesOfClass(Player.class, new AABB(b.x() - b.r() - 0.5, b.y() - 0.5, b.z() - b.r() - 0.5, b.x() + b.r() + 0.5, b.y() + 2.6, b.z() + b.r() + 0.5))) {
                    if (!objetivoValido(p) || dados.contains(p.getUUID()) || p.distanceToSqr(b.x(), p.getY(), b.z()) > b.r() * b.r()) continue;
                    if (p.hurt(damageSources().mobAttack(this), base * b.mult())) {
                        dados.add(p.getUUID());
                        p.setDeltaMovement(p.getDeltaMovement().add(0, 0.55, 0));
                        p.hurtMarked = true;
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // de madera: el fuego le hace el doble
        if (s.is(DamageTypeTags.IS_FIRE)) cuanto *= 2f;
        return super.hurt(s, cuanto);
    }

    @Override
    public boolean fireImmune() {
        return false;
    }

    @Override
    protected void alSegundaMitad() {
        raicesEspera = 20;
    }

    @Override
    protected double prisa() {
        return 1.25;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case CortezaAnims.RUGIDO -> 4;
            case CortezaAnims.RAICES -> 24 + random.nextInt(12);
            default -> (segunda ? 8 : 14) + random.nextInt(14);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return 2.6;
    }

    @Override
    protected double alturaMaxima() {
        return 3.0;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case CortezaAnims.ZARPAZO, CortezaAnims.DOBLE -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(net.minecraft.sounds.SoundEvents.WOOD_STEP, 1.0f, 0.5f + random.nextFloat() * 0.1f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CORTEZA_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_CORTEZA_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 3));
        spawnAtLocation(new net.minecraft.world.item.ItemStack(DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.HUESO).get()));
    }
}
