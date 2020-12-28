package com.gmail.andrewandy.ascendancy.serverplugin.util.game;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SpongeCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Subcommand;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("ascendencydebug|ascdebug")
@SuppressWarnings("unused")
public class DebugMatchCommand
        extends BaseCommand {

    private final SpongeCommandManager commandManager;
    @Inject
    private PlayerMatchManager matchManager;
    private DebugMatch current;

    public DebugMatchCommand() {
        Optional<PluginContainer> container =
                Sponge.getPluginManager().getPlugin("ascendencyserverplugin");
        assert container.isPresent();
        commandManager = new SpongeCommandManager(container.get());
        commandManager.getCommandCompletions().registerCompletion("custom_players", (context) -> {
            String[] values = context.getContextValue(String[].class);
            List<String> players =
                    Sponge.getServer().getOnlinePlayers().stream().map(Player::getName)
                            .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            if (values.length < 1) {
                return players;
            }
            String inMatch = context.getConfig("in_match", "");
            players.removeIf(name -> {
                final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(name);
                assert optionalPlayer.isPresent();
                final Player player = optionalPlayer.get();
                final boolean in = matchManager.isInMatch(player.getUniqueId());
                switch (inMatch.toLowerCase()) {
                    case "false":
                        return !in;
                    case "true":
                        return in;
                    default:
                        return false;
                }
            });
            for (int index = 1; index < values.length; index++) {
                final int i = index;
                players.removeIf(name -> name.equalsIgnoreCase(values[i]));
            }
            return players;
        });
    }

    private static Collection<Team> generateTeams() {
        return Arrays.asList(new Team("1", 1), new Team("2", 1));
    }

    @Subcommand("start")
    public void startMatch() {
    }

    @Subcommand("join")
    @CommandCompletion("@custom_players:in_match=false")
    public void joinDebug(final CommandSource sender, final String... players) {
        current = current == null ? new DebugMatch() : current;
        if (!(sender instanceof Player)) {
            Common.tell(sender, "&c(!) You must be a player to join a debug match!");
            return;
        }
        final UUID uuid = ((Player) sender).getUniqueId();
        if (current.containsPlayer(uuid)) {
            Common.tell(sender, "&bYou are already in this match!");
            return;
        }
        matchManager.addPlayerToMatch(uuid, current);
        Optional<Team> team = matchManager.getTeamOf(uuid);
        assert team.isPresent();
        Common.tell(sender, "&bYou have joined the debug match, team: " + team.get().getName());
    }
}
