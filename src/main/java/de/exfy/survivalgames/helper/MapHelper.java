package de.exfy.survivalgames.helper;

import de.exfy.core.ExfyCore;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.survivalgames.SurvivalGames;
import de.exfy.survivalgames.SurvivalGamesMap;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapHelper {
	
	public static void initMap(String mapName) {
		if (Maps.getLobby() == null || Maps.getMaps().size() == 0) {
			return;
		}

		for(World w : Bukkit.getWorlds()) {
			if(w.getName().equals(Maps.getLobby().getName())) continue;
			Bukkit.unloadWorld(w, false);
		}

		MinecraftMap map = null;
		while (map == null) {
			List<MinecraftMap> mapList = new ArrayList<>(Maps.getMaps());
			Collections.shuffle(mapList);
			map = mapList.remove(0);
			SurvivalGamesMap wrap = SurvivalGamesMap.wrap(map);
			if (!wrap.shouldBeUsed() || !wrap.getMinecraftMap().getName().equals(mapName)) map = null;
			wrap.invalidate();
		}

		map.load();
		SurvivalGames.setGameMap(SurvivalGamesMap.wrap(map));

		MinecraftMap m = SurvivalGames.getGameMap().getMinecraftMap();
		World w = m.getWorld();
		w.setPVP(true);
		w.setStorm(false);
		w.setThundering(false);
		w.setWeatherDuration(0);
		w.setTime(9000);
		w.setDifficulty(Difficulty.EASY);
		w.setGameRuleValue("doDaylightCycle", "false");
		w.setGameRuleValue("doFireTick", "false");
		w.setGameRuleValue("doMobSpawning", "false");
		w.setGameRuleValue("keepInventory", "false");
		w.setGameRuleValue("mobGriefing", "false");
		w.setGameRuleValue("naturalRegeneration", "true");

		Bukkit.setDefaultGameMode(GameMode.ADVENTURE);
		Bukkit.setSpawnRadius(0);

		Bukkit.getScheduler().runTaskTimer(ExfyCore.getInstance(), () -> {
			w.setStorm(false);
			w.setThundering(false);
			w.setTime(9000);
		}, 0, 20);
	}
}
