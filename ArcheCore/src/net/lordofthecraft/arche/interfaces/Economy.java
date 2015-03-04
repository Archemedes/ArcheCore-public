package net.lordofthecraft.arche.interfaces;

public interface Economy
{
    boolean has(Persona p0, double p1);
    
    double getBalance(Persona p0);
    
    void depositPersona(Persona p0, double p1);
    
    void withdrawPersona(Persona p0, double p1);
    
    void setPersona(Persona p0, double p1);
    
    String currencyNameSingular();
    
    String currencyNamePlural();
    
    double getFractionLostOnDeath();
    
    double getBeginnerAllowance();
    
    boolean requirePaymentProximity();
}
