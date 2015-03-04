package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.interfaces.ChatMessage;

import org.bukkit.command.*;
import org.bukkit.*;
import org.bukkit.entity.*;

import net.lordofthecraft.arche.help.*;
import net.lordofthecraft.arche.enums.*;

import org.apache.commons.lang.*;

import java.util.*;

public class CommandArchehelp implements CommandExecutor
{
    private final HelpDesk helpdesk;
    private final boolean overridden;
    private ArcheMessage topics;
    
    public CommandArchehelp(final HelpDesk helpdesk, final boolean overridden) {
        super();
        this.topics = null;
        this.helpdesk = helpdesk;
        this.overridden = overridden;
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            final String cmd = this.overridden ? "help" : "archehelp";
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Available Help Topics (click to view):");
            if (sender instanceof Player) {
                final Set<String> tops = this.helpdesk.getTopics();
                if (this.topics == null || this.topics.size() < tops.size()) {
                    this.topics = new ArcheMessage("");
                    for (final String x : tops) {
                        this.topics.addLine(x).setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click for help").setClickEvent(ChatBoxAction.RUN_COMMAND, "/archehelp " + x).addLine(", ");
                    }
                }
                this.topics.sendTo((Player)sender);
            }
            else {
                sender.sendMessage(this.helpdesk.getTopics().toString());
            }
            sender.sendMessage(ChatColor.AQUA + "View help on a Topic with: " + ChatColor.ITALIC + "/" + cmd + " [topic]");
            sender.sendMessage(ChatColor.AQUA + "Find info on Skills with: " + ChatColor.ITALIC + "/skill [skill]");
        }
        else {
            final String topic = StringUtils.join((Object[])args, ' ');
            if (sender instanceof Player) {
                this.helpdesk.outputHelp(topic, (Player)sender);
            }
            else {
                final String help = this.helpdesk.getHelpText(topic);
                if (help == null) {
                    sender.sendMessage(ChatColor.RED + "No such Help Topic: " + ChatColor.GRAY + topic);
                }
                else {
                    sender.sendMessage(help);
                }
            }
        }
        return true;
    }
}
