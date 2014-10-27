package com.Geekpower14.Quake.Stuff;

import com.Geekpower14.Quake.Quake;
import com.Geekpower14.Quake.Stuff.Grenade.FragGrenade;
import com.Geekpower14.Quake.Stuff.Hoe.*;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {
	
	public Quake plugin;
	
	public List<TItem> stuff = new ArrayList<TItem>(); 
	
	public ItemManager(Quake pl)
	{
		plugin = pl;
		
		stuff.add(new WoodenHoe());
		stuff.add(new StoneHoe());
		stuff.add(new IronHoe());
		stuff.add(new GoldHoe());
		stuff.add(new DiamondHoe());
		stuff.add(new AmazingHoe());
		stuff.add(new PotatoHoe());

		stuff.add(new FragGrenade());
	}
	
	public TItem getItemByName(String name)
	{
		for(TItem i : stuff)
		{
			if(i.getName().equals(name))
				return (TItem) i.clone();
		}

		return getItemByName("woodenhoe");
	}

}
