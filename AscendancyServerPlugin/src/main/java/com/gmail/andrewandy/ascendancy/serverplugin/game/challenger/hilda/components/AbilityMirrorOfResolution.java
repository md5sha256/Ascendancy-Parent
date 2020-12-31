package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.components;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.Mirror;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.MirrorData;
import com.gmail.andrewandy.ascendancy.serverplugin.items.spell.ISpellEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyReleasedEvent;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AbilityMirrorOfResolution extends AbstractCooldownAbility {

    private static final int MAX_CHARGES = 3;
    private static final int DISTANCE = 10;
    private static final int WIDTH = 5;
    private static final ParticleEffect NORMAL_EDGE = ParticleEffect.builder().type(ParticleTypes.EXPLOSION).build();
    private static final ParticleEffect HOSTILE_EDGE = ParticleEffect.builder().type(ParticleTypes.REDSTONE_DUST).build();
    private static final ParticleEffect PENDING_EDGE = ParticleEffect.builder().type(ParticleTypes.WITCH_SPELL).build();
    private static final long CHARGES_COOLDOWN = Common.toTicks(10, TimeUnit.SECONDS);
    private final AscendancyServerPlugin plugin;
    private final Map<UUID, MirrorData> mirrorMap = new HashMap<>();
    private final Map<UUID, Mirror> pendingMirrors = new HashMap<>();
    private final Map<UUID, Integer> ticksPassedSinceCharge = new HashMap<>();

    @Inject
    private ISpellEngine spellEngine;

    @Inject
    private PlayerMatchManager matchManager;

    @AssistedInject
    AbilityMirrorOfResolution(@Assisted final Challenger challenger, final AscendancyServerPlugin plugin) {
        super("Mirror Of Resolution", true, 9, TimeUnit.SECONDS, challenger);
        this.plugin = plugin;
    }

    public @NotNull MirrorData getMirrorDataFor(@NotNull UUID player) {
        if (!isRegistered(player)) {
            throw new IllegalArgumentException("Player: " + player + " is not registered!");
        }
        return mirrorMap.get(player);
    }

    public @NotNull Optional<@NotNull Mirror> getMirror(@NotNull Location<World> location) {
        final Vector3d pos = location.getPosition();
        for (MirrorData data : mirrorMap.values()) {
            for (Mirror mirror : data) {
                if (mirror.getWorld() == location.getExtent() && mirror.getBoundingBox().contains(pos)) {
                    return Optional.of(mirror);
                }
            }
        }
        return Optional.empty();
    }

    public @NotNull Set<@NotNull MirrorData> getAllMirrorData() {
        return new HashSet<>(mirrorMap.values());
    }

    @Override
    public void tick() {
        for (Map.Entry<UUID, Integer> entry : ticksPassedSinceCharge.entrySet()) {
            entry.setValue(entry.getValue() + 1);
            if (entry.getValue() >= CHARGES_COOLDOWN) {
                final MirrorData data = mirrorMap.get(entry.getKey());
                assert data != null;
                if (data.getAllocatedCharges() < MAX_CHARGES) {
                    data.allocateCharge();
                    entry.setValue(0);
                }
            }
        }
    }

    @Override
    public void register(UUID player) {
        mirrorMap.computeIfAbsent(player, MirrorData::new);
        ticksPassedSinceCharge.putIfAbsent(player, 0);
    }

    @Listener(order = Order.DEFAULT)
    public void onKeyPressed(@NotNull final ActiveKeyPressedEvent event) {
        final Player player = event.getPlayer();
        if (!isRegistered(player.getUniqueId())) {
            throw new IllegalArgumentException("Player isn't registered!");
        }
        final MirrorData data = mirrorMap.get(player.getUniqueId());
        assert data != null;
        if (data.getUsedCharges() == MAX_CHARGES) {
            // Cancel if player has run out of charges.
            // FIXME consider sending a title to the player
            return;
        }
        showMirrorPreview(player);
        scheduleMirrorRender(player.getUniqueId(), pendingMirrors.get(player.getUniqueId()));
    }

    private void showMirrorPreview(@NotNull Player player) {
        // X = roll, Y = yaw, Z = pitch
        Vector3d rotation = player.getHeadRotation();
        final double distanceToProject = Math.sqrt(DISTANCE * DISTANCE * 3) / 3D;
        rotation.project(distanceToProject, distanceToProject, distanceToProject);
        double xOffset = (rotation.getX() < 0 ? -WIDTH : WIDTH) / 2D;
        final Vector3d primary = rotation.add(xOffset, 0, 0);
        final Mirror mirror = new Mirror(player.getWorld(), primary, WIDTH);
        final AABB boundingBox = mirror.getBoundingBox();
        for (Mirror active : mirrorMap.get(player.getUniqueId())) {
            if (active.getWorld() == player.getWorld() && boundingBox.intersects(active.getBoundingBox())) {
                // Cancel if a bounding box already exists at the given location
                return;
            }
        }
        pendingMirrors.put(player.getUniqueId(), mirror);
    }

    private void scheduleMirrorRender(@NotNull UUID caster, @NotNull Mirror mirror) {
        final Scheduler scheduler = Sponge.getScheduler();
        scheduler.createTaskBuilder().delay(1, TimeUnit.SECONDS).execute(task -> {
            final MirrorData data = mirrorMap.get(caster);
            // If player was unregistered or if the mirror expires, cancel the render task.
            if (data == null || !data.containsMirror(mirror)) {
                task.cancel();
                return;
            }
            // If mirror is pending
            if (pendingMirrors.get(caster) == mirror) {
                Sponge.getServer().getPlayer(caster).ifPresent(onlinePlayer -> mirror.render(onlinePlayer, PENDING_EDGE));
                // Only render the mirror to the player who's about to cast it.;
            } else {
                renderMirrorGlobally(caster, mirror);
            }
        }).submit(plugin);
    }

    private void renderMirrorGlobally(final UUID owner, final Mirror mirror) {
        final Optional<ManagedMatch> optional = matchManager.getMatchOf(owner);
        assert optional.isPresent();
        final ManagedMatch match = optional.get();
        final Team casterTeam = match.getTeamOf(owner);
        final Server server = Sponge.getServer();
        for (UUID player : match.getPlayers()) {
            final Team team = match.getTeamOf(player);
            final ParticleEffect effect = casterTeam.equals(team) ? NORMAL_EDGE : HOSTILE_EDGE;
            server.getPlayer(player).ifPresent(onlinePlayer -> mirror.render(onlinePlayer, effect));
        }
    }

    private void renderMirror(final UUID owner, final Mirror mirror, final Player... players) {
        if (players == null || players.length == 0) {
            return;
        }
        final Optional<ManagedMatch> optional = matchManager.getMatchOf(owner);
        assert optional.isPresent();
        final ManagedMatch match = optional.get();
        final Team casterTeam = match.getTeamOf(owner);
        final Server server = Sponge.getServer();
        for (Player player : players) {
            if (!match.containsPlayer(player.getUniqueId())) {
                continue;
            }
            final Team team = match.getTeamOf(player.getUniqueId());
            final ParticleEffect effect = casterTeam.equals(team) ? NORMAL_EDGE : HOSTILE_EDGE;
            mirror.render(player, effect);
        }
    }

    private void renderMirror(final UUID owner, final Mirror mirror, final UUID... players) {
        if (players == null || players.length == 0) {
            return;
        }
        final Optional<ManagedMatch> optional = matchManager.getMatchOf(owner);
        assert optional.isPresent();
        final ManagedMatch match = optional.get();
        final Team casterTeam = match.getTeamOf(owner);
        final Server server = Sponge.getServer();
        for (UUID player : players) {
            if (!match.containsPlayer(player)) {
                continue;
            }
            final Team team = match.getTeamOf(player);
            final ParticleEffect effect = casterTeam.equals(team) ? NORMAL_EDGE : HOSTILE_EDGE;
            server.getPlayer(player).ifPresent(onlinePlayer -> mirror.render(onlinePlayer, effect));
        }
    }

    @Listener(order = Order.LAST)
    public void onKeyReleased(@NotNull final ActiveKeyReleasedEvent event) {
        final Player player = event.getPlayer();
        if (!isRegistered(player.getUniqueId())) {
            return;
        }
        final MirrorData data = mirrorMap.get(player.getUniqueId());
        data.useCharge();
        pendingMirrors.remove(player.getUniqueId());
    }

    @Listener(order = Order.DEFAULT)
    public void onProjectileHit(@NotNull final MoveEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        if (!(entity instanceof Projectile)) {
            return;
        }
        final Projectile projectile = (Projectile) entity;
        final Vector3d velocity = projectile.getVelocity();
        final Location<World> location = projectile.getLocation();
        final Optional<Mirror> optionalMirror = getMirror(location);
        if (!optionalMirror.isPresent()) {
            return;
        }
        final Mirror mirror = optionalMirror.get();
        final Vector3d primary = mirror.getPrimary();
        final Vector3d secondary = mirror.getSecondary();
        double x1 = Math.min(primary.getX(), secondary.getX());
        double x2 = x1 == primary.getX() ? secondary.getX() : primary.getX();
        double y1 = Math.min(primary.getY(), secondary.getY());
        double y2 = y1 == primary.getY() ? secondary.getY() : primary.getY();
        double z1 = Math.min(primary.getZ(), secondary.getZ());
        double z2 = z1 == primary.getZ() ? secondary.getZ() : primary.getZ();

        final Vector3d bottomLeftCorner = new Vector3d(x1, y1, z1);
        final Vector3d bottomRightCorner = new Vector3d(x2, y1, z2);
        final Vector3d topLeftCorner = new Vector3d(x1, y2, z1);
        final Vector3d topRightCorner = new Vector3d(x2, y1, z2);
        final Vector3d horizontal = bottomRightCorner.sub(bottomLeftCorner);
        final Vector3d vertical = topRightCorner.sub(topLeftCorner);
        final Vector3d normal = horizontal.cross(vertical);
        final double dot = velocity.dot(normal);
        final double magnitude = velocity.length();
        Vector3d newVelocity = normal.mul(magnitude);
        if (dot > 0) {
            newVelocity = newVelocity.negate();
        }
        projectile.setVelocity(newVelocity);
    }

}
