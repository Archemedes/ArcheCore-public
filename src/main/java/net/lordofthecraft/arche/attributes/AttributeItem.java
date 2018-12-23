package net.lordofthecraft.arche.attributes;

import java.util.Iterator;
import java.util.Map;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;

/**
 * Static class containing the methods for applying an AttributeModifier to an item.
 */
public class AttributeItem {
	private AttributeItem() { }
	
	/**
	 * Check if an item has any AttributeModifiers applied to it
	 * @param is Item to check
	 * @return if AttributeModifiers were found on the ItemMeta
	 */
	public static boolean hasAttributes(ItemStack is){
		NbtCompound tag = (NbtCompound) NbtFactory.fromItemTag(is);
		if(tag.containsKey("AttributeModifiers")) {
			NbtList<Map<String, NbtBase<?>>> attList = tag.getList("AttributeModifiers");
			return attList.size() > 0;
		} else return false;
	}
	
	/**
	 * Add a modifier to an item
	 * @param att the Minecraft attribute to modify
	 * @param m the AttributeModifier to invoke
	 * @param slot the equipment slot to apply to, can be null.
	 * @param is The base ItemStack to apply a modifier to
	 * @return The ItemStack with the modifier applied 
	 */
	public static ItemStack addModifier(Attribute att, AttributeModifier m, EquipmentSlot slot, ItemStack is){
		//Object compound = compoundConstructor().newInstance();
		NbtCompound compound = NbtFactory.ofCompound("");

		//Setting the compound with the required values from the Wrapped Modifier.
		long least = m.getUniqueId().getLeastSignificantBits();
		long most  = m.getUniqueId().getMostSignificantBits();
		compound.put("UUIDLeast", least);
		compound.put("UUIDMost", most);

		compound.put("AttributeName", getInternalAttributeName(att));
		compound.put("Name", m.getName());

		compound.put("Amount", m.getAmount());
		compound.put("Operation", m.getOperation().ordinal()); //unsafe but probably works
		if (slot != null) {
			compound.put("Slot", getInternalName(slot));
		}

		ItemStack craft = MinecraftReflection.getBukkitItemStack(is);
		NbtCompound tag = (NbtCompound) NbtFactory.fromItemTag(craft);

		NbtList<Map<String, NbtBase<?>>> attList;
		if(tag.containsKey("AttributeModifiers")) {
			attList = tag.getList("AttributeModifiers");
			removeDuplicates(attList, m);
			attList.add(compound);
		} else {
			attList = NbtFactory.ofList("AttributeModifiers");
			attList.add(compound);
			tag.put("AttributeModifiers", attList);
		}
		return craft;
	}

	/**
	 * Removes a modifier with a certain UUID for a certain Attribute from an ItemStack.
	 * @param m The modifier to remove
	 * @param is The ItemStack to remove the modifier from.
	 * @return The ItemStack with the modifier removed
	 */
	public static ItemStack removeModifier(AttributeModifier m, ItemStack is){
		ItemStack craft = MinecraftReflection.getBukkitItemStack(is);
		NbtCompound tag = (NbtCompound) NbtFactory.fromItemTag(craft);

		//No AttributeModifier list was found --> nothing to remove
		if(!tag.containsKey("AttributeModifiers")) return is;

		NbtList<Map<String, NbtBase<?>>> attList = tag.getList("AttributeModifiers");

		try {
			removeDuplicates(attList, m);
		} catch (ClassCastException e) {
			CoreLog.warning("Item: " + is.toString());
		}
		return craft;
	}

	private static void removeDuplicates(NbtList<Map<String, NbtBase<?>>> attList, AttributeModifier m) {
		Iterator<Map<String, NbtBase<?>>> iter = attList.iterator();
		long least = m.getUniqueId().getLeastSignificantBits();
		long most = m.getUniqueId().getMostSignificantBits();
		while(iter.hasNext()) {
			Map<String, NbtBase<?>> att = iter.next();
			if (att.get("UUIDLeast").getValue() instanceof Integer || att.get("UUIDMost").getValue() instanceof Integer) {
				iter.remove();
				CoreLog.warning("UUID for attribute modifier marked as Integer instead of Long");
				CoreLog.warning("AttributeName: " + att.get("AttributeName").getValue());
				CoreLog.warning("Name: " + att.get("Name").getValue());

				throw new ClassCastException();
			}

			// Despite the error in IntelliJ. This actually compiles fine.
			// Eclipse - 1  IntelliJ - 0
			long wrappedLeast = (Long) att.get("UUIDLeast").getValue();
			long wrappedMost = (Long) att.get("UUIDMost").getValue();
			if(wrappedLeast == least && wrappedMost == most) {
				iter.remove();
				return;
			}
		}
	}
	
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
	
	private static String getInternalName(EquipmentSlot slot) {
		switch(slot) {
		case CHEST: return "chest";
		case FEET: return "feet";
		case HAND: return "mainhand";
		case HEAD: return "head";
		case LEGS: return "legs";
		case OFF_HAND: return "offhand";
		default:
			throw new UnsupportedOperationException();
		
		}
	}
}
