package com.gmail.andrewandy.ascendancy.lib;

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
public abstract class AscendancyPacketHandler implements IMessageHandler<AscendancyPacket, AscendancyPacket> {

    private Map<Class<?>, Function<? extends AscendancyPacket, ? extends AscendancyPacket>> handlerMap = new ConcurrentHashMap<>();

    public <T extends AscendancyPacket, R extends AscendancyPacket> void registerHandler(Class<T> clazz, Function<T, R> handleFunction) {
        removeHandler(clazz);
        handlerMap.put(clazz, handleFunction);
    }

    public void removeHandler(Class<? extends AscendancyPacket> clazz) {
        handlerMap.remove(Objects.requireNonNull(clazz));
    }

    @SuppressWarnings("unchecked") //Checks done in registerHandler
    public <T extends AscendancyPacket> Optional<Function<T, ? extends AscendancyPacket>> getHandlerOf(Class<T> clazz) {
        if (!handlerMap.containsKey(clazz)) {
            return Optional.empty();
        }
        return Optional.of((Function<T, ? extends AscendancyPacket>) handlerMap.get(clazz));
    }

    @Override
    public AscendancyPacket onMessage(AscendancyPacket message, MessageContext ctx) {
        return onMessage(message);
    }

    /**
     * Handle a message purely based on the packet without context.
     *
     * @param message The message to handle.
     * @return Returns the response packet.
     */
    @SuppressWarnings("rawtypes, unchecked") //Raw type is ok since checks are done in #registerHandler
    public AscendancyPacket onMessage(AscendancyPacket message) {
        Class<? extends AscendancyPacket> clazz = message.getClass();
        if (getHandlerOf(clazz).isPresent()) {
            Function<? extends AscendancyPacket, ? extends AscendancyPacket> function = getHandlerOf(clazz).get();
            return (AscendancyPacket) ((Function) function).apply(message);
        }
        return null;
    }
}
