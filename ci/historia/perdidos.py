# -*- coding: utf-8 -*-
"""
Parte 3 (Fortaleza de Hueso): los libros perdidos ("cosas que decia la gente"), el libro del acertijo del amuleto y
la narracion del capitulo 6 (habla la Sombra, con la voz del narrador de siempre). En los 6 idiomas.
textos.py los mete en los ficheros de idioma: darkcraft.perdido.N.{titulo,autor,paginas,pK}, darkcraft.acertijo.*,
darkcraft.narracion.6.N.
"""

# N -> donde esta: "valdrenn" o "fortaleza"
DONDE = {1: "valdrenn", 2: "valdrenn", 3: "valdrenn", 4: "valdrenn", 5: "valdrenn",
         6: "fortaleza", 7: "fortaleza", 8: "fortaleza", 9: "fortaleza", 10: "fortaleza", 11: "fortaleza",
         12: "brumavela", 13: "brumavela", 14: "brumavela", 15: "brumavela", 16: "brumavela",
         17: "fortaleza"}

# libro -> idioma -> (titulo, autor, [paginas])
LIBROS = {
 1: {
  "es": ("La lista de Berta", "Berta, la panadera", [
   "Harina de centeno.\nSal gruesa.\nTres docenas de velas negras para el castillo (¿negras? el mayordomo no quiso decirme para qué).\nMiel para la Reina.",
   "Nota: desde que volvió, la Reina no come pan. Ni miel. Ni nada.\n\nLas velas, en cambio, se acaban cada noche."]),
  "en": ("Berta's List", "Berta, the baker", [
   "Rye flour.\nCoarse salt.\nThree dozen black candles for the castle (black? the steward would not say why).\nHoney for the Queen.",
   "Note: since she came back, the Queen eats no bread. No honey. Nothing.\n\nThe candles, though, are gone every night."]),
  "fr": ("La liste de Berta", "Berta, la boulangère", [
   "Farine de seigle.\nGros sel.\nTrois douzaines de bougies noires pour le château (noires ? l'intendant n'a pas voulu dire pourquoi).\nDu miel pour la Reine.",
   "Note : depuis son retour, la Reine ne mange plus de pain. Ni de miel. Ni rien.\n\nLes bougies, elles, s'épuisent chaque nuit."]),
  "de": ("Bertas Liste", "Berta, die Bäckerin", [
   "Roggenmehl.\nGrobes Salz.\nDrei Dutzend schwarze Kerzen für die Burg (schwarz? der Verwalter sagte nicht, wofür).\nHonig für die Königin.",
   "Notiz: Seit sie zurück ist, isst die Königin kein Brot. Keinen Honig. Nichts.\n\nDie Kerzen aber sind jede Nacht aufgebraucht."]),
  "pt": ("A lista de Berta", "Berta, a padeira", [
   "Farinha de centeio.\nSal grosso.\nTrês dúzias de velas pretas para o castelo (pretas? o mordomo não quis dizer para quê).\nMel para a Rainha.",
   "Nota: desde que voltou, a Rainha não come pão. Nem mel. Nem nada.\n\nAs velas, porém, acabam toda noite."]),
  "ja": ("ベルタの買い物メモ", "パン屋のベルタ", [
   "ライ麦粉。\n粗塩。\n城へ黒い蝋燭を三ダース(黒?執事は理由を言わなかった)。\n王妃さまに蜂蜜。",
   "覚え書き:戻ってから、王妃さまはパンを召し上がらない。蜂蜜も。何も。\n\nそれなのに蝋燭は毎晩なくなる。"]),
 },
 2: {
  "es": ("Carta a mi madre", "Joren, de la guardia", [
   "Madre: no vengáis a la ciudad para la fiesta. La Reina ha vuelto y todos lo celebran, pero yo hago la ronda de noche y la oigo reír en los jardines.",
   "No es su risa. Antes reía como campanillas. Ahora suena como el viento cuando entra en las criptas.\n\nEl capitán dice que no hable de ello. Os quiere,\nJoren."]),
  "en": ("A Letter to My Mother", "Joren, of the guard", [
   "Mother: do not come to the city for the feast. The Queen has returned and everyone celebrates, but I walk the night watch and I hear her laughing in the gardens.",
   "It is not her laugh. She used to laugh like little bells. Now it sounds like the wind when it gets into the crypts.\n\nThe captain says I must not speak of it. With love,\nJoren."]),
  "fr": ("Lettre à ma mère", "Joren, de la garde", [
   "Mère : ne venez pas en ville pour la fête. La Reine est revenue et tous célèbrent, mais je fais la ronde de nuit et je l'entends rire dans les jardins.",
   "Ce n'est pas son rire. Avant, elle riait comme des clochettes. Maintenant, on dirait le vent qui entre dans les cryptes.\n\nLe capitaine dit de ne pas en parler. Je vous aime,\nJoren."]),
  "de": ("Brief an meine Mutter", "Joren, von der Wache", [
   "Mutter: Kommt nicht zum Fest in die Stadt. Die Königin ist zurück und alle feiern, doch ich gehe die Nachtwache und höre sie in den Gärten lachen.",
   "Es ist nicht ihr Lachen. Früher lachte sie wie Glöckchen. Jetzt klingt es wie der Wind, der in die Grüfte fährt.\n\nDer Hauptmann sagt, ich soll schweigen. In Liebe,\nJoren."]),
  "pt": ("Carta para minha mãe", "Joren, da guarda", [
   "Mãe: não venha à cidade para a festa. A Rainha voltou e todos comemoram, mas eu faço a ronda da noite e a ouço rir nos jardins.",
   "Não é o riso dela. Antes ela ria como sininhos. Agora soa como o vento quando entra nas criptas.\n\nO capitão diz para eu não falar disso. Com amor,\nJoren."]),
  "ja": ("母への手紙", "衛兵ヨレン", [
   "母さん、祭りには街へ来ないで。王妃さまが戻られて皆が祝っているけれど、夜の見回りをしていると、庭で笑う声が聞こえるんだ。",
   "あれは王妃さまの笑い声じゃない。昔は鈴のように笑われた。今は地下墓所に吹き込む風のような音がする。\n\n隊長は口にするなと言う。愛をこめて、\nヨレン"]),
 },
 3: {
  "es": ("Sermón que no se dijo", "Obispo Anselm", [
   "Hermanos: hay pactos que se firman con tinta y pactos que se firman con lo que más amamos. El Rey firmó el segundo.\n\nLa Sombra no devuelve: presta.",
   "Y lo que presta, lo cobra con intereses. Rezad por Corvin. Rezad por Isolde, si aún queda algo de ella que escuche las oraciones.",
   "(El obispo nunca llegó a decirlo. Lo encontraron a la mañana siguiente sentado en su silla, mirando al norte, hacia las nubes.)"]),
  "en": ("The Sermon Never Spoken", "Bishop Anselm", [
   "Brothers and sisters: some pacts are signed in ink, and some are signed with what we love most. The King signed the second kind.\n\nThe Shadow does not give back: it lends.",
   "And what it lends, it takes back with interest. Pray for Corvin. Pray for Isolde, if anything of her is left to hear our prayers.",
   "(The bishop never spoke these words. He was found the next morning sitting in his chair, staring north, toward the clouds.)"]),
  "fr": ("Le sermon jamais prononcé", "Évêque Anselm", [
   "Mes frères : certains pactes se signent à l'encre, d'autres avec ce que nous aimons le plus. Le Roi a signé le second.\n\nL'Ombre ne rend pas : elle prête.",
   "Et ce qu'elle prête, elle le reprend avec intérêts. Priez pour Corvin. Priez pour Isolde, s'il reste d'elle quelque chose pour entendre nos prières.",
   "(L'évêque ne l'a jamais dit. On l'a trouvé le lendemain matin assis sur sa chaise, le regard tourné vers le nord, vers les nuages.)"]),
  "de": ("Die nie gehaltene Predigt", "Bischof Anselm", [
   "Brüder: Manche Pakte unterschreibt man mit Tinte, andere mit dem, was man am meisten liebt. Der König unterschrieb den zweiten.\n\nDer Schatten gibt nicht zurück: Er leiht.",
   "Und was er leiht, holt er sich mit Zinsen zurück. Betet für Corvin. Betet für Isolde, falls noch etwas von ihr unsere Gebete hört.",
   "(Der Bischof sprach diese Worte nie. Man fand ihn am nächsten Morgen in seinem Stuhl, den Blick nach Norden gerichtet, zu den Wolken.)"]),
  "pt": ("O sermão que não foi dito", "Bispo Anselm", [
   "Irmãos: há pactos que se assinam com tinta e pactos que se assinam com o que mais amamos. O Rei assinou o segundo.\n\nA Sombra não devolve: empresta.",
   "E o que empresta, cobra com juros. Rezem por Corvin. Rezem por Isolde, se ainda resta algo dela que ouça as orações.",
   "(O bispo nunca chegou a dizê-lo. Encontraram-no na manhã seguinte sentado em sua cadeira, olhando para o norte, para as nuvens.)"]),
  "ja": ("語られなかった説教", "アンセルム司教", [
   "兄弟たちよ。契約にはインクで結ぶものと、最も愛するもので結ぶものがある。王は後者を結んだ。\n\n影は返さない。貸すのだ。",
   "そして貸したものは、利子をつけて取り立てる。コルヴィンのために祈れ。イゾルデのために祈れ。祈りを聞く何かが、まだ彼女に残っているのなら。",
   "(司教がこれを口にすることはなかった。翌朝、椅子に座ったまま北の雲を見つめている姿で見つかった。)"]),
 },
 4: {
  "es": ("Mi diario secreto", "Pip, 9 años", [
   "Hoy he visto a la Reina en el jardín. Era de noche, había luna y yo estaba en la ventana. La Reina estaba muy quieta junto a la fuente.",
   "Las estatuas tenían sombra. Los árboles tenían sombra.\n\nLa Reina no.\n\nMamá dice que lo soñé. No lo soñé."]),
  "en": ("My Secret Diary", "Pip, age 9", [
   "Today I saw the Queen in the garden. It was night, there was a moon and I was at the window. The Queen was standing very still by the fountain.",
   "The statues had shadows. The trees had shadows.\n\nThe Queen did not.\n\nMum says I dreamed it. I did not dream it."]),
  "fr": ("Mon journal secret", "Pip, 9 ans", [
   "Aujourd'hui, j'ai vu la Reine dans le jardin. C'était la nuit, il y avait la lune et j'étais à la fenêtre. La Reine restait immobile près de la fontaine.",
   "Les statues avaient une ombre. Les arbres avaient une ombre.\n\nPas la Reine.\n\nMaman dit que je l'ai rêvé. Je ne l'ai pas rêvé."]),
  "de": ("Mein geheimes Tagebuch", "Pip, 9 Jahre", [
   "Heute habe ich die Königin im Garten gesehen. Es war Nacht, der Mond schien und ich war am Fenster. Die Königin stand ganz still am Brunnen.",
   "Die Statuen hatten Schatten. Die Bäume hatten Schatten.\n\nDie Königin nicht.\n\nMama sagt, ich habe geträumt. Habe ich nicht."]),
  "pt": ("Meu diário secreto", "Pip, 9 anos", [
   "Hoje vi a Rainha no jardim. Era noite, havia lua e eu estava na janela. A Rainha estava muito quieta junto à fonte.",
   "As estátuas tinham sombra. As árvores tinham sombra.\n\nA Rainha não.\n\nMamãe diz que eu sonhei. Eu não sonhei."]),
  "ja": ("ひみつの日記", "ピップ(九歳)", [
   "きょう、お庭で王妃さまを見た。夜で、月が出ていて、ぼくは窓のところにいた。王妃さまは噴水のそばでじっと立っていた。",
   "像には影があった。木にも影があった。\n\n王妃さまにはなかった。\n\nお母さんは夢だって言う。夢じゃない。"]),
 },
 5: {
  "es": ("El encargo de Ser Gavran", "Maese Dorran, herrero", [
   "Ser Gavran me trajo un hueso largo y blanco. No era de vaca ni de caballo. Me pidió una llave con él, y que no hiciera preguntas.",
   "Trabajarlo fue como tallar hielo que no se derrite. Al terminar, la llave estaba tibia. Ser Gavran dijo que abría una puerta al otro lado de las nubes, a muchas leguas de aquí.",
   "Dijo también que esperaba que nadie tuviera que usarla nunca.\n\nLo dijo como quien reza."]),
  "en": ("Ser Gavran's Commission", "Master Dorran, smith", [
   "Ser Gavran brought me a long white bone. It was not from a cow or a horse. He asked me to make a key from it, and to ask no questions.",
   "Working it was like carving ice that never melts. When I finished, the key was warm. Ser Gavran said it opened a door beyond the clouds, many leagues from here.",
   "He also said he hoped no one would ever have to use it.\n\nHe said it the way one prays."]),
  "fr": ("La commande de Ser Gavran", "Maître Dorran, forgeron", [
   "Ser Gavran m'a apporté un long os blanc. Ce n'était ni de la vache ni du cheval. Il m'a demandé d'en faire une clé, et de ne pas poser de questions.",
   "Le travailler, c'était comme tailler de la glace qui ne fond pas. À la fin, la clé était tiède. Ser Gavran a dit qu'elle ouvrait une porte au-delà des nuages, à bien des lieues d'ici.",
   "Il a dit aussi qu'il espérait que personne n'aurait jamais à s'en servir.\n\nIl l'a dit comme on prie."]),
  "de": ("Ser Gavrans Auftrag", "Meister Dorran, Schmied", [
   "Ser Gavran brachte mir einen langen weißen Knochen. Er stammte weder von einer Kuh noch von einem Pferd. Er bat mich, daraus einen Schlüssel zu machen, und keine Fragen zu stellen.",
   "Ihn zu bearbeiten war, als schnitze man Eis, das nicht schmilzt. Am Ende war der Schlüssel warm. Ser Gavran sagte, er öffne eine Tür jenseits der Wolken, viele Meilen von hier.",
   "Er sagte auch, er hoffe, niemand müsse ihn je benutzen.\n\nEr sagte es wie ein Gebet."]),
  "pt": ("A encomenda de Ser Gavran", "Mestre Dorran, ferreiro", [
   "Ser Gavran me trouxe um osso longo e branco. Não era de vaca nem de cavalo. Pediu-me uma chave feita com ele, e que eu não fizesse perguntas.",
   "Trabalhá-lo foi como entalhar gelo que não derrete. Ao terminar, a chave estava morna. Ser Gavran disse que ela abria uma porta do outro lado das nuvens, a muitas léguas daqui.",
   "Disse também que esperava que ninguém precisasse usá-la nunca.\n\nDisse isso como quem reza."]),
  "ja": ("サー・ガヴランの注文", "鍛冶屋ドラン親方", [
   "サー・ガヴランが長く白い骨を持ってきた。牛のものでも馬のものでもなかった。これで鍵を作れ、何も訊くな、と言われた。",
   "加工は、溶けない氷を削るようだった。仕上がった鍵は温かかった。サー・ガヴランは、これは雲の向こう、遥か彼方の扉を開くのだと言った。",
   "誰もこれを使わずに済むことを願う、とも言った。\n\nまるで祈るように。"]),
 },
 6: {
  "es": ("El Juramento de Hueso", "La guardia de la fortaleza", [
   "Juro guardar la puerta del cielo.\nJuro no abrirla a quien no traiga la llave de hueso.\nJuro no dormir.\nJuro no morir.",
   "(Debajo, con otra letra:)\n\nCumplimos los cuatro.\nEl último no lo elegimos."]),
  "en": ("The Oath of Bone", "The fortress guard", [
   "I swear to guard the gate of the sky.\nI swear to open it to none who lacks the bone key.\nI swear not to sleep.\nI swear not to die.",
   "(Below, in another hand:)\n\nWe kept all four.\nThe last one we did not choose."]),
  "fr": ("Le Serment d'Os", "La garde de la forteresse", [
   "Je jure de garder la porte du ciel.\nJe jure de ne l'ouvrir à nul qui n'ait la clé d'os.\nJe jure de ne pas dormir.\nJe jure de ne pas mourir.",
   "(En dessous, d'une autre main :)\n\nNous avons tenu les quatre.\nLe dernier, nous ne l'avons pas choisi."]),
  "de": ("Der Knocheneid", "Die Wache der Festung", [
   "Ich schwöre, das Tor des Himmels zu bewachen.\nIch schwöre, es niemandem ohne den Knochenschlüssel zu öffnen.\nIch schwöre, nicht zu schlafen.\nIch schwöre, nicht zu sterben.",
   "(Darunter, in anderer Schrift:)\n\nWir hielten alle vier.\nDen letzten wählten wir nicht."]),
  "pt": ("O Juramento de Osso", "A guarda da fortaleza", [
   "Juro guardar a porta do céu.\nJuro não abri-la a quem não trouxer a chave de osso.\nJuro não dormir.\nJuro não morrer.",
   "(Embaixo, com outra letra:)\n\nCumprimos os quatro.\nO último não escolhemos."]),
  "ja": ("骨の誓い", "城塞の衛兵", [
   "天の門を守ると誓う。\n骨の鍵を持たぬ者には開かぬと誓う。\n眠らぬと誓う。\n死なぬと誓う。",
   "(その下に、別の筆跡で)\n\n四つとも守った。\n最後の一つは、望んだわけではない。"]),
 },
 7: {
  "es": ("Cuaderno del cantero", "Hadric, maestro cantero", [
   "Día 40. Las piedras suben solas cuando cae la noche. Nadie las empuja. El Rey dice que es la Sombra, que nos ayuda.\n\nYo no he pedido ayuda.",
   "Día 77. Ya estamos por encima de las nubes. Desde aquí no se ve Valdrenn, pero se oye su tormenta, muy lejos, como un tambor.",
   "Día 103. Han traído huesos para las murallas. Muchos. Dicen que son de animales.\n\nHe enterrado animales toda mi vida y nunca vi huesos así."]),
  "en": ("The Mason's Notebook", "Hadric, master mason", [
   "Day 40. The stones rise on their own when night falls. No one pushes them. The King says it is the Shadow, helping us.\n\nI never asked for help.",
   "Day 77. We are above the clouds now. From here you cannot see Valdrenn, but you can hear its storm, far away, like a drum.",
   "Day 103. They brought bones for the walls. Many. They say they are animal bones.\n\nI have buried animals all my life and never seen bones like these."]),
  "fr": ("Le carnet du tailleur de pierre", "Hadric, maître tailleur", [
   "Jour 40. Les pierres montent seules quand vient la nuit. Personne ne les pousse. Le Roi dit que c'est l'Ombre qui nous aide.\n\nJe n'ai pas demandé d'aide.",
   "Jour 77. Nous sommes au-dessus des nuages. D'ici, on ne voit pas Valdrenn, mais on entend sa tempête, très loin, comme un tambour.",
   "Jour 103. On a apporté des os pour les remparts. Beaucoup. On dit que ce sont des os d'animaux.\n\nJ'ai enterré des bêtes toute ma vie, je n'ai jamais vu d'os pareils."]),
  "de": ("Das Heft des Steinmetzes", "Hadric, Steinmetzmeister", [
   "Tag 40. Die Steine steigen von selbst, wenn die Nacht kommt. Niemand schiebt sie. Der König sagt, der Schatten helfe uns.\n\nIch habe nicht um Hilfe gebeten.",
   "Tag 77. Wir sind jetzt über den Wolken. Von hier sieht man Valdrenn nicht, aber man hört seinen Sturm, weit weg, wie eine Trommel.",
   "Tag 103. Man hat Knochen für die Mauern gebracht. Viele. Es heißt, es seien Tierknochen.\n\nIch habe mein Leben lang Tiere begraben und nie solche Knochen gesehen."]),
  "pt": ("Caderno do pedreiro", "Hadric, mestre pedreiro", [
   "Dia 40. As pedras sobem sozinhas quando a noite cai. Ninguém as empurra. O Rei diz que é a Sombra, que nos ajuda.\n\nEu não pedi ajuda.",
   "Dia 77. Já estamos acima das nuvens. Daqui não se vê Valdrenn, mas se ouve sua tempestade, muito longe, como um tambor.",
   "Dia 103. Trouxeram ossos para as muralhas. Muitos. Dizem que são de animais.\n\nEnterrei animais a vida toda e nunca vi ossos assim."]),
  "ja": ("石工の帳面", "石工頭ハドリック", [
   "四十日目。夜になると石がひとりでに上がっていく。誰も押していない。王は、影が手を貸しているのだと言う。\n\n私は助けなど頼んでいない。",
   "七十七日目。もう雲の上だ。ここからヴァルドレンは見えないが、遠くで太鼓のようにその嵐が聞こえる。",
   "百三日目。城壁のために骨が運ばれてきた。大量に。獣の骨だという。\n\n私は生涯獣を葬ってきたが、こんな骨は見たことがない。"]),
 },
 8: {
  "es": ("La última carta de Ser Aldric", "Ser Aldric", [
   "Si alguien lee esto, ya no soy yo quien guarda la puerta. Siento el frío subir por las piernas, por el pecho. Los demás ya no hablan: solo gruñen dentro del yelmo.",
   "No os acerquéis a nosotros. No pidáis clemencia: ya no sabremos darla.\n\nSi tenéis la llave, subid deprisa. Y perdonadnos."]),
  "en": ("Ser Aldric's Last Letter", "Ser Aldric", [
   "If anyone reads this, I am no longer the one guarding the gate. I feel the cold rising through my legs, my chest. The others no longer speak: they only growl inside their helms.",
   "Do not come near us. Do not ask for mercy: we will no longer know how to give it.\n\nIf you have the key, climb quickly. And forgive us."]),
  "fr": ("La dernière lettre de Ser Aldric", "Ser Aldric", [
   "Si quelqu'un lit ceci, ce n'est plus moi qui garde la porte. Je sens le froid monter dans mes jambes, ma poitrine. Les autres ne parlent plus : ils grognent dans leur heaume.",
   "Ne vous approchez pas de nous. Ne demandez pas grâce : nous ne saurons plus l'accorder.\n\nSi vous avez la clé, montez vite. Et pardonnez-nous."]),
  "de": ("Ser Aldrics letzter Brief", "Ser Aldric", [
   "Wenn jemand dies liest, bin nicht mehr ich es, der das Tor bewacht. Ich spüre die Kälte in Beinen und Brust aufsteigen. Die anderen sprechen nicht mehr: Sie knurren nur noch in ihren Helmen.",
   "Kommt uns nicht nahe. Bittet nicht um Gnade: Wir werden nicht mehr wissen, wie man sie gewährt.\n\nWenn ihr den Schlüssel habt, steigt schnell hinauf. Und vergebt uns."]),
  "pt": ("A última carta de Ser Aldric", "Ser Aldric", [
   "Se alguém ler isto, já não sou eu quem guarda a porta. Sinto o frio subir pelas pernas, pelo peito. Os outros já não falam: só rosnam dentro do elmo.",
   "Não se aproximem de nós. Não peçam clemência: já não saberemos dá-la.\n\nSe vocês têm a chave, subam depressa. E perdoem-nos."]),
  "ja": ("サー・アルドリックの最後の手紙", "サー・アルドリック", [
   "これを読む者がいるなら、もう門を守っているのは私ではない。冷たさが脚を、胸を這い上がってくる。他の者はもう話さない。兜の中でうなるだけだ。",
   "我らに近づくな。慈悲を乞うな。もう与え方を覚えていない。\n\n鍵を持っているなら、急いで登れ。そして、我らを許してくれ。"]),
 },
 9: {
  "es": ("Carta sin enviar", "Isolde", [
   "Querido Corvin: me pediste que volviera y volví.\n\n¿Por qué ya no me miras a los ojos?",
   "Aquí arriba se está bien. Hace frío y nadie llora. La Sombra dice que pronto abrirá la puerta de lo más alto, y que entonces todo Valdrenn será como yo.",
   "Tranquilo. Callado. Eterno.\n\n¿No es eso lo que querías?"]),
  "en": ("An Unsent Letter", "Isolde", [
   "Dearest Corvin: you asked me to come back, and I came back.\n\nWhy will you no longer look me in the eyes?",
   "It is pleasant up here. It is cold and no one weeps. The Shadow says it will soon open the door at the very top, and then all of Valdrenn will be like me.",
   "Calm. Silent. Eternal.\n\nIsn't that what you wanted?"]),
  "fr": ("Lettre jamais envoyée", "Isolde", [
   "Mon cher Corvin : tu m'as demandé de revenir, et je suis revenue.\n\nPourquoi ne me regardes-tu plus dans les yeux ?",
   "On est bien, ici, en haut. Il fait froid et personne ne pleure. L'Ombre dit qu'elle ouvrira bientôt la porte tout en haut, et qu'alors tout Valdrenn sera comme moi.",
   "Calme. Silencieux. Éternel.\n\nN'est-ce pas ce que tu voulais ?"]),
  "de": ("Ein nie abgeschickter Brief", "Isolde", [
   "Liebster Corvin: Du batest mich zurückzukehren, und ich kehrte zurück.\n\nWarum siehst du mir nicht mehr in die Augen?",
   "Hier oben ist es schön. Es ist kalt und niemand weint. Der Schatten sagt, bald öffne er die Tür ganz oben, und dann werde ganz Valdrenn wie ich.",
   "Ruhig. Still. Ewig.\n\nIst es nicht das, was du wolltest?"]),
  "pt": ("Carta nunca enviada", "Isolde", [
   "Querido Corvin: você me pediu que voltasse, e eu voltei.\n\nPor que você já não me olha nos olhos?",
   "Aqui em cima é agradável. Faz frio e ninguém chora. A Sombra diz que logo abrirá a porta lá no alto, e então toda Valdrenn será como eu.",
   "Tranquila. Calada. Eterna.\n\nNão era isso que você queria?"]),
  "ja": ("出されなかった手紙", "イゾルデ", [
   "愛しいコルヴィン。戻ってくれと言われたから、戻りました。\n\nどうしてもう、私の目を見てくれないの?",
   "ここは心地いいわ。寒くて、誰も泣かない。影は、もうすぐ一番上の扉を開くと言っている。そうすればヴァルドレンのすべてが私のようになるって。",
   "静かで、黙して、永遠に。\n\nそれがあなたの望みだったでしょう?"]),
 },
 10: {
  "es": ("Notas sobre el portal", "Maestre Velan", [
   "En lo más alto de la torre hay un arco que no lleva a ninguna parte. La piedra está fría aunque le dé el sol.",
   "No se abre con fuerza ni con fuego. Responde a lo que recuerda: el hueso de un juramento, la luz de los diamantes, el metal del infierno.",
   "Quien lo abra verá el otro lado. Quien lo cruce no volverá a ser el mismo.\n\nYo no pienso averiguarlo."]),
  "en": ("Notes on the Portal", "Maester Velan", [
   "At the very top of the tower stands an arch that leads nowhere. The stone is cold even in full sun.",
   "It does not open to strength or fire. It answers to what it remembers: the bone of an oath, the light of diamonds, the metal of the underworld.",
   "Whoever opens it will see the other side. Whoever crosses it will never be the same.\n\nI do not intend to find out."]),
  "fr": ("Notes sur le portail", "Maître Velan", [
   "Tout en haut de la tour se dresse une arche qui ne mène nulle part. La pierre est froide même en plein soleil.",
   "Elle ne s'ouvre ni par la force ni par le feu. Elle répond à ce dont elle se souvient : l'os d'un serment, la lumière des diamants, le métal des enfers.",
   "Qui l'ouvrira verra l'autre côté. Qui la franchira ne sera plus jamais le même.\n\nJe n'ai pas l'intention de le découvrir."]),
  "de": ("Notizen über das Portal", "Magister Velan", [
   "Ganz oben im Turm steht ein Bogen, der nirgendwohin führt. Der Stein ist kalt, selbst in der Sonne.",
   "Er öffnet sich weder durch Kraft noch durch Feuer. Er antwortet auf das, woran er sich erinnert: den Knochen eines Eides, das Licht der Diamanten, das Metall der Unterwelt.",
   "Wer ihn öffnet, wird die andere Seite sehen. Wer ihn durchschreitet, wird nie mehr derselbe sein.\n\nIch habe nicht vor, es herauszufinden."]),
  "pt": ("Notas sobre o portal", "Mestre Velan", [
   "No alto da torre há um arco que não leva a lugar nenhum. A pedra é fria mesmo sob o sol.",
   "Não se abre com força nem com fogo. Responde ao que recorda: o osso de um juramento, a luz dos diamantes, o metal do inferno.",
   "Quem o abrir verá o outro lado. Quem o atravessar nunca mais será o mesmo.\n\nNão pretendo descobrir."]),
  "ja": ("門についての覚え書き", "学匠ヴェラン", [
   "塔の頂に、どこへも通じぬ拱門がある。日が当たっていても石は冷たい。",
   "力でも炎でも開かない。それが覚えているものに応える。誓いの骨、ダイヤモンドの光、冥府の金属。",
   "開く者は向こう側を見る。くぐる者は二度と元には戻らない。\n\n私は確かめるつもりはない。"]),
 },
 11: {
  "es": ("Orden del día", "Capitán de la guardia", [
   "Turno de día: todos.\nTurno de noche: todos.\nRelevo: nunca.\n\nQuien se duerma en la muralla, que no despierte."]),
  "en": ("Orders of the Day", "Captain of the guard", [
   "Day watch: everyone.\nNight watch: everyone.\nRelief: never.\n\nWhoever falls asleep on the wall, let him not wake."]),
  "fr": ("Ordre du jour", "Capitaine de la garde", [
   "Garde de jour : tous.\nGarde de nuit : tous.\nRelève : jamais.\n\nQui s'endort sur le rempart, qu'il ne se réveille pas."]),
  "de": ("Tagesbefehl", "Hauptmann der Wache", [
   "Tagwache: alle.\nNachtwache: alle.\nAblösung: nie.\n\nWer auf der Mauer einschläft, soll nicht mehr erwachen."]),
  "pt": ("Ordem do dia", "Capitão da guarda", [
   "Turno do dia: todos.\nTurno da noite: todos.\nRendição: nunca.\n\nQuem dormir na muralha, que não acorde."]),
  "ja": ("本日の命令", "衛兵隊長", [
   "昼の番:全員。\n夜の番:全員。\n交代:なし。\n\n城壁で眠った者は、二度と目覚めるな。"]),
 },
 # ---- las notas de Brumavela, la isla del pueblo (parte 3b): de momento en espanol e ingles
 12: {
  "es": ("Aviso del capitán de puerto", "Odo Varn, capitán de puerto", [
   "A quien aún sepa leer:\n\nLa Garza Gris no volverá a zarpar. El Capitán ha mandado desarmarla, y lo que él manda en Brumavela todavía se hace.",
   "La vela, al almacén del muelle. El timón se lo quedó la tabernera, en pago de lo que le debe la tripulación. El mástil nuevo lo guardan los frailes en la cripta: es madera bendecida.",
   "Del ancla solo sé que bajaron con ella al pobre Tobías por el Saliente, y que la cadena sigue allí.\n\nY el corazón se lo ha subido él al faro. Dice que mientras arda, los nuestros sabrán volver."]),
  "en": ("The Harbourmaster's Notice", "Odo Varn, harbourmaster", [
   "To whoever can still read:\n\nThe Grey Heron will not sail again. The Captain has ordered her taken apart, and what he orders in Brumavela is still done.",
   "The sail, to the warehouse on the quay. The tavern keeper kept the helm, for what the crew owes her. The friars guard the new mast in the crypt: it is blessed wood.",
   "Of the anchor I only know they lowered poor Tobias with it down the Ledge, and the chain is still there.\n\nAnd the heart he carried up to the lighthouse himself. He says that while it burns, our own will find the way back."]),
 },
 13: {
  "es": ("Cuaderno de la tabernera", "Marga, de La Red Vacía", [
   "Ya nadie paga. Se sientan, miran la jarra y no beben. Ayer le hablé a Hobb el joven y tardó un rato largo en volver la cabeza.\n\nNo había nadie detrás de sus ojos.",
   "Dicen que es la bruma, que sube del mar de nubes desde que el Rey de Valdrenn hizo lo que hizo.\n\nHe colgado el timón de la Garza tras la barra. Si vienen a por él, que vengan. Yo ya no espero a nadie."]),
  "en": ("The Tavern Keeper's Ledger", "Marga, of The Empty Net", [
   "Nobody pays any more. They sit, stare at the mug and do not drink. Yesterday I spoke to young Hobb and it took him a long while to turn his head.\n\nThere was no one behind his eyes.",
   "They say it is the mist, rising from the sea of clouds ever since the King of Valdrenn did what he did.\n\nI have hung the Heron's helm behind the bar. If they come for it, let them. I am not waiting for anyone."]),
 },
 14: {
  "es": ("Las tres campanas", "Fray Tomé, el último", [
   "Tres fundadores tuvo Brumavela, y tres campanas llevan su nombre: la del Pescador mira al muelle, la de la Tejedora al norte y la del Farero a su faro.",
   "Cuando calle el último de nosotros, que suenen en el orden en que ellos se fueron, y la cripta se abrirá para darnos sitio.\n\nSus lápidas no mienten: están al pie de la capilla."]),
  "en": ("The Three Bells", "Brother Tome, the last", [
   "Brumavela had three founders, and three bells bear their names: the Fisherman's looks to the quay, the Weaver's to the north and the Keeper's to his lighthouse.",
   "When the last of us falls silent, let them ring in the order in which the founders left, and the crypt will open to make room for us.\n\nTheir gravestones do not lie: they stand at the foot of the chapel."]),
 },
 15: {
  "es": ("Carta de un marinero", "Tobías, gaviero de la Garza Gris", [
   "Lena: el Capitán dice que la isla se nos va, que la bruma la arrastra, y quiere amarrarla a las nubes con el ancla de la Garza. Mañana bajo yo con ella por el Saliente.",
   "No te rías. Tú no has visto latir el corazón del barco: una piedra de tormenta, azul, que canta bajito. Con eso vuela la Garza. Con eso volveremos a Valdrenn.\n\nGuárdame la cena."]),
  "en": ("A Sailor's Letter", "Tobias, topman of the Grey Heron", [
   "Lena: the Captain says the island is slipping away, that the mist is dragging it, and he means to moor it to the clouds with the Heron's anchor. Tomorrow I go down the Ledge with it.",
   "Do not laugh. You have not seen the ship's heart beat: a storm stone, blue, that sings very softly. That is what makes the Heron fly. That is how we will get back to Valdrenn.\n\nKeep my supper for me."]),
 },
 16: {
  "es": ("Diario del Capitán: última hoja", "Edrik Maroll, capitán de la Garza Gris", [
   "Desarmé la Garza con mis propias manos. Si nadie puede irse, nadie llevará esto a otra isla. Eso me dije.",
   "Subí el corazón al faro. Mientras arda, los que cayeron al mar de nubes sabrán volver a casa.\n\nY volvieron. Dios nos valga: volvieron.",
   "Suben del muelle cada noche, chorreando bruma, y no me conocen. Yo tampoco me conozco ya.\n\nQuien lea esto: no te lleves el corazón. O llévatelo, y llévanos contigo."]),
  "en": ("The Captain's Log: Last Leaf", "Edrik Maroll, captain of the Grey Heron", [
   "I took the Heron apart with my own hands. If no one can leave, no one will carry this to another island. So I told myself.",
   "I carried the heart up to the lighthouse. While it burns, those who fell into the sea of clouds will find their way home.\n\nAnd they did. God help us: they did.",
   "They come up from the quay every night, dripping mist, and they do not know me. I no longer know myself.\n\nWhoever reads this: do not take the heart. Or take it, and take us with you."]),
 },
 17: {
  "es": ("Confesión bajo la fuente", "Oswin, el último vigía de la cúpula", [
   "Éramos doce vigías bajo la cúpula. Velábamos el jardín, la torre del maestre Velan y, de noche, por turnos, la puerta de lo más alto. Nunca nos dijeron qué guardaba.",
   "Cuando el cielo se puso rojo, los otros once subieron a mirar. No bajaron. Ahora rondan el jardín con los ojos encendidos y se apagan si los miras.\n\nYo me escondí aquí, bajo la fuente.",
   "El tesoro del maestre no está en la armería. Está tras el muro que no es muro: en el sótano, en la sala de guardia del oeste. Un buen golpe y se deshace como ceniza.\n\nQue me perdone. Ya nadie lo necesita."]),
  "en": ("Confession Beneath the Fountain", "Oswin, last watchman of the dome", [
   "There were twelve of us watchmen beneath the dome. We kept the garden, Maester Velan's tower and, by night, in turns, the door at the very top. No one ever told us what it guarded.",
   "When the sky turned red, the other eleven went up to look. They never came down. Now they walk the garden with burning eyes and fade away if you look at them.\n\nI hid down here, beneath the fountain.",
   "The maester's treasure is not in the armoury. It lies behind the wall that is no wall: in the cellar, in the west guardroom. Strike it well and it crumbles like ash.\n\nMay he forgive me. No one needs it now."]),
 },
}

