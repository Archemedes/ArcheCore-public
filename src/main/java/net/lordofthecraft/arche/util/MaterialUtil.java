package net.lordofthecraft.arche.util;

import static org.bukkit.Material.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

@SuppressWarnings("unchecked")
/**
 * Set of utility methods that I want to use whenever materials need to be sorted into certain catagories
 * Sort materials by certain properties that Bukkit does not select for
 * Lots of work, boilerplate and will likely need major updating for most versions
 */
public class MaterialUtil { //Why do I do this to myself
	private static final Set<Material> HELMS = wrap(LEATHER_HELMET, GOLD_HELMET, IRON_HELMET, DIAMOND_HELMET, CHAINMAIL_HELMET);
	private static final Set<Material> CHESTPLATES = wrap(LEATHER_CHESTPLATE, CHAINMAIL_CHESTPLATE, IRON_CHESTPLATE, GOLD_CHESTPLATE, DIAMOND_CHESTPLATE);
	private static final Set<Material> LEGGINGS = wrap(LEATHER_LEGGINGS, CHAINMAIL_LEGGINGS, IRON_LEGGINGS, GOLD_LEGGINGS, DIAMOND_LEGGINGS);
	private static final Set<Material> BOOTS = wrap(LEATHER_BOOTS, CHAINMAIL_BOOTS, IRON_BOOTS, GOLD_BOOTS, DIAMOND_BOOTS);
	
	private static final Set<Material> PLATE_HELMS = wrap(GOLD_HELMET, IRON_HELMET, DIAMOND_HELMET);
	private static final Set<Material> PLATE_CHESTPLATES = wrap(IRON_CHESTPLATE, GOLD_CHESTPLATE, DIAMOND_CHESTPLATE);
	private static final Set<Material> PLATE_LEGGINGS = wrap(IRON_LEGGINGS, GOLD_LEGGINGS, DIAMOND_LEGGINGS);
	private static final Set<Material> PLATE_BOOTS = wrap(IRON_BOOTS, GOLD_BOOTS, DIAMOND_BOOTS);
	
	private static final Set<Material> ARMOR = wrap(HELMS, CHESTPLATES, LEGGINGS, BOOTS);
	private static final Set<Material> PLATE_ARMOR = wrap(PLATE_HELMS, PLATE_CHESTPLATES, PLATE_LEGGINGS, PLATE_BOOTS);
	private static final Set<Material> GOLD_ARMOR = wrap(GOLD_HELMET, GOLD_BOOTS, GOLD_LEGGINGS, GOLD_CHESTPLATE);
	
	private static final Set<Material> SPADES = wrap(WOOD_SPADE, STONE_SPADE, IRON_SPADE, GOLD_SPADE, DIAMOND_SPADE);
	private static final Set<Material> /*can't trust dem*/HOES = wrap(WOOD_HOE, STONE_HOE, IRON_HOE, GOLD_HOE, DIAMOND_HOE);
	private static final Set<Material> AXES = wrap(WOOD_AXE, STONE_AXE, IRON_AXE, GOLD_AXE, DIAMOND_AXE);
	private static final Set<Material> PICKS = wrap(WOOD_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLD_PICKAXE, DIAMOND_PICKAXE);
	private static final Set<Material> SWORDS = wrap(WOOD_SWORD, STONE_SWORD, IRON_SWORD, GOLD_SWORD, DIAMOND_SWORD);
	
	private static final Set<Material> METAL_SPADES = wrap(IRON_SPADE, GOLD_SPADE, DIAMOND_SPADE);
	private static final Set<Material> METAL_HOES = wrap(IRON_HOE, GOLD_HOE, DIAMOND_HOE);
	private static final Set<Material> METAL_AXES = wrap(IRON_AXE, GOLD_AXE, DIAMOND_AXE);
	private static final Set<Material> METAL_PICKS = wrap(IRON_PICKAXE, GOLD_PICKAXE, DIAMOND_PICKAXE);
	private static final Set<Material> METAL_SWORDS = wrap(IRON_SWORD, GOLD_SWORD, DIAMOND_SWORD);
	
	private static final Set<Material> TOOLS = wrap(SPADES, AXES, PICKS, HOES);
	private static final Set<Material> METAL_TOOLS = wrap(METAL_SPADES, METAL_AXES, METAL_PICKS, METAL_HOES);
	
	private static final Set<Material> HANDHELD = wrap(TOOLS, SWORDS);
	private static final Set<Material> METAL_HANDHELD = wrap(METAL_TOOLS, METAL_SWORDS);
	
	private static final Set<Material> GOLD_TOOLS = wrap(GOLD_AXE, GOLD_SPADE, GOLD_PICKAXE, GOLD_SPADE);
	private static final Set<Material> GOLD_HANDHELD = wrap(GOLD_AXE, GOLD_SPADE, GOLD_PICKAXE, GOLD_SPADE);
	
	private static final Set<Material> GOLD = wrap(GOLD_ARMOR, GOLD_HANDHELD);
	private static final Set<Material> METAL = wrap(METAL_HANDHELD, PLATE_ARMOR);
	private static final Set<Material> EQUIPMENT = wrap(ARMOR, HANDHELD);
	
