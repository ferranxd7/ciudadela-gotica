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
   **Reino de Valdrenn**; comandos por sitio: `/darkcraft valdrenn ...` (cada sitio nuevo, su nombre) y
   `/darkcraft ayuda` (todos registrados en `Comandos.java`); estructura `darkcraft:valdrenn` (`/locate structure`). El
   repositorio de GitHub sigue llamándose `ferranxd7/ciudadela-gotica` (no tocar esa URL en el instalador).
6. El usuario quiere las cosas **por partes y publicadas** (cada parte con `[publicar]`).

## Compilar, probar y publicar

- La CI (`.github/workflows/build.yml`) compila, prueba un servidor real (con y sin mods) y un cliente con capturas
  y audio grabado, y sube una release `build-N`. Con `[publicar]` en el mensaje del commit es la *latest* (la que
  instala el usuario); sin él es "en desarrollo". Un commit vacío no lanza la CI (por los `paths-ignore` del
  workflow): para publicar tiene que cambiar algún fichero.
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
  `/darkcraft valdrenn gavran ir|estado|reiniciar|fase 2|matar`.
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
- Estacas de raíz: bloque `darkcraft:raiz_gavran` (sin objeto; solo lo muestran las `block_display`), texturas de
  `tools/gavran/raiz_bloque.py` (corteza casi negra con venas de savia verde).
- Balance (con netherite y protección IV en normal): tajos ~1,9 corazones, remates ~3, contraataque ~3,4 (como el
  Rey). Postura: +8 por golpe justo después de un ataque suyo, +2,5 si no, +7 al pegarle en guardia; baja sola si
  pasan 2,5 s sin golpes. En la sala y su torre no salen monstruos solos (`GavranEventos.onSpawn`) y los que se
  hubieran colado se van al empezar la entrada.
- Cinemática de entrada: el plano de la rama es de lado y casi a su altura (desde abajo le tapa la rama) y el del
  salto sigue a Gavran de verdad (`Shot.siguiendo(id, ...)`: el cliente mira a la entidad, aunque el servidor vaya
  con retraso; los puntos de `Gavran.enSalto` quedan de reserva). `SalaArbol.despejarVista` quita las hojas y
  lianas que le taparían, hay bloques de luz invisibles en la rama y en la entrada se dibuja con más luz propia.
- Servidor: `gavran/Gavran.java` (estados DORMIDO, INTRO, LUCHA, ATURDIDO, CAMBIO, FIN1, MUERTE), `SalaArbol` (geometría
  de la sala: anillo entre el tronco y el muro, rama del norte sobre la puerta, aterrizaje delante del trono),
  `Raices` (estacas de raíz con entidades `block_display` que crecen y se recogen solas; onda del mandoble),
  `GavranEventos` (aparece en su rama, sala cerrada, trampas, objetos de quien cae, comandos).
- Voz: el paquete `Net.ReyHabla` con línea 100+N es Gavran (subtítulos `darkcraft.gavran.voz.N`).
- Música fase 1: "Dark Descent" de Matthew Pablo (CC-BY 3.0), id `gavran1` (tema 11 de `ClientMusic.boss`).
- Fin de la fase 1 en B1: se retira a su rama, sale "Continuará" (subtítulo corto; la explicación va al chat) y
  vuelve a empezar (hay que salir de la sala y volver a entrar). En B2 eso se cambia por el cambio de fase de verdad.

### Cómo está hecho (B2)

- Cambio de fase (estado `CAMBIO`, 20 s, cinemática 34 `Cinematics.GAVRAN_CAMBIO`, planos en `Gavran.cambioPlanos`):
  se pone de rodillas delante del trono (pose de la rama), frase 11, `Raices.capullo` (raíces en espiral),
  `Raices.despertar` (ola de raíces por el suelo y raíces grandes que se quedan toda la fase 2, hojas cayendo,
  temblor de cámara en `ClientCine.temblorArbol`), revienta (`ponerFase(2)`: textura 2, ×1,15 de tamaño y caja,
  algo más rápido) con la animación `alzarse` y ruge (onda que empuja). Música: silencio y "A Slave To No One"
  (Jonathan Shaw, CC-BY 3.0, id `gavran2`, tema 12) desde que revienta. Título 34 "SER GAVRAN / Corazón del Árbol".
