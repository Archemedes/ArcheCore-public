package net.lordofthecraft.arche.util;

import org.apache.commons.lang.Validate;

public class CaseString {
	public final String value;
	private final String value_lowercase;
	
	public CaseString(String value) {
		Validate.notNull(value);
		this.value = value;
		this.value_lowercase = value.toLowerCase();
	}
	
	@Override
	public int hashCode() {
		return value_lowercase.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || other.getClass() != this.getClass())
			return false;
		
		return value_lowercase.equals(((CaseString) other).value_lowercase);
	}
	
	public String getValue() {
		return value;
	}
}
