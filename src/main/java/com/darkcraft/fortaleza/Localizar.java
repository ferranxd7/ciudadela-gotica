package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
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
 * La estructura "darkcraft:fortaleza_hueso": como la de Valdrenn, no construye nada (la fortaleza la levanta el servidor
 * cuando alguien se acerca) y solo marca su centro para {@code /locate structure darkcraft:fortaleza_hueso}.
 */
public final class Localizar {
    private Localizar() {}

    public static final DeferredRegister<StructureType<?>> TIPOS = DeferredRegister.create(Registries.STRUCTURE_TYPE, DarkCraftMod.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> COLOCACIONES = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, DarkCraftMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIEZAS = DeferredRegister.create(Registries.STRUCTURE_PIECE, DarkCraftMod.MODID);

    public static final RegistryObject<StructureType<Estructura>> TIPO = TIPOS.register("fortaleza_hueso", () -> () -> Estructura.CODEC);
    public static final RegistryObject<StructurePlacementType<Sitios>> SITIOS = COLOCACIONES.register("fortaleza_sitios", () -> () -> Sitios.CODEC);
    public static final RegistryObject<StructurePieceType> MARCA = PIEZAS.register("fortaleza_marca", () -> Marca::new);

    public static void registrar(IEventBus bus) {
        TIPOS.register(bus);
        COLOCACIONES.register(bus);
        PIEZAS.register(bus);
    }

    /** La fortaleza cuyo centro cae en el chunk (x, z), o null. */
    static int[] en(long seed, int chunkX, int chunkZ, int celda) {
        int c0x = Math.floorDiv(chunkX, celda) * celda, c0z = Math.floorDiv(chunkZ, celda) * celda;
        int bx = c0x * 16 + celda * 8, bz = c0z * 16 + celda * 8;
        for (Fortalezas.Sitio s : Fortalezas.cercanas(seed, bx, bz, celda * 12)) {
            int fx = s.x() >> 4, fz = s.z() >> 4;
            if (fx >= c0x && fx < c0x + celda && fz >= c0z && fz < c0z + celda) return new int[]{s.x(), s.z()};
        }
        return null;
    }

    /** Colocacion: el chunk del centro de cada fortaleza, buscado en celdas de 16 chunks (como Valdrenn). */
    public static final class Sitios extends RandomSpreadStructurePlacement {
        public static final Codec<Sitios> CODEC = Codec.unit(Sitios::new);
        static final int CELDA = 16;

        public Sitios() {
            super(CELDA, CELDA - 1, RandomSpreadType.LINEAR, 7140913);
        }

        @Override
        public ChunkPos getPotentialStructureChunk(long seed, int chunkX, int chunkZ) {
            int[] f = en(seed, chunkX, chunkZ, CELDA);
            if (f != null) return new ChunkPos(f[0] >> 4, f[1] >> 4);
            return new ChunkPos(Math.floorDiv(chunkX, CELDA) * CELDA, Math.floorDiv(chunkZ, CELDA) * CELDA);
        }

        @Override
        public StructurePlacementType<?> type() {
            return SITIOS.get();
        }
    }

    public static final class Estructura extends Structure {
        public static final Codec<Estructura> CODEC = simpleCodec(Estructura::new);

        public Estructura(StructureSettings settings) {
            super(settings);
        }

        @Override
        public Optional<GenerationStub> findValidGenerationPoint(GenerationContext c) {
            return findGenerationPoint(c);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
            ChunkPos cp = c.chunkPos();
            int[] f = en(c.seed(), cp.x, cp.z, Sitios.CELDA);
            if (f == null || (f[0] >> 4) != cp.x || (f[1] >> 4) != cp.z) return Optional.empty();
            BlockPos pos = new BlockPos(f[0], Fortalezas.Y, f[1]);
            return Optional.of(new GenerationStub(pos, b -> b.addPiece(new Marca(pos))));
        }

        @Override
        public StructureType<?> type() {
            return TIPO.get();
        }
    }

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
