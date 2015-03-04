package net.lordofthecraft.arche.help;

import java.lang.reflect.*;
import org.json.simple.*;
import net.lordofthecraft.arche.enums.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;

public class ChatMessage
{
    private static final String PATH;
    private static Method serializeChat;
    private static Method getHandlePlayer;
    private static Method sendMessagePlayer;
    private JSONObject sendableJSON;
    private JSONObject craftedJSON;
    protected JSONArray array;
    private ChatColor currentColor;
    private ChatColor currentFormat;
    Object preparedChat;
    public String raw_string;
    
    public ChatMessage() {
        super();
        this.currentColor = null;
        this.currentFormat = null;
        this.preparedChat = null;
        this.raw_string = "";
        (this.sendableJSON = new JSONObject()).put((Object)"text", (Object)"");
        this.array = new JSONArray();
        this.sendableJSON.put((Object)"extra", (Object)this.array);
    }
    
    public JSONObject getJson() {
        return this.sendableJSON;
    }
    
    public void setJson(final JSONObject o) {
        this.sendableJSON = o;
    }
    
    public ChatMessage addLine(String s) {
        this.preparedChat = null;
        final JSONObject object = new JSONObject();
        this.array.add((Object)object);
        this.craftedJSON = object;
        this.raw_string += s;
        while (s.startsWith("§")) {
            this.updateColor(ChatColor.getByChar(s.charAt(1)));
            s = s.substring(2);
        }
        if (s.contains("§")) {
            final JSONArray subtags = new JSONArray();
            object.put((Object)"text", (Object)"");
            object.put((Object)"extra", (Object)subtags);
            boolean first = true;
            for (String x : s.split("§")) {
                Label_0251: {
                    if (first) {
                        first = false;
                    }
                    else if (!x.isEmpty()) {
                        this.updateColor(ChatColor.getByChar(x.charAt(0)));
                        if (x.length() == 1) {
                            break Label_0251;
                        }
                        x = x.substring(1);
                    }
                    subtags.add((Object)(this.craftedJSON = new JSONObject()));
                    this.craftedJSON.put((Object)"text", (Object)x);
                    this.applyChatColor(this.currentColor);
                    this.applyChatColor(this.currentFormat);
                }
            }
            this.craftedJSON = object;
        }
        else {
            object.put((Object)"text", (Object)s);
            this.applyChatColor(this.currentColor);
            this.applyChatColor(this.currentFormat);
        }
        return this;
    }
    
    private void updateColor(final ChatColor form) {
        if (form != null) {
            if (form.isColor()) {
                this.currentColor = form;
                this.currentFormat = null;
            }
            else {
                this.currentFormat = form;
            }
        }
    }
    
    public ChatMessage select(final int i) {
        this.craftedJSON = (JSONObject)this.array.get(i);
        return this;
    }
    
    public int size() {
        return this.array.size();
    }
    
    public ChatMessage applyChatColor(final ChatColor color) {
        if (color != null) {
            if (color.isColor()) {
                this.preparedChat = null;
                this.craftedJSON.put((Object)"color", (Object)color.name().toLowerCase());
            }
            else {
                switch (color) {
                    case ITALIC: {
                        this.setItalic();
                        break;
                    }
                    case BOLD: {
                        this.setBold();
                        break;
                    }
                    case MAGIC: {
                        this.setObfuscated();
                        break;
                    }
                    case UNDERLINE: {
                        this.setUnderlined();
                        break;
                    }
                }
            }
        }
        return this;
    }
    
    public ChatMessage setBold() {
        this.preparedChat = null;
        this.craftedJSON.put((Object)"bold", (Object)true);
        return this;
    }
    
    public ChatMessage setUnderlined() {
        this.preparedChat = null;
        this.craftedJSON.put((Object)"underlined", (Object)true);
        return this;
    }
    
    public ChatMessage setItalic() {
        this.preparedChat = null;
        this.craftedJSON.put((Object)"italic", (Object)true);
        return this;
    }
    
    public ChatMessage setStrikethrough() {
        this.preparedChat = null;
        this.craftedJSON.put((Object)"strikethrough", (Object)true);
        return this;
    }
    
    public ChatMessage setObfuscated() {
        this.preparedChat = null;
        this.craftedJSON.put((Object)"obfuscated", (Object)true);
        return this;
    }
    
