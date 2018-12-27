package net.lordofthecraft.arche.command.brigadier;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.command.CmdArg;

@RequiredArgsConstructor
public class ArcheArgumentType<T> implements ArgumentType<T> {
	private final CmdArg<T> arg;
	
	@Override
	public <S> T parse(StringReader reader) throws CommandSyntaxException {
		T parsed = arg.getMapper().apply(reader.readString());
		if(parsed == null) {
			Message error = new LiteralMessage(arg.getErrorMessage());
			CommandExceptionType type = new SimpleCommandExceptionType(error);
			throw new CommandSyntaxException(type, error);
		} else {
			return parsed;
		}
	}
	
	 @Override
	 public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		 for(String sugg : arg.getCompleter().get()) {
       if (sugg.startsWith(builder.getRemaining().toLowerCase())) {
      	 builder.suggest(sugg);
       }
		 }
		 return builder.buildFuture();
	 }

}
