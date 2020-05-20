package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reprsents the instances of all champions in "Season 1"
 */
public enum Season1Challengers {

    KNAVIS(Knavis.getInstance()),
    ASTSRICTION(Astricion.getInstance()),
    SOLACE(null),
    VENGLIS(null),
    BREEZY(null);

    public static final String LOAD = null; //Invoke to force classloader to load this class
    private final int version = 0;
    private final Challenger challengerObject;

    Season1Challengers(Challenger challengerObject) {
        this.challengerObject = challengerObject;
    }

    public static List<String> getLoreOf(String name) {
        ConfigurationNode node = AscendencyServerPlugin.getInstance().getSettings();
        node = node.getNode("Champions");
        List<? extends ConfigurationNode> nodes = node.getNode(name).getNode("lore").getChildrenList();
        return nodes.parallelStream().map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public Challenger asChallenger() {
        return challengerObject;
    }
}
