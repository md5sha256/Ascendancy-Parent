package com.gmail.andrewandy.ascendancy.client.io;

import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;
import com.gmail.andrewandy.ascendancy.lib.AscendancyPacketHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ClientPacketHandler extends AscendancyPacketHandler {

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
            wrapper.registerMessage(this, AscendancyPacket.class, 10, Side.CLIENT);
        }
    }

    public void sendMessage(AscendancyPacket message) {
        wrapper.sendToServer(message);
    }

    /**
     * Handles any incoming message.
     *
     * @param message The incoming message.
     */
    @Override
    public AscendancyPacket onMessage(AscendancyPacket message) {
        if (wrapper == null) {
            initForge();
        }
        AscendancyPacket response = super.onMessage(message);
        if (response != null) {
            wrapper.sendToServer(response);
        }
        return response;
    }


    /**
     * Forge handler for the message, basically
     * this method will just call {@link #onMessage(AscendancyPacket)}
     */
    @Override
    public AscendancyPacket onMessage(AscendancyPacket message, MessageContext ctx) {
        assert ctx.side.isServer();
        return onMessage(message);
    }

}
