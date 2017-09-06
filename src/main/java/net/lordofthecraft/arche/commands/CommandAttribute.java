package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * User interface for interacting and editing attributes on player entities
 *
 * @author Sean
 */
public class CommandAttribute implements CommandExecutor {

    private static final String DIVIDER = ChatColor.LIGHT_PURPLE +
            "\n--------------------------------------------------\n" + ChatColor.YELLOW;

    public CommandAttribute() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command cannot be run from console.");
            return true;
        }
        if (args.length == 0) {
            return h(sender, command);
        }
        Player send = (Player) sender;
        Player p = Bukkit.getPlayer(args[0]);
        if (p != null) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.AQUA + "Player Attributes for: " + ChatColor.GOLD + p.getName());
                for (Attribute a : Attribute.values()) {
                    AttributeInstance ai = p.getAttribute(a);
                    if (ai != null) {
                        sender.sendMessage(ChatColor.GOLD + a.name() + ":"
                                + "\n" + ChatColor.DARK_AQUA + " Base Value: " + ChatColor.RESET + ai.getBaseValue() + ","
                                + "\n" + ChatColor.DARK_AQUA + " Current Value: " + ChatColor.RESET + ai.getValue() + ","
                                + "\n" + ChatColor.DARK_AQUA + " Modifier count: " + ChatColor.RESET + ai.getModifiers().size());
                    } else {
                        sender.sendMessage(ChatColor.RED + "No instance for " + ChatColor.AQUA + a.name());
                    }
                }
                return true;
            }
            boolean flag = false;
            for (Attribute att : Attribute.values()) {
                if (att.name().equals(args[1].toUpperCase())) {
                    flag = true;
                }
            }
            if (flag) {
                Attribute attr = Attribute.valueOf(args[1].toUpperCase());
                AttributeInstance ai = p.getAttribute(attr);
                Collection<AttributeModifier> mods = ai.getModifiers();
                if (args.length == 2) {
                    sender.sendMessage(ChatColor.AQUA + "Displaying attribute modifiers of " + ChatColor.GOLD + attr.name() + ChatColor.AQUA + " for the player " + ChatColor.GOLD + p.getName());
                    int count = 0;
                    for (AttributeModifier mod : mods) {
                        BaseComponent[] comps = new ComponentBuilder(count + ". ")
                        .color(MessageUtil.convertColor(ChatColor.GOLD))
                        .append("Name: ").color(MessageUtil.convertColor(ChatColor.DARK_AQUA))
                        .append(mod.getName() + ", ", FormatRetention.NONE)
                        .append("UUID: ").color(MessageUtil.convertColor(ChatColor.DARK_AQUA))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, mod.getUniqueId().toString()))
                        .event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to paste to your chat."))
                        .append(mod.getUniqueId().toString() + ", ", FormatRetention.EVENTS)
                        .append("Amt: ", FormatRetention.NONE).color(MessageUtil.convertColor(ChatColor.DARK_AQUA))
                        .append(mod.getAmount() + ", ", FormatRetention.NONE)
                        .append("Operation: ").color(MessageUtil.convertColor(ChatColor.DARK_AQUA))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, mod.getOperation().name()))
                        .event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to paste to your chat."))
                        .append(mod.getOperation().name() + ", ", FormatRetention.EVENTS)
                        .create();
                        
                        send.spigot().sendMessage(comps);
                        count++;
                    }
                    return true;
                } else if (args.length >= 3) {
                    if ("-r".equalsIgnoreCase(args[2]) && args.length >= 4) {
                        AttributeModifier toRemove = null;
                        if (NumberUtils.isCreatable(args[3])) {
                            int id = Integer.valueOf(args[3]);
                            if (id <= mods.size()) {
                                int count = 0;
                                for (AttributeModifier mod : mods) {
                                    if (count == id) {
                                        toRemove = mod;
                                        break;
                                    }
                                    ++count;
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "There is no attribute modifier in slot " + id);
                            }
                        } else {
                            UUID id = UUID.fromString(args[3]);
                            if (id != null) {
                                Optional<AttributeModifier> opmod = mods.stream().findFirst().filter(mod -> mod.getUniqueId().equals(id));
                                if (opmod.isPresent()) {
                                    toRemove = opmod.get();
                                }
                            }
                        }
                        if (toRemove != null) {
                            ConversationFactory factory = new ConversationFactory(ArcheCore.getPlugin());
                            factory.withFirstPrompt(new RemoveAttributePrompt(p, ai, toRemove))
                                    .thatExcludesNonPlayersWithMessage("screw you tythus")
                                    .withModality(true)
                                    .withPrefix(new Prefix());
                            addAbandoners(factory);
                            factory.buildConversation(send).begin();
                        } else {
                            sender.sendMessage(ChatColor.RED + "Error: Could not find attribute with the id of " + args[3]);
                        }
                        return true;
                    } else if ("-a".equalsIgnoreCase(args[2])) {
                        ConversationFactory factory = new ConversationFactory(ArcheCore.getPlugin())
                                .withFirstPrompt(new SelectAttributeNamePrompt(p, attr))
                                .thatExcludesNonPlayersWithMessage("screw you tythus")
                                .withModality(true)
                                .withPrefix(new Prefix());
                        addAbandoners(factory);
                        factory.buildConversation(send).begin();
                        return true;
                    }
                }
            }
        }
        return h(sender, command);
    }

    private boolean h(CommandSender s, String c) {
        s.sendMessage(ChatColor.AQUA + "/" + c + " usage:");
        s.sendMessage(ChatColor.GOLD + "/" + c + " [Player] :" + ChatColor.GRAY + " Prints out the current state of the player's attribute instances.");
        s.sendMessage(ChatColor.GOLD + "/" + c + " [Player] [Attribute] :" + ChatColor.GRAY + " Prints out all the current modifiers for the Player's attribute instance of the specified type.");
        s.sendMessage(ChatColor.GOLD + "/" + c + " [Player] [Attribute] -r [uuid/id] :" + ChatColor.GRAY + " Removes the attribute modifier of the specified ID. Id is found with the previous command.");
        s.sendMessage(ChatColor.GOLD + "/" + c + " [Player] [Attribute] -a :" + ChatColor.GRAY + " Construct a new attribute to applied to this player." + ChatColor.RED + " EXTREMELY DANGEROUS");
        StringBuilder sb = new StringBuilder();
        sb.append("Valid Attributes: ");
        String ss = "";
        for (Attribute at : Attribute.values()) {
            sb.append(ss);
            sb.append(at.name());
            ss = ", ";
        }
        s.sendMessage(sb.toString());
        return true;
    }

    private void addAbandoners(ConversationFactory factory) {
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

    private static class RemoveAttributePrompt extends BooleanPrompt {

        private final Player pl;
        private final AttributeInstance at;
        private final AttributeModifier toRemove;

        public RemoveAttributePrompt(Player pl, AttributeInstance at, AttributeModifier toRemove) {
            this.pl = pl;
            this.at = at;
            this.toRemove = toRemove;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext conversationContext, boolean b) {
            if (b) {
                at.removeModifier(toRemove);
            }
            return Prompt.END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return "Confirm Removal: Attr: " + at.getAttribute() + ", Name: " + toRemove.getName() + ", UUID: " + toRemove.getUniqueId() + ", Amt: " + toRemove.getAmount() + ", " +
                    "Operation: " + toRemove.getOperation() + "\n" +
                    "Player: " + pl.getName() + "\n" +
                    ChatColor.GRAY + "(Yes to accept, anything else to deny.)" + DIVIDER;
        }
    }

    //[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}

    private static class SelectAttributeNamePrompt extends StringPrompt {

        private final UUID player;
        private final String attr;

        public SelectAttributeNamePrompt(Player target, Attribute attribute) {
            super();
            player = target.getUniqueId();
            attr = attribute.name();
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return "Please enter the name for your attribute modifier" + DIVIDER;
        }

        @Override
        public Prompt acceptInput(ConversationContext conversationContext, String s) {
            conversationContext.setSessionData("name", s);
            conversationContext.setSessionData("player", player);
            conversationContext.setSessionData("attribute", attr);
            return new SelectAttributeUUIDPrompt();
        }
    }

    private static class SelectAttributeUUIDPrompt extends RegexPrompt {

        public SelectAttributeUUIDPrompt() {
            super("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext conversationContext, String s) {
            conversationContext.setSessionData("id", UUID.fromString(s));
            return new SelectAttributeOperationPrompt();
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            BaseComponent[] create = new ComponentBuilder(DIVIDER)
            .append("Click me to generate a random UUID.")
            .event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click me to generate a random UUID."))
            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, UUID.randomUUID().toString()))
            .color(MessageUtil.convertColor(ChatColor.DARK_GRAY))
            .create();
            
            ((Player) conversationContext.getForWhom()).spigot().sendMessage(create);
            return "Please enter a UUID or click above to generate a random one." + DIVIDER;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.RED + "Invalid UUID";
        }
    }

    private static class SelectAttributeOperationPrompt extends ValidatingPrompt {

        public SelectAttributeOperationPrompt() {
            super();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.RED + "Please select a valid operation.";
        }

        @Override
        protected boolean isInputValid(ConversationContext conversationContext, String s) {
            for (AttributeModifier.Operation op : AttributeModifier.Operation.values()) {
                if (op.name().equals(s.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext conversationContext, String s) {
            conversationContext.setSessionData("operation", s);
            return new SelectAttributeAmountPrompt();
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
        	ComponentBuilder b = new ComponentBuilder(DIVIDER);
        	b.append("Valid Operations: ");
        	
            String ss = "";
            for (AttributeModifier.Operation op : AttributeModifier.Operation.values()) {
                if(!ss.isEmpty()) b.append(ss);
                b.append(op.name())
                .event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to select"))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, op.name()));
               
                ss = ", ";
            }
        	
            ((Player) conversationContext.getForWhom()).spigot().sendMessage(b.create());
            return "Please select what operation this attribute modifier will be using." + DIVIDER;
        }
    }

    private static class SelectAttributeAmountPrompt extends NumericPrompt {

        public SelectAttributeAmountPrompt() {
            super();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.RED + "Please enter a number";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext conversationContext, Number number) {
            conversationContext.setSessionData("amount", number.floatValue());
            return new ConfirmAttributeModifierCreationPrompt();
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return "Please input the amount this attribute should modify." + DIVIDER;
        }
    }

    private static class ConfirmAttributeModifierCreationPrompt extends BooleanPrompt {

        public ConfirmAttributeModifierCreationPrompt() {
            super();
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext conversationContext, boolean b) {
            if (b) {
                Map<Object, Object> data = conversationContext.getAllSessionData();
                Player target = Bukkit.getPlayer((UUID) data.get("player"));
                if (target != null) {
                    Attribute att = Attribute.valueOf((String) data.get("attribute"));
                    String name = (String) data.get("name");
                    UUID id = (UUID) data.get("id");
                    double amt = (float) data.get("amount");
                    AttributeModifier.Operation op = AttributeModifier.Operation.valueOf((String) data.get("operation"));
                    AttributeInstance instance = target.getAttribute(att);
                    instance.addModifier(new AttributeModifier(id, name, amt, op));
                } else {
                    conversationContext.getForWhom().sendRawMessage(ChatColor.RED + "The player you were targeting has gone offline.");
                }
            }
            return Prompt.END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return "Please confirm this is the modifier you want to apply:\n" +
                    "Attribute: " + context.getSessionData("attribute") +
                    ", Name: " + context.getSessionData("name") +
                    ", ID: " + context.getSessionData("id") +
                    ", Amt: " + context.getSessionData("amount") +
                    ", Operation: " + context.getSessionData("operation") + DIVIDER;
        }
    }

    private class Prefix implements ConversationPrefix {
        @Override
        public String getPrefix(ConversationContext context) {
            return "" + ChatColor.YELLOW;
        }
    }
}
