package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.stuff.TItem;
import com.Geekpower14.quake.stuff.grenade.FragGrenade;
import com.Geekpower14.quake.utils.SimpleScoreboard;
import com.Geekpower14.quake.utils.Utils;
import net.zyuiop.MasterBundle.FastJedis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class APlayer {

	private Quake plugin;

	private Arena arena;

	private Player p;

	private Role role = Role.Player;

	private boolean vip = false;

	private boolean Reloading = false;

	private Boolean Invincible = false;

	private Scoreboard board;
	
	private SimpleScoreboard sboard;

	private int coins = 0;

	private int score = 0;

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
		
		sboard = new SimpleScoreboard(board, "" + ChatColor.RED + ChatColor.BOLD + "Quake", p.getName());
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
				HashMap<String, Response<String>> data = new HashMap<>();

				//Requetes (optimise pour resultat rapide)
				ShardedJedis jedis = FastJedis.jedis();
				ShardedJedisPipeline pipeline = jedis.pipelined();
				data.put("hoe", pipeline.get("shops:quake:hoes:" + p.getUniqueId().toString() + ":current"));
				data.put("grenade", pipeline.get("shops:quake:fragrenade:"+p.getUniqueId().toString()+":current"));
				pipeline.sync();
				FastJedis.back(jedis);

				//Shooter
				stuff.put(ItemSLot.Slot1, plugin.itemManager.getItemByName(data.get("hoe").get()));

				//Grenade
				if (data.get("grenade").get() != null) {
					String[] dj = data.get("grenade").get().split("-");
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

	public ItemSLot getSlot()
	{
		int i = p.getInventory().getHeldItemSlot();
		for(ItemSLot is : ItemSLot.values())
		{
			if(i == is.getSlot())
				return is;
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
		if(this.getArena() instanceof ArenaSolo)
		{
			ArenaSolo arenaSolo = (ArenaSolo)arena;
			sboard.reset();

			List<APlayer> ps = arenaSolo.getTopFive();

			for(int i = 0; i < ps.size(); i++)
			{
				APlayer aps = ps.get(i);
				sboard.add(aps.getName(), aps.getScore());
			}
			setLevel(this.getScore());

			sboard.build();
		}

		if(this.getArena() instanceof ArenaTeam)
		{
			ArenaSolo arenaSolo = (ArenaSolo)arena;
			sboard.reset();



			setLevel(this.getScore());

			sboard.build();
		}




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

	public void setinvincible(Boolean t)
	{
		this.Invincible = t;
	}

	public boolean isReloading()
	{
		return this.Reloading;
	}

	public void setReloading(Boolean t)
	{
		this.Reloading = t;
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
		score = i;
        try{
            setLevel(i);
        }catch(Exception e)
        {

        }
	}
	
	public void addScore(int i)
	{
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
