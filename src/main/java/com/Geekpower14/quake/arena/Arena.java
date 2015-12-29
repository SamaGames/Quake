package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.utils.StatsNames;
import com.Geekpower14.quake.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Game;
import net.samagames.api.games.IGameProperties;
import net.samagames.api.games.Status;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;


public abstract class Arena extends Game<APlayer> {

	public Quake plugin;

	/*** Constantes ***/
	public String Map_name;

	public int minPlayer = 4;
	public int maxPlayer = 24;

	public int goal = 25;

	public List<PotionEffect> potions = new ArrayList<>();

	public String warningJoinMessage = null;

	/*** Variable dynamiques ***/

	//public List<APlayer> players = new ArrayList<>();

	public Arena(Quake pl)
	{
		super("quake","Quake", "", APlayer.class);
		plugin = pl;
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
        IGameProperties properties = SamaGamesAPI.get().getGameManager().getGameProperties();

		Map_name = properties.getMapName();

		minPlayer = properties.getMinSlots();
		maxPlayer = properties.getMaxSlots();

		goal = properties.getOption("Goal", new JsonPrimitive(25)).getAsInt();

		warningJoinMessage = properties.getOption("WarningJoinMessage", new JsonPrimitive("")).getAsString();

        JsonArray potionsDefault = new JsonArray();
        potionsDefault.add(new JsonPrimitive("SPEED:2"));
        potionsDefault.add(new JsonPrimitive("JUMP:1"));

        JsonArray potions = properties.getOption("Potions", potionsDefault).getAsJsonArray();

        List<PotionEffect> l = new ArrayList<>();
        for(JsonElement data : potions)
        {
            l.add(Utils.strToPo(data.getAsString()));
        }
		this.potions = l;

		toConfigLoad();
	}

	protected abstract void toConfigLoad();

	/*** Mouvement des joueurs ***/

