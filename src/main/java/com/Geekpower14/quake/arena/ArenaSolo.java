
package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.task.ScoreHandler;
import com.Geekpower14.quake.utils.Spawn;
import com.Geekpower14.quake.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IGameProperties;
import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.messages.templates.PlayerWinTemplate;
import net.samagames.tools.ColorUtils;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArenaSolo extends Arena{

    public List<Spawn> spawn = new ArrayList<>();
    private ScoreHandler scoreHandler;

    public ArenaSolo(Quake pl) {
        super(pl);

        scoreHandler = new ScoreHandler(pl, this);

        loadConfig();
    }

    @Override
    protected void toConfigLoad() {
        IGameProperties properties = SamaGamesAPI.get().getGameManager().getGameProperties();

        JsonArray spawnDefault = new JsonArray();
        spawnDefault.add(new JsonPrimitive(""));
        spawnDefault.add(new JsonPrimitive(""));

        JsonArray potions = properties.getOption("Spawns", spawnDefault).getAsJsonArray();

        for(JsonElement data : potions)
        {
            spawn.add(new Spawn(Utils.str2loc(data.getAsString())));
        }
    }

    /*
    protected void toSaveConfig(FileConfiguration config) {
        List<String> s = new ArrayList<String>();
        for(int i = 0; i < spawn.size(); i++)
        {
            s.add(spawn.get(i).getSaveLoc());
        }
        config.set("Spawns", s);
    }*/

    @Override
    protected void execJoinPlayer(APlayer ap) {
        Player p  = ap.getP();
        p.teleport(getSpawn(p));
        scoreHandler.addPlayer(ap);
        /*this.broadcast(ChatColor.YELLOW
                + ap.getName()
                + " a rejoint l'arène "
                + ChatColor.DARK_GRAY
                + "[" + ChatColor.RED
                + players.size()
                + ChatColor.DARK_GRAY
                + "/" + ChatColor.RED
                + maxPlayer
                + ChatColor.DARK_GRAY
                + "]");*/
    }

    @Override
    protected void execLeavePlayer(APlayer ap) {
        scoreHandler.removePlayer(ap);
    }

    @Override
    protected void execAfterLeavePlayer() {
        if(getStatus() == Status.IN_GAME)
        {
            if(getInGamePlayers().size() == 1)
            {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> win(gamePlayers.values().iterator().next().getP()), 1L);
            }else if(getConnectedPlayers() <= 0){
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::handleGameEnd, 1L);
            }
        }
    }

    @Override
    protected void execStart() {

        for(APlayer ap : getInGamePlayers().values())
        {
            Player p = ap.getP();

            Bukkit.getScheduler().runTask(plugin, () -> {
                cleaner(p);
                tp(p);

                ap.giveStuff();

                giveEffect(p);

                ap.setReloading(20L);
            });
        }
    }

    @Override
    protected void execStop() {
        scoreHandler.stop();
    }

    @Override
    protected void execWin(Object o) {
        final Player p = (Player) o;
        if(p == null)
        {
            handleGameEnd();
            return;
        }

        APlayer ap = getAplayer(p);

        PlayerWinTemplate template = this.coherenceMachine.getTemplateManager().getPlayerWinTemplate();
        template.execute(p);

        try{
            addCoins(p, 20, "Victoire !");
            ap.setCoins(ap.getCoins() + 20);
            addStars(p, 3, "Premier au Quake !");
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        try{
            SamaGamesAPI.get().getStatsManager().getPlayerStats(p.getUniqueId()).getQuakeStatistics().incrByWins(1);
        }catch(Exception e)
        {}

        final int nb = (int) (10 * 1.5);


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
                Color c1 = ColorUtils.getColor(r1i);
                Color c2 = ColorUtils.getColor(r2i);

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

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> plugin.getServer().getScheduler().cancelTask(infoxp), (10 * 20));
    }

    @Override
    public void extraStuf(APlayer ap) {}

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

            coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.RED
                    + plugin.getSamaGamesAPI().getPermissionsManager().getPrefix(plugin.getSamaGamesAPI().getPermissionsManager().getPlayer(shooter.getUniqueId()))
                    + shooter.getName()
                    + ChatColor.YELLOW
                    + " a touché "
                    + plugin.getSamaGamesAPI().getPermissionsManager().getPrefix(plugin.getSamaGamesAPI().getPermissionsManager().getPlayer(victim.getUniqueId()))
                    + victim.getName(), true);
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3, 2);
        });
        ashooter.addScore(1);

        if(ashooter.getScore() == goal)
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
        List<Spawn> spawns = new ArrayList<>();
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
        scoreHandler.requestUpdate();
    }

    public void addSpawn(Location loc)
    {
        spawn.add(new Spawn(loc));
    }

    public ScoreHandler getScoreHandler() {
        return scoreHandler;
    }
}
