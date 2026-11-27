package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.santuario.SantuarioBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Los santuarios en el cliente: cual tiene el jugador a mano (para el aviso de "Pulsa [E]..."), y la vida de los que
 * tiene cerca: llamitas y motas azules que suben del fuego, alguna ceniza de los apagados y el rumor del fuego de almas.
 * (La llama grande es del modelo del bloque; esto es lo que se mueve alrededor.)
 */
public final class ClientSantuario {
    private ClientSantuario() {}

    /** El santuario que el jugador tiene a mano (o null) y si esta encendido. */
    public static BlockPos aMano;
    public static boolean aManoEncendido;

    private static final List<BlockPos> ENCENDIDOS = new ArrayList<>(), APAGADOS = new ArrayList<>();
    private static int rumor;
    private static final DustColorTransitionOptions MOTA = new DustColorTransitionOptions(new Vector3f(0.72f, 0.93f, 1.0f),
            new Vector3f(0.12f, 0.3f, 0.85f), 0.7f);
    /** Las motas de los fuegos frios (despues del final de la nueva Sombra, parte 7ad): blancas que se vuelven grises. */
    private static final DustColorTransitionOptions MOTA_FRIA = new DustColorTransitionOptions(new Vector3f(0.92f, 0.95f, 1.0f),
            new Vector3f(0.55f, 0.58f, 0.64f), 0.7f);

    static void olvidar() {
        aMano = null;
        ENCENDIDOS.clear();
        APAGADOS.clear();
        rumor = 0;
    }

    /** Lo llama {@link ClientRol} en cada tick del cliente. */
    static void tick(Minecraft mc) {
        ClientLevel lvl = mc.level;
        if (ClientRol.ticks % 4 == 0) buscarAMano(mc, lvl);
        if (ClientRol.ticks % 20 == 1) buscarCerca(mc, lvl);
        if (rumor > 0) rumor--;
        RandomSource r = lvl.random;
        BlockPos masCerca = null;
        double mejor = Double.MAX_VALUE;
        for (BlockPos p : ENCENDIDOS) {
            double x = p.getX() + 0.5, y = p.getY() + 0.8, z = p.getZ() + 0.5;
            if (r.nextInt(3) == 0)
                lvl.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + (r.nextDouble() - 0.5) * 0.5, y + 0.1 + r.nextDouble() * 0.5,
                        z + (r.nextDouble() - 0.5) * 0.5, 0, 0.012 + r.nextDouble() * 0.02, 0);
            boolean frio = com.darkcraft.client.morvain.CieloFinal.frio();
            if (r.nextInt(6) == 0)
                lvl.addParticle(frio ? MOTA_FRIA : MOTA, x + (r.nextDouble() - 0.5) * 0.7, y + 0.5 + r.nextDouble() * 0.9, z + (r.nextDouble() - 0.5) * 0.7,
                        (r.nextDouble() - 0.5) * 0.02, 0.03 + r.nextDouble() * 0.03, (r.nextDouble() - 0.5) * 0.02);
            // los fuegos saben lo que hiciste: arden frios (copos que suben de la llama)
            if (frio && r.nextInt(3) == 0)
                lvl.addParticle(ParticleTypes.SNOWFLAKE, x + (r.nextDouble() - 0.5) * 0.6, y + 0.2 + r.nextDouble() * 0.6, z + (r.nextDouble() - 0.5) * 0.6,
                        (r.nextDouble() - 0.5) * 0.02, 0.02 + r.nextDouble() * 0.03, (r.nextDouble() - 0.5) * 0.02);
            double d = mc.player.distanceToSqr(x, y, z);
            if (d < mejor) {
                mejor = d;
                masCerca = p;
            }
        }
        for (BlockPos p : APAGADOS)
            if (r.nextInt(26) == 0)
                lvl.addParticle(ParticleTypes.WHITE_ASH, p.getX() + 0.5 + (r.nextDouble() - 0.5) * 0.5, p.getY() + 0.9, p.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.5,
                        0, 0.01, 0);
        // el rumor del fuego: del mas cercano, y se vuelve a lanzar antes de que acabe para que no haya silencios
        if (masCerca != null && mejor < 15 * 15 && rumor <= 0) {
            lvl.playLocalSound(masCerca.getX() + 0.5, masCerca.getY() + 1.0, masCerca.getZ() + 0.5, DarkCraftMod.SFX_SANTUARIO_FUEGO.get(),
                    SoundSource.BLOCKS, 0.6f, 0.96f + r.nextFloat() * 0.08f, false);
            rumor = 92;
        }
    }

    /**
     * El santuario que el jugador tiene a mano: a menos de 3,6 bloques y mirando hacia el (o pegado a el). Solo con estar
     * cerca no basta: la tecla del aviso es la del inventario, y al lado de un santuario hay que poder abrirlo mirando a
     * otro lado.
     */
    private static void buscarAMano(Minecraft mc, ClientLevel lvl) {
        BlockPos c = mc.player.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        BlockPos mejor = null;
        boolean encendido = false;
        double dMejor = 3.6 * 3.6;
        net.minecraft.world.phys.Vec3 ojo = mc.player.getEyePosition(), mira = mc.player.getViewVector(1f);
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -3; dy <= 2; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    BlockState st = lvl.getBlockState(m.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz));
                    if (!(st.getBlock() instanceof SantuarioBlock)) continue;
                    double d = mc.player.distanceToSqr(m.getX() + 0.5, m.getY() + 0.5, m.getZ() + 0.5);
                    if (d >= dMejor) continue;
                    // hacia el cuenco (un poco por encima del centro del bloque): dentro de un cono de unos 33 grados
                    net.minecraft.world.phys.Vec3 a = new net.minecraft.world.phys.Vec3(m.getX() + 0.5 - ojo.x, m.getY() + 0.8 - ojo.y, m.getZ() + 0.5 - ojo.z);
                    double largo = a.length();
                    if (d > 1.5 * 1.5 && largo > 1e-3 && a.dot(mira) / largo < 0.84) continue;
                    dMejor = d;
                    mejor = m.immutable();
                    encendido = st.getValue(SantuarioBlock.ENCENDIDO);
                }
        aMano = mejor;
        aManoEncendido = encendido;
    }

    /** Los santuarios de alrededor (14 bloques), una vez por segundo. */
    private static void buscarCerca(Minecraft mc, ClientLevel lvl) {
        ENCENDIDOS.clear();
        APAGADOS.clear();
        BlockPos c = mc.player.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -14; dx <= 14; dx++)
            for (int dz = -14; dz <= 14; dz++) {
                if (!lvl.hasChunkAt(m.set(c.getX() + dx, c.getY(), c.getZ() + dz))) continue;
                for (int dy = -6; dy <= 6; dy++) {
                    BlockState st = lvl.getBlockState(m.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz));
                    if (!(st.getBlock() instanceof SantuarioBlock)) continue;
                    (st.getValue(SantuarioBlock.ENCENDIDO) ? ENCENDIDOS : APAGADOS).add(m.immutable());
                }
            }
    }
}
