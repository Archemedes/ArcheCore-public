package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.attributes.ArchebuteModifier;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class ArchebuteInsertTask extends StatementTask {

    private final ArchebuteModifier mod;

    public ArchebuteInsertTask(ArchebuteModifier mod) {
        this.mod = mod;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, mod.getMod().getUniqueId().toString());
        stat.setInt(2, mod.getPersona().getPersonaId());
        stat.setString(3, mod.getAttribute().name());
        stat.setDouble(4, mod.getMod().getAmount());
        stat.setString(5, mod.getMod().getOperation().name());
        stat.setTimestamp(6, mod.getCreation());
        stat.setTimestamp(7, mod.getDecayDate());
    }

    @Override
    protected String getQuery() {
        return "INSERT INTO persona_attributes(mod_uuid,persona_id_fk,attribute_type,mod_value,operation,created,decaytime) VALUES (?,?,?,?,?,?,?)";
    }
}
