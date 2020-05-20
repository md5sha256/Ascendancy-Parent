package com.gmail.andrewandy.ascendency.client.io;

import com.gmail.andrewandy.ascendency.lib.AscendencyPacket;
import com.gmail.andrewandy.ascendency.lib.AscendencyPacketHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ClientPacketHandler extends AscendencyPacketHandler {

    public static final String CHANNEL_NAME = "ASCENDENCY_DEFAULT";
    private static final ClientPacketHandler instance = new ClientPacketHandler();
    private SimpleNetworkWrapper wrapper;

    private ClientPacketHandler() {
    }

    public static ClientPacketHandler getInstance() {
        return instance;
    }

    public void initForge() {
        if (wrapper == null) {
            wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
            wrapper.registerMessage(this, AscendencyPacket.class, 10, Side.CLIENT);
        }
    }

    public void sendMessage(AscendencyPacket message) {
        wrapper.sendToServer(message);
    }

    /**
     * Handles any incoming message.
     *
     * @param message The incoming message.
     */
    @Override
    public AscendencyPacket onMessage(AscendencyPacket message) {
        if (wrapper == null) {
            initForge();
        }
        AscendencyPacket response = super.onMessage(message);
        if (response != null) {
            wrapper.sendToServer(response);
        }
        return response;
    }


    /**
     * Forge handler for the message, basically
     * this method will just call {@link #onMessage(AscendencyPacket)}
     */
    @Override
    public AscendencyPacket onMessage(AscendencyPacket message, MessageContext ctx) {
        assert ctx.side.isServer();
        return onMessage(message);
    }
}
