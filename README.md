# Dark Craft (Forge 1.20.1)

Una aventura oscura sobre un mar de nubes: la isla de Brumavela, el barco volador, el Reino de Valdrenn, el camino de
nubes, la Fortaleza de Hueso y el portal de la Sombra.

## Instalación
1. En TLauncher instala y abre una vez la versión **Forge 1.20.1** (47.x).
2. Baja `instalar_mods.bat` de la [última release](https://github.com/ferranxd7/ciudadela-gotica/releases/latest) y
   ábrelo: pone Dark Craft, los mods de decoración y los shaders (Complementary) en tu `.minecraft`.
3. Sube la memoria de TLauncher a **8 GB o más**: la lejanía de Distant Horizons (256 chunks) y los shaders la necesitan.
4. **Crea una partida nueva.**

Con amigos: un **servidor dedicado** de Forge 1.20.1 con el mismo `darkcraft-0.1.0.jar` en su carpeta `mods` (Oculus,
que solo es del cliente, se queda quieto en el servidor).

## Mods que lleva dentro Dark Craft
Van dentro del jar, **sin ningún cambio**, y Forge los carga como mods normales (jar-in-jar). Son software libre con
la licencia GNU LGPL-3.0 (el texto, y el de la GPL-3.0 a la que remite, van en `META-INF/darkcraft/` dentro del jar,
con `CREDITOS-MODS.txt`: versión exacta, sha512 y dónde está su código fuente):
- **Distant Horizons** — James Seibel y el equipo de Distant Horizons —
  [código fuente](https://gitlab.com/distant-horizons-team/distant-horizons) · [Modrinth](https://modrinth.com/mod/distanthorizons).
  Por defecto, lejanía de 256 chunks.
- **Oculus** (Iris para Forge) — Asek3, dima_dencep y colaboradores; Iris de coderbot, IMS212 y colaboradores —
  [código fuente](https://github.com/Asek3/Oculus) · [Modrinth](https://modrinth.com/mod/oculus).
- **Embeddium** — embeddedt (a partir de Rubidium y Sodium, de CaffeineMC / JellySquid) —
  [código fuente](https://github.com/embeddedt/embeddium) · [Modrinth](https://modrinth.com/mod/embeddium).

## Shaders
Dark Craft enciende por defecto **Complementary Shaders – Unbound**, de **EminGT**
([complementary.dev](https://www.complementary.dev/)), si está en la carpeta `shaderpacks` (lo baja el instalador desde
Modrinth; no va dentro del jar). Se usa sin cambios y con su licencia (Complementary License Agreement); cualquier
problema que dé dentro de Dark Craft es responsabilidad de Dark Craft, no de Complementary Development.

## Comandos (requieren trucos / operador)
- `/darkcraft valdrenn localizar` – te dice dónde está el Reino de Valdrenn más cercano (con botón para teletransportarte).
- `/darkcraft valdrenn ir` – te lleva directamente al Reino de Valdrenn más cercano.
- `/darkcraft ayuda` – la lista de todos los comandos (haz clic en uno para escribirlo).
- `/locate structure darkcraft:valdrenn` – también lo encuentra el comando normal del juego.

## Cómo llegar sin trucos
Junto al borde sur de la isla hay una **torre de ascenso** que sube desde el suelo hasta la isla
con una escalera de caracol, y un puente que entra en la isla.

## El Rey Corvin (jefe del salón del trono)
- Está sentado en su trono. Al entrar en la sala se cierran todos los rastrillos, se levanta y te habla.
- Tres fases (la barra de vida está partida en tres): espadas normales, espadas de fuego y espadas de lava.
  En la tercera te lleva a la dimensión de los volcanes: mira el suelo (rojo = peligro, dorado = a salvo).
- Hace falta ir con netherite encantada. No se pueden poner bloques, ni volar con élitros, ni usar perlas.
- Si mueren todos, el Rey vuelve a su trono con la vida entera. Solo se puede vencer una vez por mundo.
- Al morir aparece un cofre con sus dos espadas (Diestra y Siniestra del Rey), la Llave del Árbol y el
  capítulo 3 de la crónica. La llave rompe el sello rojo de la Sala del Árbol.
- Espadas: los golpes llenan la barra de energía. **Shift + clic izquierdo**: Tajo Real.
  **Ctrl + clic izquierdo**: Juicio del Rey. **Clic derecho**: cubrirse con las espadas cruzadas.

Comandos para probarlo (con trucos): `/darkcraft valdrenn rey ir` (te lleva al salón del trono),
`/darkcraft valdrenn rey estado`, `/darkcraft valdrenn rey reiniciar`, `/darkcraft valdrenn rey fase 2` (o `3`),
`/darkcraft valdrenn rey matar`.

## Ser Gavran (jefe de la Sala del Árbol)
- Espera en una rama del Árbol de la Corona, en lo alto de la torre-árbol. La Llave del Árbol abre la puerta de su
  sala; al entrar se cierra, baja de un salto y te habla.
- Dos fases: en la segunda el árbol despierta (raíces que atrapan, hojas cortantes, raíces por todo el suelo: sube a
  la tarima o a los balcones). En el **Juicio de las espinas** solo te salvas escondido detrás de un muro de corteza.
  Su punto débil es el **corazón del árbol** que le brilla en el pecho.
- Al caer se arrodilla, se deshace en hojas, el árbol florece y sube el cofre del capítulo 5 (el final de la
  crónica), con el **Mandoble del Juramento** y la **Llave de Hueso**.
- Mandoble del Juramento: a dos manos (con la otra mano libre). **Shift + clic izquierdo**: Raíces del Juramento.
  **Ctrl + clic izquierdo**: Juicio de Espinas. Usa la misma energía que las espadas del Rey.

Comandos para probarlo (con trucos): `/darkcraft valdrenn gavran ir`, `/darkcraft valdrenn gavran estado`,
`/darkcraft valdrenn gavran reiniciar`, `/darkcraft valdrenn gavran fase 2`, `/darkcraft valdrenn gavran matar`.

## Fases
1. ✅ Isla + castillo + generación en el mundo
2. Mobs propios por salas
3. ✅ Jefe: el Rey Corvin + botín + armas
4. ✅ Jefe: Ser Gavran + Mandoble del Juramento + final de la crónica (capítulo 5)
5. Calavera, fortalezas del cielo y nueva dimensión con hogueras

## Créditos de la música
Toda la música tiene licencia libre y se descarga de OpenGameArt al compilar (los detalles completos
van dentro del mod, en `assets/darkcraft/creditos.txt`):
- "Solemn (Opening)" de İlker Yalçıner (onemansymphony), CC-BY 4.0 — fondo de los cofres de la crónica.
- "Darkness March" de Alexandr Zhelanov, CC-BY 4.0 — Corvin, fase 1.
- "Dark souls type boss theme" de Philémon Weber (Helmet), CC-BY 4.0 — Corvin, fase 2.
- "Colossal Boss Battle Theme" de Matthew Pablo (www.matthewpablo.com), CC-BY 3.0 — Corvin, fase 3.
- "Dark Descent" de Matthew Pablo (www.matthewpablo.com), CC-BY 3.0 — Ser Gavran, fase 1.
- "A Slave To No One" de Jonathan Shaw (www.jshaw.co.uk), CC-BY 3.0 — Ser Gavran, fase 2.

Se han pasado a OGG, sin silencio al principio y al final y con el volumen igualado.
