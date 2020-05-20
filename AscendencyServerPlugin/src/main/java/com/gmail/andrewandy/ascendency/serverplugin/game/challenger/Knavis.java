package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendency.lib.game.data.IChampionData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChampionDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.Rune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.LocationMark;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendencyServerEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GameEngine;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GamePlayer;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.Tickable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Represents the Knavis challenger. All abiliities and runes for Knavis can be found here.
 */
public class Knavis extends AbstractChallenger implements Challenger {

    private static final Knavis instance = new Knavis();

    private Knavis() {
        super("Knavis",
                new Ability[]{ShadowsRetreat.instance, LivingGift.instance}, //Abilities
                new PlayerSpecificRune[]{ChosenOTEarth.instance, HeartOfTheDryad.instance, BlessingOfTeleportation.instance}, //Runes
                Season1Challengers.getLoreOf("Knavis")); //Lore
    }

    public static Knavis getInstance() {
        return instance;
    }

    @Override
    public IChampionData toData() {
        try {
            return new ChampionDataImpl(getName(), new File("Path to file on server"), getLore());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create ChampionData", ex);
        }
    }

    public static class LivingGift implements Ability {

        private static LivingGift instance = new LivingGift();
        private Map<UUID, Integer> hitHistory = new HashMap<>();

        private LivingGift() {

        }

        public static LivingGift getInstance() {
            return instance;
        }

        @Override
        public boolean isPassive() {
            return true;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public String getName() {
            return "LivingGift";
        }

        @Listener
        public void onDamage(DamageEntityEvent event) {
            Collection<Player> players = event.getCause().allOf(Player.class);
            for (Player player : players) {
                if (!hitHistory.containsKey(player.getUniqueId())) {
                    continue;
                }
                int hits = hitHistory.get(player.getUniqueId());
                if (hits++ == 3) {
                    HealthData data = player.getHealthData();
                    data.set(data.health().transform((Double val) -> val + 3.0)); //Add 3 health or 1.5 hearts.
                    player.offer(data); //Update the player object.
                    hits = 0;
                    new LivingGiftUseEvent(player).callEvent();
                }
                hitHistory.replace(player.getUniqueId(), hits); //Update hit count
            }
        }

        private class LivingGiftUseEvent extends AscendencyServerEvent {

            private Cause cause;

            public LivingGiftUseEvent(Player player) {
                this.cause = Cause.builder().named("Player", player).build();
            }

            @Override
            public Cause getCause() {
                return cause;
            }
        }
    }

    public static class ShadowsRetreat implements Ability, Tickable {

        public static final Long[] defaultTickThreshold = new Long[]{Common.toTicks(6, TimeUnit.SECONDS), Common.toTicks(6, TimeUnit.SECONDS)};
        private static final ShadowsRetreat instance = new ShadowsRetreat();
        private UUID uuid = UUID.randomUUID();
        private Map<UUID, LocationMark> dataMap = new HashMap<>();
        private BiFunction<UUID, LocationMark, Long[]> tickThresholdFunction;
        private BiConsumer<Player, Integer> onMark;

        private ShadowsRetreat() {
        }

        public static ShadowsRetreat getInstance() {
            return instance;
        }

        public void setTickThresholdSupplier(BiFunction<UUID, LocationMark, Long[]> tickThresholdFunction) {
            this.tickThresholdFunction = tickThresholdFunction;
        }

        public void setOnMark(BiConsumer<Player, Integer> onMark) {
            this.onMark = onMark;
        }

        public Optional<LocationMark> getMarkFor(UUID player) {
            if (dataMap.containsKey(player)) {
                return Optional.of(dataMap.get(player));
            }
            return Optional.empty();
        }

        @Override
        public UUID getUniqueID() {
            return uuid;
        }

        @Override
        public String getName() {
            return "Shadow's Retreat";
        }

