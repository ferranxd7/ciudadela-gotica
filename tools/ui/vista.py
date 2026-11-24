"""Vista previa de la interfaz nueva tal como la dibuja Java (mismas formulas), sobre una captura.
   python3 tools/ui/vista.py fondo.png escala carpeta"""
import sys, os, math
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from PIL import Image
import numpy as np
import arte, hud, fuente, espejo

A = hud.montar()
FS = {n: fuente.generar(n, px) for n, px in fuente.FUENTES.items()}
try:
    from PIL import ImageFont, ImageDraw
except Exception:
    pass

def T(n):
    x, y, w, h = A.sitios[n]; return A.L.recorte(x, y, w, h)

def pegar(L, sp, x, y, alfa=1.0, tinte=None):
    s = sp.copia()
    if tinte: s.a[..., :3] = np.clip(s.a[..., :3] * np.array(tinte), 0, 255)
    if alfa < 1: s.a[..., 3] = (s.a[..., 3] * alfa).astype(np.uint8)
    L.pegar(s, x, y)

def tira(L, sp, x, y, w, **k):
    i = 0
    while i < w:
        ww = min(sp.w, w - i); pegar(L, sp.recorte(0, 0, ww, sp.h), x + i, y, **k); i += ww

def tres(L, a, b, c, x, y, w, **k):
    pegar(L, T(a), x, y, **k); tira(L, T(b), x + T(a).w, y, w - T(a).w - T(c).w, **k); pegar(L, T(c), x + w - T(c).w, y, **k)

def fill(L, x0, y0, x1, y1, rgb, a=255):
    o = arte.Lienzo(max(1, x1 - x0), max(1, y1 - y0), rgb + (a,)); L.pegar(o, x0, y0)

def franja(L, x0, x1, y0, y1, rgb, alfa):
    n = 48; w = x1 - x0
    for i in range(n):
        c = (i + 0.5) / n; k = 1 - abs(c * 2 - 1); k = min(1, k * 1.9); k = k * k * (3 - 2 * k)
        a = int(round(255 * alfa * k))
        if a < 3: continue
        fill(L, x0 + w * i // n, y0, x0 + w * (i + 1) // n, y1, rgb, a)

def hexrgb(h): return ((h >> 16) & 255, (h >> 8) & 255, h & 255)

def gotica(L, n, texto, x, yArriba, color, esc=1, borde=(5, 4, 8)):
    """yArriba = borde de arriba de las mayusculas (como Letra.grande / mediana)."""
    f = FS[n]; baja = 10 if n == "gotica" else 6
    ymc = yArriba + baja * esc
    ycelda = ymc + (7 - f["ascent"]) * esc
    im = L.imagen()
    for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1), (1, 1), (0, 2)):
        fuente.escribir(im, f, texto, x + dx * esc, ycelda + dy * esc, borde + (255,), esc)
    fuente.escribir(im, f, texto, x, ycelda, color + (255,), esc)
    L.a[:] = np.asarray(im)

def anchoG(n, texto, esc=1): return fuente.ancho(FS[n], texto, esc)

