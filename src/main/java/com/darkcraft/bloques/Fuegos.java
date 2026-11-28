package com.darkcraft.bloques;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * El fuego propio (parte 8g): los siete colores (natural, de almas, de veneno, arcano, de sangre, sagrado y de escarcha) y
 * sus particulas (llama, chispa y humo; nunca las de Minecraft), con lo que las pone alrededor de un fuego. La luz no tiene
 * color: el color va en la llama, las chispas y el resplandor.
 */
public final class Fuegos {
    private Fuegos() {}

    public static final String[] COLORES = {"natural", "almas", "veneno", "arcano", "sangre", "sagrado", "escarcha"};

    public static final DeferredRegister<ParticleType<?>> PARTICULAS = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DarkCraftMod.MODID);
    public static final Map<String, RegistryObject<SimpleParticleType>> LLAMAS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<SimpleParticleType>> CHISPAS = new LinkedHashMap<>();
    public static final RegistryObject<SimpleParticleType> HUMO = PARTICULAS.register("humo", () -> new SimpleParticleType(false));

    static {
        for (String c : COLORES) {
            LLAMAS.put(c, PARTICULAS.register("llama_" + c, () -> new SimpleParticleType(false)));
            CHISPAS.put(c, PARTICULAS.register("chispa_" + c, () -> new SimpleParticleType(false)));
        }
    }

    public static void registrar(IEventBus bus) {
        PARTICULAS.register(bus);
    }

    /** El color del fuego de un bloque por su id (lo que va detras del ultimo "_", si es un color; si no, el natural). */
    public static String color(String id) {
        for (String c : COLORES) if (id.endsWith("_" + c)) return c;
        return "natural";
    }

    public static SimpleParticleType llama(String color) {
        RegistryObject<SimpleParticleType> r = LLAMAS.get(color);
        return (r == null ? LLAMAS.get("natural") : r).get();
    }

    public static SimpleParticleType chispa(String color) {
        RegistryObject<SimpleParticleType> r = CHISPAS.get(color);
        return (r == null ? CHISPAS.get("natural") : r).get();
    }

    /**
     * Lo que sale de un fuego en un tick (en el cliente): llamas que suben de una superficie de `ancho` x `ancho` centrada
     * en (x, z) a la altura y, alguna chispa, algo de humo y, de vez en cuando, el chisporroteo.
     */
    public static void avivar(Level nivel, double x, double y, double z, double ancho, String color, RandomSource r,
                              int llamas, boolean humo, boolean sonido) {
        for (int i = 0; i < llamas; i++)
            nivel.addParticle(llama(color), x + (r.nextDouble() - 0.5) * ancho, y + r.nextDouble() * 0.3,
                    z + (r.nextDouble() - 0.5) * ancho, (r.nextDouble() - 0.5) * 0.01, 0.02 + r.nextDouble() * 0.03,
                    (r.nextDouble() - 0.5) * 0.01);
        if (r.nextInt(3) == 0)
            nivel.addParticle(chispa(color), x + (r.nextDouble() - 0.5) * ancho * 0.6, y + 0.3 + r.nextDouble() * 0.3,
                    z + (r.nextDouble() - 0.5) * ancho * 0.6, (r.nextDouble() - 0.5) * 0.04, 0.06 + r.nextDouble() * 0.06,
                    (r.nextDouble() - 0.5) * 0.04);
        if (humo && r.nextInt(4) == 0)
            nivel.addParticle(HUMO.get(), x + (r.nextDouble() - 0.5) * ancho * 0.5, y + 0.8 + r.nextDouble() * 0.3,
                    z + (r.nextDouble() - 0.5) * ancho * 0.5, 0.0, 0.03 + r.nextDouble() * 0.02, 0.0);
        if (sonido && r.nextInt(70) == 0)
            nivel.playLocalSound(x, y, z, DarkCraftMod.SFX_SANTUARIO_FUEGO.get(), SoundSource.BLOCKS,
                    0.25F + r.nextFloat() * 0.15F, 0.9F + r.nextFloat() * 0.25F, false);
    }
}
