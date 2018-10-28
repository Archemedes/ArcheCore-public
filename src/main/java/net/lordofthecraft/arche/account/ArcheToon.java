package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Tags;
import net.lordofthecraft.arche.interfaces.Toon;

public class ArcheToon implements Toon {
	@Getter ArcheAccount account; //Not final for merging reasons
	@Getter private final UUID uniqueId;
	final List<String> usedNames = new ArrayList<>();
	
	@Getter private final Tags<Toon> tags;
	
	public ArcheToon(ArcheAccount account, UUID uuid) {//XXX
		this.account = account;
		this.uniqueId = uuid;
		tags = new AgnosticTags<>(this, "toon_tags", "player_fk", uuid);
	}
	
}
