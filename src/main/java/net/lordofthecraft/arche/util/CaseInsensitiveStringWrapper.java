package net.lordofthecraft.arche.util;

import org.apache.commons.lang.Validate;

public class CaseInsensitiveStringWrapper {
	public final String value;
	private final String value_lowercase;
	
	public CaseInsensitiveStringWrapper(String value) {
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
		return value_lowercase.equals(other);
	}
	
	public String getValue() {
		return value;
	}
}
