package com.gmail.andrewandy.ascendancy.lib.game.data.game;

import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.util.CommonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ChallengerDataImpl implements IChallengerData {

    private final byte[] iconData;
    private String name;
    private List<String> lore;

    public ChallengerDataImpl(String name, File icon, List<String> lore) throws IOException {
        this(name, CommonUtils.readFromStream(new FileInputStream(icon)), lore);
    }

    public ChallengerDataImpl(String name, byte[] iconData, List<String> lore) {
        this.name = name;
        this.iconData = iconData;
        this.lore = lore;
    }

    public String getName() {
        return name;
    }

    @Override public byte[] getIcon() {
        return iconData;
    }

    @Override public List<String> getLore() {
        return lore;
    }
}
