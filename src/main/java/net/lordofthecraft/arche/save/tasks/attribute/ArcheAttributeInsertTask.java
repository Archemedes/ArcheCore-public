package net.lordofthecraft.arche.save.tasks.attribute;

import java.sql.SQLException;
import java.sql.Timestamp;

import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.tasks.StatementTask;

public class ArcheAttributeInsertTask extends StatementTask {

    private final ExtendedAttributeModifier mod;
    private final int personaId;
    private final ArcheAttribute aa;
    
    public ArcheAttributeInsertTask(ExtendedAttributeModifier mod, Persona p, ArcheAttribute aa) {
        this.mod = mod;
        this.personaId = p.getPersonaId();
        this.aa = aa;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, mod.getUniqueId().toString());
        stat.setInt(2, personaId);
        stat.setString(3, aa.getName());
        stat.setString(4, mod.getName());
        stat.setDouble(5, mod.getAmount());
        stat.setString(6, mod.getOperation().name());
        stat.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
        stat.setLong(8, mod.getTicksRemaining());
        stat.setString(9, mod.getDecayStrategy().name());
        stat.setBoolean(10, mod.isLostOnDeath());
    }

    @Override
    protected String getQuery() {
        return "INSERT " + (handle instanceof WhySQLHandler ? "IGNORE " : "OR IGNORE ") + " INTO persona_attributes(mod_uuid,persona_id_fk,attribute_type,mod_name,mod_value,operation,created,decayticks,decaytype,lostondeath) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
    }
}
