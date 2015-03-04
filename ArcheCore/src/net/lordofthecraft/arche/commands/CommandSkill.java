package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.help.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.skill.*;
import org.apache.commons.lang.*;
import net.lordofthecraft.arche.persona.*;
import org.bukkit.*;
import net.lordofthecraft.arche.*;
import org.bukkit.inventory.*;
import net.lordofthecraft.arche.interfaces.*;
import java.util.*;
import net.lordofthecraft.arche.enums.*;

public class CommandSkill implements CommandExecutor
{
    private final HelpDesk helpdesk;
    private final boolean showXp;
    
    public CommandSkill(final HelpDesk helpdesk, final boolean showXp) {
        super();
        this.helpdesk = helpdesk;
        this.showXp = showXp;
        final String i = ChatColor.DARK_GREEN + "" + ChatColor.ITALIC;
        final String a = ChatColor.AQUA + "";
        final String output = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Using the command: " + i + "/skill (or /sk)\n" + i + "$/sk list$: " + a + "Show your Persona's skills and aptitude\n" + i + "$/sk [skill]$: " + a + "Get info on a skill, if available.\n" + i + "$/sk [skill] teach [who]$: " + a + "Teach [who] this skill.\n" + i + "$/sk [skill] display$: " + a + "Show this skill on Persona card.\n" + i + "$/sk [skill] select$ {main/second/bonus}: " + a + " Select a profession for your Persona, allowing for further levelling in that skill.\n";
        helpdesk.addInfoTopic("Skill Command", output);
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (sender instanceof Player) {
                this.helpdesk.outputHelp("skill command", (Player)sender);
            }
            else {
                sender.sendMessage(this.helpdesk.getHelpText("skill command"));
            }
            if (sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")) {
                sender.sendMessage(ChatColor.DARK_AQUA + "[M] /sk [skill] tome : Gives experience tome");
                sender.sendMessage(ChatColor.DARK_AQUA + "[M] /sk [skill] reveal [who]: Reveal a skill to [who]");
                sender.sendMessage(ChatColor.DARK_AQUA + "[M] /sk [skill] give [who] [xp]: Gives XP");
                sender.sendMessage(ChatColor.BLUE + "[M] See someone else's skills with " + ChatColor.ITALIC + "/sk list [who]");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            Persona who = null;
            if ((sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")) && args.length >= 2) {
                who = CommandUtil.personaFromArg(args[1]);
            }
            else if (sender instanceof Player) {
                who = ArchePersonaHandler.getInstance().getPersona((Player)sender);
            }
            if (who == null) {
                sender.sendMessage(ChatColor.RED + "Error: No Persona found.");
            }
            else {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + who.getName() + ChatColor.AQUA + " has the following proficiencies: ");
                sender.sendMessage(ChatColor.GRAY + "(Color Index: " + ChatColor.GREEN + "Selected Profession" + ChatColor.GRAY + " | " + ChatColor.BLUE + "Racial Skill" + ChatColor.GRAY + " )");
                for (final Skill s : ArcheSkillFactory.getSkills().values()) {
                    if (s.isVisible(who)) {
                        final SkillTier tier = s.getSkillTier(who);
                        ChatColor color = ChatColor.YELLOW;
                        if (s.isProfessionFor(who.getRace())) {
                            color = ChatColor.BLUE;
                        }
                        else {
                            for (final ProfessionSlot slot : ProfessionSlot.values()) {
                                if (who.getProfession(slot) == s) {
                                    color = ChatColor.GREEN;
                                    break;
                                }
                            }
                        }
                        final String txt = color.toString() + tier.getTitle() + " " + WordUtils.capitalize(s.getName());
                        if (this.showXp || sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")) {
                            final SkillTier max = s.getCapTier(who);
                            String xp;
                            if (s.achievedTier(who, max)) {
                                if (max == SkillTier.AENGULIC) {
                                    xp = "";
                                }
                                else {
                                    xp = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (Maxed)";
                                }
                            }
                            else {
                                xp = ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + (int)s.getXp(who) + "/" + tier.getNext().getXp() + ")";
                            }
                            sender.sendMessage(txt + xp);
                        }
                        else {
                            sender.sendMessage(txt);
                        }
                    }
                }
            }
            return true;
        }
        if (args.length == 1) {
            if (sender instanceof Player) {
                this.helpdesk.outputSkillHelp(args[0], (Player)sender);
            }
            else {
                final String help = this.helpdesk.getSkillHelpText(args[0]);
                if (help == null) {
                    sender.sendMessage(ChatColor.RED + "No info found for profession: " + ChatColor.GRAY + args[0]);
                }
                else {
                    sender.sendMessage(help);
                }
            }
            return true;
        }
        final Skill skill = ArcheSkillFactory.getSkill(args[0]);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Error: Invalid skill " + ChatColor.ITALIC + args[0]);
            return true;
        }
        if (args[1].equalsIgnoreCase("display")) {
            final Persona pers = this.findSenderPersona(sender);
            if (pers != null) {
                if (pers.getMainSkill() == skill) {
                    sender.sendMessage(ChatColor.YELLOW + "Cleared your displayed skill...");
                    pers.setMainSkill(null);
                }
                else {
                    sender.sendMessage(ChatColor.GOLD + "Selected skill to display: " + ChatColor.WHITE + skill.getName());
                    sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Use command again to clear.");
                    pers.setMainSkill(skill);
                }
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("select") && args.length > 2) {
            final Persona pers = this.findSenderPersona(sender);
            if (pers != null) {
                if (skill.isInert() || !skill.isVisible(pers)) {
                    sender.sendMessage(ChatColor.RED + "Error: Invalid skill " + ChatColor.ITALIC + args[0]);
                    return true;
                }
                final Race r = pers.getRace();
                ProfessionSlot slot2 = null;
                if (skill.isProfessionFor(r)) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "You do not need to select this profession.");
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "You inherit it for free from your Persona's race.");
                }
                else if (args[2].equalsIgnoreCase("primary") || args[2].equalsIgnoreCase("main")) {
                    if (pers.getProfession(ProfessionSlot.ADDITIONAL) == skill || pers.getProfession(ProfessionSlot.SECONDARY) == skill) {
                        sender.sendMessage(ChatColor.RED + "You already have this profession selected!");
                    }
                    else {
                        slot2 = ProfessionSlot.PRIMARY;
                    }
                }
                else if (args[2].equalsIgnoreCase("secondary") || args[2].equalsIgnoreCase("second")) {
                    if (pers.getProfession(ProfessionSlot.PRIMARY) == null) {
                        sender.sendMessage(ChatColor.RED + "You should select a primary profession first!");
                    }
                    else if (pers.getProfession(ProfessionSlot.PRIMARY).isIntensiveProfession()) {
                        sender.sendMessage(ChatColor.RED + "Your primary profession is too intensive for you to also take up a secondary profession.");
                    }
                    else if (pers.getProfession(ProfessionSlot.PRIMARY) == skill || pers.getProfession(ProfessionSlot.ADDITIONAL) == skill) {
                        sender.sendMessage(ChatColor.RED + "You already have this profession selected!");
                    }
                    else if (skill.isIntensiveProfession()) {
                        sender.sendMessage(ChatColor.RED + "You may only select this skill as your Primary profession");
                    }
                    else {
                        slot2 = ProfessionSlot.SECONDARY;
                    }
                }
                else {
                    if (!args[2].equalsIgnoreCase("additional") && !args[2].equalsIgnoreCase("bonus")) {
                        return false;
                    }
                    if (pers.getProfession(ProfessionSlot.PRIMARY) == skill || pers.getProfession(ProfessionSlot.SECONDARY) == skill) {
                        sender.sendMessage(ChatColor.RED + "You already have this profession selected!");
                    }
                    else if (skill.isIntensiveProfession()) {
                        sender.sendMessage(ChatColor.RED + "You may only select this skill as your Primary profession");
                    }
                    else {
                        final long hours = 250 - pers.getTimePlayed() / 60;
                        if (hours <= 0L) {
                            slot2 = ProfessionSlot.ADDITIONAL;
                        }
                        else {
                            final String amount = (hours > 150L) ? "much, much" : ((hours > 100L) ? "much" : ((hours > 50L) ? "" : ((hours > 10L) ? "a little" : "a tiny bit")));
                            sender.sendMessage(ChatColor.RED + "You must attune your Persona " + amount + " further.");
                        }
                    }
                }
                if (slot2 != null) {
                    if (pers.getProfession(slot2) == skill) {
                        if (slot2 == ProfessionSlot.PRIMARY && pers.getProfession(ProfessionSlot.SECONDARY) != null) {
                            sender.sendMessage(ChatColor.RED + "Clear your secondary profession before resetting your primary.");
                        }
                        else {
                            pers.setProfession(slot2, null);
                            sender.sendMessage(ChatColor.YELLOW + "You have reset your " + slot2.toString().toLowerCase() + " profession");
                        }
                    }
                    else {
                        pers.setProfession(slot2, skill);
                        if (skill.isIntensiveProfession() && pers.getProfession(ProfessionSlot.SECONDARY) != null) {
                            pers.setProfession(ProfessionSlot.SECONDARY, null);
                        }
                        sender.sendMessage(ChatColor.GOLD + "Set your " + slot2.toString().toLowerCase() + " profession as " + ChatColor.WHITE + skill.getName());
                        sender.sendMessage(ChatColor.YELLOW + "Your ability to train this profession has improved.");
                        sender.sendMessage(ChatColor.YELLOW + "You can undo this selection by rerunning the command:");
                        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "/sk " + skill.getName() + " select " + slot2.toString().toLowerCase());
                    }
                    ((ArchePersona)pers).handleProfessionSelection();
                }
            }
            else {
                sender.sendMessage(ChatColor.RED + "Error: No Persona found.");
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("teach")) {
            final Persona pers = this.findTargetPersona(sender, args);
            if (pers != null) {
                final Persona send = this.findSenderPersona(sender);
                if (send != null && skill.getXp(send) >= skill.getXp(pers) && sender.hasPermission("archecore.teachskill." + skill.getName() + ((send == pers) ? ".self" : ""))) {
                    if (skill.getVisibility() != 4) {
                        skill.reveal(pers);
                    }
                    if (skill.canGainXp(pers)) {
                        final String msg = ChatColor.GOLD + ((send == pers) ? "Your learnings have paid off..." : ("Your learnings with " + send.getName() + " have paid off..."));
                        Bukkit.getPlayer(pers.getPlayerUUID()).sendMessage(msg);
                        skill.addRawXp(pers, SkillTome.getXpBoost(skill.getXp(pers)));
                    }
                    else {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + pers.getName() + " cannot be taught in this skill.");
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + "You are not capable of teaching this Pupil...");
                }
            }
            return true;
        }
        if (sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")) {
            if (args[1].equalsIgnoreCase("tome") && sender instanceof Player) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Attempting to give Skill tome for: " + ChatColor.GOLD + args[0]);
                ((Player)sender).getInventory().addItem(new ItemStack[] { SkillTome.giveTome(skill) });
                return true;
            }
            if (args[1].equalsIgnoreCase("reveal")) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Attempting to reveal skill: " + ChatColor.GOLD + args[0]);
                final Persona pers = this.findTargetPersona(sender, args);
                if (pers != null) {
                    skill.reveal(pers);
                }
                return true;
            }
            if (args[1].equalsIgnoreCase("give") && args.length >= 4) {
                try {
                    final Persona pers = this.findTargetPersona(sender, args);
                    final double xp2 = Double.parseDouble(args[3]);
                    if (pers != null) {
                        sender.sendMessage(ChatColor.DARK_GREEN + "Giving " + ChatColor.GOLD + xp2 + " " + skill.getName() + ChatColor.DARK_GREEN + " xp to " + ChatColor.GOLD + pers.getName());
                        skill.addRawXp(pers, xp2, false);
                    }
                    return true;
                }
                catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return false;
    }
    
    private Persona findSenderPersona(final CommandSender sender) {
        ArchePersona result = null;
        if (sender instanceof Player) {
            final Player t = (Player)sender;
            result = ArchePersonaHandler.getInstance().getPersona(t);
        }
        if (result == null) {
            sender.sendMessage(ChatColor.RED + "Error: No persona found");
        }
        return result;
    }
    
    private Persona findTargetPersona(final CommandSender sender, final String[] args) {
        Persona result;
        if (args.length >= 3) {
            result = CommandUtil.personaFromArg(args[2]);
        }
        else if (sender instanceof Player) {
            result = ArchePersonaHandler.getInstance().getPersona((Player)sender);
        }
        else {
            result = null;
        }
        if (result == null) {
            sender.sendMessage(ChatColor.RED + "Error: No target persona found");
        }
        return result;
    }
}
