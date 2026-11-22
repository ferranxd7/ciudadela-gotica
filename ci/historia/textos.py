# -*- coding: utf-8 -*-
"""
Textos de Dark Craft: la historia del Reino de Valdrenn en todos los idiomas del mod. Genera los ficheros de idioma del mod y
comprueba que cada pagina de libro cabe en la pantalla del libro de Minecraft (114 px x 14 lineas).
Uso: python3 ci/historia/textos.py   (escribe src/main/resources/assets/darkcraft/lang/*.json)
"""
import json, os, sys

CAP = {
 1: dict(
  es_t="La Corona Rota", es_s=("La Corona", "Rota"),
  en_t="The Broken Crown", en_s=("The Broken", "Crown"),
  es=[
   "Si estás leyendo esto, has subido la Torre de Ascenso. Pocos lo intentan. Ninguno vuelve a bajar igual que subió. Siéntate y escucha a un viejo.",
   "Me llamo Aldous y fui el cronista del rey. Escribo para que alguien recuerde lo que fue Valdrenn: la joya del reino, con torres que rozaban las nubes y ríos que cantaban por cada calle.",
   "Nuestro rey, Corvin, amaba a la reina Isolde más que a su propia corona. Cuando la fiebre gris empezó a llevársela, no hubo sanador, ni rezo, ni ofrenda que pudiera salvarla.",
   "Una noche sin luna, el rey bajó solo bajo la tormenta y habló con la Sombra sin Nombre. Le ofreció un trato: la vida de la reina... a cambio de todo lo demás.",
   "La Sombra aceptó. Esa misma noche el reino entero fue arrancado de la tierra y alzado al cielo. Desde entonces sus ríos caen al vacío y la tormenta no ha cesado ni un solo día.",
   "Nadie murió. Fue peor. Soldados, criados y nobles se quedaron huecos, sin alma, y todavía recorren estos salones. No esperes piedad de ellos: ya no recuerdan su nombre.",
   "Dos guardianes mantienen viva la maldición: el Rey, que aún espera sentado en su Salón del Trono, y Ser Gavran, su caballero jurado, que vela en lo más alto del Árbol de la Corona.",
   "Mátalos a los dos y quizá la tormenta termine. No te diré cómo; yo solo escribo. Mi siguiente página te espera donde el agua aún canta, junto a la Fuente Mayor.\n\n- A.",
  ],
  en=[
   "If you are reading this, you have climbed the Ascent Tower. Few try. None come down the same as they went up. Sit, and listen to an old man.",
   "My name is Aldous, and I was the king's chronicler. I write so that someone remembers what Valdrenn was: the jewel of the kingdom, its towers touching the clouds, its rivers singing through every street.",
   "Our king, Corvin, loved Queen Isolde more than his own crown. When the grey fever began to take her, no healer, no prayer and no offering could save her.",
   "On a moonless night the king went down alone beneath the storm and spoke with the Nameless Shadow. He offered a bargain: the queen's life... in exchange for everything else.",
   "The Shadow agreed. That very night the whole kingdom was torn from the earth and raised into the sky. Since then its rivers fall into the void, and the storm has not ceased for a single day.",
   "No one died. It was worse. Soldiers, servants and nobles were left hollow, without souls, and they still walk these halls. Expect no mercy from them: they no longer remember their names.",
   "Two guardians keep the curse alive: the King, who still waits in his Throne Hall, and Sir Gavran, his sworn knight, who keeps watch at the very top of the Crown Tree.",
   "Slay them both and perhaps the storm will end. I will not tell you how; I only write. My next page awaits you where the water still sings, beside the Great Fountain.\n\n- A.",
  ]),
 2: dict(
  es_t="Donde el agua aún canta", es_s=("Donde el agua", "aún canta"),
  en_t="Where the Water Still Sings", en_s=("Where the Water", "Still Sings"),
  es=[
   "La reina se sentaba aquí cada tarde. Decía que el murmullo de esta fuente le recordaba a su tierra, al otro lado del mar.",
   "Cuando el rey volvió de la tormenta, ella abrió los ojos. Lo celebramos como locos. Durante tres días hubo música en los patios y vino en todas las bodegas.",
   "Al cuarto día los criados dejaron de comer. Al quinto, sus ojos se volvieron grises como la ceniza. Las campanas tocaban solas de madrugada, sin nadie que tirara de las cuerdas.",
   "Ser Gavran mandó cerrar las puertas para que nadie huyera ni entrara. Juró que protegería al rey aunque el mundo entero ardiera. Lo cumplió demasiado bien.",
   "Uno a uno, los míos se fueron vaciando. Los vi seguir con sus tareas: barrer salas vacías, servir mesas sin comensales, montar guardia frente a nadie.",
   "Yo no cambié. No sé si es un regalo o un castigo de la Sombra. Quizá necesita a alguien que lo recuerde todo... o a alguien que te lo cuente a ti.",
   "Busca al Rey. Sigue en su trono, esperando a una reina que nunca llega. Cuando caiga, encontrarás junto a él mi tercera página.\n\n- A.",
  ],
  en=[
   "The queen used to sit here every evening. She said the murmur of this fountain reminded her of her homeland, across the sea.",
   "When the king returned from the storm, she opened her eyes. We celebrated like madmen. For three days there was music in the courtyards and wine in every cellar.",
   "On the fourth day the servants stopped eating. On the fifth, their eyes turned grey as ash. The bells rang on their own before dawn, with no one pulling the ropes.",
   "Sir Gavran ordered the gates shut so that no one could flee or enter. He swore he would protect the king even if the whole world burned. He kept that oath far too well.",
   "One by one, my people emptied. I watched them carry on with their duties: sweeping empty halls, serving tables with no guests, standing guard before no one.",
   "I did not change. I do not know if it is a gift or a punishment from the Shadow. Perhaps it needs someone to remember everything... or someone to tell it to you.",
   "Seek the King. He still sits upon his throne, waiting for a queen who never comes. When he falls, you will find my third page beside him.\n\n- A.",
  ]),
 3: dict(
  es_t="El Rey que Espera", es_s=("El Rey", "que Espera"),
  en_t="The Waiting King", en_s=("The Waiting", "King"),
  es=[
   "Así que el Rey ha caído. Que la luz, si es que aún queda alguna, tenga piedad de él.",
   "Debes saber la verdad. La Sombra no devolvió a la reina: devolvió algo con su rostro. Isolde despertó, pero su risa ya no era suya.",
   "Corvin lo supo con el primer beso. Aun así siguió amándola. Cada noche se sentaba en ese trono a esperar que la verdadera Isolde entrara por la puerta.",
   "El trato tenía otro precio: la corona. Mientras Corvin la llevara, la Sombra tendría un pie en este mundo. Por eso se agrietó, y por eso él jamás se la quitó.",
   "Ahora la corona está rota del todo. La tormenta ruge más débil, ¿la oyes? Pero no basta. Aún queda alguien que sostiene el juramento.",
   "Cuando la reina dejó de ser la reina, el obispo Merrow la encerró bajo el altar de la catedral. Desde entonces nadie se atreve a entrar en la nave.",
   "Ve a la catedral. Mi cuarta página espera a los pies del altar. Y no hagas caso de las voces que oigas allí dentro.\n\n- A.",
  ],
  en=[
   "So the King has fallen. May the light, if any is left, have mercy on him.",
   "You must know the truth. The Shadow did not give the queen back: it gave back something wearing her face. Isolde awoke, but her laughter was no longer her own.",
   "Corvin knew it from the first kiss. Even so, he kept loving her. Every night he sat on that throne, waiting for the true Isolde to walk through the door.",
   "The bargain had another price: the crown. As long as Corvin wore it, the Shadow would keep one foot in this world. That is why it cracked, and why he never took it off.",
   "Now the crown is broken for good. The storm roars weaker, can you hear it? But it is not enough. Someone still holds the oath.",
   "When the queen stopped being the queen, Bishop Merrow sealed her beneath the altar of the cathedral. Since then no one dares to enter the nave.",
   "Go to the cathedral. My fourth page waits at the foot of the altar. And pay no heed to the voices you hear inside.\n\n- A.",
  ]),
 4: dict(
  es_t="La Reina bajo el Altar", es_s=("La Reina bajo", "el Altar"),
  en_t="The Queen Under the Altar", en_s=("The Queen", "Under the Altar"),
  es=[
   "Esta catedral fue lo más hermoso que levantó Valdrenn. Ahora sus bancos solo esperan a los muertos.",
   "El obispo Merrow encerró a la reina bajo este altar con siete cerrojos y siete oraciones. Dicen que todavía se la oye cantar, muy bajito, cuando cae un rayo.",
   "La noche en que la sellaron, la tierra tembló y un árbol brotó del corazón del torreón. Creció hasta el cielo en una sola noche, partiendo piedra y vigas a su paso.",
   "Lo llamamos el Árbol de la Corona. Sus raíces beben de la Sombra, y en su copa guarda el último pedazo del trato: el juramento de Ser Gavran.",
   "Gavran trepó por sus ramas y nunca volvió a bajar. Dicen que ha crecido con el árbol, que ya es tan alto como tres hombres y que la armadura se le ha fundido a la piel.",
   "Mientras él viva, la tormenta no se irá. No dejará que nadie llegue a la cima. Lleva el mejor acero que encuentres, y más valor del que creas tener.",
   "Sube al Árbol de la Corona, hasta la sala más alta. Si vences, mi última página estará a sus pies.\n\n- A.",
  ],
  en=[
   "This cathedral was the most beautiful thing Valdrenn ever raised. Now its pews wait only for the dead.",
   "Bishop Merrow sealed the queen beneath this altar with seven locks and seven prayers. They say you can still hear her singing, very softly, whenever lightning strikes.",
   "On the night they sealed her, the earth shook and a tree burst from the heart of the keep. It grew to the sky in a single night, splitting stone and beams on its way.",
   "We called it the Crown Tree. Its roots drink from the Shadow, and its crown holds the last piece of the bargain: the oath of Sir Gavran.",
   "Gavran climbed its branches and never came down. They say he has grown with the tree, that he now stands as tall as three men, and that his armour has melted into his skin.",
   "While he lives, the storm will not leave. He will let no one reach the top. Bring the finest steel you can find, and more courage than you think you have.",
   "Climb the Crown Tree to its highest hall. If you prevail, my last page will lie at his feet.\n\n- A.",
  ]),
 5: dict(
  es_t="El Último Juramento", es_s=("El Último", "Juramento"),
  en_t="The Last Oath", en_s=("The Last", "Oath"),
  es=[
   "Ser Gavran ha caído. Su juramento se rompe, y con él se rompe lo que ataba a la Sombra a estas piedras.",
   "Gracias. Hacía cien años que nadie me leía. Siento cómo se me escapa la tinta de los dedos; creo que por fin podré descansar.",
   "Pero escucha bien: la Sombra no vive aquí. Este reino solo era su puerta. Ella espera mucho más lejos, allí donde nace la tormenta.",
   "Gavran guardaba una llave que no es de hierro, sino de hueso. Úsala y te mostrará el camino hacia fortalezas que flotan más allá de las nubes.",
   "Dicen que tras ellas se alza una montaña que toca las estrellas, y un castillo donde arden las hogueras de los caídos. Allí se descansa... y desde allí se vuelve.",
   "Yo ya no puedo acompañarte. Lo que venga ahora es cosa tuya. Busca, lucha, cae y vuelve a levantarte. Nadie más va a escribir tu historia.",
   "Aquí termina la crónica de Valdrenn. Que tu acero no se melle y tu fuego nunca se apague.\n\n- Aldous, último cronista del rey.",
  ],
  en=[
   "Sir Gavran has fallen. His oath is broken, and with it breaks whatever bound the Shadow to these stones.",
   "Thank you. For a hundred years no one has read me. I can feel the ink slipping from my fingers; I think at last I will be able to rest.",
   "But listen well: the Shadow does not live here. This kingdom was only its door. It waits much further away, where the storm is born.",
   "Gavran kept a key that is not made of iron, but of bone. Use it, and it will show you the way to fortresses that float beyond the clouds.",
   "They say that behind them rises a mountain that touches the stars, and a castle where the bonfires of the fallen burn. There one rests... and from there one returns.",
   "I can no longer go with you. Whatever comes now is yours alone. Seek, fight, fall and rise again. No one else will write your story.",
   "Here ends the chronicle of Valdrenn. May your steel never dull and your fire never fade.\n\n- Aldous, last chronicler of the king.",
  ]),
}

