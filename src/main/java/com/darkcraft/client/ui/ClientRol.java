package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lo que sabe el cliente del rol del jugador (se lo manda el servidor): los Ecos de la Sombra, la espada de luz, la
 * estamina, si esta en combate y su ficha (atributos, mana maximo y frascos). Y lo que hace con ello: junto a la espada
 * o a un santuario, la tecla del inventario hace lo que dice el aviso en vez de abrir el inventario; las teclas de los
 * frascos piden beber; agotado, no deja saltar ni atacar; y los ecos de un enemigo caido vuelan hasta el jugador.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientRol {
    private ClientRol() {}

    // ------------------------------------------------------------------ estado

    public static int ecos;
    /** Lo que ensena el contador (va contando hasta {@link #ecos}). */
    public static float ecosVistos;
    /** El "+120" de encima del contador: cuanto y desde cuando. */
    public static int ganado, ganadoTick = -1000;
    /** El contador da un destello (acaban de llegar ecos). */
    public static int destelloTick = -1000;

    public static float estamina = 100f, estaminaMax = 100f;
    public static boolean agotado, combate;
    /** Momento del ultimo intento de hacer algo sin estamina (la barra parpadea). */
    public static int sinEstaminaTick = -1000;

    public static boolean hayEspada;
    public static String espadaDim = "";
    public static double ex, ey, ez;
    public static int ecosEspada;

    /** La ficha: los ocho atributos (en el orden de {@code Atributo}), el mana maximo y los frascos. */
    public static int[] atributos = com.darkcraft.rol.Atributo.deBase();
    public static int manaMax = 100;
    public static int frascosVida = 3, frascosMana = 1, quedanVida = 3, quedanMana = 1, mejoraFrascos;
    /** Cada vez que llega la ficha sube (las pantallas abiertas se enteran de que ha cambiado). */
    public static int fichaVersion;

    public static int ticks;
    private static int enfriamiento, enfriamientoFrasco;
    private static boolean primero = true;

    public static int nivel() {
        return com.darkcraft.rol.Nivel.de(atributos);
    }

    /** Los hechizos memorizados y el que esta elegido (la flecha arriba los recorre). */
    public static java.util.List<com.darkcraft.hechizos.Hechizo> memoria = new java.util.ArrayList<>();
    public static int hechizoElegido;
    public static int hechizoCambioTick = -1000;

    /** El hechizo elegido (o null si no hay ninguno memorizado). */
    public static com.darkcraft.hechizos.Hechizo hechizo() {
        if (memoria.isEmpty()) return null;
        return memoria.get(Math.floorMod(hechizoElegido, memoria.size()));
    }

    public static void siguienteHechizo() {
        if (memoria.size() < 2) {
            Panel.nada();
            return;
        }
        hechizoElegido = Math.floorMod(hechizoElegido + 1, memoria.size());
        hechizoCambioTick = ticks;
        Panel.mueve();
    }

    /** Los talismanes que lleva (los cuatro huecos) y cuantos huecos tiene abiertos. */
    public static net.minecraft.world.item.ItemStack[] talismanes = vacios();
    public static int huecosTalisman = 1;

    private static net.minecraft.world.item.ItemStack[] vacios() {
        net.minecraft.world.item.ItemStack[] r = new net.minecraft.world.item.ItemStack[com.darkcraft.rol.Talismanes.MAX];
        java.util.Arrays.fill(r, net.minecraft.world.item.ItemStack.EMPTY);
        return r;
    }

    public static boolean lleva(com.darkcraft.rol.Talisman.Clase c) {
        return com.darkcraft.rol.Talismanes.lleva(talismanes, c);
    }

    /** Lo que puede llevar el jugador sin pasar a sobrecargado (como lo cuenta el servidor). */
    public static float pesoMaximo() {
        return com.darkcraft.combate.Peso.maximo(atributos, lleva(com.darkcraft.rol.Talisman.Clase.PORTEADOR));
    }

    public static void ficha(Net.FichaRol m) {
        if (m.atributos().length == com.darkcraft.rol.Atributo.N) atributos = m.atributos().clone();
        huecosTalisman = m.huecosTalisman();
        net.minecraft.world.item.ItemStack[] t = vacios();
        for (int i = 0; i < Math.min(t.length, m.talismanes().length); i++) {
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(m.talismanes()[i]);
            net.minecraft.world.item.Item it = id == null || m.talismanes()[i].isEmpty() ? null : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id);
            if (it != null && it != net.minecraft.world.item.Items.AIR) t[i] = new net.minecraft.world.item.ItemStack(it);
        }
        talismanes = t;
        java.util.List<com.darkcraft.hechizos.Hechizo> mem = new java.util.ArrayList<>();
        for (String id : m.memoria()) {
            com.darkcraft.hechizos.Hechizo h = com.darkcraft.hechizos.Hechizo.de(id);
            if (h != null) mem.add(h);
        }
        memoria = mem;
        if (hechizoElegido >= mem.size()) hechizoElegido = 0;
        manaMax = Math.max(1, m.manaMax());
        frascosVida = m.frascosVida();
        frascosMana = m.frascosMana();
        quedanVida = m.vida();
        quedanMana = m.mana();
        mejoraFrascos = m.mejora();
        fichaVersion++;
    }

    public static void estado(Net.RolEstado m) {
        if (m.ganados() > 0 && !primero) {
            // si hay ecos volando hacia el jugador, el "+N" sale cuando llegan; si no (un comando, la espada), ya
            if (MOTAS.isEmpty()) llegan(m.ganados());
            else pendiente += m.ganados();
        }
        ecos = m.ecos();
        if (primero) ecosVistos = ecos;
        primero = false;
        combate = m.combate();
        hayEspada = m.espada();
        espadaDim = m.dim();
        ex = m.x();
        ey = m.y();
        ez = m.z();
        ecosEspada = m.ecosEspada();
    }

    public static void estamina(Net.Estamina m) {
        estamina = m.decimas() / 10f;
        estaminaMax = Math.max(1f, m.max() / 10f);
        agotado = m.agotado();
    }

    private static int pendiente;

    private static void llegan(int n) {
        if (n <= 0) return;
        ganado = ticks - ganadoTick < 50 ? ganado + n : n;
        ganadoTick = ticks;
        destelloTick = ticks;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_ECOS_GANADOS.get(),
                0.9f + (float) Math.random() * 0.2f, 0.6f));
    }

    /** Si el jugador esta junto a su espada de luz (se le ofrece recogerla). */
    public static boolean juntoAEspada() {
        Minecraft mc = Minecraft.getInstance();
        if (!hayEspada || mc.player == null || mc.level == null || !mc.player.isAlive()) return false;
        if (!mc.level.dimension().location().toString().equals(espadaDim)) return false;
        return mc.player.distanceToSqr(ex, ey, ez) < 3.2 * 3.2;
    }

    // ------------------------------------------------------------------ los ecos que vuelan

    private static final class Mota {
        Vec3 p, v;
        int edad;
    }

    private static final List<Mota> MOTAS = new ArrayList<>();
    private static final DustColorTransitionOptions ROJO = new DustColorTransitionOptions(new Vector3f(1.0f, 0.30f, 0.22f),
            new Vector3f(0.45f, 0.03f, 0.06f), 0.85f);

    public static void vuelan(Net.Ecos m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        int n = Mth.clamp(4 + m.n() / 40, 5, 16);
        for (int i = 0; i < n; i++) {
            Mota o = new Mota();
            o.p = new Vec3(m.x(), m.y(), m.z());
            double a = mc.level.random.nextDouble() * Math.PI * 2;
            o.v = new Vec3(Math.cos(a) * 0.16, 0.14 + mc.level.random.nextDouble() * 0.16, Math.sin(a) * 0.16);
            o.edad = -i;
            MOTAS.add(o);
        }
    }

    private static void motas(Minecraft mc) {
        if (MOTAS.isEmpty()) return;
        Vec3 pecho = mc.player.position().add(0, mc.player.getBbHeight() * 0.6, 0);
        boolean llego = false;
        for (Iterator<Mota> it = MOTAS.iterator(); it.hasNext(); ) {
            Mota o = it.next();
            if (++o.edad <= 0) continue;
            Vec3 a = pecho.subtract(o.p);
            double d = a.length();
            if (d < 0.7 || o.edad > 70) {
                it.remove();
                llego = true;
                continue;
            }
            // primero se abren hacia fuera y luego giran hacia el jugador cada vez con mas ganas
            double tirar = Mth.clamp((o.edad - 5) / 12.0, 0.0, 1.0);
            double rapidez = 0.22 + o.edad * 0.02;
            Vec3 quiere = a.scale(rapidez / d);
            o.v = o.v.scale(1 - 0.28 * tirar).add(quiere.scale(0.28 * tirar + 0.02));
            o.p = o.p.add(o.v);
            mc.level.addParticle(ROJO, o.p.x, o.p.y, o.p.z, 0, 0, 0);
        }
        if (llego && MOTAS.isEmpty() && pendiente > 0) {
            llegan(pendiente);
            pendiente = 0;
        }
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (e.phase == TickEvent.Phase.START) {
            // con un aviso de "Pulsa [E]..." delante (la espada de luz, un santuario), la tecla del inventario hace eso
            // (y no abre el inventario)
            int que = mc.player != null && mc.screen == null && !ClientCine.active() && mc.player.isAlive() ? aMano() : NADA;
            if (que != NADA) {
                boolean pulsada = false;
                while (mc.options.keyInventory.consumeClick()) pulsada = true;
                if (pulsada && enfriamiento <= 0) {
                    if (que == ESPADA) Net.CH.sendToServer(new Net.Recuperar());
                    else if (que == SANTUARIO && ClientSantuario.aMano != null) Net.CH.sendToServer(new Net.SantuarioUsar(ClientSantuario.aMano));
                    else if (que == PERSONAJE) Net.CH.sendToServer(new Net.Hablar(com.darkcraft.client.personajes.ClientPersonajes.aMano));
                    enfriamiento = 12;
                }
            }
            return;
        }
        if (mc.player == null || mc.level == null || mc.isPaused()) return;
        ticks++;
        if (enfriamiento > 0) enfriamiento--;
        if (enfriamientoFrasco > 0) enfriamientoFrasco--;
        frascos(mc);
        ClientSantuario.tick(mc);
        com.darkcraft.client.personajes.ClientPersonajes.tick(mc);
        Bebida.tick(mc);
        Conseguidos.tick();
        // el contador cuenta hacia lo que hay (lo que aun viene volando no cuenta hasta que llega)
        float hay = Math.max(0, ecos - pendiente), falta = hay - ecosVistos;
        if (Math.abs(falta) < 1f) ecosVistos = hay;
        else ecosVistos += Math.signum(falta) * Math.max(1f, Math.abs(falta) * 0.14f);
        motas(mc);
        if (pendiente > 0 && MOTAS.isEmpty()) {
            llegan(pendiente);
            pendiente = 0;
        }
        Hud.tick(mc);
        Carteles.tick(mc);
    }

    /** Lo que el jugador tiene a mano para la tecla del aviso. */
    public static final int NADA = 0, ESPADA = 1, SANTUARIO = 2, PERSONAJE = 3;

    public static int aMano() {
        if (juntoAEspada()) return ESPADA;
        if (ClientSantuario.aMano != null) return SANTUARIO;
        return com.darkcraft.client.personajes.ClientPersonajes.aMano >= 0 ? PERSONAJE : NADA;
    }

    /** Las teclas de los frascos: se pide beber (el servidor dice si se puede). */
    private static void frascos(Minecraft mc) {
        int pide = -1;
        while (Teclas.FRASCO_VIDA.consumeClick()) pide = com.darkcraft.rol.Frascos.VIDA;
        while (Teclas.FRASCO_MANA.consumeClick()) pide = com.darkcraft.rol.Frascos.MANA;
        if (pide >= 0) pedirFrasco(pide);
    }

    /** Pide beber de un frasco (0 vida, 1 mana), si se puede pedir ahora. */
    public static void pedirFrasco(int tipo) {
        Minecraft mc = Minecraft.getInstance();
        if (enfriamientoFrasco > 0 || mc.player == null || mc.screen != null || ClientCine.active() || !mc.player.isAlive() || mc.player.isSpectator()) return;
        if (Bebida.yo()) return;
        enfriamientoFrasco = 6;
        Net.CH.sendToServer(new Net.Frasco(tipo));
    }

    // ------------------------------------------------------------------ agotado: ni saltar ni atacar

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent e) {
        if (!agotado || !combate) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) return;
        Input in = e.getInput();
        if (in.jumping && mc.player.onGround()) {
            in.jumping = false;
            sinEstaminaTick = ticks;
        }
    }

    @SubscribeEvent
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered e) {
        if (!e.isAttack() || !agotado) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) return;
        // sin estamina no se ataca (romper bloques si se puede: eso no es un golpe)
        if (mc.hitResult != null && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) return;
        e.setCanceled(true);
        e.setSwingHand(false);
        sinEstaminaTick = ticks;
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        ecos = 0;
        ecosVistos = 0;
        ganado = 0;
        ganadoTick = destelloTick = sinEstaminaTick = -1000;
        estamina = estaminaMax = 100f;
        agotado = combate = hayEspada = false;
        pendiente = 0;
        primero = true;
        MOTAS.clear();
        Carteles.olvidar();
        atributos = com.darkcraft.rol.Atributo.deBase();
        manaMax = 100;
        frascosVida = quedanVida = 3;
        frascosMana = quedanMana = 1;
        mejoraFrascos = 0;
        talismanes = vacios();
        huecosTalisman = 1;
        memoria = new java.util.ArrayList<>();
        hechizoElegido = 0;
        ClientSantuario.olvidar();
        Bebida.olvidar();
        Conseguidos.olvidar();
    }

    /** Para la prueba de la CI. */
    public static String resumen() {
        return "ecos=" + ecos + " vistos=" + Math.round(ecosVistos) + " estamina=" + Math.round(estamina) + "/" + Math.round(estaminaMax)
                + (agotado ? " AGOTADO" : "") + (combate ? " combate" : "") + (hayEspada ? " espada(" + ecosEspada + ") en "
                + Math.round(ex) + "," + Math.round(ey) + "," + Math.round(ez) + " " + espadaDim : " sin espada") + " motas=" + MOTAS.size()
                + " | nivel=" + nivel() + " atributos=" + java.util.Arrays.toString(atributos) + " manaMax=" + manaMax
                + " frascos=" + quedanVida + "/" + frascosVida + " " + quedanMana + "/" + frascosMana + (Bebida.yo() ? " BEBIENDO" : "")
                + " santuario=" + (ClientSantuario.aMano == null ? "-" : ClientSantuario.aMano.toShortString() + (ClientSantuario.aManoEncendido ? " encendido" : " apagado"));
    }
}
