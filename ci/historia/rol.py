#!/usr/bin/env python3
"""
Los textos de la interfaz nueva y del rol (el plan grande, parte 2): HUD, carteles, pantalla de muerte, Ecos de la
Sombra y espada de luz. En espanol e ingles; los demas idiomas salen en ingles hasta que se traduzcan (en tandas).

Los usa ci/historia/textos.py (claves(idioma)).
"""

ES = {
    "item.darkcraft.espada_luz": "Espada de luz",
    # carteles
    "darkcraft.cartel.muerte": "HAS MUERTO",
    "darkcraft.cartel.jefe": "JEFE ABATIDO",
    "darkcraft.cartel.zona": "Zona descubierta",
    "darkcraft.cartel.ecos": "ECOS RECUPERADOS",
    # pantalla de muerte
    "darkcraft.muerte.reaparecer": "Reaparecer",
    "darkcraft.muerte.salir": "Salir al menú",
    "darkcraft.muerte.salir.seguro": "¿Seguro? Pulsa otra vez",
    "darkcraft.muerte.pista": "Conservas todo tu equipo.",
    "darkcraft.muerte.pista.ecos": "Conservas tu equipo. Tus ecos (%s) te esperan en la espada de luz.",
    # avisos
    "darkcraft.aviso.recuperar.antes": "Pulsa",
    "darkcraft.aviso.recuperar.despues": "para recuperar tus ecos",
    # brujula
    "darkcraft.brujula.n": "N",
    "darkcraft.brujula.e": "E",
    "darkcraft.brujula.s": "S",
    "darkcraft.brujula.o": "O",
    # comandos
    "darkcraft.cmd.ecos": "Ecos de la Sombra: %s · espada de luz: %s",
    "darkcraft.ayuda.ecos": "ver tus Ecos de la Sombra, darte más o quitarlos",
    # sonidos
    "subtitles.darkcraft.sfx.has_muerto": "Has muerto",
    "subtitles.darkcraft.sfx.jefe_abatido": "Jefe abatido",
    "subtitles.darkcraft.sfx.zona_descubierta": "Zona descubierta",
    "subtitles.darkcraft.sfx.ecos_recuperados": "Ecos recuperados",
    "subtitles.darkcraft.sfx.ecos_ganados": "Ecos absorbidos",
    "subtitles.darkcraft.sfx.espada_luz": "La espada de luz resuena",
    "subtitles.darkcraft.sfx.guardia_rota": "Guardia rota",
}

EN = {
    "item.darkcraft.espada_luz": "Sword of Light",
    "darkcraft.cartel.muerte": "YOU DIED",
    "darkcraft.cartel.jefe": "GREAT FOE FELLED",
    "darkcraft.cartel.zona": "Area discovered",
    "darkcraft.cartel.ecos": "ECHOES RETRIEVED",
    "darkcraft.muerte.reaparecer": "Respawn",
    "darkcraft.muerte.salir": "Quit to title",
    "darkcraft.muerte.salir.seguro": "Sure? Click again",
    "darkcraft.muerte.pista": "You keep all your gear.",
    "darkcraft.muerte.pista.ecos": "You keep your gear. Your echoes (%s) await at the sword of light.",
    "darkcraft.aviso.recuperar.antes": "Press",
    "darkcraft.aviso.recuperar.despues": "to retrieve your echoes",
    "darkcraft.brujula.n": "N",
    "darkcraft.brujula.e": "E",
    "darkcraft.brujula.s": "S",
    "darkcraft.brujula.o": "W",
    "darkcraft.cmd.ecos": "Echoes of the Shadow: %s · sword of light: %s",
    "darkcraft.ayuda.ecos": "show your Echoes of the Shadow, give yourself more or clear them",
    "subtitles.darkcraft.sfx.has_muerto": "You died",
    "subtitles.darkcraft.sfx.jefe_abatido": "Great foe felled",
    "subtitles.darkcraft.sfx.zona_descubierta": "Area discovered",
    "subtitles.darkcraft.sfx.ecos_recuperados": "Echoes retrieved",
    "subtitles.darkcraft.sfx.ecos_ganados": "Echoes absorbed",
    "subtitles.darkcraft.sfx.espada_luz": "The sword of light hums",
    "subtitles.darkcraft.sfx.guardia_rota": "Guard broken",
}

assert set(ES) == set(EN), sorted(set(ES) ^ set(EN))


def claves(idioma):
    """Las claves de idioma de la interfaz nueva: en espanol para 'es'; en ingles para todos los demas (de momento)."""
    return dict(ES if idioma == "es" else EN)
