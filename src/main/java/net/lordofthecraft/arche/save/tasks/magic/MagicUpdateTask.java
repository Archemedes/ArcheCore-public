package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created on 7/5/2017
 *
 * @author 501warhead
 */
public class MagicUpdateTask extends StatementTask {

    private final int magic_id;
    private final int tier;
    private final long learned;
    private final long last_advanced;
    private final UUID teacher;
    private final boolean visible;

    public MagicUpdateTask(int magic_id, int tier, long learned, long last_advanced, UUID teacher, boolean visible) {
        this.magic_id = magic_id;
        this.tier = tier;
        this.learned = learned;
        this.last_advanced = last_advanced;
        this.teacher = teacher;
        this.visible = visible;
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
        stat.setInt(1, tier);
        stat.setTimestamp(2, new Timestamp(learned));
        stat.setTimestamp(3, new Timestamp(last_advanced));
        stat.setString(4, (teacher == null ? null : teacher.toString()));
        stat.setBoolean(5, visible);
        stat.setInt(6, magic_id);
    }

    @Override
    protected String getQuery() {
        return "UPDATE persona_magic SET tier=? AND last_advanced=? AND teacher=? AND learned=? AND visible=? WHERE magic_id=?";
    }
}
