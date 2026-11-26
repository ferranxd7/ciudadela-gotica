#!/usr/bin/env python3
"""
Monta los audios de ElevenLabs (ci/eleven/master) para el juego:
- Voz del Rey Corvin (voz "David - British Storyteller"): eco suave de sala de piedra. En la fase 3 (frases 9 a
  13) la misma voz con efecto de monstruo: mas grave, una segunda voz una octava por debajo, un poco de
  saturacion, eco mas largo y fuego crepitando debajo.
- Efectos de sonido: sin el silencio del principio, recortados a lo que dura cada uno en el juego, en mono (para
  que se oigan desde donde suenan) y con el volumen igualado.
- Voz de Ser Gavran (voz "Final Boss - Evil All Powerful Villain"): eco de sala grande (la cupula del arbol) y un
  poco mas de graves. En la fase 2 (frases 11, 12, 14, 15 y 16) la misma voz mas grave, con una segunda voz por
  debajo y madera crujiendo (como el fuego del Rey).
- Voz del Capitan de Brumavela (voz "Matthew Schmitz - Old Pirate Captain"): mas grave, con una segunda voz sorda por
  debajo (un ahogado) y eco corto.
- Voz de la Sombra (voz "Grav - Deep, Dark & Sinister"): grave y de cerca, con susurros por debajo que repiten sus
  palabras (otra toma de la misma frase susurrada: uno a cada lado y otro mas tarde) y un eco de vacio largo y oscuro.
  Es la unica en estereo (suena sin sitio, en las cinematicas).
Sale todo en src/main/resources/assets/darkcraft/sounds/ (rey/voz_N.ogg, gavran/voz_N.ogg, sfx/NOMBRE_K.ogg) y las
duraciones de las frases en assets/darkcraft/rey_voces.json ("duraciones" del Rey, "gavran", "capitan" y "sombra"; "habla",
"habla_gavran", "habla_capitan" y "habla_sombra" = el segundo en que acaba de hablar de verdad, sin el silencio ni la cola del eco: con eso cuenta el
juego para que la siguiente frase o la pelea no tarden en llegar). A las voces se les quita el silencio del principio.
Necesita ffmpeg.
"""
import json
import os
import re
import subprocess
import sys
import tempfile

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
MASTER = os.path.join(AQUI, "master")
ASSETS = os.path.join(REPO, "src/main/resources/assets/darkcraft")

# toma elegida de cada frase del Rey (a o b) y las que van con voz de monstruo
TOMAS = {n: "a" for n in range(1, 14)}
MONSTRUO = {9, 10, 11, 12, 13}

ECO_SALA = "aecho=0.8:0.55:55|110|190:0.20|0.12|0.07"

# Ser Gavran: toma elegida de cada frase y las de la fase 2 (voz de arbol)
TOMAS_GAVRAN = {n: "a" for n in range(1, 19)}
ARBOL = {11, 12, 14, 15, 16}
ECO_CUPULA = "aecho=0.8:0.6:70|150|260:0.22|0.14|0.08"
VOZ_GAVRAN = ("highpass=f=55,equalizer=f=120:t=q:w=1.0:g=3," + ECO_CUPULA + ","
              "acompressor=threshold=-20dB:ratio=2.5:attack=5:release=140,loudnorm=I=-15:TP=-1.5:LRA=9")
VOZ = ("highpass=f=65," + ECO_SALA + ",acompressor=threshold=-20dB:ratio=2.5:attack=5:release=120,"
       "loudnorm=I=-15:TP=-1.5:LRA=9")

# Maelis, la guia de los santuarios (voz "Autumn Veil"): serena y cerca, con un poco de aire de piedra
TOMAS_GUIA = {n: "a" for n in range(1, 15)}
VOZ_GUIA = ("highpass=f=70,equalizer=f=3200:t=q:w=1.2:g=1.5,aecho=0.8:0.35:38|85:0.10|0.05,"
            "acompressor=threshold=-22dB:ratio=2.2:attack=6:release=160,loudnorm=I=-16:TP=-1.5:LRA=9")
# Dorvan, el herrero (voz "Fjalar - Gruff Scottish Dwarf Warrior"): ronco, con algo de graves y el eco corto de la
# herreria (piedra y metal). La 5b es mas corta y va al grano (la 5a se toma un respiro al empezar).
TOMAS_HERRERO = {n: "a" for n in range(1, 9)} | {5: "b"}
VOZ_HERRERO = ("highpass=f=60,equalizer=f=140:t=q:w=1.0:g=2,equalizer=f=2600:t=q:w=1.3:g=1,"
               "aecho=0.8:0.4:32|71:0.14|0.07,acompressor=threshold=-21dB:ratio=2.5:attack=5:release=140,"
               "loudnorm=I=-15:TP=-1.5:LRA=9")
# Kerrick, el mercader (voz "Cornelius - Wise Sage"): al aire libre, casi seco y cercano
TOMAS_MERCADER = {n: "a" for n in range(1, 7)}
VOZ_MERCADER = ("highpass=f=75,equalizer=f=3000:t=q:w=1.2:g=1.5,aecho=0.8:0.25:24:0.06,"
                "acompressor=threshold=-22dB:ratio=2.2:attack=6:release=150,loudnorm=I=-16:TP=-1.5:LRA=9")
# el Capitan de Brumavela (voz "Matthew Schmitz - Old Pirate Captain"): un ahogado. La misma voz algo mas grave, con una
# segunda voz sorda y temblona por debajo (como oida bajo el agua) y el eco corto del penon del faro
TOMAS_CAPITAN = {n: "a" for n in range(1, 6)}
# la Molinera de Velmor (parte 7f): las tomas a (las b de la 1, la 2 y la 4 se cortan sin acabar)
TOMAS_MOLINERA = {n: "a" for n in range(1, 6)}
# Maese Odran, el Verdugo de la Arboleda (parte 7h; voz "Declan Graves - Haunted Rasps and Old World Dread"): las tomas a
# (la 3b dice la frase en tres trozos, con un silencio delante)
TOMAS_VERDUGO = {n: "a" for n in range(1, 6)}
# el Barquero de la Laguna Negra (parte 7j; voz "Gravel Midnight - Deep Character Voice"): las tomas a
TOMAS_BARQUERO = {n: "a" for n in range(1, 6)}
# Hrodric, el Abad de la Cumbre (parte 7l; voz "Elder Rowan"): las tomas a (las b dicen lo mismo, con las mismas pausas)
TOMAS_ABAD = {n: "a" for n in range(1, 6)}
# la Sombra (voz "Grav - Deep, Dark & Sinister", la mas grave de las tres que se probaron: unos 75 Hz): de cada frase,
# la toma de la voz y la toma susurrada que va por debajo. Las 1-6 son la cinematica de inicio; las 7-9, el viaje a
# Valdrenn. Se eligen las que dicen la frase seguida (la 1a y la 2a se paran a media palabra) y los susurros con menos voz
TOMAS_SOMBRA = {1: "b", 2: "b", 3: "a", 4: "a", 5: "b", 6: "b", 7: "a", 8: "a", 9: "a"}
SUSURROS_SOMBRA = {1: "b", 2: "b", 3: "b", 4: "a", 5: "b", 6: "a", 7: "a", 8: "a", 9: "a"}
# lo que resuena el eco de vacio tras la ultima palabra (s)
COLA_SOMBRA = 3.4
# los personajes con dialogo: id -> (tomas, filtro)
PERSONAJES = {"guia": (TOMAS_GUIA, VOZ_GUIA), "herrero": (TOMAS_HERRERO, VOZ_HERRERO), "mercader": (TOMAS_MERCADER, VOZ_MERCADER)}

