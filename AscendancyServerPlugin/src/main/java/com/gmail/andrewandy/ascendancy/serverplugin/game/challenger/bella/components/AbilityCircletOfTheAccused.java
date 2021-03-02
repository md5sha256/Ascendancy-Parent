package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.api.event.AscendancyServerEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.Bella;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.BellaComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.CustomEvents;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.KeyBindHandler;
import com.google.common.base.Preconditions;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AbilityCircletOfTheAccused extends AbstractCooldownAbility {


    private final Map<UUID, CircletData> registeredMap = new HashMap<>();
    private final BellaComponentFactory factory;
    private final PlayerMatchManager matchManager;
    private final KeyBindHandler keyBindHandler;
    private DamageEntityEvent lastDamageEvent;
    private RuneCoupDEclat rune;

    @AssistedInject
    AbilityCircletOfTheAccused(
            @Assisted final Challenger bound,
            final BellaComponentFactory factory,
            final PlayerMatchManager matchManager,
            final KeyBindHandler keyBindHandler
    ) {
        super("Circlet Of The Accused", true, 5, TimeUnit.SECONDS, bound);
        this.factory = factory;
        this.matchManager = matchManager;
        this.keyBindHandler = keyBindHandler;
    }

    public void init(@NotNull final RuneCoupDEclat boundRune) {
        if (rune == null) {
            Preconditions.checkNotNull(boundRune);
            Preconditions.checkArgument(boundRune.getBoundChallenger() == this.bound);
            this.rune = boundRune;
        }
    }

    public Map<UUID, CircletData> getCircletDataMap() {
        return new HashMap<>(registeredMap);
    }


    /**
     * Activates this ability as a certain player.
     *
     * @param targetUID        The UUID to activate as.
     * @param radius           The radius of the circle.
     * @param respectCooldowns Whether we should respect cooldowns.
     */
    public void activateAs(
            final UUID caster, final UUID targetUID, final int radius,
            final boolean respectCooldowns
    ) {
        final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(targetUID);
        if (!optionalPlayer.isPresent()) {
            throw new IllegalArgumentException("Player does not exist!");
        }
        if (respectCooldowns && isOnCooldown(caster) || registeredMap.containsKey(caster)) {
            return;
        }

        resetCooldown(caster);
        final Player player = optionalPlayer.get();
        final Location<World> location = player.getLocation();
        final Vector3d playerPos = location.getPosition();
        final Collection<Entity> nearby = player.getNearbyEntities(entity -> entity instanceof Player &&
                entity.getTransform().getPosition().distanceSquared(playerPos) <= 100D);
        Entity target = null;
        double leastDistance = Double.MAX_VALUE;
        for (final Entity entity : nearby) {
            // assert entity instanceof Player;
            final double distance = playerPos.distanceSquared(entity.getLocation().getPosition());
            if (distance < leastDistance) {
                target = entity;
                leastDistance = distance;
            }
        }
        if (target == null) {
            return;
        }
        registeredMap.compute(target.getUniqueId(), (playerUID, circletData) -> {
            if (circletData == null) {
                //Caster is the playerUID, default radius = 3
                circletData = factory.createCircletData(caster, radius);
            }
            circletData.reset();
            circletData.setRingBlocks(Bella.generateCircleBlocks(location, radius));
            circletData.setRingCenter(location);
            return circletData;
        }); //Update the map.
    }

    /**
     * Delete a circlet from a player.
     *
     * @param player The player object to clear from.
     * @return Returns if the operation was successful.
     */
    public boolean clearCirclet(@NotNull final Player player) {
        final CircletData data = registeredMap.remove(player.getUniqueId());
        if (data == null) {
            return false;
        }
        data.reset();
        return true;
    }

    /**
     * Get the CircletData from a specified location.
     *
     * @param location The location to get the circlet for.
     * @return Returns an optional, populated if an circlet exists at a given
     *         location checked using {@link CircletData#generateCircleTest()}.
     */
    public Optional<CircletData> getCircletAt(@NotNull final Location<World> location) {
        final Location<World> copy = location.copy();
        for (final CircletData circletData : registeredMap.values()) {
            if (circletData.isWithinCircle(copy)) {
                return Optional.of(circletData);
            }
        }
        return Optional.empty();
    }

    public Optional<CircletData> getCircletDataFor(@NotNull final UUID player) {
        return Optional.ofNullable(registeredMap.get(player));
    }

    @Override
    public void tick() {
        super.tick();
        registeredMap.forEach((key, data) -> {
            data.incrementTick();
            if (data.getTickCount() >= getCooldownDuration()) {
                //Clear the ring
                data.reset();
                resetCooldown(key);
            }
            cooldownMap.entrySet().removeIf(ChallengerUtils.mapTickPredicate(9, TimeUnit.SECONDS,
                    (uuid) -> Sponge
                            .getServer()
                            .getPlayer(uuid)
                            .ifPresent(
                                    this::clearCirclet)
            ));


            final Optional<ManagedMatch> match = matchManager.getMatchOf(key);
            match.ifPresent(managedMatch -> {
                final Team team = managedMatch.getTeamOf(key);
                final Collection<Player> players = Common
                        .getEntities(Player.class, rune.getExtentViewFor(data), (Player player) -> {
                            final Optional<Team> optional =
                                    matchManager.getTeamOf(player.getUniqueId());
                            return optional.isPresent() && optional.get() != team && data.isWithinCircle(player.getLocation());
                        });
                for (Player player : players) {
                    final Optional<Team> optionalTeam = matchManager.getTeamOf(key);
                    if (!optionalTeam.isPresent()) {
                        return;
                    }
                    //If allied, skip
                    if (optionalTeam.get() == team) {
                        return;
                    }
                    //TODO Set scoreboard so cmd-block impl knows they are in circle.
                    //Means they are an enemy.
                    final PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Unable to get PotionEffect data for " + player.getName()));

                    peData.addElement(
                            PotionEffect.builder().potionType(PotionEffectTypes.WITHER).duration(1)
                                    .amplifier(1).build()); //Wither
                    //.addElement((PotionEffect) new BuffEffectAstralDistortion(1, 1)); //Astral Distorton == Astral Nullifcation
                    player.offer(peData);
                }
            });
        });
    }


    @Listener
    public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
        if (keyBindHandler
                .isKeyPressed(event.getPlayer())) { //If player was holding the key then skip.
            return;
        }
        final ProcEvent procEvent = new ProcEvent(event.getPlayer(), event.getPlayer(), 2);
        if (procEvent.callEvent()) { //If not cancelled
            final Optional<ManagedMatch> match =
                    matchManager.getMatchOf(event.getPlayer().getUniqueId());
            if (!match.isPresent()) {
                return;
            }
            final Player target = procEvent.getTarget();
            activateAs(procEvent.getInvoker().getUniqueId(), target.getUniqueId(),
                    procEvent.circletRadius, true
            );
        }
    }

    @Listener
    public void onFatalDamage(final DamageEntityEvent event) {
        if (event == lastDamageEvent) {
            return;
        }
        final Entity entity = event.getTargetEntity();
        if (!event.willCauseDeath()) {
            return;
        }
        registeredMap.values().stream()
                .filter(circletData -> circletData.isWithinCircle(entity.getLocation()))
                .findAny().ifPresent((circletData -> {

            event.setCancelled(true);
            final Cause cause = Cause.builder().named("Source", circletData.getCaster()).build();
            lastDamageEvent = SpongeEventFactory
                    .createDamageEntityEvent(cause, event.getOriginalFunctions(), entity,
                            event.getOriginalDamage()
                    );
            Sponge.getEventManager().post(lastDamageEvent);
            entity.damage(
                    event.getFinalDamage(),
                    DamageSource.builder().from(DamageSources.GENERIC).build()
            );
        }));
    }

    @Listener(order = Order.DEFAULT)
    public void onJump(final CustomEvents.PlayerJumpEvent event) {
        final Player player = event.getPlayer();
        final Optional<CircletData> optionalCirclet = getCircletAt(player.getLocation());
        if (optionalCirclet.isPresent() && !optionalCirclet.get().getCaster()
                .equals(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    /**
     * Represents when {@link AbilityCircletOfTheAccused} is activated.
     */
    public static class ProcEvent extends AscendancyServerEvent implements Cancellable {

        @NotNull
        private final Cause cause;
        @NotNull
        private final Player invoker;
        private boolean cancel;
        private int circletRadius;
        @NotNull
        private Player target;

        ProcEvent(
                @NotNull final Player invoker, @NotNull final Player target,
                final int circletRadius
        ) {
            this.cause = Cause.builder().named("Bella", invoker).build();
            this.invoker = invoker;
            this.target = target;
            if (circletRadius < 1) {
                throw new IllegalArgumentException("Circle radius must be greater than 0");
            }
            this.circletRadius = circletRadius;
        }

        @Override
        public boolean isCancelled() {
            return cancel;
        }

        @Override
        public void setCancelled(final boolean cancel) {
            this.cancel = cancel;
        }

        public void setCircletRadius(final int radius) {
            if (circletRadius < 1) {
                throw new IllegalArgumentException("Circle radius must be greater than 0");
            }
            this.circletRadius = radius;
        }

        @Override
        @NotNull
        public Cause getCause() {
            return cause;
        }

        public Player getTarget() {
            return target;
        }

        public void setTarget(final Player target) {
            this.target = target;
        }

        public Player getInvoker() {
            return invoker;
        }

    }

}
