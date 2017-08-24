package com.geekpower14.quake.task;

import com.geekpower14.quake.Quake;
import com.geekpower14.quake.arena.APlayer;
import com.geekpower14.quake.arena.ArenaSolo;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * This file is part of Quake.
 *
 * Quake is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quake is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quake.  If not, see <http://www.gnu.org/licenses/>.
 */
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