        @Override
        public boolean isPassive() {
            return false;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void tick() {
            dataMap.forEach((UUID player, LocationMark mark) -> {
                Long[] ticks = tickThresholdFunction == null ? defaultTickThreshold : tickThresholdFunction.apply(player, mark);
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


        private void mark(Player player) {
            if (!dataMap.containsKey(player.getUniqueId())) {
                throw new IllegalArgumentException("Player does not have this ability!");
            }
            LocationMark mark = dataMap.get(player.getUniqueId());
            mark.setPrimaryMark(player.getLocation());
            mark.resetPrimaryTick();
        }

        //TODO add listeners

        @Listener
        public void onHotbarChange(ChangeInventoryEvent.Held event) {
            Cause cause = event.getCause();
            Collection<Player> livings = cause.allOf(Player.class);
            if (livings.size() < 1) {
                return;
            }
            Player player = livings.iterator().next();
            if (!dataMap.containsKey(player.getUniqueId())) {
                return;
            }
            Inventory inventory = player.getInventory();
            Optional<ItemStack> clicked = player.getItemInHand(HandTypes.MAIN_HAND);
            clicked.ifPresent((stack) -> {
                Optional<SlotIndex> index = inventory.getProperty(SlotIndex.class, SlotIndex.of(stack));
                index.ifPresent((SlotIndex slotIndex) -> {
                    assert slotIndex.getValue() != null;
                    if (onMark != null) {
                        onMark.accept(player, slotIndex.getValue());
                    }
                    if (slotIndex.getValue() != 2 || slotIndex.getValue() != 1) {
                        return;
                    }
                    mark(player);
                });

            });
        }
    }

    /**
     * Represents the rune BlessingOfTeleportation.
     */
    public static class BlessingOfTeleportation extends AbstractRune {

        private static final BlessingOfTeleportation instance = new BlessingOfTeleportation();
        private static final long ticks = Common.toTicks(8, TimeUnit.SECONDS);
        private Collection<UUID> active = new HashSet<>();

        private BlessingOfTeleportation() {
            ShadowsRetreat.instance.setTickThresholdSupplier( //Basically checks if they have this ability active, if so increase duration of marks to 8 sec
                    (UUID player, LocationMark mark) ->
                            active.contains(player) ? new Long[]{ticks, ticks} : ShadowsRetreat.defaultTickThreshold);
            ShadowsRetreat.instance.setOnMark((Player player, Integer slot) -> {
                assert slot != null;
                if (slot == 2) { //Don't need to care about 1 since it will be handled by Shadow's retreat.
                    Optional<LocationMark> mark = ShadowsRetreat.instance.getMarkFor(player.getUniqueId());
                    assert mark.isPresent();
                    LocationMark locationMark = mark.get();
                    locationMark.setSecondaryMark(player.getLocation());
                    locationMark.resetSecondaryTick();
                }
            });
        }

        public static BlessingOfTeleportation getInstance() {
            return instance;
        }

        @Override
        public void applyTo(Player player) {
            clearFrom(player);
            active.add(player.getUniqueId());
        }

        @Override
        public void clearFrom(Player player) {
            active.remove(player.getUniqueId());
            Optional<LocationMark> optional = ShadowsRetreat.getInstance().getMarkFor(player.getUniqueId());
            optional.ifPresent(LocationMark::clear);
        }

        @Override
        public String getName() {
            return "Blessing Of Teleportation";
        }

        @Override
        public void tick() {
            //This method does not actually need to tick since that is handled by the main ability
        }

        @Override
        public int getContentVersion() {
            return 0;
        }

        @Override
        public DataContainer toContainer() {
            return null;
        }
    }

    /**
     * Represents the rune HeartOfTheDryad
     */
    public static class HeartOfTheDryad extends AbstractRune {

        private static final HeartOfTheDryad instance = new HeartOfTheDryad();
        private Map<UUID, PotionEffect[]> registered = new HashMap<>();
        private Map<UUID, Long> currentActive = new HashMap<>();
        private Map<UUID, Long> cooldownMap = new HashMap<>();

        private HeartOfTheDryad() {
        }

        public static HeartOfTheDryad getInstance() {
            return instance;
        }

        @Override
        public void applyTo(Player player) {
            clearFrom(player);
            currentActive.put(player.getUniqueId(), 0L);
            Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
            if (!optional.isPresent()) {
                throw new IllegalStateException("Potion effect data could not be gathered for " + player.getUniqueId().toString());
            }

            PotionEffectData data = optional.get();
            PotionEffect[] effects = new PotionEffect[]{PotionEffect.builder()
                    //Level 2 movement speed
                    .potionType(PotionEffectTypes.SPEED)
                    .duration(4).amplifier(2).build(), PotionEffect.builder()
                    //20% Attack speed
                    .potionType(PotionEffectTypes.HASTE)
                    .duration(4).amplifier(2).build()};
            //Root / Entanglement
            for (PotionEffect effect : effects) {
                data.addElement(effect);
            }
            player.offer(data);
            registered.put(player.getUniqueId(), effects);
            Optional<ManagedMatch> optionalMatch = SimplePlayerMatchManager.INSTANCE.getMatchOf(player.getUniqueId());
            optionalMatch.ifPresent(managedMatch -> {
                GameEngine engine = managedMatch.getGameEngine();
                Optional<? extends GamePlayer> optionalPlayer = engine.getGamePlayerOf(player.getUniqueId());
                assert optionalPlayer.isPresent();
                GamePlayer gamePlayer = optionalPlayer.get();
                Collection<Rune> runes = gamePlayer.getRunes();
                runes.remove(this);
                runes.add(this);
            });
        }

        @Override
        public void clearFrom(Player player) {
            currentActive.remove(player.getUniqueId());
            cooldownMap.remove(player.getUniqueId());
            Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
            if (!optional.isPresent()) {
                throw new IllegalStateException("Potion effect data could not be gathered for " + player.getUniqueId().toString());
            }
            //Remove buffs from data
            PotionEffectData data = optional.get();
            PotionEffect[] effects = registered.get(player.getUniqueId());
            if (effects.length != 2) {
                return;
            }
            for (PotionEffect potionEffect : effects) {
                data.remove(potionEffect);
            }
            player.offer(data);
            registered.replace(player.getUniqueId(), new PotionEffect[0]);
            //If player is in a match, update the GamePlayer object
            Optional<ManagedMatch> optionalMatch = SimplePlayerMatchManager.INSTANCE.getMatchOf(player.getUniqueId());
            optionalMatch.ifPresent(managedMatch -> {
                GameEngine engine = managedMatch.getGameEngine();
                Optional<? extends GamePlayer> optionalPlayer = engine.getGamePlayerOf(player.getUniqueId());
                assert optionalPlayer.isPresent();
                GamePlayer gamePlayer = optionalPlayer.get();
                Collection<Rune> runes = gamePlayer.getRunes();
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
        public boolean isEligible(UUID uuid) {
            return !currentActive.containsKey(uuid) && !cooldownMap.containsKey(uuid);
        }

        @Override
        public String getName() {
            return "Heart Of The Dryad";
        }

        /**
         * Updates the cooldowns and actives.
         */
        @Override
        public void tick() {
            cooldownMap.entrySet().removeIf(ChallengerUtils.mapTickPredicate(4L, TimeUnit.SECONDS, null));
            currentActive.entrySet().removeIf(ChallengerUtils.mapTickPredicate(5L, TimeUnit.SECONDS, (UUID uuid) -> {
                cooldownMap.put(uuid, 0L);
                registered.compute(uuid, (unused, unused1) -> new PotionEffect[0]); //If player is no longer active, remove his effects
            }));
        }

        @Override
        public int getContentVersion() {
            return 0;
        }

        @Override
        public DataContainer toContainer() {
            return null;
        }

        @Listener
        public void onPotionApplied(ChangeEntityPotionEffectEvent.Gain event) {
            //Check if the entity can have its this rune applied.
            if (!isEligible(event.getTargetEntity().getUniqueId())) {
                return;
            }
            PotionEffectType effect = event.getPotionEffect().getType();

            String name = effect.getName().toLowerCase();
            if (name.contains("fury")  || effect == PotionEffectTypes.STRENGTH || effect == PotionEffectTypes.RESISTANCE) {
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
        private Map<UUID, Integer> stacks = new HashMap<>();
        private Map<UUID, Long> tickHistory = new HashMap<>();

        private ChosenOTEarth() {
            Sponge.getEventManager().registerListeners(AscendencyServerPlugin.getInstance(), this);
        }

        public static ChosenOTEarth getInstance() {
            return instance;
        }

        @Override
        public void applyTo(Player player) {
            tickHistory.put(player.getUniqueId(), 0L);
        }

        @Override
        public void clearFrom(Player player) {
            tickHistory.remove(player.getUniqueId());
        }

        @Override
        public String getName() {
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
         * Handles when a player uses {@link LivingGift}
         */
        @Listener
        public void onGiftUse(LivingGift.LivingGiftUseEvent event) {
            Optional<Player> optionalPlayer = (event.getCause().get("Player", Player.class));
            assert optionalPlayer.isPresent();
            if (!tickHistory.containsKey(optionalPlayer.get().getUniqueId())) {
                return;
            }
            Player playerObj = optionalPlayer.get();
            tickHistory.replace(playerObj.getUniqueId(), 0L);
            stacks.compute(playerObj.getUniqueId(), ((UUID player, Integer stack) -> {
                int stackVal = stack == null ? 0 : stack; //Unboxing here may throw nullpointer.
                double health = 3;
                for (int index = 1; index < stackVal; ) {
                    health += index++;
                }
                Common.addHealth(playerObj, health - 3); //Sets the total health to a value between 3 and 7 (adds on to LivingGift)
                return stackVal == 4 ? stackVal : stackVal + 1; //If stack = 4, then max has been reached, therefore its 4 or stack + 1;
            }));
        }

        /**
         * Updates the stack history.
         */
        @Override
        public void tick() {
            tickHistory.entrySet().removeIf(ChallengerUtils.mapTickPredicate(6L, TimeUnit.SECONDS, stacks::remove));
        }
    }
}
