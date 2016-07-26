package com.geekpower14.quake.commands;

import com.geekpower14.quake.Quake;
import com.geekpower14.quake.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddSpawnCommand implements BasicCommand{
	
	private Quake plugin;

	public AddSpawnCommand(Quake pl)
	{
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args) {
		
		if(Quake.hasPermission(player, this.getPermission()))
		{
			Arena arena = null;
			if(plugin.getArenaManager().exist(args[0]))
			{
				arena = plugin.getArenaManager().getArena(args[0]);
			}
			if(arena == null)
			{
				player.sendMessage(ChatColor.RED + "Veuillez écrire un nom d'arène correct.");
				return true;
			}
			
			arena.addSpawn(player.getLocation());

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
			return "/q setspawn [arena] - Add spawn to an arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "quake.edit";
	}

}
