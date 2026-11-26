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

## EL PLAN GRANDE (24-11-2026): Dark Craft pasa a ser una aventura completa — leer `PLAN.md`

El usuario respondio 138 preguntas; estan todas en **`PLAN.md`** (leer el apartado de la parte que toque antes de
empezarla). Lo esencial, que cambia reglas de arriba:

- **Ya no hay mundo normal** (ni Nether ni End, ni monstruos de Minecraft): mundo nuevo propio, un **mar de nubes** con
  islas flotantes; **modo aventura** (no se rompen ni ponen bloques); los mundos viejos no hace falta que funcionen.
  Recorrido: clase -> cinematica -> isla del pueblo abandonado (5 piezas del barco, mini-jefe el Capitan) -> barco
  volador (cinematica) -> Valdrenn -> camino de nubes -> torre con cupula de cristal -> portal -> **Morvain, las Tierras
  Marchitas** (16.000 x 16.000, disenado a mano, colosal; castillo encadenado en el cielo; la Sombra en el centro).
- **Interfaz 100% del mod en pixel art** (piedra oscura, oro gastado, rojo): nada de pantallas de Minecraft (titulo,
  opciones, graficos, shaders, mods, inventario, libros, dialogos, cinematicas con marco pixel art en vez de bandas
  negras, botones [E] en pixel art). Tecla **R** = menu del mod; **M** = solo el mapa; ESC = pausa (tambien propia).
- **Sistemas**: vida / mana / estamina (arriba a la izquierda, rojo / azul / verde), sin corazones ni hambre ni comida
  (frascos rojos y azules recargables en santuarios; frutos con resistencias); cruz de acceso rapido abajo a la
  izquierda; inventario propio por categorias; rol completo como Elden Ring (8 atributos, escalado con letras, peso,
  resistencias); talismanes (1 -> 4 huecos); hechizos con baston o sello; combate propio con combos (estilo Epic
  Fight), voltereta, fijar objetivo; 10 clases; armaduras con modelo propio.
- **Ecos de la Sombra** (la moneda; contador abajo a la derecha): se ganan matando; al morir conservas el equipo y
  los ecos se quedan en una **espada de luz** donde moriste ("Pulsa [E]" para recuperarlos; si mueres otra vez, se
  pierden). Sirven para subir de nivel, comprar, mejorar armas y aprender habilidades.
- **Santuarios** = hogueras de almas con modelo propio (descansar, reaparecer, teletransporte en Morvain); los enemigos
  solo reaparecen al morir o descansar.
- **Voces**: TODO personaje con voz de ElevenLabs (ingles + subtitulos). La Sombra tiene voz propia nueva (masculina,
  grave, susurrante; eco de vacio y susurros); los capitulos de Morvain los narra **la guia de los santuarios** (voz de
  mujer serena). La voz de Aldous (Piper) queda solo para los capitulos 1-5.
- **Idiomas**: cada parte sale en **espanol e ingles**; ja/fr/de/pt se anaden despues en tandas (cambia la regla 3).
- **Distant Horizons (LGPL-3.0), Oculus y Embeddium** van dentro del .jar si su licencia lo permite, con creditos;
  256 chunks por defecto; el usuario tiene PC potente y juega con shaders (Oculus); con amigos, **servidor dedicado**
  (Aternos, exaroton): todo tiene que funcionar en servidor dedicado, mundo compartido y progreso de rol por jugador.
- Los carteles de madera de los cofres de capitulo se quitan (el cofre se marca con brillo); el espejo va sin coordenadas
  y sera redondo, con una brujula arriba en el centro.

### Plan grande, parte 1: arreglos rapidos (hecha)

- **Dialogos de los jefes sin pausas**: `ci/eleven/procesar.py` mide en el audio original donde acaba de hablar cada
  frase (`silencedetect`), quita el silencio del principio y apunta en `rey_voces.json` `habla` / `habla_gavran` (el
  segundo en que acaba de hablar, ya con lo que estira la voz de monstruo o de arbol y 0,10 s de cola). `Voces.ticks`
  usa eso (antes, lo que duraba el fichero: ~0,5 s de silencio y eco de mas), `Voces.PAUSA` = 6 ticks (0,3 s) y
  `SUBTITULO_EXTRA` = 16 (0,8 s). El Rey se levanta MIENTRAS dice la frase 3 y acaba justo al acabar de hablar; Gavran
  salta, saluda y empieza la pelea nada mas acabar cada frase; en su muerte las frases 17 y 18 van seguidas y se deshace
  al acabar. El cliente ya no corta la cola del eco de la frase anterior.
- **La Llave del Arbol en la puerta** es una entidad `item_display` (`TorreArbol.llaveEnLaCerradura`): entra de punta,
  gira un cuarto de vuelta y se desvanece antes de abrirse las hojas. Antes la dibujaba el cliente en un evento de
  render (`ReyCliente.dibujarLlave`, ya sin usar): con shaders (Oculus) no se veia, entraba por el mango y quedaba de
  canto. **Regla: lo que se tenga que ver en el mundo, con entidades (display) o bloques, no dibujado a mano en eventos
  de render** (el usuario juega con shaders). **Ojo con las `item_display`: el juego dibuja el objeto dado la vuelta
  (media vuelta sobre el eje Y, `DisplayRenderer$ItemDisplayRenderer`) ANTES de la matriz `transformation`**: lo que en
  la textura esta a la derecha llega a la matriz a la izquierda (`TorreArbol.llaveM` lo tiene en cuenta).
- **Sin monstruos de Minecraft** (`world/SinMonstruos`, "desde ya"): se corta todo lo que sale solo (natural, con el
  terreno, estructuras, patrullas, asaltos, generadores, refuerzos, jinetes) y los que quedaban de antes se van al
  cargar su chunk (salvo con nombre o con una etiqueta `darkcraft...`, como los escuderos de Gavran). Los enemigos del
  mod, los comandos y los huevos siguen igual.
- **Sin carteles en los cofres de capitulo** (`StoryChest`, `StoryEvents.placeChapter`); los de mundos de antes los quita
  `StoryEvents.buscar` (cada 2 s, chunks de alrededor de cada jugador). Los cofres brillan (`StoryEvents.brillar`: polvo
  dorado que se vuelve rojo y destellos, por jugador; casi nada si ya leyo ese capitulo).
- **Espejo sin coordenadas** (`EspejoHud.texto`: solo el nombre del sitio).
- **Escalera de la torre del homenaje**: una columna de la planta baja caia en el anillo de la escalera de caracol y
  tapaba un escalon; ademas la valla del hueco encerraba la salida en los pisos 1 y 2. `FortalezaHueso.enEscalera`,
  `despejarEscalera`, `vallasEscalera`; en fortalezas ya levantadas, `FortalezaEventos.parchear` (una vez,
  `Estado.parche`). (Desde la parte 5a esa fortaleza ya no existe: se rehace como `gen/TorreCupula`.)

### Plan grande, parte 2a: HUD nuevo, Ecos de la Sombra, estamina y muerte

- **Arte en pixel art** (`tools/ui/`, todo dibujado al tamano real, un pixel de textura = un pixel de interfaz):
  `arte.py` (paletas PIEDRA / ORO / ROJO / AZUL / VERDE, `Lienzo`, `Atlas` que escribe las constantes de Java),
  `hud.py` → `textures/gui/hud.png` + `client/ui/HudTex.java` (emblema, barras, ecos, placa, teclas, avisos, floritura,
  cenefa y ojo de las cinematicas, brujula, botones, marcas, banda), `espejo.py` → `espejo_r28/36/46.png` (el espejo
  redondo: mitad izquierda el marco, mitad derecha el cristal), `fuente.py` → la **letra gotica** `darkcraft:gotica`
  (mayusculas de 17 px) y `darkcraft:gotica_m` (13 px), sacadas de New Rocker (SIL OFL, en `tools/ui/fuentes/`) como
  fuentes de mapa de bits (lo que no traen cae en la letra del juego), `espada_luz.py` → modelo 3D y textura de
  `darkcraft:espada_luz`. Vista previa de todo sobre una captura: `python3 tools/ui/vista.py captura.png 2 carpeta`.
- **Cliente** (`client/ui/`): `Pix` (dibujar trozos del atlas: tiras, tres trozos, espejo, franjas que se apagan),
  `Letra` (la gotica con contorno; `yArriba` = borde de arriba de las mayusculas), `Hud` (capa `hud`: emblema y barras
  de vida / mana / estamina arriba a la izquierda, contador de ecos abajo a la derecha con "+N", brujula arriba en el
  centro con el cofre que toca y la espada de luz, aviso "Pulsa [E]..." encima de la barra de objetos; quita vida,
  comida, armadura y experiencia de Minecraft), `ClientRol` (lo que manda el servidor; la tecla del inventario junto a
  la espada recoge los ecos —se consume en `ClientTickEvent` START, antes de que el juego abra el inventario—; agotado
  no deja saltar ni atacar; los ecos de un enemigo vuelan al jugador como particulas), `Carteles` (capa `carteles`:
  JEFE ABATIDO, zona descubierta, ECOS RECUPERADOS; hacen cola, esperan a que no haya cinematica ni aviso del espejo),
  `MuerteScreen` (sustituye a `DeathScreen` —y hereda de ella, que el juego la cierra sola al reaparecer—: HAS MUERTO,
  botones de piedra y oro; en extremo se deja la del juego), `Marco` (las bandas de las cinematicas: piedra, cenefa de
  oro y rojo y el ojo colgando; lo usan `ClientCine` y la entrada de los jefes en `ReyCliente`).
- El **mana** de la barra azul es la energia de las armas de `Combos` (0-100; las muescas son lo que cuesta cada combo);
  la barra de energia vieja ya no se dibuja. La **brujula** se esconde mientras hay barra de jefe.
- **El Espejo es redondo** (`EspejoHud`: `RADIOS` 28 / 36 / 46 segun el tamano elegido, abanico circular, una cuenta
  roja en el aro hacia el norte, la espada de luz como marca dorada).
- **Servidor** (`rol/`): `Ficha` (NBT persistente `darkcraft_rol` del jugador: ecos, espada {dim, x, y, z, ecos}, zonas
  vistas), `Rol` (estamina 0-100: atacar 8, mandoble 13, cubrirse 6 + 2,2 x golpe, y solo en combate correr 7,2/s y
  saltar 7; se recupera a 30-44/s tras 0,8 s; a 0 queda "agotado" hasta volver a 22: no ataca, y el nivel de comida
  pasa de 10 a 6 para que el propio juego no deje correr; comida fija = sin hambre ni regeneracion; (en la 2a comer
  curaba; desde la 2b la comida no se come); ecos al matar = vida x 2,2 + golpe x 7 + armadura x 3 para cada jugador
  que le pego; Rey 9000 y Gavran 14000 con el cartel; al morir, `keepInventory` siempre y los ecos a la espada: en el suelo bajo el jugador, o en el
  ultimo suelo firme si fue caida de mas de 24, lava o vacio; `zona()` la llama el Espejo cada segundo),
  `Espadas` (la espada que se ve: `item_display` con brillo y contorno dorado, etiqueta `darkcraft_espada_luz`, se
  pone a menos de 96 bloques de su dueno y nunca vuelve del disco; chispas y zumbido solo para el dueno).
  Paquetes `Net.RolEstado`, `Estamina`, `Cartel`, `Ecos` (de servidor a cliente) y `Recuperar`. Comandos
  `/darkcraft ecos [dar <n> | quitar | cartel <1-3>]`.
- **Textos** de la interfaz nueva: `ci/historia/rol.py` (espanol e ingles; los demas idiomas salen en ingles).
- **Sonidos** (ElevenLabs, 4 tomas de cada; elegidas en `TOMAS_SFX`): `has_muerto` (3), `jefe_abatido` (1),
  `zona_descubierta` (3), `ecos_recuperados` (4), `ecos_ganados` (2 y 4), `espada_luz` (2), `guardia_rota` (1 y 3).
- La prueba del cliente tiene su tramo `rol()` (capturas `cliente_h01..h14`) y dura ya ~17,5 min: limites subidos
  (`LIMIT` 20 min, `timeout 33m`).

### Plan grande, parte 2b-1: santuarios, frascos y subir de nivel

- **El santuario** (`santuario/SantuarioBlock`, bloque `darkcraft:santuario`, irrompible, propiedad `encendido`): un
  brasero de piedra con runas y cuatro garras de hueso y fuego azul palido. Modelo y texturas en pixel art de
  `tools/santuario/modelo.py` (22 cajas; encendido, la llama son cuatro planos con textura animada de 12 fotogramas).
  **Ojo: toda textura de bloque u objeto tiene que medir potencias de dos (tambien cada fotograma)**: si no, Minecraft
  baja el suavizado a distancia de TODOS los bloques (por eso la llama de 16 x 20 va dentro de fotogramas de 16 x 32 y
  el modelo usa solo su parte de abajo). Las llamitas, las motas y el rumor del fuego los pone el cliente
  (`client/ui/ClientSantuario`, que busca los santuarios de alrededor una vez por segundo).
- **Lo que hacen** (`santuario/Santuarios`): apagado, quien llega lo enciende (cartel SANTUARIO ENCENDIDO con su nombre,
  logro `santuario`); encendido, se descansa: vida entera, fuera los males, frascos, mana y estamina llenos, punto de
  reaparicion alli (`setRespawnPosition` forzado en el sitio donde esta el jugador) y se abre su menu. Con enemigos
  encima (en combate) no se puede: aviso corto. Se usa con clic derecho o con la tecla del aviso (la del inventario)
  **mirandolo** de cerca (solo con estar al lado no: hay que poder abrir el inventario). Despertar en una cama tras pasar
  la noche tambien cuenta como descansar (mientras siga habiendo mundo normal).
- **Donde estan**: en lo que ya estaba construido se ponen solos la primera vez que alguien pasa cerca (`Santuarios.tick`,
  como los libros perdidos; SavedData `darkcraft_santuarios` con el sitio y el nombre de cada uno): en cada Reino de
  Valdrenn, junto a la Torre de Ascenso, junto a la Fuente Mayor y en la recepcion del arbol (antes de la puerta de Ser
  Gavran); en la Fortaleza de Hueso, en el rellano de la puerta. `/darkcraft rol santuario` pone uno delante.
- **Atributos y nivel** (`rol/Atributo`, `rol/Nivel`, en `Ficha`): Vigor, Mente, Aguante, Fuerza, Destreza, Inteligencia,
  Fe y Arcano, todos a 10 de base (nivel 1 = la suma - 79). `Nivel` tiene las cuentas (las usan igual servidor y
  cliente): coste de cada nivel (la curva de Elden Ring: 673 el primero), vida (20 con Vigor 10; +2 por punto hasta 30,
  +1 hasta 50, +0,4 despues), mana (100; +6 hasta 35, +3 hasta 60, +1), estamina (100; +3 hasta 40, +1) y dano de las
  armas (+1,2 % por punto de Fuerza y +0,8 % por punto de Destreza). `Rol.aplicar` lo pone en el jugador (modificadores
  de atributo que se guardan con el, mana y estamina maximos por jugador) al entrar, reaparecer y subir de nivel;
  `Rol.subir` (paquete `Net.SubirNivel`) lo comprueba todo en el servidor: junto al santuario en el que se descanso y con
  ecos de sobra. El mana maximo ya no es `Combos.MAX`: es `Rol.manaMax(jugador)`. Inteligencia, Fe y Arcano se guardan
  ya; haran algo cuando lleguen hechizos y objetos.
- **Frascos** (`rol/Frascos`): NO son objetos del inventario, van en la ficha (3 de vida y 1 de mana al empezar; hasta
  14; mejora 0-12). Teclas **Z** (vida) y **V** (mana) (`client/ui/Teclas`). Beber dura 26 ticks y hace efecto en el 14
  (cura 7 + 34 % de la vida maxima; el azul da 40 + 30 % del mana); mientras, se anda despacio y no se ataca. Con la
  vida entera o el frasco vacio no se bebe (el frasco del HUD se sacude). Se rellenan al descansar y al reaparecer; en
  el santuario se reparten entre vida y mana. Lo que se ve (`client/ui/Bebida`): en tercera persona el brazo sube a la
  boca con el frasco (pose de brazo propia puesta en `RenderPlayerEvent.Pre`, y el objeto de la mano se cambia por el
  frasco solo mientras se dibuja al jugador); en primera persona, el gesto de beber del juego dibujado en
  `RenderHandEvent`. Los objetos `frasco_vida` / `frasco_mana` solo existen para dibujarlos.
- **La comida ya no se come** (`Rol.onComer` corta el gesto y avisa); lo que tiene efectos (manzana dorada) si.
- **Menus** (`client/ui/`): `tools/ui/menu.py` → `textures/gui/menu.png` + `MenuTex` (losa de piedra con marco de oro en
  nueve trozos, rebaje oscuro, fila elegida, flechas, iconos de los atributos, frascos y santuario); `Panel` (dibujar
  esas piezas, teclas dibujadas y los sonidos de los menus), `SantuarioScreen` (losa a la izquierda: subir de nivel,
  repartir frascos, marcharse), `NivelScreen` (atributos con flechas; nivel, ecos, coste y lo que cambia antes →
  despues; confirmar). Se manejan con raton o teclado y se cierran solos si pegan al jugador. Caben en 426 x 240 (lo
  mas pequeno que da la escala automatica). Vista previa sin abrir el juego: `python3 tools/ui/vista_menus.py
  captura.png 2 carpeta /tmp/cp/cpjars/client-extra.jar` (con la letra de Minecraft de verdad).
- **HUD**: los frascos abajo a la izquierda (hueco de piedra, cuantos quedan, su tecla); las barras de mana y estamina
  crecen con sus maximos; la vida que se cura se ve subir; el aviso de "Pulsa [E]..." vale para la espada de luz y para
  el santuario; avisos cortos (`Net.Cartel.AVISO`: banda pequena con la letra del juego encima de la barra de objetos).
- **La letra gotica**: la N, la Ñ y la L de New Rocker traen una cola larga que en una fuente de mapa de bits separaba
  la letra siguiente ("SAN_TUARIO"); `tools/ui/fuente.py` la recorta donde acaba el avance de la letra.
- Sonidos (ElevenLabs): `santuario_enciende` (1), `santuario_fuego` (3, en bucle solapado), `santuario_descanso` (3),
  `frasco_beber` (2 y 1), `nivel_sube` (2), `menu_abre` (3), `menu_mueve` (1), `menu_acepta` (4), `menu_cancela` (3).
- Comandos: `/darkcraft rol santuario | frascos [<n>] | frascos mejora <n> | nivel [reiniciar] | atributo <nombre> <valor>`.
- La prueba del cliente tiene el tramo `santuarios()` (capturas `cliente_s01..s17`) y `cliente_01c_santuario_torre`.
- El santuario de la Torre de Ascenso va ARRIBA de la torre, en la plataforma (`plan.cine().ascX/ascZ`, a -4, +2 del
  centro, radio de busqueda 3): el nombre "torre_ascenso" del mapa esta desplazado fuera de la torre, en el aire.

### Plan grande, parte 2b-2: el menu (R), el inventario propio y la cruz

- **Ya no se ve el inventario de Minecraft** en supervivencia: la tecla del inventario (E) abre `MenuScreen` en la
  pestana Inventario; la R (`Teclas.MENU`) la abre en Equipo. Pestanas Equipo / Inventario / Estado / Sistema
  (`MenuScreen`, ~1100 lineas: losa de piedra, ranuras de `menu.png`, el personaje girando a la derecha; teclado y
  raton). Sistema lleva al Libro de la M, a los controles y a las opciones.
- **Los huecos** (`rol/Equipo`): mano derecha = barra 0-2 (la que se lleva es la elegida); mano izquierda = la otra mano
  (40) con dos de repuesto (3 y 4); objetos rapidos = 5-8; armadura 36-39; bolsa = 9-35. `Net.EquipoAccion`
  (MOVER / GUARDAR / TIRAR / IZQUIERDA) lo hace en el servidor; lo que se recoge entra primero en lo que ya hay y si no
  en la bolsa (`Equipo.onRecoger`), con su aviso (`Net.Conseguido` → `client/ui/Conseguidos`, abajo en el centro).
- **Teclas** (`client/ui/ClientEquipo`, START de cada tick, antes que el juego): 1-3 arma derecha; 4-5 y F y flecha
  izquierda cambian la mano izquierda; 6-9 eligen objeto rapido; flecha abajo, el siguiente; G lo usa (un frasco se
  bebe; lo demas se pone en la mano mientras se mantiene la G y vuelve el arma); rueda = arma de la derecha; Q (soltar)
  ya no tira nada. La barra de objetos y el nombre del objeto de Minecraft no se dibujan.
- **La cruz** abajo a la izquierda (`client/ui/Cruz`): arriba el hechizo elegido (desde la 2d-2), derecha arma, izquierda la otra mano,
  abajo el objeto rapido (los frascos van ahi).
- La prueba del cliente tiene el tramo `menu()` (capturas `cliente_q01..q15`).

### Plan grande, parte 2c: el combate propio

- **Armas** (`combate/Armas`, lo usan servidor y cliente): tipos PUNOS, DAGA, ESPADA, MANDOBLE, HACHA, LANZA, ALABARDA
  (`Armas.de`: mano vacia = punos; Mandoble del Juramento; Espada del Rey; alabarda; tridente = lanza; hachas; espadas,
  y daga si el id lo dice). Cada tipo: cadena de ligeros, golpe fuerte (cargado), ataque corriendo y golpe del salto;
  cada movimiento (`Mov`) con duracion, ticks de dano, multiplicador, empujon, alcance, arco, estamina, desequilibrio y
  retroceso. Herramientas, bloques y lo demas: el clic de siempre.
