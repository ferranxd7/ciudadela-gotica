# EL PLAN NUEVO (28-11-2026): la gran reforma (parte 8) — MANDA SOBRE LO DE ABAJO

El 28-11-2026 el usuario probo el mod (build 226) y pidio **rehacerlo casi todo**: un mapa "como Dios manda" (montanas
colosales, escaleras kilometricas, castillos en las cimas, llanuras con cosas, una isla con lagos, rios y cascadas del
cielo; estilo Terralith / Tectonic pero del mod), hierba con efectos al pasar, lluvia y nieve realistas, arboles "con mas
pixeles" (no cubos), **mas de 200 bloques propios** estilo Dark Souls / Elden Ring (fuera los mods de decoracion; Valdrenn,
la Fortaleza de Hueso y Brumavela con bloques del mod), estructuras enormes con muchos enemigos (ciudades, pueblos,
catacumbas, castillos, la universidad de magia, el pantano), **jefes rehechos** "que sean personas" con animaciones muy
curradas y fuego propio (no el de Minecraft), hadas y fuegos de colores, y **cadenas de objetos** para entrar a cada jefe
(como las piezas del barco, pero mas largas). Fallos que vio: no le salio la pantalla de la clase (ni arma ni cinematica)
y los sonidos y la musica con poco detalle. Despues de la reforma se sigue con lo que quedaba (parte 7 y traducciones).
Contesto 95 preguntas (abajo, tal cual; en blanco = lo decido yo).

## Hoja de ruta de la reforma (parte 8, cada sub-parte publicada con `[publicar]`)

1. **8a, arreglos urgentes**: la pantalla de la clase sale siempre al empezar; **Nueva partida propia** (solo el nombre
   del mapa y la dificultad, sin modos de juego); sonidos y musica con mas detalle; el instalador sin los mods de
   decoracion; la lejania de Distant Horizons por debajo de 256.
2. **Los bloques nuevos** (mas de 200, texturas de 64 x 64, todas las formas de cada material, cero bloques de Minecraft),
   por tandas: piedras y sillares de cada region; maderas, tejas, hierro negro y oro, vidrieras; decoracion (velas,
   candelabros, cadenas, estandartes, tapices, estatuas, gargolas, ataudes, libros, muebles con modelo); el terreno (tierra,
   hierbas que se doblan, flores, musgo, arena, nieve que se acumula, hielo, roca de cada region) y el **agua propia**;
   bloques vivos, contenedores (arcones, atriles, puertas, escaleras) y cosas que se rompen.
3. **El clima y los efectos**: lluvia, nieve, tormentas, niebla, viento con cosas que vuelan, fuegos de seis colores
   realistas, hadas, espiritus, cuervos, luciernagas, arboles con caras, estatuas que lloran. **Los arboles por piezas**.
4. **El terreno nuevo de Morvain** (isla de 24.000, doce regiones con sus zonas, montanas de 1.500-2.000, escaleras
   talladas, rios con canones, lagos, islas flotantes con cascadas, cuevas enormes, algo cada 50 bloques en las llanuras) y
   **el mundo de las nubes con los bloques nuevos** (mismo diseno, mas detalle, todo amueblado).
5. **Region a region**, empezando por las Llanuras Palidas como muestra (que el usuario la vea y corrija): estructuras
   (castillos del tamano de Valdrenn, ciudades, pueblos, catacumbas, monasterios, torres de mago, puentes, presas,
   coliseos, carceles; todo amueblado, ~80 enemigos, atajos y secretos), sus enemigos rehechos, su jefe rehecho (mucho mas
   detalle, fases, cinematica y muro de niebla, arena enorme), la cadena de ~10 pasos para llegar a el, mas de cinco
   misiones secundarias y las criaturas nuevas. Mas de 30 jefes en total.
