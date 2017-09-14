package net.lordofthecraft.arche.save.archerows.attribute;

import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PersAttrRemoveRow implements ArcheMergeableRow, ArchePersonaRow {

    final ExtendedAttributeModifier mod;
    private Connection connection;

    public PersAttrRemoveRow(ExtendedAttributeModifier mod) {
        this.mod = mod;
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
        PreparedStatement statement = connection.prepareStatement("DELETE FROM persona_attributes WHERE moduuid=? AND persona_id_fk=? AND attribute_type=?");
        statement.setString(1, mod.getUniqueId().toString());
        statement.setInt(2, mod.getPersonaId());
        statement.setString(3, mod.getAttribute().getName());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{mod.getPersona()};
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "DELETE FROM persona_attributes WHERE moduuid='" + mod.getUniqueId().toString() + "' AND persona_id_fk=" + mod.getPersonaId() + " AND attribute_type='" + SQLUtil.mysqlTextEscape(mod.getAttribute().getName()) + "';"
        };
    }
}
