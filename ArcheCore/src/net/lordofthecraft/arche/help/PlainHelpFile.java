package net.lordofthecraft.arche.help;

import org.bukkit.entity.*;

public class PlainHelpFile extends HelpFile
{
    private final String output;
    
    public PlainHelpFile(final String topic, final String output) {
        super(topic);
        this.output = output;
    }
    
    @Override
    public void output(final Player p) {
        p.sendMessage(this.output);
    }
    
    @Override
    public String outputHelp() {
        return this.output;
    }
}
