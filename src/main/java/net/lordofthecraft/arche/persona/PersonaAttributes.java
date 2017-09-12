package net.lordofthecraft.arche.persona;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.attributes.*;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.attribute.ArcheAttributeInsertTask;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Just fucking end me - Sporadic 2k17
 */
public class PersonaAttributes {
    //N.B: attributes stored here are such for the reason that they need to be saved to SQL
    //Attribute mods due to items should not be kept within this class
    //Attribute mods due to racial bonuses should not be kept in this class
	
    private final Persona persona;
    
    //Vanilla attributes can be persona-bound for various reasons, although vanilla mods shouldn't be in this map
    //For example, it is superfluous to keep the player sprinting movement speed boost in here
    private final Map<ArcheAttribute, ArcheAttributeInstance> customAttributes = Maps.newIdentityHashMap();

	private Player player;
    
    PersonaAttributes(Persona persona) {
        this.persona = persona;
    }
    
    
    public ArcheAttributeInstance getInstance(ArcheAttribute a) {
    	return customAttributes.get(a);
    }
    
    public Collection<ArcheAttribute> getExistingInstances(){
    	return new ArrayList<ArcheAttribute>(customAttributes.keySet());
    }
    
    public double getAttributeValue(ArcheAttribute a) {
    	if( a instanceof VanillaAttribute) {
    		Attribute ax = ((VanillaAttribute) a).getHandle();
    		player = persona.getPlayer();
    		return player.getAttribute(ax).getValue();
    	} else {
    		ArcheAttributeInstance instance = getInstance(a);
    		if(instance == null) return a.getDefaultValue();
    		else return instance.getValue();
    	}
    }
    
    public void addModifier(Attribute a, AttributeModifier m) {
        Optional<VanillaAttribute> att = AttributeRegistry.getInstance().getVanillaAttribute(a);
        if (!att.isPresent()) {
            return;
        }
        addModifier(att.get(), m);
    }
    
    public void addModifier(ArcheAttribute a, AttributeModifier m) {
    	ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
    	String timerWhy = null;
    	if(timer != null) {
    		timerWhy = String.format("adding attribute to %s (%d)", persona.getName(), persona.getPersonaId());
    		timer.startTiming(timerWhy);
    	}
    	
    	ArcheAttributeInstance inst = null;
    	if(!customAttributes.containsKey(a)) {
    		inst = new ArcheAttributeInstance(a, persona.getPersonaKey());
    		customAttributes.put(a, inst);
    	} else {
    		inst = customAttributes.get(a);
    	}
    	
    	if(inst.hasModifier(m)) {
    		//We remove it because the values for this modifier may have changed
    		inst.removeModifier(m);
    	}	
    	
    	inst.addModifier(m);
		a.tryApply(inst);
        if (m instanceof ExtendedAttributeModifier) {
            SaveHandler.getInstance().put(new ArcheAttributeInsertTask((ExtendedAttributeModifier) m));
        }
        //SaveHandler.getInstance().put(new ArcheAttributeInsertTask(m));
        if(timer != null) timer.stopTiming(timerWhy);
    }

    void addModifierFromSQL(ArcheAttribute a, AttributeModifier m) {
        ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
        String timerWhy = null;
        if (timer != null) {
            timerWhy = String.format("adding attribute to %s (%d)", persona.getName(), persona.getPersonaId());
            timer.startTiming(timerWhy);
        }

        ArcheAttributeInstance inst = null;
        if (!customAttributes.containsKey(a)) {
            inst = new ArcheAttributeInstance(a, persona.getPersonaKey());
            customAttributes.put(a, inst);
        } else {
            inst = customAttributes.get(a);
        }

        if (inst.hasModifier(m)) {
            //We remove it because the values for this modifier may have changed
            inst.removeModifier(m);
        }

        inst.addModifier(m);
        a.tryApply(inst);

        if (timer != null) timer.stopTiming(timerWhy);
    }
    
    public void removeModifier(Attribute a, AttributeModifier m) {
        Optional<VanillaAttribute> att = AttributeRegistry.getInstance().getVanillaAttribute(a);
        if (!att.isPresent()) {
            return;
        }
        removeModifier(att.get(), m);
    }
    
    public void removeModifier(ArcheAttribute a, AttributeModifier m) {
    	ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
    	String timerWhy = null;
    	if(timer != null) {
    		timerWhy = String.format("removing attribute from %s (%d)", persona.getName(), persona.getPersonaId());
    		timer.startTiming(timerWhy);
    	}
    	
    	if(customAttributes.containsKey(a)) {
    		ArcheAttributeInstance inst = customAttributes.get(a);
    		if(inst.hasModifier(m)) {
    			inst.removeModifier(m);
    			
    			//Logic here is somewhat dodgy
    			//The removed att obviously needs to be removed from the handle
    			//But we can't for sure say a vanilla modifier not in our ArcheAttributeInstance
    			//is in fact not supposed to be there, as those may be regulated beyond our personas
    			if(a instanceof VanillaAttribute) {
    				VanillaAttribute v = (VanillaAttribute) a;
    				if(persona.isCurrent() && persona.getPlayer() != null) {
    					persona.getPlayer().getAttribute(v.getHandle()).removeModifier(m);
    				}
    			} else { //Just recalibrate if needed for this attribute
    				a.tryApply(inst);
    			}
    			
    			if(inst.getModifiers().isEmpty()) {
    				customAttributes.remove(a);
    			}
    		}
    		
    	} //Else this modifier does not exist anyway
    	
    	if(timer != null) timer.stopTiming(timerWhy);
    }
    
    public void handleLogin() {
		for(Entry<ArcheAttribute, ArcheAttributeInstance> entry : customAttributes.entrySet()) {
			ArcheAttribute aa = entry.getKey();
			aa.tryApply(entry.getValue());
		}
    }

    public void handleSwitch(boolean logoff) {
    	//Logoff logic:
    	//true: player left server or ArcheCore plugin was disabled
    	//false: persona is being switched to or from.
    	
		for(Entry<ArcheAttribute, ArcheAttributeInstance> entry : customAttributes.entrySet()) {
			ArcheAttribute aa = entry.getKey();
			ArcheAttributeInstance aai = entry.getValue();
			
			aai.getModifiers().stream()
			.map(ExtendedAttributeModifier.class::cast)
			.forEach(m -> {if(logoff) m.handleLogoff(); else m.handleSwitch(aa, persona);} );
			
			if(aa instanceof VanillaAttribute) {
				if(logoff || !persona.isCurrent()) deactivateVanilla((VanillaAttribute) aa);
				else aa.tryApply(aai);
			} 
		}
    }
    
    private void deactivateVanilla(VanillaAttribute va) {
    	Player p = persona.getPlayer();
    	if(p != null) {
    		ArcheAttributeInstance aai = customAttributes.get(va);
    		AttributeInstance ai = p.getAttribute(va.getHandle());
            aai.getModifiers().forEach(ai::removeModifier);
        }
    }
    
}