package com.darkcraft.morvain;

/**
 * Las regiones de Morvain, las Tierras Marchitas (respuestas 89-90 del plan). Cada una con su clave (bioma
 * {@code darkcraft:morvain_<id>} y textos {@code darkcraft.morvain.region.<id>}) y un color para la vista del mapa.
 */
public enum Region {
    MAR("mar", 0x1B2A33),
    LLANURAS("llanuras", 0xB8B28A),
    BOSQUE("bosque", 0x4A4436),
    PANTANO("pantano", 0x3E4A2C),
    MONTANAS("montanas", 0xD8DCE4),
    CAPITAL("capital", 0x8A7A6A),
    ACADEMIA("academia", 0x5A6A9A),
    COSTA("costa", 0xCFC4A8),
    SIN_FE("sin_fe", 0x5A2420),
    CENTRO("centro", 0x220A0C);

    public final String id;
    public final int color;

    Region(String id, int color) {
        this.id = id;
        this.color = color;
    }
}
