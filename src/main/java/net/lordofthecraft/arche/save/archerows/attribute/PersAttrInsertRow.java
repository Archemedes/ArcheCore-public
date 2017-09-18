package net.lordofthecraft.arche.save.archerows.attribute;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PersAttrInsertRow implements ArcheMergeableRow, ArchePersonaRow {

    final ExtendedAttributeModifier mod;
    final Persona persona;
    final ArcheAttribute attribute;
    private Connection connection;

    public PersAttrInsertRow(ExtendedAttributeModifier mod, Persona persona, ArcheAttribute attribute) {
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
        return !row.isUnique() && row instanceof PersAttrInsertRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge a unique row");
        }
        return new MultiPersAttrInsertRow(this, (PersAttrInsertRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT " + (!ArcheCore.getPlugin().isUsingSQLite() ? "IGNORE " : "OR IGNORE ") + " INTO persona_attributes(mod_uuid,persona_id_fk,attribute_type,mod_name,mod_value,operation,created,decayticks,decaytype,lostondeath) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)");
        statement.setString(1, mod.getUniqueId().toString());
        statement.setInt(2, persona.getPersonaId());
        statement.setString(3, attribute.getName());
        statement.setString(4, mod.getName());
        statement.setDouble(5, mod.getAmount());
        statement.setString(6, mod.getOperation().name());
        statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
        statement.setLong(8, mod.getTicksRemaining());
        statement.setString(9, mod.getDecayStrategy().name());
        statement.setBoolean(10, mod.isLostOnDeath());
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
                "INSERT " + (!ArcheCore.getPlugin().isUsingSQLite() ? "IGNORE " : "OR IGNORE ") + " INTO persona_attributes(mod_uuid,persona_id_fk,attribute_type,mod_name,mod_value,operation,created,decayticks,decaytype,lostondeath) " +
                        "VALUES ('" + mod.getUniqueId().toString()
                        + "'," + persona.getPersonaId()
                        + ",'" + SQLUtil.mysqlTextEscape(attribute.getName())
                        + "','" + SQLUtil.mysqlTextEscape(mod.getName())
                        + "'," + mod.getAmount()
                        + ",'" + mod.getOperation().name()
                        + "',FROM_UNIXTIME(" + System.currentTimeMillis() + ")"
                        + "," + mod.getTicksRemaining()
                        + ",'" + mod.getDecayStrategy().name() +
                        "'," + mod.isLostOnDeath()
                        + ");"
        };
    }

    @Override
    public String toString() {
        return "PersAttrInsertRow{" +
                "mod=" + mod +
                ", persona=" + MessageUtil.identifyPersona(persona) +
                ", attribute=" + attribute +
                '}';
    }
}
