package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Marchito;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Puestos.Puesto;
import com.darkcraft.pueblo.gen.Casa;

import java.util.ArrayList;
import java.util.List;

/**
 * Los Marchitos de las Llanuras Palidas (parte 7e): donde espera cada uno ({@link Puestos}, en Morvain).
 * <ul>
 * <li><b>Velmor</b> (sitio {@value #VELMOR}): uno en cada cabeza del puente roto (el primero que se ve llegando del arco,
 * en la orilla de enfrente), por las calles, en la plaza (uno de rodillas ante la horca), rezando en la capilla y en el
 * cementerio, en la taberna, la herreria y la casa del alguacil, por los campos; y dos <b>campaneros</b>: a la puerta de la
 * capilla y en los campos del este. Si un campanero te ve, toca y acuden los de alrededor ({@link Marchito#LLAMADA}).</li>
 * <li><b>Las llanuras</b> (sitio {@value #LLANURAS}): tres que vagan solos entre el Arco del Umbral y Velmor.</li>
 * <li><b>El molino</b> (sitio {@value #MOLINO}, parte 7f): la Molinera de rodillas en medio de la era (un jefe: cae una
 * vez) y dos marchitos por el camino que va de Velmor al molino.</li>
 * <li><b>La Arboleda de los Ahorcados</b> (sitio {@value #ARBOLEDA}, parte 7g, en el Bosque Muerto): los ahorcados que cuelgan
 * de las sogas del Arbol de la Horca, de los arboles de alrededor, de la puerta y del patibulo ({@link Arboleda#colgados}),
 * tres de rodillas al pie del arbol, tres tumbados entre las tumbas, uno junto al patibulo y otro por el camino.</li>
 * <li><b>El patibulo</b> (sitio {@value #PATIBULO}, parte 7h): Maese Odran, el Verdugo, de pie en el tablado apoyado en su hacha
 * (un jefe: cae una vez). Mientras vive, los colgados de la arboleda vuelven a colgar; vencido, la cuenta se cierra y ya no
 * vuelven ({@link Arboleda#liberar}).</li>
 * <li><b>La Aldea de los Pilotes</b> (sitio {@value #PILOTES}, parte 7i, en el Pantano Podrido): los podridos que apunta el plano
 * ({@link Pilotes#esperas}): flotando boca abajo junto a las pasarelas, en la plaza, dentro de cada choza, rezando en la
 * capilla hundida y en el embarcadero.</li>
 * <li><b>La Laguna Negra</b> (sitio {@value #LAGUNA}, parte 7j): los podridos de la laguna ({@link LagunaNegra#esperas}): dos
 * flotando junto al muelle (se alzan al pasar) y los <b>pasajeros</b> del Barquero (cuatro flotando alrededor del embarcadero
 * y el del fuego fatuo en el islote del norte), que solo vuelven mientras el Barquero sigue en pie.</li>
 * <li><b>La barca</b> (sitio {@value #BARCA}, parte 7j): el Barquero, de pie en su barca con la linterna en alto (un jefe:
 * cae una vez). Vencido, la laguna queda en paz ({@link LagunaNegra#liberar}).</li>
 * <li><b>El Monasterio de la Helada</b> (sitio {@value #MONASTERIO}, parte 7k, en las Montanas Nevadas): los helados que apunta
 * el plano ({@link Monasterio#esperas}): de rodillas en los bancos de la iglesia, en el claustro, en las celdas y a la mesa
 * del refectorio; enterrados en la nieve del patio, del cementerio y del camino; de pie en la puerta y de ronda.</li>
 * <li><b>La Puerta Oeste de la Capital</b> (sitio {@value #CAPITAL}, parte 7m): los calcinados que apunta el plano
 * ({@link Capital#esperas}): guardias con su escudo en el camino, en la puerta, en el adarve y en la sala de la guardia;
 * incendiarios por las calles y en las ventanas; cenicientos enterrados en los montones de ceniza y dentro de las casas; y
 * los del cuartel.</li>
 * <li><b>La Academia Espectral</b> (sitio {@value #ACADEMIA}, parte 7o): los eruditos que apunta el plano ({@link Academia#esperas}):
 * aprendices leyendo en las galerias del claustro, a las mesas de la biblioteca, en los pupitres del scriptorium y en los bancos
 * del aula, o tumbados entre los libros; maestros de ronda (por el claustro y el altillo), en la catedra y en el archivo de la
 * torre; custodios en la puerta, entre los estantes, ante el grimorio y en la veta.</li>
 * <li><b>El Observatorio</b> (sitio {@value #OBSERVATORIO}, parte 7p): la Rectora leyendo el cielo en el estrado del telescopio (el
 * puesto 0, grupo de los jefes), dos custodios a la puerta del vestibulo, un maestro entre los instrumentos y dos aprendices en
 * el camino ({@link Observatorio#esperas}). Con la Rectora caida, los eruditos que esperaban leyendo (aqui y en la Academia)
 * descansan: ya no vuelven.</li>
 * <li><b>El Puerto Varado</b> (sitio {@value #PUERTO}, parte 7q): los salobres que apunta el plano ({@link Varadero#esperas}):
 * rederos remendando de rodillas en la plaza, en la lonja, en el muelle y en el espigon; bicheros de ronda o mirando al mar que no
 * esta (en el muelle, a la puerta y en lo alto del faro); salineros faenando en las salinas y enterrados en las costras de sal de
 * la bahia, junto a las barcas y dentro de la ballena.</li>
 * <li><b>El Costillar de la Ballena</b> (sitio {@value #COSTILLAR}, parte 7r): Brann, el Ballenero, de rodilla bajo las costillas
 * mirando al mar que no esta (el puesto 0, grupo de los jefes), y fuera dos rederos remendando junto a su choza, dos bicheros de
 * ronda (por la cola y junto a la calavera) y un salinero enterrado en la sal junto a la aleta ({@link Costillar#esperas}). Con
 * Brann caido, "vuelve la marea": los salobres que seguian faenando de rodillas (aqui y en el Puerto Varado) se deshacen en sal y
 * ya no vuelven.</li>
 * </ul>
 * Nadie a menos de 18 bloques del arco ni de 14 de un santuario.
 */
public final class MorvainEnemigos {
    private MorvainEnemigos() {}

    public static final String VELMOR = "velmor", LLANURAS = "llanuras", MOLINO = "molino", ARBOLEDA = "arboleda", PATIBULO = "patibulo",
            PILOTES = "pilotes", LAGUNA = "laguna", BARCA = "barca", MONASTERIO = "monasterio", ERMITA = "ermita", CUMBRE = "cumbre",
            CAPITAL = "capital", JURAMENTO = "juramento", ACADEMIA = "academia", OBSERVATORIO = "observatorio",
            PUERTO = "puerto", COSTILLAR = "costillar", GRIETA = "grieta", ALTAR = "altar", CERCO = "cerco", TEATRO = "teatro", NIDO = "nido",
            CADENA = "cadena", HOMENAJE = "homenaje", AGUJA = "aguja";
    static final String MARCHITO = "marchito", MOLINERA = "molinera", AHORCADO = "ahorcado", VERDUGO = "verdugo", PODRIDO = "podrido",
            BARQUERO = "barquero", HELADO = "helado", ABAD = "abad", CALCINADO = "calcinado", CONDESTABLE = "condestable",
            ERUDITO = "erudito", RECTORA = "rectora", SALOBRE = "salobre", BALLENERO = "ballenero", PENITENTE = "penitente", MARCADO = "marcado",
            PONTIFICE = "pontifice", ALDRIC = "aldric", BUFON = "bufon", DRAGON = "dragon", GARGOLA = "gargola", CENTINELA = "centinela",
            ISOLDE = "isolde", ESPECTRO = "espectro", PERRO = "perro", SOMBRA = "sombra";
    /** El tipo de puesto de las gargolas (para las pruebas). */
    public static final String GARGOLA_TIPO = GARGOLA;
    // hacia donde miran (el giro de Minecraft: 0 = sur, 90 = oeste, 180 = norte, -90 = este)
    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;
    private static final int DE_PIE = Enemigo.DE_PIE, REZA = Enemigo.REZA, TUMBADO = Enemigo.TUMBADO;
    private static final int G = Marchito.GUADANA, F = Marchito.FAROL, C = Marchito.CAMPANA;

    private static final List<Puesto> VELMOR_P = new ArrayList<>(), LLANURAS_P = new ArrayList<>(), MOLINO_P = new ArrayList<>(),
            ARBOLEDA_P = new ArrayList<>(), PATIBULO_P = new ArrayList<>(), PILOTES_P = new ArrayList<>(), LAGUNA_P = new ArrayList<>(),
            BARCA_P = new ArrayList<>(), MONASTERIO_P = new ArrayList<>(), ERMITA_P = new ArrayList<>(), CUMBRE_P = new ArrayList<>(),
            CAPITAL_P = new ArrayList<>(), JURAMENTO_P = new ArrayList<>(), ACADEMIA_P = new ArrayList<>(), OBSERVATORIO_P = new ArrayList<>(),
            PUERTO_P = new ArrayList<>(), COSTILLAR_P = new ArrayList<>(), GRIETA_P = new ArrayList<>(), ALTAR_P = new ArrayList<>(),
            CERCO_P = new ArrayList<>(), TEATRO_P = new ArrayList<>(), NIDO_P = new ArrayList<>(), CADENA_P = new ArrayList<>(),
            HOMENAJE_P = new ArrayList<>(), AGUJA_P = new ArrayList<>();

    /** Uno fuera, de pie en el suelo de Morvain. */
    private static void fuera(List<Puesto> l, int variante, int x, int z, float giro, int postura, int ronda, String grupo) {
        l.add(new Puesto(MARCHITO, variante, x, Velmor.pie(x, z), z, giro, postura, ronda, grupo));
    }

