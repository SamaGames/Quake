package com.Geekpower14.quake.listener;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.APlayer;
import com.Geekpower14.quake.arena.APlayer.Role;
import com.Geekpower14.quake.arena.Arena;
import com.Geekpower14.quake.stuff.TItem;
import net.samagames.gameapi.events.FinishJoinPlayerEvent;
import net.samagames.gameapi.json.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener{

	private Quake plugin;

	public PlayerListener(Quake pl)
	{
		plugin = pl;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerJoinEvent event)
	{
		event.setJoinMessage("");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFinishJoinPlayer(FinishJoinPlayerEvent event)
	{
		if(!event.isCancelled())
		{
			Player p = Bukkit.getPlayer(event.getPlayer());
			Arena arena = plugin.arenaManager.getArena();

			if (arena == null)
			{
				event.refuse("arène invalide !");
			}

			arena.joinArena(p);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player p = event.getPlayer();
		event.setQuitMessage("");
		Arena arena = plugin.arenaManager.getArena();

		if(arena == null)
			return;

		arena.leaveArena(p);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent event)
	{
		Player p = event.getPlayer();

		event.setLeaveMessage("");

		Arena arena = plugin.arenaManager.getArena();
		if(arena == null)
			return;
		
		arena.leaveArena(p);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		final Arena arena = plugin.arenaManager.getArena();

		if(arena == null)
		{
			return;
		}

		event.getRecipients().clear();
		event.getRecipients().addAll(arena.getPlayers());
		//arena.chat(player.getDisplayName()+ ChatColor.GRAY + ": " + event.getMessage());		   

		return;   
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) 
	{
		final Player player = event.getPlayer();

		Action action = event.getAction();

		ItemStack hand = player.getItemInHand();

		final Arena arena = plugin.arenaManager.getArenabyPlayer(player);

		if(arena == null)
		{
            if(!player.isOp())
                player.kickPlayer("");
			return;
		}

		APlayer ap = arena.getAplayer(player);

		event.setCancelled(true);

		if(hand != null
				&& hand.getType() == Material.WOOD_DOOR)
			arena.kickPlayer(player);


		if(ap.getRole() == Role.Spectator)
			return;

		if(arena.eta != Status.InGame)
			return;

		TItem item = ap.getStuff(player.getInventory().getHeldItemSlot());
		if(item == null)
			return;

		if(action == Action.LEFT_CLICK_AIR
				|| action == Action.LEFT_CLICK_BLOCK)
			item.leftAction(ap, ap.getSlot());

		if(action == Action.RIGHT_CLICK_AIR
				|| action == Action.RIGHT_CLICK_BLOCK)
			item.rightAction(ap, ap.getSlot());

	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerFellOutOfWorld(EntityDamageEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player p = (Player) event.getEntity();

			event.setCancelled(true);

			Arena arena = plugin.arenaManager.getArena();

			if(arena == null)
				return;

			if(event.getCause() == EntityDamageEvent.DamageCause.VOID)
			{
				if(arena.eta.isLobby())
				{
					p.teleport(arena.getSpawn(p));
					return;
				}

				APlayer ap = arena.getAplayer(p);

				if(ap.getRole() == Role.Spectator)
				{
					p.teleport(arena.getSpawn(p));
					return;
				}
				p.teleport(arena.getSpawn(p));
			}
		}

		return;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerSneak(PlayerToggleSneakEvent event)
	{
		event.setCancelled(true);
		return;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		final Player player = event.getEntity();

		final Arena arena = plugin.arenaManager.getArenabyPlayer(event.getEntity());
		if(arena == null)
		{
			return;
		}

		final APlayer ap = arena.getAplayer(player);

		ap.setinvincible(true);
		ap.setReloading(false);

		event.setDeathMessage("");
		event.getDrops().clear();
		event.setDroppedExp(0);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{ 
			public void run() 
			{
				try {
					Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
					Object packet = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".PacketPlayInClientCommand").newInstance();
					Class<?> enumClass = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EnumClientCommand");

					for(Object ob : enumClass.getEnumConstants()){
						if(ob.toString().equals("PERFORM_RESPAWN")){
							packet = packet.getClass().getConstructor(enumClass).newInstance(ob);
						}
					}

					Object con = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
					con.getClass().getMethod("a", packet.getClass()).invoke(con, packet);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}, 15L);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		final Arena arena = plugin.arenaManager.getArenabyPlayer(event.getPlayer());
		if(arena == null)
		{
			return;
		}
		final Player p = event.getPlayer();
		APlayer ap = arena.getAplayer(p);

		ap.giveStuff();

		event.setRespawnLocation(arena.getSpawn(p));

        Long timetoRespawn = 30L + Quake.msToTick(Quake.getPing(p));

		ap.setInvincible(timetoRespawn);
        ap.setReloading(timetoRespawn);

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				arena.giveEffect(p);
			}
		}, 5L);

	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerClickInventory(InventoryClickEvent event)
	{
		Arena arena = plugin.arenaManager.getArenabyPlayer((Player) event.getWhoClicked());
		if(arena == null)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerOpenInventory(InventoryOpenEvent event)
	{
		Arena arena = plugin.arenaManager.getArenabyPlayer((Player)event.getPlayer());
		if(arena == null)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerDrop(PlayerDropItemEvent event)
	{
		Arena arena = plugin.arenaManager.getArenabyPlayer(event.getPlayer());
		if(arena == null)
		{
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerPickup(PlayerPickupItemEvent event)
	{
		Arena arena = plugin.arenaManager.getArenabyPlayer(event.getPlayer());
		if(arena == null)
		{
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerFood(FoodLevelChangeEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			event.setCancelled(true);
			event.setFoodLevel(20);
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event)
	{
		World w = event.getWorld();

		if(event.toWeatherState())
		{
			event.setCancelled(true);
		}

	}
}
