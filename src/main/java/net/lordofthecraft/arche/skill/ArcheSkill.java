package net.lordofthecraft.arche.skill;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.enums.SkillTier;
import net.lordofthecraft.arche.event.GainXPEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.SkillAttachment;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ArcheSkill implements Skill {
	
	private static ArcheTimer timer;
	
	private final int id;
	private final String name;
	private final int displayStrategy;
	private final boolean inert;
	
	private final Set<Race> mains;
	private final Map<Race, Double> raceMods;

	private final PreparedStatement statement;

	ArcheSkill(int id, String name, int displayStrategy, boolean inert,
			   Set<Race> mains, Map<Race, Double> raceMods, PreparedStatement state, boolean unlockedByTime) {
		
		timer = ArcheCore.getPlugin().getMethodTimer();
		
		this.id = id;
		this.name = name;
		this.displayStrategy = displayStrategy;
		this.inert = inert;
		this.mains =  mains;
		this.raceMods = raceMods;
		this.statement = state;
	}
	
	public PreparedStatement getUpdateStatement(){
		return statement;
	}
	
	@Override
	public boolean isProfessionFor(Race race){
		return mains.contains(race);
	}	
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public int getId(){
		return id;
	}
	
	@Override
	public int getVisibility(){
		return displayStrategy;
	}
	
	@Override
	public boolean isInert(){
		return inert;
	}
	
	@Override
	public boolean isVisible(Player p){
		return isVisible(getPersona(p));
	}

	@Override
	public boolean isVisible(Persona p){
		return getAttachment(p).isVisible();
	}
	
	@Override
	public boolean reveal(Persona p){
		boolean vis = isVisible(p);
		getAttachment(p).reveal();
		return vis;
	}
	
	@Override
	public double reset(Persona p){
		SkillAttachment att = getAttachment(p);
		double xp = att.getXp();
		if(xp > 0){
			att.removeXP(xp);
			return xp;
		} else {
			return 0;
		}
	}
	
	@Override
	public void addXp(Player p, double xp){
		addXp(getPersona(p), xp);
	}
	
	@Override
	public void addXp(Persona p, double xp){
		if(timer != null) timer.startTiming("xp_" + name);
		
		//Don't hand XP to people who turned it off
		if(!p.getXPGain()) return;
		
		//Also don't hand XP to people who cannot gain it in this skill
		if(!this.canGainXp(p)) return;
		
		//Add some XP to the skill in question
		addRawXp(p, xp);
		
		if(timer != null) timer.stopTiming("xp_" + name);
	}
	
	@Override
	public double addRawXp(Player p, double xp){
		return addRawXp(getPersona(p), xp);
	}
	
	@Override
	public double addRawXp(Persona p, double xp){
		SkillAttachment attach = getAttachment(p);

		GainXPEvent event = new GainXPEvent(p, this, xp); 
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return 0;
		
		xp = event.getAmountGained();
		double oldXp = attach.getXp();
		final SkillTier cap = SkillTier.values()[SkillTier.values().length-1];
		double newXp = Math.min(cap.getXp(), oldXp + xp);
		
		if(getVisibility() == VISIBILITY_DISCOVERABLE)
			attach.reveal();
		
		if(newXp == oldXp) return 0;
		attach.setXp(newXp);
		
		if(!attach.isVisible())
			return xp;
		
		//Level up? If so display message
		if(xp > 0 ){
			SkillTier current = getSkillTier(p);
			int treshold = current.getXp();
			if(oldXp < treshold && newXp >= treshold ){ //This XP gain made player pass treshold
				Player player = p.getPlayer();
				if(player != null){
					char n = this.getName().charAt(0);
					String an = (n == 'e' || n == 'o' || n == 'i' || n == 'a')? "an" : "a";
					player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "You have improved your skill as " + an + " " + ChatColor.AQUA + WordUtils.capitalize(this.getName()));
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
				}
			}
		}
		return xp;
	}

	@Override
	public double getXp(Player p){
		return getXp(getPersona(p));
	}

	@Override
	public double getXp(Persona p){
		SkillAttachment attach = getAttachment(p);
		return attach.getXp();
	}
	
	@Override
	public boolean achievedTier(Player p, SkillTier tier){
		return achievedTier(getPersona(p), tier);
	}

	@Override
	public boolean achievedTier(Persona p, SkillTier tier){
		SkillAttachment att = getAttachment(p);
		return att.getXp() >= tier.getXp();
	}

	@Override
	public SkillTier getSkillTier(Player p){
		return getSkillTier(getPersona(p));
	}

	@Override
	public SkillTier getSkillTier(Persona p){
		double xp = getXp(p);
		SkillTier result = SkillTier.DEFAULT;
		for(SkillTier st : SkillTier.values()){
			if(st.getXp() <= xp) result = st;
			else return result;
		}
		
		return result;
	}

	@Override
	public boolean canGainXp(Persona p){
		SkillAttachment att = getAttachment(p);
		return inert || att.isVisible() || getVisibility() == VISIBILITY_DISCOVERABLE;
	}

	@Override
	public Map<Race, Double> getRaceMods() {
		return Collections.unmodifiableMap(raceMods);
	}

	@Override
	public Set<Race> getMains() {
		return Collections.unmodifiableSet(mains);
	}

	private Persona getPersona(Player p){
		return ArchePersonaHandler.getInstance().getPersona(p);
	}
	
	private SkillAttachment getAttachment(Persona pers){
		SkillAttachment attach = ((ArchePersona) pers).getSkill(getId());
		
		if(!attach.isInitialized())
			attach.initialize();
		
		return attach;
	}
}
