package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.Mirror;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.MirrorData;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AbilityIdentityOfPurity extends AbstractAbility {

    private static final PotionEffect FRIENDLY_EFFECT, HOSTILE_EFFECT;

    static {
        FRIENDLY_EFFECT = PotionEffect.builder()
                .potionType(PotionEffectTypes.SPEED)
                .amplifier(1)
                .duration((int) Common.toTicks(3, TimeUnit.SECONDS))
                .build();
        HOSTILE_EFFECT = PotionEffect.builder()
                .potionType(PotionEffectTypes.SLOWNESS)
                .amplifier(1)
                .duration((int) Common.toTicks(3, TimeUnit.SECONDS))
                .build();
    }

    @Inject
    private AbilityMirrorOfResolution mirrorOfResolution;

    @Inject
    private PlayerMatchManager matchManager;

    @AssistedInject
    AbilityIdentityOfPurity(@Assisted Challenger challenger) {
        super("Identity of Purity", false, challenger);
    }

    @Listener(order = Order.EARLY)
    public void onEntityMove(final MoveEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        final UUID uuid = entity.getUniqueId();
        MirrorData mirrorData = null;
        Mirror mirror = null;
        for (MirrorData data : mirrorOfResolution.getAllMirrorData()) {
            for (Mirror m : data) {
                if (m.getBoundingBox().contains(event.getFromTransform().getPosition())) {
                    mirror = m;
                    mirrorData = data;
                    break;
                }
            }
        }
        if (mirror == null) {
            return;
        }
        final Optional<ManagedMatch> managedMatch = matchManager.getMatchOf(uuid);
        if (!managedMatch.isPresent()) {
            return;
        }
        final PotionEffectData peData = entity.get(PotionEffectData.class)
                .orElseThrow(() -> new IllegalStateException("Failed to get PotionEffectData for Entity: " + entity.getUniqueId()));
        final ManagedMatch match = managedMatch.get();
        final Team team = match.getTeamOf(uuid);
        final Team casterTeam = match.getTeamOf(mirrorData.getPlayer());
        peData.addElement(team.equals(casterTeam) ? FRIENDLY_EFFECT : HOSTILE_EFFECT);
        entity.offer(peData);
        mirrorData.removeMirror(mirror);
    }

}
