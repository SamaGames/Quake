package com.Geekpower14.quake.stuff.hoe;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class StoneHoe extends HoeBasic{

	public StoneHoe()
	{
		super("stonehoe",
				ChatColor.DARK_AQUA + "Advanced SlowGun",
                secondToTick(1.7),
				FireworkEffect.builder().withColor(Color.SILVER).with(FireworkEffect.Type.BURST).build());
        //this.aim = 1.566;
	}

	public ItemStack getItem() {
		ItemStack coucou = setItemNameAndLore(new ItemStack(Material.STONE_HOE), ChatColor.DARK_AQUA + "Advanced SlowGun", new String[]{
			ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.7" + ChatColor.DARK_GRAY + " secondes."
		}, false);

		return coucou;
	}
<<<<<<< HEAD:src/main/java/com/Geekpower14/quake/stuff/hoe/StoneHoe.java
=======

>>>>>>> 1.8:src/main/java/com/Geekpower14/quake/stuff/hoe/StoneHoe.java
}
