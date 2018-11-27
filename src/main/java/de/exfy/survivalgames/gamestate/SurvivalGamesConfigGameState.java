package de.exfy.survivalgames.gamestate;

import de.exfy.core.modules.intake.IntakeModule;
import de.exfy.gamelib.gameState.general.ConfigGameState;
import de.exfy.survivalgames.SurvivalGames;
import de.exfy.survivalgames.gamestate.config.*;

public class SurvivalGamesConfigGameState extends ConfigGameState {

	private GeneralConfigCommands generalCommands;

	public SurvivalGamesConfigGameState(String error) {
		super(SurvivalGames.getInstance()::checkPrerequisites, error);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		generalCommands = new GeneralConfigCommands();
		
		IntakeModule.getCommandGraph().commands()
				.group("lobbyconfig")
					.registerMethods(new LobbyCommands())
					.parent()
				.group("map")
					.registerMethods(new MapCommands())
					.group("spawns")
						.registerMethods(new SpawnCommands())
						.parent()
					.group("border")
						.registerMethods(new BorderCommand())
						.parent()
					.parent()
				.registerMethods(generalCommands);
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
		IntakeModule.getCommandGraph().commands()
				.unregisterGroup("lobbyconfig")
				.unregisterGroup("map")
				.unregisterMethods(generalCommands);
	}
}
