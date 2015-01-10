package com.Geekpower14.quake.utils;

import org.bukkit.Location;

public class Spawn {
	
	private Location loc;
	private int use = 0;
	
	public Spawn(Location loc)
	{
		this.loc = loc;
	}
	
	public Spawn(String loc)
	{
		this.loc = Utils.str2loc(loc);
	}
	
	public void setLoc(Location loc)
	{
		this.loc = loc;
	}
	
	public void setLoc(String loc)
	{
		this.loc = Utils.str2loc(loc);
	}
	
	public Location getLoc()
	{
		return loc;
	}
	
	public void addUse()
	{
		use++;
	}
	
	public void addUses(int u)
	{
		use += u;
	}
	
	public void setUses(int u)
	{
		use = u;
	}
	
	public int getUses()
	{
		return use;
	}
	
	public String getSaveLoc()
	{
		return Utils.loc2str(loc);
	}

}
