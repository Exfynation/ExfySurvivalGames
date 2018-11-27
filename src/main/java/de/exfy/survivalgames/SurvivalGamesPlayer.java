package de.exfy.survivalgames;

import com.google.common.cache.CacheLoader;
import de.exfy.core.helper.player.InfoScoreboard;
import de.exfy.core.helper.player.PlayerAssigner;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.gameState.GameState;
import de.exfy.survivalgames.gamestate.SurvivalGamesInGameGameState;
import de.exfy.survivalgames.helper.DurationHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public class SurvivalGamesPlayer implements PlayerAssigner.Destroyable {

	private static final PlayerAssigner<SurvivalGamesPlayer> assigner = new PlayerAssigner<>(new CacheLoader<Player, SurvivalGamesPlayer>() {
		@Override
		public SurvivalGamesPlayer load(Player player) throws Exception {
			return new SurvivalGamesPlayer(player);
		}
	});

	public static SurvivalGamesPlayer wrap(Player player) {
		return assigner.wrap(player);
	}

	public static void destroyAll() {
		assigner.getEntries().forEach(assigner::remove);
	}

	public static void updateTimeForAll() {
		assigner.getEntries().forEach(player1 -> wrap(player1).updateRemainingTime());
	}

	@Getter
	private final Player player;

	@Getter
	private InfoScoreboard scoreboard;
	private InfoScoreboard.InfoEntry remainingTimeEntry;

	public SurvivalGamesPlayer(Player player) {
		this.player = player;
	}

	public void initScoreboard() {
		SurvivalGamesMap map = SurvivalGames.getGameMap();

		this.scoreboard = new InfoScoreboard(player, "LastManAlive");

		GameState state = GameLib.getGameStateManager().getCurrentGameState();
		if (state instanceof SurvivalGamesInGameGameState) {
			int remaining = ((SurvivalGamesInGameGameState) state).getRemainingTime();

			String time = DurationHelper.convertSecondsToCountdown(remaining);

			remainingTimeEntry = this.scoreboard.new InfoEntry("remaining", "Zeit", time);
			this.scoreboard.new InfoEntry("teams", "Teams", "§cVerboten");
		}

		this.scoreboard.update();
	}

	public void updateRemainingTime() {
		GameState state = GameLib.getGameStateManager().getCurrentGameState();
		if (state instanceof SurvivalGamesInGameGameState) {
			int remaining = ((SurvivalGamesInGameGameState) state).getRemainingTime();

			String time = DurationHelper.convertSecondsToCountdown(remaining);

			remainingTimeEntry.setValue(time);
			remainingTimeEntry.updateValue();
		}
	}

	@Override
	public void destroy() {
		if (getScoreboard() != null) {
			getScoreboard().destroy();
			scoreboard = null;
		}
	}
}
