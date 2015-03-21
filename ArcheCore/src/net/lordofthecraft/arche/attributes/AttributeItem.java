package net.lordofthecraft.arche.attributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.md_5.bungee.api.chat.TranslatableComponent;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Static class containing the methods for applying an AttributeModifier to an item.
 */
public class AttributeItem {
	final private static String PATH = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "."; 
	
	//NBT Base class
	private static Class<?> NBTBase;
	
	//NBT Compound tag, needed to create an AttributeModifier
	private static Class<?> NBTTagCompound;
	private static Constructor<?> compoundConstructor;
	
	//All methods needed to make an AttributeModifier happen
	private static Method getLong;
	private static Method setString;
	private static Method setLong;
	private static Method setInt;
	private static Method setDouble;
	private static Method hasKey;
	
	private static Method hasAttributes;
	private static Method getTagMethod;
	private static Method setTagMethod;
	
	private static Method saveMethod;
	private static Method itemNameMethod;
	
	static{
		try{
			NBTBase = Class.forName(PATH + "NBTBase");
			NBTTagCompound = Class.forName(PATH + "NBTTagCompound");
			compoundConstructor = NBTTagCompound.getConstructor();
			 
			hasKey = NBTTagCompound.getMethod("hasKey", String.class);
			getLong = NBTTagCompound.getMethod("getLong", String.class);
			setString = NBTTagCompound.getMethod("setString", String.class, String.class);
			setLong = NBTTagCompound.getMethod("setLong", String.class, long.class);
			setInt = NBTTagCompound.getMethod("setInt", String.class, int.class);
			setDouble = NBTTagCompound.getMethod("setDouble", String.class, double.class);
		}catch(Throwable t){t.printStackTrace();}
	}
	
	/**
	 * Retrieve NBT Tag saving this item as a string
	 * @param is Item to check
	 * @return String of NBT
	 */
	public static String getNBTString(ItemStack is){
		try{
			Object nmsItemStack = toNMSStack(is);
			if(saveMethod == null) saveMethod = nmsItemStack.getClass().getMethod("save", NBTTagCompound);
			return saveMethod.invoke(nmsItemStack, NBTTagCompound.newInstance()).toString();
		}catch(Throwable t){t.printStackTrace();}
		return null;
	}
	
	/**
	 * Name of ItemStack as displayed by en_US language
	 * @param is ItemStack to check
	 * @return the translated name
	 */
	public static String getItemEnglishName(ItemStack is){
		TranslatableComponent comp = new TranslatableComponent();
		comp.setTranslate(getItemLocaleName(is));
		return comp.toPlainText();
	}
	
	/**
	 * Retrieve item name recognized by MCs Locale translations
	 * @param is Item to check
	 * @return The internal ItemStack translatable name
	 */
	public static String getItemLocaleName(ItemStack is){
		try{
			Object nmsItemStack = toNMSStack(is);
			if(itemNameMethod == null) itemNameMethod = nmsItemStack.getClass().getMethod("a");
			return itemNameMethod.invoke(nmsItemStack).toString() + ".name";
		}catch(Throwable t){t.printStackTrace();}
		return null;
	}
	
	/**
	 * Check if an item has any AttributeModifiers applied to it
	 * @param is Item to check
	 * @return if AttributeModifiers were found on the ItemMeta
	 */
	public static boolean hasAttributes(ItemStack is){
		try{
			ItemMeta m = is.getItemMeta();
			if(hasAttributes == null){ 
				hasAttributes = Class.forName(Bukkit.getServer().getClass().getPackage().getName()+ ".inventory.CraftMetaItem").getDeclaredMethod("hasAttributes");
				hasAttributes.setAccessible(true);
			}
			return (Boolean) hasAttributes.invoke(m);
		}catch(Throwable t){t.printStackTrace(); return false;}
	}
	
