package vexatious.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableInt;
import vexatious.ActiveEvokerRegistry;
import vexatious.Vexatious;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VexatiousCommand implements CommandRegistrationCallback {
    CommandNode<ServerCommandSource> MAIN = literal(Vexatious.MOD_ID)
            .requires(source -> source.hasPermissionLevel(2))
            .build();
    CommandNode<ServerCommandSource> LIST = literal("list")
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                MutableInt count = new MutableInt(0);
                ActiveEvokerRegistry.get(
                                source.getWorld()
                        )
                        .forEachRef(
                                ref -> {
                                    source.sendFeedback(
                                            () ->
                                                    Text.empty()
                                                            .append(
                                                                    Text.translatable(
                                                                                    "vexatious.message.evoker_prefix"
                                                                            )
                                                                            .append(
                                                                                    Text.literal(
                                                                                                    ref.uuid()
                                                                                                            .toString()
                                                                                                            .substring(0, 10)
                                                                                            )
                                                                                            .styled(
                                                                                                    style -> style
                                                                                                            .withClickEvent(
                                                                                                                    new ClickEvent(
                                                                                                                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                                                                                            ref.uuid().toString()
                                                                                                                    )
                                                                                                            )
                                                                                                            .withHoverEvent(
                                                                                                                    new HoverEvent(
                                                                                                                            HoverEvent.Action.SHOW_TEXT,
                                                                                                                            Text.literal(
                                                                                                                                    ref.uuid().toString()
                                                                                                                            )
                                                                                                                    )
                                                                                                            )
                                                                                            )
                                                                            )
                                                                            .append(" ")
                                                                            .append(
                                                                                    Text.empty()
                                                                                            .append(
                                                                                                    Text.literal("(")
                                                                                                            .formatted(
                                                                                                                    Formatting.DARK_RED
                                                                                                            )
                                                                                            )
                                                                                            .append(
                                                                                                    Text.literal("kill")
                                                                                                            .styled(
                                                                                                                    style -> style.withClickEvent(
                                                                                                                                    new ClickEvent(
                                                                                                                                            ClickEvent.Action.SUGGEST_COMMAND,
                                                                                                                                            "/kill " + ref.uuid()
                                                                                                                                    )
                                                                                                                            )
                                                                                                                            .withColor(
                                                                                                                                    Formatting.RED
                                                                                                                            )
                                                                                                            )
                                                                                            )
                                                                                            .append(
                                                                                                    Text.literal(")")
                                                                                                            .formatted(
                                                                                                                    Formatting.DARK_RED
                                                                                                            )
                                                                                            )
                                                                            )
                                                                            .append(" ")
                                                                            .append(
                                                                                    Text.empty()
                                                                                            .append(
                                                                                                    Text.literal("(")
                                                                                                            .formatted(
                                                                                                                    Formatting.GOLD
                                                                                                            )
                                                                                            )
                                                                                            .append(
                                                                                                    Text.literal("teleport")
                                                                                                            .styled(
                                                                                                                    style -> style.withClickEvent(
                                                                                                                                    new ClickEvent(
                                                                                                                                            ClickEvent.Action.SUGGEST_COMMAND,
                                                                                                                                            "/tp @s " + ref.uuid()
                                                                                                                                    )
                                                                                                                            ).withHoverEvent(
                                                                                                                                    new HoverEvent(
                                                                                                                                            HoverEvent.Action.SHOW_TEXT,
                                                                                                                                            Text.literal(
                                                                                                                                                    ref.getEntity(source.getWorld())
                                                                                                                                                            .map(
                                                                                                                                                                    entity -> entity
                                                                                                                                                                            .getBlockPos()
                                                                                                                                                                            .toShortString()
                                                                                                                                                            )
                                                                                                                                                            .orElse(ref.uuid().toString())
                                                                                                                                            )
                                                                                                                                    )
                                                                                                                            )
                                                                                                                            .withColor(
                                                                                                                                    Formatting.DARK_AQUA
                                                                                                                            )
                                                                                                            )
                                                                                            )
                                                                                            .append(
                                                                                                    Text.literal(")")
                                                                                                            .formatted(
                                                                                                                    Formatting.GOLD
                                                                                                            )
                                                                                            )
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
                                    "vexatious.message.no_evokers"
                            ),
                            false
                    );
                }
                return 1;
            })
            .build();
    CommandNode<ServerCommandSource> REMOVE = literal("remove").build();
    CommandNode<ServerCommandSource> REMOVE_UUID_ARG = argument(
            "uuid",
            UuidArgumentType.uuid()
    )
            .suggests(
                    new EvokerUUIDSuggestionProvider("uuid")
            )
            .executes(
                    this::remove
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