- Ataques de la fase 2 (`Atq` con `fase`; los de peso 0 los lanza la pelea): `ATRAPAR` (círculo verde a los pies
  y jaula de raíces: lentitud VII + salto negativo 1,9 s; luego `SALTO` encima del atrapado), `HOJAS` (dos tandas
  de hojas cortantes en círculos rojos sobre cada uno y alrededor), `SUELO` (clava la espada y se queda de rodilla:
  la marea de raíces cubre el suelo y quita vida cada segundo a quien lo pisa; se libra en la tarima, la escalera
  o el balcón), `JUICIO` (al bajar del 70% y del 35% de la vida de la fase: cuatro muros de corteza de verdad
  —`RaizBlock`, se deshacen solos— y 4 s después espinas en todas direcciones: solo se salva quien está tapado por
  un muro, el tronco u otra cosa sólida; da el logro `gavran_juicio`), `BALCON` (si su objetivo está en un balcón
  salta a su lado; `BAJAR` vuelve al suelo con un golpe). Animaciones nuevas: `alzarse`, `juicio`, `caer`,
  `subir_balcon`, `levantarse`.
- Punto débil: el corazón del árbol (`Gavran.corazonPos`, en el pecho a su izquierda, brilla y late): golpe
  mirando a él (a menos de 0,8 bloques de la línea de la mirada) o flecha que llega a él = ×2,2 de daño y postura;
  la primera vez dice la frase 16 y da el logro `gavran_corazon`.
- Fin de la fase 2 en B2: se retira a su rama como en B1 (subtítulo `continuara2`, logro `gavran_fase2`); en B3 eso
  se cambia por la muerte con honor.

### Cómo está hecho (B3)

- Muerte con honor (estado `MUERTE`, 28 s, cinemática 35 `Cinematics.GAVRAN_MUERTE`, planos en `Gavran.muertePlanos`):
  se arrodilla delante del trono, frases 17 y 18, se deshace en hojas, `SalaArbol.florecer` (las hojas de la sala y
  de toda la copa pasan a azalea florida y cerezo, con luces invisibles por la sala a cuatro alturas y entre las
  hojas de la copa) y `VictoriaGavran.cofre` sube el cofre del capítulo 5 donde se arrodilló. Desde que sube el cofre el estado guardado
  es `MUERTO` (si el mundo se cierra a mitad, no vuelve; `GavranEventos.asegurar` abre la puerta y repone el cofre
  si faltara). Logros `gavran_fase2` (al caer) y `gavran` (challenge, cuando sube el cofre).
- Cofre del capítulo 5 (`VictoriaGavran.llenar`, solo para quien le venció): Mandoble del Juramento, Llave de Hueso
  (`LlaveHueso`: de momento solo dice que aún no se abre nada), netherite, manzanas, botellas de experiencia,
  tótems, libros encantados y azaleas floridas.
- Mandoble del Juramento (`armas/MandobleJuramento`, pose a dos manos `client/armas/MandobleCliente`): con la otra
  mano ocupada pega ×0,55 y no hace combos. Energía de `Combos` (+11 por golpe, +16 cayendo): `RAICES_M` (Shift +
  clic: línea de raíces) y `ESPINAS_M` (Ctrl + clic: salta y clava; espinas y raíces alrededor). Las raíces y
  espinas son `block_display` de `armas/EfectosRaiz` (etiqueta `darkcraft_efimera`: si quedan al cerrar el mundo,
  al volver no entran). Texturas: `tools/gavran/objetos.py`.
- Capítulo 5 con narrador: el cronista Aldous se despide (`NARRACION5`/`PAUSAS5` en `ci/historia/textos.py`,
  `narracion5` en los otros idiomas). `textos.py` escribe un `ci/narracion/capN.txt` por capítulo narrado y
  `ci/narracion/generar.sh` monta todos (`montar.py modelo voz N` → `sounds/narracion/capN.ogg` y
  `cinematica/capN.json`). `Timeline.narrated(ch)` (1 y 5), `Timeline.groups(ch)` (`GROUPS5`: la sala, el árbol en
  flor, la copa en la tormenta, la llave, el cielo y el jugador), `CineShots.chapter5` (planos cerca de la sala: no
  se mueve al jugador; los de fuera giran solos si la cámara chocaría con ramas u hojas). Durante la cinemática la
  llave de hueso flota sobre el cofre, siempre de cara a la cámara (`VictoriaGavran.llaveFlotante`), y en el
  capítulo 5 sí se ve a los jugadores (el último plano es el suyo); con la cámara de cine no se dibujan los nombres.

