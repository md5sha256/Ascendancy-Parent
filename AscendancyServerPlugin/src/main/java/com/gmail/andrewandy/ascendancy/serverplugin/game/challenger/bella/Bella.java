package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.AbilityCircletOfTheAccused;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.RuneCoupDEclat;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.game.data.game.ChallengerDataImpl;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Represents the "Bella" Character in ascendancy.
 */
public class Bella extends AbstractChallenger {

    @Inject
    private static AscendancyServerPlugin plugin;

    @Inject
    Bella(@NotNull final BellaComponentFactory componentFactory) {
        super("Bella", challenger -> abilities(challenger, componentFactory),
                challenger -> runes(challenger, componentFactory),
                ChallengerModule.getLoreOf("Bella"));
    }

    private static Ability[] abilities(@NotNull final Challenger challenger,
                                       @NotNull final BellaComponentFactory componentFactory) {
        return new Ability[]{componentFactory.createCircletAccusedFor(challenger),
                componentFactory.createReleasedRebellionFor(challenger)};
    }

    private static PlayerSpecificRune[] runes(@NotNull final Challenger challenger, @NotNull final BellaComponentFactory componentFactory) {
        AbilityCircletOfTheAccused ability = null;
        for (final Ability a : challenger.getAbilities()) {
            if (a instanceof AbilityCircletOfTheAccused) {
                ability = (AbilityCircletOfTheAccused) a;
                break;
            }
        }
        if (ability == null) {
            throw new IllegalArgumentException(
                    "Challenger does not have AbilityCircletOfTheAccused!");
        }
        final RuneCoupDEclat coupDEclat = componentFactory.createCoupDEclatFor(ability);
        return new PlayerSpecificRune[]{coupDEclat,
                componentFactory.createDivineCrownFor(ability, coupDEclat),
                componentFactory.createExpandingAgonyFor(ability, coupDEclat)};
    }


    /**
     * Creates and places a nether-brick ring.
     *
     * @param centre The centre of the circle.
     * @param radius The radius.
     * @return Returns a Collection of Blocks which were placed.
     */
    public static Collection<Location<World>> generateCircleBlocks(final Location<World> centre,
                                                                   final int radius) {
        final Collection<Location<World>> rawCircle =
                MathUtils.createCircleWithCentre(centre, radius);
        final Cause cause = Cause.builder().named("Bella", plugin).build();
        rawCircle.forEach((location -> location.setBlockType(BlockTypes.AIR, cause)));
        return rawCircle;
    }

    @Override
    @NotNull
    public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to icon"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
