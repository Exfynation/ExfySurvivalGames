package de.exfy.survivalgames.countdowns;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.TitleApi;
import de.exfy.survivalgames.gamestate.SurvivalGamesInGameGameState;
import de.exfy.survivalgames.enums.SubGameState;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class FreezeCountdown extends Countdown implements Runnable {

    private final int FREEZE_TIME_SECONDS = 15;

    private SurvivalGamesInGameGameState initializer;

    public FreezeCountdown(SurvivalGamesInGameGameState survivalGamesInGameGameState) {
        this.initializer = survivalGamesInGameGameState;
    }

    @Override
    public void onEnable() {
        seconds = FREEZE_TIME_SECONDS;
        task = Bukkit.getScheduler().runTaskTimer(ExfyCore.getInstance(), this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        task.cancel();
    }

    @Override
    public void run() {

        switch (seconds) {
            case 15:
            case 10:
            case 5:
            case 4:
            case 3:
            case 2:
                displayTitle();
                Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§7Die Jagd beginnt in §a" + seconds + " §7Sekunden.");
                break;
            case 1:
                displayTitle();
                Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§7Die Jagd beginnt in §aeiner §7Sekunde.");
                break;
            case 0:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 3, 2);
                }
                onDisable();
                Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§aDie Jagd hat begonnen.");
                initializer.setSubGameState(SubGameState.PEACE);
                initializer.startCountdown();
                break;

        }
        seconds--;

    }

    private void displayTitle() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            TitleApi.sendTitleTimes(p, 5, 30, 5);
            TitleApi.sendSubTitle(p, "§7Spiel startet in..");
            TitleApi.sendTitle(p, "§a" + seconds);
            p.playSound(p.getLocation(), Sound.NOTE_PLING, 3, 2);
        }
    }
}
