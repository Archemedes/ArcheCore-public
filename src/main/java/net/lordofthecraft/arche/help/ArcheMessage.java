package net.lordofthecraft.arche.help;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.interfaces.ChatMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

import java.util.List;

public class ArcheMessage implements ChatMessage {
	List<BaseComponent> parts = Lists.newArrayList();
	BaseComponent current;

	private ArcheMessage(){
		current = null;
	}
	
	public ArcheMessage(String text){
		addLine(text);
	}
	
	public ArcheMessage(BaseComponent component){
		parts.add(component);
	}

	public static ChatColor convertChatColor(org.bukkit.ChatColor color) {
		switch (color) {
			case AQUA:
				return ChatColor.AQUA;
			case BLACK:
				return ChatColor.BLACK;
			case BLUE:
				return ChatColor.BLUE;
			case BOLD:
				return ChatColor.BOLD;
			case DARK_AQUA:
				return ChatColor.DARK_AQUA;
			case DARK_BLUE:
				return ChatColor.DARK_BLUE;
			case DARK_GRAY:
				return ChatColor.DARK_GRAY;
			case DARK_GREEN:
				return ChatColor.DARK_GREEN;
			case DARK_PURPLE:
				return ChatColor.DARK_PURPLE;
			case DARK_RED:
				return ChatColor.DARK_RED;
			case GOLD:
				return ChatColor.GOLD;
			case GRAY:
				return ChatColor.GRAY;
			case GREEN:
				return ChatColor.GREEN;
			case ITALIC:
				return ChatColor.ITALIC;
			case LIGHT_PURPLE:
				return ChatColor.LIGHT_PURPLE;
			case MAGIC:
				return ChatColor.MAGIC;
			case RED:
				return ChatColor.RED;
			case RESET:
				return ChatColor.RESET;
			case STRIKETHROUGH:
				return ChatColor.STRIKETHROUGH;
			case UNDERLINE:
				return ChatColor.UNDERLINE;
			case WHITE:
				return ChatColor.WHITE;
			case YELLOW:
				return ChatColor.YELLOW;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static ChatMessage[] createMultiple(String text) {

		String[] segments = text.split("\n");
		ChatMessage[] result = new ChatMessage[segments.length];

		int i = 0;
		String colorCode = "";
		for (String segment : segments) {
			result[i++] = create(colorCode + segment);
			colorCode = org.bukkit.ChatColor.getLastColors(segment);
		}

		return result;
	}

	public static ChatMessage create(String text) {
		ChatMessage message = new ArcheMessage();

		boolean link = text.startsWith("@");

		for (String segment : text.split("@")) {

			if (link) { // You're a link
				StringPair sp = StringPair.parseSyntax(segment);

				//Format the link into the chatmessage
				message.addLine(sp.vis);

				message.setUnderlined();
				//message.setItalic();

				HoverEvent hEv = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextComponent.fromLegacyText("Link to Topic: "
								+ ChatColor.ITALIC + sp.url.replace('+', ' ')));
				ClickEvent cEv = new ClickEvent(Action.RUN_COMMAND, "/archehelp " + sp.url);

				message.setHoverEvent(hEv);
				message.setClickEvent(cEv);

			} else {//Not a link, maybe a command suggest?
				boolean sugg = segment.startsWith("$");

				for (String miniSegment : segment.split("\\$")) { // Let's split up into commandables

					if (sugg) { //Is a command, clicking it inserts command in chatbox
						StringPair sp = StringPair.parseSyntax(miniSegment);

						BaseComponent[] cmp = new BaseComponent[]{new TextComponent("Run Command")};

						message.addLine(sp.vis);
						HoverEvent hEv = new HoverEvent(HoverEvent.Action.SHOW_TEXT, cmp);
						ClickEvent cEv = new ClickEvent(Action.SUGGEST_COMMAND, sp.url);

						message.setHoverEvent(hEv);
						message.setClickEvent(cEv);
					} else {
						message.addLine(miniSegment);
					}

					sugg = !sugg;
				}
			}

			link = !link;
		}

		return message;
	}

	@Override
	public ArcheMessage addLine(String line){
		if(current != null) parts.add(current);

		BaseComponent[] constructed = TextComponent.fromLegacyText(line);

		if(constructed.length == 1){ //Length always at least one
			current = constructed[0];
		} else {
			current = new TextComponent("");
			for(BaseComponent extra : constructed)
				current.addExtra(extra);
		}

		return this;
	}

	@Override
	public ArcheMessage addTranslation(String translate, String... with){
		if(current != null) parts.add(current);

		TranslatableComponent comp = new TranslatableComponent();
		comp.setTranslate(translate);
		current = comp;

		for(String w : with){
			comp.addWith(w);
		}

		return this;
	}

	@Override
	public ArcheMessage select(int i){
		if(i < 0) throw new IllegalArgumentException();
		if(i > parts.size()) throw new ArrayIndexOutOfBoundsException();
		return new ArcheMessage(parts.get(i));
	}

	@Override
	public int size(){
		int andCurrent = current == null? 0 : 1;
		return parts.size() + andCurrent;
	}

	@Override
	public ChatMessage applyChatColor(org.bukkit.ChatColor color){
		if(color.isColor()){
			ChatColor convertedColor = convertChatColor(color);
			current.setColor(convertedColor);
		} else {
			switch(color){
			default: break;
			case ITALIC: setItalic(); break;
			case BOLD: setBold(); break;
			case MAGIC: setObfuscated(); break;
			case UNDERLINE: setUnderlined(); break;
			case STRIKETHROUGH: setStrikethrough(); break;
			}
		}

		return this;
	}

	@Override
	public ChatMessage setBold(){
		current.setBold(true);
		return this;
	}

	@Override
	public ChatMessage setUnderlined(){
		current.setUnderlined(true);
		return this;
	}

	@Override
	public ChatMessage setItalic(){
		current.setItalic(true);
		return this;
	}

	@Override
	public ChatMessage setStrikethrough(){
		current.setStrikethrough(true);
		return this;
	}

	@Override
	public ChatMessage setObfuscated(){
		current.setObfuscated(true);
		return this;
	}

	@Override
	public ChatMessage setClickEvent(ChatBoxAction action, String value){
		ClickEvent.Action act;
		switch(action){

			case RUN_COMMAND: act = Action.RUN_COMMAND; break;
		case SUGGEST_COMMAND: act = Action.SUGGEST_COMMAND; break;
			case OPEN_URL:
				act = Action.OPEN_URL;
				break;
		case OPEN_FILE: act = Action.OPEN_FILE; break;
		default: throw new IllegalArgumentException("Not all actions supported for ClickEvent");
		}

		ClickEvent event = new ClickEvent(act, value);
		current.setClickEvent(event);
		return this;
	}

	@Override
	public ChatMessage setClickEvent(ClickEvent event){
		current.setClickEvent(event);
		return this;
	}

	@Override
	public ChatMessage setHoverEvent(ChatBoxAction action, String value){
		HoverEvent.Action act;
		switch(action){
		case SHOW_ACHIEVEMENT: act = HoverEvent.Action.SHOW_ACHIEVEMENT; break;
		case SHOW_TEXT: act = HoverEvent.Action.SHOW_TEXT; break;
			case SHOW_ITEM:
				act = HoverEvent.Action.SHOW_ITEM;
				break;
		default: throw new IllegalArgumentException("Not all actions supported for HoverEvent");
		}

		HoverEvent event = new HoverEvent(act, new BaseComponent[]{new TextComponent(value)});
		current.setHoverEvent(event);
		return this;
	}

	@Override
	public ChatMessage setHoverEvent(HoverEvent event){
		current.setHoverEvent(event);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(BaseComponent comp : parts)
			builder.append(comp.toString());
		if(current != null) builder.append(current);

		if(builder.length() == 0) return "ChatMessage:null";

		return builder.toString();
	}
	
	public String toText() {
		StringBuilder builder = new StringBuilder();
		for(BaseComponent comp : parts)
			builder.append(comp.toPlainText());
		if(current != null) builder.append(current.toPlainText());

		if(builder.length() == 0) return "ChatMessage:null";

		return builder.toString();
	}

	@Override
	public void sendTo(Player p) {
		int length = parts.size();
		BaseComponent[] comps = new BaseComponent[length + 1];
		parts.toArray(comps);
		comps[length] = current;
		p.spigot().sendMessage(comps);
	}
	
	private static class StringPair{
		private String url,vis;

		private StringPair(String url, String vis) {
			this.url = url;
			this.vis = vis;
		}
		
		private static StringPair parseSyntax(String segment){
			String url,vis;
			//Possible to have a topic link with a different Body text.
			//Syntax: @<Human>Men@. Links to 'Human' but says 'Men'
			//We now see if this functionality was used
			if(segment.startsWith("<") && segment.contains(">")){
				int index = segment.lastIndexOf('>');

				if(index == segment.length() - 1){
					url=vis= segment.replace('<', ' ').replace('>', ' ');
				} else {
					url = segment.substring(1, index);
					vis = segment.substring(index+1);
				}
			} else {
				url=vis=segment;
			}

			return new StringPair(url, vis);
		}
	}
	
}