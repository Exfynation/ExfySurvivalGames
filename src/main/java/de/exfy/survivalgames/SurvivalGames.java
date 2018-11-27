package de.exfy.survivalgames;

import com.google.gson.JsonObject;
import de.exfy.cloud.ExfyCloud;
import de.exfy.cloud.bukkit.NetworkEventCalledEvent;
import de.exfy.core.ExfyCore;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.maps.MapManager;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.gamelib.maps.extra.Spawns;
import de.exfy.gamelib.maps.extra.SpawnsProvider;
import de.exfy.survivalgames.gamestate.SurvivalGamesConfigGameState;
import de.exfy.survivalgames.gamestate.SurvivalGamesLobbyGameState;
import de.exfy.survivalgames.helper.MapHelper;
import de.exfy.survivalgames.helper.MessageListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SurvivalGames extends JavaPlugin implements Listener {

	private SurvivalGamesMap gameMap;
	
	public static SurvivalGames getInstance() {
		return getPlugin(SurvivalGames.class);
	}

	public String checkPrerequisites() {
		MinecraftMap lobby = Maps.getLobby();
		if (lobby == null) {
			return "Keine Lobby definiert!";
		}

		Spawns spawns = SpawnsProvider.load(lobby);
		if (spawns.getGeneralSpawns().size() < 1) {
			// no spawns in lobby
			return "Keine Spawns für die Lobby definiert!";
		}

		if (Maps.getMaps().size() < 1) {
			// no maps defined
			return "Keine Maps definiert!";
		}

		boolean goodMap = false;
		for (MinecraftMap m : Maps.getMaps()) {
			SurvivalGamesMap map = SurvivalGamesMap.wrap(m);
			if (map.shouldBeUsed()) {
				map.invalidate();
				goodMap = true;
				break;
			}

			map.invalidate();
		}

		if (!goodMap) {
			return "Keine Map ist voll konfiguriert und aktiviert!";
		}

		// this method must be extended with all prerequisites for the gamemode
		return null;
	}

	private void checkPrerequisitesFail(String str) {
		ExfyCore.getInstance().getLogger().warning(str);
		GameLib.getGameStateManager().setGameState(new SurvivalGamesConfigGameState(str));
	}

	@Override
	public void onEnable() {
		if(!ExfyCore.getInstance().isModuleLoaded("GameLib")) ExfyCore.getInstance().enableModule("GameLib");

		ExfyCloud.setCustomNameFormat("LastManAlive");
		GameLib.setCurrentGameInfo("LastManAlive", "Überlebe als Letzter!");

		ExfyCloud.registerEventListener("minigame_map_data");
		ExfyCloud.registerEventListener("minigame_select_map");
		Bukkit.getPluginManager().registerEvents(this, this);

		requestMap();


	}

	private void requestMap() {
		System.out.println("Requesting Map / Waiting for Map..");
		JsonObject data = new JsonObject();
		data.addProperty("gamemode", "LastManAlive");
		ExfyCloud.emitEvent("minigame_select_map", data);
	}

	@EventHandler
	public void onMapSelect(NetworkEventCalledEvent e) {
		if(e.getEvent().equals("minigame_map_data")) {
			JsonObject data = e.getData().getAsJsonObject();

			if(!data.get("server").getAsString().equals(System.getProperty("exfycloud.name"))) return;

			String map = data.get("map").getAsString();
			continueStartup(map);
			System.out.println("Continuing Startup with Map " + map);
		}
	}

	public void continueStartup(String map) {
		MapManager.setTable("survival_games_maps");
		MapHelper.initMap(map);

		String error;
		if ((error = this.checkPrerequisites()) != null) {
			this.checkPrerequisitesFail(error);
			return;
		}

		Bukkit.getPluginManager().registerEvents(new MessageListener(), ExfyCore.getInstance());
		GameLib.getGameStateManager().setGameState(new SurvivalGamesLobbyGameState());
	}

	public static SurvivalGamesMap getGameMap() {
		return getInstance().gameMap;
	}
	
	public static void setGameMap(SurvivalGamesMap map) {
		getInstance().gameMap = map;
	}
}
