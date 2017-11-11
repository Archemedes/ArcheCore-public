package net.lordofthecraft.arche.persona;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.PersonaTags;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaTagRow;
import net.lordofthecraft.arche.save.rows.persona.PersonaTagRow;

public class ArchePersonaTags implements PersonaTags{
    private static final IConsumer consumer = ArcheCore.getConsumerControls();
    private final Map<String, TagAttachment> tags = new HashMap<>();
    private final OfflinePersona persona;
    private boolean forOffline;

    public ArchePersonaTags(OfflinePersona persona) {
        this.persona = persona;
    }
    
    void init(ResultSet rs, boolean isForOffline) throws SQLException {
    	forOffline = isForOffline;
    	while(rs.next()) {
    		String key = rs.getString("tag_key");
    		TagAttachment att = new TagAttachment(key,rs.getString("tag_value"),rs.getBoolean("offline"));
    		tags.put(key, att);
    	}
    }
    
    @Override
    public String getValue(String key) {
        if(tags.containsKey(key)) return tags.get(key).getValue();
        else return null;
    }
    
    @Override
    public TagAttachment getTag(String key) {
    	return tags.get(key);
    }
    
    @Override
    public void giveTag(String name, String value) {
    	giveTag(name, value, false);
    }

    @Override
    public void giveTag(String name, String value, boolean offline) {
    	giveTag(new TagAttachment(name, value, offline));
    }
    
    @Override
    public void giveTag(TagAttachment tag) {
    	off();
    	tags.put(tag.getKey(), tag);
    	consumer.queueRow(new PersonaTagRow(persona, tag));
    }

    @Override
    public boolean removeTag(String key) {
    	off();
        if (tags.containsKey(key)) {
            tags.remove(key);
            consumer.queueRow(new DeletePersonaTagRow(persona, key));
            return true;
        }
        
        return false;
    }
    

    private void off() {
    	if(forOffline) throw new IllegalStateException("Must load the persona before editing it!");
    }

    @Override
    public boolean hasTag(String key) {
        return tags.containsKey(key);
    }

    @Override
    public Collection<TagAttachment> getTags() {
        return Collections.unmodifiableCollection(tags.values());
    }
    
    @Override
    public Map<String, TagAttachment> getTagMap(){
    	return Collections.unmodifiableMap(tags);
    }
    
    @Override
    public int getPersonaid() {
        return persona.getPersonaId();
    }

    @Override
    public OfflinePersona getPersona() {
        return persona;
    }
}
