package net.lordofthecraft.arche.save.tasks;

import java.sql.SQLException;

import net.lordofthecraft.arche.skill.BonusExpModifier;

public class BonusExpModifierTask extends StatementTask{

	private final BonusExpModifier modifier;
	private final boolean exists;
	
	public BonusExpModifierTask(BonusExpModifier modifier, boolean exists) {
		this.modifier = modifier;
		this.exists = exists;
	}
	
	@Override
	protected void setValues() throws SQLException {
		stat.setInt(1, modifier.getId());
		stat.setString(2, modifier.getType().name());
		stat.setLong(3, modifier.getDuration());
		stat.setLong(4, modifier.getStartTime());
		stat.setInt(5, modifier.getStartExp());
		stat.setInt(6, modifier.getCapExp());
		stat.setString(7, modifier.getSkill().getName());
		stat.setString(8, modifier.getUUID().toString());
		stat.setInt(9, modifier.getPersonaID());
		stat.setDouble(10, modifier.getModifer());
	}

	@Override
	protected String getQuery() {
		if (!exists)
		return "INSERT INTO skill_personal_mods VALUES (?,?,?,?,?,?,?,?,?,?)";
		else
		return "UPDATE skill_personal_mods WHERE id=" + modifier.getId() + " SET (?,?,?,?,?,?,?,?,?,?)";
	}

}
