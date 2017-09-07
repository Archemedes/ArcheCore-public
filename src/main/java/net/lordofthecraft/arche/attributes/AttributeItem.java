package net.lordofthecraft.arche.attributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.utility.MinecraftReflection;

import net.md_5.bungee.api.chat.TranslatableComponent;


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
	private static Method saveToJson;
	
	private static Field attributeList;
	private static Method getTagMethod;
	//private static Method setTagMethod;

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
	 * Transforms an itemstack into its JSON equivalent. Useful for HoverEvent SHOW_ITEM or uncommon serialization needs
	 * @param is The item to convert
	 * @return A JSON string (can be turned to JSON object if desired)
	 */
	public static String getItemJson(ItemStack is) {
		try {
			Object nms = toNMSStack(is);
			Object compound = compoundConstructor.newInstance();
			
			if(saveToJson == null) saveToJson = nms.getClass().getMethod("save", NBTTagCompound);
			saveToJson.invoke(nms, compound);
			return compound.toString();
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
			if(attributeList == null){
				Field tags = Class.forName(Bukkit.getServer().getClass().getPackage().getName()+ ".inventory.CraftMetaItem").getDeclaredField("unhandledTags");
				tags.setAccessible(true);
				attributeList = tags;
			}
			if (attributeList.get(m) instanceof TreeMap) {
				TreeMap<?, ?> l = (TreeMap<?, ?>) attributeList.get(m);
				return !l.isEmpty();
			} else {
				HashMap<?, ?> l = (HashMap<?, ?>) attributeList.get(m);
				return !l.isEmpty();
			}

		}catch(Throwable t){t.printStackTrace(); return false;}
	}
	
	/**
	 * Add a modifier to an item
	 * @param att the Minecraft attribute to modify
	 * @param m the AttributeModifier to invoke
	 * @param slot the equipment slot to apply to, can be null.
	 * @param is The base ItemStack to apply a modifier to
	 * @return The ItemStack with the modifier applied 
	 */

	public static ItemStack addModifier(Attribute att, AttributeModifier m, Slot slot, ItemStack is){
		try{

			Object compound = compoundConstructor.newInstance();

			//Setting the compound with the required values from the Wrapped Modifier.
			long least = m.getUniqueId().getLeastSignificantBits();
			long most  = m.getUniqueId().getMostSignificantBits();
			setLong.invoke(compound, "UUIDLeast",least);
			setLong.invoke(compound, "UUIDMost", most);

			setString.invoke(compound, "AttributeName", getInternalAttributeName(att));
			setString.invoke(compound, "Name", m.getName());

			setDouble.invoke(compound, "Amount", m.getAmount());
			setInt.invoke(compound, "Operation", m.getOperation().ordinal()); //unsafe but probably works
			if (slot != null) {
				setString.invoke(compound, "Slot", slot.getField());
			}

			//Find the correct place in the NBT Item Hierarchy to put the data
			/*if(getTagMethod == null) getTagMethod = nmsItemStack.getClass().getMethod("getTag");
			Object tag = getTagMethod.invoke(nmsItemStack);*/

			return addModifier(least, most, compound, is);

		}catch(Throwable t){t.printStackTrace();}

		return null;
	}

	public static ItemStack addModifier(Long least, Long most, Object compound, ItemStack is){
		try {
			net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = (net.minecraft.server.v1_12_R1.ItemStack) toNMSStack(is);
			Object tag = nmsItemStack.getTag();
			Object listNBT;

			//no tag means no ItemMeta, which in turn means the modifier does not exist
			if(tag == null){
				tag = compoundConstructor.newInstance();
				/*if(setTagMethod == null) setTagMethod = nmsItemStack.getClass().getMethod("setTag");
			setTagMethod.invoke(nmsItemStack, tag);*/
				nmsItemStack.setTag((net.minecraft.server.v1_12_R1.NBTTagCompound) tag);
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
		}catch(Exception e){e.printStackTrace();}
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
			removeNBTModifier(listNBT, m.getUniqueId().getLeastSignificantBits(), m.getUniqueId().getMostSignificantBits());
			return toBukkitStack(nmsItemStack);

		}catch(Throwable t){t.printStackTrace();}	

		return null;
	}

	private static Object toNMSStack(ItemStack stack){return MinecraftReflection.getMinecraftItemStack(stack);}
	private static ItemStack toBukkitStack(Object nmsItemStack) {return MinecraftReflection.getBukkitItemStack(nmsItemStack);}
	
	private static String getInternalAttributeName(Attribute att) {
		switch(att) {
		case GENERIC_ARMOR: return "generic.Armor";
		case GENERIC_ARMOR_TOUGHNESS: return "generic.armorToughness";
		case GENERIC_ATTACK_DAMAGE: return "generic.attackDamage";
		case GENERIC_ATTACK_SPEED: return "generic.attackSpeed";
		case GENERIC_FLYING_SPEED: return "generic.flyingSpeed";
		case GENERIC_FOLLOW_RANGE: return "generic.followRange";
		case GENERIC_KNOCKBACK_RESISTANCE: return "generic.knockbackResistance";
		case GENERIC_LUCK: return "generic.luck";
		case GENERIC_MAX_HEALTH: return "generic.maxHealth";
		case GENERIC_MOVEMENT_SPEED: return "generic.movementSpeed";
		case HORSE_JUMP_STRENGTH: return "horse.jumpStrength";
		case ZOMBIE_SPAWN_REINFORCEMENTS: return "zombie.spawnReinforcements";
		default: throw new IllegalArgumentException("Unhandled attribute. Likely a Minecraft update has added new attribute type and the devs need to handle it.");
		}
		
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
	
	public enum Slot {
	    MAINHAND("mainhand"),
	    OFFHAND("offhand"),
	    FEET("feet"),
	    LEGS("legs"),
	    CHEST("chest"),
	    HEAD("head");

	    final String field;
	    
	    Slot(String field) {this.field = field;}

	    public String getField() {
	        return field;
	    }

	    @Override
	    public String toString() {
	        return field;
	    }
	}
}
