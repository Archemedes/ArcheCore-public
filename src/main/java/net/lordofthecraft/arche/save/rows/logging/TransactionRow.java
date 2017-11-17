package net.lordofthecraft.arche.save.rows.logging;

import java.sql.Timestamp;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.persona.ArcheEconomy;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class TransactionRow extends SingleStatementRow {
    private final int persona;
    private final Transaction transaction;
    private final ArcheEconomy.TransactionType type;
    private final double before;
    private final double after;
    private final double amount;
    
    private final Timestamp now = now();

    public TransactionRow(Persona persona, Transaction transaction, ArcheEconomy.TransactionType type, double before, double after, double amount) {
        this.persona = persona.getPersonaId();
        this.transaction = transaction;
        this.type = type;
        this.before = before;
        this.after = after;
        this.amount = amount;
    }

    
	@Override
	protected String getStatement() {
		return "INSERT INTO econ_log(date,persona_id_fk,type,amount,plugin,reason,amt_before,amt_after) VALUES (?,?,?,?,?,?,?,?)";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return now;
		case 2: return persona;
		case 3: return type.name();
		case 4: return amount;
		case 5: transaction.getRegisteringPluginName();
		case 6: transaction.getCause();
		case 7: return before;
		case 8: return after;
		default: throw new IllegalArgumentException();
		}
	}

}
