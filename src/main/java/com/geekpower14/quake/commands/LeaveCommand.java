package com.geekpower14.quake.commands;

import com.geekpower14.quake.Quake;
import com.geekpower14.quake.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class LeaveCommand implements BasicCommand{
	
	private Quake plugin;

	public LeaveCommand(Quake pl)
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
				player.sendMessage(ChatColor.RED + "Vous n'êtes pas en jeux.");
				return true;
			}
			player.kickPlayer("");
		}else
		{
			player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
		}
		
		return false;
	}

	@Override
	public String help(Player p) {
		if(Quake.hasPermission(p, this.getPermission()))
		{
			return "/q leave - Leave an arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "quake.player";
	}

}
