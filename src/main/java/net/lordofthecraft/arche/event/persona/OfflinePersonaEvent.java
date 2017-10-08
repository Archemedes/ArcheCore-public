package net.lordofthecraft.arche.event.persona;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import org.bukkit.event.Event;

public abstract class OfflinePersonaEvent extends Event {
    private OfflinePersona persona;

    public OfflinePersonaEvent(OfflinePersona persona, boolean async) {
        super(async);
        this.persona = persona;
    }

    public OfflinePersonaEvent(OfflinePersona persona) {
        this.persona = persona;
    }

    public OfflinePersona getPersona() {
        return persona;
    }


}