## Tanda «mapa y Fortaleza de Hueso» (pedida el 23-11-2026, en tres partes)

Respuestas del usuario (las 60 preguntas): primero los arreglos, luego el mapa y el menú de la M, al final la
fortaleza; cada parte publicada. Juega solo y con amigos.

### Parte 1: arreglos

- Voces de los jefes (`rey/Voces.java`: lo que dura cada frase, de `rey_voces.json`; pausa de 1,2 s): la entrada
  del Rey y la de Gavran se cuentan con lo que duran sus frases y la pelea no empieza hasta que acaban de hablar;
  el cambio de fase del Rey y la pelea en los volcanes esperan a la frase; las burlas (`hablaSiCalla`) solo si
  callan. En el cliente (`ReyCliente.habla`) una frase nunca corta a otra: espera en cola; las de relleno
  (`Voces.deRelleno`) se descartan si llevan más de 2 s esperando. Subtítulos: lo que dura la frase + 2,75 s.
- Comandos por sitio en `Comandos.java`: `/darkcraft valdrenn localizar|ir|historia ...|rey ...|arbol ...|gavran ...`
  y `/darkcraft ayuda` (clic para escribirlos; textos `darkcraft.ayuda.*`). Cada sitio nuevo tendrá su nombre.
- Torre-árbol limpia: `SalaArbol.quitarFollaje` quita follaje sin avisar a los vecinos y con lo que colgaba debajo
  (antes caían bayas y lianas al suelo); `SalaArbol.limpiar` (desde `GavranEventos.asegurar`) recoge las lianas que
  bajaban de F+8 (una vez por sesión) y quita las bayas, lianas y hojas caídas en toda la torre (no lo que haya
  tirado un jugador). Las lianas nuevas nunca bajan de F+8 (`MIN_LIANA`).
- Rey: `VIDA_FASE` 780 (antes 1200). `/locate structure darkcraft:valdrenn` ya da el más cercano (celdas de 16 chunks).
- Tronos sin huecos detrás: en la sala del árbol la tarima llega hasta el muro (antes quedaba una zanja de dos bloques
  con los cofres del tesoro flotando) y el estandarte del norte ya no se come un bloque del respaldo; en el salón del
  trono el muro de detrás va sin ventanas (`Building.detrasDelTrono`; asomaban a los lados del respaldo), con
  calaveras encima de los lados del respaldo y estandartes rojos a los lados. En mundos de antes lo arreglan
  `SalaArbol.taparTrono` (desde `limpiar`) y `Sala.taparTrono` (desde `ReyEventos`, una vez por sesión).

### Parte 2: el Espejo de la Sombra y el libro de la M

- Servidor (`espejo/Espejo.java`): despierta al pisar Valdrenn (`Storm` → `enIsla`; en mundos de antes, al entrar si ya
  tenía el logro `encontrar`), con aviso y sonido. Lo que saben todos (SavedData `darkcraft_espejo`: reinos visitados y
  lugares descubiertos: a 40 bloques los lugares, 14 los cofres, 30-36 los jefes; los libros desvelan: el cofre 1 al
  despertar, cap. 1 → cofre 2, cap. 2 → Rey y cofre 3, cap. 3 → cofre 4, cap. 4 → Gavran y cofre 5) y lo de cada
  jugador (NBT persistente `darkcraft.espejo`: colección de objetos del mod que ha tenido, bestiario, diario con el día
  del mundo; las entradas del diario salen de los logros, `darkcraft.diario.<logro>`). Paquetes `Net.EspejoEstado`
  (todo, cuando cambia), `Net.EspejoPos` (cada segundo: el sitio donde está y los demás jugadores) y `Net.EspejoAccion`
  (logros `libro_sombra` y `primera_marca`). Logro `cartografo` (todos los lugares de un reino). Comandos
  `/darkcraft valdrenn espejo despertar|dormir|revelar`.
