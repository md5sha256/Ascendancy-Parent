package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.api.event.SpellCastEvent;
import am2.api.spell.SpellComponent;
import am2.buffs.BuffEffectAstralDistortion;
import am2.buffs.BuffEffectManaRegen;
import am2.buffs.BuffEffectSilence;
import am2.items.SpellBase;
import am2.spell.component.Heal;
import am2.utils.SpellUtils;
import com.flowpowered.math.vector.Vector3i;
import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerEvent;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractTickableAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.CustomEvents;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyHandler;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.google.inject.Inject;
import javafx.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
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
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Represents the "Bella" Character in ascendancy.
 */
public class Bella extends AbstractChallenger {

    private static final Bella instance = new Bella();
    @Inject private static PlayerMatchManager matchManager;

    private Bella() {
        super("Bella", new Ability[] {CircletOfTheAccused.instance, ReleasedRebellion.instance},
            new PlayerSpecificRune[] {CoupDEclat.instance, DivineCrown.instance,
                ExpandingAgony.instance}, Challengers.getLoreOf("Bella"));
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
    public static Collection<Location<World>> generateCircleBlocks(final Location<World> centre,
        final int radius) {
        final Collection<Location<World>> rawCircle = MathUtils.createCircle(centre, radius);
        final Cause cause =
            Cause.builder().named("Bella", AscendencyServerPlugin.getInstance()).build();
        rawCircle.forEach((location -> location.setBlockType(BlockTypes.AIR, cause)));
        return rawCircle;
    }

    @Override public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to icon"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static class CircletOfTheAccused extends AbstractCooldownAbility {
        private static final CircletOfTheAccused instance = new CircletOfTheAccused();
        private final Map<UUID, CircletData> registeredMap = new HashMap<>();
        private DamageEntityEvent lastDamageEvent;

        private CircletOfTheAccused() {
            super("Circlet Of The Accused", true, 5, TimeUnit.SECONDS);
        }

        public static CircletOfTheAccused getInstance() {
            return instance;
        }


        /**
         * Activates this ability as a certain player.
         *
         * @param targetUID        The UUID to activate as.
         * @param radius           The radius of the circle.
         * @param respectCooldowns Whether we should respect cooldowns.
         */
        public void activateAs(final UUID caster, final UUID targetUID, final int radius,
            final boolean respectCooldowns) {
            final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(targetUID);
            if (!optionalPlayer.isPresent()) {
                throw new IllegalArgumentException("Player does not exist!");
            }
            if (respectCooldowns && isOnCooldown(caster) || registeredMap.containsKey(caster)) {
                return;
            }

            resetCooldown(caster);
            final Player player = optionalPlayer.get();
            final Collection<Entity> nearby = player.getNearbyEntities(
                (entity) -> MathUtils.calculateDistance(entity.getLocation(), player.getLocation())
                    <= 10D);
            Player target = null;
            double leastDistance = Double.MAX_VALUE;
            final Location<World> location = player.getLocation();
            for (final Entity entity : nearby) {
                if (!(entity instanceof Player)) {
                    continue;
                }
                final double distance = MathUtils.calculateDistance(location, entity.getLocation());
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
                    circletData = new CircletData(caster,
                        radius); //Caster is the playerUID, default radius = 4
                }
                circletData.reset();
                circletData.setRingBlocks(generateCircleBlocks(location, radius));
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
        public boolean clearCirclet(final Player player) {
            if (!registeredMap.containsKey(player.getUniqueId())) {
                return false;
            }
            registeredMap.get(player.getUniqueId()).reset();
            registeredMap.remove(player.getUniqueId());
            return true;
        }

        /**
         * Get the CircletData from a specified location.
         * @param location The location to get the circlet for.
         * @return Returns an optional, populated if an circlet exists at a given
         * location checked using {@link CircletData#generateCircleTest()}.
         */
        public Optional<CircletData> getCirecletAt(final Location<World> location) {
            final Location<World> copy = location.copy();
            for (final CircletData circletData : registeredMap.values()) {
                if (circletData.generateCircleTest().test(copy)) {
                    return Optional.of(circletData);
                }
            }
            return Optional.empty();
        }

        @Override public void tick() {
            super.tick();
            registeredMap.forEach((key, data) -> {
                data.incrementTick();
                if (data.getTickCount() >= getCooldownDuration()) {
                    data.reset(); //Clear the ring
                    resetCooldown(key);
                }
                cooldownMap.entrySet().removeIf(ChallengerUtils
                    .mapTickPredicate(9, TimeUnit.SECONDS,
                        (uuid) -> Sponge.getServer().getPlayer(uuid)
                            .ifPresent(this::clearCirclet)));


                final Optional<ManagedMatch> match =
                    SimplePlayerMatchManager.INSTANCE.getMatchOf(key);
                match.ifPresent(managedMatch -> {
                    final Team team = managedMatch.getTeamOf(key);
                    final Collection<Player> players = Common
                        .getEntities(Player.class, CoupDEclat.getInstance().getExtentViewFor(data),
                            (Player player) -> {
                                final Optional<Team> optional = SimplePlayerMatchManager.INSTANCE
                                    .getTeamOf(player.getUniqueId());
                                return optional.isPresent() && optional.get() != team && data
                                    .generateCircleTest().test(player.getLocation());
                            });
                    players.forEach((Player player) -> {
                        final Optional<Team> optionalTeam =
                            SimplePlayerMatchManager.INSTANCE.getTeamOf(key);
                        if (!optionalTeam.isPresent()) {
                            return;
                        }
                        if (optionalTeam.get() == team) { //If allied, skip
                            return;
                        }
                        //TODO Set scoreboard so cmd-block impl knows they are in circle.
                        //Means they are an enemy.
                        final PotionEffectData peData = player.get(PotionEffectData.class)
                            .orElseThrow(() -> new IllegalArgumentException(
                                "Unable to get PotionEffect data for " + player.getName()));

                        peData.addElement(
                            PotionEffect.builder().potionType(PotionEffectTypes.WITHER).duration(1)
                                .amplifier(1).build()) //Wither
                            .addElement((PotionEffect) new BuffEffectAstralDistortion(1,
                                1)); //Astral Distorton == Astral Nullifcation
                        player.offer(peData);
                    });
                });
            });
        }


        @Listener public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
            if (ActiveKeyHandler.INSTANCE
                .isKeyPressed(event.getPlayer())) { //If player was holding the key then skip.
                return;
            }
            final ProcEvent procEvent = new ProcEvent(event.getPlayer(), event.getPlayer(), 4);
            if (procEvent.callEvent()) { //If not cancelled
                final Optional<ManagedMatch> match =
                    matchManager.getMatchOf(event.getPlayer().getUniqueId());
                if (!match.isPresent()) {
                    return;
                }
                final Player target = procEvent.getTarget();
                activateAs(procEvent.getInvoker().getUniqueId(), target.getUniqueId(),
                    procEvent.circletRadius, true);
            }
        }

        @SubscribeEvent public void onSpellCast(final SpellCastEvent.Post event) {
            final ItemStack spell = event.spell;
            final int id = SpellBase.getIdFromItem(spell.getItem());
            final Item spellBase = SpellBase.REGISTRY.getObjectById(id);
            if (!(spellBase instanceof SpellBase)) {
                return;
            }
            System.out.println(spellBase.getClass().toString());
            final List<SpellComponent> component = SpellUtils.getComponentsForStage(spell, 1);
            boolean contains = false;
            for (final SpellComponent c : component) {
                if (c instanceof Heal) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return;
            }

        }

        @Listener public void onFatalDamage(final DamageEntityEvent event) {
            if (event == lastDamageEvent) {
                return;
            }
            final Entity entity = event.getTargetEntity();
            if (!event.willCauseDeath()) {
                return;
            }
            registeredMap.values().stream()
                .filter(circletData -> circletData.generateCircleTest().test(entity.getLocation()))
                .findAny().ifPresent((circletData -> {

                event.setCancelled(true);
                final Cause cause = Cause.builder().named("Source", circletData.caster).build();
                lastDamageEvent = SpongeEventFactory
                    .createDamageEntityEvent(cause, event.getOriginalFunctions(), entity,
                        event.getOriginalDamage());
                Sponge.getEventManager().post(lastDamageEvent);
                entity.damage(event.getFinalDamage(),
                    DamageSource.builder().from(DamageSources.GENERIC).build());
            }));
        }

        @Listener(order = Order.DEFAULT)
        public void onJump(final CustomEvents.PlayerJumpEvent event) {
            final Player player = event.getPlayer();
            final Optional<CircletData> optionalCirclet = getCirecletAt(player.getLocation());
            if (optionalCirclet.isPresent() && !optionalCirclet.get().caster
                .equals(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        /**
         * Represents when {@link CircletOfTheAccused} is activated.
         */
        private static class ProcEvent extends AscendencyServerEvent implements Cancellable {

            private final Cause cause;
            private final Player invoker;
            private boolean cancel;
            private int circletRadius;
            private Player target;

            ProcEvent(final Player invoker, final Player target, final int circletRadius) {
                this.cause = Cause.builder().named("Bella", invoker).build();
                this.invoker = invoker;
                this.target = target;
                if (circletRadius < 1) {
                    throw new IllegalArgumentException("Circle radius must be greater than 0");
                }
                this.circletRadius = circletRadius;
            }

            @Override public boolean isCancelled() {
                return cancel;
            }

            @Override public void setCancelled(final boolean cancel) {
                this.cancel = cancel;
            }

            public void setCircletRadius(final int radius) {
                if (circletRadius < 1) {
                    throw new IllegalArgumentException("Circle radius must be greater than 0");
                }
                this.circletRadius = radius;
            }

            @Override public Cause getCause() {
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


        /**
         * Class used to represent data regarding a circlet being cast.
         */
        private static class CircletData {

            private UUID caster;
            private final int radius;
            private long tickCount = 0;
            private Location<World> ringCenter;
            private Collection<Location<World>> ringBlocks;

            public CircletData(final UUID caster, final int radius) {
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

            public void setCaster(final UUID caster) {
                this.caster = caster;
            }

            public void incrementTick() {
                this.tickCount++;
            }

            public Collection<Location<World>> getRingBlocks() {
                return ringBlocks == null ? new HashSet<>() : new HashSet<>(ringBlocks);
            }

            public void setRingBlocks(final Collection<Location<World>> ringBlocks) {
                this.ringBlocks = ringBlocks;
            }

            public Location<World> getRingCenter() {
                return ringCenter;
            }

            public void setRingCenter(final Location<World> ringCenter) {
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
                final Cause cause =
                    Cause.builder().named("Bella", AscendencyServerPlugin.getInstance()).build();
                ringBlocks.forEach(location -> location.setBlockType(BlockTypes.AIR, cause));
                this.ringBlocks = null;
            }
        }
    }


    public static class ReleasedRebellion extends AbstractTickableAbility {

        private static final ReleasedRebellion instance = new ReleasedRebellion();
        private final Map<UUID, Pair<UUID, Long>> tickMap = new HashMap<>();

        private ReleasedRebellion() {
            super("Released Rebellion", false);
        }

        public static ReleasedRebellion getInstance() {
            return instance;
        }


        @Listener public void onPlayerAttack(final DamageEntityEvent event) {
            final Entity target = event.getTargetEntity();
            final Optional<Player> optionalPlayer =
                event.getCause().get(DamageEntityEvent.CREATOR, UUID.class)
                    .flatMap(Sponge.getServer()::getPlayer);
            if (!optionalPlayer.isPresent()) {
                return;
            }
            final Player player = optionalPlayer.get();
            if (!isRegistered(player.getUniqueId())) {
                return;
            }
            tickMap.compute(player.getUniqueId(),
                ((uuid, pair) -> new Pair<>(target.getUniqueId(), 0L)));
        }

        @Override public void tick() {
            final long ticks = Common.toTicks(3, TimeUnit.SECONDS);
            tickMap.entrySet().removeIf(entry -> {
                final Pair<UUID, Long> pair = entry.getValue();
                final long val = pair.getValue() + 1;
                entry.setValue(new Pair<>(pair.getKey(), val));
                return val >= ticks;
            });
        }

        @Listener(order = Order.LAST)
        public void onProc(final CircletOfTheAccused.ProcEvent event) { //Handles the proc event
            final Player invoker = event.getInvoker();
            if (!tickMap.containsKey(invoker.getUniqueId())) {
                return;
            }
            final Optional<Player> optionalPlayer =
                Sponge.getServer().getPlayer(tickMap.get(invoker.getUniqueId()).getKey());
            optionalPlayer.ifPresent(event::setTarget);
        }
    }


    public static class CoupDEclat extends AbstractRune {

        private static final CoupDEclat instance = new CoupDEclat();
        private final Collection<UUID> active = new HashSet<>();
        private final Map<UUID, StackData> stackCount = new HashMap<>();

        public static CoupDEclat getInstance() {
            return instance;
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
         * for Bella's AOE, please use {@link CircletOfTheAccused.CircletData#generateCircleTest()}
         * to filter entities and whatnot in the extent.
         *
         * @param circletData The data to create an extent view from.
         * @return Returns a section (extent) of the world where the ring/spherical region is.
         */
        public Extent getExtentViewFor(final CircletOfTheAccused.CircletData circletData) {
            final Location<World> location = circletData.getRingCenter();
            final double radius = circletData.getRadius();
            final Vector3i bottom = new Vector3i(location.getX() + radius, location.getY() - radius,
                location.getZ() - radius), top;
            top = new Vector3i(location.getX() - radius, location.getY() + radius,
                location.getZ() + radius);
            return location.getExtent().getExtentView(top, bottom);
        }

        @Override public void tick() {
            final Map<UUID, Long> map = CircletOfTheAccused.getInstance().getCooldowns();
            //Loop through all known circlets to update effects.
            for (final CircletOfTheAccused.CircletData data : CircletOfTheAccused
                .getInstance().registeredMap.values()) {
                Optional<Team> optional =
                    SimplePlayerMatchManager.INSTANCE.getTeamOf(data.getCaster());
                if (!optional.isPresent()) {
                    return;
                }
                final Team team = optional.get();
                final Collection<Player> players = Common
                    .getEntities(Player.class, getExtentViewFor(data),
                        (player -> data.generateCircleTest().test(player.getLocation())));
                int stacks = 0;
                for (final Player player : players) { //Loop through all nearby entities.
                    optional = SimplePlayerMatchManager.INSTANCE.getTeamOf(player.getUniqueId());
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
                final Optional<Player> optionalPlayer =
                    Sponge.getServer().getPlayer(data.getCaster());
                //Give players absorption
                optionalPlayer.ifPresent(
                    (Player player) -> Sponge.getScheduler().createTaskBuilder().execute(() -> {
                        final PotionEffectData peData = player.get(PotionEffectData.class)
                            .orElseThrow(() -> new IllegalStateException(
                                "Unable to get potiond data for " + player.getName()));
                        peData.addElement(
                            PotionEffect.builder().potionType(PotionEffectTypes.ABSORPTION)
                                .amplifier(1).build());
                        player.offer(peData);
                    }).submit(AscendencyServerPlugin.getInstance()));
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
            CircletOfTheAccused.getInstance().registeredMap.values().forEach(circletData -> {
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
                        MathUtils.calculateDistance(current, circletData.getRingCenter());
                    if (Math.abs(distanceToRadius - circletData.radius) <= 1) { //If on border edge
                        ChallengerUtils.teleportPlayer(player, 1); //Teleport 1 block forward bella.
                    }
                    return;
                }
                if (!inCircle) {
                    stackData.removePlayer(
                        entity.getUniqueId()); //Remove if player is no longer in the circle.
                    return;
                }
                stackData.addPlayer(
                    entity.getUniqueId()); //Add to stack data, will be ticked on next tick?
            });
        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override public DataContainer toContainer() {
            return null;
        }

        private static class StackData {

            private final Map<UUID, Long> stackTime = new HashMap<>();

            public void addPlayer(final UUID player) {
                if (stackTime.containsKey(player)) {
                    return;
                }
                stackTime.put(player, 0L);
            }

            public void removePlayer(final UUID player) {
                stackTime.remove(player);
            }

            public long getTickCount(final UUID player) {
                final Long val = stackTime.get(player);
                return val == null ? 0L : val;
            }


            public void tick() {
                stackTime.entrySet().forEach((entry -> entry.setValue(entry.getValue() + 1)));
            }

            public int calculateStacks() {
                int stacks = 0;
                final long ticks = Common.toTicks(1, TimeUnit.SECONDS);
                for (final Map.Entry<UUID, Long> entry : stackTime.entrySet()) {
                    final int seconds = (int) Math.floor(entry.getValue() / (double) ticks);
                    stacks += seconds;
                    if (stacks == 2) {
                        break;
                    }
                }
                return stacks;
            }


        }
    }


    public static class DivineCrown extends AbstractRune {

        private static final DivineCrown instance = new DivineCrown();
        private final Collection<UUID> casters = new HashSet<>();

        private DivineCrown() {
        }

        public static DivineCrown getInstance() {
            return instance;
        }

        @Override public void applyTo(final Player player) {
            clearFrom(player);
            casters.add(player.getUniqueId());
        }

        @Override public void clearFrom(final Player player) {
            casters.remove(player.getUniqueId());
        }

        @Override public String getName() {
            return "Divine Crown";
        }

        @Override public void tick() {
            for (final UUID uuid : casters) {
                final CircletOfTheAccused.CircletData data =
                    CircletOfTheAccused.instance.registeredMap.get(uuid);
                if (data == null) {
                    continue;
                }
                final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);
                if (!optionalPlayer.isPresent()) {
                    continue;
                }
                final Player player = optionalPlayer.get();
                final Optional<Team> optionalTeam =
                    SimplePlayerMatchManager.INSTANCE.getTeamOf(player.getUniqueId());
                if (!optionalTeam.isPresent()) {
                    return;
                }
                final Team team = optionalTeam.get();
                final Collection<Player> players = Common
                    .getEntities(Player.class, CoupDEclat.instance.getExtentViewFor(data),
                        (Player p) -> {
                            final Optional<Team> optional =
                                SimplePlayerMatchManager.INSTANCE.getTeamOf(p.getUniqueId());
                            return optional.isPresent() && optional.get() == team && data
                                .generateCircleTest()
                                .test(p.getLocation()); //If in circle and ifallied
                        }); //Get players in circle
                for (final Player p : players) {
                    final PotionEffectData peData = p.get(PotionEffectData.class).orElseThrow(
                        () -> new IllegalStateException(
                            "Unable to get potion effect data for " + p.getName()));
                    peData.asList().removeIf(
                        ChallengerUtils::isEffectNegative); //Remove all negative effects as per ChallengerUtils implementation.
                    peData.addElement(
                        PotionEffect.builder().potionType(PotionEffectTypes.SPEED).duration(1)
                            .amplifier(2).build()) //Speed 2
                        .addElement((PotionEffect) new BuffEffectManaRegen(1,
                            2)) // Mana Regen 2 | Safe cast because of sponge's runtime "mixins"
                        .addElement(
                            PotionEffect.builder().potionType(PotionEffectTypes.REGENERATION)
                                .amplifier(1).build()) // Regen 1
                        .addElement(PotionEffect.builder().potionType(PotionEffectTypes.STRENGTH)
                            .amplifier(1).build()) // Strength 1
                        .addElement(PotionEffect.builder().potionType(PotionEffectTypes.RESISTANCE)
                            .amplifier(1).build()); //Resistance 1
                    p.offer(peData); //Give the player potion effects.
                }
            }
        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override public DataContainer toContainer() {
            return null;
        }
    }


    public static class ExpandingAgony extends AbstractRune {

        private static final ExpandingAgony instance = new ExpandingAgony();
        private final Collection<UUID> registered = new HashSet<>();

        private ExpandingAgony() {
        }

        public static ExpandingAgony getInstance() {
            return instance;
        }

        @Override public void applyTo(final Player player) {
            clearFrom(player);
            this.registered.add(player.getUniqueId());
        }

        @Override public void clearFrom(final Player player) {
            registered.remove(player.getUniqueId());
        }

        @Override public String getName() {
            return "Expanding Agony";
        }

        @Override public void tick() {
            for (final UUID uuid : registered) {
                final CircletOfTheAccused.CircletData data =
                    CircletOfTheAccused.instance.registeredMap.get(uuid);
                if (data == null) {
                    continue;
                }
                final Optional<Team> optionalTeam =
                    SimplePlayerMatchManager.INSTANCE.getTeamOf(uuid);
                if (!optionalTeam.isPresent()) {
                    return;
                }
                final Team team = optionalTeam.get();
                final Collection<Player> players = Common
                    .getEntities(Player.class, CoupDEclat.instance.getExtentViewFor(data),
                        (Player p) -> {
                            final Optional<Team> optional =
                                SimplePlayerMatchManager.INSTANCE.getTeamOf(p.getUniqueId());
                            return optional.isPresent() && optional.get() != team && data
                                .generateCircleTest().test(p.getLocation());
                        });
                final int size = players.size();
                players.forEach((Player player) -> {
                    if (size >= 2) { //If 2 enemies are in the circle
                        final PotionEffectData peData = player.get(PotionEffectData.class)
                            .orElseThrow(() -> new IllegalStateException(
                                "Unable to get potion effect data for " + player.getName()));
                        peData.addElement((PotionEffect) new BuffEffectSilence(1,
                            1)); //Silence 1 | Safe cast as per sponge "mixins".
                        player.offer(peData); //Update the player.
                    }
                    player.offer(Keys.FIRE_TICKS, 1); //Give them 1 tick of fire.
                });
            }
        }

        @Listener public void onProc(final CircletOfTheAccused.ProcEvent event) {
            if (registered.contains(event.getInvoker().getUniqueId())) {
                event.setCircletRadius(6); //Change circle radius to 6.
            }
        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override public DataContainer toContainer() {
            return null;
        }
    }
}
