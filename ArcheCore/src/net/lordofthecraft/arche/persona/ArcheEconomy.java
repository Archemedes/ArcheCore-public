package net.lordofthecraft.arche.persona;

import org.bukkit.configuration.file.*;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.save.*;
import net.lordofthecraft.arche.save.tasks.*;

public class ArcheEconomy implements Economy
{
    private final String singular;
    private final String plural;
    private final double lostOnDeath;
    private final double beginnerAmount;
    private final boolean proximity;
    
    public ArcheEconomy(final FileConfiguration config) {
        super();
        this.singular = config.getString("currency.name.singular");
        this.plural = config.getString("currency.name.plural");
        this.lostOnDeath = config.getDouble("fraction.lost.on.death") / 100.0;
        this.beginnerAmount = config.getDouble("first.persona.money");
        this.proximity = config.getBoolean("require.pay.proximity");
    }
    
    public void init() {
    }
    
    @Override
    public boolean has(final Persona p, final double amount) {
        return ((ArchePersona)p).money >= amount;
    }
    
    @Override
    public double getBalance(final Persona p) {
        return ((ArchePersona)p).money;
    }
    
    @Override
    public void setPersona(final Persona p, final double amount) {
        ((ArchePersona)p).money = amount;
        SaveHandler.getInstance().put(new UpdateTask(p, PersonaField.MONEY, ((ArchePersona)p).money));
    }
    
    @Override
    public void depositPersona(final Persona p, final double amount) {
        final ArchePersona archePersona = (ArchePersona)p;
        archePersona.money += amount;
        SaveHandler.getInstance().put(new UpdateTask(p, PersonaField.MONEY, ((ArchePersona)p).money));
    }
    
    @Override
    public void withdrawPersona(final Persona p, final double amount) {
        final ArchePersona archePersona = (ArchePersona)p;
        archePersona.money -= amount;
        SaveHandler.getInstance().put(new UpdateTask(p, PersonaField.MONEY, ((ArchePersona)p).money));
    }
    
    @Override
    public String currencyNameSingular() {
        return this.singular;
    }
    
    @Override
    public String currencyNamePlural() {
        return this.plural;
    }
    
    @Override
    public double getFractionLostOnDeath() {
        return this.lostOnDeath;
    }
    
    @Override
    public double getBeginnerAllowance() {
        return this.beginnerAmount;
    }
    
    @Override
    public boolean requirePaymentProximity() {
        return this.proximity;
    }
}
