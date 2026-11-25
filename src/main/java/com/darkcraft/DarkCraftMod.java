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
    public static final RegistryObject<SoundEvent> NARRACION_6 = sound("narracion.cap6");
    public static final RegistryObject<SoundEvent> MUSICA_FORTALEZA = sound("musica.fortaleza");
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
    // Espejo de la Sombra y libro de la M
    public static final RegistryObject<SoundEvent> SFX_ESPEJO_DESPIERTA = sound("sfx.espejo_despierta");
    public static final RegistryObject<SoundEvent> SFX_PLUMA = sound("sfx.pluma_escribe");
    public static final RegistryObject<SoundEvent> SFX_LIBRO_ABRE = sound("sfx.libro_abre");
    // caballeros zombi
    public static final RegistryObject<SoundEvent> SFX_CABALLERO_GRUNIDO = sound("sfx.zombi_caballero_grunido");
    public static final RegistryObject<SoundEvent> SFX_CABALLERO_DOLOR = sound("sfx.zombi_caballero_dolor");
    public static final RegistryObject<SoundEvent> SFX_CABALLERO_MUERTE = sound("sfx.zombi_caballero_muerte");
    public static final RegistryObject<SoundEvent> SFX_ARMADURA_PASO = sound("sfx.armadura_paso");
    public static final RegistryObject<SoundEvent> SFX_ESCUDO_GOLPE = sound("sfx.escudo_golpe");
    public static final RegistryObject<SoundEvent> SFX_ALABARDA_TAJO = sound("sfx.alabarda_tajo");
    // Parte 3: la Fortaleza de Hueso
    public static final RegistryObject<SoundEvent> SFX_LLAVE_RASTRO = sound("sfx.llave_rastro");
    public static final RegistryObject<SoundEvent> SFX_PUENTE_HUESO = sound("sfx.puente_hueso");
    public static final RegistryObject<SoundEvent> SFX_PUERTA_HUESO = sound("sfx.puerta_hueso");
    public static final RegistryObject<SoundEvent> SFX_AMULETO_FORJA = sound("sfx.amuleto_forja");
    public static final RegistryObject<SoundEvent> SFX_PORTAL_ABRE = sound("sfx.portal_abre");
    // el HUD nuevo y el rol: carteles, ecos, espada de luz y guardia rota
    public static final RegistryObject<SoundEvent> SFX_HAS_MUERTO = sound("sfx.has_muerto");
    public static final RegistryObject<SoundEvent> SFX_JEFE_ABATIDO = sound("sfx.jefe_abatido");
    public static final RegistryObject<SoundEvent> SFX_ZONA_DESCUBIERTA = sound("sfx.zona_descubierta");
    public static final RegistryObject<SoundEvent> SFX_ECOS_RECUPERADOS = sound("sfx.ecos_recuperados");
    public static final RegistryObject<SoundEvent> SFX_ECOS_GANADOS = sound("sfx.ecos_ganados");
    public static final RegistryObject<SoundEvent> SFX_ESPADA_LUZ = sound("sfx.espada_luz");
    public static final RegistryObject<SoundEvent> SFX_GUARDIA_ROTA = sound("sfx.guardia_rota");
    // santuarios, frascos, subir de nivel y los menus
    public static final RegistryObject<SoundEvent> SFX_SANTUARIO_ENCIENDE = sound("sfx.santuario_enciende");
    public static final RegistryObject<SoundEvent> SFX_SANTUARIO_FUEGO = sound("sfx.santuario_fuego");
    public static final RegistryObject<SoundEvent> SFX_SANTUARIO_DESCANSO = sound("sfx.santuario_descanso");
    public static final RegistryObject<SoundEvent> SFX_FRASCO_BEBER = sound("sfx.frasco_beber");
    public static final RegistryObject<SoundEvent> SFX_NIVEL_SUBE = sound("sfx.nivel_sube");
    public static final RegistryObject<SoundEvent> SFX_MENU_ABRE = sound("sfx.menu_abre");
    public static final RegistryObject<SoundEvent> SFX_MENU_MUEVE = sound("sfx.menu_mueve");
    public static final RegistryObject<SoundEvent> SFX_MENU_ACEPTA = sound("sfx.menu_acepta");
    public static final RegistryObject<SoundEvent> SFX_MENU_CANCELA = sound("sfx.menu_cancela");
    // el combate propio
    public static final RegistryObject<SoundEvent> SFX_GOLPE_LIGERO = sound("sfx.golpe_ligero");
    public static final RegistryObject<SoundEvent> SFX_GOLPE_PESADO = sound("sfx.golpe_pesado");
    public static final RegistryObject<SoundEvent> SFX_CARGA_GOLPE = sound("sfx.carga_golpe");
    public static final RegistryObject<SoundEvent> SFX_IMPACTO_CARNE = sound("sfx.impacto_carne");
    public static final RegistryObject<SoundEvent> SFX_IMPACTO_ARMADURA = sound("sfx.impacto_armadura");
    public static final RegistryObject<SoundEvent> SFX_RODAR = sound("sfx.rodar");
    public static final RegistryObject<SoundEvent> SFX_FIJAR = sound("sfx.fijar");
    public static final RegistryObject<SoundEvent> SFX_ESTOCADA = sound("sfx.estocada");
    public static final RegistryObject<SoundEvent> SFX_GOLPE_SALTO = sound("sfx.golpe_salto");
    public static final RegistryObject<SoundEvent> SFX_ATURDIDO = sound("sfx.aturdido");
    public static final RegistryObject<SoundEvent> SFX_ESFUERZO = sound("sfx.esfuerzo");
    public static final RegistryObject<SoundEvent> SFX_COMER_FRUTO = sound("sfx.comer_fruto");
    public static final RegistryObject<SoundEvent> SFX_TALISMAN = sound("sfx.talisman");
    public static final RegistryObject<SoundEvent> SFX_HECHIZO_LANZAR = sound("sfx.hechizo_lanzar");
    public static final RegistryObject<SoundEvent> SFX_HECHIZO_IMPACTO = sound("sfx.hechizo_impacto");
    public static final RegistryObject<SoundEvent> SFX_HECHIZO_RAYO = sound("sfx.hechizo_rayo");
    public static final RegistryObject<SoundEvent> SFX_HECHIZO_LLAMA = sound("sfx.hechizo_llama");
    public static final RegistryObject<SoundEvent> SFX_HECHIZO_CURAR = sound("sfx.hechizo_curar");
    public static final RegistryObject<SoundEvent> SFX_HECHIZO_BENDICION = sound("sfx.hechizo_bendicion");
    // el herrero y el mercader: el martillo en el yunque, mejorar un arma y comprar
    public static final RegistryObject<SoundEvent> SFX_HERRERO_GOLPE = sound("sfx.herrero_golpe");
    public static final RegistryObject<SoundEvent> SFX_FORJA_MEJORA = sound("sfx.forja_mejora");
    public static final RegistryObject<SoundEvent> SFX_TIENDA_COMPRA = sound("sfx.tienda_compra");

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

    /** La reja de hueso de la puerta de la Fortaleza de Hueso (irrompible; sube con la llave). */
    public static final RegistryObject<Block> REJA_HUESO = BLOCKS.register("reja_hueso", () -> new RastrilloBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(-1.0F, 3600000.0F).noLootTable()
                    .noOcclusion().sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)));

    /** El portal de la Sombra en lo alto de la fortaleza (todavia no se puede cruzar). */
    public static final RegistryObject<Block> PORTAL_SOMBRA = BLOCKS.register("portal_sombra", () -> new com.darkcraft.fortaleza.PortalSombraBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(-1.0F, 3600000.0F).noLootTable().noCollission()
                    .noOcclusion().lightLevel(st -> 12).sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK)
                    .isValidSpawn((st, l, p, t) -> false).isRedstoneConductor((st, l, p) -> false)
                    .isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false)));

    /** El santuario: la hoguera de almas donde se descansa (irrompible; apagado da un rescoldo de luz, encendido alumbra). */
    public static final RegistryObject<Block> SANTUARIO = BLOCKS.register("santuario", () -> new com.darkcraft.santuario.SantuarioBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
                    .lightLevel(st -> st.getValue(com.darkcraft.santuario.SantuarioBlock.ENCENDIDO) ? 13 : 3).sound(SoundType.DEEPSLATE_BRICKS)
                    .pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false).isRedstoneConductor((st, l, p) -> false)
                    .isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false)));
    public static final RegistryObject<Item> SANTUARIO_ITEM = ITEMS.register("santuario",
            () -> new net.minecraft.world.item.BlockItem(SANTUARIO.get(), new Item.Properties().rarity(Rarity.EPIC)));
    /** Los frascos no son objetos que se lleven en el inventario (van en la ficha): estos solo sirven para dibujarlos en la mano al beber. */
    public static final RegistryObject<Item> FRASCO_VIDA = ITEMS.register("frasco_vida",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FRASCO_MANA = ITEMS.register("frasco_mana",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> AMULETO_HUESO = ITEMS.register("amuleto_hueso",
            () -> new com.darkcraft.fortaleza.AmuletoHueso(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LIBRO_ACERTIJO = ITEMS.register("libro_acertijo",
            () -> new com.darkcraft.fortaleza.LibroAcertijo(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ESPADA_DIESTRA = ITEMS.register("espada_diestra",
            () -> new com.darkcraft.armas.EspadaRey(true, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ESPADA_SINIESTRA = ITEMS.register("espada_siniestra",
            () -> new com.darkcraft.armas.EspadaRey(false, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANDOBLE_JURAMENTO = ITEMS.register("mandoble_juramento",
            () -> new com.darkcraft.armas.MandobleJuramento(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LLAVE_HUESO = ITEMS.register("llave_hueso",
            () -> new com.darkcraft.armas.LlaveHueso(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ALABARDA = ITEMS.register("alabarda",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.IRON, 5, -3.0f, new Item.Properties()));
    /** La espada de luz que guarda los ecos donde se muere: solo la ensena una entidad (no se puede conseguir). */
    public static final RegistryObject<Item> ESPADA_LUZ = ITEMS.register("espada_luz",
            () -> new Item(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
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

    public static final RegistryObject<EntityType<com.darkcraft.caballeros.CaballeroZombi.Espada>> CABALLERO_ESPADA = ENTITIES.register("caballero_espada",
            () -> EntityType.Builder.<com.darkcraft.caballeros.CaballeroZombi.Espada>of(com.darkcraft.caballeros.CaballeroZombi.Espada::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build(MODID + ":caballero_espada"));
    public static final RegistryObject<EntityType<com.darkcraft.caballeros.CaballeroZombi.Alabarda>> CABALLERO_ALABARDA = ENTITIES.register("caballero_alabarda",
            () -> EntityType.Builder.<com.darkcraft.caballeros.CaballeroZombi.Alabarda>of(com.darkcraft.caballeros.CaballeroZombi.Alabarda::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build(MODID + ":caballero_alabarda"));
    public static final RegistryObject<EntityType<com.darkcraft.caballeros.CaballeroZombi.Ballesta>> CABALLERO_BALLESTA = ENTITIES.register("caballero_ballesta",
            () -> EntityType.Builder.<com.darkcraft.caballeros.CaballeroZombi.Ballesta>of(com.darkcraft.caballeros.CaballeroZombi.Ballesta::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build(MODID + ":caballero_ballesta"));
    public static final RegistryObject<Item> HUEVO_ESPADA = ITEMS.register("caballero_espada_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(CABALLERO_ESPADA, 0x4A6342, 0x8E1418, new Item.Properties()));
    public static final RegistryObject<Item> HUEVO_ALABARDA = ITEMS.register("caballero_alabarda_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(CABALLERO_ALABARDA, 0x4A6342, 0x4A1C5C, new Item.Properties()));
    public static final RegistryObject<Item> HUEVO_BALLESTA = ITEMS.register("caballero_ballesta_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(CABALLERO_BALLESTA, 0x4A6342, 0x2E4A2C, new Item.Properties()));

    public static final RegistryObject<EntityType<com.darkcraft.arbol.Ascensor>> ASCENSOR = ENTITIES.register("ascensor",
            () -> EntityType.Builder.<com.darkcraft.arbol.Ascensor>of(com.darkcraft.arbol.Ascensor::new, MobCategory.MISC).sized(5f, 0.3f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(20).build(MODID + ":ascensor"));
    public static final RegistryObject<EntityType<com.darkcraft.arbol.PuertaArbol>> PUERTA_ARBOL = ENTITIES.register("puerta_arbol",
            () -> EntityType.Builder.<com.darkcraft.arbol.PuertaArbol>of(com.darkcraft.arbol.PuertaArbol::new, MobCategory.MISC).sized(7f, 10f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(40).build(MODID + ":puerta_arbol"));

    /** Maelis, la guia de los santuarios (sentada junto a los santuarios importantes). */
    public static final RegistryObject<EntityType<com.darkcraft.personajes.Guia>> GUIA = ENTITIES.register("guia",
            () -> EntityType.Builder.<com.darkcraft.personajes.Guia>of(com.darkcraft.personajes.Guia::new, MobCategory.MISC)
                    .sized(0.8f, 1.3f).fireImmune().clientTrackingRange(10).build(MODID + ":guia"));

    /** Dorvan, el herrero (de pie ante su yunque, martilleando) y Kerrick, el mercader (con su mochila enorme). */
    public static final RegistryObject<EntityType<com.darkcraft.personajes.Herrero>> HERRERO = ENTITIES.register("herrero",
            () -> EntityType.Builder.<com.darkcraft.personajes.Herrero>of(com.darkcraft.personajes.Herrero::new, MobCategory.MISC)
                    .sized(0.7f, 1.95f).fireImmune().clientTrackingRange(10).build(MODID + ":herrero"));
    public static final RegistryObject<EntityType<com.darkcraft.personajes.Mercader>> MERCADER = ENTITIES.register("mercader",
            () -> EntityType.Builder.<com.darkcraft.personajes.Mercader>of(com.darkcraft.personajes.Mercader::new, MobCategory.MISC)
                    .sized(0.7f, 1.95f).fireImmune().clientTrackingRange(10).build(MODID + ":mercader"));
    /** El martillo de Dorvan (solo para que lo lleve en la mano) y las piedras de forja (I, II y III: mejoran las armas). */
    public static final RegistryObject<Item> MARTILLO_HERRERO = ITEMS.register("martillo_herrero",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PIEDRA_FORJA_1 = ITEMS.register("piedra_forja_1",
            () -> new com.darkcraft.forja.PiedraForja(1, new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PIEDRA_FORJA_2 = ITEMS.register("piedra_forja_2",
            () -> new com.darkcraft.forja.PiedraForja(2, new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PIEDRA_FORJA_3 = ITEMS.register("piedra_forja_3",
            () -> new com.darkcraft.forja.PiedraForja(3, new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)));

    /** Los hechizos que vuelan (guijarro, lanza, esquirlas, rayo): sin modelo, se ven por sus particulas. */
    public static final RegistryObject<EntityType<com.darkcraft.hechizos.ProyectilHechizo>> PROYECTIL = ENTITIES.register("proyectil_hechizo",
            () -> EntityType.Builder.<com.darkcraft.hechizos.ProyectilHechizo>of(com.darkcraft.hechizos.ProyectilHechizo::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).noSave().clientTrackingRange(8).updateInterval(1).build(MODID + ":proyectil_hechizo"));
    /** El baston (hechicerias, Inteligencia) y el sello (milagros, Fe). */
    public static final RegistryObject<Item> BASTON = ITEMS.register("baston",
            () -> new com.darkcraft.hechizos.Catalizador(com.darkcraft.hechizos.Hechizo.Escuela.HECHICERIA, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SELLO_SAGRADO = ITEMS.register("sello",
            () -> new com.darkcraft.hechizos.Catalizador(com.darkcraft.hechizos.Hechizo.Escuela.MILAGRO, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final java.util.EnumMap<com.darkcraft.hechizos.Hechizo, RegistryObject<Item>> HECHIZOS = new java.util.EnumMap<>(com.darkcraft.hechizos.Hechizo.class);

    static {
        for (com.darkcraft.hechizos.Hechizo h : com.darkcraft.hechizos.Hechizo.values())
            HECHIZOS.put(h, ITEMS.register("hechizo_" + h.id, () -> new com.darkcraft.hechizos.HechizoItem(h, new Item.Properties().stacksTo(1).rarity(Rarity.RARE))));
    }

    /** Los frutos (dan resistencias o habilidades un rato) y los talismanes (se llevan en sus huecos). */
    public static final java.util.EnumMap<com.darkcraft.rol.Fruto.Clase, RegistryObject<Item>> FRUTOS = new java.util.EnumMap<>(com.darkcraft.rol.Fruto.Clase.class);
    public static final java.util.EnumMap<com.darkcraft.rol.Talisman.Clase, RegistryObject<Item>> TALISMANES = new java.util.EnumMap<>(com.darkcraft.rol.Talisman.Clase.class);

    static {
        for (com.darkcraft.rol.Fruto.Clase c : com.darkcraft.rol.Fruto.Clase.values())
            FRUTOS.put(c, ITEMS.register("fruto_" + c.id, () -> new com.darkcraft.rol.Fruto(c, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON))));
        for (com.darkcraft.rol.Talisman.Clase c : com.darkcraft.rol.Talisman.Clase.values())
            TALISMANES.put(c, ITEMS.register("talisman_" + c.id, () -> new com.darkcraft.rol.Talisman(c, new Item.Properties().stacksTo(1).rarity(Rarity.RARE))));
    }

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
        com.darkcraft.rol.Efectos.registrar(modBus);
        com.darkcraft.world.Valdrenn.registrar(modBus);
        com.darkcraft.fortaleza.Localizar.registrar(modBus);
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
        MinecraftForge.EVENT_BUS.register(com.darkcraft.world.SinMonstruos.class);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(REY.get(), Rey.attributes().build());
        e.put(GAVRAN.get(), com.darkcraft.gavran.Gavran.attributes().build());
        e.put(CABALLERO_ESPADA.get(), com.darkcraft.caballeros.CaballeroZombi.atributos().build());
        e.put(CABALLERO_ALABARDA.get(), com.darkcraft.caballeros.CaballeroZombi.atributos().build());
        e.put(CABALLERO_BALLESTA.get(), com.darkcraft.caballeros.CaballeroZombi.atributos().build());
        e.put(GUIA.get(), com.darkcraft.personajes.Guia.atributos().build());
        e.put(HERRERO.get(), com.darkcraft.personajes.Herrero.atributos().build());
        e.put(MERCADER.get(), com.darkcraft.personajes.Mercader.atributos().build());
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }

    private void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            e.accept(MAPA);
            e.accept(LLAVE_ARBOL);
            e.accept(LLAVE_HUESO);
            e.accept(AMULETO_HUESO);
            e.accept(LIBRO_ACERTIJO);
            e.accept(SANTUARIO_ITEM);
            e.accept(PIEDRA_FORJA_1);
            e.accept(PIEDRA_FORJA_2);
            e.accept(PIEDRA_FORJA_3);
        }
        if (e.getTabKey() == CreativeModeTabs.COMBAT) {
            e.accept(MANDOBLE_JURAMENTO);
            e.accept(ESPADA_DIESTRA);
            e.accept(ESPADA_SINIESTRA);
            e.accept(ALABARDA);
            for (RegistryObject<Item> t : TALISMANES.values()) e.accept(t);
            e.accept(BASTON);
            e.accept(SELLO_SAGRADO);
            for (RegistryObject<Item> h : HECHIZOS.values()) e.accept(h);
        }
        if (e.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            for (RegistryObject<Item> f : FRUTOS.values()) e.accept(f);
        }
        if (e.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            e.accept(HUEVO_ESPADA);
            e.accept(HUEVO_ALABARDA);
            e.accept(HUEVO_BALLESTA);
        }
    }
}
