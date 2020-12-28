package com.gmail.andrewandy.ascendancy.serverplugin.io;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;
import com.gmail.andrewandy.ascendancy.lib.AscendancyPacketHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

import java.util.Optional;
import java.util.UUID;

/**
 * Server Side ONLY! DO not shade into the client.
 * This handles the raw client packets on the sponge server.
 */
@Singleton
public class SpongeAscendancyPacketHandler extends AscendancyPacketHandler
        implements RawDataListener {


    private static final String CHANNEL_NAME = "ASCENDENCY_SPONGE";
    @Inject
    private AscendancyServerPlugin plugin;
    private ChannelBinding.RawDataChannel dataChannel;

    SpongeAscendancyPacketHandler() {
    }

    public void initSponge() {
        dataChannel = Sponge.getChannelRegistrar().getOrCreateRaw(plugin, CHANNEL_NAME);
    }

    public void disable() {
        if (dataChannel != null) {
            Sponge.getChannelRegistrar().unbindChannel(dataChannel);
        }
    }

    public void sendMessageTo(final Player player, final AscendancyPacket packet) {
        dataChannel
                .sendTo(player, (channelBuf) -> channelBuf.writeBytes(packet.getFormattedData()));
    }

    @Override
    public void handlePayload(final ChannelBuf data, final RemoteConnection connection,
                              final Platform.Type side) {
        if (dataChannel == null) {
            throw new IllegalStateException("Data channel has not be initialised!");
        }
        try {
            final int len = data.readInteger();
            final String rawClazz = new String(data.readBytes(data.readerIndex(), len));
            final Class<?> clazz = Class.forName(rawClazz);
            if (!AscendancyPacket.class.isAssignableFrom(clazz)) {
                return;
            }
            final Class<? extends AscendancyPacket> casted =
                    clazz.asSubclass(AscendancyPacket.class);
            final AscendancyPacket packet;
            packet = casted.getDeclaredConstructor().newInstance();
            packet.fromBytes(data.readByteArray());
            final AscendancyPacket response = super.onMessage(packet);

            final UUID uuid = response.getTargetPlayer();
            final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);
            optionalPlayer.ifPresent((Player player) -> dataChannel.sendTo(player,
                    (ChannelBuf channel) -> channel
                            .writeBytes(response
                                    .getFormattedData()))); //Send data to player.
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to interact with packet!", e);
        }
    }
}