# efectos: nombre -> (segundos como mucho, fundido final en s, volumen integrado LUFS)
SFX = {
    "rey_tajo": (1.4, 0.25, -14), "rey_tajo_fuego": (2.8, 0.6, -15), "rey_bola_fuego": (2.4, 0.5, -15),
    "rey_explosion": (4.5, 1.2, -13), "rey_golpe_suelo": (2.8, 0.6, -13), "rey_golpe_final": (6.0, 1.5, -12),
    "rey_rugido": (4.2, 0.8, -13), "rey_geiser": (4.0, 1.0, -15), "rey_pilares": (3.5, 0.8, -15),
    "rey_carga": (3.2, 0.5, -16), "cuenta_atras": (1.9, 0.4, -13), "circulo_dorado": (3.5, 0.8, -15),
    "puerta_runas": (4.0, 0.8, -16), "puerta_abre": (7.0, 1.2, -14), "puerta_cierra": (3.0, 0.6, -13),
    "ascensor_mueve": (7.0, 2.0, -18), "ascensor_llega": (2.5, 0.6, -16),
    # Ser Gavran (una sola toma de cada uno)
    "gavran_tajo": (1.6, 0.35, -14), "gavran_choque": (1.8, 0.5, -14), "gavran_embestida": (3.2, 0.8, -14),
    "gavran_raices": (2.6, 0.7, -15), "gavran_clavar": (3.6, 1.0, -13), "gavran_invocar": (4.5, 1.2, -15),
    "gavran_paso": (0.9, 0.25, -19), "gavran_guardia_rota": (3.2, 0.9, -13), "gavran_salto": (2.8, 0.7, -14),
    "arbol_despierta": (15.0, 3.0, -14), "hojas_cortantes": (3.2, 0.8, -16), "raices_atrapan": (2.8, 0.7, -15),
    "espinas": (3.8, 0.9, -14), "corteza_muro": (2.8, 0.7, -15), "florecer": (7.0, 2.0, -16),
    "madera_cruje": (3.2, 0.8, -16), "corazon_latido": (3.0, 0.8, -15), "hojas_disolver": (5.0, 1.5, -16),
    # Espejo de la Sombra y libro de la M
    "espejo_despierta": (3.0, 0.7, -14), "pluma_escribe": (2.0, 0.3, -20), "libro_abre": (2.6, 0.8, -17),
    # caballeros zombi (el paso: solo el primero de los dos pasos de cada toma)
    "zombi_caballero_grunido": (2.0, 0.4, -18), "zombi_caballero_dolor": (0.9, 0.2, -17), "zombi_caballero_muerte": (2.6, 0.5, -16),
    "armadura_paso": (0.75, 0.2, -23), "escudo_golpe": (1.0, 0.3, -14), "alabarda_tajo": (1.1, 0.3, -15),
    # la Fortaleza de Hueso
    "llave_rastro": (2.0, 0.5, -19), "puente_hueso": (5.5, 1.2, -15), "puerta_hueso": (8.0, 1.8, -13),
    "amuleto_forja": (5.0, 1.5, -14), "portal_abre": (7.0, 2.0, -13),
    # el capitulo 6 (parte 5e): las nubes que se arremolinan, el rayo rojo, la columna de luz y el cielo que enrojece
    "cielo_remolino": (7.0, 2.0, -14), "rayo_rojo": (6.0, 2.5, -11), "columna_roja": (6.0, 2.0, -15), "cielo_rojo": (7.0, 2.5, -15),
    # el HUD nuevo y el rol: carteles, ecos, espada de luz y guardia rota
    "has_muerto": (5.0, 2.2, -13), "jefe_abatido": (5.0, 2.0, -13), "zona_descubierta": (4.0, 1.6, -16),
    "ecos_recuperados": (2.6, 0.8, -15), "ecos_ganados": (0.7, 0.3, -22), "espada_luz": (3.8, 0.9, -21),
    "guardia_rota": (1.3, 0.3, -13),
    # santuarios, frascos, subir de nivel y los menus (parte 2b)
    "santuario_enciende": (4.0, 1.4, -14), "santuario_fuego": (6.0, 1.0, -24), "santuario_descanso": (3.0, 0.9, -16),
    "frasco_beber": (1.3, 0.25, -17), "nivel_sube": (2.6, 0.9, -14),
    "menu_abre": (0.66, 0.12, -19), "menu_mueve": (0.3, 0.1, -26), "menu_acepta": (0.45, 0.15, -20), "menu_cancela": (0.4, 0.12, -20),
    # el combate propio (parte 2c)
    "golpe_ligero": (0.55, 0.12, -17), "golpe_pesado": (0.9, 0.2, -15), "carga_golpe": (1.3, 0.1, -18),
    "impacto_carne": (0.45, 0.12, -15), "impacto_armadura": (0.6, 0.2, -16), "rodar": (0.6, 0.15, -19), "fijar": (0.45, 0.12, -20),
    "estocada": (0.45, 0.12, -16), "golpe_salto": (1.1, 0.4, -13), "aturdido": (1.3, 0.35, -15), "esfuerzo": (0.45, 0.1, -19),
    "comer_fruto": (1.0, 0.25, -18), "talisman": (0.9, 0.25, -19),
    "hechizo_lanzar": (0.7, 0.15, -17), "hechizo_impacto": (0.6, 0.2, -16), "hechizo_rayo": (1.0, 0.3, -15),
    "hechizo_llama": (1.1, 0.35, -16), "hechizo_curar": (1.6, 0.4, -18), "hechizo_bendicion": (1.5, 0.4, -18),
    # el herrero y el mercader (parte 2e-2): el martillo en el yunque, mejorar un arma y comprar
    "herrero_golpe": (0.9, 0.35, -17), "forja_mejora": (2.0, 0.7, -14), "tienda_compra": (0.8, 0.25, -18),
    # las habilidades de las armas (cenizas de guerra, parte 2e-3) y ponerle una ceniza a un arma
    "habilidad_torbellino": (1.0, 0.3, -15), "habilidad_estocada": (0.9, 0.25, -15), "habilidad_grito": (1.6, 0.4, -14),
    "habilidad_sismo": (1.8, 0.6, -13), "habilidad_paso": (0.6, 0.15, -18), "habilidad_llamas": (1.2, 0.4, -15),
    "habilidad_escarcha": (1.2, 0.4, -15), "habilidad_relampago": (1.2, 0.4, -14), "habilidad_sangre": (1.0, 0.3, -15),
    "habilidad_parada": (0.8, 0.25, -14), "ceniza_aplicar": (2.0, 0.6, -15),
    # el codice de la historia (parte 2e-4): abrirlo, pasar una pagina y cerrarlo
    "codice_abre": (1.0, 0.3, -18), "codice_pagina": (0.6, 0.15, -19), "codice_cierra": (0.8, 0.3, -16),
    # el aviso de los logros (parte 2f-1; los de desafio llevan la fanfarria de Minecraft)
    "logro": (2.0, 0.6, -18),
    # Brumavela (parte 3b): una pieza del barco, la losa de la cripta y el barco que despierta
    "pieza_barco": (2.8, 0.8, -16), "cripta_abre": (4.6, 1.2, -15), "barco_reparado": (6.0, 1.5, -14),
    # el viento de Brumavela: se repite en bucle (entra y sale poco a poco, como una racha)
    "brumavela_viento": (17.5, 3.0, -23),
    # los enemigos de Brumavela (parte 3c): el aldeano hueco y el marinero ahogado
    "hueco_gemido": (2.0, 0.4, -21), "hueco_alerta": (1.2, 0.25, -16), "hueco_dolor": (0.75, 0.2, -17), "hueco_muerte": (2.2, 0.5, -17),
    "ahogado_gorgoteo": (2.2, 0.4, -21), "ahogado_alerta": (1.4, 0.3, -16), "ahogado_dolor": (0.75, 0.2, -17),
    "ahogado_muerte": (2.4, 0.5, -16),
    # el Capitan: alza el farol, la llama que sale del suelo, llama a su tripulacion, dolor y muerte; y la reja del faro
    "capitan_farol": (2.0, 0.6, -15), "capitan_llama": (1.8, 0.6, -14), "capitan_llamar": (4.0, 1.0, -14),
    "capitan_dolor": (1.2, 0.3, -16), "capitan_muerte": (3.0, 0.6, -14), "faro_sello": (3.0, 0.8, -15),
    # la cinematica de inicio (parte 3d): el abismo bajo la voz de la Sombra (en bucle), el viento de la caida, la Sombra
    # que te atrapa (crece y da el golpe a los ~1,3 s), la marca que arde y los dos latidos antes de abrir los ojos
    "intro_abismo": (30.0, 4.0, -25), "intro_caida": (10.5, 1.2, -16), "intro_atrapa": (3.6, 0.9, -12),
    "intro_marca": (5.5, 2.0, -17), "intro_latido": (4.2, 0.8, -14),
    # el barco (parte 3d): se eleva (un quejido de cuadernas que crece), las velas que se tensan, el viento del vuelo (en
    # bucle), el golpe sordo al atracar, la campana del muelle y las tablas que vuelven a su sitio al repararlo
    "barco_eleva": (13.5, 3.0, -15), "barco_velas": (6.0, 0.8, -15), "barco_vuelo": (25.0, 3.5, -22),
    "barco_atraca": (6.0, 1.8, -13), "barco_campana": (5.5, 2.0, -14), "barco_repara": (11.2, 1.6, -15),
    # la guardia de Valdrenn y el arquero esqueleto (parte 4a)
    "guardia_alerta": (1.3, 0.3, -15),
    "arco_tensar": (1.0, 0.15, -20), "arco_suelta": (0.7, 0.2, -15),
    "arquero_alerta": (1.1, 0.3, -16), "arquero_huesos": (1.8, 0.4, -22), "arquero_dolor": (0.6, 0.2, -16),
    "arquero_muerte": (2.0, 0.5, -16),
    # el perro de hueso y el espectro de la corte (parte 4b)
    "perro_grunido": (1.6, 0.3, -20), "perro_ladrido": (1.0, 0.25, -15), "perro_aullido": (2.6, 0.5, -15),
    "perro_mordisco": (0.6, 0.15, -14), "perro_dolor": (0.7, 0.2, -15), "perro_muerte": (2.0, 0.5, -15),
    "espectro_susurro": (2.4, 0.6, -22), "espectro_alerta": (1.3, 0.3, -15), "espectro_lamento": (2.2, 0.5, -14),
    "espectro_zarpazo": (0.6, 0.15, -16), "espectro_desvanece": (1.0, 0.1, -17), "espectro_dolor": (0.7, 0.2, -16),
    "espectro_muerte": (2.2, 0.6, -16),
    # el capitan de elite y las emboscadas (parte 4c)
    "elite_alerta": (1.5, 0.3, -15), "elite_grito": (2.6, 0.5, -13), "elite_dolor": (0.7, 0.2, -15),
    "elite_muerte": (3.0, 0.6, -15), "elite_impacto": (2.0, 0.5, -13), "elite_embestida": (1.2, 0.3, -14),
    "emboscada": (2.5, 0.6, -14), "emboscada_alzarse": (1.6, 0.4, -16),
    # los dos guardianes (parte 4d): Ser Brandt, el Muro del Rey; Vareth, el Guardian de Corteza; el sello que se rompe
    "brandt_rugido": (2.4, 0.5, -13), "brandt_martillazo": (2.2, 0.6, -13), "brandt_escudo": (0.9, 0.25, -15),
    "brandt_dolor": (0.6, 0.2, -15), "brandt_muerte": (3.2, 0.8, -15),
    "corteza_rugido": (2.2, 0.5, -13), "corteza_zarpazo": (0.7, 0.15, -15), "corteza_raices": (1.6, 0.4, -14),
    "corteza_espinas": (1.3, 0.3, -14), "corteza_dolor": (0.7, 0.2, -15), "corteza_muerte": (3.4, 0.8, -15),
    "sello_rompe": (2.0, 0.6, -14),
    # el corcel de Morvain (parte 7b): el silbato, la niebla al llegar, el relincho, los cascos, el salto, deshacerse y el dolor
    "corcel_llamada": (2.2, 0.7, -15), "corcel_aparece": (1.8, 0.5, -16), "corcel_relincho": (2.0, 0.5, -14),
    "corcel_paso": (0.5, 0.15, -19), "corcel_salto": (1.0, 0.35, -17), "corcel_deshace": (2.3, 0.9, -15),
    "corcel_dolor": (0.8, 0.2, -15),
    # los Marchitos de Velmor (parte 7e): el gemido, la alerta, el dolor, la muerte en ceniza, una campanada y el farol
    "marchito_gemido": (1.8, 0.5, -21), "marchito_alerta": (1.4, 0.3, -16), "marchito_dolor": (0.6, 0.15, -17),
    "marchito_muerte": (2.3, 0.6, -17), "marchito_campana": (1.3, 0.5, -15), "marchito_farol": (0.9, 0.35, -15),
    # la Molinera de Velmor (parte 7f): el grito, la siega, la guadana contra el suelo, el giro, el dolor, la muerte; y el
    # molino roto que cruje con el viento (suena en bucle mientras ella vive)
    "molinera_grito": (2.4, 0.6, -13), "molinera_siega": (1.0, 0.3, -14), "molinera_golpe": (2.4, 0.8, -13),
    "molinera_giro": (1.4, 0.4, -14), "molinera_dolor": (0.7, 0.2, -15), "molinera_muerte": (3.4, 0.9, -14),
    "molino_viento": (5.5, 1.2, -21),
    # los ahorcados del Bosque Muerto (parte 7g): la soga que cruje (mientras cuelgan), el gemido, el chillido, la caida de
    # la rama (el golpe contra el suelo), el zarpazo, el lazo que restalla, el dolor, la muerte; y el viento de la arboleda
    "ahorcado_soga": (3.0, 0.6, -22), "ahorcado_gemido": (2.0, 0.5, -21), "ahorcado_alerta": (1.4, 0.3, -16),
    "ahorcado_cae": (1.2, 0.4, -14), "ahorcado_zarpazo": (0.6, 0.15, -16), "ahorcado_lazo": (1.2, 0.3, -15),
    "ahorcado_dolor": (0.6, 0.15, -17), "ahorcado_muerte": (2.4, 0.6, -17), "arboleda_viento": (6.0, 1.6, -24),
    # el Verdugo de la Arboleda (parte 7h): el grito, el hacha contra el suelo, el barrido, el lazo que restalla, la caida
    # del salto (del patibulo al suelo), el dolor y la muerte (el hacha cae sobre las tablas)
    "verdugo_grito": (2.4, 0.6, -13), "verdugo_hachazo": (1.4, 0.5, -13), "verdugo_barrido": (0.9, 0.3, -14),
    "verdugo_lazo": (1.3, 0.3, -15), "verdugo_salto": (1.4, 0.5, -13), "verdugo_dolor": (0.75, 0.2, -15),
    "verdugo_muerte": (3.4, 0.9, -14),
    # los podridos del Pantano Podrido (parte 7i): el gorgoteo, el rugido ahogado, el dolor, la muerte (se hunde), alzarse del
    # agua, el vomito de agua negra, el fuego fatuo que vuela, el reventon del gas, la panza contra el barro; y el pantano
    "podrido_gemido": (2.2, 0.5, -20), "podrido_alerta": (1.7, 0.3, -15), "podrido_dolor": (0.6, 0.15, -17),
    "podrido_muerte": (2.6, 0.7, -16), "podrido_alzarse": (1.8, 0.5, -16), "podrido_vomito": (1.6, 0.4, -15),
    "podrido_fatuo": (1.3, 0.4, -15), "podrido_gas": (2.0, 0.8, -17), "podrido_aplastar": (1.0, 0.3, -14),
    "pantano_ambiente": (20.0, 2.0, -27),
    # el Barquero de la Laguna Negra (parte 7j): el grito sobre el agua, el remo de plano contra el agua, el barrido, el
    # garfio que se clava, la linterna que estalla en llamas de almas, el dolor, la muerte (se hunde y la llama se apaga) y
    # el remo en el agua negra (mientras espera en su barca)
    "barquero_grito": (2.4, 0.6, -13), "barquero_remazo": (1.4, 0.5, -13), "barquero_barrido": (0.9, 0.3, -14),
    "barquero_bichero": (1.2, 0.3, -15), "barquero_linterna": (1.9, 0.6, -14), "barquero_dolor": (0.75, 0.2, -15),
    "barquero_muerte": (3.4, 0.9, -14), "barquero_remo": (4.4, 1.0, -21),
    # los helados de las Montanas Nevadas (parte 7k): el rezo roto entre el hielo, el chillido al ver a alguien, el golpe en
    # el hielo, la muerte (se rompe en esquirlas), el hielo que revienta al alzarse, el incensario de hierro, el aliento que
    # congela, el martillo de hielo con sus puas y las esquirlas que vuelan; la ventisca de las montanas y la campana del
    # monasterio
    "helado_gemido": (2.2, 0.5, -20), "helado_alerta": (1.4, 0.3, -15), "helado_dolor": (0.6, 0.15, -17),
    "helado_muerte": (1.8, 0.6, -14), "helado_romper": (1.6, 0.5, -15), "helado_incensario": (1.0, 0.3, -15),
    "helado_aliento": (1.8, 0.5, -15), "helado_martillo": (1.5, 0.5, -13), "helado_esquirlas": (1.0, 0.3, -15),
    "montana_ambiente": (20.0, 2.0, -26), "monasterio_campana": (5.6, 1.6, -16),
    # el Abad de la Cumbre (parte 7l): el grito (y la llamada a los hermanos), el baculo contra el suelo helado, el barrido,
    # el pisoton que levanta el anillo de puas, el aliento que hiela, la llamada al cielo antes de los carambanos, el carambano
    # que cae y revienta, la ventisca (dura lo que dura: 18 s), el dolor, la muerte (se rompe) y el rezo de rodillas
    "abad_grito": (3.4, 0.9, -13), "abad_baculazo": (1.6, 0.5, -13), "abad_barrido": (1.0, 0.3, -14),
    "abad_pisoton": (1.8, 0.6, -13), "abad_aliento": (2.4, 0.6, -15), "abad_invocar": (1.8, 0.6, -16),
    "abad_carambano": (1.2, 0.4, -15), "abad_ventisca": (18.0, 2.5, -20), "abad_dolor": (0.75, 0.2, -15),
    "abad_muerte": (3.6, 0.9, -14), "abad_rezo": (7.0, 1.6, -20),
}
# los que suenan en bucle (se relanzan antes de acabar y se solapan): entran poco a poco (segundos de fundido de entrada)
ENTRADA = {"pantano_ambiente": 2.0, "arboleda_viento": 1.6, "santuario_fuego": 0.8, "espada_luz": 0.5, "brumavela_viento": 2.5, "intro_abismo": 3.0, "intro_caida": 0.5,
           "barco_vuelo": 3.0, "molino_viento": 1.0, "montana_ambiente": 2.0, "abad_ventisca": 1.5, "abad_rezo": 0.4}
