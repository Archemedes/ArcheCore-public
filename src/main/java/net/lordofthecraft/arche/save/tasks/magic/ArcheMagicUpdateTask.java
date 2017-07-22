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

    public ArcheMagicUpdateTask(ArcheMagic magic) {
        this.magic = magic;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setInt(1, magic.getMaxTier());
        stat.setBoolean(2, magic.isSelfTeachable());
        stat.setString(3, magic.getName());
    }

    @Override
    protected String getQuery() {
        return "UPDATE magics SET max_tier=? AND self_teach=? WHERE name=?";
    }
}
