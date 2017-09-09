package net.lordofthecraft.arche.persona;

import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ArcheAttributeInstance;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.attributes.VanillaAttributeInstance;
import net.lordofthecraft.arche.interfaces.Persona;

/**
 * 
 */
public class PersonaAttributes {
    //N.B: attributes stored here are such for the reason that they need to be saved to SQL
    //Attribute mods due to items should not be kept within this class
    //Attribute mods due to racial bonuses should not be kept in this class
	
    private final Persona persona;
    
    private final Map<ArcheAttribute, ArcheAttributeInstance> customAttributes = Maps.newIdentityHashMap();
    //Vanilla attributes can be persona-bound for various reasons, although vanilla mods shoudn't be in this map
    //For example, it is superfluous to keep the player sprinting movement speed boost in here
    private final Map<Attribute, AttributeInstance> vanillaAttributes = Maps.newEnumMap(Attribute.class);

    PersonaAttributes(Persona persona) {
        this.persona = persona;
    }
    
    
    public void addModifier(Attribute a, AttributeModifier m) {
    	if( !(m instanceof ExtendedAttributeModifier)) {
    		m = new ExtendedAttributeModifier(m);
    	}
    	
    	AttributeInstance inst = null;
    	if(!vanillaAttributes.containsKey(a)) {
    		inst = new VanillaAttributeInstance();
    		vanillaAttributes.put(a, inst);
    	} else {
    		
    	}
    }
    
    public void addModifier(ArcheAttribute a, AttributeModifier m) {
    	if( !(m instanceof ExtendedAttributeModifier)) {
    		m = new ExtendedAttributeModifier(m);
    	}
    	
    	ArcheAttributeInstance inst = null;
    	if(!customAttributes.containsKey(a)) {
    		inst = new ArcheAttributeInstance(a, persona.getPersonaKey());
    		customAttributes.put(a, inst);
    	} else {
    		inst = customAttributes.get(a);
    	}
    	
    	if(inst.hasModifier(m)) {
    		inst.addModifier(m);
    		if(a.isVanilla()) {
    			Persona p = inst.getPersona().getPersona();
    			if(p != null) {
    				Player player = p.getPlayer();
    				if(player != null) {
    					p.getA
    				}
    			}
    		} else {
    			a.tryApply(inst);
    		}
    	} 
    	
    	
    }
    
}