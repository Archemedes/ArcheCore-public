package net.lordofthecraft.arche.account;

import com.google.gson.Gson;

import net.lordofthecraft.arche.interfaces.Tags;

public abstract class AbstractTags<T> implements Tags<T> {
	private static final Gson gson = new Gson();
	public static Gson getGson() { return gson; }
	
	
	
}
