# -*- coding: utf-8 -*-
"""
Textos de la historia de la Ciudadela (es_es y en_us). Genera los ficheros de idioma del mod y
comprueba que cada pagina de libro cabe en la pantalla del libro de Minecraft (114 px x 14 lineas).
Uso: python3 ci/historia/textos.py   (escribe src/main/resources/assets/ciudadela/lang/*.json)
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
   "La Sombra aceptó. Esa misma noche la ciudadela fue arrancada de la tierra y alzada al cielo. Desde entonces sus ríos caen al vacío y la tormenta no ha cesado ni un solo día.",
   "Nadie murió. Fue peor. Soldados, criados y nobles se quedaron huecos, sin alma, y todavía recorren estos salones. No esperes piedad de ellos: ya no recuerdan su nombre.",
   "Dos guardianes mantienen viva la maldición: el Rey, que aún espera sentado en su Salón del Trono, y Ser Gavran, su caballero jurado, que vela en lo más alto del Árbol de la Corona.",
   "Mátalos a los dos y quizá la tormenta termine. No te diré cómo; yo solo escribo. Mi siguiente página te espera donde el agua aún canta, junto a la Fuente Mayor.\n\n- A.",
  ],
  en=[
   "If you are reading this, you have climbed the Ascent Tower. Few try. None come down the same as they went up. Sit, and listen to an old man.",
   "My name is Aldous, and I was the king's chronicler. I write so that someone remembers what Valdrenn was: the jewel of the kingdom, its towers touching the clouds, its rivers singing through every street.",
   "Our king, Corvin, loved Queen Isolde more than his own crown. When the grey fever began to take her, no healer, no prayer and no offering could save her.",
   "On a moonless night the king went down alone beneath the storm and spoke with the Nameless Shadow. He offered a bargain: the queen's life... in exchange for everything else.",
   "The Shadow agreed. That very night the citadel was torn from the earth and raised into the sky. Since then its rivers fall into the void, and the storm has not ceased for a single day.",
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
   "Pero escucha bien: la Sombra no vive aquí. Esta ciudadela solo era su puerta. Ella espera mucho más lejos, allí donde nace la tormenta.",
   "Gavran guardaba una llave que no es de hierro, sino de hueso. Úsala y te mostrará el camino hacia fortalezas que flotan más allá de las nubes.",
   "Dicen que tras ellas se alza una montaña que toca las estrellas, y un castillo donde arden las hogueras de los caídos. Allí se descansa... y desde allí se vuelve.",
   "Yo ya no puedo acompañarte. Lo que venga ahora es cosa tuya. Busca, lucha, cae y vuelve a levantarte. Nadie más va a escribir tu historia.",
   "Aquí termina la crónica de Valdrenn. Que tu acero no se melle y tu fuego nunca se apague.\n\n- Aldous, último cronista del rey.",
  ],
  en=[
   "Sir Gavran has fallen. His oath is broken, and with it breaks whatever bound the Shadow to these stones.",
   "Thank you. For a hundred years no one has read me. I can feel the ink slipping from my fingers; I think at last I will be able to rest.",
   "But listen well: the Shadow does not live here. This citadel was only its door. It waits much further away, where the storm is born.",
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
 ("That night, the citadel was torn from the earth, and its rivers began to fall into the void.",
  "Esa noche la ciudadela fue arrancada de la tierra, y sus ríos empezaron a caer al vacío."),
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
]
REY_UI = {
 "es": {
  "entity.ciudadela.rey": "Corvin, el Rey que Espera",
  "entity.ciudadela.rey_proyectil": "Tajo del Rey",
  "ciudadela.rey.nombre_corto": "Corvin",
  "block.ciudadela.rastrillo": "Rastrillo de hierro negro",
  "death.attack.ciudadela.rey": "%1$s fue ejecutado por %2$s",
  "death.attack.ciudadela.rey.player": "%1$s fue ejecutado por %2$s",
  "death.attack.ciudadela.rey_area": "%1$s ardió ante %2$s",
  "death.attack.ciudadela.rey_area.player": "%1$s ardió ante %2$s",
  "subtitles.ciudadela.rey.voz": "Corvin habla",
 },
 "en": {
  "entity.ciudadela.rey": "Corvin, the King Who Waits",
  "entity.ciudadela.rey_proyectil": "King's Slash",
  "ciudadela.rey.nombre_corto": "Corvin",
  "block.ciudadela.rastrillo": "Black Iron Portcullis",
  "death.attack.ciudadela.rey": "%1$s was executed by %2$s",
  "death.attack.ciudadela.rey.player": "%1$s was executed by %2$s",
  "death.attack.ciudadela.rey_area": "%1$s burned before %2$s",
  "death.attack.ciudadela.rey_area.player": "%1$s burned before %2$s",
  "subtitles.ciudadela.rey.voz": "Corvin speaks",
 },
}

# pausa (s) despues de cada linea: las largas son los cortes de plano
PAUSAS = [0.8, 1.6, 0.8, 0.7, 1.6, 1.6, 0.8, 1.6, 0.9, 0.0]

UI = {
 "es": {
  "itemGroup.ciudadela": "Ciudadela Gótica",
  "item.ciudadela.mapa_ciudadela": "Mapa de la Ciudadela",
  "ciudadela.mapa.lore": "Clic derecho para desplegarlo",
  "ciudadela.mapa.sin_castillo": "Este mapa no pertenece a ninguna ciudadela cercana.",
  "ciudadela.mapa.titulo": "Ciudadela de Valdrenn",
  "ciudadela.mapa.cronica": "Crónica",
  "ciudadela.mapa.siguiente": "Siguiente página",
  "ciudadela.mapa.tu": "Tú",
  "ciudadela.mapa.cerrar": "Pulsa ESC para enrollar el mapa",
  "ciudadela.mapa.lugar.torre_ascenso": "Torre de Ascenso",
  "ciudadela.mapa.lugar.puerta_exterior": "Puerta Exterior",
  "ciudadela.mapa.lugar.puerta_interior": "Puerta Interior",
  "ciudadela.mapa.lugar.fuente": "Fuente Mayor",
  "ciudadela.mapa.lugar.torreon": "Torreón",
  "ciudadela.mapa.lugar.trono": "Salón del Trono",
  "ciudadela.mapa.lugar.arbol": "Árbol de la Corona",
  "ciudadela.mapa.lugar.catedral": "Catedral",
  "ciudadela.mapa.lugar.biblioteca": "Biblioteca",
  "ciudadela.mapa.lugar.cementerio": "Cementerio",
  "ciudadela.mapa.lugar.laberinto": "Laberinto",
  "ciudadela.mapa.lugar.patio_armas": "Patio de Armas",
  "ciudadela.mapa.lugar.alquimista": "Torre del Alquimista",
  "ciudadela.mapa.lugar.observatorio": "Observatorio",
  "ciudadela.mapa.lugar.pueblo": "Pueblo",
  "ciudadela.mapa.lugar.jardin": "Jardín Privado",
  "ciudadela.capitulo": "CAPÍTULO %s",
  "ciudadela.cofre": "Crónica · Capítulo %s",
  "ciudadela.libro": "Crónica de Valdrenn · Capítulo %s",
  "ciudadela.libro.lore": "Escrita por Aldous, cronista del rey",
  "ciudadela.cine.saltar": "Mantén ESPACIO para saltar",
  "ciudadela.historia.reiniciada": "Tu historia de la ciudadela empieza de nuevo.",
  "ciudadela.historia.no_cofre": "Este cofre guarda una página que todavía no te corresponde leer.",
  "subtitles.ciudadela.campana": "Campana fúnebre",
  "subtitles.ciudadela.narracion": "Narrador",
 },
 "en": {
  "itemGroup.ciudadela": "Gothic Citadel",
  "item.ciudadela.mapa_ciudadela": "Map of the Citadel",
  "ciudadela.mapa.lore": "Right-click to unroll it",
  "ciudadela.mapa.sin_castillo": "This map does not belong to any nearby citadel.",
  "ciudadela.mapa.titulo": "Citadel of Valdrenn",
  "ciudadela.mapa.cronica": "Chronicle",
  "ciudadela.mapa.siguiente": "Next page",
  "ciudadela.mapa.tu": "You",
  "ciudadela.mapa.cerrar": "Press ESC to roll up the map",
  "ciudadela.mapa.lugar.torre_ascenso": "Ascent Tower",
  "ciudadela.mapa.lugar.puerta_exterior": "Outer Gate",
  "ciudadela.mapa.lugar.puerta_interior": "Inner Gate",
  "ciudadela.mapa.lugar.fuente": "Great Fountain",
  "ciudadela.mapa.lugar.torreon": "The Keep",
  "ciudadela.mapa.lugar.trono": "Throne Hall",
  "ciudadela.mapa.lugar.arbol": "The Crown Tree",
  "ciudadela.mapa.lugar.catedral": "Cathedral",
  "ciudadela.mapa.lugar.biblioteca": "Library",
  "ciudadela.mapa.lugar.cementerio": "Cemetery",
  "ciudadela.mapa.lugar.laberinto": "Hedge Maze",
  "ciudadela.mapa.lugar.patio_armas": "Training Yard",
  "ciudadela.mapa.lugar.alquimista": "Alchemist's Tower",
  "ciudadela.mapa.lugar.observatorio": "Observatory",
  "ciudadela.mapa.lugar.pueblo": "Town",
  "ciudadela.mapa.lugar.jardin": "Private Garden",
  "ciudadela.capitulo": "CHAPTER %s",
  "ciudadela.cofre": "Chronicle · Chapter %s",
  "ciudadela.libro": "Chronicle of Valdrenn · Chapter %s",
  "ciudadela.libro.lore": "Written by Aldous, the king's chronicler",
  "ciudadela.cine.saltar": "Hold SPACE to skip",
  "ciudadela.historia.reiniciada": "Your story in the citadel begins anew.",
  "ciudadela.historia.no_cofre": "This chest keeps a page you are not meant to read yet.",
  "subtitles.ciudadela.campana": "Funeral bell",
  "subtitles.ciudadela.narracion": "Narrator",
 },
}

# --------------------------------------------------------------- ancho de letra de Minecraft
W = {' ': 4, '!': 2, '"': 4, "'": 2, '(': 4, ')': 4, '*': 4, ',': 2, '.': 2, ':': 2, ';': 2, '<': 5, '>': 5,
     '@': 7, 'I': 4, '[': 4, ']': 4, '`': 3, 'f': 5, 'i': 2, 'k': 5, 'l': 3, 't': 4, '{': 4, '|': 2, '}': 4,
     '~': 7, 'í': 3, 'Í': 4, '¡': 2, '·': 2}

def width(s, bold=False):
    return sum(W.get(ch, 6) + (1 if bold else 0) for ch in s)

def wrap(text, maxw=114):
    lines = []
    for para in text.split("\n"):
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

def check():
    ok = True
    for n, c in CAP.items():
        for lang in ("es", "en"):
            for i, page in enumerate(c[lang]):
                extra = 3 if i == 0 else 0   # pagina 1: CAPITULO N + titulo + linea en blanco
                title = c[lang + "_t"]
                tl = len(wrap(title)) if i == 0 else 0
                lines = len(wrap(page)) + extra + max(0, tl - 1)
                flag = "OK " if lines <= 13 else "LARGA"
                if lines > 13:
                    ok = False
                print(f"cap {n} {lang} p{i+1}: {lines:2d} lineas {flag}")
            for line in c[lang + "_s"]:
                if width(line) > 88:
                    print("  CARTEL LARGO:", lang, line, width(line)); ok = False
    return ok

def build():
    out = {}
    for lang in ("es", "en"):
        d = dict(UI[lang])
        for n, c in CAP.items():
            d[f"ciudadela.cap.{n}.titulo"] = c[lang + "_t"]
            d[f"ciudadela.cap.{n}.cartel1"] = c[lang + "_s"][0]
            d[f"ciudadela.cap.{n}.cartel2"] = c[lang + "_s"][1]
            d[f"ciudadela.cap.{n}.paginas"] = str(len(c[lang]))
            for i, page in enumerate(c[lang]):
                d[f"ciudadela.cap.{n}.p{i+1}"] = page
        for k, v in PISTA[lang].items():
            d[f"ciudadela.pista.{k}"] = v
        for i, (en, es) in enumerate(NARRACION):
            d[f"ciudadela.narracion.1.{i+1}"] = es if lang == "es" else en
        for i, (en, es) in enumerate(REY):
            d[f"ciudadela.rey.voz.{i+1}"] = es if lang == "es" else en
        d.update(REY_UI[lang])
        out[lang] = d
    return out

if __name__ == "__main__":
    good = check()
    root = os.path.join(os.path.dirname(__file__), "..", "..", "src", "main", "resources", "assets", "ciudadela", "lang")
    data = build()
    for lang, fname in (("es", "es_es.json"), ("en", "en_us.json")):
        with open(os.path.join(root, fname), "w", encoding="utf-8") as f:
            json.dump(data[lang], f, ensure_ascii=False, indent=2)
    # texto para el narrador (lo usa ci/narracion/generar.sh)
    nar = os.path.join(os.path.dirname(__file__), "..", "narracion", "cap1.txt")
    os.makedirs(os.path.dirname(nar), exist_ok=True)
    with open(nar, "w", encoding="utf-8") as f:
        for (en, es), p in zip(NARRACION, PAUSAS):
            f.write(f"{en}|{p}\n")
    # frases del Rey (las usa ci/rey/voces.py)
    rv = os.path.join(os.path.dirname(__file__), "..", "rey", "frases.txt")
    os.makedirs(os.path.dirname(rv), exist_ok=True)
    with open(rv, "w", encoding="utf-8") as f:
        for en, es in REY:
            f.write(en + "\n")
    print("Paginas OK" if good else "HAY PAGINAS DEMASIADO LARGAS")
    sys.exit(0 if good else 1)