# los que no se cogen desde el principio de la toma: segundos que se saltan (aqui, hasta poco antes del golpe gordo)
INICIO = {"barco_atraca": 3.8, "barquero_remo": 0.8}
# casi todo por debajo de 100 Hz (en unos altavoces pequenos no sonarian): se les suman la octava y la quinta de encima
GRAVES = {"intro_atrapa", "intro_latido", "barco_atraca"}
# un filtro mas antes de igualar el volumen (aqui, que se oiga el chisporroteo de la marca y no solo el zumbido)
EXTRA = {"intro_marca": "highshelf=f=2500:g=8,"}
# de estos se bajaron 4 tomas (NOMBRE_1..4.mp3): las elegidas salen como NOMBRE_1.ogg, NOMBRE_2.ogg...
TOMAS_SFX = {
    "espejo_despierta": (3,),   # empieza grave y se aclara (el zumbido que sube y la campanilla)
    "pluma_escribe": (2, 4),    # las dos de volumen mas parejo
    "libro_abre": (3,),         # la del golpe de la tapa al principio
    "zombi_caballero_grunido": (1, 2, 3, 4), "zombi_caballero_dolor": (1, 2, 3, 4), "zombi_caballero_muerte": (1, 2),
    "armadura_paso": (1, 2, 3, 4), "escudo_golpe": (1, 3), "alabarda_tajo": (2, 4),
    "llave_rastro": (2, 3),      # las dos que suenan enseguida (la 4 casi no se oye)
    "puente_hueso": (1, 2),      # las que crecen y se mantienen
    "puerta_hueso": (1,),        # la mas larga: el cuerpo pesado dura lo que sube la reja
    "amuleto_forja": (1,),       # golpe y brillo que se sostiene antes de apagarse
    "portal_abre": (1,),         # crece y se mantiene cinco segundos
    "cielo_remolino": (1,),      # el retumbar que se mantiene cuatro segundos (con mas graves)
    "rayo_rojo": (3,),           # el chasquido y el trueno que tarda mas en irse
    "columna_roja": (2,),        # crece desde nada hasta un rugido grave a los 2,5 s
    "cielo_rojo": (3,),          # el que sube despacio y se sostiene
    "has_muerto": (3,),          # el golpe mas fuerte y con mas graves, que se apaga despacio
    "jefe_abatido": (1,),        # el mas largo (3,5 s de cola) y con mas cuerpo
    "zona_descubierta": (3,),    # una campanada clara al principio que se va apagando
    "ecos_recuperados": (4,),    # soplo y campanilla, la que mas se oye
    "ecos_ganados": (2, 4),      # dos soplos cortos (las otras dos casi no suenan)
    "espada_luz": (2,),          # el brillo mas parejo de principio a fin
    "guardia_rota": (1, 3),      # golpe y quejido / golpe seco con graves
    "santuario_enciende": (1,),  # el soplo mas largo y con mas graves
    "santuario_fuego": (3,),     # el crepitar mas parejo
    "santuario_descanso": (3,),  # la que mas cuerpo tiene
    "frasco_beber": (2, 1),      # corcho y dos tragos
    "nivel_sube": (2,),          # la mas llena y larga
    "menu_abre": (3,),           # la losa mas grave
    "menu_mueve": (1,), "menu_acepta": (4,), "menu_cancela": (3,),
    "golpe_ligero": (1, 2, 3, 4),  # cuatro para que una cadena de golpes no suene siempre igual
    "golpe_pesado": (2, 4),      # la mas fuerte y la mas grave
    "carga_golpe": (1, 3),       # las que crecen con graves
    "impacto_carne": (1, 2, 3),  # la 4 casi no suena
    "impacto_armadura": (1, 3, 4),
    "rodar": (2, 4),             # las que se oyen enteras
    "fijar": (3,),               # clic con cuerpo, sin tanto grave
    "estocada": (1, 3, 4),       # la 2 casi no suena
    "golpe_salto": (2, 4),       # las mas largas y con mas cuerpo
    "aturdido": (1, 4),          # las mas cortas (las otras dos llevan otro golpe detras)
    "esfuerzo": (1, 2, 3, 4),
    "comer_fruto": (1, 2),
    "talisman": (2, 3),
    "hechizo_lanzar": (4, 1),
    "hechizo_impacto": (2, 1),
    "hechizo_rayo": (1, 4),
    "hechizo_llama": (4, 1),
    "hechizo_curar": (3, 2),
    "hechizo_bendicion": (4, 3),
    "herrero_golpe": (4, 1, 3),  # tres martillazos distintos (la 2 suena mas fina)
    "forja_mejora": (1, 4),      # el golpe y el brillo que se sostiene (la 1, la mas llena)
    "tienda_compra": (2, 4),     # monedas: las dos que mas se oyen
    "habilidad_torbellino": (1, 4),  # los dos silbidos mas fuertes
    "habilidad_estocada": (1, 3),    # las mas llenas
    "habilidad_grito": (4, 2),       # el rugido mas fuerte y largo
    "habilidad_sismo": (1, 4),       # las de mas graves
    "habilidad_paso": (3, 4),        # las que se oyen (la 1 y la 2 casi no suenan)
    "habilidad_llamas": (4, 2),
    "habilidad_escarcha": (1, 2),
    "habilidad_relampago": (3, 1),
    "habilidad_sangre": (1, 4),
    "habilidad_parada": (2, 3),      # el choque mas claro
    "ceniza_aplicar": (1, 4),        # la mas llena (la 2 casi no suena)
    "codice_abre": (2, 1),           # las dos que se oyen (la 3 y la 4 casi no suenan)
    "codice_pagina": (1, 3),         # la hoja que cae clara
    "codice_cierra": (4, 1),         # el golpe de la tapa con mas graves
    "logro": (2, 4),                 # las campanas mas graves y largas
    "pieza_barco": (3, 2),           # las dos campanadas con mas cuerpo
    "cripta_abre": (1, 3),           # las que arrastran la losa mas de tres segundos (la 4 casi no se oye)
    "barco_reparado": (1, 3),        # la que suena los cinco segundos enteros, y la del zumbido mas largo
    "brumavela_viento": (3,),        # la mas pareja de principio a fin (para repetirla en bucle)
    # los enemigos de Brumavela: las tomas con mas cuerpo (las muy flojas o casi todo silencio, fuera)
    "hueco_gemido": (4, 3, 1), "hueco_alerta": (3, 2), "hueco_dolor": (3, 2), "hueco_muerte": (4, 2),
    "ahogado_gorgoteo": (4, 2, 3), "ahogado_alerta": (4, 1), "ahogado_dolor": (4, 3), "ahogado_muerte": (2, 1),
    # el Capitan: las tomas mas llenas (la 2 de la reja casi no suena)
    "capitan_farol": (2, 4), "capitan_llama": (4, 3), "capitan_llamar": (4, 1), "capitan_dolor": (2, 4, 1),
    "capitan_muerte": (3, 4), "faro_sello": (3, 1),
    # la cinematica de inicio: el zumbido mas parejo, el viento que sopla igual los doce segundos, la que crece dos
    # segundos y da el golpe, la marca con mas cuerpo y los dos latidos separados dos segundos largos
    "intro_abismo": (3,), "intro_caida": (2,), "intro_atrapa": (2,), "intro_marca": (1,), "intro_latido": (4,),
    # el barco: el quejido que crece cuatro segundos antes de llenarse, las velas con dos golpes secos (a los 2 y a los
    # 3,5 s), el viento mas parejo, el golpe mas gordo (a los 4,5 s de la toma), las campanas con mas cuerpo y el
    # martilleo seguido
    "barco_eleva": (3,), "barco_velas": (1,), "barco_vuelo": (4,), "barco_atraca": (4,), "barco_campana": (1, 4),
    "barco_repara": (3,),
    # la guardia y el arquero: el grunido mas grave y el que crece; los crujidos del arco mas llenos; las cuerdas con
    # golpe seco al principio; los huesos mas llenos; los golpes mas secos y las muertes mas largas
    "guardia_alerta": (2, 1), "arco_tensar": (2, 1), "arco_suelta": (1, 2), "arquero_alerta": (2, 4),
    "arquero_huesos": (4, 3, 1), "arquero_dolor": (1, 3, 4), "arquero_muerte": (1, 3, 4),
    # el perro: el grunido mas grave, los ladridos mas llenos, los aullidos mas largos, los mordiscos mas secos; el
    # espectro: los susurros mas claros, los gritos mas llenos, el desvanecerse que crece hasta el final (aparece al
    # acabar), los dolores y la muerte mas fuertes
    "perro_grunido": (4, 1), "perro_ladrido": (2, 1), "perro_aullido": (4, 2), "perro_mordisco": (2, 3),
    "perro_dolor": (3, 1, 4), "perro_muerte": (4, 1, 3),
    "espectro_susurro": (1, 3, 4), "espectro_alerta": (4, 3), "espectro_lamento": (4, 1), "espectro_zarpazo": (2, 1),
    "espectro_desvanece": (2, 4), "espectro_dolor": (4, 2), "espectro_muerte": (4, 3),
    # el capitan de elite: las alertas mas llenas, los gritos mas fuertes, los golpes al suelo mas graves; la emboscada:
    # el cuerno mas largo y el de mas graves; los que se alzan, los mas fuertes
    "elite_alerta": (2, 4), "elite_grito": (2, 4), "elite_dolor": (1, 3, 4), "elite_muerte": (1, 4),
    "elite_impacto": (4, 1, 3), "elite_embestida": (1, 3), "emboscada": (4, 1), "emboscada_alzarse": (1, 2),
    # los guardianes (de estos se bajaron solo las tomas 1 y 2): de Brandt, el rugido mas grave y el martillazo mas seco
    # primero; de Vareth, el rugido fuerte primero (el otro es flojo: lo iguala el volumen); el sello, la toma mas llena
    "brandt_rugido": (1, 2), "brandt_martillazo": (2, 1), "brandt_escudo": (2, 1), "brandt_dolor": (2, 1),
    "brandt_muerte": (1, 2), "corteza_rugido": (2, 1), "corteza_zarpazo": (2, 1), "corteza_raices": (2, 1),
    "corteza_espinas": (1, 2), "corteza_dolor": (2, 1), "corteza_muerte": (2, 1), "sello_rompe": (2, 1),
    # el corcel: el silbido que se sostiene; la niebla que crece hasta el golpe de los cascos; los dos relinchos mas
    # largos; los cuatro cascos (uno cada paso, al azar); los dos soplos mas cortos; las dos colas mas largas; los tres
    # quejidos que se cortan antes
    "corcel_llamada": (4, 2), "corcel_aparece": (2,), "corcel_relincho": (2, 3), "corcel_paso": (1, 2, 3, 4),
    "corcel_salto": (2, 4), "corcel_deshace": (2, 3), "corcel_dolor": (3, 1, 4),
    # los Marchitos: los tres gemidos mas llenos; el chillido agudo (la 1) y el mas fuerte de los graves; tres quejidos;
    # las dos muertes que se deshacen mas despacio; las cuatro campanadas (cada una es un solo golpe: el campanero toca
    # tres veces y cada vez suena una); los dos farolazos con golpe seco y uno con mas cola
    "marchito_gemido": (1, 3, 2), "marchito_alerta": (1, 3), "marchito_dolor": (3, 1, 4), "marchito_muerte": (2, 3),
    "marchito_campana": (4, 3, 1, 2), "marchito_farol": (1, 4, 2),
    # la Molinera: los dos gritos mas largos y sostenidos; las tres siegas cortas (la 2 es un soplo de dos segundos); los
    # dos golpes con piedras que caen despues; los dos giros mas llenos; tres quejidos; las dos muertes mas graves; el
    # molino: el que mas cruje y el mas parejo (para el bucle)
    "molinera_grito": (3, 1), "molinera_siega": (4, 1, 3), "molinera_golpe": (3, 4), "molinera_giro": (2, 4),
    "molinera_dolor": (1, 3, 2), "molinera_muerte": (4, 2), "molino_viento": (4, 1),
    # los ahorcados: los tres crujidos de soga mas llenos (la 3, la mas larga); los tres gemidos con mas cuerpo; los dos
    # chillidos mas fuertes; los tres golpes contra el suelo (la 4 con mas hojas); los dos zarpazos mas llenos; los tres
    # restallidos (la 3 silba antes); tres quejidos; las dos muertes mas largas; y dos rachas de viento (una cada vez, al azar)
    "ahorcado_soga": (4, 1, 3), "ahorcado_gemido": (4, 1, 3), "ahorcado_alerta": (2, 4), "ahorcado_cae": (4, 2, 1),
    "ahorcado_zarpazo": (3, 1), "ahorcado_lazo": (3, 1, 4), "ahorcado_dolor": (4, 2, 3), "ahorcado_muerte": (1, 3),
    "arboleda_viento": (3, 2),
    # el Verdugo: los dos gritos que mas crecen por el medio; los dos hachazos con mas graves y mas cola; los dos barridos
    # mas largos; los dos lazos que silban y restallan (la 3 empieza callada); las dos caidas con mas cuerpo (la 1 es un
    # chasquido); tres quejidos; las dos muertes enteras
    "verdugo_grito": (3, 1), "verdugo_hachazo": (4, 2), "verdugo_barrido": (4, 1), "verdugo_lazo": (1, 4),
    "verdugo_salto": (4, 2), "verdugo_dolor": (1, 4, 3), "verdugo_muerte": (1, 2),
    # los podridos: los tres gorgoteos que antes empiezan; los dos rugidos que mas crecen; los dos quejidos que se oyen (2 y 4
    # casi no suenan); las dos muertes mas largas; las dos salidas del agua con mas chapoteo al principio; los dos vomitos mas
    # llenos; los tres fuegos con mas cuerpo; los dos reventones con su golpe (la 3 es solo un siseo); las dos caidas con mas
    # golpe; y dos tomas del pantano (las que tienen algo de principio a fin)
    "podrido_gemido": (3, 2, 1), "podrido_alerta": (1, 4), "podrido_dolor": (1, 3), "podrido_muerte": (2, 3),
    "podrido_alzarse": (2, 4), "podrido_vomito": (3, 1), "podrido_fatuo": (2, 3, 1), "podrido_gas": (1, 4),
    "podrido_aplastar": (4, 2), "pantano_ambiente": (2, 3),
    # el Barquero: los dos gritos que se oyen (la 3, la mas fuerte, y la 4, que crece; la 1 y la 2 apenas suenan); los dos
    # remazos con mas golpe; los dos barridos mas fuertes; los dos garfios que antes pegan; las dos llamaradas mas largas;
    # tres quejidos; las dos muertes con mas cola; y los dos remos que reman de verdad (la 2, varias paladas; la 4, una)
    "barquero_grito": (3, 4), "barquero_remazo": (3, 1), "barquero_barrido": (4, 1), "barquero_bichero": (4, 2),
    "barquero_linterna": (1, 4), "barquero_dolor": (2, 3, 1), "barquero_muerte": (4, 2), "barquero_remo": (2, 4),
    # los helados: los dos rezos mas largos y mas graves (la 4 y la 2); los dos chillidos que mas duran; los dos golpes mas
    # fuertes; las dos roturas con mas cola; los dos hielos que revientan con mas trozos cayendo; los dos incensarios con la
    # cadena (la 4 y la 3); los dos alientos que mas soplan; los dos martillazos con cola (puas); las dos esquirlas mas
    # largas; las dos campanas con mas eco (la 1 y la 4); y las dos tomas de la ventisca que suenan de principio a fin
    "helado_gemido": (4, 2), "helado_alerta": (3, 1), "helado_dolor": (2, 4), "helado_muerte": (4, 1), "helado_romper": (4, 1),
    "helado_incensario": (4, 3), "helado_aliento": (1, 2), "helado_martillo": (4, 3), "helado_esquirlas": (1, 3),
    "monasterio_campana": (1, 4), "montana_ambiente": (1, 2),
    # el Abad: los gritos que antes llegan arriba y mas aguantan (la 3 y la 4); los dos baculazos que pegan nada mas empezar;
    # los dos barridos mas rapidos; los dos pisotones con mas graves (la 4, casi todo graves); los dos alientos mas fuertes
    # desde el principio; las dos llamadas que mas suenan (la 4 casi no se oye); tres carambanos (caen muchos a la vez); la
    # ventisca: las dos que suenan de principio a fin; tres quejidos; las dos muertes que mas duran fuertes; y los dos rezos
    # mas largos y parejos
    "abad_grito": (3, 4), "abad_baculazo": (3, 1), "abad_barrido": (3, 1), "abad_pisoton": (4, 3), "abad_aliento": (4, 1),
    "abad_invocar": (1, 3), "abad_carambano": (3, 1, 4), "abad_ventisca": (2, 4), "abad_dolor": (2, 4, 1),
    "abad_muerte": (2, 3), "abad_rezo": (4, 3),
}
# efectos con una sola toma (_1)
UNA_TOMA = {k for k in SFX if k.startswith("gavran_")} | {"arbol_despierta", "hojas_cortantes", "raices_atrapan", "espinas",
                                                          "corteza_muro", "florecer", "madera_cruje", "corazon_latido",
                                                          "hojas_disolver"}


