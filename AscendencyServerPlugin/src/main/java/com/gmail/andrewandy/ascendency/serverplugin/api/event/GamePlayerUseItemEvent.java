package com.gmail.andrewandy.ascendency.serverplugin.api.event;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

public class GamePlayerUseItemEvent extends GameEvent {

    private final Team victimTeam;
    private final Entity victim;
    private ItemStack itemStack;

    public GamePlayerUseItemEvent(Player target, Team targetTeam, Entity victim, Team victimTeam, ItemStack used) {
        super(target, targetTeam);
        this.victim = victim;
        this.victimTeam = victimTeam;
        this.itemStack = used.copy();
    }


    public Team getVictimTeam() {
        return victimTeam;
    }


    public Entity getVictim() {
        return victim;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
