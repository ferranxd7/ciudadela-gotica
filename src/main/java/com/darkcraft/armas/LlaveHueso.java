package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.fortaleza.FortalezaEventos;
import com.darkcraft.fortaleza.Fortalezas;
import com.darkcraft.fortaleza.Puente;
import com.darkcraft.gen.TorreCupula;
import com.darkcraft.gen.FortalezaPlan;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * La llave de hueso que guardaba Ser Gavran: "te mostrara el camino hacia fortalezas que flotan mas alla de las nubes".
 * En la mano deja un rastro de luz hacia su Fortaleza de Hueso; con clic derecho tira mas fuerte (y dice hacia donde y a
 * cuantos bloques). Bajo la fortaleza levanta el puente de hueso y nubes, y delante de la reja abre la puerta.
 */
public class LlaveHueso extends Item {
    public LlaveHueso(Properties p) {
        super(p);
    }

    /**
     * La fortaleza de esta llave (se queda apuntada en la llave): la del reino donde se gano (la apunta el cofre de Ser
     * Gavran); si no lo tiene, la de la fortaleza donde se esta (a menos de 400 bloques) o la del reino mas cercano. No la
     * fortaleza mas cercana sin mas: la de un reino vecino puede quedar mas cerca que la del propio.
     */
    public static Fortalezas.Sitio destino(ServerPlayer p, ItemStack st) {
        CompoundTag t = st.getOrCreateTag();
        long seed = p.serverLevel().getSeed();
        if (t.contains("fortX")) return new Fortalezas.Sitio(t.getInt("fortX"), t.getInt("fortZ"), t.getInt("reinoX"), t.getInt("reinoZ"));
        Fortalezas.Sitio s = Fortalezas.cercana(seed, p.getX(), p.getZ(), 400);
        if (s == null) s = Fortalezas.delReino(seed, p.getX(), p.getZ());
        if (s == null) return null;
        apuntar(st, s);
        return s;
    }

    /** Apunta en la llave su fortaleza. */
    public static ItemStack apuntar(ItemStack st, Fortalezas.Sitio s) {
        if (s == null) return st;
        CompoundTag t = st.getOrCreateTag();
        t.putInt("fortX", s.x());
        t.putInt("fortZ", s.z());
        t.putInt("reinoX", s.reinoX());
        t.putInt("reinoZ", s.reinoZ());
        return st;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p, InteractionHand hand) {
        ItemStack st = p.getItemInHand(hand);
        if (p instanceof ServerPlayer sp) usar(sp, st);
        return InteractionResultHolder.sidedSuccess(st, lvl.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer sp) usar(sp, ctx.getItemInHand());
        return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
    }