PISTA = {
 "es": {1: "Sube a lo alto de la Torre de Ascenso, al sur de la isla.",
        2: "Donde el agua aún canta: la Fuente Mayor, en el patio, delante del torreón.",
        3: "El Rey espera en su Salón del Trono, dentro del torreón. Derrótalo.",
        4: "A los pies del altar de la catedral, al este del torreón.",
        5: "En la sala más alta del Árbol de la Corona. Derrota a Ser Gavran.",
        6: "La crónica ha terminado. Busca las fortalezas que flotan más allá de las nubes."},
 "en": {1: "Climb to the top of the Ascent Tower, south of the island.",
        2: "Where the water still sings: the Great Fountain, in the courtyard before the keep.",
        3: "The King waits in his Throne Hall, inside the keep. Defeat him.",
        4: "At the foot of the cathedral altar, east of the keep.",
        5: "In the highest hall of the Crown Tree. Defeat Sir Gavran.",
        6: "The chronicle has ended. Seek the fortresses that float beyond the clouds."},
}

# Narracion del capitulo 1 (voz en ingles, subtitulos en el idioma del juego). Una linea = un subtitulo.
NARRACION = [
 ("In the days of the Broken Crown, Valdrenn was the jewel of the kingdom.",
  "En los días de la Corona Rota, Valdrenn era la joya del reino."),
 ("Its towers touched the clouds, and its rivers sang through every street.",
  "Sus torres rozaban las nubes y sus ríos cantaban por cada calle."),
 ("But Queen Isolde fell ill, and no healer, no prayer, could save her.",
  "Pero la reina Isolde enfermó, y ningún sanador, ningún rezo, pudo salvarla."),
 ("So King Corvin went down beneath the storm... and made a pact with the Nameless Shadow.",
  "Así que el rey Corvin bajó bajo la tormenta... e hizo un pacto con la Sombra sin Nombre."),
 ("Her life... for everything else.",
  "La vida de ella... a cambio de todo lo demás."),
 ("That night, the kingdom was torn from the earth, and its rivers began to fall into the void.",
  "Esa noche el reino fue arrancado de la tierra, y sus ríos empezaron a caer al vacío."),
 ("The people of Valdrenn did not die. They were hollowed... and they still walk these halls.",
  "La gente de Valdrenn no murió. Quedó hueca... y todavía recorre estos salones."),
 ("From the heart of the keep grew the Crown Tree, and at its peak, a sworn knight keeps his watch.",
  "Del corazón del torreón brotó el Árbol de la Corona, y en su cima un caballero jurado sigue en guardia."),
 ("The King still waits upon his throne. The Knight still guards the sky.",
  "El Rey aún espera en su trono. El Caballero aún guarda el cielo."),
 ("Slay them both... and perhaps, at last, the storm will end.",
  "Mátalos a los dos... y quizá, por fin, la tormenta termine."),
]
# --------------------------------------------------------------- lo que dice el Rey (voz en ingles, subtitulos)
REY = [
 ("At last... a visitor. I have waited so long upon this throne.",
  "Por fin... una visita. He esperado tanto tiempo en este trono."),
 ("The Shadow promised me a kingdom without end. And you have come to take it from me?",
  "La Sombra me prometió un reino sin fin. ¿Y tú vienes a quitármelo?"),
 ("Then kneel before your king... or die standing!",
  "Entonces arrodíllate ante tu rey... ¡o muere de pie!"),
 ("Enough! Burn... burn with my kingdom!",
  "¡Basta! Arde... ¡arde con mi reino!"),
 ("If my kingdom must fall, you will fall with me... into the fire!",
  "Si mi reino ha de caer, caerás conmigo... ¡al fuego!"),
 ("The crown... was never mine to keep...",
  "La corona... nunca fue mía..."),
 ("Another fool for my court.",
  "Otro necio más para mi corte."),
 ("Is that all? My guards fought better than you.",
  "¿Eso es todo? Mis guardias peleaban mejor."),
 ("Feel the fire that devoured my city!",
  "¡Siente el fuego que devoró mi ciudad!"),
 ("No more robes. No more court. Look upon what the fire made of me!",
  "Se acabaron las ropas. Se acabó la corte. ¡Mira en qué me convirtió el fuego!"),
 ("Kneel... before the flames!",
  "¡Arrodíllate... ante las llamas!"),
 ("Impossible... the fire... obeys you?",
  "Imposible... ¿el fuego... te obedece?"),
 ("Another fool... for my court.",
  "Otro necio... para mi corte."),
]
REY_UI = {
 "es": {
  "entity.darkcraft.rey": "Corvin, el Rey que Espera",
  "entity.darkcraft.rey_proyectil": "Tajo del Rey",
  "darkcraft.rey.nombre_corto": "Corvin",
  "darkcraft.rey.barra_arde": "Corvin, el Rey que Arde",
  "block.darkcraft.rastrillo": "Rastrillo de hierro negro",
  "death.attack.darkcraft.rey": "%1$s fue ejecutado por %2$s",
  "death.attack.darkcraft.rey.player": "%1$s fue ejecutado por %2$s",
  "death.attack.darkcraft.rey_area": "%1$s ardió ante %2$s",
  "death.attack.darkcraft.rey_area.player": "%1$s ardió ante %2$s",
  "subtitles.darkcraft.rey.voz": "Corvin habla",
  "darkcraft.rey.titulo.30": "CORVIN",
  "darkcraft.rey.subtitulo.30": "El Rey que Arde",
  "darkcraft.rey.titulo.32": "CORVIN",
  "darkcraft.rey.subtitulo.32": "El Rey que Espera",
  "item.darkcraft.espada_diestra": "Diestra del Rey",
  "item.darkcraft.espada_siniestra": "Siniestra del Rey",
  "item.darkcraft.espada.lore": "Las espadas de Corvin, templadas en el fuego que devoró Valdrenn",
  "item.darkcraft.espada.combo1": "Shift + clic izquierdo: Tajo Real (35 de energía)",
  "item.darkcraft.espada.combo2": "Ctrl + clic izquierdo: Juicio del Rey (80 de energía)",
  "item.darkcraft.espada.bloqueo": "Clic derecho: cubrirse con las espadas cruzadas",
  "item.darkcraft.espada.dos": "Con una en cada mano los combos golpean más fuerte",
  "darkcraft.espada.sin_energia": "Te falta energía (necesitas %s): golpea con las espadas para llenarla",
  "item.darkcraft.llave_arbol": "Llave del Árbol",
  "item.darkcraft.llave_arbol.lore": "Abre la gran puerta de la Sala del Árbol",
  "item.darkcraft.llave_arbol.aqui_no": "Esta llave abre la gran puerta de la Sala del Árbol, en lo alto de la torre del árbol",
  "block.darkcraft.sello_runico": "Sello rúnico",
  "entity.darkcraft.ascensor": "Disco de runas",
  "entity.darkcraft.puerta_arbol": "Puerta de la Sala del Árbol",
  "darkcraft.ascensor.sube": "El disco de runas despierta para subir  %s",
  "darkcraft.ascensor.baja": "El disco de runas despierta para bajar  %s",
  "darkcraft.ascensor.llama": "Llamando al disco de runas  %s",
  "darkcraft.puerta.cerrada": "La puerta está cerrada con runas y cerrojos: hace falta la Llave del Árbol",
  "darkcraft.rey.titulo.31": "LA SALA DEL ÁRBOL",
  "darkcraft.rey.subtitulo.31": "Donde aguarda Ser Gavran",
  "darkcraft.rey.circulos": "¡A LOS CÍRCULOS DORADOS!",
  "darkcraft.rey.cuenta": "— %s —",
  "subtitles.darkcraft.sfx.rey_tajo": "Espadazo del Rey",
  "subtitles.darkcraft.sfx.rey_tajo_fuego": "Tajo de fuego",
  "subtitles.darkcraft.sfx.rey_bola_fuego": "Bola de fuego",
  "subtitles.darkcraft.sfx.rey_explosion": "Explosión",
  "subtitles.darkcraft.sfx.rey_golpe_suelo": "Golpe contra el suelo",
  "subtitles.darkcraft.sfx.rey_golpe_final": "Golpe final",
  "subtitles.darkcraft.sfx.rey_rugido": "Rugido del Rey",
  "subtitles.darkcraft.sfx.rey_geiser": "Géiser de lava",
  "subtitles.darkcraft.sfx.rey_pilares": "Pilares de fuego",
  "subtitles.darkcraft.sfx.rey_carga": "El fuego se acumula",
  "subtitles.darkcraft.sfx.cuenta_atras": "Tambor de guerra",
  "subtitles.darkcraft.sfx.circulo_dorado": "Brilla un círculo dorado",
  "subtitles.darkcraft.sfx.puerta_runas": "Las runas despiertan",
  "subtitles.darkcraft.sfx.puerta_abre": "La gran puerta se abre",
  "subtitles.darkcraft.sfx.puerta_cierra": "La gran puerta retumba",
  "subtitles.darkcraft.sfx.ascensor_mueve": "El disco de runas se mueve",
  "subtitles.darkcraft.sfx.ascensor_llega": "El disco de runas se detiene",
 },
 "en": {
  "entity.darkcraft.rey": "Corvin, the King Who Waits",
  "entity.darkcraft.rey_proyectil": "King's Slash",
  "darkcraft.rey.nombre_corto": "Corvin",
  "darkcraft.rey.barra_arde": "Corvin, the Burning King",
  "block.darkcraft.rastrillo": "Black Iron Portcullis",
  "death.attack.darkcraft.rey": "%1$s was executed by %2$s",
  "death.attack.darkcraft.rey.player": "%1$s was executed by %2$s",
  "death.attack.darkcraft.rey_area": "%1$s burned before %2$s",
  "death.attack.darkcraft.rey_area.player": "%1$s burned before %2$s",
  "subtitles.darkcraft.rey.voz": "Corvin speaks",
  "darkcraft.rey.titulo.30": "CORVIN",
  "darkcraft.rey.subtitulo.30": "The Burning King",
  "darkcraft.rey.titulo.32": "CORVIN",
  "darkcraft.rey.subtitulo.32": "The King Who Waits",
  "item.darkcraft.espada_diestra": "The King's Right Hand",
  "item.darkcraft.espada_siniestra": "The King's Left Hand",
  "item.darkcraft.espada.lore": "Corvin's swords, tempered in the fire that devoured Valdrenn",
  "item.darkcraft.espada.combo1": "Shift + left click: Royal Slash (35 energy)",
  "item.darkcraft.espada.combo2": "Ctrl + left click: King's Judgement (80 energy)",
  "item.darkcraft.espada.bloqueo": "Right click: guard with crossed swords",
  "item.darkcraft.espada.dos": "With one in each hand, combos hit harder",
  "darkcraft.espada.sin_energia": "Not enough energy (you need %s): strike with the swords to fill it",
  "item.darkcraft.llave_arbol": "Key of the Tree",
  "item.darkcraft.llave_arbol.lore": "Opens the great door of the Tree Hall",
  "item.darkcraft.llave_arbol.aqui_no": "This key opens the great door of the Tree Hall, at the top of the tree tower",
  "block.darkcraft.sello_runico": "Runic Seal",
  "entity.darkcraft.ascensor": "Rune Disc",
  "entity.darkcraft.puerta_arbol": "Door of the Tree Hall",
  "darkcraft.ascensor.sube": "The rune disc awakens to rise  %s",
  "darkcraft.ascensor.baja": "The rune disc awakens to descend  %s",
  "darkcraft.ascensor.llama": "Calling the rune disc  %s",
  "darkcraft.puerta.cerrada": "The door is sealed with runes and bolts: you need the Key of the Tree",
  "darkcraft.rey.titulo.31": "THE TREE HALL",
  "darkcraft.rey.subtitulo.31": "Where Sir Gavran awaits",
  "darkcraft.rey.circulos": "TO THE GOLDEN CIRCLES!",
  "darkcraft.rey.cuenta": "— %s —",
  "subtitles.darkcraft.sfx.rey_tajo": "King's sword swings",
  "subtitles.darkcraft.sfx.rey_tajo_fuego": "Fire slash",
  "subtitles.darkcraft.sfx.rey_bola_fuego": "Fireball",
  "subtitles.darkcraft.sfx.rey_explosion": "Explosion",
  "subtitles.darkcraft.sfx.rey_golpe_suelo": "Ground slam",
  "subtitles.darkcraft.sfx.rey_golpe_final": "Final blow",
  "subtitles.darkcraft.sfx.rey_rugido": "King roars",
  "subtitles.darkcraft.sfx.rey_geiser": "Lava geyser",
  "subtitles.darkcraft.sfx.rey_pilares": "Pillars of fire",
  "subtitles.darkcraft.sfx.rey_carga": "Fire gathers",
  "subtitles.darkcraft.sfx.cuenta_atras": "War drum",
  "subtitles.darkcraft.sfx.circulo_dorado": "A golden circle shines",
  "subtitles.darkcraft.sfx.puerta_runas": "Runes awaken",
  "subtitles.darkcraft.sfx.puerta_abre": "Great door opens",
  "subtitles.darkcraft.sfx.puerta_cierra": "Great door booms",
  "subtitles.darkcraft.sfx.ascensor_mueve": "Rune disc moves",
  "subtitles.darkcraft.sfx.ascensor_llega": "Rune disc stops",
 },
}

