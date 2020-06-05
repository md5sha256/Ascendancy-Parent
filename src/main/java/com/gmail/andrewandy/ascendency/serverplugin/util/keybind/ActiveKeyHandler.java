package com.gmail.andrewandy.ascendency.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendency.lib.keybind.AscendencyKey;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

public enum ActiveKeyHandler implements KeyBindHandler {

    INSTANCE;

    @Inject private static AscendencyServerPlugin plugin;
    private final Collection<UUID> pressed = new HashSet<>();
    private String scoreName;
    private Objective objective;

    ActiveKeyHandler() {
        loadSettings();
    }

    public void loadSettings() {
        ConfigurationNode node = plugin.getSettings();
        node = node.getNode("KeyBinding");
        if (node == null) {
            Common.log(Level.INFO, "&b[Key Binds] Unable to find settings for ActiveKeyHandler.");
            return;
        }
        final String objectiveName = node.getNode("ScoreboardObjective").getString();
        scoreName = node.getNode("ScoreboardScore").getString();
        this.objective = Objective.builder().name(objectiveName).criterion(Criteria.DUMMY).build();
        this.objective.getOrCreateScore(Text.of(scoreName));

    }

    @Override public AscendencyKey getTargetKey() {
        return AscendencyKey.ACTIVE_KEY;
    }

    @Override public boolean isKeyPressed(final Player player) {
        return pressed.contains(player.getUniqueId());
    }

    @Override public void onKeyPress(final Player player) {
        if (new ActiveKeyPressedEvent(player).callEvent()) {
            player.getScoreboard().addObjective(objective);
            objective.getOrCreateScore(Text.of(scoreName)).setScore(1);
            pressed.add(player.getUniqueId());
        }
        new ActiveKeyPressedEvent(player).callEvent();
    }

    @Override public void onKeyRelease(final Player player) {
        player.getScoreboard().addObjective(objective);
        objective.getOrCreateScore(Text.of(scoreName)).setScore(0);
        pressed.remove(player.getUniqueId());
        new ActiveKeyReleasedEvent(player).callEvent();
    }
}
