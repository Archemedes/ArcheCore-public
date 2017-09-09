package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.persona.MagicAttachment;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created on 7/5/2017
 *
 * @author 501warhead
 */
public class MagicUpdateTask extends StatementTask {

    private final int persona_id;
    private final String magic_name;
    private final MagicAttachment.Field field;
    private final Object toSet;

    public MagicUpdateTask(int persona_id, String magic_name, MagicAttachment.Field field, Object toSet) {
        this.persona_id = persona_id;
        this.magic_name = magic_name;
        this.field = field;
        this.toSet = toSet;
    }

    /*
CREATE TABLE IF NOT EXISTS persona_magics (
    magic_id        INT UNSIGNED AUTO_INCREMENT,
    magic_fk        VARCHAR(255) NOT NULL,
    persona_fk      CHAR(36) NOT NULL,
    tier            INT,
    last_advanced   TIMESTAMP DEFAULT NOW(),
    teacher         CHAR(36) DEFAULT NULL,
    learned         TIMESTAMP DEFAULT NOW(),
    visible         BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (magic_id),
    FOREIGN KEY (magic_fk) REFERENCES magics (name) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (persona_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, field.field);
        if (handle instanceof ArcheSQLiteHandler) {
            switch (field) {
                case TIER:
                case TEACHER:
                    stat.setInt(2, (int) toSet);
                    break;
                case LAST_ADVANCED:
                case LEARNED:
                    stat.setTimestamp(2, (Timestamp) toSet);
                    break;
                case VISIBLE:
                    stat.setBoolean(2, (boolean) toSet);
                    break;
            }
        } else {
            stat.setObject(2, toSet, field.type);
        }

        stat.setInt(3, persona_id);
        stat.setString(4, magic_name);
    }

    @Override
    protected String getQuery() {
        return "UPDATE persona_magic SET ?=? WHERE persona_id_fk=? AND magic_fk=?";
    }
}