# el libro del acertijo del amuleto (con el boton que dibuja la receta)
ACERTIJO = {
 "es": ("El acertijo de hueso", "Maestre Velan", [
  "Quien quiera abrir la puerta de lo más alto tendrá que forjar un amuleto. Lo dejé escrito en verso, para que solo lo entienda quien lo lea con calma.",
  "Dos luces que no se rompen\ncoronan la forja,\ny entre ellas descansa\nel hierro del infierno.\n\nDebajo, el hueso que guarda\nun juramento\nduerme entre dos hermanos\nde ese mismo hierro.",
  "Si la tinta se te resiste, pulsa el sello de abajo: el libro te enseñará la forja dibujada."], "Ver la forja", "La forja del amuleto"),
 "en": ("The Bone Riddle", "Maester Velan", [
  "Whoever wishes to open the door at the very top must forge an amulet. I wrote it down in verse, so that only a patient reader would understand.",
  "Two lights that never break\ncrown the forge,\nand between them rests\nthe Nether's iron.\n\nBelow, the bone that keeps\nan oath\nsleeps between two brothers\nof that same iron.",
  "If the ink resists you, press the seal below: the book will show you the forge, drawn."], "Show the forge", "The forging of the amulet"),
 "fr": ("L'énigme d'os", "Maître Velan", [
  "Qui veut ouvrir la porte tout en haut devra forger une amulette. Je l'ai écrit en vers, pour que seul un lecteur patient le comprenne.",
  "Deux lumières qui ne se brisent\ncouronnent la forge,\net entre elles repose\nle fer des enfers.\n\nDessous, l'os qui garde\nun serment\ndort entre deux frères\nde ce même fer.",
  "Si l'encre te résiste, appuie sur le sceau en bas : le livre te montrera la forge dessinée."], "Voir la forge", "La forge de l'amulette"),
 "de": ("Das Knochenrätsel", "Magister Velan", [
  "Wer die Tür ganz oben öffnen will, muss ein Amulett schmieden. Ich schrieb es in Versen auf, damit es nur versteht, wer geduldig liest.",
  "Zwei Lichter, die nie brechen,\nkrönen die Schmiede,\nund zwischen ihnen ruht\ndas Eisen der Unterwelt.\n\nDarunter schläft der Knochen,\nder einen Eid bewahrt,\nzwischen zwei Brüdern\naus demselben Eisen.",
  "Wenn dir die Tinte nicht verrät, was sie meint, drück das Siegel unten: Das Buch zeigt dir die Schmiede als Bild."], "Schmiede zeigen", "Die Schmiede des Amuletts"),
 "pt": ("O enigma de osso", "Mestre Velan", [
  "Quem quiser abrir a porta lá no alto terá de forjar um amuleto. Deixei escrito em verso, para que só o entenda quem ler com calma.",
  "Duas luzes que não se quebram\ncoroam a forja,\ne entre elas repousa\no ferro do inferno.\n\nEmbaixo, o osso que guarda\num juramento\ndorme entre dois irmãos\ndesse mesmo ferro.",
  "Se a tinta resistir, aperte o selo lá embaixo: o livro mostrará a forja desenhada."], "Ver a forja", "A forja do amuleto"),
 "ja": ("骨の謎かけ", "学匠ヴェラン", [
  "頂の扉を開きたい者は、護符を鍛えねばならない。落ち着いて読む者にだけ分かるよう、詩にして書き残す。",
  "砕けぬ二つの光が\n炉の上に冠し、\nそのあいだに眠るは\n冥府の鉄。\n\nその下で、誓いを守る骨が\n同じ鉄の\n二人の兄弟に挟まれて\n眠る。",
  "インクが手強ければ、下の封蝋を押すがよい。本が描かれた炉を見せてくれる。"], "炉を見る", "護符の鍛造"),
}

