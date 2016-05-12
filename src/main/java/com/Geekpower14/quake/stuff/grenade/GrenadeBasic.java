package com.Geekpower14.quake.stuff.grenade;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.arena.APlayer;
import com.Geekpower14.quake.utils.Utils.ItemSlot;
import com.Geekpower14.quake.arena.Arena;
import com.Geekpower14.quake.stuff.TItem;
import com.Geekpower14.quake.utils.ParticleEffect;
import com.Geekpower14.quake.utils.StatsNames;
import com.Geekpower14.quake.utils.Utils;
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

public abstract class GrenadeBasic extends TItem {

    public FireworkEffect effect;

    public double timeBeforeExplode = 1;

    public int currentNumber = 0;

    public GrenadeBasic(String name, String display, Long reload, FireworkEffect e) {
        super(name, display, 1, reload);
        effect = e;
    }

    protected void basicShot(final Player player, ItemSlot slot)
    {
        final Arena arena = plugin.getArenaManager().getArenabyPlayer(player);

        if(arena == null)
        {
            return;
        }

        final APlayer ap = arena.getAplayer(player);

        if(ap.isInvincible())
        {
            return;
        }

        ItemStack gStack = player.getInventory().getItem(slot.getSlot());

        if(gStack == null || gStack.getAmount() <= 0)
        {
            return;
        }

        gStack.setAmount(gStack.getAmount() - 1);
        //Update number of grenade
        setNB(gStack.getAmount());

        player.getInventory().setItem(slot.getSlot(), gStack);
        player.updateInventory();

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SNOW_STEP, 3F, 2.0F);

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


                if(item != null && item.isOnGround())
                {
                    try {
                        ParticleEffect.FIREWORKS_SPARK.display(0.07F, 0.04F, 0.07F, 0.00005F, 1, item.getLocation(), 50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for(Player p : Quake.getOnline())
                {
                    if(time % 2 == 0.0F)
                    {
                        p.getWorld().playSound(item.getLocation(), Sound.BLOCK_NOTE_PLING, 0.5F, 1.5F);
                    }else
                    {
                        p.getWorld().playSound(item.getLocation(), Sound.BLOCK_NOTE_PLING, 0.5F, 0.5F);
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
    }

    public void explode(BukkitRunnable br, final APlayer ap, final Item item)
    {
        final Arena arena = ap.getArena();
        Bukkit.getScheduler().cancelTask(br.getTaskId());
        //br.cancel();

        if(item == null || item.isDead())
            return;

        int compte = 0;
        for(Entity entity : item.getNearbyEntities(3, 4, 3))
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
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Utils.launchfw(item.getLocation(), effect);
            } catch (Exception e) {
                e.printStackTrace();
            }
            item.remove();
        });

        final int tt = compte;
        if (tt >= 1) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try{
                    arena.addCoins(ap.getP(), tt, "Kill !");
                    ap.setCoins(ap.getCoins() + tt);
                    plugin.getSamaGamesAPI().getStatsManager().getPlayerStats(ap.getP().getUniqueId()).getQuakeStatistics().incrByKills(tt);
                }catch(Exception e)
                {
                    e.printStackTrace();
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

    public void leftAction(APlayer p, ItemSlot slot) {}

    public void rightAction(APlayer ap, ItemSlot slot) {
        basicShot(ap.getP(), slot);
    }

    @Override
    public GrenadeBasic clone() {
        return (GrenadeBasic) super.clone();
    }

}
