package net.lordofthecraft.arche.help;

import net.lordofthecraft.arche.interfaces.*;
import com.google.common.collect.*;
import org.bukkit.*;
import net.lordofthecraft.arche.enums.*;
import net.md_5.bungee.api.chat.*;
import java.util.*;
import org.bukkit.entity.*;
import java.io.*;

public class ArcheMessage extends ChatMessage
{
    List<BaseComponent> parts;
    BaseComponent current;
    
    private ArcheMessage() {
        super();
        this.parts = Lists.newArrayList();
        this.current = null;
    }
    
    public ArcheMessage(final String text) {
        super();
        this.parts = Lists.newArrayList();
        this.addLine(text);
    }
    
    public ArcheMessage(final BaseComponent component) {
        super();
        (this.parts = Lists.newArrayList()).add(component);
    }
    
    @Override
    public ChatMessage addLine(final String line) {
        if (this.current != null) {
            this.parts.add(this.current);
        }
        final BaseComponent[] constructed = TextComponent.fromLegacyText(line);
        if (constructed.length == 1) {
            this.current = constructed[0];
        }
        else {
            this.current = (BaseComponent)new TextComponent();
            for (final BaseComponent extra : constructed) {
                this.current.addExtra(extra);
            }
        }
        return this;
    }
    
    @Override
    public ChatMessage select(final int i) {
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        if (i > this.parts.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return new ArcheMessage(this.parts.get(i));
    }
    
    @Override
    public int size() {
        final int andCurrent = (this.current != null) ? 1 : 0;
        return this.parts.size() + andCurrent;
    }
    
    @Override
    public ChatMessage applyChatColor(final ChatColor color) {
        if (color.isColor()) {
            final net.md_5.bungee.api.ChatColor convertedColor = convertChatColor(color);
            this.current.setColor(convertedColor);
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
                case STRIKETHROUGH: {
                    this.setStrikethrough();
                    break;
                }
            }
        }
        return this;
    }
    
    @Override
    public ChatMessage setBold() {
        this.current.setBold(true);
        return this;
    }
    
    @Override
    public ChatMessage setUnderlined() {
        this.current.setUnderlined(true);
        return this;
    }
    
    @Override
    public ChatMessage setItalic() {
        this.current.setItalic(true);
        return this;
    }
    
    @Override
    public ChatMessage setStrikethrough() {
        this.current.setStrikethrough(true);
        return this;
    }
    
    @Override
    public ChatMessage setObfuscated() {
        this.current.setObfuscated(true);
        return this;
    }
    
    @Override
    public ChatMessage setClickEvent(final ChatBoxAction action, final String value) {
        ClickEvent.Action act = null;
        switch (action) {
            case RUN_COMMAND: {
                act = ClickEvent.Action.RUN_COMMAND;
                break;
            }
            case SUGGEST_COMMAND: {
                act = ClickEvent.Action.SUGGEST_COMMAND;
                break;
            }
            case OPEN_URL: {
                act = ClickEvent.Action.OPEN_URL;
                break;
            }
            case OPEN_FILE: {
                act = ClickEvent.Action.OPEN_FILE;
                break;
            }
            default: {
                throw new IllegalArgumentException("Not all actions supported for ClickEvent");
            }
        }
        final ClickEvent event = new ClickEvent(act, value);
        this.current.setClickEvent(event);
        return this;
    }
    
    public ChatMessage setClickEvent(final ClickEvent event) {
        this.current.setClickEvent(event);
        return this;
    }
    
    @Override
    public ChatMessage setHoverEvent(final ChatBoxAction action, final String value) {
        HoverEvent.Action act = null;
        switch (action) {
            case SHOW_ACHIEVEMENT: {
                act = HoverEvent.Action.SHOW_ACHIEVEMENT;
                break;
            }
            case SHOW_TEXT: {
                act = HoverEvent.Action.SHOW_TEXT;
                break;
            }
            case SHOW_ITEM: {
                act = HoverEvent.Action.SHOW_ITEM;
                break;
            }
            default: {
                throw new IllegalArgumentException("Not all actions supported for HoverEvent");
            }
        }
        final HoverEvent event = new HoverEvent(act, new BaseComponent[] { new TextComponent(value) });
        this.current.setHoverEvent(event);
        return this;
    }
    
