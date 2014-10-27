package com.Geekpower14.Quake.Stuff.Hoe;

import com.Geekpower14.Quake.Arena.APlayer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WoodenHoe extends HoeBasic{

	public WoodenHoe()
	{
		super("woodenhoe",
				ChatColor.AQUA + "SlowGun",
                secondToTick(1.8),
				FireworkEffect.builder().withColor(Color.MAROON).with(FireworkEffect.Type.BALL_LARGE).build());
        //this.aim = 1.6;
	}

	public ItemStack getItem() {
		ItemStack coucou = setItemNameAndLore(new ItemStack(Material.WOOD_HOE), ChatColor.AQUA + "SlowGun", new String[]{
			ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.8" + ChatColor.DARK_GRAY + " secondes."
		}, false);

		return coucou;
	}

	public void leftAction(APlayer p) {
		return;		
	}

	public void rightAction(APlayer ap) {
		basicShot(ap.getP());		
	}

}
