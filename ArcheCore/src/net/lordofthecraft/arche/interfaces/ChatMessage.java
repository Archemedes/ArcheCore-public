package net.lordofthecraft.arche.interfaces;

import org.bukkit.*;
import net.lordofthecraft.arche.enums.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.*;

public interface ChatMessage
{
    ChatMessage addLine(String p0);
    
    ChatMessage select(int p0);
    
    int size();
    
    ChatMessage applyChatColor(ChatColor p0);
    
    ChatMessage setBold();
    
    ChatMessage setUnderlined();
    
    ChatMessage setItalic();
    
    ChatMessage setStrikethrough();
    
    ChatMessage setObfuscated();
    
    ChatMessage setClickEvent(ChatBoxAction p0, String p1);
    
    ChatMessage setClickEvent(ClickEvent p0);
    
    ChatMessage setHoverEvent(ChatBoxAction p0, String p1);
    
    ChatMessage setHoverEvent(HoverEvent p0);
    
    void sendTo(Player p0);
}
