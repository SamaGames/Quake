package com.geekpower14.quake.utils;

import org.bukkit.Location;

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
	
	public Location getLoc()
	{
		return loc;
	}
	
	public void setLoc(String loc)
	{
		this.loc = Utils.str2loc(loc);
	}
	
	public void addUse()
	{
		use++;
	}
	
	public void addUses(int u)
	{
		use += u;
	}
	
	public int getUses()
	{
		return use;
	}
	
	public void setUses(int u)
	{
		use = u;
	}
	
	public String getSaveLoc()
	{
		return Utils.loc2str(loc);
	}

}
