package com.geekpower14.quake.classes;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of Quake.
 *
 * Quake is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quake is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quake.  If not, see <http://www.gnu.org/licenses/>.
 */
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
