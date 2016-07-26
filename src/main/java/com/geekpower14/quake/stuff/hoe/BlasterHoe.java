package com.geekpower14.quake.stuff.hoe;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BlasterHoe extends HoeBasic{

	public BlasterHoe()
	{
		super(87,
				ChatColor.BLUE + "Blaster DL-22",
				secondToTick(1.1),
				FireworkEffect.builder().withColor(Color.FUCHSIA).with(FireworkEffect.Type.CREEPER).build());
        //this.aim = 1.2;
	}

	public ItemStack getItem() {
		return setItemNameAndLore(new ItemStack(Material.DIAMOND_BARDING), ChatColor.BLUE + "Blaster DL-22", new String[]{
				ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.1" + ChatColor.DARK_GRAY + " secondes."
		}, false);
	}
}