# pausa (s) despues de cada linea: las largas son los cortes de plano
PAUSAS = [0.8, 1.6, 0.8, 0.7, 1.6, 1.6, 0.8, 1.6, 0.9, 0.0]

UI = {
 "es": {
  "itemGroup.darkcraft": "Dark Craft",
  "item.darkcraft.mapa_valdrenn": "Mapa de Valdrenn",
  "darkcraft.mapa.lore": "Clic derecho para desplegarlo",
  "darkcraft.mapa.sin_castillo": "Este mapa no pertenece a ningún reino cercano.",
  "darkcraft.mapa.titulo": "Reino de Valdrenn",
  "darkcraft.mapa.cronica": "Crónica",
  "darkcraft.mapa.siguiente": "Siguiente página",
  "darkcraft.mapa.tu": "Tú",
  "darkcraft.mapa.cerrar": "Pulsa ESC para enrollar el mapa",
  "darkcraft.mapa.lugar.torre_ascenso": "Torre de Ascenso",
  "darkcraft.mapa.lugar.puerta_exterior": "Puerta Exterior",
  "darkcraft.mapa.lugar.puerta_interior": "Puerta Interior",
  "darkcraft.mapa.lugar.fuente": "Fuente Mayor",
  "darkcraft.mapa.lugar.torreon": "Torreón",
  "darkcraft.mapa.lugar.trono": "Salón del Trono",
  "darkcraft.mapa.lugar.arbol": "Árbol de la Corona",
  "darkcraft.mapa.lugar.catedral": "Catedral",
  "darkcraft.mapa.lugar.biblioteca": "Biblioteca",
  "darkcraft.mapa.lugar.cementerio": "Cementerio",
  "darkcraft.mapa.lugar.laberinto": "Laberinto",
  "darkcraft.mapa.lugar.patio_armas": "Patio de Armas",
  "darkcraft.mapa.lugar.alquimista": "Torre del Alquimista",
  "darkcraft.mapa.lugar.observatorio": "Observatorio",
  "darkcraft.mapa.lugar.pueblo": "Pueblo",
  "darkcraft.mapa.lugar.jardin": "Jardín Privado",
  "darkcraft.capitulo": "CAPÍTULO %s",
  "darkcraft.cofre": "Crónica · Capítulo %s",
  "darkcraft.libro": "Crónica de Valdrenn · Capítulo %s",
  "darkcraft.libro.lore": "Escrita por Aldous, cronista del rey",
  "darkcraft.cine.saltar": "Mantén ESPACIO para saltar",
  "darkcraft.historia.reiniciada": "Tu historia en el Reino de Valdrenn empieza de nuevo.",
  "darkcraft.historia.no_cofre": "Este cofre guarda una página que todavía no te corresponde leer.",
  "subtitles.darkcraft.campana": "Campana fúnebre",
  "subtitles.darkcraft.narracion": "Narrador",
  "darkcraft.cmd.no_reino": "No hay ningún Reino de Valdrenn cerca.",
  "darkcraft.cmd.localizar": "Reino de Valdrenn en X=%s Z=%s (a %s bloques).",
  "darkcraft.cmd.tp": "[Teletransportarme]",
  "darkcraft.cmd.bienvenido": "Bienvenido al Reino de Valdrenn.",
 },
 "en": {
  "itemGroup.darkcraft": "Dark Craft",
  "item.darkcraft.mapa_valdrenn": "Map of Valdrenn",
  "darkcraft.mapa.lore": "Right-click to unroll it",
  "darkcraft.mapa.sin_castillo": "This map does not belong to any nearby kingdom.",
  "darkcraft.mapa.titulo": "Kingdom of Valdrenn",
  "darkcraft.mapa.cronica": "Chronicle",
  "darkcraft.mapa.siguiente": "Next page",
  "darkcraft.mapa.tu": "You",
  "darkcraft.mapa.cerrar": "Press ESC to roll up the map",
  "darkcraft.mapa.lugar.torre_ascenso": "Ascent Tower",
  "darkcraft.mapa.lugar.puerta_exterior": "Outer Gate",
  "darkcraft.mapa.lugar.puerta_interior": "Inner Gate",
  "darkcraft.mapa.lugar.fuente": "Great Fountain",
  "darkcraft.mapa.lugar.torreon": "The Keep",
  "darkcraft.mapa.lugar.trono": "Throne Hall",
  "darkcraft.mapa.lugar.arbol": "The Crown Tree",
  "darkcraft.mapa.lugar.catedral": "Cathedral",
  "darkcraft.mapa.lugar.biblioteca": "Library",
  "darkcraft.mapa.lugar.cementerio": "Cemetery",
  "darkcraft.mapa.lugar.laberinto": "Hedge Maze",
  "darkcraft.mapa.lugar.patio_armas": "Training Yard",
  "darkcraft.mapa.lugar.alquimista": "Alchemist's Tower",
  "darkcraft.mapa.lugar.observatorio": "Observatory",
  "darkcraft.mapa.lugar.pueblo": "Town",
  "darkcraft.mapa.lugar.jardin": "Private Garden",
  "darkcraft.capitulo": "CHAPTER %s",
  "darkcraft.cofre": "Chronicle · Chapter %s",
  "darkcraft.libro": "Chronicle of Valdrenn · Chapter %s",
  "darkcraft.libro.lore": "Written by Aldous, the king's chronicler",
  "darkcraft.cine.saltar": "Hold SPACE to skip",
  "darkcraft.historia.reiniciada": "Your story in the Kingdom of Valdrenn begins anew.",
  "darkcraft.historia.no_cofre": "This chest keeps a page you are not meant to read yet.",
  "subtitles.darkcraft.campana": "Funeral bell",
  "subtitles.darkcraft.narracion": "Narrator",
  "darkcraft.cmd.no_reino": "There is no Kingdom of Valdrenn nearby.",
  "darkcraft.cmd.localizar": "Kingdom of Valdrenn at X=%s Z=%s (%s blocks away).",
  "darkcraft.cmd.tp": "[Teleport me]",
  "darkcraft.cmd.bienvenido": "Welcome to the Kingdom of Valdrenn.",
 },
}

