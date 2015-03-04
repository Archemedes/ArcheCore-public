package net.lordofthecraft.arche.skill;

import java.util.concurrent.*;
import net.lordofthecraft.arche.persona.*;
import net.lordofthecraft.arche.SQL.*;
import java.sql.*;

public class SkillDataCallable implements Callable<SkillData>
{
    private final ArchePersona persona;
    private final String skill;
    private final SQLHandler handler;
    
    public SkillDataCallable(final ArchePersona persona, final String skill, final SQLHandler handler) {
        super();
        this.persona = persona;
        this.skill = skill;
        this.handler = handler;
    }
    
    @Override
    public SkillData call() throws SQLException {
        final String query = "SELECT xp,visible FROM sk_" + this.skill + " WHERE player='" + this.persona.getPlayerUUID().toString() + "' AND id=" + this.persona.getId() + ";";
        SkillData data;
        synchronized (this.handler) {
            final ResultSet res = this.handler.query(query);
            if (res.next()) {
                final double xp = res.getDouble(1);
                final boolean visible = res.getBoolean(2);
                data = new SkillData(xp, visible);
            }
            else {
                data = null;
            }
            res.close();
            res.getStatement().close();
        }
        return data;
    }
}
