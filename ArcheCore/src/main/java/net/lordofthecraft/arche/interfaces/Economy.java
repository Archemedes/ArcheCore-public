package net.lordofthecraft.arche.interfaces;

/**
 * The ArcheCore economy activates Persona-tied currency tracking and basic functionality to maniplate such 
 */
public interface Economy {

	/**
	 * @param p Persona to check
	 * @param amount Amount of money the Persona must have
	 * @return If Persona p has this amount of money
	 */
	boolean has(Persona p, double amount);
	
	/**
	 * @param p Persona to check
	 * @return the amount of Minas a Persona possesses
	 */
	double getBalance(Persona p);
	
	/**
	 * Give money to a Persona. Negative amounts possible, but consider using {@link #withdrawPersona(Persona, double)} instead.
	 * @param p The Persona to manipulate
	 * @param amount The amount of money to modify the Persona account by
	 */
	void depositPersona(Persona p, double amount);
	
	/**
	 * Take money from a Persona. Negative amounts possible, but consider using {@link #depositPersona(Persona, double)} instead.
	 * @param p The Persona to manipulate
	 * @param amount The amount of money to modify the Persona account by
	 */
	void withdrawPersona(Persona p, double amount);
	
	/**
	 * Set a Persona's money balance to the provided amount
	 * @param p The Persona to manipulate
	 * @param amount The amount of money to set this Persona's account to.
	 */
	void setPersona(Persona p, double amount);
	
	/**
	 * @return Singular form of the config-set currency name
	 */
	String currencyNameSingular();
	
	/**
	 @return Plural form of the config-set currency name
	 */
	String currencyNamePlural();
	
	/**
	 * @return What fraction of total money is taken when the Persona dies
	 */
	double getFractionLostOnDeath();
	
	/**
	 * @return The amount of Minas a new Players' first Persona is gifted on creation 
	 */
	double getBeginnerAllowance();
	
	/**
	 * @return Whether or not usage of /money pay requires the receiving player to be nearby 
	 */
	boolean requirePaymentProximity();
	
}
