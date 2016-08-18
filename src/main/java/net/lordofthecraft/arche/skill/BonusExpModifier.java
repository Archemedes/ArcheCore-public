package net.lordofthecraft.arche.skill;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;

public class BonusExpModifier {

	private final int id;

	private final ExpModifier type;
	private final long starttime;
	private final int startxp;
	private final int capxp;
	private final Skill skill;
	private final UUID uuid;
	private final int pid;
	private final double modifier;

	//Not final for cancellation reasons.
	private long duration;

	/**
	 * Default constructor for SQL building.
	 * @param type
	 * @param persona
	 * @param skill
	 * @param starttime
	 * @param startxp
	 * @param capxp
	 * @param duration
	 * @param modifier
	 */

	public BonusExpModifier(int mId, ExpModifier type, UUID uuid, int id, Skill skill, long starttime, long duration, int startxp, int capxp, double modifier) {
		this.type = type;
		this.starttime = starttime;
		this.duration = duration;
		this.startxp = startxp;
		this.capxp = capxp;
		this.skill = skill;
		this.uuid = uuid;
		this.pid = id;
		this.modifier = modifier;
		this.id = mId;
	}

	/**
	 * Persona specific, starts right away.
	 * @param type
	 * @param persona
	 * @param skill
	 * @param starttime
	 * @param startxp
	 * @param capxp
	 * @param duration
	 * @param modifier
	 */

	public BonusExpModifier(Persona persona, Skill skill, long duration, int capxp, double modifier) {
		this.id = BonusExpModifierHandler.nextId();
		this.type = ExpModifier.PERSONA;
		this.starttime = System.currentTimeMillis();
		this.duration = duration;
		int tostartxp = 0;
		if (skill == null) {
			for (Skill sk : ((ArchePersona)persona).professions) tostartxp += sk.getXp(persona);
		} else {
			tostartxp += skill.getXp(persona);
		}
		startxp = tostartxp;
		this.capxp = capxp;
		this.skill = skill;
		this.uuid = persona.getPlayerUUID();
		this.pid = persona.getId();
		this.modifier = modifier;
	}

	/**
	 * Account based, starts right away.
	 * @param type
	 * @param persona
	 * @param skill
	 * @param starttime
	 * @param startxp
	 * @param capxp
	 * @param duration
	 * @param modifier
	 */

	public BonusExpModifier(OfflinePlayer player, Skill skill, long duration, int capxp, double modifier) {
		this.id = BonusExpModifierHandler.nextId();
		this.type = ExpModifier.ACCOUNT;
		this.starttime = System.currentTimeMillis();
		this.duration = duration;
		int tostartxp = 0;
		for (Persona persona : ArcheCore.getControls().getPersonaHandler().getAllPersonas(player.getUniqueId())) {
			if (skill == null) {
				if (persona != null) for (Skill sk : ((ArchePersona)persona).professions) tostartxp += sk.getXp(persona);
			} else {
				tostartxp += skill.getXp(persona);
			}
		}
		startxp = tostartxp;
		this.capxp = capxp;
		this.skill = skill;
		this.uuid = player.getUniqueId();
		this.pid = -1;
		this.modifier = modifier;
	}

	/**
	 * Global, player credited. Starts right away
	 * @param type
	 * @param skill
	 * @param starttime
	 * @param duration
	 * @param player
	 * @param modifier
	 */

	public BonusExpModifier(Skill skill, long duration, double modifier, OfflinePlayer player) {
		this.id = BonusExpModifierHandler.nextId();
		this.type = ExpModifier.GLOBAL;
		this.starttime = System.currentTimeMillis();
		this.duration = duration;
		this.startxp = -1;
		this.capxp = -1;
		this.skill = skill;
		this.pid = -1;
		this.uuid = player.getUniqueId();
		this.modifier = modifier;
	}

	/**
	 * Global server initiated. Stars when told.
	 * @param type
	 * @param skill
	 * @param starttime
	 * @param duration
	 * @param modifier
	 */

	public BonusExpModifier(Skill skill, long starttime, long duration, double modifier) {
		this.id = BonusExpModifierHandler.nextId();
		this.type = ExpModifier.GLOBAL;
		this.starttime = starttime;
		this.duration = duration;
		this.startxp = -1;
		this.capxp = -1;
		this.skill = skill;
		this.uuid = null;
		this.pid = -1;
		this.modifier = modifier;
	}

	public ExpModifier getType() {
		return type;
	}

	public long getStartTime() {
		return starttime;
	}

	public long getDuration() {
		return duration;
	}

	public int getStartExp() {
		return startxp;
	}

	public int getCapExp() {
		return capxp;
	}

	public Skill getSkill() {
		return skill;
	}

	public UUID getUUID() {
		if (uuid != null) return uuid;
		else return null;
	}

	public int getPersonaID() {
		return pid;
	}

	public boolean isExpired() {
		if (this.getDuration() != -1) 
		{ if (this.getStartTime() + this.getDuration() > System.currentTimeMillis()) return true; }
		else if (this.skill == null) {
			ArchePersona[] personas = (ArchePersona[]) ArcheCore.getControls().getPersonaHandler().getAllPersonas(this.uuid);
			if (personas == null) return false; //If the player hasn't logged in recently, just load it and don't bother with it to avoid issues.
			int expnow = 0;
			if (this.getPersonaID() == -1) {
				for (ArchePersona pers : personas) {
					if (pers != null) for (Skill skill : pers.professions) expnow += skill.getXp(pers);
				}
			} else {
				ArchePersona pers = personas[this.getPersonaID()];
				if (pers == null) return true;
				else for (Skill skill : pers.professions) expnow += skill.getXp(pers);
			}
			if (this.getStartExp() + this.getCapExp() > expnow) return true;
		}
		else if (this.getCapExp() != -1) {
			Persona[] personas = ArcheCore.getControls().getPersonaHandler().getAllPersonas(this.uuid);
			if (personas == null) return false; //If the player hasn't logged in recently, just load it and don't bother with it to avoid issues.
			int expnow = 0;
			if (this.getPersonaID() == -1) {
				for (Persona pers : personas) {
					if (pers != null) expnow += this.getSkill().getXp(pers);
				}
			} else {
				Persona pers = personas[this.getPersonaID()];
				if (pers == null) return true;
				else expnow += this.getSkill().getXp(pers);
			}
			if (this.getStartExp() + this.getCapExp() > expnow) return true;
		}
		//broken modifier (but we were meant to be forever!)
		return true;
	}

	public double getModifer() {
		return modifier;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getId() {
		return this.id;
	}
}