# --------------------------------------------------------------- ancho de letra de Minecraft
W = {' ': 4, '!': 2, '"': 4, "'": 2, '(': 4, ')': 4, '*': 4, ',': 2, '.': 2, ':': 2, ';': 2, '<': 5, '>': 5,
     '@': 7, 'I': 4, '[': 4, ']': 4, '`': 3, 'f': 5, 'i': 2, 'k': 5, 'l': 3, 't': 4, '{': 4, '|': 2, '}': 4,
     '~': 7, 'í': 3, 'Í': 4, '¡': 2, '·': 2}

def ancho_letra(ch):
    o = ord(ch)
    # japones (kana, kanji, signos de ancho completo): en Minecraft salen de la fuente unifont, unos 9 px
    if 0x2E80 <= o <= 0x9FFF or 0xF900 <= o <= 0xFAFF or 0xFF00 <= o <= 0xFFEF or 0x3000 <= o <= 0x303F:
        return 9
    return W.get(ch, 6)


def width(s, bold=False):
    return sum(ancho_letra(ch) + (1 if bold else 0) for ch in s)


def wrap(text, maxw=114, sin_espacios=False):
    """Parte el texto en lineas como el libro de Minecraft (por espacios; en japones, por letras)."""
    lines = []
    for para in text.split("\n"):
        if sin_espacios:
            cur = ""
            for ch in para:
                if width(cur + ch) <= maxw:
                    cur += ch
                else:
                    lines.append(cur)
                    cur = ch
            lines.append(cur)
            continue
        cur = ""
        for word in para.split(" "):
            cand = word if not cur else cur + " " + word
            if width(cand) <= maxw:
                cur = cand
            else:
                if cur:
                    lines.append(cur)
                cur = word
        lines.append(cur)
    return lines


