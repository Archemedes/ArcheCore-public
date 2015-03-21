package net.lordofthecraft.arche.skill;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Set;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.enums.ProfessionSlot;
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

public class ArcheSkill implements Skill {
	
	private static Set<ExpModifier> xpMods = ArcheSkillFactory.xpMods;
	private static ArcheTimer timer;
	
	private final int id;
	private final String name;
	private final int displayStrategy;
	private final boolean inert;
	
	private final Set<Race> mains;
	private final Map<Race, Double> raceMods;
	
	private final boolean intensive;
	
	private final PreparedStatement statement;
	
	ArcheSkill(int id, String name, int displayStrategy, boolean inert, 
			Set<Race> mains, Map<Race,Double> raceMods, PreparedStatement state, boolean intensive){
		
		timer = ArcheCore.getPlugin().getMethodTimer();
		
		this.id = id;
		this.name = name;
		this.displayStrategy = displayStrategy;
		this.inert = inert;
		this.mains =  mains;
		this.raceMods = raceMods;
		this.statement = state;
		this.intensive = intensive;
	}
	
	public PreparedStatement getUpdateStatement(){
		return statement;
	}
	
	@Override
	public boolean isProfessionFor(Race race){
		return mains.contains(race);
	}
	
	@Override
	public boolean isIntensiveProfession(){
		return intensive;
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
		
		/*//Deduce XP from other skills
		if( !inert && xp > 0){ //Inert skills do not cause Personas to forget other skills
			
			Collection<ArcheSkill> all = ArcheSkillFactory.getSkills().values();
			double deduct = -xp / all.size();
			
			for(Skill s : all){
				if(s != this && s.canForget(p)){ //Deduce from all other applicable skills
					s.addRawXp(p, deduct, false);
				}
			}	
		}*/
		
		if(timer != null) timer.stopTiming("xp_" + name);
	}
	
	@Override
	public void addRawXp(Player p, double xp){
		addRawXp(getPersona(p), xp);
	}
	
	@Override
	public void addRawXp(Player p, double xp, boolean modify){
		addRawXp(getPersona(p), xp, modify);
	}
	
	@Override
	public void addRawXp(Persona p, double xp){
		addRawXp(p, xp, true);
	}
	
	@Override
	public void addRawXp(Persona p, double xp, boolean modify){
		SkillAttachment attach = getAttachment(p);
		
		if(modify){ //Add xp modifiers
				double mod = getXPModifier(p, attach);
				if(mod > 0) xp *=  xp > 0?  mod : 1/mod; 
		}
		
		GainXPEvent event = new GainXPEvent(p, this, xp); 
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		
		xp = event.getAmountGained();
		
		double oldXp = attach.getXp();
		final SkillTier cap = this.getCapTier(p);
		double newXp = Math.min(cap.getXp(), oldXp + xp);
		
		if(getVisibility() == VISIBILITY_DISCOVERABLE)
			attach.reveal();
		
		if(newXp == oldXp) return;
		attach.setXp(newXp);
		
		if(!attach.isVisible())
			return;
		
		Player player = p.getPlayer();
		if(player != null){
			SkillTier now = this.getSkillTier(p);
			
			if(now != SkillTier.RUSTY && !(now == SkillTier.INEXPERIENCED && newXp < 0)){
				int nxp = now == SkillTier.INEXPERIENCED? 0 : now.getXp();
				player.setExp(now == cap? 0f : (float) ( (newXp - nxp) / (now.getNext().getXp() - nxp) ));
				player.setLevel(now == cap? 0 : (int) (now.getNext().getXp() - newXp));
			}
		}
		
		//Level up? If so display message
		if(xp > 0 ){
			SkillTier current = getSkillTier(p);
			int treshold = current.getXp();
			if(oldXp < treshold && newXp >= treshold ){ //This XP gain made player pass treshold
				
				if(player != null){
					char n = this.getName().charAt(0);
					String an = (n == 'e' || n == 'o' || n == 'i' || n == 'a')? "an" : "a";
					player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "You have improved your skill as " + an + " " + ChatColor.AQUA + WordUtils.capitalize(this.getName()));
					player.playSound(player.getLocation(), Sound.LEVEL_UP, 3f, 1f);
				}
			}
		}
	}
	
	private double getXPModifier(Persona p, SkillAttachment att){
		if(xpMods.isEmpty()) return 1;
		
		double mod = att.getModifier(); 
		if(mod > 0) return mod; //Cached value
		
		
		Race r = p.getRace();
		
		mod = xpMods.contains(ExpModifier.RACIAL)? (raceMods.containsKey(r)? raceMods.get(p.getRace()) : r.getBaseXpMultiplier()) : 1 ;
		
		if(xpMods.contains(ExpModifier.PLAYTIME)){
			int mins = p.getTimePlayed();
			if(mins >= 1200){ 
				double f = 0.05 * (mins / 12000 + Math.pow(mins, 1.5) / 700000);
				double dmod = Math.min(1, f);
				mod += dmod;
			}
		}
		
		if(xpMods.contains(ExpModifier.AUTOAGE)){
			if(p.doesAutoAge()){
				if(r == Race.HUMAN || r == Race.HALFLING || r == Race.NORTHENER || r == Race.SOUTHERON || r == Race.HEARTLANDER)
					mod += 0.10;
			}
		}

		att.setModifier(mod);
		return mod;
	}
	
	@Override
	public SkillTier getCapTier(Persona p){
		if(this.isInert()) return SkillTier.AENGULIC;
		
		if(this.isProfessionFor(p.getRace()) || p.getTimePlayed() > 2000*60) return SkillTier.AENGULIC;
		
		for(ProfessionSlot slot : ProfessionSlot.values()){
			if(p.getProfession(slot) == this) return SkillTier.AENGULIC;
		}
		
		SkillTier t = p.getProfession(ProfessionSlot.PRIMARY) == null? SkillTier.ADEQUATE :
			p.getProfession(ProfessionSlot.SECONDARY) == null && !p.getProfession(ProfessionSlot.PRIMARY).isIntensiveProfession()? SkillTier.CLUMSY : SkillTier.RUSTY;
		
		Race r = p.getRace();
		if(t != SkillTier.RUSTY && (r == Race.HUMAN || r == Race.NORTHENER || r == Race.SOUTHERON || r == Race.HEARTLANDER))
			return t.getNext().getNext();
		else return t;
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
		SkillTier result = SkillTier.RUSTY;
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
