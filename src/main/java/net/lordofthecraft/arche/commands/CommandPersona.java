package net.lordofthecraft.arche.commands;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.TagAttachment;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.util.AsyncRunner;
import net.lordofthecraft.arche.util.CommandUtil;
import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandPersona implements CommandExecutor {
	private final HelpDesk helpdesk;
	private final ArchePersonaHandler handler;
	private final int delay;
	private final boolean prefix;


    public enum PersonaCommand {
        HELP("archecore.command.persona.help", false, "help", "viewhelp"),
        VIEW("archecore.command.persona.view", true, true, "view", "see", "card"),
        MORE("archecore.command.persona.view.more", true, true, "more", "extrainfo", "viewmore"),
        NAME("archecore.command.persona.name", false, "name", "rename", "setname"),
        PREFIX("archecore.command.persona.prefix", false, "prefix", "setprefix"),
        CLEARPREFIX("archcecore.command.persona.prefix.clear", false, "clearprefix", "delprefix", "rmprefix", "noprefix", "removeprefix"),
        CLEARAGE("archecore.command.persona.age", false, "clearage", "resetage","removeage","noage"),
        AGE("archecore.command.persona.age", false, "age", "setage"),
        BIRTHDATE("archecore.command.persona.age", false, "birthdate", "setbirthdate","birthyear","setbirthyear"),
        CLEARINFO("archecore.command.persona.desc.clear", false, "clearbio", "cleardesc", "deldesc", "delinfo", "clearinfo", "delbio", "cleardescription"),
        ADDINFO("archecore.command.persona.desc", false, "addinfo", "addbio", "bioadd"),
        SETINFO("archecore.command.persona.desc", false, "setbio", "setinfo", "info", "bio"),
        LIST("archecore.command.persona.list", true, "list", "listpersonas", "viewpersonas"),
        PROFESSION("archecore.command.persona.profession", false, "profession", "setprofession", "setprof", "skill", "setskill", "sk"),
        PERMAKILL("archecore.admin.command.persona.permakill", true, "permakill", "pk", "remove", "delete"),
        TIME("archecore.command.persona.time", true, true, "time", "timeplayed", "played", "viewtime", "viewplayed"),
        SETRACE("archecore.command.persona.setrace", false, "setrace", "setvisiblerace", "setapparentrace"),
        REALRACE("archecore.command.persona.realrace", false, "realrace", "viewrealrace", "underlyingrace", "viewunderrace"),
        WIPERACE("archecore.command.persona.wiperace", false, "wiperace", "clearrace", "clearvisrace", "clearoverlying"),
        OPENINV("archecore.command.persona.openinv", true, "openinv", "inv", "viewinv"),
        OPENENDER("archecore.command.persona.openender", true, "openender", "ender", "viewender"),
        ICON("archecore.command.persona.icon", true, "icon", "head", "seticon", "sethead"),
        CREATED("archecore.command.persona.created", true, "created", "viewcreateddate", "viewcreation"),
        CONSTRUCT("archecore.command.persona.construct", true, "construct", "setconstruct"),
        NECROLYTE("archecore.command.persona.necrolyte", true, "necrolyte", "setnecrolyte"),
        SPECTRE("archecore.command.persona.spectre", true, "spectre", "setspectre"),
        ASCENDED("archecore.command.persona.ascended", true, "aengulbound", "setaengulbound"),
        ASSIGNRACE("archecore.admin.command.persona.assignrace", false, "assignrace", "setunderlyingrace", "setunder"),
        ASSIGNGENDER("archecore.admin.command.persona.assigngender", false, "assigngender", "setgender"),
        SETTYPE("archecore.admin.command.persona.settype", false, "settype", "type", "assigntype"),
        READTAG("archecore.admin.command.persona.tag", true, "readtag", "viewtags", "readtags", "tagread", "tags", "tagview"),
        SETTAG("archecore.admin.command.persona.tag.set", false, "settag", "settagvalue", "tagset"),
        DELTAG("archecore.admin.command.persona.tag.remove", false, "deltag", "tagdel"),
        FATIGUEVIEW("archecore.admin.command.persona.fatigue", true, "fatigue", "fatigueview"),
        FATIGUESET("archecore.admin.command.persona.fatigue.set", false, "fatigueset", "setfatigue");

        public final String permission;
        private final String[] aliases;
        public final boolean onearg;
        public final boolean acceptsOffline;

        PersonaCommand(String permission, boolean onearg, String... aliases) {
            this.permission = permission;
            this.aliases = aliases;
            this.onearg = onearg;
            acceptsOffline = false;
        }

        PersonaCommand(String permission, boolean onearg, boolean acceptsOffline, String... aliases) {
            this.permission = permission;
            this.aliases = aliases;
            this.onearg = onearg;
            this.acceptsOffline = acceptsOffline;
        }

        public static PersonaCommand getCommand(String alias) {
            for (PersonaCommand c : PersonaCommand.values()) {
                if (Arrays.asList(c.aliases).contains(alias)) {
                    return c;
                }
            }
            return null;
        }

        public static List<String> getCommandsFromPartial(String partial, CommandSender sender) {
            ArrayList<String> strings = Lists.newArrayList();
            for (PersonaCommand c : PersonaCommand.values()) {
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

        public static List<String> getAllCommands(CommandSender sender) {
            List<String> strings = Lists.newArrayList();
            for (PersonaCommand c : PersonaCommand.values()) {
                if (sender.hasPermission(c.permission)) {
                    strings.addAll(Arrays.asList(c.aliases));
                }
            }
            return strings;
        }
    }

	public CommandPersona(HelpDesk helpdesk, ArchePersonaHandler handler, int delay, boolean prefix) {
		this.helpdesk = helpdesk;
		this.handler = handler;
		this.delay = delay;
		this.prefix = prefix;

		String i = ChatColor.BLUE + "" + ChatColor.ITALIC;
		String a = ChatColor.AQUA + "";
        String b = ChatColor.DARK_RED + "";
        String r = ChatColor.RESET + "";
        String d = ChatColor.DARK_AQUA + "";
        String l = ChatColor.BOLD + "";
        String output = d + "" + l + "How to use the command: " + i + "/persona\n"
                + ChatColor.BLUE + "Type " + i + "/persona [par]" + ChatColor.BLUE + " where " + i + "'[par]'" + ChatColor.BLUE + " is any of the following:\n" + a
				+ i + "$</persona view >view {player}$: " + a + "View the current Character Card of {Player}.\n"
				+ i + "$</persona name >name [new name]$: " + a + "Rename your Persona to the given name.\n"
				+ (prefix ? (i + "$</persona prefix >prefix [prefix]$: " + a + "Sets Persona Prefix (delete with $</persona clearprefix>clearprefix$).\n") : "")
				+ i + "$</persona profession >profession [skill]$: " + a + "Sets your Persona's profession.\n"
				+ i + "$</persona age >age [new age]$: " + a + "Set your Persona's age.\n"
				+ i + "$</persona clearage>clearage$: " + a + "Stop persona age from being shown.\n"
				+ i + "$</persona addbio >addinfo$: " + a + "Add a line of text to your Persona's description..\n"
				+ i + "$</persona clearbio>clearinfo$: " + a + "Clear your Persona's description completely.\n"
				+ i + "$</persona time>time$: " + a + "View the hours spent playing your Persona.\n"
				+ i + "$</persona created>created$: " + a + "View how long ago you created your Persona.\n"
				+ i + "$</persona icon>icon$: " + a + "Save your current skin as your persona icon.\n"
                + i + "$</persona list>list$: " + a + "View all your Personas with names + IDs.\n"
                + i + "$</persona points>points$: " + a + "View and spend your skill points.\n";

        String modOutput = ChatColor.DARK_PURPLE + l + "How to use Persona Moderator Commands\n" +
                d + "[M]" + i + " $</persona setrace >setrace [visiblerace]$: " + a + "Chance the apparent race of a persona. Changes the visible race but not the mechanical race. \n" +
                "(view real race with $</persona realrace >realrace$ or wipe this race with $</persona wiperace >wiperace$)\n" +
                d + "[M]" + i + " $</persona openinv >openinv {player}@{personaid}$: " + a + "Open the inventory of a persona/player.\n" +
                d + "[M]" + i + " $</persona openender>openender {player}@{personaid}$: " + a + "Open the enderchest of this persona/player.\n" +
                d + "[M] @@<Persona Target>Click to view@@ how to run commands on other players.\n" +
                d + "[M] @@<Persona Target Other>Click to learn@@ how to run commands on a players other personas.";

        String targetOtherPlayers = ChatColor.LIGHT_PURPLE + l + "How to run commands on other players\n" +
                d + "[M]" + a + " You can use the flag '-p {player}' to modify another player's persona, e.g. " + r + "/persona name newname -p GenericMinecrafter" + a + " will change GenericMinecrafter's persona name to newname.";

        String targetOtherPersonas = ChatColor.DARK_GREEN + l + "How to run commands on other personas\n" +
                d + "[M]" + a + " In order to effect other personas of a player you can use the format {player}@{personaid}, where personaid is the slot # minus 1. E.g. " + r + "/persona name newname -p GenericMinecrafter@0 " + a +
                "will set GenericMinecrafter's first persona's name to 'newname', while " + r + "/persona name newname -p GenericMinecrafter@3" + a + " will set his fourth persona name to newname.";

        String adminOutput = ChatColor.RED + l + "How to to use Persona Admin commands\n" +
                d + "[" + b + "A" + d + "]" + i + " $</persona permakill >permakill {player}$: " + a + "Force a permakill of a persona. Default your current persona, use with care.\n" +
                d + "[" + b + "A" + d + "]" + i + " $</persona assignrace >assignrace {race} (-p {player}@{personaid})$: " + a + "Sets the underlying race of a persona, default your current persona. Use with care.\n" +
                d + "[" + b + "A" + d + "]" + i + " $</persona assigngender >assigngender {gender} (-p {player}@{personaid})$: " + a + "Sets the gender of a persona, default your current persona. Use with care.\n" +
                d + "[" + b + "A" + d + "]" + i + " $</persona settype >settype {type} (-p {player}@{personaid})$: " + a + "Sets the persona \"type\". Available options: " + r + " NORMAL, EVENT, STAFF, LORE.\n" +
                d + "[" + b + "A" + d + "] Please be careful and cautious using any of these commands, they make lasting changes to personas and could potentially cause errors. Do not use lightly.";


        helpdesk.addInfoTopic("Persona Command", output, "archecore.mayuse");
        helpdesk.addInfoTopic("Persona Mod", modOutput, "archecore.persona.mod");
        helpdesk.addInfoTopic("Persona Target", targetOtherPlayers, "archecore.mod.other");
        helpdesk.addInfoTopic("Persona Target Other", targetOtherPersonas, "archecore.mod.other");
        helpdesk.addInfoTopic("Persona Admin", adminOutput, "archecore.admin");


    }


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			if (sender instanceof Player) helpdesk.outputHelp("persona command", (Player) sender);
			else sender.sendMessage(helpdesk.getHelpText("persona command"));

            if (sender.hasPermission("archecore.mod.persona") && sender instanceof Player) {
                BaseComponent base = new TextComponent("As a persona moderator you have additional commands. Click to view. \n");
                base.addExtra(MessageUtil.CommandButton("Mod", "/archehelp persona mod", "Click to view help."));
                if (sender.hasPermission("archecore.admin")) {
                    base.addExtra(" ");
                    base.addExtra(MessageUtil.CommandButton("Admin", "/archehelp persona admin", "Click to view help."));
                }
                ((Player) sender).spigot().sendMessage(base);
            }

			return true;
		} else {
            PersonaCommand cmd = PersonaCommand.getCommand(args[0]);
            if (cmd == null || cmd == PersonaCommand.HELP) {
                if (sender instanceof Player) helpdesk.outputHelp("persona command", (Player) sender);
                else sender.sendMessage(helpdesk.getHelpText("persona command"));

                if (sender.hasPermission("archecore.mod.persona") && sender instanceof Player) {
                    BaseComponent base = new TextComponent("As a persona moderator you have additional commands. Click to view. \n");
                    base.addExtra(MessageUtil.CommandButton("Mod Commands", "/archehelp persona mod", "Click to view help."));
                    if (sender.hasPermission("archecore.admin")) {
                        base.addExtra(" ");
                        base.addExtra(MessageUtil.CommandButton("Admin Commands", "/archehelp persona admin", "Click to view help."));
                    }
                    ((Player) sender).spigot().sendMessage(base);
                }
                return true;
            }

            if (!sender.hasPermission(cmd.permission)) {
                sender.sendMessage(ChatColor.RED + "Error: You do not have permission to do this.");
                return true;
            }

			//Go through process to find the Persona we want
            OfflinePersona opers = null;
            boolean willTryToLoad = false;	
            if ((cmd == PersonaCommand.VIEW || cmd == PersonaCommand.MORE || cmd == PersonaCommand.LIST)
                    && args.length > 1) {
                opers = CommandUtil.personaFromArg(args[1]);
            } else if (cmd.onearg
                    && args.length > 1
                    && sender.hasPermission("archecore.mod.other")) {
                opers = CommandUtil.personaFromArg(args[1]);
                if (opers == null && sender instanceof Player)
                    willTryToLoad = loadAndReexecute(args[1], (Player) sender, command, args);
            } else if (args.length > 2 && args[args.length - 2].equalsIgnoreCase("-p") 
            		&& (sender.hasPermission("archecore.mod.other"))) {
                opers = CommandUtil.personaFromArg(args[args.length - 1]);
                if (opers == null && sender instanceof Player)
                    willTryToLoad = loadAndReexecute(args[args.length - 1], (Player) sender, command, args);
            } else if (sender instanceof Player) {
                opers = handler.getPersona((Player) sender);
            }

            if (opers == null) {
                if (willTryToLoad)
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Error: Persona not loaded. Will try to load it now. Please wait...");
                else sender.sendMessage(ChatColor.RED + "Error: No persona found to modify");
                return true;
            }

            if (cmd.acceptsOffline) {
                if (cmd == PersonaCommand.VIEW || cmd == PersonaCommand.MORE) {
                    boolean extendedInfo = args[0].equalsIgnoreCase("more");

                    Player t = Bukkit.getPlayer(opers.getPlayerUUID());
                    if (t != null && !handler.mayUse(t)) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "This player is a Wandering Soul (may not use Personas)");
                    } else {
                        //If the persona is found the Whois should always succeed
                        //We have assured the persona is found earlier
                        boolean mod = sender.hasPermission("archecore.mod.other");
                        (extendedInfo ? handler.whoisMore(opers, mod, sender == opers.getPlayer()) : handler.whois(opers, mod))
                                .forEach(o -> MessageUtil.send(o, sender));
                    }
                    return true;
                } else if (cmd == PersonaCommand.TIME) {
                    sender.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GOLD.toString() + ChatColor.BOLD + (int) Math.floor(opers.getTimePlayed() / 60) + ChatColor.AQUA + " hours on " + opers.getName() + " in " + ArcheCore.getControls().getServerWorldName() + ".");
                    return true;
                }
            } else {
                if (!opers.isLoaded()) {
                    sender.sendMessage(ChatColor.RED + "Error: This persona is not active currently.");
                    return true;
                }
                Persona pers = (Persona) opers;
                if (cmd == PersonaCommand.CREATED) {
                    String time = millsToDaysHours(System.currentTimeMillis() - pers.getCreationTime().getTime());
                    sender.sendMessage(ChatColor.AQUA + "You created " + pers.getName() + ChatColor.GOLD.toString() + ChatColor.BOLD + time + ChatColor.AQUA + " ago.");
                    return true;
                } else if (cmd == PersonaCommand.CLEARPREFIX && prefix) {
                    pers.clearPrefix();
                    sender.sendMessage(ChatColor.AQUA + "Persona prefix was cleared for " + pers.getName() + ".");
                    return true;
                } else if (cmd == PersonaCommand.CLEARINFO) {
                    pers.clearDescription();
                    sender.sendMessage(ChatColor.AQUA + "Persona description was cleared for " + pers.getName() + ".");
                    return true;
                } else if (cmd == PersonaCommand.CLEARAGE) {
                    pers.setDateOfBirth(0);
                    sender.sendMessage(ChatColor.AQUA + "Persona age was cleared for " + pers.getName() + ".");
                    return true;
                } else if (cmd == PersonaCommand.LIST) {
                    ArchePersona[] personas = handler.getAllPersonas(pers.getPlayerUUID());
                    sender.sendMessage(ChatColor.AQUA + ArcheCore.getControls().getPlayerNameFromUUID(pers.getPlayerUUID()) + "'s personas:");
                    for (int i = 0; i <= 3; i++) {
                        Persona persona = personas[i];
                        if (persona != null) {
                            sender.sendMessage(ChatColor.GRAY + "[" + i + "] " + ChatColor.AQUA + persona.getName());
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "[" + i + "] " + ChatColor.WHITE + "Empty");
                        }
                    }
                    return true;
                } else if (cmd == PersonaCommand.PERMAKILL) {
                    Player other = pers.getPlayer();

                    if (!(args.length > 1)) {
                        sender.sendMessage(ChatColor.RED + "Don't delete yourself by mistake friend!");
                        return true;
                    }

                    //Only do if online else problems
                    else if (other == null) {
                        sender.sendMessage(ChatColor.RED + "Too risky to remove personas of offline players");
                        return true;
                    }

                    if (pers.remove()) {
                        if (handler.countPersonas(other) == 0 && !other.hasPermission("archecore.exempt"))
                            other.kickPlayer("Your final Persona was Permakilled. Please relog.");
                        else
                            other.sendMessage(ChatColor.AQUA + "A persona of yours was Permakilled: " + ChatColor.RESET + pers.getName());
                        sender.sendMessage(ChatColor.AQUA + "You have permakilled Persona " + ChatColor.WHITE + pers.getName() + ChatColor.AQUA + " belonging to " + ChatColor.WHITE + pers.getPlayerName());
                    } else sender.sendMessage(ChatColor.RED + "I'm afraid I can't do that.");
                    return true;
                } else if (cmd == PersonaCommand.PROFESSION && args.length == 1 && sender instanceof Player) {
                    sender.sendMessage(ChatColor.BLUE + "Available Professions: " + ChatColor.DARK_GRAY + "[Click for Info]");
                    final BaseComponent m = new TextComponent();
                    ArcheSkillFactory.getSkills().values().stream().forEach(s -> {
                        m.addExtra(MessageUtil.CommandButton(s.getName(), "/archehelp " + s.getName()));
                        m.addExtra("  ");
                    });
                    MessageUtil.addNewlines(m);
                    MessageUtil.send(m, sender);

                    BaseComponent m2 = new TextComponent("Select a profession by using ");
                    m2.setColor(MessageUtil.convertColor(ChatColor.BLUE));
                    BaseComponent extra = new TextComponent("/persona skill [skillname]");
                    extra.setColor(MessageUtil.convertColor(ChatColor.GOLD));
                    extra.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/persona skill "));
                    m2.addExtra(extra);
                    MessageUtil.send(m2, sender);
                    return true;
                } else if (args.length > 1) {
                    if (cmd == PersonaCommand.NAME) {
                        int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
                        String name = StringUtils.join(args, ' ', 1, parseTo);

                        long timeLeft = (pers.getRenamed().getTime() / 60000) - (System.currentTimeMillis() / 60000) + delay;
                        if (timeLeft > 0 && !sender.hasPermission("archecore.persona.quickrename")) {
                            sender.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " minutes before renaming again");
                        } else if (name.length() <= 32 || sender.hasPermission("archecore.persona.longname")) {
                            pers.setName(name);
                            sender.sendMessage(ChatColor.AQUA + "Persona name was set to: " + ChatColor.RESET + name);
                            if (sender == pers.getPlayer()) {
                                ArcheCore.getConsumerControls().queueRow(new UpdatePersonaRow(pers, PersonaField.STAT_RENAMED, new Timestamp(System.currentTimeMillis())));
                            } //Player renamed by his own accord
                        } else {
                            sender.sendMessage(ChatColor.RED + "Error: Name too long. Max length 32 characters");
                        }
                        return true;

                    } else if (cmd == PersonaCommand.PREFIX && prefix) {
                        int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
                        String name = StringUtils.join(args, ' ', 1, parseTo);

                        if (name.length() <= 16) {
                            pers.setPrefix(name);
                            sender.sendMessage(ChatColor.AQUA + "Set the prefix of " + pers.getName() + " to: " + ChatColor.RESET + name);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Error: Prefix too long. Max length 16 characters");
                        }
                        return true;
                    } else if (cmd == PersonaCommand.AGE || cmd == PersonaCommand.BIRTHDATE) {
                        try {
                        	/* wow still no women on the dev team? Didn't you know its*/ int currentYear =  ArcheCore.getControls().getCalendar().getYear();
                        	int parsedArg = Integer.parseInt(args[1]);
                        	int birthyear = cmd == PersonaCommand.AGE? currentYear - parsedArg : parsedArg;
                        	
                        	if(birthyear >= currentYear || birthyear <= 0)
                        		return false;
                        	int age = currentYear - birthyear;
                        	if(!sender.hasPermission("archecore.ageless") && (age < 5 || age > pers.getRace().getMaximumAge())) {
                        		sender.sendMessage(ChatColor.RED + "Error: Age outside of the acceptable age range of " + ChatColor.RESET + "5-"+pers.getRace().getMaximumAge());
                        		return false;
                        	}
                        	
                        	pers.setDateOfBirth(birthyear);
                        	sender.sendMessage(ChatColor.AQUA + "Set birthyear of " + pers.getName() + " to: " + ChatColor.RESET + birthyear 
                        			+ ChatColor.AQUA + " (age: " + ChatColor.RESET + age + ChatColor.AQUA + ")");                    		
                        	return true;
                        } catch(NumberFormatException e) {
                        	return false;
                        }
                    } else if (cmd == PersonaCommand.ADDINFO) {
                        int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
                        String line = StringUtils.join(args, ' ', 1, parseTo);

                        int length = line.length();
                        if (pers.getDescription() != null) length += pers.getDescription().length();
                        if (length > 150 && !sender.hasPermission("archecore.persona.longbio")) {
                            sender.sendMessage(ChatColor.RED + "Error: Description too long.");
                        } else {
                            pers.addDescription(line);
                            sender.sendMessage(ChatColor.AQUA + pers.getName() + "'s description now reads: " + ChatColor.RESET + pers.getDescription());
                        }

                        return true;
                    } else if (cmd == PersonaCommand.PROFESSION) {
                        if (pers.getMainSkill() == null || sender.hasPermission("archecore.command.persona.profession.switch")) {
                            Skill skill = ArcheCore.getControls().getSkill(args[1]);
                            if (skill == null) {
                                sender.sendMessage(ChatColor.RED + "Error: This profession could not be found!");
                            } else if (args.length == 3 && "confirm".equals(args[2])) {
                                pers.setMainSkill(skill);
                                sender.sendMessage(ChatColor.GOLD + "You have dedicated yourself to " + ChatColor.RESET + skill.getName());
                            } else { //ARE YOU SURE~?!
                                sender.sendMessage(ChatColor.GRAY + "You are wishing to become a " + ChatColor.GOLD + skill.getProfessionalName(pers.isFemale()));
                                sender.sendMessage(ChatColor.GRAY + "Selecting a profession will take dedication, and you cannot change your mind afterwards. If you are absolutely sure about your selection, please confirm below.");
                                MessageUtil.CommandButton("Yes, I will be a " + skill.getProfessionalName(pers.isFemale()), "/persona skill " + skill.getName() + " confirm");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You have already selected a profession!");
                        }
                        return true;
                    } else if (cmd == PersonaCommand.SETINFO) {
                        int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
                        String line = StringUtils.join(args, ' ', 1, parseTo);

                        pers.setDescription(line);
                        sender.sendMessage(ChatColor.AQUA + pers.getName() + "'s description now reads: " + line);
                        return true;
                    } else if (cmd == PersonaCommand.SETRACE) {
                        int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
                        String race = StringUtils.join(args, ' ', 1, parseTo);
                        pers.setApparentRace(race);
                        sender.sendMessage(ChatColor.AQUA + "Set visible race of " + pers.getName() + " to: " + ChatColor.RESET + race);
                        return true;
                    } else if (cmd == PersonaCommand.ASSIGNGENDER) {
                        String gender = args[1].toLowerCase();
                        switch (gender) {
                            case "male":
                            case "female":
                            case "other": {
                                pers.setGender(gender);
                                sender.sendMessage(ChatColor.AQUA + "Set gender of " + pers.getName() + " to: " + ChatColor.RESET + gender);
                                return true;
                            }
                            default:
                                sender.sendMessage(ChatColor.RED + "Please enter 'male', 'female', or 'other'.");
                                return true;
                        }
                    } else if (cmd == PersonaCommand.REALRACE) {
                        sender.sendMessage(ChatColor.AQUA + "Underlying for " + pers.getName() + " is: " + ChatColor.GOLD + pers.getRace().getName());
                        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "(Visible: " + pers.getRaceString(sender.hasPermission("archecore.mod.persona")) + ")");
                        return true;
                    } else if (cmd == PersonaCommand.WIPERACE) {
                        sender.sendMessage(ChatColor.AQUA + "Visible race of " + pers.getName() + " has been reset to from '" + pers.getRaceString(sender.hasPermission("archecore.mod.persona")) + "' to: " + ChatColor.RESET + pers.getRace().getName());
                        pers.setApparentRace(null);
                        return true;
                    } else if (cmd == PersonaCommand.ASSIGNRACE) {
                        int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
                        Race race = findRace(StringUtils.join(args, ' ', 1, parseTo));
                        if (race == null) {
                            sender.sendMessage(ChatColor.RED + "Please enter a valid race.");
                        } else if (pers.getRace() == race) {
                            sender.sendMessage(ChatColor.RED + "Race for " + pers.getName() + " is already " + race.getName());
                        } else {
                            doRaceChange(sender, pers, race);
                        }
                        return true;
                    } else if (cmd == PersonaCommand.SETTYPE) {
                        PersonaType type = PersonaType.getType(args[1]);
                        if (type != null) {
                            pers.setPersonaType(type);
                            sender.sendMessage(ChatColor.AQUA + "The Persona Type of " + pers.getName() + " has been set to " + type.readableName);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Error! Could not set the persona type of " + pers.getName() + "! Acceptable values are: " + ChatColor.RESET + " NORMAL, LORE, EVENT, STAFF");
                        }
                        return true;
                    } else if (cmd == PersonaCommand.OPENINV) {
                        if (sender instanceof Player) {
                            Player pl = (Player) sender;
                            pl.closeInventory();
                            sender.sendMessage(ChatColor.AQUA + "Opening inventory contents for " + pers.getName() + ".");
                            pl.openInventory(pers.getInventory());

                        } else {
                            sender.sendMessage(ChatColor.RED + "This command can only be run from in game!");
                        }
                        return true;
                    } else if (cmd == PersonaCommand.OPENENDER) {
                        if (sender instanceof Player) {
                            Player pl = (Player) sender;
                            pl.closeInventory();
                            Inventory inv = pers.getEnderChest();
                            sender.sendMessage(ChatColor.AQUA + "Opening enderchest contents for " + pers.getName() + ".");
                            pl.openInventory(inv);
                        } else {
                            sender.sendMessage(ChatColor.RED + "This command can only be run from in game!");
                        }
                        return true;
                    } else if (cmd == PersonaCommand.READTAG) {
                        sender.sendMessage("");
                        sender.sendMessage("~~~~ " + ChatColor.GOLD + pers.getName() + "" + ChatColor.RESET + "'s tags. ~~~~");
                        if (pers.tags().getTags().size() == 0) {
                            sender.sendMessage(ChatColor.RED + "None!");
                        } else {
                            for (Map.Entry<String, TagAttachment> ent : pers.tags().getTagMap().entrySet()) {
                                sender.sendMessage(ChatColor.BLUE + ent.getKey() + ": \"" + ChatColor.GRAY + ent.getValue().getValue() + '"') ;
                            }
                        }
                        return true;
                    } else if (cmd == PersonaCommand.SETTAG) {
                        if (args.length >= 3 && !args[1].equalsIgnoreCase("-p") && !args[2].equalsIgnoreCase("-p")) {
                            String key = args[1];
                            String value = args[2];
                            pers.tags().giveTag(key, value);
                            sender.sendMessage(ChatColor.AQUA + key + " has been set to " + value + " for " + MessageUtil.identifyPersona(pers));
                        } else {
                            sender.sendMessage(ChatColor.RED + "Use /help Persona Command to see the syntax for this.");
                        }
                        return true;
                    } else if (cmd == PersonaCommand.DELTAG) {
                        if (!args[1].equalsIgnoreCase("-p")) {
                            if (!pers.tags().hasTag(args[1])) {
                                sender.sendMessage(ChatColor.RED + "Error: " + MessageUtil.identifyPersona(pers) + " doesn't have a tag with the value of " + args[1]);
                            } else {
                                pers.tags().removeTag(args[1]);
                                sender.sendMessage(ChatColor.GREEN + "Success! Removed the tag " + args[1] + " from " + MessageUtil.identifyPersona(pers));
                            }
                        }
                        return true;
                    } else if (cmd == PersonaCommand.FATIGUEVIEW) {
                        sender.sendMessage(ChatColor.AQUA + "The persona " + pers.getName() + ChatColor.GRAY + " (" + pers.getPlayerName() + ")" + ChatColor.AQUA + " has " + ChatColor.GOLD + pers.getFatigue() + ChatColor.AQUA + "/" + pers.attributes().getAttributeValue(AttributeRegistry.MAX_FATIGUE) + " fatigue");
                        return true;
                    } else if (cmd == PersonaCommand.FATIGUESET) {
                        if (NumberUtils.isNumber(args[1])) {
                            pers.setFatigue(Double.valueOf(args[1]));
                            sender.sendMessage(ChatColor.GREEN + "Success!" + ChatColor.BLUE + " The persona " + MessageUtil.identifyPersona(pers) + " has had their fatigue set to " + ChatColor.GOLD + pers.getFatigue());
                            return true;
                        }
                        return false;
                    } else if (cmd == PersonaCommand.CONSTRUCT) {
                        return doRaceChange(sender, pers, Race.CONSTRUCT);
                    } else if (cmd == PersonaCommand.SPECTRE) {
                        return doRaceChange(sender, pers, Race.SPECTRE);
                    } else if (cmd == PersonaCommand.NECROLYTE) {
                        return doRaceChange(sender, pers, Race.NECROLYTE);
                    } else if (cmd == PersonaCommand.ASCENDED) {
                        return doRaceChange(sender, pers, Race.ASCENDED);
                    }
                }
            }
		}
		return false;
	}

	private boolean doRaceChange(CommandSender sender, Persona pers, Race race) {
		ArchePersona apers = (ArchePersona) pers;
		apers.setRace(race);
		sender.sendMessage(ChatColor.AQUA + "Underlying race for " + pers.getName()+ " has been changed to: " + ChatColor.RESET+ race.getName());
		Player p = pers.getPlayer();
		if (sender != p && p != null) {
			sender.sendMessage(ChatColor.AQUA + "Underlying race for " + pers.getName()+ " has been changed to: " + ChatColor.RESET + race.getName());
		}
		return true;
	}

	private Race findRace(String s) {
		s = s.replace('\'', ' ');
		for (Race r : Race.values()) {
			if (s.equalsIgnoreCase(r.getName().replace('\'', ' '))) return r;
		}
		return null;
	}

	/**
	 * Convert a millisecond duration to a string format
	 * 
	 * @param millis A duration to convert to a string form
	 * @return A string of the form "X Days Y Hours.
	 */
	public static String millsToDaysHours(long millis)
	{
		if(millis < 0)

		{
			throw new IllegalArgumentException("Duration must be greater than zero!");
		}

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);

		String sb = String.valueOf(days) +
				" days and " +
				hours +
				" hours";

		return(sb);
	}

    private boolean loadAndReexecute(String personaToLoad, Player caller, Command command, String[] args) {
        final OfflinePersona offlinePersona = CommandUtil.offlinePersonaFromArg(personaToLoad);
        if (offlinePersona == null) return false; //Nothing we can do at this point
        if (offlinePersona.isLoaded()) throw new IllegalStateException("Persona is already loaded!");

        new AsyncRunner(ArcheCore.getPlugin()) {
			ArchePersona persona = null;
			
			public void doAsync() { persona = (ArchePersona) offlinePersona.loadPersona();}

			@Override
			public void andThen() {
                ArchePersona otherPersona = ArchePersonaHandler.getInstance().getPersonaStore().addOnlinePersona(persona);
                if(otherPersona != persona) {
                	ArcheCore.getPlugin().getLogger().warning("Interleaved Persona loading: Persona " + MessageUtil.identifyPersona(persona)
                	+ " has come online while " + caller.getName() +  " also tried to load it.");
                }
                caller.performCommand(command.getName() + ' ' + StringUtils.join(args, ' '));
			}
        }.go();

        return true;
    }

}