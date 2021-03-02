package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents Knavis' rune named "Chosen of the Earth"
 */
public class RuneChosenOTEarth extends AbstractRune {

    private final Map<UUID, Integer> stacks = new HashMap<>();
    private final Map<UUID, Long> tickHistory = new HashMap<>();

    @AssistedInject
    RuneChosenOTEarth(@Assisted final Challenger toBind, AscendancyServerPlugin plugin) {
        super(toBind);
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Override
    public void applyTo(final Player player) {
        tickHistory.put(player.getUniqueId(), 0L);
    }

    @Override
    public void clearFrom(final Player player) {
        tickHistory.remove(player.getUniqueId());
    }

    @Override
    public @NotNull String getName() {
        return "Chosen of the Earth";
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }

    /**
     * Handles when a player uses {@link AbilityLivingGift}
     */
    @Listener
    public void onGiftUse(final AbilityLivingGift.LivingGiftUseEvent event) {
        final Optional<Player> optionalPlayer = (event.getCause().get("Player", Player.class));
        assert optionalPlayer.isPresent();
        if (!tickHistory.containsKey(optionalPlayer.get().getUniqueId())) {
            return;
        }
        final Player playerObj = optionalPlayer.get();
        tickHistory.replace(playerObj.getUniqueId(), 0L);
        stacks.compute(playerObj.getUniqueId(), ((UUID player, Integer stack) -> {
            final int stackVal = stack == null ? 0 : stack; //Unboxing here may throw null pointer.
            double health = 3;
            for (int index = 1; index < stackVal; ) {
                health += index++;
            }
            Common.addHealth(playerObj, health
                    - 3); //Sets the total health to a value between 3 and 7 (adds on to LivingGift)

            return stackVal == 4 ?
                    stackVal :
                    stackVal
                            + 1; //If stack = 4, then max has been reached, therefore its 4 or stack + 1;
        }));
    }

    /**
     * Updates the stack history.
     */
    @Override
    public void tick() {
        tickHistory.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(6L, TimeUnit.SECONDS, stacks::remove));
    }

}
