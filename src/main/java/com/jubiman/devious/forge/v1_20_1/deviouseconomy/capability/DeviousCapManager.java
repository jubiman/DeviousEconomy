package com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashSet;

@Mod.EventBusSubscriber
public class DeviousCapManager {
	public static final ResourceLocation DEVIOUS_PLAYER_CAP = new ResourceLocation("devious", "devious_player_cap");

	public static final Capability<DeviousPlayer> DEVIOUS_PLAYER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
	private static final HashSet<DeviousPlayer> dirtyPlayers = new HashSet<>();

	/**
	 * Returns the DeviousPlayer capability of a player
	 * @param player The player to get the capability from
	 * @return The DeviousPlayer capability
	 */
	public static DeviousPlayer asDeviousPlayer(Player player) {
		// TODO: maybe make orEmpty work?
		return player.getCapability(DEVIOUS_PLAYER_CAPABILITY, null).orElseThrow(() -> new RuntimeException("Could not get DeviousPlayer capability from player"));
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(DEVIOUS_PLAYER_CAP, new DeviousPlayerCapProvider());
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(PlayerEvent.Clone event) {
		DeviousPlayer.copy(asDeviousPlayer(event.getOriginal()), asDeviousPlayer(event.getEntity()));
	}

	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		asDeviousPlayer(event.getEntity()).bindPlayer(event.getEntity());
	}

	/**
	 * Marks a player as dirty, so that their data will be saved to disk
	 * @param deviousPlayer The player to mark as dirty
	 */
	public static void markDirty(DeviousPlayer deviousPlayer) {
		dirtyPlayers.add(deviousPlayer);
	}

	/**
	 * Returns a collection of all dirty players
	 * @return A collection of all dirty players
	 */
	public static Collection<DeviousPlayer> getDirtyPlayers() {
		return dirtyPlayers;
	}
}
