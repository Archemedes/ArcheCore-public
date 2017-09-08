package net.lordofthecraft.arche.save.tasks.logging;

import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.sql.Timestamp;

public class InsertEconomyLogTask extends StatementTask {

    private final int persona_id;
    private final Transaction transaction;
    private final double amount;
    private final double amt_before;
    private final double amt_after;

    public InsertEconomyLogTask(int persona_id, Transaction transaction, double amount, double amt_before, double amt_after) {
        this.persona_id = persona_id;
        this.transaction = transaction;
        this.amount = amount;
        this.amt_before = amt_before;
        this.amt_after = amt_after;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        stat.setInt(2, persona_id);
        stat.setString(3, transaction.getType().name());
        stat.setDouble(4, amount);
        stat.setString(5, transaction.getRegisteringPluginName());
        stat.setString(6, transaction.getCause());
        stat.setDouble(7, amt_before);
        stat.setDouble(8, amt_after);
    }

    @Override
    protected String getQuery() {
        /*
                statement.execute("CREATE TABLE IF NOT EXISTS econ_log(" +
                "date TIMESTAMP," +
                "persona_id_fk INT UNSIGNED" +
                "type VARCHAR(255)," +
                "amount DOUBLE," +
                "plugin TEXT," +
                "reason TEXT," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona(persona_id) ON UPDATE CASCADE ON DELETE SET NULL" +
                ")" +
                end);
         */
        return "INSERT INTO econ_log(date,persona_id_fk,type,amount,plugin,reason,amt_before,amt_after) VALUES (?,?,?,?,?,?,?,?)";
    }
}
