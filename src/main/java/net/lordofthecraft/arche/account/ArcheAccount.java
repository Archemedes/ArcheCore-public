package net.lordofthecraft.arche.account;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Tags;

public class ArcheAccount implements Account {
	@Getter private final int id; //Something to make sql happy
	
	@Getter private long forumId; //e.g. 22277 = sporadic
	@Getter private long discordId; //e.g 69173255004487680=Telanir
	
	@Getter private final Tags<Account> tags;
	
	private WeakReference<Player> playerObject;
	
	@Getter long timePlayed;
	@Getter long lastSeen;
	
	final Set<UUID> alts = new HashSet<>();
	final Set<String> ips = new HashSet<>();
	
	public ArcheAccount(int id) {
		this(id, 0, 0);
	}
	
	public ArcheAccount(int id, long forumId, long discordId) {
		this.id = id;
		this.forumId = forumId;
		this.discordId = discordId;
		tags = new AgnosticTags<>(this, "account_tags", "account_id_fk", id);
	}
	
	@Override
	public Player getPlayer(){
		Player play;
		if(playerObject == null || (play = playerObject.get()) == null || play.isDead()){
			var op = alts.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).findAny();
			if(op.isPresent()) {
				play = op.get();
				playerObject = new WeakReference<>(play);
				return play;
			} else {
				playerObject = null;
				return null;
			}
		}

		return play;
	}
	
	@Override
	public List<Persona> getPersonas(){
		var aph = ArcheCore.getPersonaControls();
		
		return alts.stream()
		.flatMap( u-> Stream.of(aph.getAllPersonas(u)) )
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
	}
	
	@Override
	public List<String> getUsernames(){
		var ac = ArcheCore.getControls();
		
		return alts.stream()
		.flatMap(u->ac.getKnownAliases(u).stream())
		.collect(Collectors.toList());
	}
	
	@Override
	public Set<UUID> getUUIDs() { return Collections.unmodifiableSet(alts); }
	
	
	@Override
	public Set<String> getIPs() { return Collections.unmodifiableSet(ips); }
	
	
	void updateLastSeen() {
		lastSeen = System.currentTimeMillis();
		var c = ArcheCore.getConsumerControls();
		c.update("accounts").set("last_seen", new Date(lastSeen)).where("account_id", getId()).queue();
	}
	
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

