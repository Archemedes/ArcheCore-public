package net.lordofthecraft.arche.help;

import com.google.common.collect.Lists;

import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.SimpleButton;
import co.lotc.core.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class HelpFile {
	private final String topic;
	private String perm = "archecore.mayuse";
	private Material icon = Material.SIGN;

	public HelpFile(String topic, String perm) {
		this.topic = topic;
		this.perm = perm;
	}

	public HelpFile(String topic) {
		this.topic = WordUtils.capitalize(topic);
	}

	public String getPerm() {
		return perm;
	}

	public boolean canView(CommandSender sender) {
		return perm == null || sender.hasPermission(perm);
	}

	public void setPerm(String perm) {
		this.perm = perm;
	}

	public Material getIcon() {
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
	
	public Icon asIcon() {
		return asIcon(asItem());
	}
	
	public Icon asSkillIcon() {
		return asIcon(asSkillItem());
	}
	
	private Icon asIcon(ItemStack item) {
		return new SimpleButton(item, ma->{
			Player x = ma.getPlayer();
			HelpDesk.getInstance().outputHelp(topic, x);
			ma.getMenuAgent().close();
		});
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

        boolean link = text.startsWith("@@");

        for (String segment : text.split("@@")) {

			if (link) { // You're a link
				StringPair sp = StringPair.parseSyntax(segment);

				//Format the link into the chatmessage
				BaseComponent theLink = MessageUtil.legacyText(sp.vis);
				theLink.setUnderlined(true);

				HoverEvent hEv = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextComponent.fromLegacyText("Link to Topic: "
								+ ChatColor.ITALIC + sp.url.replace('+', ' ')));
				ClickEvent cEv = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/archehelp " + sp.url);

				theLink.setHoverEvent(hEv);
				theLink.setClickEvent(cEv);
				message.addExtra(theLink);
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
