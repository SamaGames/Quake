package com.Geekpower14.Quake.classes;

import com.Geekpower14.Quake.Quake;


/**
 * Created by Geekpower14 on 25/10/2014.
 */
public abstract class Classe implements Cloneable{

    public Quake plugin;

    public String name = "Unknown";

    public String alias = "";

    public String givePerm = "Quake.admin";

    public Classe(String name, String display)
    {
        this.name = name;
        this.alias = display;

        plugin = Quake.getPlugin();
    }

    public String getName()
    {
        return name;
    }

    public String getDisplayName()
    {
        return alias;
    }

    public String getGivePerm()
    {
        return this.givePerm;
    }

    public Object clone() {
        Object o = null;
        try {
            // On récupère l'instance à renvoyer par l'appel de la
            // méthode super.clone()
            o = super.clone();
        } catch(CloneNotSupportedException cnse) {
            // Ne devrait jamais arriver car nous implémentons
            // l'interface Cloneable
            cnse.printStackTrace(System.err);
        }
        // on renvoie le clone
        return o;
    }
}
