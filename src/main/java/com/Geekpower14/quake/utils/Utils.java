package com.Geekpower14.quake.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Utils {
	
	public static Location str2loc(String loc)
	{
		if(loc == null)
			return null;
		
		Location res = null;

			String[] loca = loc.split(", ");
			
			res = new Location(Bukkit.getServer().getWorld(loca[0]), 
					Double.parseDouble(loca[1]), 
					Double.parseDouble(loca[2]), 
					Double.parseDouble(loca[3]),
					Float.parseFloat(loca[4]),
					Float.parseFloat(loca[5]));
			
		return res;
	}
	
	public static String loc2str(Location loc)
	{
		return "" 
		+ loc.getWorld().getName() 
		+ ", " 
		+ loc.getX() 
		+ ", " 
		+ loc.getY() 
		+ ", " 
		+ loc.getZ() 
		+ ", " 
		+ loc.getYaw() 
		+ ", " 
		+ loc.getPitch()
		+ ", " ;
	}
	
	public static String PoToStr(PotionEffect popo)
	{
		return popo.getType().getName()+ ":" + popo.getAmplifier();
	}
	
	public static PotionEffect StrToPo(String popo)
	{
		String[] list = popo.split(":");
		return new PotionEffect(PotionEffectType.getByName(list[0]), Integer.MAX_VALUE, Integer.valueOf(list[1]));
	}
	
	public static Boolean hasPermission(Player p, String perm)
	{
		if(perm.equalsIgnoreCase(""))
			return true;
		if(p.isOp())
			return true;
		if(p.hasPermission("UpperVoid.admin"))
			return true;
		if(p.hasPermission(perm))
			return true;
		
		return false;
	}

}
