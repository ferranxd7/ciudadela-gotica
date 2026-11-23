# Dark Craft — notas permanentes para Claude

Mod de Forge 1.20.1 (Java 17, mappings oficiales). El usuario habla español, juega en **TLauncher** e instala con
`instalar_mods.bat` (baja la última release de GitHub). **Nunca compila**: todo se compila y se prueba en la CI.

## Reglas que ha pedido el usuario (no cambiarlas sin que lo pida)

1. **La voz de los libros (el narrador) es SIEMPRE la misma**: Piper, voz `alan` en_GB medium
   (`ci/narracion/voz.txt`), con los efectos de narrador de `ci/narracion/montar.py`. No se cambia nunca, tampoco
   por ElevenLabs. Las narraciones de capítulos nuevos se hacen con esa voz y esos efectos.
2. **Siempre hay logros (progresos)**: por cada jefe derrotado, cada capítulo de la historia, cada misión y los
   secretos. Van en `src/main/resources/data/darkcraft/advancements/`, se dan desde `story/Logros.java` y sus
   textos van en todos los idiomas. Los de jefe son `challenge` (con la fanfarria).
3. **Las voces de los personajes van en inglés** (ElevenLabs) y **los subtítulos y textos en el idioma del juego**:
   es_es, en_us, ja_jp, fr_fr, de_de, pt_br (y pt_pt, la misma). Todo texto nuevo:
   - español e inglés en `ci/historia/textos.py`;
   - japonés, francés, alemán y portugués en `ci/historia/idiomas/{ja,fr,de,pt}.py` (mismas páginas por capítulo;
     `python3 ci/historia/textos.py` comprueba que cada página cabe en el libro y avisa de lo que falte).
4. **Voces de ElevenLabs** (siempre ElevenLabs para las voces de personajes):
   - Rey Corvin: **"David - British Storyteller"** `BNgbHR0DNeZixGQVzloa`, modelo `eleven_v4` (etiquetas como
     `[deep voice]`, `[angry]`, `[shouting]`). Eco suave de sala de piedra; en la fase 3 la misma voz con efecto de
     monstruo (más grave, voz de octava baja, eco largo y fuego crepitando): lo monta `ci/eleven/procesar.py`.
   - Ser Gavran (voz "fría y despiadada"): **"Final Boss - Evil All Powerful Villain"** `d3qrd1ge6zWDvUxIeXiH`
     (elegida por el usuario), modelo `eleven_v4`. Eco de sala; en la fase 2 la misma voz más grave con madera
     crujiendo debajo (como el fuego del Rey).
   - Efectos de sonido: ElevenLabs (`eleven_text_to_sound_v3`), estilo "épico de cine" (tipo Dark Souls); sustituyen
     a los del juego donde encajan (`sfx.*` en sounds.json, `DarkCraftMod.SFX_*`).
   - La música no se puede hacer con este conector: música con licencia libre (`ci/musica/pistas.json`, con créditos).
5. **Nombres**: el mod es **Dark Craft** (id `darkcraft`, paquete `com.darkcraft`); el sitio de los jefes es el
   **Reino de Valdrenn**; comandos `/darkcraft ...`; estructura `darkcraft:valdrenn` (`/locate structure`). El
   repositorio de GitHub sigue llamándose `ferranxd7/ciudadela-gotica` (no tocar esa URL en el instalador).
6. El usuario quiere las cosas **por partes y publicadas** (cada parte con `[publicar]`).

## Compilar, probar y publicar

- La CI (`.github/workflows/build.yml`) compila, prueba un servidor real (con y sin mods) y un cliente con capturas
  y audio grabado, y sube una release `build-N`. Con `[publicar]` en el mensaje del commit es la *latest* (la que
  instala el usuario); sin él es "en desarrollo".
- Compilar en local (solo para ver errores, ~4 s): bajar la release `classpath` (asset `classpath.tgz`) a
  `/tmp/cp/cpjars` y lanzar `javac` sobre `src/main/java` con esos jars (`[cp]` en un commit la vuelve a subir).
- Bajar assets de releases: `gh api -H "Accept: application/octet-stream" repos/ferranxd7/ciudadela-gotica/releases/assets/<id>`.
  Las rutas REST de Actions (artifacts, secrets) no pasan por el proxy; `gh run list` sí funciona.
- Commits: `git -c user.name="Claude" -c user.email="noreply@anthropic.com" commit`.

## Audios de ElevenLabs (cómo se traen al repositorio)

- Se generan con el conector de ElevenLabs (en la CI no hay clave). Los enlaces firmados de los audios caducan a
  las **00:00 UTC**; desde el espacio de trabajo no se pueden bajar, desde GitHub Actions sí.
