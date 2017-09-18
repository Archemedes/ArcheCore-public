package net.lordofthecraft.arche.save.tasks.attribute;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class ArcheAttributeRemoveTask extends StatementTask {

    private final ExtendedAttributeModifier mod;
    private final int personaId;
    private final ArcheAttribute aa;
    
    public ArcheAttributeRemoveTask(ExtendedAttributeModifier archeAttributeModifier, Persona p, ArcheAttribute aa) {
        mod = archeAttributeModifier;
        this.personaId = p.getPersonaId();
        this.aa = aa;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, mod.getUniqueId().toString());
        stat.setInt(2, personaId);
        stat.setString(3, aa.getName());
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM persona_attributes WHERE moduuid=? AND persona_id_fk=? AND attribute_type=?";
    }
}
