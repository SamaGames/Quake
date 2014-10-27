package com.Geekpower14.Quake.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.Geekpower14.Quake.Quake;
import com.Geekpower14.Quake.Arena.Arena;

public class AddSpawnCommand implements BasicCommand{
	
	private Quake plugin;

	/**
	 * @param pl
	 */
	public AddSpawnCommand(Quake pl)
	{
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args) {
		
		if(Quake.hasPermission(player, this.getPermission()))
		{
			Arena arena = null;
			if(plugin.am.exist(args[0]))
			{
				arena = plugin.am.getArena(args[0]);
			}
			if(arena == null)
			{
				player.sendMessage(ChatColor.RED + "Veuillez écrire un nom d'arène correct.");
				return true;
			}
			
			arena.addSpawn(player.getLocation());
					
			arena.saveConfig();
			player.sendMessage(ChatColor.YELLOW + "Spawn aouté avec succés");
			
		}else
		{
			player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
		}
		
		return true;		
	}

	@Override
	public String help(Player p) {
		if(Quake.hasPermission(p, this.getPermission()))
		{
			return "/q setspawn [Arena] - Add spawn to an arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "Quake.edit";
	}

}
