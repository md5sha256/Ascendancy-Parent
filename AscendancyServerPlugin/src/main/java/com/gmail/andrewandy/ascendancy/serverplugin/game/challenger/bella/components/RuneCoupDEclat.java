package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components;

import com.flowpowered.math.vector.Vector3i;
import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.StackData;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RuneCoupDEclat extends AbstractRune {

    private final Collection<UUID> active = new HashSet<>();
    private final Map<UUID, StackData> stackCount = new HashMap<>();
    private final AbilityCircletOfTheAccused boundAbility;

    private final PlayerMatchManager matchManager;
    private final AscendancyServerPlugin plugin;

    @AssistedInject
    RuneCoupDEclat(@Assisted final AbilityCircletOfTheAccused cotcInstance,
                   final PlayerMatchManager matchManager,
                   final AscendancyServerPlugin plugin) {
        super(cotcInstance.getBoundChallenger());
        this.matchManager = matchManager;
        this.plugin = plugin;
        this.boundAbility = cotcInstance;
    }

    @Override
    public void applyTo(final Player player) {
        clearFrom(player);
        active.add(player.getUniqueId());
    }

    @Override
    public void clearFrom(final Player player) {
        active.remove(player.getUniqueId());
    }

    @Override
    @NotNull
    public String getName() {
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

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    @NotNull
    public DataContainer toContainer() {
        return null;
    }

    @Override
    public void tick() {
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
                            (player -> data.isWithinCircle(player.getLocation())));
            int stacks = 0;
            //Loop through all nearby entities.
            for (final Player player : players) {
                optional = matchManager.getTeamOf(player.getUniqueId());
                //Continue if no team or allied.
                if (!optional.isPresent() || team == optional.get()) {
                    continue;
                }
                final StackData stackData = stackCount.get(data.getCaster());
                assert stackData != null;
                //Tick before adding players.
                stackData.tick();
                stackData.addPlayer(player.getUniqueId());
                stacks += stackData.calculateStacks();
                if (stacks == 2) {
                    break;
                }
            }
            final long cooldownRemove =
                    Math.round(Common.toTicks(stacks * 2L, TimeUnit.SECONDS) / 2D);
            assert map.containsKey(data.getCaster());
            final long val = map.get(data.getCaster());
            //Reduce cooldown
            final long newVal = val - cooldownRemove;
            //Remove if cooldown is negative.
            if (newVal < 0) {
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
    @Listener
    public void onMove(final MoveEntityEvent event) {
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
            final boolean inCircle = circletData.isWithinCircle(location);
            //If the player is bella.
            if (entity.getUniqueId().equals(caster)) {
                final Location<World> current = entity.getLocation();

                final double distanceToCentreSquared =
                        current.getPosition().distanceSquared(circletData.getRingCenter().getPosition());
                //If on border edge
                if (Math.abs(distanceToCentreSquared - circletData.getRadius() * circletData.getRadius()) <= 1) {
                    //Teleport 1 block forward bella.
                    ChallengerUtils.teleportPlayer(player, 1);
                }
                return;
            }
            if (!inCircle) {
                //Remove if player is no longer in the circle.
                stackData.removePlayer(entity.getUniqueId());
                return;
            }
            //Add to stack data, will be ticked on next tick?
            stackData.addPlayer(entity.getUniqueId());
        }
    }
}