    private void usar(ServerPlayer p, ItemStack st) {
        if (p.getCooldowns().isOnCooldown(this)) return;
        p.getCooldowns().addCooldown(this, 30);
        ServerLevel lvl = p.serverLevel();
        if (lvl.dimension() != Level.OVERWORLD) {
            p.displayClientMessage(Component.translatable("darkcraft.llave.nada").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            lvl.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SKELETON_AMBIENT, SoundSource.PLAYERS, 0.4f, 0.5f);
            return;
        }
        Fortalezas.Sitio s = destino(p, st);
        if (s == null) return;
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        lvl.playSound(null, p.getX(), p.getY(), p.getZ(), DarkCraftMod.SFX_LLAVE_RASTRO.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        // delante de la reja: abre la puerta
        int[] r = f.f.reja();
        double dPuerta = Math.hypot(p.getX() - (f.cx + 0.5), p.getZ() - (r[2] + 0.5));
        if (dPuerta < 13 && Math.abs(p.getY() - f.Y) < 6) {
            if (FortalezaEventos.abrirPuerta(p, s))
                p.displayClientMessage(Component.translatable("darkcraft.llave.puerta").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
            else p.displayClientMessage(Component.translatable("darkcraft.llave.puerta_ya").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return;
        }
        // en el mirador de la montana, delante del arco de colmillos: tiende el puente largo hasta la isla
        int[] a = Puente.arco(f);
        double dArco = Math.hypot(p.getX() - (a[0] + 0.5), p.getZ() - (a[2] + 0.5));
        if (dArco < 14 && Math.abs(p.getY() - a[1]) < 8) {
            if (FortalezaEventos.levantarPuente(p, s))
                p.displayClientMessage(Component.translatable("darkcraft.llave.puente").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
            else {
                p.displayClientMessage(Component.translatable("darkcraft.llave.puente_ya").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
                rastro(p, new Vec3(f.cx + 0.5, f.Y, f.bordeZ + 0.5), 40, 18);
            }
            return;
        }
        // lejos: el rastro tira fuerte hacia donde toca (el pie de la montana, el mirador o la puerta) y dice hacia donde
        Vec3 obj = objetivo(p, s, f);
        rastro(p, obj, 60, 30);
        double d = Math.hypot(obj.x - p.getX(), obj.z - p.getZ());
        Component dir = Component.translatable("darkcraft.dir." + rumbo(obj.x - p.getX(), obj.z - p.getZ()));
        String clave = d < 300 ? "darkcraft.llave.cerca" : "darkcraft.llave.rastro";
        p.displayClientMessage(Component.translatable(clave, dir, (int) Math.round(d)).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), true);
    }

    /**
     * Hacia donde tira la llave: con el puente tendido y ya arriba (en el puente o en la isla), a la reja de la puerta; en
     * la montana, al arco del mirador; si no, al pie del sendero de la montana (donde llega el camino de nubes).
     */
    static Vec3 objetivo(ServerPlayer p, Fortalezas.Sitio s, FortalezaPlan f) {
        boolean puente = com.darkcraft.fortaleza.FortalezaDatos.get(p.server).de(s).puente;
        boolean arriba = Math.abs(p.getY() - f.Y) < 6 && (Math.hypot(p.getX() - f.cx, p.getZ() - f.cz) < 80
                || Puente.enPuente(f, p.getX(), p.getY(), p.getZ()));
        if (puente && arriba) return new Vec3(f.cx + 0.5, f.Y, f.cz + TorreCupula.PZ + 1.5);
        if (f.montana.distancia(p.getX(), p.getZ()) < 6 && p.getY() > com.darkcraft.gen.Montana.NUBES - 3) {
            int[] a = Puente.arco(f);
            return new Vec3(a[0] + 0.5, a[1], a[2] + 0.5);
        }
        double[] pie = f.montana.pie();
        return new Vec3(pie[0] + 0.5, pie[2], pie[1] + 0.5);
    }

    /** "n", "ne", "e", "se", "s", "so", "o" o "no" (el norte es -z). */
    public static String rumbo(double dx, double dz) {
        double a = Math.toDegrees(Math.atan2(dx, -dz));
        String[] r = {"n", "ne", "e", "se", "s", "so", "o", "no"};
        return r[Math.floorMod((int) Math.round(a / 45.0), 8)];
    }

    /** Un rastro de luz desde el jugador hacia obj (solo lo ve el). */
    static void rastro(ServerPlayer p, Vec3 obj, int n, double largo) {
        ServerLevel lvl = p.serverLevel();
        Vec3 o = p.position().add(0, 1.1, 0);
        Vec3 h = new Vec3(obj.x - o.x, 0, obj.z - o.z);
        double dh = h.length();
        if (dh < 1e-3) return;
        Vec3 dir = h.scale(1 / dh);
        double sube = Math.max(-0.6, Math.min(0.6, (obj.y - o.y) / Math.max(dh, 1)));
        for (int i = 0; i < n; i++) {
            double k = 1.2 + i * largo / n;
            if (k > dh) break;
            double y = o.y + k * sube + Math.sin(i * 0.7 + p.tickCount * 0.2) * 0.12;
            double x = o.x + dir.x * k, z = o.z + dir.z * k;
            lvl.sendParticles(p, ParticleTypes.END_ROD, true, x, y, z, 1, 0.03, 0.03, 0.03, 0.0);
            if (i % 3 == 0) lvl.sendParticles(p, ParticleTypes.SOUL_FIRE_FLAME, true, x, y, z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    /** Cada segundo, con la llave en una mano: un rastro tenue hacia la fortaleza (o hacia la escalera si esta cerca). */
    public static void alSostener(ServerPlayer p) {
        ItemStack st = p.getMainHandItem().is(DarkCraftMod.LLAVE_HUESO.get()) ? p.getMainHandItem()
                : p.getOffhandItem().is(DarkCraftMod.LLAVE_HUESO.get()) ? p.getOffhandItem() : null;
        if (st == null || p.level().dimension() != Level.OVERWORLD) return;
        Fortalezas.Sitio s = destino(p, st);
        if (s == null) return;
        FortalezaPlan f = Fortalezas.plan(p.serverLevel().getSeed(), s);
        Vec3 obj = objetivo(p, s, f);
        rastro(p, obj, 16, 12);
        if (p.tickCount % 120 == 13)
            p.serverLevel().playSound(null, p.getX(), p.getY(), p.getZ(), DarkCraftMod.SFX_LLAVE_RASTRO.get(), SoundSource.PLAYERS, 0.35f, 1.15f);
    }

    @Override
    public boolean isFoil(ItemStack s) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.llave_hueso.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("item.darkcraft.llave_hueso.uso").withStyle(ChatFormatting.DARK_GRAY));
    }
}
