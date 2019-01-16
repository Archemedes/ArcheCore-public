package net.lordofthecraft.arche.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.RaceBonusHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class RacialBonusListener implements Listener {
	//private final UUID random_uuid = UUID.randomUUID();
	private final Random rnd = new Random();
	private final ArchePersonaHandler handler;
	private final ArcheCore plugin;
	private final HashSet<Byte> IGNORE_BLOCKS;

	private final List<String> sneakAttempts = Lists.newArrayList();
	private final List<String> sneakers = Lists.newArrayList();

	public RacialBonusListener(ArcheCore plugin, ArchePersonaHandler handler){
		this.plugin = plugin;
		this.handler = handler;
		IGNORE_BLOCKS = new HashSet<>();
		initIgnore();
	}

	private void initIgnore(){
		IGNORE_BLOCKS.add((byte) 0);
		IGNORE_BLOCKS.add((byte) 6);
		IGNORE_BLOCKS.add((byte) 31);
		IGNORE_BLOCKS.add((byte) 37);
		IGNORE_BLOCKS.add((byte) 38);
		IGNORE_BLOCKS.add((byte) 39);
		IGNORE_BLOCKS.add((byte) 40);
		IGNORE_BLOCKS.add((byte) 175);
	}

	private boolean hasTogglePower(Race race){
		switch(race){
		case ORC:
		case DARK_ELF:
			return true;
		default:
			return false;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e){
		Player p = e.getPlayer();
		RaceBonusHandler.reset(p);

		Persona ps = handler.getPersona(p);
		if (ps != null) {
			RaceBonusHandler.apply(ps);
			if (handler.getRacespawns().containsKey(ps.getRace())) {
				e.setRespawnLocation(handler.getRacespawns().get(ps.getRace()));
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onHunger(FoodLevelChangeEvent e){
		Player p = (Player) e.getEntity();

		if(p.getFoodLevel() - e.getFoodLevel() == 1){ //Exactly 1 point drain, normal decay
			ArchePersona pers = handler.getPersona(p);
			if(pers != null){
				Race race = pers.getRace();

				if(race == Race.ORC || race == Race.GOBLIN){
					//Allows eating of rotten food
					p.removePotionEffect(PotionEffectType.HUNGER);

					/*if(race == Race.OLOG && p.isSprinting()){
						e.setFoodLevel(Math.max(0, e.getFoodLevel() - 3));
					}*/
				}  else {
					if(rnd.nextBoolean())
						e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority= EventPriority.MONITOR)
	public void onSneak(PlayerToggleSneakEvent e){
		//Dark Elf Speed Boost on Sneak
		final Player p = e.getPlayer();
		if(e.isSneaking()){
			ArchePersona pers = handler.getPersona(p);
			if(pers != null && hasTogglePower(pers.getRace())){
				if(!sneakers.contains(p.getName())){
					if(!sneakAttempts.contains(p.getName())){ //Shift pressed down once.
						sneakAttempts.add(p.getName());
						new BukkitRunnable(){@Override
							public void run(){sneakAttempts.remove(p.getName());}}.runTaskLater(plugin, 12);
					} else { //Shift pressed down twice in short time
						if (pers.getRace() == Race.DARK_ELF) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1, true), true);
						}else if(pers.getRace() == Race.ORC) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,100,1),true);
						}

						p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 0.8f, 2f);
						sneakers.add(p.getName());
						new BukkitRunnable(){@Override
							public void run(){
							sneakers.remove(p.getName());
							p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
						}}.runTaskLater(plugin, 600);
					}
				}
			}
		}
	}

	private boolean holdsGoldenWeapon(Entity e){
		if(e instanceof LivingEntity){
			LivingEntity le = (LivingEntity) e;
			ItemStack is = le.getEquipment().getItemInMainHand();
			if(is != null){
				switch(is.getType()){
				case GOLDEN_SWORD:
				case GOLDEN_AXE:
				case GOLDEN_PICKAXE:
				case GOLDEN_SHOVEL:
				case GOLDEN_HOE:
				case GOLD_INGOT:
				case GOLD_BLOCK:
				case GOLD_NUGGET:
					return true;
				default:
					break;
				}
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageByEntityEvent e){
		//Racial Resistance Bonuses
		if(e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			Persona pers = handler.getPersona(p);
			if(pers != null){
				Race r = pers.getRace();
				if (r == Race.HIGH_ELF) {
					e.setDamage(DamageModifier.MAGIC, e.getDamage(DamageModifier.MAGIC) * 0.7);
				}
			}
		}

		//Racial Damage bonuses
		if (plugin.areRacialDamageBonusesEnabled()) {
			//Wood Elf arrow bonus
			if(e.getDamager() instanceof Arrow){
				if(((Arrow) e.getDamager()).getShooter() instanceof Player){
					Player p =  (Player) ((Arrow) e.getDamager()).getShooter();
					Persona pers = handler.getPersona(p);
					if(pers != null && pers.getRace() == Race.WOOD_ELF){
						double dmg = e.getDamage();
						dmg *= 1.1;
						e.setDamage(dmg);
					}
				}
			} else if (e.getDamager() instanceof Player){
				Player p = (Player) e.getDamager();
				Persona pers = handler.getPersona(p);
				if(pers != null){
					double dmg = e.getDamage();
					Race r = pers.getRace();
					switch(r){
					//Magical Affinity. A portion of damage a high elf does is converted to Magic damage. Increases if using a gold weapon.
					case HIGH_ELF:
						if (holdsGoldenWeapon(e.getDamager())) {
							e.setDamage(dmg * 0.5);
							e.setDamage(DamageModifier.MAGIC, dmg * 0.5);
						} else {
							e.setDamage(dmg * 0.8);
							e.setDamage(DamageModifier.MAGIC, dmg * 0.20);
						}
						break;
					case ORC:
						if (e.getDamage() > 2) {
							e.setDamage(dmg + 2);
						}
						break;
					case HUMAN:
						if (e.getEntity() instanceof Player) {
							int count = 0;
							for (Entity ent : p.getNearbyEntities(10, 5, 10)) {
								if (ent instanceof Player) {
									ArchePersona x = handler.getPersona((Player) ent);
									if (x != null && x.getRace() == Race.HUMAN) {
										if (++count >= 5 && e.getDamage() > 2) {
											dmg *= 1.25;
											e.setDamage(dmg);
											break;
										}
									}
								}
							}
						}
						break;
					default:
						break;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent e){

		if(e.getEntity() instanceof Player){
			ArchePersona pers = handler.getPersona((Player) e.getEntity());
			DamageCause c = e.getCause();
			if(pers == null) return;

			final Race r = pers.getRace();
			if(c == DamageCause.MAGIC){
				double dmg = e.getDamage();
				if(r == Race.HIGH_ELF){
					dmg *= 0.7;
					e.setDamage(dmg);
				}
			}
		}
	}
}
