package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.help.ArcheMessage;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.SaveExecutorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Created on 7/11/2017
 *
 * @author 501warhead
 */
public class CommandMagic implements CommandExecutor {

    private final PersonaHandler handler;
    private final SaveExecutorManager buffer;

    public CommandMagic(PersonaHandler handler, SaveExecutorManager buffer) {
        this.handler = handler;
        this.buffer = buffer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (args.length == 0) {
            //TODO Help
        }
        if (args[0].equalsIgnoreCase("create")) {
            return true;
        } else if (args[0].equalsIgnoreCase("give")) {
            return true;
        } else if (args[0].equalsIgnoreCase("show")) {
            if (args.length < 2) {
                return h(sender);
            }
            Optional<ArcheMagic> optmagic = ArcheMagic.getMagicByName(args[1]);
            if (!optmagic.isPresent()) {
                sender.sendMessage(ChatColor.RED + "Error: No magic with the name of " + args[1] + " was found!");
            } else {
                ArcheMagic magic = optmagic.get();
                sender.sendMessage(ChatColor.AQUA + "============= " + ChatColor.DARK_PURPLE + magic.getName() + ChatColor.AQUA + " =============");
                sender.sendMessage(ChatColor.BLUE + "Max Tier: " + ChatColor.RESET + magic.getMaxTier());
                sender.sendMessage(ChatColor.BLUE + "Self Teachable?: " + ChatColor.RESET + (magic.isSelfTeachable() ? "Yes" : "No"));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("list")) {
            if (sender instanceof Player) {
                ArcheMessage m = new ArcheMessage("");
                String j = "";
                for (ArcheMagic magic : ArcheMagic.getMagics()) {
                    m.addLine(j);
                    m.addLine(magic.getName()).setUnderlined().setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click me to view this magic").setClickEvent(ChatBoxAction.RUN_COMMAND, "/magic show " + magic.getName());
                    j = ", ";
                }
                m.sendTo((Player) sender);
            } else {
                StringBuilder b = new StringBuilder("");
                String j = "";
                for (ArcheMagic magic : ArcheMagic.getMagics()) {
                    b.append(j);
                    b.append(magic.getName());
                    j = ", ";
                }
                sender.sendMessage(b.toString());
            }
            return true;
        } else if (args[0].equalsIgnoreCase("remove")) {
            return true;
        } else if (args[0].equalsIgnoreCase("tier")) {
            return true;
        }
        return false;
    }

    private boolean hasPermission(CommandSender sender, String command) {

        return sender.hasPermission("archecore.command.magic." + command) || sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.magic.mod");
    }

    private boolean h(CommandSender sender) {

        return true;
    }
}
