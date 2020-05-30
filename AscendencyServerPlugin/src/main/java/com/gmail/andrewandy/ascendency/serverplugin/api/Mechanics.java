package com.gmail.andrewandy.ascendency.serverplugin.api;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import org.spongepowered.api.entity.living.player.Player;

public class Mechanics {

    public enum DamageType {

        AD() {
            @Override
            public void applyDamage(Player player, double baseDamage) {

            }
        },
        AP() {
            @Override
            public void applyDamage(Player player, double baseDamage) {
                player.get()
            }
        },
        TRUE;

        DamageType() {
        }

        public String getToKey() {
            return name();
        }

        public abstract void applyDamage(final Player player, final double baseDamage);

        double calculateDamage(Challenger challenger, double damage) {
            //return challenger.getDamageType() == this ? damage : damage * modifier;
            throw new UnsupportedOperationException("Unimplemented");
        }

    }

}
