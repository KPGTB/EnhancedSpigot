/*
 * Copyright 2025 KPG-TB
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.projectenhanced.enhancedspigot.util.time;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object that handles time (days, hours, minutes, seconds)
 */
@Getter public class EnhancedTime {
	private final long millis;
	private final String text;
	private final String input;

	/**
	 * Get object of Time from millis
	 *
	 * @param millis Milliseconds
	 */
	public EnhancedTime(long millis) {
		this.millis = millis;
		this.text = this.defaultFormat();
		this.input = this.as("<days:d><hours:h><minutes:m><seconds:s>", "");
	}

	/**
	 * Get object of Time from string
	 *
	 * @param text String in format XdXhXmXs (d=days, h=hours, m=minutes, s=seconds, X=integer)
	 */
	public EnhancedTime(String text) {
		this.millis = this.toMillis(text);
		this.text = this.defaultFormat();
		this.input = text;
	}

	/**
	 * Get time in ticks
	 *
	 * @return ticks
	 */
	public long getTicks() {
		return (millis / 1000L * 20L);
	}

	private long toMillis(String time) {
		String temp = "";

		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;

		for (String s : time.split("")) {
			int num = -1;

			try {
				num = Integer.parseInt(s);
			} catch (Exception e) {
			}

			if (num >= 0) {
				temp += num;
				continue;
			}

			if (temp.isEmpty()) {
				continue;
			}

			switch (s) {
				case "d":
					days = Integer.parseInt(temp);
					break;
				case "h":
					hours = Integer.parseInt(temp);
					break;
				case "m":
					minutes = Integer.parseInt(temp);
					break;
				case "s":
					seconds = Integer.parseInt(temp);
					break;
			}

			temp = "";
		}

		hours += days * 24;
		minutes += hours * 60;
		seconds += minutes * 60;

		return seconds * 1000L;
	}

	public String as(String format, String emptyReplace) {
		int seconds = (int) Math.floorDiv(this.millis, 1000L);
		int minutes = Math.floorDiv(seconds, 60);
		seconds -= minutes * 60;
		int hours = Math.floorDiv(minutes, 60);
		minutes -= hours * 60;
		int days = Math.floorDiv(hours, 24);
		hours -= days * 24;

		Map<String, Integer> timeMap = new HashMap<>();
		timeMap.put("days", days);
		timeMap.put("hours", hours);
		timeMap.put("minutes", minutes);
		timeMap.put("seconds", seconds);

		String output = format;
		Map<String, String> replaces = new HashMap<>();
		timeMap.forEach((key, value) -> {
			String regex = "<" + key + ":([^>]+)>";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(format);

			while (matcher.find()) {
				String extra = matcher.group(1);

				String toReplace = "<" + key + ":" + extra + ">";
				String replace = value + extra;

				if (value <= 0) {
					replaces.put(toReplace, "");
				} else {
					replaces.put(toReplace, replace);
				}
			}
		});
		for (Map.Entry<String, String> entry : replaces.entrySet()) {
			output = output.replaceAll(entry.getKey(), entry.getValue());
		}

		return output.isEmpty() ?
			emptyReplace :
			output;
	}

	private String defaultFormat() {
		return as(Configuration.FORMAT, Configuration.EMPTY_REPLACER);
	}

	@Override
	public String toString() {
		return this.input;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EnhancedTime time = (EnhancedTime) o;
		return millis == time.millis;
	}

	@Override
	public int hashCode() {
		return Objects.hash(millis);
	}

	public static class Configuration {
		public static String FORMAT = "<days:d ><hours:h ><minutes:m ><seconds:s>";
		public static String EMPTY_REPLACER = "now";
	}
}
