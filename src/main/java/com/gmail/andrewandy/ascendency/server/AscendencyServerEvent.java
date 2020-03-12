package com.gmail.andrewandy.ascendency.server;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class AscendencyServerEvent extends Event {

    public boolean callEvent() {
        return MinecraftForge.EVENT_BUS.post(this);
    }

}
