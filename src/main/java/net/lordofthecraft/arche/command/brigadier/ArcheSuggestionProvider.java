package net.lordofthecraft.arche.command.brigadier;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.command.CmdArg;

@RequiredArgsConstructor
public class ArcheSuggestionProvider<T> implements SuggestionProvider<T> {
	private final CmdArg<T> arg;

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> unused, SuggestionsBuilder builder) throws CommandSyntaxException {
		 for(String sugg : arg.getCompleter().get()) {
			 if (sugg.startsWith(builder.getRemaining().toLowerCase())) {
				 builder.suggest(sugg);
			 }
		 }
		 return builder.buildFuture();
	}

}
