package com.darkcraft;

import com.darkcraft.arbol.TorreArbol;
import com.darkcraft.gavran.GavranEventos;
import com.darkcraft.rey.ReyEventos;
import com.darkcraft.story.StoryEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Todos los comandos del mod, ordenados por sitio: /darkcraft valdrenn ... (y los de los sitios que vengan, cada uno
 * con su nombre) y /darkcraft ayuda, que los lista con clic para escribirlos. Todos necesitan trucos.
 */
public final class Comandos {
    private Comandos() {}

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("darkcraft").requires(s -> s.hasPermission(2))
                .executes(c -> ayuda(c.getSource()))
                .then(Commands.literal("ayuda").executes(c -> ayuda(c.getSource())))
                .then(Commands.literal("valdrenn")
                        .executes(c -> ayuda(c.getSource()))
                        .then(Commands.literal("localizar").executes(c -> ModEvents.localizar(c.getSource())))
                        .then(Commands.literal("ir").executes(c -> ModEvents.ir(c.getSource())))
                        .then(StoryEvents.comandoHistoria())
                        .then(ReyEventos.comando())
                        .then(TorreArbol.comando())
                        .then(GavranEventos.comando())
                        .then(com.darkcraft.espejo.Espejo.comando())
                        .then(com.darkcraft.caballeros.Caballeros.comando()))
                .then(com.darkcraft.fortaleza.ComandosFortaleza.comando())
                .then(com.darkcraft.rol.Rol.comando())
                .then(com.darkcraft.rol.Rol.comandoRol()));
    }

    /** Cada linea de la ayuda: lo que se escribe al hacer clic, lo que se ve y la clave de su explicacion. */
    private record Linea(String escribe, String ve, String clave) {}

    private static final String V = "/darkcraft valdrenn ";

    private static final Linea[] VALDRENN = {
            new Linea(V + "localizar", V + "localizar", "localizar"),
            new Linea(V + "ir", V + "ir", "ir"),
            new Linea(V + "historia capitulo ", V + "historia capitulo <1-6>", "capitulo"),
            new Linea(V + "historia libro ", V + "historia libro <1-6>", "libro"),
            new Linea(V + "historia mapa", V + "historia mapa", "mapa"),
            new Linea(V + "historia cofre ", V + "historia cofre <1-5>", "cofre"),
            new Linea(V + "historia reiniciar", V + "historia reiniciar", "reiniciar"),
            new Linea(V + "rey ", V + "rey ir / estado / reiniciar / fase <1-3> / matar / golpefinal", "rey"),
            new Linea(V + "arbol ", V + "arbol ir / arriba / llave / cerrar", "arbol"),
            new Linea(V + "gavran ", V + "gavran ir / estado / reiniciar / fase <1-2> / matar / guardia / ataque <nombre>", "gavran"),
            new Linea(V + "espejo ", V + "espejo despertar / dormir / revelar", "espejo"),
            new Linea(V + "caballeros ", V + "caballeros grupo / quitar", "caballeros"),
    };

    private static final String F = "/darkcraft fortaleza ";

    private static final Linea[] FORTALEZA = {
            new Linea(F + "localizar", F + "localizar", "f_localizar"),
            new Linea(F + "ir", F + "ir / escalera", "f_ir"),
            new Linea(F + "puente", F + "puente / abrir / portal", "f_puente"),
            new Linea(F + "amuleto", F + "amuleto / acertijo / libros", "f_objetos"),
            new Linea(F + "construir", F + "construir / reiniciar", "f_reiniciar"),
    };

    private static int ayuda(CommandSourceStack src) {
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.titulo").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.valdrenn").withStyle(ChatFormatting.GOLD), false);
        for (Linea l : VALDRENN) src.sendSuccess(() -> linea(l), false);
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.fortaleza").withStyle(ChatFormatting.GOLD), false);
        for (Linea l : FORTALEZA) src.sendSuccess(() -> linea(l), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft ecos ", "/darkcraft ecos / ecos dar <n> / ecos quitar", "ecos")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft rol ", "/darkcraft rol santuario / frascos [<n>] / nivel [reiniciar] / atributo <nombre> <valor> / talisman <nombre> [poner <hueco>] / fruto <nombre> [<n>] / hechizo <nombre>|memorizar|olvidar|baston|sello / guia [olvidar] / herrero [olvidar] / mercader [olvidar] / mejora <0-9> / piedras / ceniza <nombre> [poner] | quitar / resistencias", "rol")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft ayuda", "/darkcraft ayuda", "ayuda")), false);
        return 1;
    }

    private static Component linea(Linea l) {
        MutableComponent cmd = Component.literal(" " + l.ve()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, l.escribe()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("darkcraft.ayuda.clic"))));
        return cmd.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable("darkcraft.ayuda." + l.clave()).withStyle(ChatFormatting.GRAY));
    }
}
