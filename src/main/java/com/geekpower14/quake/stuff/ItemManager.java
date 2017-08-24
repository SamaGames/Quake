package com.geekpower14.quake.stuff;

import com.geekpower14.quake.stuff.grenade.FragGrenade;
import com.geekpower14.quake.stuff.hoe.*;

import java.util.ArrayList;
import java.util.List;

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
public class ItemManager {
	
	public List<TItem> stuff = new ArrayList<>();
	
	public ItemManager()
	{
		
		stuff.add(new WoodenHoe());
		stuff.add(new StoneHoe());
		stuff.add(new IronHoe());
		stuff.add(new GoldHoe());
		stuff.add(new DiamondHoe());
		stuff.add(new AmazingHoe());
		stuff.add(new BlasterHoe());
		stuff.add(new PotatoHoe());

		stuff.add(new FragGrenade(90, 0));
		stuff.add(new FragGrenade(91, 1));
		stuff.add(new FragGrenade(92, 2));
		stuff.add(new FragGrenade(93, 3));
		stuff.add(new FragGrenade(94, 4));
		stuff.add(new FragGrenade(95, 5));
		stuff.add(new FragGrenade(95, 5));
	}
	
	public TItem getItemByID(int id)
	{
		for(TItem i : stuff)
		{
			if(i.getId() == id)
				return (TItem) i.clone();
		}

		return getItemByID(81); //wooden hoe
	}

	public TItem getItemByID(int id, int defaut)
	{
		for(TItem i : stuff)
		{
			if(i.getId() == id)
				return (TItem) i.clone();
		}

		return getItemByID(defaut);
	}

}