    public ChatMessage setClickEvent(final ChatBoxAction action, final String value) {
        this.preparedChat = null;
        final JSONObject ob = new JSONObject();
        ob.put((Object)"action", (Object)action.toString());
        ob.put((Object)"value", (Object)value);
        this.craftedJSON.put((Object)"clickEvent", (Object)ob);
        return this;
    }
    
    public ChatMessage setHoverEvent(final ChatBoxAction action, final String value) {
        this.preparedChat = null;
        final JSONObject ob = new JSONObject();
        ob.put((Object)"action", (Object)action.toString());
        ob.put((Object)"value", (Object)value);
        this.craftedJSON.put((Object)"hoverEvent", (Object)ob);
        return this;
    }
    
    @Override
    public String toString() {
        return this.raw_string;
    }
    
    public void sendTo(final Player p) {
        try {
            if (ChatMessage.serializeChat == null) {
                ChatMessage.serializeChat = Class.forName(ChatMessage.PATH + "ChatSerializer").getMethod("a", String.class);
                ChatMessage.getHandlePlayer = p.getClass().getMethod("getHandle", (Class<?>[])new Class[0]);
                ChatMessage.sendMessagePlayer = ChatMessage.getHandlePlayer.getReturnType().getMethod("sendMessage", ChatMessage.serializeChat.getReturnType());
            }
            if (this.preparedChat == null) {
                this.preparedChat = ChatMessage.serializeChat.invoke(null, this.sendableJSON.toJSONString());
            }
            final Object player = ChatMessage.getHandlePlayer.invoke(p, new Object[0]);
            ChatMessage.sendMessagePlayer.invoke(player, this.preparedChat);
        }
        catch (Exception e) {
            ArcheCore.getPlugin().getLogger().severe("Reflection Failure in ChatMessage send handling.");
            e.printStackTrace();
        }
    }
    
    public static ChatMessage[] createMultiple(final String text) {
        final String[] segments = text.split("\n");
        final ChatMessage[] result = new ChatMessage[segments.length];
        int i = 0;
        String colorCode = "";
        for (final String segment : segments) {
            result[i++] = create(colorCode + segment);
            colorCode = ChatColor.getLastColors(segment);
        }
        return result;
    }
    
    public static ChatMessage create(final String text) {
        final ChatMessage message = new ChatMessage();
        boolean link = text.startsWith("@");
        for (final String segment : text.split("@")) {
            if (link) {
                final StringPair sp = StringPair.parseSyntax(segment);
                message.addLine(sp.vis);
                message.setUnderlined();
                message.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Link to Topic: " + ChatColor.ITALIC + sp.url.replace('+', ' '));
                message.setClickEvent(ChatBoxAction.RUN_COMMAND, "/archehelp " + sp.url);
            }
            else {
                boolean sugg = segment.startsWith("$");
                for (final String miniSegment : segment.split("\\$")) {
                    if (sugg) {
                        final StringPair sp2 = StringPair.parseSyntax(miniSegment);
                        message.addLine(sp2.vis);
                        message.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Run Command");
                        message.setClickEvent(ChatBoxAction.SUGGEST_COMMAND, sp2.url);
                    }
                    else {
                        message.addLine(miniSegment);
                    }
                    sugg = !sugg;
                }
            }
            link = !link;
        }
        return message;
    }
    
    static {
        PATH = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        ChatMessage.serializeChat = null;
        ChatMessage.getHandlePlayer = null;
        ChatMessage.sendMessagePlayer = null;
    }
    
    private static class StringPair
    {
        private String url;
        private String vis;
        
        private static StringPair parseSyntax(final String segment) {
            String url;
            String vis;
            if (segment.startsWith("<") && segment.contains(">")) {
                final int index = segment.lastIndexOf(62);
                if (index == segment.length() - 1) {
                    vis = (url = segment.replace('<', ' ').replace('>', ' '));
                }
                else {
                    url = segment.substring(1, index);
                    vis = segment.substring(index + 1);
                }
            }
            else {
                vis = segment;
                url = segment;
            }
            return new StringPair(url, vis);
        }
        
        private StringPair(final String url, final String vis) {
            super();
            this.url = url;
            this.vis = vis;
        }
    }
}
