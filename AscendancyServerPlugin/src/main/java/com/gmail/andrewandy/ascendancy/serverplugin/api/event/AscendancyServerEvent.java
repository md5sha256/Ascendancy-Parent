package com.gmail.andrewandy.ascendancy.serverplugin.api.event;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.impl.AbstractEvent;

public abstract class AscendancyServerEvent extends AbstractEvent {

    public boolean callEvent() {
        return Sponge.getEventManager().post(this);
    }

}
