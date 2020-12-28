package com.gmail.andrewandy.ascendancy.serverplugin.api.event;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

public class GamePlayerDamageEvent extends GameEvent {

    private final Team victimTeam;
    private final Entity victim;
    private double damage;

    public GamePlayerDamageEvent(final Player target, final Team targetTeam, final Entity victim,
                                 final Team victimTeam, final double damage) {
        super(target, targetTeam);
        this.victim = victim;
        this.victimTeam = victimTeam;
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(final double damage) {
        this.damage = damage;
    }

    public Entity getVictim() {
        return victim;
    }

    public Team getVictimTeam() {
        return victimTeam;
    }

}
