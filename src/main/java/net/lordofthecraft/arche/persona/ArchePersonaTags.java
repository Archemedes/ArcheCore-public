package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.PersonaTags;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaTagRow;
import net.lordofthecraft.arche.save.rows.persona.PersonaTagRow;
import org.apache.commons.lang.Validate;

import com.google.common.base.Objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArchePersonaTags implements PersonaTags {
    private static final IConsumer consumer = ArcheCore.getConsumerControls();
    private final Map<String, TagAttachment> tags = new HashMap<>();
    private final OfflinePersona persona;
    private boolean forOffline;

    private boolean wasInit = false;

    public ArchePersonaTags(OfflinePersona persona) {
        this.persona = persona;
    }

    void init(ResultSet rs, boolean isForOffline) throws SQLException {
        Validate.isTrue(!wasInit, "Can only init a PersonaTags instance once");


        forOffline = isForOffline;
        while (rs.next()) {
            String key = rs.getString("tag_key");
            TagAttachment att = new TagAttachment(key, rs.getString("tag_value"), forOffline);
            tags.put(key, att);
        }

        wasInit = true;
    }

    void merge(ArchePersonaTags fromOffline) {
        Validate.isTrue(!forOffline, "Trying to merge INTO PersonaTags that are for OFFLINE Persona");
        Validate.isTrue(fromOffline.forOffline, "Trying to merge FROM PersonaTags that are for ONLINE Persona");

        fromOffline.getTags().forEach(t -> tags.put(t.getKey(), t));
    }
    
    @Override
    public String getValue(String key) {
        if (tags.containsKey(key)) return tags.get(key).getValue();
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
        if (forOffline && !tag.isAvailableOffline())
            throw new IllegalArgumentException("Trying to add online-only tags to an Offline Persona");

        String k = tag.getKey();
        
        if(tags.containsKey(k)) {
        	TagAttachment othertag = tags.get(k);
        	if(tag.isAvailableOffline() == othertag.isAvailableOffline()
        			&& Objects.equal(othertag.getValue(), tag.getValue()))
        		return; //Tag already exists fully
        }
        
        tags.put(k, tag);
        consumer.queueRow(new PersonaTagRow(persona, tag));
    }

    @Override
    public boolean removeTag(String key) {
        if (tags.containsKey(key)) {
            tags.remove(key);
            consumer.queueRow(new DeletePersonaTagRow(persona, key));
            return true;
        }

        return false;
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
    public Map<String, TagAttachment> getTagMap() {
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public OfflinePersona getPersona() {
        return persona;
    }
}
