package net.lordofthecraft.arche.command.brigadier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.lordofthecraft.arche.command.CmdArg;

public class ItemArg<T> extends CmdArg<T> {
	private static final BrigadierProvider provider = BrigadierProvider.get();
	
	private ItemArg(String name, String errorMessage, String defaultInput, String description) {
		super(name, errorMessage, defaultInput, description);
		this.setBrigadierType(provider.argumentItemStack());
	}
	
	public static ItemArg<Material> material(String name, String errorMessage, String defaultInput, String description){
		ItemArg<Material> result = new ItemArg<>(name, errorMessage, defaultInput, description);
		result.setMapper(s->{
			ItemStack x = result.parse(s);
			return x == null? null : x.getType();
		});
		
		return result;
	}
	
	public static ItemArg<ItemStack> item(String name, String errorMessage, String defaultInput, String description){
		ItemArg<ItemStack> result = new ItemArg<>(name, errorMessage, defaultInput, description);
		result.setMapper(s->result.parse(s));
		return result;
	}
	
	private ItemStack parse(String input) {
		try {
			Object argumentPredicateItemStack = this.getBrigadierType().parse(new StringReader(input));
			Object nmsStack = provider.getItemStackParser().invoke(argumentPredicateItemStack, 1, false);
			ItemStack is = MinecraftReflection.getBukkitItemStack(nmsStack);
			return is;
		} catch (CommandSyntaxException e) { //User Parsing error
			return null;
		} catch (Exception e) { //Unexpected error
			e.printStackTrace();
			return null;
		}
	}
	
}
