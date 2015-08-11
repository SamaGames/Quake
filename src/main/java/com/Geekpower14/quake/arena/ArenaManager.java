package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import org.bukkit.entity.Player;

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
		Arena arena = null;
		if(plugin.type.equals("team"))
		{
			arena = new ArenaTeam(plugin);
		}else{
			arena = new ArenaSolo(plugin);
		}

		plugin.samaGamesAPI.getGameManager().registerGame(arena);
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
		if(ARENA != null)
			return true;

		return false;
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