    public ChatMessage setHoverEvent(final HoverEvent event) {
        this.current.setHoverEvent(event);
        return this;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final BaseComponent comp : this.parts) {
            builder.append(comp.toPlainText());
        }
        if (this.current != null) {
            builder.append(this.current);
        }
        if (builder.length() == 0) {
            return "ChatMessage:null";
        }
        return builder.toString();
    }
    
    @Override
    public void sendTo(final Player p) {
        final int length = this.parts.size();
        final BaseComponent[] comps = new BaseComponent[length + 1];
        this.parts.toArray(comps);
        comps[length] = this.current;
        p.spigot().sendMessage(comps);
    }
    
    private static net.md_5.bungee.api.ChatColor convertChatColor(final ChatColor color) {
        switch (color) {
            case AQUA: {
                return net.md_5.bungee.api.ChatColor.AQUA;
            }
            case BLACK: {
                return net.md_5.bungee.api.ChatColor.BLACK;
            }
            case BLUE: {
                return net.md_5.bungee.api.ChatColor.BLUE;
            }
            case BOLD: {
                return net.md_5.bungee.api.ChatColor.BOLD;
            }
            case DARK_AQUA: {
                return net.md_5.bungee.api.ChatColor.DARK_AQUA;
            }
            case DARK_BLUE: {
                return net.md_5.bungee.api.ChatColor.DARK_BLUE;
            }
            case DARK_GRAY: {
                return net.md_5.bungee.api.ChatColor.DARK_GRAY;
            }
            case DARK_GREEN: {
                return net.md_5.bungee.api.ChatColor.DARK_GREEN;
            }
            case DARK_PURPLE: {
                return net.md_5.bungee.api.ChatColor.DARK_PURPLE;
            }
            case DARK_RED: {
                return net.md_5.bungee.api.ChatColor.DARK_RED;
            }
            case GOLD: {
                return net.md_5.bungee.api.ChatColor.GOLD;
            }
            case GRAY: {
                return net.md_5.bungee.api.ChatColor.GRAY;
            }
            case GREEN: {
                return net.md_5.bungee.api.ChatColor.GREEN;
            }
            case ITALIC: {
                return net.md_5.bungee.api.ChatColor.ITALIC;
            }
            case LIGHT_PURPLE: {
                return net.md_5.bungee.api.ChatColor.LIGHT_PURPLE;
            }
            case MAGIC: {
                return net.md_5.bungee.api.ChatColor.MAGIC;
            }
            case RED: {
                return net.md_5.bungee.api.ChatColor.RED;
            }
            case RESET: {
                return net.md_5.bungee.api.ChatColor.RESET;
            }
            case STRIKETHROUGH: {
                return net.md_5.bungee.api.ChatColor.STRIKETHROUGH;
            }
            case UNDERLINE: {
                return net.md_5.bungee.api.ChatColor.UNDERLINE;
            }
            case WHITE: {
                return net.md_5.bungee.api.ChatColor.WHITE;
            }
            case YELLOW: {
                return net.md_5.bungee.api.ChatColor.YELLOW;
            }
            default: {
                throw new IllegalArgumentException();
            }
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
        final ArcheMessage message = new ArcheMessage();
        boolean link = text.startsWith("@");
        for (final String segment : text.split("@")) {
            if (link) {
                final StringPair sp = StringPair.parseSyntax(segment);
                message.addLine(sp.vis);
                message.setUnderlined();
                final HoverEvent hEv = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Link to Topic: " + net.md_5.bungee.api.ChatColor.ITALIC + sp.url.replace('+', ' ')));
                final ClickEvent cEv = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/archehelp " + sp.url);
                message.setHoverEvent(hEv);
                message.setClickEvent(cEv);
            }
            else {
                boolean sugg = segment.startsWith("$");
                for (final String miniSegment : segment.split("\\$")) {
                    if (sugg) {
                        final StringPair sp2 = StringPair.parseSyntax(miniSegment);
                        final BaseComponent[] cmp = { new TextComponent("Run Command") };
                        message.addLine(sp2.vis);
                        final HoverEvent hEv2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, cmp);
                        final ClickEvent cEv2 = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, sp2.url);
                        message.setHoverEvent(hEv2);
                        message.setClickEvent(cEv2);
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
