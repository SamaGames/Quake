package com.Geekpower14.Quake.Commands;

import org.bukkit.entity.Player;

public interface BasicCommand {
	
	public boolean onCommand(Player player, String[] args);

    public String help(Player p);
    
    public String getPermission();
	

}
