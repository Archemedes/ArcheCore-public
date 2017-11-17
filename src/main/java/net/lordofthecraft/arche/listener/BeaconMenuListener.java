package net.lordofthecraft.arche.listener;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheBeacon;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;

public class BeaconMenuListener implements Listener {
	private final Map<UUID, Long> switchCooldown = Maps.newConcurrentMap();
	
	private final ArcheCore plugin;
	private final HelpDesk helpdesk;
	private final ArchePersonaHandler handler;
	
	private final int switchCooldownMinutes;
	
	public BeaconMenuListener(ArcheCore plugin, int switchCooldownMins){
		this.plugin = plugin;
		helpdesk = HelpDesk.getInstance();
		handler = plugin.getPersonaHandler();
		
		this.switchCooldownMinutes = switchCooldownMins;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onClick(InventoryClickEvent e){
		Inventory inv = e.getInventory();
		if (ArcheBeacon.BEACON_HEADER == inv.getTitle()) {
			e.setCancelled(true);
			final Player p = (Player) e.getWhoClicked();
			final int s = e.getRawSlot();
			
			if(e.getClickedInventory() instanceof PlayerInventory 
					|| e.getAction() == InventoryAction.NOTHING 
					|| e.getCurrentItem() == null) 
				return;
			
			switch(s){
				
			case 0: 
				new BukkitRunnable(){
					@Override public void run(){ helpdesk.openHelpMenu(p);}
				}.runTask(plugin);
				break;
			case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8:
				ClickType ct = e.getClick();
				if(ct == ClickType.LEFT || ct == ClickType.SHIFT_LEFT ||
						ct == ClickType.RIGHT || ct == ClickType.SHIFT_RIGHT) {
					Persona ps = handler.getPersona(p);
					Validate.notNull(ps);
					try{ 
						ItemStack apply = ArcheBeacon.getFunction(s).apply(ct, ps);
						if(apply != null) inv.setItem(s, apply);
					} catch(Throwable t) {
						t.printStackTrace();
						p.sendMessage(ChatColor.RED + "There's an error with this button! Please report this occurrence.");
						Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->{
							p.closeInventory();
						});
						return;
					}
				}
				break;
			case 9:
				helpdesk.outputHelp("persona", p);
				new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
				break;
			default:
				ArchePersona[] prs = handler.getAllPersonas(p);
				if(prs == null){
					plugin.getLogger().severe(" [Event] Player walking around without registered Personas File!");
					return;
				}

				int count = 0;
				int current = -1;
				int firstFree = -1;
				for(int i = 0; i < prs.length; i++){
					if(prs[i] != null){
						count++;
						if(prs[i].isCurrent())
							current = i;
					} else if(firstFree < 0) {
						firstFree = i;
					}
				}

				int t = s - 10;

				//Prepare the object necessary for Persona creation.
				CreationDialog dialog = new CreationDialog();

				if(prs[t] == null){ //Clicked Persona does NOT exist
					if(count < handler.getAllowedPersonas(p) && t == firstFree){
						if (!ArcheCore.getControls().canCreatePersonas()) {
							p.sendMessage(ChatColor.RED + "Persona creation is disabled for this server, please go to the main server to create your persona.");
						} else {
							//Player may make new persona here, let's do so now.
							dialog.addPersona(p, t, true);
							new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
						}
					}
					return;
				} else { //Clicked Persona does exist
					if(e.isShiftClick()){ // Tried to modify persona somehow
						if(e.isLeftClick()){ //Create new
							if (!ArcheCore.getControls().canCreatePersonas()) {
								p.sendMessage(ChatColor.RED + "Persona creation is disabled for this server, please go to the main server to create your persona.");
								return;
							}
							dialog.addPersona(p, t, false);
							new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
						}else if(e.isRightClick()){ //Remove
							new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);

							if(count > 1 || p.hasPermission("archecore.exempt")){
								dialog.removePersona(prs[t]);
							} else {
								p.sendMessage(ChatColor.RED + "You may not remove your last Persona!");
							}
						}
					} else if(current != t){ //Tried to switch Personas
						if (((switchCooldown.containsKey(p.getUniqueId()) && System.currentTimeMillis() - switchCooldown.get(p.getUniqueId()) > 60*1000*switchCooldownMinutes)) 
								|| (switchCooldown.containsKey(p.getUniqueId()) &&  p.hasPermission("archecore.persona.quickswitch"))) {
							switchCooldown.remove(p.getUniqueId());
						}
						if (switchCooldown.containsKey(p.getUniqueId())) {
							p.sendMessage(ChatColor.RED+"You must wait another " + getMinutesFromMilli((switchCooldown.get(p.getUniqueId()) + 60*1000*switchCooldownMinutes) - System.currentTimeMillis())+" minutes before you can switch personas.");
						}else {
							if(!p.hasPermission("archecore.persona.quickswitch")){
								switchCooldown.put(p.getUniqueId(), System.currentTimeMillis());
							}

							final boolean suc = handler.switchPersona(p, t);
							if (suc) p.sendMessage(ChatColor.AQUA + "You are now Roleplaying as: " + ChatColor.YELLOW + "" + ChatColor.ITALIC + prs[t].getName());
						}
						new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
					}

				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDrag(InventoryDragEvent e){
		Inventory inv = e.getInventory();
		if (ArcheBeacon.BEACON_HEADER.equals(inv.getTitle())) {
			e.setCancelled(true);
		}
	}

	private int getMinutesFromMilli(long l) {
		return (int) (l / 1000) / 60;
	}
}
