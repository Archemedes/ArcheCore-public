package net.lordofthecraft.arche.skill;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
	 */

	public BonusExpModifier(Persona persona, Skill skill, long duration, int capxp, double modifier) {
		this.id = BonusExpModifierHandler.nextId();
		this.type = ExpModifier.PERSONA;
		this.starttime = System.currentTimeMillis();
		this.duration = duration;
		int tostartxp = 0;
		if (skill == null) {
			for (Skill sk : ((ArchePersona)persona).professions) if (sk != null) tostartxp += sk.getXp(persona);
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
	 */

	public BonusExpModifier(OfflinePlayer player, Skill skill, long duration, int capxp, double modifier) {
		this.id = BonusExpModifierHandler.nextId();
		this.type = ExpModifier.ACCOUNT;
		this.starttime = System.currentTimeMillis();
		this.duration = duration;
		int tostartxp = 0;
		for (Persona persona : ArcheCore.getControls().getPersonaHandler().getAllPersonas(player.getUniqueId())) {
			if (persona != null) 
				if (skill == null) {
					for (Skill sk : ((ArchePersona)persona).professions) if (sk != null) tostartxp += sk.getXp(persona);
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
		if (this.getStartTime() != -1) {
			// Not yet started
			if (this.getStartTime() > System.currentTimeMillis()) return true;
			//Expired
			else if (this.getStartTime() + this.getDuration() < System.currentTimeMillis()) return true;
			else return false;
		}
		else if (this.skill == null) {
			ArchePersona[] personas = (ArchePersona[]) ArcheCore.getControls().getPersonaHandler().getAllPersonas(this.uuid);
			if (personas == null) return false; //If the player hasn't logged in recently, just load it and don't bother with it to avoid issues.
			int expnow = 0;
			if (this.getPersonaID() == -1) {
				for (ArchePersona pers : personas) {
					if (pers != null) for (Skill skill : pers.professions) if (skill != null) expnow += skill.getXp(pers);
				}
			} else {
				ArchePersona pers = personas[this.getPersonaID()];
				if (pers == null) return true;
				else for (Skill skill : pers.professions) if (skill != null) expnow += skill.getXp(pers);
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

	public String readableString() {
		BonusExpModifier m = this;
		String g = ChatColor.GOLD + "";
		String r = ChatColor.RESET + "";
		return 
				ChatColor.GRAY + "["+(!this.isExpired() ? ChatColor.DARK_GREEN +""+ ChatColor.BOLD + "ACTIVE" : ChatColor.RED +""+ ChatColor.BOLD + "INACTIVE") + ChatColor.GRAY + "]"
				+g+ " ID: " + r + m.getId()
				+g+ " Type: " + r + m.getType().toString()
				+g+ " Modifer: " + r + m.getModifer()
				+g+ " Initiated by: " + r + (m.getUUID() == null ? "Server" : Bukkit.getServer().getOfflinePlayer(m.getUUID()).getName())
				+g+ (m.getPersonaID() == -1 ? "" : " Persona ID: " + r + m.getPersonaID())
				+g+ " Skill: " + r + (m.getSkill() == null ? "All" : m.getSkill().getName())
				+g+ (m.getStartTime() <= System.currentTimeMillis() ? "" : " Starting In: " + r + TimeUnit.MILLISECONDS.toMinutes(m.getStartTime() - System.currentTimeMillis())+ "m")
				+g+ (m.getDuration() == -1 ? "" : " Duration: " + r + TimeUnit.MILLISECONDS.toMinutes(m.getDuration()) + "m")
				+g+ (m.getCapExp() == -1 ? "" : " Max XP Gain: " + r + m.getCapExp() + "xp");
	}
}
