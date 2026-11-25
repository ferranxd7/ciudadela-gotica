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
# --------------------------------------------------------------- lo que dice Ser Gavran (voz en ingles, subtitulos)
GAVRAN = [
 ("So... the King is dead. I felt it in the roots, the moment his heart stopped.",
  "Así que... el Rey ha muerto. Lo sentí en las raíces, en el instante en que su corazón se detuvo."),
 ("He was my king. My friend. And a fool who sold us all to the Shadow.",
  "Era mi rey. Mi amigo. Y un necio que nos vendió a todos a la Sombra."),
 ("But I swore an oath... and an oath does not die with the man who heard it.",
  "Pero hice un juramento... y un juramento no muere con el hombre que lo escuchó."),
 ("Draw your blade. Let us end this... as knights.",
  "Desenvaina. Acabemos con esto... como caballeros."),
 ("Too slow.",
  "Demasiado lento."),
 ("KNEEL!",
  "¡DE RODILLAS!"),
 ("Rise, my squires. Your oath is not yet fulfilled.",
  "Alzaos, mis escuderos. Vuestro juramento aún no se ha cumplido."),
 ("Is this the blade that felled my king?",
  "¿Es esta la espada que derribó a mi rey?"),
 ("You fight well. It changes nothing.",
  "Luchas bien. No cambia nada."),
 ("Rest now. The tree will keep you.",
  "Descansa ya. El árbol te guardará."),
 ("Enough! Tree of the Crown... take what is left of me!",
  "¡Basta! Árbol de la Corona... ¡toma lo que queda de mí!"),
 ("Judgment of the thorns!",
  "¡Juicio de las espinas!"),
 ("The roots run deeper than your courage.",
  "Las raíces llegan más hondo que tu valor."),
 ("Nowhere in this hall is beyond my reach!",
  "¡No hay rincón en esta sala fuera de mi alcance!"),
 ("The floor is mine. Climb... or be taken.",
  "El suelo es mío. Trepa... o te llevará."),
 ("The heart... you found the heart...",
  "El corazón... has encontrado el corazón..."),
 ("Well fought... The oath is broken... at last.",
  "Bien luchado... El juramento se ha roto... por fin."),
 ("Corvin, my friend... I kept my word. Let the tree... bloom.",
  "Corvin, amigo mío... cumplí mi palabra. Que el árbol... florezca."),
]
GAVRAN_UI = {
 "es": {
  "entity.darkcraft.gavran": "Ser Gavran, el Caballero Jurado",
  "darkcraft.gavran.nombre_corto": "Ser Gavran",
  "subtitles.darkcraft.gavran.voz": "Ser Gavran habla",
  "darkcraft.rey.titulo.33": "SER GAVRAN",
  "darkcraft.rey.subtitulo.33": "El Caballero Jurado",
  "darkcraft.rey.titulo.34": "SER GAVRAN",
  "darkcraft.rey.subtitulo.34": "Corazón del Árbol",
  "item.darkcraft.mandoble_juramento": "Mandoble del Juramento",
  "item.darkcraft.mandoble_juramento.lore": "La espada de Ser Gavran: raíces y espinas que aún recuerdan su juramento",
  "item.darkcraft.mandoble_juramento.combo1": "Shift + clic izquierdo: Raíces del Juramento (35 de energía)",
  "item.darkcraft.mandoble_juramento.combo2": "Ctrl + clic izquierdo: Juicio de Espinas (80 de energía)",
  "item.darkcraft.mandoble_juramento.dos": "A dos manos: con algo en la otra mano pega mucho menos",
  "darkcraft.mandoble.dos_manos": "El Mandoble del Juramento se empuña con las dos manos (vacía la otra mano)",
  "item.darkcraft.llave_hueso": "Llave de Hueso",
  "item.darkcraft.llave_hueso.lore": "Te mostrará el camino a las fortalezas que flotan más allá de las nubes",
  "item.darkcraft.llave_hueso.aun_no": "La llave tiembla en tu mano... pero su camino aún no se ha abierto (llegará en una próxima parte)",
  "darkcraft.rey.titulo.35": "JURAMENTO CUMPLIDO",
  "darkcraft.rey.subtitulo.35": "Ser Gavran descansa en el Árbol de la Corona",
  "darkcraft.gavran.continuara2": "Ser Gavran resiste en su rama",
  "darkcraft.gavran.continuara2.aviso": "Su muerte con honor llegará en la próxima parte del mod. Sal de la sala y vuelve a entrar para repetir el duelo.",
  "darkcraft.gavran.escudero": "Escudero del Juramento",
  "darkcraft.gavran.continuara.titulo": "Continuará",
  "darkcraft.gavran.continuara": "Ser Gavran vuelve a su rama",
  "darkcraft.gavran.continuara.aviso": "Su segunda fase llegará en la próxima parte del mod. Sal de la sala y vuelve a entrar para repetir la primera.",
  "block.darkcraft.raiz_gavran": "Raíz de Gavran",
  "darkcraft.ascensor.bloqueado": "El disco de runas no responde: arriba se libra una batalla",
  "death.attack.darkcraft.gavran": "%1$s cayó ante el mandoble de %2$s",
  "death.attack.darkcraft.gavran.player": "%1$s cayó ante el mandoble de %2$s",
  "death.attack.darkcraft.gavran_area": "%1$s fue atravesado por las raíces de %2$s",
  "death.attack.darkcraft.gavran_area.player": "%1$s fue atravesado por las raíces de %2$s",
  "subtitles.darkcraft.sfx.gavran_tajo": "Mandoble de Gavran",
  "subtitles.darkcraft.sfx.gavran_choque": "Choque de acero",
  "subtitles.darkcraft.sfx.gavran_embestida": "Gavran embiste",
  "subtitles.darkcraft.sfx.gavran_raices": "Brotan raíces",
  "subtitles.darkcraft.sfx.gavran_clavar": "El mandoble se clava en el suelo",
  "subtitles.darkcraft.sfx.gavran_invocar": "Los escuderos despiertan",
  "subtitles.darkcraft.sfx.gavran_paso": "Pasos de hierro",
  "subtitles.darkcraft.sfx.gavran_guardia_rota": "Guardia rota",
  "subtitles.darkcraft.sfx.gavran_salto": "Gavran salta",
  "subtitles.darkcraft.sfx.arbol_despierta": "El árbol despierta",
  "subtitles.darkcraft.sfx.hojas_cortantes": "Hojas cortantes",
  "subtitles.darkcraft.sfx.raices_atrapan": "Raíces que atrapan",
  "subtitles.darkcraft.sfx.espinas": "Brotan espinas",
  "subtitles.darkcraft.sfx.corteza_muro": "Se alza un muro de corteza",
  "subtitles.darkcraft.sfx.florecer": "El árbol florece",
  "subtitles.darkcraft.sfx.madera_cruje": "Cruje la madera",
  "subtitles.darkcraft.sfx.corazon_latido": "Late el corazón del árbol",
  "subtitles.darkcraft.sfx.hojas_disolver": "Se deshace en hojas",
  "item.darkcraft.amuleto_hueso": "Amuleto de Hueso",
  "item.darkcraft.amuleto_hueso.lore": "Abre la puerta de lo más alto.",
  "item.darkcraft.libro_acertijo": "El acertijo de hueso",
  "item.darkcraft.llave_hueso.uso": "En la mano marca el camino · Clic derecho: tira más fuerte",
  "block.darkcraft.reja_hueso": "Reja de hueso",
  "block.darkcraft.portal_sombra": "Portal de la Sombra",
  "darkcraft.llave.rastro": "La llave de hueso tira hacia el %s… (%s bloques)",
  "darkcraft.llave.cerca": "La llave tiembla: la fortaleza está cerca, al %s (%s bloques). Busca la sombra frente a su puerta.",
  "darkcraft.llave.puente": "¡La llave levanta una escalera de hueso y nubes hasta la puerta!",
  "darkcraft.llave.puente_ya": "La escalera de hueso ya está aquí. En su hueco, la corriente de nubes te sube.",
  "darkcraft.llave.puerta": "La llave encaja en la reja de hueso…",
  "darkcraft.llave.puerta_ya": "La puerta ya está abierta.",
  "darkcraft.llave.nada": "La llave no siente nada en este mundo.",
  "darkcraft.dir.n": "norte",
  "darkcraft.dir.ne": "noreste",
  "darkcraft.dir.e": "este",
  "darkcraft.dir.se": "sureste",
  "darkcraft.dir.s": "sur",
  "darkcraft.dir.so": "suroeste",
  "darkcraft.dir.o": "oeste",
  "darkcraft.dir.no": "noroeste",
  "darkcraft.corriente": "La corriente de nubes te sube  ·  Agáchate para bajar despacio",
  "darkcraft.portal.aun_no": "El umbral aún no te conoce.",
  "darkcraft.portal.ya": "El portal ya está abierto.",
  "darkcraft.amuleto.lejos": "El amuleto solo despierta en la Fortaleza de Hueso.",
  "darkcraft.amuleto.altar": "Llévalo al altar de lo más alto de la torre.",
  "darkcraft.cap6.libro": "Has recibido el capítulo VI de la crónica.",
  "darkcraft.perdido.anotado": "Libro perdido anotado: «%s»",
  "darkcraft.perdido.lore": "Un libro perdido de Valdrenn",
  "darkcraft.acertijo.lista": "Dos diamantes, tres lingotes de netherita y la llave de hueso (la llave se gasta).",
  "darkcraft.fortaleza.localizar": "La Fortaleza de Hueso más cercana: %s (a %s bloques; su reino está en %s)",
  "darkcraft.fortaleza.ninguna": "No hay ninguna Fortaleza de Hueso cerca.",
  "darkcraft.fortaleza.reiniciada": "Fortaleza reiniciada: puerta cerrada, sin portal y sin puente.",
  "darkcraft.ayuda.fortaleza": "La Fortaleza de Hueso",
  "darkcraft.ayuda.f_localizar": "dónde está la fortaleza",
  "darkcraft.ayuda.f_ir": "ir a su puerta / al pie de la escalera",
  "darkcraft.ayuda.f_puente": "levantar el puente / abrir la puerta / el portal (capítulo 6)",
  "darkcraft.ayuda.f_objetos": "darte el amuleto / el acertijo / los libros perdidos",
  "darkcraft.ayuda.f_reiniciar": "levantarla ya / dejarla como al principio",
  "darkcraft.mapa.lugar.fortaleza": "Fortaleza de Hueso",
  "darkcraft.mapa.lugar.fortaleza_puerta": "Puerta de Hueso",
  "darkcraft.mapa.lugar.fortaleza_homenaje": "Torre del Homenaje",
  "darkcraft.mapa.lugar.fortaleza_portal": "Portal de la Sombra",
  "darkcraft.mapa.lugar.fortaleza_escalera": "Escalera de Hueso",
  "darkcraft.mapa.lugar.fortaleza_cupula": "Jardín de la Cúpula",
  "darkcraft.fortaleza.llave_devuelta": "La Fortaleza de Hueso se ha alzado de nuevo sobre sus ruinas. Ser Gavran ya te entregó su llave: aquí la tienes otra vez.",
  "darkcraft.rey.titulo.36": "LA FORTALEZA DE HUESO",
  "darkcraft.rey.subtitulo.36": "La puerta se abre",
  "darkcraft.rey.titulo.37": "AMULETO DE HUESO",
  "darkcraft.rey.subtitulo.37": "Forjado con tus propias manos",
  "subtitles.darkcraft.sfx.llave_rastro": "La llave de hueso brilla",
  "subtitles.darkcraft.sfx.puente_hueso": "Se forma un puente de hueso",
  "subtitles.darkcraft.sfx.puerta_hueso": "Sube la reja de hueso",
  "subtitles.darkcraft.sfx.amuleto_forja": "Se forja el amuleto",
  "subtitles.darkcraft.sfx.portal_abre": "Se abre el portal de la Sombra",
  "advancements.darkcraft.fortaleza.title": "Sobre las nubes",
  "advancements.darkcraft.fortaleza.description": "Ve la Fortaleza de Hueso",
  "advancements.darkcraft.puente.title": "Un puente de hueso y nubes",
  "advancements.darkcraft.puente.description": "Levanta el puente con la llave de hueso",
  "advancements.darkcraft.puerta_hueso.title": "La puerta se abre",
  "advancements.darkcraft.puerta_hueso.description": "Abre la puerta de la fortaleza con la llave",
  "advancements.darkcraft.acertijo.title": "El acertijo de hueso",
  "advancements.darkcraft.acertijo.description": "Lee el libro del maestre Velan",
  "advancements.darkcraft.amuleto.title": "Forjado con tus manos",
  "advancements.darkcraft.amuleto.description": "Forja el Amuleto de Hueso",
  "advancements.darkcraft.portal.title": "El umbral",
  "advancements.darkcraft.portal.description": "Abre el portal de lo más alto",
  "advancements.darkcraft.capitulo6.title": "La puerta sobre las nubes",
  "advancements.darkcraft.capitulo6.description": "Escucha el capítulo VI de la crónica",
  "advancements.darkcraft.libro_perdido.title": "Lo que decía la gente",
  "advancements.darkcraft.libro_perdido.description": "Lee un libro perdido",
  "advancements.darkcraft.libros_perdidos.title": "La biblioteca perdida",
  "advancements.darkcraft.libros_perdidos.description": "Encuentra los dieciséis libros perdidos",
  "darkcraft.diario.fortaleza": "Vi la Fortaleza de Hueso flotando sobre las nubes. La llave no deja de temblar.",
  "darkcraft.diario.puente": "La llave levantó una escalera de hueso y nubes desde el suelo hasta su puerta.",
  "darkcraft.diario.puerta_hueso": "La reja de hueso subió sola cuando la llave la tocó.",
  "darkcraft.diario.acertijo": "El maestre Velan dejó un acertijo: hay que forjar un amuleto.",
  "darkcraft.diario.amuleto": "Forjé el Amuleto de Hueso. La llave se fundió con él.",
  "darkcraft.diario.portal": "Puse el amuleto en el altar y el arco se llenó de sombra.",
  "darkcraft.diario.capitulo6": "La Sombra me habló. Dice que siempre espera.",
  "darkcraft.diario.libro_perdido": "Encontré un libro perdido: alguien de Valdrenn dejó escrito lo que veía.",
  "darkcraft.diario.libros_perdidos": "Ya tengo todos los libros perdidos. Brumavela y Valdrenn me lo han contado todo.",
  "darkcraft.coleccion.alabarda": "La alabarda oxidada de un alabardero zombi. Aún corta.",
  "darkcraft.coleccion.libro_acertijo": "El acertijo en verso del maestre Velan. Su sello dibuja la forja del amuleto.",
  "darkcraft.coleccion.amuleto_hueso": "Hueso, netherita y diamante. Abre la puerta de lo más alto de la Fortaleza de Hueso.",
 },
 "en": {
  "entity.darkcraft.gavran": "Ser Gavran, the Sworn Knight",
  "darkcraft.gavran.nombre_corto": "Ser Gavran",
  "subtitles.darkcraft.gavran.voz": "Ser Gavran speaks",
  "darkcraft.rey.titulo.33": "SER GAVRAN",
  "darkcraft.rey.subtitulo.33": "The Sworn Knight",
  "darkcraft.rey.titulo.34": "SER GAVRAN",
  "darkcraft.rey.subtitulo.34": "Heart of the Tree",
  "item.darkcraft.mandoble_juramento": "Greatsword of the Oath",
  "item.darkcraft.mandoble_juramento.lore": "Ser Gavran's blade: roots and thorns that still remember his oath",
  "item.darkcraft.mandoble_juramento.combo1": "Shift + left click: Roots of the Oath (35 energy)",
  "item.darkcraft.mandoble_juramento.combo2": "Ctrl + left click: Judgment of Thorns (80 energy)",
  "item.darkcraft.mandoble_juramento.dos": "Two-handed: with something in your other hand it hits much weaker",
  "darkcraft.mandoble.dos_manos": "The Greatsword of the Oath needs both hands (empty your other hand)",
  "item.darkcraft.llave_hueso": "Bone Key",
  "item.darkcraft.llave_hueso.lore": "It will show you the way to the fortresses that float beyond the clouds",
  "item.darkcraft.llave_hueso.aun_no": "The key trembles in your hand... but its path has not opened yet (coming in a future part)",
  "darkcraft.rey.titulo.35": "OATH FULFILLED",
  "darkcraft.rey.subtitulo.35": "Ser Gavran rests in the Crown Tree",
  "darkcraft.gavran.continuara2": "Ser Gavran holds on to his branch",
  "darkcraft.gavran.continuara2.aviso": "His honourable death arrives in the next part of the mod. Leave the hall and come back in to fight the duel again.",
  "darkcraft.gavran.escudero": "Squire of the Oath",
  "darkcraft.gavran.continuara.titulo": "To be continued",
  "darkcraft.gavran.continuara": "Ser Gavran returns to his branch",
  "darkcraft.gavran.continuara.aviso": "His second phase arrives in the next part of the mod. Leave the hall and come back in to fight the first phase again.",
  "block.darkcraft.raiz_gavran": "Gavran's Root",
  "darkcraft.ascensor.bloqueado": "The rune disc does not answer: a battle rages above",
  "death.attack.darkcraft.gavran": "%1$s fell to %2$s's greatsword",
  "death.attack.darkcraft.gavran.player": "%1$s fell to %2$s's greatsword",
  "death.attack.darkcraft.gavran_area": "%1$s was impaled by %2$s's roots",
  "death.attack.darkcraft.gavran_area.player": "%1$s was impaled by %2$s's roots",
  "subtitles.darkcraft.sfx.gavran_tajo": "Gavran's greatsword",
  "subtitles.darkcraft.sfx.gavran_choque": "Clash of steel",
  "subtitles.darkcraft.sfx.gavran_embestida": "Gavran charges",
  "subtitles.darkcraft.sfx.gavran_raices": "Roots burst out",
  "subtitles.darkcraft.sfx.gavran_clavar": "The greatsword strikes the ground",
  "subtitles.darkcraft.sfx.gavran_invocar": "The squires awaken",
  "subtitles.darkcraft.sfx.gavran_paso": "Iron footsteps",
  "subtitles.darkcraft.sfx.gavran_guardia_rota": "Guard broken",
  "subtitles.darkcraft.sfx.gavran_salto": "Gavran leaps",
  "subtitles.darkcraft.sfx.arbol_despierta": "The tree awakens",
  "subtitles.darkcraft.sfx.hojas_cortantes": "Cutting leaves",
  "subtitles.darkcraft.sfx.raices_atrapan": "Grasping roots",
  "subtitles.darkcraft.sfx.espinas": "Thorns burst out",
  "subtitles.darkcraft.sfx.corteza_muro": "A bark wall rises",
  "subtitles.darkcraft.sfx.florecer": "The tree blooms",
  "subtitles.darkcraft.sfx.madera_cruje": "Wood creaks",
  "subtitles.darkcraft.sfx.corazon_latido": "The tree's heart beats",
  "subtitles.darkcraft.sfx.hojas_disolver": "Dissolves into leaves",
  "item.darkcraft.amuleto_hueso": "Bone Amulet",
  "item.darkcraft.amuleto_hueso.lore": "Opens the door at the very top.",
  "item.darkcraft.libro_acertijo": "The Bone Riddle",
  "item.darkcraft.llave_hueso.uso": "In hand it shows the way · Right-click: it pulls harder",
  "block.darkcraft.reja_hueso": "Bone Portcullis",
  "block.darkcraft.portal_sombra": "Portal of the Shadow",
  "darkcraft.llave.rastro": "The bone key pulls %s… (%s blocks)",
  "darkcraft.llave.cerca": "The key trembles: the fortress is close, to the %s (%s blocks). Seek the shadow before its gate.",
  "darkcraft.llave.puente": "The key raises a stairway of bone and cloud up to the gate!",
  "darkcraft.llave.puente_ya": "The bone stairway is already here. In its shaft, the cloud current lifts you.",
  "darkcraft.llave.puerta": "The key fits the bone portcullis…",
  "darkcraft.llave.puerta_ya": "The gate is already open.",
  "darkcraft.llave.nada": "The key feels nothing in this world.",
  "darkcraft.dir.n": "north",
  "darkcraft.dir.ne": "northeast",
  "darkcraft.dir.e": "east",
  "darkcraft.dir.se": "southeast",
  "darkcraft.dir.s": "south",
  "darkcraft.dir.so": "southwest",
  "darkcraft.dir.o": "west",
  "darkcraft.dir.no": "northwest",
  "darkcraft.corriente": "The cloud current lifts you  ·  Sneak to drift down",
  "darkcraft.portal.aun_no": "The threshold does not know you yet.",
  "darkcraft.portal.ya": "The portal is already open.",
  "darkcraft.amuleto.lejos": "The amulet only wakes in the Bone Fortress.",
  "darkcraft.amuleto.altar": "Take it to the altar at the top of the tower.",
  "darkcraft.cap6.libro": "You received chapter VI of the chronicle.",
  "darkcraft.perdido.anotado": "Lost book noted: “%s”",
  "darkcraft.perdido.lore": "A lost book of Valdrenn",
  "darkcraft.acertijo.lista": "Two diamonds, three netherite ingots and the bone key (the key is used up).",
  "darkcraft.fortaleza.localizar": "Nearest Bone Fortress: %s (%s blocks away; its kingdom is at %s)",
  "darkcraft.fortaleza.ninguna": "No Bone Fortress nearby.",
  "darkcraft.fortaleza.reiniciada": "Fortress reset: gate closed, no portal and no bridge.",
  "darkcraft.ayuda.fortaleza": "The Bone Fortress",
  "darkcraft.ayuda.f_localizar": "where the fortress is",
  "darkcraft.ayuda.f_ir": "go to its gate / to the foot of the stairway",
  "darkcraft.ayuda.f_puente": "raise the bridge / open the gate / the portal (chapter 6)",
  "darkcraft.ayuda.f_objetos": "give you the amulet / the riddle / the lost books",
  "darkcraft.ayuda.f_reiniciar": "build it now / reset it",
  "darkcraft.mapa.lugar.fortaleza": "Bone Fortress",
  "darkcraft.mapa.lugar.fortaleza_puerta": "Bone Gate",
  "darkcraft.mapa.lugar.fortaleza_homenaje": "The Keep",
  "darkcraft.mapa.lugar.fortaleza_portal": "Portal of the Shadow",
  "darkcraft.mapa.lugar.fortaleza_escalera": "Bone Stairway",
  "darkcraft.mapa.lugar.fortaleza_cupula": "Garden of the Dome",
  "darkcraft.fortaleza.llave_devuelta": "The Bone Fortress has risen anew from its ruins. Ser Gavran already handed you his key: here it is again.",
  "darkcraft.rey.titulo.36": "THE BONE FORTRESS",
  "darkcraft.rey.subtitulo.36": "The gate opens",
  "darkcraft.rey.titulo.37": "BONE AMULET",
  "darkcraft.rey.subtitulo.37": "Forged with your own hands",
  "subtitles.darkcraft.sfx.llave_rastro": "Bone key shimmers",
  "subtitles.darkcraft.sfx.puente_hueso": "A bone bridge forms",
  "subtitles.darkcraft.sfx.puerta_hueso": "Bone portcullis rises",
  "subtitles.darkcraft.sfx.amuleto_forja": "The amulet is forged",
  "subtitles.darkcraft.sfx.portal_abre": "The Shadow's portal opens",
  "advancements.darkcraft.fortaleza.title": "Above the Clouds",
  "advancements.darkcraft.fortaleza.description": "See the Bone Fortress",
  "advancements.darkcraft.puente.title": "A Bridge of Bone and Cloud",
  "advancements.darkcraft.puente.description": "Raise the bridge with the bone key",
  "advancements.darkcraft.puerta_hueso.title": "The Gate Opens",
  "advancements.darkcraft.puerta_hueso.description": "Open the fortress gate with the key",
  "advancements.darkcraft.acertijo.title": "The Bone Riddle",
  "advancements.darkcraft.acertijo.description": "Read Maester Velan's book",
  "advancements.darkcraft.amuleto.title": "Forged by Your Hands",
  "advancements.darkcraft.amuleto.description": "Forge the Bone Amulet",
  "advancements.darkcraft.portal.title": "The Threshold",
  "advancements.darkcraft.portal.description": "Open the portal at the very top",
  "advancements.darkcraft.capitulo6.title": "The Door Above the Clouds",
  "advancements.darkcraft.capitulo6.description": "Hear chapter VI of the chronicle",
  "advancements.darkcraft.libro_perdido.title": "What People Said",
  "advancements.darkcraft.libro_perdido.description": "Read a lost book",
  "advancements.darkcraft.libros_perdidos.title": "The Lost Library",
  "advancements.darkcraft.libros_perdidos.description": "Find all sixteen lost books",
  "darkcraft.diario.fortaleza": "I saw the Bone Fortress floating above the clouds. The key will not stop trembling.",
  "darkcraft.diario.puente": "The key raised a stairway of bone and cloud from the ground up to its gate.",
  "darkcraft.diario.puerta_hueso": "The bone portcullis rose by itself when the key touched it.",
  "darkcraft.diario.acertijo": "Maester Velan left a riddle: an amulet must be forged.",
  "darkcraft.diario.amuleto": "I forged the Bone Amulet. The key melted into it.",
  "darkcraft.diario.portal": "I placed the amulet on the altar and the arch filled with shadow.",
  "darkcraft.diario.capitulo6": "The Shadow spoke to me. It says it always waits.",
  "darkcraft.diario.libro_perdido": "I found a lost book: someone from Valdrenn wrote down what they saw.",
  "darkcraft.diario.libros_perdidos": "I have all the lost books. Brumavela and Valdrenn have told me everything.",
  "darkcraft.coleccion.alabarda": "The rusty halberd of a zombie halberdier. It still cuts.",
  "darkcraft.coleccion.libro_acertijo": "Maester Velan's riddle in verse. Its seal draws the forging of the amulet.",
  "darkcraft.coleccion.amuleto_hueso": "Bone, netherite and diamond. It opens the door at the top of the Bone Fortress.",
 },
}
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
  "darkcraft.espada.sin_energia": "Te falta maná (necesitas %s): golpea con el arma para llenarlo",
  "item.darkcraft.llave_arbol": "Llave del Árbol",
  "item.darkcraft.llave_arbol.lore": "Abre la gran puerta de la Sala del Árbol",
  "item.darkcraft.llave_arbol.aqui_no": "Esta llave abre la gran puerta de la Sala del Árbol, en lo alto de la torre del árbol",
  "block.darkcraft.sello_runico": "Sello rúnico",
  "entity.darkcraft.ascensor": "Disco de runas",
  "entity.darkcraft.puerta_arbol": "Puerta de la Sala del Árbol",
  "darkcraft.ascensor.sube": "El disco de runas despierta para subir  %s",
  "darkcraft.ascensor.baja": "El disco de runas despierta para bajar  %s",
  "darkcraft.ascensor.llama": "Llamando al disco de runas  %s",
  "darkcraft.ascensor.hueso.sube": "La placa de hueso despierta para subir  %s",
  "darkcraft.ascensor.hueso.baja": "La placa de hueso despierta para bajar  %s",
  "darkcraft.ascensor.hueso.llama": "Llamando a la placa de hueso  %s",
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
  "darkcraft.espada.sin_energia": "Not enough mana (you need %s): strike with your weapon to fill it",
  "item.darkcraft.llave_arbol": "Key of the Tree",
  "item.darkcraft.llave_arbol.lore": "Opens the great door of the Tree Hall",
  "item.darkcraft.llave_arbol.aqui_no": "This key opens the great door of the Tree Hall, at the top of the tree tower",
  "block.darkcraft.sello_runico": "Runic Seal",
  "entity.darkcraft.ascensor": "Rune Disc",
  "entity.darkcraft.puerta_arbol": "Door of the Tree Hall",
  "darkcraft.ascensor.sube": "The rune disc awakens to rise  %s",
  "darkcraft.ascensor.baja": "The rune disc awakens to descend  %s",
  "darkcraft.ascensor.llama": "Calling the rune disc  %s",
  "darkcraft.ascensor.hueso.sube": "The bone plate awakens to rise  %s",
  "darkcraft.ascensor.hueso.baja": "The bone plate awakens to descend  %s",
  "darkcraft.ascensor.hueso.llama": "Calling the bone plate  %s",
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

