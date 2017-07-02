package net.lordofthecraft.arche.enums;

import net.lordofthecraft.arche.save.PersonaField;

public enum ProfessionSlot {
	PRIMARY( 0, "main"),
	SECONDARY(1, "second"),
	ADDITIONAL(2, "bonus"),
	UNSELECTED(3, "unselected");

	private final int slot;
	private final String simple;

	ProfessionSlot(int slot, String simple) {
		this.slot = slot;
		this.simple = simple;
	}
	
	public int getSlot(){
		return slot;
	}

	public String toSimpleString() {
		return simple;
	}

	public static ProfessionSlot byInt(int i) {
		for (ProfessionSlot slot : values()) {
			if (slot.slot == i) {
				return slot;
			}
		}
		return UNSELECTED;
	}
	
}
