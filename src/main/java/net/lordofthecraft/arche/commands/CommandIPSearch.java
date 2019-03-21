package net.lordofthecraft.arche.commands;

import static org.bukkit.ChatColor.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.bukkit.util.Run;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import lombok.val;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;

import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.command.annotate.Arg;
import net.lordofthecraft.arche.command.annotate.Cmd;
import net.lordofthecraft.arche.interfaces.Account;

public class CommandIPSearch extends CommandTemplate {
	ArcheCore plugin = ArcheCore.getPlugin();
	
	@Cmd("Find player names using an IP")
	public void search(@Arg("ip") String ip) {
		msg(GOLD + " Looking for matching players for the ip " + RESET + ip);
		var ips = Collections.singleton(ip);
		Run.as(plugin).async(()-> uuids(ips)).then(uuids->callback(null, uuids));
	}
	
	@Cmd("Find alts based on a alias")
	public void alts(@Arg("player") UUID u) {
		msg(GOLD + " Looking for matching players for the user " + RESET + plugin.getPlayerNameFromUUID(u));
		var aah = plugin.getAccountHandler();
		aah.loadAccount(u).then(acc->{
			val ips = acc.getIPs();
			Run.as(plugin).async(()-> uuids(ips)).then(uuids-> callback(acc, uuids));
		});
	}
	
	@Cmd("Find minecraft toons connected to each other through IP")
	public void connections(@Arg("player") UUID u) {
		msg(GOLD + " Looking for matching players for the user " + RESET + plugin.getPlayerNameFromUUID(u));
		var aah = plugin.getAccountHandler();
		aah.loadAccount(u).then(acc->{
			val ips = acc.getIPs();
			Run.as(plugin).async(()->connections(ips)).then(mm->this.callback(acc, mm));
		});
	}
	
	private Multimap<String, UUID> connections(Set<String> ips){
		Multimap<String, UUID> result = MultimapBuilder.hashKeys().hashSetValues().build();
		
		for(var ip : ips) {
			var uuids = uuids(Collections.singleton(ip));
			result.putAll(ip, uuids);
		}
		
		return result;
	}
	
	private Set<UUID> uuids(Set<String> ips){
		if(ips.isEmpty()) return Collections.emptySet();
		Set<UUID> result = new HashSet<>();

		String clause = "WHERE " + IntStream.range(0, ips.size())
			.mapToObj($->"ip_address=?")
			.collect(Collectors.joining(" OR "));
		String stat = "SELECT player FROM playeraccounts WHERE account_id_fk IN (SELECT account_id_fk FROM account_ips " + clause + ")";
		try(var c = ArcheCore.getSQLControls().getConnection(); var ps = c.prepareStatement(stat)){
			int i = 0;
			for(String ip : ips) ps.setString(++i, ip);
			var rs = ps.executeQuery();
			while(rs.next()) {
				UUID u = UUID.fromString(rs.getString(1));
				result.add(u);
			}
			rs.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private void callback(Account acc, Multimap<String, UUID> linkeduuids) {
		new ChatBuilder().append("Query Found the Following Matches: ").color(GRAY).newline()
		.append("green").color(GREEN).append( " names are linked to account ").color(DARK_AQUA).append('#').color(DARK_GRAY).append(acc.getId())
		.send(getSender());
		
		for(var ip : linkeduuids.keySet()) {
			var cb = new ChatBuilder(ip).color(WHITE).append(" → ").color(DARK_AQUA).bold();
			boolean comma = false;
			for(var uuid : linkeduuids.get(ip)) {
				if(comma) cb.append(", ").color(GRAY);
				else comma = true;
				
				String name = ArcheCore.getControls().getPlayerNameFromUUID(uuid);
				cb.append(name);
				if(acc.getUUIDs().contains(uuid)) cb.color(GREEN);
				else cb.color(RED);
			}
		}
		
		
	}

	private void callback(Account acc, Set<UUID> uuids) {
		var cb = new ChatBuilder().append("Query Found the Following Matches: ").color(GRAY);

		if(acc != null) {
			cb.newline().append("green").color(GREEN).append( " names are linked to account ").color(DARK_AQUA).append('#').color(DARK_GRAY).append(acc.getId());
		}

		cb.send(getSender());
		
		new ChatBuilder().append("Query Found the Following Matches: ").color(DARK_AQUA).send(getSender());
		for(UUID uuid : uuids) {
			String name = ArcheCore.getControls().getPlayerNameFromUUID(uuid);
			cb = new ChatBuilder().append(name);
			if(acc == null) cb.color(AQUA);
			else if(acc.getUUIDs().contains(uuid)) cb.color(GREEN);
			else cb.color(RED);

			cb.append(" (").color(GRAY).append(uuid).append(")")
			.send(getSender());
		}
	}
}
