package de.exfy.survivalgames.gamestate;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.Coins;
import de.exfy.core.modules.Stats;
import de.exfy.core.modules.stats.GameStat;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.features.borderFeature.BorderFeature;
import de.exfy.gamelib.features.spectatorFeature.SpectatorFeature;
import de.exfy.gamelib.gameState.GameState;
import de.exfy.survivalgames.SurvivalGames;
import de.exfy.survivalgames.gamestate.ingame.listener.DisallowPatchListener;
import de.exfy.survivalgames.gamestate.ingame.listener.IngameListener;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SurvivalGamesFinishedGameState extends GameState implements Listener {

    private final Player player;
    private Set<Listener> listeners = new HashSet<>();

    public SurvivalGamesFinishedGameState(Player winPlayer) {
        this.player = winPlayer;
    }

    @Override
    public String getName() {
        return "Finished";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, ExfyCore.getInstance());
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.getInventory().clear();
        });

        SpectatorFeature feature = GameLib.getFeatureManager().getFeature(SpectatorFeature.class);
        feature.disable();
        feature.enable();

        Bukkit.getOnlinePlayers().forEach(p -> {
            feature.addSpectator(p);

        });

        listeners.add(new DisallowPatchListener(this));
        listeners.forEach(l -> Bukkit.getPluginManager().registerEvents(l, ExfyCore.getInstance()));
        //listeners.add(new IngameListener(this));

        Color[] niceColors = new Color[]{
                Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.GREEN, Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE,
                Color.ORANGE, Color.PURPLE, Color.RED, Color.TEAL, Color.YELLOW
        };


        if (!(player == null || !player.isOnline())) {
            FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(niceColors[ThreadLocalRandom.current().nextInt(niceColors.length)])
                    .withTrail()
                    .build();

            Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();

            meta.addEffect(effect);
            meta.setPower(1);

            firework.setFireworkMeta(meta);
            firework.detonate();

            if (player.hasPermission("exfy.premiumplus")) {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a2500 §7⛀ für deinen Sieg, da du ein §aPremium+ §7Mitglied bist!");
                Coins.addCoins(player.getUniqueId(), 2500);
            } else if (player.hasPermission("exfy.premium")) {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a1500 §7⛀ für deinen Sieg, da du ein §aPremium §7Mitglied bist!");
                Coins.addCoins(player.getUniqueId(), 1500);
            } else {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a1000 §7⛀ für deinen Sieg!");
                Coins.addCoins(player.getUniqueId(), 1000);
            }

            Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§a" + player.getDisplayName() + " §7hat das Spiel gewonnen!");
            Bukkit.broadcastMessage(ExfyCore.getPrefix() + "§7Der Server wird neu gestartet..");

            GameStat games = Stats.getStats(player.getUniqueId()).getGameStats("LastManAlive").getStat("stat.wins");
            games.addScore(1);
        }


        Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), Bukkit::shutdown, 10 * 20);
    }

    @Override
    public void onDisable() {
        // well :)
        // let's try to be clean
        GameLib.getFeatureManager().getFeature(SpectatorFeature.class).disable();
        GameLib.getFeatureManager().getFeature(BorderFeature.class).disable();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage("");
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        e.setLeaveMessage("");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        e.getPlayer().kickPlayer("lobby");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getHeldItemSlot() == 8) {
            //Well, goodybe :)
            e.getPlayer().kickPlayer("lobby");
        }
    }
}
