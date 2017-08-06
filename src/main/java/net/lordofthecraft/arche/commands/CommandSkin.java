package net.lordofthecraft.arche.commands;

import java.io.UnsupportedEncodingException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.help.ArcheMessage;
import net.lordofthecraft.arche.interfaces.ChatMessage;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;

public class CommandSkin implements CommandExecutor {
	final private ArcheCore plugin;
	final private SkinCache cache;
	
	public CommandSkin(ArcheCore plugin) {
		this.plugin = plugin;
		this.cache = plugin.getSkinCache();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("Generic no consoles allowed message");
			return true;
		}

		Player p = (Player) sender;

		if(args.length == 0 || arg(args[0],"help")) {
			
			help(p, "list", "Shows a list of all stored skins, highlighting the one used for your Persona");
			help(p, "use [id]", "Use the skin saved in slot [id]");
			help(p, "clear", "Stop using a stored skin for your current Persona");
			help(p, "store [id] [name]", "Store a skin in slot [id] with a given name.");
			help(p, "delete [id]", "Delete a skin from your stored skins.");
			
			p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC + "You are allowed to store" + maxAllowed(p) + " skins.");
			return true;
		} else if( arg(args[0], "view", "list", "show")) {
			p.sendMessage(ChatColor.AQUA + "Now showing you your personal skin storage:");
			for(int i = 1; i <= maxAllowed(p); i++) {
				ChatMessage msg = ArcheMessage.create("[" + i + "]").applyChatColor(ChatColor.AQUA);
		
				ArcheSkin sk = cache.getSkinAtSlot(p.getUniqueId(), i);
				if(sk == null) msg.addLine(ChatColor.GRAY + "" + ChatColor.ITALIC + "Empty...");
				else {
					msg.addLine(ChatColor.GOLD + sk.getName());
					Persona pers = plugin.getPersonaHandler().getPersona(p);
					if(cache.getSkinFor(pers) == sk) msg.addLine(ChatColor.LIGHT_PURPLE + " [in use]");
					msg.setClickEvent(ChatBoxAction.RUN_COMMAND, "/skin apply " + i);
					msg.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to apply this skin");
				}
				
				msg.sendTo(p);
			}
			return true;
		} else if(arg(args[0],"delete","remove","del","rm")) {
			try {
				Persona pers = plugin.getPersonaHandler().getPersona(p);
				if(pers != null) cache.clearSkin(pers);
				
				int i = Integer.parseInt(args[1]);
				boolean result = cache.removeSkin(p.getUniqueId(), i);
				if(result)p.sendMessage(ChatColor.GOLD + "successfully cleared the skin file in slot: " + ChatColor.RESET + i);
				else p.sendMessage(ChatColor.DARK_PURPLE + "There was no skin to be removed in that slot!");
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				return false;
			}
		} else if(arg(args[0],"save","store")) {
			try {
				int i = Integer.parseInt(args[1]);
				if(i < 1 || i > maxAllowed(p)) {
					p.sendMessage(ChatColor.RED + "That is not a valid skin slot");
					return true;
				}
				
				String name = args[2];
				cache.storeSkin(p, i, name);
				
				p.sendMessage(ChatColor.GOLD + "Stored your current skin successfully under slot: " + ChatColor.RESET + i);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				return false;
			} catch (UnsupportedEncodingException | ParseException e) {
				plugin.getLogger().severe("Failed parsing results. Mojang Likely changed its API response format. Error message: " + e.getMessage() );
			}
			return true;
		} else {
			Persona pers = plugin.getPersonaHandler().getPersona(p);
			if(pers == null) {
				p.sendMessage(ChatColor.RED + "need to be playing a valid Persona to do this!");
				return true;
			}
			
			if(arg(args[0], "apply", "use")) {
				try {
					int i = Integer.parseInt(args[1]);
					boolean success = cache.applySkin(pers, i);
					if(success) {
						p.sendMessage(ChatColor.GOLD + "We have now applied stored skin: " + i);
						cache.refreshPlayer(p);
					} else { p.sendMessage(ChatColor.RED + "No skin stored under : " + i); }
				} catch(NumberFormatException e) { return false; }
			} else if (arg(args[0], "clear")) {
				boolean cleared = cache.clearSkin(pers);
				if(cleared)p.sendMessage(ChatColor.GOLD + "You are no longer using a stored skin on " + ChatColor.RESET + pers.getName());
				else p.sendMessage(ChatColor.DARK_PURPLE + "This Persona was not using a stored skin!");
			}
		}
		return true;
	}
	
	private boolean arg(String arg, String... aliases) {
		String alc = arg.toLowerCase();
		for(String alias : aliases) if( alias.equals(alc)) return true;
		return false;
	}
	
	private void help(Player p, String subcmd, String desc) {
		p.sendMessage(ChatColor.GRAY + "/skin " + subcmd + ": " + ChatColor.GREEN + desc);
	}
	
	private int maxAllowed(Player p) {
		if(p.hasPermission("archecore.skincommand.4")) return 4;
		if(p.hasPermission("archecore.skincommand.3")) return 3;
		if(p.hasPermission("archecore.skincommand.2")) return 2;
		return 1;
	}
}
