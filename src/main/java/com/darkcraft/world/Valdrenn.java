package com.darkcraft.world;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.CastlePlan;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

/**
 * La estructura "darkcraft:valdrenn": no construye nada (el reino lo levanta CastleFeature), solo deja una marca
 * en el centro de cada Reino de Valdrenn para que funcione {@code /locate structure darkcraft:valdrenn}. Usa la
 * misma rejilla que {@link CastleSites} (regiones de 2400 bloques = 150 chunks).
 */
public final class Valdrenn {
    private Valdrenn() {}

    public static final DeferredRegister<StructureType<?>> TIPOS = DeferredRegister.create(Registries.STRUCTURE_TYPE, DarkCraftMod.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> COLOCACIONES = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, DarkCraftMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIEZAS = DeferredRegister.create(Registries.STRUCTURE_PIECE, DarkCraftMod.MODID);

    public static final RegistryObject<StructureType<Estructura>> TIPO = TIPOS.register("valdrenn", () -> () -> Estructura.CODEC);
    public static final RegistryObject<StructurePlacementType<Sitios>> SITIOS = COLOCACIONES.register("valdrenn_sitios", () -> () -> Sitios.CODEC);
    public static final RegistryObject<StructurePieceType> MARCA = PIEZAS.register("valdrenn_marca", () -> Marca::new);

    public static void registrar(IEventBus bus) {
        TIPOS.register(bus);
        COLOCACIONES.register(bus);
        PIEZAS.register(bus);
    }

    /** Centro del reino de la region donde cae el chunk, o null si esa region no tiene reino. */
    static int[] sitio(long seed, int chunkX, int chunkZ) {
        return CastleSites.site(seed, Math.floorDiv(chunkX, Sitios.CELDA), Math.floorDiv(chunkZ, Sitios.CELDA));
    }

    /** Colocacion: un posible sitio por region, justo el chunk del centro del reino. */
    public static final class Sitios extends RandomSpreadStructurePlacement {
        public static final Codec<Sitios> CODEC = Codec.unit(Sitios::new);
        static final int CELDA = CastleSites.REGION / 16;

        public Sitios() {
            super(CELDA, CELDA - 1, RandomSpreadType.LINEAR, 1571286);
        }

        @Override
        public ChunkPos getPotentialStructureChunk(long seed, int chunkX, int chunkZ) {
            int[] s = sitio(seed, chunkX, chunkZ);
            if (s == null) return new ChunkPos(Math.floorDiv(chunkX, CELDA) * CELDA, Math.floorDiv(chunkZ, CELDA) * CELDA);
            return new ChunkPos(s[0] >> 4, s[1] >> 4);
        }

        @Override
        public StructurePlacementType<?> type() {
            return SITIOS.get();
        }
    }

    /** La estructura: solo existe en el chunk del centro de un reino de verdad. */
    public static final class Estructura extends Structure {
        public static final Codec<Estructura> CODEC = simpleCodec(Estructura::new);

        public Estructura(StructureSettings settings) {
            super(settings);
        }

        /** Sin mirar el bioma: el reino pinta el suyo propio (la tormenta) despues de colocarse. */
        @Override
        public Optional<GenerationStub> findValidGenerationPoint(GenerationContext c) {
            return findGenerationPoint(c);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
            ChunkPos cp = c.chunkPos();
            int[] s = sitio(c.seed(), cp.x, cp.z);
            if (s == null || (s[0] >> 4) != cp.x || (s[1] >> 4) != cp.z) return Optional.empty();
            BlockPos pos = new BlockPos(s[0], CastlePlan.Y0, s[1]);
            return Optional.of(new GenerationStub(pos, b -> b.addPiece(new Marca(pos))));
        }

        @Override
        public StructureType<?> type() {
            return TIPO.get();
        }
    }

    /** Pieza vacia de un bloque: marca el centro, no coloca nada. */
    public static final class Marca extends StructurePiece {
        Marca(BlockPos p) {
            super(MARCA.get(), 0, new BoundingBox(p.getX(), p.getY(), p.getZ(), p.getX(), p.getY(), p.getZ()));
        }

        public Marca(StructurePieceSerializationContext ctx, CompoundTag tag) {
            super(MARCA.get(), tag);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator gen, RandomSource random, BoundingBox box,
                                ChunkPos chunk, BlockPos pivot) {
        }
    }
}
