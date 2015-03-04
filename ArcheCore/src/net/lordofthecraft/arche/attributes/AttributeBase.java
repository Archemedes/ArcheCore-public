package net.lordofthecraft.arche.attributes;

import java.util.logging.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.*;
import java.lang.reflect.*;

public class AttributeBase
{
    private static Constructor<?> maker;
    private static Method getHandle;
    private static Method fetchAll;
    private static Method remover;
    private static Method adder;
    private static Method grabber;
    
    public static void clearModifiers(final LivingEntity e, final AttributeType type) {
        try {
            final Object nms = getNMSEntity(e, type);
            if (nms == null) {
                return;
            }
            final Object attInstance = getAttributeInstance(type, nms);
            if (AttributeBase.fetchAll == null) {
                AttributeBase.fetchAll = attInstance.getClass().getMethod("c", (Class<?>[])new Class[0]);
            }
            final Set<?> modifiers = (Set<?>)AttributeBase.fetchAll.invoke(attInstance, new Object[0]);
            for (final Object modifier : modifiers) {
                removeModifier(attInstance, modifier);
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static void addModifier(final LivingEntity e, final AttributeModifier m) {
        try {
            final Object nmsEntity = getNMSEntity(e, m.getAttribute());
            if (nmsEntity == null) {
                return;
            }
            final Package pack = nmsEntity.getClass().getPackage();
            final Object attModifier = createModifier(m, pack);
            final Object attInstance = getAttributeInstance(m.getAttribute(), nmsEntity);
            removeModifier(attInstance, attModifier);
            if (AttributeBase.adder == null) {
                for (final Method met : attInstance.getClass().getDeclaredMethods()) {
                    if (met.getName().equals("b") && met.getReturnType().equals(Void.TYPE)) {
                        AttributeBase.adder = met;
                        break;
                    }
                }
            }
            AttributeBase.adder.invoke(attInstance, attModifier);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static void removeModifier(final LivingEntity e, final AttributeModifier m) {
        try {
            final Object nmsEntity = getNMSEntity(e, m.getAttribute());
            if (nmsEntity == null) {
                return;
            }
            final Package pack = nmsEntity.getClass().getPackage();
            final Object attModifier = createModifier(m, pack);
            final Object attInstance = getAttributeInstance(m.getAttribute(), nmsEntity);
            removeModifier(attInstance, attModifier);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private static Object getNMSEntity(final LivingEntity e, final AttributeType type) {
        if (type == AttributeType.ZOMBIE_REINFORCEMENTS && !(e instanceof Zombie)) {
            Logger.getLogger("ArcheCore").warning("[Attr] non-applicable modifier 'zombie reinforcements' to non-zombie entity");
            return null;
        }
        if (type == AttributeType.HORSE_JUMPSTRENGTH && !(e instanceof Horse)) {
            Logger.getLogger("ArcheCore").warning("[Attr] non-applicable modifier 'horse jumpstrength' to non-horse entity");
            return null;
        }
        try {
            if (AttributeBase.getHandle == null) {
                AttributeBase.getHandle = e.getClass().getMethod("getHandle", (Class<?>[])new Class[0]);
            }
            return AttributeBase.getHandle.invoke(e, new Object[0]);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    
    private static void removeModifier(final Object attInstance, final Object attModifier) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (AttributeBase.remover == null) {
            for (final Method met : attInstance.getClass().getDeclaredMethods()) {
                if (met.getName().equals("c") && met.getReturnType().equals(Void.TYPE)) {
                    AttributeBase.remover = met;
                    break;
                }
            }
        }
        AttributeBase.remover.invoke(attInstance, attModifier);
    }
    
    private static Object createModifier(final AttributeModifier m, final Package pack) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        if (AttributeBase.maker == null) {
            AttributeBase.maker = Class.forName(pack.getName() + "." + "AttributeModifier").getConstructor(UUID.class, String.class, Double.TYPE, Integer.TYPE);
        }
        final Object attModifier = AttributeBase.maker.newInstance(m.getUUID(), m.getName(), m.getValue(), m.getOperation());
        return attModifier;
    }
    
    private static Object getAttributeInstance(final AttributeType at, final Object nmsEntity) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final Package pack = nmsEntity.getClass().getPackage();
        final Field f = (at == AttributeType.ZOMBIE_REINFORCEMENTS) ? nmsEntity.getClass().getField("b") : ((at == AttributeType.HORSE_JUMPSTRENGTH) ? nmsEntity.getClass().getField("attributeJumpStrength") : Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + "GenericAttributes").getField("" + at));
        if (at == AttributeType.ZOMBIE_REINFORCEMENTS || at == AttributeType.HORSE_JUMPSTRENGTH) {
            f.setAccessible(true);
        }
        if (AttributeBase.grabber == null) {
            final Class<?> interf = Class.forName(pack.getName() + "." + "IAttribute");
            AttributeBase.grabber = nmsEntity.getClass().getMethod("getAttributeInstance", interf);
        }
        final Object attInstance = AttributeBase.grabber.invoke(nmsEntity, f.get(null));
        return attInstance;
    }
    
    static {
        AttributeBase.maker = null;
        AttributeBase.getHandle = null;
        AttributeBase.fetchAll = null;
        AttributeBase.remover = null;
        AttributeBase.adder = null;
        AttributeBase.grabber = null;
    }
}
