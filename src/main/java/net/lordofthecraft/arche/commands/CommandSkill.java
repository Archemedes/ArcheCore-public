package net.lordofthecraft.arche.commands;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SkillTome;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.enums.ProfessionSlot;
import net.lordofthecraft.arche.enums.SkillTier;
import net.lordofthecraft.arche.help.ArcheMessage;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.Race;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skill.BonusExpModifier;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandSkill implements CommandExecutor {
	private static final int XP_TRESHOLD_BEFORE_WARNING = 5000; 
	private final HelpDesk helpdesk;
	private final boolean showXp;

	private final Set<UUID> skillResets = Sets.newHashSet();

	public CommandSkill(HelpDesk helpdesk, boolean showXp){
		this.helpdesk = helpdesk;
		this.showXp = showXp;

		String i = ChatColor.DARK_GREEN + "" + ChatColor.ITALIC;
		String a = ChatColor.AQUA+ "";
		String output = ChatColor.DARK_AQUA +""+ ChatColor.BOLD + "Using the command: " + i + "/skill (or /sk)\n"
				+ i + "$/sk list$: " + a + "Show your Persona's skills and aptitude\n"
				+ i + "$/sk [skill]$: " + a + "Get info on a skill, if available.\n"
				+ i + "$/sk [skill] teach [who]$: " + a + "Teach [who] this skill.\n"
				+ i + "$/sk [skill] display$: " + a + "Show this skill on Persona card.\n"
				+ i + "$/sk [skill] top$: " + a + "Show the current leaderboard for this skill.\n"
				+ i + "$/sk [skill] select$ {main/second/bonus}: " + a + " elect a profession for your Persona, allowing for further levelling in that skill.\n"
				+ i + "$/sk [skill] assign$ [xp]: " + a + "Assign instant XP to this skill (if free xp is available).\n";

		helpdesk.addInfoTopic("Skill Command", output);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
			if (sender instanceof Player) helpdesk.outputHelp("skill command", (Player) sender);
			else sender.sendMessage(helpdesk.getHelpText("skill command"));

			String d = ChatColor.DARK_AQUA + "";

			if (sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")) {
				String m = ChatColor.DARK_AQUA + "[M] " + ChatColor.WHITE;
				sender.sendMessage(m + "/sk [skill] tome: " + d + "Gives experience tome");
				sender.sendMessage(m + "/sk [skill] reveal [who]: " + d + "Reveal a skill to [who]");
				sender.sendMessage(m + "/sk [skill] give [who] [xp]: " + d + "Gives XP");
				sender.sendMessage(m + "/sk [skill] drain [who] [return]: " + d + "Drains skill xp and returns a factor for their personal redistribution");
				sender.sendMessage(m + "/sk drainall [return] [who]: " + d + "Drain all xp and returns a factor for their personal redistribution");
				sender.sendMessage(m + "See someone else's skills with " + ChatColor.ITALIC + "/sk list [who]");
			}

			if (sender.hasPermission("archecore.admin") && sender instanceof Player) {
				ArcheMessage m = new ArcheMessage("");
				m.addLine("[" + ChatColor.DARK_RED + "" + ChatColor.BOLD + "A" + ChatColor.DARK_AQUA + "] ");
				m.addLine("[Click to view Help for skill bonuses (/sk bonushelp)]").setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click me!").setClickEvent(ChatBoxAction.RUN_COMMAND, "/skill bonushelp");
				m.sendTo((Player) sender);
			}

			/*if( (sender instanceof Player) && ArchePersonaHandler.getInstance().hasPersona((Player) sender)
					&& ArcheCore.getControls().getSkill("internal_drainxp").getXp((Player) sender) > 0 ){
				sender.sendMessage(ChatColor.BLUE + "/sk [skill] assign [xp]: Assign instant XP to this skill");
			}*/


			return true;
		} else if (args[0].equalsIgnoreCase("bonushelp") &&  sender.hasPermission("archecore.admin")) {
			String a = ChatColor.DARK_AQUA + "[" + ChatColor.DARK_RED + "" + ChatColor.BOLD + "A" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE;
			sender.sendMessage(a + "/sk [skill] bonus [who/when] [modifier] [time] [amount] -[p/a/g]: " + ChatColor.DARK_AQUA + "Gives [who] experience multiplier x[modifer] for [amount] max xp, [time] max mins.\n"
					+ ChatColor.GRAY + ChatColor.ITALIC + "Enter -1 for no max. -a makes it account based. -g is global. Using [when] will start the bonus in X minutes. "
					+ "Omit [skill] from the bonus command to apply to all skills.");
			return true;
		} else if (args[0].equalsIgnoreCase("bonus")) {
			if (sender.hasPermission("archecore.admin"))
				return processBonusCommand(sender, null, args);
		} else if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("bonuslist")) {

			Persona who = null;

			if((sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill"))&& args.length >= 2){
				who = CommandUtil.personaFromArg(args[1]);
			} else if (sender instanceof Player){
				who = ArchePersonaHandler.getInstance().getPersona((Player) sender);
			}

			if ("bonuslist".equalsIgnoreCase(args[0])) {
				if (sender.hasPermission("archecore.admin") && args.length == 1) {
					sender.sendMessage(ChatColor.LIGHT_PURPLE + "Global exp modifiers:");
					for (BonusExpModifier m : ArcheCore.getControls().getBonusExpModifierHandler().getGlobalModifiers()) 
						sender.sendMessage(m.readableString());
				}
				else if (sender instanceof Player)
					if(who == null){
						sender.sendMessage(ChatColor.RED + "Error: No Persona found.");
						return true;
					} else {
						sender.sendMessage(ChatColor.LIGHT_PURPLE + "Bonus exp modifiers for " + who.getPlayerName() + ":");
						for (BonusExpModifier m : ArcheCore.getControls().getBonusExpModifierHandler().getModifiers(who.getPlayerUUID())) 
							sender.sendMessage(m.readableString());
					}
				return true;
			}

			else if(who == null){
				sender.sendMessage(ChatColor.RED + "Error: No Persona found.");
			} else {

				sender.sendMessage(ChatColor.LIGHT_PURPLE + who.getName() + ChatColor.AQUA + " has the following proficiencies: ");

				double bonusXp = ArcheSkillFactory.getSkill("internal_drainxp").getXp(who);
				if(bonusXp >= 1){
					sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Free XP (assign with /sk [skill] assign [xp]) ");
					sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.ITALIC + "Free XP remaining: " + ChatColor.RESET + (int) bonusXp);
				}

				sender.sendMessage(ChatColor.GRAY + "(Color Index: " + ChatColor.DARK_GREEN + "Main Profession" + ChatColor.GRAY + ", " + ChatColor.GREEN + "Second Profession"  + ChatColor.GRAY + ", "
						+ ChatColor.BLUE + "Racial Profession" + ChatColor.GRAY + ", " + ChatColor.AQUA + "Bonus Profession" + ChatColor.GRAY + " )");
				for(Skill s : ((ArchePersona) who).getOrderedProfessions()){
					//SkillAttachment att = who.getSkill(i++);
					if(s.isVisible(who)){
						SkillTier tier = s.getSkillTier(who);

						ChatColor color = who.colorForSkill(s);
						/*if(s.isProfessionFor(who.getRace()))
							color = ChatColor.BLUE;
						else {
							if(who.getProfession(ProfessionSlot.PRIMARY) == s){
								color = ChatColor.DARK_GREEN;
							} else if(who.getProfession(ProfessionSlot.SECONDARY) == s){
								color = ChatColor.GREEN;
							} else if(who.getProfession(ProfessionSlot.ADDITIONAL) == s){
								color = ChatColor.AQUA;
							}
						}*/

						String txt = color.toString() + tier.getTitle() + " " + WordUtils.capitalize(s.getName());

						if(showXp || sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")){
							SkillTier max = s.getCapTier(who);
							String xp;
							if(s.achievedTier(who, max)){
								if(max == SkillTier.SUPER) xp = ""; 
								else xp = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (Maxed)";
							} else {
								xp = ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + ((int) s.getXp(who)) + "/" + tier.getNext().getXp() + ")";
							}
							if (!s.achievedTier(who, max)
									&& sender instanceof Player){
								SkillTier next = s.getSkillTier(who).getNext();
								new ArcheMessage(txt + xp)
								.setHoverEvent(ChatBoxAction.SHOW_TEXT," Next tier: " + (next == SkillTier.SUPER ? "???" : (next.getTitle() + " " + WordUtils.capitalize(s.getName()))))
								.sendTo((Player) sender);
							} else
								sender.sendMessage(txt + xp);
						}else sender.sendMessage(txt);
					}
				}
				if (sender instanceof Player) sender.sendMessage(ChatColor.GRAY+"(Hover over a non-maxed skill to see the next level)");
			}
			return true;
		} else if(args.length == 1){

			if(sender instanceof Player)
				helpdesk.outputSkillHelp(args[0], (Player) sender);
			else{
				String help = helpdesk.getSkillHelpText(args[0]);

				if(help == null) sender.sendMessage(ChatColor.RED + "No info found for profession: " + ChatColor.GRAY + args[0]);
				else sender.sendMessage(help);
			}

			return true;
		} else if(args[0].equalsIgnoreCase("drainall")){
			if(sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")){
				Persona pers = findTargetPersona(sender, args, 2);
				if(pers == null){
					sender.sendMessage(ChatColor.RED + "Error: No Persona found.");
					return true;
				}

				double xp = 0;
				try{
					double factor = Double.parseDouble(args[1]);
					for(Skill s : ArcheSkillFactory.getSkills().values()){
						if(s.isVisible(pers)){
							double val = s.reset(pers);
							xp += factor * val;
							sender.sendMessage(ChatColor.DARK_GREEN + "Draining profession " + ChatColor.GOLD + s.getName() + ChatColor.DARK_GREEN + " for " + ChatColor.GOLD + (int) val);
						}
					}

					sender.sendMessage(ChatColor.DARK_AQUA + "XP returned in total: " + (int) xp);
					ArcheSkillFactory.getSkill("internal_drainxp").addRawXp(pers, xp, false);
				}catch(NumberFormatException e){return false;}
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission!");
			}
			return true;
		}else{
			Skill skill = ArcheSkillFactory.getSkill(args[0]);

			if(skill == null){
				sender.sendMessage(ChatColor.RED + "Error: Invalid skill " + ChatColor.ITALIC + args[0]);
				return true;
			}

			Persona pers;
			if(args[1].equalsIgnoreCase("display")){
				pers = findSenderPersona(sender);
				if(pers != null){
					if(pers.getMainSkill() == skill){
						sender.sendMessage(ChatColor.YELLOW + "Cleared your displayed skill...");
						pers.setMainSkill(null);
					}else{
						sender.sendMessage(ChatColor.GOLD + "Selected skill to display: " + ChatColor.WHITE + skill.getName());
						sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Use command again to clear.");
						pers.setMainSkill(skill);
					}
				}
				return true;
			} else if (args[1].equalsIgnoreCase("top")) {
				List<Persona> top = ArcheCore.getControls().getPersonaHandler().getTopHandler().getTopList(skill);
				sender.sendMessage(ChatColor.DARK_AQUA+""+ChatColor.BOLD+".:: Top "+WordUtils.capitalize(skill.getName())+" ::.");
				if (!top.isEmpty()) {
					Persona holder;
					SkillTier t;
					int count = 0;
					String p;
					for (Persona ent : top) {
						holder = ent;
						t = skill.getSkillTier(ent);
						p = count==0?ChatColor.GOLD+""+ChatColor.BOLD : ChatColor.BLUE+"";
						sender.sendMessage(
								p+(count+1)+". "/*number in list*/
								+ChatColor.AQUA+holder.getPlayerName()+"@"+holder.getId()+" " /*(persona id/persona rp name),*/
								+t.getTitle()+";" /*skilltier skill (aengulic woodworker)*/
								+(t == SkillTier.SUPER ? "" : ChatColor.GOLD+" " +ChatColor.RESET+""+ChatColor.GOLD+"("+Math.round(skill.getXp(holder))+")")); /*Total experience*/
						++count;
					}
				} else {
					sender.sendMessage(ChatColor.RED+"None! Everyone sucks. This is very likely an error, please post a bug report.");
				}
				return true;
			}else if(args[1].equalsIgnoreCase("select") && args.length > 2){
				pers = findSenderPersona(sender);
				if(pers != null){
					if(skill.isInert() || !skill.isVisible(pers)){
						sender.sendMessage(ChatColor.RED + "Error: Invalid skill " + ChatColor.ITALIC + args[0]);
						return true;
					}
					Race r = pers.getRace();
					ProfessionSlot slot = null;
					if(skill.isProfessionFor(r)){
						sender.sendMessage(ChatColor.LIGHT_PURPLE + "You do not need to select this profession.");
						sender.sendMessage(ChatColor.LIGHT_PURPLE + "You inherit it for free from your Persona's race.");
					}else if(args[2].equalsIgnoreCase("primary") || args[2].equalsIgnoreCase("main") || args[2].equalsIgnoreCase("first")){
						Optional<Skill> oskill = pers.getProfession(ProfessionSlot.ADDITIONAL);

						if (pers.isSkillInSlot(skill, ProfessionSlot.ADDITIONAL) || pers.isSkillInSlot(skill, ProfessionSlot.SECONDARY)){
							sender.sendMessage(ChatColor.RED + "You already have this profession selected!");
						}else{	
							slot = ProfessionSlot.PRIMARY;
						}
					} else if(args[2].equalsIgnoreCase("secondary") || args[2].equalsIgnoreCase("second")){
						Optional<Skill> prim = pers.getProfession(ProfessionSlot.PRIMARY);
						if(!prim.isPresent()){
							if (pers.isSkillInSlot(skill, ProfessionSlot.ADDITIONAL) || pers.isSkillInSlot(skill, ProfessionSlot.SECONDARY)){
								sender.sendMessage(ChatColor.RED + "You already have this profession selected!");
							}else{	
								slot = ProfessionSlot.PRIMARY;
							}
						} else {
							Skill sk = prim.get();
							if (sk.isIntensiveProfession()){
								sender.sendMessage(ChatColor.RED + "Your main profession is too intensive for you to also take up a second profession.");
							} else if (pers.isSkillInSlot(skill, ProfessionSlot.ADDITIONAL) || pers.isSkillInSlot(skill, ProfessionSlot.SECONDARY)){
								sender.sendMessage(ChatColor.RED + "You already have this profession selected!");
							} else if (skill.isIntensiveProfession()){
								sender.sendMessage(ChatColor.RED + "You may only select this skill as your main profession");
							} else {
								slot = ProfessionSlot.SECONDARY;
							}
						}
					} else if(args[2].equalsIgnoreCase("additional") || args[2].equalsIgnoreCase("bonus")){
						if (pers.isSkillInSlot(skill, ProfessionSlot.PRIMARY) || pers.isSkillInSlot(skill, ProfessionSlot.SECONDARY)){
							sender.sendMessage(ChatColor.RED + "You already have this profession selected!");
						} else if (skill.isIntensiveProfession()){
							sender.sendMessage(ChatColor.RED + "You may only select this skill as your main profession");
						}else{	
							long hours = 250 - pers.getTimePlayed()/60;
							if(hours <= 0) slot = ProfessionSlot.ADDITIONAL;
							else{
								String amount = hours > 150? "much, much " :
									hours > 100? "much " : hours > 50? "" : 
										hours > 10? "a little " : "a tiny bit ";
										sender.sendMessage(ChatColor.RED + "You must attune your Persona " + amount + "further.");
							}
						}
					} else return false;

					if(slot != null){
						ArchePersona apers = (ArchePersona) pers;
						Skill[] oldProfs = apers.getArrayOfProfessions();
						Skill[] newProfs = oldProfs.clone();

						String messageToSend = null;

						if(pers.isSkillInSlot(skill, slot)){
							if(slot == ProfessionSlot.PRIMARY && pers.getProfession(ProfessionSlot.SECONDARY).isPresent()) {
								messageToSend = ChatColor.YELLOW + "Cleared your main profession. Your second profession has been transferred to the main slot.";
								//pers.setProfession(slot, skill);
								newProfs[slot.getSlot()] = pers.getProfession(ProfessionSlot.SECONDARY).get();
								newProfs[ProfessionSlot.SECONDARY.getSlot()] = null;
							}else{
								//pers.deselectSlot(slot);
								newProfs[slot.getSlot()] = null;
								messageToSend = ChatColor.YELLOW + "You have reset your " + slot.toSimpleString().toLowerCase() + " profession";	
							}
						} else {
							newProfs[slot.getSlot()] = skill;
							//pers.setProfession(slot, skill);
							if(skill.isIntensiveProfession() && pers.getProfession(ProfessionSlot.SECONDARY).isPresent())
								newProfs[1] = null;

							messageToSend = ChatColor.GOLD + "Your " + slot.toSimpleString().toLowerCase() + " profession is now " + ChatColor.WHITE + skill.getName() + "\n"
									+ ChatColor.YELLOW + "Your ability to train this profession has improved." + "\n"
									+ ChatColor.YELLOW + "You can undo this selection by rerunning the command:" + "\n"
									+ ChatColor.GRAY + "" + ChatColor.ITALIC + "/sk " + skill.getName() + " select " + slot.toSimpleString().toLowerCase();
						}

						if(!this.skillResets.contains(apers.getPlayerUUID())){
							//apers.professions = newProfs;
							double xplost = apers.getXpLost();
							if(xplost > XP_TRESHOLD_BEFORE_WARNING){ // buggy still?
								skillResets.add(apers.getPlayerUUID());
								sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "WARNING: " + ChatColor.RED + "Rearranging professions will effect new level caps that will drain this Persona's total experience significantly.");
								sender.sendMessage(ChatColor.DARK_RED + "You will lose " + ChatColor.GOLD + ChatColor.BOLD + xplost + ChatColor.DARK_RED + " experience that can not be recovered.");
								sender.sendMessage(ChatColor.LIGHT_PURPLE + "If you are okay with this, repeat the command and accept the experience penalty.");

								//apers.professions = oldProfs;
								return true;
							} /*else {
								apers.professions = oldProfs;
							}*/
						}

						sender.sendMessage(messageToSend);
						for(ProfessionSlot s: ProfessionSlot.values()){
							if (s != ProfessionSlot.UNSELECTED) {
								apers.setProfession(s, newProfs[s.getSlot()]);
							}
						}
						apers.handleProfessionSelection();
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Error: No Persona found.");
				}
				return true;
			}else if (args[1].equalsIgnoreCase("teach")){
				pers = findTargetPersona(sender, args, 2);
				if(pers != null){
					Persona send = findSenderPersona(sender);
					if(send != null && skill.getXp(send) >= skill.getXp(pers) && sender.hasPermission("archecore.teachskill." + skill.getName() + (send == pers? ".self" : ""))){

						//Via teaching, reveal any skill of Grantable visibility or less
						if(skill.getVisibility() != Skill.VISIBILITY_HIDDEN) 
							skill.reveal(pers);

						if(skill.canGainXp(pers)){ //The skill was made visible, but in some cases the Pupil still cannot gain XP.
							String msg = ChatColor.GOLD + (send == pers? "Your learnings have paid off..." : "Your learnings with " + send.getName() + " have paid off...");  
							Bukkit.getPlayer(pers.getPlayerUUID()).sendMessage(msg);
							skill.addRawXp(pers, SkillTome.getXpBoost(skill.getXp(pers)));
						} else {
							sender.sendMessage(ChatColor.LIGHT_PURPLE + pers.getName() + " cannot be taught in this skill.");	
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You are not capable of teaching this Pupil...");
					}
				}
				return true;
			} else if (args[1].equalsIgnoreCase("assign") && args.length > 1){
				try{
					Persona send = findSenderPersona(sender);
					Skill drainXp = ArcheSkillFactory.getSkill("internal_drainxp");
					double xp;
					if (args.length > 2) {
						xp = Double.parseDouble(args[2]);
					} else {
						xp = drainXp.getXp(send);
					}
					if(send != null){
						if (xp > 0){
							SkillTier cap = (skill.getCapTier(send).getTier() > 12) ? SkillTier.AENGULIC : skill.getCapTier(send);
							if (skill.getSkillTier(send).getTier() >= cap.getTier()) {
								sender.sendMessage(ChatColor.RED + "You cannot assign experience over Aengulic (1,000,000 experience)");
								return true;
							}
							xp = Math.min((double) cap.getXp() - skill.getXp(send), xp);
							if(xp > drainXp.getXp(send)){
								sender.sendMessage(ChatColor.RED + "Error: Insuffcicient Free XP available");
							} else {
								if (xp != 0){
									drainXp.addRawXp(send, -1*xp, false);
									sender.sendMessage(ChatColor.DARK_GREEN + "Giving " + ChatColor.GOLD + xp + " " + skill.getName() + ChatColor.DARK_GREEN + " xp.");
									skill.addRawXp(send, xp, false);
								} else {
									sender.sendMessage(ChatColor.RED+"Error: You cannot go over "+cap.getXp()+" experience.");
								}
							}
						} else {
							sender.sendMessage(ChatColor.RED+"Error: Exp must be over 0");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Error: No Persona found.");
					}
				}catch(NumberFormatException e){return false;}	
				return true;
			} else if(sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.skill")){
				if(args[1].equalsIgnoreCase("tome") && (sender instanceof Player)){
					sender.sendMessage(ChatColor.DARK_GREEN + "Attempting to give Skill tome for: " + ChatColor.GOLD + args[0]);
					((Player) sender).getInventory().addItem(SkillTome.giveTome(skill));
					return true;
				} else if(args[1].equalsIgnoreCase("reveal")){
					sender.sendMessage(ChatColor.DARK_GREEN + "Attempting to reveal skill: " + ChatColor.GOLD + args[0]);
					pers = findTargetPersona(sender, args, 2);

					if(pers != null)
						skill.reveal(pers);
					return true;
				} else if(args[1].equalsIgnoreCase("give") && args.length >= 4){

					try{
						pers = findTargetPersona(sender, args, 2);
						double xp = Double.parseDouble(args[3]); 
						if(pers != null){
							sender.sendMessage(ChatColor.DARK_GREEN + "Giving " + ChatColor.GOLD + xp + " " + skill.getName() + ChatColor.DARK_GREEN + " xp to " + ChatColor.GOLD + pers.getName());
							skill.addRawXp(pers, xp, false);
						}
						return true;
					}catch(NumberFormatException e){return false;}
				} else if(args[1].equalsIgnoreCase("drain") && args.length >= 4){
					try{
						pers = findTargetPersona(sender, args, 2);
						double factor = Double.parseDouble(args[3]); 
						if(pers != null){
							sender.sendMessage(ChatColor.DARK_GREEN + "Draining profession " + ChatColor.GOLD + skill.getName() + ChatColor.DARK_GREEN + " from " + ChatColor.GOLD + pers.getName());
							double xp = skill.reset(pers) * factor;
							ArcheSkillFactory.getSkill("internal_drainxp").addRawXp(pers, xp);
						}
						return true;
					}catch(NumberFormatException e){return false;}
				} else if (args[1].equalsIgnoreCase("bonus")) {
					String[] clipArgs = Arrays.copyOfRange(args, 1, args.length);
					return this.processBonusCommand(sender, skill, clipArgs);
				}
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean processBonusCommand(CommandSender sender, Skill sk, String[] args) {
		if (args.length < 6 || ("server".equalsIgnoreCase(args[1]) && !"-g".equalsIgnoreCase(args[5]))) {
			sender.sendMessage(ChatColor.RED + "Error. Server must use global.");
			return true;
		}

		BonusExpModifier m = null;

		double mod;
		int time;
		int xp;

		try {
			mod = Double.parseDouble(args[2]);
			time = Integer.parseInt(args[3]);
			xp = Integer.parseInt(args[4]);
		} catch(NumberFormatException e){return false;}

		long duration = -1;
		if (time > 0) duration = TimeUnit.MINUTES.toMillis(time);

		if (mod <= 0 || time < -1 || xp < -1) return false;

		OfflinePlayer player = null;
		int when = 0;
		try {
			when = Integer.parseInt(args[1]);
			if (!"-g".equalsIgnoreCase(args[5])) return false;
		} catch(NumberFormatException e){
			player = Bukkit.getOfflinePlayer(args[1]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Player not found.");
				return true;
			}
		}

		switch (args[5].toLowerCase()) {
		case "-g" : {
			if (player == null) {
				long starttime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(when);
				m = new BonusExpModifier(sk, starttime, duration, mod);
				break;
			} else {
				m = new BonusExpModifier(sk, duration, mod, player);
				break;
			}
		}
		case "-a" : {
			m = new BonusExpModifier(player, sk, duration, xp, mod);
			break;
		}
		case "-p" : {
			Persona persona = this.findTargetPersona(sender, args, 1);
			if (persona == null) return true;
			m = new BonusExpModifier(persona, sk, duration, xp, mod);
			break;
		}
		default : {
			sender.sendMessage(ChatColor.RED + "Error: Invalid type.");
			return true;
		}
		}

		ArcheCore.getControls().getBonusExpModifierHandler().addModifier(m);

		sender.sendMessage(m.readableString());
		return true;
	}

	private Persona findSenderPersona(CommandSender sender){
		ArchePersona result = null;
		if(sender instanceof Player){
			Player t = (Player) sender;
			result = ArchePersonaHandler.getInstance().getPersona(t);
		}
		if(result == null) sender.sendMessage(ChatColor.RED + "Error: No persona found.");
		return result;
	}

	private Persona findTargetPersona(CommandSender sender, String[] args, int pLocation){
		Persona result;
		if(args.length >= 3){
			result = CommandUtil.personaFromArg(args[pLocation]);
		} else {
			if(sender instanceof Player) result = ArchePersonaHandler.getInstance().getPersona((Player) sender);
			else result = null;
		}
		if(result == null) sender.sendMessage(ChatColor.RED + "Error: No target persona found.");
		return result;
	}
}
