
package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.task.ScoreHandler;
import com.Geekpower14.quake.utils.Spawn;
import com.Geekpower14.quake.utils.StatsNames;
import com.Geekpower14.quake.utils.Utils;
import net.samagames.api.games.Status;
import net.samagames.api.player.PlayerData;
import net.samagames.permissionsapi.PermissionsAPI;
import net.samagames.permissionsbukkit.PermissionsBukkit;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Geekpower14 on 17/12/2014.
 */
public class ArenaSolo extends Arena{

    public List<Spawn> spawn = new ArrayList<>();
    private ScoreHandler scoreHandler;

    public ArenaSolo(Quake pl, String name) {
        super(pl, name);

        loadConfig();

        setStatus(Status.READY_TO_START);
    }

    @Override
    protected void toConfigLoad(FileConfiguration config) {
        List<String> s = config.getStringList("Spawns");
        for(int i = 0; i < s.size(); i++)
        {
            spawn.add(new Spawn(s.get(i)));
        }
    }

    @Override
    protected FileConfiguration toBasicConfig(FileConfiguration config) {

        setDefaultConfig(config, "Goal", 25);

        setDefaultConfig(config, "Spawns", new ArrayList<String>());

        return config;
    }

    @Override
    protected void toSaveConfig(FileConfiguration config) {
        List<String> s = new ArrayList<String>();
        for(int i = 0; i < spawn.size(); i++)
        {
            s.add(spawn.get(i).getSaveLoc());
        }
        config.set("Spawns", s);
    }

    @Override
    protected void execJoinPlayer(APlayer ap) {
        Player p  = ap.getP();
        p.teleport(getSpawn(p));

        this.broadcast(ChatColor.YELLOW
                + ap.getName()
                + " a rejoint l'arène "
                + ChatColor.DARK_GRAY
                + "[" + ChatColor.RED
                + players.size()
                + ChatColor.DARK_GRAY
                + "/" + ChatColor.RED
                + maxPlayer
                + ChatColor.DARK_GRAY
                + "]");

        if(players.size() >= minPlayer && eta == Status.READY_TO_START)
        {
            startCountdown();
        }
    }

    @Override
    protected void execLeavePlayer(APlayer ap) {

    }

