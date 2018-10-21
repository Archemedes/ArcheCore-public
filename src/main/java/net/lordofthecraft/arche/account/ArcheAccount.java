package net.lordofthecraft.arche.account;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Tags;

public class ArcheAccount implements Account {
	@Getter private final int id; //Something to make sql happy
	@Getter private int forumId; //e.g. 22277 = sporadic
	@Getter private long discordId; //e.g 69173255004487680=Telanir

	private final Tags<Account> tags;
	
	List<ArcheToon> toons;
	List<String> ips;
	
	public ArcheAccount(int id) { //XXX
		this.id = id;
		tags = new AgnosticTags<>(this, "account_tags", "account_id_fk", id);
	}
	
	public List<ArcheToon> getToons(){
		return Collections.unmodifiableList(toons);
	}
}
