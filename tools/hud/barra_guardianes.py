"""Barras de vida de los dos guardianes de Valdrenn (parte 4d), de la Molinera de Velmor (parte 7f) y del Verdugo de la
Arboleda (parte 7h), pixel a pixel, con
la misma disposicion que la del Capitan (la dibuja client/enemigos/GuardianCliente):

  Ser Brandt, el Muro del Rey: un marco de hierro negro remachado con filos rojos, el paves con la corona del Rey en el
  centro y dos puas en los extremos; relleno de sangre oscura que corre; chispas de hierro al recibir dano.
  Vareth, el Guardian de Corteza: un marco de corteza con musgo y raices que cuelgan, el corazon verde latiendo en el
  centro entre dos ramas; relleno de savia verde que corre; hojas que caen al recibir dano.
  La Molinera de Velmor: una viga de madera vieja del molino con flejes de hierro y espigas secas por encima, un aspa rota
  en cada punta y la piedra de moler en el centro; relleno de brasas que corren (las de sus grietas); ceniza y harina que
  caen al recibir dano.

  python3 tools/hud/barra_guardianes.py [carpeta_de_vistas]

  El Verdugo de la Arboleda (parte 7h): la viga de la horca de roble oscuro con vueltas de soga, los postes con sus
  tornapuntas en las puntas y, en el centro, el lazo que cuelga con la capucha del verdugo y sus ojos encendidos dentro;
  relleno de una soga empapada de sangre que gira; hebras de canamo y hojas secas que caen al recibir dano.

  El Barquero de la Laguna Negra (parte 7j): el remo tumbado (la pertiga mojada con anillas de hierro, la pala en la punta
  derecha y el garfio en la izquierda), cadenitas con monedas viejas colgando y la linterna de almas en el centro; relleno
  de agua negra con ondas y destellos de almas; gotas, una moneda y chispas de almas que caen al recibir dano.

  Hrodric, el Abad de la Cumbre (parte 7l): el baculo tumbado (la vara de plata escarchada con nudos de oro, el regaton y la
  voluta de hielo con su cristal), carambanos colgando y en el centro la mitra de hielo con su banda de oro; relleno de hielo
  azul que corre; copos y esquirlas que caen al recibir dano.

  El Condestable del Juramento (parte 7n): el montante tumbado (la hoja negra con los filos al rojo, la cruz de oro, el puno
  y el pomo a la izquierda, la punta a la derecha), dos manos alzadas de oro y en el centro el gran yelmo cerrado con la
  rendija encendida y la cimera en ascuas; relleno de fuego que corre; brasas y chispas que suben al recibir dano.

  Ysolde, la Rectora de la Academia (parte 7p): el baculo de plata tumbado con anillos de oro y, a la derecha, la estrella de
  cristal entre sus dos puntas; estrellas sueltas por encima y en el centro la aureola de estrellas (el aro de oro con sus
  doce puntos de luz y la estrella grande); relleno de un cielo de noche que corre; estrellas que caen titilando.

  Brann, el Ballenero (parte 7r): el arpon tumbado (el asta de fresno con vueltas de cuerda, la cana y la punta de hierro
  con sus dos barbas a la derecha y el cabo atado que se enrolla a la izquierda), costras de sal y percebes, y en el centro
  la cola de la Vieja que sale del agua (la aleta ancha, palida, con su muesca y el agua que le chorrea); relleno de mar
  verde oscuro con olas de espuma; gotas de agua y cristales de sal que caen al recibir dano.

  Anselmo, el Pontifice Sangrante (parte 7t): la ferula tumbada (la vara de oro con sangre seca, la cruz de tres travesanos a
  la derecha y el regaton a la izquierda; gotas de sangre que cuelgan) y en el centro la tiara de tres coronas, con la sangre
  que le chorrea; relleno de sangre espesa que corre; gotas de sangre y chispas de oro que caen al recibir dano.

  Aldric, el Primer Marcado (parte 7v): la cadena de hierro negro de punta a punta (eslabones tumbados y de canto), el gancho
  a la izquierda y el eslabon roto que cuelga a la derecha, y en el centro la marca (el ojo en el aro roto con sus tres
  espinas) al rojo; relleno de hierro al rojo que corre; esquirlas de hierro y brasas que caen al recibir dano.

  Los gigantes errantes de Morvain (parte 7w; los tres con la misma): el dintel de sillares viejos con grietas y musgo, el
  munon de una columna a la izquierda y la punta partida a la derecha, y en el centro la cabeza de piedra de un gigante con
  los ojos cerrados (una rendija de luz); relleno de tierra que corre; lascas de piedra y polvo que caen al recibir dano.

  Ser Lucan, el Jinete Nocturno (parte 7x): la guja tumbada (el asta de fresno negro con anillas de hierro y el regaton a la
  izquierda; a la derecha el cubo y la hoja, con el espolon, la punta que se alza y el filo encendido de almas), en el centro el
  yelmo cerrado del heraldo con la corona del Rey y la rendija azul, y debajo la banderola roja con la corona de oro; relleno
  de fuego de almas que corre; chispas de almas y esquirlas de hierro que caen al recibir dano.

  Pell, el Bufon del Rey (parte 7y): una banda de arlequin (rombos rojos y negros entre dos filetes de oro) con un cascabel en
  cada punta; en el centro su mascara (mitad blanca, mitad negra, los ojos encendidos y la sonrisa) bajo el gorro de tres puntas
  con cascabeles, y debajo dos dagas cruzadas; relleno de rombos rojos que corren; confeti que cae al recibir dano.

  Vorthak, el Dragon de Ceniza (parte 7z): su lomo de punta a punta (escamas de carbon con grietas de brasa y la fila de puas de
  hueso por encima), la hoja de la punta de la cola a la izquierda y una garra a la derecha; en el centro su cabeza de frente, con
  los cuernos hacia atras, las cejas de hueso, los ojos de oro fundido y brasas en los ollares; relleno de brasas que corren entre
  escamas negras; ascuas y ceniza que suben al recibir dano.

  La reina Isolde (parte 7ab): su velo de encaje negro tendido de punta a punta (seda negra con el encaje en rombos y florecitas,
  el ribete de plata arriba y las ondas del encaje colgando abajo), una rosa negra marchita en cada punta y, en el centro, la cara
  palida tras el velo con dos ojos rojos que apenas se ven, la corona de oro viejo con sus gemas encima y dos lagrimas negras que le
  caen por debajo; relleno de seda carmesi con pliegues que corren y un hilo de plata; petalos negros y rojos, lagrimas y chispas
  de plata que caen meciendose al recibir dano.

  Sigrun, la Guardiana del Paso (parte 7ap): su espada larga tumbada de punta a punta (la hoja escarchada con nieve encima y
  carambanos, la empunadura de cuero con la guarda a la izquierda, la punta a la derecha) y en el centro su escudo de lagrima azul con
  el colmillo blanco; relleno de ventisca sobre el azul de noche; copos y esquirlas que caen al recibir dano.

  Hakon, el Capataz (parte 7ar): su pico tumbado de punta a punta (el mango con aros de hierro, el puno de cuero y el regaton a la
  izquierda, la cabeza del pico doble de hierro negro con la veta roja a la derecha) con cristales de la veta que le crecen y, en el
  centro, su casco de hierro con el candil encendido; relleno de la veta roja que corre por la roca negra; piedrecitas y esquirlas de
  cristal rojo que caen al recibir dano.

  Fray Osmund, el Sacristan (parte 7at): la cadena de su incensario tendida de punta a punta (eslabones de bronce de frente y de
  canto), el hueso con su anilla por donde se coge a la izquierda y el incensario redondo con sus brasas a la derecha; en el centro
  su aureola de huesos con cinco calaveras y cuatro velas y, dentro, su calavera bajo la capucha con las almas en las cuencas;
  relleno de polvo de hueso que corre con almas azules; esquirlas de hueso, una calaverita y chispas de almas al recibir dano.

  La Sombra sin Nombre (parte 7ac): sus cadenas de hierro negro de punta a punta, rotas por tres sitios (las puntas al rojo), y en
  el centro la capucha sin cara (la oscuridad y dos ojos rojos) con la corona rota de Corvin flotando encima; relleno de oscuridad
  con venas rojas que laten; jirones de sombra y chispas rojas que suben al recibir dano.

Salen textures/gui/brandt_barra.png, corteza_barra.png, molinera_barra.png, verdugo_barra.png, barquero_barra.png,
abad_barra.png, condestable_barra.png, rectora_barra.png, ballenero_barra.png, pontifice_barra.png, aldric_barra.png,
gigante_barra.png, jinete_barra.png, bufon_barra.png, dragon_barra.png, isolde_barra.png, sombra_barra.png, sigrun_barra.png,
capataz_barra.png y osmund_barra.png
(256x128):
  v 0..39   marco (el relleno va en x 8..247, y 19..25)
  v 48..79  relleno, 4 cuadros de 8 px (240 x 7)
  v 80      hueco (240 x 7)        v 88  rastro del ultimo dano (240 x 7)
  v 104     lo que cae: 4 de 8 x 8 (u 0, 8, 16, 24)
"""
import math
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from barras import FW, FX0, FX1, FY0, FY1, OUT, GUI, gem, hexc, mix, noise2, put, rect  # noqa: E402

# ---------------------------------------------------------------- Ser Brandt

H_DD, H_D, H, H_L = hexc("0d0e10"), hexc("1d1f23"), hexc("34373d"), hexc("5d626b")
R_DD, R_D, R, R_L, R_H = hexc("2a0505"), hexc("5c0d0e"), hexc("8e1b1d"), hexc("c93a33"), hexc("ffb4a0")


def marco_brandt():
    n = noise2(71)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.2, y * 0.6)
            put(img, x, y, H_D if v < 0.4 else H if v < 0.85 else H_L)
    # filos rojos arriba y abajo
    for x in range(x0 + 1, x1):
        put(img, x, y0 + 1, R_D if x % 2 else R)
        put(img, x, y1 - 1, R_DD if x % 2 else R_D)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, H_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # remaches
    for x in range(x0 + 7, x1 - 4, 12):
        if abs(x - FW // 2) > 16:
            put(img, x, y0 + 2, H_L)
            put(img, x, y1 - 2, H_L)
    # puas en los extremos
    for side in (0, 1):
        s = -1 if side == 0 else 1
        bx = x0 - 1 if side == 0 else x1 + 1
        cy = (FY0 + FY1) // 2
        for i in range(7):
            for dy in range(-(6 - i) // 2, (6 - i) // 2 + 1):
                put(img, bx + s * i, cy + dy, OUT)
        for i in range(6):
            for dy in range(-(4 - i) // 2, (4 - i) // 2 + 1):
                put(img, bx + s * i, cy + dy, H_L if dy < 0 else H)
    # en el centro: el paves con la corona (acaba en punta justo encima del relleno)
    cx, top = FW // 2, 0
    for y in range(top, top + 18):
        w = 7 if y < top + 11 else 7 - (y - top - 10)
        if w < 0:
            continue
        for x in range(cx - w - 1, cx + w + 2):
            put(img, x, y, OUT if abs(x - cx) > w or y == top else (H_L if abs(x - cx) == w else H))
    corona = ["R.R.R", "RRRRR", "RRRRR", ".RRR."]
    for j, fila in enumerate(corona):
        for i, ch in enumerate(fila):
            if ch == "R":
                put(img, cx - 2 + i, top + 3 + j, R_L if j == 0 else R)
    for x in range(cx - 3, cx + 4):
        put(img, x, top + 9, R_D)
    return img


def relleno_brandt(k):
    n = noise2(83)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 6) * 0.08, y * 0.5)
            t = 0.2 + 0.55 * mid + 0.45 * (v - 0.5)
            c = mix(R_D, R, max(0.0, min(1.0, t * 1.4)))
            if t > 0.7:
                c = mix(c, R_L, min(1.0, (t - 0.7) * 3))
            if y == 0:
                c = mix(c, R_L, 0.4)
            if y == 6:
                c = mix(c, R_DD, 0.6)
            img.putpixel((x, y), c)
    return img


def chispas():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["..h", ".L.", "m.."], ["h..", ".L.", "..m"], [".h.", "hLh", ".m."], ["L", "m"])
    col = {"h": R_H, "L": R_L, "m": R}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Vareth

C_DD, C_D, C, C_L = hexc("0e0a07"), hexc("201810"), hexc("3a2b1c"), hexc("5a442c")
M_D, M, M_L = hexc("1f3214"), hexc("355a22"), hexc("5e8f3b")
S_DD, S_D, S, S_L, S_H = hexc("0b2a14"), hexc("1f7a3c"), hexc("3fc265"), hexc("8cf0a8"), hexc("e6fff0")


def marco_corteza():
    rnd = random.Random(29)
    n = noise2(97)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.35, y * 0.12)
            c = C_D if v < 0.35 else C if v < 0.8 else C_L
            if x % 7 == 0:
                c = C_DD                                      # las vetas
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, C_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # musgo por arriba
    for x in range(x0 + 2, x1 - 1):
        if n(x * 0.3, 7.7) > 0.45:
            put(img, x, y0, M if x % 3 else M_L)
            if n(x * 0.5, 3.1) > 0.7:
                put(img, x, y0 - 1, M_D)
    # raices que cuelgan por debajo
    for _ in range(14):
        xx = rnd.randint(x0 + 6, x1 - 6)
        if abs(xx - FW // 2) < 14:
            continue
        largo = rnd.randint(2, 7)
        for i in range(largo):
            put(img, xx + (1 if (i // 2) % 2 else 0), y1 + 1 + i, C if i < largo - 1 else C_DD)
    # en los extremos, dos nudos con un brote
    for side in (0, 1):
        cx = x0 + 1 if side == 0 else x1 - 1
        cy = (FY0 + FY1) // 2
        for dy in range(-5, 6):
            for dx in range(-4, 5):
                if math.hypot(dx, dy * 0.8) <= 4.3:
                    put(img, cx + dx, cy + dy, OUT if math.hypot(dx, dy * 0.8) > 3.4 else (C_L if dx + dy < 0 else C))
        gem(img, cx, cy, 1, S_DD, S, S_L)
    # en el centro: el corazon entre dos ramas
    cx, cy = FW // 2, 12
    for s in (-1, 1):
        for i in range(12):
            x = cx + s * (6 + i)
            y = cy - 2 - int(i * 0.45)
            put(img, x, y, C_L)
            put(img, x, y + 1, C)
            put(img, x, y - 1, OUT)
            put(img, x, y + 2, OUT)
        put(img, cx + s * 17, cy - 9, M_L)
        put(img, cx + s * 16, cy - 8, M)
    for dy in range(-9, 10):
        for dx in range(-8, 9):
            d = math.hypot(dx, dy * 1.1)
            if d <= 8.2:
                put(img, cx + dx, cy + dy, OUT if d > 7.0 else (C_L if dx + dy < -2 else C))
    for dy in range(-6, 7):
        for dx in range(-5, 6):
            d = math.hypot(dx, dy * 1.05)
            if d <= 5.2:
                c = S_H if d < 1.3 else S_L if d < 2.6 else S if d < 4.0 else S_D
                put(img, cx + dx, cy + dy, c)
    return img


def relleno_corteza(k):
    n = noise2(101)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 5) * 0.1, y * 0.6)
            t = 0.2 + 0.55 * mid + 0.45 * (v - 0.5)
            c = mix(S_D, S, max(0.0, min(1.0, t * 1.4)))
            if t > 0.68:
                c = mix(c, S_L, min(1.0, (t - 0.68) * 3))
            if y == 0:
                c = mix(c, S_L, 0.4)
            if y == 6:
                c = mix(c, S_DD, 0.6)
            if (x * 3 + 7 * y - k * 17) % 83 == 0 and 1 <= y <= 5:
                c = S_H
            img.putpixel((x, y), c)
    return img


def hojas():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["..L", ".Lm", "Lm.", "m.."], [".L.", "LmL", ".m."], ["LL.", ".mm"], ["L.", "Lm", ".m"])
    col = {"L": M_L, "m": M}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- la Molinera de Velmor

W_DD, W_D, W, W_L = hexc("120c08"), hexc("2a1d13"), hexc("45321f"), hexc("66503a")
F_D, F, F_L = hexc("26262b"), hexc("44444c"), hexc("6c6c76")
T_D, T, T_L = hexc("5e5136"), hexc("8a7a52"), hexc("b9a877")
P_DD, P_D, P, P_L = hexc("1e1d1c"), hexc("4a4846"), hexc("6f6c68"), hexc("98948e")
B_DD, B_D, B, B_L, B_H = hexc("3a0c04"), hexc("7a2008"), hexc("c0401a"), hexc("f07a2a"), hexc("ffc070")
H_ = hexc("dcd7c8")


def marco_molinera():
    rnd = random.Random(41)
    n = noise2(59)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la viga: vetas a lo largo, nudos y grietas
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.08, y * 0.9)
            c = W_D if v < 0.38 else W if v < 0.82 else W_L
            if (x * 7 + y * 3) % 53 == 0:
                c = W_DD
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, W_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # flejes de hierro con sus clavos
    for x in range(x0 + 14, x1 - 10, 26):
        if abs(x - FW // 2) < 18:
            continue
        for y in range(y0 + 1, y1):
            if FY0 - 1 <= y <= FY1:
                continue
            put(img, x, y, F)
            put(img, x + 1, y, F_D)
        put(img, x, y0 + 2, F_L)
        put(img, x, y1 - 2, F_L)
    # espigas secas que asoman por arriba (y alguna caida)
    for _ in range(26):
        xx = rnd.randint(x0 + 4, x1 - 4)
        if abs(xx - FW // 2) < 15:
            continue
        alto = rnd.randint(3, 7)
        inclina = rnd.choice((-1, 0, 0, 1))
        for i in range(alto):
            px = xx + (inclina if i > alto // 2 else 0)
            put(img, px, y0 - 1 - i, T_D if i < alto - 3 else T)
        cab = y0 - alto
        put(img, xx + inclina, cab, T_L)
        put(img, xx + inclina - 1, cab + 1, T)
        put(img, xx + inclina + 1, cab + 1, T)
        put(img, xx + inclina, cab - 1, T_L)
    # en las puntas: el extremo de un aspa (el armazon de listones), de pie sobre la viga
    for side in (0, 1):
        bx = 1 if side == 0 else FW - 5
        for y in range(y0 - 7, y1 + 6):
            for dx in range(4):
                borde = dx in (0, 3)
                liston = (y - y0) % 3 == 0
                if borde or liston:
                    put(img, bx + dx, y, W_L if borde and dx == 0 else W)
                elif y < y0 or y > y1:
                    put(img, bx + dx, y, (0, 0, 0, 0))
            put(img, bx - 1, y, OUT)
            put(img, bx + 4, y, OUT)
        for dx in range(-1, 5):
            put(img, bx + dx, y0 - 8, OUT)
            put(img, bx + dx, y1 + 6, OUT)
    # en el centro: la piedra de moler, con su ojo y sus surcos (encima de la viga, sin tapar el relleno)
    cx, cy = FW // 2, 8
    for dy in range(-10, 11):
        for dx in range(-10, 11):
            d = math.hypot(dx, dy)
            if d > 9.9 or cy + dy >= FY0 - 1:
                continue
            if d > 8.9 or cy + dy == FY0 - 2:
                put(img, cx + dx, cy + dy, OUT)
                continue
            a = math.atan2(dy, dx)
            surco = 3.6 < d < 8.2 and abs(((a + math.pi) * 10 / (2 * math.pi)) % 1 - 0.5) < 0.1
            c = P_L if dx + dy < -6 else P if dx + dy < 6 else P_D
            if surco:
                c = P_DD
            if n(dx * 0.4 + 30, dy * 0.4) > 0.8:
                c = mix(c, P_DD, 0.5)
            put(img, cx + dx, cy + dy, c)
    # el ojo de la piedra: un hueco con brasas dentro
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            if abs(dx) == 3 and abs(dy) == 3:
                continue
            put(img, cx + dx, cy + dy, OUT if max(abs(dx), abs(dy)) == 3 else (B_H if dx == 0 and dy == 0 else B_L if abs(dx) + abs(dy) < 2 else B))
    # harina en el borde de abajo de la piedra
    for dx in range(-7, 8):
        if rnd.random() < 0.6:
            put(img, cx + dx, FY0 - 3, H_)
    return img


def relleno_molinera(k):
    n = noise2(127)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 6) * 0.09, y * 0.55)
            t = 0.2 + 0.55 * mid + 0.45 * (v - 0.5)
            c = mix(B_D, B, max(0.0, min(1.0, t * 1.4)))
            if t > 0.66:
                c = mix(c, B_L, min(1.0, (t - 0.66) * 3))
            if y == 0:
                c = mix(c, B_L, 0.35)
            if y == 6:
                c = mix(c, B_DD, 0.6)
            # grietas de ceniza que corren por las brasas
            if (x * 5 + y * 11 - k * 23) % 97 == 0:
                c = P_D
            elif (x * 3 + 7 * y - k * 19) % 89 == 0 and 1 <= y <= 5:
                c = B_H
            img.putpixel((x, y), c)
    return img


def ceniza():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["..h", ".c.", "c.."], ["h.", "cc"], [".h.", "hcH", ".c."], ["H.", ".c", "c."])
    col = {"h": H_, "c": P_L, "H": B_L}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- el Verdugo de la Arboleda (parte 7h)

