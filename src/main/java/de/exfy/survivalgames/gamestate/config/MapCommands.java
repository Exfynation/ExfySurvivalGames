package de.exfy.survivalgames.gamestate.config;

import com.google.gson.JsonObject;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.intake.parametric.annotation.Range;
import com.sk89q.intake.parametric.annotation.Switch;
import de.exfy.core.modules.intake.module.classifier.Sender;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.survivalgames.SurvivalGamesMap;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class MapCommands {

	@Command(aliases = "define", desc = "Definiert eine neue Map")
	public void define(@Sender Player player, String name, @Optional String builder) throws CommandException {
		World world = player.getWorld();

		if (builder == null) {
			builder = "Exfy-Team";
		}

		if (Maps.getMap(name) != null) {
			throw new CommandException("Eine Map mit dem Namen " + name + " existiert bereits!");
		}

		MinecraftMap map = new MinecraftMap(name, builder, world.getName(), null);
		Maps.setMap(map);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map wurde definiert! Sie muss noch konfiguriert werden.");
	}

	@Command(aliases = "disable", desc = "Deaktiviert eine Map")
	public void disable(@Sender Player player, MinecraftMap m) {
		m.getExtraData().remove("disabled");
		m.getExtraData().addProperty("disabled", true);

		Maps.setMap(m);
		SurvivalGamesMap.reload(m);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName() +
				" §7wurde erfolgreich deaktiviert!");
	}

	@Command(aliases = "enable", desc = "Aktiviert eine Map")
	public void enable(@Sender Player player, MinecraftMap m) {
		m.getExtraData().remove("disabled");

		Maps.setMap(m);
		SurvivalGamesMap.reload(m);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName() +
				" §7wurde erfolgreich aktiviert!");
	}

	@Command(aliases = {"min", "minPlayers"}, desc = "Liest oder setzt die minimale Spieleranzahl")
	public void min(@Sender Player player, MinecraftMap m, @Optional @Range(min = 1, max = Integer.MAX_VALUE) Integer min) {
		if (min != null) {
			// set value
			JsonObject data = m.getExtraData();
			data.remove("min_players");
			data.addProperty("min_players", min);
			Maps.setMap(m);
			SurvivalGamesMap.reload(m);

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die minimale Spieleranzahl der Map §a" + m.getName()
					+ " §7wurde auf §a" + min + " §7gesetzt.");
		} else {
			// get value
			JsonObject data = m.getExtraData();

			if (!data.has("min_players")) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
						+ " §7hat noch keine minimale Spieleranzahl gesetzt.");
				return;
			}

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
					+ " §7hat eine minimale Spieleranzahl von §a" + data.get("min_players").getAsInt() + "§7.");
		}
	}

	@Command(aliases = {"max", "maxPlayers"}, desc = "Liest oder setzt die maximale Spieleranzahl")
	public void max(@Sender Player player, MinecraftMap m, @Optional @Range(min = 1, max = Integer.MAX_VALUE) Integer max) {
		if (max != null) {
			// set value
			JsonObject data = m.getExtraData();
			data.remove("max_players");
			data.addProperty("max_players", max);
			Maps.setMap(m);
			SurvivalGamesMap.reload(m);

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die maximale Spieleranzahl der Map §a" + m.getName()
					+ " §7wurde auf §a" + max + " §7gesetzt.");
		} else {
			// get value
			JsonObject data = m.getExtraData();

			if (!data.has("max_players")) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
						+ " §7hat noch keine maximale Spieleranzahl gesetzt.");
				return;
			}

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
					+ " §7hat eine maximale Spieleranzahl von §a" + data.get("max_players").getAsInt() + "§7.");
		}
	}

	@Command(aliases = {"time"}, desc = "Liest oder setzt die Spielzeit")
	public void time(@Sender Player player, MinecraftMap m, @Optional @Range(min = 1, max = Integer.MAX_VALUE) Integer time) {
		if (time != null) {
			// set value
			JsonObject data = m.getExtraData();
			data.remove("time");
			data.addProperty("time", time);
			Maps.setMap(m);
			SurvivalGamesMap.reload(m);

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Spielzeit der Map §a" + m.getName()
					+ " §7wurde auf §a" + time + " §7gesetzt.");
		} else {
			// get value
			JsonObject data = m.getExtraData();

			if (!data.has("time")) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
						+ " §7hat noch keine Spielzeit gesetzt.");
				return;
			}

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
					+ " §7hat eine Spielzeit von §a" + data.get("time").getAsInt() + "§7.");
		}
	}

	@Command(aliases = {"dmmap"}, desc = "Liest oder setzt die DM-Map")
	public void deatchmap(@Sender Player player, MinecraftMap m, @Optional String map) {
		if (map != null) {
			// set value
			JsonObject data = m.getExtraData();
			data.remove("dmmap");
			data.addProperty("dmmap", map);
			Maps.setMap(m);
			SurvivalGamesMap.reload(m);

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die DM-Map von §a" + m.getName()
					+ " §7wurde auf §a" + map + " §7gesetzt.");
		} else {
			// get value
			JsonObject data = m.getExtraData();

			if (!data.has("dmmap")) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
						+ " §7hat noch keine DM-Map gesetzt.");
				return;
			}

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
					+ " §7hat die DM-Map von §a" + data.get("dmmap").getAsString() + "§7.");
		}
	}

	@Command(aliases = "list", desc = "Listet alle Maps auf")
	public void list(@Sender Player player, @Switch('l') boolean local) {
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Momentan" + (local ? " lokal " : " ") + "definierte Karten:");
		for (MinecraftMap map : Maps.getMaps()) {
			if (local && !map.getWorldFolderName().equalsIgnoreCase(player.getWorld().getName())) {
				continue;
			}

			SurvivalGamesMap pbm = SurvivalGamesMap.wrap(map);

			StringBuilder line = new StringBuilder(GameLib.getCurrentGamePrefix());
			line.append("  §a");
			line.append(map.getName());

			if (!pbm.isFullyConfigured()) {
				line.append(" §c(Unkonfiguriert)");
			}

			player.sendMessage(line.toString());
		}
	}

	@Command(aliases = "status", desc = "Gibt den Status einer Map zurück")
	public void status(@Sender Player player, MinecraftMap m) {
		SurvivalGamesMap pbm = SurvivalGamesMap.wrap(m);
		String status = pbm.getConfigureError();
		status = status != null ? "§c" + status.replace("§f", "§c") : "§aVoll konfiguriert!";
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Status der Map §a" + m.getName() + " §7ist: " + status);
	}


}
