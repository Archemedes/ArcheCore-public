package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArcheToon { //Implements Toon at some point?
	private final ArcheAccount account;
	private final UUID uuid;
	private final List<String> usedNames = new ArrayList<>();
	
	//Probably some handy indexing of Personas here at some point
	
	public ArcheToon(ArcheAccount account, UUID uuid) {//XXX
		this.account = account;
		this.uuid = uuid;
	}
}
