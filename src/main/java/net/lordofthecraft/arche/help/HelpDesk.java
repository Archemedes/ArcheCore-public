package net.lordofthecraft.arche.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Pad;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.help.WikiHelpFile.WikiBrowser;

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

    public Set<String> getTopics(CommandSender forwho) {
        return normalTopics.entrySet().stream().filter(t -> t.getValue().canView(forwho)).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public void addTopic(HelpFile help){
		normalTopics.put(help.getTopic().toLowerCase(), help);
	}

    public void addTopic(String topic, String output, String permission) {
        topic = topic.toLowerCase();
        HelpFile help = constructHelp(topic, output);
        help.setPerm(permission);
        normalTopics.put(topic, help);
    }

    public void addTopic(String topic, String output, Material icon, String permission) {
        topic = topic.toLowerCase();
        HelpFile help = constructHelp(topic, output);
        help.setIcon(icon);
        help.setPerm(permission);
        normalTopics.put(topic, help);
    }

    public void addTopic(String topic, String output) {
        addTopic(topic, output, "archecore.mayuse");
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

    public void addInfoTopic(String topic, String output, String permission) {
        topic = topic.toLowerCase();
        HelpFile help = constructHelp(topic, output);
        help.setPerm(permission);
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

    public boolean outputHelp(String topic, Player p) {
        //System.out.println(infoTopics);
		topic = topic.toLowerCase();
		HelpFile h = findHelpFile(topic);
		
		
		if(h == null){ //Help not found in internal db (help+info)
			if(ArcheCore.getPlugin().getWikiUsage()){ //Lookup from LotC Wiki maybe
				p.sendMessage(ChatColor.LIGHT_PURPLE + "Fetching help file, please wait...");
				WikiHelpFile wh = new WikiHelpFile(topic);
				new WikiBrowser(p, wh).runTaskAsynchronously(ArcheCore.getPlugin());
                return true;
            } else {
                p.sendMessage(ChatColor.RED + "No such help topic: " + ChatColor.GRAY + topic);
                return true;
            }
        } else {
            if (!h.canView(p)) {
                return false;
            }
            h.output(p);
            return true;
        }
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

		if(h==null)return getSkillHelpText(topic);
		else return h.outputHelp();
	}

	public String getSkillHelpText(String topic){
		topic = topic.toLowerCase();
		HelpFile h = skillTopics.get(topic);

		if(h==null) return null;
		else return h.outputHelp();
	}
	
	public void openHelpMenu(Player p){
		List<Icon> topics = new ArrayList<>();
		
		normalTopics.values().stream().map(HelpFile::asIcon).forEach(topics::add);
		
		if(normalTopics.size() > 0 && skillTopics.size() > 0) {
			int rest = topics.size() % 9;
			int padding = 18 - (rest == 0? 9:rest);
			IntStream.range(0, padding).forEach($->topics.add(new Pad(Material.AIR)));
			skillTopics.values().stream().map(HelpFile::asSkillIcon).forEach(topics::add);
		}
		
		Menu.fromIcons(HELP_HEADER, topics).openSession(p);
	}
	
	public HelpFile findHelpFile(String topic){
		HelpFile h = normalTopics.get(topic);
		if(h == null) h = infoTopics.get(topic);
		if(h == null) h = skillTopics.get(topic);

/*		while(h != null && h.outputHelp().startsWith("@") && h.outputHelp().endsWith("@")
				&& StringUtils.countMatches(h.outputHelp(), "@") == 2){
			String referTopic = h.outputHelp().substring(1, h.outputHelp().length() - 2);
			h = normalTopics.get(referTopic);
			if(h == null) h = infoTopics.get(referTopic);
		}*/

		return h;
	}

	private static class SingletonHolder {
		private static final HelpDesk INSTANCE = new HelpDesk();
	}
}
