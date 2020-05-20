package com.gmail.andrewandy.ascendency.serverplugin.util.game;

import java.util.UUID;

public interface Tickable {

    UUID getUniqueID();

    void tick();

}
