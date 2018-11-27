package de.exfy.survivalgames;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.gamelib.maps.extra.Border;
import de.exfy.gamelib.maps.extra.BorderProvider;
import de.exfy.gamelib.maps.extra.Spawns;
import de.exfy.gamelib.maps.extra.SpawnsProvider;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SurvivalGamesMap {

	private static LoadingCache<MinecraftMap, SurvivalGamesMap> mapCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<MinecraftMap, SurvivalGamesMap>() {
				@Override
				public SurvivalGamesMap load(MinecraftMap minecraftMap) throws Exception {
					return new SurvivalGamesMap(minecraftMap);
				}
			});

	public static SurvivalGamesMap wrap(MinecraftMap map) {
		return mapCache.getUnchecked(map);
	}

	public static void reload(MinecraftMap map) {
		mapCache.invalidate(map);
	}

	public static void reloadAll() {
		mapCache.invalidateAll();
	}

	@Getter
	private MinecraftMap minecraftMap;

	@Getter
	private Spawns spawns;

	@Getter
	private Border border;

	@Getter
	private int maxPlayers;

	@Getter
	private int minPlayers;

	@Getter
	private int time;

	@Getter
	private String deathmatchMap;

	private static Multimap<String, Location> usedSpawns = HashMultimap.create();

	@Getter
	private boolean disabled;

	private SurvivalGamesMap(MinecraftMap minecraftMap) {
		this.minecraftMap = minecraftMap;
		this.spawns = SpawnsProvider.load(minecraftMap);
		this.border = BorderProvider.load(minecraftMap);

		JsonElement disabled = minecraftMap.getExtraData().get("disabled");
		if (disabled != null && disabled.getAsBoolean()) {
			this.disabled = true;
		}

		if (!isFullyConfigured()) {
			return;
		}

		this.maxPlayers = minecraftMap.getExtraData().get("max_players").getAsInt();
		this.minPlayers = minecraftMap.getExtraData().get("min_players").getAsInt();
		this.time = minecraftMap.getExtraData().get("time").getAsInt();
		this.deathmatchMap = minecraftMap.getExtraData().get("dmmap").getAsString();
	}

	public boolean shouldBeUsed() {
		return isFullyConfigured() && !disabled;
	}

	public boolean isFullyConfigured() {
		return getConfigureError() == null;
	}

	public String getConfigureError() {

		if (!minecraftMap.getExtraData().has("min_players") || !minecraftMap.getExtraData().has("max_players")) {
			return "Die Karte hat keine Spielerzahlen definiert.";
		}

		if (!minecraftMap.getExtraData().has("time")) {
			return "Die Zeit wurde nicht konfiguriert!";
		}

		if (!minecraftMap.getExtraData().has("dmmap")) {
			return "Die DM-Map wurde nicht konfiguriert!";
		}

		//if (!border.isDefined()) {
		//	return "Der Kartenrand muss noch definiert werden!";
		//}


		return null;
	}

	public Location getRandomSpawn(String team) {
		if (!usedSpawns.containsKey(team) || usedSpawns.get(team).size() < 1) {
			usedSpawns.putAll(team, spawns.getSpawns(team));
		}

		List<Location> uSpawns = new ArrayList<>(usedSpawns.get(team));
		Collections.shuffle(uSpawns);
		Location spawn = uSpawns.remove(0);
		usedSpawns.remove(team, spawn);
		return spawn;
	}

	public void invalidate() {
		reload(getMinecraftMap());
		getMinecraftMap().clearCache();
	}
}
