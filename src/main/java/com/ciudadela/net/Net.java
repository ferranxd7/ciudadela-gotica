package com.ciudadela.net;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.cine.Cinematics;
import com.ciudadela.cine.Shot;
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

    private static final String V = "1";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(new ResourceLocation(CiudadelaMod.MODID, "main"),
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
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.ciudadela.client.ClientCine.start(this));
            ctx.get().setPacketHandled(true);
        }
    }

    public record CineStop() {
        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.ciudadela.client.ClientCine.stop(false));
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
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.ciudadela.client.MapaScreen.open(this));
            ctx.get().setPacketHandled(true);
        }
    }
}
