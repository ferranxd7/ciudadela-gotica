package com.darkcraft.client;

import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Distant Horizons (la lejania) y Oculus (los shaders), que van dentro del jar de Dark Craft desde la parte 6. Todo por
 * reflexion y sin tirar nunca: el mod funciona igual si alguno no esta (en el entorno de desarrollo, por ejemplo).
 *
 * <p>Distant Horizons por su API ({@code DhApi.Delayed.configs}): lo que se pone con {@link #dhPoner} es un valor "de la
 * API" que manda sobre el del jugador mientras dura la sesion y no se guarda; {@link #dhQuitar} lo devuelve al suyo.
 * Oculus por la API de Iris ({@code IrisApi.getInstance().getConfig()}).</p>
 */
public final class Lejania {
    private Lejania() {}

    public static boolean hayDH() {
        return ModList.get() != null && ModList.get().isLoaded("distanthorizons");
    }

    public static boolean hayOculus() {
        return ModList.get() != null && ModList.get().isLoaded("oculus");
    }

    public static String version(String mod) {
        if (ModList.get() == null) return "-";
        return ModList.get().getModContainerById(mod).map(c -> c.getModInfo().getVersion().toString()).orElse("no esta");
    }

    // ------------------------------------------------------------------ Distant Horizons

    private static Object configs() throws Exception {
        Class<?> d = Class.forName("com.seibel.distanthorizons.api.DhApi$Delayed");
        return d.getField("configs").get(null);
    }

    /** Sin argumentos, el metodo publico con ese nombre (en la clase o en sus interfaces). */
    private static Object llamar(Object o, String nombre) throws Exception {
        return invocar(o, nombre, 0, null);
    }

    /**
     * El metodo publico con ese nombre y tantos argumentos. Si la clase que lo tiene no es publica, por la interfaz que lo
     * declara (asi no hace falta abrir nada).
     */
    private static Object invocar(Object o, String nombre, int n, Object arg) throws Exception {
        Method m = null;
        for (Method x : o.getClass().getMethods())
            if (x.getName().equals(nombre) && x.getParameterCount() == n) {
                m = x;
                break;
            }
        if (m == null) throw new NoSuchMethodException(o.getClass().getName() + "." + nombre + " (hay: " + metodos(o) + ")");
        Object[] args = n == 0 ? new Object[0] : new Object[]{arg};
        try {
            return m.invoke(o, args);
        } catch (IllegalAccessException e) {
            for (Class<?> i : interfaces(o.getClass()))
                for (Method x : i.getMethods())
                    if (x.getName().equals(nombre) && x.getParameterCount() == n) return x.invoke(o, args);
            m.setAccessible(true);
            return m.invoke(o, args);
        }
    }

    private static List<Class<?>> interfaces(Class<?> c) {
        List<Class<?>> l = new ArrayList<>();
        for (Class<?> k = c; k != null; k = k.getSuperclass())
            for (Class<?> i : k.getInterfaces()) {
                l.add(i);
                l.addAll(interfaces(i));
            }
        return l;
    }

    private static String metodos(Object o) {
        List<String> l = new ArrayList<>();
        for (Method m : o.getClass().getMethods())
            if (m.getDeclaringClass() != Object.class) l.add(m.getName() + "/" + m.getParameterCount());
        return String.join(" ", l);
    }

    /** La opcion de DH en esa ruta ("graphics", "chunkRenderDistance"): el objeto IDhApiConfigValue. */
    private static Object opcion(String... ruta) throws Exception {
        Object o = configs();
        if (o == null) throw new IllegalStateException("DH aun no ha arrancado");
        for (String r : ruta) o = llamar(o, r);
        return o;
    }

    /** Lo que vale ahora (con el valor de la API si lo hay), o null. */
    public static Object dh(String... ruta) {
        try {
            return llamar(opcion(ruta), "getValue");
        } catch (Throwable t) {
            return null;
        }
    }

    /** Lo que ha puesto el jugador (sin el valor de la API), o null. */
    public static Object dhDelJugador(String... ruta) {
        try {
            return llamar(opcion(ruta), "getTrueValue");
        } catch (Throwable t) {
            return null;
        }
    }

    /** Pone un valor de la API (no se guarda). Devuelve lo que ha pasado, para el registro. */
    public static String dhPoner(Object valor, String... ruta) {
        try {
            invocar(opcion(ruta), "setValue", 1, valor);
            return String.join(".", ruta) + "=" + valor;
        } catch (Throwable t) {
            return String.join(".", ruta) + ": " + t;
        }
    }

    /** Quita el valor de la API: vuelve a mandar el del jugador. */
    public static String dhQuitar(String... ruta) {
        try {
            llamar(opcion(ruta), "clearValue");
            return String.join(".", ruta) + " del jugador";
        } catch (Throwable t) {
            return String.join(".", ruta) + ": " + t;
        }
    }

    /** Para el registro: la lejania que hay, la del jugador y si dibuja. */
    public static String resumenDH() {
        if (!hayDH()) return "Distant Horizons no esta";
        try {
            Object c = configs();
            if (c == null) return "Distant Horizons " + version("distanthorizons") + " (aun sin arrancar)";
        } catch (Throwable t) {
            return "Distant Horizons " + version("distanthorizons") + ": sin API (" + t + ")";
        }
        return "Distant Horizons " + version("distanthorizons") + ": lejania " + dh("graphics", "chunkRenderDistance")
                + " (la del jugador " + dhDelJugador("graphics", "chunkRenderDistance") + "), dibuja "
                + dh("graphics", "renderingEnabled") + ", genera " + dh("worldGenerator", "enableDistantGeneration");
    }

    // ------------------------------------------------------------------ Oculus

    private static Object irisConfig() throws Exception {
        Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
        Object inst = api.getMethod("getInstance").invoke(null);
        return api.getMethod("getConfig").invoke(inst);
    }

    public static boolean shadersEncendidos() {
        try {
            return (boolean) llamar(irisConfig(), "areShadersEnabled");
        } catch (Throwable t) {
            return false;
        }
    }

    /** Si hay un pack de shaders funcionando ahora mismo. */
    public static boolean shadersEnUso() {
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object inst = api.getMethod("getInstance").invoke(null);
            return (boolean) api.getMethod("isShaderPackInUse").invoke(inst);
        } catch (Throwable t) {
            return false;
        }
    }

    /** Enciende o apaga los shaders (y se guarda, como el boton de Oculus). */
    public static String shaders(boolean si) {
        try {
            invocar(irisConfig(), "setShadersEnabledAndApply", 1, si);
            return "shaders " + (si ? "encendidos" : "apagados");
        } catch (Throwable t) {
            return "shaders: " + t;
        }
    }

    /** El pack elegido en Oculus (o "-"). */
    public static String pack() {
        try {
            Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
            Object o = llamar(iris.getMethod("getIrisConfig").invoke(null), "getShaderPackName");
            return o instanceof Optional<?> op ? op.map(Object::toString).orElse("-") : String.valueOf(o);
        } catch (Throwable t) {
            return "-";
        }
    }

    public static String resumenOculus() {
        if (!hayOculus()) return "Oculus no esta";
        return "Oculus " + version("oculus") + ": pack " + pack() + ", encendidos " + shadersEncendidos() + ", en uso " + shadersEnUso()
                + "; Embeddium " + version("embeddium");
    }
}
