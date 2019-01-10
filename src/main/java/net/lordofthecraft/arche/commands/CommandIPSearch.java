package net.lordofthecraft.arche.commands;

import static org.bukkit.ChatColor.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.val;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.command.annotate.Arg;
import net.lordofthecraft.arche.command.annotate.Cmd;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.Run;

public class CommandIPSearch extends CommandTemplate {
	ArcheCore plugin = ArcheCore.getPlugin();
	
	@Cmd("Find player names using an IP")
	public void search(@Arg("ip") String ip) {
		msg(GOLD + " Looking for matching players for the ip " + RESET + ip);
		var ips = Collections.singleton(ip);
		Run.as(plugin).async(()-> uuids(ips)).then(this::callback);
	}
	
	@Cmd("Find alts based on a alias")
	public void alts(@Arg("player") UUID u) {
		msg(GOLD + " Looking for matching players for the user " + RESET + plugin.getPlayerNameFromUUID(u));
		var aah = plugin.getAccountHandler();
		aah.loadAccount(u).then(acc->{
			val ips = acc.getIPs();
			Run.as(plugin).async(()-> uuids(ips)).then(this::callback);
		});
	}
	
	private Set<UUID> uuids(Set<String> ips){
		if(ips.isEmpty()) return Collections.emptySet();
		Set<UUID> result = new HashSet<>();

		String clause = "WHERE " + IntStream.range(0, ips.size())
			.mapToObj($->"ip_address=?")
			.collect(Collectors.joining(" OR "));
		String stat = "SELECT player FROM playeraccounts WHERE account_id_fk=(SELECT account_id_fk FROM account_ips " + clause + ")";
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
	
	private void callback(Set<UUID> uuids) {
		MessageUtil.builder().append("Query Found the Following Matches: ").color(GRAY).send(getSender());
		for(UUID uuid : uuids) {
			String name = ArcheCore.getControls().getPlayerNameFromUUID(uuid);
			MessageUtil.builder().append(name).color(AQUA)
			.append(" (").color(GRAY).append(uuid).append(")")
			.send(getSender());
		}
	}
}
