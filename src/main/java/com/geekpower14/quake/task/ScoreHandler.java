package com.geekpower14.quake.task;

import com.geekpower14.quake.Quake;
import com.geekpower14.quake.arena.APlayer;
import com.geekpower14.quake.arena.ArenaSolo;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreHandler implements Runnable{

    public Quake plugin;

    public ArenaSolo arena;

    public BukkitTask bt;
    public boolean needToUpdate = true;

    public List<APlayer> players = new ArrayList<>();

    public ScoreHandler(Quake plugin, ArenaSolo aren)
    {
        this.plugin = plugin;
        this.arena = aren;
        players.addAll(aren.getAPlayersList());

        bt = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0L, 1L);
    }

    public void addPlayer(APlayer aPlayer)
    {
        players.add(aPlayer);
    }

    public void removePlayer(APlayer aPlayer)
    {
        players.remove(aPlayer);
    }

    public void stop()
    {
        bt.cancel();
    }

    public void requestUpdate()
    {
        needToUpdate = true;
    }

    public void setNeedUpdate(boolean t)
    {
        needToUpdate = t;
    }

    public boolean needToUpdate()
    {
        return needToUpdate;
    }

    @Override
    public void run() {

        if(needToUpdate)
        {
            //plugin.log.info("LOOLL");
            needToUpdate = false;

            Collections.sort(players, (o1, o2) -> -Integer.compare(o1.getScore(), o2.getScore()));

            try{
                Bukkit.getScheduler().runTask(plugin, () -> arena.getAPlayersList().forEach(APlayer::updateScoreboard));
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public List<APlayer> getSortedList()
    {
        return players;
    }
}
