package com.Geekpower14.quake.stuff.grenade;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.APlayer;
import com.Geekpower14.quake.arena.Arena;
import com.Geekpower14.quake.stuff.TItem;
import com.Geekpower14.quake.utils.ParticleEffects;
import com.Geekpower14.quake.utils.StatsNames;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by charles on 10/08/2014.
 */
public abstract class GrenadeBasic extends TItem {

    public FireworkEffect effect;

    public double aim = 1.5;

    public double timeBeforeExplode = 1;

    public int currentNumber = 0;

    public GrenadeBasic(String name, String display, Long reload, FireworkEffect e) {
        super(name, display, 1, reload);
        effect = e;
    }

    protected void basicShot(final Player player, APlayer.ItemSLot slot)
    {
        final Arena arena = plugin.arenaManager.getArenabyPlayer(player);

        if(arena == null)
        {
            return;
        }

        final APlayer ap = arena.getAplayer(player);

        ItemStack gStack = player.getInventory().getItem(slot.getSlot());

        if(gStack == null || gStack.getAmount() <= 0)
        {
            return;
        }

        gStack.setAmount(gStack.getAmount()-1);
        player.getInventory().setItem(slot.getSlot(), gStack);
        player.updateInventory();

        player.getWorld().playSound(player.getLocation(), Sound.STEP_SNOW, 3F, 2.0F);

        int typeID = EntityType.CREEPER.ordinal();
        ItemStack stack = new ItemStack(Material.MONSTER_EGG, 1, (short) typeID);

        final Item grenad = player.getWorld().dropItem(player.getEyeLocation(), stack);

        grenad.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(1.5));
        grenad.setPickupDelay(999999);
        new BukkitRunnable() {

            public double time = timeBeforeExplode;

            public Item item = grenad;

            @Override
            public void run() {

                for(Player p : Quake.getOnline())
                {
                    if(time%2 == 0)
                    {
                        p.getWorld().playSound(item.getLocation(), Sound.NOTE_STICKS, 0.5F, 1.5F);
                    }else
                    {
                        p.getWorld().playSound(item.getLocation(), Sound.NOTE_STICKS, 0.5F, 0.5F);
                    }
                    if(item != null && item.isOnGround())
                    {
                        try {
                            if(player.getLocation().getWorld() == item.getLocation().getWorld()
                                    && player.getLocation().distance(item.getLocation()) < 50)
                            {
                                ParticleEffects.FIREWORKS_SPARK.sendToPlayer(player, item.getLocation(), 1F, 2F, 1F, 0.00005F, 2);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(item == null || item.isDead())
                {
                    this.cancel();
                    return;
                }

                if(time <= 0)
                {
                    explode(this, ap, item);
                    //this.cancel();
                    return;
                }

                time-=0.25;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 5L);

        return;
    }

    public void explode(BukkitRunnable br, final APlayer ap, final Item item)
    {
        final Arena arena = ap.getArena();
        Bukkit.getScheduler().cancelTask(br.getTaskId());
        //br.cancel();

        if(item == null || item.isDead())
            return;

        int compte = 0;
        for(Entity entity : item.getNearbyEntities(5, 4, 5))
        {
            if(entity instanceof Player)
            {
                Player target = (Player) entity;

                if(arena.shotplayer(ap.getP(), target, this.effect))
                {
                    compte++;
                }
            }
        }
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    arena.fplayer.playFirework(item.getWorld(), item.getLocation(), effect);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                item.remove();
            }
        });

        final int tt = compte;
        if (tt >= 1) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
            {
                public void run() {
                    try{
                        int up = CoinsManager.syncCreditJoueur(ap.getP().getUniqueId(), tt * 1, true, true);
                        ap.setCoins(ap.getCoins() + up);
                        StatsApi.increaseStat(ap.getP().getUniqueId(), StatsNames.GAME_NAME, StatsNames.KILL, tt);
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            arena.updateScore();
        }
    }

    @Override
    public void setNB(int nb)
    {
        this.currentNumber = nb;
        this.nb = nb;
    }

    public int getCurrentNumber()
    {
        return currentNumber;
    }

    public void setCurrentNumber(int nb)
    {
        currentNumber = nb;
    }

    public void leftAction(APlayer p, APlayer.ItemSLot slot) {
        return;
    }

    public void rightAction(APlayer ap, APlayer.ItemSLot slot) {
        basicShot(ap.getP(), slot);
    }

    @Override
    public GrenadeBasic clone() {
        GrenadeBasic o = null;
        o = (GrenadeBasic) super.clone();
        return o;
    }

}
