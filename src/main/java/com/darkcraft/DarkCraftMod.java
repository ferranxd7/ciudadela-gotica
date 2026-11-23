package com.darkcraft;

import com.darkcraft.cine.Cinematics;
import com.darkcraft.net.Net;
import com.darkcraft.story.MapaItem;
import com.darkcraft.story.StoryEvents;
import com.darkcraft.rey.RastrilloBlock;
import com.darkcraft.rey.Rey;
import com.darkcraft.rey.ReyEventos;
import com.darkcraft.rey.ReyProyectil;
import com.darkcraft.rey.Victoria;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import com.darkcraft.world.CastleFeature;
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

@Mod(DarkCraftMod.MODID)
public class DarkCraftMod {
    public static final String MODID = "darkcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CASTLE = FEATURES.register("valdrenn", CastleFeature::new);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> MAPA = ITEMS.register("mapa_valdrenn",
            () -> new MapaItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<SoundEvent> CAMPANA = sound("campana");
    public static final RegistryObject<SoundEvent> NARRACION_1 = sound("narracion.cap1");
    public static final RegistryObject<SoundEvent> NARRACION_5 = sound("narracion.cap5");
    public static final RegistryObject<SoundEvent> MUSICA_COFRE = sound("musica.cofre");
    public static final RegistryObject<SoundEvent> MUSICA_REY_1 = sound("musica.rey1");
    public static final RegistryObject<SoundEvent> MUSICA_REY_2 = sound("musica.rey2");
    public static final RegistryObject<SoundEvent> MUSICA_REY_3 = sound("musica.rey3");
    public static final RegistryObject<SoundEvent> MUSICA_GAVRAN_1 = sound("musica.gavran1");
    public static final RegistryObject<SoundEvent> MUSICA_GAVRAN_2 = sound("musica.gavran2");

    // efectos de sonido de ElevenLabs (ci/eleven): sustituyen a los del juego donde encajan
    public static final RegistryObject<SoundEvent> SFX_TAJO = sound("sfx.rey_tajo");
    public static final RegistryObject<SoundEvent> SFX_TAJO_FUEGO = sound("sfx.rey_tajo_fuego");
    public static final RegistryObject<SoundEvent> SFX_BOLA_FUEGO = sound("sfx.rey_bola_fuego");
    public static final RegistryObject<SoundEvent> SFX_EXPLOSION = sound("sfx.rey_explosion");
    public static final RegistryObject<SoundEvent> SFX_GOLPE_SUELO = sound("sfx.rey_golpe_suelo");
    public static final RegistryObject<SoundEvent> SFX_GOLPE_FINAL = sound("sfx.rey_golpe_final");
    public static final RegistryObject<SoundEvent> SFX_RUGIDO = sound("sfx.rey_rugido");
    public static final RegistryObject<SoundEvent> SFX_GEISER = sound("sfx.rey_geiser");
    public static final RegistryObject<SoundEvent> SFX_PILARES = sound("sfx.rey_pilares");
    public static final RegistryObject<SoundEvent> SFX_CARGA = sound("sfx.rey_carga");
    public static final RegistryObject<SoundEvent> SFX_CUENTA = sound("sfx.cuenta_atras");
    public static final RegistryObject<SoundEvent> SFX_CIRCULO = sound("sfx.circulo_dorado");
    public static final RegistryObject<SoundEvent> SFX_PUERTA_RUNAS = sound("sfx.puerta_runas");
    public static final RegistryObject<SoundEvent> SFX_PUERTA_ABRE = sound("sfx.puerta_abre");
    public static final RegistryObject<SoundEvent> SFX_PUERTA_CIERRA = sound("sfx.puerta_cierra");
    public static final RegistryObject<SoundEvent> SFX_ASCENSOR_MUEVE = sound("sfx.ascensor_mueve");
    public static final RegistryObject<SoundEvent> SFX_ASCENSOR_LLEGA = sound("sfx.ascensor_llega");
    // Ser Gavran
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_TAJO = sound("sfx.gavran_tajo");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_CHOQUE = sound("sfx.gavran_choque");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_EMBESTIDA = sound("sfx.gavran_embestida");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_RAICES = sound("sfx.gavran_raices");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_CLAVAR = sound("sfx.gavran_clavar");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_INVOCAR = sound("sfx.gavran_invocar");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_PASO = sound("sfx.gavran_paso");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_GUARDIA_ROTA = sound("sfx.gavran_guardia_rota");
    public static final RegistryObject<SoundEvent> SFX_GAVRAN_SALTO = sound("sfx.gavran_salto");
    public static final RegistryObject<SoundEvent> SFX_ARBOL_DESPIERTA = sound("sfx.arbol_despierta");
    public static final RegistryObject<SoundEvent> SFX_HOJAS_CORTANTES = sound("sfx.hojas_cortantes");
    public static final RegistryObject<SoundEvent> SFX_RAICES_ATRAPAN = sound("sfx.raices_atrapan");
    public static final RegistryObject<SoundEvent> SFX_ESPINAS = sound("sfx.espinas");
    public static final RegistryObject<SoundEvent> SFX_CORTEZA_MURO = sound("sfx.corteza_muro");
    public static final RegistryObject<SoundEvent> SFX_FLORECER = sound("sfx.florecer");
    public static final RegistryObject<SoundEvent> SFX_MADERA_CRUJE = sound("sfx.madera_cruje");
    public static final RegistryObject<SoundEvent> SFX_CORAZON_LATIDO = sound("sfx.corazon_latido");
    public static final RegistryObject<SoundEvent> SFX_HOJAS_DISOLVER = sound("sfx.hojas_disolver");

