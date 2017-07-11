package net.lordofthecraft.arche.skill;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.interfaces.SkillFactory;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.Race;
import net.lordofthecraft.arche.save.SaveExecutorManager;
import net.lordofthecraft.arche.save.tasks.SelectSkillTask;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;

public class ArcheSkillFactory implements SkillFactory {
	private static final Map<String, String> VALS;
	
	private static final Map<String, ArcheSkill> skills = Maps.newLinkedHashMap();
	static Set<ExpModifier> xpMods = EnumSet.noneOf(ExpModifier.class);
	private static int count = 0;
	
	static{
		Map<String, String> vals = Maps.newLinkedHashMap();
		vals.put("player", "TEXT");
		vals.put("id", "INT");
		vals.put("xp", "DOUBLE NOT NULL");
		vals.put("visible", "INT NOT NULL");
		//vals.put("FOREIGN KEY (player, id)", "REFERENCES persona(player, id) ON DELETE CASCADE");
		vals.put("UNIQUE (player, id)", "ON CONFLICT REPLACE");

		VALS = Collections.unmodifiableMap(vals);

	}

	private final String name;
	private final int id;
	private final Set<Race> mains = Sets.newConcurrentHashSet();
	private final Map<Race, Double> raceMods = Maps.newConcurrentMap();
	private int strategy = Skill.VISIBILITY_VISIBLE;
	private boolean inert = false;
	private boolean intensive = false;
	private boolean unlockedByTime = false;
	private String helpText = null;
	private Material helpIcon = null;
	private boolean forceRacials = false;

	private ArcheSkillFactory(String name) {
		this.name = name;
		this.id = count++;
	}
	
	public static void activateXpMod(ExpModifier mod){
		xpMods.add(mod);
	}
	
	/**
	 * Method to retrieve an existing Skill object
	 * @param name The skill you want to retrieve
	 * @return The skill associated with the given name, or null if not found
	 */
	public static Skill getSkill(String name){
		if(name == null ) return null;
		return skills.get(name.toLowerCase());
	}
	
	/**
	 * Retrieve all registered Skills in an unmodifiable map.
	 * @return All skills, with the skill names (in lowercase) as the Map Keys.
	 */
	public static Map<String, ArcheSkill> getSkills(){
		return Collections.unmodifiableMap(skills);
	}
	
	/**
	 * Method to create and register a new Skill with the default settings. This
	 * skill must not yet exist. Use {@link #getSkill(String)} to retrieve an already registered Skill
	 * @param name The name of the Skill to be created.
	 * @return the Created Skill
	 * @throws DuplicateSkillException If a skill with the same name already exists
	 */
	public static Skill createSkill(String name){
		return registerNewSkill(name).register();
	}
	
	/**
	 * Method to create a Skill with arbitrary settings, each of them initialised
	 * with the help of the SkillFactory object.
	 * @param name The name of the Skill to be created.
	 * @return the constructed SkillFactory object
	 * @throws DuplicateSkillException If a skill with the same name already exists
	 */
	public static SkillFactory registerNewSkill(String name){
		name = name.toLowerCase();

		Skill test = skills.get(name);
		if(test != null) throw new DuplicateSkillException("Skill " + name + " already exists");

		return new ArcheSkillFactory(name);
	}
	
	@Override
	public SkillFactory withVisibilityType(int visibility){
		this.strategy = visibility;
		return this;
	}
	
	@Override
	public SkillFactory withXpGainWhileHidden(boolean inert){
		this.inert = inert;
		return this;
	}
	
	@Override
	public SkillFactory asRacialProfession(Race race){
		mains.add(race);
		return this;
	}
	
	@Override
	public SkillFactory withRacialModifier(Race race, double modifier){
		raceMods.put(race, modifier);
		return this;
	}
	
	@Override
	public SkillFactory withHelpFile(String helpText, Material helpIcon){
		this.helpText = helpText;
		this.helpIcon = helpIcon;
		return this;
	}
	
	@Override
	public SkillFactory setIntensiveProfession(boolean intensive){
		this.intensive = intensive;
		return this;
	}

	@Override
	public SkillFactory setUnlockedByTime(boolean unlockByTime) {
		unlockedByTime = unlockByTime;
		return this;
	}

	public void setForceRacials(boolean forceRacials) {
		this.forceRacials = forceRacials;
	}

	@Override
	public Skill register(){
		
		
		try {
			SQLHandler handler = ArcheCore.getControls().getSQLHandler();
			Connection con = handler.getConnection();
			
			//Creates the underlying SQL table, if necessary
			PreparedStatement createStat = con.prepareStatement("INSERT IGNORE INTO skills VALUES (?,?)");
			createStat.setString(1, name);
			createStat.setInt(2, strategy);
			createStat.execute();
			createStat.close();

			/*ArcheCore.getPlugin().getSQLHandler().createTable("sk_" + name, VALS);*/
			
			//And the SQL statement to provide values to it
			PreparedStatement statement = con.prepareStatement("INSERT INTO persona_skills VALUES (?,?,?,?)");
			/*
			CREATE TABLE IF NOT EXISTS race_xp (
    race_key_fk 	VARCHAR(255),
    skill_fk 		VARCHAR(255),
    xp_mult 		DOUBLE(10,2) DEFAULT 1.0,
    racial_skill	BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (race_key_fk, skill_fk),
    FOREIGN KEY (race_key_fk) REFERENCES races (race_key),
    FOREIGN KEY (skill_fk) REFERENCES skills (skill)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

			 */
			if (!forceRacials) {
				PreparedStatement racialStat = con.prepareStatement("SELECT xp_mult,racial_skill FROM race_xp WHERE skill_fk=? AND race_key_fk=?");
				racialStat.setString(1, name);
				for (Race race : Race.getRaces()) {
					racialStat.setString(2, race.getRaceId());
					ResultSet rs = racialStat.executeQuery();
					while (rs.next()) {
						if (rs.getBoolean("racial_skill")) {
							mains.add(race);
						}
						raceMods.put(race, rs.getDouble("xp_mult"));
					}
					rs.close();
				}
				racialStat.close();
			}

			ArcheSkill skill = new ArcheSkill(id, name, strategy, inert, mains, raceMods, statement, intensive, unlockedByTime);
			
			//Make sure skill is registered for Plugins.
			skills.put(name, skill);
			
			//Create the Help File, if it was specified
			if(helpText != null && helpIcon != null)
				HelpDesk.getInstance().addSkillTopic(name, helpText, helpIcon);
			
			//Add the Skill in question to all currently logged in Personas
			//(if a skill gets added after server startup; shouldn't happen, but might)
			ArchePersonaHandler ph = ArchePersonaHandler.getInstance();
			for(ArchePersona[] prs : ph.getPersonas()){
				for(ArchePersona p : prs){
					if(p != null && p.isCurrent()){
						
						//Start loading this Persona's Skill data for this one particular skill
						SelectSkillTask task = new SelectSkillTask(p, skill);
						FutureTask<SkillData> fut = task.getFuture();
						SaveExecutorManager.getInstance().submit(task);
						
						p.addSkill(skill, fut);
						break;
					}
				}
			}
			
			//ArcheCore.getPlugin().getSktop().registerTop(skill);
			return skill;
			
		} catch (SQLException e) {
			ArcheCore.getPlugin().getLogger().severe("Error while preparing SQL statement for Skill xp gains");
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static class DuplicateSkillException extends RuntimeException{
		private static final long serialVersionUID = -6769690779325926399L;

		private DuplicateSkillException() {
			super();
		}

		private DuplicateSkillException(String message) {
			super(message);
		}
	}
	
}
