package com.jubiman.devious.forge.v1_20_1.deviouseconomy.commands;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.Config;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousCapManager;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousPlayer;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.chunk.ChunkManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class DebugCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		// Commands
		{
			dispatcher.register(Commands.literal("devious")
				.then(Commands.literal("debug")
				.requires(source -> source.hasPermission(4))
				.then(Commands.literal("coindrop")
					.executes(context -> {
						if (context.getSource().getPlayer() == null) {
							context.getSource().sendFailure(Component.literal("You must be a player to use this command!"));
							return -1;
						}
						DeviousPlayer player = DeviousCapManager.asDeviousPlayer(context.getSource().getPlayer());
						// Check last tick and current tick
						// Update last tick
						player.lastCoinDropTick = context.getSource().getPlayer().tickCount;
						// Add coins
						int amount = Config.ranks.getCoinDropAmount(player.rank);
						player.coins += amount;
						// Send message
						String message = String.format("§bYou have received %d coins!", amount);
						context.getSource().getPlayer().displayClientMessage(Component.literal(message), true);

						context.getSource().sendSuccess(() -> Component.literal("Sent coin drop event"), false);
						return 1;
					}))
				.then(Commands.literal("d_execute_fail")
					.executes(context -> {
						CommandSourceStack stack = CommandUtils.getTemporaryCommandSourceStack(context);
						try {
							DeviousEconomy.LOGGER.debug("Return value of unknown cmd: " + dispatcher.execute("devious admin reconnect socket", stack));
						} catch (CommandSyntaxException e) {
							DeviousEconomy.LOGGER.debug("Failed to execute command", e);
						}
						context.getSource().sendSuccess(() -> Component.literal("Executed test"), false);
						return 1;
					})
				))
			);
		}
	}
}
