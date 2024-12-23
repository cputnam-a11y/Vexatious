package vexatious.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import vexatious.ActiveEvokerRegistry;
import vexatious.Vexatious;

import java.util.concurrent.CompletableFuture;

public class EvokerUUIDSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    public EvokerUUIDSuggestionProvider(String argumentName) {
    }
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ActiveEvokerRegistry registry = ActiveEvokerRegistry.get(context.getSource().getWorld());
        registry.forEachRef(ref -> {
            if (CommandSource.shouldSuggest(builder.getRemaining(), ref.uuid().toString()) || builder.getRemaining().trim().isEmpty()) {
                builder.suggest(ref.uuid().toString());
            }
        });
        return builder.buildFuture();
    }
}
