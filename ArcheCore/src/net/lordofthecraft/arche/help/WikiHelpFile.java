package net.lordofthecraft.arche.help;

import net.lordofthecraft.arche.interfaces.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import net.lordofthecraft.arche.enums.*;
import org.bukkit.scheduler.*;
import java.util.*;
import net.lordofthecraft.arche.*;
import org.bukkit.plugin.*;
import java.net.*;
import java.io.*;

public class WikiHelpFile extends HelpFile
{
    private static final String WIKI = "http://wiki.lordofthecraft.net/";
    private ChatMessage[] messages;
    
    public WikiHelpFile(final String topic) {
        super(topic);
        this.messages = null;
    }
    
    @Override
    public void output(final Player p) {
        if (this.messages != null) {
            for (final ChatMessage message : this.messages) {
                message.sendTo(p);
            }
            new ArcheMessage("Read more...").setItalic().applyChatColor(ChatColor.YELLOW).setHoverEvent(ChatBoxAction.SHOW_TEXT, "Open the wiki page").setClickEvent(ChatBoxAction.OPEN_URL, "http://wiki.lordofthecraft.net/" + this.getTopic().substring(2)).sendTo(p);
        }
        else {
            p.sendMessage(ChatColor.RED + "Help topic not available...");
        }
    }
    
    @Override
    public String outputHelp() {
        if (this.messages == null) {
            return "Topic not found";
        }
        final StringBuilder builder = new StringBuilder(256);
        String prefix = "";
        for (final ChatMessage message : this.messages) {
            builder.append(prefix).append(message.toString());
            prefix = "\n";
        }
        return builder.toString();
    }
    
    public static class WikiBrowser extends BukkitRunnable
    {
        private final UUID player;
        private final WikiHelpFile boss;
        
        public WikiBrowser(final Player p, final WikiHelpFile boss) {
            super();
            this.player = p.getUniqueId();
            this.boss = boss;
        }
        
        public void run() {
            final String help = this.getParagraph();
            if (help == null) {
                this.boss.messages = null;
            }
            else {
                this.boss.messages = ArcheMessage.createMultiple(help);
            }
            new BukkitRunnable() {
                public void run() {
                    final Player p = ArcheCore.getPlayer(WikiBrowser.this.player);
                    if (p != null) {
                        WikiBrowser.this.boss.output(p);
                    }
                }
            }.runTask((Plugin)ArcheCore.getPlugin());
        }
        
        private String getParagraph() {
            BufferedReader in = null;
            try {
                HttpURLConnection.setFollowRedirects(true);
                final String topic = this.boss.getTopic().substring(2).replace("\\", "").replace("/", "").replace('?', ' ');
                URL url = new URL("http://wiki.lordofthecraft.net/" + topic);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                url = new URL(con.getHeaderField("Location"));
                con = (HttpURLConnection)url.openConnection();
                url = new URL(con.getHeaderField("Location"));
                con = (HttpURLConnection)url.openConnection();
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                boolean start = false;
                final boolean end = false;
                final String first = "<div class=\"wiki wikiPage\" id=\"content_view\">";
                final String last = "  </div>";
                final String wikif = "<!--";
                String line;
                while ((line = in.readLine()) != null) {
                    if ((line.startsWith(wikif) || line.equalsIgnoreCase(last)) && start) {
                        continue;
                    }
                    if (start && !end) {
                        final String format = line.trim().replace("<br />", "").replaceAll("<!--(.*)-->", "").replace("<a class=\"wiki_link\" href=\"/", "@<").replace("</a>", "@").replace("\">", ">").replace("<em>", "").replace("</em>", "");
                        in.close();
                        if (format.isEmpty()) {
                            return null;
                        }
                        return format;
                    }
                    else {
                        if (!line.endsWith(first)) {
                            continue;
                        }
                        start = true;
                    }
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e3) {}
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
