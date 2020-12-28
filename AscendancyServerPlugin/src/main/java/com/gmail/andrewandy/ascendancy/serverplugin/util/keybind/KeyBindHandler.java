package com.gmail.andrewandy.ascendancy.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendancy.lib.keybind.AscendancyKey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public interface KeyBindHandler {

    @NotNull AscendancyKey getTargetKey();

    void onKeyPress(@NotNull Player player);

    void onKeyRelease(@NotNull Player player);

    @Listener(order = Order.LAST)
    default void onPlayerDisconnect(final ClientConnectionEvent.Disconnect event) {
        onKeyRelease(event.getTargetEntity());
    }

    boolean isKeyPressed(@NotNull Player player);

}
