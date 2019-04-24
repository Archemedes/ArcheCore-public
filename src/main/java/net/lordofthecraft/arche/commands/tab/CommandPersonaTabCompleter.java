package net.lordofthecraft.arche.commands.tab;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.commands.CommandPersona;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * Provides in depth tab completion for /persona, hopefully making this command very moderator friendly.
 *
 * @author 501warhead
 */
public class CommandPersonaTabCompleter implements TabCompleter {

    public CommandPersonaTabCompleter() {
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("persona")) {
            if (args.length == 1) {
                return CommandPersona.PersonaCommand.getCommandsFromPartial(args[0], commandSender);
            } else if (args.length == 0) {
                return CommandPersona.PersonaCommand.getAllCommands(commandSender);
            } else {
                CommandPersona.PersonaCommand pscmd = CommandPersona.PersonaCommand.getCommand(args[0]);
                if (pscmd != null) {
                    if (!commandSender.hasPermission(pscmd.permission)) {
                        return null;
                    }
                    if (pscmd == CommandPersona.PersonaCommand.ASSIGNRACE && args.length == 2) {
                        return Lists.newArrayList(Race.values()).stream().map(Race::name).filter(ss -> args[1].isEmpty() || ss.startsWith(args[1].toUpperCase())).collect(Collectors.toList());
                    } else if (pscmd == CommandPersona.PersonaCommand.SETTYPE && args.length == 2) {
                        return Lists.newArrayList(PersonaType.values()).stream().map(PersonaType::name).filter(ss -> args[1].isEmpty() || ss.startsWith(args[1].toUpperCase())).collect(Collectors.toList());
                    } else if (commandSender.hasPermission("archecore.mod.other") && (args[args.length - 2].equalsIgnoreCase("-p") || (pscmd.onearg && args.length == 2))) {
                        String user = args[args.length - 1];
                        if (user.endsWith("@")) {
                            user = user.substring(0, user.lastIndexOf("@"));
                        }
                        return getValuesForPlayer(user);
                    } else if (!args[args.length - 1].equalsIgnoreCase("-p") && !pscmd.onearg) {
                        for (String arg : args) {
                            if (arg.equalsIgnoreCase("-p")) {
                                return Lists.newArrayList();
                            }
                        }
                        return Lists.newArrayList("-p");
                    }/* else if (commandSender instanceof Player) {
                        Player pl = (Player) commandSender;
                        if (pscmd == CommandPersona.PersonaCommand.PROFESSION) {
                            return ArcheSkillFactory.getSkills().values().stream().filter(m -> m.isVisible(pl)).map(ArcheSkill::getName).filter(ss -> args[1].isEmpty() || ss.startsWith(args[1].toUpperCase())).collect(Collectors.toList());
                        }

                    }*/
                }
            }
        }
        return null;
    }

    /**
     * Creates a list of probable results from a username or a partial username.
     * <p>
     * The way this operates is that we need the username first so if the username is invalid, e.g. 501w, we want to get 501warhead before we start looking for personas
     * So all players who have a username that starts with 501w and have personas are fetched, this continues until a valid username is passed in.
     * <p>
     * Once a valid username has been retrieved the personas of the found player will be filtered through for all valid ids, so the end result is that tab completion will
     * be highly accurate and step-by-step while hopefully being performant.
     * <p>
     * Fingers crossed that this is cached though.
     *
     * @param username The username, either full one (501warhead) or a partial one (501wa).
     * @return A list of matches for tab completion
     */
    public static List<String> getValuesForPlayer(String username) {
        Player pl = Bukkit.getPlayerExact(username);
        if (pl == null) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(ArcheCore::hasPersona)
                    .map(Player::getName)
                    .filter(ss -> username.isEmpty() || ss.startsWith(username))
                    .collect(Collectors.toList());
        }
        List<String> vals = Lists.newArrayList();
        ArchePersona[] personas = ArchePersonaHandler.getInstance().getAllPersonas(pl);
        if (personas != null) {
            for (ArchePersona pers : personas) {
                if (pers != null) {
                    vals.add(username + "@" + pers.getSlot());
                }
            }
        }
        return vals;
    }
}
