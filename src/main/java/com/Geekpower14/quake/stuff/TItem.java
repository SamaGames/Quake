package com.Geekpower14.quake.stuff;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.APlayer;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public abstract class TItem implements Cloneable{
	
	public Quake plugin;
	
	public String name = "Unknown";
	
	public String alias = "";
	
	public String givePerm = "quake.admin";
	
	public long reloadTime;
	
	public int nb = 1;
	
	public TItem(String name, String display, int nb, long l)
	{
		this.name = name;
		this.alias = display;
		this.reloadTime = l;
		
		this.nb = nb;
		
		plugin = Quake.getPlugin();
	}
	
	public static ItemStack addGlow(ItemStack item){
		  net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		  NBTTagCompound tag = null;
		  if (!nmsStack.hasTag()) {
		      tag = new NBTTagCompound();
		      nmsStack.setTag(tag);
		  }
		  if (tag == null) tag = nmsStack.getTag();
		  NBTTagList ench = new NBTTagList();
		  tag.set("ench", ench);
		  nmsStack.setTag(tag);
		  return CraftItemStack.asCraftMirror(nmsStack);
	}
	
	public static ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore, boolean glow)
	{
		ItemMeta im = item.getItemMeta();
		if (im == null)
			return item;
		if (name != "")
			im.setDisplayName(name);
		if (lore != null)
			im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		if(glow)
			item = addGlow(item);
		return item;
	}
	
    public static long secondToTick(double second)
    {
        return (long) (second * 20);
    }
	
	public String getName()
	{
		return name;
	}
	
	public String getDisplayName()
	{
		return alias;
	}
	
	public String getGivePerm()
	{
		return this.givePerm;
	}
	
	public Boolean istheSame(ItemStack it)
	{
		ItemStack item = this.getItem();

		ItemMeta meta = item.getItemMeta();
		ItemMeta met = it.getItemMeta();

		if(meta == null && met == null)
		{
			return true;
		}

		if(meta == null || met == null)
		{
			return false;
		}

		if (!meta.getDisplayName().equalsIgnoreCase(met.getDisplayName()))
		{
			return false;
		}

		if (!meta.getLore().equals(met.getLore()))
		{
			return false;
		}

		return true;
	}
	
	public Object clone() {
		Object o = null;
		try {
			// On récupère l'instance à renvoyer par l'appel de la
			// méthode super.clone()
			o = super.clone();
		} catch(CloneNotSupportedException cnse) {
			// Ne devrait jamais arriver car nous implémentons
			// l'interface Cloneable
			cnse.printStackTrace(System.err);
		}
		// on renvoie le clone
		return o;
	}
	
	public int getNB()
	{
		return nb;
	}

	public void setNB(int nb)
	{
		this.nb = nb;
	}
	
	public abstract ItemStack getItem();
	
	public abstract void rightAction(APlayer ap, APlayer.ItemSLot slot);
	
	public abstract void leftAction(APlayer ap, APlayer.ItemSLot slot);
	
	//public abstract void onItemTouchGround(arena arena, Item item);

}
