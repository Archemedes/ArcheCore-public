package net.lordofthecraft.arche.attributes.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier.Decay;
import net.md_5.bungee.api.ChatColor;

public class Decorator {
	private static final String TELANIR_DIVIDER = StringUtils
			.join( new ChatColor[]{ChatColor.AQUA,ChatColor.GOLD,ChatColor.BLUE,ChatColor.GOLD,ChatColor.GOLD});
	
	public static void showAttributes(ItemStack is) {
		ItemMeta meta = is.getItemMeta();
		
		if(meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) return;
		List<String> attributes = new ArrayList<>();
		List<String> rightclick_atts = new ArrayList<>();
		List<String> consumable_atts = new ArrayList<>();
		
		CustomTag tag = CustomTag.getFrom(is);
		for(Entry<String, String> e : tag.entrySet()) {
			String key = e.getKey();
			if(key.startsWith("na_")) {
				attributes.add(parseAttribute(key, e.getValue()));
			}else if(key.startsWith("nac_")) {
				consumable_atts.add(parseStoredAttribute(key, e.getValue()));
			}else if(key.startsWith("nar_")) {
				rightclick_atts.add(parseStoredAttribute(key, e.getValue()));
			}
		}
		
		final List<String> lore = meta.getLore() == null? new ArrayList<>() : meta.getLore();
		
		Iterator<String> iter = lore.iterator();
		boolean prune = false;
		while(iter.hasNext()) {
			String s = iter.next();
			if(!prune) {
				if(TELANIR_DIVIDER.equals(s)) {
					prune = true;
					iter.remove();
				}
			} else {
				iter.remove();
			}
		}
		
		if(!attributes.isEmpty()){
			lore.add(TELANIR_DIVIDER);
			lore.add(ChatColor.GRAY + "When equipped:");
			attributes.stream().distinct().forEach(lore::add);
		}
		
		if(!rightclick_atts.isEmpty()){
			lore.add(TELANIR_DIVIDER);
			lore.add(ChatColor.GRAY + "When activated:");
			rightclick_atts.forEach(lore::add);
		}
		
		if(!consumable_atts.isEmpty()){
			lore.add(TELANIR_DIVIDER);
			lore.add(ChatColor.GRAY + "When consumed:");
			consumable_atts.forEach(lore::add);
		}
		
		meta.setLore(lore);
		is.setItemMeta(meta);
	}
	
	private static String parseStoredAttribute(String key, String value) {
		StoredAttribute sa = StoredAttribute.fromTag(key, value);
		
		String beginning = beginning(sa);
		long ticks = sa.getTicks();
		
		if(sa.getDecayStrategy() != Decay.NEVER) {
			String time = null;
			long TICKS_PER_DAY = 1728000;
			long TICKS_PER_HOUR = 72000;
			long TICKS_PER_MINUTE = 1200;
			long TICKS_PER_SECOND = 20;
			if(ticks > 2*TICKS_PER_DAY) {
				time = Long.toString(ticks/TICKS_PER_DAY) + 'd' + Long.toString( (ticks%TICKS_PER_DAY)/TICKS_PER_HOUR ) + 'h';
			} else if (ticks > 3*TICKS_PER_HOUR) {
				time = Long.toString(ticks/TICKS_PER_HOUR) + 'h' + Long.toString( (ticks%TICKS_PER_HOUR)/TICKS_PER_MINUTE ) + 'm';
			} else {
				time = Long.toString(ticks/TICKS_PER_MINUTE) + ":" + Long.toString( (ticks%TICKS_PER_MINUTE)/TICKS_PER_SECOND );
			}
			
			return beginning + " (" + time + ")";
		} else return beginning;

	}
	
	private static String parseAttribute(String key, String value) {
		ItemAttribute ia = ItemAttribute.fromTag(key, value);
		
		return beginning(ia);
	}
	
	private static String beginning(TagAttribute ia) {
		return ' ' + ExtendedAttributeModifier.readablePercentage(ia.getModifier(), ia.getAttribute())
		+ ' ' + ia.getAttribute().getReadableName();
	}
	
}
