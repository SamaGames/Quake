package com.Geekpower14.quake.stuff.grenade;

import com.Geekpower14.quake.arena.APlayer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by charles on 10/08/2014.
 */
public class FragGrenade extends GrenadeBasic{
    public FragGrenade() {
        super("fragrenade",
                ""+ChatColor.RED + ChatColor.BOLD + "grenade à Fragmentation",
                0L,
                FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).build());
    }

    @Override
    public ItemStack getItem() {
        ItemStack coucou = setItemNameAndLore(new ItemStack(Material.COCOA), ""+ChatColor.RED + ChatColor.BOLD + "grenade à Fragmentation", new String[]{
                ChatColor.DARK_GRAY + "Explose au bout de " + ChatColor.GOLD + "3" + ChatColor.DARK_GRAY + " secondes.",
                ChatColor.DARK_GRAY + "Élimine les joueurs " + ChatColor.GOLD + "3" + ChatColor.DARK_GRAY + " blocs au tour."
        }, true);
        coucou.setAmount(this.nb);
        return coucou;
    }

    @Override
    public void rightAction(APlayer ap) {
        basicShot(ap.getP());
    }

    @Override
    public void leftAction(APlayer p) {
        return;
    }
}
