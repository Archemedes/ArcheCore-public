package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Tags;

public class ArcheAccount implements Account {
	@Getter private final int id; //Something to make sql happy
	@Getter private final UUID uniqueId;
	
	@Getter private int forumId; //e.g. 22277 = sporadic
	@Getter private long discordId; //e.g 69173255004487680=Telanir
	
	@Getter private final Tags<Account> tags;
	
	final List<String> usedNames = new ArrayList<>();
	final List<String> ips = new ArrayList<>();
	
	public ArcheAccount(int id, UUID uuid) { //XXX
		this.id = id;
		this.uniqueId = uuid;
		tags = new AgnosticTags<>(this, "account_tags", "account_id_fk", id);
	}
}

