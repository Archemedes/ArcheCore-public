package net.lordofthecraft.arche;

import java.util.UUID;

import co.lotc.core.bukkit.command.Commands;
import co.lotc.core.bukkit.command.SenderTypes;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
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
	
	public static void registerOfflinePersonaType() {
		Commands.defineArgumentType(OfflinePersona.class)
		.mapper(CommandUtil::offlinePersonaFromArg)
		.senderMapper(SenderTypes.UNWRAP_PLAYER.andThen(ArcheCore::getPersona))
		.completer(SenderTypes.PLAYER_COMPLETER)
		.register();
	}
	
	public static void registerUUIDType() {
		Commands.defineArgumentType(UUID.class)
		.mapper(s->{
			UUID u = SenderTypes.uuidFromString(s);
			if(u != null) return u;
			return ArcheCore.getControls().getPlayerUUIDFromAlias(s);
		})
		.completer(SenderTypes.PLAYER_COMPLETER)
		.register();
	}
	
}
