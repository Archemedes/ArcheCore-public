package net.lordofthecraft.arche.commands;

import com.google.common.collect.Sets;

import co.lotc.core.util.MessageUtil;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class CommandSkin implements CommandExecutor {
	final private ArcheCore plugin;
	final private SkinCache cache;

	final private Set<UUID> cooldowns = Sets.newHashSet();

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
		int maxAllowed = maxAllowed(p);

		if(args.length == 0 || arg(args[0],"help")) {
			help(p, "list", "Lists your stored skins.");
			help(p, "use [id]", "Use the skin saved in slot [id].");
			help(p, "clear", "Stop using a stored skin for this Persona.");
			help(p, "store [id] [label]", "Store a skin in slot [id] with any label.");
			help(p, "delete [id]", "Delete a skin from your stored skins.");

			p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC + "\nYou are allowed to store " + maxAllowed + " skins.");
			return true;
		} else if(arg(args[0],"delete","remove","del","rm")) {
			try {
				Persona pers = plugin.getPersonaHandler().getPersona(p);
				if(pers != null) cache.clearSkin(pers);

				int i = Integer.parseInt(args[1]);
				boolean result = cache.removeSkin(p.getUniqueId(), i);
				if(result)p.sendMessage(ChatColor.GOLD + "successfully cleared the skin file in slot: " + ChatColor.RESET + i);
				else p.sendMessage(ChatColor.DARK_PURPLE + "There was no skin to be removed in that slot!");
				return true;
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				return false;
			}
		}

		if (maxAllowed == 0) {
			BaseComponent msg = new TextComponent("Please purchase a VIP rank to use automatic skin switching. Click here to visit the store.");
			msg.setColor(ChatColor.GREEN.asBungee());
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/store"));
			msg.setHoverEvent(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, ChatColor.GRAY + "" + ChatColor.ITALIC + "Click here to visit the store."));
			p.spigot().sendMessage(msg);
			return true;
		}
		
		if( arg(args[0], "view", "list", "ls", "show")) {
			p.sendMessage(ChatColor.AQUA + "Now showing you your personal skin storage:");
			for(int i = 1; i <= maxAllowed; i++) {
				BaseComponent msg = new TextComponent("[" + i + "] ");
				msg.setColor(ChatColor.AQUA.asBungee());

				ArcheSkin sk = cache.getSkinAtSlot(p.getUniqueId(), i);
				if(sk == null) msg.addExtra(ChatColor.GRAY + "" + ChatColor.ITALIC + "Empty...");
				else {
					Persona pers = plugin.getPersonaHandler().getPersona(p);
                    boolean current = pers.getSkin() == sk;
                    msg.addExtra((current ? ChatColor.LIGHT_PURPLE : ChatColor.GOLD) + sk.getName());
					if(current) msg.setHoverEvent(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, ChatColor.LIGHT_PURPLE + "Currently in use for " + pers.getName() + "."));
					else {
						msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skin apply " + i));
						msg.setHoverEvent(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, ChatColor.GRAY + "" + ChatColor.ITALIC + "Click to apply this skin."));
					}
				}

				p.spigot().sendMessage(msg);
			}
			return true;
		} else if(arg(args[0],"save","store")) {
			try {
				int i = Integer.parseInt(args[1]);
				if(i < 1 || i > maxAllowed) {
					p.sendMessage(ChatColor.RED + "That is not a valid skin slot");
					return true;
				}

				Persona pers = plugin.getPersonaHandler().getPersona(p);
				ArcheSkin other = pers.getSkin();
				if(other != null) {
					p.sendMessage(ChatColor.RED + "Can't store skins while using a custom skin!");
					return true;
				}

				String name = args[2];
				int index = cache.storeSkin(p, i, name);
				if(index > 0) {
					p.sendMessage("You already have this skin saved in slot: " + ChatColor.RESET + index);
					return true;
				}

				p.sendMessage(ChatColor.GOLD + "Stored your current skin successfully under slot: " + ChatColor.RESET + i);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				return false;
			} catch (UnsupportedEncodingException | ParseException e) {
				CoreLog.severe("Failed parsing results. Mojang Likely changed its API response format. Error message: " + e.getMessage() );
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
					if(isCoolingDown(p)) {
						p.sendMessage(ChatColor.RED + "You are applying new skins too often! Be decisive!");
						return true;
					}
					int i = Integer.parseInt(args[1]);
					if(i < 1 || i > maxAllowed) {
						p.sendMessage(ChatColor.RED + "That is not a valid skin slot");
						return true;
					}

					boolean success = cache.applySkin(pers, i);
					if(success) {
						p.sendMessage(ChatColor.GOLD + "We have now applied stored skin: " + i);
						cache.refreshPlayer(p);
						addCooldown(p);
					} else { p.sendMessage(ChatColor.RED + "No skin stored under : " + i); }
				} catch(NumberFormatException | IndexOutOfBoundsException e) { return false; }
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
		if(p.hasPermission("archecore.command.skin.4")) return 4;
		else if(p.hasPermission("archecore.command.skin.3")) return 3;
		else if(p.hasPermission("archecore.command.skin.2")) return 2;
		else if (p.hasPermission("archecore.command.skin.1")) return 1;
		return 0;
	}

	private boolean isCoolingDown(Player p) {
		return cooldowns.contains(p.getUniqueId());
	}

	private void addCooldown(Player p) {
		if(p.hasPermission("archecore.command.skin.nocooldown")) return;
		cooldowns.add(p.getUniqueId());
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, ()->{cooldowns.remove(p.getUniqueId());}, 5*60*20);

	}
}
