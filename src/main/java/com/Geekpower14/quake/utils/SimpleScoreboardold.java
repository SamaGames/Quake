package com.Geekpower14.quake.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class SimpleScoreboardold {

	private Scoreboard scoreboard;
	private Objective obj = null;

	private String title;
	private Map<String, Integer> scores;
	private List<Team> teams;
	
	private List<OfflinePlayer> last = new ArrayList<>();

	public SimpleScoreboardold(Scoreboard b, String title) {
		this.scoreboard = b;
		this.title = title;
		this.scores = Maps.newLinkedHashMap();
		this.teams = Lists.newArrayList();
	}

	public SimpleScoreboardold(String title) {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.title = title;
		this.scores = Maps.newLinkedHashMap();
		this.teams = Lists.newArrayList();
	}

	public void blankLine() {
		add(ChatColor.RESET+" ");
	}

	public void clear()
	{
		scores.clear();
		teams.clear();
	}

	public void add(String text) {
		add(text, null);
	}

	public void add(String text, Integer score) {
		Preconditions.checkArgument(text.length() < 48, "text cannot be over 48 characters in length");
		text = fixDuplicates(text);
		scores.put(text, score);
	}

	private String fixDuplicates(String text) {
		/*while (scores.containsKey(text))
			text += "§r";*/
		if (text.length() > 48)
			text = text.substring(0, 47);
		return text;
	}

	private Map.Entry<Team, String> createTeam(String text) {
		String result = "";
		if (text.length() <= 16)
			return new AbstractMap.SimpleEntry<>(null, text);
			Team team = scoreboard.registerNewTeam("text-" + scoreboard.getTeams().size());
			Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
			team.setPrefix(iterator.next());
			result = iterator.next();
			if (text.length() > 32)
				team.setSuffix(iterator.next());
			teams.add(team);
			return new AbstractMap.SimpleEntry<>(team, result);
	}

	public void build() {

		String tt = (title.length() > 16 ? title.substring(0, 15) : title);
		
		if(obj == null)
		{
			obj = scoreboard.registerNewObjective(tt, "dummy");
			obj.setDisplayName(title);
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		last.clear();

		int index = scores.size();

		for (Map.Entry<String, Integer> text : scores.entrySet()) {
			Map.Entry<Team, String> team = createTeam(text.getKey());
			Integer score = text.getValue() != null ? text.getValue() : index;
			OfflinePlayer player = Bukkit.getOfflinePlayer(team.getValue());
			
			if (team.getKey() != null)
				team.getKey().addPlayer(player);
			
			last.add(player);
			obj.getScore(player.getName()).setScore(score);
			index -= 1;
		}
	}

	public void reset() {
		//title = null;
		scores.clear();
		for (Team t : teams)
			t.unregister();
		teams.clear();
		
		for(OfflinePlayer op : last)
		{
			scoreboard.resetScores(op.getName());
		}
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void send(Player... players) {
		for (Player p : players)
			p.setScoreboard(scoreboard);
	}

}