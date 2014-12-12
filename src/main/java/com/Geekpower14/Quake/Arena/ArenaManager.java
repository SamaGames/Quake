package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import net.samagames.gameapi.GameAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
		File folder = new File(plugin.getDataFolder(), "../../world/arenas/");
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
        	
        	addArena(mapname);
        }
        
	}

	public void addArena(String name) {
		if (name == null) {
			return;
		}

		Arena arena = new Arena(plugin, name);

		GameAPI.registerArena(arena);
		ARENA = arena;
	}

	@Deprecated
	public void removeArena(String name)
	{
		Arena aren = getArena(name);
		
		aren.stop();

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

	@Deprecated
	public void deleteArena(String name)
	{
		Arena aren = getArena(name);
		
		aren.stop();
		
		File file = new File(plugin.getDataFolder(), "../../world/arenas/"+ aren.getName() +".yml");
		
		file.delete();

		ARENA = null;
	}
	
	public boolean exist(String name)
	{
		if(ARENA != null)
			return true;

		return false;
	}
	
	public void disable()
	{
		ARENA.disable();
	}

	public String getArenaName()
	{
		return ARENA.getName();
	}

	public Arena getArenabyPlayer(Player p)
	{
		return getArenabyPlayer(p.getName());
	}
	
	public Arena getArenabyPlayer(String p)
	{
		if(ARENA.hasPlayer(p))
		{
			return ARENA;
		}
		
		return null;
	}

	public Arena getArenaByUUID(UUID uuid)
	{
		if(ARENA.getUUID().equals(uuid))
		{
			return ARENA;
		}
		return null;
	}
}
