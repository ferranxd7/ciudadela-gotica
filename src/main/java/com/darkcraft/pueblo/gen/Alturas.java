package com.darkcraft.pueblo.gen;

/**
 * Lo que pisa cada columna: el bloque de arriba del terreno (o {@link Terreno#NADA} si no hay suelo). Lo usan las casas
 * para su zocalo. La isla de Brumavela es un {@link Terreno}; Velmor, en Morvain (parte 7e), pone el de Morvain.
 */
public interface Alturas {
    int alto(int x, int z);
}