O_DD, O_D, O, O_L = hexc("150e09"), hexc("2c1f15"), hexc("45311f"), hexc("624a31")
S_DD, S_D, S, S_L = hexc("3e3020"), hexc("56442c"), hexc("876e46"), hexc("b39a6a")
C_DD, C_D, C_L = hexc("070606"), hexc("141212"), hexc("2a2624")
J_D, J, J_L, J_H = hexc("5a1806"), hexc("a03a0c"), hexc("e0701a"), hexc("ffd070")
X_DD, X_D, X, X_L = hexc("1e0404"), hexc("420a08"), hexc("6e1610"), hexc("9c2a1c")


def marco_verdugo():
    """La viga de la horca de roble oscuro, con vueltas de soga; en las puntas los postes con sus tornapuntas; en el centro
    el lazo que cuelga de la viga, y dentro la capucha del verdugo con sus ojos encendidos."""
    rnd = random.Random(73)
    n = noise2(61)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la viga: vetas a lo largo, nudos y alguna mancha oscura
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.07, y * 0.8)
            c = O_D if v < 0.36 else O if v < 0.8 else O_L
            if (x * 5 + y * 9) % 61 == 0:
                c = O_DD
            if n(x * 0.3 + 40, y * 0.3) > 0.86:
                c = mix(c, X_D, 0.55)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, O_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # vueltas de soga alrededor de la viga (en diagonal, como se enrollan)
    for x in range(x0 + 18, x1 - 14, 30):
        if abs(x - FW // 2) < 20:
            continue
        for y in range(y0 + 1, y1):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(3):
                put(img, x + k + (y % 2), y, S_L if k == 0 else S if k == 1 else S_D)
        put(img, x - 1, y0, S_D)
        put(img, x + 3, y1, S_D)
    # en las puntas: los postes de la horca (de arriba abajo) con la tornapunta hacia el centro
    for side in (0, 1):
        bx = 1 if side == 0 else FW - 6
        for y in range(y0 - 6, y1 + 7):
            for dx in range(5):
                c = O_L if dx == 1 else O if dx < 4 else O_D
                if (y * 3 + dx) % 13 == 0:
                    c = O_DD
                put(img, bx + dx, y, c)
            put(img, bx - 1, y, OUT)
            put(img, bx + 5, y, OUT)
        for dx in range(-1, 6):
            put(img, bx + dx, y0 - 7, OUT)
            put(img, bx + dx, y1 + 7, OUT)
        # la tornapunta: de lo alto del poste a la viga
        sx = 1 if side == 0 else -1
        x_ini = bx + (5 if side == 0 else -1)
        for i in range(7):
            xx = x_ini + sx * i
            yy = y0 - 6 + i
            if yy >= y0:
                break
            put(img, xx, yy, O_L)
            put(img, xx, yy + 1, O_D)
            put(img, xx, yy - 1, OUT)
    # unas hebras sueltas que cuelgan bajo la viga
    for _ in range(14):
        xx = rnd.randint(x0 + 8, x1 - 8)
        if abs(xx - FW // 2) < 16:
            continue
        for i in range(rnd.randint(1, 3)):
            put(img, xx, y1 + i, S_D if i else S)
    # en el centro: el lazo que cuelga de la viga, y dentro la capucha con los ojos
    cx, cy = FW // 2, 8
    # la soga de arriba (desde la viga) y el nudo
    for y in range(0, 3):
        put(img, cx - 1, y, OUT)
        put(img, cx, y, S)
        put(img, cx + 1, y, S_D)
        put(img, cx + 2, y, OUT)
    for dy in range(-10, 11):
        for dx in range(-10, 11):
            d = math.hypot(dx * 0.95, dy)
            if cy + dy >= FY0 - 1 or cy + dy < 2:
                continue
            if 7.0 <= d < 9.6:
                # el lazo: canamo trenzado (en diagonal)
                t = (dx + dy * 2) % 4
                c = S_L if t == 0 else S if t < 3 else S_D
                if d >= 9.0:
                    c = OUT
                put(img, cx + dx, cy + dy, c)
            elif d < 7.0:
                # la capucha (negra, con algo de brillo arriba a la izquierda) y su pico
                c = C_L if dx + dy < -7 else C_D if dx + dy < 4 else C_DD
                if d > 6.2:
                    c = OUT
                put(img, cx + dx, cy + dy, c)
    # los ojos encendidos
    for ex in (-3, 2):
        put(img, cx + ex, cy, J_L)
        put(img, cx + ex + 1, cy, J_H)
        put(img, cx + ex, cy + 1, J)
        put(img, cx + ex + 1, cy + 1, J_L)
        put(img, cx + ex - 1, cy, J_D)
    return img


def relleno_verdugo(k):
    """Una soga empapada de sangre que gira: franjas en diagonal que corren con el cuadro."""
    n = noise2(131)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            fase = (x + y * 2 - k * 2) % 8
            t = 0.25 + 0.5 * mid + 0.35 * (n((x - k * 3) * 0.12, y * 0.6) - 0.5)
            c = mix(X_D, X, max(0.0, min(1.0, t * 1.3)))
            if fase < 2:
                c = mix(c, X_L, 0.45 + 0.3 * mid)
            elif fase == 7:
                c = mix(c, X_DD, 0.5)
            if y == 0:
                c = mix(c, X_L, 0.3)
            if y == 6:
                c = mix(c, X_DD, 0.6)
            # hebras de canamo que asoman
            if (x * 7 + y * 13 - k * 17) % 83 == 0 and 1 <= y <= 5:
                c = S
            img.putpixel((x, y), c)
    return img


def fibras():
    """Lo que cae al recibir dano: hebras de canamo, una hoja seca y gotas oscuras."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["s..", ".s.", "..S"], ["S.", "s.", ".s"], [".h.", "hHh", ".h."], ["x.", "x.", ".X"])
    col = {"s": S, "S": S_L, "h": O_L, "H": O, "x": X, "X": X_L}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- el Barquero de la Laguna Negra (parte 7j)

W_DD, W_D, W, W_L = hexc("0e0b09"), hexc("1f1812"), hexc("352a1f"), hexc("4f4030")
I_DD, I_D, I, I_L = hexc("101013"), hexc("26262c"), hexc("3c3c45"), hexc("63636e")
A_D, A, A_L, A_H = hexc("0f4d5e"), hexc("2aa3b8"), hexc("7fe3f2"), hexc("e6fdff")
B_DD, B_D, B, B_L = hexc("030607"), hexc("0a1317"), hexc("122228"), hexc("1f3a44")
G_D, G, G_L = hexc("6b5420"), hexc("a07d28"), hexc("e0bf4a")


def marco_barquero():
    """El remo tumbado: la pertiga de madera mojada con anillas de hierro, la pala ancha en la punta derecha y el garfio de
    hierro en la izquierda; de la pertiga cuelgan cadenitas con monedas viejas (el peaje); en el centro, la linterna de
    almas con su asa, la reja de hierro y el fuego azul palido dentro."""
    rnd = random.Random(97)
    n = noise2(67)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la pertiga: vetas a lo largo, oscura y mojada (mas clara arriba, donde brilla el agua)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.06, y * 0.9)
            c = W_D if v < 0.4 else W if v < 0.82 else W_L
            if y == y0 + 1:
                c = mix(c, B_L, 0.35)
            if (x * 7 + y * 5) % 67 == 0:
                c = W_DD
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, W_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # las anillas de hierro
    for x in range(x0 + 22, x1 - 20, 34):
        if abs(x - FW // 2) < 22:
            continue
        for y in range(y0, y1 + 1):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(3):
                put(img, x + k, y, I_L if k == 0 else I if k == 1 else I_D)
        put(img, x - 1, y0, OUT)
        put(img, x + 3, y1, OUT)
    # la pala (punta derecha): ancha, de arriba abajo, con su banda de hierro y el borde comido
    px0 = FW - 14
    for y in range(y0 - 7, y1 + 8):
        ancho = 12 - (abs(y - (y0 + y1) // 2) // 4)
        for dx in range(ancho):
            c = W if dx > 2 else W_L if dx == 1 else W_D
            if dx == 0:
                c = I_L
            if (y * 3 + dx * 5) % 17 == 0:
                c = W_DD
            put(img, px0 + dx, y, c)
        put(img, px0 - 1, y, OUT)
        put(img, px0 + ancho, y, OUT)
    for dx in range(-1, 13):
        put(img, px0 + dx, y0 - 8, OUT)
        put(img, px0 + dx, y1 + 8, OUT)
    # el garfio (punta izquierda): la punta hacia fuera y el gancho que sube y vuelve
    for x in range(0, 9):
        for y in (y0 + 3, y0 + 4):
            put(img, x, y, I_L if y == y0 + 3 else I_D)
        put(img, x, y0 + 2, OUT)
        put(img, x, y0 + 5, OUT)
    put(img, 0, y0 + 3, I_DD)
    for y in range(y0 - 6, y0 + 3):
        put(img, 5, y, I)
        put(img, 6, y, I_D)
        put(img, 4, y, OUT)
        put(img, 7, y, OUT)
    for x in range(6, 11):
        put(img, x, y0 - 6, I_L)
        put(img, x, y0 - 7, OUT)
        put(img, x, y0 - 5, I_D if x > 7 else I)
    put(img, 10, y0 - 4, I)
    put(img, 11, y0 - 5, OUT)
    # las cadenitas con monedas que cuelgan bajo la pertiga
    for xx in (x0 + 40, x0 + 64, x1 - 70, x1 - 46, x1 - 30):
        largo = rnd.randint(3, 5)
        for i in range(largo):
            put(img, xx, y1 + 1 + i, I if i % 2 == 0 else I_D)
        cy = y1 + 1 + largo
        for dx in (-1, 0, 1):
            for dy in (0, 1, 2):
                if abs(dx) + abs(dy - 1) <= 1:
                    put(img, xx + dx, cy + dy, G_L if dx < 0 or dy == 0 else G)
        put(img, xx + 1, cy + 2, G_D)
    # en el centro: la linterna de almas (el asa, la tapa, la reja y el fuego)
    cx = FW // 2
    # el asa (un aro) que asoma por encima de la pertiga
    for dx in range(-3, 4):
        put(img, cx + dx, 0, OUT)
    for y in range(1, 4):
        put(img, cx - 3, y, I)
        put(img, cx + 3, y, I_D)
        put(img, cx - 4, y, OUT)
        put(img, cx + 4, y, OUT)
    for dx in range(-2, 3):
        put(img, cx + dx, 1, I_L)
    # el cuerpo: de y 4 a y 17 (sobre la pertiga), 15 de ancho
    for y in range(4, 18):
        for dx in range(-7, 8):
            borde = abs(dx) == 7 or y in (4, 17)
            tapa = y in (5, 6, 15, 16)
            barra = dx in (-6, 0, 6)
            if borde:
                c = OUT
            elif tapa:
                c = I_L if y in (5, 15) else I_D
            elif barra:
                c = I if dx != 6 else I_D
            else:
                # el fuego de almas: blanco en el centro abajo, azul por los bordes
                d = math.hypot(dx * 0.9, (y - 12) * 0.8)
                c = A_H if d < 1.6 else A_L if d < 3.0 else A if d < 4.6 else A_D
            put(img, cx + dx, y, c)
    return img


def relleno_barquero(k):
    """Agua negra que corre, con ondas y destellos de almas azules que pasan."""
    n = noise2(151)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.2 + 0.45 * mid + 0.4 * (n((x - k * 4) * 0.09, y * 0.5) - 0.5)
            c = mix(B_D, B, max(0.0, min(1.0, t * 1.4)))
            onda = (x - k * 4 + int(3 * math.sin((x + y * 3) * 0.2))) % 23
            if onda in (0, 1) and 1 <= y <= 5:
                c = mix(c, B_L, 0.7)
            if y == 0:
                c = mix(c, B_L, 0.35)
            if y == 6:
                c = mix(c, B_DD, 0.6)
            # los destellos de almas
            if (x * 11 + y * 7 - k * 29) % 97 == 0 and 1 <= y <= 5:
                c = A_L
            elif (x * 11 + y * 7 - k * 29) % 97 == 1 and 1 <= y <= 5:
                c = A
            img.putpixel((x, y), c)
    return img


def gotas():
    """Lo que cae al recibir dano: gotas de agua negra, una moneda y una chispa de almas."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".b.", "bBb", ".b."], ["b.", "B.", "b."], [".g.", "gGg", ".g."], [".a.", "aAa", ".a."])
    col = {"b": B_L, "B": A_D, "g": G, "G": G_L, "a": A, "A": A_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Hrodric, el Abad de la Cumbre (parte 7l)

P_DD, P_D, P, P_L, P_H = hexc("1b2026"), hexc("353e47"), hexc("5b6873"), hexc("95a3ad"), hexc("d6e0e6")
Q_DD, Q_D, Q, Q_L, Q_H = hexc("10283a"), hexc("2f5f7a"), hexc("4a86a3"), hexc("9bd2e6"), hexc("ecfbff")
O2_D, O2, O2_L = hexc("6e4a14"), hexc("b8862a"), hexc("e8c560")


def marco_abad():
    """El baculo tumbado: la vara de plata escarchada con nudos de oro, el regaton a la izquierda y a la derecha la voluta
    de hielo que sube y se enrosca con el cristal que luce; carambanos colgando de la vara; en el centro la mitra de hielo
    (las dos hojas en punta, la banda de oro, el cristal de la punta) y a los lados dos cruces pequenas."""
    rnd = random.Random(131)
    n = noise2(137)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la vara: plata con vetas a lo largo, luz arriba, escarcha a trozos
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.07, y * 0.8)
            c = P_D if v < 0.38 else P if v < 0.8 else P_L
            if y == y0 + 1:
                c = mix(c, P_H, 0.5)
            if y == y1 - 1:
                c = mix(c, P_DD, 0.5)
            if n(x * 0.3 + 40, y * 0.9) > 0.86:
                c = mix(c, Q_H, 0.6)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, P_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # los nudos de oro (anillos) a lo largo
    for x in range(x0 + 26, x1 - 30, 38):
        if abs(x - FW // 2) < 26:
            continue
        for y in range(y0 - 1, y1 + 2):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(4):
                put(img, x + k, y, O2_L if k == 0 else O2 if k < 3 else O2_D)
        for k in range(-1, 5):
            put(img, x + k, y0 - 2, OUT)
            put(img, x + k, y1 + 2, OUT)
    # el regaton (punta izquierda): una punta de plata
    for x in range(0, 6):
        for y in range(y0 + 1, y1):
            if abs(y - (y0 + y1) / 2) <= (x + 1) * 0.7:
                put(img, x, y, P_L if y < (y0 + y1) / 2 else P_D)
    # la voluta de hielo (punta derecha): sube por la derecha, se enrosca hacia dentro y luce el cristal en la curva
    vx = FW - 12
    for y in range(1, y1):
        for dx in range(3):
            put(img, vx + 6 + dx, y, Q_L if dx == 0 else Q if dx == 1 else Q_D)
        put(img, vx + 5, y, OUT)
        put(img, vx + 9, y, OUT)
    for x in range(vx - 2, vx + 9):
        for dy in range(3):
            put(img, x, 1 + dy, Q_L if dy == 0 else Q if dy == 1 else Q_D)
        put(img, x, 0, OUT)
        put(img, x, 4, OUT)
    for y in range(4, 11):
        for dx in range(3):
            put(img, vx - 2 + dx, y, Q_L if dx == 0 else Q if dx == 1 else Q_D)
        put(img, vx - 3, y, OUT)
    for x in range(vx - 2, vx + 2):
        put(img, x, 11, OUT)
    # el cristal dentro de la curva
    for y in range(5, 10):
        for x in range(vx + 1, vx + 5):
            d = abs(x - (vx + 2.5)) + abs(y - 7)
            if d <= 2.5:
                put(img, x, y, Q_H if d < 1.2 else Q_L)
    # los carambanos que cuelgan de la vara
    for xx in (x0 + 36, x0 + 50, x0 + 80, x1 - 86, x1 - 62, x1 - 44):
        largo = rnd.randint(3, 6)
        for i in range(largo):
            ancho = 1 if i >= largo - 2 else 2
            for dx in range(ancho):
                put(img, xx + dx, y1 + 1 + i, Q_H if dx == 0 and i < 2 else Q_L if i < largo - 1 else Q)
        put(img, xx - 1, y1 + 1, OUT)
    # dos cruces de oro pequenas sobre la vara, a los lados de la mitra
    for cx in (FW // 2 - 40, FW // 2 + 40):
        for y in range(y0 - 7, y0 + 1):
            put(img, cx, y, O2_L if y < y0 - 3 else O2)
            put(img, cx + 1, y, O2_D)
        for x in range(cx - 2, cx + 4):
            put(img, x, y0 - 5, O2_L)
        put(img, cx, y0 - 8, OUT)
    # en el centro: la mitra de hielo (de y 0 a 18), con su banda de oro y el cristal de la punta
    cx = FW // 2
    for y in range(0, 19):
        semi = 7 if y >= 7 else max(1, int((y + 1) * 1.0))
        for dx in range(-semi - 1, semi + 2):
            put(img, cx + dx, y, OUT)
    for y in range(1, 18):
        semi = 6 if y >= 7 else max(0, int(y * 0.95))
        for dx in range(-semi, semi + 1):
            c = Q_L if dx < -semi // 2 else Q if dx <= semi // 2 else Q_D
            if dx == 0 and y >= 4:
                c = mix(O2, Q_L, 0.35)          # la franja de oro de en medio, bajo el hielo
            if y >= 15:
                c = O2_L if (dx + y) % 2 else O2   # la banda
            put(img, cx + dx, y, c)
    for y in range(0, 4):
        put(img, cx, y, Q_H)
    put(img, cx - 2, 16, Q_H)
    put(img, cx + 2, 16, Q_H)
    return img


def relleno_abad(k):
    """Hielo azul que corre, con vetas claras y destellos blancos que pasan."""
    n = noise2(167)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.25 + 0.45 * mid + 0.4 * (n((x - k * 3) * 0.08, y * 0.5) - 0.5)
            c = mix(Q_D, Q, max(0.0, min(1.0, t * 1.4)))
            veta = (x - k * 3 + int(2 * math.sin((x + y * 4) * 0.17))) % 19
            if veta in (0, 1) and 1 <= y <= 5:
                c = mix(c, Q_L, 0.65)
            if y == 0:
                c = mix(c, Q_H, 0.45)
            if y == 6:
                c = mix(c, Q_DD, 0.6)
            if (x * 13 + y * 5 - k * 31) % 89 == 0 and 1 <= y <= 5:
                c = Q_H
            img.putpixel((x, y), c)
    return img


def copos():
    """Lo que cae al recibir dano: copos de nieve y esquirlas de hielo."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".w.", "wWw", ".w."], ["w.w", ".W.", "w.w"], [".q", "qQ", "q."], ["q", "Q", "q"])
    col = {"w": Q_L, "W": Q_H, "q": Q, "Q": Q_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- el Condestable del Juramento (parte 7n)

K_DD, K_D, K, K_L, K_H = hexc("08080a"), hexc("17161a"), hexc("2a282e"), hexc("47434c"), hexc("6e6874")
E_DD, E_D, E, E_L, E_H = hexc("3a0d06"), hexc("8a2208"), hexc("d24a14"), hexc("f88a2c"), hexc("ffe08a")
G_D, G, G_L = hexc("6e5622"), hexc("a8842e"), hexc("e2c464")
CR_D, CR = hexc("451210"), hexc("74221c")


def marco_condestable():
    """El montante tumbado: la hoja negra con los filos al rojo y rajas de brasa; a la izquierda la cruz de oro, el puno de
    cuero y el pomo; a la derecha la punta; dos manos alzadas de oro a los lados y en el centro el gran yelmo cerrado con la
    rendija encendida y su cimera de crines en ascuas."""
    rnd = random.Random(171)
    n = noise2(173)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la hoja: acero ennegrecido; los filos de arriba y de abajo al rojo; rajas de brasa
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.06, y * 0.7)
            c = K_D if v < 0.4 else K if v < 0.82 else K_L
            if y == y0 + 1:
                c = E_L if (x // 3) % 4 else E
            if y == y1 - 1:
                c = E if (x // 3) % 4 else E_D
            if n(x * 0.25 + 70, y * 0.8) > 0.88 and y0 + 1 < y < y1 - 1:
                c = E_D
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, K_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # la punta (derecha): la hoja se estrecha
    for x in range(x1, FW):
        semi = (FW - x) * 0.45
        for y in range(y0 - 1, y1 + 2):
            m = (y0 + y1) / 2
            if abs(y - m) <= semi:
                put(img, x, y, E_L if abs(abs(y - m) - semi) < 1 else K)
    # la cruz de oro (izquierda), el puno y el pomo
    cx0 = x0 + 2
    for y in range(y0 - 7, y1 + 8):
        for dx in range(3):
            put(img, cx0 + dx, y, G_L if dx == 0 else G if dx == 1 else G_D)
        put(img, cx0 - 1, y, OUT)
        put(img, cx0 + 3, y, OUT)
    for dx in range(-1, 4):
        put(img, cx0 + dx, y0 - 8, OUT)
        put(img, cx0 + dx, y1 + 8, OUT)
    for x in range(0, cx0 - 1):
        for y in range(y0 + 2, y1 - 1):
            put(img, x, y, CR_D if (x + y) % 3 == 0 else hexc("2e2015"))
    for y in range(y0, y1 + 1):
        for x in range(0, 2):
            put(img, x, y, G_L if x == 0 else G)
    # dos manos alzadas de oro, a los lados del yelmo
    mano = ["#.#.#", "#.#.#", "#####", "#####", ".###.", "..#.."]
    for mx in (FW // 2 - 42, FW // 2 + 38):
        for yy, fila in enumerate(mano):
            for xx, ch in enumerate(fila):
                if ch == "#":
                    put(img, mx + xx, y0 - 6 + yy, G_L if yy < 2 else G if yy < 4 else G_D)
    # en el centro: el gran yelmo (de y 1 a 18) con la rendija encendida, la nervadura y la cimera
    cx = FW // 2
    for y in range(1, 19):
        semi = 7 if y >= 4 else 5 + (y - 1) // 2
        for dx in range(-semi - 1, semi + 2):
            put(img, cx + dx, y, OUT)
    for y in range(2, 18):
        semi = 6 if y >= 4 else 4 + (y - 2) // 2
        for dx in range(-semi, semi + 1):
            c = K_L if dx < -semi // 2 else K if dx <= semi // 2 else K_D
            if y == 9 and abs(dx) <= 5:
                c = OUT if abs(dx) in (0, 1) else (E_H if abs(dx) in (3, 4) else E_L)   # la rendija y los ojos
            if dx == 0 and y >= 10:
                c = K_H                                       # la nervadura
            if y in (12, 14) and abs(dx) in (2, 4):
                c = K_DD                                      # los respiraderos
            if y == 5:
                c = G if (dx + y) % 2 else G_L                # la banda de oro de la frente
            put(img, cx + dx, y, c)
    # la cimera: crines en ascuas que suben del yelmo
    for dx in range(-2, 3):
        alto = 3 + rnd.randint(0, 2) - abs(dx)
        for i in range(alto):
            put(img, cx + dx, 1 - i if 1 - i >= 0 else 0, E_H if i == alto - 1 else E_L)
    return img


def relleno_condestable(k):
    """Fuego que corre: brasas rojas y naranjas que se mueven, con lenguas amarillas que pasan."""
    n = noise2(181)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 5) * 0.1, y * 0.6 - k * 0.3)
            t = 0.25 + 0.5 * mid + 0.5 * (v - 0.5)
            c = mix(E_D, E, max(0.0, min(1.0, t * 1.5)))
            if t > 0.62:
                c = mix(c, E_L, min(1.0, (t - 0.62) * 3))
            if t > 0.86:
                c = mix(c, E_H, min(1.0, (t - 0.86) * 4))
            if y == 0:
                c = mix(c, E_L, 0.4)
            if y == 6:
                c = mix(c, E_DD, 0.65)
            if (x * 7 + y * 13 - k * 29) % 101 == 0 and 1 <= y <= 5:
                c = E_H
            img.putpixel((x, y), c)
    return img


def brasas():
    """Lo que salta al recibir dano: brasas y chispas (suben, no caen: lo hace el cliente)."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".e.", "eHe", ".e."], ["h.", ".E"], [".h", "hE", "e."], ["H", "e", "."])
    col = {"e": E_L, "E": E, "h": E_H, "H": E_H}
    for kk, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, kk * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- la Rectora de la Academia (parte 7p)

N_DD, N_D, N, N_L = hexc("05060f"), hexc("0d1230"), hexc("1a2660"), hexc("33459a")
L_D, L, L_L, L_H = hexc("7f8fd8"), hexc("b6c6ff"), hexc("dde6ff"), hexc("ffffff")
PL_DD, PL_D, PL, PL_L, PL_H = hexc("2a2f40"), hexc("4a5168"), hexc("7a849e"), hexc("aab3cc"), hexc("e4e9f6")
V_D, V = hexc("3b2466"), hexc("6a46a8")


def estrella(img, cx, cy, r, c_centro, c_rayo):
    """Una estrella de cuatro puntas de radio r (pixeles), con su contorno."""
    for d in range(-r - 1, r + 2):
        for (x, y) in ((cx + d, cy), (cx, cy + d)):
            put(img, x, y, OUT)
    for (x, y) in ((cx - 1, cy - 1), (cx + 1, cy - 1), (cx - 1, cy + 1), (cx + 1, cy + 1)):
        put(img, x, y, OUT)
    for d in range(-r, r + 1):
        c = c_centro if abs(d) <= 1 else c_rayo
        put(img, cx + d, cy, c)
        put(img, cx, cy + d, c)


def marco_rectora():
    """El baculo de plata tumbado con sus anillos de oro: a la derecha la estrella de cristal entre sus dos puntas, a la
    izquierda el regaton; estrellas sueltas por encima y, en el centro, la aureola de estrellas (un astrolabio de oro con
    sus doce puntos de luz y la estrella grande en medio)."""
    rnd = random.Random(191)
    n = noise2(193)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la vara: plata con vetas, luz arriba, sombra abajo
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.07, y * 0.8)
            c = PL_D if v < 0.38 else PL if v < 0.8 else PL_L
            if y == y0 + 1:
                c = mix(c, PL_H, 0.5)
            if y == y1 - 1:
                c = mix(c, PL_DD, 0.5)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, PL_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # los anillos de oro a lo largo
    for x in range(x0 + 22, x1 - 26, 34):
        if abs(x - FW // 2) < 26:
            continue
        for y in range(y0 - 1, y1 + 2):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(3):
                put(img, x + k, y, G_L if k == 0 else G if k == 1 else G_D)
        for k in range(-1, 4):
            put(img, x + k, y0 - 2, OUT)
            put(img, x + k, y1 + 2, OUT)
    # el regaton (punta izquierda)
    for x in range(0, 6):
        for y in range(y0 + 1, y1):
            if abs(y - (y0 + y1) / 2) <= (x + 1) * 0.7:
                put(img, x, y, PL_L if y < (y0 + y1) / 2 else PL_D)
    # la cabeza del baculo (derecha): dos puntas de plata y la estrella de cristal entre ellas
    sx = FW - 9
    for x in range(sx - 6, FW):
        for yy in (y0 - 5, y1 + 5):
            pass
    for y in range(y0 - 7, y1 + 8):
        for dx in (-6, 6):
            if y0 - 7 <= y <= y0 - 1 or y1 + 1 <= y <= y1 + 7:
                put(img, sx + dx, y, PL_L)
                put(img, sx + dx - 1, y, OUT)
                put(img, sx + dx + 1, y, OUT)
    estrella(img, sx, (y0 + y1) // 2, 7, L_H, L_L)
    # estrellas sueltas por encima de la vara
    for xx, yy in ((x0 + 30, y0 - 6), (x0 + 62, y0 - 9), (x0 + 88, y0 - 5), (x1 - 92, y0 - 8), (x1 - 60, y0 - 5), (x1 - 34, y0 - 9)):
        put(img, xx, yy, L_H)
        for (ax, ay) in ((xx - 1, yy), (xx + 1, yy), (xx, yy - 1), (xx, yy + 1)):
            put(img, ax, ay, L if rnd.random() < 0.6 else L_D)
    # en el centro: la aureola (un aro de oro con doce puntos de luz) y la estrella grande en medio
    cx, cy, ra = FW // 2, 10, 9
    for k in range(360):
        a = math.radians(k)
        x, y = cx + ra * math.cos(a), cy + ra * math.sin(a)
        put(img, int(round(x)), int(round(y)), G)
    for k in range(360):
        a = math.radians(k)
        for rr, c in ((ra + 1, OUT), (ra - 1, G_D)):
            put(img, int(round(cx + rr * math.cos(a))), int(round(cy + rr * math.sin(a))), c)
    for k in range(12):
        a = math.radians(k * 30)
        x, y = int(round(cx + ra * math.cos(a))), int(round(cy + ra * math.sin(a)))
        put(img, x, y, L_H if k % 3 == 0 else L_L)
    for y in range(cy - ra + 2, cy + ra - 1):
        for x in range(cx - ra + 2, cx + ra - 1):
            if (x - cx) ** 2 + (y - cy) ** 2 < (ra - 1.5) ** 2:
                put(img, x, y, mix(N_D, V_D, 0.4 + 0.3 * math.sin(x * 0.7 + y)))
    estrella(img, cx, cy, 5, L_H, L)
    return img


def relleno_rectora(k):
    """Un cielo de noche que corre: azul profundo con nubes moradas, y estrellas que pasan."""
    n = noise2(197)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 2) * 0.07, y * 0.5)
            t = 0.3 + 0.45 * mid + 0.45 * (v - 0.5)
            c = mix(N_D, N, max(0.0, min(1.0, t * 1.4)))
            if v > 0.7:
                c = mix(c, V, min(1.0, (v - 0.7) * 2.5))
            if y == 0:
                c = mix(c, N_L, 0.45)
            if y == 6:
                c = mix(c, N_DD, 0.6)
            if (x * 11 + y * 7 - k * 23) % 53 == 0 and 1 <= y <= 5:
                c = L_H
            elif (x * 5 + y * 17 - k * 13) % 61 == 0 and 1 <= y <= 5:
                c = L
            img.putpixel((x, y), c)
    return img


def estrellitas():
    """Lo que cae al recibir dano: estrellas pequenas (caen despacio, titilando: lo hace el cliente)."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".l.", "lHl", ".l."], ["l.l", ".H.", "l.l"], [".h", "hL"], ["H"])
    col = {"l": L_L, "L": L, "h": L_D, "H": L_H}
    for kk, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, kk * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- comun


# ---------------------------------------------------------------- Brann, el Ballenero (parte 7r)

F_DD, F_D, F, F_L = hexc("1d150d"), hexc("3a2b1c"), hexc("5f4a32"), hexc("8a7150")      # el fresno del asta
C_D, C, C_L = hexc("4a3f2c"), hexc("6c5d3c"), hexc("9c8b66")                             # la cuerda
K_DD, K_D, K, K_L = hexc("16171a"), hexc("34373d"), hexc("5b6068"), hexc("9aa1aa")      # el hierro de la punta
X_D, X = hexc("5f3d31"), hexc("93604a")                                                   # el oxido
S_D, S, S_L = hexc("b9c0bd"), hexc("dce2df"), hexc("ffffff")                             # la sal
V_D, V, V_L, V_H = hexc("5d6d78"), hexc("8a9ba6"), hexc("bccad2"), hexc("e9f1f4")      # la Vieja (palida)
M_DD, M_D, M, M_L, M_H = hexc("03100f"), hexc("0a2622"), hexc("124038"), hexc("2a6a5c"), hexc("cfeee6")  # el mar


def marco_ballenero():
    """El arpon tumbado: el asta de fresno con vueltas de cuerda, a la derecha la cana y la punta de hierro con sus dos barbas,
    a la izquierda el cabo atado que se enrolla y cuelga; costras de sal y percebes; en el centro, la cola de la Vieja que
    sale del agua, con la muesca y el agua que le chorrea."""
    rnd = random.Random(173)
    n = noise2(79)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # el asta: vetas a lo largo, gastada (mas clara arriba)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.05, y * 0.8)
            c = F_D if v < 0.38 else F if v < 0.8 else F_L
            if y == y0 + 1:
                c = mix(c, F_L, 0.3)
            if (x * 7 + y * 5) % 59 == 0:
                c = F_DD
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, F_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # vueltas de cuerda por el asta (rayas en diagonal)
    for xc in (x0 + 26, x0 + 70, x1 - 72, x1 - 30):
        if abs(xc - FW // 2) < 24:
            continue
        for y in range(y0 - 1, y1 + 2):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(5):
                c = C_L if (k + y) % 3 == 0 else C if (k + y) % 3 == 1 else C_D
                put(img, xc + k, y, c)
            put(img, xc - 1, y, OUT)
            put(img, xc + 5, y, OUT)
    # costras de sal y percebes por los cantos
    for _ in range(26):
        x = rnd.randint(x0 + 4, x1 - 4)
        if abs(x - FW // 2) < 22:
            continue
        y = rnd.choice((y0, y0 + 1, y1, y1 - 1))
        put(img, x, y, S if rnd.random() < 0.7 else S_D)
        if rnd.random() < 0.4:
            put(img, x + 1, y, S_L)
    # la punta (derecha): la cana de hierro, la hoja en punta y las dos barbas hacia atras
    cy = (y0 + y1) // 2
    for x in range(x1, x1 + 3):
        for y in range(cy - 1, cy + 2):
            put(img, x, y, K if y == cy else K_D)
    puntas = FW - (x1 + 3)
    for i in range(puntas):
        x = x1 + 3 + i
        alto = max(0, 5 - i // 2)
        for y in range(cy - alto, cy + alto + 1):
            c = K_L if y < cy else K if y == cy else K_D
            if (x + y) % 6 == 0:
                c = X
            put(img, x, y, c)
        put(img, x, cy - alto - 1, OUT)
        put(img, x, cy + alto + 1, OUT)
    for k in range(6):
        put(img, x1 + 2 - k, cy - 6 - k // 2, K_L if k < 4 else K)
        put(img, x1 + 2 - k, cy + 6 + k // 2, K_D if k < 4 else K_DD)
    # el cabo atado (izquierda): un rollo de vueltas y el extremo que cuelga
    for dx in range(-5, 6):
        for dy in range(-5, 6):
            d = math.hypot(dx, dy)
            if 2.2 <= d <= 5.2:
                c = C_L if (dx + dy) % 3 == 0 else C if (dx + dy) % 3 == 1 else C_D
                put(img, 6 + dx, cy + dy, c)
            elif 5.2 < d <= 6.0:
                put(img, 6 + dx, cy + dy, OUT)
    for i in range(10):
        put(img, 6 + i // 4, cy + 6 + i, C if i % 2 == 0 else C_D)
        put(img, 7 + i // 4, cy + 6 + i, OUT)
    # en el centro: la cola de la Vieja saliendo del agua (la aleta ancha en V, la muesca, el tallo y las gotas)
    cx = FW // 2
    for y in range(0, 18):
        # la aleta: dos lobulos que se abren hacia arriba
        for dx in range(-15, 16):
            ax = abs(dx)
            arriba = 2 + int((ax / 15.0) ** 1.6 * 2) if ax > 1 else 5
            abajo = 7 + int((1 - ax / 15.0) * 5)
            if ax <= 15 and arriba <= y <= abajo and not (ax <= 1 and y < 6):
                c = V_L if y <= arriba + 1 else V if y < abajo - 1 else V_D
                if (dx * 3 + y * 5) % 17 == 0:
                    c = V_H
                put(img, cx + dx, y, c)
        # el tallo de la cola, que baja al agua
        if 10 <= y <= 17:
            for dx in range(-3, 4):
                put(img, cx + dx, y, V_D if abs(dx) == 3 else V)
    # el contorno de la cola
    for y in range(0, 18):
        for dx in range(-17, 18):
            if img.getpixel((cx + dx, y))[3] == 0:
                vecinos = [(cx + dx + a, y + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))]
                if any(0 <= vx < FW and 0 <= vy < 40 and img.getpixel((vx, vy))[3] and img.getpixel((vx, vy))[:3] in (V_D[:3], V[:3], V_L[:3], V_H[:3])
                       for vx, vy in vecinos):
                    put(img, cx + dx, y, OUT)
    # el agua que le chorrea de la aleta
    for gx, gy in ((cx - 12, 11), (cx - 7, 13), (cx + 9, 12), (cx + 13, 10)):
        put(img, gx, gy, M_H)
        put(img, gx, gy + 2, M_L)
    return img


def relleno_ballenero(k):
    """El mar verde oscuro que corre, con olas de espuma que pasan."""
    n = noise2(167)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.25 + 0.4 * mid + 0.4 * (n((x - k * 4) * 0.08, y * 0.5) - 0.5)
            c = mix(M_D, M_L, max(0.0, min(1.0, t * 1.3)))
            ola = (x - k * 4 + int(3 * math.sin((x + y * 2) * 0.17))) % 29
            if ola in (0, 1, 2) and y <= 2:
                c = mix(c, M_H, 0.75 - 0.2 * ola)
            if y == 0:
                c = mix(c, M_H, 0.3)
            if y == 6:
                c = mix(c, M_DD, 0.6)
            img.putpixel((x, y), c)
    return img


def salpicaduras():
    """Lo que cae al recibir dano: gotas de agua de mar y cristales de sal."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".m.", "mMm", ".m."], ["m.", "M.", "m."], [".s.", "sSs", ".s."], ["s.", "Ss", ".s"])
    col = {"m": M_L, "M": M_H, "s": S, "S": S_L}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Anselmo, el Pontifice Sangrante (parte 7t)

PO_DD, PO_D, PO, PO_L, PO_H = hexc("3a2a0c"), hexc("6b511b"), hexc("a07d28"), hexc("d2ae4a"), hexc("f6e19a")
PS_DD, PS_D, PS, PS_L, PS_H = hexc("1e0405"), hexc("4a080c"), hexc("7c1218"), hexc("b0232a"), hexc("e0605a")
PW_D, PW, PW_L = hexc("8f887a"), hexc("c8c0aa"), hexc("ece6d2")


def marco_pontifice():
    """La ferula tumbada: la vara de oro con sangre seca, a la derecha la cruz de tres travesanos (de pie: el mas corto, el
    de fuera), a la izquierda el regaton; gotas de sangre que cuelgan de la vara; en el centro la tiara de tres coronas de oro,
    la seda blanca manchada de la sangre que le chorrea desde la cruz de lo alto."""
    rnd = random.Random(271)
    n = noise2(83)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la vara de oro: brillo arriba, sombra abajo, sangre seca a manchas
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.06, y * 0.7)
            c = PO_D if v < 0.35 else PO if v < 0.78 else PO_L
            if y == y0 + 1:
                c = mix(c, PO_H, 0.45)
            if y == y1 - 1:
                c = mix(c, PO_DD, 0.5)
            if n(x * 0.21 + 9, y * 0.4) > 0.8:
                c = mix(c, PS_D, 0.75)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, PO_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # los nudos de la vara (anillos mas anchos)
    for xc in (x0 + 30, x1 - 34):
        for y in range(y0 - 1, y1 + 2):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(4):
                put(img, xc + k, y, PO_H if k == 1 else PO_L if k == 2 else PO)
            put(img, xc - 1, y, OUT)
            put(img, xc + 4, y, OUT)
    # la cruz de tres travesanos (derecha): de pie sobre el final de la vara (pasan por detras del relleno), el de fuera el
    # mas corto, y el remate de la vara
    cy = (y0 + y1) // 2
    for x in range(x1, FW - 1):
        for y in range(cy - 1, cy + 2):
            put(img, x, y, PO_L if y == cy - 1 else PO if y == cy else PO_D)
    for xb, alto in ((x1 - 11, 13), (x1 - 6, 11), (x1 - 1, 9)):
        for y in range(cy - alto, cy + alto + 1):
            if FY0 - 1 <= y <= FY1:
                continue
            put(img, xb, y, PO_L)
            put(img, xb + 1, y, PO)
            put(img, xb + 2, y, PO_D)
        for xx in (xb - 1, xb + 3):
            for y in range(cy - alto, cy + alto + 1):
                if not (FY0 - 1 <= y <= FY1) and not (y0 <= y <= y1):
                    put(img, xx, y, OUT)
        for xx in range(xb, xb + 3):
            put(img, xx, cy - alto - 1, OUT)
            put(img, xx, cy + alto + 1, OUT)
        # la sangre que gotea de cada travesano
        put(img, xb + 1, cy + alto + 2, PS_L)
        put(img, xb + 1, cy + alto + 3, PS)
    # el regaton (izquierda)
    for x in range(1, x0 + 1):
        for y in range(cy - 2, cy + 3):
            put(img, x, y, PO_D if abs(y - cy) == 2 else PO)
        put(img, x, cy - 3, OUT)
        put(img, x, cy + 3, OUT)
    # gotas de sangre que cuelgan de la vara
    for _ in range(14):
        x = rnd.randint(x0 + 6, x1 - 6)
        if abs(x - FW // 2) < 18:
            continue
        largo = rnd.randint(1, 3)
        for k in range(largo):
            put(img, x, y1 + 1 + k, PS_L if k == largo - 1 else PS)
    # en el centro: la tiara (tres pisos de seda que se estrechan, las tres coronas de oro, la bola y la cruz)
    cx = FW // 2
    pisos = ((14, 18, 9), (12, 13, 8), (10, 9, 7), (8, 5, 5))
    for top, bottom, ancho in ((5, 9, 9), (10, 14, 10), (15, 19, 11)):
        pass
    capas = ((16, 20, 11), (11, 15, 9), (6, 10, 7), (3, 5, 5))
    for ya, yb, w in capas:
        for y in range(ya, yb + 1):
            for dx in range(-w, w + 1):
                c = PW if abs(dx) < w - 1 else PW_D
                if dx < -w + 3:
                    c = mix(c, PW_D, 0.4)
                if dx > 2 and abs(dx) < w - 1:
                    c = PW_L
                put(img, cx + dx, y, c)
        # la corona de oro del pie de cada piso, con sus flores y sus piedras
        for dx in range(-w - 1, w + 2):
            put(img, cx + dx, yb, PO_L if dx % 2 else PO)
            put(img, cx + dx, yb - 1, PO if (dx % 4) else hexc("c41f33"))
            if dx % 3 == 0:
                put(img, cx + dx, yb - 2, PO_H)
    # la bola y la cruz
    for dx in (-1, 0, 1):
        for y in (1, 2):
            put(img, cx + dx, y, PO_L)
    put(img, cx, 0, PO_H)
    # la sangre que le chorrea a la tiara desde arriba
    for gx, largo in ((cx - 4, 9), (cx + 3, 13), (cx - 8, 5), (cx + 7, 7), (cx, 4)):
        for y in range(4, 4 + largo):
            if img.getpixel((gx, y))[3]:
                put(img, gx, y, PS_L if y == 3 + largo else PS)
    # el contorno de la tiara
    for y in range(0, 22):
        for dx in range(-14, 15):
            if img.getpixel((cx + dx, y))[3] == 0:
                vecinos = [(cx + dx + a, y + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))]
                if any(0 <= vx < FW and 0 <= vy < 40 and img.getpixel((vx, vy))[3] and img.getpixel((vx, vy))[:3] in (PW[:3], PW_D[:3], PW_L[:3], PO_L[:3], PO[:3], PO_H[:3])
                       for vx, vy in vecinos) and y < FY0 - 1:
                    put(img, cx + dx, y, OUT)
    return img


def relleno_pontifice(k):
    """La sangre que corre, espesa, con vetas mas claras que pasan."""
    n = noise2(191)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.2 + 0.45 * mid + 0.4 * (n((x - k * 3) * 0.07, y * 0.6) - 0.5)
            c = mix(PS_D, PS_L, max(0.0, min(1.0, t * 1.3)))
            veta = (x - k * 3 + int(2 * math.sin((x + y * 3) * 0.13))) % 37
            if veta in (0, 1) and 1 <= y <= 3:
                c = mix(c, PS_H, 0.6 - 0.25 * veta)
            if y == 0:
                c = mix(c, PS_H, 0.25)
            if y == 6:
                c = mix(c, PS_DD, 0.6)
            img.putpixel((x, y), c)
    return img


def gotas_sangre():
    """Lo que cae al recibir dano: gotas de sangre y chispas de oro."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".s.", "sSs", ".s."], ["s.", "S.", "s."], [".o.", "oOo", ".o."], ["s", "S", "S"])
    col = {"s": PS, "S": PS_L, "o": PO, "O": PO_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Aldric, el Primer Marcado (parte 7v)

HN_DD, HN_D, HN, HN_L, HN_H = hexc("08080a"), hexc("18181d"), hexc("2c2d33"), hexc("4b4d56"), hexc("7b7e89")
OX_D, OX = hexc("3a1c10"), hexc("6a3418")
MR_DD, MR_D, MR, MR_L, MR_H = hexc("2a0403"), hexc("6a0b07"), hexc("b81a10"), hexc("ff5a2e"), hexc("ffd2a8")


def marco_aldric():
    """La cadena de hierro negro de punta a punta: eslabones tumbados (el aro, con su luz arriba y el oxido) y de pie (de
    canto: una barra gruesa que pasa por encima y por debajo del hueco); a la izquierda el gancho, a la derecha el eslabon
    roto que cuelga; en el centro la marca (el ojo en el aro roto con sus tres espinas) al rojo, con brasas que le caen."""
    rnd = random.Random(907)
    n = noise2(97)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    cy = (y0 + y1) / 2.0

    def hierro(x, y, base):
        v = n(x * 0.3, y * 0.5)
        c = mix(base, HN_D, 0.35) if v < 0.3 else base if v < 0.75 else mix(base, HN_L, 0.4)
        if n(x * 0.17 + 5, y * 0.33) > 0.78:
            c = mix(c, OX, 0.55)
        return c

    # los eslabones tumbados: aros de 2 y pico de grueso que asoman por encima y por debajo del hueco, cada 24 px
    paso = 24
    centros = list(range(x0 + 12, x1 - 6, paso))
    for cx in centros:
        rx, ry = 11.8, 8.6
        for y in range(0, 40):
            for x in range(cx - 14, cx + 15):
                d = math.hypot((x - cx) / rx, (y - cy) / ry)
                if 0.74 <= d <= 1.0:
                    c = hierro(x, y, HN)
                    if y < cy - 4:
                        c = mix(c, HN_H, 0.55 if d < 0.86 else 0.3)
                    elif y > cy + 4:
                        c = mix(c, HN_DD, 0.35)
                    put(img, x, y, c)
                elif 1.0 < d <= 1.13 or 0.64 <= d < 0.74:
                    if img.getpixel((x, y))[3] == 0:
                        put(img, x, y, OUT)
    # los eslabones de pie (de canto) en las juntas: una barra que pasa por los dos aros
    for cx in centros[:-1]:
        bx = cx + paso // 2 - 1
        for y in range(int(cy) - 8, int(cy) + 9):
            for k in range(3):
                c = hierro(bx + k, y, HN_L if k == 0 else HN if k == 1 else HN_D)
                if abs(y - cy) > 7:
                    c = OUT
                put(img, bx + k, y, c)
            put(img, bx - 1, y, OUT)
            put(img, bx + 3, y, OUT)
    # el hueco del relleno, limpio
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, OUT)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # el gancho de la izquierda
    gy = int(cy)
    for x in range(0, x0 + 1):
        for y in range(gy - 2, gy + 3):
            put(img, x, y, hierro(x, y, HN if abs(y - gy) < 2 else HN_D))
        put(img, x, gy - 3, OUT)
        put(img, x, gy + 3, OUT)
    for k, (dx, dy) in enumerate(((0, 4), (0, 5), (1, 6), (2, 7), (3, 7), (4, 6), (4, 5))):
        put(img, 1 + dx, gy + dy, HN_L if k < 3 else HN)
    # el eslabon roto de la derecha: cuelga del ultimo, abierto (la grieta al rojo)
    ex = x1 + 1
    for y in range(gy, 40):
        for x in range(ex - 3, FW):
            d = math.hypot((x - (ex + 1)) / 3.2, (y - 31) / 7.5)
            if 0.7 <= d <= 1.08 and not (y > 34 and x > ex + 1):
                put(img, x, y, hierro(x, y, HN))
    put(img, ex + 2, 36, MR_L)
    put(img, ex + 3, 35, MR)
    # en el centro, la marca: el aro roto, el ojo y las tres espinas, al rojo
    mx, my = FW // 2, 10
    for y in range(0, FY0 - 1):
        for x in range(mx - 13, mx + 14):
            d = math.hypot(x - mx, (y - my) * 1.05)
            ang = math.degrees(math.atan2(y - my, x - mx))
            if 7.0 <= d <= 9.4 and not (-75 <= ang <= -48):
                put(img, x, y, MR_L if d < 8.2 else MR)
            elif 9.4 < d <= 10.6 and not (-75 <= ang <= -48):
                put(img, x, y, MR_DD)
    # el ojo
    for y in range(my - 3, my + 4):
        for x in range(mx - 6, mx + 7):
            e = ((x - mx) / 6.2) ** 2 + ((y - my) / 3.3) ** 2
            if e <= 1.0:
                put(img, x, y, MR_D if e > 0.55 else MR)
    for y in range(my - 2, my + 3):
        for x in range(mx - 2, mx + 3):
            if (x - mx) ** 2 + (y - my) ** 2 <= 4:
                put(img, x, y, MR_H if (x - mx) ** 2 + (y - my) ** 2 <= 1 else MR_L)
    # las tres espinas (abajo, a un lado y a otro)
    for k, (ax, ay) in enumerate(((0, 1), (-0.87, -0.5), (0.87, -0.5))):
        for t in range(10, 15):
            x, y = int(round(mx + ax * t)), int(round(my + ay * t * 0.95))
            put(img, x, y, MR_L if t < 13 else MR)
            if t < 12:
                put(img, x + (1 if ax >= 0 else -1), y, MR_D)
    # brasas que le caen a la marca
    for _ in range(7):
        x = mx + rnd.randint(-9, 9)
        y = rnd.randint(1, 16)
        if img.getpixel((x, y))[3] == 0:
            put(img, x, y, MR_L if rnd.random() < 0.5 else MR_H)
    return img


def relleno_aldric(k):
    """El hierro al rojo que corre: brasas oscuras con vetas de fuego que pasan."""
    n = noise2(311)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.15 + 0.5 * mid + 0.45 * (n((x - k * 4) * 0.09, y * 0.7) - 0.5)
            c = mix(MR_DD, MR, max(0.0, min(1.0, t * 1.4)))
            veta = (x - k * 4 + int(3 * math.sin((x + y * 5) * 0.11))) % 29
            if veta in (0, 1, 2) and 2 <= y <= 4:
                c = mix(c, MR_L if veta != 1 else MR_H, 0.6)
            if y == 0:
                c = mix(c, MR_L, 0.2)
            if y == 6:
                c = mix(c, MR_DD, 0.7)
            img.putpixel((x, y), c)
    return img


def esquirlas():
    """Lo que cae al recibir dano: esquirlas de hierro y brasas."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["h.", "Hh", ".h"], [".r.", "rRr", ".r."], ["hH", "h."], ["r", "R", "r"])
    col = {"h": HN_L, "H": HN_H, "r": MR, "R": MR_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- los gigantes errantes (parte 7w)

PI_DD, PI_D, PI, PI_L, PI_H = hexc("15130f"), hexc("2f2b24"), hexc("514a3e"), hexc("7d745f"), hexc("b0a58a")
MUS_D, MUS = hexc("24301b"), hexc("3e5530")
TI_DD, TI_D, TI, TI_L, TI_H = hexc("1c110a"), hexc("3c2615"), hexc("6b4a27"), hexc("9e7642"), hexc("d8b071")
OJO_G = hexc("e6b45a")


def marco_gigante():
    """El dintel de los gigantes: una viga de sillares de piedra vieja (juntas, grietas y musgo) de punta a punta; a la izquierda
    el munon de una columna rota, a la derecha la punta partida; en el centro la cabeza de piedra de un gigante (la frente baja,
    las cejas, los ojos cerrados con una rendija de luz, la nariz y la barba tallada) que asoma por encima."""
    rnd = random.Random(1201)
    n = noise2(131)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 5, FX1 + 4, FY0 - 6, FY1 + 5

    def piedra(x, y):
        v = n(x * 0.22, y * 0.45)
        c = PI_D if v < 0.28 else PI if v < 0.74 else PI_L
        if n(x * 0.13 + 7, y * 0.3) > 0.8:
            c = mix(c, MUS, 0.55)
        return c

    juntas = set(range(x0 + 19, x1 - 6, 23))
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            c = piedra(x, y)
            if y == y0:
                c = mix(c, PI_H, 0.5)
            elif y == y0 + 1:
                c = mix(c, PI_L, 0.3)
            elif y == y1:
                c = mix(c, PI_DD, 0.6)
            if x in juntas:
                c = mix(PI_DD, c, 0.25)
            put(img, x, y, c)
        put(img, x0 - 1, y, OUT)
        put(img, x1 + 1, y, OUT)
    for x in range(x0 - 1, x1 + 2):
        put(img, x, y0 - 1, OUT)
        put(img, x, y1 + 1, OUT)
    # grietas
    for _ in range(9):
        x, y = rnd.randint(x0 + 4, x1 - 4), rnd.choice((y0 + 1, y1 - 1))
        for k in range(rnd.randint(3, 6)):
            put(img, x, y, PI_DD)
            x += rnd.choice((-1, 0, 1))
            y += 1 if y < (y0 + y1) / 2 else -1
            if not (y0 < y < y1):
                break
    # musgo que cuelga por debajo
    for x in range(x0 + 2, x1 - 2, 3):
        if rnd.random() < 0.35:
            for k in range(rnd.randint(1, 3)):
                put(img, x, y1 + 1 + k, MUS if k == 0 else MUS_D)
    # el hueco del relleno, limpio
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, OUT)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # el munon de la columna (izquierda) y la punta partida (derecha)
    for y in range(y0 - 4, y1 + 5):
        for x in range(0, x0):
            if y < y0 - 4 + (x % 3):
                continue
            c = piedra(x, y)
            if x % 3 == 1:
                c = mix(c, PI_D, 0.45)                      # las estrias
            put(img, x, y, c)
    for x in range(x1 + 2, FW):
        for y in range(y0 + (x - x1) // 2, y1 + 1 - (x - x1) // 3):
            put(img, x, y, piedra(x, y))
    # la cabeza de piedra del centro
    mx, my = FW // 2, 9
    for y in range(0, FY0 - 1):
        for x in range(mx - 14, mx + 15):
            dx, dy = (x - mx) / 13.5, (y - my) / 10.0
            if dx * dx + dy * dy * 0.8 <= 1.0 or (y >= my and abs(x - mx) <= 12):
                c = piedra(x, y)
                put(img, x, y, c)
    # contorno de la cabeza (mirando una copia: si no, el contorno se extiende solo)
    antes = img.copy()
    for y in range(0, FY0 - 1):
        for x in range(mx - 15, mx + 16):
            if antes.getpixel((x, y))[3] == 0 and any(0 <= x + a < FW and 0 <= y + b < 40 and antes.getpixel((x + a, y + b))[3] > 0
                                                       for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, OUT)
    # la frente y las cejas, los ojos cerrados (una rendija de luz), la nariz y la barba tallada
    for x in range(mx - 9, mx + 10):
        put(img, x, my - 1, PI_DD if abs(x - mx) > 1 else PI_D)
        put(img, x, my - 2, PI_H if abs(x - mx) > 2 else PI_L)
    for s_ in (-1, 1):
        for k in range(2, 8):
            put(img, mx + s_ * k, my + 1, OJO_G if 3 <= k <= 6 else PI_DD)
            put(img, mx + s_ * k, my + 2, PI_DD if 2 <= k <= 7 else PI_D)
    for y in range(my, my + 6):
        put(img, mx, y, PI_L)
        put(img, mx + 1, y, PI_D)
    for x in range(mx - 2, mx + 3):
        put(img, x, my + 6, PI_DD)
    for x in range(mx - 8, mx + 9):
        for y in range(my + 8, FY0 - 1):
            if (x + y) % 3 == 0:
                put(img, x, y, PI_D)
    return img


def relleno_gigante(k):
    """La tierra que corre: vetas de ocre y polvo de piedra que pasan despacio."""
    n = noise2(331)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.2 + 0.5 * mid + 0.45 * (n((x - k * 3) * 0.08, y * 0.6) - 0.5)
            c = mix(TI_DD, TI_L, max(0.0, min(1.0, t * 1.3)))
            veta = (x - k * 3 + int(2 * math.sin((x + y * 7) * 0.09))) % 31
            if veta in (0, 1) and 1 <= y <= 5:
                c = mix(c, TI_H, 0.45)
            if y == 0:
                c = mix(c, TI_L, 0.25)
            if y == 6:
                c = mix(c, TI_DD, 0.7)
            img.putpixel((x, y), c)
    return img


def piedrecitas():
    """Lo que cae al recibir dano: lascas de piedra y polvo."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["pP", "Pp"], [".p.", "pPp", ".p."], ["P.", "pp"], ["t", "T", "t"])
    col = {"p": PI_L, "P": PI_H, "t": TI_L, "T": TI_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Ser Lucan, el Jinete Nocturno (parte 7x)

AS_DD, AS_D, AS, AS_L = hexc("100b09"), hexc("231812"), hexc("3a2a1f"), hexc("5a4334")
HJ_DD, HJ_D, HJ, HJ_L, HJ_H = hexc("0b0d11"), hexc("20242c"), hexc("3d434e"), hexc("68707d"), hexc("a9b2bf")
GU_D, GU, GU_L = hexc("3e0a11"), hexc("6e1621"), hexc("9c2b35")
OJ_D, OJ, OJ_L = hexc("6b4a12"), hexc("b8892a"), hexc("ecc860")
AL_DD, AL_D, AL, AL_L, AL_H = hexc("06141e"), hexc("10364c"), hexc("2a7299"), hexc("78c2e6"), hexc("e4f7ff")


def dentro(px, py, pol):
    """Si el punto esta dentro del poligono (par-impar)."""
    d = False
    j = len(pol) - 1
    for i in range(len(pol)):
        xi, yi = pol[i]
        xj, yj = pol[j]
        if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi) + xi:
            d = not d
        j = i
    return d


def marco_jinete():
    """La guja de Ser Lucan tumbada: el asta de fresno negro con anillas de hierro y el regaton a la izquierda; a la derecha el
    cubo y la hoja de hierro (con el espolon por arriba, la punta que se alza y el filo encendido de almas); en el centro el
    yelmo cerrado del heraldo con la corona del Rey y la rendija azul, y debajo la banderola roja con la corona de oro."""
    rnd = random.Random(1307)
    n = noise2(173)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    cy = (FY0 + FY1 - 1) / 2.0                       # 22: el centro del hueco
    xa0, xa1 = 2, 176                                 # el asta
    ya0, ya1 = FY0 - 3, FY1 + 2                       # 16..28

    def madera(x, y):
        v = n(x * 0.05, y * 0.9)
        c = AS_D if v < 0.3 else AS if v < 0.78 else AS_L
        if (y - ya0) in (0,):
            c = mix(c, AS_L, 0.5)
        if y == ya1:
            c = mix(c, AS_DD, 0.6)
        return c

    def hierro(x, y, base):
        v = n(x * 0.3 + 40, y * 0.5)
        return mix(base, HJ_D, 0.35) if v < 0.3 else base if v < 0.75 else mix(base, HJ_L, 0.35)

    # el asta
    for y in range(ya0, ya1 + 1):
        for x in range(xa0, xa1 + 1):
            put(img, x, y, madera(x, y))
    for x in range(xa0, xa1 + 1):
        put(img, x, ya0 - 1, OUT)
        put(img, x, ya1 + 1, OUT)
    # la hoja (un poligono: el lomo con el espolon, la punta que se alza a la derecha y el filo por abajo)
    hoja = [(184, 14.5), (200, 13.5), (214, 13.0), (217, 12.5), (222, 3.5), (225.5, 12.5), (236, 12.0), (243, 9.5),
            (248, 6.0), (252, 2.5), (255.6, 0.4), (255.6, 7.0), (255.2, 15.0), (254.6, 22.0), (253.0, 28.0),
            (249.5, 32.2), (240, 33.6), (225, 33.2), (210, 32.6), (196, 31.6), (184, 30.2)]
    for y in range(0, 40):
        for x in range(178, FW):
            if dentro(x + 0.5, y + 0.5, hoja):
                c = hierro(x, y, HJ)
                # el lomo con su luz, el vaceo (la acanaladura) y el filo encendido
                if not dentro(x + 0.5, y - 0.5, hoja):
                    c = mix(c, HJ_H, 0.6)
                elif not dentro(x + 0.5, y - 1.5, hoja):
                    c = mix(c, HJ_L, 0.4)
                if y == 16 and 188 <= x <= 238:
                    c = mix(c, HJ_DD, 0.5)
                filo = not dentro(x + 1.5, y + 1.5, hoja) or not dentro(x + 0.5, y + 1.5, hoja)
                if filo and (y > 26 or x > 248):
                    c = AL_L if (x + y) % 5 else AL_H
                elif filo:
                    c = mix(c, AL, 0.5)
                put(img, x, y, c)
    # contorno de la hoja
    antes = img.copy()
    for y in range(0, 40):
        for x in range(176, FW):
            if antes.getpixel((x, y))[3] == 0 and any(0 <= x + a < FW and 0 <= y + b < 40 and antes.getpixel((x + a, y + b))[3] > 0
                                                       for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, OUT)
    # el cubo de hierro (donde la hoja abraza el asta) con sus remaches
    for y in range(ya0 - 3, ya1 + 4):
        for x in range(172, 185):
            c = hierro(x, y, HJ)
            if y == ya0 - 3:
                c = mix(c, HJ_H, 0.55)
            elif y == ya1 + 3:
                c = mix(c, HJ_DD, 0.6)
            if x in (172, 184):
                c = OUT
            put(img, x, y, c)
        put(img, 171, y, OUT)
    for x in range(171, 186):
        put(img, x, ya0 - 4, OUT)
        put(img, x, ya1 + 4, OUT)
    for ry in (ya0 - 1, ya1 + 1):
        put(img, 176, ry, HJ_H)
        put(img, 180, ry, HJ_H)
    # las anillas de hierro del asta
    for ax in (24, 58, 92, 150):
        for y in range(ya0 - 2, ya1 + 3):
            for k in range(4):
                c = hierro(ax + k, y, HJ_L if k == 0 else HJ if k < 3 else HJ_D)
                if y in (ya0 - 2, ya1 + 2):
                    c = OUT
                put(img, ax + k, y, c)
            put(img, ax - 1, y, OUT)
            put(img, ax + 4, y, OUT)
    # el regaton (la contera de hierro de la izquierda), en punta
    for x in range(0, 12):
        h = 1.2 + x * 0.55
        for y in range(int(cy - h), int(cy + h) + 2):
            c = hierro(x, y, HJ if abs(y - cy) < h - 1 else HJ_D)
            if y < cy - h + 1.2:
                c = mix(c, HJ_H, 0.5)
            put(img, x, y, c)
        put(img, x, int(cy - h) - 1, OUT)
        put(img, x, int(cy + h) + 2, OUT)
    # el hueco del relleno, limpio
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, OUT)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # debajo del centro, la banderola roja con la corona de oro (la gualdrapa del caballo), de dos puntas
    mx = FW // 2
    for y in range(ya1 + 2, 40):
        for x in range(mx - 12, mx + 13):
            corte = y > 34 and abs(x - mx) < (y - 34) * 1.6
            if corte:
                continue
            c = GU if n(x * 0.2 + 9, y * 0.3) < 0.7 else GU_L
            if abs(x - mx) >= 11:
                c = OJ if (y % 2) else OJ_D
            elif y == 39 or (y > 34 and abs(abs(x - mx) - (y - 34) * 1.6) < 1.2):
                c = OJ_D
            put(img, x, y, c)
        put(img, mx - 13, y, OUT)
        put(img, mx + 13, y, OUT)
    for k, (dx, dy) in enumerate(((-3, 33), (-3, 32), (-1, 32), (0, 31), (1, 32), (3, 32), (3, 33), (-2, 33), (-1, 33), (0, 33),
                                   (1, 33), (2, 33), (-2, 34), (-1, 34), (0, 34), (1, 34), (2, 34))):
        put(img, mx + dx, dy, OJ_L if dy < 33 else OJ)
    # en el centro, el yelmo cerrado del heraldo con la corona y la rendija azul
    for y in range(2, FY0 - 2):
        for x in range(mx - 9, mx + 10):
            dx = abs(x - mx + 0.5)
            ancho = 8.6 if y >= 6 else 8.6 - (6 - y) * 0.6
            if dx <= ancho:
                c = hierro(x, y, HJ)
                if x < mx - 5:
                    c = mix(c, HJ_L, 0.35)
                elif x > mx + 5:
                    c = mix(c, HJ_DD, 0.35)
                put(img, x, y, c)
    antes = img.copy()
    for y in range(0, FY0 - 1):
        for x in range(mx - 11, mx + 12):
            if antes.getpixel((x, y))[3] == 0 and any(0 <= x + a < FW and 0 <= y + b < 40 and antes.getpixel((x + a, y + b))[3] > 0
                                                       for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, OUT)
    # la rendija (encendida de almas) y los respiraderos
    for x in range(mx - 7, mx + 8):
        if abs(x - mx + 0.5) > 1:
            put(img, x, 8, AL_H if abs(x - mx + 0.5) < 5 else AL_L)
            put(img, x, 9, AL)
    for y in range(4, FY0 - 2):
        put(img, mx, y, OJ if y != 8 and y != 9 else OJ_L)
    for yy in (12, 14):
        for xx in (mx - 5, mx - 3, mx + 3, mx + 5):
            put(img, xx, yy, HJ_DD)
    # la corona del Rey sobre el yelmo
    for x in range(mx - 6, mx + 7):
        put(img, x, 2, OJ_D)
        put(img, x, 1, OJ)
    for px_ in (mx - 6, mx - 3, mx, mx + 3, mx + 6):
        put(img, px_, 0, OJ_L)
    # unas chispas de almas alrededor del yelmo
    for _ in range(8):
        x = mx + rnd.choice((-1, 1)) * rnd.randint(11, 16)
        y = rnd.randint(1, 15)
        if img.getpixel((x, y))[3] == 0:
            put(img, x, y, AL_L if rnd.random() < 0.6 else AL_H)
    return img


def relleno_jinete(k):
    """El fuego de almas que corre: una niebla azul oscura con jirones palidos que pasan deprisa."""
    n = noise2(353)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.12 + 0.55 * mid + 0.5 * (n((x - k * 5) * 0.07, y * 0.6) - 0.5)
            c = mix(AL_DD, AL, max(0.0, min(1.0, t * 1.35)))
            jiron = (x - k * 5 + int(3 * math.sin((x * 0.6 + y * 9) * 0.1))) % 37
            if jiron in (0, 1, 2, 3) and 1 <= y <= 5:
                c = mix(c, AL_L if jiron in (1, 2) else AL, 0.6)
            if jiron == 2 and y == 3:
                c = mix(c, AL_H, 0.7)
            if y == 0:
                c = mix(c, AL_L, 0.18)
            if y == 6:
                c = mix(c, AL_DD, 0.7)
            img.putpixel((x, y), c)
    return img


def chispas_almas():
    """Lo que cae al recibir dano: chispas de almas y esquirlas de hierro."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".a.", "aAa", ".a."], ["h.", "Hh", ".h"], ["a", "A", "a"], ["Aa", ".a"])
    col = {"a": AL_L, "A": AL_H, "h": HJ_L, "H": HJ_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Pell, el Bufon del Rey (parte 7y)

BU_R_D, BU_R, BU_R_L = hexc("5a0a10"), hexc("a8141c"), hexc("d8404a")
BU_K_D, BU_K, BU_K_L = hexc("0c080c"), hexc("221a22"), hexc("3e3440")
BU_O_D, BU_O, BU_O_L, BU_O_H = hexc("6e4a10"), hexc("b8862a"), hexc("e6be52"), hexc("fff0b0")
BU_W_D, BU_W, BU_W_L = hexc("9c968c"), hexc("dcd6cc"), hexc("fbf8f2")


def rombo_bufon(x, y, lado=4):
    """Los rombos de arlequin: 0 rojo, 1 negro."""
    return ((x + y) // lado + (x - y + 400) // lado) % 2


def cascabel(img, cx, cy, r):
    """Un cascabel dorado con su ranura y su brillo."""
    for y in range(int(cy - r - 1), int(cy + r + 2)):
        for x in range(int(cx - r - 1), int(cx + r + 2)):
            d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
            if d <= r:
                luz = ((x + 0.5 - cx + r * 0.4) ** 2 + (y + 0.5 - cy + r * 0.4) ** 2) ** 0.5 / (r * 1.6)
                c = BU_O_L if luz < 0.35 else BU_O if luz < 0.75 else BU_O_D
                if abs(y + 0.5 - cy - r * 0.25) < 0.6 and abs(x + 0.5 - cx) < r * 0.7:
                    c = BU_K_D
                put(img, x, y, c)
            elif d <= r + 1.0:
                put(img, x, y, OUT)
    put(img, int(cx - r * 0.35), int(cy - r * 0.4), BU_O_H)


def marco_bufon():
    """La banda de arlequin de Pell (rombos rojos y negros entre dos filetes de oro) con un cascabel en cada punta y, en el
    centro, su mascara (mitad blanca y mitad negra, los ojos encendidos y la sonrisa) bajo el gorro de tres puntas con sus
    cascabeles; debajo, dos dagas cruzadas."""
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    ya0, ya1 = FY0 - 3, FY1 + 2                       # 16..28
    x0, x1 = 10, FW - 11
    # la banda
    for y in range(ya0, ya1 + 1):
        for x in range(x0, x1 + 1):
            k = rombo_bufon(x, y)
            c = (BU_R if k == 0 else BU_K)
            if y == ya0:
                c = mix(c, BU_R_L if k == 0 else BU_K_L, 0.5)
            if y == ya1:
                c = mix(c, BU_K_D, 0.5)
            put(img, x, y, c)
    for x in range(x0, x1 + 1):
        put(img, x, ya0 - 1, BU_O_L if x % 6 else BU_O_H)
        put(img, x, ya1 + 1, BU_O if x % 6 else BU_O_L)
        put(img, x, ya0 - 2, OUT)
        put(img, x, ya1 + 2, OUT)
    # los cascabeles de las puntas
    cascabel(img, 6.5, (ya0 + ya1) / 2 + 0.5, 5.2)
    cascabel(img, FW - 6.5, (ya0 + ya1) / 2 + 0.5, 5.2)
    # el hueco del relleno, limpio
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, OUT)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    mx = FW // 2
    # debajo del centro, dos dagas cruzadas (la punta hacia abajo)
    for k in range(0, 9):
        for s in (-1, 1):
            x, y = mx + s * (5 - k), ya1 + 3 + k
            if 0 <= y < 40:
                put(img, x, y, BU_W_L if k < 6 else BU_W_D)
    for s in (-1, 1):
        put(img, mx + s * 6, ya1 + 2, BU_O)
        put(img, mx + s * 4, ya1 + 2, BU_O)
    # en el centro, la mascara: mitad blanca (a la izquierda) y mitad negra, con los ojos en rombo encendidos y la sonrisa
    for y in range(5, FY0 - 2):
        for x in range(mx - 8, mx + 8):
            dx = abs(x + 0.5 - mx)
            ancho = 7.6 if y < 13 else 7.6 - (y - 12) * 1.4
            if dx > ancho:
                continue
            blanca = x < mx
            c = (BU_W if dx < 5 else BU_W_D) if blanca else (BU_K_L if dx < 5 else BU_K)
            put(img, x, y, c)
    for (ex, ey) in ((mx - 4, 9), (mx + 3, 9)):
        for (a, b) in ((0, -1), (-1, 0), (0, 0), (1, 0), (0, 1)):
            put(img, ex + a, ey + b, hexc("ff5a3c") if (a, b) == (0, 0) else hexc("b01818"))
    for x in range(mx - 4, mx + 4):
        y = 13 + (0 if abs(x + 0.5 - mx) > 2.5 else 1)
        put(img, x, y, BU_R if x < mx else BU_R_L)
    # el gorro: tres puntas (izquierda, derecha y en medio, hacia arriba) rojas y negras, con su cascabel en cada una
    for y in range(2, 6):
        for x in range(mx - 8, mx + 8):
            put(img, x, y, BU_R if (x < mx) == (y % 2 == 0) else BU_K)
    for k in range(0, 5):
        put(img, mx - 9 - k, 4 - k // 2, BU_R)
        put(img, mx + 8 + k, 4 - k // 2, BU_K)
    put(img, mx, 1, BU_R)
    put(img, mx - 1, 1, BU_K)
    cascabel(img, mx - 14.5, 3.0, 2.4)
    cascabel(img, mx + 13.5, 3.0, 2.4)
    cascabel(img, mx - 0.5, 0.5, 1.2)
    # contorno de la mascara y el gorro
    antes = img.copy()
    for y in range(0, FY0 - 1):
        for x in range(mx - 18, mx + 18):
            if antes.getpixel((x, y))[3] == 0 and any(0 <= x + a < FW and 0 <= y + b < 40 and antes.getpixel((x + a, y + b))[3] > 0
                                                       for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, OUT)
    return img


def relleno_bufon(k):
    """La vida de Pell: rombos rojos que corren, con un filo dorado que brilla."""
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            r = rombo_bufon(x - k * 3, y, 3)
            c = BU_R_L if r == 0 else BU_R
            if y == 0:
                c = mix(c, BU_O_L, 0.45)
            if y == 6:
                c = mix(c, BU_R_D, 0.6)
            if (x - k * 3) % 29 == 0 and 1 <= y <= 5:
                c = mix(c, BU_O_H, 0.5)
            img.putpixel((x, y), c)
    return img


def confeti():
    """Lo que cae al recibir dano: confeti rojo, dorado, negro y blanco."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["rr", "rR"], ["o.", "oO", ".o"], ["kK", "k."], ["w", "W", "w"])
    col = {"r": BU_R, "R": BU_R_L, "o": BU_O, "O": BU_O_H, "k": BU_K_L, "K": BU_K, "w": BU_W, "W": BU_W_L}
    for i, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, i * 8 + 3 + x, 3 + y, col[ch])
    return img


# ---------------------------------------------------------------- Vorthak, el Dragon de Ceniza (parte 7z)

DR_E = [hexc(c) for c in ("0b0a0a", "131111", "1b1817", "24201e", "2e2926", "3a3430", "48413b")]
DR_C = [hexc(c) for c in ("4e4944", "67615a", "827a71", "9d948a", "b6ada2")]
DR_H = [hexc(c) for c in ("3a332b", "5c5345", "82765f", "a89b80", "c9bea3", "e2d9c2")]
DR_B = [hexc(c) for c in ("4a0e02", "8a2204", "d24c0c", "ff8a26", "ffc35a", "fff0b0")]
DR_O = [hexc(c) for c in ("7a3a06", "ff9a20", "ffd24a", "fff4b8")]


def escama_dragon(x, y, n):
    """El color de las escamas del lomo en (x, y): filas de escamas redondas (la de arriba de cada una, con brillo de ceniza) y
    grietas de brasa que las cruzan."""
    fila = y // 3
    sx = (x + (fila % 2) * 3) % 6
    sy = y % 3
    c = DR_E[3]
    if sy == 0 and 1 <= sx <= 4:
        c = DR_C[0] if sx in (2, 3) else DR_E[5]
    elif sy == 2 or sx == 0:
        c = DR_E[1]
    v = n(x * 0.12, y * 0.9)
    if v > 0.86:
        c = DR_B[3] if v > 0.93 else DR_B[2]
    elif v > 0.8:
        c = DR_B[1]
    return c


def marco_dragon():
    """El lomo de Vorthak de punta a punta (escamas de carbon con grietas de brasa y su fila de puas de hueso por encima), la
    hoja de la punta de la cola a la izquierda y una garra a la derecha; en el centro su cabeza de frente, con los cuernos hacia
    atras, las cejas de hueso, los ojos de oro fundido y brasas en los ollares."""
    n = noise2(331)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    ya0, ya1 = FY0 - 4, FY1 + 3
    x0, x1 = 13, FW - 13
    for y in range(ya0, ya1 + 1):
        for x in range(x0, x1 + 1):
            c = escama_dragon(x, y, n)
            if y == ya0:
                c = mix(c, DR_C[1], 0.4)
            if y == ya1:
                c = mix(c, DR_E[0], 0.6)
            put(img, x, y, c)
    for x in range(x0, x1 + 1):
        put(img, x, ya0 - 1, OUT)
        put(img, x, ya1 + 1, OUT)
    # las puas del espinazo, inclinadas hacia la cola (a la izquierda)
    for px in range(x0 + 6, x1 - 4, 9):
        if abs(px - FW // 2) < 22:
            continue
        for k in range(4):
            for w in range(max(1, 3 - k)):
                put(img, px - k + w, ya0 - 2 - k, DR_H[4] if w == 0 else DR_H[2])
        put(img, px - 4, ya0 - 5, DR_H[5])
    # el hueco del relleno, limpio
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, OUT)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # la punta de la cola (izquierda): una hoja de hueso negro con los cantos de brasa
    my = (ya0 + ya1) / 2
    for x in range(0, x0 + 1):
        semi = 6.5 * math.sin(math.pi * min(1.0, (x + 1) / (x0 + 2))) + 0.5
        for y in range(int(my - semi) - 1, int(my + semi) + 2):
            d = abs(y + 0.5 - my)
            if d > semi + 0.6:
                continue
            c = DR_E[2] if d < semi - 1.2 else (DR_B[3] if d < semi else DR_B[1])
            if abs(y + 0.5 - my) < 0.8:
                c = DR_E[5]
            put(img, x, y, c)
    # la garra (derecha): tres unas curvas de hueso sobre los dedos negros
    for y in range(ya0, ya1 + 1):
        for x in range(x1 + 1, x1 + 5):
            put(img, x, y, DR_E[4] if (y + x) % 3 else DR_E[2])
    for i, gy in enumerate((ya0 + 1, int(my), ya1 - 1)):
        for k in range(6):
            gx = x1 + 5 + k
            yy = gy + (k * k) // 9
            put(img, gx, yy, DR_H[4] if k < 4 else DR_H[5])
            put(img, gx, yy - 1, DR_H[2] if k < 5 else (0, 0, 0, 0))
    # en el centro, la cabeza de frente (de y 0 a 22): el craneo, el hocico y la mandibula
    cx = FW // 2
    def ancho(y):
        if y < 3:
            return 3 + y
        if y < 11:
            return 9
        if y < 16:
            return 9 - (y - 10)
        return max(1, 4 - (y - 16) // 2)
    for y in range(0, 23):
        a = ancho(y)
        for dx in range(-a - 1, a + 1):
            put(img, cx + dx, y, OUT)
    for y in range(1, 22):
        a = ancho(y) - 1
        for dx in range(-a, a):
            v = n((cx + dx) * 0.4 + 40, y * 0.4)
            c = DR_E[4] if dx < -a // 3 else DR_E[3] if dx < a // 3 else DR_E[2]
            if v > 0.84:
                c = DR_B[2]
            if y == 1 or (y < 3 and abs(dx) < 2):
                c = DR_C[1]
            put(img, cx + dx, y, c)
    # las cejas de hueso y los ojos de oro fundido
    for dx in range(-8, 8):
        if abs(dx + 0.5) > 1.5:
            put(img, cx + dx, 6, DR_H[3] if dx < 0 else DR_H[2])
    for s in (-1, 1):
        ex = cx + (-5 if s < 0 else 4)
        for (a, b, c) in ((0, 0, DR_O[3]), (1, 0, DR_O[2]), (-1, 0, DR_O[1]), (0, 1, DR_O[1]), (1, 1, DR_O[0])):
            put(img, ex + a, 8 + b, c)
    # los ollares con brasas y la boca entreabierta con dientes
    for s in (-1, 1):
        put(img, cx + (-3 if s < 0 else 2), 15, DR_B[4])
        put(img, cx + (-3 if s < 0 else 2), 16, DR_B[2])
    for dx in range(-3, 3):
        put(img, cx + dx, 19, DR_B[1] if dx % 2 else DR_B[2])
        if dx % 2 == 0:
            put(img, cx + dx, 18, DR_H[5])
    # los cuernos: del craneo hacia atras y arriba, a cada lado (dos trozos)
    for s in (-1, 1):
        bx = cx + (-9 if s < 0 else 8)
        for k in range(10):
            x = bx + s * (k + 1)
            y = 5 - (k * 2) // 3
            put(img, x, y, DR_H[4] if k < 7 else DR_H[5])
            put(img, x, y + 1, DR_H[2])
            put(img, x, y - 1, OUT)
            put(img, x, y + 2, OUT)
        # el cuerno de la mejilla, hacia abajo y atras
        for k in range(5):
            put(img, bx + s * (k + 1), 13 + k // 2, DR_H[3])
            put(img, bx + s * (k + 1), 14 + k // 2, OUT)
    return img


def relleno_dragon(k):
    """La vida de Vorthak: brasas que corren entre escamas negras (las grietas de su lomo), con lenguas amarillas que pasan."""
    n = noise2(337)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 4) * 0.13, y * 0.7 + k * 0.2)
            t = 0.2 + 0.55 * mid + 0.55 * (v - 0.5)
            c = mix(DR_B[0], DR_B[2], max(0.0, min(1.0, t * 1.4)))
            if t > 0.6:
                c = mix(c, DR_B[3], min(1.0, (t - 0.6) * 3))
            if t > 0.84:
                c = mix(c, DR_B[4], min(1.0, (t - 0.84) * 4))
            # las escamas negras que asoman entre las brasas
            if ((x - k * 4 + (y // 3) * 3) % 6 == 0) and t < 0.5:
                c = mix(c, DR_E[2], 0.6)
            if y == 0:
                c = mix(c, DR_B[3], 0.35)
            if y == 6:
                c = mix(c, DR_E[1], 0.6)
            img.putpixel((x, y), c)
    return img


def ascuas_dragon():
    """Lo que salta al recibir dano: ascuas y copos de ceniza (suben, no caen: lo hace el cliente)."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".e.", "eHe", ".e."], ["h.", ".E"], ["cC", "C."], [".c", "c."])
    col = {"e": DR_B[3], "E": DR_B[2], "h": DR_B[5], "H": DR_B[4], "c": DR_C[2], "C": DR_C[4]}
    for kk, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, kk * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- la reina Isolde (parte 7ab)

IS_K = [hexc(c) for c in ("070508", "100c12", "1a141e", "261e2c", "33293b")]
IS_P = [hexc(c) for c in ("55515e", "7d7888", "a7a2b2", "d2cddb", "f4f0fa")]
IS_R = [hexc(c) for c in ("240307", "4a0810", "780f1a", "a81a28", "d83444", "f47a86")]
IS_O = [hexc(c) for c in ("4a320e", "7c5a1c", "b08632", "dcb658", "f6e29a")]
IS_F = [hexc(c) for c in ("8e8690", "b6aeb8", "d6ced6", "ece6ec")]


def encaje(x, y):
    """El encaje del velo: 0 seda, 1 hilo, 2 florecita (cada 8 px, en los cruces)."""
    if (x % 8 == 4) and (y % 8 == 4 or y % 8 == 0):
        return 2
    return 1 if (x + y) % 4 == 0 or (x - y) % 4 == 0 else 0


def rosa_negra(img, cx, cy, r):
    """Una rosa negra marchita: petalos en espiral (negros con el filo rojo oscuro) y una espina."""
    for y in range(int(cy - r - 1), int(cy + r + 2)):
        for x in range(int(cx - r - 1), int(cx + r + 2)):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = (dx * dx + dy * dy) ** 0.5
            if d <= r:
                ang = math.atan2(dy, dx)
                # la espiral de los petalos
                v = (ang / (2 * math.pi) + d / r * 1.6) % 1.0
                c = IS_K[3] if v < 0.45 else IS_K[1]
                if 0.42 < v < 0.52:
                    c = IS_R[2] if d > r * 0.35 else IS_R[3]
                if d < r * 0.22:
                    c = IS_R[1]
                put(img, x, y, c)
            elif d <= r + 1.0:
                put(img, x, y, OUT)


def lagrima(img, x, y, largo):
    """Una lagrima negra que cae (la punta arriba), con su brillo."""
    for k in range(largo):
        w = 0 if k < largo - 3 else 1
        for a in range(-w, w + 1):
            put(img, x + a, y + k, IS_K[0] if a == 0 else IS_K[2])
        put(img, x - w - 1, y + k, OUT)
        put(img, x + w + 1, y + k, OUT)
    put(img, x, y + largo, OUT)
    put(img, x, y - 1, OUT)
    put(img, x, y + largo - 2, IS_P[2])


def marco_isolde():
    """El velo de Isolde tendido de punta a punta (encaje negro con su ribete de plata y las ondas colgando), las rosas negras de
    las puntas y, en el centro, la cara tras el velo, la corona y las lagrimas."""
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    ya0, ya1 = FY0 - 3, FY1 + 1
    x0, x1 = 11, FW - 12
    for y in range(ya0, ya1 + 1):
        for x in range(x0, x1 + 1):
            k = encaje(x, y)
            c = IS_K[1] if k == 0 else IS_K[3] if k == 1 else IS_P[1]
            if y == ya1:
                c = mix(c, IS_K[0], 0.5)
            put(img, x, y, c)
    # el ribete de plata de arriba (con una perla cada 6) y las ondas del encaje colgando abajo
    for x in range(x0, x1 + 1):
        put(img, x, ya0 - 1, IS_P[3] if x % 6 else IS_P[4])
        put(img, x, ya0 - 2, OUT)
        put(img, x, ya1 + 1, IS_K[3])
        onda = ya1 + 2 + (1 if (x % 6) in (2, 3) else 0)
        for y in range(ya1 + 2, onda + 1):
            put(img, x, y, IS_K[2] if (x % 6) not in (0, 5) else IS_P[0])
        put(img, x, onda + 1, OUT)
    # el hueco del relleno, limpio
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, OUT)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # las rosas negras de las puntas, con una hoja seca
    my = (ya0 + ya1) / 2 + 0.5
    for cx in (6.0, FW - 6.0):
        rosa_negra(img, cx, my, 5.4)
        s = -1 if cx < FW / 2 else 1
        for k in range(4):
            put(img, int(cx) + s * (3 + k), int(my + 5 + k // 2), IS_K[3] if k < 3 else OUT)
    mx = FW // 2
    # la cara tras el velo: un ovalo palido que asoma por los huecos del encaje, con los ojos rojos
    for y in range(4, FY0 - 1):
        for x in range(mx - 8, mx + 8):
            dx, dy = (x + 0.5 - mx) / 6.6, (y + 0.5 - 10.5) / 7.2
            if dx * dx + dy * dy > 1.0:
                continue
            k = encaje(x, y)
            luz = 1.0 - (dx * 0.7 + 0.3) ** 2 * 0.5
            c = (IS_F[2] if luz > 0.7 else IS_F[1]) if k == 0 else IS_K[3] if k == 1 else IS_P[2]
            put(img, x, y, c)
    for ex in (mx - 3, mx + 2):
        put(img, ex, 10, IS_R[4])
        put(img, ex + (1 if ex < mx else -1), 10, IS_R[2])
    # el velo cae a los lados de la cara hasta la banda
    for y in range(4, FY0 - 1):
        for s in (-1, 1):
            for w in range(2):
                x = mx + s * (7 + w + (y - 4) // 5)
                put(img, x if s > 0 else x - 1, y, IS_K[2] if w == 0 else IS_K[1])
    # la corona: la banda de oro viejo y tres puntas con gemas rojas
    for x in range(mx - 6, mx + 6):
        put(img, x, 4, IS_O[2] if x % 3 else IS_O[3])
        put(img, x, 5, IS_O[1])
    for px, alto in ((mx - 5, 3), (mx - 1, 4), (mx + 4, 3)):
        for k in range(alto):
            put(img, px, 3 - k, IS_O[3] if k < alto - 1 else IS_O[4])
            put(img, px + 1, 3 - k, IS_O[1])
    put(img, mx - 1, 4, IS_R[4])
    put(img, mx, 4, IS_R[3])
    put(img, mx - 5, 4, IS_R[3])
    put(img, mx + 4, 4, IS_R[3])
    # contorno de la cara, el velo y la corona
    antes = img.copy()
    for y in range(0, FY0 - 2):
        for x in range(mx - 14, mx + 14):
            if antes.getpixel((x, y))[3] == 0 and any(0 <= x + a < FW and 0 <= y + b < 40 and antes.getpixel((x + a, y + b))[3] > 0
                                                       for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, OUT)
    # dos lagrimas negras que caen bajo el centro
    lagrima(img, mx - 3, ya1 + 4, 5)
    lagrima(img, mx + 2, ya1 + 5, 6)
    return img


def relleno_isolde(k):
    """La vida de Isolde: seda carmesi con sus pliegues, que corren despacio, y un hilo de plata."""
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            f = 0.5 + 0.5 * math.sin((x - k * 3) * 0.21 + y * 0.35)
            g = 0.5 + 0.5 * math.sin((x - k * 3) * 0.067 + 1.3)
            t = 0.25 + 0.5 * f * 0.7 + 0.3 * g
            c = mix(IS_R[1], IS_R[3], max(0.0, min(1.0, t)))
            if f > 0.93:
                c = mix(c, IS_R[4], 0.5)
            if y == 0:
                c = mix(c, IS_R[5], 0.35)
            if y == 6:
                c = mix(c, IS_R[0], 0.6)
            if y == 3 and (x - k * 3) % 11 < 6:
                c = mix(c, IS_P[3], 0.35)
            img.putpixel((x, y), c)
    return img


def petalos():
    """Lo que cae al recibir dano: un petalo negro, uno rojo, una lagrima negra y una chispa de plata."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".kk", "kKk", "kk."], [".r", "rR", "r."], ["t", "t", "T", "T"], ["s.s", ".S.", "s.s"])
    col = {"k": IS_K[2], "K": IS_K[4], "r": IS_R[2], "R": IS_R[4], "t": IS_K[1], "T": IS_K[0], "s": IS_P[2], "S": IS_P[4]}
    for i, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, i * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- la Sombra sin Nombre (parte 7ac)

SB_K = [hexc(c) for c in ("020103", "07040a", "0e0812", "17101e", "21172b")]


def marco_sombra():
    """Las cadenas de la Sombra de punta a punta, rotas por tres sitios (las puntas de los eslabones partidos, al rojo), y en el centro
    la capucha sin cara: la oscuridad, sus dos ojos rojos y, flotando encima, la corona rota de Corvin (el oro viejo, partido)."""
    img = marco_aldric()
    mx = FW // 2
    # fuera la marca del centro (y lo que le caia): ahi va la capucha
    rect(img, mx - 16, 0, mx + 16, FY0 - 2, (0, 0, 0, 0))
    # tres roturas en la cadena: se quita un trozo de un aro y las puntas quedan al rojo
    for bx in (52, 172, 214):
        rect(img, bx, FY0 - 5, bx + 2, FY0 - 2, (0, 0, 0, 0))
        put(img, bx - 1, FY0 - 4, MR_L)
        put(img, bx - 1, FY0 - 3, MR)
        put(img, bx + 3, FY0 - 4, MR_L)
        put(img, bx + 3, FY0 - 3, MR_H)
    # la capucha: un arco apuntado de tela negra que cae hasta la banda, con su filo algo mas claro
    for y in range(4, FY0 - 1):
        for x in range(mx - 14, mx + 15):
            h = (y - 4) / (FY0 - 5)
            medio = 4.0 + 9.5 * min(1.0, h * 1.35)
            if abs(x + 0.5 - mx) > medio:
                continue
            borde = abs(x + 0.5 - mx) > medio - 1.6
            c = SB_K[3] if borde else SB_K[2]
            if borde and y < 8:
                c = SB_K[4]
            put(img, x, y, c)
    # dentro, la oscuridad (un ovalo negro) y los ojos
    for y in range(7, FY0 - 1):
        for x in range(mx - 8, mx + 9):
            e = ((x + 0.5 - mx) / 6.4) ** 2 + ((y + 0.5 - 12.5) / 6.0) ** 2
            if e <= 1.0:
                put(img, x, y, SB_K[0] if e < 0.7 else SB_K[1])
    for ex in (mx - 4, mx + 3):
        put(img, ex, 12, MR_L)
        put(img, ex + (1 if ex < mx else -1), 12, MR)
        put(img, ex, 11, MR_D)
    # la corona rota, flotando encima (ladeada: mas alta a la izquierda) y partida en medio
    for x in range(mx - 7, mx + 8):
        if x in (mx, mx + 1):
            continue
        yy = 2 + (1 if x > mx + 2 else 0)
        put(img, x, yy, IS_O[2] if x % 3 else IS_O[3])
        put(img, x, yy + 1, IS_O[1])
    for px, alto in ((mx - 6, 2), (mx - 2, 3), (mx + 3, 2), (mx + 6, 1)):
        base = 2 + (1 if px > mx + 2 else 0)
        for k in range(alto):
            put(img, px, base - 1 - k, IS_O[3] if k < alto - 1 else IS_O[4])
    put(img, mx - 2, 2, MR_L)
    # contorno de la capucha y la corona
    antes = img.copy()
    for y in range(0, FY0 - 1):
        for x in range(mx - 17, mx + 18):
            if antes.getpixel((x, y))[3] == 0 and any(0 <= x + a < FW and 0 <= y + b < 40 and antes.getpixel((x + a, y + b))[3] > 0
                                                       for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, OUT)
    return img


def relleno_sombra(k):
    """La vida de la Sombra: la oscuridad que corre con venas rojas que laten."""
    n = noise2(733)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.1 + 0.25 * mid + 0.4 * (n((x - k * 3) * 0.08, y * 0.6) - 0.5)
            c = mix(SB_K[0], SB_K[4], max(0.0, min(1.0, t)))
            vena = (x - k * 3 + int(4 * math.sin((x * 0.13) + y * 0.9))) % 23
            if vena in (0, 1) and 1 <= y <= 5:
                c = mix(c, MR if vena == 0 else MR_L, 0.75)
            if y == 3 and (x - k * 3) % 37 < 3:
                c = mix(c, MR_H, 0.4)
            if y == 0:
                c = mix(c, MR_D, 0.25)
            if y == 6:
                c = mix(c, SB_K[0], 0.6)
            img.putpixel((x, y), c)
    return img


def jirones():
    """Lo que salta al recibir dano (sube: lo hace el cliente): jirones de sombra y chispas rojas."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".k.", "kKk", ".k."], ["k.", "kk", ".k"], [".r.", "rRr", ".r."], ["r", "R"])
    col = {"k": SB_K[3], "K": SB_K[4], "r": MR, "R": MR_H}
    for i, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, i * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Sigrun, la Guardiana del Paso (parte 7ap)

S_DD, S_D, S, S_L, S_H = hexc("15191e"), hexc("2c333b"), hexc("4d5863"), hexc("8794a0"), hexc("cfdae2")
N_DD, N_D, N, N_L = hexc("070c18"), hexc("101b33"), hexc("1d3058"), hexc("34508a")
W_D, W, W_H = hexc("aeb8c2"), hexc("e3e8ee"), hexc("ffffff")
C_D, C, C_L = hexc("2a1a10"), hexc("4a2f1c"), hexc("6e4a2e")


def marco_sigrun():
    """Su espada larga tumbada de punta a punta: la hoja de acero con escarcha (el vaceo por el medio y la nieve encima), la
    empunadura a la izquierda (el puno de cuero, la guarda y el pomo) y la punta a la derecha; carambanos pequenos colgando, y en
    el centro su escudo de lagrima azul con el colmillo blanco y el cerco de acero escarchado."""
    rnd = random.Random(717)
    n = noise2(719)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 1, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la hoja: acero con vetas a lo largo, luz arriba, escarcha a trozos y el filo de arriba claro
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.06, y * 0.8)
            c = S_D if v < 0.36 else S if v < 0.8 else S_L
            if y == y0 + 1:
                c = mix(c, S_H, 0.55)
            if y == y1 - 1:
                c = mix(c, S_DD, 0.5)
            if n(x * 0.3 + 60, y * 0.9) > 0.84:
                c = mix(c, Q_H, 0.55)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, S_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # nieve encima de la hoja, a montoncitos
    for x in range(x0 + 2, x1 - 2):
        if n(x * 0.21, 3.3) > 0.55:
            put(img, x, y0 - 1, W if n(x * 0.5, 7.1) > 0.4 else W_D)
            put(img, x, y0, W_H)
    # la punta (a la derecha): se estrecha hasta la punta
    for x in range(x1, FW):
        k = x - x1
        semi = (y1 - y0) / 2 - k * 0.9
        cy = (y0 + y1) / 2
        for y in range(y0, y1 + 1):
            d = abs(y - cy)
            if d <= semi:
                put(img, x, y, S_L if y < cy else S_D)
            elif d <= semi + 1:
                put(img, x, y, OUT)
    # la empunadura (a la izquierda): el pomo, la guarda de canto (alta, de acero) y el arranque del puno de cuero
    cy = (y0 + y1) // 2
    for y in range(y0 - 5, y1 + 6):
        put(img, 0, y, OUT)
        put(img, 1, y, S_L)
        put(img, 2, y, S if (y - y0) % 5 else S_H)
        put(img, 3, y, S_D)
        put(img, 4, y, OUT)
    for x in range(0, 5):
        put(img, x, y0 - 6, OUT)
        put(img, x, y1 + 6, OUT)
    for y in (y0 - 5, y1 + 5):
        put(img, 2, y, Q_H)
    put(img, 2, cy, C_L)
    put(img, 2, cy + 1, C)
    # los carambanos pequenos que cuelgan de la hoja
    for xx in (x0 + 30, x0 + 46, x0 + 74, x1 - 80, x1 - 58, x1 - 36):
        largo = rnd.randint(2, 5)
        for i in range(largo):
            put(img, xx, y1 + 1 + i, Q_H if i < 2 else Q_L)
            if i == 0:
                put(img, xx + 1, y1 + 1, Q_L)
        put(img, xx - 1, y1 + 1, OUT)
    # en el centro: el escudo de lagrima (de y 0 a 22): azul de noche, el cerco de acero con escarcha y el colmillo blanco
    cx = FW // 2
    def semi(y):
        return 8 if y < 12 else max(0, int(8 - (y - 11) * 0.85))
    for y in range(0, 23):
        a = semi(y)
        for dx in range(-a - 1, a + 2):
            put(img, cx + dx, y, OUT)
    put(img, cx, 23, OUT)
    for y in range(1, 22):
        a = semi(y) - 1
        if a < 0:
            continue
        for dx in range(-a, a + 1):
            borde = abs(dx) >= a - 0 or y == 1
            if borde:
                c = S_L if dx < 0 else S
                if (dx + y) % 4 == 0:
                    c = Q_H
            else:
                t = n((cx + dx) * 0.2, y * 0.2)
                c = N_D if t < 0.35 else N if t < 0.8 else N_L
                if dx < -a // 2 and y < 8:
                    c = mix(c, N_L, 0.4)
            put(img, cx + dx, y, c)
    # el colmillo blanco
    fang = ["WWWWW", ".WWWW", ".WWWW", "..WWW", "..WW.", "...W."]
    for fy, fila in enumerate(fang):
        for fx, ch in enumerate(fila):
            if ch == "W":
                put(img, cx - 3 + fx, 6 + fy, W_H if fx < 2 else W)
    return img


def relleno_sigrun(k):
    """La ventisca que corre sobre el azul de noche: rachas de nieve blanca y copos que pasan."""
    n = noise2(733)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.3 + 0.4 * mid + 0.4 * (n((x - k * 4) * 0.07, y * 0.45) - 0.5)
            c = mix(N_D, N_L, max(0.0, min(1.0, t * 1.3)))
            racha = (x - k * 4 + int(3 * math.sin((x + y * 5) * 0.11))) % 23
            if racha in (0, 1, 2) and 1 <= y <= 5:
                c = mix(c, W, 0.55 - 0.15 * abs(racha - 1))
            if y == 0:
                c = mix(c, W_D, 0.4)
            if y == 6:
                c = mix(c, N_DD, 0.6)
            if (x * 17 + y * 7 - k * 29) % 71 == 0 and 1 <= y <= 5:
                c = W_H
            img.putpixel((x, y), c)
    return img


# ---------------------------------------------------------------- Hakon, el Capataz (parte 7ar)

CM_DD, CM_D, CM, CM_L = hexc("1a110a"), hexc("2e1f12"), hexc("47321d"), hexc("66492b")      # el mango
CC_D, CC, CC_L = hexc("2a170c"), hexc("43281a"), hexc("5e3a24")                            # el cuero del puno
CN_DD, CN_D, CN, CN_L, CN_H = hexc("08080a"), hexc("15151a"), hexc("26262d"), hexc("3d3d47"), hexc("626270")   # hierro negro
CV_DD, CV_D, CV, CV_L, CV_H = hexc("3a0705"), hexc("7a120b"), hexc("b8230f"), hexc("ef5a2c"), hexc("ffb27a")  # la veta
CL_D, CL, CL_L = hexc("6b4a12"), hexc("b8892a"), hexc("ecc860")                            # el laton del candil
CR_D, CR, CR_L = hexc("26252a"), hexc("3a3940"), hexc("57555e")                            # la roca


def marco_capataz():
    """El pico del capataz tumbado de punta a punta: el mango de madera con aros de hierro, el puno de cuero y el regaton a la
    izquierda y, a la derecha, la cabeza del pico doble de hierro negro con la veta roja incrustada; cristales rojos de la veta que le
    crecen por encima; en el centro el casco de hierro del capataz con su candil encendido (la llama roja)."""
    rnd = random.Random(1717)
    n = noise2(1719)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 - 6, FY0 - 3, FY1 + 2
    rect(img, x0, y0, x1, y1, OUT)
    # el mango: madera con la veta a lo largo, luz arriba
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.05, y * 0.9)
            c = CM_D if v < 0.35 else CM if v < 0.8 else CM_L
            if y == y0 + 1:
                c = mix(c, CM_L, 0.5)
            if y == y1 - 1:
                c = mix(c, CM_DD, 0.6)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, CM_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # los aros de hierro del mango
    for xx in (x0 + 40, x0 + 82, x1 - 82, x1 - 40):
        for y in range(y0 - 1, y1 + 2):
            put(img, xx - 1, y, OUT)
            put(img, xx, y, CN_L if y < (y0 + y1) // 2 else CN)
            put(img, xx + 1, y, CN_D)
            put(img, xx + 2, y, OUT)
    # el puno de cuero y el regaton (a la izquierda)
    for x in range(0, x0 + 18):
        for y in range(y0 - 1, y1 + 2):
            if x < 5:
                c = CN_L if y < (y0 + y1) // 2 else CN_D
            else:
                c = CC_L if (x + y) % 4 == 0 else CC if (x // 2) % 2 == 0 else CC_D
            put(img, x, y, c)
        put(img, x, y0 - 2, OUT)
        put(img, x, y1 + 2, OUT)
    for y in range(y0 - 2, y1 + 3):
        put(img, 0, y, OUT)
    # la cabeza del pico (a la derecha): de canto, alta, de hierro negro, con las puntas curvadas hacia el mango y la veta
    hx = x1 + 1
    for y in range(0, 40):
        d = abs(y - (y0 + y1) / 2)
        ancho = 6 - int(d * 0.22)
        cx = hx + 2 - int(0.012 * d * d * 3)
        if ancho <= 0 or d > 19:
            continue
        for x in range(cx - ancho // 2 - 1, cx + ancho // 2 + 2):
            put(img, x, y, OUT)
        for x in range(cx - ancho // 2, cx + ancho // 2 + 1):
            c = CN if x < cx else CN_D
            if x == cx - ancho // 2:
                c = CN_L
            if x == cx and 4 < d < 15:
                c = CV_L if (y % 3) else CV_H                  # la veta incrustada
            put(img, x, y, c)
    # el ojo de hierro donde entra el mango
    for y in range(y0 - 2, y1 + 3):
        for x in range(hx - 4, hx):
            put(img, x, y, CN_L if y < (y0 + y1) // 2 else CN)
        put(img, hx - 5, y, OUT)
    # cristales rojos de la veta que crecen sobre el mango (y alguno por debajo)
    for xx, alto, arriba in ((x0 + 22, 6, True), (x0 + 27, 4, True), (x0 + 58, 5, True), (x0 + 63, 3, True), (x1 - 62, 6, True),
                             (x1 - 57, 4, True), (x1 - 24, 5, True), (x0 + 100, 3, False), (x1 - 100, 3, False)):
        for i in range(alto):
            yy = y0 - 1 - i if arriba else y1 + 1 + i
            semi = 1 if i < alto - 2 else 0
            for dx in range(-semi - 1, semi + 2):
                put(img, xx + dx, yy, OUT)
        for i in range(alto):
            yy = y0 - 1 - i if arriba else y1 + 1 + i
            semi = 1 if i < alto - 2 else 0
            for dx in range(-semi, semi + 1):
                put(img, xx + dx, yy, CV_H if i >= alto - 1 else (CV_L if dx <= 0 else CV))
    # en el centro: el casco del capataz (de frente) con el ala, la cresta y el candil con su llama roja
    cx = FW // 2
    for y in range(4, 24):
        semi = 9 if y >= 9 else 9 - (9 - y) // 2
        if y >= 20:
            semi = 12
        for dx in range(-semi - 1, semi + 2):
            put(img, cx + dx, y, OUT)
    for y in range(5, 23):
        semi = 8 if y >= 9 else 8 - (9 - y) // 2
        if y >= 20:
            semi = 11
        for dx in range(-semi, semi + 1):
            t = n((cx + dx) * 0.25, y * 0.25)
            c = CN_D if t < 0.35 else CN if t < 0.8 else CN_L
            if dx < -semi // 2 and y < 14:
                c = mix(c, CN_H, 0.35)
            if y >= 20:
                c = CN_L if y == 20 else CN_D                  # el ala
            if dx == 0 and y < 20:
                c = CN_H if y % 2 else CN_L                    # la cresta
            put(img, cx + dx, y, c)
    # el candil: la cajita de laton y la llama roja encima
    for y in range(9, 16):
        for dx in range(-3, 4):
            put(img, cx + dx, y, OUT)
    for y in range(10, 15):
        for dx in range(-2, 3):
            put(img, cx + dx, y, CL if (dx + y) % 3 else CL_L)
    for y, semi, c in ((4, 0, CV_H), (5, 1, CV_L), (6, 1, CV_L), (7, 1, CV), (8, 2, CV)):
        for dx in range(-semi, semi + 1):
            put(img, cx + dx, y, c if dx != 0 else CV_H)
    put(img, cx, 3, CV_D)
    return img


def relleno_capataz(k):
    """La veta que corre por la roca negra: vetas rojas al rojo que pasan entre la piedra y algun destello."""
    n = noise2(1733)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.2 + 0.45 * mid + 0.45 * (n((x - k * 3) * 0.08, y * 0.6) - 0.5)
            c = mix(CV_DD, CV_D, max(0.0, min(1.0, t * 1.3)))
            veta = (x - k * 3 + int(2.5 * math.sin((x + y * 7) * 0.13))) % 19
            if veta in (0, 1) and 1 <= y <= 5:
                c = mix(c, CV_L if veta == 0 else CV, 0.7)
            if n(x * 0.21 + 40, y * 0.8) > 0.86:
                c = mix(c, CR_L, 0.6)                          # la roca negra entre la veta
            if (x * 13 + y * 5 - k * 23) % 67 == 0 and 2 <= y <= 4:
                c = CV_H
            if y == 0:
                c = mix(c, CV_L, 0.25)
            if y == 6:
                c = mix(c, CV_DD, 0.6)
            img.putpixel((x, y), c)
    return img


def cascajo_veta():
    """Lo que cae al recibir dano: piedrecitas de la mina y esquirlas de cristal rojo."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["pP", "Pp"], [".v.", "vVv", ".v."], ["P.", "pp"], ["v", "V", "v"])
    col = {"p": CR_L, "P": CR, "v": CV_L, "V": CV_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Fray Osmund, el Sacristan (parte 7at)

CB_DD, CB_D, CB, CB_L, CB_H = hexc("1f150a"), hexc("3a2810"), hexc("6e4c1e"), hexc("a07632"), hexc("d6b060")
HU_DD, HU_D, HU, HU_L, HU_H = hexc("3e382c"), hexc("6d6450"), hexc("9a9078"), hexc("c4b994"), hexc("e6dcbc")
AL_D, AL, AL_H = hexc("2a6a9c"), hexc("4fb4f0"), hexc("b6ecff")
HB_D, HB, HB_L = hexc("16110d"), hexc("2c2219"), hexc("45362a")
BR, BR_H = hexc("e0641e"), hexc("ffc27a")


def marco_osmund():
    """La cadena del incensario tendida de punta a punta (eslabones de bronce, uno de frente y otro de canto), a la izquierda el hueso
    con su anilla por donde se coge y a la derecha el incensario de bronce con sus agujeros encendidos; en el centro su aureola de
    huesos con las cinco calaveras y cuatro velas, y dentro la calavera de su cara bajo la capucha, con las almas en las cuencas."""
    n = noise2(1747)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 - 8, FY0 - 3, FY1 + 2
    ym = (y0 + y1) // 2
    # la cadena: eslabones de frente (un aro con su hueco) y de canto (una barra), alternos
    for x in range(x0, x1 + 1):
        k, t = divmod(x - x0, 9)
        for y in range(y0 - 1, y1 + 2):
            put(img, x, y, OUT)
        for y in range(y0, y1 + 1):
            if k % 2 == 0:
                borde = t in (0, 8) or y in (y0, y1)
                c = CB_L if y < ym else CB if y < y1 - 1 else CB_D
                if not borde and 1 < t < 7 and y0 + 2 < y < y1 - 2:
                    c = CB_DD                                  # el hueco del aro (se ve oscuro)
                if t == 0 or y == y0:
                    c = mix(c, CB_H, 0.5)
            else:
                c = CB if y0 + 3 <= y <= y1 - 3 else OUT       # de canto: una barra mas fina
                if y == y0 + 3:
                    c = CB_H
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, CB_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # a la izquierda: la anilla de bronce y el hueso para cogerlo (con sus dos cabezas)
    for x in range(0, x0 + 2):
        for y in range(y0 - 2, y1 + 3):
            put(img, x, y, OUT)
    for x in range(1, x0 + 1):
        cabeza = x < 4 or x > x0 - 4
        semi = 6 if cabeza else 3
        for y in range(ym - semi, ym + semi + 1):
            t = n(x * 0.4, y * 0.4)
            c = HU_L if y < ym - 1 else HU if y < ym + 2 else HU_D
            if t > 0.8:
                c = mix(c, HU_H, 0.5)
            put(img, x, y, c)
    # a la derecha: el incensario de bronce, redondo, con sus agujeros encendidos y la tapa en punta
    cx, cy, r = x1 + 7, ym, 9.5
    for y in range(0, 40):
        for x in range(cx - 12, min(FW, cx + 13)):
            d = math.hypot(x - cx, y - cy)
            if d <= r + 1:
                put(img, x, y, OUT)
    for y in range(0, 40):
        for x in range(cx - 12, min(FW, cx + 13)):
            d = math.hypot(x - cx, y - cy)
            if d > r:
                continue
            c = CB_L if (x - cx) + (y - cy) < -3 else CB if d < r - 1.5 else CB_D
            if abs(y - cy) <= 0.5:
                c = CB_H                                       # la banda labrada
            if d < r - 2 and (x + y) % 3 == 0 and abs(y - cy) > 1:
                c = BR_H if d < 4 else BR                      # los agujeros, con las brasas
            put(img, x, y, c)
    for i, (y, semi) in enumerate(((cy - 13, 0), (cy - 12, 1), (cy - 11, 1), (cy - 10, 2))):
        for dx in range(-semi - 1, semi + 2):
            put(img, cx + dx, y, OUT)
        for dx in range(-semi, semi + 1):
            put(img, cx + dx, y, CB_H if dx <= 0 else CB_L)
    # en el centro: la aureola de huesos
    ax, ay, ar = FW // 2, 14, 11.0
    for y in range(0, 32):
        for x in range(ax - 14, ax + 15):
            d = math.hypot(x - ax, y - ay)
            if ar - 2.2 <= d <= ar + 1.2:
                put(img, x, y, OUT)
    for y in range(0, 32):
        for x in range(ax - 14, ax + 15):
            d = math.hypot(x - ax, y - ay)
            if ar - 1.2 <= d <= ar + 0.2:
                a = math.atan2(y - ay, x - ax)
                put(img, x, y, HU_L if math.sin(a * 5) > 0 else HU)
    # dentro: la capucha y la calavera, con las almas en las cuencas
    for y in range(6, 23):
        semi = 4 if y < 8 else 6
        for dx in range(-semi - 1, semi + 2):
            put(img, ax + dx, y, OUT)
        for dx in range(-semi, semi + 1):
            put(img, ax + dx, y, HB_L if dx < -semi + 2 else HB if dx < semi - 1 else HB_D)
    cara = ["..HhhH..", ".HHHHHh.", "HoaHHaoH", "HooHHooH", "hHHnnHHh", ".hHnnHh.", ".DdDdDd.", "..dDdD.."]
    col = {"H": HU_L, "h": HU, "o": OUT, "a": AL_H, "n": OUT, "D": HU_H, "d": HU_D}
    for j, fila in enumerate(cara):
        for i, ch in enumerate(fila):
            if ch != ".":
                put(img, ax - 4 + i, 10 + j, col[ch])
    # las cinco calaveras del aro (mirando adelante) y las cuatro velas encendidas
    for grados in (0, 72, 144, 216, 288):
        a = math.radians(grados)
        sx, sy = int(round(ax + ar * math.sin(a))), int(round(ay - ar * math.cos(a)))
        for dy in range(-3, 3):
            for dx in range(-3, 3):
                put(img, sx + dx, sy + dy, OUT)
        for dy in range(-2, 2):
            for dx in range(-2, 2):
                put(img, sx + dx, sy + dy, HU_L if dy < 0 else HU)
        put(img, sx - 2, sy - 1, OUT)
        put(img, sx + 1, sy - 1, OUT)
        put(img, sx - 2, sy - 1, AL)
        put(img, sx + 1, sy - 1, AL)
    for grados in (36, 108, 252, 324):
        a = math.radians(grados)
        vx, vy = int(round(ax + (ar + 1.5) * math.sin(a))), int(round(ay - (ar + 1.5) * math.cos(a)))
        for dy in range(-5, 1):
            put(img, vx - 1, vy + dy, OUT)
            put(img, vx + 1, vy + dy, OUT)
        for dy in range(-4, 1):
            put(img, vx, vy + dy, HU_H if dy > -3 else BR_H if dy == -4 else BR)
        put(img, vx, vy - 5, BR)
    return img


def relleno_osmund(k):
    """El polvo de hueso que corre, palido, con almas azules que pasan."""
    n = noise2(1759)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.25 + 0.45 * mid + 0.45 * (n((x - k * 3) * 0.09, y * 0.6) - 0.5)
            c = mix(HU_D, HU_L, max(0.0, min(1.0, t * 1.2)))
            if n(x * 0.23 + 40, y * 0.8) > 0.84:
                c = mix(c, HU_DD, 0.5)                         # una mota oscura
            alma = (x - k * 4 + int(2.0 * math.sin((x + y * 5) * 0.17))) % 29
            if alma in (0, 1) and 2 <= y <= 4:
                c = mix(c, AL_H if alma == 0 else AL, 0.75)
            if y == 0:
                c = mix(c, HU_H, 0.3)
            if y == 6:
                c = mix(c, HU_DD, 0.5)
            img.putpixel((x, y), c)
    return img


def huesos_almas():
    """Lo que cae al recibir dano: esquirlas de hueso, una calaverita y chispas de almas."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["hH", ".h"], [".a.", "aAa", ".a."], ["HhH", "hoh"], ["H", "h", "H"])
    col = {"h": HU, "H": HU_H, "a": AL, "A": AL_H, "o": OUT}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


def hueco(a, b):
    n = noise2(113)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            img.putpixel((x, y), mix(hexc(a), hexc(b), n(x * 0.3, y * 0.5)))
    return img


def rastro(a, b):
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            img.putpixel((x, y), mix(hexc(a), hexc(b), abs(y - 3) / 3))
    return img


def atlas(marco, relleno, hue, ras, cae):
    a = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    a.paste(marco, (0, 0))
    for k in range(4):
        a.paste(relleno(k), (0, 48 + 8 * k))
    a.paste(hue, (0, 80))
    a.paste(ras, (0, 88))
    a.paste(cae, (0, 104))
    return a


if __name__ == "__main__":
    os.makedirs(GUI, exist_ok=True)
    barras = {
        "brandt": atlas(marco_brandt(), relleno_brandt, hueco("080809", "17181b"), rastro("fff2ea", "e8b0a0"), chispas()),
        "corteza": atlas(marco_corteza(), relleno_corteza, hueco("050a06", "121c14"), rastro("f2fff6", "b9f0c8"), hojas()),
        "molinera": atlas(marco_molinera(), relleno_molinera, hueco("0b0908", "1c1815"), rastro("f6f2e6", "d6cdb6"), ceniza()),
        "verdugo": atlas(marco_verdugo(), relleno_verdugo, hueco("0a0606", "1a1110"), rastro("f6ece2", "d8b9a6"), fibras()),
        "barquero": atlas(marco_barquero(), relleno_barquero, hueco("040607", "0f1a1e"), rastro("eefcff", "a9dbe6"), gotas()),
        "abad": atlas(marco_abad(), relleno_abad, hueco("05090c", "121c24"), rastro("f6fcff", "c4e2ef"), copos()),
        "condestable": atlas(marco_condestable(), relleno_condestable, hueco("0a0605", "1c1210"), rastro("fff2e0", "f0b88a"), brasas()),
        "rectora": atlas(marco_rectora(), relleno_rectora, hueco("05060c", "10142a"), rastro("f2f6ff", "b8c6ff"), estrellitas()),
        "ballenero": atlas(marco_ballenero(), relleno_ballenero, hueco("030807", "0d1c19"), rastro("effcf8", "a9d8cc"), salpicaduras()),
        "pontifice": atlas(marco_pontifice(), relleno_pontifice, hueco("0a0405", "1e0c0d"), rastro("fff0e6", "e8b8a8"), gotas_sangre()),
        "aldric": atlas(marco_aldric(), relleno_aldric, hueco("050505", "151417"), rastro("fff2ea", "e8b0a0"), esquirlas()),
        "gigante": atlas(marco_gigante(), relleno_gigante, hueco("0a0907", "1b1813"), rastro("fff6e6", "e0caa0"), piedrecitas()),
        "jinete": atlas(marco_jinete(), relleno_jinete, hueco("04070b", "0e1822"), rastro("eefaff", "a8d8ee"), chispas_almas()),
        "bufon": atlas(marco_bufon(), relleno_bufon, hueco("0a0506", "1c0d10"), rastro("fff4e8", "f0c8a0"), confeti()),
        "dragon": atlas(marco_dragon(), relleno_dragon, hueco("070505", "180e0b"), rastro("fff2e0", "f0b080"), ascuas_dragon()),
        "isolde": atlas(marco_isolde(), relleno_isolde, hueco("060407", "140d16"), rastro("fff0f2", "e8b0bc"), petalos()),
        "sombra": atlas(marco_sombra(), relleno_sombra, hueco("030204", "0e0a12"), rastro("ffe8e8", "e8a0a0"), jirones()),
        "sigrun": atlas(marco_sigrun(), relleno_sigrun, hueco("04070c", "0e1626"), rastro("f6fbff", "c8d8ea"), copos()),
        "capataz": atlas(marco_capataz(), relleno_capataz, hueco("070505", "17100d"), rastro("fff0e6", "f0b49a"), cascajo_veta()),
        "osmund": atlas(marco_osmund(), relleno_osmund, hueco("060606", "15130f"), rastro("f6f2e8", "c8d8e6"), huesos_almas()),
    }
    for nombre, b in barras.items():
        b.save(os.path.join(GUI, f"{nombre}_barra.png"))
        if len(sys.argv) > 1:
            d = sys.argv[1]
            os.makedirs(d, exist_ok=True)
            v = Image.new("RGBA", (256, 48), (58, 66, 76, 255))
            v.paste(b.crop((0, 80, 240, 87)), (8, 19 + 4))
            v.paste(b.crop((0, 88, 170, 95)), (8, 19 + 4))
            v.paste(b.crop((0, 48, 149, 55)), (8, 19 + 4))
            v.alpha_composite(b.crop((0, 0, 256, 40)), (0, 4))
            v.resize((256 * 4, 48 * 4), Image.NEAREST).save(os.path.join(d, f"{nombre}_barra_vista.png"))
    print("ok")
