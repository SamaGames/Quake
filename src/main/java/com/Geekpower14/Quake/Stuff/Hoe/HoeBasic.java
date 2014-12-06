package com.Geekpower14.quake.stuff.hoe;

import com.Geekpower14.quake.arena.APlayer;
import com.Geekpower14.quake.arena.Arena;
import com.Geekpower14.quake.Quake;
import com.Geekpower14.quake.stuff.TItem;
import com.Geekpower14.quake.utils.ParticleEffects;
import com.Geekpower14.quake.utils.StatsNames;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class HoeBasic extends TItem{

	public FireworkEffect effect;

    public double aim = 1.5;

	public HoeBasic(String name, String display, Long reload, FireworkEffect e) {
		super(name, display, 1, reload);
		effect = e;
	}

	protected void basicShot(final Player player)
	{
		final Arena arena = plugin.arenaManager.getArenabyPlayer(player);

        Location el = player.getEyeLocation();
        Vector to = el.getDirection().normalize().multiply(2);
        Location last = el.add(to);

        if(arena == null)
		{
			return;
		}

		final APlayer ap = arena.getAplayer(player);

		if (ap.isReloading())
		{
			return;
		}

		ap.setReloading(true);
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 0.09F, 2.0F);
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 0.05F, 1.5F);
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 0.05F, 1.4F);
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 0.09F, 1.3F);
		//player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 0.1F, 1.2F);

		//String wait = "Coded by geekpower14 if you use it put my name !";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                int compte = 0;

                List<Player> victims = getTargetV3(arena, player, 100, aim, false);

                for(Player victim : victims)
                {
                    if(arena.shotplayer(player, victim, effect))
                        compte++;
                }

                final int finalCompte = compte;
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (finalCompte == 2)
                        {
                            arena.broadcast("Double kill !");
                        } else if (finalCompte == 3)
                        {
                            arena.broadcast("Triple kill !");
                        } else if (finalCompte >= 4)
                        {
                            arena.broadcast("Amazing kill !");
                        }
                    }
                });

                ap.setReloading(reloadTime);

                long estimatedTime = System.currentTimeMillis() - startTime;

                plugin.log.info("Shot time : "+ estimatedTime);

                final int tt = compte;
                if(tt >= 1)
                {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
                    {
                        public void run() {
                            try{
                                int up = CoinsManager.syncCreditJoueur(ap.getP().getUniqueId(), tt*1, true, true);
                                ap.setCoins(ap.getCoins() + up);
                                StatsApi.increaseStat(ap.getP().getUniqueId(), StatsNames.GAME_NAME, StatsNames.KILL, tt);
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
			/*Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

				@Override
				public void run() {*/
                    arena.updateScore();
            /*
				}
			});*/
                }

            }
        });

		return;
	}

	/**
	 * Retourne le premier joueur en ligne de mire
	 * @param player le shooter
	 * @param maxRange en bloc
	 * @param aiming 1 par défaut, pour augmenter la marge de 20% 1.20
	 * @param wallHack Si la cible peut être trouvé à travers une bloc solid
	 * @return
	 */
	public List<Player> getTargetV3(Arena arena,Player player, int maxRange, double aiming, boolean wallHack) {
		List<Player> target = new ArrayList<Player>();
		Location playerEyes = player.getEyeLocation();

		Vector direction = playerEyes.getDirection().normalize();

		// Filtre de target
		List<Player> targets = new ArrayList<Player>();
		for (Player online : Quake.getOnline()) {
			if (online != player && online.getLocation().distanceSquared(playerEyes) < maxRange * maxRange) {
				targets.add(online);
			}
		}

		Block block;
		Location loc = playerEyes.clone();
		Location testLoc;
		double lx, ly, lz;
		double px, py, pz;
		// Adapter au format joueur
		Vector progress = direction.clone().multiply(0.70);
		maxRange = (100 * maxRange / 70);

		int loop = 0;
		while (loop < maxRange) {
			loop++;
			loc.add(progress);
			block = loc.getBlock();
			if (!wallHack) if (!block.getType().isTransparent()) break;
			lx = loc.getX();
			ly = loc.getY();
			lz = loc.getZ();

			for(Player apa : arena.getPlayers())
			{
				try {
					//ParticleEffects.FIREWORKS_SPARK.sendToPlayer(apa, loc, 0.1F, 0.1F, 0.1F, 0.05F, 2);
					//ParticleEffects.MOB_SPELL_AMBIENT.sendToPlayer(apa, loc, 0.1F, 0.1F, 0.1F, RandomUtils.nextFloat(), 2);
					ParticleEffects.FIREWORKS_SPARK.sendToPlayer(apa, loc, 0.01F, 0.01F, 0.01F, 0.00005F, 1);
                    if(loop%10 == 0)
                    {
                        apa.getWorld().playSound(apa.getLocation(), Sound.FIREWORK_LAUNCH, 0.042F, 0.01F);
                    }
					//ParticleEffects.INSTANT_SPELL.sendToPlayer(apa, loc, 0.006F, 0.006F, 0.006F, 6.0F, 3);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Player possibleTarget : targets) {
				if (possibleTarget.getUniqueId() == player.getUniqueId()) continue;
				testLoc = possibleTarget.getLocation().add(0, 0.85, 0);
				px = testLoc.getX();
				py = testLoc.getY();
				pz = testLoc.getZ();

				// Touche ou pas
				boolean dX = Math.abs(lx - px) < 0.70 * aiming;
				boolean dY = Math.abs(ly - py) < 1 * aiming;
				boolean dZ = Math.abs(lz - pz) < 0.70 * aiming;

				if (dX && dY && dZ) {
					if(!target.contains(possibleTarget))
						target.add(possibleTarget);
				}
			}
		}
		return target;
	}

	@Override
	public HoeBasic clone() {
		HoeBasic o = null;
		o = (HoeBasic) super.clone();
		return o;
	}

}