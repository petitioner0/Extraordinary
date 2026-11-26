package com.petitoner0.extraordinary.finalritual.chokmah;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class ChokmahCommand {
	private ChokmahCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("chokmah")
					.then(Commands.literal("test").executes(context -> {
						var source = context.getSource();
						if (!new Chokmah().start(source.getPlayerOrException())) {
							source.sendFailure(Component.literal("This client does not support the Chokmah camera packets."));
							return 0;
						}

						source.sendSuccess(() -> Component.literal("Chokmah camera test started."), false);
						return Command.SINGLE_SUCCESS;
					})));
		});
	}
}
