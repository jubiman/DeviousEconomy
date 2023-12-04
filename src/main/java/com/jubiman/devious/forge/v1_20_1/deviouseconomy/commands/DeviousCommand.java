package com.jubiman.devious.forge.v1_20_1.deviouseconomy.commands;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.Config;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.enums.Transaction;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.command.EnumArgument;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DeviousCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, DeviousEconomy deviousDiscord) {
		DeviousEconomy.LOGGER.info("Registering commands");
		// Database commands
		{
			dispatcher.register(Commands.literal("devious")
				.then(Commands.literal("admin")
				.requires(source -> source.hasPermission(2)).then(Commands.literal("database")
				.then(Commands.literal("coins")
				.then(Commands.argument("player", EntityArgument.player())
				.then(Commands.argument("transaction", EnumArgument.enumArgument(Transaction.class))
				.then(Commands.argument("amount", IntegerArgumentType.integer())
				.executes(context -> {
					Player player = EntityArgument.getPlayer(context, "player");
					UUID uuid = player.getUUID();
					int amount = IntegerArgumentType.getInteger(context, "amount");
					switch (context.getArgument("transaction", Transaction.class)) {
						case ADD, BUY, PURCHASE -> {
							try {
								deviousDiscord.getDatabase().purchase(uuid, amount);
							} catch (IllegalArgumentException e) {
								context.getSource().sendFailure(Component.literal(e.getMessage()));
								return -1;
							}
							context.getSource().sendSuccess(() -> Component.literal("Removed " + amount + " coins from player " + player.getName().getString()), false);
						}
						case REMOVE, SELL -> {
							deviousDiscord.getDatabase().sell(uuid, amount);
							context.getSource().sendSuccess(() -> Component.literal("Added " + amount + " coins to player " + player.getName().getString()), false);
						}
						case SET -> {
							deviousDiscord.getDatabase().setCoins(uuid, amount);
							context.getSource().sendSuccess(() -> Component.literal("Set player " + player.getName().getString() + "'s coins to " + amount), false);
						}
						default -> {
							context.getSource().sendFailure(Component.literal("Invalid transaction type!"));
							return -1;
						}
					}
					return 1;
				}))).then(Commands.literal("GET")
				.executes(context -> {
					DeviousEconomy.LOGGER.debug(context.getInput());
					Player player = EntityArgument.getPlayer(context, "player");
					UUID uuid = player.getUUID();
					try {
						int coins = deviousDiscord.getDatabase().getCoins(uuid);
						context.getSource().sendSuccess(() -> Component.literal("Player " + player.getName().getString() + " has " + coins + " coins"), false);
					} catch (IllegalArgumentException e) {
						context.getSource().sendFailure(Component.literal(e.getMessage()));
						return -1;
					}
					return 1;
				})
			))))));
		}
		// Register command to reconnect to the websocket, only works if the user has the permission level of OP
		{
			dispatcher.register(Commands.literal("devious")
				.then(Commands.literal("admin")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("reconnect")
				.then(Commands.literal("database")
				.executes(context -> {
					try {
						deviousDiscord.getDatabase().reconnect();
						context.getSource().sendSuccess(() -> Component.literal(Config.getIdentifier() + " reconnected to DB"), false);
					} catch (Exception e) {
						DeviousEconomy.LOGGER.warn("Failed to reconnect to DB", e);
						context.getSource().sendFailure(Component.literal("Failed to reconnect to DB! (See server logs for more info)"));
						return -1;
					}
					return 1;
				}))
				.then(Commands.literal("db")
				.redirect(dispatcher.getRoot().getChild("devious").getChild("admin").getChild("database")))
				.then(Commands.literal("all")
				.executes(context -> {
					// Try to run the socket reconnect command if it exists
					try {
						// Create temporary stack, so we can run the command and check if it executed successfully
						CommandSourceStack stack = new CommandSourceStack(new CommandSource() {
							@Override
							public void sendSystemMessage(@NotNull Component p_230797_) {
							}

							@Override
							public boolean acceptsSuccess() {
								return true;
							}

							@Override
							public boolean acceptsFailure() {
								return true;
							}

							@Override
							public boolean shouldInformAdmins() {
								return false;
							}
						},
								context.getSource().getPosition(),
								context.getSource().getRotation(),
								context.getSource().getLevel(),
								4,
								context.getSource().getDisplayName().getString(),
								context.getSource().getDisplayName(),
								context.getSource().getServer(),
								context.getSource().getEntity()
						);
						if (dispatcher.execute("devious admin reconnect socket", stack) == -1) {
							context.getSource().sendFailure(Component.literal("Failed to reconnect to Devious Socket! (See server logs for more info)"));
						} else {
							context.getSource().sendSuccess(() -> Component.literal(Config.getIdentifier() + " reconnected to Devious Socket"), false);
						}
					} catch (Exception e) {
						DeviousEconomy.LOGGER.warn("Failed to reconnect to Devious Socket", e);
						context.getSource().sendFailure(Component.literal("Failed to reconnect to Devious Socket! (See server logs for more info)"));
						return -1;
					}
					// Reconnect to the database
					try {
						DeviousEconomy.LOGGER.info("Reconnecting to Devious Database");
						deviousDiscord.getDatabase().reconnect();
						context.getSource().sendSuccess(() -> Component.literal(Config.getIdentifier() + " reconnected to Devious Socket and DB"), false);
					} catch (Exception e) {
						DeviousEconomy.LOGGER.warn("Failed to reconnect to Devious Database", e);
						context.getSource().sendFailure(Component.literal("Failed to reconnect to Devious Database! (See server logs for more info)"));
						return -1;
					}
					return 1;
				}))
			)));
		}
	}


}
