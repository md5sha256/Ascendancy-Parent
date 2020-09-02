package com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.components;

import com.flowpowered.math.vector.Vector3i;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.StackData;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RuneCoupDEclat extends AbstractRune {

    private final Collection<UUID> active = new HashSet<>();
    private final Map<UUID, StackData> stackCount = new HashMap<>();
    private final AbilityCircletOfTheAccused boundAbility;

    private final PlayerMatchManager matchManager;
    private final AscendencyServerPlugin plugin;

    @AssistedInject RuneCoupDEclat(@Assisted final AbilityCircletOfTheAccused cotcInstance,
                                   final PlayerMatchManager matchManager,
                                   final AscendencyServerPlugin plugin) {
        super(cotcInstance.getBoundChallenger());
        this.matchManager = matchManager;
        this.plugin = plugin;
        this.boundAbility = cotcInstance;
    }

    @Override public void applyTo(final Player player) {
        clearFrom(player);
        active.add(player.getUniqueId());
    }

    @Override public void clearFrom(final Player player) {
        active.remove(player.getUniqueId());
    }

    @Override public String getName() {
        return "Coup D'eclat";
    }

    /**
     * Generate a cuboid extent of the spherical region.
     * Note, this extent will cover all and more of the spherical region
     * for Bella's AOE, please use {@link CircletData#generateCircleTest()}
     * to filter entities and whatnot in the extent.
     *
     * @param circletData The data to create an extent view from.
     * @return Returns a section (extent) of the world where the ring/spherical region is.
     */
    public Extent getExtentViewFor(final CircletData circletData) {
        final Location<World> location = circletData.getRingCenter();
        final double radius = circletData.getRadius();
        final Vector3i bottom = new Vector3i(location.getX() + radius, location.getY() - radius,
                                             location.getZ() - radius), top;
        top = new Vector3i(location.getX() - radius, location.getY() + radius,
                           location.getZ() + radius);
        return location.getExtent().getExtentView(top, bottom);
    }

    @Override public int getContentVersion() {
        return 0;
    }

    @Override public DataContainer toContainer() {
        return null;
    }

    @Override public void tick() {
        final Map<UUID, Long> map = boundAbility.getCooldowns();
        //Loop through all known circlets to update effects.
        for (final CircletData data : boundAbility.getCircletDataMap().values()) {
            Optional<Team> optional = matchManager.getTeamOf(data.getCaster());
            if (!optional.isPresent()) {
                return;
            }
            final Team team = optional.get();
            final Collection<Player> players = Common
                .getEntities(Player.class, getExtentViewFor(data),
                             (player -> data.generateCircleTest().test(player.getLocation())));
            int stacks = 0;
            for (final Player player : players) { //Loop through all nearby entities.
                optional = matchManager.getTeamOf(player.getUniqueId());
                if (!optional.isPresent() || team == optional
                    .get()) { //Continue if no team or allied.
                    continue;
                }
                final StackData stackData = stackCount.get(data.getCaster());
                assert stackData != null;
                stackData.tick(); //Tick before adding players.
                stackData.addPlayer(player.getUniqueId());
                stacks += stackData.calculateStacks();
                if (stacks == 2) {
                    break;
                }
            }
            final long cooldownRemove =
                Math.round(Common.toTicks(stacks * 2, TimeUnit.SECONDS) / 2D);
            assert map.containsKey(data.getCaster());
            final long val = map.get(data.getCaster());
            //Reduce cooldown
            final long newVal = val - cooldownRemove;
            if (newVal < 0) { //Remove if cooldown is negative.
                map.remove(data.getCaster());
            } else {
                map.replace(data.getCaster(), newVal);
            }
            final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(data.getCaster());
            //Give players absorption
            optionalPlayer.ifPresent(
                (Player player) -> Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    final PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(
                        () -> new IllegalStateException(
                            "Unable to get potiond data for " + player.getName()));
                    peData.addElement(
                        PotionEffect.builder().potionType(PotionEffectTypes.ABSORPTION).amplifier(1)
                            .build());
                    player.offer(peData);
                }).submit(plugin));
        }
    }

    /**
     * Handles bella teleporting in and out of the circle.
     * We don't need to check for the proc event because each entity in the circle will be "ticked".
     */
    @Listener public void onMove(final MoveEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) entity;
        final Location<World> location = player.getLocation();
        for (final CircletData circletData : boundAbility.getCircletDataMap().values()) {
            final UUID caster = circletData.getCaster();
            if (!active.contains(caster)) {
                return;
            }
            final StackData stackData = stackCount.get(caster);
            if (stackData == null) {
                return;
            }
            final boolean inCircle = circletData.generateCircleTest().test(location);
            if (entity.getUniqueId().equals(caster)) { //If the player is bella.
                final Location<World> current = entity.getLocation();

                final double distanceToRadius =
                    MathUtils.calculateDistance3D(current, circletData.getRingCenter());
                if (Math.abs(distanceToRadius - circletData.getRadius()) <= 1) { //If on border edge
                    ChallengerUtils.teleportPlayer(player, 1); //Teleport 1 block forward bella.
                }
                return;
            }
            if (!inCircle) {
                stackData.removePlayer(
                    entity.getUniqueId()); //Remove if player is no longer in the circle.
                return;
            }
            stackData
                .addPlayer(entity.getUniqueId()); //Add to stack data, will be ticked on next tick?
        }
    }
}