PAGINAS = {1: 8, 2: 7, 3: 7, 4: 7, 5: 7}   # las mismas que Story.PAGES


def check_cap(nombre, caps, sin_espacios=False):
    """caps: {n: (titulo, (cartel1, cartel2), [paginas])}. Cada pagina tiene que caber en el libro."""
    ok = True
    for n, (titulo, carteles, paginas) in caps.items():
        if len(paginas) != PAGINAS[n]:
            print(f"cap {n} {nombre}: {len(paginas)} paginas y tienen que ser {PAGINAS[n]}")
            ok = False
        for i, page in enumerate(paginas):
            extra = 3 if i == 0 else 0   # pagina 1: CAPITULO N + titulo + linea en blanco
            tl = len(wrap(titulo, sin_espacios=sin_espacios)) if i == 0 else 0
            lines = len(wrap(page, sin_espacios=sin_espacios)) + extra + max(0, tl - 1)
            if lines > 13:
                ok = False
            print(f"cap {n} {nombre} p{i+1}: {lines:2d} lineas {'OK ' if lines <= 13 else 'LARGA'}")
        for line in carteles:
            if width(line) > 88:
                print("  CARTEL LARGO:", nombre, line, width(line))
                ok = False
    return ok


def check():
    ok = True
    for lang in ("es", "en"):
        ok &= check_cap(lang, {n: (c[lang + "_t"], c[lang + "_s"], c[lang]) for n, c in CAP.items()})
    for code, mod in otros_idiomas().items():
        ok &= check_cap(code, mod.T["cap"], mod.T.get("sin_espacios", False))
    return ok


