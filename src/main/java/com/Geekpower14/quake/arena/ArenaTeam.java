package com.Geekpower14.quake.arena;

import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.stuff.TItem;
import com.Geekpower14.quake.utils.Spawn;
import com.Geekpower14.quake.utils.StatsNames;
import com.Geekpower14.quake.utils.Utils;
import net.samagames.gameapi.json.Status;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.Wool;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Geekpower14 on 17/12/2014.
 */
public class ArenaTeam extends Arena{

    public List<ATeam> teams = new ArrayList<ATeam>();

    public HashMap<String, List<Spawn>> spawns = new HashMap<String, List<Spawn>>();

    private Objective score;
    private Objective perso;

    public ArenaTeam(Quake pl, String name) {
        super(pl, name);

        teams.add(new ATeam(plugin, this, "Red", ChatColor.RED, Color.RED, DyeColor.RED));
        teams.add(new ATeam(plugin, this, "Blue", ChatColor.BLUE, Color.BLUE, DyeColor.BLUE));
        teams.add(new ATeam(plugin, this, "Yellow", ChatColor.YELLOW, Color.YELLOW, DyeColor.YELLOW));
        teams.add(new ATeam(plugin, this, "Green", ChatColor.GREEN, Color.GREEN, DyeColor.GREEN));

        loadConfig();

        eta = Status.Available;
    }

    @Override
    protected void toConfigLoad(FileConfiguration config)
    {
        for(ATeam team : teams)
        {
            List<String> s = config.getStringList("Spawns_"+team.getName());
            List<Spawn> sp = new ArrayList<>();
            for(int i = 0; i < s.size(); i++)
            {
                sp.add(new Spawn(s.get(i)));
                team.setActive(true);
            }
            spawns.put(team.getName(), sp);
        }
    }

    @Override
    protected FileConfiguration toBasicConfig(FileConfiguration config)
    {
        for(ATeam team : teams)
        {
            setDefaultConfig(config, "Spawns_"+team.getName(), new ArrayList<String>());
        }

        return config;
    }

    @Override
    protected void toSaveConfig(FileConfiguration config)
    {
        for(ATeam team : teams)
        {
            List<String> s = new ArrayList<String>();
            List<Spawn> sp = spawns.get(team.getName());
            for(int i = 0; i < sp.size(); i++)
            {
                s.add(sp.get(i).getSaveLoc());
            }
            config.set("Spawns_"+team.getName(), s);
        }
    }

