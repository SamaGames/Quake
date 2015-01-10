package com.Geekpower14.quake.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.Arena;

public class SaveCommand implements BasicCommand{
	
	private Quake plugin;

	public SaveCommand(Quake pl)
	{
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args) {
		
		if(Quake.hasPermission(player, this.getPermission()))
		{
			Arena arena = null;
			if(plugin.arenaManager.exist(args[0]))
			{
				arena = plugin.arenaManager.getArena(args[0]);
			}
			if(arena == null)
			{
				player.sendMessage(ChatColor.RED + "Veuillez écrire un nom d'arène correct.");
				return true;
			}
			
			arena.saveConfig();
			
			player.sendMessage(ChatColor.GREEN + "config saved for the arena : " + args[0]);
			
			
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
			return "/q save [arena] - Save config of the arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "quake.edit";
	}

}