def otros_idiomas():
    """Los idiomas traducidos aparte (ci/historia/idiomas/*.py), cada uno con su diccionario T."""
    import importlib.util
    out = {}
    carpeta = os.path.join(os.path.dirname(os.path.abspath(__file__)), "idiomas")
    if not os.path.isdir(carpeta):
        return out
    for f in sorted(os.listdir(carpeta)):
        if not f.endswith(".py"):
            continue
        spec = importlib.util.spec_from_file_location("idioma_" + f[:-3], os.path.join(carpeta, f))
        m = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(m)
        out[f[:-3]] = m
    return out


LOGROS_UI = {
 "es": {
  "advancements.darkcraft.raiz.title": "Dark Craft",
  "advancements.darkcraft.raiz.description": "Dicen que en el cielo flota un reino maldito",
  "advancements.darkcraft.encontrar.title": "Una isla entre las nubes",
  "advancements.darkcraft.encontrar.description": "Pisa la isla flotante del Reino de Valdrenn",
  "advancements.darkcraft.capitulo1.title": "La crónica comienza",
  "advancements.darkcraft.capitulo1.description": "Abre el cofre del capítulo 1 y escucha su historia",
  "advancements.darkcraft.rey.title": "El Rey ha caído",
  "advancements.darkcraft.rey.description": "Vence a Corvin, el Rey que Espera",
  "advancements.darkcraft.arbol.title": "Las puertas del Árbol",
  "advancements.darkcraft.arbol.description": "Abre la puerta de la Sala del Árbol con la llave del Rey",
  "advancements.darkcraft.capitulo2.title": "Donde el agua aún canta",
  "advancements.darkcraft.capitulo2.description": "Encuentra la segunda página de la crónica junto a la Fuente Mayor",
  "advancements.darkcraft.capitulo3.title": "La verdad del Rey",
  "advancements.darkcraft.capitulo3.description": "Lee la tercera página junto al Rey caído",
  "advancements.darkcraft.capitulo4.title": "La Reina bajo el Altar",
  "advancements.darkcraft.capitulo4.description": "Encuentra la cuarta página a los pies del altar de la catedral",
  "advancements.darkcraft.golpe_final.title": "Bajo la luz dorada",
  "advancements.darkcraft.golpe_final.description": "Sobrevive al golpe final del Rey dentro de un círculo dorado",
 },
 "en": {
  "advancements.darkcraft.raiz.title": "Dark Craft",
  "advancements.darkcraft.raiz.description": "They say a cursed kingdom floats in the sky",
  "advancements.darkcraft.encontrar.title": "An Island Among the Clouds",
  "advancements.darkcraft.encontrar.description": "Set foot on the floating island of Valdrenn",
  "advancements.darkcraft.capitulo1.title": "The Chronicle Begins",
  "advancements.darkcraft.capitulo1.description": "Open the chest of chapter 1 and hear its story",
  "advancements.darkcraft.rey.title": "The King Has Fallen",
  "advancements.darkcraft.rey.description": "Defeat Corvin, the King Who Waits",
  "advancements.darkcraft.arbol.title": "The Gates of the Tree",
  "advancements.darkcraft.arbol.description": "Open the door of the Tree Hall with the King's key",
  "advancements.darkcraft.capitulo2.title": "Where the Water Still Sings",
  "advancements.darkcraft.capitulo2.description": "Find the chronicle's second page by the Great Fountain",
  "advancements.darkcraft.capitulo3.title": "The King's Truth",
  "advancements.darkcraft.capitulo3.description": "Read the third page beside the fallen King",
  "advancements.darkcraft.capitulo4.title": "The Queen Under the Altar",
  "advancements.darkcraft.capitulo4.description": "Find the fourth page at the foot of the cathedral altar",
  "advancements.darkcraft.golpe_final.title": "Beneath the Golden Light",
  "advancements.darkcraft.golpe_final.description": "Survive the King's final blow inside a golden circle",
 },
}

