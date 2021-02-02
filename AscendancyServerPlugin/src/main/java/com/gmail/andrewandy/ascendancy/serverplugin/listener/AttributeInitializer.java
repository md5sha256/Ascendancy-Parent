package com.gmail.andrewandy.ascendancy.serverplugin.listener;

import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.google.inject.Inject;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Optional;

public class AttributeInitializer {

    @Inject
    private PlayerMatchManager matchManager;

    @Listener(order = Order.FIRST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        final Optional<ManagedMatch> optional = matchManager.getMatchOf(event.getTargetEntity().getUniqueId());
        if (!optional.isPresent()) {
            // Clear existing data if not in game
            event.getTargetEntity().remove(AttributeData.class);
        }
        // Initialize empty attribute data
        event.getTargetEntity().getOrCreate(AttributeData.class);
    }

}
