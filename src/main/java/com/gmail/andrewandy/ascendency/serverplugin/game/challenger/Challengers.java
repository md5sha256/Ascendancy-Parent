package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.Rune;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reprsents the instances of all champions in "Season 1"
 */
public enum Challengers {

    KNAVIS(Knavis.getInstance()),
    ASTSRICTION(Astricion.getInstance()),
    SOLACE(Solace.getInstance()),
    VENGLIS(null),
    BREEZY(null);

    public static final String LOAD = null; //Invoke to force classloader to load this class
    private final int version = 0;
    private final Challenger challengerObject;

    Challengers(Challenger challengerObject) {
        this.challengerObject = challengerObject;
    }

    public static void initHandlers() {
        EventManager manager = Sponge.getEventManager();
        Object plugin = AscendencyServerPlugin.getInstance();
        for (Challengers s1Challenger : values()) {
            Challenger challenger = s1Challenger.challengerObject;
            if (challenger == null) {
                continue;
            }
            for (Ability ability : challenger.getAbilities()) {
                manager.unregisterListeners(ability);
                manager.registerListeners(plugin, ability);
            }
            for (Rune rune : challenger.getRunes()) {
                manager.unregisterListeners(rune);
                manager.registerListeners(plugin, rune);
            }
        }
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