_mc = None
def mc(L, texto, x, y, color, sombra=True):
    """Letra del juego aproximada (una fuente de pixeles de 8 px)."""
    global _mc
    from PIL import ImageDraw, ImageFont
    im = L.imagen(); d = ImageDraw.Draw(im); d.fontmode = "1"
    f = ImageFont.load_default()
    if sombra: d.text((x + 1, y - 1), texto, fill=(color[0] // 4, color[1] // 4, color[2] // 4, 255), font=f)
    d.text((x, y - 2), texto, fill=color + (255,), font=f)
    L.a[:] = np.asarray(im)

def anchoMc(texto):
    from PIL import ImageFont
    return int(ImageFont.load_default().getlength(texto))

def barra(L, x, y, largo, f, fr, rell, tinte=None):
    wf = int(round(largo * max(0, min(1, f)))); wr = int(round(largo * max(0, min(1, fr))))
    tira(L, T("barra_fondo"), x + 2, y + 2, largo + 1)
    anchos = [3, 4, 5, 4, 3]
    fo = T("barra_fondo")
    for i in range(5): L.pegar(fo.recorte(0, i, anchos[i], 1), x + 3 + largo, y + 2 + i)
    if wr > wf: tira(L, T("rastro"), x + 2 + wf, y + 2, wr - wf + 1)
    if wf > 0:
        tira(L, T(rell), x + 2, y + 2, wf + 1, tinte=tinte)
        if wf >= largo:
            r = T(rell)
            for i in range(5): L.pegar(r.recorte(0, i, anchos[i], 1), x + 3 + largo, y + 2 + i)
    pegar(L, T("barra_izq"), x, y); tira(L, T("barra_med"), x + 3, y, largo); pegar(L, T("barra_punta"), x + 3 + largo, y)

def hud_normal(W, H, vida=0.62, rastro=0.8, mana=0.9, est=0.45, ecos="12.480", mas="+120", aviso=True, espejo_r=36, brujula=True):
    L = arte.Lienzo(W, H)
    X0, Y0 = 3, 3; x = X0 + 22
    barra(L, x, Y0, 120, vida, rastro, "vida"); barra(L, x, Y0 + 8, 76, mana, 0, "mana"); barra(L, x, Y0 + 16, 96, est, 0, "estamina")
    pegar(L, T("emblema"), X0, Y0)
    # ecos
    tw = anchoG("gotica_m", ecos); n = max(2, (tw + 6 + 17) // 18); wpl = 24 + 18 * n + 16; px, py = W - 4 - wpl, H - 4 - 22
    pegar(L, T("placa_izq"), px, py); tira(L, T("placa_med"), px + 24, py, 18 * n); pegar(L, T("placa_der"), px + 24 + 18 * n, py)
    pegar(L, T("eco_0"), px + 3, py + 3)
    gotica(L, "gotica_m", ecos, px + 24 + 18 * n + 2 - tw, py + 5, hexrgb(0xE2BF62))
    if mas:
        mw = anchoG("gotica_m", mas); gotica(L, "gotica_m", mas, px + 24 + 18 * n + 2 - mw, py - 20, hexrgb(0xFFF2C4))
    # espejo redondo
    lado = 2 * (espejo_r + 8) + 1
    if espejo_r:
        m, cr = espejo.marco(espejo_r), espejo.cristal(espejo_r)
        ex0, ey0 = W - lado - 3, 2
        c = espejo_r + 8
        for yy in range(m.h):
            for xx in range(m.w):
                if math.hypot(xx - c, yy - (c + 7)) <= espejo_r + 0.5:
                    v = (xx * 7 + yy * 13) % 23
                    L.px(ex0 + xx, ey0 + yy, (30 + v, 22 + v // 2, 26 + v // 2, 255))
        L.pegar(cr, ex0, ey0); L.pegar(m, ex0, ey0)
        nombre = "Torre de Ascenso"; w1 = anchoMc(nombre); w = w1 + 10
        tx = min(ex0 + c - w // 2, W - 2 - w); ty = ey0 + 7 + lado + 1
        fill(L, tx, ty, tx + w, ty + 12, (10, 4, 6), 0x9a); fill(L, tx, ty + 12, tx + w, ty + 13, hexrgb(0x96702A), 0xc0)
        mc(L, nombre, tx + 5, ty + 2, hexrgb(0xEADBC4))
    # brujula
    if brujula:
        fin = x + 3 + 120 + 8
        libre = W // 2 - max(fin, (lado + 6 + 8) if espejo_r else 0); bw = min(180, libre * 2)
        if bw >= 84:
            bx = (W - bw) // 2; y = 3; cx = W // 2
            tres(L, "brujula_izq", "brujula_med", "brujula_der", bx, y, bw)
            yaw = 150.0; medio = bw / 2
            for a in range(0, 360, 15):
                if a % 90 == 0: continue
                d = (a - yaw + 180) % 360 - 180
                if abs(d) > medio - 6: continue
                al = int(200 * max(0, min(1, (medio - abs(d)) / 22)))
                rx = cx + int(round(d)); fill(L, rx, y + (4 if a % 45 == 0 else 6), rx + 1, y + 9, hexrgb(0xC0963C if a % 45 == 0 else 0x96702A), al)
            for r, s in ((180, "N"), (-90, "E"), (0, "S"), (90, "O")):
                d = (r - yaw + 180) % 360 - 180
                if abs(d) > medio - 8: continue
                mc(L, s, cx + int(round(d)) - anchoMc(s) // 2, y + 3, hexrgb(0xE8302A if s == "N" else 0xE2BF62))
            pegar(L, T("marca_objetivo"), cx + 40 - 3, y + 3); pegar(L, T("marca_espada"), cx - 62 - 3, y + 1)
            pegar(L, T("brujula_aguja"), cx - 4, y - 2)
    if aviso:
        antes, kt, despues, num = "Pulsa", "E", "para recuperar tus ecos", "12.480"
        wa = anchoMc(antes) + 5; wk = max(13, anchoMc(kt) + 8); wd = anchoMc(despues) + 5; wn = anchoMc(num) + 4 + 16
        ancho = wa + wk + wd + wn + 28; ax = (W - ancho) // 2; ay = H - 80
        tres(L, "aviso_izq", "aviso_med", "aviso_der", ax, ay, ancho)
        cx = ax + 14; mc(L, antes, cx, ay + 6, hexrgb(0xEADBC4)); cx += wa
        tres(L, "tecla_izq", "tecla_med", "tecla_der", cx, ay + 3, wk); mc(L, kt, cx + (wk - anchoMc(kt)) // 2 + 1, ay + 5, hexrgb(0xF6E3A0), False); cx += wk + 5
        mc(L, despues, cx, ay + 6, hexrgb(0xEADBC4)); cx += wd
        pegar(L, T("eco_0"), cx - 2, ay + 1); mc(L, num, cx + 15, ay + 6, hexrgb(0xE2BF62))
    return L

def cartel(L, W, H, tipo, titulo, sub):
    muerte, zona, ecos = tipo == 0, tipo == 2, tipo == 3
    tw1 = anchoG("gotica", titulo); esc = 2 if (not ecos and tw1 * 2 + 200 <= W) else 1
    tw = tw1 * esc; alto = 17 * esc
    cy = int(round(H * (0.40 if muerte else 0.24 if zona else 0.30))); arriba = cy - alto // 2
    y0 = arriba - (20 if zona else 12); y1 = arriba + alto + (26 if (sub and not zona) else 14)
    franja(L, 0, W, y0, y1, hexrgb(0x0A0204 if muerte else 0x050408), 0.84 if muerte else 0.72)
    oro = hexrgb(0x8A121B if muerte else 0x96702A); osc = hexrgb(0x3A070C if muerte else 0x4A3312)
    franja(L, W // 8, W - W // 8, y0, y0 + 1, oro, 0.9); franja(L, W // 8, W - W // 8, y1 - 1, y1, oro, 0.9)
    franja(L, W // 5, W - W // 5, y0 + 2, y0 + 3, osc, 0.8); franja(L, W // 5, W - W // 5, y1 - 3, y1 - 2, osc, 0.8)
    color = hexrgb(0xC01A22 if muerte else 0xF4F0E0 if zona else 0xE9C766)
    fy = cy - 5
    if tw + 2 * 82 <= W:
        tinte = (1, 0.45, 0.4) if muerte else None
        pegar(L, T("floritura"), W // 2 - tw // 2 - 10 - 72, fy, tinte=tinte); pegar(L, T("floritura").espejo_h(), W // 2 + tw // 2 + 10, fy, tinte=tinte)
    gotica(L, "gotica", titulo, W // 2 - tw // 2, arriba, color, esc)
    if sub:
        if zona: mc(L, sub, W // 2 - anchoMc(sub) // 2, y0 + 7, hexrgb(0xC0963C))
        else:
            sw = anchoG("gotica_m", sub); gotica(L, "gotica_m", sub, W // 2 - sw // 2, arriba + alto + 7, hexrgb(0xFFF2C4 if ecos else 0xEADBC4))

def muerte(W, H):
    L = arte.Lienzo(W, H)
    for y in range(H):
        k = y / H; a = int(150 + (205 - 150) * k)
        fill(L, 0, y, W, y + 1, (int(0x12 + (0x06 - 0x12) * k), 2, 4), a)
    cartel(L, W, H, 0, "HAS MUERTO", None)
    bx = (W - 150) // 2
    for i, (txt, luz) in enumerate((("Reaparecer", True), ("Salir al menu", False))):
        by = int(round(H * 0.40)) + 58 + i * 26
        if luz: tres(L, "boton_luz_izq", "boton_luz_med", "boton_luz_der", bx, by, 150)
        else: tres(L, "boton_izq", "boton_med", "boton_der", bx, by, 150)
        mc(L, txt, bx + (150 - anchoMc(txt)) // 2, by + 6, hexrgb(0xFFF2C4 if luz else 0xE2BF62))
    p = "Conservas tu equipo. Tus ecos (12.480) te esperan en la espada de luz."
    mc(L, p, (W - anchoMc(p)) // 2, int(round(H * 0.40)) + 58 + 26 + 30, hexrgb(0xB8A888), False)
    return L

def cine(W, H):
    L = arte.Lienzo(W, H); bh = int(round(H * 0.12))
    b = T("banda")
    for (y0) in (0, H - bh):
        for j in range(0, bh, b.h):
            for i in range(0, W, b.w): L.pegar(b.recorte(0, 0, min(b.w, W - i), min(b.h, bh - j)), i, y0 + j)
    yc = bh - 7; tira(L, T("cenefa"), 0, yc, W); tira(L, T("cenefa").espejo_v(), 0, H - bh - 2, W)
    pegar(L, T("cine_centro"), W // 2 - 32, yc - 2)
    return L

if __name__ == "__main__":
    fondo, escala, out = sys.argv[1], int(sys.argv[2]), sys.argv[3]
    os.makedirs(out, exist_ok=True)
    bg0 = Image.open(fondo).convert("RGBA")
    W, H = bg0.width // escala, bg0.height // escala
    def guardar(L, nombre):
        bg = bg0.copy(); bg.alpha_composite(L.imagen().resize((W * escala, H * escala), Image.NEAREST), (0, 0)); bg.save(os.path.join(out, nombre)); print(nombre)
    guardar(hud_normal(W, H), "v_hud.png")
    L = hud_normal(W, H, aviso=False, mas=None); cartel(L, W, H, 1, "JEFE ABATIDO", "El Rey que Espera"); guardar(L, "v_jefe.png")
    L = hud_normal(W, H, aviso=False, mas=None); cartel(L, W, H, 2, "Reino de Valdrenn", "Zona descubierta"); guardar(L, "v_zona.png")
    L = hud_normal(W, H, aviso=False, mas="+12.480"); cartel(L, W, H, 3, "ECOS RECUPERADOS", "+12.480"); guardar(L, "v_ecos.png")
    guardar(muerte(W, H), "v_muerte.png")
    guardar(cine(W, H), "v_cine.png")