6. **Sonido**: musica propia orquestal con coros (ElevenLabs: hace falta el conector completo,
   https://api.elevenlabs.io/v1/mcp), ambiente por zona, pasos segun el suelo. **Shader** de licencia abierta adaptado a
   los bloques del mod.
7. Despues, lo que quedaba del plan de abajo (mazmorras que falten, jefes secretos, traducciones).

## Las 95 preguntas y sus respuestas (28-11-2026)

A. Lo que se rehace y en que orden
1. Que se rehace -> **Morvain y el mundo de las nubes** (Brumavela, Valdrenn, la Fortaleza de Hueso y el camino).
2. Los sistemas (rol, combate, menus, HUD, santuarios, frascos, corcel, forja, clases, dialogos) -> **se quedan, pero mejorados**.
3. Lo hecho sin publicar (7av Maese Galen, 7aw el Fuerte, 7ax el Castellano) -> **guardarlo y rehacerlo** con lo nuevo.
4. Como publicar durante la obra -> **por partes** (aunque Morvain quede a medias un tiempo).
5. -> **Solo mundos nuevos**.
6. Orden (bloques, mapa, estructuras, pueblos y castillos, jefes) -> en blanco (decido yo: el de la hoja de ruta).
7. Region de muestra primero -> en blanco (decido yo: las Llanuras Palidas).
8. Personajes y misiones de ahora (Maelis, Oswin, Hedda, Ylva, Corwen, Sigrun, herreros, Silas) -> en blanco (se quedan y se mejoran).
9. Duracion del juego -> **80-120 horas**.
10. Que pesa mas -> **el detalle**.

B. El mapa de Morvain
11. Tamano -> **24.000 x 24.000**.
12. Forma -> **una gran isla**, costa irregular y mar alrededor.
13. "No recta" -> **los bordes del mapa**.
14. Altura de las montanas -> **de 1.500 a 2.000 bloques**.
15. Escaleras kilometricas -> **talladas en la roca** (cuantas y a donde: decido yo).
16. En las llanuras -> **ruinas, torres de vigia, carros y caravanas, estatuas, campamentos, molinos, rocas grandes,
    arboles sueltos y flores**, **algo cada 50 bloques**.
17. Rios -> **anchos y hondos, canones, puentes de piedra, molinos y cascadas**.
18. Lagos enormes -> **de tres a cinco**.
19. Cascadas del cielo -> **de islas flotantes y de la ciudadela encadenada**.
20. "Menos seco" -> **mezcla de zonas verdes y vivas con lo marchito**.
21. Regiones -> las nueve de ahora **mas un bosque vivo, un gran lago y un desierto de ceniza**.
22. Zonas dentro de cada region (valles, mesetas, gargantas, prados) como Terralith -> **si**.
23. Bajo tierra -> **cuevas enormes ya** (cavernas, lagos y raices gigantes).
24. Dia y noche y clima por region -> en blanco (decido yo).

C. Hierba, arboles, clima y efectos
25. La hierba -> **se dobla y se aplasta al pasar, y suena**.
26. Que se mueva con el viento -> **si** (sin shaders, texturas animadas).
27. Arboles -> **de los tres tamanos**: normales (8-15), grandes (20-40) y gigantes (60-100).
28. Arboles con piezas propias (ramas de 2 a 14 px que se curvan, raices, racimos de hojas) -> **asi vale**.
29. Texturas del mod -> **64 x 64**.
30. Lluvia -> **salpicaduras, charcos y niebla**.
31. Nieve -> **copos de varios tamanos, ventiscas, se acumula en suelo y tejados, huellas**.
32. Tormentas -> **montanas, pantano y Tierra sin Fe**.
33. Niebla -> **solo en el pantano y los valles**.
34. Viento visible -> **hojas, ceniza, polvo y petalos**.
35. Fuegos de colores -> **los seis**: azul (almas), verde (veneno), morado (magia), rojo (sangre), dorado (sagrado),
    blanco (escarcha). La luz no tiene color: el color va en la llama, las chispas y el resplandor.
36. Las hadas -> **luces de ambiente y guias** (el aspecto lo decido yo).
37. Mas fantasia oscura -> **espiritus que vagan, cuervos, luciernagas, arboles con caras, estatuas que lloran**.

D. Los bloques nuevos
38. Bloques de Minecraft -> **cero** (solo lo que no se puede quitar).
39. Agua -> **propia** (color por region, mas oscura, espuma).
40. Que no puede faltar -> **todo**: sillares y piedras, columnas y molduras, gargolas, tejas, vidrieras, maderas viejas,
    hierro negro y oro gastado, huesos y calaveras, velas y candelabros, cadenas, estandartes y tapices, estatuas,
    ataudes, libros y estanterias, muebles.
41. Formas de cada material (escaleras, losas, muros, columnas, arcos, cornisas) -> **si, todas**.
42. Muebles -> **con modelo propio**.
43. Cosas que se rompen en combate y sueltan objetos -> **si**.
44. Bloques "vivos" -> **todos**: raices que se mueven, ojos en las paredes, carne, cristales que brillan, sangre que gotea.
45. Instalador -> **solo Dark Craft y el shader** (fuera los mods de decoracion).
46. Valdrenn, la Fortaleza de Hueso y Brumavela -> **mismo diseno con los bloques nuevos y mas detalle**.
47. Interiores -> **todo amueblado**.

E. Estructuras y ciudades
48. Castillo grande -> **como Valdrenn** (~450 bloques).
49. Estilo -> en blanco (gotico europeo, Stormveil / Leyndell).
50. Castillos grandes en Morvain -> **de 6 a 10**.
51. Ciudades ademas de la Capital -> **una o dos**, con **calles y plazas, mercado, catedral, puerto, alcantarillas,
    barrios ricos y pobres**.
52. Pueblos -> **dos o tres por region**.
53. Casas -> **entramado, piedra, tejados de tejas, chimeneas, balcones y sotanos**.
54. Catacumbas -> **varias plantas, laberintos, trampas y osarios**.
55. La universidad de magia (tal cual): "Como la de elden ring una gran escuela situada en un estilo pantano pero con agua
    cristalina pero que se pueda andar normal situada en medio de ese sitio en una montaña muy alta pero sin pasarse unos
    200 bloques o algo asi estilo capilla con universidad."
56. El pantano (tal cual): "Arboles que lo flipas de distintos tamaños que cueste mas andar por ahi con muchos detalles de
    musgo y demas."
57. Otras estructuras -> **monasterios, torres de mago, puentes enormes, presas, coliseos y carceles**.
58. Ruinas y entero -> **casi todo entero**.
59. Atajos que se abren -> **si**.
60. Enemigos en una estructura grande -> **unos 80**.
61. Secretos (muros ilusorios, cofres, llaves, pasadizos) -> **en todas**.

F. Jefes y enemigos
62. "Que sean personas" -> **estilo Minecraft con muchisimo mas detalle** (dedos, pelo, telas, placas de armadura,
    texturas de mas resolucion).
63. Tamano -> **segun el jefe**.
64. Animaciones -> **mas fluidas y rapidas, capa y pelo que se mueven, respiracion, golpes que se ven venir**.
65. Fases -> **se transforma y cambia la arena**.
66. -> **cinematica de entrada y muro de niebla** para cada jefe.
67. Arenas enormes pensadas para la pelea -> **si**.
68. Los jefes de ahora -> **rehacerlos todos**; en total **mas de 30**.
69. Los enemigos normales -> **se rehacen todos**.
70. Fuego propio -> **mas realista** (llamas suaves con resplandor).
71. Criaturas nuevas -> **bestias, golems, aranas gigantes y arboles vivos**.
72. Espiritus que se invocan para ayudar -> **si**.
73. Dificultad -> **igual**.

G. Misiones y objetos para entrar en las zonas
74. Cadena de objetos -> **para cada jefe**, de **unos 10 pasos**.
75. Tipos de pasos -> **objetos escondidos, puzles, personajes que piden cosas y llaves de otros jefes**.
76. Misiones secundarias -> **mas de cinco por region**.
77. Pestana de misiones -> **no**: se descubre sin ayuda.
78. Marcas de objetivos -> **sin marcas**.
79. Recompensas -> **armas unicas, armaduras, hechizos, talismanes y cenizas**.

H. Sonido y musica
80. Que se oye peor -> **musica, voces y efectos**: "No se escuchan mal del todo pero sube un poco el detalle de los sonidos."
81. -> **altavoces** (volumenes sin decir).
82. Musica -> **propia con ElevenLabs** (conector completo), **orquestal con coros**.
83. Al explorar -> **silencio con sonidos de ambiente**.
84. Ambiente por zona y pasos segun el suelo -> **si**.
85. Las voces -> **le gustan**.
86. Mas calidad aunque el mod pese mas -> **si**.

I. Distant Horizons, shaders y su PC
87. PC -> **RTX 5060 Ti 16 GB, Ryzen 5 5600X, 32 GB DDR4 3200**.
88. Lejania -> **menos de 256 chunks**.
89. Shader -> **buscar otro de licencia abierta** que se pueda adaptar al mod.
90. -> **juega solo** (lo del servidor dedicado sigue valiendo).
91. -> **no le importa que el mundo tarde en generarse**.

J. Lo que probo
92. Empezo **en supervivencia** y **no vio la cinematica** (no le salio la pantalla de la clase).
93. (tal cual) "Nada en las manos me aparecía como se tuviera la espada. / Mira quiero que no dejes lo de supervivencia y
    eso quiero que lo quites todo y quiero poder poner la dificultad y el nombre del mapa." -> **Nueva partida propia**:
    solo el nombre del mapa y la dificultad; nada de modos de juego.
94. -> nada mas.
95. Parecido a -> **Liurnia, Caelid y Anor Londo**.

---

# Dark Craft — el plan grande (24-11-2026)

Lo que pidio el usuario el 24-11-2026 y sus respuestas a 138 preguntas. **Es la referencia de diseno**: antes de
empezar cada parte, leer su apartado aqui. El mod pasa de "anadido a Minecraft" a **aventura completa estilo Elden
Ring / Dark Souls** con su propio mundo, interfaz y sistemas.

## Hoja de ruta (cada parte, y cada sub-parte grande, publicada con `[publicar]`)

1. **Arreglos rapidos** sobre el mod actual (respuestas 5-9, 46b/49, 92, 137).
2. **Interfaz nueva y sistemas de rol** (17, 21-26, 94-120, 131, 133, 136).
3. **Mundo de nubes, isla del pueblo y barco** (69-80, 121-124).
4. **Valdrenn dificil** (10-20).
5. **Torre nueva, camino de nubes y portal** (3, 4, 27-68, 81).
6. **Distant Horizons, Oculus y Embeddium dentro del mod** (71, 72, 82-84, 134, 135).
7. **Morvain, las Tierras Marchitas**, por regiones (85-96, 125-132).

El recorrido del jugador: elegir clase -> cinematica -> isla del pueblo abandonado (5 piezas del barco, el Capitan)
-> barco volador (cinematica) -> Valdrenn (enemigos, mini-jefes, Rey, Ser Gavran, llave de hueso) -> camino de nubes
-> la torre con la cupula de cristal -> amuleto -> portal (rayo rojo, nubes rojas) -> Morvain -> la Sombra en el centro.

## Las respuestas (tal cual, numeradas como las preguntas)

1. Orden: arreglos -> fortaleza/portal/voz/animacion -> Distant Horizons -> dimension por partes.
2. Publicar cada parte en cuanto este.
3. La fortaleza vieja se rehace sola al acercarse (se pierde lo que hubiera puesto alli).
4. Al rehacerla empieza de cero y se le devuelve la llave de hueso (los logros se quedan).
5. Pausa tras cada frase de jefe: 0,3 s (antes 1,2).
6. Subtitulos: 0,8 s tras acabar la voz (antes 2,75).
7. La pausa se nota rara en todo: entrada del Rey, entrada de Gavran, cambios de fase y muertes, frases de pelea.
8. Tras la ultima frase de la entrada el jefe ataca nada mas acabar.
9. Llave invisible: la Llave del Arbol en la mini cinematica de la puerta de la torre (no se ve girar).
10. Valdrenn: muchisimos enemigos (Dark Souls): cada patio y pasillo con guardias.
11. Enemigos: mas caballeros zombi (3 tipos), arqueros esqueleto en murallas/torres/balcones, enemigos nuevos
    (perros de hueso, espectros de la corte), capitanes de elite al mando de cada grupo.
12. Donde: puente y entrada, pueblo/patios/calles, pasillos y salas del castillo, torre del arbol y antesalas.
13. Reaparecen solo si mueres o descansas en un punto de descanso (Dark Souls).
14. Emboscadas: unas pocas (4-5 sitios clave: puente, plaza, escalera del castillo...).
15. Mini-jefe guardian antes de CADA jefe (salon del Rey y torre del arbol), con barra de vida y logro.
16. Tras derrotar al Rey y a Gavran: menos enemigos (reino mas tranquilo).
17. Botin -> SISTEMA DE MONEDAS (idea del usuario): contador abajo a la derecha, muy currado; suma al matar enemigos,
    jefes y demas. Al morir conservas todo el equipo y las monedas se quedan donde moriste en una ESPADA DE LUZ clavada
    en el suelo (estilo Elden Ring); al acercarte sale encima de la barra de objetos, no muy grande, "Pulsa [E]" (la E
    como boton en pixel art) para recuperarlas.
18. Puntos de descanso: en Valdrenn 3 (hogueras de almas); para los demas sitios ya se vera.
19. Dureza de los enemigos normales: como ahora (la dificultad es que hay muchos mas).
20. Con amigos: los mismos enemigos pero con mas vida.
21. La moneda se llama "Ecos de la Sombra" (fragmentos rojos oscuros, a juego con el Espejo).
22. Si mueres otra vez antes de recoger la espada: los ecos de la vieja se pierden para siempre.
23. Conservar equipo + espada de luz: en TODO el juego (tambien el mundo normal).
24. Los ecos sirven para: subir de nivel (en puntos de descanso), comprar a mercaderes, mejorar armas y armaduras
    (herrero), aprender habilidades.
25. Contador de ecos: siempre visible (marco estilo Espejo, icono + numero, sube contando con brillo y "+120").
26. Junto a la espada de luz la E recoge los ecos en vez de abrir el inventario (aviso con boton E pixel art).
27. Fortaleza: una gran torre en el centro, su patio alrededor, todo bajo una cupula de cristal enorme que la torre atraviesa.
28. Cupula: cristal ahumado oscuro con paneles rojos y nervios de hueso y hierro negro.
29. Cupula rota en parte (paneles caidos, cristales en el patio, entran lluvia y enredaderas).
30. Torre alta, ~70 bloques.
31. Fortaleza ~90x90.
32. Entrada: arco gotico alto con dos estatuas de caballeros encapuchados y la reja de hueso (sin calavera).
33. Murallas en ruinas alrededor (anillo bajo con tramos caidos y alguna torrecilla rota).
34. Materiales: piedra oscura de Valdrenn + hueso tallado + musgo y enredaderas.
35. Sigue siendo una isla flotante sobre las nubes (misma altura), mas grande y mejor hecha.
36. SUBIDA NUEVA: un puente largo de hueso y nubes que la llave tiende desde la cima de una montana cercana.
37. La montana la crea el mod junto a la fortaleza: roca oscura, sendero en zigzag, restos de torres, enemigos por el
    camino y un mirador en la cima con el arco donde se usa la llave. (Ya no hay escalera de caracol ni corriente.)
38. Patio bajo la cupula: jardin muerto (arboles secos, rosales marchitos, fuente rota, bancos) y estanques con
    nenufares donde cae la lluvia por los huecos.
39. Interior medio derrumbado (suelos con agujeros, vigas y columnas caidas, escombros, algun techo abierto; recorrible).
40. Vegetacion: enredaderas y lianas, musgo y hierba, hongos y flores que brillan (NO raices/arboles dentro).
41-42. Salas: capilla, armeria y sala de guardias, mazmorras (sotanos de la isla), observatorio (arriba), aposentos.
    (No biblioteca, no comedor, no laboratorio.)
43/45/50. Para subir al portal: ASCENSOR como el del arbol de Valdrenn (funciona desde el principio, sin repararlo),
    pero con otro color y forma (a mi criterio).
44. Trampas: unas pocas (suelo que cede, pinchos de hueso, flechas).
46. Secretos: un libro perdido extra muy escondido + sala secreta con tesoro tras un muro falso.
46b/49. QUITAR los carteles de madera de junto a los cofres de los capitulos (tambien en mundos de antes) y marcar el
    cofre de otra forma (brillo o particulas suaves).
47. Enemigos dentro: caballeros zombi + espectros nuevos que aparecen y desaparecen.
48. Portal grande: 9 de ancho x 13 de alto.
51. Forma del portal: arco gotico apuntado con marco labrado.
52. Bloques nuevos para el marco: piedra de sombra con runas rojas que laten, hueso labrado, hierro negro y cadenas,
    gemas rojas que se encienden al abrirse.
53. Superficie: remolino rojo y negro que gira hacia el centro (un solo dibujo grande, no por bloque).
54. Portal en lo alto de la torre, a cielo abierto, por encima de la cupula.
55. Alrededor: estatuas encapuchadas gigantes mirando al portal + pilares con runas y cadenas que sujetan el marco
    (se rompen al abrirse).
56. Amuleto: pedestal tallado con su hueco; el amuleto se queda colocado, flota y gira.
57. Voz de la Sombra: masculina, grave y susurrante (ElevenLabs).
58. La voz exacta la elijo yo directamente (si no le gusta, se cambia).
59. Efectos: eco de vacio largo + susurros por debajo que repiten sus palabras.
60. Texto del portal reescrito, mas amenazante (agradece que le abras la puerta, invita a cruzar; pensado para el rayo y
    las nubes rojas).
61. Capitulos de la dimension nueva: UN NARRADOR NUEVO (personaje de la dimension, otra voz de ElevenLabs). Aldous
    (voz de los libros) se queda para los capitulos 1-5; el 6 lo dice la Sombra con su voz nueva.
62. Cinematica del portal: ~1 minuto.
63. Rayo: las nubes se arremolinan, cae un rayo rojo gigantesco sobre el portal y queda una columna de luz roja al cielo.
64. Nubes rojas en todo el mundo.
65. Duran hasta vencer al jefe final (la Sombra) de la dimension.
66. Ademas: cielo y niebla rojizos + luna de sangre.
67. Con amigos: TODOS ven la cinematica entera, esten donde esten.
68. El portal se cruza directo; el jefe de la fortaleza se anade mas adelante.
69. Los monstruos de Minecraft NO existen nunca (desde ya): solo los enemigos del mod en sus sitios.
70. !!! CAMBIO GRANDE: "el mundo normal no existira". Ni Nether ni End. Apareces en una ISLA CON UN PUEBLO donde
    tienes que REPARAR UN BARCO con piezas que vas encontrando por el pueblo (y armadura y demas); con el barco llegas
    a VALDRENN; en Valdrenn, al tener la llave de hueso, se abre el camino a la torre siguiente (la fortaleza) con un
    CAMINO DE NUBES currado. (Sustituye a la montana + puente de la 36/37.)
71. Distant Horizons DENTRO del .jar de Dark Craft (con su licencia LGPL y creditos).
72. Su PC es potente (grafica dedicada buena, 16 GB+).
73. Mundo NUEVO obligatorio; los mundos viejos NO hace falta que sigan funcionando (se puede quitar codigo viejo).
74. Entre las islas: un MAR DE NUBES sin fin (no hay suelo; todo son islas flotantes).
75. El barco VUELA: al repararlo se eleva y navega por el cielo hasta el muelle de Valdrenn.
76. El viaje es una CINEMATICA (luego se puede ir y volver desde el muelle).
77. Isla del pueblo: pueblo ABANDONADO con enemigos (nadie vivo: ruinas, notas y enemigos).
78. 5 piezas del barco, cada una distinta: vela, timon, mastil, ancla y el corazon que lo hace volar (cofre, tras
    enemigos, acertijo, [nota/cadaver en vez de vecino] y mini-jefe).
79. MODO AVENTURA: no se rompen ni se ponen bloques; todo el equipo se encuentra, se compra o se mejora.
80. Equipo: cofres, enemigos y herrero (comprar con Ecos, mejorar). Comida: pesca, huertos, mercader.
81. Camino de nubes Valdrenn -> torre: largo, a pie, con paradas (islotes con ruinas, enemigos y algun cofre).
82. Distant Horizons: 256 chunks por defecto.
83. Juega con shaders con OCULUS (DH tiene que ir bien con Oculus).
84. Con amigos usan SERVIDOR DEDICADO (Aternos, exaroton u otros) -> todo tiene que funcionar en servidor dedicado.
85. La dimension se llama "Morvain, las Tierras Marchitas".
86. Mapa de 16.000 x 16.000 bloques.
87. El mismo mapa para todos, disenado a mano.
88. Referencia visible desde todo el mapa: un CASTILLO ENCADENADO EN EL CIELO (ciudadela flotante sujeta por cadenas enormes).
89-90. Regiones: Llanuras palidas (llegada), Bosque muerto, Pantano podrido, Montanas nevadas, la Capital en ruinas,
    la Academia espectral, Costa de los huesos, "La tierra sin fe" (todo marchito, rios de sangre). Montanas y todo
    ULTRA MEGA GRANDE, colosal. Mucho mas adelante: zona del cielo y zona de las profundidades.
91. Cielo segun la zona; de normal oscuro con un poco de rojo (muy poco) y dorado.
92. Moverse: montura espectral, santuarios con teletransporte, fragmentos de mapa. Y QUITAR LAS COORDENADAS del espejo.
93. Avance: mundo abierto con zonas cerradas por jefes/llaves.
94. Objetos magicos: armas, talismanes, hechizos... HUD NUEVO: arriba a la izquierda vida, mana y estamina (rojo, azul,
    verde). "Inventario" abajo a la izquierda como Elden Ring/Dark Souls. El espejo REDONDO (no ovalado). La M abre SOLO
    el mapa. ESC o R abre una interfaz estilo Elden Ring (talismanes, armas, objetos...) hecha por el mod, no vanilla;
    logros e historia ahi; la interfaz de los libros distinta.
95. Personajes: guia en los santuarios, personajes con misiones, mercaderes ambulantes, herrero en un refugio y VARIOS
    herreros por el mapa con animacion currada golpeando el yunque. Interfaces de personajes/mercaderes en pixel art
    currado. Modelos de personajes currados. "Tirate el tiempo que haga falta."
96. TODO con voz de ElevenLabs y efectos de sonido. La interfaz de las cinematicas: en vez de bandas negras con linea
    roja, marco pixel art (esos colores + un poco de dorado). Dialogos: caja abajo, pulsar E con boton E pixel art;
    todos los botones con ese estilo.
(El usuario invita a 30-40 preguntas mas.)
97. ORDEN NUEVO: 1) arreglos rapidos; 2) interfaz nueva y sistemas (barras, Ecos, espada de luz, menu, equipo);
    3) mundo de nubes con la isla del pueblo y el barco; 4) Valdrenn dificil; 5) torre, camino de nubes y portal;
    6) Distant Horizons; 7) Morvain por regiones.
