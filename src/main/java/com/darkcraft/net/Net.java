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

    private static final String V = "20";
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
        CH.messageBuilder(EspejoEstado.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EspejoEstado::write).decoder(EspejoEstado::read).consumerMainThread(EspejoEstado::handle).add();
        CH.messageBuilder(EspejoPos.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EspejoPos::write).decoder(EspejoPos::read).consumerMainThread(EspejoPos::handle).add();
        CH.messageBuilder(EspejoAccion.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EspejoAccion::write).decoder(EspejoAccion::read).consumerMainThread(EspejoAccion::handle).add();
        CH.messageBuilder(Fortaleza.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Fortaleza::write).decoder(Fortaleza::read).consumerMainThread(Fortaleza::handle).add();
        CH.messageBuilder(CieloRojo.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CieloRojo::write).decoder(CieloRojo::read).consumerMainThread(CieloRojo::handle).add();
        CH.messageBuilder(RolEstado.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RolEstado::write).decoder(RolEstado::read).consumerMainThread(RolEstado::handle).add();
        CH.messageBuilder(Estamina.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Estamina::write).decoder(Estamina::read).consumerMainThread(Estamina::handle).add();
        CH.messageBuilder(Cartel.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Cartel::write).decoder(Cartel::read).consumerMainThread(Cartel::handle).add();
        CH.messageBuilder(Recuperar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder((m, b) -> {}).decoder(b -> new Recuperar()).consumerMainThread(Recuperar::handle).add();
        CH.messageBuilder(Ecos.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Ecos::write).decoder(Ecos::read).consumerMainThread(Ecos::handle).add();
        CH.messageBuilder(FichaRol.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FichaRol::write).decoder(FichaRol::read).consumerMainThread(FichaRol::handle).add();
        CH.messageBuilder(SubirNivel.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SubirNivel::write).decoder(SubirNivel::read).consumerMainThread(SubirNivel::handle).add();
        CH.messageBuilder(Frasco.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Frasco::write).decoder(Frasco::read).consumerMainThread(Frasco::handle).add();
        CH.messageBuilder(Bebe.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Bebe::write).decoder(Bebe::read).consumerMainThread(Bebe::handle).add();
        CH.messageBuilder(SantuarioUsar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SantuarioUsar::write).decoder(SantuarioUsar::read).consumerMainThread(SantuarioUsar::handle).add();
        CH.messageBuilder(Santuario.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Santuario::write).decoder(Santuario::read).consumerMainThread(Santuario::handle).add();
        CH.messageBuilder(RepartoFrascos.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RepartoFrascos::write).decoder(RepartoFrascos::read).consumerMainThread(RepartoFrascos::handle).add();
        CH.messageBuilder(EquipoAccion.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EquipoAccion::write).decoder(EquipoAccion::read).consumerMainThread(EquipoAccion::handle).add();
        CH.messageBuilder(Conseguido.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Conseguido::write).decoder(Conseguido::read).consumerMainThread(Conseguido::handle).add();
        CH.messageBuilder(Ataque.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Ataque::write).decoder(Ataque::read).consumerMainThread(Ataque::handle).add();
        CH.messageBuilder(Rodar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Rodar::write).decoder(Rodar::read).consumerMainThread(Rodar::handle).add();
        CH.messageBuilder(GolpeVisto.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(GolpeVisto::write).decoder(GolpeVisto::read).consumerMainThread(GolpeVisto::handle).add();
        CH.messageBuilder(GolpeNo.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((m, b) -> {}).decoder(b -> new GolpeNo()).consumerMainThread(GolpeNo::handle).add();
        CH.messageBuilder(Impacto.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Impacto::write).decoder(Impacto::read).consumerMainThread(Impacto::handle).add();
        CH.messageBuilder(Lanzar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Lanzar::write).decoder(Lanzar::read).consumerMainThread(Lanzar::handle).add();
        CH.messageBuilder(Memorizar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Memorizar::write).decoder(Memorizar::read).consumerMainThread(Memorizar::handle).add();
        CH.messageBuilder(Hablar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Hablar::write).decoder(Hablar::read).consumerMainThread(Hablar::handle).add();
        CH.messageBuilder(Dialogo.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Dialogo::write).decoder(Dialogo::read).consumerMainThread(Dialogo::handle).add();
        CH.messageBuilder(DialogoAccion.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DialogoAccion::write).decoder(DialogoAccion::read).consumerMainThread(DialogoAccion::handle).add();
        CH.messageBuilder(AbrirNivel.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((m, b) -> {}).decoder(b -> new AbrirNivel()).consumerMainThread(AbrirNivel::handle).add();
        CH.messageBuilder(AbrirForja.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AbrirForja::write).decoder(AbrirForja::read).consumerMainThread(AbrirForja::handle).add();
        CH.messageBuilder(Mejorar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Mejorar::write).decoder(Mejorar::read).consumerMainThread(Mejorar::handle).add();
        CH.messageBuilder(ForjaHecho.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ForjaHecho::write).decoder(ForjaHecho::read).consumerMainThread(ForjaHecho::handle).add();
        CH.messageBuilder(Ceniza.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Ceniza::write).decoder(Ceniza::read).consumerMainThread(Ceniza::handle).add();
        CH.messageBuilder(AbrirTienda.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AbrirTienda::write).decoder(AbrirTienda::read).consumerMainThread(AbrirTienda::handle).add();
        CH.messageBuilder(Comprar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Comprar::write).decoder(Comprar::read).consumerMainThread(Comprar::handle).add();
        CH.messageBuilder(TiendaHecho.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TiendaHecho::write).decoder(TiendaHecho::read).consumerMainThread(TiendaHecho::handle).add();
        CH.messageBuilder(AbrirClase.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AbrirClase::write).decoder(AbrirClase::read).consumerMainThread(AbrirClase::handle).add();
        CH.messageBuilder(ElegirClase.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ElegirClase::write).decoder(ElegirClase::read).consumerMainThread(ElegirClase::handle).add();
        CH.messageBuilder(Cosas.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Cosas::write).decoder(Cosas::read).consumerMainThread(Cosas::handle).add();
        CH.messageBuilder(CosaUsar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CosaUsar::write).decoder(CosaUsar::read).consumerMainThread(CosaUsar::handle).add();
        CH.messageBuilder(CineFase.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CineFase::write).decoder(CineFase::read).consumerMainThread(CineFase::handle).add();
        // el viaje entre santuarios de Morvain (parte 7c)
        CH.messageBuilder(Viajar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Viajar::write).decoder(Viajar::read).consumerMainThread(Viajar::handle).add();
        CH.messageBuilder(Fundido.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Fundido::write).decoder(Fundido::read).consumerMainThread(Fundido::handle).add();
        CH.messageBuilder(Conocidos.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Conocidos::write).decoder(Conocidos::read).consumerMainThread(Conocidos::handle).add();
        // los fragmentos del mapa de Morvain y viajar desde el mapa (parte 7d)
        CH.messageBuilder(Mapas.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Mapas::write).decoder(Mapas::read).consumerMainThread(Mapas::handle).add();
        CH.messageBuilder(EstelaUsar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EstelaUsar::write).decoder(EstelaUsar::read).consumerMainThread(EstelaUsar::handle).add();
        CH.messageBuilder(ViajarMapa.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ViajarMapa::write).decoder(ViajarMapa::read).consumerMainThread(ViajarMapa::handle).add();
        // el corcel de Morvain (parte 7b)
        CH.messageBuilder(CorcelLlamar.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder((m, b) -> {}).decoder(b -> new CorcelLlamar()).consumerMainThread(CorcelLlamar::handle).add();
        CH.messageBuilder(CorcelSalto.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CorcelSalto::write).decoder(CorcelSalto::read).consumerMainThread(CorcelSalto::handle).add();
    }

    // ------------------------------------------------------------------ el viaje entre santuarios (parte 7c)

    /** Viajar desde el santuario en el que se descansa al de Morvain {@code id}. */
    public record Viajar(String id) {
        void write(FriendlyByteBuf b) {
            b.writeUtf(id, 48);
        }

        static Viajar read(FriendlyByteBuf b) {
            return new Viajar(b.readUtf(48));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.morvain.Viaje.viajar(p, id);
            ctx.get().setPacketHandled(true);
        }
    }

    /** La pantalla se funde a negro y vuelve en {@code ticks} (el viaje entre santuarios). */
    public record Fundido(int ticks) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(ticks);
        }

        static Fundido read(FriendlyByteBuf b) {
            return new Fundido(Math.min(b.readVarInt(), 400));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.morvain.ClientViaje.fundido(ticks));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Los santuarios de Morvain que conoce (sus ids): para sus marcas en la brujula. */
    public record Conocidos(List<String> ids) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(ids.size());
            for (String s : ids) b.writeUtf(s, 48);
        }

        static Conocidos read(FriendlyByteBuf b) {
            int k = Math.min(b.readVarInt(), 64);
            List<String> l = new ArrayList<>();
            for (int i = 0; i < k; i++) l.add(b.readUtf(48));
            return new Conocidos(l);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.morvain.ClientViaje.conocidos(ids));
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ los fragmentos del mapa de Morvain (parte 7d)

    /** Las regiones de Morvain cuyo mapa tiene (sus ids): el mapa de la M las ensena dibujadas. */
    public record Mapas(List<String> ids) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(ids.size());
            for (String s : ids) b.writeUtf(s, 32);
        }

        static Mapas read(FriendlyByteBuf b) {
            int k = Math.min(b.readVarInt(), 32);
            List<String> l = new ArrayList<>();
            for (int i = 0; i < k; i++) l.add(b.readUtf(32));
            return new Mapas(l);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.morvain.ClientMapa.mapas(ids));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Usar la estela del mapa de pos (la tecla del aviso). */
    public record EstelaUsar(net.minecraft.core.BlockPos pos) {
        void write(FriendlyByteBuf b) {
            b.writeBlockPos(pos);
        }

        static EstelaUsar read(FriendlyByteBuf b) {
            return new EstelaUsar(b.readBlockPos());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.morvain.Fragmentos.usar(p, pos);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Viajar desde el mapa de la M al santuario de Morvain {@code id} (sin estar descansando). */
    public record ViajarMapa(String id) {
        void write(FriendlyByteBuf b) {
            b.writeUtf(id, 48);
        }

        static ViajarMapa read(FriendlyByteBuf b) {
            return new ViajarMapa(b.readUtf(48));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.morvain.Viaje.desdeMapa(p, id);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ el corcel (parte 7b)

    /** La tecla del corcel: que venga (o, montado, que se vaya). */
    public record CorcelLlamar() {
        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.montura.Montura.llamar(p);
            ctx.get().setPacketHandled(true);
        }
    }

    /** El corcel ha saltado (lo mueve el cliente del jinete): que suene y eche niebla para todos. */
    public record CorcelSalto(boolean doble) {
        void write(FriendlyByteBuf b) {
            b.writeBoolean(doble);
        }

        static CorcelSalto read(FriendlyByteBuf b) {
            return new CorcelSalto(b.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.montura.Montura.salto(p, doble);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ Brumavela (parte 3b)

    /** Las cosas de Brumavela que se usan con la tecla del aviso (las que hay ahora mismo; vacia al irse de la isla). */
    public record Cosas(java.util.List<com.darkcraft.pueblo.Pueblo.Cosa> lista, int piezas, boolean reparado) {
        void write(FriendlyByteBuf b) {
            b.writeByte(piezas);
            b.writeBoolean(reparado);
            b.writeVarInt(lista.size());
            for (com.darkcraft.pueblo.Pueblo.Cosa c : lista) {
                b.writeByte(c.id());
                b.writeInt(c.x());
                b.writeInt(c.y());
                b.writeInt(c.z());
                b.writeByte(c.accion());
            }
        }

        static Cosas read(FriendlyByteBuf b) {
            int piezas = b.readByte();
            boolean reparado = b.readBoolean();
            int n = Math.min(64, b.readVarInt());
            java.util.List<com.darkcraft.pueblo.Pueblo.Cosa> l = new java.util.ArrayList<>(n);
            for (int i = 0; i < n; i++) l.add(new com.darkcraft.pueblo.Pueblo.Cosa(b.readByte(), b.readInt(), b.readInt(), b.readInt(), b.readByte()));
            return new Cosas(l, piezas, reparado);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ClientPueblo.recibir(lista, piezas, reparado));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador pulsa la tecla del aviso junto a una cosa de Brumavela. */
    public record CosaUsar(int id) {
        void write(FriendlyByteBuf b) {
            b.writeByte(id);
        }

        static CosaUsar read(FriendlyByteBuf b) {
            return new CosaUsar(b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.pueblo.Pueblo.usar(p, id);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ las clases (parte 2f-3)

    /** Hay que elegir clase (al entrar por primera vez: {@code forzada}, no se puede cerrar; o con el comando). */
    public record AbrirClase(boolean forzada) {
        void write(FriendlyByteBuf b) {
            b.writeBoolean(forzada);
        }

        static AbrirClase read(FriendlyByteBuf b) {
            return new AbrirClase(b.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ClaseScreen.abrir(forzada));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El cliente elige su clase. */
    public record ElegirClase(String id) {
        void write(FriendlyByteBuf b) {
            b.writeUtf(id, 32);
        }

        static ElegirClase read(FriendlyByteBuf b) {
            return new ElegirClase(b.readUtf(32));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.rol.Clase.pedir(p, id);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ hechizos

    /** El cliente lanza un hechizo (con el baston o el sello en la mano). */
    public record Lanzar(String id) {
        void write(FriendlyByteBuf b) {
            b.writeUtf(id, 32);
        }

        static Lanzar read(FriendlyByteBuf b) {
            return new Lanzar(b.readUtf(32));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.hechizos.Lanzar.pedir(p, id);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ hablar con los personajes

    /** El jugador quiere hablar con el personaje {@code entidad} (la tecla del aviso). */
    public record Hablar(int entidad) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
        }

        static Hablar read(FriendlyByteBuf b) {
            return new Hablar(b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.dialogo.Conversaciones.hablar(p, entidad);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Empieza una conversacion con el personaje {@code entidad} por el nodo {@code nodo}. */
    public record Dialogo(int entidad, String personaje, int nodo) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeUtf(personaje, 32);
            b.writeVarInt(nodo);
        }

        static Dialogo read(FriendlyByteBuf b) {
            return new Dialogo(b.readVarInt(), b.readUtf(32), b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.DialogoScreen.abrir(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Al acabar una frase con accion (subir de nivel con la guia...), el cliente se lo pide al servidor. */
    public record DialogoAccion(int entidad, String accion) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeUtf(accion, 32);
        }

        static DialogoAccion read(FriendlyByteBuf b) {
            return new DialogoAccion(b.readVarInt(), b.readUtf(32));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.dialogo.Conversaciones.accion(p, entidad, accion);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Se abre subir de nivel (lo pide la guia). */
    public record AbrirNivel() {
        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.DialogoScreen.abrirNivel());
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ la forja de Dorvan y la tienda de Kerrick

    /** Se abre la forja de Dorvan ({@code entidad}). */
    public record AbrirForja(int entidad) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
        }

        static AbrirForja read(FriendlyByteBuf b) {
            return new AbrirForja(b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ForjaScreen.abrir(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El cliente pide mejorar lo que hay en el hueco {@code hueco} de su inventario. */
    public record Mejorar(int entidad, int hueco) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeVarInt(hueco);
        }

        static Mejorar read(FriendlyByteBuf b) {
            return new Mejorar(b.readVarInt(), b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.forja.Forja.mejorar(p, entidad, hueco);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Lo que ha pasado al forjar ({@code forja.Forja}: HECHO, FALTA, TOPE o NO) y como queda el arma. */
    public record ForjaHecho(int entidad, int hueco, int nivel, int codigo) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeVarInt(hueco);
            b.writeByte(nivel);
            b.writeByte(codigo);
        }

        static ForjaHecho read(FriendlyByteBuf b) {
            return new ForjaHecho(b.readVarInt(), b.readVarInt(), b.readByte(), b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ForjaScreen.hecho(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El cliente pide ponerle al arma del hueco {@code arma} la ceniza del hueco {@code ceniza} (-1: quitarle la suya). */
    public record Ceniza(int entidad, int arma, int ceniza) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeVarInt(arma);
            b.writeVarInt(ceniza + 1);
        }

        static Ceniza read(FriendlyByteBuf b) {
            return new Ceniza(b.readVarInt(), b.readVarInt(), b.readVarInt() - 1);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.forja.Forja.ceniza(p, entidad, arma, ceniza);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Se abre la tienda de Kerrick ({@code entidad}) con lo que ya ha comprado el jugador de cada cosa. */
    public record AbrirTienda(int entidad, int[] comprados) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeVarIntArray(comprados);
        }

        static AbrirTienda read(FriendlyByteBuf b) {
            return new AbrirTienda(b.readVarInt(), b.readVarIntArray(64));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.TiendaScreen.abrir(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El cliente compra {@code veces} la oferta {@code oferta}. */
    public record Comprar(int entidad, int oferta, int veces) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeVarInt(oferta);
            b.writeVarInt(veces);
        }

        static Comprar read(FriendlyByteBuf b) {
            return new Comprar(b.readVarInt(), b.readVarInt(), b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.tienda.Tienda.comprar(p, entidad, oferta, veces);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Lo que ha pasado al comprar ({@code tienda.Tienda}: HECHO, FALTA, AGOTADO o NO) y lo comprado ya de esa oferta. */
    public record TiendaHecho(int entidad, int oferta, int comprado, int codigo) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entidad);
            b.writeVarInt(oferta);
            b.writeVarInt(comprado);
            b.writeByte(codigo);
        }

        static TiendaHecho read(FriendlyByteBuf b) {
            return new TiendaHecho(b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.TiendaScreen.hecho(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El menu del santuario deja memorizados estos hechizos. */
    public record Memorizar(String[] ids) {
        void write(FriendlyByteBuf b) {
            b.writeByte(ids.length);
            for (String i : ids) b.writeUtf(i, 32);
        }

        static Memorizar read(FriendlyByteBuf b) {
            String[] r = new String[Math.max(0, Math.min(16, b.readByte()))];
            for (int i = 0; i < r.length; i++) r[i] = b.readUtf(32);
            return new Memorizar(r);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.rol.Memoria.memorizar(p, ids);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ el combate propio

    /** El cliente empieza un golpe (accion de {@code combate.Armas}, paso de la cadena, mano izquierda, ticks cargados). */
    public record Ataque(int accion, int paso, boolean izquierda, int carga) {
        void write(FriendlyByteBuf b) {
            b.writeByte(accion);
            b.writeByte(paso);
            b.writeBoolean(izquierda);
            b.writeByte(carga);
        }

        static Ataque read(FriendlyByteBuf b) {
            return new Ataque(b.readByte(), b.readByte(), b.readBoolean(), b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.combate.Combate.pedir(p, accion, paso, izquierda, carga);
            ctx.get().setPacketHandled(true);
        }
    }

    /** El cliente rueda hacia (dx, dz) (o da un paso atras). */
    public record Rodar(float dx, float dz, boolean atras) {
        void write(FriendlyByteBuf b) {
            b.writeFloat(dx);
            b.writeFloat(dz);
            b.writeBoolean(atras);
        }

        static Rodar read(FriendlyByteBuf b) {
            return new Rodar(b.readFloat(), b.readFloat(), b.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.combate.Combate.rodar(p, dx, dz, atras);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Otro jugador empieza un golpe, una carga o una voltereta (extra: el rumbo de la voltereta en grados). */
    public record GolpeVisto(int entity, int accion, int paso, boolean izquierda, int tipo, int extra) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(entity);
            b.writeByte(accion);
            b.writeByte(paso);
            b.writeBoolean(izquierda);
            b.writeByte(tipo);
            b.writeShort(extra);
        }

        static GolpeVisto read(FriendlyByteBuf b) {
            return new GolpeVisto(b.readVarInt(), b.readByte(), b.readByte(), b.readBoolean(), b.readByte(), b.readShort());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.combate.Poses.visto(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El servidor no deja el golpe o la voltereta que pidio el cliente (sin estamina, bebiendo...). */
    public record GolpeNo() {
        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.combate.ClientCombate.rechazado());
            ctx.get().setPacketHandled(true);
        }
    }

    /** El golpe del jugador ha alcanzado a alguien (la camara se sacude; mas si es fuerte). */
    public record Impacto(boolean fuerte) {
        void write(FriendlyByteBuf b) {
            b.writeBoolean(fuerte);
        }

        static Impacto read(FriendlyByteBuf b) {
            return new Impacto(b.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.combate.ClientCombate.impacto(fuerte));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador acaba de recoger algo: sale abajo en el centro (el dibujo, el nombre y cuantos). */
    public record Conseguido(net.minecraft.world.item.ItemStack objeto) {
        void write(FriendlyByteBuf b) {
            b.writeItem(objeto);
        }

        static Conseguido read(FriendlyByteBuf b) {
            return new Conseguido(b.readItem());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.Conseguidos.recibir(this));
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ rol: el equipo (lo que pide el menu del mod)

    /**
     * El menu del mod pide algo con el equipo: {@link #MOVER} cambia entre si lo de dos huecos del inventario (a y b),
     * {@link #GUARDAR} deja en la bolsa lo del hueco a, {@link #TIRAR} lo tira al suelo e {@link #IZQUIERDA} pasa la mano
     * izquierda a su repuesto (a = 3 o 4, o -1 para el siguiente). Los huecos son los de {@code rol.Equipo}.
     */
    public record EquipoAccion(int que, int a, int b) {
        /** Y los talismanes: PONER el del hueco b del inventario en el hueco de talisman a; QUITAR el del hueco a. */
        public static final int MOVER = 0, GUARDAR = 1, TIRAR = 2, IZQUIERDA = 3, TALISMAN_PONER = 4, TALISMAN_QUITAR = 5;

        void write(FriendlyByteBuf b2) {
            b2.writeByte(que);
            b2.writeByte(a);
            b2.writeByte(b);
        }

        static EquipoAccion read(FriendlyByteBuf b) {
            return new EquipoAccion(b.readByte(), b.readByte(), b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) {
                switch (que) {
                    case MOVER -> com.darkcraft.rol.Equipo.mover(p, a, b);
                    case GUARDAR -> com.darkcraft.rol.Equipo.guardar(p, a);
                    case TIRAR -> com.darkcraft.rol.Equipo.tirar(p, a);
                    case IZQUIERDA -> com.darkcraft.rol.Equipo.manoIzquierda(p, a);
                    case TALISMAN_PONER -> com.darkcraft.rol.Talismanes.poner(p, a, b);
                    case TALISMAN_QUITAR -> com.darkcraft.rol.Talismanes.quitar(p, a);
                    default -> {
                    }
                }
            }
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ rol: la ficha, subir de nivel, frascos y santuarios

    /**
     * La ficha del jugador: sus ocho atributos (de ellos salen el nivel, la vida, la estamina...), el mana maximo y sus
     * frascos (cuantos lleva de vida y de mana, cuantos le quedan y su mejora).
     */
    public record FichaRol(int[] atributos, int manaMax, int frascosVida, int frascosMana, int vida, int mana, int mejora,
                           int huecosTalisman, String[] talismanes, String[] memoria) {
        void write(FriendlyByteBuf b) {
            b.writeVarIntArray(atributos);
            b.writeVarInt(manaMax);
            b.writeVarInt(frascosVida);
            b.writeVarInt(frascosMana);
            b.writeVarInt(vida);
            b.writeVarInt(mana);
            b.writeVarInt(mejora);
            b.writeByte(huecosTalisman);
            b.writeByte(talismanes.length);
            for (String t : talismanes) b.writeUtf(t, 128);
            b.writeByte(memoria.length);
            for (String m : memoria) b.writeUtf(m, 32);
        }

        static FichaRol read(FriendlyByteBuf b) {
            int[] at = b.readVarIntArray(16);
            int mm = b.readVarInt(), fv = b.readVarInt(), fm = b.readVarInt(), v = b.readVarInt(), m = b.readVarInt(), mj = b.readVarInt();
            int huecos = b.readByte();
            String[] t = new String[Math.max(0, Math.min(8, b.readByte()))];
            for (int i = 0; i < t.length; i++) t[i] = b.readUtf(128);
            String[] mem = new String[Math.max(0, Math.min(16, b.readByte()))];
            for (int i = 0; i < mem.length; i++) mem[i] = b.readUtf(32);
            return new FichaRol(at, mm, fv, fm, v, m, mj, huecos, t, mem);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ClientRol.ficha(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador, descansando en un santuario, confirma los atributos que quiere (el servidor comprueba y cobra los ecos). */
    public record SubirNivel(int[] atributos) {
        void write(FriendlyByteBuf b) {
            b.writeVarIntArray(atributos);
        }

        static SubirNivel read(FriendlyByteBuf b) {
            return new SubirNivel(b.readVarIntArray(16));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.rol.Rol.subir(p, atributos);
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador quiere beber de un frasco (0 el de vida, 1 el de mana). */
    public record Frasco(int tipo) {
        void write(FriendlyByteBuf b) {
            b.writeByte(tipo);
        }

        static Frasco read(FriendlyByteBuf b) {
            return new Frasco(b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.rol.Frascos.pedir(p, tipo);
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * Alguien bebe de un frasco (lo ven todos los que le ven: levanta el frasco) o, solo para el que lo pidio, por que no
     * puede: {@link #VACIO} no le quedan, {@link #LLENO} no le hace falta.
     */
    public record Bebe(int entity, int tipo, int que) {
        public static final int BEBE = 0, VACIO = 1, LLENO = 2;

        void write(FriendlyByteBuf b) {
            b.writeVarInt(entity);
            b.writeByte(tipo);
            b.writeByte(que);
        }

        static Bebe read(FriendlyByteBuf b) {
            return new Bebe(b.readVarInt(), b.readByte(), b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.Bebida.recibir(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador usa el santuario que tiene delante con la tecla (lo mismo que hacerle clic derecho). */
    public record SantuarioUsar(net.minecraft.core.BlockPos pos) {
        void write(FriendlyByteBuf b) {
            b.writeBlockPos(pos);
        }

        static SantuarioUsar read(FriendlyByteBuf b) {
            return new SantuarioUsar(b.readBlockPos());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.santuario.Santuarios.usar(p, pos);
            ctx.get().setPacketHandled(true);
        }
    }

    /** El jugador descansa en un santuario: se abre su menu (con el nombre del santuario). */
    /**
     * Se ha descansado en un santuario: abre su menu. En Morvain (parte 7c) lleva tambien a cuales se puede viajar (los ids
     * de los santuarios de Morvain que conoce) y en cual esta ({@code aqui}, "" fuera de Morvain).
     */
    public record Santuario(net.minecraft.core.BlockPos pos, String nombre, List<String> destinos, String aqui) {
        void write(FriendlyByteBuf b) {
            b.writeBlockPos(pos);
            b.writeUtf(nombre, 96);
            b.writeVarInt(destinos.size());
            for (String d : destinos) b.writeUtf(d, 48);
            b.writeUtf(aqui, 48);
        }

        static Santuario read(FriendlyByteBuf b) {
            net.minecraft.core.BlockPos p = b.readBlockPos();
            String n = b.readUtf(96);
            int k = Math.min(b.readVarInt(), 64);
            List<String> l = new ArrayList<>();
            for (int i = 0; i < k; i++) l.add(b.readUtf(48));
            return new Santuario(p, n, l, b.readUtf(48));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.SantuarioScreen.abrir(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** En el santuario, el jugador reparte sus frascos: cuantos quiere de vida (los demas, de mana). */
    public record RepartoFrascos(int vida) {
        void write(FriendlyByteBuf b) {
            b.writeByte(vida);
        }

        static RepartoFrascos read(FriendlyByteBuf b) {
            return new RepartoFrascos(b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.rol.Frascos.repartir(p, vida);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ rol: ecos, espada de luz, estamina y carteles

    /**
     * Lo que sabe el cliente del rol del jugador: sus Ecos de la Sombra, cuantos acaba de ganar (para el "+120"), si esta
     * en combate y su espada de luz (donde murio, con los ecos que guarda), si la hay.
     */
    public record RolEstado(int ecos, int ganados, boolean combate, boolean espada, String dim, double x, double y, double z,
                            int ecosEspada) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(ecos);
            b.writeVarInt(ganados);
            b.writeBoolean(combate);
            b.writeBoolean(espada);
            if (espada) {
                b.writeUtf(dim, 128);
                b.writeDouble(x);
                b.writeDouble(y);
                b.writeDouble(z);
                b.writeVarInt(ecosEspada);
            }
        }

        static RolEstado read(FriendlyByteBuf b) {
            int ecos = b.readVarInt(), ganados = b.readVarInt();
            boolean combate = b.readBoolean(), espada = b.readBoolean();
            if (!espada) return new RolEstado(ecos, ganados, combate, false, "", 0, 0, 0, 0);
            return new RolEstado(ecos, ganados, combate, true, b.readUtf(128), b.readDouble(), b.readDouble(), b.readDouble(), b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ClientRol.estado(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** La estamina (en decimas), su maximo y si esta agotado (no puede correr, saltar ni atacar hasta recuperarse un poco). */
    public record Estamina(int decimas, int max, boolean agotado) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(decimas);
            b.writeVarInt(max);
            b.writeBoolean(agotado);
        }

        static Estamina read(FriendlyByteBuf b) {
            return new Estamina(b.readVarInt(), b.readVarInt(), b.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ClientRol.estamina(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * Un cartel en la pantalla: jefe abatido (la clave es su nombre), zona descubierta, ecos recuperados (n), santuario
     * encendido (la clave es su nombre) o un aviso corto (la clave es el texto). El 5 es de los carteles del cliente.
     */
    public record Cartel(int tipo, String clave, int n) {
        public static final int MUERTE = 0, JEFE = 1, ZONA = 2, ECOS = 3, GUARDIA = 4, SANTUARIO = 6, AVISO = 7;
        /** Una pieza del barco encontrada (la clave es su nombre; n, cuantas van) y el barco reparado. */
        public static final int PIEZA = 8, BARCO = 9;
        /** Un fragmento del mapa de Morvain (la clave es el nombre de la region; n, cuantos van). */
        public static final int FRAGMENTO = 10;

        void write(FriendlyByteBuf b) {
            b.writeByte(tipo);
            b.writeUtf(clave, 128);
            b.writeVarInt(n);
        }

        static Cartel read(FriendlyByteBuf b) {
            return new Cartel(b.readByte(), b.readUtf(128), b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.Carteles.recibir(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Ecos que salen volando de un enemigo caido hacia el jugador (n: cuantos, para el tamano del rastro). */
    public record Ecos(double x, double y, double z, int n) {
        void write(FriendlyByteBuf b) {
            b.writeDouble(x);
            b.writeDouble(y);
            b.writeDouble(z);
            b.writeVarInt(n);
        }

        static Ecos read(FriendlyByteBuf b) {
            return new Ecos(b.readDouble(), b.readDouble(), b.readDouble(), b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ui.ClientRol.vuelan(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Del cliente: ha pulsado la tecla junto a su espada de luz (recoger los ecos). */
    public record Recuperar() {
        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.rol.Rol.recuperar(p);
            ctx.get().setPacketHandled(true);
        }
    }

    /** Si el jugador esta en la zona de una Fortaleza de Hueso (su musica). */
    public record Fortaleza(boolean dentro) {
        void write(FriendlyByteBuf b) {
            b.writeBoolean(dentro);
        }

        static Fortaleza read(FriendlyByteBuf b) {
            return new Fortaleza(b.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.ClientMusic.fortaleza(dentro));
            ctx.get().setPacketHandled(true);
        }
    }

    /** El cielo rojo (parte 5e): si esta, desde que tick del mundo y las columnas de luz ({x, y, z} seguidos). */
    public record CieloRojo(boolean activo, long desde, int[] columnas) {
        void write(FriendlyByteBuf b) {
            b.writeBoolean(activo);
            b.writeLong(desde);
            b.writeVarIntArray(columnas);
        }

        static CieloRojo read(FriendlyByteBuf b) {
            return new CieloRojo(b.readBoolean(), b.readLong(), b.readVarIntArray());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.fortaleza.CieloRojoCliente.recibir(activo, desde, columnas));
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ Espejo de la Sombra

    /** Un lugar conocido del mapa (estado 1 conocido, 2 hecho: cofre leido o jefe vencido). */
    public record LugarE(String id, int tipo, int icono, int x, int y, int z, String nombre, int cap, int estado) {}

    /** Un logro del mod: id, icono (objeto), marco (0 task, 1 challenge, 2 goal), secreto, hecho, claves del texto. */
    public record LogroE(String id, String icono, int marco, boolean secreto, boolean hecho, String titulo, String desc) {}

    /** Una entrada del diario: dia del mundo y clave (darkcraft.diario.CLAVE). */
    public record DiarioE(int dia, String clave) {}

    /** Otro jugador en la misma dimension. */
    public record Amigo(java.util.UUID id, String nombre, int x, int y, int z) {}

    /** Todo lo que sabe el espejo de este jugador (aviso: se acaba de despertar). */
    public record EspejoEstado(boolean activo, boolean aviso, int leidos, List<String> coleccion, List<String> bestiario,
                               List<LugarE> lugares, List<LogroE> logros, List<DiarioE> diario, int perdidos) {
        void write(FriendlyByteBuf b) {
            b.writeBoolean(activo);
            b.writeBoolean(aviso);
            b.writeVarInt(leidos);
            b.writeVarInt(perdidos);
            b.writeCollection(coleccion, (bb, s) -> bb.writeUtf(s, 64));
            b.writeCollection(bestiario, (bb, s) -> bb.writeUtf(s, 64));
            b.writeCollection(lugares, (bb, l) -> {
                bb.writeUtf(l.id(), 96);
                bb.writeVarInt(l.tipo());
                bb.writeVarInt(l.icono());
                bb.writeInt(l.x());
                bb.writeInt(l.y());
                bb.writeInt(l.z());
                bb.writeUtf(l.nombre(), 96);
                bb.writeVarInt(l.cap());
                bb.writeVarInt(l.estado());
            });
            b.writeCollection(logros, (bb, l) -> {
                bb.writeUtf(l.id(), 64);
                bb.writeUtf(l.icono(), 96);
                bb.writeVarInt(l.marco());
                bb.writeBoolean(l.secreto());
                bb.writeBoolean(l.hecho());
                bb.writeUtf(l.titulo(), 128);
                bb.writeUtf(l.desc(), 128);
            });
            b.writeCollection(diario, (bb, e) -> {
                bb.writeVarInt(e.dia());
                bb.writeUtf(e.clave(), 64);
            });
        }

        static EspejoEstado read(FriendlyByteBuf b) {
            boolean activo = b.readBoolean(), aviso = b.readBoolean();
            int leidos = b.readVarInt(), perdidos = b.readVarInt();
            List<String> col = b.readList(bb -> bb.readUtf(64));
            List<String> bes = b.readList(bb -> bb.readUtf(64));
            List<LugarE> ls = b.readList(bb -> new LugarE(bb.readUtf(96), bb.readVarInt(), bb.readVarInt(), bb.readInt(), bb.readInt(),
                    bb.readInt(), bb.readUtf(96), bb.readVarInt(), bb.readVarInt()));
            List<LogroE> lg = b.readList(bb -> new LogroE(bb.readUtf(64), bb.readUtf(96), bb.readVarInt(), bb.readBoolean(),
                    bb.readBoolean(), bb.readUtf(128), bb.readUtf(128)));
            List<DiarioE> di = b.readList(bb -> new DiarioE(bb.readVarInt(), bb.readUtf(64)));
            return new EspejoEstado(activo, aviso, leidos, col, bes, ls, lg, di, perdidos);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.espejo.EspejoCliente.estado(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Cada segundo: el sitio donde esta el jugador (clave de idioma o "") y los demas jugadores de su dimension. */
    public record EspejoPos(String lugar, List<Amigo> amigos) {
        void write(FriendlyByteBuf b) {
            b.writeUtf(lugar, 96);
            b.writeCollection(amigos, (bb, a) -> {
                bb.writeUUID(a.id());
                bb.writeUtf(a.nombre(), 32);
                bb.writeInt(a.x());
                bb.writeInt(a.y());
                bb.writeInt(a.z());
            });
        }

        static EspejoPos read(FriendlyByteBuf b) {
            String l = b.readUtf(96);
            List<Amigo> a = b.readList(bb -> new Amigo(bb.readUUID(), bb.readUtf(32), bb.readInt(), bb.readInt(), bb.readInt()));
            return new EspejoPos(l, a);
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.espejo.EspejoCliente.pos(this));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Del cliente: ha abierto el libro (0) o ha escrito una marca (1). */
    public record EspejoAccion(int tipo) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(tipo);
        }

        static EspejoAccion read(FriendlyByteBuf b) {
            return new EspejoAccion(b.readVarInt());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) com.darkcraft.espejo.Espejo.accion(p, tipo);
            ctx.get().setPacketHandled(true);
        }
    }

    // ------------------------------------------------------------------ jefe

    /**
     * Aviso en el suelo de donde va a caer un golpe: forma, centro, medidas (a = radio o largo,
     * b = radio interior o ancho, ang = hacia donde mira en grados), cuanto falta (ticks) y color.
     */
    public record Aviso(int forma, double x, double y, double z, float a, float b, float ang, int dur, int color) {
        /** ONDA: anillo que se abre desde el centro (a = radio maximo, b = bloques por tick). */
        public static final int CIRCULO = 0, ANILLO = 1, LINEA = 2, SECTOR = 3, ONDA = 4;
        public static final int ROJO = 0, ORO = 1, NARANJA = 2, VERDE = 3;

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

    /** El Rey (lineas 1..13) o Ser Gavran (100 + linea) dice una frase (voz y subtitulo). */
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

    /** El mana (la energia de los combos) para la barra: pasa de 127 con la Mente alta, asi que va como VarInt. */
    public record Energia(int valor) {
        void write(FriendlyByteBuf b) {
            b.writeVarInt(Math.max(0, valor));
        }

        static Energia read(FriendlyByteBuf b) {
            return new Energia(b.readVarInt());
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

    /**
     * El cliente dice por donde va un viaje del barco (cinematicas 40-42): ya tiene la pantalla en negro en el corte
     * {@code fase} (1: el barco de bloques deja paso al que vuela; 2: el salto al otro amarre; 3: vuelve a ser de bloques)
     * y el servidor puede cambiar el mundo; {@link Cinematics#FIN_CLIENTE}: ha acabado de verla.
     */
    public record CineFase(int fase) {
        void write(FriendlyByteBuf b) {
            b.writeByte(fase);
        }

        static CineFase read(FriendlyByteBuf b) {
            return new CineFase(b.readByte());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) Cinematics.fase(p, fase);
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
