package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.lordofthecraft.arche.interfaces.Tags;
import net.lordofthecraft.arche.interfaces.Toon;

public class ArcheToon implements Toon {
	private final ArcheAccount account;
	private final UUID uuid;
	private final List<String> usedNames = new ArrayList<>();
	
	private final Tags<Toon> tags;
	
	public ArcheToon(ArcheAccount account, UUID uuid) {//XXX
		this.account = account;
		this.uuid = uuid;
		tags = new AgnosticTags<>(this, "toon_tags", "player_fk", uuid);
	}

}
