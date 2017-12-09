package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.listener.PersonaCreationAbandonedListener;
import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class CreationDialog {

    public static final String DIVIDER = ChatColor.LIGHT_PURPLE +
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

        p.sendTitle(ChatColor.BOLD + "" + ChatColor.GOLD + "Welcome",
                ChatColor.YELLOW + "Type your Persona's name to continue",
				 20, 60*20, 20);

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

    public void addPersona(Player p, int slot, boolean nullPersona){
        if(!mayConverse(p)){
            p.sendRawMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You are already in a dialog!");
            p.sendRawMessage(ChatColor.RED + "Type 'cancel' to abandon your current dialog.");
            return;
        }

        if (!canDelete(ArcheCore.getControls().getPersonaHandler().getPersona(p.getUniqueId(), slot), p, nullPersona)) return;

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

        if (!canDelete(pers, p, false)) return;

        addAbandoners();

        Map<Object, Object> data = Maps.newHashMap();
        data.put("persona", pers);

        factory.withInitialSessionData(data)
                .withFirstPrompt(new ConfirmRemovalPrompt())
                .buildConversation(p)
                .begin();
    }

    private boolean canDelete(Persona pers, Player p, boolean nullPersona) {
        if (nullPersona || pers == null) return true;
        if(p.hasPermission("archecore.persona.quickkill")) return true;

        int permakillDays = ArcheCore.getControls().getNewPersonaPermakillDelay();
        if(permakillDays <= 0) return true;

        long permakillMs = permakillDays * 24 * 3600 * 1000;
        final long goodTimeSinceCreation = pers.getCreationTime().getTime() + permakillMs;
        if ((goodTimeSinceCreation < System.currentTimeMillis())) {
            return true;
        }
        p.sendMessage(ChatColor.RED + "You must wait at least "+ChatColor.WHITE + permakillDays + ChatColor.RED + " days before deleting your persona " + ChatColor.WHITE + pers.getName() + ChatColor.RED + ".");
        p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Type /persona created to see when you made this persona.");
        return false;

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

    private class RemoveExecutedPrompt extends MessagePrompt {
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

    private class RedoCharacterPrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext arg0) {
            return "You can register a new Persona in this slot. We will do this now.";
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext arg0) {
            return new PickNamePrompt();
        }
    }

    private class WelcomePrompt extends MessagePrompt {

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

    private class PickNamePrompt extends ValidatingPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Player p = (Player) context.getForWhom();
            String pretext = "Please type a name for your Roleplay Persona!";
            String affix = "\n" + NOTE + "You may only change your Persona's name every 2 hours.";

            return pretext + (p.hasPermission("archecore.persona.quickrename") ? "" : affix) + DIVIDER;
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            Player p = (Player) context.getForWhom();

            if (input.startsWith("/")) return false;
            String lower = input.toLowerCase();
            return !lower.contains("help") && !lower.contains("hello") && !lower.contains("why") && !lower.contains("?") && (p.hasPermission("archecore.longname") || input.length() <= 32);

        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, String input) {
           return new ConfirmNamePrompt(input);
        }
    }

    private class ConfirmNamePrompt extends BooleanPrompt {

    	private String name;

        public ConfirmNamePrompt(String input) {
			this.name = input;
		}

		@Override
        public String getPromptText(ConversationContext context) {
            Player p = (Player) context.getForWhom();
            BaseComponent mains = new TextComponent(ChatColor.YELLOW + "You have entered " +
                    ChatColor.GREEN + name + ChatColor.YELLOW + " as your character name. Is this " +
                    ChatColor.YELLOW + "correct?\n");
            mains.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select!"));
            mains.addExtra(" ");
            mains.addExtra(MessageUtil.CommandButton("No", "No", "Click to select!"));
            p.spigot().sendMessage(mains);
            return DIVIDER;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
        	 if (input) {
        		 context.setSessionData("name", name);
                 //Player p = (Player) context.getForWhom();
                 return new PickSexPrompt();
        	 } else {
        		 return new PickNamePrompt();
        	 }
        }
    }

    private class PickSexPrompt extends FixedSetPrompt {

        private PickSexPrompt(){
            super("Female", "Male", "Other");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            Player p = (Player) context.getForWhom();

            p.spigot().sendMessage(new ComponentBuilder("Please type the desired Gender of your Persona.")
            		.color(MessageUtil.convertColor(ChatColor.YELLOW))
            		.create()
            		);
            BaseComponent mains = new TextComponent("Available Options: ");
            mains.setColor(MessageUtil.convertColor(ChatColor.YELLOW));

            for (String s : new String[]{"Female", "Male", "Other"}) {
                mains.addExtra(MessageUtil.CommandButton(s, s, "Click to select"));
                mains.addExtra("  ");
            }

            p.spigot().sendMessage(mains);
            return DIVIDER;
        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, String input) {
            context.setSessionData("gender", input);
            return new PickRacePrompt();
        }


    }

    private class PickRacePrompt extends ValidatingPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Player p = (Player) context.getForWhom();

            BaseComponent mains = new TextComponent("Main Races: ");
            mains.setColor(MessageUtil.convertColor(ChatColor.YELLOW));

            for(int i = 0; i < 5; i++){
                Race race = Race.values()[i];
                if(p.hasPermission("archecore.race." + race.toString().toLowerCase())){
                	mains.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to select this Race"));
                    mains.addExtra("  ");
                }
            }

            mains.addExtra( new ComponentBuilder("more...")
            		.italic(true)
            		.color(MessageUtil.convertColor(ChatColor.GRAY))
            		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "more"))
            		.event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click for more races"))
            		.create()[0]
            		);

            MessageUtil.addNewlines(mains);
            p.spigot().sendMessage(mains);

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
            else return new PersonaConfirmPrompt();
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
            BaseComponent subraces = new TextComponent("Sub Race Options: ");
            subraces.setColor(MessageUtil.convertColor(ChatColor.YELLOW));

            subraces.addExtra(MessageUtil.CommandButton(selected.getName(), selected.getName(), "Click to select this Race"));

            for (Race race : Race.values()){
                if (Objects.equals(race.getParentRace(), selected.getName())
                        && race != selected){
                	subraces.addExtra("  ");
                	subraces.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to select this Race"));
                }
            }

            subraces.addExtra( new ComponentBuilder("  back...")
            		.italic(true)
            		.color(MessageUtil.convertColor(ChatColor.GRAY))
            		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "back"))
            		.event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click for more races"))
            		.create()[0]
            		);

            MessageUtil.addNewlines(subraces);
            p.spigot().sendMessage(subraces);
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
            return new PersonaConfirmPrompt();
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

            BaseComponent m = new TextComponent("Available Races: ");
            m.setColor(MessageUtil.convertColor(ChatColor.YELLOW));
            Player p = (Player) context.getForWhom();

            for(Race race : Race.values()){
                if(p.hasPermission("archecore.race." + race.toString().toLowerCase())){
                	m.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to select this Race"));
                    m.addExtra("  ");
                }
            }

            MessageUtil.addNewlines(m);
            p.spigot().sendMessage(m);
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
            return new PersonaConfirmPrompt();
        }

        private Race findRace(String s){
            s = s.replace('\'', ' ');
            for(Race r : Race.values()){
                if(s.equalsIgnoreCase(r.getName().replace('\'', ' '))) return r;
            }
            return null;
        }
    }

    private class PersonaConfirmPrompt extends MessagePrompt{

        @Override
        public String getPromptText(ConversationContext context) {
            String name = (String) context.getSessionData("name");
            return ChatColor.BLUE + "We are now creating Persona with name: " + ChatColor.RESET + name;
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            Player p = (Player) context.getForWhom();

            int id = (Integer) context.getSessionData("slot");
            String name = (String) context.getSessionData("name");
            String gender = (String) context.getSessionData("gender");
            Race race = (Race) context.getSessionData("race");
            long creationTimeMS = System.currentTimeMillis();

            if (ArcheCore.getPlugin().debugMode()) {
                ArcheCore.getPlugin().getLogger().info("New persona created in the world " + p.getWorld().getName() + " with the uuid of " + p.getWorld().getUID());
            }

            ArchePersona persona = new ArchePersona(ArchePersonaHandler.getInstance().getNextPersonaId(), p.getUniqueId(), id, name, race, 0, gender, new Timestamp(creationTimeMS), new Timestamp(System.currentTimeMillis()));

            ArchePersonaHandler.getInstance().registerPersona(persona);
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Created your new Persona: " + ChatColor.GREEN + name + ChatColor.GOLD + "!");

            p.sendTitle(ChatColor.GOLD + "" + ChatColor.ITALIC + name,
                    ChatColor.YELLOW + "Welcome to the realm of " + ArcheCore.getControls().getServerWorldName(),
                    30, 120, 30);

            if (context.getSessionData("first") != null) {
                long treshold = System.currentTimeMillis() - (1000 * 90);
                if (lastAnnounce < treshold) {
                    lastAnnounce = System.currentTimeMillis();
                    String message = ChatColor.DARK_GREEN + "Please welcome " + ChatColor.GOLD + name + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (" + p.getName() + ") " + ChatColor.DARK_GREEN + "the " + ChatColor.GOLD + race.getName() + ChatColor.DARK_GREEN + " to Lord of the Craft.";
                    for (Player x : Bukkit.getOnlinePlayers()) if (x != p) x.sendMessage(message);
                }

            }
            return Prompt.END_OF_CONVERSATION;
        }
    }

    public static class Prefix implements ConversationPrefix {

        @Override
        public String getPrefix(ConversationContext context) {
            return ""+ChatColor.YELLOW;
        }


    }
}