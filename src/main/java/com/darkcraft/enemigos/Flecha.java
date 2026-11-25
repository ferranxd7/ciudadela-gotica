package com.darkcraft.enemigos;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

/**
 * La flecha de los arqueros y ballesteros: una flecha del juego que pasa entre los suyos (desde lo alto de la puerta
 * disparan por encima de la pelea y, si no, se la clavaban a los de abajo o rebotaba en sus escudos).
 */
public class Flecha extends Arrow {
    public Flecha(Level nivel, LivingEntity tirador) {
        super(nivel, tirador);
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return !(e instanceof Enemigo) && super.canHitEntity(e);
    }
}