- Cliente (`client/espejo/`): `Exploracion` (mapa explorado por mundo y dimensión en
  `.minecraft/darkcraft/espejo/<mundo>/<dim>/r.X.Z.bin`: regiones de 512×512, un píxel por bloque, con los chunks que
  tiene cargados el cliente), `Marcas` (`marcas.json`), `EspejoHud` (el espejo arriba a la derecha: textura de 320×320
  con estilo de reflejo oscuro dibujada en un abanico con la forma del cristal que gira con el jugador, humo, el ojo de
  la cresta que late, la N del norte, lugares, cofres, jefes, marcas, amigos con su cara, nombre del sitio y
  coordenadas; bajo techo, la planta del piso), `LibroScreen` (Historia: crónica, libros perdidos —Parte 3— y diario;
  Mapa: niebla de la Sombra, regiones con estilo de tinta, selector de dimensión, marcas con clic derecho, tamaño del
  espejo; Logros: logros, colección y bestiario con la criatura dibujada), `Escena` (el personaje del jugador escribe la
  marca con la pluma en 3D, 2,6 s, `sfx.pluma_escribe`). Teclas M, N, + y − (las del numérico, y el + / − de la
  distribución del teclado si no chocan con otra tecla). Opciones en `.minecraft/darkcraft/espejo/opciones.properties`.
- Ojo al dibujar en la GUI (1.20.1): `g.fill`, `drawString` y `PlayerFaceRenderer` (el sombrero) dejan la mezcla
  APAGADA al acabar, y `g.blit` no la enciende: lo transparente sale opaco (manchas negras, brillos como discos rojos).
  Antes de cada blit con transparencia, `EspejoHud.mezcla()` (o `suma()` para brillos). Las regiones del mapa del
  libro se dibujan con `EspejoHud.cuadro` (esquinas en coma flotante compartidas: sin rendijas entre regiones).
- Texturas: `tools/espejo/texturas.py` (+ `iconos.py`, iconos en pixel art escritos como texto). Sonidos de ElevenLabs:
  `espejo_despierta` (toma 3), `pluma_escribe` (tomas 2 y 4), `libro_abre` (toma 3), elegidos en `TOMAS_SFX` de
  `procesar.py`; de esta tanda se bajaron las 4 tomas de los 14 efectos (también los de la Parte 3).

### Parte 3: la Fortaleza de Hueso

- Sitio (`fortaleza/Fortalezas.java`): una por cada Reino de Valdrenn, a 2500 bloques, en la primera de 16 direcciones
  (en el orden que da la semilla) que queda a 1500+ de cualquier reino; patio a Y = 320 (sobre las nubes, que estan a
  192). `/locate structure darkcraft:fortaleza_hueso` (`fortaleza/Localizar`, como la de Valdrenn).
- No se genera con el mundo: la levanta el servidor chunk a chunk (uno por tick) cuando alguien se acerca a 420 bloques
  (`FortalezaEventos.encolar/construirUno`, chunks hechos en el SavedData `darkcraft_fortalezas`), asi funciona igual en
  mundos de antes. Al construir cada chunk pinta el bioma `darkcraft:fortaleza` (lluvia, cielo gris; como /fillbiome) y
  aplica el estado guardado (puerta abierta, portal).
- Plano (`gen/FortalezaPlan`): isla de ceniza y hueso (`Island`), `gen/FortalezaHueso` (murallas de 60x60 con cuatro
  torres, puerta con calavera gigante y reja de hueso `darkcraft:reja_hueso` irrompible, patio con costillas, torre del
  homenaje de 4 pisos con escalera de caracol, el cofre del acertijo (`chests/acertijo`) en la planta baja, atriles con
  los libros perdidos 6-11 y en la azotea el arco del portal y el altar), el camino de huesos hasta el borde con el arco
  de colmillos, ruinas y el mar de nubes (nunca donde va la escalera).
