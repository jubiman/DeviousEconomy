package com.jubiman.devious.forge.v1_20_1.deviouseconomy;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousCapManager;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.chunk.ChunkManager;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.commands.*;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.database.DatabaseConnection;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(DeviousEconomy.MODID)
public class DeviousEconomy {
	public static final String MODID = "deviouseconomy";
	public static final Logger LOGGER = LogUtils.getLogger();
	private DatabaseConnection database;
	private static DeviousEconomy instance;

	public DeviousEconomy() {
		LOGGER.info("DeviousEconomy is loading");

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		// Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);

		instance = this;
	}

	/**
	 * Returns the database connection
	 * @return The database connection
	 */
	public DatabaseConnection getDatabase() {
		return database;
	}

	/**
	 * Returns the mod instance
	 * @return The mod instance
	 */
	public static DeviousEconomy getInstance() {
		return instance;
	}

	@SubscribeEvent
	public void onCommandsRegister(RegisterCommandsEvent event) {
		DeviousCommand.register(event.getDispatcher(), this);
		BalCommand.register(event.getDispatcher(), this);
		DebugCommands.register(event.getDispatcher());
		ChunkManager.registerCommand(event.getDispatcher());
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		LOGGER.info("Trying to connect to DB");
		database = new DatabaseConnection();
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		LOGGER.info("Closing DB connection");
		database.close();
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		database.addPlayer(event.getEntity().getUUID());

		DeviousCapManager.onPlayerJoin(event);
	}
}
