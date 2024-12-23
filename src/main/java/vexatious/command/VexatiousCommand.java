package vexatious.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import vexatious.ActiveEvokerRegistry;
import vexatious.Vexatious;

public class VexatiousCommand implements CommandRegistrationCallback {
    CommandNode<ServerCommandSource> MAIN = CommandManager.literal(Vexatious.MOD_ID).build();
    CommandNode<ServerCommandSource> LIST = CommandManager.literal("list").executes(context -> {
        ServerCommandSource source = context.getSource();
        MutableInt count = new MutableInt(0);
        ActiveEvokerRegistry.get(
                        source.getWorld()
                )
                .forEachRef(
                        ref -> {
                            source.sendFeedback(
                                    () -> Text.translatable(
                                                    "vexatious.evoker_prefix"
                                            )
                                            .append(
                                                    Text.of(
                                                            ref.uuid()
                                                    )
                                            ),
                                    false
                            );
                            count.increment();
                        }
                );
        if (count.intValue() <= 0) {
            source.sendFeedback(
                    () -> Text.translatable(
                            "vexatious.no_evokers"
                    ),
                    false
            );
        }
        return 1;
    }).build();
    CommandNode<ServerCommandSource> REMOVE = CommandManager.literal("remove").build();
    CommandNode<ServerCommandSource> REMOVE_UUID_ARG = CommandManager.argument(
                    "uuid",
                    UuidArgumentType.uuid()
            )
            .executes(
                    this::remove
            )
            .suggests(
                    new EvokerUUIDSuggestionProvider("uuid")
            )
            .build();
    {
        REMOVE.addChild(REMOVE_UUID_ARG);
        MAIN.addChild(LIST);
        MAIN.addChild(REMOVE);
    }

    @Override
    public void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.getRoot().addChild(MAIN);
    }

    private int remove(CommandContext<ServerCommandSource> context) {
        ActiveEvokerRegistry registry = ActiveEvokerRegistry.get(context.getSource().getWorld());
        registry.removeEvoker(UuidArgumentType.getUuid(context, "uuid"));
        return 1;
    }

}
