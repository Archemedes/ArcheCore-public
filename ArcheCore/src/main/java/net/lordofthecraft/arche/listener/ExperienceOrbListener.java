package net.lordofthecraft.arche.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class ExperienceOrbListener implements Listener{
	  @EventHandler(priority=EventPriority.HIGH)
      public void onBlockBreak(BlockBreakEvent e) {
              e.setExpToDrop(0);
      }
      @EventHandler(priority=EventPriority.HIGH)
      public void entityEvent(EntityDeathEvent e) {
              e.setDroppedExp(0);
      }
      //broke as shit
      @EventHandler(priority=EventPriority.HIGH)
      public void EntityEvent(CreatureSpawnEvent e) {
              if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
                      for (Entity t : e.getEntity().getNearbyEntities(2, 2, 2)) {
                    	  if (t.getType() == EntityType.EXPERIENCE_ORB) t.remove();
                      }
              }
      }
      //not broke
      @EventHandler(priority=EventPriority.HIGH)
      public void Event(PlayerFishEvent e) { //might need to change from Event because im stupid
              e.setExpToDrop(0);
      }
      @EventHandler(priority=EventPriority.HIGH)
      public void Event(FurnaceExtractEvent e) { //might need to change from Event because im stupid
              e.setExpToDrop(0);
      }
      @EventHandler(priority=EventPriority.NORMAL)
      public void PlayerEvent(PlayerExpChangeEvent e) {
              e.setAmount(0);
      }
}
