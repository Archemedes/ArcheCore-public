package net.lordofthecraft.arche.save.tasks.attribute;

import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class ArcheAttributeRemoveTask extends StatementTask {

    private final ExtendedAttributeModifier mod;

    public ArcheAttributeRemoveTask(ExtendedAttributeModifier archeAttributeModifier) {
        mod = archeAttributeModifier;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, mod.getUniqueId().toString());
        stat.setInt(2, mod.getPersonaId());
        stat.setString(3, mod.getAttribute().getName());
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM persona_attributes WHERE moduuid=? AND persona_id_fk=? AND attribute_type=?";
    }
}