def ff(args):
    r = subprocess.run(["ffmpeg", "-y", "-v", "error"] + args, capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError(r.stderr[-600:])


def duracion(f):
    p = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", f],
                       capture_output=True, text=True)
    return float(p.stdout.strip())


def habla(f):
    """(silencio del principio, segundo en que acaba de hablar, duracion) de un audio original."""
    r = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-i", f, "-af", "silencedetect=noise=-38dB:d=0.10",
                        "-f", "null", "-"], capture_output=True, text=True)
    dur = duracion(f)
    ini = [float(x) for x in re.findall(r"silence_start: (-?[0-9.]+)", r.stderr)]
    fin = [float(x) for x in re.findall(r"silence_end: ([0-9.]+)", r.stderr)]
    principio = fin[0] if ini and ini[0] <= 0.01 and fin else 0.0
    final = dur
    if ini and (len(fin) < len(ini) or fin[-1] >= dur - 0.06):
        final = max(principio + 0.2, ini[-1])
    return principio, final, dur


def recorte(src):
    """El filtro que quita el silencio del principio, lo que quita (s) y lo que queda hablado (s)."""
    principio, final, _ = habla(src)
    r = max(0.0, principio - 0.03)
    return (f"atrim=start={r:.3f},asetpts=PTS-STARTPTS," if r > 0.01 else ""), r, final - r


