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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
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
 * ({@code _puerta}) y la trampilla ({@code _trampilla}); columnas y vigas son un bloque con eje ({@code RotatedPillarBlock}).
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
