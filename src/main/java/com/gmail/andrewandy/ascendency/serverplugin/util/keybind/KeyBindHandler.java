package com.gmail.andrewandy.ascendency.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendency.lib.keybind.AscendencyKey;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public interface KeyBindHandler {

    AscendencyKey getTargetKey();

    void onKeyPress(Player player);

    void onKeyRelease(Player player);

    @Listener(order = Order.LAST)
    default void onPlayerDisconnect(final ClientConnectionEvent.Disconnect event) {
        onKeyRelease(event.getTargetEntity());
    }

    boolean isKeyPressed(Player player);

}
