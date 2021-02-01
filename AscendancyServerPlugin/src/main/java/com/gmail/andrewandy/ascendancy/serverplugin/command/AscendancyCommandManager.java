package com.gmail.andrewandy.ascendancy.serverplugin.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SpongeCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import com.flowpowered.math.vector.Vector3d;
import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.common.primitives.Doubles;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.world.extent.EntityUniverse;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class AscendancyCommandManager extends BaseCommand {

    @Inject
    private AscendancyServerPlugin plugin;

    @Inject
    AscendancyCommandManager() {
        final SpongeCommandManager commandManager = new SpongeCommandManager(
                Sponge.getPluginManager().getPlugin("ascendencyserverplugin")
                        .orElseThrow(() -> new IllegalStateException("Unable to find plugin container!")));
        commandManager.registerCommand(this);
    }

    @CommandAlias("readinventory|readinv")
    public void showInventory(final Player player,
                              @co.aikar.commands.annotation.Optional final String targetPlayer) {
        final Player other;
        if (targetPlayer != null) {
            final Optional<Player> optional = Sponge.getServer().getPlayer(targetPlayer);
            if (!optional.isPresent()) {
                Common.tell(player, "&cUnknown player.");
                return;
            }
            other = optional.get();
        } else {
            final Set<EntityUniverse.EntityHit> entities =
                    player.getWorld().getIntersectingEntities(player, 10);
            //Get closest player in sender's vision.
            final Optional<Player> optionalPlayer =
                    entities.stream().filter(entityHit -> entityHit.getEntity() instanceof Player)
                            .min(Comparator.comparingDouble(EntityUniverse.EntityHit::getDistance))
                            .map(entityHit -> (Player) entityHit.getEntity());

            if (!optionalPlayer.isPresent()) {
                Common.tell(player, "&cYou must be looking at a player within 10 blocks!");
                return;
            }
            other = optionalPlayer.get();
        }
        final Inventory inventory = other.getInventory();
        player.openInventory(inventory, Cause.source(plugin).build());
    }

}
