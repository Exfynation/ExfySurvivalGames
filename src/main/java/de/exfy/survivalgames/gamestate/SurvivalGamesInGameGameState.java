package de.exfy.survivalgames.gamestate;

import de.exfy.core.ExfyCore;
import de.exfy.core.helper.player.InfoScoreboard;
import de.exfy.core.helper.player.PlayerUtils;
import de.exfy.core.modules.Coins;
import de.exfy.core.modules.Stats;
import de.exfy.core.modules.intake.IntakeModule;
import de.exfy.core.modules.stats.GameStat;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.features.borderFeature.BorderFeature;
import de.exfy.gamelib.features.borderFeature.CircleInfluenceShape;
import de.exfy.gamelib.features.borderFeature.RectangleBorderShape;
import de.exfy.gamelib.features.disallowFeature.DisallowFeature;
import de.exfy.gamelib.features.disallowFeature.DisallowSettingsBuilder;
import de.exfy.gamelib.features.spectatorFeature.SpectatorFeature;
import de.exfy.gamelib.gameState.GameState;
import de.exfy.gamelib.gameState.general.IngameGameState;
import de.exfy.gamelib.maps.extra.Border;
import de.exfy.survivalgames.SurvivalGames;
import de.exfy.survivalgames.SurvivalGamesMap;
import de.exfy.survivalgames.SurvivalGamesPlayer;
import de.exfy.survivalgames.countdowns.Countdown;
import de.exfy.survivalgames.countdowns.DeathmatchCountdown;
import de.exfy.survivalgames.countdowns.FreezeCountdown;
import de.exfy.survivalgames.countdowns.PeaceCountdown;
import de.exfy.survivalgames.enums.SubGameState;
import de.exfy.survivalgames.gamestate.ingame.IngameCommands;
import de.exfy.survivalgames.gamestate.ingame.listener.ChestListener;
import de.exfy.survivalgames.gamestate.ingame.listener.DisallowPatchListener;
import de.exfy.survivalgames.gamestate.ingame.listener.IngameListener;
import de.exfy.survivalgames.helper.LobbyChatListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SurvivalGamesInGameGameState extends IngameGameState {

    private DisallowFeature disallowFeature;
    private SpectatorFeature spectatorFeature;

    private Set<Player> hiddenPlayers = new HashSet<>();

    private Set<Listener> listeners = new HashSet<>();
    private BukkitTask timeout;

    private Countdown countdown;

    @Getter
    private int remainingTime = SurvivalGames.getGameMap().getTime() * 60;

    private PlayerUtils.ResetFlags resetFlags = PlayerUtils.ResetFlags.defaultResetBuilder()
            .resetHeldItem(false).gameMode(GameMode.SURVIVAL).build();


    private BorderFeature borderFeature;

    @Getter
    @Setter
    private SubGameState subGameState;

    @Override
    public void onEnable() {
        super.onEnable();

        setSubGameState(SubGameState.FREEZE);

        disallowFeature = GameLib.getFeatureManager().getFeature(DisallowFeature.class);

        disallowFeature.setSettings(
                DisallowSettingsBuilder
                        .defaultDisallowBuilder()
                        .allowDealDamage()
                        .allowTakeDamage()
                        .allowHunger()
                        .allowPickUpItems()
                        .allowItemWorldInteraction()
                        .allowDropItems()
                        .allowUseInventory()
                        .allowAllInteraction()
                        .allowMove()
                        .build()
        );

        disallowFeature.enable();

        listeners.add(new DisallowPatchListener(this));
        listeners.add(new IngameListener(this));
        listeners.add(new ChestListener());
        listeners.add(new LobbyChatListener());

        listeners.forEach(l -> Bukkit.getPluginManager().registerEvents(l, ExfyCore.getInstance()));

        spectatorFeature = GameLib.getFeatureManager().getFeature(SpectatorFeature.class);
        spectatorFeature.enable();

        //borderFeature = GameLib.getFeatureManager().getFeature(BorderFeature.class);

        //SurvivalGamesMap map = SurvivalGames.getGameMap();
        //Border border = map.getBorder();
        //Location sm = border.getSmallCorner();
        //Location lg = border.getLargeCorner();

        //borderFeature.setWorld(SurvivalGames.getGameMap().getMinecraftMap().getWorld());
        //borderFeature.setBorderShape(new RectangleBorderShape(sm.getBlockX(), sm.getBlockZ(), lg.getBlockX(), lg.getBlockZ()));
        //borderFeature.setPlayerShape(new CircleInfluenceShape(7));


        Bukkit.getScheduler().runTaskAsynchronously(ExfyCore.getInstance(), () -> {
            SurvivalGames.getGameMap().getMinecraftMap().announce();


            Bukkit.getOnlinePlayers().forEach(p -> {
                GameStat games = Stats.getStats(p.getUniqueId()).getGameStats("LastManAlive").getStat("stat.games");
                games.addScore(1);
            });
        });

        Bukkit.getOnlinePlayers().forEach(this::preparePlayer);

        startCountdown();

        Bukkit.getScheduler().runTaskLater(ExfyCore.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(p -> SurvivalGamesPlayer.wrap(p).initScoreboard());
            Bukkit.getOnlinePlayers().forEach(p -> SurvivalGamesPlayer.wrap(p).getScoreboard().setActive());

            // timeout
            timeout = Bukkit.getScheduler().runTaskTimer(SurvivalGames.getInstance(), () -> {
                if (remainingTime-- <= 0) {
                    startCountdown();
                }
                SurvivalGamesPlayer.updateTimeForAll();
            }, 20, 20);

            //borderFeature.enable();
            super.loadingDone();

            IntakeModule.getCommandGraph().commands().registerMethods(new IngameCommands(this));
        }, 20L);

        for (Player player : getIngamePlayers()) {
            if(player.hasPermission("exfy.premiumplus")) {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a400 §7⛀ fürs Mitspielen, da du ein §aPremium+ §7Mitglied bist!");
                Coins.addCoins(player.getUniqueId(), 400);
                continue;
            }
            if(player.hasPermission("exfy.premium")) {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a300 §7⛀ fürs Mitspielen, da du ein §aPremium §7Mitglied bist!");
                Coins.addCoins(player.getUniqueId(), 300);
                continue;
            }
            player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a200 §7⛀ fürs Mitspielen!");
            Coins.addCoins(player.getUniqueId(), 200);
        }

    }

    public void playerKilled(Player player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(ExfyCore.getInstance(), () -> {
            Bukkit.getScheduler().runTaskLater(ExfyCore.getInstance(), () -> {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bist nun §aausgeschieden§7!");
                GameLib.getFeatureManager().getFeature(SpectatorFeature.class).addSpectator(player, false);

                if(getActivePlayers().size() <= 1) {
                    finishRound();
                    return;
                }

                if(getActivePlayers().size() <= 4) {
                    if(getSubGameState() != SubGameState.INGAME)
                        return;

                    timeout.cancel();
                    startCountdown();
                }
            }, 1L);
        }, 15L);
    }

    public void startCountdown() {
        if(getSubGameState().equals(SubGameState.FREEZE)) {
            this.countdown = new FreezeCountdown(this);
            this.countdown.onEnable();
        }
        else if(getSubGameState().equals(SubGameState.PEACE)) {
            this.countdown = new PeaceCountdown(this);
            this.countdown.onEnable();
        }
        else if(getSubGameState().equals(SubGameState.INGAME)) {
            setSubGameState(SubGameState.DEATHMATCH);
            this.countdown = new DeathmatchCountdown(this);
            this.countdown.onEnable();

            for(Player p : getIngamePlayers()) {
                InfoScoreboard scoreboard = new InfoScoreboard(p, "LastManAlive");
                scoreboard.new InfoEntry("deathmatch", "Deathmatch", "Möge der bessere gewinnen!");
                scoreboard.update();
            }
        }

    }


    public void preparePlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        PlayerUtils.reset(player, resetFlags);

        Location spawn = SurvivalGames.getGameMap().getRandomSpawn("general");

        player.teleport(spawn);
        player.teleport(spawn);
    }

    public boolean isHidden(Player player) {
        return hiddenPlayers.contains(player);
    }

    public boolean isAttackable(Player damager, Player damaged) {
        return !(isSpectator(damager) || !isActivelyPlaying(damaged)) && !damager.equals(damaged);
    }

    public Player getWinningPlayers() {

        if(getActivePlayers().size() > 1)
            return null;

        Player winner = getActivePlayers().iterator().next();

        return winner;
    }

    public void finishRound() {
        GameLib.getGameStateManager().setGameState(new SurvivalGamesFinishedGameState(getWinningPlayers()));
    }


    public void playerOffline(Player player) {
        if(getActivePlayers().size() <= 2) {
            finishRound();
            return;
        }

        if(getActivePlayers().size() <= 5) {
            startCountdown();
        }

        hiddenPlayers.remove(player);
    }

    public Collection<Player> getActivePlayers() {
        return spectatorFeature.getPlayingPlayers().stream().filter(this::isActivelyPlaying).collect(Collectors.toList());
    }

    public Collection<Player> getIngamePlayers() {
        return spectatorFeature.getPlayingPlayers();
    }

    @Override
    public String getMapName() {
        return SurvivalGames.getGameMap().getMinecraftMap().getName();
    }

    public boolean isActivelyPlaying(Player player) {
        return !isSpectator(player) && !isHidden(player);
    }

    public boolean isSpectator(Player player) {
        return spectatorFeature.getSpectators().contains(player);
    }


    @Override
    public void onDisable() {
        super.onDisable();

        if (IntakeModule.getCommandGraph() != null) IntakeModule.getCommandGraph().commands().unregisterMethods(this);

        countdown.onDisable();
        disallowFeature.disable();

        listeners.forEach(HandlerList::unregisterAll);
        listeners.clear();


        //BorderFeature borderFeature = GameLib.getFeatureManager().getFeature(BorderFeature.class);
        //borderFeature.disable();

        spectatorFeature.getPlayingPlayers().forEach(pl -> SurvivalGamesPlayer.wrap(pl).destroy());

        timeout.cancel();

    }

}
