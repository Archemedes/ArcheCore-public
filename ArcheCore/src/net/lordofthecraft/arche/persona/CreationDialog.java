package net.lordofthecraft.arche.persona;

import org.bukkit.plugin.*;

import net.lordofthecraft.arche.*;

import org.bukkit.entity.*;

import net.lordofthecraft.arche.listener.*;

import com.google.common.collect.*;

import org.bukkit.potion.*;
import org.bukkit.*;

import java.lang.reflect.*;

import net.lordofthecraft.arche.help.*;
import net.lordofthecraft.arche.help.ChatMessage;
import net.lordofthecraft.arche.enums.*;

import org.bukkit.conversations.*;

import net.lordofthecraft.arche.interfaces.*;

import java.util.*;

public class CreationDialog
{
    private static final String DIVIDER;
    private static final String NOTE;
    private final ConversationFactory factory;
    private final Plugin plugin;
    private static long lastAnnounce;
    
    public CreationDialog() {
        super();
        this.plugin = (Plugin)ArcheCore.getPlugin();
        this.factory = new ConversationFactory(this.plugin).thatExcludesNonPlayersWithMessage("NO! FU Tythus how are you even seeing this?").withPrefix((ConversationPrefix)new Prefix()).withModality(true);
    }
    
    public void makeFirstPersona(final Player p) {
        final Map<Object, Object> data = (Map<Object, Object>)Maps.newHashMap();
        data.put("slot", 0);
        data.put("first", new Object());
        this.factory.withFirstPrompt((Prompt)new WelcomePrompt()).withInitialSessionData((Map)data).addConversationAbandonedListener((ConversationAbandonedListener)new PersonaCreationAbandonedListener()).buildConversation((Conversable)p).begin();
        final Collection<PotionEffect> e = Lists.newArrayList();
        e.add(new PotionEffect(PotionEffectType.SLOW, 40000, 10));
        e.add(new PotionEffect(PotionEffectType.WEAKNESS, 40000, 10));
        p.addPotionEffects((Collection)e);
    }
    
    public void addPersona(final Player p, final int slot) {
        if (!mayConverse(p)) {
            p.sendRawMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You are already in a dialog!");
            p.sendRawMessage(ChatColor.RED + "Type 'cancel' to abandon your current dialog.");
            return;
        }
        this.addAbandoners();
        final Map<Object, Object> data = (Map<Object, Object>)Maps.newHashMap();
        data.put("slot", slot);
        this.factory.withInitialSessionData((Map)data).withFirstPrompt((Prompt)new RedoCharacterPrompt()).buildConversation((Conversable)p).begin();
    }
    
    public void removePersona(final ArchePersona pers) {
        final Player p = Bukkit.getPlayer(pers.getPlayerUUID());
        if (p == null) {
            this.plugin.getLogger().severe("We tried to engage Persona removal Dialog while owning player was offline!");
            return;
        }
        if (!mayConverse(p)) {
            p.sendRawMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You are already in a dialog!");
            p.sendRawMessage(ChatColor.RED + "Type 'cancel' to abandon your current dialog.");
            return;
        }
        this.addAbandoners();
        final Map<Object, Object> data = (Map<Object, Object>)Maps.newHashMap();
        data.put("persona", pers);
        this.factory.withInitialSessionData((Map)data).withFirstPrompt((Prompt)new ConfirmRemovalPrompt()).buildConversation((Conversable)p).begin();
    }
    
    private void addAbandoners() {
        this.factory.withEscapeSequence("quit").withEscapeSequence("exit").withEscapeSequence("cancel").withEscapeSequence("stop").withEscapeSequence("/stop").withEscapeSequence("/quit").withEscapeSequence("/exit").withEscapeSequence("/cancel").withEscapeSequence("/me").withEscapeSequence("/beaconme").withEscapeSequence("/bme");
    }
    
