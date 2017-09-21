package net.lordofthecraft.arche.save.tasks.attribute;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class ArcheAttributeUpdateTask extends StatementTask {

    private final ExtendedAttributeModifier mod;
    private final int personaId;
    private final ArcheAttribute aa;
    
    public ArcheAttributeUpdateTask(ExtendedAttributeModifier mod, Persona p, ArcheAttribute aa) {
        this.mod = mod;
        this.personaId = p.getPersonaId();
        this.aa = aa;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, mod.getDecayStrategy().name());
        stat.setLong(2, mod.getTicksRemaining());
        stat.setBoolean(3, mod.isLostOnDeath());
        stat.setInt(4, personaId);
        stat.setString(5, mod.getUniqueId().toString());
        stat.setString(6, aa.getName());
    }

    @Override
    protected String getQuery() {
        return "UPDATE persona_attributes SET decaytype=? AND decaytime=? AND lostondeath=? WHERE persona_id_fk=? AND mod_uuid=? AND attribute_type=?";
    }
}
