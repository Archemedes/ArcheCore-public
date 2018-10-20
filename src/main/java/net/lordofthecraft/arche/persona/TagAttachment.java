package net.lordofthecraft.arche.persona;

import java.util.UUID;

import net.lordofthecraft.arche.account.AbstractTags;

public class TagAttachment {
	private final String key, value;
	private final boolean offline;

	public TagAttachment(String key, String value, boolean offline) {
		this.key = key;
		this.value = value;
		this.offline = offline;
	}
	
	public static TagAttachment build(String key, Object data) {
		return build(key, data, false);
	}
	
	public static TagAttachment build(String key, Object data, boolean offline) {
		String value; Class<?> c = data.getClass();
		if(c == String.class || c.isEnum()) value = data.toString();
		else value = AbstractTags.getGson().toJson(data);
		
		return new TagAttachment(key, value, offline);
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

	@SuppressWarnings("unchecked")
	public <T> T getAs(Class<T> c) {
		if(c == String.class) return (T) value;
		return AbstractTags.getGson().fromJson(value, c);
	}
}
