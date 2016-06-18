package net.lordofthecraft.arche.persona;

import com.mojang.authlib.properties.Property;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

public class PersonaSkin {
	
	private String data;
	private String signature;
	
	public PersonaSkin(String combined) {
		String[] comb = StringUtils.split(combined, ';');
		this.data = comb[0];
		this.signature = comb[1]; 
	}
	
	public PersonaSkin(String data, String signature) {
		this.data = data;
		this.signature = signature;
	}

	public PersonaSkin(Player skinee) {
		// TODO Auto-generated constructor stub
	}

	public String getCombined() {
		return data + ';' + signature;
	}
	
	public Property getProperty() {
		return new Property("", data, signature);
	}
	
	public String getData() {
		return data;
	}
	
	public String getSignature() {
		return signature;
	}

}
