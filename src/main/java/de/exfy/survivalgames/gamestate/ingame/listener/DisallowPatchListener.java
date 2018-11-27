package de.exfy.survivalgames.gamestate.ingame.listener;

import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.features.spectatorFeature.SpectatorFeature;
import de.exfy.gamelib.gameState.GameState;
import de.exfy.survivalgames.gamestate.SurvivalGamesInGameGameState;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisallowPatchListener implements Listener {
	private final GameState gameState;

	public DisallowPatchListener(GameState gameState) {
		this.gameState = gameState;
	}

	@EventHandler
	public void onDoorInteract(PlayerInteractEvent e) {
		if (GameLib.getFeatureManager().getFeature(SpectatorFeature.class).getSpectators().contains(e.getPlayer())) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!e.getClickedBlock().getType().name().contains("DOOR")) return;

		e.setCancelled(false);
	}


	@EventHandler
	public void breakPainting(EntityDamageByEntityEvent e) {
		if (e.getDamager().getType() != EntityType.PLAYER) return;
		if (e.getEntityType() != EntityType.PAINTING) return;

		// don't damage paintings!
		e.setCancelled(true);
	}

	@EventHandler
	public void pressurePlate(PlayerInteractEvent e) {
		if (GameLib.getFeatureManager().getFeature(SpectatorFeature.class).getSpectators().contains(e.getPlayer())) return;
		if (e.getAction() != Action.PHYSICAL) return;

		e.setCancelled(false);
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent e) {
		if(e.getBlockPlaced().getType() == Material.WEB) return;

		e.setBuild(false);
		e.setCancelled(true);
	}
}
