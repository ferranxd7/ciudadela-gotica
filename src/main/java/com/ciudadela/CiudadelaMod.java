package com.ciudadela;

import com.ciudadela.cine.Cinematics;
import com.ciudadela.net.Net;
import com.ciudadela.story.MapaItem;
import com.ciudadela.story.StoryEvents;
import com.ciudadela.rey.RastrilloBlock;
import com.ciudadela.rey.Rey;
import com.ciudadela.rey.ReyEventos;
import com.ciudadela.rey.ReyProyectil;
import com.ciudadela.rey.Victoria;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import com.ciudadela.world.CastleFeature;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(CiudadelaMod.MODID)
public class CiudadelaMod {
    public static final String MODID = "ciudadela";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CASTLE = FEATURES.register("ciudadela", CastleFeature::new);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> MAPA = ITEMS.register("mapa_ciudadela",
            () -> new MapaItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<SoundEvent> CAMPANA = sound("campana");
    public static final RegistryObject<SoundEvent> NARRACION_1 = sound("narracion.cap1");
    public static final RegistryObject<SoundEvent> MUSICA_COFRE = sound("musica.cofre");
    public static final RegistryObject<SoundEvent> MUSICA_REY_1 = sound("musica.rey1");
    public static final RegistryObject<SoundEvent> MUSICA_REY_2 = sound("musica.rey2");
    public static final RegistryObject<SoundEvent> MUSICA_REY_3 = sound("musica.rey3");

    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] REY_VOZ = new RegistryObject[13];

    static {
        for (int i = 1; i < REY_VOZ.length; i++) REY_VOZ[i] = sound("rey.voz." + i);
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> RASTRILLO = BLOCKS.register("rastrillo", () -> new RastrilloBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable()
                    .noOcclusion().sound(SoundType.CHAIN).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)));

    public static final RegistryObject<Block> SELLO = BLOCKS.register("sello_runico", () -> new net.minecraft.world.level.block.HalfTransparentBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
                    .lightLevel(st -> 11).sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK)
                    .isValidSpawn((st, l, p, t) -> false).isRedstoneConductor((st, l, p) -> false)
                    .isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false)));

    public static final RegistryObject<Item> ESPADA_DIESTRA = ITEMS.register("espada_diestra",
            () -> new com.ciudadela.armas.EspadaRey(true, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ESPADA_SINIESTRA = ITEMS.register("espada_siniestra",
            () -> new com.ciudadela.armas.EspadaRey(false, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LLAVE_ARBOL = ITEMS.register("llave_arbol",
            () -> new com.ciudadela.armas.LlaveArbol(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<Rey>> REY = ENTITIES.register("rey", () -> EntityType.Builder.<Rey>of(Rey::new, MobCategory.MONSTER)
            .sized(1.4f, 3.1f).fireImmune().clientTrackingRange(10).updateInterval(1).build(MODID + ":rey"));
    public static final RegistryObject<EntityType<ReyProyectil>> REY_PROYECTIL = ENTITIES.register("rey_proyectil",
            () -> EntityType.Builder.<ReyProyectil>of(ReyProyectil::new, MobCategory.MISC).sized(0.8f, 0.8f).fireImmune().noSave()
                    .clientTrackingRange(8).updateInterval(1).build(MODID + ":rey_proyectil"));

    public static final RegistryObject<EntityType<com.ciudadela.arbol.Ascensor>> ASCENSOR = ENTITIES.register("ascensor",
            () -> EntityType.Builder.<com.ciudadela.arbol.Ascensor>of(com.ciudadela.arbol.Ascensor::new, MobCategory.MISC).sized(5f, 0.3f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(20).build(MODID + ":ascensor"));
    public static final RegistryObject<EntityType<com.ciudadela.arbol.PuertaArbol>> PUERTA_ARBOL = ENTITIES.register("puerta_arbol",
            () -> EntityType.Builder.<com.ciudadela.arbol.PuertaArbol>of(com.ciudadela.arbol.PuertaArbol::new, MobCategory.MISC).sized(7f, 10f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(40).build(MODID + ":puerta_arbol"));

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, name)));
    }

    public CiudadelaMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        FEATURES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        BLOCKS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(this::attributes);
        modBus.addListener(this::setup);
        modBus.addListener(this::tabs);
        MinecraftForge.EVENT_BUS.register(ModEvents.class);
        MinecraftForge.EVENT_BUS.register(StoryEvents.class);
        MinecraftForge.EVENT_BUS.register(Cinematics.class);
        MinecraftForge.EVENT_BUS.register(ReyEventos.class);
        MinecraftForge.EVENT_BUS.register(Victoria.class);
        MinecraftForge.EVENT_BUS.register(com.ciudadela.rey.Volcan.class);
        MinecraftForge.EVENT_BUS.register(com.ciudadela.arbol.TorreArbol.class);
        MinecraftForge.EVENT_BUS.register(com.ciudadela.armas.Combos.class);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(REY.get(), Rey.attributes().build());
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }

    private void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            e.accept(MAPA);
            e.accept(LLAVE_ARBOL);
        }
        if (e.getTabKey() == CreativeModeTabs.COMBAT) {
            e.accept(ESPADA_DIESTRA);
            e.accept(ESPADA_SINIESTRA);
        }
    }
}
