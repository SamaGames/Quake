package com.Geekpower14.Quake.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.Geekpower14.Quake.Quake;
import com.Geekpower14.Quake.Arena.Arena;


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
			Arena arena = plugin.am.getArenabyPlayer(player);
			if(arena == null)
			{
				player.sendMessage(ChatColor.RED + "Vous n'êtes pas en jeux.");
				return true;
			}
		arena.leaveArena(player);
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
		return "Quake.player";
	}

}
