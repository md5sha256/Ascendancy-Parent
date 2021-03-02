package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components.AbilityLivingGift;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components.AbilityShadowsRetreat;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components.RuneBlessingOfTeleportation;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components.RuneChosenOTEarth;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components.RuneHeartOfTheDryad;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface KnavisComponentFactory {

    AbilityLivingGift createLivingGiftFor(@NotNull Challenger challenger);

    AbilityShadowsRetreat createShadowsRetreatFor(@NotNull Challenger challenger);

    RuneBlessingOfTeleportation createBlessingOfTeleportationFor(@NotNull Challenger challenger);

    RuneChosenOTEarth createChosenOfTEarthFor(@NotNull Challenger challenger);

    RuneHeartOfTheDryad createHeartOfTheDryadFor(@NotNull Challenger challenger);

    LocationMarkedEvent createLocationMarkedEvent(
            @NotNull final Player marker, final int markSlot,
            @NotNull final AbilityShadowsRetreat retreat
    );

    MarkTeleportationEvent createMarkTeleportationEvent(@NotNull final Player player, @NotNull final Location<World> toTeleport);


}
