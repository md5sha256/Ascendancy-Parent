package com.gmail.andrewandy.ascendency.common.io;

import com.gmail.andrewandy.ascendencyserverplugin.util.Common;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Represents a message broker / manager for all connected players.
 * This is a very rudimentary design and is not very efficient, designed
 * for < 10 concurrent players.
 * Acts just like a thread, call {@link #start()} to start.
 */
public class MessageBroker {

    private final int sponge_server_port;
    private boolean enabled = false;
    private long timeout = TimeUnit.SECONDS.toMillis(10);
    private long refreshTime = TimeUnit.SECONDS.toMillis(30);
    private Future<?> future;
    private Map<UUID, Integer> portMap = new ConcurrentHashMap<>();
    private Map<UUID, Socket> socketMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);

    public MessageBroker() {
        assert Sponge.getServer().getBoundAddress().isPresent();
        sponge_server_port = Sponge.getServer().getBoundAddress().get().getPort();
        start();
    }

    public void start() {
        enabled = true;
        future = executorService.scheduleWithFixedDelay(this::run, 0, refreshTime, TimeUnit.MILLISECONDS);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void stop() {
        future.cancel(true);
        //Close the sockets.
        for (Socket socket : socketMap.values()) {
            try {
                socket.close();
            } catch (IOException ex) {
                Common.log(Level.SEVERE, "Error occurred when trying to stop the message broker!");
                ex.printStackTrace();
            }
        }
        //Clear the maps
        socketMap.clear();
        portMap.clear();
        enabled = false;
    }

    public Optional<Socket> getOrOpenSocket(UUID player) {
        return Optional.ofNullable(socketMap.computeIfAbsent(Objects.requireNonNull(player), (UUID unused) -> {
            try {
                return openSocket(player);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public Future<Optional<Socket>> getOrOpenSocketAsync(UUID player) {
        return executorService.submit(() -> getOrOpenSocket(player));
    }

    /**
     * Open a socket for a player.
     *
     * @param player The UUID of the player.
     * @return Returns a newly opened socket to the player's machine.
     * @throws IOException           Thrown if an IO exception occurs when opening a socket.
     * @throws IllegalStateException Thrown if the player UUID is invalid - such as if
     *                               no such player UUID is online.
     */
    private Socket openSocket(UUID player) throws IOException, IllegalStateException {
        int port = assignPortFor(player);
        InetAddress address;
        Future<InetAddress> future = Common.getSyncExecutor().submit(() -> {
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(player);
            return optionalPlayer.map(value -> value.getConnection().getAddress().getAddress()).orElse(null);
        });
        while ((!future.isDone())) ;
        try {
            address = future.get();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        ServerSocket serverSocket = new ServerSocket(port);
        //Accept the incoming connection.
        Socket target = serverSocket.accept();
        target.setKeepAlive(true);
        target.setSoTimeout(Math.toIntExact(timeout));
        if (!target.getInetAddress().equals(address)) {
            throw new IllegalStateException("Player address != connected address!");
        }
        return target;
    }

    private Future<Socket> openSocketAsync(UUID player) {
        return executorService.submit(() -> openSocket(player));
    }

    public int assignPortFor(UUID player) {
        return portMap.computeIfAbsent(Objects.requireNonNull(player), (UUID unused) -> generateRandomPort());
    }

    public void unregister(UUID player) throws IOException {
        portMap.remove(player);
        if (socketMap.containsKey(player)) {
            Socket socket = socketMap.get(player);
            if (socket != null) {
                socket.close();
            }
        }
        socketMap.remove(player);
    }

    private int generateRandomPort() {
        int randomPort = 0;
        //Get a port
        while (portMap.containsValue(randomPort) || randomPort == sponge_server_port) {
            if (portMap.size() >= 25500) {
                throw new IllegalStateException("Not enough ports to allocate!");
            }
            randomPort = ThreadLocalRandom.current().nextInt(0, 25500);
        }
        return randomPort;
    }

    /**
     * Task to update the player sockets.
     */
    public void run() {
        if (!enabled) {
            if (future != null) {
                future.cancel(true);
                return;
            }
        }
        Collection<UUID> onlinePlayers;
        try {
            //Get online players from main thread
            Future<Collection<Player>> futurePlayers = Common.getSyncExecutor().submit(() -> Sponge.getServer().getOnlinePlayers());
            while (!futurePlayers.isDone()) ;
            onlinePlayers = futurePlayers.get().stream().map(Player::getUniqueId).collect(Collectors.toSet());
            //Close all inactive sockets.
            for (Map.Entry<UUID, Socket> entry : socketMap.entrySet()) {
                if (!onlinePlayers.contains(entry.getKey())) {
                    try {
                        entry.getValue().close();
                    } catch (IOException ex) {
                        //Print error if any socket error occurs.
                        Common.log(Level.SEVERE, "Socket error occurred!");
                        ex.printStackTrace();
                    }
                }
            }
            //Leave only players which are online in the socket map and the port map.
            socketMap.keySet().retainAll(onlinePlayers);
            portMap.keySet().retainAll(onlinePlayers);
        } catch (Exception ex) {
            Common.log(Level.SEVERE, "Execution Error Occurred!");
            ex.printStackTrace();
        }
        //Update and try reopening closed sockets for each player.
        for (Map.Entry<UUID, Socket> entry : socketMap.entrySet()) {
            Runnable runnable = () -> {
                UUID uuid = entry.getKey();
                Socket socket = entry.getValue();
                if (socket.isClosed()) {
                    try {
                        socket = openSocket(uuid);

                    } catch (IOException ex) {
                        if (ex instanceof SocketTimeoutException) {
                            return; //Ignore if its because of a timeout.
                        }
                        Common.log(Level.SEVERE, "Socket error occurred!");
                        ex.printStackTrace();
                    }
                }
                entry.setValue(socket);
            };
            executorService.submit(runnable); //Submit a task to update the socket for EACH player.
        }
    }
}
