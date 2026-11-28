package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.dialogo.Conversaciones;
import com.darkcraft.dialogo.Dialogos;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Sigrun;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.morvain.Terreno;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Equipo;
import com.darkcraft.rol.Ficha;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * El duelo del Paso del Colmillo (parte 7ap; respuestas 95 y 117 del plan): la mision de <b>Sigrun, la Guardiana del Paso</b>
 * ({@link Sigrun}), de pie en el paso, al norte del santuario del Paso del Colmillo (en las Montanas Nevadas), con la espada clavada en
 * la nieve delante y las nueve espadas de sus hermanos clavadas en arco detras de ella.
 * <ol>
 * <li>"Alto. Nadie cruza el Colmillo mientras yo siga en pie... Soy Sigrun, Guardiana del Paso. Mi juramento me ata aqui... hasta que
 * alguien me derribe" (frase 1; las demas veces, 5: "Tu otra vez. ¿Has encontrado el valor?"). Se le responde: <b>el duelo</b> (2: "Entonces
 * desenvaina..."; al acabarla empieza: {@link #aceptar}), <b>por que sigue aqui</b> (3: los nueve dias, y otra vez las respuestas) o
 * <b>no</b> (4: "Entonces vuelve por donde has venido"). Pegarle tambien es aceptar el duelo.</li>
 * <li><b>El duelo</b>: pelea hasta rendirse de rodillas (no muere; "Basta... Me has vencido. El juramento... se ha roto", frase 6): a
 * cada uno de los que peleaban, el cartel de JEFE ABATIDO con sus ecos, la hazana {@code sigrun} y piedras de forja ({@link #vencida}). Si
 * se cae: "Has caido. No hay verguenza en eso... Siempre estoy aqui" (10) y vuelve a su puesto.</li>
 * <li>De rodillas (7: "...Ahora acaba conmigo... o dejame marchar. Tu eliges"): <b>acabar con ella</b> (8: "Gracias. Diles... que el
 * paso resistio": cae y se deshace en nieve; su espada, {@code espada_guardiana}; logro {@code sigrun_paso}) o <b>dejarla marchar</b>
 * (9: se va al monasterio a buscar los nombres de sus nueve hermanos; te da su escudo, {@code escudo_guardiana}; logro
 * {@code sigrun_nombres}). Lo que se elige vale para todos los que la vencieron y estan cerca.</li>
 * </ol>
 * Lo de cada jugador, en su ficha ({@code Ficha.mision}); a quien ya acabo su duelo no le toca verla. Sigrun y las espadas se ponen una
 * vez ({@code Morvain.Datos} "sigrun", "sigrun_espadas"); si ella faltara con su sitio cargado, se vuelve a poner.
 */
public final class Duelo {
    private Duelo() {}

    public static final String ID = "sigrun";
    /** Las marcas de la mision ({@code Ficha.mision}). */
    public static final int CONOCIDA = 1, ACEPTADA = 2, RECHAZADA = 4, HISTORIA = 8, CAIDO = 16, VENCIDA = 32, REMATADA = 64, LIBRE = 128;
    /** Acabada (de una de las dos maneras). */
    public static final int ACABADA = REMATADA | LIBRE;
    /** Su puesto: en el paso, 34 bloques al norte del santuario del Paso del Colmillo (-3132, -3588). */
    public static final int X = -3132, Z = -3622;
    /** Hacia donde mira (el giro de Minecraft: 0 = sur): hacia el santuario, por donde se llega. */
    public static final float GIRO = 0f;
    /** A esta distancia de su puesto se pone (y las espadas). */
    static final int CERCA = 64;
    /** Las nueve espadas: a cuantos bloques de ella, por detras, y lo que se abren a cada lado (grados). */
    static final double ESPADAS_R = 5.6, ESPADAS_ABRE = 72;
    public static final String TAG_ESPADA = "darkcraft_sigrun_espada";
    /** Lo que deja ademas de los ecos al vencerla: piedras de forja II. */
    public static final int PIEDRAS = 2;
    private static int falta;

    // ------------------------------------------------------------------ donde esta todo

    /** Su puesto (lo que pisa: encima del terreno). */
    public static BlockPos sitio() {
        return new BlockPos(X, (int) Math.floor(Terreno.columna(X, Z).altura()) + 1, Z);
    }

    /** Donde va la espada k (0-8) de sus hermanos: en arco detras de ella. */
    public static BlockPos espada(int k) {
        double a = Math.toRadians(-ESPADAS_ABRE + k * (2 * ESPADAS_ABRE / 8));
        double r = ESPADAS_R + (k % 2) * 1.3;
        // detras = al contrario de hacia donde mira (mira al sur: detras es el norte, -z)
        int x = (int) Math.round(X + Math.sin(a) * r), z = (int) Math.round(Z - Math.cos(a) * r);
        return new BlockPos(x, (int) Math.floor(Terreno.columna(x, z).altura()) + 1, z);
    }

    // ------------------------------------------------------------------ cada segundo: ponerla

    static void tick(ServerLevel m) {
        var srv = m.getServer();
        BlockPos s = sitio();
        if (!m.isPositionEntityTicking(s) || !alguien(m, s, CERCA)) {
            falta = 0;
            return;
        }
        if (!Morvain.hecho(srv, "sigrun_espadas")) ponerEspadas(m);
        java.util.List<Sigrun> todas = m.getEntitiesOfClass(Sigrun.class, new AABB(s).inflate(64, 32, 64));
        // (nunca dos)
        for (int i = 1; i < todas.size(); i++) todas.get(i).discard();
        if (!todas.isEmpty()) {
            Morvain.marcarHecho(srv, "sigrun");
            falta = 0;
            return;
        }
        // si falta con su sitio cargado (la primera vez, o algo se la llevo), se vuelve a poner
        if (!Morvain.hecho(srv, "sigrun") || ++falta > 3) {
            falta = 0;
            poner(m);
        }
    }

    private static boolean alguien(ServerLevel m, BlockPos p, int r) {
        for (ServerPlayer sp : m.players())
            if (!sp.isSpectator() && sp.blockPosition().distSqr(p) < (double) r * r) return true;
        return false;
    }

    /** Pone a Sigrun en su puesto, de pie con la espada clavada, mirando al santuario. */
    public static Sigrun poner(ServerLevel m) {
        BlockPos s = sitio();
        m.getChunk(s.getX() >> 4, s.getZ() >> 4);
        int y = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX(), s.getZ());
        if (Math.abs(y - s.getY()) > 3) y = s.getY();
        BlockPos pie = new BlockPos(s.getX(), y, s.getZ());
        despejar(m, pie);
        Sigrun g = DarkCraftMod.SIGRUN.get().create(m);
        if (g == null) return null;
        g.moveTo(pie.getX() + 0.5, pie.getY(), pie.getZ() + 0.5, GIRO, 0f);
        g.asignar("sigrun", -1, pie, GIRO, Enemigo.DE_PIE, 0);
        m.addFreshEntity(g);
        Morvain.marcarHecho(m.getServer(), "sigrun");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Sigrun, la Guardiana del Paso, en el Paso del Colmillo ({})", pie.toShortString());
        return g;
    }

    /** Lo que crece donde va (hierba, flores, nieve en capas) fuera: alrededor y de dos de alto. */
    private static void despejar(ServerLevel m, BlockPos pie) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int dy = 0; dy <= 2; dy++) {
                    BlockPos q = pie.offset(dx, dy, dz);
                    BlockState b = m.getBlockState(q);
                    if (!b.isAir() && b.canBeReplaced() && b.getFluidState().isEmpty()) m.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
                }
    }

    /**
     * Las nueve espadas de sus hermanos, clavadas en arco en la nieve detras de ella: espadas de hierro ({@code item_display}), cada una
     * con su giro y su inclinacion (siempre igual para el mismo sitio).
     */
    public static void ponerEspadas(ServerLevel m) {
        for (int k = 0; k < 9; k++) {
            BlockPos p = espada(k);
            m.getChunk(p.getX() >> 4, p.getZ() >> 4);
            double y = sueloEn(m, p);
            Display.ItemDisplay d = EntityType.ITEM_DISPLAY.create(m);
            if (d == null) continue;
            java.util.Random r = new java.util.Random(7717L * (k + 3));
            float giro = (float) Math.toRadians(-30 + r.nextInt(60) + k * 9);
            float ladeo = (float) Math.toRadians(r.nextInt(17) - 8);
            // el objeto del juego va de esquina a esquina: dado la vuelta (el juego le da media vuelta antes) y girado 135 grados, la
            // punta hacia abajo; se hunde algo menos de medio bloque en la nieve
            Matrix4f mt = new Matrix4f().translate(0f, 0.28f, 0f).rotateY(giro).rotateX(ladeo).rotateZ((float) Math.toRadians(135)).scale(1.1f);
            CompoundTag tag = new CompoundTag();
            tag.put("item", new ItemStack(Items.IRON_SWORD).save(new CompoundTag()));
            tag.putString("item_display", "none");
            ListTag l = new ListTag();
            for (int i = 0; i < 16; i++) l.add(FloatTag.valueOf(mt.getRowColumn(i >> 2, i & 3)));
            tag.put("transformation", l);
            tag.putFloat("view_range", 1.2f);
            d.load(tag);
            d.moveTo(p.getX() + 0.5, y, p.getZ() + 0.5, 0f, 0f);
            d.addTag(TAG_ESPADA);
            m.addFreshEntity(d);
        }
        Morvain.marcarHecho(m.getServer(), "sigrun_espadas");
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: las nueve espadas de los hermanos de Sigrun, en el Paso del Colmillo");
    }

    /** Lo alto de lo que se pisa en esa columna (lo que crece y la nieve fina no cuentan: no tienen choque). */
    private static double sueloEn(ServerLevel m, BlockPos p) {
        int top = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ());
        for (int y = top; y >= top - 4; y--) {
            BlockPos q = new BlockPos(p.getX(), y, p.getZ());
            var s = m.getBlockState(q).getCollisionShape(m, q);
            if (!s.isEmpty()) return y + s.max(net.minecraft.core.Direction.Axis.Y);
        }
        return top;
    }

    /** Sigrun, si su sitio esta cargado. */
    public static Sigrun buscar(ServerLevel m) {
        BlockPos s = sitio();
        if (!m.isLoaded(s)) return null;
        for (Sigrun g : m.getEntitiesOfClass(Sigrun.class, new AABB(s).inflate(64, 32, 64))) return g;
        return null;
    }

    /** Las espadas de sus hermanos que esten cargadas. */
    public static java.util.List<Display.ItemDisplay> espadas(ServerLevel m) {
        return m.getEntitiesOfClass(Display.ItemDisplay.class, new AABB(sitio()).inflate(16, 12, 16), d -> d.getTags().contains(TAG_ESPADA));
    }

    /** Para las pruebas y el comando: la quita (y las espadas cargadas) y olvida que se pusieron. */
    public static void olvidar(ServerLevel m) {
        Sigrun g = buscar(m);
        if (g != null) g.discard();
        for (Display.ItemDisplay d : espadas(m)) d.discard();
        Morvain.olvidarHecho(m.getServer(), "sigrun");
        Morvain.olvidarHecho(m.getServer(), "sigrun_espadas");
    }

    // ------------------------------------------------------------------ lo de cada jugador

    public static int marcas(ServerPlayer sp) {
        return Ficha.mision(sp, ID);
    }

    /** Si ya acabo su duelo (la remato o la dejo marchar): ya no le toca verla. */
    public static boolean acabado(ServerPlayer sp) {
        return (marcas(sp) & ACABADA) != 0;
    }

    /** Si puede retarla (no la ha vencido aun). */
    public static boolean puedeRetar(ServerPlayer sp) {
        return (marcas(sp) & (VENCIDA | ACABADA)) == 0 && !sp.isCreative();
    }

    /** Adonde tiene que ir (la brujula y el mapa): conociendola y sin acabar, a su puesto. */
    public static BlockPos objetivo(ServerPlayer sp) {
        int k = marcas(sp);
        return (k & CONOCIDA) != 0 && (k & ACABADA) == 0 ? sitio() : null;
    }

    /**
     * Por donde empieza a hablar (y lo apunta), o -1 si ahora no se le puede hablar: quien ya acabo su duelo no la ve; en duelo, caida
     * o marchandose, no habla; de rodillas, solo con quien la vencio (7). En paz: quien ya la vencio (se arrodilla otra vez, 7), quien
     * ya la conoce (5) o la primera vez (1).
     */
    public static int inicio(ServerPlayer sp, Sigrun s) {
        int k = marcas(sp);
        if ((k & ACABADA) != 0) return -1;
        int modo = s.modo();
        if (modo == Sigrun.RENDIDA) return (k & VENCIDA) != 0 ? 7 : -1;
        if (modo != Sigrun.PAZ || s.accion() != 0) return -1;
        if ((k & VENCIDA) != 0) {
            s.arrodillar(sp);
            return 7;
        }
        if ((k & CONOCIDA) != 0) return 5;
        Ficha.mision(sp, ID, k | CONOCIDA);
        Misiones.enviar(sp);
        return 1;
    }

    /** Se le habla (la E o el clic derecho; lo manda {@code Conversaciones}). */
    public static void hablar(ServerPlayer sp, Sigrun s) {
        if (!sp.isAlive() || sp.isSpectator() || sp.distanceToSqr(s) > (Conversaciones.ALCANCE + 1) * (Conversaciones.ALCANCE + 1)) return;
        Dialogos.Personaje d = Dialogos.de(ID);
        if (d == null) return;
        // en paz hay que estar fuera de combate (de rodillas no: se acaba de pelear con ella)
        if (s.modo() == Sigrun.PAZ && !sp.isCreative() && com.darkcraft.rol.Rol.enCombate(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.dialogo.combate", 0));
            return;
        }
        int nodo = inicio(sp, s);
        if (nodo < 0) return;
        Net.to(sp, new Net.Dialogo(s.getId(), ID, nodo));
        DarkCraftMod.LOGGER.info("[DarkCraft] {} habla con Sigrun (nodo {})", sp.getGameProfile().getName(), nodo);
    }

    /** Lo que se le responde (lo manda el cliente): el duelo, por que sigue aqui, no, acabar con ella o dejarla marchar. */
    public static void accion(ServerPlayer sp, Sigrun s, String accion) {
        int k = marcas(sp);
        if ((k & ACABADA) != 0) return;
        switch (accion) {
            case "duelo" -> aceptar(sp, s);
            case "precio" -> Ficha.mision(sp, ID, k | HISTORIA | CONOCIDA);
            case "rechazar" -> Ficha.mision(sp, ID, k | RECHAZADA | CONOCIDA);
            case "rematar" -> rematar(sp, s);
            case "soltar" -> soltar(sp, s);
            default -> {
                return;
            }
        }
        Misiones.enviar(sp);
    }

    /** Acepta el duelo: lo apunta y Sigrun desenvaina. */
    public static void aceptar(ServerPlayer sp, Sigrun s) {
        int k = marcas(sp);
        if ((k & (VENCIDA | ACABADA)) != 0 || s.modo() != Sigrun.PAZ) return;
        if ((k & ACEPTADA) == 0) Net.to(sp, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.sigrun.titulo", 0));
        Ficha.mision(sp, ID, (k | ACEPTADA | CONOCIDA) & ~RECHAZADA);
        Misiones.enviar(sp);
        s.retar(sp);
    }

    /** Cayo en el duelo (lo apunta: el diario lo dice). */
    public static void caido(ServerPlayer sp) {
        int k = marcas(sp);
        if ((k & CAIDO) != 0) return;
        Ficha.mision(sp, ID, k | CAIDO);
        Misiones.enviar(sp);
    }

    /**
     * Se ha rendido: a cada uno de los que la vencieron, lo apunta, el cartel de JEFE ABATIDO con sus ecos, la hazana, el bestiario y
     * dos piedras de forja II; y dice su frase (6).
     */
    public static void vencida(Sigrun s, ServerLevel sl, java.util.List<ServerPlayer> quienes) {
        for (ServerPlayer p : quienes) {
            int k = marcas(p);
            if ((k & VENCIDA) == 0) {
                Ficha.mision(p, ID, k | VENCIDA | CONOCIDA);
                com.darkcraft.rol.Rol.jefe(p, "entity.darkcraft.sigrun", Sigrun.ECOS);
                Equipo.dar(p, new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), PIEDRAS));
            }
            Logros.dar(p, "sigrun");
            com.darkcraft.espejo.Espejo.bestia(p, "sigrun");
            Misiones.enviar(p);
        }
        decir(s, sl, 6, 48);
    }

    /** Los que la vencieron, estan cerca y aun no han acabado su duelo (con quien elige): a todos ellos les vale lo que se elige. */
    private static java.util.List<ServerPlayer> afectados(ServerPlayer sp, Sigrun s) {
        java.util.List<ServerPlayer> l = new java.util.ArrayList<>();
        l.add(sp);
        for (ServerPlayer p : sp.serverLevel().players())
            if (p != sp && s.vencedores().contains(p.getUUID()) && p.distanceToSqr(s) < 48 * 48 && (marcas(p) & VENCIDA) != 0 && !acabado(p)) l.add(p);
        return l;
    }

    /** Acabar con ella: cae y se deshace en nieve; su espada para cada uno. */
    public static void rematar(ServerPlayer sp, Sigrun s) {
        if ((marcas(sp) & VENCIDA) == 0 || acabado(sp) || (s.modo() != Sigrun.RENDIDA && s.modo() != Sigrun.PAZ)) return;
        for (ServerPlayer p : afectados(sp, s)) {
            Ficha.mision(p, ID, marcas(p) | REMATADA);
            Equipo.dar(p, new ItemStack(DarkCraftMod.ESPADA_GUARDIANA.get()));
            Logros.dar(p, "sigrun_paso");
            Net.to(p, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.sigrun.titulo", 1));
            Misiones.enviar(p);
        }
        s.caer();
        DarkCraftMod.LOGGER.info("[DarkCraft] {} acaba con Sigrun (el paso resistio)", sp.getGameProfile().getName());
    }

    /** Dejarla marchar: se va al monasterio a buscar los nombres de sus hermanos; su escudo para cada uno. */
    public static void soltar(ServerPlayer sp, Sigrun s) {
        if ((marcas(sp) & VENCIDA) == 0 || acabado(sp) || (s.modo() != Sigrun.RENDIDA && s.modo() != Sigrun.PAZ)) return;
        for (ServerPlayer p : afectados(sp, s)) {
            Ficha.mision(p, ID, marcas(p) | LIBRE);
            Equipo.dar(p, new ItemStack(DarkCraftMod.ESCUDOS.get(com.darkcraft.armas.Escudo.Tamano.GUARDIANA).get()));
            Logros.dar(p, "sigrun_nombres");
            Net.to(p, new Net.Cartel(Net.Cartel.MISION, "darkcraft.mision.sigrun.titulo", 1));
            Misiones.enviar(p);
        }
        s.marchar(sp);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} deja marchar a Sigrun", sp.getGameProfile().getName());
    }

    /** Dice su frase {@code n} en el mundo (con su voz, su retrato y el texto) a los que esten cerca y aun la vean. */
    public static void decir(Sigrun s, ServerLevel sl, int n, double r) {
        for (ServerPlayer p : sl.players())
            if (p.distanceToSqr(s) < r * r && !acabado(p)) Net.to(p, new Net.Cartel(Net.Cartel.VOZ, ID, n));
        DICHAS++;
    }

    /** Para las pruebas: las frases que ha dicho en el mundo desde que arranco el juego. */
    public static int DICHAS;

    /** Para las pruebas y el comando: como va la mision de cada uno. */
    public static String resumen(ServerPlayer sp) {
        int k = marcas(sp);
        StringBuilder b = new StringBuilder("sigrun ").append(k).append(" [");
        String[] n = {"conocida", "aceptada", "rechazada", "historia", "caido", "vencida", "rematada", "libre"};
        for (int i = 0; i < n.length; i++) if ((k & (1 << i)) != 0) b.append(' ').append(n[i]);
        b.append(" ]");
        BlockPos o = objetivo(sp);
        if (o != null) b.append(", ir a ").append(o.toShortString());
        return b.toString();
    }

    /** Lleva al jugador a Morvain, delante de ella (a seis bloques al sur, mirandola). */
    public static void llevar(ServerPlayer sp, ServerLevel m) {
        BlockPos s = sitio();
        m.getChunk(s.getX() >> 4, s.getZ() >> 4);
        int x = s.getX(), z = s.getZ() + 6;
        m.getChunk(x >> 4, z >> 4);
        int y = m.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        Sigrun g = buscar(m);
        double ax = g != null ? g.getX() : s.getX() + 0.5, az = g != null ? g.getZ() : s.getZ() + 0.5, ay = g != null ? g.getY() + 1.6 : s.getY() + 1.6;
        double dx = ax - (x + 0.5), dz = az - (z + 0.5), dy = ay - (y + 1.62);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz)), pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        sp.teleportTo(m, x + 0.5, y, z + 0.5, yaw, pitch);
    }
}
