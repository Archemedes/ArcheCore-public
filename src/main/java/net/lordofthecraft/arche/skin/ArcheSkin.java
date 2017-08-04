package net.lordofthecraft.arche.skin;

import java.util.UUID;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;

public class ArcheSkin {
	private int index; //
	private UUID owner; //Player this cached skin belongs to
	private String skinUrl; //We use mojang url to refresh the validated skin data.
	private boolean slim;
	
	private long timeLastRefreshed; //We need this to make sure we refresh every 24 hrs.
	private WrappedSignedProperty mojangSkinData; //This is valid data to add to GameProfile
	
	public ArcheSkin(int index, String url, boolean isSlim) {
		this.index = index;
		this.skinUrl = url;
		this.slim = isSlim;
	}
	
	public String getURL() {
		return skinUrl;
	}
	
	public boolean isSlim() {
		return slim;
	}
	
}
