package com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.Config;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.enums.Rank;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.UUID;

@AutoRegisterCapability
public class DeviousPlayer {
	public Rank rank;
	public int coins;
	public long lastCoinDropTick;
	private UUID uuid;

	public DeviousPlayer(Rank rank, int coins, long lastCoinDropTick) {
		this.rank = rank;
		this.coins = coins;
		this.lastCoinDropTick = lastCoinDropTick;
	}

	public DeviousPlayer() {
		this(Rank.DEFAULT, Config.ranks.getStarterCoins(), -1);
	}

	public static void copy(DeviousPlayer from, DeviousPlayer to) {
		to.rank = from.rank;
		to.coins = from.coins;
		to.lastCoinDropTick = from.lastCoinDropTick;
		to.uuid = from.uuid;
	}

	/**
	 * Mark this capability as dirty.
	 */
	public void markDirty() {
		// Only mark dirty if a player is bound to the capability
		if (uuid != null) DeviousCapManager.markDirty(this);
		else DeviousEconomy.LOGGER.warn("Tried to mark unbound player as dirty");
	}

	/**
	 * Bind a player to this capability.
	 * @param player The player to bind to.
	 */
	void bindPlayer(Player player) {
		uuid = player.getUUID();
		rank = DeviousEconomy.getInstance().getDatabase().getRank(uuid);
		coins = DeviousEconomy.getInstance().getDatabase().getCoins(uuid);
		lastCoinDropTick = player.tickCount;
	}

	/**
	 * Get the UUID of the player this capability belongs to.
	 * @return The UUID of the player.
	 */
	public UUID getUUID() {
		return uuid;
	}
}
