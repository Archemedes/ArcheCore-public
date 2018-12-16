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
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.TimeUtil;
import net.md_5.bungee.api.chat.BaseComponent;

public class CommandSeen extends CommandTemplate {

	private final Set<UUID> cd = new HashSet<>();
	
	@Flag(name = "m", description="Perform full Moderator printout.", permission="archecore.mod")
	public void invoke(CommandSender s, String someName) {
		validate(cooldown(), "This command has a 15s cooldown. Please wait a bit");
		UUID u = ArcheCore.getControls().getPlayerUUIDFromAlias(someName);
		validate(u != null, "We don't know anyone with the username " + RESET + someName);
		
		var aah = ArcheCore.getControls().getAccountHandler();
		aah.loadAccount(u).then(this::printout);
		
	}
	
	private boolean cooldown() {
		var s = getSender();
		if(s.hasPermission("archecore.mod")) return true;
		if(!(s instanceof Player)) return true;
		
		UUID u = ((Player)s).getUniqueId();
		if(cd.contains(u)) return false;
		
		cd.add(u);
		Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->cd.remove(u), 300l);
		return true;
	}
	
	private void printout(Account account) {
		long elapsed = System.currentTimeMillis() - account.getLastSeen();
		BaseComponent lastSeen = TimeUtil.printMillis(elapsed);
		
		var b = MessageUtil.builder();
		Player p = account.getPlayer();
		
		if(p != null) b.append("Online").color(GREEN);
		else b.append("Offline").append(RED);
		b.append(" Since ").color(GRAY).append(lastSeen);
		
		for(var ps : account.getPersonas()) {
			ps.
		}
	}

}
