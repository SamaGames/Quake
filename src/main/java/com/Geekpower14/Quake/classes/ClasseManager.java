package com.Geekpower14.Quake.classes;

import com.Geekpower14.Quake.Quake;
import com.Geekpower14.Quake.Stuff.Grenade.FragGrenade;
import com.Geekpower14.Quake.Stuff.Hoe.*;
import com.Geekpower14.Quake.Stuff.TItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geekpower14 on 25/10/2014.
 */
public class ClasseManager {

    public Quake plugin;

    public List<Classe> classes = new ArrayList<>();

    public ClasseManager(Quake pl)
    {
        plugin = pl;


    }

    public Classe getClasseByName(String name)
    {
        for(Classe i : classes)
        {
            if(i.getName().equals(name))
                return (Classe) i.clone();
        }

        return classes.get(0);
    }

}
