package de.exfy.survivalgames.gamestate.ingame.listener;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.Stats;
import de.exfy.core.modules.stats.GameStat;
import de.exfy.survivalgames.enums.DeathType;
import de.exfy.survivalgames.enums.SubGameState;
import de.exfy.survivalgames.gamestate.SurvivalGamesInGameGameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class IngameListener implements Listener {

    private final SurvivalGamesInGameGameState gameState;
    private List<String> killedDeathMessages;
    private List<String> suicideDeathMessages;

    public IngameListener(SurvivalGamesInGameGameState gameState) {
        this.gameState = gameState;
        initDeathMessageList();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        gameState.playerOffline(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        gameState.playerOffline(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (gameState.getSubGameState() == SubGameState.FREEZE) {
            if (!(e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())) {
                Location loc = e.getFrom();
                loc.setYaw(e.getTo().getYaw());
                loc.setPitch(e.getTo().getPitch());
                e.setTo(loc);
            }

        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getEntity().getKiller() instanceof Player) {
                GameStat killer = Stats.getStats(e.getEntity().getKiller().getUniqueId()).getGameStats("LastManAlive").getStat("stat.kills");
                killer.addScore(1);

                GameStat death = Stats.getStats(e.getEntity().getUniqueId()).getGameStats("LastManAlive").getStat("stat.deaths");
                death.addScore(1);

                e.setDeathMessage(getRandomDeathMessage(DeathType.KILLED).replace("{PLAYER}", e.getEntity().getKiller().getDisplayName()).replace("{TARGET}", e.getEntity().getDisplayName()));
                gameState.playerKilled(e.getEntity());
            } else if (e.getEntity().getKiller() == null || e.getEntity().getKiller() == e.getEntity()) {
                GameStat death = Stats.getStats(e.getEntity().getUniqueId()).getGameStats("SurvivalGames").getStat("stat.deaths");
                death.addScore(1);

                e.setDeathMessage(getRandomDeathMessage(DeathType.SUICIDE).replace("{PLAYER}", e.getEntity().getDisplayName()));
                gameState.playerKilled(e.getEntity());
            }

        }

    }


    private void initDeathMessageList() {
        suicideDeathMessages = new ArrayList<>();
        killedDeathMessages = new ArrayList<>();

        suicideDeathMessages.add("§8≫ §a{PLAYER} §7hat sich zu weit aus dem Fenster gelehnt.");
        suicideDeathMessages.add("§8≫ §a{PLAYER} §7hat die Nadel im Heuhaufen nicht gefunden.");
        suicideDeathMessages.add("§8≫ §a{PLAYER} §7ist an einem Splitter verblutet.");
        suicideDeathMessages.add("§8≫ §a{PLAYER} §7ist in der Toilette ertrunken.");
        suicideDeathMessages.add("§8≫ §a{PLAYER} §7hat in den Spiegel gesehen.");

        killedDeathMessages.add("§8≫ §a{PLAYER} §7hat §a{TARGET} §7getötet.");
        killedDeathMessages.add("§8≫ §a{PLAYER} §7hat §a{TARGET} §7eine zu stark gewischt.");
        killedDeathMessages.add("§8≫ §a{PLAYER} §7hat §a{TARGET} §7zu stark geschubst.");
        killedDeathMessages.add("§8≫ §a{PLAYER} §7hat §a{TARGET} §7mit einer Nadel gepiekst.");
    }

    private String getRandomDeathMessage(DeathType type) {

        if (type == DeathType.KILLED) {
            return this.killedDeathMessages.get(new Random().nextInt(this.killedDeathMessages.size()));
        }
        else if (type == DeathType.SUICIDE) {
            return this.suicideDeathMessages.get(new Random().nextInt(this.suicideDeathMessages.size()));
        }
        return "";
    }

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(gameState.getSubGameState() == SubGameState.PEACE || gameState.getSubGameState() == SubGameState.FREEZE) {
			e.setCancelled(true);
			return;
		}
	}
}
