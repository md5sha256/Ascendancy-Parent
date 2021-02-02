package com.gmail.andrewandy.ascendancy.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendancy.lib.keybind.AscendancyKey;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ActiveKeyHandler implements KeyBindHandler {

    private final Collection<UUID> pressed = new HashSet<>();

    @Inject
    @Named("internal-config")
    private ConfigurationNode internalConfig;

    private String scoreName;
    private Objective objective;

    public ActiveKeyHandler() {
    }

    public void loadSettings() {
        ConfigurationNode node = internalConfig;
        node = node.node("KeyBinding");
        if (node.virtual() || node.empty()) {
            Common.log(Level.INFO, "&b[Key Binds] Unable to find settings for ActiveKeyHandler.");
            return;
        }
        final String objectiveName = node.node("ScoreboardObjective").getString();
        Objects.requireNonNull(objectiveName, "&b[Key Binds] ObjectiveName cannot be null!");
        scoreName = node.node("ScoreboardScore").getString();
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
