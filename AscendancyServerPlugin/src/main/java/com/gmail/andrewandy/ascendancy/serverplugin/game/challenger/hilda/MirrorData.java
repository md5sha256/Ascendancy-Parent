package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.util.game.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class MirrorData implements Iterable<Mirror>, Tickable, Cloneable {

    private final UUID uuid = UUID.randomUUID();

    private final UUID player;
    private final Map<Mirror, Long> mirrorDurationMap = new HashMap<>(3);
    private int charges = 0;
    private int usedCharges = 0;

    public MirrorData(@NotNull UUID player) {
        this.player = player;
    }

    @Override
    public void tick() {
        final Predicate<Map.Entry<Mirror, Long>> cooldownPredicate = ChallengerUtils.mapTickPredicate(9, TimeUnit.SECONDS, null);
        mirrorDurationMap.entrySet().removeIf(cooldownPredicate);
    }

    public @NotNull UUID getPlayer() {
        return player;
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    public int getCharges() {
        return charges;
    }

    public int getUsedCharges() {
        return usedCharges;
    }

    public int getAllocatedCharges() {
        return charges + usedCharges;
    }

    public void allocateCharge() {
        charges++;
    }

    public void useCharge() {
        if (charges > 0) {
            charges--;
        }
        usedCharges++;
    }

    public void clearUsedCharge() {
        if (usedCharges > 0) {
            usedCharges--;
        }
    }

    public void resetCharges() {
        charges = 0;
        usedCharges = 0;
    }

    public void addMirror(@NotNull Mirror mirror) {
        mirrorDurationMap.put(mirror, 0L);
    }

    public void removeMirror(@NotNull Mirror mirror) {
        mirrorDurationMap.remove(mirror);
    }

    @NotNull
    @Override
    public Iterator<Mirror> iterator() {
        final Iterator<Mirror> iterator = mirrorDurationMap.keySet().iterator();
        return new Iterator<Mirror>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Mirror next() {
                return iterator.next();
            }
        };
    }

    public @NotNull Set<@NotNull Mirror> getMirrors() {
        return new HashSet<>(mirrorDurationMap.keySet());
    }

    public boolean containsMirror(@NotNull Mirror mirror) {
        return mirrorDurationMap.containsKey(mirror);
    }

    @Override
    public MirrorData clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        final MirrorData data = new MirrorData(player);
        data.mirrorDurationMap.putAll(this.mirrorDurationMap);
        return data;
    }
}
