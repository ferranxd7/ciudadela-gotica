package com.darkcraft.world;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.CastlePlan;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * El mundo de nubes (parte 3a), lo que no es generar: el registro del generador ({@code darkcraft:nubes}, lo usa el
 * preset {@code minecraft:normal} de {@code data/minecraft/worldgen/world_preset/normal.json}), donde se aparece al
 * crear el mundo y caer al vacio.
 * <ul>
 * <li><b>Donde se aparece</b> (de momento, hasta que llegue la isla del pueblo): en el camino de nubes, delante de la
 * Torre de Ascenso del reino mas cercano al origen, a 24 bloques de su centro (fuera de la proteccion del punto de
 * aparicion de un servidor dedicado, 16 bloques por defecto: dentro de ella quien no es operador no puede abrir cofres
 * ni usar nada). Radio de aparicion 0: se aparece justo ahi.</li>
 * <li><b>El vacio</b>: quien cae por debajo de las nubes (10 bloques bajo su fondo) muere en el acto, con el dano de
 * caer del mundo (la espada de luz se queda en el ultimo suelo firme; los jefes vuelven a su sitio). En creativo o
 * espectador, nada.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class MundoNubes {
    private MundoNubes() {}

    public static final DeferredRegister<Codec<? extends ChunkGenerator>> GENERADORES =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, DarkCraftMod.MODID);
    public static final RegistryObject<Codec<? extends ChunkGenerator>> NUBES = GENERADORES.register("nubes", () -> NubesGenerador.CODEC);

    public static void registrar(IEventBus bus) {
        GENERADORES.register(bus);
    }

    /** Lo que hay que bajar por debajo del fondo de la nube para perderse en el vacio. */
    public static final int VACIO = 10;

    /** Donde se aparece en un mundo de nubes nuevo (null si no hay ningun reino cerca del origen). */
    public static BlockPos aparicion(long seed) {
        int[] s = CastleSites.nearest(seed, 0, 0, 6);
        if (s == null) return null;
        CastlePlan.Cine ci = PlanCache.get(seed, s[0], s[1], (x, z) -> NubesGenerador.SUELO).cine();
        if (!ci.asc()) return null;
        return new BlockPos(ci.ascX(), NubesGenerador.SUELO, ci.ascZ() + 24);
    }

    @SubscribeEvent
    public static void onCrearAparicion(LevelEvent.CreateSpawnPosition e) {
        if (!(e.getLevel() instanceof ServerLevel lvl) || lvl.dimension() != Level.OVERWORLD || !NubesGenerador.es(lvl)) return;
        BlockPos p = null;
        try {
            p = aparicion(lvl.getSeed());
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("No se pudo buscar donde aparecer en el mundo de nubes", t);
        }
        if (p == null) p = new BlockPos(0, NubesGenerador.SUELO + 60, 0);
        e.getSettings().setSpawn(p, 180f);
        GameRules r = lvl.getGameRules();
        r.getRule(GameRules.RULE_SPAWN_RADIUS).set(0, lvl.getServer());
        r.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, lvl.getServer());
        r.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(false, lvl.getServer());
        r.getRule(GameRules.RULE_DOINSOMNIA).set(false, lvl.getServer());
        DarkCraftMod.LOGGER.info("[DarkCraft] Mundo de nubes: se aparece en {}", p.toShortString());
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onVivo(LivingEvent.LivingTickEvent e) {
        LivingEntity le = e.getEntity();
        if (le.getY() > NubesGenerador.MEDIA || le.level().isClientSide() || !le.isAlive()) return;
        if (le instanceof Player p && (p.isCreative() || p.isSpectator())) return;
        if (!NubesGenerador.es(le.level())) return;
        if (le.getY() > NubesGenerador.fondo(Mth.floor(le.getX()), Mth.floor(le.getZ())) - VACIO) return;
        le.hurt(le.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
    }
}
