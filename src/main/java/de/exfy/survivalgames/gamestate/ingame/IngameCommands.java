package de.exfy.survivalgames.gamestate.ingame;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import de.exfy.survivalgames.gamestate.SurvivalGamesInGameGameState;

public class IngameCommands {
	private final SurvivalGamesInGameGameState gameState;

	public IngameCommands(SurvivalGamesInGameGameState gameState) {
		this.gameState = gameState;
	}

	@Command(aliases = "endgame", desc = "Beendet das Spiel JETZT")
	@Require("exfy.endgame")
	public void endGame() {
		gameState.finishRound();
	}

}
