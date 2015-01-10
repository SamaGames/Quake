package com.Geekpower14.quake.task;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.APlayer;
import com.Geekpower14.quake.arena.ArenaSolo;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Comparator;

public class ScoreHandler implements Runnable{

    public Quake plugin;

    public ArenaSolo arena;

    public BukkitTask bt;

    public boolean running = true;

    public boolean needToUpdate = true;

    public ScoreHandler(Quake plugin, ArenaSolo aren)
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

            Collections.sort(arena.getAPlayersList(), new Comparator<APlayer>() {
                @Override
                public int compare(APlayer o1, APlayer o2) {
                    return -Integer.compare(o1.getScore(), o2.getScore());
                }
            });

            try{
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (APlayer player : arena.getAPlayersList()) {
                            player.updateScoreboard();
                        }
                    }
                });
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
