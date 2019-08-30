package com.mindtree.timesheet;

import java.util.ArrayList;

public class Intro {
	
	public static void start() {
		ArrayList<String> start = new ArrayList<String>();
		start.add("	 ____    _____      _      ____    _____");
		start.add("	/ ___|  |_   _|    / \\    |  _ \\  |_   _|");
		start.add("	\\___ \\    | |     / _ \\   | |_) |   | |");
		start.add("	 ___) |   | |    / ___ \\  |  _ <    | |");
		start.add("	|____/    |_|   /_/   \\_\\ |_| \\_\\   |_|");
		start.forEach((n) -> System.out.println(n));
	}
	
	public static void end() {
		ArrayList<String> start = new ArrayList<String>();
		start.add("	 _____   _   _   ____");
		start.add("	| ____| | \\ | | |  _ \\");
		start.add("	|  _|   |  \\| | | | | |");
		start.add("	| |___  | |\\  | | |_| |");
		start.add("	|_____| |_| \\_| |____/");
		start.forEach((n) -> System.out.println(n));
	}

}
