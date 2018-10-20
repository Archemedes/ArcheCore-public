package net.lordofthecraft.arche.persona;

import lombok.Getter;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.account.AbstractTags;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.PersonaTags;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaTagRow;
import net.lordofthecraft.arche.save.rows.persona.PersonaTagRow;

public class ArchePersonaTags extends AbstractTags<OfflinePersona> implements PersonaTags {
    private static final IConsumer consumer = ArcheCore.getConsumerControls();
    @Getter private final OfflinePersona persona;

    public ArchePersonaTags(OfflinePersona persona) {
    	this.persona = persona;
    }

    @Override
    protected void commitTag(TagAttachment tag) {
    	consumer.queueRow(new PersonaTagRow(persona, tag));
    }

    @Override
    protected void deleteTag(String tagKey) {
    	consumer.queueRow(new DeletePersonaTagRow(persona, tagKey));
    }
}
