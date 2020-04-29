package com.gmail.andrewandy.ascendency.serverplugin.test;

import com.gmail.andrewandy.ascendency.serverplugin.configuration.YamlConfig;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.DefaultMatchService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick.DraftMatchFactory;
import com.gmail.andrewandy.ascendency.serverplugin.test.util.MockPlayer;

import java.util.Collection;
import java.util.HashSet;

public class MatchMakingServiceTest {

    private static final int maxPlayers = 5;
    private static final int minPlayers = 3;
    private static final DefaultMatchService service =
        new DefaultMatchService(new DraftMatchFactory(new YamlConfig()), new YamlConfig());
    private static Collection<Team> teams = new HashSet<>();
    //TODO Fix
    private static Collection<MockPlayer> players = new HashSet<>();

    static {
        populatePlayers();
        setupTeams();
    }

    public MatchMakingServiceTest() {

    }

    public static void populatePlayers() {
        for (int index = 0; index < minPlayers * maxPlayers; index++) {
            final MockPlayer mockPlayer = new MockPlayer();
            players.add(mockPlayer);
        }
    }

    public static void setupTeams() {
        //Make 2 teams;
        for (int index = 0; index < 3; index++) {
            //Team team = new Team(String.valueOf(index), minPlayers);
            //teams.add(team);
        }
    }
/*
    public void testQueue() {
        for (MatchMakingService.MatchMakingMode mode : MatchMakingService.MatchMakingMode.values()) {
            testQueue(mode);
        }
    }

    public void testQueue(MatchMakingService.MatchMakingMode mode) {
        int val;
        switch (mode) {
            case FASTEST:
                val = 3;
                break;
            case BALANCED:
                val = 4;
                break;
            case OPTIMAL:
                val = 5;
                break;
            default:
                throw new IllegalStateException();
        }
        service.setMatchMakingMode(mode);
        for (int index = 0; index < val - 1; index++) {
            assert players.iterator().hasNext();
            Player player = players.iterator().next();
            service.addToQueueAndTryMatch(player); //Try match should fail.
            Assertions.assertEquals(service.getQueueSize(), index);
            Assertions.assertFalse(SimplePlayerMatchManager.INSTANCE.getMatchOf(player.getUniqueId()).isPresent());
        }
        assert players.iterator().hasNext();
        Player player = players.iterator().next();
        service.addToQueue(player);
        service.tryMatch();
        Assertions.assertTrue(SimplePlayerMatchManager.INSTANCE.getMatchOf(player.getUniqueId()).isPresent());
    }
*/

}
