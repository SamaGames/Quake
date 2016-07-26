package com.geekpower14.quake.commands;

import org.bukkit.entity.Player;

public interface BasicCommand {
	
	public boolean onCommand(Player player, String[] args);

    public String help(Player p);
    
    public String getPermission();
	

}
