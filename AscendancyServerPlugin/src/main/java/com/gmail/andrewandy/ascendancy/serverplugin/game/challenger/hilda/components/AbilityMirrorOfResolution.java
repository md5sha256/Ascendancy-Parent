package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.components;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.Mirror;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.MirrorData;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyReleasedEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.util.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AbilityMirrorOfResolution extends AbstractCooldownAbility {

    private static final int MAX_CHARGES = 3;
    private static final int DISTANCE = 10;
    private static final int WIDTH = 5;
    private static final ParticleEffect NORMAL_EDGE = ParticleEffect.builder().type(ParticleTypes.EXPLOSION).build();
    private static final ParticleEffect PENDING_EDGE = ParticleEffect.builder().type(ParticleTypes.WITCH_SPELL).build();
    private static final long CHARGES_COOLDOWN = Common.toTicks(10, TimeUnit.SECONDS);
    private final AscendancyServerPlugin plugin;
    private final Map<UUID, MirrorData> mirrorMap = new HashMap<>();
    private final Map<UUID, Mirror> pendingMirrors = new HashMap<>();
    private final Map<UUID, Integer> ticksPassedSinceCharge = new HashMap<>();

    @AssistedInject
    AbilityMirrorOfResolution(@Assisted final Challenger challenger, final AscendancyServerPlugin plugin) {
        super("Mirror Of Resolution", true, 9, TimeUnit.SECONDS, challenger);
        this.plugin = plugin;
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

    @Listener(order = Order.LAST)
    public void onKeyReleased(@NotNull final ActiveKeyReleasedEvent event) {
        final Player player = event.getPlayer();
        if (!isRegistered(player.getUniqueId())) {
            return;
        }
        final MirrorData data = mirrorMap.get(player.getUniqueId());
        data.useCharge();
        final Mirror pending = pendingMirrors.remove(player.getUniqueId());
        pending.setParticleEffect(NORMAL_EDGE);
    }

    private void showMirrorPreview(@NotNull Player player) { // X = roll, Y = yaw, Z = pitch
        Vector3d rotation = player.getHeadRotation();
        final double distanceToProject = Math.sqrt(DISTANCE * DISTANCE * 3) / 3D;
        rotation.project(distanceToProject, distanceToProject, distanceToProject);
        double xOffset = (rotation.getX() < 0 ? -WIDTH : WIDTH) / 2D;
        final Vector3d primary = rotation.add(xOffset, 0, 0);
        final Mirror mirror = new Mirror(PENDING_EDGE, player.getWorld(), primary, WIDTH);
        final AABB boundingBox = mirror.getBoundingBox();
        for (Mirror active : mirrorMap.get(player.getUniqueId())) {
            if (boundingBox.intersects(active.getBoundingBox())) {
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
                // Only render the mirror to the player who's about to cast it.
                Sponge.getServer().getPlayer(caster).ifPresent(mirror::render);
            } else {
                // Render the mirror to all players
                mirror.render();
            }
        }).submit(plugin);
    }

}
