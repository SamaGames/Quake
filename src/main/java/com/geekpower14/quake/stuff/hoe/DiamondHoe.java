package com.geekpower14.quake.stuff.hoe;

import com.geekpower14.quake.stuff.TItem;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/*
 * This file is part of Quake.
 *
 * Quake is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quake is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quake.  If not, see <http://www.gnu.org/licenses/>.
 */
public class DiamondHoe extends HoeBasic{

	public DiamondHoe()
	{
		super(85,
				ChatColor.BLUE + "PortalGun",
                TItem.secondToTick(1.4),
				FireworkEffect.builder().withColor(Color.BLUE).with(FireworkEffect.Type.BURST).build());
        //this.aim = 1.3;
	}

	public ItemStack getItem() {
		return TItem.setItemNameAndLore(new ItemStack(Material.DIAMOND_HOE), ChatColor.BLUE + "PortalGun", new String[]{
				ChatColor.DARK_GRAY + "Recharge en " + ChatColor.GOLD +"1.4" + ChatColor.DARK_GRAY + " secondes."
		}, false);
	}

}
