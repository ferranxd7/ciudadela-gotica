package com.darkcraft.bloques;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Los bloques nuevos de la gran reforma (parte 8b en adelante): cada material de {@link CatalogoBloques} (que escribe
 * {@code tools/bloques/bloques.py} junto con sus texturas de 64 x 64, estados, modelos, botin, etiquetas y nombres) con
 * todas sus formas: el bloque entero ({@code <id>}, con variantes de textura al azar), la escalera ({@code _escalera}), la
 * losa ({@code _losa}), el muro ({@code _muro}), la valla ({@code _valla}), la cancela ({@code _cancela}), la puerta
 * ({@code _puerta}) y la trampilla ({@code _trampilla}); columnas y vigas son un bloque con eje ({@code RotatedPillarBlock});
 * las plantas ({@link Planta}), las capas de nieve ({@code SnowLayerBlock}), el hielo ({@code HalfTransparentBlock}) y, de
 * los arboles (parte 8e), las ramas ({@link Rama}), las hojas ({@link Hojas}), lo que cuelga ({@link Colgante}), las lianas
 * ({@code VineBlock}) y la hojarasca ({@code CarpetBlock}) van sin sufijo; los troncos son columnas.
 * Todos con su objeto y en la pestana del creativo "Dark Craft · Bloques".
 */
public final class Bloques {
    private Bloques() {}

