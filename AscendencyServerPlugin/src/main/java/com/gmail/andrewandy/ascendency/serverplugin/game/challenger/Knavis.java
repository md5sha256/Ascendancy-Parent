package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerEvent;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractTickableAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.Rune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.LocationMark;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GameEngine;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GamePlayer;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Represents the Knavis challenger. All abiliities and runes for Knavis can be found here.
 * //TODO test in game!
 */
public class Knavis extends AbstractChallenger implements Challenger {

    private static final Knavis instance = new Knavis();
    @Inject private static AscendencyServerPlugin plugin;

    private Knavis() {
        super("Knavis", new Ability[] {ShadowsRetreat.instance, LivingGift.instance}, //Abilities
            new PlayerSpecificRune[] {ChosenOTEarth.instance, HeartOfTheDryad.instance,
                BlessingOfTeleportation.instance}, //Runes
            Challengers.getLoreOf("Knavis")); //Lore
    }

    public static Knavis getInstance() {
        return instance;
    }

    @Override public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to file on server"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException("Unable to create ChampionData", ex);
        }
    }

    public static class LivingGift extends AbstractAbility {

        private static final LivingGift instance = new LivingGift();
        private final Map<UUID, Integer> hitHistory = new HashMap<>();

        private LivingGift() {
            super("LivingGift", false);
        }

        public static LivingGift getInstance() {
            return instance;
        }

        @Override public String getName() {
            return "LivingGift";
        }

        @Listener public void onDamage(final DamageEntityEvent event) {
            final Optional<Player> optionalPlayer =
                event.getCause().get(DamageEntityEvent.CREATOR, UUID.class)
                    .flatMap(Sponge.getServer()::getPlayer);
            if (!optionalPlayer.isPresent()) {
                return;
            }
            final Player player = optionalPlayer.get();
            if (!hitHistory.containsKey(player.getUniqueId())) {
                return;
            }
            int hits = hitHistory.get(player.getUniqueId());
            if (hits++ == 3) {
                final HealthData data = player.getHealthData();
                data.set(data.health()
                    .transform((Double val) -> val + 3.0)); //Add 3 health or 1.5 hearts.
                player.offer(data); //Update the player object.
                hits = 0;
                new LivingGiftUseEvent(player).callEvent();
            }
            hitHistory.replace(player.getUniqueId(), hits); //Update hit count
        }

        private static class LivingGiftUseEvent extends AscendencyServerEvent {

            private final Cause cause;

            public LivingGiftUseEvent(final Player player) {
                this.cause = Cause.builder().named("Player", player).build();
            }

            @Override @NotNull public Cause getCause() {
                return cause;
            }
        }
    }


    public static class ShadowsRetreat extends AbstractTickableAbility {

        public static final Long[] defaultTickThreshold =
            new Long[] {Common.toTicks(6, TimeUnit.SECONDS), Common.toTicks(6, TimeUnit.SECONDS)};
        private static final ShadowsRetreat instance = new ShadowsRetreat();
        private final Map<UUID, LocationMark> dataMap = new HashMap<>();
        private final Map<UUID, Integer> castCounter = new HashMap<>();
        private BiFunction<UUID, LocationMark, Long[]> tickThresholdFunction;
        private BiConsumer<Player, Integer> onMark;

        private ShadowsRetreat() {
            super("Shadow's Retreat", true);
        }

        public static ShadowsRetreat getInstance() {
            return instance;
        }

        public void setTickThresholdSupplier(
            final BiFunction<UUID, LocationMark, Long[]> tickThresholdFunction) {
            this.tickThresholdFunction = tickThresholdFunction;
        }


        public Optional<LocationMark> getMarkFor(final UUID player) {
            if (dataMap.containsKey(player)) {
                return Optional.of(dataMap.get(player));
            }
            return Optional.empty();
        }

        @Override public void tick() {
            dataMap.forEach((UUID player, LocationMark mark) -> {
                final Long[] ticks = tickThresholdFunction == null ?
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

        private @NotNull LocationMark castAbilityAs(@NotNull final Player player) {
            final LocationMark mark = dataMap.compute(player.getUniqueId(),
                (key, value) -> value == null ? new LocationMark() : value);
            castCounter.compute(player.getUniqueId(), (uuid, castCount) -> {
                if (castCount == null) {
                    castCount = 0;
                }
                if (castCount == 0) {
                    mark.setPrimaryMark(player.getLocation());
                    mark.resetPrimaryTick();
                } else {
                    final MarkTeleportationEvent event =
                        new MarkTeleportationEvent(player, mark.getPrimaryMark());
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
                .map((uniqueID) -> Sponge.getServer().getPlayer(uniqueID))
                .filter(Optional::isPresent).map(Optional::get).findAny();
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
            final Optional<ManagedMatch> managedMatch =
                SimplePlayerMatchManager.INSTANCE.getMatchOf(player.getUniqueId());
            if (!managedMatch.isPresent()) {
                return;
            }
            final ManagedMatch match = managedMatch.get();
            final Optional<? extends GamePlayer> optional =
                match.getGamePlayerOf(player.getUniqueId());
            optional.ifPresent(gamePlayer -> {
                final Challenger challenger = gamePlayer.getChallenger();
                if (challenger != Knavis.getInstance()) {
                    return;
                }
                final LocationMark mark = castAbilityAs(event.getPlayer());
                //Now safe to call LME because the mark is now guaranteed to be created.
                LocationMarkedEvent lme = new LocationMarkedEvent(player, 1);
                if (lme.callEvent()) {
                    //TODO give the player the mark itemstack.
                }
            });
        }

        public static class LocationMarkedEvent extends AscendencyServerEvent {

            @NotNull private final Player player;
            @NotNull private final LocationMark locationMark;
            private int markSlot;

            public LocationMarkedEvent(@NotNull final Player marker, final int markSlot) {
                this.player = marker;
                setMarkedSlot(markSlot);
                this.locationMark = ShadowsRetreat.getInstance().getMarkFor(marker.getUniqueId())
                    .orElseThrow(
                        () -> new IllegalStateException("Location mark not found for player!"));
            }

            public @NotNull LocationMark getLocationMark() {
                return locationMark;
            }

            public int getMarkedSlot() {
                return markSlot;
            }

            public void setMarkedSlot(final int markSlot) {
                if (markSlot < 0 || markSlot > 9) {
                    throw new IllegalArgumentException("Invalid Mark Slot!");
                }
                this.markSlot = markSlot;
            }

            @NotNull public Player getPlayer() {
                return player;
            }

            @Override @NotNull public Cause getCause() {
                return Cause.builder().named("Knavis", Knavis.getInstance()).build();
            }
        }


        private static class MarkTeleportationEvent extends AscendencyServerEvent
            implements Cancellable {

            @NotNull private final Player player;
            @NotNull private final Cause cause;
            private boolean cancel;
            @NotNull private Location<World> location;

            public MarkTeleportationEvent(@NotNull final Player player,
                @NotNull final Location<World> toTeleport) {
                this.player = player;
                this.location = toTeleport;
                this.cause = Cause.builder().named("Knavis", Knavis.getInstance()).build();
            }

            public Location<World> getTargetLocation() {
                return location;
            }

            public void setTargetLocation(final Location<World> location) {
                this.location = location;
            }

            public Player getPlayer() {
                return player;
            }

            @Override @NotNull public Cause getCause() {
                return cause;
            }

            @Override public boolean isCancelled() {
                return cancel;
            }

            @Override public void setCancelled(final boolean cancel) {
                this.cancel = cancel;
            }
        }
    }


    /**
     * Represents the rune BlessingOfTeleportation.
     */
    public static class BlessingOfTeleportation extends AbstractRune {

        @NotNull private static final BlessingOfTeleportation instance =
            new BlessingOfTeleportation();
        private static final long ticks = Common.toTicks(8, TimeUnit.SECONDS);
        @NotNull private final Collection<UUID> active = new HashSet<>();

        private BlessingOfTeleportation() {
            ShadowsRetreat.instance.setTickThresholdSupplier(
                //Basically checks if they have this ability active, if so increase duration of marks to 8 sec
                (UUID player, LocationMark mark) -> active.contains(player) ?
                    new Long[] {ticks, ticks} :
                    ShadowsRetreat.defaultTickThreshold);
        }

        @NotNull public static BlessingOfTeleportation getInstance() {
            return instance;
        }

        @Listener(order = Order.EARLY)
        public void onMark(final ShadowsRetreat.LocationMarkedEvent event) {
            final LocationMark locationMark = event.getLocationMark();
            if (event.getMarkedSlot() == 2) {
                locationMark.setSecondaryMark(event.getPlayer().getLocation());
                locationMark.resetSecondaryTick();
            }
        }

        @Override public void applyTo(@NotNull final Player player) {
            clearFrom(player);
            active.add(player.getUniqueId());
        }

        @Override public void clearFrom(@NotNull final Player player) {
            active.remove(player.getUniqueId());
            final Optional<LocationMark> optional =
                ShadowsRetreat.getInstance().getMarkFor(player.getUniqueId());
            optional.ifPresent(LocationMark::clear);
        }

        @Override @NotNull public String getName() {
            return "Blessing Of Teleportation";
        }

        @Override public void tick() {
            //This method does not actually need to tick since that is handled by the main ability
        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override @NotNull public DataContainer toContainer() {
            return null;
        }
    }


    /**
     * Represents the rune HeartOfTheDryad
     */
    public static class HeartOfTheDryad extends AbstractRune {

        private static final HeartOfTheDryad instance = new HeartOfTheDryad();
        private final Map<UUID, PotionEffect[]> registered = new HashMap<>();
        private final Map<UUID, Long> currentActive = new HashMap<>();
        private final Map<UUID, Long> cooldownMap = new HashMap<>();

        private HeartOfTheDryad() {
        }

        public static HeartOfTheDryad getInstance() {
            return instance;
        }

        @Override public void applyTo(final Player player) {
            clearFrom(player);
            currentActive.put(player.getUniqueId(), 0L);
            final Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
            if (!optional.isPresent()) {
                throw new IllegalStateException(
                    "Potion effect data could not be gathered for " + player.getUniqueId()
                        .toString());
            }

            final PotionEffectData data = optional.get();
            final PotionEffect[] effects = new PotionEffect[] {PotionEffect.builder()
                //Level 2 movement speed
                .potionType(PotionEffectTypes.SPEED).duration(4).amplifier(2).build(),
                PotionEffect.builder()
                    //20% Attack speed
                    .potionType(PotionEffectTypes.HASTE).duration(4).amplifier(2).build()};
            //Root / Entanglement
            for (final PotionEffect effect : effects) {
                data.addElement(effect);
            }
            player.offer(data);
            registered.put(player.getUniqueId(), effects);
            final Optional<ManagedMatch> optionalMatch =
                SimplePlayerMatchManager.INSTANCE.getMatchOf(player.getUniqueId());
            optionalMatch.ifPresent(managedMatch -> {
                final GameEngine engine = managedMatch.getGameEngine();
                final Optional<? extends GamePlayer> optionalPlayer =
                    engine.getGamePlayerOf(player.getUniqueId());
                assert optionalPlayer.isPresent();
                final GamePlayer gamePlayer = optionalPlayer.get();
                final Collection<Rune> runes = gamePlayer.getRunes();
                runes.remove(this);
                runes.add(this);
            });
        }

        @Override public void clearFrom(final Player player) {
            currentActive.remove(player.getUniqueId());
            cooldownMap.remove(player.getUniqueId());
            final Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
            if (!optional.isPresent()) {
                throw new IllegalStateException(
                    "Potion effect data could not be gathered for " + player.getUniqueId()
                        .toString());
            }
            //Remove buffs from data
            final PotionEffectData data = optional.get();
            final PotionEffect[] effects = registered.get(player.getUniqueId());
            if (effects.length != 2) {
                return;
            }
            for (final PotionEffect potionEffect : effects) {
                data.remove(potionEffect);
            }
            player.offer(data);
            registered.replace(player.getUniqueId(), new PotionEffect[0]);
            //If player is in a match, update the GamePlayer object
            final Optional<ManagedMatch> optionalMatch =
                SimplePlayerMatchManager.INSTANCE.getMatchOf(player.getUniqueId());
            optionalMatch.ifPresent(managedMatch -> {
                final GameEngine engine = managedMatch.getGameEngine();
                final Optional<? extends GamePlayer> optionalPlayer =
                    engine.getGamePlayerOf(player.getUniqueId());
                assert optionalPlayer.isPresent();
                final GamePlayer gamePlayer = optionalPlayer.get();
                final Collection<Rune> runes = gamePlayer.getRunes();
                runes.remove(this);
            });
        }

        /**
         * Reflects whether the player can have this rune applied.
         *
         * @param uuid The UUID of the player.
         * @return Returns whether the player can see noticable changes when the rune is "applied", checks
         * for if the player already has it or if they are on cooldown.
         */
        public boolean isEligible(final UUID uuid) {
            return !currentActive.containsKey(uuid) && !cooldownMap.containsKey(uuid);
        }

        @Override public String getName() {
            return "Heart Of The Dryad";
        }

        /**
         * Updates the cooldowns and actives.
         */
        @Override public void tick() {
            cooldownMap.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(5L, TimeUnit.SECONDS, null));
            currentActive.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(4L, TimeUnit.SECONDS, (UUID uuid) -> {
                    cooldownMap.put(uuid, 0L);
                    registered.compute(uuid,
                        (unused, unused1) -> new PotionEffect[0]); //If player is no longer active, remove his effects
                }));
        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override @NotNull public DataContainer toContainer() {
            return null; //TODO
        }

        @Listener public void onPotionApplied(final ChangeEntityPotionEffectEvent.Gain event) {
            //Check if the entity can have its this rune applied.
            if (!isEligible(event.getTargetEntity().getUniqueId())) {
                return;
            }
            final PotionEffectType effect = event.getPotionEffect().getType();

            final String name = effect.getName().toLowerCase();
            if (name.contains("fury") || effect == PotionEffectTypes.STRENGTH
                || effect == PotionEffectTypes.RESISTANCE) {
                assert event.getTargetEntity() instanceof Player;
                applyTo((Player) event.getTargetEntity());
            }
        }
    }


    /**
     * Represents Knavis' rune named "Chosen of the Earth"
     */
    public static class ChosenOTEarth extends AbstractRune {

        private static final ChosenOTEarth instance = new ChosenOTEarth();
        private final Map<UUID, Integer> stacks = new HashMap<>();
        private final Map<UUID, Long> tickHistory = new HashMap<>();

        private ChosenOTEarth() {
            Sponge.getEventManager().registerListeners(plugin, this);
        }

        public static ChosenOTEarth getInstance() {
            return instance;
        }

        @Override public void applyTo(final Player player) {
            tickHistory.put(player.getUniqueId(), 0L);
        }

        @Override public void clearFrom(final Player player) {
            tickHistory.remove(player.getUniqueId());
        }

        @Override public String getName() {
            return "Chosen of the Earth";
        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override public DataContainer toContainer() {
            return null;
        }

        /**
         * Handles when a player uses {@link LivingGift}
         */
        @Listener public void onGiftUse(final LivingGift.LivingGiftUseEvent event) {
            final Optional<Player> optionalPlayer = (event.getCause().get("Player", Player.class));
            assert optionalPlayer.isPresent();
            if (!tickHistory.containsKey(optionalPlayer.get().getUniqueId())) {
                return;
            }
            final Player playerObj = optionalPlayer.get();
            tickHistory.replace(playerObj.getUniqueId(), 0L);
            stacks.compute(playerObj.getUniqueId(), ((UUID player, Integer stack) -> {
                final int stackVal =
                    stack == null ? 0 : stack; //Unboxing here may throw null pointer.
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
        @Override public void tick() {
            tickHistory.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(6L, TimeUnit.SECONDS, stacks::remove));
        }
    }
}
