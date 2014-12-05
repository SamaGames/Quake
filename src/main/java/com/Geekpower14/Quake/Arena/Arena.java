package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.arena.APlayer.Role;
import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.task.ScoreHandler;
import com.Geekpower14.quake.utils.FireworkEffectPlayer;
import com.Geekpower14.quake.utils.Spawn;
import com.Geekpower14.quake.utils.StatsNames;
import com.Geekpower14.quake.utils.Utils;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.types.GameArena;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class Arena implements GameArena {

	public Quake plugin;

	/*** Constantes ***/

	public String name;
	public String Map_name;
	public UUID uuid;

	public int minPlayer = 4;
	public int maxPlayer = 24;
	public int vipSlots = 5;
	public int spectateSlots = 5;

	public int Time_Before = 20;
	public int Time_After = 15;

	public int Goal = 25;

	public List<Spawn> spawn = new ArrayList<>();

	public List<PotionEffect> potions = new ArrayList<>();

	public boolean vip;

	/*** Variable dynamiques ***/

	public FireworkEffectPlayer fplayer = new FireworkEffectPlayer();

	public Status eta = Status.Idle;

	public List<APlayer> players = new ArrayList<APlayer>();

	public List<UUID> waitPlayers = new ArrayList<UUID>();

	private Starter CountDown = null;

	//ScoreBoard teams
	private Scoreboard tboard;
	
	private Objective score;

	private Team spectates;

    private ScoreHandler sh;

	public Arena(Quake pl, String name)
	{
		super();
		plugin = pl;

		this.name = name;

		loadConfig();

		tboard = Bukkit.getScoreboardManager().getNewScoreboard();

		spectates = tboard.registerNewTeam("spectator");
		spectates.setCanSeeFriendlyInvisibles(true);

		eta = Status.Available;

	}

	/*
	 * Configuration.
	 */

	private void loadConfig()
	{
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "/arenas/"+ name + ".yml"));

		config = basicConfig(config);

		Map_name = config.getString("Map");

		uuid = UUID.fromString(config.getString("UUID"));

		vip = config.getBoolean("VIP");

		Time_Before = config.getInt("Time-Before");
		Time_After = config.getInt("Time-After");

		minPlayer = config.getInt("MinPlayers");
		maxPlayer = config.getInt("MaxPlayers");

		Goal = config.getInt("Goal");

		vipSlots = config.getInt("Slots-VIP");
		spectateSlots = config.getInt("Slots-Spectator");

		List<PotionEffect> l = new ArrayList<>();
		for(String popo : config.getStringList("Potions"))
		{
			l.add(Utils.StrToPo(popo));
		}
		potions = l;

		List<String> s = config.getStringList("Spawns");
		for(int i = 0; i < s.size(); i++)
		{
			spawn.add(new Spawn(s.get(i)));
		}

		saveConfig();
	}

	private FileConfiguration basicConfig(FileConfiguration config)
	{
		setDefaultConfig(config, "Name", name);
		setDefaultConfig(config, "Map", "Unknown");
		setDefaultConfig(config, "UUID", UUID.randomUUID().toString());

		setDefaultConfig(config, "VIP", false);

		setDefaultConfig(config, "Time-Before", 20);
		setDefaultConfig(config, "Time-After", 15);

		setDefaultConfig(config, "MaxPlayers", 24);
		setDefaultConfig(config, "MinPlayers", 4);

		setDefaultConfig(config, "Goal", 25);

		setDefaultConfig(config, "Slots-VIP", 5);
		setDefaultConfig(config, "Slots-Spectator", 5);

		setDefaultConfig(config, "Spawns", new ArrayList<String>());

		List<String> l = new ArrayList<String>();
		l.add("SPEED:2");
		l.add("JUMP:1");
		setDefaultConfig(config, "Potions", l);

		return config;
	}

	public void saveConfig()
	{
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "/arenas/"+ name + ".yml"));

		config.set("Name", name);
		config.set("Map", Map_name);
		config.set("UUID", uuid.toString());

		config.set("VIP", vip);

		config.set("Time-Before", Time_Before);
		config.set("Time-After", Time_After);

		config.set("MinPlayers", minPlayer);
		config.set("MaxPlayers", maxPlayer);

		config.set("Goal", Goal);

		config.set("Slots-VIP", vipSlots);
		config.set("Slots-Spectator", spectateSlots);

		List<String> l = new ArrayList<String>();
		for(PotionEffect popo : potions)
		{
			l.add(Utils.PoToStr(popo));
		}
		config.set("Potions", l);

		List<String> s = new ArrayList<String>();
		for(int i = 0; i < spawn.size(); i++)
		{
			s.add(spawn.get(i).getSaveLoc());
		}
		config.set("Spawns", s);

		try {
			config.save(new File(plugin.getDataFolder(), "/arenas/"+ name + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void setDefaultConfig(FileConfiguration config, String key, Object value) {
		if (!config.contains(key))
			config.set(key, value);
	}	

	/*** Mouvement des joueurs ***/

	public String requestJoin(final VPlayer p)
	{

		if(plugin.arenaManager.getArenabyPlayer(p.getName()) != null)
		{
			return ChatColor.RED + "Vous êtes en jeux.";
		}

		if(spawn == null)
		{
			return ChatColor.RED + "Il n'y a pas de spawn.";
		}

		if(eta.isIG())
		{
			return ChatColor.RED + "Jeu en cours.";
		}

        if (CountDown != null && CountDown.time <= 2) {
            return ChatColor.RED + "Jeu en cours.";
        }

		boolean isVIP = p.hasPermission("quake.vip");
		boolean isAdmin = p.hasPermission("quake.yt");

		if(players.size() >= maxPlayer && !isVIP)
		{
			return ChatColor.RED + "Cette arène est au complet.";
		}

		if(players.size() >= maxPlayer + vipSlots && !isAdmin)
		{
			return ChatColor.RED + "Cette arène est au complet.";
		}

		waitPlayers.add(p.getUUID());

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				waitPlayers.remove(p.getUUID());				
			}

		}, 80L);

		return "good";		
	}

	@SuppressWarnings("deprecation")
	public void joinArena(Player p)
	{
		if(!waitPlayers.contains(p.getUniqueId()))
		{
			p.kickPlayer(ChatColor.RED + "Erreur vous n'avez pas le droit de rejoindre.");
			return;
		}

		waitPlayers.remove(p.getUniqueId());

		joinHider(p);
		p.setFlying(false);
		p.setAllowFlight(false);
	
		cleaner(p);

		p.teleport(getSpawn(p));

		/*for(APlayer app : players)
		{
			try {
				plugin.addPacket(app.getP(), p.getPlayerListName(), false, 100);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

		APlayer ap = new APlayer(plugin, this, p);
		players.add(ap);
		
		/*for(APlayer app : players)
		{
			try {
				plugin.addPacket(p, app.getP().getPlayerListName(), false, 100);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		/*try {
			plugin.addPacket(p, p.getPlayerListName(), false, 100);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//ap.setTab(5, 10, "quake", 0);
		
		//ap.updatePlayer();

		this.broadcast(ChatColor.YELLOW + ap.getName() + " a rejoint l'arène " 
				+ ChatColor.DARK_GRAY 
				+ "[" + ChatColor.RED 
				+ players.size() 
				+ ChatColor.DARK_GRAY 
				+ "/" + ChatColor.RED 
				+ maxPlayer 
				+ ChatColor.DARK_GRAY 
				+ "]");

		if(players.size() >= minPlayer && eta == Status.Available)
		{
			startCountdown();
		}

		p.getInventory().setItem(8, this.getLeaveDoor());
		p.getInventory().setHeldItemSlot(0);
		try{
			p.updateInventory();
		}catch(Exception e)
		{/*LOL*/}		
	}

	public void leaveArena(Player p)
	{
		APlayer ap = getAplayer(p);

		leaveCleaner(p);

		for(Player pp : Quake.getOnline())
		{
			p.showPlayer(pp);
		}

		ap.removeScoreboard();

		p.setAllowFlight(false);

		//p.setScoreboard(lol);

		players.remove(ap);

		//kickPlayer(p);

		//orderList();

		if(getActualPlayers() <= 1 && eta == Status.InGame)
		{
			if(players.size() >= 1)
			{

				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
					@Override
					public void run() {
						win(players.get(0).getP());
					}
				}, 1L);
			}else{
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
					@Override
					public void run() {
						stop();
					}
				}, 1L);
			}
		}

		if(players.size() < getMinPlayers() && eta == Status.Starting)
		{
			resetCountdown();
		}

		return;
	}
	
	public void clearTab(Player p)
	{
		
	}

	/*
	 * Gestion de l'arène.
	 */

	public void start()
	{
		eta = Status.InGame;

        sh = new ScoreHandler(plugin, this);
		
		/*if(score != null)
		{
			score.unregister();
		}*/
		//score = tboard.registerNewObjective(name , "dummy");
		
		//Setting where to display the scoreboard/objective (either SIDEBAR, PLAYER_LIST or BELOW_NAME)
		//score.setDisplaySlot(DisplaySlot.SIDEBAR);
		 
		//Setting the display name of the scoreboard/objective
		//score.setDisplayName(""+ChatColor.RED + ChatColor.BOLD + "quake");

		for(APlayer ap : players)
		{
			Player p = ap.getP();

			ap.setScoreboard();

			cleaner(p);	
			tp(p);

			ap.giveStuff();

			giveEffect(p);

			ap.setReloading(1 * 20L);		

			StatsApi.increaseStat(p.getUniqueId(), StatsNames.GAME_NAME, StatsNames.PARTIES, 1);
		}
		updateScore();
	}

	public void stop()
	{
		eta = Status.Stopping;

		/*
		 * TO/DO: Stopper l'arène.
		 */

        sh.stop();
        sh = null;

		List<Player> tokick = new ArrayList<Player>();
		for(int i = players.size()-1; i >= 0; i--)
		{
			APlayer ap = players.get(i);
			Player p = ap.getP();
			tokick.add(p);			
		}

		for(Player p : tokick)
		{
			kickPlayer(p);
		}

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				reset();				
			}
		}, 20 * 4L);

	}

	public void reset()
	{
		/*
		 * TODO: Reset variables.
		 */
		List<OfflinePlayer> pls = new ArrayList<OfflinePlayer>();
		for(OfflinePlayer p : spectates.getPlayers())
		{
			pls.add(p);
		}
		for(OfflinePlayer p : pls)
		{
			spectates.removePlayer(p);
		}
		
		for(Spawn s : spawn)
		{
			s.setUses(0);
		}

		waitPlayers.clear();
		
		List<Player> tokick = new ArrayList<Player>();
		for(int i = players.size()-1; i >= 0; i--)
		{
			APlayer ap = players.get(i);
			Player p = ap.getP();
			tokick.add(p);			
		}

		for(Player p : tokick)
		{
			p.kickPlayer("Redirection..");
		}
		
		players.clear();

		eta = Status.Available;
	}

	public void disable()
	{		
		stop();
		return;
	}

	public void kickPlayer(Player p)
	{
		kickPlayer(p, "");
	}

	public void kickPlayer(Player p, String msg)
	{

		if(!plugin.isEnabled())
		{
			p.kickPlayer(msg);
			return;
		}

		if(!p.isOnline())
			return;

		//kickPlayer(p, "");
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("Connect");
			out.writeUTF("lobby");

		} catch (IOException eee) {
			Bukkit.getLogger().info("You'll never see me!");
		}
		p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());

	}

	public void win(final Player p)
	{
		eta = Status.Stopping;

		if(p != null)
		{
			APlayer ap = getAplayer(p);
			//this.broadcast(ChatColor.AQUA + p.getDisplayName() + ChatColor.YELLOW + " a gagné !");

			this.nbroadcast(ChatColor.AQUA + "#"+ ChatColor.GRAY + "--------------------" + ChatColor.AQUA + "#");
			this.nbroadcast(ChatColor.GRAY + "");
			this.nbroadcast(ChatColor.AQUA + p.getDisplayName() + ChatColor.YELLOW + " a gagné !");
			this.nbroadcast(ChatColor.GRAY + "");
			this.nbroadcast(ChatColor.AQUA + "#"+ ChatColor.GRAY + "--------------------" + ChatColor.AQUA + "#");

			try{
				int up = CoinsManager.syncCreditJoueur(ap.getP().getUniqueId(), 20, true, true);
				ap.setCoins(ap.getCoins() + up);
			}catch(Exception e)
			{
				e.printStackTrace();
			}

			for(APlayer a : players)
			{
				broadcast(a.getP(), ChatColor.GOLD + "Tu as gagné " + a.getCoins() + " coins au total !");
			}
			try{
				StatsApi.increaseStat(p.getUniqueId(), StatsNames.GAME_NAME, StatsNames.VICTOIRES, 1);
			}catch(Exception e)
			{}

		}

		if(p == null)
		{
			stop();
			return;
		}

		final int nb = (int) (Time_After * 1.5);

		final int infoxp = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable()
		{
			int compteur = 0;
			public void run() {

				if(compteur >= nb)
				{
					return;
				}

				//Spawn the Firework, get the FireworkMeta.
				Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
				FireworkMeta fwm = fw.getFireworkMeta();

				//Our random generator
				Random r = new Random();   

				//Get the type
				int rt = r.nextInt(4) + 1;
				Type type = Type.BALL;       
				if (rt == 1) type = Type.BALL;
				if (rt == 2) type = Type.BALL_LARGE;
				if (rt == 3) type = Type.BURST;
				if (rt == 4) type = Type.CREEPER;
				if (rt == 5) type = Type.STAR;

				//Get our random colours   
				int r1i = r.nextInt(17) + 1;
				int r2i = r.nextInt(17) + 1;
				Color c1 = getColor(r1i);
				Color c2 = getColor(r2i);

				//Create our effect with this
				FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

				//Then apply the effect to the meta
				fwm.addEffect(effect);

				//Generate some random power and set it
				int rp = r.nextInt(2) + 1;
				fwm.setPower(rp);

				//Then apply this to our rocket
				fw.setFireworkMeta(fwm);

				compteur++;
			}
		}, 5L, 5L);

		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
		{
			public void run() {
				plugin.getServer().getScheduler().cancelTask(infoxp);
				stop();
			}
		}
		, (Time_After*20));
	}

	public void kill(final Player p)
	{
        try{
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    p.setHealth(0.0);
                }
            });
        }catch(Exception e)
        {
            e.printStackTrace();
        }
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				StatsApi.increaseStat(p.getUniqueId(), StatsNames.GAME_NAME, StatsNames.DEATH, 1);
			}
		});
	}

	public boolean shotplayer(final Player shooter, final Player victim, final FireworkEffect effect)
	{
        if(eta == Status.Stopping)
            return false;

		final APlayer ashooter = this.getAplayer(shooter);
		APlayer avictim = this.getAplayer(victim);
		if(avictim == null)
			return false;
		if(!victim.equals(shooter) && !avictim.isInvincible())
		{
			avictim.setinvincible(true);
			kill(victim);

            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        fplayer.playFirework(victim.getWorld(), victim.getLocation(), effect);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    broadcast(ChatColor.RED + shooter.getDisplayName() + ChatColor.YELLOW + " a touché " + victim.getDisplayName());
                }
            });
			
			ashooter.addScore(1);
            //shooter.playSound(shooter.getLocation(), "", 1, 1);
            shooter.playSound(shooter.getLocation(), Sound.SUCCESSFUL_HIT, 3, 2);

			if(ashooter.getScore() == Goal)
			{
                eta = Status.Stopping;
				Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
				{
					public void run() {
						win(shooter);
					}
				}, 2);					
			}
			/*Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
			{
				public void run() {
					orderList();
				}
			});*/
			
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private Player getWin(Player p)
	{
		for(APlayer ap : players)
		{
			if(ap.getRole() == Role.Spectator)
				continue;

			if(!ap.getName().equals(p.getName()))
			{
				return ap.getP();
			}
		}
		return null;
	}

	public boolean isWaiting(Player p)
	{
		return isWaiting(p.getUniqueId());
	}

	public boolean isWaiting(UUID uuid)
	{
		if(waitPlayers.contains(uuid))
		{
			return true;
		}

		return false;
	}

	@SuppressWarnings("unused")
	private ItemStack creator(Material m, String name, String[] lore)
	{
		ItemStack lol = new ItemStack(m);

		List<String> l = new ArrayList<String>();
		for(String s : lore)
		{
			l.add(s);
		}

		ItemMeta me = lol.getItemMeta();
		me.setDisplayName(name);
		me.setLore(l);

		lol.setItemMeta(me);

		return lol;
	}

	public void tp(Player p) {
		if(spawn != null)
		{
			p.teleport(getSpawn(p));
		}
	}

	public Location getSpawn(Player p)
	{
		Spawn r = null;
		List<Spawn> spawns = new ArrayList<Spawn>();		
		for(Spawn s : spawn)
		{
			if(r == null)
			{
				r = s;
				continue;
			}

			if(s.getUses() < r.getUses())
				r = s;
		}
		
		for(Spawn s : spawn)
		{
			if(s.getUses() <= r.getUses())
				spawns.add(s);
		}
		Random rr = new Random();
		
		int i = rr.nextInt(spawns.size());
		
		Spawn l = spawns.get(i);
		l.addUse();

		return l.getLoc();
	}

	public APlayer getAplayer(Player p)
	{
		for(APlayer ap : players)
		{
			if(ap.getName().equals(p.getName()))
			{
				return ap;
			}
		}

		return null;
	}
	
	/*public void orderList()
	{
		if(!plugin.isEnabled())
			return;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				Collections.sort(players, new Comparator<APlayer>(){

					public int compare(APlayer o1, APlayer o2) {
						return -Integer.compare(o1.getScore(), o2.getScore());
					}
				});				
				/*Bukkit.getScheduler().runTask(plugin, new Runnable() {
					public void run() {
						updateScorebords();						
					}
				});*/ /*
			}
		});
	}*/
	
	public void updateScore()
	{
		if(players.size() <= 0)
		{
			return;
		}
        sh.requestUpdate();
	}

    public void sortPlayers()
    {
        Collections.sort(players, new Comparator<APlayer>() {

            @Override
            public int compare(APlayer o1, APlayer o2) {
                return -Integer.compare(o1.getScore(), o2.getScore());
            }
        });
    }

    public void refreshScoreBoards()
    {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(APlayer player : players)
                {
                    player.updateScoreboard();
                    //score.getScore(player.getP()).setScore(player.getScore());
                }
            }
        });
    }
	
	public Scoreboard getScoreboard()
	{
		return tboard;
	}
	
	public List<APlayer> getTopFive()
	{
		List<APlayer> r = new ArrayList<APlayer>();
		
		for(int i = 0; i < players.size() && i < 10; i++)
		{
			r.add(players.get(i));
		}
		
		return r;
	}
	
	public int getPositionScore(APlayer p)
	{
		for(int i = 0; i < players.size(); i++)
		{
			APlayer pp = players.get(i);
			if(p.getUUID().equals(pp.getUUID()))
			{
				return i+1;
			}
		}
		
		return players.size();
	}
	
	public void broadcast(String message)
	{
		for(APlayer player : players)
		{
			broadcast(player.getP(), message);
		}
	}

	public void broadcast(Player p, String message)
	{
		p.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "quake" + ChatColor.DARK_AQUA + "] "+ ChatColor.ITALIC + message);
	}

	public void chat(String message)
	{
		for(APlayer player : players)
		{
			player.tell(message);
		}
	}

	public void nbroadcast(String message)
	{
		for(APlayer player : players)
		{
			player.tell(message);
		}
	}

	public void broadcastXP(int xp)
	{
		for(APlayer player : players)
		{
			player.setLevel(xp);
		}
	}

	public void playsound(Sound sound, float a, float b)
	{
		for(APlayer player : players)
		{
			player.getP().playSound(player.getP().getLocation(), sound, a, b);
		}
	}

	public static void leaveCleaner(Player player)
	{
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		return;
	}

	public Collection<Player> getPlayers()
	{
		List<Player> t = new ArrayList<Player>();
		for(APlayer ap : players)
			t.add(ap.getP());

		return t;
	}
	
	public List<Player> getPlayersList()
	{
		List<Player> t = new ArrayList<Player>();
		for(APlayer ap : players)
			t.add(ap.getP());
		
		return t;
	}
	
	public List<APlayer> getAPlayersList()
	{
		return players;
	}

	@SuppressWarnings("deprecation")
	public void cleaner(Player player)
	{
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(20F);
		player.setSaturation(20.0F);
		player.setFoodLevel(20);
		player.getInventory().clear();
		player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
		player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
		player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
		player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
		player.getInventory().setHeldItemSlot(0);
		player.setExp(0);
		player.setLevel(0);

		try{
			player.updateInventory();
		}catch(Exception e)
		{/*LOL*/}

		return;
	}

	public void giveEffect(Player player)
	{
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		for(PotionEffect popo : potions)
		{
			player.addPotionEffect(popo);
		}
	}

	public ItemStack getLeaveDoor()
	{
		ItemStack coucou = new ItemStack(Material.WOOD_DOOR, 1);

		ItemMeta coucou_meta = coucou.getItemMeta();

		coucou_meta.setDisplayName(ChatColor.GOLD + "Quitter l'arène " + ChatColor.GRAY + "(Clique-Droit)");
		coucou.setItemMeta(coucou_meta);

		return coucou;
	}

	public void startCountdown()
	{
		eta = Status.Starting;

		CountDown = new Starter(plugin, this, Time_Before);

		CountDown.start();		
	}

	public void resetCountdown()
	{
		eta = Status.Available;

		if(CountDown != null)
		{
			CountDown.abord();
			broadcast(ChatColor.YELLOW + "Compte à rebour remis à zero.");
		}		
	}


	public void joinHider(Player p)
	{
		for(Player pp : Quake.getOnline())
		{	
			if(!this.hasPlayer(pp))
			{
				pp.hidePlayer(p);
				p.hidePlayer(pp);
			}else
			{
				pp.showPlayer(p);
				p.showPlayer(pp);
			}
		}
	}

	public void loseHider(Player p)
	{
		for(APlayer ap : players)
		{
			if(ap.getRole() != Role.Spectator)
			{
				ap.getP().hidePlayer(p);
			}else if(ap.getRole() == Role.Spectator)
			{
				ap.getP().showPlayer(p);
				p.showPlayer(ap.getP());
			}
		}
	}

	public UUID getUUID()
	{
		return uuid;
	}

	public boolean hasPlayer(UUID uuid)
	{
		return hasPlayer(Bukkit.getPlayer(uuid));
	}

	public boolean hasPlayer(Player p)
	{
		return hasPlayer(p.getName());
	}

	public boolean hasPlayer(String p)
	{
		for(APlayer ap : players)
		{
			if(ap.getName().equals(p))
			{
				return true;
			}
		}

		return false;
	}

	public String getName()
	{
		return name;
	}

	public boolean isVip()
	{
		return vip;
	}

	public int getMinPlayers()
	{
		return minPlayer;
	}

	@Override
	public int countGamePlayers() {
		return this.getActualPlayers();
	}

	public int getMaxPlayers()
	{
		return maxPlayer;
	}

	@Override
	public int getTotalMaxPlayers() {
		return this.getMaxPlayers();
	}

	@Override
	public int getVIPSlots() {
		return this.vipSlots;
	}

	@Override
	public Status getStatus() {
		return eta;
	}

	@Override
	public void setStatus(Status status) {
		this.eta = status;
	}

	public int getActualPlayers()
	{
		int nb = 0;

		for(APlayer ap : players)
		{
			if(ap.getRole() == Role.Player)
			{
				nb++;
			}
		}

		return nb;
	}

	/*public void updateScorebords()
	{
		for(APlayer ap : players)
		{
			ap.updateScoreboard();
		}
	}*/

	public void initScorebords()
	{
		for(APlayer ap : players)
		{
			ap.setScoreboard();
		}
	}

	public String getMapName()
	{
		return Map_name;
	}

	public boolean isFamous() {
		return false;
	}

	public int countPlayersIngame() {
		return this.getActualPlayers();
	}

	public int getTimeBefore()
	{
		return Time_Before;
	}

	public int getTimeAfter()
	{
		return Time_After;
	}

	public void setVip(boolean vip)
	{
		this.vip = vip;
	}

	public void setMinPlayers(int nb)
	{
		minPlayer = nb;
	}

	public void setMaxPlayers(int nb)
	{
		maxPlayer = nb;
	}

	public void setMapName(String name)
	{
		Map_name = name;
	}

	public void setTimeBefore(int time)
	{
		Time_Before = time;
	}

	public void setTimeAfter(int time)
	{
		Time_After = time;
	}

	public void addSpawn(Location loc)
	{
		spawn.add(new Spawn(loc));
	}

	public int getCountDownRemain()
	{
		if(CountDown == null)
			return 0;

		return this.CountDown.time;
	}

	public class Starter implements Runnable
	{

		public int time = 0;
		
		public int tt = 0;

		private Quake plugin;

		private Arena arena;

		private int ID;

		public Starter(Quake pl, Arena aren, int time)
		{
			this.time = time;
			this.tt = time;
			plugin = pl;
			arena = aren;

		}

		public void abord()
		{
			time = tt;
			Bukkit.getScheduler().cancelTask(ID);
		}

		public void start()
		{
			arena.broadcast(ChatColor.YELLOW + "Le jeu va démarrer dans " + Time_Before +" sec.");
			arena.broadcastXP(time);

			ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 20L);
		}

		@Override
		public void run() {
			arena.broadcastXP(time);

			if(time == 10)
			{
				arena.broadcast(ChatColor.YELLOW + "Le jeu va démarrer dans 10 sec.");
				arena.playsound(Sound.NOTE_PLING, 0.6F, 50F);
			}

			if(time  <= 5 && time >=1)
			{
				arena.broadcast(ChatColor.YELLOW + "Le jeu va démarrer dans " + time + " sec.");
				arena.playsound(Sound.NOTE_PLING, 0.6F, 50F);
			}

			if(time == 0)
			{
				arena.playsound(Sound.NOTE_PLING, 9.0F, 1F);
				arena.playsound(Sound.NOTE_PLING, 9.0F, 5F);
				arena.playsound(Sound.NOTE_PLING, 9.0F, 10F);
				arena.broadcast(ChatColor.YELLOW + "C'est parti !");
				arena.start();
				abord();
			}

			if(time <= 0)
			{				
				abord();
			}
			time--;
		}

	}

	public Color getColor(int i) {
		Color c = null;
		if(i==1){
			c=Color.AQUA;
		}
		if(i==2){
			c=Color.BLACK;
		}
		if(i==3){
			c=Color.BLUE;
		}
		if(i==4){
			c=Color.FUCHSIA;
		}
		if(i==5){
			c=Color.GRAY;
		}
		if(i==6){
			c=Color.GREEN;
		}
		if(i==7){
			c=Color.LIME;
		}
		if(i==8){
			c=Color.MAROON;
		}
		if(i==9){
			c=Color.NAVY;
		}
		if(i==10){
			c=Color.OLIVE;
		}
		if(i==11){
			c=Color.ORANGE;
		}
		if(i==12){
			c=Color.PURPLE;
		}
		if(i==13){
			c=Color.RED;
		}
		if(i==14){
			c=Color.SILVER;
		}
		if(i==15){
			c=Color.TEAL;
		}
		if(i==16){
			c=Color.WHITE;
		}
		if(i==17){
			c=Color.YELLOW;
		}

		return c;
	}
}
