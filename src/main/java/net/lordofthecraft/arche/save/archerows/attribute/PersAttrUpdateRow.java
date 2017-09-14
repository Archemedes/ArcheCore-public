package net.lordofthecraft.arche.save.archerows.attribute;

import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PersAttrUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    final ExtendedAttributeModifier mod;
    private Connection connection;

    public PersAttrUpdateRow(ExtendedAttributeModifier mod) {
        this.mod = mod;
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
        PreparedStatement statement = connection.prepareStatement("UPDATE persona_attributes SET decaytype=? AND decaytime=? AND lostondeath=? WHERE persona_id_fk=? AND moduuid=? AND attribute_type=?");
        statement.setString(1, mod.getDecayStrategy().name());
        statement.setLong(2, mod.getTicksRemaining());
        statement.setBoolean(3, mod.isLostOnDeath());
        statement.setInt(4, mod.getPersonaId());
        statement.setString(5, mod.getUniqueId().toString());
        statement.setString(6, mod.getAttribute().getName());
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
                "UPDATE persona_attributes SET decaytype='" + mod.getDecayStrategy().name() + "'" +
                        " AND decaytime=" + mod.getTicksRemaining() + "" +
                        " AND lostondeath=" + mod.isLostOnDeath() + "" +
                        " WHERE persona_id_fk=" + mod.getPersonaId() + "" +
                        " AND moduuid='" + mod.getUniqueId().toString() + "'" +
                        " AND attribute_type='" + SQLUtil.mysqlTextEscape(mod.getAttribute().getName()) + "';"
        };
    }
}
