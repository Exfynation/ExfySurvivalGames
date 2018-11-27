package de.exfy.survivalgames.helper;

public class DurationHelper {
	public static String convertSecondsToCountdown(int remaining) {
		String seconds = Integer.toString(remaining % 60);
		if (seconds.length() < 2) {
			seconds = "0" + seconds;
		}

		String minutes = Integer.toString(remaining / 60);
		return minutes + ":" + seconds;
	}
}
