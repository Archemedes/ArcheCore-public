package net.lordofthecraft.arche.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

import com.comphenix.protocol.utility.MinecraftReflection;

public class ReflectionUtil {
	private ReflectionUtil() {}
	
	final public static String PATH = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";

	//NBT Compound tag construction
	private static Class<?> NBTTagCompound;
	private static Constructor<?> compoundConstructor;

	//All methods needed to make ItemUtil happen
	private static Method saveToJson;
	private static Method itemNameMethod;

	static{
		try{
			NBTTagCompound = Class.forName(PATH + "NBTTagCompound");
			compoundConstructor = NBTTagCompound.getConstructor();
			
			saveToJson = MinecraftReflection.getItemStackClass().getMethod("save", NBTTagCompound);
			itemNameMethod = MinecraftReflection.getItemStackClass().getMethod("j");
			
		}catch(Throwable t){t.printStackTrace();}
	}
	
	public static Constructor<?> compoundConstructor() { return compoundConstructor; }
	
	public static Method saveToJson() { return saveToJson; }
	
	public static Method itemNameMethod() { return itemNameMethod; }
	
}
