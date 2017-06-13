package net.lordofthecraft.arche.persona;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PersonaFlags implements Serializable {
	
	private static final long serialVersionUID = -4481347109844409875L;
	
	public HashMap<String, PersonaFlag> flags = Maps.newHashMap();
	
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
	
	public static class PersonaFlag implements Serializable {

		private static final long serialVersionUID = 992575205150259285L;
		
		private final String id;
		private List<String> values;
		
		public PersonaFlag(String id, String value) {
			this.id = id.toLowerCase();
			this.values = Lists.newArrayList();
			if (value != null) values.add(value);
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
