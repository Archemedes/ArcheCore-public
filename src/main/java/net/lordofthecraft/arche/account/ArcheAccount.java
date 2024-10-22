package net.lordofthecraft.arche.account;

import java.util.ArrayList;
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
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.Getter;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Tags;

public class ArcheAccount implements Account {
	private static final String ITEMCACHE_TAG_KEY = "ac_itemcache";
	
	@Getter private final int id; //Something to make sql happy
	
	@Getter private long forumId; //e.g. 22277 = sporadic
	@Getter private long discordId; //e.g 69173255004487680=Telanir
	
	@Getter private final Tags<Account> tags;
	
	@Getter long timePlayed;
	@Getter long timePlayedThisWeek;
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
	public String getName() {
		var p = getPlayer();
		if(p != null) return p.getName();
		
		//Poorly defined behavior but whatever
		return ArcheCore.getControls().getPlayerNameFromUUID(alts.stream().findAny().get());
	}
	
	@Override
	public Player getPlayer(){
		return getUUIDs().stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}
	
	@Override
	public boolean hasForumId() {
		return forumId > 0;
	}
	
	@Override
	public void setForumId(long forumId) {
		this.forumId = forumId;
		ArcheCore.getConsumerControls()
		.update("accounts")
		.where("account_id", getId())
		.set("forum_id", forumId)
		.queue();
	}
	
	@Override
	public boolean hasDiscordId() {
		return discordId > 0;
	}
	
	@Override
	public void setDiscordId(long discordId) {
		this.discordId = discordId;
		ArcheCore.getConsumerControls()
		.update("accounts")
		.where("account_id", getId())
		.set("discord_id", discordId)
		.queue();
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
	
	@Override
	public void addItemsToCache(List<ItemStack> items) {
		List<ItemStack> merge = new ArrayList<>(getItemCache());
		merge.addAll(items);
		setItemCache(merge);
	}
	
	@Override
	public void setItemCache(List<ItemStack> items) {
		if(items.isEmpty()) {
			tags.removeTag(ITEMCACHE_TAG_KEY);
		} else {
			String yaml = InventoryUtil.serializeItems(items);
			tags.giveTag(ITEMCACHE_TAG_KEY, yaml);
		}
	}
		
	@Override
	public List<ItemStack> getItemCache(){
		if(tags.hasTag(ITEMCACHE_TAG_KEY)){
			String value = tags.getValue(ITEMCACHE_TAG_KEY);
			return InventoryUtil.deserializeItems(value);
		} else {
			return Lists.newArrayList();
		}
	}
	
	
	public void addTimePlayed(long mins) {
		timePlayed += mins;
		timePlayedThisWeek += mins;
	}
	
	void initTimes() {
		lastSeen = System.currentTimeMillis();
	}
	
	void merge(ArcheAccount alt) {
		alt.remove();
		
		alt.getUUIDs().forEach(this::registerAlt);
		alt.getIPs().forEach(this::registerIp);
		alt.getTags().getTagMap().forEach((k,v) -> {if(!tags.hasTag(k)) tags.giveTag(k, v);});
		
		//Update sessions
		ArcheCore.getConsumerControls().update("account_sessions")
			.set("account_id_fk", this.id)
			.where("account_id_fk", alt.getId())
			.queue();
	}
	
	private void remove() {
		cleanseTable("playeraccounts");
		cleanseTable("account_ips");
		cleanseTable("account_tags");
		ArcheCore.getConsumerControls().delete("accounts").where("account_id", getId()).queue();
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

