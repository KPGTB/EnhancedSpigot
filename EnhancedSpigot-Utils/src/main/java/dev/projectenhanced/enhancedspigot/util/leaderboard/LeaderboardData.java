/*
 * Copyright 2026 KPG-TB
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

package dev.projectenhanced.enhancedspigot.util.leaderboard;

import dev.projectenhanced.enhancedspigot.util.Pair;
import dev.projectenhanced.enhancedspigot.util.time.EnhancedTime;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter @Setter public class LeaderboardData<K, V> {
	private final String key;

	private final List<String> header;
	/*
	Placeholders:
	- <position> - entry position in the leaderboard
	- <key> - entry key
	- <value> - entry value
	 */
	private final String entryFormat;
	/*
	Placeholders:
	- <value> - entry value
	- <position> - entry position in the leaderboard
	- <refresh> - time until the next refresh in a human-readable format
	 */
	private final List<String> footer;

	private final Supplier<CompletableFuture<Map<K, V>>> entiresSupplier;
	private final Comparator<V> comparator;
	private final Function<OfflinePlayer, Pair<K, V>> placeholderParser;
	private final Function<V, String> valueFormatter;

	@Getter private final SequencedMap<K, V> cachedLeaderboard;
	@Getter private final SequencedMap<String, V> cachedRanges; // key - position range (e.g. "1-10"), value - value of the last entry in the range
	@Getter private long nextRefresh;

	@Setter
	@Getter
	private int leaderboardDisplaySize = 10;
	@Setter
	@Getter
	private int leaderboardSize = 20;
	@Setter
	@Getter
	private List<Pair<Integer, Integer>> ranges = List.of(
		new Pair<>(1, 50), new Pair<>(51, 100), new Pair<>(101, 500), new Pair<>(501, 1000), new Pair<>(1001, 5000), new Pair<>(5001, 10000), new Pair<>(10001, 50000), new Pair<>(50001, 100000),
		new Pair<>(100001, 500000), new Pair<>(500001, 1000000)
	); // The first one and the last one is dynamic

	public LeaderboardData(String key, List<String> header, String entryFormat, List<String> footer, Supplier<CompletableFuture<Map<K, V>>> entiresSupplier, Comparator<V> comparator, Function<OfflinePlayer, Pair<K, V>> placeholderParser, Function<V, String> valueFormatter) {
		this.key = key;
		this.header = header;
		this.entryFormat = entryFormat;
		this.footer = footer;
		this.entiresSupplier = entiresSupplier;
		this.comparator = comparator;
		this.placeholderParser = placeholderParser;
		this.valueFormatter = valueFormatter;

		this.cachedLeaderboard = new LinkedHashMap<>();
		this.cachedRanges = new LinkedHashMap<>();
		this.nextRefresh = System.currentTimeMillis();
	}

	public String getPosition(K key, V value) {
		if (this.cachedLeaderboard.containsKey(key)) {
			return String.valueOf(new ArrayList<>(this.cachedLeaderboard.keySet()).indexOf(key));
		}

		if (this.cachedRanges.isEmpty()) return "-";

		for (Map.Entry<String, V> entry : this.cachedRanges.entrySet()) {
			if (this.comparator.compare(value, entry.getValue()) < 0) return entry.getKey();
		}

		return this.cachedRanges.lastEntry()
			.getKey();
	}

	public Pair<K, V> getEntryAtPosition(int position) {
		if (position < 1 || position > this.cachedLeaderboard.size()) return null;
		Map.Entry<K, V> entry = new ArrayList<>(this.cachedLeaderboard.entrySet()).get(position - 1);
		return new Pair<>(entry.getKey(), entry.getValue());
	}

	public void refresh(long nextRefresh) {
		this.cachedLeaderboard.clear();
		this.cachedRanges.clear();

		this.entiresSupplier.get()
			.thenAcceptAsync((entries) -> {
				List<Map.Entry<K, V>> sorted = entries.entrySet()
					.stream()
					.sorted((e1, e2) -> this.comparator.compare(e1.getValue(), e2.getValue()))
					.toList();

				if (sorted.isEmpty()) return;

				for (int i = 0; i < Math.min(this.leaderboardSize, sorted.size()); i++) {
					Map.Entry<K, V> entry = sorted.get(i);
					this.cachedLeaderboard.put(entry.getKey(), entry.getValue());
				}

				boolean isFirstRange = true;
				for (Pair<Integer, Integer> range : this.ranges) {
					if (isFirstRange && range.getSecond() <= this.cachedLeaderboard.size()) continue;

					boolean isLastRange = sorted.size() <= range.getSecond();
					String rangeDisplay = (isFirstRange ?
						this.cachedLeaderboard.size() + 1 :
						range.getFirst()) // Range Start
						+ (isLastRange ?
						"+" :
						" - " + range.getSecond()); // Range End
					V rangeValue = sorted.get(Math.min(range.getSecond() - 1, sorted.size() - 1))
						.getValue();
					isFirstRange = false;

					this.cachedRanges.put(rangeDisplay, rangeValue);
				}
			})
			.thenRun(() -> {
				this.nextRefresh = nextRefresh;
			});
	}

	public List<String> getLeaderboardText(@Nullable Pair<K, V> yourValue) {
		List<String> result = new ArrayList<>(this.header);

		List<Map.Entry<K, V>> list = new ArrayList<>(this.cachedLeaderboard.entrySet());
		for (int i = 0; i < Math.min(this.cachedLeaderboard.size(), this.leaderboardDisplaySize); i++) {
			Map.Entry<K, V> entry = list.get(i);

			result.add(this.entryFormat.replace("<position>", String.valueOf(i + 1))
				.replace("<key>", String.valueOf(entry.getKey()))
				.replace("<value>", this.valueFormatter.apply(entry.getValue())));
		}

		String valueReplace = yourValue == null ?
			"<value>" :
			this.valueFormatter.apply(yourValue.getSecond());
		String positionReplace = yourValue == null ?
			"<position>" :
			this.getPosition(yourValue.getFirst(), yourValue.getSecond());

		for (String line : this.footer) {
			result.add(line.replace("<value>", valueReplace)
				.replace("<position>", positionReplace)
				.replace("<refresh>", new EnhancedTime(Math.max(0, this.nextRefresh - System.currentTimeMillis())).getText()));
		}

		return result;
	}

	/*
	key_<position>
	value_<position>
	key_me
	value_me
	position_me
	 */
	public String handlePlaceholder(OfflinePlayer offlinePlayer, String placeholder) {
		String[] parts = placeholder.split("_", 2);
		if (parts.length != 2) return null;

		if (parts[1].equalsIgnoreCase("me")) {
			Pair<K, V> pair = this.placeholderParser.apply(offlinePlayer);
			if (pair == null) return null;

			return switch (parts[0].toLowerCase()) {
				case "key" -> String.valueOf(pair.getFirst());
				case "value" -> this.valueFormatter.apply(pair.getSecond());
				case "position" -> this.getPosition(pair.getFirst(), pair.getSecond());
				default -> null;
			};
		}

		try {
			int position = Integer.parseInt(parts[1]);
			Pair<K, V> pair = this.getEntryAtPosition(position);
			if (pair == null) return null;

			return switch (parts[0].toLowerCase()) {
				case "key" -> String.valueOf(pair.getFirst());
				case "value" -> this.valueFormatter.apply(pair.getSecond());
				default -> null;
			};
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
