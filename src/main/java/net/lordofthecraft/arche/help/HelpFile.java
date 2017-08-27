package net.lordofthecraft.arche.help;

import com.google.common.collect.Lists;

import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class HelpFile {
	private final String topic; 
	private Material icon = Material.SIGN;
	
	public HelpFile(String topic){
		this.topic = WordUtils.capitalize(topic);
	}

	public Material getIcon(){
		return icon;
	}

	public void setIcon(Material icon) {
		this.icon = icon;
	}
	
	public ItemStack asItem(){
		ItemStack i = new ItemStack(icon);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + topic);
		i.setItemMeta(meta);
		
		return i;
	}
	
	public ItemStack asSkillItem(){
		ItemStack i = new ItemStack(icon);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + topic);
		List<String> lore = Lists.newArrayList();
		lore.add(ChatColor.DARK_GRAY + "Learn about this Skill.");
		meta.setLore(lore);
		i.setItemMeta(meta);
		
		return i;
	}
	
	public String getTopic(){
		return ChatColor.RESET + topic;
	}
	
	public abstract void output(Player p);

	public abstract String outputHelp();
	
	
	//Global parsing methods for helpfiles
	public static BaseComponent[] parseMultiple(String text) {
		String[] segments = text.split("\n");
		BaseComponent[] result = new BaseComponent[segments.length];

		int i = 0;
		String colorCode = "";
		for (String segment : segments) {
			result[i++] = parse(colorCode + segment);
			colorCode = org.bukkit.ChatColor.getLastColors(segment);
		}

		return result;
	}
	
	public static BaseComponent parse(String text) {
		BaseComponent message = new TextComponent();

		boolean link = text.startsWith("@");

		for (String segment : text.split("@")) {

			if (link) { // You're a link
				StringPair sp = StringPair.parseSyntax(segment);

				//Format the link into the chatmessage
				MessageUtil.legacyAdd(message, sp.vis);
				message.setUnderlined(true);

				HoverEvent hEv = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextComponent.fromLegacyText("Link to Topic: "
								+ ChatColor.ITALIC + sp.url.replace('+', ' ')));
				ClickEvent cEv = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/archehelp " + sp.url);

				message.setHoverEvent(hEv);
				message.setClickEvent(cEv);

			} else {//Not a link, maybe a command suggest?
				boolean sugg = segment.startsWith("$");

				for (String miniSegment : segment.split("\\$")) { // Let's split up into commandables

					if (sugg) { //Is a command, clicking it inserts command in chatbox
						StringPair sp = StringPair.parseSyntax(miniSegment);

						BaseComponent[] cmp = new BaseComponent[]{new TextComponent("Run Command")};

						BaseComponent vis = MessageUtil.legacyText(sp.vis);
						HoverEvent hEv = new HoverEvent(HoverEvent.Action.SHOW_TEXT, cmp);
						ClickEvent cEv = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, sp.url);

						vis.setHoverEvent(hEv);
						vis.setClickEvent(cEv);
						message.addExtra(vis);
					} else {
						MessageUtil.legacyAdd(message, miniSegment);
					}
					sugg = !sugg;
				}
			}

			link = !link;
		}

		return message;
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
