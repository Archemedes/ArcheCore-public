package net.lordofthecraft.arche.account;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Tags;

public class ArcheAccount implements Account {
	@Getter private final int id; //Something to make sql happy
	@Getter private final UUID uniqueId;
	
	@Getter private long forumId; //e.g. 22277 = sporadic
	@Getter private long discordId; //e.g 69173255004487680=Telanir
	
	@Getter private final Tags<Account> tags;
	
	long timePlayed;
	Date lastSeen;
	
	final Set<UUID> alts = new HashSet<>();
	final Set<String> ips = new HashSet<>();
	
	public ArcheAccount(int id, UUID uuid, long forumId, long discordId) {
		this.id = id;
		this.uniqueId = uuid;
		this.forumId = forumId;
		this.discordId = discordId;
		tags = new AgnosticTags<>(this, "account_tags", "account_id_fk", id);
	}
	
	public void registerAlt(UUID uuid) {
		//TODO
	}
	
	public void registerIp(String ip) {
		if(ips.add(ip))
			ArcheCore.getConsumerControls()
			.insert("account_ips")
			.set("account_id_fk", id)
			.set("ip_address", ip)
			.queue();
	}
	
}