# Narracion del capitulo 5 (el cronista Aldous se despide; misma voz que la del capitulo 1)
NARRACION5 = [
 ("Sir Gavran has fallen. His oath is broken... and with it, the chain that bound the Shadow to these stones.",
  "Ser Gavran ha caído. Su juramento se ha roto... y con él, la cadena que ataba a la Sombra a estas piedras."),
 ("Thank you. For a hundred years, no one has read my words.",
  "Gracias. En cien años, nadie había leído mis palabras."),
 ("I can feel the ink slipping from my fingers. I think, at last, I may rest.",
  "Siento cómo se me escapa la tinta de los dedos. Creo que, por fin, podré descansar."),
 ("But listen well. The Shadow does not live here. This kingdom was only its door.",
  "Pero escucha bien. La Sombra no vive aquí. Este reino solo era su puerta."),
 ("It waits much farther away... where the storm is born.",
  "Espera mucho más lejos... allí donde nace la tormenta."),
 ("Gavran kept a key that is not made of iron, but of bone.",
  "Gavran guardaba una llave que no es de hierro, sino de hueso."),
 ("Use it, and it will show you the way to fortresses that float beyond the clouds.",
  "Úsala, y te mostrará el camino hacia fortalezas que flotan más allá de las nubes."),
 ("Beyond them, they say, rises a mountain that touches the stars... where the bonfires of the fallen burn.",
  "Dicen que tras ellas se alza una montaña que toca las estrellas... donde arden las hogueras de los caídos."),
 ("I cannot walk with you any longer. What comes next is yours.",
  "Ya no puedo acompañarte. Lo que venga ahora es cosa tuya."),
 ("Search. Fight. Fall... and rise again. No one else will write your story.",
  "Busca. Lucha. Cae... y vuelve a levantarte. Nadie más va a escribir tu historia."),
 ("Here ends the chronicle of Valdrenn. May your steel never dull... and your fire never fade.",
  "Aquí termina la crónica de Valdrenn. Que tu acero no se melle... y tu fuego nunca se apague."),
]
PAUSAS5 = [1.2, 0.8, 1.6, 0.8, 1.6, 0.7, 1.2, 1.6, 0.8, 1.6, 0.0]

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
  "darkcraft.ayuda.titulo": "Comandos de Dark Craft (haz clic en uno para escribirlo)",
  "darkcraft.ayuda.valdrenn": "Reino de Valdrenn:",
  "darkcraft.ayuda.clic": "Clic para escribirlo",
  "darkcraft.ayuda.localizar": "dónde está el Reino de Valdrenn más cercano",
  "darkcraft.ayuda.ir": "te lleva al Reino de Valdrenn más cercano",
  "darkcraft.ayuda.capitulo": "vuelve a ver la cinemática de un capítulo",
  "darkcraft.ayuda.libro": "te da el libro de un capítulo",
  "darkcraft.ayuda.mapa": "te da el mapa de Valdrenn",
  "darkcraft.ayuda.cofre": "pone delante de ti el cofre de un capítulo",
  "darkcraft.ayuda.reiniciar": "tu historia en Valdrenn empieza de nuevo",
  "darkcraft.ayuda.rey": "pruebas con el Rey Corvin",
  "darkcraft.ayuda.arbol": "pruebas con la torre-árbol y su puerta",
  "darkcraft.ayuda.gavran": "pruebas con Ser Gavran",
  "darkcraft.ayuda.ayuda": "esta lista",
  "entity.darkcraft.caballero_espada": "Caballero Zombi",
  "entity.darkcraft.caballero_alabarda": "Alabardero Zombi",
  "entity.darkcraft.caballero_ballesta": "Ballestero Zombi",
  "item.darkcraft.alabarda": "Alabarda oxidada",
  "item.darkcraft.caballero_espada_spawn_egg": "Huevo de Caballero Zombi",
  "item.darkcraft.caballero_alabarda_spawn_egg": "Huevo de Alabardero Zombi",
  "item.darkcraft.caballero_ballesta_spawn_egg": "Huevo de Ballestero Zombi",
  "subtitles.darkcraft.sfx.zombi_caballero_grunido": "Caballero zombi gruñe",
  "subtitles.darkcraft.sfx.zombi_caballero_dolor": "Caballero zombi herido",
  "subtitles.darkcraft.sfx.zombi_caballero_muerte": "Caballero zombi muere",
  "subtitles.darkcraft.sfx.armadura_paso": "Pasos con armadura",
  "subtitles.darkcraft.sfx.escudo_golpe": "Golpe en el escudo",
  "subtitles.darkcraft.sfx.alabarda_tajo": "Tajo de alabarda",
  "darkcraft.bestiario.caballero_espada.desc": "Guardias del Rey que no supieron morir. Se cubren con el escudo si les atacas de frente: rodéalos.",
  "darkcraft.bestiario.caballero_alabarda.desc": "Su alabarda llega más lejos que tu espada. Es lenta, pero cada tajo duele.",
  "darkcraft.bestiario.caballero_ballesta.desc": "Disparan desde lejos y recargan despacio. Acércate entre disparo y disparo.",
  "advancements.darkcraft.caballero.title": "La guardia que no duerme",
  "advancements.darkcraft.caballero.description": "Vence a un caballero zombi",
  "advancements.darkcraft.caballeros_50.title": "Cincuenta yelmos",
  "advancements.darkcraft.caballeros_50.description": "Vence a cincuenta caballeros zombi",
  "darkcraft.ayuda.caballeros": "caballeros zombi (un grupo cerca / quitar los de alrededor)",
  "darkcraft.diario.caballero": "Me enfrenté a la guardia muerta del Rey y gané.",
  "darkcraft.diario.caballeros_50": "Cincuenta yelmos oxidados quedan a mi paso.",
  "darkcraft.ayuda.espejo": "el Espejo de la Sombra (despertar / dormir / revelar todo)",
  "key.categories.darkcraft": "Dark Craft",
  "key.darkcraft.libro": "Libro de la Sombra (mapa)",
  "key.darkcraft.espejo": "Mostrar u ocultar el espejo",
  "key.darkcraft.acercar": "Espejo: acercar",
  "key.darkcraft.alejar": "Espejo: alejar",
  "darkcraft.espejo.dormido": "El espejo aún duerme… Encuentra el Reino de Valdrenn.",
  "darkcraft.espejo.mostrar": "Espejo de la Sombra: visible",
  "darkcraft.espejo.ocultar": "Espejo de la Sombra: oculto",
  "darkcraft.espejo.despierta": "El Espejo de la Sombra despierta…",
  "darkcraft.espejo.despierta.teclas": "%s: el Libro de la Sombra   ·   %s: ocultar el espejo",
  "darkcraft.espejo.desconocido": "Tierras desconocidas",
  "biome.darkcraft.tormenta": "La tormenta de Valdrenn",
  "biome.darkcraft.volcanes": "Los volcanes del Rey",
  "biome.darkcraft.fortaleza": "Los cielos de la Fortaleza",
  "darkcraft.espejo.cofre": "Crónica, capítulo %s",
  "darkcraft.sombra.titulo": "Libro de la Sombra",
  "darkcraft.sombra.historia": "Historia",
  "darkcraft.sombra.mapa": "Mapa",
  "darkcraft.sombra.logros": "Logros",
  "darkcraft.sombra.cronica": "Crónica de Valdrenn",
  "darkcraft.sombra.sin_leer": "Aún sin leer",
  "darkcraft.sombra.libros": "Libros perdidos",
  "darkcraft.sombra.libros.ninguno": "Aún no has encontrado ninguno. Dicen que la gente de Valdrenn dejó escritos por todas partes.",
  "darkcraft.sombra.diario": "Diario",
  "darkcraft.sombra.diario.vacio": "Todavía no hay nada escrito.",
  "darkcraft.sombra.dia": "Día %s",
  "darkcraft.sombra.intro": "Lo que el Espejo de la Sombra ve queda escrito en estas páginas: la crónica del reino, los libros perdidos y tu propio diario. La cinta del Mapa enseña lo que has explorado y la de Logros lo que has conseguido.",
  "darkcraft.sombra.pagina": "%s / %s",
  "darkcraft.sombra.mapa.centrar": "Centrar",
  "darkcraft.sombra.mapa.nueva": "Nueva marca",
  "darkcraft.sombra.mapa.tamano": "Espejo: %s",
  "darkcraft.sombra.mapa.tam.0": "pequeño",
  "darkcraft.sombra.mapa.tam.1": "mediano",
  "darkcraft.sombra.mapa.tam.2": "grande",
  "darkcraft.sombra.mapa.ayuda": "Arrastra: mover · Rueda: zoom · Clic derecho: marcar",
  "darkcraft.sombra.dim.minecraft.overworld": "Superficie",
  "darkcraft.sombra.dim.minecraft.the_nether": "Nether",
  "darkcraft.sombra.dim.minecraft.the_end": "El Fin",
  "darkcraft.sombra.dim.darkcraft.volcanes": "Volcanes de Corvin",
  "darkcraft.sombra.marca.titulo": "Nueva marca",
  "darkcraft.sombra.marca.nombre": "Nombre",
  "darkcraft.sombra.marca.icono": "Símbolo",
  "darkcraft.sombra.marca.color": "Tinta",
  "darkcraft.sombra.marca.escribir": "Escribir",
  "darkcraft.sombra.marca.cancelar": "Cancelar",
  "darkcraft.sombra.marca.defecto": "Mi marca",
  "darkcraft.sombra.marca.borrar": "¿Borrar «%s»?",
  "darkcraft.sombra.marca.si": "Borrar",
  "darkcraft.sombra.marca.no": "Dejarla",
  "darkcraft.sombra.marca.quitar": "Clic derecho: borrar",
  "darkcraft.sombra.coleccion": "Colección",
  "darkcraft.sombra.bestiario": "Bestiario",
  "darkcraft.sombra.secreto": "Secreto",
  "darkcraft.sombra.conseguido": "Conseguido",
  "darkcraft.sombra.pendiente": "Aún no",
  "darkcraft.sombra.marco.0": "Tarea",
  "darkcraft.sombra.marco.1": "Desafío",
  "darkcraft.sombra.marco.2": "Meta",
  "darkcraft.sombra.coleccion.falta": "Aún no lo has encontrado.",
  "darkcraft.sombra.bestiario.falta": "Aún no has vencido a esta criatura.",
  "darkcraft.sombra.bestiario.derrotado": "DERROTADO",
  "darkcraft.sombra.jefe.vivo": "Te espera",
  "darkcraft.sombra.cofre.leido": "Ya leído",
  "darkcraft.sombra.cofre.pendiente": "Aún sin abrir",
  "darkcraft.coleccion.mapa_valdrenn": "Un mapa de pergamino del reino. Marca dónde espera la siguiente página de la crónica.",
  "darkcraft.coleccion.libro": "Capítulo %s de la crónica de Aldous, el cronista del rey. Puedes releerlo en la cinta de Historia.",
  "darkcraft.coleccion.espada_diestra": "La espada de la mano derecha del Rey Corvin. Su fuego no se apagó con él.",
  "darkcraft.coleccion.espada_siniestra": "La espada de la mano izquierda del Rey Corvin. Juntas, las dos despiertan su poder.",
  "darkcraft.coleccion.llave_arbol": "La llave que guardaba el Rey. Abre las puertas de la Sala del Árbol.",
  "darkcraft.coleccion.mandoble_juramento": "El mandoble de Ser Gavran. Aún guarda el poder de las raíces y las espinas.",
  "darkcraft.coleccion.llave_hueso": "Una llave tallada en hueso. Algo, muy lejos, espera ser abierto.",
  "darkcraft.bestiario.escudero.desc": "Los huesos que Ser Gavran levanta del suelo para que luchen a su lado. Llevan su verde.",
  "darkcraft.bestiario.rey.desc": "El rey de Valdrenn esperó siglos en su trono. Lucha con dos espadas de fuego y, herido, arrastra a quien le desafía a sus volcanes.",
  "darkcraft.bestiario.gavran.desc": "El campeón y mejor amigo del Rey. Cumplió su juramento hasta el final, con raíces, espinas y un corazón de árbol.",
  "darkcraft.diario.espejo": "Al pisar Valdrenn, un espejo de obsidiana despertó a mi lado. Se llama a sí mismo el Espejo de la Sombra y ve todo lo que yo veo.",
  "darkcraft.diario.capitulo1": "Encontré la primera página de la crónica de Aldous.",
  "darkcraft.diario.capitulo2": "Junto a la Fuente Mayor leí la segunda página.",
  "darkcraft.diario.rey": "Vencí a Corvin, el Rey que Espera. Su trono está vacío.",
  "darkcraft.diario.golpe_final": "Sobreviví a su golpe final bajo la luz dorada.",
  "darkcraft.diario.capitulo3": "Junto al Rey caído leí la verdad del pacto.",
  "darkcraft.diario.capitulo4": "Bajo el altar de la catedral encontré a la Reina.",
  "darkcraft.diario.arbol": "La llave del Rey abrió las puertas de la Sala del Árbol.",
  "darkcraft.diario.gavran_guardia": "Rompí la guardia de Ser Gavran.",
  "darkcraft.diario.gavran_fase1": "Aguanté la primera rama del duelo.",
  "darkcraft.diario.gavran_corazon": "Golpeé el corazón del árbol que brillaba en su pecho.",
  "darkcraft.diario.gavran_juicio": "Me escondí tras un muro de corteza y sobreviví al Juicio de las espinas.",
  "darkcraft.diario.gavran_fase2": "Aguanté la segunda rama del duelo.",
  "darkcraft.diario.gavran": "Ser Gavran cayó con honor. El árbol floreció.",
  "darkcraft.diario.capitulo5": "A sus pies leí la última página. Aldous se despidió.",
  "darkcraft.diario.cartografo": "Ya conozco cada rincón de Valdrenn.",
  "advancements.darkcraft.libro_sombra.title": "Lo que el espejo recuerda",
  "advancements.darkcraft.libro_sombra.description": "Abre el Libro de la Sombra",
  "advancements.darkcraft.primera_marca.title": "Tinta y sombra",
  "advancements.darkcraft.primera_marca.description": "Escribe tu primera marca en el mapa",
  "advancements.darkcraft.cartografo.title": "Cartógrafo de Valdrenn",
  "advancements.darkcraft.cartografo.description": "Descubre todos los lugares del Reino de Valdrenn",
  "subtitles.darkcraft.sfx.espejo_despierta": "El espejo despierta",
  "subtitles.darkcraft.sfx.pluma_escribe": "Una pluma escribe",
  "subtitles.darkcraft.sfx.libro_abre": "Se abre un libro",
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
  "darkcraft.ayuda.titulo": "Dark Craft commands (click one to type it)",
  "darkcraft.ayuda.valdrenn": "Kingdom of Valdrenn:",
  "darkcraft.ayuda.clic": "Click to type it",
  "darkcraft.ayuda.localizar": "where the nearest Kingdom of Valdrenn is",
  "darkcraft.ayuda.ir": "takes you to the nearest Kingdom of Valdrenn",
  "darkcraft.ayuda.capitulo": "watch a chapter's cinematic again",
  "darkcraft.ayuda.libro": "gives you a chapter's book",
  "darkcraft.ayuda.mapa": "gives you the map of Valdrenn",
  "darkcraft.ayuda.cofre": "places a chapter's chest in front of you",
  "darkcraft.ayuda.reiniciar": "your story in Valdrenn starts over",
  "darkcraft.ayuda.rey": "tests with King Corvin",
  "darkcraft.ayuda.arbol": "tests with the tree tower and its door",
  "darkcraft.ayuda.gavran": "tests with Ser Gavran",
  "darkcraft.ayuda.ayuda": "this list",
  "entity.darkcraft.caballero_espada": "Zombie Knight",
  "entity.darkcraft.caballero_alabarda": "Zombie Halberdier",
  "entity.darkcraft.caballero_ballesta": "Zombie Crossbowman",
  "item.darkcraft.alabarda": "Rusty Halberd",
  "item.darkcraft.caballero_espada_spawn_egg": "Zombie Knight Spawn Egg",
  "item.darkcraft.caballero_alabarda_spawn_egg": "Zombie Halberdier Spawn Egg",
  "item.darkcraft.caballero_ballesta_spawn_egg": "Zombie Crossbowman Spawn Egg",
  "subtitles.darkcraft.sfx.zombi_caballero_grunido": "Zombie Knight growls",
  "subtitles.darkcraft.sfx.zombi_caballero_dolor": "Zombie Knight hurts",
  "subtitles.darkcraft.sfx.zombi_caballero_muerte": "Zombie Knight dies",
  "subtitles.darkcraft.sfx.armadura_paso": "Armored footsteps",
  "subtitles.darkcraft.sfx.escudo_golpe": "Shield blocks",
  "subtitles.darkcraft.sfx.alabarda_tajo": "Halberd swings",
  "darkcraft.bestiario.caballero_espada.desc": "The King's guards who never learned how to die. They raise their shields when struck from the front: circle around them.",
  "darkcraft.bestiario.caballero_alabarda.desc": "Their halberd reaches farther than your sword. It is slow, but every swing hurts.",
  "darkcraft.bestiario.caballero_ballesta.desc": "They shoot from afar and reload slowly. Close in between shots.",
  "advancements.darkcraft.caballero.title": "The Sleepless Guard",
  "advancements.darkcraft.caballero.description": "Defeat a zombie knight",
  "advancements.darkcraft.caballeros_50.title": "Fifty Helms",
  "advancements.darkcraft.caballeros_50.description": "Defeat fifty zombie knights",
  "darkcraft.ayuda.caballeros": "zombie knights (a group nearby / remove the ones around)",
  "darkcraft.diario.caballero": "I faced the King's dead guard and won.",
  "darkcraft.diario.caballeros_50": "Fifty rusty helms lie in my wake.",
  "darkcraft.ayuda.espejo": "the Mirror of the Shadow (awaken / sleep / reveal all)",
  "key.categories.darkcraft": "Dark Craft",
  "key.darkcraft.libro": "Book of the Shadow (map)",
  "key.darkcraft.espejo": "Show or hide the mirror",
  "key.darkcraft.acercar": "Mirror: zoom in",
  "key.darkcraft.alejar": "Mirror: zoom out",
  "darkcraft.espejo.dormido": "The mirror still sleeps… Find the Kingdom of Valdrenn.",
  "darkcraft.espejo.mostrar": "Mirror of the Shadow: shown",
  "darkcraft.espejo.ocultar": "Mirror of the Shadow: hidden",
  "darkcraft.espejo.despierta": "The Mirror of the Shadow awakens…",
  "darkcraft.espejo.despierta.teclas": "%s: the Book of the Shadow   ·   %s: hide the mirror",
  "darkcraft.espejo.desconocido": "Unknown lands",
  "biome.darkcraft.tormenta": "Valdrenn's Storm",
  "biome.darkcraft.volcanes": "The King's Volcanoes",
  "biome.darkcraft.fortaleza": "The Fortress Skies",
  "darkcraft.espejo.cofre": "Chronicle, chapter %s",
  "darkcraft.sombra.titulo": "Book of the Shadow",
  "darkcraft.sombra.historia": "History",
  "darkcraft.sombra.mapa": "Map",
  "darkcraft.sombra.logros": "Achievements",
  "darkcraft.sombra.cronica": "Chronicle of Valdrenn",
  "darkcraft.sombra.sin_leer": "Not read yet",
  "darkcraft.sombra.libros": "Lost books",
  "darkcraft.sombra.libros.ninguno": "You haven't found any yet. They say the people of Valdrenn left writings everywhere.",
  "darkcraft.sombra.diario": "Journal",
  "darkcraft.sombra.diario.vacio": "Nothing has been written yet.",
  "darkcraft.sombra.dia": "Day %s",
  "darkcraft.sombra.intro": "What the Mirror of the Shadow sees is written in these pages: the chronicle of the kingdom, the lost books and your own journal. The Map ribbon shows what you have explored, the Achievements ribbon what you have achieved.",
  "darkcraft.sombra.pagina": "%s / %s",
  "darkcraft.sombra.mapa.centrar": "Center",
  "darkcraft.sombra.mapa.nueva": "New mark",
  "darkcraft.sombra.mapa.tamano": "Mirror: %s",
  "darkcraft.sombra.mapa.tam.0": "small",
  "darkcraft.sombra.mapa.tam.1": "medium",
  "darkcraft.sombra.mapa.tam.2": "large",
  "darkcraft.sombra.mapa.ayuda": "Drag: move · Wheel: zoom · Right-click: mark",
  "darkcraft.sombra.dim.minecraft.overworld": "Overworld",
  "darkcraft.sombra.dim.minecraft.the_nether": "Nether",
  "darkcraft.sombra.dim.minecraft.the_end": "The End",
  "darkcraft.sombra.dim.darkcraft.volcanes": "Corvin's Volcanoes",
  "darkcraft.sombra.marca.titulo": "New mark",
  "darkcraft.sombra.marca.nombre": "Name",
  "darkcraft.sombra.marca.icono": "Symbol",
  "darkcraft.sombra.marca.color": "Ink",
  "darkcraft.sombra.marca.escribir": "Write",
  "darkcraft.sombra.marca.cancelar": "Cancel",
  "darkcraft.sombra.marca.defecto": "My mark",
  "darkcraft.sombra.marca.borrar": "Erase “%s”?",
  "darkcraft.sombra.marca.si": "Erase",
  "darkcraft.sombra.marca.no": "Keep it",
  "darkcraft.sombra.marca.quitar": "Right-click: erase",
  "darkcraft.sombra.coleccion": "Collection",
  "darkcraft.sombra.bestiario": "Bestiary",
  "darkcraft.sombra.secreto": "Secret",
  "darkcraft.sombra.conseguido": "Achieved",
  "darkcraft.sombra.pendiente": "Not yet",
  "darkcraft.sombra.marco.0": "Task",
  "darkcraft.sombra.marco.1": "Challenge",
  "darkcraft.sombra.marco.2": "Goal",
  "darkcraft.sombra.coleccion.falta": "You haven't found it yet.",
  "darkcraft.sombra.bestiario.falta": "You have not defeated this creature yet.",
  "darkcraft.sombra.bestiario.derrotado": "DEFEATED",
  "darkcraft.sombra.jefe.vivo": "Awaits you",
  "darkcraft.sombra.cofre.leido": "Already read",
  "darkcraft.sombra.cofre.pendiente": "Not opened yet",
  "darkcraft.coleccion.mapa_valdrenn": "A parchment map of the kingdom. It marks where the next page of the chronicle awaits.",
  "darkcraft.coleccion.libro": "Chapter %s of the chronicle of Aldous, the king's chronicler. You can read it again in the History ribbon.",
  "darkcraft.coleccion.espada_diestra": "King Corvin's right-hand sword. Its fire did not die with him.",
  "darkcraft.coleccion.espada_siniestra": "King Corvin's left-hand sword. Together, the two awaken their power.",
  "darkcraft.coleccion.llave_arbol": "The key the King kept. It opens the doors of the Tree Hall.",
  "darkcraft.coleccion.mandoble_juramento": "Ser Gavran's greatsword. It still holds the power of roots and thorns.",
  "darkcraft.coleccion.llave_hueso": "A key carved from bone. Something, far away, waits to be opened.",
  "darkcraft.bestiario.escudero.desc": "The bones Ser Gavran raises from the ground to fight at his side. They wear his green.",
  "darkcraft.bestiario.rey.desc": "The king of Valdrenn waited centuries on his throne. He fights with two swords of fire and, when wounded, drags his challengers into his volcanoes.",
  "darkcraft.bestiario.gavran.desc": "The King's champion and closest friend. He kept his oath to the end, with roots, thorns and a heart of wood.",
  "darkcraft.diario.espejo": "When I set foot on Valdrenn, a mirror of obsidian awoke beside me. It calls itself the Mirror of the Shadow and sees all that I see.",
  "darkcraft.diario.capitulo1": "I found the first page of Aldous's chronicle.",
  "darkcraft.diario.capitulo2": "By the Great Fountain I read the second page.",
  "darkcraft.diario.rey": "I defeated Corvin, the King Who Waits. His throne is empty.",
  "darkcraft.diario.golpe_final": "I survived his final blow under the golden light.",
  "darkcraft.diario.capitulo3": "Beside the fallen King I read the truth of the pact.",
  "darkcraft.diario.capitulo4": "Beneath the cathedral altar I found the Queen.",
  "darkcraft.diario.arbol": "The King's key opened the doors of the Tree Hall.",
  "darkcraft.diario.gavran_guardia": "I broke Ser Gavran's guard.",
  "darkcraft.diario.gavran_fase1": "I endured the first branch of the duel.",
  "darkcraft.diario.gavran_corazon": "I struck the heart of the tree glowing in his chest.",
  "darkcraft.diario.gavran_juicio": "I hid behind a wall of bark and survived the Judgment of the Thorns.",
  "darkcraft.diario.gavran_fase2": "I endured the second branch of the duel.",
  "darkcraft.diario.gavran": "Ser Gavran fell with honor. The tree blossomed.",
  "darkcraft.diario.capitulo5": "At his feet I read the last page. Aldous said farewell.",
  "darkcraft.diario.cartografo": "I now know every corner of Valdrenn.",
  "advancements.darkcraft.libro_sombra.title": "What the Mirror Remembers",
  "advancements.darkcraft.libro_sombra.description": "Open the Book of the Shadow",
  "advancements.darkcraft.primera_marca.title": "Ink and Shadow",
  "advancements.darkcraft.primera_marca.description": "Write your first mark on the map",
  "advancements.darkcraft.cartografo.title": "Cartographer of Valdrenn",
  "advancements.darkcraft.cartografo.description": "Discover every place in the Kingdom of Valdrenn",
  "subtitles.darkcraft.sfx.espejo_despierta": "Mirror awakens",
  "subtitles.darkcraft.sfx.pluma_escribe": "Quill writes",
  "subtitles.darkcraft.sfx.libro_abre": "Book opens",
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
  "advancements.darkcraft.gavran_guardia.title": "Juramento quebrado",
  "advancements.darkcraft.gavran_guardia.description": "Rompe la guardia de Ser Gavran",
  "advancements.darkcraft.gavran_fase1.title": "La primera rama",
  "advancements.darkcraft.gavran_fase1.description": "Aguanta la primera fase del duelo con Ser Gavran",
  "advancements.darkcraft.gavran_fase2.title": "La segunda rama",
  "advancements.darkcraft.gavran_fase2.description": "Aguanta la segunda fase del duelo con Ser Gavran",
  "advancements.darkcraft.gavran_corazon.title": "El corazón del árbol",
  "advancements.darkcraft.gavran_corazon.description": "Golpea el corazón que brilla en el pecho de Ser Gavran",
  "advancements.darkcraft.gavran_juicio.title": "Tras el muro de corteza",
  "advancements.darkcraft.gavran_juicio.description": "Sobrevive al Juicio de las espinas escondido tras un muro",
  "advancements.darkcraft.gavran.title": "Ser Gavran ha caído",
  "advancements.darkcraft.gavran.description": "Vence a Ser Gavran, el Caballero Jurado",
  "advancements.darkcraft.capitulo5.title": "El Último Juramento",
  "advancements.darkcraft.capitulo5.description": "Lee la última página de la crónica a los pies de Ser Gavran",
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
  "advancements.darkcraft.gavran_guardia.title": "Broken Oath",
  "advancements.darkcraft.gavran_guardia.description": "Break Ser Gavran's guard",
  "advancements.darkcraft.gavran_fase1.title": "The First Branch",
  "advancements.darkcraft.gavran_fase1.description": "Endure the first phase of the duel with Ser Gavran",
  "advancements.darkcraft.gavran_fase2.title": "The Second Branch",
  "advancements.darkcraft.gavran_fase2.description": "Endure the second phase of the duel with Ser Gavran",
  "advancements.darkcraft.gavran_corazon.title": "Heart of the Tree",
  "advancements.darkcraft.gavran_corazon.description": "Strike the heart glowing in Ser Gavran's chest",
  "advancements.darkcraft.gavran_juicio.title": "Behind the Bark Wall",
  "advancements.darkcraft.gavran_juicio.description": "Survive the Judgment of the Thorns hidden behind a wall",
  "advancements.darkcraft.gavran.title": "Ser Gavran Has Fallen",
  "advancements.darkcraft.gavran.description": "Defeat Ser Gavran, the Sworn Knight",
  "advancements.darkcraft.capitulo5.title": "The Last Oath",
  "advancements.darkcraft.capitulo5.description": "Read the last page of the chronicle at Ser Gavran's feet",
 },
}

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import perdidos  # noqa: E402
import rol  # noqa: E402


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
        for i, (en, es) in enumerate(NARRACION5):
            d[f"darkcraft.narracion.5.{i+1}"] = es if lang == "es" else en
        for i, (en, es) in enumerate(REY):
            d[f"darkcraft.rey.voz.{i+1}"] = es if lang == "es" else en
        for i, (en, es) in enumerate(GAVRAN):
            d[f"darkcraft.gavran.voz.{i+1}"] = es if lang == "es" else en
        d.update(REY_UI[lang])
        d.update(GAVRAN_UI[lang])
        d.update(LOGROS_UI[lang])
        d.update(perdidos.claves(lang))
        d.update(rol.claves(lang))
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
        for i, v in enumerate(t.get("narracion5", [])):
            d[f"darkcraft.narracion.5.{i+1}"] = v
        if len(t.get("narracion5", [])) != len(NARRACION5):
            print(f"{code}: faltan frases de la narracion del capitulo 5 ({len(t.get('narracion5', []))} de {len(NARRACION5)})")
        for i, v in enumerate(t["rey"]):
            d[f"darkcraft.rey.voz.{i+1}"] = v
        for i, v in enumerate(t.get("gavran", [])):
            d[f"darkcraft.gavran.voz.{i+1}"] = v
        if len(t.get("gavran", [])) != len(GAVRAN):
            print(f"{code}: faltan frases de Ser Gavran ({len(t.get('gavran', []))} de {len(GAVRAN)})")
        sobra = [k for k in t["ui"] if k not in out["en"]]
        if sobra:
            print(f"{code}: claves que no existen en ingles: {sobra}")
        d.update(t["ui"])
        d.update(perdidos.claves(code))
        faltan = [k for k in out["en"] if d[k] == out["en"][k] and k not in t["ui"] and not k.endswith(".paginas")
                  and not k.startswith("darkcraft.cap.") and not k.startswith("darkcraft.pista.")
                  and not k.startswith("darkcraft.narracion.") and not k.startswith("darkcraft.rey.voz.")
                  and not k.startswith("darkcraft.gavran.voz.")]
        # la interfaz nueva (rol.py) sale en espanol e ingles; los demas idiomas, mas adelante en tandas
        faltan = [k for k in faltan if k not in rol.EN]
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
    # texto para el narrador, un fichero por capitulo narrado (lo usa ci/narracion/generar.sh)
    for cap, lineas, pausas in ((1, NARRACION, PAUSAS), (5, NARRACION5, PAUSAS5), (6, perdidos.NARRACION6, perdidos.PAUSAS6)):
        assert len(lineas) == len(pausas), f"capitulo {cap}: {len(lineas)} frases y {len(pausas)} pausas"
        nar = os.path.join(os.path.dirname(__file__), "..", "narracion", f"cap{cap}.txt")
        os.makedirs(os.path.dirname(nar), exist_ok=True)
        with open(nar, "w", encoding="utf-8") as f:
            for (en, es), p in zip(lineas, pausas):
                f.write(f"{en}|{p}\n")
    print("Paginas OK" if good else "HAY PAGINAS DEMASIADO LARGAS")
    sys.exit(0 if good else 1)
