package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.logging.TransactionRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import org.bukkit.configuration.file.FileConfiguration;

public class ArcheEconomy implements Economy {
    public enum TransactionType {
        WITHDRAW,
        DEPOSIT,
        SET
    }
	
	private final String singular,plural;
	private final double lostOnDeath;
	private final double beginnerAmount;
	private final boolean proximity;
    //private ArcheExecutor buffer = ArcheExecutor.getInstance();
    private IConsumer consumer;

    public ArcheEconomy(FileConfiguration config){
		singular = config.getString("currency.name.singular");
		plural = config.getString("currency.name.plural");
		
		lostOnDeath = config.getDouble("fraction.lost.on.death") / 100d;
		beginnerAmount = config.getDouble("first.persona.money");
		proximity = config.getBoolean("require.pay.proximity");
	}
	
	public void init(){
        consumer = ArcheCore.getConsumerControls();
    }
	
	@Override
	public boolean has(Persona p, double amount){
		return ((ArchePersona) p).money >= amount;
	}
	
	@Override
	public double getBalance(Persona p){
		return ((ArchePersona) p).money;
	}
	
	@Override
    public void setPersona(Persona p, double amount, Transaction transaction) {
        double before = getBalance(p);
        ((ArchePersona) p).money = amount;
        consumer.queueRow(new UpdatePersonaRow(p, PersonaField.MONEY, ((ArchePersona) p).money));
        consumer.queueRow(new TransactionRow(p, transaction, TransactionType.SET, amount, before, getBalance(p)));
    }

    @Override
    public void depositPersona(Persona p, double amount, Transaction transaction) {
        double before = getBalance(p);
        ((ArchePersona) p).money += amount;
        consumer.queueRow(new UpdatePersonaRow(p, PersonaField.MONEY, ((ArchePersona) p).money));
        consumer.queueRow(new TransactionRow(p, transaction, TransactionType.DEPOSIT, amount, before, getBalance(p)));
    }

    @Override
    public void withdrawPersona(Persona p, double amount, Transaction transaction) {
        double before = getBalance(p);
        ((ArchePersona) p).money -= amount;
        consumer.queueRow(new UpdatePersonaRow(p, PersonaField.MONEY, ((ArchePersona) p).money));
        consumer.queueRow(new TransactionRow(p, transaction, TransactionType.WITHDRAW, amount, before, getBalance(p)));
    }

    @Override
	public String currencyNameSingular(){
		return singular;
	}
	
	@Override
	public String currencyNamePlural(){
		return plural;
	}
	
	@Override
	public double getFractionLostOnDeath(){
		return lostOnDeath;
	}
	
	@Override
	public double getBeginnerAllowance(){
		return beginnerAmount;
	}

	@Override
	public boolean requirePaymentProximity() {
		return proximity;
	}
}
