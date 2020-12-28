package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.LocationMarkedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.LocationMark;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents the rune BlessingOfTeleportation.
 */
public class RuneBlessingOfTeleportation extends AbstractRune {

    private static final long ticks = Common.toTicks(8, TimeUnit.SECONDS);
    @NotNull
    private final Collection<UUID> active = new HashSet<>();
    private final AbilityShadowsRetreat boundAbility;

    @AssistedInject
    RuneBlessingOfTeleportation(@Assisted final Challenger challenger,
                                @Assisted final AbilityShadowsRetreat toBind) {
        super(challenger);
        this.boundAbility = toBind;
        toBind.setTickThresholdSupplier(
                //Basically checks if they have this ability active, if so increase duration of marks to 8 sec
                (UUID player, LocationMark mark) -> active.contains(player) ?
                        new long[]{ticks, ticks} :
                        AbilityShadowsRetreat.defaultTickThreshold);
    }

    @Listener(order = Order.EARLY)
    public void onMark(final LocationMarkedEvent event) {
        final LocationMark locationMark = event.getLocationMark();
        if (event.getMarkedSlot() == 2) {
            locationMark.setSecondaryMark(event.getPlayer().getLocation());
            locationMark.resetSecondaryTick();
        }
    }

    @Override
    public void applyTo(@NotNull final Player player) {
        clearFrom(player);
        active.add(player.getUniqueId());
    }

    @Override
    public void clearFrom(@NotNull final Player player) {
        active.remove(player.getUniqueId());
        final Optional<LocationMark> optional = boundAbility.getMarkFor(player.getUniqueId());
        optional.ifPresent(LocationMark::clear);
    }

    @Override
    @NotNull
    public String getName() {
        return "Blessing Of Teleportation";
    }

    @Override
    public void tick() {
        //This method does not actually need to tick since that is handled by the main ability
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    @NotNull
    public DataContainer toContainer() {
        return null;
    }
}