# capitulo 6: habla la Sombra (en ingles para la voz; los subtitulos en cada idioma)
NARRACION6 = [
 ("So the bone key has brought you here, above the clouds.", "Así que la llave de hueso te ha traído hasta aquí, sobre las nubes."),
 ("Aldous writes no more. From now on, I write this chronicle.", "Aldous ya no escribe. Desde ahora, esta crónica la escribo yo."),
 ("Those who dared not speak my name called me the Shadow.", "Los que no se atrevían a decir mi nombre me llamaron la Sombra."),
 ("I gave a king what he asked for. I gave him back his queen.", "Le di a un rey lo que pidió. Le devolví a su reina."),
 ("That he did not recognize her was not my fault.", "Que no la reconociera no fue culpa mía."),
 ("You have defeated his king. You have defeated his knight. You have read his secrets.", "Has vencido a su rey. Has vencido a su caballero. Has leído sus secretos."),
 ("And now you open my door with the amulet you forged with your own hands.", "Y ahora abres mi puerta con el amuleto que forjaste con tus propias manos."),
 ("Look closely at what lies on the other side.", "Mira bien lo que hay al otro lado."),
 ("You cannot cross yet. The threshold does not know you.", "Todavía no puedes cruzar. El umbral aún no te conoce."),
 ("But I know you. I have seen you in every reflection of my mirror.", "Pero yo sí te conozco. Te he visto en cada reflejo de mi espejo."),
 ("Come back when you are ready. I will be waiting.", "Vuelve cuando estés preparado. Te estaré esperando."),
 ("I always wait.", "Yo siempre espero."),
]
PAUSAS6 = [1.2, 1.4, 1.0, 0.9, 1.6, 1.2, 1.4, 1.8, 1.2, 1.6, 1.2, 0.0]
NARRACION6_OTROS = {
 "fr": ["Ainsi, la clé d'os t'a mené jusqu'ici, au-dessus des nuages.", "Aldous n'écrit plus. Désormais, c'est moi qui écris cette chronique.",
        "Ceux qui n'osaient dire mon nom m'ont appelée l'Ombre.", "J'ai donné à un roi ce qu'il demandait. Je lui ai rendu sa reine.",
        "Qu'il ne l'ait pas reconnue n'était pas ma faute.", "Tu as vaincu son roi. Tu as vaincu son chevalier. Tu as lu ses secrets.",
        "Et maintenant tu ouvres ma porte avec l'amulette que tu as forgée de tes mains.", "Regarde bien ce qui se trouve de l'autre côté.",
        "Tu ne peux pas encore passer. Le seuil ne te connaît pas.", "Mais moi, je te connais. Je t'ai vu dans chaque reflet de mon miroir.",
        "Reviens quand tu seras prêt. Je t'attendrai.", "J'attends toujours."],
 "de": ["So hat dich der Knochenschlüssel hierher gebracht, über die Wolken.", "Aldous schreibt nicht mehr. Von nun an schreibe ich diese Chronik.",
        "Die meinen Namen nicht auszusprechen wagten, nannten mich den Schatten.", "Ich gab einem König, worum er bat. Ich gab ihm seine Königin zurück.",
        "Dass er sie nicht erkannte, war nicht meine Schuld.", "Du hast seinen König besiegt. Du hast seinen Ritter besiegt. Du hast seine Geheimnisse gelesen.",
        "Und nun öffnest du meine Tür mit dem Amulett, das du mit eigenen Händen geschmiedet hast.", "Sieh genau hin, was auf der anderen Seite liegt.",
        "Du kannst noch nicht hindurch. Die Schwelle kennt dich nicht.", "Aber ich kenne dich. Ich habe dich in jedem Spiegelbild meines Spiegels gesehen.",
        "Komm zurück, wenn du bereit bist. Ich werde warten.", "Ich warte immer."],
 "pt": ["Então a chave de osso trouxe você até aqui, acima das nuvens.", "Aldous não escreve mais. De agora em diante, esta crônica quem escreve sou eu.",
        "Os que não ousavam dizer meu nome me chamaram de Sombra.", "Dei a um rei o que ele pediu. Devolvi-lhe a sua rainha.",
        "Que ele não a reconhecesse não foi culpa minha.", "Você venceu o rei dele. Venceu o cavaleiro dele. Leu os segredos dele.",
        "E agora abre minha porta com o amuleto que forjou com suas próprias mãos.", "Olhe bem o que há do outro lado.",
        "Você ainda não pode atravessar. O limiar ainda não o conhece.", "Mas eu conheço você. Eu o vi em cada reflexo do meu espelho.",
        "Volte quando estiver pronto. Estarei esperando.", "Eu sempre espero."],
 "ja": ["骨の鍵が、お前をここへ、雲の上へと導いたのだな。", "アルダスはもう書かない。これからは、この年代記は私が書く。",
        "私の名を口にできぬ者たちは、私を影と呼んだ。", "私は王の望みを叶えた。王妃を返してやったのだ。",
        "王が彼女を見分けられなかったのは、私のせいではない。", "お前は王を倒した。騎士を倒した。秘密を読んだ。",
        "そして今、自らの手で鍛えた護符で、私の扉を開く。", "向こう側にあるものを、よく見るがいい。",
        "まだ渡ることはできぬ。門はまだお前を知らない。", "だが私は知っている。我が鏡に映るたび、お前を見てきた。",
        "覚悟ができたら戻ってこい。待っている。", "私はいつも待っている。"],
}
TITULO6 = {"es": "La puerta sobre las nubes", "en": "The Door Above the Clouds", "fr": "La porte au-dessus des nuages",
           "de": "Die Tür über den Wolken", "pt": "A porta acima das nuvens", "ja": "雲の上の扉"}


