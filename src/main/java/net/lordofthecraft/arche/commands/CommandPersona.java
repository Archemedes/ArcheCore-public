package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.persona.UpdateTask;
import net.lordofthecraft.arche.util.CommandUtil;
import net.lordofthecraft.arche.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class CommandPersona implements CommandExecutor {
	private final HelpDesk helpdesk;
	private final ArchePersonaHandler handler;
	private final int delay;
	private final boolean prefix;

	public CommandPersona(HelpDesk helpdesk, ArchePersonaHandler handler, int delay, boolean prefix) {
		this.helpdesk = helpdesk;
		this.handler = handler;
		this.delay = delay;
		this.prefix = prefix;

		String i = ChatColor.BLUE + "" + ChatColor.ITALIC;
		String a = ChatColor.AQUA + "";
		String output = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "How to use the command: " + i + "/persona\n"
				+ ChatColor.BLUE + "Type " + i + "/persona [par]" + ChatColor.BLUE + " where " + i + "'[par]'" + ChatColor.BLUE + " is any of the following:\n" + a
				+ i + "$</persona view >view {player}$: " + a + "View the current Character Card of {Player}.\n"
				+ i + "$</persona name >name [new name]$: " + a + "Rename your Persona to the given name.\n"
				+ (prefix ? (i + "$</persona prefix >prefix [prefix]$: " + a + "Sets Persona Prefix (delete with $</persona clearprefix>clearprefix$).\n") : "")
				+ i + "$</persona profession >profession [skill]$: " + a + "Sets your Persona's profession.\n"
				+ i + "$</persona age >age [new age]$: " + a + "Set your character's age.\n"
				+ i + "$</persona autoage>autoage$: " + a + "Toggle automatic aging for this persona.\n"
				+ i + "$</persona addbio >addinfo$: " + a + "Add a line of text to your Persona's description..\n"
				+ i + "$</persona clearbio>clearinfo$: " + a + "Clear your Persona's description completely.\n"
				+ i + "$</persona time>time$: " + a + "View the hours spent playing your Persona.\n"
				+ i + "$</persona created>created$: " + a + "View how long ago you created your Persona.\n"
				+ i + "$</persona icon>icon$: " + a + "Save your current skin as your persona icon.\n"
				+ i + "$</persona list>list$: " + a + "View all your Personas with names + IDs.\n";

		helpdesk.addInfoTopic("Persona Command", output);
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			if (sender instanceof Player) helpdesk.outputHelp("persona command", (Player) sender);
			else sender.sendMessage(helpdesk.getHelpText("persona command"));

			if (sender.hasPermission("archecore.mod.persona")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] Change apparant race with 'setrace'. This changes visible race, but not the underlying race.");
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] View the real race of a persona with 'realrace' and reset the apparent race with 'wiperace.");
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] Open the inventory of a persona with openinv [player]@[personaid]");
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can add the flag '-p {player}' to the end of the command to modify someone's current Persona.");
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can use [player]@[personaid] to modify a different Persona");
			}

			if (sender.hasPermission("archecore.admin")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.DARK_RED + "" + ChatColor.BOLD + "A" + ChatColor.DARK_AQUA + "] Force a permakill with 'permakill [persona]'. Default on your current Persona");
				sender.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.DARK_RED + "" + ChatColor.BOLD + "A" + ChatColor.DARK_AQUA + "] Reassign the underlying race with 'assignrace'");
				sender.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.DARK_RED + "" + ChatColor.BOLD + "A" + ChatColor.DARK_AQUA + "] Perform gender reassignment surgery with 'assigngender'.");
			}
			return true;
		} else {

			//Go through process to find the Persona we want
			Persona pers = null;
			if ((args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("more") ||
					args[0].equalsIgnoreCase("list"))
					&& args.length > 1) {
				pers = CommandUtil.personaFromArg(args[1]);
			} else if ((args[0].equalsIgnoreCase("permakill")
					|| args[0].equalsIgnoreCase("time")
					|| args[0].equalsIgnoreCase("construct")
					|| args[0].equalsIgnoreCase("spectre")
					|| args[0].equalsIgnoreCase("necrolyte")
					|| args[0].equalsIgnoreCase("golem")
					|| args[0].equalsIgnoreCase("spectral")
					|| args[0].equalsIgnoreCase("specter")
					|| args[0].equalsIgnoreCase("necro")
					|| args[0].equalsIgnoreCase("ascended")
					|| args[0].equalsIgnoreCase("aengulbound")
					|| args[0].equalsIgnoreCase("keeper")
					|| args[0].equalsIgnoreCase("realrace")
					|| args[0].equalsIgnoreCase("wiperace")
					|| args[0].equalsIgnoreCase("openinv")
                    || args[0].equalsIgnoreCase("openender")
                    || args[0].equalsIgnoreCase("head")
					|| args[0].equalsIgnoreCase("icon")
					|| args[0].equalsIgnoreCase("created"))
					&& args.length > 1
					&& (sender.hasPermission("archecore.mod.persona") || sender.hasPermission("archecore.mod.other"))) {
				pers = CommandUtil.personaFromArg(args[1]);
			} else if (args.length > 2 && args[args.length - 2].equalsIgnoreCase("-p") && (sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.persona"))) {
				pers = CommandUtil.personaFromArg(args[args.length - 1]);
			} else if (sender instanceof Player) {
				pers = handler.getPersona((Player) sender);
			}

			if (pers == null) {
				sender.sendMessage(ChatColor.RED + "Error: No persona found to modify");
				return true;
			}

			if (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("more")) {
				boolean extendedInfo = args[0].equalsIgnoreCase("more");
				
				Player t = Bukkit.getPlayer(pers.getPlayerUUID());
				if (t != null && !handler.mayUse(t)) {
					sender.sendMessage(ChatColor.DARK_AQUA + "This player is a Wandering Soul (may not use Personas)");
				} else {
					//If the persona is found the Whois should always succeed
					//We have assured the persona is found earlier
					boolean mod = sender.hasPermission("archecore.mod.other");
					(extendedInfo ? handler.whoisMore(pers, mod, sender == pers.getPlayer()) : handler.whois(pers, mod))
							.forEach(o->MessageUtil.send(o, sender));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("time")) {
				sender.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GOLD.toString() + ChatColor.BOLD + (int)Math.floor(pers.getTimePlayed() / 60) + ChatColor.AQUA + " hours on " + pers.getName() + " in " + ArcheCore.getControls().getServerWorldName() + ".");
				sender.sendMessage(ChatColor.AQUA + "You have a total of " + ChatColor.GOLD.toString() + ChatColor.BOLD + (int)Math.floor(pers.getTotalPlaytime() / 60) + ChatColor.AQUA + " hours on " + pers.getName() + "!");
				return true;
			} else if (args[0].equalsIgnoreCase("created")) {
                String time = millsToDaysHours(System.currentTimeMillis() - pers.getCreationTime().getTime());
                sender.sendMessage(ChatColor.AQUA + "You created " + pers.getName() + ChatColor.GOLD.toString() + ChatColor.BOLD + time + ChatColor.AQUA + " ago.");
				return true;
			} else if (args[0].equalsIgnoreCase("clearprefix") && prefix) {
				pers.clearPrefix();
				sender.sendMessage(ChatColor.AQUA + "Persona prefix was cleared for " + pers.getName() + ".");
				return true;
			} else if (args[0].equalsIgnoreCase("clearbio") || args[0].equalsIgnoreCase("clearinfo") || args[0].equalsIgnoreCase("cleardesc") || args[0].equalsIgnoreCase("cleardescription")) {
				pers.clearDescription();
				sender.sendMessage(ChatColor.AQUA + "Persona description was cleared for " + pers.getName() + ".");
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				ArchePersona[] personas = handler.getAllPersonas(pers.getPlayerUUID());
				sender.sendMessage(ChatColor.AQUA + ArcheCore.getPlugin().getServer().getOfflinePlayer(pers.getPlayerUUID()).getName() + "'s personas:");
				for (int i = 0; i <= 3; i++ ) {
					Persona persona = personas[i];
					if (persona != null) {
						sender.sendMessage(ChatColor.GRAY + "[" + i + "] " + ChatColor.AQUA + persona.getName());
					} else {
						sender.sendMessage(ChatColor.GRAY + "[" + i + "] " + ChatColor.WHITE + "Empty");
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("permakill")) {
				if (!sender.hasPermission("archecore.admin")) {
					sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
				} else {
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
							other.sendMessage(ChatColor.AQUA+ "A persona of yours was Permakilled: " + ChatColor.RESET + pers.getName());
						sender.sendMessage(ChatColor.AQUA + "You have permakilled Persona " + ChatColor.WHITE + pers.getName() + ChatColor.AQUA + " belonging to " + ChatColor.WHITE + pers.getPlayerName());
					} else sender.sendMessage(ChatColor.RED + "I'm afraid I can't do that.");
				}
				return true;
			} else if (args.length > 1) {
				if (args[0].equalsIgnoreCase("name")) {
					int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
					String name = StringUtils.join(args, ' ', 1, parseTo);

                    long timeLeft = (pers.getRenamed().getTime() / 60000) - (System.currentTimeMillis() / 60000) + delay;
                    if (timeLeft > 0 && !sender.hasPermission("archecore.persona.quickrename")) {
						sender.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " minutes before renaming again");
					} else if (name.length() <= 32 || sender.hasPermission("archecore.persona.longname")) {
						pers.setName(name);
						sender.sendMessage(ChatColor.AQUA + "Persona name was set to: " + ChatColor.RESET + name);
						if (sender == pers.getPlayer()) {
                            SaveHandler.getInstance().put(new UpdateTask(pers, PersonaField.STAT_RENAMED, new Timestamp(System.currentTimeMillis())));
                        } //Player renamed by his own accord
						//SaveHandler.getInstance().put(new PersonaRenameTask(pers));
					} else {
						sender.sendMessage(ChatColor.RED + "Error: Name too long. Max length 32 characters");
					}
					return true;

				} else if (args[0].equalsIgnoreCase("prefix") && prefix) {
					int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
					String name = StringUtils.join(args, ' ', 1, parseTo);

					if (name.length() <= 16) {
						pers.setPrefix(name);
						sender.sendMessage(ChatColor.AQUA + "Set the prefix of " + pers.getName() + " to: " + ChatColor.RESET + name);
					} else {
						sender.sendMessage(ChatColor.RED + "Error: Prefix too long. Max length 16 characters");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("addbio") || args[0].equalsIgnoreCase("addinfo") || args[0].equalsIgnoreCase("adddesc") || args[0].equalsIgnoreCase("adddescription")) {
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
				} else if (args[0].equalsIgnoreCase("profession") || args[0].equalsIgnoreCase("skill")) {
					Persona target = pers;
					if(target.getMainSkill() == null || sender.hasPermission("archecore.persona.switchprofession")){
						Skill skill = ArcheCore.getControls().getSkill(args[1]);
						if(skill == null) {
							sender.sendMessage(ChatColor.RED + "Error: This profession could not be found!");
						} else {
							pers.setMainSkill(skill);
							sender.sendMessage(ChatColor.GOLD + "You have dedicated yourself to " + ChatColor.RESET + skill.getName());
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You have already selected a profession!");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("setbio") || args[0].equalsIgnoreCase("setinfo") || args[0].equalsIgnoreCase("setdesc") || args[0].equalsIgnoreCase("setdescription")) {
					int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
					String line = StringUtils.join(args, ' ', 1, parseTo);

					pers.setDescription(line);
					sender.sendMessage(ChatColor.AQUA + pers.getName() + "'s description now reads: " + line);
					return true;
				} else if (args[0].equalsIgnoreCase("setrace")) {
					if (!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.mod.persona") && !sender.hasPermission("archecore.persona.setrace")) {
						sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
					} else {
						int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
						String race = StringUtils.join(args, ' ', 1, parseTo);
						pers.setApparentRace(race);
						sender.sendMessage(ChatColor.AQUA + "Set visible race of " + pers.getName() + " to: " + ChatColor.RESET + race);
					}
					return true;
				} else if (args[0].equalsIgnoreCase("assigngender")) {
					if (!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.persona.gender")) {
						sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
					} else {
						String gender = args[1].toLowerCase();
						switch (gender) {
							case "male": case "female" : case "other" : {
								pers.setGender(gender);
								sender.sendMessage(ChatColor.AQUA + "Set gender of " + pers.getName() + " to: " + ChatColor.RESET + gender);
								return true;
							}
							default :
								sender.sendMessage(ChatColor.RED + "Please enter 'male', 'female', or 'other'.");
								return true;
						}  
					}
					return true;
				} else if (args[0].equalsIgnoreCase("realrace")) {
					if (!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.mod.persona") && !sender.hasPermission("archecore.persona.realrace")) {
						sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
					} else {
						sender.sendMessage(ChatColor.AQUA + "Underlying for " + pers.getName() + " is: " + ChatColor.GOLD + pers.getRace().getName());
                        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "(Visible: " + pers.getRaceString(sender.hasPermission("archecore.mod.persona")) + ")");
                    }
					return true;
				} else if (args[0].equalsIgnoreCase("wiperace")) {
					if (!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.mod.persona") && !sender.hasPermission("archecore.persona.setrace")) {
						sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
					} else {
                        sender.sendMessage(ChatColor.AQUA + "Visible race of " + pers.getName() + " has been reset to from '" + pers.getRaceString(sender.hasPermission("archecore.mod.persona")) + "' to: " + ChatColor.RESET + pers.getRace().getName());
                        pers.setApparentRace(null);
					}
					return true;
				} else if (args[0].equalsIgnoreCase("assignrace")) {
					if (!sender.hasPermission("archecore.admin")) {
						sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
					} else {
						int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? args.length - 2 : args.length;
						Race race = findRace(StringUtils.join(args, ' ', 1, parseTo));
						if (race == null) {
							sender.sendMessage(ChatColor.RED + "Please enter a valid race.");
						} else if (pers.getRace() == race) {
							sender.sendMessage(ChatColor.RED + "Race for " + pers.getName() + " is already " + race.getName());
						} else {
							doRaceChange(sender, pers, race);
						}
					}
					return true;
                } else if (args[0].equalsIgnoreCase("openinv")) {
                    if (!sender.hasPermission("archecore.admin")) {
                        if (sender.hasPermission("archecore.mod.persona"))
                            sender.sendMessage(ChatColor.RED + "Error: This command is currently suspended from moderators until we can sort out some bugs - Dev Team");
                        else
                            sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
                    } else {
                        if (sender instanceof Player) {
                            Player pl = (Player) sender;
                            pl.closeInventory();
                            Inventory inv = pers.getInventory();
                            if (inv == null) sender.sendMessage(ChatColor.RED + "This persona is currently active! Please use /openinv <player>");
                            else {
                                sender.sendMessage(ChatColor.AQUA + "Opening inventory contents for " + pers.getName() + ".");
                                pl.openInventory(pers.getInventory());
                            }

                        } else {
                            sender.sendMessage(ChatColor.RED + "This command can only be run from in game!");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("openender")) {
                    if (!sender.hasPermission("archecore.admin")) {
                        if (sender.hasPermission("archecore.mod.persona"))
                            sender.sendMessage(ChatColor.RED + "Error: This command is currently suspended from moderators until we can sort out some bugs - Dev Team");
                        else
                            sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
                    } else {
                        if (sender instanceof Player) {
                            Player pl = (Player) sender;
                            pl.closeInventory();
                            Inventory inv = pers.getEnderChest();
                            if (inv == null) {
                                sender.sendMessage(ChatColor.RED + "Something went wrong opening the enderchest.");
                            } else {
                                sender.sendMessage(ChatColor.AQUA + "Opening enderchest contents for " + pers.getName() + ".");
                                pl.openInventory(inv);
                            }

                        } else {
                            sender.sendMessage(ChatColor.RED + "This command can only be run from in game!");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("construct") || args[0].equalsIgnoreCase("golem")) {
                    if (!sender.hasPermission("archecore.command.construct") && !sender.hasPermission("archecore.admin")) {
                        sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
                        return true;
                    } else
                        return doRaceChange(sender, pers, Race.CONSTRUCT);
                } else if (args[0].equalsIgnoreCase("spectre") || args[0].equalsIgnoreCase("spectral") || args[0].equalsIgnoreCase("specter")) {
                    if (!sender.hasPermission("archecore.command.spectre") && !sender.hasPermission("archecore.admin")) {
                        sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
                        return true;
                    } else
                        return doRaceChange(sender, pers, Race.SPECTRE);
                } else if (args[0].equalsIgnoreCase("necrolyte") || args[0].equalsIgnoreCase("necro")) {
                    if (!sender.hasPermission("archecore.command.necrolyte") && !sender.hasPermission("archecore.admin")) {
                        sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
                        return true;
                    } else
                        return doRaceChange(sender, pers, Race.NECROLYTE);
                } else if (args[0].equalsIgnoreCase("aengulbound")
                        || args[0].equalsIgnoreCase("keeper")
                        || args[0].equalsIgnoreCase("ascended")) {
                    if (!sender.hasPermission("archecore.command.ascended") && !sender.hasPermission("archecore.admin")) {
                        sender.sendMessage(ChatColor.RED + "Error: Permission denied");
                        return true;
                    } else
                        return doRaceChange(sender, pers, Race.ASCENDED);
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

}