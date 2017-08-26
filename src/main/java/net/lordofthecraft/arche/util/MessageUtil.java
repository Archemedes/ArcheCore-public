package net.lordofthecraft.arche.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import static org.bukkit.ChatColor.*;

import java.util.Arrays;

/**
 * Set of utility methods to quickly retrieve often-used BaseComponents compositions
 * or convenience methods making the spigot BaseComponent API slightly more bearable
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

	public static void send(BaseComponent[] m, CommandSender sender) {
		if(sender instanceof Player) ((Player) sender).spigot().sendMessage(m);
		else Arrays.stream(m).forEach(o->sender.sendMessage(o.toPlainText()));
	}

	public static void send(BaseComponent m, CommandSender sender) {
		if(sender instanceof Player) ((Player) sender).spigot().sendMessage(m);
		else sender.sendMessage(m.toPlainText());
	}

}