	@Override
	public void handleLogin(Player p)
	{
		p.setFlying(false);
		p.setAllowFlight(false);
		cleaner(p);

		APlayer ap = new APlayer(plugin, this, p);
		gamePlayers.put(p.getUniqueId(), ap);

        this.coherenceMachine.getMessageManager().writePlayerJoinToAll(p);
		execJoinPlayer(ap);

		refresh();

		p.getInventory().setItem(8, this.getLeaveDoor());
		p.getInventory().setHeldItemSlot(0);
		try{
			p.updateInventory();
		}catch(Exception e)
		{/*LOL*/}

		if(warningJoinMessage != null && !warningJoinMessage.isEmpty())
		{
			p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + warningJoinMessage);
		}
	}

	protected abstract void execJoinPlayer(APlayer ap);

	/*
	 * Gestion de l'arène.
	 */

	@Override
	public void handleLogout(Player p)
	{
		p.setAllowFlight(false);
        leaveCleaner(p);

        APlayer ap = getAplayer(p);

        execLeavePlayer(ap);

        super.handleLogout(p);

		execAfterLeavePlayer();

		return;
	}

	protected abstract void execLeavePlayer(APlayer ap);

	protected abstract void execAfterLeavePlayer();

	@Override
	public void startGame()
	{
		setStatus(Status.IN_GAME);

		execStart();

		updateScore();

		super.startGame();
	}

	protected abstract void execStart();

	public void handleGameEnd()
	{
        execStop();
        super.handleGameEnd();
	}

	protected abstract void execStop();

	public void refresh()
	{
		plugin.getSamaGamesAPI().getGameManager().refreshArena();
	}

	public void disable()
	{
		handleGameEnd();
		return;
	}

	public void win(Object p)
	{
		setStatus(Status.FINISHED);

		execWin(p);

        handleGameEnd();
	}

	protected abstract void execWin(Object p);

	public void kill(final Player p)
	{
		final APlayer ap = getAplayer(p);
		ap.setinvincible(true);
		ap.setReloading(true);

		Bukkit.getScheduler().runTask(plugin, () -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3));
        });

		//BETA
		Bukkit.getScheduler().runTaskLater(plugin, () -> {

            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 35, 3));

            p.teleport(getSpawn(p));
            ap.giveStuff();

            Long timetoRespawn = 35L + Quake.msToTick(Quake.getPing(p));

            ap.setInvincible(timetoRespawn+10L);
            ap.setReloading(timetoRespawn);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> giveEffect(p), 5L);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getSamaGamesAPI().getStatsManager(getGameCodeName()).increase(p.getUniqueId(), StatsNames.DEATH, 1));
        }, 5L);

	}

	public boolean shotplayer(final Player shooter, final Player victim, final FireworkEffect effect)
	{
        if(getStatus() == Status.REBOOTING)
            return false;

		return execShotPlayer(shooter, victim, effect);
	}

	protected abstract boolean execShotPlayer(Player shooter, Player victim, FireworkEffect effect);

	protected ItemStack creator(Material m, String name, String[] lore)
	{
		ItemStack lol = new ItemStack(m);

		List<String> l = new ArrayList<>();
		Collections.addAll(l, lore);

		ItemMeta me = lol.getItemMeta();
		me.setDisplayName(name);
		me.setLore(l);

		lol.setItemMeta(me);

		return lol;
	}

	public abstract void tp(Player p);

	public abstract Location getSpawn(Player p);

	public APlayer getAplayer(Player p) {
        return getAplayer(p.getUniqueId());
	}
	
	public APlayer getAplayer(UUID p)
	{
		return gamePlayers.get(p);
	}

	public abstract void extraStuf(APlayer ap);

	public abstract void updateScore();

	public void broadcastXP(int xp)
	{
		for(APlayer player : gamePlayers.values()) {
            player.setLevel(xp);
		}
	}

	public void playsound(Sound sound, float a, float b)
	{
		for(APlayer player : gamePlayers.values())
		{
			player.getP().playSound(player.getP().getLocation(), sound, a, b);
		}
	}
	
	public Collection<APlayer> getAPlayersList()
	{
		return gamePlayers.values();
	}

    public List<Player> getPlayers()
    {
        return gamePlayers.values().stream().map(APlayer::getP).collect(Collectors.toList());
    }

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
		Bukkit.getScheduler().runTask(plugin, () -> {
            for (PotionEffect effect : player.getActivePotionEffects())
                player.removePotionEffect(effect.getType());

            potions.forEach(player::addPotionEffect);
        });
	}

	public ItemStack getLeaveDoor()
	{
		ItemStack coucou = new ItemStack(Material.WOOD_DOOR, 1);

		ItemMeta coucou_meta = coucou.getItemMeta();

		coucou_meta.setDisplayName(ChatColor.GOLD + "Quitter le jeu " + ChatColor.GRAY + "(Clique-Droit)");
		coucou.setItemMeta(coucou_meta);

		return coucou;
	}

	public void sendBar(String message)
	{
		for(APlayer ap : gamePlayers.values())
		{
			sendBarTo(ap.getP(), message);
		}
	}

	public void sendBarTo(Player p, String message)
	{
		IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
		PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte)2);
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(ppoc);
	}

	public int getMinPlayers()
	{
		return minPlayer;
	}

	public void setMinPlayers(int nb)
	{
		minPlayer = nb;
	}

	public int getMaxPlayers()
	{
		return maxPlayer;
	}

	public void setMaxPlayers(int nb)
	{
		maxPlayer = nb;
	}

	/*public int getConnectedPlayers()
	{
		int nb = 0;

		for(APlayer ap : gamePlayers.values())
		{
			if(ap.getRole() == Role.Player)
			{
				nb++;
			}
		}

		return nb;
	}*/

	public boolean isTeam()
	{
		if(this instanceof ArenaTeam)
			return true;

		return false;
	}

	public String getMapName()
	{
		return Map_name;
	}

	public void setMapName(String name)
	{
		Map_name = name;
	}

	public abstract void addSpawn(Location loc);
}
