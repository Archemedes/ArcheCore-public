package net.lordofthecraft.arche.commands;

import static org.bukkit.ChatColor.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.util.ChatBuilder;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.TimeUtil;
import net.md_5.bungee.api.chat.BaseComponent;

public class CommandSeen extends CommandTemplate {

	private final Set<UUID> cd = new HashSet<>();
	
	public void invoke(CommandSender s, UUID u) {
		validate(cooldown(), "This command has a 10s cooldown. Please wait a bit");
		
		var aah = ArcheCore.getControls().getAccountHandler();
		aah.loadAccount(u).then(acc->printout(s,acc).send(s));
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
	
	private ChatBuilder printout(CommandSender s, Account account) {
		var b = MessageUtil.builder();
		
		
		b.append(account.getName()).color(DARK_AQUA).bold().append(" is ").reset().color(GRAY);
		long ls = account.getLastSeen();
		long elapsed = ls == 0? 0 : System.currentTimeMillis() - account.getLastSeen();
		BaseComponent lastSeen = TimeUtil.printMillis(elapsed);
		
		if(canSeeOnline(s,account.getPlayer())) b.append("Online").color(GREEN);
		else b.append("Offline").append(RED);
		b.append(" since ").color(GRAY).append(lastSeen).newline();
		
		long weekMs = account.getTimePlayedThisWeek() * 60 * 1000;
		if(weekMs > 0) b.append("Played ").color(GRAY).append(TimeUtil.printMillis(weekMs)).append(" in the last week.");
		
		
		for(var ps : account.getPersonas()) {
			b.newline().append(" - ").color(GRAY).append(ps.getName());
			
			if(ps.isCurrent()) b.color(GREEN).append(": Active persona!");
			else {
				long since = System.currentTimeMillis() - ps.getLastSeen();
				b.color(YELLOW).append(": Last seen ").color(GRAY);
				if(ps.getLastSeen() == 0) b.append("never").color(WHITE);
				else b.append(TimeUtil.printMillis(since)).append( " ago");
			}
			b.color(GRAY);
			
		}
		
		return b;
	}
	
	private boolean canSeeOnline(CommandSender s, Player p) {
		if(p == null) return false;
		if(!(s instanceof Player)) return true;
		if(s.hasPermission("archecore.mod")) return true;
		
		Player o = (Player) s;
		return o.canSee(p);
	}
}