    /** Uno a una altura dada (el puente, la capilla, dentro de una casa). */
    private static void en(List<Puesto> l, int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {
        l.add(new Puesto(MARCHITO, variante, x, y, z, giro, postura, ronda, grupo));
    }

    /** Dentro de una casa: (a, b) en las coordenadas de quien entra. */
    private static void casa(List<Puesto> l, Casa k, int variante, int a, int b, float giro, int postura, String grupo) {
        l.add(new Puesto(MARCHITO, variante, k.lx(a, b), k.y, k.lz(a, b), giro, postura, 0, grupo));
    }

    static synchronized void preparar() {
        if (!VELMOR_P.isEmpty()) return;
        Velmor v = Velmor.get();
        List<Puesto> l = VELMOR_P;
        int t = Velmor.TABLERO + 1, g = v.capillaY;
        // ---- el puente roto: uno en cada cabeza (el de la orilla del oeste mira a quien llega del arco)
        en(l, G, Velmor.PUENTE_X0 + 6, t, Velmor.PUENTE_Z, OESTE, DE_PIE, 0, "puente");
        en(l, F, Velmor.PUENTE_X1 - 4, t, Velmor.PUENTE_Z + 1, OESTE, DE_PIE, 0, "puente");
        // ---- la calle del puente
        fuera(l, G, 790, 4041, ESTE, DE_PIE, 5, "calle");
        fuera(l, G, 818, 4039, OESTE, DE_PIE, 0, "calle");
        fuera(l, G, 905, 4041, ESTE, DE_PIE, 0, "calle");
        fuera(l, G, 940, 4038, OESTE, TUMBADO, 0, "calle");
        // ---- la plaza: el del farol junto al pozo, uno de rodillas ante la horca, otro tirado
        fuera(l, F, 850, 4034, SUR, DE_PIE, 3, "plaza");
        fuera(l, G, 854, 4029, SUR, REZA, 0, "plaza");
        fuera(l, G, 842, 4041, ESTE, TUMBADO, 0, "plaza");
        // ---- la calle mayor
        fuera(l, G, 850, 3995, SUR, DE_PIE, 4, "calle");
        fuera(l, G, 849, 3962, SUR, DE_PIE, 0, "calle");
        fuera(l, G, 851, 4100, NORTE, DE_PIE, 5, "calle");
        fuera(l, F, 850, 4146, SUR, DE_PIE, 0, "puerta_sur");
        // ---- la capilla: el campanero a la puerta, dos rezando en los bancos y uno ante el altar
        en(l, C, 850, g, 3931, SUR, DE_PIE, 0, "capilla");
        en(l, G, 844, g, 3917, NORTE, REZA, 0, "capilla");
        en(l, G, 856, g, 3914, NORTE, REZA, 0, "capilla");
        en(l, F, 850, g, 3909, SUR, DE_PIE, 0, "capilla");
        // ---- el cementerio
        fuera(l, F, 820, 3914, ESTE, DE_PIE, 3, "cementerio");
        fuera(l, G, 817, 3917, SUR, TUMBADO, 0, "cementerio");
        fuera(l, G, 814, 3913, NORTE, REZA, 0, "cementerio");
        // ---- dentro: la taberna (dos sentados a sus jarras), la herreria y la casa del alguacil
        casa(l, v.taberna, G, 2, 3, ESTE, REZA, "taberna");
        casa(l, v.taberna, G, v.taberna.A - 3, 5, OESTE, REZA, "taberna");
        casa(l, v.herreria, G, 3, 4, NORTE, DE_PIE, "herreria");
        casa(l, v.alcaldia, F, 4, 4, SUR, DE_PIE, "alcaldia");
        // ---- los campos: el otro campanero en los del este, y quien sigue segando
        fuera(l, C, 920, 4090, NORTE, DE_PIE, 0, "campos");
        fuera(l, F, 935, 4105, OESTE, DE_PIE, 4, "campos");
        fuera(l, G, 910, 4075, ESTE, REZA, 0, "campos");
        fuera(l, G, 780, 3970, SUR, DE_PIE, 5, "campos");
        fuera(l, G, 771, 3986, ESTE, TUMBADO, 0, "campos");
        // ---- los que vagan por las llanuras, entre el arco y Velmor
        int[][] solos = {{250, 3640}, {420, 3790}, {596, 3905}};
        for (int[] s : solos) {
            int x = s[0], z = s[1];
            LLANURAS_P.add(new Puesto(MARCHITO, G, x, (int) Math.floor(Terreno.columna(x, z).altura()) + 1, z, NORTE, DE_PIE, 8, "llanuras"));
        }
        // ---- el molino (parte 7f): la Molinera de rodillas ante la piedra de moler, mirando al molino (el puesto 0); y
        // por el camino desde Velmor, uno con su farol que mira hacia el pueblo y otro de rodillas mirando al molino
        MOLINO_P.add(new Puesto(MOLINERA, 0, Molino.EX, Molino.Y + 1, Molino.EZ, ESTE, REZA, 0, Puestos.JEFE));
        MOLINO_P.add(new Puesto(MARCHITO, F, 1030, Molino.pie(1030, 4055), 4055, OESTE, DE_PIE, 4, "camino_molino"));
        MOLINO_P.add(new Puesto(MARCHITO, G, 1062, Molino.pie(1062, 4059), 4059, ESTE, REZA, 0, "camino_molino"));
        // ---- la Arboleda de los Ahorcados (parte 7g): los que cuelgan de las sogas (bajo cada una) y los demas
        Arboleda a = Arboleda.get();
        for (Arboleda.Colgado c : a.colgados())
            ARBOLEDA_P.add(new Puesto(AHORCADO, c.variante(), c.x(), c.y(), c.z(), c.giro(), Enemigo.COLGADO, 0, "colgados"));
        for (Arboleda.Otro o : a.otros())
            ARBOLEDA_P.add(new Puesto(AHORCADO, o.variante(), o.x(), o.y(), o.z(), o.giro(), o.postura(), o.ronda(), o.grupo()));
        // ---- el patibulo (parte 7h): el Verdugo de pie en el tablado, apoyado en el hacha, mirando al Arbol de la Horca
        int[] ve = a.sitio("verdugo");
        PATIBULO_P.add(new Puesto(VERDUGO, 0, ve[0], ve[1], ve[2], SUR, REZA, 0, Puestos.JEFE));
        // ---- la Aldea de los Pilotes (parte 7i): los podridos que apunta el plano
        for (Pilotes.Espera e : Pilotes.get().esperas())
            PILOTES_P.add(new Puesto(PODRIDO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- la Laguna Negra (parte 7j): los podridos de la laguna (los del muelle y los pasajeros del Barquero)
        LagunaNegra ln = LagunaNegra.get();
        for (LagunaNegra.Espera e : ln.esperas())
            LAGUNA_P.add(new Puesto(PODRIDO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- la barca (parte 7j): el Barquero de pie en ella, la linterna en alto, mirando al embarcadero (al oeste)
        int[] bq = ln.sitio("barquero");
        BARCA_P.add(new Puesto(BARQUERO, 0, bq[0], bq[1], bq[2], OESTE, REZA, 0, Puestos.JEFE));
        // ---- el Monasterio de la Helada (parte 7k): los helados que apunta el plano
        for (Monasterio.Espera e : Monasterio.get().esperas())
            MONASTERIO_P.add(new Puesto(HELADO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- la escalera de los penitentes y el calvario (parte 7l): los helados de rodillas en los rellanos y los enterrados
        Ermita er = Ermita.get();
        for (Ermita.Espera e : er.esperas())
            ERMITA_P.add(new Puesto(HELADO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- la cumbre (parte 7l): el Abad, de rodillas en la rosa de hielo del calvario, rezando de cara a la ermita
        int[] ab = er.sitio("abad");
        CUMBRE_P.add(new Puesto(ABAD, 0, ab[0], ab[1], ab[2], NORTE, REZA, 0, Puestos.JEFE));
        // ---- la Puerta Oeste de la Capital (parte 7m): los calcinados que apunta el plano
        for (Capital.Espera e : Capital.get().esperas())
            CAPITAL_P.add(new Puesto(CALCINADO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- el Patio del Juramento (parte 7n): el Condestable de rodillas en el estrado, de cara a la puerta (el puesto 0), y
        // la guardia de su puerta
        for (Juramento.Espera e : Juramento.get().esperas())
            JURAMENTO_P.add(new Puesto(Puestos.JEFE.equals(e.grupo()) ? CONDESTABLE : CALCINADO, e.variante(), e.x(), e.y(), e.z(), e.giro(),
                    e.postura(), e.ronda(), e.grupo()));
        // ---- la Academia Espectral (parte 7o): los eruditos que apunta el plano
        for (Academia.Espera e : Academia.get().esperas())
            ACADEMIA_P.add(new Puesto(ERUDITO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- el Observatorio (parte 7p): la Rectora ante el telescopio (el puesto 0) y los eruditos de su puerta y su camino
        for (Observatorio.Espera e : Observatorio.get().esperas())
            OBSERVATORIO_P.add(new Puesto(Puestos.JEFE.equals(e.grupo()) ? RECTORA : ERUDITO, e.variante(), e.x(), e.y(), e.z(), e.giro(),
                    e.postura(), e.ronda(), e.grupo()));
        // ---- el Puerto Varado (parte 7q): los salobres que apunta el plano
        for (Varadero.Espera e : Varadero.get().esperas())
            PUERTO_P.add(new Puesto(SALOBRE, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- el Costillar de la Ballena (parte 7r): Brann bajo las costillas (el puesto 0) y los salobres de alrededor
        for (Costillar.Espera e : Costillar.get().esperas())
            COSTILLAR_P.add(new Puesto(Puestos.JEFE.equals(e.grupo()) ? BALLENERO : SALOBRE, e.variante(), e.x(), e.y(), e.z(), e.giro(),
                    e.postura(), e.ronda(), e.grupo()));
        // ---- el Puente de la Grieta (parte 7s): los penitentes que apunta el plano
        for (Grieta.Espera e : Grieta.get().esperas())
            GRIETA_P.add(new Puesto(PENITENTE, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- el Altar Profanado (parte 7t): Anselmo ante el altar (el puesto 0) y los penitentes de la basilica y de alrededor
        for (Altar.Espera e : Altar.get().esperas())
            ALTAR_P.add(new Puesto(Puestos.JEFE.equals(e.grupo()) ? PONTIFICE : PENITENTE, e.variante(), e.x(), e.y(), e.z(), e.giro(),
                    e.postura(), e.ronda(), e.grupo()));
        // ---- el Cerco del Ancla (parte 7u): los marcados que apunta el plano
        // (y Aldric, el Primer Marcado, en lo alto del ancla: el puesto 0, parte 7v)
        for (Cerco.Espera e : Cerco.get().esperas())
            CERCO_P.add(new Puesto(Puestos.JEFE.equals(e.grupo()) ? ALDRIC : MARCADO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(),
                    e.ronda(), e.grupo()));
        // ---- el Teatro Hundido (parte 7y): Pell, el Bufon del Rey, en su sala (el puesto 0) y los calcinados del teatro
        for (Teatro.Espera e : Teatro.get().esperas())
            TEATRO_P.add(new Puesto(Puestos.JEFE.equals(e.grupo()) ? BUFON : CALCINADO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(),
                    e.ronda(), e.grupo()));
        // ---- el Nido de Ceniza (parte 7z): Vorthak, el Dragon de Ceniza, dormido sobre su oro (el puesto 0) y los calcinados de la
        // guarnicion de la Atalaya del Cuervo
        for (Nido.Espera e : Nido.get().esperas())
            NIDO_P.add(new Puesto(Puestos.JEFE.equals(e.grupo()) ? DRAGON : CALCINADO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(),
                    e.ronda(), e.grupo()));
        // ---- la subida por la cadena del sur (parte 7aa): los marcados que subieron y se quedaron en los rellanos y las gargolas
        // (en sus pedestales, de piedra, o en ronda por el aire)
        for (Subida.Espera e : Subida.get().esperas())
            CADENA_P.add(new Puesto(GARGOLA.equals(e.tipo()) ? GARGOLA : MARCADO, e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(),
                    e.ronda(), e.grupo()));
        // ---- la casa de la Sombra (parte 7ab): la reina Isolde en su trono (el puesto 0) y los de la torre del homenaje (los
        // centinelas, los espectros del archivo, los marcados de la prision y sus perros); el tipo de cada uno lo dice el plano
        for (Homenaje.Espera e : Homenaje.get().esperas())
            HOMENAJE_P.add(new Puesto(e.tipo(), e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
        // ---- el Corazon de la Aguja (parte 7ac): la Sombra sin Nombre, colgada de sus cadenas en medio de la sala (el puesto 0)
        for (Homenaje.Espera e : Homenaje.get().esperasAguja())
            AGUJA_P.add(new Puesto(e.tipo(), e.variante(), e.x(), e.y(), e.z(), e.giro(), e.postura(), e.ronda(), e.grupo()));
    }

    /** El numero del puesto de la Molinera en el sitio del molino. */
    public static int molinera() {
        return 0;
    }

    /** El numero del puesto del Verdugo en el sitio del patibulo. */
    public static int verdugo() {
        return 0;
    }

    /** El numero del puesto del Barquero en el sitio de la barca. */
    public static int barquero() {
        return 0;
    }

    /** El numero del puesto del Abad en el sitio de la cumbre. */
    public static int abad() {
        return 0;
    }

    /** El numero del puesto del Condestable en el sitio del Juramento. */
    public static int condestable() {
        return 0;
    }

    /** El numero del puesto de la Rectora en el sitio del Observatorio. */
    public static int rectora() {
        return 0;
    }

    /** El numero del puesto de Brann, el Ballenero, en el sitio del Costillar. */
    public static int ballenero() {
        return 0;
    }

    /** El numero del puesto de Anselmo, el Pontifice Sangrante, en el sitio del Altar. */
    public static int pontifice() {
        return 0;
    }

    /** El numero del puesto de Aldric, el Primer Marcado, en el sitio del Cerco. */
    public static int aldric() {
        return 0;
    }

    /** El numero del puesto de Pell, el Bufon del Rey, en el sitio del Teatro. */
    public static int bufon() {
        return 0;
    }

    /** El numero del puesto de Vorthak, el Dragon de Ceniza, en el sitio del Nido. */
    public static int dragon() {
        return 0;
    }

    /** El numero del puesto de la reina Isolde en el sitio de la torre del homenaje. */
    public static int isolde() {
        return Homenaje.ISOLDE;
    }

    /** Lo apunta el mod al arrancar. */
    public static void registrar() {
        Puestos.tipo(MARCHITO, DarkCraftMod.MARCHITO);
        Puestos.tipo(MOLINERA, DarkCraftMod.MOLINERA);
        Puestos.tipo(AHORCADO, DarkCraftMod.AHORCADO);
        Puestos.tipo(VERDUGO, DarkCraftMod.VERDUGO);
        Puestos.tipo(PODRIDO, DarkCraftMod.PODRIDO);
        Puestos.tipo(BARQUERO, DarkCraftMod.BARQUERO);
        Puestos.tipo(HELADO, DarkCraftMod.HELADO);
        Puestos.tipo(ABAD, DarkCraftMod.ABAD);
        Puestos.tipo(CALCINADO, DarkCraftMod.CALCINADO);
        Puestos.tipo(CONDESTABLE, DarkCraftMod.CONDESTABLE);
        Puestos.tipo(ERUDITO, DarkCraftMod.ERUDITO);
        Puestos.tipo(RECTORA, DarkCraftMod.RECTORA);
        Puestos.tipo(SALOBRE, DarkCraftMod.SALOBRE);
        Puestos.tipo(BALLENERO, DarkCraftMod.BALLENERO);
        Puestos.tipo(PENITENTE, DarkCraftMod.PENITENTE);
        Puestos.tipo(MARCADO, DarkCraftMod.MARCADO);
        Puestos.tipo(PONTIFICE, DarkCraftMod.PONTIFICE);
        Puestos.tipo(ALDRIC, DarkCraftMod.ALDRIC);
        Puestos.tipo(BUFON, DarkCraftMod.BUFON);
        Puestos.tipo(DRAGON, DarkCraftMod.DRAGON);
        Puestos.tipo(GARGOLA, DarkCraftMod.GARGOLA);
        Puestos.tipo(CENTINELA, DarkCraftMod.CENTINELA);
        Puestos.tipo(ISOLDE, DarkCraftMod.ISOLDE);
        Puestos.tipo(SOMBRA, DarkCraftMod.SOMBRA);
        Puestos.tipo(ESPECTRO, DarkCraftMod.ESPECTRO);
        Puestos.tipo(PERRO, DarkCraftMod.PERRO);
        preparar();
        Puestos.registrar(VELMOR, VELMOR_P, Morvain::es);
        Puestos.registrar(LLANURAS, LLANURAS_P, Morvain::es);
        Puestos.registrar(MOLINO, MOLINO_P, Morvain::es);
        // los colgados solo mientras vive el Verdugo (vencido, la cuenta se cierra: ya no vuelven a colgar)
        Puestos.registrar(ARBOLEDA, ARBOLEDA_P, Morvain::es, (l, p) -> !"colgados".equals(p.grupo()) || !Arboleda.cuentaCerrada(l.getServer()));
        Puestos.registrar(PATIBULO, PATIBULO_P, Morvain::es);
        Puestos.registrar(PILOTES, PILOTES_P, Morvain::es);
        // los pasajeros del Barquero solo mientras vive el (vencido, la laguna queda en paz: ya no vuelven)
        Puestos.registrar(LAGUNA, LAGUNA_P, Morvain::es, (l, p) -> !"pasajeros".equals(p.grupo()) || !LagunaNegra.enPaz(l.getServer()));
        Puestos.registrar(BARCA, BARCA_P, Morvain::es);
        // los hermanos de rodillas en la iglesia solo mientras vive el Abad (vencido, descansan: ya no vuelven)
        Puestos.registrar(MONASTERIO, MONASTERIO_P, Morvain::es, (l, p) -> !"iglesia".equals(p.grupo()) || !Ermita.enPaz(l.getServer()));
        Puestos.registrar(ERMITA, ERMITA_P, Morvain::es);
        Puestos.registrar(CUMBRE, CUMBRE_P, Morvain::es);
        // la guardia de la Puerta Oeste y los que querian salir solo mientras vive el Condestable (vencido, descansan)
        Puestos.registrar(CAPITAL, CAPITAL_P, Morvain::es, (l, p) -> !Juramento.DESCANSAN.contains(p.grupo()) || !Juramento.enPaz(l.getServer()));
        Puestos.registrar(JURAMENTO, JURAMENTO_P, Morvain::es);
        // los eruditos que esperan leyendo solo mientras vive la Rectora (vencida, el cielo deja de leerlos: descansan)
        Puestos.registrar(ACADEMIA, ACADEMIA_P, Morvain::es, (l, p) -> p.postura() != REZA || !Observatorio.enPaz(l.getServer()));
        Puestos.registrar(OBSERVATORIO, OBSERVATORIO_P, Morvain::es,
                (l, p) -> Puestos.JEFE.equals(p.grupo()) || p.postura() != REZA || !Observatorio.enPaz(l.getServer()));
        // los salobres que faenan de rodillas solo mientras vive Brann (vencido, vuelve la marea: se deshacen en sal)
        Puestos.registrar(PUERTO, PUERTO_P, Morvain::es, (l, p) -> p.postura() != REZA || !Costillar.enPaz(l.getServer()));
        Puestos.registrar(COSTILLAR, COSTILLAR_P, Morvain::es,
                (l, p) -> Puestos.JEFE.equals(p.grupo()) || p.postura() != REZA || !Costillar.enPaz(l.getServer()));
        // los penitentes que rezan de rodillas solo mientras vive Anselmo (vencido, la Tierra sin Fe descansa: se deshacen en sangre)
        Puestos.registrar(GRIETA, GRIETA_P, Morvain::es, (l, p) -> p.postura() != REZA || !Altar.enPaz(l.getServer()));
        Puestos.registrar(ALTAR, ALTAR_P, Morvain::es,
                (l, p) -> Puestos.JEFE.equals(p.grupo()) || p.postura() != REZA || !Altar.enPaz(l.getServer()));
        // los marcados que golpean el ancla de rodillas solo mientras vive Aldric (vencido, descansan: se deshacen en sombra)
        Puestos.registrar(CERCO, CERCO_P, Morvain::es,
                (l, p) -> Puestos.JEFE.equals(p.grupo()) || p.postura() != REZA || !Cerco.enPaz(l.getServer()));
        // el Teatro Hundido: los calcinados del teatro y Pell en su sala
        Puestos.registrar(TEATRO, TEATRO_P, Morvain::es);
        // el Nido de Ceniza: los calcinados de la atalaya y del camino, y Vorthak sobre su oro
        Puestos.registrar(NIDO, NIDO_P, Morvain::es);
        // la cadena del sur: los marcados de los rellanos y las gargolas
        Puestos.registrar(CADENA, CADENA_P, Morvain::es);
        // la torre del homenaje: los centinelas, los del archivo y la prision, y la reina en su trono
        Puestos.registrar(HOMENAJE, HOMENAJE_P, Morvain::es);
        // la aguja: la Sombra (cae una vez y no vuelve: es del grupo de los jefes)
        Puestos.registrar(AGUJA, AGUJA_P, Morvain::es);
        // al caer la Molinera se abre la reja del molino
        Puestos.alCaer((e, quien) -> {
            if (e instanceof com.darkcraft.enemigos.Molinera && MOLINO.equals(e.sitio()) && e.level() instanceof net.minecraft.server.level.ServerLevel sl)
                Molino.puerta(sl, true, true);
            // y al caer el Verdugo, los colgados de la arboleda descansan
            if (e instanceof com.darkcraft.enemigos.Verdugo && PATIBULO.equals(e.sitio()) && e.level() instanceof net.minecraft.server.level.ServerLevel sl)
                Arboleda.liberar(sl);
            // y al caer el Barquero, la laguna queda en paz
            if (e instanceof com.darkcraft.enemigos.Barquero && BARCA.equals(e.sitio()) && e.level() instanceof net.minecraft.server.level.ServerLevel sl)
                LagunaNegra.liberar(sl);
            // y al caer el Abad, los hermanos de la iglesia del monasterio descansan
            if (e instanceof com.darkcraft.enemigos.Abad && CUMBRE.equals(e.sitio()) && e.level() instanceof net.minecraft.server.level.ServerLevel sl)
                Ermita.liberar(sl);
            // y al caer el Condestable, la capital deja de guardar sus puertas (lo hace Juramento.paz cada segundo; aqui, el aviso)
            if (e instanceof com.darkcraft.enemigos.Condestable && JURAMENTO.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] El Condestable del Juramento cae: se abren las puertas de la capital");
            // y al caer la Rectora, los que leian en la academia descansan (lo hace Observatorio.paz cada segundo; aqui, el aviso)
            if (e instanceof com.darkcraft.enemigos.Rectora && OBSERVATORIO.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] La Rectora cae: el cielo deja de leer la academia");
            // y al caer Brann vuelve la marea: los salobres que faenaban descansan (lo hace Costillar.paz cada segundo; aqui, el aviso)
            if (e instanceof com.darkcraft.enemigos.Ballenero && COSTILLAR.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] Brann, el Ballenero, cae: vuelve la marea a la Costa de los Huesos");
            // y al caer el Pontifice la Tierra sin Fe descansa (lo hace Altar.paz cada segundo; aqui, el aviso)
            if (e instanceof com.darkcraft.enemigos.Pontifice && ALTAR.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] Anselmo, el Pontifice Sangrante, cae: la Tierra sin Fe descansa");
            // y al caer Aldric los marcados del cerco descansan y la cadena del sur queda libre (lo hace Cerco.paz; aqui, el aviso)
            if (e instanceof com.darkcraft.enemigos.Aldric && CERCO.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] Aldric, el Primer Marcado, cae: los marcados del cerco descansan y la cadena queda libre");
            // y al caer Pell, el teatro vacio aplaude (lo hace su muerte; aqui, el aviso)
            if (e instanceof com.darkcraft.enemigos.Bufon && TEATRO.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] Pell, el Bufon del Rey, cae: la ultima funcion se acaba");
            // y al caer Vorthak el nido queda en silencio (su oro, para quien suba)
            if (e instanceof com.darkcraft.enemigos.Dragon && NIDO.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] Vorthak, el Dragon de Ceniza, cae: el Nido de Ceniza queda en silencio");
            // y al caer la reina Isolde se abre el salon y la reja de la escalera de la azotea (lo hace CasaSombra cada segundo)
            if (e instanceof com.darkcraft.enemigos.Isolde && HOMENAJE.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] La reina Isolde cae: se abre la escalera de la azotea");
            // y al caer la Sombra (despues de responder) se acaba: lo apunta Aguja.alMorir; aqui, el aviso
            if (e instanceof com.darkcraft.enemigos.Sombra && AGUJA.equals(e.sitio()))
                DarkCraftMod.LOGGER.info("[DarkCraft] La Sombra sin Nombre cae: se acaba");
        });
    }

    public static List<Puesto> molino() {
        preparar();
        return List.copyOf(MOLINO_P);
    }

    public static List<Puesto> arboleda() {
        preparar();
        return List.copyOf(ARBOLEDA_P);
    }

    public static List<Puesto> patibulo() {
        preparar();
        return List.copyOf(PATIBULO_P);
    }

    public static List<Puesto> pilotes() {
        preparar();
        return List.copyOf(PILOTES_P);
    }

    public static List<Puesto> laguna() {
        preparar();
        return List.copyOf(LAGUNA_P);
    }

    public static List<Puesto> barca() {
        preparar();
        return List.copyOf(BARCA_P);
    }

    public static List<Puesto> monasterio() {
        preparar();
        return List.copyOf(MONASTERIO_P);
    }

    public static List<Puesto> ermita() {
        preparar();
        return List.copyOf(ERMITA_P);
    }

    public static List<Puesto> cumbre() {
        preparar();
        return List.copyOf(CUMBRE_P);
    }

    public static List<Puesto> capital() {
        preparar();
        return List.copyOf(CAPITAL_P);
    }

    public static List<Puesto> juramento() {
        preparar();
        return List.copyOf(JURAMENTO_P);
    }

    public static List<Puesto> academia() {
        preparar();
        return List.copyOf(ACADEMIA_P);
    }

    public static List<Puesto> observatorio() {
        preparar();
        return List.copyOf(OBSERVATORIO_P);
    }

    public static List<Puesto> grieta() {
        preparar();
        return List.copyOf(GRIETA_P);
    }

    public static List<Puesto> altar() {
        preparar();
        return List.copyOf(ALTAR_P);
    }

    public static List<Puesto> cerco() {
        preparar();
        return List.copyOf(CERCO_P);
    }

    public static List<Puesto> teatro() {
        preparar();
        return List.copyOf(TEATRO_P);
    }

    public static List<Puesto> nido() {
        preparar();
        return List.copyOf(NIDO_P);
    }

    public static List<Puesto> cadena() {
        preparar();
        return List.copyOf(CADENA_P);
    }

    public static List<Puesto> homenaje() {
        preparar();
        return List.copyOf(HOMENAJE_P);
    }

    public static List<Puesto> aguja() {
        preparar();
        return List.copyOf(AGUJA_P);
    }

    public static List<Puesto> puerto() {
        preparar();
        return List.copyOf(PUERTO_P);
    }

    public static List<Puesto> costillar() {
        preparar();
        return List.copyOf(COSTILLAR_P);
    }

    public static List<Puesto> velmor() {
        preparar();
        return List.copyOf(VELMOR_P);
    }

    public static List<Puesto> llanuras() {
        preparar();
        return List.copyOf(LLANURAS_P);
    }

    // ------------------------------------------------------------------ el comando

    /** {@code /darkcraft velmor [ir <sitio> | enemigos [volver | fuera | sacar]]}. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("velmor")
                .executes(c -> {
                    var src = c.getSource();
                    src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(comprobar() + "; " + estado(src.getServer())), false);
                    return 1;
                })
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> ir(c.getSource(), "pozo"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Velmor.get().sitios().keySet(), b))
                                .executes(c -> ir(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> {
                            var src = c.getSource();
                            src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(src.getServer())), false);
                            return 1;
                        })
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), VELMOR, false);
                            Puestos.todos(c.getSource().getServer(), LLANURAS, false);
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("vuelven todos los marchitos"), false);
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), VELMOR, true);
                            Puestos.todos(c.getSource().getServer(), LLANURAS, true);
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("fuera todos los marchitos"), false);
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, VELMOR).size();
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("fuera " + n), false);
                            return n;
                        })));
    }

    private static int ir(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Velmor.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Velmor.get().sitios().keySet() + ")"));
            return 0;
        }
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 1.5, 180f, 10f);
        return 1;
    }

    /** Cuantos quedan en pie y cuantos andan fuera. */
    public static String estado(net.minecraft.server.MinecraftServer srv) {
        return "Velmor: quedan " + Puestos.quedan(srv, VELMOR) + " de " + VELMOR_P.size() + " (fuera " + Puestos.fuera(VELMOR) + "); llanuras: quedan "
                + Puestos.quedan(srv, LLANURAS) + " de " + LLANURAS_P.size() + "; molino: quedan " + Puestos.quedan(srv, MOLINO) + " de " + MOLINO_P.size()
                + " (la Molinera " + (Molino.vencida(srv) ? "caida" : Puestos.enemigo(MOLINO, molinera()) != null ? "fuera" : "esperando") + ")";
    }

    /** {@code /darkcraft arboleda [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7g). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoArboleda() {
        return net.minecraft.commands.Commands.literal("arboleda")
                .executes(c -> decir(c, estadoArboleda(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irArboleda(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Arboleda.get().sitios().keySet(), b))
                                .executes(c -> irArboleda(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoArboleda(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ARBOLEDA, false);
                            return decir(c, "vuelven todos los ahorcados (y vuelven a colgar)");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ARBOLEDA, true);
                            return decir(c, "fuera todos los ahorcados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, ARBOLEDA).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irArboleda(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Arboleda.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Arboleda.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al arbol de la horca
        float giro = (float) (Math.toDegrees(Math.atan2(Arboleda.GZ - s[2], Arboleda.GX - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos ahorcados quedan y cuantos andan fuera (y cuantos cuelgan todavia). */
    public static String estadoArboleda(net.minecraft.server.MinecraftServer srv) {
        int cuelgan = 0;
        var sitio = Puestos.sitio(ARBOLEDA);
        if (sitio != null)
            for (int i = 0; i < ARBOLEDA_P.size(); i++)
                if (Puestos.enemigo(ARBOLEDA, i) instanceof com.darkcraft.enemigos.Ahorcado ah && ah.cuelga()) cuelgan++;
        String s = "arboleda: quedan " + Puestos.quedan(srv, ARBOLEDA) + " de " + ARBOLEDA_P.size() + " (fuera " + Puestos.fuera(ARBOLEDA) + ", colgados "
                + cuelgan + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Arboleda.PX, 80, Arboleda.PZ))) s += "; " + Arboleda.resumen(m);
        return s;
    }

    /** {@code /darkcraft pilotes [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7i). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoPilotes() {
        return net.minecraft.commands.Commands.literal("pilotes")
                .executes(c -> decir(c, estadoPilotes(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irPilotes(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Pilotes.get().sitios().keySet(), b))
                                .executes(c -> irPilotes(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoPilotes(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), PILOTES, false);
                            return decir(c, "vuelven todos los podridos");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), PILOTES, true);
                            return decir(c, "fuera todos los podridos");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, PILOTES).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irPilotes(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Pilotes.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Pilotes.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando a la plaza
        float giro = (float) (Math.toDegrees(Math.atan2(Pilotes.CZ - s[2], Pilotes.CX - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 8f);
        return 1;
    }

    /** Cuantos podridos quedan y cuantos andan fuera (y como esta la aldea, si esta cargada). */
    public static String estadoPilotes(net.minecraft.server.MinecraftServer srv) {
        String s = "aldea de los pilotes: quedan " + Puestos.quedan(srv, PILOTES) + " de " + PILOTES_P.size() + " (fuera " + Puestos.fuera(PILOTES) + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Pilotes.CX, Pilotes.PASO, Pilotes.CZ - 37))) s += "; " + Pilotes.resumen(m);
        return s;
    }

    /** {@code /darkcraft laguna [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7j). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoLaguna() {
        return net.minecraft.commands.Commands.literal("laguna")
                .executes(c -> decir(c, estadoLaguna(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irLaguna(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(LagunaNegra.get().sitios().keySet(), b))
                                .executes(c -> irLaguna(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoLaguna(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), LAGUNA, false);
                            return decir(c, "vuelven todos los podridos de la laguna");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), LAGUNA, true);
                            return decir(c, "fuera todos los podridos de la laguna");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, LAGUNA).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irLaguna(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = LagunaNegra.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + LagunaNegra.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al embarcadero (desde el embarcadero, a la barca)
        double tx = LagunaNegra.CX, tz = LagunaNegra.CZ;
        if (Math.hypot(s[0] - tx, s[2] - tz) < 3) tx += LagunaNegra.BARCA_U;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 8f);
        return 1;
    }

    /** Cuantos podridos de la laguna quedan y cuantos andan fuera (y como esta la laguna, si esta cargada). */
    public static String estadoLaguna(net.minecraft.server.MinecraftServer srv) {
        String s = "laguna negra: quedan " + Puestos.quedan(srv, LAGUNA) + " de " + LAGUNA_P.size() + " (fuera " + Puestos.fuera(LAGUNA) + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(LagunaNegra.CX + LagunaNegra.BARCA_U, LagunaNegra.PASO, LagunaNegra.CZ)))
            s += "; " + LagunaNegra.resumen(m);
        return s;
    }

    /** {@code /darkcraft barquero [ir | vencer | volver]}: como esta el Barquero, llevar al embarcadero, vencerle o que vuelva. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoBarquero() {
        return net.minecraft.commands.Commands.literal("barquero")
                .executes(c -> decir(c, estadoBarquero(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    var sp = c.getSource().getPlayerOrException();
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    // al embarcadero, junto al muelle, mirando a la barca (al este)
                    int[] e = LagunaNegra.get().sitio("ante_embarcadero");
                    sp.teleportTo(m, e[0] + 0.5, e[1] + 0.05, e[2] + 0.5, -90f, 5f);
                    return decir(c, estadoBarquero(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(BARCA, barquero());
                    if (e != null) e.kill();
                    else {
                        Puestos.marcar(srv, BARCA, barquero(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "barquero");
                        var m = Morvain.nivel(srv);
                        if (m != null) LagunaNegra.liberar(m);
                    }
                    return decir(c, estadoBarquero(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, BARCA, barquero(), false);
                    // los pasajeros vuelven a flotar en cuanto salgan (y los faroles, de almas: los vuelve a poner la laguna al generarse)
                    Puestos.todos(srv, LAGUNA, false);
                    var m = Morvain.nivel(srv);
                    if (m != null) LagunaNegra.almas(m);
                    return decir(c, estadoBarquero(srv));
                }));
    }

    /** Como esta el Barquero (para el comando y las pruebas). */
    public static String estadoBarquero(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(BARCA, barquero());
        String s = "el Barquero: " + (LagunaNegra.enPaz(srv) ? "caido (la laguna esta en paz)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Barquero b) s += ", " + b.estado();
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(LagunaNegra.CX, LagunaNegra.PASO, LagunaNegra.CZ))) s += "; " + LagunaNegra.faroles(m);
        return s;
    }

    /** {@code /darkcraft molinera [ir | vencer | volver]}: como esta la Molinera, llevar ante la era, vencerla o que vuelva. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoMolinera() {
        return net.minecraft.commands.Commands.literal("molinera")
                .executes(c -> decir(c, estadoMolinera(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    var sp = c.getSource().getPlayerOrException();
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    int[] e = Molino.get().sitio("entrada");
                    sp.teleportTo(m, e[0] + 0.5, e[1] + 0.05, e[2] + 0.5, -90f, 8f);
                    return decir(c, estadoMolinera(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(MOLINO, molinera());
                    if (e != null) e.kill();
                    else {
                        Puestos.marcar(srv, MOLINO, molinera(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "molinera");
                    }
                    var m = Morvain.nivel(srv);
                    if (m != null) Molino.puerta(m, true, true);
                    return decir(c, estadoMolinera(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, MOLINO, molinera(), false);
                    var m = Morvain.nivel(srv);
                    if (m != null) Molino.puerta(m, false, false);
                    return decir(c, estadoMolinera(srv));
                }));
    }

    /** {@code /darkcraft verdugo [ir | vencer | volver]}: como esta el Verdugo, llevar ante el patibulo, vencerle o que vuelva. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoVerdugo() {
        return net.minecraft.commands.Commands.literal("verdugo")
                .executes(c -> decir(c, estadoVerdugo(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    var sp = c.getSource().getPlayerOrException();
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    // al claro, entre el arbol y el patibulo, mirando al patibulo
                    int[] e = Arboleda.get().sitio("ante_patibulo");
                    sp.teleportTo(m, e[0] + 0.5, e[1] + 0.05, e[2] + 0.5, 180f, -8f);
                    return decir(c, estadoVerdugo(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(PATIBULO, verdugo());
                    if (e != null) e.kill();
                    else {
                        Puestos.marcar(srv, PATIBULO, verdugo(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "verdugo");
                        var m = Morvain.nivel(srv);
                        if (m != null) Arboleda.liberar(m);
                    }
                    return decir(c, estadoVerdugo(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, PATIBULO, verdugo(), false);
                    // los colgados vuelven a su sitio (y a colgar) en cuanto salgan
                    Puestos.todos(srv, ARBOLEDA, false);
                    return decir(c, estadoVerdugo(srv));
                }));
    }

    /** Como esta el Verdugo (para el comando y las pruebas). */
    public static String estadoVerdugo(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(PATIBULO, verdugo());
        String s = "el Verdugo: " + (Arboleda.cuentaCerrada(srv) ? "caido (la cuenta esta cerrada)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Verdugo v) s += ", " + v.estado();
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Arboleda.PX, 80, Arboleda.PZ))) s += "; " + Arboleda.sogasColgados(m);
        return s;
    }

    /** {@code /darkcraft monasterio [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7k). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoMonasterio() {
        return net.minecraft.commands.Commands.literal("monasterio")
                .executes(c -> decir(c, estadoMonasterio(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irMonasterio(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Monasterio.get().sitios().keySet(), b))
                                .executes(c -> irMonasterio(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoMonasterio(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), MONASTERIO, false);
                            return decir(c, "vuelven todos los helados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), MONASTERIO, true);
                            return decir(c, "fuera todos los helados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, MONASTERIO).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irMonasterio(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Monasterio.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Monasterio.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando a la fuente del claustro (o, desde dentro de la iglesia, al altar)
        double tx = Monasterio.CX, tz = Monasterio.CZ;
        if (s[2] < Monasterio.CZ - 12) tz = Monasterio.CZ - 30;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos helados quedan y cuantos andan fuera (y como esta el monasterio, si esta cargado). */
    public static String estadoMonasterio(net.minecraft.server.MinecraftServer srv) {
        String s = "monasterio de la helada: quedan " + Puestos.quedan(srv, MONASTERIO) + " de " + MONASTERIO_P.size() + " (fuera "
                + Puestos.fuera(MONASTERIO) + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Monasterio.CX, Monasterio.PISO, Monasterio.CZ - 28))) s += "; " + Monasterio.resumen(m);
        return s;
    }

    /** {@code /darkcraft ermita [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7l): la escalera de los penitentes y la ermita. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoErmita() {
        return net.minecraft.commands.Commands.literal("ermita")
                .executes(c -> decir(c, estadoErmita(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irErmita(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Ermita.get().sitios().keySet(), b))
                                .executes(c -> irErmita(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoErmita(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ERMITA, false);
                            return decir(c, "vuelven todos los helados de la escalera");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ERMITA, true);
                            return decir(c, "fuera todos los helados de la escalera");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, ERMITA).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irErmita(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Ermita.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Ermita.get().sitios().keySet() + ")"));
            return 0;
        }
        // en la escalera, mirando cuesta arriba (al norte); en el calvario, al centro (y desde el centro, a la ermita)
        float giro = 180f;
        if (!Ermita.enEscalera(s[0] + 0.5, s[2] + 0.5) && Math.hypot(s[0] - Ermita.CX, s[2] - Ermita.CZ) > 3)
            giro = (float) (Math.toDegrees(Math.atan2(Ermita.CZ - s[2], Ermita.CX - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos helados de la escalera quedan y cuantos andan fuera (y como esta la ermita, si esta cargada). */
    public static String estadoErmita(net.minecraft.server.MinecraftServer srv) {
        String s = "escalera de la ermita: quedan " + Puestos.quedan(srv, ERMITA) + " de " + ERMITA_P.size() + " (fuera " + Puestos.fuera(ERMITA) + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Ermita.CX, Ermita.PISO, Ermita.CZ))) s += "; " + Ermita.resumen(m);
        return s;
    }

    /** {@code /darkcraft abad [ir | vencer | volver]} (parte 7l): como esta el Abad, llevar ante el, vencerle o que vuelva. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoAbad() {
        return net.minecraft.commands.Commands.literal("abad")
                .executes(c -> decir(c, estadoAbad(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    var sp = c.getSource().getPlayerOrException();
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    // a la entrada del calvario, mirando al abad (al norte)
                    int[] e = Ermita.get().sitio("entrada");
                    sp.teleportTo(m, e[0] + 0.5, e[1] + 0.05, e[2] + 0.5, 180f, 5f);
                    return decir(c, estadoAbad(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(CUMBRE, abad());
                    if (e != null) e.kill();
                    else {
                        Puestos.marcar(srv, CUMBRE, abad(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "abad");
                        var m = Morvain.nivel(srv);
                        if (m != null) Ermita.liberar(m);
                    }
                    return decir(c, estadoAbad(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, CUMBRE, abad(), false);
                    // los hermanos de la iglesia vuelven a rezar en cuanto salgan
                    Puestos.todos(srv, MONASTERIO, false);
                    return decir(c, estadoAbad(srv));
                }));
    }

    /** Como esta el Abad (para el comando y las pruebas). */
    public static String estadoAbad(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(CUMBRE, abad());
        String s = "el Abad: " + (Ermita.enPaz(srv) ? "caido (la cumbre esta en paz)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Abad a) s += ", " + a.estado();
        return s;
    }

    /** {@code /darkcraft capital [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7m): la Puerta Oeste de la Capital. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoCapital() {
        return net.minecraft.commands.Commands.literal("capital")
                .executes(c -> decir(c, estadoCapital(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irCapital(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Capital.get().sitios().keySet(), b))
                                .executes(c -> irCapital(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoCapital(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), CAPITAL, false);
                            return decir(c, "vuelven todos los calcinados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), CAPITAL, true);
                            return decir(c, "fuera todos los calcinados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, CAPITAL).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irCapital(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Capital.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Capital.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando hacia la plaza (al este); en la plaza y en el cuartel, al norte
        double tx = Capital.CX + Capital.PLAZA_U, tz = Capital.CZ;
        if (s[0] >= tx - 4) {
            tx = s[0];
            tz = s[2] - 10;
        }
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos calcinados quedan y cuantos andan fuera (y como esta la puerta, si esta cargada). */
    public static String estadoCapital(net.minecraft.server.MinecraftServer srv) {
        String s = "puerta oeste de la capital: quedan " + Puestos.quedan(srv, CAPITAL) + " de " + CAPITAL_P.size() + " (fuera "
                + Puestos.fuera(CAPITAL) + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Capital.CX, Capital.PISO, Capital.CZ))) s += "; " + Capital.resumen(m);
        return s;
    }

