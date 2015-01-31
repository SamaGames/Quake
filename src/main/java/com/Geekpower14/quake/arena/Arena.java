package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.APlayer.Role;
import com.Geekpower14.quake.utils.StatsNames;
import com.Geekpower14.quake.utils.Utils;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.types.GameArena;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public abstract class Arena implements GameArena {

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

	public List<PotionEffect> potions = new ArrayList<>();

	public boolean vip;

	public String warningJoinMessage = null;

	/*** Variable dynamiques ***/

	public Status eta = Status.Idle;

	public List<APlayer> players = new ArrayList<>();
	//ScoreBoard teams
	protected Scoreboard tboard;
	protected Starter CountDown = null;

	//ScoreBoard teams
	protected Scoreboard tboard;

	protected Team spectates;

	public Arena(Quake pl, String name)
	{
		super();
		plugin = pl;

		this.name = name;

		tboard = Bukkit.getScoreboardManager().getNewScoreboard();

		spectates = tboard.registerNewTeam("spectator");
		spectates.setCanSeeFriendlyInvisibles(true);

	}

	/*
	 * Configuration.
	 */

	public static void leaveCleaner(Player player)
	{
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		return;
	}

	protected void loadConfig()
	{
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "../../world/arenas/"+ name + ".yml"));

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

		warningJoinMessage = config.getString("WarningJoinMessage");

		List<PotionEffect> l = new ArrayList<>();
		for(String popo : config.getStringList("Potions"))
		{
			l.add(Utils.StrToPo(popo));
		}
		potions = l;

		toConfigLoad(config);

		saveConfig();
	}

	protected abstract void toConfigLoad(FileConfiguration config);

	protected FileConfiguration basicConfig(FileConfiguration config)
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

		setDefaultConfig(config, "WarningJoinMessage", "");

		List<String> l = new ArrayList<>();
		l.add("SPEED:2");
		l.add("JUMP:1");
		setDefaultConfig(config, "Potions", l);

		return toBasicConfig(config);
	}

	protected abstract FileConfiguration toBasicConfig(FileConfiguration config);

	public void saveConfig()
	{
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "../../world/arenas/"+ name + ".yml"));

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

		config.set("WarningJoinMessage", warningJoinMessage);

		List<String> l = new ArrayList<String>();
		for(PotionEffect popo : potions)
		{
			l.add(Utils.PoToStr(popo));
		}
		config.set("Potions", l);

		toSaveConfig(config);

		try {
			config.save(new File(plugin.getDataFolder(), "../../world/arenas/"+ name + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract void toSaveConfig(FileConfiguration config);

	protected void setDefaultConfig(FileConfiguration config, String key, Object value) {
		if (!config.contains(key))
			config.set(key, value);
	}

	/*** Mouvement des joueurs ***/

	@SuppressWarnings("deprecation")
	public void joinArena(Player p)
	{

		joinHider(p);
		p.setFlying(false);
		p.setAllowFlight(false);

		cleaner(p);

		APlayer ap = new APlayer(plugin, this, p);
		players.add(ap);

		execJoinPlayer(ap);

		refresh();

		p.getInventory().setItem(8, this.getLeaveDoor());
		p.getInventory().setHeldItemSlot(0);
		try{
			p.updateInventory();
		}catch(Exception e)
		{/*LOL*/}

		if(warningJoinMessage != null && !warningJoinMessage.equals(""))
		{
			p.sendMessage(""+ChatColor.RED + ChatColor.BOLD + warningJoinMessage);
		}
	}

	protected abstract void execJoinPlayer(APlayer ap);

	/*
	 * Gestion de l'arène.
	 */

	public void leaveArena(Player p)
	{
		p.setAllowFlight(false);
		leaveCleaner(p);
		for(Player pp : Quake.getOnline())
		{
			p.showPlayer(pp);
		}

		APlayer ap = getAplayer(p);
		ap.removeScoreboard();

		execLeavePlayer(ap);

		players.remove(ap);

		refresh();

		execAfterLeavePlayer();

		if(players.size() < getMinPlayers() && eta == Status.Starting)
		{
			resetCountdown();
		}

		return;
	}

	protected abstract void execLeavePlayer(APlayer ap);

	protected abstract void execAfterLeavePlayer();

	public void start()
	{
		eta = Status.InGame;

		refresh();

		execStart();

		updateScore();
	}

	protected abstract void execStart();

	public void stop()
	{
		eta = Status.Stopping;
		refresh();
		/*
		 * TO/DO: Stopper l'arène.
		 */

        execStop();

		List<Player> tokick = new ArrayList<>();
		for(APlayer ap : players)
		{
			tokick.add(ap.getP());
		}

		for(Player p : tokick)
		{
			kickPlayer(p);
		}
		refresh();

		if (plugin.isEnabled()) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					Bukkit.getLogger().info(">>>>> RESTARTING <<<<<");
					Bukkit.getLogger().info("server will reboot now");
					Bukkit.getLogger().info(">>>>> RESTARTING <<<<<");
					Bukkit.getServer().shutdown();

				}
			}, 5 * 20L);
		}

	}

	protected abstract void execStop();

	public void refresh()
	{
		GameAPI.getManager().sendArena();
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

	public void win(Object p)
	{
		eta = Status.Stopping;

		refresh();

		execWin(p);
	}

	protected abstract void execWin(Object p);

	public void kill(final Player p)
	{
		final APlayer ap = getAplayer(p);
		ap.setinvincible(true);
		ap.setReloading(true);

		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3));

		//BETA
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {

				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 0));
				p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 35, 3));

				p.teleport(getSpawn(p));
				ap.giveStuff();

				Long timetoRespawn = 35L + Quake.msToTick(Quake.getPing(p));

				ap.setInvincible(timetoRespawn+10L);
				ap.setReloading(timetoRespawn);

				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

					@Override
					public void run() {
						giveEffect(p);
					}
				}, 5L);

				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){

					@Override
					public void run() {
						StatsApi.increaseStat(p.getUniqueId(), StatsNames.GAME_NAME, StatsNames.DEATH, 1);
					}
				});
			}
		}, 5L);

	}

	public abstract void extraStuf(APlayer ap);

	public boolean shotplayer(final Player shooter, final Player victim, final FireworkEffect effect)
	{
        if(eta == Status.Stopping)
            return false;

		return execShotPlayer(shooter, victim, effect);
	}

	protected abstract boolean execShotPlayer(Player shooter, Player victim, FireworkEffect effect);

	@SuppressWarnings("unused")
	protected ItemStack creator(Material m, String name, String[] lore)
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

	public abstract void tp(Player p);

	public abstract Location getSpawn(Player p);

	public APlayer getAplayer(Player p)
	{
		return getAplayer(p.getName());
	}
	
	public APlayer getAplayer(String p)
	{
		for(APlayer ap : players)
		{
			if(ap.getName().equals(p))
			{
				return ap;
			}
		}

		return null;
	}

	public abstract void extraStuf(APlayer ap);

	public abstract void updateScore();
	
	public Scoreboard getScoreboard()
	{
		return tboard;
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
		p.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "Quake" + ChatColor.DARK_AQUA + "] "+ ChatColor.ITALIC + message);
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

	public void sendBar(String message)
	{
		for(APlayer ap : players)
		{
			sendBarTo(ap.getP(), message);
		}
	}

	public void sendBarTo(Player p, String message)
	{
		IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
		PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte)2);
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(ppoc);
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

	public void setVip(boolean vip)
	{
		this.vip = vip;
	}

	public int getMinPlayers()
	{
		return minPlayer;
	}

	public void setMinPlayers(int nb)
	{
		minPlayer = nb;
	}

	@Override
	public int countGamePlayers() {
		return this.getActualPlayers();
	}

	public int getMaxPlayers()
	{
		return maxPlayer;
	}

	public void setMaxPlayers(int nb)
	{
		maxPlayer = nb;
	}

	@Override
	public int getTotalMaxPlayers() {
		return this.getMaxPlayers() + this.getVIPSlots();
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

	public boolean isTeam()
	{
		if(this instanceof ArenaTeam)
			return true;

		return false;
	}

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

	public void setMapName(String name)
	{
		Map_name = name;
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

	public void setTimeBefore(int time)
	{
		Time_Before = time;
	}

	public int getTimeAfter()
	{
		return Time_After;
	}

	public void setTimeAfter(int time)
	{
		Time_After = time;
	}

	public abstract void addSpawn(Location loc);


	public int getCountDownRemain()
	{
		if(CountDown == null)
			return 0;

		return this.CountDown.time;
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
			arena.broadcast(ChatColor.GOLD + "Le jeu va démarrer dans " + Time_Before +" sec.");
			arena.broadcastXP(time);

			ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 20L);
		}

		@Override
		public void run() {
			arena.broadcastXP(time);

			if(time == 10)
			{
				arena.sendBar("" +ChatColor.BOLD + ChatColor.GOLD + "Le jeu va démarrer dans "+ ChatColor.RED + "10" + ChatColor.GOLD +" sec.");
				//arena.broadcast(ChatColor.YELLOW + "Le jeu va démarrer dans 10 sec.");
				arena.playsound(Sound.NOTE_PLING, 0.6F, 50F);
			}

			if(time  <= 5 && time >=1)
			{
				arena.sendBar("" +ChatColor.BOLD + ChatColor.GOLD + "Le jeu va démarrer dans " + ChatColor.RED + time + ChatColor.GOLD + " sec.");
				//arena.broadcast(ChatColor.YELLOW + "Le jeu va démarrer dans " + time + " sec.");
				arena.playsound(Sound.NOTE_PLING, 0.6F, 50F);
			}
			if(time == 1)
			{
				arena.setStatus(Status.InGame);
				refresh();
			}

			if(time == 0)
			{
				arena.playsound(Sound.NOTE_PLING, 9.0F, 1F);
				arena.playsound(Sound.NOTE_PLING, 9.0F, 5F);
				arena.playsound(Sound.NOTE_PLING, 9.0F, 10F);
				arena.sendBar("" +ChatColor.BOLD + ChatColor.GOLD + "C'est parti !");
				//arena.broadcast(ChatColor.YELLOW + "C'est parti !");
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
}
