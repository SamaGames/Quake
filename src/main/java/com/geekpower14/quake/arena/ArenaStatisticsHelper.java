package com.geekpower14.quake.arena;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IGameStatisticsHelper;

import java.util.UUID;

public class ArenaStatisticsHelper implements IGameStatisticsHelper
{
    @Override
    public void increasePlayedTime(UUID uuid, long playedTime)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getQuakeStatistics().incrByPlayedTime(playedTime);
    }

    @Override
    public void increasePlayedGames(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getQuakeStatistics().incrByPlayedGames(1);
    }

    @Override
    public void increaseWins(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getQuakeStatistics().incrByWins(1);
    }

    public void increaseKills(UUID uuid, int kills)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getQuakeStatistics().incrByKills(kills);
    }

    public void increaseDeaths(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getQuakeStatistics().incrByDeaths(1);
    }
}
