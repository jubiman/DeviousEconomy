package com.jubiman.devious.forge.v1_20_1.deviouseconomy.event;

import com.jubiman.devious.forge.v1_20_1.deviousdiscord.Config;
import com.jubiman.devious.forge.v1_20_1.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.forge.v1_20_1.deviousdiscord.capability.DeviousCapManager;
import com.jubiman.devious.forge.v1_20_1.deviousdiscord.capability.DeviousPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class PlayerEventHandler {
	@SubscribeEvent
	public static void onPlayerEvent(PlayerEvent event) {
		if (event instanceof PlayerEvent.PlayerLoggedInEvent
				|| event instanceof PlayerEvent.PlayerLoggedOutEvent
				||event instanceof PlayerEvent.StartTracking
				|| event instanceof PlayerEvent.StopTracking) {
			return;
		}
		DeviousDiscord.LOGGER.debug("Event type: {}", event.getClass().getSimpleName());
		DeviousPlayer player = DeviousCapManager.asDeviousPlayer(event.getEntity());

//		DeviousDiscord.LOGGER.debug("Player {} has is active", event.getEntity().getName());
//		DeviousDiscord.LOGGER.debug("Last coin drop tick: {}", player.lastCoinDropTick);
//		DeviousDiscord.LOGGER.debug("Current tick: {}", event.getEntity().tickCount);
//		DeviousDiscord.LOGGER.debug("Coin drop interval: {}", Config.ranks.getCoinDropInterval(player.rank) * 20);

		// Check last tick and current tick
		// Get server tick count
		if (ServerLifecycleHooks.getCurrentServer().getTickCount() - player.lastCoinDropTick > Config.ranks.getCoinDropInterval(player.rank) * 20) {
			DeviousDiscord.LOGGER.debug("Player {} has received a coin drop", event.getEntity().getName().getString());
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
