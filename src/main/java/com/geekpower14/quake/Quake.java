package com.geekpower14.quake;

import com.geekpower14.quake.arena.ArenaManager;
import com.geekpower14.quake.commands.CommandsManager;
import com.geekpower14.quake.listener.PlayerListener;
import com.geekpower14.quake.stuff.ItemManager;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamesNames;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Quake extends JavaPlugin{

	private static Quake plugin;

	private Logger log;
	private ArenaManager arenaManager;
	private CommandsManager commandsManager;
	private ItemManager itemManager;

	private SamaGamesAPI samaGamesAPI;

	private String type = "solo";

	public static Quake getPlugin() {
		return plugin;
	}

	public static Boolean hasPermission(Player p, String perm)
	{
		return perm.equalsIgnoreCase("") || p.isOp() || p.hasPermission(perm);
	}

    public static List<Player> getOnline() {
        List<Player> list = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            list.addAll(world.getPlayers());
        }
        return Collections.unmodifiableList(list);
    }

    public static int getPing(Player p)
    {
        CraftPlayer cp = (CraftPlayer) p;
        EntityPlayer ep = cp.getHandle();

        return ep.ping;
    }
	
    public static int msToTick(int ms)
    {
        return (ms*20)/1000;
    }

	public void onEnable()
	{
		log = getLogger();
		plugin = this;

		samaGamesAPI = SamaGamesAPI.get();

		this.getServer().getWorld("world").setAutoSave(false);

		type = getConfig().getString("Type", "solo");

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		arenaManager = new ArenaManager(this);

		itemManager = new ItemManager();

		commandsManager = new CommandsManager(this);

		getCommand("q").setExecutor(commandsManager);

		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		samaGamesAPI.getStatsManager().setStatsToLoad(GamesNames.QUAKE, true);
		samaGamesAPI.getGameManager().setKeepPlayerCache(true);
		log.info("quake enabled!");
	}

	public void onDisable()
	{
		//TabTask.cancel();
		arenaManager.disable();

		log.info("quake disabled!");
	}

	public SamaGamesAPI getSamaGamesAPI()
	{
		return samaGamesAPI;
	}

	public ArenaManager getArenaManager()
	{
		return arenaManager;
	}

	public ItemManager getItemManager()
	{
		return itemManager;
	}

	public String getType()
	{
		return type;
	}
}
