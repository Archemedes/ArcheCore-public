package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.help.HelpDesk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HelpMenuListener implements Listener {
	private final HelpDesk helpdesk;
	private final Plugin plugin;
	
	public HelpMenuListener(Plugin plugin, HelpDesk helpdesk){
		this.plugin = plugin;
		this.helpdesk = helpdesk;
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onDrag(InventoryDragEvent e){
		Inventory inv = e.getInventory();
		if(inv.getTitle() == HelpDesk.HELP_HEADER){
			e.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onClick(InventoryClickEvent e){
		Inventory inv = e.getInventory();
		if(inv.getTitle() == HelpDesk.HELP_HEADER){
			e.setCancelled(true);
			
			int slot = e.getSlot();
			if(e.isLeftClick() && slot >= 0 && e.getRawSlot() == e.getSlot()){
				ItemStack i = inv.getItem(slot);
				if(i != null && i.hasItemMeta()){
					ItemMeta meta = i.getItemMeta();
					String topic = meta.getDisplayName().substring(2);
					final Player p = (Player) e.getWhoClicked();
					
					if(meta.hasLore()) 
						helpdesk.outputSkillHelp(topic, p); 
					else 
						helpdesk.outputHelp(topic,p);
					
					
					new BukkitRunnable(){
						@Override 
						public void run(){
							p.closeInventory();}
					}.runTask(plugin);
				}
			}
			
			
		}
	}
	
	
	
	
}
