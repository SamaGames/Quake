package com.Geekpower14.quake.task;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class ScoreHandler implements Runnable{

    public Quake plugin;

    public Arena arena;

    public BukkitTask bt;

    public boolean running = true;

    public boolean needToUpdate = true;

    public ScoreHandler(Quake plugin, Arena aren)
    {
        this.plugin = plugin;
        this.arena = aren;
        bt = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0L, 1L);
    }

    public void stop()
    {
        running = false;
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

            arena.sortPlayers();

            try{
                arena.refreshScoreBoards();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
