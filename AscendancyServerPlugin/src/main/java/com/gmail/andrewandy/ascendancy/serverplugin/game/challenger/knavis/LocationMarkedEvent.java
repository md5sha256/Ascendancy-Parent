package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis;

import com.gmail.andrewandy.ascendancy.serverplugin.api.event.AscendancyServerEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components.AbilityShadowsRetreat;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.LocationMark;
import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class LocationMarkedEvent extends AscendancyServerEvent {

    @NotNull
    private final Player player;
    @NotNull
    private final LocationMark locationMark;
    @NotNull
    private final Knavis knavis;
    private int markSlot;


    LocationMarkedEvent(
            @Assisted final Player marker, @Assisted final int markSlot,
            @Assisted final AbilityShadowsRetreat retreat, final Knavis knavis
    ) {
        this.player = marker;
        setMarkedSlot(markSlot);
        this.locationMark = retreat.getMarkFor(marker.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("Location mark not found for player!"));
        this.knavis = knavis;
    }

    public @NotNull LocationMark getLocationMark() {
        return locationMark;
    }

    public int getMarkedSlot() {
        return markSlot;
    }

    public void setMarkedSlot(final int markSlot) {
        if (markSlot < 0 || markSlot > 9) {
            throw new IllegalArgumentException("Invalid Mark Slot!");
        }
        this.markSlot = markSlot;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    @NotNull
    public Cause getCause() {
        return Cause.builder().named("Knavis", knavis).build();
    }

}
