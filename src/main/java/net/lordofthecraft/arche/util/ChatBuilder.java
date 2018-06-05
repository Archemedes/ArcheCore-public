package net.lordofthecraft.arche.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Wrapper class around ComponentBuilder with lots of syntactic sugar.
 * Doesn't extend {@link net.md_5.bungee.api.chat.ComponentBuilder} because it is final.
 * Unfortunately this means we lose their javadoc on these methods
 * This hides calls that use {@link net.md_5.bungee.api.ChatColor} because it's not useful for us
 */
public class ChatBuilder {
	private final ComponentBuilder handle;
	
	ChatBuilder(String initial) {
		handle = new ComponentBuilder(initial);
	}
	
	public ChatBuilder newline() {
		handle.append(System.lineSeparator(), FormatRetention.NONE);
		return this;
	}
	
	public ChatBuilder append(char ch) {
		handle.append(String.valueOf(ch));
		return this;
	}
	
	public ChatBuilder append(Object o) {
		handle.append(String.valueOf(o));
		return this;
	}
	
	public ChatBuilder append(int i) {
		handle.append(String.valueOf(i));
		return this;
	}
	
	public ChatBuilder append(long l) {
		handle.append(String.valueOf(l));
		return this;
	}
	
	public ChatBuilder append(String text) {
		handle.append(text);
		return this;
	}
	
	public ChatBuilder append(String text, FormatRetention retention) {
		handle.append(text, retention);
		return this;
	}
	
	public ChatBuilder append(BaseComponent component) {
		handle.append(component);
		return this;
	}
	
	public ChatBuilder append(BaseComponent[] components) {
		handle.append(components);
		return this;
	}
	
	public ChatBuilder append(BaseComponent component, FormatRetention retention){
		handle.append(component, retention);
		return this;
	}
	
	public ChatBuilder append(BaseComponent[] components, FormatRetention retention){
		handle.append(components, retention);
		return this;
	}
	
	public ChatBuilder appendButton(String text, String cmd) {
		return append(MessageUtil.CommandButton(text, cmd));
	}
	
	public ChatBuilder appendButton(String text, String cmd, String hover) {
		return append(MessageUtil.CommandButton(text, cmd, hover));
	}
	
	public ChatBuilder appendButton(String text, String cmd, ChatColor textcolor, ChatColor rimcolor) {
		return append(MessageUtil.CommandButton(text, cmd, textcolor, rimcolor));
	}
	
	public ChatBuilder appendButton(String text, String cmd, String hover, ChatColor textcolor, ChatColor rimcolor) {
		return append(MessageUtil.CommandButton(text, cmd, hover, textcolor, rimcolor));
	}
	
	public ChatBuilder reset() {
		handle.reset();
		return this;
	}
	
	public ChatBuilder retainEvents() {
		return retain(FormatRetention.EVENTS);
	}
	
	public ChatBuilder retainColors() {
		return retain(FormatRetention.FORMATTING);
	}
	
	public ChatBuilder retain(FormatRetention retention) {
		handle.retain(retention);
		return this;
	}
	
	public ChatBuilder bold() {
		return bold(true);
	}
	
	public ChatBuilder bold(boolean bold) {
		handle.bold(bold);
		return this;
	}
	
	public ChatBuilder italic() {
		return italic(true);
	}
	
	public ChatBuilder italic(boolean italic) {
		handle.italic(italic);
		return this;
	}
	
	public ChatBuilder obfuscated() {
		return obfuscated(true);
	}
	
	public ChatBuilder obfuscated(boolean obfuscated) {
		handle.obfuscated(obfuscated);
		return this;
	}
	
	public ChatBuilder strikethrough() {
		return strikethrough(true);
	}
	
	public ChatBuilder strikethrough(boolean strikethrough) {
		handle.strikethrough(strikethrough);
		return this;
	}
	
	public ChatBuilder underlined() {
		return underlined(true);
	}
	
	public ChatBuilder underlined(boolean underlined) {
		handle.underlined(underlined);
		return this;
	}
	
	public ChatBuilder color(ChatColor color) {
		handle.color(MessageUtil.convertColor(color));
		return this;
	}
	
	public ChatBuilder hoverItem(ItemStack is) {
		return event(HoverEvent.Action.SHOW_ITEM, ItemUtil.getItemJson(is));
	}
	
	public ChatBuilder hover(String text) {
		return event(HoverEvent.Action.SHOW_TEXT, text);
	}

	public ChatBuilder event(HoverEvent.Action action, String text) {
		return event(MessageUtil.hoverEvent(action, text));
	}
	
	public ChatBuilder event(HoverEvent event) {
		handle.event(event);
		return this;
	}
	
	public ChatBuilder command(String command) {
		return event(ClickEvent.Action.RUN_COMMAND, command);
	}
	
	public ChatBuilder suggest(String suggestion) {
		return event(ClickEvent.Action.SUGGEST_COMMAND, suggestion);
	}
	
	public ChatBuilder event(ClickEvent.Action action, String text) {
		return event(new ClickEvent(action, text));
	}
	
	public ChatBuilder event(ClickEvent event) {
		handle.event(event);
		return this;
	}
	
	public ChatBuilder insertion(String insertion) {
		handle.insertion(insertion);
		return this;
	}
	
	public BaseComponent[] create() {
		return handle.create();
	}
	
	public BaseComponent build() {
		return new TextComponent(handle.create());
	}
	
	public ChatBuilder send(Player p) {
		p.spigot().sendMessage(handle.create());
		return this;
	}
	
	public ChatBuilder send(CommandSender s) {
		if(s instanceof Player) return send((Player) s);
		s.sendMessage(this.build().toLegacyText());
		
		return this;
	}
	
	public String toLegacyText() {
		return BaseComponent.toLegacyText(handle.create());
	}
	
	public String toPlainText() {
		return BaseComponent.toPlainText(handle.create());
	}
	
}
