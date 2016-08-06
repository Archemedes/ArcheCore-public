package net.lordofthecraft.arche.help;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.interfaces.ChatMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

public class WikiHelpFile extends HelpFile {
	private final static String WIKI = "https://wikia.lordofthecraft.net/index.php?title=";
	private ChatMessage[] messages = null;

	public WikiHelpFile(String topic) {
		super(topic);
	}

	@Override
	public void output(Player p) {
		if(messages != null){
			for(ChatMessage message : messages) message.sendTo(p);
			new ArcheMessage("Read more...")
				.setItalic()
				.applyChatColor(ChatColor.YELLOW)
				.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Open the wiki page")
					.setClickEvent(ChatBoxAction.OPEN_URL, WIKI + getTopic().substring(2).replace("\\", "").replace("/", "").replace('?', ' ').replace(' ', '_'))
				.sendTo(p);
		} else {
			p.sendMessage(ChatColor.RED + "Help topic not available...");
		}
	}

	@Override
	public String outputHelp() {
		if(messages == null) return "Topic not found";
		
		StringBuilder builder = new StringBuilder(256);
		String prefix = "";
		
		for(ChatMessage message : messages){
			builder.append(prefix).append(message.toString());
			prefix = "\n";
		}
		
		return builder.toString();
	}
	
	
	public static class WikiBrowser extends BukkitRunnable{
		private final UUID player;
		private final WikiHelpFile boss;
		
		public WikiBrowser(Player p, WikiHelpFile boss){
			player = p.getUniqueId();
			this.boss = boss;
		}
		
		@Override
		public void run(){
			String help = getParagraph();
			if(help == null) boss.messages = null;
			else boss.messages = ArcheMessage.createMultiple(help);
			
			new BukkitRunnable(){
				
				@Override
				public void run(){
					Player p = ArcheCore.getPlayer(player);
					if(p != null) boss.output(p);
				}
				
			}.runTask(ArcheCore.getPlugin());
		}
		
		private String getParagraph(){
			BufferedReader in = null;
			try{
				//HttpURLConnection.setFollowRedirects(true);
				String topic = boss.getTopic().substring(2).replace("\\", "").replace("/", "").replace('?', ' ').replace(' ', '_');
				Document doc = Jsoup.connect(WIKI + topic).get();
				Elements ele = doc.select("#mw-content-text p");
				//we only want to get the first.
				String text = ele.get(0).html();

				/*List<String> text = Lists.newArrayList();
				ele.forEach(e -> text.add(e.text()));
				String result = "";
				for (String t : text) {
					String format = t.trim()
							.replace("<a href=\"/index.php?title=(.*)\">", "@<")
							.replace("</a>", "@");
					result += format;
				}*/

				return text.trim()
						.replaceAll("<a href=\"(.*)\">", "@")
						.replace("</a>", "@")
						.replace("<b>", ChatColor.BOLD.toString())
						.replace("</b>", ChatColor.RESET.toString())
						.replace("<em>", ChatColor.ITALIC.toString())
						.replace("</em>", ChatColor.RESET.toString());


				/*ArcheCore.getPlugin().getLogger().info("topic is "+topic);
				URL url = new URL(WIKI + topic);
				ArcheCore.getPlugin().getLogger().info("url is "+url);
				
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				ArcheCore.getPlugin().getLogger().info("response code is "+con.getResponseCode());
				//con.setInstanceFollowRedirects(true);
				url = new URL(con.getHeaderField("Location"));
				ArcheCore.getPlugin().getLogger().info("url is "+url);
				con = (HttpURLConnection) url.openConnection();
				ArcheCore.getPlugin().getLogger().info("response code is "+con.getResponseCode());
				
				url = new URL(con.getHeaderField("Location"));
				ArcheCore.getPlugin().getLogger().info("url is "+url);
				con = (HttpURLConnection) url.openConnection();
				ArcheCore.getPlugin().getLogger().info("response code is "+con.getResponseCode());
				//System.out.println(con.getResponseCode());
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line;
				boolean start = false;
				boolean end = false;
				//String first = "<div id=\"mw-content-text\" lang=\"en\" dir=\"ltr\" class=\"mw-content-ltr\">";
				String first = "<div id=\"jump-to-nav\" class=\"mw-jump\">";
				//<div id="jump-to-nav" class="mw-jump">
				String last  = "  </div>";
				String wikif = "<!--";
				String tocstart = "<div id=\"toc\" class=\"toc\">";

				while((line = in.readLine()) != null){
					ArcheCore.getPlugin().getLogger().info("line is "+in.readLine());
					if(( line.startsWith(wikif)  || line.equalsIgnoreCase(last))&& start){
						continue;
					}else if(start && !end){
						String format = line.trim().replace("<br />", "")
								.replaceAll("<!--(.*)-->","")
								.replace("<a href=\"/index.php?title=(.*)\">", "@<")
								.replace("</a>", "@")
								.replace("\">", ">")
								.replace("<em>", "")
								.replace("</em>", "");
						
						in.close();
						
						if(format.isEmpty()) return null;
						else return format;
						
					} else if (line.endsWith(first)){
						start = true;
					}
				}
				*/
			}
			catch(MalformedURLException e){e.printStackTrace();}
			catch (IOException e) { /*Means Wiki had no proper page*/}
			finally{if(in != null) try {in.close();} catch (IOException e) {e.printStackTrace();}}
			
			
			return null; //Help file can handle this although something is odd.
		}	
	}
}