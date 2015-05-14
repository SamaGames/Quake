package com.Geekpower14.quake;

import com.Geekpower14.quake.arena.ArenaManager;
import com.Geekpower14.quake.commands.CommandsManager;
import com.Geekpower14.quake.listener.PlayerListener;
import com.Geekpower14.quake.stuff.ItemManager;
import net.minecraft.server.v1_8_R2.EntityPlayer;
import net.samagames.api.SamaGamesAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Quake extends JavaPlugin{

	public static Quake plugin;

	public Logger log;
	public ArenaManager arenaManager;
	public CommandsManager commandsManager;
	public ItemManager itemManager;

	public SamaGamesAPI samaGamesAPI;

	public String type = "solo";

	public static Quake getPlugin() {
		return plugin;
	}

	public static Boolean hasPermission(Player p, String perm)
	{
		if(perm.equalsIgnoreCase(""))
			return true;
		if(p.isOp())
			return true;
		if(p.hasPermission("Quake.admin"))
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

		this.saveDefaultConfig();

		type = getConfig().getString("Type", "solo");

		String type_ = (type.equals("team"))?"quaketeam":"quake";

		String overrideType = getConfig().getString("OverrideType");

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		arenaManager = new ArenaManager(this);

		itemManager = new ItemManager(this);

		commandsManager = new CommandsManager(this);

		getCommand("q").setExecutor(commandsManager);

		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
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

}
