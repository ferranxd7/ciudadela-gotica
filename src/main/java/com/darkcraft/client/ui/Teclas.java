package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Las teclas del rol (se pueden cambiar en los controles, en el apartado Dark Craft): el menu del mod, los dos frascos,
 * la cruz del equipo (cambiar de arma en cada mano, cambiar de objeto rapido y usarlo) y las del combate (rodar y fijar
 * el objetivo).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Teclas {
    private Teclas() {}

    /** Activa en el juego (sin pantallas) y sin chocar con ninguna otra tecla. */
    private static final net.minecraftforge.client.settings.IKeyConflictContext SIN_CHOQUE = new net.minecraftforge.client.settings.IKeyConflictContext() {
        @Override
        public boolean isActive() {
            return net.minecraftforge.client.settings.KeyConflictContext.IN_GAME.isActive();
        }

        @Override
        public boolean conflicts(net.minecraftforge.client.settings.IKeyConflictContext otro) {
            return false;
        }
    };

    private static KeyMapping tecla(String nombre, int codigo) {
        return new KeyMapping("key.darkcraft." + nombre, InputConstants.Type.KEYSYM, codigo, "key.categories.darkcraft");
    }

    public static final KeyMapping MENU = tecla("menu", GLFW.GLFW_KEY_R);
    public static final KeyMapping FRASCO_VIDA = tecla("frasco_vida", GLFW.GLFW_KEY_Z);
    public static final KeyMapping FRASCO_MANA = tecla("frasco_mana", GLFW.GLFW_KEY_V);
    public static final KeyMapping MANO_DERECHA = tecla("mano_derecha", GLFW.GLFW_KEY_RIGHT);
    public static final KeyMapping MANO_IZQUIERDA = tecla("mano_izquierda", GLFW.GLFW_KEY_LEFT);
    public static final KeyMapping OBJETO_SIGUIENTE = tecla("objeto_siguiente", GLFW.GLFW_KEY_DOWN);
    public static final KeyMapping OBJETO_USAR = tecla("objeto_usar", GLFW.GLFW_KEY_G);
    /** El hechizo siguiente de los memorizados (el hueco de arriba de la cruz). */
    public static final KeyMapping HECHIZO_SIGUIENTE = tecla("hechizo_siguiente", GLFW.GLFW_KEY_UP);
    /** Rodar (con una direccion) o dar un paso atras (sin ninguna). */
    public static final KeyMapping RODAR = tecla("rodar", GLFW.GLFW_KEY_LEFT_ALT);
    /**
     * La habilidad del arma de la mano derecha (la de su ceniza de guerra). Va en la C, que en el juego es "guardar la barra
     * de objetos" (solo en creativo): no choca con ella, asi que los controles no la marcan en rojo.
     */
    public static final KeyMapping HABILIDAD = new KeyMapping("key.darkcraft.habilidad", SIN_CHOQUE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C,
            "key.categories.darkcraft");
    /**
     * Fijar el objetivo (y soltarlo). Va en la Q, que en el juego es "soltar objeto" (con el mod no se tira nada con una
     * tecla): no choca con ella, asi que los controles no la marcan en rojo. La rueda del raton (pulsarla) tambien fija.
     */
    public static final KeyMapping FIJAR = new KeyMapping("key.darkcraft.fijar", SIN_CHOQUE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Q,
            "key.categories.darkcraft");

    @SubscribeEvent
    public static void registrar(RegisterKeyMappingsEvent e) {
        e.register(MENU);
        e.register(FRASCO_VIDA);
        e.register(FRASCO_MANA);
        e.register(MANO_DERECHA);
        e.register(MANO_IZQUIERDA);
        e.register(OBJETO_SIGUIENTE);
        e.register(OBJETO_USAR);
        e.register(HECHIZO_SIGUIENTE);
        e.register(RODAR);
        e.register(HABILIDAD);
        e.register(FIJAR);
    }
}