    /** Frases del Rey (1..13; la 13 es la 7 con la voz de monstruo de la fase 3). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] REY_VOZ = new RegistryObject[14];

    /** Frases de Ser Gavran (1..18). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] GAVRAN_VOZ = new RegistryObject[19];

    static {
        for (int i = 1; i < REY_VOZ.length; i++) REY_VOZ[i] = sound("rey.voz." + i);
        for (int i = 1; i < GAVRAN_VOZ.length; i++) GAVRAN_VOZ[i] = sound("gavran.voz." + i);
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

    /** Raiz de Gavran: las estacas de sus ataques (entidades block_display) y los muros de corteza del Juicio (se deshacen solos). */
    public static final RegistryObject<Block> RAIZ_GAVRAN = BLOCKS.register("raiz_gavran", () -> new com.darkcraft.gavran.RaizBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(-1.0F, 3600000.0F).noLootTable()
                    .sound(SoundType.WOOD).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)));

    public static final RegistryObject<Item> ESPADA_DIESTRA = ITEMS.register("espada_diestra",
            () -> new com.darkcraft.armas.EspadaRey(true, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ESPADA_SINIESTRA = ITEMS.register("espada_siniestra",
            () -> new com.darkcraft.armas.EspadaRey(false, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANDOBLE_JURAMENTO = ITEMS.register("mandoble_juramento",
            () -> new com.darkcraft.armas.MandobleJuramento(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LLAVE_HUESO = ITEMS.register("llave_hueso",
            () -> new com.darkcraft.armas.LlaveHueso(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LLAVE_ARBOL = ITEMS.register("llave_arbol",
            () -> new com.darkcraft.armas.LlaveArbol(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<Rey>> REY = ENTITIES.register("rey", () -> EntityType.Builder.<Rey>of(Rey::new, MobCategory.MONSTER)
            .sized(1.4f, 3.1f).fireImmune().clientTrackingRange(10).updateInterval(1).build(MODID + ":rey"));
    public static final RegistryObject<EntityType<ReyProyectil>> REY_PROYECTIL = ENTITIES.register("rey_proyectil",
            () -> EntityType.Builder.<ReyProyectil>of(ReyProyectil::new, MobCategory.MISC).sized(0.8f, 0.8f).fireImmune().noSave()
                    .clientTrackingRange(8).updateInterval(1).build(MODID + ":rey_proyectil"));

    public static final RegistryObject<EntityType<com.darkcraft.gavran.Gavran>> GAVRAN = ENTITIES.register("gavran",
            () -> EntityType.Builder.<com.darkcraft.gavran.Gavran>of(com.darkcraft.gavran.Gavran::new, MobCategory.MONSTER)
                    .sized(2.4f, 5.6f).fireImmune().clientTrackingRange(10).updateInterval(1).build(MODID + ":gavran"));

    public static final RegistryObject<EntityType<com.darkcraft.arbol.Ascensor>> ASCENSOR = ENTITIES.register("ascensor",
            () -> EntityType.Builder.<com.darkcraft.arbol.Ascensor>of(com.darkcraft.arbol.Ascensor::new, MobCategory.MISC).sized(5f, 0.3f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(20).build(MODID + ":ascensor"));
    public static final RegistryObject<EntityType<com.darkcraft.arbol.PuertaArbol>> PUERTA_ARBOL = ENTITIES.register("puerta_arbol",
            () -> EntityType.Builder.<com.darkcraft.arbol.PuertaArbol>of(com.darkcraft.arbol.PuertaArbol::new, MobCategory.MISC).sized(7f, 10f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(40).build(MODID + ":puerta_arbol"));

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, name)));
    }

    public DarkCraftMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        FEATURES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        BLOCKS.register(modBus);
        ENTITIES.register(modBus);
        com.darkcraft.world.Valdrenn.registrar(modBus);
        modBus.addListener(this::attributes);
        modBus.addListener(this::setup);
        modBus.addListener(this::tabs);
        MinecraftForge.EVENT_BUS.register(ModEvents.class);
        MinecraftForge.EVENT_BUS.register(Comandos.class);
        MinecraftForge.EVENT_BUS.register(StoryEvents.class);
        MinecraftForge.EVENT_BUS.register(Cinematics.class);
        MinecraftForge.EVENT_BUS.register(ReyEventos.class);
        MinecraftForge.EVENT_BUS.register(Victoria.class);
        MinecraftForge.EVENT_BUS.register(com.darkcraft.rey.Volcan.class);
        MinecraftForge.EVENT_BUS.register(com.darkcraft.arbol.TorreArbol.class);
        MinecraftForge.EVENT_BUS.register(com.darkcraft.armas.Combos.class);
        MinecraftForge.EVENT_BUS.register(com.darkcraft.armas.EfectosRaiz.class);
        MinecraftForge.EVENT_BUS.register(com.darkcraft.gavran.GavranEventos.class);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(REY.get(), Rey.attributes().build());
        e.put(GAVRAN.get(), com.darkcraft.gavran.Gavran.attributes().build());
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }

    private void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            e.accept(MAPA);
            e.accept(LLAVE_ARBOL);
            e.accept(LLAVE_HUESO);
        }
        if (e.getTabKey() == CreativeModeTabs.COMBAT) {
            e.accept(MANDOBLE_JURAMENTO);
            e.accept(ESPADA_DIESTRA);
            e.accept(ESPADA_SINIESTRA);
        }
    }
}