	private static final Set<Material> MISC = wrap(FLINT_AND_STEEL, FISHING_ROD, CARROT_STICK, ELYTRA);
	
	private MaterialUtil() {}
	
	public static Set<Material> helms() { return HELMS; }
	public static Set<Material> chestplates() { return CHESTPLATES; }
	public static Set<Material> leggings() { return LEGGINGS; }
	public static Set<Material> boots() { return HELMS; }
	
	public static Set<Material> plateHelms() { return PLATE_HELMS; }
	public static Set<Material> plateChestplates() { return PLATE_CHESTPLATES; }
	public static Set<Material> plateLeggins() { return PLATE_LEGGINGS; }
	public static Set<Material> plateBoots() { return PLATE_BOOTS; }
	
	public static Set<Material> armor() { return ARMOR; }
	public static Set<Material> plateArmor() { return PLATE_ARMOR; }
	public static Set<Material> goldArmor() { return GOLD_ARMOR; }
	
	public static Set<Material> spades() { return SPADES; }
	public static Set<Material> hoes() { return HOES; }
	public static Set<Material> axes() { return AXES; }
	public static Set<Material> pickaxes() { return PICKS; }
	public static Set<Material> swords() { return SWORDS; }
	
	public static Set<Material> metalSpades() { return METAL_SPADES; }
	public static Set<Material> metalHoes() { return METAL_HOES; }
	public static Set<Material> metalAxes() { return METAL_AXES; }
	public static Set<Material> metalPickaxes() { return METAL_PICKS; }
	public static Set<Material> metalSwords() { return METAL_SWORDS; }
	
	public static Set<Material> tools() { return TOOLS; }
	public static Set<Material> goldTools() { return GOLD_TOOLS; }
	public static Set<Material> metalTools() { return METAL_TOOLS; }

	public static Set<Material> handhelds() { return HANDHELD; }
	public static Set<Material> goldHandhelds() { return GOLD_HANDHELD; }
	public static Set<Material> metalHandhelds() { return METAL_HANDHELD; }
	
	public static Set<Material> equipment() { return EQUIPMENT; }
	public static Set<Material> goldEquipment() { return GOLD; }
	public static Set<Material> metalEquipment() { return METAL; }
	
	public static Set<Material> misc() { return MISC; }
	public static Set<Material> bow() {return wrap(BOW); } //Convenience method
	
	//////////////////////////// End of Material equipment groupings ////////////////////////
	
	public static EquipmentSlot getLikelySlot(Material m){
		switch(m){
		case LEATHER_HELMET: case CHAINMAIL_HELMET: case IRON_HELMET: case GOLD_HELMET: case DIAMOND_HELMET:
		case SKULL_ITEM: case PUMPKIN:
			return EquipmentSlot.HEAD;
		case LEATHER_CHESTPLATE: case CHAINMAIL_CHESTPLATE: case IRON_CHESTPLATE:
		case GOLD_CHESTPLATE: case DIAMOND_CHESTPLATE: case ELYTRA:
			return EquipmentSlot.CHEST;
		case LEATHER_LEGGINGS: case CHAINMAIL_LEGGINGS: case IRON_LEGGINGS: case GOLD_LEGGINGS: case DIAMOND_LEGGINGS:
			return EquipmentSlot.LEGS;
		case LEATHER_BOOTS: case CHAINMAIL_BOOTS: case IRON_BOOTS: case GOLD_BOOTS: case DIAMOND_BOOTS:
			return EquipmentSlot.FEET;
		case SHIELD: case TOTEM:
			return EquipmentSlot.OFF_HAND;
		default: return EquipmentSlot.HAND;
		}
	}
	
	public static EquipmentSlot isArmor(Material m){
		switch(m){
		case LEATHER_HELMET: case CHAINMAIL_HELMET: case IRON_HELMET: case GOLD_HELMET: case DIAMOND_HELMET:
			return EquipmentSlot.HEAD;
		case LEATHER_CHESTPLATE: case CHAINMAIL_CHESTPLATE: case IRON_CHESTPLATE:
		case GOLD_CHESTPLATE: case DIAMOND_CHESTPLATE: case ELYTRA:
			return EquipmentSlot.CHEST;
		case LEATHER_LEGGINGS: case CHAINMAIL_LEGGINGS: case IRON_LEGGINGS: case GOLD_LEGGINGS: case DIAMOND_LEGGINGS:
			return EquipmentSlot.LEGS;
		case LEATHER_BOOTS: case CHAINMAIL_BOOTS: case IRON_BOOTS: case GOLD_BOOTS: case DIAMOND_BOOTS:
			return EquipmentSlot.FEET;
		default: return null;
		}
	}
	
	//////////////////////////// Some utility methods for list creation ///////////////////////////
	
	private static Set<Material> wrap(Material... mats) {
		EnumSet<Material> set = EnumSet.noneOf(Material.class);
		set.addAll(Arrays.asList(mats));
		return Collections.unmodifiableSet(set);
	}
	
	public static Set<Material> wrap(Set<Material>...lists){
		Set<Material> megaList = EnumSet.noneOf(Material.class);
		Arrays.stream(lists).forEach(megaList::addAll);
		return Collections.unmodifiableSet(megaList);
	}
}
