package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Las pantallas de Minecraft que no se abren nunca: en su lugar sale la del mod (parte 2e-4 y 2f).
 * <ul>
 * <li>el libro y el atril: el codice ({@link CodiceScreen});</li>
 * <li>la pausa (ESC): {@link PausaScreen} (la de F3 + ESC, que no tiene menu, se deja);</li>
 * <li>las opciones (desde el titulo de Minecraft, mientras no haya uno propio): {@link OpcionesScreen};</li>
 * <li>la lista de mods de Forge: {@link ModsScreen};</li>
 * <li>los progresos de Minecraft (la L): la pestana de logros del menu ({@link MenuScreen});</li>
 * <li>el titulo ({@link TituloScreen}) y la lista de mundos ({@link PartidasScreen}).</li>
 * </ul>
 * Las demas (graficos, controles, idioma, recursos...) siguen siendo las de Minecraft, pero con los botones, los
 * deslizadores y los fondos del mod (tools/ui/vanilla.py).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class Pantallas {
    private Pantallas() {}

    @SubscribeEvent
    public static void alAbrir(ScreenEvent.Opening e) {
        Screen nueva = e.getNewScreen();
        Screen cambiada = cambiar(nueva);
        if (cambiada != nueva) e.setNewScreen(cambiada);
    }

    static Screen cambiar(Screen s) {
        if (s == null) return null;
        Screen c = CodiceScreen.alAbrir(s);
        if (c != s) return c;
        if (s.getClass() == PauseScreen.class && conMenu((PauseScreen) s)) return new PausaScreen();
        if (s.getClass() == OptionsScreen.class) return new OpcionesScreen(anterior(s, OptionsScreen.class));
        if (s.getClass() == ModListScreen.class) return new ModsScreen(anterior(s, ModListScreen.class));
        // el titulo y la lista de mundos (parte 2f-2)
        if (s.getClass() == TitleScreen.class) return new TituloScreen();
        if (s.getClass() == SelectWorldScreen.class) return new PartidasScreen(anterior(s, SelectWorldScreen.class));
        // los progresos (la L): la pestana de logros del menu
        if (s instanceof AdvancementsScreen && Minecraft.getInstance().player != null) {
            Panel.abre();
            return new MenuScreen(MenuScreen.LOGROS);
        }
        return s;
    }

    /** La pausa de F3 + ESC no trae menu (solo congela el juego): esa se deja como esta. */
    private static boolean conMenu(PauseScreen p) {
        Minecraft mc = Minecraft.getInstance();
        if (InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_F3)) return false;
        // el unico booleano de la pausa de Minecraft es "con menu" (se busca por el tipo: los nombres cambian fuera
        // del entorno de desarrollo)
        try {
            for (Field f : PauseScreen.class.getDeclaredFields())
                if (f.getType() == boolean.class && !Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    return f.getBoolean(p);
                }
        } catch (ReflectiveOperationException | RuntimeException ignorada) {
            // si no se puede mirar, con menu (es lo normal)
        }
        return true;
    }

    /** La pantalla a la que volvia la de Minecraft (su unico campo de tipo pantalla). */
    private static Screen anterior(Screen s, Class<?> clase) {
        try {
            for (Field f : clase.getDeclaredFields())
                if (Screen.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    return (Screen) f.get(s);
                }
        } catch (ReflectiveOperationException | RuntimeException ignorada) {
            // sin pantalla anterior: se vuelve al juego (o al titulo)
        }
        return Minecraft.getInstance().screen;
    }
}
