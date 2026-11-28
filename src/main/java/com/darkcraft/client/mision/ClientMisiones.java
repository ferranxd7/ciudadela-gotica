package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.mision.Baliza;
import com.darkcraft.mision.Campana;
import com.darkcraft.mision.Corwen;
import com.darkcraft.mision.Duelo;
import com.darkcraft.mision.FarolBrann;
import com.darkcraft.mision.Hedda;
import com.darkcraft.mision.Oswin;
import com.darkcraft.mision.RestosTam;
import com.darkcraft.mision.Ylva;
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
 * su mision con la verdad o la mentira ({@link #alfa}), y sus agujas, que se oyen de cerca. Desde la parte 7an, Ylva: su farol en el
 * regazo (no lo tiene mientras te lo ha prestado: {@link #ylvaSinFarol}), los dos faroles de Brann ({@code FarolBrann}: el de la arena,
 * que se coge con la E —{@link #farol}—, y el del muelle, el de quien se lo dio), Ylva que descansa cuando le das el farol de su padre
 * y los enemigos que se ven a traves de las paredes con el Farol del ballenero ({@code Enemigo.BRILLA}). Desde la parte 7ao, Corwen: las
 * balizas que ve encendidas quien juega ({@code Baliza.ENCENDIDAS}, con sus llamitas), la que se tiene delante para encenderla con la
 * E ({@link #baliza}), el farol de Corwen que luce con las tres encendidas ({@link #corwenLuz}), su pertiga (no la tiene si se quedo:
 * {@link #corwenSinPertiga}) y Corwen que se pierde en la niebla cuando le dejas ir a recibir las barcas.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientMisiones {
    private ClientMisiones() {}

    public static final List<Net.MisionE> LISTA = new ArrayList<>();
    /** La campana, Tam o el farol de Brann que se tienen delante para usarlos con la E (o -1). */
    public static int campana = -1, restos = -1, farol = -1, baliza = -1;
    /** Si el farol de Corwen luce (las tres balizas encendidas) y si ya no tiene su pertiga (se la dio a quien le pidio que se quedara). */
    public static boolean corwenLuz, corwenSinPertiga;
    /** Lo que tarda Corwen en perderse en la niebla (ticks). */
    public static final int FUNDIDO_CORWEN = 90;
    /** Si Ylva le ha prestado su farol a quien juega (no lo tiene en el regazo). */
    public static boolean ylvaSinFarol;
    /** Si quien juega lleva el Farol del ballenero (ve brillar a los enemigos cercanos). */
    private static boolean vigia;
    /** Lo que tarda Ylva en apagarse (ticks). */
    public static final int FUNDIDO_YLVA = 70;
    /** Hasta donde se ven brillar los enemigos con el Farol del ballenero. */
    public static final double VIGIA_R = 24.0;
    /** Lo de cara que se mira a Tam (el coseno), para elegir entre el y Hedda de rodillas si estan los dos delante. */
    public static double restosCos;
    /** Lo que se haria con Tam: {@link #BUFANDA} o {@link #LANZA}. */
    public static int restosQue;
    public static final int BUFANDA = 1, LANZA = 2;
    /** Lo que tarda Hedda en apagarse (ticks). */
    public static final int FUNDIDO = 50;

    static {
        Viajero.OCULTO = ClientMisiones::oculto;
        Enemigo.BRILLA = ClientMisiones::brilla;
        com.darkcraft.enemigos.Sigrun.OCULTA = ClientMisiones::sigrunOculta;
    }

    /** Sigrun (parte 7ap): quien juega acaba de acabar su duelo (la ve caer o marcharse hasta que se deshace en nieve). */
    private static boolean sigrunPropio;
    private static int sigrunDesde = -1;

    /** Ylva se va (descansa) al cerrar la conversacion en que se le da el farol de su padre: como la Hedda de la mentira. */
    private static boolean ylvaPendiente;
    private static int ylvaDesde = -1;
    /** Corwen se va (baja al agua a recibir las barcas) al cerrar la conversacion en que se le deja ir. */
    private static boolean corwenPendiente;
    private static int corwenDesde = -1;

    private static boolean recibido;
    /** El reloj del cliente (ticks) y cuando empezo a apagarse Hedda (-1: no se esta yendo). */
    private static int reloj, seVaDesde = -1;
    /** Se va (la verdad o la mentira) en cuanto se cierre la conversacion; y si es por la mentira (descansa). */
    private static boolean seVaPendiente, seVaEnPaz;
    private static int agujas = 60;

    public static void recibir(Net.Misiones m) {
        int antes = marcas(Hedda.ID), antesY = marcas(Ylva.ID), antesC = marcas(Corwen.ID), antesS = marcas(Duelo.ID);
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
        int y = marcas(Ylva.ID);
        FarolBrann.sinFarol = (y & (Ylva.LLEVA | Ylva.ACABADA)) != 0;
        FarolBrann.enMuelle = (y & Ylva.DADA) != 0;
        ylvaSinFarol = (y & (Ylva.ACEPTADA | Ylva.DADA)) != 0 && (y & Ylva.QUEDADA) == 0;
        if ((y & Ylva.DADA) == 0) {
            ylvaPendiente = false;
            ylvaDesde = -1;
        } else if (!primera && (antesY & Ylva.DADA) == 0) ylvaPendiente = true;
        int sg = marcas(Duelo.ID);
        if ((sg & Duelo.ACABADA) == 0) {
            sigrunPropio = false;
            sigrunDesde = -1;
        } else if (!primera && (antesS & Duelo.ACABADA) == 0) {
            sigrunPropio = true;
            sigrunDesde = reloj;
        }
        int c = marcas(Corwen.ID);
        Baliza.ENCENDIDAS = (c & Corwen.ACABADA) != 0 ? 7 : (c / Corwen.B1) & 7;
        corwenLuz = (c & Corwen.TODAS) == Corwen.TODAS;
        corwenSinPertiga = (c & Corwen.QUEDA) != 0;
        if ((c & Corwen.IDA) == 0) {
            corwenPendiente = false;
            corwenDesde = -1;
        } else if (!primera && (antesC & Corwen.IDA) == 0) corwenPendiente = true;
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
        if (Ylva.ID.equals(e.id())) return (k & Ylva.ACABADA) != 0;
        if (Corwen.ID.equals(e.id())) return (k & Corwen.ACABADA) != 0;
        if (Duelo.ID.equals(e.id())) return (k & Duelo.ACABADA) != 0;
        return false;
    }

    /**
     * Lo que dice el diario de como va (la clave de su texto, {@code darkcraft.mision.<id>.<estado>}). Oswin: devuelta, quedada,
     * volver (lleva la campana y conoce a Oswin), encontrada (la lleva sin conocerle), buscar (aceptada) o pendiente. Hedda: verdad (y
     * lanza, si aun no la saco), mentira, callada, volver, encontrada, buscar o pendiente.
     */
    public static String estado(Net.MisionE e) {
        int k = e.marcas();
        if (Duelo.ID.equals(e.id())) {
            if ((k & Duelo.REMATADA) != 0) return "rematada";
            if ((k & Duelo.LIBRE) != 0) return "libre";
            if ((k & Duelo.VENCIDA) != 0) return "vencida";
            if ((k & Duelo.CAIDO) != 0) return "caido";
            if ((k & Duelo.ACEPTADA) != 0) return "duelo";
            return "pendiente";
        }
        if (Corwen.ID.equals(e.id())) {
            if ((k & Corwen.IDA) != 0) return "ida";
            if ((k & Corwen.QUEDA) != 0) return "queda";
            if ((k & Corwen.ACEPTADA) == 0) return "pendiente";
            return switch (Corwen.encendidas(k)) {
                case 0 -> "buscar";
                case 1 -> "una";
                case 2 -> "dos";
                default -> "volver";
            };
        }
        if (Ylva.ID.equals(e.id())) {
            if ((k & Ylva.DADA) != 0) return "dada";
            if ((k & Ylva.QUEDADA) != 0) return "quedada";
            if ((k & Ylva.LLEVA) != 0) return (k & Ylva.CONOCIDA) == 0 ? "encontrada" : (k & Ylva.CALMA) != 0 ? "volver_luz" : "volver";
            if ((k & Ylva.CALMA) != 0) return "calma";
            if ((k & Ylva.ACEPTADA) != 0) return "buscar";
            return "pendiente";
        }
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
        if (Duelo.ID.equals(e.id())) return com.darkcraft.client.ui.CodiceScreen.MIN_MISION_SIGRUN;
        if (Corwen.ID.equals(e.id())) return com.darkcraft.client.ui.CodiceScreen.MIN_MISION_CORWEN;
        return Ylva.ID.equals(e.id()) ? com.darkcraft.client.ui.CodiceScreen.MIN_MISION_YLVA
                : Hedda.ID.equals(e.id()) ? com.darkcraft.client.ui.CodiceScreen.MIN_MISION_HEDDA : com.darkcraft.client.ui.CodiceScreen.MIN_MISION_OSWIN;
    }

    // ------------------------------------------------------------------ quien da una mision y a quien juega ya no le toca ver

    /**
     * Si a quien juega ya no le toca ver a este (no se dibuja ni se le puede mirar): Hedda de rodillas, sin haberle dicho la verdad; la
     * del camino, despues de la verdad o la mentira (cuando acaba de apagarse).
     */
    public static boolean oculto(Viajero v) {
        if (v.quien() == Viajero.Quien.CORWEN) {
            // a quien le dejo ir a recibir las barcas: se pierde en la niebla al cerrar la conversacion
            if ((marcas(Corwen.ID) & Corwen.IDA) == 0) return false;
            return !(corwenPendiente || (corwenDesde >= 0 && reloj - corwenDesde < FUNDIDO_CORWEN));
        }
        if (v.quien() == Viajero.Quien.YLVA) {
            // a quien le dio el farol de su padre: descansa (se apaga al cerrar la conversacion)
            if ((marcas(Ylva.ID) & Ylva.DADA) == 0) return false;
            return !(ylvaPendiente || (ylvaDesde >= 0 && reloj - ylvaDesde < FUNDIDO_YLVA));
        }
        if (v.quien() != Viajero.Quien.HEDDA) return false;
        int h = marcas(Hedda.ID);
        if (v.variante() == 1) return (h & Hedda.VERDAD) == 0;
        if ((h & (Hedda.VERDAD | Hedda.MENTIRA)) == 0) return false;
        return !(seVaPendiente || (seVaDesde >= 0 && reloj - seVaDesde < FUNDIDO));
    }

    /**
     * Si a quien juega ya no le toca ver a Sigrun (parte 7ap): acabo su duelo; quien lo acaba de acabar la sigue viendo caer o
     * marcharse (de rodillas, tendida o yendose) hasta que se deshace en nieve.
     */
    public static boolean sigrunOculta(com.darkcraft.enemigos.Sigrun s) {
        // (la del bestiario no esta en el mundo: esa siempre se ve)
        if ((marcas(Duelo.ID) & Duelo.ACABADA) == 0 || !s.isAddedToWorld()) return false;
        int m = s.modo();
        return !(sigrunPropio && (m == com.darkcraft.enemigos.Sigrun.RENDIDA || m == com.darkcraft.enemigos.Sigrun.CAIDA || m == com.darkcraft.enemigos.Sigrun.MARCHA));
    }

    /** Si a quien juega le toca hablar ahora con Sigrun: en paz en su puesto (sin haber acabado su duelo) o, de rodillas, si la vencio. */
    public static boolean sigrunHabla(com.darkcraft.enemigos.Sigrun s) {
        int k = marcas(Duelo.ID);
        if ((k & Duelo.ACABADA) != 0 || !s.isAlive() || s.funde()) return false;
        if (s.modo() == com.darkcraft.enemigos.Sigrun.RENDIDA) return (k & Duelo.VENCIDA) != 0 && s.accionVista() == 0;
        return s.modo() == com.darkcraft.enemigos.Sigrun.PAZ && s.clavada() && s.accionVista() == 0;
    }

    /** Lo que se ve de el (1, o menos mientras Hedda se apaga). */
    public static float alfa(Viajero v, float pt) {
        if (v.quien() == Viajero.Quien.CORWEN) {
            if (corwenPendiente || corwenDesde < 0) return 1f;
            return Mth.clamp(1f - (reloj + pt - corwenDesde) / FUNDIDO_CORWEN, 0f, 1f);
        }
        if (v.quien() == Viajero.Quien.YLVA) {
            if (ylvaPendiente || ylvaDesde < 0) return 1f;
            return Mth.clamp(1f - (reloj + pt - ylvaDesde) / FUNDIDO_YLVA, 0f, 1f);
        }
        if (v.quien() != Viajero.Quien.HEDDA || v.variante() != 0 || seVaPendiente || seVaDesde < 0) return 1f;
        return Mth.clamp(1f - (reloj + pt - seVaDesde) / FUNDIDO, 0f, 1f);
    }

    // ------------------------------------------------------------------ cada tick

    public static void tick(Minecraft mc) {
        reloj++;
        campana = -1;
        restos = -1;
        farol = -1;
        baliza = -1;
        if (mc.player == null || mc.level == null) return;
        vigia = com.darkcraft.client.ui.ClientRol.lleva(com.darkcraft.rol.Talisman.Clase.BALLENERO);
        ylva(mc);
        corwen(mc);
        // quien acaba de acabar su duelo con Sigrun deja de verla cuando vuelve a su puesto (o pasado un buen rato)
        if (sigrunPropio && reloj - sigrunDesde > 40) {
            boolean enPaz = reloj - sigrunDesde > 2400;
            for (com.darkcraft.enemigos.Sigrun g : mc.level.getEntitiesOfClass(com.darkcraft.enemigos.Sigrun.class, mc.player.getBoundingBox().inflate(96)))
                if (g.modo() == com.darkcraft.enemigos.Sigrun.PAZ || g.modo() == com.darkcraft.enemigos.Sigrun.DUELO) enPaz = true;
            if (enPaz) sigrunPropio = false;
        }
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
        // una baliza de los Pilotes que se pueda encender (parte 7ao): con la mision en marcha y apagada para quien juega; se mira el
        // poste o el farol (a menos de 4 bloques)
        int c = marcas(Corwen.ID);
        if ((c & Corwen.ACEPTADA) != 0 && (c & Corwen.ACABADA) == 0) {
            double mejor = Math.cos(Math.toRadians(24));
            for (Baliza b : mc.level.getEntitiesOfClass(Baliza.class, mc.player.getBoundingBox().inflate(6))) {
                if (b.encendida()) continue;
                for (double h : new double[]{0.6, 1.3, Baliza.ALTO_FAROL}) {
                    boolean arriba = h == Baliza.ALTO_FAROL;
                    Vec3 d = new Vec3(arriba ? b.farolX() : b.getX(), b.getY() + h, arriba ? b.farolZ() : b.getZ()).subtract(ojo);
                    double l = d.length();
                    if (l > 4.0 || l < 1e-3) continue;
                    double cos = d.dot(mira) / l + (l < 1.6 ? 0.1 : 0);
                    if (cos > mejor) {
                        mejor = cos;
                        baliza = b.getId();
                    }
                }
            }
        }
        // el farol de Brann en la arena del Costillar (parte 7an), si aun no se cogio
        if (!FarolBrann.sinFarol) {
            double mejor = Math.cos(Math.toRadians(28));
            for (FarolBrann f : mc.level.getEntitiesOfClass(FarolBrann.class, mc.player.getBoundingBox().inflate(5))) {
                if (f.variante() != 0) continue;
                Vec3 d = f.position().add(0, 0.25, 0).subtract(ojo);
                double l = d.length();
                if (l > 3.8 || l < 1e-3) continue;
                double cos = d.dot(mira) / l + (l < 1.6 ? 0.1 : 0);
                if (cos > mejor) {
                    mejor = cos;
                    farol = f.getId();
                }
            }
        }
    }

    /**
     * Ylva (parte 7an): descansa al cerrar la conversacion en que se le da el farol de su padre (el mar en calma y los dos faroles que
     * crujen en su cadena, almas y luz que suben); y el farol de Brann del muelle, encendido, echa alguna llamita.
     */
    private static void ylva(Minecraft mc) {
        Viajero y = null;
        for (Viajero v : mc.level.getEntitiesOfClass(Viajero.class, mc.player.getBoundingBox().inflate(40)))
            if (v.quien() == Viajero.Quien.YLVA) y = v;
        if (ylvaPendiente && !(mc.screen instanceof com.darkcraft.client.ui.DialogoScreen)) {
            ylvaPendiente = false;
            ylvaDesde = reloj;
            if (y != null) mc.level.playLocalSound(y.getX(), y.getY() + 1, y.getZ(), DarkCraftMod.SFX_YLVA_DESCANSA.get(), SoundSource.NEUTRAL, 1.2f, 1f, false);
        }
        var r = mc.level.random;
        if (y != null && ylvaDesde >= 0 && reloj - ylvaDesde < FUNDIDO_YLVA)
            for (int i = 0; i < 3; i++)
                mc.level.addParticle(r.nextInt(3) == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.END_ROD,
                        y.getX() + (r.nextDouble() - 0.5) * 0.8, y.getY() + 0.2 + r.nextDouble() * 1.2, y.getZ() + (r.nextDouble() - 0.5) * 0.8,
                        0, 0.03 + r.nextDouble() * 0.03, 0);
        if (FarolBrann.enMuelle && reloj % 7 == 0)
            for (FarolBrann f : mc.level.getEntitiesOfClass(FarolBrann.class, mc.player.getBoundingBox().inflate(24)))
                if (f.variante() == 1 && r.nextInt(3) == 0)
                    mc.level.addParticle(ParticleTypes.SMALL_FLAME, f.getX() + (r.nextDouble() - 0.5) * 0.08, f.getY() + 0.33, f.getZ() + (r.nextDouble() - 0.5) * 0.08,
                            0, 0.004, 0);
    }

    /**
     * Corwen (parte 7ao): se pierde en la niebla al cerrar la conversacion en que se le deja ir (sus pasos en el agua, el farol que cruje
     * y las campanas lejos; salpicaduras y luces que suben); el farol de su pertiga (o el del suelo) echa alguna llamita con las tres
     * balizas encendidas, y las balizas encendidas de quien juega tambien.
     */
    private static void corwen(Minecraft mc) {
        Viajero cw = null;
        for (Viajero v : mc.level.getEntitiesOfClass(Viajero.class, mc.player.getBoundingBox().inflate(48)))
            if (v.quien() == Viajero.Quien.CORWEN) cw = v;
        if (corwenPendiente && !(mc.screen instanceof com.darkcraft.client.ui.DialogoScreen)) {
            corwenPendiente = false;
            corwenDesde = reloj;
            if (cw != null) mc.level.playLocalSound(cw.getX(), cw.getY() + 1, cw.getZ(), DarkCraftMod.SFX_CORWEN_SE_VA.get(), SoundSource.NEUTRAL, 1.2f, 1f, false);
        }
        var r = mc.level.random;
        if (cw != null && corwenDesde >= 0 && reloj - corwenDesde < FUNDIDO_CORWEN)
            for (int i = 0; i < 3; i++) {
                double x = cw.getX() + (r.nextDouble() - 0.5) * 0.9, z = cw.getZ() + (r.nextDouble() - 0.5) * 0.9;
                if (i == 0) mc.level.addParticle(ParticleTypes.SPLASH, x, cw.getY() + 0.1, z, 0, 0.1, 0);
                else mc.level.addParticle(r.nextInt(3) == 0 ? ParticleTypes.FLAME : ParticleTypes.END_ROD, x, cw.getY() + 0.3 + r.nextDouble() * 1.4, z,
                        0, 0.02 + r.nextDouble() * 0.03, 0);
            }
        // el farol de Corwen, encendido
        if (cw != null && corwenLuz && !oculto(cw) && corwenDesde < 0 && reloj % 6 == 0 && r.nextInt(2) == 0) {
            double s = Math.sin(Math.toRadians(cw.yBodyRot)), co = Math.cos(Math.toRadians(cw.yBodyRot));
            double fx, fy, fz;
            if (corwenSinPertiga) {
                // en el suelo, a su izquierda
                fx = cw.getX() + co * 0.5 - s * 0.12;
                fz = cw.getZ() + s * 0.5 + co * 0.12;
                fy = cw.getY() + 0.22;
            } else {
                // colgado de la pertiga, delante a su derecha
                fx = cw.getX() - co * 0.5 - s * 0.55;
                fz = cw.getZ() - s * 0.5 + co * 0.55;
                fy = cw.getY() + 2.05;
            }
            mc.level.addParticle(ParticleTypes.SMALL_FLAME, fx + (r.nextDouble() - 0.5) * 0.06, fy, fz + (r.nextDouble() - 0.5) * 0.06, 0, 0.004, 0);
        }
        // las balizas encendidas de quien juega
        if (Baliza.ENCENDIDAS != 0 && reloj % 5 == 0)
            for (Baliza b : mc.level.getEntitiesOfClass(Baliza.class, mc.player.getBoundingBox().inflate(48)))
                if (b.encendida() && r.nextInt(2) == 0) {
                    double fx = b.farolX(), fz = b.farolZ(), fy = b.getY() + Baliza.ALTO_FAROL - 0.15;
                    mc.level.addParticle(ParticleTypes.SMALL_FLAME, fx + (r.nextDouble() - 0.5) * 0.12, fy, fz + (r.nextDouble() - 0.5) * 0.12, 0, 0.006, 0);
                    if (r.nextInt(5) == 0) mc.level.addParticle(ParticleTypes.SMOKE, fx, fy + 0.55, fz, 0, 0.02, 0);
                }
    }

    /** Con el Farol del ballenero, los enemigos vivos a {@link #VIGIA_R} bloques de quien juega brillan (se ven a traves de las paredes). */
    private static boolean brilla(Enemigo e) {
        if (!vigia || !e.isAlive()) return false;
        var p = Minecraft.getInstance().player;
        return p != null && e.distanceToSqr(p) < VIGIA_R * VIGIA_R;
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

    /** La E con el farol de Brann delante: se coge (el servidor decide). */
    public static void cogerFarol(Minecraft mc) {
        usar(mc, farol);
    }

    /** La E con una baliza delante: se enciende (el servidor decide). */
    public static void encenderBaliza(Minecraft mc) {
        usar(mc, baliza);
    }

    private static void usar(Minecraft mc, int id) {
        if (id < 0 || mc.level == null || mc.player == null || mc.gameMode == null) return;
        Entity e = mc.level.getEntity(id);
        if (e instanceof Campana || e instanceof RestosTam || e instanceof FarolBrann || e instanceof Baliza)
            mc.gameMode.interact(mc.player, e, InteractionHand.MAIN_HAND);
    }

    @SubscribeEvent
    public static void onSalir(ClientPlayerNetworkEvent.LoggingOut e) {
        LISTA.clear();
        campana = -1;
        restos = -1;
        farol = -1;
        baliza = -1;
        recibido = false;
        seVaPendiente = false;
        seVaDesde = -1;
        ylvaPendiente = false;
        ylvaDesde = -1;
        ylvaSinFarol = vigia = false;
        corwenPendiente = false;
        corwenDesde = -1;
        corwenLuz = corwenSinPertiga = false;
        sigrunPropio = false;
        sigrunDesde = -1;
        Baliza.ENCENDIDAS = 0;
        Campana.oculta = false;
        RestosTam.sinBufanda = RestosTam.sinLanza = RestosTam.nada = false;
        FarolBrann.sinFarol = FarolBrann.enMuelle = false;
    }
}
