package com.darkcraft.client.bloques;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.bloques.Fuegos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

/**
 * Las particulas del fuego propio (parte 8g), en el cliente: la llama (sube, se encoge y se apaga, a plena luz), la chispa
 * (sube deprisa y parpadea) y el humo (sube despacio, crece y se deshace); y la gota de sangre (parte 8k). Los dibujos son los de
 * {@code textures/particle} (de {@code tools/bloques/fuego.py}).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ParticulasFuego {
    private ParticulasFuego() {}

    private static final int PLENA_LUZ = 0xF000F0;

    @SubscribeEvent
    public static void alRegistrar(RegisterParticleProvidersEvent e) {
        for (RegistryObject<SimpleParticleType> r : Fuegos.LLAMAS.values())
            e.registerSpriteSet(r.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new Llama(l, x, y, z, dx, dy, dz, s));
        for (RegistryObject<SimpleParticleType> r : Fuegos.CHISPAS.values())
            e.registerSpriteSet(r.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new Chispa(l, x, y, z, dx, dy, dz, s));
        e.registerSpriteSet(Fuegos.HUMO.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new Humo(l, x, y, z, dx, dy, dz, s));
        e.registerSpriteSet(Fuegos.GOTA_SANGRE.get(), s -> (t, l, x, y, z, dx, dy, dz) -> new Gota(l, x, y, z, s));
    }

    /** La gota de sangre (parte 8k): cuelga un momento y engorda, cae y se deshace al tocar el suelo. */
    static final class Gota extends TextureSheetParticle {
        private int colgada;

        Gota(ClientLevel l, double x, double y, double z, SpriteSet s) {
            super(l, x, y, z);
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;
            this.lifetime = 90;
            this.colgada = 12 + random.nextInt(24);
            this.quadSize = 0.035F;
            this.gravity = 0.0F;
            this.hasPhysics = true;
            pickSprite(s);
        }

        @Override
        public void tick() {
            xo = x;
            yo = y;
            zo = z;
            if (age++ >= lifetime) {
                remove();
                return;
            }
            if (colgada > 0) {
                colgada--;
                quadSize = Math.min(0.065F, quadSize + 0.0012F);
                return;
            }
            yd -= 0.035;
            move(xd, yd, zd);
            yd *= 0.98;
            if (onGround) remove();
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }
    }

    /** Lo comun: la velocidad que se le da (el juego la cambia al azar en su constructor). */
    private abstract static class Base extends TextureSheetParticle {
        protected final SpriteSet dibujos;

        Base(ClientLevel l, double x, double y, double z, double dx, double dy, double dz, SpriteSet s) {
            super(l, x, y, z, dx, dy, dz);
            this.dibujos = s;
            this.xd = dx;
            this.yd = dy;
            this.zd = dz;
            this.hasPhysics = false;
            this.gravity = 0.0F;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    static final class Llama extends Base {
        Llama(ClientLevel l, double x, double y, double z, double dx, double dy, double dz, SpriteSet s) {
            super(l, x, y, z, dx, dy, dz, s);
            this.lifetime = 10 + random.nextInt(12);
            this.quadSize = 0.10F + random.nextFloat() * 0.09F;
            this.friction = 0.94F;
            setSpriteFromAge(s);
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                setSpriteFromAge(dibujos);
                alpha = 1.0F - 0.75F * age / (float) lifetime;
                quadSize *= 0.965F;
                xd += (random.nextDouble() - 0.5) * 0.004;
                zd += (random.nextDouble() - 0.5) * 0.004;
            }
        }

        @Override
        protected int getLightColor(float parcial) {
            return PLENA_LUZ;
        }
    }

    static final class Chispa extends Base {
        Chispa(ClientLevel l, double x, double y, double z, double dx, double dy, double dz, SpriteSet s) {
            super(l, x, y, z, dx, dy, dz, s);
            this.lifetime = 18 + random.nextInt(20);
            this.quadSize = 0.035F + random.nextFloat() * 0.03F;
            this.friction = 0.97F;
            this.gravity = -0.002F;
            pickSprite(s);
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                alpha = (age / 2) % 3 == 0 ? 0.55F : 1.0F;
                xd += (random.nextDouble() - 0.5) * 0.01;
                zd += (random.nextDouble() - 0.5) * 0.01;
            }
        }

        @Override
        protected int getLightColor(float parcial) {
            return PLENA_LUZ;
        }
    }

    static final class Humo extends Base {
        Humo(ClientLevel l, double x, double y, double z, double dx, double dy, double dz, SpriteSet s) {
            super(l, x, y, z, dx, dy, dz, s);
            this.lifetime = 40 + random.nextInt(30);
            this.quadSize = 0.12F + random.nextFloat() * 0.1F;
            this.friction = 0.98F;
            this.alpha = 0.55F;
            pickSprite(s);
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                alpha = 0.55F * (1.0F - age / (float) lifetime);
                quadSize *= 1.012F;
                xd += (random.nextDouble() - 0.5) * 0.002;
                zd += (random.nextDouble() - 0.5) * 0.002;
            }
        }
    }
}
