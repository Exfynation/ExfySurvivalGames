package de.exfy.survivalgames.countdowns;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.TitleApi;
import de.exfy.survivalgames.SurvivalGames;
import de.exfy.survivalgames.gamestate.SurvivalGamesInGameGameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class DeathmatchCountdown extends Countdown implements Runnable {

    private final int WAIT_TIME_SECONDS = 15;

    private SurvivalGamesInGameGameState initializer;

    public DeathmatchCountdown(SurvivalGamesInGameGameState survivalGamesInGameGameState) {
        this.initializer = survivalGamesInGameGameState;
    }

    @Override
    public void onEnable() {
        seconds = WAIT_TIME_SECONDS;
        Bukkit.createWorld(new WorldCreator(SurvivalGames.getGameMap().getDeathmatchMap()));

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
                Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§7Das Deathmatch startet in §a" + seconds + " §7Sekunden.");
                break;
            case 1:
                displayTitle();
                Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§7Das Deathmatch startet in §aeiner §7Sekunde.");
                break;
            case 0:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 3, 2);
                }
                onDisable();
                Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§aDas Deathmatch hat begonnen!");


                for(Player player : Bukkit.getOnlinePlayers()) {

                    if(initializer.isSpectator(player)) {
                        Location spawn = SurvivalGames.getGameMap().getRandomSpawn("dmspec");
                        spawn.setWorld(Bukkit.getWorld(SurvivalGames.getGameMap().getDeathmatchMap()));

                        player.teleport(spawn);
                        player.teleport(spawn);

                    } else if(initializer.isActivelyPlaying(player)) {
                        Location spawn = SurvivalGames.getGameMap().getRandomSpawn("deathmatch");
                        spawn.setWorld(Bukkit.getWorld(SurvivalGames.getGameMap().getDeathmatchMap()));

                        player.teleport(spawn);
                        player.teleport(spawn);
                    }

                }

                break;
        }
        seconds--;

    }

    private void displayTitle() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            TitleApi.sendTitleTimes(p, 5, 30, 5);
            TitleApi.sendSubTitle(p, "§7Deathmatch startet in..");
            TitleApi.sendTitle(p, "§a" + seconds);
            p.playSound(p.getLocation(), Sound.NOTE_PLING, 3, 2);
        }
    }
}
