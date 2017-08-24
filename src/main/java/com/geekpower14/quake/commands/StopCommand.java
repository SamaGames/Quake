package com.geekpower14.quake.commands;

import com.geekpower14.quake.Quake;
import com.geekpower14.quake.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/*
 * This file is part of Quake.
 *
 * Quake is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quake is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quake.  If not, see <http://www.gnu.org/licenses/>.
 */
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
			Arena arena = plugin.getArenaManager().getArenabyPlayer(player);
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