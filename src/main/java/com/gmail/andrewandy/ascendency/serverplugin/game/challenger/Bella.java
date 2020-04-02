package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendency.lib.game.data.IChampionData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChampionDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.MathUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class Bella extends AbstractChallenger {

    private static final Bella instance = new Bella();

    private Bella() {
        super("Bella",
                new Ability[0],
                new PlayerSpecificRune[0],
                Challengers.getLoreOf("Bella"));
    }

    public static Bella getInstance() {
        return instance;
    }

    /**
     * Creates (but does not place) a nether-brick ring.
     * @param centre The centre of the circle.
     * @param radius The radius.
     * @return Returns a Collection of BlockStates which can be placed to create a ring.
     */
    public Collection<BlockState> generateCircleBlocks(Location<World> centre, int radius) {
        Collection<Location<World>> rawCircle = MathUtils.createCircle(centre, radius);
        Collection<BlockState> blockStates = new HashSet<>(rawCircle.size());
        for (Location<World> location : rawCircle) {
            BlockState state = location.getBlock();
            if (state.getType() == BlockTypes.AIR) {
                continue;
            }
            blockStates.add(BlockState.builder().from(state).blockType(BlockTypes.NETHER_BRICK_FENCE).build());
        }
        return blockStates;
    }

    private static class CircletOfTheAccused {

        public void activateAs(UUID uuid) {
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);
            if (!optionalPlayer.isPresent()) {
                throw new IllegalArgumentException("Player does not exist!");
            }
            Player player = optionalPlayer.get();

        }


    }

    @Override
    public IChampionData toData() {
        try {
            return new ChampionDataImpl(getName(), new File("Path to icon"), getLore());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
