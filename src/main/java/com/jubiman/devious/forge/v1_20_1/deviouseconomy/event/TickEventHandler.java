package com.jubiman.devious.forge.v1_20_1.deviouseconomy.event;

import com.jubiman.devious.forge.v1_20_1.deviousdiscord.database.DatabaseConnection;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickEventHandler {
	private static byte counter = 0;

	@SubscribeEvent
	public static void onTickEvent(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;
		if (!event.haveTime()) return;

		if (++counter == 20) {
			counter = 0;
			DatabaseConnection.cleanDirtyPlayers();
		}

	}
}
