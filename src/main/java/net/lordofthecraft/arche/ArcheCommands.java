package net.lordofthecraft.arche;

import co.lotc.core.bukkit.command.Commands;
import co.lotc.core.bukkit.command.SenderTypes;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.CommandUtil;

public final class ArcheCommands {

	private ArcheCommands() { }
	
	public static void registerPersonaType() {
		Commands.defineArgumentType(Persona.class)
			.mapper(CommandUtil::personaFromArg)
			.senderMapper(SenderTypes.UNWRAP_PLAYER.andThen(ArcheCore::getPersona))
			.completer(SenderTypes.PLAYER_COMPLETER)
			.register();
	}
}
