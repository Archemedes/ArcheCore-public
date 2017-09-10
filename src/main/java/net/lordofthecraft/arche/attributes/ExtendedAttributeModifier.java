package net.lordofthecraft.arche.attributes;

import java.util.UUID;

import org.bukkit.attribute.AttributeModifier;

//Slightly more data for temp atts, which is not serialized through the bukkit method
//Note that by default this is a permanent attribute
public class ExtendedAttributeModifier extends AttributeModifier {
	public boolean lostOnDeath = false;
	public boolean decayWhileOffline = true;
	public boolean isFromItem = false;
	
	public ExtendedAttributeModifier(AttributeModifier other) {
		super(other.getUniqueId(), other.getName(), other.getAmount(), other.getOperation());
	}
	
	public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation) {
		super(uuid, name, amount, operation);
	}
	
	public boolean isLostOnDeath() {
		return lostOnDeath;
	}
	
	public boolean willDecayWhileOffline() {
		return decayWhileOffline;
	}
	
	public void setLostOnDeath(boolean lost) {
		lostOnDeath = lost;
	}
	
	public void setDecayWhileOffline(boolean decay) {
		decayWhileOffline = decay;
	}

}
