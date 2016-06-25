package net.lordofthecraft.arche.commands;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Sean on 6/23/2016.
 */
public class CommandAttribute implements CommandExecutor {

    public CommandAttribute() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (args.length == 0) {
            return h(sender, command);
        }
        Player p = Bukkit.getPlayer(args[0]);
        if (p != null) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.AQUA + "Player Attributes for: " + ChatColor.GOLD + p.getName());
                for (Attribute a : Attribute.values()) {
                    AttributeInstance ai = p.getAttribute(a);
                    sender.sendMessage(a.name() + ": Base Value: " + ai.getBaseValue() + ", Current Value: " + ai.getValue() + ", Modifier count: " + ai.getModifiers().size());
                }
            }
            Attribute attr = Attribute.valueOf(args[1].toUpperCase());
            if (attr != null) {
                AttributeInstance ai = p.getAttribute(attr);
                Collection<AttributeModifier> mods = ai.getModifiers();
                if (args.length == 2) {
                    sender.sendMessage(ChatColor.AQUA + "Displaying attribute modifiers of " + ChatColor.GOLD + attr.name() + ChatColor.AQUA + " for the player " + ChatColor.GOLD + p.getName());
                    int count = 0;
                    for (AttributeModifier mod : mods) {
                        sender.sendMessage((count + 1) + ". Name: " + mod.getName() + ", UUID: " + mod.getUniqueId() + ", Amt: " + mod.getAmount() + ", Operation: " + mod.getOperation().name());
                        count++;
                    }
                } else if (args.length >= 3) {
                    if ("-r".equalsIgnoreCase(args[2]) && args.length >= 4) {
                        AttributeModifier toRemove;
                        if (NumberUtils.isNumber(args[3])) {
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
                            } else {

                            }
                        }
                        //todo remove
                    } else if ("-a".equalsIgnoreCase(args[2])) {

                    }
                }
            }
        }
        return false;
    }

    private boolean h(CommandSender s, String c) {
        s.sendMessage("/" + c + " usage:");
        s.sendMessage("/" + c + " [Player] : Prints out the current state of the player's attribute instances.");
        s.sendMessage("/" + c + " [Player] [Attribute] : Prints out all the current modifiers for the Player's attribute instance of the specified type.");
        s.sendMessage("/" + c + " [Player] [Attribute] -r [uuid/id] : Removes the attribute modifier of the specified ID. Id is found with the previous command.");
        s.sendMessage("/" + c + " [Player] [Attribute] -a : Construct a new attribute to applied to this player. EXTREMELY DANGEROUS");
        return true;
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
                    ChatColor.GRAY + "(Yes to accept, anything else to deny.)";
        }
    }
}
