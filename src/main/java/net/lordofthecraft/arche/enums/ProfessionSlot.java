package net.lordofthecraft.arche.enums;

import net.lordofthecraft.arche.save.PersonaField;

public enum ProfessionSlot {
	PRIMARY(PersonaField.SKILL_PRIMARY, 0, "main"),
	SECONDARY(PersonaField.SKILL_SECONDARY, 1, "second"),
	ADDITIONAL(PersonaField.SKILL_ADDITIONAL, 2, "bonus");
	
	private final PersonaField field;
	private final int slot;
	private final String simple;

	ProfessionSlot(PersonaField field, int slot, String simple) {
		this.field = field;
		this.slot = slot;
		this.simple = simple;
	}
	
	public PersonaField getPersonaField(){
		return field;
	}
	
	public int getSlot(){
		return slot;
	}

	public String toSimpleString() {
		return simple;
	}
	
}
