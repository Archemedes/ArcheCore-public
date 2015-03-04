package net.lordofthecraft.arche.help;

import net.lordofthecraft.arche.interfaces.*;
import org.bukkit.entity.*;

public class LinkedHelpFile extends HelpFile
{
    private final ChatMessage[] messages;
    
    public LinkedHelpFile(final String topic, final String text) {
        super(topic);
        this.messages = ArcheMessage.createMultiple(text);
    }
    
    @Override
    public void output(final Player p) {
        for (final ChatMessage message : this.messages) {
            message.sendTo(p);
        }
    }
    
    @Override
    public String outputHelp() {
        final StringBuilder builder = new StringBuilder(256);
        String prefix = "";
        for (final ChatMessage message : this.messages) {
            builder.append(prefix).append(message.toString());
            prefix = "\n";
        }
        return builder.toString();
    }
}
