package net.lordofthecraft.arche.account;

import java.util.List;

import net.lordofthecraft.arche.interfaces.Account;

public class ArcheAccount implements Account {
	private final int id; //Something to make sql happy
	private int forumId; //e.g. 22277 = sporadic
	private long discordId; //e.g 69173255004487680=Telanir

	AccountTags tags;
	
	List<ArcheToon> toons;
	List<String> ips;
	
	public ArcheAccount(int id) { //XXX
		this.id = id;
	}
}
