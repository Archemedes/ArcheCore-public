package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.persona.PersonaTagTask;

import java.util.Collections;
import java.util.Map;

/**
 * Created on 6/16/2017
 *
 * @author 501warhead
 */
public class TagAttachment {
    private static final SaveHandler buffer = SaveHandler.getInstance();
    private final Map<String, String> tags;
    private final int persona_id;
    private final boolean save;

    public TagAttachment(Map<String, String> tags, int persona_id, boolean save) {
        this.tags = tags;
        this.persona_id = persona_id;
        this.save = save;
    }

    public String getValue(String name) {
        return tags.get(name);
    }

    public void setValue(String name, String value) {
        if (tags.containsKey(name)) {
            tags.replace(name, value);
            if (save) buffer.put(new PersonaTagTask(false, persona_id, name, value));
        } else {
            tags.put(name, value);
            if (save) buffer.put(new PersonaTagTask(true, persona_id, name, value));
        }
    }

    public void delValue(String name) {
        if (tags.containsKey(name)) {
            tags.remove(name);
            if (save) buffer.put(new PersonaTagTask(false, persona_id, name, null));
        }
    }

    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public int getPersonaid() {
        return persona_id;
    }
}
