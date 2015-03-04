package net.lordofthecraft.arche.save.tasks;

import java.util.*;
import org.bukkit.*;
import net.lordofthecraft.arche.persona.*;
import java.sql.*;

public class PersonaSwitchTask extends StatementTask
{
    private final UUID uuid;
    private final int id;
    private final Location l;
    private final PersonaInventory inv;
    
    public PersonaSwitchTask(final ArchePersona p) {
        super();
        this.uuid = p.getPlayerUUID();
        this.id = p.getId();
        this.l = p.getLocation();
        this.inv = p.getInventory();
    }
    
    @Override
    protected void setValues() throws SQLException {
        this.stat.setString(1, this.l.getWorld().getUID().toString());
        this.stat.setInt(2, this.l.getBlockX());
        this.stat.setInt(3, this.l.getBlockY());
        this.stat.setInt(4, this.l.getBlockZ());
        this.stat.setString(5, this.inv.getAsString());
        this.stat.setString(6, this.uuid.toString());
        this.stat.setInt(7, this.id);
    }
    
    @Override
    protected String getQuery() {
        return "UPDATE persona SET world=?, x=?, y=?, z=?, inv=? WHERE player=? AND id=?";
    }
}
