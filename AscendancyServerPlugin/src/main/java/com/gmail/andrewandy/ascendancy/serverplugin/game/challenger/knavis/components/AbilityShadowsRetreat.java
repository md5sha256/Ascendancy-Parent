package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractTickableAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.KnavisComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.LocationMarkedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.MarkTeleportationEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.LocationMark;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GamePlayer;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class AbilityShadowsRetreat extends AbstractTickableAbility {

    public static final long[] defaultTickThreshold =
            new long[]{Common.toTicks(6, TimeUnit.SECONDS), Common.toTicks(6, TimeUnit.SECONDS)};

    private final Map<UUID, LocationMark> dataMap = new HashMap<>();
    private final Map<UUID, Integer> castCounter = new HashMap<>();
    private final PlayerMatchManager matchManager;
    private final KnavisComponentFactory factory;
    private BiFunction<UUID, LocationMark, long[]> tickThresholdFunction;
    private BiConsumer<Player, Integer> onMark;

    @AssistedInject
    AbilityShadowsRetreat(@Assisted final Challenger challenger,
                          final PlayerMatchManager matchManager,
                          final KnavisComponentFactory componentFactory) {
        super("Shadow's Retreat", true, challenger);
        this.matchManager = matchManager;
        this.factory = componentFactory;
    }

    public void setTickThresholdSupplier(
            final BiFunction<UUID, LocationMark, long[]> tickThresholdFunction) {
        this.tickThresholdFunction = tickThresholdFunction;
    }


    public Optional<LocationMark> getMarkFor(final UUID player) {
        if (dataMap.containsKey(player)) {
            return Optional.of(dataMap.get(player));
        }
        return Optional.empty();
    }

    @Override
    public void tick() {
        dataMap.forEach((UUID player, LocationMark mark) -> {
            final long[] ticks = tickThresholdFunction == null ?
                    defaultTickThreshold :
                    tickThresholdFunction.apply(player, mark);
            //ticks is basically a long (tick threshold) for primary and secondary
            assert ticks.length == 2;
            if (mark.getPrimaryTick() >= ticks[0]) {
                mark.setPrimaryMark(null);
                mark.resetPrimaryTick();
            } else {
                mark.incrementPrimary();
            }
            if (mark.getSecondaryTick() >= ticks[1]) {
                mark.setPrimaryMark(null);
                mark.resetSecondaryTick();
            } else {
                mark.incrementSecondary();
            }
        });
    }

    @NotNull
    private LocationMark castAbilityAs(@NotNull final Player player) {
        final LocationMark mark = dataMap.compute(player.getUniqueId(),
                (key, value) -> value == null ?
                        new LocationMark() :
                        value);
        castCounter.compute(player.getUniqueId(), (uuid, castCount) -> {
            if (castCount == null) {
                castCount = 0;
            }
            if (castCount == 0) {
                mark.setPrimaryMark(player.getLocation());
                mark.resetPrimaryTick();
            } else {
                final MarkTeleportationEvent event =
                        factory.createMarkTeleportationEvent(player, mark.getPrimaryMark());
                if (event.callEvent()) {
                    player.setLocationSafely(event.getTargetLocation());
                }
            }
            return ++castCount;
        });
        return mark;
    }


    @Listener(order = Order.LAST)
    public void onHotbarChange(final ChangeInventoryEvent.Held event) {
        final Cause cause = event.getCause();
        final Optional<Player> optionalPlayer = cause.allOf(UUID.class).parallelStream()
                .map((uniqueID) -> Sponge.getServer().getPlayer(uniqueID)).filter(Optional::isPresent)
                .map(Optional::get).findAny();
        if (!optionalPlayer.isPresent()) {
            return;
        }
        final Player player = optionalPlayer.get();
        if (!dataMap.containsKey(player.getUniqueId())) {
            return;
        }
        final Inventory inventory = player.getInventory();
        final Optional<ItemStack> clicked = player.getItemInHand(HandTypes.MAIN_HAND);
        clicked.ifPresent((stack) -> {
            final Optional<SlotIndex> index =
                    inventory.getProperty(SlotIndex.class, SlotIndex.of(stack));
            index.ifPresent((SlotIndex slotIndex) -> {
                assert slotIndex.getValue() != null;
                if (onMark != null) {
                    onMark.accept(player, slotIndex.getValue());
                }
                if (slotIndex.getValue() != 2 || slotIndex.getValue() != 1) {
                    return;
                }
                castAbilityAs(player);
            });

        });
    }

    @Listener(order = Order.LAST)
    public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
        final Player player = event.getPlayer();
        final Optional<ManagedMatch> managedMatch = matchManager.getMatchOf(player.getUniqueId());
        if (!managedMatch.isPresent()) {
            return;
        }
        final ManagedMatch match = managedMatch.get();
        final Optional<? extends GamePlayer> optional = match.getGamePlayerOf(player.getUniqueId());
        optional.ifPresent(gamePlayer -> {
            final Challenger challenger = gamePlayer.getChallenger();
            if (challenger != bound) {
                return;
            }
            final LocationMark mark = castAbilityAs(event.getPlayer());
            //Now safe to call LME because the mark is now guaranteed to be created.
            final LocationMarkedEvent lme =
                    factory.createLocationMarkedEvent(player, 1, this);
            if (lme.callEvent()) {
                //TODO give the player the mark itemstack.
            }
        });
    }
}
