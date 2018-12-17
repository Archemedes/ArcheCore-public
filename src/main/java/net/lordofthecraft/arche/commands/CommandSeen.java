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
import net.lordofthecraft.arche.command.annotate.Flag;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.util.ChatBuilder;
import net.lordofthecraft.arche.util.Hastebin;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.TimeUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import net.md_5.bungee.api.chat.BaseComponent;

public class CommandSeen extends CommandTemplate {

	private final Set<UUID> cd = new HashSet<>();
	
	@Flag(name = "m", description="Perform full Moderator printout.", permission="archecore.mod")
	public void invoke(CommandSender s, String someName) {
		validate(cooldown(), "This command has a 10s cooldown. Please wait a bit");
		UUID u = ArcheCore.getControls().getPlayerUUIDFromAlias(someName);
		validate(u != null, "We don't know anyone with the username " + RESET + someName);
		
		var aah = ArcheCore.getControls().getAccountHandler();
		aah.loadAccount(u).then(acc->{
			if(hasFlag("m")) {
				Bukkit.getScheduler().runTaskAsynchronously(ArcheCore.getPlugin(), ()->{
					String link = Hastebin.upload(this.printoutMod(acc));
					s.sendMessage(BLUE + "Your command result has been prepared into a hastebin prinout.");
					s.sendMessage(BLUE + "These expire quickly, so copy-paste for any logging purposes.");
					s.sendMessage(BLUE + "Also do not share any sensitive information contained therein.");
					s.sendMessage(link);
				});
			} else {
				printout(acc).send(s);
			}
		});
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
	
	private ChatBuilder printout(Account account) {
		var b = MessageUtil.builder();
		
		long ls = account.getLastSeen();
		long elapsed = ls == 0? 0 : System.currentTimeMillis() - account.getLastSeen();
		BaseComponent lastSeen = TimeUtil.printMillis(elapsed);
		
		Player p = account.getPlayer();
		if(p != null) b.append("Online").color(GREEN);
		else b.append("Offline").append(RED);
		b.append(" since ").color(GRAY).append(lastSeen).newline();
		
		long weekMs = account.getTimePlayedThisWeek() * 60 * 1000;
		if(weekMs > 0) b.append(" played ").append(TimeUtil.printMillis(weekMs)).append(" in the last week.").newline();
		
		b.append("Has the following personas:").color(BLUE).newline();
		for(var ps : account.getPersonas()) {
			b.append(ps.getName());
			if(ps.isCurrent()) b.color(GREEN).append(": Active persona!");
			else b.color(YELLOW).append(": Last seen ").color(GRAY).append(TimeUtil.printMillis(ps.getLastSeen())).append( " ago.");
			b.color(GRAY).newline();
			
		}
		
		return b;
	}
	
	private String printoutMod(Account account) {
		var b = new StringBuilder(1024);
		
		long ls = account.getLastSeen();
		long elapsed = ls == 0? 0 : System.currentTimeMillis() - account.getLastSeen();
		String lastSeen = TimeUtil.printMillisRaw(elapsed).toPlainText();
		
		Player p = account.getPlayer();
		if(p != null) b.append("Online");
		else b.append("Offline");
		b.append(" since ").append(lastSeen).append('\n');
		
		long weekMs = account.getTimePlayedThisWeek() * 60 * 1000;
		if(weekMs > 0) b.append(" played ").append(TimeUtil.printMillis(weekMs)).append(" in the last week.").append('\n');
		
		b.append("Personas: ").append('\n');
		for(var psx : account.getPersonas()) {
			var ps = (ArchePersona) psx;
			b.append(ps.getName());
			if(ps.isCurrent()) b.append(": active ");
			else b.append(": since ").append(TimeUtil.printBrief(ps.getLastSeen()));
			b.append(" at ").append(new WeakBlock(ps.getLocation()).toString()).append('\n');
		}
		
		b.append("UUIDs:").append('\n');
		account.getUUIDs().forEach(u->b.append(u.toString()).append('\n'));
		
		b.append("Aliases:").append('\n');
		account.getUsernames().forEach(u->b.append(u).append('\n'));
		
		b.append("IP Addresses:").append('\n');
		account.getIPs().forEach(u->b.append(u).append('\n'));
		
		
		return b.toString();
	}
	

}
