package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.stuff.TItem;
import com.Geekpower14.quake.utils.Utils;
import com.Geekpower14.quake.utils.Utils.ItemSlot;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.games.themachine.messages.IMessageManager;
import net.samagames.api.shops.IPlayerShop;
import net.samagames.api.shops.IShopsManager;
import net.samagames.tools.scoreboards.VObjective;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class APlayer extends GamePlayer{

	private Quake plugin;

	private Arena arena;

	private Player p;

	private Role role = Role.Player;

	private boolean Reloading = false;

	private Boolean Invincible = false;

    private VObjective objective;

	private int killstreak = 0;

	private int score = 0;

	private HashMap<ItemSlot, TItem> stuff = new HashMap<>();

	private BukkitTask[] reloadTasks = null;
	
	public APlayer(Quake pl, Arena arena, Player p)
	{
		super(p);
		plugin = pl;
		this.arena = arena;
		this.p = p;

        objective = new VObjective("quake", "" + ChatColor.RED + ChatColor.BOLD + "Quake");
        objective.addReceiver(p);

		resquestStuff();

	}

    public void resquestStuff()
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run()
            {
                IShopsManager shopsManager = SamaGamesAPI.get().getShopsManager();
                IPlayerShop player = shopsManager.getPlayer(getUUID());

                //Shooter
                try {
                    TItem itemByName = plugin.getItemManager().getItemByID(player.getSelectedItemFromList(new int[]{81, 82, 83, 84, 85, 86, 87}), 81);
                    stuff.put(ItemSlot.Slot1, itemByName);
                } catch (Exception ignored) {
                    TItem itemByName = plugin.getItemManager().getItemByID(81);
                    stuff.put(ItemSlot.Slot1, itemByName);
                }

                //Grenade
                try {
                    TItem itemByName = plugin.getItemManager().getItemByID(player.getSelectedItemFromList(new int[]{90, 91, 92, 93, 94, 95}), 90);
                    stuff.put(ItemSlot.Slot2, itemByName);
                } catch (Exception ignored) {
                    TItem itemByName = plugin.getItemManager().getItemByID(90);
                    stuff.put(ItemSlot.Slot2, itemByName);
                }
            }
        });
    }

    public void giveStuff()
    {
        for(Map.Entry<ItemSlot, TItem> entry : stuff.entrySet())
        {
            TItem item = entry.getValue();

			if(item == null)
				continue;

			p.getInventory().setItem(entry.getKey().getSlot(), item.getItem());
		}

		arena.extraStuf(this);

		p.updateInventory();
	}

	public TItem getStuff(int i)
	{
		for(ItemSlot is : ItemSlot.values())
		{
			if(i == is.getSlot())
				return stuff.get(is);
		}
		
		return null;
	}

	public ItemSlot getSlot()
	{
		int i = p.getInventory().getHeldItemSlot();
		for(Utils.ItemSlot is : Utils.ItemSlot.values())
		{
			if(i == is.getSlot())
				return is;
		}

		return null;
	}

	public void updateScoreboard()
	{
		if(this.getArena() instanceof ArenaSolo)
		{

			ArenaSolo arenaSolo = (ArenaSolo)arena;
            objective.clearScores();
            List<APlayer> sortedList = arenaSolo.getScoreHandler().getSortedList();

            for(int i = 0; i < Math.min(8, sortedList.size()); i++)
            {
                APlayer aPlayer = sortedList.get(i);
                objective.getScore(aPlayer.getName()).setScore(aPlayer.getScore());
            }
			objective.getScore(getName()).setScore(getScore());
            objective.updateScore(true);
        }

        setLevel(this.getScore());

		/*if(this.getArena() instanceof ArenaTeam)
		{
			setLevel(this.getScore());
		}*/
	}

	public void setReloading(Long Ticks)
	{
		if (reloadTasks != null)
			for (BukkitTask task : reloadTasks)
				if (task != null)
					task.cancel();
		
		Reloading = true;

		final Long temp = Ticks;

		p.setExp(0);
		
		final BukkitTask infoxp = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            float xp = p.getExp();
            xp += getincr(temp);
            if (xp >= 1) {
                xp = 1;
            }
            p.setExp(xp);
        }, 0L, 2L);

		BukkitTask cancelTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
			p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 10);
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
	            Reloading = false;
	            arena.sendBarTo(p, "" +ChatColor.BOLD + ChatColor.GREEN +"►██████ Rechargé ██████◄");
	            p.setExp(1);
	            infoxp.cancel();
	            reloadTasks = null;
	            //p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
	            Bukkit.getScheduler().runTaskLater(plugin, () -> arena.sendBarTo(p, ""), 5);
	        }, 5);
		}, Ticks - 5);

		reloadTasks = new BukkitTask[]{infoxp, cancelTask};
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

        final BukkitTask infoxp = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            float xp = p.getExp();
            xp += getincr(temp);
            if (xp >= 1) {
                xp = 1;
            }
            p.setExp(xp);
        }, 0L, 2L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Invincible = false;
            p.setExp(1);
            infoxp.cancel();
        }, Ticks);
    }

    public float getincr(Long time)
    {
        float result;

        float temp = time;

        result = (100 / (temp / 2)) / 100;


        return result;
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

	public int getKillstreak()
	{
		return killstreak;
	}

	public void setKillstreak(int killstreak)
	{
		this.killstreak = killstreak;
	}

	public void addKill()
	{
		killstreak++;

		String message = "";
		switch(killstreak)
		{
			case 3:
				message = p.getDisplayName() + ChatColor.GOLD + " est recherché par la police du quartier !";
				break;
			case 5:
				message = ChatColor.GOLD + "Une enquête est lancée sur " + p.getDisplayName() + ChatColor.GOLD + " !";
				break;
			case 8:
				message = ChatColor.GOLD + "Le GIGN est à la poursuite de " + p.getDisplayName() + ChatColor.GOLD + " !";
				break;
			case 13:
				message = ChatColor.GOLD + "Le raid essaye d'encercler " + p.getDisplayName() + ChatColor.GOLD + " !";
				break;
			case 16:
				message = ChatColor.GOLD + "Le BRI veux mettre la main sur " + p.getDisplayName() + ChatColor.GOLD + " !";
				break;
			case 20:
				message = ChatColor.GOLD + "Maïte a envoyé un cookie de soutient à " + p.getDisplayName() + ChatColor.GOLD + " !";
				break;
			case 25:
				message = p.getDisplayName() + ChatColor.GOLD + " a réussi à semer les forces spéciales !";
				break;
			default:
				break;
		}
		killStreakMessage(message);
	}

	public void killStreakMessage(String message)
	{
		if(message == null || message.isEmpty())
			return;
        IMessageManager messageManager = SamaGamesAPI.get().getGameManager().getCoherenceMachine().getMessageManager();
        messageManager.writeCustomMessage("" + ChatColor.RED + ChatColor.BOLD + killstreak + " kills à la suite !", true);
        messageManager.writeCustomMessage("" + ChatColor.BOLD + message, true);
        messageManager.writeCustomMessage("", false);
	}

	public void hasDiedBy(String player)
	{
		if(killstreak >= 3)
		{
            IMessageManager messageManager = SamaGamesAPI.get().getGameManager().getCoherenceMachine().getMessageManager();
            messageManager.writeCustomMessage("" + ChatColor.BOLD + p.getDisplayName() + ChatColor.GOLD + ChatColor.BOLD + " s'est fait arrêter par " + player, true);
            messageManager.writeCustomMessage("", false);
		}
		killstreak = 0;
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
		addKill();
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
	public Role getRole()
	{
		return role;
	}

	public void setRole(Role r)
	{
		role = r;
	}

	public void setLevel(int xp) {
		p.setLevel(xp);		
	}

	public enum Role{
		Player("Player", 20),
		Spectator("Spectateur", 10);

		private String info;
		private int value;

		Role(String info, int value)
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
