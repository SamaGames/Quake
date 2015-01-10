package com.Geekpower14.quake.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.Geekpower14.quake.Quake;

public class CreateCommand implements BasicCommand{
	
	private Quake plugin;

	public CreateCommand(Quake pl)
	{
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args) {
		
			if(Quake.hasPermission(player, this.getPermission()))
			{
				if(args.length != 1)
				{
					player.sendMessage(ChatColor.RED + "Veuillez écrire un nom pour l'arène !");
					return true;
				}
					if(plugin.arenaManager.exist(args[0]))
					{
						player.sendMessage(ChatColor.RED + "arena " + args[0] + " existe déjà !");
						return true;
					}
					
					plugin.arenaManager.addArena(args[0]);
					player.sendMessage(ChatColor.YELLOW + "arena " + args[0] + " créée avec succés");
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
			return "/q create [arena name] - Create an arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "quake.edit";
	}

}
