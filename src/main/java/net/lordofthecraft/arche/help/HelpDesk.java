package net.lordofthecraft.arche.help;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.help.WikiHelpFile.WikiBrowser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class HelpDesk {
	public static final String HELP_HEADER = ChatColor.DARK_AQUA + "Please choose a Topic";
	
	private final Map<String, HelpFile> normalTopics = Maps.newLinkedHashMap();
	private final Map<String, HelpFile> skillTopics = Maps.newHashMap();
	private final Map<String, HelpFile> infoTopics = Maps.newHashMap();

	public static HelpDesk getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Set<String> getTopics(){
		return Collections.unmodifiableSet(normalTopics.keySet());
	}
	
	public void addTopic(HelpFile help){
		normalTopics.put(help.getTopic().toLowerCase(), help);
	}
	
	public void addTopic(String topic, String output){
		topic = topic.toLowerCase();
		HelpFile help = constructHelp(topic, output);
		normalTopics.put(topic, help);
	}
	
	public void addTopic(String topic, String output, Material icon){
		topic = topic.toLowerCase();
		HelpFile help = constructHelp(topic, output);
		help.setIcon(icon);
		normalTopics.put(topic, help);
	}
	
	public void addInfoTopic(HelpFile help){
		infoTopics.put(help.getTopic().toLowerCase(), help);
	}

	public void addInfoTopic(String topic, String output){
		topic = topic.toLowerCase();
		HelpFile help = constructHelp(topic, output);
		infoTopics.put(topic, help);
	}
	
	public void addSkillTopic(HelpFile help){
		skillTopics.put(help.getTopic().toLowerCase(), help);
	}
	
	public void addSkillTopic(String topic, String output){
		topic = topic.toLowerCase();
		HelpFile help = constructHelp(topic, output);
		skillTopics.put(topic, help);
	}
	
	public void addSkillTopic(String topic, String output, Material icon){
		topic = topic.toLowerCase();
		HelpFile help = constructHelp(topic, output);
		help.setIcon(icon);
		skillTopics.put(topic, help);
	}
	
	private HelpFile constructHelp(final String topic, final String output){
		return new LinkedHelpFile(topic, output);
	}
	
	public void outputHelp(String topic, Player p){
		//System.out.println(infoTopics);
		topic = topic.toLowerCase();
		HelpFile h = findHelpFile(topic);

		if(h == null){ //Help not found in internal db (help+info)
			if(ArcheCore.getPlugin().getWikiUsage()){ //Lookup from LotC Wiki maybe
				p.sendMessage(ChatColor.LIGHT_PURPLE + "Fetching help file, please wait...");
				WikiHelpFile wh = new WikiHelpFile(topic);
				new WikiBrowser(p, wh).runTaskAsynchronously(ArcheCore.getPlugin());
			}else p.sendMessage(ChatColor.RED + "No such help topic: " + ChatColor.GRAY + topic);
		}else h.output(p);
	}
	
	public void outputSkillHelp(String topic, Player p){
		topic = topic.toLowerCase();
		HelpFile h = skillTopics.get(topic);

		if(h == null) p.sendMessage(ChatColor.RED + "No such skill: " + ChatColor.GRAY + topic);
		else h.output(p);
	}

	public String getHelpText(String topic){
		topic = topic.toLowerCase();
		HelpFile h = findHelpFile(topic);

		if(h==null){
			return null;
		}else return h.outputHelp();
	}

	public String getSkillHelpText(String topic){
		topic = topic.toLowerCase();
		HelpFile h = skillTopics.get(topic);

		if(h==null) return null;
		else return h.outputHelp();
	}
	
	public void openHelpMenu(Player p){
		int sizeone = round(normalTopics.size(),9) + 9;
		int sizetwo = round(skillTopics.size(), 9);

		boolean large = sizeone+sizetwo > 54;
		int size = large? 54:sizeone+sizetwo;

		if(size == 0) return; //No help topics found so no need for any more

		Inventory inv = Bukkit.createInventory(p, size, HELP_HEADER);
		int i =  0;

		for(HelpFile h : normalTopics.values()){
			inv.setItem(i++, h.asItem());
		}

		//Put some space between different topics, if possible
		if(!large){
			i = sizeone;
		}

		for(HelpFile h : skillTopics.values()){
			inv.setItem(i++, h.asSkillItem());
		}

		p.openInventory(inv);
	}
	
	public HelpFile findHelpFile(String topic){
		HelpFile h = normalTopics.get(topic);
		if(h == null) h = infoTopics.get(topic);

/*		while(h != null && h.outputHelp().startsWith("@") && h.outputHelp().endsWith("@")
				&& StringUtils.countMatches(h.outputHelp(), "@") == 2){
			String referTopic = h.outputHelp().substring(1, h.outputHelp().length() - 2);
			h = normalTopics.get(referTopic);
			if(h == null) h = infoTopics.get(referTopic);
		}*/

		return h;
	}
	
	private int round(double i, int v){
	    return (int) (Math.ceil(i/v) * v);
	}
	
	private static class SingletonHolder {
		private static final HelpDesk INSTANCE = new HelpDesk();
	}
}
