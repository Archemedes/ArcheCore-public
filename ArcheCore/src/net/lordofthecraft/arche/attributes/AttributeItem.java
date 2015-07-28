package net.lordofthecraft.arche.attributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.chat.TranslatableComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import net.minecraft.server.v1_8_R3.NBTTagList;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.server.v1_8_R3.NBTTagCompound;


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
	 * Retrieve NBT Tag saving this item as a string
	 * @param is Item to check
	 * @return String of NBT
	 */

	public static LinkedHashMap<String, Object> serialize(ItemStack is) {
		final LinkedHashMap<String, Object> result = Maps.newLinkedHashMap();
		if (is == null) return null;
		if (is.getType() == Material.AIR) return null;
		result.put("type", is.getType().name());
		if (is.getDurability() != 0) {
			result.put("damage", is.getDurability());
		}
		if (is.getAmount() != 1) {
			result.put("amount", is.getAmount());
		}
		final ItemMeta meta = is.getItemMeta();
		if (!Bukkit.getItemFactory().equals(meta, null)) {
			result.put("meta", meta);
		}

		net.minecraft.server.v1_8_R3.ItemStack nmsItemStack = null;
		try {
			nmsItemStack = (net.minecraft.server.v1_8_R3.ItemStack) toNMSStack(is);
		} catch (Exception e) {
		}
		if (nmsItemStack != null) {
			NBTTagCompound tagc = nmsItemStack.getTag();
			if (tagc != null) {
				NBTTagList tag = (NBTTagList) tagc.get("AttributeModifiers");
				if (tag != null) {
					List<HashMap<String, Object>> atts = Lists.newArrayList();
					for (int i = 0 ; i < tag.size() ; i++) {
						NBTTagCompound mod = tag.get(i);
						HashMap<String, Object> modmap = Maps.newHashMapWithExpectedSize(6);
						String name = mod.getString("Name");
						modmap.put("Name", name);
						String attname = mod.getString("AttributeName");
						modmap.put("AttributeName", attname);
						modmap.put("Amount", mod.getDouble("Amount"));
						modmap.put("Operation", mod.getInt("Operation"));
						modmap.put("UUIDMost", mod.getLong("UUIDMost"));
						modmap.put("UUIDLeast", mod.getLong("UUIDLeast"));
						atts.add(modmap);
					}
					if (atts.size() > 0) result.put("att", atts);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static ItemStack deserialize(final Map<String, Object> args) {
		if (args == null) return new ItemStack(Material.AIR);
		final Material type = Material.getMaterial((String)args.get("type"));
		int damage = 0;
		int amount = 1;
		if (args.containsKey("damage")) {
			damage = (Integer) args.get("damage");
		}
		if (args.containsKey("amount")) {
			amount = (Integer) args.get("amount");
		}
		ItemStack result = new ItemStack(type, amount, (short)damage);
		if (args.containsKey("meta")) {
			final Object raw = args.get("meta");
			if (raw instanceof ItemMeta) {
				result.setItemMeta((ItemMeta)raw);
			}
		}

		if (args.containsKey("att")) {
			List<HashMap<String, Object>> atts = (List<HashMap<String, Object>>) args.get("att");
			for (HashMap<String, Object> att : atts) {
				try{
					NBTTagCompound compound = new NBTTagCompound();
				
					long least = (Long) att.get("UUIDLeast");
					
					long most = (Long) att.get("UUIDMost");
					setLong.invoke(compound, "UUIDLeast",least);
					setLong.invoke(compound, "UUIDMost", most);

					setString.invoke(compound, "AttributeName", att.get("AttributeName"));
					setString.invoke(compound, "Name", att.get("Name"));

					setDouble.invoke(compound, "Amount", att.get("Amount"));
					setInt.invoke(compound, "Operation", att.get("Operation"));
					result = addModifier(least,most,compound,result);
				} catch(Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		return result;
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
			HashMap<?,?> l = (HashMap<?,?>) attributeList.get(m);
			return !l.isEmpty();
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
			/*if(getTagMethod == null) getTagMethod = nmsItemStack.getClass().getMethod("getTag");
			Object tag = getTagMethod.invoke(nmsItemStack);*/

			return addModifier(least, most, compound, is);

		}catch(Throwable t){t.printStackTrace();}

		return null;
	}

	public static ItemStack addModifier(Long least, Long most, Object compound, ItemStack is){
		try {
			net.minecraft.server.v1_8_R3.ItemStack nmsItemStack = (net.minecraft.server.v1_8_R3.ItemStack) toNMSStack(is);
			Object tag = nmsItemStack.getTag();
			Object listNBT;

			//no tag means no ItemMeta, which in turn means the modifier does not exist
			if(tag == null){
				tag = compoundConstructor.newInstance();
				/*if(setTagMethod == null) setTagMethod = nmsItemStack.getClass().getMethod("setTag");
			setTagMethod.invoke(nmsItemStack, tag);*/
				nmsItemStack.setTag((net.minecraft.server.v1_8_R3.NBTTagCompound) tag);
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
			removeNBTModifier(listNBT, m.getUUID().getLeastSignificantBits(), m.getUUID().getMostSignificantBits());
			return toBukkitStack(nmsItemStack);

		}catch(Throwable t){t.printStackTrace();}	

		return null;
	}

	private static Object toNMSStack(ItemStack stack) 
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		/*Method convert = Class.forName(Bukkit.getItemFactory().getClass().getPackage().getName() + ".CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
		Object result = convert.invoke(null, stack);*/
		return CraftItemStack.asNMSCopy(stack);
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
	/*
	@SuppressWarnings("unchecked")
	public static Map<String, Object> serializeItem(ItemStack is) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		if (is == null) is = new ItemStack(Material.AIR);
		result.put("type", is.getType().name());

		if (is.getDurability() != 0) {
			result.put("damage", is.getDurability());
		}

		if (is.getAmount() != 1) {
			result.put("amount", is.getAmount());
		}

		ItemMeta meta = is.getItemMeta();
		if (!Bukkit.getItemFactory().equals(meta, null)) {
			result.put("meta", meta);
		}

		net.minecraft.server.v1_8_R1.NBTTagCompound tag = null;
		net.minecraft.server.v1_8_R1.ItemStack nms = null;
		try {
			nms = (net.minecraft.server.v1_8_R1.ItemStack) toNMSStack(is);
		} catch (Exception e) {
		}
		if (nms != null) { // is not air
			tag = nms.getTag();
			NBTTagList nbtlist = (NBTTagList) tag.get("AttributeModifiers");
			if (listField == null) {
				try {
					listField = nbtlist.getClass().getDeclaredField("list");
				} catch (Exception e) {

				}
				listField.setAccessible(true);
			}

			List<NBTBase> list = null;
			try {
				list = (List<NBTBase>) listField.get(nbtlist);
			} catch (Exception e) {

			}
			if (list != null) {
				for (NBTBase base : list) {
					base.toString();
				}
			}

		}

		return result;
	}

	public static ItemStack deserializeItem(Map<String, Object> args) {
		Material type = Material.getMaterial((String) args.get("type"));
		short damage = 0;
		int amount = 1;

		if (args.containsKey("damage")) {
			damage = ((Number) args.get("damage")).shortValue();
		}

		if (args.containsKey("amount")) {
			amount = (Integer) args.get("amount");
		}

		ItemStack result = new ItemStack(type, amount, damage);

		if (args.containsKey("meta")) {
			Object raw = args.get("meta");
			if (raw instanceof ItemMeta) {
				result.setItemMeta((ItemMeta) raw);
			}
		}

		if (args.containsKey("nbt")) {
			net.minecraft.server.v1_8_R1.ItemStack nms;
			try {
				nms = (net.minecraft.server.v1_8_R1.ItemStack) toNMSStack(result);
				net.minecraft.server.v1_8_R1.NBTTagCompound tag = nms.getTag();
				if (mapField == null) {
					try {
						mapField = net.minecraft.server.v1_8_R1.NBTTagCompound.class.getDeclaredField("map");
						mapField.setAccessible(true);
					} catch (NoSuchFieldException e) {}
				}
				mapField.set(tag, args.get("nbtmap"));
				nms.setTag(tag);
			} catch (Exception e) {
			}
		}

		return result;
	}*/

}
