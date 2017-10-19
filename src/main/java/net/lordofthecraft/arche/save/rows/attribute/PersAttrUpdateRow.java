package net.lordofthecraft.arche.save.rows.attribute;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PersAttrUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    final ExtendedAttributeModifier mod;
    final Persona persona;
    final ArcheAttribute attribute;
    private Connection connection;

    public PersAttrUpdateRow(ExtendedAttributeModifier mod, Persona persona, ArcheAttribute attribute) {
        this.mod = mod;
        this.persona = persona;
        this.attribute = attribute;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof PersAttrUpdateRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return new MultiPersAttrUpdateRow(this, (PersAttrUpdateRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE persona_attributes SET decaytype=?, decayticks=?, lostondeath=? WHERE persona_id_fk=? AND mod_uuid=? AND attribute_type=?");
        statement.setString(1, mod.getDecayStrategy().name());
        statement.setLong(2, mod.getTicksRemaining());
        statement.setBoolean(3, mod.isLostOnDeath());
        statement.setInt(4, persona.getPersonaId());
        statement.setString(5, mod.getUniqueId().toString());
        statement.setString(6, attribute.getName());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "UPDATE persona_attributes SET decaytype='" + mod.getDecayStrategy().name() + "'" +
                        ", decaytime=" + mod.getTicksRemaining() + "" +
                        ", lostondeath=" + mod.isLostOnDeath() + "" +
                        " WHERE persona_id_fk=" + persona.getPersonaId() + "" +
                        " AND mod_uuid='" + mod.getUniqueId().toString() + "'" +
                        " AND attribute_type='" + SQLUtil.mysqlTextEscape(attribute.getName()) + "';"
        };
    }

    @Override
    public String toString() {
        return "PersAttrUpdateRow{" +
                "mod=" + mod +
                ", persona=" + MessageUtil.identifyPersona(persona) +
                ", attribute=" + attribute +
                '}';
    }
}
