package com.Geekpower14.quake.stuff.hoe;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GoldHoe extends HoeBasic{

	public GoldHoe()
	{
		super(84,
				ChatColor.GOLD + "GoldenGun",
                secondToTick(1.5),
				FireworkEffect.builder().withColor(Color.ORANGE).with(FireworkEffect.Type.BURST).build());
        //this.aim = 1.4;
	}

	public ItemStack getItem() {
		return setItemNameAndLore(new ItemStack(Material.GOLD_HOE), ChatColor.GOLD + "GoldenGun", new String[]{
				ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.5" + ChatColor.DARK_GRAY + " secondes."
		}, false);
	}
}
