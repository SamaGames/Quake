package com.Geekpower14.quake.commands;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class StopCommand implements BasicCommand{

	private Quake plugin;

	public StopCommand(Quake pl)
	{
		plugin = pl;
	}

	@Override
	public boolean onCommand(Player player, String[] args) {

		if(Quake.hasPermission(player, this.getPermission()))
		{
			Arena arena = plugin.arenaManager.getArenabyPlayer(player);
			if(arena == null)
			{
				player.sendMessage(ChatColor.RED + "Vous n'êtes pas dans une arène!");
				return true;
			}
			
			arena.handleGameEnd();
			
			player.sendMessage(ChatColor.GREEN + "Force beginning for the arena : " + args[0]);

			return true;
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
			return "/q stop [arena] - Force stop une arene.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "quake.modo";
	}

}