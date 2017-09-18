package net.lordofthecraft.arche.save.archerows.logging;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.persona.ArcheEconomy;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class EconomyLogRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final Transaction transaction;
    final ArcheEconomy.TransactionType type;
    final double before;
    final double after;
    final double amount;
    private Connection connection;

    public EconomyLogRow(Persona persona, Transaction transaction, ArcheEconomy.TransactionType type, double before, double after, double amount) {
        this.persona = persona;
        this.transaction = transaction;
        this.type = type;
        this.before = before;
        this.after = after;
        this.amount = amount;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof EconomyLogRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return new MultiEconomyLogRow(this, (EconomyLogRow) second);
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
        PreparedStatement statement = connection.prepareStatement("INSERT INTO econ_log(date,persona_id_fk,type,amount,plugin,reason,amt_before,amt_after) VALUES (?,?,?,?,?,?,?,?)");
        statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        statement.setInt(2, persona.getPersonaId());
        statement.setString(3, type.name());
        statement.setDouble(4, amount);
        statement.setString(5, transaction.getRegisteringPluginName());
        statement.setString(6, transaction.getCause());
        statement.setDouble(7, before);
        statement.setDouble(8, after);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        //I try to make this pretty and readable but honestly I CBA at this point enjoy
        return new String[]{
                "INSERT INTO econ_log(date,persona_id_fk,type,amount,plugin,reason,amt_before,amt_after) VALUES (FROM_UNIXTIME(" + System.currentTimeMillis() + ")," +
                        persona.getPersonaId() + "," +
                        "'" + type.name() + "',"
                        + amount + ",'"
                        + transaction.getRegisteringPluginName() + "',"
                        + "'" + transaction.getCause() + "'" +
                        "," + before + "," + after + ");"
        };
    }

    @Override
    public String toString() {
        return "EconomyLogRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", transaction=" + transaction +
                ", type=" + type.name() +
                ", before=" + before +
                ", after=" + after +
                ", amount=" + amount +
                '}';
    }
}
