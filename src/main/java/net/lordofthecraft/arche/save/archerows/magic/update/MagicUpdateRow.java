package net.lordofthecraft.arche.save.archerows.magic.update;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.MagicAttachment;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;

import java.sql.Connection;
import java.sql.SQLException;

public class MagicUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final String magic_name;
    final MagicAttachment.Field field;
    final Object toSet;
    private Connection connection = null;

    public MagicUpdateRow(Persona persona, String magic_name, MagicAttachment.Field field, Object toSet) {
        this.persona = persona;
        this.magic_name = magic_name;
        this.field = field;
        this.toSet = toSet;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof MagicUpdateRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        return null;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {

    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        return new String[0];
    }
}
