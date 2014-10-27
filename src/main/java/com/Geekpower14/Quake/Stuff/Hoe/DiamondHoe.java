package com.Geekpower14.Quake.Stuff.Hoe;

import com.Geekpower14.Quake.Arena.APlayer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DiamondHoe extends HoeBasic{

	public DiamondHoe()
	{
		super("diamondhoe",
				ChatColor.BLUE + "PortalGun",
                secondToTick(1.4),
				FireworkEffect.builder().withColor(Color.BLUE).with(FireworkEffect.Type.BURST).build());
        //this.aim = 1.3;
	}

	public ItemStack getItem() {
		ItemStack coucou = setItemNameAndLore(new ItemStack(Material.DIAMOND_HOE), ChatColor.BLUE + "PortalGun", new String[]{
			ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.4" + ChatColor.DARK_GRAY + " secondes."
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
