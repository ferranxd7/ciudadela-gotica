package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * El Capitan de Brumavela, Edrik Maroll: el primer jefe con barra. Guarda la puerta de su faro (dentro esta el corazon
 * de La Garza Gris). No es una sala cerrada: la pelea empieza cuando ve a alguien en lo alto del penon y, si todos caen
 * o se alejan, vuelve a su puesto con la vida entera.
 * <ul>
 * <li><b>El sable</b>: un tajo de arriba abajo que sostiene un instante (el que mas duele), dos tajos anchos avanzando y
 * una estocada larga a fondo para quien se queda a media distancia.</li>
 * <li><b>El farol de animas</b>: lo alza y el suelo se enciende bajo los pies de cada uno; la marca avisa un momento
 * antes de que salga la llama (hay que salir de ella, o rodar). Por debajo de media vida, tres tandas seguidas.</li>
 * <li><b>A media vida</b> (una vez por pelea) <b>llama a su tripulacion</b>: salen marineros ahogados de la bruma (dos, y
 * uno mas por cada jugador de mas, hasta cuatro). Se deshacen cuando el cae o la pelea se acaba.</li>
 * </ul>
 * Parar un golpe suyo le deja aturdido un momento, como a los demas. Al caer: {@link Puestos#alCaer}.
 */
public class Capitan extends Enemigo {
    private static final EntityDataAccessor<Boolean> EN_PELEA = SynchedEntityData.defineId(Capitan.class, EntityDataSerializers.BOOLEAN);

    public static final int VOZ_BASE = Voces.BASE_CAPITAN;
    /** Los ecos que deja a cada uno de los que le vencen. */
    public static final int ECOS = 1500;
    /** Lo que tarda la llama en salir de su marca (ticks), antes y despues de media vida. */
    private static final int MECHA = 17, MECHA_2 = 14;
    private static final double RADIO_LLAMA = 1.7;

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private final ServerBossEvent barra = new ServerBossEvent(Component.translatable("entity.darkcraft.capitan"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private boolean llamo, amenazo;
    private int sinPresa, sinFarol = 120, hablaHasta, olas, siguienteOla;
    private LivingEntity ultimaPresa;
    private final List<Ahogado> tripulacion = new ArrayList<>();
    /** Los que le han plantado cara en esta pelea (para repartir lo que deja). */
    private final Set<UUID> rivales = new HashSet<>();

    /** Una llama que va a salir del suelo: donde y en que tick. */
    private record Llama(double x, double y, double z, int cuando) {}

    private final List<Llama> llamas = new ArrayList<>();
    /** Cuantas marcas ha puesto en el suelo desde que existe (para las pruebas). */
    private int llamasPuestas;

    public int llamasPuestas() {
        return llamasPuestas;
    }

    public Capitan(EntityType<? extends Capitan> t, Level l) {
        super(t, l);
        xpReward = 0;
        barra.setVisible(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 260.0).add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 26.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.85);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EN_PELEA, false);
    }

    /** Si esta peleando (lo sabe tambien el cliente: la musica). */
    public boolean enPelea() {
        return entityData.get(EN_PELEA);
    }

    public boolean segundaMitad() {
        return getHealth() < getMaxHealth() * 0.5f;
    }

    public Set<UUID> rivales() {
        return rivales;
    }

    @Override
    public String tipo() {
        return "capitan";
    }

    @Override
    protected int[] ticks() {
        return CapitanAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return CapitanAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return CapitanAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return CapitanAnims.MORIR;
    }

    @Override
    protected double correa() {
        return 26;
    }

    @Override
    protected double distanciaDeseada() {
        return 2.3;
    }

    @Override
    protected double prisa() {
        return segundaMitad() ? 1.12 : 1.0;
    }

    /** No hace falta que mire: guarda su puerta, y ve a quien suba al penon. */
    @Override
    protected boolean percibe(LivingEntity p) {
        return distanceToSqr(p) < 15 * 15 && Math.abs(p.getY() - getY()) < 6;
    }

    @Override
    protected int pausaTras(int accion) {
        if (accion == CapitanAnims.LLAMAR) return 10;
        return segundaMitad() ? 7 + random.nextInt(10) : 13 + random.nextInt(13);
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (!llamo && segundaMitad() && d < 24) return CapitanAnims.LLAMAR;
        // el farol: a quien se queda lejos, y de vez en cuando tambien de cerca
        if (sinFarol <= 0 && d < 20 && (d > 5.2 || random.nextFloat() < 0.3f)) return CapitanAnims.FAROL;
        if (d < 3.1) {
            float r = random.nextFloat();
            return r < 0.42f ? CapitanAnims.TAJO : r < 0.84f ? CapitanAnims.DOBLE : CapitanAnims.ESTOCADA;
        }
        if (d < 5.4) return CapitanAnims.ESTOCADA;
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case CapitanAnims.TAJO -> new Golpe(3.5f, 70f, 1.45f, 0.6f);
            case CapitanAnims.DOBLE -> n == 0 ? new Golpe(3.4f, 150f, 0.95f, 0.3f) : new Golpe(3.4f, 150f, 1.0f, 0.5f);
            case CapitanAnims.ESTOCADA -> new Golpe(4.7f, 32f, 1.25f, 0.8f);
            default -> null;
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case CapitanAnims.ESTOCADA -> DarkCraftMod.SFX_ESTOCADA.get();
            case CapitanAnims.TAJO, CapitanAnims.DOBLE -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = CapitanAnims.GOLPES[accion];
        switch (accion) {
            case CapitanAnims.TAJO -> {
                if (t == g[0] - 1) empujar(0.55);
            }
            case CapitanAnims.DOBLE -> {
                if (t == g[0] - 1) empujar(0.6);
                else if (t == g[1] - 1) empujar(0.65);
            }
            case CapitanAnims.ESTOCADA -> {
                if (t == g[0] - 2) empujar(1.4);
            }
            case CapitanAnims.FAROL -> {
                if (t == 6) {
                    playSound(DarkCraftMod.SFX_CAPITAN_FAROL.get(), 1.6f, 0.95f + random.nextFloat() * 0.1f);
                    if (!amenazo) {
                        amenazo = true;
                        hablaSiCalla(4);
                    }
                }
                if (t >= 6 && t <= g[0] + 12 && level() instanceof ServerLevel sl) {
                    Vec3 f = farol();
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, f.x, f.y, f.z, 3, 0.18, 0.2, 0.18, 0.02);
                }
                if (t == g[0]) {
                    marcar();
                    olas = segundaMitad() ? 2 : 0;
                    siguienteOla = tickCount + 12;
                    sinFarol = (segundaMitad() ? 120 : 190) + random.nextInt(70);
                }
            }
            case CapitanAnims.LLAMAR -> {
                if (t == 4) {
                    habla(2);
                    playSound(DarkCraftMod.SFX_CAPITAN_LLAMAR.get(), 2.2f, 1.0f);
                }
                if (t == g[0]) llamar();
            }
            default -> {
            }
        }
    }

    /** Donde tiene el farol (para las chispas): arriba, a su izquierda. */
    private Vec3 farol() {
        Vec3 izq = Vec3.directionFromRotation(0, getYRot() - 90f);
        return position().add(izq.x * 0.7, getBbHeight() * 0.92, izq.z * 0.7);
    }

    // ------------------------------------------------------------------ el farol: el suelo se enciende

    /** Una marca bajo los pies de cada uno de los que tiene cerca (la llama sale poco despues). */
    private void marcar() {
        if (!(level() instanceof ServerLevel sl)) return;
        int mecha = segundaMitad() ? MECHA_2 : MECHA;
        for (ServerPlayer p : sl.players()) {
            if (!p.isAlive() || p.isSpectator() || p.isCreative() || p.distanceToSqr(this) > 22 * 22) continue;
            Vec3 s = suelo(sl, p);
            if (s == null) continue;
            llamas.add(new Llama(s.x, s.y, s.z, tickCount + mecha));
            llamasPuestas++;
            sl.playSound(null, s.x, s.y, s.z, net.minecraft.sounds.SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.4f, 0.7f);
        }
    }

    /**
     * Donde se enciende el suelo bajo un jugador: sus pies si pisa (aunque asome medio cuerpo por un borde y bajo su centro
     * no haya nada), y si va por el aire el suelo que tenga debajo o justo al lado; null si cae al vacio.
     */
    private static Vec3 suelo(ServerLevel sl, Player p) {
        if (p.onGround()) return p.position();
        Vec3 s = suelo(sl, p.position());
        if (s != null) return s;
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            s = suelo(sl, p.position().add(d[0] * 0.6, 0, d[1] * 0.6));
            if (s != null) return new Vec3(p.getX(), s.y, p.getZ());
        }
        return null;
    }

    /** El suelo firme bajo un punto (hasta cuatro bloques mas abajo), o null. */
    private static Vec3 suelo(ServerLevel sl, Vec3 p) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int y0 = (int) Math.floor(p.y + 0.3);
        for (int y = y0; y >= y0 - 4; y--) {
            m.set(Math.floor(p.x), y, Math.floor(p.z));
            VoxelShape f = sl.getBlockState(m).getCollisionShape(sl, m);
            if (f.isEmpty()) continue;
            double arriba = y + f.max(net.minecraft.core.Direction.Axis.Y);
            if (arriba > p.y + 0.6) continue;
            return new Vec3(p.x, arriba, p.z);
        }
        return null;
    }

    private void llamas(ServerLevel sl) {
        if (olas > 0 && tickCount >= siguienteOla) {
            olas--;
            siguienteOla = tickCount + 12;
            marcar();
        }
        if (llamas.isEmpty()) return;
        Iterator<Llama> it = llamas.iterator();
        while (it.hasNext()) {
            Llama l = it.next();
            if (tickCount < l.cuando()) {
                // la marca: un corro de llamitas azules que se cierra
                double falta = (l.cuando() - tickCount) / (double) MECHA;
                for (int k = 0; k < 6; k++) {
                    double a = (tickCount * 0.5 + k * Math.PI / 3);
                    double r = RADIO_LLAMA * (0.55 + 0.45 * falta);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, l.x() + Math.cos(a) * r, l.y() + 0.08, l.z() + Math.sin(a) * r, 1, 0, 0, 0, 0);
                }
                if (tickCount % 3 == 0) sl.sendParticles(ParticleTypes.SOUL, l.x(), l.y() + 0.1, l.z(), 1, 0.5, 0.02, 0.5, 0.01);
                continue;
            }
            it.remove();
            // la llama
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, l.x(), l.y() + 0.2, l.z(), 46, 0.55, 0.1, 0.55, 0.09);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, l.x(), l.y() + 1.3, l.z(), 30, 0.35, 0.9, 0.35, 0.03);
            sl.sendParticles(ParticleTypes.SOUL, l.x(), l.y() + 0.6, l.z(), 8, 0.5, 0.5, 0.5, 0.04);
            sl.playSound(null, l.x(), l.y(), l.z(), DarkCraftMod.SFX_CAPITAN_LLAMA.get(), SoundSource.HOSTILE, 1.5f, 0.92f + random.nextFloat() * 0.16f);
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f;
            AABB caja = new AABB(l.x() - RADIO_LLAMA, l.y() - 1.0, l.z() - RADIO_LLAMA, l.x() + RADIO_LLAMA, l.y() + 2.8, l.z() + RADIO_LLAMA);
            for (Player p : sl.getEntitiesOfClass(Player.class, caja, q -> q.isAlive() && objetivoValido(q))) {
                double dx = p.getX() - l.x(), dz = p.getZ() - l.z();
                if (dx * dx + dz * dz > RADIO_LLAMA * RADIO_LLAMA) continue;
                if (p.hurt(damageSources().indirectMagic(this, this), dano)) p.setDeltaMovement(p.getDeltaMovement().add(0, 0.28, 0));
            }
        }
    }

    // ------------------------------------------------------------------ la tripulacion

    private void llamar() {
        llamo = true;
        if (!(level() instanceof ServerLevel sl)) return;
        int jugadores = 0;
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) jugadores++;
        int n = Math.max(2, Math.min(4, 1 + jugadores));
        double giro = random.nextDouble() * Math.PI * 2;
        int salen = 0;
        for (int k = 0; k < 12 && salen < n; k++) {
            double a = giro + k * (Math.PI * 2 / n) + (k >= n ? 0.6 : 0);
            double r = 3.6 + (k % 3) * 0.9;
            Vec3 s = suelo(sl, position().add(Math.cos(a) * r, 2.2, Math.sin(a) * r));
            if (s == null || Math.abs(s.y - getY()) > 3.2) continue;
            Ahogado m = DarkCraftMod.AHOGADO.get().create(sl);
            if (m == null) return;
            m.moveTo(s.x, s.y, s.z, (float) Math.toDegrees(a) + 90f, 0f);
            if (!sl.noCollision(m)) continue;
            // el tercero y el cuarto (con amigos), con arpon
            m.variante(salen >= 2 ? Ahogado.ARPON : Ahogado.SABLE);
            m.efimero(160);
            sl.addFreshEntity(m);
            m.pausa = 24;
            LivingEntity t = sl.getNearestPlayer(m.getX(), m.getY(), m.getZ(), 30, e -> e instanceof Player q && objetivoValido(q));
            if (t != null) m.setTarget(t);
            sl.sendParticles(ParticleTypes.CLOUD, s.x, s.y + 0.6, s.z, 26, 0.4, 0.7, 0.4, 0.02);
            sl.sendParticles(ParticleTypes.SOUL, s.x, s.y + 0.4, s.z, 8, 0.3, 0.4, 0.3, 0.03);
            tripulacion.add(m);
            salen++;
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] El Capitan llama a su tripulacion: {} marineros ({} jugadores)", salen, jugadores);
    }

    private void despedir() {
        for (Ahogado m : tripulacion) if (m.isAlive() && !m.isRemoved()) m.disolver();
        tripulacion.clear();
    }

    /** Cuantos de los suyos siguen en pie (para las pruebas). */
    public int tripulacion() {
        int n = 0;
        for (Ahogado m : tripulacion) if (m.isAlive() && !m.isRemoved()) n++;
        return n;
    }

    // ------------------------------------------------------------------ la pelea: empieza, se acaba, la barra y lo que dice

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
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t == null || level().isClientSide || !isAlive()) return;
        ultimaPresa = t;
        if (t instanceof ServerPlayer p) rivales.add(p.getUUID());
        if (!enPelea()) empezarPelea();
    }

    private void empezarPelea() {
        entityData.set(EN_PELEA, true);
        llamo = amenazo = false;
        sinFarol = 110 + random.nextInt(60);
        sinPresa = 0;
        int cerca = 0;
        if (level() instanceof ServerLevel sl) for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 48 * 48) cerca++;
        paraGrupo(cerca);
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // no se le echa encima mientras lo dice entero: un respiro para verle
            pausa = Math.max(pausa, 50);
        } else pausa = Math.max(pausa, 16);
        DarkCraftMod.LOGGER.info("[DarkCraft] El Capitan de Brumavela: empieza la pelea ({} jugadores, {} de vida)", cerca, Math.round(getMaxHealth()));
    }

    /** Todos han caido o se han ido: vuelve a lo suyo con la vida entera. */
    private void acabarPelea() {
        entityData.set(EN_PELEA, false);
        despedir();
        llamas.clear();
        olas = 0;
        rivales.clear();
        setHealth(getMaxHealth());
        barra.removeAllPlayers();
        DarkCraftMod.LOGGER.info("[DarkCraft] El Capitan de Brumavela vuelve a su puerta");
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl)) {
            // el farol: una llamita de vez en cuando, y mas cuando lo alza
            if (isAlive() && tickCount % 9 == 0) {
                Vec3 izq = Vec3.directionFromRotation(0, yBodyRot - 90f);
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() + izq.x * 0.75, getY() + 0.95, getZ() + izq.z * 0.75, 0, 0.012, 0);
            }
            return;
        }
        barra.setProgress(Math.max(0f, getHealth() / getMaxHealth()));
        if (!isAlive()) return;
        if (sinFarol > 0) sinFarol--;
        llamas(sl);
        if (enPelea()) {
            if (getTarget() == null) {
                // ha caido aquel con quien peleaba
                if (ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
                    ultimaPresa = null;
                    hablaSiCalla(3);
                }
                if (++sinPresa > 110) acabarPelea();
            } else sinPresa = 0;
        }
        if (tickCount % 10 == 0) {
            // la barra: para los que estan en la pelea
            for (ServerPlayer p : sl.players()) {
                boolean cerca = enPelea() && p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 44 * 44;
                if (cerca) barra.addPlayer(p);
                else barra.removePlayer(p);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // el fuego de sus propias llamas y las caidas no le hacen nada
        if (s.is(net.minecraft.tags.DamageTypeTags.IS_FALL) || s.getEntity() instanceof Enemigo) return false;
        boolean ok = super.hurt(s, cuanto);
        if (ok && s.getEntity() instanceof ServerPlayer p) rivales.add(p.getUUID());
        return ok;
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        habla(5);
        despedir();
        llamas.clear();
        olas = 0;
        entityData.set(EN_PELEA, false);
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.2, getZ(), 30, 0.5, 0.8, 0.5, 0.04);
    }

    @Override
    public void remove(RemovalReason r) {
        barra.removeAllPlayers();
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        barra.removePlayer(p);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CAPITAN_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_CAPITAN_MUERTE.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEDRA_FORJA_1.get(), 3));
        spawnAtLocation(new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 1));
    }
}
