package net.lordofthecraft.arche.persona;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.attributes.*;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.MessageUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
        if (!customAttributes.containsKey(a)) {
        	ArcheAttributeInstance inst = new ArcheAttributeInstance(a, persona.getPersonaKey());
            customAttributes.put(a, inst);
            return inst;
        } else {
        	return customAttributes.get(a);
        }
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
        VanillaAttribute att = AttributeRegistry.getInstance().getVanillaAttribute(a);
        addModifier(att, m);
    }

    public void addModifier(ArcheAttribute a, AttributeModifier m) {
        addModifier(a, m, false);
    }

    public void addModifier(ArcheAttribute a, AttributeModifier m, boolean force) {
        ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
        String timerWhy = null;
        if(timer != null) {
            Logger logger = ArcheCore.getPlugin().getLogger();
            logger.info("[Debug] Adding attribute: " + toString(m));
            logger.info("[Debug] Current attributes for " + a.getName() + ":");
            if(customAttributes.containsKey(a)) {
                customAttributes.get(a).getModifiers()
                        .forEach(x-> logger.info("[Debug] " + toString(x)));
            } else {
                logger.info("[Debug] NONE!");
            }

            timerWhy = "adding attribute to " + MessageUtil.identifyPersona(persona);
            timer.startTiming(timerWhy);
        }

        ArcheAttributeInstance inst = getInstance(a);

        if(inst.hasModifier(m)) {
            //We remove it because the values for this modifier may have changed
            inst.removeModifier(m);
        }

        inst.addModifier(m, force);
        a.tryApply(inst);

        if(timer != null) timer.stopTiming(timerWhy);
    }
    
    public void removeModifier(Attribute a, AttributeModifier m) {
        VanillaAttribute att = AttributeRegistry.getInstance().getVanillaAttribute(a);
        removeModifier(att, m);
    }
    
    public void removeModifier(ArcheAttribute a, AttributeModifier m) {
    	ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
    	String timerWhy = null;
    	if(timer != null) {
    		Logger logger = ArcheCore.getPlugin().getLogger();
    		logger.info("[Debug] Removing attribute: " + toString(m));
    		logger.info("[Debug] Current attributes for " + a.getName() + ":");
    		if(customAttributes.containsKey(a)) {
    			customAttributes.get(a).getModifiers()
    			.forEach(x-> logger.info("[Debug] " + toString(x)));
    		} else {
    			logger.info("[Debug] NONE!");
    		}
    		
    		timerWhy = String.format("removing attribute from " + MessageUtil.identifyPersona(persona));
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
    	customAttributes.entrySet().forEach(e-> e.getKey().tryApply(e.getValue()));
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
                    .forEach(m -> {
                        if (logoff) m.handleLogoff(persona, aa);
                        else m.handleSwitch(aa, persona);
                    });

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
    
    String toString(AttributeModifier m) {
    	String base = "ATTMOD uuid:" + m.getUniqueId().toString().substring(0, 8) + " o:" + m.getOperation().ordinal() + ". a:" + m.getAmount() + " n:\"" + m.getName()  + "\"";
    	if(m instanceof ExtendedAttributeModifier) {
    		ExtendedAttributeModifier eam = (ExtendedAttributeModifier) m;
    		base += " save:" + (eam.save? 'y' : 'n');
    	}
    	
    	return base;
    }
    
}