package net.lordofthecraft.arche.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import co.lotc.core.bukkit.util.ItemUtil;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier.Decay;
import net.lordofthecraft.arche.attributes.ModifierBuilder;
import net.lordofthecraft.arche.attributes.VanillaAttribute;
import net.lordofthecraft.arche.attributes.items.Decorator;
import net.lordofthecraft.arche.attributes.items.ItemAttribute;
import net.lordofthecraft.arche.attributes.items.StoredAttribute;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.CommandUtil;

public class CommandAttribute implements CommandExecutor {


    private enum Target{ REMOVE, PERSONA, CONSUME, USE, HEAD, CHEST, LEGS, FEET, HAND, OFF_HAND};
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        Persona target = getPersona(sender, args);
        if(target == null) {
        	sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "No valid Persona specified.");
        	return true;
        }
        
        if(args.length >= 2 && args[args.length - 2].equalsIgnoreCase("-p"))
        	args = Arrays.copyOf(args, args.length - 2);
        
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
        	h(sender);
        	return true;
        } else if(args[0].equalsIgnoreCase("types")) {
        	listAttributeTypes(sender);
        } else if(args[0].equalsIgnoreCase("list")) {
        	listPersonaAttributes(sender, target);
        } else if(args.length < 4 || (args.length < 6 && !args[0].equalsIgnoreCase("remove") )) {
        	h(sender);
        } else {
        	//Meat of the exercise: Adding an attribute
       
          Target tx = null;
        	try { tx = Target.valueOf(args[0].toUpperCase()); }
        	catch(IllegalArgumentException e) { sender.sendMessage("Invalid target argument"); return false;}
        	
        	if(tx != Target.PERSONA && tx != Target.REMOVE) {
        		Player x = target.getPlayer();
        		if(x == null || !ItemUtil.exists(x.getInventory().getItemInMainHand())) {
        			sender.sendMessage("target player must be holding item to have an item-targeted attribute resolve");
        			return true;
        		}
        	}
          
        	ArcheAttribute attribute = AttributeRegistry.getSAttribute(args[1]);
        	if(attribute == null) attribute = AttributeRegistry.getSAttribute(args[1].replace('_', ' '));
        	if(attribute == null) {
        		sender.sendMessage("Invalid Attribute Type. Use '/attribute types' to get a valid list");
        		return true;
        	}
        	
          ModifierBuilder b = new ModifierBuilder();
          b.name(args[2].replace('_', ' '));
          try { handleUUID(b, args[3]); } catch(IllegalArgumentException e) { sender.sendMessage("Invalid UUID argument"); return false; }
          
          if(tx == Target.REMOVE) {
          	target.attributes().removeModifier(attribute, b.create());
          	sender.sendMessage(ChatColor.LIGHT_PURPLE + " Removed an attribute modifier from: " + ChatColor.RESET + target.getName());
          } else {
            try { handleValue(b, args[4]); } catch(NumberFormatException e) { sender.sendMessage("Invalid value argument"); return false; }
            try { handleOperation(b, args[5]); } catch(IllegalArgumentException e) { sender.sendMessage("Invalid operation argument"); return false; }
            
            if(args.length > 6) handleSave(b, args[6]);
            if(args.length > 8) try { handleDecay(b, args[7], args[8]); } catch(IllegalArgumentException e) { sender.sendMessage("Invalid decay args"); return false;}
            
            handleTarget(b, attribute, tx, target);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Resolved an attribute modifier for: " + ChatColor.RESET + target.getName());
          }
        }
        return true;
    }
    
    private Persona getPersona(CommandSender sender, String[] args) {
			//Go through process to find the Persona we want
    	if(args.length > 2 && args[args.length - 2].equalsIgnoreCase("-p")) {
    		return CommandUtil.personaFromArg(args[args.length - 1]);
    	} else if(sender instanceof Player) {
    		return ArcheCore.getPersona(((Player) sender));
    	} else {
    		return null;
    	}
    }
    
    private void h(CommandSender s) {
        s.sendMessage(ChatColor.AQUA + "usage of the /attribute command:");
        s.sendMessage(ChatColor.GOLD + "/attribute " + ChatColor.BLUE + "[target] "
        		+ ChatColor.YELLOW + "[att-type] "
        		+ ChatColor.GRAY + "[name] "
        		+ ChatColor.RED + "[uuid] "
        		+ ChatColor.GREEN + "[value] "
        		+ ChatColor.LIGHT_PURPLE + "[operation] "
        		+ ChatColor.WHITE + "{save} "
        		+ ChatColor.DARK_AQUA + "{decay-type} "
        		+ ChatColor.DARK_GREEN + "{decay-ticks} ");
        s.sendMessage(ChatColor.YELLOW + "Use /attr types for the registered ArcheAttributes");
        s.sendMessage(ChatColor.BLUE + "persona/consume/use/head/chest/legs/feet/hand/off_hand");
        s.sendMessage(ChatColor.GRAY + "name of modifier ( no name with prefix # or hide fully with / )");
        s.sendMessage(ChatColor.RED + "uuid: random/namebased or any valid uuid (with dashes)");
        s.sendMessage(ChatColor.GREEN + "modifier value of att (remember 0 is x1 for multipliers)");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "operations: add_number/add_scalar/multiply_scalar_1");
        s.sendMessage(ChatColor.WHITE + "(optional) save: true/yes/false/no");
        s.sendMessage(ChatColor.DARK_AQUA + "(optional) decaytype: never/active/offline");
        s.sendMessage(ChatColor.DARK_GREEN + "(optional) decay-ticks: amount of ticks until the attribute fades");
        s.sendMessage(ChatColor.GOLD + "ALSO TRY: /attr remove [target] [att-type] [name] [uuid]");
        s.sendMessage(ChatColor.GOLD + "ALSO TRY: /attr list -p {persona-identifier}");
        
    }
    
    private void listAttributeTypes(CommandSender sender) {
    	String theList = AttributeRegistry.getInstance().getAttributes().keySet().stream().collect(Collectors.joining(", "));
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "These are the attributes known to ArcheCore:");
    	sender.sendMessage(theList);
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "Replace spaces with '_' when using as an argument");
    }
    
    private void listPersonaAttributes(CommandSender sender, Persona target) {
			sender.sendMessage(ChatColor.AQUA + "Persona Modifiers:");
			
			List<String> lore = new ArrayList<>();
			target.attributes().getExistingInstances().stream().forEach(aa ->
				target.attributes().getInstance(aa).getModifiers().stream()
				.map(ExtendedAttributeModifier.class::cast)
				.filter(mod -> StringUtils.isEmpty(mod.getName()) || !mod.getName().startsWith("/"))
				.forEach(mod->{
					String modName = mod.getName();
					boolean isCommented = StringUtils.isEmpty(modName) || modName.startsWith("#");
					lore.add(	mod.asReadablePercentage(aa) + ' ' + aa.getName() +
							(isCommented? "" : ( " " + ChatColor.GRAY + "" + ChatColor.ITALIC + '(' + mod.getName() + ')'))
							);
				})
			);
			
			lore.forEach(sender::sendMessage);
    }
    
    private void handleUUID(ModifierBuilder b, String arg) {
    	if(arg.equalsIgnoreCase("random")){
    		b.randomUUID();
    	} else if (arg.equalsIgnoreCase("namebased")) {
    		return; //Taken care of
    	} else {
    		UUID.fromString(arg);
    	}
    }
    
    private void handleValue(ModifierBuilder b, String arg) {
    	b.amount(Double.valueOf(arg));
    }
    
    private void handleOperation(ModifierBuilder b, String arg) {
    	Operation o = Operation.valueOf(arg.toUpperCase());
    	b.operation(o);
    }
    
    private void handleSave(ModifierBuilder b, String arg) {
    	if(arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("no")) b.shouldSave(false);
    }
    
    private void handleDecay(ModifierBuilder b, String type, String ticks) {
    	long ts = Long.valueOf(ticks);
    	Decay d = Decay.valueOf(type.toUpperCase());

    	b.withDecayStrategy(d, ts);
    }
    
    private void handleTarget(ModifierBuilder b, ArcheAttribute attribute, Target tx, Persona target) {
    	PlayerInventory i;
    	ItemStack is;
    	switch(tx) {
    	case PERSONA:
    		target.attributes().addModifier(attribute, b.create());
    		break;
    	case CONSUME: case USE:
    		i = target.getPlayer().getInventory();
    		ExtendedAttributeModifier eam = b.create();
    		StoredAttribute sa = new StoredAttribute(attribute, eam, eam.getTicksRemaining(), eam.getDecayStrategy(), tx == Target.CONSUME);
    		is = sa.apply(i.getItemInMainHand());
    		Decorator.showAttributes(is);
    		i.setItemInMainHand(is);
    		break;
    	default:
    		EquipmentSlot slot = EquipmentSlot.valueOf(tx.toString());
    		var attMod = b.slot(slot).create();

    		i = target.getPlayer().getInventory();

    		if(attribute instanceof VanillaAttribute) {
    			Attribute vanilla = ((VanillaAttribute) attribute).getHandle();
    			is = i.getItemInMainHand();
    			var meta = is.getItemMeta();
    			meta.addAttributeModifier(vanilla, attMod);
    			is.setItemMeta(meta);
    		} else {
    			ItemAttribute ia = new ItemAttribute(attribute, attMod);
    			is = ia.apply(i.getItemInMainHand());
    			Decorator.showAttributes(is);
    		}

    		i.setItemInMainHand(is);
    		break;

    	}
    }
}
