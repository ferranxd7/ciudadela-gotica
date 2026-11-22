package com.darkcraft.net;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Shot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Paquetes del mod: cinematicas (servidor -> cliente y saltar) y el mapa de pergamino. */
public final class Net {
    private Net() {}

    private static final String V = "3";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(new ResourceLocation(DarkCraftMod.MODID, "main"),
            () -> V, V::equals, V::equals);

    public static void register() {
        int id = 0;
        CH.messageBuilder(CineStart.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CineStart::write).decoder(CineStart::read).consumerMainThread(CineStart::handle).add();
        CH.messageBuilder(CineStop.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((m, b) -> {}).decoder(b -> new CineStop()).consumerMainThread(CineStop::handle).add();
        CH.messageBuilder(CineSkip.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder((m, b) -> {}).decoder(b -> new CineSkip()).consumerMainThread(CineSkip::handle).add();
        CH.messageBuilder(MapOpen.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MapOpen::write).decoder(MapOpen::read).consumerMainThread(MapOpen::handle).add();
        CH.messageBuilder(Aviso.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Aviso::write).decoder(Aviso::read).consumerMainThread(Aviso::handle).add();
        CH.messageBuilder(ReyHabla.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ReyHabla::write).decoder(ReyHabla::read).consumerMainThread(ReyHabla::handle).add();
        CH.messageBuilder(ReyFoco.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ReyFoco::write).decoder(ReyFoco::read).consumerMainThread(ReyFoco::handle).add();
        CH.messageBuilder(Cofre.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Cofre::write).decoder(Cofre::read).consumerMainThread(Cofre::handle).add();
        CH.messageBuilder(Romper.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Romper::write).decoder(Romper::read).consumerMainThread(Romper::handle).add();
        CH.messageBuilder(Combo.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Combo::write).decoder(Combo::read).consumerMainThread(Combo::handle).add();
        CH.messageBuilder(Energia.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Energia::write).decoder(Energia::read).consumerMainThread(Energia::handle).add();
        CH.messageBuilder(Llave.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Llave::write).decoder(Llave::read).consumerMainThread(Llave::handle).add();
        CH.messageBuilder(Isla.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Isla::write).decoder(Isla::read).consumerMainThread(Isla::handle).add();
    }

    // ------------------------------------------------------------------ jefe

    /**
     * Aviso en el suelo de donde va a caer un golpe: forma, centro, medidas (a = radio o largo,
     * b = radio interior o ancho, ang = hacia donde mira en grados), cuanto falta (ticks) y color.
     */
    public record Aviso(int forma, double x, double y, double z, float a, float b, float ang, int dur, int color) {
        public static final int CIRCULO = 0, ANILLO = 1, LINEA = 2, SECTOR = 3;
        public static final int ROJO = 0, ORO = 1, NARANJA = 2;

        void write(FriendlyByteBuf buf) {
            buf.writeByte(forma);
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
            buf.writeFloat(a);
            buf.writeFloat(b);
            buf.writeFloat(ang);
            buf.writeVarInt(dur);
            buf.writeByte(color);
        }

        static Aviso read(FriendlyByteBuf buf) {
            return new Aviso(buf.readByte(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), Math.min(buf.readVarInt(), 400), buf.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.rey.ReyCliente.aviso(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El Rey dice una frase (voz y subtitulo). */
    public record ReyHabla(int entity, int linea) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entity);
            b.writeByte(linea);
        }

        static ReyHabla read(FriendlyByteBuf b) {
            return new ReyHabla(b.readVarInt(), b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.rey.ReyCliente.habla(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** La camara mira al Rey (intro): bandas de cine y el jugador quieto durante ticks. */
    public record ReyFoco(int entity, int ticks) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entity);
            b.writeVarInt(ticks);
        }

        static ReyFoco read(FriendlyByteBuf b) {
            return new ReyFoco(b.readVarInt(), Math.min(b.readVarInt(), 2400));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.rey.ReyCliente.foco(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador pide un combo de las espadas del Rey (1 Tajo Real, 2 Juicio del Rey). */
    public record Combo(int tipo) {
        void write(FriendlyByteBuf b) {
            b.writeByte(tipo);
        }

        static Combo read(FriendlyByteBuf b) {
            return new Combo(b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) com.darkcraft.armas.Combos.pedir(sp, tipo);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Energia de los combos (0-100) para la barra. */
    public record Energia(int valor) {
        void write(FriendlyByteBuf b) {
            b.writeByte(valor);
        }

        static Energia read(FriendlyByteBuf b) {
            return new Energia(b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.armas.ArmasCliente.energia(valor));
            ctx.get().setPacketHandled(true);
        }
    }

    /** La llave del arbol gira en el sello (para la mini cinematica). */
    public record Llave(double x, double y, double z, int ticks) {
        void write(FriendlyByteBuf b) {
            b.writeDouble(x);
            b.writeDouble(y);
            b.writeDouble(z);
            b.writeVarInt(ticks);
        }

        static Llave read(FriendlyByteBuf b) {
            return new Llave(b.readDouble(), b.readDouble(), b.readDouble(), Math.min(b.readVarInt(), 400));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.rey.ReyCliente.llave(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador entra en la tormenta de la isla (o sale): rayos sin destello y truenos lejanos. */
    public record Isla(boolean dentro) {
        void write(FriendlyByteBuf b) {
            b.writeBoolean(dentro);
        }

        static Isla read(FriendlyByteBuf b) {
            return new Isla(b.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ClientIsla.dentro(dentro));
            ctx.get().setPacketHandled(true);
        }
    }

    /** La dimension de los volcanes se rompe como un cristal (grietas en la pantalla y destello). */
    public record Romper(int ticks) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(ticks);
        }

        static Romper read(FriendlyByteBuf b) {
            return new Romper(Math.min(b.readVarInt(), 200));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.rey.ReyCliente.romper(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El cofre del premio sube del suelo con un haz de luz (durante ticks). */
    public record Cofre(int x, int y, int z, int ticks) {
        void write(FriendlyByteBuf b) {
            b.writeInt(x);
            b.writeInt(y);
            b.writeInt(z);
            b.writeVarInt(ticks);
        }

        static Cofre read(FriendlyByteBuf b) {
            return new Cofre(b.readInt(), b.readInt(), b.readInt(), Math.min(b.readVarInt(), 2400));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.rey.ReyCliente.cofre(this));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void to(ServerPlayer p, Object msg) {
        CH.send(PacketDistributor.PLAYER.with(() -> p), msg);
    }

    // ------------------------------------------------------------------ cinematica

    /** Empieza una cinematica: capitulo, planos de camara (solo el 1) y rayos programados {t, x, y, z}. */
    public record CineStart(int chapter, List<Shot> shots, double[] bolts) {
        void write(FriendlyByteBuf b) {
            b.writeByte(chapter);
            b.writeVarInt(shots.size());
            for (Shot s : shots) s.write(b);
            b.writeVarInt(bolts.length);
            for (double d : bolts) b.writeDouble(d);
        }

        static CineStart read(FriendlyByteBuf b) {
            int ch = b.readByte();
            int n = Math.min(b.readVarInt(), 16);
            List<Shot> l = new ArrayList<>();
            for (int i = 0; i < n; i++) l.add(Shot.read(b));
            double[] bolts = new double[Math.min(b.readVarInt(), 400)];
            for (int i = 0; i < bolts.length; i++) bolts[i] = b.readDouble();
            return new CineStart(ch, l, bolts);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ClientCine.start(this));
            ctx.get().setPacketHandled(true);
        }
    }

    public record CineStop() {
        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ClientCine.stop(false));
            ctx.get().setPacketHandled(true);
        }
    }

    public record CineSkip() {
        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) Cinematics.skip(p);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ mapa

    /** Datos del mapa de pergamino: formas del castillo, nombres, capitulos y progreso del jugador. */
    public record MapOpen(int cx, int cz, int read, int[] shapes, String[] labels, int[] labelPos, int[] chapters) {
        void write(FriendlyByteBuf b) {
            b.writeInt(cx);
            b.writeInt(cz);
            b.writeVarInt(read);
            b.writeVarIntArray(shapes);
            b.writeVarInt(labels.length);
            for (String s : labels) b.writeUtf(s, 64);
            b.writeVarIntArray(labelPos);
            b.writeVarIntArray(chapters);
        }

        static MapOpen read(FriendlyByteBuf b) {
            int cx = b.readInt(), cz = b.readInt(), read = b.readVarInt();
            int[] shapes = b.readVarIntArray(200000);
            String[] labels = new String[Math.min(b.readVarInt(), 64)];
            for (int i = 0; i < labels.length; i++) labels[i] = b.readUtf(64);
            int[] pos = b.readVarIntArray(256);
            int[] ch = b.readVarIntArray(64);
            return new MapOpen(cx, cz, read, shapes, labels, pos, ch);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.MapaScreen.open(this));
            ctx.get().setPacketHandled(true);
        }
    }
}
