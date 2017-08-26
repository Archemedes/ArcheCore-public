package net.lordofthecraft.arche.util;

import org.bukkit.ChatColor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import static org.bukkit.ChatColor.*;

public class MessageUtil {
	
	public static net.md_5.bungee.api.ChatColor convertColor(org.bukkit.ChatColor color){
		return net.md_5.bungee.api.ChatColor.values()[color.ordinal()]; //This works since the order is the same
	}
	
	public static org.bukkit.ChatColor convertColor(net.md_5.bungee.api.ChatColor color){
		return ChatColor.values()[color.ordinal()]; //This works since the order is the same
	}
	
	public static void setColor(BaseComponent comp, ChatColor color) {
		comp.setColor(convertColor(color));
	}
	
	public static BaseComponent CommandButton(String text, String cmd) {
		return CommandButton(text, cmd, GRAY, BLUE);
	}
	
	public static BaseComponent CommandButton(String text, String cmd, ChatColor textcolor, ChatColor rimcolor) {
		net.md_5.bungee.api.ChatColor txt_bungee = convertColor(textcolor);
		net.md_5.bungee.api.ChatColor rim_bungee = convertColor(rimcolor);
		
		TextComponent tc = new TextComponent();
		tc.setColor(rim_bungee);
		tc.addExtra("[");
		TextComponent sub = new TextComponent(text);
		sub.setItalic(true);
		sub.setColor(txt_bungee);
		sub.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
		tc.addExtra(sub);
		tc.addExtra("]");
		
		return tc;
	}
}
