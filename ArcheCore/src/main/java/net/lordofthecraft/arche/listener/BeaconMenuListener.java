package net.lordofthecraft.arche.listener;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheBeacon;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SkillTome;
import net.lordofthecraft.arche.executables.OpenEnderRunnable;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BeaconMenuListener implements Listener {
	private final List<String> switchCooldowns = Lists.newArrayList();
	
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
		if(ArcheBeacon.BEACON_HEADER.equals(inv.getTitle())){
			e.setCancelled(true);
			final Player p = (Player) e.getWhoClicked();
			final int s = e.getRawSlot();
			
			switch(s){
				
			case 1: 
				new BukkitRunnable(){
					@Override public void run(){ helpdesk.openHelpMenu(p);}
				}.runTask(plugin);
				break;
			case 2:
					new BukkitRunnable(){
						@Override
						public void run() {
							p.closeInventory();
							Persona pers = handler.getPersona(p);
							if (pers != null) {
								if (p.hasPermission("archecore.enderchest") && pers.getTimePlayed() >= 15000)
									OpenEnderRunnable.begin(pers);
								else
									p.sendMessage(ChatColor.RED + "You do not have access to your Ender Chest.");
							} else {
								p.sendMessage(ChatColor.RED + "You must be attuned to a persona to use your enderchest.");
							}
						}
					}.runTask(plugin);
				break;
			
			case 3:
				SkillTome.consumeTomes(p);
				new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
				break;
			case 4:
				helpdesk.outputHelp("persona", p);
				new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
				break;
			case 0: case 5: case 6: case 7: case 8:
				ArchePersona[] prs = handler.getAllPersonas(p);
				if(prs == null){
					plugin.getLogger().severe(" [Event] Player walking around without registered Personas File!");
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
				
				if(s == 0 && current >= 0){
					ArchePersona a = prs[current];
					a.setXPGain(!a.getXPGain());
					p.sendMessage(ChatColor.GRAY + "Toggled XP Gain for: " + a.getName());
					new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
				} else if (s > 4){
					int t = s - 5;
					
					//Prepare the object necessary for Persona creation.
					CreationDialog dialog = new CreationDialog();
					
					if(prs[t] == null){ //Clicked Persona does NOT exist
						if(count < handler.getAllowedPersonas(p)){
							//Player may make new persona here, let's do so now.
							dialog.addPersona(p, t);
							new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
						}
					} else { //Clicked Persona does exist
						if(e.isShiftClick()){ // Tried to modify persona somehow
							if(e.isLeftClick()){ //Create new
								dialog.addPersona(p, t);
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
							final String pname = p.getName();
							if(switchCooldowns.contains(pname)){
								p.sendMessage(ChatColor.RED + "You have a " + switchCooldownMinutes + " minute delay between switching Personas.");
							}else {
								if(!p.hasPermission("archecore.persona.quickswitch")){
									switchCooldowns.add(pname); //Cooldown if the Player does not have the appropriate permission
									new BukkitRunnable(){@Override public void run(){switchCooldowns.remove(pname);}}.runTaskLater(plugin, switchCooldownMinutes * 60 * 20);
								}
								
								final boolean suc = handler.switchPersona(p, t);
								/* Move this within the scope of switchPersona
								p.setHealth(p.getMaxHealth());
								p.setFoodLevel(20);
								for(PotionEffectType pet : PotionEffectType.values()) p.removePotionEffect(pet);
								*/
								
								if (suc) p.sendMessage(ChatColor.AQUA + "You are now Roleplaying as: " + ChatColor.YELLOW + "" + ChatColor.ITALIC + prs[t].getName());
							}
							new BukkitRunnable(){@Override public void run(){ p.closeInventory();}}.runTask(plugin);
						}
						
					}
					
				}
				
			default: break;	
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDrag(InventoryDragEvent e){
		Inventory inv = e.getInventory();
		if(ArcheBeacon.BEACON_HEADER.equals(inv.getTitle())){
			e.setCancelled(true);
		}
	}
}
