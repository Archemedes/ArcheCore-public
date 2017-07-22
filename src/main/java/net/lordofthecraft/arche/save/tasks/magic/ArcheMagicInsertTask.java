package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

/**
 * Created on 7/11/2017
 *
 * @author 501warhead
 */
public class ArcheMagicInsertTask extends StatementTask {

    private final ArcheMagic magic;

    public ArcheMagicInsertTask(ArcheMagic magic) {
        this.magic = magic;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, magic.getName());
        stat.setInt(2, magic.getMaxTier());
        stat.setBoolean(3, magic.isSelfTeachable());
    }

    @Override
    protected String getQuery() {
        return "INSERT INTO magics (name,max_tier,self_teach) VALUES (?,?,?)";
    }
}
