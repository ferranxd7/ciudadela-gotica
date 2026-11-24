package com.darkcraft.client.combate;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.client.ui.Bebida;
import com.darkcraft.client.ui.Carteles;
import com.darkcraft.client.ui.ClientEquipo;
import com.darkcraft.client.ui.ClientRol;
import com.darkcraft.client.ui.Teclas;
import com.darkcraft.combate.Armas;
import com.darkcraft.combate.Peso;
import com.darkcraft.net.Net;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * El combate propio, parte del cliente: el que lleva el ritmo. Mira la tecla de atacar (pulsar y soltar = golpe
 * ligero, que encadena con el siguiente si se pulsa a tiempo; mantener = cargar el golpe fuerte, que sale al soltar;
 * corriendo = ataque corriendo; en el aire = golpe del salto), la de rodar (con direccion = voltereta, sin ninguna =
 * paso atras) y lo pide al servidor, que es el que pega. Mientras, anima al jugador ({@link Poses}), lo frena, le da el
 * empujon de cada golpe y lo mueve en la voltereta (el movimiento del jugador lo lleva su cliente).
 * <p>
 * Solo con un arma del combate en la mano derecha ({@link Armas#de}) y en supervivencia. Con la mano vacia o con un
 * hacha mirando a un bloque, o mirando algo que no es una criatura (un marco, un soporte), el clic hace lo de siempre.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientCombate {
    private ClientCombate() {}

    /** Ticks que hay que mantener la tecla para que empiece a cargar el golpe fuerte. */
    private static final int CARGA_TRAS = 5;
    /** Lo que se guarda una accion pedida antes de tiempo, y lo que dura la cadena de ligeros tras acabar un golpe. */
    private static final int ESPERA_GUARDADA = 10, VENTANA_CADENA = 8;

    /** Lo que se esta animando ahora (un golpe, la carga, la voltereta), o null. */
    private static Poses.Anim_ actual;
    /** El siguiente golpe de la cadena de ligeros y hasta que tick se puede seguir. */
    private static int paso, cadenaHasta = -1000;
    /** La tecla de atacar: ticks que lleva pulsada (-1 = suelta) y si ya se esta cargando el golpe fuerte. */
    private static int pulsada = -1;
    private static boolean cargando;
    /** Una accion pedida antes de tiempo (ligero, pesado con su carga, rodar), que sale en cuanto se pueda. */
    private static int guardada = -1, guardadaTick, guardadaCarga;
    /** La voltereta: hacia donde y lo rapido (bloques por tick). */
    private static float rodarDx, rodarDz, rodarRapidez;
    /** El temblor de la camara al dar un golpe. */
    private static int temblor;
    private static float temblorFuerza;
    /** Mientras se pregunta a los demas si se puede atacar (para no cancelarse uno mismo). */
    private static boolean preguntando;
    /** Para la prueba de la CI: golpes y volteretas hechas, rechazadas y golpes que han dado. */
    private static int hechos, rechazados, impactos;
    private static String ultimo = "-";

    /** La animacion del propio jugador (la usa {@link Poses}). */
    static Poses.Anim_ anim() {
        return actual;
    }

    /** Si el propio jugador esta en medio de un golpe, de la carga o de la voltereta. */
    public static boolean ocupado() {
        return actual != null;
    }

    public static boolean rodando() {
        return actual != null && (actual.accion == Armas.RODAR || actual.accion == Armas.PASO_ATRAS);
    }

    // ------------------------------------------------------------------ cuando se pelea con el combate propio

    /** Si el jugador puede moverse por su cuenta (rodar): supervivencia, vivo, sin cinematica, sin montar. */
    private static boolean suelto(Minecraft mc) {
        LocalPlayer p = mc.player;
        return p != null && mc.level != null && ClientEquipo.activo() && p.isAlive() && !p.isSpectator() && !p.isPassenger()
                && !p.isFallFlying() && !ClientCine.active();
    }

    /** El arma de la mano derecha si se pelea con el combate propio, o null. */
    public static Armas.Tipo armado(Minecraft mc) {
        if (!suelto(mc)) return null;
        if (mc.player.getInventory().selected > 2) return null;
        return Armas.de(mc.player.getMainHandItem());
    }

    /** Si el clic hace lo de siempre: romper un bloque con el puno o con una herramienta, o golpear algo que no es una criatura. */
    private static boolean loDeSiempre(Minecraft mc) {
        HitResult h = mc.hitResult;
        if (h == null) return false;
        if (h.getType() == HitResult.Type.BLOCK) {
            ItemStack st = mc.player.getMainHandItem();
            return st.isEmpty() || st.getItem() instanceof DiggerItem;
        }
        if (h instanceof EntityHitResult eh) {
            Entity e = eh.getEntity();
            return !(e instanceof LivingEntity) || e instanceof ArmorStand;
        }
        return false;
    }

    /**
     * Si se puede atacar ahora: se pregunta por el mismo evento que el clic del juego, asi los que ya decidian sobre el
     * clic siguen decidiendo (los combos de la Espada del Rey con Shift o Ctrl, las cinematicas, beber, sin estamina...).
     */
    private static boolean permitido(Minecraft mc) {
        preguntando = true;
        try {
            return !ForgeHooksClient.onClickInput(0, mc.options.keyAttack, InteractionHand.MAIN_HAND).isCanceled();
        } finally {
            preguntando = false;
        }
    }

    /** La clase de peso del equipo que se lleva (la misma cuenta que hace el servidor). */
    public static int clasePeso(LocalPlayer p) {
        return Peso.clase(Peso.de(p.getInventory()), ClientRol.pesoMaximo());
    }

    // ------------------------------------------------------------------ cada tick, antes de que el juego mire el raton

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (e.phase == TickEvent.Phase.END) {
            if (temblor > 0) temblor--;
            Poses.tick(mc);
            return;
        }
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        Armas.Tipo tipo = armado(mc);
        avanzar(mc, tipo);
        boolean juego = mc.screen == null;
        // rodar
        boolean rueda = false;
        while (Teclas.RODAR.consumeClick()) rueda = true;
        if (rueda && juego && suelto(mc)) rodar(mc, false);
        // atacar
        if (tipo == null || !juego) {
            if (pulsada >= 0 || cargando) soltarTecla();
            return;
        }
        boolean abajo = mc.options.keyAttack.isDown();
        int clics = 0;
        if (pulsada >= 0 || !loDeSiempre(mc)) while (mc.options.keyAttack.consumeClick()) clics++;
        if (pulsada < 0) {
            if (clics == 0 || !permitido(mc)) return;
            if (ClientRol.agotado) {
                ClientRol.sinEstaminaTick = ClientRol.ticks;
                return;
            }
            // corriendo o en el aire, el golpe sale al momento; si no, se espera a ver si se mantiene la tecla
            if (p.isSprinting() && p.onGround() && !p.isInWater()) pedir(mc, tipo, Armas.CORRIENDO, 0);
            else if (!p.onGround() && !p.isInWater() && !p.onClimbable() && !p.getAbilities().flying) pedir(mc, tipo, Armas.SALTO, 0);
            else if (!abajo) pedir(mc, tipo, Armas.LIGERO, 0);
            else pulsada = 0;
            return;
        }
        if (abajo) {
            pulsada++;
            if (!cargando && pulsada >= CARGA_TRAS && libre()) cargar(mc, tipo);
            else if (cargando && actual != null && ClientRol.ticks - actual.inicio >= Armas.CARGA_MAX + 8) {
                // cargado del todo y un poco mas: sale solo
                pedir(mc, tipo, Armas.PESADO, Armas.CARGA_MAX);
                pulsada = -2;
            }
        } else {
            if (cargando) pedir(mc, tipo, Armas.PESADO, actual != null ? ClientRol.ticks - actual.inicio : 0);
            else if (pulsada >= 0) pedir(mc, tipo, Armas.LIGERO, 0);
            pulsada = -1;
        }
        // -2: el golpe fuerte salio solo; hasta soltar la tecla no se hace nada mas
        if (pulsada == -2 && !abajo) pulsada = -1;
    }

    /** La animacion en marcha: se acaba, se corta si se cambia de arma, empuja en el golpe y mueve en la voltereta. */
    private static void avanzar(Minecraft mc, Armas.Tipo tipo) {
        LocalPlayer p = mc.player;
        if (actual != null) {
            Poses.Anim_ a = actual;
            int t = ClientRol.ticks - a.inicio;
            boolean rueda = a.accion == Armas.RODAR || a.accion == Armas.PASO_ATRAS;
            if (!p.isAlive() || ClientCine.active() || (!rueda && tipo != a.tipo)) {
                cortar();
                return;
            }
            switch (a.accion) {
                case Armas.CARGA -> {
                    if (t > Armas.CARGA_MAX + 60) cortar();
                }
                case Armas.SALTO -> {
                    if (a.aterrizo < 0 && t >= 2 && (p.onGround() || p.isInWater() || t >= 30)) {
                        a.aterrizo = ClientRol.ticks;
                        sacudir(true);
                    } else if (a.aterrizo < 0 && t == 1 && p.getDeltaMovement().y > -0.2) {
                        // el golpe del salto cae con fuerza
                        p.setDeltaMovement(p.getDeltaMovement().add(0, -0.18, 0));
                    }
                    if (a.aterrizo >= 0 && ClientRol.ticks - a.aterrizo >= 8) terminar();
                }
                case Armas.RODAR, Armas.PASO_ATRAS -> {
                    if (t >= a.mov.dura()) terminar();
                    else mover(p, a, t);
                }
                default -> {
                    if (t == Math.max(0, a.mov.golpe0() - 2)) silbido(mc, a);
                    if (t == Math.max(0, a.mov.golpe0() - 1)) {
                        // el empujon hacia delante del golpe
                        if (a.mov.empuje() > 0 && p.onGround()) {
                            Vec3 m = p.getLookAngle();
                            Vec3 h = new Vec3(m.x, 0, m.z);
                            if (h.lengthSqr() > 1e-4) {
                                h = h.normalize().scale(a.mov.empuje());
                                p.setDeltaMovement(p.getDeltaMovement().add(h.x, 0, h.z));
                            }
                        }
                        if (a.tipo == Armas.Tipo.PUNOS)
                            p.swing(a.izquierda && mc.options.getCameraType() != CameraType.FIRST_PERSON ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                    }
                    if (a.accion == Armas.CORRIENDO && t == a.mov.golpe1()) p.setSprinting(false);
                    if (t >= a.mov.dura()) terminar();
                }
            }
        }
        // lo que se pidio antes de tiempo
        if (guardada >= 0) {
            if (ClientRol.ticks - guardadaTick > ESPERA_GUARDADA) guardada = -1;
            else if (libre()) {
                int g = guardada;
                guardada = -1;
                if (g == Armas.RODAR) {
                    if (suelto(mc)) rodar(mc, true);
                } else if (tipo != null) empezar(mc, tipo, g, guardadaCarga);
            }
        }
    }

    /** Si ya se puede empezar otra cosa (lo que queda del golpe es recuperarse). */
    private static boolean libre() {
        if (actual == null) return true;
        int t = ClientRol.ticks - actual.inicio;
        return switch (actual.accion) {
            case Armas.CARGA -> false;
            case Armas.SALTO -> actual.aterrizo >= 0 && ClientRol.ticks - actual.aterrizo >= 4;
            case Armas.RODAR, Armas.PASO_ATRAS -> t >= actual.mov.dura() - 2;
            default -> t >= actual.mov.libre();
        };
    }

    private static void terminar() {
        if (actual != null && (actual.accion == Armas.LIGERO || actual.accion == Armas.CORRIENDO || actual.accion == Armas.SALTO))
            cadenaHasta = ClientRol.ticks + VENTANA_CADENA;
        actual = null;
    }

    /** Se corta lo que se estaba haciendo (sin acabarlo). */
    private static void cortar() {
        actual = null;
        cargando = false;
        guardada = -1;
        if (pulsada >= 0) pulsada = -2;
    }

    private static void soltarTecla() {
        if (cargando) cortar();
        pulsada = -1;
    }

    // ------------------------------------------------------------------ pedir golpes

    private static void pedir(Minecraft mc, Armas.Tipo tipo, int accion, int carga) {
        if (accion == Armas.PESADO) {
            // el golpe fuerte sale de la carga (que ya tiene el arma arriba)
            empezar(mc, tipo, accion, carga);
            return;
        }
        if (!libre()) {
            guardada = accion;
            guardadaTick = ClientRol.ticks;
            guardadaCarga = carga;
            return;
        }
        empezar(mc, tipo, accion, carga);
    }

    private static void cargar(Minecraft mc, Armas.Tipo tipo) {
        empezar(mc, tipo, Armas.CARGA, 0);
    }

    private static void empezar(Minecraft mc, Armas.Tipo tipo, int accion, int carga) {
        LocalPlayer p = mc.player;
        if (Bebida.yo()) return;
        // con el escudo en alto (o tensando el arco) se ataca igual: se baja primero
        if (p.isUsingItem() && mc.gameMode != null) mc.gameMode.releaseUsingItem(p);
        if (ClientRol.agotado && accion != Armas.PESADO) {
            ClientRol.sinEstaminaTick = ClientRol.ticks;
            cargando = false;
            return;
        }
        int pasoUsado = 0;
        boolean izquierda = false;
        if (accion == Armas.LIGERO) {
            boolean sigue = ClientRol.ticks <= cadenaHasta
                    || (actual != null && (actual.accion == Armas.LIGERO || actual.accion == Armas.CORRIENDO || actual.accion == Armas.SALTO));
            if (!sigue) paso = 0;
            pasoUsado = paso;
            Armas.Mov m = tipo.mov(Armas.LIGERO, pasoUsado);
            izquierda = m.anim() == Armas.Anim.PUNO_I || (Armas.dobles(p) && pasoUsado % 2 == 1);
            paso++;
        } else if (accion == Armas.CORRIENDO || accion == Armas.SALTO) paso = 1;
        else if (accion == Armas.PESADO) paso = 0;
        Net.CH.sendToServer(new Net.Ataque(accion, pasoUsado, izquierda, Mth.clamp(carga, 0, Armas.CARGA_MAX)));
        Poses.Anim_ a = new Poses.Anim_();
        a.accion = accion;
        a.paso = pasoUsado;
        a.tipo = tipo;
        a.mov = tipo.mov(accion, pasoUsado);
        a.izquierda = izquierda;
        a.inicio = ClientRol.ticks;
        actual = a;
        cargando = accion == Armas.CARGA;
        cadenaHasta = -1000;
        if (accion != Armas.CORRIENDO) p.setSprinting(false);
        if (accion == Armas.CARGA) sonar(mc, DarkCraftMod.SFX_CARGA_GOLPE.get(), 0.8f, 1f);
        else if (accion == Armas.PESADO || accion == Armas.SALTO) sonar(mc, DarkCraftMod.SFX_ESFUERZO.get(), 0.7f, 0.95f + (float) Math.random() * 0.1f);
        if (accion != Armas.CARGA) hechos++;
        ultimo = nombre(accion) + "#" + pasoUsado + (izquierda ? " izq" : "") + (accion == Armas.PESADO ? " carga " + carga : "");
    }

    private static String nombre(int accion) {
        return switch (accion) {
            case Armas.LIGERO -> "ligero";
            case Armas.PESADO -> "pesado";
            case Armas.CORRIENDO -> "corriendo";
            case Armas.SALTO -> "salto";
            case Armas.CARGA -> "carga";
            case Armas.RODAR -> "rodar";
            case Armas.PASO_ATRAS -> "paso_atras";
            default -> "?";
        };
    }

    /** El silbido del arma, a su tiempo (los demas lo oyen por el servidor). */
    private static void silbido(Minecraft mc, Poses.Anim_ a) {
        boolean fuerte = a.accion == Armas.PESADO || a.tipo.dosManos;
        SoundEvent s = a.mov.estocada() ? DarkCraftMod.SFX_ESTOCADA.get() : fuerte ? DarkCraftMod.SFX_GOLPE_PESADO.get() : DarkCraftMod.SFX_GOLPE_LIGERO.get();
        sonar(mc, s, 0.8f, 0.92f + (float) Math.random() * 0.16f);
    }

    private static void sonar(Minecraft mc, SoundEvent s, float vol, float tono) {
        LocalPlayer p = mc.player;
        mc.getSoundManager().play(new SimpleSoundInstance(s, SoundSource.PLAYERS, vol, tono, p.getRandom(), p.getX(), p.getY() + 1, p.getZ()));
    }

    // ------------------------------------------------------------------ rodar

    /** Rodar hacia donde se anda (o un paso atras si no se anda): solo en el suelo, y no con demasiado peso. */
    private static void rodar(Minecraft mc, boolean guardadaYa) {
        LocalPlayer p = mc.player;
        if (!p.onGround() || p.isInWater() || p.isInLava() || Bebida.yo() || p.getAbilities().flying) return;
        if (ClientRol.agotado) {
            ClientRol.sinEstaminaTick = ClientRol.ticks;
            return;
        }
        int clase = clasePeso(p);
        if (clase == Peso.SOBRECARGADO) {
            Carteles.aviso(Component.translatable("darkcraft.aviso.sobrecargado"));
            return;
        }
        if (!guardadaYa && !libre()) {
            guardada = Armas.RODAR;
            guardadaTick = ClientRol.ticks;
            return;
        }
        var o = mc.options;
        float f = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0), s = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
        boolean atras = f == 0 && s == 0;
        float yaw = p.getYRot() * Mth.DEG_TO_RAD;
        float dx, dz;
        if (atras) {
            dx = Mth.sin(yaw);
            dz = -Mth.cos(yaw);
        } else {
            float n = Mth.sqrt(f * f + s * s);
            f /= n;
            s /= n;
            dx = s * Mth.cos(yaw) - f * Mth.sin(yaw);
            dz = f * Mth.cos(yaw) + s * Mth.sin(yaw);
        }
        if (p.isUsingItem() && mc.gameMode != null) mc.gameMode.releaseUsingItem(p);
        Net.CH.sendToServer(new Net.Rodar(dx, dz, atras));
        Poses.Anim_ a = new Poses.Anim_();
        a.accion = atras ? Armas.PASO_ATRAS : Armas.RODAR;
        a.paso = clase;
        a.tipo = Armas.Tipo.PUNOS;
        a.mov = atras ? Armas.PASO_ATRAS_MOV : Armas.rodar(clase);
        a.extra = Math.round((float) Math.toDegrees(Math.atan2(dz, dx)));
        a.inicio = ClientRol.ticks;
        actual = a;
        cargando = false;
        if (pulsada >= 0) pulsada = -2;
        rodarDx = dx;
        rodarDz = dz;
        rodarRapidez = atras ? Armas.PASO_ATRAS_RAPIDEZ : Armas.rapidezRodar(clase);
        p.setSprinting(false);
        sonar(mc, DarkCraftMod.SFX_RODAR.get(), 0.8f, 0.95f + (float) Math.random() * 0.1f);
        hechos++;
        ultimo = nombre(a.accion) + " peso " + clase;
        mover(p, a, 0);
    }

    /** La voltereta mueve al jugador: rapido al principio y frenando al final. */
    private static void mover(LocalPlayer p, Poses.Anim_ a, int t) {
        float k = t / (float) a.mov.dura();
        float v = rodarRapidez * (k < 0.6f ? 1f : 1f - (k - 0.6f) / 0.4f * 0.7f);
        Vec3 d = p.getDeltaMovement();
        p.setDeltaMovement(rodarDx * v, d.y, rodarDz * v);
    }

    // ------------------------------------------------------------------ lo que dice el servidor

    /** El servidor no ha dejado hacerlo (sin estamina, bebiendo, sin arma...): se corta. */
    public static void rechazado() {
        rechazados++;
        if (actual != null) {
            actual = null;
            ClientRol.sinEstaminaTick = ClientRol.ticks;
        }
        cargando = false;
        guardada = -1;
        if (pulsada >= 0) pulsada = -2;
    }

    /** El golpe ha dado a alguien: la camara tiembla y el golpe se para un instante (se nota el peso). */
    public static void impacto(boolean fuerte) {
        impactos++;
        sacudir(fuerte);
        if (actual != null && actual.accion != Armas.RODAR && actual.accion != Armas.PASO_ATRAS) actual.inicio += fuerte ? 2 : 1;
    }

    private static void sacudir(boolean fuerte) {
        temblor = fuerte ? 6 : 4;
        temblorFuerza = fuerte ? 1.6f : 0.8f;
    }

    // ------------------------------------------------------------------ moverse mientras se pega

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onInput(MovementInputUpdateEvent e) {
        Poses.Anim_ a = actual;
        if (a == null || e.getEntity() != Minecraft.getInstance().player) return;
        Input in = e.getInput();
        int t = ClientRol.ticks - a.inicio;
        float k = switch (a.accion) {
            case Armas.RODAR, Armas.PASO_ATRAS -> 0f;
            case Armas.CARGA -> 0.3f;
            case Armas.SALTO -> 1f;
            case Armas.CORRIENDO -> t <= a.mov.golpe1() ? 0.7f : 0.3f;
            default -> t < a.mov.libre() ? 0.2f : 0.5f;
        };
        in.forwardImpulse *= k;
        in.leftImpulse *= k;
        if (a.accion != Armas.SALTO) in.jumping = false;
        if (k == 0f) in.shiftKeyDown = false;
    }

    /**
     * En combate, el clic que mantiene el juego (picar el bloque que se mira) no rompe bloques con el arma; y en medio de
     * un golpe no se usa nada con el otro boton (el escudo sube al acabar el golpe, si se sigue pulsando).
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClic(InputEvent.InteractionKeyMappingTriggered e) {
        if (preguntando) return;
        Minecraft mc = Minecraft.getInstance();
        if (armado(mc) == null) return;
        if (e.isAttack() && (pulsada >= 0 || actual != null || !loDeSiempre(mc))) {
            e.setCanceled(true);
            e.setSwingHand(false);
        } else if (e.isUseItem() && actual != null && !rodando()) {
            e.setCanceled(true);
            e.setSwingHand(false);
        }
    }

    // ------------------------------------------------------------------ la camara

    @SubscribeEvent
    public static void camara(ViewportEvent.ComputeCameraAngles e) {
        float pt = (float) e.getPartialTick();
        if (temblor > 0) {
            float k = Math.max(0f, (temblor - pt) / 6f);
            float s = temblorFuerza * k;
            float t = ClientRol.ticks + pt;
            e.setRoll(e.getRoll() + Mth.sin(t * 9.1f) * s);
            e.setPitch(e.getPitch() + Mth.sin(t * 7.3f + 1f) * s * 0.6f);
        }
        Poses.Anim_ a = actual;
        Minecraft mc = Minecraft.getInstance();
        if (a != null && (a.accion == Armas.RODAR || a.accion == Armas.PASO_ATRAS) && mc.options.getCameraType() == CameraType.FIRST_PERSON
                && mc.getCameraEntity() == mc.player) {
            float k = Mth.clamp((ClientRol.ticks - a.inicio + pt) / a.mov.dura(), 0f, 1f);
            e.setPitch(e.getPitch() + (a.accion == Armas.RODAR ? 14f : -6f) * Mth.sin(k * Mth.PI));
        }
    }

    // ------------------------------------------------------------------ primera persona: el arma

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void mano(RenderHandEvent e) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        Poses.Anim_ a = actual;
        if (p == null || a == null) return;
        float t = ClientRol.ticks - a.inicio + e.getPartialTick();
        if (a.accion == Armas.RODAR || a.accion == Armas.PASO_ATRAS) {
            // las manos bajan mientras se rueda (lo que se mueve aqui vale para las dos)
            if (e.getHand() == InteractionHand.MAIN_HAND) {
                float k = Mth.sin(Mth.clamp(t / a.mov.dura(), 0f, 1f) * Mth.PI);
                e.getPoseStack().translate(0f, -0.5f * k, 0.1f * k);
            }
            return;
        }
        float[] r = Poses.fp(a, t);
        if (r == null) return;
        InteractionHand mano = a.izquierda ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack st = e.getItemStack();
        if (e.getHand() != mano) {
            // a dos manos, lo de la otra mano no estorba
            if (a.tipo.dosManos && !st.isEmpty()) e.setCanceled(true);
            return;
        }
        if (st.isEmpty()) return;
        e.setCanceled(true);
        boolean derecha = (p.getMainArm() == HumanoidArm.RIGHT) != a.izquierda;
        int l = derecha ? 1 : -1;
        PoseStack ps = e.getPoseStack();
        ps.pushPose();
        ps.translate(l * (0.56f + r[0]), -0.52f + r[1], -0.72f + r[2]);
        ps.mulPose(Axis.XP.rotationDegrees(r[3]));
        ps.mulPose(Axis.YP.rotationDegrees(l * r[4]));
        ps.mulPose(Axis.ZP.rotationDegrees(l * r[5]));
        mc.gameRenderer.itemInHandRenderer.renderItem(p, st, derecha ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                !derecha, ps, e.getMultiBufferSource(), e.getPackedLight());
        ps.popPose();
    }

    // ------------------------------------------------------------------ al salir

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        actual = null;
        cargando = false;
        pulsada = guardada = -1;
        paso = 0;
        cadenaHasta = -1000;
        temblor = 0;
        Poses.olvidar();
        Fijar.soltar();
    }

    /** Para la prueba de la CI. */
    public static String resumen() {
        Minecraft mc = Minecraft.getInstance();
        Armas.Tipo tipo = mc.player != null ? armado(mc) : null;
        String a = actual == null ? "nada" : nombre(actual.accion) + "#" + actual.paso + " t=" + (ClientRol.ticks - actual.inicio)
                + (actual.aterrizo >= 0 ? " aterrizo" : "");
        return "arma=" + (tipo == null ? "-" : tipo) + " ahora=" + a + " ultimo=" + ultimo + " hechos=" + hechos + " rechazados=" + rechazados
                + " impactos=" + impactos + " cadena=" + paso + (cargando ? " CARGANDO" : "") + " peso=" + (mc.player != null ? clasePeso(mc.player) : -1)
                + " fijado=" + Fijar.resumen();
    }
}
