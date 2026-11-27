package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Sangrado;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Los escudos (parte 7ak, respuesta 109 del plan: "escudos pequeno, mediano y grande"), un {@link ShieldItem} del juego (se
 * cubre igual: clic derecho, la postura y lo que para) con su tamano ({@link Tamano}): lo que para de cada golpe, lo que cuesta
 * de estamina cubrirse ({@code rol.Rol.guardia}), lo que pesa y si se puede <b>parar</b> con el (la rodela: el golpe de cuerpo a
 * cuerpo que llega en el instante de alzarla se desvia entero y el que lo dio se queda aturdido). Su modelo 3D lo dibuja
 * {@code client.escudos.EscudoRenderer} (no se rompen).
 */
public class Escudo extends ShieldItem {
    public enum Tamano {
        /** La rodela: para el 75 %, cuesta un 15 % mas, pesa 2; se para en los primeros {@code parada} ticks; se anda mas rapido cubierto. */
        PEQUENO("rodela", 0.75f, 1.15f, 2.0f, 9),
        /** El escudo de caballero: lo para todo. */
        MEDIANO("escudo_caballero", 1.0f, 1.0f, 4.5f, 0),
        /** El paves: lo para todo y cuesta casi la mitad, pero pesa 9. */
        GRANDE("paves", 1.0f, 0.55f, 9.0f, 0);

        public final String id;
        /** Lo que para de cada golpe (0..1), lo que cuesta cubrirse (por lo que cuesta con el escudo del juego) y lo que pesa. */
        public final float para, estamina, peso;
        /** Los ticks (desde que ya se esta cubierto) en que un golpe se para. */
        public final int parada;

        Tamano(String id, float para, float estamina, float peso, int parada) {
            this.id = id;
            this.para = para;
            this.estamina = estamina;
            this.peso = peso;
            this.parada = parada;
        }
    }

    /** Para las pruebas: las paradas que ha habido. */
    public static int PARADAS;
    /** Lo que tarda el juego en dar por cubierto a quien alza un escudo (ticks). */
    public static final int ALZAR = 5;

    public final Tamano tamano;

    public Escudo(Tamano tamano, Properties p) {
        super(p);
        this.tamano = tamano;
    }

    /** El tamano del escudo que se lleva alzado (o null si no es uno de estos). */
    public static Tamano alzado(LivingEntity le) {
        return le.isUsingItem() && le.getUseItem().getItem() instanceof Escudo e ? e.tamano : null;
    }

    /** Sin colores de estandarte: siempre su nombre. */
    @Override
    public String getDescriptionId(ItemStack st) {
        return getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft." + tamano.id + ".desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("darkcraft.escudo.para", Math.round(tamano.para * 100), Math.round(tamano.estamina * 100),
                String.format(Locale.ROOT, "%.1f", tamano.peso)).withStyle(ChatFormatting.GOLD));
        if (tamano.parada > 0) tip.add(Component.translatable("darkcraft.escudo.parada").withStyle(ChatFormatting.YELLOW));
        if (tamano == Tamano.GRANDE) tip.add(Component.translatable("darkcraft.escudo.paves").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // ojo: Forge llama a esto desde el constructor de Item (aun sin tamano): la extension lo mira en el objeto al dibujar
        consumer.accept(com.darkcraft.client.escudos.EscudoCliente.EXTENSION);
    }

    /**
     * Si ese golpe se para: con la rodela, un golpe de cuerpo a cuerpo (quien pega es quien da) que llega en los primeros
     * {@code parada} ticks de estar cubierto. Al que pego le deja aturdido (a los jefes, no) y suena la parada.
     */
    public static boolean parar(LivingEntity quien, DamageSource s) {
        Tamano t = alzado(quien);
        if (t == null || t.parada <= 0 || quien.level().isClientSide) return false;
        int cubierto = quien.getTicksUsingItem() - ALZAR;
        if (cubierto < 0 || cubierto > t.parada) return false;
        if (!(s.getEntity() instanceof LivingEntity a) || s.getDirectEntity() != a || a == quien) return false;
        PARADAS++;
        if (!Sangrado.jefe(a)) {
            if (a instanceof Enemigo en) en.aturdir();
            else {
                a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 9, false, false, false));
                a.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 4, false, false, false));
                if (a instanceof Mob m) m.getNavigation().stop();
            }
        }
        if (quien.level() instanceof ServerLevel sl) {
            Vec3 c = quien.getEyePosition().add(quien.getLookAngle().scale(0.7)).add(0, -0.4, 0);
            sl.sendParticles(ParticleTypes.CRIT, c.x, c.y, c.z, 16, 0.2, 0.2, 0.2, 0.4);
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, c.x, c.y, c.z, 10, 0.2, 0.2, 0.2, 0.3);
            sl.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_HABILIDAD_PARADA.get(), SoundSource.PLAYERS, 1.0f, 1.05f + quien.getRandom().nextFloat() * 0.1f);
        }
        if (quien instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "parada");
        return true;
    }

    /** Lo que pesa un escudo (el del juego, lo de siempre: 4). */
    public static float peso(ItemStack st) {
        return st.getItem() instanceof Escudo e ? e.tamano.peso : 4f;
    }

    /** Para que se vea en las pruebas quien se esta cubriendo. */
    public static String resumen(Player p) {
        Tamano t = alzado(p);
        return t == null ? (p.isBlocking() ? "cubierto (escudo del juego)" : "sin cubrirse")
                : t.id + (p.isBlocking() ? " cubierto " + (p.getTicksUsingItem() - ALZAR) + " ticks" : " alzandose");
    }
}
