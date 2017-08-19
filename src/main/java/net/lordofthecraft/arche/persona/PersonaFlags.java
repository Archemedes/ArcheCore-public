package net.lordofthecraft.arche.persona;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PersonaFlags {
	
	public static HashMap<String, PersonaFlag> flags = Maps.newHashMap();

	private final static String flag_divider = "\u2691";
	
	public static String serialize(PersonaFlags flagObject) {
		
		for (PersonaFlag f : flags.values()) {
			System.out.println(f.id);
			for (String s : f.values)
				System.out.println("-" + s);
		}
		
		if (flags.size() == 0) return null;
			
		StringBuilder sb = new StringBuilder();
		
		System.out.println(flags.size());
		System.out.println(flags.values().size());
		
		for (PersonaFlag flag : flags.values())
			sb.append(flag.serialize() + flag_divider);
		
		return sb.substring(0, sb.length()-1);
	}
	
	public static PersonaFlags deserialize(String flagString) {
		PersonaFlags toReturn = new PersonaFlags();
		
		String[] flags = flagString.split(flag_divider);
		
		for (String flag : flags) {
			toReturn.addFlag(PersonaFlag.deserialize(flag));
		}
		
		return toReturn;
	}
	
	public boolean addFlag(PersonaFlag flag) {
		if (flags.containsKey(flag.getID())) return false;
		else flags.put(flag.getID(), flag);
		return true;
	}
	
	public boolean hasFlag(String flagID) {
		return (flags.containsKey(flagID.toLowerCase()));
	}
	
	public boolean removeFlag(PersonaFlag flag) {
		if (!flags.containsKey(flag.getID())) return false;
		else flags.remove(flag.getID());
		return true;
	}
	
	public boolean removeFlag(String flagID) {
		if (!flags.containsKey(flagID.toLowerCase())) return false;
		else flags.remove(flagID.toLowerCase());
		return true;
	}
	
	public void updateFlag(PersonaFlag flag) {
		flags.put(flag.getID(), flag);
		return;
	}
	
	public PersonaFlag getFlag(String flagID) {
		if (flags.containsKey(flagID.toLowerCase())) return flags.get(flagID.toLowerCase());
		return null;
	}
	
	public static class PersonaFlag {

		private final static String flaglist_divider = "\u2690";
		
		private final String id;
		private List<String> values;
		
		public PersonaFlag(String id, String value) {
			this.id = id.toLowerCase();
			this.values = Lists.newArrayList();
			if (value != null) values.add(value);
		}
		
		public String serialize() {
			
			StringBuilder sb = new StringBuilder(id);
			
			for (String v : values) {
				sb.append(flaglist_divider + v);
			}
			
			return sb.toString();
		}
		
		public static PersonaFlag deserialize(String s) {
			String[] flagstrings = s.split(flaglist_divider);
			
			String id = flagstrings[0];
			
			ArrayList<String> values = Lists.newArrayList(flagstrings);
			
			values.remove(0);
			
			return new PersonaFlag(id, values);
		}

		public PersonaFlag(String id, List<String> values) {
			this.id = id.toLowerCase();
			if (values == null)
				values = Lists.newArrayList();
			else this.values = values;
		}
		
		public PersonaFlag(String id) {
			this.id = id.toLowerCase();
			this.values = Lists.newArrayList();
		}

		public String getID() {
			return this.id;
		}
		
		public String getValue(int index) {
			return this.values.get(index);
		}
		
		public List<String> getValues() {
			return this.values;
		}
		
		public void setValues(List<String> values) {
			this.values = values;
		}
		
		public void setValue(String value) {
			this.values = Lists.newArrayList();
			values.add(value);
		}
		
		public void clearValues(String value) {
			this.values = Lists.newArrayList();
		}
		
		public void addValue(String value) {
			values.add(value);
		}
		
		public boolean removeValue(int i) {
			if (i >= values.size()) return false;
			else values.remove(i);
			return false;
		}
		
	}
}
