package com.Geekpower14.quake.utils;

import com.Geekpower14.quake.Quake;
import net.minecraft.server.v1_8_R1.EntityFireworks;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftFirework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Utils {

	public static void launchfw(final Location loc, final FireworkEffect effect)
	{
		final Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.addEffect(effect);
		fwm.setPower(0);
		fw.setFireworkMeta(fwm);
		((CraftFirework)fw).getHandle().setInvisible(true);
		Bukkit.getScheduler().runTaskLater(Quake.getPlugin(), new Runnable() {
			@Override
			public void run() {
				net.minecraft.server.v1_8_R1.World w = (((CraftWorld) loc.getWorld()).getHandle());
				EntityFireworks fireworks = ((CraftFirework)fw).getHandle();
				w.broadcastEntityEffect(fireworks, (byte)17);
				fireworks.die();
			}
		}, 1);
	}
	
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
