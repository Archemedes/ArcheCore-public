package net.lordofthecraft.arche.listener;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityMountEvent;

public class JistumaCollectionListener implements Listener{

	@EventHandler
	public void onDrinkingPotion (PlayerItemConsumeEvent e){
		ItemStack s = e.getItem();
		final Player p = e.getPlayer();
		if (s.getType()==Material.POTION){
			List<?> lore = s.getItemMeta().getLore();
			if (lore==null && !p.hasPermission("potions.mayuse")){
				e.setCancelled(true);
				p.setItemInHand(new ItemStack(Material.GLASS_BOTTLE,1));
			}
		}
		if (s.hasItemMeta() && p.isInsideVehicle()){
			String name = s.getItemMeta().getDisplayName();
			if (name.equalsIgnoreCase("Stoneskin Potion") && p.getVehicle().getType() == EntityType.HORSE){
				p.leaveVehicle();
				p.sendMessage(ChatColor.RED + "The potion you drank makes your movements sluggish, you fall off your horse");
			}
		}
	}
	
	@EventHandler
	public void PlayerMountingHorse(EntityMountEvent e){
		if (e.getEntity() instanceof Player && e.getMount() instanceof Horse){
			Player p = (Player) e.getEntity();
			if (p.hasPotionEffect(PotionEffectType.SLOW)){
				e.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You are too weak to mount the Horse. (You can't use Horses while having Slowness)");
			}
		}
	}
	
}
