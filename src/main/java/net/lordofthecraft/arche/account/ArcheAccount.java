package net.lordofthecraft.arche.account;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Tags;
import net.lordofthecraft.arche.interfaces.Toon;

public class ArcheAccount implements Account {
	@Getter private final int id; //Something to make sql happy
	@Getter private int forumId; //e.g. 22277 = sporadic
	@Getter private long discordId; //e.g 69173255004487680=Telanir

	@Getter private final Tags<Account> tags;
	
	List<ArcheToon> toons;
	List<String> ips;
	
	public ArcheAccount(int id) { //XXX
		this.id = id;
		tags = new AgnosticTags<>(this, "account_tags", "account_id_fk", id);
	}
	
	@Override
	public List<Toon> getToons(){
		return Collections.unmodifiableList(toons);
	}
	
	void addToon(UUID uuid, String name) {
		ArcheToon toon = new ArcheToon(this, uuid);
		toon.usedNames.add(name);
		ArcheCore.getConsumerControls()
			.insert("minecraft_toons")
			.set("account_id_fk", this.id)
			.set("player", uuid.toString())
			.queue();
		this.toons.add(toon);
	}
}

