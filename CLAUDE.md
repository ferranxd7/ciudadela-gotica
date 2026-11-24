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
  `Estado.parche`). La prueba del cliente lo comprueba (`comprobarEscalera`).

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
- **La cruz** abajo a la izquierda (`client/ui/Cruz`): arriba hechizo (aun vacio), derecha arma, izquierda la otra mano,
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
