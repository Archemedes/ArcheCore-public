package net.lordofthecraft.arche.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.interfaces.ChatMessage;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WikiHelpFile extends HelpFile {
	private final static String WIKI = "http://wiki.lordofthecraft.net/";
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
				.setClickEvent(ChatBoxAction.OPEN_URL, WIKI + getTopic().substring(2))
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
				HttpURLConnection.setFollowRedirects(true);
				String topic = boss.getTopic().substring(2).replace("\\", "").replace("/","").replace('?', ' ');
				URL url = new URL(WIKI + topic);
				
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				//con.setInstanceFollowRedirects(true);
				url = new URL(con.getHeaderField("Location"));
				con = (HttpURLConnection) url.openConnection();
				
				url = new URL(con.getHeaderField("Location"));
				con = (HttpURLConnection) url.openConnection();
				//System.out.println(con.getResponseCode());
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				
				String line;
				boolean start = false;
				boolean end = false;
				String first = "<div class=\"wiki wikiPage\" id=\"content_view\">";
				String last  = "  </div>";
				String wikif = "<!--";
				
				while((line = in.readLine()) != null){
					if(( line.startsWith(wikif)  || line.equalsIgnoreCase(last) )&& start){
						continue;
					}else if(start && !end){
						String format = line.trim().replace("<br />", "")
								.replaceAll("<!--(.*)-->","")
								.replace("<a class=\"wiki_link\" href=\"/", "@<")
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
				
			}
			catch(MalformedURLException e){e.printStackTrace();}
			catch (IOException e) { /*Means Wiki had no proper page*/}
			finally{if(in != null) try {in.close();} catch (IOException e) {e.printStackTrace();}}
			
			
			return null; //Help file can handle this although something is odd.
		}	
	}
}
