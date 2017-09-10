package net.lordofthecraft.arche.attributes;

import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.interfaces.Persona;

public class ArcheAttribute {
	public static final ArcheAttribute MAX_FATIGUE = new ArcheAttribute("Maximum Fatigue",100.0);
	public static final ArcheAttribute FATIGUE_GAIN = new ArcheAttribute("Fatigue Gained",100.0);
	public static final ArcheAttribute EXHAUSTION = new ArcheAttribute("Exhaustion", 0);
	
	private static final Map<Attribute, VanillaAttribute> VANILLA = Maps.newEnumMap(Attribute.class);
	
	static {
		for (Attribute a : Attribute.values()) {
			VANILLA.put(a, new VanillaAttribute(a.toString(), 0.0, a));
		}
	}
	
	public static ArcheAttribute getFromVanilla(Attribute a) {
		return VANILLA.get(a); 
	}
	
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
	
	//This class is meant to be overriden
	//If the player is online, the value, derived from current Persona's att instance, is used
	//For any arbitrary actions
	public void calibrate(Player p, double value) {
		
	}
}
