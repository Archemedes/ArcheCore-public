package net.lordofthecraft.arche.account;

import java.sql.Date;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Tags;

public class ArcheAccount implements Account {
	@Getter private final int id; //Something to make sql happy
	
	@Getter private long forumId; //e.g. 22277 = sporadic
	@Getter private long discordId; //e.g 69173255004487680=Telanir
	
	@Getter private final Tags<Account> tags;
	
	long timePlayed;
	Date lastSeen;
	
	final Set<UUID> alts = new HashSet<>();
	final Set<String> ips = new HashSet<>();
	
	public ArcheAccount(int id, long forumId, long discordId) {
		this.id = id;
		this.forumId = forumId;
		this.discordId = discordId;
		tags = new AgnosticTags<>(this, "account_tags", "account_id_fk", id);
	}
	
	@Override
	public Set<UUID> getUUIDs() { return Collections.unmodifiableSet(alts); }
	
	
	@Override
	public Set<String> getIPs() { return Collections.unmodifiableSet(ips); }
	
	void merge(ArcheAccount alt) {
		alt.remove();
		
		alt.getUUIDs().forEach(this::registerAlt);
		alt.getIPs().forEach(this::registerIp);
		alt.getTags().getTagMap().forEach((k,v) -> {if(!tags.hasTag(k)) tags.giveTag(k, v);});
	}
	
	private void remove() {
		cleanseTable("playeraccounts");
		cleanseTable("account_ips");
		cleanseTable("account_tags");
		cleanseTable("accounts");
	}
	
	private void cleanseTable(String tableName) {
		ArcheCore.getConsumerControls().delete(tableName).where("account_id_fk", getId()).queue();
	}
	
	private void registerAlt(UUID uuid) {
		if(alts.contains(uuid)) throw new IllegalStateException("Alt was already registered to " + id + ". UUID=" + uuid);
		ArcheCore.getConsumerControls().insert("playeraccounts").set("player", uuid).set("account_id_fk", id).queue();
		alts.add(uuid);
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

