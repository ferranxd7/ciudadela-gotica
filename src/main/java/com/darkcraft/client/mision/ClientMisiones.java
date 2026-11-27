package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.mision.Campana;
import com.darkcraft.mision.Hedda;
import com.darkcraft.mision.Oswin;
import com.darkcraft.mision.RestosTam;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Viajero;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que sabe el cliente de las misiones (parte 7ag; 7ah): la lista que manda el servidor ({@code Net.Misiones}: cada una con sus marcas
 * y adonde ir ahora), para el diario de la Historia, la brujula y el mapa; lo que le toca ver a quien juega (la campana del Arbol de la
 * Horca, {@link Campana#oculta}; Hedda en el camino o de rodillas junto a Tam, {@link #oculto}; la bufanda y la lanza de Tam,
 * {@code RestosTam}); lo que se tiene delante para usarlo con la E ({@link #campana}, {@link #restos}); Hedda que se apaga cuando acaba
 * su mision con la verdad o la mentira ({@link #alfa}), y sus agujas, que se oyen de cerca.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientMisiones {
    private ClientMisiones() {}

    public static final List<Net.MisionE> LISTA = new ArrayList<>();
    /** La campana o Tam que se tienen delante para usarlos con la E (o -1). */
    public static int campana = -1, restos = -1;
    /** Lo de cara que se mira a Tam (el coseno), para elegir entre el y Hedda de rodillas si estan los dos delante. */
    public static double restosCos;
    /** Lo que se haria con Tam: {@link #BUFANDA} o {@link #LANZA}. */
    public static int restosQue;
    public static final int BUFANDA = 1, LANZA = 2;
    /** Lo que tarda Hedda en apagarse (ticks). */
    public static final int FUNDIDO = 50;

    static {
        Viajero.OCULTO = ClientMisiones::oculto;
    }

    private static boolean recibido;
    /** El reloj del cliente (ticks) y cuando empezo a apagarse Hedda (-1: no se esta yendo). */
    private static int reloj, seVaDesde = -1;
    /** Se va (la verdad o la mentira) en cuanto se cierre la conversacion; y si es por la mentira (descansa). */
    private static boolean seVaPendiente, seVaEnPaz;
    private static int agujas = 60;

    public static void recibir(Net.Misiones m) {
        int antes = marcas(Hedda.ID);
        boolean primera = !recibido;
        LISTA.clear();
        LISTA.addAll(m.lista());
        recibido = true;
        int k = marcas(Oswin.ID);
        Campana.oculta = (k & (Oswin.LLEVA | Oswin.DEVUELTA | Oswin.QUEDADA)) != 0;
        int h = marcas(Hedda.ID);
        RestosTam.sinBufanda = (h & (Hedda.LLEVA | Hedda.ACABADA)) != 0;
        RestosTam.sinLanza = (h & Hedda.LANZA) != 0;
        RestosTam.nada = (h & Hedda.VERDAD) != 0 ? (h & Hedda.LANZA) != 0 : (h & (Hedda.LLEVA | Hedda.ACABADA)) != 0;
        int fuera = Hedda.VERDAD | Hedda.MENTIRA;
        if ((h & fuera) == 0) {
            seVaPendiente = false;
            seVaDesde = -1;
        } else if (!primera && (antes & fuera) == 0) {
            // acaba de acabarse (con quien juega delante): se apaga al cerrar la conversacion
            seVaPendiente = true;
            seVaEnPaz = (h & Hedda.MENTIRA) != 0;
        }
    }

    public static Net.MisionE de(String id) {
        for (Net.MisionE e : LISTA) if (e.id().equals(id)) return e;
        return null;
    }

    public static int marcas(String id) {
        Net.MisionE e = de(id);
        return e == null ? 0 : e.marcas();
    }

    /** Si la mision esta acabada (de una manera o de otra; la de Hedda, tras la verdad, cuando se ha sacado la lanza). */
    public static boolean acabada(Net.MisionE e) {
        int k = e.marcas();
        if (Oswin.ID.equals(e.id())) return (k & (Oswin.DEVUELTA | Oswin.QUEDADA)) != 0;
        if (Hedda.ID.equals(e.id())) return (k & (Hedda.MENTIRA | Hedda.CALLADA)) != 0 || (k & (Hedda.VERDAD | Hedda.LANZA)) == (Hedda.VERDAD | Hedda.LANZA);
        return false;
    }

    /**
     * Lo que dice el diario de como va (la clave de su texto, {@code darkcraft.mision.<id>.<estado>}). Oswin: devuelta, quedada,
     * volver (lleva la campana y conoce a Oswin), encontrada (la lleva sin conocerle), buscar (aceptada) o pendiente. Hedda: verdad (y
     * lanza, si aun no la saco), mentira, callada, volver, encontrada, buscar o pendiente.
     */
    public static String estado(Net.MisionE e) {
        int k = e.marcas();
        if (Hedda.ID.equals(e.id())) {
            if ((k & Hedda.VERDAD) != 0) return (k & Hedda.LANZA) != 0 ? "verdad" : "lanza";
            if ((k & Hedda.MENTIRA) != 0) return "mentira";
            if ((k & Hedda.CALLADA) != 0) return "callada";
            if ((k & Hedda.LLEVA) != 0) return (k & Hedda.CONOCIDA) != 0 ? "volver" : "encontrada";
            if ((k & Hedda.ACEPTADA) != 0) return "buscar";
            return "pendiente";
        }
        if ((k & Oswin.DEVUELTA) != 0) return "devuelta";
        if ((k & Oswin.QUEDADA) != 0) return "quedada";
        if ((k & Oswin.LLEVA) != 0) return (k & Oswin.CONOCIDA) != 0 ? "volver" : "encontrada";
        if ((k & Oswin.ACEPTADA) != 0) return "buscar";
        return "pendiente";
    }

    /** La miniatura del diario de cada mision ({@code CodiceScreen}). */
    public static int miniatura(Net.MisionE e) {
        return Hedda.ID.equals(e.id()) ? com.darkcraft.client.ui.CodiceScreen.MIN_MISION_HEDDA : com.darkcraft.client.ui.CodiceScreen.MIN_MISION_OSWIN;
    }

    // ------------------------------------------------------------------ quien da una mision y a quien juega ya no le toca ver

    /**
     * Si a quien juega ya no le toca ver a este (no se dibuja ni se le puede mirar): Hedda de rodillas, sin haberle dicho la verdad; la
     * del camino, despues de la verdad o la mentira (cuando acaba de apagarse).
     */
    public static boolean oculto(Viajero v) {
        if (v.quien() != Viajero.Quien.HEDDA) return false;
        int h = marcas(Hedda.ID);
        if (v.variante() == 1) return (h & Hedda.VERDAD) == 0;
        if ((h & (Hedda.VERDAD | Hedda.MENTIRA)) == 0) return false;
        return !(seVaPendiente || (seVaDesde >= 0 && reloj - seVaDesde < FUNDIDO));
    }

    /** Lo que se ve de el (1, o menos mientras Hedda se apaga). */
    public static float alfa(Viajero v, float pt) {
        if (v.quien() != Viajero.Quien.HEDDA || v.variante() != 0 || seVaPendiente || seVaDesde < 0) return 1f;
        return Mth.clamp(1f - (reloj + pt - seVaDesde) / FUNDIDO, 0f, 1f);
    }

    // ------------------------------------------------------------------ cada tick

    public static void tick(Minecraft mc) {
        reloj++;
        campana = -1;
        restos = -1;
        if (mc.player == null || mc.level == null) return;
        Viajero hedda = heddaCerca(mc);
        // Hedda se va al cerrar la conversacion: se apaga (la mentira: en paz, con su luz y su sonido; la verdad: se va con su hijo)
        if (seVaPendiente && !(mc.screen instanceof com.darkcraft.client.ui.DialogoScreen)) {
            seVaPendiente = false;
            seVaDesde = reloj;
            if (hedda != null && seVaEnPaz)
                mc.level.playLocalSound(hedda.getX(), hedda.getY() + 1, hedda.getZ(), DarkCraftMod.SFX_HEDDA_DESCANSA.get(), SoundSource.NEUTRAL, 1.2f, 1f, false);
        }
        if (hedda != null && seVaDesde >= 0 && reloj - seVaDesde < FUNDIDO) {
            var r = mc.level.random;
            for (int i = 0; i < (seVaEnPaz ? 3 : 1); i++)
                mc.level.addParticle(seVaEnPaz ? (r.nextInt(3) == 0 ? ParticleTypes.SOUL : ParticleTypes.END_ROD) : ParticleTypes.ASH,
                        hedda.getX() + (r.nextDouble() - 0.5) * 0.8, hedda.getY() + 0.3 + r.nextDouble() * 1.2, hedda.getZ() + (r.nextDouble() - 0.5) * 0.8,
                        0, seVaEnPaz ? 0.03 + r.nextDouble() * 0.03 : 0.005, 0);
        }
        // sus agujas, de cerca, mientras teje
        if (hedda != null && !oculto(hedda) && seVaDesde < 0 && mc.player.distanceToSqr(hedda) < 12 * 12 && --agujas <= 0) {
            agujas = 120 + mc.level.random.nextInt(80);
            mc.level.playLocalSound(hedda.getX(), hedda.getY() + 0.8, hedda.getZ(), DarkCraftMod.SFX_AGUJAS.get(), SoundSource.NEUTRAL, 0.55f, 1f, false);
        }
        if (!mc.player.isAlive() || mc.player.isSpectator()) return;
        Vec3 ojo = mc.player.getEyePosition(), mira = mc.player.getLookAngle();
        // la campana del peregrino delante (a menos de 3,6 bloques, mirandola)
        if (!Campana.oculta) {
            double mejor = Math.cos(Math.toRadians(24));
            for (Campana c : mc.level.getEntitiesOfClass(Campana.class, mc.player.getBoundingBox().inflate(5))) {
                Vec3 d = c.position().add(0, Campana.ALTO * 0.5, 0).subtract(ojo);
                double l = d.length();
                if (l > 3.6 || l < 1e-3) continue;
                double cos = d.dot(mira) / l + (l < 1.5 ? 0.1 : 0);
                if (cos > mejor) {
                    mejor = cos;
                    campana = c.getId();
                }
            }
        }
        // Tam delante (la bufanda o la lanza), si queda algo que coger
        if (!RestosTam.nada) {
            double mejor = Math.cos(Math.toRadians(26));
            for (RestosTam t : mc.level.getEntitiesOfClass(RestosTam.class, mc.player.getBoundingBox().inflate(5))) {
                Vec3 d = t.position().add(0, 0.55, 0).subtract(ojo);
                double l = d.length();
                if (l > 3.8 || l < 1e-3) continue;
                double cos = d.dot(mira) / l + (l < 1.6 ? 0.1 : 0);
                if (cos > mejor) {
                    mejor = cos;
                    restos = t.getId();
                    restosCos = cos;
                }
            }
            restosQue = (marcas(Hedda.ID) & Hedda.VERDAD) != 0 ? LANZA : BUFANDA;
        }
    }

    /** La Hedda del camino, si esta cerca (para su sonido y su luz al irse). */
    private static Viajero heddaCerca(Minecraft mc) {
        for (Viajero v : mc.level.getEntitiesOfClass(Viajero.class, mc.player.getBoundingBox().inflate(40)))
            if (v.quien() == Viajero.Quien.HEDDA && v.variante() == 0) return v;
        return null;
    }

    /** La E con la campana delante: se coge (como el clic derecho: el servidor decide). */
    public static void coger(Minecraft mc) {
        usar(mc, campana);
    }

    /** La E con Tam delante: la bufanda o la lanza (el servidor decide). */
    public static void cogerRestos(Minecraft mc) {
        usar(mc, restos);
    }

    private static void usar(Minecraft mc, int id) {
        if (id < 0 || mc.level == null || mc.player == null || mc.gameMode == null) return;
        Entity e = mc.level.getEntity(id);
        if (e instanceof Campana || e instanceof RestosTam) mc.gameMode.interact(mc.player, e, InteractionHand.MAIN_HAND);
    }

    @SubscribeEvent
    public static void onSalir(ClientPlayerNetworkEvent.LoggingOut e) {
        LISTA.clear();
        campana = -1;
        restos = -1;
        recibido = false;
        seVaPendiente = false;
        seVaDesde = -1;
        Campana.oculta = false;
        RestosTam.sinBufanda = RestosTam.sinLanza = RestosTam.nada = false;
    }
}
