package com.geekpower14.quake.stuff.hoe;

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
public class PotatoHoe extends HoeBasic{

	public PotatoHoe()
	{
		super(-1,
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
