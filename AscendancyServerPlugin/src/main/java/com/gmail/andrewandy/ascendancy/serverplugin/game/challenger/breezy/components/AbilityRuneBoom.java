package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class AbilityRuneBoom extends AbstractAbility {

    @AssistedInject
    AbilityRuneBoom(@Assisted final Challenger challenger) {
        super("RuneBoom", false, challenger);
    }


}
