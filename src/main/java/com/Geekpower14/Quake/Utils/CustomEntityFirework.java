package com.Geekpower14.quake.utils;

import net.minecraft.server.v1_8_R1.EntityFireworks;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomEntityFirework extends EntityFireworks {
    List<Player> players = null;
    boolean gone = false;

    public CustomEntityFirework(World world, List<Player> p) {
        super(world);
        players = p;
        this.a(0.25F, 0.25F);
    }

    public static void spawn(Location location, FireworkEffect effect, int radius)
    {
        List<Player> players = new ArrayList<>();
        for(Player p : location.getWorld().getPlayers())
        {
            if(location.distanceSquared(p.getLocation()) < radius)
            {
                players.add(p);
            }
        }
        spawn(location, effect, players);
    }

    public static void spawn(Location location, FireworkEffect effect, List<Player> players) {
        try {
            CustomEntityFirework firework = new CustomEntityFirework(((CraftWorld) location.getWorld()).getHandle(), players);
            FireworkMeta meta = ((Firework) firework.getBukkitEntity()).getFireworkMeta();
            meta.addEffect(effect);
            ((Firework) firework.getBukkitEntity()).setFireworkMeta(meta);
            firework.setPosition(location.getX(), location.getY(), location.getZ());

            if ((((CraftWorld) location.getWorld()).getHandle()).addEntity(firework)) {
                firework.setInvisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void h() {
        if (gone) {
            return;
        }

        if (!this.world.isStatic) {
            gone = true;

            if (players != null) {
                if (players.size() > 0) {
                    try{
                        for (Player player : players) {
                            (((CraftPlayer) player).getHandle()).playerConnection.sendPacket(new PacketPlayOutEntityStatus(this, (byte) 17));
                        }
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                    this.die();
                    return;
                }
            }

            world.broadcastEntityEffect(this, (byte) 17);
            this.die();
        }
    }
}
