package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.Rune;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.TickHandler;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.Tickable;
import net.minecraftforge.common.MinecraftForge;
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
    ASTSRICION(Astricion.getInstance()),
    SOLACE(Solace.getInstance()),
    VENGLIS(Vengelis.getInstance()),
    BREEZY(Breezy.getInstance()),
    BELLA(Bella.getInstance());

    public static final String LOAD = null; //Invoke to force classloader to load this class
    private final Challenger challengerObject;

    Challengers(final Challenger challengerObject) {
        this.challengerObject = challengerObject;
    }

    public static void initHandlers() {
        final EventManager manager = Sponge.getEventManager();
        final Object plugin = AscendencyServerPlugin.getInstance();
        for (final Challengers s1Challenger : values()) {
            final Challenger challenger = s1Challenger.challengerObject;
            if (challenger == null) {
                continue;
            }
            for (final Ability ability : challenger.getAbilities()) {
                manager.unregisterListeners(ability);
                manager.registerListeners(plugin, ability);
                MinecraftForge.EVENT_BUS.unregister(ability);
                MinecraftForge.EVENT_BUS.register(ability);
                if (ability instanceof Tickable) {
                    TickHandler.getInstance().removeTickable((Tickable) ability);
                    TickHandler.getInstance().submitTickable((Tickable) ability);
                }
            }
            for (final Rune rune : challenger.getRunes()) {
                manager.unregisterListeners(rune);
                manager.registerListeners(plugin, rune);
                MinecraftForge.EVENT_BUS.unregister(rune);
                MinecraftForge.EVENT_BUS.register(rune);
                TickHandler.getInstance().removeTickable(rune);
                TickHandler.getInstance().submitTickable(rune);
            }
        }
    }

    public static List<String> getLoreOf(final String name) {
        ConfigurationNode node = AscendencyServerPlugin.getInstance().getSettings();
        node = node.getNode("Champions");
        final List<? extends ConfigurationNode> nodes =
            node.getNode(name).getNode("lore").getChildrenList();
        return nodes.parallelStream().map(ConfigurationNode::getString)
            .collect(Collectors.toList());
    }

    public Challenger asChallenger() {
        return challengerObject;
    }
}
