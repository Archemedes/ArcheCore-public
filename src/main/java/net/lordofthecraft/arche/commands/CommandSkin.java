package net.lordofthecraft.arche.commands;

import java.io.UnsupportedEncodingException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.parser.ParseException;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.skin.MojangCommunicator.MinecraftAccount;
import net.md_5.bungee.api.ChatColor;
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

		if(args.length == 0) {

		} else if( arg(args[0], "view", "list", "show")) {
			//TODO: List of stored skins, skin slots, and which slot is being used
		} else if(arg(args[0],"save","store")) {
			try {
				int i = Integer.parseInt(args[1]);
				cache.storeSkin(p, i);
				p.sendMessage(ChatColor.GOLD + "Stored your current skin successfully under slot: " + ChatColor.RESET + i);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				return false;
			} catch (UnsupportedEncodingException | ParseException e) {
				plugin.getLogger().severe("Failed parsing results. Mojang Likely changed its API response format. Error message: " + e.getMessage() );
			}
			return true;
		} else if(arg(args[0], "apply", "use")) {
			Persona pers = plugin.getPersonaHandler().getPersona(p);
			if(pers == null) {
				p.sendMessage(ChatColor.RED + "need to be playing a valid Persona to do this!");
			} else try {
				int i = Integer.parseInt(args[1]);
				boolean success = cache.applySkin(pers, i);
				if(success) p.sendMessage(ChatColor.GOLD + "We have now applied stored skin: " + i);
			} catch(NumberFormatException e) { return false; }
			return true;
		}
		
		return true;
	}
	
	private boolean arg(String arg, String... aliases) {
		String alc = arg.toLowerCase();
		for(String alias : aliases) if( alias.equals(alc)) return true;
		return false;
	}
}
