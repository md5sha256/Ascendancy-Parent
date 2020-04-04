package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.buffs.BuffEffectAstralDistortion;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendencyServerEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.Tickable;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyPressedEvent;
import javafx.util.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Bella extends AbstractChallenger {

    private static final Bella instance = new Bella();

    private Bella() {
        super("Bella",
                new Ability[]{CircletOfTheAccused.instance, ReleasedRebellion.instance},
                new PlayerSpecificRune[0],
                Challengers.getLoreOf("Bella"));
    }

    public static Bella getInstance() {
        return instance;
    }

    /**
     * Creates and places a nether-brick ring.
     *
     * @param centre The centre of the circle.
     * @param radius The radius.
     * @return Returns a Collection of Blocks which were placed.
     */
    public static Collection<Location<World>> generateCircleBlocks(Location<World> centre, int radius) {
        Collection<Location<World>> rawCircle = MathUtils.createCircle(centre, radius);
        final Cause cause = Cause.builder().named("Bella", AscendencyServerPlugin.getInstance()).build();
        rawCircle.forEach((location -> location.setBlockType(BlockTypes.AIR, cause)));
        return rawCircle;
    }

    @Override
    public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to icon"), getLore());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static class CircletOfTheAccused extends AbstractAbility implements Tickable {
        private static final CircletOfTheAccused instance = new CircletOfTheAccused();
        private static final long timeout = Common.toTicks(5, TimeUnit.SECONDS);
        private UUID uniqueID = UUID.randomUUID();
        private Map<UUID, CircletData> registeredMap = new HashMap<>();
        private Map<UUID, Long> cooldownMap = new HashMap<>();
        private DamageEntityEvent lastDamageEvent;

        private CircletOfTheAccused() {
            super("Circlet Of The Accused", true);
        }

        public static CircletOfTheAccused getInstance() {
            return instance;
        }

        @Override
        public UUID getUniqueID() {
            return uniqueID;
        }

        /**
         * Activates this ability as a certain player.
         *
         * @param targetUID        The UUID to activate as.
         * @param radius
         * @param respectCooldowns
         */
        public void activateAs(UUID caster, UUID targetUID, int radius, boolean respectCooldowns) {
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(targetUID);
            if (!optionalPlayer.isPresent()) {
                throw new IllegalArgumentException("Player does not exist!");
            }
            if (respectCooldowns && cooldownMap.containsKey(caster) || registeredMap.containsKey(caster)) {
                return;
            }

            Player player = optionalPlayer.get();
            Collection<Entity> nearby = player.getNearbyEntities((entity) -> MathUtils.calculateDistance(entity.getLocation(), player.getLocation()) <= 10D);
            Player target = null;
            double leastDistance = Double.MAX_VALUE;
            Location<World> location = player.getLocation();
            for (Entity entity : nearby) {
                if (!(entity instanceof Player)) {
                    continue;
                }
                double distance = MathUtils.calculateDistance(location, entity.getLocation());
                if (distance < leastDistance) {
                    target = (Player) entity;
                    leastDistance = distance;
                }
            }
            if (target == null) {
                return;
            }
            registeredMap.compute(target.getUniqueId(), (playerUID, circletData) -> {
                if (circletData == null) {
                    circletData = new CircletData(caster, radius); //Caster is the playerUID, default radius = 4
                }
                circletData.reset();
                circletData.setRingBlocks(generateCircleBlocks(location, radius));
                circletData.setRingCenter(location);
                return circletData;
            }); //Update the map.
        }

        public boolean clearCircletFrom(Player player) {
            if (!registeredMap.containsKey(player.getUniqueId())) {
                return false;
            }
            registeredMap.get(player.getUniqueId()).reset();
            registeredMap.remove(uniqueID);
            return true;
        }

        @Override
        public void tick() {
            registeredMap.forEach((key, data) -> {
                data.incrementTick();
                if (data.getTickCount() >= timeout) {
                    data.reset(); //Clear the ring
                    cooldownMap.put(key, 0L);
                }
            });
            cooldownMap.entrySet().removeIf(ChallengerUtils.mapTickPredicate(9, TimeUnit.SECONDS, null));
        }

        @Listener
        public void onPlayerMove(MoveEntityEvent event) {
            Entity entity = event.getTargetEntity();
            if (!(entity instanceof Player)) {
                return;
            }
            Optional<ManagedMatch> match = SimplePlayerMatchManager.INSTANCE.getMatchOf(entity.getUniqueId());
            match.ifPresent(managedMatch -> {
                for (CircletData data : registeredMap.values()) {
                    if (!data.generateCircleTest().test(entity.getLocation())) {
                        continue;
                    }
                    Optional<Team> optionalTeam = SimplePlayerMatchManager.INSTANCE.getTeamOf(entity.getUniqueId());
                    Team entityTeam = managedMatch.getTeamOf(entity.getUniqueId());
                    if (!data.getCaster().equals(entity.getUniqueId()) && optionalTeam.isPresent() && optionalTeam.get() != entityTeam) {
                        //Means they are an enemy.
                        Optional<PotionEffectData> peData = entity.get(PotionEffectData.class);
                        if (!peData.isPresent()) {
                            throw new IllegalArgumentException("Unable to get PotionEffect data for " + ((Player) entity).getName());
                        }
                        PotionEffectData effectData = peData.get();
                        effectData.addElement(
                                PotionEffect.builder().potionType(PotionEffectTypes.WITHER).duration(1).amplifier(1).build()) //Wither
                                .addElement((PotionEffect) new BuffEffectAstralDistortion(1, 1)); //Astral "Distortion" or "Nullification" ??? TODO
                    }
                }
            });
        }

        //TODO
        @Listener
        public void onActiveKeyPress(ActiveKeyPressedEvent event) {
            ProcEvent procEvent = new ProcEvent(event.getPlayer(), event.getPlayer(), 4);
            if (procEvent.callEvent()) { //If not cancelled
                Optional<ManagedMatch> match = SimplePlayerMatchManager.INSTANCE.getMatchOf(uniqueID);
                if (!match.isPresent()) {
                    return;
                }
                Player target = procEvent.getTarget();
                activateAs(procEvent.getInvoker().getUniqueId(), target.getUniqueId(), procEvent.circletRadius, true);
            }
        }

        @Listener
        public void onFatalDamage(DamageEntityEvent event) {
            if (event == lastDamageEvent) {
                return;
            }
            Entity entity = event.getTargetEntity();
            if (!event.willCauseDeath()) {
                return;
            }
            registeredMap.values().stream()
                    .filter(circletData -> circletData.generateCircleTest().test(entity.getLocation()))
                    .findAny().ifPresent((circletData -> {

                event.setCancelled(true);
                Cause cause = Cause.builder().named("Source", circletData.caster).build();
                lastDamageEvent = SpongeEventFactory.createDamageEntityEvent(cause, event.getOriginalFunctions(), entity, event.getOriginalDamage());
                Sponge.getEventManager().post(lastDamageEvent);
                entity.damage(event.getFinalDamage(), DamageSource.builder().from(DamageSources.MAGIC).build());
            }));
        }

        private static class ProcEvent extends AscendencyServerEvent implements Cancellable {

            private boolean cancel;
            private Cause cause;
            private int circletRadius;
            private Player invoker;
            private Player target;

            ProcEvent(Player invoker, Player target, int circletRadius) {
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
            public void setCancelled(boolean cancel) {
                this.cancel = cancel;
            }

            @Override
            public Cause getCause() {
                return cause;
            }

            public Player getTarget() {
                return target;
            }

            public void setTarget(Player target) {
                this.target = target;
            }

            public Player getInvoker() {
                return invoker;
            }
        }


        private static class CircletData {

            private UUID caster;
            private int radius;
            private long tickCount = 0;
            private Location<World> ringCenter;
            private Collection<Location<World>> ringBlocks;

            public CircletData(UUID caster, int radius) {
                this.caster = caster;
                if (radius < 1) {
                    throw new IllegalArgumentException("Radius must be greater than 1");
                }
                this.radius = radius;
            }

            public int getRadius() {
                return radius;
            }

            public UUID getCaster() {
                return caster;
            }

            public void setCaster(UUID caster) {
                this.caster = caster;
            }

            public void incrementTick() {
                this.tickCount++;
            }

            public Collection<Location<World>> getRingBlocks() {
                return ringBlocks == null ? new HashSet<>() : new HashSet<>(ringBlocks);
            }

            public void setRingBlocks(Collection<Location<World>> ringBlocks) {
                this.ringBlocks = ringBlocks;
            }

            public Location<World> getRingCenter() {
                return ringCenter;
            }

            public void setRingCenter(Location<World> ringCenter) {
                this.ringCenter = ringCenter;
            }

            public long getTickCount() {
                return tickCount;
            }

            public Predicate<Location<World>> generateCircleTest() {
                if (ringCenter != null) {
                    return MathUtils.isWithinSphere(ringCenter, 4);
                }
                return (unused) -> false;
            }

            public void reset() {
                this.tickCount = 0L;
                this.ringCenter = null;
                Cause cause = Cause.builder().named("Bella", AscendencyServerPlugin.getInstance()).build();
                ringBlocks.forEach(location -> location.setBlockType(BlockTypes.AIR, cause));
                this.ringBlocks = null;
            }
        }
    }

    public static class ReleasedRebellion extends AbstractAbility implements Tickable {

        private static final ReleasedRebellion instance = new ReleasedRebellion();
        private UUID uuid = UUID.randomUUID();
        private Collection<UUID> active = new HashSet<>();
        private Map<UUID, Pair<UUID, Long>> tickMap = new HashMap<>();

        private ReleasedRebellion() {
            super("Released Rebellion", false);
        }

        public static ReleasedRebellion getInstance() {
            return instance;
        }

        public void register(UUID player) {
            active.remove(player);
            active.add(player);
        }

        public void unregister(UUID player) {
            active.remove(player);
            active.add(player);
        }

        @Listener
        public void onPlayerAttack(DamageEntityEvent event) {
            Entity target = event.getTargetEntity();
            Player player;
            Optional<Optional<Player>> optionalPlayer = event.getCause().allOf(UUID.class).stream().map((uuid) -> Sponge.getServer().getPlayer(uuid)).findAny();
            player = optionalPlayer.orElse(Optional.empty()).orElse(null);
            if (player == null || !active.contains(player.getUniqueId())) {
                return;
            }
            tickMap.compute(player.getUniqueId(), ((uuid, pair) -> new Pair<>(target.getUniqueId(), 0L)));
        }


        @Override
        public UUID getUniqueID() {
            return uuid;
        }

        @Override
        public void tick() {
            long ticks = Common.toTicks(3, TimeUnit.SECONDS);
            tickMap.entrySet().removeIf(entry -> {
                Pair<UUID, Long> pair = entry.getValue();
                long val = pair.getValue() + 1;
                entry.setValue(new Pair<>(pair.getKey(), val));
                return val >= ticks;
            });
        }

        @Listener
        public void onProc(CircletOfTheAccused.ProcEvent event) { //Handles the proc event
            Player invoker = event.getInvoker();
            if (!tickMap.containsKey(invoker.getUniqueId())) {
                return;
            }
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(tickMap.get(invoker.getUniqueId()).getKey());
            optionalPlayer.ifPresent(event::setTarget);
        }
    }

    public static class CoupDEclat extends AbstractRune {

        private static final CoupDEclat instance = new CoupDEclat();

        public static CoupDEclat getInstance() {
            return instance;
        }

        private Collection<UUID> active = new HashSet<>();
        private Map<UUID, StackData> stackCount = new HashMap<>();


        @Override
        public void applyTo(Player player) {
            clearFrom(player);
            active.add(player.getUniqueId());
        }

        @Override
        public void clearFrom(Player player) {
            active.remove(player.getUniqueId());
        }

        @Override
        public String getName() {
            return "Coup D'eclat";
        }

        @Override
        public void tick() {
            Map<UUID, Long> map = CircletOfTheAccused.getInstance().cooldownMap;
            //Loop through all known circlets to update effects.
            for (CircletOfTheAccused.CircletData data : CircletOfTheAccused.getInstance().registeredMap.values()) {
                Optional<Team> optional = SimplePlayerMatchManager.INSTANCE.getTeamOf(data.getCaster());
                if (!optional.isPresent()) {
                    return;
                }
                Team team = optional.get();
                Location<World> location = data.getRingCenter();
                Vector3i vector = new Vector3i();
                vector.sub(location.getX(), location.getY(), location.getZ());
                Extent view = location.getExtent().getExtentView(vector, vector);
                Collection<Player> players = view.getEntities().stream().filter((entity -> entity instanceof Player)).map((entity -> (Player) entity)).collect(Collectors.toSet());
                int stacks = 0;
                for (Player player : players) { //Loop through all nearby entities.
                    optional = SimplePlayerMatchManager.INSTANCE.getTeamOf(player.getUniqueId());
                    if (!optional.isPresent() || team == optional.get()) { //Continue if no team or allied.
                        continue;
                    }
                    StackData stackData = stackCount.get(data.getCaster());
                    assert stackData != null;
                    stackData.tick(); //Tick before adding players.
                    stackData.addPlayer(player.getUniqueId());
                    stacks += stackData.calculateStacks();
                    if (stacks == 2) {
                        break;
                    }
                }
                long cooldownRemove = Math.round(Common.toTicks(stacks * 2, TimeUnit.SECONDS) / 2D);
                assert map.containsKey(data.getCaster());
                long val = map.get(data.getCaster());
                //Reduce cooldown
                long newVal = val - cooldownRemove;
                if (newVal < 0) { //Remove if cooldown is negative.
                    map.remove(data.getCaster());
                } else {
                    map.replace(data.getCaster(), newVal);
                }
                Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(data.getCaster());
                //Give players absorption
                optionalPlayer.ifPresent((Player player) -> Sponge.getScheduler().createTaskBuilder()
                        .execute(() -> {
                            PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(() -> new IllegalStateException("Unable to get potiond data for " + player.getName()));
                            peData.addElement(PotionEffect.builder().potionType(PotionEffectTypes.ABSORPTION).amplifier(1).build());
                            player.offer(peData);
                        }).submit(AscendencyServerPlugin.getInstance()));
            }
        }

        @Listener //TODO Should we be listening to a move event or a proc event?
        public void onMove(MoveEntityEvent event) {
            Entity entity = event.getTargetEntity();
            if (!(entity instanceof Player)) {
                return;
            }
            Location<World> location = entity.getLocation();
            CircletOfTheAccused.getInstance().registeredMap.values().forEach(circletData -> {
                UUID caster = circletData.getCaster();
                if (!active.contains(caster)) {
                    return;
                }
                StackData stackData = stackCount.get(caster);
                if (stackData == null) {
                    return;
                }
                boolean inCircle = circletData.generateCircleTest().test(location);
                if (entity.getUniqueId().equals(caster)) { //If the player is bella.
                    Location<World> current = entity.getLocation();
                    Player player = (Player) entity;
                    Vector3d headRotation = player.getHeadRotation();
                    double distanceToRadius = MathUtils.calculateDistance(current, circletData.getRingCenter());
                    if (distanceToRadius - 1 <= circletData.radius) { //If on border edge
                        //TODO Teleport based on direction (Pitch & Roll)
                        player.setLocationSafely(new Location<>(current.getExtent(), headRotation)); //Teleport bella.
                        return;
                    }
                }
                if (!inCircle) {
                    stackData.removePlayer(entity.getUniqueId()); //Remove if player is no longer in the circle.
                    return;
                }
                stackData.addPlayer(entity.getUniqueId()); //Add to stack data, will be ticked on next tick?
            });
        }

        @Override
        public int getContentVersion() {
            return 0;
        }

        @Override
        public DataContainer toContainer() {
            return null;
        }

        private static class StackData {

            private Map<UUID, Long> stackTime = new HashMap<>();

            public void addPlayer(UUID player) {
                if (stackTime.containsKey(player)) {
                    return;
                }
                stackTime.put(player, 0L);
            }

            public void removePlayer(UUID player) {
                stackTime.remove(player);
            }

            public long getTickCount(UUID player) {
                Long val = stackTime.get(player);
                return val == null ? 0L : val;
            }


            public void tick() {
                stackTime.entrySet().forEach((entry -> entry.setValue(entry.getValue() + 1)));
            }

            public int calculateStacks() {
                int stacks = 0;
                long ticks = Common.toTicks(1, TimeUnit.SECONDS);
                for (Map.Entry<UUID, Long> entry : stackTime.entrySet()) {
                    int seconds = (int) Math.floor(entry.getValue() / (double) ticks);
                    stacks += seconds;
                    if (stacks == 2) {
                        break;
                    }
                }
                return stacks;
            }


        }
    }
}
