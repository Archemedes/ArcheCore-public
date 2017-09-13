package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

public interface PersonaKey {

	/**
	 * Retrieve a mojang-issued UUID belonging to a player account
	 * @return The UUID belonging to the player
	 */
	UUID getPlayerUUID();
	
	/**
	 * Retrieve an ArcheCore-issued (session)-consistent id to identify a Persona belonging to a certain player
     * @return The immutable integer id (0-15) corresponding to a player's persona.
     */
    int getPersonaSlot();

    /**
     * Retrieve the unique, immutable integer ID which represents this persona and is used to reference it in SQL.
     *
     * @return The immutable integer ID which represents this specific persona
     */
    int getPersonaID();

    /**
	 * Attempts to retrieve a Persona uniquely corresponding to this key. Persona might not exist or might not be loaded. 
	 * @return A Persona with this key
	 */
	Persona getPersona();
}
