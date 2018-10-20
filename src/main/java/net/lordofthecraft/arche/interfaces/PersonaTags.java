package net.lordofthecraft.arche.interfaces;

public interface PersonaTags extends Tags<OfflinePersona>, PersonaExtension {
	String REFRESH_MC_SPECIFICS = "refreshMCSpecifics";
	
	@Override
	default OfflinePersona getHolder() {
		return getPersona();
	}
}
