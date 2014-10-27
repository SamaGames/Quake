package com.Geekpower14.Quake.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.Geekpower14.Quake.Quake;
import com.Geekpower14.Quake.Arena.Arena;

public class SetMinCommand implements BasicCommand{
	
	private Quake plugin;

	public SetMinCommand(Quake pl)
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
			
			if(args.length < 2)
			{
				player.sendMessage(ChatColor.RED + "Please type a number !");
				return true;
			}
			arena.setMinPlayers(Integer.parseInt(args[1]));
			arena.saveConfig();
			player.sendMessage(ChatColor.GREEN + "Set the minimum player with success !");
			
			
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
			return "/q setmin [Arena] [Number] - Set minimum player in the arena.";
		}
		return "";
	}

	@Override
	public String getPermission() {
		return "Quake.edit";
	}

}
