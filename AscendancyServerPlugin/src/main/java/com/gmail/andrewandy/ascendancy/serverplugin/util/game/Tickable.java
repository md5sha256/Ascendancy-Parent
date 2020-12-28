package com.gmail.andrewandy.ascendancy.serverplugin.util.game;

import java.util.UUID;

public interface Tickable {

    UUID getUniqueID();

    void tick();

}
