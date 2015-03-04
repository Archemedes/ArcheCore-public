package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.save.*;
import java.sql.*;

public class UpdateTask extends ArcheTask
{
    private final Persona persona;
    private final PersonaField field;
    private final Object value;
    
    public UpdateTask(final Persona persona, final PersonaField field, final Object value) {
        super();
        this.persona = persona;
        this.field = field;
        this.value = value;
    }
    
    @Override
    public void run() {
        try {
            final Connection c = UpdateTask.handle.getSQL().getConnection();
            final PreparedStatement stat = this.field.getStatement(c);
            if (this.value instanceof String) {
                stat.setString(1, (String)this.value);
            }
            else if (this.value instanceof Boolean) {
                stat.setBoolean(1, (boolean)this.value);
            }
            else if (this.value instanceof Integer) {
                stat.setInt(1, (int)this.value);
            }
            else if (this.value instanceof Long) {
                stat.setLong(1, (long)this.value);
            }
            else if (this.value == null) {
                stat.setString(1, null);
            }
            else {
                stat.setString(1, this.value.toString());
            }
            stat.setString(2, this.persona.getPlayerUUID().toString());
            stat.setInt(3, this.persona.getId());
            stat.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
