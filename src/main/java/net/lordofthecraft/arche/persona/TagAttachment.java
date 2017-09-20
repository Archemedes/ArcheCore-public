package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.persona.delete.DelPersTagRow;
import net.lordofthecraft.arche.save.archerows.persona.insert.PersTagRow;
import net.lordofthecraft.arche.save.archerows.persona.update.UpdatePersTagRow;

import java.util.Collections;
import java.util.Map;

/**
 * Created on 6/16/2017
 *
 * @author 501warhead
 */
public class TagAttachment {
    //private static final SaveHandler buffer = SaveHandler.getInstance();
    private static final IConsumer consumer = ArcheCore.getConsumerControls();
    private final Map<String, String> tags;
    private final Persona persona;
    private final boolean save;

    public TagAttachment(Map<String, String> tags, Persona persona, boolean save) {
        this.tags = tags;
        this.persona = persona;
        this.save = save;
    }

    public String getValue(String name) {
        return tags.get(name);
    }

    public void setValue(String name, String value) {
        if (tags.containsKey(name)) {
            tags.replace(name, value);
            if (save) consumer.queueRow(new UpdatePersTagRow(persona, name, value));
        } else {
            tags.put(name, value);
            if (save) consumer.queueRow(new PersTagRow(persona, name, value));
        }
    }

    public void delValue(String name) {
        if (tags.containsKey(name)) {
            tags.remove(name);
            if (save) consumer.queueRow(new DelPersTagRow(persona, name));
        }
    }

    public boolean hasKey(String name) {
        return tags.containsKey(name);
    }

    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public int getPersonaid() {
        return persona.getPersonaId();
    }

    public Persona getPersona() {
        return persona;
    }
}
