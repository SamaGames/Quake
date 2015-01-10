package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ATeam {
	
	private Team team;
	
	private String name;
	
	private ChatColor color;
	
	private Color Scolor;
	
	private DyeColor dc;
	
	private Arena aren;
	
	private int Score = 0;
	
	private boolean isActive = false;

    private List<OfflinePlayer> players = new ArrayList<>();

	public ATeam(Quake pl, Arena aren, String name, ChatColor color, Color scolor, DyeColor dc)
	{
		this.name = name;
		this.color = color;
		this.aren = aren;
		Scolor = scolor;
		this.dc = dc;
		
		createTeam();
	}
	
	public void createTeam()
	{
		team = aren.getScoreboard().registerNewTeam(name);
		
		team.setPrefix("" + color);
		team.setAllowFriendlyFire(false);
		team.setCanSeeFriendlyInvisibles(true);
	}
	
	public boolean isActive()
	{
		return isActive;
	}
	
	public void setActive(boolean a)
	{
		isActive = a;
	}
	
	public Arena getArena()
	{
		return aren;
	}
	
	public int getScore()
	{
		return Score;
	}
	
	public void setScore(int s)
	{
		Score = s;
	}
	
	public void addScore(int s)
	{
		Score += s;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addPlayer(Player p)
	{
        players.add(p);
		team.addPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
	}
	
	public void removePlayer(Player p)
	{
        players.remove(p);
		team.removePlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
	}
	
	public Boolean hasPlayer(Player p)
	{
		for(OfflinePlayer op : players)
        {
            if(op.getUniqueId().equals(p.getUniqueId()))
                return true;
        }
        return false;
	}
	
	public void giveChestplate(Player p)
	{
		ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
		meta.setDisplayName(color + "Team " + name);
		List<String> l = new ArrayList<String>();
		l.add(ChatColor.RESET + "A beautiful leather dress!");
		meta.setLore(l);
		meta.setColor(Scolor);
		item.setItemMeta(meta);
		
		p.getInventory().setChestplate(item);
	}
	
	public List<OfflinePlayer> getPlayers()
	{
		return players;
	}
	
	public Collection<Player> getTalkPlayers()
	{
		List<Player> p = new ArrayList<Player>();
		
		for(OfflinePlayer ppp : getPlayers())
		{
			p.add(ppp.getPlayer());
		}
		return p;
	}

    public boolean isBlockTeam(Block b)
    {
        if(b.getData() == getDyeColor().getWoolData())
        {
            return true;
        }

        return false;
    }
	
	public int getSize()
	{
		return players.size();
	}
	
	public void reset()
	{
		Score = 0;
        players.clear();
		team.unregister();
		createTeam();
	}
	
	public ChatColor getColor()
	{
		return color;
	}
	
	public DyeColor getDyeColor()
	{
		return dc;
	}

}