def build():
    out = {}
    for lang in ("es", "en"):
        d = dict(UI[lang])
        for n, c in CAP.items():
            d[f"darkcraft.cap.{n}.titulo"] = c[lang + "_t"]
            d[f"darkcraft.cap.{n}.cartel1"] = c[lang + "_s"][0]
            d[f"darkcraft.cap.{n}.cartel2"] = c[lang + "_s"][1]
            d[f"darkcraft.cap.{n}.paginas"] = str(len(c[lang]))
            for i, page in enumerate(c[lang]):
                d[f"darkcraft.cap.{n}.p{i+1}"] = page
        for k, v in PISTA[lang].items():
            d[f"darkcraft.pista.{k}"] = v
        for i, (en, es) in enumerate(NARRACION):
            d[f"darkcraft.narracion.1.{i+1}"] = es if lang == "es" else en
        for i, (en, es) in enumerate(REY):
            d[f"darkcraft.rey.voz.{i+1}"] = es if lang == "es" else en
        d.update(REY_UI[lang])
        d.update(LOGROS_UI[lang])
        out[lang] = d
    # el resto de idiomas: lo que falte se queda en ingles (y se avisa)
    for code, mod in otros_idiomas().items():
        t = mod.T
        d = dict(out["en"])
        for n, (titulo, carteles, paginas) in t["cap"].items():
            d[f"darkcraft.cap.{n}.titulo"] = titulo
            d[f"darkcraft.cap.{n}.cartel1"] = carteles[0]
            d[f"darkcraft.cap.{n}.cartel2"] = carteles[1]
            d[f"darkcraft.cap.{n}.paginas"] = str(len(paginas))
            for i, page in enumerate(paginas):
                d[f"darkcraft.cap.{n}.p{i+1}"] = page
        for k, v in t["pista"].items():
            d[f"darkcraft.pista.{k}"] = v
        for i, v in enumerate(t["narracion"]):
            d[f"darkcraft.narracion.1.{i+1}"] = v
        for i, v in enumerate(t["rey"]):
            d[f"darkcraft.rey.voz.{i+1}"] = v
        sobra = [k for k in t["ui"] if k not in out["en"]]
        if sobra:
            print(f"{code}: claves que no existen en ingles: {sobra}")
        d.update(t["ui"])
        faltan = [k for k in out["en"] if d[k] == out["en"][k] and k not in t["ui"] and not k.endswith(".paginas")
                  and not k.startswith("darkcraft.cap.") and not k.startswith("darkcraft.pista.")
                  and not k.startswith("darkcraft.narracion.") and not k.startswith("darkcraft.rey.voz.")]
        if faltan:
            print(f"{code}: {len(faltan)} textos sin traducir (salen en ingles): {faltan[:8]}")
        out[code] = d
    return out


# fichero de idioma de Minecraft de cada idioma
FICHEROS = {"es": ["es_es.json"], "en": ["en_us.json"], "ja": ["ja_jp.json"], "fr": ["fr_fr.json"],
            "de": ["de_de.json"], "pt": ["pt_br.json", "pt_pt.json"]}

if __name__ == "__main__":
    good = check()
    root = os.path.join(os.path.dirname(__file__), "..", "..", "src", "main", "resources", "assets", "darkcraft", "lang")
    data = build()
    for lang, d in data.items():
        for fname in FICHEROS.get(lang, [lang + ".json"]):
            with open(os.path.join(root, fname), "w", encoding="utf-8") as f:
                json.dump(d, f, ensure_ascii=False, indent=2)
    # texto para el narrador (lo usa ci/narracion/generar.sh)
    nar = os.path.join(os.path.dirname(__file__), "..", "narracion", "cap1.txt")
    os.makedirs(os.path.dirname(nar), exist_ok=True)
    with open(nar, "w", encoding="utf-8") as f:
        for (en, es), p in zip(NARRACION, PAUSAS):
            f.write(f"{en}|{p}\n")
    print("Paginas OK" if good else "HAY PAGINAS DEMASIADO LARGAS")
    sys.exit(0 if good else 1)