- **Servidor** (`combate/Combate`): `Net.Ataque` (accion, paso, mano, carga) → comprueba (vivo, supervivencia, no
  bebiendo, arma), cobra la estamina (`Rol.gastarCombate`) y en los ticks de dano busca a quien alcanza (arco o linea
  recta, a la vista) y le pega con todo lo del juego (encantamientos, desgaste, `hurtEnemy` —asi las armas del Rey y el
  Mandoble siguen cargando energia—). Carga: hasta 24 ticks, +45 % al maximo. Desequilibrio: a un enemigo normal
  (no jefes) se le acumula y queda aturdido 1,5 s. `Net.Rodar` → instantes invulnerables (9/8/7 ticks segun el peso,
  paso atras 5) en los que no llega nada que pegue o se dispare. Los demas jugadores ven los golpes por
  `Net.GolpeVisto`; el que pega recibe `Net.Impacto` (temblor y un instante de parada) o `Net.GolpeNo` (se corta).
- **Peso del equipo** (`combate/Peso`): armadura por material y pieza + las armas de las dos manos; maximo 45 (+1,8 por
  punto de Aguante); ligero <= 30 %, medio <= 70 %, pesado <= 100 %, sobrecargado (no rueda y anda un 35 % mas lento).
  Se ve en el menu (Equipo y Estado).
- **Cliente** (`client/combate/`): `ClientCombate` lleva el ritmo en el START de cada tick (consume los clics de atacar
  antes que el juego): tocar = ligero (encadena si se pulsa antes de que acabe el anterior o 8 ticks despues; lo pedido
  antes de tiempo se guarda 10 ticks), mantener 5 ticks = carga, soltar = golpe fuerte; corriendo = ataque corriendo; en
  el aire = golpe del salto (cae con fuerza, golpe al tocar suelo). Antes de cada golpe pregunta con el mismo evento del
  clic del juego (`ForgeHooksClient.onClickInput`): asi los combos de Shift/Ctrl de las armas del Rey (Ctrl solo si no se
  va corriendo), las cinematicas, beber o estar agotado siguen mandando. Frena al jugador mientras pega, le da el
  empujon del golpe y lo mueve en la voltereta (el movimiento lo lleva el cliente). Tecla **Alt izquierdo** = rodar (con
  direccion) o paso atras (sin ella). En primera persona dibuja el arma en `RenderHandEvent` con dos poses por golpe
  (preparar y acabar; `Poses.fp`). `Poses`: en tercera persona, pose de brazos propia (`ArmPose` puesta en
  `RenderPlayerEvent.Pre`) con giro del cuerpo, espejo para la mano izquierda, dos manos para los tipos a dos manos; la
  voltereta gira el cuerpo entero (push en Pre a prioridad LOWEST, pop en Post). Los punetazos son el golpe del juego.
- **Fijar el objetivo** (`client/combate/Fijar`): Q (va con su propio contexto de tecla: no choca con "soltar objeto")
  o pulsar la rueda. Elige al de delante (cono de 65 grados, a la vista, enemigos antes que animales; nunca jugadores
  ni aldeanos); la camara le sigue cada fotograma (`RenderTickEvent` START); mover el raton de golpe salta al siguiente
  por ese lado; se suelta solo si muere, se aleja de 32 o no se ve en 2 s. La marca (`menu.png` `fijar` / `fijar_luz`)
  se dibuja en el HUD donde cae su pecho (proyectado en `RenderLevelStageEvent` AFTER_PARTICLES: con shaders tambien).
- Sonidos (ElevenLabs): `golpe_ligero` (4), `golpe_pesado` (2), `carga_golpe` (2), `impacto_carne` (3),
  `impacto_armadura` (3), `rodar` (2), `fijar` (1), `estocada` (3), `golpe_salto` (2), `aturdido` (2), `esfuerzo` (4).
- La prueba del cliente tiene el tramo `combate()` (capturas `cliente_c01..c20`); `LIMIT` 23 min y `timeout 37m`.

### Plan grande, parte 2d-1: talismanes, frutos, resistencias y armas que escalan

- **Escalado** (`combate/Escalado`, lo usan servidor y cliente): cada arma tiene letras por Fuerza, Destreza, Inteligencia,
  Fe y Arcano (S 1,30 · A 1,00 · B 0,70 · C 0,45 · D 0,25 · E 0,10 del ataque base, por `curva(atributo)`: 0,08 a 10,
  0,35 a 20, 0,70 a 40, 0,85 a 60, 1 a 99) y requisitos (sin ellos pega un 40 % menos). Por tipo (espada D/C, daga E/B,
  mandoble B/E...) y las con nombre aparte (Espada del Rey C/C/Fe D, Mandoble B/Arc D). `Combate.pegar` usa
  `Escalado.total(arma, base(arma), atributos)` + lo demas del atributo de ataque (pociones). El viejo +1,2 %/Fuerza
  para todas (`MOD_ATAQUE`) ya no se pone (se quita a quien lo tuviera). El menu ensena "Ataque 6,0 + 0,3", las letras y
  los requisitos (en rojo los que faltan); subir de nivel ensena lo que pega el arma antes → despues.
- **Talismanes** (`rol/Talisman`, 12: vigor +10 % vida, aliento +25 % recuperar estamina, mente +15 % mana, porteador
  +15 % peso, brasa / escarcha / trueno / sombra -40 % de su dano, hueso -10 % fisico, codicia +20 % ecos, garra +15 %
  golpe del salto, furia +12 % golpe fuerte): van en la FICHA (`rol/Talismanes`, NBT `talismanes`, cache por jugador),
  no en el inventario; 1 hueco + 1 por gran jefe vencido (logros `rey` y `gavran`; maximo 4); dos iguales no. Se ponen en
  el menu (Equipo, fila de la armadura: `Net.EquipoAccion` TALISMAN_PONER / TALISMAN_QUITAR; los huecos cerrados con
  candado) o desde el inventario (clase nueva Talismanes). El cliente los sabe por `Net.FichaRol` (huecos y nombres).
  **Ojo: `Talismanes.llevados(jugador)` solo en el servidor** (la cache es estatica: en un mundo de un jugador el cliente
  y el servidor comparten la JVM); el cliente usa `ClientRol.talismanes` / `ClientRol.lleva`.
- **Frutos** (`rol/Fruto`, 8: brasa fuego, escarcha hielo + la nieve no congela, tormenta rayo, lunar magia: -50 %;
  ceniza cura veneno y marchitamiento y el veneno no entra; hueso -15 % fisico; sombra -50 % marchitamiento; pluma cae
  despacio y sin dano): se comen en 18 ticks (no son comida) y dan su efecto (`rol/Efectos`, `darkcraft:fruto_*`) 1-2 min.
- **Resistencias** (`rol/Resistencias`: fisico, fuego, hielo, rayo, magia, veneno, sombra; segun el tipo de dano): de
  talismanes y frutos, multiplicadas, tope 90 %; se ven en el menu (Estado).
- **HUD**: los efectos activos (frutos, pociones) en fila bajo las barras, con una rayita de lo que les queda y
  parpadeando los ultimos 10 s (`Hud.efectos`); los iconos del juego arriba a la derecha ya no salen.
- Donde salen: cofres de Valdrenn (tablas `chests/*`: frutos en casi todos, talismanes raros, mas en tesoro y cripta);
  el cofre del Rey da el Talisman de la Brasa y 3 frutos de brasa; el de Gavran, el del Escudo de Hueso y 3 de ceniza.
- Arte: `tools/rol/objetos.py` (frutos, talismanes y los iconos de los efectos). Sonidos: `comer_fruto` (tomas 1 y 2),
  `talisman` (2 y 3). Comandos `/darkcraft rol talisman [<nombre> [poner <hueco>] | quitar]`, `fruto <nombre> [<n>]`,
  `resistencias`. La prueba del cliente tiene el tramo `talismanes()` (capturas `cliente_t01..t07`).

### Plan grande, parte 2d-2: hechizos (baston y sello)

- **Catalizadores** (`hechizos/Catalizador`): el **Baston de la Sombra** (`darkcraft:baston`, hechicerias, escala A con
  la Inteligencia) y el **Sello Sagrado** (`darkcraft:sello`; la constante es `SELLO_SAGRADO` porque `SELLO` ya es un
  bloque; milagros, A con la Fe). Con uno en la mano derecha (barra 0-2) el clic lanza el hechizo elegido.
- **Hechizos** (`hechizos/Hechizo`, objetos `darkcraft:hechizo_<id>`, pergaminos azules / plegarias con lacre rojo):
  `guijarro` (8 mana, 1 hueco, Int 10), `lanza` (24, 2, Int 20, atraviesa), `esquirlas` (16, 1, Int 14, cinco que
  buscan al enemigo), `espada` (12, 1, Int 12, tajo en arco delante), `llama` (12, 1, Fe 10, llamarada que quema),
  `rayo` (22, 2, Fe 18), `curar` (30, 1, Fe 12, cura 6 + 30 % al que lanza y a los jugadores a 6 bloques),
  `bendicion` (25, 1, Fe 14, efecto `darkcraft:bendicion` 30 s: +15 % de dano con las armas). Cada uno: duracion,
  tick en que sale, dano base; el dano escala `dano x (1 + 1,00 x curva(atributo))`.
- **Memoria** (`rol/Memoria`, NBT `memoria` de la ficha): solo se lanzan los memorizados; huecos = 2 + uno a Mente 14,
  20, 28, 38 y 50 (`Hechizo.memoria`). Se cambian en el santuario (opcion nueva "Memorizar hechizos" →
  `client/ui/MemoriaScreen`, lista en el orden de los hechizos; `Net.Memorizar`, el servidor exige haber descansado en
  ese santuario —`Santuarios.junto`— o creativo, llevar los hechizos y que quepan). El cliente los sabe por
  `Net.FichaRol` (`ClientRol.memoria`); la **flecha arriba** (`Teclas.HECHIZO_SIGUIENTE`) elige el siguiente y la cruz
  lo ensena arriba (con puntitos).
- **Lanzar**: el cliente (`ClientCombate.lanzar`) comprueba y avisa (ninguno memorizado, otro catalizador, requisito,
  mana) y manda `Net.Lanzar`; el servidor (`hechizos/Lanzar`) lo vuelve a comprobar, cobra mana (`Combos`) y 6 de
  estamina, y en el tick `sale` hace el efecto: proyectiles `hechizos/ProyectilHechizo` (sin gravedad, `NoopRenderer`:
  solo particulas en el cliente; dano magico o de rayo) o arcos delante (espada, llamarada). Poses `LANZAR` y `REZAR`
  (primera y tercera persona, `Poses`); los demas lo ven por `Net.GolpeVisto`.
- Donde salen: tablas de cofres (biblioteca: hechicerias y curar; cripta: milagros; alquimia y tesoro: todos;
  armeria y tesoro: los catalizadores). Comandos `/darkcraft rol hechizo [<nombre> | memorizar | olvidar | baston |
  sello]` (`memorizar` = todos los que se lleven, sin mirar huecos).
- Arte: `tools/rol/hechizos.py` (baston, sello, los 8 pergaminos y el icono del efecto). Sonidos (ElevenLabs):
  `hechizo_lanzar` (tomas 4 y 1), `hechizo_impacto` (2 y 1), `hechizo_rayo` (1 y 4), `hechizo_llama` (4 y 1),
  `hechizo_curar` (3 y 2), `hechizo_bendicion` (4 y 3). La prueba del cliente tiene el tramo `hechizos()` (capturas
  `cliente_hz01..hz15`; las `cliente_mNN` son las del Mandoble).
- El mana pasa de 127 con la Mente alta: `Net.Energia` va en VarInt (antes un byte: con 208 el cliente veia -48).

### Plan grande, parte 2e-1: dialogos y Maelis, la guia de los santuarios

- **Maelis** (`personajes/Guia`, entidad `darkcraft:guia`, `MobCategory.MISC`, 0,8 x 1,3): sentada en el suelo junto a
  los santuarios importantes (de momento el de la Fuente Mayor de cada Valdrenn: `Santuarios.valdrenn` la sienta una vez
  a 2-3 bloques, de espaldas al fuego; `puestos` `reino:guia`), con un farol de almas en la mano. No se mueve, no recibe
  dano (salvo lo que salta la invulnerabilidad, como /kill), no se empuja, mira a quien tenga cerca (solo gira la
  cabeza: `BodyRotationControl` quieto), guarda su santuario. Modelo: `PlayerModel` de brazos finos con su piel
  (`tools/personajes/guia.py` → `textures/entity/guia.png`; `tools/personajes/piel.py` = donde cae cada cara en una
  piel de 64 x 64 y una vista de frente y espalda) en la postura de ir montado, las manos en el regazo y 0,75 mas abajo
  (`client/personajes/GuiaRenderer`). Su retrato de 64 x 64: `textures/gui/retratos/guia.png` (mismo script).
- **Dialogos** (`dialogo/Dialogos`, igual en servidor y cliente): cada personaje, nodos de frase (voz
  `darkcraft:voz.<pj>.<n>` → `sounds/<pj>/voz_N.ogg`, subtitulo `darkcraft.dialogo.<pj>.<n>`, siguiente y una accion al
  acabar) o un menu (nodo 100) de respuestas. `dialogo/Conversaciones` (servidor): de cerca (4,5) y sin estar en combate,
  decide por donde empieza segun lo ya hablado (marcas en la ficha, `Ficha.dialogo`; Maelis: la primera vez 1-2-3, luego
  8, y una vez 9 tras el Rey y 10 tras Gavran), da el logro `maelis` y manda `Net.Dialogo`; al acabar la frase 6
  (`Accion.NIVEL`) el cliente manda `Net.DialogoAccion` y el servidor deja subir de nivel en el santuario de la guia
  (`Santuarios.permitir`) y abre `NivelScreen` (`Net.AbrirNivel`).
