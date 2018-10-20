package net.lordofthecraft.arche.interfaces;

public interface PersonaExtension {

    /**
     * @return numerical id of Persona this extension is for
     */
    default int getPersonaid() { return getPersona().getPersonaId(); }

    /**
     * @return Persona this extension is for
     */
    OfflinePersona getPersona();
}
