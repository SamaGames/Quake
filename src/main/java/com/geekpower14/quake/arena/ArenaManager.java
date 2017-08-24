package com.geekpower14.quake.arena;

import com.geekpower14.quake.Quake;
import org.bukkit.entity.Player;

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
public class ArenaManager {
	
	private Quake plugin;

	private Arena ARENA;

	public ArenaManager(Quake pl)
	{
		plugin = pl;
		
		loadArenas();
	}
	
	public void loadArenas()
	{
		addArena();
		/*File folder = new File(plugin.getDataFolder(), "../../world/arenas/");
        if(!folder.exists())
            folder.mkdir();
        
        List<String> Maps = new ArrayList<String>();
        
        int zl = 0;
        File afile[];
        if(folder.listFiles() == null)
        	return;
        int k = (afile = folder.listFiles()).length;
        for(int j = 0; j < k; j++)
        {
            File f = afile[j];
            
            String name = f.getName().replaceAll(".yml", "");
            Maps.add(name);
            plugin.log.info("Found arena : " + name);
            zl++;
        }
        if(zl == 0 || Maps.get(0) == null)
        {
        	plugin.log.info(ChatColor.RED + "No arena found in folder ");
            return;
        }
        
        plugin.log.info(Maps.size() + " SIZE");
        for(String mapname : Maps)
        {
        	plugin.log.info(ChatColor.GREEN + "arena " + mapname);
        	
        }*/
        
	}

	public void addArena() {
		Arena arena;
		if(plugin.getType().equals("team"))
		{
			arena = new ArenaTeam(plugin);
		}else{
			arena = new ArenaSolo(plugin);
		}

		plugin.getSamaGamesAPI().getGameManager().registerGame(arena);
		ARENA = arena;
	}

	@Deprecated
	public void removeArena(String name)
	{
		Arena aren = getArena(name);
		
		aren.handleGameEnd();

		ARENA = null;
	}

	public Arena getArena()
	{
		return ARENA;
	}
	
	public Arena getArena(String name)
	{
		return ARENA;
	}
	
	public boolean exist(String name)
	{
		return ARENA != null;
	}
	
	public void disable()
	{
		ARENA.disable();
	}

	public Arena getArenabyPlayer(Player p)
	{
		if(ARENA.hasPlayer(p))
		{
			return ARENA;
		}

		return null;
	}
}
