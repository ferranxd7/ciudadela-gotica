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
                        .then(com.darkcraft.caballeros.Caballeros.comando())
                        .then(com.darkcraft.valdrenn.ValdrennEnemigos.comando())
                        .then(com.darkcraft.valdrenn.Guardianes.comando()))
                .then(com.darkcraft.fortaleza.ComandosFortaleza.comando())
                .then(com.darkcraft.camino.Paradas.comando())
                .then(com.darkcraft.pueblo.Pueblo.comando())
                .then(com.darkcraft.montura.Montura.comando())
                .then(com.darkcraft.morvain.Viaje.comando())
                .then(com.darkcraft.morvain.Fragmentos.comando())
                .then(com.darkcraft.morvain.MorvainEnemigos.comando())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoMolinera())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoArboleda())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoVerdugo())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoPilotes())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoLaguna())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoBarquero())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoMonasterio())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoErmita())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoAbad())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoCapital())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoCondestable())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoRectora())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoAcademia())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoPuerto())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoGrieta())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoCerco())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoBallenero())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoPontifice())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoAldric())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoTeatro())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoMina())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoCapataz())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoBufon())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoNido())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoCadena())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoDragon())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoHomenaje())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoIsolde())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoAguja())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoSombra())
                .then(com.darkcraft.morvain.MorvainEnemigos.comandoFinal())
                .then(com.darkcraft.morvain.CronicaMorvain.comando())
                .then(com.darkcraft.morvain.Herrerias.comando())
                .then(com.darkcraft.morvain.Ambulante.comando())
                .then(com.darkcraft.mision.Misiones.comando())
                .then(com.darkcraft.mision.Misiones.comandoOswin())
                .then(com.darkcraft.mision.Misiones.comandoHedda())
                .then(com.darkcraft.mision.Misiones.comandoYlva())
                .then(com.darkcraft.mision.Misiones.comandoCorwen())
                .then(com.darkcraft.mision.Misiones.comandoSigrun())
                .then(com.darkcraft.armadura.Armaduras.comando())
                .then(com.darkcraft.armas.Arsenal.comando())
                .then(com.darkcraft.morvain.Gigantes.comando())
                .then(com.darkcraft.camino.Jinetes.comando())
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
            new Linea(V + "enemigos", V + "enemigos [volver | fuera | sacar | ir <grupo>]", "v_enemigos"),
            new Linea(V + "guardian ", V + "guardian <brandt | vareth> [ir | vencer | volver]", "v_guardian"),
    };

    private static final String F = "/darkcraft fortaleza ";

    private static final Linea[] FORTALEZA = {
            new Linea(F + "localizar", F + "localizar", "f_localizar"),
            new Linea(F + "ir", F + "ir / montana / mirador", "f_ir"),
            new Linea(F + "puente", F + "puente / abrir / portal", "f_puente"),
            new Linea(F + "amuleto", F + "amuleto / acertijo / libros", "f_objetos"),
            new Linea(F + "secreto", F + "secreto / muro / cripta / trampas", "f_secretos"),
            new Linea(F + "enemigos", F + "enemigos [volver | fuera | sacar]", "f_enemigos"),
            new Linea(F + "construir", F + "construir / reiniciar", "f_reiniciar"),
    };

    private static int ayuda(CommandSourceStack src) {
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.titulo").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.valdrenn").withStyle(ChatFormatting.GOLD), false);
        for (Linea l : VALDRENN) src.sendSuccess(() -> linea(l), false);
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.fortaleza").withStyle(ChatFormatting.GOLD), false);
        for (Linea l : FORTALEZA) src.sendSuccess(() -> linea(l), false);
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.camino").withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft camino", "/darkcraft camino / ir <0-5> / levantar <0-5> / enemigos volver|fuera", "c_paradas")), false);
        src.sendSuccess(() -> Component.translatable("darkcraft.ayuda.brumavela").withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft brumavela ir ", "/darkcraft brumavela ir [<sitio>]", "b_ir")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft brumavela pieza ", "/darkcraft brumavela pieza <nombre>|todas / cripta / reparar [ya]", "b_piezas")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft brumavela zarpar", "/darkcraft brumavela zarpar [primero] / llamar / muelle / barco <brumavela|valdrenn>", "b_barco")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft brumavela estado", "/darkcraft brumavela estado / reiniciar", "b_estado")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft brumavela enemigos", "/darkcraft brumavela enemigos [volver | fuera]", "b_enemigos")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft brumavela capitan", "/darkcraft brumavela capitan [vencer | volver]", "b_capitan")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft brumavela inicio", "/darkcraft brumavela inicio", "b_inicio")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft corcel ", "/darkcraft corcel [llamar | silbato | deshacer | herir <n> | espera]", "corcel")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft viaje ", "/darkcraft viaje [lista | conocer <id>|todos | olvidar | ir <id>]", "viaje")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft mapa ", "/darkcraft mapa [lista | dar <region>|todos | quitar | ir <region>]", "mapa")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft velmor ", "/darkcraft velmor [ir <sitio> | enemigos [volver | fuera | sacar]]", "velmor")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft molinera ", "/darkcraft molinera [ir | vencer | volver]", "molinera")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft arboleda ", "/darkcraft arboleda [ir <sitio> | enemigos [volver | fuera | sacar]]", "arboleda")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft verdugo ", "/darkcraft verdugo [ir | vencer | volver]", "verdugo")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft pilotes ", "/darkcraft pilotes [ir <sitio> | enemigos [volver | fuera | sacar]]", "pilotes")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft laguna ", "/darkcraft laguna [ir <sitio> | enemigos [volver | fuera | sacar]]", "laguna")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft barquero ", "/darkcraft barquero [ir | vencer | volver]", "barquero")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft monasterio ", "/darkcraft monasterio [ir <sitio> | enemigos [volver | fuera | sacar]]", "monasterio")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft ermita ", "/darkcraft ermita [ir <sitio> | enemigos [volver | fuera | sacar]]", "ermita")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft abad ", "/darkcraft abad [ir | vencer | volver]", "abad")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft capital ", "/darkcraft capital [ir <sitio> | enemigos [volver | fuera | sacar]]", "capital")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft condestable ", "/darkcraft condestable [ir | vencer | volver | patio <sitio> | rastrillo [subir]]",
                "condestable")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft rectora ", "/darkcraft rectora [ir | vencer | volver | observatorio <sitio> | sello [abrir]]",
                "rectora")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft academia ", "/darkcraft academia [ir <sitio> | enemigos [volver | fuera | sacar]]", "academia")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft puerto ", "/darkcraft puerto [ir <sitio> | enemigos [volver | fuera | sacar]]", "puerto")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft grieta ", "/darkcraft grieta [ir <sitio> | campana | enemigos [volver | fuera | sacar]]", "grieta")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft cerco ", "/darkcraft cerco [ir <sitio> | enemigos [volver | fuera | sacar]]", "cerco")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft ballenero ", "/darkcraft ballenero [ir | vencer | volver | costillar <sitio>]", "ballenero")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft pontifice ", "/darkcraft pontifice [ir | vencer | volver | altar <sitio>]", "pontifice")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft aldric ", "/darkcraft aldric [ir | vencer | volver | puerta [abrir]]", "aldric")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft gigante ", "/darkcraft gigante [<0-2> [ir | aqui | vencer | derribar | volver]]", "gigante")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft jinete ", "/darkcraft jinete [ir | aqui | vencer | derribar | volver | noche | dia]", "jinete")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft teatro ", "/darkcraft teatro [ir <sitio> | enemigos [volver | fuera | sacar] | muro <puerta | trono> [abrir | cerrar]]", "teatro")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft mina ", "/darkcraft mina [ir <sitio> | enemigos [volver | fuera | sacar] | derrumbe [<n>] | escotilla [abrir | cerrar] | muro [abrir | cerrar]]", "mina")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft capataz ", "/darkcraft capataz [ir | vencer | volver | media | accion <n> | suelta]", "capataz")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft bufon ", "/darkcraft bufon [ir | vencer | volver | partir]", "bufon")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft nido ", "/darkcraft nido [ir <sitio> | enemigos [volver | fuera | sacar]]", "nido")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft cadena ", "/darkcraft cadena [ir <sitio> | enemigos [volver | fuera | sacar] | rafaga | sombra olvidar | gargola [vuelo | tierra | picado | garras | posarse]]", "cadena")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft dragon ", "/darkcraft dragon [ir | vencer | volver | media | despegar | rasante | bolas | picado | ascuas | bajar | derribar]", "dragon")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft homenaje ", "/darkcraft homenaje [ir <sitio> | enemigos [volver | fuera | sacar] | rastrillo [abrir | cerrar] | sombra olvidar | centinela [despertar | tajo | golpe | humo | estocada | barrido | hachazo]]", "homenaje")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft isolde ", "/darkcraft isolde [ir | vencer | volver | media | garras | velo | abrazo | nana | desvanece | lagrimas | corte]", "isolde")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft aguja ", "/darkcraft aguja [ir | abrir | cerrar | olvidar]", "aguja")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft sombra ", "/darkcraft sombra [ir | vencer | volver | media | golpear | tomar | zarpazos | giro | cadena | manos | lanzas | niebla | mirada | grito | caidos]", "sombra")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft final ", "/darkcraft final [alba | sombra | creditos | epilogo | olvidar | deshacer]", "final")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft cronica ", "/darkcraft cronica [ir <7-10> | poner | olvidar]", "cronica")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft herreros ", "/darkcraft herreros [ir <hagen|brenna|ulrik|tobin> | poner | olvidar]", "herreros")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft silas ", "/darkcraft silas [ir | mover <0-5>]", "silas")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft misiones ", "/darkcraft misiones [olvidar | marcar <n> | hedda <n> | ylva <n> | corwen <n> | sigrun <n>]", "misiones")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft oswin ", "/darkcraft oswin [ir | campana | poner | olvidar]", "oswin")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft hedda ", "/darkcraft hedda [ir | tam | poner | olvidar]", "hedda")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft ylva ", "/darkcraft ylva [ir | farol | poner | olvidar]", "ylva")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft corwen ", "/darkcraft corwen [ir | baliza <0-2> | poner | olvidar]", "corwen")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft sigrun ", "/darkcraft sigrun [ir | poner | olvidar | duelo | media | rendir | rematar | soltar | volver | accion <n>]", "sigrun")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft armadura ", "/darkcraft armadura [<conjunto> [poner] | quitar]", "armadura")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft armadura jefes", "/darkcraft armadura jefes [olvidar]", "armadura_jefes")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft armadura mejora ", "/darkcraft armadura mejora <0-5>", "armadura_mejora")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft armas ", "/darkcraft armas [martillos | flechas | sangre <n> | frio <n> | limpiar]", "armas")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft ecos ", "/darkcraft ecos / ecos dar <n> / ecos quitar", "ecos")), false);
        src.sendSuccess(() -> linea(new Linea("/darkcraft rol ", "/darkcraft rol clase [<nombre> | elegir | olvidar | ninguna] / santuario / frascos [<n>] / nivel [reiniciar] / atributo <nombre> <valor> / talisman <nombre> [poner <hueco>] / fruto <nombre> [<n>] / hechizo <nombre>|memorizar|olvidar|baston|sello / guia [olvidar] / herrero [olvidar] / mercader [olvidar] / mejora <0-9> / piedras / ceniza <nombre> [poner] | quitar / resistencias", "rol")), false);
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