    @Override
    protected void execJoinPlayer(APlayer ap)
    {
        final Player p = ap.getP();
        ATeam at = addPlayerToTeam(ap.getP());
        ap.getP().teleport(getSpawn(ap.getP()));

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                for(Player pp : Quake.getOnline())
                {
                    pp.setScoreboard(getScoreboard());
                }
            }
        }, 5L);


        setWoolStuff(ap);
        this.broadcast(ChatColor.YELLOW + ap.getName() + " a rejoint l'arène "
                + ChatColor.DARK_GRAY
                + "[" + ChatColor.RED
                + players.size()
                + ChatColor.DARK_GRAY
                + "/" + ChatColor.RED
                + maxPlayer
                + ChatColor.DARK_GRAY
                + "]");

        if (players.size() >= minPlayer && eta == Status.Available) {
            startCountdown();
        }
    }

    @Override
    protected void execLeavePlayer(APlayer ap)
    {
        final Player p = ap.getP();
        getTeam(p).removePlayer(p);

        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    @Override
    protected void execAfterLeavePlayer() {
        final List<ATeam> remain = new ArrayList<ATeam>();
        for (ATeam team : getActiveTeams()) {
            if (team.getSize() > 0) {
                remain.add(team);
            }
        }

        if (remain.size() <= 1 && eta == Status.InGame) {
            if (remain.size() == 1) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        win(remain.get(0));
                    }
                }, 1L);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        stop();
                    }
                }, 1L);
            }
        }
    }

    @Override
    protected void execStart() {
        for (APlayer ap : players) {
            Player p = ap.getP();

            //ap.setScoreboard();
            if (score != null) {
                score.unregister();
            }
            if (perso != null) {
                perso.unregister();
            }

            perso = tboard.registerNewObjective(name + "_Perso", "dummy");
            perso.setDisplaySlot(DisplaySlot.PLAYER_LIST);

            score = tboard.registerNewObjective(name + "_Teams", "dummy");
            score.setDisplaySlot(DisplaySlot.SIDEBAR);
            score.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Quake");

            cleaner(p);
            tp(p);

            ap.giveStuff();

            giveEffect(p);

            ap.setReloading(1 * 20L);
            try{
                StatsApi.increaseStat(p.getUniqueId(), StatsNames.GAME_NAME, StatsNames.PARTIES, 1);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void execStop() {
        //vide
    }

    @Override
    protected void execWin(Object o)
    {
        final ATeam team = (ATeam) o;

        this.nbroadcast(ChatColor.AQUA + "#" + ChatColor.GRAY + "--------------------" + ChatColor.AQUA + "#");
        this.nbroadcast(ChatColor.GRAY + "");
        this.nbroadcast(ChatColor.AQUA + "L'équipe " + team.getColor() + team.getName() + ChatColor.YELLOW + " a gagné !");
        this.nbroadcast(ChatColor.GRAY + "");
        this.nbroadcast(ChatColor.AQUA + "#" + ChatColor.GRAY + "--------------------" + ChatColor.AQUA + "#");

        //ap.updateScoreboard();

        for (OfflinePlayer p : team.getPlayers()) {
            APlayer ap = this.getAplayer(p.getName());
            if (ap == null)
                continue;
            try {
                int up = CoinsManager.syncCreditJoueur(ap.getP().getUniqueId(), 20, true, true);
                ap.setCoins(ap.getCoins() + up);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                StatsApi.increaseStat(p.getUniqueId(), StatsNames.GAME_NAME, StatsNames.VICTOIRES, 1);
            } catch (Exception e) {
            }
        }

        for (APlayer a : players) {
            broadcast(a.getP(), ChatColor.GOLD + "Tu as gagné " + a.getCoins() + " coins au total !");
        }

        final int nb = (int) (Time_After * 1.5);

        final int infoxp = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            int compteur = 0;

            public void run() {

                if (compteur >= nb) {
                    return;
                }
                for (OfflinePlayer pp : team.getPlayers()) {
                    APlayer ap = getAplayer(pp.getName());

                    if (ap == null)
                        continue;
                    Player p = ap.getP();

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
                }

                compteur++;
            }
        }, 5L, 5L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            public void run() {
                plugin.getServer().getScheduler().cancelTask(infoxp);
                stop();
            }
        }
                , (Time_After * 20));

    }

    @Override
    public void extraStuf(APlayer ap)
    {
        final Player p = ap.getP();
        final ATeam team = getTeam(p);
        Wool w = new Wool(team.getDyeColor());
        ItemStack itemStack = w.toItemStack(1);
        itemStack = TItem.setItemNameAndLore(itemStack,ChatColor.GOLD + "Vous êtes dans la team: " + team.getColor() + team.getName(), new String[]{}, true);
        for(int i = 0; i < 9; i++)
        {
            if(p.getInventory().getItem(i) == null
                    || p.getInventory().getItem(i).getType() == Material.AIR)
            {
                p.getInventory().setItem(i, itemStack);
            }
        }

        getTeam(p).giveChestplate(p);

    }

    @Override
    protected boolean execShotPlayer(final Player shooter, final Player victim, final FireworkEffect effect) {
        final APlayer ashooter = this.getAplayer(shooter);
        final APlayer avictim = this.getAplayer(victim);

        if (avictim == null)
            return false;
        if (eta == Status.Stopping)
            return false;
        if (victim.equals(shooter) || avictim.isInvincible() || isSameTeam(victim, shooter))
            return false;

        final ATeam s = getTeam(shooter);
        final ATeam v = getTeam(victim);

        avictim.setinvincible(true);
        kill(victim);

        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.launchfw(victim.getLocation(), effect);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                broadcast(s.getColor() + shooter.getName() + ChatColor.YELLOW + " a touché " + v.getColor() + victim.getName());
                shooter.playSound(shooter.getLocation(), Sound.SUCCESSFUL_HIT, 3, 2);
            }
        });
        ashooter.addScore(1);
        s.addScore(1);

        if (s.getScore() >= Goal) {
            eta = Status.Stopping;
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    win(s);
                }
            }, 2);
        }

        return true;
    }

    @Override
    public void tp(Player p)
    {
        p.teleport(getSpawn(p));
    }

    @Override
    public Location getSpawn(Player p)
    {
        Spawn r = null;
        ATeam at = getTeam(p);
        if(at == null)
            return null;

        for(Spawn s : spawns.get(at.getName()))
        {
            if(r == null)
            {
                r = s;
                continue;
            }

            if(s.getUses() < r.getUses())
                r = s;
        }
        r.addUse();

        return r.getLoc();
    }

    @Override
    public void updateScore() {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (players.size() <= 0) {
                    return;
                }

                for (APlayer player : players) {
                    perso.getScore(player.getP()).setScore(player.getScore());
                }

                for (ATeam at : getActiveTeams()) {
                    score.getScore(Bukkit.getOfflinePlayer(at.getColor() + at.getName() + "    ")).setScore(at.getScore());
                }
            }
        });
    }

    @Override
    public void addSpawn(Location loc) {
        return;
    }

    public Boolean isSameTeam(Player p, Player b) {
        if (this.getTeam(p).hasPlayer(b)) {
            return true;
        }

        return false;
    }

    public void changeTeam(Player p, String steam)
    {
        ATeam nteam = getTeam(steam);
        ATeam oteam = getTeam(p);
        APlayer ap = getAplayer(p);

        if(nteam == null)
        {
            this.broadcast(p, ChatColor.RED + "Team invalide.");
            return;
        }

        if(eta == Status.InGame && !Quake.hasPermission(p, "quake.ChangeTeamInGame"))
        {
            this.broadcast(p, ChatColor.RED + "Vous ne pouvez pas changer de Team en jeu.");
            return;
        }

        if(nteam.hasPlayer(p))
        {
            this.broadcast(p, ChatColor.RED + "Vous êtes déja dans cette équipe.");
            return;
        }

        if(nteam.getSize() >= (this.getActualPlayers()/this.getActiveTeams().size())+1)
        {
            this.broadcast(p, ChatColor.RED + "La Team "+ nteam.getColor() + nteam.getName() + ChatColor.RED + " est pleine.");
            return;
        }

        oteam.removePlayer(p);

        if(eta == Status.InGame)
        {
            kill(p);
        }
        nteam.addPlayer(p);

        if(eta.isLobby())
        {
            setWoolStuff(ap);
        }

        this.broadcast(p, ChatColor.GREEN + "Vous êtes maintenant dans la Team: " + nteam.getColor() + nteam.getName());
        return;
    }

    public void setWoolStuff(APlayer ap)
    {
        int i = 0;
        for(ATeam at : getActiveTeams())
        {
            Wool it = new Wool(at.getDyeColor());

            if(at.hasPlayer(ap.getP()))
            {
                ap.getP().getInventory().setItem(i,
                        TItem.setItemNameAndLore(it.toItemStack(1),
                                "Votre équipe: " + at.getColor() + at.getName(), null, true));
                i++;
                continue;
            }
            ap.getP().getInventory().setItem(i,
                    TItem.setItemNameAndLore(it.toItemStack(1),
                            "Rejoindre l'équipe "+ at.getColor() + at.getName(), null, true));

            i++;
        }
        ap.getP().updateInventory();
    }

    public ATeam getTeam(Player p)
    {
        for(ATeam t : getActiveTeams())
        {
            if(t.hasPlayer(p))
            {
                return t;
            }
        }

        return getActiveTeams().get(0);
    }

    public ATeam getTeam(String name)
    {
        for(ATeam t : teams)
        {
            if(t.getName().equalsIgnoreCase(name))
            {
                return t;
            }
        }

        return null;
    }

    public List<ATeam> getActiveTeams()
    {
        List<ATeam> r = new ArrayList<ATeam>();
        for(ATeam at : teams)
        {
            if(at.isActive())
                r.add(at);
        }
        return r;
    }

    public ATeam getTeamByColor(DyeColor d)
    {
        for(ATeam t : teams)
        {
            if(t.getDyeColor().equals(d))
                return t;
        }
        return null;
    }

    private ATeam addPlayerToTeam(Player p)
    {
        List<ATeam> tt = getActiveTeams();
        ATeam result = tt.get(0);

        for(ATeam t : tt)
        {
            if(t.getSize() < result.getSize())
            {
                result = t;
            }
        }

        result.addPlayer(p);

        return result;
    }
}
