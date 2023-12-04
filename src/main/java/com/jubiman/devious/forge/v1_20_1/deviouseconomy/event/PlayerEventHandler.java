package com.jubiman.devious.forge.v1_20_1.deviouseconomy.event;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.Config;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousCapManager;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousPlayer;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class PlayerEventHandler {
	@SubscribeEvent
	public static void onPlayerEvent(PlayerEvent event) {
		// Ignore events that don't classify as "active"
		switch (event.getClass().getSimpleName()) {
			case "PlayerLoggedInEvent":
			case "PlayerLoggedOutEvent":
			case "PlayerRespawnEvent":
			case "PlayerChangedDimensionEvent":
			case "PlayerSpawnPhantomsEvent":
			case "PlayerChangeGameModeEvent":
			case "StartTracking":
			case "StopTracking":
			case "LoadFromFile":
			case "SaveToFile":
			case "NameFormat":
			case "TabListNameFormat":
			case "Clone":
				return;
		}
		DeviousPlayer player = DeviousCapManager.asDeviousPlayer(event.getEntity());

//		DeviousEconomy.LOGGER.debug("Player {} has is active", event.getEntity().getName());
//		DeviousEconomy.LOGGER.debug("Last coin drop tick: {}", player.lastCoinDropTick);
//		DeviousEconomy.LOGGER.debug("Current tick: {}", event.getEntity().tickCount);
//		DeviousEconomy.LOGGER.debug("Coin drop interval: {}", Config.ranks.getCoinDropInterval(player.rank) * 20);

		// Check last tick and current tick
		// Get server tick count
		if (ServerLifecycleHooks.getCurrentServer().getTickCount() - player.lastCoinDropTick > Config.ranks.getCoinDropInterval(player.rank) * 20) {
			DeviousEconomy.LOGGER.debug("Player {} has received a coin drop", event.getEntity().getName().getString());
			// Update last tick
			player.lastCoinDropTick = ServerLifecycleHooks.getCurrentServer().getTickCount();
			// Add coins
			int amount = Config.ranks.getCoinDropAmount(player.rank);
			player.coins += amount;
			player.markDirty();
			// Send message
			String message = String.format("§bYou have received %d coins!", amount);
			event.getEntity().displayClientMessage(Component.literal(message), true);
		}
	}
}
