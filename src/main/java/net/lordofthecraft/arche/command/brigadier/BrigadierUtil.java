package net.lordofthecraft.arche.command.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public final class BrigadierUtil {

	private BrigadierUtil() { }
	
	
	public static LiteralArgumentBuilder<Object> literal(String literal) {
		return LiteralArgumentBuilder.literal(literal);
	}

}