- Pasos: apuntar `{archivo, url}` en `ci/eleven/descargas.json` → push → el workflow `eleven.yml` los baja a
  `ci/eleven/master/` y hace commit él solo → en cada build `ci/eleven/procesar.py` los monta en OGG
  (`sounds/rey/voz_N.ogg`, `sounds/sfx/NOMBRE_K.ogg`, `rey_voces.json`). Si un enlace caducó, se vuelve a pedir el
  estado de la generación (da enlaces nuevos) y se repite.
- `ci/eleven/sesiones.json` guarda los flows y sesiones de cada audio.

## Ser Gavran (Parte B, por partes)

El segundo jefe, en la Sala del Árbol (TreeTower). Todo lo que pidió el usuario está en el resumen de la Parte B:
caballero gigante (~5.5 bloques) de armadura negra fundida con corteza y raíces, ojos VERDES, mandoble; poder de
raíces y espinas. Dificultad como el Rey, ~2 min por fase, 2-3 corazones por golpe, escala con jugadores y
dificultad, marcas en el suelo como el Rey.

- **B1 (fase 1)**: modelo (`tools/gavran/`), entidad, cinemática de entrada con diálogo (las puertas se cierran,
  baja del árbol y habla), sala cerrada (puerta y ascensor) durante la pelea, barra de vida de corteza y espinas
  (relleno verde que brilla, hojas que caen), ataques de la fase 1 (combos de 3-5 tajos, contraataque si le pegas
  cuando se cubre, embestida con el hombro, raíces en línea, onda al clavar la espada, 2-3 escuderos esqueleto),
  aturdido tras romperle la guardia, morir = como el Rey, música de la fase 1, comandos
  `/darkcraft gavran ir|estado|reiniciar|fase 2|matar`.
- **B2 (fase 2)**: cinemática larga (~20 s, las raíces le envuelven, crece, corteza y espinas), "el árbol
  despierta" (raíces rompen el suelo, ramas, hojas), raíces que atrapan 1-2 s, lluvia de hojas cortantes, salto a
  los balcones, raíces por todo el suelo (subir a los balcones), definitivo "Juicio de las espinas" (solo te salvas
  detrás de muros de corteza), punto débil: el corazón del árbol que brilla en su pecho. Música de la fase 2.
- **B3 (final)**: muerte con honor (se arrodilla, últimas palabras, se deshace en hojas, el árbol florece y se
  ilumina), llave de hueso + "Mandoble del Juramento" (dos manos, raíces y espinas, energía como las espadas del
  Rey), logro "Derrotar a Ser Gavran" (fanfarria), capítulo 5 (cofre, libro y narración con la voz de los libros).
- Lore: era el campeón y mejor amigo del Rey; lealtad con dudas (sabe que el pacto fue un error pero cumple su
  juramento); sabe que mataste al Rey y se lo toma con frialdad (su primera frase va de eso).

### Cómo está hecho (B1)

- Modelo: `tools/gavran/` (`partes.py` piezas y cajas, medidas en múltiplos de 0,5; `pintar.py` texturas
  procedurales; `ik.py` cinemática inversa: las poses se escriben con la mano derecha, la dirección de la hoja y el
  filo, y la izquierda agarra la empuñadura sola; `animaciones.py` fotogramas; `vista.py` vistas previas).
  `python3 tools/gavran/generar.py [--vistas carpeta]` (necesita numpy, Pillow y scipy; la primera vez tarda unos
  minutos, se cachea en `tools/gavran/.ik_cache.pkl*`, que no se sube). Sale `GavranGeometria.java`,
  `GavranAnimaciones.java` (fotogramas en cadenas que se leen al cargar: la mano izquierda lleva uno por tick),
  `gavran/GavranAnims.java` (números, duraciones y ticks de cada golpe) y `textures/entity/gavran/*.png`.
- Barra de vida: `tools/hud/barra_gavran.py` → `textures/gui/gavran_barra.png`; la dibuja `client/gavran/GavranCliente`.
- Servidor: `gavran/Gavran.java` (estados DORMIDO, INTRO, LUCHA, ATURDIDO, FIN1, MUERTE), `SalaArbol` (geometría
  de la sala: anillo entre el tronco y el muro, rama del norte sobre la puerta, aterrizaje delante del trono),
  `Raices` (estacas de raíz con entidades `block_display` que crecen y se recogen solas; onda del mandoble),
  `GavranEventos` (aparece en su rama, sala cerrada, trampas, objetos de quien cae, comandos).
- Voz: el paquete `Net.ReyHabla` con línea 100+N es Gavran (subtítulos `darkcraft.gavran.voz.N`).
- Música fase 1: "Dark Descent" de Matthew Pablo (CC-BY 3.0), id `gavran1` (tema 11 de `ClientMusic.boss`).
- Fin de la fase 1 en B1: se retira a su rama, sale "Continuará" y vuelve a empezar (hay que salir de la sala y
  volver a entrar). En B2 eso se cambia por el cambio de fase de verdad.
