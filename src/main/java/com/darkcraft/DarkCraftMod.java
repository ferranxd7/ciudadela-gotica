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
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BRUMAVELA = FEATURES.register("brumavela", com.darkcraft.world.PuebloFeature::new);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> MAPA = ITEMS.register("mapa_valdrenn",
            () -> new MapaItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<SoundEvent> CAMPANA = sound("campana");
    public static final RegistryObject<SoundEvent> NARRACION_1 = sound("narracion.cap1");
    public static final RegistryObject<SoundEvent> NARRACION_5 = sound("narracion.cap5");
    public static final RegistryObject<SoundEvent> NARRACION_6 = sound("narracion.cap6");
    public static final RegistryObject<SoundEvent> MUSICA_FORTALEZA = sound("musica.fortaleza");
    public static final RegistryObject<SoundEvent> MUSICA_TITULO = sound("musica.titulo");
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
    /** El capitulo 6 (parte 5e): las nubes que se arremolinan, el rayo rojo, la columna de luz y el cielo que enrojece. */
    public static final RegistryObject<SoundEvent> SFX_CIELO_REMOLINO = sound("sfx.cielo_remolino");
    public static final RegistryObject<SoundEvent> SFX_RAYO_ROJO = sound("sfx.rayo_rojo");
    public static final RegistryObject<SoundEvent> SFX_COLUMNA_ROJA = sound("sfx.columna_roja");
    public static final RegistryObject<SoundEvent> SFX_CIELO_ROJO = sound("sfx.cielo_rojo");
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
    // las habilidades de las armas (cenizas de guerra) y ponerle una ceniza a un arma
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_TORBELLINO = sound("sfx.habilidad_torbellino");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_ESTOCADA = sound("sfx.habilidad_estocada");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_GRITO = sound("sfx.habilidad_grito");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_SISMO = sound("sfx.habilidad_sismo");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_PASO = sound("sfx.habilidad_paso");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_LLAMAS = sound("sfx.habilidad_llamas");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_ESCARCHA = sound("sfx.habilidad_escarcha");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_RELAMPAGO = sound("sfx.habilidad_relampago");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_SANGRE = sound("sfx.habilidad_sangre");
    public static final RegistryObject<SoundEvent> SFX_HABILIDAD_PARADA = sound("sfx.habilidad_parada");
    public static final RegistryObject<SoundEvent> SFX_CENIZA_APLICAR = sound("sfx.ceniza_aplicar");
    // el codice de la historia (parte 2e-4): abrirlo, pasar una pagina y cerrarlo
    public static final RegistryObject<SoundEvent> SFX_CODICE_ABRE = sound("sfx.codice_abre");
    public static final RegistryObject<SoundEvent> SFX_CODICE_PAGINA = sound("sfx.codice_pagina");
    public static final RegistryObject<SoundEvent> SFX_CODICE_CIERRA = sound("sfx.codice_cierra");
    // el aviso de los logros (parte 2f-1)
    public static final RegistryObject<SoundEvent> SFX_LOGRO = sound("sfx.logro");
    public static final RegistryObject<SoundEvent> SFX_PIEZA = sound("sfx.pieza_barco");
    // los marchitos de Morvain (parte 7e)
    public static final RegistryObject<SoundEvent> SFX_MARCHITO_GEMIDO = sound("sfx.marchito_gemido");
    public static final RegistryObject<SoundEvent> SFX_MARCHITO_ALERTA = sound("sfx.marchito_alerta");
    public static final RegistryObject<SoundEvent> SFX_MARCHITO_DOLOR = sound("sfx.marchito_dolor");
    public static final RegistryObject<SoundEvent> SFX_MARCHITO_MUERTE = sound("sfx.marchito_muerte");
    public static final RegistryObject<SoundEvent> SFX_MARCHITO_CAMPANA = sound("sfx.marchito_campana");
    public static final RegistryObject<SoundEvent> SFX_MARCHITO_FAROL = sound("sfx.marchito_farol");
    // la Molinera de Velmor (parte 7f) y su molino
    public static final RegistryObject<SoundEvent> SFX_MOLINERA_GRITO = sound("sfx.molinera_grito");
    public static final RegistryObject<SoundEvent> SFX_MOLINERA_SIEGA = sound("sfx.molinera_siega");
    public static final RegistryObject<SoundEvent> SFX_MOLINERA_GOLPE = sound("sfx.molinera_golpe");
    public static final RegistryObject<SoundEvent> SFX_MOLINERA_GIRO = sound("sfx.molinera_giro");
    public static final RegistryObject<SoundEvent> SFX_MOLINERA_DOLOR = sound("sfx.molinera_dolor");
    public static final RegistryObject<SoundEvent> SFX_MOLINERA_MUERTE = sound("sfx.molinera_muerte");
    public static final RegistryObject<SoundEvent> SFX_MOLINO_VIENTO = sound("sfx.molino_viento");
    // los ahorcados del Bosque Muerto (parte 7g) y el viento de su arboleda
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_SOGA = sound("sfx.ahorcado_soga");
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_GEMIDO = sound("sfx.ahorcado_gemido");
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_ALERTA = sound("sfx.ahorcado_alerta");
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_CAE = sound("sfx.ahorcado_cae");
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_ZARPAZO = sound("sfx.ahorcado_zarpazo");
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_LAZO = sound("sfx.ahorcado_lazo");
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_DOLOR = sound("sfx.ahorcado_dolor");
    public static final RegistryObject<SoundEvent> SFX_AHORCADO_MUERTE = sound("sfx.ahorcado_muerte");
    public static final RegistryObject<SoundEvent> AMBIENTE_ARBOLEDA = sound("ambiente.arboleda");
    // Maese Odran, el Verdugo de la Arboleda (parte 7h)
    public static final RegistryObject<SoundEvent> SFX_VERDUGO_GRITO = sound("sfx.verdugo_grito");
    public static final RegistryObject<SoundEvent> SFX_VERDUGO_HACHAZO = sound("sfx.verdugo_hachazo");
    public static final RegistryObject<SoundEvent> SFX_VERDUGO_BARRIDO = sound("sfx.verdugo_barrido");
    public static final RegistryObject<SoundEvent> SFX_VERDUGO_LAZO = sound("sfx.verdugo_lazo");
    public static final RegistryObject<SoundEvent> SFX_VERDUGO_SALTO = sound("sfx.verdugo_salto");
    public static final RegistryObject<SoundEvent> SFX_VERDUGO_DOLOR = sound("sfx.verdugo_dolor");
    public static final RegistryObject<SoundEvent> SFX_VERDUGO_MUERTE = sound("sfx.verdugo_muerte");
    // los podridos del Pantano Podrido y el ambiente del pantano (parte 7i)
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_GEMIDO = sound("sfx.podrido_gemido");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_ALERTA = sound("sfx.podrido_alerta");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_DOLOR = sound("sfx.podrido_dolor");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_MUERTE = sound("sfx.podrido_muerte");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_ALZARSE = sound("sfx.podrido_alzarse");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_VOMITO = sound("sfx.podrido_vomito");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_FATUO = sound("sfx.podrido_fatuo");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_GAS = sound("sfx.podrido_gas");
    public static final RegistryObject<SoundEvent> SFX_PODRIDO_APLASTAR = sound("sfx.podrido_aplastar");
    public static final RegistryObject<SoundEvent> AMBIENTE_PANTANO = sound("ambiente.pantano");
    // el Barquero de la Laguna Negra (parte 7j)
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_GRITO = sound("sfx.barquero_grito");
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_REMAZO = sound("sfx.barquero_remazo");
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_BARRIDO = sound("sfx.barquero_barrido");
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_BICHERO = sound("sfx.barquero_bichero");
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_LINTERNA = sound("sfx.barquero_linterna");
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_DOLOR = sound("sfx.barquero_dolor");
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_MUERTE = sound("sfx.barquero_muerte");
    public static final RegistryObject<SoundEvent> SFX_BARQUERO_REMO = sound("sfx.barquero_remo");
    // los helados de las Montanas Nevadas, la ventisca y la campana del monasterio (parte 7k)
    public static final RegistryObject<SoundEvent> SFX_HELADO_GEMIDO = sound("sfx.helado_gemido");
    public static final RegistryObject<SoundEvent> SFX_HELADO_ALERTA = sound("sfx.helado_alerta");
    public static final RegistryObject<SoundEvent> SFX_HELADO_DOLOR = sound("sfx.helado_dolor");
    public static final RegistryObject<SoundEvent> SFX_HELADO_MUERTE = sound("sfx.helado_muerte");
    public static final RegistryObject<SoundEvent> SFX_HELADO_ROMPER = sound("sfx.helado_romper");
    public static final RegistryObject<SoundEvent> SFX_HELADO_INCENSARIO = sound("sfx.helado_incensario");
    public static final RegistryObject<SoundEvent> SFX_HELADO_ALIENTO = sound("sfx.helado_aliento");
    public static final RegistryObject<SoundEvent> SFX_HELADO_MARTILLO = sound("sfx.helado_martillo");
    public static final RegistryObject<SoundEvent> SFX_HELADO_ESQUIRLAS = sound("sfx.helado_esquirlas");
    public static final RegistryObject<SoundEvent> SFX_MONASTERIO_CAMPANA = sound("sfx.monasterio_campana");
    public static final RegistryObject<SoundEvent> AMBIENTE_MONTANA = sound("ambiente.montana");
    public static final RegistryObject<SoundEvent> SFX_CRIPTA = sound("sfx.cripta_abre");
    public static final RegistryObject<SoundEvent> SFX_BARCO = sound("sfx.barco_reparado");
    public static final RegistryObject<SoundEvent> AMBIENTE_BRUMAVELA = sound("ambiente.brumavela");
    // los enemigos de Brumavela (parte 3c)
    public static final RegistryObject<SoundEvent> SFX_HUECO_GEMIDO = sound("sfx.hueco_gemido");
    public static final RegistryObject<SoundEvent> SFX_HUECO_ALERTA = sound("sfx.hueco_alerta");
    public static final RegistryObject<SoundEvent> SFX_HUECO_DOLOR = sound("sfx.hueco_dolor");
    public static final RegistryObject<SoundEvent> SFX_HUECO_MUERTE = sound("sfx.hueco_muerte");
    // ---- la guardia de Valdrenn y el arquero esqueleto (parte 4a)
    public static final RegistryObject<SoundEvent> SFX_GUARDIA_ALERTA = sound("sfx.guardia_alerta");
    public static final RegistryObject<SoundEvent> SFX_ARCO_TENSAR = sound("sfx.arco_tensar");
    public static final RegistryObject<SoundEvent> SFX_ARCO_SUELTA = sound("sfx.arco_suelta");
    public static final RegistryObject<SoundEvent> SFX_ARQUERO_ALERTA = sound("sfx.arquero_alerta");
    public static final RegistryObject<SoundEvent> SFX_ARQUERO_HUESOS = sound("sfx.arquero_huesos");
    public static final RegistryObject<SoundEvent> SFX_ARQUERO_DOLOR = sound("sfx.arquero_dolor");
    public static final RegistryObject<SoundEvent> SFX_ARQUERO_MUERTE = sound("sfx.arquero_muerte");
    // ---- el perro de hueso y el espectro de la corte (parte 4b)
    public static final RegistryObject<SoundEvent> SFX_PERRO_GRUNIDO = sound("sfx.perro_grunido");
    public static final RegistryObject<SoundEvent> SFX_PERRO_LADRIDO = sound("sfx.perro_ladrido");
    public static final RegistryObject<SoundEvent> SFX_PERRO_AULLIDO = sound("sfx.perro_aullido");
    public static final RegistryObject<SoundEvent> SFX_PERRO_MORDISCO = sound("sfx.perro_mordisco");
    public static final RegistryObject<SoundEvent> SFX_PERRO_DOLOR = sound("sfx.perro_dolor");
    public static final RegistryObject<SoundEvent> SFX_PERRO_MUERTE = sound("sfx.perro_muerte");
    public static final RegistryObject<SoundEvent> SFX_ESPECTRO_SUSURRO = sound("sfx.espectro_susurro");
    public static final RegistryObject<SoundEvent> SFX_ESPECTRO_ALERTA = sound("sfx.espectro_alerta");
    public static final RegistryObject<SoundEvent> SFX_ESPECTRO_LAMENTO = sound("sfx.espectro_lamento");
    public static final RegistryObject<SoundEvent> SFX_ESPECTRO_ZARPAZO = sound("sfx.espectro_zarpazo");
    public static final RegistryObject<SoundEvent> SFX_ESPECTRO_DESVANECE = sound("sfx.espectro_desvanece");
    public static final RegistryObject<SoundEvent> SFX_ESPECTRO_DOLOR = sound("sfx.espectro_dolor");
    public static final RegistryObject<SoundEvent> SFX_ESPECTRO_MUERTE = sound("sfx.espectro_muerte");
    // ---- el capitan de elite y las emboscadas de Valdrenn (parte 4c)
    public static final RegistryObject<SoundEvent> SFX_ELITE_ALERTA = sound("sfx.elite_alerta");
    public static final RegistryObject<SoundEvent> SFX_ELITE_GRITO = sound("sfx.elite_grito");
    public static final RegistryObject<SoundEvent> SFX_ELITE_DOLOR = sound("sfx.elite_dolor");
    public static final RegistryObject<SoundEvent> SFX_ELITE_MUERTE = sound("sfx.elite_muerte");
    public static final RegistryObject<SoundEvent> SFX_ELITE_IMPACTO = sound("sfx.elite_impacto");
    public static final RegistryObject<SoundEvent> SFX_ELITE_EMBESTIDA = sound("sfx.elite_embestida");
    public static final RegistryObject<SoundEvent> SFX_EMBOSCADA = sound("sfx.emboscada");
    public static final RegistryObject<SoundEvent> SFX_EMBOSCADA_ALZARSE = sound("sfx.emboscada_alzarse");
    // ---- los guardianes: Ser Brandt y Vareth (parte 4d)
    public static final RegistryObject<SoundEvent> SFX_BRANDT_RUGIDO = sound("sfx.brandt_rugido");
    public static final RegistryObject<SoundEvent> SFX_BRANDT_MARTILLAZO = sound("sfx.brandt_martillazo");
    public static final RegistryObject<SoundEvent> SFX_BRANDT_ESCUDO = sound("sfx.brandt_escudo");
    public static final RegistryObject<SoundEvent> SFX_BRANDT_DOLOR = sound("sfx.brandt_dolor");
    public static final RegistryObject<SoundEvent> SFX_BRANDT_MUERTE = sound("sfx.brandt_muerte");
    public static final RegistryObject<SoundEvent> SFX_CORTEZA_RUGIDO = sound("sfx.corteza_rugido");
    public static final RegistryObject<SoundEvent> SFX_CORTEZA_ZARPAZO = sound("sfx.corteza_zarpazo");
    public static final RegistryObject<SoundEvent> SFX_CORTEZA_RAICES = sound("sfx.corteza_raices");
    public static final RegistryObject<SoundEvent> SFX_CORTEZA_ESPINAS = sound("sfx.corteza_espinas");
    public static final RegistryObject<SoundEvent> SFX_CORTEZA_DOLOR = sound("sfx.corteza_dolor");
    public static final RegistryObject<SoundEvent> SFX_CORTEZA_MUERTE = sound("sfx.corteza_muerte");
    public static final RegistryObject<SoundEvent> SFX_SELLO_ROMPE = sound("sfx.sello_rompe");
    // el corcel de Morvain (parte 7b): el silbido, la niebla al llegar, el relincho, los cascos, el salto, deshacerse, el dolor
    public static final RegistryObject<SoundEvent> SFX_CORCEL_LLAMADA = sound("sfx.corcel_llamada");
    public static final RegistryObject<SoundEvent> SFX_CORCEL_APARECE = sound("sfx.corcel_aparece");
    public static final RegistryObject<SoundEvent> SFX_CORCEL_RELINCHO = sound("sfx.corcel_relincho");
    public static final RegistryObject<SoundEvent> SFX_CORCEL_PASO = sound("sfx.corcel_paso");
    public static final RegistryObject<SoundEvent> SFX_CORCEL_SALTO = sound("sfx.corcel_salto");
    public static final RegistryObject<SoundEvent> SFX_CORCEL_DESHACE = sound("sfx.corcel_deshace");
    public static final RegistryObject<SoundEvent> SFX_CORCEL_DOLOR = sound("sfx.corcel_dolor");
    public static final RegistryObject<SoundEvent> SFX_AHOGADO_GORGOTEO = sound("sfx.ahogado_gorgoteo");
    public static final RegistryObject<SoundEvent> SFX_AHOGADO_ALERTA = sound("sfx.ahogado_alerta");
    public static final RegistryObject<SoundEvent> SFX_AHOGADO_DOLOR = sound("sfx.ahogado_dolor");
    public static final RegistryObject<SoundEvent> SFX_AHOGADO_MUERTE = sound("sfx.ahogado_muerte");
    // el Capitan: el farol que se alza, la llama que sale del suelo, la llamada a la tripulacion, y la reja del faro
    public static final RegistryObject<SoundEvent> SFX_CAPITAN_FAROL = sound("sfx.capitan_farol");
    public static final RegistryObject<SoundEvent> SFX_CAPITAN_LLAMA = sound("sfx.capitan_llama");
    public static final RegistryObject<SoundEvent> SFX_CAPITAN_LLAMAR = sound("sfx.capitan_llamar");
    public static final RegistryObject<SoundEvent> SFX_CAPITAN_DOLOR = sound("sfx.capitan_dolor");
    public static final RegistryObject<SoundEvent> SFX_CAPITAN_MUERTE = sound("sfx.capitan_muerte");
    public static final RegistryObject<SoundEvent> SFX_FARO_SELLO = sound("sfx.faro_sello");
    public static final RegistryObject<SoundEvent> MUSICA_CAPITAN = sound("musica.capitan");
    public static final RegistryObject<SoundEvent> MUSICA_BRANDT = sound("musica.brandt");
    public static final RegistryObject<SoundEvent> MUSICA_VARETH = sound("musica.vareth");
    public static final RegistryObject<SoundEvent> MUSICA_MOLINERA = sound("musica.molinera");
    public static final RegistryObject<SoundEvent> MUSICA_VERDUGO = sound("musica.verdugo");
    public static final RegistryObject<SoundEvent> MUSICA_BARQUERO = sound("musica.barquero");
    // la cinematica de inicio (parte 3d): el abismo bajo la voz de la Sombra, la caida, la Sombra que te atrapa, la marca
    // que arde y los latidos antes de abrir los ojos
    public static final RegistryObject<SoundEvent> SFX_INTRO_ABISMO = sound("sfx.intro_abismo");
    public static final RegistryObject<SoundEvent> SFX_INTRO_CAIDA = sound("sfx.intro_caida");
    public static final RegistryObject<SoundEvent> SFX_INTRO_ATRAPA = sound("sfx.intro_atrapa");
    public static final RegistryObject<SoundEvent> SFX_INTRO_MARCA = sound("sfx.intro_marca");
    public static final RegistryObject<SoundEvent> SFX_INTRO_LATIDO = sound("sfx.intro_latido");
    // el barco que va y viene (parte 3d): se eleva, las velas, el viento del vuelo, atraca, la campana del muelle y la
    // reparacion; y la musica del viaje
    public static final RegistryObject<SoundEvent> SFX_BARCO_ELEVA = sound("sfx.barco_eleva");
    public static final RegistryObject<SoundEvent> SFX_BARCO_VELAS = sound("sfx.barco_velas");
    public static final RegistryObject<SoundEvent> SFX_BARCO_VUELO = sound("sfx.barco_vuelo");
    public static final RegistryObject<SoundEvent> SFX_BARCO_ATRACA = sound("sfx.barco_atraca");
    public static final RegistryObject<SoundEvent> SFX_BARCO_CAMPANA = sound("sfx.barco_campana");
    public static final RegistryObject<SoundEvent> SFX_BARCO_REPARA = sound("sfx.barco_repara");
    public static final RegistryObject<SoundEvent> MUSICA_VIAJE = sound("musica.viaje");

    /** Frases del Rey (1..13; la 13 es la 7 con la voz de monstruo de la fase 3). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] REY_VOZ = new RegistryObject[14];

    /** Frases de Ser Gavran (1..18). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] GAVRAN_VOZ = new RegistryObject[19];

    /** Frases del Capitan de Brumavela (1..5). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] CAPITAN_VOZ = new RegistryObject[6];

    /** Frases de la Molinera de Velmor (1..5, parte 7f). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] MOLINERA_VOZ = new RegistryObject[6];

    /** Frases de Maese Odran, el Verdugo de la Arboleda (1..5, parte 7h). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] VERDUGO_VOZ = new RegistryObject[6];

    /** Frases del Barquero de la Laguna Negra (1..5, parte 7j). */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] BARQUERO_VOZ = new RegistryObject[6];

    /** Frases de la Sombra (1..6 la cinematica de inicio, 7..9 el viaje a Valdrenn): en estereo, suenan sin sitio. */
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SoundEvent>[] SOMBRA_VOZ = new RegistryObject[10];

    static {
        for (int i = 1; i < REY_VOZ.length; i++) REY_VOZ[i] = sound("rey.voz." + i);
        for (int i = 1; i < GAVRAN_VOZ.length; i++) GAVRAN_VOZ[i] = sound("gavran.voz." + i);
        for (int i = 1; i < CAPITAN_VOZ.length; i++) CAPITAN_VOZ[i] = sound("capitan.voz." + i);
        for (int i = 1; i < MOLINERA_VOZ.length; i++) MOLINERA_VOZ[i] = sound("molinera.voz." + i);
        for (int i = 1; i < VERDUGO_VOZ.length; i++) VERDUGO_VOZ[i] = sound("verdugo.voz." + i);
        for (int i = 1; i < BARQUERO_VOZ.length; i++) BARQUERO_VOZ[i] = sound("barquero.voz." + i);
        for (int i = 1; i < SOMBRA_VOZ.length; i++) SOMBRA_VOZ[i] = sound("sombra.voz." + i);
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

    /** El marco del portal de la Sombra (parte 5e): piedra de sombra con runas rojas que laten (tres tallas), hueso labrado,
     * hierro negro y las gemas rojas que se encienden al abrirse. Irrompibles. */
    public static final RegistryObject<Block> PIEDRA_SOMBRA = BLOCKS.register("piedra_sombra", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable()
                    .lightLevel(st -> 4).sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)));
    public static final RegistryObject<Block> HUESO_LABRADO = BLOCKS.register("hueso_labrado", () -> new net.minecraft.world.level.block.RotatedPillarBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(-1.0F, 3600000.0F).noLootTable()
                    .sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)));
    public static final RegistryObject<Block> HIERRO_NEGRO = BLOCKS.register("hierro_negro", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(-1.0F, 3600000.0F).noLootTable()
                    .sound(SoundType.NETHERITE_BLOCK).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)));
    public static final RegistryObject<Block> GEMA_ROJA = BLOCKS.register("gema_roja", () -> new com.darkcraft.fortaleza.GemaBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(-1.0F, 3600000.0F).noLootTable()
                    .lightLevel(st -> st.getValue(com.darkcraft.fortaleza.GemaBlock.LUZ) ? 11 : 1).sound(SoundType.AMETHYST)
                    .pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)));

    /** El santuario: la hoguera de almas donde se descansa (irrompible; apagado da un rescoldo de luz, encendido alumbra). */
    public static final RegistryObject<Block> SANTUARIO = BLOCKS.register("santuario", () -> new com.darkcraft.santuario.SantuarioBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
                    .lightLevel(st -> st.getValue(com.darkcraft.santuario.SantuarioBlock.ENCENDIDO) ? 13 : 3).sound(SoundType.DEEPSLATE_BRICKS)
                    .pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false).isRedstoneConductor((st, l, p) -> false)
                    .isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false)));
    /** El mar de nubes (parte 3a): la nube de verdad no se pisa (se cae a traves, al vacio); la firme es la de los caminos. */
    public static final RegistryObject<Block> NUBE = BLOCKS.register("nube", () -> new com.darkcraft.world.NubeBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(-1.0F, 3600000.0F).noLootTable().noCollission()
                    .noOcclusion().sound(SoundType.WOOL).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)
                    .isRedstoneConductor((st, l, p) -> false).isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false),
            false));
    public static final RegistryObject<Block> NUBE_FIRME = BLOCKS.register("nube_firme", () -> new com.darkcraft.world.NubeBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(-1.0F, 3600000.0F).noLootTable()
                    .noOcclusion().sound(SoundType.WOOL).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)
                    .isRedstoneConductor((st, l, p) -> false).isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false),
            true));
    public static final RegistryObject<Item> SANTUARIO_ITEM = ITEMS.register("santuario",
            () -> new net.minecraft.world.item.BlockItem(SANTUARIO.get(), new Item.Properties().rarity(Rarity.EPIC)));
    /** La estela del mapa de Morvain (parte 7d): irrompible, con un rescoldo de luz en el pergamino. */
    public static final RegistryObject<Block> ESTELA_MAPA = BLOCKS.register("estela_mapa", () -> new com.darkcraft.morvain.EstelaBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
                    .lightLevel(st -> 4).sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK)
                    .isValidSpawn((st, l, p, t) -> false).isRedstoneConductor((st, l, p) -> false)
                    .isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false)));
    public static final RegistryObject<Item> ESTELA_MAPA_ITEM = ITEMS.register("estela_mapa",
            () -> new net.minecraft.world.item.BlockItem(ESTELA_MAPA.get(), new Item.Properties().rarity(Rarity.EPIC)));
    /** La soga de la Arboleda de los Ahorcados (parte 7g): cuelga de las ramas; ni se choca con ella ni se rompe. */
    public static final RegistryObject<Block> SOGA = BLOCKS.register("soga", () -> new com.darkcraft.morvain.SogaBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
                    .noCollission().sound(SoundType.WOOL).pushReaction(PushReaction.BLOCK).isValidSpawn((st, l, p, t) -> false)
                    .isRedstoneConductor((st, l, p) -> false).isSuffocating((st, l, p) -> false).isViewBlocking((st, l, p) -> false)));
    /** Los frascos no son objetos que se lleven en el inventario (van en la ficha): estos solo sirven para dibujarlos en la mano al beber. */
    public static final RegistryObject<Item> FRASCO_VIDA = ITEMS.register("frasco_vida",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FRASCO_MANA = ITEMS.register("frasco_mana",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    /** Las piezas de La Garza Gris (parte 3b): no se llevan en el inventario; se dibujan en el mundo y en los carteles. */
    public static final RegistryObject<Item> PIEZA_VELA = ITEMS.register("pieza_vela", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PIEZA_TIMON = ITEMS.register("pieza_timon", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PIEZA_MASTIL = ITEMS.register("pieza_mastil", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PIEZA_ANCLA = ITEMS.register("pieza_ancla", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PIEZA_CORAZON = ITEMS.register("pieza_corazon", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

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
    /** El silbato del corcel de Morvain (parte 7b): lo da Maelis en el Arco del Umbral. */
    public static final RegistryObject<Item> SILBATO_CORCEL = ITEMS.register("silbato_corcel",
            () -> new com.darkcraft.montura.SilbatoCorcel(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.RARE)));

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
    // ---- los enemigos de Brumavela (parte 3c)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Hueco>> HUECO = ENTITIES.register("hueco",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Hueco>of(com.darkcraft.enemigos.Hueco::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.9f).clientTrackingRange(8).build(MODID + ":hueco"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Ahogado>> AHOGADO = ENTITIES.register("ahogado",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Ahogado>of(com.darkcraft.enemigos.Ahogado::new, MobCategory.MONSTER)
                    .sized(0.7f, 1.95f).clientTrackingRange(8).build(MODID + ":ahogado"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Capitan>> CAPITAN = ENTITIES.register("capitan",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Capitan>of(com.darkcraft.enemigos.Capitan::new, MobCategory.MONSTER)
                    .sized(0.85f, 2.35f).fireImmune().clientTrackingRange(10).build(MODID + ":capitan"));
    // ---- los marchitos de Morvain (parte 7e)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Marchito>> MARCHITO = ENTITIES.register("marchito",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Marchito>of(com.darkcraft.enemigos.Marchito::new, MobCategory.MONSTER)
                    .sized(0.6f, 2.05f).clientTrackingRange(8).build(MODID + ":marchito"));
    // ---- los ahorcados del Bosque Muerto (parte 7g)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Ahorcado>> AHORCADO = ENTITIES.register("ahorcado",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Ahorcado>of(com.darkcraft.enemigos.Ahorcado::new, MobCategory.MONSTER)
                    .sized(0.6f, 2.4f).clientTrackingRange(8).build(MODID + ":ahorcado"));
    // ---- los podridos del Pantano Podrido y el fuego fatuo que lanzan (parte 7i)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Podrido>> PODRIDO = ENTITIES.register("podrido",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Podrido>of(com.darkcraft.enemigos.Podrido::new, MobCategory.MONSTER)
                    .sized(0.7f, 1.95f).clientTrackingRange(8).build(MODID + ":podrido"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.FuegoFatuo>> FUEGO_FATUO = ENTITIES.register("fuego_fatuo",
            () -> EntityType.Builder.<com.darkcraft.enemigos.FuegoFatuo>of(com.darkcraft.enemigos.FuegoFatuo::new, MobCategory.MISC)
                    .sized(0.35f, 0.35f).noSave().clientTrackingRange(8).updateInterval(2).build(MODID + ":fuego_fatuo"));
    // ---- los helados de las Montanas Nevadas y las esquirlas de hielo que lanzan (parte 7k)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Helado>> HELADO = ENTITIES.register("helado",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Helado>of(com.darkcraft.enemigos.Helado::new, MobCategory.MONSTER)
                    .sized(0.7f, 2.15f).clientTrackingRange(8).build(MODID + ":helado"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Esquirla>> ESQUIRLA = ENTITIES.register("esquirla",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Esquirla>of(com.darkcraft.enemigos.Esquirla::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).noSave().clientTrackingRange(8).updateInterval(1).build(MODID + ":esquirla"));
    // ---- la Molinera de Velmor, el jefe de las Llanuras Palidas (parte 7f)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Molinera>> MOLINERA = ENTITIES.register("molinera",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Molinera>of(com.darkcraft.enemigos.Molinera::new, MobCategory.MONSTER)
                    .sized(1.3f, 3.7f).fireImmune().clientTrackingRange(10).build(MODID + ":molinera"));
    // ---- Maese Odran, el Verdugo de la Arboleda, el jefe del Bosque Muerto (parte 7h): se manda cada 2 ticks (el salto)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Verdugo>> VERDUGO = ENTITIES.register("verdugo",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Verdugo>of(com.darkcraft.enemigos.Verdugo::new, MobCategory.MONSTER)
                    .sized(1.2f, 3.4f).fireImmune().clientTrackingRange(10).updateInterval(2).build(MODID + ":verdugo"));
    // ---- el Barquero de la Laguna Negra, el jefe del Pantano Podrido (parte 7j): se manda cada 2 ticks (el salto)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Barquero>> BARQUERO = ENTITIES.register("barquero",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Barquero>of(com.darkcraft.enemigos.Barquero::new, MobCategory.MONSTER)
                    .sized(1.2f, 3.6f).fireImmune().clientTrackingRange(10).updateInterval(2).build(MODID + ":barquero"));
    // ---- la guardia de Valdrenn (parte 4a)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Guardia>> GUARDIA = ENTITIES.register("guardia",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Guardia>of(com.darkcraft.enemigos.Guardia::new, MobCategory.MONSTER)
                    .sized(0.65f, 2.05f).clientTrackingRange(8).build(MODID + ":guardia"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Arquero>> ARQUERO = ENTITIES.register("arquero",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Arquero>of(com.darkcraft.enemigos.Arquero::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build(MODID + ":arquero"));
    // ---- el perro de hueso y el espectro de la corte (parte 4b)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Perro>> PERRO = ENTITIES.register("perro",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Perro>of(com.darkcraft.enemigos.Perro::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.95f).clientTrackingRange(8).build(MODID + ":perro"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Espectro>> ESPECTRO = ENTITIES.register("espectro",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Espectro>of(com.darkcraft.enemigos.Espectro::new, MobCategory.MONSTER)
                    .sized(0.65f, 2.0f).clientTrackingRange(8).build(MODID + ":espectro"));
    // ---- los guardianes de Valdrenn (parte 4d)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Brandt>> BRANDT = ENTITIES.register("brandt",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Brandt>of(com.darkcraft.enemigos.Brandt::new, MobCategory.MONSTER)
                    .sized(1.1f, 3.0f).fireImmune().clientTrackingRange(10).build(MODID + ":brandt"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Corteza>> CORTEZA = ENTITIES.register("corteza",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Corteza>of(com.darkcraft.enemigos.Corteza::new, MobCategory.MONSTER)
                    .sized(1.1f, 2.9f).clientTrackingRange(10).build(MODID + ":corteza"));
    // ---- el capitan de elite de la guardia (parte 4c)
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Elite>> ELITE = ENTITIES.register("elite",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Elite>of(com.darkcraft.enemigos.Elite::new, MobCategory.MONSTER)
                    .sized(0.75f, 2.3f).clientTrackingRange(10).build(MODID + ":elite"));
    public static final RegistryObject<EntityType<com.darkcraft.enemigos.Arpon>> ARPON = ENTITIES.register("arpon",
            () -> EntityType.Builder.<com.darkcraft.enemigos.Arpon>of(com.darkcraft.enemigos.Arpon::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(MODID + ":arpon"));
    public static final RegistryObject<Item> HUEVO_ESPADA = ITEMS.register("caballero_espada_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(CABALLERO_ESPADA, 0x4A6342, 0x8E1418, new Item.Properties()));
    public static final RegistryObject<Item> HUEVO_ALABARDA = ITEMS.register("caballero_alabarda_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(CABALLERO_ALABARDA, 0x4A6342, 0x4A1C5C, new Item.Properties()));
    public static final RegistryObject<Item> HUEVO_BALLESTA = ITEMS.register("caballero_ballesta_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(CABALLERO_BALLESTA, 0x4A6342, 0x2E4A2C, new Item.Properties()));

    /** La Garza Gris volando en las cinematicas del viaje (parte 3d): solo existe en el cliente de quien la ve. */
    public static final RegistryObject<EntityType<com.darkcraft.cine.BarcoVolador>> BARCO_VOLADOR = ENTITIES.register("barco_volador",
            () -> EntityType.Builder.<com.darkcraft.cine.BarcoVolador>of(com.darkcraft.cine.BarcoVolador::new, MobCategory.MISC).sized(1f, 1f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(16).updateInterval(Integer.MAX_VALUE).build(MODID + ":barco_volador"));
    /** El remolino del portal de la Sombra (parte 5e): solo se dibuja; no se guarda. */
    public static final RegistryObject<EntityType<com.darkcraft.fortaleza.Remolino>> REMOLINO = ENTITIES.register("remolino",
            () -> EntityType.Builder.<com.darkcraft.fortaleza.Remolino>of(com.darkcraft.fortaleza.Remolino::new, MobCategory.MISC).sized(1f, 1f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(16).updateInterval(20).build(MODID + ":remolino"));
    public static final RegistryObject<EntityType<com.darkcraft.arbol.Ascensor>> ASCENSOR = ENTITIES.register("ascensor",
            () -> EntityType.Builder.<com.darkcraft.arbol.Ascensor>of(com.darkcraft.arbol.Ascensor::new, MobCategory.MISC).sized(5f, 0.3f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(20).build(MODID + ":ascensor"));
    public static final RegistryObject<EntityType<com.darkcraft.arbol.PuertaArbol>> PUERTA_ARBOL = ENTITIES.register("puerta_arbol",
            () -> EntityType.Builder.<com.darkcraft.arbol.PuertaArbol>of(com.darkcraft.arbol.PuertaArbol::new, MobCategory.MISC).sized(7f, 10f)
                    .fireImmune().noSave().noSummon().clientTrackingRange(12).updateInterval(40).build(MODID + ":puerta_arbol"));

    /** El corcel de hueso y niebla de Morvain (parte 7b): no se guarda; lo llama el silbato. */
    public static final RegistryObject<EntityType<com.darkcraft.montura.Corcel>> CORCEL = ENTITIES.register("corcel",
            () -> EntityType.Builder.<com.darkcraft.montura.Corcel>of(com.darkcraft.montura.Corcel::new, MobCategory.MISC)
                    .sized(1.3f, 1.6f).fireImmune().clientTrackingRange(10).build(MODID + ":corcel"));

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

    /** Las cenizas de guerra: cada una cambia la habilidad de un arma (en la forja de Dorvan). */
    public static final java.util.EnumMap<com.darkcraft.combate.Habilidad, RegistryObject<Item>> CENIZAS = new java.util.EnumMap<>(com.darkcraft.combate.Habilidad.class);

    static {
        for (com.darkcraft.combate.Habilidad h : com.darkcraft.combate.Habilidad.values())
            CENIZAS.put(h, ITEMS.register("ceniza_" + h.id, () -> new com.darkcraft.forja.CenizaGuerra(h, new Item.Properties().stacksTo(1).rarity(Rarity.RARE))));
    }

    /** Los hechizos que vuelan (guijarro, lanza, esquirlas, rayo): sin modelo, se ven por sus particulas. */
    public static final RegistryObject<EntityType<com.darkcraft.hechizos.ProyectilHechizo>> PROYECTIL = ENTITIES.register("proyectil_hechizo",
            () -> EntityType.Builder.<com.darkcraft.hechizos.ProyectilHechizo>of(com.darkcraft.hechizos.ProyectilHechizo::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).noSave().clientTrackingRange(8).updateInterval(1).build(MODID + ":proyectil_hechizo"));
    /** El baston (hechicerias, Inteligencia) y el sello (milagros, Fe). */
    /** La daga de los bandidos (parte 2f-3): el combate la trata como daga por su nombre. */
    public static final RegistryObject<Item> DAGA = ITEMS.register("daga",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.IRON, 1, -1.6f, new Item.Properties()));
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
        com.darkcraft.world.MundoNubes.registrar(modBus);
        com.darkcraft.morvain.Morvain.registrar(modBus);
        com.darkcraft.pueblo.PuebloEnemigos.registrar();
        com.darkcraft.valdrenn.ValdrennEnemigos.registrar();
        com.darkcraft.fortaleza.FortalezaEnemigos.registrar();
        com.darkcraft.camino.Paradas.registrar();
        com.darkcraft.morvain.MorvainEnemigos.registrar();
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
        e.put(HUECO.get(), com.darkcraft.enemigos.Hueco.atributos().build());
        e.put(MARCHITO.get(), com.darkcraft.enemigos.Marchito.atributos().build());
        e.put(AHORCADO.get(), com.darkcraft.enemigos.Ahorcado.atributos().build());
        e.put(PODRIDO.get(), com.darkcraft.enemigos.Podrido.atributos().build());
        e.put(HELADO.get(), com.darkcraft.enemigos.Helado.atributos().build());
        e.put(AHOGADO.get(), com.darkcraft.enemigos.Ahogado.atributos().build());
        e.put(CAPITAN.get(), com.darkcraft.enemigos.Capitan.atributos().build());
        e.put(GUARDIA.get(), com.darkcraft.enemigos.Guardia.atributos().build());
        e.put(ARQUERO.get(), com.darkcraft.enemigos.Arquero.atributos().build());
        e.put(PERRO.get(), com.darkcraft.enemigos.Perro.atributos().build());
        e.put(ESPECTRO.get(), com.darkcraft.enemigos.Espectro.atributos().build());
        e.put(ELITE.get(), com.darkcraft.enemigos.Elite.atributos().build());
        e.put(BRANDT.get(), com.darkcraft.enemigos.Brandt.atributos().build());
        e.put(CORTEZA.get(), com.darkcraft.enemigos.Corteza.atributos().build());
        e.put(MOLINERA.get(), com.darkcraft.enemigos.Molinera.atributos().build());
        e.put(VERDUGO.get(), com.darkcraft.enemigos.Verdugo.atributos().build());
        e.put(BARQUERO.get(), com.darkcraft.enemigos.Barquero.atributos().build());
        e.put(CORCEL.get(), com.darkcraft.montura.Corcel.atributos().build());
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
            e.accept(ESTELA_MAPA_ITEM);
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
            e.accept(DAGA);
            e.accept(BASTON);
            e.accept(SELLO_SAGRADO);
            for (RegistryObject<Item> h : HECHIZOS.values()) e.accept(h);
            for (RegistryObject<Item> c : CENIZAS.values()) e.accept(c);
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
