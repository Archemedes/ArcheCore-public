package net.lordofthecraft.arche.attributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

/**
 * Static class containing the methods for applying an AttributeModifier to an entity.
 */
public class AttributeBase {
	private static Constructor<?> maker = null;
	
	private static Method fetchAll = null;
	private static Method remover = null;
	private static Method adder   = null;
	private static Method grabber = null;
	
	//Ok
	public static void clearModifiers(LivingEntity e, AttributeType type){
		try{
			Object nms = getNMSEntity(e, type);
			if(nms == null) return;

			Object attInstance = getAttributeInstance(type, nms);
			if(fetchAll == null) fetchAll = attInstance.getClass().getMethod("c");
			
			Set<?> modifiers = (Set<?>) fetchAll.invoke(attInstance);
			for(Object modifier : modifiers){
				removeModifier(attInstance, modifier);
			}
			
		}catch(Throwable t){t.printStackTrace();}
		
	}
	
	/**
	 * Add an AttributeModifier to a mob.
	 * @param e The LivingEntity to modify
	 * @param m The modifier to apply.
	 */
	public static void addModifier(LivingEntity e, AttributeModifier m){
		try {
			Object nmsEntity = getNMSEntity(e, m.getAttribute());
			if(nmsEntity == null) return;
			Package pack = nmsEntity.getClass().getPackage();
			Object attModifier = createModifier(m,pack);
			
			Object attInstance = getAttributeInstance(m.getAttribute(), nmsEntity);
			
			removeModifier(attInstance, attModifier);
			
			//Adds the new modifier we just created
			if(adder == null){
				for(Method met : attInstance.getClass().getDeclaredMethods()){
					if(met.getName().equals("b") && met.getReturnType().equals(Void.TYPE)){
						adder = met;
						break;
					}
				}
			}
			
			adder.invoke(attInstance, attModifier);
			
		} catch (Throwable t){t.printStackTrace();}
	}
	
	/**
	 * Removes the modifier with a certain UUID for a certain attribute from an Entity.
	 * @param e The LivingEntity to affect
	 * @param m The modifier to remove
	 */
	public static void removeModifier(LivingEntity e, AttributeModifier m){
		try {
			Object nmsEntity = getNMSEntity(e, m.getAttribute());
			if(nmsEntity == null) return;
			
			Package pack = nmsEntity.getClass().getPackage();
			Object attModifier = createModifier(m,pack);
			
			Object attInstance = getAttributeInstance(m.getAttribute(), nmsEntity);
			removeModifier(attInstance, attModifier);
			
		} catch (Throwable t){t.printStackTrace();}
	}
	
	//Ok
	private static Object getNMSEntity(LivingEntity e, AttributeType type){
		if(type == AttributeType.ZOMBIE_REINFORCEMENTS && !(e instanceof Zombie)){
			Logger.getLogger("ArcheCore").warning("[Attr] non-applicable modifier 'zombie reinforcements' to non-zombie entity");
			return null;
		}
		
		if(type == AttributeType.HORSE_JUMPSTRENGTH && !(e instanceof Horse)){
			Logger.getLogger("ArcheCore").warning("[Attr] non-applicable modifier 'horse jumpstrength' to non-horse entity");
			return null;
		}
		
		try{
			Method getHandle = e.getClass().getMethod("getHandle");
			return getHandle.invoke(e); 
		}catch(Throwable t){t.printStackTrace();}
		
		return null;
	}
	
	
	private static void removeModifier(Object attInstance, Object attModifier) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		if(remover == null){
			for(Method met : attInstance.getClass().getDeclaredMethods()){
				if(met.getName().equals("c") && met.getReturnType().equals(Void.TYPE)){
					remover = met;
					break;
				}
			}
		}
		
		remover.invoke(attInstance, attModifier);
	}
	
	//Ok
	private static Object createModifier(AttributeModifier m, Package pack) 
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException{
		
			if(maker == null) maker = Class.forName(pack.getName() + "." + "AttributeModifier").getConstructor(UUID.class, String.class, double.class, int.class);
			Object attModifier = maker.newInstance(m.getUUID(),m.getName(),m.getValue(),m.getOperation());
			return attModifier;
	}
	
	//Ok
	private static Object getAttributeInstance(AttributeType at, Object nmsEntity) 
			throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException{
		Package pack = nmsEntity.getClass().getPackage();
		//AttributeType at = m.getAttribute();
		Field f = 
				at == AttributeType.ZOMBIE_REINFORCEMENTS? nmsEntity.getClass().getField("b"): 
				at == AttributeType.HORSE_JUMPSTRENGTH? nmsEntity.getClass().getField("attributeJumpStrength"): 
				Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + "GenericAttributes").getField(""+at);
		if(at == AttributeType.ZOMBIE_REINFORCEMENTS || at == AttributeType.HORSE_JUMPSTRENGTH) f.setAccessible(true);
		
		if(grabber == null){
			Class<?> interf = Class.forName(pack.getName() + "." + "IAttribute");
			grabber = nmsEntity.getClass().getMethod("getAttributeInstance",interf);
		}
		
		Object attInstance = grabber.invoke(nmsEntity, f.get(null));
		
		return attInstance;
	}
}
