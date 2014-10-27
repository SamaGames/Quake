package com.Geekpower14.Quake.Arena;


public class TabHolder{

	public String[][]tabs;
	public int[][]tabPings;

	public int maxh = 0;

	public int maxv = 0;
	
	static int HORIZSZE = 3;
	static int VERTSIZE = 20;

	public TabHolder() {
		this.tabs = new String[20][VERTSIZE];
		this.tabPings = new int[20][VERTSIZE];
	}

	public TabHolder getCopy() {
		TabHolder newCopy = new TabHolder();
		newCopy.tabs = copyStringArray(tabs);
		newCopy.tabPings = copyIntArray(tabPings);
		return newCopy;
	}

	/* utils method, copy tab array to new array */
	private static String[][] copyStringArray(String[][] tab){
		int horzTabSize = HORIZSZE;
		int vertTabSize = VERTSIZE;

		String[][] temp = new String[horzTabSize][vertTabSize];
		for(int b = 0; b < vertTabSize; b++){
			for(int a = 0; a < horzTabSize ; a++){
				temp[a][b] = tab[a][b];
			}
		}
		return temp;
	}

	private static int[][] copyIntArray(int[][] tab){
		int horzTabSize = HORIZSZE;
		int vertTabSize = VERTSIZE;

		int[][] temp = new int[horzTabSize][vertTabSize];
		for(int b = 0; b < vertTabSize; b++){
			for(int a = 0; a < horzTabSize ; a++){
				temp[a][b] = tab[a][b];
			}
		}
		return temp;
	}
}
