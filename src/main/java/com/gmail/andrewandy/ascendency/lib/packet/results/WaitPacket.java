package com.gmail.andrewandy.ascendency.lib.packet.results;

import java.util.concurrent.TimeUnit;

public class WaitPacket extends AscendencyResultPacket {

    private long duration;
    private TimeUnit timeUnit;

    public WaitPacket() {
        super("WAIT", Result.WAIT);
    }

    public WaitPacket setDuration(long duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
