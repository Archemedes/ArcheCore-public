package net.lordofthecraft.arche.skill;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.interfaces.SkillFactory;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ArcheSkillFactory implements SkillFactory {
	private static final Map<String, String> VALS;
	
	private static final Map<String, ArcheSkill> skills = Maps.newLinkedHashMap();

    /**
     * This method fetches all skills that are in SQL and establishes them early.
     * Any changes made to skill that are preloaded <b>will not</b> be effective unless FORCE = true on {@link #register()}
     *
     * @see #withForceUpdate(boolean) For setting a skill to force update
     */
    public static void preloadSkills(SQLHandler handler) {
        /*
	    CREATE TABLE IF NOT EXISTS skills (
        skill_id    VARCHAR(255),
        hidden 		INT DEFAULT 0,
        help_text   TEXT DEFAULT 'UNSET',
        help_icon   TEXT DEFAULT 'BARRIER',
        PRIMARY KEY (skill_id)
        )
        ENGINE=InnoDB DEFAULT CHARSET=utf8;
	     */

	    /*
	    CREATE TABLE IF NOT EXISTS skill_races (
        skill_id_fk     VARCHAR(255),
        race            TEXT NOT NULL,
        racial_skill    BOOLEAN DEFAULT FALSE,
        racial_mod      DOUBLE DEFAULT 1.0,
        PRIMARY KEY (skill_id_fk,race),
        FOREIGN KEY (skill_id_fk) REFERENCES skills (skill_id) ON UPDATE CASCADE
        )
        ENGINE=InnoDB DEFAULT CHARSET=utf8;
	     */
        if (!skills.isEmpty()) {
            return;
        }
        try {
            Connection fetcherConn = handler.getConnection();

            PreparedStatement fetcher = fetcherConn.prepareStatement("SELECT skill_id,hidden,help_text,help_icon,inert,male_name,female_name FROM skills");
            PreparedStatement racialFetch = fetcherConn.prepareStatement("SELECT race,racial_skill,racial_mod FROM skill_races WHERE skill_id_fk=?");

            ResultSet rs = fetcher.executeQuery();
            while (rs.next()) {
                String name = rs.getString("skill_id");
                int hidden = rs.getInt("hidden");
                String text = rs.getString("help_text");
                String helpIconInfo = rs.getString("help_icon");
                boolean inert = rs.getBoolean("inert");
                String maleName = rs.getString("male_name");
                String femaleName = rs.getString("female_name");
                SkillFactory factory = ArcheSkillFactory.registerNewSkill(name, ArcheCore.getPlugin())
                        .withFemaleProfessionalName(femaleName)
                        .withVisibilityType(hidden)
                        .withXpGainWhileHidden(inert);
                if (helpIconInfo != null && text != null) {
                    factory.withHelpFile(text, Material.valueOf(helpIconInfo));
                }

                racialFetch.setString(1, name);
                ResultSet rrs = racialFetch.executeQuery();
                racialFetch.clearParameters();
                while (rrs.next()) {
                    Race race = Race.valueOf(rrs.getString("race"));
                    if (rrs.getBoolean("racial_skill")) {
                        factory.asRacialProfession(race);
                    }
                    factory.withRacialModifier(race, rrs.getDouble("racial_mod"));
                }
                rrs.close();
                ((ArcheSkillFactory) factory).internal = true;
                factory.register();
            }
            rs.close();
            fetcher.close();
            racialFetch.close();
            if (handler instanceof WhySQLHandler) {
                fetcherConn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
	private String maleName, femaleName;
	private final Set<Race> mains = EnumSet.noneOf(Race.class);
	private final Map<Race, Double> raceMods = new EnumMap<>(Race.class);
	private int strategy = Skill.VISIBILITY_VISIBLE;
	private boolean inert = false;
	private String helpText = null;
	private Material helpIcon = null;
    private boolean force = false;
    private final Plugin controller;
    //This value is here to flag
    private boolean internal = false;

    private ArcheSkillFactory(String name, Plugin controller) {
        this.name = name;
        this.controller = controller;
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
    public static Skill createSkill(String name, Plugin controller) {
        if (getSkill(name) != null) {
            return getSkill(name);
        }
        return registerNewSkill(name, controller).register();
    }
	
	/**
	 * Method to create a Skill with arbitrary settings, each of them initialised
	 * with the help of the SkillFactory object.
	 * @param name The name of the Skill to be created.
	 * @return the constructed SkillFactory object
	 * @throws DuplicateSkillException If a skill with the same name already exists
     */
    public static SkillFactory registerNewSkill(String name, Plugin controller){
        name = name.toLowerCase();

        //This is commented out.
        //DuplicateException will be fired under other circumstances.
        /*Skill test = skills.get(name);
		if(test != null) {

		    throw new DuplicateSkillException("Skill " + name + " already exists");
        }*/

        return new ArcheSkillFactory(name, controller);
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
    public SkillFactory withForceUpdate(boolean force) {
        this.force = force;
        return this;
    }

	@Override
	public Skill register() {
        if (skills.containsKey(name) && !force) {
            ArcheSkill skill = skills.get(name);
            if (!(controller instanceof ArcheCore)) {
                if (!skill.hasController()) {
                    skill.setControllingPlugin(controller);
                } else {
                    return skill;
                }
            }
            return skill;
        }
        try {
            //While potentially internal could be replaced with !(controller instanceof ArcheCore)
            //I worry that jistuma-like programmers will decide to put ArcheCore in for their controller.
            //Better safe than sorry. -501
            if ((skills.containsKey(name) && force) || (!internal && !skills.containsKey(name))) {
                SQLHandler handler = ArcheCore.getControls().getSQLHandler();
                Connection con = handler.getConnection();

                PreparedStatement insertSkillStatement = con.prepareStatement("INSERT " + (handler instanceof ArcheSQLiteHandler ? "OR" : "") + " IGNORE INTO skills(skill_id,hidden,help_text,help_text,help_icon,male_name,female_name) VALUES (?,?,?,?,?,?)");
                PreparedStatement insertRacialSkillData = con.prepareStatement("INSERT " + (handler instanceof ArcheSQLiteHandler ? "OR" : "") + " IGNORE INTO skill_races(skill_id_fk,race,racial_skill,");

                /* skill_id,hidden,help_text,help_text,help_icon,male_name,female_name */
                insertSkillStatement.setString(1, name);
                insertSkillStatement.setInt(2, strategy);
                insertSkillStatement.setString(3, helpText);
                insertSkillStatement.setString(4, helpIcon.name());
                insertSkillStatement.setString(5, maleName);
                insertSkillStatement.setString(6, femaleName);
                insertSkillStatement.executeQuery();
                insertSkillStatement.clearParameters();
                //TODO Racial skills
            }


            ArcheSkill skill = new ArcheSkill(name, maleName, femaleName, strategy, inert, mains, raceMods);

            //Make sure skill is registered for Plugins.
			skills.put(name, skill);
			
			//Create the Help File, if it was specified
			if(helpText != null && helpIcon != null)
				HelpDesk.getInstance().addSkillTopic(name, helpText, helpIcon);

            //Honestly not even needed anymore since we're going with a COMPLETELY optional setup
            //Instances will be generated and removed on a situational basis

			//Add the Skill in question to all currently logged in Personas
			//(if a skill gets added after server startup; shouldn't happen, but might)
            /*ArchePersonaHandler ph = ArchePersonaHandler.getInstance();
            for(ArchePersona[] prs : ph.getPersonas()){
				for(ArchePersona p : prs){
					if(p != null && p.isCurrent()){
						
						//Start loading this Persona's Skill data for this one particular skill
						//SelectSkillTask task = new SelectSkillTask(p, skill);
						//FutureTask<SkillData> fut = task.getFuture();
						//SaveHandler.getInstance().put(task);

						//p.addSkill(skill, fut);
						break;
					}
				}
			}*/
			
			//ArcheCore.getPlugin().getSktop().registerTop(skill);
			return skill;
			
		} catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Error while preparing SQL statement for Skill xp gains", e);
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

	@Override
	public SkillFactory withProfessionalName(String name) {
		this.maleName = name;
		return this;
	}

	@Override
	public SkillFactory withFemaleProfessionalName(String name) {
		this.femaleName = name;
		return this;
	}

}
