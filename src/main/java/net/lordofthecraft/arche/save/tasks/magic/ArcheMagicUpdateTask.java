package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

/**
 * Created on 7/11/2017
 *
 * @author 501warhead
 */
public class ArcheMagicUpdateTask extends StatementTask {

    /*
    CREATE TABLE IF NOT EXISTS magics (
    name        VARCHAR(255),
    max_tier    INT,
    self_teach  BOOLEAN,
    PRIMARY KEY (name)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX idx_magic ON magics (name);
     */

    private final ArcheMagic magic;
    private final ArcheMagic.Field field;
    private final Object data;

    public ArcheMagicUpdateTask(ArcheMagic magic, ArcheMagic.Field field, Object data) {
        this.magic = magic;
        this.field = field;
        this.data = data;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setObject(1, data, field.type);
        stat.setString(2, magic.getName());
    }

    @Override
    protected String getQuery() {
        return "UPDATE magics SET " + field.field + "=? WHERE name=?";
    }
}
