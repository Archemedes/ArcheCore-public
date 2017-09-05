package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

/**
 * Created on 6/6/2017
 *
 * @author 501warhead
 */
public class PersonaTagTask extends StatementTask {

    private final boolean insert;
    private final int persona;
    private final String value;
    private final String name;

    public PersonaTagTask(boolean insert, int persona, String name, String value) {
        this.insert = insert;
        this.persona = persona;
        this.value = value;
        this.name = name;
    }

    @Override
    protected void setValues() throws SQLException {
        if (value == null) {
            stat.setString(1, name);
            stat.setInt(2, persona);
        } else {
            stat.setString(1, value);
            stat.setString(2, name);
            stat.setInt(3, persona);
        }
    }

    @Override
    protected String getQuery() {
        return (insert ? "INSERT INTO persona_tags(key,value,persona_id_fk) VALUES (?,?,?)" : (value == null ? "DELETE FROM persona_tags WHERE persona_id_fk=? AND key=?" : "UPDATE persona_tags SET value=? WHERE key=? AND persona_id_fk=?"));
    }
}
