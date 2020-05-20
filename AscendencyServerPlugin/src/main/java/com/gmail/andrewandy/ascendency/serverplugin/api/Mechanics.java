package com.gmail.andrewandy.ascendency.serverplugin.api;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;

public class Mechanics {

    public enum DamageType {

        ATTACK_DAMAGE, ABILITY_POWER, TRUE;


        DamageType() {
        }

        double calculateDamage(Challenger challenger, double damage) {
            //return challenger.getDamageType() == this ? damage : damage * modifier;
            throw new UnsupportedOperationException("Unimplemented");
        }

    }

}
