package com.Geekpower14.quake.stuff.hoe;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DiamondHoe extends HoeBasic{

	public DiamondHoe()
	{
		super(85,
				ChatColor.BLUE + "PortalGun",
                secondToTick(1.4),
				FireworkEffect.builder().withColor(Color.BLUE).with(FireworkEffect.Type.BURST).build());
        //this.aim = 1.3;
	}

	public ItemStack getItem() {
		return setItemNameAndLore(new ItemStack(Material.DIAMOND_HOE), ChatColor.BLUE + "PortalGun", new String[]{
				ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.4" + ChatColor.DARK_GRAY + " secondes."
		}, false);
	}

}
