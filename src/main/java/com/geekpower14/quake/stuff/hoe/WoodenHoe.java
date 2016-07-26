package com.geekpower14.quake.stuff.hoe;

import com.geekpower14.quake.stuff.TItem;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WoodenHoe extends HoeBasic{

	public WoodenHoe()
	{
		super(81,
				ChatColor.AQUA + "SlowGun",
                TItem.secondToTick(1.8),
				FireworkEffect.builder().withColor(Color.MAROON).with(FireworkEffect.Type.BALL_LARGE).build());
        //this.aim = 1.6;
	}

	public ItemStack getItem() {
		return TItem.setItemNameAndLore(new ItemStack(Material.WOOD_HOE), ChatColor.AQUA + "SlowGun", new String[]{
				ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.8" + ChatColor.DARK_GRAY + " secondes."
		}, false);
	}
}
