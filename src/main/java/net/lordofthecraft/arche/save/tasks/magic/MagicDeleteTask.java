package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created on 7/11/2017
 *
 * @author 501warhead
 */
public class MagicDeleteTask extends StatementTask {

    private final UUID persona_id;
    private final String magic_name;

    public MagicDeleteTask(UUID persona_id, String magic_name) {
        this.persona_id = persona_id;
        this.magic_name = magic_name;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, persona_id.toString());
        stat.setString(2, magic_name);
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM persona_magic WHERE persona_id_fk=? AND magic_fk=?";
    }
}
