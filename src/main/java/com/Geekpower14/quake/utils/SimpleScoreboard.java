package com.Geekpower14.quake.utils;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;

public class SimpleScoreboard {

	private Scoreboard scoreboard;
	private Objective obj = null;

	private String title;
	private String pseudo;
	private int pos;
	private Map<String, Integer> scores;
	private Map<String, Integer> scores_;
	//private Team team = null;
	
	public SimpleScoreboard(Scoreboard b, String title, String pseudo) {
		this.scoreboard = b;
		this.pseudo = pseudo;
		this.title = title;
		this.scores = Maps.newLinkedHashMap();
		//this.team = null;
	}

	public void clear()
	{
		scores.clear();
	}

	public void add(String text) {
		add(text, null);
	}
	
	public void setPos(int s)
	{
		pos = s;
	}

	public void add(String text, Integer score) {
		//Preconditions.checkArgument(text.length() < 16, "text cannot be over 16 characters in length");
		text = fixDuplicates(text);
		scores.put(text, score);
	}

	private String fixDuplicates(String text) {
		/*while (scores.containsKey(text))
			text += "§r";*/
		if (text.length() > 16)
			text = text.substring(0, 15);
		return text;
	}

	/*private Map.Entry<Team, String> createTeam(String text) {
		String result = "";
		if (text.length() <= 16)
			return new AbstractMap.SimpleEntry<>(null, text);
			Team team = scoreboard.registerNewTeam("text-" + pseudo);
			Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
			team.setPrefix(iterator.next());
			result = iterator.next();
			if (text.length() > 32)
				team.setSuffix(iterator.next());
			//teams.add(team);
			this.team = team;
			return new AbstractMap.SimpleEntry<>(team, result);
	}*/

	@SuppressWarnings("deprecation")
	public void build() {

		String tt = (title.length() > 16 ? title.substring(0, 15) : title);
		
		if(obj == null)
		{
			obj = scoreboard.registerNewObjective(tt, "dummy");
			obj.setDisplayName(title);
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		/*if(team == null)
		{
			team = scoreboard.registerNewTeam(pseudo);
			team.addPlayer(Bukkit.getOfflinePlayer(pseudo.substring(1)));
		}*/
		
		for(OfflinePlayer sp : scoreboard.getPlayers())
		{
			if(!scores.containsKey(sp.getName()))
				scoreboard.resetScores(sp);
		}
		
		//team.setPrefix(pos+ ". " + ChatColor.RED + ChatColor.BOLD + pseudo.charAt(0));
		
		for(String sp : scores.keySet())
		{
			int score = scores.get(sp);

			obj.getScore(Bukkit.getOfflinePlayer(sp)).setScore(score);
		}

	}

	public void reset() {
		//title = null;
		scores_ = scores;
		scores.clear();
		//team.unregister();
		
		/*for(OfflinePlayer op : last)
		{
			scoreboard.resetScores(op.getName());
		}*/
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void send(Player... players) {
		for (Player p : players)
			p.setScoreboard(scoreboard);
	}

}