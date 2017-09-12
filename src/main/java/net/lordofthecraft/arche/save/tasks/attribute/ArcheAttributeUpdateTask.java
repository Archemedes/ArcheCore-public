package net.lordofthecraft.arche.save.tasks.attribute;

import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class ArcheAttributeUpdateTask extends StatementTask {

    private final ExtendedAttributeModifier mod;

    public ArcheAttributeUpdateTask(ExtendedAttributeModifier mod) {
        this.mod = mod;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, mod.getDecayStrategy().name());
        stat.setLong(2, mod.getTicksRemaining());
        stat.setBoolean(3, mod.isLostOnDeath());
        stat.setInt(4, mod.getPersonaId());
        stat.setString(5, mod.getUniqueId().toString());
        stat.setString(6, mod.getAttribute().getName());
    }

    @Override
    protected String getQuery() {
        return "UPDATE persona_attributes SET decaytype=? AND decaytime=? AND lostondeath=? WHERE persona_id_fk=? AND moduuid=? AND attribute_type=?";
    }
}