    @Override
    protected void execAfterLeavePlayer() {
        if(getConnectedPlayers() <= 1 && eta == Status.READY_TO_START)
        {
            if(players.size() >= 1)
            {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> win(players.get(0).getP()), 1L);
            }else{
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> stop(), 1L);
            }
        }
    }

    @Override
    protected void execStart() {
        scoreHandler = new ScoreHandler(plugin, this);

        for(APlayer ap : players)
        {
            Player p = ap.getP();

            ap.setScoreboard();

            cleaner(p);
            tp(p);

            ap.giveStuff();

            giveEffect(p);

            ap.setReloading(1 * 20L);

            try{
                plugin.samaGamesAPI.getStatsManager(getOriginalGameName()).increase(p.getUniqueId(), StatsNames.PARTIES, 1);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void execStop() {
        scoreHandler.stop();
        scoreHandler = null;
    }

    @Override
    protected void execWin(Object o) {
        final Player p = (Player) o;
        if(p == null)
        {
            stop();
            return;
        }

        APlayer ap = getAplayer(p);

        this.nbroadcast(ChatColor.AQUA + "#"+ ChatColor.GRAY + "--------------------" + ChatColor.AQUA + "#");
        this.nbroadcast(ChatColor.GRAY + "");
        this.nbroadcast(ChatColor.AQUA + p.getDisplayName() + ChatColor.YELLOW + " a gagné !");
        this.nbroadcast(ChatColor.GRAY + "");
        this.nbroadcast(ChatColor.AQUA + "#"+ ChatColor.GRAY + "--------------------" + ChatColor.AQUA + "#");

        try{
            PlayerData playerData = plugin.samaGamesAPI.getPlayerManager().getPlayerData(ap.getP().getUniqueId());
            playerData.creditStars(1, "Premier au Quake !");
            playerData.creditCoins(20, "Victoire !", true, (newAmount, difference, error) -> ap.setCoins((int) (ap.getCoins() + difference)));
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        for(APlayer a : players)
        {
            broadcast(a.getP(), ChatColor.GOLD + "Tu as gagné " + a.getCoins() + " coins au total !");
        }
        try{
            plugin.samaGamesAPI.getStatsManager(getOriginalGameName()).increase(p.getUniqueId(), StatsNames.VICTOIRES, 1);
        }catch(Exception e)
        {}

        final int nb = (int) (Time_After * 1.5);

        final int infoxp = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable()
        {
            int compteur = 0;
            public void run() {

                if(compteur >= nb)
                {
                    return;
                }

                //Spawn the Firework, get the FireworkMeta.
                Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                //Our random generator
                Random r = new Random();

                //Get the type
                int rt = r.nextInt(4) + 1;
                FireworkEffect.Type type = FireworkEffect.Type.BALL;
                if (rt == 1) type = FireworkEffect.Type.BALL;
                if (rt == 2) type = FireworkEffect.Type.BALL_LARGE;
                if (rt == 3) type = FireworkEffect.Type.BURST;
                if (rt == 4) type = FireworkEffect.Type.CREEPER;
                if (rt == 5) type = FireworkEffect.Type.STAR;

                //Get our random colours
                int r1i = r.nextInt(17) + 1;
                int r2i = r.nextInt(17) + 1;
                Color c1 = getColor(r1i);
                Color c2 = getColor(r2i);

                //Create our effect with this
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                //Then apply the effect to the meta
                fwm.addEffect(effect);

                //Generate some random power and set it
                int rp = r.nextInt(2) + 1;
                fwm.setPower(rp);

                //Then apply this to our rocket
                fw.setFireworkMeta(fwm);

                compteur++;
            }
        }, 5L, 5L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            plugin.getServer().getScheduler().cancelTask(infoxp);
            stop();
        }, (Time_After * 20));
    }

    @Override
    public void extraStuf(APlayer ap) {
        return;
    }

    @Override
    protected boolean execShotPlayer(final Player shooter, final Player victim, final FireworkEffect effect) {
        final APlayer ashooter = this.getAplayer(shooter);
        final APlayer avictim = this.getAplayer(victim);

        if(avictim == null)
            return false;
        if(victim.equals(shooter) || avictim.isInvincible())
            return false;

        avictim.setinvincible(true);
        kill(victim);
        avictim.hasDiedBy(ashooter.getDisplayName());
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Utils.launchfw(victim.getLocation(), effect);
            } catch (Exception e) {
                e.printStackTrace();
            }

            PermissionsAPI permissionsAPI = plugin.samaGamesAPI.getPermissionsManager().getApi();

            broadcast(ChatColor.RED
                    + PermissionsBukkit.getPrefix(permissionsAPI.getUser(shooter.getUniqueId()))
                    + shooter.getName()
                    + ChatColor.YELLOW
                    + " a touché "
                    + PermissionsBukkit.getPrefix(permissionsAPI.getUser(victim.getUniqueId()))
                    + victim.getName());
            shooter.playSound(shooter.getLocation(), Sound.SUCCESSFUL_HIT, 3, 2);
        });
        ashooter.addScore(1);

        if(ashooter.getScore() == Goal)
        {
            setStatus(Status.REBOOTING);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> win(shooter), 2);
        }
        return true;
    }

    public void tp(Player p)
    {
        if(spawn != null)
        {
            p.teleport(getSpawn(p));
        }
    }

    public Location getSpawn(Player p)
    {
        Spawn r = null;
        List<Spawn> spawns = new ArrayList<Spawn>();
        for(Spawn s : spawn)
        {
            if(r == null)
            {
                r = s;
                continue;
            }

            if(s.getUses() < r.getUses())
                r = s;
        }

        for(Spawn s : spawn)
        {
            if(s.getUses() <= r.getUses())
                spawns.add(s);
        }
        Random rr = new Random();

        int i = rr.nextInt(spawns.size());

        Spawn l = spawns.get(i);
        l.addUse();

        return l.getLoc();
    }

    public void updateScore()
    {
        if(players.size() <= 0)
        {
            return;
        }
        scoreHandler.requestUpdate();
    }

    public List<APlayer> getTopFive()
    {
        List<APlayer> r = new ArrayList<>();

        for(int i = 0; i < players.size() && i < 10; i++)
        {
            r.add(players.get(i));
        }

        return r;
    }

    public void addSpawn(Location loc)
    {
        spawn.add(new Spawn(loc));
    }

    public String getGameName()
    {
        return "quake";
    }


}
