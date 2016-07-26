package com.geekpower14.quake.stuff.hoe;

import com.geekpower14.quake.stuff.TItem;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class IronHoe extends HoeBasic{

	public IronHoe()
	{
		super(83,
				ChatColor.DARK_AQUA + "QuickGun",
                TItem.secondToTick(1.6),
				FireworkEffect.builder().withColor(Color.GRAY).with(FireworkEffect.Type.BALL).build());
        //this.aim = 1.5;
	}

	public ItemStack getItem() {
		return TItem.setItemNameAndLore(new ItemStack(Material.IRON_HOE), ChatColor.DARK_AQUA + "QuickGun", new String[]{
				ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.6" + ChatColor.DARK_GRAY + " secondes."
		}, false);
	}
}
