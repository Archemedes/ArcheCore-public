package net.lordofthecraft.arche.persona;

import java.util.UUID;

public class TagAttachment {
	private final String key,value;
	private final boolean offline;

	TagAttachment(String key, String value){
		this(key, value, false);
	}
	
	TagAttachment(String key, String value, boolean offline) {
		this.key = key;
		this.value = value;
		this.offline = offline;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getAsString() {
		return value;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isAvailableOffline() {
		return offline;
	}
	
	public int getAsInt() {
		return Integer.parseInt(value);
	}
	
	public double getAsDouble() {
		return Double.parseDouble(value);
	}
	
	public long getAsLong() {
		return Long.parseLong(value);
	}
	
	public UUID getAsUUID() {
		return UUID.fromString(value);
	}
	
	public boolean getAsBoolean() {
		return Boolean.valueOf(value);
	}
}
