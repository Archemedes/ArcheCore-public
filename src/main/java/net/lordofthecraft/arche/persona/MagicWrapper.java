package net.lordofthecraft.arche.persona;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.text.WordUtils;

import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.persona.PersonaFlags.PersonaFlag;

public class MagicWrapper extends PersonaFlag {

	/*
	 * 0 date acquired Timestamp
	 * 1 tier 1-5, 0 if n/a
	 * 2 visible 0/1
	 * 3 teacher 0/1
	 * 4 usable 0/1
	 * 5 teacher UID
	 */
	
	public MagicWrapper(String id, List<String> values) {
		super(id, values);
	}

	public static MagicWrapper fromFlag(PersonaFlag flag) {
		return new MagicWrapper(flag.getID(), flag.getValues());
	}
	
	public String getDateString() {
		return (this.getValue(0));
	}
	
	public int getTier() {
		return (Integer.valueOf(getValue(1)));
	}
	
	public boolean isVisible() {
		return (this.getValue(2) == "1");
	}
	
	public boolean isTeacher() {
		return (this.getValue(3) == "1");
	}
	
	public boolean isUsable() {
		return (this.getValue(4) == "1");
	}
	
	public PersonaKey getTeacher() {
		try {
			String[] key = this.getValue(5).split("@");
			return new ArchePersonaKey(UUID.fromString(key[0]), Integer.valueOf(key[1]));
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getTeacherString() {
		return (this.getValue(5));
	}

}
