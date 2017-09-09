package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.attributes.ArchebuteModifier;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class ArchebuteRemoveTask extends StatementTask {

    private final ArchebuteModifier mod;

    public ArchebuteRemoveTask(ArchebuteModifier mod) {
        this.mod = mod;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setInt(1, mod.getPersona().getPersonaId());
        stat.setString(2, mod.getMod().getUniqueId().toString());
        stat.setString(3, mod.getAttribute().name());
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM persona_attributes WHERE persona_id_fk=? AND mod_uuid=? AND attribute_type=?";
    }
}
