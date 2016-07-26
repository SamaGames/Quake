package com.geekpower14.quake.stuff.grenade;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FragGrenade extends GrenadeBasic{
    public FragGrenade(int id, int nb) {
        super(id,
                ""+ChatColor.RED + ChatColor.BOLD + "Grenade à Fragmentation",
                0L,
                FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).build());
        setNB(nb);
    }

    @Override
    public ItemStack getItem() {
        ItemStack coucou = setItemNameAndLore(new ItemStack(Material.CLAY_BALL), ""+ChatColor.RED + ChatColor.BOLD + "Grenade à Fragmentation", new String[]{
                ChatColor.DARK_GRAY + "Explose au bout de " + ChatColor.GOLD + "3" + ChatColor.DARK_GRAY + " secondes.",
                ChatColor.DARK_GRAY + "Élimine les joueurs " + ChatColor.GOLD + "3" + ChatColor.DARK_GRAY + " blocs autour."
        }, true);
        coucou.setAmount(this.nb);
        return coucou;
    }
}
