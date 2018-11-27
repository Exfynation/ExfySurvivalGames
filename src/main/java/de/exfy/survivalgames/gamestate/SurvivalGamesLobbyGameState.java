package de.exfy.survivalgames.gamestate;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.intake.IntakeModule;
import de.exfy.gamelib.features.lobbyFeature.LobbySettings;
import de.exfy.gamelib.gameState.GameState;
import de.exfy.gamelib.gameState.general.LobbyGameState;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.gamelib.maps.extra.SpawnsProvider;
import de.exfy.survivalgames.LobbyPlayer;
import de.exfy.survivalgames.SurvivalGames;
import de.exfy.survivalgames.SurvivalGamesMap;
import de.exfy.survivalgames.helper.LobbyChatListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

public class SurvivalGamesLobbyGameState extends LobbyGameState implements Listener {

	private Listener chatListener;

	@Override
	public void onEnable() {
		super.onEnable();

		//CatchGameFeature feature = GameLib.getFeatureManager().getFeature(CatchGameFeature.class);
		//feature.enable();

		Bukkit.getPluginManager().registerEvents(this, ExfyCore.getInstance());
		Bukkit.getPluginManager().registerEvents(chatListener = new LobbyChatListener(), ExfyCore.getInstance());


		IntakeModule.getCommandGraph().commands()
				.registerMethods(this);
	}

	@Override
	public void onDisable() {
		super.onDisable();

		HandlerList.unregisterAll(this);

		//CatchGameFeature feature = GameLib.getFeatureManager().getFeature(CatchGameFeature.class);
		//feature.disable();

		if(Bukkit.getOnlinePlayers().size() != 0) LobbyPlayer.destroyAll();

		if(IntakeModule.getCommandGraph() != null) IntakeModule.getCommandGraph().commands().unregisterMethods(this);
	}

	@Override
	public String getMapName() {
		return SurvivalGames.getGameMap().getMinecraftMap().getName();
	}

	@Override
	protected LobbySettings getLobbySettings() {
		MinecraftMap lobby = Maps.getLobby();
		Collection<Location> spawns = SpawnsProvider.load(lobby).getGeneralSpawns();
		SurvivalGamesMap map = SurvivalGames.getGameMap();
		return new LobbySettings(map.getMinPlayers(), map.getMaxPlayers(), 30, lobby.getWorldFolderName(), spawns.iterator().next());
	}

	@Override
	protected GameState getNextGameState() {
		return new SurvivalGamesInGameGameState();
	}
	
	@Override
	protected GameState getConfigureGameState() {
		return new SurvivalGamesConfigGameState(null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		LobbyPlayer.wrap(p).initScoreboard();
	}
}
