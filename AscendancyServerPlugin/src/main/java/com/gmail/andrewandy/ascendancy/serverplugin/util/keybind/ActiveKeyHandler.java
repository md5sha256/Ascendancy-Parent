package com.gmail.andrewandy.ascendancy.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.lib.keybind.AscendancyKey;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

public class ActiveKeyHandler implements KeyBindHandler {

    private final Collection<UUID> pressed = new HashSet<>();
    @Inject
    private AscendancyServerPlugin plugin;
    private String scoreName;
    private Objective objective;

    ActiveKeyHandler() {
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

    @Override
    @NotNull
    public AscendancyKey getTargetKey() {
        return AscendancyKey.ACTIVE_KEY;
    }

    @Override
    public void onKeyPress(@NotNull final Player player) {
        if (new ActiveKeyPressedEvent(player).callEvent()) {
            player.getScoreboard().addObjective(objective);
            objective.getOrCreateScore(Text.of(scoreName)).setScore(1);
            pressed.add(player.getUniqueId());
        }
        new ActiveKeyPressedEvent(player).callEvent();
    }

    @Override
    public void onKeyRelease(@NotNull final Player player) {
        player.getScoreboard().addObjective(objective);
        objective.getOrCreateScore(Text.of(scoreName)).setScore(0);
        pressed.remove(player.getUniqueId());
        new ActiveKeyReleasedEvent(player).callEvent();
    }

    @Override
    public boolean isKeyPressed(@NotNull final Player player) {
        return pressed.contains(player.getUniqueId());
    }
}
