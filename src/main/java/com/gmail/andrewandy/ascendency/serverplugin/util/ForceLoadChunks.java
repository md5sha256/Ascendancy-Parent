package com.gmail.andrewandy.ascendency.serverplugin.util;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.logging.Level;

/**
 * A test implementation to keep chunks force loaded. Have no idea if this is a good
 * way of doing this or even a viable way.
 */
public class ForceLoadChunks {

    private static final ForceLoadChunks instance = new ForceLoadChunks();
    private static boolean init = false;
    private Map<World, ChunkTicketManager.LoadingTicket> ticketMap = new HashMap<>();
    private Collection<Location<? extends World>> locations = new HashSet<>();

    public static ForceLoadChunks getInstance() {
        if (!init && Sponge.getGame().isServerAvailable()) {
            Sponge.getEventManager().unregisterListeners(instance);
            Sponge.getEventManager()
                .registerListeners(AscendencyServerPlugin.getInstance(), instance);
            init = true;
        }
        return instance;
    }

    /**
     * Load the from the settings .yml.
     */
    public void loadSettings() {
        ConfigurationNode node = AscendencyServerPlugin.getInstance().getSettings();
        node = node.getNode("ChunkLoading");
        if (node == null) {
            return;
        }
        for (ConfigurationNode inner : node.getChildrenList()) {
            int x = inner.getNode("x").getInt();
            int z = inner.getNode("z").getInt();
            String world = inner.getNode("world").getString();
            Optional<World> worldObj = Sponge.getServer().getWorld(world);
            if (!worldObj.isPresent()) {
                Common.log(Level.WARNING, "&eUnable to find world " + world + "! Skipping...");
                continue;
            }
            addForceLoadLocation(new Location<>(worldObj.get(), x, 0, z));
        }
        updateTickets();
    }

    //Handles memory leakages.
    @Listener(order = Order.LAST) public void onWorldUnload(UnloadWorldEvent event) {
        if (ticketMap.containsKey(event.getTargetWorld())) {
            ticketMap.get(event.getTargetWorld()).release();
        }
        ticketMap.remove(event.getTargetWorld());
    }

    /**
     * @return Returns a cloned Collection of the currently force-loaded locations.
     */
    public Collection<Location<? extends World>> getLocations() {
        return new HashSet<>(locations);
    }

    /**
     * Add a location whose chunk should be force loaded.
     *
     * @param location The location to force-load.
     */
    public void addForceLoadLocation(Location<? extends World> location) {
        for (Location<? extends World> loc : locations) {
            if (loc.getExtent() == location
                .getExtent()) { //If they are the same chunks then return.
                return;
            }
        }
        if (!ticketMap.containsKey(location.getExtent())) {
            Optional<ChunkTicketManager.LoadingTicket> ticket;
            ticket = Sponge.getServer().getChunkTicketManager()
                .createTicket(AscendencyServerPlugin.getInstance(), location.getExtent());
            if (!ticket.isPresent()) {
                return;
            }
            ticketMap.put(location.getExtent(), ticket.get());
        }
        ticketMap.get(location.getExtent()).forceChunk(location.getChunkPosition());
        locations.add(location);
    }

    /**
     * Remove a location from being force loaded by THIS instance.
     *
     * @param location The location to stop force loading.
     */
    public void removeLocation(Location<? extends World> location) {
        Collection<Location<? extends World>> toRemove = new HashSet<>();
        for (Location<? extends World> loc : locations) {
            if (loc.getExtent() == location.getExtent()) {
                toRemove.add(loc);
                assert ticketMap.containsKey(loc.getExtent());
                ticketMap.get(loc.getExtent()).unforceChunk(loc.getChunkPosition());
            }
        }
        locations.removeAll(toRemove);
    }

    /**
     * Update the current {@link org.spongepowered.api.world.ChunkTicketManager.LoadingTicket}
     * which tells the server to force load chunks.
     */
    public void updateTickets() {
        //Update active;
        Collection<World> worlds = new HashSet<>();
        for (Location<? extends World> location : locations) {
            ChunkTicketManager.LoadingTicket ticket = ticketMap
                .computeIfAbsent(location.getExtent(),
                    (world) -> Sponge.getServer().getChunkTicketManager()
                        .createTicket(AscendencyServerPlugin.getInstance(), world).orElseThrow(
                            () -> new IllegalStateException("Unable to create chunk ticket!")));
            if (!ticket.getChunkList().contains(location.getChunkPosition())) {
                ticket.forceChunk(location.getChunkPosition());
            }
            worlds.add(location.getExtent());
        }
        //Remove all references to tickets.
        for (Map.Entry<World, ChunkTicketManager.LoadingTicket> entry : ticketMap.entrySet()) {
            if (!worlds.contains(entry.getKey())) {
                entry.getValue().release();
            }
        }
        ticketMap.keySet().removeAll(worlds);
    }

}
