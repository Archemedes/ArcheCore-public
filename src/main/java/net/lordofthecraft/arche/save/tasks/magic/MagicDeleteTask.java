package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

/**
 * Created on 7/11/2017
 *
 * @author 501warhead
 */
public class MagicDeleteTask extends StatementTask {

    private final int magicid;

    public MagicDeleteTask(int magicid) {
        this.magicid = magicid;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setInt(1, magicid);
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM persona_magic WHERE magic_id=?";
    }
}
