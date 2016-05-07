package com.Geekpower14.quake.stuff.hoe;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PotatoHoe extends HoeBasic{

	public PotatoHoe()
	{
		super("potatohoe",
				ChatColor.BLUE + "PatatoGun",
				1L,
				FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.STAR).build());
        //this.aim = 2;
	}

	public ItemStack getItem() {
		return setItemNameAndLore(new ItemStack(Material.POTATO_ITEM), ChatColor.BLUE + "FritoGun", new String[]{
				ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"0.1" + ChatColor.DARK_GRAY + " secondes.",
				ChatColor.DARK_GRAY + "Précision cheaté à " + ChatColor.GOLD +"2" + ChatColor.DARK_GRAY +" blocks.",
				ChatColor.RED + "Geekpower14's friends only !"
		}, true);
	}
}
