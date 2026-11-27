package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Gigante;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Los gigantes errantes de Morvain (parte 7w): por donde va cada uno y cuando esta de verdad en el mundo.
 * <ul>
 * <li>Cada uno anda su vuelta ({@link RutasGigantes}) a {@link Gigante#PASO} bloques por tick (dos por segundo) aunque no le vea
 * nadie: lo andado se guarda ({@code darkcraft_gigantes}: lo andado de cada uno, si ha caido y donde quedo su arma).</li>
 * <li>Sale (la entidad) cuando un jugador de Morvain esta a {@link #SALE} bloques de por donde va y ese sitio esta cargado; se
 * recoge (sin guardarse) cuando ya no queda nadie a {@link #SE_VA} y no esta peleando. Desde ahi sigue andando "de memoria".</li>
 * <li>El que cae no vuelve nunca. Donde cae queda su arma ({@link #monumento}): la campana de Ogmund (que se puede tocar: "tocala
 * por mi, una vez"), la costilla de Ulfgar hincada en el suelo y la columna de Haldor tumbada con su capitel.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Gigantes {
    private Gigantes() {}

    public static final int N = 3;
    /** Lo cerca de por donde va que tiene que estar alguien para que salga, y lo lejos para que se recoja. */
    public static final double SALE = 112, SE_VA = 160;
    private static final Gigante[] FUERA = new Gigante[N];

    /** El que esta fuera (o null). */
    public static Gigante fuera(int i) {
        Gigante g = FUERA[i];
        return g != null && !g.isRemoved() ? g : null;
    }

    /** Cada tick (lo llama {@link Morvain#onTick}): los que estan fuera, a lo suyo; cada medio segundo, los que andan de memoria. */
    public static void tick(ServerLevel m) {
        tick(m, m.getServer().getTickCount() % 10 == 3);
    }

    /** Lo mismo, diciendo si toca lo de cada medio segundo (las pruebas lo llaman seguido, sin que pase el tiempo del servidor). */
    public static void tick(ServerLevel m, boolean cada) {
        MinecraftServer srv = m.getServer();
        Datos d = Datos.get(srv);
        for (int i = 0; i < N; i++) {
            Gigante g = FUERA[i];
            if (g != null && g.isRemoved()) {
                FUERA[i] = g = null;
            }
            if (d.muerto[i]) continue;
            if (g != null) {
                if (!g.isAlive()) continue;
                d.s[i] = g.s();
                if (cada) {
                    d.setDirty();
                    boolean alguien = false;
                    for (ServerPlayer p : m.players())
                        if (!p.isSpectator() && Math.hypot(p.getX() - g.getX(), p.getZ() - g.getZ()) < SE_VA) alguien = true;
                    if ((!alguien && !g.enPelea()) || !m.isPositionEntityTicking(g.blockPosition())) {
                        g.discard();
                        FUERA[i] = null;
                        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: el gigante {} se aleja (lo andado {})", i, Math.round(d.s[i]));
                    }
                }
                continue;
            }
            if (!cada) continue;
            // de memoria: sigue andando su vuelta
            d.s[i] = (d.s[i] + Gigante.PASO * 10) % RutasGigantes.largo(i);
            d.setDirty();
            double[] p = RutasGigantes.punto(i, d.s[i]);
            boolean alguien = false;
            for (ServerPlayer pl : m.players())
                if (!pl.isSpectator() && Math.hypot(pl.getX() - p[0], pl.getZ() - p[1]) < SALE) alguien = true;
            if (alguien) sacar(m, i, d.s[i]);
        }
    }

    /** Saca al gigante i donde va de su vuelta (si su sitio esta cargado). Devuelve el que sale (o null). */
    public static Gigante sacar(ServerLevel m, int i, double s) {
        double[] p = RutasGigantes.punto(i, s);
        BlockPos b = BlockPos.containing(p[0], 100, p[1]);
        if (!m.isPositionEntityTicking(b)) return null;
        return crear(m, i, s);
    }

    /** Lo mismo sin mirar si su sitio procesa entidades (las pruebas, que lo mueven a mano). */
    public static Gigante crear(ServerLevel m, int i, double s) {
        double[] p = RutasGigantes.punto(i, s);
        Gigante g = DarkCraftMod.GIGANTE.get().create(m);
        if (g == null) return null;
        g.variante(i);
        g.s(s);
        double y = Gigante.suelo(m, p[0], p[1], 80);
        float giro = (float) (Mth.atan2(p[3], p[2]) * Mth.RAD_TO_DEG) - 90f;
        g.moveTo(p[0], y, p[1], giro, 0);
        g.setYHeadRot(giro);
        g.setYBodyRot(giro);
        m.addFreshEntity(g);
        FUERA[i] = g;
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: sale el gigante {} en {} {} {} (lo andado {})", i, Math.round(p[0]), Math.round(y), Math.round(p[1]), Math.round(s));
        return g;
    }

    /** Ha caido (lo llama {@link Gigante#die}): ya no vuelve. */
    public static void caido(ServerLevel sl, Gigante g) {
        int i = Mth.clamp(g.variante(), 0, N - 1);
        Datos d = Datos.get(sl.getServer());
        if (Morvain.es(sl)) {
            d.muerto[i] = true;
            d.setDirty();
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: ha caido el gigante {} ({})", i, g.tipo());
    }

    /** Lo que lleva andado cada uno, si ha caido y si esta fuera (comandos y pruebas). */
    public static String estado(MinecraftServer srv) {
        Datos d = Datos.get(srv);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < N; i++) {
            Gigante g = fuera(i);
            double[] p = RutasGigantes.punto(i, d.s[i]);
            b.append(i == 0 ? "" : "; ").append(Gigante.TIPOS[i]).append(d.muerto[i] ? " caido" : g != null ? " fuera en " + g.blockPosition().toShortString()
                    + (g.enPelea() ? " peleando" : "") : " andando por " + Math.round(p[0]) + " " + Math.round(p[1]));
        }
        return b.toString();
    }

    /** Vuelve a la vida (comando y pruebas): ya no esta caido. */
    public static void revivir(MinecraftServer srv, int i) {
        Datos d = Datos.get(srv);
        d.muerto[i] = false;
        d.setDirty();
    }

    /** Si ha caido. */
    public static boolean muerto(MinecraftServer srv, int i) {
        return Datos.get(srv).muerto[i];
    }

    /** Recoge al que este fuera (comando y pruebas). */
    public static void recoger(int i) {
        Gigante g = fuera(i);
        if (g != null) g.discard();
        FUERA[i] = null;
    }

    /** Pone lo andado (comando y pruebas). */
    public static void poner(MinecraftServer srv, int i, double s) {
        Datos d = Datos.get(srv);
        d.s[i] = s;
        d.setDirty();
    }

    /** Lo andado de cada uno. */
    public static double andado(MinecraftServer srv, int i) {
        return Datos.get(srv).s[i];
    }

    // ------------------------------------------------------------------ lo que queda donde cae

    /**
     * Donde cae un gigante queda su arma, de bloques, delante de donde cayo (hacia donde miraba): la campana de Ogmund (una
     * campana de verdad, en el suelo, con la viga detras), la costilla de Ulfgar (un arco de bloques de hueso hincado en la
     * tierra) o la columna de Haldor (tumbada, de cuarzo, con su capitel cincelado). Solo en el aire o lo que se aparta (hierba,
     * flores, nieve): nunca rompe nada.
     */
    public static java.util.List<BlockPos> monumento(ServerLevel sl, int v, double x, double y, double z, float giro) {
        Vec3 f = Vec3.directionFromRotation(0, giro);
        Direction hacia = Direction.fromYRot(giro);
        Direction.Axis eje = hacia.getAxis();
        java.util.List<BlockPos> puestos = new java.util.ArrayList<>();
        PUESTOS = puestos;
        int n = 0;
        if (v == Gigante.OGMUND) {
            // la viga (troncos de roble oscuro sin corteza) y la campana al cabo
            for (int k = 2; k <= 5; k++)
                n += poner(sl, x + f.x * k, z + f.z * k, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, eje));
            BlockPos c = suelo(sl, x + f.x * 6.5, z + f.z * 6.5);
            if (c != null && sl.getBlockState(c).canBeReplaced()) {
                sl.setBlock(c, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.FLOOR).setValue(BellBlock.FACING, hacia), 3);
                puestos.add(c);
                Datos d = Datos.get(sl.getServer());
                d.campana = c;
                d.setDirty();
                n++;
            }
        } else if (v == Gigante.ULFGAR) {
            // la costilla: sale del suelo, se alza y se curva hacia delante (bloques de hueso)
            int[][] arco = {{1, 0}, {2, 1}, {2, 2}, {3, 2}, {3, 3}, {4, 3}, {5, 3}, {6, 2}};
            for (int[] a : arco)
                n += poner(sl, x + f.x * (a[0] + 1), z + f.z * (a[0] + 1), a[1], Blocks.BONE_BLOCK.defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, a[1] >= 3 ? eje : Direction.Axis.Y));
        } else {
            // la columna tumbada (cuarzo) con el capitel cincelado en la punta y un trozo partido
            for (int k = 2; k <= 6; k++)
                n += poner(sl, x + f.x * k, z + f.z * k, 0, Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, eje));
            n += poner(sl, x + f.x * 7, z + f.z * 7, 0, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            n += poner(sl, x + f.x * 1 - f.z * 1.5, z + f.z * 1 + f.x * 1.5, 0, Blocks.QUARTZ_SLAB.defaultBlockState());
        }
        sl.sendParticles(ParticleTypes.CLOUD, x + f.x * 4, y + 1, z + f.z * 4, 30, 2.0, 0.6, 2.0, 0.02);
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: donde cayo el gigante {} queda su arma ({} bloques)", v, n);
        ULTIMO = puestos;
        return puestos;
    }

    /** Los bloques del ultimo monumento que se puso (pruebas: para quitarlos). */
    public static java.util.List<BlockPos> ULTIMO = new java.util.ArrayList<>();
    private static java.util.List<BlockPos> PUESTOS = new java.util.ArrayList<>();

    /** Pone un bloque a ras de suelo en (x, z) (y {@code alto} por encima), si alli no hay nada que no se pueda apartar. */
    private static int poner(ServerLevel sl, double x, double z, int alto, BlockState b) {
        BlockPos c = suelo(sl, x, z);
        if (c == null) return 0;
        c = c.above(alto);
        if (!sl.getBlockState(c).canBeReplaced()) return 0;
        sl.setBlock(c, b, 3);
        PUESTOS.add(c);
        return 1;
    }

    /** El primer hueco encima del suelo en (x, z) (lo del mapa de alturas, sin hojas). */
    private static BlockPos suelo(ServerLevel sl, double x, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z);
        if (!sl.hasChunkAt(new BlockPos(bx, 0, bz))) return null;
        int y = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        BlockPos c = new BlockPos(bx, y, bz);
        // lo que no frena (hierba alta, flores) no cuenta como suelo
        while (c.getY() > sl.getMinBuildHeight() + 1 && sl.getBlockState(c.below()).canBeReplaced()) c = c.below();
        return c;
    }

    /** Las veces que se ha tocado la campana de Ogmund (pruebas). */
    public static int TOQUES;

    /** Donde quedo la campana de Ogmund (o null). */
    public static BlockPos campana(MinecraftServer srv) {
        return Datos.get(srv).campana;
    }

    /** Quien toca la campana de Ogmund donde cayo: suena su campana y, la primera vez, la entrada del diario. */
    @SubscribeEvent
    public static void onCampana(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel sl) || !Morvain.es(sl)) return;
        Datos d = Datos.get(sl.getServer());
        if (d.campana == null || !d.campana.equals(e.getPos()) || !sl.getBlockState(e.getPos()).is(Blocks.BELL)) return;
        TOQUES++;
        sl.playSound(null, e.getPos(), DarkCraftMod.SFX_GIGANTE_CAMPANA.get(), SoundSource.BLOCKS, 3.0f, 0.9f);
        com.darkcraft.espejo.Espejo.diario(p, "campana_ogmund");
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        for (int i = 0; i < N; i++) FUERA[i] = null;
    }

    // ------------------------------------------------------------------ el comando

    /**
     * {@code /darkcraft gigante [<0-2> [ir | aqui | vencer | derribar | volver]]}: como van los tres; llevar ante uno (delante de
     * por donde va, a 26 bloques, mirandole: asi sale), sacarlo delante de quien lo pide, vencerle, tirarle sobre una rodilla o que
     * vuelva (si habia caido, ya no lo esta; el que estaba fuera se recoge y sale de nuevo donde iba).
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        var n = net.minecraft.commands.Commands.argument("n", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, N - 1));
        return net.minecraft.commands.Commands.literal("gigante")
                .executes(c -> decir(c, estado(c.getSource().getServer())))
                .then(n.executes(c -> decir(c, estado(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                            int i = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n");
                            ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m != null && c.getSource().getEntity() instanceof ServerPlayer sp) ir(sp, m, i);
                            return decir(c, estado(c.getSource().getServer()));
                        }))
                        .then(net.minecraft.commands.Commands.literal("aqui").executes(c -> {
                            int i = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n");
                            if (c.getSource().getEntity() instanceof ServerPlayer sp && sp.level() instanceof ServerLevel sl) {
                                recoger(i);
                                Vec3 f = Vec3.directionFromRotation(0, sp.getYRot());
                                Gigante g = DarkCraftMod.GIGANTE.get().create(sl);
                                if (g != null) {
                                    g.variante(i);
                                    double x = sp.getX() + f.x * 16, z = sp.getZ() + f.z * 16;
                                    g.s(RutasGigantes.donde(i, x, z));
                                    g.moveTo(x, Gigante.suelo(sl, x, z, sp.getY()), z, sp.getYRot() + 180f, 0);
                                    sl.addFreshEntity(g);
                                    if (Morvain.es(sl)) FUERA[i] = g;
                                }
                            }
                            return decir(c, estado(c.getSource().getServer()));
                        }))
                        .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                            int i = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n");
                            Gigante g = fuera(i);
                            if (g != null) g.vencer();
                            else {
                                Datos d = Datos.get(c.getSource().getServer());
                                d.muerto[i] = true;
                                d.setDirty();
                                if (c.getSource().getEntity() instanceof ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, Gigante.TIPOS[i]);
                            }
                            return decir(c, estado(c.getSource().getServer()));
                        }))
                        .then(net.minecraft.commands.Commands.literal("derribar").executes(c -> {
                            Gigante g = fuera(com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n"));
                            if (g != null) g.derribar();
                            return decir(c, estado(c.getSource().getServer()));
                        }))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            int i = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n");
                            recoger(i);
                            revivir(c.getSource().getServer(), i);
                            return decir(c, estado(c.getSource().getServer()));
                        })));
    }

    /** Lleva a un jugador delante de por donde va el gigante i (a 26 bloques, de cara a el). */
    public static void ir(ServerPlayer sp, ServerLevel m, int i) {
        Gigante g = fuera(i);
        double gx, gz;
        double[] p = RutasGigantes.punto(i, andado(m.getServer(), i));
        if (g != null) {
            gx = g.getX();
            gz = g.getZ();
        } else {
            gx = p[0];
            gz = p[1];
        }
        double x = gx + p[2] * 26, z = gz + p[3] * 26;
        double y = Gigante.suelo(m, x, z, 120);
        float giro = (float) (Mth.atan2(gz - z, gx - x) * Mth.RAD_TO_DEG) - 90f;
        sp.teleportTo(m, x, y + 0.05, z, giro, 8f);
    }

    private static int decir(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, String s) {
        c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
        return 1;
    }

    // ------------------------------------------------------------------ lo que se guarda

    public static final class Datos extends SavedData {
        public static final String ID = "darkcraft_gigantes";
        final double[] s = new double[N];
        final boolean[] muerto = new boolean[N];
        BlockPos campana;

        public static Datos get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Datos::load, Datos::nuevo, ID);
        }

        /** Al empezar, cada uno en un sitio distinto de su vuelta. */
        static Datos nuevo() {
            Datos d = new Datos();
            for (int i = 0; i < N; i++) d.s[i] = RutasGigantes.largo(i) * (0.15 + 0.3 * i);
            return d;
        }

        static Datos load(CompoundTag t) {
            Datos d = nuevo();
            for (int i = 0; i < N; i++) {
                if (t.contains("s" + i)) d.s[i] = t.getDouble("s" + i);
                d.muerto[i] = t.getBoolean("muerto" + i);
            }
            if (t.contains("campana")) d.campana = BlockPos.of(t.getLong("campana"));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            for (int i = 0; i < N; i++) {
                t.putDouble("s" + i, s[i]);
                t.putBoolean("muerto" + i, muerto[i]);
            }
            if (campana != null) t.putLong("campana", campana.asLong());
            return t;
        }
    }
}
