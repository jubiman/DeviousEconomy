package com.jubiman.devious.forge.v1_20_1.deviouseconomy.commands;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.Config;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousCapManager;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DebugCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, DeviousEconomy deviousEconomy) {
		LiteralArgumentBuilder<CommandSourceStack> debug = Commands.literal("devious")
				.then(Commands.literal("debug")
				.requires(source -> source.hasPermission(2))
		);
		// Commands
		{
			dispatcher.register(debug
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
				})
			));
		}
	}
}