def claves(lang):
    """Las claves de idioma de todo lo de este fichero para un idioma."""
    d = {}
    for n, libro in LIBROS.items():
        # (los que aun no estan traducidos salen en ingles)
        titulo, autor, paginas = libro.get(lang) or libro["en"]
        d[f"darkcraft.perdido.{n}.titulo"] = titulo
        d[f"darkcraft.perdido.{n}.autor"] = autor
        d[f"darkcraft.perdido.{n}.paginas"] = str(len(paginas))
        for i, p in enumerate(paginas):
            d[f"darkcraft.perdido.{n}.p{i + 1}"] = p
    titulo, autor, paginas, boton, receta = ACERTIJO[lang]
    d["darkcraft.acertijo.titulo"] = titulo
    d["darkcraft.acertijo.autor"] = autor
    d["darkcraft.acertijo.paginas"] = str(len(paginas))
    for i, p in enumerate(paginas):
        d[f"darkcraft.acertijo.p{i + 1}"] = p
    d["darkcraft.acertijo.boton"] = boton
    d["darkcraft.acertijo.receta"] = receta
    if lang == "es":
        lineas = [es for en, es in NARRACION6]
    elif lang == "en":
        lineas = [en for en, es in NARRACION6]
    else:
        lineas = NARRACION6_OTROS[lang]
    assert len(lineas) == len(NARRACION6), lang
    for i, l in enumerate(lineas):
        d[f"darkcraft.narracion.6.{i + 1}"] = l
    d["darkcraft.cap.6.titulo"] = TITULO6[lang]
    # el libro del capitulo 6: lo que dijo la Sombra en cuatro paginas (lo escribe ella: ya no Aldous)
    sep = "" if lang == "ja" else " "
    for k, (a, b) in enumerate(((0, 2), (2, 5), (5, 8), (8, 12))):
        d[f"darkcraft.cap.6.p{k + 1}"] = sep.join(lineas[a:b])
    d["darkcraft.cap.6.paginas"] = "4"
    d["darkcraft.cap.6.cartel1"] = TITULO6[lang]
    d["darkcraft.cap.6.cartel2"] = ""
    return d
