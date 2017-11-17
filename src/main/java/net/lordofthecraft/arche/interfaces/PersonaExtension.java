package net.lordofthecraft.arche.interfaces;

public interface PersonaExtension {

	/**
	 * @return numerical id of Persona this extension is for
	 */
    public int getPersonaid();

    /**
     * @return Persona this extension is for
     */
    public OfflinePersona getPersona();
}