    /** {@code /darkcraft academia [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7o): la Academia Espectral. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoAcademia() {
        return net.minecraft.commands.Commands.literal("academia")
                .executes(c -> decir(c, estadoAcademia(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irAcademia(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Academia.get().sitios().keySet(), b))
                                .executes(c -> irAcademia(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoAcademia(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ACADEMIA, false);
                            return decir(c, "vuelven todos los eruditos");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ACADEMIA, true);
                            return decir(c, "fuera todos los eruditos");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, ACADEMIA).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irAcademia(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Academia.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Academia.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando hacia el norte (a la biblioteca); en la biblioteca, al estrado del grimorio
        double tx = s[0], tz = s[2] - 10;
        if (Academia.enBiblioteca(s[0], s[2])) {
            tx = Academia.CX;
            tz = Academia.CZ - 48;
            if (Math.hypot(tx - s[0], tz - s[2]) < 3) tz = s[2] + 10;
        }
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** {@code /darkcraft grieta [ir <sitio> | campana | enemigos [volver | fuera | sacar]]} (parte 7s): el Puente de la Grieta. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoGrieta() {
        return net.minecraft.commands.Commands.literal("grieta")
                .executes(c -> decir(c, estadoGrieta(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irGrieta(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Grieta.get().sitios().keySet(), b))
                                .executes(c -> irGrieta(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("campana").executes(c -> {
                    net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp && sp.level() == m)
                        return decir(c, "la campana llama: acuden " + Grieta.llamar(m, sp));
                    Grieta.tanir(m, 5.0f);
                    return decir(c, "tañe la campana");
                }))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoGrieta(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), GRIETA, false);
                            return decir(c, "vuelven todos los penitentes");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), GRIETA, true);
                            return decir(c, "fuera todos los penitentes");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, GRIETA).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irGrieta(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Grieta.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Grieta.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al medio del puente (desde el puente, hacia el norte)
        double tx = Grieta.CX + 0.5, tz = Grieta.CZ + 0.5;
        if (Math.hypot(tx - s[0], tz - s[2]) < 8) tz = Grieta.CZ - 30;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos penitentes quedan y cuantos andan fuera (y como esta la grieta, si esta cargada). */
    public static String estadoGrieta(net.minecraft.server.MinecraftServer srv) {
        String s = "puente de la grieta: quedan " + Puestos.quedan(srv, GRIETA) + " de " + GRIETA_P.size() + " (fuera " + Puestos.fuera(GRIETA)
                + "); la campana ha llamado " + Grieta.LLAMADAS + " veces y han acudido " + Grieta.ACUDEN;
        var m = Morvain.nivel(srv);
        int[] at = Grieta.get().sitio("atril");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(Grieta.CX, Grieta.PISO, Grieta.CZ)))
            s += "; " + Grieta.resumen(m);
        return s;
    }

    /** {@code /darkcraft cerco [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7u): el Cerco del Ancla. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoCerco() {
        return net.minecraft.commands.Commands.literal("cerco")
                .executes(c -> decir(c, estadoCerco(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irCerco(c.getSource(), "puerta"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Cerco.get().sitios().keySet(), b))
                                .executes(c -> irCerco(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoCerco(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), CERCO, false);
                            return decir(c, "vuelven todos los marcados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), CERCO, true);
                            return decir(c, "fuera todos los marcados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, CERCO).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irCerco(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Cerco.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Cerco.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al ancla (desde el ancla, hacia la puerta)
        double tx = Cerco.CX + 0.5, tz = Cerco.CZ + 0.5;
        if (Math.hypot(tx - s[0], tz - s[2]) < 6) tz = Cerco.CZ + 40;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos marcados quedan y cuantos andan fuera (y como esta el cerco, si esta cargado). */
    public static String estadoCerco(net.minecraft.server.MinecraftServer srv) {
        String s = "cerco del ancla: quedan " + Puestos.quedan(srv, CERCO) + " de " + CERCO_P.size() + " (fuera " + Puestos.fuera(CERCO) + ")";
        var m = Morvain.nivel(srv);
        int[] at = Cerco.get().sitio("atril");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(Cerco.CX, Cerco.PISO, Cerco.CZ)))
            s += "; " + Cerco.resumen(m);
        return s;
    }

    /** {@code /darkcraft puerto [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7q): el Puerto Varado. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoPuerto() {
        return net.minecraft.commands.Commands.literal("puerto")
                .executes(c -> decir(c, estadoPuerto(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irPuerto(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Varadero.get().sitios().keySet(), b))
                                .executes(c -> irPuerto(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoPuerto(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), PUERTO, false);
                            return decir(c, "vuelven todos los salobres");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), PUERTO, true);
                            return decir(c, "fuera todos los salobres");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, PUERTO).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irPuerto(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Varadero.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Varadero.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando hacia el faro (desde el faro, hacia el pueblo)
        double tx = Varadero.CX + Varadero.FARO_U, tz = Varadero.CZ + Varadero.FARO_V;
        if (Math.hypot(tx - s[0], tz - s[2]) < 12) {
            tx = Varadero.CX;
            tz = Varadero.CZ;
        }
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos salobres quedan y cuantos andan fuera (y como esta el puerto, si esta cargado). */
    public static String estadoPuerto(net.minecraft.server.MinecraftServer srv) {
        String s = "puerto varado: quedan " + Puestos.quedan(srv, PUERTO) + " de " + PUERTO_P.size() + " (fuera " + Puestos.fuera(PUERTO) + ")";
        var m = Morvain.nivel(srv);
        int[] at = Varadero.get().sitio("atril");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2]))) s += "; " + Varadero.resumen(m);
        return s;
    }

    /** Cuantos eruditos quedan y cuantos andan fuera (y como esta la academia, si esta cargada). */
    public static String estadoAcademia(net.minecraft.server.MinecraftServer srv) {
        String s = "academia espectral: quedan " + Puestos.quedan(srv, ACADEMIA) + " de " + ACADEMIA_P.size() + " (fuera "
                + Puestos.fuera(ACADEMIA) + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Academia.CX, Academia.PISO, Academia.CZ - 48))) s += "; " + Academia.resumen(m);
        return s;
    }

    /**
     * {@code /darkcraft rectora [ir | vencer | volver | observatorio <sitio> | sello [abrir]]} (parte 7p): como esta la Rectora,
     * llevar ante ella (a la puerta de la rotonda), vencerla, que vuelva (y la academia como estaba), ir a un sitio del
     * observatorio o abrir el sello a mano.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoRectora() {
        return net.minecraft.commands.Commands.literal("rectora")
                .executes(c -> decir(c, estadoRectora(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irObservatorio(c.getSource(), "puerta")))
                .then(net.minecraft.commands.Commands.literal("observatorio")
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Observatorio.get().sitios().keySet(), b))
                                .executes(c -> irObservatorio(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(OBSERVATORIO, rectora());
                    if (e instanceof com.darkcraft.enemigos.Rectora r) r.vencer();
                    else {
                        Puestos.marcar(srv, OBSERVATORIO, rectora(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "rectora");
                    }
                    return decir(c, estadoRectora(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, OBSERVATORIO, rectora(), false);
                    var m = Morvain.nivel(srv);
                    if (m != null) Observatorio.deshacerPaz(m);
                    // los que leian en la academia y a su puerta vuelven a su sitio en cuanto salgan
                    Puestos.todos(srv, ACADEMIA, false);
                    return decir(c, estadoRectora(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("sello")
                        .executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            return m == null ? 0 : decir(c, Observatorio.resumen(m));
                        })
                        .then(net.minecraft.commands.Commands.literal("abrir").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            Observatorio.abrir(m, true);
                            return decir(c, Observatorio.resumen(m));
                        })));
    }

    private static int irObservatorio(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Observatorio.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Observatorio.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al telescopio (al norte); en las salas de los lados, al centro de la rotonda
        double tx = Observatorio.CX, tz = Observatorio.CZ - 12;
        if (Math.abs(s[0] - Observatorio.CX) > 15) {
            tx = Observatorio.CX;
            tz = Observatorio.CZ;
        }
        if (Math.hypot(tx - s[0], tz - s[2]) < 3) tz = s[2] - 10;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return decir(src, estadoRectora(src.getServer()));
    }

    /** Como esta la Rectora (y su observatorio, si esta cargado): para el comando y las pruebas. */
    public static String estadoRectora(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(OBSERVATORIO, rectora());
        String s = "la Rectora: " + (Observatorio.enPaz(srv) ? "caida (el cielo ya no la lee)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Rectora r) s += ", " + r.estado();
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Observatorio.CX, Observatorio.PISO, Observatorio.CZ))) s += "; " + Observatorio.resumen(m);
        return s;
    }

    /**
     * {@code /darkcraft ballenero [ir | vencer | volver | costillar <sitio>]} (parte 7r): como esta Brann, llevar ante el (a la
     * entrada de la sala, entre las costillas de la cola), vencerle, que vuelva (y los salobres que faenaban, como estaban) o ir a
     * un sitio del costillar.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoBallenero() {
        return net.minecraft.commands.Commands.literal("ballenero")
                .executes(c -> decir(c, estadoBallenero(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irCostillar(c.getSource(), "entrada")))
                .then(net.minecraft.commands.Commands.literal("costillar")
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Costillar.get().sitios().keySet(), b))
                                .executes(c -> irCostillar(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(COSTILLAR, ballenero());
                    if (e instanceof com.darkcraft.enemigos.Ballenero b) b.vencer();
                    else {
                        Puestos.marcar(srv, COSTILLAR, ballenero(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "ballenero");
                    }
                    return decir(c, estadoBallenero(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, COSTILLAR, ballenero(), false);
                    // los salobres que faenaban en el puerto y en el costillar vuelven a su sitio en cuanto salgan
                    Puestos.todos(srv, PUERTO, false);
                    Puestos.todos(srv, COSTILLAR, false);
                    return decir(c, estadoBallenero(srv));
                }));
    }

    private static int irCostillar(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Costillar.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Costillar.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando a Brann (al centro de la sala); desde la sala, hacia la calavera
        double tx = Costillar.CX, tz = Costillar.CZ;
        if (Math.hypot(tx - s[0], tz - s[2]) < 8) tx = Costillar.CX + Costillar.CRANEO_U0;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return decir(src, estadoBallenero(src.getServer()));
    }

    /** Como esta Brann (y su costillar, si esta cargado): para el comando y las pruebas. */
    public static String estadoBallenero(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(COSTILLAR, ballenero());
        String s = "el Ballenero: " + (Costillar.enPaz(srv) ? "caido (vuelve la marea)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Ballenero b) s += ", " + b.estado();
        s += "; salobres del costillar: quedan " + Puestos.quedan(srv, COSTILLAR) + " de " + COSTILLAR_P.size() + " (fuera " + Puestos.fuera(COSTILLAR)
                + "), descansan " + Costillar.DESCANSAN;
        var m = Morvain.nivel(srv);
        int[] at = Costillar.get().sitio("atril");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(Costillar.CX, Costillar.PISO, Costillar.CZ)))
            s += "; " + Costillar.resumen(m);
        return s;
    }

    /**
     * {@code /darkcraft pontifice [ir | vencer | volver | altar <sitio>]} (parte 7t): como esta Anselmo, llevar ante el (a la puerta
     * de la basilica), vencerle, que vuelva (y los penitentes que rezaban, como estaban) o ir a un sitio del altar.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoPontifice() {
        return net.minecraft.commands.Commands.literal("pontifice")
                .executes(c -> decir(c, estadoPontifice(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irAltar(c.getSource(), "puerta")))
                .then(net.minecraft.commands.Commands.literal("altar")
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Altar.get().sitios().keySet(), b))
                                .executes(c -> irAltar(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(ALTAR, pontifice());
                    if (e instanceof com.darkcraft.enemigos.Pontifice b) b.vencer();
                    else {
                        Puestos.marcar(srv, ALTAR, pontifice(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "pontifice");
                    }
                    return decir(c, estadoPontifice(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, ALTAR, pontifice(), false);
                    // los penitentes que rezaban en el puente y en el altar vuelven a su sitio en cuanto salgan
                    Puestos.todos(srv, GRIETA, false);
                    Puestos.todos(srv, ALTAR, false);
                    return decir(c, estadoPontifice(srv));
                }));
    }

    private static int irAltar(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Altar.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Altar.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al altar; desde el presbiterio, hacia la puerta
        int[] al = Altar.get().sitio("altar");
        double tx = al[0], tz = al[2];
        if (Math.hypot(tx - s[0], tz - s[2]) < 6) tz = Altar.CZ + 30;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return decir(src, estadoPontifice(src.getServer()));
    }

    /** Como esta Anselmo (y su altar, si esta cargado): para el comando y las pruebas. */
    public static String estadoPontifice(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(ALTAR, pontifice());
        String s = "el Pontifice: " + (Altar.enPaz(srv) ? "caido (la Tierra sin Fe descansa)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Pontifice b) s += ", " + b.estado();
        s += "; penitentes del altar: quedan " + Puestos.quedan(srv, ALTAR) + " de " + ALTAR_P.size() + " (fuera " + Puestos.fuera(ALTAR)
                + "), descansan " + Altar.DESCANSAN;
        var m = Morvain.nivel(srv);
        int[] at = Altar.get().sitio("atril");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(Altar.CX, Altar.PISO, Altar.CZ)))
            s += "; " + Altar.resumen(m);
        return s;
    }

    /**
     * {@code /darkcraft aldric [ir | vencer | volver | puerta [abrir]]} (parte 7v): como esta Aldric, llevar ante el (al patio, al
     * pie de la escalera del ancla), vencerle, que vuelva (y los marcados del cerco como estaban) o abrir a mano la cadena de la
     * puerta.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoAldric() {
        return net.minecraft.commands.Commands.literal("aldric")
                .executes(c -> decir(c, estadoAldric(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    irCerco(c.getSource(), "patio");
                    return decir(c, estadoAldric(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(CERCO, aldric());
                    if (e instanceof com.darkcraft.enemigos.Aldric b) b.vencer();
                    else {
                        Puestos.marcar(srv, CERCO, aldric(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "aldric");
                    }
                    return decir(c, estadoAldric(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, CERCO, aldric(), false);
                    // los marcados que golpeaban el ancla vuelven a su sitio en cuanto salgan
                    Puestos.todos(srv, CERCO, false);
                    return decir(c, estadoAldric(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("puerta").executes(c -> {
                    var m = Morvain.nivel(c.getSource().getServer());
                    return decir(c, "la puerta del cerco: " + (m != null && Cerco.sellado(m) ? "cerrada por la cadena" : "abierta"));
                }).then(net.minecraft.commands.Commands.literal("abrir").executes(c -> {
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m != null) Cerco.abrir(m, true);
                    return decir(c, "la cadena de la puerta, rota");
                })));
    }

    /**
     * {@code /darkcraft teatro [ir <sitio> | enemigos [volver | fuera | sacar] | muro <puerta | trono> [abrir | cerrar]]} (parte 7y):
     * el Teatro Hundido: ir a uno de sus sitios, sus calcinados, y sus dos muros ilusorios.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoTeatro() {
        return net.minecraft.commands.Commands.literal("teatro")
                .executes(c -> decir(c, estadoTeatro(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irTeatro(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Teatro.get().sitios().keySet(), b))
                                .executes(c -> irTeatro(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoTeatro(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), TEATRO, false);
                            return decir(c, "vuelven todos los del teatro");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), TEATRO, true);
                            return decir(c, "fuera todos los del teatro");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, TEATRO).size();
                            return decir(c, "fuera " + n);
                        })))
                .then(net.minecraft.commands.Commands.literal("muro")
                        .then(net.minecraft.commands.Commands.argument("cual", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(List.of("puerta", "trono"), b))
                                .executes(c -> {
                                    var m = Morvain.nivel(c.getSource().getServer());
                                    return decir(c, m == null ? "Morvain no esta cargado" : MurosIlusorios.estado(m));
                                })
                                .then(net.minecraft.commands.Commands.literal("abrir").executes(c -> muroTeatro(c, true)))
                                .then(net.minecraft.commands.Commands.literal("cerrar").executes(c -> muroTeatro(c, false)))));
    }

    private static int muroTeatro(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, boolean abrir) {
        var m = Morvain.nivel(c.getSource().getServer());
        if (m == null) return 0;
        String cual = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "cual");
        String id = "trono".equals(cual) ? Teatro.MURO_TRONO : Teatro.MURO_PUERTA;
        MurosIlusorios.Muro w = MurosIlusorios.muro(id);
        if (abrir) MurosIlusorios.abrir(m, w, c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
        else MurosIlusorios.cerrar(m, id);
        return decir(c, MurosIlusorios.estado(m));
    }

    private static int irTeatro(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Teatro.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Teatro.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al escenario (al norte); en la sala, al trono
        float giro = 180f;
        if ("entrada".equals(sitio) || "summa".equals(sitio)) giro = 180f;
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos calcinados del teatro quedan, como estan sus muros ilusorios y lo que hay (si esta cargado). */
    public static String estadoTeatro(net.minecraft.server.MinecraftServer srv) {
        String s = "teatro hundido: quedan " + Puestos.quedan(srv, TEATRO) + " de " + TEATRO_P.size() + " (fuera " + Puestos.fuera(TEATRO) + ")";
        var m = Morvain.nivel(srv);
        int[] at = Teatro.get().sitio("atril");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(Teatro.CX, Teatro.SALA, Teatro.CZ + Teatro.SALA_V)))
            s += "; " + Teatro.resumen(m) + "; muros: " + MurosIlusorios.estado(m);
        return s;
    }

    /**
     * {@code /darkcraft nido [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7z): el Nido de Ceniza: ir a uno de sus sitios
     * y sus calcinados.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoNido() {
        return net.minecraft.commands.Commands.literal("nido")
                .executes(c -> decir(c, estadoNido(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irNido(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Nido.get().sitios().keySet(), b))
                                .executes(c -> irNido(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoNido(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), NIDO, false);
                            return decir(c, "vuelven todos los del nido");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), NIDO, true);
                            return decir(c, "fuera todos los del nido");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, NIDO).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irNido(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Nido.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Nido.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al centro de la repisa (desde el centro, al tesoro)
        double dx = Nido.CX + 0.5 - (s[0] + 0.5), dz = Nido.CZ + 0.5 - (s[2] + 0.5);
        if ("centro".equals(sitio)) {
            dx = Nido.TESORO_U;
            dz = Nido.TESORO_V;
        }
        float giro = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /**
     * {@code /darkcraft cadena [ir <sitio> | enemigos [volver | fuera | sacar] | rafaga | sombra olvidar | gargola [vuelo | picado |
     * garras | posarse | tierra]]} (parte 7aa): la subida por la cadena del sur: ir a uno de sus sitios, sus marcados y gargolas, una rafaga
     * de viento ya, que la Sombra vuelva a hablar y lo que hace la gargola mas cercana.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoCadena() {
        return net.minecraft.commands.Commands.literal("cadena")
                .executes(c -> decir(c, estadoCadena(c.getSource())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irCadena(c.getSource(), "inicio"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Subida.get().sitios().keySet(), b))
                                .executes(c -> irCadena(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoCadena(c.getSource())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), CADENA, false);
                            return decir(c, "vuelven todos los de la cadena");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), CADENA, true);
                            return decir(c, "fuera todos los de la cadena");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, CADENA).size();
                            return decir(c, "fuera " + n);
                        })))
                .then(net.minecraft.commands.Commands.literal("rafaga").executes(c -> {
                    var sp = c.getSource().getPlayerOrException();
                    Cadena.rafaga(sp.serverLevel(), sp, sp.getRandom().nextBoolean() ? 1 : -1);
                    return decir(c, "sopla una rafaga");
                }))
                .then(net.minecraft.commands.Commands.literal("sombra").then(net.minecraft.commands.Commands.literal("olvidar").executes(c -> {
                    Cadena.olvidar(c.getSource().getPlayerOrException());
                    return decir(c, "la Sombra volvera a hablar en la cadena");
                })))
                .then(net.minecraft.commands.Commands.literal("gargola")
                        .executes(c -> decir(c, gargola(c.getSource(), "")))
                        .then(net.minecraft.commands.Commands.literal("vuelo").executes(c -> decir(c, gargola(c.getSource(), "vuelo"))))
                        .then(net.minecraft.commands.Commands.literal("tierra").executes(c -> decir(c, gargola(c.getSource(), "tierra"))))
                        .then(net.minecraft.commands.Commands.literal("picado").executes(c -> decir(c, gargola(c.getSource(), "picado"))))
                        .then(net.minecraft.commands.Commands.literal("garras").executes(c -> decir(c, gargola(c.getSource(), "garras"))))
                        .then(net.minecraft.commands.Commands.literal("posarse").executes(c -> decir(c, gargola(c.getSource(), "posarse")))));
    }

    /** La gargola mas cercana (a 48 bloques): como esta, o que haga algo ya. */
    private static String gargola(net.minecraft.commands.CommandSourceStack src, String que) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var sp = src.getPlayerOrException();
        com.darkcraft.enemigos.Gargola g = null;
        double mejor = 48 * 48;
        for (var e : sp.serverLevel().getEntitiesOfClass(com.darkcraft.enemigos.Gargola.class, sp.getBoundingBox().inflate(48), x -> x.isAlive())) {
            double d = e.distanceToSqr(sp);
            if (d < mejor) {
                mejor = d;
                g = e;
            }
        }
        if (g == null) return "no hay ninguna gargola a 48 bloques";
        switch (que) {
            case "vuelo" -> g.probarVuelo(true);
            case "tierra" -> g.probarVuelo(false);
            case "picado" -> {
                g.setTarget(sp);
                g.probarPicado();
            }
            case "garras" -> {
                g.setTarget(sp);
                g.probarGarras();
            }
            case "posarse" -> {
                g.setTarget(sp);
                if (!g.probarPosarse()) return "la gargola no encuentra donde posarse";
            }
            default -> {
            }
        }
        return "gargola a " + Math.round(Math.sqrt(mejor)) + " bloques: " + g.estado();
    }

    private static int irCadena(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Subida.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Subida.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando cuesta arriba (al norte: a la ciudadela)
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, 180f, -8f);
        return 1;
    }

    /** Cuantos de la cadena quedan, lo que hay (si esta cargado) y como va el jugador. */
    public static String estadoCadena(net.minecraft.commands.CommandSourceStack src) {
        var srv = src.getServer();
        String s = "cadena del sur: quedan " + Puestos.quedan(srv, CADENA) + " de " + CADENA_P.size() + " (fuera " + Puestos.fuera(CADENA) + ")";
        var m = Morvain.nivel(srv);
        int[] at = Subida.get().sitio("atril"), bo = Subida.get().sitio("boca");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(bo[0], bo[1], bo[2])))
            s += "; " + Subida.resumen(m);
        if (src.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) s += "; " + Cadena.resumen(sp);
        return s;
    }

    /**
     * {@code /darkcraft homenaje [ir <sitio> | enemigos [volver | fuera | sacar] | rastrillo [abrir | cerrar] | sombra olvidar]}
     * (parte 7ab): la torre del homenaje de la ciudadela: ir a uno de sus sitios, los de la casa, el rastrillo de la puerta y que la
     * Sombra vuelva a hablar al entrar.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoHomenaje() {
        return net.minecraft.commands.Commands.literal("homenaje")
                .executes(c -> decir(c, estadoHomenaje(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irHomenaje(c.getSource(), "patio"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Homenaje.get().sitios().keySet(), b))
                                .executes(c -> irHomenaje(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoHomenaje(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), HOMENAJE, false);
                            return decir(c, "vuelven todos los de la torre del homenaje");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), HOMENAJE, true);
                            return decir(c, "fuera todos los de la torre del homenaje");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, HOMENAJE).size();
                            return decir(c, "fuera " + n);
                        })))
                .then(net.minecraft.commands.Commands.literal("rastrillo")
                        .then(net.minecraft.commands.Commands.literal("abrir").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            CasaSombra.abrirRastrilloYa(m);
                            return decir(c, estadoHomenaje(c.getSource().getServer()));
                        }))
                        .then(net.minecraft.commands.Commands.literal("cerrar").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            CasaSombra.cerrarRastrillo(m);
                            return decir(c, estadoHomenaje(c.getSource().getServer()));
                        })))
                .then(net.minecraft.commands.Commands.literal("sombra").then(net.minecraft.commands.Commands.literal("olvidar").executes(c -> {
                    CasaSombra.olvidar(c.getSource().getPlayerOrException());
                    return decir(c, "la Sombra volvera a hablar al entrar en la torre del homenaje");
                })))
                .then(comandoCentinela());
    }

    /** {@code centinela [despertar | tajo | golpe | humo | estocada | barrido | hachazo]}: el centinela mas cercano (a 32 bloques). */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoCentinela() {
        var raiz = net.minecraft.commands.Commands.literal("centinela").executes(c -> decir(c, centinela(c.getSource(), -1)));
        String[] nombres = {"despertar", "tajo", "golpe", "humo", "estocada", "barrido", "hachazo"};
        int[] acciones = {0, com.darkcraft.enemigos.CentinelaAnims.TAJO, com.darkcraft.enemigos.CentinelaAnims.GOLPE, -2,
                com.darkcraft.enemigos.CentinelaAnims.ESTOCADA, com.darkcraft.enemigos.CentinelaAnims.BARRIDO, com.darkcraft.enemigos.CentinelaAnims.HACHAZO};
        for (int k = 0; k < nombres.length; k++) {
            int a = acciones[k];
            raiz.then(net.minecraft.commands.Commands.literal(nombres[k]).executes(c -> decir(c, centinela(c.getSource(), a))));
        }
        return raiz;
    }

    /** El centinela mas cercano: como esta, o que haga esa accion ya (0 despertar, -2 el humo de su arma). */
    private static String centinela(net.minecraft.commands.CommandSourceStack src, int accion) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var sp = src.getPlayerOrException();
        com.darkcraft.enemigos.Centinela c = null;
        double mejor = 32 * 32;
        for (var e : sp.serverLevel().getEntitiesOfClass(com.darkcraft.enemigos.Centinela.class, sp.getBoundingBox().inflate(32), x -> x.isAlive())) {
            double d = e.distanceToSqr(sp);
            if (d < mejor) {
                mejor = d;
                c = e;
            }
        }
        if (c == null) return "no hay ningun centinela a 32 bloques";
        if (accion == 0) c.despertar();
        else if (accion != -1) {
            if (c.postura() != Enemigo.DE_PIE) c.postura(Enemigo.DE_PIE);
            c.setTarget(sp);
            c.probar(accion == -2 ? (c.variante() == com.darkcraft.enemigos.Centinela.ALABARDA ? com.darkcraft.enemigos.CentinelaAnims.HUMO_ALABARDA
                    : com.darkcraft.enemigos.CentinelaAnims.HUMO_ESPADON) : accion);
        }
        return String.format(java.util.Locale.ROOT, "centinela a %.0f bloques: %s, postura %d, accion %d, vida %.0f/%.0f", Math.sqrt(mejor), c.tipo(), c.postura(),
                c.accion(), c.getHealth(), c.getMaxHealth());
    }

    private static int irHomenaje(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Homenaje.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Homenaje.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al norte (la puerta del homenaje desde el patio; dentro, a lo largo de cada sala); ante el trono, a la reina
        float giro = sitio.equals("trono") || sitio.equals("sala_trono") || sitio.equals("tesoro") ? 0f : 180f;
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos de la torre del homenaje quedan y como esta (si esta cargada). */
    public static String estadoHomenaje(net.minecraft.server.MinecraftServer srv) {
        String s = "torre del homenaje: quedan " + Puestos.quedan(srv, HOMENAJE) + " de " + HOMENAJE_P.size() + " (fuera " + Puestos.fuera(HOMENAJE) + ")";
        var m = Morvain.nivel(srv);
        int[] at = Homenaje.get().sitio("atril"), tr = Homenaje.get().sitio("trono");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(tr[0], tr[1], tr[2])))
            s += "; " + Homenaje.resumen(m) + "; " + CasaSombra.resumen(m);
        return s;
    }

    /**
     * {@code /darkcraft isolde [ir | vencer | volver | media | garras | velo | abrazo | nana | desvanece | lagrimas | corte]} (parte 7ab):
     * como esta la reina, llevar ante ella (al pie del estrado), vencerla, que vuelva, pasarla a media vida y que haga ya lo que se le
     * diga.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoIsolde() {
        var raiz = net.minecraft.commands.Commands.literal("isolde")
                .executes(c -> decir(c, estadoIsolde(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    irHomenaje(c.getSource(), "sala_trono");
                    return decir(c, estadoIsolde(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(HOMENAJE, isolde());
                    if (e instanceof com.darkcraft.enemigos.Isolde i) i.vencer();
                    else {
                        Puestos.marcar(srv, HOMENAJE, isolde(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "isolde");
                    }
                    return decir(c, estadoIsolde(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, HOMENAJE, isolde(), false);
                    return decir(c, estadoIsolde(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("media").executes(c -> {
                    if (Puestos.enemigo(HOMENAJE, isolde()) instanceof com.darkcraft.enemigos.Isolde i) i.mediaVida();
                    return decir(c, estadoIsolde(c.getSource().getServer()));
                }));
        String[] nombres = {"garras", "velo", "abrazo", "nana", "desvanece", "lagrimas", "corte"};
        int[] acciones = {com.darkcraft.enemigos.IsoldeAnims.GARRAS, com.darkcraft.enemigos.IsoldeAnims.VELO, com.darkcraft.enemigos.IsoldeAnims.ABRAZO,
                com.darkcraft.enemigos.IsoldeAnims.NANA, com.darkcraft.enemigos.IsoldeAnims.DESVANECE, com.darkcraft.enemigos.IsoldeAnims.LAGRIMAS,
                com.darkcraft.enemigos.IsoldeAnims.GRITO};
        for (int k = 0; k < nombres.length; k++) {
            int a = acciones[k];
            raiz.then(net.minecraft.commands.Commands.literal(nombres[k]).executes(c -> {
                if (Puestos.enemigo(HOMENAJE, isolde()) instanceof com.darkcraft.enemigos.Isolde i) {
                    if (i.getTarget() == null && c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) i.setTarget(sp);
                    i.probar(a);
                }
                return decir(c, estadoIsolde(c.getSource().getServer()));
            }));
        }
        return raiz;
    }

    /**
     * {@code /darkcraft sombra [ir | vencer | volver | media | golpear | tomar | zarpazos | giro | cadena | manos | lanzas | niebla |
     * mirada | grito | caidos]} (parte 7ac): como esta la Sombra, llevar ante ella (a la entrada de su sala), que se rinda ya, que vuelva
     * (y se olvide todo: sin rendir ni final), pasarla a media vida, responder ya (golpear o tomar su corazon) y que haga ya lo que se le
     * diga.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoSombra() {
        var raiz = net.minecraft.commands.Commands.literal("sombra")
                .executes(c -> decir(c, estadoSombra(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    irHomenaje(c.getSource(), "aguja");
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    if (Aguja.sombra() != null) Aguja.sombra().vencer();
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m != null) Aguja.reiniciar(m);
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("media").executes(c -> {
                    if (Aguja.sombra() != null) Aguja.sombra().mediaVida();
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }));
        for (String r : new String[]{"golpear", "tomar"})
            raiz.then(net.minecraft.commands.Commands.literal(r).executes(c -> {
                if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp && sp.level() instanceof net.minecraft.server.level.ServerLevel m)
                    for (Corazon k : m.getEntitiesOfClass(Corazon.class, sp.getBoundingBox().inflate(40))) {
                        sp.teleportTo(k.getX(), sp.getY(), k.getZ() + 1.5);
                        Aguja.elegir(sp, k, "golpear".equals(r));
                        break;
                    }
                return decir(c, estadoSombra(c.getSource().getServer()));
            }));
        String[] nombres = {"zarpazos", "giro", "cadena", "manos", "lanzas", "niebla", "mirada", "grito", "caidos"};
        int[] acciones = {com.darkcraft.enemigos.SombraAnims.ZARPAZOS, com.darkcraft.enemigos.SombraAnims.GIRO, com.darkcraft.enemigos.SombraAnims.CADENA,
                com.darkcraft.enemigos.SombraAnims.MANOS, com.darkcraft.enemigos.SombraAnims.LANZAS, com.darkcraft.enemigos.SombraAnims.NIEBLA,
                com.darkcraft.enemigos.SombraAnims.MIRADA, com.darkcraft.enemigos.SombraAnims.GRITO, com.darkcraft.enemigos.SombraAnims.CAIDOS};
        for (int k = 0; k < nombres.length; k++) {
            int a = acciones[k];
            raiz.then(net.minecraft.commands.Commands.literal(nombres[k]).executes(c -> {
                com.darkcraft.enemigos.Sombra s = Aguja.sombra();
                if (s != null) {
                    if (s.getTarget() == null && c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) s.setTarget(sp);
                    s.probar(a);
                }
                return decir(c, estadoSombra(c.getSource().getServer()));
            }));
        }
        return raiz;
    }

    /** Como esta la Sombra y su sala: para el comando y las pruebas. */
    public static String estadoSombra(net.minecraft.server.MinecraftServer srv) {
        com.darkcraft.enemigos.Sombra e = Aguja.sombra();
        String s = "la Sombra: " + (Puestos.muerto(srv, AGUJA, Homenaje.SOMBRA) ? "caida" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e != null) s += ", " + e.estado();
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(net.minecraft.core.BlockPos.containing(Aguja.puerta()))) s += "; " + Aguja.resumen(m);
        return s;
    }

    /**
     * {@code /darkcraft aguja [abrir | cerrar | olvidar]} (parte 7ac): como esta la sala de la Sombra, abrir ya su puerta (de golpe),
     * volver a cerrarla y que la Sombra vuelva a decir sus frases de la aguja a quien lo pide.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoAguja() {
        return net.minecraft.commands.Commands.literal("aguja")
                .executes(c -> decir(c, estadoSombra(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    irHomenaje(c.getSource(), "puerta_sombra");
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("abrir").executes(c -> {
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m != null) Aguja.abrir(m, true);
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("cerrar").executes(c -> {
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m != null) Aguja.cerrar(m);
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("olvidar").executes(c -> {
                    if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) Aguja.olvidar(sp);
                    return decir(c, estadoSombra(c.getSource().getServer()));
                }));
    }

    /** Como esta la reina Isolde: para el comando y las pruebas. */
    public static String estadoIsolde(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(HOMENAJE, isolde());
        String s = "la reina Isolde: " + (Puestos.muerto(srv, HOMENAJE, isolde()) ? "caida" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Isolde i) s += ", " + i.estado();
        return s;
    }

    /** Cuantos calcinados del nido quedan y lo que hay (si esta cargado). */
    public static String estadoNido(net.minecraft.server.MinecraftServer srv) {
        String s = "nido de ceniza: quedan " + Puestos.quedan(srv, NIDO) + " de " + NIDO_P.size() + " (fuera " + Puestos.fuera(NIDO) + ")";
        var m = Morvain.nivel(srv);
        int[] at = Nido.get().sitio("atril"), te = Nido.get().sitio("arcon_tesoro");
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(at[0], at[1], at[2])) && m.isLoaded(new net.minecraft.core.BlockPos(te[0], te[1], te[2])))
            s += "; " + Nido.resumen(m);
        return s;
    }

    /**
     * {@code /darkcraft dragon [ir | vencer | volver | media | despegar | rasante | bolas | picado | ascuas | bajar | derribar]}
     * (parte 7z): como esta Vorthak, llevar ante el (al centro de la repisa), vencerle, que vuelva, y que haga ya lo que se le diga.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoDragon() {
        var raiz = net.minecraft.commands.Commands.literal("dragon")
                .executes(c -> decir(c, estadoDragon(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    irNido(c.getSource(), "centro");
                    return decir(c, estadoDragon(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(NIDO, dragon());
                    if (e instanceof com.darkcraft.enemigos.Dragon d) d.vencer();
                    else {
                        Puestos.marcar(srv, NIDO, dragon(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "dragon");
                    }
                    return decir(c, estadoDragon(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, NIDO, dragon(), false);
                    return decir(c, estadoDragon(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("media").executes(c -> {
                    if (Puestos.enemigo(NIDO, dragon()) instanceof com.darkcraft.enemigos.Dragon d) d.mediaVida();
                    return decir(c, estadoDragon(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("despegar").executes(c -> {
                    if (Puestos.enemigo(NIDO, dragon()) instanceof com.darkcraft.enemigos.Dragon d) d.despegar();
                    return decir(c, estadoDragon(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("derribar").executes(c -> {
                    if (Puestos.enemigo(NIDO, dragon()) instanceof com.darkcraft.enemigos.Dragon d) d.derribar();
                    return decir(c, estadoDragon(c.getSource().getServer()));
                }));
        for (String m : List.of("rasante", "bolas", "picado", "ascuas", "bajar"))
            raiz.then(net.minecraft.commands.Commands.literal(m).executes(c -> {
                if (Puestos.enemigo(NIDO, dragon()) instanceof com.darkcraft.enemigos.Dragon d) d.maniobra(m);
                return decir(c, estadoDragon(c.getSource().getServer()));
            }));
        return raiz;
    }

    /** Como esta Vorthak: para el comando y las pruebas. */
    public static String estadoDragon(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(NIDO, dragon());
        String s = "Vorthak: " + (Puestos.muerto(srv, NIDO, dragon()) ? "caido" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Dragon d) s += ", " + d.estado();
        return s;
    }

    /**
     * {@code /darkcraft bufon [ir | vencer | volver | partir]} (parte 7y): como esta Pell, llevar ante el (a la entrada de su sala,
     * con la puerta real abierta), vencerle, que vuelva o que se parta en tres ya.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoBufon() {
        return net.minecraft.commands.Commands.literal("bufon")
                .executes(c -> decir(c, estadoBufon(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m != null) MurosIlusorios.abrir(m, MurosIlusorios.muro(Teatro.MURO_PUERTA), null);
                    irTeatro(c.getSource(), "sala");
                    return decir(c, estadoBufon(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(TEATRO, bufon());
                    if (e instanceof com.darkcraft.enemigos.Bufon b) b.vencer();
                    else {
                        Puestos.marcar(srv, TEATRO, bufon(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "bufon");
                    }
                    return decir(c, estadoBufon(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, TEATRO, bufon(), false);
                    return decir(c, estadoBufon(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("partir").executes(c -> {
                    Enemigo e = Puestos.enemigo(TEATRO, bufon());
                    if (e instanceof com.darkcraft.enemigos.Bufon b) b.mediaVida();
                    return decir(c, estadoBufon(c.getSource().getServer()));
                }));
    }

    /** Como esta Pell: para el comando y las pruebas. */
    public static String estadoBufon(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(TEATRO, bufon());
        String s = "Pell: " + (Puestos.muerto(srv, TEATRO, bufon()) ? "caido" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Bufon b) s += ", " + b.estado();
        return s;
    }

    /** Como esta Aldric (y su cerco, si esta cargado): para el comando y las pruebas. */
    public static String estadoAldric(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(CERCO, aldric());
        String s = "Aldric: " + (Cerco.enPaz(srv) ? "caido (los marcados del cerco descansan y la cadena queda libre)" : e == null ? "esperando (no ha salido)"
                : "fuera");
        if (e instanceof com.darkcraft.enemigos.Aldric b) s += ", " + b.estado();
        var m = Morvain.nivel(srv);
        s += "; marcados del cerco: quedan " + Puestos.quedan(srv, CERCO) + " de " + CERCO_P.size() + " (fuera " + Puestos.fuera(CERCO) + "), descansan "
                + Cerco.DESCANSAN + "; la puerta " + (m != null && Cerco.sellado(m) ? "cerrada" : "abierta") + " (se cierra " + Cerco.SELLADOS
                + ", se abre " + Cerco.ABIERTOS + ")";
        return s;
    }

    /**
     * {@code /darkcraft condestable [ir | vencer | volver | patio <sitio> | rastrillo [bajar | subir]]} (parte 7n): como esta el
     * Condestable, llevar ante el (a la puerta del patio), vencerle, que vuelva (y la capital como estaba), ir a un sitio del patio
     * o mover el rastrillo a mano.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoCondestable() {
        return net.minecraft.commands.Commands.literal("condestable")
                .executes(c -> decir(c, estadoCondestable(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irJuramento(c.getSource(), "patio")))
                .then(net.minecraft.commands.Commands.literal("patio")
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Juramento.get().sitios().keySet(), b))
                                .executes(c -> irJuramento(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(JURAMENTO, condestable());
                    if (e instanceof com.darkcraft.enemigos.Condestable k) k.vencer();
                    else {
                        Puestos.marcar(srv, JURAMENTO, condestable(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "condestable");
                    }
                    return decir(c, estadoCondestable(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, JURAMENTO, condestable(), false);
                    var m = Morvain.nivel(srv);
                    if (m != null) Juramento.deshacerPaz(m);
                    // la guardia de la puerta oeste vuelve a su sitio en cuanto salga
                    Puestos.todos(srv, CAPITAL, false);
                    return decir(c, estadoCondestable(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("rastrillo")
                        .executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            return m == null ? 0 : decir(c, Juramento.resumen(m));
                        })
                        .then(net.minecraft.commands.Commands.literal("subir").executes(c -> {
                            var m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            Juramento.alzarYa(m);
                            return decir(c, Juramento.resumen(m));
                        })));
    }

    private static int irJuramento(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Juramento.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Juramento.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al estrado (al este); en la torre, al patio (al oeste)
        double tx = Juramento.CX + Juramento.CENTRO_U, tz = Juramento.CZ;
        if (s[0] > tx + 10) tx = s[0] - 20;
        float giro = (float) (Math.toDegrees(Math.atan2(tz - s[2], tx - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return decir(src, estadoCondestable(src.getServer()));
    }

    private static int decir(net.minecraft.commands.CommandSourceStack src, String s) {
        src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
        return 1;
    }

    /** Como esta el Condestable (y su patio, si esta cargado): para el comando y las pruebas. */
    public static String estadoCondestable(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(JURAMENTO, condestable());
        String s = "el Condestable: " + (Juramento.enPaz(srv) ? "caido (la capital ya no guarda sus puertas)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Condestable k) s += ", " + k.estado();
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Juramento.CX + Juramento.CENTRO_U, Juramento.PISO, Juramento.CZ)))
            s += "; " + Juramento.resumen(m);
        return s;
    }

    private static int decir(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, String s) {
        c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
        return 1;
    }

    /** Como esta la Molinera (para el comando y las pruebas). */
    public static String estadoMolinera(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(MOLINO, molinera());
        String s = "la Molinera: " + (Molino.vencida(srv) ? "caida" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Molinera mo)
            s += String.format(java.util.Locale.ROOT, ", vida %.0f/%.0f, postura %d, accion %d, pelea %s, segunda %s, suyos %d, en %.1f %.1f %.1f",
                    mo.getHealth(), mo.getMaxHealth(), mo.postura(), mo.accion(), mo.enPelea(), mo.segunda(), mo.suyos(), mo.getX(), mo.getY(), mo.getZ());
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Molino.MX, Molino.Y, Molino.MZ))) s += "; " + Molino.resumen(m);
        return s;
    }

    /** Para la prueba: que ningun puesto quede cerca del arco ni de un santuario. */
    public static String comprobar() {
        preparar();
        int cerca = 0;
        List<Puesto> todos = new ArrayList<>(VELMOR_P);
        todos.addAll(LLANURAS_P);
        todos.addAll(MOLINO_P);
        todos.addAll(ARBOLEDA_P);
        todos.addAll(PATIBULO_P);
        todos.addAll(PILOTES_P);
        todos.addAll(LAGUNA_P);
        todos.addAll(BARCA_P);
        todos.addAll(MONASTERIO_P);
        todos.addAll(ERMITA_P);
        todos.addAll(CUMBRE_P);
        todos.addAll(CAPITAL_P);
        todos.addAll(JURAMENTO_P);
        todos.addAll(ACADEMIA_P);
        todos.addAll(OBSERVATORIO_P);
        todos.addAll(PUERTO_P);
        todos.addAll(COSTILLAR_P);
        todos.addAll(GRIETA_P);
        todos.addAll(ALTAR_P);
        todos.addAll(CERCO_P);
        for (Puesto p : todos) {
            if (Math.hypot(p.x() - Llegada.X, p.z() - Llegada.Z) < 18) cerca++;
            for (SantuariosMorvain.Sitio s : SantuariosMorvain.todos()) if (Math.hypot(p.x() - s.x(), p.z() - s.z()) < 14) cerca++;
        }
        int camp = 0;
        for (Puesto p : VELMOR_P) if (p.variante() == C) camp++;
        int colg = 0;
        for (Puesto p : ARBOLEDA_P) if (p.postura() == Enemigo.COLGADO) colg++;
        return VELMOR_P.size() + " en Velmor (" + camp + " campaneros), " + LLANURAS_P.size() + " por las llanuras, " + MOLINO_P.size()
                + " en el molino, " + ARBOLEDA_P.size() + " en la arboleda (" + colg + " colgados) y " + PATIBULO_P.size()
                + " en el patibulo, " + PILOTES_P.size() + " en la aldea de los pilotes, " + LAGUNA_P.size() + " en la laguna negra y "
                + BARCA_P.size() + " en la barca, " + MONASTERIO_P.size() + " en el monasterio, " + ERMITA_P.size() + " en la escalera de la ermita y "
                + CUMBRE_P.size() + " en la cumbre, " + CAPITAL_P.size() + " en la puerta oeste de la capital, " + JURAMENTO_P.size()
                + " en el patio del Juramento, " + ACADEMIA_P.size() + " en la academia espectral, " + OBSERVATORIO_P.size()
                + " en el observatorio, " + PUERTO_P.size() + " en el puerto varado, " + COSTILLAR_P.size()
                + " en el costillar, " + GRIETA_P.size() + " en el puente de la grieta, " + ALTAR_P.size()
                + " en el altar profanado, " + CERCO_P.size() + " en el cerco del ancla; cerca del arco o de un santuario " + cerca;
    }
}
