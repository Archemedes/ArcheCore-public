package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

/**
 * Created on 7/11/2017
 *
 * @author 501warhead
 */
public class ArcheMagicDeleteTask extends StatementTask {

    private final String name;

    public ArcheMagicDeleteTask(String name) {
        this.name = name;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, name);
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM magics WHERE name=?";
    }
}
