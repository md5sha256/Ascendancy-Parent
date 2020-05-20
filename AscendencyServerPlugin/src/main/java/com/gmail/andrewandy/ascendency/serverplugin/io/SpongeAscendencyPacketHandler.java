package com.gmail.andrewandy.ascendency.serverplugin.io;

import com.gmail.andrewandy.ascendency.lib.AscendencyPacket;
import com.gmail.andrewandy.ascendency.lib.AscendencyPacketHandler;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
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
public class SpongeAscendencyPacketHandler extends AscendencyPacketHandler implements RawDataListener {


    private static final SpongeAscendencyPacketHandler instance = new SpongeAscendencyPacketHandler();
    private static final String CHANNEL_NAME = "ASCENDENCY_SPONGE";
    private ChannelBinding.RawDataChannel dataChannel;

    private SpongeAscendencyPacketHandler() {
    }

    public static SpongeAscendencyPacketHandler getInstance() {
        return instance;
    }

    public void initSponge() {
        dataChannel = Sponge.getChannelRegistrar().getOrCreateRaw(AscendencyServerPlugin.getInstance(), CHANNEL_NAME);
    }

    public void disable() {
        if (dataChannel != null) {
            Sponge.getChannelRegistrar().unbindChannel(dataChannel);
        }
    }

    public void sendMessageTo(Player player, AscendencyPacket packet) {
        dataChannel.sendTo(player, (channelBuf) -> channelBuf.writeBytes(packet.getFormattedData()));
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Platform.Type side) {
        if (dataChannel == null) {
            throw new IllegalStateException("Data channel has not be initialised!");
        }
        try {
            int len = data.readInteger();
            String rawClazz = new String(data.readBytes(data.readerIndex(), len));
            Class<?> clazz = Class.forName(rawClazz);
            if (!AscendencyPacket.class.isAssignableFrom(clazz)) {
                return;
            }
            Class<? extends AscendencyPacket> casted = clazz.asSubclass(AscendencyPacket.class);
            AscendencyPacket packet;
            packet = casted.getDeclaredConstructor().newInstance();
            packet.fromBytes(data.readByteArray());
            AscendencyPacket response = super.onMessage(packet);

            UUID uuid = response.getTargetPlayer();
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);
            optionalPlayer.ifPresent(
                    (Player player) -> dataChannel.sendTo(player, (ChannelBuf channel) -> channel.writeBytes(response.getFormattedData()))); //Send data to player.
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to interact with packet!", e);
        }
    }
}