98. El inventario de Minecraft DESAPARECE: inventario propio por categorias (armas, armaduras, talismanes, hechizos,
    objetos, materiales, objetos clave); nunca se ve la pantalla de Minecraft.
99. La R abre el menu del mod; ESC sigue siendo la pausa de Minecraft.
100. Sistema de rol COMPLETO como Elden Ring: 8 atributos, armas que escalan con letras (S, A, B...), peso del equipo
    que cambia como ruedas, resistencias.
101. Sin corazones, sin hambre, sin comida. Pociones en pixel art: rojas = vida, azules = mana. Hay FRUTOS que dan
    habilidades/resistencias (menos dano de fuego, de congelacion, etc.).
102. La estamina la gastan: atacar, esquivar y saltar, correr, bloquear con escudo (si se acaba, rompen la guardia).
103. Esquiva: voltereta con una tecla, con instante invulnerable; mas lenta con mucho peso.
104. Primera persona + tecla de fijar objetivo (con marca).
105. Abajo a la izquierda: cruz de 4 huecos (arriba hechizo, derecha arma, izquierda escudo/segunda mano, abajo objeto
    rapido); se cambia con teclas o rueda entre lo equipado en cada hueco.
106. Frascos rojos y azules que se rellenan al descansar en un santuario; mejoras para llevar mas y que curen mas.
107. Talismanes: empiezas con 1 hueco y llegas a 4 venciendo jefes.
108. Hechizos: se memorizan en un santuario (huecos segun el atributo Mente), se lanzan con baston o sello en la mano;
    tecla para elegir hechizo; gastan mana.
