package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.stuff.TItem;
import com.Geekpower14.quake.stuff.grenade.FragGrenade;
import com.Geekpower14.quake.utils.SimpleScoreboard;
import com.Geekpower14.quake.utils.Utils;
import net.zyuiop.MasterBundle.FastJedis;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class APlayer {

	private Quake plugin;

	private Arena arena;

	private Player p;

	private Role role = Role.Player;

	private boolean vip = false;

	//private Location lastLoc = null;

	private int killstreak = 0;

	private boolean Reloading = false;

	private Boolean Invincible = false;

	private Scoreboard board;
	
	private SimpleScoreboard sboard;

	//private Objective bar;

	private int coins = 0;

	private int score = 0;
	
	public TabHolder tabh = new TabHolder();

	private long lastChangeBlock = System.currentTimeMillis();

	private HashMap<ItemSLot, TItem> stuff = new HashMap<ItemSLot, TItem>();

	public APlayer(Quake pl, Arena arena, Player p)
	{
		plugin = pl;
		this.arena = arena;
		this.p = p;

		if(Utils.hasPermission(p, "quake.vip"))
		{
			vip = true;
		}

		/*if(p.getScoreboard() != null)
		{
			board = p.getScoreboard();
		}
		else{*/
			
		//}
		board = Bukkit.getScoreboardManager().getNewScoreboard();
		
		sboard = new SimpleScoreboard(board, "" + ChatColor.RED + ChatColor.BOLD + "quake", p.getName());
		//updateScoreboard();
		//setScoreboard();

		resquestStuff();

	}

	public void resquestStuff()
	{
		final APlayer ap = this;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				//TODO recherche database
				final String key_hoe = "shops:quake:hoes:"+p.getUniqueId().toString()+":current";//REQUEST
				final String key_grenade = "shops:quake:fragrenade:"+p.getUniqueId().toString()+":current";//REQUEST

				//Shooter
				String data = FastJedis.get(key_hoe);
				stuff.put(ItemSLot.Slot1, plugin.itemManager.getItemByName(data));

				//Grenade
				data = FastJedis.get(key_grenade);
				if (data != null) {
					String[] dj = data.split("-");
					if (dj[0].equals("fragrenade")) {
						final int add = Integer.parseInt(dj[1]);
						FragGrenade grenade = (FragGrenade) plugin.itemManager.getItemByName("fragrenade");
						grenade.setNB(1 + add);
						stuff.put(ItemSLot.Slot2, grenade);
					}
				} else {
					FragGrenade grenade = (FragGrenade) plugin.itemManager
							.getItemByName("fragrenade");

					grenade.setNB(1);
					stuff.put(ItemSLot.Slot2, grenade);
				}

			}
		});		
	}

	@SuppressWarnings("deprecation")
	public void giveStuff()
	{
		for(ItemSLot i : stuff.keySet())
		{
			TItem item = stuff.get(i);

			p.getInventory().setItem(i.getSlot(), item.getItem());
		}
		p.updateInventory();
	}

	public TItem getStuff(int i)
	{
		for(ItemSLot is : ItemSLot.values())
		{
			if(i == is.getSlot())
				return stuff.get(is);
		}
		
		return null;
	}

	public void removeScoreboard()
	{
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public void setScoreboard()
	{
		p.setScoreboard(board);
		//updateScoreboard();
	}

	@SuppressWarnings("deprecation")
	public void updateScoreboard()
	{

		sboard.reset();
		
		List<APlayer> ps = arena.getTopFive();
		
		for(int i = 0; i < ps.size(); i++)
		{
			APlayer aps = ps.get(i);
			/*if(aps.getUUID().equals(p.getUniqueId()))
			{
				continue;
			}*/
			
			sboard.add(aps.getName(), aps.getScore());
		}
        setLevel(this.getScore());
		//int p = arena.getPositionScore(this);
		//sboard.add(this.getName(), this.getScore());
		//sboard.setPos(p);
		/*sboard.blankLine();
		List<APlayer> aaap = arena.getAPlayersList();
		for(int i = 0; i < aaap.size(); i++)
		{
			sboard.add(aaap.get(i).getName() + ": " + aaap.get(i).getScore());
		}*/
        sboard.build();

		//sboard.send(p);
	}

	public void setReloading(Long Ticks)
	{
		Reloading = true;

		final Long temp = Ticks;

		p.setExp(0);

		final int infoxp = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{

			public void run() {
				float xp = p.getExp();
				xp += getincr(temp);
				if(xp >= 1)
				{
					xp = 1;
				}
				p.setExp(xp);
			}
		}, 0L, 2L);

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run() {
                p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
			}
		}
		, Ticks-5);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run() {
				Reloading = false;
				p.setExp(1);
				plugin.getServer().getScheduler().cancelTask(infoxp);
                //p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
			}
		}
		, Ticks);

		return;
	}

	public void setReloading(Boolean t)
	{
		this.Reloading = t;
	}

	public void setinvincible(Boolean t)
	{
		this.Invincible = t;
	}

	public boolean isReloading()
	{
		return this.Reloading;
	}

	public boolean isInvincible()
	{
		return this.Invincible;
	}

	public void setInvincible(Long Ticks)
	{
		Invincible = true;

		final Long temp = Ticks;

		p.setExp(0);

		final int infoxp = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{

			public void run() {
				float xp = p.getExp();
				xp += getincr(temp);
				if(xp >= 1)
				{
					xp = 1;
				}
				p.setExp(xp);


			}

		}, 0L, 2L);

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()

		{
			public void run() {

				Invincible = false;
				p.setExp(1);
				plugin.getServer().getScheduler().cancelTask(infoxp);
			}
		}
		, Ticks);

		return;
	}

	public float getincr(Long time)
	{
		float result = 0;

		float temp = time; 

		result = (100/(temp/2))/100;


		return result;
	}

	public boolean isVIP()
	{
		return vip;
	}

	public Arena getArena()
	{
		return arena;
	}

	/***
	 * 
	 * @return Player p
	 */
	public Player getP()
	{
		return p;
	}

	public String getName()
	{
		return p.getName();
	}

	public UUID getUUID()
	{
		return p.getUniqueId();
	}

	public int getCoins()
	{
		return coins;
	}

	public void setCoins(int c)
	{
		coins = c;
		//updateScoreboard();
	}
	
	public void addCoins(int c)
	{
		coins += c;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public void setScore(int i)
	{
		killstreak = i;
		score = i;
        try{
            setLevel(i);
        }catch(Exception e)
        {

        }
	}
	
	public void addScore(int i)
	{
		killstreak += i;
		score += i;
        try{
            setLevel(i);
        }catch(Exception e)
        {

        }
	}

	public String getDisplayName()
	{
		return p.getDisplayName();
	}

	public Location getLocation() {
		return p.getLocation();
	}

	public Location getEyeLocation() {
		return p.getEyeLocation();
	}

	public boolean isDead() {
		return p.isDead();
	}

	public Role getRole()
	{
		return role;
	}

	public void setRole(Role r)
	{
		role = r;
	}

	public void tell(String message)
	{
		p.sendMessage(message);
	}

	public void setLevel(int xp) {
		p.setLevel(xp);		
	}

	public void checkAntiAFK()
	{
		long time = System.currentTimeMillis();

		if(time - lastChangeBlock > 900)
		{
			/*Location loc = p.getLocation();

			double X = loc.getX();
			double Y = loc.getBlockY() - 1;
			double Z = loc.getZ();

			Location b = getPlayerStandOnBlockLocation(new Location(loc.getWorld(), X, Y, Z));

			arena.getBM().addDamage(b.getBlock());*/
		}
	}

	public Location getPlayerStandOnBlockLocation(Location locationUnderPlayer)
	{
		Location b11 = locationUnderPlayer.clone().add(0.3,0,-0.3);
		if (b11.getBlock().getType() != Material.AIR)
		{
			return b11;
		} 
		Location b12 = locationUnderPlayer.clone().add(-0.3,0,-0.3);
		if (b12.getBlock().getType() != Material.AIR)
		{
			return b12;
		}
		Location b21 = locationUnderPlayer.clone().add(0.3,0,0.3);
		if (b21.getBlock().getType() != Material.AIR)
		{
			return b21;
		}
		Location b22 = locationUnderPlayer.clone().add(-0.3,0,+0.3);
		if (b22.getBlock().getType() != Material.AIR)
		{
			return b22;
		}
		return locationUnderPlayer;
	}
	
	/**
	 * Clear a players tab menu
	 *
	 */
	public void clearTab(){
		plugin.clearTab(p, tabh);
	}
	
	public void updatePlayer(){
		plugin.updatePlayer(p, tabh);
	}
	
	public void setTab(int x, int y, String msg, int ping) {

		TabHolder t = tabh;

		t.tabs[y][x] = msg;
		t.tabPings[y][x] = ping;
		t.maxh = 3;
		t.maxv = Math.max(x+1, t.maxv);
	}

	/*public boolean isOnSameBlock()
	{
		boolean result = true;
		Location loc = p.getLocation();

		if(lastLoc == null)
		{
			lastLoc = loc;
			return result;
		}

		if(loc.getBlockX() != lastLoc.getBlockX())
		{
			result = false;
		}

		if(loc.getBlockY() != lastLoc.getBlockY())
		{
			result = false;
		}

		if(loc.getBlockZ() != lastLoc.getBlockZ())
		{
			result = false;
		}

		if(result == false)
		{
			lastLoc = loc;
			lastChangeBlock = System.currentTimeMillis();
		}

		return result;
	}*/

	public enum ItemSLot{
		// http://redditpublic.com/images/b/b2/Items_slot_number.png
		Head("Head", 103),
		Armor("Armor", 102),
		Slot1("Slot1", 0),
		Slot2("Slot2", 1),
		Slot3("Slot3", 2),
		Slot4("Slot4", 3),
		Slot5("Slot5", 4),
		Slot6("Slot6", 5),
		Slot7("Slot7", 6),
		Slot8("Slot8", 7);

		private String info;
		private int value;

		private ItemSLot(String info, int value)
		{
			this.info = info;
			this.value = value;
		}

		public String getString()
		{
			return info;
		}

		public int getValue()
		{
			return value;
		}

		public int getSlot()
		{
			return value;
		}
	}

	public enum Role{
		Player("Player", 20),
		Spectator("Spectateur", 10);

		private String info;
		private int value;

		private Role(String info, int value)
		{
			this.info = info;
			this.value = value;
		}

		public String getString()
		{
			return info;
		}

		public int getValue()
		{
			return value;
		}
	}
}
