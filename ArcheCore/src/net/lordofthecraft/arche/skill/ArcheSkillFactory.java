package net.lordofthecraft.arche.skill;

import net.lordofthecraft.arche.enums.*;
import org.bukkit.*;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.*;
import net.lordofthecraft.arche.help.*;
import net.lordofthecraft.arche.persona.*;
import net.lordofthecraft.arche.save.*;
import net.lordofthecraft.arche.save.tasks.*;
import net.lordofthecraft.arche.SQL.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.common.collect.*;

public class ArcheSkillFactory implements SkillFactory
{
    private static final Map<String, String> VALS;
    private static final Map<String, ArcheSkill> skills;
    private static int count;
    static Set<ExpModifier> xpMods;
    private final String name;
    private final int id;
    private int strategy;
    private boolean inert;
    private final Set<Race> mains;
    private final Map<Race, Double> raceMods;
    private boolean intensive;
    private String helpText;
    private Material helpIcon;
    
    public static void activateXpMod(final ExpModifier mod) {
        ArcheSkillFactory.xpMods.add(mod);
    }
    
    public static Skill getSkill(final String name) {
        if (name == null) {
            return null;
        }
        return ArcheSkillFactory.skills.get(name.toLowerCase());
    }
    
    public static Map<String, ArcheSkill> getSkills() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends ArcheSkill>)ArcheSkillFactory.skills);
    }
    
    public static Skill createSkill(final String name) {
        return registerNewSkill(name).register();
    }
    
    public static SkillFactory registerNewSkill(String name) {
        name = name.toLowerCase();
        final Skill test = ArcheSkillFactory.skills.get(name);
        if (test != null) {
            throw new DuplicateSkillException("Skill " + name + " already exists");
        }
        return new ArcheSkillFactory(name);
    }
    
    private ArcheSkillFactory(final String name) {
        super();
        this.strategy = 1;
        this.inert = false;
        this.mains = EnumSet.noneOf(Race.class);
        this.raceMods = new EnumMap<Race, Double>(Race.class);
        this.intensive = false;
        this.helpText = null;
        this.helpIcon = null;
        this.name = name;
        this.id = ArcheSkillFactory.count++;
    }
    
    @Override
    public SkillFactory withVisibilityType(final int visibility) {
        this.strategy = visibility;
        return this;
    }
    
    @Override
    public SkillFactory withXpGainWhileHidden(final boolean inert) {
        this.inert = inert;
        return this;
    }
    
    @Override
    public SkillFactory asRacialProfession(final Race race) {
        this.mains.add(race);
        return this;
    }
    
    @Override
    public SkillFactory withRacialModifier(final Race race, final double modifier) {
        this.raceMods.put(race, modifier);
        return this;
    }
    
    @Override
    public SkillFactory withHelpFile(final String helpText, final Material helpIcon) {
        this.helpText = helpText;
        this.helpIcon = helpIcon;
        return this;
    }
    
    @Override
    public SkillFactory setIntensiveProfession(final boolean intensive) {
        this.intensive = intensive;
        return this;
    }
    
    @Override
    public Skill register() {
        try {
            final SQLHandler handler = ArcheCore.getControls().getSQLHandler();
            final Connection con = handler.getSQL().getConnection();
            ArcheCore.getPlugin().getSQLHandler().createTable("sk_" + this.name, ArcheSkillFactory.VALS);
            final PreparedStatement statement = con.prepareStatement("INSERT INTO sk_" + this.name + " VALUES (?,?,?,?)");
            final ArcheSkill skill = new ArcheSkill(this.id, this.name, this.strategy, this.inert, this.mains, this.raceMods, statement, this.intensive);
            ArcheSkillFactory.skills.put(this.name, skill);
            if (this.helpText != null && this.helpIcon != null) {
                HelpDesk.getInstance().addSkillTopic(this.name, this.helpText, this.helpIcon);
            }
            final ArchePersonaHandler ph = ArchePersonaHandler.getInstance();
            for (final ArchePersona[] array : ph.getPersonas()) {
                final ArchePersona[] prs = array;
                for (final ArchePersona p : array) {
                    if (p != null && p.isCurrent()) {
                        final SelectSkillTask task = new SelectSkillTask(p, skill);
                        final FutureTask<SkillData> fut = task.getFuture();
                        SaveHandler.getInstance().put(task);
                        p.addSkill(skill, fut);
                        break;
                    }
                }
            }
            return skill;
        }
        catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().severe("Error while preparing SQL statement for Skill xp gains");
            e.printStackTrace();
            return null;
        }
    }
    
    static {
        skills = Maps.newLinkedHashMap();
        ArcheSkillFactory.count = 0;
        ArcheSkillFactory.xpMods = EnumSet.noneOf(ExpModifier.class);
        final Map<String, String> vals = Maps.newLinkedHashMap();
        vals.put("player", "TEXT");
        vals.put("id", "INT");
        vals.put("xp", "DOUBLE NOT NULL");
        vals.put("visible", "INT NOT NULL");
        vals.put("UNIQUE (player, id)", "ON CONFLICT REPLACE");
        VALS = Collections.unmodifiableMap((Map<? extends String, ? extends String>)vals);
    }
    
    public static class DuplicateSkillException extends RuntimeException
    {
        private static final long serialVersionUID = -6769690779325926399L;
        
        private DuplicateSkillException() {
            super();
        }
        
        private DuplicateSkillException(final String message) {
            super(message);
        }
    }
}