109. Armas: espadas, dagas y mandobles; lanzas, alabardas, hachas (y martillos); arcos y ballestas (flechas de fuego,
    sangre, escarcha); bastones, sellos y escudos (pequeno, mediano, grande).
110. Cada arma con habilidad especial que se puede cambiar con "cenizas" en el herrero. Y COMBATE COMO EPIC FIGHT.
111. Armaduras: conjuntos con modelo 3D propio (yelmo, pecho, guantes, piernas).
112. Al empezar: pantalla pixel art para elegir CLASE (Caballero, Vagabundo, Hechicero, Bandido, Desdichado...).
113. Combate: sistema PROPIO del mod (cadena de golpes ligeros, golpe fuerte cargado, ataque corriendo y en salto por
    tipo de arma; personaje animado). No se incluye Epic Fight.
114. Vista: las dos (primera y tercera persona con F5), golpes animados para verse bien en ambas.
115. Menu (R): paneles de piedra oscura con marcos dorados gastados, detalles rojos, iconos pixel art grandes y tu
    personaje girando a un lado.
116. Libros de la historia: codice iluminado a pantalla completa en pixel art (pergamino oscuro, letra capital,
    ilustracion de cada capitulo, pasar pagina con animacion).
117. Dialogos: retrato pixel art, nombre, texto que se escribe al ritmo de la voz, [E] para seguir y a veces 2-3
    respuestas que cambian la mision.
