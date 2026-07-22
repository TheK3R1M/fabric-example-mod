package com.example.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.example.config.ModConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CustomBeaconCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("custombeacon")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("reload")
                    .executes(context -> {
                        ModConfig.load();
                        context.getSource().sendSuccess(() -> Component.literal("CustomBeacon configuration reloaded from disk."), true);
                        return 1;
                    })
                )
                .then(Commands.literal("get")
                    .then(Commands.literal("maxTier")
                        .executes(context -> {
                            int tier = ModConfig.get().maxTier;
                            context.getSource().sendSuccess(() -> Component.literal("maxTier: " + tier), false);
                            return tier;
                        })
                    )
                    .then(Commands.literal("enableActiveDrain")
                        .executes(context -> {
                            boolean drain = ModConfig.get().enableActiveDrain;
                            context.getSource().sendSuccess(() -> Component.literal("enableActiveDrain: " + drain), false);
                            return drain ? 1 : 0;
                        })
                    )
                )
                .then(Commands.literal("set")
                    .then(Commands.literal("maxTier")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 256))
                            .executes(context -> {
                                int val = IntegerArgumentType.getInteger(context, "value");
                                ModConfig.get().maxTier = val;
                                ModConfig.save();
                                context.getSource().sendSuccess(() -> Component.literal("Updated maxTier to: " + val), true);
                                return 1;
                            })
                        )
                    )
                    .then(Commands.literal("enableActiveDrain")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(context -> {
                                boolean val = BoolArgumentType.getBool(context, "value");
                                ModConfig.get().enableActiveDrain = val;
                                ModConfig.save();
                                context.getSource().sendSuccess(() -> Component.literal("Updated enableActiveDrain to: " + val), true);
                                return 1;
                            })
                        )
                    )
                )
        );
    }
}
