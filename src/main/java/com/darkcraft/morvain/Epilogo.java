package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Desenlace;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Guia;
import com.darkcraft.rol.Ficha;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * El final (parte 7ad): lo que pasa cuando la Sombra sin Nombre se deshace ({@link Aguja#alMorir}). A los que estan en su sala la
 * cinematica del final ({@link Desenlace}: el alba o la nueva Sombra); a todos, el cielo de Morvain segun como acabo
 * ({@code Net.Final}, lo pinta {@code client/morvain/CieloFinal}; los fuegos de los santuarios arden frios si se tomo su corazon).
 * Despues de la cinematica, los creditos en su cliente; al acabarlos ({@code Net.CreditosFin}), a la capilla de la torre del
 * homenaje, delante de Maelis junto a la Ultima Llama, que le habla: su epilogo (18-20 o 22-23; quien no vio la cinematica, desde la
 * 17 o la 21, la primera vez que hable con ella: {@code Conversaciones.inicioGuia}). Mientras mira los creditos no le pasa nada.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Epilogo {
    private Epilogo() {}

    /** La marca de la ficha: 1 vio la cinematica del final (le espera Maelis), 2 ya la oyo. */
    public static final String FICHA = "epilogo";
    public static final int VIO = 1, OYO = 2;
    /** Hasta donde de la Sombra se ve la cinematica (bloques). */
    public static final double VEN = 64;
    /** Lo que tarda Maelis en hablar al llegar a la capilla (ticks). */
    static final int ESPERA_MAELIS = 30;
    /** Lo mas que se protege a quien mira la cinematica y los creditos (por si su cliente nunca avisa): 6 minutos. */
    static final int PROTEGE = 20 * 60 * 6;

    /** Para las pruebas: cinematicas empezadas, creditos acabados, llevados a la capilla, veces que habla Maelis y epilogos oidos. */
    public static int CINEMATICAS, CREDITOS, LLEVADOS, MAELIS, EPILOGOS;

    /** Los que miran la cinematica o los creditos (ticks que quedan de proteccion). */
    private static final Map<UUID, Integer> VIENDO = new HashMap<>();
    /** Los que acaban de llegar a la capilla: ticks hasta que Maelis les hable. */
    private static final Map<UUID, Integer> MAELIS_EN = new HashMap<>();

    // ------------------------------------------------------------------ el final

    /**
     * La Sombra ha caido ({@code final_}: 1 se golpeo su corazon, 2 se tomo; ya apuntado en {@link Morvain.Datos}): el cielo de
     * Morvain cambia para todos (desde {@link Desenlace#CIELO} segundos) y los que estan a {@link #VEN} de {@code sombra} ven la
     * cinematica ({@code quien}: el que eligio; los planos de la sala van hacia el). Devuelve cuantos la ven.
     */
    public static int empezar(ServerLevel m, Vec3 sombra, int final_, ServerPlayer quien) {
        int id = Desenlace.id(final_);
        enviarATodos(m.getServer(), m.getGameTime() + Math.round(Desenlace.CIELO * 20));
        List<ServerPlayer> ven = new ArrayList<>();
        for (ServerPlayer p : m.players())
            if (p.isAlive() && !p.isSpectator() && p.position().distanceToSqr(sombra) <= VEN * VEN) ven.add(p);
        double pies = Homenaje.pisa(Homenaje.AZOTEA);
        for (ServerPlayer p : ven) {
            Vec3 q = quien != null && ven.contains(quien) ? quien.position() : p.position();
            Cinematics.playGuion(p, id, Desenlace.planos(id, sombra, q, pies), Desenlace.FIN + 0.6f, new double[]{final_});
            Ficha.dialogo(p, FICHA, VIO);
            VIENDO.put(p.getUUID(), PROTEGE);
            CINEMATICAS++;
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] El final ({}): la cinematica para {} jugador(es)", final_ == 2 ? "la nueva Sombra" : "el alba", ven.size());
        return ven.size();
    }

    /** Si a ese jugador le espera el epilogo de Maelis (vio la cinematica del final y aun no la ha oido). */
    public static boolean pendiente(ServerPlayer p) {
        return Ficha.dialogo(p, FICHA) == VIO;
    }

    /** Si esta mirando la cinematica o los creditos. */
    public static boolean viendo(ServerPlayer p) {
        return VIENDO.containsKey(p.getUUID());
    }

    /** Lo llama {@code Conversaciones} al empezar Maelis su epilogo ({@code fin}: "alba" o "sombra"). */
    public static void alHablar(ServerPlayer p, String fin) {
        Ficha.dialogo(p, FICHA, OYO);
        EPILOGOS++;
        Logros.dar(p, "epilogo");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} oye el epilogo de Maelis ({})", p.getGameProfile().getName(), fin);
    }

    /** Su cliente ha acabado de ver los creditos: si le espera Maelis y esta en Morvain, a la capilla, delante de ella. */
    public static void creditosFin(ServerPlayer p) {
        VIENDO.remove(p.getUUID());
        CREDITOS++;
        if (!pendiente(p) || Aguja.final_(p.server) == null || !(p.level() instanceof ServerLevel m) || !Morvain.es(m)) return;
        double[] a = sitioCapilla();
        p.stopRiding();
        p.teleportTo(m, a[0], a[1], a[2], (float) a[3], (float) a[4]);
        p.setDeltaMovement(Vec3.ZERO);
        p.fallDistance = 0;
        LLEVADOS++;
        MAELIS_EN.put(p.getUUID(), ESPERA_MAELIS);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} despierta junto a la Ultima Llama", p.getGameProfile().getName());
    }

    /** Donde se despierta tras los creditos: en la nave de la capilla, a unos tres bloques de Maelis y mirandola {x, y, z, giro, cabeceo}. */
    public static double[] sitioCapilla() {
        double x = 0.5, z = 6.5, y = Homenaje.pisa(Homenaje.CAPILLA);
        double gx = Homenaje.GUIA_X + 0.5, gz = Homenaje.GUIA_Z + 0.5;
        float giro = (float) (Math.toDegrees(Math.atan2(gz - z, gx - x)) - 90.0);
        return new double[]{x, y, z, giro, 14f};
    }

    /** Maelis en la capilla, junto a la Ultima Llama (si no esta y {@code poner}, se la sienta alli). */
    public static Guia maelis(ServerLevel m, boolean poner) {
        BlockPos c = new BlockPos(Homenaje.GUIA_X, Homenaje.pisa(Homenaje.CAPILLA), Homenaje.GUIA_Z);
        List<Guia> l = m.getEntitiesOfClass(Guia.class, new AABB(c).inflate(12, 4, 12));
        if (!l.isEmpty()) return l.get(0);
        if (!poner || !m.isLoaded(c)) return null;
        return Guia.poner(m, c, new BlockPos(6, c.getY(), 18), SantuariosMorvain.ULTIMA.pos());
    }

    private static void hablarMaelis(ServerPlayer p) {
        if (!(p.level() instanceof ServerLevel m) || !Morvain.es(m) || !p.isAlive()) return;
        Guia g = maelis(m, true);
        if (g == null) {
            // aun no estan cargados sus chunks: otro poco
            MAELIS_EN.put(p.getUUID(), 20);
            return;
        }
        MAELIS++;
        com.darkcraft.dialogo.Conversaciones.hablar(p, g);
    }

    // ------------------------------------------------------------------ el cielo (lo que se manda a los clientes)

    /** Como acabo: 0 aun no, 1 el alba, 2 la nueva Sombra. */
    public static int cual(MinecraftServer srv) {
        String f = Aguja.final_(srv);
        return f == null ? 0 : f.equals("sombra") ? 2 : 1;
    }

    /** A todos: como acabo ({@code desde}: el tick en que empieza a cambiar el cielo; -1, ya cambiado). */
    public static void enviarATodos(MinecraftServer srv, long desde) {
        Net.Final pk = new Net.Final(cual(srv), desde);
        for (ServerPlayer p : srv.getPlayerList().getPlayers()) Net.to(p, pk);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Net.to(p, new Net.Final(cual(p.server), -1));
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Net.to(p, new Net.Final(cual(p.server), -1));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        VIENDO.remove(e.getEntity().getUUID());
        MAELIS_EN.remove(e.getEntity().getUUID());
    }

    // ------------------------------------------------------------------ cada tick y la proteccion

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) tick(e.getServer());
    }

    /** Cada tick del servidor (las pruebas lo llaman a mano): la proteccion que se acaba y Maelis que habla. */
    public static void tick(MinecraftServer srv) {
        if (VIENDO.isEmpty() && MAELIS_EN.isEmpty()) return;
        VIENDO.replaceAll((k, v) -> v - 1);
        VIENDO.values().removeIf(v -> v <= 0);
        List<UUID> hablan = new ArrayList<>();
        Iterator<Map.Entry<UUID, Integer>> it = MAELIS_EN.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> en = it.next();
            int v = en.getValue() - 1;
            if (v > 0) {
                en.setValue(v);
                continue;
            }
            it.remove();
            hablan.add(en.getKey());
        }
        for (UUID u : hablan) {
            ServerPlayer p = srv.getPlayerList().getPlayer(u);
            if (p == null) p = PRUEBA.get(u);
            if (p != null) hablarMaelis(p);
        }
    }

    /** Para la prueba del servidor: jugadores de mentira (no estan en la lista del servidor) a los que tambien les habla Maelis. */
    public static final Map<UUID, ServerPlayer> PRUEBA = new HashMap<>();

    /** Mientras mira la cinematica o los creditos no le pasa nada. */
    @SubscribeEvent
    public static void onAtaque(LivingAttackEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && VIENDO.containsKey(p.getUUID())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        VIENDO.clear();
        MAELIS_EN.clear();
    }

    // ------------------------------------------------------------------ para los comandos y las pruebas

    /**
     * El final sin pelea: lo apunta (1 el alba, 2 la nueva Sombra), lleva al jugador a la sala de la aguja (si no estaba) y empieza
     * la cinematica para los que esten alli. El cielo rojo no se toca.
     */
    public static void probar(ServerPlayer p, int final_) {
        ServerLevel m = Morvain.nivel(p.server);
        if (m == null) return;
        Morvain.Datos d = Morvain.Datos.get(p.server);
        d.quitar(Aguja.FINAL_ALBA);
        d.quitar(Aguja.FINAL_SOMBRA);
        d.marcar(final_ == 2 ? Aguja.FINAL_SOMBRA : Aguja.FINAL_ALBA);
        Ficha.dialogo(p, Aguja.FICHA_FINAL, final_ == 2 ? 2 : 1);
        double y = Homenaje.pisa(Homenaje.AZOTEA);
        if (p.level() != m || !Homenaje.enAguja(p.getX(), p.getY() + 0.1, p.getZ())) p.teleportTo(m, 0.5, y, 6.5, 180f, 0f);
        empezar(m, new Vec3(0.5, y + 0.5, 0.5), final_ == 2 ? 2 : 1, p);
    }

    /** Lo que sabe ese jugador del final: que lo vuelva a ver todo (la cinematica, los creditos y el epilogo de Maelis). */
    public static void olvidar(ServerPlayer p) {
        Ficha.dialogo(p, FICHA, 0);
        com.darkcraft.dialogo.Conversaciones.olvidarEpilogo(p);
        VIENDO.remove(p.getUUID());
        MAELIS_EN.remove(p.getUUID());
    }

    /** Quita el final del mundo (el cielo de Morvain vuelve a ser el de antes para todos). */
    public static void deshacer(MinecraftServer srv) {
        Morvain.Datos d = Morvain.Datos.get(srv);
        d.quitar(Aguja.FINAL_ALBA);
        d.quitar(Aguja.FINAL_SOMBRA);
        enviarATodos(srv, -1);
    }

    public static String resumen(ServerPlayer p) {
        String f = Aguja.final_(p.server);
        return "final " + (f == null ? "ninguno" : f) + ", su marca " + Ficha.dialogo(p, FICHA) + (pendiente(p) ? " (le espera Maelis)" : "")
                + (viendo(p) ? ", mirando la cinematica o los creditos" : "") + (MAELIS_EN.containsKey(p.getUUID()) ? ", Maelis va a hablarle" : "")
                + "; cinematicas " + CINEMATICAS + ", creditos " + CREDITOS + ", llevados " + LLEVADOS + ", Maelis habla " + MAELIS + ", epilogos " + EPILOGOS;
    }
}
