package com.Geekpower14.quake;

import com.Geekpower14.quake.arena.ArenaManager;
import com.Geekpower14.quake.commands.CommandsManager;
import com.Geekpower14.quake.listener.PlayerListener;
import com.Geekpower14.quake.stuff.ItemManager;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.samagames.api.SamaGamesAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
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
		if(perm.equalsIgnoreCase(""))
			return true;
		if(p.isOp())
			return true;
		if(p.hasPermission(perm))
			return true;

		return false;
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

		Bukkit.getWorld("world").setAutoSave(false);

		type = getConfig().getString("Type", "solo");

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		arenaManager = new ArenaManager(this);

		itemManager = new ItemManager();

		commandsManager = new CommandsManager(this);

		getCommand("q").setExecutor(commandsManager);

		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		samaGamesAPI.getGameManager().disableNature();
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
