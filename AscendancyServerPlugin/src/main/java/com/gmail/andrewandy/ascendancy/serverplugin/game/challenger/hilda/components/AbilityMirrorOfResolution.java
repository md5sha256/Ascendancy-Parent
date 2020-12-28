package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.components;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractTickableAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public class AbilityMirrorOfResolution extends AbstractTickableAbility {


    @AssistedInject
    AbilityMirrorOfResolution(@Assisted final Challenger challenger) {
        super("Mirror Of Resolution", true, challenger);
    }

    @Override
    public void tick() {

    }

    public void onCast(@NotNull final Player player) {
        if (!isRegistered(player.getUniqueId())) {
            throw new IllegalArgumentException("Player isn't registered!");
        }
        // X = roll, Y = yaw, Z = pitch
        Vector3d rotation = player.getHeadRotation();
    }
}
