package com.darkcraft.arranque;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Lo primero de Dark Craft que corre (parte 6): el "plugin" de su configuracion de mixins ({@code darkcraft.mixins.json},
 * en el manifiesto del jar). Mixin lo carga antes de que empiece el juego, y Dark Craft lo usa para dos cosas que no se
 * pueden hacer desde el mod (cuando se construyen los mods ya es tarde):
 * <ul>
 * <li>En el cliente, poner por defecto la lejania de Distant Horizons y los shaders ({@link PorDefecto}): Oculus lee su
 * configuracion al crear las opciones del juego, antes de construir los mods.</li>
 * <li>En un servidor dedicado, que Oculus (va dentro del jar y es solo del cliente) no lo tumbe: su clase de mod toca
 * pantallas y teclas que en un servidor no existen. Su mixin vacio ({@code OculusEnServidor}, solo en el lado del servidor)
 * hace que Mixin nos pase la clase {@code net.irisshaders.iris.Iris} antes de cargarla, y aqui se le quitan todos los
 * metodos salvo el inicializador estatico y un constructor vacio: Forge la construye sin mas y Oculus no hace nada.</li>
 * </ul>
 * No hay mixins de verdad: ninguna clase de Minecraft se toca. Nada de aqui puede tirar el juego.
 */
public final class Arranque implements IMixinConfigPlugin {
    static final Logger LOG = LoggerFactory.getLogger("Dark Craft (arranque)");
    static final String IRIS = "net.irisshaders.iris.Iris";

    private static boolean servidor() {
        try {
            return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void onLoad(String paquete) {
        if (servidor()) {
            LOG.info("Servidor dedicado: Oculus (si esta) se queda sin hacer nada; Distant Horizons y Embeddium van solos");
            return;
        }
        try {
            for (String s : PorDefecto.aplicar(FMLPaths.GAMEDIR.get()))
                LOG.info("Por defecto (primera vez): {}", s);
        } catch (Throwable t) {
            LOG.warn("No se han podido poner la lejania y los shaders por defecto", t);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String objetivo, String mixin) {
        return !IRIS.equals(objetivo) || servidor();
    }

    @Override
    public void acceptTargets(Set<String> mios, Set<String> otros) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String objetivo, ClassNode clase, String mixin, IMixinInfo info) {
        if (!IRIS.equals(objetivo) || !servidor()) return;
        try {
            vaciar(clase);
            LOG.info("Oculus en el servidor dedicado: {} vaciada ({} metodos)", objetivo, clase.methods.size());
        } catch (Throwable t) {
            LOG.warn("No se ha podido vaciar " + objetivo, t);
        }
    }

    /** Deja solo el inicializador estatico y un constructor que no hace nada (el de Object). */
    static void vaciar(ClassNode clase) {
        clase.methods.removeIf(m -> !m.name.equals("<clinit>") && !(m.name.equals("<init>") && m.desc.equals("()V")));
        boolean hay = clase.methods.stream().anyMatch(m -> m.name.equals("<init>"));
        MethodNode c = hay ? clase.methods.stream().filter(m -> m.name.equals("<init>")).findFirst().get()
                : new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        InsnList i = new InsnList();
        i.add(new VarInsnNode(Opcodes.ALOAD, 0));
        i.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clase.superName == null ? "java/lang/Object" : clase.superName, "<init>", "()V", false));
        i.add(new InsnNode(Opcodes.RETURN));
        c.instructions = i;
        c.tryCatchBlocks.clear();
        if (c.localVariables != null) c.localVariables.clear();
        c.maxStack = 1;
        c.maxLocals = 1;
        if (!hay) clase.methods.add(c);
    }

    @Override
    public void postApply(String objetivo, ClassNode clase, String mixin, IMixinInfo info) {}
}