	/**
	 * Add a modifier to an item
	 * @param m the AttributeModifier to invoke
	 * @param is The base ItemStack to apply a modifier to
	 * @return The ItemStack with the modifier applied 
	 */
	public static ItemStack addModifier(AttributeModifier m, ItemStack is){
		try{
			net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = (net.minecraft.server.v1_8_R1.ItemStack) toNMSStack(is);
			Object compound = compoundConstructor.newInstance();
			
			//Setting the compound with the required values from the Wrapped Modifier.
			long least = m.getUUID().getLeastSignificantBits();
			long most  = m.getUUID().getMostSignificantBits();
			setLong.invoke(compound, "UUIDLeast",least);
			setLong.invoke(compound, "UUIDMost", most);
			
			setString.invoke(compound, "AttributeName", m.getAttribute().getName());
			setString.invoke(compound, "Name", m.getName());
			
			setDouble.invoke(compound, "Amount", m.getValue());
			setInt.invoke(compound, "Operation", m.getOperation());
			
			//Find the correct place in the NBT Item Hierarchy to put the data
			//if(getTagMethod == null) getTagMethod = nmsItemStack.getClass().getMethod("getTag");
			//Object tag = getTagMethod.invoke(nmsItemStack);
			Object tag = nmsItemStack.getTag();
			Object listNBT;
			
			//no tag means no ItemMeta, which in turn means the modifier does not exist
			if(tag == null){
				tag = compoundConstructor.newInstance();
				//if(setTagMethod == null) setTagMethod = nmsItemStack.getClass().getMethod("setTag");
				//setTagMethod.invoke(nmsItemStack, tag);
				nmsItemStack.setTag((net.minecraft.server.v1_8_R1.NBTTagCompound) tag);
				listNBT = createAttributeModifierList(tag);
			} else if(!NBTCompoundHasKey(tag, "AttributeModifiers")) {
				listNBT = createAttributeModifierList(tag);
			} else {
				listNBT = NBTTagCompound.getMethod("get", String.class).invoke(tag, "AttributeModifiers");
				removeNBTModifier(listNBT, least, most);
			}
			
			//Adds the modifier to the list of modifiers
			listNBT.getClass().getMethod("add", NBTBase).invoke(listNBT, compound);
			
			return toBukkitStack(nmsItemStack);
		}catch(Throwable t){t.printStackTrace();}
		
		return null;
	}
	
	private static Object createAttributeModifierList(Object tag) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		
		Class<?> clazz = Class.forName(PATH + "NBTTagList");
		Object list = clazz.getConstructor().newInstance();
		NBTTagCompound.getMethod("set", String.class, NBTBase).invoke(tag, "AttributeModifiers",list);
		return list;
	}
	
	/**
	 * Removes a modifier with a certain UUID for a certain Attribute from an ItemStack.
	 * @param m The modifier to remove
	 * @param is The ItemStack to remove the modifier from.
	 * @return The ItemStack with the modifier removed
	 */
	public static ItemStack removeModifier(AttributeModifier m, ItemStack is){
		try{
			Object nmsItemStack = toNMSStack(is);
			
			//Grab the ItemMeta tag
			if(getTagMethod == null) getTagMethod = nmsItemStack.getClass().getMethod("getTag");
			Object tag = getTagMethod.invoke(nmsItemStack);
			
			//no tag means no ItemMeta, which in turn means the modifier does not exist
			if(tag == null) return is;
			
			//No AttributeModifier list was found --> nothing to remove
			if(!NBTCompoundHasKey(tag,"AttributeModifiers")) return is;
			
			Object listNBT = NBTTagCompound.getMethod("get", String.class).invoke(tag, "AttributeModifiers");
			removeNBTModifier(listNBT, m.getUUID().getLeastSignificantBits(), m.getUUID().getMostSignificantBits());
			return toBukkitStack(nmsItemStack);
			
		}catch(Throwable t){t.printStackTrace();}	
		
		return null;
	}
	
	private static Object toNMSStack(ItemStack stack) 
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method convert = Class.forName(Bukkit.getItemFactory().getClass().getPackage().getName() + ".CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
		Object result = convert.invoke(null, stack);
		return result;
	}
	
	private static ItemStack toBukkitStack(Object nmsItemStack) 
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method convert = Class.forName(Bukkit.getItemFactory().getClass().getPackage().getName() + ".CraftItemStack").getMethod("asCraftMirror",nmsItemStack.getClass());
		ItemStack result = (ItemStack) convert.invoke(null, nmsItemStack);
		return result;
	}
	
	//Use to check if the specified key exists in the NBT compound object.
	//For example the 'tag' and 'AttributeModifiers' key
	private static boolean NBTCompoundHasKey(Object compound, String key) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (Boolean) hasKey.invoke(compound, key);
	}
	
	private static void removeNBTModifier(Object NBTList, long least, long most) 
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException{
		
		Field f = NBTList.getClass().getDeclaredField("list");
		f.setAccessible(true);
		List<?> list = (List<?>) f.get(NBTList);
		
		for(Object compound : list){
			long nbtLeast = (Long) getLong.invoke(compound, "UUIDLeast"); 
			long nbtMost  = (Long) getLong.invoke(compound, "UUIDMost");
			if(least == nbtLeast && most == nbtMost){
				list.remove(compound);
				break;
			}
		}
		
		f.setAccessible(false);
	}
	
	
}
