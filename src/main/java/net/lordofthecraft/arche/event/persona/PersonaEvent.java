package net.lordofthecraft.arche.event.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.event.player.PlayerEvent;

/**
 * Represents an event related to a Persona, the Roleplay Character object of a Player
 */
public abstract class PersonaEvent extends PlayerEvent{
    protected final Persona persona;

	public PersonaEvent(Persona persona) {
		super(persona.getPlayer());
		this.persona = persona;
	}

    /**
     * The Persona object relevant to this event.
	 * @return The Persona object
	 */
    public Persona getPersona() {
        return persona;
	}

}
