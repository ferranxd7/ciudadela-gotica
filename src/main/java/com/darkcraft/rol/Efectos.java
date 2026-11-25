package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Los efectos de los frutos: cada uno da una resistencia un rato ({@link Resistencias} los suma a los de los
 * talismanes) y el de la pluma deja caer despacio y sin dano. Sus iconos son los de los frutos (tools/rol/objetos.py).
 */
public final class Efectos {
    private Efectos() {}

    public static final DeferredRegister<MobEffect> EFECTOS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, DarkCraftMod.MODID);

    /** Un efecto bueno sin nada que hacer cada tick (lo que da lo miran los demas). */
    static final class Efecto extends MobEffect {
        Efecto(int color) {
            super(MobEffectCategory.BENEFICIAL, color);
        }
    }

    /** Caer como una pluma: frena la caida y no hay dano al llegar al suelo. */
    static final class Pluma extends MobEffect {
        Pluma(int color) {
            super(MobEffectCategory.BENEFICIAL, color);
        }

        @Override
        public void applyEffectTick(LivingEntity e, int nivel) {
            Vec3 v = e.getDeltaMovement();
            if (!e.onGround() && v.y < -0.12) e.setDeltaMovement(v.x, -0.12, v.z);
            e.resetFallDistance();
        }

        @Override
        public boolean isDurationEffectTick(int dura, int nivel) {
            return true;
        }
    }

    public static final RegistryObject<MobEffect> BRASA = EFECTOS.register("fruto_brasa", () -> new Efecto(0xE8641A));
    public static final RegistryObject<MobEffect> ESCARCHA = EFECTOS.register("fruto_escarcha", () -> new Efecto(0x8FD0EE));
    public static final RegistryObject<MobEffect> TORMENTA = EFECTOS.register("fruto_tormenta", () -> new Efecto(0xF2D640));
    public static final RegistryObject<MobEffect> LUNAR = EFECTOS.register("fruto_lunar", () -> new Efecto(0x7A62D0));
    public static final RegistryObject<MobEffect> CENIZA = EFECTOS.register("fruto_ceniza", () -> new Efecto(0x808A66));
    public static final RegistryObject<MobEffect> HUESO = EFECTOS.register("fruto_hueso", () -> new Efecto(0xE2DCC4));
    public static final RegistryObject<MobEffect> SOMBRA = EFECTOS.register("fruto_sombra", () -> new Efecto(0x6A1020));
    public static final RegistryObject<MobEffect> PLUMA = EFECTOS.register("fruto_pluma", () -> new Pluma(0xBFE8EE));
    /** El milagro de la bendicion: las armas pegan un 15 % mas ({@code combate.Combate}). */
    public static final RegistryObject<MobEffect> BENDICION = EFECTOS.register("bendicion", () -> new Efecto(0xF6E3A0));
    /** El grito de guerra (habilidad de las armas): las armas pegan un 20 % mas ({@code combate.Combate}). */
    public static final RegistryObject<MobEffect> FURIA = EFECTOS.register("furia", () -> new Efecto(0xC83A1E));

    public static void registrar(IEventBus bus) {
        EFECTOS.register(bus);
    }
}
