package net.lordofthecraft.arche.commands;


import static net.md_5.bungee.api.ChatColor.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.util.TimeUtil;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

public class CommandSeen extends CommandTemplate {

	private final Set<UUID> cd = new HashSet<>();

	public void invoke(CommandSender s, UUID u) {
		validate(cooldown(), "This command has a 10s cooldown. Please wait a bit");
		var ac = ArcheCore.getControls();
		validate(!ac.getKnownAliases(u).isEmpty(), "Player has not logged in to this server");
		
		var aah = ac.getAccountHandler();
		aah.loadAccount(u).then(acc->printout(s,acc,u).send(s));
	}

	private boolean cooldown() {
		var s = getSender();
		if(s.hasPermission("archecore.mod")) return true;
		if(!(s instanceof Player)) return true;

		UUID u = ((Player)s).getUniqueId();
		if(cd.contains(u)) return false;

		cd.add(u);
		Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->cd.remove(u), 200l);
		return true;
	}

	private ChatBuilder printout(CommandSender s, Account account, UUID calledBy) {
		var b = new ChatBuilder();
		String mainName = ArcheCore.getControls().getPlayerNameFromUUID(calledBy);
		
		b.append(mainName).color(DARK_AQUA).bold().append(" is ").reset().color(GRAY);
		long ls = account.getLastSeen();
		long elapsed = ls == 0? 0 : System.currentTimeMillis() - account.getLastSeen();
		elapsed -= (elapsed % 60000l); //this way it wont show seconds
		BaseComponent lastSeen = TimeUtil.printMillis(elapsed);
		Player them = account.getPlayer();
		
		if(s.hasPermission("archecore.mod") && them != null && ArcheCore.getControls().isAfk(them))
			b.append("AFK").color(DARK_GRAY);
		else if(canSeeOnline(s,them))
			b.append("Online").color(GREEN);
		else
			b.append("Offline").color(DARK_PURPLE);
		
		b.append(" since ").color(GRAY).append(lastSeen).newline();

		if(s.hasPermission("archecore.mod")) {
			var aka = getAka(account);
			aka.remove(mainName);
			if(!aka.isEmpty()) {
				b.append("AKA: ").color(GRAY);
				boolean j = true;
				for(String a : aka) {
					if(j) j = false;
					else b.append(", ").color(WHITE);
					b.append(a).color(YELLOW);
				}
				b.newline();
			}
		}
		
		if(account.hasForumId()) b.append("Forum Account: ").color(GRAY)
		.hover("Click to go to forum profile")
		.event(ClickEvent.Action.OPEN_URL, "https://lotc.co/profile/"+account.getForumId()+"--")
		.append(account.getForumId()).color(WHITE)
		.newline();

		long weekMs = account.getTimePlayedThisWeek() * 60 * 1000;
		if(weekMs > 0) b.append("Played ").color(GRAY).append(TimeUtil.printMillis(weekMs)).append(" in the last week.");

		for(var ps : account.getPersonas()) {
			if(ps.getPlayerUUID().equals(calledBy) || s.hasPermission("archecore.mod.persona")) {
				b.newline().append(" - ").color(GRAY).append(ps.getName());
	
				if(ps.isCurrent()) b.color(GREEN).append(": Active persona!");
				else {
					long since = System.currentTimeMillis() - ps.getLastSeen();
					
					//Don't make time needlessly precise
					if(since > TimeUnit.HOURS.toMillis(1)) since -= (since % (60l*60l*1000l));
					else since -= (since % 60000l);
					
					b.color(YELLOW).append(": Seen ").color(GRAY);
					if(ps.getLastSeen() == 0) b.append("never").color(WHITE);
					else if(since == 0) b.append("just now").color(WHITE);
					else b.append(TimeUtil.printMillis(since)).append( " ago");
				}
				b.color(GRAY);
			}
		}

		return b;
	}
	
	private List<String> getAka(Account acc){
		int maxNamesPerToon = 2;
		List<String> result = new ArrayList<>();
		for(UUID u : acc.getUUIDs()) {
			var aliases = ArcheCore.getControls().getKnownAliases(u);
			int sz = aliases.size();
			if(sz <= maxNamesPerToon) {
				result.addAll(aliases);
			} else for(int i = sz-maxNamesPerToon; i<sz; i++) {
				result.add(aliases.get(i));
			}
		}
		
		return result;
	}

	private boolean canSeeOnline(CommandSender s, Player p) {
		if(p == null) return false;
		if(!(s instanceof Player)) return true;
		if(s.hasPermission("archecore.mod")) return true;

		Player o = (Player) s;
		return o.canSee(p);
	}
}
