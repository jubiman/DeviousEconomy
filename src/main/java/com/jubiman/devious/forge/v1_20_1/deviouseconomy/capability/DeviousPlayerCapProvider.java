package com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability provider for DeviousPlayer.
 * I still don't know what this does, but it's required for the capability to work. I think. I hope. I don't know.
 */
public class DeviousPlayerCapProvider implements ICapabilityProvider {
	/**
	 * Retrieves the Optional handler for the capability requested on the specific side.
	 * The return value <strong>CAN</strong> be the same for multiple faces.
	 * Modders are encouraged to cache this value, using the listener capabilities of the Optional to
	 * be notified if the requested capability get lost.
	 *
	 * @param cap  The capability to check
	 * @param side The Side to check from,
	 *             <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
	 * @return The requested an optional holding the requested capability.
	 */
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return DeviousCapManager.DEVIOUS_PLAYER_CAPABILITY.orEmpty(cap, LazyOptional.of(DeviousPlayer::new));
	}
}
