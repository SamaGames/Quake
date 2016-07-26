package com.geekpower14.quake.arena;

import com.geekpower14.quake.Quake;
import com.geekpower14.quake.stuff.TItem;
import com.geekpower14.quake.utils.Spawn;
import com.geekpower14.quake.utils.Utils;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.tools.ColorUtils;
import net.samagames.tools.PlayerUtils;
import net.samagames.tools.chat.ChatUtils;
import net.samagames.tools.scoreboards.ObjectiveSign;
import net.samagames.tools.scoreboards.TeamHandler;
import net.samagames.tools.scoreboards.VObjective;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ArenaTeam extends Arena{

    public List<ATeam> teams = new ArrayList<>();

    public HashMap<String, List<Spawn>> spawns = new HashMap<>();

    private ObjectiveSign objectiveScore;
    private ObjectiveSign objectivePerso;
    private TeamHandler teamHandler;

    public ArenaTeam(Quake pl) {
        super(pl);

        teamHandler = new TeamHandler();

        teams.add(new ATeam(plugin, this, "Red", ChatColor.RED, Color.RED, DyeColor.RED));
        teams.add(new ATeam(plugin, this, "Blue", ChatColor.BLUE, Color.BLUE, DyeColor.BLUE));
        teams.add(new ATeam(plugin, this, "Yellow", ChatColor.YELLOW, Color.YELLOW, DyeColor.YELLOW));
        teams.add(new ATeam(plugin, this, "Green", ChatColor.GREEN, Color.GREEN, DyeColor.GREEN));

        objectiveScore = new ObjectiveSign("score", "" + ChatColor.RED + ChatColor.BOLD + "Quake");
        objectiveScore.setLocation(VObjective.ObjectiveLocation.SIDEBAR);

        objectivePerso = new ObjectiveSign("perso", "");
        objectivePerso.setLocation(VObjective.ObjectiveLocation.LIST);

        loadConfig();
    }

    @Override
    protected void toConfigLoad()
    {/*
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
        }*/
    }

    /*@Override
    protected FileConfiguration toBasicConfig(FileConfiguration config)
    {
        for(ATeam team : teams)
        {
            setDefaultConfig(config, "Spawns_" + team.getName(), new ArrayList<String>());
        }

        return config;
    }*/

    /*@Override
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
            config.set("Spawns_" + team.getName(), s);
        }
    }*/

    @Override
    protected void execJoinPlayer(APlayer ap)
    {
        ap.getP().teleport(getSpawn(ap.getP()));

        objectiveScore.addReceiver(ap.getP());
        objectivePerso.addReceiver(ap.getP());
        teamHandler.addReceiver(ap.getP());

        setWoolStuff(ap);
        /*this.broadcast(ChatColor.YELLOW + ap.getName() + " a rejoint l'arène "
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
    protected void execLeavePlayer(APlayer ap)
    {
        final Player p = ap.getP();
        getTeam(p).removePlayer(p);

        objectiveScore.removeReceiver(ap.getP());
        objectivePerso.removeReceiver(ap.getP());
        teamHandler.removeReceiver(ap.getP());
    }

    @Override
    protected void execAfterLeavePlayer() {
        final List<ATeam> remain = getActiveTeams().stream().filter(team -> team.getSize() > 0).collect(Collectors.toList());

        if (remain.size() <= 1 && getStatus() == Status.IN_GAME) {
            if (remain.size() == 1) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> win(remain.get(0)), 1L);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, this::handleGameEnd, 1L);
            }
        }
    }

    @Override
    protected void execStart() {
        for (APlayer ap : gamePlayers.values()) {
            Player p = ap.getP();

            cleaner(p);
            tp(p);

            ap.giveStuff();

            giveEffect(p);

            ap.setReloading(20L);
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

        StringBuilder players = new StringBuilder();

        for(OfflinePlayer aPlayer : team.getPlayers())
        {
            players.append(PlayerUtils.getColoredFormattedPlayerName(aPlayer.getPlayer())).append(ChatColor.GRAY).append(", ");
        }

        ArrayList<String> winTemplateLines = new ArrayList<>();
        winTemplateLines.add(ChatUtils.getCenteredText(ChatColor.GREEN + "Gagnant" + ChatColor.GRAY + " - " + ChatColor.RESET + "Equipe " + team.getColor() + team.getName()));
        winTemplateLines.add(players.substring(0, players.length() - 2));

        this.coherenceMachine.getTemplateManager().getWinMessageTemplate().execute(winTemplateLines);

        //ap.updateScoreboard();

        for (OfflinePlayer p : team.getPlayers()) {
            APlayer ap = this.getAplayer(p.getUniqueId());
            this.handleWinner(p.getUniqueId());

            if (ap == null)
                continue;

            try{
                addCoins(ap.getP(), 20, "Victoire !");
                addStars(ap.getP(), 1, "Gagné en quake équipe !");
                ap.setCoins(ap.getCoins() + 20);
            }catch(Exception e)
            {
                e.printStackTrace();
            }

            try{
                plugin.getSamaGamesAPI().getStatsManager().getPlayerStats(p.getUniqueId()).getQuakeStatistics().incrByWins(1);
            }catch(Exception e)
            {}
        }

        final int nb = (int) (10 * 1.5);

        final BukkitTask infoxp = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
            int compteur = 0;

            public void run() {

                if (compteur >= nb) {
                    return;
                }
                for (OfflinePlayer pp : team.getPlayers()) {
                    APlayer ap = getAplayer(pp.getUniqueId());

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
                }

                compteur++;
            }
        }, 5L, 5L);

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            infoxp.cancel();
            handleGameEnd();
        }, 10 * 20);

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
        if (getStatus() == Status.REBOOTING)
            return false;
        if (victim.equals(shooter) || avictim.isInvincible() || isSameTeam(victim, shooter))
            return false;

        final ATeam s = getTeam(shooter);
        final ATeam v = getTeam(victim);

        ((ArenaStatisticsHelper) SamaGamesAPI.get().getGameManager().getGameStatisticsHelper()).increaseKills(shooter.getUniqueId());
        ((ArenaStatisticsHelper) SamaGamesAPI.get().getGameManager().getGameStatisticsHelper()).increaseDeaths(victim.getUniqueId());

        avictim.setinvincible(true);
        kill(victim);

        avictim.hasDiedBy(ashooter.getDisplayName());

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Utils.launchfw(victim.getLocation(), effect);
            } catch (Exception e) {
                e.printStackTrace();
            }

            coherenceMachine.getMessageManager().writeCustomMessage(s.getColor() + shooter.getName() + ChatColor.YELLOW + " a touché " + v.getColor() + victim.getName(), true);
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3, 2);
        });
        ashooter.addScore(1);
        s.addScore(1);

        if (s.getScore() >= goal) {
            setStatus(Status.REBOOTING);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> win(s), 2);
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
        Bukkit.getScheduler().runTask(plugin, () -> {

            for (APlayer player : gamePlayers.values()) {
                objectivePerso.setLine(player.getScore(), player.getP().getName());
            }
            objectivePerso.updateLines(false);

            for (ATeam at : getActiveTeams()) {
                objectiveScore.setLine(at.getScore(), at.getColor() + at.getName() + "    ");
            }
            objectiveScore.updateLines(false);
        });
    }

    @Override
    public void addSpawn(Location loc) {}

    public Boolean isSameTeam(Player p, Player b) {
        return this.getTeam(p).hasPlayer(b);
    }

    public void changeTeam(Player p, String steam)
    {
        ATeam nteam = getTeam(steam);
        ATeam oteam = getTeam(p);
        APlayer ap = getAplayer(p);

        if(nteam == null)
        {
            p.sendMessage(coherenceMachine.getGameTag() + ChatColor.RED + "Team invalide.");
            return;
        }

        if(getStatus() == Status.IN_GAME && !Quake.hasPermission(p, "quake.ChangeTeamInGame")) {
            p.sendMessage(coherenceMachine.getGameTag() + ChatColor.RED + "Vous ne pouvez pas changer de Team en jeu.");
            return;
        }

        if(nteam.hasPlayer(p)) {
            p.sendMessage(coherenceMachine.getGameTag() + ChatColor.RED + "Vous êtes déja dans cette équipe.");
            return;
        }

        if(nteam.getSize() >= (this.getConnectedPlayers()/this.getActiveTeams().size())+1) {
            p.sendMessage(coherenceMachine.getGameTag() + ChatColor.RED + "La Team " + nteam.getColor() + nteam.getName() + ChatColor.RED + " est pleine.");
            return;
        }

        oteam.removePlayer(p);

        if(getStatus() == Status.IN_GAME) {
            kill(p);
        }
        nteam.addPlayer(p);

        if(getStatus() == Status.READY_TO_START || getStatus() == Status.STARTING)
        {
            setWoolStuff(ap);
        }

        p.sendMessage(coherenceMachine.getGameTag() + ChatColor.GREEN + "Vous êtes maintenant dans la Team: " + nteam.getColor() + nteam.getName());
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
        return teams.stream().filter(ATeam::isActive).collect(Collectors.toList());
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

    public TeamHandler getTeamHandler() {
        return teamHandler;
    }
}
