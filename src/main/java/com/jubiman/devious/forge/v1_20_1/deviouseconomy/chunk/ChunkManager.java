package com.jubiman.devious.forge.v1_20_1.deviouseconomy.chunk;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.Config;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.database.DatabaseConnection;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.enums.Rank;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.function.BiConsumer;

// TODO: make a tick thing?
public class ChunkManager {
	/**
	 * Test method to check the save data of the forced chunks.
	 */
	private static void test() {
		ServerLifecycleHooks.getCurrentServer().getAllLevels().forEach(level -> {
			ForcedChunksSavedData saveData = level.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
			CompoundTag tag = new CompoundTag();
			saveData.save(tag);
			DeviousEconomy.LOGGER.debug("Save data: {}", tag);
			// Remove coins from owner
			saveData.getEntityForcedChunks().getChunks().forEach(ChunkManager::removeCoinsFromOwner);
			saveData.getEntityForcedChunks().getTickingChunks().forEach(ChunkManager::removeCoinsFromOwner);
		});
	}

	/**
	 * Register the command to force and test chunks.
	 * @param dispatcher The command dispatcher to register the command to.
	 */
	public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("devious")
			.then(Commands.literal("debug")
			.requires(source -> source.hasPermission(4))
			.then(Commands.literal("chunk")
			.then(Commands.literal("test")
				.executes(context -> {
					ChunkManager.test();
					return 1;
				}))
			.then(Commands.literal("force")
			.then(Commands.argument("x", IntegerArgumentType.integer())
			.then(Commands.argument("z", IntegerArgumentType.integer())
			.then(Commands.argument("add", BoolArgumentType.bool())
				.executes(context -> {
					int x = IntegerArgumentType.getInteger(context, "x");
					int z = IntegerArgumentType.getInteger(context, "z");
					boolean add = BoolArgumentType.getBool(context, "add");
					UUID owner = context.getSource().getPlayerOrException().getUUID();
					ServerLevel level = context.getSource().getLevel();
					ChunkManager.force(level, owner, x, z, add);
					return 1;
				})))))
			))
		);
	}

	/**
	 * Remove coins from the owner of a ticket.
	 * @param owner The owner of the ticket.
	 * @param chunkPos The chunk position.
	 */
	private static void removeCoinsFromOwner(ForgeChunkManager.TicketOwner<UUID> owner, LongSet chunkPos) {
		// Use java reflection to get the owner's UUID
		Field ownerField;
		try {
			ownerField = owner.getClass().getDeclaredField("owner");
			ownerField.setAccessible(true);
			UUID ownerUUID = (UUID) ownerField.get(owner);
			DatabaseConnection database = DeviousEconomy.getInstance().getDatabase();
			Rank rank = database.getRank(ownerUUID);
			try {
				database.purchase(ownerUUID, Config.ranks.getChunkCost(rank));
			} catch (IllegalArgumentException e) {
				// Notify user they cannot afford chunk
				ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(ownerUUID);
				if (player == null) {
					// TODO: somehow notify the player later?
				} else player.displayClientMessage(Component.literal("You have insufficient funds to load a chunk. It has been unloaded!"), true);
				// Unload the chunk
				ChunkManager.unloadAllChunks(owner, ownerUUID);
			}
			DeviousEconomy.LOGGER.debug("Owner: {}", ownerUUID);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			DeviousEconomy.LOGGER.error("Failed to get owner of ticket", e);
		}
	}

	/**
	 * Unload all chunks owned by a player.
	 * @param owner The owner of the chunks.
	 * @param ownerUUID The UUID of the owner.
	 */
	private static void unloadAllChunks(ForgeChunkManager.TicketOwner<UUID> owner, UUID ownerUUID) {
		// Unload all chunks
		ServerLifecycleHooks.getCurrentServer().getAllLevels().forEach(level -> {
			ForcedChunksSavedData saveData = level.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
			CompoundTag tag = new CompoundTag();
			saveData.save(tag);
			DeviousEconomy.LOGGER.debug("Save data: {}", tag);
			// Remove coins from owner
			BiConsumer<ForgeChunkManager.TicketOwner<UUID>, LongSet> unloadChunk = (o, longSet) -> {
				if (o.equals(owner)) {
					longSet.forEach(chunkPos -> {
						ChunkPos pos = new ChunkPos(chunkPos);
						DeviousEconomy.LOGGER.info("Unloading chunk at {} due to insufficient funds of owner {}", pos, ownerUUID);
						ChunkManager.force(level, ownerUUID, pos.x, pos.z, false);
					});
				}
			};
			saveData.getEntityForcedChunks().getChunks().forEach(unloadChunk);
			saveData.getEntityForcedChunks().getTickingChunks().forEach(unloadChunk);
		});
	}

	/**
	 * Force a chunk to load or unload.
	 * @param level The level to force the chunk in.
	 * @param owner The owner of the chunk.
	 * @param chunkX The X coordinate of the chunk.
	 * @param chunkZ The Z coordinate of the chunk.
	 * @param add Whether to add or remove the chunk.
	 */
	private static void force(ServerLevel level, UUID owner, int chunkX, int chunkZ, boolean add) {
		ForgeChunkManager.forceChunk(level, DeviousEconomy.MODID, owner, chunkX, chunkZ, add, true);
	}
}