- La llave de hueso (`armas/LlaveHueso`): apunta su fortaleza en su NBT la primera vez; en la mano deja un rastro de luz
  (solo lo ve quien la lleva); clic derecho: dice hacia donde y cuantos bloques; a menos de 48 de la escalera la levanta
  (`fortaleza/Puente`: escalera cuadrada de caracol de 3 de ancho con descansillos, de hueso y piedra negra con nubes,
  desde el suelo hasta Y, y un puente corto al arco; en ~8 s), en su hueco la corriente de nubes (levitacion; agachado,
  bajada lenta; arriba te deja en el descansillo); delante de la reja abre la puerta (cinematica 36, 15 s, la reja sube
  fila a fila). Se gasta al forjar el amuleto.
- Acertijo (`LibroAcertijo` + `client/fortaleza/AcertijoScreen`): 3 paginas en verso y un sello que dibuja la forja; al
  leerlo, logro y la receta en el libro de recetas. Amuleto (`AmuletoHueso`, receta DND/NKN): al craftearlo, cinematica
  37 de la forja (gira alrededor de la mesa). En el altar: se gasta, capitulo 6 narrado (habla la Sombra con la voz de
  siempre; `perdidos.NARRACION6`, `Timeline.GROUPS6`, `CineShots.chapter6`) y el portal (`portal_sombra`) se abre a
  mitad; no se puede cruzar (empuja hacia atras). Al acabar, el libro del capitulo VI (lo escribe la Sombra).
- Libros perdidos (`fortaleza/Perdidos`, textos en `ci/historia/perdidos.py`): 1-5 en Valdrenn (se ponen en atriles la
  primera vez que alguien pasa cerca de su sitio), 6-11 en la fortaleza. Se apuntan al leerlos en el atril o al tenerlos;
  salen en la Historia del libro de la M. Logros `libro_perdido` y `libros_perdidos`.
- Zona (130 bloques): lluvia sin tormenta con el dia y la noche normales, musica "The Dark Amulet" (Matthew Pablo,
  CC-BY 3.0, id `fortaleza`, `ClientMusic.fortaleza`), caballeros zombi en la isla. Logros: `fortaleza`, `puente`,
  `puerta_hueso`, `acertijo`, `amuleto`, `portal` (desafio), `capitulo6`. Comandos `/darkcraft fortaleza ...`.
- Efectos de ElevenLabs: `llave_rastro` (tomas 2 y 3), `puente_hueso` (1 y 2), `puerta_hueso` (1), `amuleto_forja` (1),
  `portal_abre` (1).
- Luz y monstruos: dentro (torre del homenaje, torres, puerta) hay faroles de almas a media altura, antorchas de almas
  en las columnas y bloques de luz invisibles (`FortalezaHueso.luces`); en la isla (Y >= 280, a 150 bloques) no salen
  monstruos solos (`FortalezaEventos.onSpawn`): la guardan los caballeros zombi. Al empezar el capitulo 6 se quitan los
  monstruos de la azotea (`despejarAzotea`). Junto al altar, candeleros de hueso con farol de almas (el fuego de verdad
  echa humo negro, las hogueras humo gris: delante del portal, nada de eso).
- La isla es de ceniza y hueso (grava, toba, basalto, tierra de almas, calcita; `Island.subsurface` para el borde).
- Construir: un chunk por tick, pero si uno tarda T ms se descansan T/50 ticks (en ordenadores lentos no se queda atras).
- Texturas (`tools/fortaleza/texturas.py`): el portal son vetas de energia con ondas de un ciclo por bloque (casa con el
  de al lado: no se ven baldosas).
- Prueba de la CI: las vistas de fuera van a menos de 80 bloques (distancia de vision 7) con `mc.smartCull = false` y
  esperando ~3 s a que se compilen los chunks recien hechos; para abrir un cofre desde el servidor hay que estar a menos
  de 8 bloques (si no, el servidor lo cierra en el acto).
- La llave apunta a la Fortaleza de su reino: el cofre de Ser Gavran ya la da apuntada (`LlaveHueso.apuntar`); una llave
  sin apuntar coge la fortaleza donde se esta (a 400 bloques) o la del reino mas cercano, nunca "la fortaleza mas cercana"
  (la de un reino vecino puede quedar mas cerca que la propia).
- La escalera (`Puente`) lleva cuatro pilares de hueso en las esquinas (a OUT + 1, por fuera de los escalones) con un
  anillo de hueso en cada vuelta y faroles de almas en la barandilla; todo crece de abajo arriba a la vez.