- **La pantalla** (`client/ui/DialogoScreen`): la caja de piedra abajo, el retrato en su hueco, el nombre en gotica y su
  titulo, la frase que se escribe al ritmo de la voz (`voces.json`, que escribe `procesar.py`: lo que tarda en decir cada
  frase) y [E] Seguir latiendo; menus con flechas, raton y las preguntas ya hechas mas apagadas. Voz sin posicion
  (`SoundSource.VOICE`), se para al pasar de frase o cerrar. Se habla con la tecla del aviso (la E: "Pulsa [E] para
  hablar", `client/personajes/ClientPersonajes` mira si se la tiene delante, cono de 26 grados) o con clic derecho.
- Voz (ElevenLabs, `eleven_v4`): **"Autumn Veil"** `KoVIHoyLDrQyd4pGalbs` (mujer serena), flow `FNZWSYJq2nK8zd5QwV5m`;
  10 frases (tomas a y b, se usa la a: `TOMAS_GUIA`); en `procesar.py` `VOZ_GUIA` (cerca, un poco de aire de piedra).
- Comandos `/darkcraft rol guia [olvidar]`. La prueba del cliente tiene el tramo `dialogos()` (capturas `cliente_d01..d10`).

### Plan grande, parte 2e-2: Dorvan el herrero, Kerrick el mercader, la forja y la tienda

- **Personajes** (`personajes/PersonajeBase`: lo comun —quietos, sin dano ni empujones, no se van, miran a quien tengan
  cerca girando solo la cabeza, hablan con clic derecho o la E—; `Guia`, `Herrero` y `Mercader` lo heredan y dicen su
  dialogo con `dialogo()`). Entidades `darkcraft:herrero` y `darkcraft:mercader` (`MobCategory.MISC`, 0,7 x 1,95).
- **Dorvan** (`personajes/Herrero`): de pie ante su yunque, martilleando sin parar (todo en el cliente: `ciclo` de 30
  ticks, el golpe en el 20 con chispas —llamitas y lava— y `sfx.herrero_golpe`); si hay un jugador a menos de 4,6 deja el
  martillo abajo y le mira (`atencion`, suavizado). Modelo: `PlayerModel` de brazos anchos con su piel
  (`tools/personajes/herrero.py`: calvo, barba trenzada con anilla de hierro, delantal chamuscado, guante en la
  izquierda) y el `martillo_herrero` en la mano (`client/personajes/HerreroRenderer`, pose del martillazo en
  `martillo(f)`). En cada Valdrenn, una vez, `Santuarios.herreria` le monta la herreria en el patio de armas
  (`HERRERIA` = 112, 161 del centro, el lado este del campo): yunque delante (al oeste), alto horno encendido y hoguera a
  su izquierda, barril, piedra de afilar y farol (`puestos` `reino:herrero`).
- **Kerrick** (`personajes/Mercader`): de pie con los pulgares en las correas, al otro lado del santuario de la fuente
  (`Santuarios.cerca`, lejos de la guia), con dos barriles y un farol (`reino:mercader`). Modelo de brazos finos con
  **piezas de mas** (`client/personajes/MercaderRenderer.crear`, capa `darkcraft:mercader`, textura de 128 x 64): sombrero
  de ala ancha (copa y ala, en la cabeza), mochila enorme, manta enrollada, olla y farol (en el cuerpo). La textura y el
  retrato salen de `tools/personajes/mercader.py` (`piel.py` ya es la clase `Piel` con cajas de mas y pieles de 128 x 64).
- **Dialogos** (`Dialogos.HERRERO` / `MERCADER`): la primera vez se presentan (1) y luego saludan (2) (`Ficha.dialogo`,
  bit 1); las respuestas pueden llevar accion (`Respuesta.accion`: `FORJA`, `TIENDA`): el cliente la pide
  (`Net.DialogoAccion`) y espera a que el servidor abra la pantalla (`Net.AbrirForja` / `AbrirTienda`). Logros `dorvan`,
  `kerrick`, `mejora`, `mejora_maxima` (desafio) y `compra`; todos con su entrada del diario (y la de `maelis`, que faltaba).
- **La forja** (`forja/Forja`, lo usan servidor y cliente): armas del combate (no los punos) y catalizadores, de +0 a +9
  (NBT `darkcraft_mejora`); cada nivel +8 % (`Escalado.total` multiplica base y escalado; `Lanzar` el dano y lo que cura
  el hechizo). Pide piedras de forja (`forja/PiedraForja`, `darkcraft:piedra_forja_1..3`): +1/+2/+3 una, dos y tres de
  grado I; +4..+6 de grado II; +7..+9 de grado III; y ecos (`ecos(d)` = 150 + 100 d + 25 d²: 12 925 de +0 a +9).
  `Net.Mejorar` → `Forja.mejorar` (cerca de Dorvan, lo comprueba todo, cobra con `Rol.pagar`, chispas y
  `sfx.forja_mejora` en el yunque) → `Net.ForjaHecho` (HECHO, FALTA, TOPE, NO). El nombre sale con "+3" en el menu, la cruz
  y la forja (`Forja.nombre`); la ficha del menu ensena el ataque ya mejorado y "Mejora +3 de +9".
- **La tienda** (`tienda/Tienda.KERRICK`, igual en los dos lados): 16 cosas con precio y algunas con limite por jugador
  (lo comprado en la ficha, `Ficha.comprado`): piedras I (220, sin limite) y II (760, 6), frutos, arco y flechas, espada y
  hacha de hierro, baston, sello, tres hechizos y el talisman del porteador. `Net.Comprar` → `Tienda.comprar` (lo da con
  `Equipo.dar`: a la bolsa, nunca a las manos; con su aviso de "conseguido") → `Net.TiendaHecho`.
- **Pantallas** (`client/ui/ForjaScreen`, `TiendaScreen`): losa con la lista a la izquierda (icono, nombre, "+n" o precio),
  el detalle a la derecha (lo de ahora → lo de despues; piedras y ecos en rojo si faltan; cantidad con flechas y total),
  botones y teclas; abajo lo que dice el personaje (`client/ui/Bocadillo`: retrato pequeno, la frase al ritmo de su voz,
  sin repetirla seguida). Dorvan: al abrir la 3, al mejorar la 4, si falta algo la 5, al maximo la 8. Kerrick: al comprar
  la 3, sin ecos la 4.
- **Los yunques** (`personajes/Yunques`): ya no abren la pantalla de Minecraft (salvo en creativo): junto a un herrero es
  hablar con el; sin herrero, aviso.
- Donde salen las piedras: cofres (armeria I y II, tesoro I-III, cripta I y II, comun y bodega I, raras), los caballeros
  zombi (7 % una piedra I), el cofre del Rey (3 de grado II) y el de Gavran (3 de grado III), y Kerrick.
- Voces (ElevenLabs, `eleven_v4`): Dorvan **"Fjalar - Gruff Scottish Dwarf Warrior"** `TLS15X9bCEDl1vGoUK57` (8 frases,
  toma a salvo la 5b; `VOZ_HERRERO`: graves y eco corto de herreria); Kerrick **"Cornelius - Wise Sage"**
  `6sFKzaJr574YWVu4UuJF` (6 frases, tomas a; `VOZ_MERCADER`: casi seca). `procesar.py` monta todos los personajes con
  `PERSONAJES` (id → tomas y filtro) y escribe `voces.json`. Efectos: `herrero_golpe` (tomas 4, 1 y 3), `forja_mejora`
  (1 y 4), `tienda_compra` (2 y 4).
- Arte: `tools/personajes/herrero.py`, `mercader.py` (pieles y retratos) y `tools/rol/forja.py` (piedras y martillo).
- Comandos `/darkcraft rol herrero [olvidar] | mercader [olvidar] | mejora <0-9> | piedras` (el herrero sale con su yunque
  entre los dos). La prueba del cliente tiene el tramo `forja()` (capturas `cliente_fg01..fg19`).

### Plan grande, parte 2e-3: cenizas de guerra (las habilidades de las armas)

- **Habilidades** (`combate/Habilidad`, igual en servidor y cliente; tecla **C**, `Teclas.HABILIDAD`, sin chocar con
  "guardar la barra" del creativo): cada una con su movimiento del combate (`Armas.Mov`, accion `Armas.HABILIDAD` = 8), su
  mana (de `Combos`) y su estamina. `torbellino` (14, vuelta entera: en tercera persona gira el cuerpo entero —
  `Poses.girar`—; espada, hacha, alabarda, mandoble), `estocada` (14, se lanza hacia delante y atraviesa en linea; lanza,
  espada, daga, alabarda), `grito` (18, efecto `darkcraft:furia` 20 s: +20 % de dano; mandoble, hacha, alabarda), `sismo`
  (20, salta y la onda al caer, radio 3,4, levanta a los que pilla; hacha, mandoble, alabarda), `paso` (6, paso rapido de
  sombra hacia donde se ande, 8 ticks invulnerable; daga, espada), `llamas` (16, quema 5 s), `escarcha` (16, frena y
  congela), `relampago` (20, en linea, +35 % de dano de rayo) —estas tres valen para todas—, `sangre` (no gasta mana: el 8 %
  de la vida maxima; espada, daga, hacha, mandoble) y `parada` (0; daga, espada: lo que llegue de delante en los ticks 2-7 se
  desvia —`Combate.onAtaque`—, el que pego se queda aturdido —los jefes no— y da el logro `parada`).
- **La de cada arma** (`Habilidad.porDefecto`): espada y alabarda el torbellino, daga el paso, mandoble el grito, hacha el
  sismo, lanza (tridente) la estocada. Las armas con nombre (Espadas del Rey, Mandoble del Juramento) no tienen: llevan sus
  combos de Shift/Ctrl. Una ceniza puesta va en el NBT del arma (`darkcraft_ceniza`).
- **Servidor** (`Combate.habilidad` / `tickHabilidad`): lo comprueba todo (arma, mana, vida de la sangrienta, estamina),
  cobra y en los ticks de golpe pega con `pegar` (+ `efectoGolpe`: fuego, hielo, rayo, sangre); la estela de cada una
  (`estela`, particulas para todos); el sismo cuenta el golpe al caer; el grito pone la furia en el tick 10.
  **Cliente** (`ClientCombate.habilidad` / `avanzarHabilidad`): avisos (sin habilidad, sin mana, sin vida), el salto del
  sismo, el paso (direccion de las teclas, adelante si no se anda), el empujon de la estocada y sus sonidos. Poses nuevas
  `GIRO`, `GRITO`, `PARADA`, `PASO` (primera y tercera persona); los demas lo ven por `Net.GolpeVisto`.
- **Cenizas** (`forja/CenizaGuerra`, objetos `darkcraft:ceniza_<id>`, raros, de una en una): se ponen en la forja de
  Dorvan, pestana nueva **Cenizas** (`ForjaScreen`: pestanas Mejorar / Cenizas con TAB o clic; el arma a la izquierda, las
  cenizas que se llevan y "quitar la suya" abajo a la derecha, con lo que hace y lo que gasta): `Net.Ceniza` →
  `Forja.ceniza` (codigos `CENIZA` y `QUITADA`; la ceniza que tenia el arma vuelve a la bolsa; logro `ceniza`; llamas de
  almas y `sfx.ceniza_aplicar` en el yunque). La ficha del arma en el menu dice su habilidad y lo que gasta.
- Donde salen: cofres (armeria: torbellino, estocada, parada, sismo, grito; tesoro: llamas, escarcha, relampago, sangre,
  paso; cripta: sangre, escarcha, paso), Kerrick (parada 2200, llamas y escarcha 2800, una de cada), el cofre del Rey
  (llamas) y el de Gavran (sangre).
- Efectos de ElevenLabs (tomas en `TOMAS_SFX`): `habilidad_torbellino` (1 y 4), `habilidad_estocada` (1 y 3),
  `habilidad_grito` (4 y 2), `habilidad_sismo` (1 y 4), `habilidad_paso` (3 y 4), `habilidad_llamas` (4 y 2),
  `habilidad_escarcha` (1 y 2), `habilidad_relampago` (3 y 1), `habilidad_sangre` (1 y 4), `habilidad_parada` (2 y 3),
  `ceniza_aplicar` (1 y 4).
- Arte: `tools/rol/cenizas.py` (las diez cenizas: una brasa de su color sobre un monton de ceniza, y el icono de la furia).
- Comandos `/darkcraft rol ceniza <id> [poner] | quitar` (sin `poner` la da; con `poner` se la pone al arma de la mano
  derecha). La prueba del cliente tiene el tramo `habilidades()` (capturas `cliente_cg01..cg18`). `Net.V` = "14" (desde la 2f-3, "15").

### Plan grande, parte 2e-4: el codice, y la historia y los logros en el menu (R)

- **El codice** (`client/ui/CodiceScreen`): los libros a pantalla completa en pixel art. La tapa de cuero negro con
  filete de oro (nueve trozos de `textures/gui/codice.png` + `codice_cuero.png` repetido), dos paginas de pergamino oscuro
  (`codice_pergamino.png`, 256 x 256 que se repite sin costuras; bordes quemados, canto de las hojas, sombra y costura del
  lomo, filigrana en las esquinas de fuera, numero de pagina) y la cinta roja por el lomo. La primera pagina de cada libro:
  su **miniatura** (`textures/gui/miniaturas.png`, diez de 128 x 80 con marco de oro: los seis capitulos —la corona rota,
  la fuente, el trono, la catedral, el juramento, la puerta sobre las nubes—, libros perdidos de Valdrenn y de la
  fortaleza, el diario y el acertijo), la cabecera en gotica, el titulo en cursiva, el autor y un floron; el texto con la
  letra del juego en tinta, la **letra capital** iluminada (marco rojo en la cronica, negro y rojo en la Sombra —con tinta
  roja oscura—, azul en los libros perdidos, verde en el diario) y un calderon rojo en cada parrafo; los saltos de linea
  se respetan (versos). Al final, una hoja de hiedra roja. **Pasar pagina**: la hoja gira sobre el lomo (se estrecha la
  pagina de delante y luego crece la de detras, oscurecida al levantarse, con su sombra; 380 ms) con
  `sfx.codice_pagina`; flechas, A/D, espacio, la E, la rueda o clic en cada pagina; en el ultimo pliego, adelante cierra.
  Al abrir `sfx.codice_abre` y al cerrar `sfx.codice_cierra` (ElevenLabs: tomas 2 y 1, 1 y 3, 4 y 1).
- **Libros** (`CodiceScreen.Libro`): `capitulo(n)`, `perdido(n)` y `diario(entradas)` se montan con las claves de idioma
  (cada jugador en su idioma); `acertijo()` (sus versos y el sello: al pulsarlo sale la pagina de la receta, la forja
  dibujada; sustituye a la `AcertijoScreen` vieja); `escrito(libro)` para cualquier libro escrito (los del mod por sus
  marcas; los demas con lo que llevan en el NBT).
- **Nunca sale la pantalla de libro ni la del atril de Minecraft** (`Pantallas`, `ScreenEvent.Opening`): el libro de la
  mano se abre en el codice; el del atril tambien (el atril sigue siendo un contenedor del servidor —asi se apuntan los
  libros perdidos—: el libro llega un poco despues y al cerrar se cierra el contenedor).
- **El menu (R) tiene seis pestanas**: Equipo, Inventario, Estado, **Historia** (la cronica con los capitulos leidos —los
  demas con candado—, los libros perdidos encontrados y el diario; a la derecha la miniatura, el titulo y como empieza;
  elegir lo abre en el codice y al cerrarlo se vuelve al menu), **Logros** (apartados Logros / Coleccion / Bestiario con
  las flechas izquierda y derecha: lista a la izquierda y la ficha a la derecha, con la criatura girando en el bestiario;
  `client/ui/Coleccion`) y Sistema. Si no caben las seis con la letra gotica, se encogen igual. **La M ya solo abre el
  mapa** (`LibroScreen` sin cintas ni historia ni logros).
- El Espejo manda su estado aunque aun no haya despertado (con `activo` falso): el menu siempre tiene la historia, el
  diario y los logros (`EspejoCliente.estado()`).
- Arte: `tools/ui/codice.py` (pergamino, cuero, tapa, capitales, floron, hiedra, cinta, marco, esquinas, lacre;
  escribe `CodiceTex.java`) y `tools/ui/miniaturas.py` (las diez miniaturas, dibujadas con poligonos, degradados con
  tramado y contorno de tinta).
- La prueba del cliente tiene el tramo `codice()` (capturas `cliente_cx01..cx08`: portada, la hoja girando, segundo
  pliego, final, el capitulo 6, el atril y el diario); la historia y los logros del menu salen en `cliente_e10..e12`,
  `e18..e20` y `f41..f43`, y el acertijo en `f15_acertijo_*` y `f16_forja`.

### Plan grande, parte 2f-1: la pausa, las opciones, los mods y el aviso de los logros

- **Pantallas de Minecraft que ya no salen** (`client/ui/Pantallas`, `ScreenEvent.Opening`; sustituye a `ClientCodice`):
  el libro y el atril (el codice), la pausa (`PausaScreen`; la de F3 + ESC, sin menu, se deja: se mira la tecla F3 y el
  unico booleano de `PauseScreen`, buscado por su tipo), las opciones de Minecraft (`OpcionesScreen`, con la pantalla a
  la que volvian: su unico campo de tipo pantalla), la lista de mods de Forge (`ModsScreen`) y los progresos de la L
  (la pestana Logros del menu). **Campos privados de
  Minecraft: se buscan por su tipo, nunca por el nombre** (fuera del entorno de desarrollo los nombres son otros).
- **La pausa** (`PausaScreen`): losa con "Pausa" en gotica, la partida, el nivel y los ecos, y botones de piedra y oro:
  volver, menu (R), opciones, mods, abrir a la red local (solo en un mundo de un jugador sin abrir) y guardar y salir
  (desconectar en un servidor: lo mismo que hace la pausa de Minecraft). Teclado (flechas, Intro, la E) y raton.
- **Las opciones** (`OpcionesScreen`): los apartados a la izquierda y las opciones del elegido a la derecha, de dos en
  dos, con barra y rueda (filas enteras: las que no caben se esconden, nada de recortes). Los botones y deslizadores son
  los de Minecraft (`OptionInstance.createButton`) con el texto en oro. General (dificultad en un mundo propio, campo de
  vision, brillo...), Sonido (cada volumen, dispositivo, subtitulos), Controles (raton, saltar, agacharse; boton a las
  teclas de Minecraft), Chat y Accesibilidad aqui mismo; Graficos (o la de Embeddium si esta), Idioma, Recursos, Aspecto
  (la piel), Shaders (Oculus) y Lejania (Distant Horizons) —estas dos solo si el mod esta— y Mods abren su pantalla.
- **Los mods** (`ModsScreen`): Dark Craft el primero; ficha con nombre, version, autores y descripcion; "Configurar"
  (`ConfigScreenHandler`) y la carpeta de los mods.
- **Las pantallas de Minecraft que quedan** (graficos, teclas, idioma, recursos, la piel, crear mundo, las de otros
  mods...) salen con los botones, deslizadores, casillas, fondos, separadores y avisos del mod: `tools/ui/vanilla.py`
  reescribe `assets/minecraft/textures/gui/` (widgets.png —solo las filas de los botones—, slider.png, checkbox.png,
  options_background.png, light_dirt_background.png, toasts.png —las cuatro bandas—, header/footer_separator.png)
  partiendo de las de verdad del juego (`client-extra.jar`).
- **El aviso de los logros** (`AvisoLogro`): el de Minecraft (`AdvancementToast`, por `ToastAddEvent`) se cambia por una
  losa con el icono, "Logro" (o "Hazana" en los de desafio, con la fanfarria de Minecraft) en gotica y el nombre; en el
  juego arriba en el centro bajo la brujula, con una pantalla abierta arriba a la derecha; de uno en uno; en las
  cinematicas esperan. Sonido `sfx.logro` (ElevenLabs, tomas 2 y 4: campana grave con brillo).
- La prueba del cliente tiene el tramo `pausa()` (capturas `cliente_pa01..pa16`: pausa, opciones por apartados, las
  pantallas de Minecraft con el estilo del mod, mods, el aviso de un logro, una hazana sobre el menu y un aviso del
  sistema). El codice pasa paginas en un diario largo (`cliente_cx02..cx05`).

### Plan grande, parte 2f-2: la pantalla de titulo y las partidas

- **El titulo** (`client/ui/TituloScreen`, sustituye a `TitleScreen` desde `Pantallas`): Valdrenn sobre su isla flotante
  en una noche de tormenta, en capas de 480 x 270 que se estiran a toda la pantalla (`tools/ui/titulo.py` →
  `textures/gui/titulo/`: `cielo.png` con la luna y las nubes altas, `lejos.png` con el castillo —la aguja de la Torre de
  Ascenso delante de la luna, la torre-arbol de Ser Gavran con su copa, los rios que caen al vacio— e islas pequenas,
  `nubes_lejos.png` / `nubes_cerca.png` de 512 de ancho que se repiten y se mueven a distinta velocidad, `rayo.png` y
  `logo.png`). Rayos cada 7-17 s con destello detras del castillo y trueno; brasas que suben; un velo oscuro a la
  izquierda para leer el menu; sale de negro al abrir. El logo "Dark Craft" (New Rocker en oro con filo rojo y tinta) a
  escala entera. Menu en gotica a la izquierda: continuar (la ultima partida, con su linea de nivel y zona), partidas,
  nueva partida (de momento la de Minecraft, con el estilo del mod), multijugador, opciones, mods y salir; teclado y raton.
- **Musica del titulo** (`ClientMusic.tickTitulo`): "Ashbringer (Epic Main Menu Theme)" de William Hector (CC-BY 4.0,
  `ci/musica/pistas.json` id `titulo`), en bucle mientras no hay mundo y hay una pantalla de menu delante (no las de
  cargar o conectar); para la musica del juego.
- **Las partidas** (`PartidasScreen`, sustituye a la lista de mundos): cada mundo con su dibujo (`FaviconTexture`), su
  nombre, "Nivel 12 · Torre de Ascenso", las horas jugadas y la fecha; jugar (doble clic o Intro), nueva, borrar (con
  pregunta; `LevelStorageAccess.deleteLevel`) y volver. Lo del mod sale de `darkcraft_partida.json` en la carpeta del
  mundo (`rol/Partida`: nivel, ecos, zona —`Rol.de(p).zona`, clave de idioma—, clase —desde la 2f-3—, dia, horas y
  version), que se escribe al guardar el mundo y al salir, solo en los mundos de un jugador (lo de su dueno).
  `client/ui/Guardadas` las carga (`LevelStorageSource.loadLevelSummaries`) con lo del fichero.
- La prueba del cliente acaba con el tramo `titulo()`: apunta la partida, guarda y sale desde la pausa del mod y captura
  el titulo (`cliente_ti01..ti03`, con un rayo y otra opcion elegida), las partidas (`ti04`) y las opciones fuera del
  mundo (`ti05`). Desde que se sale, los pasos siguen corriendo sin mundo (los de `server(...)` ya no valen).

### Plan grande, parte 2f-3: las diez clases

- **Clases** (`rol/Clase`, como las de Elden Ring; el nivel sale de los atributos: suma - 79): Caballero (9, espada,
  escudo y hierro), Vagabundo (9, espada, arco y cuero), Barbaro (7, hacha, escudo y malla), Guerrero (8, una espada en
  cada mano), Bandido (5, `darkcraft:daga` —nueva, `tools/rol/daga.py`— y arco; arcano 14), Hechicero (6, baston y
  guijarro, esquirlas y espada de la Sombra memorizados; frascos 2 + 2), Profeta (7, sello, curar, llamarada y bendicion,
  tridente; 2 + 2), Confesor (10, espada, escudo, sello, curar y bendicion), Prisionero (9, espada, baston, guijarro y
  espada de la Sombra, el casco de hierro de la jaula) y Desdichado (1, todo a 10, una espada de madera). `Clase.kit()`:
  las tres armas de la derecha (barra 0-2), la izquierda, la armadura, la bolsa y los hechizos; `Clase.elegir` vacia el
  inventario y lo pone todo (atributos, `Memoria.forzar`, frascos, vida y mana llenos, logro `clase` "Marcado por la
  Sombra" con su entrada del diario).
- Se guarda en la ficha (`Ficha.clase`: "" = aun no ha elegido; "-" = `Clase.SIN_CLASE`, quien ya jugaba antes de las
  clases). Al entrar (`Clase.onEntrar`): quien es nuevo de verdad (menos de 1 min jugado, nivel 1, sin ecos, no en
  creativo) recibe `Net.AbrirClase(forzada)`; los demas se quedan sin clase y no se les pregunta. `Net.ElegirClase` →
  `Clase.pedir` (solo si no tenia; con `/darkcraft rol clase elegir` se puede volver a elegir una vez). `Net.V` = "15".
- **La pantalla** (`client/ui/ClaseScreen`, con el fondo del titulo): las diez a la izquierda (nombre en gotica y nivel),
  el personaje en el centro con lo que lleva esa clase (un `RemotePlayer` con su perfil y el equipo puesto, girando) y la
  ficha a la derecha (nombre, nivel, lo que es, los ocho atributos con sus iconos —en verde los de mas de 10, en rojo los
  de menos—, frascos y los objetos con que empieza, con su recuadro al pasar el raton). "Elegir" pregunta antes de
  empezar. Forzada (al entrar por primera vez) no se cierra con ESC.
- `darkcraft_partida.json` ya apunta la clase (la lista de partidas la usara). Comandos `/darkcraft rol clase [<id> |
  elegir | olvidar | ninguna]` (con id la pone ya, sin preguntar). La prueba del cliente: al entrar comprueba que sale la
  pantalla forzada y deja al jugador sin clase (en la CI entra en creativo: no se le pregunta); el tramo `clases()`
  (capturas `cliente_cl01..cl06`: caballero, hechicero, desdichado, la pregunta, el bandido en el juego y su equipo).

### Plan grande, parte 3a: el mundo de nubes

- **Generador** `darkcraft:nubes` (`world/NubesGenerador`, registrado en `world/MundoNubes`): sin suelo; un mar de nubes del
  bloque `darkcraft:nube` (translucido, sin choque ni recuadro al mirarlo, la luz del cielo pasa, sin caras entre nube y
  nube) en bollos de ruido fijo (no depende de la semilla): lo alto entre 235 y 261 (media 248), 14-21 de grueso; por debajo,
  el vacio. La nube lleva la propiedad `tono` (0-3, `NubeBlock.tono`: lo honda que esta bajo lo alto de su columna; cuatro
  texturas, de casi blanca a gris lila) y su modelo va sin sombreado por caras ni oclusion: el volumen lo dan los tonos
  (con el sombreado de los cubos parecia piedra gris a terrazas). Sustituye al preset `minecraft:normal` (`data/minecraft/worldgen/world_preset/normal.json`: solo el
  overworld, sin Nether ni End; vale igual en servidor dedicado, `level-type` por defecto) con el bioma fijo
  `darkcraft:nubes` (sin lluvia ni bichos). El bioma va en `#darkcraft:has_structure/valdrenn` (la estructura de
  Valdrenn y la de la fortaleza) y en `#darkcraft:islas` (el `CastleFeature`, por `add_valdrenn`); **no** en
  `#minecraft:is_overworld` (saldrian minas abandonadas y las fortalezas de los ojos de Ender flotando en el vacio). `getBaseHeight` = 251
  (`NubesGenerador.SUELO`): ahi sale la Torre de Ascenso (ya solo sube 128) y la escalera de hueso de la fortaleza
  (`Puente.base`).
- **Camino de nubes provisional** (`world/CaminoNubes`, lo pone `buildSurface` antes que el castillo; el de verdad, con
  paradas, en la parte 5): de la puerta de la Torre de Ascenso a la escalera de la fortaleza de su reino. Tres de ancho de
  `darkcraft:nube_firme` (se pisa; oro en el mapa) a Y 250 sobre un banco de nube, con la nube despejada por encima y a
  los lados (zanja donde el mar sube mas; el camino asoma un bloque) y un farol de almas sobre un hueso cada 28 bloques a
  un lado y a otro. Sale al sur, rodea la isla a 445 del centro hasta quedar de cara a la fortaleza, llega a la escalera
  por el sur (si viene del norte rodea la isla de la fortaleza) y esquiva los demas reinos (445) y fortalezas (112);
  plazoletas delante de la torre (r 5) y donde se levanta la escalera (r 10). La cuenta rapida (`cerca`, sin planos)
  descarta chunks; la ruta de verdad (`ruta`, con `PlanCache` y `Fortalezas.plan`) se guarda por reino. En el Espejo,
  de pie en el camino: "Camino de nubes".
- **Al crear el mundo** (`MundoNubes.onCrearAparicion`): se aparece en el camino, 24 al sur del centro de la Torre de
  Ascenso del reino mas cercano al origen (fuera de los 16 de la proteccion del punto de aparicion de un servidor
  dedicado, donde quien no es operador no puede abrir cofres), radio de aparicion 0, sin comerciantes, patrullas ni
  fantasmas. De momento: la isla del pueblo y el barco (3b-3d) cambian esto. **El vacio**: 10 bloques por debajo del fondo
  de la nube se muere en el acto (dano de caer del mundo: la espada de luz va al ultimo suelo firme; los jefes vuelven a
  su sitio); creativo y espectador no. Con la camara dentro de una nube, niebla blanca cerrada (`client/ClientNubes`).
  Las nubes de Minecraft van a Y 640 (`ClientNubes.Efectos`, los efectos `darkcraft:nubes` del tipo de dimension del
  overworld): a 192 quedaban escondidas bajo el mar de nubes.
- En el origen del mundo (donde ira la isla del pueblo) el mar de nubes esta **en calma**: a 40 bloques de (0, 136) lo alto
  es 245-247 y se funde con los bollos de siempre hasta los 78 (`NubesGenerador.CALMA_*`): ahi iran el muelle y el barco.
- `Canvas.unir(x, y, z, estado)`: para vallas, muros bajos, barrotes, cristales y escalones de tejado en esquina; los pone
  sueltos y apunta la posicion para que el juego los una a sus vecinos al acabar de generarse el chunk (tambien en los
  bordes entre chunks). Solo al generar el mundo (`WorldGenRegion`).
- `/darkcraft fortaleza escalera` lleva a la plazoleta. Texturas: `tools/mundo/nubes.py`. Prueba del servidor: generador,
  aparicion y muestras del camino (`informe.txt`); prueba del cliente: el escaparate sube a Y 320 (por encima de las
  nubes) y el tramo `nubes()` (capturas `cliente_n01..n05`: la torre desde el camino, la torre y Valdrenn, el camino,
  el camino desde arriba, dentro de una nube; y un muneco que se pierde en el vacio).

### Plan grande, parte 3b: Brumavela, la isla del pueblo abandonado

- **La isla** (`pueblo/gen/`, en el origen del mundo, igual en todos: semilla fija `PuebloPlan.SEMILLA`; la levanta
  `world/PuebloFeature` chunk a chunk al generar, y pinta el bioma `darkcraft:brumavela` —hierba apagada, bruma, ceniza
  blanca en el aire—). `PuebloPlan` (singleton): `Terreno` (meseta a Y 266 que sale de las nubes, colina de la capilla al
  noroeste, penon del faro al noreste con su rampa, la cala del muelle al sur a Y 250, la Cuesta —un tajo que baja de la
  plaza al muelle—, la bajada y el Saliente al oeste; medias losas en las cuestas y donde una calle sube un bloque),
  `Casa` (26: entramado, piedra o madera; ruina 0-2; taberna, herreria, capitania, almacen, granero), `Capilla` (nave,
  campanario con escalera de mano y tres campanas en sus arcos, la cripta bajo la losa negra), `Faro` (7 de ancho,
  escalera de caracol sin saltos: rellanos llanos en las esquinas, galeria y linterna), `Muelle` (frente de sillar,
  espigon, cabria, embarcadero y bote), `Barco` (La Garza Gris, roto o `entero`), `Detalles` (pozo, puestos, farolas,
  carros, cementerio, campos, arboles muertos, rocas, el arco de la Cuesta, el Saliente, y lo de la historia dentro de
  las casas) y `Trozo` (una pieza pequena escrita en el sitio). Los **sitios con nombre** (`plan.sitio("vela")`...: la
  vela, el timon, las campanas, donde se despierta...) los apunta cada pieza al crearse; todo lo demas los usa.
- **Disenar sin abrir el juego**: `tools/vista3d/vista.sh pueblo /tmp/pueblo --vistas "planta;so" --zoom 4` compila el
  mod, arranca solo los registros de Minecraft, levanta el plano en un mundo de mentira (`Volcado.java`), lo dibuja
  (`vista.py`: planta, cortes `x=N` / `z=N` / `y=N`, vistas `so/se/no/ne` o `"azimut,elevacion"`, `--caja` para un trozo)
  y **comprueba que se recorre a pie** (`pueblo/gen/Andar`: andar, saltar un bloque, caer hasta cuatro, escaleras de
  mano; `Recorrido`: de donde se despierta a todos los sitios, y a la cripta solo con la losa apartada). Fuera del juego
  no hay bloques del mod (solo vainilla en los planos; `instanceof NubeBlock` si vale) ni etiquetas de bloque.
- **Se despierta en el muelle** (`MundoNubes.aparicion()`, el sitio `inicio`), en **modo aventura** (al crear el mundo y
  al entrar: quien esta en supervivencia pasa a aventura; creativo sigue). A menos de 17 bloques no hay puertas, cofres
  ni nada de clic (la proteccion del punto de aparicion de un servidor dedicado). El barco deja en `MundoNubes.llegada`
  (el camino de nubes, al pie de la Torre de Ascenso). Las fortalezas y el camino de nubes esquivan el origen.
- **Lo que pasa** (`pueblo/Pueblo`, SavedData `darkcraft_pueblo`, **de todos**: la pieza que encuentra uno cuenta para
  el grupo): cinco piezas (`Pieza`: la vela en el arcon del almacen, el timon tras la barra de la taberna, el mastil en
  la cripta, el ancla en el Saliente con lo que queda de Tobias, el corazon en la linterna del faro —al llevarselo el
  faro se apaga—). No son objetos del inventario: se ven como `item_display` de cara a quien mira (no se guardan) con
  chispas doradas, y salen en un cartel ("La Vela Mayor / Pieza de La Garza Gris · 1 de 5"). **Las tres campanas**: en
  el orden en que murieron los fundadores (Pescador 187, Tejedora 198, Farero 212: lo dicen sus lapidas y la nota de la
  capilla) apartan la losa de la cripta. Con las cinco, en la cubierta: **reparar** (`Pueblo.levantar`: se vuelve a
  levantar `Barco` entero encima —palo, vergas y velas, la rueda del timon (una piedra de afilar), el corazon en la
  bodega—) y desde el timon **zarpar** (de momento un salto a la llegada; el viaje es de la 3d) y volver desde el
  noray de la llegada. **El ancla** del barco reparado no es un bloque (un yunque colgado se cae: los bloques que caen
  no se pueden colgar): es el dibujo de la pieza en un `item_display` quieto, de canto al barco, bajo la serviola
  (`Barco.ancla()`; lo mantiene `Pueblo.onServidor`, como el corazon de la bodega). `Barco.despejar` quita lo del barco
  antes de volver a levantarlo: fuera de la manga del casco (donde esta el espigon) solo de `CUBIERTA + 5` para arriba.
- **El marcador de las piezas** (`Hud.piezas`): en el borde derecho de la pantalla, las cinco una debajo de otra (las que
  faltan, apagadas; la recien encontrada destella), mientras se esta en la isla con el barco roto. `Net.Cosas` lleva
  tambien las piezas encontradas y si el barco esta reparado.
- **El viento**: en el bioma de la isla suena en bucle `ambiente.brumavela` (`ClientMusic.tickBruma`, calla la musica de
  Minecraft; master `brumavela_viento`, toma 3).
- **Las "cosas"** (lo que se usa con la tecla del aviso, "Pulsa [E] para..."): el servidor manda la lista de las que hay
  (`Net.Cosas`, al entrar en la isla y cuando cambia) y el cliente (`client/ui/ClientPueblo`) elige la que se tiene
  delante a menos de 4 bloques y pide usarla (`Net.CosaUsar` → `Pueblo.usar`). El clic derecho en el arcon de la vela,
  las campanas y la rueda del timon hace lo mismo (y no abre nada de Minecraft). `Net.V` = "16".
- **Notas**: son los libros perdidos 12-16 (`Perdidos.TOTAL` = 16; solo en espanol e ingles, los demas idiomas salen en
  ingles), en atriles: el aviso del capitan de puerto (capitania; dice donde esta cada pieza), el cuaderno de la
  tabernera, las tres campanas (capilla), la carta de Tobias y la ultima hoja del diario del Capitan (faro). Miniatura
  nueva en el codice (`tools/ui/miniaturas.py`, la 11).
- Santuario en la plaza del pozo (se pone al llegar alguien). Sitios con nombre (`Pueblo.lugar`: Brumavela, el Muelle, la
  Cuesta, la Plaza del Pozo, La Red Vacia, la Capilla, la Cripta, el Faro, el Saliente, los Campos Secos): tambien sin
  el Espejo despierto. Logros `brumavela`, `pieza`, `campanas`, `piezas`, `barco`, `zarpar` (con su entrada del diario).
  Botin de las casas: `chests/pueblo` y `pueblo_bueno`. En la fortaleza hay ya una mesa de trabajo junto al cofre del
  acertijo (en aventura no se puede poner una).
- Arte: `tools/pueblo/piezas.py` (las cinco piezas, 32 x 32). Sonidos (ElevenLabs): `pieza_barco` (tomas 3 y 2),
  `cripta_abre` (1 y 3), `barco_reparado` (1 y 3).
- Comandos `/darkcraft brumavela ir [<sitio>] | pieza <nombre>|todas | cripta | reparar | zarpar | estado | reiniciar`.
  Prueba del servidor: genera la isla entera, la recorre a pie, mira los bloques de la historia, abre la cripta, repara
  el barco y lo deja todo como estaba (compara el barco bloque a bloque). Prueba del cliente: tramo `pueblo()` (capturas
  `cliente_b01..b18`). **Ojo: la prueba del cliente juega en el mundo que deja la prueba del servidor** (`srv2/world`):
  lo que esta deje mal puesto sale en las capturas (asi se vio que `despejar` dejaba trozos de vela al reiniciar).

### Plan grande, parte 3c: los enemigos de Brumavela (huecos, ahogados y el Capitan)

- **Enemigos propios con forma de persona** (`enemigos/Enemigo`, hereda de `Monster`; nada de zombis con otra piel):
  cada ataque es una **accion** con su animacion, que dura lo que dura y pega en sus **ticks de golpe** a quien pille
  delante, en su alcance y su arco (`Golpe(alcance, arco, dano, empuje)`); mientras se prepara sigue a su presa con el
  cuerpo y 3 ticks antes del golpe ya no gira (apartarse = esquivar). No pegan por tocar. Estado sincronizado: variante,
  accion + serie (para que el cliente arranque la animacion), postura (`DE_PIE`, `REZA`, `TUMBADO`). No ven a quien
  llega por la espalda agachado (`percibe`); el que recibe un golpe avisa a los que tiene a 7 bloques; si su presa se
  aleja de su puesto (`correa`) la dejan y vuelven. Morir es su animacion (`die` / `tickDeath`), no la voltereta.
  `aturdir()` (su accion de aturdido): lo llama `Combate.desequilibrar` (romper la guardia o **parar un golpe**).
- **Los puestos** (`enemigos/Puestos`, SavedData `darkcraft_puestos`): cada sitio apunta su lista (`registrar(clave,
  puestos, aqui)`: tipo, variante, x y z, giro, postura, ronda, grupo). Salen a 72 bloques de un jugador y se recogen a
  104 (no se guardan con el mundo); el que muere no vuelve hasta que alguien **descansa en un santuario o muere**
  (`reaparecer`, lo llaman `Santuarios.descanso` y `Rol.muerte`; no vuelven los que quedarian a 28 de OTRO jugador, ni
  los del grupo `Puestos.JEFE`, nunca). Con amigos, +50 % de vida por jugador de mas (`paraGrupo`); en pacifico, nadie.
  `quedan(sitio[, grupo])`, `enemigo(sitio, n)`, `marcar`, `todos`, `sacarTodos` (pruebas), `alCaer(...)`.
- **Aldeano hueco** (`Hueco`, 24 de vida, 4,5 de golpe): horca (estocada larga, barrido), hoz (tajo y doble tajo,
  rapidos) y antorcha (golpe y barrido que prenden). Esperan de pie, **rezando** o **tumbados** como un muerto mas (se
  levantan si te acercas). **Marinero ahogado** (`Ahogado`, 34 y 6, lento): sable (tajo ancho y tanda de tres
  avanzando) y arpon (lo **lanza** —`Arpon`, una flecha pesada con su dibujante `ArponRenderer`; apunta un instante,
  se esquiva de lado; tambien cuesta abajo— y pincha de cerca; tras lanzar saca otro de la espalda: mientras, el modelo
  no dibuja el arpon de la mano, `AhogadoAnims.ARPON_OTRO`).
- **El Capitan** (`Capitan`, Edrik Maroll; 260 de vida, 10 de golpe; el primer jefe con barra): espera ante la puerta
  del faro; no hay sala cerrada: la pelea empieza cuando ve a alguien en lo alto del penon y, si todos caen o se van
  (110 ticks sin presa), vuelve con la vida entera. Tajo de arriba abajo que sostiene un instante, dos tajos anchos
  avanzando, estocada a fondo; **el farol de animas** (marca bajo los pies de cada uno y a los 17 ticks sale la llama;
  a media vida, tres tandas; la marca va a los pies del jugador si pisa, aunque este en un borde sin suelo bajo su
  centro: en build-96 ahi no salia) y, a media vida, **llama a su tripulacion** (2 ahogados, +1 por jugador de mas hasta 4:
  `efimero`, se deshacen al acabar). Barra propia (`client/enemigos/CapitanCliente`, arte de
  `tools/hud/barra_capitan.py`: liston de cubierta con cabos, su farol en el centro, relleno de bruma verdemar, gotas),
  musica "Blackmoor Tides" de Matthew Pablo (CC-BY 3.0, id `capitan`, tema 21 de `ClientMusic.boss`; del zip se
  prefiere la version sin voces: `zip_prefiere` en `pistas.json`) y **voz** (ElevenLabs, **"Matthew Schmitz - Old
  Pirate Captain"** `4Vl3K2x290GidNvuaLm7`, `eleven_v4`, flow `JMcBqvpYNqJH1beI5j7S`; 5 frases, tomas a; en
  `procesar.py` `voz_capitan`: mas grave, una segunda voz sorda por debajo y eco corto): `Net.ReyHabla` con linea
  **40 + N** (`Voces.BASE_CAPITAN`; subtitulos `darkcraft.capitan.voz.N`): 1 al empezar (no la repite en 4 min), 2 al
  llamar, 3 si cae su presa, 4 la primera vez que alza el farol, 5 al morir. Al caer: `Rol.jefe` (1500 ecos y el
  cartel), logro `capitan` (hazana), **un frasco mas** (`Frascos.otro`) y piedras de forja.
- **En Brumavela** (`pueblo/PuebloEnemigos`, sitio `brumavela`, 38 puestos): el primero, un hueco de rodillas en el
  canto del muelle; ahogados al pie de la Cuesta (de espaldas), en el almacen de la vela, en el muelle, en el Saliente y
  al pie del penon; un hueco a media Cuesta y un arponero arriba; **los cuatro de la taberna** (grupo `taberna`: la
  tabernera tras la barra) —mientras quede uno, **el timon no se coge**—; calles, capitania (Odo Varn ante su mesa),
  la casa de Lena, cementerio (tumbados), capilla (rezando), cripta, campos; y el Capitan ante la puerta del faro, que
  lleva **una reja** (`PuebloEnemigos.reja`, cada segundo, barrotes de hierro) mientras siga en pie: el **corazon no se
  coge** hasta vencerle (`Pueblo.Datos.capitan`). Nadie a menos de 18 bloques de donde se despierta ni de 14 del
  santuario; `PuebloEnemigos.comprobar` (lo pasan la prueba del servidor y `tools/vista3d/vista.sh pueblo`) mira que
  cada puesto se pisa y se llega a pie. En pacifico no hay nadie y nada esta guardado.
- **Hacer otro enemigo** (`tools/enemigos/`, `python3 tools/enemigos/generar.py [--vistas carpeta] [--solo nombre]`):
  un modulo por personaje (`hueco.py`, `ahogado.py`, `capitan.py`) con `rig()` (esqueleto comun `modelo.humanoide`:
  caderas, muslos y piernas, torso, cabeza, brazos con codo y mano; las armas cuelgan de la mano y apuntan a -Z),
  `materiales(v)` / `brillo(v)` (texturas pintadas por cajas con `pinta.py`; la capa de brillo es lo que luce: ojos,
  llamas), `animaciones(rig)` (bucles y acciones por poses; las manos se escriben **donde van** y **hacia donde apunta
  el arma** —`_mano_d`, `_arma`, `_mano_i`, `_arma_i`, `_agarra`: cinematica inversa de `ik.py`, con memoria en
  `.ik_cache.pkl`—) y `OCULTAS` (piezas que esconde cada variante). Sale la textura y su brillo
  (`textures/entity/enemigos/NOMBRE_V[_brillo].png`), `client/enemigos/<Nombre>Geometria.java` y
  `<Nombre>Animaciones.java` (fotogramas en cadenas, `Animaciones.leer`) y `enemigos/<Nombre>Anims.java` (numeros,
  duraciones y ticks de golpe para el servidor). `--vistas` dibuja cada variante y cada animacion fotograma a
  fotograma (`vista.py`, un dibujante por software) para revisarlo todo sin abrir el juego. En Java: una clase que
  herede de `Enemigo` (`elegir`, `golpe`, `alAvanzar`), su `<Nombre>Modelo` (hereda de `EnemigoModelo`: que bucles y
  acciones se ponen) y el `EnemigoRenderer` generico (textura por variante + capa de brillo + escala).
- Efectos (ElevenLabs, tomas en `TOMAS_SFX`): `hueco_gemido/alerta/dolor/muerte`, `ahogado_gorgoteo/alerta/dolor/muerte`,
  `capitan_farol` (2 y 4), `capitan_llama` (4 y 3), `capitan_llamar` (4 y 1), `capitan_dolor` (2, 4 y 1),
  `capitan_muerte` (3 y 4), `faro_sello` (3 y 1). Bestiario: `hueco_horca/hoz/antorcha`, `ahogado_sable/arpon`,
  `capitan`. Logros `hueco` (el primero que se vence) y `capitan`.
- Comandos `/darkcraft brumavela enemigos [volver | fuera]` y `capitan [vencer | volver]` (sin mas, lleva ante el).
  Prueba del servidor: los puestos, todos fuera haciendo todas sus acciones, lo que guardan y la caida del Capitan.
  Prueba del cliente: tramo `enemigos()` (capturas `cliente_en01..en28`); el tramo `pueblo()` de la 3b corre sin
  enemigos y con el faro abierto.

### Plan grande, parte 3d-1: la cinematica de inicio (la Sombra, la caida y el despertar)

- **Cuando sale**: nada mas elegir clase (`Clase.pedir` → `cine/Intro.empezar`), donde este el jugador (en un mundo nuevo,
  en el muelle de Brumavela, mirando al barco roto). Otra vez: `/darkcraft brumavela inicio`. Dura 40 s; se salta con ESPACIO.
- **Lo que pasa** (`cine/Intro`: los tiempos y los dos planos; es la cinematica **38**): en negro, la Sombra habla
  ("Estabas muerto." / "Sin nombre... Sin tumba... Solo la oscuridad." / "Pero meti la mano... y te arroje de vuelta."); a
  los 11,6 s **la caida** (camara en primera persona, de 510 bloques de alto hasta 2,3 sobre los pies, mirando abajo y
  girando: se ve la isla entera; "Ahora llevas mi marca..."); a los 21,4 s **la Sombra te atrapa** (negro de golpe); **la
  marca** arde en lo negro (un ojo en un aro roto con tres espinas: `tools/cine/marca.py` → `textures/gui/marca_sombra.png`;
  "Hay una puerta que yo no puedo abrir. Tu la abriras por mi." / "Ahora despierta... marcado."); dos latidos y **se abren
  los ojos** (los parpados, un parpadeo, la luz que deslumbra): tumbado boca arriba, se incorpora mirando al barco y se
  pone en pie; a los 39,6 s la camara ya es la del jugador (misma posicion, giro, cabeceo y campo de vision: no hay corte).
- **Las cinematicas "con guion"** (del numero 38 en adelante; 39 es reparar el barco y 40-42 los viajes: parte 3d-2): el servidor
  solo manda los planos (`Cinematics.playShots`) y el cliente lleva lo demas en `client/Guion` (lo que suena y cuando
  —lista de pasos con su segundo—, el negro, lo que se pinta debajo y encima del negro, el ladeo y el campo de vision de
  mas, los subtitulos de la Sombra) con los tiempos de la clase comun (`Intro`). `ClientCine` pregunta a `Guion` en sus
  ganchos (`GUION` = 38): sin oscurecer la imagen ni subir el brillo (acaba en los ojos del jugador y se notaria).
- **La voz de la Sombra** (ElevenLabs, `eleven_v4`): **"Grav - Deep, Dark & Sinister"** `6qtlupOXBYtv2nYY2VgL`, flow
  `AqkIj7OE07LCh5cMIoZU` (elegida por tono: la mas grave de las probadas, F0 ~65-83 Hz; si al usuario no le gusta, se
  cambia). 9 frases con la voz normal (tomas a/b) y **las mismas susurradas** (`susurro_N`): `procesar.py` `voz_sombra`
  monta la voz (un 5 % mas grave) + tres copias del susurro retrasadas a izquierda, derecha y centro + tres ecos + un
  eco de vacio de 5,5 s (`afir` con ruido que se apaga: `dry=1`, que es la ganancia de la ENTRADA), en **estereo**, a
  -15 LUFS. Salen `sounds/sombra/voz_N.ogg` (suenan sin sitio: `sombra.voz.N`) y `habla_sombra` en `rey_voces.json`
  (`Voces.sombra(n)`: lo que tarda en decir cada frase; los subtitulos `darkcraft.sombra.voz.N` duran eso + 0,9 s). Las
  frases 7-9 son las del viaje a Valdrenn (3d-2).
- Efectos (ElevenLabs, 4 tomas; las elegidas en `TOMAS_SFX`): `intro_abismo` (3; el zumbido bajo la voz, 30 s),
  `intro_caida` (2; el viento), `intro_atrapa` (2; crece y da el golpe a los ~1,3 s), `intro_marca` (1), `intro_latido`
  (4). Los que son casi todo graves (`GRAVES`) llevan sumadas la octava y la quinta de encima: en altavoces pequenos no
  se oian.
- **Ojo con los enlaces de ElevenLabs: caducan a las ~2 horas de generar el audio** (no a las 00:00 UTC): se apunta
  `descargas.json` y se hace push nada mas generar.
- Pruebas: servidor, linea `Inicio: ...` (nada tapa la caida, donde acaba cada plano, lo que duran las frases; en la
  prueba CON los otros mods sale "bloques que tapan 1 [Y 701 ...red_mosaic_glass]": es el escaparate de bloques de esa
  prueba, que esta encima del origen, no un fallo); cliente,
  tramo `intro()` (capturas `cliente_in01..in14`) y los niveles del audio grabado (`audio_cliente.txt` va ~9 s por delante
  del reloj del informe).

### Plan grande, parte 3d-2: el barco que va y viene (la reparacion, el viaje y el muelle de Valdrenn)

- **El barco es de todos y solo hay uno.** `Pueblo.Datos` guarda donde esta: `barcoEn` (el amarre donde esta o al que va:
  `Amarres.BRUMAVELA` 0 / `VALDRENN` 1), `puesto` (donde estan de verdad sus bloques; -1 mientras vuela), `resto` (el
  amarre del que aun hay que quitarlo; -1), `muelle` (el de Valdrenn ya esta hecho), `carga` (lo que llevaban sus
  barriles mientras no se ha vuelto a meter) y `obra` (se esta reparando). `Travesia.onTick` pone al dia lo que se pueda
  cada tick: si el servidor se cierra a medias o todos saltan la cinematica, el barco aparece en su destino en cuanto
  estan cargados sus chunks.
- **`pueblo/Garza`: el plano del barco, celda a celda** (roto y entero). Se saca dibujando `gen/Barco` en un `Canvas` que
  solo apunta (`Canvas.apunte`: sin mundo). Ojo: la generacion del mundo **solo une las vallas puestas con `unir`**; las
  puestas con `set` se quedan como postes sueltos, y el plano hace lo mismo (`Apunte.une`). `Garza.dejar(lvl, que, dx,
  dz, cima)` deja un amarre con el barco roto (0), entero (1) o sin barco (-1: nube hasta lo alto del mar de nubes de ese
  sitio y aire). **Todo se pone con `UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE` (18)**: con la bandera 2 de antes los vecinos
  se actualizaban y saltaban al suelo las escaleras de mano, el farol y la placa de presion. Lo que guardan los barriles
  viaja con el barco (`carga` / `descargar`); un cofre que haya puesto un jugador en un hueco del barco suelta lo suyo.
  `remendar` (una vez por sesion y amarre) vuelve a poner los bloques del barco que falten, sin tocar lo demas.
- **`pueblo/Amarres`: los dos amarres son el mismo barco desplazado** (`de(lvl, cual)` → {dx, dz}; proa al sur, el muelle
  al este). El de Valdrenn sale de `MundoNubes.llegada`: el anden va pegado al oeste del camino de nubes, 35-59 bloques
  al sur de la Torre de Ascenso. **El muelle de Valdrenn no se genera con el mundo** (asi sale igual en los mundos de
  antes): se hace la primera vez que alguien esta a menos de 150 bloques o zarpa hacia alli (`pedir` → `tick`; pide los
  chunks con un billete propio): primero **se calma el mar de nubes** en un disco (llano hasta 36 bloques, se funde con
  el natural hasta 56; un chunk por tick; las columnas con algo que no sea nube no se tocan) y despues se pone el anden
  (`pueblo/gen/MuelleValdrenn`: farolas, pretiles, norays, la **campana** y un **santuario** «Muelle de Valdrenn»).
  `hacerYa` lo hace de una vez (pruebas y comandos). La campana del espigon de Brumavela se pone sola en (2, 252, 135).
- **Reparar** (cinematica **39**, `cine/Reparacion`): con las cinco piezas, desde la cubierta. Los bloques que le faltan
  al barco roto se ponen por etapas (`Reparacion.cuando`: se va lo roto, el casco de proa a popa, el palo de abajo arriba,
  las vergas, las velas desplegandose, las piezas y un destello a los 12,8 s); tres planos; la ven los que esten a menos
  de 56 bloques.
- **Zarpar** (desde la rueda del timon; cosa 12): viajan todos los que esten a menos de 40 bloques del palo. Cinematica
  **40** la primera vez que se va a Valdrenn (55 s; la Sombra dice sus frases 7-9 al llegar) y **41** las demas (34 s, ir
  o volver). **El barco que vuela es una entidad que solo existe en el cliente** (`cine/BarcoVolador` +
  `client/cine/BarcoVoladorRenderer`: los bloques del plano con sus modelos, girados alrededor del pie del palo): mientras
  la pantalla esta en negro (`cambioA`) el barco de bloques se quita del muelle y aparece la entidad; sube en vertical 11
  bloques, avanza al sur, corte en negro (`corteB`: se lleva a los jugadores al otro amarre), llega del sur con la proa
  al norte, frena sobre su amarre, **se da la vuelta a 12 de alto** (por debajo el casco daria en las farolas) y baja; en
  otro negro (`cambioC`) vuelve a ser de bloques y el ultimo plano baja del aparejo a los ojos del jugador, de pie en el
  alcazar. `cine/Viaje` tiene los tiempos, la trayectoria (`barco(T, t, o, d)`) y los planos (muestreados: siguen al
  barco), iguales para servidor y cliente. Mientras tanto los jugadores flotan invisibles 80 bloques por encima (asi no
  se les ve la armadura desde el muelle); `Cinematics.playViaje` los lleva y los deja (si se salta, en cubierta si el
  barco ya esta; si no, en el muelle o en el camino de nubes).
- **Los cortes los marca el cliente, no el reloj del servidor** (`Viaje.corte`, `Net.CineFase`): en cada uno de los tres
  negros en que cambia el mundo (1: se quita el barco de bloques; 2: el salto al otro amarre; 3: se vuelve a poner) el
  cliente avisa al servidor y **espera con la pantalla en negro y el reloj de la cinematica parado** (`Guion.espera`,
  desde `ClientCine.onTick`) hasta ver hecho lo que toca (el palo ya no esta / el jugador ya esta alli con sus chunks /
  el palo ya esta), con un tope de 5, 12 y 8 s. El servidor hace cada cosa cuando TODOS los que miran han avisado (o 6 s
  despues de su hora, si alguno no avisa) y acaba la sesion cuando el cliente dice que ha terminado (`FIN_CLIENTE`).
  Asi da igual que el servidor o el cliente vayan con retraso (al llegar a un sitio nuevo se generan muchos chunks):
  con los dos relojes sueltos se veia el barco dos veces o el muelle vacio. En la prueba del cliente el reloj de los
  pasos tambien se para mientras se espera (`Guion.esperadoSiempre`).
- **Llamar** (la campana del muelle donde no esta el barco; cosas 13 y 14): cinematica **42** (20 s), sin mover al
  jugador: el barco llega volando y atraca (sin musica: la campana, el viento, las velas al detenerse, el casco al bajar
  y el golpe). Con el barco ya alli, de camino o roto, la campana solo suena y lo dice.
- **Los planos** (`Viaje.planos`): el timon con el jugador de cara (se le lleva detras de la rueda) | se eleva, desde la
  aleta de babor a ras de nube | las velas, desde lo alto por la popa | (solo la primera vez) Valdrenn, siguiendole por
  detras, y los juramentos, desde abajo | **atraca: desde el mar de nubes en calma, al sureste** (el muelle delante y la
  torre o el pueblo detras; desde el propio anden el barco no cabia en el plano) | en cubierta: la camara baja del aparejo
  a los ojos del jugador. **Al acabar el jugador mira a donde acaba mirando la camara** (`ClientCine.mirar`, al pasar el
  corte 3: si no, `ClientCine` le dejaba mirando a donde miraba al empezar y la vista daba media vuelta).
- **El barco que vuela se dibuja siempre con toda la luz del cielo** y un minimo de luz propia
  (`BarcoVoladorRenderer.getSkyLightLevel` / `getBlockLightLevel`): lejos del jugador o muy alto el cliente a veces da
  luz 0 y en build-100 se veia negro al alejarse. En Valdrenn es de noche cerrada y no hay rayos (regla de `Storm`: solo
  truenos): la llegada sale oscura en las capturas sin shaders; el anden lleva cuatro farolas.
- **Sonido** (ElevenLabs; tomas en `TOMAS_SFX`): `barco_campana` (1 y 4), `barco_eleva` (3), `barco_velas` (1),
  `barco_vuelo` (4; el viento, 25 s, en streaming), `barco_atraca` (4; empieza en el segundo 3,8 de la toma: `INICIO`),
  `barco_repara` (3). **Musica del viaje**: "The Traveller" de Viktor Kraus (CC-BY 3.0), cortada a 84 s (`"corte"` en
  `pistas.json`): `ClientMusic.viaje`. **Mientras habla la Sombra la musica baja a 0,36 y el viento a 0,4** (`Guion.viaje`:
  entre dos frases seguidas no suben; sin eso, en build-100 la voz quedaba al mismo nivel que la musica). Los que se
  quedan en el muelle oyen las velas al irse, la campana y el golpe al atracar (`Travesia.oyen`).
- En Valdrenn siempre es de noche y hay tormenta (`world/Storm`): el corte en negro tapa el cambio y la lluvia entra en
  el ultimo plano.
- Comandos: `/darkcraft brumavela reparar [ya] | zarpar [primero] | llamar | muelle | barco <brumavela|valdrenn>`.
- Pruebas: servidor, `barcoQueVaYViene` (lineas `La Garza Gris, ...`, `Muelle de Valdrenn: ...`, `Cinematica 40/41/42
  ...`, `La reparacion: ...`: el plano contra el barco generado, nada salta al suelo, el mar en calma, el barril con sus
  diamantes, se llega a pie a todo, ni la camara ni el casco dan en nada); cliente, tramo `barco()` (capturas
  `cliente_ba01..10` la reparacion, `vj01..19` el primer viaje y el muelle, `vn01..08` la vuelta, `vl01..06` la llamada).

### Plan grande, parte 4a: la guardia de Valdrenn (caballeros y arqueros en sus puestos)

Respuestas 10-20 de `PLAN.md` ("Valdrenn dificil"), primera tanda. **4b**: perros de hueso, espectros de la corte y el
torreon por dentro. **4c**: capitanes de elite, emboscadas y la torre del arbol por dentro. **4d**: los dos guardianes
(mini-jefes con barra y logro) antes del Rey y de Ser Gavran.
- **`enemigos/Guardia`** (los caballeros de antes rehechos como `Enemigo`, modelo de `tools/enemigos/guardia.py`): tres
  variantes, `ESPADA` 0 (espada y escudo, tabardo rojo), `ALABARDA` 1 (morado) y `BALLESTA` 2 (verde). 40 de vida,
  golpe 5,5, armadura 8. Acciones (`GuardiaAnims`): espada_tajo / combo / escudazo / **bloquea**, alabarda_pica /
  hachazo / barrido, ballesta_disparo / culatazo, aturdido, morir, levantarse, alzarse. **El de espada se cubre**: de pie,
  sin atacar y con el golpe de frente (`cubre`), las flechas no le hacen nada y los golpes x0,2 (suena el escudo y hace
  `ESPADA_BLOQUEA`); por la espalda o mientras ataca, todo. El de ballesta se queda a ~9 bloques y dispara (recarga 1,7-3 s).
- **`enemigos/Arquero`** (esqueleto, `tools/enemigos/arquero.py`): 22 de vida, **alcance de vista 64** (en lo alto de las
  murallas los jugadores quedan a 40-60 bloques), dispara a menos de 52 en horizontal y hasta 56 de desnivel, no persigue
  a nadie (solo patada o golpe con el arco a quien llega a su lado). **Las flechas se apuntan con la caida en cuenta**
  (`Enemigo.tiro`: el tiro bajo que con la fisica de las flechas del juego —avanza, frena un 1%, cae 0,05 por tick— cae
  en el blanco; lo usan arco y ballesta): con la formula de los esqueletos del juego, desde una muralla, caian cortas.
  Disparan `enemigos/Flecha` (una flecha del juego que **pasa entre los suyos**: desde lo alto de la puerta tiran por
  encima de la pelea; ademas `Enemigo.hurt` no deja que un proyectil de un enemigo dane a otro).
- **`valdrenn/ValdrennEnemigos`: 62 puestos en coordenadas del reino** (`locales(sur)`; `sur` = donde acaba el puente de la
  Torre de Ascenso): el puente (2), el camino y la puerta exterior (3 alabardas), arqueros en las **almenas** de la muralla
  exterior (x ±36 y ±60) y en las de **lo alto de la puerta** (x ±10, y 413: `puerta_en_alto`, sin camino a proposito: ven
  el camino desde 12 bloques de la puerta hasta el puente y hay que pasar corriendo), el camino mayor, el cuartel y el
  patio de armas (entrenando de dos en dos), la puerta interior (2 alabardas y 2 arqueros en las almenas de su tejado, que
  ven todo el camino mayor), las almenas de la muralla interior, el patio de la fuente y la puerta del torreon, cuatro de
  ronda, once en los cruces del pueblo, el cementerio (velando de rodillas y tirados entre los muertos) y los edificios del
  este y del norte. **Los arqueros se ponen EN el hueco de la almena (de pie sobre el antepecho), nunca en el adarve**:
  desde el adarve el muro les tapa todo lo de abajo. Los huecos caen en las x con `x % 4` 3 o 0 en las dos murallas y en
  las dos puertas; un arquero en una almena solo ve lo que tiene mas o menos delante (los merlones tapan los lados).
  Puestos con `quita` 1 / 2 (grupo `~rey` / `~gavran`): dejan de salir con el Rey / Ser Gavran vencidos (el reino mas
  tranquilo). Se apuntan solos cuando alguien pasa a menos de `FOOTPRINT + 160` del centro (`onServidor`).
- **`enemigos/Puestos`** (lo de 3c, ahora para mas sitios): `registrar(clave, puestos, aqui, activo)` (activo: si ese puesto
  sale ahora), `apuntado`, `olvidar`, `claves`, `marcar`, `todos`, `sacarTodos`; si el sitio del puesto esta ocupado busca al
  lado (a 1-3 bloques). Un caido no vuelve hasta que alguien descansa en un santuario o muere (`reaparecer`).
- **Ya no salen caballeros al azar en Valdrenn** (`caballeros/Caballeros`); fuera (la Fortaleza de Hueso...) los que salen
  son `Guardia`. El logro de los huecos solo cuenta huecos y ahogados.
- `gen/Canvas.nivel()` / `crear(tipo)`: en un `Canvas` sin mundo (`tools/vista3d` y el plano de `Garza`) los soportes de
  armadura y los cuadros no se ponen (antes saltaba `getLevel()` y se cortaba el edificio a medias).
- Comandos: `/darkcraft valdrenn enemigos [volver | fuera | sacar | ir <grupo>]`.
- Sonido (ElevenLabs, `TOMAS_SFX`): `guardia_alerta`, `arco_tensar`, `arco_suelta`, `arquero_alerta`, `arquero_huesos`,
  `arquero_dolor`, `arquero_muerte`; los caballeros siguen con los de antes (`zombi_caballero_*`, `armadura_paso`,
  `escudo_golpe`).
- Sin abrir el juego: `tools/vista3d/Volcado.java valdrenn` levanta el reino entero (marcas de los puestos: G espada, A
  alabarda o arquero, B ballesta) y escribe `ValdrennEnemigos.comprobar`: cada puesto se pisa y se llega andando desde el
  puente (salvo los `_en_alto`) y lo que ve cada arquero del camino a tiro.
- Pruebas: servidor, `guardiaDeValdrenn` (lineas `Valdrenn, puestos de la guardia`, `Valdrenn, la guardia fuera` —todos en el
  suelo, ninguno metido en un bloque, todas las acciones acaban— y `Valdrenn, la guardia: escudo ...; disparan 2 flechas;
  con el Rey vencido salen ...`); cliente, tramo `guardia()` (capturas `cliente_va01..17`: la entrada, la puerta, el arquero
  de la almena, la fila delante de la fuente y sus golpes, uno que cae, el cementerio y en supervivencia ante la puerta;
  con vision nocturna: en Valdrenn siempre es de noche). Al acabar el tramo los puestos de ese reino quedan por caidos (las
  pruebas de despues son de antes de la guardia).

### Plan grande, parte 4b: perros de hueso y espectros de la corte (y el torreon por dentro)

- **`enemigos/Perro`** (`tools/enemigos/perro.py`: un esqueleto de perro con jirones de pellejo, collar de pinchos y brasas en
  las cuencas; las patas se llaman como las de una persona —brazo / antebrazo / mano delante, muslo / pierna / pata
  detras— y van sin cinematica inversa). 16 de vida, 3,5 de golpe, muy rapido (`prisa` 1,25). **Mordisco** (1 s) y
  **salto** (se agacha medio segundo —se ve venir— y vuela hasta 4-5 bloques: en `alAvanzar` se le da la velocidad en el
  tick 11 y se le quita el frenado de las acciones mientras va por el aire; muerde al caer). El primero que ve a alguien
  **aulla** (`setTarget` → accion `aullido`; como mucho uno cada 6 s en todo el servidor) y los perros a 20 bloques
  acuden. Posturas: `REZA` = echado durmiendo, `TUMBADO` = un monton de huesos (se levanta al acercarse). En el cliente
  trota o galopa segun lo que corre (`PerroModelo`: dos bucles de andar mezclados).
- **`enemigos/Espectro`** (`tools/enemigos/espectro.py`): sin piernas (el faldon acaba en jirones), flota (en el modelo:
  `ARRIBA` 3,2 px), no cae con dano ni hace ruido al moverse. Variantes `ESTOQUE` 0 (el cortesano: gorguera, gorra con
  pluma, media capa; **estocada** que llega a 4,4 y **tajo doble**) y `GARRAS` 1 (la dama: diadema, velo, cuello alto;
  **zarpazo** que deja lentitud y **lamento**: a los 0,9 s un grito a 5 bloques —dano magico, lentitud y debilidad—, con
  la cabeza atras y los brazos abiertos antes). Los dos se **desvanecen** (lejos de su presa, o a veces tras un golpe) y
  aparecen a 2-3,5 bloques a su lado o a su espalda. **Se dibujan medio transparentes** (`EspectroRenderer` +
  `EspectroModelo`, `RenderType.entityTranslucent`; el alfa respira, baja a 0 al desvanecerse y al morir se va apagando
  mientras sube; la capa de brillo tambien). `EnemigoModelo` acepta otro tipo de dibujo; `Enemigo.accionDesde` = el tick
  del cliente en que empezo la accion que se ve.
- **Puestos nuevos** (`ValdrennEnemigos`): fuera, jaurias (la perrera junto al cuartel, durmiendo; cuatro sueltas por las
  calles; dos tiradas en el cementerio; tres en el laberinto de setos; dos a la puerta de las cuadras) y la catedral (dos
  rezando en el pasillo de la nave, una dama en el altar y dos cortesanos que rondan por las naves de los lados). **Dentro
  del torreon** (`interiores(plan)`, de las salas del plano: `CastlePlan.torreon()` y `Building.plantas()`): la planta
  baja entera (gran salon: un cortesano y una dama; comedor: dos; cocina: dos perros durmiendo; sala: uno; armeria: dos
  caballeros) y, arriba y en la cripta, salas sueltas por turnos de cada clase (capilla y cripta: una dama rezando;
  biblioteca / estudio: un cortesano; galeria: uno; dormitorio: una dama acurrucada; carcel: un perro tirado; tesoro: un
  caballero), separadas 10 bloques y con tope por planta (5, 4, 3, 3; el primer sotano no: lleno de toneles y sin paso).
  `todos(plan)` = los de fuera y los de dentro (~116 en total); `comprobar` los mira todos.
- Sonido (ElevenLabs, `TOMAS_SFX`): `perro_grunido`, `perro_ladrido`, `perro_aullido`, `perro_mordisco`, `perro_dolor`,
  `perro_muerte`, `espectro_susurro`, `espectro_alerta`, `espectro_lamento`, `espectro_zarpazo`, `espectro_desvanece`,
  `espectro_dolor`, `espectro_muerte`. Bestiario: `perro`, `espectro_cortesano`, `espectro_dama`.
- Pruebas: servidor, `perrosYEspectros` (linea `Valdrenn, perros y espectros: el salto avanza ...; el espectro aparece a ...;
  el caballero sin un rasguno, la flecha sigue`) y los de siempre (`la guardia fuera` ya cuenta perros y espectros);
  cliente, tramo `perrosEspectros()` (capturas `cliente_vb01..17`: la fila y sus golpes, uno que se deshace, una jauria,
  la catedral, el torreon, la perrera en supervivencia y el lamento de cerca).

### Plan grande, parte 4c: capitanes de elite, emboscadas y la torre del arbol por dentro

- **`enemigos/Elite`** (`tools/enemigos/elite.py`, escala 1,15: armadura negra y oro, corona con penacho, capa, mandoble;
  ojos rojos en la capa de brillo): 120 de vida, golpe 7, armadura 12, bestiario `capitan_elite`. Al ver a alguien a menos
  de 16 **grita** primero (`arengar`, en el tick 9 del grito: el y los enemigos a 18 bloques +30 % de dano 12 s —
  `Enemigo.animo(mas, ticks)`, chispas rojas— y van a por esa presa —`Enemigo.acudir`—); **tajo** de arriba abajo con
  **onda** (`onda()`: a quien este DE PIE a 2,7 bloques del golpe, 0,65 del dano; saltando o rodando se libra), **barrido**,
  **estocada** y **embestida** (entre 4,6 y 9,5 bloques; cruza media plaza). Al caer: logro `capitan_elite` y los suyos a 18
  bloques **se desaniman** (-25 % de dano y de paso 15 s, humo; dudan un momento). Deja piedra de forja I (60 %) y II (18 %).
- **Capitanes** (`ValdrennEnemigos`): en la puerta exterior (con sus alabardas), en el patio de armas (se va con el Rey) y
  de ronda por el patio de la fuente.
- **Emboscadas** (`valdrenn/Emboscadas`, respuesta 14 del plan: el puente, la plaza de la fuente, la escalera del torreon,
  el cementerio y la biblioteca): cada una, una zona (`AABB`) y sus puestos (grupo `emb_<sitio>`, tumbados como un muerto
  mas) que **no salen hasta que se dispara**: un jugador en supervivencia entra en la zona → cuerno (`sfx.emboscada`), humo
  y almas, se alzan todos a la vez (`Puestos.sacarGrupo`, `sfx.emboscada_alzarse`) y van a por el. Al descansar o morir
  (`Puestos.alVolver` → `rearmar`) los que siguen en pie vuelven a esconderse y se vuelve a armar; vencerlos a todos da el
  logro `emboscada`. La de la escalera del torreon: dos caballeros en el rellano del tercer piso y un perro en el del segundo.
- **La torre del arbol por dentro** (`ValdrennEnemigos.arbol`, de `TreeTower.escalera()`): en uno de cada dos rellanos,
  por turnos, un caballero, dos perros, una alabarda o una dama; en el ultimo, un capitan (grupo `torre_arbol_tras_puerta`,
  se van con Ser Gavran; `comprobar` los cuenta aparte).
- Sonido (ElevenLabs, `TOMAS_SFX`): `elite_alerta`, `elite_grito`, `elite_dolor`, `elite_muerte`, `elite_impacto`,
  `elite_embestida`, `emboscada`, `emboscada_alzarse`. Comandos: `/darkcraft valdrenn enemigos emboscada [armar | <nombre>]`.
- Pruebas: servidor, `elitesYEmboscadas` (lineas `Valdrenn, el capitan de elite: ...` —arenga, onda, embestida, desanimo— y
  `Valdrenn, las emboscadas: N de 5 bien ...`); cliente, tramo `elitesEmboscadas()` (capturas `cliente_vc01..17`: el capitan
  y sus golpes, su caida, el de la puerta, un rellano de la torre del arbol y la emboscada de la plaza en supervivencia).

### Plan grande, parte 4d: los dos guardianes (Ser Brandt y Vareth)

Respuesta 15 del plan: un mini-jefe guardian antes de CADA jefe (el salon del Rey y la torre del arbol), con barra y logro.
- **`enemigos/Guardian`** (lo comun): esperan de rodillas en su puesto (grupo `Puestos.JEFE`: caen una vez y no vuelven al
  descansar; `quita` 1 / 2: con el Rey / Ser Gavran vencidos ya no salen). De rodillas ven a 16 bloques si hay linea de
  vista. Al ver a alguien empieza la pelea (`EN_PELEA`, sincronizado): se levanta y ruge. **Barra de vida propia**
  (`ServerBossEvent` con su nombre para quien este a 48; el cliente la dibuja con su arte: `client/enemigos/GuardianCliente`
  con `textures/gui/<tipo>_barra.png` de `tools/hud/barra_guardianes.py`; chispas o hojas que caen) y **su musica** mientras
  pelea (temas 31 y 32 de `ClientMusic.boss`: `ReyCliente` la pone si hay un guardian peleando a 72 bloques). Con amigos, mas
  vida (`paraGrupo`). A **media vida**, una vez por pelea, ruge otra vez y cambia (`alSegundaMitad`). 110 ticks sin presa:
  vuelve a lo suyo con la vida entera. Ni las caidas ni los demas enemigos le hacen dano; el fuego tampoco (salvo a Vareth).
- **Ser Brandt, el Muro del Rey** (`enemigos/Brandt`, `tools/enemigos/brandt.py`, escala 1,5: coraza, yelmo con corona y
  puas, un paves en la muneca izquierda —angulos fijos del brazo, sin cinematica inversa— y un martillo de guerra): 420 de
  vida, golpe 9, armadura 14. De rodillas en el gran salon, 7 bloques delante de la puerta del trono, de espaldas a ella.
  **El paves**: de frente (75 grados a cada lado), quieto, en el muro o embistiendo, deja pasar el 15 % (las flechas, nada).
  Tras tres golpes seguidos se pone en el **muro** (no le entra nada) y, si le pegaron, contesta con un **escudazo**.
  **Martillazo** (lo alza casi un segundo; onda al dar en el suelo: saltando o rodando se libra), **golpe** de lado,
  **escudazo** (empuja y frena), **embestida**; a media vida, el **terremoto** (salta y la onda llega a 6 bloques). Deja
  3500 ecos, dos piedras de forja II y la ceniza del sismo.
- **Vareth, el Guardian de Corteza** (`enemigos/Corteza`, `tools/enemigos/corteza.py`, escala 1,45: corteza y musgo,
  astas de ramas, garras de tres dedos, el corazon verde y los ojos en la capa de brillo): 520 de vida, golpe 9,5,
  armadura 8; **el fuego le hace el doble**. De rodillas en la base de la torre del arbol, detras del hueco de cristal del
  ascensor (no ve a quien entra hasta que lo rodea). **Zarpazo**, **doble zarpazo**, **raices en linea** (brotan por el
  suelo hacia delante cada 1,4 bloques hasta 13: `EfectosRaiz.estaca` y el dano un instante despues, `Brote`), **espinas**
  (abanico de 70 grados hasta 8 bloques, frenan); a media vida, **raices alrededor** (dos anillos). Deja 5000 ecos, tres
  piedras II y el talisman del hueso.
- **Los sellos** (`valdrenn/Guardianes`): mientras vive su guardian (y su puesto esta en uso) y hay alguien a 48 bloques,
  barreras invisibles con una cortina de chispas rojas y negras y fuego de almas cierran todas las puertas del salon del
  trono (`ValdrennEnemigos.selloTrono`: 45 bloques) y la escalera y el ascensor de la base de la torre del arbol
  (`selloArbol`: 21); solo en huecos de aire. Aviso al acercarse (`darkcraft.guardian.sello.<tipo>`). Al caer el guardian
  el sello se rompe en el acto (`sfx.sello_rompe`, almas) y 4 s despues, a cada uno de los que le plantaron cara (y a los
  que esten a 48): el cartel de JEFE ABATIDO con sus ecos, la hazana (`brandt` / `vareth`, con su entrada del diario) y el
  bestiario. Al abrirse quita toda barrera de sus huecos (tambien las que quedaron de antes de cerrar el servidor).
- Sonido (ElevenLabs, solo se bajaron las tomas 1 y 2): `brandt_rugido`, `brandt_martillazo`, `brandt_escudo`,
  `brandt_dolor`, `brandt_muerte`, `corteza_rugido`, `corteza_zarpazo`, `corteza_raices`, `corteza_espinas`,
  `corteza_dolor`, `corteza_muerte`, `sello_rompe`. Musica: "Fantasy - The March Upon the Red Mountain" de HitCtrl
  (CC-BY 3.0, id `brandt`) y "Sinister Boss Appears!" de cynicmusic (CC0, id `vareth`).
- Comandos: `/darkcraft valdrenn guardian <brandt | vareth> [ir | vencer | volver]` (sin mas: como esta y su sello).
- Sin abrir el juego: `tools/vista3d/Volcado.java valdrenn` marca a los guardianes (K Brandt, V Vareth) y escribe que hay en
  los huecos de cada sello (`sello del trono: 45 bloques {air=45}`).
- Pruebas: servidor, `guardianesDeValdrenn` (lineas `Valdrenn, los guardianes: ...` —sellos cerrados, la pelea, el paves,
  el fuego y las raices de Vareth— y `Valdrenn, los guardianes caen: ...` —sellos rotos, repartos, no vuelven al
  descansar, con sus jefes vencidos no salen—); cliente, tramo `guardianes()` (capturas `cliente_vd01..24` Ser Brandt,
  `cliente_ve01..24` Vareth y los dos en el bestiario, `vd30` y `ve30`). Al acabar el tramo los puestos del reino quedan
  por caidos (las pruebas de despues, el Rey y Ser Gavran, son de antes).

### Plan grande, parte 5a: la torre bajo la cupula (la Fortaleza de Hueso nueva)

Respuestas 3, 4, 27-35, 38-43, 45 y 50 del plan. La fortaleza de murallas cuadradas (`FortalezaHueso`, borrada) se cambia
por **`gen/TorreCupula`** (en `gen/FortalezaPlan`, que sigue siendo el plano: isla de radio 74, camino, ruinas, nubes):
- **Medidas** (constantes publicas de `TorreCupula`): murallas en ruinas en anillo de radio `RM` 44 (seis tramos caidos
  con escombros, ocho torrecillas rotas); **cupula** semiesfera de radio `RD` 40 con el centro en el suelo del patio
  (nervios de hueso y basalto pulido, paneles ahumados, negros y rojos —una banda entera roja—, rotos uno de cada cinco,
  mas por el oeste); **torre** octogonal de apotema `AT` 10 y muros de 2 que atraviesa la cupula: sotano (mazmorras) en
  `Y + SOTANO` (-10), plantas cada `PISO` (10) desde Y (planta baja: sala de guardias y armeria con el cofre del acertijo;
  capilla; aposentos x2; ruina; observatorio) y la **terraza** en `Y + TERRAZA` (60), cuadrada de 27 (`TL` 13), con el
  portal (hueco 9 x 13, `AX`/`AALTO`, en `cz + AZ` = -11; arco con `arcoPortal`) y el altar delante (`cz - 6`).
- **Puerta** (`PX`, `PALTO` 13, `PZ` 45 = la reja, `PFUERA` 48 = la cara de fuera): cuerpo de puerta de 30 de alto con
  arco gotico apuntado en las dos caras (`arco(h)`), paso rectangular de 7 x 13 por dentro, la reja en su ranura (sube
  `PALTO` bloques), la sala del torno encima (escalera de mano al este) y el rellano de fuera (z `PFUERA+1..+7`). A los
  lados, **dos caballeros encapuchados** de 18 de alto con espada clavada (`ojos()`: se les encienden al abrir).
- **Jardin muerto** bajo la cupula: caminos, parterres, estanques con nenufares bajo los huecos (`bajoHueco`), cristales
  caidos, 9 arboles secos (nunca a menos de 8 del camino del sur: por alli pasan las camaras de las cinematicas), 14
  rosales, 16 bancos y la fuente rota en `cz + 28`. Lianas que cuelgan de la cupula (no mas abajo de Y + 3), enredaderas
  por la torre, musgo y hongos dentro.
- **Escalera**: de caracol en la torrecilla del este (centro `cx + TX`, radio `RTU`): 10 escalones por vuelta, uno por
  bloque de altura (`pisaEscalon`), el del oeste es el descansillo de cada planta con su puerta hacia la torre (del sotano
  a la terraza); sigue por encima de la terraza hasta su tejado conico (la terraza no la pisa) y arriba tiene suelo donde
  ya no hay escalones.
- **Ascensor** (respuestas 43/45/50): el mismo `arbol/Ascensor` de tipo `FORTALEZA` (`setTipo`, sincronizado y guardado):
  **placa cuadrada de hueso** con runas rojas y una calavera (`textures/entity/ascensor_hueso[_brillo].png`, de
  `tools/fortaleza/ascensor_hueso.py <carpeta>`), particulas rojas y ceniza blanca, avisos
  `darkcraft.ascensor.hueso.*`. Sube por el tubo de barrotes y cristal rojo del centro de la torre (5 x 5) de Y a la
  terraza; abajo se entra por el sur, arriba el hueco esta tapado con barrera (encima se la llama). Lo pone
  `FortalezaEventos.asegurarAscensor` (uno, con la fortaleza en pie y el chunk procesando entidades).
- **Mundos de antes** (respuestas 3 y 4): `FortalezaDatos.VERSION` = 2; una fortaleza guardada sin version (= 1) se
  rehace al acercarse alguien (`rehacer`): sus chunks pasan a `porLimpiar` (se vacian de Y - 55 a Y + 110 antes de
  construir), puerta cerrada, sin puente ni portal (los logros se quedan), el santuario se vuelve a poner en el rellano
  nuevo (`Santuarios.olvidar`), lo que quedara de su escalera de hueso por debajo se quita (`puenteViejo`,
  `FortalezaPlan.centroPuenteAntiguo` rehace la isla de antes, `quitarPuenteViejo`: solo sus bloques) y a quien ya
  vencio a Ser Gavran y no lleva la llave se le da otra (`devolverLlave`, una vez por jugador, `llave_devuelta`).
- **En pie** (`completa`) con los chunks de la isla y la fortaleza (`FortalezaPlan.nucleo`): el mar de nubes llega a 136
  bloques y con distancia de vision 7 no se carga entero; sus chunks se siguen haciendo segun se cargan.
- Cinematicas nuevas: `CineShots.puertaHueso` (desde el camino entre las estatuas, la reja de cerca, cruza al jardin y
  sube mirando la torre) y `chapter6` (de fuera, el jardin, la torre, la terraza y el portal; `primero` elige la toma
  con la camara libre).
- El Espejo: "Jardin de la Cupula" (`fortaleza_cupula`), la torre sigue siendo "Torre del Homenaje".
- Sin abrir el juego: `tools/vista3d/Volcado.java torre` (la isla y la torre en 0,0, Y 320).
- Pruebas (cliente, tramo `fortaleza()`): al llegar la convierte en una "de antes" con escalera (columna de hueso) y
  comprueba que se rehace (linea `fortaleza de antes, rehecha: ...`); `comprobarEscalera` sube la escalera de la
  torrecilla del sotano a la terraza (`... 0 tapados`) y cuenta lo que se anda en cada planta; al final viaja en el
  ascensor de hueso (`cliente_f45..47`).

### Plan grande, parte 5b: la montana y el puente largo

Respuestas 36 y 37 del plan: ya no hay escalera de caracol ni corriente de nubes; se sube por una montana y la llave
tiende un puente largo desde su cima.
- **`gen/Montana`** (en `FortalezaPlan.montana`, cima en `(cx, bordeZ + LARGO + RP + 1)` a `Y - BAJO_CIMA`, es decir 304;
  `LARGO` 78): solo del mundo de nubes. Un cono de roca oscura (`CONO` 2,6 de bajada por bloque, crestas con ruido y ocho
  agujas de roca por los lados, nunca al sur ni al norte) con un **hombro** al sur (`HOMBRO` 22 de medio ancho, baja 0,8
  por bloque) por el que va el **sendero en zigzag**: tramos de `2*MEDIO` (32) a lo ancho, un bloque de bajada por cada
  tres (siguen el plano del hombro: avanzan hacia el sur), y **recodos** de radio `RECODO` (3) con medias losas; tres de
  ancho, enlosado, despejado por encima, relleno por debajo y con pretil donde hay caida (faroles en postes de hueso).
  Acaba en `NUBES` (251, lo que se pisa en los caminos de nube firme) en una **plazoleta** (`pie()`), un poco mas alla del
  ultimo punto; ahi el hombro se corta. Por debajo de `CORTE` (232) la roca cuelga como una isla hasta `FONDO` (190).
  **Restos de torres** en los recodos (uno de cada dos y el ultimo). Arriba, el **mirador** (`RP` 9, enlosado, anillo de
  hueso, pretil) con el **arco de colmillos** al norte (`arco()`), de cara a la fortaleza. `distancia(x, z)`: lo lejos
  de la montana (la zona de la fortaleza la incluye: musica, lluvia, `lugar`).
- **El puente** (`fortaleza/Puente`, reescrito): del mirador (`z0` = mz - RP - 1, a la altura de la cima) al borde de la
  isla (`bordeZ + 1`, a Y) en medias losas; tablero de cinco (espinazo de hueso, piedra negra), barandillas con postes de
  hueso y calaveras, costillas de hueso por encima cada seis, farolillos colgados, viga y vertebras por debajo y nubes
  colgando. Se forma del mirador a la isla en ~10 s (`levantar` / `tick`; los muretes con la bandera 3 para que se unan).
  `enChunk` lo vuelve a poner en un chunk que se construya despues (`aplicarEstado`); `volcar` da sus bloques (vistas y
  pruebas); `enPuente` y `altura`.
- **La llave** (`LlaveHueso`): ante el arco del mirador (a 14 bloques) tiende el puente; si no, su rastro va a donde toque
  (`objetivo`): el pie de la montana, el arco (si se esta en la montana) o la reja (con el puente tendido y ya arriba).
- **Mundos de antes**: `FortalezaDatos.VERSION` = 3. Una de la 5a (version 2) se queda, pero su escalera de caracol (si
  se levanto) se quita pieza a pieza (`fortaleza/EscaleraVieja`, el codigo de la escalera de la 5a, solo para eso:
  `puenteViejo` con un 1 al final) y `puente` vuelve a falso. Una de antes de la 5a se rehace entera (como en la 5a).
- **Camino de nubes** (`CaminoNubes`): lo lejano sigue el trazado de siempre (hacia la escalera de la fortaleza de antes
  de la 5a, `centroPuenteAntiguo`: en un mundo con parte del camino hecho no se rompe) y desde alli `alPie` rodea la
  montana por el lado por el que llega (a 54 del centro) hasta su plazoleta, desde el sur; la cuenta rapida (`cerca`) usa
  la ruta de verdad a menos de 300 de la fortaleza. Las nubes del plano no tapan la linea del puente.
- El Espejo: "Monte del Colmillo" (`fortaleza_montana`, el pie), "Mirador del Colmillo" (`fortaleza_mirador`) y "Puente
  de Hueso" (`fortaleza_puente`). Comandos `/darkcraft fortaleza montana` (al pie) y `mirador` (ante el arco); `escalera`
  ya no existe.
- Sin abrir el juego: `tools/vista3d/Volcado.java montana` (la fortaleza con su montana en 0,0 y el puente tendido; con
  `-Dtodo=true` todos los chunks del plano): escribe donde caen la cima, el arco y el pie y **si se llega a pie** del pie
  al arco, por el puente a la isla y a la puerta (`Andar`).
- Pruebas: servidor, `montanaDeLaFortaleza` (linea `Montana de la fortaleza: ...`: levanta sus chunks en el mundo de
  nubes de verdad, sube a pie del pie al mirador y cruza el puente; luego lo quita); cliente, tramo `fortaleza()`
  (capturas `cliente_f05_montana_pie`, `f05_montana_recodo`, `f05_mirador`, el puente formandose `f06_puente_0/1`,
  `f06_puente_mirador` y `f07_puente_cruzar`; lineas `montana a pie: ...` y `puente a pie: ...`).

### Plan grande, parte 5c: trampas, secretos y enemigos de la fortaleza

Respuestas 37, 44, 46 y 47 del plan.
- **Trampas** (`fortaleza/Trampas`, en memoria por fortaleza; solo saltan con jugadores en supervivencia o aventura): el
  **suelo que cede** (`TorreCupula.sueloQueCede`, 3 x 3 de losas agrietadas en la planta 4, la de la ruina: al pisarlo
  cruje, a los 0,3 s se hunde y se cae a la planta de abajo; vuelve a los 30 s si no hay nadie en el hueco —`vuelve`, la
  prueba lo acorta—; una vez por sesion, con su chunk cargado, se repone si quedo hundido), los **pinchos de hueso**
  (`pinchos`: seis losas cinceladas en el pasillo norte del sotano y seis en el camino norte del jardin; al pisar una, un
  chasquido y a los 0,3 s `EfectosRaiz.estaca` con BONE_BLOCK en las seis —la sobrecarga con bloque es de la 5c—, 6 de
  dano y lentitud; 2,5 s entre una y otra) y las **flechas** del paso de la puerta (`flechasDisparo`, la fila de dentro de
  la reja; salen de las cuatro aspilleras de los lados, `flechasAspilleras`: flechas del juego que no se recogen, 3 de
  dano cada una; 4 s entre tandas). **Ojo con los relojes de las esperas: no empezar en `Long.MIN_VALUE`** (t - MIN_VALUE
  se da la vuelta y sale negativo: la trampa no saltaba nunca).
- **El muro falso** (`muroFalso`: el muro de la torre al oeste de la sala de guardia del sotano, 2 x 3 x 3 de la misma
  piedra, `bloqueMuro`): se deshace al golpearlo. En aventura el cliente no manda el clic de romper bloques: lo abre el
  ataque del combate (`Combate.pedir` llama a `Trampas.golpe`, que mira 4 bloques delante de los ojos); en creativo, el
  clic izquierdo (`PlayerInteractEvent.LeftClickBlock`, cancelado). Queda abierto para siempre (`FortalezaDatos.Estado.muro`,
  `aplicarEstado` lo quita en un chunk que se vuelva a levantar), logro `secreto` (oculto, "El muro que no es muro").
  Detras, **la sala secreta** (`salaSecreta`, x -18..-12 del centro, un paso de tres): el cofre `chests/fortaleza_secreto`
  (piedra de forja III, dos o tres II, un talisman, una ceniza, frutos y oro), oro, velas rojas y los huesos.
- **La cripta del vigia** (`cripta`, bajo la fuente rota): una trampilla de roble oscuro en la taza (`trampilla`, cerrada,
  mitad de arriba: parece suelo), escalera de mano y una salita con el **libro perdido 17** en un atril
  (`atrilEscondido`): "Confesion bajo la fuente", de Oswin, el ultimo vigia (cuenta de los vigias y donde esta el muro
  falso). `Perdidos.TOTAL` = 17 (logro `libros_perdidos`: diecisiete); en el codice lleva la miniatura de la fortaleza.
  Abierta, una trampilla sobre una escalera de mano que mira igual hace de escalera (asi se sube); `Andar` no lo sabe: en
  `Volcado` se cambia por una escalera para comprobar que se llega.
- **Las mazmorras se rehicieron** (en la 5a habia 11 celdas a las que se llegaba): pasillo norte-sur con celdas a los dos
  lados y dos salas de guardia alrededor del tubo (este, la de la escalera; oeste, la del muro falso) que unen las dos
  mitades.
- **Enemigos** (`fortaleza/FortalezaEnemigos`, sitio `fortaleza@x,z` de `Puestos`, 34 puestos): en el **Monte del
  Colmillo** un caballero a media cuesta de cada tramo (mirando cuesta abajo), dos perros dormidos en el ultimo tramo,
  arqueros en los recodos sin torre (tiran a lo largo del tramo de debajo), algo dentro de cada torre en ruinas y, en el
  **mirador**, un capitan de elite con dos alabardas ante el arco; dentro (solo con la fortaleza en pie, `completa`):
  alabardas al salir del paso de la puerta (a 16 del santuario), caballeros de ronda por el jardin, una ballesta, una dama
  velando, los **vigias** junto a la fuente, la sala de guardias y la armeria, una dama rezando en la capilla, espectros
  por los aposentos y el observatorio, un caballero en la ruina; en el sotano perros dormidos en la sala del muro falso,
  huesos en una celda y un carcelero. Ya no salen caballeros zombi al azar en la isla (`Caballeros.ZONAS` queda vacia).
  `comprobar(mundo, plan[, soloFuera])`: cada puesto se pisa y se llega (los de fuera desde el pie, los de dentro desde el
  jardin).
- **El vigia** (`Espectro.VIGIA` = 2, bestiario `espectro_vigia`, "Vigia de la cupula"): capucha de punta, habito negro y
  rojo con cordon, la capa y el estoque del cortesano, la cara en sombra y **ojos rojos** (`tools/enemigos/espectro.py`:
  piezas `capucha` y `cordon`, `OCULTAS[2]`; `espectro_2.png`); se dibuja algo rojizo en vez de azulado. Pelea como el
  cortesano (6 de golpe); **sin presa se apaga y aparece en otro sitio de su ronda** cada 6-13 s
  (`aparecerEnRonda`), peleando se desvanece mucho mas (tambien tras pegar, `irse`) y aparece siempre a la espalda.
- **Mundos de antes**: `FortalezaDatos.VERSION` = 4. Una de la 5b (version 3) vuelve a levantar los chunks de la isla y
  la torre (`nucleo`; la montana y las nubes no) con la puerta, el puente y el portal como estaban; una de la 5a pasa por
  la 3 y luego por esto; las de antes, enteras como siempre.
- Comandos `/darkcraft fortaleza secreto` (ante el muro falso), `muro` (lo deshace), `cripta` (junto a la trampilla),
  `trampas` (como estan), `enemigos [volver | fuera | sacar]`; `reiniciar` tambien vuelve a poner el muro.
- Sin abrir el juego: `tools/vista3d/Volcado.java montana` con `-Dtodo=true` escribe la linea `enemigos: ...`, las trampas,
  lo que se llega desde el jardin (sotano, muro, terraza) y con el muro deshecho y la trampilla abierta (cofre secreto,
  atril de la cripta); las marcas de los puestos (G caballero, A arquero, P perro, E espectro, C capitan).
- Pruebas: servidor, linea `Montana de la fortaleza, enemigos: ...`; cliente, tramo `secretos()` (capturas
  `cliente_f50_muro_falso`, `f51_sala_secreta`, `f52_trampilla`, `f53_cripta`, `f54_suelo_cede`, `f55_pinchos`,
  `f56_flechas`, `f57_jardin_enemigos`, `f58_vigia`, `f59_capitan_mirador`). En el resto del tramo de la fortaleza sus
  enemigos estan por caidos (las vistas de la 5a y la 5b van sin ellos).

### Plan grande, parte 5d: las paradas del camino de nubes

Respuesta 81 del plan ("largo, a pie, con paradas: islotes con ruinas, enemigos y algun cofre").
- **Seis paradas por camino** (`camino/Paradas`: `de(seed, reinoX, reinoZ)`, `colocar` con un camino cualquiera): repartidas
  por el camino de nubes del reino (`CaminoNubes.ruta`), sin ninguna en los primeros `DESDE` (220) ni en los ultimos `HASTA`
  (260) bloques; con un camino mas corto, menos (`tipos`: siempre la capilla y el campamento). Cada una es un islote de roca
  (`gen/Parada`, radio 14-19) centrado en el camino, con lo de arriba a la altura del camino (`Parada.Y` = 250, los pies en
  251): el camino lo cruza enlosado (`alCamino` < 1,6; nada se pone a menos de 2,4 por debajo de Y + 4) y lo de cada
  parada va a un lado (`lado`, alternando), en un marco girado a la direccion cardinal mas cercana (`Marco`: a a la
  derecha, b hacia el camino; `cimiento` rellena hasta la roca). Encima se despeja la nube y alrededor se baja (un claro).
  1. **Atalaya Rota**: torre redonda en ruinas con escalera de mano a la plataforma (el arquero) y el cofre dentro.
  2. **Capilla del Peregrino**: la nave sin techo, el altar y los bancos rotos; delante, un **santuario** (sin enemigos).
  3. **La Caravana Perdida**: un carro con toldo, otro volcado, la carga, los huesos del caballo y el cofre.
  4. **Puerta de las Nubes**: un arco apuntado sobre el camino, dos estatuas y la muralla rota a los lados.
  5. **Cementerio de las Nubes**: tumbas en filas, verja, un arbol seco y una cripta con escalones (el cofre abajo).
  6. **Campamento de la Guardia**: tiendas, la hoguera, yunque y estandartes; al llegar, el ultimo **santuario**.
- **No se generan con el mundo**: se levantan chunk a chunk (uno por tick, con descanso si tarda) la primera vez que alguien
  pasa a menos de `CERCA` (170) de una (`onPlayerTick` cada 2 s → `encolar` → `construirUno`); SavedData `darkcraft_paradas`
  (chunks hechos y completas). Los santuarios los pone `Santuarios.ponerEn` al hacerse su chunk, con su nombre
  (`darkcraft.santuario.parada_capilla` / `_campamento`). Cofres con `chests/camino` (frutos, flechas, oro, piedras de
  forja I y a veces II; raro, un hechizo, una ceniza o un talisman).
- **Enemigos** (`Paradas.puestos`, sitio `camino@x,z` del reino, grupo `nombre:k`: salen cuando su parada esta en pie):
  atalaya (el arquero arriba, una ballesta en la puerta y dos caballeros de ronda), caravana (tres perros y dos huecos),
  arco (dos alabardas bajo el arco, un caballero y una ballesta), cementerio (una dama velando, un cortesano, dos perros y
  un hueco tumbados), campamento (el capitan de elite, dos caballeros, una ballesta y un perro, a mas de 14 del santuario);
  la capilla, nadie. Todos miran hacia donde se llega desde Valdrenn.
- El Espejo: el nombre de cada parada (`Paradas.lugar`, antes que "Camino de nubes"; `darkcraft.mapa.lugar.parada_*`).
- Comandos `/darkcraft camino` (las paradas del camino del reino de alrededor), `ir <0-5>`, `levantar <0-5>`,
  `enemigos volver | fuera`.
- Sin abrir el juego: `tools/vista3d/Volcado.java camino` (las seis en un camino de mentira de lana blanca;
  `-Dparada=N` solo una): por cada una `Paradas.comprobar` (se cruza por el camino, se llega al cofre y al santuario, cada
  puesto se pisa, ninguno a menos de 14 del santuario) y marcas de los puestos, cofres y santuarios.
- Pruebas: servidor, `paradasDelCamino` (lineas `Camino de nubes, paradas: ...` y una por parada: las levanta en el mundo
  de nubes de verdad y las comprueba); cliente, tramo `camino()` (capturas `cliente_cn01_atalaya` ... `cn06_campamento`,
  lineas `en la parada N: lugar ...`).

### Plan grande, parte 5e-1: el portal nuevo (marco, remolino, pilares, estatuas y pedestal)

Respuestas 48-56 del plan. En la terraza de lo alto de la torre (`TorreCupula.terraza` → `marcoPortal`, `pilarPortal`,
`estatua`, `pedestal`):
- **Bloques nuevos** (irrompibles, sin objeto; texturas de `tools/fortaleza/portal.py`): `piedra_sombra` (tres tallas al
  azar por sitio, una lista de modelos en el blockstate; encima, sus runas rojas animadas en otro elemento del mismo cubo
  con `forge_data` `block_light`/`sky_light` 15 y `shade: false`: brillan de noche), `hueso_labrado` (columna, como el
  bloque de hueso), `hierro_negro` y `gema_roja` (`GemaBlock`, propiedad `luz`: apagada luz 1, encendida 11 con su brillo
  propio). En el mundo de mentira de las vistas no hay bloques del mod: `TorreCupula.piedraSombra()` etc. devuelven
  obsidiana, hueso, netherita y redstone/luz de hongo.
- **El marco**: arco apuntado de 9 x 13 (`arcoPortal`), tres de hondo; anillos por distancia a lo ajedrez al hueco
  (`anillo(dx, h)`): 1 hueso labrado (de pie en los lados, tumbado arriba), 2 piedra de sombra; zocalo de hierro negro,
  remate con punta de hueso; gemas en los lados (filas 2, 5 y 8) y la clave, solo en las caras de fuera.
- **Pilares** a `cx +- PILAR` (10) en el plano del portal: fuste en cruz de piedra de sombra con bandas de hierro, capitel de
  hierro, punta de hueso con gema; **cadenas** (filas 3, 7 y 10) del marco al pilar.
- **Estatuas encapuchadas** de 16 de alto en `cx +- EX, cz + EZ` (9, 7), mirando al norte (al portal): manto de pizarra,
  capucha de piedra negra con la cara oscura y dos gemas por ojos, manos sobre una espada de hueso con una gema en el pomo.
- **Pedestal** en `altar()`: zocalo de hierro negro, columna de hueso con contrafuertes y el hueco arriba (losas); dos
  braseros a los lados.
- **Al abrirse** (`TorreCupula.alAbrir()`: lista de `Cambio` con el bloque de antes, el de despues y la fase): se encienden
  las gemas del marco de abajo arriba, luego los ojos y pomos de las estatuas, saltan las cadenas, revientan lo alto de los
  pilares (queda un munon) y caen cascotes a la terraza (`FortalezaEventos.abrirPortal`, con sonidos y particulas).
  `aplicarEstado` lo deja asi en un chunk que se vuelve a levantar; `cerrarPortal` lo deshace (comando reiniciar).
- **El remolino** (`fortaleza/Remolino`, entidad sin guardar ni cuerpo en el pie del hueco): el cliente dibuja todo el
  hueco fila a fila con `textures/entity/remolino.png` girando hacia el centro en dos capas (`RemolinoRenderer`,
  `entityTranslucentEmissive`, luz plena); al abrirse crece desde el ojo (`abierto` 0 → 1 en 2 s). El bloque
  `portal_sombra` ya no se dibuja (`RenderShape.INVISIBLE`): pone el roce, el empujon y las chispas.
- **El amuleto** se queda en su pedestal: un `item_display` (etiqueta `darkcraft_amuleto_altar`, efimero) que flota y gira
  (un octavo de vuelta cada medio segundo, `girarAmuletos`). `asegurarPortal` (cada segundo con alguien en la zona) pone el
  remolino y el amuleto si faltan.
- **Version 5** de `FortalezaDatos`: de la 4 se rehacen solo los chunks de la terraza (`FortalezaPlan.terraza()`); de la 3,
  todo el nucleo (como antes).
- Pruebas: cliente, `cliente_f40a_portal_noche` (con la linea `portal abierto: hueco N/N; gemas ... remolino 1 ... amuleto
  en el pedestal 1`, de `FortalezaEventos.resumenPortal`) y `cliente_f40b_estatuas`. Sin abrir el juego: `Volcado torre`
  (`-Dabierto=true`: abierto) y `montana -Dtodo=true` (se llega ante el pedestal, ante el portal y al atril de la terraza).

### Plan grande, parte 5e-2: el capitulo 6 con la voz de la Sombra, el rayo rojo y el cielo rojo

Respuestas 57-67 del plan.
- **La voz**: el capitulo 6 lo dice la Sombra (ElevenLabs, la voz de la parte 3d, flow `AqkIj7OE07LCh5cMIoZU`): frases
  10-20 de `voz_sombra` (tomas a/b y su susurro), texto en `ci/historia/perdidos.py` `NARRACION6` (11 frases, en ingles
  en el audio y subtitulos en cada idioma; el libro del capitulo, en 4 paginas). `ci/eleven/procesar.py` `capitulo6()`
  monta cada frase con `voz_sombra` (susurros y eco de vacio) y las junta con sus pausas (`CAPITULO6`, la de tras "mira
  arriba" es la del rayo) en `sounds/narracion/cap6.ogg` (estereo) y `cinematica/cap6.json` (como los de Aldous: asi la
  cinematica corta planos y pone subtitulos igual). Piper ya no hace el capitulo 6 (no hay `ci/narracion/cap6.txt`).
- **La cinematica** (~80 s, `CineShots.chapter6`, grupos `Timeline.GROUPS6`): la fortaleza de fuera, el jardin bajo la
  cupula, la terraza (se abre el portal, `Timeline.abrePortal6`), el cielo sobre el portal desde la terraza (las nubes se
  arremolinan, `nubes6`; cae el rayo, `rayo6`), la fortaleza de lejos con la columna y el cielo enrojeciendo, el remolino
  de cerca y el jugador. **La ven todos los jugadores** (`Cinematics.playCapitulo6(..., todos)`): a los que estan a mas
  de 100 bloques se les lleva de plano en plano sin verse (como en el capitulo 1) y vuelven a su sitio; su ultimo plano
  es el amuleto en el pedestal.
- **Las nubes y el rayo** (`FortalezaEventos.nubes` / `rayo`; el `Remolino` lleva `tormenta` y `rayo`, el tick del mundo):
  `RemolinoRenderer` dibuja un disco de nubes en espiral 72 bloques por encima del hueco (`entity/nubes_remolino.png`)
  que crece, gira y desde el rayo se enrojece (se deshace a los 24 s); el rayo es un zigzag con ramas en tres capas
  (`RenderType.lightning`) que parpadea 1,5 s, con el destello del cielo. Sonidos para cada uno de los que la ven:
  `sfx.cielo_remolino`, `sfx.rayo_rojo` (+ trueno), `sfx.columna_roja` y `sfx.cielo_rojo` (ElevenLabs, flow de efectos).
  Si el portal se abrio antes (o se corto la cinematica), el rayo cae al llegar alguien (`asegurarPortal`, `Estado.rayo`).
- **El cielo rojo** (`fortaleza/CieloRojo`, SavedData `darkcraft_cielo_rojo`: activo, desde, columnas, vencida; se
  manda al entrar con `Net.CieloRojo`): en el cliente (`client/fortaleza/CieloRojoCliente`) en 8 s: la niebla roja
  (`ComputeFogColor`), un velo rojo sobre el cielo (`AFTER_SKY`, una caja sin profundidad alrededor de la camara), la luz
  del cielo tira a rojo (`ClientNubes.Efectos.adjustLightmapColors`), las nubes de Minecraft rojas (`renderClouds`: se
  dibujan con otro color y se deja como estaba), las del mar de nubes rojas (los modelos de `nube` llevan `tintindex` y un
  `BlockColor`; al cambiar se rehacen los chunks), la luna de sangre (`environment/luna_sangre.png`, encima de la luna y
  mas grande, de noche) y la columna de luz roja de cada portal (el haz de un faro, `BeaconRenderer.renderBeaconBeam`,
  1024 de alto, se ve de lejos). Dura hasta vencer a la Sombra (`CieloRojo.apagar(srv, true)`, parte 7); el comando
  reiniciar de la fortaleza lo apaga.
- Desde la parte 7a el portal lleva a Morvain (si la dimension no existiera, empuja como antes con "aun no se han alzado").
- Pruebas: servidor, linea `Capitulo 6 (la Sombra): audio true, 11 frases ...`; cliente, capturas
  `cliente_f301_capitulo6_titulo` ... `f310_..._final` (las gemas, los pilares, las nubes, el rayo, el cielo rojo),
  `cliente_f40c_luna_sangre`, `cliente_f40d_cielo_rojo` y las lineas `tras el rayo: cielo rojo SI ... columnas 1`.

### Plan grande, parte 6: Distant Horizons, Oculus y Embeddium dentro del mod

Respuestas 71, 72, 82-84, 131, 134 y 135 del plan.
- **Dentro del jar** (`ci/dentro/meter.py`, paso de la CI justo despues de compilar): baja de Modrinth (Forge, 1.20.1) las
  versiones fijadas en `ci/dentro/mods.json` (Distant Horizons 3.3.2-1.20.1, Oculus 1.20.1-1.8.0, Embeddium
  0.3.31+mc1.20.1; si una ya no esta, la mas nueva con el mismo principio, avisando), comprueba su sha512 y las mete SIN
  CAMBIOS en `META-INF/jarjar/` (guardadas sin comprimir) con `META-INF/jarjar/metadata.json` (el jar-in-jar de Forge:
  `identifier` maven.modrinth:<proyecto>, `range` [version] exacta, `isObfuscated` false). Forge las carga como mods
  normales (tambien los jars que lleva Oculus dentro). Licencias (`ci/dentro/LGPL-3.0.txt` y `GPL-3.0.txt`, las de
  Debian) y `CREDITOS-MODS.txt` (version, sha512, codigo fuente) en `META-INF/darkcraft/`; los creditos van tambien en la
  pagina de cada release publicada (`out/notas.md`), en el README y en la descripcion del mod (`gradle.properties`).
  `out/mods_dentro.txt` dice lo que pide cada uno (Forge, mixins).
- **El shader: Complementary Shaders - Unbound** (EminGT). Su licencia (Complementary License Agreement 1.8, punto 1.2.d)
  NO deja subirlo dentro del jar: se anade "con el sistema de Modrinth". `meter.py` lo baja a `out/shaderpacks/` (solo
  para la prueba del cliente; no se sube a la release) y escribe `out/shader.json`; el **instalador** lo baja a
  `.minecraft/shaderpacks/` y el **modpack** (`ci/make_mrpack.py`) lo lleva como un fichero mas de Modrinth. Tampoco se le
  cambian opciones (la licencia prohibe hasta "simple variable changes").
- **El instalador lo escribe la CI** (`ci/instalador.py` → `out/instalar_mods.bat`, CRLF, subido a cada release): los mods
  de decoracion que ha probado la CI (`srv/mrpack_files.json`), Dark Craft (de la ultima release) y el shader; avisa de
  subir la memoria de TLauncher a 8 GB. `instalar_mods.bat` del repositorio es una copia de uno de esos.
- **El arranque** (`com.darkcraft.arranque`): `darkcraft.mixins.json` (en el manifiesto del jar, `MixinConfigs`) solo
  tiene su plugin `Arranque`, que Mixin carga antes que el juego. En el cliente, `PorDefecto.aplicar` pone UNA vez por
  instalacion (lo apunta en `config/darkcraft-arranque.properties`: `lejania=<version>`, `shaders=1`): en
  `config/DistantHorizons.toml` la lejania a 256 (`[client.advanced.graphics.quality] lodChunkRenderDistanceRadius`), sin
  actualizador (`[client.advanced.autoUpdater] enableAutoUpdater = false`: DH no puede actualizarse dentro de otro jar)
  y con las nubes en la lejania (`[client.advanced.graphics.culling] blocksToIgnore = "NONE"`: con NON_COLLIDING, el de DH,
  el mar de nubes —que no tiene choque— no salia); si hay un Complementary en `shaderpacks/`, en `config/oculus.properties`
  `shaderPack` ese y `enableShaders=true`. Tiene que ser asi de pronto: **Oculus lee su configuracion al crear las
  opciones del juego (`Options.load`), antes de construir los mods**. `DH_VERSION` sube si cambia lo de DH (se vuelve a
  poner una vez). En un servidor dedicado, `PorDefecto.servidor` solo quita el actualizador de DH.
- **Servidor dedicado** (respuesta 84): Oculus es solo del cliente y su clase de mod (`net.irisshaders.iris.Iris`) no
  carga en un servidor (toca pantallas y teclas que no existen: "invalid dist DEDICATED_SERVER"). El mixin vacio
  `arranque/mixin/OculusEnServidor` (solo en `"server"`, `@Pseudo`) hace que Mixin pase esa clase por
  `Arranque.preApply`, que le quita todos los metodos salvo `<clinit>` y un constructor vacio: Forge la construye y
  Oculus no hace nada. Embeddium y DH van solos en un servidor (DH genera alli la lejania para los jugadores).
- **En el cliente** (`client/Lejania`, por reflexion: el mod va igual sin ellos): la API de DH (`DhApi.Delayed.configs`:
  `graphics().chunkRenderDistance()`, `renderingEnabled()`, `renderingMode()`, `worldGenerator().enableDistantWorldGeneration()`;
  `setValue` pone un valor "de la API" que manda en la sesion sin guardarse, `clearValue` lo quita; **ojo: poner
  `renderingEnabled` cambia tambien `renderingMode`, y quitarlo despues no lo devuelve**) y la de Iris
  (`IrisApi.getInstance().getConfig().setShadersEnabledAndApply`, `isShaderPackInUse`). Las opciones del mod abren la
  pantalla de Embeddium en Graficos, la de Oculus en Shaders y la de DH en Lejania (`ConfigScreenHandler`). El titulo
  avisa si el lanzador da menos de 5,5 GB (`darkcraft.titulo.memoria`).
- **Con shaders** lo dibujado a mano con `BufferUploader` en el cielo salia roto (triangulos rosas): el velo rojo y la luna
  de sangre (`CieloRojoCliente.cielo`) no se dibujan si `Lejania.shadersEnUso()`; el cielo rojo queda en la niebla (el
  pack la usa para sus nubes de noche), el mar de nubes tenido y las columnas. Lo que va por `RenderType` (entidades,
  `Tipos.BRILLO` de los avisos del suelo, el remolino) se dibuja con el programa del pack.
- **La prueba del cliente es el juego de verdad** (`ci/cliente/lanzar.sh`): portablemc 5.0.6 (en un venv) instala Forge
  47.3.0 (cache `~/.pmc`) y lanza el jar de la release con DH, Oculus y Embeddium; `-Ddarkcraft.ciClient=true`, 5 GB.
  Si portablemc falla o el juego no entra en el mundo en 14 min, se corta y se repite con `gradle runClient` (sin esos
  mods; `out/cliente_modo.txt` dice cual fue). Por eso en `ClientCiTest` nada de reflexion con nombres de desarrollo
  (abrir el mundo y las capturas van directas; el titulo de la pantalla se busca por el orden de los campos). Al empezar
  apunta lo que dejo el arranque y apaga shaders y lejania (con el dibujo por software irian lentisimos); el tramo
  `lejania()` del final los enciende: `cliente_l01..03` lejania sin shaders (sin el cielo rojo), `l04..l09` con shaders
  (Brumavela, la fortaleza, Valdrenn, el portal con el cielo rojo, el jardin y una vitrina: santuario, enemigos y avisos
  de jefe). Se suben tambien `cliente_config_*.txt` (las configuraciones de DH y Oculus), `crash_*.txt` y los logs.
- Las capturas de los demas tramos salen iguales que en desarrollo (Embeddium dibuja los bloques, los brillos y las
  runas igual).

### Plan grande, parte 7a: Morvain se alza (la dimension, el Arco del Umbral y la ciudadela encadenada)

Respuestas 85-91 del plan (publicada con la build de prueba 130 bien: la llegada, las regiones con Distant Horizons solo
con chunks de verdad, la ciudadela y la vuelta). Morvain va por sub-partes: **7a** la dimension entera (terreno de todas
las regiones, cruzar y volver, la llegada y la ciudadela del cielo); despues la montura, los santuarios con teletransporte y los fragmentos de
mapa (respuestas 92 y 129), y region a region su contenido (ruinas, mazmorras, enemigos, jefes: 125, 131, 132).
- **La dimension** `darkcraft:morvain` (`data/darkcraft/dimension*/morvain.json`): de 0 a 767 (`Morvain.ALTO`), hora fija
  12500 (el sol a punto de ponerse: cielo oscuro con un poco de dorado y rojo, respuesta 91), sin camas. Generador
  `darkcraft:morvain` (`morvain/MorvainGenerador`) y fuente de biomas `darkcraft:morvain` (`MorvainBiomas`), registrados
  en `Morvain.registrar`. **El mapa es el mismo para todos y no depende de la semilla** (`morvain/Terreno`, solo Java y
  sin estado: lo usan el generador, Distant Horizons desde sus hilos y la vista sin juego): 16.000 x 16.000
  (`MITAD` 8000) con un continente de costa irregular y bahias rodeado del Mar Muerto; regiones por semillas con las
  fronteras torcidas y pesos suaves (`temple`: la meseta de la academia casi de golpe, acantilados): al sur las Llanuras
  Palidas (la llegada), al suroeste el Bosque Muerto, al oeste el Pantano Podrido, al norte de lado a lado las Montanas
  Nevadas (crestas de 550-650 y cuatro colosos de mas de 700, `COLOSOS`), al noreste la Academia Espectral en su meseta,
  al este la Capital en ruinas (aun sin ruinas), al sureste la Costa de los Huesos (costillas de gigante), alrededor del
  centro la Tierra sin Fe (grietas que bajan hasta la sangre) y en el centro el Crater de Sangre con el lago y el peñon de
  la Sombra (hasta Y 340). Tres rios (`RIOS`) cavan hasta el agua (63). El suelo de cada region: `Suelo`; lo de encima
  (arboles muertos, rocas, setas, nenufares, costillas): `Adorno`, dentro del propio chunk. El agua es roja en la Tierra
  sin Fe y el crater por el color de su bioma.
- **Los biomas** (`worldgen/biome/morvain_<region>.json`, `Region`): cielo, niebla (dorada en las llanuras, fria en las
  montanas, verdosa en el pantano, violeta en la academia, roja en la Tierra sin Fe y el crater), agua, hierba, particulas
  (ceniza, copos, esporas...) y sin bichos ni lluvia. `MorvainBiomas` guarda por hilo la region de las ultimas 64 columnas
  (el juego pide el bioma 192 veces por columna). Nombres: `biome.darkcraft.morvain_*` y `darkcraft.zona.morvain.*`.
- **El cielo** (`client/morvain/MorvainCliente`, efectos `darkcraft:morvain`): sin nubes de Minecraft, el resplandor del
  ocaso dorado con algo de rojo (`getSunriseColor`) y nunca llueve (el tiempo de las dimensiones es el del mundo de las
  nubes: el cliente le quita la lluvia). El cielo rojo del portal es solo del mundo de las nubes.
- **Cruzar** (`Morvain.portal`, desde `FortalezaEventos.tocarPortal`; los que no son jugadores siguen saliendo
  despedidos): al entrar en el hueco del portal de la fortaleza se apunta la vuelta en el jugador (`darkcraft_vuelta`:
  dimension, delante del portal del lado por el que entro, de espaldas a el) y se le lleva al **Arco del Umbral**
  (logro `morvain`, hazana, con su entrada del diario; cartel grande "Morvain, las Tierras Marchitas"). El portal del
  arco devuelve alli. 3 s sin volver a cruzar (`ESPERA`).
- **El Arco del Umbral** (`morvain/Llegada`, en (0, 3400) a la altura del terreno; se escribe al generar): plaza
  redonda de piedra vieja (radio 15, se funde con el terreno hasta 27) con seis pilares rotos, el santuario en el centro
  ("Arco del Umbral"), al sur el arco del portal de vuelta (el mismo arco apuntado de la fortaleza, `TorreCupula.anillo`,
  con las gemas encendidas y bloques `portal_sombra`), dos farolas de almas y un camino de tierra hacia el norte (120).
  Se llega a 10 al norte del santuario mirando al norte (`Llegada.pies`). La primera vez que llega alguien
  (`Morvain.preparar`): el nombre del santuario (`Santuarios.ponerEn`, `morvain:llegada`) y Maelis sentada al lado
  (una vez, SavedData `darkcraft_morvain`); el remolino del portal (no se guarda) lo pone `Morvain.onTick` con alguien a
  160. `Adorno` no pone nada en sus chunks.
- **La ciudadela encadenada** (`morvain/Ciudadela`, respuesta 88; se escribe al generar, la ve Distant Horizons): una isla
  de roca negra (radio ~102, el patio en Y 540, cono colgante con picos hasta ~Y 365) sobre el peñon de la Sombra, con
  muralla octogonal (apotema 78, 24 de alto, almenas), ocho torres de chapitel con troneras rojas, la torre del homenaje
  (52 de lado con las esquinas cortadas, 84 de alto, contrafuertes escalonados, ventanas rojas y gemas encendidas, suelos
  por dentro), cuatro torreones con chapitel y la aguja del centro (gema en la punta en Y 719, `Vista.cima`). **Ocho
  cadenas** de eslabones de hierro negro (41 x 21, grueso 2,5, uno tumbado y otro de pie) de la isla a sus anclas
  (piramides escalonadas con puas) a 860 del centro, en la ladera de dentro del crater. De momento no se entra.
- **Morir en Morvain** sin haber descansado en un santuario de alli: se reaparece en el arco (`Morvain.onMuerte` pone ese
  punto y guarda el de antes, que se devuelve al volver por el portal si sigue siendo el del arco).
- **El Espejo**: `Morvain.lugar` (el arco, "El Arco del Umbral", o la region: cartel grande la primera vez de cada una).
- Sin abrir el juego: `java -cp /tmp/mv Mapa salida.png [paso] [x0 z0 lado]` (`tools/morvain/Mapa.java`: el mapa entero
  por regiones con relieve y curvas de nivel; compilar `Ruido`, `Region`, `Terreno` y `Mapa`) y
  `tools/vista3d/vista.sh morvain_ciudadela|morvain_cadena|morvain_llegada carpeta` (`morvain/Vista`: el terreno con su
  suelo, el arco y la ciudadela, chunk a chunk como el generador; en las vistas los bloques del mod salen como bloques de
  Minecraft).
- Pruebas: servidor, `morvain()` (lineas `Morvain: ...` —alto, generador, hora—, `Morvain, regiones: ...` —una muestra de
  cada una con su bioma, su suelo y lo que tarda el chunk—, `Morvain, el Arco del Umbral: ...` y `Morvain, la ciudadela:
  ...`; vistas `morvain_llegada.png`, `morvain_ciudadela.png` y `morvain_ciudadela_alzado.png`); cliente, tramo
  `morvain()` despues de la lejania (con Distant Horizons): entra en el portal de la fortaleza, la llegada
  (`cliente_mv01`, `mv02` con el cartel), el arco, la plaza, cada region y la ciudadela (`mv03..mv14`), con shaders
  (`mv15..mv17`) y la vuelta por el arco (`mv18`). `LIMIT` 56 min (`lanzar.sh` 62 min, el paso 90, el trabajo 130).

### Plan grande, parte 7b: el corcel de hueso y niebla (la montura de Morvain)

Respuesta 129 del plan ("corcel de hueso y niebla; la guia te da el silbato en el primer santuario de Morvain; corre,
doble salto, se ataca montado, si lo hieren mucho se deshace un rato"). Publicada con la build de prueba 131 bien.
- **El corcel** (`montura/Corcel`, entidad `darkcraft:corcel`, `MobCategory.MISC`, 1,3 x 1,6, no se guarda con el mundo):
  modelo de `tools/enemigos/corcel.py` (caballo de hueso palido con las costillas al aire, silla negra con remaches de oro
  y manta roja; la crin, la cola, los ojos y el fuego de los cascos van en la capa de brillo; patas sin cinematica
  inversa, con los nombres de las del perro) → `CorcelGeometria`, `CorcelAnimaciones` (bucles QUIETO, ANDAR, CORRER y
  SALTO; acciones ENCABRITARSE y DESHACERSE) y `enemigos/CorcelAnims`. Lo dibuja `client/montura/CorcelRenderer` con
  `CorcelModelo` (parado, quieto; andando, el paso y el galope mezclados segun lo que corre, cada ciclo al ritmo de lo que
  avanza —`fasePaso`, `faseGalope`: el `walkAnimation` del juego se satura a 0,25 bloques por tick y no distingue el trote
  del galope—; en el aire, el salto; el brillo se apaga al deshacerse). Vida 40, armadura 4, sin dano al caer, inmune al
  fuego, no se empuja, vadea el agua poco honda como si fuera tierra (`isAffectedByFluids` solo con mas de 1,1 de agua).
- **Montado** (como un caballo: lo mueve el cliente del jinete): mira a donde mira el jinete (`tickRidden`), al trote
  (velocidad 0,24, unos 10 bloques por segundo) y con la tecla de correr al galope (x1,75: en la build 131 iba a 0,89
  bloques por tick, unos 18 por segundo; `Corcel.galopeCliente`,
  que pone `client/montura/ClientCorcel`); hacia atras despacio. **Salto y doble salto**: la tecla de saltar (flanco, en
  `ClientCorcel`) pide el salto y el corcel lo hace en su tick: en el suelo 0,62 (unos 2,6 bloques), en el aire otro de
  0,70 (hasta unos 5,6 en total; si cae de un borde, solo el segundo); el cliente avisa al servidor (`Net.CorcelSalto`)
  para el sonido y la niebla. Escalones de 1,1. El jinete va sentado en `ASIENTO` 1,30 (con el -0,35 del jugador, los
  muslos sobre la silla) y 0,14 adelante; se baja por un lado (Shift) como de un caballo.
- **Se pelea montado** (`ClientCombate.pelea` = suelto o montado en el corcel): los golpes y los hechizos van igual (al
  galope sale el golpe de correr; nunca el del salto) y el corcel no frena mientras se pega; no se rueda. Los golpes del
  combate no le tocan (`Combos.valido`) ni le hace dano su dueno.
- **El silbato** (`montura/SilbatoCorcel`, objeto `darkcraft:silbato_corcel`, objeto clave; arte de
  `tools/montura/silbato.py`, 32 x 32: hueso curvo con agujeros, cuero negro, cordon rojo y una voluta de niebla) y la tecla
  **H** (`Teclas.CORCEL` → `Net.CorcelLlamar`) llaman al corcel (`montura/Montura.llamar`): solo en Morvain, con el
  silbato encima (en creativo no hace falta) y donde quepa (hasta 2 bloques al lado, sin agua honda); aparece debajo de
  quien silba ya montado (a la fuerza: justo despues de bajarse el juego no deja montar en 3 s), con el silbido, la niebla y
  su relincho; montado, la H lo despide. Cada jugador tiene uno (`Montura.ACTIVOS`): si tenia otro, se va. Se monta tambien
  con clic derecho (solo su dueno). Avisos cortos `darkcraft.corcel.*` (los avisos llevan ya un numero: `Cartel.AVISO` con
  `n` > 0 lo pasa a la clave).
- **Se deshace en niebla** (`Corcel.deshacer`, su animacion de 32 ticks, sin morir: `die` no llega a morir) si se queda
  solo 30 s, si su dueno se aleja 64 bloques, se va de la dimension o muere, si entra en agua honda o si lo hieren mucho.
  **La vida** se apunta en la ficha (`darkcraft_rol` → `corcel`: vida, cuando, vuelve) y se recupera sola (una cada 2 s
  sin el, una cada 5 s con el). Herido de muerte no vuelve hasta pasado `Montura.VUELTA` (60 s, con media vida; aviso con
  lo que falta). Descansar en un santuario lo deja entero y sin espera (`Montura.descanso`, desde `Santuarios.descanso`).
  **Los golpes al jinete** se reparten (`Montura.onHerido`, lo que tiene quien pega y las explosiones): el jinete se lleva
  el 65 % y el corcel pierde el 60 % del golpe (`Corcel.recibir`); lo que le da a el (flechas...), entero. Al irse del
  servidor montado se va antes de guardarse el jugador. Por el portal: el corcel se deshace y su jinete cruza.
- **El HUD**: montado, la barra de vida del corcel bajo la estamina (herradura de hueso con fuego de almas y relleno de
  niebla: `HudTex.CORCEL` / `CORCEL_ICONO`, de `tools/ui/hud.py`), con su rastro del dano; las barras de montura de
  Minecraft no salen.
- **Maelis en Morvain** (`Dialogos.GUIA`, `Conversaciones.inicioGuia`, marca `GUIA_MORVAIN` = 8): la primera vez que se
  habla con ella alli, 11 (bienvenida) → 12 (el silbato; al acabarla `Accion.SILBATO` → `Conversaciones.darSilbato`: el
  silbato a la bolsa, aviso de la H y logro `silbato`) → 13 (como es el corcel) → menu; si lo ha perdido, 12 → 13 otra vez;
  despues, 14 ("los fuegos de Morvain son pocos"). Voz (la de siempre, "Autumn Veil", flow `FNZWSYJq2nK8zd5QwV5m`):
  frases 11-14, tomas a.
- Logros `silbato` (de Maelis) y `corcel` (llamarlo la primera vez), con su entrada del diario.
- Sonidos (ElevenLabs, `TOMAS_SFX`): `corcel_llamada` (4 y 2), `corcel_aparece` (2), `corcel_relincho` (2 y 3),
  `corcel_paso` (las cuatro; un casco cada 1,6 de `moveDist`, mas fuerte al galope), `corcel_salto` (2 y 4),
  `corcel_deshace` (2 y 3), `corcel_dolor` (3, 1 y 4).
- Comandos `/darkcraft corcel [llamar | silbato | deshacer | herir <n> | espera]` (sin mas: como esta).
- Pruebas: servidor, linea `Morvain, el corcel: ...` (atributos; sin dano al caer; herido de muerte no muere, se deshace);
  cliente, tramo `corcel()` dentro del de Morvain, en el Arco del Umbral: Maelis y el silbato (`cliente_mv20..22`), la H
  (`mv23` en tercera persona con su barra, `mv24` de lado), al galope (`mv25`), el doble salto (`mv26`, linea `el salto
  doble subio ...`), un golpe montado (`mv27`), deshacerse (`mv28`), el aviso de la espera (`mv29`), encabritarse
  esperando (`mv30`), montar con clic derecho y despedirlo con la H (lineas `corcel, ...` con `Montura.resumen`).

### Lo anterior: parte 5 (torre nueva, camino de nubes y portal) — por sub-partes

Respuestas 3, 4, 27-68 y 81 de `PLAN.md`:
- **5a, la torre bajo la cupula**: hecha (arriba).
- **5b, la montana y el puente largo**: hecha (arriba). Los enemigos del sendero van con los de dentro (5c).
- **5c, dentro de la fortaleza y en la montana**: hecha (arriba).
- **5d, el camino de nubes**: hecha (arriba).
- **5e, el portal** (48-68): hecha (arriba: 5e-1 el portal nuevo; 5e-2 el capitulo 6 con la voz de la Sombra, el rayo
  rojo y el cielo rojo). Cruzar el portal llega con Morvain (parte 7).
- Despues, la **parte 6** (arriba) y la **parte 7**, Morvain, las Tierras Marchitas (respuestas 85-96 y 125-132 de
  `PLAN.md`), por sub-partes (la 7a y la 7b arriba; despues, los santuarios con teletransporte y los fragmentos de mapa).

### Lo anterior: parte 3 (mundo de nubes, isla del pueblo y barco) — por sub-partes

Respuestas 69-80 y 121-124 de `PLAN.md`. Idea de como partirla (cada una publicada):
- **3a, el mundo de nubes**: hecha (arriba).
- **3b, la isla del pueblo abandonado**: hecha (arriba).
- **3c, enemigos**: hecha (arriba).
- **3d-1, la cinematica de inicio**: hecha (arriba).
- **3d-2, el barco**: hecha (arriba). Con esto la parte 3 esta entera; lo siguiente es la **parte 4** de `PLAN.md`
  (Valdrenn dificil: 4a la guardia, 4b perros y espectros, 4c capitanes de elite, emboscadas y la torre del arbol por
  dentro, y 4d los dos guardianes, hechas arriba). Despues, la **parte 5** de `PLAN.md`.

## Compilar, probar y publicar

- La CI (`.github/workflows/build.yml`) compila, prueba un servidor real (con y sin mods) y un cliente con capturas
  y audio grabado, y sube una release `build-N`. Con `[publicar]` en el mensaje del commit es la *latest* (la que
  instala el usuario); sin él es "en desarrollo". Un commit vacío no lanza la CI (por los `paths-ignore` del
  workflow): para publicar tiene que cambiar algún fichero.
- Compilar en local (solo para ver errores, ~4 s): bajar la release `classpath` (asset `classpath.tgz`) a
  `/tmp/cp/cpjars` y lanzar `javac` sobre `src/main/java` con esos jars (`[cp]` en un commit la vuelve a subir).
- Bajar assets de releases: `gh api -H "Accept: application/octet-stream" repos/ferranxd7/ciudadela-gotica/releases/assets/<id>`.
  Desde la build 73 las capturas van en `capturas.zip` y los audios sueltos en `audios.zip` (subir ~600 ficheros
  sueltos chocaba con el limite secundario de GitHub y la release se quedaba a medias, en borrador).
  Las rutas REST de Actions (artifacts, secrets) no pasan por el proxy; `gh run list` sí funciona.
- Commits: `git -c user.name="Claude" -c user.email="noreply@anthropic.com" commit`.
- Si una build falla por la red (maven), `gh run rerun ID --failed` la repite con el mismo numero: el paso "Subir
  release" borra antes la release `build-N` que dejo el intento fallido (desde esta sesion no se pueden borrar
  releases a mano: da 403). Un reintento usa el workflow del commit original: para builds de antes de ese arreglo,
  mejor lanzar otra (`gh workflow run build.yml --ref RAMA`, numero nuevo).

## Audios de ElevenLabs (cómo se traen al repositorio)

- Se generan con el conector de ElevenLabs (en la CI no hay clave). Los enlaces firmados de los audios caducan a
  las **~2 horas** de generarlos; desde el espacio de trabajo no se pueden bajar, desde GitHub Actions sí.
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
- (Desde la **parte 5a** del plan grande la fortaleza es otra: `gen/TorreCupula`, ver alli; lo de aqui es la de antes.)
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
