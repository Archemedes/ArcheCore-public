package net.lordofthecraft.arche;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;
import net.lordofthecraft.arche.persona.PersonaSkin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.logging.Logger;

public class ArcheBeacon {
	public static final String BEACON_HEADER = ChatColor.AQUA + "" + ChatColor.BOLD + "Your settings:";

	private ArcheBeacon() {
	}
	
	public static void openBeacon(Player p){
		if(!CreationDialog.mayConverse(p))
			return;

		Logger log = ArcheCore.getPlugin().getLogger();
		//log.info("ArcheBeacon opened");
		ArchePersonaHandler handler = ArchePersonaHandler.getInstance();
		if(handler.mayUse(p) || (handler.countPersonas(p) == 0 && !p.hasPermission("archecore.exempt"))){
			ArchePersona[] prs = handler.getAllPersonas(p);
			if(prs == null){
				log.severe(" [Beacon] Player walking around without registered Personas File!");
				return;
			}

			int count = 0;
			int current = -1;
			for(int i = 0; i < prs.length; i++){
				if(prs[i] != null){
					count++;
					if(prs[i].isCurrent())
						current = i;
				}
			}

			Inventory inv = Bukkit.createInventory(p, 9, BEACON_HEADER);
			ItemStack is;
			final String r = ChatColor.RESET.toString();
			final String g = ChatColor.DARK_GRAY.toString();

			if(current < 0){
				if(count == 0) log.warning("[Beacon] Zero personas for: " + p.getName());
				else log.warning("[Beacon] no current persona for: " + p.getName());
			}
			else{
				boolean xp = prs[current].getXPGain();

				is = new ItemStack(xp? Material.EYE_OF_ENDER : Material.ENDER_PEARL);
				String xpGain = xp ? ChatColor.GREEN + "" + ChatColor.ITALIC + "ON" :
					ChatColor.DARK_RED + "" + ChatColor.ITALIC + "OFF";

				buildItem(is, r + "XP gain: " + xpGain, g + "Click to toggle");
				inv.setItem(0, is);

				is = new ItemStack(Material.ENCHANTED_BOOK);
				buildItem(is, r + "Read Skill Tomes", g + "Consume all skill tomes", g + "in your Persona's inventory.");
				inv.setItem(3, is);
			}

			//Everybody gets these buttons
			is = new ItemStack(Material.BOOK);
			buildItem(is, r + "Help", g + "Receive help on", g + "various topics.");
			inv.setItem(1, is);

			is = new ItemStack(Material.ENDER_CHEST);
			buildItem(is, r + "Ender Chest", g + "Open your Ender Chest");
			inv.setItem(2, is);


			int max = handler.getAllowedPersonas(p);

			is = new ItemStack(Material.REDSTONE_COMPARATOR);
			buildItem(is, r + "Your Personas to the right", ChatColor.GRAY + "Max Personas: " + ChatColor.LIGHT_PURPLE + max , g + "Left Click to select", g + "SHIFT + Left Click: Create new", g + "SHIFT + Right Click: Permakill Persona", ChatColor.GRAY + "Click me for more info.");
			inv.setItem(4, is);

			//Buttons for switching Personas
			boolean mayMakeMore = count < max;

			for(int i = 0; i < 4; i++){
				ArchePersona a = prs[i];
				if(a == null){
					is = new ItemStack(Material.SKULL_ITEM, 1, mayMakeMore ? (short)0 : (short)1);
					if(mayMakeMore)
						buildItem(is, "Empty Persona", ChatColor.GREEN+""+ChatColor.ITALIC + "Click here", g + "To create a new Persona");
					else
						buildItem(is, "Locked Slot", g + "Please " + ChatColor.GREEN+""+ChatColor.ITALIC + "Purchase", g + "You may purchase more personas in the store");
				} else {
				    PersonaSkin skin = a.getSkin();
				    // Give a normal human head if skin is null.
				    is = skin != null ? a.getSkin().getHeadItem() : new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

					String name = ChatColor.YELLOW + "" + ChatColor.ITALIC + a.getName();
					String gender = a.getGender() == null? "" : a.getGender();
					String desc = ChatColor.GRAY + a.getRaceString() + " " + gender + ", " + a.getAge();
					String d2 = (i == current? ChatColor.DARK_GREEN + "Selected!": ChatColor.GREEN + "Click to select");
					buildItem(is, name, desc, d2);
				}

				//Always do this
				inv.setItem(5+i, is);
			}


			p.openInventory(inv);
		} else {
			p.sendMessage(ChatColor.RED + "You may not yet access this.");
		}

	}
	
	private static ItemStack buildItem(ItemStack is, String title, String... lore){
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(title);
		List<String> loreList = Lists.newLinkedList();

		for(String x : lore) loreList.add(ChatColor.DARK_GRAY + x);
		meta.setLore(loreList);

		is.setItemMeta(meta);
		return is;
	}
	
}
