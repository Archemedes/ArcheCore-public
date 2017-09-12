package net.lordofthecraft.arche.commands;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.magic.Archenomicon;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created on 7/20/2017
 *
 * @author 501warhead
 */
public class CommandArchenomicon implements CommandExecutor {

    public enum MagicCommand {
        HELP("archecore.command.magic.help", "help"),
        CREATURELIST("archecore.command.magic.creature.list", "creaturelist", "creatures"),
        CREATUREADD("archecore.command.magic.creature.create", "creatureadd"),
        CREATUREREMOVE("archecore.command.magic.creature.remove", "creatureremove", "delcreature"),
        CREATUREINFO("archecore.command.magic.creature.view", "creature", "creatureinfo"),
        CREATUREWHOIS("archecore.command.magic.creature.listplayers", "creaturewhois"),
        MAGICADD("archecore.command.magic.add", "magicadd", "mgadd"),
        MAGICREMOVE("archecore.command.magic.remove", "magicremove", "magicdel"),
        MAGICLIST("archecore.command.magic.list", "magics", "magiclist"),
        MAGICINFO("archecore.command.magic.view", "magicinfo", "magic"),
        ARCHETYPEADD("archecore.command.magic.archetype.add", "archetypeadd"),
        ARCHETYPELIST("archecore.command.magic.archetype.list", "archetypelist"),
        ARCHETYPEREMOVE("archecore.command.magic.archetype.remove", "archetyperemove"),
        SETMAGIC("archecore.command.magic.teach", "teachmagic"),
        SETCREATURE("archecore.command.magic.creature.teach", "setcreature");

        public final String permission;
        public final String[] aliases;

        MagicCommand(String permission, String... aliases) {
            this.permission = permission;
            this.aliases = aliases;
        }

        public static MagicCommand getCommand(String alias) {
            for (MagicCommand c : MagicCommand.values()) {
                if (Arrays.asList(c.aliases).contains(alias)) {
                    return c;
                }
            }
            return null;
        }

        public static List<String> getCommandsFromPartial(CommandSender sender, String partial) {
            ArrayList<String> strings = Lists.newArrayList();
            for (MagicCommand c : MagicCommand.values()) {
                if (sender.hasPermission(c.permission)) {
                    strings.addAll(Arrays.asList(c.aliases));
                }
            }
            if (!partial.isEmpty()) {
                strings.removeIf(s -> !s.startsWith(partial));
            }
            strings.sort(Comparator.naturalOrder());
            return strings;
        }
    }

    private final Archenomicon archenomicon;

    public CommandArchenomicon(Archenomicon archenomicon) {
        this.archenomicon = archenomicon;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length == 0) {
            return h(sender);
        }
        MagicCommand mcmd = MagicCommand.getCommand(args[0]);
        if (mcmd == null) {

            return h(sender);
        }
        if (!sender.hasPermission(mcmd.permission)) {
            sender.sendMessage(ChatColor.RED + "ERROR: You do not have permission for this command!");
            return true;
        }
        if (mcmd == MagicCommand.HELP) {
            return h(sender);
        } else if (mcmd == MagicCommand.MAGICLIST) {

        } else if (mcmd == MagicCommand.CREATURELIST) {

        } else if (mcmd == MagicCommand.ARCHETYPELIST) {

        }
        return false;
    }

    private boolean h(CommandSender sender) {

        return true;
    }
}
