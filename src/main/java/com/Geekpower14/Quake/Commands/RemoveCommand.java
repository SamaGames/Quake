package com.Geekpower14.Quake.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.Geekpower14.Quake.Quake;
import com.Geekpower14.Quake.Arena.Arena;

public class RemoveCommand implements BasicCommand{
	
	private Quake plugin;

	public RemoveCommand(Quake pl)
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
				
				if(args.length != 1)
				{
					player.sendMessage(ChatColor.RED + "Please type a number !");
					return true;
				}

				plugin.am.deleteArena(arena.getName());
				player.sendMessage(ChatColor.GREEN + "Arena supprimé avec succés.");
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
			return "/q remove [Arena name] - Remove an arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "Quake.edit";
	}

}
