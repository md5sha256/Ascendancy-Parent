package com.gmail.andrewandy.ascendency.common.io.packet;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Represents the main packet handler for Ascendency.
 * Packet implementations should register their handling methods on class loading.
 */
public class AscendencyPacketHandler implements IMessageHandler<AscendencyPacket, AscendencyPacket> {

    //TODO Add a packet tracker class!

    private static final AscendencyPacketHandler instance = new AscendencyPacketHandler();
    private static final String CHANNEL_NAME = "Ascendency_Data_Channel";
    private Map<Class<?>, Function<? extends AscendencyPacket, ? extends AscendencyPacket>> handlerMap = new ConcurrentHashMap<>();

    public static AscendencyPacketHandler getInstance() {
        return instance;
    }

    public <T extends AscendencyPacket, R extends AscendencyPacket> void registerHandler(Class<T> clazz, Function<T, R> handleFunction) {
        removeHandler(clazz);
        handlerMap.put(clazz, handleFunction);
    }

    public void removeHandler(Class<? extends AscendencyPacket> clazz) {
        handlerMap.remove(Objects.requireNonNull(clazz));
    }

    @SuppressWarnings("unchecked") //Checks done in registerHandler
    public <T extends AscendencyPacket> Optional<Function<T, ? extends AscendencyPacket>> getHandlerOf(Class<T> clazz) {
        if (!handlerMap.containsKey(clazz)) {
            return Optional.empty();
        }
        return Optional.of((Function<T, ? extends AscendencyPacket>) handlerMap.get(clazz));
    }

    @Override
    public AscendencyPacket onMessage(AscendencyPacket message, MessageContext ctx) {
        return onMessage(message);
    }

    /**
     * Handle a message purely based on the packet without context.
     *
     * @param message The message to handle.
     * @return Returns the response packet.
     */
    @SuppressWarnings("rawtypes, unchecked") //Raw type is ok since checks are done in #registerHandler
    public AscendencyPacket onMessage(AscendencyPacket message) {
        Class<? extends AscendencyPacket> clazz = message.getClass();
        if (getHandlerOf(clazz).isPresent()) {
            Function<? extends AscendencyPacket, ? extends AscendencyPacket> function = getHandlerOf(clazz).get();
            return (AscendencyPacket) ((Function) function).apply(message);
        }
        return null;
    }
}
