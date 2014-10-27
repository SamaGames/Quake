package com.Geekpower14.Quake.Task;

import com.Geekpower14.Quake.Quake;

public class InfosSender implements Runnable{

	public Quake plugin;
	
	public boolean run = true;
	
	public InfosSender(Quake pl)
	{
		plugin = pl;
	}
	
	@Override
	public void run() {
		while(run)
		{
			plugin.cm.sendArenasInfos(true);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void disable()
	{
		run = false;
	}

}
