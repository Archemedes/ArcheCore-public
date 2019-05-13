package net.lordofthecraft.arche.interfaces;

public interface PersonaTags extends Tags<OfflinePersona>, PersonaExtension {
	
	@Override
	default OfflinePersona getHolder() { return getPersona(); }
}
