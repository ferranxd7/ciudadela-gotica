package com.darkcraft.pueblo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.Canvas;
import com.darkcraft.net.Net;
import com.darkcraft.pueblo.gen.Barco;
import com.darkcraft.pueblo.gen.Capilla;
import com.darkcraft.pueblo.gen.PuebloPlan;
import com.darkcraft.pueblo.gen.Terreno;
import com.darkcraft.rol.Rol;
import com.darkcraft.santuario.Santuarios;
import com.darkcraft.story.Logros;
import com.darkcraft.world.MundoNubes;
import com.darkcraft.world.NubesGenerador;
import com.darkcraft.world.PuebloFeature;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Brumavela, la isla del pueblo abandonado donde se despierta (parte 3b): lo que pasa en ella. Las cinco piezas de La
 * Garza Gris (la vela en el arcon del almacen, el timon en la taberna, el mastil en la cripta de la capilla, el ancla
 * en el Saliente y el corazon en lo alto del faro), las tres campanas que abren la cripta (en el orden en que murieron
 * los fundadores: lo dicen sus lapidas), reparar el barco y zarpar hacia Valdrenn.
 * <p>
 * Todo es <b>de todos</b>: en un servidor, la pieza que encuentra uno cuenta para el grupo y el barco se repara una vez
 * (SavedData {@code darkcraft_pueblo}). Las cosas que se usan (las "cosas": un arcon, una campana, una lapida, el
 * barco) van con la tecla del aviso ("Pulsa [E] para..."): el servidor manda al cliente la lista de las que hay
 * ({@link Net.Cosas}) y el cliente pide usarlas ({@link Net.CosaUsar}); asi funcionan igual dentro de la proteccion del
 * punto de aparicion de un servidor dedicado. Las piezas que se ven son entidades {@code item_display} que no se guardan.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Pueblo {
    private Pueblo() {}

    public enum Pieza {
        VELA, TIMON, MASTIL, ANCLA, CORAZON;

        public String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public String clave() {
            return "darkcraft.pieza." + id();
        }

        public Item item() {
            return switch (this) {
                case VELA -> DarkCraftMod.PIEZA_VELA.get();
                case TIMON -> DarkCraftMod.PIEZA_TIMON.get();
                case MASTIL -> DarkCraftMod.PIEZA_MASTIL.get();
                case ANCLA -> DarkCraftMod.PIEZA_ANCLA.get();
                default -> DarkCraftMod.PIEZA_CORAZON.get();
            };
        }
    }

    public static final int TODAS = (1 << Pieza.values().length) - 1;

    // ---- las cosas que se usan con la tecla del aviso: las cinco piezas (0-4) y...
    public static final int CAMPANA = 5, LAPIDA = 8, REPARAR = 11, ZARPAR = 12, VOLVER = 13, COSAS = 14;
    /** Lo que dice el aviso de cada cosa ("Pulsa [E] para " + darkcraft.aviso.cosa.N). */
    public static final int ABRIR = 0, RECOGER = 1, REGISTRAR = 2, TOCAR = 3, LEER = 4, A_REPARAR = 5, A_ZARPAR = 6, A_VOLVER = 7;
    private static final String[] QUIEN = {"pescador", "tejedora", "farero"};
    /** El orden en que murieron los fundadores: el Pescador, la Tejedora y el Farero. */
    private static final int[] ORDEN = {0, 1, 2};
    private static final float[] TONO = {0.72f, 0.94f, 1.18f};
    private static final double ALCANCE = 4.6;
    static final String TAG = "darkcraft_pieza";

    public record Cosa(int id, int x, int y, int z, int accion) {}

    // ------------------------------------------------------------------ donde

    public static boolean mundo(Level l) {
        return l instanceof ServerLevel sl && sl.dimension() == Level.OVERWORLD && NubesGenerador.es(sl);
    }

    /** Si el jugador esta en Brumavela (la isla, la cala, el espigon) o volando cerca. */
    public static boolean en(ServerPlayer p) {
        return mundo(p.level()) && PuebloFeature.dentro(p.getX(), p.getZ()) && p.getY() > 200;
    }

    private static int[] sitio(String nombre) {
        return PuebloPlan.get().sitio(nombre);
    }

    private static BlockPos pos(String nombre) {
        int[] s = sitio(nombre);
        return new BlockPos(s[0], s[1], s[2]);
    }

    /** Donde esta una cosa (el bloque), o null si no existe ahora. */
    private static int[] donde(ServerLevel lvl, int id) {
        if (id < Pieza.values().length) return sitio(Pieza.values()[id].id());
        if (id >= CAMPANA && id < CAMPANA + 3) return sitio("campana_" + QUIEN[id - CAMPANA]);
        if (id >= LAPIDA && id < LAPIDA + 3) return sitio("lapida_" + QUIEN[id - LAPIDA]);
        if (id == REPARAR) return sitio("barco");
        if (id == ZARPAR) return sitio("barco_timon");
        if (id == VOLVER) {
            BlockPos l = noray(lvl);
            return l == null ? null : new int[]{l.getX(), l.getY(), l.getZ()};
        }
        return null;
    }

    /** El noray de la llegada a Valdrenn (desde el se vuelve a Brumavela), a un lado de la plazoleta de la torre. */
    private static BlockPos noray(ServerLevel lvl) {
        BlockPos l = MundoNubes.llegada(lvl.getSeed());
        return l == null ? null : l.offset(3, 0, 2);
    }

    /** Las cosas que hay ahora mismo. */
    public static List<Cosa> cosas(ServerLevel lvl) {
        Datos d = Datos.get(lvl.getServer());
        List<Cosa> r = new ArrayList<>();
        for (int id = 0; id < COSAS; id++) {
            int accion;
            if (id < Pieza.values().length) {
                if ((d.piezas & (1 << id)) != 0 || d.reparado) continue;
                if (id == Pieza.MASTIL.ordinal() && !d.cripta) continue;
                accion = id == Pieza.VELA.ordinal() ? ABRIR : id == Pieza.ANCLA.ordinal() ? REGISTRAR : RECOGER;
            } else if (id < LAPIDA) accion = TOCAR;
            else if (id < REPARAR) accion = LEER;
            else if (id == REPARAR) {
                if (d.reparado) continue;
                accion = A_REPARAR;
            } else {
                if (!d.reparado) continue;
                accion = id == ZARPAR ? A_ZARPAR : A_VOLVER;
            }
            int[] s = donde(lvl, id);
            if (s != null) r.add(new Cosa(id, s[0], s[1], s[2], accion));
        }
        return r;
    }

    // ------------------------------------------------------------------ lo que se guarda

    public static final class Datos extends SavedData {
        public static final String ID = "darkcraft_pueblo";
        public int piezas;
        public boolean reparado, cripta, santuario;
        /** Sube cada vez que cambia algo que el cliente tiene que saber. */
        int version = 1;

        public static Datos get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Datos::load, Datos::new, ID);
        }

        static Datos load(CompoundTag t) {
            Datos d = new Datos();
            d.piezas = t.getInt("piezas");
            d.reparado = t.getBoolean("reparado");
            d.cripta = t.getBoolean("cripta");
            d.santuario = t.getBoolean("santuario");
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            t.putInt("piezas", piezas);
            t.putBoolean("reparado", reparado);
            t.putBoolean("cripta", cripta);
            t.putBoolean("santuario", santuario);
            return t;
        }

        void cambio() {
            version++;
            setDirty();
        }
    }

    /** La version de la lista de cosas que tiene cada jugador. */
    private static final Map<UUID, Integer> ENVIADO = new HashMap<>();
    private static final EnumMap<Pieza, Display.ItemDisplay> VISTAS = new EnumMap<>(Pieza.class);
    /** Las campanas: cuantas van bien seguidas y cuando sono la ultima. */
    private static int seguidas;
    private static long ultimoToque;

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        ENVIADO.clear();
        VISTAS.clear();
        seguidas = 0;
    }

    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        ENVIADO.remove(e.getEntity().getUUID());
    }

    /** Una pieza que se quedo guardada en el disco (el mundo se cerro con ella a la vista) no vuelve a entrar. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent ev) {
        if (ev.getLevel().isClientSide() || !ev.loadedFromDisk()) return;
        if (ev.getEntity() instanceof Display && ev.getEntity().getTags().contains(TAG)) ev.setCanceled(true);
    }

    // ------------------------------------------------------------------ cada segundo

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 20 != 11) return;
        boolean aqui = en(p);
        ServerLevel lvl = p.serverLevel();
        boolean llegada = false;
        if (!aqui && mundo(lvl)) {
            BlockPos n = Datos.get(p.server).reparado ? noray(lvl) : null;
            llegada = n != null && n.distSqr(p.blockPosition()) < 96 * 96;
            if (llegada) ponerNoray(lvl, n);
        }
        if (!aqui && !llegada) {
            if (ENVIADO.remove(p.getUUID()) != null) Net.to(p, new Net.Cosas(List.of()));
            return;
        }
        Datos d = Datos.get(p.server);
        Integer v = ENVIADO.get(p.getUUID());
        if (v == null || v != d.version) {
            ENVIADO.put(p.getUUID(), d.version);
            Net.to(p, new Net.Cosas(cosas(lvl)));
        }
        if (!aqui) return;
        if (!d.santuario) santuario(lvl, d);
        Logros.dar(p, "brumavela");
        // el nombre del sitio (la primera vez, el de la isla)
        String zona = "darkcraft.zona.brumavela";
        Rol.zona(p, Rol.zonaVista(p, zona) ? lugar(p) : zona);
    }

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.getServer().getTickCount() % 20 != 3) return;
        ServerLevel lvl = e.getServer().overworld();
        if (!mundo(lvl)) return;
        Datos d = Datos.get(e.getServer());
        for (Pieza q : Pieza.values()) {
            int[] s = sitio(q.id());
            BlockPos b = new BlockPos(s[0], s[1], s[2]);
            boolean falta = (d.piezas & (1 << q.ordinal())) == 0 && !d.reparado;
            boolean cerca = falta && lvl.hasChunkAt(b) && lvl.getNearestPlayer(s[0] + 0.5, s[1] + 0.5, s[2] + 0.5, 72, false) != null;
            Display.ItemDisplay v = VISTAS.get(q);
            if (!cerca || q == Pieza.VELA) {
                if (v != null) {
                    v.discard();
                    VISTAS.remove(q);
                }
            } else if (v == null || v.isRemoved()) {
                v = vista(lvl, q, s);
                if (v != null) VISTAS.put(q, v);
            }
            // el brillo de lo que falta por encontrar
            if (cerca && (q != Pieza.MASTIL || d.cripta))
                lvl.sendParticles(ParticleTypes.WAX_ON, s[0] + 0.5, s[1] + (q == Pieza.CORAZON ? 0.5 : 1.0), s[2] + 0.5, 3, 0.35, 0.4, 0.35, 0.02);
        }
        // el corazon del barco reparado late en la bodega
        if (d.reparado) {
            int[] s = sitio("barco_corazon");
            Display.ItemDisplay v = VISTAS.get(Pieza.CORAZON);
            boolean cerca = lvl.hasChunkAt(new BlockPos(s[0], s[1], s[2])) && lvl.getNearestPlayer(s[0] + 0.5, s[1], s[2] + 0.5, 72, false) != null;
            if (cerca && (v == null || v.isRemoved())) {
                v = vista(lvl, Pieza.CORAZON, new int[]{s[0], s[1] - 1, s[2]});
                if (v != null) VISTAS.put(Pieza.CORAZON, v);
            } else if (!cerca && v != null) {
                v.discard();
                VISTAS.remove(Pieza.CORAZON);
            }
            if (cerca) lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, s[0] + 0.5, s[1] + 0.4, s[2] + 0.5, 1, 0.2, 0.2, 0.2, 0.005);
        }
    }

    private static void santuario(ServerLevel lvl, Datos d) {
        BlockPos s = pos("santuario");
        if (!lvl.hasChunkAt(s)) return;
        Santuarios.poner(lvl, s, "darkcraft.santuario.brumavela");
        d.santuario = true;
        d.setDirty();
        DarkCraftMod.LOGGER.info("[DarkCraft] Santuario de Brumavela puesto en {}", s.toShortString());
    }

    /** La pieza que se ve: el dibujo del objeto, de cara a quien mira, con luz propia. */
    private static Display.ItemDisplay vista(ServerLevel lvl, Pieza q, int[] s) {
        Display.ItemDisplay d = EntityType.ITEM_DISPLAY.create(lvl);
        if (d == null) return null;
        float alto = q == Pieza.CORAZON ? 0.55f : 1.1f, tam = q == Pieza.CORAZON ? 1.0f : 1.25f;
        // (el juego dibuja el objeto dado la vuelta: media vuelta para que quede como en su textura)
        Matrix4f m = new Matrix4f().translate(0f, alto, 0f).rotateY((float) Math.PI).scale(tam);
        CompoundTag tag = new CompoundTag();
        tag.put("item", new ItemStack(q.item()).save(new CompoundTag()));
        tag.putString("item_display", "none");
        tag.putString("billboard", "vertical");
        ListTag l = new ListTag();
        for (int i = 0; i < 16; i++) l.add(FloatTag.valueOf(m.getRowColumn(i >> 2, i & 3)));
        tag.put("transformation", l);
        CompoundTag luz = new CompoundTag();
        luz.putInt("sky", 15);
        luz.putInt("block", 15);
        tag.put("brightness", luz);
        tag.putFloat("view_range", 1.2f);
        d.load(tag);
        d.moveTo(s[0] + 0.5, s[1], s[2] + 0.5, 0f, 0f);
        d.addTag(TAG);
        lvl.addFreshEntity(d);
        return d;
    }

    // ------------------------------------------------------------------ el nombre de cada sitio

    /** El sitio de Brumavela donde esta el jugador (clave de idioma), o null si no esta en la isla. */
    public static String lugar(ServerPlayer p) {
        if (!en(p)) return null;
        PuebloPlan plan = PuebloPlan.get();
        double x = p.getX(), y = p.getY(), z = p.getZ();
        String l = "darkcraft.mapa.lugar.bv_";
        if (x >= Capilla.CX0 - 1 && x <= Capilla.CX1 + 2 && z >= Capilla.CZ0 - 1 && z <= Capilla.CZ1 + 2 && y < plan.capilla.y - 1.5 && y > plan.capilla.cripta - 2)
            return l + "cripta";
        if (Math.hypot(x - Terreno.COLINA_X, z - Terreno.COLINA_Z) < 21 || (x >= -68 && x <= -36 && z >= -40 && z <= -19)) return l + "capilla";
        if (Math.hypot(x - Terreno.PENON_X, z - Terreno.PENON_Z) < Terreno.PENON_R + 1) return l + "faro";
        if (z >= 84 && (plan.terreno.alto((int) Math.floor(x), (int) Math.floor(z)) == Terreno.NADA || y < Terreno.Q + 8)) return l + "muelle";
        if (Math.abs(x) <= Terreno.CUESTA_ANCHO + 2 && z >= Terreno.CUESTA_Z0 - 1 && z < 84 && y < Terreno.P + 4) return l + "cuesta";
        if (x < -100) return l + "saliente";
        if (plan.taberna.fp.contains((int) Math.floor(x), (int) Math.floor(z))) return l + "taberna";
        if (Math.hypot(x - PuebloPlan.PLAZA_X, z - PuebloPlan.PLAZA_Z) < PuebloPlan.PLAZA_R + 3) return l + "plaza";
        if (x >= 24 && x <= 68 && z >= -56 && z <= -32) return l + "campos";
        return "darkcraft.zona.brumavela";
    }

    // ------------------------------------------------------------------ usar una cosa

    /** El jugador pulsa la tecla del aviso junto a una cosa (o le hace clic derecho a su bloque). */
    public static void usar(ServerPlayer p, int id) {
        if (!p.isAlive() || p.isSpectator() || !mundo(p.level()) || id < 0 || id >= COSAS) return;
        ServerLevel lvl = p.serverLevel();
        Cosa c = null;
        for (Cosa k : cosas(lvl)) if (k.id() == id) c = k;
        if (c == null) return;
        if (p.getEyePosition().distanceTo(new net.minecraft.world.phys.Vec3(c.x() + 0.5, c.y() + 0.5, c.z() + 0.5)) > ALCANCE + 1.2) return;
        Datos d = Datos.get(p.server);
        if (id < Pieza.values().length) tomar(p, Pieza.values()[id]);
        else if (id < LAPIDA) tocar(p, id - CAMPANA, c);
        else if (id < REPARAR) Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.lapida." + QUIEN[id - LAPIDA], 0));
        else if (id == REPARAR) {
            if (d.piezas == TODAS) reparar(p);
            else Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.faltan." + (Pieza.values().length - Integer.bitCount(d.piezas)), 0));
        } else if (id == ZARPAR) zarpar(p);
        else if (id == VOLVER) volver(p);
    }

    /** Todos los que estan en la isla. */
    private static List<ServerPlayer> presentes(MinecraftServer srv) {
        List<ServerPlayer> r = new ArrayList<>();
        for (ServerPlayer q : srv.getPlayerList().getPlayers()) if (en(q)) r.add(q);
        return r;
    }

    /** Alguien encuentra una pieza: cuenta para todos. */
    public static void tomar(ServerPlayer p, Pieza q) {
        ServerLevel lvl = p.serverLevel();
        Datos d = Datos.get(p.server);
        if ((d.piezas & (1 << q.ordinal())) != 0) return;
        d.piezas |= 1 << q.ordinal();
        d.cambio();
        int[] s = sitio(q.id());
        Display.ItemDisplay v = VISTAS.remove(q);
        if (v != null) v.discard();
        double x = s[0] + 0.5, y = s[1] + 1.0, z = s[2] + 0.5;
        lvl.sendParticles(ParticleTypes.END_ROD, x, y, z, 24, 0.25, 0.35, 0.25, 0.05);
        lvl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        lvl.playSound(null, x, y, z, DarkCraftMod.SFX_PIEZA.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        BlockPos b = new BlockPos(s[0], s[1], s[2]);
        if (q == Pieza.VELA) {
            // el arcon se queda abierto
            lvl.blockEvent(b, Blocks.CHEST, 1, 1);
            lvl.playSound(null, b, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.7f, 0.8f);
        } else if (q == Pieza.CORAZON) {
            // el faro se apaga
            if (lvl.getBlockState(b).is(Blocks.LIGHT)) lvl.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
            lvl.playSound(null, b, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), SoundSource.BLOCKS, 1.0f, 0.6f);
        } else if (q == Pieza.ANCLA) lvl.playSound(null, b, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.2f, 0.6f);
        int n = Integer.bitCount(d.piezas);
        for (ServerPlayer o : presentes(p.server)) Net.to(o, new Net.Cartel(Net.Cartel.PIEZA, q.clave(), n));
        Logros.dar(p, "pieza");
        if (d.piezas == TODAS) for (ServerPlayer o : presentes(p.server)) Logros.dar(o, "piezas");
        DarkCraftMod.LOGGER.info("[DarkCraft] Brumavela: {} encuentra la pieza {} ({} de 5)", p.getGameProfile().getName(), q.id(), n);
    }

    /** Suena una campana; las tres en el orden de las lapidas apartan la losa de la cripta. */
    private static void tocar(ServerPlayer p, int cual, Cosa c) {
        ServerLevel lvl = p.serverLevel();
        BlockPos b = new BlockPos(c.x(), c.y(), c.z());
        if (lvl.getBlockEntity(b) instanceof BellBlockEntity be) be.onHit(cual == 0 ? Direction.NORTH : cual == 1 ? Direction.SOUTH : Direction.WEST);
        lvl.playSound(null, b, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 3.0f, TONO[cual]);
        Datos d = Datos.get(p.server);
        if (d.cripta) return;
        long ahora = lvl.getGameTime();
        if (ahora - ultimoToque > 20 * 45) seguidas = 0;
        ultimoToque = ahora;
        if (cual == ORDEN[seguidas]) {
            seguidas++;
            if (seguidas == ORDEN.length) {
                seguidas = 0;
                abrirCripta(lvl, p);
            }
            return;
        }
        boolean iba = seguidas > 0;
        seguidas = cual == ORDEN[0] ? 1 : 0;
        if (iba) {
            lvl.playSound(null, b, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0f, 0.5f);
            Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.campanas.mal", 0));
        }
    }

    /** La losa de la cripta se aparta. */
    public static void abrirCripta(ServerLevel lvl, ServerPlayer quien) {
        Datos d = Datos.get(lvl.getServer());
        if (d.cripta) return;
        d.cripta = true;
        d.cambio();
        int y = PuebloPlan.get().capilla.y;
        for (int x = Capilla.BX0; x <= Capilla.BX1; x++)
            for (int z = Capilla.BZ0; z <= Capilla.BZ1; z++) {
                BlockPos b = new BlockPos(x, y, z);
                BlockState s = lvl.getBlockState(b);
                if (s.isAir()) continue;
                lvl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s), x + 0.5, y + 0.3, z + 0.5, 10, 0.3, 0.15, 0.3, 0.02);
                lvl.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
            }
        BlockPos c = pos("losa");
        lvl.playSound(null, c, DarkCraftMod.SFX_CRIPTA.get(), SoundSource.BLOCKS, 3.0f, 1.0f);
        lvl.sendParticles(ParticleTypes.SOUL, c.getX() + 0.5, y + 0.4, c.getZ() + 0.5, 30, 1.6, 0.2, 0.8, 0.02);
        for (ServerPlayer o : presentes(lvl.getServer())) Net.to(o, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.cripta", 0));
        if (quien != null) Logros.dar(quien, "campanas");
        DarkCraftMod.LOGGER.info("[DarkCraft] Brumavela: la cripta de la capilla se abre");
    }

    // ------------------------------------------------------------------ el barco

    /** Vuelve a levantar el barco, entero, encima del roto. */
    public static void reparar(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        Datos d = Datos.get(p.server);
        if (d.reparado) return;
        levantar(lvl, true);
        d.reparado = true;
        d.cambio();
        int[] s = sitio("barco");
        double x = Barco.BX + 0.5, z = Barco.PALO + 0.5;
        for (int k = 0; k < Barco.PALO_ALTO; k += 2) lvl.sendParticles(ParticleTypes.END_ROD, x, s[1] + k, z, 6, 0.5, 0.6, 0.5, 0.03);
        lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, s[1] + 1, Barco.CORAZON_Z + 0.5, 40, 2.5, 0.6, 5, 0.03);
        lvl.playSound(null, x, s[1], z, DarkCraftMod.SFX_BARCO.get(), SoundSource.PLAYERS, 2.5f, 1.0f);
        for (ServerPlayer o : presentes(p.server)) {
            Net.to(o, new Net.Cartel(Net.Cartel.BARCO, "darkcraft.cartel.barco.sub", 0));
            Logros.dar(o, "barco");
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] Brumavela: {} repara La Garza Gris", p.getGameProfile().getName());
    }

    /** Pone el barco (roto o entero) en el mundo ya generado, chunk a chunk, y une vallas y cristales. */
    public static void levantar(ServerLevel lvl, boolean entero) {
        Barco b = new Barco(PuebloPlan.get().barco.semilla(), entero);
        RandomSource azar = RandomSource.create(7);
        for (int cx = b.x0 >> 4; cx <= b.x1 >> 4; cx++)
            for (int cz = b.z0 >> 4; cz <= b.z1 >> 4; cz++) {
                Canvas c = new Canvas(lvl, cx, cz, azar);
                b.despejar(c);
                b.build(c);
            }
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = b.x0; x <= b.x1; x++)
            for (int z = b.z0; z <= b.z1; z++)
                for (int y = b.y0; y <= b.y1; y++) {
                    BlockState s = lvl.getBlockState(m.set(x, y, z));
                    if (!(s.getBlock() instanceof CrossCollisionBlock) && !(s.getBlock() instanceof WallBlock)) continue;
                    BlockState n = Block.updateFromNeighbourShapes(s, lvl, m);
                    if (n != s) lvl.setBlock(m, n, 2);
                }
    }

    /** Del timon de La Garza Gris a Valdrenn (de momento, sin viaje: la cinematica llega en la parte 3d). */
    private static void zarpar(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        BlockPos l = MundoNubes.llegada(lvl.getSeed());
        if (l == null) {
            Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.sin_rumbo", 0));
            return;
        }
        lvl.playSound(null, p.getX(), p.getY(), p.getZ(), DarkCraftMod.SFX_BARCO.get(), SoundSource.PLAYERS, 1.0f, 1.25f);
        p.teleportTo(lvl, l.getX() + 0.5, l.getY() + 0.1, l.getZ() + 0.5, 180f, 0f);
        Logros.dar(p, "zarpar");
        Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.llegada", 0));
    }

    private static void volver(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        int[] s = sitio("barco_timon");
        lvl.playSound(null, p.getX(), p.getY(), p.getZ(), DarkCraftMod.SFX_BARCO.get(), SoundSource.PLAYERS, 1.0f, 1.25f);
        p.teleportTo(lvl, s[0] + 0.5, s[1] + 0.1, s[2] + 2.5, 0f, 0f);
    }

    /** El noray de la llegada: un poste con su farol junto a la plazoleta de la torre (se pone una vez). */
    private static void ponerNoray(ServerLevel lvl, BlockPos n) {
        if (!lvl.hasChunkAt(n) || lvl.getBlockState(n).is(Blocks.DARK_OAK_LOG)) return;
        lvl.setBlock(n.below(), DarkCraftMod.NUBE_FIRME.get().defaultBlockState(), 3);
        lvl.setBlock(n, Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
        lvl.setBlock(n.above(), Blocks.DARK_OAK_FENCE.defaultBlockState(), 3);
        lvl.setBlock(n.above(2), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
    }

    // ------------------------------------------------------------------ el clic derecho en sus bloques

    @SubscribeEvent
    public static void onClic(PlayerInteractEvent.RightClickBlock e) {
        Level l = e.getLevel();
        BlockPos b = e.getPos();
        if (l.dimension() != Level.OVERWORLD || !PuebloFeature.dentro(b.getX(), b.getZ())) return;
        if (!l.isClientSide() ? !mundo(l) : false) return;
        BlockState s = l.getBlockState(b);
        int id = -1;
        if (s.is(Blocks.CHEST) && igual(b, sitio("vela"))) id = Pieza.VELA.ordinal();
        else if (s.is(Blocks.BELL)) {
            for (int k = 0; k < 3; k++) if (igual(b, sitio("campana_" + QUIEN[k]))) id = CAMPANA + k;
        } else if (s.is(Blocks.GRINDSTONE) && b.getX() == Barco.BX && b.getZ() == Barco.TIMON_Z) id = ZARPAR;
        if (id < 0) return;
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);
        if (!l.isClientSide() && e.getEntity() instanceof ServerPlayer p && e.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            if (id == Pieza.VELA.ordinal() && (Datos.get(p.server).piezas & 1) != 0) Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.pueblo.vacio", 0));
            else usar(p, id);
        }
    }

    private static boolean igual(BlockPos b, int[] s) {
        return s != null && b.getX() == s[0] && b.getY() == s[1] && b.getZ() == s[2];
    }

    // ------------------------------------------------------------------ comandos

    private static int con(com.mojang.brigadier.context.CommandContext<CommandSourceStack> c, java.util.function.Consumer<ServerPlayer> f)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!mundo(p.level())) {
            c.getSource().sendFailure(Component.translatable("darkcraft.pueblo.no_aqui"));
            return 0;
        }
        f.accept(p);
        return 1;
    }

    /** /darkcraft brumavela ir [sitio] | pieza <nombre>|todas | cripta | reparar | zarpar | estado | reiniciar. */
    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("brumavela")
                .then(Commands.literal("ir").executes(c -> con(c, p -> ir(p, "inicio")))
                        .then(Commands.argument("sitio", StringArgumentType.word())
                                .suggests((c, sb) -> SharedSuggestionProvider.suggest(PuebloPlan.get().sitios().keySet(), sb))
                                .executes(c -> con(c, p -> ir(p, StringArgumentType.getString(c, "sitio"))))))
                .then(Commands.literal("pieza")
                        .then(Commands.argument("nombre", StringArgumentType.word())
                                .suggests((c, sb) -> SharedSuggestionProvider.suggest(new String[]{"vela", "timon", "mastil", "ancla", "corazon", "todas"}, sb))
                                .executes(c -> con(c, p -> {
                                    String n = StringArgumentType.getString(c, "nombre");
                                    for (Pieza q : Pieza.values()) if (n.equals("todas") || n.equals(q.id())) tomar(p, q);
                                }))))
                .then(Commands.literal("cripta").executes(c -> con(c, p -> abrirCripta(p.serverLevel(), p))))
                .then(Commands.literal("reparar").executes(c -> con(c, Pueblo::reparar)))
                .then(Commands.literal("zarpar").executes(c -> con(c, Pueblo::zarpar)))
                .then(Commands.literal("estado").executes(c -> con(c, p -> {
                    Datos d = Datos.get(p.server);
                    c.getSource().sendSuccess(() -> Component.literal(estado(d)), false);
                })))
                .then(Commands.literal("reiniciar").executes(c -> con(c, p -> reiniciar(p.serverLevel()))));
    }

    public static String estado(Datos d) {
        StringBuilder b = new StringBuilder("Brumavela: piezas ");
        for (Pieza q : Pieza.values()) b.append((d.piezas & (1 << q.ordinal())) != 0 ? q.id().toUpperCase(java.util.Locale.ROOT) : q.id()).append(' ');
        return b.append("| cripta ").append(d.cripta ? "abierta" : "cerrada").append(" | barco ").append(d.reparado ? "reparado" : "roto")
                .append(" | santuario ").append(d.santuario ? "puesto" : "sin poner").toString();
    }

    private static void ir(ServerPlayer p, String nombre) {
        int[] s = sitio(nombre);
        if (s == null) return;
        p.teleportTo(p.serverLevel(), s[0] + 0.5, s[1] + 0.1, s[2] + 0.5, p.getYRot(), p.getXRot());
    }

    /** Lo deja todo como al crear el mundo: las piezas en su sitio, la cripta cerrada, el faro encendido y el barco roto. */
    public static void reiniciar(ServerLevel lvl) {
        Datos d = Datos.get(lvl.getServer());
        d.piezas = 0;
        d.cripta = false;
        boolean estaba = d.reparado;
        d.reparado = false;
        d.cambio();
        seguidas = 0;
        int y = PuebloPlan.get().capilla.y;
        for (int x = Capilla.BX0; x <= Capilla.BX1; x++)
            for (int z = Capilla.BZ0; z <= Capilla.BZ1; z++) lvl.setBlock(new BlockPos(x, y, z), Capilla.losa(x, z), 3);
        BlockPos c = pos("corazon");
        if (lvl.getBlockState(c).isAir()) lvl.setBlock(c, Blocks.LIGHT.defaultBlockState(), 3);
        lvl.blockEvent(pos("vela"), Blocks.CHEST, 1, 0);
        if (estaba) levantar(lvl, false);
    }
}
