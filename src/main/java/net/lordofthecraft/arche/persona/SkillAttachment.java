package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.rows.skills.DelSkillRow;
import net.lordofthecraft.arche.save.rows.skills.UpdateSkillRow;
import net.lordofthecraft.arche.skill.ArcheSkill;

import java.sql.JDBCType;
import java.sql.SQLType;

public class SkillAttachment {

	public enum Field {
		XP("xp", JDBCType.DOUBLE),
		VISIBLE("visible", JDBCType.BOOLEAN);

		public final String field;
		public final SQLType type;

		Field(String field, SQLType type) {
			this.field = field;
			this.type = type;
		}
	}

	private static final double DEFAULT_XP = 0.0;

    //private static final ArcheExecutor buffer = ArcheExecutor.getInstance();
    private static final IConsumer consumer = ArcheCore.getConsumerControls();
    final ArcheSkill skill;
	private final PersonaSkills handle;
    private final Persona persona;
	private double xp;
	private boolean canSee;
	private boolean error = false;
	private boolean inPersonaSkills = true;

	//NB: This constructor is ONLY called from PersonaSkills when it is NEWLY INITIALIZED
	//it is NOT called when loaded from SQL. If called, the PersonaSkills will NOT hold a reference to this attachment
	//We set inPersonaSkills to false in this constructor. We only add it if there is a need to.
    //That is, if the persona's xp/visibility values are non-default and we need to keep track of it
    SkillAttachment(Skill skill, Persona persona) {
        this(skill, persona, DEFAULT_XP, skill.getVisibility() == Skill.VISIBILITY_VISIBLE);
        inPersonaSkills = false;
    }

    SkillAttachment(Skill skill, Persona persona, double xp, boolean visible) {
        this.handle = persona.skills();

        this.xp = xp;
        this.canSee = visible;

        this.skill = (ArcheSkill) skill;
        this.persona = persona;
    }
	
	private boolean hasDefaultValues() {
		return (xp == DEFAULT_XP) && skill.getVisibility() == Skill.VISIBILITY_VISIBLE;
	}

	public double getXp(){
		return xp;
	}

	public void setXp(double xp) {
		this.xp = xp;
		if(this.xp < DEFAULT_XP) this.xp = 0;
		performSQLUpdate(Field.XP);
	}
	
	public boolean isVisible(){
		return canSee;
	}
	
	public void reveal(){
		if(!canSee){
			canSee = true;
			performSQLUpdate(Field.VISIBLE);
		}
	}
	
	public void addXp(double added){
		xp += added;

		performSQLUpdate(Field.XP);
	}
	
	public void removeXP(double removed){
		xp -= removed;
		if(this.xp < DEFAULT_XP) this.xp = 0;
		performSQLUpdate(Field.XP);
	}

	private Object getValueForField(Field f) {
		switch (f) {
			case XP:
				return xp;
			case VISIBLE:
				return canSee;
			default:
				return null;
		}
	}
	
	private void performSQLUpdate(Field f){
		if(error) return;
		
		if(inPersonaSkills && hasDefaultValues()) {
            //buffer.put(new SkillDeleteTask(skill.getName(), persona_id));
            consumer.queueRow(new DelSkillRow(persona, skill));
            handle.removeSkillAttachment(this);
			inPersonaSkills = false;
		} else {
            //ArcheTask task = new SkillUpdateTask(persona_id, skill.getName(), f, getValueForField(f));
            //buffer.put(task);
            consumer.queueRow(new UpdateSkillRow(persona, skill, f, getValueForField(f)));
            if(!inPersonaSkills) { //Start tracking this skill in PersonaSkills
				handle.addSkillAttachment(this);
				inPersonaSkills = true;
			}
		}
	}
	
}
