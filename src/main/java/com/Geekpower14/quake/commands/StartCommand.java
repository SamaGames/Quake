package com.Geekpower14.quake.commands;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class StartCommand implements BasicCommand{
	
	private Quake plugin;

	public StartCommand(Quake pl)
	{
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args) {
		
		if(Quake.hasPermission(player, this.getPermission()))
		{
			Arena arena = plugin.getArenaManager().getArenabyPlayer(player);
			if(arena == null)
			{
				player.sendMessage(ChatColor.RED + "Vous n'êtes pas dans une arène!");
				return true;
			}
			
			arena.startGame();
			
			player.sendMessage(ChatColor.GREEN + "Force beginning for the arena");
			
			
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
			return "/q startGame [arena] - Force startGame an arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "quake.modo";
	}

}