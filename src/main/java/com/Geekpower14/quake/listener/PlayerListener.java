package com.Geekpower14.quake.listener;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.APlayer;
import com.Geekpower14.quake.arena.APlayer.Role;
import com.Geekpower14.quake.arena.ATeam;
import com.Geekpower14.quake.arena.Arena;
import com.Geekpower14.quake.arena.ArenaTeam;
import com.Geekpower14.quake.stuff.TItem;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import org.bukkit.material.Wool;

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

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		/*Player player = event.getPlayer();
		final Arena arena = plugin.arenaManager.getArena();

		if(arena == null)
		{
			return;
		}

		event.getRecipients().clear();
		event.getRecipients().addAll(arena.getPlayers());*/
		//arena.chat(player.getDisplayName()+ ChatColor.GRAY + ": " + event.getMessage());		   

		return;   
	}

	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) 
	{
		final Player player = event.getPlayer();

		Action action = event.getAction();

		ItemStack hand = player.getItemInHand();

		final Arena arena = plugin.getArenaManager().getArenabyPlayer(player);

		if(arena == null)
		{
            if(!player.isOp())
                player.kickPlayer("");
			return;
		}

		final APlayer ap = arena.getAplayer(player);

		event.setCancelled(true);

		if(hand != null
				&& hand.getType() == Material.WOOD_DOOR
				&& (action == Action.LEFT_CLICK_AIR
				|| action == Action.LEFT_CLICK_BLOCK
				|| action == Action.RIGHT_CLICK_AIR
				|| action == Action.RIGHT_CLICK_BLOCK))
			SamaGamesAPI.get().getGameManager().kickPlayer(player, "");

		//TEAM
		if(hand != null
				&& hand.getType() == Material.WOOL
				&& (action == Action.LEFT_CLICK_AIR
				|| action == Action.LEFT_CLICK_BLOCK
				|| action == Action.RIGHT_CLICK_AIR
				|| action == Action.RIGHT_CLICK_BLOCK))
		{
			if(!arena.isTeam())
				return;
			final ArenaTeam arenaTeam = (ArenaTeam) arena;

			if(!arena.getStatus().isAllowJoin())
				return;

			ATeam at = arenaTeam.getTeamByColor(DyeColor.getByWoolData(hand.getData().getData()));
			if(at == null)
				return;

			arenaTeam.changeTeam(player, at.getName());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> arenaTeam.setWoolStuff(ap), 1L);
			return;
		}

		//ALL

		if(ap.getRole() == Role.Spectator)
			return;

		if(arena.getStatus() != Status.IN_GAME)
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

			Arena arena = plugin.getArenaManager().getArena();

			if(arena == null)
				return;

			if(event.getCause() == EntityDamageEvent.DamageCause.VOID)
			{
				if(arena.getStatus().isAllowJoin())
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
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if(event.getFrom().getBlock().equals(event.getTo().getBlock()))
			return;

		final Player p = event.getPlayer();
		if(p.isDead())
			return;

		final Arena arena = plugin.getArenaManager().getArena();

		if(arena == null)
			return;
		if(!arena.isTeam())
			return;

		final ArenaTeam arenaTeam = (ArenaTeam) arena;
		APlayer ap = arena.getAplayer(p);

		if(ap.isInvincible())
			return;
		if(arena.getStatus() != Status.IN_GAME)
			return;

		// For all.

		if(ap.getRole() == Role.Spectator)
			return;

		final Location loc = p.getLocation();

		for(int i = 1; i <= 5; i++)
		{
			Location tmp = loc.subtract(0, i, 0);
			Block b = tmp.getBlock();
			if(b.getState().getData() instanceof Wool)
			{
				//plugin.log.info("stand on wool !");
				DyeColor color = ((Wool)b.getState().getData()).getColor();
				ATeam pt = arenaTeam.getTeam(p);
				ATeam tt = arenaTeam.getTeamByColor(color);
				if(tt == null || !tt.isActive())
				{
					return;
				}

				if(!pt.isBlockTeam(b))
				{
					//plugin.log.info("stand not good team !");
					arena.kill(p);
					ATeam at = arenaTeam.getTeamByColor(color);
					SamaGamesAPI.get().getGameManager().getCoherenceMachine().getMessageManager().writeCustomMessage(pt.getColor() + p.getName() + ChatColor.YELLOW + " est entré dans le spawn de la team " + at.getColor() + at.getName(), true);
					at.addScore(1);
					arena.updateScore();
					return;
				}
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

		final Arena arena = plugin.getArenaManager().getArenabyPlayer(event.getEntity());
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

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
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
        }, 15L);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		final Arena arena = plugin.getArenaManager().getArenabyPlayer(event.getPlayer());
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

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> arena.giveEffect(p), 5L);

	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerClickInventory(InventoryClickEvent event)
	{
		Arena arena = plugin.getArenaManager().getArenabyPlayer((Player) event.getWhoClicked());
		if(arena == null)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerOpenInventory(InventoryOpenEvent event)
	{
		Arena arena = plugin.getArenaManager().getArenabyPlayer((Player)event.getPlayer());
		if(arena == null)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerDrop(PlayerDropItemEvent event)
	{
		Arena arena = plugin.getArenaManager().getArenabyPlayer(event.getPlayer());
		if(arena == null)
		{
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerPickup(PlayerPickupItemEvent event)
	{
		Arena arena = plugin.getArenaManager().getArenabyPlayer(event.getPlayer());
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
		if(event.toWeatherState())
		{
			event.setCancelled(true);
		}

	}
}
