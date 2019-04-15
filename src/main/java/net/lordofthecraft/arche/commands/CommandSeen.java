package net.lordofthecraft.arche.commands;


import static net.md_5.bungee.api.ChatColor.*;

import java.util.HashSet;
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
		var b = new ChatBuilder();

		b.append(account.getName()).color(DARK_AQUA).bold().append(" is ").reset().color(GRAY);
		long ls = account.getLastSeen();
		long elapsed = ls == 0? 0 : System.currentTimeMillis() - account.getLastSeen();
		elapsed -= (elapsed % 60000l); //this way it wont show seconds
		BaseComponent lastSeen = TimeUtil.printMillis(elapsed);

		if(canSeeOnline(s,account.getPlayer())) b.append("Online").color(GREEN);
		else b.append("Offline").color(DARK_PURPLE);
		b.append(" since ").color(GRAY).append(lastSeen).newline();

		if(account.hasForumId()) b.append("Forum Account: ").color(GRAY)
		.hover("Click to go to forum profile")
		.event(ClickEvent.Action.OPEN_URL, "https://lotc.co/profile/"+account.getForumId()+"--")
		.append(account.getForumId()).color(WHITE)
		.newline();

		long weekMs = account.getTimePlayedThisWeek() * 60 * 1000;
		if(weekMs > 0) b.append("Played ").color(GRAY).append(TimeUtil.printMillis(weekMs)).append(" in the last week.");

		for(var ps : account.getPersonas()) {
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