    public static boolean mayConverse(final Player p) {
        try {
            final Field f = p.getClass().getDeclaredField("conversationTracker");
            f.setAccessible(true);
            final Object conversationTracker = f.get(p);
            final Field f2 = conversationTracker.getClass().getDeclaredField("conversationQueue");
            f2.setAccessible(true);
            final LinkedList<Conversation> list = (LinkedList<Conversation>)f2.get(conversationTracker);
            return list.isEmpty();
        }
        catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    static {
        DIVIDER = ChatColor.LIGHT_PURPLE + "\n--------------------------------------------------\n" + ChatColor.YELLOW;
        NOTE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "NB:" + ChatColor.YELLOW;
        CreationDialog.lastAnnounce = -1L;
    }
    
    private class ConfirmRemovalPrompt extends BooleanPrompt
    {
        public String getPromptText(final ConversationContext context) {
            final String name = ((ArchePersona)context.getSessionData((Object)"persona")).getName();
            return "Are you sure you wish to permakill poor " + ChatColor.GREEN + name + ChatColor.YELLOW + "?";
        }
        
        protected Prompt acceptValidatedInput(final ConversationContext context, final boolean input) {
            if (input) {
                return (Prompt)new RemoveExecutedPrompt();
            }
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    private class RemoveExecutedPrompt extends MessagePrompt
    {
        public String getPromptText(final ConversationContext context) {
            return "This Persona will be permakilled. So long :(";
        }
        
        protected Prompt getNextPrompt(final ConversationContext context) {
            final ArchePersona pers = (ArchePersona)context.getSessionData((Object)"persona");
            pers.remove();
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    private class RedoCharacterPrompt extends MessagePrompt
    {
        public String getPromptText(final ConversationContext arg0) {
            return "You can register a new Persona in this slot. We will do this now.";
        }
        
        protected Prompt getNextPrompt(final ConversationContext arg0) {
            return (Prompt)new PickNamePrompt();
        }
    }
    
    private class WelcomePrompt extends MessagePrompt
    {
        public String getPromptText(final ConversationContext arg0) {
            final String welcome = ChatColor.AQUA + "~ " + ChatColor.GREEN + "~ " + ChatColor.RED + "~ " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Welcome to The Lord of the Craft!" + ChatColor.RED + " ~" + ChatColor.GREEN + " ~" + ChatColor.AQUA + " ~";
            final String tx = ChatColor.AQUA + "Get ready, your adventure is about to unfold...\n";
            final String ta = ChatColor.GRAY + "Before you can start, you must register " + "your Persona or Character. We will do this now.";
            return welcome + CreationDialog.DIVIDER + tx + ta + CreationDialog.DIVIDER;
        }
        
        protected Prompt getNextPrompt(final ConversationContext arg0) {
            return (Prompt)new PickNamePrompt();
        }
    }
    
    private class PickNamePrompt extends ValidatingPrompt
    {
        public String getPromptText(final ConversationContext context) {
            final Player p = (Player)context.getForWhom();
            final String pretext = "Please type a name for your RP Persona!";
            final String affix = "\n" + CreationDialog.NOTE + "You may only change your Persona's name every 2 days.";
            return pretext + (p.hasPermission("archecore.quickrename") ? "" : affix) + CreationDialog.DIVIDER;
        }
        
        protected boolean isInputValid(final ConversationContext context, final String input) {
            final Player p = (Player)context.getForWhom();
            return p.hasPermission("archecore.longname") || input.length() <= 32;
        }
        
        public Prompt acceptValidatedInput(final ConversationContext context, final String input) {
            context.setSessionData((Object)"name", (Object)input);
            return (Prompt)new PickSexPrompt();
        }
    }
    
    private class PickSexPrompt extends FixedSetPrompt
    {
        private PickSexPrompt() {
            super(new String[] { "female", "male", "other" });
        }
        
        public String getPromptText(final ConversationContext context) {
            final Player p = (Player)context.getForWhom();
            new ArcheMessage("Please type the desired Gender of your Persona.").applyChatColor(ChatColor.YELLOW).sendTo(p);
            final ChatMessage mains = new ArcheMessage("Available Options: ").applyChatColor(ChatColor.YELLOW);
            for (final String s : new String[] { "female", "male", "other" }) {
                mains.addLine(s).setUnderlined().applyChatColor(ChatColor.WHITE).setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select").setClickEvent(ChatBoxAction.RUN_COMMAND, s).addLine(",  ");
            }
            mains.sendTo(p);
            return CreationDialog.DIVIDER;
        }
        
        public Prompt acceptValidatedInput(final ConversationContext context, final String input) {
            final int gender = input.equals("female") ? 0 : (input.equals("male") ? 1 : 2);
            context.setSessionData((Object)"gender", (Object)gender);
            return (Prompt)new PickRacePrompt();
        }
    }
    
    private class PickRacePrompt extends ValidatingPrompt
    {
        public String getPromptText(final ConversationContext context) {
            final Player p = (Player)context.getForWhom();
            final ChatMessage mains = new ArcheMessage("Main Races: ").applyChatColor(ChatColor.YELLOW);
            for (int i = 0; i < 5; ++i) {
                final Race race = Race.values()[i];
                if (p.hasPermission("archecore.race." + race.toString().toLowerCase())) {
                    mains.addLine(race.getName()).setUnderlined().applyChatColor(ChatColor.WHITE).setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select this Race").setClickEvent(ChatBoxAction.RUN_COMMAND, race.getName()).addLine(",  ");
                }
            }
            mains.addLine("more...").setItalic().applyChatColor(ChatColor.GRAY).setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click for more races").setClickEvent(ChatBoxAction.RUN_COMMAND, "more");
            mains.sendTo(p);
            final String pretext = ChatColor.YELLOW + "Please type your Persona's race, or type " + ChatColor.WHITE + "more" + ChatColor.YELLOW + " to see all available races.";
            return pretext + CreationDialog.DIVIDER;
        }
        
        protected boolean isInputValid(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("more")) {
                return true;
            }
            final Race r = this.findRace(input);
            final Player p = (Player)context.getForWhom();
            if (r == null || !p.hasPermission("archecore.race." + r.toString().toLowerCase())) {
                return false;
            }
            context.setSessionData((Object)"race", (Object)r);
            return true;
        }
        
        protected Prompt acceptValidatedInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("more")) {
                return (Prompt)new PickMoreRacePrompt();
            }
            return (Prompt)new SetAgePrompt();
        }
        
        private Race findRace(String s) {
            s = s.replace('\'', ' ');
            for (final Race r : Race.values()) {
                if (s.equalsIgnoreCase(r.getName().replace('\'', ' '))) {
                    return r;
                }
            }
            return null;
        }
    }
    
    private class PickMoreRacePrompt extends ValidatingPrompt
    {
        public String getPromptText(final ConversationContext context) {
            final ChatMessage m = new ArcheMessage("Available Races: ").applyChatColor(ChatColor.YELLOW);
            final Player p = (Player)context.getForWhom();
            for (final Race race : Race.values()) {
                if (p.hasPermission("archecore.race." + race.toString().toLowerCase())) {
                    m.addLine(race.getName()).applyChatColor(ChatColor.WHITE).setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click to select this Race").setClickEvent(ChatBoxAction.RUN_COMMAND, race.getName()).addLine(",  ");
                }
            }
            m.sendTo(p);
            return CreationDialog.DIVIDER;
        }
        
        protected boolean isInputValid(final ConversationContext context, final String input) {
            final Race r = this.findRace(input);
            final Player p = (Player)context.getForWhom();
            if (r == null || !p.hasPermission("archecore.race." + r.toString().toLowerCase())) {
                return false;
            }
            context.setSessionData((Object)"race", (Object)r);
            return true;
        }
        
        protected Prompt acceptValidatedInput(final ConversationContext context, final String input) {
            return (Prompt)new SetAgePrompt();
        }
        
        private Race findRace(String s) {
            s = s.replace('\'', ' ');
            for (final Race r : Race.values()) {
                if (s.equalsIgnoreCase(r.getName().replace('\'', ' '))) {
                    return r;
                }
            }
            return null;
        }
    }
    
    private class SetAgePrompt extends NumericPrompt
    {
        public String getPromptText(final ConversationContext context) {
            final Player p = (Player)context.getForWhom();
            final int nr = ((Race)context.getSessionData((Object)"race")).getMaximumAge();
            final String pretext = "Please specify the age of your Persona.\n";
            final String affix = "\n" + CreationDialog.NOTE + "Age must be between 5 and " + nr + " and " + "make sense with the lore of your chosen race.";
            return pretext + (p.hasPermission("archecore.ageless") ? "" : affix) + CreationDialog.DIVIDER;
        }
        
        public boolean isNumberValid(final ConversationContext context, final Number input) {
            final Player p = (Player)context.getForWhom();
            if (p.hasPermission("archecore.ageless")) {
                return true;
            }
            final int nr = ((Race)context.getSessionData((Object)"race")).getMaximumAge();
            final int age = input.intValue();
            return age >= 5 && age <= nr;
        }
        
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number age) {
            context.setSessionData((Object)"age", (Object)age.intValue());
            return (Prompt)new AutoAgePrompt();
        }
    }
    
    private class AutoAgePrompt extends BooleanPrompt
    {
        public String getPromptText(final ConversationContext context) {
            return "Automatically age your Persona: yes(recommended) or no?" + CreationDialog.DIVIDER;
        }
        
        protected Prompt acceptValidatedInput(final ConversationContext context, final boolean input) {
            context.setSessionData((Object)"autoage", (Object)input);
            return (Prompt)new PersonaConfirmPrompt();
        }
    }
    
    private class PersonaConfirmPrompt extends MessagePrompt
    {
        public String getPromptText(final ConversationContext context) {
            final String name = (String)context.getSessionData((Object)"name");
            return ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Created your new Persona: " + ChatColor.GREEN + name;
        }
        
        protected Prompt getNextPrompt(final ConversationContext context) {
            final Player p = (Player)context.getForWhom();
            final int id = (int)context.getSessionData((Object)"slot");
            final String name = (String)context.getSessionData((Object)"name");
            final int gender = (int)context.getSessionData((Object)"gender");
            final Race race = (Race)context.getSessionData((Object)"race");
            final int age = (int)context.getSessionData((Object)"age");
            final boolean autoAge = (boolean)context.getSessionData((Object)"autoage");
            final Persona pers = ArchePersonaHandler.getInstance().createPersona(p, id, name, race, gender, age, autoAge);
            if (pers != null && context.getSessionData((Object)"first") != null) {
                final Economy econ = ArcheCore.getControls().getEconomy();
                if (econ != null) {
                    econ.setPersona(pers, econ.getBeginnerAllowance());
                }
                if (ArcheCore.getControls().getPersonaHandler().willModifyDisplayNames()) {
                    p.setDisplayName(name);
                }
                final long treshold = System.currentTimeMillis() - 90000L;
                if (CreationDialog.lastAnnounce < treshold) {
                    CreationDialog.lastAnnounce = System.currentTimeMillis();
                    final String message = ChatColor.DARK_GREEN + "Please welcome " + ChatColor.GOLD + name + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (" + p.getName() + ") " + ChatColor.DARK_GREEN + "the " + ChatColor.GOLD + race.getName() + ChatColor.DARK_GREEN + " to Lord of the Craft.";
                    for (final Player x : Bukkit.getOnlinePlayers()) {
                        if (x != p) {
                            x.sendMessage(message);
                        }
                    }
                }
            }
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    private class Prefix implements ConversationPrefix
    {
        public String getPrefix(final ConversationContext context) {
            return "" + ChatColor.YELLOW;
        }
    }
}
