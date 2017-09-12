package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.sql.Timestamp;

public class MagicInsertTask extends StatementTask {

    private final int persona_id;
    private final ArcheMagic magic;
    private final int tier;
    private final Integer teacher;
    private final boolean visible;

    public MagicInsertTask(int persona_id, ArcheMagic magic, int tier, Integer teacher, boolean visible) {
        this.persona_id = persona_id;
        this.magic = magic;
        this.tier = tier;
        this.teacher = teacher;
        this.visible = visible;
    }

    @Override
    protected void setValues() throws SQLException {
        long time = System.currentTimeMillis();
        stat.setString(1, magic.getName());
        stat.setInt(2, persona_id);
        stat.setInt(3, tier);
        stat.setTimestamp(4, new Timestamp(time));
        stat.setInt(5, teacher);
        stat.setTimestamp(6, new Timestamp(time));
        stat.setBoolean(7, visible);
    }

    @Override
    protected String getQuery() {
        return "INSERT INTO persona_magics(magic_fk,persona_id_fk,tier,last_advanced,teacher,learned,visible) VALUES (?,?,?,?,?,?,?)";
    }
}
