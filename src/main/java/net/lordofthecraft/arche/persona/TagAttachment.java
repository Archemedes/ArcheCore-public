package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.save.SaveExecutorManager;
import net.lordofthecraft.arche.save.tasks.persona.PersonaTagTask;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 6/16/2017
 *
 * @author 501warhead
 */
public class TagAttachment {
    private static final SaveExecutorManager buffer = SaveExecutorManager.getInstance();
    private final Map<String,String> tags;
    private final UUID persona_id;
    private final boolean save;

    public TagAttachment(Map<String,String> tags, UUID persona_id, boolean save) {
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
            if (save) buffer.submit(new PersonaTagTask(false, persona_id, name, value));
        } else {
            tags.put(name, value);
            if (save) buffer.submit(new PersonaTagTask(true, persona_id, name, value));
        }
    }

    public void delValue(String name) {
        if (tags.containsKey(name)) {
            tags.remove(name);
            if (save) buffer.submit(new PersonaTagTask(false, persona_id, name, null));
        }
    }

    public Map<String,String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public UUID getPersonaid() {
        return persona_id;
    }
}