# lo que estira la voz cada efecto (asetrate + atempo) y la cola de eco que aun se oye al acabar de hablar
ESTIRA_MONSTRUO = 1 / (0.82 * 1.10)
ESTIRA_ARBOL = 1 / (0.88 * 1.0606)
COLA = 0.10


def voz_normal(src, dst):
    pre, _, hablado = recorte(src)
    ff(["-i", src, "-af", pre + VOZ, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


def voz_monstruo(src, dst, fuego, desfase):
    pre, quita, hablado = recorte(src)
    d = (duracion(src) - quita) * 1.12 + 0.9
    grafo = (
        "[0:a]" + pre + "asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.82,aresample=44100,atempo=1.10[baja];"
        "[v2]asetrate=44100*0.60,aresample=44100,atempo=1.5033,volume=0.55[sub];"
        "[baja][sub]amix=inputs=2:weights=1 0.7:normalize=0,"
        "asoftclip=type=atan:threshold=0.6,equalizer=f=95:t=q:w=1.1:g=5,equalizer=f=2600:t=q:w=1.4:g=-2,"
        "aecho=0.8:0.6:70|150|270:0.28|0.18|0.10[voz];"
        f"[1:a]atrim=start={desfase:.2f}:duration={d:.2f},asetpts=PTS-STARTPTS,highpass=f=180,volume=0.32,"
        f"afade=t=in:d=0.35,afade=t=out:st={max(0.1, d - 0.8):.2f}:d=0.8[fuego];"
        "[voz][fuego]amix=inputs=2:weights=1 1:normalize=0:duration=first,"
        "acompressor=threshold=-18dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-i", fuego, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1",
        "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado * ESTIRA_MONSTRUO + COLA


def voz_gavran(src, dst):
    pre, _, hablado = recorte(src)
    ff(["-i", src, "-af", pre + VOZ_GAVRAN, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


def voz_arbol(src, dst, madera, desfase):
    """La voz de la fase 2: mas grave, una segunda voz por debajo y madera crujiendo."""
    pre, quita, hablado = recorte(src)
    d = (duracion(src) - quita) * 1.08 + 1.0
    grafo = (
        "[0:a]" + pre + "asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.88,aresample=44100,atempo=1.0606[baja];"
        "[v2]asetrate=44100*0.66,aresample=44100,atempo=1.4141,volume=0.5[sub];"
        "[baja][sub]amix=inputs=2:weights=1 0.65:normalize=0,"
        "asoftclip=type=atan:threshold=0.7,equalizer=f=100:t=q:w=1.1:g=4,equalizer=f=2800:t=q:w=1.4:g=-2,"
        "aecho=0.8:0.6:80|170|290:0.26|0.16|0.09[voz];"
        f"[1:a]atrim=start={desfase:.2f}:duration={d:.2f},asetpts=PTS-STARTPTS,highpass=f=120,volume=0.35,"
        f"afade=t=in:d=0.3,afade=t=out:st={max(0.1, d - 0.8):.2f}:d=0.8[madera];"
        "[voz][madera]amix=inputs=2:weights=1 1:normalize=0:duration=first,"
        "acompressor=threshold=-18dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-i", madera, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1",
        "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado * ESTIRA_ARBOL + COLA


def voz_capitan(src, dst):
    """El Capitan: mas grave (sin alargarla), una segunda voz sorda con coro por debajo y eco corto de piedra."""
    pre, _, hablado = recorte(src)
    grafo = (
        "[0:a]" + pre + "asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.92,aresample=44100,atempo=1.0870[baja];"
        "[v2]asetrate=44100*0.92,aresample=44100,atempo=1.0870,lowpass=f=650,"
        "chorus=0.6:0.9:50|60:0.4|0.32:0.25|0.4:2|1.3,volume=0.7[agua];"
        "[baja][agua]amix=inputs=2:weights=1 0.6:normalize=0,"
        "equalizer=f=110:t=q:w=1.0:g=4,equalizer=f=3000:t=q:w=1.4:g=-1.5,"
        "aecho=0.8:0.5:60|135|220:0.20|0.12|0.06,"
        "acompressor=threshold=-19dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


def voz_molinera(src, dst):
    """
    La Molinera de Velmor: una vieja enorme. Algo mas grave y ronca (sin alargarla), una segunda voz una octava y media por
    debajo, sorda, que le da el tamano, y el eco abierto de las llanuras (lejos y poco: no hay sala).
    """
    pre, _, hablado = recorte(src)
    grafo = (
        "[0:a]aresample=44100," + pre + "asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.90,aresample=44100,atempo=1.1111[baja];"
        "[v2]asetrate=44100*0.62,aresample=44100,atempo=1.6129,lowpass=f=900,volume=0.42[honda];"
        "[baja][honda]amix=inputs=2:weights=1 0.55:normalize=0,"
        "asoftclip=type=atan:threshold=0.75,equalizer=f=140:t=q:w=1.0:g=3,equalizer=f=3200:t=q:w=1.2:g=-1.5,"
        "aecho=0.8:0.35:240|520:0.16|0.08,"
        "acompressor=threshold=-19dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


def voz_verdugo(src, dst):
    """
    Maese Odran, el Verdugo de la Arboleda: un hombreton muerto y cosido. Mas grave (sin alargarla), una segunda voz casi
    una octava por debajo, sorda y baja, que le da el cuerpo, algo de raspa y el eco corto del bosque muerto (troncos cerca,
    ninguna sala).
    """
    pre, _, hablado = recorte(src)
    grafo = (
        "[0:a]aresample=44100," + pre + "asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.89,aresample=44100,atempo=1.1236[baja];"
        "[v2]asetrate=44100*0.66,aresample=44100,atempo=1.5152,lowpass=f=700,volume=0.38[honda];"
        "[baja][honda]amix=inputs=2:weights=1 0.5:normalize=0,"
        "asoftclip=type=atan:threshold=0.78,equalizer=f=120:t=q:w=1.0:g=3,equalizer=f=3000:t=q:w=1.2:g=-1,"
        "aecho=0.8:0.3:70|160|310:0.16|0.10|0.05,"
        "acompressor=threshold=-19dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


def voz_barquero(src, dst):
    """
    El Barquero de la Laguna Negra: un muerto hueco que habla desde el agua. Algo mas grave, una segunda voz una octava por
    debajo muy baja (el hueco del pecho), un poco de raspa y el eco sobre el agua de la laguna (largo y apagado: sin paredes).
    """
    pre, _, hablado = recorte(src)
    grafo = (
        "[0:a]aresample=44100," + pre + "asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.93,aresample=44100,atempo=1.0753[baja];"
        "[v2]asetrate=44100*0.5,aresample=44100,atempo=2.0,lowpass=f=600,volume=0.3[honda];"
        "[baja][honda]amix=inputs=2:weights=1 0.45:normalize=0,"
        "asoftclip=type=atan:threshold=0.8,equalizer=f=140:t=q:w=1.0:g=2.5,highshelf=f=6000:g=-3,"
        "aecho=0.8:0.35:120|260|480:0.20|0.12|0.07,"
        "acompressor=threshold=-19dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA + 0.1


def voz_abad(src, dst):
    """
    Hrodric, el Abad de la Cumbre: un viejo gigante helado que habla desde lo alto del calvario. Algo mas grave, una segunda
    voz una octava por debajo muy baja (el pecho del gigante), un poco de brillo frio arriba y el eco largo de la cumbre
    entre las rocas del circo (como en la nave de una iglesia, pero al raso: largo, abierto y sin retumbar).
    """
    pre, _, hablado = recorte(src)
    grafo = (
        "[0:a]aresample=44100," + pre + "asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.92,aresample=44100,atempo=1.0870[baja];"
        "[v2]asetrate=44100*0.5,aresample=44100,atempo=2.0,lowpass=f=650,volume=0.28[honda];"
        "[baja][honda]amix=inputs=2:weights=1 0.4:normalize=0,"
        "asoftclip=type=atan:threshold=0.85,equalizer=f=150:t=q:w=1.0:g=2,highshelf=f=5000:g=1.5,"
        "aecho=0.8:0.4:110|250|430|680:0.22|0.15|0.10|0.06,"
        "acompressor=threshold=-19dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA + 0.15


def sonoridad(f):
    """(LUFS integrado, pico real en dBFS) de un audio."""
    r = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-i", f, "-af", "ebur128=peak=true", "-f", "null", "-"],
                       capture_output=True, text=True)
    cola = r.stderr[r.stderr.rfind("Summary:"):]
    i = float(re.search(r"I:\s+(-?[0-9.]+) LUFS", cola).group(1))
    p = float(re.search(r"Peak:\s+(-?[0-9.]+) dBFS", cola).group(1))
    return i, p


def voz_sombra(src, sus, dst, lufs=-15.0):
    """La Sombra: grave y de cerca; por debajo, la misma frase susurrada tres veces (a la izquierda a los 0,4 s, a la
    derecha y mas aguda a los 0,9 s, y en medio, mas grave, a los 1,65 s), tres ecos cada vez mas sordos y un eco de
    vacio largo (ruido que se apaga en 5,5 s, distinto a cada lado). En estereo. Devuelve el segundo en que acaba de hablar."""
    pre, _, hablado = recorte(src)
    pre_s, _, _ = recorte(sus)
    with tempfile.TemporaryDirectory() as tmp:
        seco, mojado, mezcla = (os.path.join(tmp, n + ".wav") for n in ("seco", "mojado", "mezcla"))
        grafo = (
            "[0:a]" + pre + "aresample=44100,asetrate=44100*0.95,aresample=44100,atempo=1.0526,highpass=f=40,"
            "equalizer=f=80:t=q:w=1.0:g=2,equalizer=f=320:t=q:w=1.2:g=-2,equalizer=f=2400:t=q:w=0.8:g=6,highshelf=f=6000:g=3,"
            "acompressor=threshold=-24dB:ratio=3.5:attack=5:release=160:makeup=3,"
            f"apad=pad_dur={COLA_SOMBRA},asplit=4[v][va][vb][vc];"
            "[1:a]" + pre_s + "aresample=44100,highpass=f=700,equalizer=f=3000:t=q:w=1.0:g=4,"
            "acompressor=threshold=-30dB:ratio=4:attack=3:release=120:makeup=4,asplit=3[s1][s2][s3];"
            "[s1]adelay=420:all=1,volume=0.55,pan=stereo|c0=c0|c1=0.2*c0[w1];"
            "[s2]asetrate=44100*1.06,aresample=44100,atempo=0.9434,adelay=930:all=1,volume=0.44,pan=stereo|c0=0.2*c0|c1=c0[w2];"
            "[s3]asetrate=44100*0.92,aresample=44100,atempo=1.087,lowpass=f=3800,adelay=1650:all=1,volume=0.31,"
            "pan=stereo|c0=0.75*c0|c1=0.75*c0[w3];"
            "[va]adelay=560:all=1,lowpass=f=2000,volume=0.36,pan=stereo|c0=c0|c1=0.5*c0[e1];"
            "[vb]adelay=1180:all=1,lowpass=f=1500,volume=0.24,pan=stereo|c0=0.5*c0|c1=c0[e2];"
            "[vc]adelay=1900:all=1,lowpass=f=1100,volume=0.15,pan=stereo|c0=c0|c1=c0[e3];"
            "[v]pan=stereo|c0=c0|c1=c0[d];"
            "[d][w1][w2][w3][e1][e2][e3]amix=inputs=7:normalize=0:duration=first[seco]"
        )
        ff(["-i", src, "-i", sus, "-filter_complex", grafo, "-map", "[seco]", "-ar", "44100", "-ac", "2", "-c:a", "pcm_s24le", seco])
        vacio = (
            "anoisesrc=d=5.5:c=white:r=44100:a=0.5:seed=4217,lowpass=f=2300,highpass=f=140,afade=t=out:st=0:d=5.5:curve=exp[irl];"
            "anoisesrc=d=5.5:c=white:r=44100:a=0.5:seed=9133,lowpass=f=2300,highpass=f=140,afade=t=out:st=0:d=5.5:curve=exp[irr];"
            "[irl][irr]join=inputs=2:channel_layout=stereo[ir];"
            "[0:a]adelay=70:all=1[ida];[ida][ir]afir=dry=1:wet=1:gtype=peak[eco]"
        )
        ff(["-i", seco, "-filter_complex", vacio, "-map", "[eco]", "-ar", "44100", "-ac", "2", "-c:a", "pcm_f32le", mojado])
        # el eco de vacio, 9,5 LU por debajo de lo seco; y el conjunto, al volumen de las demas voces
        i_s, _ = sonoridad(seco)
        i_m, _ = sonoridad(mojado)
        d = duracion(seco)
        ff(["-i", seco, "-i", mojado, "-filter_complex",
            f"[1:a]volume={(i_s - 9.5) - i_m:.2f}dB[m];[0:a][m]amix=inputs=2:normalize=0:duration=first,"
            f"afade=t=out:st={d - 0.9:.2f}:d=0.9[out]", "-map", "[out]", "-ar", "44100", "-ac", "2", "-c:a", "pcm_s24le", mezcla])
        i, _ = sonoridad(mezcla)
        ff(["-i", mezcla, "-af", f"volume={lufs - i:.2f}dB,alimiter=level_in=1:level_out=1:limit=0.84:attack=4:release=60:level=false",
            "-ar", "44100", "-ac", "2", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


# el capitulo 6 (parte 5e): lo dice la Sombra (frases 10-20 de voz_sombra). Por frase: la toma de la voz, la del susurro
# y lo que se calla despues (s; las de ci/historia/perdidos.py PAUSAS6: tras "mira arriba", las nubes y el rayo)
CAPITULO6 = [(10, "a", "a", 1.4), (11, "b", "a", 0.9), (12, "b", "a", 1.5), (13, "b", "a", 2.2), (14, "a", "a", 1.6),
             (15, "a", "a", 4.6), (16, "b", "a", 1.2), (17, "a", "a", 1.8), (18, "b", "a", 1.4), (19, "b", "a", 2.2),
             (20, "b", "a", 0.0)]
LEAD6 = 0.3


def capitulo6():
    """Las frases de la Sombra del capitulo 6, cada una con sus susurros y su eco (que se solapa con el silencio de
    detras), juntas en sounds/narracion/cap6.ogg; y sus tiempos en cinematica/cap6.json, como los de los capitulos que
    narra Aldous (asi la cinematica corta los planos y pone los subtitulos igual). Devuelve si salio."""
    ogg = os.path.join(ASSETS, "sounds", "narracion", "cap6.ogg")
    jsn = os.path.join(ASSETS, "cinematica", "cap6.json")
    with tempfile.TemporaryDirectory() as tmp:
        partes, lineas, t, fin = [], [], LEAD6, 0.0
        for n, toma, ts, pausa in CAPITULO6:
            src = os.path.join(MASTER, "sombra", f"voz_{n}{toma}.mp3")
            sus = os.path.join(MASTER, "sombra", f"susurro_{n}{ts}.mp3")
            if not os.path.exists(src) or not os.path.exists(sus):
                print("capitulo 6: falta", src if not os.path.exists(src) else sus)
                return False
            dst = os.path.join(tmp, f"frase_{n}.ogg")
            h = voz_sombra(src, sus, dst)
            d = duracion(dst)
            partes.append((dst, t))
            lineas.append([round(t, 3), round(t + h, 3)])
            fin = max(fin, t + d)
            print(f"capitulo 6, frase {n}: empieza a los {t:.2f} s, habla {h:.2f} s (con el eco {d:.2f} s)")
            t += h + pausa
        args, grafo = [], ""
        for i, (f, ti) in enumerate(partes):
            args += ["-i", f]
            grafo += f"[{i}:a]adelay={int(ti * 1000)}:all=1[d{i}];"
        grafo += "".join(f"[d{i}]" for i in range(len(partes)))
        grafo += (f"amix=inputs={len(partes)}:normalize=0:duration=longest,"
                  "alimiter=level_in=1:level_out=1:limit=0.9:attack=4:release=80:level=false[out]")
        os.makedirs(os.path.dirname(ogg), exist_ok=True)
        ff(args + ["-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "2", "-c:a", "libvorbis", "-q:a", "6", ogg])
    dur = duracion(ogg)
    os.makedirs(os.path.dirname(jsn), exist_ok=True)
    with open(jsn, "w") as fh:
        json.dump({"voz": "sombra", "audio": True, "duracion": round(max(dur, lineas[-1][1] + 1.0), 3), "lineas": lineas}, fh)
    print(f"capitulo 6: {len(lineas)} frases, {dur:.1f} s")
    return True


def efecto(src, dst, maxs, fade, lufs, entrada=0.0, graves=False, extra="", inicio=0.0):
    filtro = ((f"atrim=start={inicio},asetpts=PTS-STARTPTS," if inicio > 0 else "")
              + f"silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.02,atrim=0:{maxs},"
              + (f"afade=t=in:st=0:d={entrada}," if entrada > 0 else "")
              + f"afade=t=out:st={max(0.05, maxs - fade):.2f}:d={fade}," + extra
              + f"loudnorm=I={lufs}:TP=-1.0:LRA=11")
    if graves:
        grafo = ("[0:a]aresample=44100,asplit=3[a][b][c];[b]asetrate=44100*2,aresample=44100,atempo=0.5,volume=0.9[o1];"
                 "[c]asetrate=44100*3,aresample=44100,atempo=0.5,atempo=0.666667,volume=0.5[o2];"
                 "[a][o1][o2]amix=inputs=3:normalize=0,asoftclip=type=tanh:threshold=0.8," + filtro + "[out]")
        ff(["-i", src, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
        return
    ff(["-i", src, "-af", filtro, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])


def main():
    if not os.path.isdir(MASTER):
        print("no hay ci/eleven/master: nada que montar")
        return 1
    rey = os.path.join(ASSETS, "sounds", "rey")
    sfx = os.path.join(ASSETS, "sounds", "sfx")
    os.makedirs(rey, exist_ok=True)
    os.makedirs(sfx, exist_ok=True)
    fuego = os.path.join(MASTER, "sfx", "fuego_crepitar_1.mp3")
    dur, hab, fallos = {}, {}, 0
    for n, toma in sorted(TOMAS.items()):
        src = os.path.join(MASTER, "rey", f"voz_{n}{toma}.mp3")
        dst = os.path.join(rey, f"voz_{n}.ogg")
        try:
            if n in MONSTRUO and os.path.exists(fuego):
                h = voz_monstruo(src, dst, fuego, 1.5 + (n * 2.3) % 20)
            else:
                h = voz_normal(src, dst)
            dur[n] = round(duracion(dst), 2)
            hab[n] = round(min(h, dur[n]), 2)
            print(f"voz {n:2d}{' (monstruo)' if n in MONSTRUO else ''}: {dur[n]} s (habla hasta {hab[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO voz {n}: {e}")
    # Ser Gavran
    gav = os.path.join(ASSETS, "sounds", "gavran")
    os.makedirs(gav, exist_ok=True)
    madera = os.path.join(MASTER, "sfx", "madera_cruje_1.mp3")
    dur_g, hab_g = {}, {}
    for n, toma in sorted(TOMAS_GAVRAN.items()):
        src = os.path.join(MASTER, "gavran", f"voz_{n}{toma}.mp3")
        dst = os.path.join(gav, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            if n in ARBOL and os.path.exists(madera):
                h = voz_arbol(src, dst, madera, (n * 0.7) % 2.5)
            else:
                h = voz_gavran(src, dst)
            dur_g[n] = round(duracion(dst), 2)
            hab_g[n] = round(min(h, dur_g[n]), 2)
            print(f"gavran {n:2d}{' (arbol)' if n in ARBOL else ''}: {dur_g[n]} s (habla hasta {hab_g[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO gavran {n}: {e}")
    # el Capitan de Brumavela
    cap = os.path.join(ASSETS, "sounds", "capitan")
    os.makedirs(cap, exist_ok=True)
    dur_c, hab_c = {}, {}
    for n, toma in sorted(TOMAS_CAPITAN.items()):
        src = os.path.join(MASTER, "capitan", f"voz_{n}{toma}.mp3")
        dst = os.path.join(cap, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            h = voz_capitan(src, dst)
            dur_c[n] = round(duracion(dst), 2)
            hab_c[n] = round(min(h, dur_c[n]), 2)
            print(f"capitan {n:2d}: {dur_c[n]} s (habla hasta {hab_c[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO capitan {n}: {e}")
    # la Molinera de Velmor (parte 7f)
    mol = os.path.join(ASSETS, "sounds", "molinera")
    os.makedirs(mol, exist_ok=True)
    dur_m, hab_m = {}, {}
    for n, toma in sorted(TOMAS_MOLINERA.items()):
        src = os.path.join(MASTER, "molinera", f"voz_{n}{toma}.mp3")
        dst = os.path.join(mol, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            h = voz_molinera(src, dst)
            dur_m[n] = round(duracion(dst), 2)
            hab_m[n] = round(min(h, dur_m[n]), 2)
            print(f"molinera {n:2d}: {dur_m[n]} s (habla hasta {hab_m[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO molinera {n}: {e}")
    # Maese Odran, el Verdugo de la Arboleda (parte 7h)
    ver = os.path.join(ASSETS, "sounds", "verdugo")
    os.makedirs(ver, exist_ok=True)
    dur_v, hab_v = {}, {}
    for n, toma in sorted(TOMAS_VERDUGO.items()):
        src = os.path.join(MASTER, "verdugo", f"voz_{n}{toma}.mp3")
        dst = os.path.join(ver, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            h = voz_verdugo(src, dst)
            dur_v[n] = round(duracion(dst), 2)
            hab_v[n] = round(min(h, dur_v[n]), 2)
            print(f"verdugo {n:2d}: {dur_v[n]} s (habla hasta {hab_v[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO verdugo {n}: {e}")
    # el Barquero de la Laguna Negra (parte 7j)
    bar = os.path.join(ASSETS, "sounds", "barquero")
    os.makedirs(bar, exist_ok=True)
    dur_b, hab_b = {}, {}
    for n, toma in sorted(TOMAS_BARQUERO.items()):
        src = os.path.join(MASTER, "barquero", f"voz_{n}{toma}.mp3")
        dst = os.path.join(bar, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            h = voz_barquero(src, dst)
            dur_b[n] = round(duracion(dst), 2)
            hab_b[n] = round(min(h, dur_b[n]), 2)
            print(f"barquero {n:2d}: {dur_b[n]} s (habla hasta {hab_b[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO barquero {n}: {e}")
    # Hrodric, el Abad de la Cumbre (parte 7l)
    aba = os.path.join(ASSETS, "sounds", "abad")
    os.makedirs(aba, exist_ok=True)
    dur_a, hab_a = {}, {}
    for n, toma in sorted(TOMAS_ABAD.items()):
        src = os.path.join(MASTER, "abad", f"voz_{n}{toma}.mp3")
        dst = os.path.join(aba, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            h = voz_abad(src, dst)
            dur_a[n] = round(duracion(dst), 2)
            hab_a[n] = round(min(h, dur_a[n]), 2)
            print(f"abad {n:2d}: {dur_a[n]} s (habla hasta {hab_a[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO abad {n}: {e}")
    # la Sombra
    som = os.path.join(ASSETS, "sounds", "sombra")
    os.makedirs(som, exist_ok=True)
    dur_s, hab_s = {}, {}
    for n, toma in sorted(TOMAS_SOMBRA.items()):
        src = os.path.join(MASTER, "sombra", f"voz_{n}{toma}.mp3")
        sus = os.path.join(MASTER, "sombra", f"susurro_{n}{SUSURROS_SOMBRA[n]}.mp3")
        dst = os.path.join(som, f"voz_{n}.ogg")
        if not os.path.exists(src) or not os.path.exists(sus):
            print("falta", src if not os.path.exists(src) else sus)
            fallos += 1
            continue
        try:
            h = voz_sombra(src, sus, dst)
            dur_s[n] = round(duracion(dst), 2)
            hab_s[n] = round(min(h, dur_s[n]), 2)
            print(f"sombra {n:2d}: {dur_s[n]} s (habla hasta {hab_s[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO sombra {n}: {e}")
    # el capitulo 6, con la voz de la Sombra
    try:
        if not capitulo6():
            fallos += 1
    except Exception as e:  # noqa: BLE001
        fallos += 1
        print(f"FALLO capitulo 6: {e}")
    with open(os.path.join(ASSETS, "rey_voces.json"), "w") as fh:
        json.dump({"duraciones": dur, "gavran": dur_g, "habla": hab, "habla_gavran": hab_g, "capitan": dur_c, "habla_capitan": hab_c,
                   "sombra": dur_s, "habla_sombra": hab_s, "molinera": dur_m, "habla_molinera": hab_m,
                   "verdugo": dur_v, "habla_verdugo": hab_v, "barquero": dur_b, "habla_barquero": hab_b,
                   "abad": dur_a, "habla_abad": hab_a}, fh)
    # los personajes con dialogo: voz_N.ogg y lo que tardan en decir cada frase (voces.json, para que el texto del
    # dialogo se escriba a su ritmo)
    voces = {}
    for pj, (tomas, filtro) in PERSONAJES.items():
        voces[pj] = {}
        carpeta = os.path.join(ASSETS, "sounds", pj)
        os.makedirs(carpeta, exist_ok=True)
        for n, toma in sorted(tomas.items()):
            src = os.path.join(MASTER, pj, f"voz_{n}{toma}.mp3")
            dst = os.path.join(carpeta, f"voz_{n}.ogg")
            if not os.path.exists(src):
                print("falta", src)
                fallos += 1
                continue
            try:
                pre, _, hablado = recorte(src)
                ff(["-i", src, "-af", pre + filtro, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
                voces[pj][str(n)] = round(min(hablado + COLA, duracion(dst)), 2)
                print(f"{pj} {n:2d}: {duracion(dst):.2f} s (habla hasta {voces[pj][str(n)]})")
            except Exception as e:  # noqa: BLE001
                fallos += 1
                print(f"FALLO {pj} {n}: {e}")
    with open(os.path.join(ASSETS, "voces.json"), "w") as fh:
        json.dump(voces, fh)
    for nombre, (maxs, fade, lufs) in SFX.items():
        tomas = TOMAS_SFX.get(nombre) or ((1,) if nombre in UNA_TOMA else (1, 2))
        for i, k in enumerate(tomas, 1):
            src = os.path.join(MASTER, "sfx", f"{nombre}_{k}.mp3")
            if not os.path.exists(src):
                print("falta", src)
                fallos += 1
                continue
            dst = os.path.join(sfx, f"{nombre}_{i}.ogg")
            try:
                efecto(src, dst, maxs, fade, lufs, ENTRADA.get(nombre, 0.0), nombre in GRAVES, EXTRA.get(nombre, ""), INICIO.get(nombre, 0.0))
                print(f"efecto {nombre}_{i} (toma {k}): {duracion(dst):.2f} s")
            except Exception as e:  # noqa: BLE001
                fallos += 1
                print(f"FALLO efecto {nombre}_{k}: {e}")
    print("OK" if not fallos else f"FALLOS: {fallos}")
    return 1 if fallos else 0


if __name__ == "__main__":
    sys.exit(main())
