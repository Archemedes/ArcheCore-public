package net.lordofthecraft.arche.save.archerows.magic.delete;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MagicDeleteRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final ArcheMagic magic;
    private Connection connection = null;

    public MagicDeleteRow(Persona persona, ArcheMagic magic) {
        this.persona = persona;
        this.magic = magic;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof MagicDeleteRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return new MultiMagicDeleteRow(this, (MagicDeleteRow) second);
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM persona_magic WHERE persona_id_fk=? AND magic_fk=?");
        statement.setInt(1, persona.getPersonaId());
        statement.setString(2, magic.getName());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "DELETE FROM persona_magic WHERE persona_id_fk=" + persona.getPersonaId() + " AND magic_fk='" + magic.getName() + "';"
        };
    }
}
