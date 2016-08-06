package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.help.ArcheMessage;
import net.lordofthecraft.arche.interfaces.ChatMessage;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.listener.PersonaCreationAbandonedListener;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CreationDialog {

	private static final String DIVIDER = ChatColor.LIGHT_PURPLE + 
			"\n--------------------------------------------------\n" + ChatColor.YELLOW;
	private static final String NOTE = ChatColor.DARK_RED + "" + ChatColor.BOLD + 
			"NB:" + ChatColor.YELLOW;
	private static long lastAnnounce = -1;
	private final ConversationFactory factory;
	private final Plugin plugin;

	public CreationDialog(){
		plugin = ArcheCore.getPlugin();
		factory = new ConversationFactory(plugin)
		.thatExcludesNonPlayersWithMessage("NO! FU Tythus how are you even seeing this?")
		.withPrefix(new Prefix())
		.withModality(true);
	}

	@SuppressWarnings("unchecked")
	public static boolean mayConverse(Player p) {
		try {
			Field f = p.getClass().getDeclaredField("conversationTracker");
			f.setAccessible(true);
			Object conversationTracker = f.get(p);

			Field f2 = conversationTracker.getClass().getDeclaredField("conversationQueue");
			f2.setAccessible(true);
			LinkedList<Conversation> list = (LinkedList<Conversation>) f2.get(conversationTracker);
			return list.isEmpty();
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}

	}

	public void makeFirstPersona(Player p){

		Map<Object, Object> data = Maps.newHashMap();
		data.put("slot", 0);
		data.put("first", new Object());


		factory.withFirstPrompt(new WelcomePrompt())
		.withInitialSessionData(data)
		.addConversationAbandonedListener(new PersonaCreationAbandonedListener())
		.buildConversation(p)
		.begin();

		Collection<PotionEffect> e = Lists.newArrayList();
		e.add(new PotionEffect(PotionEffectType.SLOW, 40000, 10));
		e.add(new PotionEffect(PotionEffectType.WEAKNESS, 40000, 10));
		p.addPotionEffects(e);
	}

	public void addPersona(Player p, int slot){
		if(!mayConverse(p)){
			p.sendRawMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You are already in a dialog!");
			p.sendRawMessage(ChatColor.RED + "Type 'cancel' to abandon your current dialog.");
			return;
		}
		//@ teegah make this stuff into a method l8r
		final ArchePersona pers = ArcheCore.getPlugin().getPersonaHandler().getPersona(p);
		final long weekSinceCreation = pers.creationTimeMS + 604800000;
		final long hoursSince = TimeUnit.MILLISECONDS.toHours((System.currentTimeMillis() - pers.creationTimeMS));
		//Bukkit.broadcastMessage("created on" + pers.creationTimeMS);
		//Bukkit.broadcastMessage("current time" + System.currentTimeMillis());
		//Bukkit.broadcastMessage("expires on" + weekSinceCreation);
		if(weekSinceCreation > System.currentTimeMillis()){
			p.sendMessage(ChatColor.RED + "You must wait at least 1 week before deleting a persona!");
			p.sendMessage(ChatColor.YELLOW + "You created this persona " + ChatColor.WHITE + hoursSince + ChatColor.YELLOW +" hours ago!");
			return;
		}

		addAbandoners();
		Map<Object, Object> data = Maps.newHashMap();
		data.put("slot", slot);

		factory.withInitialSessionData(data)
		.withFirstPrompt(new RedoCharacterPrompt())
		.buildConversation(p)
		.begin();
	}

	public void removePersona(ArchePersona pers){
		Player p = Bukkit.getPlayer(pers.getPlayerUUID());
		if(p == null){
			plugin.getLogger().severe("We tried to engage Persona removal Dialog while owning player was offline!");
			return;
		}

		if(!mayConverse(p)){
			p.sendRawMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You are already in a dialog!");
			p.sendRawMessage(ChatColor.RED + "Type 'cancel' to abandon your current dialog.");
			return;
		}

		addAbandoners();

		/*cancel persona deletion if persona was created less than a week ago to prevent people from creating and
			deleting personas to just get the free skill xp
		*/
		//1 week in ms = 604800000
		final long weekSinceCreation = pers.getCreationTime() + 604800000;
		final long daysSince = TimeUnit.MILLISECONDS.toDays((System.currentTimeMillis() - pers.getCreationTime()));
		//Bukkit.broadcastMessage("created on" + pers.creationTimeMS);
		//Bukkit.broadcastMessage("current time" + System.currentTimeMillis());
		//Bukkit.broadcastMessage("expires on" + weekSinceCreation);
		if(weekSinceCreation > System.currentTimeMillis()){
			p.sendMessage(ChatColor.RED + "You must wait at least 1 week before deleting a persona!");
			p.sendMessage(ChatColor.YELLOW + "You created this persona " + ChatColor.WHITE + daysSince + ChatColor.YELLOW +" hours ago!");
			return;
		}


		Map<Object, Object> data = Maps.newHashMap();
		data.put("persona", pers);

		factory.withInitialSessionData(data)
		.withFirstPrompt(new ConfirmRemovalPrompt())
		.buildConversation(p)
		.begin();
	}

	private void addAbandoners(){
		factory
		.withEscapeSequence("quit")
		.withEscapeSequence("exit")
		.withEscapeSequence("cancel")
		.withEscapeSequence("stop")
		.withEscapeSequence("/stop")
		.withEscapeSequence("/quit")
		.withEscapeSequence("/exit")
		.withEscapeSequence("/cancel")
		.withEscapeSequence("/me")
		.withEscapeSequence("/beaconme")
		.withEscapeSequence("/bme");
	}

	private class ConfirmRemovalPrompt extends BooleanPrompt{

		@Override
		public String getPromptText(ConversationContext context) {
			String name = ((ArchePersona) context.getSessionData("persona")).getName();
			return "Are you sure you wish to permakill poor " +
			ChatColor.GREEN + name + ChatColor.YELLOW + "?";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if(input) return new RemoveExecutedPrompt();
			else return Prompt.END_OF_CONVERSATION;
		}
	}

	private class RemoveExecutedPrompt extends MessagePrompt{
		@Override
		public String getPromptText(ConversationContext context) {
			return "This Persona will be permakilled. So long :(";
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext context) {
			ArchePersona pers = (ArchePersona) context.getSessionData("persona");
			pers.remove();
			return Prompt.END_OF_CONVERSATION;
		}
	}

	private class RedoCharacterPrompt extends MessagePrompt{

		@Override
		public String getPromptText(ConversationContext arg0) {
			return "You can register a new Persona in this slot. We will do this now.";
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext arg0) {
			return new PickNamePrompt();
		}
	}

	private class WelcomePrompt extends MessagePrompt{

		@Override
		public String getPromptText(ConversationContext arg0) {

			String welcome = ChatColor.AQUA + "~ " + ChatColor.GREEN + "~ " + ChatColor.RED + "~ "
					+ ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Welcome to The Lord of the Craft!"
					+ ChatColor.RED + " ~" + ChatColor.GREEN + " ~" + ChatColor.AQUA + " ~";

			String tx = ChatColor.AQUA + "Get ready, your adventure is about to unfold...\n";

			String ta = ChatColor.GRAY + "Before you can start, you must register "
					+ "your Persona or Character. We will do this now.";

			return welcome + DIVIDER + tx + ta + DIVIDER;
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext arg0) {
			return new PickNamePrompt();
		}
	}

	private class PickNamePrompt extends ValidatingPrompt{

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			String pretext = "Please type a name for your RP Persona!";
			String affix = "\n" + NOTE + "You may only change your Persona's name every 2 days.";

			return pretext + (p.hasPermission("archecore.quickrename")? "" : affix) + DIVIDER;
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			return p.hasPermission("archecore.longname") || input.length() <= 32;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			context.setSessionData("name", input);
			return new PickSexPrompt();
		}
	}

	private class PickSexPrompt extends FixedSetPrompt {

		private PickSexPrompt(){
			super("female", "male", "other");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			new ArcheMessage("Please type the desired Gender of your Persona.").applyChatColor(ChatColor.YELLOW).sendTo(p);
			ChatMessage mains = new ArcheMessage("Available Options: ").applyChatColor(ChatColor.YELLOW);

			for(String s : new String[]{"female", "male", "other"}){
				mains.addLine(s)
				.setUnderlined()
				.applyChatColor(ChatColor.WHITE)
				.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select")
				.setClickEvent(ChatBoxAction.RUN_COMMAND, s)
				.addLine(",  ");
			}

			mains.sendTo(p);
			return DIVIDER;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			int gender = input.equals("female")? 0 : input.equals("male")? 1:2;
			context.setSessionData("gender", gender);
			return new PickRacePrompt();
		}


	}

	private class PickRacePrompt extends ValidatingPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			ChatMessage mains = new ArcheMessage("Main Races: ").applyChatColor(ChatColor.YELLOW);

			for(int i = 0; i < 5; i++){
				Race race = Race.values()[i];
				if(p.hasPermission("archecore.race." + race.toString().toLowerCase())){
					mains.addLine(race.getName())
					.setUnderlined()
					.applyChatColor(ChatColor.WHITE)
					.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select this Race")
					.setClickEvent(ChatBoxAction.RUN_COMMAND, race.getName())
					.addLine(",  ");
				}
			}

			mains.addLine("more...")
			.setItalic()
			.applyChatColor(ChatColor.GRAY)
			.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click for more races")
			.setClickEvent(ChatBoxAction.RUN_COMMAND, "more");

			mains.sendTo(p);

			String pretext = ChatColor.YELLOW + "Please type your Persona's race, or type " + ChatColor.WHITE + "more"
					+ ChatColor.YELLOW + " to see all available races.";

			return pretext + DIVIDER;
		}


		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			if(input.equalsIgnoreCase("more")) return true;

			Race r = findRace(input);

			Player p = (Player) context.getForWhom();
			if(r==null || !p.hasPermission("archecore.race." + r.toString().toLowerCase())){
				return false;
			} else {
				context.setSessionData("race", r);
				return true;
			}
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if(input.equalsIgnoreCase("more")) return new PickMoreRacePrompt();
			if(findRace(input).hasChildren()) return new PickSubRacePrompt(input);
			else return new SetAgePrompt();
		}

		private Race findRace(String s){
			s = s.replace('\'', ' ');
			for(Race r : Race.values()){
				if(s.equalsIgnoreCase(r.getName().replace('\'', ' '))) return r;
			}
			return null;
		}
	}

	private class PickSubRacePrompt extends ValidatingPrompt {
		String string;

		private PickSubRacePrompt(String string){
			this.string = string;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			Race selected = findRace(string);

			ChatMessage subraces = new ArcheMessage("Sub Race Options: ").applyChatColor(ChatColor.YELLOW);
			subraces.addLine(selected.getName())
				.setUnderlined()
				.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select this Race")
				.setClickEvent(ChatBoxAction.RUN_COMMAND, selected.getName())
				.addLine(", ");

			for (Race race : Race.values()){
				if (Objects.equals(race.getParentRace(), selected.getName())
						&& race != selected){
					subraces.addLine(race.getName())
					.setUnderlined()
					.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select this Race")
					.setClickEvent(ChatBoxAction.RUN_COMMAND, race.getName())
					.addLine(", ");
				}
			}

			subraces.addLine("back...")
				.setItalic()
				.applyChatColor(ChatColor.GRAY)
				.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Go back to Race selection")
				.setClickEvent(ChatBoxAction.RUN_COMMAND, "back");

			subraces.sendTo(p);

			String pretext = "You have selected "+ChatColor.WHITE+selected.getName()+ChatColor.YELLOW+". These are the available subraces to you, should you choose them. Type the name of the subrace or click to select.";

			return pretext + DIVIDER;
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			if ("back".equalsIgnoreCase(input)) return true;
			Race r = findRace(input);

			Player p = (Player) context.getForWhom();
			if(r==null || !p.hasPermission("archecore.race." + r.toString().toLowerCase())){
				return false;
			} else {
				context.setSessionData("race", r);
				return true;
			}
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if ("back".equalsIgnoreCase(input)) return new PickRacePrompt();
			return new SetAgePrompt();
		}

		private Race findRace(String s){
			s = s.replace('\'', ' ');
			for(Race r : Race.values()){
				if(s.equalsIgnoreCase(r.getName().replace('\'', ' '))) return r;
			}
			return null;
		}
	}

	private class PickMoreRacePrompt extends ValidatingPrompt {

		@Override
		public String getPromptText(ConversationContext context) {

			ChatMessage m = new ArcheMessage("Available Races: ").applyChatColor(ChatColor.YELLOW);

			Player p = (Player) context.getForWhom();

			for(Race race : Race.values()){
				if(p.hasPermission("archecore.race." + race.toString().toLowerCase())){
					m.addLine(race.getName())
					.applyChatColor(ChatColor.WHITE)
					.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select this Race")
					.setClickEvent(ChatBoxAction.RUN_COMMAND, race.getName())
					.addLine(",  ");
				}
			}

			m.sendTo(p);
			return DIVIDER;
		}


		@Override
		protected boolean isInputValid(ConversationContext context, String input) {

			Race r = findRace(input);

			Player p = (Player) context.getForWhom();
			if(r==null || !p.hasPermission("archecore.race." + r.toString().toLowerCase())){
				return false;
			} else {
				context.setSessionData("race", r);
				return true;
			}
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			return new SetAgePrompt();
		}

		private Race findRace(String s){
			s = s.replace('\'', ' ');
			for(Race r : Race.values()){
				if(s.equalsIgnoreCase(r.getName().replace('\'', ' '))) return r;
			}
			return null;
		}
	}

	private class SetAgePrompt extends NumericPrompt{

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			int nr = ((Race) context.getSessionData("race")).getMaximumAge();
			String pretext = "Please specify the age of your Persona.\n";
			String affix = "\n" + NOTE + "Age must be between 5 and "+nr+" and "
					+ "make sense with the lore of your chosen race.";

			return pretext + (p.hasPermission("archecore.ageless")? "" : affix) + DIVIDER;
		}

		@Override
		public boolean isNumberValid(ConversationContext context, Number input){
			Player p = (Player) context.getForWhom();

			if(p.hasPermission("archecore.ageless")) return true;

			int nr = ((Race) context.getSessionData("race")).getMaximumAge();
			int age = input.intValue();
			return (age >= 5 && age <= nr);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context,	Number age) {
			context.setSessionData("age", age.intValue());
			return new AutoAgePrompt();
		}
	}

	private class AutoAgePrompt extends BooleanPrompt{

		@Override
		public String getPromptText(ConversationContext context) {
			return "Automatically age your Persona: yes(recommended) or no?" + DIVIDER;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			context.setSessionData("autoage", input);
			return new PersonaConfirmPrompt();
		}
	}

	private class PersonaConfirmPrompt extends MessagePrompt{

		@Override
		public String getPromptText(ConversationContext context) {
			String name = (String) context.getSessionData("name");
			return ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Created your new Persona: " + ChatColor.GREEN + name;
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			int id = (Integer) context.getSessionData("slot");
			String name = (String) context.getSessionData("name");
			int gender = (Integer) context.getSessionData("gender");
			Race race = (Race) context.getSessionData("race");
			int age = (Integer) context.getSessionData("age");
			boolean autoAge = (Boolean) context.getSessionData("autoage");
			long creationTimeMS = System.currentTimeMillis();
			Persona pers = ArchePersonaHandler.getInstance().createPersona(p, id, name, race, gender, age, autoAge, creationTimeMS);
			ArcheSkillFactory.getSkill("internal_drainxp").addRawXp(pers,75000);

			if(pers != null && context.getSessionData("first") != null){
				Economy econ = ArcheCore.getControls().getEconomy();
				if(econ != null) econ.setPersona(pers, econ.getBeginnerAllowance());
				if(ArcheCore.getControls().getPersonaHandler().willModifyDisplayNames()) p.setDisplayName(name);

				long treshold = System.currentTimeMillis() - (1000*90);
				if(lastAnnounce < treshold){
					lastAnnounce = System.currentTimeMillis();
					String message = ChatColor.DARK_GREEN + "Please welcome " + ChatColor.GOLD + name + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (" + p.getName() + ") " + ChatColor.DARK_GREEN + "the " + ChatColor.GOLD + race.getName() + ChatColor.DARK_GREEN + " to Lord of the Craft.";
					for(Player x : Bukkit.getOnlinePlayers()) if (x != p) x.sendMessage(message);
				}

			}

			return Prompt.END_OF_CONVERSATION;
		}
	}

	private class Prefix implements ConversationPrefix{

		@Override
		public String getPrefix(ConversationContext context) {
			return ""+ChatColor.YELLOW;
		}


	}
}
