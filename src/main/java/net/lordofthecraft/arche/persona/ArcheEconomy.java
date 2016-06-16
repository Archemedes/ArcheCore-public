package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.UpdateTask;
import org.bukkit.configuration.file.FileConfiguration;

public class ArcheEconomy implements Economy {
	private final String singular,plural;
	private final double lostOnDeath;
	private final double beginnerAmount;
	private final boolean proximity;
	
	public ArcheEconomy(FileConfiguration config){
		singular = config.getString("currency.name.singular");
		plural = config.getString("currency.name.plural");
		
		lostOnDeath = config.getDouble("fraction.lost.on.death") / 100d;
		beginnerAmount = config.getDouble("first.persona.money");
		proximity = config.getBoolean("require.pay.proximity");
	}
	
	public void init(){
		
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
	public void setPersona(Persona p, double amount){
		((ArchePersona) p).money = amount;
		SaveHandler.getInstance().put(new UpdateTask(p, PersonaField.MONEY, ((ArchePersona) p).money));
	}
	
	@Override
	public void depositPersona(Persona p, double amount){
		((ArchePersona) p).money += amount;
		SaveHandler.getInstance().put(new UpdateTask(p, PersonaField.MONEY, ((ArchePersona) p).money));
	}
	
	@Override
	public void withdrawPersona(Persona p, double amount){
		((ArchePersona) p).money -= amount;
		SaveHandler.getInstance().put(new UpdateTask(p, PersonaField.MONEY, ((ArchePersona) p).money));
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
