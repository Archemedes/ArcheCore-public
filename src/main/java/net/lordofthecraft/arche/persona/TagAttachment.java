package net.lordofthecraft.arche.persona;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaTagRow;
import net.lordofthecraft.arche.save.rows.persona.PersonaTagRow;

/**
 * Created on 6/16/2017
 *
 * @author 501warhead
 */
public class TagAttachment {
    private static final IConsumer consumer = ArcheCore.getConsumerControls();
    private final Map<String, String> tags = new HashMap<>();
    private final Persona persona;
    private boolean save = false;

    public TagAttachment(Persona persona) {
        this.persona = persona;
    }
    
    void init(ResultSet rs) throws SQLException {
    	while(rs.next()) {
    		tags.put(rs.getString("tag_key"), rs.getString("tag_value"));
    	}
    }

    public String getValue(String name) {
        return tags.get(name);
    }

    public void setValue(String name, String value) {
    	tags.put(name, value);
    	if (save) consumer.queueRow(new PersonaTagRow(persona, name, value));
    }

    public void delValue(String name) {
        if (tags.containsKey(name)) {
            tags.remove(name);
            if (save) consumer.queueRow(new DeletePersonaTagRow(persona, name));
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
