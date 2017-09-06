package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created on 6/6/2017
 *
 * @author 501warhead
 */
public class PersonaTagTask extends StatementTask {

    private final boolean insert;
    private final UUID persona;
    private final String value;
    private final String name;

    public PersonaTagTask(boolean insert, UUID persona, String name, String value) {
        this.insert = insert;
        this.persona = persona;
        this.value = value;
        this.name = name;
    }

    @Override
    protected void setValues() throws SQLException {
        if (value == null) {
            stat.setString(1, name);
            stat.setString(2, persona.toString());
        } else {
            stat.setString(1, value);
            stat.setString(2, name);
            stat.setString(3, persona.toString());
        }
    }

    @Override
    protected String getQuery() {
        return (insert ? "INSERT INTO persona_tags(tag_key,tag_value,persona_id_fk) VALUES (?,?,?)" : (value == null ? "DELETE FROM persona_tags WHERE persona_id_fk=? AND tag_key=?" : "UPDATE persona_tags SET value=? WHERE tag_key=? AND persona_id_fk=?"));
    }
}