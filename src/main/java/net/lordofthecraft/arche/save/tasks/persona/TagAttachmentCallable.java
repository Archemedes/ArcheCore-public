package net.lordofthecraft.arche.save.tasks.persona;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.persona.TagAttachment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created on 6/20/2017
 *
 * @author 501warhead
 */
public class TagAttachmentCallable implements Callable<TagAttachment> {

    private final int persona_id;
    private final SQLHandler handler;

    public TagAttachmentCallable(int persona_id, SQLHandler handler) {
        this.persona_id = persona_id;
        this.handler = handler;
    }

    @Override
    public TagAttachment call() throws Exception {
        PreparedStatement stat = handler.getConnection().prepareStatement("SELECT key,value FROM persona_tags WHERE persona_id_fk=?");
        stat.setInt(1, persona_id);
        ResultSet rs = stat.executeQuery();
        Map<String, String> tags = Maps.newConcurrentMap();
        while (rs.next()) {
            tags.put(rs.getString("key"), rs.getString("value"));
        }
        rs.close();
        stat.close();
        return new TagAttachment(tags, persona_id, true);
    }
}