    public static final DeferredRegister<Block> BLOQUES = DeferredRegister.create(ForgeRegistries.BLOCKS, DarkCraftMod.MODID);
    public static final DeferredRegister<Item> OBJETOS = DeferredRegister.create(ForgeRegistries.ITEMS, DarkCraftMod.MODID);
    public static final DeferredRegister<CreativeModeTab> PESTANAS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DarkCraftMod.MODID);

    /** Todos los bloques nuevos por id, en el orden del catalogo (cada material: entero, escalera, losa, muro). */
    private static final Map<String, RegistryObject<Block>> TODOS = new LinkedHashMap<>();
    /** Los ids de cada material, en orden. */
    private static final Map<String, List<String>> FORMAS = new LinkedHashMap<>();

    static {
        for (CatalogoBloques.Material m : CatalogoBloques.MATERIALES) alta(m);
    }

    public static final RegistryObject<CreativeModeTab> PESTANA = PESTANAS.register("bloques", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.darkcraft.bloques"))
            .icon(() -> new ItemStack(TODOS.values().iterator().next().get()))
            .displayItems((p, out) -> {
                for (RegistryObject<Block> b : TODOS.values()) out.accept(b.get());
            })
            .build());

    public static void registrar(IEventBus bus) {
        BLOQUES.register(bus);
        OBJETOS.register(bus);
        PESTANAS.register(bus);
    }

    private static BlockBehaviour.Properties props(CatalogoBloques.Material m) {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(m.mapa()).sound(m.sonido())
                .strength(m.dureza(), m.pico() ? 6.0F : 3.0F);
        return m.pico() ? p.requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)
                : p.instrument(NoteBlockInstrument.BASS).ignitedByLava();
    }

    private static void alta(CatalogoBloques.Material m) {
        String f = m.formas();
        List<String> ids = new ArrayList<>();
        FORMAS.put(m.id(), ids);
        if (f.indexOf('C') >= 0) {
            reg(ids, m.id(), () -> new RotatedPillarBlock(props(m)));
            return;
        }
        if (f.indexOf('X') >= 0) {
            // una planta: cruz sin choque, se rompe al instante, se mueve un poco en cada sitio
            reg(ids, m.id(), () -> new Planta(BlockBehaviour.Properties.of().mapColor(m.mapa()).sound(m.sonido()).replaceable()
                    .noCollission().instabreak().offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));
            return;
        }
        if (f.indexOf('N') >= 0) {
            // capas de nieve (1-8), como las del juego
            reg(ids, m.id(), () -> new SnowLayerBlock(BlockBehaviour.Properties.of().mapColor(m.mapa()).sound(m.sonido())
                    .replaceable().forceSolidOff().randomTicks().strength(0.1F)
                    .isViewBlocking((st, l, p) -> st.getValue(SnowLayerBlock.LAYERS) >= 8).pushReaction(PushReaction.DESTROY)));
            return;
        }
        if (f.indexOf('R') >= 0) {
            // una rama: palo de 8 pixeles que se une por sus seis caras
            reg(ids, m.id(), () -> new Rama(props(m).forceSolidOff()));
            return;
        }
        if (f.indexOf('O') >= 0) {
            // hojas: con huecos, no tapan la vista, no se caen
            reg(ids, m.id(), () -> new Hojas(BlockBehaviour.Properties.of().mapColor(m.mapa()).sound(m.sonido()).strength(m.dureza())
                    .noOcclusion().isValidSpawn((st, l, p, t) -> false).isSuffocating((st, l, p) -> false)
                    .isViewBlocking((st, l, p) -> false).isRedstoneConductor((st, l, p) -> false).ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));
            return;
        }
        if (f.indexOf('G') >= 0) {
            // lo que cuelga: cruz sin choque que se sostiene de lo de arriba
            reg(ids, m.id(), () -> new Colgante(BlockBehaviour.Properties.of().mapColor(m.mapa()).sound(m.sonido()).replaceable()
                    .noCollission().instabreak().ignitedByLava().pushReaction(PushReaction.DESTROY)));
            return;
        }
        if (f.indexOf('Y') >= 0) {
            // una liana: como la enredadera (sin ticks al azar: no crece sola)
            reg(ids, m.id(), () -> new VineBlock(BlockBehaviour.Properties.of().mapColor(m.mapa()).sound(m.sonido()).replaceable()
                    .noCollission().strength(m.dureza()).ignitedByLava().pushReaction(PushReaction.DESTROY)));
            return;
        }
        if (f.indexOf('A') >= 0) {
            // hojarasca: una alfombra de un pixel con huecos (no tapa la cara de arriba del suelo)
            reg(ids, m.id(), () -> new CarpetBlock(BlockBehaviour.Properties.of().mapColor(m.mapa()).sound(m.sonido())
                    .strength(m.dureza()).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
            return;
        }
        if (f.indexOf('H') >= 0) {
            // hielo: translucido y resbaladizo (no se derrite en agua: es un bloque como otro)
            reg(ids, m.id(), () -> new HalfTransparentBlock(props(m).friction(0.98F).noOcclusion()
                    .isValidSpawn((st, l, p, t) -> false).isRedstoneConductor((st, l, p) -> false)
                    .isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false)));
            return;
        }
        RegistryObject<Block> base = reg(ids, m.id(), () -> new Block(props(m)));
        if (f.indexOf('E') >= 0) reg(ids, m.id() + "_escalera", () -> new StairBlock(() -> base.get().defaultBlockState(), props(m)));
        if (f.indexOf('L') >= 0) reg(ids, m.id() + "_losa", () -> new SlabBlock(props(m)));
        if (f.indexOf('M') >= 0) reg(ids, m.id() + "_muro", () -> new WallBlock(props(m).forceSolidOn()));
        if (f.indexOf('V') >= 0) reg(ids, m.id() + "_valla", () -> new FenceBlock(props(m).forceSolidOn()));
        if (f.indexOf('P') >= 0) reg(ids, m.id() + "_cancela", () -> new FenceGateBlock(props(m).forceSolidOn(), m.madera()));
        if (f.indexOf('D') >= 0) regPuerta(ids, m.id() + "_puerta", () -> new DoorBlock(props(m).noOcclusion(), m.juego()));
        if (f.indexOf('T') >= 0) reg(ids, m.id() + "_trampilla", () -> new TrapDoorBlock(props(m).noOcclusion()
                .isValidSpawn((st, l, p, t) -> false), m.juego()));
    }

    private static RegistryObject<Block> reg(List<String> ids, String id, Supplier<Block> s) {
        RegistryObject<Block> b = BLOQUES.register(id, s);
        OBJETOS.register(id, () -> new BlockItem(b.get(), new Item.Properties()));
        TODOS.put(id, b);
        ids.add(id);
        return b;
    }

    /** Las puertas, con el objeto de las puertas (pone las dos mitades). */
    private static RegistryObject<Block> regPuerta(List<String> ids, String id, Supplier<Block> s) {
        RegistryObject<Block> b = BLOQUES.register(id, s);
        OBJETOS.register(id, () -> new DoubleHighBlockItem(b.get(), new Item.Properties()));
        TODOS.put(id, b);
        ids.add(id);
        return b;
    }

    /** El bloque con ese id (sin el "darkcraft:"), o null si no es de los nuevos. */
    public static Block de(String id) {
        RegistryObject<Block> b = TODOS.get(id);
        return b == null ? null : b.get();
    }

    /** Los ids de todos los bloques nuevos, en orden. */
    public static List<String> ids() {
        return Collections.unmodifiableList(new ArrayList<>(TODOS.keySet()));
    }

    /** Los ids de las formas de un material (el entero primero), o una lista vacia. */
    public static List<String> formas(String material) {
        List<String> l = FORMAS.get(material);
        return l == null ? List.of() : Collections.unmodifiableList(l);
    }

    /** Cuantos bloques nuevos hay. */
    public static int cuantos() {
        return TODOS.size();
    }
}
