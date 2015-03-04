package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.interfaces.*;
import java.sql.*;

public class PersonaRenameTask extends StatementTask
{
    private final PersonaKey key;
    private final String name;
    
    public PersonaRenameTask(final Persona pers) {
        super();
        this.key = pers.getPersonaKey();
        this.name = pers.getName();
    }
    
    @Override
    protected void setValues() throws SQLException {
        this.stat.setString(1, this.key.getPlayerUUID().toString());
        this.stat.setInt(2, this.key.getPersonaId());
        this.stat.setString(3, this.name);
    }
    
    @Override
    protected String getQuery() {
        return "INSERT INTO persona_names (player,id,name) VALUES (?,?,?)";
    }
}
