package com.gmail.andrewandy.ascendency.serverplugin.util;

import com.gmail.andrewandy.ascendency.lib.AscendencyPacket;
import com.gmail.andrewandy.ascendency.lib.game.AscendencyChampions;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataMarkerPacket;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataPacket;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.io.SpongeAscendencyPacketHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


//TODO add handler to remove from syncguard when the SUCCESS packet is received.
public enum GameRegistry {

    INSTANCE;

    private Map<UUID, Task> syncGuard = new HashMap<>();
    private Collection<UUID> notUpdated = ConcurrentHashMap.newKeySet();

    private Map<AscendencyChampions, Challenger> championRegistry = new HashMap<>();

    public void mapChampion(AscendencyChampions champion, Challenger object, boolean invalidate) {
        championRegistry.remove(champion);
        championRegistry.put(champion, object);
        if (invalidate) {
            notUpdated.addAll(
                Sponge.getServer().getOnlinePlayers().stream().map(Player::getUniqueId)
                    .collect(Collectors.toSet()));
        }
    }

    @Listener public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        notUpdated.add(player.getUniqueId());
        queueResync(player.getUniqueId());
    }

    @Listener public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        syncGuard.remove(player.getUniqueId());
        notUpdated.remove(player.getUniqueId());
    }


    public boolean isUpdated(UUID uuid) {
        return !notUpdated.contains(uuid);
    }

    public void setUpdate(UUID uuid, boolean updated) {
        if (updated) {
            notUpdated.remove(uuid);
        } else {
            notUpdated.add(uuid);
        }
    }

    /**
     * Updates the known not {@link #isUpdated(UUID)} players.
     *
     * @param async Whether to update the players async.
     * @return Returns a {@link Task} if async is true, <code>null</code> if not.
     */
    public Task updateNotUpdated(boolean async) {
        if (async) {
            return queueResync(notUpdated);
        } else {
            forceResync(notUpdated);
            return null;
        }
    }

    /**
     * Internal method to resync a player.
     *
     * @param uuid The UUID of the player.
     */
    private void resync(UUID uuid, boolean async) {
        if (syncGuard.containsKey(uuid)) {
            return;
        }
        syncGuard.put(uuid, null);
        ChallengerDataMarkerPacket packet = new ChallengerDataMarkerPacket(championRegistry.size());
        Queue<AscendencyPacket> packets = new ArrayDeque<>(championRegistry.size() + 1);
        championRegistry.values()
            .forEach((challenger -> packets.add(new ChallengerDataPacket(challenger.toData()))));
        packets.add(packet);
        Optional<Player> optional;
        if (async) {
            Future<Optional<Player>> future =
                Common.getSyncExecutor().submit(() -> Sponge.getServer().getPlayer(uuid));
            while (!future.isDone())
                ;
            try {
                optional = future.get();
            } catch (ExecutionException | InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            optional = Sponge.getServer().getPlayer(uuid);
        }
        optional.ifPresent(player -> {
            SpongeAscendencyPacketHandler handler = SpongeAscendencyPacketHandler.getInstance();
            while (packets.size() != 0) {
                handler.sendMessageTo(player, packets.remove());
            }
        });
    }

    /**
     * Resynchronise the client's champion data.
     * This method will execute the resync on the next
     * tick as per the sponge executor.
     *
     * @param players The UUID of the player.
     */
    public void forceResync(UUID... players) {
        Sponge.getScheduler().createTaskBuilder().async().execute(() -> {
            for (UUID uuid : players) {
                Common.getSyncExecutor().submit(() -> resync(uuid, false));
            }
        });
    }

    /**
     * @see #forceResync(UUID...)
     */
    public void forceResync(Collection<UUID> players) {
        forceResync((UUID[]) players.toArray());
    }

    /**
     * Force a resync for all online players.
     */
    public void forceResyncAll() {
        forceResync((UUID[]) Sponge.getServer().getOnlinePlayers().stream().map(Player::getUniqueId)
            .toArray());
    }

    /**
     * Queues a resync to be done asynchrnously.
     * Note that player's will still be "Up to date" so long as
     * the execution has not started.
     *
     * @param players The UUIDs of the players to resynchronise.
     * @return Returns an {@link Task} which represents the state of execution.
     */
    public Task queueResync(UUID... players) {
        return Sponge.getScheduler().createTaskBuilder().execute(task -> {
            for (UUID uuid : players) {
                resync(uuid, true);
            }
        }).async().submit(AscendencyServerPlugin.getInstance());
    }

    /**
     * @see #queueResync(UUID...)
     */
    public Task queueResync(Collection<UUID> players) {
        return queueResync((UUID[]) players.toArray());
    }

    /**
     * Queues a resync of all online players.
     */
    public Task queueResyncAll() {
        return queueResync(
            (UUID[]) Sponge.getServer().getOnlinePlayers().stream().map(Player::getUniqueId)
                .toArray());
    }

}
