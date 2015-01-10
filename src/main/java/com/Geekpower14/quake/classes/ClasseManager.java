package com.Geekpower14.quake.classes;

import com.Geekpower14.quake.Quake;

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
