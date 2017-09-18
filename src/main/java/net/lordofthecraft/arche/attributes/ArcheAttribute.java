package net.lordofthecraft.arche.attributes;

import com.google.common.base.Objects;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.entity.Player;

public class ArcheAttribute {
	
	private final String name;
	private final double defaultValue;

	public ArcheAttribute(String name, double defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}
	
	
	public double getDefaultValue() {
		return defaultValue;
	}
	
	public void tryApply(ArcheAttributeInstance instance) {
		if(this.getClass() == ArcheAttribute.class) return; //200-IQ optimization it's OK to be impressed
		Persona p = instance.getPersona().getPersona();
		if(p != null && p.isCurrent()) {
			Player player = p.getPlayer();
			if(player != null) {
				double value = instance.getValue();
				calibrate(player, value);
			}
		}
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArcheAttribute)) return false;
        ArcheAttribute that = (ArcheAttribute) o;
        return Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    //This class is meant to be overriden
    //If the player is online, the value, derived from current Persona's att instance, is used
	//For any arbitrary actions
	public void calibrate(Player p, double value) {
		
	}
}
