package net.lordofthecraft.arche.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.help.ArcheMessage;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.WhyPermissionHandler;

public class CommandPersonaPermissions implements CommandExecutor{

	private WhyPermissionHandler handler;

	public CommandPersonaPermissions(WhyPermissionHandler handler){
		this.handler = handler;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0)
			return sendUsage(sender);
		
		Persona pers = null;
		if (args[0].equalsIgnoreCase("view")
				|| args[0].equalsIgnoreCase("add")
				|| args[0].equalsIgnoreCase("remove")
				&& args.length > 1){
			pers = CommandUtil.personaFromArg(args[1]);
		}
		
		switch (args[0].toLowerCase()){
		case "info":
			return whyInfo(sender);
		case "view":
			if (args.length != 2 || pers == null){
				sender.sendMessage("Usage: /perspex view {Player}");
				return true;
			}
			return viewPermissions(sender, pers);
		case "add":
			if (args.length != 3 || pers == null){
				sender.sendMessage("Usage: /perspex add {Player} {Permission}");
				return true;
			}
			return addPermission(sender, pers, args[2]);
		case "remove":
			if (args.length != 3 || pers == null){
				sender.sendMessage("Usage: /perspex remove {Player} {Permission}");
				return true;
			}
			return removePermission(sender, pers, args[2]);
		default:
			return sendUsage(sender);
		}
	}

	private boolean viewPermissions(CommandSender sender, Persona pers) {
		final Player p = pers.getPlayer();
		final Persona[] personas = ArcheCore.getControls().getPersonaHandler().getAllPersonas(p);
		sender.sendMessage(p.getName()+"'s permissions");
		Persona holder;
		for (int i = 0; i < personas.length; ++i){
			holder = personas[i];
			sender.sendMessage(ChatColor.BLUE+"- "+i+" - "+holder.getChatName()+"\n\n");
			for (String ss : handler.getPermissions(holder))
				sender.sendMessage(ChatColor.DARK_AQUA+" * "+ss);
		}
		return true;
	}
	
	private boolean addPermission(CommandSender sender, Persona pers, String string) {
		sender.sendMessage(ChatColor.DARK_AQUA+"Successfully added "+ChatColor.GOLD
				+string+" to "+ChatColor.GOLD
				+pers.getPlayerName()+ChatColor.DARK_AQUA
				+" on persona # "+ChatColor.GOLD+pers.getId()+1);
		handler.addPermission(pers, string);
		return true;
	}
	
	private boolean removePermission(CommandSender sender, Persona pers, String string) {
		sender.sendMessage(ChatColor.DARK_AQUA+"Successfully removed "+ChatColor.GOLD
				+string+ChatColor.DARK_AQUA+" from "+ChatColor.GOLD
				+pers.getPlayerName()+ChatColor.DARK_AQUA+" on persona # "+ChatColor.GOLD
				+pers.getId()+1);
		handler.removePermission(pers, string);
		return true;
	}

	private boolean sendUsage(CommandSender sender) {
		String g = ChatColor.BLUE+"";
		String i = ChatColor.DARK_AQUA+""+ChatColor.ITALIC+"";
		String output = ChatColor.DARK_AQUA+".:: Why's Persona PEx ::.\n"
				+ i+"[M] /perspex info: "+g+"Curious about this feature? Start here!\n"
				+ i+"[M] /perspex view {player}: "+g+"View the permission sets of the {player}'s personas.\n"
				+ i+"[M] /perspex add {player} {permission}: "+g+"Add {permission} to the {player}'s current persona.\n"
				+ i+"[M] /perspex remove {player} {permission}: "+g+"Remove {permission} from the {player}'s current persona.\n"
				+ g+"[M] You can use [player]@[personaid] to modify a different Persona";
		sender.sendMessage(output);
		return true;
	}

	private boolean whyInfo(CommandSender sender) {
		if (!(sender instanceof Player)){
			ArcheMessage message = new ArcheMessage("");
			message.addLine("Welcome to Why's Persona Permisisons.\n").applyChatColor(ChatColor.DARK_AQUA);
			message.addLine("What is it? ").applyChatColor(ChatColor.GOLD).setBold();
			message.addLine("This is a new feature of ArcheCore, built upon the Persona system. What this allows "
					+ "is for permissions to be added on a per-persona basis. Permissions can be added to a single "
					+ "persona and will be removed when the user switches their persona, and added when they "
					+ "switch back.\n").applyChatColor(ChatColor.DARK_AQUA);
			message.addLine("How does it work? ").setBold().applyChatColor(ChatColor.GOLD);
			message.addLine("When a player is assigned a permission node via this plugin if they are currently on "
					+ "the specific persona then they will have the nodes applied to them directly. Otherwise, when "
					+ "they switch to said persona they will gain the nodes. When the persona is permakilled the nodes "
					+ "are deleted.\n").applyChatColor(ChatColor.DARK_AQUA);
			message.sendTo((Player) sender); 
		} else {
			sender.sendMessage("Error: This can only be performed from ingame.");
		}
		return true;
	}


}
