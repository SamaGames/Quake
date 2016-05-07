package com.Geekpower14.quake.classes;

import java.util.ArrayList;
import java.util.List;

public class ClasseManager {

    public List<Classe> classes = new ArrayList<>();


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
