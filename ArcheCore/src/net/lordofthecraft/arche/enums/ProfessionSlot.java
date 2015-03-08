package net.lordofthecraft.arche.enums;

import net.lordofthecraft.arche.save.PersonaField;

public enum ProfessionSlot {
	PRIMARY(PersonaField.SKILL_PRIMARY, 0),
	SECONDARY(PersonaField.SKILL_SECONDARY, 1),
	ADDITIONAL(PersonaField.SKILL_ADDITIONAL, 2);
	
	private final PersonaField field;
	private final int slot;
	
	private ProfessionSlot(PersonaField field, int slot){
		this.field = field;
		this.slot = slot;
	}
	
	public PersonaField getPersonaField(){
		return field;
	}
	
	public int getSlot(){
		return slot;
	}
	
}
