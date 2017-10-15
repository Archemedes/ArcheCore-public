package net.lordofthecraft.arche;

import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.FatigueHandler;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArcheFatigueHandler implements FatigueHandler {
	int fatigueDecreaseHours = 24; //Will be set by ArcheCore onEnable
	private static final ArcheFatigueHandler INSTANCE = new ArcheFatigueHandler();
	
	private final Set<PersonaKey> prompted = new HashSet<>();
	
	private ArcheFatigueHandler() {}
	public static ArcheFatigueHandler getInstance() { return INSTANCE; }
	
	
	@Override
	public void addFatigue(Persona pers, double add) {
		if(fatigueDecreaseHours == 0) return;
		double fat = pers.getFatigue();

        fat = Math.min(pers.attributes().getAttributeValue(AttributeRegistry.MAX_FATIGUE), fat + add);
        pers.setFatigue(fat);
	}

	@Override
	public void reduceFatigue(Persona pers, double remove) {
		if(fatigueDecreaseHours == 0) return;
		
		double fat = pers.getFatigue();
		fat = Math.max(0.0, fat - remove);
		pers.setFatigue(fat);
	}

	@Override
	public void setFatigue(Persona pers, double fatigue) {
		if(fatigueDecreaseHours == 0) return;
		
		double toSet = fatigue;
		if(fatigue < 0) toSet = 0.0;		
        else {
        	double maxFat = getMaxFatigue(pers);
        	if (fatigue > maxFat) toSet = maxFat;
        }
		
        if(toSet != fatigue) pers.setFatigue(toSet);
	}

	@Override
	public void showFatigueBar(Player p) {
		Persona pers = ArchePersonaHandler.getInstance().getPersona(p);
		if(pers != null) showFatigueBar(p, pers);
	}
	
	@Override
	public void showFatigueBar(Persona pers) {
		Player p = pers.getPlayer();
		if(p != null) showFatigueBar(p, pers);
	}
	
	@Override
	public void showFatigueBar(Player p, Persona pers) {
		if(fatigueDecreaseHours == 0) return;
		double max = getMaxFatigue(pers);
        float factor = 1.0f - ((float) (pers.getFatigue() / getMaxFatigue(pers)));
        
        p.setExp(factor);
		p.setLevel((int) (max - pers.getFatigue())); 
	}

	@Override
	public double getFatigue(Persona pers) {
		return pers.getFatigue();
	}

	@Override
	public boolean hasEnoughEnergy(Persona pers, double needed) {
		if(fatigueDecreaseHours == 0) return true;
		
		double curr = pers.getFatigue();
        return curr + needed <= getMaxFatigue(pers);
    }
	
	@Override
	public boolean handleFatigue(final Persona pers, double add, Skill skill) {
		if(fatigueDecreaseHours == 0) return true;
		
		ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
		if(timer != null) timer.startTiming("fatigue_" + pers.getName());
		
		add = modifyNetFatigue(pers, add, skill);
		
		Player p = pers.getPlayer();
		boolean able = hasEnoughEnergy(pers, add);
		if(able) {
            double newFatigue = Math.min(getMaxFatigue(pers), add + pers.getFatigue());
            pers.setFatigue(newFatigue);
			if(p!= null) showFatigueBar(p, pers);
		} else if(p != null && !prompted.contains(pers.getPersonaKey())) {
			prompted.add(pers.getPersonaKey());
			Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->prompted.remove(pers.getPersonaKey()), 3*60*20);
			p.sendMessage(FatigueHandler.NO_FATIGUE_MESSAGE);
		}
		
		if(timer != null) timer.stopTiming("fatigue_" + pers.getName());
		return able;
	}
	
	@Override
	public double modifyNetFatigue(Persona mlk, double value, Skill... racist) {
		if(value <= 0 ) return value;
		
		if(racist.length > 0) {
			Race poc = mlk.getRace();
			double bestMod = -1;
			for(Skill skill : racist) {
				Map<Race, Double> affirmitiveAction = skill.getRaceMods();
				if(affirmitiveAction.containsKey(poc))
					bestMod = Math.max(affirmitiveAction.get(poc), bestMod);
			}
			
			if(bestMod > 0) {
				value /= bestMod;
			}
		}
		
		value *= mlk.attributes().getAttributeValue(AttributeRegistry.FATIGUE_GAIN);
		
		return value;
	}

	private double getMaxFatigue(Persona p) {
        return p.attributes().getAttributeValue(AttributeRegistry.MAX_FATIGUE);
    }
}
