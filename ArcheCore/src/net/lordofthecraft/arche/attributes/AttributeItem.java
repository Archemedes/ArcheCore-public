package net.lordofthecraft.arche.attributes;

import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.inventory.meta.*;
import java.lang.reflect.*;
import java.util.*;

public class AttributeItem
{
    private static final String PATH;
    private static Class<?> NBTBase;
    private static Class<?> NBTTagCompound;
    private static Constructor<?> compoundConstructor;
    private static Method getLong;
    private static Method setString;
    private static Method setLong;
    private static Method setInt;
    private static Method setDouble;
    private static Method hasKey;
    private static Method hasAttributes;
    
    public static boolean hasAttributes(final ItemStack is) {
        try {
            final ItemMeta m = is.getItemMeta();
            if (AttributeItem.hasAttributes == null) {
                (AttributeItem.hasAttributes = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".inventory.CraftMetaItem").getDeclaredMethod("hasAttributes", (Class<?>[])new Class[0])).setAccessible(true);
            }
            return (boolean)AttributeItem.hasAttributes.invoke(m, new Object[0]);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    public static ItemStack addModifier(final AttributeModifier m, final ItemStack is) {
        try {
            final Object nmsItemStack = toNMSStack(is);
            final Object compound = AttributeItem.compoundConstructor.newInstance(new Object[0]);
            final long least = m.getUUID().getLeastSignificantBits();
            final long most = m.getUUID().getMostSignificantBits();
            AttributeItem.setLong.invoke(compound, "UUIDLeast", least);
            AttributeItem.setLong.invoke(compound, "UUIDMost", most);
            AttributeItem.setString.invoke(compound, "AttributeName", m.getAttribute().getName());
            AttributeItem.setString.invoke(compound, "Name", m.getName());
            AttributeItem.setDouble.invoke(compound, "Amount", m.getValue());
            AttributeItem.setInt.invoke(compound, "Operation", m.getOperation());
            final Field tagField = nmsItemStack.getClass().getField("tag");
            Object tag = tagField.get(nmsItemStack);
            Object listNBT;
            if (tag == null) {
                tag = AttributeItem.compoundConstructor.newInstance(new Object[0]);
                tagField.set(nmsItemStack, tag);
                listNBT = createAttributeModifierList(tag);
            }
            else if (!NBTCompoundHasKey(tag, "AttributeModifiers")) {
                listNBT = createAttributeModifierList(tag);
            }
            else {
                listNBT = AttributeItem.NBTTagCompound.getMethod("get", String.class).invoke(tag, "AttributeModifiers");
                removeNBTModifier(listNBT, least, most);
            }
            listNBT.getClass().getMethod("add", AttributeItem.NBTBase).invoke(listNBT, compound);
            return toBukkitStack(nmsItemStack);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    
    private static Object createAttributeModifierList(final Object tag) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        final Class<?> clazz = Class.forName(AttributeItem.PATH + "NBTTagList");
        final Object list = clazz.getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]);
        AttributeItem.NBTTagCompound.getMethod("set", String.class, AttributeItem.NBTBase).invoke(tag, "AttributeModifiers", list);
        return list;
    }
    
    public static ItemStack removeModifier(final AttributeModifier m, final ItemStack is) {
        try {
            final Object nmsItemStack = toNMSStack(is);
            final Object tag = nmsItemStack.getClass().getField("tag").get(nmsItemStack);
            if (tag == null) {
                return is;
            }
            if (!NBTCompoundHasKey(tag, "AttributeModifiers")) {
                return is;
            }
            final Object listNBT = AttributeItem.NBTTagCompound.getMethod("get", String.class).invoke(tag, "AttributeModifiers");
            removeNBTModifier(listNBT, m.getUUID().getLeastSignificantBits(), m.getUUID().getMostSignificantBits());
            return toBukkitStack(nmsItemStack);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    
    private static Object toNMSStack(final ItemStack stack) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Method convert = Class.forName(Bukkit.getItemFactory().getClass().getPackage().getName() + ".CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
        final Object result = convert.invoke(null, stack);
        return result;
    }
    
    private static ItemStack toBukkitStack(final Object nmsItemStack) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Method convert = Class.forName(Bukkit.getItemFactory().getClass().getPackage().getName() + ".CraftItemStack").getMethod("asCraftMirror", nmsItemStack.getClass());
        final ItemStack result = (ItemStack)convert.invoke(null, nmsItemStack);
        return result;
    }
    
    private static boolean NBTCompoundHasKey(final Object compound, final String key) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (boolean)AttributeItem.hasKey.invoke(compound, key);
    }
    
    private static void removeNBTModifier(final Object NBTList, final long least, final long most) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException {
        final Field f = NBTList.getClass().getDeclaredField("list");
        f.setAccessible(true);
        final List<?> list = (List<?>)f.get(NBTList);
        for (final Object compound : list) {
            final long nbtLeast = (long)AttributeItem.getLong.invoke(compound, "UUIDLeast");
            final long nbtMost = (long)AttributeItem.getLong.invoke(compound, "UUIDMost");
            if (least == nbtLeast && most == nbtMost) {
                list.remove(compound);
                break;
            }
        }
        f.setAccessible(false);
    }
    
    static {
        PATH = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        try {
            AttributeItem.NBTBase = Class.forName(AttributeItem.PATH + "NBTBase");
            AttributeItem.NBTTagCompound = Class.forName(AttributeItem.PATH + "NBTTagCompound");
            AttributeItem.compoundConstructor = AttributeItem.NBTTagCompound.getConstructor((Class<?>[])new Class[0]);
            AttributeItem.hasKey = AttributeItem.NBTTagCompound.getMethod("hasKey", String.class);
            AttributeItem.getLong = AttributeItem.NBTTagCompound.getMethod("getLong", String.class);
            AttributeItem.setString = AttributeItem.NBTTagCompound.getMethod("setString", String.class, String.class);
            AttributeItem.setLong = AttributeItem.NBTTagCompound.getMethod("setLong", String.class, Long.TYPE);
            AttributeItem.setInt = AttributeItem.NBTTagCompound.getMethod("setInt", String.class, Integer.TYPE);
            AttributeItem.setDouble = AttributeItem.NBTTagCompound.getMethod("setDouble", String.class, Double.TYPE);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
