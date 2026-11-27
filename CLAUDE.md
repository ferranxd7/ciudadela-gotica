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

### Plan grande, parte 7c: los santuarios de Morvain y el viaje entre ellos

Respuesta 92 del plan ("santuarios con teletransporte"). Publicada con la build de prueba 132 bien.
- **Diecinueve santuarios** mas el del Arco del Umbral (`morvain/SantuariosMorvain`: `SITIOS`, `UMBRAL`, `todos()`), dos o
  tres por region y uno junto al crater: sitios elegidos sin abrir el juego (con `Terreno`: lo mas llano y seco cerca de
  donde se queria, en su region; en el pantano, un islote). Cada uno es una tarima redonda (radio 4, fundida con el
  terreno hasta 10) de la piedra de su region, con el santuario (apagado) en el centro, dos pilares rotos y dos farolas de
  almas; se escribe al generar el chunk (`MorvainGenerador` → `SantuariosMorvain.chunk`; el `Adorno` no pone nada encima;
  lo ve Distant Horizons). Nombre de cada uno `darkcraft.santuario.morvain_<id>` (`Santuarios.nombre` lo saca del sitio).
  Vista sin juego: `tools/vista3d` caso `morvain_santuario` (`-Dsitio=<id>`).
- **Encender y descansar** como siempre; la ficha apunta cada santuario (`Ficha.santuarioNuevo`, "dimension@x,y,z") y
  esos son los que **conoce** (`Viaje.conocidos`). Descansando en uno de Morvain, con otro conocido, el menu del santuario
  tiene **Viajar** (`SantuarioScreen`: `Net.Santuario` lleva los destinos y en cual esta) → `client/ui/ViajeScreen`: la
  lista de los conocidos (el de aqui marcado) y el **mapa de Morvain en pergamino** (`textures/gui/mapa_morvain.png`,
  1024 x 1024, de `tools/morvain/Pergamino.java` con el `Terreno`: si cambia el terreno, rehacerla) con una marca por
  santuario conocido; abajo, la region y lo lejos que esta. Al elegir uno (`Net.Viajar` → `Viaje.viajar`: tiene que estar
  descansando junto a uno de Morvain, no en combate, y conocer el destino): fundido a negro en su cliente (`Net.Fundido`
  → `client/morvain/ClientViaje`, 34 ticks), a los 12 se le lleva delante del santuario mirandolo (`Viaje.llevar`; el
  corcel, si iba montado, se deshace) y logro `viaje`.
- **La brujula** marca los santuarios de Morvain cercanos (`Hud.santuarios`): los que conoce con su llama de almas hasta
  1600 bloques; los que no, apagados y solo a menos de 480 (`HudTex.MARCA_SANTUARIO` / `MARCA_SANTUARIO_APAGADO`, de
  `tools/ui/hud.py`). El cliente sabe cuales conoce por `Net.Conocidos` (al entrar en Morvain, al conectarse alli, al
  encender uno y con los comandos).
- Comandos `/darkcraft viaje [lista | conocer <id>|todos | olvidar | ir <id>]` (`ir` lleva delante de uno desde donde sea).
- Pruebas: servidor, linea `Morvain, los santuarios: ...` (cada uno con su bloque en su sitio y con su nombre) y vistas
  `morvain_santuario_{encrucijada,juncos,helada}.png`; cliente, tramo `viaje()` dentro del de Morvain (despues del corcel):
  la Encrucijada de los Peregrinos (`cliente_mv31`), encenderla (`mv32`, cartel con su nombre), descansar en el Arco
  (sin descansar y a uno que no conoce no se viaja), el menu con Viajar (`mv33`), el mapa (`mv34`), el fundido (`mv35`),
  delante de la Encrucijada (`mv36`, linea `tras el viaje: ...`) y la brujula con un santuario conocido y otro no
  (`mv37`, `mv38`).

### Plan grande, parte 7d: los fragmentos del mapa y el mapa de la M en Morvain

Respuesta 92 del plan ("fragmentos de mapa"), como en Elden Ring. Publicada con la build de prueba 134 bien.
- **Las estelas del mapa** (`morvain/EstelaBlock`, bloque `darkcraft:estela_mapa`, irrompible, `facing`; modelo y
  texturas de `tools/morvain/estela.py`: obelisco bajo de piedra negra, el mapa de la region tallado con filete de oro,
  runas azules en los costados y un pergamino clavado arriba). Una por region de tierra (`morvain/Fragmentos.ESTELAS`): la
  de las Llanuras en la plaza del Arco del Umbral, donde empieza el camino del norte (`Llegada`, 11 al norte del
  santuario); las demas en la tarima de uno de sus santuarios, 3 al norte del fuego, mirandolo (`Fragmentos.JUNTO`:
  ahorcados, embarcadero, colmillo, puerta_academia, puerta_capital, costillar, grieta, ancla_sur). Se escriben al
  generar el chunk.
- **Copiar el mapa**: clic derecho o la E mirandola (`client/morvain/ClientMapa`: el aviso "para copiar el mapa de la
  estela" solo en las de regiones que aun no tiene; runas del encantamiento que vuelan hacia ellas) → `Net.EstelaUsar` →
  `Fragmentos.usar`: se apunta en la ficha (`Ficha.mapaNuevo`, lista `mapas`), cartel "<region> / Fragmento de mapa · n de
  9" (`Net.Cartel.FRAGMENTO`), logros `fragmento` y `mapa_morvain` (hazana, los nueve) y `Net.Mapas` al cliente (tambien al
  conectarse y al entrar en Morvain).
- **El mapa de la M en Morvain** (`LibroScreen.Vista`, `morvain()`): se abre de lejos (`ppb` 0,12); encima de lo explorado
  (la tinta de siempre), el pergamino de `mapa_morvain.png` **solo en las regiones cuyo mapa tiene** y el Mar Muerto
  (`client/morvain/MapaMorvain`: compone una textura de 1024 con la mascara de `mapa_morvain_regiones.png` —un gris por
  region, su orden x 25; lo escribe tambien `tools/morvain/Pergamino.java`—, bordes fundidos y quemados; se rehace al
  cambiar los mapas; de cerca se transparenta para ver lo andado). Encima: el nombre de cada region que tiene (en su
  punto mas metido, `MapaMorvain.nombre`), la ciudadela, el Arco del Umbral y los santuarios que conoce (iconos
  `EspejoHud.I_SANTUARIO`, `I_CIUDADELA`, `I_ESTELA` de `tools/espejo/iconos.py`).
- **Viajar desde el mapa**: clic en un santuario que conoce (estando en Morvain) → "¿Viajar a «...»?" → `Net.ViajarMapa`
  → `Viaje.desdeMapa` (sin estar descansando; no en combate: aviso `darkcraft.viaje.enemigos`; el mismo fundido).
- Comandos `/darkcraft mapa [lista | dar <region>|todos | quitar | ir <region>]`. `Net.V` = "20".
- Pruebas: servidor, linea `Morvain, las estelas del mapa: ...` (cada una en su sitio, mirando al sur y con hueco
  delante); cliente, tramo `mapaMorvain()` despues del viaje: la estela de las Llanuras con su aviso (`cliente_mv39`), la E
  (`mv40`, cartel), la M con solo las Llanuras (`mv41`), con todo (`mv42`), de cerca (`mv43`), la pregunta del viaje
  (`mv44`) y despues del viaje al vado (`mv45`, linea `tras viajar desde el mapa: ...`); en combate no se viaja.

### Plan grande, parte 7e: las Llanuras Palidas — los Marchitos y Velmor, el pueblo marchito

Respuestas 125, 131 ("pueblos derrumbados") y 132 del plan; la primera region con su contenido. Publicada con la build de
prueba 136 bien.
- **El Marchito** (`enemigos/Marchito`, entidad `darkcraft:marchito`; modelo de `tools/enemigos/marchito.py`: aldeano de
  ceniza agrietada con brasas en las grietas, harapos y capucha; `MarchitoGeometria/Animaciones`, `MarchitoAnims`,
  `MarchitoModelo`, el `EnemigoRenderer` de siempre a escala 1,08). 32 de vida, golpe 5,5, lento. Tres: **guadana**
  (siega a ras de suelo de 170 grados —se salta o se rueda— y tajo de arriba abajo que llega a 3,8), **farol** (dos
  cuchilladas y el farol de almas: lo estrella delante y deja **fuego de almas** `FUEGO_TICKS` 50 en un radio de 1,6 que
  quema a quien lo pisa; sus golpes prenden) y **campanero** (baculazo; si ve a alguien entre 4,5 y 20 bloques **toca la
  campana**: tres campanadas, cada una llama a los enemigos a `LLAMADA` 26 a por su presa —`Enemigo.acudir`— y frena a
  quien este a 6; una vez cada `ESPERA_CAMPANA` 22 s; `Marchito.CAMPANADAS` para la prueba). Posturas de pie, de
  rodillas o tumbado. Al caer se deshace en ceniza (bestiario `marchito_guadana/farol/campana`, logro `marchito`); a veces
  una piedra de forja I.
- **Velmor** (`morvain/Velmor`, en la orilla este del rio palido, al norte del santuario del Vado; el cruce de sus dos
  calles en (850, 4040)): se dibuja como Brumavela —las casas son `pueblo/gen/Casa` (`CasaVelmor`: barro, adobe y madera
  vieja en vez de cal) sobre el terreno de Morvain (`Velmor.alto`, con cache; `Casa` ya acepta cualquier
  `pueblo/gen/Alturas` y su botin con `botin(normal, bueno)`, null = sin arcon)— chunk a chunk al adornar
  (`MorvainGenerador.applyBiomeDecoration`, despues de `Adorno`, que en la caja de Velmor solo pone hierba). 33 casas por
  las dos calles (muchas en ruina; la taberna, la herreria, la casa del alguacil y el granero), la plaza con el pozo, la
  **horca** y un puesto; al norte **la capilla** sin tejado (la esquina del noreste caida, bancos rotos, altar con velas,
  el atril con el **libro perdido 18**, "La campana de Velmor" de Anselm el campanero; arcon de la sacristia) y su
  **campanario** de 22 con chapitel, escalera de mano y la campana (arcon arriba); al oeste el **cementerio** con el
  **panteon** (arcon); campos secos al oeste y al sureste (trigo seco, gavillas, espantapajaros); farolas de almas (unas
  apagadas o caidas), carros, arboles muertos sin hojas (`Landscape.DeadTree.sinHojas`), las puertas del sur y del este;
  el **puente roto** sobre el rio (cuatro arcos, el tercero hundido: un hueco de diez que a pie no se salta y el corcel
  si) y el **vado** de piedras al sur. Botin `chests/velmor` y `velmor_bueno`. Sitios con nombre (`Velmor.sitio`: atril,
  campana, pozo, horca, puente_oeste/este/hueco, vado, puerta_sur/este, capilla, cementerio, panteon, cofres...). Al
  entrar: el nombre "Velmor" (`Morvain.lugar`, cartel la primera vez) y el logro `velmor`.
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_velmor carpeta` (`-Dcaja=x0,z0,x1,z1` para un trozo) dibuja el
  terreno y el pueblo con las marcas de sus sitios.
- **Los puestos** (`morvain/MorvainEnemigos`): sitio `velmor` (29: uno en cada cabeza del puente, calles, la plaza —uno de
  rodillas ante la horca—, la capilla —el campanero a la puerta, dos rezando y uno ante el altar—, el cementerio, la
  taberna, la herreria, la casa del alguacil y los campos, con el otro campanero) y `llanuras` (tres que vagan entre el
  arco y Velmor). **`Puestos` ya vale para cualquier dimension**: cada sitio va en el primer mundo del servidor que diga
  que es el suyo (`Puestos.nivel`); antes solo miraba el de las nubes.
- Sonidos (ElevenLabs, `TOMAS_SFX`): `marchito_gemido` (1, 3 y 2), `marchito_alerta` (1 y 3), `marchito_dolor` (3, 1 y
  4), `marchito_muerte` (2 y 3), `marchito_campana` (las cuatro: cada toma es una sola campanada) y `marchito_farol` (1,
  4 y 2).
- Comandos `/darkcraft velmor [ir <sitio> | enemigos [volver | fuera | sacar]]`.
- Pruebas: servidor, lineas `Morvain, Velmor: ...` (chunks, casas, el atril con el libro 18, la campana, arcones; vistas
  `morvain_velmor*.png`), `Morvain, los marchitos: ...` (puestos, ninguno cerca del arco ni de un santuario), `... fuera:`
  (en el suelo, ninguno metido en un bloque, todas sus acciones acaban) y `Morvain, la campana de los marchitos: ...` (las
  campanadas llaman al de 8 y al de 12 bloques y no al de 40; el farol deja fuego; uno cae en ceniza); cliente, tramo
  `velmor()` despues del mapa: el puente desde la otra orilla (`cliente_mv46`), el pueblo (`mv47`), la plaza (`mv48`), la
  capilla (`mv49`), el altar y el atril (`mv50`), la campana (`mv51`), los tres marchitos (`mv52`), el campanero tocando
  (`mv53`), el farol (`mv54`), la siega (`mv55`), la ceniza (`mv56`), el libro del campanero (`mv57`) y entrar a pie por la
  puerta del sur (`mv58`, el cartel y el logro).

### Plan grande, parte 7f: la Molinera de Velmor (el jefe de las Llanuras Palidas) y su molino

Respuestas 125 ("mas de 10 jefes principales, varios por region") y 131 del plan; el primer jefe de Morvain. Publicada con
la build de prueba 138 bien.
- **La Molinera** (`enemigos/Molinera`, entidad `darkcraft:molinera`, 1,3 x 3,7; hereda de `Guardian`: barra, musica, rugido
  al empezar y a media vida, vuelve a su puesto con la vida entera si todos caen o se van). La molinera del libro del
  campanero, crecida por la Sombra hasta el doble de alto: modelo de `tools/enemigos/molinera.py` (vieja enorme y encorvada
  de ceniza agrietada con brasas, saya y delantal blanqueados de harina, chal rojo, panuelo negro y una guadana del doble de
  larga; `MolineraGeometria/Animaciones`, `MolineraAnims`, `client/enemigos/MolineraModelo`, el `EnemigoRenderer` de siempre
  a escala 1,9). 700 de vida, golpe 11, armadura 8. **Siega** (5,6 de alcance, 210 grados, a ras de suelo: no pilla a quien
  la salta —`Enemigo.pilla`, los pies a mas de 0,55—; nunca tres seguidas), **tajo** (la hoja se clava y levanta una
  **onda** a 4,6 delante, radio 2,7: saltando o rodando se libra), **estocada** (se lanza a quien esta entre 5,4 y 9,5),
  **pisoton** (a quien se le pone detras o pegado: lo aparta, con su onda); a **media vida** grita y **llama a los suyos**
  (dos marchitos de guadana y farol, uno mas por amigo hasta cuatro, que se alzan de la ceniza de la era: efimeros, sin
  puesto; se deshacen si la pelea se acaba) y empieza el **giro** (dos vueltas avanzando hacia su presa, 360 grados).
  `Guardian` tiene ahora `alEmpezarPelea`, `alAcabarPelea` y `puedeRugir`.
- **Voz** (ElevenLabs, `eleven_v4`, **"Kayla - Evil Witch Villian Storyteller"** `5PWbsfogbLtky5sxqtBz`, flow
  `eDmyp1z5phPwszhGJdPA`; 5 frases, tomas a; en `procesar.py` `voz_molinera`: algo mas grave y ronca, una segunda voz
  octava y media por debajo, sorda, y el eco abierto de las llanuras): `Net.ReyHabla` con linea **60 + N**
  (`Voces.BASE_MOLINERA`, `esMolinera`; `habla_molinera` en `rey_voces.json`; subtitulos `darkcraft.molinera.voz.N`):
  1 al empezar (no la repite en 4 min; **hasta acabarla no grita ni ataca**: se levanta y se acerca despacio), 2 al llamar a
  los suyos, 3 si cae su presa, 4 la primera vez que gira y 5 al morir.
- **Barra** (`GuardianCliente`, arte de `tools/hud/barra_guardianes.py` "molinera": la viga del molino con flejes y
  espigas secas, el extremo de un aspa en cada punta y la piedra de moler en el centro; relleno de brasas que corren;
  ceniza y harina que caen). **Musica**: "Final_Boss_Lair" de HitCtrl (CC-BY 3.0, id `molinera`, tema 33 de
  `ClientMusic.boss`).
- **Al caer** (lo reparte `valdrenn/Guardianes`, ahora en el mundo donde cae: `Reparto.dim`): el cartel de JEFE ABATIDO con
  7000 ecos, la hazana `molinera` ("La rueda se ha parado", con su entrada del diario) y el bestiario; deja dos piedras de
  forja II, una III y la ceniza del torbellino. **El molino deja de crujir y se abre su reja.**
- **El molino** (`morvain/Molino`, al este de Velmor, al final del camino que sale por la puerta del este; se dibuja como
  Velmor, chunk a chunk al adornar, despues del pueblo; `Adorno` solo pone hierba en su caja): una **loma** (`loma(x, z)`:
  llana a Y 76 alrededor de la era y del molino, baja en 9 bloques; el camino sube con medias losas), la **era** (radio 17,
  losas en anillos, un murete roto con entradas al oeste —el camino—, al este —la puerta del molino—, al norte y al sur,
  cuatro farolas de almas, la piedra de moler en el centro ante la que espera ella de rodillas mirando al molino, de
  espaldas a quien llega, sacos, gavillas), la **torre** (radio 6, 13 de piedra con la puerta al oeste, el cuerpo de madera
  hasta 22 y el tejado a dos aguas de este a oeste; dentro las muelas, sacos, la escalera de mano pegada al arbol hasta el
  piso de arriba con el **arcon** `velmor_bueno`), las **aspas** (cuatro en cruz desde el cubo del hastial del oeste, 19
  de largo, con su armazon de listones y jirones de lona; la de abajo partida, su armazon caido al norte), la **reja** de la
  puerta (barrotes: cerrada mientras vive la Molinera; `Molino.puerta`, la pone como toca `Molino.crujir` cada 5 s y la
  abre al caer ella), el **crujido** del molino con el viento (`sfx.molino_viento` cada 5 s desde el cubo, mientras vive),
  campos secos, carros, arboles muertos y farolas por el camino. Sitios: `molinera`, `era`, `entrada`, `puerta_molino`,
  `cofre_molino`, `camino`, `aspas`. El Espejo: "El Molino de Velmor" (`Morvain.lugar`).
- **El santuario del Camino del Molino** (`SantuariosMorvain` "molino", en (1086, 4048), a la orilla del camino, a 47 de
  la era): ya son veinte, mas el del arco.
- **Los puestos** (`MorvainEnemigos`, sitio `molino`): la Molinera (puesto 0, grupo `Puestos.JEFE`: cae una vez) y dos
  marchitos por el camino (uno con farol que mira hacia Velmor, otro de rodillas mirando al molino).
- Sonidos (ElevenLabs, `TOMAS_SFX`): `molinera_grito` (3 y 1), `molinera_siega` (4, 1 y 3), `molinera_golpe` (3 y 4),
  `molinera_giro` (2 y 4), `molinera_dolor` (1, 3 y 2), `molinera_muerte` (4 y 2), `molino_viento` (4 y 1, en bucle).
- Comandos `/darkcraft molinera [ir | vencer | volver]` (sin mas: como esta; `volver` tambien cierra la reja).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_molino carpeta` (`-Dcaja=...`): la loma, la era, el molino y el
  camino, y si se llega a pie (del camino a la era y a la puerta; dentro y al arcon de arriba solo con la reja quitada).
- Pruebas: servidor, lineas `Morvain, el molino: ...` (la era despejada, la muela, el cubo de las aspas, el arcon, la reja
  y el nombre del santuario; vista `morvain_molino.png`), `Morvain, la Molinera: ...` (sale de rodillas en su sitio; al
  verle se levanta y grita al acabar la frase 1; todas sus acciones acaban; la siega pilla de pie y no saltando; el tajo y
  el pisoton con onda; a media vida salen los suyos) y `Morvain, la Molinera cae: ...` (la reja abierta); cliente, tramo
  `molinera()` despues de Velmor: el molino desde el camino (`cliente_mv59`), ella de rodillas (`mv60`), se alza hablando
  (`mv61`), grita con su barra (`mv62`), la siega, el tajo con su onda, el giro, la estocada y el pisoton de cerca
  (`mv63..mv67`), a media vida llama a los suyos (`mv68`), cae (`mv69`), el cartel (`mv70`), la reja abierta (`mv71`) y el
  bestiario (`mv72`).

### Plan grande, parte 7g: el Bosque Muerto — los Ahorcados y la Arboleda de los Ahorcados

Respuestas 125, 131 y 132 del plan; la segunda region con su contenido (despues, la **7h**: el jefe del bosque, el Verdugo
de la Arboleda —Maese Odran, el del libro 19—).
- **El ahorcado** (`enemigos/Ahorcado`, entidad `darkcraft:ahorcado`, 0,6 x 2,4; modelo de `tools/enemigos/ahorcado.py`: largo
  y flaco, el cuello estirado y torcido, piel gris verdosa con moratones, camisa y calzones rotos, la soga al cuello —en el
  torso: la cabeza se ladea sin moverla— y el cabo cortado por la espalda; `AhorcadoGeometria/Animaciones`, `AhorcadoAnims`,
  `client/enemigos/AhorcadoModelo` y `AhorcadoRenderer`, escala 1,1). 28 de vida, golpe 5, rapido. Dos: **garras**
  (zarpazo, doble zarpazo y **salto** a quien esta a 3,6-7,5) y **soga** (el del saco en la cabeza: **latigazo** que llega a
  3,8 y **lazo** a quien esta a 4-10 bloques: en el tick 18 engancha si le entra el golpe —rodar o cubrirse libran— y tira
  de el hasta sus pies con `hurtMarked`; el cliente dibuja la soga de la mano a quien lleva, como la correa del juego,
  `AhorcadoRenderer.soga`, con el id sincronizado `LAZO`). Posturas: **colgado** (`Enemigo.COLGADO` = 3, nueva), de
  rodillas, tumbado o de pie. Bestiario `ahorcado_garras` / `ahorcado_soga`, logro `ahorcado`; a veces una piedra de forja I.
- **Colgado**: el puesto es el suelo bajo su soga; al salir (`Ahorcado.asignar` → `colgar`) busca la soga de bloques encima
  (la primera a 1-9 bloques) y se cuelga sin gravedad con el **nudo** justo debajo: el modelo tiene la pieza `colgadero`
  (pivote en el nudo, `NUDO` de `ahorcado.py`, exportado a `AhorcadoAnims.NUDO_X/Y/Z` y `SOGA_ARRIBA`): el bucle `colgado`
  lo gira alrededor del nudo y la pieza `soga_arriba` sube 12 px hasta la soga de bloques (`NUDO_ALTO` + `SOGA_ALTO`: los
  pies 2,84 por debajo del fondo de la soga; el centro 0,30 al otro lado del nudo, segun su giro). Nada le empuja; cruje la
  soga (`sfx.ahorcado_soga`). **Se descuelga** (`CAER`, la accion de despertar del colgado) sobre quien pasa a menos de
  `CAIDA` 4,2 en horizontal (agachado, la mitad) o si le pegan: tick 1 la soga se parte (el bloque de abajo pasa a
  `cabo`; al volver a colgar se rehace), tick 2 cae hacia su presa, tick 8 el golpe de la caida (radio 2,6, 360 grados) con
  `sfx.ahorcado_cae`. Vuelve a colgar al reaparecer (descansar, morir, o alejarse y volver).
- **La soga** (`morvain/SogaBlock`, `darkcraft:soga`, irrompible, sin choque; estado `parte`: `cuerda`, `cabo` deshilachado,
  `lazo` vacio; modelos y textura de `tools/morvain/soga.py`). En las vistas sin juego sale como cadena.
- **La Arboleda de los Ahorcados** (`morvain/Arboleda`, en el Bosque Muerto, al oeste del santuario "ahorcados" —el de la
  estela del bosque—; centro del **Arbol de la Horca** en (-4888, 3192); se dibuja como Velmor, chunk a chunk al adornar;
  `Adorno` solo pone setas y helechos en su caja): el suelo del bosque (podzol, raices, setas, huesos), el **camino** del
  santuario al claro con picas con calaveras y farolas de almas, la **puerta** (dos arboles a los lados del camino, uno
  colgado encima), el **Arbol de la Horca** (tronco de 3 x 3, raices al aire, ocho ramas, seis colgados, lazos vacios y
  cabos; la copa muerta), diez **arboles con sus sogas** alrededor, el **patibulo** al norte (tablado a 3 del suelo con
  barandilla y escalera al sur, la horca con tres sogas —el colgado sobre la trampilla—, el **atril con el libro perdido 19**
  "El registro del verdugo" de Maese Odran —`Perdidos.TOTAL` = 19— y un arcon `velmor_bueno`) y las **tumbas** al suroeste
  (cruces de madera). Lo decidido de cada pieza va en listas de bloques (`Lista` → `Trozo`). Sitios: `arbol`, `claro`,
  `puerta`, `entrada`, `patibulo`, `atril`, `arcon`, `escalera`, `tumbas`. El Espejo: "La Arboleda de los Ahorcados"
  (`Morvain.lugar`); logro `arboleda` al entrar; el **viento de la arboleda** (`ambiente.arboleda`, rachas de una de dos
  tomas: `ClientMusic.tickArboleda`).
- **Los puestos** (`MorvainEnemigos`, sitio `arboleda`, 22): los catorce colgados que apunta el plano (`Arboleda.colgados`:
  seis en el Arbol de la Horca, seis en los de alrededor, el de la puerta y el del patibulo) y los demas
  (`Arboleda.otros`): tres de rodillas al pie del arbol mirando al tronco, tres tumbados entre las tumbas, uno junto al
  patibulo y otro que ronda por el camino.
- Sonidos (ElevenLabs, `TOMAS_SFX`): `ahorcado_soga` (4, 1 y 3), `ahorcado_gemido` (4, 1 y 3), `ahorcado_alerta` (2 y 4),
  `ahorcado_cae` (4, 2 y 1), `ahorcado_zarpazo` (3 y 1), `ahorcado_lazo` (3, 1 y 4), `ahorcado_dolor` (4, 2 y 3),
  `ahorcado_muerte` (1 y 3), `arboleda_viento` (3 y 2); el chasquido de la soga es el del nudo de una correa del juego.
- Comandos `/darkcraft arboleda [ir <sitio> | enemigos [volver | fuera | sacar]]` (sin mas: como esta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_arboleda carpeta` (`-Dcaja=...`): el claro, los arboles, el patibulo
  y las tumbas con las marcas de los puestos (C colgados, A los demas) y si se llega a pie (de la entrada al arbol, a la
  puerta, a lo alto del patibulo y a cada puesto) y que cada colgado tiene su soga encima y nada le toca.
- Pruebas: servidor, lineas `Morvain, la arboleda: ...` (las sogas, el atril con el libro 19, el arcon; vistas
  `morvain_arboleda*.png`), `Morvain, los ahorcados: ...` (los colgados bajo su soga y a la altura, los demas en el suelo) y
  `Morvain, los ahorcados en accion: ...` (uno se descuelga hacia su presa y la soga queda deshilachada; cada accion acaba;
  el lazo engancha y tira; el salto; cae sin dejar el cuerpo); cliente, tramo `arboleda()` despues del molino: desde el
  santuario (`cliente_mv73`), entrar a pie con el cartel y el logro (`mv74`), el Arbol de la Horca (`mv75`), un colgado de
  cerca (`mv76`), la puerta (`mv77`), el patibulo (`mv78`), en supervivencia el de la puerta se descuelga al pasar
  (`mv79`, `mv80`), el lazo (`mv81`), el zarpazo y el salto (`mv82`, `mv83`), las tumbas (`mv84`), el libro del verdugo
  (`mv85`) y el bestiario (`mv86`).

### Plan grande, parte 7h: Maese Odran, el Verdugo de la Arboleda (el jefe del Bosque Muerto)

Respuestas 125 y 131 del plan; el segundo jefe de Morvain (el del libro perdido 19, "alguien tiene que llevar la cuenta").
- **El Verdugo** (`enemigos/Verdugo`, entidad `darkcraft:verdugo`, 1,2 x 3,4, se manda cada 2 ticks —`updateInterval(2)`: el
  salto—; hereda de `Guardian`): modelo de `tools/enemigos/verdugo.py` (un hombreton muerto y cosido: la capucha negra con
  los ojos encendidos —capa de brillo—, el pecho al aire con costuras, el mandil de cuero manchado, el cinto con la soga
  enrollada, brazales y botas; el hacha de verdugo a dos manos y el lazo en la izquierda; escala 1,5, `VerdugoAnims.ESCALA`;
  `client/enemigos/VerdugoModelo` y `VerdugoRenderer`, que dibuja la soga del lazo como la de los ahorcados). 820 de vida,
  golpe 12, armadura 10. Espera **de pie en el tablado del patibulo, apoyado en el hacha** (postura `REZA` = el bucle
  `apoyado`; el puesto 0 del sitio `patibulo` de `MorvainEnemigos`, grupo `Puestos.JEFE`), mirando al Arbol de la Horca.
- **La pelea**: al verle (16 bloques, con linea de vista) se yergue y dice la frase 1; mientras la dice no se mueve del
  patibulo (`distanciaDeseada` 99) ni ataca; al acabarla ruge y **salta** sobre su presa (`SALTO`: despega en el tick 9 y
  cae en el 20 en un arco que no choca con nada —`noPhysics`: pasa por encima de la barandilla—, hasta poco antes de ella,
  como mucho 11 bloques, donde quepa; golpe alrededor al caer y una onda por el suelo). Despues: **hachazo** (onda delante:
  saltando o rodando se libra), **barrido** a media altura (no se salta), **doble** avanzando, **pisoton** a quien se le pone
  detras, **lazo** a quien esta a 5-12 bloques (engancha si le entra el golpe —rodar o cubrirse libran—, lo arrastra a sus
  pies y le sigue la **ejecucion**, el hachazo rapido) y un **salto** a quien se aleja. A **media vida** grita y **despierta a
  los colgados**: los que siguen colgando a 28 bloques se descuelgan sobre el jugador que tengan mas cerca; si no hay
  bastantes (dos, uno mas por amigo, hasta cuatro), se alzan otros ahorcados de la tierra (efimeros). `Verdugo.mediaVida()`
  (pruebas) lo hace ya.
- **Voz** (ElevenLabs, `eleven_v4`, **"Declan Graves - Haunted Rasps and Old World Dread"** `1BfrkuYXmEwp8AWqSLWk`, flow
  `3ClnHX7LlNDqJW4ZiWtF`; 5 frases, tomas a; en `procesar.py` `voz_verdugo`: mas grave, una segunda voz casi una octava por
  debajo, algo de raspa y el eco corto del bosque): linea **70 + N** (`Voces.BASE_VERDUGO`, `esVerdugo`; `habla_verdugo` en
  `rey_voces.json`; subtitulos `darkcraft.verdugo.voz.N`): 1 al empezar (no la repite en 4 min), 2 al despertar a los
  colgados, 3 si cae su presa, 4 la primera vez que engancha a alguien y 5 al morir.
- **Barra** (`GuardianCliente`, arte de `tools/hud/barra_guardianes.py` "verdugo": la viga de la horca con vueltas de soga, los
  postes con sus tornapuntas y en el centro el lazo con la capucha y los ojos; relleno de una soga con sangre que gira;
  hebras de canamo y hojas que caen). **Musica**: "Epic boss theme" (Ordmaer, King of the Windymoon) de ProjectHelmet (Philemon
  Weber, CC-BY 4.0, id `verdugo`, tema 34 de `ClientMusic.boss`; la primera que se puso era la de la fase 2 del Rey: **cada
  jefe, su musica**, mirar `ci/musica/pistas.json` antes de elegir).
- **Al caer** (lo reparte `valdrenn/Guardianes`): el cartel de JEFE ABATIDO con 8000 ecos, la hazana `verdugo` ("La cuenta
  esta cerrada", con su entrada del diario) y el bestiario; deja dos piedras de forja II, una III y la ceniza de la sangre.
  **La cuenta se cierra** (`Arboleda.liberar`, desde `Puestos.alCaer`): los colgados que aun cuelgan se deshacen en almas,
  la soga de cada colgado se queda con el **lazo vacio** (`Arboleda.cuenta` lo repasa cada 5 s con alguien cerca) y **ya no
  vuelven a colgar**: el sitio `arboleda` se registra con `activo` = los del grupo `colgados` solo mientras el Verdugo no
  este muerto (`Arboleda.cuentaCerrada`). Los demas ahorcados siguen.
- Sonidos (ElevenLabs, `TOMAS_SFX`): `verdugo_grito` (3 y 1), `verdugo_hachazo` (4 y 2), `verdugo_barrido` (4 y 1),
  `verdugo_lazo` (1 y 4), `verdugo_salto` (4 y 2; tambien el pisoton), `verdugo_dolor` (1, 4 y 3), `verdugo_muerte` (1 y 2).
- Sitios nuevos de la arboleda: `verdugo` (su puesto) y `ante_patibulo` (en el claro, a 16 de el). Comandos
  `/darkcraft verdugo [ir | vencer | volver]` (sin mas: como esta y las sogas de los colgados; `volver` le pone en pie y
  vuelven los colgados).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_arboleda carpeta` escribe tambien la linea `el Verdugo: ...` (su caja en
  el tablado libre, se llega a pie, el claro de delante).
- Pruebas: servidor, lineas `Morvain, el Verdugo: ...` (sale apoyado en el tablado; al verle se yergue, ruge al acabar la frase
  y salta del patibulo hasta su presa sin meterse en nada; cada accion acaba; los golpes a un marchito; el lazo engancha,
  arrastra y ejecuta), `Morvain, el Verdugo a media vida: ...` (se descuelgan los de cerca; sin colgados se alzan de la
  tierra), `Morvain, el Verdugo cae: ...` (la cuenta cerrada, las sogas con el lazo vacio, ya no salen colgados) y
  `Morvain, el Verdugo vuelve: ...`; cliente, tramo `verdugo()` despues de la arboleda: el patibulo desde el claro
  (`cliente_mv87`), de cerca (`mv88`), en supervivencia se yergue hablando (`mv89`), ruge con su barra (`mv90`), salta (`mv91`)
  y cae delante (`mv92`), sus golpes (`mv93..mv97`: hachazo, barrido, doble, pisoton, el salto de lado), el lazo y la
  ejecucion (`mv98`, `mv99`), despierta a los colgados (`mv100`), cae (`mv101`), el cartel (`mv102`), los lazos vacios del
  Arbol de la Horca (`mv103`) y el bestiario (`mv104`).

### Plan grande, parte 7i: el Pantano Podrido — los Podridos y la Aldea de los Pilotes

Respuestas 125, 131 ("pueblos derrumbados") y 132 del plan; la tercera region con su contenido (despues, la **7j**: el jefe
del pantano, el Barquero —el del libro 20—: hecha, arriba).
- **El podrido** (`enemigos/Podrido`, entidad `darkcraft:podrido`, 0,7 x 1,95; modelo de `tools/enemigos/podrido.py`: un ahogado
  hinchado de piel gris verdosa palida con venas moradas, manchas de podredumbre y sanguijuelas, barro hasta la cintura, algas
  colgando de la cabeza y los hombros, la boca abierta y los ojos amarillos en la capa de brillo; `PodridoGeometria/Animaciones`,
  `PodridoAnims`, `client/enemigos/PodridoModelo` y `PodridoRenderer`, escala 1,12; el hinchado, un 12 % mas). 34 de vida (el
  hinchado +65 %), golpe 5, lento. Tres: **podrido** (zarpazo, mordisco a la carrera que envenena y el **vomito**: un chorro de
  agua negra de 5 bloques delante que envenena y frena y deja un charco), **fatuo** (un fuego verde en la palma izquierda —pieza
  `fatuo` con su halo en la capa de brillo—: golpe con el y, de 4 a 18 bloques, lo **lanza**: `enemigos/FuegoFatuo`, un
  proyectil en arco sin modelo —particulas verdes, con shaders igual—, magia y veneno al dar y una nube donde cae; el modelo
  esconde el fuego desde que sale hasta que acaba la accion) e **hinchado** (la panza enorme con una raja que brilla: se **deja
  caer** de panza encima —charco que frena—, **eructa** una nube de gas alrededor —veneno y debilidad— y al morir **revienta**:
  golpe a quien este pegado y una nube grande). Las nubes son la nube de efectos de Minecraft (`AreaEffectCloud`, con
  particulas de polvo de color: `Podrido.nube`); a los podridos no les hace nada su veneno (`canBeAffected`). Posturas:
  **flotando boca abajo** en el agua (`TUMBADO`: la entidad flota como todos los enemigos —`FloatGoal`— y el dibujante sube el
  cuerpo hasta que asoma la espalda, `PodridoRenderer.subida`; al alzarse vuelve a bajar mientras se da la vuelta; sin agua, el
  bucle `tendido`), de rodillas (`REZA`) o de pie. Bestiario `podrido` / `podrido_fatuo` / `podrido_hinchado`, logro `podrido`;
  a veces una piedra de forja I. Contadores para las pruebas: `VOMITOS`, `FATUOS`, `NUBES`, `REVENTONES`.
- **La Aldea de los Pilotes** (`morvain/Pilotes`, en el Pantano Podrido, al noroeste del santuario de los Juncos; el centro de la
  plaza en (-5650, -770); se dibuja como la arboleda, chunk a chunk al adornar; en su caja `Adorno` no pone nada): **cava una
  laguna** en el pantano (radio 34 con un lobulo al norte para la capilla, `orilla`/`suelo`: fondo a 60-61, dos o tres de agua,
  se funde con el pantano en 12; islotes de barro con juncos y mangles muertos), la **plaza** (tablado de 13 x 13 a `PASO` = 64,
  un bloque por encima del agua: la hoguera de almas en su cerco de barro, la campana colgada, las redes, barriles), **ocho
  chozas** (`CHOZAS`: de las redes, el ahumadero, la del barquero —con arcon—, la curandera, el pescador, la viuda, el cestero, el
  calafate) sobre sus pilotes un escalon por encima (`CHOZA` = 65): paredes de mangle con troncos en las esquinas, puerta hacia
  su pasarela y ventanas, tejado a cuatro aguas de roble oscuro, alguna medio hundida o sin tejado (`ruina`), farol colgado de una
  cadena, barril con botin; las **pasarelas** (`PASARELAS`, dos de ancho, pilotes cada tres y postes de amarre con farol a los
  lados; escalones a las chozas, `ESCALONES`), la **capilla hundida** al norte (zocalo de piedra en la laguna, la nave con un
  palmo de agua y los bancos sumergidos, el presbiterio seco al norte con el altar y sus velas, el **atril con el libro perdido
  20** —"Lo que trae el agua negra", de la hermana Ilse; `Perdidos.TOTAL` = 20— y un arcon; muros con ventanas y huecos, medio
  tejado y su hastial, vigas sueltas y cascotes; el campanario partido y su campana en el fondo de la laguna), el
  **embarcadero** largo al sur con el muelle y la pasarela que sigue hasta el santuario, y barcas varadas o hundidas. Sitios
  (`Pilotes.sitio`): `plaza`, `fuego`, `pasarela`, `muelle`, `entrada`, `capilla`, `puerta_capilla`, `presbiterio`, `atril`,
  `arcon`, `campana` y `choza_<nombre>`. El Espejo: "La Aldea de los Pilotes" (`Morvain.lugar`); logro `pilotes` al entrar.
- **Los puestos** (`MorvainEnemigos`, sitio `pilotes`, 24, de `Pilotes.esperas`): ocho flotando a tres de las pasarelas (se alzan
  al pasar), en la plaza un hinchado, uno de rodillas ante el fuego y un fatuo, uno en cada choza, dos rezando en el
  presbiterio y un hinchado en el agua de la nave, un fatuo en el muelle y uno que ronda por la pasarela del santuario.
- **El pantano suena** (`ClientMusic.tickPantano`): en toda la region del pantano, `ambiente.pantano` (borbotea, croan las ranas,
  zumban los insectos; una de dos tomas cada vez).
- Sonidos (ElevenLabs, flow `um9O1CHUekivKrw70cJm`, `TOMAS_SFX`): `podrido_gemido` (3, 2 y 1), `podrido_alerta` (1 y 4),
  `podrido_dolor` (1 y 3), `podrido_muerte` (2 y 3), `podrido_alzarse` (2 y 4), `podrido_vomito` (3 y 1), `podrido_fatuo` (2, 3
  y 1), `podrido_gas` (1 y 4), `podrido_aplastar` (4 y 2), `pantano_ambiente` (2 y 3).
- Comandos `/darkcraft pilotes [ir <sitio> | enemigos [volver | fuera | sacar]]` (sin mas: como esta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_pilotes carpeta` (`-Dcaja=...`): la laguna, la aldea y la capilla con
  las marcas de los podridos (P) y si se llega a pie desde la entrada (junto al santuario) a cada sitio y a cada podrido, y que
  ninguno esta metido en un bloque.
- Pruebas: servidor, lineas `Morvain, la aldea de los pilotes: ...` (el atril con el libro 20, el arcon, el fuego, el agua de la
  laguna, las tablas del embarcadero; vistas `morvain_pilotes*.png`), `Morvain, los podridos: ...` (los del agua a flote, los
  demas en su suelo, ninguno metido en un bloque) y `Morvain, los podridos en accion: ...` (el del agua se alza; cada accion
  acaba; el vomito, el fuego fatuo y el eructo envenenan; su veneno no les hace nada; el hinchado revienta); cliente, tramo
  `pilotes()` despues del Verdugo: desde el santuario de los Juncos (`cliente_mv105`), entrar por el embarcadero con el cartel y
  el logro (`mv106`), la plaza (`mv107`), las chozas desde lo alto (`mv108`), los que flotan (`mv109`), la capilla hundida y su
  presbiterio (`mv110`, `mv111`), en supervivencia uno se alza del agua (`mv112`), el vomito (`mv113`), el fuego fatuo y su nube
  (`mv114`, `mv115`), el hinchado se deja caer, eructa y revienta (`mv116..mv118`), el libro de la hermana Ilse (`mv119`) y el
  bestiario (`mv120`).

### Plan grande, parte 7j: el Barquero de la Laguna Negra (el jefe del Pantano Podrido)

Respuestas 125 y 131 del plan; el tercer jefe de Morvain (el del libro perdido 20: "le oigo remar bajo la capilla... no subas
a su barca").
- **La Laguna Negra** (`morvain/LagunaNegra`, en el Pantano Podrido, al este del santuario del Embarcadero Podrido; el centro
  del embarcadero en (-3955, -1880); se dibuja como la aldea de los pilotes, chunk a chunk al adornar; en su caja `Adorno` no
  pone nada): **cava una laguna** honda (radio 30, fondo a 59-60: tres o cuatro de agua, `orilla`/`suelo`, se funde con el
  pantano en 12) con islotes de barro, juncos, mangles muertos, barcas hundidas y **estacas con calaveras** (los que no
  pagaron); en medio, **el embarcadero**: un octogono de tablas (radio `R_PLATAFORMA` 11) a `PASO` = 64 (un bloque por encima
  del agua) sobre pilotes, con dos anillos de roble oscuro, los ocho postes de amarre de las esquinas con su farol de almas, la
  barandilla rota (abierta donde llegan el muelle y la barca), rollos de cabo, barriles, cadenas y velas; desde la orilla del
  oeste el **muelle** (tres de ancho, postes con farol a los lados) y hasta el un camino de tierra pisada desde el santuario
  (`CAMINO`, con faroles); al este, amarrada, **la barca del Barquero** (cinco por once, la proa al norte, el suelo a ras de
  agua —`BARCA_SUELO`—, la borda de medias losas, el farol de proa colgando y el **arcon del peaje** en la popa) y la estaca
  de amarre; en la otra orilla, un arco de piedra medio caido. Sitios (`LagunaNegra.sitio`): `barquero`, `embarcadero`,
  `ante_embarcadero`, `muelle`, `orilla`, `entrada`, `barca`, `arco`. El Espejo: "La Laguna Negra" (`Morvain.lugar`).
- **El Barquero** (`enemigos/Barquero`, entidad `darkcraft:barquero`, 1,2 x 3,6, se manda cada 2 ticks —el salto—; hereda de
  `Guardian`): modelo de `tools/enemigos/barquero.py` (un muerto alto y descarnado: el habito empapado hasta los pies, roto
  por abajo y con algas, manto y capa, la capucha honda con la calavera en sombra y los ojos de fuego de almas —capa de
  brillo—, el cordel con la bolsa y la sarta de monedas; en la derecha el remo largo con un garfio de hierro atado a la pala;
  en la izquierda, colgando de su cadena, la linterna de almas —el vidrio luce—; escala 1,55, `BarqueroAnims.ESCALA`;
  `client/enemigos/BarqueroModelo` y `BarqueroRenderer`, que apaga el brillo mientras muere). 900 de vida, golpe 12,5,
  armadura 9; no se ahoga. Espera **de pie en su barca con la linterna en alto** (postura `REZA` = el bucle `vigia`; el puesto
  0 del sitio `barca` de `MorvainEnemigos`, grupo `Puestos.JEFE`) y de vez en cuando **rema** (`sfx.barquero_remo`, se le oye
  desde el muelle).
- **La pelea**: al verle (16 bloques, con linea de vista) se yergue y dice la frase 1; mientras la dice no se mueve de la barca
  (`distanciaDeseada` 99) ni ataca; al acabarla ruge y **salta** de la barca a las tablas (`SALTO`, como el del Verdugo:
  despega en el tick 9 y cae en el 20 en un arco que no choca con nada, donde quepa y no haya agua; golpe alrededor al caer y
  una ola por las tablas); sube de un salto siempre que esta mas abajo que su presa (en el agua, en la barca). Despues:
  **remazo** (el remo de plano delante: ola de agua negra; saltando o rodando se libra), **barrido** a media altura (no se
  salta), **golpe de linterna** (de cerca o a quien se le pone detras: quema 4 s), el **bichero** a quien esta a 4-6,8 bloques
  (lo que llega el remo: si le entra —rodar o cubrirse libran— el garfio engancha, tira de el hasta sus pies y enseguida el
  golpe de linterna), la **linterna** (la alza y el fuego de almas se abre en un **anillo** por el suelo, de 1 a 9 bloques en
  ~20 ticks: a quien pilla de pie en su borde le quema; saltando se libra; a media vida, dos anillos seguidos) y un **salto** a
  quien se aleja. A **media vida** grita y **llama a sus pasajeros**: los podridos que siguen flotando en la laguna a 30
  bloques se alzan y van a por el jugador que tengan mas cerca; si no hay bastantes (dos, uno mas por amigo, hasta cuatro),
  suben otros del agua a las tablas alrededor de el (efimeros; podrido, fatuo y, el cuarto, hinchado). `Barquero.mediaVida()`
  (pruebas) lo hace ya. Acabada la pelea lejos de su barca, vuelve a ella de un salto.
- **Voz** (ElevenLabs, `eleven_v4`, **"Gravel Midnight - Deep Character Voice"** `M5E055lOUxMi0kJpGyE9`, flow
  `MwR3v95olYFzccpu1TzZ`; 5 frases, tomas a; en `procesar.py` `voz_barquero`: algo mas grave, una segunda voz una octava por
  debajo muy baja, un poco de raspa y el eco largo y apagado sobre el agua): linea **80 + N** (`Voces.BASE_BARQUERO`,
  `esBarquero`; `habla_barquero` en `rey_voces.json`; subtitulos `darkcraft.barquero.voz.N`): 1 al empezar (no la repite en 4
  min), 2 al llamar a sus pasajeros, 3 si cae su presa, 4 la primera vez que engancha a alguien con el bichero y 5 al morir.
- **Barra** (`GuardianCliente`, arte de `tools/hud/barra_guardianes.py` "barquero": el remo tumbado con anillas de hierro, la
  pala en la punta derecha y el garfio en la izquierda, cadenitas con monedas colgando y la linterna de almas en el centro;
  relleno de agua negra con ondas y destellos de almas; gotas, una moneda y chispas de almas). **Musica**: "Demon Lord" de
  Scrabbit (CC-BY 4.0, id `barquero`, tema 35 de `ClientMusic.boss`).
- **Al caer** (lo reparte `valdrenn/Guardianes`): el cartel de JEFE ABATIDO con 9500 ecos, la hazana `barquero` ("La ultima
  travesia", con su entrada del diario) y el bestiario; deja dos piedras de forja II, una III y la ceniza de las llamas.
  **La laguna queda en paz** (`LagunaNegra.liberar`, desde `Puestos.alCaer`): los pasajeros que siguen flotando se deshacen en
  almas y los **faroles de almas** de la laguna (`faroles`: postes, muelle, camino, la proa) **se vuelven faroles de verdad**
  (`LagunaNegra.paz` lo repasa cada 5 s con alguien cerca); los pasajeros **ya no vuelven**: el sitio `laguna` se registra con
  `activo` = los del grupo `pasajeros` solo mientras el Barquero no este muerto (`LagunaNegra.enPaz`). Los dos del muelle siguen.
- **Los puestos de la laguna** (`MorvainEnemigos`, sitio `laguna`, 7, de `LagunaNegra.esperas`): dos flotando junto al muelle
  (se alzan al pasar) y sus pasajeros: cuatro flotando alrededor del embarcadero, a seis o siete de sus tablas (no se alzan con
  la pelea: los llama el), y el del fuego fatuo de pie en el islote del norte, entre las estacas.
- Sonidos (ElevenLabs, flow `hDtV8BlS51uvJUsEJk6K`, `TOMAS_SFX`): `barquero_grito` (3 y 4), `barquero_remazo` (3 y 1),
  `barquero_barrido` (4 y 1), `barquero_bichero` (4 y 2), `barquero_linterna` (1 y 4), `barquero_dolor` (2, 3 y 1),
  `barquero_muerte` (4 y 2), `barquero_remo` (2 y 4; empieza en el segundo 0,8 de la toma: `INICIO`).
- Comandos `/darkcraft laguna [ir <sitio> | enemigos [volver | fuera | sacar]]` y `/darkcraft barquero [ir | vencer | volver]`
  (sin mas: como esta y los faroles; `volver` le pone en su barca, vuelven sus pasajeros y los faroles de almas).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_laguna carpeta` (`-Dcaja=...`): la laguna, el embarcadero, el muelle, la
  barca y el camino con las marcas de los podridos (P); si se llega a pie desde la entrada (junto al santuario), cuantas tablas
  del embarcadero se pisan, que el Barquero cabe en su barca y que ningun podrido esta metido en un bloque.
- Pruebas: servidor, lineas `Morvain, la laguna negra: ...` (el arcon de la barca, el agua, las tablas del embarcadero, los
  faroles; vistas `morvain_laguna*.png`), `Morvain, la laguna negra, sus podridos: ...`, `Morvain, el Barquero: ...` (sale de
  vigia en su barca; al verle se yergue, ruge al acabar la frase y salta a las tablas sin meterse en nada; cada accion acaba;
  los golpes a un marchito; el bichero engancha, arrastra y quema; el anillo llega de pie y saltando no), `Morvain, el
  Barquero a media vida: ...` (se alzan los que flotan; sin ellos suben del agua a las tablas), `Morvain, el Barquero cae: ...`
  (la laguna en paz, los faroles de verdad, ya no salen pasajeros) y `Morvain, el Barquero vuelve: ...`; cliente, tramo
  `barquero()` despues de la aldea de los pilotes: la laguna desde el santuario (`cliente_mv121`), el embarcadero desde el muelle
  (`mv122`), el Barquero de vigia (`mv123`), en supervivencia habla (`mv124`), ruge con su barra (`mv125`), salta (`mv126`) y
  cae en las tablas (`mv127`), sus golpes (`mv128..mv131`: remazo, barrido, golpe de linterna, el anillo), el bichero y la
  linterna (`mv132`, `mv133`), llama a sus pasajeros (`mv134`), cae (`mv135`), el cartel (`mv136`), los faroles en paz (`mv137`)
  y el bestiario (`mv138`).

### Plan grande, parte 7k: las Montanas Nevadas — los Helados y el Monasterio de la Helada

Respuestas 125, 131 ("iglesias y ruinas") y 132 del plan; la cuarta region con su contenido (despues, la **7l**: el jefe de las
montanas, el abad Hrodric —el del libro 21: "subio a la cumbre a pedirle al frio que nos perdonara... bajar de noche, mas alto
que la torre, con una mitra de hielo"—).
- **El helado** (`enemigos/Helado`, entidad `darkcraft:helado`, 0,7 x 2,15; modelo de `tools/enemigos/helado.py`: piel azul gris con
  escarcha a motas y a grumos —`_azar01`, sin el hash de Python—, la barba de hielo, los ojos azul blanco en la capa de brillo y
  carambanos en los antebrazos; brazo de 14: la mano llega a unos 11 de su hombro; `HeladoGeometria/Animaciones`, `HeladoAnims`,
  `client/enemigos/HeladoModelo` y `HeladoRenderer`, escala 1,1; el penitente, un 6 % mas). 32 de vida (el penitente +45 %), golpe
  5,5, armadura 3. Tres: **monje** (capucha, habito con escapulario rojo bordado; el **incensario** de hierro colgando de su cadena
  —la pieza gira en la mano para voltearlo— y el **aliento**: un soplo blanco de 5 bloques y 50 grados que hiela un poco cada tick a
  quien este en el, con el dano de frio y lentitud la primera vez), **penitente** (el torso al aire con cadenas cruzadas, cristales
  de hielo en la espalda que lucen, martillo de hielo a dos manos: el **martillazo** hace brotar cuatro **puas de hielo** en fila
  delante —`EfectosRaiz.estaca` con hielo compacto y azul, el dano un instante despues, mas alla del martillo— y **barrido**) y
  **peregrino** (sombrero de ala ancha nevado, capa de viaje, baston con un cristal de hielo que luce: **bastonazo** de punta y, de
  4 a 16 bloques, tres **esquirlas**: `enemigos/Esquirla`, casi rectas y rapidas, sin modelo —escarcha y copos—, dano de frio,
  hielan y frenan; a los enemigos no les dan). **El frio**: lo suyo hiela (`Helado.helar`: `setTicksFrozen`, la escarcha en la
  pantalla, frena y, helado del todo, duele; el fruto de la escarcha lo quita, `Resistencias`; la armadura de cuero tambien, como en
  la nieve en polvo) y el dano de frio (`Helado.frio`: `DamageTypes.FREEZE` con quien lo hace) lo frenan el fruto y el talisman de
  la escarcha. **A ellos el frio no les hace nada y el fuego un 60 % mas.** Esperan **de rodillas** helados rezando (`REZA`, un bucle
  por variante: el incensario colgando, el martillo al hombro, el baston de pie) y al acercarse **rompen el hielo** (trozos de hielo,
  `sfx.helado_romper`) o **enterrados** boca arriba en la nieve (`TUMBADO`, el dibujante los hunde 0,2; se alzan de golpe). Al morir
  se quedan de rodillas, vencidos, y **se rompen en esquirlas** (`tickDeath` propio: sin el humo de Minecraft). Bestiario
  `helado_monje` / `helado_penitente` / `helado_peregrino`, logro `helado`; a veces una piedra de forja (el penitente, alguna II).
  Contadores para las pruebas: `ALIENTOS`, `PUAS`, `ESQUIRLAS`, `ROTOS`, `HELADOS`, `ESTALLIDOS` y `Esquirla.DAN`.
- **El Monasterio de la Helada** (`morvain/Monasterio`, en las Montanas Nevadas, al noroeste del santuario de la Cumbre Helada; el
  centro del claustro en (3312, -5452); se dibuja como la aldea de los pilotes, chunk a chunk al adornar; en su caja `Adorno` no pone
  nada): una **terraza** cortada en la ladera (`SUELO` = 294; por el norte la montana queda por encima de la muralla, por el sur el
  muro de contencion con contrafuertes) dentro de una **muralla** almenada con nieve (`U0..U1` x `V0..V1`, pilares mas altos en las
  esquinas, carambanos); la **puerta** del sur (cuerpo de puerta con arco, almenas y una cruz de hielo encima) y la **escalera** que
  baja al terreno; el **patio de entrada**; el **claustro** (cuatro arquerias de columnas cada tres con arcos y el portico del patio
  —`arqueria`, columnas donde |k| % 3 == fase—, galerias con techo de pizarra y faroles colgando, el patio nevado y la **fuente
  helada**); la **iglesia** al norte (muros de 11, tejado a dos aguas de pizarra con nieve —`tejado`—, contrafuertes, ocho vidrieras
  azules, alguna rota con la nieve entrando, la puerta con su arco y el roseton; dentro, el pasillo con alfombra roja, seis filas de
  bancos, dos lamparas colgando de la cumbrera, la grada y el presbiterio con el altar y sus velas, la **cruz de hielo**, el **atril con
  el libro perdido 21** —"Lo que vi desde la porteria", del hermano Wendel; `Perdidos.TOTAL` = 21— y un arcon); el **campanario**
  en su esquina del noroeste (la sala de la campana con arcos y la **campana rota**, el chapitel de pizarra y la cruz de hierro); al
  oeste las **celdas** (pasillo y cuatro celdas con su jergon, vela y ventanuco con barrotes), al este el **refectorio** (la mesa larga
  con sus bancos y velas, el hogar frio con la chimenea, la cocina con un caldero de nieve en polvo); en la esquina del noroeste el
  **cementerio** de los monjes (lapidas con su monton de nieve y un arbol muerto) y en la del noreste el **huerto** (el pozo helado,
  los bancales y la lenera); fuera, el **camino** de grava hasta el santuario con faroles. Botin: `chests/monasterio` y
  `monasterio_bueno` (frutos de la escarcha, talisman de la escarcha, hechizo de llamarada, ceniza de las llamas: lo que sirve contra
  los helados). Sitios (`Monasterio.sitio`): `entrada`, `escalera`, `puerta`, `patio`, `claustro`, `fuente`, `iglesia`, `altar`,
  `atril`, `arcon`, `campana`, `celdas`, `refectorio`, `cementerio`, `huerto`. El Espejo: "El Monasterio de la Helada"
  (`Morvain.lugar`); logro `monasterio` al entrar (con un toque de la campana); y de vez en cuando la **campana suena sola** para
  quien este a 140 bloques (`Monasterio.campana`, desde `Morvain.onTick`).
- **Los puestos** (`MorvainEnemigos`, sitio `monasterio`, 31, de `Monasterio.esperas`): peregrinos de rodillas y enterrados por el
  camino y un monje enterrado al pie de la escalera; un penitente en la puerta; en el patio de entrada; en el claustro (uno de rodillas
  ante la puerta de la iglesia, dos de ronda, dos rezando a la fuente y uno enterrado en el patio); en la iglesia, cinco de rodillas
  entre los bancos, el penitente ante la grada y un peregrino en el presbiterio; dos en sus celdas y uno de ronda por el pasillo;
  tres a la mesa del refectorio y un penitente junto a la cocina; dos enterrados en el cementerio y un penitente de rodillas ante las
  tumbas; uno de ronda por el huerto.
- **Las montanas suenan** (`ClientMusic.tickMontana`): en la region de las montanas por encima de 150, `ambiente.montana` (la
  ventisca; una de dos tomas cada vez).
- Sonidos (ElevenLabs, flow `rhWNGHLuqUSprUyMuX2H`, `TOMAS_SFX`): `helado_gemido` (4 y 2), `helado_alerta` (3 y 1), `helado_dolor`
  (2 y 4), `helado_muerte` (4 y 1), `helado_romper` (4 y 1), `helado_incensario` (4 y 3), `helado_aliento` (1 y 2),
  `helado_martillo` (4 y 3), `helado_esquirlas` (1 y 3), `monasterio_campana` (1 y 4), `montana_ambiente` (1 y 2).
- Comandos `/darkcraft monasterio [ir <sitio> | enemigos [volver | fuera | sacar]]` (sin mas: como esta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_monasterio carpeta` (`-Dcaja=...`; para verlo de cerca, `vista.py` con
  `--caja 3286 286 -5486 3338 325 -5426 --zoom 7` y cortes `y=295`, `x=3312`): el monasterio y el camino con las marcas de los
  helados (H); si se llega a pie desde la entrada (junto al santuario) a cada sitio y a cada helado, que ninguno esta metido en un
  bloque (ojo: la alfombra cuenta) y que todos tienen suelo.
- Pruebas: servidor, lineas `Morvain, el monasterio de la helada: ...` (el atril con el libro 21, el arcon, la campana, la fuente
  helada, los bancos, la nieve del patio; vistas `morvain_monasterio*.png`), `Morvain, los helados: ...` (de rodillas, enterrados y de
  pie en su suelo, ninguno metido en un bloque) y `Morvain, los helados en accion: ...` (el de rodillas rompe el hielo y el enterrado
  se alza; cada accion acaba; el aliento hiela y frena; las puas alcanzan a 4,4, mas alla del martillo; las esquirlas dan y hielan;
  el frio no les hace nada y el fuego mas; al morir se rompen y se van) —**las presas de las pruebas son aldeanos sin IA**: las
  esquirlas (y el fuego fatuo de los podridos) no dan a los enemigos, y a los marchitos no les entra el veneno (no muertos)—;
  cliente, tramo `monasterio()` despues del Barquero: desde el santuario de la Cumbre Helada (`cliente_mv139`), entrar por la puerta
  con el cartel, el logro y la ventisca (`mv140`), el claustro desde lo alto (`mv141`), la iglesia (`mv142`), el refectorio (`mv143`),
  el campanario desde el cementerio (`mv144`), en supervivencia uno rompe el hielo (`mv145`), el incensario (`mv146`), el aliento que
  hiela (`mv147`), el martillazo con sus puas (`mv148`), las esquirlas (`mv149`), uno enterrado que se alza (`mv150`, `mv151`), uno
  que se rompe al morir (`mv152`), el libro del hermano Wendel (`mv153`) y el bestiario (`mv154`). **Antes del codice y del menu, sin
  frio, sin efectos y en creativo**: cada golpe (del veneno, del frio) cierra las pantallas (asi salieron vacias las `mv119`/`mv120` de
  la build 145).

### Plan grande, parte 7l: Hrodric, el Abad de la Cumbre (el jefe de las Montanas Nevadas)

Respuestas 125 y 131 del plan; el cuarto jefe de Morvain (el del libro perdido 21: "subio a la cumbre a pedirle al frio que nos
perdonara, y el frio le contesto... y no te arrodilles nunca en esta casa").
- **La escalera de los penitentes y la Ermita de la Cumbre** (`morvain/Ermita`, en las Montanas Nevadas, al norte del monasterio;
  el centro del calvario en (3326, -5612), `SUELO` = 385; se dibuja chunk a chunk al adornar, despues del monasterio; en su caja
  `Adorno` no pone nada): la **escalera** sale del **postigo** del huerto del monasterio (`Monasterio.postigo`: un hueco en la
  muralla del norte con su dintel) y sube recta al norte (x = 3326, tres de ancho, de -5484 a -5596; `Ermita.escalon(z)`: nunca
  sube mas de uno por fila —una pasada hacia arriba con la ladera y otra hacia abajo para llegar al calvario—, peldanos de
  `stone_brick_stairs`); donde va por debajo de la ladera, **entre muros** cortados en la roca (con hornacinas con farol cada doce y
  **estaciones**: cruces de hielo metidas en el muro), donde va por encima, sobre su relleno con pretil y contrafuertes, y a ras,
  con bordillo y faroles en postes. Arriba, en un circo de la montana, el **calvario**: una terraza redonda (`R_PLAZA` 16, se funde
  con la ladera en 12,5) de sillares con un anillo de andesita, la **rosa de hielo** en el centro (donde reza el abad; sin nieve),
  ocho **pilares** rotos alrededor y dos con dintel en la entrada, un **murete caido** (r 21-22), los **cuatro braseros**
  encendidos en (±9, ±9) (`Ermita.braseros`) y al norte, un escalon mas arriba, la **ermita**: muros de 7, puerta con arco,
  ventanas con barrotes y rosetones, tejado a dos aguas de pizarra, la **espadana** con su campana, dentro el altar con velas, la
  cruz de hielo, reclinatorios, un farol y el **arcon** (`chests/ermita`: piedras de forja II y III, talismanes, hechizos, frutos de
  la escarcha, algun libro encantado); detras, en lo alto, una **gran cruz de hielo** de 18. Sitios (`Ermita.sitio`): `postigo`,
  `escalera`, `mitad`, `corte`, `entrada`, `ante_abad`, `abad`, `calvario`, `ermita`, `puerta_ermita`, `altar`, `arcon`, `cruz`,
  `brasero_0..3`, `cruz_alta`. El Espejo: "La Escalera de los Penitentes" y "La Ermita de la Cumbre" (`Morvain.lugar`).
- **Los helados de la escalera** (`MorvainEnemigos`, sitio `ermita`, 8, de `Ermita.esperas`): seis de rodillas por la escalera
  rezando hacia la cumbre (cada quince o veinte filas: en lo llano en medio; en el tramo empinado en un escalon, a un lado) y dos
  peregrinos enterrados a los lados de la llegada al calvario.
- **El Abad** (`enemigos/Abad`, entidad `darkcraft:abad`, 1,6 x 4,6; hereda de `Guardian`): modelo de `tools/enemigos/abad.py` (un
  viejo gigante helado: la mitra de hielo con sus cristales e infulas, la barba con carambanos, la capa carmesi con capillo, estola y
  broche de oro, el alba blanca con escarcha, cristales de hielo en la espalda, mangas con carambanos; el baculo largo con la voluta
  y un cristal que luce; escala 2, `AbadAnims.ESCALA`; `client/enemigos/AbadModelo` y `AbadRenderer`, que apaga el brillo mientras
  muere). 1100 de vida, golpe 14, armadura 10. **El frio no le hace nada; el fuego, un 50 % mas.** Espera **de rodillas en la rosa
  de hielo, de cara a la ermita, rezando** (postura `REZA`; el puesto 0 del sitio `cumbre`, grupo `Puestos.JEFE`; `sfx.abad_rezo`
  de vez en cuando: se le oye desde la escalera).
- **La pelea**: al verle (16 bloques) dice la frase 1 mientras se levanta y al acabarla ruge. Despues: el **baculazo** (alza el
  baculo y lo estrella delante: del golpe brota una **fila de puas de hielo** por el suelo hasta 12 bloques —`EfectosRaiz.estaca`, el
  dano al brotar; de lado se libra—; a media vida, tres filas en abanico), el **barrido** a media altura (frena), el **pisoton** a
  quien se le pega o se le pone detras (dos anillos de puas), el **aliento de la cumbre** (un soplo de 9 bloques que barre de su
  derecha a su izquierda: hiela, duele y frena) y los **carambanos** (alza el baculo al cielo; bajo cada uno, y en dos o tres sitios
  mas, una marca de escarcha en el suelo; 22 ticks despues cae alli un carambano enorme —`EfectosRaiz.carambano`, un BlockDisplay
  que cae de 13 bloques— y revienta; a media vida, otra tanda). Lo que pega, hiela (`Helado.helar`). A **media vida** grita:
  "¡hermanos, alzaos de la nieve!": se alzan **helados de la nieve** del calvario (efimeros; dos, uno mas por amigo, hasta cuatro) y
  se levanta la **ventisca** (18 s; vuelve cada 16 mientras dura la pelea): quien no este **junto a un brasero** (`Abad.alCalor`,
  3,6 bloques) se va helando y, helado del todo, le duele; junto al fuego se deshiela. `Abad.mediaVida()` y `Abad.ventisca(bool)` /
  `ventiscaSobre(le)` (pruebas).
- **Voz** (ElevenLabs, `eleven_v4`, **"Elder Rowan"** `N8jsIhEtPnj3PWFmH8hZ`, flow `jCsDZ5HeBZc9gQNRbklp`; 5 frases, tomas a; en
  `procesar.py` `voz_abad`: algo mas grave, una segunda voz una octava por debajo muy baja, un poco de brillo frio y el eco largo de
  la cumbre): linea **90 + N** (`Voces.BASE_ABAD`, `esAbad`; `habla_abad` en `rey_voces.json`; subtitulos `darkcraft.abad.voz.N`): 1
  al empezar (no la repite en 4 min), 2 al llamar a sus hermanos, 3 si cae su presa, 4 la primera vez que alguien se hiela del todo
  con la ventisca y 5 al morir.
- **Barra** (`GuardianCliente`, arte de `tools/hud/barra_guardianes.py` "abad": el marco de hielo con carambanos y el baculo;
  relleno de escarcha; caen copos que se mecen). **Musica**: "Final Confrontation" de tcarisland (CC-BY 4.0, id `abad`, tema 36 de
  `ClientMusic.boss`).
- **Al caer** se queda de rodillas apoyado en el baculo y **se rompe en hielo** (`tickDeath` propio); lo demas lo reparte
  `valdrenn/Guardianes`: el cartel de JEFE ABATIDO con 11000 ecos, la hazana `abad` ("El ultimo toque", con su entrada del diario) y
  el bestiario; deja dos piedras de forja III, dos II y la ceniza de la escarcha. **La cumbre queda en paz** (`Ermita.liberar`, desde
  `Puestos.alCaer`): los hermanos que seguian de rodillas en la **iglesia del monasterio** se deshacen en escarcha y la **campana del
  monasterio da su ultimo toque** para todos los de alrededor; ya no suena sola (`Morvain.onTick`) y los de la iglesia **ya no
  vuelven** (el sitio `monasterio` se registra con `activo` = los del grupo `iglesia` solo mientras el Abad no este muerto:
  `Ermita.enPaz`; `Ermita.paz` repasa cada 5 s los que quedaran). Los demas del monasterio siguen.
- Sonidos (ElevenLabs, flow `GbVjOICP5JrNCshfCnzi`, `TOMAS_SFX`): `abad_grito` (3 y 4), `abad_baculazo` (3 y 1), `abad_barrido`
  (3 y 1), `abad_pisoton` (4 y 3), `abad_aliento` (4 y 1), `abad_invocar` (1 y 3), `abad_carambano` (3, 1 y 4), `abad_ventisca`
  (2 y 4, 18 s, `stream`), `abad_dolor` (2, 4 y 1), `abad_muerte` (2 y 3), `abad_rezo` (4 y 3).
- Comandos `/darkcraft ermita [ir <sitio> | enemigos [volver | fuera | sacar]]` y `/darkcraft abad [ir | vencer | volver]` (sin
  mas: como esta; `volver` le pone otra vez de rodillas y vuelven los hermanos de la iglesia).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_ermita carpeta` (con el monasterio debajo): la escalera, el calvario, la
  ermita y la cruz con las marcas de los helados (H); si se llega a pie desde el postigo a cada sitio, la escalera escalon a escalon
  (se pisa y hay hueco encima; ojo: un poco de nieve cuenta como bloque), que ningun helado esta metido en un bloque y que donde reza
  el Abad no estorba nada.
- Pruebas: servidor, lineas `Morvain, la ermita de la cumbre: ...` (los braseros encendidos, el arcon, la campana, la rosa, la cruz,
  el suelo del calvario; la escalera; a pie desde el postigo; vistas `morvain_ermita.png` y `morvain_escalera.png`), `Morvain, la
  escalera de los penitentes: ...`, `Morvain, el Abad: ...` (de rodillas en la rosa; al verle habla, se levanta y ruge al acabar la
  frase; cada accion acaba), `Morvain, los golpes del Abad a un marchito: ...` (el baculazo de cerca y sus puas de lejos, el
  barrido, el pisoton, el aliento, los carambanos), `Morvain, la ventisca del Abad: ...` (lejos de los braseros se hiela y le
  duele; junto a uno, se deshiela), `Morvain, el Abad a media vida: ...` (se alzan sus hermanos, la ventisca; el frio no le hace
  nada y el fuego mas), `Morvain, el Abad cae: ...` (la cumbre en paz; los de la iglesia descansan y ya no salen) y `Morvain, el
  Abad vuelve: ...`; cliente, tramo `abad()` despues del monasterio: la escalera desde el postigo (`cliente_mv155`), a media subida
  (`mv156`) y entre muros (`mv157`), llegar al calvario con el cartel (`mv158`), el calvario desde lo alto (`mv159`), el Abad
  rezando (`mv160`), la ermita (`mv161`), la cruz (`mv162`), en supervivencia habla (`mv163`) y ruge con su barra (`mv164`), sus
  golpes (`mv165..mv169`: baculazo, barrido, pisoton, aliento, carambanos), llama a sus hermanos (`mv170`), la ventisca (`mv171`),
  junto al brasero (`mv172`), cae de rodillas (`mv173`) y se rompe (`mv174`), el cartel (`mv175`), la iglesia en paz (`mv176`) y el
  bestiario (`mv177`).

### Plan grande, parte 7m: la Capital en ruinas — los Calcinados y la Puerta Oeste

Respuestas 125, 131 y 132 del plan; la quinta region con su contenido (despues, la **7n**: el jefe de la capital, el Condestable
del Juramento —el que mando cerrar las puertas la noche del cielo rojo, libro 22—).
- **El calcinado** (`enemigos/Calcinado`, entidad `darkcraft:calcinado`, 0,7 x 2,05, `fireImmune`; modelo de
  `tools/enemigos/calcinado.py`: carne de carbon con grietas de brasa que lucen —`_grietas`, en la capa de brillo con los ojos, el
  filo de la espada, la mecha y la llama—; `CalcinadoGeometria/Animaciones`, `CalcinadoAnims`, `client/enemigos/CalcinadoModelo` y
  `CalcinadoRenderer`, escala 1,05). 30 de vida, golpe 5,5, armadura 3. Tres: **guardia** (la guardia del Juramento: yelmo con
  cimera, peto, sobreveste roja, espada al rojo y **escudo de torre** en el brazo izquierdo; +40 % de vida y +5 de armadura: **tajo**
  que quema y **escudazo** que empuja y frena; de pie, sin atacar y de frente —70 grados a cada lado— **el escudo** deja pasar el 20 %
  y las flechas nada —`ESCUDO`, `cubre`, accion `BLOQUEO`—; por la espalda o mientras ataca, todo), **incendiario** (vecinos con
  trapo en la cabeza y delantal, cantarillos al cinto y el **cantaro de brea** con su mecha en la mano: de 4,5 a 15 bloques lo
  **lanza** en arco —`enemigos/Cantaro`, un `ThrowableItemProjectile` con el dibujo del jarron decorado; `Cantaro.tiro` apunta con la
  caida; no da a los enemigos— que revienta en barro y fuego y deja **llamas** en el suelo —`Cantaro.Llamas`, una nube sin efectos de
  radio 1,8 que encoge en 3,5 s y quema cada medio segundo a quien este dentro (no a los calcinados)—; de cerca, la **tea**) y
  **ceniciento** (harapos y ceniza: **zarpazo** y **agarron** —de 2 a 4,4 bloques se echa encima: sujeta (lentitud IV) y quema—;
  al morir **revienta en ceniza**: ciega y frena a los jugadores a 2,6 y deja una nube que ciega). Lo que pega, quema
  (`Calcinado.quemar`). **El fuego no les hace nada (ni la lava); el frio, un 60 % mas; el agua les duele** (`isSensitiveToWater`);
  la ceguera no les entra (si la lentitud: la ceniza de la escarcha sirve). Esperan **agazapados** junto a los rescoldos (`REZA`) o
  **enterrados en los montones de ceniza** (`TUMBADO`, el dibujante los hunde 0,25 y suben al alzarse) o de pie. Al morir se
  deshacen en carbon y ceniza (`tickDeath` propio). Bestiario `calcinado_guardia` / `calcinado_incendiario` /
  `calcinado_ceniciento`, logro `calcinado`; a veces una piedra de forja (la guardia, alguna II). Contadores para las pruebas:
  `BLOQUEOS`, `CANTAROS`, `QUEMADOS`, `AGARRONES`, `CENIZAS`, `ALZADOS` y `Cantaro.REVIENTAN`, `DAN`, `LLAMAS_QUEMAN`.
- **La Puerta Oeste de la Capital** (`morvain/Capital`, en la Capital en ruinas, al este del santuario de la Puerta de la Capital; la
  muralla en x = 3240, el camino en z = 345, `SUELO` = 93; coordenadas de la ciudad u = x - CX, v = z - CZ; se dibuja chunk a chunk al
  adornar; en su caja `Adorno` no pone nada): la **muralla** de norte a sur (u -2..1, sillares ennegrecidos con hollin —`piedra`—,
  almenas por fuera, pretil por dentro, el adarve en `PISO + 8`), **cuatro torres** huecas con su escalera de mano (la de v -26
  rota), **dos trozos caidos** con rampas de escombros (`CAIDOS`) y las escaleras de subida al adarve pegadas por dentro; la
  **puerta**: dos torres (u -5..5) con el paso abovedado, el **rastrillo** medio bajado (barrotes en u -3, alguno doblado), las hojas
  quemadas caidas por dentro, estandartes negros y faroles de almas; encima la **sala de la guardia** (se sube por la escalera de la
  torre del norte —la puerta de abajo por dentro, al este—): la mesa, el caldero, el torno con las cadenas del rastrillo, el arcon y el
  **atril con el libro perdido 22** —"La guardia de la Puerta Oeste", del sargento Brannoc; `Perdidos.TOTAL` = 22—. Fuera, el
  **camino** del santuario con faroles y **cuatro carros quemados**. Dentro, la **calle mayor** (u 2..96, |v| <= 3) entre **casas
  quemadas** (`CasaCapital`: las de Brumavela ennegrecidas; `ruina`: casi todas con el tejado hundido, alguna en pie y alguna en
  ruinas), la **calle del cruce** (u 44), la ronda pegada a la
  muralla, **solares** con cimientos y arboles quemados, y al final la **plaza** (radio 15) con el pedestal y **la estatua del
  caballero derribada** (las piernas en pie, el torso, el yelmo y el brazo de la mano alzada en el suelo), los puestos quemados del
  mercado y, al norte, el **cuartel del Juramento** (u 84..108, v -46..-24: el tejado hundido, literas quemadas, el armero, la mesa
  larga, el arcon bueno; delante, muñecos de practicar). **Montones de ceniza** (diez: alli esperan los cenicientos), escombros y
  rescoldos por todas partes. Botin: `chests/capital` y `capital_bueno` (frutos de la escarcha y de la brasa, el talisman de la brasa,
  la ceniza de la escarcha: lo que sirve contra ellos). Sitios (`Capital.sitio`): `entrada`, `puerta`, `paso`, `camara`, `atril`,
  `arcon_camara`, `adarve`, `calle`, `cruce`, `plaza`, `estatua`, `cuartel`, `dentro_cuartel`, `arcon`. El Espejo: "La Puerta Oeste
  de la Capital" (`Morvain.lugar`, `Capital.dentro`); logro `capital` al cruzar la puerta (`Capital.intramuros`).
- **Los puestos** (`MorvainEnemigos`, sitio `capital`, 31, de `Capital.esperas`): dos cenicientos enterrados junto a los carros y
  una guardia en el paso; dos incendiarios en el adarve de cara al camino y una guardia de ronda; uno agazapado junto al atril;
  por la calle mayor tres cenicientos en los montones, dos guardias de ronda y un incendiario agazapado; cinco en la calle del cruce;
  siete en la plaza; cuatro en el cuartel; y dos agazapados dentro de las casas (junto a la puerta).
- **La capital suena** (`ClientMusic.tickCapital`): en la region de la capital por debajo de 170, `ambiente.capital` (viento,
  brasas, madera que cruje y algun derrumbe lejos; una de dos tomas cada vez).
- Sonidos (ElevenLabs, flow `ztrM0NNznpPby1X4ViYn`, `TOMAS_SFX`): `calcinado_gemido` (4 y 2), `calcinado_alerta` (1 y 3),
  `calcinado_dolor` (3, 4 y 1), `calcinado_muerte` (3 y 2), `calcinado_alzarse` (1 y 2), `calcinado_tajo` (4 y 2),
  `calcinado_escudo` (3 y 2), `calcinado_cantaro` (3 y 4), `calcinado_fuego` (2 y 4; suena una vez al reventar: dura lo que las
  llamas), `calcinado_ceniza` (2 y 4), `capital_ambiente` (1 y 4, `stream`).
- Comandos `/darkcraft capital [ir <sitio> | enemigos [volver | fuera | sacar]]` (sin mas: como esta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_capital carpeta`: la puerta y el barrio con las marcas de los calcinados
  (Cn); si se llega a pie desde la entrada a cada sitio (salvo lo alto de la estatua) y a cada calcinado (**las alfombras de ceniza
  no cuentan como bloque**), que ninguno esta metido en un bloque y que todos tienen suelo.
- Pruebas: servidor, lineas `Morvain, la puerta oeste de la capital: ...` (el atril con el libro 22, los arcones, el rastrillo, la
  estatua, la calle libre; a pie desde el camino; vistas `morvain_capital.png` y `morvain_capital_puerta.png`), `Morvain, los
  calcinados: ...`, `Morvain, los calcinados en accion: ...` (el agazapado se levanta y el enterrado se alza; cada accion acaba) y
  `Morvain, los calcinados contra una presa: ...` (el escudo de frente, la flecha y por la espalda; el tajo quema; el cantaro revienta
  y sus llamas queman; el agarron frena y quema; el fuego y la lava nada, el frio mas; el ceniciento revienta en ceniza); cliente,
  tramo `capital()` despues del Abad: desde el santuario (`cliente_mv178`), el camino con los carros (`mv179`), entrar por la puerta
  con el cartel, el logro y el ruido de las ruinas (`mv180`), el paso del rastrillo (`mv181`), la sala de la guardia (`mv182`), el
  adarve (`mv183`), la calle mayor (`mv184`), la plaza (`mv185`), el cuartel (`mv186`), todo desde lo alto (`mv187`), en
  supervivencia el escudo (`mv188`) y el tajo (`mv189`), el cantaro en el aire (`mv190`) y sus llamas (`mv191`), un ceniciento
  enterrado (`mv192`) que se alza (`mv193`), su agarron (`mv194`), la nube de ceniza al morir (`mv195`), el libro del sargento Brannoc
  (`mv196`) y el bestiario (`mv197`).

### Plan grande, parte 7n: Ser Halvard, el Condestable del Juramento (el jefe de la Capital en ruinas)

Respuestas 125 y 131 del plan; el quinto jefe de Morvain (el del libro perdido 22: el que mando cerrar todas las puertas de la
capital la noche del cielo rojo para que el fuego no saliera, y la gente ardio en las calles).
- **El Patio del Juramento** (`morvain/Juramento`, al este de la plaza de la capital, mismas coordenadas que `Capital`: u = x - 3240,
  v = z - 345, `PISO` = 94; se dibuja chunk a chunk al adornar, despues de la capital; en su caja `Adorno` no pone nada): la **calle
  del Juramento** sigue la calle mayor desde la plaza (`Capital.calle` la deja libre hasta u 118; `Juramento` de u 119 a la puerta,
  con escombros, montones de ceniza y un ceniciento enterrado) hasta el **cuerpo de la puerta** (u 128..135, el paso abovedado de
  cinco con el **rastrillo** del mod —`DarkCraftMod.RASTRILLO`, eje Z— subido en u 132: se le ven los dientes; estandartes y faroles).
  Dentro, el **patio** (u 136..184, v ±24) cerrado por muros de 9 con almenas y **cuatro torres de esquina**; en medio el **estrado**
  con la mano alzada en el suelo, la **piedra del Juramento** (u 168, oro en lo alto) con su **asta y bandera** (u 172), **cuatro
  braseros** encendidos en (u 160 ±11, ±11) y seis **montones de ceniza** (`Juramento.montones`). Al este, fuera del muro, la **torre
  del Condestable** (u 187..199, v ±6, 20 de alto): su **reja** (u 186) esta bajada mientras el viva; dentro la mesa larga con el mapa
  y las velas, el hogar, el yunque, barriles, el **arcon** (`chests/juramento`: piedras de forja II y III, talismanes de la brasa, el
  hueso, la furia o el vigor, la ceniza del grito, del torbellino o de las llamas, hechizos, frutos de la escarcha, la brasa y la
  ceniza, oro y diamantes) y el **atril con el libro perdido 23** —"La ultima orden del Condestable", de Ser Halvard;
  `Perdidos.TOTAL` = 23—; una escalera de mano sube a lo alto (un brasero de senales). Sitios (`Juramento.sitio`): `calle`,
  `puerta`, `rastrillo`, `patio`, `ante_condestable`, `condestable`, `piedra`, `brasero_0..3`, `torre`, `dentro_torre`, `arcon`,
  `atril`, `alto_torre`. El Espejo: "El Patio del Juramento" (`Juramento.dentro`, antes que la Puerta Oeste); logro `juramento`
  ("Y nadie sale") al pisar el patio (`Juramento.enPatio`, pasado el rastrillo).
- **El rastrillo** (`Juramento.tick`, cada tick con alguien a 140 del patio, desde `Morvain.onTick`, con su propio reloj: en las
  pruebas del servidor no corre el del mundo): **cae de golpe** (una fila por tick, y antes saca hacia dentro a quien este debajo) en
  cuanto el Condestable empieza a pelear —detras de quien entro: no se sale— y **se alza despacio** (una fila cada 16 ticks, con el
  torno) cuando acaba: si caen todos o se van, al volver el a su sitio; si cae el, **50 ticks despues**, mientras dice "abrid las
  puertas" (`cerrar` guarda al ultimo Condestable visto: caido, `Puestos.enemigo` ya no lo da). Lo bajado se mira en el mundo (si el
  servidor se para a media pelea, al volver se alza solo). `CAIDAS` / `ALZADOS`, `rastrilloBajado`, `alzarYa` (pruebas).
- **Al caer el Condestable** (`Juramento.paz`, cada segundo; tambien con alguien a 100 de la Puerta Oeste aunque el patio quede
  lejos): se alza la reja de su torre, **se abre por fin el rastrillo de la Puerta Oeste** (lo que quedaba bajado desde la noche
  del cielo rojo, `abrirPuertaOeste`) y los de la Puerta Oeste que seguian esperando (grupos `fuera`, `puerta`, `muralla`, `camara`:
  `Juramento.DESCANSAN`) se deshacen en ceniza y **ya no vuelven** (el sitio `capital` se registra con `activo`). `deshacerPaz`
  (comando y pruebas) lo deja como estaba.
- **Los puestos** (`MorvainEnemigos`, sitio `juramento`, de `Juramento.esperas`): el Condestable (puesto 0, grupo `Puestos.JEFE`),
  dos guardias calcinadas de rodillas a la puerta y un ceniciento enterrado en la calle.
- **El Condestable** (`enemigos/Condestable`, entidad `darkcraft:condestable`, 1,6 x 4,6, `fireImmune`; hereda de `Guardian`):
  modelo de `tools/enemigos/condestable.py` (un caballero gigante con la armadura quemada y grietas de brasa que lucen, yelmo con
  cresta y cimera, gola, coraza, faldar, la sobreveste negra con **la mano alzada en oro**, capa, hombreras, guanteletes, grebas y
  el **montante** de dos manos con los filos al rojo; escala 2; `client/enemigos/CondestableModelo` y `CondestableRenderer`, el
  brillo late y se apaga al morir). 1024 de vida (el tope del juego), golpe 15, armadura 14 (dureza 8), no se le empuja. **El fuego
  no le hace nada; el frio, un 60 % mas; el agua le duele.** Espera **de rodilla en su estrado** (`REZA`, mirando a la puerta) con
  el fuego quieto de su armadura (`sfx.condestable_espera` de vez en cuando).
- **La pelea** (solo con quien esta dentro del patio: `percibe` y `objetivoValido`): al verle dice la frase 1 mientras se levanta y
  ruge al acabarla. El **tajo** (manda el fuego por el suelo en linea hasta 13 bloques —`Cantaro.Llamas` una tras otra, quema a quien
  pillan al salir—; a media vida y con furia, tres lineas en abanico), el **barrido** ancho (frena; con furia deja llamas), la
  **embestida** (de 6,5 a 17 bloques cruza el patio arrollando; si da contra un muro se queda **aturdido**), la **llamarada** a quien
  se le pega o se le pone detras (clava el montante y revienta un **anillo de fuego** de 8,5: saltandolo no quema) y **alzar** el
  montante al cielo: desde lo alto de los muros (`Juramento.almenasDeDentro`) cae **brea** ardiendo —un `Cantaro` hacia cada uno y a
  dos o tres sitios mas, con la marca en el suelo mientras vuela—. Lo que pega, quema. A **media vida** toca el **cuerno**: "¡Guardia,
  a mi!" y de los montones de ceniza se alza **su guardia** (calcinados efimeros; dos, uno mas por amigo, hasta cuatro). **El
  juramento**: el primer golpe de muerte de cada pelea no le mata —`setHealth` lo para en 1—: hinca la rodilla ("Jure... ¡no
  morir!"; mientras, no le entra nada), se cura hasta el 30 % y se alza con un anillo de fuego de 11 y **furia** (mas rapido, lineas
  de fuego mas largas). `Condestable.mediaVida()`, `vencer()` (sin jurar) y contadores para las pruebas.
- **Voz** (ElevenLabs, `eleven_v4`, **"Jerry B. - Military Commander"** `TxWZERZ5Hc6h9dGxVmXa`, flow `wtXHOi3XBvDIwjFd82AX`; 5
  frases, tomas a; en `procesar.py` `voz_condestable`: algo mas grave con una voz una octava por debajo, el eco metalico del yelmo,
  un poco de distorsion y el eco del patio): linea **20 + N** (`Voces.BASE_CONDESTABLE`, `esCondestable`; `habla_condestable` en
  `rey_voces.json`; subtitulos `darkcraft.condestable.voz.N`): 1 al empezar (no la repite en 4 min), 2 al tocar el cuerno, 3 si cae
  su presa, 4 al jurar y 5 al morir ("Abrid... abrid las puertas. Que vuelvan a casa").
- **Barra** (`GuardianCliente`, arte de `tools/hud/barra_guardianes.py` "condestable": el montante con la cruz y el pomo de oro, el
  yelmo en el centro y las manos de oro; relleno de brasas; suben brasas). **Musica**: "Massive Battle" de Eldritch Grim (CC0, id
  `condestable`, tema 37 de `ClientMusic.boss`).
- **Al caer** se queda de rodilla apoyado en el montante, dice su ultima frase, se apaga y **se deshace en ceniza** (`tickDeath`
  propio, 130 ticks); lo demas lo reparte `valdrenn/Guardianes`: el cartel de JEFE ABATIDO con 12500 ecos, la hazana `condestable`
  ("Que vuelvan a casa", con su entrada del diario) y el bestiario; deja tres piedras de forja III, dos II y la ceniza del grito.
- Sonidos (ElevenLabs, flow `wtXHOi3XBvDIwjFd82AX`, `TOMAS_SFX`): `condestable_grito` (4 y 2), `condestable_tajo` (4 y 1),
  `condestable_barrido` (4 y 1), `condestable_embestida` (3 y 2), `condestable_llamarada` (2 y 1), `condestable_rastrillo` (4 y 1),
  `condestable_abrir` (1 y 4), `condestable_cuerno` (4 y 2), `condestable_alzar` (3 y 2), `condestable_dolor` (3, 1 y 4),
  `condestable_muerte` (2 y 4), `condestable_espera` (1 y 3).
- Comandos `/darkcraft condestable [ir | patio <sitio> | vencer | volver | rastrillo [subir]]` (sin mas: como esta; `volver` le pone
  otra vez de rodilla, vuelve a bajar la Puerta Oeste y la reja de su torre y vuelven los de la puerta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_juramento carpeta` (con la capital al lado): la calle, la puerta, el patio y
  la torre con las marcas de los puestos (K el Condestable, C los calcinados); si se llega a pie a cada sitio (la torre, solo sin la
  reja), que ninguno esta metido en un bloque y que el estrado y el paso del rastrillo estan libres.
- Pruebas: servidor, lineas `Morvain, el patio del Juramento: ...` (el atril con el libro 23, el arcon, la reja, los dientes del
  rastrillo, la piedra, los braseros, el suelo del patio; a pie desde la plaza; vista `morvain_juramento.png`), `Morvain, el
  Condestable: ...` (de rodilla; al verle habla, se levanta, cae el rastrillo —y saca del paso al que estaba debajo— y ruge al acabar
  la frase; cada accion acaba), `Morvain, los golpes del Condestable a un marchito: ...` (el tajo de cerca y su linea de fuego de
  lejos, el barrido, la llamarada por la espalda, la embestida, la brea; contra el muro se estrella), `Morvain, el Condestable a
  media vida: ...` (la guardia que se alza; el fuego nada, el frio mas), `Morvain, el juramento del Condestable: ...`, `Morvain, el
  Condestable cae: ...` (el rastrillo se alza, la Puerta Oeste se abre, los de la puerta descansan y no vuelven) y `Morvain, el
  Condestable vuelve: ...`; cliente, tramo `juramento()` despues de la capital: la calle (`cliente_mv198`), la puerta y su guardia
  (`mv199`), entrar a pie con el cartel y el logro (`mv200`), el patio desde lo alto (`mv201`), el Condestable de rodilla (`mv202`), en
  supervivencia habla (`mv203`), el rastrillo cae a la espalda (`mv204`) y ruge con su barra (`mv205`), sus golpes (`mv206..mv210`:
  tajo, barrido, embestida, llamarada, alzar), el cuerno y su guardia (`mv211`), jura de rodilla (`mv212`) y se alza con furia
  (`mv213`), cae de rodilla (`mv214`), el rastrillo se alza (`mv215`), se deshace en ceniza (`mv216`), el cartel (`mv217`), la Puerta
  Oeste abierta (`mv218`), la torre por dentro (`mv219`), el libro de Ser Halvard (`mv220`) y el bestiario (`mv221`). Con este tramo
  la prueba del cliente pasa de la hora: `LIMIT` 66 min (`lanzar.sh` 72 y 70 min, el paso 100, el trabajo 145).

### Plan grande, parte 7o: la Academia Espectral — los Eruditos y la Academia

Respuestas 125, 131 y 132 del plan; la sexta region con su contenido (despues, la **7p**: el jefe de la academia, la Rectora
Ysolde en el observatorio —"subio a leer el fuego que caia... y el cielo la lee a ella", libro 24—).
- **El erudito** (`enemigos/Erudito`, entidad `darkcraft:erudito`, 0,65 x 2,0; modelo de `tools/enemigos/erudito.py`: toga larga y
  esclavina, la cara en sombra con los ojos lilas en la capa de brillo; `EruditoGeometria/Animaciones`, `EruditoAnims`,
  `client/enemigos/EruditoModelo` y `EruditoRenderer`). **Flotan como los espectros** (`ARRIBA` 3,2 px; ni caen con dano ni hacen
  ruido) y **se dibujan medio transparentes** (`entityTranslucent`, el alfa respira; en el parpadeo se apagan y vuelven; al morir se
  deshacen en paginas y letras). Tres: **aprendiz** (26 de vida, 4,5 de golpe: cartera, calamo con pluma y tintero; **plumazo**, dos
  punaladas con el calamo, y de 4 a 12 bloques el **tintero**: `enemigos/Tinta`, un `ThrowableItemProjectile` con el dibujo del saco
  de tinta que cae en arco —`Cantaro.tiro`— y revienta en un charco de tinta de radio 1,8: ceguera 2,5 s, lentitud y 2 de dano a los
  jugadores), **maestro** (30 y 5: birrete con borla, barba, estola con runas que lucen, vara de cristal y libro; se queda lejos: el
  **orbe** —`enemigos/Orbe`, sin gravedad, solo particulas (`NoopRenderer`), sigue a su presa girando 0,1 por tick a 0,55 bloques por
  tick durante 4,5 s y hace dano magico; se rompe al darle—, las **runas** —un circulo lila bajo los pies de su presa que revienta a
  los 22 ticks (`RUNAS_TICKS`, radio 1,8): dano x1,3 y la lanza por el aire; hay que salir— y el **parpadeo** —si se le acercan a
  menos de 3,2 (o a veces tras un golpe) se apaga y aparece a 6-9 bloques, de espaldas a su presa—) y **custodio** (70, 7 y 4 de
  armadura; capucha, llaves al cinto y el **tomo encadenado**, escala 1,25: el **tomazo** —lo estrella contra el suelo: la onda da a
  quien este de pie a 2,8 del golpe, `R_ONDA`; saltando o rodando se libra— y el **cadenazo** —lo voltea con la cadena: llega a 4,8
  y frena—). Esperan **leyendo** (`REZA`: de rodillas con el libro abierto) o **tumbados entre los libros** (`TUMBADO`) o de pie (de
  ronda); al despertar, `ALZARSE` / `LEVANTARSE`. Bestiario `erudito_aprendiz` / `erudito_maestro` / `erudito_custodio`, logro
  `erudito`; a veces una piedra de forja (el custodio, alguna II). Contadores para las pruebas: `TINTEROS`, `ORBES`, `RUNAS`,
  `RUNAS_DAN`, `PARPADEOS`, `TOMAZOS`, `ONDAS`, `CADENAZOS`, `Tinta.REVIENTAN` / `CIEGAN` y `Orbe.DAN`.
- **La Academia Espectral** (`morvain/Academia`, en la meseta de caliza, a la puerta del santuario de la Puerta de la Academia —el
  santuario en (4110, -3176), la puerta del recinto 27 bloques al norte—; `CX` 4110, `CZ` -3250, `SUELO` 242; u = x - CX, v = z - CZ;
  se dibuja chunk a chunk al adornar; en su caja `Adorno` no pone nada; la piedra es la caliza de la meseta —`piedra`: calcita,
  diorita y algo de toba— con zocalos, cornisas, marcos y arcos de pizarra labrada —`oscura`—, tejados de pizarra, vidrieras moradas,
  faroles de almas, amatista y obsidiana llorosa): el **recinto** (|u| <= 36, v -60..44: muralla de 8 con almenas, contrafuertes por
  fuera y cuatro torres de esquina con chapitel); al sur el **cuerpo de la puerta** (v 40..47: el paso de cinco con boveda apuntada,
  dos torres de 20 con chapitel y su aguja con luz, pendones morados, el **ojo que llora** —obsidiana llorosa en su medallon— sobre el
  arco, faroles y las hojas de la puerta: una abierta contra el paso, la otra caida en el camino); fuera, el **camino** hasta el
  santuario. Dentro: el **patio de entrada** (pebeteros de almas y arboles secos); el **claustro** (|u| <= 22, v -2..28: cuatro
  galerias con techo de pizarra, arquerias de columnas de pizarra con arcos apuntados —`arqueria`: cada escalon mira a su columna mas
  cercana; el arco del medio, de cinco, donde entra el sendero—, faroles colgando, bancos; el **jardin** con la cruz de senderos, los
  pebeteros y la **fuente de amatista**: pilon ochavado con agua y el obelisco de amatista con la obsidiana llorosa); al norte la
  **Gran Biblioteca** (|u| <= 16, v -51..-3, muros de 16 con contrafuertes de dos escalones, tejado a dos aguas hasta `SUELO + 33`,
  ventanales de tres con su punta sobre el altillo y lancetas abajo —alguno roto—, dos **rosetones** en los hastiales; dentro la
  alfombra morada del pasillo, **nueve filas de estantes** a cada lado —alguno vacio (estanteria cincelada) o caido; en dos se vino
  abajo el medio—, atriles al final de las filas, **mesas de lectura** con velas moradas, el **altillo** —`ALTILLO`, suelo de tablas
  sobre pilares, baranda con velas, estantes contra los muros, dos escalas al sur—, tres **lamparas** de almas colgando de la
  cumbrera y al fondo el **estrado del grimorio**: el escalon, el suelo ajedrezado, el muro de libros, los pendones, el **relicario de
  amatista**, las velas en sus pies y el **atril con el libro perdido 24** —"Notas del bibliotecario", de Maese Orlen;
  `Perdidos.TOTAL` = 24— con el arcon `chests/academia_bueno`); al oeste el **scriptorium** (|u| 23..34: los pupitres de los copistas
  —atriles— junto a las ventanas, la mesa larga con velas y papeles, estantes, manchas de tinta, barriles); al este el **aula** (la
  catedra con su atril y la **pizarra**, y seis filas de bancos con pupitre); al noreste la **torre del archivo** (u 21..29, v -57..-49,
  `TORRE_ALTO` = SUELO + 38 y su chapitel con el mastil y la luz: lo mas alto de la meseta; por dentro una escala pegada al muro del
  norte hasta la sala del archivo —`ARCHIVO`—: estantes, atril, velas y un arcon) y las **ruinas del aula vieja** (muros rotos,
  columnas de cuarzo en pie y caidas, escombros); al noroeste la **veta**: el afloramiento de amatista sobre el que se levanto la
  academia (un lomo de calcita, toba y basalto con la amatista asomando, racimos y cinco agujas). Las alas tienen tejado a dos aguas
  con vigas y faroles. Botin: `chests/academia` y `academia_bueno` (papel, libros, tinta, amatista, lapislazuli, frutos lunares y de la
  tormenta, piedras, los hechizos de la Sombra, el baston, el talisman de la mente, cenizas del relampago y del paso). Sitios
  (`Academia.sitio`): `entrada`, `puerta`, `patio`, `claustro`, `jardin`, `fuente`, `biblioteca`, `pasillo`, `altillo`, `estrado`,
  `atril`, `arcon`, `scriptorium`, `aula`, `catedra`, `torre`, `archivo`, `veta`, `ruinas`. El Espejo: "La Academia Espectral" y,
  dentro de la biblioteca, "La Gran Biblioteca" (`Morvain.lugar`, `Academia.dentro` / `enBiblioteca`); logro `academia` al entrar.
- **Los puestos** (`MorvainEnemigos`, sitio `academia`, 36, de `Academia.esperas`): un aprendiz leyendo junto al camino; un custodio
  en el paso de la puerta; dos aprendices de ronda en el patio de entrada; en el claustro dos maestros de ronda por las galerias y tres
  aprendices leyendo; en el jardin un maestro ante la fuente y dos tumbados en la hierba; en la biblioteca dos custodios de ronda entre
  los estantes, tres aprendices leyendo y dos tumbados entre los libros, dos maestros de ronda por el altillo y un custodio ante el
  grimorio; en el scriptorium tres copistas y un maestro; en el aula cuatro aprendices en los bancos y el maestro en la catedra; un
  custodio y un tumbado en la veta; un maestro y un tumbado en las ruinas; y un maestro arriba en el archivo.
- **La academia suena** (`ClientMusic.tickAcademia`): en la region de la academia entre Y 200 y 330, `ambiente.academia` (viento en
  la meseta, susurros lejanos de lecciones, paginas y campanillas de cristal; una de dos tomas cada vez).
- Sonidos (ElevenLabs, flow `oREixXYwkQfS64iE6kai`, `TOMAS_SFX`): `erudito_susurro` (1 y 4), `erudito_alerta` (3 y 1),
  `erudito_dolor` (3, 1 y 4), `erudito_muerte` (1 y 2), `erudito_pluma` (1 y 3), `erudito_tinta` (4 y 3), `erudito_orbe` (2 y 4),
  `erudito_runas` (1 y 3), `erudito_parpadeo` (1 y 2), `erudito_tomazo` (1 y 4), `erudito_cadena` (2 y 3), `erudito_alzarse` (1 y 2),
  `academia_ambiente` (3 y 4, `stream`).
- Comandos `/darkcraft academia [ir <sitio> | enemigos [volver | fuera | sacar]]` (sin mas: como esta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_academia carpeta` (con `--techo Y` para ver dentro): el recinto entero con
  las marcas de los eruditos (En); si se llega a pie desde la entrada a cada sitio (el altillo y el archivo por sus escalas) y a cada
  erudito (las alfombras —los papeles— no cuentan como bloque), que ninguno esta metido en un bloque y que todos tienen suelo.
- Pruebas: servidor, lineas `Morvain, la academia espectral: ...` (el atril con el libro 24, el arcon, la fuente, la aguja de la
  torre, los estantes; a pie desde el camino; vistas `morvain_academia.png` y `morvain_academia_biblioteca.png`), `Morvain, los
  eruditos: ...`, `Morvain, los eruditos en accion: ...` (el que lee se alza y el tumbado se levanta; cada accion acaba) y `Morvain,
  los eruditos contra una presa: ...` (plumazo, tintero, orbe, runas, parpadeo, tomazo y su onda, cadenazo que frena); cliente, tramo
  `academia()` despues del Condestable: desde el santuario (`cliente_mv222`), la puerta (`mv223`), entrar a pie con el cartel, el
  logro y el viento (`mv224`), el claustro y la fuente (`mv225`), una galeria (`mv226`), la biblioteca desde la puerta (`mv227`) y
  desde el altillo (`mv228`), el estrado del grimorio (`mv229`), el scriptorium (`mv230`), el aula (`mv231`), la torre (`mv232`) y la
  sala del archivo (`mv233`), la veta (`mv234`), todo desde lo alto (`mv235`); en supervivencia, en el patio de entrada: el plumazo
  (`mv236`), el tintero en el aire (`mv237`) y la tinta que ciega (`mv238`), el orbe (`mv239`) y en vuelo (`mv240`), las runas
  (`mv241`) y al reventar (`mv242`), el parpadeo (`mv243`) y donde aparece (`mv244`), el tomazo (`mv245`), el cadenazo (`mv246`), un
  aprendiz tumbado (`mv247`) que se levanta (`mv248`) y cae (`mv249`), el libro de Maese Orlen (`mv250`) y el bestiario (`mv251`).
  `LIMIT` 70 min (`lanzar.sh` 76 y 74 min).

### Plan grande, parte 7p: Ysolde, la Rectora de la Academia (el jefe de la Academia Espectral) y el Observatorio

Respuestas 125 y 131 del plan; el sexto jefe de Morvain (el del libro 24: "subio a leer el fuego que caia... y el cielo la lee a
ella").
- **El Observatorio** (`morvain/Observatorio`, en la meseta al este de la academia, a la puerta del santuario del Observatorio
  —(5577, -3543), 57 al sur—; `CX` 5577, `CZ` -3600, `SUELO` 240 y `PISO` 241; u = x - CX, v = z - CZ; se dibuja chunk a chunk al
  adornar y en su caja `Adorno` no pone nada): una **terraza** de caliza (|u| <= 30, v -24..31, dos sobre la meseta, balaustrada,
  obeliscos en las esquinas y la escalinata del sur), la **rotonda** (lo que se pisa hasta `R_DENTRO` 14,5; el muro hasta `R_FUERA`
  16,5 y `MURO_ALTO` = PISO + 12: zocalo y cornisa de pizarra, ocho lancetas moradas) con la **cupula de cobre** (semiesfera de radio
  15 / 16,5 sobre el muro, nervios de pizarra) y su **rendija** abierta del cenit al norte (|u| <= 1, v -16..-1). Dentro: el suelo es
  una **carta del cielo** (el disco de lapislazuli con estrellas de luz, las cuatro constelaciones de cuarzo, el anillo de las horas
  y el de los doce signos), el **planetario** colgado de la cupula (el sol —una luz de hongo en PISO + 16— y cuatro planetas en sus
  brazos de cadena), al norte el **estrado** (un escalon, v <= -7) con el **gran telescopio** de cobre que sale por la rendija (el
  ocular en v -11, el objetivo de cristal azul en PISO + 21) y cuatro **hornacinas** en el muro con los atriles de los maestros
  (`hornacinas()`, a 45, 135, 225 y 315 grados). Al sur el **vestibulo** (estantes, atriles, la alfombra morada) con la puerta de la
  rotonda (|u| <= 1, cuatro de alto y su punta: el **sello** va en v 15, la cara de dentro del muro); al este la **celda de la
  Rectora** (su mesa, el globo celeste, el arcon `chests/observatorio` y el **atril con el libro perdido 25** —"Las ultimas
  observaciones", de Ysolde; `Perdidos.TOTAL` = 25—), cerrada con una **reja** en el muro de la rotonda (u 15, 13 celdas con la punta
  del arco) mientras ella viva; al oeste la **sala de los instrumentos** (el globo grande, mesas con instrumentos y una escala a la
  **azotea** con su anteojo). En la terraza el **reloj de sol**; fuera el **camino** con faroles y el **circulo de piedras** (diez
  menhires a 46). Sitios (`Observatorio.sitio`): `entrada`, `camino`, `escalinata`, `porche`, `vestibulo`, `puerta`, `rotonda`,
  `centro`, `estrado`, `rectora`, `telescopio`, `hornacina_0..3`, `celda`, `atril`, `arcon`, `instrumentos`, `azotea`, `reloj`,
  `circulo`. El Espejo: "El Observatorio" (`Observatorio.dentro`, la terraza); logro `observatorio` ("Bajo la rendija") al entrar en
  la rotonda (`enRotonda`).
- **El sello** (`Observatorio.tick`, cada tick con alguien a 120, desde `Morvain.onTick`, con su propio reloj): en cuanto la
  Rectora empieza a pelear, un muro de luz (cristal morado, `sfx.observatorio_sello`) cierra la puerta de la rotonda —antes pasa
  dentro a quien este en el hueco— y se rompe (`sfx.observatorio_abre`) al acabar la pelea o 60 ticks despues de caer ella
  (`ABRIR_TRAS`, mientras dice su ultima frase; `cerrar` guarda a la ultima Rectora vista). Lo cerrado se mira en el mundo (si el
  servidor se para a media pelea, al volver se abre solo). `SELLADOS` / `ABIERTOS`, `sellado` (pruebas).
- **Al caer la Rectora** (`Observatorio.paz`, cada segundo): se alza la reja de su celda y los eruditos que seguian **leyendo** (los
  `REZA` de la academia y de la puerta del observatorio; no los que pelean) se deshacen en letras de luz (`descansar`, `DESCANSAN`) y
  **ya no vuelven** (los sitios `academia` y `observatorio` se registran con `activo`). `deshacerPaz` (comando y pruebas) lo deja
  como estaba.
- **Los puestos** (`MorvainEnemigos`, sitio `observatorio`, de `Observatorio.esperas`): la Rectora (puesto 0, grupo `Puestos.JEFE`,
  en el estrado bajo el ocular, mirando al telescopio), dos custodios leyendo a la puerta del vestibulo, un maestro de ronda entre los
  instrumentos y dos aprendices en el camino (uno leyendo junto a un farol y otro tumbado).
- **La Rectora** (`enemigos/Rectora`, entidad `darkcraft:rectora`, 0,95 x 3,3; hereda de `Guardian`): modelo de
  `tools/enemigos/rectora.py` (un espectro alto sin piernas: el vestido azul de noche con estrellas bordadas acaba en jirones, el
  cuello alto, el manto con constelaciones que lucen, melena blanca, diadema con su gema, la **aureola de estrellas** detras de la
  cabeza, un astrolabio al cinto y el **baculo de plata** con la estrella de cristal; escala 1,6; se dibuja medio transparente como
  los eruditos: `client/enemigos/RectoraModelo` y `RectoraRenderer`). 960 de vida, golpe 12, armadura 6 (dureza 2), casi no se la
  empuja. **La magia le hace la mitad; el fuego, un 40 % mas** (los libros y la tinta arden). Espera **leyendo el cielo** (`REZA`:
  flotando ante el ocular con la mano alzada; letras que suben al telescopio y `sfx.rectora_espera` de vez en cuando).
- **La pelea** (solo con quien esta en la rotonda: `percibe` y `objetivoValido`): al verle dice la frase 1 mientras baja la vista
  y se levanta, y grita al acabarla. El **baculo** (barre de su derecha a su izquierda: llega a 4,4, empuja y frena) a quien se le
  acerca; si se le pega o se le pone **a la espalda** (o a veces tras un golpe) **parpadea**: se apaga y aparece a 6-10 bloques,
  dentro de la rotonda, mirandole. De lejos, los **orbes** (tres `Orbe` en abanico que siguen a su presa, x0,55 del golpe). La
  **lluvia de estrellas** (alza el baculo y la mano al cielo): una estrella sobre cada uno de los que pelean y otras sueltas por la
  rotonda, cada una con su **circulo de luz** en el suelo que se cierra; caen por la rendija y a los 24 ticks revientan (radio 2,1,
  dano magico x1,25 y lanza hacia arriba): hay que salir del circulo; a media vida cae una segunda tanda. La **constelacion**: clava
  estrellas en el suelo (una bajo su presa; 5, o 7 a media vida) unidas en cadena y con una linea que cruza; a los 30 ticks se
  encienden 10 ticks y **queman a quien este de pie sobre una linea** (a 0,75; dano x1,1 y frena; saltandola, no: la altura de la
  linea sube con el escalon del estrado). A **media vida** no grita: **se alza** ("¡Maestros! ¡A vuestros puestos!") y de las
  hornacinas salen sus **maestros** (eruditos efimeros, de los atriles, leyendo, y se alzan de golpe con su presa; dos, uno mas por
  amigo, hasta cuatro; las hornacinas mas lejos de los jugadores primero) y desde entonces lee mas deprisa. `Rectora.mediaVida()`,
  `vencer()`, `quieta` (pruebas: no elige golpes ni grita por su cuenta) y contadores (`baculos`, `orbes`, `lluvias`,
  `estrellasCaidas` / `estrellasQueDan`, `constelaciones` / `lineasQueDan`, `parpadeos`, `maestrosAlzados`, `marcas`, `nodos`).
- **Voz** (ElevenLabs, `eleven_v4`, **"Enchantress - mysterious, witchy, villain character"** `sssn4wp3AspuK2kvy3Ym`, flow
  `L04DwwrYLk66HudZzb1F`; 5 frases, tomas a salvo la 2b; en `procesar.py` `voz_rectora`: algo mas grave, una segunda voz una quinta
  por encima muy baja, un coro lento que la hace temblar, brillo y el eco largo de la cupula): linea **30 + N**
  (`Voces.BASE_RECTORA`, `esRectora`; `habla_rectora` en `rey_voces.json`; subtitulos `darkcraft.rectora.voz.N`): 1 al empezar (no
  la repite en 4 min), 2 al alzarse, 3 si cae su presa, 4 la primera vez que hace caer las estrellas o dibuja la constelacion ("Lee
  las estrellas... si puedes") y 5 al caer ("Por fin... el cielo ha dejado de leerme").
- **Barra** (`GuardianCliente`, arte de `tools/hud/barra_guardianes.py` "rectora": el baculo de plata con anillos de oro y la
  estrella de cristal, estrellas sueltas y en el centro la aureola; relleno de cielo de noche; estrellas que caen). **Musica**: "Boss
  Battle Music (Epic Boss Battle)" de SubspaceAudio / Juhani Junkala (CC0, id `rectora`, tema 38 de `ClientMusic.boss`).
- **Al caer** se hunde (el baculo cae a su lado y baja la cabeza), dice su ultima frase, mira arriba una vez mas y **se deshace en
  estrellas que suben por la rendija** (`tickDeath` propio, 120 ticks); lo demas lo reparte `valdrenn/Guardianes`: el cartel de JEFE
  ABATIDO con 14000 ecos, la hazana `rectora` ("El cielo deja de leer", con su entrada del diario) y el bestiario; deja tres piedras
  de forja III, dos II y la ceniza del relampago.
- Sonidos (ElevenLabs, flow `L04DwwrYLk66HudZzb1F`, `TOMAS_SFX`): `rectora_grito` (4 y 2), `rectora_baculo` (4 y 3),
  `rectora_orbes` (3 y 2), `rectora_estrellas` (1 y 4), `rectora_estrella_cae` (3 y 2), `rectora_constelacion` (2 y 4),
  `rectora_parpadeo` (2 y 4), `rectora_alzarse` (1 y 4), `rectora_dolor` (4, 1 y 3), `rectora_muerte` (4 y 1), `rectora_espera` (2 y
  3), `observatorio_sello` (1 y 4), `observatorio_abre` (1 y 3).
- Comandos `/darkcraft rectora [ir | observatorio <sitio> | vencer | volver | sello [abrir]]` (sin mas: como esta; `volver` la pone
  otra vez a leer el cielo, baja la reja de su celda, abre el sello y vuelven los lectores de la academia).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_observatorio carpeta` (con `--techo Y` para ver dentro): la terraza, la
  rotonda y las salas con las marcas de los puestos; si se llega a pie desde la entrada a cada sitio (la celda, solo sin la reja), a
  cada erudito y a las cuatro hornacinas, y que con el sello cerrado no se entra en la rotonda desde el vestibulo.
- Pruebas: servidor, lineas `Morvain, el observatorio: ...` (el atril con el libro 25, el arcon, la reja, el sello, el cobre de la
  cupula, la rendija, el sol del planetario, el objetivo del telescopio, el suelo de la rotonda; a pie desde el camino con la reja y
  sin ella; las hornacinas libres; vistas `morvain_observatorio.png` y `morvain_observatorio_rotonda.png`), `Morvain, la Rectora:
  ...` (leyendo el cielo; al verla habla, se levanta, el sello cierra la puerta —y pasa a la rotonda al que estaba en el hueco— y
  grita al acabar la frase; cada accion acaba), `Morvain, los golpes de la Rectora a una presa: ...` (el baculo de cerca y de lejos,
  los orbes, las estrellas y sus marcas en el suelo de la rotonda, la constelacion de pie y saltando, el parpadeo), `Morvain, la
  Rectora a media vida: ...` (se alza, los maestros ante sus hornacinas; la magia la mitad, el fuego mas), `Morvain, la Rectora cae:
  ...` (el sello se rompe, la reja se alza, los que leian descansan y no vuelven) y `Morvain, la Rectora vuelve: ...`; cliente, tramo
  `observatorio()` despues de la academia: desde el santuario (`cliente_mv252`), el camino y los custodios (`mv253`), entrar en la
  rotonda con el cartel y el logro (`mv254`), la rotonda por dentro (`mv255`), la cupula con el planetario y la rendija (`mv256`), la
  Rectora leyendo el cielo (`mv257`), la reja de su celda (`mv258`), los instrumentos (`mv259`), todo desde lo alto (`mv260`); en
  supervivencia habla (`mv261`), el sello a la espalda (`mv262`) y grita con su barra (`mv263`); sus golpes (`mv264..mv270`: el
  baculo, los orbes, las marcas de las estrellas y las estrellas cayendo, la constelacion y sus lineas encendidas, el parpadeo), se
  alza con sus maestros (`mv271`), se hunde (`mv272`), el sello se rompe (`mv273`), se deshace en estrellas (`mv274`), el cartel
  (`mv275`), su celda abierta (`mv276`), su libro (`mv277`) y el bestiario (`mv278`). `LIMIT` 78 min (`lanzar.sh` 84 y 82 min, el
  paso 115, el trabajo 160).

### Plan grande, parte 7q: la Costa de los Huesos — los Salobres y el Puerto Varado

Respuestas 125, 131 y 132 del plan; la septima region con su contenido (despues, la **7r**: el jefe de la costa, el Ballenero —"el
viejo Brann sigue arponeando una ballena que hace mucho que no esta", libro 26— en el Costillar de la Ballena, (5244, 3000)).
- **El salobre** (`enemigos/Salobre`, entidad `darkcraft:salobre`, 0,65 x 1,95; modelo de `tools/enemigos/salobre.py`: chubasquero
  de hule mostaza con costras de sal, botas altas, cristales de sal en los hombros y la cabeza, la piel blanca y agrietada y los ojos
  con una luz fria de agua en la capa de brillo; `SalobreGeometria/Animaciones`, `SalobreAnims`, `client/enemigos/SalobreModelo` y
  `SalobreRenderer`). **Andan** (no flotan). La gente del puerto: la noche del cielo rojo el mar se retiro de golpe, bajaron a la
  arena a recoger lo que dejaba y el mar no volvio; la sal se los comio. Tres: **redero** (30 de vida, 4,5 de golpe; gorro de lana,
  la red al hombro y el plomo colgando de su cuerda: la **plomada** —lo voltea por encima de la cabeza y lo descarga— y de 3,6 a 11
  bloques la **red** —`enemigos/Red`, un `ThrowableItemProjectile` con el dibujo de la telarana a 2,2 que vuela en arco
  (`Cantaro.tiro`); a quien pilla lo **enreda** 45 ticks (lentitud 6: casi quieto) y se le queda enganchada en los pies (`PEGADA`,
  sincronizado: el cliente la pone donde esta su presa); si da en el suelo se queda 30 ticks y se deshace; desde que la lanza hasta
  que acaba la accion el modelo no dibuja la del hombro—), **bichero** (36 y 5,5; el sueste y el bichero, la pertiga con su gancho de
  hierro: el **garfio** —llega a 4,8; clava y **tira**: arrastra a su presa hacia el, `TIRON` 0,85 menos lo que aguante el empujon— y
  el **bichazo**, un barrido ancho) y **salinero** (60, 7,5 y 3 de armadura, escala 1,15; sombrero de paja, delantal, los brazos al
  aire comidos de sal, el saco a la espalda y el rastro de las salinas: el **rastrillazo** —lo estrella delante: frena, y alli queda
  una **costra de sal** 80 ticks, radio 2,3, que frena a quien la pisa— y la **sal** —un punado a la cara en un cono de 64 grados
  hasta 4,6: dano x0,5, lentitud, debilidad y se lleva 38 de **estamina** (`Rol.quitarEstamina`, nueva: no en creativo ni en la
  prueba)—). Esperan **faenando de rodillas** (`REZA`: cada uno lo suyo) o **enterrados en la costra de sal** (`TUMBADO`: el
  dibujante los hunde 0,22 y se alzan de golpe rompiendo la costra) o de pie. Al morir caen de rodillas, de bruces, y **se deshacen
  en sal**. Bestiario `salobre_redero` / `salobre_bichero` / `salobre_salinero`, logro `salobre`; a veces una piedra de forja (el
  salinero, alguna II). Contadores: `REDES`, `ENREDADOS`, `GARFIOS`, `ARRASTRE`, `COSTRAS`, `FRENADOS`, `SALES`, `SALADOS`,
  `ALZADOS`, `DESHECHOS`, `Red.PILLAN` / `AL_SUELO`.
- **El Puerto Varado** (`morvain/Varadero`, en la Costa de los Huesos, junto al santuario del Faro de los Huesos —(5612, 4779), su
  tarima no se toca: radio 11—; `CX` 5640, `CZ` 4740, `SUELO` 65 —el de la tarima—, `PISO` 66 y `FONDO` 57 —el de la bahia—; u = x -
  CX, v = z - CZ; se dibuja chunk a chunk al adornar y en su caja `Adorno` no pone nada). El mar de verdad queda a unos 650 bloques al
  este: el puerto se quedo en seco. El **pueblo** (u -54..23, v -60..56, allanado): el **arco de costillas** de ballena sobre el camino
  del santuario (`arcoHueso`), la **plaza** redonda con el **ancla** del monumento sobre su peana, los bancos y el **pozo** seco, la
  **lonja** (nave abierta de postes de picea con tejado a dos aguas hundido por un lado, mesas de piedra, barriles, la balanza y las
  redes colgadas), siete **casas** de pescadores (`casa`: zocalo, paredes de cal, picea o piedra de arena, ventanas, tejado a dos
  aguas con hastiales, algunas en ruinas; dentro mesa, barril, el hogar apagado, redes y la sal que ha entrado) con dos barcas
  volcadas, los **secaderos** (tres filas de varas con las algas secas encima y las redes), las **salinas** (estanques de sal uno mas
  bajo que el suelo entre caballones de barro, dos con salmuera, montones de sal), el **varadero** (la **grada** —`GRADA_V` 40, una
  rampa de losas con dos carriles de madera que baja del pueblo a la bahia cortando el muelle, con medias losas— con el barco a medio
  hacer —quilla, cuadernas, roda— y sus andamios, y el taller) y la **casa del farero** (al arranque del espigon: estantes, la mesa de
  cartas, su catalejo, el arcon `chests/puerto_bueno` y el **atril con el libro perdido 26** —"El cuaderno del farero", de Tome;
  `Perdidos.TOTAL` = 26—). El **muelle** (`MUELLE_U` 22: un muro de sillar de dos que cae ocho bloques a la bahia, algas secas abajo,
  norays y cadenas en sus argollas, el paseo de losas; dos **escaleras** bajan pegadas al muro, `ESCALERAS`). La **bahia seca**
  (`bahia`: media elipse desde el muelle, 100 x 60; hasta 0,78 el fondo llano, despues la orilla sube hasta el terreno): arena,
  **costras de sal** (calcita y hormigon blanco), barro y **charcos** de agua sobre arcilla, corales muertos; cinco **barcas**
  varadas, escoradas o volcadas (`barca`: el casco puntiagudo hundido un bloque, los bancos, el palo roto y la vela hecha jirones),
  montones de sal, un ancla perdida con su cadena y la **ballena** (`BALLENA_V` 30: la cola hundida, el espinazo con sus apofisis por
  encima de siete **costillas** en arco —alguna rota—, la calavera con sus cuencas, el hocico y las dos quijadas tiradas). El
  **espigon** (`ESPIGON_V` -20, siete de ancho con su pretil, faroles de almas y una bajada a la bahia por el sur, `BAJADA_U`) llega
  a la roca del **Faro de los Huesos** (`FARO_U` 86, `ROCA_R` 8): la torre de hueso (siete de ancho, bandas de piedra de arena,
  saeteras), cuatro **costillas** que la abrazan por fuera, dos **quijadas** en arco a la puerta (al oeste), la escalera de caracol
  de Brumavela (`ANILLO`, tres vueltas de doce peldanos, `FARO_ALTO` 36) con faroles de almas, la **galeria** con su barandilla de
  hierro y la **linterna** (`LINTERNA` = PISO + 36: cristales azules, la **luz del mar** con el **fuego frio** de almas encima, el
  arcon `chests/puerto_bueno` y el tejado de piedra de arena con su punta de hueso). Sitios (`Varadero.sitio`): `entrada`, `arco`,
  `plaza`, `lonja`, `casa_farero`, `atril`, `arcon`, `secadero`, `salinas`, `muelle`, `varadero`, `bahia`, `barcas`, `ballena`,
  `espigon`, `faro`, `faro_alto`. El Espejo: "El Puerto Varado" (`Varadero.dentro`) y "El Faro de los Huesos" (`enFaro`); logros
  `puerto` ("Bajamar eterna") al entrar y `faro` ("Luz para nadie", de los que se ven en el cielo) arriba del faro (`arribaDelFaro`).
  Botin: `chests/puerto` y `puerto_bueno` (cuerda, huesos, algas secas, prismarina, nautilos, oro, frutos del hueso, la tormenta y la
  pluma, piedras de forja, el talisman del porteador y el del hueso, cenizas de la estocada, la escarcha y la parada, la lanza de la
  Sombra, un catalejo).
- **Los puestos** (`MorvainEnemigos`, sitio `puerto`, 33, de `Varadero.esperas`): rederos remendando en la plaza, en la lonja, junto
  a las casas, en los secaderos, en el borde del muelle, en la grada, en el espigon y junto a una barca; bicheros de ronda o mirando al
  mar que no esta (la lonja, las casas, los secaderos, el muelle, la puerta de la casa del farero, la grada, el espigon, la puerta del
  faro y **arriba, en la galeria**); salineros faenando y enterrados en las salinas, enterrados en las costras de la bahia y **dentro de
  la ballena**, y uno de ronda junto a ella; uno tirado en la casa del farero.
- **La costa suena** (`ClientMusic.tickCosta`): en la region de la costa entre Y 30 y 160, `ambiente.costa` (viento frio sobre la
  arena seca, un mar lejano que no llega, barcas varadas que crujen, cabos contra los palos y el zumbido de los huesos; una de dos
  tomas cada vez).
- Sonidos (ElevenLabs, flow `0mqKDVbquq0P7oXJ6BVV`, `TOMAS_SFX`): `salobre_gemido` (2 y 3), `salobre_alerta` (3 y 4),
  `salobre_dolor` (2, 4 y 3), `salobre_muerte` (2 y 1), `salobre_plomo` (2 y 4), `salobre_red` (2 y 1), `salobre_garfio` (4 y 2),
  `salobre_rastrillo` (4 y 1), `salobre_sal` (2 y 3), `salobre_alzarse` (3 y 4: empiezan tarde, `INICIO` 4,2), `costa_ambiente` (1 y
  3, `stream`).
- Comandos `/darkcraft puerto [ir <sitio> | enemigos [volver | fuera | sacar]]` (sin mas: como esta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_puerto carpeta` (con `--techo Y` para ver dentro): el pueblo, el muelle, la
  bahia y el faro con las marcas de los salobres (Sn); si se llega a pie desde la entrada a cada sitio (la bahia por las escaleras y
  la grada, lo alto del faro por su escalera) y a cada salobre (las capas finas de nieve —la sal— no cuentan como bloque; el agua de
  los charcos vale como suelo), que ninguno esta metido en un bloque y que todos tienen suelo.
- Pruebas: servidor, lineas `Morvain, el puerto varado: ...` (el atril con el libro 26, el arcon, el fuego del faro, el muelle, el
  fondo seco, las costillas de la ballena; a pie desde la entrada; vistas `morvain_puerto.png` y `morvain_puerto_pueblo.png`),
  `Morvain, los salobres: ...`, `Morvain, los salobres en accion: ...` (el de rodillas se levanta y el enterrado se alza; cada accion
  acaba) y `Morvain, los salobres contra una presa: ...` (la plomada, la red que enreda, el garfio que arrastra, el bichazo, el
  rastrillazo con su costra que frena, la sal; uno que cae se deshace en sal); cliente, tramo `puerto()` despues del Observatorio:
  desde el santuario (`cliente_mv279`), bajo el arco (`mv280`), entrar a pie con el cartel, el logro y el viento (`mv281`), la plaza
  (`mv282`), la lonja (`mv283`), los secaderos (`mv284`), las salinas (`mv285`), el varadero (`mv286`), la bahia desde el muelle
  (`mv287`), las barcas (`mv288`), la ballena por dentro (`mv289`), el espigon (`mv290`), el faro (`mv291`), su linterna (`mv292`), la
  bahia desde la galeria con el logro (`mv293`), todo desde lo alto (`mv294`), la casa del farero (`mv295`); en supervivencia, en la
  calle mayor: la plomada (`mv296`), la red en el aire (`mv297`) y el enredo (`mv298`), el garfio (`mv299`), el bichazo (`mv300`), el
  rastrillazo y su costra (`mv301`), la sal (`mv302`), un salinero enterrado (`mv303`) que se alza (`mv304`) y cae (`mv305`), el
  cuaderno del farero (`mv306`) y el bestiario (`mv307`). `LIMIT` 82 min (`lanzar.sh` 88 y 86 min).

### Plan grande, parte 7r: Brann, el Ballenero (el jefe de la Costa de los Huesos) y el Costillar de la Ballena

Respuestas 125 y 131 del plan; el septimo jefe de Morvain (el del libro 26: "el viejo Brann sigue arponeando una ballena que hace
mucho que no esta"). Despues, la Tierra sin Fe (dos partes) y el crater con la Sombra.
- **El Costillar de la Ballena** (`morvain/Costillar`, en la Costa de los Huesos, a la puerta del santuario del Costillar —(5244,
  3000), 62 al oeste; su tarima no se toca: radio 11—; `CX` 5306, `CZ` 3000, `SUELO` 70 y `PISO` 71; u = x - CX, v = z - CZ; se dibuja
  chunk a chunk al adornar y en su caja `Adorno` no pone nada): la arena allanada (u -48..66, |v| <= 32, se funde en 8: arena gris,
  costras de sal, grava, algun hueso suelto, matas secas y sal fuera de la sala) y el **esqueleto** de una ballena colosal mirando al
  mar que ya no esta (al este): el **espinazo** (`lomo(u)`: PISO + 21 en el centro; vertebras de tres con su apofisis cada dos) de la
  cola a la nuca; trece pares de **costillas** (`COSTILLA_U0` -26 a `COSTILLA_U1` 22, cada 4; en arco del espinazo a la arena,
  `radio(u)` 16 en el centro; dos gruesas, alguna rota con su trozo caido) que cierran la **sala** (`enSala`: u -27..24 y |v| <=
  radio - 1,6) donde se pelea; la **cola** (baja del lomo a la arena entre u -30 y -50 y se tuerce al sur, lejos del santuario); la
  **pelvis**; las **aletas** (la escapula contra las costillas y los dedos tirados en la arena) y la **calavera** al este
  (`CRANEO_U0` 25: el craneo como un escudo, hueco, con sus cuencas; el **hocico** largo hasta `HOCICO_U1` 62, las dos **quijadas**
  tiradas en la arena y dientes sueltos). Dentro, lo de Brann: la **fundidora** (el hogar de ladrillo con dos calderos del aceite,
  las brasas y la chimenea), barriles de aceite, su **barca** volcada contra las costillas del sur con un remo roto, arpones de
  repuesto clavados (pararrayos), rollos de cuerda, sal y huesos. Fuera, al suroeste, su **choza** (tablas de barcas, el tejado de un
  casco del reves, la puerta al norte): la cama, la mesa con el farol, un barril, el arcon `chests/costillar_bueno` y el **atril con
  el libro perdido 27** (los dos de cara a la puerta) —"La bitacora del ballenero", de Brann; `Perdidos.TOTAL` = 27—. Botin:
  `chests/costillar` (cuerda, huesos, polvo de hueso, algas secas, oro, una correa, prismarina, tinta, frutos) y `costillar_bueno`.
  Sitios (`Costillar.sitio`): `entrada`, `costillar`, `centro`, `brann`, `fundidora`, `barca`, `craneo`, `hocico`, `aleta`, `cola`,
  `choza`, `atril`, `arcon` y `alto` (lo alto de la calavera: solo para mirar). El Espejo: "El Costillar de la Ballena"
  (`Costillar.dentro`); logro `costillar` ("Bajo las costillas") al entrar en la sala.
- **Los puestos** (`MorvainEnemigos`, sitio `costillar`, de `Costillar.esperas`): Brann (puesto 0, grupo `Puestos.JEFE`, de rodilla
  en medio de la sala, mirando al este), dos rederos remendando junto a la choza, dos bicheros de ronda (por la cola y junto a la
  calavera) y un salinero enterrado en la sal junto a la aleta del norte.
- **Brann** (`enemigos/Ballenero`, entidad `darkcraft:ballenero`, 1,2 x 3,1; hereda de `Guardian`): modelo de
  `tools/enemigos/ballenero.py` (un viejo gigante comido por la sal: calvo, la piel blanca y agrietada, melena y barba blancas, ojos
  con luz fria de agua; chaqueton largo de hule con costras de sal y su cuello alto, faldones; la cuerda en bandolera y un rollo a la
  cadera; cristales de sal en los hombros; la **pata de palo** de hueso de ballena y una bota alta; un arpon de repuesto a la
  espalda; en la derecha el **arpon** —asta, cana de hierro, la punta con sus dos barbas y el cabo atado— y en la izquierda el
  **farol de aceite**; escala 1,55: `client/enemigos/BalleneroModelo` —de vigia mientras no esta de pie; no dibuja el arpon mientras
  esta fuera ni el farol desde que lo suelta— y `BalleneroRenderer`, con la luz de los ojos y la sal que parpadea). 1000 de vida,
  golpe 12,5, armadura 6 (dureza 2), casi no se le empuja, **el fuego no le hace nada**. Espera **de vigia** (`REZA`: de rodilla
  sobre la buena, la pata de palo delante, el arpon de pie como un baston y el farol en alto; `sfx.ballenero_espera` de vez en
  cuando).
- **La pelea** (solo con quien esta bajo las costillas: `percibe` y `objetivoValido`): al verle dice la frase 1 mientras se levanta
  y grita al acabarla ("¡Alli resopla!": agua y sal). El **arponazo** (lo echa atras y lo clava a fondo, tirandose hacia delante:
  llega a 4,9 y deja sangrando —frena un poco—) y el **tajo** doble (barre con la punta a un lado y al otro: 4,2, 150 grados) de
  cerca; de lejos (5-22, viendole) el **arpon** con su cabo (`ArponBallenero`, una flecha pesada: x0,9 del golpe; **se queda
  clavado** en quien pilla —no si rueda o para— y entonces Brann **tira del cabo**: el **tiron** le arrastra hacia el, `TIRON` 1,05,
  mas cuanto mas lejos y menos lo que aguante el empujon; despues el arpon vuelve volando a la mano; el dibujante pinta el cabo de
  la mano a la cola) y el **farol** (`FarolAceite`, en arco: revienta en cristales y aceite y deja un **charco ardiendo**,
  `Cantaro.Llamas` de radio 2,6 y 110 ticks); el **grito** al empezar. A **media vida** no grita: **llama a la Vieja** (apunta con el
  arpon y alza el farol; la frase 2) y desde entonces la vuelve a llamar cada `VIEJA_CADA` 300 ticks (mas un poco) y **salta**
  (3,5-10 de su presa: cae clavando el arpon y la **onda** —radio `R_ONDA` 5,2— tumba a quien este de pie cerca; saltando o
  rodando se libra). `Ballenero.mediaVida()`, `vencer()`, `quieta` (pruebas) y contadores (`arponazos`, `tajos`, `lanzados`,
  `tirones`, `arrastrados` / `arrastre`, `faroles`, `saltos`, `ondasQueDan`, `llamadas`; `ArponBallenero.CLAVAN` / `AL_SUELO`,
  `FarolAceite.REVIENTAN` / `DAN`).
- **La Vieja** (`enemigos/Vieja`, entidad `darkcraft:vieja`, no se guarda): la cachalote blanca que se le escapo hace cuarenta anos,
  hecha espectro (modelo de `tools/enemigos/vieja.py`: cuerpo con su joroba, la cabeza enorme, la mandibula, las aletas, la cola con
  su aleta; gris azulada palida con surcos, cicatrices y percebes, tres arpones clavados con su cabo; `client/enemigos/ViejaModelo` y
  `ViejaRenderer`, a escala 3, medio transparente, con los ojos y las cicatrices que lucen). **Embiste a lo largo de la sala** (en el
  costillar, de una punta a otra —u -33 a 25, alternando el sentido— por la v de la presa; fuera, por la linea de Brann a su presa):
  primero el **aviso** (`AVISO_TICKS` 30: su **chorro** de agua donde va a salir y una linea de espuma por donde va a pasar), sale de
  la arena (22 ticks), cruza a `VELOCIDAD` 1,05 bloques por tick a ras de arena y se hunde (18); a quien pilla a lo largo de su cuerpo
  (`LARGO` 17, `RADIO` 3,1) le da x1,35 del golpe de Brann y lo lanza de lado y hacia arriba (una vez por embestida). Solo da a los
  jugadores y a la presa de Brann. `SALEN`, `EMBISTEN`, `DAN` (pruebas).
- **Al caer Brann** cae de rodillas agarrado al arpon y dice su ultima frase; a los 40 ticks **la Vieja sale de la arena por ultima
  vez y sube al cielo** (`Vieja.subir`, 110 ticks) y el **se deshace en sal** (`tickDeath` propio, 122 ticks); lo demas lo reparte
  `valdrenn/Guardianes`: el cartel de JEFE ABATIDO con 15000 ecos, la hazana `ballenero` ("Vuelve la marea", con su entrada del
  diario) y el bestiario; deja tres piedras de forja III, dos II y la ceniza de la estocada. Y **vuelve la marea** (`Costillar.paz`,
  cada segundo desde `Morvain.onTick`): los salobres que seguian **faenando de rodillas** (los `REZA` del Puerto Varado y del
  costillar; no los que pelean) se deshacen en sal (`DESCANSAN`) y **ya no vuelven** (los sitios `puerto` y `costillar` se registran
  con `activo`).
- **Voz** (ElevenLabs, `eleven_v4`, **"Declan Graves - Haunted Rasps and Old World Dread"** `1BfrkuYXmEwp8AWqSLWk` —la del Verdugo—,
  flow `yIxYNc9wejnQYo6s9uh8`; 5 frases, tomas a salvo la 1b; en `procesar.py` `voz_ballenero`: algo mas grave pero menos que el
  Verdugo, una segunda voz una octava por debajo muy baja, raspa, un vaiven lento y el eco corto y hueco de las costillas): linea
  **50 + N** (`Voces.BASE_BALLENERO`, `esBallenero`; `habla_ballenero` en `rey_voces.json`; subtitulos `darkcraft.ballenero.voz.N`): 1
  al empezar ("¿Otra mano para las barcas?..."; no la repite en 4 min), 2 al llamar a la Vieja ("¡Alli resopla!"), 3 si cae su
  presa, 4 la primera vez que se le clava el arpon a alguien ("Cuarenta anos detras de ella...") y 5 al caer ("La marea... oigo la
  marea que vuelve...").
- **Barra** (`GuardianCliente`, estilo "mar": letras verde agua y gotas que caen; arte de `tools/hud/barra_guardianes.py`
  "ballenero": el arpon con su cuerda enrollada y la aleta de la cola de una ballena; relleno de mar; salpicaduras). **Musica**: "The
  Kraken" del Ocean Music Pack de Leonardo Paz (leohpaz, CC-BY 4.0, id `ballenero`, tema 39 de `ClientMusic.boss`).
- **El costillar suena** (`ClientMusic.tickCosta`): a menos de 75 del centro de la sala, en vez del viento de la costa,
  `ambiente.costillar` (el viento que silba entre los huesos).
- Sonidos (ElevenLabs, flow `yIxYNc9wejnQYo6s9uh8`, `TOMAS_SFX`): `ballenero_grito` (4 y 1), `ballenero_arpon` (3 y 1),
  `ballenero_tiron` (4 y 2, `INICIO` 1,1), `ballenero_arponazo` (1 y 4), `ballenero_cuchillo` (3 y 2, `INICIO` 0,4),
  `ballenero_aceite` (3 y 4), `ballenero_vieja` (4 y 1), `ballenero_embestida` (4 y 1), `ballenero_dolor` (3 y 4),
  `ballenero_muerte` (1 y 3), `ballenero_espera` (4 y 3, `stream`) y `costillar_ambiente` (3 y 1, `stream`).
- Comandos `/darkcraft ballenero [ir | costillar <sitio> | vencer | volver]` (sin mas: como esta; `volver` le pone otra vez de vigia y
  vuelven los salobres que faenaban, aqui y en el puerto).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_costillar carpeta`: el esqueleto, la fundidora, la barca y la choza con las
  marcas de Brann (B0) y los salobres (Sn); si se llega a pie desde la entrada a cada sitio (menos `alto`) y a cada uno de los que
  esperan, que ninguno esta metido en un bloque, que todos tienen suelo y lo que estorba a ras de suelo en la sala.
- Pruebas: servidor, lineas `Morvain, el costillar: ...` (el atril con el libro 27, el arcon, el espinazo, las costillas en la arena,
  la calavera, el suelo de la sala; a pie desde la entrada; vistas `morvain_costillar.png` y `morvain_costillar_sala.png`), `Morvain,
  el Ballenero: ...` (de vigia; al verle habla, se levanta y grita; cada accion acaba y el arpon vuelve a la mano), `Morvain, los
  golpes del Ballenero a una presa: ...` (el arponazo, el tajo, el arpon que se clava y el tiron que arrastra, el farol y su charco,
  el salto y su onda, la Vieja: sale, embiste por la sala y da), `Morvain, el Ballenero a media vida: ...` (llama a la Vieja y la
  vuelve a llamar), `Morvain, el Ballenero cae: ...` (la Vieja sube al cielo, se deshace en sal, los salobres que faenaban descansan y
  no vuelven) y `Morvain, el Ballenero vuelve: ...`; cliente, tramo `costillar()` despues del puerto: desde el santuario
  (`cliente_mv308`), la cola (`mv309`), entrar bajo las costillas con el cartel, el logro y el viento (`mv310`), la sala por dentro
  (`mv311`), Brann de vigia (`mv312`), la fundidora (`mv313`), la barca (`mv314`), la calavera (`mv315`), el hocico (`mv316`), la
  choza (`mv317`), todo desde lo alto (`mv318`); en supervivencia habla (`mv319`) y grita con su barra (`mv320`); sus golpes
  (`mv321..mv327`: el arponazo, el tajo, el arpon en el aire, el tiron, el farol y su charco, el salto y la onda), la Vieja (`mv328`
  el chorro, `mv329` sale, `mv330` embiste), a media vida la vuelve a llamar (`mv331`), cae de rodillas (`mv332`), la Vieja sube al
  cielo (`mv333`), se deshace en sal (`mv334`), el cartel (`mv335`), su bitacora (`mv336`) y el bestiario (`mv337`). `LIMIT` 88 min
  (`lanzar.sh` 94 y 92 min, el paso 122, el trabajo 170).

### Plan grande, parte 7s: la Tierra sin Fe — los Penitentes y el Puente de la Grieta

Respuestas 125, 131 y 132 del plan; la octava region con su contenido (despues, la **7t**: el jefe de la Tierra sin Fe, Anselmo, el
Pontifice Sangrante, en el santuario del Altar Profanado —(-1864, -1600)—; lo anuncia el libro 28: "se los lleva al noroeste, al
Altar de la Sangre"). Despues, el crater y la Sombra.
- **El penitente** (`enemigos/Penitente`, entidad `darkcraft:penitente`, 0,65 x 1,95; modelo de `tools/enemigos/penitente.py`: tunica y
  capirote de su cofradia, el antifaz con la luz roja de los ojos en la capa de brillo; `PenitenteGeometria/Animaciones`,
  `PenitenteAnims`, `client/enemigos/PenitenteModelo` —`REZA` reza a su manera cada variante, `TUMBADO` = postrado en cruz— y
  `PenitenteRenderer`, el portador a escala 1,25). Los peregrinos que iban al crater a pedir perdon. Tres: **flagelante** (32 de vida,
  5 de golpe; rojo sangre, la espalda en carne viva: el **azote** y los tres **azotes** con la disciplina, que suman **sangre**; herido
  —por debajo del 60 %—, la **penitencia**: se azota a si mismo —el 6 % de su vida cada azote— y se **enfervoriza** 200 ticks: animo
  +0,4 y Velocidad II), **portador** (70, 8,5 y 4 de armadura; negro, la cruz de madera a rastras: el **cruzazo** —la estrella en el
  suelo; la **onda**, a 2,8 delante y radio 2,6, da x0,6 del golpe y lanza a quien este de pie: saltando o rodando se libra—, el
  **barrido** a una mano y la **embestida** —de 4,5 a 9,5
  bloques, con la cruz por delante—) y **turiferario** (36, 4,5 y 1; morado y oro, el incensario de laton: lo voltea alrededor y
  **quema**; el **humo** —`enemigos/Incienso`, una nube de efectos del juego sin efectos con su propio tick: radio 3, 120 ticks; a quien
  este dentro le suma 12 de sangre cada medio segundo y le frena; a los penitentes les da Resistencia— y, con los suyos heridos, el
  **cantico**: los penitentes a 10 bloques se curan el 15 % y llevan Regeneracion y Resistencia). Esperan **rezando de rodillas** (se
  levantan si te acercas), **postrados** (se alzan de golpe) o de pie. Al morir caen de rodillas, de bruces, y **se desangran**.
  Bestiario `penitente_flagelante` / `_portador` / `_turiferario`, logro `penitente` ("Sin absolucion").
- **La sangre** (`enemigos/Sangrado`): lo que suman los azotes y el humo se apunta en el jugador (`darkcraft_sangre` en sus datos) y
  baja 2 por tick tras 60 ticks sin sumar; al llegar a 100 (`TOPE`) **revienta la hemorragia**: el 15 % de su vida maxima de golpe (3
  como poco), lentitud, chorro de sangre y `sfx.penitente_hemorragia`. Ni creativo ni espectador; los penitentes tampoco sangran.
  `HEMORRAGIAS` (pruebas).
- **El Puente de la Grieta** (`morvain/Grieta`, en la Tierra sin Fe, junto al santuario de la Grieta —(24, 2181), "Grieta de los
  Apostatas", al suroeste: el camino sale de su tarima—; `CX` 135, `CZ` 2095, `SUELO` 122, `PISO` 123; u = x - CX, v = z - CZ, el norte
  es el lado del crater; se dibuja chunk a chunk al adornar; `Adorno` no pone nada en lo allanado —u -52..92, v -62..50, se funde en
  8— ni en la grieta; en el camino del santuario si, y el camino quita lo que le caiga encima). La **grieta** (`Sima`, sin plano:
  cada columna se calcula con `centro(u)`, `ancho(u)` y `media(u, y, lado)`): de oeste a este (`G_U0` -62 a `G_U1` 102), 22 de ancho
  arriba, se estrecha al bajar (0,12 por bloque, con salientes de ruido) hasta la **sangre** (el agua roja de la region, `SANGRE` =
  63, sobre el fondo en 56); paredes en **vetas** de terracota roja, naranja, parda y normal, piedra negra y basalto (sangre y roca del
  infierno abajo), huesos de los que cayeron y **enredaderas rojas** colgando de los salientes; se cierra poco a poco en sus dos
  puntas (se puede rodear). El **puente** (`PUENTE_V0` -19 a `PUENTE_V1` 19, siete de ancho): tablero de ladrillo de piedra negra, el
  **arco** por debajo (`bajo(v)`: dovelas lisas, enjuta de sillar y los estribos pegados a las paredes), la cornisa sobre mensulas, los
  pretiles de muro con pilares y **velas rojas** (faroles en las puntas), la **espadana** en medio con la **campana** (dos pilares, el
  dintel sobre el paso, el muro con su hueco, tejadillo y cruz de hierro; dos faroles colgados) y una **jaula** colgada bajo el arco.
  Al sur: el **camino** del santuario (`CAMINO`, tierra pisada fuera, losas dentro) con la **cruz de termino**, la **hospederia**
  (`HOSP_*`: zocalo de piedra negra, adobe con entramado de roble oscuro, tejado a dos aguas hundido sobre el refectorio, chimenea y
  cruz; el **refectorio** con la mesa larga, bancos, velas, el hogar, la olla, barriles —uno con `chests/grieta`—, jergones de paja y
  la lampara; tras el tabique, la **capilla**: el altar con su caliz —una rosa marchita—, la cruz ante la vidriera roja, bancos, el
  **arcon** `chests/grieta_bueno` y el **atril con el libro perdido 28** —"El registro de la hospederia", del hermano Ulric;
  `Perdidos.TOTAL` = 28—; fuera, el pozo) y el **campamento** de los penitentes (la hoguera, cinco tiendas de lana, los **postes de
  los azotes** con la tierra empapada, la olla sobre el fuego, la **cruz de guia** con su sudario y velas, braseros, sacos, barriles con
  `chests/grieta` y dos estandartes). Al norte: la **puerta** (dos torres con almenas y cornisa, el arco apuntado con la reja medio
  alzada, la cruz labrada y los estandartes), el **paso** abandonado en mitad del camino (la parihuela con su canasto de velas, los
  faroles, los varales tirados y la imagen tapada con su manto morado ante la cruz), el **cementerio** de los peregrinos (tapia rota,
  tumbas con cruces de madera, de piedra, rotas o caidas, alguna abierta con su calavera, velas y un arbol seco) y el **humilladero**
  (grada, cuatro pilares, tejadillo en piramide y la cruz de piedra); al este, lo que queda del **puente viejo** de cuerdas colgando de
  las dos orillas. Sitios (`Grieta.sitio`): `entrada`, `camino`, `hospederia`, `refectorio`, `capilla`, `atril`, `arcon`,
  `campamento`, `hoguera`, `postes`, `cruz`, `puente_sur`, `puente`, `campana`, `puente_norte`, `puerta`, `paso`, `cementerio`,
  `humilladero`, `puente_viejo`, `alto` y `fondo` (estos tres, la campana, lo alto y el fondo de la grieta, solo para mirar). El Espejo:
  "El Puente de la Grieta" (`Grieta.dentro`, tambien en el fondo); logro `grieta` ("Sobre el abismo") al pasar por el medio del puente
  (`enPuente`): la campana le recibe.
- **La campana** (`Grieta.campana`, cada 5 s desde `Morvain.onTick`: de vez en cuando tañe sola, `sfx.grieta_campana`, para quien
  este a 150) y **tocarla llama** (`Morvain.onCampana` → `Grieta.llamar`: quien la toca —clic derecho, como cualquier campana— hace
  que los penitentes de la grieta a menos de `LLAMA` 72 bloques sin presa vayan a por el —`Enemigo.acudir`—; como mucho una vez cada
  8 s; `LLAMADAS`, `ACUDEN`). El libro 28 lo avisa: "que no toque la campana. Ellos acuden".
- **Los puestos** (`MorvainEnemigos`, sitio `grieta`, 30, de `Grieta.esperas`): en el puente dos flagelantes de rodillas y el
  turiferario bajo la espadana, un portador en la cabeza del sur; en el camino uno que va y viene y un portador rezando ante la cruz de
  termino; en el campamento tres flagelantes en los postes, el turiferario de la olla, dos postrados ante la cruz de guia, un portador
  a la puerta de una tienda y uno de ronda; en la hospederia dos rezando en el refectorio y uno echado en la paja, y en la capilla un
  turiferario de rodillas ante el altar y uno postrado en el pasillo; dos portadores de guardia tras la puerta del norte; los cuatro
  portadores de rodillas junto a los varales del paso y el turiferario que lo guia; dos rezando en el cementerio y uno postrado sobre
  una tumba; un turiferario en el humilladero.
- **La Tierra sin Fe suena** (`ClientMusic.tickSinFe`): en su region entre Y 40 y 190, `ambiente.sinfe` (viento caliente sobre la
  tierra agrietada, polvo, el rumor de las grietas y lamentos lejanos).
- Sonidos (ElevenLabs, flow `rA0LWiSNDaEcajA8osLQ`, `TOMAS_SFX`): `penitente_rezo`, `penitente_alerta`, `penitente_dolor`,
  `penitente_muerte`, `penitente_azote`, `penitente_penitencia`, `penitente_hemorragia`, `penitente_cruz`, `penitente_arrastre`,
  `penitente_incensario`, `penitente_humo`, `penitente_cantico`, `penitente_alzarse`, `grieta_campana` y `sinfe_ambiente`
  (`stream`).
- Comandos `/darkcraft grieta [ir <sitio> | campana | enemigos [volver | fuera | sacar]]` (sin mas: como esta; `campana` la toca como
  quien la usa).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_grieta carpeta` (los cortes `x=135` —el arco del puente— y `x=160` —la
  grieta— la ensenan bien): la grieta, el puente, la puerta, el paso, el cementerio, el humilladero, la hospederia y el campamento con
  las marcas de los penitentes (Pn); si se llega a pie desde la entrada a cada sitio (menos la campana, lo alto y el fondo) y a cada
  penitente, que ninguno esta metido en un bloque, que todos tienen suelo y si sin el puente se llega al otro lado (rodeando).
- Pruebas: servidor, lineas `Morvain, el puente de la grieta: ...` (el atril con el libro 28, el arcon, la campana, el tablero, el
  hueco y la sangre de la grieta, el arco de la puerta, la imagen del paso; a pie desde la entrada; vistas `morvain_grieta.png` y
  `morvain_grieta_puente.png`), `Morvain, los penitentes: ...`, `Morvain, los penitentes en accion: ...` (el de rodillas se levanta y
  el postrado se alza; cada accion acaba) y `Morvain, los penitentes contra una presa: ...` (el azote y los azotes con su sangre, la
  penitencia y el fervor, el cruzazo y su onda, el barrido, la embestida, el incensario que quema, el humo, el cantico que cura; tres
  tandas de azotes hasta la hemorragia; uno que cae se desangra); cliente, tramo `grieta()` despues del costillar: desde el santuario
  (`cliente_mv338`), la cruz de termino (`mv339`), entrar a pie con el cartel y el viento (`mv340`), la hospederia (`mv341`), el
  refectorio (`mv342`), la capilla (`mv343`), el campamento (`mv344`), los postes (`mv345`), el puente desde el sur (`mv346`), en medio
  del puente con el logro (`mv347`), la sima desde el puente (`mv348`), el arco desde el fondo (`mv349`), la puerta (`mv350`), el paso
  (`mv351`), el cementerio (`mv352`), el humilladero (`mv353`), el puente viejo (`mv354`), todo desde lo alto (`mv355`); en
  supervivencia, en la orilla del norte: el azote (`mv356`), los azotes (`mv357`), la penitencia (`mv358`), el cruzazo (`mv359`), el
  barrido (`mv360`), la embestida (`mv361`), el incensario (`mv362`), el humo (`mv363`), el cantico (`mv364`), un postrado que se alza
  (`mv365`), la campana que los llama (`mv366`), uno que cae y se desangra (`mv367`), el registro de la hospederia (`mv368`) y el
  bestiario (`mv369`). `LIMIT` 94 min (`lanzar.sh` 100 y 98 min, el paso 128, el trabajo 176).

### Plan grande, parte 7t: Anselmo, el Pontifice Sangrante (el jefe de la Tierra sin Fe) y el Altar Profanado

Respuestas 125 y 131 del plan; el octavo jefe de Morvain (el del libro 28: "se los lleva al noroeste, al Altar de la Sangre").
Despues, el crater con la Sombra.
- **El Altar Profanado** (`morvain/Altar`, en la Tierra sin Fe, a la puerta del santuario del Altar —(-1864, -1600), al sur; su tarima
  no se toca: `SANT_R` 6—; `CX` -1864, `CZ` -1652, `SUELO` 114 y `PISO` 115; u = x - CX, v = z - CZ, el norte con v negativa; se dibuja
  chunk a chunk al adornar y en su caja `Adorno` no pone nada; la tierra allanada u -33..33, v -37..47, se funde en 8). **La sangre es
  agua**: en la Tierra sin Fe el agua es roja (el color de su bioma). De sur a norte: el **camino** del santuario, el **atrio**
  enlosado con sus columnas rotas (y sus fustes caidos), la **fachada** (`FACHADA_V` 30: la puerta grande apuntada, las dos pequenas,
  el **roseton** roto con su traceria y el hastial) y el **campanario** medio caido a su derecha (`TORRE_U` 15..20, `TORRE_V` 24..29, con
  su campana); la **nave** sin techo (`NAVE_U` 11; se hundio: quedan las **arcadas** apuntadas sobre los pilares —`PILAR_U` 6, cada 5
  de v 26 a -4—, cuatro arcos que la cruzaban, casi todos rotos, el triforio en los extremos, los contrafuertes y escombros), con los
  **bancos** rotos en las naves de los lados, las vidrieras rotas de sus ventanas y el **canal de sangre** por el medio (losas de
  piedra negra inundadas a ras del suelo, `canal`, de u = 0 y v -14 a 28); el **crucero** (`CRUCERO_V0` -19 a `CRUCERO_V1` -9, los
  brazos hasta `BRAZO_U` 22): en cada brazo una **pila** de sangre (3 x 3 con su brocal, `PILA_U` 13, `PILA_V` -14; de ella baja otro
  canal al del medio) y al fondo, tras un muro bajo con su puerta (`CUARTO_U` 18, en v -14), la **sacristia** al este (estanterias,
  la mesa de los ornamentos, el arcon `chests/altar_bueno` y el **atril con el libro perdido 29** —"El ultimo sermon", de Anselmo;
  `Perdidos.TOTAL` = 29—) y el **osario** al oeste (los huesos de los peregrinos amontonados contra las paredes, calaveras y velas
  negras); y el **abside** (`ABSIDE_V` -20, radio 11,5, sus cinco ventanas altas) con el **presbiterio** alzado un escalon tras el
  arco triunfal: el **altar de la sangre** (`ALTAR_V` -24: la pila del altar llena —un caldero con agua—, calaveras y velas rojas) y
  detras la gran cruz rota con sus cadenas. Fuera: el **campamento** de los peregrinos al este (tiendas, la hoguera fria, barriles) y
  sus **tumbas** al oeste (cruces de madera y montones de tierra). Botin: `chests/altar` (velas rojas, cuerda, cuero, huesos, polvo
  de redstone, verruga, oro, papel, frutos, piedras de forja y la ceniza de la sangre) y `altar_bueno` (oro, frascos de experiencia,
  amatista, una manzana de oro, piedras de forja hasta III, el talisman del vigor y los hechizos de curar y de bendicion). Sitios (`Altar.sitio`): `entrada`, `atrio`,
  `puerta`, `nave`, `crucero`, `pontifice`, `altar`, `pila_este`, `pila_oeste`, `sacristia`, `atril`, `arcon`, `osario`,
  `campanario`, `campamento`, `tumbas` y `alto` (solo para mirar). El Espejo: "El Altar Profanado" (`Altar.dentro`); logro `altar`
  ("El altar tiene sed") al entrar en la basilica (`Altar.enNave`: la nave, el crucero y el abside, no los cuartos).
- **Los puestos** (`MorvainEnemigos`, sitio `altar`, de `Altar.esperas`): Anselmo (puesto 0, grupo `Puestos.JEFE`, de rodillas ante el
  altar en (0, -17), de espaldas a quien entra) y los penitentes: tres rezando en los bancos de atras de la nave (lejos de el: no le
  despiertan) y un turiferario de pie junto a la puerta; dos de pie y uno de rodillas en el atrio; en el campamento uno postrado, uno
  de rodillas y uno de ronda; dos en las tumbas y un turiferario que guarda el libro en la sacristia.
- **Anselmo** (`enemigos/Pontifice`, entidad `darkcraft:pontifice`, 1,1 x 3,1; hereda de `Guardian`): el obispo que guiaba a los
  peregrinos al crater a pedir perdon; la noche del cielo rojo la Sombra les contesto y el lo entendio como una orden: que dieran su
  sangre. Modelo de `tools/enemigos/pontifice.py` (un viejo altisimo y enjuto, la piel cerea, la barba blanca y larga y los ojos con
  luz roja; la **tiara** de tres coronas con su orbe y su cruz y las infulas; la muceta de armino con el broche, el palio, la capa
  rota por detras, el alba larga y las mangas anchas; en la derecha la **ferula** de tres travesanos —la cruz papal— con su nudo y su
  regaton y en la izquierda el **caliz** de oro lleno de sangre que luce; escala 1,45: `client/enemigos/PontificeModelo` —de rodillas
  rezando mientras no esta de pie— y `PontificeRenderer`, con la luz de los ojos y de la sangre del caliz, que late y se apaga al
  morir). 1100 de vida, golpe 12, armadura 5 (dureza 2), casi no se le empuja (0,9), **el fuego no le hace nada**. Espera **de
  rodillas** (`REZA`, su `sfx.pontifice_rezo` de vez en cuando).
- **La pelea** (solo con quien esta dentro de la basilica: `percibe` y `objetivoValido`; a los penitentes no les hace nada): al verle
  dice la frase 1 mientras se levanta y al acabarla **predica** (el sermon, su "rugido": los penitentes de alrededor —24— se
  enfervorizan, `animo` 0,25 240 ticks, y van a por su presa). De cerca el **ferulazo** (la alza y la estrella delante: 4,8, 50
  grados) y el **barrido** (4,5, 160 grados): los travesanos **cortan** (hacen sangrar, `Sangrado`: 18, ungido 28); la **aspersion**
  (de cerca o hasta 7: echa la sangre del caliz en abanico delante —`R_ASPERSION` 7,5, `ABANICO` 42 grados a cada lado, a la vista—:
  x0,35 del golpe, **sangrar** 34 —ungido 44— y frena un poco); la **comunion** (si hay alguien sangrando —25 o mas— a menos de
  `R_COMUNION` 14: alza el caliz por encima de la tiara; la sangre de cada uno que sangra vuela al caliz —le duele un poco, se le
  quita la sangre— y **le cura** lo que llevaba, hasta 70 por cada uno; despues **bebe**: mejor no llegar sangrando) y el **juicio**
  (de lejos —5 a 22— o, ungido, tambien de cerca: alza la ferula al cielo y la clava; bajo los pies de cada jugador —y de su presa—,
  hasta 24, sale una **marca** —un circulo de polvo rojo que gira y gotea— y a los `MARCA_T` 22 ticks la **columna de sangre**: a
  quien siga encima —`R_COLUMNA` 1,45— x0,85 del golpe, lo **lanza por el aire** y le hace sangrar 40; apartarse a tiempo). A **media
  vida** no predica: la **uncion** (se vierte el caliz por la cabeza; la frase 2): desde entonces sangra —gotas que le caen de la tiara
  y del alba— y deja **charcos de sangre** por donde pisa (`enemigos/CharcoSangre`, una nube de efecto de area: radio 1,8, 220 ticks;
  cada 10 hace sangrar 9 a quien este en el —no a los enemigos— y frena; cada `CHARCO_CADA` 55 ticks, mas un poco), juzga mas a menudo
  y con una marca mas cerca de cada uno, y de los canales de la nave se alza la **procesion** (`Altar.procesion`: diez sitios junto al
  canal, de dos a cuatro penitentes —uno mas que los jugadores—, los mas lejos de los jugadores, postrados en la sangre que se alzan de
  golpe; **efimeros**: sin puesto, se deshacen al acabar la pelea o a los 240 ticks). `Pontifice.mediaVida()`, `vencer()`, `quieta`
  (pruebas) y contadores (`ferulazos`, `barridos`, `aspersiones` / `asperjados`, `comuniones` / `bebidos` / `curado`, `juicios` /
  `marcadas` / `columnas` / `columnasQueDan`, `sermones` / `enfervoriza`, `alzados`, `charcos`; `CharcoSangre.SANGRAN`).
- **Al caer** se queda de rodillas, el caliz se le cae y se derrama delante de el (la sangre se extiende), dice su ultima frase y se
  **deshace en sangre** (`tickDeath` propio, 130 ticks: almas que suben y al final la sangre que revienta); lo demas lo reparte
  `valdrenn/Guardianes`: el cartel de JEFE ABATIDO con 17000 ecos, la hazana `pontifice` ("Sin mas sangre", con su entrada del
  diario) y el bestiario; deja tres piedras de forja III, dos II y la ceniza de la sangre. Y **la Tierra sin Fe descansa** (`Altar.paz`, cada
  segundo desde `Morvain.onTick`): los penitentes que seguian **rezando de rodillas** (los `REZA` del Puente de la Grieta y del altar;
  no los que pelean) se deshacen en sangre (`DESCANSAN`) y **ya no vuelven** (los sitios `grieta` y `altar` se registran con
  `activo`).
- **Voz** (ElevenLabs, `eleven_v4`, **"Supernal"** `J4Gklq1lvygedV6xCpKJ`, flow `lCcsSxKt2uupHxtewNEY`; 5 frases, tomas a salvo la a;
  en `procesar.py` `voz_pontifice`: un poco mas grave, una segunda voz una quinta por encima muy baja y temblorosa —el coro que le
  contesta—, un coro lento, algo de raspa y el eco largo de la nave de piedra): linea **14 + N** (`Voces.BASE_PONTIFICE`, `esPontifice`;
  `habla_pontifice` en `rey_voces.json`; subtitulos `darkcraft.pontifice.voz.N`): 1 al empezar ("Arrodillate, hijo mio. El altar
  tiene sed... y tu aun estas tan lleno."; no la repite en 4 min), 2 al ungirse ("¿Lo ves? Yo tambien doy la mia. Alzaos,
  hermanos..."), 3 si cae su presa ("Ya esta. Ahora estas perdonado."), 4 la primera vez que bebe la sangre de alguien ("Tu sangre es
  una oracion... y yo la oigo.") y 5 al caer ("Perdonadme... yo solo queria... que alguien contestara.").
- **Barra** (`GuardianCliente`, estilo "sangre": letras de cera palida y gotas de sangre que caen; arte de
  `tools/hud/barra_guardianes.py` "pontifice": la ferula de oro de punta a punta con la cruz de tres travesanos al final y la tiara en
  medio; relleno de sangre). **Musica**: "The Descecrated Temple" de Insydnis (OpenGameArt, CC-BY 3.0, id `pontifice`, tema 40 de
  `ClientMusic.boss`).
- **El altar suena** (`ClientMusic.tickSinFe`): a menos de 70 del crucero, en vez del viento de la Tierra sin Fe,
  `ambiente.altar` (el aire de la basilica: un coro lejano, goteo y el viento por los arcos).
- Sonidos (ElevenLabs, flow `lCcsSxKt2uupHxtewNEY`, `TOMAS_SFX`): `pontifice_sermon` (4 y 1), `pontifice_ferula` (3 y 1),
  `pontifice_golpe` (4 y 1), `pontifice_aspersion` (4 y 2), `pontifice_comunion` (4 y 1), `pontifice_bebe` (4 y 2), `pontifice_juicio`
  (3 y 2), `pontifice_columna` (1 y 4), `pontifice_uncion` (4 y 1), `pontifice_procesion` (4 y 3), `pontifice_dolor` (4 y 3),
  `pontifice_muerte` (1 y 4), `pontifice_rezo` (4 y 1) y `altar_ambiente` (3 y 1, `stream`).
- Comandos `/darkcraft pontifice [ir | altar <sitio> | vencer | volver]` (sin mas: como esta; `volver` le pone otra vez de rodillas y
  vuelven los penitentes que rezaban, aqui y en el puente).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_altar carpeta`: la basilica, el atrio, el campamento y las tumbas con las
  marcas de Anselmo (A0) y los penitentes (Pn); si se llega a pie desde la entrada a cada sitio (menos `alto`) y a cada uno de los que
  esperan, que ninguno esta metido en un bloque, que todos tienen suelo y que el suelo de la nave y el crucero esta libre.
- Pruebas: servidor, lineas `Morvain, el altar profanado: ...` (el atril con el libro 29, el arcon, la pila del altar, el canal con
  su sangre, las pilas, la puerta, la campana, el suelo de la nave y del crucero; a pie desde la entrada; vistas `morvain_altar.png` y
  `morvain_altar_nave.png`), `Morvain, el Pontifice: ...` (de rodillas; al verle habla, se levanta y predica; cada accion acaba),
  `Morvain, los golpes del Pontifice a una presa: ...` (el ferulazo y el barrido que hacen sangrar, la aspersion, la comunion que bebe
  y cura, el juicio con sus marcas y la columna que lanza), `Morvain, el Pontifice a media vida: ...` (se unge, la procesion, los
  charcos que hacen sangrar), `Morvain, el Pontifice cae: ...` (se deshace en sangre, los penitentes que rezaban descansan y no
  vuelven) y `Morvain, el Pontifice vuelve: ...`; cliente, tramo `altar()` despues de la grieta: desde el santuario (`cliente_mv370`),
  la fachada (`mv371`), el campanario (`mv372`), entrar a pie con el cartel, el logro y su aire (`mv373`), la nave hacia el altar
  (`mv374`), el crucero con las pilas (`mv375`), Anselmo de rodillas (`mv376`), el presbiterio (`mv377`), la sacristia (`mv378`), el
  osario (`mv379`), todo desde lo alto (`mv380`); en supervivencia habla (`mv381`) y predica con su barra (`mv382`); sus golpes
  (`mv383..mv388`: el ferulazo, el barrido, la aspersion, la comunion, el juicio con su marca y la columna), a media vida se unge
  (`mv389`), la procesion (`mv390`), los charcos (`mv391`), cae de rodillas con el caliz derramado (`mv392`), se deshace en sangre
  (`mv393`), el cartel (`mv394`), su ultimo sermon (`mv395`) y el bestiario (`mv396`). `LIMIT` 101 min (`lanzar.sh` 107 y 105 min, el
  paso 135, el trabajo 183).

### Plan grande, parte 7u: el Crater de Sangre — los Marcados y el Cerco del Ancla

Respuestas 125, 131 y 132 del plan; la novena region con su contenido: el crater, al pie de la cadena del sur (despues, la **7v**:
Aldric, el Primer Marcado, el jefe del crater, que abre la subida por la cadena; luego los especiales y la Sombra).
- **El marcado** (`enemigos/Marcado`, entidad `darkcraft:marcado`, 0,65 x 1,95; modelo de `tools/enemigos/marcado.py`, escala 1,04;
  `MarcadoGeometria/Animaciones`, `MarcadoAnims`, `client/enemigos/MarcadoModelo` y `MarcadoRenderer` —el rompedor a escala 1,12—).
  Los de Morvain a los que la Sombra saco de la muerte ANTES que al jugador, con la misma marca (el ojo en el aro roto con tres
  espinas, en la capa de brillo con los ojos), para que rompieran sus cadenas; no pudieron. Tres: **rompedor** (58 de vida, 7,5 de
  golpe, 5 de armadura; desnudo bajo el delantal de herrero, grilletes y la marca a fuego en la frente, el mazo de forja: el
  **mazazo** —lo alza sobre la cabeza y lo estrella: la **onda**, a `ONDA_DELANTE` 2,9 y radio `R_ONDA` 2,7, x0,6 del golpe a quien
  este de pie, lo levanta y le frena— y el **barrido**; de rodillas golpea el suelo al pie del ancla al ritmo de su bucle, con el
  golpe de la cadena —`sfx.marcado_cadena`— y chispas, todo en el cliente), **caballero** (48, 6, 8; armadura negra con la marca en
  la coraza, yelmo con espina, escudo con el ojo pintado: el **tajo**, la **estocada** a fondo —de 3,8 a 7,5 bloques, cada 5 s— y el
  **escudazo**, que se lleva 32 de estamina y frena; **se cubre** de frente —70 grados a cada lado, quieto o en su `BLOQUEO`—: el
  golpe x0,25 (`ESCUDO`), las flechas nada; ni fuego ni explosiones ni lo que salta la armadura) y **cazador** (34, 5, 2; capucha,
  embozo y gaban, el arco y la aljaba: el **disparo** —`Flecha`, con `tiro`— desde lejos y desde lo alto —16 de desnivel—, la **flecha
  negra** —`enemigos/FlechaNegra`: tensa 29 ticks con la sombra enroscandose; a quien da, `DARKNESS` 130 y `WEAKNESS` 90; cada 9 s—, la
  **punzada** con la flecha de la mano y el **desliz** —si se le acercan: se deshace en sombra y aparece de 4,5 a 7,5 bloques, de lado o
  hacia atras, donde haya suelo; cada 5 s—). **La marca arde** a media vida (fogonazo rojo, brasas del pecho; sincronizado `ARDE`):
  desde entonces cada golpe y cada disparo lo repite su **reflejo** `RETRASO` 12 ticks despues desde donde lo dio (otro `Marcado` con
  `REFLEJO`: medio transparente y oscuro —`MarcadoModelo` translucido—, sin gravedad, no se le apunta, no se le empuja ni se le puede
  pegar, no se guarda; hace ese golpe hacia donde se dio y se deshace al acabar). Esperan **de rodillas** (`REZA`: se levantan),
  **caidos de bruces** como un muerto mas (`TUMBADO`: se alzan de golpe, `ALZADOS`) o de guardia. Al morir se llevan la mano a la
  marca, caen de bruces y **se deshacen en sombra** (`tickDeath` propio, `DESHECHOS`). Deja piedra de forja I (8 %) y II (7 % el
  rompedor, 4 %). Bestiario `marcado_rompedor` / `_caballero` / `_cazador`; logro `marcado` ("Lo que pudiste ser"). Contadores para las
  pruebas: `MAZAZOS`, `ONDAS`, `BARRIDOS`, `TAJOS`, `ESTOCADAS`, `ESCUDAZOS`, `PARADOS`, `DISPAROS`, `NEGRAS`, `NUBLADOS`, `PUNZADAS`,
  `DESLICES`, `REFLEJOS`, `REFLEJOS_DAN`; `reflejoDe(accion)` y `encenderMarca()`.
- **El Cerco del Ancla** (`morvain/Cerco`, en la ladera de dentro del crater, al pie del ancla del sur —la de la cadena
  `Ciudadela.CADENAS[2]`— y a la puerta del santuario del Ancla del Sur —(50, 1080), 220 al sur, su tarima no se toca: `SANT_R` 6—;
  `CX` 0, `CZ` 860, `SUELO` el del ancla y `PISO` uno mas; u = x - CX, v = z - CZ, el norte —v negativa— hacia el lago y la ciudadela;
  se dibuja chunk a chunk al adornar y en su caja —`X0..Z1`— `Adorno` no pone nada). La **terraza** allanada (radio `R_LLANO` 39, se
  funde en `FUNDE` 10; corta como mucho hasta `PISO + 5`: la cadena pasa por encima) con el suelo roto a golpes; la **muralla**
  redonda (de `R_MURO0` 32,5 a `R_MURO1` 34,5, 6 de alto, almenas y la banda de piedra de sombra con sus runas) con la **puerta**
  apuntada al sur entre dos **torres** y cuatro mas en las esquinas (macizas, 10 de alto, merlones); el **ancla** (la piramide de la
  ciudadela: no se toca —`piramide`—, salvo la **escalera** tallada en su cara del sur, tres de ancho, hasta lo alto donde empieza
  la cadena, con sus velas rojas); alrededor, los golpes (cadenas rotas, piedra suelta y velas); la **fragua** al este (el hogar con
  las brasas y su chimenea, dos yunques rotos, dos altos hornos, el pilon de sangre, la piedra de afilar, los mazos rotos y montones de
  cadena y carbon), el **campamento** al oeste (cuatro petates, la hoguera fria con sus troncos, barriles y el arcon `chests/cerco`) y
  la **choza del primero** al sureste (la cama, la mesa con velas, el **atril con el libro perdido 30** —"El diario del primero", de
  Aldric; `Perdidos.TOTAL` = 30— y el arcon `chests/cerco_bueno`), ocho farolas de almas; y del santuario a la puerta el **camino de
  los marcados** (losas negras rotas por la ladera, nueve **estelas** —poste de piedra negra con su remate, una calavera o una vela al
  pie— y dos **jaulas** colgadas de su horca con sus huesos). Botin: `chests/cerco` (cadena, pepitas y lingotes de hierro, carbon,
  huesos, frutos, piedras de forja) y `cerco_bueno`. Sitios (`Cerco.sitio`): `entrada`, `camino`, `puerta`, `patio`, `ancla`, `cima`,
  `fragua`, `campamento`, `choza`, `atril`, `arcon`, `torre_este`, `torre_oeste` y `alto` (estos tres, solo para mirar). El Espejo:
  "El Cerco del Ancla" (`Cerco.dentro`: lo allanado y el camino); logro `cerco` ("Al pie de la cadena") al entrar en la muralla
  (`enCerco`, y no muy por encima).
- **Los puestos** (`MorvainEnemigos`, sitio `cerco`, 25, de `Cerco.esperas`): por el camino tres de rodillas mirando a la cadena y dos
  caidos de bruces; a la puerta dos caballeros de guardia y en lo alto de las torres los **cazadores** (las dos de la puerta y las dos
  del norte: `en_alto`, no se llega a ellos: estan para disparar); al pie del ancla cinco **rompedores de rodillas** golpeandola; en el
  patio dos caballeros de ronda y uno a la puerta de la choza; en lo alto del ancla, desde la 7v, **Aldric** (el puesto 0; en la 7u
  habia alli un cazador); en la fragua un rompedor de rodillas ante el yunque y otro de pie; en el campamento dos caidos en los
  petates y uno de rodillas junto a la hoguera.
- **El crater suena** (`ClientMusic.tickCrater`): en su region (`Region.CENTRO`) entre Y 40 y 330, `ambiente.crater` (las cadenas
  que crujen arriba, golpes de hierro lejanos, un zumbido grave y viento seco).
- Sonidos (ElevenLabs, flow `sjsGv1UGBLAnN0YbBhO1`, `TOMAS_SFX`): `marcado_alerta` (1 y 3), `marcado_dolor` (2 y 4), `marcado_muerte`
  (1 y 2), `marcado_mazazo` (3 y 1), `marcado_cadena` (1 y 3), `marcado_reflejo` (2 y 3), `marcado_desliz` (4 y 3), `marcado_flecha`
  (1 y 4), `marcado_susurro` (2 y 3, el sonido de estar), `marcado_esfuerzo` (3 y 1) y `crater_ambiente` (3 y 1, `stream`).
- Comandos `/darkcraft cerco [ir <sitio> | enemigos [volver | fuera | sacar]]` (sin mas: como esta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_cerco carpeta` (marcas R rompedor, C caballero, Z cazador): si se llega a pie
  desde la entrada a cada sitio (menos lo alto de las torres y `alto`) y a cada marcado (menos los de lo alto de las torres), que
  ninguno esta metido en un bloque, que todos tienen suelo, que se sube a lo alto del ancla y que el cerco no toca ni el hierro de la
  cadena ni la piramide (salvo la escalera).
- Pruebas: servidor, lineas `Morvain, el cerco del ancla: ...` (el atril con el libro 30, el arcon, la escalera 8/8, la puerta, el
  ancla, el pilon, el suelo del patio; a pie desde la entrada; vistas `morvain_cerco.png` y `morvain_cerco_patio.png`), `Morvain, los
  marcados: ...`, `Morvain, los marcados en accion: ...` (el de rodillas se levanta y el caido se alza; cada accion de cada uno acaba),
  `Morvain, los marcados contra una presa: ...` (el mazazo y su onda, el barrido, el tajo, la estocada que se tira, el escudazo, el
  disparo, la flecha negra que nubla y debilita, la punzada y el desliz), `Morvain, el escudo del caballero: ...` (de frente, por la
  espalda, una flecha; la marca arde a media vida), `Morvain, el reflejo de los marcados: ...` (el mazazo, el tajo y el disparo
  repetidos; no se le puede pegar; se deshace) y `Morvain, un marcado que cae: ...`; cliente, tramo `cerco()` despues del altar: desde
  el santuario (`cliente_mv397`), el camino (`mv398`), entrar a pie con el cartel, el logro y el aire del crater (`mv399`), la puerta
  (`mv400`), el patio con los rompedores (`mv401`), la escalera (`mv402`), la cadena desde lo alto del ancla (`mv403`), la fragua
  (`mv404`), el campamento (`mv405`), la choza y su atril (`mv406`), todo desde lo alto (`mv407`); en supervivencia, en el patio: el
  mazazo (`mv408`), el barrido (`mv409`), el tajo (`mv410`), la estocada (`mv411`), el escudazo (`mv412`), el escudo (`mv413`), el
  disparo (`mv414`), la flecha negra (`mv415`) y la vista nublada (`mv416`), la punzada (`mv417`), el desliz (`mv418`), la marca que arde
  (`mv419`) y su reflejo (`mv420`), uno caido de bruces (`mv421`) que se alza (`mv422`), uno que cae (`mv423`) y se deshace en sombra
  (`mv424`), el diario del primero (`mv425`) y el bestiario (`mv426`). `LIMIT` 105 min (`lanzar.sh` 111 y 109 min, el paso 139, el
  trabajo 187).

### Plan grande, parte 7v: Aldric, el Primer Marcado (el jefe del Crater de Sangre)

Respuestas 125 y 131 del plan; el noveno jefe de Morvain, el del libro 30 ("El diario del primero"), en lo alto del ancla del
Cerco (parte 7u). Despues, los especiales (el dragon, el jinete nocturno, los gigantes errantes, los jefes secretos) y la subida
por la cadena a la ciudadela y la Sombra.
- **Aldric** (`enemigos/Aldric`, entidad `darkcraft:aldric`, 1,2 x 3,0, inmune al fuego; hereda de `Guardian`): el primero al que
  la Sombra saco de la muerte y le puso su marca; golpeo la cadena del sur mil inviernos, subio por ella hasta ver la ciudadela de
  cerca y bajo para no dejar subir a nadie con la marca. Modelo de `tools/enemigos/aldric.py` (escala 1,42: un viejo enorme y
  consumido con la piel agrietada de los marcados, pelo y barba cenicientos, la coraza negra con el agujero quemado por donde luce la
  marca, la hombrera de placas, el collar de un grillete roto, la capa roja quemada; en el brazo derecho la cadena enrollada y en la
  mano un trozo de la cadena de la Sombra —cuatro tramos— que acaba en el eslabon roto: su arma). **La cadena se anima con su propia
  cuenta**: `anim.py` tiene un gancho `posproceso` y `aldric.py` resuelve `_cadena` despues de los brazos (colgando con su peso y lo
  que sobra tendido en el suelo, en linea hacia donde se diga o tramo a tramo). `AldricGeometria/Animaciones`, `AldricAnims`
  (cadenazo, latigazo, estrella, torbellino, salto, encadenar, rugido, marca, aturdido, morir, levantarse; bucles quieto, arrastra,
  andar, listo y rezando), `client/enemigos/AldricModelo` (de rodillas rezando mientras no esta de pie; andando en pelea arrastra la
  cadena) y `AldricRenderer` (1,42; la luz de la marca late mas fuerte cuando le arde y se apaga al morir; el reflejo, translucido y
  rojo oscuro como el de los marcados). 1300 de vida, golpe 13, armadura 8 (dureza 3), casi no se le empuja (0,95). Espera **de
  rodillas** en lo alto del ancla (`REZA`, de cara a la puerta, con su `sfx.aldric_rezo` de vez en cuando).
- **La pelea** (solo con quien esta dentro del cerco: `percibe` —de rodillas ve a `VE` 20, a la vista— y `objetivoValido`; a los
  marcados no les hace nada): al verle dice la frase 1 mientras se levanta (no ataca ni se mueve hasta acabarla) y al acabarla
  **ruge**: los marcados del cerco a menos de `Cerco.R_RENDIR` (42) del ancla se **rinden** (`Marcado.rendir`: se arrodillan donde
  estan —los que estaban golpeando, al acabar—, sueltan a su presa y no toman otra; si un jugador les pega dejan de estar rendidos y
  pelean; `sfx.cerco_arrodillan`) y **la cadena cierra la puerta** del cerco (`Cerco.tick`, desde `Morvain.onTick` con alguien a 120:
  barrera en la cara de dentro del hueco y cadenas de pie en la de fuera, chispas rojas; antes saca hacia dentro a quien este en el
  hueco; se abre sola si la pelea acaba, y 120 ticks despues de caer el —mientras dice su ultima frase—; lo cerrado se mira en el
  mundo: si el servidor se para a media pelea, al volver se abre). Desde lo alto del ancla (o desde donde este mas alto que su presa
  —3—) **salta** sobre ella: se agacha, en el tick `DESPEGA` 12 sale con la velocidad justa para caer a un paso (`subida`: la que con
  la gravedad del juego llega a esa altura en `CAE - DESPEGA` ticks; hacia abajo sale igual hacia arriba —0,35— y el vuelo dura lo que
  diga `vuelo`, con lo de lado repartido en eso) y al caer la **onda** (`R_SALTO` 4,2). De cerca el **cadenazo** (de lado a lado:
  5,2, 150 grados, frena un poco), la **estrella** (la alza al cielo y la estrella delante: la onda a `ESTRELLA_DELANTE` 3,9, radio
  3,1, a quien este de pie) y el **torbellino** (dos vueltas: pega dos veces todo alrededor); de 4 a 8,2 el **latigazo** (recto hasta
  8,4: a quien alcanza **lo arrastra** hacia el); de 7 a 18, a veces, otro salto. A **media vida** (`alSegundaMitad`, la frase 2) le
  **arde la marca** (`ARDE`, sincronizado: el **estallido** —brasas alrededor, x0,55 del golpe y fuego a `R_MARCA` 4,4—) y desde
  entonces su **reflejo** repite cada golpe de cadena `RETRASO` 12 ticks despues desde donde lo dio (otro `Aldric` con `REFLEJO`: no se
  le apunta ni se le empuja ni se le puede pegar, no se guarda, se deshace al acabar; pega a los jugadores y a la presa del de verdad)
  y **encadena** (golpea el suelo con el puno —la frase 4 la primera vez—: bajo los pies de cada jugador a 24, y de su presa, una
  **marca** —un aro de brasas que se cierra— y otra delante de por donde va; a los `CADENA_T` 22 ticks brotan tres **cadenas** del
  suelo —`EfectosRaiz.estaca` con cadenas—: x0,7 del golpe y **sujeta** `SUJETA` 50 ticks —lentitud 5: casi no se anda ni se salta—).
  Si cae su presa, la frase 3. `Aldric.mediaVida()`, `vencer()`, `reflejoDe(accion)`, `quieta` (pruebas) y contadores (`cadenazos`,
  `latigazos` / `arrastrados`, `estrellas` / `ondas`, `torbellinos`, `saltos` / `caidas` / `ondasSalto`, `encadenados` / `marcadas` /
  `brotes` / `sujetos`, `reflejos`, `rendidos`, `estallidos`; `REFLEJOS_DAN`).
- **Al caer** se queda de rodillas mirando a la ciudadela, dice su ultima frase y **se deshace en sombra** (`tickDeath` propio, 160
  ticks: la marca que se apaga, la sombra que sube y al final el estallido de sombra y almas); lo demas lo reparte
  `valdrenn/Guardianes`: el cartel de JEFE ABATIDO con 20000 ecos, la hazana `aldric` ("El primero", con su entrada del diario) y el
  bestiario; deja tres piedras de forja III, dos II y la ceniza del torbellino. Y **el cerco queda en paz** (`Cerco.enPaz`: su puesto
  caido; `Cerco.paz`, cada segundo desde `Morvain.onTick`): los marcados que seguian **de rodillas** y los **rendidos** (sin presa y
  sin hacer nada) se deshacen en sombra (`DESCANSAN`); los de rodillas **ya no vuelven** (el sitio `cerco` se registra con `activo`:
  los `REZA` solo mientras Aldric vive) y los rendidos cuentan como caidos (vuelven al descansar, como cualquiera). La cadena del sur
  queda libre (la subida, mas adelante). Si la pelea acaba sin caer el (todos caen o se van), los rendidos se levantan
  (`soltar`) y la puerta se abre.
- **Voz** (ElevenLabs, `eleven_v4`, **"Frederick - Old Gnarly Narrator"** `uVKHymY7OYMd6OailpG5`, flow `i8PApavQwvirt3cERg4n`; 5
  frases, tomas a; en `procesar.py` `voz_aldric`: un poco mas grave, una segunda voz una octava por debajo sorda y muy baja —la marca
  que le arde dentro—, algo de raspa y el eco seco y espaciado del crater): linea **120 + N** (`Voces.BASE_ALDRIC`, `esAldric`;
  `habla_aldric` en `rey_voces.json`; subtitulos `darkcraft.aldric.voz.N`): 1 al empezar ("Otro mas... con la marca. Vuelve abajo,
  muchacho. Alli arriba no hay descanso."; no la repite en 4 min), 2 al arderle la marca ("¡Yo tambien subi! Vi lo que espera
  arriba... ¡No dejare que te tenga!"), 3 si cae su presa ("Descansa. Mejor aqui... que alli arriba."), 4 la primera vez que encadena
  ("La cadena sujeta todo lo que toca.") y 5 al caer ("Ve, entonces... sube. Y cuando abra la boca... no le contestes.").
- **Barra** (`GuardianCliente`, estilo "cadena": letras de hierro al rojo y brasas que caen; arte de `tools/hud/barra_guardianes.py`
  "aldric": la cadena de hierro negro de punta a punta —eslabones tumbados y de pie—, el gancho a la izquierda, el eslabon roto que
  cuelga a la derecha y la marca al rojo en medio; relleno de hierro al rojo con vetas de fuego). **Musica**: "A Champion's Will" de
  ATMANAN (OpenGameArt, CC-BY 4.0, id `aldric`, tema 41 de `ClientMusic.boss`).
- Sonidos (ElevenLabs, flow `GnQ7JmflqS52oHTbj1uG`, `TOMAS_SFX`): `aldric_cadenazo` (4 y 2), `aldric_latigazo` (4 y 3),
  `aldric_estrella` (3 y 1), `aldric_torbellino` (1 y 3), `aldric_salto` (1 y 3), `aldric_encadenar` (1 y 4), `aldric_marca` (2 y 4),
  `aldric_rugido` (2 y 3), `aldric_dolor` (1 y 4), `aldric_muerte` (4 y 2), `aldric_rezo` (1 y 2), `aldric_arrastre` (2 y 4; la cadena
  que arrastra al andar en pelea) y `cerco_arrodillan` (3 y 1; los rendidos que se arrodillan).
- Comandos `/darkcraft aldric [ir | vencer | volver | puerta [abrir]]` (sin mas: como esta; `volver` le pone otra vez de rodillas y
  vuelven los marcados del cerco como estaban; `puerta abrir` rompe a mano la cadena de la puerta).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_cerco carpeta` escribe si la caja de Aldric en lo alto del ancla esta libre
  (`Aldric en ...: libre`, tambien de la cadena) y su suelo; la cima del ancla a pie ya la miraba (la 7u).
- Pruebas: servidor, lineas `Morvain, Aldric: ...` (de rodillas en lo alto del ancla y en su suelo; al verle habla, se levanta, ruge
  —los marcados del cerco se rinden y uno rendido no toma presa; la puerta cerrada: barreras y cadenas— y salta desde lo alto hasta
  su presa, en el suelo del patio; cada accion acaba), `Morvain, los golpes de Aldric a una presa: ...` (el cadenazo, el latigazo que
  arrastra, la estrella con su onda, el torbellino que pega dos veces, el salto con su onda y encadenar: las marcas, las cadenas que
  brotan y sujetan), `Morvain, Aldric a media vida: ...` (la marca que estalla; el reflejo que repite el cadenazo, no se le puede pegar
  ni apuntar y se deshace), `Morvain, Aldric cae: ...` (la puerta cerrada mientras pelea y mientras dice su ultima frase y abierta
  despues, se deshace en sombra, los de rodillas y los rendidos descansan y los de rodillas ya no vuelven) y `Morvain, Aldric vuelve:
  ...`; cliente, tramo `aldric()` despues del cerco: de rodillas en lo alto del ancla (`cliente_mv427`); en supervivencia en el patio
  habla mientras se levanta (`mv428`), ruge y los marcados se arrodillan (`mv429`), la cadena cierra la puerta (`mv430`), salta desde
  lo alto (`mv431`) y cae con su onda (`mv432`); sus golpes de lado (`mv433..mv436`: el cadenazo, el latigazo, la estrella, el
  torbellino), le arde la marca (`mv437`), su reflejo (`mv438`), encadena: las marcas (`mv439`) y las cadenas (`mv440`); cae de rodillas
  (`mv441`), el cartel (`mv442`), se deshace en sombra (`mv443`), el cerco en paz (`mv444`), la puerta abierta (`mv445`) y el bestiario
  (`mv446`). `LIMIT` 109 min (`lanzar.sh` 115 y 113 min, el paso 143, el trabajo 191).

### Plan grande, parte 7w: los gigantes errantes de Morvain

Respuesta 132 del plan (los especiales: "dragon, jinete nocturno —solo de noche, en caminos—, gigantes errantes, jefes secretos tras
muros ilusorios"); la primera tanda: los gigantes. Despues el jinete nocturno, los jefes secretos, el dragon y la subida por la cadena.
- **Tres gigantes** (`enemigos/Gigante`, entidad `darkcraft:gigante`, 3,0 x 7,8, inmune al fuego, `noSave`, `clientTrackingRange` 16;
  hereda de `Guardian`; variante = gigante = su vuelta): 0 **Ogmund, el de la Campana** (las Llanuras Palidas; la campana de su aldea,
  que ya no existe, atada al cabo de una viga; dobla al andar), 1 **Ulfgar, el de las Costillas** (la Costa de los Huesos; pega con una
  costilla de los suyos) y 2 **Haldor, el de la Columna** (junto a la Capital; una columna de marmol con su capitel; la piel se le va
  volviendo piedra). `tipo()` / `logro()` = `gigante_campana`, `gigante_costilla`, `gigante_columna` (bestiario, nombre en la barra
  —`variante(v)` le pone el nombre—, cartel y hazana). 2200 de vida, golpe 16, armadura 6 (dureza 4), no se le empuja; 12000 ecos.
- **El modelo** (`tools/enemigos/gigante.py`): hecho **al doble de lo normal** (torso 24 x 30 x 14, brazos de 36, piernas de 28, cabeza
  pequena y adelantada entre los hombros, el cogote de toro, la panza, los punos y los pies descalzos) y dibujado a 1,8 (unos ocho
  bloques): asi la textura (256 x 512) tiene el detalle de los bloques. **Las armas largas van "tumbadas"**: una pieza hija de la mano
  girada 90 grados con las cajas a lo largo de su Y (con las cajas a lo largo de Z, su zona de la textura se comia media hoja). **La
  campana cuelga a plomo** donde se pide (`_campana` en la pose: un `posproceso` que le pone el giro que deja su +Y hacia abajo, sea cual
  sea la pose del brazo; en el mazazo cae boca abajo contra el suelo; alzada, rigida). Piezas de cada uno en `OCULTAS` (y `roca_mano`, la
  roca que arranca, en las tres: el modelo del juego la ensena entre que la arranca y la tira). Bucles `quieto`, `porte_campana` /
  `porte_costilla` / `porte_columna` (los brazos con lo suyo: la viga al hombro con la campana a la espalda, la costilla a rastras, la
  columna al hombro), `andar` (un paso cada 1,6 s de su animacion) y `listo`; acciones (`GiganteAnims`): mazazo (64 ticks, golpe 30),
  barrido (48, 26), pisoton (40, 22), patada (32, 16), roca (64; la arranca en el 20 y la tira en el 42), rugido (56, 20), aturdido (60)
  y morir (180: de rodillas en 1,6 s, habla, de bruces en 7,4 s). `generar.py` acepta `ZOOM_VARIANTES`, `ZOOM_TIRA` y `LADO_TIRA` del
  modulo (las hojas de revision de algo de ocho bloques). `client/enemigos/GiganteModelo` (el paso al ritmo de lo andado:
  `animateWalk` con escala `ESCALA_PASO` 2,5) y `GiganteRenderer` (1,8; los ojos se apagan al caer de bruces).
- **Sin fisica** (`noPhysics`, sin gravedad, sin navegador ni metas del juego: `registerGoals` solo pone la de buscar presa): anda
  derecho (`andarHacia`: gira como mucho 2 grados por tick por su vuelta y 3,5 en pelea; si tiene que girar mucho, casi en el sitio) y
  los pies a ras de suelo (`pies` / `Gigante.suelo`: en Morvain lo mas alto del `Terreno` bajo las cuatro esquinas de sus pies —no los
  arboles ni las ruinas—; en otro mundo, el mapa de alturas; como mucho 0,3 por tick). Se mueve antes de `super.aiStep` (asi el paso de
  la animacion del juego, que se calcula en `travel`, cuenta lo andado). No se le empuja, no lo empuja el agua, no se guarda; la caja
  para no dejar de dibujarse llega 8 bloques (el arma).
- **Por donde van** (`morvain/RutasGigantes`: tres vueltas cerradas de puntos, 5400, 5249 y 4736 bloques, por tierra llana —pendiente
  como mucho 0,09—, seca y lejos de los sitios; las eligio `tools/morvain/Rutas.java` con el `Terreno`, que tambien dibuja el mapa de
  cada una) y **cuando estan** (`morvain/Gigantes`, desde `Morvain.onTick`; SavedData `darkcraft_gigantes`: lo andado de cada uno, si
  ha caido y donde quedo la campana): cada uno anda su vuelta a `Gigante.PASO` (0,1 bloques por tick: dos por segundo) **aunque no le
  vea nadie** ("de memoria", cada medio segundo); **sale** (la entidad) cuando un jugador de Morvain esta a `SALE` 112 de por donde va y
  ese sitio procesa entidades (`isPositionEntityTicking`); se **recoge** (sin guardarse; sigue de memoria) cuando no queda nadie a
  `SE_VA` 160 y no pelea. Por su vuelta va hacia un punto 14 bloques mas adelante (asi dobla las esquinas en curva); si su presa se aleja
  de su vuelta mas de `CORREA` 48 (o el mas de 64), la deja y vuelve. **El que cae no vuelve nunca.**
- **La pelea**: ve a quien tiene delante a 30 (`FOLLOW_RANGE`, como los demas: la mitad agachado). Al empezar dice su frase (no la
  repite en 4 min) y se le acerca sin pegar hasta acabarla; al acabarla **ruge** (empuja a los de alrededor —7— y los deja sin fuerzas
  3 s). A sus pies (menos de 4,4) el **pisoton** (alza la rodilla y pisa: la onda alrededor del pie, 4,6, a los de pie: x1,25 y los
  levanta) o la **patada** (5,6, 110 grados, x0,9 y los lanza lejos y hacia arriba); a la distancia de su arma (3,2 a 8,6) el **mazazo**
  (lo alza a dos manos y lo estrella delante, a 6 bloques y algo a su derecha: de lleno —2,6— x1,6; la **onda** —4,8— x0,7 a los de pie;
  se queda clavado un momento) o el **barrido** (de su derecha a su izquierda, 9 bloques, 200 grados, x1,15; no llega al que esta
  pegado a sus piernas); de lejos (11 a 40, a la vista) la **roca** (se agacha, la arranca del suelo con la izquierda y se la tira:
  `enemigos/RocaGigante`, un `ThrowableItemProjectile` de 1,4 que vuela en arco —`tiro`: de 18 a 44 ticks segun lo lejos— y al caer
  revienta: de lleno x1,4 y alrededor —`RADIO` 3,2— x0,75; la dibuja `RocaGiganteRenderer` con dos bloques de piedra que dan vueltas). Si
  se le pega mucho en poco tiempo (el 7 % de su vida en 6 s) **cae sobre una rodilla** (aturdido 3 s, una vez cada 30 s: la cabeza a
  tiro y **los golpes le entran un 50 % mas**). A media vida ruge otra vez y dice su frase 2. Pruebas: `quieta`, `parado`, `vencer()`,
  `mediaVida()`, `derribar()` y contadores (`mazazos` / `golpesMazazo` / `ondasMazazo`, `barridos`, `pisotones` / `ondasPisoton`,
  `patadas`, `rocas`, `aturdidos`, `empujados`; `RocaGigante.CAEN` / `DAN`).
- **Al caer** (`die` → `Gigantes.caido`: ya no vuelve): de rodillas dice su ultima frase y cae de bruces (el golpe, el polvo); lo demas lo
  reparte `valdrenn/Guardianes` (le llega por `Puestos.alCaer` como a cualquier guardian): el cartel de JEFE ABATIDO con sus ecos, la
  hazana (`gigante_campana` "La campana dobla", `gigante_costilla` "Junto al mar", `gigante_columna` "Piedra a piedra", con su entrada
  del diario) y el bestiario. **Donde cae queda su arma, de bloques** (`Gigantes.monumento`, solo en aire o lo que se aparta): la viga
  (troncos de roble oscuro) y **una campana de verdad** (la de Ogmund: "tocala... por mi, una vez": quien la toca —`onCampana`— la oye
  sonar por las Llanuras y apunta `campana_ogmund` en su diario), la costilla de Ulfgar (un arco de bloques de hueso hincado en la tierra)
  o la columna de Haldor tumbada (cuarzo, con el capitel cincelado y un trozo partido).
- **Lo que se nota en el cliente** (`client/enemigos/GiganteCliente`, sin paquetes): **los pasos** (cada vez que planta un pie —dos por
  ciclo de su animacion de andar: cada `PASO` puntos del paso del juego—: suena el paso, el polvo del suelo bajo el pie y la camara
  tiembla un poco, menos cuanto mas lejos) y **los golpes contra el suelo** (el mazazo, el pisoton, el rugido, la rodilla, las rodillas
  y el cuerpo al morir) y la roca al reventar (`RocaGigante.AL_CAER`, desde `onClientRemoval`): la camara tiembla fuerte si se esta cerca.
- **Voz** (ElevenLabs, `eleven_v4`, **"Mossbeard | The God of the Wild"** `bFrjFL4nlpeYNwNRhXxq`, flow `MO9WC12B0DMR0NNuRERt`; 9
  frases, tomas a; en `procesar.py` `voz_gigante`: un 12 % mas grave, una segunda voz una octava por debajo, el pecho, algo de raspa y
  el eco abierto del campo): linea **130 + 3 x gigante + N** (`Voces.BASE_GIGANTE`, `esGigante`, `gigante(linea)`; `habla_gigante` en
  `rey_voces.json`; subtitulos `darkcraft.gigante.voz.1..9` con el nombre `darkcraft.gigante.nombre_corto.<gigante>`): Ogmund 1 "Pequeno...
  no deberias estar aqui. La campana... dobla por ti.", 2 "Cada toque... es un nombre... ¡un nombre que no pude salvar!", 3 "Tocala...
  por mi, pequeno. Tocala... una vez."; Ulfgar 4 "Estos huesos... eran mis hermanos. Y tu... caminas sobre ellos.", 5 "La Sombra se los
  llevo a todos... ¡No te llevaras al ultimo!", 6 "Dejame... con ellos. Junto al mar... junto al mar."; Haldor 7 "Yo levante estas
  murallas... piedra a piedra. Nada... entra.", 8 "¡El rey ya no esta! ¡La ciudad ya no esta! ¡Pero la piedra... RECUERDA!", 9
  "Construye... algo mejor... pequeno." (1 al empezar, 2 a media vida, 3 al caer).
- **Barra** (`GuardianCliente`: los `gigante_*` usan la misma, `gigante_barra.png`, letras de piedra clara; arte de
  `tools/hud/barra_guardianes.py` "gigante": el dintel de sillares viejos con grietas y musgo, el munon de una columna y la punta
  partida, y la cabeza de piedra de un gigante con los ojos cerrados —una rendija de luz—; relleno de tierra que corre; lascas y polvo
  que caen). **Musica**: "Stone Guardian" de Ronhul Maggot (OpenGameArt, CC-BY 4.0, id `gigante`, la version en bucle; tema 42 de
  `ClientMusic.boss`).
- Sonidos (ElevenLabs, flow `lu5d15B0nhsALspXT3Bi`, `TOMAS_SFX`): `gigante_paso` (1 y 4), `gigante_rugido` (2 y 4), `gigante_mazazo`
  (2 y 1), `gigante_barrido` (2 y 3), `gigante_pisoton` (4 y 1), `gigante_patada` (3 y 2), `gigante_arranca` (4 y 3), `gigante_roca`
  (4 y 1), `gigante_dolor` (1 y 4), `gigante_muerte` (3 y 1), `gigante_campana` (1 y 4; la campana de Ogmund) y `gigante_cadena` (1 y
  4; lo que cruje al andar).
- Comandos `/darkcraft gigante [<0-2> [ir | aqui | vencer | derribar | volver]]` (sin mas: por donde va cada uno; `ir` lleva delante de
  por donde va —asi sale—; `aqui` lo saca 16 bloques delante; `volver`: si habia caido ya no lo esta y el que estaba fuera sale de nuevo).
- **Sin abrir el juego**: `tools/morvain/Rutas.java` (con `Ruido`, `Region`, `Terreno` y `RutasGigantes`) mide cada vuelta y la
  dibuja; `python3 tools/enemigos/generar.py --solo gigante --vistas carpeta`.
- Pruebas: servidor, lineas `Morvain, gigantes: vueltas ...` (lo que miden, la pendiente, sin agua, en su region; de memoria andan 20
  bloques en 10 s), `Morvain, gigante gigante_campana: ...` (anda por su vuelta con los pies a ras de suelo), `Morvain, gigante al ver a
  alguien: ...` (habla, ruge al acabar la frase, empuja), `Morvain, los golpes del gigante a una presa: ...` (el mazazo de lleno y su
  onda, el barrido, el pisoton, la patada que lanza, la roca que cae junto a la presa), `Morvain, el gigante sobre una rodilla: ...`,
  `Morvain, el gigante a media vida: ...` y `Morvain, el gigante cae: ...` (de bruces, su campana en el suelo, ya no vuelve; luego todo
  como estaba) y la vista `morvain_gigantes.png` (las tres vueltas sobre el mapa); cliente, tramo `gigantes()` despues de Aldric: cada
  uno por su vuelta (`cliente_mv447` Ogmund, `mv448` andando de lado, `mv449` Ulfgar, `mv450` Haldor), en supervivencia ante Ogmund habla
  con su barra (`mv451`) y ruge (`mv452`); sus golpes de lado (`mv453` el mazazo, `mv454` el barrido, `mv455` el pisoton, `mv456` la roca
  en la mano, `mv457` por el aire), sobre una rodilla (`mv458`), a media vida (`mv459`); cae de rodillas (`mv460`), el cartel (`mv461`),
  de bruces (`mv462`), su campana en el suelo (`mv463`) y suena al tocarla (`mv464`); y el bestiario (`mv465`). `LIMIT` 114 min
  (`lanzar.sh` 120 y 118 min, el paso 148, el trabajo 196).

### Plan grande, parte 7x: Ser Lucan, el Jinete Nocturno (el camino de nubes, de noche)

Respuesta 132 del plan, la segunda tanda de los especiales ("jinete nocturno, solo de noche, en caminos"). Lore: Ser Lucan era el
heraldo del rey Corvin; la noche en que el reino subio al cielo salio de la Torre de Ascenso con la ultima carta del Rey para la
Fortaleza de Hueso ("cierra la puerta de lo alto de tu torre y no la abras a nadie: ni al que lleve mi corona ni al que hable con mi
voz") y algo le alcanzo en el camino. Desde entonces cabalga cada noche y el alba lo deshace antes de ver la escalera. (Se llamo
"Ser Corwen" al principio: se cambio por Lucan, que se parecia demasiado a Corvin; en `sesiones.json` sigue el nombre viejo.)
- **El jinete** (`enemigos/Jinete`, entidad `darkcraft:jinete`, 1,6 x 3,3, inmune al fuego, `noSave`, `clientTrackingRange` 12,
  `updateInterval` 1; hereda de `Guardian`, tipo y logro `jinete`): un fantasma montado, **sin fisica** (`noPhysics`, sin gravedad) y
  con los cascos siempre a la altura del camino de nubes (`pies`, `NubesGenerador.SUELO`): galopa por el camino y, peleando, tambien
  por encima de la nube (donde nadie mas puede ir). 1100 de vida, golpe 12, armadura 10 (dureza 3), no se le empuja; 8000 ecos.
  Barra azul (`BossBarColor.BLUE`), musica tema 43.
- **El modelo** (`tools/enemigos/jinete.py`, variante "lucan", escala 1,15): el caballo (piezas `c_*`: cadera, torso, cuello, cabeza
  y mandibula, las patas como las del perro —brazo / antebrazo / mano delante, muslo / pierna / pata detras—, la cola en dos) con
  gualdrapa roja con la corona de oro, peto, crinera y testera con pua; el jinete (la cadera hija del torso del caballo, las piernas
  abiertas a los lados, tabardo, gola, yelmo con corona y penacho, capa en dos, el tubo de la carta con su lacre al costado, el cuerno
  al cinto y en la mano —`cuerno_mano`, que solo se ve tocandolo—) y la **guja** (pieza tumbada, `rot` 90: asta, regaton, anilla,
  hoja, punta y espolon). Bucles separados del caballo (`quieto`, `andar`, `galope`) y del jinete (`jinete_quieto`, `jinete_galope`):
  `client/enemigos/JineteModelo` los mezcla segun lo que corre (`CICLO_PASO` 2,6 y `CICLO_GALOPE` 4,6: el paso al ritmo de lo
  avanzado) y pone las acciones del jinete (tajo, barrido, cuerno) sobre el caballo que sigue andando (`base_de` en el generador).
  Acciones (`JineteAnims`): tajo (24 ticks, golpe 13), barrido (34, 18), carga (60), pisotea (40, 24), cuerno (48, 20), aturdido (60),
  aparece (30), desaparece (40) y morir (90). Se dibuja **translucido** (`JineteRenderer`: el alfa sube al alzarse, baja al
  deshacerse y al morir); capa de brillo (los ojos, el fuego de los cascos, el filo de la guja).
- **Por donde va** (`camino/Jinetes`; uno por camino de nubes de cada reino, clave "x,z" del reino; SavedData `darkcraft_jinetes`
  con los caidos): de noche (13000 a 23000) va del principio del camino hacia la fortaleza; **lo que lleva andado lo dice la hora**
  (`andado`: de 0 al caer la noche al 93 % del camino al amanecer —`FRACCION`—: nunca llega a la escalera); de dia no sale. **Sale**
  la entidad cuando un jugador del mundo de nubes (Y >= 200) esta a `SALE` 128 de por donde va y ese sitio procesa entidades: se alza
  de la niebla (`APARECE`) y el cuerno se oye de lejos; se **recoge** (sin guardarse) cuando no queda nadie a `SE_VA` 176 y no pelea.
  Por su camino va al trote (`TROTE` 0,32 bloques por tick) hacia un punto 9 bloques mas adelante; si se sale del camino (peleando),
  vuelve. **Al amanecer** (`amanecer`, desde `Jinetes.tick`): si hay alguien a 48 dice su frase 3 y se deshace (`DESAPARECE`); si
  no, se deshace sin mas. **El que cae no vuelve nunca a ese camino.**
- **La pelea**: ve a quien tiene delante a 40 (no repara en los que estan 12 bloques por encima o por debajo); si su presa se va a
  mas de `CORREA` 72, la deja. Al empezar dice su frase 1 (no la repite en 4 min por camino) y al acabarla **toca el cuerno** (su
  rugido: a 10 bloques lentitud II y debilidad 3,5 s, aro de almas y copos). De lejos (8,5 a 34, de cara) la **carga**: baja la guja
  como una lanza y cruza al galope (`CARGA` 0,95 por tick de los ticks 15 a 51) en linea recta por donde estaba su presa, llevandose a
  todo lo que pilla en una franja de 1,7 a cada lado (una vez por carga, x1,5, empuja fuerte); a veces grita "¡Por el Rey!" (frase 4).
  De cerca el **tajo** (5,4, 150 grados), el **barrido** (la guja da la vuelta entera mientras el caballo gira; a quien se le pone
  detras o si son varios) y el **pisoton** (el caballo se alza y cae con las manos: onda de 3,4 a 2,4 por delante, x1,3). Tras
  pegar a veces se aleja al galope para volver a cargar. **Lo que empuja en el camino lo empuja a lo largo del camino** (si la
  victima esta a menos de 3,5 de el: el empujon se endereza a la tangente; `fueraDelCamino` cuenta los enderezados): caer del camino
  es caer al vacio. Si se le pega mucho en poco tiempo (el 8 % de su vida en 5 s) **el caballo tropieza** y cae de rodillas 3 s (una
  vez cada 25 s; le entra un 30 % mas). **A media vida** el cuerno otra vez, la frase 2 y el **fuego de almas**: por donde galopa (y
  en la carga) quedan llamas azules 5,5 s que queman a quien las pisa (3 cada medio segundo y prenden). Pruebas: `quieta`, `parado`,
  `vencer()`, `mediaVida()`, `derribar()` y contadores (`cargas` / `golpesCarga`, `tajos`, `barridos`, `pisotones` / `ondasPisoton`,
  `cuernos` / `helados`, `fuegosPuestos` / `fuegos` / `quemados`, `aturdidos`, `fueraDelCamino`).
- **Al caer** (`die` → `Jinetes.caido`): su frase 5, el caballo cae y se deshace en almas; **la carta que llevaba** (el **libro
  perdido 31**, "La carta sin entregar", del rey Corvin por mano de Ser Lucan: el sobre y la carta del Rey, y al dorso la nota del
  heraldo; `Perdidos.TOTAL` = 31; en el codice con la miniatura de Valdrenn) a cada uno de los que le plantaron cara (y a los que
  esten a 48), con `Equipo.dar` y `Perdidos.encontrado`; lo demas lo reparte `valdrenn/Guardianes` (el cartel de JEFE ABATIDO con sus
  ecos, la hazana `jinete` "Todas las noches", con su entrada del diario, y el bestiario). Ojo: con el libro 31 la mascara de libros
  perdidos usa el bit de signo: las cuentas van con `>>> 1` (`Perdidos`, `MenuScreen`, `ClientCiTest`).
- **En el cliente** (`client/enemigos/JineteCliente`): el galope suena pegado a el (`EntityBoundSoundInstance`, relanzado cada 72
  ticks mientras corre), llamitas de almas en los cascos, el rastro de almas de la carga y la camara tiembla un poco con la carga y
  el pisoton (`GiganteCliente.sacudir`).
- **Voz** (ElevenLabs, `eleven_v4`, **"Zane - Deep Dramatic Late Night Voice (Neutral Accent)"** `mfxPGiKweaQEsXJix2Ve`, flow
  `tUkP1qh0LtTTd2tsgMDM`; 5 frases, tomas a; en `procesar.py` `voz_jinete`: un poco mas grave, la resonancia del yelmo, una segunda voz
  algo mas grave y retrasada —el fantasma que se dobla— y el eco abierto del cielo): linea **140 + N** (`Voces.BASE_JINETE`,
  `esJinete`; `habla_jinete` en `rey_voces.json`; subtitulos `darkcraft.jinete.voz.1..5` con el nombre `darkcraft.jinete.nombre_corto`):
  1 "¿Quien cabalga de noche por el camino del Rey? Apartate, forastero... o caeras bajo mis cascos.", 2 "¡Llevo la palabra del Rey!
  ¡Nada... detiene la palabra del Rey!", 3 "Otra vez el alba... y la escalera aun tan lejos.", 4 "¡Por el Rey! ¡Despejad el camino!"
  (de relleno), 5 "La carta... llevala a la fortaleza. Diles... que cabalgue todas las noches. Todas las noches."
- **Barra** (`GuardianCliente`, estilo de almas: motas que suben, nombre azul palido; arte de `tools/hud/barra_guardianes.py`
  "jinete": la guja tumbada —el asta de fresno negro con anillas y el regaton, el cubo y la hoja con el espolon, la punta que se alza y
  el filo encendido de almas—, el yelmo del heraldo con la corona y la rendija azul, y debajo la banderola roja con la corona; relleno
  de fuego de almas que corre; chispas de almas y esquirlas que caen). **Musica**: "Determined Pursuit (epic orchestra loop)" de
  Emma_MA (OpenGameArt, CC0, id `jinete`; tema 43 de `ClientMusic.boss`).
- Sonidos (ElevenLabs, flow `AyxqnFBiZuJQngB0Knnu`, `TOMAS_SFX`): `jinete_galope` (1 y 2; en bucle, entra en 0,3 s), `jinete_relincho`
  (3 y 4), `jinete_cuerno` (3 y 1; se oye a 128), `jinete_tajo` (2 y 3), `jinete_carga` (1 y 3), `jinete_pisotea` (2 y 1),
  `jinete_fuego` (3 y 1), `jinete_dolor` (2 y 4), `jinete_muerte` (4 y 3), `jinete_aparece` (1 y 4) y `jinete_arnes` (4 y 3).
- Comandos `/darkcraft jinete [ir | aqui | vencer | derribar | volver | noche | dia]` (sin mas: como esta el del camino de alrededor;
  `ir` lleva 30 bloques por delante de por donde va, de cara a el —asi sale—; `aqui` lo saca en el camino 18 bloques mas atras —hacia la torre—, de cara a quien lo pide;
  `volver`: si habia caido ya no lo esta; `noche` pone la hora 13500 y `dia` la 1000).
- **Sin abrir el juego**: `python3 tools/enemigos/generar.py --solo jinete --vistas carpeta`.
- Pruebas: servidor, `jinete()` (lineas `Jinete Nocturno, el camino ...` —lo andado a cada hora, lo que le falta al amanecer, un sitio
  lejos de las paradas: `Jinetes.libre`—, `Jinete Nocturno fuera: ...` —se alza, cabalga hacia la fortaleza con los cascos a ras del
  camino—, `... al ver a alguien: ...` —el cuerno al acabar la frase, hiela—, `..., sus golpes a una presa en el camino: ...` —el tajo,
  el barrido por detras, el pisoton, la carga y un tajo de lado: el empujon siempre a lo largo del camino—, `..., el caballo tropieza:
  ...`, `... a media vida: ...` —el cuerno y el fuego de almas que quema—, `... al amanecer: ...` y `... cae: ...` —ya no vuelve, la
  carta—); cliente, tramo `jinete()` despues de Morvain, en el camino de nubes del reino de la prueba, de noche (en el resto de la
  prueba no sale solo: `Jinetes.quietos`): sale solo al acercarse el jugador, se alza de la niebla (`cliente_jn01`), cabalga (`jn02`),
  en supervivencia habla con su barra (`jn03`) y toca el cuerno (`jn04`); sus golpes de lado (`jn05` la carga, `jn06` el tajo, `jn07`
  el barrido, `jn08` y `jn09` el pisoton), tropieza (`jn10`), a media vida (`jn11`), el rastro de fuego (`jn12`); cae (`jn13`, `jn14`),
  el cartel (`jn15`), la carta en el codice (`jn16`, `jn17`) y el bestiario (`jn18`); otro al amanecer dice su frase (`jn19`) y se
  deshace (`jn20`). `LIMIT` 119 min (`lanzar.sh` 125 y 123 min, el paso 153, el trabajo 201).
- **Arreglo (para todos los jefes): la vida maxima ya no se queda en 1024**, el tope del juego (`Enemigo.sinTopeDeVida`, al
  construir el mod en el servidor y en el cliente: sube el maximo de `RangedAttribute` de la vida, buscado por su valor, a
  `TOPE_VIDA`). Hasta la 7w los de mas de 1024 (el Abad, el Pontifice, Aldric, los gigantes —2200—) se quedaban en 1024 y con
  amigos casi ninguno ganaba la vida de mas (`paraGrupo`: con dos jugadores la Molinera ya pasaba de 1024).

### Plan grande, parte 7y: el Teatro Hundido y Pell, el Bufon del Rey (el jefe secreto tras el muro ilusorio)

Respuesta 132 del plan, la tercera tanda de los especiales ("jefes secretos detras de muros ilusorios"). Lore: Pell hacia reir al
ultimo rey de Morvain; la noche del cielo rojo estaba en escena, a mitad de "La corona de papel": todos corrieron, tapiaron la
puerta real con el detras y siguio actuando. Mil anos despues aun espera publico bajo el escenario.
- **El Teatro Hundido** (`morvain/Teatro`, en (3670, 712), al este de la Capital, a 430 de su Puerta Oeste; llanura a 96: la
  terraza es casi nada). u = x - CX, v = z - CZ (el norte, v negativa, es el escenario). Se ve de lejos solo el muro de la escena,
  que asoma seis bloques. Todo se hace **columna a columna** (`Obra`, la funcion `columna`: zonas que no se pisan) mas lo pequeno en
  una `Pilotes.Lista` y los cofres con `Trozo`:
  - la **grada** (cavea, v >= -1): diez filas (`fila(r)`: de `R_ORQ` 9,5 a `R_GRADA1` 27,5, 1,8 de hondo, de 87 a 96), cada una con
    su escalon delante (un asiento que mira al escenario: `unir`, para que se unan las esquinas) y la piedra detras; cinco escaleras
    (`escaleraGrada`, a -62, -26, 0, 26 y 62 grados del sur); el trozo hundido en escombros (38-53 grados, filas 3+); arriba el
    paseo (summa, hasta 31,5) con las columnas rotas del portico y el murete con la entrada al sur; el camino al santuario.
  - la **orquesta** (r < 9,5, suelo `ORQ` 86: anillos claros y oscuros y el centro dorado) y los **pasos** de los lados (|u| 10-31, v
    -7..-3: suben un bloque cada dos hasta la llanura, con sus muros; ojo: los rincones entre el circulo y los pasos van a la altura
    de la orquesta, si no salian columnas de tierra).
  - el **escenario** (|u| <= 20, v -14..-9, suelo `ESC` 88, de tablas quemadas; escalones desde la orquesta en u = ±4/5; las
    **candilejas**, velas encendidas en v = -10) y el **muro de la escena** (v -17..-15, de 88 a 101-103, roto arriba): columnas de
    dos pisos (u ±4, 12, 16, 20) con su cornisa, las dos **mascaras** (5 x 5 de calcita y piedra negra: la que rie al oeste, la que
    llora al este, sobre las puertas de los lados), la **corona del rey** sobre la puerta real, dos hornacinas con busto, colgaduras
    rojas; tres puertas: las de los lados (u ±8-9) dan a los **bastidores** (salas bajo la llanura: al este el atril con el **libro
    perdido 32**, al oeste el arcon `chests/teatro` y trastos; la puerta del oeste medio cegada de cascotes) y la del centro, la
    **puerta real, tapiada** (3 x 4 en la cara de fuera): el primer **muro ilusorio**.
  - detras, el **pasillo** (3 de ancho, baja por una escalera de v -20 a -25) a la **Sala de las Mascaras** (redonda, centro v
    `SALA_V` -44, radio 13, suelo `SALA` 82, techo 92 de tablas con vigas, bajo la llanura): suelo y paredes en **rombos de
    arlequin** (calcita y piedra negra; ladrillo rojo del Nether y piedra negra, con la cenefa dorada), mascaras por las paredes
    (calaveras blancas y negras), velas, cuatro lamparas y la del centro, el publico (calaveras en estacas mirando al tablado) y al
    norte el **tablado** (suelo 84) con el telon rojo y el **trono de Pell**; detras del trono el segundo **muro ilusorio** (3 x 3) y
    el **camerino** (madera, el tocador con su espejo, los trajes, la alfombra a cuadros rojos y amarillos y el arcon
    `chests/teatro_bueno`).
  Santuario nuevo "El Teatro Hundido" (`SantuariosMorvain`, id `teatro`, 48 al sur de la orquesta). Logro `teatro` (bajar al teatro,
  con su entrada del diario). El Espejo: "El Teatro Hundido" y, bajo la llanura, "La Sala de las Mascaras" (`Teatro.dentro`,
  `enSala`). Mientras la puerta real siga tapiada, a quien esta en el escenario le llega de vez en cuando detras del muro una risa
  ahogada o unos cascabeles (`Teatro.tick`): la pista. `Adorno` no pone nada en su caja.
- **Los muros ilusorios** (`morvain/MurosIlusorios`, de momento los dos del teatro: `Teatro.muro(id)` y `bloqueMuro`): parecen de
  verdad; **se deshacen al golpearlos** (el combate: `Combate.pedir` llama a `MurosIlusorios.golpe`, que mira 4,5 bloques delante de
  los ojos, como el muro falso de la fortaleza; en creativo, el clic izquierdo) en polvo de almas y ceniza con `sfx.muro_ilusorio`,
  el aviso "El muro no era mas que una ilusion" y el logro `secreto`; quedan abiertos para siempre (SavedData `darkcraft_muros`; si un
  chunk vuelve a tener sus bloques, se quitan al pasar alguien: `tick`). `cerrar` (pruebas y comando) los vuelve a poner.
- **Los de alli** (`MorvainEnemigos`, sitio `teatro`, 16): Pell (el puesto 0, grupo `JEFE`) y los calcinados de la parte 7m —la
  gente que ardio en la grada—: seis cenicientos enterrados en la ceniza por las filas, uno en la orquesta, la guardia a los lados
  de la puerta real, dos incendiarios en lo alto de la grada (tiran cantaros a la orquesta), uno en cada paso y uno en cada
  bastidor.
- **Pell** (`enemigos/Bufon`, entidad `darkcraft:bufon`, 0,7 x 2,2, hereda de `Guardian`; tipo y logro `bufon`): 1000 de vida, golpe
  11, armadura 4, rapido (prisa 1,25; a media vida 1,35); 16000 ecos; deja dos piedras III, tres II y la ceniza del paso. Espera
  **de pie** en medio de su sala dando saltitos (sin nadie, a ratos se rie solo); repara en quien entra en su sala (a 20, a la
  vista); al empezar dice su frase 1 y hace la **reverencia** (su "rugido") y no ataca hasta acabar la frase. El **tajo** doble (3,3,
  110 grados), la **pirueta** (dos vueltas con los brazos abiertos: 360 grados, frena), los **cuchillos** (tres, uno tras otro, rectos
  y deprisa: `ProyectilBufon`), los **malabares** (tres bolas de fuego en arco, a la izquierda, a su presa y a la derecha de por
  donde va: revientan en llamas —radio 1,7, prenden— y dejan fuego en el suelo; la primera vez su frase 4), el **humo** (estrella la
  bomba: ciega 2 s a quien este a 3,8; se esfuma —invisible— y sale de un salto por la espalda de su presa, `APARECE`) y la
  **voltereta** hacia atras (si se le pega seguido; mientras gira, 13 ticks, no le entra nada; luego cuchillos). **A media vida**
  (frase 2) **se parte en tres**: dos **ilusiones** (la misma entidad con `ILUSION` sincronizado: no empiezan peleas —
  `Guardian.empiezaPeleas`—, sin barra, no se guardan, pegan la mitad, duran 18 s y se deshacen de un golpe con su risa) y el de verdad
  cambia de sitio con una de ellas; **pegarle al de verdad deshace las que queden**; lo repite cada 16-22 s cuando no le queda
  ninguna. Las ilusiones brillan un poco y no tienen sombra (lo que las delata). Al caer: se sienta, se tumba, dice su frase 5,
  su ultima risa, **el teatro vacio aplaude** (`sfx.teatro_aplauso`) y se deshace en confeti; lo demas lo reparte
  `valdrenn/Guardianes` (el cartel, la hazana `bufon` "La ultima funcion" y el bestiario). Pruebas: `quieta`, `vencer()`,
  `mediaVida()` y contadores (`tajos`, `piruetas`, `cuchillos`, `bolas`, `malabares`, `humos` / `cegados` / `apariciones`,
  `volteretas` / `esquivados`, `partidas` / `deshechas`).
- **El modelo** (`tools/enemigos/bufon.py`, variante "pell", escala 1,1): mascara mitad blanca y mitad negra, gorro de tres puntas
  con cascabeles, gorguera, traje de rombos rojos y negros, zapatos de punta, una daga en cada mano y las tres bolas de los malabares
  (`malabar`, oculta salvo en los malabares: `BufonModelo` la ensena hasta la ultima bola). Bucles `quieto` (saltitos), `andar`, `listo`;
  acciones (`BufonAnims`): reverencia (52), tajo (22), pirueta (28), cuchillos (26), malabares (52), humo (18), aparece (14),
  voltereta (18), ilusion (32), risa (36), aturdido (28), morir (80). `BufonRenderer`: las ilusiones con el modelo translucido
  (`alfa` ~0,8) y sin sombra; invisible (en el humo), nada.
- **Lo que tira** (`enemigos/ProyectilBufon`, `darkcraft:proyectil_bufon`, una sola entidad: es cuchillo o bola segun su objeto,
  `cuchillo_bufon` / `bola_bufon`, que solo sirven para dibujarlos con `ThrownItemRenderer`; arte de `tools/rol/bufon_objetos.py`).
- **Voz** (ElevenLabs, `eleven_v4`, **"Creepy Liminal Entity"** `chQx45TGebwefgePtdqT`, flow `ZE8N58Gx76xf2dc2HiBY`; 5 frases, tomas
  a; en `procesar.py` `voz_bufon`: un poco mas aguda, una segunda voz mas aguda y retrasada, muy baja —sus ilusiones— y el eco corto
  de la sala de piedra): linea **150 + N** (`Voces.BASE_BUFON`, `esBufon`; `habla_bufon` en `rey_voces.json`; subtitulos
  `darkcraft.bufon.voz.1..5` con `darkcraft.bufon.nombre_corto`): 1 "¡Oh! ¡Publico! Nadie ha venido a verme en mil anos... ¿Te reiras
  con Pell?", 2 "¿Cual es Pell? ¿Cual es el de verdad? ¡Hasta yo lo olvide, hace mucho!", 3 "El Rey se reia con todos mis chistes...
  hasta la noche en que el cielo se puso rojo." (si cae su presa), 4 "¡Hop! ¡Atrapala!" (de relleno), 5 "Bravo... bravo... El ultimo
  acto... y nadie... aplaude."
- **Barra** (`GuardianCliente`, "bufon": confeti que cae meciendose, nombre dorado; arte de `tools/hud/barra_guardianes.py`: la banda
  de arlequin entre dos filetes de oro con un cascabel en cada punta, la mascara bajo el gorro de tres puntas y dos dagas cruzadas;
  relleno de rombos rojos que corren). **Musica**: "Ring Master" de Bobjt (OpenGameArt, CC0, id `bufon`, el fichero "ring master
  black build III"; tema 44 de `ClientMusic.boss`).
- Sonidos (ElevenLabs, flow `XMf8CEcsVPO7sIucWQt0`, `TOMAS_SFX` elegidas midiendo las tomas): `bufon_risa` (2 y 4), `bufon_cascabeles`
  (2 y 3; los pasos), `bufon_tajo` (2 y 1), `bufon_cuchillos` (4 y 3), `bufon_malabar` (2 y 1), `bufon_bola` (2 y 3), `bufon_humo` (2 y
  4), `bufon_ilusion` (4 y 2), `bufon_pirueta` (3 y 2), `bufon_dolor` (2 y 1), `bufon_muerte` (4 y 1), `muro_ilusorio` (1 y 4) y
  `teatro_aplauso` (3 y 4).
- **El libro perdido 32**, "El programa de la ultima funcion" (la compania del Teatro Real: el programa de "La corona de papel", los
  tres actos, y al dorso la nota de Pell: "la puerta no es de piedra. Nunca lo fue. Golpeala y entra"). `Perdidos.TOTAL` = 32: **la
  mascara de libros perdidos ya no cabe en un int** (bit n = libro n): va en un long (`Espejo.perdidos` lee lo de antes, un int,
  sin el signo; `Net.EspejoEstado` con `writeVarLong`; `MenuScreen`, `ClientCiTest`). `Net.V` = "21".
- Comandos `/darkcraft teatro [ir <sitio> | enemigos [volver | fuera | sacar] | muro <puerta | trono> [abrir | cerrar]]` y
  `/darkcraft bufon [ir | vencer | volver | partir]` (`ir` abre la puerta real y lleva a la entrada de la sala).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_teatro carpeta` (con `-Dabierto=true` los dos muros abiertos): escribe a
  que se llega a pie desde la entrada con todo cerrado (no al pasillo ni a la sala), con la puerta real abierta (a la sala y a Pell) y
  con los dos abiertos (al camerino), y si cada uno de los del teatro se pisa, se llega y no esta metido en un bloque. Pell:
  `python3 tools/enemigos/generar.py --solo bufon --vistas carpeta`.
- Pruebas: servidor, `teatro()` (lineas `Morvain, el teatro hundido: ...` —el atril, los arcones, el tapiado, el trono, a pie con
  todo cerrado / la puerta real abierta (con un golpe de un jugador mirandolo) / los dos abiertos, el santuario, los sitios— y
  `Morvain, los del teatro: ...`) y `bufon()` (`Morvain, Pell: ...` —le ve entrar, la reverencia, el primer golpe despues de la
  frase, las acciones acaban—, `... los golpes de Pell a una presa: ...` —tajo, pirueta, cuchillos, malabares, humo (se esfuma y sale
  a su espalda), voltereta (esquiva) y el humo que ciega—, `Pell a media vida: ...` —se parte en tres, una ilusion se deshace de un
  golpe y pegarle a el deshace la otra— y `Pell cae: ...`); deja los dos muros tapiados (la prueba del cliente juega en ese mundo).
  Cliente, tramo `teatro()` despues de los gigantes: el teatro desde el santuario (`cliente_mv466`), la grada (`mv467`), el escenario
  (`mv468`), las mascaras y el tapiado (`mv469`), el atril (`mv470`); en aventura un golpe al tapiado (`mv471`, `mv472`); el pasillo
  (`mv473`) y la sala con Pell (`mv474`); en supervivencia la reverencia con su barra (`mv475`); sus golpes de lado (`mv476` el tajo,
  `mv477` la pirueta, `mv478` los cuchillos, `mv479`-`mv480` los malabares, `mv481`-`mv482` el humo); los tres Pell (`mv483`) y una
  ilusion que se deshace (`mv484`); cae (`mv485`), el cartel (`mv486`) y el confeti (`mv487`); el camerino tras el muro del trono
  (`mv488`) y el bestiario (`mv489`). `LIMIT` 123 min (`lanzar.sh` 129 y 127 min, el paso 157, el trabajo 205).

### Plan grande, parte 7z: el Nido de Ceniza y Vorthak, el Dragon de Ceniza (el ultimo de los especiales)

Respuesta 132 del plan, la ultima tanda de los especiales ("dragon"). Lore: Vorthak guardo siglos los pasos de las Montanas Nevadas
para los reyes de Morvain, a cambio de una carreta de oro cada invierno; la noche del cielo rojo se volvio loco (algo le quemaba por
dentro), quemo la Atalaya del Cuervo con su guarnicion y desde entonces duerme sobre el oro de los que suben a matarlo.
- **El Nido de Ceniza** (`morvain/Nido`, en (-5040, -4200), una repisa cortada en la ladera; la montana sube al noroeste y el valle
  baja al este; u = x - CX, v = z - CZ): la **repisa** (radio `R_ARENA` 30, suelo `SUELO` 285 —se pisa en `PISO`—, se funde con la
  ladera hasta `R_MEZCLA` 46) de suelo quemado (piedra negra, basalto, carbon, toba, grava, tierra de almas y alguna brasa de magma),
  el corte de roca al noroeste y la nieve en el borde; cuatro **penascos** (`PENASCOS`: detras de ellos no llega el aliento), siete
  troncos quemados con ramas de valla y 64 restos (calaveras, montones de hueso y lanzas de barrotes); al fondo (noroeste, `TESORO_U/V`
  -13, -13) el **tesoro**: un monticulo de oro (radio 7,5, alto 2) con velas, calaveras, dos campanas y el arcon `chests/nido_tesoro`;
  al este, al borde, la **Atalaya del Cuervo** (`ATALAYA_U/V` 32, -4): torre redonda (radio 4,5, 14 de alto) quemada, con el lado
  oeste caido, el atril con el **libro perdido 33**, el arcon `chests/nido`, la escalera de mano al piso de arriba (de tablas, medio
  hundido) con la hoguera apagada; y al sur el camino que sube del santuario. Todo en una `Obra` (columna a columna) mas lo pequeno en
  una `Pilotes.Lista` y los cofres con `Trozo`; `Adorno` no pone nada en su caja. Santuario nuevo "Senda del Nido" (`SantuariosMorvain`,
  id `nido`, al pie del camino). Logro `nido` (subir a la repisa, con su entrada del diario). El Espejo: "El Nido de Ceniza" y "La
  Atalaya del Cuervo" (`Nido.dentro`, `enAtalaya`). `Nido.apartar` (lo que el dragon no pisa andando: penascos, troncos, la torre).
- **Los de alli** (`MorvainEnemigos`, sitio `nido`, 6): Vorthak (el puesto 0, grupo `JEFE`) y los calcinados de la guarnicion: dos
  cenicientos enterrados en el camino, la guardia a la puerta de la atalaya, uno agazapado dentro y un incendiario arriba.
- **Vorthak** (`enemigos/Dragon`, entidad `darkcraft:dragon`, 5,2 x 4,4 —la caja es el cuerpo: el cuello, la cola y las alas quedan
  fuera; `getBoundingBoxForCulling` ancho—, hereda de `Guardian`; tipo y logro `dragon`): 2400 de vida, golpe 15, armadura 10 (+3),
  22000 ecos; deja tres piedras III, tres II y la ceniza de las llamas. **Sin fisica** (`noPhysics`, como el Jinete: `Puestos.libre`
  ya no mira si choca quien no tiene fisica); todo lo mueve el (`mover`, antes de `super.aiStep`): en tierra los pies en lo que pisa
  (`pisable`: el primer bloque entero de debajo), anda a 0,17 y gira despacio (si tiene que girar mucho, gira antes de andar); en el
  aire va hacia un punto girando como mucho 4,5 grados por tick y nunca a menos de 6 del terreno (`sobreTerreno`, el mapa de alturas
  aqui y delante). Sin objetivos del juego: a quien ve lo busca el (`buscarPresa`, `ve`).
  - **Dormido** (postura `REZA`, sobre su oro): le despierta quien se le acerca a 24 y le ve (agachado, a 6,5), quien le pega o quien
    abre un cofre a 14 (el oro suena). Al despertar dice su frase 1 (no la repite en 4 min) mientras se levanta y mira; al acabarla,
    **ruge** (a 16: lentitud y debilidad) y empieza.
  - **En tierra**: el **mordisco** (la cabeza a 4-10 delante; nada bajo el pecho), el **zarpazo** (barre por delante y echa a su
    izquierda), el **coletazo** (a quien se le pone detras, a 13,5), el **aletazo** (todos los de delante, poco dano y los echa lejos),
    el **pisoton** (se alza y cae: la onda de 6 a 4,5 delante; quien salta se libra) y el **aliento** (`ALIENTO_DESDE`-`HASTA`: cono de
    17 de su derecha a su izquierda —`giroAliento`, `bocaAliento`—; quema cada 5 ticks y prende; lo que lo tapa —un penasco— lo para;
    a media vida deja el suelo ardiendo). Si en 5 s recibe el 7 % de su vida cae de lado aturdido (+30 %; cada 25 s como mucho). A los 20
    s en tierra (15 a media vida), o si su presa esta en alto o lejos, **despega** (deja el suelo en el tick 15: el viento aparta).
  - **En el aire** (maniobras, `maniobraActual`): sube y da vueltas (radio 26, 20 sobre la repisa) y ataca dos veces (tres a media
    vida): el **vuelo rasante** (`planRasante`: una linea que pasa por su presa y nunca viene de la montana; baja a ras de suelo y
    echa fuego: el fuego da en el suelo delante, `sueloRasante`, un disco de 3,2 que avanza y deja llamas), las **bolas de fuego** (se
    queda en el aire a 16 de su presa y escupe tres: `ProyectilDragon`, en arco; revientan en 3,2 y dejan el suelo ardiendo) o el
    **picado** (sube a 28, marca en el suelo un corro de fuego de 7 donde esta su presa, se lanza y al tocar el suelo la onda); despues
    **baja** a tierra cerca de su presa (la onda del aterrizaje). Si en el aire recibe el 3,5 % de su vida en 5 s, **cae** y queda
    aturdido. Sin presa vuelve volando a su tesoro y, pasado un rato, se vuelve a dormir.
  - **A media vida** (frase 2): ruge (si vuela, al cielo) y la **lluvia de ascuas**: se queda sobre el centro de la repisa y durante
    6,5 s caen ascuas (unas junto a los jugadores, otras por toda la repisa; una marca avisa 1,3 s antes; queman en 2,4 y dejan
    llamas; caen en lo mas alto de cada columna: bajo techo no llegan); despues baja a tierra.
  - Habla (frase 3 si cae su presa, 4 "Burn!" de vez en cuando al echar fuego) y al caer (si vuela, cae del cielo) se desploma, dice su
    frase 5, las brasas de sus grietas se apagan (`DragonRenderer`: la capa de brillo) y queda en ceniza; lo demas lo reparte
    `valdrenn/Guardianes` (el cartel, la hazana `dragon` "Ceniza a la ceniza" y el bestiario). Pruebas y comando: `quieta`, `vencer()`,
    `mediaVida()`, `despegar()`, `maniobra(que)`, `derribar()` y contadores (`estado()`).
- **El modelo** (`tools/enemigos/dragon.py`, escala 1,75: unos 21 bloques de largo y 16 de alas): escamas de carbon con grietas de
  brasa (en la capa de brillo, con los ojos, los ollares, la boca y los cantos de la hoja de la cola), tripa de placas, cuello en tres
  trozos, cabeza con cejas de hueso y cuatro cuernos, alas de murcielago en tres trozos (`ala`/`alas` con `orientar`: hacia donde va
  cada hueso y donde cae su membrana; la izquierda con sus membranas pintadas en espejo), cola en seis trozos con la punta en hoja.
  Bucles `quieto`, `listo`, `andar` (al ritmo de lo que anda: `DragonModelo.CICLO_ANDAR` 5 bloques), `dormido`, `vuelo` (aleteo) y
  `planeo`; acciones (`DragonAnims`): despierta (64), rugido (52), mordisco (28), zarpazo (26), coletazo (36), aletazo (30), pisoton
  (40), aliento (76), despega (40), aterriza (32), rasante (60, solo el cuello: va sobre el vuelo), escupe (24, igual), picado (48),
  ascuas (60), aturdido (60), morir (100). `DragonRenderer`: volando se inclina con lo que sube o baja y se ladea en los giros
  (`cabeceo`, `alabeo`, alrededor del centro del cuerpo).
- **En el cliente** (`client/enemigos/DragonCliente`, sin paquetes): el fuego del aliento, del rasante y de escupir (llamas desde la
  boca, lo que da en el suelo), el aleteo en bucle mientras bate las alas y su respiracion dormido (pegados a el), el humo de los
  ollares dormido y las brasas que se le escapan, y los temblores de camara (pasos, pisoton, aterrizar, despegar, rugir, caer).
- **La bola de fuego** (`ProyectilDragon`, `darkcraft:proyectil_dragon`, su objeto `bola_dragon` dibujado grande y a plena luz:
  `ThrownItemRenderer` a 3,2; arte de `tools/rol/dragon_objetos.py`).
- **Voz** (ElevenLabs, `eleven_v4`, **"Malvoryx The Monster - Villain. Demon. Dragon."** `ysswSXp8U9dFpzPJqFje`, flow
  `yj2HeJUSKF4WFJfanGJL`; 5 frases, tomas a; en `procesar.py` `voz_dragon`: mas grave, una segunda voz aun mas grave y retrasada —el
  pecho—, el chisporroteo de las brasas, distorsion suave y el eco de la montana): linea **160 + N** (`Voces.BASE_DRAGON`, `esDragon`;
  `habla_dragon` en `rey_voces.json`; subtitulos `darkcraft.dragon.voz.1..5` con `darkcraft.dragon.nombre_corto`): 1 "Otra chispita
  de la Sombra... subiendo a mi montana. He quemado reyes por menos.", 2 "¿Te atreves a herirme? ¡Yo ya era viejo cuando tu Sombra
  era un susurro en la oscuridad!", 3 "Ceniza... como todos los demas." (de relleno), 4 "¡Arde!" (de relleno), 5 "El cielo... el
  cielo era mio... antes de volverse rojo...".
- **Barra** (`GuardianCliente`, "dragon": ascuas que suben, nombre de oro viejo; arte de `tools/hud/barra_guardianes.py`: su lomo de
  escamas con grietas de brasa y las puas, la hoja de la cola a la izquierda, una garra a la derecha y su cabeza de frente con los
  ojos de oro; relleno de brasas entre escamas). **Musica**: "The Fire Dragon" de Umplix (OpenGameArt, CC0, id `dragon`, el bucle
  "the_fire_dragon_loopv2"; tema 45 de `ClientMusic.boss`).
- Sonidos (ElevenLabs, flow `LTdZckFRfg5JWcBJqnkH`, `TOMAS_SFX` elegidas midiendo las tomas): `dragon_rugido` (2 y 1), `dragon_aleteo`
  (1, en bucle), `dragon_fuego` (3 y 2), `dragon_mordisco` (3 y 1), `dragon_cola` (1 y 3), `dragon_pisoton` (3 y 4; tambien sus
  pasos), `dragon_aterriza` (1 y 3), `dragon_despega` (2 y 4), `dragon_picado` (1 y 3), `dragon_dolor` (3 y 4), `dragon_muerte` (2),
  `dragon_respira` (2, en bucle) y `dragon_ascuas` (2 y 1).
- **El libro perdido 33**, "El cuaderno del vigia" (Odo, el vigia de la atalaya: treinta anos con Vorthak, la noche del cielo rojo,
  y que detras de las rocas no llega su fuego y cuando le pesan las alas baja a tierra). `Perdidos.TOTAL` = 33.
- El bestiario dibuja al dragon por lo que mide de verdad (`Coleccion.tamano`, 22 bloques), no por su caja.
- Comandos `/darkcraft nido [ir <sitio> | enemigos [volver | fuera | sacar]]` y `/darkcraft dragon [ir | vencer | volver | media |
  despegar | rasante | bolas | picado | ascuas | bajar | derribar]`.
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_nido carpeta` (a que se llega a pie desde el santuario, cada uno de los del
  nido se pisa y se llega, y que la repisa queda despejada por encima); el dragon: `python3 tools/enemigos/generar.py --solo dragon
  --vistas carpeta`.
- Pruebas: servidor, `nido()` (`Morvain, el nido de ceniza: ...` —el atril con el libro 33, los arcones, el oro, la repisa llana y
  despejada, a pie desde el santuario a todos los sitios, el santuario— y `Morvain, los del nido: ...`) y `dragon()` (`Morvain,
  Vorthak: ...` —dormido sobre su oro, a quien ve dormido, despierta y ruge al acabar la frase, las acciones de tierra acaban—, `... los
  golpes de Vorthak en tierra: ...` —mordisco, zarpazo, coletazo, aletazo, pisoton (y saltando se libra), aliento (y tras un muro no
  quema)—, `Vorthak en el aire: ...` —despega, rasante, bolas, picado, media vida y las ascuas, derribado— y `Vorthak cae: ...`).
  Cliente, tramo `nido()` despues del teatro: el nido desde el camino (`cliente_mv490`), la repisa desde lo alto (`mv491`), la atalaya
  (`mv492`), Vorthak dormido (`mv493`); en supervivencia despierta (`mv494`) y ruge (`mv495`); sus golpes de lado (`mv496` mordisco,
  `mv497` zarpazo, `mv498`-`mv499` aliento, `mv500` coletazo, `mv501`-`mv502` pisoton, `mv503` aletazo); en el aire (capturas que
  esperan a que pase: `esperarDragon` / `dragonTick`) despega (`mv504`), vuela (`mv505`), rasante (`mv506`), bolas (`mv507`), la marca
  del picado (`mv508`), el picado (`mv509`), aterriza (`mv510`), las ascuas (`mv511`-`mv512`); cae del cielo (`mv513`), muerto
  (`mv514`), el cartel (`mv515`), en ceniza (`mv516`) y el bestiario (`mv517`). `LIMIT` 128 min (`lanzar.sh` 134 y 132 min, el paso
  162, el trabajo 210).

### Plan grande, parte 7aa: la subida por la cadena del sur (las gargolas, el viento, el Descansillo y la Boca)

Respuestas 88, 122 y 125 del plan (el castillo encadenado en el cielo; el marcado que la Sombra trajo de vuelta para que le abra
la puerta; la Sombra en el centro del mapa como jefe final), primera de las tres partes del final: **7aa** la
subida, **7ab** la ciudadela por dentro (la guardia, un mini-jefe y el ultimo santuario), **7ac** la Sombra (la pelea, los finales, el
cielo rojo que se apaga con `CieloRojo.apagar`, el epilogo de Maelis y los creditos). Se sube por la cadena del sur
(`Ciudadela.CADENAS[2]`), desde lo alto del ancla del Cerco (donde rezaba Aldric: hay que pasar por el) hasta la isla.
- **El camino** (`morvain/Subida`, singleton; lo escribe `MorvainGenerador` despues del Nido, chunk a chunk; lo ve Distant Horizons):
  cuatro de ancho (x `X0` -2 a `X1` 1; la cadena va por x = 0), `OFF` 12,6 por encima del eje de la cadena (un bloque mas que lo alto
  de los eslabones de pie) y sube como ella (`PENDIENTE` 0,4756: medias losas y escalones de roble oscuro y algo de abeto); sobre los
  eslabones tumbados es un puente con travesanos, caballetes y barandilla de cuerda (cadenas entre postes, faroles). Donde el camino
  daria con el hierro de la cadena (`hierro(x,y,z)`) no se pone nada (`hierroPisado` lo cuenta). Al pie, peldanos tallados en el lazo
  del ultimo eslabon (`Z_PELDANOS` 856; la caja de Aldric queda libre) y una escalera hasta lo alto de la cadena. **Rotos**: siete
  **huecos** (`HUECOS`, dos tablones de todo el ancho: se saltan), cinco trozos **estrechos** (`ESTRECHOS`, solo x -1 y 0, sin
  barandilla) y barandillas caidas. El perfil (`perfil`: lo que se pisa en cada z en medios bloques, `pisa(z)`, `superficie(z)`,
  `hayCamino(z)`) va de 157 en el ancla a 541 en la torre.
- **Los rellanos** (`RELLANOS` {z, medio largo, medio ancho, tipo}: 764, 652, la tarima rota 568, **el Descansillo** 484, 372, 260 y
  el de la Boca 150): tarimas llanas (`alturaDeRellano`: a su lado mas alto y por encima del hierro de debajo) con su escalera por el
  lado de abajo, barandilla, barriles, velas, calaveras, faroles y **pedestales** de piedra negra (`pedestal(k, n)`: lo alto, donde
  tiene los pies la gargola; cuatro en el de la Boca). El poste de la cadena va a `zl + 3` (antes los de alli quedaban dentro).
- **El Descansillo** (a media cadena): la tarima grande con el **santuario de la Mitad de la Cadena** (`SANT_X` -5, `SANT_Z`; corro de
  losas y dos farolas de almas; en `SantuariosMorvain.CADENA`, id `cadena`: en `todos()` pero NO en `SITIOS`, que no lleva tarima; su
  suelo `Subida.santuarioY()` sale sin levantar la subida entera), la choza de tablones del campamento de los que subieron con Aldric
  (petates, mesa con velas, el atril con el **libro perdido 34** y el arcon `chests/cadena`).
- **La Boca** (`Z_BOCA` 101: la cara de la isla): mandibulas de roca con dientes (estalactitas de piedra puntiaguda arriba y abajo,
  `BOCA_SALE` 5 hacia fuera, `BOCA_W` 7) y dos ojos rojos encima; dentro, **la Garganta** (`Z_GARGANTA` 96; 7 de ancho, `GARGANTA_ALTO`
  5, nunca por encima de `TECHO_ROCA` = TOP - 2: lo de encima de la isla no se toca) de carne (verrugas, nilio carmesi, setas que
  alumbran, roca negra, magma); de `Z_SUBE` 89 a `Z_TORRE` 85 peldanos de piedra negra que suben a la planta baja de la torre del sur de
  la muralla (`Y_TORRE` 541, el hueco solo dentro de la torre) y su puerta del norte (`Z_PUERTA` 77) da al **patio** de la ciudadela.
- Sitios con nombre (`sitio(n)`, `sitios()`): ancla, inicio, rellano1, rellano2, tarima, descansillo, rellano4, rellano5, rellano_boca,
  santuario, atril, arcon, choza, puente, hueco, estrecho, alto_cadena, medio, arriba, boca, garganta, torre, puerta, patio y, para mirar
  de lejos, lado y lado_boca. Donde se esta: `enCadena`, `sobreCadena` (y no abajo en el Cerco: `y > Y_ANCLA - 2`), `enCamino`,
  `enDescansillo`, `enBoca` (y la Garganta), `enCiudadela`, `suelo(z)`. El Espejo: "La Cadena del Sur", "El Descansillo", "La Boca", "La
  Ciudadela Encadenada" (`Morvain.lugar`, antes que el Cerco).
- **Lo que pasa en la cadena** (`morvain/Cadena`, desde `Morvain.onTick`, cada tick a cada jugador cerca, `cerca(x, z)`):
  - **La Sombra le habla** una vez a cada jugador (marca `sombra_cadena` de `Ficha.dialogo`: bits 1, 2, 4): al pisar el camino su
    frase **21** ("Si... sube, marcado. Cada eslabon te acerca a mi."), al llegar al Descansillo la **22** ("El primero tambien descanso
    aqui... y luego echo a correr. ¿Correras tu?") y al entrar en la Boca la **23** ("No hay puerta, marcado... solo mi boca. Entra.").
    `Net.SombraDice` → `client/morvain/ClientCadena.sombra`: suena sin sitio (`SOMBRA_VOZ[21..23]`, que ya tiene 24 huecos: las 10-20
    son del capitulo 6 y no se registran) y su subtitulo lila abajo (`darkcraft.sombra.voz.N`, `Voces.sombra(n)` + 0,9 s); si ya habla,
    la siguiente espera.
  - **Las rafagas**: por el camino (no en el Descansillo ni en la Boca, ni volando) cada 22-40 s (`RAFAGA_MIN/MAX`; la primera a los
    10-20 s) una de lado (`Net.Rafaga`: lado, `AVISO` 30 ticks en que se oye venir —`sfx.cadena_rafaga` desde ese lado— y se ven jirones
    de nube cruzando, y `EMPUJA` 30 ticks en que empuja; `FUERZA` 0,05 a la velocidad cada tick en lo mas fuerte, crece y se apaga: de pie
    en el suelo, unos dos bloques; con 0,075, en la build 185, echaba de la cadena desde el centro del camino). La
    lleva el cliente (`ClientCadena.viento`, en el START de cada tick); **agachado, una quinta parte** (y con el borde del juego no se
    cae); saltando, en el aire, mucho mas. `calma(sp, ticks)` y `rafagaYa(sp)` para las pruebas.
  - **Caer**: quien cae de la cadena (en el aire, habiendo pisado el camino en los ultimos 20 s) y pasa `CAIDA` 28 bloques por debajo
    del camino muere (dano de caer del mundo: la espada de luz va al ultimo suelo firme). Con el fruto de la pluma, no (cae despacio).
  - **Sin corcel**: `Montura.llamar` no lo llama sobre la cadena ni en la Boca (aviso `darkcraft.corcel.cadena`) y si se entra montado
    se deshace.
  - Logros `cadena` (pisar el camino), `descansillo`, `boca` y `ciudadela` (llegar al patio; meta), con su entrada del diario.
- **Los de la cadena** (`MorvainEnemigos`, sitio `cadena`, 30, de `Subida.esperas()`): en los rellanos los marcados que subieron y se
  quedaron (rompedores de rodillas golpeando la cadena, caballeros de guardia, cazadores que tiran escaleras abajo; tipo `marcado`) y
  las **gargolas** (tipo `gargola`): de piedra en los pedestales (de rodillas, `REZA`) y en ronda por el aire sobre el camino (de pie,
  ronda 6, 5-7 por encima del camino); cuatro en los pedestales del rellano de la Boca.
- **La gargola** (`enemigos/Gargola`, entidad `darkcraft:gargola`, 1 x 1,9, la posicion se manda cada tick; tipo, bestiario y logro
  `gargola`): 56 de vida, golpe 7, armadura 10. **De piedra** (`REZA`, sin fisica: no se cae ni se empuja, `postura(int)` lo pone) ve a
  14 (agachado 7) con linea de vista; al despertar (`DESPIERTA`, 44 ticks: se agrieta —particulas de piedra—, `sfx.gargola_despierta`,
  grita) alza el vuelo. **En el aire** sin fisica (`vuela()`, sincronizado; `travel` no hace nada en el servidor y `knockback` suma a lo
  que vuela): da vueltas alrededor de su presa (`R_ORBITA` 6,5, `ALTO_ORBITA` 4,5 por encima, `GIRO_ORBITA` 0,035 rad/tick, a
  `V_VUELO` 0,42; si da con algo, sube) y cada `ESPERA_AIRE` (70 + algo) ataca: el **picado** (55 %: se para, chilla y en el tick
  `PICADO_DESDE` 12 se lanza en linea recta a `V_PICADO` 1,05 contra el pecho de su presa —con lo que anda—; a quien pille, x1,1 del
  golpe y un empujon de lado `EMPUJE_PICADO` 1,5 + 0,32 hacia arriba: fuera de la cadena; **agachado, el empujon es 0,3 y sin
  levantarle**; nunca baja de la altura de su presa; si da con algo, se acaba), **posarse** (25 %, si hay suelo firme a 2,5-3,5 de su
  presa: `ATERRIZA`) o las **garras** (se cierne delante de su presa a dos bloques y a la altura de su cabeza y lanza las patas).
  Aturdida en el aire baja un poco. La que vuela grita al ver a alguien (`GRITO`). **En tierra** (con fisica, anda): el **zarpazo**
  (dos garrazos) y el **coletazo** (vuelta entera; a quien se le pone detras); a las dos o tres, si su presa se aleja (8,5) o sube o baja
  mucho (2,5), **despega** (deja el suelo en `DESPEGA_SALTO` 9); si la echan del camino (sin suelo), alza el vuelo. Sin presa las de
  piedra vuelven volando a su pedestal y al rato vuelven a ser estatua; las de ronda dan vueltas a su puesto. **Al morir** se le parte la
  piedra: si volaba cae (con fisica) y se va. `Enemigo.anda()` (nuevo): los que se mueven solos en el aire no vuelven, rondan ni
  persiguen con el camino del juego. Para las pruebas: `quieta`, `probarVuelo`, `probarPicado`, `probarGarras`, `probarPosarse`,
  `estado()` y contadores.
- **El modelo** (`tools/enemigos/gargola.py`, escala 1,05: piedra de granito con grietas y liquen, cabeza de perro con cuernos,
  brazos largos con tres garras, piernas de garra, puas por el espinazo, alas de murcielago en tres trozos —las cuentas de las del
  dragon girandolas al torso— y cola con la punta en pala; ojos y grietas rojas en la capa de brillo): bucles `posada` (la estatua),
  `quieto`, `listo`, `andar`, `vuelo` (tendida) y `cerne` (en el sitio); acciones (`GargolaAnims`) despierta (44), grito (28), zarpazo
  (26), coletazo (24), despega (18), aterriza (16), picado (32), garras (20), aturdido (30), morir (40). `GargolaModelo`: en el aire
  mezcla `vuelo` y `cerne` segun lo que avanza (el paso del juego que calcula el cliente). `GargolaRenderer`: **de piedra, sin brillo**;
  al despertar se le encienden los ojos poco a poco y al morir se apagan.
- **En el cliente** (`ClientCadena`): las rafagas, la Sombra, los eslabones que crujen bajo los pies de vez en cuando
  (`sfx.cadena_crujido`, en el eje de la cadena) y, cerca de la Boca, su aliento y su latido. **El aire de la cadena**
  (`ambiente.cadena`, en bucle) lo pone `ClientMusic.tickCrater` sobre la cadena y en la Boca (en lugar del del crater).
- Sonidos (ElevenLabs, flow `T96XJh7q5bh21jnYDQgI`, `TOMAS_SFX` elegidas midiendo las tomas): `gargola_despierta` (3 y 4),
  `gargola_grito` (1 y 3), `gargola_aleteo` (1 y 2; cada 2,5 s mientras vuela), `gargola_picado` (1 y 4), `gargola_garras` (3 y 4),
  `gargola_cola` (4 y 2), `gargola_aterriza` (1 y 4), `gargola_dolor` (3 y 1), `gargola_muerte` (1 y 3), `cadena_rafaga` (3 y 1),
  `cadena_crujido` (2 y 3), `cadena_ambiente` (2 y 4, bucle), `boca_aliento` (3 y 2) y `boca_latido` (4 y 3, con los graves de mas).
  La Sombra (voz Grav, flow `Mm0FdcLcpVa1l39aUIC2`): `TOMAS_SOMBRA` / `SUSURROS_SOMBRA` 21 a/a, 22 b/b, 23 a/a.
- **El libro perdido 34**, "El diario del Descansillo" (Ivo, el ultimo de la cuerda: el campamento, el viento que se llevo a Mattis,
  Aldric que subio solo a la Boca y bajo corriendo hasta el ancla, y "descansa junto al fuego; luego decide tu si corres").
  `Perdidos.TOTAL` = 34. Botin `chests/cadena` (frutos de la pluma, piedras de forja II y alguna III, y a veces un talisman o una ceniza).
- Comandos `/darkcraft cadena [ir <sitio> | enemigos [volver | fuera | sacar] | rafaga | sombra olvidar | gargola [vuelo | tierra |
  picado | garras | posarse]]` (sin mas: como esta; `gargola` = la mas cercana a 48 bloques).
- **Los planos que se hacen al construir el mod** (`Subida`, `Cerco` y, desde la 7ab, `Homenaje`: `MorvainEnemigos.preparar` apunta sus
  puestos al arrancar el mod) se vuelven a hacer en `Morvain.onArranque` (`ServerAboutToStartEvent`, `rehacer()`): hechos antes de que
  esten registrados los bloques del mod, el santuario de la cadena salia como piedra iman, las gemas como redstone y la piedra de sombra
  y el hierro negro del Cerco como obsidiana y netherita (los sustitutos de las vistas sin juego).
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_subida carpeta` (`Volcado.andarSubida`: a pie desde lo alto del ancla a todos
  los sitios —los huecos se ven con tablon—, cada uno de los de la cadena se pisa y se llega y ninguno metido en un bloque; el perfil);
  la gargola: `python3 tools/enemigos/generar.py --solo gargola --vistas carpeta`; un trozo en texto: `herr/corte.py`.
- Pruebas: servidor, `cadena()` (`Morvain, la cadena del sur: ...` —el resumen, a pie a todos los sitios, el santuario, donde se esta y
  el corcel que no viene— y `Morvain, los de la cadena: ...`) y `gargolas()` (`Morvain, la gargola: ...` —de piedra, lo que ve, despierta
  y vuela, sus vueltas, el picado que pilla y echa, las garras, se posa, el zarpazo y el coletazo, despega y muere en el aire—).
  Cliente, tramo `cadena()` despues del nido: el camino desde el ancla (`cliente_mv520`), la cadena de lado (`mv521`), la gargola de
  piedra (`mv522`), el Descansillo (`mv523`) y su atril (`mv524`), la Boca (`mv525`), la Garganta (`mv526`), el patio (`mv527`); en
  supervivencia la Sombra (`mv528`, con su subtitulo), una rafaga (`mv529`; lineas de lo que mueve de pie y agachado), el corcel que no
  viene, la gargola (capturas que esperan: `esperarA` / `esperaTick`): despierta (`mv530`), vuela (`mv531`), el picado (`mv532`), las
  garras (`mv533`), posada (`mv534`), cae (`mv535`), el bestiario (`mv536`) y caer de la cadena (`mv537`).

### Plan grande, parte 7ab: la ciudadela por dentro (la casa de la Sombra, los centinelas, la Ultima Llama y la reina Isolde)

Segunda de las tres partes del final (respuestas 88, 122 y 125 del plan). Lore: cuando el rey Corvin suplico a la Sombra por su reina
muerta, lo que volvio tenia su cara, su voz y su corona, pero no era ella; espera en el trono de la ciudadela. La reina de verdad, en
los ratos en que era ella, escribio un diario (el libro perdido 35).
- **La torre del homenaje por dentro** (`morvain/Homenaje`, singleton; lo escribe `MorvainGenerador` despues de la subida, chunk a
  chunk, sobre lo que `Ciudadela` dejo hueco; coordenadas del mundo, la ciudadela esta en 0,0): la **puerta** en la cara del sur (x
  `PX0` -3 a `PX1` 2, arco apuntado con su portico, la gema encima) con el **rastrillo** de barrotes (`rastrillo()`, en `PZ_MURO` 25:
  cerrado al generarse); en el patio, el camino desde la torre del sur, braseros y cuatro estatuas. Seis plantas (`SUELO`: TOP, +15,
  +29, +43, +57, +71; `pisa(k)` = lo que se pisa): 0 el **Salon de la Guardia** (pilares, la alfombra, armaduras), 1 la **Armeria**, 2
  el **Archivo** (estanterias, mesas y el atril con el **libro perdido 35**), 3 la **Prision** (celdas, jaulas), 4 la **Capilla Negra**
  (bancos, el altar y la **Ultima Llama** en el centro, `SANT_X` -1, `SANT_Z` 1; Maelis en `GUIA_X` 2, `GUIA_Z` 3) y 5 el **Salon del
  Trono** (el estrado al sur con el trono —`TRONO_X` 0, `TRONO_Z` 17: el asiento es aire, la reina flota sentada— la cortina roja,
  braseros, espejos rotos y lamparas). Las **escaleras** van pegadas a los muros, cada una al lado contrario de la anterior (`lado(k)`:
  las de las plantas 0, 2 y 4 por el norte subiendo al oeste; las de 1 y 3 por el sur subiendo al este; `peldano`, `filas`, `enTramo`,
  `enHueco`); de la sala del trono a la **azotea**, la del muro del este tras una **reja** (`rejaAzotea()`) que no se abre hasta que cae
  la reina; en la azotea, la puerta de la aguja cegada (la Sombra: parte 7ac). Sitios (`sitio(n)`): patio, camino, puerta, vestibulo,
  salon, armeria, archivo, atril, prision, celda, capilla, santuario, altar, trono_entrada, sala_trono, trono, tesoro, reja_azotea,
  azotea, puerta_sombra, frente (y los arcones y escaleras). Donde se esta: `enHomenaje`, `planta(y)`, `enSalaTrono`, `enCapilla`,
  `enAzotea`; el Espejo dice cada sala (`sala(k)`: `darkcraft.morvain.lugar.homenaje_*`, en `Morvain.lugar` antes que la ciudadela).
  Arcones `chests/homenaje` (armeria y archivo) y `chests/homenaje_tesoro` (junto al trono).
- **La casa de la Sombra** (`morvain/CasaSombra`, desde `Morvain.onTick`): el **rastrillo** sube solo la primera vez que alguien llega
  ante la puerta (`LLEGA` 10; una fila de barrotes cada cuatro ticks, `sfx.puerta_homenaje`; se apunta en los datos de Morvain:
  `homenaje_rastrillo`); al entrar, **la Sombra** dice su frase **24** ("Bienvenido a mi casa, marcado. Cuidado con mi guardia... nunca
  aprendieron a morir": bit 8 de la marca `sombra_cadena`, por `Cadena.decir`) y el logro `homenaje`; junto a la Ultima Llama
  encendida, el logro `ultima_llama`; **Maelis** se sienta una vez en la capilla (`homenaje_guia`); el **sello del salon del trono**
  mientras la reina pelea (barreras sobre lo alto de la escalera que sube de la capilla, `sello()`: x `SX0` -6 a `SX1` -1, z -23 a
  -20, tres de alto; quien este en medio pasa al salon; cortina de chispas; un sello que se quedo puesto al cerrar el servidor se quita);
  la **reja de la azotea**, abierta con el puesto de la reina caido (y cerrada si vuelve: `revisar`). Avisos `darkcraft.homenaje.sello`,
  `.reja` (y `.aguja`, que desde la 7ac ya no sale). `resumen`, `cerrarRastrillo`, `abrirRastrilloYa`, `olvidar` para las pruebas y los comandos.
- **La Ultima Llama** (`SantuariosMorvain.ULTIMA`, id `ultima_llama`: en `todos()`, sin tarima; la escribe `Homenaje`).
- **Maelis en la capilla** (`Dialogos.GUIA` 15 → 16 → menu; `Conversaciones.inicioGuia(sp, guia)`: con la guia en la capilla, la
  primera vez 15 —bit 16, `GUIA_CAPILLA`—, despues 16, y con la reina ya caida 14). Voz "Autumn Veil", frases 15 y 16 (tomas a).
- **Los de la casa** (`MorvainEnemigos`, sitio `homenaje`, 30, de `Homenaje.esperas()`, el tipo de cada uno lo dice el plano): la
  reina (puesto 0, `Puestos.JEFE`), centinelas de pie y quietos (`REZA`) o tirados (`TUMBADO`) o de ronda en el patio, el salon, la
  armeria, el archivo y la prision; espectros en el archivo; marcados de rodillas en las celdas y perros dormidos en la prision.
- **El centinela** (`enemigos/Centinela`, entidad `darkcraft:centinela`, 0,9 x 2,5; bestiario `centinela_espadon` / `centinela_alabarda`,
  logro `centinela`): una armadura vacia con humo dentro. 72 de vida, golpe 8, armadura 12. Quieto (`REZA`) despierta a 6 bloques (3
  agachado); tirado se levanta juntando las piezas. Espadon (variante 0): **tajo** (de lado a lado) y **golpe** (de arriba abajo, con
  onda: saltando o rodando se libra); alabarda (1): **estocada** (llega a 5,3), **barrido** (vuelta entera; a quien se le pone al lado o
  detras) y **hachazo**. Los dos, el **humo** (de 4,5 a 10 bloques; se deshace —el dibujante no lo dibuja—, cruza y sale con una
  estocada). Al morir se desploma y se le escapa el humo. Modelo `tools/enemigos/centinela.py` (escala 1,3; brasas en el yelmo y la marca
  en la capa de brillo, apagadas mientras duerme). Comando `/darkcraft homenaje centinela [despertar | tajo | golpe | humo | estocada |
  barrido | hachazo]` (el mas cercano).
- **La reina Isolde** (`enemigos/Isolde`, `Guardian`, entidad `darkcraft:isolde`, 0,9 x 2,9; 1800 de vida, golpe 13, armadura 6;
  barra propia —`tools/hud/barra_guardianes.py`: el velo de encaje con rosas negras, la cara velada y la corona; seda carmesi; petalos y
  lagrimas que caen—, musica "Dark souls type boss theme" de ProjectHelmet, CC-BY 4.0, id `isolde`, tema 46; voz ElevenLabs "Elariel X
  - Epic Ethereal Queen" `ksryVoNAGZT8GxWCTiVm`, flow `dAtA9WXZ451rqHwQNb2f`, `voz_isolde` en `procesar.py`: una segunda voz grave y
  baja por debajo y el eco alto del salon; lineas `Voces.BASE_ISOLDE` 170 + N; logro `isolde`, hazana). Espera **sentada tarareando**
  (`client/morvain/ClientHomenaje`: el tarareo sigue a la reina y se calla al levantarse); ve a quien entra en el salon (a la vista, a
  44). Al empezar: frase 1 mientras se levanta y grita al acabarla (no repite la frase en 4 min). **Garras** (dos cortes), **velo**
  (vuelta entera), **abrazo** (de 4,5 a 12: cruza del tick 10 al 16 y a quien pilla lo coge: **aprieta** —le arrastra delante, le chupa
  la vida en los ticks 8 y 16 y se cura, y le suelta de un empujon en el 26—), **nana** (frase 5 la primera vez; a quien la este
  mirando a la cara —`laMira`: 44 grados, sin nada en medio— la mitad del canto, al acabar se le cierran los ojos: `dormir`, ceguera,
  lentitud, debilidad y cansancio 3,5 s, aviso `darkcraft.isolde.dormido`) y **desvanecerse** (del tick 6 al 13 no se le puede pegar;
  en el 12 aparece detras de su presa —segun hacia donde mira— y le echa las garras). **A media vida le arde el velo** (frase 3; la
  variante 1 desde el tick 26: se le ve la cara): el velo quema, **lagrimas** (marcas bajo cada uno —y por donde va— y a los 28 ticks
  la lluvia negra: dano y marchitamiento) y **su corte** (frase 2 la primera vez; 2 espectros efimeros, uno mas por jugador, hasta 4).
  Si cae su presa, la frase 4. Al caer, frase 6 de rodillas en el aire y se va al acabarla subiendo y apagandose (`fin()`); deja tres
  piedras de forja III, dos II y la ceniza de la sangre. Modelo `tools/enemigos/isolde.py` (escala 1,5; sentada, quieto, listo, andar
  —se desliza—; translucida: `IsoldeRenderer.alfa`). Para las pruebas: `quieta`, `probar`, `mediaVida`, `vencer`, `estado` y contadores.
- Sonidos (ElevenLabs, flow `08HSxlBxAnMWii5NxHHr`, `TOMAS_SFX` elegidas midiendo las tomas): `centinela_alerta` (4, 2), `_paso` (1, 2,
  4), `_tajo` (4, 1), `_golpe` (2, 4), `_alabarda` (3, 1), `_humo` (2, 1), `_dolor` (3, 4), `_muerte` (3, 2); `isolde_tarareo` (4, 1;
  bucle), `_canto` (1, 3), `_garras` (4, 2), `_velo` (2, 1), `_abrazo` (2, 4), `_desvanece` (2, 3), `_grito` (3, 4), `_velo_arde` (1,
  4), `_lagrimas` (1, 2), `_dolor` (1, 3), `_muerte` (3, 1); `homenaje_ambiente` (1, 3; el aire de la torre, `ambiente.homenaje`, lo
  pone `ClientMusic.tickCrater` dentro de la torre) y `puerta_homenaje` (3, 1). La Sombra 24: voz y susurro a/a.
- **El libro perdido 35**, "El diario de la reina Isolde" (la fuente, Corvin que lloraba, la guardia que nunca aprendio a morir, "cuando
  cante, no la mires", "nunca fui yo la que volvio"). `Perdidos.TOTAL` = 35.
- Comandos `/darkcraft homenaje [ir <sitio> | enemigos [volver | fuera | sacar] | rastrillo [abrir | cerrar] | sombra olvidar |
  centinela ...]` y `/darkcraft isolde [ir | vencer | volver | media | garras | velo | abrazo | nana | desvanece | lagrimas | corte]`.
- **Sin abrir el juego**: `tools/vista3d/vista.sh morvain_homenaje carpeta` (`Volcado.andarHomenaje`: a pie desde el patio cerrado, con
  el rastrillo abierto y con todo abierto; cada uno de la casa se pisa, se llega y no esta metido en un bloque); los modelos:
  `python3 tools/enemigos/generar.py --solo centinela|isolde --vistas carpeta`.
- **Las capturas que esperan** (`ClientCiTest.esperarA`): llamadas mientras se apuntan los pasos (`planificando`) se ponen en cola al
  llegar el ultimo paso apuntado; antes se ponian ya y el primer paso del tramo, que vacia la cola, se las llevaba (en la build de la 7aa
  no salieron las de la gargola).
- Pruebas: servidor, `homenaje()` (`Morvain, la torre del homenaje: ...` —el resumen, a pie cerrado / con el rastrillo / con todo, la
  Ultima Llama, donde se esta—, `Morvain, la casa de la Sombra: ...` —el rastrillo que sube, Maelis, la reja con la reina caida y si
  vuelve— y `Morvain, los de la torre del homenaje: ...`), `centinelas()` (`Morvain, los centinelas: ...`: lo que ve quieto, despierta,
  tajo, golpe, el humo, la estocada, el barrido por la espalda, el hachazo, se desploma) e `isolde()` (`Morvain, la reina Isolde: ...` y
  `... cae: ...`). Cliente, tramo `homenaje()` despues de la cadena: la torre desde el patio (`cliente_mv540`), el rastrillo (`mv541`),
  la Sombra al entrar (`mv542`), el salon (`mv543`), un centinela despierta (`mv544`), su estocada (`mv545`) y su humo (`mv546`), la
  armeria (`mv547`), el archivo (`mv548`), la prision (`mv549`), la capilla (`mv550`), Maelis (`mv551`), la reina en su trono (`mv552`),
  se levanta (`mv553`), grita (`mv554`), las garras (`mv555`), el abrazo (`mv556`) y el aprieto (`mv557`), la nana (`mv558`) y los ojos
  cerrados (`mv559`), desvanecerse (`mv560`), el velo que arde (`mv561`), las lagrimas (`mv562`), su corte (`mv563`), cae (`mv564`), la
  reja abierta (`mv565`), la azotea (`mv566`) y el bestiario (`mv567`).

### Plan grande, parte 7ac: el Corazon de la Aguja (la Sombra sin Nombre, el jefe final)

Tercera de las partes del final (respuestas 57, 65, 88, 122 y 125 del plan). La Sombra espera encadenada en la aguja de lo alto de la
ciudadela desde hace mil inviernos; trajo de vuelta a cada marcado para que alguien le abriera la puerta. El final (las cinematicas, el
epilogo de Maelis con sus frases 17-23 ya montadas, los creditos y el cielo de Morvain segun como acabo) es la parte 7ad.
- **La sala de la aguja** (`Ciudadela.aguja` y `Homenaje.aguja`): la aguja (`Ciudadela.RAG` 16 de radio, cuerpo de `CUERPO` 48, punta
  `PUNTA` 54) con ventanales rojos en arco y franjas de gemas; dentro (`Homenaje.R_AGUJA` 14, techo `TECHO_AGUJA`) el suelo es la marca
  de la Sombra en gemas (el ojo con la pupila de rendija, el aro roto con sus puas, los goterones hacia la puerta) con un anillo dorado;
  seis pilares (`PILARES_AGUJA`, a `R_PILAR` 11,5; angulo 0 = el sur) con su **grillete** a `GRILLETE` 14 de alto (hierro negro, la
  cadena rota colgando y un farol de almas encima), seis faroles colgados, la corona de cadenas del centro y dos braseros junto a la
  puerta. **La puerta** (en la azotea, al norte: `puertaAguja()`, 2 de ancho, 4 de alto, el grueso del muro; `sello(x, y, z)`: obsidiana
  que llora por dentro, gemas en medio, piedra negra cincelada) con su marco dorado y dos braseros. `enAguja(x, y, z)`; sitios
  `puerta_sombra`, `aguja`, `corazon`, `aguja_fondo`; la espera de la Sombra en `esperasAguja()` (el puesto `SOMBRA` 0, en su propio
  sitio `MorvainEnemigos.AGUJA`, grupo `Puestos.JEFE`, colgada: `REZA`). El Espejo: "El Corazon de la Aguja" (`darkcraft.morvain.lugar.aguja`).
- **Lo que pasa** (`morvain/Aguja`, desde `Morvain.onTick`, con su propio contador de ticks: las pruebas lo llaman sin que pase el tiempo
  del mundo): ante la puerta cerrada la Sombra dice su frase **25** ("Ahi esta... la puerta que nunca pude abrir. Pon la mano en ella,
  marcado. Dejame salir."; bit 16 de la marca `sombra_cadena`); **poner la mano** (la tecla del aviso: `client/morvain/ClientAguja` →
  `ClientRol.PUERTA` → `Net.PuertaAguja`; o clic derecho en ella) con la reina Isolde caida (si no: `darkcraft.aguja.no_cede`): el sello
  tiembla 1,5 s y se deshace bloque a bloque de abajo arriba (`sfx.puerta_aguja`); queda abierta para siempre (`aguja_puerta` en los
  datos de Morvain; si un chunk la trae cerrada, se quita una vez por sesion). **El sello** mientras pelea la Sombra (barreras en el hueco,
  cortina de chispas; quien este en medio pasa dentro). Dentro, el logro `aguja`. El aviso viejo de la 7ab (`darkcraft.homenaje.aguja`)
  ya no sale.
- **La Sombra sin Nombre** (`enemigos/Sombra`, `Guardian`, entidad `darkcraft:sombra`, 1,8 x 6; 2800 de vida, golpe 15, armadura 8; el
  modelo de `tools/enemigos/sombra.py` a 2,4: capucha sin cara con los ojos rojos, la corona rota de Corvin flotando encima, el manto de
  placas de hueso con puas, el pecho abierto con el corazon que late, brazos largos con grilletes y garras, la tunica en jirones que acaba
  en humo; variante 1 = la furia: seis ojos, grietas rojas y el bajo de la tunica ardiendo). Espera **colgada de sus cadenas** (el
  dibujante, `client/enemigos/SombraRenderer`, dibuja cuatro cadenas con la textura del juego desde donde estan de verdad sus munecas y su
  espalda en la animacion hasta los grilletes de los pilares del este, oeste y de detras: `Sombra.anclas()`); colgada no se le puede pegar.
  Ve a quien entra en la sala. **Despierta**: si alguno de los de la sala no la ha oido (bit 32), la **larga** (`DESPIERTA`, 17,6 s: su
  frase **26** —el discurso— mientras tira tres veces de las cadenas y en el tick 332 las rompe: `sfx.sombra_rompe`, eslabones por el
  aire); si no, la **corta** (`DESPIERTA_CORTA`, frase **33** "¿Otra vez aqui? Bien.", rompe en el 52). Ruge (la onda empuja). Sus
  frases suenan sin sitio con subtitulo (`Net.SombraDice`, `+ YA` corta la que sonaba: `ClientCadena.sombraYa`). **Zarpazos** (dos),
  **giro** (todo alrededor, empuja), el **latigazo de la cadena** (de 4 a 15: apunta en el tick 12 y el eslabon vuela en linea; a quien
  pilla —a 1,1 de la linea— lo arrastra hacia ella), las **manos** (golpea el suelo: bajo los pies de cada uno sale a los 26 ticks una
  mano de cinco dedos de piedra de sombra —`EfectosRaiz.estaca`— que agarra y frena), las **lanzas** (`enemigos/LanzaSombra`, tres en
  abanico, cinco con la furia; dano de sombra y debilidad) y la **niebla** (del tick 6 al 17 no se le puede pegar; aparece a la espalda
  de su presa —`Enemigo.fijarRumbo`— y le echa los zarpazos). **A media vida rompe la ultima cadena** (frase **27** "¡Basta! Eres
  MIO...": `FURIA`, la variante 1 en su tick 32; otro tema de musica): la **mirada** (sube, carga y el rayo rojo de sus ojos al suelo, a
  12 bloques, barre 140 grados del tick 31 al 61: `Sombra.rumboMirada`/`rayoSuelo`, lo dibuja el cliente con la misma cuenta; de cerca
  pasa por encima), el **grito** (radio 10: empuja, dana y oscurece) y **los que le fallaron** (frase **29** la primera vez: marcados
  efimeros que se alzan del suelo, 2 + 1 por jugador de mas, hasta 4). Si cae su presa, la frase **28**. Sin presa 110 ticks: vuelve a lo
  suyo, se cura, sus dos ojos, y al rato **vuelve a colgar de sus cadenas**.
- **No muere de un golpe** (`Sombra.setHealth`: la vida que fuera a quedar a cero se queda en 1): **se rinde** (`rendirse`: deja de
  pelear sin curarse —`Guardian.rendirPelea`—, se apunta `sombra_rendida` en los datos de Morvain —si el servidor se cierra vuelve a
  salir de rodillas: `yaRendida`—, no se mueve, `CAE` de rodillas en el aire y despues el bucle `RODILLAS`); a los 18 ticks ofrece su
  corazon (frase **30**: "Espera... espera, marcado. Toma mi corazon. Lleva mi corona... y la muerte no volvera a tocarte. O golpea... y
  vuelve a morir como todos los demas. Contestame.") y a los 62 sale **el corazon** (`morvain/Corazon`, un `PersonajeBase` sin guardar:
  flota delante de su pecho latiendo —`client/morvain/CorazonRenderer`, el objeto `corazon_sombra` en cubos a plena luz—). Se habla con el
  (la tecla del aviso o clic derecho): su dialogo `Dialogos.SOMBRA` (retrato `retratos/sombra.png`): quien oyo la oferta empieza por
  las respuestas, quien no por la 30 (`Aguja.inicio`): **golpear el corazon** (`Accion.GOLPEAR`), **tomarlo** (`Accion.TOMAR`) o no decir
  nada. Golpearlo con el arma tambien es responder "golpear" (cuando ya ha acabado de ofrecerlo). Al responder (`Aguja.elegir`): el
  corazon se rompe (`sfx.corazon_rompe`) o vuela al pecho de quien lo toma (`sfx.corazon_toma`, oscuridad); la Sombra dice su ultima
  frase (**31** "No... yo esperaba... siempre esperaba... que frio..." o **32** "Si... Ahora esperaras tu... como espere yo.") y al
  acabarla muere de verdad y se deshace subiendo: el reparto de `Guardianes` (JEFE ABATIDO, 60000 ecos, logro `sombra` —hazana— y el
  bestiario) y `Aguja.alMorir`: se apunta como acabo para todos (`final_alba` / `final_sombra`; y en la ficha de quien respondio,
  `sombra_final` 1 o 2), **el cielo rojo se apaga** (`CieloRojo.apagar(srv, true)`) y los logros del final a los de la sala: `alba` (o
  `nueva_sombra` a quien tomo el corazon). Deja cuatro piedras de forja III.
- **Barra, musica, voz**: barra propia (`tools/hud/barra_guardianes.py` "sombra": sus cadenas rotas por tres sitios con las puntas al
  rojo, la capucha sin cara con los ojos y la corona rota; oscuridad con venas rojas que laten; jirones y chispas que suben); musica
  "The Final Battle" de skrjablin (dominio publico, id `sombra1`, tema 47) hasta media vida y "Epic Endgame Cinematic" de cynicmusic
  (CC0, id `sombra2`, tema 48) despues (`Sombra.musica()` segun la variante); el aire de la sala (`ambiente.aguja`, master
  `aguja_ambiente`, en `ClientMusic.tickCrater`, `craterCual` 3). Voz: la de siempre de la Sombra ("Grav"), frases 25-33 con su susurro
  (`TOMAS_SOMBRA`/`SUSURROS_SOMBRA`; la 30 tambien como voz de dialogo, `voz.sombra.30`). Efectos (ElevenLabs, `TOMAS_SFX`):
  `sombra_cadenas`, `_rompe`, `_rugido`, `_zarpazo`, `_cadena`, `_manos`, `_lanzas`, `_giro`, `_niebla`, `_mirada_carga`, `_mirada`,
  `_grito`, `_caidos`, `_dolor`, `_cae`, `_latido`, `puerta_aguja`, `aguja_ambiente`, `corazon_rompe`, `corazon_toma`. Las frases de
  Maelis del epilogo (17-23) ya se montan (`TOMAS_GUIA`), para la 7ad.
- Arte: `tools/morvain/aguja_arte.py` (la lanza, el corazon —textura y modelo en cubos— y el retrato). Logros `aguja`, `sombra`
  (hazana), `alba` y `nueva_sombra` (hazanas ocultas), con su entrada del diario (`Espejo.ORDEN_LOGROS`).
- Comandos `/darkcraft aguja [ir | abrir | cerrar | olvidar]` y `/darkcraft sombra [ir | vencer | volver | media | golpear | tomar |
  zarpazos | giro | cadena | manos | lanzas | niebla | mirada | grito | caidos]` (`vencer` = que se rinda ya; `volver` = todo como al
  principio: sin rendir ni final, su puesto en pie).
- Para las pruebas: `CieloRojo.copia` / `restaurar` (lo dejan como estaba), `Sombra.quieta`, `probar`, `mediaVida`, `vencer`, `estado` y
  contadores; `Aguja.resumen`, `reiniciar`, `cerrar`, `abrir(m, true)`.
- Pruebas: servidor, `sombra()` (`Morvain, la aguja: ...` —la puerta con la reina en pie y caida, la Sombra colgada, lo que ve, despierta
  (la corta y la larga), rompe, la puerta sellada, los golpes, la niebla—, `Morvain, la Sombra a media vida: ...` y `Morvain, la Sombra se
  rinde: ...` —un golpe de muerte la rinde, el corazon, golpearlo antes de tiempo no cuenta, golpearlo y, otra vez, tomarlo: muere, su
  puesto caido, el final, el cielo rojo apagado; despues todo como estaba—). Cliente, tramo `aguja()` despues del homenaje: la puerta con
  la Sombra y el aviso (`cliente_mv570`), el sello que se deshace (`mv571`), la Sombra encadenada (`mv572`), los tirones (`mv573`), las
  cadenas rotas (`mv574`), el rugido (`mv575`), zarpazos, giro, cadena, manos, lanzas y niebla (`mv576..581`), la furia (`mv582`), la
  mirada (`mv583`), el grito (`mv584`), los caidos (`mv585`), rendida con el corazon (`mv586`), las respuestas (`mv587`), su ultima frase
  (`mv588`), se deshace (`mv589`), la sala vacia (`mv590`) y el bestiario (`mv591`). `LIMIT` 132 min (`lanzar.sh` 138, el paso 166, el trabajo 214).

### Plan grande, parte 7ad: el final (la cinematica, los creditos, el epilogo de Maelis y el cielo de Morvain)

La ultima de las partes del final (respuestas 57, 65 y 125 del plan). Cuando la Sombra sin Nombre se deshace (`Sombra.die` ->
`Aguja.alMorir`, que ya apunta el final y apaga el cielo rojo) empieza **el final** (`morvain/Epilogo.empezar`):
- **La cinematica del final** (`cine/Desenlace`: los tiempos y los planos, para servidor y cliente; cinematicas con guion **43** el alba
  —se golpeo el corazon— y **44** la nueva Sombra —se tomo—; la ven los que estan a 64 de la Sombra, `Cinematics.playGuion`; 39 s; se
  salta con ESPACIO). Cuatro planos con un corte en negro entre uno y otro (`P` / `Q`): 1) la sala de la aguja, de lado entre la Sombra
  y quien eligio (dentro de la sala y lejos de los pilares, `dentro` 9,2): en el alba sube luz (motas de luz, `Guion.tick`) de donde se
  deshace y la camara la sigue hacia arriba; en la otra acaba en quien tomo el corazon, con la oscuridad que le entra (tinta), dos
  latidos y las brasas; 2) fuera, la aguja contra el ocaso (el sol se pone al oeste, detras de la aguja), subiendo y alejandose: ahi
  **cambia el cielo de Morvain** (desde `CIELO` 8 s, en `FUNDIDO` 9 s); 3) la isla colgada de sus cadenas desde el sudeste: entra la
  musica de los creditos (`MUSICA` 16,1 s) y **habla Maelis en off** (`VOZ` 17 s: su frase 17 en el alba, la 21 en la otra; voz
  `voz.guia.N`, subtitulo `darkcraft.dialogo.guia.N` en la banda de abajo, la musica baja mientras); 4) por encima de todo: el alba mira
  al horizonte y acaba en una luz calida (`LUZ` 33,6 s) que lo llena todo; la nueva Sombra mira abajo, a la ciudadela que ahora es suya,
  girando. A negro desde `NEGRO` 36,9 s. Todo lo de cada final esta en `Guion.desenlace` (pasos, el aire de lo alto —`ambiente.cadena`—,
  la musica y la voz), `negro`, `jugadores` (en la sala se ve a los jugadores), `brasas`, `bajoNegro` (la luz) y `subtitulos`.
- **Los creditos** (`client/ui/CreditosScreen`): al acabar la cinematica sola o saltada (`ClientCine.creditos`; si la corta el servidor,
  no: la musica se va). A pantalla completa sobre negro: "Dark Craft" en gotica a doble tamano, como acabo ("El alba" / "La nueva
  Sombra") y una frase; quien lo hizo; las voces (cada personaje con su voz de ElevenLabs, `VOCES`); el narrador; **la musica** (la
  lista que escribe la CI, `creditos_musica.json`, de `ci/musica/pistas.json`: titulo, autor y licencia de cada pista); los sonidos; lo
  que va dentro (DH, Oculus, Embeddium, el shader); la letra; Forge; y al final "Gracias por jugar" con la ultima frase. Suben en lo que
  le queda a la musica (70-170 s, con la duracion de `musica.json`); ESPACIO seis veces mas deprisa, ESC los acaba; al llegar al final
  esperan unos segundos y se apagan. Con el alba, motas doradas que suben y un resplandor calido abajo; con la nueva Sombra, ceniza gris
  que cae y frio arriba. Al cerrarse avisan al servidor (`Net.CreditosFin`). Mientras se mira la cinematica o los creditos no le pasa
  nada al jugador (`Epilogo.VIENDO`, hasta 6 min).
- **La musica del final** (`ClientMusic.final_`, entra en la cinematica y sigue en los creditos; mientras suena callan las demas:
  `sinFinal()` en cada zona): el alba "New Sunrise" de nene (CC0, id `final_alba`, OpenGameArt node 69912, la version V2); la nueva
  Sombra "Fantasy: Lament for a Warrior's Soul" de RandomMind (CC0, id `final_sombra`, la que se volvio a subir).
- **El epilogo de Maelis**: al acabar los creditos, a quien vio la cinematica (`Ficha.dialogo(p, "epilogo")` 1) y esta en Morvain se le
  lleva a la nave de la capilla de la torre del homenaje, a unos tres bloques de Maelis y mirandola (`Epilogo.sitioCapilla`); a los 30
  ticks (`ESPERA_MAELIS`) habla ella sola (`Conversaciones.hablar`; si no estaba, se la sienta: `Epilogo.maelis`). `Conversaciones.
  inicioGuia`: despues del final, una vez (marca `GUIA_EPILOGO` = 32), su epilogo: el alba **18 → 19 → 20** ("los fuegos se apagan...",
  "tu marca se apaga...", "gracias... por no contestarle"), la nueva Sombra **22 → 23** ("su hambre despertara en ti...", "...tu siempre
  esperas"); quien no vio la cinematica (un amigo en otro sitio) empieza por la 17 o la 21 la primera vez que hable con ella, este donde
  este. Despues, lo de siempre. Logro `epilogo` ("Lo que queda") con su entrada del diario segun el final (`epilogo_alba` /
  `epilogo_sombra`, `Espejo.logroHecho`). Voz de Maelis (la de siempre), frases 17-23 (tomas a; masters de la 7ac).
- **El cielo de Morvain despues** (`client/morvain/CieloFinal`; el servidor lo manda con `Net.Final(cual, desde)` al acabar, al entrar
  y al cambiar de dimension; `Epilogo.cual`): con el alba el ocaso dorado y rojo se vuelve un amanecer (velo azul palido arriba y rosado
  en el horizonte —la caja sin profundidad de `AFTER_SKY`, como el cielo rojo—, la niebla melocoton clara, el resplandor del sol rosa y
  dorado —`MorvainCliente.Efectos.getSunriseColor` -> `CieloFinal.ocaso`— y la luz del cielo mas viva —`adjustLightmapColors` ->
  `luz`—); con la nueva Sombra, gris y frio (velo y niebla grises, sin resplandor, la luz sin color). Con shaders, el velo no se
  dibuja. **Los fuegos arden frios** con la nueva Sombra, en todas partes: los santuarios echan copos de nieve y motas blancas
  (`ClientSantuario`, `CieloFinal.frio`). `Aguja.reiniciar` quita el final y lo manda.
- Comandos `/darkcraft final [alba | sombra | creditos | epilogo | olvidar | deshacer]` (`alba` / `sombra`: lo apunta, lleva a la sala de
  la aguja y pone su cinematica, con sus creditos y Maelis; `creditos`: solo los creditos, `Net.Creditos`; `epilogo`: a la capilla y
  habla Maelis; `olvidar`: que ese jugador lo vuelva a ver todo; `deshacer`: quita el final del mundo). `Net.V` = "24".
- Pruebas: servidor, `desenlace()` (`Morvain, el final: ...` —por donde empieza Maelis en cada final, viendo o sin ver la cinematica, y
  despues ya no; al acabar los creditos se le lleva a la capilla y Maelis habla a los 30 ticks con el epilogo— y `Morvain, el final, la
  camara: ...` —la camara de los cuatro planos de las dos cinematicas no se mete en nada y en la sala se ve a la Sombra—). Cliente, en el
  tramo `aguja()`: al golpear el corazon, la cinematica del alba (`cliente_mv589` la sala con la Sombra que se deshace, `mv600` la luz,
  `mv601` y `mv602` la aguja con el cielo que cambia, `mv603` la isla con Maelis, `mv604` el horizonte, `mv605` la luz), los creditos
  (`mv606` el titulo, `mv607` a la mitad, `mv608` "Gracias por jugar"), el epilogo en la capilla (`mv609..mv611`, frases 18-20) y el
  cielo del alba (`mv612`); despues de la sala vacia y el bestiario, el final de la nueva Sombra con el comando (`mv613..mv616`, los
  creditos `mv617..mv619`, el epilogo `mv620` y `mv621`, el cielo gris `mv622` y el fuego frio de la Ultima Llama `mv623`); al acabar,
  todo como estaba. `LIMIT` 136 min (`lanzar.sh` 142, el paso 170, el trabajo 218).

### Plan grande, parte 7ae: la cronica de Morvain (los capitulos 7-10, que cuenta Maelis)

Respuestas 61 y 126 del plan: los capitulos de Morvain los narra la guia de los santuarios (la voz de Aldous queda para el 1-5).
- **Cuatro capitulos** (`Story.CHAPTERS` = 10, `Story.MORVAIN` = 7, `Story.morvain(ch)`): VII **Las Tierras Marchitas** (lo que era
  Morvain, la noche del cielo rojo, los que se quedaron atados a su juramento, la ciudadela de las ocho cadenas), VIII **Los que se
  quedaron** (Halvard, la Rectora, el Abad, el barquero, Brann, Anselmo: "recuerda sus nombres"), IX **El primer marcado** (Aldric,
  los que golpeaban la cadena, la subida y la boca sin puerta: la Sombra necesita una mano viva que le abra una puerta) y X **Las
  hermanas del fuego** (las ocho cadenas forjadas con el fuego de sus almas, cada santuario una hermana, el juramento roto de Corvin,
  la ultima llama, "no puedo elegir por ti"). Textos en `ci/historia/perdidos.py` (`NARRACION_MORVAIN`: ingles del audio y espanol;
  titulos `TITULOS_MORVAIN`; las paginas del libro `PAGINAS_MORVAIN`: `Story.PAGES` 6, 5, 5 y 6; los demas idiomas, en ingles hasta su
  tanda); `textos.py` comprueba que caben. El libro: "Cronica de Morvain N", de Maelis (`darkcraft.libro.morvain`, `...lore.morvain`).
- **La voz** (ElevenLabs, "Autumn Veil", flow `hempVQvOuj9vp33JKP9I`; `voz_guia.cronica` en `sesiones.json`): 33 frases con dos
  tomas (masters `guia/capC_N{a,b}.mp3`). `ci/eleven/procesar.py` `capitulo_guia` monta cada capitulo con `VOZ_CRONICA` (la de
  Maelis con algo mas de sala) y sus pausas (`CAPITULOS_GUIA`, todas tomas a) en `sounds/narracion/capC.ogg` (estereo, en streaming) y
  `cinematica/capC.json`, como el 6. `narracion.cap7..10` (`DarkCraftMod.NARRACION_7..10`), subtitulo `darkcraft.narracion.C.N`.
- **Los cofres** (`morvain/CronicaMorvain`): el generador de Morvain no escribe datos de bloque, asi que se ponen una vez en tiempo de
  juego (`Morvain.Datos` "cronica7"...) la primera vez que alguien pasa a menos de 64 con su chunk cargado (`tick`, cada segundo desde
  `Morvain.onTick`): el 7 junto al santuario del Arco del Umbral (al noroeste, al otro lado de Maelis), el 8 ante la estatua caida de la
  plaza del Juramento de la Capital (al oeste del pedestal), el 9 dentro de la choza de Aldric en el Cerco del Ancla (a la izquierda de
  la puerta) y el 10 en la capilla negra, en la nave ante el altar. Brillan y se abren como los de siempre (`StoryEvents`).
- **Las cinematicas** (`morvain/PlanosCronica`, desde `Cinematics.play` si se esta en Morvain; grupos `Timeline.GROUPS7..10`): todo
  cerca del cofre, sin mover a nadie. VII: sube por encima del arco mirando a las llanuras; los pilares rotos a ras de suelo; el arco
  del portal desde abajo; el camino del norte con la estela; el horizonte donde cuelga la ciudadela (con la lejania se ve); Maelis de
  cara con el santuario detras. VIII: la Puerta Oeste desde la calle mayor; la camara sube en el cruce sobre los tejados quemados; las
  fachadas de la calle; la plaza alrededor de la estatua (por el este); la estatua de cerca; el jugador. IX: el ancla y la cadena desde
  el patio; el ancla entera desde el lado del campamento (a 26, con el primer eslabon encima: a ras de suelo y a 17 solo se veia la
  pared de la piramide); el primer eslabon de lado; la camara sube junto a la cadena; la cima del ancla (Aldric);
  el Cerco entero desde lo alto. X: la nave hacia el retablo; la isla colgada de sus cadenas desde fuera; los candelabros; el trono de
  la reina (un piso mas arriba); la Ultima Llama de cerca; la aguja contra el cielo; Maelis junto al fuego. Cada plano se busca libre
  (`buscar`: si choca con algo o se tapa lo que mira, prueba al lado; nunca pasa a menos de 1,3 del jugador); en estos capitulos se ve
  a los jugadores (`ClientCine.onPlayerRender`). Preguntan a un `PlanosCronica.Mundo` (el de verdad o el de mentira).
- **Sin abrir el juego**: `VOLCADO_OPTS="-Dcronica=N" tools/vista3d/vista.sh morvain_llegada|morvain_capital|morvain_cerco|morvain_homenaje
  carpeta` escribe por plano si la camara se mete en algo o se tapa lo que mira y `volcado.bin.planos.txt`; **`tools/vista3d/camara.py
  volcado.bin planos.txt carpeta`** dibuja lo que ve la camara en tres momentos de cada plano (perspectiva con el campo de vision de
  Minecraft, los colores de vista.py, el jugador como una columna roja). **Ojo: con Forge, fuera del juego cada estado de bloque no
  sabe si es macizo (`blocksMotion`, `isSolid`) hasta que se le llama `initCache()`** (`Blocks.rebuildCache` no sirve: la lista de
  estados esta vacia): `Volcado.main` lo hace con todos al empezar; antes las cuentas de "tapada" y "metida" (y lo que toca la
  ciudadela) salian 0 siempre. Ahora salen como en la CI (los planos del 9 de la build 192: 15, 25, 0... en la CI y 13, 25, 0... aqui).
- **El menu y el codice**: la Historia tiene su apartado "Cronica de Morvain" (desde que se abrio el portal o se leyo alguno); las
  miniaturas 12-15 (`CodiceScreen.miniaturaCapitulo`; el atlas `miniaturas.png` pasa a 512 x 320: las tierras marchitas con el molino y
  el campanario, la puerta de la Capital con la estatua y los calcinados, la cadena que sube del ancla con Aldric arriba, las ocho
  llamas alrededor de la Sombra y la ultima llama con Maelis).
- **Logros** `capitulo7..10` (de la llegada, la capital, el cerco y la ultima llama; con su entrada del diario). Comandos
  `/darkcraft cronica [ir <7-10> | poner | olvidar]` (sin mas: como estan).
- Pruebas: servidor, `cronicaMorvain()` (`Morvain, la cronica: ...` —los cuatro cofres puestos con su marca, una sola vez, y sus
  libros— y `Morvain, la cronica, la camara: ...` —audio, frases y duracion de cada capitulo y por plano si la camara se mete o se tapa
  lo que mira—). Cliente, tramo `cronica()` despues de `aguja()`: en cada cofre un clic derecho de verdad, la cinematica (`cliente_crCC_0`
  el titulo y `crCC_1..` un plano cada una, con el estado de la voz) y el cofre con el libro (`crCC_cofre`); la Historia del menu
  (`cr90`) y los capitulos 7 y 10 en el codice (`cr91`, `cr92`). `LIMIT` 144 min (`lanzar.sh` 150, el paso 178, el trabajo 226).

### Plan grande, parte 7af: los herreros de Morvain y Silas, el mercader ambulante

Respuesta 95 del plan ("herrero en un refugio y VARIOS herreros por el mapa con animacion currada golpeando el yunque";
"mercaderes ambulantes"):
- **Las herrerias** (`morvain/Herrerias`, escritas al generar el chunk, desde `MorvainGenerador`; las ve Distant Horizons). Cada
  una en su marco (u, v) con la entrada hacia +v, girada a su `frente`; lo que se pisa en y + 1; el suelo se allana y se funde con
  el de alrededor en `FUNDE` (7) bloques, solo si hay que cambiarlo; `Adorno` no pone arboles en sus chunks.
  - El **Refugio de la Fragua** (`REFUGIO`, 20 al norte de la Encrucijada de los Peregrinos): casa de piedra con esquinas y viga de
    roble oscuro, tejado de pizarra a dos aguas con un trozo hundido al noroeste (musgo y escombros debajo), chimenea de ladrillo
    con una hoguera de senales en su hueco (el humo sale por arriba), alto horno y horno, el yunque de **Hagen**, la tina, la
    piedra de afilar, el banco de herrero, pararrayos colgados como hojas, un yunque mellado, barriles (uno con botin:
    `loot_tables/chests/refugio`), el petate, la mesa con su farol; fuera la lena, el abrevadero, el heno, dos farolas y el
    camino. Entrar en el es el logro `refugio` (y su lugar en el mapa, `darkcraft.mapa.lugar.refugio`).
  - Tres **campamentos** (tienda de lona sobre cuatro palos, fragua al aire, yunque, tina, piedra de afilar y lo de su sitio):
    **Brenna** (`FARO`, entrada del Puerto Varado, al oeste del Faro de los Huesos: lona de vela, barca volcada, cadenas, huesos),
    **Ulrik** (`COLMILLO`, al oeste del Paso del Colmillo: lona nevada, banco de troncos, pieles, lena) y **Tobin** (`CAPITAL`, al
    norte de la Puerta de la Capital: lona roja de estandarte, cascotes, un carro de barriles, el estandarte de la ciudad).
  - Los herreros se ponen una vez (`Morvain.Datos`), la primera vez que alguien pasa a menos de `CERCA` (56) con su chunk cargado.
- **Los herreros** (`Herrero.Quien`: DORVAN, HAGEN, BRENNA, ULRIK, TOBIN; sincronizado, NBT `quien`): cada uno con su ritmo
  (`ciclo`, `golpe`), su fuerza al golpear y su tamano (Ulrik 1,08, Tobin 0,92); cada `CADA_MIRA` (5) martillazos para y mira la
  hoja (`mirando`). Pieles y retratos de `tools/personajes/herreros.py` (Hagen viejo y canoso con delantal de cuero, Brenna con
  trenza y panuelo, Ulrik grande con barba trenzada y pieles, Tobin joven con gorro). Su forja es la de Dorvan (las mismas
  mejoras y cenizas) con su nombre en el titulo (`darkcraft.forja.de`) y su voz.
- **Silas** (`Mercader.Quien.SILAS`, `morvain/Ambulante`): va de santuario en santuario (`RUTA`: el Vado, los Juncos, la Puerta de
  la Academia, el Costillar, el Mirador y los Ahorcados), `ESTANCIA` (48000 ticks, dos dias del mundo de las nubes: en Morvain la
  hora no pasa) en cada uno; donde esta lo dice la hora del mundo (`donde`), solo se guarda un desfase para los comandos. Con alguien
  a menos de 64 se le pone junto al fuego del santuario, al otro lado de la guia, mirando hacia fuera; los que esten en otro sitio
  se van con una bocanada de humo en cuanto no hay nadie a 12 bloques. Lanza una moneda al aire de vez en cuando. Su tienda
  (`Tienda.SILAS`, lo comprado con la clave `silas:`): piedras de forja II y III, frutos de tormenta, lunar, hueso y sombra,
  flechas y lo raro (hechizos de esquirlas, lanza y rayo, la bendicion, talismanes del aliento, la mente y la codicia, cenizas de
  paso, relampago y sangre; uno por jugador). "Adonde iras despues" (frase 5) acaba con un aviso del santuario siguiente
  (`Accion.RUMBO`, `Ambulante.rumbo`).
- **Las voces** (ElevenLabs, flow `axmpHbUFmXhAI6aMSFpC`, dos tomas cada frase, masters `ci/eleven/master/<id>/voz_N{a,b}.mp3`):
  Hagen "Daniel - The Gruff Old British Wizard", Brenna "Emma - Wry, Gritty Hero", Ulrik "Bjorn The Viking", Tobin "Alfwyn -
  Excitable & Squeaky" y Silas "Callum - Husky Trickster" (con la sala de Kerrick). Los herreros: 1 se presenta, 2 saluda, 3 al
  abrir la forja, 4 al mejorar o poner una ceniza, 5 si falta algo, 6 su historia, 7 se despide, 8 al maximo; Hagen ademas la 9 (las
  cenizas de guerra). Silas: 1 se presenta, 2 saluda, 3 al comprar, 4 si no llegan los ecos, 5 adonde ira, 6 se despide.
  `procesar.py`: `TOMAS_*`/`VOZ_*` de cada uno. Subtitulos y respuestas en `ci/historia/rol.py` (ES/EN).
- **Logros**: `refugio`, `hagen` (hablar con el), `herreros` (con los cuatro de Morvain: `Conversaciones.herrerosMorvain`), `silas`
  y `silas_compra` (cada uno con su entrada del diario). Comandos `/darkcraft herreros [ir <quien> | poner | olvidar]` y
  `/darkcraft silas [ir | mover <0-5>]` (sin mas: como estan).
- **Sin abrir el juego**: `VOLCADO_OPTS="-Dsitio=hagen|brenna|ulrik|tobin" tools/vista3d/vista.sh morvain_herreria carpeta` (dice si
  se llega a pie desde el santuario al herrero y delante del yunque).
- Pruebas: servidor, `herrerosMorvain()` (`Morvain, los herreros: ...` —puestos una vez, cada herrero donde toca, mirando al yunque,
  los fuegos, los dialogos, los logros, la forja de Hagen y el botin— y `Morvain, Silas: ...` —en cada santuario de la ruta, se va
  si no le toca, la tienda con su clave, el rumbo—). Cliente, tramo `herreros()` despues de `cronica()`: el Refugio por fuera
  (`cliente_hr01`), Hagen martilleando (`hr02`, `hr03`) y mirando (`hr04`), hablar con el (`hr05`), su menu (`hr06`) y su forja
  (`hr07`); los tres campamentos (`hr10`..`hr15`, de lejos y hablando); Silas (`hr20`), hablando (`hr21`), su tienda (`hr22`), una
  compra (`hr23`) y el aviso de adonde ira (`hr24`).

### Plan grande, parte 7ag: la campana del peregrino (Oswin, la primera mision)

Respuestas 95 ("personajes con misiones") y 117 ("a veces 2-3 respuestas que cambian la mision") del plan.
- **Quien da las misiones** (`personajes/Viajero`, entidad `darkcraft:viajero`, `Quien` sincronizado y guardado: de momento
  `OSWIN`): de pie, apoyado con las dos manos en su bordon. Modelo (`client/personajes/ViajeroRenderer`, capa `darkcraft:viajero`,
  textura de 128 x 64 de `tools/personajes/oswin.py`): el de jugador de brazos anchos con el sombrero de peregrino (copa baja, ala
  ancha y la concha delante), la esclavina, el zurron y el bordon con su calabaza (suelto, de pie delante de el); la cabeza gacha
  (es ciego: escucha). Al devolverle la campana la alza con la derecha y la toca (`Viajero.tocar`, evento 61; la campana es el
  mismo modelo que la del arbol).
- **Oswin, el peregrino ciego** (`mision/Oswin`): junto a la Encrucijada de los Peregrinos (6 al oeste y 3 al norte del fuego,
  de cara a el), con un mojon de piedra con musgo y un farol a su espalda. Se pone una vez (`Morvain.Datos` "oswin") cuando
  alguien pasa a 56 con el chunk cargado y sus entidades al dia (si ya estaba, solo se apunta).
- **La mision** (marcas en la ficha, `Ficha.mision(jugador, "oswin")`: CONOCIDA, ACEPTADA, RECHAZADA, PRECIO, TOMADA, DEVUELTA,
  QUEDADA): se presenta (1: perdio su campana y los muertos no le dejan pasar) y su menu (nodo 100) tiene tres respuestas:
  **buscarla** (2; `Accion.ACEPTAR`, cartel grande "Nueva mision"), **que se gana** (3: el rosario de su orden, y otra vez el menu)
  o **no** (4). La vez siguiente: 6 (el que paso de largo) y el menu, o 5 (con la mision y sin la campana). Con la campana: 7 y el
  segundo menu (101, o 102 si no pregunto que ganaba: el servidor empieza en el nodo 107): **devolversela** (8; el Rosario del
  peregrino, `Talisman.Clase.PEREGRINO` —los frascos de vida curan un 25 % mas—; si no pregunto que ganaba, 9 y las limosnas de su
  orden, 3000 ecos; los peregrinos muertos echan a andar; logro `campana_peregrino`, cartel "Mision cumplida") o **quedarsela**
  (10; la campana pasa a ser la **Campana de los difuntos**, `mision/CampanaDifuntos`: se toca y los muertos a 9 bloques —no los
  jefes— se apartan, torpes y debiles 6 s; vuelve a sonar a los 30 s; logro `campana_difuntos`). Despues, 12 o 11. **Las respuestas
  con accion y destino** (`Dialogos.r(pj, clave, destino, accion)`): el cliente manda la accion y la conversacion sigue
  (`DialogoScreen.elegir`); las que abren la forja o la tienda siguen igual. Nodos con otro numero que su voz: `frase(id, voz, sig)`;
  mas de un menu: `menu(id, ...)`.
- **La campana** (`mision/Campana`, entidad `darkcraft:campana_arbol`): cuelga del cabo de una soga nueva en la punta de la rama
  este del Arbol de la Horca (`Arboleda`, soga de tipo `CAMPANA` en el paso 9 de la rama; `Arboleda.campana()`, sitios
  `campana` y `ante_campana`), a dos bloques del suelo, entre dos ahorcados. Se pone una vez ("campana") con alguien a 48; si en
  ese chunk no estuviera la soga, la pone hasta la rama. Suena con el viento cada 8-18 s con alguien a 40 (`sfx.campana_viento`) y
  se mece (`CampanaRenderer`: modelo de cinco cajas y el badajo, textura de `tools/mision/campana.py`); golpearla la hace sonar.
  **Se coge** con clic derecho o con la E (`ClientMisiones.campana`: "Pulsa [E] para coger la campana", `ClientRol.CAMPANA`): cada
  jugador la suya (objeto clave `campana_peregrino`), tambien sin conocer a Oswin; a quien ya la lleva o acabo la mision su
  cliente no se la dibuja ni la deja mirar (`Campana.oculta`).
- **Los peregrinos muertos** (`mision/Peregrino`, no se guardan): cinco, al oeste de Oswin, andan rezando hacia el norte 250 ticks
  por encima del suelo, medio transparentes y con luz propia (`PeregrinoRenderer`: el modelo de Oswin sin bordon, las manos juntas,
  la piel palida de `oswin.py`), y se apagan; con `sfx.peregrinos`.
- **Lo que sabe el cliente** (`mision/Misiones` → `Net.Misiones`: cada mision con sus marcas —mas `Oswin.LLEVA` si lleva la
  campana— y adonde ir; al entrar, al cambiar de dimension, al cambiar algo y cada segundo si coge o suelta la campana; `Net.V` 25):
  `client/mision/ClientMisiones`. **El diario**: la Historia del menu tiene arriba el apartado **Misiones** (las en marcha antes; a la
  derecha la miniatura nueva de la quinta fila del atlas —`miniaturas.png` pasa a 512 x 400: la campana colgada del arbol al ocaso
  con el peregrino al fondo del camino—, "Mision en marcha"/"acabada" y como va: `darkcraft.mision.oswin.<pendiente | buscar |
  volver | encontrada | devuelta | quedada>`). **La brujula** marca adonde ir con una campanita de oro (`HudTex.MARCA_MISION`) y **el
  mapa de la M**, con la campana de los iconos del espejo (`I_MISION` 40, `I_MISION_HECHA` 41) latiendo y, con el raton encima, la
  mision y lo que toca.
- **Voces** (ElevenLabs, `eleven_v4`, flow `P5ZsmqWbPOGerwlbHTeo`): Oswin **"Alden - Tired Old Man"** `S9V0fuqWOqX0i8Kb8ctu`, 12
  frases (tomas a; `VOZ_OSWIN`: al aire libre, cerca). Efectos (`eleven_text_to_sound_v3`): `campana_viento` (1, 2 y 3),
  `campana_toque` (1, 2 y 3), `peregrinos` (2 y 1).
- Logros `campana_peregrino` ("La campana del peregrino") y `campana_difuntos` ("Por quien doblan"), con su entrada del diario.
  Comandos `/darkcraft misiones [olvidar | marcar <n>]` y `/darkcraft oswin [ir | campana | poner | olvidar]`.
- Pruebas: servidor, `misionOswin()` (linea `Morvain, la campana del peregrino: ...`: Oswin y la campana puestos —pisa, mira al
  fuego, el mojon, la soga hasta la rama, la boca de la campana sobre el suelo—, el dialogo sin nodos que lleven a nada y los cuatro
  caminos: con precio, sin precio —las limosnas—, quedarsela —el marchito lento y debil— y cogerla sin conocerle). Cliente, tramo
  `oswin()` despues de `herreros()`: Oswin (`cliente_ow01`, `ow02`), hablar (`ow03`), el menu (`ow04`), el precio (`ow05`), el menu
  otra vez (`ow06`), aceptar (`ow07`) y su cartel (`ow08`), la Historia (`ow09`), la brujula (`ow10`), el mapa (`ow11`), la campana en
  el arbol (`ow12`, `ow13`) y cogida (`ow14`), con ella ante Oswin (`ow15`, `ow16`), devolverla (`ow17`), los peregrinos (`ow18`), el
  cartel (`ow19`), la Historia (`ow20`), quedarsela (`ow21`, `ow22`) y la campana de los difuntos (`ow23`).

### Plan grande, parte 7ah: la bufanda roja (Hedda, la madre de la Puerta Oeste)

La segunda mision (respuestas 95 y 117 del plan), con tres finales y con **cada jugador viendo lo suyo**.
- **Hedda** (`personajes/Viajero`, `Quien.HEDDA`; la entidad lleva ya una `variante` sincronizada y guardada): una vieja de luto
  sentada en su taburete en el camino de la Puerta Oeste de la Capital (u -45, v 4: al sur del camino, entre el santuario y los carros
  quemados), de cara a la puerta, tejiendo una bufanda roja, con su farol en el suelo al norte. Modelo (`ViajeroRenderer.ModeloHedda`,
  capa `darkcraft:viajero_hedda`, textura de 128 x 64 de `tools/personajes/hedda.py`, con su retrato): el de brazos finos con el chal,
  el faldon de la falda que cae de las rodillas, los zapatos, el taburete, el ovillo y, en las manos, las agujas y la labor roja; las
  agujas van y vienen y se oyen de cerca (`sfx.agujas`, `ClientMisiones`). **`ViajeroRenderer` ya tiene un modelo por `Quien`** (los
  dos heredan de `Base`, que lleva el `alfa` y lo que se baja respecto al suelo). La variante 1 es **Hedda de rodillas junto a su hijo**
  en la sala de la guardia (u 0, v -1), la mano tendida hacia el, con la bufanda.
- **Tam** (`mision/RestosTam`, entidad `darkcraft:restos_tam`; `client/mision/RestosRenderer`, textura de `tools/mision/tam.py`): un
  esqueleto sentado en el suelo de la sala de la guardia, encima del arco de la Puerta Oeste (u -1, v 0), de espaldas al torno del
  rastrillo (la piedra de afilar), con su capacete abollado, la sobreveste quemada de la guardia, la **bufanda roja** al cuello y su
  **lanza atravesada en la rueda del torno**: nadie iba a subir la reja. Con clic derecho o la E (`ClientMisiones.restos`, "Pulsa [E]
  para coger la bufanda" / "para sacar la lanza", `ClientRol.RESTOS`; si a la vez se mira a Hedda de rodillas, gana lo que se mira mas
  de frente): la bufanda (objeto clave `bufanda_roja`, `mision/ObjetoMision`: su descripcion cuenta lo que se ve) o, tras la verdad,
  la lanza (`Hedda.tomar`).
- **La mision** (`mision/Hedda`, marcas CONOCIDA, ACEPTADA, RECHAZADA, NOCHE, TOMADA, VERDAD, MENTIRA, CALLADA, LANZA): se presenta (1)
  y su menu: **buscarle** (2: la sala de encima del arco, junto al torno; cartel "Nueva mision"), **que paso aquella noche** (3: el
  Condestable mando cerrar todas las puertas; otra vez el menu) o **no** (4). Despues, 6 (y el menu) o 5. Con la bufanda: 7 (o 14 si no la
  conocia) y el segundo menu (101), que **acaba la mision de tres maneras**:
  - **la verdad** ("Sostuvo la puerta cerrada. Por orden."; 8 y 9): se va a cerrarle los ojos; para quien se lo dijo, la del camino
    se apaga y se la encuentra de rodillas junto a Tam (13); le pide que saque su lanza de la rueda: la **Lanza de la Puerta Oeste**
    (`mision/LanzaTam`, `darkcraft:lanza_tam`: una lanza para el combate —`Armas.de`—, escala C/B con Fuerza y Destreza —`Escalado`—, 7
    de ataque; arte de `tam.py`, en la mano mas larga como la alabarda); logro `hedda_verdad` ("Lo que hizo"). La mision se da por
    acabada al sacar la lanza.
  - **la mentira** ("Murio intentando abrirla."; 10): se pone la bufanda, deja de esperar y **se apaga en paz** (en el cliente de quien
    se lo dijo: al cerrar la conversacion, 50 ticks con luz y almas y `sfx.hedda_descansa`); deja la **Bendicion de Hedda**
    (`Talisman.Clase.MADRE`: un golpe que deja por debajo de un cuarto de la vida, sin matar, cura un 30 % en el tick siguiente, una vez
    cada dos minutos; `Hedda.golpe` / `curar` desde `Misiones.onDano` y el tick del jugador); logro `hedda_mentira` ("Una mentira piadosa").
  - **callar** ("No lo encontre."; 11): ella seguira tejiendo y esperando (despues, 12); la bufanda se queda contigo como la **Bufanda
    de Tam** (`Talisman.Clase.BUFANDA`: cubrirse cuesta un 30 % menos de estamina —`Rol.guardia`— y el fuego hace un 20 % menos
    —`Resistencias`—); logro `hedda_callar` ("Aun espera").
- **Cada jugador ve lo suyo** (en un servidor con amigos el mundo es de todos): `Viajero.OCULTO` (lo pone `ClientMisiones`: la del
  camino se deja de ver tras la verdad o la mentira; la de rodillas solo tras la verdad), `isPickable` y `ViajeroRenderer.shouldRender`
  lo miran, `ClientPersonajes` no ofrece hablar con quien no se ve y el servidor no deja hablarle (`Misiones.visible`); la bufanda y la
  lanza de Tam se dejan de dibujar para quien ya las cogio (`RestosTam.sinBufanda` / `sinLanza` / `nada`).
- Se ponen una vez (`Morvain.Datos` "hedda", "tam" y "hedda_luto"; la de rodillas solo cuando llega a 40 alguien que le dijo la verdad).
  **`Misiones` ya lleva las dos** (`TODAS`, lo mandado por jugador es un `long` con las dos huellas; `estado`, `acabada` y la
  miniatura de cada una en `ClientMisiones`): la Historia del menu, la brujula y el mapa de la M las ensenan igual. Miniatura nueva
  (`CodiceScreen.MIN_MISION_HEDDA` = 17: la Puerta Oeste al ocaso con el humo y la vieja en su taburete, la labor roja cayendo al suelo).
- **Voces** (ElevenLabs, `eleven_v4`, flow `qTrFGBVJb5W5tYckIrim`): Hedda **"Harriet- Mature British Actress"**
  `aAsWcN5jdLdiYG7Hq0YL`, 14 frases (tomas a; `VOZ_HEDDA`: al aire libre, cerca). Efectos: `agujas` (1 y 4), `hedda_descansa` (4 y 2),
  `lanza_sacar` (1 y 2).
- Comandos `/darkcraft hedda [ir | tam | poner | olvidar]` y `/darkcraft misiones hedda <n>` (sus marcas). Sin abrir el juego,
  `tools/vista3d/vista.sh morvain_capital` marca donde va cada uno (Hd, Tm, Hl) y dice que hay debajo y alrededor.
- Pruebas: servidor, `misionHedda()` (linea `Morvain, la bufanda roja: ...`: los tres puestos —pisa, mira a la puerta, el farol, el
  torno a la espalda de Tam—, el dialogo sin nodos rotos y los cuatro caminos: la verdad —quien ve a quien, la de rodillas, la lanza y
  sus letras—, la mentira —la bendicion salva una vez y espera—, callar —la resistencia al fuego— y coger la bufanda sin conocerla);
  cliente, tramo `hedda()` despues de `oswin()`: Hedda de lejos y de cerca (`cliente_hd01`, `hd02`), hablar, el menu, aquella noche,
  aceptar y su cartel (`hd03..hd07`), la Historia (`hd08`), la brujula (`hd09`), Tam en la sala (`hd10`, `hd11`) y la bufanda cogida
  (`hd12`), volver con ella (`hd13`, `hd14`), la verdad (`hd15`), se va (`hd16`, `hd17`), de rodillas junto a Tam (`hd18`, `hd19`), la
  lanza (`hd20`, `hd21`, `hd22` en la mano), la mentira y se apaga (`hd23..hd25`) y callar (`hd26`, `hd27`).

### Plan grande, parte 7ai: las armaduras con modelo propio (los conjuntos de las clases)

Respuesta 111 del plan ("conjuntos con modelo 3D propio: yelmo, pecho, guantes, piernas"). Primera tanda: un conjunto por clase.
- **Conjuntos** (`armadura/Conjunto`, enum): `caballero`, `confesor`, `vagabundo`, `guerrero`, `barbaro`, `bandido`, `hechicero`,
  `profeta` y `prisionero` (el desdichado, nada). Cada uno: su defensa por pieza (puntos de armadura de Minecraft; el caballero 17 en
  total, el hechicero 7), su dureza y lo que aguanta los empujones (los de placas), lo que pesa entero (repartido 20/40/13/27 %:
  `REPARTO`) y lo que resiste entero de fuego, hielo, rayo, magia, veneno y sombra (cada pieza `1 - (1 - total)^reparto`: con las
  cuatro puestas, multiplicadas, da justo el total; lo fisico lo para la defensa). Su `ArmorMaterial`: no se rompe (durabilidad 0),
  no se encanta ni se repara; el sonido de ponerse es uno de los cuatro de ElevenLabs (placas, malla, cuero y pieles, tela).
- **Las piezas** (`armadura/PiezaArmadura`, un `ArmorItem`; objetos `darkcraft:<conjunto>_<yelmo|peto|guantes|grebas>`, 36, en
  `DarkCraftMod.ARMADURAS`; `DarkCraftMod.armadura(conjunto, pieza)`). **Los guantes van en el hueco de los PIES de Minecraft** (no
  hay hueco de manos; su modelo pone las cajas en los brazos) y las grebas llevan tambien las botas. La ficha (`lineas()`: el menu y el
  recuadro) dice "Defensa N · Peso N", lo que resiste y el conjunto y la descripcion; los "+N de armadura" de Minecraft no salen
  (`getDefaultTooltipHideFlags`: dirian "en los pies" para los guantes). Su textura: `getArmorTexture` →
  `textures/models/armor/<conjunto>.png`.
- **El modelo** (`client/armadura/ModelosArmadura`): una capa por conjunto y pieza (`darkcraft:armadura_<conjunto>#<pieza>`), la
  geometria de `ArmaduraGeometria` (GENERADA), horneada la primera vez que se dibuja (se vuelve a hornear al recargar los recursos:
  `EntityRenderersEvent.AddLayers`). `PiezaArmadura.initializeClient` da `ModelosArmadura.EXTENSION`, cuyo `getHumanoidArmorModel`
  mira el conjunto y la pieza en el objeto que se dibuja (**ojo: Forge llama a `initializeClient` desde el constructor de `Item`,
  antes de que se pongan los campos de la subclase**: en la build 199, con `conjunto.id` ahi, el cliente no arrancaba) y devuelve el
  `Modelo` (un `HumanoidModel` con las siete piezas del jugador y las cajas de la armadura colgadas de ellas, con piezas
  hijas con giro para cuernos, plumas, la punta del sombrero...). La capa de armadura de Minecraft le copia la postura del jugador (las
  poses del combate, beber, rodar...) y enciende solo las piezas del hueco (Forge copia esa visibilidad): `Modelo.renderToBuffer`
  las enciende todas (asi los guantes, en el hueco de los pies, se ven en los brazos, y el yelmo del confesor y el del vagabundo
  llevan su esclavina / camal en el cuerpo).
- **Hacerlos sin abrir el juego** (`tools/armaduras/armaduras.py [--vistas carpeta] [--solo conjunto]`): cada conjunto es una
  funcion que pone cajas (`Conjunto.caja(pieza, parte, x, y, z, w, h, d, material, crecer)`, medidas enteras; `lados(...)` la misma en
  los dos brazos o las dos piernas; `hija(...)` una pieza con giro) en el espacio de cada parte del jugador (la cabeza con el cuello en
  0, el brazo derecho de -2 a 10 en Y colgado de (-5, 2, 0), la pierna de 0 a 12) y sus pintores por material (los de
  `tools/enemigos/pinta.py` y los de aqui: `Pintor.placa` —acero con la luz de arriba a la izquierda y contornos, como el hierro de
  Minecraft—, `malla`, `cuero`, `tela`, `pelo`; lo transparente deja ver al jugador: la cara bajo la cofia, los ojos del bandido, el
  pecho del barbaro, la cabeza en la jaula). Para no pelearse con la piel del jugador: los yelmos crecen 0,6 o mas (la capa del
  sombrero de la piel crece 0,5), lo del cuerpo 0,5 o mas (la chaqueta, 0,25). Saca la textura, el icono de cada pieza (sus cajas
  dibujadas de tres cuartos y encajadas en 32 x 32 con contorno; el de los guantes, el derecho solo en diagonal), el modelo de objeto y
  `ArmaduraGeometria.java`; con `--vistas`, cada conjunto puesto en un maniqui con la piel de Steve (de frente, de espaldas, de lado
  y andando) con sus iconos, la textura en grande y `todos.png`.
- **Las clases empiezan con el suyo** (`Clase.kit`; la armadura del kit va en el orden cabeza, pecho, piernas, pies: el peto, las
  grebas y los guantes) y la pantalla de elegir clase los ensena puestos. Botin: la armeria de Valdrenn da piezas del caballero (y el
  peto del confesor y el del guerrero), las casas de Brumavela las del bandido y las buenas las del vagabundo (en vez de las de hierro,
  malla y cuero de Minecraft, que siguen existiendo).
- **Lo demas**: `Peso.item` (lo suyo), `Resistencias.de` (cada pieza puesta multiplica lo que queda), el menu (R) ensena los huecos
  como en Elden Ring: yelmo, peto, **guantes** (el hueco de los pies, con su silueta: `MenuTex.SIL_GUANTES` / `ICO_GUANTES` de
  `tools/ui/menu.py`) y grebas; su ficha es la de la pieza. Comando `/darkcraft armadura [<conjunto> [poner] | quitar]` (sin mas:
  los conjuntos y lo que se lleva puesto).
- Efectos (ElevenLabs, flow `HcCgofcQ0l53Gsm2ZI50`): `armadura_placas` (tomas 1 y 2), `armadura_malla` (2 y 1; la 3 es casi toda
  silencio), `armadura_cuero` (1 y 2), `armadura_tela` (1 y 4). **`procesar.py` numera las salidas por orden** (`_1`, `_2`...), no
  con el numero de la toma: en `sounds.json` van `armadura_tela_1` y `_2` (en la build 199 ponia `_4`, que no existe).
- Pruebas: servidor, `armaduras()` (linea `Armaduras: ...`: las 36 piezas bien —hueco, sin romperse, defensa, peso, sonido—, cada
  conjunto entero con su defensa, su peso y su fuego y su magia iguales al total, la armadura de cada clase y cuantas piezas dan 40
  cofres de la armeria y de las casas de Brumavela); cliente, tramo `armaduras()` despues de `clases()`: cada conjunto puesto en
  tercera persona de frente (`cliente_ar01_caballero` ... `ar09_prisionero`, lineas `armadura <conjunto>: ...` con lo que lleva, lo
  que resiste y los modelos horneados), el caballero de espaldas (`ar10`), el menu con sus cuatro huecos (`ar11`) y el estado con sus
  resistencias (`ar12`); y las de las clases (`cliente_cl01`, `cl02`, `cl05`, `cl06`) ya salen con su armadura.

### Plan grande, parte 7aj: los martillos y las flechas de fuego, de sangre y de escarcha

Respuesta 109 del plan ("armas: ... hachas (y martillos); arcos y ballestas (flechas de fuego, sangre, escarcha) ..."), primera
tanda (los escudos pequeno, mediano y grande van en la 7ak).
- **Los martillos** (`armas/Martillo`, un `SwordItem` de hierro con su `Armas.Tipo`): la **Maza de Clerigo** (`darkcraft:maza`,
  7 de ataque, una mano, escala C/D y **Fe C**, pesa 4,5), el **Martillo de Guerra** (`martillo_guerra`, 8, una mano, B/E, 5,5) y el
  **Gran Martillo de Piedra** (`gran_martillo`, 11, a dos manos, A/E, 14). Tipos nuevos del combate `MARTILLO` (golpes de arriba
  abajo y en diagonal, mazazo cargado, desequilibran mucho) y `GRAN_MARTILLO` (barridos y mazazos lentos y muy fuertes); su habilidad:
  el grito (una mano) y el sismo (el grande); valen el torbellino, el grito, el sismo y las de todas. Lo suyo (`Combate.pegar`):
  **+15 % a los que llevan armadura** (`Martillo.CONTRA_ARMADURA`, los mismos que suenan a metal) y **a quien se cubre con un escudo
  le rompen la guardia** (`romperGuardia`: un jugador no se puede volver a cubrir en 5 s —`disableShield`, como el hacha del juego—;
  un caballero zombi baja el escudo y no se cubre en 3 s —`CaballeroZombi.guardiaRota`—; el golpe entra entero; suena la guardia
  rota). Los escudos de los enemigos del mod tambien (`Martillo.rompe(fuente)`): el caballero de la guardia de Valdrenn recibe el
  golpe entero y se queda aturdido; el paves de Ser Brandt deja pasar la mitad (en el muro, la cuarta parte: `PAVES_MARTILLO`). Su
  golpe suena a martillo (`sfx.martillo_golpe`) y el fuerte levanta polvo. Sprites de 32 x 32: `tools/rol/martillos.py [--vista
  carpeta]`; el gran martillo y el de guerra, mas grandes en la mano (sus modelos).
- **Las flechas** (`armas/FlechaItem`, un `ArrowItem` en `#minecraft:arrows`: `darkcraft:flecha_fuego`, `flecha_sangre`,
  `flecha_escarcha`, en `DarkCraftMod.FLECHAS`) y la que vuela (`armas/FlechaEspecial`, una `AbstractArrow`, entidad
  `darkcraft:flecha_especial` con su clase sincronizada; `client/armas/FlechaEspecialRenderer` con
  `textures/entity/flecha_<clase>.png`, el reparto de la flecha del juego; rastro de llamitas, gotas o escarcha). Las tiran el arco y
  la ballesta de siempre; **el arco tira primero las de la mano izquierda** (como en Minecraft: se eligen poniendolas en uno de los
  tres huecos de la izquierda; lo dice su ficha). De fuego: va ardiendo y prende 6 s a quien da (`sfx.flecha_fuego`). De sangre: suma
  34 de **sangrado**. De escarcha: suma 34 de **congelacion**. Se recogen del suelo (las que dan en alguien se pierden). Sprites:
  `tools/rol/flechas.py [--vista carpeta]` (objeto 16 x 16 y entidad).
- **El sangrado** (`enemigos/Sangrado`, el de los penitentes de la 7s, para todos): 100 = hemorragia (15 % de la vida; **a los jefes
  —`Sangrado.jefe`: los guardianes, el Capitan, el Rey, Ser Gavran y los del juego— les entra la mitad y les quita el 5 %**); no
  sangran (`sinSangre`) los penitentes, los esqueletos (los arqueros y los del juego), los perros de hueso, los espectros, las
  gargolas, los calcinados, la Sombra ni el corcel. **La congelacion** (`enemigos/Congelacion`, igual): 100 = se congela (golpe de
  frio del 10 %, a los jefes la mitad de lo sumado y el 4 %; queda helado —`Helado.helar`—, lento 5 s y **6 s recibiendo un 20 % mas
  de todo**, `DEBIL`; `sfx.congelacion`); no se congelan los helados, la Sombra ni lo que el juego no deja (`canFreeze`). Lo sumado
  baja solo (a los 3 s, 2 por tick); descansar lo quita. El tajo sangriento suma 40 de sangrado y el tajo helado 30 de congelacion
  (`Combate.SANGRE_SUMA`, `ESCARCHA_SUMA`).
- **Las barritas** (`Net.Acumula` → `client/combate/Acumulados`, a los que ven a esa entidad): encima de cada uno con algo sumado
  (a menos de 22 bloques y a la vista) una barrita roja o azul palido con las esquinas de oro que baja sola como en el servidor y al
  reventar se llena y destella; las del propio jugador van bajo sus barras (y sus efectos) con una gota o un copo. Se proyectan como
  la marca de `Fijar` (`RenderLevelStageEvent`; con shaders tambien). `Net.V` = "26".
- **Donde salen**: Kerrick (la maza 1300, el martillo de guerra 1500, la ballesta 1100 —una de cada—, 5 de fuego 150, 5 de escarcha
  180), Silas (el gran martillo 4200; 5 de sangre 240, de fuego 170, de escarcha 200), la armeria de Valdrenn (los tres martillos y las
  tres flechas), el monasterio (escarcha; el bueno, la maza), la Tierra sin Fe (sangre; la buena, la maza), la Capital (fuego; la buena,
  el de guerra y el grande) y las paradas del camino de nubes (alguna de fuego).
- Comando `/darkcraft armas [martillos | flechas | sangre <n> | frio <n> | limpiar]` (`armas/Arsenal`; `sangre` y `frio` a lo que se
  tiene delante o a uno mismo). Efectos (ElevenLabs, flow `XeqoCBgCDlv3wBHDJFd8`): `martillo_golpe` (tomas 2 y 3), `flecha_fuego`
  (3 y 1; la 2 dura 5 s), `congelacion` (2 y 4).
- Pruebas: servidor, `armas7aj()` (linea `Armas 7aj: ...`: los tres martillos —tipo, letras, peso, ataque, habilidad—, la guardia con
  escudo contra la espada y el martillo, el paves de Brandt, Brandt es jefe y le entra la mitad, el arco y la ballesta tiran la de la
  mano izquierda, tres flechas de cada clase en un aldeano —arde; hemorragia a la tercera; congelacion a la tercera y un golpe de 5 le
  quita 6—, el arquero no sangra y el helado no se congela, las tiendas y el botin); cliente, tramo `armas7aj()` despues de las
  armaduras: el gran martillo cargando y su mazazo (`cliente_aj01`, `aj02`), el de guerra de lado (`aj03`), un caballero cubierto con
  el escudo (la espada no entra, el martillo le rompe la guardia: `aj04`), el arco tenso con la flecha de fuego de la mano izquierda
  y la flecha ardiendo (`aj05`, `aj06`), dos de sangre con su barrita y la hemorragia (`aj07`, `aj08`), dos de escarcha y la
  congelacion (`aj09`, `aj10`; linea `congelado: un golpe de 10 le quita 12`), las barritas del jugador (`aj11`), el inventario con las
  armas (`aj12`) y la ficha del gran martillo (`aj13`).

### Plan grande, parte 7ak: los escudos pequeno, mediano y grande

Respuesta 109 del plan ("... bastones, sellos y escudos (pequeno, mediano, grande)"); con la 7aj, la variedad de armas.
- **Los escudos** (`armas/Escudo`, un `ShieldItem` del juego: se alzan con el clic derecho y paran como el del juego; objetos
  `darkcraft:rodela`, `escudo_caballero`, `paves`, en `DarkCraftMod.ESCUDOS`; no se rompen) con su `Tamano`: **la Rodela de
  Hierro** (pequena y redonda: para el 75 % de cada golpe —el resto entra—, cubrirse cuesta el 115 %, pesa 2; **parada**: el
  golpe de cuerpo a cuerpo que llega en los primeros 9 ticks de estar cubierto se para entero, cuesta la cuarta parte y deja
  aturdido al que lo dio —los jefes no—, con el sonido y las chispas de la parada y el logro `parada`: `Escudo.parar`; cubierto
  con ella se anda a la mitad en vez de a la quinta parte: `EscudoCliente.Juego.andar`, `MovementInputUpdateEvent`), **el
  Escudo de Caballero** (de punta, rojo con la corona de oro: lo para todo, cuesta lo de siempre, pesa 4,5) y **el Paves de
  la Guardia** (hasta el suelo, granate con bandas de hierro negro y lomo de hierro: lo para todo y cuesta el 55 %, pesa 9).
  Lo hace `Rol.onEscudo` (`ShieldBlockEvent`: lo que para y la estamina, `Rol.guardia(jugador, golpe, mult)`); el peso,
  `Peso.item` (el escudo del juego sigue pesando 4). Un martillo (7aj) les rompe la guardia a todos (la espera es la del
  escudo que se lleve). El caballero y el confesor empiezan con el escudo de caballero y el barbaro con la rodela.
- **El modelo** (`tools/escudos/escudos.py [--vista carpeta]`): cada uno, cajas como el escudo del juego (la placa de un
  pixel de grueso en z -2..-1, hecha de franjas sin solaparse, y el asa en el mismo sitio que la del juego; la rodela con su
  cuenco y su punta, el paves con su lomo y el pincho del pie); el frente se pinta con coordenadas de todo el escudo (el aro,
  el filo, las bandas siguen la forma entera). Saca `textures/entity/escudos/<id>.png`, `client/escudos/<Nombre>Geometria.java`
  (GENERADO) y los modelos de objeto `<id>.json` / `<id>_blocking.json` (`builtin/entity`, las posturas del escudo del juego
  con su escala en la mano y en el menu). Lo dibuja `client/escudos/EscudoRenderer` (un `BlockEntityWithoutLevelRenderer` como
  el del juego: dado la vuelta en Y y en Z, `entityCutoutNoCull`, el brillo si esta encantado; los modelos se hornean la
  primera vez y al recargar los recursos), que da `EscudoCliente.EXTENSION` (la pide el objeto desde su constructor: no mira
  sus campos); la propiedad `blocking` (el modelo de cubierto) la registra `EscudoCliente.setup`.
- **Donde salen**: Kerrick (la rodela 800 y el escudo de caballero 1200, uno de cada), Silas (el paves 3000), la armeria de
  Valdrenn (los tres; el escudo del juego sale menos), la Capital buena (el paves) y las paradas del camino (alguna rodela).
- Pruebas: servidor, `escudos7ak()` (linea `Escudos 7ak: ...`: cada uno su peso, lo que para y lo que cuesta, que cubre y que
  no se rompe; un caballero zombi con cada uno, cubriendose: un golpe de 10 al instante —la rodela, PARADA y el que pega
  aturdido— y otro ya cubierto —la rodela deja pasar algo, los otros nada—; lo que cuesta cubrirse de un golpe de 6 con cada
  uno; los escudos de las clases; tiendas y botin); cliente, tramo `escudos7ak()` despues del de las armas: el escudo de
  caballero alzado en primera persona y de cara (`cliente_es01`, `es02`), la rodela (`es03`; lineas `escudos golpe rodela al
  instante: ... PARADA` y `rodela cubierto: -1,5 de vida`), el paves de cara y en primera persona (`es04`, `es05`) y los tres
  en el menu (`es06`).

### Plan grande, parte 7al: los conjuntos de los jefes

Respuesta 111 del plan (los conjuntos con modelo propio), segunda tanda: los de los jefes, que se ganan al vencerlos.
- **Los conjuntos** (en `armadura/Conjunto`, con su jefe: el ultimo dato, el id de entidad; `esDeJefe()`, `deJefe(id)`):
  `capitan` (la ropa del Capitan de Brumavela, Edrik Maroll: tricornio azul con galon de oro, casaca con botones y charreteras,
  faja roja, faldones, guantes y botas de vuelta, algo de alga; cuero, defensa 12, peso 9,5, hielo y veneno), `brandt` (la de Ser
  Brandt, el Muro del Rey: placas negras con filos de oro, yelmo con aro de clavos y puas y rendija roja, tabardo rojo con la torre
  negra, hombreras de dos lamas; defensa 21, dureza 2, la mas pesada: 29), `vareth` (la de corteza de Vareth: corteza con surcos y
  musgo, yelmo con astas de ramas y rendija verde, corazon verde en el pecho, garras de raiz, raices enroscadas; defensa 16,
  peso 16,5, nada de fuego —el fuego le hacia el doble— y mucho veneno), `rey` (la del Rey Corvin: placas negras con filos de oro,
  la corona de ocho puntas con su piedra, rendija de brasas, esclavina de armino, tabardo y capa carmesi; defensa 21, mucho fuego) y
  `gavran` (la del Juramento de Ser Gavran: acero negro viejo con musgo, corona de raices con espinas, rendija verde, tabardo con la
  espada del juramento, raices en las grebas; defensa 22, dureza 3, la mejor de todo). Sus piezas son epicas (rareza) y su ficha
  dice "Se la ganaste a <jefe>" (`darkcraft.armadura.jefe`). Las placas de los tres de placas las pone `_placa_fuerte` en
  `tools/armaduras/armaduras.py` (coraza, hombreras de dos lamas, brazales, guanteletes con su puno, escarcela, quijotes,
  rodilleras y grebas; cada uno sus colores y su yelmo).
- **Donde se ganan**: el Capitan y los guardianes de Valdrenn (Brandt y Vareth) las dan al repartir lo del jefe a los que
  pelearon y a los que estaban cerca (`PuebloEnemigos.repartir`, `Guardianes.repartir` → `Armaduras.premio` / `premioDeJefe`):
  las cuatro piezas a la bolsa con sus avisos de "conseguido", **una sola vez a cada jugador** (la ficha guarda
  `premios.armadura_<conjunto>`: `Ficha.premio`). El Rey y Ser Gavran, en el cofre de su capitulo (cada jugador su copia): la del
  Rey en la fila del medio (huecos 9, 11, 15 y 17) y la del Juramento a los lados del mandoble (2, 3, 5 y 6), sin pisar lo que ya
  habia.
- **Logro** `armadura_jefe` ("Ropa de Muerto": ponerse entero el conjunto de un jefe; `Armaduras.onEquipo`, con
  `LivingEquipmentChangeEvent`, que salta en el tick del jugador: los FakePlayer de las pruebas no lo tienen).
- Comando `/darkcraft armadura jefes [olvidar]` (los que se le han dado ya; olvidarlos para probar otra vez).
- Pruebas: servidor, `armaduras7al()` (linea `Armaduras de jefe 7al: ...`: cada uno con su jefe, su defensa, su dureza, su peso,
  su fuego y su veneno, entero; el premio del Capitan la primera vez si y la segunda no, Brandt y Vareth por su id, la guardia
  nada, lo que queda en la bolsa; los cofres del Rey y de Ser Gavran con sus cuatro piezas y cuantas cosas); cliente, tramo
  `armaduras7al()` despues del de los escudos: cada uno de frente en tercera persona (`cliente_al01_capitan` ... `al05_gavran`,
  lineas `armadura 7al <conjunto>: ...`), el Rey y el Capitan de espaldas (`al06`, `al07`), el premio del Capitan con sus avisos
  (`al08`; linea `armadura 7al premio del Capitan: primera true, segunda false, piezas en la bolsa 4 | logro armadura_jefe true`) y el
  menu con el equipo del Rey (`al09`).

### Lo anterior: parte 5 (torre nueva, camino de nubes y portal) — por sub-partes

Respuestas 3, 4, 27-68 y 81 de `PLAN.md`:
- **5a, la torre bajo la cupula**: hecha (arriba).
- **5b, la montana y el puente largo**: hecha (arriba). Los enemigos del sendero van con los de dentro (5c).
- **5c, dentro de la fortaleza y en la montana**: hecha (arriba).
- **5d, el camino de nubes**: hecha (arriba).
- **5e, el portal** (48-68): hecha (arriba: 5e-1 el portal nuevo; 5e-2 el capitulo 6 con la voz de la Sombra, el rayo
  rojo y el cielo rojo). Cruzar el portal llega con Morvain (parte 7).
- Despues, la **parte 6** (arriba) y la **parte 7**, Morvain, las Tierras Marchitas (respuestas 85-96 y 125-132 de
  `PLAN.md`), por sub-partes (la 7a, la 7b, la 7c, la 7d, la 7e, la 7f, la 7g, la 7h, la 7i, la 7j, la 7k, la 7l, la 7m, la 7n, la 7o, la 7p, la 7q y la 7r arriba; despues, region a region su
  contenido).

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
