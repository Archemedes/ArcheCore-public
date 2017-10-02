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

public class PersAttrRemoveRow implements ArcheMergeableRow, ArchePersonaRow {

    final ExtendedAttributeModifier mod;
    final ArcheAttribute attribute;
    final Persona persona;
    private Connection connection;

    public PersAttrRemoveRow(ExtendedAttributeModifier mod, ArcheAttribute attribute, Persona persona) {
        this.mod = mod;
        this.attribute = attribute;
        this.persona = persona;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof PersAttrRemoveRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return null;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM persona_attributes WHERE mod_uuid=? AND persona_id_fk=? AND attribute_type=?");
        statement.setString(1, mod.getUniqueId().toString());
        statement.setInt(2, persona.getPersonaId());
        statement.setString(3, attribute.getName());
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
                "DELETE FROM persona_attributes WHERE mod_uuid='" + mod.getUniqueId().toString() + "' AND persona_id_fk=" + persona.getPersonaId() + " AND attribute_type='" + SQLUtil.mysqlTextEscape(attribute.getName()) + "';"
        };
    }

    @Override
    public String toString() {
        return "PersAttrRemoveRow{" +
                "mod=" + mod +
                ", attribute=" + attribute +
                ", persona=" + MessageUtil.identifyPersona(persona) +
                '}';
    }
}