118. Letra gotica pixel art propia para titulos/nombres/carteles; texto largo con la letra de Minecraft.
119. Carteles grandes: HAS MUERTO, jefe derrotado, zona descubierta, santuario encendido / ecos recuperados.
120. Espejo redondo arriba a la derecha (sin coordenadas) + brujula fina arriba en el centro (norte, marcas, santuarios).
121. Inicio: tras elegir clase, cinematica corta con voz (quien eres, caes del cielo) y despiertas en el borde de la
    isla junto a los restos del barco.
122. Eres un MARCADO POR LA SOMBRA: deberias estar muerto; la Sombra te trajo de vuelta para que le abras la puerta
    (por eso no mueres del todo y dejas tus Ecos en la espada de luz).
123. Enemigos de la isla del pueblo: aldeanos huecos (horcas, hoces, antorchas), marineros ahogados (sables, arpones,
    cerca del muelle) y el mini-jefe EL CAPITAN (guarda el corazon del barco; primer jefe con barra). (Sin perros/ratas.)
124. Dificultad: dura pero justa (Souls en normal), ajustable con la dificultad de Minecraft.
125. Morvain: MAS DE 10 jefes principales (varios por region) + la Sombra en el CENTRO del mapa como jefe final.
126. Narrador nuevo de los capitulos de Morvain: LA GUIA de los santuarios (voz de mujer serena; tambien te sube de nivel).
127. Servidor con amigos: mundo compartido (jefes/puertas para todos); Ecos, nivel, equipo y botin por jugador.
128. Idiomas: cada parte sale en ESPANOL E INGLES; ja/fr/de/pt se anaden despues en tandas.
129. Montura: corcel de hueso y niebla; la guia te da el silbato en el primer santuario de Morvain; corre, doble salto,
    se ataca montado, si lo hieren mucho se deshace un rato.
