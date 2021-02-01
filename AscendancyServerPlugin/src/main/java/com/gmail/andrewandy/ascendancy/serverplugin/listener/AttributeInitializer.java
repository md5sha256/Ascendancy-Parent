package com.gmail.andrewandy.ascendancy.serverplugin.listener;

import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class AttributeInitializer {

    @Listener(order = Order.FIRST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        event.getTargetEntity().getOrCreate(AttributeData.class);
    }

}
