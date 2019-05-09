package net.lordofthecraft.arche.commands;

import static net.md_5.bungee.api.ChatColor.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import lombok.val;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;

public class CommandNew extends CommandTemplate {
	private static Set<UUID> NEWBIE_LISTENERS = new HashSet<>();
	
	public void invoke(CommandSender s, Persona newbie) {
		
		newbie.setNewbie(!newbie.isNewbie());
		String ourmsg,theirmsg;
		
		if(newbie.isNewbie()) {
			ourmsg = "Persona no longer marked as new: ";
			theirmsg = "You were marked as not new by ";
		} else {
			ourmsg = "Persona now marked as new: ";
			theirmsg = "You were marked as new by ";
		}
		
		msg(LIGHT_PURPLE + ourmsg + WHITE + newbie.getPlayerName());
		Player p = newbie.getPlayer();
		if(p != null) p.sendMessage(LIGHT_PURPLE + theirmsg + WHITE + s.getName());
	}

	@Cmd("Lists new players online")
	public void list() {
		msg(LIGHT_PURPLE + "New players online:");
		val cb = new ChatBuilder();
		
		Bukkit.getOnlinePlayers().stream()
		.map(ArcheCore::getPersona)
		.filter(Objects::nonNull)
		.filter(Persona::isNewbie)
		.map(Persona::getPlayerName)
		.forEach(n-> cb.append(" - ").color(GRAY).append(n).color(WHITE)
				.command("/tp " + n).hover(GRAY+""+ITALIC+"Click to teleport"));
	}
	
	@Cmd("Notifies when a new player joins the server.")
	public void notify(Player p) {
		if(willNotify(p)) {
			NEWBIE_LISTENERS.remove(p.getUniqueId());
			msg(LIGHT_PURPLE + "Will now notify you when new players join");
		} else {
			NEWBIE_LISTENERS.add(p.getUniqueId());
			msg(DARK_PURPLE + "Will no longer notify you when new players join");
		}
	}
	
	@Cmd("Lists staff who are notified on new player login.")
	public void notifyList() {
		msg(LIGHT_PURPLE + "Staff listening to new player logins:");
		NEWBIE_LISTENERS.forEach(u->msg(GRAY + " - " + WHITE + ArcheCore.getControls().getPlayerNameFromUUID(u)));
	}
	
	private boolean willNotify(Player p) {
		return NEWBIE_LISTENERS.contains(p.getUniqueId());
	}
	
	public void notifyListenersIfNew(Player p) {
		if(isNew(p) && !NEWBIE_LISTENERS.isEmpty()) {
			val cb = new ChatBuilder("The new player").color(LIGHT_PURPLE)
					.command("/tp " + p.getName()).hover(GRAY+""+ITALIC+"Click to teleport")
					.append(p.getName()).color(WHITE).append(" has joined.").color(LIGHT_PURPLE)
					.build();
			CommandNew.NEWBIE_LISTENERS.stream()
			.map(Bukkit::getPlayer)
			.filter(Objects::nonNull)
			.forEach(x->x.spigot().sendMessage(cb));
		}
	}
	
	private boolean isNew(Player x) {
		Persona ps = ArcheCore.getPersona(x);
		return ps != null && ps.isNewbie();
	}
}