130. Santuarios en todo el juego: hogueras de almas (fuego azul palido en brasero de piedra y hueso) con MODELO PROPIO
    currado (no la fogata de Minecraft); la guia sentada al lado en los importantes.
131. Mazmorras: criptas de huesos, minas y tuneles, torres de mago, fuertes y campamentos, iglesias y ruinas, pueblos
    derrumbados. MENU: un apartado "Mods"; TODA la interfaz es del mod (nada de pantallas de Minecraft): graficos,
    packs de texturas, shaders, mods como Distant Horizons, sonido, salir... Todo pensado para shaders con Oculus.
132. Especiales: dragon, jinete nocturno (solo de noche, en caminos), gigantes errantes, jefes secretos tras muros ilusorios.
133. Tambien pantalla de TITULO propia (fondo animado, logo, musica; Nueva partida, Continuar, Multijugador, Opciones,
    Salir) y los mundos como partidas guardadas (clase, nivel, zona).
134. Oculus + Embeddium dentro del .jar si sus licencias lo permiten (si no, los baja el instalador).
135. Shader: el que mejor quede con Distant Horizons (lo elijo yo; si su licencia no deja incluirlo, lo baja el instalador).
136. 10 clases iniciales como Elden Ring.
137. Parte 1 (publicar ya): pausas 0,3 s y subtitulos 0,8 s, Llave del Arbol visible en la puerta, cofres sin cartel
    (con brillo), espejo sin coordenadas, escalera de la fortaleza actual sin atascos.
138. Nada mas que anadir: empezar.
