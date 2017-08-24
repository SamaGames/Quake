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
