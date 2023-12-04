package com.jubiman.devious.forge.v1_20_1.deviouseconomy.commands;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class BalCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, DeviousEconomy deviousEconomy) {
		// Main command
		CommandNode<CommandSourceStack> main;
		{
			main = dispatcher.register(Commands.literal("bal")
					.requires(cs -> cs.hasPermission(0))
					.executes(ctx -> {
						int coins = deviousEconomy.getDatabase().getCoins(ctx.getSource().getPlayerOrException().getUUID());
						ctx.getSource().sendSuccess(() -> Component.literal("You have " + coins + " coins"), false);
						return 1;
					}).then(Commands.argument("player", EntityArgument.player())
					.requires(cs -> cs.hasPermission(2)) // TODO: which permission level to view other people's balance?
					.executes(ctx -> {
						try {
							Player player = EntityArgument.getPlayer(ctx, "player");
							int coins = deviousEconomy.getDatabase().getCoins(player.getUUID());
							ctx.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " has " + coins + " coins"), false);
						} catch (CommandSyntaxException e) {
							ctx.getSource().sendFailure(Component.literal("Invalid player!"));
							return -1;
						}
						return 1;
					})
			));
		}
		// Aliases
		{
			dispatcher.register(Commands.literal("balance")
					.redirect(main)
			);
			dispatcher.register(Commands.literal("coins")
					.redirect(main)
			);
			dispatcher.register(Commands.literal("money")
					.redirect(main)
			);
		}
	}
}
