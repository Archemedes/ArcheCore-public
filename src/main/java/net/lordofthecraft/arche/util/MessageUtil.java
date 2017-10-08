package net.lordofthecraft.arche.util;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.GRAY;

/**
 * Set of utility methods to quickly retrieve often-used BaseComponents compositions
 * or convenience methods making the spigot ChatMessage API slightly more bearable
 * @author Sporadic
 */
public class MessageUtil {
	
	public static net.md_5.bungee.api.ChatColor convertColor(org.bukkit.ChatColor color){
		return net.md_5.bungee.api.ChatColor.values()[color.ordinal()]; //This works since the order is the same
	}
	
	public static org.bukkit.ChatColor convertColor(net.md_5.bungee.api.ChatColor color){
		return ChatColor.values()[color.ordinal()]; //This works since the order is the same
	}
	
	public static BaseComponent setColor(BaseComponent comp, ChatColor color) {
		comp.setColor(convertColor(color));
		return comp;
	}

	public static BaseComponent legacyText(String legacy) {
		return legacyAdd(new TextComponent(), legacy);
	}
	
	public static BaseComponent legacyAdd(BaseComponent m, String toAdd) {
		Arrays.stream(TextComponent.fromLegacyText(toAdd))
			.forEach(bc->m.addExtra(bc));
		return m;
	}
	
	public static HoverEvent hoverEvent(String text) {
		return hoverEvent(HoverEvent.Action.SHOW_TEXT, text);
	}
	
	public static HoverEvent hoverEvent(HoverEvent.Action action, String text) {
		return new HoverEvent(action, new BaseComponent[]{new TextComponent(text)});
	}
	
	public static BaseComponent CommandButton(String text, String cmd) {
		return CommandButton(text, cmd, null, GRAY, BLUE);
	}
	
	public static BaseComponent CommandButton(String text, String cmd, String hover) {
		return CommandButton(text, cmd, hover, GRAY, BLUE);
	}
	
	public static BaseComponent CommandButton(String text, String cmd, ChatColor textcolor, ChatColor rimcolor) {
		return CommandButton(text, cmd, null, textcolor, rimcolor);
	}
		
	public static BaseComponent CommandButton(String text, String cmd, String hover, ChatColor textcolor, ChatColor rimcolor) {
		net.md_5.bungee.api.ChatColor txt_bungee = convertColor(textcolor);
		net.md_5.bungee.api.ChatColor rim_bungee = convertColor(rimcolor);
		
		TextComponent tc = new TextComponent();
		tc.setColor(rim_bungee);
		tc.addExtra("[");
		TextComponent sub = new TextComponent(text);
		sub.setItalic(true);
		sub.setColor(txt_bungee);
		sub.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
		if(hover != null) {
			BaseComponent[] hoe = new ComponentBuilder(hover)
			.color(convertColor(ChatColor.GRAY)).italic(true)
			.create();
			sub.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoe));
		}
		tc.addExtra(sub);
		tc.addExtra("]");
		
		return tc;
	}
	
	public static BaseComponent ArcheHelpButton(String topic) {
		return CommandButton(topic, "/archehelp " + topic, "Click for help");
	}

	public static void addNewlines(BaseComponent x) {
		breakUp(x, 0, null);
	}
	
	private static int breakUp(BaseComponent x, int lineLength, BaseComponent prev) {
		if(x instanceof TextComponent) {
			TextComponent tc = (TextComponent) x;
			String text = tc.getText();
			
			if(text.trim().length() == 0) {
				lineLength += text.length();
				if(lineLength >= 60) {
					tc.setText("\n");
					lineLength = 0;
				} else if(lineLength == 0) {
					tc.setText("");
				}
			} else {
				String[] parts = text.split(" ");
				StringBuilder recoveredParts = new StringBuilder(text.length() + 4);
				boolean first = true;

				for(String part : parts) {
					if(lineLength+part.length() >= 60 && part.length() != 1) {
						if(prev != null && prev instanceof TextComponent) {
							TextComponent prev_tc = (TextComponent) prev;
							String txt = prev_tc.getText();
							if(txt.length() <2) {
								prev_tc.setText('\n' + txt);
							} else {
								recoveredParts.append('\n');
							}
						} else {
							recoveredParts.append('\n');
						}
						lineLength = 0;
					} else if(!first) {
						recoveredParts.append(' ');
						lineLength++;
					}
					
					if(first) {
						first = false;
						prev = null;
					}
					
					lineLength += part.length();
					recoveredParts.append(part);
				}
				if(text.charAt(text.length() - 1) == ' ')
					recoveredParts.append(' ');
				tc.setText(recoveredParts.toString());
	
			}
		} else {
			lineLength += x.toPlainText().length();
		}
		
		prev = null;
		if(x.getExtra() != null) for(BaseComponent o : x.getExtra()) {
			lineLength = breakUp(o, lineLength, prev);
			prev = o;
		}
		
		return lineLength;
	}
	
	public static void send(BaseComponent[] m, CommandSender sender) {
		if(sender instanceof Player) ((Player) sender).spigot().sendMessage(m);
		else Arrays.stream(m).forEach(o->sender.sendMessage(o.toPlainText()));
	}

	public static void send(BaseComponent m, CommandSender sender) {
		if(sender instanceof Player) ((Player) sender).spigot().sendMessage(m);
		else sender.sendMessage(m.toPlainText());
	}

    public static String identifyPersona(OfflinePersona p) {
        return p.getPersonaId() + ":" + p.getName() + " (" + p.getPlayerName() + ")";
    }

}
