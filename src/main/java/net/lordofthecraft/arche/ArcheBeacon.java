package net.lordofthecraft.arche;

import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.event.persona.PersonaWhoisEvent;
import net.lordofthecraft.arche.executables.OpenEnderRunnable;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.util.ItemUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ArcheBeacon {
	//public static final String BEACON_HEADER = ChatColor.AQUA + "" + ChatColor.BOLD + "Your settings:";
	public static class ArcheBeaconHolder implements InventoryHolder{
		private Inventory inv;
		public Inventory getInventory() {return inv;}
		private void setInventory(Inventory inv) {this.inv = inv;}
	}
	
	
	@SuppressWarnings("unchecked")
	private static final BiFunction<ClickType, Persona, ItemStack>[] FUNCTIONS = new BiFunction[8];
	
	
	private ArcheBeacon() {}
	
	static {
		FUNCTIONS[0] = (click,pers) -> { //Ender chest button
			if(ArcheCore.getControls().showEnderchestInMenu()) {
				if(click != null) { //open ender chest
					Player p = pers.getPlayer();
					closeAnd(p, () -> {
                        if (p.hasPermission("archecore.enderchest") && pers.getTimePlayed() >= 200)
                            OpenEnderRunnable.begin(pers);
						else 
							p.sendMessage(ChatColor.RED + "You do not have access to your Ender Chest.");
					});
				}
				ItemStack is = new ItemStack(Material.ENDER_CHEST);
				return ItemUtil.decorate(is, ChatColor.RESET + "Ender Chest", ChatColor.GRAY + "Open your Ender Chest");
			} else {
				return new ItemStack(Material.AIR); //Do not draw an item now
			}
		};
		
		FUNCTIONS[1] = (click,pers) -> { //List of persona att mods
			if(click == null) { //Make the icon
				ItemStack icon = new ItemStack(Material.GOLDEN_APPLE);
				ItemMeta m = icon.getItemMeta();
				m.setDisplayName(ChatColor.AQUA + "Persona Modifiers:");
				
				List<String> lore = new ArrayList<>();
				pers.attributes().getExistingInstances().stream().forEach(aa ->
				pers.attributes().getInstance(aa).getModifiers().stream()
				.map(ExtendedAttributeModifier.class::cast)
				.filter(mod -> StringUtils.isEmpty(mod.getName()) || !mod.getName().startsWith("/"))
				.forEach(mod->{
					String modName = mod.getName();
					boolean isCommented = StringUtils.isEmpty(modName) || modName.startsWith("#");
					lore.add(	mod.asReadablePercentage(aa) + ' ' + aa.getReadableName() + 
							(isCommented? "" : ( " " + ChatColor.GRAY + "" + ChatColor.ITALIC + '(' + mod.getName() + ')'))  
							);
				})
						);
				lore.add(ChatColor.GRAY + "Click to learn about attributes");
				m.setLore(lore);
				icon.setItemMeta(m);
				return icon;
			} else { //Close inv and give help on attributes
				Player p = pers.getPlayer();
				closeAnd(p, ()->HelpDesk.getInstance().outputHelp("Attributes", p));
				return null; //
			}
		};
		
		
	}

	
	/**
	 * By default, claiming a slot overrides any icon originally there. If you want to avoid this, call this function first
	 * Slot 1 and 2 are used by ArcheCore by default
	 * @param index slot on the menu to check, between 1 and 8
	 * @return whether or not an icon is already in the asked for slot
	 */
	public static boolean isSlotTaken(int index) {
		if(index == 0 || index > 8) return true; //Help slot and persona slots
		return FUNCTIONS[index-1] != null;
	}
	
	/**
	 * @param index slot on the menu to check, between 1 and 8
	 * @return The BiFunction coupled to this slot, else null
	 */
	public static BiFunction<ClickType, Persona, ItemStack> getFunction(int index){
		return FUNCTIONS[index-1];
	}
	
	/**
	 * Claim a slot in the ArcheCore "beacon menu". The top row of slots (apart from slot 0, which is reserved for help)
	 * can be used by your plugin if you wish for some super-critical "main menu" actions to be performed by players
	 * Note that the provided function must return the Icon the menu will draw. It will provide the ClickType 'null' when the menu is first created
	 * @param index The slot in which to place your executable icon.
	 * @param bif Function to execute when menu is created or slot is clicked.
	 */
	public static void claimSlot(int index, BiFunction<ClickType, Persona, ItemStack> bif) {
		FUNCTIONS[index-1] = bif;
	}
	
	
	private static void closeAnd(Player p, Runnable actionToTake) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()-> {
			p.closeInventory();
			actionToTake.run();
		});
	}
	
	public static void openBeacon(Player p){
		if(!CreationDialog.mayConverse(p))
			return;

		Logger log = ArcheCore.getPlugin().getLogger();
		if(ArcheCore.isDebugging()) log.info("ArcheBeacon opened by " + p.getName());
		ArchePersonaHandler handler = ArchePersonaHandler.getInstance();
		if(handler.mayUse(p) || (handler.countPersonas(p) == 0 && !p.hasPermission("archecore.exempt"))){
			ArchePersona[] prs = handler.getAllPersonas(p);
			if(prs == null){
				log.severe(" [Beacon] Player walking around without registered Personas File!");
				return;
			}

			int highestUsed = 0;
			int firstFree = -1;
			int count = 0;
			int current = -1;
			for(int i = 0; i < prs.length; i++){
				if(prs[i] != null){
					highestUsed = i;
					count++;
					if(prs[i].isCurrent())
						current = i;
				} else if(firstFree < 0){
					firstFree = i;
				}
			}


			int max = handler.getAllowedPersonas(p);
			int absmax = ArcheCore.getControls().personaSlots();
			int requiredSize = requiredSize(highestUsed, max, absmax, firstFree);
			ArcheBeaconHolder hh = new ArcheBeaconHolder();
			Inventory inv = Bukkit.createInventory(hh, 9*(2 + requiredSize/9 ), ArcheCore.getControls().getCalendar().toPrettyString());
			hh.setInventory(inv);
			
			
			ItemStack is;
			final String r = ChatColor.RESET.toString();
			final String g = ChatColor.DARK_GRAY.toString();

			if(current < 0){
				if(count == 0) log.warning("[Beacon] Zero personas for: " + p.getName());
				else log.warning("[Beacon] no current persona for: " + p.getName());
			} else if(ArcheCore.getControls().showEnderchestInMenu()) {
				is = new ItemStack(Material.ENDER_CHEST);
				ItemUtil.decorate(is, r + "Ender Chest", g + "Open your Ender Chest");
				inv.setItem(1, is);
			}	


            //Everybody gets these buttons
			is = new ItemStack(Material.BOOK);
			ItemUtil.decorate(is, r + "Help", g + "Receive help on", g + "various topics.");
			inv.setItem(0, is);

			is = new ItemStack(Material.REDSTONE_COMPARATOR);
			ItemUtil.decorate(is, r + "Your Personas to the right", ChatColor.GRAY + "Max Personas: " + ChatColor.LIGHT_PURPLE + max,
                    g + "Left Click to select", (ArcheCore.getControls().canCreatePersonas() ? g + "SHIFT + Left Click: Create new" : ChatColor.RED + "Creating new personas is disabled on this server"),
                    g + "SHIFT + Right Click: Permakill Persona",
                    ChatColor.GRAY + "Click me for more info.");
            inv.setItem(9, is); //First item on second row

            //populate the top row using the FUNCTIONS array
            if(current >= 0) {
            	for(int i = 0; i < FUNCTIONS.length; i++) {
            		if(FUNCTIONS[i] != null) try {
            			is = FUNCTIONS[i].apply(null, prs[current]);
            			if(ItemUtil.exists(is)) inv.setItem(i+1, is);
            		} catch(Throwable t) {
            			t.printStackTrace();
            			continue;
            		}
            	}
            }
            
			//Buttons for switching Personas
            int freeSlots = ArcheCore.getControls().canCreatePersonas()? max - count : 0;
            boolean mayMakeMore = freeSlots > 0;
            
			for(int i = 0; i < requiredSize; i++){
				ArchePersona a = prs[i];
				if(a == null){
					if(mayMakeMore) {
						is = new ItemStack(Material.SKULL_ITEM, 1, (short) 0);
						ItemUtil.decorate(is, "Empty Persona", ChatColor.GREEN+""+ChatColor.ITALIC + "Click here", g + "To create a new Persona");
                        mayMakeMore = false;
                        freeSlots--;
                    } else if (freeSlots-- > 0) {
                        is = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
                        ItemUtil.decorate(is, "Empty Persona", ChatColor.GREEN+""+ChatColor.ITALIC + "Slot is available", g + "");
					}else {
						is = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
						ItemUtil.decorate(is, "Locked Slot", g + "Please " + ChatColor.GREEN+""+ChatColor.ITALIC + "Purchase", g + "You may purchase more personas in the store");
                    }
                } else {
                    ArcheSkin sk = a.getSkin();
                    is = (sk != null ? sk.getHeadItem() : new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
                    String name = ChatColor.YELLOW + "" + ChatColor.ITALIC + a.getName();
					String gender = a.getGender() == null? "" : a.getGender();
                    String desc = ChatColor.GRAY + a.getRaceString(false) + " " + gender;
                    List<BaseComponent> sent = Lists.newArrayList(TextComponent.fromLegacyText(desc));
                    boolean mod = p.hasPermission("archecore.mod") || p.hasPermission("archecore.admin");
                    PersonaWhoisEvent event = new PersonaWhoisEvent(a, p, sent, PersonaWhoisEvent.Query.BEACON_ICON, mod, false);
                    Bukkit.getPluginManager().callEvent(event);
                    
                    if(event.isCancelled()) continue; //This means the persona pip won't show up on the select screen and persona wont be selectable
                    
                    List<String> finalLore = event.getSent().stream().map(bc -> bc.toLegacyText()).collect(Collectors.toList());
                    finalLore.add( (i == current? ChatColor.DARK_GREEN + "Selected!": ChatColor.GREEN + "Click to select") );
                    ItemUtil.decorate(is, name, finalLore.toArray(new String[finalLore.size()]));
                }

				//Always do this (except if PersonaWhoisEvent is cancelled)
				inv.setItem(10+i, is);
			}


			p.openInventory(inv);
		} else {
			p.sendMessage(ChatColor.RED + "You may not yet access this.");
		}

	}
	
	private static int requiredSize(int highestUsed, int allowedPersonas, int maxPersonas, int firstFree) {
		int result = highestUsed+1;

        if (firstFree > highestUsed && firstFree < allowedPersonas) {
            //Need an extra slot for a white skull (= open slot)
            result++;
        } else if (firstFree > highestUsed && maxPersonas > result && result != 8) {
            //Need an extra slot to tell people they can buy MORE
            result++;
        }

        return result;
    }
	
}
