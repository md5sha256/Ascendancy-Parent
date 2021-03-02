package com.gmail.andrewandy.ascendancy.lib.results;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WaitPacket extends AscendancyResultPacket {

    private long duration;
    private TimeUnit timeUnit;

    public WaitPacket() {
        super("WAIT", Result.WAIT);
    }

    public WaitPacket(WaitPacket other) {
        this.duration = other.duration;
        this.timeUnit = other.timeUnit;
    }

    public WaitPacket setDuration(long duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
        return this;
    }

    public WaitPacket forPlayer(UUID player) {
        WaitPacket packet = new WaitPacket(this);
        packet.setTargetPlayer(player);
        return packet;
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

}
