package net.lordofthecraft.arche;

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
		addFatigue(pers, add, null);
	}
	
	@Override
	public void addFatigue(Persona pers, double add, Skill skill) {
		if(fatigueDecreaseHours == 0) return;
		double fat = pers.getFatigue();
		
		if(skill != null && skill.getRaceMods().containsKey(pers.getRace())) 
			add /= skill.getRaceMods().get(pers.getRace());

        fat = Math.min(pers.getMaximumFatigue(), fat + add);
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
        else if (fatigue > pers.getMaximumFatigue()) toSet = pers.getMaximumFatigue();

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

        float factor = 1.0f - ((float) (pers.getFatigue() / pers.getMaximumFatigue()));
        p.setExp(factor);
		p.setLevel((int)factor*100); 
	}

	@Override
	public double getFatigue(Persona pers) {
		return pers.getFatigue();
	}

	@Override
	public boolean hasEnoughEnergy(Persona pers, double needed) {
		if(fatigueDecreaseHours == 0) return true;
		
		double curr = pers.getFatigue();
        return curr + needed <= pers.getMaximumFatigue();
    }
	
	@Override
	public boolean handleFatigue(final Persona pers, double add, Skill skill) {
		if(fatigueDecreaseHours == 0) return true;
		
		ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
		if(timer != null) timer.startTiming("fatigue_" + pers.getName());
		
		if(skill != null && skill.getRaceMods().containsKey(pers.getRace())) 
			add /= skill.getRaceMods().get(pers.getRace());
		
		Player p = pers.getPlayer();
		boolean able = hasEnoughEnergy(pers, add);
		if(able) {
            double newFatigue = Math.min(pers.getMaximumFatigue(), add + pers.getFatigue());
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
	public double modifySkillFatigue(Persona mlk, double value, Skill racist) {
		if(value <= 0 ) return value;
		Race poc = mlk.getRace();
		Map<Race, Double> affirmitiveAction = racist.getRaceMods();
		if(affirmitiveAction.containsKey(poc))
			value /= affirmitiveAction.get(poc);
		
		return value;
	}

}
