package com.jubiman.devious.forge.v1_20_1.deviouseconomy.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CommandUtils {

	public static CommandSourceStack getTemporaryCommandSourceStack(CommandContext<CommandSourceStack> context) {
		return new CommandSourceStack(new CommandSource() {
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
	}
}
